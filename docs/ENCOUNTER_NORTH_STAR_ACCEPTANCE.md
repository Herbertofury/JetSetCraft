# JetSetCraft Encounter Experience North Star — Operational Acceptance Scorecard

**Status:** Owner-approved aspirational vision / **proposed production and QA checklist**, created 2026-11-15. Nothing in this document is proof of a playable encounter, licensed audio, real visual effect, or measured performance.

**Canonical experiential vision:** [Encounter North Star — Beyond Minecraft](../wiki/Encounter-North-Star-Beyond-Minecraft.md). This scorecard is the *outside judge* for the owner's stated goal: **an encounter so breathtaking, fun, expressive and kinetic that it stops feeling like a Minecraft minigame and feels like an original premium Jet Set Radio / HYPERFUNK-class game**. Competitive comparisons are inspiration and quality goals, not assets to copy or achieved status claims.

## Encounter receipt: one copy per independently implemented event

| Field | Required evidence, not assumptions |
| --- | --- |
| Event identity | Stable event ID, gang(s), player role, locale/biome, genre of contest |
| Version and hardware | Exact Git SHA, Minecraft/loader build, modpack/profile, GPU/CPU, RAM, display refresh, graphics tier, recorded test settings |
| Event-state lifecycle | Eligible → gated reveal → playable encounter → measured payoff → success/fail/retry → full cleanup |
| Source assets | Original art, models, rigs, animation files, palette/FX graph; attribution and visual QA screenshots |
| Sound | Music ID/ISRC/master edition, artist/label rights receipt, original vs licensed cues, beat grid/cue sheet, streaming/decoding strategy and gang/event-only routing |
| Rival identity | Original choreography, crew roles, entrance, unique movement, ability interactions, player counterplay, positive/negative outcome reactions |
| Quality evidence | Exact-build game captures: real-time footage with input timing overlay, frame-time trace and game audio, not only still shots or edited trailer |
| Multiplayer and safety | Server authority, owner/control consistency, player opt-out, network traces, accessibility, protected-world check, full session teardown |
| Reviewer verdict | Outside playtesters, date, comfort/performance baseline, repeat-intent comments, what failed and retest results |

## Proposed milestone gates — do not tick from documentation

- [ ] **G0: protected-world baseline.** Existing vanilla/modded features, entity identity, user builds/inventories, protections, controls, persistence and multiplayer are regression tested before the new encounter starts.
- [ ] **G1: core movement is independently fun.** Movement, rail/grind, boost, transfers, air handling, landing, wall/skate collisions and retries feel skillful with default camera and *without* sound or effects. No forced animation lock or contact snapping across mobs/vehicles.
- [ ] **G2: ensemble characters look first-party to the game.** Complete original gang entrance, skating/riding, dance, taunts, reactions, win and fail cycles; grounded foot/hand contact, no warped rigs, no silent freezes or cloned identical crew loops. Tested at fast and slow motion.
- [ ] **G3: stage ignition is a seamless transformation.** Light, color, FX, typography, crowd/route read, graffiti and course overlay respond to the *actual* venue; no destroyed/overwritten original content and no mandatory shaders. Different graphics tiers preserve gameplay parity.
- [ ] **G4: gameplay reacts musically.** Immediate local feedback + server-confirmed score/outcome; stems/phrase events and musical payoffs only for real player/crew interactions, no input delay for quantization, no unowned soundtrack or accidental general playlist.
- [ ] **G5: conflict is exciting and fair.** Detectable tell → readable action → meaningful counterplay → earned combo and payoff; AI respects source gang control, removed powers, accessible alternatives, and no infinite "surprise after surprise" triggers.
- [ ] **G6: unforgettable and replayable.** At least two distinct playtestable routes or viable tactical responses; source of delight is skill expression, choreography, emergent music and variety, not visual overload, grind penalties or deceptive random rewards.
- [ ] **G7: accessibility and recovery.** Mute, reduced motion/flash, alternative cues, skip/decline, save/rejoin/reload, simultaneous players, spectator behavior and explicit defeat/rematch rules all keep the event understandable and the world intact.
- [ ] **G8: actual performance, no evidence laundering.** Measure GPU/CPU, p50/p95/p99 frames, 1% lows, input latency, server tick and network behavior on named baseline hardware. A proposed initial focus is stable 60fps/16.7ms in representative non-degraded 1080p play, but final hardware-tier targets depend on real device data, not a blanket guaranteed minimum. Find and fix spikes without deleting approved features or hard-capping content.
- [ ] **G9: blind outside judge.** Share live playable test builds, capture first reactions, make note of "that felt like a premium standalone skating/music game" vs "that felt like a Minecraft mod" complaints, and collect separate delight, readability, control, performance and replay scores. Provisional *aspiration* median ≥9/10 standout delight and ≥9/10 cohesive premium-game feel; threshold, sample and pass/fail to be calibrated after real tests.
- [ ] **G10: evidence-gated release.** Link exact SHA/JAR, successful automated/real GameTests, client/dedicated-server captures, rights receipts and accessibility/regression data; only then update shipped/release labels. No "fully implemented" language from a wiki entry or a Spotify ID.

