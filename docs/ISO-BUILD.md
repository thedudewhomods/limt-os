# Building the Limt-OS live ISO

The first target is a Debian 12 (Bookworm) amd64/x86-64 live ISO with XFCE, basic diagnostic tools, QEMU command-line utilities, and Limt-OS's source-based package dispatcher.

## Build

On Debian 12 or a compatible Linux build host:

```sh
sudo apt update
sudo apt install -y live-build debootstrap debian-archive-keyring xorriso squashfs-tools grub-efi-amd64-bin grub-pc-bin mtools dosfstools
sudo bash build/iso/build-iso.sh
```

The script explicitly pins the bootstrap/chroot/binary mirrors to Debian Bookworm. The archive-signing keyring must be installed; the missing keyring warning and Ubuntu trixie URL in earlier CI logs were caused by an unpinned mirror/keyring configuration. The subsequent `chroot /usr/bin/env` error was a cascade after debootstrap failed, not a separate root cause.

The image and checksum are written to `out/limt-os-amd64.iso` and `out/limt-os-amd64.iso.sha256`.

## Virtual machines

Use an x86-64 Linux guest profile in UTM or VMware, UEFI firmware, at least 2 vCPUs, 4 GB RAM, and a 25 GB virtual disk. Attach the ISO as a virtual CD/DVD. UTM emulation on ARM hosts may be slow. Host and virtual firmware compatibility still need testing.

## Runtime limits

- Windows PE: Wine is detected when installed; compatibility varies by application and CPU architecture.
- APK: Waydroid is optional and is not bundled in this first ISO.
- IPA: metadata inspection only; no iOS runtime or signing bypass is included.
- DeX-style adaptive desktop: future work; the first image uses XFCE.
- ARM64: separate future build target, not yet available.
