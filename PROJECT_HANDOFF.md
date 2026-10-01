# JetSetCraft Project Handoff

## Canonical release

- Version: 0.3.0
- Public source: <https://github.com/Herbertofury/JetSetCraft>
- Release: <https://github.com/Herbertofury/JetSetCraft/releases/tag/v0.3.0>
- Local canonical project: `C:\Users\Owner\Desktop\JetSetCraft`
- Google Drive canonical folder: complete 0.3.0 source, release artifact, checksum, screenshots, and verification evidence

## Continuation-safe state

`main` and tag `v0.3.0` are the production baseline. The release reconciles the valuable historical branch work described in `docs/RECONCILIATION_0.3.0.md`. Do not revive an archive/export branch as a new base; compare its actual feature content against `main` first.

The physical ride item in `JetSetData` is the source of truth. The server owns movement, scoring, graffiti selection/custom payloads, Street Gear, gangs, and persistence. Clients send bounded input and render sanitized snapshots. PlayerAnimator is client-only; optional integrations remain isolated and optional.

Neutral directional input means neutral: stale momentum cannot manufacture camera-forward travel. Water, lava, and swimming give the entire velocity vector and animation pose back to Minecraft. Preserve those authority boundaries in future movement work.

The custom graffiti format is deliberately bounded to a 16 × 10, 4-bit palette payload. Do not replace it with arbitrary client texture/file uploads. Paid Marketplace and research-only references remain non-redistributable even when present in the private Desktop/Drive source-assets area.

## Verification entry points

- `python tools/validate_assets.py`
- `python tools/validate_gameplay_contract.py`
- `python tools/validate_java_syntax.py`
- `python tools/validate_premium_polish.py`
- `python tools/validate_wiki.py`
- `gradlew.bat clean build --no-daemon`
- `gradlew.bat runGameTestServer --no-daemon`
- `gradlew.bat -Djetsetcraft.visualAudit=true runClient --no-daemon`

Release evidence and the exact artifact checksum are recorded in `docs/RELEASE_EVIDENCE_0.3.0.md`.


## Current expansion execution authority

For ongoing post-v0.3 implementation, do not rely on this short release handoff alone. Use:

- `docs/JETSETCRAFT_CLAUDE_FINISH_EXECUTION.md` — the executable finish contract, including HUD, movement, progression, universal challenges, curated modded gangs, and runtime/release gates.
- `docs/CURATED_MODDED_GANG_ATLAS.md` — the version-aware curated gang map for popular mod mobs plus per-gang collectible graffiti/tag/logo/mural progression.

Those documents preserve stable task IDs and must be resumed rather than rewritten from scratch. The universal runtime Mob Atlas remains the fallback for any installed creature not yet premium-curated.
