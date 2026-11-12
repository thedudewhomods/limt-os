#!/usr/bin/env bash
set -euo pipefail

ROOT="$(cd "$(dirname "${BASH_SOURCE[0]}")/../.." && pwd)"
WORK="${ROOT}/build/iso/work"
OUT="${ROOT}/dist"
ARCH="${ARCH:-amd64}"

if [[ "$ARCH" != "amd64" ]]; then
  echo "This first bootable ISO builder supports amd64 only. ARM64 needs a separate tested boot path." >&2
  exit 2
fi

command -v lb >/dev/null || { echo "live-build is required (install the Debian live-build package)." >&2; exit 127; }
mkdir -p "$WORK" "$OUT"
cd "$WORK"

lb clean --purge || true
lb config \
  --distribution bookworm \
  --architectures amd64 \
  --binary-images iso-hybrid \
  --archive-areas "main contrib non-free-firmware" \
  --bootloaders "grub-efi" \
  --debian-installer none \
  --apt-recommends true \
  --bootappend-live "boot=live components username=limt hostname=limt-os quiet"

mkdir -p config/package-lists config/includes.chroot/usr/local/bin
cp "$ROOT/build/iso/package-list.chroot" config/package-lists/limt-os.list.chroot
install -m 0755 "$ROOT/scripts/omni-exec.py" config/includes.chroot/usr/local/bin/omni-exec
install -m 0755 "$ROOT/build/iso/limt-welcome" config/includes.chroot/usr/local/bin/limt-welcome

sudo lb build

mkdir -p "$OUT"
ISO="$(find . -maxdepth 1 -type f -name '*.iso' -print -quit)"
if [[ -z "$ISO" ]]; then
  echo "live-build completed but did not produce an ISO." >&2
  exit 1
fi
cp "$ISO" "$OUT/limt-os-amd64.iso"
sha256sum "$OUT/limt-os-amd64.iso" > "$OUT/limt-os-amd64.iso.sha256"
echo "Created: $OUT/limt-os-amd64.iso"
