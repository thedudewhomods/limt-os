# Building the Limt-OS live ISO

## Current target

The first image is a **Debian 12 (Bookworm), amd64/x86-64 live ISO** with an XFCE desktop, basic diagnostics, QEMU user tools, and the existing OmniOS package dispatcher. It is intended for x86-64 UTM and VMware guests using a compatible BIOS/UEFI virtual machine profile.

It is not a universal-CPU image. ARM64 is a separate build target and is not claimed to boot until its own image and VM tests pass.

## Build locally

On a Debian 12 or compatible Linux build host:

```sh
sudo apt update
sudo apt install -y live-build debootstrap xorriso squashfs-tools grub-efi-amd64-bin grub-pc-bin mtools dosfstools
./build/iso/build-iso.sh
```

The result is written to `dist/limt-os-amd64.iso` with a SHA-256 file.

## GitHub Actions

The `Limt-OS ISO` workflow builds the ISO and uploads it as a workflow artifact. A successful workflow run is required before calling the image boot-tested. The artifact is not automatically a GitHub Release.

## UTM and VMware

Create a new Linux virtual machine with:
- Architecture: x86-64 / x86_64
- Firmware: UEFI (try BIOS only if the VM's firmware setup requires it)
- 2 or more virtual CPUs
- 4 GB RAM recommended
- 25 GB virtual disk
- Attach the ISO as a virtual CD/DVD and boot from it

UTM's emulated x86-64 mode on an ARM host can be much slower than native virtualization. VMware support depends on the host product and CPU virtualization support. This project does not guarantee booting on every host.

## Runtime support status

- Windows PE: the dispatcher recognizes PE-style MZ files and uses Wine only when Wine is installed. Application compatibility is not guaranteed.
- APK: the dispatcher inspects the ZIP contents and can pass a valid APK to an already configured Waydroid installation. Waydroid is not included in this initial ISO.
- IPA: metadata inspection only. Linux cannot run arbitrary iOS apps by changing Mach-O headers or loading them as shared libraries.
- QEMU: command-line QEMU tools are included; guest acceleration depends on host CPU and VM configuration.
- DeX-style adaptive desktop: planned. The initial image uses XFCE and does not claim Samsung DeX parity.

## Reproducibility and testing

Pin package versions and base-image snapshots before treating builds as reproducible. Boot-test each architecture in UTM and VMware separately, then record the host, firmware, virtual CPU model, RAM, and result. A CI artifact existing is not proof that every VM boots.
