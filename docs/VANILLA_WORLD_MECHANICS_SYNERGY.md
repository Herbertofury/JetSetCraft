# JetSetCraft — Vanilla World Mechanics Synergy

**Status:** Core design doctrine  
**Captured:** 2026-08-20  

## Northpoint: Minecraft itself is the moveset

JetSetCraft should not feel like a skating minigame pasted on top of Minecraft. The existing world, block states, redstone, materials, hazards, fluids, status effects, physics and terrain should actively participate in movement and tricks in ways that are immediately understandable to a Minecraft player.

The desired feeling is: **“Of course that block does that.”**

A player should be able to look at an ordinary vanilla build and start mentally seeing routes, boosts, bounces, transfers, shortcuts, hazards and trick lines without needing every useful object to be a JetSetCraft-specific prop.

This is a **core JetSetCraft requirement**, not optional polish.

---

# Primary vanilla interaction matrix

## Rails and redstone rails

### Normal Rail
- Grindable as a natural narrow rail path.
- Curves and slopes remain continuous grind paths.
- Momentum carries naturally through rail geometry instead of snapping at block boundaries.

### Powered Rail
- **Powered = boost.** Grinding/riding across a powered powered-rail accelerates the player, analogous to how it accelerates minecarts.
- Chained powered rails create intentional high-speed lines.
- Boost strength should compose with current momentum instead of replacing velocity with a fixed number.
- **Unpowered powered rail = brake/drag**, analogous to the vanilla minecart behavior.
- Redstone state changes should affect the rail immediately, allowing player-built boost/brake courses.

### Detector Rail
- Crossing/grinding a detector rail should be able to produce a redstone signal while a JetSetCraft rider is in contact with it.
- This makes vanilla redstone capable of building timers, gates, lights, doors, score triggers, route switches and trick-course machinery without special JetSetCraft logic blocks.
- It should remain compatible with normal minecart detection behavior.

### Activator Rail
- Treat powered activator rails as a general **ride-action trigger** rather than an ordinary boost rail.
- Default behavior should be something readable and useful to movement, such as a pop/launch/action pulse, while exposing a data/config hook for map and mod creators.
- It should be possible to use activator rails to trigger equipment-specific actions without hard-coding every future ride type.

## Ice family — boat-on-ice speed fantasy

JetSetCraft should deliberately embrace Minecraft’s famous boat-on-ice behavior.

### Ice
- Low friction.
- Noticeably higher top speed and momentum retention.
- Wider, driftier carving at speed.

### Packed Ice
- Faster/cleaner than ordinary ice.
- Excellent for long traversal lines and race courses.

### Blue Ice
- **Extreme speed surface.** This should be the “holy crap” version of skating/hoverboarding, inspired by boats on blue ice.
- High momentum retention and very long braking distance.
- Requires meaningful steering skill at maximum speed.
- Must remain performant and multiplayer-stable at the high velocities it enables.

### Frosted Ice
- Same slippery fantasy but temporary/unreliable, creating naturally risky timed routes.

### Design rule
- Do not arbitrarily clamp ice until it feels like ordinary ground. High-speed ice is part of the feature identity.
- Safety should come from good collision prediction, chunk/loading safeguards and sane maximum physics limits rather than making the mechanic boring.

## Slime Blocks

- **Bounce.** Landing onto slime while skating/boarding should preserve Minecraft’s recognizable bounce behavior.
- Horizontal momentum should carry into the bounce instead of being erased.
- A successful slime bounce should preserve/continue a combo.
- High-speed landings can produce higher/farther launches within controlled physics limits.
- Side impacts can support deliberate wall-bounce/rebound tricks where geometry permits.
- Sneak/crouch behavior should respect vanilla expectations where practical, allowing players to suppress or modify bounce behavior.
- Slime pistons can create player-built launchers and moving trick machinery.

## Honey Blocks

- Strong drag/braking surface.
- Sticky wall contact can support a short wall-stall/slow slide rather than behaving like a normal wall.
- Horizontal momentum drops significantly when crossing honey.
- Useful as a **vanilla brake pad** for courses and high-speed lines.
- Honey/slime distinction must remain obvious through physics, sound and particles.

## Soul Sand / Soul Soil

- Strong drag by default, preserving vanilla terrain identity.
- If JetSetCraft equipment can meaningfully support **Soul Speed**, allow the enchantment to convert these blocks from hazards into high-speed specialty routes rather than ignoring the enchantment.
- This creates an elegant vanilla-derived build choice: slow terrain for everyone, specialized fast line for equipped riders.

