# JetSetCraft Architecture

## Non-negotiable gameplay contracts

1. **Momentum follows real motion.** Boosting, landing, grinding, wall riding, tricks, block materials, slopes, redstone, and external impulses may add, redirect, or bleed momentum. Neutral input may settle to a complete vanilla-like stop; stale solver state may never manufacture camera-forward velocity.
2. **Combat is composable.** A weapon or item action is not an implicit ride-state exit. Movement owns translation and lower-body ride presentation; weapon systems retain item input and upper-body animation authority.
3. **The server owns truth.** Inputs are sent to the server. Movement, scoring, Flow, dancing, cyphers, landings, loadout state, and combo state are authoritative and synchronized to local and tracking clients.
4. **World geometry is the playground.** Grinding understands arbitrary exposed collision-shape edges and authored rail networks. Vanilla/Forge rails use their real `RailShape`; Create uses native track axes, normals, graph junctions, and Bezier geometry.
5. **Optional integrations remain optional.** Missing mods cannot cause classloading or datapack errors. APIs are isolated behind adapters and optional block entries use `required: false`.
6. **Animation is presentation, not gameplay authority.** Gameplay state never depends on PlayerAnimator, Epic Fight, TACZ, Better Combat, or YSM internals.
7. **Accessibility preserves control.** Reduced-motion settings remove presentation effects, never mechanics, responsiveness, score, or authoritative movement.

## State flow

`ClientEvents` -> `C2SInputPacket` -> `JetSetData` -> `DanceSystem` / `JetSetMovement` -> movement and Style Flow state -> `S2CStatePacket` -> `ClientRideState` -> HUD, camera, gear renderer, and animation adapters.

`JetSetData` is the single persistent player source of truth for:

- ride gear, active ride style, boost, momentum, and movement flags;
- grind kind/direction, wall state, manual, powerslide, and external impulses;
- combo score/multiplier/grace, Flow, trick history, repeat count, variety masks, boost tricks, and landing grade;
- dance family, named move, phrase time, chain length, cypher size, and dance variety mask.

## Style Flow modules

- `TrickCatalog`: stable 24-slot vocabulary for aerial, grind, and ground actions; style-specific display names; animation mapping; rank and landing labels.
- `TrickCombo`: contextual trick selection, repeat penalties, variety rewards, boost tricks, ground stunts, landing grading, combo bridge, and Flow decay.
- `DanceCatalog`: stable 28-move vocabulary across six dance families, deterministic animation mapping, phrase timing, points, multiplier gain, and boost recovery.
- `DanceSystem`: no-gear/full-loadout dancing, immediate cancellation, automatic phrase chaining, nearby-player cypher discovery, scoring, Flow, and feedback.
- `StyleFeedback`: server-triggered Minecraft-native particles and sounds for tricks, dances, and landings.
- `S2CStatePacket`: compact synchronized snapshot used by every presentation adapter.

## Movement modules

- `EdgeFinder`: searches current voxel collision boxes and scores exposed top edges while rejecting internal seams, hazards, blocked clearance, and `no_grind` entries.
- `VanillaRailFinder`: follows `BaseRailBlock#getRailDirection`, preserving straight, curved, and ascending geometry for vanilla and Forge-compatible rails.
- `CreateRailProvider`: optional Create 6.0.8 adapter using `ITrackBlock`, `TrackGraphBounds`, and `BezierConnection` instead of block-name guessing.
- `GrindFinder`: arbitrates native rails/tracks versus arbitrary block edges and preserves active rail kind through transfers.
- `GrindTraversal`: continuation, curve shaping, junction steering, rail hops, stuck recovery, and dimension-transition grace.
- `WallRideFinder` / `WallTraversal`: real collision-based wall detection and continuation.
- `VanillaWorldPhysics`: surfaces, redstone rails, enchantments, block materials, external impulses, bounce/sticky contact, and micro-terrain continuity.
- `RideMotion`: input-driven ground acceleration, steering, air control, neutral/standstill authority, and boost while preserving above-cap external momentum.
- `JetSetMovement` fluid boundary: immediately yields the complete velocity vector and pose to Minecraft in water, lava, or swimming states.
- `RideStyle`: handling data for inline, quad, board, BMX, hoverboard, and scooter.

