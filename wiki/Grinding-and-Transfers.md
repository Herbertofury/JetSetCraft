# Grinding and Transfers

Hold **G** near a valid path. JetSetCraft evaluates authored rails/tracks and real collision-shape ledges, scores candidates against direction and distance, and attaches to the strongest safe target.

## Supported geometry

- Vanilla rails, powered rails, detector rails, and activator rails using their real `RailShape`.
- Forge-compatible rail subclasses and datapack-opted custom rails.
- Create 6.0.8 native track axes, normals, graph bounds, junctions, slopes, diagonals, and long Bezier connections when Create is installed.
- Exposed tops of collision boxes, including fences, walls, panes, bars, logs, slabs, and modded shapes.
- Datapack `grindable` and `no_grind` tags.

The edge solver rejects internal seams, shared edges, insufficient rider clearance, hazards, and blacklisted surfaces. This prevents the common failure where every block seam becomes an accidental rail.

## Transfers

Press **Space** while grinding to hop from the current path. Momentum is preserved into the launch. Hold a direction and **G** to influence the next candidate. Junction steering and short reattach grace make rail networks feel continuous without teleporting the player through missing geometry.

A transfer contributes style and refreshes combo grace. Reattaching across a dimension change receives a bounded grace window, preventing an active path from trapping or corrupting the player.

## Corners and curves

Block-edge grinds receive an intentional corner-steering bias so exposed 90-degree turns can be followed. Rail and Create paths instead use their actual tangent/curvature. Curve-speed shaping reduces only what is needed for stability and preserves legitimate above-cap momentum.

## Rail semantics

- Powered powered-rails add configurable momentum each tick.
- Unpowered powered-rails apply configurable retention rather than a hard stop.
- Detector rails still produce redstone behavior.
- Activator rails can trigger JetSetCraft's action pulse while retaining normal rail semantics.
- Material profiles change cap, retention, sound, pitch, and particles for metal, copper, glass, wood, stone, and other surfaces.

## Owner requirement: every ride, either rail

**Approved requirement, implementation coverage still to verify.** Every ride the player uses must be able to grind the **left or right physical rail** of vanilla and modded railway tracks. Centerline-only attachment does not satisfy this requirement. Preserve all existing ride identities and make rail entry, balancing, tricks, side-switches and transfers feel as responsive and stylish as the Jet Set Radio / Bomb Rush Cyberfunk quality references.

### Movement and presentation
- Resolve each rail as a distinct continuous path using the track's real shape, gauge, tangent and surface normal. Keep the chosen rail stable through curves, slopes, diagonals and junctions; no involuntary left/right snapping.
- Let players deliberately hop between the two rails, transfer to nearby tracks/ledges, choose branches, reverse or exit safely, and chain aerial tricks back into a grind without an arbitrary combo reset.
- Use ride-specific contact poses: skate frames, board trucks/slides, BMX/bike pegs and appropriate equivalents for every additional ride. Include visible balance/lean, contextual sparks/sound and animation transitions that preserve camera readability and player control.
- Wingsuit/boostpack combinations can launch from and land into these grind chains. For minecarts/Trick Trains, preserve the separate opt-in vehicle mode and whole-vehicle clearance rather than applying a small rider collision box to a train.

### Compatibility and engineering checklist
- [ ] Audit the existing solver first; retain its useful vanilla/Create geometry and momentum behavior rather than build a competing movement controller.
- [ ] Test both rail sides on all vanilla rail types, straight/curved/sloped sections, crossings, junctions, Create Bezier tracks and supported modded gauges/materials.
- [ ] Cover all six baseline rides plus every approved new ride with actual side-specific visual/contact tests; no silent style exclusions.
- [ ] Add provider-aware adapters for modded track geometry. Moving tracks and portal transitions need explicit transform/lifecycle validation; unsupported combinations must be reported honestly, not claimed universally tested.
- [ ] Preserve rail redstone, railway signaling, trains, inventory, ordinary movement and collision safety. If another rail-grinding mod is installed, arbitrate controller ownership to prevent double acceleration or conflicting player attachment.
- [ ] Verify server authority, multiplayer prediction/correction, chunk unload, removed tracks, reattach/escape, low overhead and no repeated world-wide rail scans.

### Owner reference and reuse lead
[Create: Train Track Rail Grinding](https://www.curseforge.com/minecraft/mc-mods/create-train-track-rail-grinding) · [author's source repository](https://github.com/JuniKnytt/CreateRailGrinding). The listing checked 2026-10-14 identifies Minecraft 1.21.1 / NeoForge and MIT licensing. Its author describes momentum-preserving Create grinding and additional track integrations; these are upstream claims, not verified JetSetCraft compatibility. JetSetCraft's Forge 1.20.1 target needs a source/version/license review and adapted tests, not a blind binary dependency. Study and contribute reusable fixes upstream where appropriate.

Related: [Ride Styles](Ride-Styles), [Trick Trains & Create](Trick-Trains-and-Create), [train integration backlog #37](https://github.com/Herbertofury/JetSetCraft/issues/37).
