# JetSetCraft — Bedrock Skating Reference & Trick/Grind Surface Catalog

**Reference snapshot:** 2026-08-20  
**Purpose:** Preserve skating/vehicle research and translate it into JetSetCraft world-interaction requirements.  
**Important:** The products below are reference/inspiration material. A feature appearing in a reference pack does **not** automatically make it a JetSetCraft requirement. Explicit JetSetCraft wants are tracked separately below.

## Explicit JetSetCraft wants captured in this update

1. Preserve the Bedrock skating/skateboarding research list as design/reference material.
2. Make the Minecraft world itself a trick playground: players should be able to grind, slide, jump, transfer, bonk, wallride, spin, grab, manual, and chain tricks on suitable Minecraft geometry rather than only on special skatepark props.
3. **Metal fence / narrow metal geometry is a required grind target.** In vanilla terms, Iron Bars are the primary example; modded metal fences/railings should be supportable through tags/geometry rules rather than hard-coded IDs.
4. Add purpose-built JetSetCraft blocks that are exceptionally good for grinding and trick construction.
5. **Add hoverboards as a first-class JetSetCraft ride/trick type**, not merely a cosmetic board.

## Bedrock reference library

This is the research snapshot gathered on 2026-08-20. Re-check version/availability before treating any external pack as a dependency or implementation source.

| Reference | Source | Useful mechanics / ideas to study | Existing-world capable? |
|---|---|---|---|
| RedSkate Rebellion [BE] | CurseForge | Roller skates, rollerblades, ice skates, skate parks, spray painting/posters, futuristic city, skating progression | Yes — `.mcaddon` |
| LB Street Skate — Lifeboat | Marketplace | 38 tricks, grinding, bailing, challenges, board customization, large trick/park vocabulary | No — Marketplace world |
| Bikes, Scooters & Skateboards — Lifeboat | Marketplace | Skateboards, roller skates, ice skates, longboards, hoverboards, bikes, scooters, tricycles; half-pipes and 180s | No — Marketplace world |
| Sports Day: Summer City — Blockception | Marketplace | Skateboard, roller skates, bicycle, scooter, surfboard, tricks, custom player animation | No — Marketplace world |
| Skateboards — Kreatik Studios | Marketplace | Nine animated tricks, board customization, halfpipes, rails, ramps, stairs, indoor/outdoor parks | No — Marketplace world |
| Hoverboards — BLOCKLAB Studios | Marketplace | Hoverboards/skateboards, trick play and obstacle courses | No — Marketplace world |
| School City — Chillcraft | Marketplace | Skateboard, scooter and bicycle traversal | No — Marketplace world |
| This Is High School — Lifeboat | Marketplace | Skateboarding through built environments rather than only dedicated parks | No — Marketplace world |
| Vehicles Pack — Dodo Studios | Marketplace | Skateboards, bikes, quads, cars, motorcycles, aircraft; broad rideable-vehicle coexistence | Dynamic World |
| BMX — Project Moonboot | Marketplace | BMX complex, paintable bikes, racing, progression/upgrades | No — Marketplace world |
| Mountain Bikes — Lifeboat | Marketplace | Multiple bike types, tracks, jumps/gaps, wheelies | No — Marketplace world |
| Suburbia — Project Moonboot | Marketplace | Bike traversal integrated into a roleplay/city environment | No — Marketplace world |
| Hockeyland — Blockworks | Marketplace | Ice skating, skating progression and sport-specific movement | No — Marketplace world |
| MINE SURFER — Pickaxe Studios | Marketplace | Board-riding/obstacle movement and unlockable boards | No — Marketplace world |
| Advanced Roleplay — Lifeboat | Marketplace | Skating as one activity inside a larger city/activity sandbox | No — Marketplace world |
| Mine BMX — Bikes for Minecraft! | CurseForge Bedrock | Inventory-item-driven BMX movement rather than a conventional mount; useful alternative control architecture | Yes — `.mcaddon` |
| BicycleCraft | MCPEDL | Craftable BMX/cruiser/mountain bikes with different stats | Yes — `.mcaddon` |
| Segway And Bike Addon | MCPEDL | Rideable bike/Segway with custom riding animation | Yes — add-on |
| BikeCraft | MCPEDL | Motorcycles, motocross, scooter, quad-bike vehicle variety | Yes — older add-on |
| CT Cubiks Skater | MCPEDL | Command-driven ollies, rotations, trick XP/unlocks and skatepark construction | Map / archival |
| Tynker skate community add-ons | Tynker | Small experimental rideable skateboard implementations | Community experiments |

