#!/usr/bin/env python3
"""Seal verified Street + Gang Runtime build evidence into JetSetCraft project memory.

This script is intentionally standard-library-only so GitHub Actions can run it after the
real Forge GameTests and dedicated-server smoke test without adding another dependency.
It updates project-owned recovery state, while the complete machine receipt is written
under build/verification (ignored by Git) for artifact publication.
"""
from __future__ import annotations

import argparse
import hashlib
import json
import os
import re
import subprocess
from datetime import datetime, timezone
from pathlib import Path
from typing import Any

ROOT = Path(__file__).resolve().parents[1]
MEMORY = ROOT / ".agents-memory"
DEFAULT_RECEIPT = ROOT / "build/verification/style-flow-verification.json"
DEFAULT_GAMETEST_LOG = ROOT / "build/verification/style-flow-gametest.log"
DEFAULT_SERVER_LOG = ROOT / "build/verification/server-smoke.log"
VERSION = "0.3.0-alpha.1"


def sha256(path: Path) -> str:
    digest = hashlib.sha256()
    with path.open("rb") as handle:
        for chunk in iter(lambda: handle.read(1024 * 1024), b""):
            digest.update(chunk)
    return digest.hexdigest()


def file_evidence(path: Path) -> dict[str, Any]:
    return {
        "file": path.name,
        "bytes": path.stat().st_size,
        "sha256": sha256(path),
    }


def read_json(path: Path) -> dict[str, Any]:
    if not path.exists():
        return {}
    return json.loads(path.read_text(encoding="utf-8"))


def write_json(path: Path, value: dict[str, Any]) -> None:
    path.parent.mkdir(parents=True, exist_ok=True)
    path.write_text(json.dumps(value, indent=2, ensure_ascii=False) + "\n", encoding="utf-8")


def first_matching_line(text: str, patterns: tuple[str, ...]) -> str:
    for line in text.splitlines():
        if any(re.search(pattern, line, re.IGNORECASE) for pattern in patterns):
            return line.strip()
    return ""


def git_output(*args: str) -> str:
    try:
        return subprocess.check_output(
            ["git", *args], cwd=ROOT, text=True, stderr=subprocess.DEVNULL
        ).strip()
    except (OSError, subprocess.CalledProcessError):
        return ""


def find_runtime_jar() -> Path:
    candidates = sorted((ROOT / "build/libs").glob("*.jar"))
    candidates = [
        path
        for path in candidates
        if not path.name.endswith(("-sources.jar", "-javadoc.jar", "-dev.jar"))
    ]
    if len(candidates) != 1:
        names = ", ".join(path.name for path in candidates) or "none"
        raise SystemExit(f"Expected exactly one runtime JAR in build/libs, found: {names}")
    return candidates[0]


def verify_logs(gametest_log: Path, server_log: Path) -> tuple[str, str]:
    if not gametest_log.exists():
        raise SystemExit(f"Missing GameTest log: {gametest_log}")
    if not server_log.exists():
        raise SystemExit(f"Missing server smoke log: {server_log}")

    gametest_text = gametest_log.read_text(encoding="utf-8", errors="replace")
    server_text = server_log.read_text(encoding="utf-8", errors="replace")

    required_markers = (
        "JETSETCRAFT_GAMETEST_PASS hoverboard",
        "JETSETCRAFT_GAMETEST_PASS scooter",
        "JETSETCRAFT_GAMETEST_PASS dance_flow",
        "JETSETCRAFT_GAMETEST_PASS combat_sovereignty",
        "JETSETCRAFT_GAMETEST_PASS catalogs",
        "JETSETCRAFT_GAMETEST_PASS street_gear",
    )
    missing = [marker for marker in required_markers if marker not in gametest_text]
    if missing:
        raise SystemExit("GameTest log is missing required pass markers: " + ", ".join(missing))

    pass_line = first_matching_line(
        gametest_text,
        (r"All\s+6\s+required\s+tests\s+passed", r"6\s+tests?\s+passed"),
    )
    if not pass_line:
        raise SystemExit("GameTest log does not report all six required tests passing")

    ready_line = first_matching_line(
        server_text,
        (r"Done \([0-9.]+s\)!", r"For help, type \"help\""),
    )
    if not ready_line:
        raise SystemExit("Dedicated-server log does not contain the ready marker")
    return pass_line, ready_line