## Snow and Powder Snow

- Snow layers create small, forgiving surface-height changes instead of constantly destroying momentum.
- Deeper snow adds drag.
- Powder Snow remains a genuine sink/trap hazard unless equipment or enchantments explicitly counter it.
- Powder-snow recovery can become part of trick/route risk rather than silently disabling vanilla behavior.

## Cobwebs

- Extreme slowdown / emergency brake.
- Retain vanilla trap identity.
- Useful to map creators as a natural catch-net, finish-line brake or failed-route punishment.

## Water

- Entering deep water normally kills most ground momentum and transitions out of ordinary skating behavior.
- Shallow water should create drag without feeling like hitting an invisible wall.
- **Hoverboards may skim across water while above a minimum speed**, converting momentum into a temporary water-surface route.
- Water-skimming gradually loses speed unless another mechanic sustains it, so oceans do not become free infinite roads.
- Flowing water can push or bias drift in its flow direction.
- Splashdown, water exit and shore transitions should preserve as much physically sensible momentum as possible.

## Bubble Columns

- Soul Sand bubble columns provide upward vertical route possibilities.
- Magma bubble columns pull downward and can turn a water route into a risky drop/transfer.
- Airborne/skimming riders that intersect a bubble column should inherit the expected vertical impulse rather than ignoring it.
- This enables fully vanilla underwater/harbor trick courses.

## Magma Blocks

- Remain dangerous.
- Heat/damage should not be magically neutralized just because the player is in a combo.
- Can be used as a risk/reward surface or route constraint.
- Underwater magma retains its bubble-column pull behavior.

## Pistons and Sticky Pistons

- Moving piston geometry can push, redirect or launch riders.
- Piston launchers should be a supported emergent course-building technique, not treated as an exploit to suppress.
- Sticky pistons + slime/honey should retain their vanilla machinery behavior so players can build moving ramps, launchers, gates and timing obstacles.
- Movement code must tolerate blocks moving while a trick line is being approached without desyncing the player.

## TNT and explosions

- Preserve vanilla explosion knockback.
- If an explosion launches a rider into the air and they recover cleanly, the resulting air time can continue into tricks rather than forcibly cancelling the system.
- Damage and destruction rules remain vanilla/config-driven; JetSetCraft should not make TNT safe by default.
- This allows emergent “blast launch” challenge courses without inventing a special cannon block.

## Ladders, vines and climbable surfaces

- Can be used as wall-transition/grab surfaces when approached with valid movement.
- A player can jump/transfer away without an awkward forced stop.
- Normal vanilla climbing behavior remains available when the player actually intends to climb.

## Scaffolding

- Forgiving vertical traversal/transfer object.
- Edge tricks and drops should respect its unusual collision shape.
- Falling through scaffolding should not incorrectly snap the rider onto an invisible grind path.

## Doors, trapdoors and fence gates

- Their **actual open/closed block state** matters to the route.
- Closed states may provide valid ledges/continuations where geometry supports it.
- Open states should remove or change those paths immediately.
- A redstone-powered moving door/gate can therefore become a real timing obstacle.

## Iron Bars, fences, walls and panes

- Remain first-class grind geometry.
- Connected shape changes should be detected from the actual block state.
- Corners, intersections and posts should create intentional transfer possibilities rather than random detachments.

## Stairs and slabs

- Movement smoothing should make normal stairs/slabs feel rideable at speed.
- They can serve as low jumps, stair gaps, ledges, manuals and transitions.
- Micro-height changes should not arbitrarily zero velocity.

## Logs and natural geometry

- Horizontal logs are natural grind/slide surfaces.
- Vertical logs/tree trunks support bonks/wall interactions rather than pretending to be horizontal rails.
- Forests should naturally produce trick opportunities without requiring a skatepark build.

---

# Vanilla material language

The same geometry can feel different depending on Minecraft material.

| Material | JetSetCraft feel |
|---|---|
| Metal / iron | Clean, fast grinds; strong sparks and metallic audio |
| Copper | Metal-like grind; visual/audio variation can follow oxidation state without making old copper unusably slow |
| Stone / deepslate / concrete | Rough, solid slide/grind with moderate friction |
| Wood | Softer contact sound and slightly more drag |
| Glass | Slick contact, glass audio, no arbitrary block breaking |
| Ice | Very low friction and high speed |
| Slime | Elastic bounce / rebound |
| Honey | Sticky drag / braking |
| Snow / mud / soul terrain | Progressive drag and terrain challenge |
| Hazard blocks | Retain understandable vanilla consequences |

