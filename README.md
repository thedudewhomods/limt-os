# Limt-OS

Limt-OS is a bootable Linux operating-system project. Its current target is an **x86_64 Debian Live ISO**, not an Android or iOS application.

## Current OS preview

The ISO is built on Debian Trixie with:

- Linux kernel and Debian Live boot environment
- XFCE desktop with LightDM
- NetworkManager for network management
- A small Limt-OS Settings launcher for display, network, keyboard, window-manager settings, and About
- QEMU system-emulation tools and SPICE guest-agent support
- VMware open-vm-tools for supported virtual-machine guests
- Basic command-line utilities and Python tooling

This is an early live-image preview. It is not yet a custom kernel or a fully independent distribution, and it does not support every physical machine.

## Build the ISO

On a Debian or Ubuntu build host:

```sh
sudo apt-get update
sudo apt-get install -y live-build debootstrap squashfs-tools xorriso \
  grub-pc-bin grub-efi-amd64-bin mtools

sudo lb config \
  --distribution trixie \
  --architectures amd64 \
  --binary-images iso-hybrid \
  --archive-areas "main contrib non-free-firmware" \
  --bootappend-live "boot=live components username=limt hostname=limt-os"

sudo lb build
```

The generated ISO is normally named `live-image-amd64.hybrid.iso` in the repository root. GitHub Actions can also build it: run **Build Limt-OS x86_64 ISO** from the Actions tab. The workflow uploads the ISO and its SHA-256 checksum as an artifact.

## Repository layout

- `.github/workflows/build-iso.yml`: automated ISO build
- `build/iso/README.md`: build and virtual-machine notes
- `config/package-lists/limt-os.list.chroot`: packages installed in the live image
- `config/hooks/live/0100-limt-desktop.hook.chroot`: Limt-OS settings launcher setup
- `docs/ISO-ROADMAP.md`: planned runtime, desktop, hardware, and ARM64 work
- `THIRD_PARTY_NOTICES.md`: upstream projects and licensing notes
- `backend/`: separate experimental AI helper; not required to boot or build the ISO

## Roadmap — not implemented yet

- Wine-based Windows application compatibility
- Android runtime/container integration (requires Linux kernel and graphics support)
- Package metadata inspection for PE, APK, AAB, and IPA files
- Native process inspector and developer console
- A Limt-OS desktop shell and hardware-specific configuration
- A separate ARM64 image

These are plans, not current capabilities. An APK or IPA is not directly executable by Linux; each needs an appropriate runtime. The iOS-only projects in the research notes are not drop-in Linux runtimes.

## Virtual-machine testing

The x86_64 ISO can be tested in UTM or another x86_64-capable hypervisor. On Apple Silicon, x86_64 requires emulation and will be slower than an ARM64 guest. An ARM64 Limt-OS image is a separate future target.

The live image may use the default account `limt` with password `live`, depending on the Debian Live version. Change this before any public or distributable release.
