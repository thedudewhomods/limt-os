#!/usr/bin/env bash
set -euo pipefail

ROOT="$(cd "$(dirname "${BASH_SOURCE[0]}")/../.." && pwd)"
WORK="${ROOT}/build/iso/work"
OUT="${ROOT}/out"
ARCH="${ARCH:-amd64}"

if [[ "$ARCH" != "amd64" ]]; then
  echo "This builder currently supports amd64 only; ARM64 needs its own tested image." >&2
  exit 2
fi
if [[ "$(id -u)" -ne 0 ]]; then
  echo "Run as root: sudo bash build/iso/build-iso.sh" >&2
  exit 1
fi
command -v lb >/dev/null || { echo "Install live-build first." >&2; exit 127; }

mkdir -p "$WORK" "$OUT"
rm -f "$OUT/limt-os-amd64.iso" "$OUT/limt-os-amd64.iso.sha256"
cd "$WORK"
lb clean --purge || true

# Use Debian Bookworm live-build (the workflow runs inside debian:bookworm).
# Bootstrap uses the main mirror; the security mirror is configured for
# chroot and final-image package operations only.
lb config \
  --distribution bookworm \
  --parent-distribution bookworm \
  --architectures amd64 \
  --binary-images iso-hybrid \
  --archive-areas "main contrib non-free-firmware" \
  --bootloaders grub-efi \
  --debian-installer none \
  --apt-recommends true \
  --keyring-packages debian-archive-keyring \
  --mirror-bootstrap http://deb.debian.org/debian/ \
  --mirror-chroot http://deb.debian.org/debian/ \
  --mirror-chroot-security http://security.debian.org/debian-security/ \
  --mirror-binary http://deb.debian.org/debian/ \
  --mirror-binary-security http://security.debian.org/debian-security/ \
  --bootappend-live "boot=live components username=limt hostname=limt-os quiet"

mkdir -p config/package-lists config/includes.chroot/usr/local/bin config/includes.chroot/usr/local/lib/limt-os
cp "$ROOT/build/iso/package-list.chroot" config/package-lists/limt-os.list.chroot
install -m 0755 "$ROOT/scripts/omni-exec.py" config/includes.chroot/usr/local/bin/omni-exec
install -m 0755 "$ROOT/build/iso/limt-welcome" config/includes.chroot/usr/local/bin/limt-welcome
cp -R "$ROOT/src/limt" config/includes.chroot/usr/local/lib/limt-os/
printf '%s\n' '#!/usr/bin/env python3' 'import sys' 'sys.path.insert(0, "/usr/local/lib/limt-os")' 'from limt.dispatcher import main' 'raise SystemExit(main())' > config/includes.chroot/usr/local/bin/limt-dispatch
chmod 0755 config/includes.chroot/usr/local/bin/limt-dispatch

# Build in the live-build work directory, then place the finished image in
# the dedicated repository-root out/ directory.
lb build
ISO="$(find . -maxdepth 1 -type f -name '*.iso' -print -quit)"
if [[ -z "$ISO" ]]; then
  echo "live-build completed but did not produce an ISO." >&2
  exit 1
fi

install -m 0644 "$ISO" "$OUT/limt-os-amd64.iso"
(
  cd "$OUT"
  sha256sum limt-os-amd64.iso > limt-os-amd64.iso.sha256
  sha256sum --check limt-os-amd64.iso.sha256
)

echo "Created: $OUT/limt-os-amd64.iso"
echo "Checksum: $OUT/limt-os-amd64.iso.sha256"
