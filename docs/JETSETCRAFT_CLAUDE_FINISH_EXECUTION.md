# JetSetCraft — Claude Finish Execution Contract

> **Purpose:** Hand this file to Claude and have it continue JetSetCraft as a real implementation project, not as a planning exercise. Claude should finish the mod into the polished, cohesive, vanilla-feeling street-movement / skating / graffiti / gang-world experience described here while preserving everything already verified.

## 0. Objective

Finish **JetSetCraft** as the best possible Forge 1.20.1 street-movement mod: skating, BMX, scooter, hoverboard, grinding, parkour, tricks, graffiti, dances, boomboxes, gangs, deep Street Level/skill-tree/ride-mastery progression, hangouts, challenges, per-gang reputation/Signature Techniques, posse/chapter systems, and installed-mod creature compatibility should feel like one coherent Minecraft-native system rather than separate minigames.

The user wants the result to feel like something Mojang could have shipped in an ambitious movement/adventure update: readable, tactile, immediate, low-friction, and visually integrated with vanilla HUD language — while still carrying the depth and style of Jet Set Radio / Bomb Rush Cyberfunk / aggressive skating culture.

For this project, the user states that they already hold **full studio authorization to reuse and ship Jet Set Radio / Jet Set Radio Future / Bomb Rush Cyberfunk source-game material**, including models, textures, graffiti/art, music, SFX, dances/animations, props, and other relevant game assets. Treat that user-provided authorization as an explicit project permission boundary rather than downgrading these sources to inspiration-only. Preserve per-asset provenance/credits and any written authorization evidence supplied with the project, but do not refuse to integrate an otherwise useful covered asset merely because it originated in one of those games.

A core social fantasy is that **any ordinary AI mob—vanilla or modded—can be approached as a real participant in street culture**. While the player is wearing skates/Street Gear, a dedicated rebindable Challenge modifier plus normal interaction should open a compact Minecraft-native interaction flow for Dance Battle, Skate-Off, and other context-valid challenges. A mob that needs skates should receive safe temporary loaner Street Gear/ride presentation for the activity and have its exact prior state restored afterward. This must extend source mobs rather than replace them.

The project is not complete merely because it compiles. It is complete only when the exact current build is exercised in real Forge runtime and the final user-facing behavior matches this contract.

## 1. Canonical project identity and continuity

- Repository: `https://github.com/Herbertofury/JetSetCraft`
- Public production baseline: `main`
- Verified release baseline: `v0.3.0`
- Minecraft: **1.20.1**
- Forge: **47.4.23** verification target
- Java: **17**
- Local canonical project historically: `C:\Users\Owner\Desktop\JetSetCraft`
- Canonical Drive project folder: `https://drive.google.com/drive/folders/1npHf1VwOj-tybm791XShz2IvY8LmjjUU`

### Critical continuation warning

Do **not** blindly reset to public `main` and do not revive random historical branches.

There is newer Sept. 30 hardening/checkpoint work that may not yet be fully published to `main`. Recover and compare the newest verified checkpoint first. The latest confirmed recovery record found during this handoff is:

- Recovery sheet: `JetSetCraft-2026-09-30-graffiti-hand-context-boundary-recovery`
- Recovery sheet ID: `17o_3GmwT8aNNvM6nPeXldEezGq7BvqZ7TPxCxg0wxaM`
- Base tree: `7cf369bd099689e586583fc169de5247a2b3c7a3`
- Local verification commit: `c344c5233c254044ab4b9e379acb4475c8f588fd` — verification only, not necessarily canonical GitHub state
- Candidate tree: `b636b0a55f9b3a47c360f812a554683020a9d517`
- Targeted verification: PASS for hand/context behavior, mutation controls, graffiti boundary, input boundary, lifecycle boundary
- General verification: PASS for gameplay contract, Java syntax, wiki, wrapper portability, JSON, Python compileall, `git diff --check`, patch round-trip
- Full runtime gate at that checkpoint: **UNRESOLVED** because Gradle 8.8 distribution/cache was unavailable in that environment

Before changing code, compare the actual current worktree/repo state against this recovery state and any newer authoritative checkpoint. Preserve newer verified fixes.

## 2. Non-negotiable engineering invariants

1. **Never solve problems by removing features, reducing content, lowering fidelity, skipping validation, or weakening compatibility.**
2. **Performance and quality must improve together.** Faster-by-doing-less is failure.
3. **Preserve all verified v0.3.0 behavior unless explicitly replaced by a superior implementation with equivalent-or-better capability.**
4. **Fix shared causes, not symptoms.** If several defects share architecture, repair the architecture.
5. **Server authority remains canonical** for movement, scoring, graffiti selection/custom payloads, Street Gear, gangs, progression, challenge state, and persistence.
6. Clients may send bounded input and render sanitized snapshots; never trust client score/state authority.
7. **The physical ride `ItemStack` remains the source of truth** for equipped ride gear. Do not replace this with fake client state or disposable mount entities.
8. Neutral directional input means neutral. Never manufacture camera-forward motion from stale momentum.
9. Water/lava/swimming must yield full movement authority back to Minecraft.
10. Ordinary ride animation may own lower-body presentation but must not steal arms/hands/head from vanilla combat, TACZ, Better Combat, Epic Fight, held-item use, etc.
11. Optional integrations must remain optional and isolated. Missing optional mods may not stop JetSetCraft from loading.
12. Do not replace source mobs to create gang members. Gangification is additive, reversible, and attached to the original entity.
13. No force-loaded gang chunks.
14. No fake buttons, fake progress, decorative menus, placeholder systems, or tests that bypass production wiring.
15. Every major failure discovered during implementation should become a regression test or reusable hardening rule.
16. Do not claim completion while any accepted task below is blocked or unverified.
17. **The player owns JetSetCraft HUD placement.** Default auto-layout must coexist with vanilla and mod overlays, while a full client-side layout editor lets each user move/scale/anchor/compact/hide JetSetCraft HUD modules without a server forcing cosmetic placement.
18. **Never steal another mod's interaction.** Universal mob challenges are entered only through an explicit rebindable Challenge modifier/context action; ordinary right-click/use behavior remains source-owned when that modifier is not active.
19. **Never destroy mod-owned AI state.** Do not clear goal selectors, wipe Brain memories, replace navigation implementations, erase equipment, or replace the entity to make it participate in a challenge. Use a bounded reversible JetSetCraft activity lease/controller and restore the original mob state.
20. **Temporary challenge gear is a loan, not loot.** Loaner skates/Street Gear may never enter normal inventories, drops, trades, loot tables, equipment persistence, or duplication paths and must be removed/restored on every exit path.
21. **Universal means default-eligible.** Every real AI `Mob` should be challengeable by default; an exclusion requires a concrete runtime safety/ownership reason (dead/removed entity, incompatible scripted state, no safe activity space, explicit provider opt-out, etc.) and the player must receive a useful reason instead of a silent failure.
22. **Authorized JSR/BRC assets are first-class production inputs.** The user's stated authorization covers direct reuse/integration of relevant Jet Set Radio-family and Bomb Rush Cyberfunk game assets. Do not replace licensed source assets with lower-fidelity approximations merely to avoid using them.
23. **Provenance is mandatory, not a blocker.** Track source title, source path/archive, asset type, conversion/transformation, author/artist metadata when known, and the project authorization record for every imported licensed asset; provenance/credits must survive packaging and updates.
24. **Core gameplay must not depend on a brittle giant-asset load.** Heavy licensed media may be packaged as a first-party JetSetCraft content/resource pack or split release artifact when that improves startup/memory/patching, but it must install and activate as a seamless supported part of the official JetSetCraft release rather than a manual scavenger hunt.
25. **Music ownership stays user-controlled.** Radio playback, playlist state, volume, shuffle/repeat, favorites, station choice, and whether licensed music plays are client/user preferences unless a server-owned world event intentionally broadcasts local in-world audio.

## 2A. User-authorized Jet Set Radio / Bomb Rush Cyberfunk production asset scope

Claude must treat the following as **authorized production material for this project**, based on the user's explicit statement that permission has already been established with the relevant studios/rightsholders:

- **Jet Set Radio** and authorized official variants/releases;
- **Jet Set Radio Future** and authorized official variants/releases;
- **Bomb Rush Cyberfunk** and authorized official variants/releases;
- studio-provided/exported/source packages, official asset bundles, or game-install assets that fall under that permission;
- covered **character/NPC/gear/vehicle/prop models**, rigs, textures/materials, graffiti/art, particles/VFX, SFX, music, dances/emotes/animations, UI/icon motifs, environmental props, and other directly useful game content.

The implementation rule is **reuse/adapt when superior, not mimic when the real authorized asset is available**. Convert assets into Minecraft/Forge/GeckoLib/resource-pack-friendly representations while preserving the source character, timing, silhouette, animation intent, audio quality, and artistic identity as closely as Minecraft's renderer/gameplay permits.

### Rights/provenance operating rule

The authorization statement in this handoff is user-provided project authority. Claude should not silently relabel these assets as "reference only," strip them because they are copyrighted, or replace them with generic substitutes. At the same time, maintain a durable manifest such as `docs/LICENSED_ASSET_MANIFEST.*` / `assets/licensed_manifest.*` that records enough provenance to update, credit, audit, and reproduce the imported content later. If the project contains the studio permission/grant itself, retain/link it in the private project evidence location without exposing confidential text in public builds unless the user wants that published.

### Asset-quality rule

- Prefer direct source extraction/export from the highest-quality authorized source available.
- Avoid lossy re-ripping when a cleaner source/export exists.
- Preserve original animation timing/keyframe intent before adapting scale/rig coordinates.
- Preserve high-quality source audio; transcode only as needed for Minecraft playback/distribution and avoid destructive repeated transcoding.
- Preserve texture alpha/emissive/material information where Minecraft/GeckoLib can represent it.
- Do not collapse distinct dances, music tracks, props, characters, or skins into a small sample just to reduce work.
- Deduplicate identical assets by content hash while retaining all title/credit aliases in the manifest.
- Asset ingestion must be reproducible so a future refresh can re-import updated source packages without hand-editing hundreds of files.

## 3. Current verified v0.3.0 capability that must survive

JetSetCraft already has a substantial finished baseline. Preserve all of it while improving it:

- Six persistent ride styles:
  - Neo Inline Skates
  - Neon Quad Skates
  - Street Deck
  - Street BMX
  - Flux Hoverboard
  - Neon Street Scooter
- 24 named tricks
- 28 dance moves
- Flow, combo multiplier, boost reward, landing grades
- exposed-edge / fence / wall / pane / vanilla rail / Forge rail grinding
- optional Create 6.0.8 track grinding bridge
- rhythmic skate strides, powerslides, wall kicks, wind redirects, sprint kickoffs, ledge vaults
- hands-free ride slot
- combat-safe lower-body animation ownership
- 139-entry graffiti gallery
- 16x10 custom painter
- Free Paint
- 16 paint balloons
- six-face splash placement
- repaint-in-place
- bounded per-chunk graffiti safety
- leg-baked rollerblades
- Street Gear for compatible vanilla/modded mobs
- Boombox + mob-head gang tuner
- data-driven gangs and head mappings
- 80 original gang entrance stingers
- compact Boost/Flow HUD
- dynamic camera/FOV controls
- reduced-motion controls
- Forge GameTests, validators, dedicated-server smoke, real-client visual audit infrastructure

## 4. Exact skate/trick reference the user meant: Dream Burst Spirit Vector

This is important. The user was **not** referring to Better Flight.

The intended reference is **Dream Burst Spirit Vector** by hama Industries:

- Project: `https://modrinth.com/mod/spirit-vector`
- Source: `https://github.com/HamaIndustries/spirit-vector`
- JetSetCraft already has an authorized MIT snapshot / adaptation lineage.
- Imported revision recorded by JetSetCraft: `623e7df026788add9737ea508b65baf80f66623d`
- License: MIT, preserved under `third_party/spirit-vector/LICENSE`

JetSetCraft currently records selected Spirit Vector ideas such as movement-state boundaries, kickoff, vault detection, wall-plane reuse protection, coyote-time and parkour concepts.

### The specific UI / “wing boost” behavior to study

The key client-side references are:

- `src/client/java/symbolics/division/spirit_vector/render/SpiritVectorHUD.java`
- `src/client/java/symbolics/division/spirit_vector/mixin/client/InGameHudMixin.java`

Spirit Vector:

- builds movement-earned **Poise**;
- exposes wings / feather-style feedback at high Poise;
- derives up to ~10 feather indicators from momentum;
- integrates the resource readout directly around vanilla survival HUD space;
- specifically renders Poise **above the vanilla food row** instead of creating a detached generic RPG overlay.

