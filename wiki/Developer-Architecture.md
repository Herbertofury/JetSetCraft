# Developer Architecture

## State flow

`ClientEvents → C2SInputPacket → JetSetData → DanceSystem / JetSetMovement → player velocity and style state → S2CStatePacket → ClientRideState → camera, HUD, render, and animation adapters`

The server owns movement and scoring. Client code presents synchronized state; it does not decide whether a trick, landing, dance reward, or grind attachment succeeded.

## Current alpha.7 candidate: room contracts and shared services

This section describes the implemented `0.4.0-alpha.7` candidate. Its **63-test and 23-capture acceptance remains pending**; [Testing and Verification](Testing-and-Verification) separates the required gates from historical alpha.6 and earlier results. Stable `v0.3.0` retains its own release identity.

| Component | Authority and bounds |
|---|---|
| `RushArenaCatalog` | Optional `contract` data selects none, Relay (2–4 ordered beacons), Paint (1–8 fresh patches) or Hunt (1–4 elite/captain defeats). Old profiles default to none; invalid specifications are rejected. Existing map bounds and optional-provider admission still apply. |
| `RushArenaContract` | One room's bounded optional progress. Relay consumes the Manager's actual riding observations and rejects paused/discontinuous movement; unloaded points wait and unsafe built points disable the optional goal. Paint and Hunt observe accepted in-room tagging and owned defeats. No contract grants currency or completes a room. |
| `RushCircuit` | Keeps required time/defeats, the route, current contract and bounded room history. The Manager also enforces headliner victory gates. A clear's canonical receipt is derived from the run, set and room. |
| `RushRun` / `RushPitStop` | Own the shared service budget, per-kind usage, exact capped mutations, previews and detached accepted results. One actual clear earns one credit; a completed optional contract makes two total. New stops replace unused budgets. |
| `RivalRushManager` / `C2SRushActionPacket` | Validate bounded intents, owner/run UUID, current revision and phase. Capture the actual receipt/contract before completion, record the clear once, then open the stop. Keep the existing once-only crew settlement; service actions do not call it again. |
| `RushEventFeed` / `RushFeedback` | Keep four bounded server-authored notices with increasing per-run IDs. Accepted cache claims, mastery, headliners, evolutions, contracts, clears and services produce feedback. Client snapshots/toasts never authorize rewards; reduced-motion presentation uses chat. |
| `RushDebrief` | Adds optional `ContractsBanked` and `ServicesUsed` counters to Schema 1. Missing old fields read as zero; reports remain detached, bounded display data saved after successful source-state restoration and settlement. |

### Service transaction

`pit_stop` accepts `TUNE|<Upgrade ID>`, `PATCH` or `REWIND`; the existing `remix` action takes its source and replacement. All use the current run revision. Services require a between-arena window with no pending XP draft, including room six's Encore decision. A successful claim advances the revision and spends one shared credit; each kind can be used once at that stop. Stale, malformed, unowned, capped or no-effect requests spend nothing.

Tune advances one owned, available stat/artifact by one rank without fallback XP or score. Patch applies up to 40% maximum vitality and 50% equipped ward capacity, counting actual recovery and creating no unequipped ward. Rewind replenishes one reroll and one banish within 3/2 caps; accumulated draft exclusions remain bounded by the upgrade catalogue. Remix retains the exact invested rank, three-family limit and removed-family charge cleanup. It has no independent production budget.

`openPitStop(receipt, bonus)` checks both the canonical receipt and a newer recorded clear ordinal, avoiding an expanding receipt ledger. The trusted legacy `grantRemix()` model entry opens the same budget; production clear settlement uses the actual receipt API. Successful route departure or Encore calls `closePitStop()`, which invalidates the revision even after all credits were spent. Finishing the run removes service authority.

