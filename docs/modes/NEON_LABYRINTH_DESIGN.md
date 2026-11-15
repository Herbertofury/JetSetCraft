# Adventure Mode — Platformer + Metroidvania + Roguelike Engineering Handoff (former Neon Labyrinth code name)

**Updated owner decision, 2026-11-15:** the name is **Adventure Mode**. A **dedicated custom platformer dimension** `jetsetcraft:adventure_mode` is mandatory, with original built regions entirely separate from the player's source worlds and from Rival Rush/Liberty Grind. The prior **Neon Labyrinth** name is a historical internal working codename/legacy file slug, **not the canonical mode label**. The standalone dimension JSON/type, typed dimension key, an unexecuted registration GameTest and two unloaded region template files have been checked into source; the mode's **run state, level generation, activity menu, teleport/return and boss gameplay remain unimplemented and unverified**. Read [the exact dimension architecture contract](ADVENTURE_DIMENSION_ARCHITECTURE.md) before making any new runtime changes.

**Owner-readable vision:** [Mode wiki](../../wiki/Neon-Labyrinth-Platformer-Metroidvania-Roguelike.md). **Tracking:** [GitHub issue #65](https://github.com/Herbertofury/JetSetCraft/issues/65). **Global quality gate:** [Encounter North Star](../../wiki/Encounter-North-Star-Beyond-Minecraft.md), [Acceptance scorecard](../ENCOUNTER_NORTH_STAR_ACCEPTANCE.md).

## 0.1 Locked dimension requirement and concrete scaffold

- **World ID:** `jetsetcraft:adventure_mode`, separate from `jetsetcraft:rival_rush`, `jetsetcraft:liberty_grind`, vanilla and provider worlds. Java/Forge uses a **static data-defined dimension with separately allocated run map areas**, not temporary runtime-generated dimensions.
- **Source now present:** `src/main/resources/data/jetsetcraft/dimension/adventure_mode.json`, `src/main/resources/data/jetsetcraft/dimension_type/adventure_mode.json`, `src/main/java/com/herberto/jetsetcraft/adventure/AdventureWorld.java`, `src/main/java/com/herberto/jetsetcraft/gametest/AdventureDimensionGameTests.java`. The new GameTest is a test *definition*, not a passing result.
- **Map seeds/blueprints:** `src/main/resources/data/jetsetcraft/adventure_regions/rail_garden.json` and `src/main/resources/data/jetsetcraft/adventure_regions/crosswire_towers.json` are authored illustrative region descriptors **currently ignored by runtime**. Build the JSON reload listener, validated structure geometry, gap/grind collision probes, slot allocator, checkpoint and boss pipelines before calling them game levels.
- **No player-world interference:** source Overworld/Nether/End/provider dimensions and their blocks, mobs, chests, machines, inventories, existing mode sessions and owner capabilities are off limits. Save a return journal and allocate a private slot before travel; never recreate a foreign mob or restore entire player inventories.
- **Persistence:** one Adventure dimension inevitably contributes independent chunks to the world save. Clean separation means *no modification of the player's normal gameplay worlds*, not the impossible claim of no additional save data.
- **Testing gate:** native Forge GameTest + actual client/dedicated-server start, map generation, return, multi-run isolation, crash recovery, performance and original-world comparison must pass. Current checkpoint has **not** executed those tests.

## 1. Evidence-based gap inventory from the existing repository

| Source | Real capability/design | Do not confuse with completed Neon Labyrinth |
| --- | --- | --- |
| `RivalRushManager` / `RushCircuit` / `RushRun` | Server-held active run state, segmented six-room circuit, choosing/travel/arena/completion phases, earned upgrades, rewards, leave/return | Current run graph is arena choices with combat time and defeats, not a persistent nonlinear region map, traversal-key locks, bosses per level plus a unique final-boss endgame |
| `RushArenaCatalog` / `RushArenaBuilder` | Bounded, validated data-pack room profiles and room building in a registered district | Does not prove authored platformer obstacle sequences are traversable with present speed/air/ride controller |
| `RushDirector` | Existing elite telegraphs and phased headliners; room 3/6 gates in circuit | Not a fully themed, unique multi-phase level-end/final-boss system |
| `RushWorld` / `RushReturnState` | Existing isolated district and protected return/recovery patterns | A new mode needs its own session namespace/slot lifecycle, idempotent cleanup and valid return under reload/late join |
| `ParkourTraversal`, skating/grind controller, `RushMotionTracker` | Real movement affordances and existing motion observations | A future ability gate cannot assume unsupported grapple, dash, underwater skating, double jump or moving-platform physics |
| `RivalRushScreen` / `ActivityManager` | Existing explicit activity mode UI | Any new option must remain **disabled/not shown as playable** until its runtime, networking, UX and test evidence exist |
| `Terrain‐Aware-Event-Circuits-and-Boss-Arenas` wiki | Proposed runner/crate/coin stages and Metroidvania-like shortcuts | Separate idea from the requested integrated mode with persistent route state and final boss |

Do not regress or rewrite those existing systems merely to satisfy a new label. Share **small proven contracts** where possible; maintain separate semantics for existing Rush run states, chest/card rewards and headliner timing.

## 2. Mode boundaries and source ownership

**Owner-approved mode label:** `Adventure Mode`. **Proposed `ModeId`:** `jetsetcraft:adventure_mode` (not registered in the activity menu). **World dimension ID:** `jetsetcraft:adventure_mode` (definition exists; runtime registry/native loading must be verified).

- **Entry:** opt-in separate tile in existing Boombox/activity selection; preview this as platformer/exploration/roguelike, not an ambiguous “Rival Rush variant.” Prevent starting while street activities, Rush, Liberty Grind or another run is active. Do not insert into the public activity catalogue until implemented.
- **World:** allocate bounded, mutually isolated slots in the **dedicated new `jetsetcraft:adventure_mode` dimension** after validating its registry/load state; **never use Rush or Liberty dimensions**. This static dimension is a separate world, not a generated/destroyed dimension per run. Verify coordinate ownership, chunk use and return semantics. Never force-create dynamic dimensions or copy other mods' live entities/loot. Isolated level arenas must be distinguishable from the real Overworld.
- **Run model:** distinct `AdventureRun` state and versioned serialized snapshot; server owns seed, phase, level graph, room state, traversal unlocks, checkpoints, branch discovery, boss phases, currency/collectibles and complete/defeated flags. The client only requests legal actions and receives state/deltas.
- **Modularity:** special stage modules and boss scripts must be data-driven and validated independently. Named story beats are authored while selection of safe optional routes, hazards, bonuses, gang variants and boss modifiers can use deterministic seeded composition.
- **Cross-mode progress:** if using shared crew XP/reputation, credit only once from server-authored settlement receipts; other existing run levels, cash, Pit stops, Style Library and personal Minecraft inventory remain untouched unless owner approves explicit cross-mode rewards.

## 3. Core data contracts (proposed, not runtime schemas)

`AdventureManifest`: `schema_version`, `proposed_mode_id`, `session_capacity_policy`, `supported_player_counts`, `stage_set_ids`, `difficulty_preset_ids`, `reward_policy`, `allowed_dimensions`, `fallback_policy`.

`LevelModule`: stable `module_id`, author/version, theme, entry/exit anchors (position + velocity + direction envelope), movement prerequisites, collider/material/palette references, collision/hazard map, collectibles, checkpoint anchors, optional secrets, segment cost, network-safe cues, age/accessibility flags and cleanup manifest. All world mutations are **event-owned and auditable**.

`RegionGraph`: `seed`, `graph_revision`, nodes with `region_id` and neighbors, `gate_id`/required ability set, one-way vs reversible edge policy, secrets, seen/unseen map state and guaranteed exit/return path. Both geography and ability prerequisites are required; random walk alone is not a Metroidvania.

`TraversalUpgrade`: ID, required controller capabilities, owner source, upgrade acquisition and exit/effect policy, legal states, UI/hint, accessibility alternative, movement envelope calibration and explicit scope `run_only` (unless a separate owner-approved permanent-unlock rule exists).

`BossBlueprint`: stage/arena, gang or original character identity, telegraph IDs, transition conditions, server-authoritative hit/defeat semantics, attack phase entry/exit, recoverable safe tiles/lanes, soundtrack cues (original/licensed only), checkpoint/respawn behavior, victory receipt and cleanup references. For an end boss, require at least a tested distinct movement encounter, not merely bigger stats.

`RunSnapshot`: owner UUID, session UUID, run seed, schema/version, phase, selected difficulty, explored regions, opened gates, saved checkpoint, run upgrade inventory, reward claims, boss state/victory flag, return intent, recovery state and idempotent award keys. Reject stale client versions and duplicate attempts; validate old snapshots during schema changes.

## 4. Proposed run lifecycle and handoff rules

`IDLE → PREVIEW → RESERVING → ENTRY_LOADING → EXPLORING ↔ CHECKPOINT/UPGRADE ↔ BOSS/POST_BOSS → FINAL_BOSS → WON/FAILED/ABORTED → RETURNING → CLEANUP → IDLE`.

- **Start:** validate source world, event exclusivity, local protection, active mount/vehicle, return destination and session slot; persist return intent **before** transfer; wait for actual safe arrival before starting timers or boss AI.
- **Checkpoint/recovery:** checkpoint update is server-owned; a crash/rejoin must resume or safely extract without duplicating reward. Avoid player-bag or survival inventory replacement as a recovery hack.
- **Backtracking:** discovered locks stay on the map; applying a new traversal upgrade recomputes reachable nodes and opens actual previously seen shortcuts. Support manual revisit without forced teleporting.
- **Boss:** a real encounter state machine, clear phase ownership, one enemy victory receipt, safe retries and committed progression only on actual defeat. Mandatory final boss must exist before run can be completed.
- **Exit:** cancel audio, animations, controlled props and temporary triggers; remove only event-owned geometry; restore safe world position/mount state and existing equipment effects; no leaked tickets or orphaned arena entities.

## 5. Generated levels must be solvable, not only visually impressive

Suggested algorithm:

1. Assemble a small **authored topological grammar** (hub, forward region, loop-back, reward branch, traversal gate, miniboss, stage boss, finale). Connect multiple routes with clearly advertised movement prerequisites.
2. Place **required traversal upgrades before mandatory gates** in the dependency DAG. Optional gates may be visible early, making the return satisfying rather than frustrating. Keep baseline escape accessible for every legal run state.
3. Assign real **controller-level movement envelopes**: entry speed, momentum, air control, grind radius, step/wall kick reach, dynamic surface support, landing area, phase-dependent hazard timing and accessibility. Check swept collision, not just block adjacency.
4. Solve a **stateful navigation graph** where state includes `(room, acquired abilities, unlocked shortcuts)`; verify spawn-to-first-upgrade, upgraded backtrack, stage-boss reachability, optional secret reachability if advertised, final-boss reachability and ability to return.
5. Compose only validated modules inside a protected session footprint, using deterministic run seeds and authored variety. Treat unloaded/foreign chunks as unavailable and any failed collision/visibility/support validation as a generation rejection—not a reason to force a player into a dead end.
6. Test many deterministic seeds with actual movement probes and property tests before enabling random generation. Author a safe fixed fallback zone; **never downgrade to random kill-rooms while still marketing it as Metroidvania**.

Required module test receipts: exact seed, stage graph, ability-state reachability, block/collision snapshots, route visualization/captures, controller version, action envelope, per-stage spend, cleanup and server logs.

## 6. Boss pipeline: real platforming finales

**Stage boss**: unique entrance and silhouette → explicitly taught tell → dodge/rail/transfer counter → punish/recovery window → escalating second/third phase → a true defeat/victory receipt → route/map alteration → post-boss world-state checkpoint.

**Final boss**: culminates two or more previously learned traversal skills and the run's build identity; arena shifts legally between safe authored patterns, each phase allows multiple fair responses and telegraphs. Music/crew reactions can intensify with real success while preserving controls and accessibility. Different difficulty/seed modifiers are valid only with full ability-state and hazard-window verification.

No imported copyrighted stage/boss/audio assets. Gang-linked fights use approved motifs and rights-cleared tracks **only where genre/cue/license match**; metadata-only Spotify selections are not playable.

## 7. Vertical-slice deliverables and external review

**V0 — technical contract:** lock an exact tested Forge build, document existing Rush ownership, approve mode ID/title later, link an acceptance issue, and author data schemas with strict version validation.

**V1 — fun-first platforming:** one independently playable hand-authored vertical level with meaningful 3D jumps, rail transfer, wall kick, optional shortcut and checkpoint. Observe unprompted players; fix movement feel before multiplying content.

**V2 — true exploration:** one entrance hub, two connected regions, one acquired run-local traversal key and one earlier visible ability-gated shortcut. A player must discover the lock, earn the ability elsewhere, navigate back and open it without cheats; capture the complete sequence.

**V3 — satisfying boss and return:** one themed stage boss with real telegraphed phases, player movement mastery and recorded win/retry, plus safe, verified exit to original world.

**V4 — roguelike replay and final encounter:** validated variation across multiple seeds, at least one route/secret decision that truly changes play, earned run builds, several distinct stage families and actual multi-phase final boss. No 'all levels complete' state before winning it.

**V5 — native release gate:** Forge GameTests on exact build, client video of the **full route including backtracking and boss**, dedicated server and missing-optional-provider test, multiplayer/spectator/reconnect, 1% lows and frame-time graphs, accessibility, rights receipts and cleanup verification. [Issue #65](https://github.com/Herbertofury/JetSetCraft/issues/65) holds the checklist.

**Playtest questions:** Did first-time players find the levels genuinely fun before the soundtrack? Did they understand the unlocked map shortcuts without a walkthrough? Did all bosses teach and test real movement? Did different seeds feel hand-authored yet fresh? Was losing instructive instead of punitive? Did the finale feel earned and could the player return to Minecraft with all world state intact?

**No shipped claim:** This file is not an engine source implementation, a working activity tile, a playable level, a boss prototype, a CI test or an approved asset license.