### Reference links

- RedSkate Rebellion: https://www.curseforge.com/minecraft-bedrock/addons/redskate-rebellion-be
- LB Street Skate: https://chunk.gg/@lifeboat/lb-street-skate
- Bikes, Scooters & Skateboards: https://chunk.gg/en/@lifeboat/bikes-scooters-and-skateboards
- Sports Day: Summer City: https://chunk.gg/@blockception/sports-day-summer-city
- Skateboards: https://chunk.gg/@kreatik-studios/skateboards
- Hoverboards: https://chunk.gg/@blocklab-studios/hoverboards
- School City: https://chunk.gg/en/@chillcraft/school-city
- This Is High School: https://chunk.gg/@lifeboat/this-is-high-school
- Vehicles Pack: https://chunk.gg/en/@dodo-studios/vehicles-pack
- BMX: https://chunk.gg/@project-moonboot/bmx
- Mountain Bikes: https://chunk.gg/en/@lifeboat/mountain-bikes
- Suburbia: https://chunk.gg/en/@project-moonboot/suburbia
- Hockeyland: https://www.bedrockexplorer.com/marketplace-offer/blockworks/hockeyland
- MINE SURFER: https://www.bedrockexplorer.com/marketplace-offer/pickaxe-studios/mine-surfer
- Advanced Roleplay: https://www.bedrockexplorer.com/@lifeboat/advanced-roleplay
- Mine BMX: https://www.curseforge.com/minecraft-bedrock/addons/mine-bmx-bikes-for-minecraft
- BicycleCraft: https://mcpedl.com/bicyclecraft-survival-friendly-bicycles/
- Segway And Bike Addon: https://mcpedl.com/segway-and-bike-addon/
- BikeCraft: https://mcpedl.com/bikecraft-add-on/
- CT Cubiks Skater: https://mcpedl.com/ct-cubiks-skater/
- Tynker skate add-ons: https://www.tynker.com/minecraft/skate/addons/
- Marketplace skating category snapshot: https://www.bedrockexplorer.com/tags/skating

### Marketplace takeaway

Marketplace has polished skating worlds, but skating mechanics are much less common as true existing-world Add-Ons. JetSetCraft should therefore study the **interaction ideas** while keeping its own movement/trick architecture world-native, data-driven, and compatible with ordinary survival/build worlds.

---

# JetSetCraft world trick-object catalog

The goal is not a tiny whitelist. JetSetCraft should understand useful Minecraft geometry and allow builders to turn ordinary structures into lines.

## Tier 1 — must feel excellent for grinding

These should be primary acceptance-test surfaces.

| Minecraft object / geometry | Intended interaction |
|---|---|
| **Iron Bars** | Core metal-fence/rail grind target. Fast, narrow, satisfying sparks/audio. Must handle connected bars and corners. |
| **Fences** — wood families, Nether Brick Fence, compatible modded fences | Top grind, transfers between connected segments, post bonks, jumps on/off gates where geometry permits. |
| **Walls** — cobblestone/stone/deepslate/brick/etc. | Ledge grind along wall tops; tolerate posts and connections without random detachments. |
| **Minecart Rails** — normal, powered, detector, activator | Rail-style grind path; slopes and turns are especially valuable test geometry. Riding a grind line must not require spawning a minecart. |
| **Glass Panes / Stained Glass Panes** | Narrow-edge grind analogous to iron bars, with material-appropriate sound/effects. |
| **Full-block ledges** | Grind/slide along exposed block edges and roof/parapet lines where there is a valid continuous edge. |
| **Slabs** | Low ledge grind, manual, nose/tail contact, transfers and small drops. |
| **Stairs** | Stair-set gaps, edge grinds where valid, launch/landing transitions, ride-down/ride-up trick lines. |
| **Scaffolding** | Edge tricks, transitions, drops and wall/vertical traversal opportunities without treating the whole volume as a solid rail. |
| **Trapdoors** | Thin ledge/edge tricks when closed; context-aware behavior when open. |
| **Fence Gates** | Continue a fence grind when closed and gracefully break/transfer when opened. |
| **Custom/modded railings and metal fences** | Supported via tags/shape rules, not a hard-coded vanilla-only list. |

