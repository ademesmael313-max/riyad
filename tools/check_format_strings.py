#!/usr/bin/env python3
"""Static guard against Android format-string crashes.

`context.getString(R.string.hadiths_count, someString)` throws
`IllegalFormatConversionException: d != java.lang.String` at runtime when the
string uses a `%d` placeholder, and it throws
`MissingFormatArgumentException` when fewer arguments are passed than the
string expects. Both are instant crashes on the screen that renders the text,
so they are checked here instead of on a device.

The checker parses `app/src/main/res/values/strings.xml`, then every
`stringResource(R.string.x, ...)` and `getString(R.string.x, ...)` call in the
Kotlin sources, and reports:

  * a call that passes fewer arguments than the string's placeholders need;
  * a call that passes more arguments than the string has placeholders;
  * a numeric placeholder (`%d`, `%f`, ...) fed a value that is clearly a
    string — a quoted literal or a `toString()` result.

Exit code 1 when a problem is found.
"""
from __future__ import annotations

import re
import sys
import xml.etree.ElementTree as ET
from pathlib import Path

ROOT = Path(__file__).resolve().parents[1]
STRINGS_XML = ROOT / "app/src/main/res/values/strings.xml"
KOTLIN_ROOT = ROOT / "app/src/main/java"

PLACEHOLDER = re.compile(r"%(?:(\d+)\$)?([sdfx])")
CALL = re.compile(r"(?:stringResource|getString)\(\s*(?:id\s*=\s*)?R\.string\.(\w+)")
STRINGISH = re.compile(r'^"|\.toString\(\)\s*$|formatCount|String\.format')


def load_placeholders() -> dict[str, list[str]]:
    tree = ET.parse(STRINGS_XML)
    result: dict[str, list[str]] = {}
    for node in tree.getroot().findall("string"):
        name = node.get("name")
        if not name:
            continue
        text = "".join(node.itertext())
        # %% is a literal percent sign, not a placeholder.
        kinds = [m.group(2) for m in PLACEHOLDER.finditer(text.replace("%%", ""))]
        if kinds:
            result[name] = kinds
    return result


def split_args(raw: str) -> list[str]:
    """Split the argument list of a call, ignoring commas inside strings/parens."""
    args: list[str] = []
    depth = 0
    quote = ""
    current = ""
    for char in raw:
        if quote:
            current += char
            if char == quote:
                quote = ""
            continue
        if char in '"\'s':
            if char in '"\'':  # start of a literal
                quote = char
            current += char
        elif char in "([{":
            depth += 1
            current += char
        elif char in ")]}":
            depth -= 1
            current += char
        elif char == "," and depth == 0:
            if current.strip():
                args.append(current.strip())
            current = ""
        else:
            current += char
    if current.strip():
        args.append(current.strip())
    return args


def call_arguments(source: str, start: int) -> list[str]:
    """Return the arguments a string call passes after the string id.

    `start` points just past `R.string.<name>`; scanning runs to the matching
    closing parenthesis so nested calls such as `numberLabel(hadith)` stay in a
    single argument.
    """
    depth = 1
    quote = ""
    current = ""
    args: list[str] = []
    index = start
    while index < len(source):
        char = source[index]
        if quote:
            current += char
            if char == quote:
                quote = ""
        elif char in "\"'":
            quote = char
            current += char
        elif char in "([{":
            depth += 1
            current += char
        elif char in ")]}":
            depth -= 1
            if depth == 0:
                if current.strip():
                    args.append(current.strip())
                return args
            current += char
        elif char == "," and depth == 1:
            if current.strip():
                args.append(current.strip())
            current = ""
        else:
            current += char
        index += 1
    return args


def expected_arguments(kinds: list[str]) -> int:
    return len(kinds)


def main() -> int:
    placeholders = load_placeholders()
    problems: list[str] = []

    for path in sorted(KOTLIN_ROOT.rglob("*.kt")):
        source = path.read_text(encoding="utf-8")
        for match in CALL.finditer(source):
            name = match.group(1)
            args = call_arguments(source, match.end())
            if name not in placeholders:
                if args:
                    problems.append(
                        f"{path.relative_to(ROOT)}: {name}() is given arguments but its "
                        "string has no placeholders"
                    )
                continue
            kinds = placeholders[name]
            if len(args) != expected_arguments(kinds):
                problems.append(
                    f"{path.relative_to(ROOT)}: {name}() takes {len(kinds)} argument(s), "
                    f"the call passes {len(args)}"
                )
                continue
            for kind, arg in zip(kinds, args):
                if kind in "dfx" and STRINGISH.search(arg):
                    problems.append(
                        f"{path.relative_to(ROOT)}: {name}() passes a string to a "
                        f"%{kind} placeholder ({arg})"
                    )

    if problems:
        print("Format-string problems found:\n")
        for problem in problems:
            print(f"  - {problem}")
        return 1

    print(f"Format strings OK ({len(placeholders)} strings with placeholders checked).")
    return 0


if __name__ == "__main__":
    sys.exit(main())
