#!/usr/bin/env python3
"""Deterministic Wave 2 mob-compatibility manifest validation."""

from __future__ import annotations

import json
from pathlib import Path

ROOT = Path(__file__).resolve().parents[1]
DATA = ROOT / "src/main/resources/data/jetsetcraft/jetsetcraft_gangs"

EXPECTED = {
    "alexsmobs": (90, 7, "1.22.9"),
    "alexscaves": (43, 2, "2.0.2"),
    "cataclysm": (39, 0, "3.31"),
}

REQUIRED_ENTRY_KEYS = {
    "gang_id",
    "display_name",
    "entity",
    "crew_family_id",
    "ride_profile",
    "challenge_profile",
    "graffiti_motif",
    "boombox_eligible",
    "legendary",
}


def fail(message: str) -> None:
    raise SystemExit(f"Wave 2 compatibility validation failed: {message}")


def main() -> None:
    all_entities: set[str] = set()
    all_gangs: set[str] = set()
    safe_total = 0
    hidden_total = 0

    for provider, (safe_expected, hidden_expected, version_expected) in EXPECTED.items():
        path = DATA / f"wave2_{provider}.json"
        if not path.is_file():
            fail(f"missing {path.relative_to(ROOT)}")
        payload = json.loads(path.read_text(encoding="utf-8"))

        if payload.get("schema") != 1:
            fail(f"{provider}: schema must be 1")
        if payload.get("provider_mod_id") != provider:
            fail(f"{provider}: provider_mod_id mismatch")
        if payload.get("provider_version") != version_expected:
            fail(f"{provider}: expected version {version_expected!r}")
        if payload.get("expected_safe_count") != safe_expected:
            fail(f"{provider}: expected_safe_count metadata mismatch")
        if payload.get("expected_hidden_count") != hidden_expected:
            fail(f"{provider}: expected_hidden_count metadata mismatch")

        entries = payload.get("entries")
        hidden = payload.get("hidden_entities")
        if not isinstance(entries, list) or len(entries) != safe_expected:
            fail(f"{provider}: expected {safe_expected} entries, got {len(entries) if isinstance(entries, list) else 'non-list'}")
        if not isinstance(hidden, list) or len(hidden) != hidden_expected:
            fail(f"{provider}: expected {hidden_expected} hidden ids")

        provider_entities: set[str] = set()
        provider_gangs: set[str] = set()
        for index, entry in enumerate(entries):
            missing = sorted(REQUIRED_ENTRY_KEYS - set(entry))
            if missing:
                fail(f"{provider} entry {index}: missing keys {missing}")
            entity = entry["entity"]
            gang_id = entry["gang_id"]
            family = entry["crew_family_id"]
            if not isinstance(entity, str) or not entity.startswith(provider + ":"):
                fail(f"{provider} entry {index}: entity namespace mismatch: {entity!r}")
            if not isinstance(gang_id, str) or not gang_id.startswith("jetsetcraft:w2_" + provider + "_"):
                fail(f"{provider} entry {index}: unstable gang id: {gang_id!r}")
            if not isinstance(family, str) or not family.startswith("jetsetcraft:" + provider + "_"):
                fail(f"{provider} entry {index}: crew family mismatch: {family!r}")
            if not entry["display_name"].strip():
                fail(f"{provider} entry {index}: blank display_name")
            if not entry["ride_profile"].strip() or not entry["challenge_profile"].strip():
                fail(f"{provider} entry {index}: blank compatibility profile")
            if not entry["graffiti_motif"].strip():
                fail(f"{provider} entry {index}: blank graffiti motif")
            if "boss-gated" in entry["challenge_profile"]:
                if entry["boombox_eligible"]:
                    fail(f"{entity}: boss-gated records cannot be Boombox-spawn eligible")
                if not entry["legendary"]:
                    fail(f"{entity}: boss-gated records must be marked legendary")
            if "owner-gated" in entry["challenge_profile"] and entry["boombox_eligible"]:
                fail(f"{entity}: owner-gated records cannot be Boombox-spawn eligible")
            if entity in provider_entities or entity in all_entities:
                fail(f"duplicate entity id {entity}")
            if gang_id in provider_gangs or gang_id in all_gangs:
                fail(f"duplicate gang id {gang_id}")
            provider_entities.add(entity)
            provider_gangs.add(gang_id)

        hidden_set = set(hidden)
        if len(hidden_set) != len(hidden):
            fail(f"{provider}: duplicate hidden entity id")
        if provider_entities & hidden_set:
            fail(f"{provider}: safe and hidden entity sets overlap")
        if any(not value.startswith(provider + ":") for value in hidden):
            fail(f"{provider}: hidden entity namespace mismatch")

        all_entities.update(provider_entities)
        all_gangs.update(provider_gangs)
        safe_total += len(entries)
        hidden_total += len(hidden)

    if safe_total != 172:
        fail(f"expected 172 safe curated records, got {safe_total}")
    if hidden_total != 9:
        fail(f"expected 9 hidden technical records, got {hidden_total}")

    print(f"Wave 2 compatibility manifests OK: {safe_total} curated safe records, {hidden_total} hidden technical records.")


if __name__ == "__main__":
    main()
