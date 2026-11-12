#!/usr/bin/env python3
"""OmniOS safe application dispatcher foundation."""
from __future__ import annotations

import argparse
import hashlib
import os
import shutil
import subprocess
import sys
import zipfile
from pathlib import Path

HOME = Path.home()
DATA_ROOT = Path(os.environ.get("XDG_DATA_HOME", HOME / ".local/share")) / "omni"
PREFIX_ROOT = DATA_ROOT / "prefixes"
IOS_ROOT = DATA_ROOT / "ios-inspection"


def run(command: list[str]) -> int:
    try:
        return subprocess.run(command, check=False).returncode
    except FileNotFoundError:
        print(f"Required program not found: {command[0]}", file=sys.stderr)
        return 127


def package_id(path: Path) -> str:
    digest = hashlib.sha256()
    with path.open("rb") as stream:
        for block in iter(lambda: stream.read(1024 * 1024), b""):
            digest.update(block)
    return digest.hexdigest()


def inspect_zip(path: Path) -> tuple[str, list[str]]:
    try:
        with zipfile.ZipFile(path) as archive:
            names = archive.namelist()
            if "AndroidManifest.xml" in names:
                return "apk", names
            if any(n.startswith("Payload/") and n.endswith(".app/Info.plist") for n in names):
                return "ipa", names
            if "BundleConfig.pb" in names or any(n.startswith("base/") for n in names):
                return "aab", names
    except (zipfile.BadZipFile, OSError):
        pass
    return "unknown", []


def dispatch_windows(path: Path, digest: str) -> int:
    wine = shutil.which("wine")
    if not wine:
        print("Wine is not installed. Install Wine/Proton using your distribution package manager.")
        return 127
    prefix = PREFIX_ROOT / digest
    prefix.mkdir(parents=True, exist_ok=True)
    env = os.environ.copy()
    env["WINEPREFIX"] = str(prefix)
    env.setdefault("WINEDEBUG", "-all")
    command = [wine, "msiexec", "/i", str(path)] if path.suffix.lower() == ".msi" else [wine, str(path)]
    print(f"Launching Windows application with isolated Wine prefix: {prefix}")
    return subprocess.run(command, env=env, check=False).returncode


def dispatch_android(path: Path) -> int:
    waydroid = shutil.which("waydroid")
    if not waydroid:
        print("Waydroid is not installed. Install and initialize a compatible Waydroid image first.")
        return 127
    print("Installing APK into the configured Waydroid container.")
    return run([waydroid, "app", "install", str(path)])


def inspect_ios(path: Path, names: list[str]) -> int:
    IOS_ROOT.mkdir(parents=True, exist_ok=True)
    digest = package_id(path)
    report = IOS_ROOT / f"{digest}.txt"
    plist_paths = [n for n in names if n.startswith("Payload/") and n.endswith(".app/Info.plist")]
    report.write_text(
        "IPA metadata inspection only; this file was not installed or executed.\n"
        f"SHA-256: {digest}\nInfo.plist entries: {plist_paths}\n",
        encoding="utf-8",
    )
    print("iOS IPA detected. Linux execution is not implemented; no signing or DRM checks were bypassed.")
    print(f"Inspection report: {report}")
    return 0


def main() -> int:
    parser = argparse.ArgumentParser(description="Inspect and dispatch Windows, Android and iOS packages.")
    parser.add_argument("file", type=Path)
    args = parser.parse_args()
    path = args.file.expanduser().resolve()
    if not path.is_file():
        print(f"Not a regular file: {path}", file=sys.stderr)
        return 2

    digest = package_id(path)
    with path.open("rb") as stream:
        magic = stream.read(2)

    if magic == b"MZ":
        return dispatch_windows(path, digest)

    kind, names = inspect_zip(path)
    if kind == "apk":
        return dispatch_android(path)
    if kind == "aab":
        print("Android App Bundle detected. Convert it to APKs with Google's bundletool, then install the APK set in Waydroid.")
        return 2
    if kind == "ipa":
        return inspect_ios(path, names)

    print("Unsupported or unrecognized package. No file was executed.")
    return 2


if __name__ == "__main__":
    raise SystemExit(main())
