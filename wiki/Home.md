# JetSetCraft

JetSetCraft turns ordinary Minecraft worlds into a street-sports playground. Inline skates, quad skates, skateboards, BMX bikes, hoverboards, and scooters all use one momentum-first movement system built around speed preservation, tricks, grinding, transfers, wall rides, powerslides, manuals, breakdance, graffiti, combat composition, and emergent Minecraft physics.

The goal is not to place the player inside a disconnected vehicle minigame. The player remains a Minecraft player: they can explore dimensions, fight mobs, use weapons and spell systems, ride through villages and modded structures, grind real world geometry, and turn blocks such as powered rails, ice, slime, honey, soul sand, fluids, pistons, and explosions into movement tech.

## Style Flow release

The `0.2.0-alpha.1` Style Flow release adds:

- Six first-class ride styles plus the no-gear dance system.
- Twenty-four named air, grind, and ground tricks with style-specific vocabulary.
- Sixty-eight validated animation clips, including eight ride-trick variants, eight grind-trick variants, 28 dance phrases, and eight full-body ground stunts.
- Twenty-eight named dance moves across Toprock, Popping, House, Breaking, Hip-Hop, and Locking, each with its own addressable full-body phrase.
- Automatic dance chaining, multiplayer cyphers, repeat penalties, trick-variety rewards, Flow ranks, boost tricks, and graded landings.
- A dedicated high-detail hoverboard model instead of a skateboard reuse, plus a high-detail street scooter.
- Optional, non-required integrations for Create, The Aether, Twilight Forest, TACZ, and broad Forge rail/block ecosystems.
- A polished HUD for boost, Flow, rank, combo, trick names, landing grades, dance moves, and cypher size.
- A reduced-motion option that disables camera roll/FOV pulses and rapid equipment stunt rotations.

## Gang Wars, Boombox & Mob Atlas expansion

The next major living-world pillar is defined in [[Gang Wars, Boombox & Mob Atlas|Gang-Wars-Boombox-and-Mob-Atlas]]. It establishes the Boombox as the universal street-jam initializer, **persistent equipment-bound gangification** that augments rather than replaces vanilla/modded mobs, dynamic reputation and membership with every gang, renameable crew aliases backed by stable IDs, rare cinematic encounters, competitive gang AI, Turf Wars, graffiti contests, trick battles, Tag, races, dance battles, spray combat, themed rewards, a complete vanilla Gang Atlas, a Junior/Baby Gang Atlas, and a runtime Atlas for mobs from installed mods.

The core interoperability promise is strict: **Minecraft and other mods keep ownership of their entities; JetSetCraft adds an optional street-culture layer on top.** A normal mob becomes a gang rider only when actual JetSetCraft Street Gear is equipped. Ending an event does not revert it. It remains gangified across normal idle time, chunk reloads, and save/restart until that gear is genuinely removed, stolen, broken, or unequipped.

For the hard compatibility rules—including Fox/Allay/native pickup, dropped-gear walk-over, direct equip, dispenser/redstone equip, and anatomy-aware skating for quadrupeds, spiders, slimes, babies, and modded animals—read the [[Standalone Compatibility Covenant|Standalone-Compatibility-Covenant]]. That covenant supersedes any older temporary/event-only wording.

## Start here

1. Read [[Getting Started|Getting-Started]] for installation and first ride.
2. Learn the complete input language on [[Controls]].
3. Choose equipment on [[Ride Styles|Ride-Styles]].
4. Build long lines with [[Tricks, Combos and Flow|Tricks-Combos-and-Flow]].
5. Turn your world into a course with [[Grinding and Transfers|Grinding-and-Transfers]] and [[World Physics|World-Physics]].
6. Explore the future rival-crew ecosystem in [[Gang Wars, Boombox & Mob Atlas|Gang-Wars-Boombox-and-Mob-Atlas]].
7. Read the [[Standalone Compatibility Covenant|Standalone-Compatibility-Covenant]], then review [[Compatibility]] before assembling a large modpack.

## Core design promises

**Momentum is continuous.** Legitimate speed from boosts, slopes, rails, ice, pistons, explosions, currents, knockback, and modded mechanics is allowed to become tech rather than being flattened back to a vehicle cap.

**The world is the skatepark.** JetSetCraft follows real rail shapes, collision-shape ledges, walls, Create track geometry, and datapack tags. It does not require special purpose-built course blocks.

**Combat remains composable.** Normal item use and third-party combat systems keep their input and upper-body animation authority while JetSetCraft owns movement and lower-body ride presentation. Full-body dance/stunt clips are suppressed when a weapon overlay is active.

**Optional mods stay optional.** Compatibility entries use APIs, Forge behavior, registry lookups, or non-required datapack tags. The Aether, Twilight Forest, Create, TACZ, and other supported mods are never hard dependencies.

**Mobs keep their owners.** JetSetCraft never needs to replace vanilla or modded entity types to make them gang members. JetSetCraft owns only its persistent Street Gear/gang layer. The actual source mob remains the original entity, and gear removal cleanly restores source-owned non-gang behavior.