### JetSetCraft HUD direction

Do **not** copy Spirit Vector branding, fantasy runes, or exact art identity.

Do adapt the **interaction language**:

- compact icon/pip resource display aligned with vanilla hearts/armor/food/bubbles;
- clear visual depletion/recharge;
- no giant floating sci-fi bar unless explicitly enabled as an optional style;
- use JetSetCraft-owned iconography appropriate to each ride/resource;
- readable at Minecraft GUI scales;
- no conflict with armor, food, air, mount health, modded status rows, or accessibility settings;
- reserve room intelligently when vanilla HUD elements shift;
- dynamically coexist with Forge-registered and adapter-known mod overlays instead of assuming the vanilla HUD is the only occupant;
- expose full per-user placement control: drag/drop, anchor, X/Y offset, scale, spacing, stack direction, compactness, opacity, context visibility, lock, reset, and Auto-Avoid versus Fixed placement;
- persist layout per client/user and normalize it across GUI scale/resolution changes so a custom layout does not drift off-screen;
- publish JetSetCraft overlay bounds/anchors through a lightweight optional client integration surface so other mods can cooperate without hard dependencies;
- reduced-motion mode must remove pulsing/rapid effects without reducing information;
- Boost and Flow remain distinct resources with instantly understandable meaning.

This is a first-class acceptance requirement, not optional polish.

## 5. Existing code anchors Claude should inspect first

Known current owners include:

- `src/main/java/com/herberto/jetsetcraft/client/ClientEvents.java`
- `src/main/java/com/herberto/jetsetcraft/client/state/ClientRideState.java`
- `src/main/java/com/herberto/jetsetcraft/network/S2CStatePacket.java`
- `src/main/java/com/herberto/jetsetcraft/network/C2SInputPacket.java`
- `src/main/java/com/herberto/jetsetcraft/movement/JetSetMovement.java`
- `src/main/java/com/herberto/jetsetcraft/movement/RideMotion.java`
- `src/main/java/com/herberto/jetsetcraft/movement/MovementTuning.java`
- `src/main/java/com/herberto/jetsetcraft/movement/TrickCombo.java`
- `src/main/java/com/herberto/jetsetcraft/movement/TrickCatalog.java`
- `src/main/java/com/herberto/jetsetcraft/movement/DanceSystem.java`
- `src/main/java/com/herberto/jetsetcraft/gang/GangChallengeController.java`
- `src/main/java/com/herberto/jetsetcraft/gang/GangRegistry.java`
- `src/main/java/com/herberto/jetsetcraft/gang/GangActorFactory.java`
- `src/main/java/com/herberto/jetsetcraft/blockentity/BoomboxBlockEntity.java`
- `src/main/java/com/herberto/jetsetcraft/mob/MobRideRig.java`
- `src/main/java/com/herberto/jetsetcraft/mob/MobRideRigResolver.java`
- `src/main/java/com/herberto/jetsetcraft/graffiti/*`
- `src/main/java/com/herberto/jetsetcraft/client/screen/GraffitiSelectorScreen.java`
- `src/main/java/com/herberto/jetsetcraft/client/screen/GraffitiEditorScreen.java`
- `tools/validate_gameplay_contract.py`
- `tools/validate_premium_polish.py`
- `tools/validate_assets.py`
- `wiki/Testing-and-Verification.md`
- `docs/GANG_WARS_BOOMBOX_MOB_ATLAS_MASTER_SPEC.md`
- `wiki/Gang-Wars-Boombox-and-Mob-Atlas.md`
- `wiki/Hangouts-Territory-and-Reputation.md`
- `wiki/Player-Chapters-Posse-and-Mob-Archetypes.md`
- `wiki/Research-Street-Movement-and-Rides.md`

## 6. Execution / resume behavior

- Continue from the earliest unchecked task whose dependencies are satisfied.
- Do not rewrite this plan from scratch.
- Preserve proof notes and checked tasks unless later work invalidates them.
- If later work invalidates a completed task, reopen that exact task ID.
- Work in bounded coherent windows of roughly 6-12 leaf tasks.
- After two materially unchanged failed attempts, change strategy instead of retrying the same route.
- Use targeted changed-path verification during iteration and broad gates at convergence.
- A blocked task remains unchecked using:
  - `BLOCKED: <exact reason>`
  - `NEXT: <exact recovery action>`
- Do not stop after writing another plan. This file is the plan. Implement.

---

# PHASE A — Recover the true current source and close hardening gaps

- [ ] **T001** · Resolve the authoritative current worktree and compare it against GitHub `main`, `v0.3.0`, the Sept. 30 recovery sheet, and any newer verified checkpoint.
- [ ] **T002** · Preserve every newer verified hardening change instead of resetting to an older public tree.
- [ ] **T003** · Record exact starting branch/commit/tree hash and dirty-state boundaries.
- [ ] **T004** · Re-run the lightweight deterministic validators that do not require Minecraft runtime.
- [ ] **T005** · Repair any regression in gameplay-contract, Java syntax, wiki, JSON, Python, or diff validation without weakening tests.
- [ ] **T006** · Complete the previously unresolved Gradle/Forge runtime toolchain path instead of treating missing distribution/cache as task completion.
- [ ] **T007** · Run `gradlew.bat clean build --no-daemon` or platform equivalent using the actual project wrapper.
- [ ] **T008** · Run `gradlew.bat runGameTestServer --no-daemon` and inspect fresh logs.
- [ ] **T009** · Run the dedicated-server readiness gate with the exact built JAR.
- [ ] **T010** · Confirm all Sept. 30 input/graffiti/lifecycle boundary hardening survives real Forge runtime.
- [ ] **T011** · Reconcile any currently open hardening PRs/branches by semantic content, not age.
- [ ] **T012** · Create a coherent source checkpoint before expanding gameplay systems.
- [ ] **G001 · GATE** — Current canonical source is resolved, current hardening is preserved, and the baseline builds/runs under real Forge.

# PHASE B — Make Boost / Flow HUD feel truly vanilla-native

- [ ] **T013** · Read JetSetCraft’s current HUD renderer in `ClientEvents` and map every displayed state: ride style, grind mode, Boost, Flow, combo, rank, trick/dance/landing labels.
- [ ] **T014** · Read Spirit Vector’s `SpiritVectorHUD` and `InGameHudMixin` as the explicit prior reference.
- [ ] **T015** · Design JetSetCraft-owned vanilla-style Boost iconography that can occupy survival-HUD space without copying Spirit Vector art.
- [ ] **T016** · Design JetSetCraft-owned vanilla-style Flow iconography that remains visually distinct from Boost.
- [ ] **T017** · Replace the generic detached colored-pip presentation with a compact survival-HUD composition inspired by vanilla hearts/food/armor semantics.
- [ ] **T018** · Keep `H` instant-toggle behavior and make visibility preference persistent when appropriate rather than session-fragile.
- [ ] **T019** · Ensure HUD placement dynamically avoids armor, food, air bubbles, mount health, experience, offhand, boss bars, subtitles, chat, and modded overlay collisions where Forge exposes positioning information.
- [ ] **T020** · Ensure GUI scale 1 through large GUI scales remain readable with no clipping.
- [ ] **T021** · Ensure ultrawide and narrow window layouts remain anchored correctly.
- [ ] **T022** · Use bounded animation for charge/depletion/landing feedback; reduced-motion mode must preserve all information without pulsing/rapid motion.
- [ ] **T023** · Keep trick/dance/landing feedback short, readable, and visually subordinate to vanilla gameplay.
- [ ] **T024** · Preserve HUD correctness while swimming or while JetSetCraft movement yields to other systems.
- [ ] **T025** · Preserve HUD correctness with TACZ / Better Combat / Epic Fight / vanilla weapon presentation active.
- [ ] **T026** · Add config for HUD anchor/compactness only if needed; defaults must already feel correct.
- [ ] **T027** · Add deterministic HUD state tests where feasible and real-client visual-audit scenes for Boost empty/partial/full and Flow ranks.
- [ ] **T028** · Capture real-client screenshots at representative GUI scales for final acceptance.
- [ ] **G002 · GATE** — JetSetCraft’s in-game status UI reads like a natural Minecraft survival HUD extension while conveying Boost/Flow/style immediately.


## PHASE B2 — HUD coexistence, collision avoidance, and full user layout control

The default HUD must look intentional with vanilla, but compatibility cannot depend on every other mod choosing a different corner. Treat JetSetCraft HUD modules as cooperative overlays with an automatic layout mode **and** a complete user-owned editor.

- [ ] **T221** · Audit the actual Forge 1.20.1 overlay/render APIs and JetSetCraft's current HUD hooks; prefer public overlay ordering/events over brittle injections, and document any unavoidable mixin boundary before extending it.
- [ ] **T222** · Introduce one shared client-side JetSetCraft HUD layout owner for Boost, Flow, combo/rank, trick/landing feedback, challenge prompts/status, and future modules; do not let each widget invent private coordinates.
- [ ] **T223** · Model each JetSetCraft HUD element as a bounded module with an anchor, measured bounds, preferred/default position, priority, visibility policy, scale, and layout mode so collision handling is deterministic.
- [ ] **T224** · Implement **Auto-Avoid** layout that accounts for current vanilla HUD rectangles and Forge-registered overlays when discoverable, then shifts/stacks JetSetCraft modules instead of drawing through occupied space.
- [ ] **T225** · Add an adapter mechanism for important HUD mods whose occupied regions cannot be discovered generically; adapters must be optional, version-gated, fail-soft, and must never hard-classload absent mods.
- [ ] **T226** · Expose JetSetCraft's own current overlay bounds/anchors through a lightweight optional client integration API/event so cooperating mods can avoid JetSetCraft in return without depending on JetSetCraft internals.
- [ ] **T227** · Build a polished **HUD Layout Editor** accessible from JetSetCraft client settings with live in-game-style preview and direct drag/drop positioning.
- [ ] **T228** · Give the user full per-module controls for anchor, X/Y offset, scale, spacing, stack direction, compactness, opacity, visibility/context rules, lock/unlock, and reset-to-default.
- [ ] **T229** · Support at least **Auto-Avoid**, **Fixed/User Position**, **Compact**, and **Hidden** behavior where sensible; an explicit user-fixed position wins over automatic movement except for hard screen-bound/clipping safety.
- [ ] **T230** · Keep the editor Minecraft-native: pixel/safe-zone snapping, keyboard nudging, clear selection outlines, Reset This Module, Reset All, and a one-click return to the recommended vanilla-style layout.
- [ ] **T231** · Persist HUD layout **per client/user**, not as a server-authored cosmetic coordinate. Allow optional named local layout profiles if the existing config architecture supports them cleanly; server configuration may control gameplay information availability but not silently relocate a user's cosmetic HUD.
- [ ] **T232** · Store positions in anchor-relative/safe-area form so layouts survive GUI-scale changes, window resize, fullscreen/windowed transitions, ultrawide/narrow aspect ratios, and resolution changes without drifting off-screen.
- [ ] **T233** · Recompute expensive collision/layout decisions only when relevant state changes (GUI scale, resolution, overlay visibility/registration, context row, user edit), not through broad per-frame registry or screen scans.
- [ ] **T234** · Preserve mouse/keyboard/focus ownership: the ordinary HUD never intercepts input, the editor has correct focus/tab/escape behavior, and reduced-motion/accessibility settings remain honored.
- [ ] **T235** · Create a compatibility matrix covering vanilla survival rows plus representative Forge overlays such as nutrition/status extensions, minimap/info overlays, combat/status bars, Curios/accessory UI, mount/boss rows, subtitles/chat, and at least several real popular 1.20.1 HUD mods present in the test environment.
- [ ] **T236** · Runtime-prove Auto-Avoid and manually fixed layouts at multiple GUI scales/resolutions with simultaneous mod overlays; restart the client and verify the exact user layout survives with no clipping, jitter, overlap thrash, or per-frame layout churn.
- [ ] **G018 · GATE** — JetSetCraft HUD is cooperative by default and fully user-owned when customized: it auto-avoids vanilla/modded UI where possible, exposes integration bounds, persists user placement, never steals input, and remains stable across scale/resolution/context changes.

# PHASE C — Finish the ride system as one premium movement grammar

