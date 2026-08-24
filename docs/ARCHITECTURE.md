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

## Verification architecture

The repository verifies implementation in layers:

1. deterministic model, animation, and brand generation;
2. JSON, model, animation, gameplay-contract, and wiki validators;
3. ForgeGradle production build;
4. eight real Forge GameTests for hoverboard/scooter persistence and movement, neutral/stop/swim authority, no-gear dance scoring, combat sovereignty, hostile state/input handling, mob Street Gear/Boombox lifecycle, and graffiti support cleanup;
5. unattended real-client ride/HUD/graffiti visual capture;
6. real dedicated-server startup smoke;
7. exact tested-source and binary artifact publication.

The v0.3.0 release uses dense original item meshes and renderer-agnostic player/mob equipment layers. Any later presentation upgrade must preserve the current gameplay state machine, combat composition, accessibility settings, source-mob identity, and optional-mod isolation.
