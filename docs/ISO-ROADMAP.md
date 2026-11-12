# Limt-OS bootable image roadmap

## Milestone 1 — x86_64 virtual-machine preview

- Debian Live base with XFCE desktop and LightDM.
- Basic Limt-OS Settings launcher for display, network, keyboard and window-manager panels.
- QEMU system/emulator utilities and SPICE guest agent.
- VMware open-vm-tools.
- CI-generated hybrid ISO artifact.

## Milestone 2 — runtime integration

- Wine backend with isolated per-app prefixes and clear compatibility reporting.
- Android container integration only after validating Binder, cgroups, namespaces,
  graphics and host-kernel requirements.
- Package inspector for PE, APK, AAB and IPA metadata; archive signatures and
  metadata are treated as untrusted input.
- Native process inspector and developer console using ordinary Linux interfaces.

## Milestone 3 — desktop and hardware

- Limt-OS adaptive desktop shell, external-display detection and window management.
- Hardware-specific kernel configuration and firmware packaging.
- VMware and UTM guest test matrix.

## Milestone 4 — ARM64

- Separate ARM64 live image and CI artifact.
- Test with UTM ARM64 virtualization on Apple Silicon and ARM64 VMware hosts.
- Keep x86_64 and ARM64 packages/build outputs distinct.

## Compatibility truths

An ISO cannot execute natively on every CPU architecture. x86_64 and ARM64
need separate kernels and userspace builds. UTM can emulate a foreign CPU, but
emulation is slower than native virtualization. APK and IPA are ZIP containers,
not executable formats for Linux `binfmt_misc`. APK installation requires an
Android runtime/container. IPA support in this project is metadata inspection
only; it does not modify Mach-O binaries or bypass Apple platform security.