- [ ] **T029** · Audit all six ride styles against the shared movement controller and ensure none silently bypass newer safety fixes.
- [ ] **T030** · Preserve distinct steering, acceleration, air-control, grind, boost, and trick identities for all six styles.
- [ ] **T031** · Ensure inline skates feel direct, fast, and technical rather than generic sprint speed.
- [ ] **T032** · Ensure quad skates feel tighter, dance-forward, playful, and distinct from inline skates.
- [ ] **T033** · Ensure street deck has committed steering, strong grind identity, flip/slide readability, and board-specific animation/audio.
- [ ] **T034** · Ensure BMX has the largest gap/boost identity with bike-specific tricks and correct hand/handlebar animation ownership without breaking weapon compatibility when combat takes priority.
- [ ] **T035** · Ensure hoverboard has a smooth levitation identity, high-quality dedicated model, air correction, magnetic-feeling grind response, and no fake wheel physics.
- [ ] **T036** · Ensure scooter has nimble steering, tailwhip/barspin vocabulary, compact transfers, and distinct stance.
- [ ] **T037** · Improve shared acceleration/coasting/braking curves using the strongest relevant research references without flattening style identities.
- [ ] **T038** · Preserve external impulses from explosions, pistons, knockback, currents, slime, ice, redstone rails, etc. as legitimate movement tech.
- [ ] **T039** · Preserve exact vanilla swimming ownership.
- [ ] **T040** · Preserve ladder/vine/climbable vertical authority unless explicit JetSetCraft action has priority.
- [ ] **T041** · Improve micro-terrain continuity without enabling wall-clipping or invisible step teleports.
- [ ] **T042** · Ensure slopes/ice/slime/honey/Soul Speed interactions remain intuitive and exploitable as skill tech, not overridden.
- [ ] **T043** · Ensure ledge-vault collision checks cannot phase through protected geometry.
- [ ] **T044** · Ensure wall-kick same-wall reuse protection is stable under lag, multiplayer, and high FPS.
- [ ] **T045** · Ensure grind transfer preserves useful velocity and cannot snap to invisible/unphysical centerlines.
- [ ] **T046** · Expand Create / Steam ’n’ Rails / moving-structure compatibility only through optional adapters that preserve source-mod authority.
- [ ] **T047** · Test dimension transitions and portal-adjacent traversal without stale movement state.
- [ ] **T048** · Add regression fixtures for neutral input, kickoff, swimming, impulse capture, walls, vaults, grind transfers, and dimension/session resets.
- [ ] **G003 · GATE** — All six ride styles are deep, distinct, interoperable, and share one safe server-authoritative movement grammar.

# PHASE D — Tricks, Flow, combo expression, and animation depth

- [ ] **T049** · Preserve the existing 24 stable trick IDs and 28 stable dance IDs for compatibility.
- [ ] **T050** · Expand trick presentation and per-style naming without breaking stable network/persistence IDs.
- [ ] **T051** · Audit repetition penalties and variety bonuses so optimal play rewards expressive lines, not spam.
- [ ] **T052** · Keep Flow as a separate expression meter from Boost and combo score.
- [ ] **T053** · Make Flow gains clearly tied to skilled movement: varied tricks, clean landings, difficult transfers, wall sequences, cyphers, stylish route continuity.
- [ ] **T054** · Make Flow loss/drain readable and fair; no arbitrary real-time cooldown frustration.
- [ ] **T055** · Deepen Perfect/Clean/Sketchy landing rules using airtime, impact, state, alignment, and combo context without bypassing vanilla fall damage.
- [ ] **T056** · Add style-specific audio/particle accents only where they improve readability.
- [ ] **T057** · Expand animation vocabulary while preserving deterministic resource identities.
- [ ] **T058** · Guarantee ordinary ride/trick animation cannot claim weapon arms/hands/head except a narrowly owned action that explicitly yields when combat/item use takes priority.
- [ ] **T059** · Preserve clean animation cancellation when item use, swing, swimming, passenger state, elytra flight, death, or state transitions take authority.
- [ ] **T060** · Add slow-motion only as a deliberate optional accessibility/style mechanic if it does not alter server-authoritative outcome or multiplayer fairness.
- [ ] **T061** · Build at least one deterministic trick-line acceptance route that exercises boost, air trick, grind trick, transfer, wall action, landing, combo and Flow.
- [ ] **G004 · GATE** — Tricks/Flow feel expressive and skill-based, animate correctly, and remain fully compatible with combat/item systems.

# PHASE E — Graffiti becomes a flagship Minecraft-native creation system

- [ ] **T062** · Preserve 139 built-in decals, custom 16x10 painter, Free Paint, paint balloons, six-face decals, repaint-in-place, cleanup, durability and per-chunk safety limits.
- [ ] **T063** · Preserve the bounded 16x10 4-bit custom-graffiti wire format; do not permit arbitrary client file/texture upload.
- [ ] **T064** · Ensure the Sept. 30 hand/context boundary hardening remains intact in final code.
- [ ] **T065** · Polish graffiti selector UX so it feels like a Minecraft inventory/workbench interaction rather than a disconnected web-like screen.
- [ ] **T066** · Improve controller/keyboard navigation, paging, search/favorites if useful, without bloating basic spray interaction.
- [ ] **T067** · Preserve direct quick-use paths for common tags so advanced UI never slows ordinary tagging.
- [ ] **T068** · Add clear placement preview/feedback when a surface is invalid or obstructed.
- [ ] **T069** · Keep surface placement deterministic and server-authoritative.
- [ ] **T070** · Ensure paint balloons and spray interaction never mutate hidden/unloaded geometry.
- [ ] **T071** · Ensure decals survive save/reload and remove safely when supporting blocks disappear.
- [ ] **T072** · Preserve repaint behavior without creating overlapping duplicate entities.
- [ ] **T073** · Add collaborative mural support only through bounded data and normal server rules.
- [ ] **T074** · Integrate graffiti meaningfully into gang challenges, reputation, turf, and player expression.
- [ ] **T075** · Add real-client visual-audit scenes for built-in decal, Free Paint, custom painter, repaint, multi-face splat, invalid surface, and cleanup.
- [ ] **G005 · GATE** — Graffiti is fast to use, deep to master, safe in multiplayer, persistent, and fully integrated into the rest of JetSetCraft.

# PHASE F — Boombox, music, and gang encounter foundation

- [ ] **T076** · Preserve the physical Boombox item/block and real one-item gang target slot.
- [ ] **T077** · Preserve head/emblem resolution priority and never guess identity from textures/display names.
- [ ] **T078** · Preserve data-driven gang definitions and atomic reload behavior.
- [ ] **T079** · Preserve source entity types when creating event actors.
- [ ] **T080** · Preserve persistent gangification for ordinary mobs only while real Street Gear remains equipped.
- [ ] **T081** · Preserve one active session per Boombox and bounded actor lifecycle.
- [ ] **T082** · Improve Boombox presentation: animated controls/equalizer, gang color theme, visible target emblem/head, clear tuned/active state.
- [ ] **T083** · Keep comparator behavior useful and documented.
- [ ] **T084** · Preserve 80 original gang entrance stingers and stable gang music IDs.
- [ ] **T085** · Replace the old unauthorized-track restriction with the user's authorized-content model: support full-length licensed JSR/JSRF/BRC music through stable track IDs and the first-party licensed content/resource-pack pipeline, with seamless official release installation.
- [ ] **T086** · Make gang arrival/departure cinematic but bounded: skating/grinding exits when practical, safe timeout cleanup otherwise.
- [ ] **T087** · Ensure event actors cannot become loot/XP/Street-Gear farms.
- [ ] **T088** · Ensure cancellation and immediate restart remain safe and deterministic.
- [ ] **G006 · GATE** — Boombox tuning and gang encounters are polished, physical, data-driven, safe, and fun before deeper progression is added.

# PHASE F2 — Authorized JSR / JSRF / BRC asset integration and complete radio system

This phase is **accepted implementation scope**, not a research-only appendix. The user has explicitly stated full permission to use the covered source-game assets. Take advantage of that permission wherever it materially improves JetSetCraft.

- [ ] **T269** · Resolve the authorized source asset locations/packages for Jet Set Radio, Jet Set Radio Future, and Bomb Rush Cyberfunk without replacing a higher-quality known source with a lower-quality web rip.
- [ ] **T270** · Build or extend a deterministic licensed-asset ingest pipeline that inventories models, rigs, textures/materials, graffiti/art, dances/animations, SFX/VFX, props, UI/icon material, and music by source title and stable asset ID.
- [ ] **T271** · Create a durable licensed-asset provenance manifest containing source title, original asset identifier/path, content hash, imported output path, conversion steps, artist/author/track metadata when known, credits, and authorization-evidence pointer.
- [ ] **T272** · Deduplicate byte-identical/repackaged source assets by hash without losing title-specific aliases, metadata, credits, or legitimate alternate versions.
- [ ] **T273** · Convert authorized character/NPC models and useful outfit/gear pieces into high-fidelity Minecraft/GeckoLib-compatible assets while preserving silhouette, proportions, material identity, and animation attachment points.
- [ ] **T274** · Integrate authorized skates, boards, BMX/scooter/ride props, boombox/radio props, graffiti props, environmental street props, and other useful equipment/models where they improve existing JetSetCraft content rather than sitting unused in an archive.
- [ ] **T275** · Import/adapt the full useful authorized dance/emote/pose library and preserve source timing, loops, transitions, expressive keyframes, and character intent; map dances into JetSetCraft's dance/challenge system instead of exposing them as disconnected test animations.
- [ ] **T276** · Re-target dances/animations across compatible Minecraft/player/mob rigs with graceful species-aware fallback while retaining the original motion identity as closely as possible.
- [ ] **T277** · Import useful authorized trick/movement animation material and blend it into JetSetCraft ride/trick presentation where it improves fidelity without violating movement authority or combat-arm ownership invariants.
- [ ] **T278** · Import authorized graffiti/art textures and organize them into coherent galleries/categories with source-game/title provenance, search/favorites compatibility, and proper scaling/filtering for Minecraft surfaces.
- [ ] **T279** · Import useful authorized SFX/VFX/particles and map them to appropriate movement, graffiti, boombox, menu, gang-arrival, dance, trick, and challenge feedback without creating an overwhelming constant-noise layer.
- [ ] **T280** · Preserve/adapt authorized UI/icon motifs only where they enhance JetSetCraft while still respecting the contract's vanilla-native in-game interaction rules; licensed style may decorate, but must not turn normal Minecraft interaction into an unrelated console-game menu.
- [ ] **T281** · Build a stable music catalog containing **every authorized track available from the covered JSR/JSRF/BRC source sets**, not a hand-picked sample, with title, artist, source game, album/station grouping where available, duration, stable track ID, content hash, and credits.
- [ ] **T282** · Preserve the highest practical authorized audio quality and perform at most one controlled distribution transcode per source when Minecraft-compatible encoding is required; prevent repeated generation-loss across rebuilds.
- [ ] **T283** · Implement a real **JetSetCraft Radio** music browser/player with vanilla-feeling compact controls and both world/Boombox access and a convenient player-accessible radio surface.
- [ ] **T284** · Radio must expose All Tracks plus title/station groupings for Jet Set Radio, Jet Set Radio Future, Bomb Rush Cyberfunk, and JetSetCraft-original music/stingers without mixing provenance or losing track identity.
- [ ] **T285** · Implement search, sort, title/game/artist filters, favorites, user playlists, queue, previous/next, seek/progress where technically reliable, shuffle, repeat-one/repeat-all, and clear Now Playing metadata.
- [ ] **T286** · Persist each client's selected station/playlist, favorites, queue policy, shuffle/repeat state, music volume, radio enabled/disabled state, and resume preference across restart without server-side cosmetic preference overwrite.
- [ ] **T287** · Provide configurable music ducking/mixing against Minecraft music, records, ambient sounds, dialogue/important SFX, Boombox-local playback, and other mods rather than simply blasting two music sources over one another.
- [ ] **T288** · Prevent simultaneous duplicate playback when the same licensed track is requested by personal radio, nearby Boombox/event music, menu preview, or another JetSetCraft source; establish clear priority/crossfade/hand-off rules.
- [ ] **T289** · Make Boomboxes able to browse/play the authorized catalog through stable IDs, including per-game stations, custom user playlists, gang/event presets, and JetSetCraft originals, while preserving normal spatial audio behavior for in-world playback.
- [ ] **T290** · Keep personal-radio playback client-owned/private by default; multiplayer servers may synchronize event/Boombox track identity and timing for shared local playback without forcing every client to use the same personal station outside the event.
- [ ] **T291** · Handle clients missing/corrupt licensed content gracefully with exact diagnostics and repair/reinstall guidance while preserving gameplay/network compatibility; do not crash a dedicated server because audio/model content is client-side missing.
- [ ] **T292** · Package heavy licensed assets in the architecture that gives the best startup/memory/update behavior—core JAR only when appropriate, otherwise a first-party JetSetCraft licensed content/resource pack automatically discovered/activated by the official release bundle—with no manual copying required for a normal install.
- [ ] **T293** · Add versioned asset-pack identity/hash checks so updates patch/replace changed media deterministically and stale partial packs cannot silently mix incompatible models/animations/audio.
- [ ] **T294** · Add an in-game Credits/Licensed Content view with source-game/artist/studio attribution and track/model/dance provenance sourced from the manifest, without exposing confidential permission documents unless explicitly approved.
- [ ] **T295** · Add deterministic validators/tests for catalog completeness, duplicate IDs, broken media references, manifest/hash drift, missing credits/provenance, invalid animation/model outputs, and unresolvable radio tracks.
- [ ] **T296** · Real-runtime test the complete licensed-content path: representative imported character/gear model, multiple source dances, source graffiti/props/SFX, and music playback from every covered title through both personal Radio and an in-world Boombox; verify save/restart and multiplayer behavior.
- [ ] **G020 · GATE** — The user's authorized JSR/JSRF/BRC content is genuinely integrated at production quality: useful models/dances/art/SFX are in real gameplay, the complete authorized music catalog is available through a polished Radio/Boombox system, provenance/credits are durable, asset packaging is performant/update-safe, and runtime proof shows the actual shipped content working rather than placeholder media.

