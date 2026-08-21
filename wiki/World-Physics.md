# World Physics

JetSetCraft treats Minecraft mechanics as movement vocabulary. The server samples the real block, friction, fluid, effects, enchantments, impulses, rail state, and collision geometry instead of replacing the world with a fixed vehicle controller.

## Surface language

| World mechanic | JetSetCraft behavior |
| --- | --- |
| Ice | Higher coasting and speed |
| Packed ice | Stronger route speed |
| Blue ice | Extreme 2.15× default top-speed profile, intentionally evoking boats on blue ice |
| Frosted ice | Frost Walker-created routes remain meaningful |
| Slime | Preserves vertical impact into a configurable bounce |
| Honey | Drag, braking, and airborne side-contact behavior |
| Soul sand / soul soil | Slow by default; Soul Speed on skate footwear converts it into a route |
| Snow layers | Drag scales with snow depth |
| Cobweb / powder snow | Real movement traps instead of ignored decoration |
| Magma / cactus / campfire | Hazard-aware edge rejection and normal damage semantics |
| Water | Retains currents, bubble columns, Depth Strider, and Dolphin's Grace composition |
| Lava | Heavy but controllable movement without pretending it is normal ground |

## External impulses

JetSetCraft compares the previous solver-written velocity with the next server-tick velocity. Real external changes—explosions, pistons, knockback, moving contraptions, currents, and other mods—are captured into a short impulse-preservation window. Steering is temporarily reduced rather than allowing the ride solver to erase the impulse.

## Micro-terrain continuity

Collision-verified rises up to the configured step height can preserve a ride across slabs, stairs, snow, and similar small obstacles. Full blocks remain real obstacles. The system checks clearance and cooldowns; it does not enable generic step-height clipping.

## Datapack hooks

Modpack authors can extend:

- `jetsetcraft:boost_surfaces`
- `jetsetcraft:low_friction_surfaces`
- `jetsetcraft:bounce_surfaces`
- `jetsetcraft:sticky_surfaces`
- `jetsetcraft:brake_surfaces`
- `jetsetcraft:hazard_surfaces`
- `jetsetcraft:fluid_skimmable`
- `jetsetcraft:grindable`
- `jetsetcraft:wallrideable`
- `jetsetcraft:no_grind`

See [[Modpack Maker Guide|Modpack-Maker-Guide]] for optional-entry syntax.
