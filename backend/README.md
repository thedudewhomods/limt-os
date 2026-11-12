# Limt OS backend

This is the server used by both the Android and iOS apps. It provides `/health`, `/models`, and `/chat`.

## Deploy your own HTTPS backend with Render

1. Push this repository to GitHub.
2. Open https://render.com and sign in. If you are under 18, ask a parent or guardian to help create/manage the hosting and AI-provider accounts and approve any charges.
3. In Render, choose **New → Blueprint** and select this repository. Render reads the root `render.yaml` file and creates the `limt-os-api` web service.
4. Open the service's **Environment** page and set `OPENAI_API_KEY` to an API key from an AI provider account that supports the OpenAI-compatible API. Do not put the key in GitHub files, the mobile apps, or chat messages.
5. Wait for deployment. Test `https://YOUR-SERVICE.onrender.com/health`; it should return JSON with `"ok": true`.
6. Copy the service's HTTPS base URL (without a trailing slash).
7. In GitHub, open **Settings → Secrets and variables → Actions → New repository secret**. Create `LIMT_API_BASE_URL` with that HTTPS URL.
8. Run both **Build Limt OS APK** and **Build Limt OS iOS** workflows again. Both apps will then use the same backend URL.

The API key stays on the server. The mobile apps receive only the public HTTPS server address.

## Local development

From the repository root:

```sh
cd backend
python -m venv .venv
# Windows: .venv\\Scripts\\activate
# macOS/Linux: source .venv/bin/activate
pip install -r requirements.txt
cp .env.example .env
# Edit .env locally and set OPENAI_API_KEY. Never commit .env.
uvicorn main:app --reload --host 127.0.0.1 --port 8000
```

Check http://127.0.0.1:8000/health.

## Important

- Hosting the backend and using an AI model are separate costs. A free hosting tier does not make model API calls free; check the provider's pricing and set usage limits with a parent/guardian.
- Render's free service may sleep when idle, so the first request after inactivity can take longer.
- Never commit API keys, passwords, or private credentials.
- This starter backend is intended for development/testing. Before sharing it publicly, add user authentication, request rate limits, abuse monitoring, and stricter CORS/network controls.
- The iPhone IPA produced by CI is unsigned; it is a build package, not directly installable on a normal iPhone.
