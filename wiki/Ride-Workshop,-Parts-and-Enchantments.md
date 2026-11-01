# Ride Workshop, Parts and Enchantments

**Scope update:** The user has now approved seven new equipment styles, a customizable earnable boostpack and trick-based ammunition replenishment. [Approved expansion and combat requirements](Approved-Ride-Expansion-and-Trick-Combat) supersede earlier proposal-only wording about that scope. Implementation remains pending.

Status: **approved design requirements and implementation backlog**, 2026-10-14. This page does not claim the workshop, new enchantments or additional ride classes are implemented.

## Baseline to preserve
[Ride Styles](Ride-Styles) currently documents six handling identities: Neo Inline Skates, Neon Quad Skates, Street Deck, Street BMX, Flux Hoverboard and Neon Street Scooter. Its dedicated real ItemStack loadout preserves equipment NBT without consuming the hand or vanilla boots slot. It documents skate support for Frost Walker, Soul Speed, Depth Strider and Feather Falling with max-not-sum composition where appropriate. Verify the current implementation and table eligibility before extending it; an enchantment surviving NBT is not proof its effect or table integration works.

## The player's garage
A single ride workshop should let players rotate and zoom a live preview, select a physical part, swap its shape/material, recolor its separate regions, apply unlocked art or their own permitted tag, name the ride, preview its trail and sound, compare handling and save named builds. Offer reset/undo and a clearly priced confirmation before consuming survival resources. Show locked parts with their actual acquisition challenge. Preview grinding, pedaling, wheel rotation, jump, landing and nighttime glow.

Cosmetic identity and mechanical tuning are separate selections so a favorite skin remains usable with a preferred handling build. Every upgrade must explain acceleration, turning, grip, braking, boost, air control and tradeoffs. No compulsory repair chore or durability system should be added just to justify an enchantment. Cosmetic wheels and engines must visibly animate correctly; engine sound previews need independent volume controls.

## Individually customizable physical parts
| Ride | Required part slots and detail |
|---|---|
| Inline skates | Boots/shells, cuffs, liners, laces/buckles, frames, individual wheel sets, bearings, axles, soul plates and grind blocks; linked or deliberately mismatched left/right cosmetics |
| Quad skates | Boots, laces, plates, trucks, cushions, wheels, bearings, toe stops and grind blocks; dance-oriented wheel and plate options |
| Skateboard | Deck shape and top/bottom artwork, grip tape or stencil, trucks, wheels, bearings, hardware and optional rail guards; wheel patterns remain readable in rotation |
| BMX | Frame, fork, bars, grips, stem, headset, saddle/post, front/rear rims, tires, hubs/spokes, crank, pedals, chain, sprocket, rear driver/freewheel and pegs; brake/gyro visual variants |
| Hoverboard | Deck/body shell, nose/tail, foot pads, engine/core, thrusters/nozzles, field generators, stabilizer fins, luminous trim, boost sound, trail shape/color and particle pattern |
| Scooter | Deck and grip art, bars, grips, clamp, headset, fork, wheels, bearings, brake/fender and optional pegs |

BMX gear customization should include chainring/rear-driver ratios with readable acceleration-versus-cruise tradeoffs. Multi-speed shifting belongs on compatible geared-bike builds or a future mountain bike; do not depict every freestyle BMX as having a derailleur. A part may be cosmetic, functional, or both, but the UI must state which.

For later rides, define equivalent real slots: snowboard deck/base/edges/bindings; mountainboard deck/trucks/tires/bindings; mountain bike frame/suspension/drivetrain/wheels/brakes; ice skates boots/blades; aquatic board hull/fins/propulsion. These are proposed extensions, not silently added shipped classes.

## Paint, graffiti and identity
- Per-part primary/secondary/accent colors; matte, gloss, metallic and emissive finishes with clear visual preview.
- Decal layers with placement, scale, rotation and mirroring; separate deck top/bottom, frame panels and wheel faces. Reuse approved player graffiti and gang art rather than creating disconnected inventories.
- Ride nickname, builder signature, crew emblem, earned badges and optional wear presets. Rename through the anvil as well as the workshop.
- Reputation unlocks character/gang signature parts, palettes, finishes and art; challenge mastery unlocks trophies and animated cosmetic variants.
- Preview day/night and biome lighting. Combo-reactive wheels, underglow, exhaust and trails must match the music/visualizer system where a timing source is available, with a predictable fallback.
- Reduced-flash mode, trail opacity, other-player VFX limits and readable combat silhouettes. Cosmetics must not hide attack tells or blind rivals.
- Validate user art dimensions, formats and payload size; cache processed textures and permit server moderation. Never fetch arbitrary user-supplied URLs during rendering.

## Full Minecraft enchantment workflow
All six complete ride items should be eligible for appropriate **enchanting-table offers**, with normal lapis, XP, bookshelf and seed behavior. Enchanted books, anvils, repairs where applicable, rename costs, grindstones and enchantment glint must behave coherently. The workshop complements these vanilla stations.

Enchantment ownership belongs to the complete ride ItemStack. Swapping cosmetic or functional parts must preserve its enchantments and cannot clone them onto returned parts. If separately enchanted components are introduced later, specify transfer/consumption rules and an explicit anti-duplication test first.

