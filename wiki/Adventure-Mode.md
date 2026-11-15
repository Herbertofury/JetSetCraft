# Adventure Mode — Custom Platformer Dimension

> **OWNER CONFIRMED 2026-11-15:** The mode's displayed name is **Adventure Mode**. It must be a self-contained **platformer + Metroidvania + roguelike with fun custom-made maps, end-of-level bosses and a final boss**, using a **dedicated custom dimension**, not the player's ordinary Minecraft world.
>
> **Engineering status:** A **separate dimension resource scaffold and two initial stage blueprints** are now checked into source. The mode selection, server-owned run state, stage generator, teleport/return pipeline, collision validation, bosses and full gameplay **are not yet wired or tested**. Do not expose an active Adventure Mode entry or claim the level maps generate until those systems exist.

[Full game design / legacy Neon Labyrinth working-title page](Neon-Labyrinth-Platformer-Metroidvania-Roguelike) · [Dimension architecture](https://github.com/Herbertofury/JetSetCraft/blob/main/docs/modes/ADVENTURE_DIMENSION_ARCHITECTURE.md) · [Tracking #65](https://github.com/Herbertofury/JetSetCraft/issues/65) · [Actual release status](Release-Status)

## One completely separate platformer world

**Runtime dimension resource ID:** `jetsetcraft:adventure_mode`. Its JSON definition is separate from `jetsetcraft:rival_rush`, `jetsetcraft:liberty_grind`, and all vanilla/provider dimensions. Source files:

- [Adventure dimension JSON](https://github.com/Herbertofury/JetSetCraft/blob/main/src/main/resources/data/jetsetcraft/dimension/adventure_mode.json)
- [Adventure dimension type](https://github.com/Herbertofury/JetSetCraft/blob/main/src/main/resources/data/jetsetcraft/dimension_type/adventure_mode.json)
- [Typed AdventureWorld key](https://github.com/Herbertofury/JetSetCraft/blob/main/src/main/java/com/herberto/jetsetcraft/adventure/AdventureWorld.java)
- [Draft registration/isolation GameTest](https://github.com/Herbertofury/JetSetCraft/blob/main/src/main/java/com/herberto/jetsetcraft/gametest/AdventureDimensionGameTests.java) — **created, not executed**

It starts from a deliberately **minimal, structure-free flat platformer staging surface**, not copied Overworld terrain. Later the stage builder composes its own authored regions, 3D obstacles, music-reactive scenery, rail/grind routes, checkpoints, secrets and boss arenas **inside protected, session-owned map slots**. The base layer is a safety surface, **not the completed map**. Build only near intentionally loaded run regions with bounded per-tick work.

### Two initial custom-area blueprints (not yet generated in game)

| Blueprint | Intended purpose | What's still missing |
| --- | --- | --- |
| [Rail Garden](https://github.com/Herbertofury/JetSetCraft/blob/main/src/main/resources/data/jetsetcraft/adventure_regions/rail_garden.json) | A vivid first platforming hub with jumps, distinct route choices, a visible locked shortcut and real skating lines | Runtime region loader/builder, route collision proof, assets, screenshots and real player traversal |
| [Crosswire Towers](https://github.com/Herbertofury/JetSetCraft/blob/main/src/main/resources/data/jetsetcraft/adventure_regions/crosswire_towers.json) | Vertical traversal, an earnable run-local movement upgrade, a route back to the early hub and stage boss progression | Verified acquisition/ability system, authored collision geometry, tested boss, multiplayer safety |

These are **authored JSON planning resources**, not actual `.nbt` structure files or already-loading playable stages. They deliberately cannot be counted as functioning content until the runtime parser, placements and native playtests are implemented.

## Clean isolation: no interaction with the player's world

1. **No Overworld map copies.** Build Adventure levels from original, authored templates in the private dimension. Never copy chests, tile entities, modded machines, blocks, mobs, loot or player builds out of the source world.
2. **Explicit consent and entry.** Choosing Adventure Mode from the existing activity menu is opt-in after actual implementation. Block overlapping Rival Rush, Liberty Grind or other event sessions. Save exact source dimension/position/yaw/pitch and safe dismount/return intent **before** any transfer.
3. **Separate slots per run/party.** Assign session UUID plus generation/slot, bounded floorplan and chunk ownership. Multiple users can play independent maps without seeing each other's stages or modifying each other's rewards.
4. **Author maps here.** Prepare modular level areas, boss rooms, shortcut gates, jump/rail sequences, secrets, music/animation cues and backgrounds in this dimension, driven by server-validated data definitions. No runtime world edits outside its owned slot.
5. **Safe backtracking.** A run-local unlock enables a known shortcut to an earlier area; the server validates level connectivity under every legal ability set. Procedurally selected modules must remain solvable for the exact ride/controller and accessible alternate controls.
6. **Clear finish and bosses.** The level-end boss changes the room and route when defeated; the true final boss closes the run. Never mistake existing Rush headliners for completed Adventure bosses.
7. **Leave exactly as you entered.** On exit, loss, disconnect, crash, reload, server restart or corruption, recover safely to the original world. Never overwrite survival inventory, XP, original mobs, optional mod capabilities, beds, spawn points or player-made structures. Use event-owned temporary cleanup and bounded chunk budgets.
8. **Unambiguous technical quality.** Run dedicated server tests, source-world hash/snapshot comparisons on protected fixtures, concurrent-session tests, no forced chunks, no fake teleport shortcuts, accessibility/flash/motion controls, memory/frame/tick profiling and actual client captures.

**This separate dimension does not promise the player's overall save folder contains no new dimension data.** A custom dimension necessarily has its own persisted world storage. Isolation means **no direct mutation of the Overworld, Nether, End or provider dimension terrain and player property through Adventure gameplay**.

## Proposed memorable flow

A real hand-authored arrival stage teaches satisfying tricks and rail-to-air movement → branching platforms and collectibles lead to a vertical district → unlock a new traversal technique → return to a previously visible gate to discover a secret/shortcut → choose different seeded regions, beat real level-end bosses → a climactic multi-phase final boss → clear victory/report or replay → safe exit to intact Minecraft.

The direction is **original breathtaking arcade adventure** with Metroidvania discovery and roguelike variety—not just a combat room, a particle show or Minecraft parkour.

## Delivery / acceptance status

- [x] Owner confirmed **Adventure Mode**, independent of regular Minecraft survival and other activity modes.
- [x] Added a distinct **dedicated Adventure dimension JSON/type** and typed dimension key to source.
- [x] Authored two **unloaded prototype region blueprints**, named and cross-linked.
- [x] Added an **unrun dimension-isolation GameTest** for follow-up native validation.
- [ ] Implement server-owned Adventure run/party slot manager, allocation and recovery journal.
- [ ] Implement data-driven region loading, template builders and authored collision/route generation.
- [ ] Implement real platforming, secrets, movement unlocks, backtracking, procedural variation and checkpoint system.
- [ ] Implement unique stage-ending bosses and original final boss; real audiovisual reaction and legal source audio.
- [ ] Register a working Adventure Mode activity tile only once the complete start/return flow is proven safe.
- [ ] Pass native client, dedicated server, multi-session, crash/rejoin, accessibility and original-world-protection acceptance.

The full technical backlog lives in [Issue #65](https://github.com/Herbertofury/JetSetCraft/issues/65). **Resources checked in ≠ runtime gameplay implemented.**
