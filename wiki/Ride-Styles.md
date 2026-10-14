# Ride Styles

## Existing six-style baseline

Every ride uses the same server-authoritative movement, trick, grind, wall-ride, world-physics, and loadout architecture. The differences are handling identities rather than isolated vehicle systems.

| Style | Identity | Best at | Tradeoff |
| --- | --- | --- | --- |
| Neo Inline Skates | Fast, direct street flow | Balanced speed, technical air lines, classic soul/royale grind vocabulary | Less steering than quad skates |
| Neon Quad Skates | Playful, dance-forward rolling | Tight steering, ground style, cross-steps, stable transitions | Lower raw top speed |
| Street Deck | Committed board lines | Strong grind multiplier, flips, slides, ledge creativity | More deliberate steering and air correction |
| Street BMX | Power and gap coverage | Highest raw cruise/boost range, large jumps, pegs and bike tricks | Lower grind multiplier and a larger visual footprint |
| Flux Hoverboard | Smooth futuristic flow | Air correction, magnetic-feeling grinds, strong coasting | Slightly below BMX's maximum boost ceiling |
| Neon Street Scooter | Nimble technical riding | Fast steering, tailwhips/barspins, compact transfers | Less raw speed than BMX and slightly less grind focus than boards |

## Dedicated loadout

Ride gear is stored as the real `ItemStack` in a persistent player capability. It does not consume a hand or the vanilla boots slot. This is why JetSetCraft can preserve combat input, equipment NBT, enchantments, death drops, save/load behavior, and future cosmetic upgrades without pretending the gear is a separate vehicle entity.

## Footwear enchantments

Inline and quad skates can accept Frost Walker, Soul Speed, Depth Strider, and Feather Falling through JetSetCraft's dedicated slot. The implementation composes with vanilla armor enchantments using max-not-sum behavior where appropriate, preventing accidental double stacking while retaining Minecraft's movement language.

## Visual equipment

All six styles have generated high-detail OBJ geometry. The hoverboard is a dedicated wheel-free model with field generators, luminous rails, and stabilizer fins; it no longer reuses the skateboard mesh. Scooter, BMX, board, skate, and spray-can models also exceed the project's production mesh quality floor.

## Approved expansion: seven additional styles
These are **approved development requirements**, not claims of released or tested gameplay. Preserve the original six above.

| New style | Handling identity and trick vocabulary | Terrain / challenge fit | Key customizable parts |
|---|---|---|---|
| Snowboard | Edge carving, buttering, grabs, spins, switch landings and rail transfers | Snowy mountains, ice-biome event courses and snowparks | Deck, top/base graphics, edges, bindings and stance |
| Wingsuit | Controlled gliding, banks, dives, proximity lines and air-to-ground trick transitions | Cliffs, canyons, aerial races and carefully bounded boss arenas | Wing/suit panels, fabric/art, harness and compatible boostpack attachment |
| Skis | Twin-ski control, slalom, crossed-ski tricks, grabs and switch riding | Snow routes with distinct lines from snowboard courses | Ski pair, bindings, attachment cosmetics and optional poles |
| Ice Skates | Quick edge cuts, crossovers, spins and synchronized dance | Frozen lakes, ice races and skating choreography | Boots, blades, holders, laces and blade trails |
| Mountain Bike | Suspension landings, whips, scrubs, climbs and rough downhill lines | Forest, mountain, dirt and rocky routes | Frame, fork/shock, gears/drivetrain, wheels/tires, brakes, bars, saddle and pedals |
| Aquatic Carveboard | Surface carving, breach jumps, underwater slaloms and barrel rolls | Ocean/water-mob challenges and surface-to-submerged routes | Hull/deck, fins, pads, propulsion module, wake and underwater trail |
| Mountainboard | Off-road carving, powerslides, grabs and rough-ground jumps | Dirt trails, roots, forest gaps and natural terrain | Deck, trucks, pneumatic wheels/tires, bearings and bindings |

Aquatic Carveboard is a specialist; other compatible rides still support the requested underwater skating and tricks. Wingsuit flight is a distinct movement mode with explicit equipment compatibility, not an excuse to overwrite vanilla armor or backpack slots.

## Earnable, customizable boostpack
The boostpack is a major progression reward usable with compatible ride builds. Unlock it through crew challenges, then customize its shell, engine/core, thrusters/nozzles, stabilizers, harness, colors, graffiti, name, lighting, exhaust/trail and sound.

Distinct clean tricks build charge for controlled ground bursts, air dashes and advanced maneuvers. Momentum, transitions and variety should capture the satisfying feel studied in **Bomb Rush Cyberfunk and HYPERFUNK**, with original tested implementation, responsive controls and reliable high-speed collision. Support compatible wingsuit-plus-pack builds and preserve ordinary backpack/armor integration.

## Full per-part ride customization
The workshop must support physical component swaps as well as color and art:
- Inline/quad skates: boots, frames/plates, wheels, bearings, laces, grind hardware and applicable stops/trucks.
- Skateboards: deck, grip art, trucks, wheels, bearings and hardware.
- BMX: frame, fork, bars/grips, wheels/tires/hubs, saddle, crank, pedals, chain, gear ratio and pegs.
- Hoverboards: body, engine/core, thrusters, field generators, fins, luminous trim, trail and sound.
- Scooters: deck/grip, bars/grips, fork, clamp, wheels, bearings, brake and pegs.
- New styles: equivalent individual parts shown in the table above.

Per-part paint/finish, decals/tags, naming, gang signature art and saved builds belong in one live-preview workshop. Cosmetics and handling choices remain independently selectable.

## Enchanting-table and combat goals
Every complete ride should support its appropriate vanilla enchanting-table, enchanted-book, anvil/rename and grindstone workflows. Enchantments remain attached to the real ride item through component swaps and cannot be duplicated. Extend meaningful vanilla enchantments and add tested movement enchantments such as **Railrunner**, **Airweave**, **Tidecarver** and **Trick Capacitor**.

The approved combat loop is **skate → dodge → trick → replenish ammunition → attack → chain the next line**, including explicit TaCZ integration. Server-validated successful tricks grant ammo rewards with variety incentives, clear HUD feedback, reload/ammo-type compatibility and anti-duplication checks. Support event presets and configurable free-roam survival behavior.

## Detailed implementation requirements
- [Approved Rides, Boostpack and Trick Combat](Approved-Ride-Expansion-and-Trick-Combat)
- [Ride Workshop, Parts and Enchantments](Ride-Workshop,-Parts-and-Enchantments)
- [Primary-source Ride Research and New Styles](Ride-Research-and-New-Styles)

**Quality gate:** keep vanilla-compatible movement selectable; preserve each ride's identity, survival persistence and multiplayer behavior; improve startup/runtime performance in parallel and verify frame-time lows without removing capabilities. Documentation approval is not implementation completion.

