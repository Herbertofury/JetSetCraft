# Testing and Verification

JetSetCraft treats a passing Java compile as intermediate evidence, not final proof. The repository CI regenerates assets, validates contracts, builds the reobfuscated JAR, runs real Forge GameTests, and starts a real dedicated server.

## Versioned verification evidence

The published **v0.3.0** release has **nine** GameTests, as recorded in its [release evidence](https://github.com/Herbertofury/JetSetCraft/blob/main/docs/RELEASE_EVIDENCE_0.3.0.md). The **0.4.0-alpha.1 development source** adds six Rival Rush tests, for **fifteen** required tests. A development checkpoint passing CI does not change the published release's version or evidence.

The 1 November 2026 checkpoint containing feature commit [`7283dd8`](https://github.com/Herbertofury/JetSetCraft/commit/7283dd8f912c2c6903e156d468fd1326ee8966b7) passed the following gates. GitHub checked out the PR integration commit [`520591e`](https://github.com/Herbertofury/JetSetCraft/commit/520591e049e38fcd7592679788d97b28cd27d4ba), which merges that feature commit into the then-current base.

| Configuration | Completed evidence |
| --- | --- |
| Base Forge 1.20.1 / Java 17 | [Build, all fifteen GameTests, dedicated-server readiness and clean shutdown, and a real Minecraft client audit](https://github.com/Herbertofury/JetSetCraft/actions/runs/41346460726) |
| Alex's Mobs, Alex's Caves, Cataclysm, and all three together | [Four separate provider jobs, each passing its build, all fifteen GameTests, and dedicated-server readiness](https://github.com/Herbertofury/JetSetCraft/actions/runs/41346460787) |

These links identify that checkpoint's evidence. Later code changes require their own passing run. Provider jobs establish the documented registry and gameplay regression baseline; they do not prove every optional creature's behavior or every combination of other mods.

## Local source checks

```text
python -m pip install -r tools/requirements.txt
python tools/generate_models.py
python tools/generate_animations.py
python tools/generate_brand.py
python tools/validate_assets.py
python tools/validate_gameplay_contract.py
python tools/validate_premium_polish.py
python tools/validate_java_syntax.py
python tools/validate_wiki.py
./gradlew clean build --no-daemon
./gradlew runGameTestServer --no-daemon
```

The model generator is offline-safe: when the pinned optional graffiti archive cannot be fetched, it removes any partial download and produces the original JetSetCraft fallback catalog.

## Forge GameTests

The nine original tests verify:

1. Hoverboard registry identity, persistent loadout NBT, and the production ground movement solver.
2. Neutral ride input cannot relaunch stale momentum, deliberate input restarts correctly, directional sprint kickoff works while neutral kickoff cannot manufacture motion, and swimming retains the exact vanilla velocity vector.
3. Scooter registry identity, persistent loadout NBT, and the universal `JetSetMovement` path.
4. Breakdance starting with no ride gear, server-owned move selection, completed-phrase scoring, and Flow.
5. Immediate dance cancellation when a combat swing takes presentation/input authority.
6. Stable one-to-one identity for all 28 dance moves, complete 24-trick catalog coverage, and hostile input/NBT sanitization.
7. Same-entity Street Gear identity, physical-item persistence, real entity NBT save/load, clean removal, spider/slime anatomy routing, persistent gangification/de-gangification, and a real placed Boombox proving Zombie Head → Dead Beat tuning plus start/cancel/immediate-restart/target-return behavior.
8. All sixteen registered/craftable paint-balloon colors, deterministic bounded splat encoding, Tag/Free Paint item state, real 1,000-ray surface placement, exact decal materialization, and persistent six-face sizing.
9. Built-in and custom graffiti NBT persistence plus automatic removal after the supporting wall is broken.

The six additional development tests exercise production Rival Rush paths:

10. Deterministic three-choice upgrades, rejection of stale or repeated choices, bounded ranks and counters, the thirty-minute decision pause, and explicit Endless continuation.
11. All seven mob-card mastery thresholds, increasing milestone rewards, three starting-draft unlocks, distinct gang/species identities, saved album data, and unchanged Minecraft XP.
12. Recovery through the production return-journal path, exact original inventory and ride NBT, survival state and effects, preserved album progress, and rejection of incomplete, foreign-owner, nonfinite, or duplicate-slot journals before inventory mutation.
13. Actual session entry and exit across dimensions, loan equipment, exact original position/items/ride/XP restoration, and Forge capability invalidation/revival with the same durable ride data.
14. Real director-created actors and card pickups, independent player ownership, rejection of cross-session damage and duplicate claims, and suppression of event XP without changing unrelated Minecraft mob XP.
15. The real spray-can placement hook and points for a fresh wall, rejection of repaint farming, and authoritative upgrade actions that reject stale run IDs, stale offer versions, and replays.

The player tests use uniquely identified Forge fake players and a real `ServerLevel` rather than mocked movement classes. Unique profiles prevent parallel GameTests from moving or mutating the same automation player.

FakePlayer tests establish in-memory lifecycle behavior; they do not establish player-file durability. The real-client audit exercises the actual save acknowledgement and network entry/return paths. Forced process termination, storage failure, arbitrary third-party player capabilities, and a human thirty-minute or long Endless playtest are separate acceptance work; no passing gate above implies those have been exercised.

## Input transport acceptance

Client controls remain change-driven: any digital mask or analog movement change is transmitted immediately. While riding, dancing, or holding live movement input, unchanged state is refreshed every 5 client ticks instead of being resent every tick. A neutral inactive client sends its release state once and then stays quiet. The server independently sanitizes unknown bits/non-finite axes and clears stale armed input after 20 ticks, so the active heartbeat retains a four-times safety margin without needless 20-packets-per-second steady-state traffic.

## Dedicated-server smoke

CI launches Forge with `eula=true`, waits for the real ready line, and fails on lifecycle/mod-loading/fatal JetSetCraft errors. The produced JAR and logs are uploaded as workflow artifacts.

## Real-client visual audit

Run `gradlew.bat -Djetsetcraft.visualAudit=true runClient --no-daemon` on Windows (or the wrapper equivalent elsewhere). The opt-in harness quick-loads its development world, creates a controlled inline-skate/HUD/painted-wall scene, captures the selector in persistent Free Paint mode and the custom painter, prints `JETSETCRAFT_VISUAL_AUDIT_PASS`, and exits cleanly. It is inert in ordinary launches.

The development CI also runs `./gradlew runClient -Djetsetcraft.rushVisualAudit=true --no-daemon` with a disposable `RivalRushAudit` world prepared by the [build workflow](https://github.com/Herbertofury/JetSetCraft/blob/main/.github/workflows/build.yml). It captures the setup menu, district HUD, album, and return result; enters and leaves through real network actions; and compares the returned dimension, inventory and vanilla XP with the observed pre-entry fixture. The gate requires all four written screenshots and `JETSETCRAFT_RUSH_VISUAL_AUDIT_PASS`. Screenshots and logs accompany the workflow artifact. This short automated run does not measure pacing, sustained performance, or how a complete session feels to play.

## Runtime acceptance world

Run:

- `/jetsetcraft build_vanilla_lab`
- `/jetsetcraft status`
- `/jetsetcraft set_momentum <speed>` as an operator-only diagnostic

Then exercise every ride style, dance family, surface lane, rail type, wall, hazard, transfer, combat overlay, dimension transition, death/respawn, reconnect, and multiplayer tracking path.

## Wiki verification

`tools/validate_wiki.py` ensures the complete page set exists, sidebar destinations resolve, internal wiki links are valid, required technical terms are present, and no unfinished-marker language ships. The validated `wiki/` directory is the canonical source published to the GitHub wiki.

## Premium offline gates

The premium gate additionally rejects malformed or degenerate OBJ geometry, unresolved materials/textures, non-finite or visibly unclosed animation loops, hard optional-mod class imports, invasive mixin/ASM paths, stale client-cache regressions, and Java syntax failures before ForgeGradle begins. These checks are strong preflight evidence, but they never replace the real Forge build, GameTests, dedicated-server smoke, or client acceptance.