## Tier 2 — world objects that should participate in tricks

These do not all need identical “rail grind” behavior. They should provide the most natural trick interaction for their shape.

| Object / family | Interaction ideas |
|---|---|
| Chains | Grab/swing/pole-like transitions where orientation permits; custom horizontal chain/rail variants may grind. |
| Lightning Rods | Pole bonk, stall, short balance/grind contact, transfer point. |
| End Rods | Narrow pole/rail contact, stall/bonk/transfer. |
| Ladders | Wallride/wallrun transition, wall tap, jump-off, vertical line continuation. |
| Vines / Weeping Vines / Twisting Vines | Grab/transfer or soft traversal interactions rather than hard rail grinding. |
| Signs / Hanging Signs | Bonk/tap/clearance tricks; hanging signs can become traversal markers without making tiny text hitboxes frustrating. |
| Doors | Wall tap/bonk/clearance interaction; never trap movement because of animation/state changes. |
| Buttons / Levers | Trick-trigger targets or optional combo interactables, not mandatory collision snags. |
| Pressure Plates | Optional line triggers, score gates, boost/event triggers, or park scripting hooks. |
| Bells | Bonk/ring trick target with audiovisual feedback. |
| Anvils | Short heavy ledge/stall surface. |
| Stonecutters | Hazard/stall object; contact rules should be deliberate and configurable. |
| Campfires / Soul Campfires | Hazard gap/clearance object; successful clears can score while failed contact obeys normal damage rules. |
| Cauldrons | Lip stall/bonk/manual target. |
| Hoppers | Short industrial ledge/stall geometry. |
| Brewing Stands | Small obstacle/bonk target rather than a rail. |
| Chests / Barrels / Shulker Boxes | Box/funbox-style manuals, slides and hops without breaking inventory interaction. |
| Beds | Soft obstacle / bounce-style trick target where appropriate. |
| Bookshelves / Chiseled Bookshelves | Ledge/obstacle use in interior lines. |
| Pistons / Moving piston heads | Dynamic trick obstacle, launch/transfer timing. |
| Slime Blocks | Bounce-launch tricks and vertical combo continuation. |
| Honey Blocks | Sticky slowdown/drag surface with distinct movement feel. |
| Ice / Packed Ice / Blue Ice | Low-friction slide/skate surface with speed-retention differences. |
| Soul Sand / Soul Soil | High-drag terrain that changes line planning. |
| Mud | Drag/soft-terrain behavior. |
| Snow layers / Powder Snow | Variable-height soft terrain and risk/recovery behavior. |
| Logs / Stripped Logs | Natural log grind/slide when horizontal; tree/stump bonks when vertical. |
| Bamboo | Pole/bonk/clearance interaction and dense-obstacle lines. |
| Leaves / hedge builds | Soft/top-skim traversal where collision makes sense, with forgiving recovery. |
| Cactus | Hazard gap/clearance target; do not silently remove vanilla danger. |
| Rooftops / parapets | Major free-roam grind/ledge environment. |
| Bridges | Railings, beams and edges should form long lines. |
| Stair rails / balconies | Continuous edge detection should make architectural builds naturally skateable. |
| Boats / Minecarts | Optional moving-object landings/transfer tricks if multiplayer-safe and predictable. |

## Terrain/material behavior worth supporting

Surface material should influence **feedback and selected physics**, but must not make basic traversal unpredictable.

- **Metal:** fastest/cleanest grind feel; sparks and metallic audio.
- **Stone/concrete:** rougher grind/slide audio and slightly heavier friction.
- **Wood:** softer sound and somewhat more drag.
- **Glass:** glass-like sound, visually distinct contact; avoid random block breaking by default.
- **Ice:** extremely low rolling/sliding friction and long momentum retention.
- **Slime:** bounce/launch interactions.
- **Honey:** sticky slowdown.
- **Soft terrain:** snow/mud/soul terrain changes acceleration and recovery.
- **Hazards:** fire, magma, cactus and similar blocks preserve understandable Minecraft consequences unless a game mode/config explicitly changes them.

