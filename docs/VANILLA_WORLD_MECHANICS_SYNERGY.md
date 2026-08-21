# JetSetCraft Vanilla World Mechanics Synergy

## Northpoint: Minecraft itself is part of the moveset

JetSetCraft must feel as though its movement belongs in Minecraft rather than as a sealed skating minigame layered over it. The JetSetCraft momentum/trick/grind system remains authoritative for its own mechanics, but it **composes with** Minecraft block state, material behavior, fluids, redstone, status effects, enchantments, hazards, collision shapes and external impulses.

The desired reaction is: **"Of course that block does that."**

This document mirrors the durable design doctrine stored in the JetSetCraft Drive project folder and is an implementation/acceptance contract for the Forge 1.20.1 branch.

## Non-negotiable composition rules

1. Never zero or hard-clamp a valid vanilla/modded impulse merely because JetSetCraft is active. Explosions, pistons, knockback, slime, currents and moving geometry can become movement tech.
2. Momentum is added, redirected, retained or drained. It is not repeatedly reset to canned walk/sprint values.
3. Block state matters immediately: powered/unpowered, open/closed, connected geometry, flowing/still, temporary/permanent.
4. Vanilla meaning wins: ice is fast/slippery, slime bounces, honey drags, hazards hurt, powered rails boost.
5. Redstone is gameplay. Ordinary Minecraft builds must be able to become dynamic JetSetCraft lines.
6. Geometry is data/shape-driven. Mods and datapacks can opt into surface/grind behavior without JetSetCraft source patches.
7. Ride types can express the same world differently, but must not make the world's semantics unrecognizable.
8. Survival remains Minecraft. JetSetCraft tricks do not secretly grant hazard immunity.

## Implemented 1.20.1 surface and world interactions

### Rails and redstone

- Normal rails are continuous native grind paths, including slopes/turns.
- Powered powered-rail: acceleration/boost while rolling or grinding.
- Unpowered powered-rail: strong predictable drag/braking.
- Detector rail: a JetSetCraft rider crossing/grinding it produces a short redstone pulse.
- Powered activator rail: one-shot action/pop/launch pulse, preserving combo state.
- Dynamic rail state is sampled at runtime, so redstone changes affect active routes.
- Create tracks/Bezier curves retain their native geometry integration and coexist with these vanilla semantics.

### Ice family

- Ice: faster cap, low rolling resistance, driftier carving.
- Packed Ice: clearly faster/cleaner long-distance line than normal Ice.
- Blue Ice: deliberate **extreme-speed route**, inspired by boats on blue ice; long retention and lower steering authority are part of the skill expression.
- Frosted Ice: temporary slippery speed line and immediate Frost Walker synergy.
- External speed above normal ride caps is retained and naturally decays rather than being flattened back to ordinary ground speed.

### Slime

- Landing bounce uses incoming vertical impact speed instead of a canned hop.
- Horizontal momentum and combo flow survive a clean bounce.
- Sneaking suppresses the JetSet bounce assist, respecting vanilla player intent.
- Side contact can create a controlled momentum-preserving rebound.
- Slime/piston launchers are valid emergent course mechanics.

### Honey

- Strong ground drag and emergency-brake behavior.
- Airborne side contact produces a sticky wall-stall/slow-slide.
- Jumping out of a honey wall contact gives a deliberate transfer while paying momentum.
- Honey remains distinct from slime: sticky/dragging, never elastic.

### Soul terrain

- Soul Sand / Soul Soil strongly drag normal riders.
- Soul Speed converts them into a specialist fast route instead of being ignored.
- JetSetCraft ride items are not boot-slot armor, so normal enchanted boots can coexist with skating equipment.

### Snow, Powder Snow, Cobweb, Mud

- Snow drag scales by snow-layer depth; tiny layers are less punishing than deep snow.
- Powder Snow and Cobweb body contact takes precedence over the block under the player's feet, preventing momentum from bypassing trap behavior.
- Mud is a readable soft-terrain drag surface.
- Vanilla equipment/physics remain free to contribute because JetSetCraft does not replace the player's vertical/fluid state wholesale.

### Water, lava and bubble columns

- Shallow/deep water progressively drains ground momentum instead of causing an arbitrary hard reset.
- Current horizontal motion is preserved as the base direction so flowing water can bias the line.
- Depth Strider and Dolphin's Grace improve water retention.
- Lava heavily drains momentum but is still allowed to move the rider.
- Vertical velocity is deliberately left to Minecraft so bubble columns, sinking/rising and other fluid impulses still work.
- Future hoverboard water-skimming must plug into the same `fluid_skimmable` data profile rather than bypassing water semantics.

