#!/usr/bin/env python3
"""Build the offline content assets for the Riyad as-Salihin (Amharic) app.

What it does
------------
1. Downloads the Arabic + English hadith text (once) from the public
   ``CheeseWithSauce/HadithsJSONFormat`` GitHub repository into ``tools/.cache``.
2. Merges the hand-written Amharic translations from ``content/am/hadiths.json``
   and the book metadata from ``content/books.json``.
3. Validates the result (numbering, missing text, unknown translation keys, ...).
4. Writes the assets consumed by the Android app:
       app/src/main/assets/content/index.json
       app/src/main/assets/content/books/book-01.json ... book-20.json

Usage
-----
    python3 tools/build_content.py            # build assets
    python3 tools/build_content.py --check    # validate only, write nothing
    python3 tools/build_content.py --refresh  # re-download the source data

Only the Python standard library is required.
"""

from __future__ import annotations

import argparse
import base64
import json
import os
import re
import sys
import urllib.request
from dataclasses import dataclass
from pathlib import Path

ROOT = Path(__file__).resolve().parent.parent
CACHE = ROOT / "tools" / ".cache" / "riyad_src"
ASSETS = ROOT / "app" / "src" / "main" / "assets" / "content"
BOOKS_META = ROOT / "content" / "books.json"
TRANSLATIONS = ROOT / "content" / "am" / "hadiths.json"

GITHUB_API = "https://api.github.com"
SOURCE_REPO = "CheeseWithSauce/HadithsJSONFormat"
SOURCE_DIR = "Sunnah/riyadussalihin"
EXPECTED_TOTAL = 1896

BLESSING_AR = "صلى الله عليه وسلم"
BLESSING_EN = " (peace be upon him)"


@dataclass
class Hadith:
    number: int
    arabic: str
    english: str
    amharic: str | None = None


# --------------------------------------------------------------------------- #
# Source download
# --------------------------------------------------------------------------- #
def api_json(url: str) -> dict:
    request = urllib.request.Request(url, headers={"User-Agent": "riyad-build"})
    with urllib.request.urlopen(request, timeout=120) as response:
        return json.load(response)


def download_sources(meta: list[dict], refresh: bool) -> None:
    CACHE.mkdir(parents=True, exist_ok=True)
    missing = [book for book in meta if not (CACHE / book["source"]).exists()]
    if not missing and not refresh:
        return

    print(f"Downloading {len(missing) if not refresh else len(meta)} source file(s) "
          f"from github.com/{SOURCE_REPO} ...")
    tree = api_json(f"{GITHUB_API}/repos/{SOURCE_REPO}/git/trees/HEAD?recursive=1")
    blobs = {
        entry["path"]: entry["sha"]
        for entry in tree.get("tree", [])
        if entry.get("type") == "blob"
    }
    for book in meta:
        path = f"{SOURCE_DIR}/{book['source']}"
        if path not in blobs:
            raise SystemExit(f"Source file not found in repository: {path}")
        if not refresh and (CACHE / book["source"]).exists():
            continue
        blob = api_json(f"{GITHUB_API}/repos/{SOURCE_REPO}/git/blobs/{blobs[path]}")
        raw = base64.b64decode(blob["content"])
        (CACHE / book["source"]).write_bytes(raw)
        print(f"  {book['source']} ({len(raw) // 1024} KiB)")


# --------------------------------------------------------------------------- #
# Text clean-up
# --------------------------------------------------------------------------- #
def clean_arabic(text: str) -> str:
    text = text.replace(BLESSING_AR, "ﷺ")
    text = text.replace("\u200f", "").replace("\u200e", "")
    text = re.sub(r"[ \t]+", " ", text)
    text = re.sub(r"\s*\n\s*", " ", text)
    return text.strip()


def clean_english(text: str) -> str:
    text = re.sub(r"[\(\[]?صلى الله عليه وسلم[\)\]]?", "(peace be upon him)", text)
    text = text.replace("((peace be upon him))", "(peace be upon him)")
    text = re.sub(r"[ \t]+", " ", text)
    text = re.sub(r"\s*\n\s*", " ", text)
    text = re.sub(r"\.\s*\.\s*", ". ", text)
    text = re.sub(r"\s+([,.;:!?])", r"\1", text)
    return text.strip()


def extract_number(reference: str) -> int:
    match = re.search(r"Riyad as-Salihin (\d+)", reference or "")
    if not match:
        raise ValueError(f"Cannot read hadith number from reference: {reference!r}")
    return int(match.group(1))


# --------------------------------------------------------------------------- #
# Build
# --------------------------------------------------------------------------- #
def build(meta: list[dict], translations: dict[str, str]) -> tuple[list[dict], list[list[Hadith]]]:
    books: list[list[Hadith]] = []
    index_books: list[dict] = []
    seen: set[int] = set()

    for order, book in enumerate(meta, start=1):
        source = json.loads((CACHE / book["source"]).read_text(encoding="utf-8-sig"))
        hadiths: list[Hadith] = []
        for item in source:
            number = extract_number(item.get("reference", ""))
            if number in seen:
                raise SystemExit(f"Duplicate hadith number {number}")
            seen.add(number)
            hadiths.append(
                Hadith(
                    number=number,
                    arabic=clean_arabic(item.get("arabic", "")),
                    english=clean_english(item.get("english", "")),
                    amharic=translations.get(str(number)),
                )
            )
        hadiths.sort(key=lambda h: h.number)
        books.append(hadiths)

        translated = sum(1 for h in hadiths if h.amharic)
        index_books.append(
            {
                "id": book["id"],
                "titleAr": book["titleAr"],
                "titleEn": book["titleEn"],
                "titleAm": book["titleAm"],
                "hadithCount": len(hadiths),
                "translatedCount": translated,
            }
        )
        print(
            f"  Book {order:02d}. {book['titleAm']:<28} "
            f"{len(hadiths):>4} hadith(s), {translated:>3} in Amharic"
        )

    return index_books, books


