# Limt-OS live ISO (x86_64)

This is the first bootable Limt-OS preview target. It uses Debian Live with XFCE,
LightDM, NetworkManager, QEMU system tools, SPICE agent support and VMware
open-vm-tools.

## Build locally (Debian/Ubuntu build host)

Install the build dependencies:

```sh
sudo apt-get update
sudo apt-get install -y live-build debootstrap squashfs-tools xorriso \
  grub-pc-bin grub-efi-amd64-bin mtools
```

From the repository root:

```sh
sudo lb config \
  --distribution trixie \
  --architectures amd64 \
  --binary-images iso-hybrid \
  --archive-areas "main contrib non-free-firmware" \
  --bootappend-live "boot=live components username=limt hostname=limt-os"
sudo lb build
```

The generated image is normally named `live-image-amd64.hybrid.iso` in the
repository root. Exact output naming can vary with the live-build version.

## CI build

Run **Build Limt-OS x86_64 ISO** from GitHub Actions. The resulting ISO is
uploaded as the `limt-os-x86_64-iso` workflow artifact.

## Virtual machine settings

- **VMware:** create a Linux/Debian 64-bit VM, attach the ISO, use a virtual
  SATA or SCSI disk, and enable UEFI or BIOS boot.
- **UTM on Intel Macs:** use an x86_64 virtual machine with QEMU virtualization
  where available.
- **UTM on Apple Silicon:** select x86_64 emulation. It is slower than native
  ARM64 virtualization. A separate ARM64 image is a future target.

The image is a preview, not a claim of support for every CPU or physical
machine. It targets x86_64 and common virtual hardware. GPU acceleration,
Wi-Fi, audio and suspend behavior depend on the hypervisor and host.

The default Debian Live account is `limt` with password `live` when the
live-config username option is honored by the selected Debian Live release.
Change this before any public/distributable release.
