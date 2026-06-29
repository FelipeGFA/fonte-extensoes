import gzip
import html
import sys
import json
from pathlib import Path
import shutil
import re
import index_pb2
from google.protobuf import json_format

REMOTE_REPO: Path = Path.cwd()
LOCAL_REPO: Path = REMOTE_REPO.parent.joinpath("main/repo")

LANGUAGE_REGEX = re.compile(r"tachiyomi-([^.]+)")

to_delete: list[str] = json.loads(sys.argv[1])

def pkg_to_module(pkg: str) -> str:
    return pkg.replace("eu.kanade.tachiyomi.extension.", "")

def get_existing_modules() -> set[str]:
    modules = set()
    src_dir = REMOTE_REPO.parent.joinpath("main/src")
    if not src_dir.is_dir():
        return modules
    for lang_dir in src_dir.iterdir():
        if not lang_dir.is_dir():
            continue
        for ext_dir in lang_dir.iterdir():
            if ext_dir.is_dir():
                modules.add(f"{lang_dir.name}.{ext_dir.name}")
    return modules

existing_modules = get_existing_modules()

# Load indexes
with REMOTE_REPO.joinpath("index.json").open() as f:
    remote_proto = json_format.Parse(f.read(), index_pb2.Index())
with LOCAL_REPO.joinpath("index.json").open() as f:
    local_proto = json_format.Parse(f.read(), index_pb2.ExtensionList())

remote_modules = {pkg_to_module(ext.packageName) for ext in remote_proto.extensionList.extensions}
remote_by_pkg = {ext.packageName: ext for ext in remote_proto.extensionList.extensions}

skipped_downgrades = set()
filtered_local_extensions = []

for ext in local_proto.extensions:
    remote_ext = remote_by_pkg.get(ext.packageName)
    if remote_ext is not None and ext.versionCode < remote_ext.versionCode:
        module = pkg_to_module(ext.packageName)
        skipped_downgrades.add(module)
        print(f"Skipping downgrade for {module}: local code {ext.versionCode} < remote code {remote_ext.versionCode}")
        continue
    filtered_local_extensions.append(ext)

# Stale modules detection
stale_modules = remote_modules - existing_modules
if stale_modules:
    print(f"Removing stale modules not found in source tree: {sorted(stale_modules)}")

to_delete = list((set(to_delete) | stale_modules) - skipped_downgrades)

# Delete old files
for module in to_delete:
    apk_name = f"tachiyomi-{module}-v*.*.*.apk"
    icon_name = f"eu.kanade.tachiyomi.extension.{module}.png"
    for file in REMOTE_REPO.joinpath("apk").glob(apk_name):
        print(f"Removing old APK: {file.name}")
        file.unlink(missing_ok=True)
    for file in REMOTE_REPO.joinpath("icon").glob(icon_name):
        print(f"Removing old Icon: {file.name}")
        file.unlink(missing_ok=True)

# Copy APKs
remote_apk_dir = REMOTE_REPO.joinpath("apk")
remote_apk_dir.mkdir(exist_ok=True)
local_apks = {ext.resources.apkUrl.split("/")[-1] for ext in filtered_local_extensions}
for file in LOCAL_REPO.joinpath("apk").glob("*"):
    if file.name not in local_apks:
        continue
    shutil.copy2(file, remote_apk_dir / file.name)

# Copy Icons
remote_icon_dir = REMOTE_REPO.joinpath("icon")
remote_icon_dir.mkdir(exist_ok=True)
local_icons = {f"{ext.packageName}.png" for ext in filtered_local_extensions}
for file in LOCAL_REPO.joinpath("icon").glob("*"):
    if file.name not in local_icons:
        continue
    shutil.copy2(file, remote_icon_dir / file.name)

# Build final index
all_extensions = [
    ext for ext in remote_proto.extensionList.extensions
    if not any(ext.packageName.endswith(f".{module}") for module in to_delete)
]
all_extensions.extend(filtered_local_extensions)
all_extensions.sort(key=lambda ext: ext.packageName)

index = index_pb2.Index(
    name="FelipeGFA",
    badgeLabel="FGFA",
    signingKey="9bf5754a79fd686cb77e6335a2a9e5351ad73ca824c27b09e7b3437f806fea77",
    contact=index_pb2.Contact(
        website="https://felipegfa.github.io", discord="https://discord.gg/QpyjwsWENq"
    ),
    extensionList=index_pb2.ExtensionList(extensions=all_extensions),
)

with REMOTE_REPO.joinpath("index.json").open("w", encoding="utf-8") as f:
    f.write(
        json_format.MessageToJson(
            index,
            always_print_fields_with_no_presence=False,
            preserving_proto_field_name=True,
        )
    )

with REMOTE_REPO.joinpath("index.pb").open("wb") as f:
    f.write(index.SerializeToString())

with REMOTE_REPO.joinpath("index.pb.gz").open("wb") as f:
    f.write(gzip.compress(index.SerializeToString()))


def get_legacy_lang(ext) -> str:
    apk_filename = ext.resources.apkUrl.split("/")[-1]
    lang = LANGUAGE_REGEX.search(apk_filename).group(1)
    if len(ext.sources) == 1:
        source_language = ext.sources[0].language
        if (
            source_language != lang
            and source_language not in {"all", "other"}
            and lang not in {"all", "other"}
        ):
            lang = source_language
    return lang


legacy_json_index = [
    {
        "name": f"Tachiyomi: {ext.name}",
        "pkg": ext.packageName,
        "apk": ext.resources.apkUrl.split("/")[-1],
        "lang": get_legacy_lang(ext),
        "code": ext.versionCode,
        "version": ext.versionName,
        "nsfw": 1 if ext.contentWarning > 2 else 0,
        "sources": [
            {
                "name": source.name,
                "lang": source.language,
                "id": str(source.id),
                "baseUrl": source.homeUrl,
            }
            for source in ext.sources
        ],
    }
    for ext in all_extensions
]

with REMOTE_REPO.joinpath("index.min.json").open("w", encoding="utf-8") as f:
    json.dump(legacy_json_index, f, ensure_ascii=False, separators=(",", ":"))

with REMOTE_REPO.joinpath("index.html").open("w", encoding="utf-8") as f:
    f.write(
        '<!DOCTYPE html>\n<html>\n<head>\n<meta charset="UTF-8">\n<title>apks</title>\n</head>\n<body>\n<pre>\n'
    )
    for ext in all_extensions:
        apk_escaped = 'apk/' + html.escape(ext.resources.apkUrl.split("/")[-1])
        name_escaped = html.escape(f"Tachiyomi: {ext.name}")
        f.write(f'<a href="{apk_escaped}">{name_escaped}</a>\n')
    f.write("</pre>\n</body>\n</html>\n")
