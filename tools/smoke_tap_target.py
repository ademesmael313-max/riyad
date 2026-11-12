#!/usr/bin/env python3
"""Find the screen coordinates of the first book row in a uiautomator dump.

Used by the launch smoke test: after the home screen renders we tap the first
book so the reader screen is exercised too. Prints "x y" on success and nothing
when the row cannot be located (the caller then simply skips the tap).
"""
from __future__ import annotations

import json
import re
import sys

NODE = re.compile(r'text="([^"]*)"[^>]*bounds="\[(\d+),(\d+)\]\[(\d+),(\d+)\]"')


def main() -> int:
    xml_path = sys.argv[1] if len(sys.argv) > 1 else "ui.xml"
    index_path = (
        sys.argv[2] if len(sys.argv) > 2 else "app/src/main/assets/content/index.json"
    )

    with open(index_path, encoding="utf-8") as fh:
        first_title = json.load(fh)["books"][0]["titleAm"]

    try:
        with open(xml_path, encoding="utf-8", errors="ignore") as fh:
            xml = fh.read()
    except OSError:
        return 0

    for match in NODE.finditer(xml):
        if match.group(1).strip() != first_title:
            continue
        left, top, right, bottom = (int(value) for value in match.groups()[1:])
        print((left + right) // 2, (top + bottom) // 2)
        return 0
    return 0


if __name__ == "__main__":
    sys.exit(main())
