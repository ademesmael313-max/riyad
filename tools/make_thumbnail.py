#!/usr/bin/env python3
"""Shrink a device screenshot so it fits in GitHub annotation messages."""
import sys

from PIL import Image

src, dst = sys.argv[1], sys.argv[2]
image = Image.open(src)
image.thumbnail((240, 900))
image.convert("RGB").save(dst, "JPEG", quality=52, optimize=True)
print(f"{dst}: {__import__('os').path.getsize(dst)} bytes")
