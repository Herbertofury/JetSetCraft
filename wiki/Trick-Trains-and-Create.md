[← Ride Styles](Ride-Styles) · [Roadmap](Roadmap) · [Home](Home)

# 🚆 Trick Trains & Create

**Owner-approved major development goal · October 14, 2026.** JetSetCraft should be an outstanding **Create addon**, bringing expressive train riding into its music, tricks, gang rivalry and customization loop. This is a planned expansion, not a claim of implemented compatibility.

## The fantasy
Build your own train, give it a crew identity, activate **Trick Train mode**, and turn a rail journey into a moving skate park and rival-crew showdown. A humble vanilla minecart must offer a fun entry point; a full player-built Create train should deliver the spectacular flagship experience. Other train mods belong behind tested integration adapters.

[Denshattack!](https://store.steampowered.com/app/2524850/Denshattack/) is the owner-selected movement, train-trick and presentation reference. Study its expressive flips, grinds, pace and readable spectacle. The systems below are JetSetCraft design goals, not claims about Denshattack features.

## Core gameplay loop
1. Build, acquire or assemble a minecart/train, then customize its identity and choose crew roles.
2. Opt into Trick Train mode for a challenge or compatible free-roam route.
3. Race rival crews, chain train tricks, hit rhythm/route objectives, tag designated surfaces and earn reputation.
4. Face gang-specific trains and escalating boss convoys, with music-reactive entrances and counterplay.
5. Unlock liveries, parts, crew recruits, maneuvers and challenges; return safely to ordinary rail transport with the build intact.

## Movement that feels incredible
- Train drifts, controlled slides, jumps, flips, spins, rail grinds, track transfers, near-miss lines and satisfying landings where the tested adapter supports the maneuver.
- Aerial movement must have readable anticipation, controllable trajectories, expressive animation, strong audio feedback and recoverable mistakes.
- Whole-train choreography should account for carriage count and shape. Long custom trains need deliberate handling and spectacle rather than clipping, detached cargo or invisible collisions.
- Link skating on carriage roofs, grinding along the train, boarding, wingsuit transfers and returning to the driver's seat into the existing combo system.
- Make switching between normal transport and Trick Train mode explicit, reversible and multiplayer-authoritative. Ordinary logistics and schedules must keep working when the mode is off.

## Customization workshop
Support live previews and saved loadouts for train name, per-carriage paint, liveries/skins, graffiti, gang emblems, lighting, trails, smoke/exhaust, horn/music presentation and compatible component cosmetics. Respect the player's actual Create block-built design and supported bogies rather than replacing every train with one generic model.

Separate cosmetic identity from handling upgrades. Unlock signature gang trains and skin variants through skill/reputation, with original memorable designs and properly authorized reference assets. Preserve names, inventories, passengers, ownership, item/block data and component provenance through customization and mode changes.

## Rival crews, mob trains and modes
- **Crew Express:** parallel-track races with train trick chains, route choices and synchronized crew performances.
- **Tag the Line:** compete to tag event-owned train panels and designated route targets. Never repaint another player's build without permission.
- **Convoy Clash:** rival-train battles, roof-skating, boarding objectives and TaCZ-compatible combat where enabled; clear roles for driver, trick rider and defender.
- **Rail King / Flag Freight:** moving king-of-the-hill and capture-the-flag objectives adapted to carriages and stations.
- **Runaway Rhythm:** survival/chase courses with escalating obstacles, collectible lines, rescue/cargo objectives and recovery points.
- **Mob train bosses:** biome- and gang-specific convoys with distinct music, dances, abilities and unlocks. Example proposals: skeleton sharpshooter train, creeper hazard convoy and Nether blaze express, with event-safe effects.
- Music-genre crews get recognizable train skins, choreography and entrances. Existing gang control, challenger recruitment and rare Honkistas escalation should connect to train events without resetting earned rewards.

## Create-first compatibility
**Flagship:** full Create train support, with exact Minecraft/loader/Create versions documented and tested. Evaluate compatible Steam 'n' Rails features and other train mods individually; never label an untested integration supported.

Implementation design must respect railway graphs, stations, schedules, signals, switches, carriage/bogie state, seats, collision, disassembly and save/reload. Do not detach a real logistics train from its network and hope it reconstructs correctly.

Where conventional train physics cannot express a trick safely, investigate an opt-in event representation linked to the original train identity and reversible state transaction. This is an engineering option to validate, not an excuse to show a cosmetic trick while scoring fictitious movement or to lose the actual custom build. Prevent duplicated cargo, trains or rewards on reconnect, crash or cancellation.

**Vanilla minecarts:** independently playable without Create installed, sharing scoring, progression and cosmetic concepts where appropriate.

**Other train mods:** versioned capability adapters for identity, movement, geometry, customization, passengers and persistence. Missing optional mods must not break startup. Explain adapter limitations clearly while pursuing full integration.

## Course and world safety
Reuse the biome/terrain-aware event system: natural-looking rail/obstacle palettes, alternate lines, checkpoints and room for the full consist. Nether/End events can use the always-on music-reactive rave lighting; overworld night events get the same spectacle.

Generated courses must avoid protected builds and existing rail infrastructure, respect claims, and clean up only event-owned additions. Preserve player edits and pre-existing tracks. Use arenas/instances when a safe temporary route is impossible. Recover riders and trains on timeout, disconnect and server restart.

## Installed-game research handoff
The owner authorizes a future Codex/Claude task to inspect their installed Denshattack game for direct comparison and potentially reusable code/assets. First locate the exact installation and build; no installation path has been supplied here.

Measure acceleration/braking, drift response, steering, air control, jump arcs, trick timing, combo windows, camera response, landing feedback and transition behavior. Record reproducible clips/measurements and connect each finding to a JetSetCraft control or acceptance test. Review skins and train silhouettes as visual references.

Before distributing copied code, skins, music or other assets, verify the relevant license or rights-holder permission and record provenance/scope. Owner access to an installed copy does not by itself establish redistribution rights. Do not bypass DRM or authentication. Use original implementation/assets when direct reuse is not authorized. No new agent or installed-game inspection was started by this documentation task.

## Implementation checklist and release gates
- [ ] Establish a fun minecart vertical slice with explicit Trick Train mode, scoring and reversible state.
- [ ] Prove Create train identity/state preservation and normal logistics regression tests before spectacular movement integration.
- [ ] Implement and validate full-train tricks, collision, recovery and multiplayer synchronization.
- [ ] Add the customization workshop, gang liveries, progression and train-versus-train encounters.
- [ ] Add compatible crew boarding, ride/wingsuit transitions, TaCZ and music-reactive effects.
- [ ] Validate selected Steam 'n' Rails and other train adapters against exact supported versions.
- [ ] Test cargo/passenger preservation, coupling/consist changes, disassembly, portals/dimensions, chunk unload, disconnect, cancellation and restart recovery; no duplication or world damage.
- [ ] Profile startup, tick time, frame-time tails, packet traffic and large custom trains. Reuse geometry/state caches and update only changed data; no repeated whole-world scans or quality/content reductions to claim speed.
- [ ] Run human playtests for feel, readable obstacles, meaningful trick variety, rewarding progression and rival personality. A technically functional train that is dull to ride fails the goal.

## Tools & references
- [Denshattack official Steam page](https://store.steampowered.com/app/2524850/Denshattack/) — movement/presentation inspiration.
- [Create source](https://github.com/Creators-of-Create/Create) and [releases](https://github.com/Creators-of-Create/Create/releases) — authoritative integration/version research.
- [Create Train implementation example](https://github.com/Creators-of-Create/Create/blob/mc1.21.1/dev/src/main/java/com/simibubi/create/content/trains/entity/Train.java) — architecture reference from a different branch; not a drop-in Forge 1.20.1 API contract.
- [Steam 'n' Rails source](https://github.com/Layers-of-Railways/Railway) — compatibility candidate; verify exact supported versions and licenses before reuse.

**Status:** approved goals and research direction. Runtime implementation, compatibility and performance remain to be demonstrated.