`PitStop` snapshots expose available/earned credits, revision, receipt, four options, at most fourteen eligible owned Tune targets and the last accepted result. The Manager adds `Usable` and a phase-specific reason. Actual before/after resource/rank values describe accepted effects; they never become client-supplied award amounts. Contract completion earns its bonus only when the required room is cleared. `ContractsBanked` counts those qualified clears, and `ServicesUsed` counts accepted claims, without creating another permanent award.

### Source and runtime boundaries

The original Minecraft/provider entity remains source-owned. Rush progression, vitality, steering directives and artifact effects stay within the owned session; this continuation does not replace native AI or rewrite native health, attributes or status effects. Optional providers remain isolated and unavailable content fails closed. Required time, defeat and headliner gates remain independent of side-contract success.

Model GameTests exercise production transactions directly; runtime fixtures separately exercise sessions, accepted events and the server action handler. Native Forge source-set execution and a separately built distributable JAR are distinct evidence. Neither the new candidate nor historical provider results imply every native spellbook, foreign capability, mob or mod combination is supported. The saved return journal and accepted crew receipts remain the ordinary-state and permanent-reward boundaries.

## Movement modules

- `RideMotion`: stride-pulsed ground acceleration, angular drift steering/release, wind-state air control, and boost.
- `GrindFinder` / `GrindTraversal`: path arbitration, continuation, junctions, hops, transfers, stuck recovery.
- `EdgeFinder`: exposed collision-shape ledges and clearance validation.
- `VanillaRailFinder`: real `RailShape` traversal.
- `CreateRailProvider`: optional native Create axes/Bezier geometry.
- `WallRideFinder` / `WallTraversal`: wall detection, plane-aware continuation, grounded-reset wall kicks, and wind redirect state.
- `ParkourTraversal`: input-gated sprint kickoff and collision-checked ledge vaults.
- `VanillaWorldPhysics`: facade over surfaces, rails, impulses, enchantments, landing, materials, and feedback.
- `TrickCombo` / `TrickCatalog`: move selection, repeat/variety scoring, boost tricks, landing grades, Flow.
- `DanceSystem` / `DanceCatalog`: 28 moves, phrase chaining, no-gear dance, cyphers.

## Animation layers

Two PlayerAnimator layers are registered:

1. `ride_lower_body` at priority 24 for locomotion, manuals, grinds, wall rides, powerslides, and air tricks. Authored clips may not contain arm, hand, held-item, or head keys.
2. `style_full_body` at priority 32 for deliberate dance and ground-stunt performance. It never animates held-item bones and is suppressed whenever a weapon overlay is active.

Gameplay state is backend-independent. PlayerAnimator consumes state but does not own the movement machine.

## Assets

`tools/generate_models.py` builds dense deterministic OBJ equipment and textures. `tools/generate_animations.py` builds deterministic UUID-stable animation JSON. Validators enforce model coverage, face indices, minimum mesh quality, animation bone contracts, catalog integrity, and exact brand/source rules.

`PaintSplash` owns server-authoritative Fibonacci-sphere surface exposure, six-face support/permission checks, deterministic 16 × 10 splat encoding, repaint replacement, and per-chunk limits. `RollerbladeModel` copies the actual player-leg pose into the authorized baked skate mesh every frame. The public source contains the adapted implementation and MIT notices; immutable authorized upstream snapshots remain in the private canonical archive.

## Compatibility boundaries

Optional integrations must not introduce class loading when the target mod is absent. Prefer public APIs, compile-only dependencies, Forge behavior, non-required tag objects, or registry lookup adapters. Never infer compatibility from a block name when native geometry/API data is available.

## Owner-approved multi-version architecture

### Target order
1. **Minecraft 1.20.1 first**: preserve the established Forge baseline and complete its gameplay/compatibility gates.
2. **Minecraft 1.21.1 second**: the owner's corrected target, not 1.12.1. Choose and pin the appropriate loader/integration versions during implementation.
3. **Latest stable Minecraft release third**: resolve the exact release and loader/toolchain when work starts; record it in a checked-in target manifest rather than letting builds drift with a floating latest dependency.
4. **Eventually all Minecraft versions**: the long-term coverage ambition, expanded through explicit tested targets. Do not imply untested versions or every edition already work.