The material system should be data-driven so modded blocks can inherit a profile through tags rather than requiring bespoke code for every block ID.

---

# Status effects should remain meaningful

JetSetCraft should respect vanilla movement-affecting effects instead of replacing player physics with an isolated movement simulation.

- **Speed:** increases useful acceleration/top-end in a controlled way.
- **Slowness:** visibly reduces acceleration and movement.
- **Jump Boost:** increases pop/jump potential.
- **Slow Falling:** changes aerial timing and landing windows.
- **Levitation:** interrupts normal ground movement and becomes an air-state interaction rather than being ignored.
- **Dolphin’s Grace:** can improve relevant water transition/skimming behavior where sensible.
- Beacon-applied versions of these effects should work consistently with potion-applied versions.

Avoid double-applying vanilla multipliers. The goal is predictable composition, not exponential speed bugs.

---

# Enchantments can create vanilla-native build strategies

Where equipment architecture allows it, JetSetCraft should integrate useful vanilla enchantments instead of making them irrelevant.

## Frost Walker
- Creates ice routes that JetSetCraft can immediately exploit for speed.
- This can enable temporary self-made lines over water.
- If skates/boots are mutually exclusive, consider allowing compatible JetSetCraft footwear to accept Frost Walker rather than deleting the interaction.

## Soul Speed
- Converts soul terrain from a major slowdown into a specialized traversal surface.

## Feather Falling
- Reduces damage from large trick drops according to vanilla rules without automatically guaranteeing a successful trick landing.

## Depth Strider
- Reduces water-related slowdown for applicable transitions if the equipment slot model permits it.

The rule is not “every enchantment must affect skating.” It is: **when vanilla already describes a relevant physical behavior, JetSetCraft should compose with it rather than bypass it.**

---

# Redstone should be able to build skateparks without a JetSetCraft scripting mod

A major acceptance target is that a technically skilled Minecraft player can build a dynamic trick course from mostly vanilla components.

Examples:

1. Detector rail senses the rider.
2. Redstone opens a timed iron door.
3. Powered rails activate and create a boost lane.
4. Pistons move a ramp or rail into place.
5. Slime piston launches the rider to a rooftop.
6. A second detector rail triggers lights/note blocks and starts the next obstacle.
7. An unpowered powered-rail segment becomes a braking zone.
8. Honey acts as an emergency physical brake if the rider misses the intended line.

JetSetCraft should expose redstone-compatible signals/hooks where doing so makes sense, but avoid inventing special blocks when a vanilla component can already communicate the idea.

---

# Environment and weather

Environmental state should influence feel carefully, without turning normal play into random frustration.

- Rain can add subtle slipperiness to exposed smooth surfaces and stronger audiovisual wetness, but should not make controls unusable.
- Snow accumulation can gradually alter route height/drag where vanilla snow layers exist.
- Water flow should bias movement where the player is actually in contact with water.
- Lightning remains dangerous. A future charged-rail or lightning-rod interaction may be explored, but should preserve understandable vanilla risk rather than becoming unexplained free speed.
- Day/night should not arbitrarily alter physics unless another vanilla mechanic causes the change.

---

# Preserve emergent vanilla physics

This is a broad guardrail:

**Do not cancel a vanilla impulse just because JetSetCraft currently owns the player’s movement state.**

Examples:

- Explosion knockback should launch the rider.
- Slime should bounce the rider.
- Water currents should push the rider.
- Bubble columns should move the rider vertically.
- Pistons should push the rider.
- Entity knockback should still matter.
- Falling blocks/moving obstacles should still collide according to Minecraft rules.
- Status effects should still modify relevant movement.

JetSetCraft should then intelligently transition that impulse back into its trick/momentum system where possible.

This is what makes surprising player-created lines possible.

---

# Equipment-specific use of the same vanilla world

The same Minecraft mechanic can produce different but related behavior by ride type.

## Roller / inline skates
- Strong carving and direct ground response.
- Ice becomes extremely fast but more drift-prone.
- Slime bounce preserves body rotation and combo flow.
- Honey heavily drags wheels/skates.
- Rail grinds use skate-specific stance/grind animations.