# PHASE G — Implement the full Gang Atlas / Black Book

This is preserved design lineage and is now accepted implementation scope.

- [ ] **T089** · Implement a polished **Gang Atlas / Black Book** as the player’s collection, relationship, progression, and customization hub.
- [ ] **T090** · Atlas entries must use stable `gang_id`, not disposable actor identity.
- [ ] **T091** · Show display name, disposition, colors, known habitat/context, discovered status, relationship/reputation rank, membership, allies, rivals, preferred challenge types, music identity, and notable unlocks where available.
- [ ] **T092** · Keep undiscovered information appropriately hidden without fake blank pages.
- [ ] **T093** · Add **Crew Naming Rights** unlocked by configured Friendly/relationship thresholds.
- [ ] **T094** · Support personal alias mode and optional shared-world alias rules without changing canonical `gang_id`.
- [ ] **T095** · Persist aliases safely across save/reload and multiplayer reconnect.
- [ ] **T096** · Implement the gang relationship graph: allies, rivals, grudges, support/hostility modifiers, betrayal and reconciliation.
- [ ] **T097** · Allow long-term maximum relationship/membership with every gang unless server rules explicitly change that; do not hard-lock the player into one faction forever.
- [ ] **T098** · Implement membership ranks with stable normalized thresholds and optional gang-themed visible rank names.
- [ ] **T099** · Preserve major relationship history only as bounded meaningful flags/events; do not create unbounded transcripts.
- [ ] **T100** · Make helping one gang against another produce coherent bounded relationship effects.
- [ ] **T101** · Make betrayal meaningful but recoverable through gameplay rather than irreversible save punishment.
- [ ] **T102** · Integrate rewards, cosmetics, Chapter Boombox unlocks, naming rights, challenge variants, and allied interactions into progression.
- [ ] **T103** · Keep progression optional to normal Minecraft survival; JetSetCraft must enrich a world, not turn every save into a mandatory quest campaign.
- [ ] **T104** · Add server config for progression scale without permitting contradictory or corrupt relationship states.

## Approved Sift + modern-vanilla gang naming expansion

Treat the following display names as **approved JetSetCraft defaults**. Every row keeps one stable namespaced `gang_id`; the visible name is presentation only. When a player reaches the existing **Crew Naming Rights** threshold for that gang, the Gang Atlas must expose the curated **Alternate** as an instant preset alongside any server-permitted custom alias. Switching names must never fork reputation, membership, allies/rivals, Chapter state, rewards, or save identity.

For Sift mobs whose upstream/public mob label is still provisional or changes before vanilla release, bind the stable gang mapping to the actual registry/provider identity or durable semantic archetype rather than baking a temporary community label into save identity. Display-name migrations must be lossless.

### Sift crews

| Mob / archetype | Default JetSetCraft gang name | Unlockable alternate | Design identity |
|---|---|---|---|
| Seedling | **Fresh Cuts** | **Root Rookies** | tiny fresh-start street crew |
| Nester | **Nest Level** | **Home Turf** | territorial home-base crew |
| Sentinel | **Dead Center** | **Lock-On** | precision / homing-shot crew |
| Pollinator | **Pollen Posse** | **Buzz Business** | projectile / territory support crew |
| Sprout | **Support Group** | **Green Room** | buff/support crew |
| Bloombud | **Petal Pushers** | **Bud Breakers** | aggressive flower punks |
| Shroomer / durable provider archetype | **Mush Pit** | **Cap Crew** | mushroom mosh-pit crew |
| Fusefly | **Flash Mob** | **Short Fuse** | flying explosive chaos crew |
| Hurler | **Pitch Perfect** | **Throwdown** | projectile + musical crew |
| Roamroot | **Root Runners** | **Road Roots** | traversal / roaming crew |
| Wobble | **Wobble Trouble** | **Loose Steps** | unstable goofy trick crew |
| Stalker | **Cut Above** | **Slash Track** | aggressive cutting/spin crew |
| Scavenger | **Soul Searchers** | **Ghost Notes** | soul-projectile crew |
| Hunter / Monarchling family | **Crown Chasers** | **Royal Runners** | Monarch-linked hunter crew |
| Blub | **Blub Club** | **Bubble Trouble** | cute aquatic street crew |
| Singer | **Headliners** | **Lead Vocals** | performance / social crew |
| Tuner | **Pocket Beats** | **Fine Tuned** | tiny rhythm-tech crew |
| Echo Golem | **Soul Speakers** | **Heavy Reverb** | heavyweight music/soul crew |
| Slabber | **Slow Jam** | **Low Tempo** | laid-back heavy ambient crew |
| Nuzzle | **Snuggle Hustle** | **Soft Touch** | cute social crew |
| Prickle / durable provider archetype | **Point Taken** | **Sharp Dressed** | spiky precision crew |
| Trill | **High Notes** | **Sky Chorus** | aerial rhythm/flock crew |
| Antenna Sifter / durable provider archetype | **Signal Boost** | **Good Reception** | communication / signal crew |

### Sift legendary / boss identities

These are Atlas legendary encounter identities, not ordinary common-spawn gangs. They still use stable encounter/gang IDs where progression hooks require them, and their alternate title unlock follows the same naming-rights presentation rule when applicable.

| Encounter | Default identity | Unlockable alternate |
|---|---|---|
| Harmonizer | **Perfect Pitch** | **Resonance** |
| Dartback | **Needle Drop** | **Backspin** |
| Monarch | **Crown Control** | **King of the Hill** |
| Sculk Monstrosity | **Feedback Loop** | **Bass Abyss** |
| Twisted Warden | **Deep Cuts — Twisted Mix** | **Deep Cuts — B-Side** |

### Modern vanilla catch-up crews missing from the original 1.20.1-era Atlas

| Mob / archetype | Default JetSetCraft gang name | Unlockable alternate | Design identity |
|---|---|---|---|
| Armadillo | **Roll Models** | **Round Trip** | rolling / armor / downhill crew |
| Breeze | **Air Time** | **Gust Hustle** | wind boosts and aerial combos |
| Bogged | **Muck Shots** | **Bog Standard** | swamp projectile crew |
| Creaking | **Dead Wood** | **Stiff Competition** | eerie stop/go forest crew |
| Happy Ghast | **Cloud Nine** | **Sky High** | peaceful aerial riding crew |
| Ghastling | **Little Lift** | **Cloud Cubs** | Junior Atlas partner to Cloud Nine |
| Copper Golem | **The Conductors** | **Patina Patrol** | copper/music-conductor crew |
| Nautilus | **Pressure Drop** | **Shell Riders** | underwater pressure/speed crew |
| Zombie Nautilus | **Dead Reckoning** | **Sunken Circuit** | undead ocean rider crew |
| Camel Husk | **Dead Heat** | **Dune Dread** | undead desert racing crew |
| Parched | **Dry Fire** | **Weak Links** | desert ranged / Weakness crew |
| Sulfur Cube | **Bounce House** | **Cube Roots** | sulfur-zone bounce/absorption crew |
| Frostbite | **Freeze Frame** | **Cold Snap** | freezing / ice projectile crew |

Variant policy: biome/temperature/color variants of an existing mob stay with the parent gang unless they have materially distinct gameplay/identity. Coral Zombie Nautilus belongs under **Dead Reckoning** as a variant/chapter; warm/cold pig/cow/chicken variants stay with **Hog Wild / The Milk Run / The Pecking Order**; wolf variants stay **Pack Mentality**; baby forms belong in the Junior Atlas instead of becoming duplicate adult gangs.

- [ ] **T363** · Add the complete approved Sift crew table above to the data-driven Gang Atlas with stable IDs/archetype bindings, default names, curated alternate aliases, colors/music/challenge hooks, and no dependence on temporary display labels for persistence.
- [ ] **T364** · Add the Sift legendary identities above as rare/boss encounter profiles rather than ordinary roaming populations, preserving source boss ownership and existing `Deep Cuts` Warden lineage.
- [ ] **T365** · Add the complete modern-vanilla catch-up table above, including **Sulfur Cube = Bounce House** by default with **Cube Roots** as its unlockable alternate.
- [ ] **T366** · Upgrade Crew Naming Rights so reaching the configured naming-rights reputation/membership threshold unlocks a polished Atlas rename control with the curated alternate(s) as one-click presets plus server-permitted custom alias entry; changing display name must never change `gang_id` or fork progression/state.
- [ ] **T367** · Persist personal/shared alias choices, permissions, rename history needed for moderation/recovery, and migration-safe fallback when a curated default/alternate is renamed in a later JetSetCraft update.
- [ ] **T368** · Add validation/runtime coverage proving default -> alternate -> custom/default restoration survives save/reload/reconnect and multiplayer, with two players allowed independent personal aliases when personal mode is enabled and one authoritative shared alias when shared-world mode is enabled.
- [ ] **G007 · GATE** — The Gang Atlas is a complete, persistent, useful social/progression system rather than a lore menu; approved Sift/modern crews have stable identity, and unlocked renaming changes presentation without ever splitting gang state.

# PHASE H — Universal Installed-Mod Mob Atlas + archetype intelligence

- [ ] **T105** · Implement runtime **Installed-Mod Mob Atlas** discovery using registries/tags/public metadata rather than hard class dependencies.
- [ ] **T106** · Separate `mob_archetype_id`, `entity_type_id`, and `gang_id`.
- [ ] **T107** · Implement confidence-based archetype resolution: explicit tags/overrides > curated aliases > normalized names/translation keys > safe traits > user aliases > unresolved.
- [ ] **T108** · Never identify creatures by renderer internals or texture scanning.
- [ ] **T109** · Cache archetype resolution after registry load; no global per-tick registry scanning.
- [ ] **T110** · Support deterministic provider priority when multiple mods supply the same archetype.
- [ ] **T111** · Never randomly swap a living member’s provider/entity type.
- [ ] **T112** · Preserve roster identity when a provider disappears; move member to missing-provider/dormant state instead of deleting data.
- [ ] **T113** · Allow safe provider rebinding only during later spawn/recovery when confidence is sufficient.
- [ ] **T114** · Expose chosen provider and confidence/debug reasoning in the Atlas/debug UI.
- [ ] **T115** · Make unknown creature mods degrade gracefully and remain crash-safe.
- [ ] **T116** · Preserve/adapt the Ground Contact / Ride Rig abstraction for bipeds, quadrupeds, multi-leg, aquatic, aerial, floating, body-contact and unusual species.
- [ ] **T117** · Improve curated high-fidelity adapters for popular vanilla/modded creatures without making them required dependencies.
- [ ] **T118** · Ensure legless/incompatible creatures get a board/hover/contact solution or reject unsuitable gear cleanly instead of clipping fake feet.
- [ ] **G008 · GATE** — Installed and future creature mods can participate safely through archetypes and ride rigs without JetSetCraft owning their entities/models.

# PHASE I — Junior Atlas and creature-family flavor

