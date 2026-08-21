# Multiplayer and Servers

Movement truth, scoring, dance state, loadouts, and world interactions are server authoritative. Clients send bounded input state; the server runs the solver and synchronizes snapshots to the player and tracking clients.

## Installation

Install the same JetSetCraft JAR and required dependencies on the dedicated server and every connecting client. Optional compatibility mods only need to be present where their normal mod requirements demand them; JetSetCraft itself does not make them required.

## What is synchronized

- Ride style and active state
- Boost, momentum, combo score/multiplier, and Flow
- Grind kind, wall ride, manual, powerslide, and boost state
- Trick ID/timer and boost-trick flag
- Ground stunt and landing callout state
- Dance family, move ID, phrase timer, chain, and cypher size
- Persistent ride gear through player capability NBT

The network protocol is versioned. Mismatched JetSetCraft builds are rejected instead of decoding incompatible state silently.

## Cyphers

Cypher membership is calculated by the server from nearby non-spectator players who are actually dancing. Radius and rewards are bounded and configurable. No client can declare an arbitrary crew size.

## Fake players and automation

Forge GameTests and some automation mods create server players without a Netty channel. JetSetCraft still runs server capability/movement logic for them but treats “no client endpoint” as nothing to synchronize, preventing fake-player crashes.

## Performance

The solver uses local block/shape searches and compact state packets. For large servers:

- Keep grind snap/tolerance within normal ranges.
- Avoid datapacks that mark every full block as an intentional grind target.
- Test dense Create track junctions under real player load.
- Preserve the default bounded cypher radius.
- Diagnose with `/jetsetcraft status` and server profiling before reducing gameplay fidelity.