## Animation and combat layering

JetSetCraft registers two PlayerAnimator layers:

- `ride_lower_body` at lower priority for ride, boost, grind, manual, powerslide, wallride, and ordinary trick clips. These clips are validated to exclude arms, hands, held-item bones, and head.
- `style_full_body` at higher priority for dances and low-speed ground stunts. This layer is presentation-only and is suppressed when a weapon overlay or active item action needs upper-body ownership.

Hands-free BMX and scooter handlebar clips may use the arms only while the rider is genuinely moving and both hands are empty. At rest, while swimming, or when any held-item/weapon presentation takes authority, both animation layers disengage and vanilla/owning-mod poses resume.

`tools/validate_assets.py` enforces the separation. A clip cannot silently seize weapon bones merely because it looks correct in isolation.

## Rendering and accessibility

`RideGearLayer` renders synchronized equipment without creating vehicle entities. Dedicated models exist for hoverboard and scooter. While dancing or performing a full-body ground stunt, ride equipment is hidden to avoid clipping. `reducedMotion` disables camera roll, speed FOV pulses, hover bobbing, and rapid equipment stunt rotations while retaining the complete gameplay state.

## Optional ecosystem integration

- Create code is compile-only and guarded behind the compatibility adapter.
- TACZ uses its public `IGun` API when present.
- Aether and Twilight Forest surface/grind/wall behavior is expressed through non-required datapack entries.
- Unknown Forge rails follow `BaseRailBlock` behavior; unusual blocks can opt in through JetSetCraft tags.

## Optional Rival Rush mode

Rival Rush owns a separate session boundary inside the existing mod. `RivalRushManager` is the server authority for entry, encounter ownership, mode vitality, rewards, and exit; `RushRun` holds the temporary clock and build. The normal movement and graffiti systems still supply actual riding and painting actions. The mode does not turn ordinary mob kills into run progression.

`RushReturnState` captures the supported normal player state before entry. `RushPlayerStorage` requires a synchronous individual-player save acknowledgement before the manager lends equipment or changes dimensions. The album is stored separately from that temporary return journal, so collected cards survive a completed return without importing temporary items or Rush XP into survival. Login, logout, clone, respawn, and unexpected dimension changes have explicit recovery paths. This is not a generic snapshot of every capability that another mod might attach.

Every rival, paint projectile, and card pickup belongs to a player/run pair. Reward claims and upgrade choices validate that identity on the server. `RushWorld` decorates already-loaded district chunks incrementally, and `RushRosters` reads named crew casts from datapacks. Players share the dedicated dimension, with separated starting positions and independent encounter ownership; it is not an instance-per-player world or a cooperative party mode.

The shared `JetSetDataProvider` renews its capability handle after invalidation while retaining the same data object. Old handles still invalidate, and the entity's own validity gate remains authoritative. This preserves the physical ride across real dimension transitions without reviving a removed host.

See [Rival Rush implementation and design](RIVAL_RUSH_DESIGN.md) for current behavior, compatibility limits, and proposals that are not yet implemented.

## Verification architecture

The repository verifies implementation in layers:

1. deterministic model, animation, and brand generation;
2. JSON, model, animation, gameplay-contract, and wiki validators;
3. ForgeGradle production build;
4. fifteen real Forge GameTests: the nine existing movement, combat, Street Gear, graffiti, and paint-splash cases plus six Rival Rush cases for rules, albums, return journals, real session transitions, ownership, and objectives;
5. real dedicated-server startup, followed by a normal stop and completed world save;
6. an unattended real-client Rival Rush audit that captures setup, the district HUD, album, and result while checking an actual inventory/vanilla-XP round trip;
7. exact tested-source and binary artifact publication.

The historical v0.3.0 release evidence contains nine GameTests and its own ride/HUD/graffiti captures. The fifteen-test gate applies to the Rival Rush development line; it does not rewrite that release's provenance. Automated integration checks do not establish thirty-minute pacing or long Endless performance.

The v0.3.0 release uses dense original item meshes and renderer-agnostic player/mob equipment layers. Any later presentation upgrade must preserve the current gameplay state machine, combat composition, accessibility settings, source-mob identity, and optional-mod isolation.
