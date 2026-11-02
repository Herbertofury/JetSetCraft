# Liberty Grind

**Drop hard. Paint loud. Skate home.** Liberty Grind is JetSetCraft's selectable expedition mode: choose a district on the war map, ride a pod into a large rival-held skate arena, use the real TaCZ Helldivers loadout to clear objectives, gather research and super credits, then board an extraction ship back to the original world.

This document describes current source behavior. It is separate from historical alpha.7 verification. See the [player guide](../wiki/Liberty-Grind.md) for controls and the [pack integration guide](LIBERTY_GRIND_PACK.md) for exact upstream files, installation, API boundaries and test commands.

## A complete connected session

Liberty Grind appears alongside Tag, King of the Hill, Crew Showdown and Rival Rush in the activity catalogue. Accepting its invitation opens the war map; deploying is a separate deliberate action. `/jetsetcraft liberty` also opens the map. Each player receives a separate generated district in `jetsetcraft:liberty_grind`, and the normal world remains the return destination.

The three operations use radii of **160, 224 and 288 blocks**, producing playable districts approximately **320, 448 and 576 blocks across**. Their time budgets are **25, 30 and 35 minutes**. Roads, rails, cover and mural stations appear as the player explores. A rendered pod carries the player down; extraction has a rendered ship, an arrival stage, boarding and departure. These are original JetSetCraft transports.

The objective set makes skating and painting necessary:

- **Paint the town free:** defeat the marked rival crew, then paint three distinct marked mural panels. Signal Lacquer research reduces that requirement to two.
- **Pirate radio:** clear defenders and paint the station, then accumulate ten seconds of actual skating within its broadcast area.
- **Grindline courier:** clear defenders, tag the dispatch point, then ride through three lit beacons in order.

Standing still, repeating the same panel, out-of-order beacons, teleports and another player's kills do not supply their respective progress. Actual successful spray-can placement advances the mural step. TaCZ retains aiming, fire mode, ammunition, reload and native bullet damage; JetSetCraft does not replace gunfire with paint damage.

The **Liberty field radio**, a loan compass in hotbar slot five, opens the mission panel on right-click. It exposes resupply, extraction and early return; `/jetsetcraft liberty` also opens the panel. A movable, scalable HUD shows current objectives, currency, distance and extraction state.

After all objectives, call extraction within twelve blocks of the pad and remain within forty-eight blocks for thirty seconds. Boarding becomes available after the arriving ship lands and requires being within eight blocks. Right-click your landed ship or use **Board ship** in the panel. A missed ship can be called again while time remains. The native rival director adds bounded patrol pressure between objective fights every twelve seconds, increasing to every three seconds around extraction; existing actors limit further spawns.

## Rewards that flow into other modes

Super credits come from play. Each operation has three credit caches, three common-sample caches, two rare-sample caches and one super-sample cache. Completed objectives also award credits, common and rare samples, and one restored resupply charge. Credits and completed-objective crew progress are retained on a controlled early return or loss; **samples require successful ship extraction**.

The six research projects have effects in the implemented systems:

| Project | Effect | Credits | Common | Rare | Super |
| --- | --- | ---: | ---: | ---: | ---: |
| Pressure Saver | Every fourth successful graffiti placement spends no spray durability, including ordinary-world painting | 20 | 8 | 0 | 0 |
| Magnetic Pigment | Makes the existing Magnetic Mixtape available as the selected Rush preparation: +1.5-block pickup reach | 20 | 6 | 0 | 0 |
| Blast Stencil | Makes Bigger Chorus available as the selected Rush preparation: +12% paint/artifact area | 35 | 12 | 4 | 0 |
| Prism Ink | Makes Finishing Touch available as the selected Rush preparation: +25% base damage on critical hits | 50 | 16 | 6 | 1 |
| Signal Lacquer | Reduces the Liberty mural objective from three marked panels to two | 30 | 10 | 2 | 0 |
| Supply Skates | Adds one starting Liberty resupply charge, taking three to four | 40 | 16 | 4 | 2 |

