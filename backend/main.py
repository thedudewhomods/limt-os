import os
import re
from typing import Any
from fastapi import FastAPI, HTTPException
from fastapi.middleware.cors import CORSMiddleware
from pydantic import BaseModel, Field
from openai import OpenAI
from dotenv import load_dotenv

load_dotenv()

app = FastAPI(title="Limt OS API", version="0.2.0")
app.add_middleware(
    CORSMiddleware,
    allow_origins=["*"],  # Native Android clients do not use browser CORS; restrict web origins if a web client is added.
    allow_credentials=False,
    allow_methods=["GET", "POST"],
    allow_headers=["Content-Type", "Authorization"],
)

API_KEY = os.getenv("OPENAI_API_KEY", "")
BASE_URL = os.getenv("OPENAI_BASE_URL") or None
DEFAULT_MODEL = os.getenv("OPENAI_MODEL", "gpt-5.5")
ALLOWED_MODELS = [
    value.strip()
    for value in os.getenv("OPENAI_ALLOWED_MODELS", DEFAULT_MODEL).split(",")
    if value.strip()
]
client = OpenAI(api_key=API_KEY or "missing-api-key", base_url=BASE_URL)

class ChatRequest(BaseModel):
    message: str = Field(min_length=1, max_length=12000)
    model: str | None = Field(default=None, max_length=160)
    device_info: dict[str, Any] = Field(default_factory=dict)
    history: list[dict[str, str]] = Field(default_factory=list)

def extract_files(text: str) -> list[dict[str, str]]:
    files = []
    pattern = r"---FILE:\\s*([A-Za-z0-9_./-]+)---\\s*\\n(.*?)(?=\\n---FILE:|\\Z)"
    for name, content in re.findall(pattern, text, flags=re.S):
        safe_name = name.replace("\\\\", "/")
        if safe_name.startswith("/") or ".." in safe_name.split("/"):
            continue
        files.append({"name": safe_name, "content": content.rstrip() + "\\n"})
    return files

@app.get("/health")
def health():
    return {"ok": True, "service": "limt-os-api", "version": "0.2.0"}

@app.get("/models")
def models():
    return {"default": DEFAULT_MODEL, "models": ALLOWED_MODELS}

@app.post("/chat")
def chat(request: ChatRequest):
    if not API_KEY:
        raise HTTPException(status_code=503, detail="Backend API key is not configured.")
    model = request.model or DEFAULT_MODEL
    if model not in ALLOWED_MODELS:
        raise HTTPException(status_code=400, detail="That model is not enabled by this server.")
    system = (
        "You are Limt OS, an Android device compatibility and OS project assistant. "
        "Only make read-only device assessments and generate source/configuration files. "
        "Never claim bootloader state unless the app supplied verified evidence; if absent, "
        "say 'Unable to detect bootloader'. Never instruct the user to flash firmware, unlock "
        "a bootloader, bypass protections, or run destructive device commands. "
        "Do not claim a device can run a custom OS without a verified device profile. "
        "When asked for files, emit each file using exactly this format: "
        "---FILE: relative/path.ext--- then the complete file contents. "
        "Keep paths relative, do not include secrets, and explain any untested assumptions."
    )
    context = "Device information (untrusted diagnostic data): " + str(request.device_info)
    messages = [{"role": "system", "content": system + "\\n" + context}]
    for item in request.history[-12:]:
        role = item.get("role")
        content = item.get("content", "")
        if role in ("user", "assistant") and isinstance(content, str):
            messages.append({"role": role, "content": content[:12000]})
    messages.append({"role": "user", "content": request.message})
    try:
        # Chat Completions is supported by OpenAI and many OpenAI-compatible gateways.
        result = client.chat.completions.create(
            model=model,
            messages=messages,
            max_tokens=12000,
        )
        answer = result.choices[0].message.content or "The model returned no text."
        return {"reply": answer, "files": extract_files(answer), "model": model}
    except Exception as exc:
        raise HTTPException(status_code=502, detail=f"AI request failed: {type(exc).__name__}")
