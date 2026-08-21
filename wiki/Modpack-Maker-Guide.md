# Modpack Maker Guide

JetSetCraft exposes server configuration and Forge datapack tags so a pack can teach the movement solver about new materials without adding Java dependencies.

## Optional tag entries

Use Forge's non-required object form when the referenced mod may be absent:

```json
{
  "replace": false,
  "values": [
    "minecraft:blue_ice",
    { "id": "examplemod:neon_glass", "required": false }
  ]
}
```

A missing optional registry entry is ignored. Do not use a plain string for a block from a genuinely optional mod, because vanilla tag loading treats a missing required entry as an error.

## Choose the narrowest hook

- Use `boost_surfaces` for active acceleration/route rewards.
- Use `low_friction_surfaces` for coasting and cap behavior.
- Use `bounce_surfaces` only when vertical rebound fits the block's identity.
- Use `sticky_surfaces` or `brake_surfaces` for drag/control.
- Use `hazard_surfaces` to keep edge detection away from damaging geometry.
- Use `grindable` for intentional shapes that should win candidate scoring.
- Use `wallrideable` for surfaces that should remain eligible even when unusual collision/material rules would reject them.
- Use `no_grind` as the final safety override.

## Balance guidance

JetSetCraft preserves legitimate above-cap momentum. A very large multiplier can therefore compound with blue ice, powered rails, external impulses, boost, and modded effects. Test the complete route, not only a single block.

Recommended acceptance checks:

1. Enter and leave the surface at ordinary speed.
2. Enter above cap from boost or an explosion.
3. Test slopes, corners, chunk boundaries, and dimension transitions.
4. Test with low and high server TPS.
5. Test a real multiplayer client, not only integrated single-player.
6. Confirm hazards, walls, and internal block seams do not become accidental rails.

## Pack defaults

Server/common values live in the Forge common/server config. Client HUD and camera preferences stay client-side. Do not force camera roll or FOV effects as a server requirement; players may need reduced motion.
