import os
import re
from typing import Any
from fastapi import FastAPI, HTTPException
from fastapi.middleware.cors import CORSMiddleware
from pydantic import BaseModel, Field
from openai import OpenAI
from dotenv import load_dotenv

load_dotenv()

app = FastAPI(title="Limt OS API", version="0.1.0")
app.add_middleware(
    CORSMiddleware,
    allow_origins=["*"],  # Restrict this to your app's domain before production.
    allow_credentials=False,
    allow_methods=["GET", "POST"],
    allow_headers=["*"],
)

client = OpenAI()
MODEL = os.getenv("OPENAI_MODEL", "gpt-6-luna")

class ChatRequest(BaseModel):
    message: str = Field(min_length=1, max_length=12000)
    device_info: dict[str, Any] = Field(default_factory=dict)
    history: list[dict[str, str]] = Field(default_factory=list)

def extract_files(text: str) -> list[dict[str, str]]:
    # AI file format: ---FILE: relative/path.ext--- followed by file content.
    files = []
    pattern = r"---FILE:\s*([A-Za-z0-9_./-]+)---\s*\n(.*?)(?=\n---FILE:|\Z)"
    for name, content in re.findall(pattern, text, flags=re.S):
        safe_name = name.replace("\\", "/")
        if safe_name.startswith("/") or ".." in safe_name.split("/"):
            continue
        files.append({"name": safe_name, "content": content.rstrip() + "\n"})
    return files

@app.get("/health")
def health():
    return {"ok": True, "service": "limt-os-api"}

@app.post("/chat")
def chat(request: ChatRequest):
    if not os.getenv("OPENAI_API_KEY"):
        raise HTTPException(status_code=503, detail="Backend API key is not configured.")
    system = (
        "You are Limt OS, an Android device compatibility and OS project assistant. "
        "Only make read-only device assessments and generate source/configuration files. "
        "Never claim bootloader state unless the app supplied verified evidence; if absent, "
        "say 'Unable to detect bootloader'. Never instruct the user to flash firmware, unlock "
        "a bootloader, bypass protections, or run destructive device commands. "
        "When the user asks for files, emit each file using exactly this format: "
        "---FILE: relative/path.ext--- then the complete file contents. "
        "Keep paths relative and never include secrets or API keys in generated files."
    )
    context = "Device information (untrusted diagnostic data): " + str(request.device_info)
    messages = [{"role": "system", "content": system + "\n" + context}]
    for item in request.history[-12:]:
        role = item.get("role")
        content = item.get("content", "")
        if role in ("user", "assistant") and isinstance(content, str):
            messages.append({"role": role, "content": content[:12000]})
    messages.append({"role": "user", "content": request.message})
    try:
        result = client.responses.create(model=MODEL, input=messages)
        answer = result.output_text or "The model returned no text."
        return {"reply": answer, "files": extract_files(answer), "model": MODEL}
    except Exception as exc:
        raise HTTPException(status_code=502, detail=f"AI request failed: {type(exc).__name__}")
