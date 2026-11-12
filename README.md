# Limt-OS

Limt-OS is an experimental Linux operating-system project. The repository is organized around native OS source, compatibility runtimes, package inspection, virtualization, and a bootable live-image build.

## Source layout

- `src/limt/dispatcher.py`: native Limt-OS package inspection and runtime dispatch logic.
- `scripts/omni-exec.py`: compatibility command-line entry point.
- `kernel/omni.fragment`: optional kernel configuration fragment.
- `systemd/omni-binfmt.service`: package-dispatch integration service.
- `build/iso/`: Debian live-build configuration for the first x86-64 ISO.
- `config/`: OS and device configuration.
- `backend/`: optional API service, not required to boot the ISO.

## Package support

- Windows PE: detected by its PE header and launched only if Wine is installed.
- APK: inspected as a ZIP package and installed only into an explicitly configured Waydroid runtime.
- IPA: metadata inspection only. Linux does not run arbitrary iOS apps.
- Unknown packages: rejected; no attempt is made to execute them.

APK and IPA files are archives, not native executable formats. They must not be registered as executable ZIP magic in `binfmt_misc`.

## Build the first ISO

See [docs/ISO-BUILD.md](docs/ISO-BUILD.md). The first image targets x86-64. ARM64 requires a separate build and validation pass. A CI artifact is not considered boot-tested until it has been tested in UTM and VMware.

## Third-party integration

See [THIRD_PARTY_NOTICES.md](THIRD_PARTY_NOTICES.md). Upstream application projects are treated as references for portable core ideas and libraries; platform-specific Android/iOS app shells are not part of this Linux OS tree.