def build_status(
    prior: dict[str, Any],
    jar: Path,
    gametest_log: Path,
    server_log: Path,
    pass_line: str,
    ready_line: str,
) -> dict[str, Any]:
    verified = dict(prior.get("verified", {}))
    verified.update(
        {
            "street_gang_workflow_run": int(os.getenv("GITHUB_RUN_ID", "0") or 0),
            "street_gang_workflow_run_number": int(os.getenv("GITHUB_RUN_NUMBER", "0") or 0),
            "workflow_input_commit": os.getenv("GITHUB_SHA", "") or git_output("rev-parse", "HEAD"),
            "workflow_branch": os.getenv("GITHUB_HEAD_REF") or os.getenv("GITHUB_REF_NAME", ""),
            "forge_clean_build": True,
            "asset_gameplay_and_wiki_validation": True,
            "real_forge_street_gang_gametests": {
                "required": 6,
                "passed": 6,
                "observed": pass_line,
                **file_evidence(gametest_log),
            },
            "dedicated_server_ready": True,
            "dedicated_server_smoke": {
                "observed": ready_line,
                **file_evidence(server_log),
            },
            "street_gang_build": file_evidence(jar),
            "ride_style_count": 6,
            "dance_move_count": 28,
            "trick_count": 24,
            "animation_resource_count": 68,
            "wiki_page_count": 25,
        }
    )

    return {
        "version": VERSION,
        "phase": "Street + Gang Runtime / physical Boombox, persistent gangification, real Forge GameTests and dedicated-server verified",
        "implemented": [
            "six server-authoritative ride styles: inline, quad, skateboard, BMX, hoverboard, scooter",
            "persistent hands-free ride loadout with scooter and dedicated hoverboard geometry",
            "continuous momentum, boost, manuals, powerslides, wall rides, rail transfers, and block-edge grinding",
            "vanilla, Forge, Create 6.0.8 track, and data-driven optional mod surface compatibility",
            "twenty-four named contextual ride, grind, boost, and ground tricks",
            "repeat penalties, trick variety tracking, graded landings, combo ranks, and Flow meter",
            "twenty-eight named breakdance and street-dance moves across six families",
            "no-gear server-authoritative dancing, automatic phrase chaining, and multiplayer cypher bonuses",
            "immediate combat and item-use sovereignty over full-body style animation",
            "lower-body PlayerAnimator locomotion composition that preserves weapon arms",
            "sixty-eight validated deterministic animation resources",
            "material-aware grind feedback, trick/dance particles, sounds, and accessible reduced-motion presentation",
            "configurable Style HUD, trick labels, camera roll/FOV, dance/cypher rules, and Flow tuning",
            "dense procedural inline, quad, skateboard, hoverboard, BMX, scooter, and spray-can meshes",
            "offline-safe deterministic asset generation and complete graffiti fallback catalog",
            "optional Aether Quicksoil/Blue Aercloud/Aerogel and Twilight Forest Aurora route hooks without dependencies",
            "complete validated twenty-five-page GitHub wiki source and automatic wiki publication workflow",
            "six isolated real Forge GameTests with unique fake-player identities, including Street Gear",
            "craftable/placeable Boombox with a physical visible one-item mob-head gang target slot",
            "stable curated vanilla Gang Registry plus deterministic safe IDs for optional-mod entities",
            "original-source EntityType gang actor factory with species-aware Street Gear instead of entity replacement",
            "persistent reversible gangification bound to actual Street Gear equipment",
            "server-authoritative Boombox sessions with staggered entrances, actor caps, loaded-terrain safety, UUID tracking, and hard expiry",
            "ephemeral Boombox event cast with no normal loot/XP farming and clean cancellation cleanup",
            "per-gang registered music slots with valid Vorbis placeholder audio ready for owner-authored tracks",
            "provider-agnostic mob-head targeting wired end-to-end into the physical Boombox",
        ],
        "verified": verified,
        "pending_validation": [
            "real client visual smoke test across all ride styles, dance families, named tricks, HUD, VFX, and reduced-motion mode",
            "real Create 6.0.8 straight, sloped, junction, and Bezier interoperability runtime smoke test",
            "TACZ live aim/fire/reload composition smoke test while riding and immediately after dancing",
            "Aether and Twilight Forest optional route smoke tests with their exact Forge 1.20.1 releases",
            "multiplayer movement, cypher, combat, dimension-transfer, and persistence acceptance world",
        ],
        "pending_features": [],
        "pending_assets": [],
        "source_inputs": prior.get("source_inputs", {}),
    }


