#!/usr/bin/env python3
"""Classify the outcome of a smoke run from the crash buffer and the app pid.

Prints one token to stdout and to the given output file:

    APPCRASH    the app itself threw a fatal exception
    SYSTEMDIED  the emulator's system_server died (infrastructure, not our bug)
    NOPROC      the app process is gone without a crash trace
    OK          the app is still running
"""
from __future__ import annotations

import sys

PACKAGE = "com.riyadsalihin.amharic"


def main() -> int:
    crash_path = sys.argv[1] if len(sys.argv) > 1 else "crash.txt"
    pid_path = sys.argv[2] if len(sys.argv) > 2 else "pid.txt"
    out_path = sys.argv[3] if len(sys.argv) > 3 else "verdict.txt"

    try:
        with open(crash_path, errors="ignore") as fh:
            crash = fh.read()
    except OSError:
        crash = ""
    try:
        with open(pid_path, errors="ignore") as fh:
            pid = fh.read().strip()
    except OSError:
        pid = ""

    crashed = any(
        f"Process: {PACKAGE}" in block and "DeadSystemException" not in block
        for block in crash.split("FATAL EXCEPTION")[1:]
    )
    if crashed:
        verdict = "APPCRASH"
    elif "DeadSystemException" in crash:
        verdict = "SYSTEMDIED"
    elif not pid:
        verdict = "NOPROC"
    else:
        verdict = "OK"

    print(verdict)
    with open(out_path, "w", encoding="utf-8") as fh:
        fh.write(verdict + "\n")
    return 0


if __name__ == "__main__":
    sys.exit(main())
