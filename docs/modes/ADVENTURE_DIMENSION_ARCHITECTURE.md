# Adventure Mode — Separate Dimension and Custom Map Architecture

**Owner decision (2026-11-15):** Canonical user-facing mode name **Adventure Mode**. It is the new standalone-feeling **platformer / Metroidvania / roguelike** with authored custom map areas, bosses and replay. An independent dimension is **mandatory**, not merely an option to reuse Rival Rush's world. The earlier `Neon Labyrinth` is a non-canonical internal design/possible theme name. Issue: [#65](https://github.com/Herbertofury/JetSetCraft/issues/65).

**Implemented in this checkpoint:** static datapack definitions and typed Java key for `jetsetcraft:adventure_mode`, a minimal source-only dimension registration/isolation GameTest, and two **not-yet-loaded authored JSON region blueprints**. This checkpoint **does not** wire activity menu entry, teleports, run state, generation, boss mechanics, collision solvers, event ownership or cleanup. No native client/server GameTest was executed.

## Existing compatibility baseline

- `jetsetcraft:rival_rush` already has a separate flat dimension, `RushWorld`, session state, six-room combat circuit, real entry/return. Do **not** reuse its coordinate/slot allocation or its registered dimension for Adventure maps.
- `jetsetcraft:liberty_grind` already has its own separate dimension with expedition slots. Neither Liberty nor Rival Rush should have their JSON or session semantics altered.
- Forge 1.20.1 custom dimensions are **data-defined at load**; a full runtime dimension created/deleted on each run is not a safe assumption. Register **one permanent dimension type and level**, then allocate/clear isolated session/party stage **regions** within it.
- Existing `RushReturnState` provides a pattern for exact-source dimension/position return and crash recovery; reuse only after an ownership/compatibility audit. No source-world snapshot overwrite or inventory replacement.

## Resource files, exact intended responsibility

| Resource | Responsibility / current status |
| --- | --- |
| `src/main/resources/data/jetsetcraft/dimension/adventure_mode.json` | Unique `jetsetcraft:adventure_mode` data-defined world; minimal, structure-free flat staging canvas; not the gameplay builder |
| `src/main/resources/data/jetsetcraft/dimension_type/adventure_mode.json` | Separate environment profile, natural spawning/time/respawn policy; recheck exact loaded registry in Forge runtime |
| `src/main/java/com/herberto/jetsetcraft/adventure/AdventureWorld.java` | Typed dimension key and identity helper, **no teleportation** |
| `src/main/java/com/herberto/jetsetcraft/gametest/AdventureDimensionGameTests.java` | Test definition expecting a distinct loaded dimension; **unrun** |
| `src/main/resources/data/jetsetcraft/adventure_regions/rail_garden.json` | Authored region **prototype**, conceptual geometry/path metadata; **no JSON reload listener yet** |
| `src/main/resources/data/jetsetcraft/adventure_regions/crosswire_towers.json` | Authored vertical region **prototype**, traversal-key shortcut and stage-boss handoff; **no builder yet** |

**World data note:** This new dimension persists as a separate folder inside the same save. The user-approved isolation means **never read/overwrite player-created structures, chests, entities, native/mob ownership state or progression in the original dimensions** as part of course generation. Do not promise “zero new save files.”

## Mandatory slot and stage pipeline (unimplemented)

1. **Offer:** show Adventure Mode only after verified entry/return, with difficulty/session/co-op and loot policy clear. Reject active other-modes, unsupported ride/vehicle and incompatible event statuses; no forced teleports.
2. **Reserve:** server issues an opaque UUID/generation to a bounded slot, accounting for online, reconnecting and recovering parties. Distinct from Rush/Liberty slots. Limit concurrent run count through tested server configuration rather than arbitrary hidden caps; handle exhaustion with a clean response.
3. **Journal:** record the exact source `ResourceKey<Level>`, 3D position, yaw/pitch, current native mount/ride, respawn plan and return intent before teleport. Persist idempotently; never fake-save or overwrite foreign capability data.
4. **Plan:** compile the region graph from authenticated authored templates (future resource reload listener) and deterministic seed. Validate required traversal abilities in topological order, option/shortcut reachability and a complete baseline route. Optional maps may be locked and revisited, mandatory exits cannot be.
5. **Build:** only in reserved slot within `jetsetcraft:adventure_mode`. First guarantee safe arrival floor and exit; place geometry and authored structures with loaded-chunk safeguards, budgeted per-tick work, validated collision/landing/hazard timing, no third-party blocks/inventories. Maintain a precise manifest of event-owned blocks/entities/effects.
6. **Travel:** allow entrance only once target region built/readiness committed. Stage types: lobby, traversal/puzzle, secret/bonus, boss arena, checkpoint, branch connector and finale. Keep a map of discovered and revisitable regions and run-local movement skills.
7. **Finish:** require real multi-phase boss victories, server-owned completion receipts, optional replay/alternate branch. Never award completion from an unverified teleport, timed wait or owner music metadata.
8. **Recover:** on death/disconnect/rejoin/reload/exit/server stop, resolve exact slot and journal and restore the player to a safe original dimension/location. Clear only event-owned stage modifications, chunk tickets and temporary actors; never copy a whole old inventory snapshot into live state.
9. **Prove:** exact-build Forge GameTests plus actual native playable client/dedicated-server recording of start → first area → meaningful traversal unlock → return loop → boss → final boss → original-world restoration. Multiple players in separate slots must never see/affect each other's content or rewards.

## Initial region storyboard (not runtime-authored stages)

**Rail Garden:** approachable 3D skating platforming with momentum transfers, three optional lines, visible later gate and a safe checkpoint. The fun test must demonstrate first-time players preferring this to generic Minecraft parkour without relying on a soundtrack.

**Crosswire Towers:** vertical rails, wall-launch ability acquisition, loop-back to Rail Garden's previously locked route, new boss approach and a higher-difficulty shortcut. If wall-launch is not an installed movement technique, treat it as **future equipment design**, never require the inaccessible gate in a runtime run.

**Templates are *planning data*, not byte-for-byte platform placements verified for existing controller dynamics.** Both need real structure modules, material/shape fidelity, parsers, builders, audio hooks and tests. Never mark a stage playable from JSON existence alone.

## Explicit acceptance conditions

- [ ] `AdventureWorld.DIMENSION` resolves in a native Forge 1.20.1 server and is distinct from every original/provider/JSC dimension.
- [ ] Spawn/readiness is safe in generated Adventure chunks and game rules do not alter source-world defaults.
- [ ] One working isolated session builds at least one authored region and completes a platforming path without touching any external dimension.
- [ ] Two independent sessions cannot overlap slots, routes, owned mobs, rewards, music or world chunks; lifecycle is crash/rejoin robust.
- [ ] Adventure upgrades unlock a *previously visited* gate; multiple seed validation passes actual movement tests.
- [ ] A stage boss and final boss are functional, distinct, fair and fully playable, with real victory receipt and safe cleanup.
- [ ] Original-world before/after fixtures prove source blocks, containers, modded entities, rider capabilities, position, inventory, XP and ordinary event state unchanged.
- [ ] No licensed full-recording audio unless separate explicit sync + redistribution rights; Hakken Horse music is **a character event reference**, not a universal soundtrack entitlement.
- [ ] Usable accessibility, offline/online client, dedicated server, performance trace and complete return/failure modes recorded.

All checkboxes here are **open** until separately proven by executable and human-playtest evidence.