def write_handoff(receipt: dict[str, Any]) -> None:
    jar = receipt["artifacts"]["jar"]
    text = f"""# JetSetCraft Handoff

Canonical repo: `Herbertofury/JetSetCraft`; target Forge 1.20.1 / Java 17 / mod version `{VERSION}`.

The verified runtime keeps the complete Style Flow foundation (six ride styles, 24 contextual tricks, 28 dance moves, hoverboard/scooter meshes, Create/native rail hooks, combat-safe animation composition, graffiti and the 25-page wiki) and adds the first production Gang Wars layer: a physical craftable Boombox, visible mob-head target slot, the approved stable vanilla gang atlas, reversible Street-Gear gangification, original-source EntityType event actors, species-aware gang gear, safe staggered Boombox sessions, no-cooldown restart, anti-farm ephemeral cast cleanup, and stable per-gang music slots.

Real Forge proof for this checkpoint: six required GameTests passed (with `street_gear` now covering physical Boombox Zombie Head → Dead Beat tuning and start/cancel/restart), the dedicated server reached its ready state, and runtime JAR `{jar['file']}` is {jar['bytes']} bytes with SHA-256 `{jar['sha256']}`. Workflow run `{receipt['workflow']['run_id']}` on branch `{receipt['workflow']['branch']}` produced the evidence.

Remaining acceptance is real-client/modpack validation and the broader design roadmap rather than this runtime being untested source: visual Boombox/head rendering and audio smoke, live optional head-mod packs, Create/TACZ/Aether/Twilight integration, multiplayer soak, and later Atlas/reputation/territory/chapter/minigame layers. See `docs/BOOMBOX_GANG_RUNTIME.md`, `docs/GANG_WARS_BOOMBOX_MOB_ATLAS_MASTER_SPEC.md`, and `wiki/Testing-and-Verification.md`.
"""
    (MEMORY / "HANDOFF.md").write_text(text, encoding="utf-8")


def seal_tree(receipt_path: Path, tree_sha: str) -> None:
    receipt = read_json(receipt_path)
    if not receipt:
        raise SystemExit(f"Cannot seal missing receipt: {receipt_path}")
    receipt["source_tree"] = tree_sha
    receipt["sealed_at"] = datetime.now(timezone.utc).isoformat()
    write_json(receipt_path, receipt)


def main() -> None:
    parser = argparse.ArgumentParser()
    parser.add_argument("--gametest-log", type=Path, default=DEFAULT_GAMETEST_LOG)
    parser.add_argument("--server-log", type=Path, default=DEFAULT_SERVER_LOG)
    parser.add_argument("--receipt", type=Path, default=DEFAULT_RECEIPT)
    parser.add_argument("--seal-tree", default="")
    args = parser.parse_args()

    receipt_path = args.receipt if args.receipt.is_absolute() else ROOT / args.receipt
    if args.seal_tree:
        seal_tree(receipt_path, args.seal_tree)
        print(f"Sealed Street + Gang Runtime receipt with source tree {args.seal_tree}")
        return

    gametest_log = args.gametest_log if args.gametest_log.is_absolute() else ROOT / args.gametest_log
    server_log = args.server_log if args.server_log.is_absolute() else ROOT / args.server_log
    jar = find_runtime_jar()
    pass_line, ready_line = verify_logs(gametest_log, server_log)

    prior = read_json(MEMORY / "STATUS.json")
    status = build_status(prior, jar, gametest_log, server_log, pass_line, ready_line)
    write_json(MEMORY / "STATUS.json", status)

    receipt: dict[str, Any] = {
        "schema": 1,
        "project": "JetSetCraft",
        "version": VERSION,
        "verified_at": datetime.now(timezone.utc).isoformat(),
        "workflow": {
            "run_id": int(os.getenv("GITHUB_RUN_ID", "0") or 0),
            "run_number": int(os.getenv("GITHUB_RUN_NUMBER", "0") or 0),
            "input_commit": os.getenv("GITHUB_SHA", "") or git_output("rev-parse", "HEAD"),
            "branch": os.getenv("GITHUB_HEAD_REF") or os.getenv("GITHUB_REF_NAME", ""),
        },
        "contracts": {
            "ride_styles": 6,
            "named_tricks": 24,
            "dance_moves": 28,
            "dance_families": 6,
            "animation_resources": 68,
            "wiki_pages": 25,
            "real_forge_gametests_required": 6,
            "real_forge_gametests_passed": 6,
            "dedicated_server_ready": True,
        },
        "observed": {
            "gametest": pass_line,
            "server": ready_line,
        },
        "artifacts": {
            "jar": file_evidence(jar),
            "gametest_log": file_evidence(gametest_log),
            "server_log": file_evidence(server_log),
        },
    }
    write_json(receipt_path, receipt)

    recovery = dict(receipt)
    recovery["remaining_acceptance"] = status["pending_validation"]
    recovery["supersedes"] = ".agents-memory/RECOVERY_2026-08-21_STYLE_FLOW.json"
    write_json(MEMORY / "RECOVERY_2026-08-23_BOOMBOX_GANG_RUNTIME.json", recovery)
    write_handoff(receipt)
    print(f"JetSetCraft Street + Gang Runtime verification sealed: {jar.name} {receipt['artifacts']['jar']['sha256']}")


if __name__ == "__main__":
    main()
