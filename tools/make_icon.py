"""Build Nova Client's mod icon from the provided logo.

Crops to the starburst emblem (dropping the "NOVAWORK" wordmark), trims the
page shadow, and exports a clean 128x128 PNG.
"""
from pathlib import Path

from PIL import Image, ImageChops

SRC = Path(r"C:/Users/mackey/.workbuddy-ai/clipboard-images/clipboard-2026-10-07T06-49-36-026Z-483075c7.jpg")
OUT = Path(r"D:/Nova Client/src/main/resources/assets/nova/icon.png")

SIZE = 128
# Fraction of the source height that holds the emblem (above the wordmark).
EMBLEM_BOTTOM = 0.765

img = Image.open(SRC).convert("RGB")
w, h = img.size
print("source:", w, "x", h)

region = img.crop((0, 0, w, int(h * EMBLEM_BOTTOM)))

# Trim the white page margins (and the soft drop shadow) around the ink.
gray = region.convert("L")
ink = gray.point(lambda p: 255 if p < 160 else 0)
bbox = ink.getbbox()
print("ink bbox:", bbox)

if bbox:
    pad = int(min(bbox[2] - bbox[0], bbox[3] - bbox[1]) * 0.06)
    bbox = (max(0, bbox[0] - pad), max(0, bbox[1] - pad),
            min(region.width, bbox[2] + pad), min(region.height, bbox[3] + pad))
    region = region.crop(bbox)

# Pad to a square so the resize keeps the emblem's proportions.
ew, eh = region.size
side = max(ew, eh)
square = Image.new("RGB", (side, side), (255, 255, 255))
square.paste(region, ((side - ew) // 2, (side - eh) // 2))

icon = square.resize((SIZE, SIZE), Image.LANCZOS)
icon = icon.convert("RGBA")

OUT.parent.mkdir(parents=True, exist_ok=True)
icon.save(OUT, "PNG")
print("wrote:", OUT, OUT.stat().st_size, "bytes")