- Define allowed ride families, level bounds, rarity/cost bands, exclusions, treasure-only status and discoverability for each enchantment.
- Table availability must be tested on the real ride item, not only on books or via commands.
- Unbreaking and Mending are useful only when the ride has a genuine supported damage/repair lifecycle; avoid XP disappearing into a non-repairable cosmetic item.
- Extend appropriate vanilla movement enchantments by explicit applicability and effect adapters. Preserve documented boot-plus-ride max-not-sum rules and vanilla incompatibilities; do not promise every unrelated weapon or armor enchantment is meaningful on every ride.
- Treasure acquisition remains consistent with the enchantment's intended rules; full station support does not mean making all treasure enchants table-rollable.
- Optional mod enchantments need an adapter/compatibility matrix: can apply, effect works, stacking rule and tested mod version. Unknown IDs survive save/load but must not execute a guessed effect.
- Multiplayer server validates offers, XP/lapis consumption, anvil output, swaps and movement effects. Client previews never grant stats.

## Proposed fun enchantments
Names and level ranges below are design candidates; numeric tuning requires playtests.
| Enchantment | Levels | Fun effect | Limit / counterplay |
|---|---:|---|---|
| Railrunner | I–III | Better momentum retention and a little more control on long grinds and clean transfers | Requires a valid rail and input; never automatic rail teleporting |
| Kickstart | I–III | Stronger first push after a stop or clean landing | Cooldown and meaningful movement required; incompatible with Longstride |
| Longstride | I–III | Better sustained coasting and efficient cruise lines | No free standing acceleration; incompatible with Kickstart |
| Airweave | I–III | More deliberate midair steering for creative gaps | No extra flight or repeated jump reset; keep readable trajectories |
| Perfect Landing | I–III | Well-timed aligned landings retain more flow and reduce stumble | Rewards timing; not blanket fall immunity or an extra Feather Falling stack |
| Wallflow | I–III | Longer controlled wall-rides and cleaner exits | Valid surface and movement required; cannot climb forever or bypass protected barriers |
| Slipstream | I–III | Build a brief drafting charge behind a moving rider, then slingshot out | Breaks with separation; no stationary teammate farming |
| Tidecarver | I–III | Sharper underwater carving and smoother surface-to-dive transitions | Requires water; does not grant breathing, erase currents or freeze the course |
| Ashwake | I–II | Better handling on designated Nether event surfaces and temporary event lava rails | No blanket lava immunity, lava block conversion or protection bypass |
| Beatbound | I–III | Optional beat-timed trick links give a small flow-efficiency reward with synced VFX | Use authoritative event clock; equivalent cues without Spotify; no network-latency penalty |
| Tagflow | I–III | Preserve more combo flow through a legal moving tag | Must complete normal tag interaction; cannot paint from outside valid reach or claims |
| Trick Capacitor | I–III | Distinct clean tricks charge the next burst | Repeated trick decay; spending charge cannot generate itself |
| Counterglide | I–II | A well-timed dodge while riding earns a short handling recovery window | Does not change gun damage, bypass TaCZ reloads or supply ammunition |
| Crew Current | I–II | Clean synchronized crew lines share a small flow pulse | Radius, cooldown and anti-loop rules; no multiplicative squad stacking |

Do not make enchantments mandatory to perform each ride's signature tricks. Ranked event rules should support normalized handling/enchantment presets while free-roam permits earned builds. Accessibility assists are separate from enchantment progression.

## Technical and performance requirements
- Versioned ride specification: stable ride ID, schema version, part IDs, per-part cosmetics, decal asset IDs, name, tuning selection and references to ordinary enchantment data.
- One authoritative source of handling values; compute/cached resolved stats on equipment or registry changes rather than scanning all enchantments/parts per tick.
- Model attachment points and animation bindings per part. Cache shared meshes/materials; do not bake a new mesh every frame. Use bounded texture caches and distance-aware cosmetic detail.
- Synchronize validated changes and compact IDs; do not retransmit whole textures/NBT every movement tick. Late joiners must resolve the same ride.
- Unknown/removed mod part has a visible fallback without discarding its saved data. Migrate old uncustomized rides to sensible defaults without changing their handling.
- Measure startup, resource reload, workshop opening, frame-time lows, server tick cost and bandwidth on identical scenes before/after. Optimization is required in parallel, with no capability loss or unmeasured “zero cost” claims.

## Implementation order and acceptance checklist
- [ ] Audit current six ride items, capability serialization, enchantment application/effect paths and rendering attachment points.
- [ ] Deliver one complete vertical slice: skateboard deck/trucks/wheels/grip + per-part paint/tag + saved name + live preview + persistence.
- [ ] Expand physical slots to every existing ride, including BMX ratios and hoverboard engine/trail.
- [ ] Implement vanilla table/book/anvil/grindstone workflow; test normal and conflicting offers, creative/survival, insufficient XP/lapis and treasure rules.
- [ ] Prototype Railrunner, Trick Capacitor and Tidecarver, then tune remaining candidates from controlled challenge tests.
- [ ] Verify part swaps preserve name, art, enchantments, durability where supported and ownership across death, drops, trading, dimension travel, restart and reconnect.
- [ ] Test packet replay, forged part IDs, illegal enchant levels, double-click crafting, full inventory, item deletion and disconnect during a swap.
- [ ] Verify boots and ride effects compose correctly; test optional mods present/absent, TaCZ combat input, old saves, resource reload and dedicated server.
- [ ] Measure performance with many customized riders; document regressions, fixes and reproducible results.
- [ ] Expand ride classes only after prototype handling and course compatibility prove a distinct gameplay purpose.

See [Ride Research and New Styles](Ride-Research-and-New-Styles) for primary-source comparisons and recommended additions.

