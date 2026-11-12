#!/usr/bin/env python3
"""Compatibility entry point for the Limt-OS source dispatcher."""
from pathlib import Path
import sys

ROOT = Path(__file__).resolve().parents[1]
sys.path.insert(0, str(ROOT / "src"))
from limt.dispatcher import main

if __name__ == "__main__":
    raise SystemExit(main())
