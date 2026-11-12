# OmniOS architecture (foundation)

OmniOS is the proposed Linux distribution direction for Limt OS. This repository currently contains an Android device-information/project-generator app and a FastAPI assistant backend; it is not yet a bootable operating system.

## Feasibility boundaries

| Format | Foundation integration | Important limitation |
|---|---|---|
| Windows PE (.exe/.msi) | Wine/Proton; Box64 or FEX on supported ARM64 hosts; DXVK/VKD3D where Vulkan is available | Compatibility is application-specific. Kernel drivers and some anti-cheat systems will not work. |
| Android APK | Waydroid/Android userspace with Binder support and a compatible kernel | An APK is a ZIP package, not a directly executable ELF. Install it through Android package tools inside the container. |
| Android AAB | Bundletool converts an app bundle into installable APK sets | AAB is a publishing format, not a directly launchable package. |
| iOS IPA | Inspect and catalogue bundle metadata; research a separate Darwin compatibility project | An IPA is not a Linux executable. LiveContainer is iOS-hosted, not a Linux runtime. UIKit, dyld, XNU services, signing, entitlements and Apple frameworks cannot be supplied by changing a Mach-O file type. This project does not bypass code signing, FairPlay or device security. |

## Runtime architecture

- omni-exec: safe user-facing dispatcher. It inspects file contents, not just extensions.
- Windows: Wine/Proton is the first supported execution backend. ARM64 translation is a later optional integration.
- Android: Waydroid is the first supported backend. APK installation happens inside the Android container.
- iOS: metadata inspection only in this foundation release. No fake dlopen path and no unsigned-app execution claims.
- VM fallback: QEMU/KVM is a later optional backend for guests that require a separate kernel. Startup time depends on hardware and guest configuration.
- Storage: per-package directories under the user's data directory. This first implementation does not mount OverlayFS or change host permissions.

## Kernel configuration guidance

Use architecture-specific kernel configs. KVM Intel and AMD options are platform-dependent; they are not both usable on every machine. AVX-512 and SVE2 are CPU capabilities, not generic kernel switches that work on unsupported hardware. Binder options vary by kernel version; verify the selected kernel's Kconfig before building. Ashmem is obsolete on many modern Android stacks; prefer memfd where supported.

The kernel/omni.fragment file is a starting fragment, not a complete defconfig and not a claim that every option exists in every kernel version.

## Security model

- Never execute a downloaded binary as root.
- Keep app data isolated per package.
- Do not auto-extract untrusted archives into privileged directories.
- Do not disable platform code-signing or DRM checks.
- Keep API keys out of the repository and client binaries.
- Treat device properties and archive metadata as untrusted input.

## Build status

This branch adds a dispatcher and project documentation. It does not yet produce an installable ISO. A bootable distribution requires a pinned kernel, root filesystem, bootloader configuration, init system, graphics stack, package repositories, licensing review and hardware testing.