- [ ] **T119** · Implement the **Junior Atlas** as a sub-atlas linked to parent gangs, not a disconnected faction system.
- [ ] **T120** · Preserve real vanilla/mod age/life-stage relationships where they exist.
- [ ] **T121** · Do not invent baby age data for size-based or alternate-life-stage entities; model those relationships truthfully.
- [ ] **T122** · Preserve the approved adult/main gang atlas and documented junior names/relationships from the master spec.
- [ ] **T123** · Give junior crews appropriately playful/squeaky music variants through stable IDs without trivializing gameplay rules.
- [ ] **T124** · Inherit parent-friendly/hostile disposition logically while allowing shared reputation rules to change relationships.
- [ ] **T125** · Ensure junior challenge/reputation transactions reconcile to parent/subcrew rules and cannot be exploited for duplicate rewards.
- [ ] **T126** · Keep junior population/encounters bounded and source-entity ownership intact.
- [ ] **G009 · GATE** — Junior crews add charm and systemic depth without becoming a duplicate progression system or compatibility hazard.

# PHASE J — Natural hangouts, soft territory, and world life

- [ ] **T127** · Implement ultra-rare Natural Hangout discovery using existing generated terrain; no new invasive worldgen structures.
- [ ] **T128** · Persist compact `site_id` records with `gang_id`, dimension, anchor, radius, roster refs, dressing seed, bounded local history, validity state.
- [ ] **T129** · Never force-load a hangout chunk.
- [ ] **T130** · Treat territory as influence, never land ownership; do not block player building/spawns/structures/mod systems.
- [ ] **T131** · Implement safe post-generation micro-dressing using only air/replaceable positions.
- [ ] **T132** · Never flatten terrain, carve solid blocks, cut trees, reroute fluids, or restore old terrain over player changes.
- [ ] **T133** · Store a placement manifest so retirement removes only JetSetCraft-owned dressing.
- [ ] **T134** · Add a low-frequency shared Hangout Brain that assigns cheap intents to nearby loaded residents.
- [ ] **T135** · Keep residents source-owned entities with real Street Gear and persistent `site_id` membership.
- [ ] **T136** · Allow cheap idle skating, emotes, practice tricks, prop use, greeting/taunting based on reputation.
- [ ] **T137** · Avoid strategic competition AI until an actual challenge begins.
- [ ] **T138** · Use activation/deactivation radii with hysteresis to prevent boundary thrashing.
- [ ] **T139** · Bound active hangouts per dimension/server.
- [ ] **T140** · Revalidate/migrate/retire sites when terrain changes materially; never fight player edits.
- [ ] **T141** · Prove hundreds/thousands of unloaded site records have negligible live tick cost.
- [ ] **G010 · GATE** — Gangs feel like they live in the world while unloaded territory remains essentially free and worldgen remains source-owned.

# PHASE K — Reputation, membership, history, and relationships in live gameplay

- [ ] **T142** · Implement canonical `player UUID + gang_id -> GangReputation` persistence.
- [ ] **T143** · Implement bounded local `site_id` affinity/history as a secondary layer, never a second giant reputation meter.
- [ ] **T144** · One event/encounter resolves one balanced gang/chapter reputation transaction; never award per spawned actor.
- [ ] **T145** · Implement Friendly/Member/Veteran-style progression thresholds with server-configurable tuning.
- [ ] **T146** · Implement hostile crews offering deals/challenges in ways consistent with their disposition.
- [ ] **T147** · Implement betrayal and reconciliation through actual gameplay actions.
- [ ] **T148** · Preserve alliance/rival effects without creating permanent impossible-to-repair hostility unless explicitly configured.
- [ ] **T149** · Make world/Atlas dialogue/feedback reflect major remembered events without spam.
- [ ] **T150** · Persist and migrate reputation safely across updates.
- [ ] **G011 · GATE** — Reputation and relationships are persistent, understandable, bounded, and affect real gameplay.

# PHASE K2 — Street Level, mastery, skill trees, and gang-specific progression

JetSetCraft should feel rewarding for hundreds of hours without becoming a grindy MMO bolted onto Minecraft. Progression must reward **playing stylishly, trying new things, exploring, improving, befriending/rivaling gangs, and mastering ride styles**. It must never reward AFK movement, repetitive exploit loops, or mandatory daily chores.

Use three complementary progression layers with one canonical server-authoritative owner:

1. **Street Level** — the player-wide long-term progression track and primary source of general Technique Points.
2. **Ride Mastery** — separate mastery tracks for Skates, Skateboard, BMX, Scooter, Hoverboard, and Parkour/On-Foot movement, earned primarily by actually using that style well.
3. **Gang Reputation** — one persistent relationship/reputation track per stable `gang_id`, with gang-specific social reactions, unlocks, Signature Techniques, cosmetics, chapter/posse privileges, music/flavor, and world behavior.

These layers may interact, but they must not become three copies of the same XP bar. Street Level represents broad mastery, Ride Mastery represents mechanical specialization, and Gang Reputation represents social/world progression.

## Street Level and progression economy

- [ ] **T297** · Implement a persistent server-authoritative **Street Level** with a deliberately tuned finite power curve and a post-cap Legend/Prestige-style continuation that primarily rewards expression, collection, cosmetics, titles, mastery challenges, and bragging rights rather than runaway stat inflation.
- [ ] **T298** · Award Street XP for meaningful play: completing challenges, varied tricks/combos, clean landings, discovering new lines/hangouts/gangs, meaningful graffiti, dance battles, exploration objectives, first-time Atlas discoveries, crew activities, and difficult stylish movement accomplishments.
- [ ] **T299** · Do not award exploitable XP for raw time moving, AFK loops, standing on automated transport, trivial repeated hops, packet spam, repeatedly challenging the same easy mob, or other low-information repetition.
- [ ] **T300** · Implement variety and novelty weighting: first-time tricks, new trick chains, new locations/surfaces, new opponents, harder lines, clean execution, underdog wins, risky routes, and creative mixed-style play should be worth more than grinding one optimal action forever.
- [ ] **T301** · Apply bounded diminishing returns to identical short-loop repetition without punishing normal practice. Recovery should be gameplay/variety based rather than real-time daily timers.
- [ ] **T302** · Keep XP arithmetic deterministic and server-authoritative; client HUD/UI may predict presentation but never grant progression.
- [ ] **T303** · Persist Street Level, XP, unspent Technique Points, unlocked nodes, loadouts, mastery, reputation-linked unlock receipts, and migration versioning safely across save/reload/update.
- [ ] **T304** · Add server configuration for progression pace, XP multipliers, maximum effective power scaling, respec policy, and optional disable/vanilla-light modes without corrupting saves or invalidating earned cosmetic/social unlocks.
- [ ] **T305** · Add clear anti-exploit accounting and diagnostics so duplicate packet/action rewards, reconnect duplication, challenge-abort farming, mob-respawn farming, or concurrent reward races cannot mint XP/points twice.

## General Street Skill Tree

The general tree must be fun to browse and make builds feel different, but **no mandatory trap choices** and no permanent irreversible mistakes. Use strong milestones, sidegrades, synergies, and new actions rather than hundreds of tiny +1% filler nodes.

- [ ] **T306** · Build a vanilla-native **Street Skills** screen integrated with the Gang Atlas/Black Book family rather than a web-dashboard UI. Use Minecraft-style panels, advancement-like spatial readability, item/icon language, tooltips, keyboard/controller navigation, and clear locked/unlocked/available states.
- [ ] **T307** · Give each node a stable data-driven ID, prerequisites, cost, category, effects, tooltip/lore, optional icon, server config policy, and migration-safe unlock state. Do not hardcode the entire tree in client rendering code.
- [ ] **T308** · Implement a **Momentum / Speed** branch centered on acceleration quality, momentum retention, carve/pump efficiency, slope conversion, landing carry, high-speed control, and advanced traversal—not flat unconditional movement-speed inflation that breaks Minecraft.
- [ ] **T309** · Implement a **Boost / Burst** branch centered on Boost capacity/efficiency, recharge through skillful play, burst shaping, launch control, boost-combo interactions, recovery from near-misses, and higher-skill perfect-boost windows.
- [ ] **T310** · Implement a **Flow / Style** branch centered on combo grace, Flow retention through legitimate transitions, multiplier expression, perfect landing bonuses, transfer creativity, recovery tools, and additional ways to convert varied stylish play into Flow.
- [ ] **T311** · Implement an **Air / Rail / Parkour** branch centered on aerial control, wall/ledge transitions, manuals/balances, rail transfers, vault quality, wall-kick options, landing precision, and route creativity rather than automated movement.
- [ ] **T312** · Implement a **Rhythm / Dance** branch centered on timing windows, chained emotes/dances, dance-battle expression, crowd/crew synergy, rhythm-based Flow conversion, and music-linked challenge options without turning success into an inaccessible rhythm-game requirement.
- [ ] **T313** · Implement a **Graffiti / Creative** branch centered on faster expressive tagging, additional creative/editor affordances, combo-to-paint synergy, mural/collab capabilities, paint economy/QoL, and cosmetic presentation—not bypassing server permissions or protection mods.
- [ ] **T314** · Implement a **Crew / Street Legend** branch centered on social/challenge QoL, posse coordination, gang interaction options, challenge stakes, crowd hype, rescue/rejoin tools, Chapter benefits, and long-term world presence without replacing source mob AI.
- [ ] **T315** · Include meaningful cross-branch synergy nodes so builds such as high-speed Flow, boost-heavy aerial, rail technician, rhythm skater, graffiti runner, or crew leader feel distinct without requiring one mathematically mandatory meta tree.
- [ ] **T316** · Prefer nodes that create a new decision/action/timing opportunity over passive number inflation. Small numeric modifiers are acceptable only where they support a tangible mechanical identity.
- [ ] **T317** · Enforce soft/hard safety caps on effective speed, acceleration, boost impulse, air control, knockback interaction, and collision-sensitive effects so maxed progression remains fast and exciting without tunneling through blocks, desyncing, invalidating server authority, or breaking ordinary combat/world physics.
- [ ] **T318** · Make all progression effects conditional on the correct JetSetCraft state/gear/context; do not globally mutate vanilla base attributes in ways that remain active while sleeping, swimming normally, flying, riding unrelated mounts, using Elytra, or participating in another mod's movement state.
- [ ] **T319** · Add at least several active/triggered high-tier techniques—perfect-timed boost, advanced transfer, controlled recovery, signature landing, rhythm burst, or similar—so the endgame adds mastery rather than only bigger numbers.

## Respecs, loadouts, and experimentation

- [ ] **T320** · Allow players to respec without deleting progression. Default behavior should encourage experimentation rather than punish a player for trying a build.
- [ ] **T321** · Prevent exploitative mid-challenge or mid-combat respec swapping; apply changes only when safe, such as out of an active challenge/combat state or at a Boombox/Atlas interaction depending on chosen UX.
- [ ] **T322** · Add multiple named **Skill Loadout** presets that remember node allocation and can be switched through a vanilla-native screen when safe.
- [ ] **T323** · Make unavailable nodes explain *why* they are locked (level, prerequisite, mastery milestone, gang technique, challenge achievement) instead of silently graying out.
- [ ] **T324** · Add a one-click recommended starter path and branch previews for players who do not want to theorycraft, while never auto-spending points without explicit user action.
- [ ] **T325** · Preserve skill loadouts and UI preferences per player across reconnect/restart; server policy remains authoritative over which nodes/effects are enabled.

## Ride Mastery — use the style to master the style

- [ ] **T326** · Implement independent mastery tracks for **Skates, Skateboard, BMX, Scooter, Hoverboard, and Parkour/On-Foot** using the existing six-style ride taxonomy.
- [ ] **T327** · Award mastery from representative real actions for that style—clean tricks, difficult routes, transfers, unique lines, challenges, speed control, terrain use—not simple distance/time accumulation.
- [ ] **T328** · Give each mastery track milestone unlocks including style-specific tricks, handling techniques, signature animations, cosmetic parts/variants, trick slots/variants, titles/badges, and carefully bounded mechanical perks.
- [ ] **T329** · Create style-specific high-tier techniques that reinforce identity: e.g. skate rail/edge expertise, board manual/carve depth, BMX pump/bunny-hop/air control, scooter spin/transfer precision, hoverboard hover/energy handling, parkour wall/vault/landing mastery.
- [ ] **T330** · Allow mastery to contribute to general build synergies without making one ride style mandatory for unlocking unrelated core functionality.
- [ ] **T331** · Add clear mastery progress visibility to the ride/style selection UI and Street Skills screen without cluttering the moment-to-moment HUD.

## Gang reputation as a full game system

Each stable `gang_id` gets its own persistent relationship. This expands the existing GangReputation architecture; do **not** replace it with a second incompatible faction system.

