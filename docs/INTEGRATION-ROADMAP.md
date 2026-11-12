# Limt-OS runtime integration roadmap

## Stage 1 — bootable foundation
- Debian live-build based amd64 ISO.
- XFCE desktop, system tools, package dispatcher.
- CI artifact and SHA-256 checksum.
- UTM and VMware boot testing.

## Stage 2 — Windows compatibility
- Integrate Wine through distribution packages.
- Add a tested Box64/FEX path for supported ARM64 systems.
- Add per-application prefixes, graphics capability checks, and clear unsupported-app reporting.
- Keep guest applications unprivileged.

## Stage 3 — Android compatibility
- Add a compatible Binder-enabled kernel and supported Waydroid userspace.
- Detect APK package metadata and install only inside the Android runtime.
- Do not treat APK ZIP signatures as executable magic bytes.

## Stage 4 — virtualization
- Add QEMU machine profiles for x86-64 and ARM64.
- Detect KVM availability and fall back to TCG only when appropriate.
- Add separate UTM and VMware guest profiles and test them independently.

## Stage 5 — adaptive desktop and Settings
- Implement display hotplug detection and responsive desktop layouts.
- Add a Limt-OS Settings application for display, desktop, compatibility, virtualization, developer tools, and updates.
- Initial XFCE session remains the fallback until the adaptive shell is tested.

## Stage 6 — iOS research
- IPA archive inspection, app metadata catalogue, and documentation.
- No arbitrary iOS app execution on Linux, Mach-O header conversion, code-signing bypass, or entitlement bypass is promised.
