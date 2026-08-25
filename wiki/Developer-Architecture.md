# Developer Architecture

## State flow

`ClientEvents → C2SInputPacket → JetSetData → DanceSystem / JetSetMovement → player velocity and style state → S2CStatePacket → ClientRideState → camera, HUD, render, and animation adapters`

The server owns movement and scoring. Client code presents synchronized state; it does not decide whether a trick, landing, dance reward, or grind attachment succeeded.

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
