# Limt OS

An Android device information viewer and AI-assisted Android OS project generator.

## Current starter features

- Kotlin + Jetpack Compose Android application.
- Read-only device details: manufacturer, model, Android release, SDK, hardware, supported ABIs and SoC fields when Android exposes them.
- Serial number is deliberately not collected.
- Bootloader state is displayed as **Unable to detect bootloader**. The starter does not guess or change bootloader state.
- FastAPI backend with configurable OpenAI-compatible API endpoint.
- Server-configured model allowlist exposed through `GET /models`; the Android app displays a model picker.
- Multi-turn chat context.
- AI-generated files parsed from `---FILE: path---` blocks and individually saved with Android's system document picker.
- Health endpoint at `GET /health`.

## Project layout

- `app/`: Android application
- `backend/main.py`: AI API service
- `backend/requirements.txt`: Python dependencies
- `backend/.env.example`: environment variable template
- `backend/Dockerfile`: container build
- `config/device_profiles.yaml`: compatibility policy/profile placeholder

## Configure the AI backend

1. Create a Python virtual environment.
2. Install dependencies: `pip install -r backend/requirements.txt`.
3. Copy `backend/.env.example` to `backend/.env`.
4. Put a newly created provider API key in the private `.env` file or deployment secret manager as `OPENAI_API_KEY`.
5. Set `OPENAI_MODEL` to the model identifier supported by your provider.
6. Add selectable model IDs as a comma-separated list in `OPENAI_ALLOWED_MODELS`.
7. For the official OpenAI API, leave `OPENAI_BASE_URL` unset. For an OpenAI-compatible gateway, set it to that provider's documented API base URL.
8. Start from the `backend` directory: `uvicorn main:app --host 0.0.0.0 --port 8000`.

Never put a real key in Git, Android source code, an APK, screenshots, or chat messages. Keep it only in private server environment settings. Use HTTPS when deploying. Add authentication and rate limiting before exposing the backend publicly; the starter is not hardened for public production use.

## Build the Android app

1. Open this repository in Android Studio.
2. Wait for Gradle sync.
3. Deploy the backend to an HTTPS host.
4. Replace `https://YOUR_BACKEND_HOST` in `app/build.gradle.kts` with your backend base URL (without a trailing slash).
5. Build and run the `app` configuration on an Android device.

A Gradle wrapper is not included yet. Android Studio can import the project, but the app has not been built or device-tested in this repository.

## Generated files

Ask the assistant to output files using this format:

```text
---FILE: relative/path/to/file.ext---
complete file contents
```

The backend returns file names and contents separately. The Android app displays a save action for each generated file.

## Compatibility and safety limits

This is a project generator, not an OS installer or flasher. It does not unlock bootloaders, flash partitions, install firmware, or bypass device protections. Hardware detection is best-effort and cannot prove that a phone has the required kernel sources, vendor files, drivers, or a supported build path. The compatibility profile database is currently a placeholder, so the app must not claim a device is compatible until a verified profile is added.

Serial numbers are not needed for compatibility checks and are not collected. Android restricts access to persistent device identifiers for ordinary apps.
