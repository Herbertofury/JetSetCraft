# Approved Ride Expansion and Trick Combat

Status: **user-approved feature goals**, 2026-10-14. Implementation and gameplay verification remain pending.

## Approved equipment
Add **snowboard, wingsuit, skis, ice skates, mountain bike, aquatic carveboard and mountainboard**, alongside the six existing ride styles. Their inclusion is approved; the engineering gates in [Ride Research and New Styles](Ride-Research-and-New-Styles) are quality checks, not a request to reapprove the roster.

Add an **earnable, fully customizable boostpack/backpack**, inspired by the movement and visual identity of Bomb Rush Cyberfunk and HYPERFUNK. Preserve their satisfying acceleration, momentum, boost transitions and trick variety as major quality targets. Study permitted reference gameplay carefully and document measurable feel comparisons rather than calling a generic speed increase equivalent.

The user states they have full permission from Team Reptile to implement these ideas in Minecraft/JetSetCraft. Keep that statement in project provenance. The written grant and its precise asset/code/music scope have not been reviewed here; record any supplied agreement before distributing borrowed proprietary materials. Mechanics, reference analysis and original implementation/design work can proceed.

## Boostpack: a major progression reward
- Earn the initial pack through a memorable crew challenge, then unlock cores, shells, nozzles, fins, harnesses, straps, tanks/cells, exhaust effects and gang signature designs.
- Customize individual part colors/materials, graffiti, stickers, name, lighting, exhaust/trail shape, sound and combo-reactive VFX in the same [Ride Workshop](Ride-Workshop,-Parts-and-Enchantments).
- Fully animated idle, ignition, sustained thrust, air correction, braking and cooldown. The pack should feel physically attached to the character; no floating pieces or clipping through wings.
- Trick chains build boost charge. Distinct tricks, well-timed landings, transfers and creative routes increase efficiency; repeated trivial input does not generate infinite charge.
- Ground burst, controlled air dash and optional advanced maneuver modules share explicit resources and cooldowns. Strong momentum should stay controllable at high speed.
- Make pack tuning meaningful: acceleration, charge efficiency, sustained thrust, recovery and air control. Preserve cosmetic freedom independently of preferred mechanical tuning.
- A dedicated compatible back-equipment/loadout arrangement should coexist with rides, armor and common backpack mods. Define mutually exclusive flight/thrust modules only where physics or slot conflicts require them; do not silently occupy chest armor.
- Wingsuit and boostpack can be visually and mechanically combined through an explicit compatible build. No infinite free flight from repeatedly swapping equipment.
- Save all customization, name, enchantments and charge consistently through reconnect, death, drops, trade and dimension travel. Repair/enchantment behavior must be explicit.

## Tricks replenish ammunition: approved core combat loop
**Skate → dodge → perform tricks → replenish ammo → attack → chain into the next line.** Implement this as a real supported combat-skating feature, including TaCZ integration, with clear HUD feedback and good enemy pressure.