- [ ] **T332** · Expand each gang's reputation track into a clear progression ladder such as **Unknown/Hostile -> Recognized -> Neutral -> Friendly -> Trusted -> Member -> Veteran -> Legend/Family**, while allowing gangs to start at different dispositions and preserving configurable internal thresholds.
- [ ] **T333** · Keep the underlying reputation value and relationship state separate enough that temporary hostility, betrayal, event context, or allied/rival modifiers can be represented without destroying earned history.
- [ ] **T334** · Make each gang respond visibly to reputation: greeting/taunting, challenge willingness, idle reactions, prices/rewards where applicable, entrance behavior, ally assistance, posse eligibility, hangout access, Chapter privileges, naming rights, and dialogue/UI flavor.
- [ ] **T335** · Award gang reputation for gang-relevant actions: challenges, helping/defending residents, participating in their preferred activity, respectful wins/losses, graffiti/turf events, contracts, rescuing members, shared rival events, chapter interaction, and other data-driven gang-specific hooks.
- [ ] **T336** · Apply coherent negative reputation for betrayal, attacking members, sabotaging events, siding with rivals in explicit conflicts, vandalizing protected gang interests, or abusing a gang's trust; never infer betrayal from unrelated vanilla combat events without strong context.
- [ ] **T337** · Preserve the prior rule that a long-term player can ultimately repair and maximize relationship with every gang. Rivalry must create interesting friction and consequences, not permanent mutually-exclusive save-file lockouts unless a server/datapack explicitly opts into that mode.
- [ ] **T338** · Implement reconciliation arcs: apology/rematch/help contracts, restitution challenges, defending a former rival, or other gameplay routes to repair damaged trust instead of waiting out a timer.
- [ ] **T339** · Keep local `site_id` affinity/history bounded and secondary; a specific hangout can remember the player while canonical gang reputation remains the main relationship track.
- [ ] **T340** · Make relationship history inspectable in the Gang Atlas through concise milestone memories—first meeting, first win/loss, betrayal, reconciliation, membership, legendary victory, chapter founded—without an unbounded event log.

## Gang Signature Techniques, rewards, and themed mini-trees

- [ ] **T341** · Give every gang a data-driven **reputation reward table** keyed to stable `gang_id` and normalized tiers, with support for cosmetics, music, graffiti, gear skins, emotes/dances, titles, challenge variants, Chapter/posse rights, and technique unlocks.
- [ ] **T342** · Implement **Gang Signature Techniques**: special movement/boost/Flow/trick/dance/graffiti/crew abilities learned from a gang at appropriate trust/membership tiers. These should reflect that gang's personality/species/context rather than being palette-swapped generic perks.
- [ ] **T343** · Surface learned Signature Techniques as an optional side-tree/socket layer that can synergize with general Street Skills while remaining bounded by the same server-authoritative movement and safety caps.
- [ ] **T344** · Do not require the player to permanently choose only one gang's techniques. Let players collect techniques across a long-running world, then use loadout/slot/budget rules if needed for balance.
- [ ] **T345** · Tie major gang rewards to memorable milestones and bespoke challenges—not only numeric reputation thresholds—so joining a gang, becoming Veteran, or learning its signature move feels earned and distinct.
- [ ] **T346** · Integrate approved adult gangs, Junior Atlas crews, legendary/boss gangs, and future datapack/modded gangs through the same reward/technique schema, with graceful fallback when a gang supplies no custom tree.
- [ ] **T347** · Allow datapacks/server packs to define or override gang tier names, XP/rep sources, reward tables, signature techniques, icons, descriptions, and challenge unlock conditions through validated schemas rather than code forks.

## Progression presentation and moment-to-moment feedback

- [ ] **T348** · Add compact optional vanilla-native progression feedback: XP/rep gain toasts or action-bar feedback, level-up celebration, mastery milestone, rank-up, and Signature Technique learned. Batch/deduplicate spam during long combo sessions.
- [ ] **T349** · Level-up/rank-up presentation may use licensed JSR/JSRF/BRC music/SFX/visual motifs under the established authorization lane, but keep effects readable, accessibility-aware, and not constantly intrusive.
- [ ] **T350** · Let the player independently configure or disable Street XP, mastery, and reputation popups/sounds without disabling the systems themselves.
- [ ] **T351** · The Gang Atlas should show each gang's current rep/rank, next meaningful unlock, recent major relationship milestone, available challenges/contracts, and learned/available Signature Techniques.
- [ ] **T352** · The Street Skills screen should show current Street Level, XP to next level, Technique Points, active loadout, branch investment, ride mastery summaries, and gang-technique slots without exposing internal debug arithmetic.
- [ ] **T353** · Use readable vanilla-style icons/tooltips and colorblind-safe state differences; do not rely on color alone for available/locked/maxed/reputation states.

## Balance, compatibility, and proof

- [ ] **T354** · Build a data-driven progression simulator/test harness that can replay representative XP/rep events and verify level curves, point totals, unlock timing, diminishing returns, respecs, migrations, and duplicate-reward protection deterministically.
- [ ] **T355** · Playtest/tune at least three build archetypes—speed/boost specialist, technical Flow/trick specialist, and social/gang/creative specialist—and prove each has meaningful strengths without invalidating the others.
- [ ] **T356** · Test max-effective progression against collision, server correction, combat, Elytra, swimming, mounts, teleportation, portals, ladders, vehicles, common movement mods, and low-TPS conditions; progression must not create a new desync/dupe/exploit class.
- [ ] **T357** · Test reputation with ally/rival interactions, betrayal/recovery, gang challenge wins/losses, hostile-start gangs, Junior crews, Chapter/posse unlocks, event cleanup, save/reload, and multiple players with independent relationships.
- [ ] **T358** · Ensure multiplayer rewards are contribution-aware and non-duplicating: co-op challenge participation can reward every legitimate participant without multiplying one mob/event transaction into exploitable server-wide XP/rep.
- [ ] **T359** · Profile progression hot paths. No per-tick global gang/player scans, no repeated full-tree recomputation, and no client UI traversal of every gang every frame; use event-driven updates, cached derived stats, stable IDs, and dirty/versioned recomputation.
- [ ] **T360** · Provide admin/debug commands or equivalent diagnostics to inspect/set/reset Street Level, Technique Points, Ride Mastery, gang reputation, unlocked nodes, and reward receipts for testing/support, with permission checks and no ordinary-player exploit surface.
- [ ] **T361** · Add migration coverage so future tree rebalance/node removal/renames never silently eat spent points or corrupt saves; refund or remap safely with explicit versioned rules.
- [ ] **T362** · Native-runtime proof must show a new player earning Street XP, leveling, spending points, switching/respeccing a loadout safely, raising and lowering reputation with at least two distinct gangs, unlocking a gang Signature Technique, advancing one ride mastery track, reconnecting/restarting, and retaining the exact intended progression state.
- [ ] **G021 · GATE** — JetSetCraft has a deep, replayable, non-grindy progression game: Street Level and skill-tree builds create meaningful mechanical expression, every ride style has mastery, every gang has persistent relationship/reward progression, Signature Techniques feel thematic and collectible, respec/loadouts encourage experimentation, max progression remains compatible/server-safe, and real runtime proof demonstrates persistence and balance rather than only UI or test fixtures.

# PHASE L — Full challenge/minigame system

Implement complete scored modes on top of the existing actor lifecycle rather than one-off scripts.

- [ ] **T151** · Create one shared challenge state machine with deterministic start/countdown/active/result/cleanup phases.
- [ ] **T152** · Implement **Turf War** using tagging/defense/coverage rules that cannot grief unrelated player builds by default.
- [ ] **T153** · Implement **Timed Lines** using movement/trick checkpoints and style scoring.
- [ ] **T154** · Implement **Races** with route/checkpoint validation, anti-skip logic, recovery, and ride-style fairness.
- [ ] **T155** · Implement **Dance Battles** using existing dance/cypher state with server-authoritative phrase/style scoring.
- [ ] **T156** · Implement **Graffiti Contests** using valid bounded painting surfaces and deterministic scoring.
- [ ] **T157** · Implement **Tag** with safe pursuit/tag transfer rules and no combat exploit dependence.
- [ ] **T158** · Reuse gangs, Boombox tuning, residents/event actors, reputation, rewards, music, and Atlas progression across all challenge modes.
- [ ] **T159** · Ensure challenge failure/cancel/server stop cannot strand actors, temporary blocks, scoreboard state, input state, or rewards.
- [ ] **T160** · Add configurable player count and gang actor budgets.
- [ ] **T161** · Add deterministic GameTests/fixtures for each mode’s scoring and cleanup.
- [ ] **T162** · Run multiplayer runtime tests for at least race, dance battle, Turf War, and Tag.
- [ ] **G012 · GATE** — All six challenge families are real, replayable, server-authoritative, and converge on shared progression/cleanup architecture.


## PHASE L2 — Challenge any AI mob: Minecraft-native social interaction + reversible loaner skates

The player should not need a mob to already belong to a gang before street interaction becomes fun. **Every ordinary AI mob is a potential opponent.** This must work for vanilla and modded mobs while preserving the source entity's identity, equipment, AI, owner/job/quest state, and normal interaction semantics.

### Player interaction contract

When the player is looking at a valid AI mob in normal interaction range:

- holding/pressing a dedicated **rebindable Challenge modifier** while using/interacting opens a compact vanilla-style challenge interaction rather than stealing ordinary right-click;
- while the player is wearing skates/Street Gear, **Skate-Off** is prominently offered along with **Dance Battle** and other context-valid modes;
- without skates, Dance Battle can still be offered and the UI may explain what gear is needed for a skating mode;
- the target acknowledges the invitation with a short readable look/emote/sound/particle/text response, then a normal countdown starts;
- if the target needs ride gear, JetSetCraft visibly loans it compatible skates/Street Gear for the activity and restores the target afterward.