The three Rush preparations occupy the **existing single starting-rank slot**. Selecting one replaces the Style Library/home choice for that run; selecting a crew style later clears the research override. There is no extra artifact slot or stacked starting rank. Pressure Saver, Signal Lacquer and Supply Skates apply automatically once learned. Research is purchased and selected after returning, not during an active activity.

Crew settlement uses the same permanent supplies, crew XP, member-training and gang-reputation account as street activities, Rush and dispatch jobs. Three completed objectives plus extraction award up to **20 Supplies, 125 crew XP, 55 member-training XP and 14 reputation**, subject to existing account caps and server settings. Those rewards also advance the Style Library and are retained before founding a house. The Progress receipt identifies the source as Liberty Grind.

A practical loop is to earn crew lessons through street games, deploy to recover pigments, research a new Rush preparation, use it through a branching arena circuit, and bring the resulting crew progress back to home development. Each mode contributes through its actual reward path.

## Equipment and survival

The mission issues a temporary copy of the player's equipped ride, or inline skates when no ride is equipped, plus armor, food, a spray can, the field radio, a Liberator, a Peacemaker and six reserve magazines for each gun. The [pack guide](LIBERTY_GRIND_PACK.md) documents why these ammo items represent whole magazine reloads. Resupply tops reserves up to six, restores eight health points and refills boost; it has a thirty-second cooldown. Deployments start with three charges, or four with Supply Skates. Each completed objective restores one charge up to a cap of four. TaCZ's normal reload input is still required.

The mode uses native mob health and real combat. The player's ordinary inventory, physical ride, XP and return dimension/position are captured before temporary equipment or teleportation. On return, the saved Minecraft state is restored and the mode settles its separate earned rewards. Loan items cannot become normal-world loot. Disconnect and death end the deployment through the return path; saved journals support recovery if the process is interrupted.

## Architecture and ownership

`LibertyOperation` owns phase, objective and collection rules. `LibertyWorld` builds bounded district geometry near explored chunks. `LibertyManager` owns admission, escrow, transport, director, extraction and settlement. `LibertyEvents` scopes damage, drops, interaction restrictions and lifecycle recovery to the expedition dimension and its participants. `LibertyResearch` owns a versioned player research ledger and exact mission settlement intent. `LibertyTaczCompat` owns only the optional weapon API bridge.

Real bullets retain the TaCZ `tacz:bullet` type and shooter attribution. The manager records the actual admitted projectile instance against its mission, rejects foreign-session damage and cleans up in-flight entities when the session ends. Rival types remain their original vanilla classes. Session entities, loan gear, inventory snapshots and progression balances are distinct ownership domains.

The no-TaCZ build stays a complete standalone JetSetCraft installation. Missing or mismatched Helldivers content explains why deployment is unavailable; it does not fabricate replacement guns. There are no bundled upstream gun assets, automatic mod downloads during gameplay, registry overrides or required TaCZ dependency. Server settings can disable Liberty Grind, and admission requires graffiti plus a non-Peaceful world.

## Implemented scope and next expansions

Current source implements three replayable operation profiles, individual isolated deployments, native guns, three objective types, exploration caches, pod and ship transport, research, shared progression and return/recovery. Several players can run separate operations on the same server. The war map selects those operations; it does not yet represent a shared galactic campaign.

Cooperative squads inside one mission, a shared liberation front, additional stratagems, broader Helldivers weapon selection, rival-faction campaign events and authored dimension-mod theaters are future expansion work. Initial district layouts and reward numbers need human playtesting for route variety, skating feel, difficulty and long-session balance.

## Verification scope

The implementation adds focused operation, research, optional-loadout, real reload and native combat tests. The native combat fixture calls the provider's shooting API, obtains its real bullet, advances its real collision tick and observes the resulting Forge damage and mission credit. Its pod and rival director advance through ordinary Forge ticks. Fake-player positioning, a shortened draw delay and a one-health target control combat setup; separate lifecycle fixtures shorten selected operation timers. These fixtures do not establish client aiming feel or human difficulty.

Run the commands in [the pack guide](LIBERTY_GRIND_PACK.md) to exercise no-TaCZ, TaCZ-without-pack and full-provider conditions. Record successful runtime results, exact build source and client captures with the release evidence when available. Historical test counts and screenshots do not automatically apply to this mode.