def validate(index_books: list[dict], books: list[list[Hadith]], translations: dict[str, str]) -> None:
    problems: list[str] = []

    numbers = [h.number for book in books for h in book]
    if len(numbers) != len(set(numbers)):
        problems.append("Duplicate hadith numbers found")

    missing_numbers = sorted(set(range(1, EXPECTED_TOTAL + 1)) - set(numbers))
    unexpected = sorted(set(numbers) - set(range(1, EXPECTED_TOTAL + 1)))
    if missing_numbers:
        problems.append(f"Missing hadith numbers: {missing_numbers[:20]}")
    if unexpected:
        problems.append(f"Unexpected hadith numbers: {unexpected[:20]}")
    if len(numbers) != EXPECTED_TOTAL:
        problems.append(f"Expected {EXPECTED_TOTAL} hadiths, found {len(numbers)}")

    for book in books:
        for hadith in book:
            if not hadith.arabic:
                problems.append(f"Hadith {hadith.number} has no Arabic text")
            elif not re.search(r"[\u0621-\u064A]", hadith.arabic):
                problems.append(f"Hadith {hadith.number} Arabic text looks wrong")
            if not hadith.english:
                problems.append(f"Hadith {hadith.number} has no English text")
            if hadith.amharic is not None:
                if not hadith.amharic.strip():
                    problems.append(f"Hadith {hadith.number} has an empty Amharic translation")
                elif not re.search(r"[\u1200-\u137F]", hadith.amharic):
                    problems.append(f"Hadith {hadith.number} Amharic text looks wrong")
                if len(hadith.amharic) < 20:
                    problems.append(f"Hadith {hadith.number} Amharic translation is suspiciously short")

    unknown = sorted(set(translations) - {str(n) for n in numbers}, key=lambda value: int(value))
    if unknown:
        problems.append(f"Translations for unknown hadith numbers: {unknown}")

    for entry in index_books:
        if entry["translatedCount"] > entry["hadithCount"]:
            problems.append(f"Book {entry['id']} reports too many translations")

    if problems:
        print("\nContent validation FAILED:", file=sys.stderr)
        for problem in problems:
            print(f"  - {problem}", file=sys.stderr)
        raise SystemExit(1)

    covered = sum(entry["translatedCount"] for entry in index_books)
    print(
        f"\nValidation passed: {len(numbers)} hadiths in {len(books)} books, "
        f"{covered} with Amharic ({covered / len(numbers):.1%})."
    )


def write(index_books: list[dict], books: list[list[Hadith]], translated_total: int) -> None:
    books_dir = ASSETS / "books"
    books_dir.mkdir(parents=True, exist_ok=True)

    for entry, hadiths in zip(index_books, books):
        payload = {
            "id": entry["id"],
            "titleAr": entry["titleAr"],
            "titleEn": entry["titleEn"],
            "titleAm": entry["titleAm"],
            "hadiths": [
                {
                    "number": h.number,
                    "arabic": h.arabic,
                    "english": h.english,
                    **({"amharic": h.amharic} if h.amharic else {}),
                }
                for h in hadiths
            ],
        }
        target = books_dir / f"book-{entry['id']:02d}.json"
        target.write_text(
            json.dumps(payload, ensure_ascii=False, separators=(",", ":")),
            encoding="utf-8",
        )
        print(f"  wrote {target.relative_to(ROOT)} ({target.stat().st_size // 1024} KiB)")

    index = {
        "schema": 1,
        "totalHadiths": EXPECTED_TOTAL,
        "translatedHadiths": translated_total,
        "books": index_books,
    }
    (ASSETS / "index.json").write_text(
        json.dumps(index, ensure_ascii=False, indent=2), encoding="utf-8"
    )
    print(f"  wrote {(ASSETS / 'index.json').relative_to(ROOT)}")


def main() -> None:
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument("--check", action="store_true", help="validate without writing assets")
    parser.add_argument("--refresh", action="store_true", help="re-download the source data")
    args = parser.parse_args()

    meta = json.loads(BOOKS_META.read_text(encoding="utf-8"))
    if [book["id"] for book in meta] != list(range(1, len(meta) + 1)):
        raise SystemExit("content/books.json: book ids must be 1..N in order")

    raw_translations = json.loads(TRANSLATIONS.read_text(encoding="utf-8"))
    translations = raw_translations.get("translations", {})
    for key, value in translations.items():
        if not key.isdigit():
            raise SystemExit(f"Translation key is not a hadith number: {key!r}")
        if not isinstance(value, str):
            raise SystemExit(f"Translation for hadith {key} must be a string")

    download_sources(meta, refresh=args.refresh)

    print("\nBuilding content:")
    index_books, books = build(meta, translations)
    validate(index_books, books, translations)

    if args.check:
        print("--check: no files written.")
        return

    translated_total = sum(entry["translatedCount"] for entry in index_books)
    write(index_books, books, translated_total)


if __name__ == "__main__":
    main()
