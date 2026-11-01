# Testing and Verification

JetSetCraft treats a passing Java compile as intermediate evidence, not final proof. The repository CI regenerates assets, validates contracts, builds the reobfuscated JAR, runs real Forge GameTests, and starts a real dedicated server.

## Alpha.3 activity and arena acceptance

The activity and arena implementation merged in [PR #41](https://github.com/Herbertofury/JetSetCraft/pull/41) passed **all 28 real Forge GameTests in each of nine configurations**, plus every configuration's build and dedicated-server readiness/save/shutdown checks. The base run also produced **ten reviewed 1280 × 720 real-client captures**. The [verification receipt, machine-readable record and gallery](https://github.com/Herbertofury/JetSetCraft/tree/main/docs/verification/arena-circuit) preserve the exact source identities, artifact and scope.

The verified feature commit is [`c733852`](https://github.com/Herbertofury/JetSetCraft/commit/c733852b260ef5dbf2cb54e7d41fc98ac10b8363), tested through PR integration [`2626a8d`](https://github.com/Herbertofury/JetSetCraft/commit/2626a8d63c7c065a6ca5c241610993a28bdb58ca). PR #41 merged as [`e57e9ab`](https://github.com/Herbertofury/JetSetCraft/commit/e57e9ab9732a84d90e6ac35a82439e54cd4461ab). All three commits have Git tree `e5e127156d0335a9c6f2e33ba153e8ecfc75e757`, tying the merged implementation to the tested source. These results were recorded on **2026-11-01**.

| Configuration | Completed evidence |
|---|---|
| Base Forge 1.20.1 / Java 17 | [Verify JetSetCraft #280](https://github.com/Herbertofury/JetSetCraft/actions/runs/41356358215/job/124997134020): build, 28 GameTests, dedicated-server readiness/save/shutdown and ten real-client captures |
| Alex's Mobs 1.22.9 | [Provider #56 — `alexsmobs`](https://github.com/Herbertofury/JetSetCraft/actions/runs/41356358302/job/124997134620): build, 28 GameTests and dedicated-server readiness/save/shutdown |
| Alex's Caves 2.0.2 | [Provider #56 — `alexscaves`](https://github.com/Herbertofury/JetSetCraft/actions/runs/41356358302/job/124997134535): build, 28 GameTests and dedicated-server readiness/save/shutdown |
| Cataclysm 3.31 | [Provider #56 — `cataclysm`](https://github.com/Herbertofury/JetSetCraft/actions/runs/41356358302/job/124997134668): build, 28 GameTests and dedicated-server readiness/save/shutdown |
| Alex's Mobs + Alex's Caves + Cataclysm | [Provider #56 — `all`](https://github.com/Herbertofury/JetSetCraft/actions/runs/41356358302/job/124997134415): build, 28 GameTests and dedicated-server readiness/save/shutdown |
| Aether 1.20.1-1.5.2-neoforge | [Provider #56 — `aether`](https://github.com/Herbertofury/JetSetCraft/actions/runs/41356358302/job/124997134603): build, 28 GameTests and dedicated-server readiness/save/shutdown |
| Iron's Spells 1.20.1-3.4.0.2 | [Provider #56 — `irons_spells`](https://github.com/Herbertofury/JetSetCraft/actions/runs/41356358302/job/124997134635): build, 28 GameTests and dedicated-server readiness/save/shutdown |
| AoA 1.20.1-3.7.1 Beta | [Provider #56 — `aoa`](https://github.com/Herbertofury/JetSetCraft/actions/runs/41356358302/job/124997134536): build, 28 GameTests and dedicated-server readiness/save/shutdown |
| Aether + Iron's Spells + AoA + Cataclysm | [Provider #56 — `adventure_all`](https://github.com/Herbertofury/JetSetCraft/actions/runs/41356358302/job/124997134611): build, 28 GameTests and dedicated-server readiness/save/shutdown |

### Activity and arena test coverage

| Alpha.3 marker | Scope |
|---|---|
| `activity_progression` | Cross-mode pre-house earnings, once-only transfer, training, receipts, account persistence and schema migration |
| `activity_tag` | Actual Boombox preview and launch, source actors, owner attack hooks, six objectives and bystander/replay denial |
| `activity_safety` | Stable random invitation, stale context rejection, three-hit Showdown, orphan/missing actor and anchor removal handling |
| `activity_hill` | Contested capture rejection, physical presence at three hills and 480 real uncontested ticks |
| `activity_dimension_return` | Selected provider versions and actual Aether/Precasia → circuit → exact source return, with unchanged Minecraft/ride state |
| `rush_arena_catalogue` | Live profile loading, optional gating, fallback materials, authored map bounds and forbidden payload rejection |
| `rush_arena_rules` | Six-room completion/Encore, three artifact slots, cache refresh and stale draft denial |
| `rush_arena_session` | Real manager route choices, generated passable connection, actual owned rivals, shared settlement and exact return |
| `rush_spell_relay` | Provider-absent behavior, or the real native Iron Firebolt collision, bounded damage, ownership, virtual defeat and cleanup |

The route rules/session tests use explicit fixture-clock compression for long arena durations. They do not speed up the real-client audit. The graphical audit shows a real Boombox activity preview and a genuine three-choice first route, selects that route, enters the connected-arena travel phase and returns normally. Arena combat, objective completion and settlement are established by GameTests; the client audit does not show a cleared room or a reward screen.

The provider-present proofs passed with the actual source mods loaded. The Iron's and `adventure_all` jobs report native Firebolt admission, collision, bounded virtual damage, foreign-owner rejection and cleanup. The Aether/AoA jobs report actual `aether:the_aether` and `aoa3:precasia` departure and exact source return; the combined job reports both dimensions. Provider-absent configurations separately pass the clean unavailable paths. Full native spellbooks, arbitrary foreign capabilities, every modded mob and long human sessions remain outside these results. See [Compatibility](https://github.com/Herbertofury/JetSetCraft/wiki/Compatibility#rival-rush-dimensions-rivals-and-the-firebolt-relay) for the precise adapter contracts and known upstream messages.

## Versioned verification evidence

The published **v0.3.0** release has **nine** GameTests, as recorded in its [release evidence](https://github.com/Herbertofury/JetSetCraft/blob/main/docs/RELEASE_EVIDENCE_0.3.0.md). The historical **0.4.0-alpha.1** checkpoint added six Rival Rush tests, for fifteen; its [receipt and original captures](https://github.com/Herbertofury/JetSetCraft/tree/main/docs/verification/rival-rush) are preserved. The historical **0.4.0-alpha.2** source added four crew-house/integration tests, bringing that checkpoint to **nineteen**. The **alpha.3** activity/arena implementation adds nine tests, bringing the verified total to **28**, with its completed evidence linked above. Development evidence does not change the published stable release's version or history.

### Historical alpha.2 checkpoint

The recorded alpha.2 candidate is [`d5ecadd`](https://github.com/Herbertofury/JetSetCraft/commit/d5ecadd0651c35fdffb505fa2790bda718e830db), tested through PR integration [`b9bcec8`](https://github.com/Herbertofury/JetSetCraft/commit/b9bcec8258e164aa41af5838a89df8aac04bd7c7). [PR #40](https://github.com/Herbertofury/JetSetCraft/pull/40) merged as [`b852dfc`](https://github.com/Herbertofury/JetSetCraft/commit/b852dfce9b17eadbb29699ee8d5d50e2c6ab8101), whose Git tree is identical to that tested integration. The [crew-house verification receipt](https://github.com/Herbertofury/JetSetCraft/tree/main/docs/verification/crew-house) records those source identities, the binary, workflow artifacts and captures.

| Configuration | Completed evidence |
| --- | --- |
| Base Forge 1.20.1 / Java 17 | [Verify JetSetCraft #274: clean build, all nineteen GameTests, dedicated-server readiness and normal RCON shutdown, and the eight-screen real Minecraft client audit](https://github.com/Herbertofury/JetSetCraft/actions/runs/41350812751) |
| Alex's Mobs, Alex's Caves, Cataclysm, and all three together | [Provider matrix #51: four separate jobs, each passing its build, all nineteen GameTests and dedicated-server smoke](https://github.com/Herbertofury/JetSetCraft/actions/runs/41350812645) |

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
9. Built-in and custom graffiti NBT persistence, including the support position, plus automatic removal after the supporting wall is broken. The current fixture observes a complete supported cycle first, then requires native discard within 40 additional entity ticks; all nine configurations recorded `native_support_ticks=40`.

The six Rival Rush tests introduced in alpha.1 remain part of the current suite:

10. Deterministic three-choice upgrades, rejection of stale or repeated choices, bounded ranks and counters, the thirty-minute decision pause, and explicit Endless continuation.
11. All seven mob-card mastery thresholds, increasing milestone rewards, three starting-draft unlocks, distinct gang/species identities, saved album data, and unchanged Minecraft XP.
12. Recovery through the production return-journal path, exact original inventory and ride NBT, survival state and effects, preserved album progress, and rejection of incomplete, foreign-owner, nonfinite, or duplicate-slot journals before inventory mutation.
13. Actual session entry and exit across dimensions, loan equipment, exact original position/items/ride/XP restoration, and Forge capability invalidation/revival with the same durable ride data.
14. Real director-created actors and card pickups, independent player ownership, rejection of cross-session damage and duplicate claims, and suppression of event XP without changing unrelated Minecraft mob XP.
15. The real spray-can placement hook and points for a fresh wall, rejection of repaint farming, and authoritative upgrade actions that reject stale run IDs, stale offer versions, and replays.

The four alpha.2 crew-house/integration tests require these exact CI pass-marker IDs:

| Test marker | Production behavior exercised |
|---|---|
| **`crew_house`** | A real placed Boombox founds one house; physical facility registration and support-dependent upgrades charge the correct supplies; expansion preserves placed blocks, checks overlap before spending and acquires no forced chunks. |
| **`crew_missions`** | Dispatch freezes role-adjusted rewards and a saved deadline; stale, early, repeated and cross-player claims are rejected; the exact ready-time boundary settles supplies, member/crew XP and reputation once; cancellation grants no reward. |
| **`crew_persistence`** | World SavedData round-trips member/job identity, facilities, progress and run receipts; unknown schemas remain read-only; missing anchors retain the crew; relocation clears physical bindings while keeping purchased tiers; duplicate Rush settlement and training of busy members are rejected. |
| **`crew_rush`** | Actual house-to-Rush entry grants one chosen preparation alongside an independent earned album draft; the director creates real owned rivals, production tagging defeats them, and the real return path awards bounded crew progress once while preserving ordinary inventory/XP. |

Mission deadline checks use the persisted ready-time boundary deterministically. The integration test advances only its run's active clock before exercising actual actors and return; it does not claim a one-minute human play session or alter the shared world clock.

The player tests use uniquely identified Forge fake players and a real `ServerLevel` rather than mocked movement classes. Unique profiles prevent parallel GameTests from moving or mutating the same automation player. Isolated activity, arena and graffiti fixtures explicitly supply their required terrain and bounded temporary loading/ticking tickets; those fixture tickets do not add production chunk-loading behavior.

FakePlayer tests establish in-memory lifecycle behavior; they do not establish player-file durability. The real-client audit exercises the actual save acknowledgement and network entry/return paths. Forced process termination, storage failure, arbitrary third-party player capabilities, and a human thirty-minute or long Endless playtest are separate acceptance work; no passing gate above implies those have been exercised.

## Input transport acceptance

Client controls remain change-driven: any digital mask or analog movement change is transmitted immediately. While riding, dancing, or holding live movement input, unchanged state is refreshed every 5 client ticks instead of being resent every tick. A neutral inactive client sends its release state once and then stays quiet. The server independently sanitizes unknown bits/non-finite axes and clears stale armed input after 20 ticks, so the active heartbeat retains a four-times safety margin without needless 20-packets-per-second steady-state traffic.

## Dedicated-server smoke

CI launches Forge with `eula=true`, waits for the real ready line, and fails on lifecycle/mod-loading/fatal JetSetCraft errors. The produced JAR and logs are uploaded as workflow artifacts.

### Known upstream messages retained in passing logs

The final [`adventure_all` log](https://github.com/Herbertofury/JetSetCraft/actions/runs/41356358302/job/124997134611) contains two nonfatal upstream issues alongside its successful tests and server smoke:

- **AoA's remote update response:** Forge's version checker could not parse the response from the URL declared in AoA's `META-INF/mods.toml` (`Expected ':' at line 120 column 13`). This affected update information, not the tested arena or dimension data.
- **Iron's concentration-amulet recipe:** `data/irons_spellbooks/recipes/curios/concentration_amulet.json` referenced unregistered `irons_spellbooks:mithril_ingot`, so Minecraft rejected recipe `irons_spellbooks:curios/concentration_amulet`. That crafting recipe did not load.

The [Compatibility warning ledger](https://github.com/Herbertofury/JetSetCraft/wiki/Compatibility#known-upstream-messages-in-passing-provider-logs) identifies the exact resources and evidence. Passing integration results do not certify all provider recipes or features, and the upstream resources and version pins were not modified to conceal these messages.

## Real-client visual audit

Run `gradlew.bat -Djetsetcraft.visualAudit=true runClient --no-daemon` on Windows (or the wrapper equivalent elsewhere). The opt-in harness quick-loads its development world, creates a controlled inline-skate/HUD/painted-wall scene, captures the selector in persistent Free Paint mode and the custom painter, prints `JETSETCRAFT_VISUAL_AUDIT_PASS`, and exits cleanly. It is inert in ordinary launches.

The development CI also runs `./gradlew runClient -Djetsetcraft.rushVisualAudit=true --no-daemon` with a disposable `RivalRushAudit` world prepared by the [build workflow](https://github.com/Herbertofury/JetSetCraft/blob/main/.github/workflows/build.yml). It enters and leaves Rush through real network actions, compares the returned dimension, inventory and vanilla XP with its observed pre-entry fixture, and writes the setup, district HUD, empty-album and return-result captures.

After return, the crew audit uses an actual Charter on placed fixture blocks, founds a home through production actions, checks two real roster profiles, registers an operational workshop and dispatches a real timed job. It captures **Home, Crew, Facilities and Jobs**. The activity audit then adds **Activities** and **Arena route**, bringing the current total to **ten written 1280 × 720 screenshots**. The gate requires `JETSETCRAFT_RUSH_VISUAL_AUDIT_PASS`, `JETSETCRAFT_CREW_VISUAL_AUDIT_PASS`, `JETSETCRAFT_ACTIVITY_VISUAL_AUDIT_PASS`, the observed Rush fixture marker and all ten capture markers/files. The final [arena-circuit receipt and gallery](https://github.com/Herbertofury/JetSetCraft/tree/main/docs/verification/arena-circuit) preserve the reviewed captures; the earlier [eight-screen crew-house checkpoint](https://github.com/Herbertofury/JetSetCraft/tree/main/docs/verification/crew-house) remains historical evidence.

The album is empty in capture 03, captures 05–07 retain a hover overlay, and the job is still running in capture 08. These are the actual client states photographed. Captures 09–10 and the audit markers establish Boombox selection, the first arena fork, entry into travel and ordinary return. Forge tests cover real arena combat/clear/settlement and job deadline/claim behavior; the visual audit does not play a complete room or wait through a mission. This short automated run does not measure house progression balance, thirty-minute/Endless enjoyment, sustained performance, physical follower behavior or a complete human session.

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