## Mandatory visual/behavior review: "Does this disappear into JetSetCraft?"

Review in a repeatable captured session, with controls enabled and audio on **and** muted:

| What to compare | Automatic fail | Pass pattern to verify |
| --- | --- | --- |
| World identity | Vanilla world broken/replaced, invented buildings in protected chunks, materials not restored | Familiar Minecraft space temporarily stage-lit and reinterpreted, clean exit to intact original state |
| Rider feeling | Slide/clip teleport, generic Minecraft sprint-only motion, interrupted input | Weighty, fast, readable boost/grind/skate with controllable fluid transitions |
| Rival crew | Recolored vanilla humanoid with same loop and a track | Unique animation arcs, recognizable silhouettes, rival strategies, crew choreography and original graffiti |
| Music | Spotify metadata pretends audio is shipped; generic Rush track plays anywhere | Legally imported exact recording where permitted, event/crew scoped, validated real beat events and cleared fallback |
| Wow moment | Non-interactive cinematic, screen blinding, artificially forced payoff | Earned real-game crescendo where a successful action drives reaction, musical peak and distinct audiovisual payoff |
| Replay | Static scripted once-only show and no skill improvement | Routes, timing, faction tactics, score/style, progression and optional discovered variation reward real mastery |
| Multiplayer and inclusion | Unsynced opponents, inaccessible cues, duplicate rewards, unwanted camera hijack | Server-authoritative events and accessibility parity, clean return even across disconnect/failure |

## Additional production trial: Neon Labyrinth mode (working title)

The owner's new **platformer + Metroidvania + roguelike with end bosses** mode has its own [issue #65](https://github.com/Herbertofury/JetSetCraft/issues/65), [wiki](../wiki/Neon-Labyrinth-Platformer-Metroidvania-Roguelike.md) and [technical handoff](modes/NEON_LABYRINTH_DESIGN.md). Do **not** mark it complete based on existing Rival Rush headliners.

- [ ] First-time testers complete real platforming routes with satisfying control, safe landings, checkpoints and fun deliberate obstacles (not merely survive spawn quotas).
- [ ] A player encounters a meaningful locked side route, earns a **real traversal ability**, returns through a previously visited area and now opens a shortcut/secret.
- [ ] A seed-controlled run offers different safe choices across attempts, with every ability-state graph valid and a genuine finish.
- [ ] One thematic stage boss and the **actual final boss** are defeated through readable, movement-centric, multi-phase interactions in native gameplay.
- [ ] Win/fail/retry/restart/rejoin/dedicated server/cleanup do not duplicate unlocks, delete Minecraft inventory, strand players, or regress old Rival Rush.
- [ ] Exact-build media demonstrates **full level → unlock → backtrack → stage boss → final boss → safe return**, with outside assessments of movement fun, map comprehension, spectacle and replay intent.

## Character special boss gate: Hakken Horse / Gabberina's

- [x] Owner-confirmed Hakken Horse and exact `GAB-HORSE-MUSIC-001` **“My Little White Pony” — MCR-T & horsegiirL** documented in the [boss profile](../wiki/Hakken-Horse.md), explicitly **event-only**; recording still rights-restricted.
- [ ] Real client test that a Gabberina-owned horse mob gang may spawn the **separate, original human Hakken Horse boss** with distinct movement/gabber/hardtekk choreography, *without cloning/removing source horse entities*.
- [ ] Test losing full horse mob gang control suppresses the signature boss and strips related borrowed abilities; stale ownership never triggers a boss or invalidates earned defeat.
- [ ] Inspect original costume from multiple angles, fair mechanics, reactive but legally cleared boss music, accessibility, multiplayer, exit cleanup and complete original world restoration. A Spotify link or nonplayable JSON does not pass this gate.

## Required event trials to prioritize

1. **Gabberina's surprising rival encore** after legitimate control-gated event, especially original high-energy choreography and distinct jumpstyle/gabber/hardtekk identity.
2. **Rare Cumbias × Bananettas Décalco Mambo**, preserving the two owner-confirmed *event-only* music selections and independent Ocarina of Cumbia event arbitration.
3. **Mortal Kumbia** staged music/reactive fight + dance experience and **night rave** spectacle with readable gameplay and reduced-motion mode.
4. **Signal '81**, **Phonkistas**, **Bananettas**, **Honkistas**, **Hardtekkas**, **Footworkas**, **MoccaSins**, creature-specific boss gangs, and terrain-aware circuits: repeat quality standards **without reskins or copy/paste cast routines**.

## Evidence status at creation

- [x] Owner-authored high-level vision captured in GitHub source and cross-linked in the wiki.
- [x] Specific visual/audio/movement/AI/experience/compatibility quality acceptance checklist authored.
- [ ] Signature high-fidelity encounter with actual native client footage exists and is judged.
- [ ] Runtime gang music filtering, sample-accurate cues, authorized soundtrack files and scene orchestration exist.
- [ ] Repeated blind playtests meet calibrated emotional, mechanical and technical thresholds.
- [ ] External comparison confirms a level of actual playing quality that can credibly compete with Jet Set Radio and HYPERFUNK.

**No automatic approval or completion from these checkboxes**; the released runtime must be independently verified.