## Skateboard
- Pop/ollie access makes small terrain and rail entry more deliberate.
- Ice increases roll speed and drift.
- Slime is a bounce-launch surface.
- Powered rails boost while grinding/rolling over a supported line.
- Ledges expose skateboard-specific slide/grind families.

## Hoverboard
- Smooths tiny terrain discontinuities while still respecting large obstacles.
- Blue ice can become an extreme-speed hover line rather than being ignored because the board is already hovering.
- Powered rails can magnetically/energetically boost a locked rail grind.
- Slime produces a hover-rebound/launch rather than simply suppressing contact.
- Honey can interfere with/drag the hover field near the surface.
- Water can be skimmed at sufficient speed.
- Bubble columns can create vertical hoverboard transfers over/through water.

Shared world semantics should remain recognizable even when the animation/feedback differs.

---

# Data-driven implementation doctrine

Vanilla synergy must not become a giant hard-coded switch statement.

Use composable capabilities/profiles such as:

- `surface_friction`
- `surface_boost`
- `surface_drag`
- `bounce_coefficient`
- `grind_profile`
- `hazard_profile`
- `fluid_interaction`
- `redstone_interaction`
- `moving_block_interaction`
- `material_feedback`
- `ride_type_overrides`

Suggested JetSetCraft tags / registries:

- `jetsetcraft:boost_surfaces`
- `jetsetcraft:brake_surfaces`
- `jetsetcraft:low_friction_surfaces`
- `jetsetcraft:bounce_surfaces`
- `jetsetcraft:sticky_surfaces`
- `jetsetcraft:grindable`
- `jetsetcraft:wallrideable`
- `jetsetcraft:hazard_surfaces`
- `jetsetcraft:fluid_skimmable`
- `jetsetcraft:redstone_trigger_surfaces`

Vanilla blocks should receive sensible defaults automatically. Modded blocks can then opt into the same behavior using tags/data rather than requiring JetSetCraft patches.

---

# Non-negotiable feel rules

1. **Vanilla meaning wins.** Slime bounces, honey drags, powered rail powers movement, ice is slippery/fast, hazards remain hazards.
2. **Momentum composes.** Interactions add, redirect, preserve or drain velocity instead of constantly resetting it.
3. **Block state matters.** Powered/unpowered, open/closed, flowing/still and connected geometry should affect behavior.
4. **Redstone is gameplay.** Players should be able to build dynamic courses with vanilla circuitry.
5. **Emergent behavior is a feature.** If players discover a clever route using ordinary Minecraft physics and it is stable/fair, prefer supporting it rather than patching it out.
6. **No fake vanilla.** Do not add arbitrary behavior solely because it looks themed. The interaction should follow an existing Minecraft concept or a clear physical/material expectation.
7. **Equipment has identity without breaking world semantics.** Skates, skateboard and hoverboard can respond differently, but all should recognize the same underlying world mechanics.
8. **Survival remains Minecraft.** Damage, hazards, redstone builds, terrain and status effects remain meaningful.

---

# Acceptance course: “Vanilla Physics Line”

A future test world should prove this doctrine using an almost entirely vanilla-built route:

1. Start on stone and build momentum.
2. Grind Iron Bars into a curved normal rail line.
3. Cross **powered powered-rails** and visibly accelerate.
4. Hit an **unpowered powered-rail** braking branch and verify deceleration.
5. Cross a **detector rail** that opens a redstone door and turns on lamps/note blocks.
6. Enter regular Ice, then Packed Ice, then **Blue Ice**, verifying clear speed/friction progression.
7. Launch from a piston/slime mechanism.
8. Land on a **Slime Block**, bounce, retain horizontal momentum and continue the combo.
9. Cross Honey and verify strong predictable drag.
10. Traverse Soul Sand/Soul Soil with and without applicable Soul Speed integration.
11. Transfer through flowing water and confirm current influence.
12. Use a Bubble Column for a vertical transfer.
13. Clear a magma/campfire/cactus hazard without those hazards becoming fake scenery.
14. Use a redstone-timed piston gate as a moving obstacle.
15. Take controlled explosion knockback and successfully recover into an aerial trick/landing.
16. Repeat representative sections with roller/inline skates, skateboard and hoverboard.
17. Repeat in multiplayer and verify boosts, bounces, block-state changes and redstone triggers agree between server and clients without rubber-banding.

If this course feels coherent without explanatory UI, JetSetCraft is approaching the intended **“vanilla added this”** quality bar.
