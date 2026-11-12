# Limt OS

An Android device information viewer and AI-assisted Android OS project generator.

## Current starter features

- Android app built with Kotlin and Jetpack Compose.
- Read-only device information: manufacturer, model, Android release, SDK, hardware, supported ABIs and SoC fields when Android exposes them.
- Serial number is deliberately not collected.
- Bootloader status is reported as **Unable to detect bootloader**. The starter does not guess or attempt to change bootloader state.
- FastAPI backend using the OpenAI Responses API.
- Configurable model through `OPENAI_MODEL`.
- AI response file blocks are parsed and returned to the app; the app offers Android's document picker to save each generated file.

## Project layout

- `app/`: Android application
- `backend/main.py`: AI API service
- `backend/requirements.txt`: Python dependencies
- `backend/.env.example`: environment variable template
- `config/device_profiles.yaml`: compatibility policy and profile placeholder

## Run the backend

Use Python 3.11+ or the included Dockerfile.

1. Create a virtual environment.
2. Install dependencies: `pip install -r backend/requirements.txt`.
3. Copy `backend/.env.example` to `backend/.env`.
4. Set `OPENAI_API_KEY` to your own server-side API key.
5. Set `OPENAI_MODEL` to a model enabled for your API project (the example uses `gpt-6-luna`).
6. Start from the `backend` directory: `uvicorn main:app --host 0.0.0.0 --port 8000`.

Do not commit `.env`, API keys, or other credentials. For a deployed backend, use HTTPS and restrict CORS to your app's trusted origin.

## Build the Android app

1. Open this repository in Android Studio (current stable version with Android Gradle Plugin 8.7.3 support).
2. Wait for Gradle sync.
3. Deploy the backend to an HTTPS host.
4. Replace `https://YOUR_BACKEND_HOST` in `app/build.gradle.kts` with your backend base URL (without a trailing slash).
5. Build and run the `app` configuration on your Android device.

A Gradle wrapper is not included yet; Android Studio can import the project and configure Gradle. This starter has not been built or device-tested in this repository.

## Generated files

Ask the assistant to output files. It should format each one like this:

```text
---FILE: relative/path/to/file.ext---
file contents here
```

The backend returns parsed file names and contents separately. The Android app lets you save each file using the system document picker.

## Safety and limitations

This is a project generator, not an OS installer or flasher. It does not unlock bootloaders, flash partitions, or install firmware. Hardware detection is best-effort and cannot establish whether a phone has all required vendor sources, drivers, or a supported build path. A real compatibility verdict needs a verified device profile and maintainers' device-specific source information.

Serial numbers are not required for compatibility checks and are not collected. Android restricts access to persistent device identifiers for ordinary apps.