This is an approved architecture requirement, not a completed port. It supersedes vague earlier future-version ordering.

### Shared core, thin adapters, reproducible version builds
Use a **Stonecutter-style multi-version workflow** so new targets reuse gameplay and tests instead of spawning divergent copies. Evaluate the maintained Stonecutter toolchain before adopting or extending it; prefer contributing reusable improvements to existing tooling over rebuilding everything. This follows the owner's desired Enderloom-style rapid-port architecture, without claiming an Enderloom implementation was inspected.

- **Shared gameplay core:** movement math/state machines, trick scoring, gang progression, event rules, music timing, customization schemas and rail-path contracts should avoid direct Minecraft/loader class dependencies wherever practical.
- **Version/loader boundary:** isolate input/events, world/collision queries, entities, registries, networking, persistence, rendering, animation, resources and lifecycle hooks behind explicit adapters. Use localized source preprocessing for small API differences; separate adapters for substantially different APIs.
- **Optional integration boundary:** Create, other railway mods, TaCZ and model/animation providers get per-target capability/dependency matrices. An absent upstream mod must not prevent core JetSetCraft from loading. Report unavailable integrations honestly and preserve shared gameplay wherever the target can support it.
- **Target manifest:** record Minecraft, loader, mappings, Java toolchain, Gradle/plugin versions, dependency pins, resource/pack formats, supported integrations and artifact identifiers. Use isolated generated outputs and target-specific caches; do not mutate shared source underneath concurrent CI jobs.
- **Assets and saves:** share authoritative art/animation/catalog data, generate target-specific resource formats, version serialized data and test migration. Never imply Minecraft world downgrades or cross-version multiplayer compatibility from shared mod code alone.
- **Fast iteration:** incremental builds, content-keyed generated assets and dependency caches; no repeated full repository/asset scans when inputs are unchanged. Build-time version selection should avoid unnecessary runtime dispatch/scanning.

### Port acceptance and implementation to-dos
- [ ] Inventory existing modules/dependencies and identify platform leakage before refactoring. Preserve current 1.20.1 behavior and artifact reproducibility.
- [ ] Prototype one vertical slice across 1.20.1 and 1.21.1: input → server-authoritative movement → ride rendering → left/right rail grind → save/reload.
- [ ] Establish shared deterministic movement/scoring fixtures plus per-target real-client and dedicated-server smoke tests. Use the target's available test framework; a successful compile alone is not compatibility.
- [ ] Run feature-parity and optional-mod matrices, including ride poses, Create track geometry, train restoration, multiplayer correction and resource reload.
- [ ] Benchmark startup, frame-time lows, tick time and allocation per target with the same representative scenarios; no silent feature or visual-quality reductions to make a port pass.
- [ ] Add a documented single-command target build/test workflow and CI matrix with checksummed, clearly versioned release artifacts.
- [ ] Prove a further target can be added chiefly through its manifest/adapters and shared tests, then expand coverage in the approved order.

### Tooling references
- [Stonecutter project mirror and current upstream notice](https://github.com/stonecutter-versioning/stonecutter): the project directs development to [Codeberg](https://codeberg.org/stonecutter/stonecutter). Pin a verified release and inspect current tool requirements; an older fork's examples are not proof of current compatibility.
- [Community Stonecutter multi-loader template](https://github.com/rotgruengelb/stonecutter-mod-template): an evaluation lead, not a verified drop-in migration for this repository.

No tool can automatically resolve every Minecraft API, rendering, save-format and third-party dependency change. The required outcome is a reusable architecture that makes each verified port substantially easier without sacrificing gameplay.
