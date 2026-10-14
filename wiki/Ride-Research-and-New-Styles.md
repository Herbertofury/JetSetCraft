# Ride Research and New Styles

Research checked 2026-10-14. Recommendations below are **proposals for JetSetCraft**, not claims of implemented support or announcements by the reference games. Preserve the existing six [Ride Styles](Ride-Styles).

## What the reference games actually contribute
| Primary source | Verified reference | JetSetCraft lesson |
|---|---|---|
| [SEGA: Jet Set Radio](https://store.steampowered.com/app/205950/Jet_Set_Radio/) | Magnetically driven inline skates, tricks, graffiti and territory | Make inline skating an expressive street identity with memorable grind poses and readable routes |
| [Team Reptile: Bomb Rush Cyberfunk](https://store.steampowered.com/app/1353230/Bomb_Rush_Cyberfunk/) | Skateboarding, inline and BMX; manuals extend combos; long combos earn boost | Distinct trick vocabularies with a shared flow system; customization should reinforce identity |
| [Team Reptile: HYPERFUNK](https://store.steampowered.com/app/3183480/HYPERFUNK/) | Announced skates, skateboard and BMX; trick combos fuel faster personal boostpacks | Prototype escalating flow with reliable high-speed collision and route readability. Steam still lists release as TBA; these are advertised features |
| [Ubisoft: Riders Republic](https://www.ubisoft.com/en-us/game/riders-republic) | Mountain-bike disciplines, BMX, snowboard, skis and wingsuit sports | Biome-specific handling and mixed-discipline routes can create new challenges |
| [RageSquid: Descenders Next](https://store.steampowered.com/app/2375530/Descenders_Next/) | Snowboarding and mountainboarding; freestyle control, reputation and gear customization | Dirt and snow should feel different, and route choice should matter |
| [Roll7: Rollerdrome combat guidance](https://blog.playstation.com/2022/08/11/prepare-for-rollerdrome-with-dev-combat-tips-out-august-16/) | Tricks replenish ammo, while skating, dodging and attacking form a combat loop | Develop combat-skating challenges for existing quad skates. Any TaCZ economy adaptation is an explicit optional event rule, never silent ammunition creation |

JSR, BRC and HYPERFUNK do not supply six additional documented ride classes beyond our current set. Their strongest value is movement feel and trick variety. New families should earn their place through a new surface, control model or challenge vocabulary.

## Recommended additions, in priority order
| Proposal | Distinct play | Signature tricks / challenges | Tradeoff and customization |
|---|---|---|---|
| **Mountainboard** | Rough dirt, forest trails and rolling natural terrain | Off-road carving, powerslides, grabs, jumps through roots and terrain gaps | Less nimble on street ledges; pneumatic tires, trucks, deck and bindings |
| **Snowboard** | Snow and mountain event routes | Edge changes, buttering, grabs, spins and powder-to-rail transfers | Needs suitable snow or event surfaces; deck/base art, edges, binding stance |
| **Aquatic carveboard** | Ocean surface routes and deliberate underwater transitions | Wake slashes, surface breach jumps, coral slaloms and underwater barrel rolls | Water-only strength, limited land flow; hull/deck, fins and optional water-jet module. Original JetSetCraft proposal |
| **Mountain bike** | Climbing and fast rough downhill lines, clearly distinct from freestyle BMX | Scrubs, whips, suspension-assisted landings, technical rock/root routes | Larger turn radius and footprint; fork/shock, gears, wheels, tires and brakes |
| **Ice skates** | Tight frozen-lake racing and dance challenges | Crossovers, edge cuts, spins and paired choreography | Specializes in ice; blades, boots and guards. Original biome-led proposal |
| **Freestyle skis** | Two-foot snow control and wider mountain lines | Crossed-ski tricks, grabs, switch riding and slalom transitions | More animation and collision work than snowboard; skis, bindings and optional poles |

Top initial prototypes: **mountainboard, snowboard, aquatic carveboard**. These add dirt, snow and water identities rather than another ordinary street deck.

Aquatic riding must complement the existing goal of underwater skating/tricks for all supported ride families. It should be a specialist, not a paywall or prerequisite for entering water-mob events. Breathing, visibility, current response and oxygen rules must remain explicit.

## Styles better treated as variants first
- Longboard / surfskate: carving, pumping and downhill slides under the board family; promote to its own style only if input/handling tests show enough distinction.
- Aggressive / speed / dance inline builds and dance / derby quad builds: part sets and tuning presets within the existing skate families.
- Flatland / street / dirt BMX: trick sets and builds before multiplying base ride IDs.
- Hoverboard surf or race builds: use the requested core, thrusters, fins and trail customization.
- Rocket skates: experimental powered skate build with controlled burst resources, rather than duplicating inline with a bigger speed number.
- Parkour footwear and wingsuits: complementary movement modes, not automatically new ride slots. Preserve vanilla movement selection and Minecraft's existing equipment interactions.
- Street luge / pogo: later novelty-event prototypes; high camera, collision and content cost before they justify a full progression tree.

## Approval gate for every new style
- [ ] Demonstrates a distinct handling and trick vocabulary in an actual prototype.
- [ ] Works with terrain-aware courses, safe temporary obstacles, tagging and gang races.
- [ ] Has its own part slots, sounds, animation poses, grind/transfer behavior and unlock loop.
- [ ] Supports survival persistence, multiplayer authority and compatible enchantments.
- [ ] Has a readable failure/recovery loop and accessible controls.
- [ ] Measures startup, low-frame-time and server-tick impact against the same baseline.
- [ ] Keeps every existing ride relevant; cosmetic variants do not create hidden stat advantages.

Detailed required workshop and enchanting workflow: [Ride Workshop, Parts and Enchantments](Ride-Workshop,-Parts-and-Enchantments).