- [ ] **T237** · Define one universal challenge-target resolver over real AI `Mob` entities, including modded entities. Default to eligible; reject only concrete unsafe states and return a player-facing reason code/message.
- [ ] **T238** · Add a dedicated rebindable **Challenge** modifier/action and ensure it composes with normal Use/Interact instead of replacing or globally cancelling another mod's right-click behavior.
- [ ] **T239** · When Challenge+Interact targets a mob in valid range/line-of-sight, open a compact Minecraft-native interaction menu/prompt showing target name plus **Dance Battle**, **Skate-Off**, and other challenge types that are currently valid.
- [ ] **T240** · Make the menu contextual and fast: if the player is currently wearing skates, default/highlight Skate-Off; if a mode lacks a safe nearby setup, explain the concrete requirement rather than hiding the mob or failing silently.
- [ ] **T241** · Server-authoritatively validate target entity ID/UUID, dimension, alive/loaded state, reach/line-of-sight, challenge availability, and request freshness before starting a session; never trust the client to enroll arbitrary entities.
- [ ] **T242** · Add a bounded **challenge activity lease/controller** that temporarily coordinates a source mob only while it participates. It must not clear/replace goal selectors, Brain memories, navigation implementations, ownership/taming data, profession/job data, quest state, or provider capabilities.
- [ ] **T243** · Preserve ordinary source AI as the base personality. Challenge behavior may temporarily steer navigation/velocity/look/animation through public safe hooks and JetSetCraft-owned state, then release control so the exact source behavior resumes.
- [ ] **T244** · For hostile mobs, make the active participant relationship safe enough to perform the challenge without permanently pacifying the mob: suppress only participant-vs-participant challenge-disrupting aggression through bounded session state and restore original hostility/targets afterward.
- [ ] **T245** · External danger remains real by default. Damage from third parties/environment may interrupt/cancel a challenge according to configurable safe rules; cancellation must converge through the same restoration path rather than leaving an invulnerable/frozen mob.
- [ ] **T246** · If the target is not wearing suitable JetSetCraft ride gear, create **temporary loaner Street Gear/skates** owned by the challenge session and visibly fit it through `MobRideRigResolver`/the canonical rig pipeline.
- [ ] **T247** · Never overwrite existing modded/vanilla equipment to fake loaner skates. Where the anatomy lacks a real equipment slot, use JetSetCraft's reversible attachment/rig presentation while keeping the source entity/equipment intact.
- [ ] **T248** · If the target already owns/equips JetSetCraft Street Gear, reuse that exact item/state and never replace its custom data with a generic loaner.
- [ ] **T249** · Snapshot only the minimum JetSetCraft-owned/transient state required for the activity and restore it atomically on win/loss/cancel, player disconnect, target unload/death, dimension transfer, server stop, exception recovery, or later save reconciliation.
- [ ] **T250** · Loaner gear can never enter normal inventory/trading/loot/drop/equipment persistence paths and cannot be duplicated, stolen, retained after the session, or farmed by killing the target.
- [ ] **T251** · Make **Dance Battle available to every safe AI mob** using anatomy-aware dance/pose adapters; when an animation skeleton cannot express a human move literally, map the same beat/intent to a species-appropriate motion rather than replacing the mob model.
- [ ] **T252** · Make **Skate-Off available to every safe AI mob** through the existing generic ride-rig architecture. Bipeds, quadrupeds, tiny/large mobs, flying mobs, and modded anatomies must use appropriate reversible rig/stance adapters rather than being excluded merely for body shape.
- [ ] **T253** · Resolve a safe activity space/route before Skate-Off. Never teleport an aquatic, flying, large, or scripted mob into lethal/incompatible terrain just to satisfy the mode; preserve the invitation and give an actionable “move to/open a suitable lane” result when the current location cannot safely host it.
- [ ] **T254** · Let providers/modpacks customize what “safe activity space” means through tags/adapters/API, including aquatic/aerial/special navigation, without hard dependencies or global pathfinding replacement.
- [ ] **T255** · Drive AI opponents with the same canonical trick/Flow/dance scoring rules as players. AI may choose lines/tricks/phrases through a server-side opponent controller, but it may not receive impossible score authority, teleport through checkpoints, or bypass landing/route rules.
- [ ] **T256** · Add deterministic configurable AI skill/personality profiles (casual, capable, expert, gang/legendary variants) using stable entity/gang/session seeds so behavior feels intentional instead of random every tick.
- [ ] **T257** · Make the invitation itself expressive: target looks toward the player, responds with a short species/personality-aware acknowledgement, loaner gear appears/equips cleanly when needed, and the countdown begins without a giant modal RPG screen.
- [ ] **T258** · A random non-gang mob challenged this way remains a **neutral Street Challenge participant**; do not invent permanent gang membership. Existing gangified mobs keep their real gang identity and may affect normal gang reputation/rewards.
- [ ] **T259** · Preserve tamed-owner state, villager profession/trades, Brain memories, age/variant, leash/passenger state, custom name, attributes, inventories, capabilities, and mod-owned persistent data across the challenge; add targeted restoration tests for each relevant category.
- [ ] **T260** · Prevent concurrent ownership conflicts: one mob cannot be controlled by two incompatible challenge sessions at once, requests use stable session/entity identity, and multiplayer receives a clean “already challenged/busy” response rather than racing state.
- [ ] **T261** · Make challenge join/cancel/result idempotent and safe under duplicated packets, reconnect, chunk unload/reload, save/restart, and late/stale client input.
- [ ] **T262** · Keep challenge movement/AI performance bounded: no global mob scans, no per-tick registry sweeps, no permanent extra AI goals on every mob; target discovery happens from the player's explicit interaction and only active participants receive challenge work.
- [ ] **T263** · Expose optional compatibility hooks/tags/events for other mods to mark temporary busy/protected states, customize acceptance/response/animation/rig behavior, or veto only their own genuinely unsafe scripted entities without taking ownership of JetSetCraft challenge state.
- [ ] **T264** · Preserve interaction-mod compatibility: ordinary right-click, trading, taming, mounting, pet commands, quest dialogue, mob-specific GUIs, and mod actions work exactly as before whenever the Challenge modifier is not intentionally held.
- [ ] **T265** · Integrate accessibility/reduced motion: every invitation/result has readable text/subtitle feedback; particles/camera motion are optional; the Challenge key is fully rebindable and does not require rapid or simultaneous inaccessible input.
- [ ] **T266** · Balance repeat challenges through skill/difficulty/variety/first-time bonuses and normal progression rather than arbitrary real-time waiting; prevent one easy mob from becoming an infinite reward/reputation farm.
- [ ] **T267** · Add deterministic GameTests/regression fixtures for eligibility, request validation, temporary gear lifecycle, AI-state preservation, duplicate packets, unload/reload, interruption, hostile participant restoration, and reward anti-farm behavior.
- [ ] **T268** · Native-runtime test the complete flow against a representative matrix: passive mob, hostile mob, villager/trader, tamed mob, tiny mob, large mob, flying mob, aquatic/special-navigation mob in a safe route, existing gang member, and at least several real modded AI mobs. For each, prove invitation -> acknowledgement -> temporary gear if needed -> challenge -> cleanup -> exact source behavior/state restoration.
- [ ] **G019 · GATE** — The player can naturally Challenge+Interact with essentially any safe vanilla/modded AI mob, receive a Minecraft-native Dance Battle/Skate-Off flow, see compatible temporary skates when needed, complete a fair server-authoritative challenge, and return the exact original source mob to normal with no lost AI/equipment/provider state, no duplicated gear, and no stolen interactions.

# PHASE M — Player chapters, Chapter Boombox, permanent crew, and posse

- [ ] **T163** · Unlock gang-specific **Chapter Boomboxes** at high configured reputation/membership tiers.
- [ ] **T164** · Make themed Chapter Boombox presentation data-driven over one shared implementation.
- [ ] **T165** · Placing a Chapter Boombox creates one stable player-founded `site_id` and bounded resident roster.
- [ ] **T166** · Use original vanilla/mod entity types and real Street Gear for permanent residents.
- [ ] **T167** · Introduce stable `crew_member_id` independent of runtime entity UUID.
- [ ] **T168** · Persist gang/archetype/provider/name/role/gear/home/posse/history/signature-move data in bounded form.
- [ ] **T169** · Allow naming permanent crew members.
- [ ] **T170** · Allow eligible trusted residents to become posse followers.
- [ ] **T171** · Followers may skate/travel, join appropriate JetSetCraft activities, pose/dance, and return home.
- [ ] **T172** · Never give followers chunk tickets.
- [ ] **T173** · Use bounded catch-up only when a follower is hopelessly separated; avoid constant teleport rubber-banding.
- [ ] **T174** · Allow moving the Chapter Boombox without duplicating/losing roster identity.
- [ ] **T175** · Treat Chapter Boombox as the roster’s home/spawn anchor without hijacking vanilla spawn systems.
- [ ] **T176** · On true resident death, mark roster recovering; respawn/re-form only when home chunk is naturally loaded after configured delay.
- [ ] **T177** · Preserve JetSetCraft identity across recovery while allowing a new runtime UUID.
- [ ] **T178** · Prevent Street Gear/inventory/loot duplication and repeated mob-loot farming through recovery.
- [ ] **T179** · Keep player relationship canonical at gang level rather than creating five global friendship meters for five residents.
- [ ] **G013 · GATE** — Player-founded chapters and posse members feel persistent and personal without becoming a settlement simulator or chunk-loading burden.

# PHASE N — Legendary crews, personalities, and long-term world variety

- [ ] **T180** · Preserve documented gang personalities and dispositions as data-driven presentation/gameplay traits, not hardcoded entity replacement AI.
- [ ] **T181** · Implement special/legendary encounter profiles for documented boss/rare gangs where source entities exist and are safely enabled.
- [ ] **T182** · Keep Ender Dragon / Wither / Warden / Elder Guardian style legendary events additive and non-destructive to vanilla boss ownership.
- [ ] **T183** · Ensure rare/technical entities remain out of ordinary progression unless explicitly enabled.
- [ ] **T184** · Add recurring challenge variety so beating a crew once does not exhaust its content.
- [ ] **T185** · Add optional allied collaborative murals / world flavor only when safe and reversible.
- [ ] **T186** · Make gang personality visible through music, color, preferred challenge modes, idle behavior, arrival style, trick preferences and rewards rather than intrusive custom AI replacement.
- [ ] **G014 · GATE** — Gangs remain recognizable, replayable, and differentiated throughout a long-running world.

# PHASE O — Compatibility, modpack coexistence, and performance hardening

- [ ] **T187** · Maintain optional compatibility with PlayerAnimator, Create 6.0.8, TACZ, Epic Fight, Better Combat, Aether, Twilight Forest and other supported ecosystems.
- [ ] **T188** · Add compatibility adapters only behind mod/version detection and safe public APIs/reflection boundaries.
- [ ] **T189** · Remove any hard optional-mod imports that can load classes while the provider is absent.
- [ ] **T190** · Prove JetSetCraft launches after removing each supported optional provider from a separate test instance.
- [ ] **T191** · Prove an unknown creature mod cannot crash Mob Atlas / Ride Rig logic.
- [ ] **T192** · Profile active ride movement CPU cost and remove avoidable per-tick allocations/scans without reducing physics quality.
- [ ] **T193** · Profile gang/hangout systems at realistic loaded population and reduce cost through low-frequency coordination, caching, spatial indexing, and bounded work — never by deleting accepted behavior.
- [ ] **T194** · Profile networking and preserve change-driven input with bounded active heartbeat instead of per-tick spam.
- [ ] **T195** · Ensure no stale asynchronous/client packet can overwrite newer server truth.
- [ ] **T196** · Stress save/reload, death/respawn, dimension transfer, reconnect, rapid ride equip/remove, provider reload, and challenge cancellation.
- [ ] **T197** · Add performance-equivalence evidence: optimized paths must produce the same gameplay results/state transitions.
- [ ] **G015 · GATE** — JetSetCraft remains lightweight and compatible in large modpacks without reducing mechanics, content, fidelity, or correctness.

# PHASE Q - Curated popular-mod gang ecosystem + collectible gang art

This phase extends the universal Installed-Mod Mob Atlas with premium authored coverage. It does not replace generic fallback. The canonical detailed mapping is `docs/CURATED_MODDED_GANG_ATLAS.md`.

- [ ] **T369** - Add a validated curated modded-gang schema containing stable `gang_id`, optional `crew_family_id`, provider mod ID, exact entity IDs, supported provider/version metadata, canonical/alternate names, starting disposition, colors, art resources, challenge preferences, signature technique hooks, ride/contact hints, and Junior/legendary relationships.
- [ ] **T370** - Implement a per-gang collectible art ladder so every curated gang can reward its sticker/decal, canonical graffiti tag, full emblem/logo, alternate throw-up/stencil/character mark, and prestige mural/masterpiece through reputation/mastery/milestone progression. No curated gang may silently have an empty art-reward path.
- [ ] **T371** - Key every gang-art unlock receipt to stable `gang_id` plus stable reward ID. Gang aliases/renames, display-name migrations, reconnect, restart, or provider version changes must never duplicate or invalidate earned art.
- [ ] **T372** - Wire earned gang tags/emblems into the real Graffiti Gallery/selector/editor/placement pipeline; where supported, also expose unlocked gang decals to ride cosmetics, Boombox/Chapter presentation, stickers, profile badges, or other existing cosmetic surfaces without creating a parallel fake inventory.
- [ ] **T373** - Make provider removal/resource loss safe: preserve unlock receipts and gang history in dormant form, hide or mark temporarily unavailable resources truthfully, and restore them automatically if the same stable provider/resources return. Never delete progression to silence a missing-resource error.
- [ ] **T374** - Give Junior crews parent-linked but deliberately distinct cute art variants where available; legendary/boss crews may add encounter-victory art conditions. Both still use the same stable progression/reward architecture.
- [ ] **T375** - Curate the complete Aether 1.20.1 wave from the pinned `1.20.1-develop` entity registry: account for all 20 primary mobs plus Cloud Minion as a special affiliate, using the exact mappings and names in `docs/CURATED_MODDED_GANG_ATLAS.md`.
- [ ] **T376** - Curate all 59 active Twilight Forest 1.20.1 creature/monster registrations from the pinned `1.20.1` registry, excluding the commented-out Boggard and non-Mob projectile/misc entities. Every detected source mob gets an explicit stable mapping; related mobs may share a high-quality family crew while retaining per-entity Atlas roles.
- [ ] **T377** - Curate the Undergarden 1.20 wave: all 16 normal living mobs, Forgotten Guardian as legendary boss, and the special Minion/Boomgourd records with explicit safety classification. Do not turn special/misc actors into ordinary roaming progression unless runtime proof says that is safe.
- [ ] **T378** - Curate Advent of Ascension through strong dimension/faction/family crews instead of mass-generating weak one-off names. Use the public 1.20 branch as a candidate superset only; enable mappings strictly from the exact entity IDs actually registered by the installed Forge 1.20.1 AoA build, leave absent IDs dormant, and keep technical/unused candidates hidden unless explicitly safe/enabled.
- [ ] **T379** - For AoA, preserve unique entity-level Atlas records underneath family crews so later premium species subcrews can be added without breaking existing reputation, unlocks, history, boss relationships, or save identity.
- [ ] **T380** - Add the next premium Forge 1.20.1 curation queue: Alex's Mobs, Mowzie's Mobs, L_Ender's Cataclysm, Alex's Caves, Ice and Fire: Dragons, Blue Skies, Born in Chaos, Critters and Companions, Naturalist, Deeper and Darker, The Bumblezone, Aquamirae, Unusual Prehistory, Friends & Foes where an exact compatible provider exists, then continue from real installed-mod discovery/popularity rather than a permanently hardcoded finite list.
- [ ] **T381** - Before naming/mapping each new provider, pin the exact Forge 1.20.1 release/source roster and reconcile discovered safe Mob count -> curated exact-ID count -> generic fallback count -> dormant/missing count -> intentionally hidden technical count -> unresolved count. `unresolved > 0` remains active work unless every unresolved entry has an explicit safety reason.
- [ ] **T382** - Extend Gang Atlas/Black Book UI for modded gangs with provider/mod name, exact source creature, family/subcrew, curated-vs-generic status, leader/champion/Junior relationship, current reputation, next meaningful reward, and gang-art unlock track without turning the screen into a web dashboard.
- [ ] **T383** - Enforce premium art differentiation for curated gangs: bespoke handstyle/composition tied to creature/world identity, readable icon silhouette, distinct mural/tag language, no repeated generic logo template, no default crown motif, no photoreal creature pasted beside text, and no palette-swap-only prestige variants.
- [ ] **T384** - Add deterministic validation for curated data: duplicate `gang_id`/reward IDs, nonexistent hard-required entity IDs, malformed provider constraints, dangling family/Junior/legendary links, duplicate reward receipts, missing required art fallback, and alias/name migrations.
- [ ] **T385** - Native-runtime test the four first-wave providers both individually and in a combined modpack where feasible: Aether, Twilight Forest, Undergarden, and AoA. Prove source entities remain provider-owned, normal AI/trade/boss behavior survives outside explicit JetSetCraft activities, gangification/challenges cleanly yield control, and absent-provider launches stay clean.
- [ ] **T386** - Native-runtime prove the art progression path with at least one friendly, hostile, boss/legendary, Junior, and modded gang: discover -> earn reputation/milestone -> unlock tag/logo -> use in Graffiti Gallery -> place art -> save/reload/reconnect -> rename gang -> verify same stable unlock -> remove/reinstall provider -> verify dormant/restored state.
- [ ] **T387** - Keep the curated system event-driven and cacheable: no per-tick registry sweeps, no every-frame traversal of every installed mod/gang/reward, no hard optional-mod class imports, and no performance shortcut that reduces supported mobs or art/reward coverage.
- [ ] **G022 · GATE** - The modded gang ecosystem has complete reconciled first-wave coverage, safe generic fallback for everything else, stable provider-independent history, premium distinct gang identities, and real collectible gang graffiti/tag/logo rewards that are earned, usable, persistent, rename-safe, provider-removal-safe, and runtime-proven.

