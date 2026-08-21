# Compatibility

JetSetCraft is designed to enter large adventure/combat modpacks without making those mods dependencies.

## Create 6.0.8

When Create is present, JetSetCraft uses Create's own `ITrackBlock` geometry and track graph/Bezier data. This supports real slopes, diagonals, junctions, and long curves instead of guessing from registry names. Create remains compile-optional/runtime-optional.

## TACZ and combat mods

TACZ detection uses the public `IGun` API. Dynamic camera effects are reduced while a weapon overlay is active. Ride animation is lower-body only, leaving arms, hands, head, and held-item bones free for TACZ, Epic Fight, Better Combat, vanilla items, bows, shields, spellbooks, and similar systems.

Full-body dance/ground-stunt animation runs on a separate layer and is suppressed when weapon activity is detected. Movement never uses “weapon equipped” as an implicit ride-state exit.

## The Aether

All entries are non-required datapack references:

- `aether:quicksoil`, `quicksoil_glass`, and `quicksoil_glass_pane` are boost/low-friction routes.
- `aether:blue_aercloud` participates in the bounce language while retaining its native launch behavior.
- `aether:aerogel` and `aether:holystone_bricks` can be wall-ride surfaces.
- Quicksoil glass panes can be discovered as grind geometry.

The Aether can be absent without missing-registry errors.

## Twilight Forest

Non-required entries make Aurora Palace materials especially expressive:

- Aurora blocks, pillars, and slabs can become low-friction route pieces.
- Aurora pillars, slabs, and auroralized glass can be grind targets.
- Aurora blocks, pillars, and glass can be wall-ride surfaces.

All normal dimension travel, progression, mob combat, and structure rules remain owned by Twilight Forest.

## Other rails and dimensions

Vanilla/Forge rail subclasses work through `BaseRailBlock` behavior. Modded blocks can opt into surface/grind/wall tags through datapacks. Because the movement state lives on the player and does not assume the Overworld, Aether, Twilight Forest, Nether, End, and custom dimensions use the same solver.

## Visual model mods

The lower-body animation contract improves composition with player-model and action systems, but extreme skeleton replacements may still need a dedicated adapter. JetSetCraft keeps gameplay state independent from PlayerAnimator, GeckoLib, Epic Fight, TACZ, or YSM internals so adapters can be added without replacing movement.