---

# Purpose-built JetSetCraft park/building blocks

Ordinary Minecraft blocks should work, but creators also need pieces that are **perfectly readable and reliable** for trick-line construction.

## Grind Rail family — required direction

A purpose-built narrow rail system designed around continuous trick paths:

- Straight Grind Rail
- Corner Grind Rail
- Up-Slope Grind Rail
- Down-Slope Grind Rail
- Short/Low Grind Rail
- Tall Grind Rail
- End Cap
- Junction / branch piece
- Curved rail segments where the engine supports smooth pathing
- Iron/steel visual family
- Copper family, including oxidation/waxed visuals
- Paintable/dyeable or style variants where practical

### Grind Rail acceptance behavior

- Smoothly chains through straight pieces, corners and slopes.
- A visible rail corresponds to the actual grind path; no invisible offset collision.
- Entry can occur from either direction.
- Player can intentionally jump off, transfer, reverse or continue.
- Speed and balance behavior are deterministic enough for skill play.
- Multiplayer authority cannot desync the rider from the rail.
- Rail paths are discoverable by AI/debug tooling for park testing.

## Grind Ledge family

Purpose-built ledges for skateboard/skate/hoverboard slides and grinds:

- Low grind box
- Tall grind box
- Thin ledge
- Wide ledge/manual pad
- Corner ledge
- Sloped ledge
- Coping block for bowls/ramps

## Grind Strip — recommended outside-the-box building tool

A **thin attachable edge strip** that can be placed on an existing block edge to convert almost any build into a deliberate, perfect grind line without replacing the underlying block.

Desired behavior:

- Attaches to eligible top/side edges.
- Preserves the original block’s texture/build identity as much as possible.
- Provides one clean continuous grind path.
- Has subtle variants (metal, painted, invisible/debug-builder variant if appropriate).
- Can bridge awkward vanilla collision seams in creator-built parks.
- Should never be required for normal world grinding; it is a precision creator tool.

## Ramp / park primitives

Recommended creator pieces for lines that vanilla blocks cannot express smoothly:

- Kicker ramp
- Bank ramp
- Quarter pipe
- Halfpipe
- Bowl / curved transition pieces
- Spine
- Funbox
- Manual pad
- Stair set
- Hubba ledge
- Handrail
- Wallride panel
- Launch ramp
- Landing ramp

The important requirement is **smooth collision/pathing**, not merely decorative curved models over blocky collision.

---

# Trick vocabulary the world geometry should enable

This is a design target / recommended interaction vocabulary, not a promise that every named trick ships in the first implementation.

## Universal movement/trick concepts

- Jump / ollie-equivalent
- Nollie/reverse-pop equivalent where equipment supports it
- 180 / 360 / 540 / 720+ rotations
- Grabs
- Manuals / nose manuals / balance lines
- Grinds and slides
- Stall
- Bonk / tap
- Wallride
- Wallrun / wall-transition where equipment allows it
- Gap / transfer
- Drop-in
- Revert / direction change
- Air-to-grind and grind-to-air transfers
- Grind-to-grind transfers
- Ramp-to-wall and wall-to-rail transfers
- Combo chaining with intentional landing/recovery states

## Skateboard-specific reference vocabulary

- Kickflip / heelflip family
- Pop shove-it family
- 50-50
- Boardslide
- Lipslide
- Nose/tail slide
- Nose/tail grind
- Crooked-style / feeble-style / smith-style families where animation/stance support is good enough

## Roller-skate / inline-skate reference vocabulary

- Soul-style grinds
- Royale-style grinds
- Makio-style one-foot grinds
- Frontside/backside rail positions
- Cess slide / powerslide-style ground moves
- Wallride/wall-kick transitions
- Spins, grabs and gap transfers

---

# Hoverboards — explicit JetSetCraft requirement

Hoverboards must be a **real movement class** with their own feel, animation and trick identity.

## Core hoverboard behavior

