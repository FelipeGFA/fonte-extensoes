import gzip
import html
import json
import os
import re
import subprocess
import sys
from functools import cache
from pathlib import Path
from zipfile import ZipFile

from google.protobuf import json_format

import index_pb2

APPLICATION_ICON_320_REGEX = re.compile(r"^application-icon-320:'([^']+)'", re.MULTILINE)
LANGUAGE_REGEX = re.compile(r"tachiyomi-([^.]+)")


@cache
def aapt() -> Path:
    *_, build_tools = (Path(os.environ["ANDROID_HOME"]) / "build-tools").iterdir()
    return build_tools / "aapt"

def pkg_to_module(pkg: str) -> str:
    return pkg.replace("eu.kanade.tachiyomi.extension.", "")

# Artifacts downloaded from the build jobs: one APK per extension plus the source metadata JSON
# emitted by each assembleRelease.
ARTIFACTS_DIR = Path.home() / "apk-artifacts"

# The checked-out `repo` branch we publish into (the working directory).
REPO_DIR = Path.cwd()
REPO_APK_DIR = REPO_DIR / "apk"
REPO_ICON_DIR = REPO_DIR / "icon"
REPO_APK_DIR.mkdir(parents=True, exist_ok=True)
REPO_ICON_DIR.mkdir(parents=True, exist_ok=True)

APK_BASE_URL = "https://raw.githubusercontent.com/FelipeGFA/extensoes/refs/heads/repo/apk"
ICON_BASE_URL = "https://raw.githubusercontent.com/FelipeGFA/extensoes/refs/heads/repo/icon"

def get_existing_modules() -> set[str]:
    modules = set()
    src_dir = REPO_DIR.parent.joinpath("main/src")
    if not src_dir.is_dir():
        return modules
    for lang_dir in src_dir.iterdir():
        if not lang_dir.is_dir():
            continue
        for ext_dir in lang_dir.iterdir():
            if ext_dir.is_dir():
                modules.add(f"{lang_dir.name}.{ext_dir.name}")
    return modules

to_delete: list[str] = json.loads(sys.argv[1])

# Merge with the already-published index, dropping the deleted/rebuilt modules.
with REPO_DIR.joinpath("index.json").open() as f:
    remote_proto = json_format.Parse(f.read(), index_pb2.Index())

remote_modules = {pkg_to_module(ext.packageName) for ext in remote_proto.extensionList.extensions}
remote_by_pkg = {ext.packageName: ext for ext in remote_proto.extensionList.extensions}
existing_modules = get_existing_modules()

new_extensions: list[index_pb2.Extension] = []
rebuilt_modules = set()  # Track which modules were actually rebuilt successfully
skipped_downgrades = set()

for info_file in ARTIFACTS_DIR.glob("**/keiyoushi-source-info.json"):
    with info_file.open(encoding="utf-8") as f:
        info = json.load(f)
    package_name = info["packageName"]
    version_code = info["versionCode"]
    module = pkg_to_module(package_name)

    remote_ext = remote_by_pkg.get(package_name)
    if remote_ext is not None and version_code < remote_ext.versionCode:
        skipped_downgrades.add(module)
        print(f"Skipping downgrade for {module}: local code {version_code} < remote code {remote_ext.versionCode}")
        continue

    apk = next((info_file.parent / "outputs/apk/release").glob("*.apk"))

    apk_name = apk.name.replace("-release.apk", ".apk")
    (REPO_APK_DIR / apk_name).write_bytes(apk.read_bytes())

    badging = subprocess.check_output(
        [aapt(), "dump", "--include-meta-data", "badging", apk]
    ).decode()
    application_icon = APPLICATION_ICON_320_REGEX.search(badging).group(1)
    with (
        ZipFile(apk) as z,
        z.open(application_icon) as i,
        (REPO_ICON_DIR / f"{package_name}.png").open("wb") as f,
    ):
        f.write(i.read())

    rebuilt_modules.add(module)
    new_extensions.append(
        index_pb2.Extension(
            name=info["name"],
            packageName=package_name,
            resources=index_pb2.Resources(
                apkUrl=f"{APK_BASE_URL}/{apk_name}",
                iconUrl=f"{ICON_BASE_URL}/{package_name}.png",
            ),
            extensionLib=info["extensionLib"],
            versionCode=info["versionCode"],
            versionName=info["versionName"],
            contentWarning=info["contentWarning"],
            sources=[
                index_pb2.Source(
                    id=int(source["id"]),
                    name=source["name"],
                    language=source["lang"],
                    homeUrl=source["baseUrl"],
                    mirrorUrls=source.get("mirrorUrls", []),
                )
                for source in info["sources"]
            ],
        )
    )

# Detect modules that exist in the remote index but have been genuinely deleted
# from the source tree (not just modules that failed to build).
genuinely_deleted = remote_modules - existing_modules
if genuinely_deleted:
    print(f"Removing genuinely deleted modules not found in source tree: {sorted(genuinely_deleted)}")

# SAFETY: Only remove from the index modules that were:
#   1) Successfully rebuilt (have a new APK to replace the old one), OR
#   2) Genuinely deleted from the source tree (no longer exist in src/)
# This prevents wiping the entire index when core files change and trigger a
# full rebuild that only partially succeeds (e.g., timeout, compile errors).
safe_to_delete = (rebuilt_modules | genuinely_deleted) - skipped_downgrades

print(f"Modules rebuilt: {len(rebuilt_modules)}, genuinely deleted: {len(genuinely_deleted)}, safe to delete from old index: {len(safe_to_delete)}")

# Drop apks/icons for modules that were deleted or rebuilt (rebuilt ones are re-added below).
for module in safe_to_delete:
    for file in REPO_APK_DIR.glob(f"tachiyomi-{module}-v*.*.*.apk"):
        print(f"removing {file.name}")
        file.unlink(missing_ok=True)
    for file in REPO_ICON_DIR.glob(f"eu.kanade.tachiyomi.extension.{module}.png"):
        print(f"removing {file.name}")
        file.unlink(missing_ok=True)


all_extensions = [
    ext
    for ext in remote_proto.extensionList.extensions
    if not any(ext.packageName.endswith(f".{module}") for module in safe_to_delete)
]
all_extensions.extend(new_extensions)
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

with REPO_DIR.joinpath("index.json").open("w", encoding="utf-8") as f:
    f.write(
        json_format.MessageToJson(
            index,
            always_print_fields_with_no_presence=False,
            preserving_proto_field_name=True,
        )
    )

with REPO_DIR.joinpath("index.pb").open("wb") as f:
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

with REPO_DIR.joinpath("index.min.json").open("w", encoding="utf-8") as f:
    json.dump(legacy_json_index, f, ensure_ascii=False, separators=(",", ":"))

with REPO_DIR.joinpath("index.html").open("w", encoding="utf-8") as f:
    f.write(
        '<!DOCTYPE html>\n<html>\n<head>\n<meta charset="UTF-8">\n<title>apks</title>\n</head>\n<body>\n<pre>\n'
    )
    for ext in all_extensions:
        apk_escaped = html.escape(ext.resources.apkUrl.split("/")[-1])
        name_escaped = html.escape(f"Tachiyomi: {ext.name}")
        f.write(f'<a href="apk/{apk_escaped}">{name_escaped}</a>\n')
    f.write("</pre>\n</body>\n</html>\n")
