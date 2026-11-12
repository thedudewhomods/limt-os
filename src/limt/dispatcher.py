#!/usr/bin/env python3
"""Limt-OS package inspection and compatibility dispatcher.

APK/IPA are inspected as archives; they are not treated as native executables.
Windows programs are launched only when Wine is installed by the system owner.
"""
from __future__ import annotations

import argparse
import hashlib
import os
import shutil
import subprocess
import sys
import zipfile
from pathlib import Path

DATA_ROOT = Path(os.environ.get("XDG_DATA_HOME", Path.home() / ".local/share")) / "limt-os"
PREFIX_ROOT = DATA_ROOT / "wine-prefixes"
REPORT_ROOT = DATA_ROOT / "package-reports"


def sha256(path: Path) -> str:
    digest = hashlib.sha256()
    with path.open("rb") as stream:
        for chunk in iter(lambda: stream.read(1024 * 1024), b""):
            digest.update(chunk)
    return digest.hexdigest()


def inspect_archive(path: Path) -> tuple[str, list[str]]:
    try:
        with zipfile.ZipFile(path) as archive:
            names = archive.namelist()
    except (OSError, zipfile.BadZipFile):
        return "unknown", []
    if "AndroidManifest.xml" in names:
        return "apk", names
    if any(n.startswith("Payload/") and n.endswith(".app/Info.plist") for n in names):
        return "ipa", names
    if "BundleConfig.pb" in names or any(n.startswith("base/") for n in names):
        return "aab", names
    return "zip", names


def is_pe(path: Path) -> bool:
    try:
        with path.open("rb") as stream:
            if stream.read(2) != b"MZ":
                return False
            stream.seek(0x3C)
            offset_bytes = stream.read(4)
            if len(offset_bytes) != 4:
                return False
            pe_offset = int.from_bytes(offset_bytes, "little")
            if pe_offset > 16 * 1024 * 1024:
                return False
            stream.seek(pe_offset)
            return stream.read(4) == b"PE\0\0"
    except OSError:
        return False


def dispatch_windows(path: Path, digest: str) -> int:
    wine = shutil.which("wine")
    if not wine:
        print("Windows PE detected, but Wine is not installed.", file=sys.stderr)
        return 127
    prefix = PREFIX_ROOT / digest
    prefix.mkdir(parents=True, exist_ok=True)
    env = os.environ.copy()
    env["WINEPREFIX"] = str(prefix)
    env.setdefault("WINEDEBUG", "-all")
    print(f"Launching through Wine with a per-file prefix: {prefix}")
    return subprocess.run([wine, str(path)], env=env, check=False).returncode


def dispatch_apk(path: Path) -> int:
    waydroid = shutil.which("waydroid")
    if not waydroid:
        print("APK detected. Waydroid is not installed/configured; nothing was executed.", file=sys.stderr)
        return 127
    return subprocess.run([waydroid, "app", "install", str(path)], check=False).returncode


def inspect_ipa(path: Path, names: list[str], digest: str) -> int:
    REPORT_ROOT.mkdir(parents=True, exist_ok=True)
    plist_paths = [n for n in names if n.startswith("Payload/") and n.endswith(".app/Info.plist")]
    report = REPORT_ROOT / f"{digest}.txt"
    report.write_text(
        "Limt-OS IPA inspection report\n"
        "Execution: unsupported on Linux; archive was not installed or run.\n"
        f"SHA-256: {digest}\n"
        f"App metadata paths: {plist_paths}\n",
        encoding="utf-8",
    )
    print(f"IPA metadata inspected only. Report: {report}")
    return 0


def main(argv: list[str] | None = None) -> int:
    parser = argparse.ArgumentParser(description="Inspect packages and dispatch supported runtimes.")
    parser.add_argument("file", type=Path)
    args = parser.parse_args(argv)
    path = args.file.expanduser().resolve()
    if not path.is_file():
        parser.error(f"not a regular file: {path}")
    digest = sha256(path)
    if is_pe(path):
        return dispatch_windows(path, digest)
    kind, names = inspect_archive(path)
    if kind == "apk":
        return dispatch_apk(path)
    if kind == "ipa":
        return inspect_ipa(path, names, digest)
    if kind == "aab":
        print("Android App Bundle detected; convert it to APKs with bundletool before installing.", file=sys.stderr)
        return 2
    print(f"Unsupported package type: {kind}. No file was executed.", file=sys.stderr)
    return 2


if __name__ == "__main__":
    raise SystemExit(main())