- Constant small hover gap over ordinary ground.
- Smooth acceleration with lower rolling resistance than wheeled boards/skates.
- Momentum-focused carving/turning rather than behaving like a reskinned minecart/horse.
- Jump/pop/air control and rotations.
- Full access to the combo/trick system.
- Can grind purpose-built rails and eligible world rails/ledges.
- Distinct hover-grind presentation: magnetic/energy lock, controlled float, sparks/energy effects appropriate to theme.
- Intentional jump-off and rail transfer at any time.
- Equipment-specific sound/particles can be customized without changing gameplay logic.

## Hoverboard environment interactions to explore

These are recommended design directions to prototype rather than unconditional requirements:

- Brief water-skim behavior while sufficient speed is maintained.
- Small terrain-gap smoothing so carpet/slabs/minor block-height changes do not constantly kill momentum.
- Charged/boost trick that spends a meter earned through clean combos rather than free permanent sprint speed.
- Magnetic-style rail attraction only inside a narrow assist window; never snap the player across large distances.
- Different board archetypes such as speed, control, trick/air and balanced, while avoiding pay-to-win/stat bloat.

## Hoverboard guardrail

The hoverboard must not trivialize the whole movement system. It should be easier over rough micro-terrain but still reward line choice, timing, landings, rail entry and combo skill.

---

# Technical interaction model recommendation

To make “grind almost anything sensible” scale to vanilla updates and other mods, do **not** build the system as hundreds of hard-coded `if block == ...` checks.

Recommended architecture:

1. **Data-driven tags/registries** for explicit grindable, slideable, wallrideable, launchable and excluded blocks.
2. **Collision-shape/edge analysis** to discover viable continuous edges on ordinary blocks.
3. **Material profile** separate from geometry so iron/wood/stone/glass can share path logic but produce different feedback/friction.
4. **Continuous path solver** across connected block shapes so fences, walls, panes, rails, ledges, corners and slopes do not detach at every block boundary.
5. **Mod compatibility hooks/tags** allowing other mods and datapacks/configs to opt blocks in/out without a JetSetCraft code release.
6. **Manual override/debug tool** for creators to visualize detected grind paths, entry points, normals, gaps and invalid seams.
7. **Server-authoritative movement with client prediction/interpolation** so grinding and fast traversal remain responsive without multiplayer desync/teleport correction.
8. **Accessibility/config assists** for rail-entry forgiveness, balance difficulty, camera motion and motion effects without removing the underlying skill system.

## Suggested interaction tags

Names are illustrative and can be adapted to the actual mod namespace/API:

- `jetsetcraft:grindable`
- `jetsetcraft:grindable_metal`
- `jetsetcraft:grindable_ledge`
- `jetsetcraft:slideable`
- `jetsetcraft:wallrideable`
- `jetsetcraft:launch_surface`
- `jetsetcraft:soft_surface`
- `jetsetcraft:hazard_surface`
- `jetsetcraft:no_grind`
- `jetsetcraft:no_trick_collision`

---

# Initial acceptance-test world

A future JetSetCraft test world should contain one compact line that proves the system is genuinely world-integrated:

1. Build speed on ordinary stone/concrete.
2. Jump onto **Iron Bars** and grind through a connected section.
3. Transfer onto a fence line.
4. Gap to a stone wall/roof ledge.
5. Drop to a slab/manual pad.
6. Hit stairs and a purpose-built kicker.
7. Land on minecart rails, including a slope/turn.
8. Transfer to a purpose-built Grind Rail corner/slope sequence.
9. Wallride a supported wall section.
10. Finish on a custom Grind Ledge / funbox.
11. Repeat the line with roller skates/inline skates, skateboard and **hoverboard**, confirming equipment-specific feel without breaking the shared world geometry.
12. Repeat in multiplayer and confirm there are no visible rail desyncs, rubber-banding, stale positions or forced dismounts at block seams.

---

# Scope boundary

The Bedrock products above are **reference material**, not code or asset dependencies and not automatic feature commitments. The durable requirements added by the user in this update are:

- broad Minecraft-world trick/grind interaction;
- metal-fence/Iron-Bar-style grinding;
- purpose-built blocks that make excellent grind/trick lines;
- hoverboards as a first-class JetSetCraft ride/trick type;
- preservation of this Bedrock research for future JetSetCraft design/implementation decisions.
