# Buildroot starting point

This directory documents the initial route to a reproducible OmniOS live image. The current branch does not yet contain a complete Buildroot external tree or a bootable image.

## Host packages (Debian/Ubuntu)

    sudo apt-get update
    sudo apt-get install -y build-essential git cpio rsync unzip bc wget libncurses-dev file python3 python3-pip perl xz-utils

## Fetch a pinned Buildroot release

Choose and record a stable Buildroot release before production builds. Then:

    git clone https://github.com/buildroot/buildroot.git
    cd buildroot
    git checkout 2025.02.1
    make qemu_x86_64_defconfig
    make menuconfig
    make -j"$(nproc)"

This is a smoke-test image for QEMU, not the OmniOS desktop. Add a reviewed rootfs overlay, kernel config fragment, systemd or another init system, Wayland compositor, Mesa, PipeWire and runtime packages in later milestones. Do not claim hardware support until tested on the target machine.

## Runtime package plan

- Base: systemd, dbus, udev, bash, coreutils, util-linux, sudo, e2fsprogs, btrfs-progs.
- Desktop: Wayland, wlroots-based compositor, XWayland, Mesa, PipeWire, WirePlumber.
- Windows: Wine and optionally DXVK/VKD3D-Proton; package availability and licensing vary.
- Android: Waydroid requires a suitable host kernel and Android image; it is not a simple Buildroot package toggle.
- Virtualization: QEMU; KVM only when the host CPU and kernel support it.
- Diagnostics: Python 3 and the OmniOS dispatcher.

Review upstream licenses and redistribution terms before bundling third-party components.
