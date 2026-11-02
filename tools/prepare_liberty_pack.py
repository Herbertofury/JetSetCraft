#!/usr/bin/env python3
"""Prepare the pinned, unmodified Helldivers ZIP in an ignored test instance; never extract or commit it."""
from __future__ import annotations

import argparse
import hashlib
import os
from pathlib import Path
import shutil
import tempfile
import urllib.request
import zipfile

ROOT = Path(__file__).resolve().parents[1]
FILE_NAME = "[eof]helldivers_gun_pack.zip"
SOURCE_PAGE = "https://www.curseforge.com/minecraft/customization/tacz-helldivers-escalation-of-freedom/files/9341504"
DOWNLOAD_URL = "https://edge.forgecdn.net/files/9341/504/%5Beof%5Dhelldivers_gun_pack.zip"
EXPECTED_BYTES = 72_883_841
EXPECTED_SHA256 = "fe4e01693cf3957d971e556a1d2fcd9ae0c98cb973cf47aa6d52e157c187378a"
PRIMARY_INDEX = "data/helldiver2/index/guns/ar23_liberator.json"
SECONDARY_INDEX = "data/helldiver2/index/guns/p2_peacemaker.json"


def verify(path: Path) -> None:
    if not path.is_file() or path.stat().st_size != EXPECTED_BYTES:
        raise ValueError(f"Pinned pack missing or wrong byte count: {path}")
    digest = hashlib.sha256()
    with path.open("rb") as source:
        for chunk in iter(lambda: source.read(1 << 20), b""):
            digest.update(chunk)
    if digest.hexdigest() != EXPECTED_SHA256:
        raise ValueError(f"Pack SHA-256 does not match the pinned Liberty Day release: {path}")
    with zipfile.ZipFile(path) as archive:
        for resource in (PRIMARY_INDEX, SECONDARY_INDEX, "data/zeta/index/ammo/ar_ammo.json",
                         "data/zeta/index/ammo/p113_ammo.json"):
            if resource not in archive.namelist():
                raise ValueError(f"Pack is missing required resource {resource}")


def reject_duplicates(folder: Path, destination: Path) -> None:
    for other in folder.glob("*.zip"):
        if other == destination:
            continue
        try:
            with zipfile.ZipFile(other) as archive:
                if PRIMARY_INDEX in archive.namelist() or SECONDARY_INDEX in archive.namelist():
                    raise ValueError(f"Duplicate Helldivers gun pack: {other}. Remove it from this test instance first.")
        except zipfile.BadZipFile:
            # Unrelated malformed archives belong to their owner; do not delete or repair them here.
            continue


def main() -> None:
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument("--game-directory", type=Path, default=ROOT / "run",
                        help="Instance working directory (default: ignored repository run/)")
    parser.add_argument("--source", type=Path, help="Use an already downloaded official ZIP, still checking its exact hash")
    parser.add_argument("--verify-only", action="store_true", help="Check the prepared ZIP without downloading or copying")
    args = parser.parse_args()
    folder = args.game_directory.resolve() / "tacz"
    destination = folder / FILE_NAME
    reject_duplicates(folder, destination)
    if destination.exists() or args.verify_only:
        verify(destination)
    else:
        folder.mkdir(parents=True, exist_ok=True)
        temporary: Path | None = None
        try:
            with tempfile.NamedTemporaryFile(dir=folder, prefix=".liberty-pack-", suffix=".part", delete=False) as output:
                temporary = Path(output.name)
                if args.source:
                    verify(args.source)
                    with args.source.open("rb") as source:
                        shutil.copyfileobj(source, output)
                else:
                    request = urllib.request.Request(DOWNLOAD_URL, headers={"User-Agent": "JetSetCraft-Liberty-Verification/1.0"})
                    with urllib.request.urlopen(request, timeout=45) as response:
                        received = 0
                        while chunk := response.read(1 << 20):
                            received += len(chunk)
                            if received > EXPECTED_BYTES:
                                raise ValueError("Upstream response exceeds the pinned release size")
                            output.write(chunk)
            verify(temporary)
            os.replace(temporary, destination)
            temporary = None
        finally:
            if temporary is not None:
                temporary.unlink(missing_ok=True)
    print(f"JETSETCRAFT_LIBERTY_PACK_READY file=9341504 bytes={EXPECTED_BYTES} sha256={EXPECTED_SHA256}")
    print(f"Source: {SOURCE_PAGE}")
    print(f"Prepared: {destination}")


if __name__ == "__main__":
    try:
        main()
    except (OSError, ValueError, zipfile.BadZipFile) as error:
        raise SystemExit(f"Liberty pack preparation failed: {error}") from error
