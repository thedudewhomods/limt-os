# Third-party components and upstream projects

Limt-OS uses distribution packages and may integrate selected components from
the following projects. This notice is an integration plan, not a statement
that their source code has already been copied into this repository.

| Project | Intended use | Upstream license noted during initial review | Integration status |
|---|---|---|---|
| UTM | Virtual-machine UX and QEMU reference | Apache-2.0 | Use upstream QEMU on Linux; no UTM iOS application code vendored |
| Runexesonomnios | Windows compatibility research (Wine/Box64 graphics and input integration) | LGPL-2.1 root license | No source vendored yet; review each component and its notices before porting |
| Omniosiparunner | iOS app-hosting research | AGPL-3.0 root license | No source vendored; iOS-hosted mechanisms are not a Linux iOS runtime |
| Husk | Android-on-iOS research | GPL-2.0 root license | No source vendored; not a ready-made Linux Android runtime |
| StikDebug | iOS developer-tooling research | AGPL-3.0 root license | No source vendored; iOS-specific JIT/device features are not Linux features |

Licenses may differ for bundled dependencies and individual files. Before
copying or modifying any upstream implementation, record its exact source
revision, preserve copyright and license notices, inspect dependency licenses,
and satisfy applicable source-distribution obligations. AGPL/GPL/LGPL code
must not be mixed into a proprietary component without a deliberate license
review.

The current ISO is built from Debian packages. Consult Debian's package
copyright metadata and the upstream package licenses for the complete notice
set for each built image.
