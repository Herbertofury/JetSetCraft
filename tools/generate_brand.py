#!/usr/bin/env python3
from __future__ import annotations
from pathlib import Path
from PIL import Image

ROOT = Path(__file__).resolve().parents[1]
SOURCE = ROOT / "source_assets/brand/jetsetcraft_official_art.png"
RUNTIME = ROOT / "src/main/resources/jetsetcraft.png"
DIST = ROOT / "art/generated"

def fit_contain(im: Image.Image, size: tuple[int, int]) -> Image.Image:
    src = im.convert("RGBA")
    src.thumbnail(size, Image.Resampling.LANCZOS)
    out = Image.new("RGBA", size, (0, 0, 0, 0))
    out.alpha_composite(src, ((size[0] - src.width)//2, (size[1] - src.height)//2))
    return out

def fit_cover(im: Image.Image, size: tuple[int, int]) -> Image.Image:
    src = im.convert("RGBA")
    scale = max(size[0] / src.width, size[1] / src.height)
    resized = src.resize((round(src.width * scale), round(src.height * scale)), Image.Resampling.LANCZOS)
    left = (resized.width - size[0]) // 2
    top = (resized.height - size[1]) // 2
    return resized.crop((left, top, left + size[0], top + size[1]))

def main() -> int:
    if not SOURCE.exists():
        print(f"JetSetCraft brand source not mounted yet: {SOURCE}")
        return 0
    im = Image.open(SOURCE)
    RUNTIME.parent.mkdir(parents=True, exist_ok=True)
    DIST.mkdir(parents=True, exist_ok=True)
    fit_contain(im, (128, 128)).quantize(colors=192, method=Image.Quantize.FASTOCTREE, dither=Image.Dither.FLOYDSTEINBERG).save(RUNTIME, optimize=True)
    fit_contain(im, (512, 512)).save(DIST / "jetsetcraft-icon-512.png", optimize=True)
    # Distribution banner is a crop of the owner's art only; no synthetic extension or repainting.
    fit_cover(im, (1200, 630)).save(DIST / "jetsetcraft-banner-1200x630.png", optimize=True)
    print("JetSetCraft official brand derivatives generated")
    return 0

if __name__ == "__main__":
    raise SystemExit(main())