# PHASE P — Documentation, release surface, and real runtime proof

- [ ] **T198** · Update README to reflect only actually shipped capability.
- [ ] **T199** · Update wiki pages for HUD (including Auto-Avoid + per-user Layout Editor), ride styles, Flow, graffiti, gangs, Atlas, hangouts, relationships, universal mob challenges/loaner Street Gear, challenge modes, chapters/posse, compatibility and verification.
- [ ] **T200** · Update `THIRD_PARTY_NOTICES.md`, licensed-content credits/manifest, and provenance for Spirit Vector/upstream material plus all authorized JSR/JSRF/BRC assets actually imported; keep open-source license obligations distinct from the user's studio-authorization evidence.
- [ ] **T201** · Preserve exact source revision/license boundaries for all upstream material.
- [ ] **T202** · Run all deterministic generators and validators.
- [ ] **T203** · Run clean Forge build.
- [ ] **T204** · Run full Forge GameTest suite.
- [ ] **T205** · Run real dedicated-server startup/readiness/shutdown with the exact final JAR.
- [ ] **T206** · Run real client/integrated-server verification with the exact final build.
- [ ] **T207** · Exercise all six ride styles in the runtime acceptance world.
- [ ] **T208** · Exercise Boost/Flow HUD at multiple GUI scales and reduced-motion mode, including Auto-Avoid against real mod overlays and persisted manually placed user layouts.
- [ ] **T209** · Exercise graffiti selector/editor/paint balloons/persistence/cleanup.
- [ ] **T210** · Exercise Boombox tuning and gang session start/cancel/restart.
- [ ] **T211** · Exercise at least one Natural Hangout lifecycle from activation through unload/reload/revalidation.
- [ ] **T212** · Exercise Gang Atlas relationship progression and Crew Naming Rights, including a default gang name -> curated alternate -> persisted alias round trip without changing stable gang identity.
- [ ] **T213** · Exercise Installed-Mod Mob Atlas with at least one optional mod creature and one missing-provider recovery scenario.
- [ ] **T214** · Exercise Chapter Boombox create/move/reload/resident death/recovery/posse flow.
- [ ] **T215** · Exercise every challenge mode end-to-end with cleanup proof, including Challenge+Interact against vanilla and modded AI mobs with temporary-loaner-gear restoration.
- [ ] **T216** · Exercise combat while riding with at least vanilla + one compatible combat/weapon mod path.
- [ ] **T217** · Inspect fresh client/server logs and resolve all task-related warnings/errors.
- [ ] **T218** · Build the final distributable JAR and record SHA-256/size.
- [ ] **T219** · Preserve a source checkpoint and exact final artifact identity.
- [ ] **T220** · Publish coherent final source/docs/release artifacts to the canonical repo/Drive only after runtime proof.
- [ ] **G016 · GATE** — Final release is build-proven, GameTest-proven, dedicated-server-proven, native-client-proven, documented, packaged, hashed, and durably published.

---

## 7. Required verification commands / entry points

Use the current project’s actual wrapper and scripts. Known release entry points include:

```text
python tools/generate_models.py
python tools/generate_animations.py
python tools/generate_brand.py
python tools/validate_assets.py
python tools/validate_gameplay_contract.py
python tools/validate_premium_polish.py
python tools/validate_java_syntax.py
python tools/validate_wiki.py
gradlew.bat clean build --no-daemon
gradlew.bat runGameTestServer --no-daemon
gradlew.bat -Djetsetcraft.visualAudit=true runClient --no-daemon
```

Do not repeatedly rerun the entire suite after micro-edits. Use the cheapest decisive changed-path check during iteration, then broad gates at parent/final convergence.

## 8. Reference and authorized-source hierarchy Claude should use — without letting references override JetSetCraft architecture

### User-authorized production asset sources

These are not inspiration-only for this project. Direct asset integration is authorized by the user and should be preferred when it produces a better result than recreating an approximation:

- **Jet Set Radio** — authorized models/characters/props, art/graffiti, animations/dances, music, SFX/VFX, UI/icon material, and other useful covered game assets.
- **Jet Set Radio Future** — same authorized production-use scope.
- **Bomb Rush Cyberfunk** — same authorized production-use scope, including movestyle/character/gear/dance/music/street-art material where useful.

### Already-authorized / existing open-source integration lineage

- **Dream Burst Spirit Vector** — movement state machine, Poise/style economy, wing/feather HUD language, vault/wall/kickoff ideas.
- **Street Art** — rollerblade presentation, painting/splash behavior, movement/media cohesion.

### High-value gameplay / engineering references

- **I Wanna Skate** — Minecraft-native skateboard customization, tricks, grinds, enchantments.
- **Create: Train Track Rail Grinding** — moving rail continuity / Create interoperability stress case.
- **Momentum - Parkour Movement** — input responsiveness, prediction, water/momentum behavior.
- **ParCool!** — action-state transitions, geometry detection, cancellation.
- **Slide!** — slope momentum and slide timing.
- **flowstate** — movement-earned Flow design comparison.
- **Momentum for Automobility** — steering/acceleration/braking/HUD tuning.
- **Emotecraft** — extensible emotes/networking/radial UX inspiration.

Unlicensed external references remain evidence/inspiration rather than authority. **The user-authorized JSR/JSRF/BRC production sources above are the explicit exception:** covered assets may be directly imported/adapted, while JetSetCraft still keeps its own server authority, gameplay architecture, persistence, compatibility, and vanilla-native interaction rules.

## 9. UI design principles

JetSetCraft should feel vanilla-native even when feature-rich:

- use Minecraft spacing, icon scale, tooltip language, screen transitions, button behavior, inventory conventions and sound grammar;
- avoid web-dashboard-looking in-game menus;
- avoid oversized full-screen HUD unless the user deliberately opens a full Atlas/editor screen;
- keep moment-to-moment movement HUD compact;
- use world interactions first where possible: physical Boombox, real gear items, graffiti on real surfaces, mobs remaining real mobs;
- make advanced systems discoverable through tooltips/Atlas rather than dumping tutorial walls on the player;
- controller/keyboard navigation must be intentional;
- no feature may require mouse-only operation unless Minecraft itself does;
- preserve reduced-motion/accessibility paths.

## 10. Performance invariant

Every performance change must satisfy **both**:

1. measured improvement in the targeted hot path; and
2. equivalent-or-better gameplay/content/fidelity/correctness.

Preferred techniques: caching, bounded concurrency, incremental updates, low-frequency coordination, spatial indexing, avoiding repeated registry scans, packet coalescing, change-driven sync, reusing computed geometry, stable IDs, and minimizing allocation.

Forbidden “optimizations”: fewer supported mobs, fewer hangouts, fewer checks, reduced trick fidelity, disabled effects by default just to gain FPS, skipped server verification, or removing mechanics.

## 11. Final convergence loop

Before declaring success:

`implement -> targeted verify -> inspect runtime/diff -> compare to this contract -> repair gaps -> repeat`

Then perform one whole-project challenge pass for:

- unchecked/invalidated tasks;
- placeholder/no-op code;
- stale historical branch behavior accidentally revived;
- tests that pass without production wiring;
- save migration loss;
- stale client overwrites;
- duplicate roster/site/challenge identities;
- optional-mod hard dependencies;
- performance wins achieved by doing less;
- UI that looks modded/generic rather than vanilla-native;
- HUD overlap/jitter with other mods or a user layout that fails to persist across scale/resolution/restart;
- ordinary mob interactions accidentally hijacked when the Challenge modifier is not held;
- challenge sessions that clear/replace source AI, leak targets, strand navigation state, or fail to restore mod-owned entity data;
- temporary/loaner skates entering loot, inventory, trade, drop, save, or duplication paths;
- a vanilla/modded AI mob class excluded only because no one implemented a compatible rig/adapter rather than because of a concrete safety constraint;
- licensed JSR/JSRF/BRC assets silently omitted, downsampled, replaced with generic approximations, or left in an unused archive despite being useful and covered by the user's authorization;
- incomplete radio catalog coverage, duplicate/broken track IDs, lost artist/title/source-game credits, repeated lossy audio transcodes, or stale partial content packs;
- licensed media packaging that causes unnecessary startup/memory regressions when a first-party split content/resource pack would preserve full quality with better performance;
- missing native runtime proof;
- features documented as shipped but not actually implemented.

## 12. Claude operating instruction

Do not answer this handoff with another summary or roadmap. Start by resolving the real current source/checkpoint, then implement the earliest ready task. Continue automatically across bounded execution windows. Ask the user only for a genuinely user-only decision or authorization that blocks the next required mutation.

- [ ] **G017 · FINAL COMPLETION GATE** — Every accepted task and parent gate above is complete; no blocker remains; the final artifact is a fresh verified Forge 1.20.1 build; the exact changed gameplay paths have been exercised in real client/server runtime; Boost/Flow HUD visibly follows the Spirit Vector-inspired vanilla survival-HUD principle while retaining JetSetCraft identity, auto-coexists with real mod overlays, and gives each user persistent full placement control; Challenge+Interact works end-to-end against representative vanilla and modded AI mobs for Dance Battle/Skate-Off with reversible loaner skates and exact source-AI/equipment/provider-state restoration; the user's authorized JSR/JSRF/BRC production assets are genuinely integrated rather than treated as reference-only, the complete available authorized soundtrack catalog is playable through polished personal Radio and in-world Boombox flows with durable provenance/credits and performant packaging; all preserved gang/Atlas/hangout/reputation/challenge/chapter/posse systems are real and production-wired, the approved Sift/modern gang defaults and unlockable alternate names are implemented with stable `gang_id` identity and naming-rights persistence; Street Level, build-defining skill trees, ride mastery, per-gang reputation ladders, respec/loadouts, and thematic Gang Signature Techniques are persistent, balanced, server-authoritative, non-grindy, and runtime-proven; performance improvements preserve full results; docs match reality; and final source/artifacts are durably checkpointed and published.
