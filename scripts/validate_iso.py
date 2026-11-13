#!/usr/bin/env python3
"""Inspect a Limt-OS ISO before checksum generation and artifact upload."""
from __future__ import annotations

import re
import subprocess
import sys
from pathlib import Path


def run_xorriso(iso: Path, *args: str) -> str:
    command = ["xorriso", "-indev", str(iso), *args]
    result = subprocess.run(command, text=True, stdout=subprocess.PIPE,
                            stderr=subprocess.STDOUT, check=False)
    if result.returncode != 0:
        raise RuntimeError(
            f"xorriso failed ({result.returncode}): {' '.join(command)}\n"
            f"{result.stdout[-6000:]}"
        )
    return result.stdout


def main() -> int:
    if len(sys.argv) != 2:
        print("Usage: validate_iso.py PATH_TO_ISO", file=sys.stderr)
        return 2

    iso = Path(sys.argv[1]).resolve()
    if not iso.is_file() or iso.stat().st_size == 0:
        print(f"ERROR: ISO is missing or empty: {iso}", file=sys.stderr)
        return 1

    try:
        files = run_xorriso(iso, "-find", "/", "-type", "f", "-exec", "lsdl")
        dirs = run_xorriso(iso, "-find", "/", "-type", "d", "-exec", "lsdl")
        boot = run_xorriso(iso, "-report_el_torito", "plain")
    except RuntimeError as exc:
        print(f"ERROR: {exc}", file=sys.stderr)
        return 1

    inventory = (files + "\n" + dirs).lower()
    checks = {
        "Linux kernel": re.search(r"/live/vmlinuz[^\s]*", inventory) is not None,
        "Live initramfs": re.search(r"/live/initrd[^\s]*", inventory) is not None,
        "Compressed live filesystem": "/live/filesystem.squashfs" in inventory,
        "Limt-OS source dispatcher": "/usr/share/limt-os/src/limt/dispatcher.py" in inventory,
        "BIOS El Torito entry": "bios" in boot.lower(),
        "UEFI El Torito entry": "uefi" in boot.lower(),
    }

    print(f"ISO: {iso}")
    print(f"Size: {iso.stat().st_size:,} bytes")
    print(f"ISO inventory: {len(files.splitlines())} file listing lines; "
          f"{len(dirs.splitlines())} directory listing lines")
    for name, passed in checks.items():
        print(f"[{'OK' if passed else 'FAIL'}] {name}")

    if not all(checks.values()):
        print("\nERROR: ISO validation failed; it will not be checksummed or uploaded.",
              file=sys.stderr)
        print("\nBoot report:\n" + boot[-6000:], file=sys.stderr)
        return 1

    print("ISO validation passed.")
    return 0


if __name__ == "__main__":
    raise SystemExit(main())
