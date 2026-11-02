# JetSetCraft Project Handoff

## Canonical release

- Version: 0.3.0
- Public source: <https://github.com/Herbertofury/JetSetCraft>
- Release: <https://github.com/Herbertofury/JetSetCraft/releases/tag/v0.3.0>
- Local canonical project: `C:\Users\Owner\Desktop\JetSetCraft`
- Google Drive canonical folder: complete 0.3.0 source, release artifact, checksum, screenshots, and verification evidence

## Continuation-safe state

Tag `v0.3.0` remains the stable release baseline. `main` now also contains the optional **0.4.0-alpha.6** development mode and shared progression described below. The stable release reconciles the valuable historical branch work in `docs/RECONCILIATION_0.3.0.md`. Do not revive an archive/export branch as a new base; compare its actual feature content against `main` first.

The physical ride item in `JetSetData` is the source of truth. The server owns movement, scoring, graffiti selection/custom payloads, Street Gear, gangs, and persistence. Clients send bounded input and render sanitized snapshots. PlayerAnimator is client-only; optional integrations remain isolated and optional.

Neutral directional input means neutral: stale momentum cannot manufacture camera-forward travel. Water, lava, and swimming give the entire velocity vector and animation pose back to Minecraft. Preserve those authority boundaries in future movement work.

The custom graffiti format is deliberately bounded to a 16 × 10, 4-bit palette payload. Do not replace it with arbitrary client texture/file uploads. Paid Marketplace and research-only references remain non-redistributable even when present in the private Desktop/Drive source-assets area.

## Verified continuation — 0.4.0-alpha.6

[PR #45](https://github.com/Herbertofury/JetSetCraft/pull/45) merged as `6e85fb3015620a19a6bd4a69b0154cbe8d248527`, with tree `b2618bbabd94649a88039eefdd2625f9f0839127` identical to local checkpoint `f438241e32f8d753cab095ff8f70a03c1302116c`, feature `1e8da0f47b2a9622a9ed454cbe8ac10fafdbad0a` and tested integration `988362f3dc4159df2ded4889059f67c8ed9406b0`. It adds bounded elite/headliner encounters, three rank-three build evolutions, versioned rerolls/banishes, exact next-rank previews, a movable threat/resource HUD and a persistent optional run debrief including its saved exit reason. Protocol is 14. Headliners gate circuit rooms three and six, while Free streets preserves its thirty-minute extraction and Encore. Evolution opportunities are earned once per actual owned victory and selected only at an existing decision stop.

**All nine CI configurations passed on their first attempt:** [base run 41392665091](https://github.com/Herbertofury/JetSetCraft/actions/runs/41392665091) and [provider run 41392665154](https://github.com/Herbertofury/JetSetCraft/actions/runs/41392665154) completed every required step, including **55 GameTests each — 495 executions**, builds and normal dedicated-server save/shutdown. Base also passed its nineteen-frame native gate. The fresh local replay separately passed all five validators and the same base gates; all nineteen local images were reviewed. Preserve the evidence distinction: the new native audit uses logged owned-rival hit/placement and clock-compression fixtures; it observes a live headliner without defeating it, then returns with two defeats, two cards, no arena/headliner clears and no qualifying return award. These are controlled integration/visual results, not a completed human session or proof of CI artifact identity.

Use the [alpha.6 source and verification receipt](docs/verification/headliners/README.md) for exact identities and limits. ForgeGradle executes `sourceSets.main` in native Forge; separately building the reobfuscated JAR does not establish the finish contract's final installed-package gate. New encounter/evolution fixtures use the Dead Beat zombie-family cast. Provider lanes demonstrate the pinned modpack's coexistence and exercised paths, including native Aether/AoA origin return and the scoped Iron's projectile bridge where available; arbitrary foreign capabilities, every native mob phase and broad spellbook/mana coverage remain unclaimed.

## Verified predecessor — 0.4.0-alpha.5

[PR #44](https://github.com/Herbertofury/JetSetCraft/pull/44) merged as `5bad45891b6fe7a5c7c133ed0a19ac0556dd5af8`, with the exact tested tree `878476f60413bfe47c18864a99674c363432ce87`. The [Style Library receipt](docs/verification/style-library/README.md) identifies the nine successful CI configurations, 44 GameTests per configuration, normal dedicated-server save/shutdown gates, and the independently reproduced fifteen-screen local gallery. CI archive metadata and local evidence are explicitly distinguished.

The continuation connects all activity rewards to sixteen permanent Rush starting lessons, adds six artifact families, improves cache visibility and scouting, favors unvisited arena routes, and extends optional provider arenas and editable data-pack examples. Existing crew houses, shared reward receipts, six-room circuits, optional remixes, normal inventory/XP return and mode selection remain part of the current implementation. Use the current [Rival Rush guide](wiki/Rival-Rush.md), [crew-house guide](wiki/Player-Chapters-Posse-and-Mob-Archetypes.md), and [Dungeons I/II implementation matrix](docs/RIVAL_RUSH_DESIGN.md) when planning the next slice.

Human pacing, full-session economy/Endless balance, multiplayer scale, visible crew residents, the complete native Dungeons equipment catalogue, companion/beam systems and broad spellbook/mana integration remain open work. The current custom arena format is a bounded data pack, not an in-game map editor. Preserve the once-only reward transactions, one starting preparation, three artifact slots, optional-provider boundaries and saved return journal.

## Verification entry points

- `python tools/validate_assets.py`
- `python tools/validate_gameplay_contract.py`
- `python tools/validate_java_syntax.py`
- `python tools/validate_premium_polish.py`
- `python tools/validate_wiki.py`
- `gradlew.bat clean build --no-daemon`
- `gradlew.bat runGameTestServer --no-daemon`
- `gradlew.bat -Djetsetcraft.visualAudit=true runClient --no-daemon`
- `gradlew.bat -Djetsetcraft.rushVisualAudit=true runClient --no-daemon` — Rush/crew/activity/progression audit, only with the workflow's disposable `saves/RivalRushAudit` setup.

Release evidence and the exact artifact checksum are recorded in `docs/RELEASE_EVIDENCE_0.3.0.md`.


## Current expansion execution authority

For ongoing post-v0.3 implementation, do not rely on this short release handoff alone. Use:

- `docs/JETSETCRAFT_CLAUDE_FINISH_EXECUTION.md` — the executable finish contract, including HUD, movement, progression, universal challenges, curated modded gangs, and runtime/release gates.
- `docs/CURATED_MODDED_GANG_ATLAS.md` — the version-aware curated gang map for popular mod mobs plus per-gang collectible graffiti/tag/logo/mural progression.

Those documents preserve stable task IDs and must be resumed rather than rewritten from scratch. The universal runtime Mob Atlas remains the fallback for any installed creature not yet premium-curated.
