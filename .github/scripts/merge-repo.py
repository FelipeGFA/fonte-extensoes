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
with REMOTE_REPO.joinpath("index.min.json").open() as remote_index_file:
    remote_index = json.load(remote_index_file)

with LOCAL_REPO.joinpath("index.json").open() as local_index_file:
    local_index = json.load(local_index_file)

remote_modules = {pkg_to_module(item["pkg"]) for item in remote_index}
remote_by_pkg = {item["pkg"]: item for item in remote_index}

skipped_downgrades = set()
filtered_local_index = []

for item in local_index:
    remote_item = remote_by_pkg.get(item["pkg"])
    if remote_item is not None and item["code"] < remote_item["code"]:
        module = pkg_to_module(item["pkg"])
        skipped_downgrades.add(module)
        print(
            "Skipping downgrade for "
            f"{module}: local code {item['code']} < remote code {remote_item['code']}"
        )
        continue

    filtered_local_index.append(item)

local_index = filtered_local_index

# Extensions that still exist in repo branch but no longer exist in source tree
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
local_apks = {item["apk"] for item in local_index}
for file in LOCAL_REPO.joinpath("apk").glob("*"):
    if file.name not in local_apks:
        continue
    shutil.copy2(file, remote_apk_dir / file.name)

# Copy Icons
remote_icon_dir = REMOTE_REPO.joinpath("icon")
remote_icon_dir.mkdir(exist_ok=True)
local_icons = {f"{item['pkg']}.png" for item in local_index}
for file in LOCAL_REPO.joinpath("icon").glob("*"):
    if file.name not in local_icons:
        continue
    shutil.copy2(file, remote_icon_dir / file.name)


# Build final index
# 1. Keep remote items that are NOT in to_delete
index = [
    item for item in remote_index
    if not any([item["pkg"].endswith(f".{module}") for module in to_delete])
]

# 2. Add local items
index.extend(local_index)
index.sort(key=lambda x: x["pkg"])

# Save files


def extract_extension_lib(version: str) -> str:
    if match := re.search(r'(\d+)\.(\d+)', version):
        return f"{match.group(1)}.{match.group(2)}"

    raise ValueError(f"Version {version} doesn't contain MAJOR.MINOR")

index_pb = index_pb2.Index(
    name = "FelipeGFA",
    badgeLabel = "FGFA",
    signingKey = "9bf5754a79fd686cb77e6335a2a9e5351ad73ca824c27b09e7b3437f806fea77",
    contact=index_pb2.Contact(
        website="https://felipegfa.github.io",
        discord="https://discord.gg/QpyjwsWENq"
    ),
    extensionList=index_pb2.ExtensionList(
        extensions=[
            index_pb2.Extension(
                name=extension["name"].replace("Tachiyomi: ", ""),
                packageName=extension["pkg"],
                resources=index_pb2.Resources(
                    apkUrl=f"https://raw.githubusercontent.com/FelipeGFA/extensoes/refs/heads/repo/apk/{extension['apk']}",
                    iconUrl=f"https://raw.githubusercontent.com/FelipeGFA/extensoes/refs/heads/repo/icon/{extension['pkg']}.png",
                ),
                extensionLib=extract_extension_lib(extension["version"]),
                versionCode=extension["code"],
                versionName=extension["version"],
                contentWarning=int(extension.get("contentWarning", 2 if extension.get("nsfw", 0) == 1 else 0)) + 1,
                sources=[
                    index_pb2.Source(
                        id=int(source["id"]),
                        name=source["name"],
                        language=source["lang"],
                        homeUrl=source["baseUrl"],
                    )
                    for source in extension["sources"]
                ]
            )
            for extension in index
        ]
    )
)

with REMOTE_REPO.joinpath("index.json").open("w", encoding="utf-8") as index_file:
    index_file.write(json_format.MessageToJson(index_pb, always_print_fields_with_no_presence=False, preserving_proto_field_name=True))

with REMOTE_REPO.joinpath("index.pb").open("wb") as index_pb_file:
    index_pb_file.write(index_pb.SerializeToString())

with REMOTE_REPO.joinpath("index.pb.gz").open("wb") as index_pb_file:
    index_pb_file.write(gzip.compress(index_pb.SerializeToString()))

legacy_json_index = []
for entry in index:
    legacy_entry = entry.copy()
    cw = legacy_entry.get("contentWarning", 0)
    legacy_entry["nsfw"] = 1 if cw > 0 else 0
    legacy_entry.pop("libVersion", None)
    legacy_entry.pop("contentWarning", None)
    legacy_json_index.append(legacy_entry)

with REMOTE_REPO.joinpath("index.min.json").open("w", encoding="utf-8") as index_min_file:
    json.dump(legacy_json_index, index_min_file, ensure_ascii=False, separators=(",", ":"))

with REMOTE_REPO.joinpath("index.html").open("w", encoding="utf-8") as index_html_file:
    index_html_file.write('<!DOCTYPE html>\n<html>\n<head>\n<meta charset="UTF-8">\n<title>apks</title>\n</head>\n<body>\n<pre>\n')
    for entry in index:
        apk_escaped = 'apk/' + html.escape(entry["apk"])
        name_escaped = html.escape(entry["name"])
        index_html_file.write(f'<a href="{apk_escaped}">{name_escaped}</a>\n')
    index_html_file.write('</pre>\n</body>\n</html>\n')
