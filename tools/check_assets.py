#!/usr/bin/env python3
"""Validate the committed offline assets (no network or downloads needed).

Checks that the generated files in app/src/main/assets/content are complete,
consistently numbered and that the Amharic coverage reported in index.json
matches the actual translations. Run it in CI and before releasing:

    python3 tools/check_assets.py
"""

from __future__ import annotations

import json
import re
import sys
from pathlib import Path

ROOT = Path(__file__).resolve().parent.parent
ASSETS = ROOT / "app" / "src" / "main" / "assets" / "content"
EXPECTED_TOTAL = 1896
ARABIC = re.compile(r"[\u0621-\u064A]")
ETHIOPIC = re.compile(r"[\u1200-\u137F]")


def main() -> int:
    problems: list[str] = []

    index = json.loads((ASSETS / "index.json").read_text(encoding="utf-8"))
    books = index.get("books", [])
    if len(books) != 20:
        problems.append(f"index.json lists {len(books)} books, expected 20")
    if [b.get("id") for b in books] != list(range(1, len(books) + 1)):
        problems.append("book ids in index.json are not 1..N")
    if index.get("totalHadiths") != EXPECTED_TOTAL:
        problems.append(f"index.json totalHadiths is {index.get('totalHadiths')}, expected {EXPECTED_TOTAL}")

    seen: list[int] = []
    translated_total = 0
    hadith_total = 0

    for entry in books:
        path = ASSETS / "books" / f"book-{entry['id']:02d}.json"
        if not path.exists():
            problems.append(f"missing {path.relative_to(ROOT)}")
            continue
        book = json.loads(path.read_text(encoding="utf-8"))
        for field in ("titleAr", "titleEn", "titleAm"):
            if book.get(field) != entry.get(field):
                problems.append(f"book {entry['id']}: {field} differs between index and book file")

        hadiths = book.get("hadiths", [])
        if len(hadiths) != entry.get("hadithCount"):
            problems.append(
                f"book {entry['id']}: {len(hadiths)} hadiths but index says {entry.get('hadithCount')}"
            )

        translated = 0
        for hadith in hadiths:
            number = hadith.get("number")
            seen.append(number)
            hadith_total += 1
            if not hadith.get("arabic") or not ARABIC.search(hadith["arabic"]):
                problems.append(f"hadith {number}: missing/incorrect Arabic text")
            if not hadith.get("english"):
                problems.append(f"hadith {number}: missing English text")
            amharic = hadith.get("amharic")
            if amharic:
                translated += 1
                if not ETHIOPIC.search(amharic):
                    problems.append(f"hadith {number}: Amharic text has no Ethiopic script")
                if len(amharic) < 20:
                    problems.append(f"hadith {number}: Amharic text suspiciously short")
        translated_total += translated
        if translated != entry.get("translatedCount"):
            problems.append(
                f"book {entry['id']}: {translated} translations but index says {entry.get('translatedCount')}"
            )

    if sorted(seen) != list(range(1, EXPECTED_TOTAL + 1)):
        missing = sorted(set(range(1, EXPECTED_TOTAL + 1)) - set(seen))
        duplicates = sorted({n for n in seen if seen.count(n) > 1})
        problems.append(f"hadith numbering problems (missing={missing[:10]}, duplicates={duplicates[:10]})")
    if hadith_total != EXPECTED_TOTAL:
        problems.append(f"found {hadith_total} hadiths, expected {EXPECTED_TOTAL}")
    if index.get("translatedHadiths") != translated_total:
        problems.append(
            f"index.json translatedHadiths is {index.get('translatedHadiths')}, counted {translated_total}"
        )

    if problems:
        print("Asset validation FAILED:", file=sys.stderr)
        for problem in problems:
            print(f"  - {problem}", file=sys.stderr)
        return 1

    print(
        f"Assets OK: {hadith_total} hadiths in {len(books)} books, "
        f"{translated_total} with Amharic ({translated_total / hadith_total:.1%})."
    )
    return 0


if __name__ == "__main__":
    raise SystemExit(main())