The reference principle comes from [Roll7's developer combat guidance](https://blog.playstation.com/2022/08/11/prepare-for-rollerdrome-with-dev-combat-tips-out-august-16/). JetSetCraft must implement its own tested adapter and economy, rather than assuming TaCZ already exposes the exact same mechanic.

### Reward rules
- Server-authoritative verified tricks award an ammo charge after successful completion/landing or a valid completed grind segment.
- Reward difficulty, variety, near-miss dodges and flowing attack/trick transitions. Use diminishing returns on repeated easy tricks and no rewards for zero-distance idle loops.
- Convert charge through the active gun's registered ammunition mapping. Explicitly support ammo types, magazine/reserve behavior, reload state and weapon switching.
- UI shows which ammo was replenished, how much and why. Clearly signal full reserves or ineligible weapons instead of silently losing rewards.
- Gameplay prototype must compare direct magazine refill versus reserve refill. The chosen mode must keep pressure and flow without breaking reload animations, chamber state or ammo accounting.
- Separate compatibility behavior from balancing: unsupported guns fail visibly and safely, never consume charge or create guessed item IDs.
- Event presets may supply event-only ammo with precise end-of-event cleanup. Free-roam survival also gets a configurable, documented replenishment mode; this feature is not restricted to events.
- Survival economy policy belongs to server settings: generated trick ammo, conversion from carried reserves, or event-only operation. Default tuning is a playtest decision, not a hidden limitation.
- No duplication through gun swapping, reconnect, repeated completion packets, canceled tricks, container transfers or multiple simultaneous adapters.
- Combo finishers and music/lighting can celebrate ammo bursts, with readable combat cues and reduced-flash options.

### Combat variety
Airborne attacks, grind-line shootouts, timed dodge windows, trick-linked resupply, moving capture objectives, escalating survival waves and gang pursuit events should reinforce one another. Gun input, aiming and reload remain usable while riding. Avoid mandatory tricks during every encounter; skilled play should create more opportunities rather than force a single loop.

## New ride identity and customizable parts
| Approved style | Movement and challenge identity | Parts |
|---|---|---|
| Snowboard | Edge carving, buttering, switch riding, grabs and snow-to-rail transfers | Deck/top/base, edges, bindings, stance and graphics |
| Skis | Independent twin-ski poses, slalom, crossed-ski tricks, switch landings | Ski pair, bindings, boots/attachment cosmetics and optional poles |
| Ice skates | Fast edge changes, crossovers, spins, frozen-lake races and dance duels | Boots, blades, holders, laces and blade trails |
| Mountain bike | Rough downhill, climbs, whips, scrubs and suspension landings | Frame, fork/shock, wheels/tires, drivetrain/gears, brakes, bars, saddle and pedals |
| Mountainboard | Dirt carving, powerslides, grabs and rough-ground jumps | Deck, trucks, pneumatic wheels/tires, bearings and bindings |
| Aquatic carveboard | Ocean-surface carving, underwater slaloms, breach jumps and barrel rolls | Hull/deck, fins, pads, optional propulsion, wake and underwater trail |
| Wingsuit | Controlled gliding, proximity routes, dives, banks and trick transitions | Suit panels/wings, fabric palette/art, harness, fins where designed and compatible boostpack attachment |

All receive distinctive sounds, animations, unlocks, customization and applicable enchantments. Water riding remains available to other supported ride styles; the carveboard specializes in it. Biome courses need suitable routes, transfer points and clear boundaries, with protected terrain unchanged after events.

## Feel and trick-variety acceptance
- [ ] Record reference maneuvers and inputs from permitted JSR/BRC/HYPERFUNK material, distinguishing released gameplay from HYPERFUNK's announced features.
- [ ] Measure acceleration, braking, turn response, boost impulse/duration, jump arcs, air control, grind attachment, wall-ride exits and landing recovery.
- [ ] Prototype shared flow transitions, then tune every ride's distinct vocabulary: flips/manuals for decks, soul/royale-style poses for inline, cross-steps for quads, peg/manual/whip vocabulary for bikes, and appropriate snow/water/air moves.
- [ ] Test trick chaining into attacks, tags, dances and crew choreography without losing input responsiveness.
- [ ] Compare mouse/keyboard and controller behavior, first/third-person camera, low/high FPS and multiplayer latency.
- [ ] Verify swept/high-speed collision and chunk readiness without tunneling through obstacles or forcing unsafe chunk loads.
- [ ] Preserve vanilla-compatible movement selection and the ability to dismount/recover safely.

## Delivery gates
- [ ] Complete a playable existing-ride + boostpack + TaCZ trick-ammo vertical slice before multiplying unfinished systems.
- [ ] Add all seven approved equipment styles with their own complete handling, parts and progression.
- [ ] Verify survival, dedicated-server, optional-mod absent/present, save migration and anti-duplication behavior.
- [ ] Test ammo refill against reload interruptions, empty/full magazines, multiple ammo types and weapons, death/reconnect and end-of-event cleanup.
- [ ] Measure startup, resource reload, frame-time lows, server tick and network cost; improve performance in parallel without removing capabilities.
- [ ] Publish actual build/test evidence separately from this requirements page.

Primary movement references: [Bomb Rush Cyberfunk](https://store.steampowered.com/app/1353230/Bomb_Rush_Cyberfunk/), [HYPERFUNK](https://store.steampowered.com/app/3183480/HYPERFUNK/). See the [research comparison](Ride-Research-and-New-Styles) for other sources.

