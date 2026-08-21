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

## Start here

1. Read [[Getting Started|Getting-Started]] for installation and first ride.
2. Learn the complete input language on [[Controls]].
3. Choose equipment on [[Ride Styles|Ride-Styles]].
4. Build long lines with [[Tricks, Combos and Flow|Tricks-Combos-and-Flow]].
5. Turn your world into a course with [[Grinding and Transfers|Grinding-and-Transfers]] and [[World Physics|World-Physics]].
6. Review [[Compatibility]] before assembling a large modpack.

## Core design promises

**Momentum is continuous.** Legitimate speed from boosts, slopes, rails, ice, pistons, explosions, currents, knockback, and modded mechanics is allowed to become tech rather than being flattened back to a vehicle cap.

**The world is the skatepark.** JetSetCraft follows real rail shapes, collision-shape ledges, walls, Create track geometry, and datapack tags. It does not require special purpose-built course blocks.

**Combat remains composable.** Normal item use and third-party combat systems keep their input and upper-body animation authority while JetSetCraft owns movement and lower-body ride presentation. Full-body dance/stunt clips are suppressed when a weapon overlay is active.

**Optional mods stay optional.** Compatibility entries use APIs, Forge behavior, registry lookups, or non-required datapack tags. The Aether, Twilight Forest, Create, TACZ, and other supported mods are never hard dependencies.
