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
