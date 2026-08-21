# Tricks, Combos and Flow

JetSetCraft's Style Flow system rewards variety, risk, continuity, and clean execution. It deliberately discourages repeatedly pressing the same move.

## Trick contexts

There are eight slots in each of three contexts, for twenty-four network-stable trick IDs:

- **Air:** style-specific aerials such as flips, grabs, barspins, tailwhips, corkscrews, and hoverboard phase moves.
- **Grind:** style-specific slides, souls, pegs, smiths, feebles, darkslides, magnetic rails, and scooter deck tricks.
- **Ground:** Backspin, Windmill, Flare, Headspin, Air Freeze, Handplant, Halo, and 1990 Spin.

The same input can produce different names and handling on different equipment. Direction, current chain, prior move, player tick timing, and ride style feed deterministic server selection while the animation index remains compact for synchronization.

## Variety and repeat penalties

The server records a 24-bit unique-trick mask for the active combo. A first use earns a freshness bonus and grows the variety scale. Repeating the same trick applies a progressive score and multiplier reduction with a floor, so repetition remains legal but no longer optimal.

Unique dance moves have a separate 28-bit history. New dance vocabulary earns a freshness bonus in the same overall style chain.

## Boost tricks

Hold **Left Alt** while pressing **R** with at least a small amount of boost. A boost trick:

- Raises point value and multiplier gain.
- Adds extra Flow.
- Produces stronger particles and a brighter HUD callout.
- Uses additional boost and returns less boost than a normal trick, creating a real risk/reward decision.

Servers can disable boost tricks independently.

## Flow meter and ranks

Flow is a 0–100 expression of current style heat. It rises through tricks, fresh vocabulary, grinds, manuals, landings, dances, and cyphers. It slowly decays when the player is no longer sustaining a line.

Rank combines score, multiplier, and Flow:

`WARM UP → FRESH → HYPE → WILD → LEGEND → ALL CITY`

## Combo continuity

Manuals, grinding, wall rides, dance phrases, and recent successful actions refresh combo grace. When the player remains grounded without a continuity action and grace expires, score/multiplier/variety reset. Flow decays separately, so a finished line still leaves a short-lived sense of heat.

## Landings

Landing grade uses airborne duration and the previous server-tick vertical impact:

- **Perfect Landing:** long enough air and very controlled impact.
- **Clean Landing:** controlled impact or a skilled manual-assisted touchdown.
- **Sketchy Landing:** the line survives, but the touchdown was heavy.

Landings award score, multiplier, Flow, boost, sound, particles, and a HUD callout proportional to grade.