### Status effects and enchantments

- Speed and Slowness compose with JetSet acceleration/top end without double-applying vanilla movement modifiers.
- Jump Boost affects JetSet-specific grind/wall exits; normal vanilla jumps remain vanilla-owned.
- Slow Falling and Levitation remain authoritative over vertical motion during ordinary aerial control.
- Frost Walker-created Frosted Ice is immediately understood as a speed surface.
- Soul Speed and Depth Strider are integrated as described above.
- Feather Falling remains a normal damage rule and does not guarantee a successful trick landing.

### Pistons, explosions, knockback and moving obstacles

JetSetCraft captures incoming horizontal velocity into momentum but no longer clamps an above-cap external impulse back down to the ride's ordinary cap. This is essential for:

- piston/slime launchers,
- TNT/explosion launches,
- mob/player knockback,
- modded impulses,
- downhill/rail speed,
- water flow and moving-world interactions.

The movement engine should then transition that velocity into tricks/grinds/landings where valid.

### Dynamic world geometry

The grind/edge detector works from current block state/collision geometry plus tags, so fences, walls, panes, stairs, slabs, trapdoors, gates, rooftops, logs and modded equivalents can form routes without a separate JetSetCraft prop for every shape. Open/closed and connected states naturally change the available geometry.

## Data-driven extension surface

Current block tags:

- `jetsetcraft:boost_surfaces`
- `jetsetcraft:brake_surfaces`
- `jetsetcraft:low_friction_surfaces`
- `jetsetcraft:bounce_surfaces`
- `jetsetcraft:sticky_surfaces`
- `jetsetcraft:hazard_surfaces`
- `jetsetcraft:fluid_skimmable`
- `jetsetcraft:grindable`
- `jetsetcraft:wallrideable`
- `jetsetcraft:no_grind`
- existing `jetsetcraft:grind_rails` and `jetsetcraft:grind_rail_blacklist`

Vanilla receives sensible defaults. Mods/datapacks can opt in/out without hard-coded IDs.

## Still-required deeper passes

These remain product requirements, not discarded ideas:

- high-speed collision/chunk-boundary stress testing rather than nerfing Blue Ice,
- material-specific grind friction/sounds/sparks (metal/copper/stone/wood/glass/ice),
- purpose-built creator grind rails, ledges, coping and smooth ramp/transition blocks,
- hoverboard as a first-class ride type including speed-gated water skim,
- acceptance/test-world automation for the complete vanilla physics line,
- multiplayer soak tests for redstone state changes, slime launches, fluids and extreme ice speed.

## Acceptance line

A representative test route must be able to chain ordinary ground -> Iron Bars/fence/ledge -> vanilla rail -> powered rail -> detector-triggered redstone obstacle -> Ice -> Packed Ice -> Blue Ice -> slime/piston launch -> slime landing -> honey braking -> Soul Sand/Soul Soil with/without Soul Speed -> flowing water/bubble column -> hazards -> explosion launch -> clean trick landing, and reproduce server/client state without rubber-banding.

## Style Flow optional dimension routes

`0.2.0-alpha.1` extends the same data-driven interaction language into optional dimensions without requiring either mod:

- The Aether Quicksoil, Quicksoil Glass, and Quicksoil Glass Pane are optional speed-route entries.
- Blue Aercloud is an optional bounce-route entry; JetSetCraft composes with the block rather than replacing its native vertical launch behavior.
- Aerogel and Holystone Bricks are optional wall-route entries; Quicksoil Glass Pane is also an optional grind line.
- Twilight Forest Aurora Block, Aurora Pillar, Aurora Slab, and Auroralized Glass are optional low-friction, wall, and grind route entries.

Every external registry entry is encoded as `{ "id": "namespace:block", "required": false }`. A world or server without those mods loads the same JetSetCraft build without a hard dependency.

## Style Flow completed requirements

The earlier hoverboard and material-feedback requirements are now implemented at the alpha level:

- hoverboard is a first-class ride style with persistence, tuning, dedicated mesh, animations, GameTest, and universal trick/grind/world-physics behavior;
- scooter is a first-class sixth ride style using the same server-authoritative system;
- grind feedback selects material profiles and emits Minecraft-native sound/particle language;
- the acceptance lab, runtime diagnostics, and Forge GameTests cover the core movement doctrine.

Remaining gates are real-client visual acceptance, high-speed chunk-boundary stress, optional-mod runtime smoke, and multiplayer soak. Those tests must harden the capable behavior rather than nerfing Blue Ice, removing supported geometry, or weakening momentum continuity.
