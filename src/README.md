# Limt-OS source

This directory contains OS-side implementation code, not Android or iOS application projects.

- `limt/dispatcher.py`: package inspection and runtime dispatch.
- `limt/__init__.py`: package marker.

The dispatcher recognizes Windows PE headers, inspects APK/IPA ZIP contents, and rejects unknown formats. It never treats APK/IPA archives as native Linux executables.