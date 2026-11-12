# Third-party project and license notes

This file records upstream projects considered for Limt-OS integration. It does not mean their source code has been copied into this repository. Review the license of each individual file and bundled dependency before vendoring or modifying source.

| Project | Intended use in Limt-OS | Root license reviewed | Integration status |
|---|---|---|---|
| UTM | QEMU/VM architecture reference; use upstream QEMU on Linux | Apache-2.0 | No UTM app code copied; Linux QEMU is the target |
| Runexesonomnios | Windows compatibility concepts from its Winlator-derived stack | LGPL-2.1 | No source copied; evaluate Wine/Box64 and dependency licenses separately |
| Omniosiparunner | iOS-hosted app-container research only | AGPL-3.0 | No source copied; not a Linux iOS runtime |
| Husk | Android-on-iOS research reference | GPL-2.0 (root LICENSE) | No source copied; not a Linux Android runtime |
| StikDebug | iOS developer-tooling reference | AGPL-3.0 | No source copied; iOS-specific functionality is not transplanted |

Upstream repositories:
- https://github.com/thedudewhomods/UTM
- https://github.com/thedudewhomods/Runexesonomnios
- https://github.com/thedudewhomods/Omniosiparunner
- https://github.com/thedudewhomods/Husk
- https://github.com/thedudewhomods/StikDebug

Before copying code, preserve copyright notices, include the complete applicable license text, identify modifications, and satisfy source-offer/source-distribution obligations where required. AGPL/GPL/LGPL obligations can apply to modified or combined components; obtain a component-level review rather than assuming the root license covers every dependency.
