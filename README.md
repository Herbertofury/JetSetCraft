![JetSetCraft](art/generated/jetsetcraft-banner-1200x630.png)

# JetSetCraft

[![Verify JetSetCraft](https://github.com/Herbertofury/JetSetCraft/actions/workflows/build.yml/badge.svg)](https://github.com/Herbertofury/JetSetCraft/actions/workflows/build.yml)
[![Minecraft 1.20.1](https://img.shields.io/badge/Minecraft-1.20.1-62B47A)](https://files.minecraftforge.net/net/minecraftforge/forge/index_1.20.1.html)
[![Forge 47.4.23](https://img.shields.io/badge/Forge-47.4.23-E04E39)](https://files.minecraftforge.net/net/minecraftforge/forge/index_1.20.1.html)
[![Release v0.3.0](https://img.shields.io/badge/release-v0.3.0-35D4D8)](https://github.com/Herbertofury/JetSetCraft/releases/tag/v0.3.0)

JetSetCraft turns ordinary Minecraft terrain into a momentum-first street sports game. Ride, boost, trick, grind, wall-ride, dance, tag walls, equip mobs with Street Gear, and start data-driven gang sessions—without replacing Minecraft’s world, combat, mobs, rails, or movement ecosystems.

## What ships in v0.3.0

- Six persistent ride styles: inline skates, quad skates, street board, BMX, hoverboard, and scooter.
- Twenty-four named tricks and twenty-eight dance moves with combo variety, Flow, boost rewards, landing grades, and multiplayer cyphers.
- Exposed block-edge, fence, wall, pane, vanilla/Forge rail, and optional Create 6.0.8 track grinding.
- Minecraft-native momentum tech across ice, slime, honey, Soul Speed, redstone rails, pistons, explosions, knockback, and small terrain transitions, while vanilla swimming retains complete control.
- Street Art/Spirit Vector movement merge: rhythmic skate strides, charged angular powerslides, three wall kicks, wind redirects, sprint kickoffs, ledge vaults, and same-wall reuse protection—with exact neutral stopping preserved.
- A hands-free, server-authoritative ride slot that leaves both hands and the vanilla boots slot available.
- Combat-safe presentation: ride locomotion owns the lower body while item and weapon systems retain arms, hands, held items, and head.
- A 139-entry graffiti gallery plus an in-game 16 × 10 custom-paint canvas, toggleable 16-color Free Paint, all 16 throwable paint balloons, six-face surface splashes, repaint-in-place behavior, support cleanup, durability, and a configurable per-chunk safety limit.
- Leg-baked rollerblade meshes follow the player's actual animated legs instead of floating as duplicated item models.
- Persistent Street Gear for compatible vanilla and modded mobs, using conservative anatomy rigs without replacing the original entity, AI, ownership, UUID, or registry type.
- A physical Boombox and mob-head gang tuner with data-pack mappings, safe event-only actors, cleanup/anti-farm rules, and eighty original gang entrance stingers.
- A compact vanilla-adjacent Boost/Flow HUD with an instant `H` toggle, plus reduced-motion, camera, FOV, and trick-name controls.

The [wiki](https://github.com/Herbertofury/JetSetCraft/wiki) covers mechanics, configuration, compatibility, modpack tags, and verification in depth.

## Liberty Grind — 0.4.0-alpha.9 campaign expansion

**Drop hard. Paint loud. Skate home.** [Liberty Grind](wiki/Liberty-Grind.md) is the selectable Helldivers-inspired mode on skates. Choose a region, a mission, **difficulty 1–10** and a **20, 25 or 30 minute** budget; review both guns, armor and **four call-in slots** before launching your pod. The expansion supplies **28 replayable mission adaptations**, generated from a seed across isolated arenas **1,025–1,537 blocks wide**. Turf painting, skating relays, ordered courier routes, scans, moving escort rigs, physical base demolition, defense and designated boss hunts give different operations different sequences. [See the complete mission mapping](docs/research/LIBERTY_MISSION_RESEARCH.md).

The six-region war map connects Neon Docks, Overpass Uprising and Blackout Borough to three unlockable frontier routes. Terminid swarms, Automaton bases and Illuminate ships use actual Minecraft mobs and damageable weak points. A bounded director replenishes pressure, with **24–96 resident combat actors per mission** according to difficulty. Deployments are **solo**, while liberation, threat and regional defense progress are shared by the server. Optional home-defense offers require an explicit deployment and use temporary owned actors and paint overlays, preserving ordinary terrain and belongings.

The armory discovers the pinned pack's **68 live TaCZ gun definitions** and offers real light, medium and heavy armor kits. The catalogue has **100 selectable call-ins: 94 sourced Helldivers entries and six JetSetCraft originals**, plus **25 shared, mission-specific or historical reference entries** outside those four slots. Nine calls map to native **MCDivers 1.0.3** actors and seventeen support calls map to exact native TaCZ guns when installed; the remaining behaviors are documented JetSetCraft adaptations. **CHROMAGEDDON, Tag Team, Bassquake, Inkstorm Curtain, Fresh Tracks and Graffiti Ghost** make the street-art identity part of combat. [Read the call-in/provider mapping](docs/research/LIBERTY_CALLIN_IMPLEMENTATION.md).

Extract to your walkable ship, spend earned requisitions on call-ins and four ship modules, then deploy again or choose **Return to Minecraft** to recover your original world and belongings. Six retained research projects connect samples and super credits to ordinary graffiti, resupply and the existing single Rival Rush preparation slot. Ship modules connect that research to supply capacity, cooldowns, Eagle charges and sample recovery; completed missions also feed crew supplies, XP, training, reputation and the Style Library. [Read the implemented progression loop and boundaries](docs/LIBERTY_GRIND.md).

The [exact pack guide](docs/LIBERTY_GRIND_PACK.md) covers optional installation and native-provider boundaries. JetSetCraft remains standalone; missing required gun content explains why deployment is unavailable. These are Minecraft mission adaptations: escort/evacuation uses moving objective rigs, and vehicles are one-rider hardware. Full original-game NPC evacuation, every source-game mechanic and shared squads are outside the implemented scope. Human 20–30 minute pacing, combat balance and multiplayer server load still require playtesting.

**Local alpha.9 verification passed:** all **118 required Forge GameTests in each of three configurations**, the clean build and ten offline gates, **nineteen individually reviewed native-client captures**, and a separate installed-JAR startup/save/shutdown check. The [campaign receipt and native gallery](docs/verification/liberty-campaign/README.md) identify the exact source/JAR, disclose the two absence lanes' intentional provider skips, preserve failed iterations and separate native gameplay, client pixels and installed-package evidence. The [archived alpha.8 receipt and fifteen-image gallery](docs/verification/liberty-grind/README.md) remain attached to the earlier expedition. Client and server must both use **alpha.9 / protocol 17**; human pacing and balance remain playtesting work.

## Optional Rival Rush mode — development alpha

**Alpha.7 adds optional arena contracts and earned Pit stops.** Ride a checkpoint relay, paint fresh patches or hunt dangerous rivals while clearing a room. Every actual clear earns one service; completing its contract earns a second. Choose **Tune, Patch, Rewind or Remix**, with exact previews and accepted results. The next route carries the build you shaped at that stop.

**Headliners, build evolutions and draft controls continue from alpha.6.** Fixed lane, circle and ring attacks give movement a purpose; three-phase headliners guard the third and sixth circuit arenas and appear every five active minutes in Free streets. Earn an evolution opportunity, build toward **Cryo shatter**, **Railstorm** or **Radiant barrier**, and use three rerolls and two banishes to steer the run. Build Paths shows actual requirements, while Last run records the final build and accepted return reward.

The permanent **Style Library** continues to learn sixteen preparations from the same crew XP earned through Tag, King of the Hill, Showdown, arena clears, qualifying Rush returns and claimed missions. Choose one learned artifact to begin future runs at rank one, even before founding a home. Sixteen temporary artifact families, colored reward caches and a scout view support different arena routes and builds.

Rival Rush remains an opt-in skating, paint-combat and mob-card mode with separate Rush XP, thirty-minute/Endless Free streets and six-arena circuits. Ride against named gangs, draft temporary upgrades, paint walls and visit music stops. Returning restores the saved normal inventory, physical ride and vanilla XP. Ordinary Minecraft and the other JetSetCraft activities continue independently.

Craft a reusable **Rival Rush Flyer** from paper + cyan dye, or use `/jetsetcraft rush`. The alpha supports Easy, Normal and Hard and never changes the world's difficulty. Controls, card rewards and limits are documented in [Rival Rush](wiki/Rival-Rush.md); the [18-game research/design study](docs/RIVAL_RUSH_DESIGN.md) separates the implemented headliners and three build paths from proposed companions, broader equipment collections and rhythm-scored challenges.

The preceding **`0.4.0-alpha.7`** gameplay checkpoint merged through [PR #46](https://github.com/Herbertofury/JetSetCraft/pull/46) as [`af8e7f1b7a41b0e491f04d3f3f8ca4288ac5eed7`](https://github.com/Herbertofury/JetSetCraft/commit/af8e7f1b7a41b0e491f04d3f3f8ca4288ac5eed7), with network protocol **15**. That original checkpoint has tested and merged source tree `e33c221bb02df4c949c28766007893215d7ca2ed`. [Base verification](https://github.com/Herbertofury/JetSetCraft/actions/runs/41397720888) and the [provider matrix](https://github.com/Herbertofury/JetSetCraft/actions/runs/41397720946) passed **63 required Forge GameTests in each of nine configurations — 567 executions of 63 distinct tests**, all builds and normal dedicated-server save/shutdown. Base also passed its **23-capture native-client gate**. Liberty Grind followed in **alpha.8 / protocol 16**; the current campaign expansion is **alpha.9 / protocol 17** and requires matching clients and servers.

**Verification follow-up:** [PR #47](https://github.com/Herbertofury/JetSetCraft/pull/47) merged as [`1fcb78852f893f083749aac2e566addc3acca78b`](https://github.com/Herbertofury/JetSetCraft/commit/1fcb78852f893f083749aac2e566addc3acca78b) after correcting the retained midpoint fixture to resolve actual pickup-earned XP drafts before asserting settlement. Production gameplay is unchanged. The [addendum](docs/verification/contracts/README.md#post-merge-verification-follow-up) preserves the failed push and records the separate corrected checks; the 23-image gallery remains evidence for the original PR #46 source.

Alpha.7 builds on [alpha.6 / PR #45](https://github.com/Herbertofury/JetSetCraft/pull/45), [alpha.5 / PR #44](https://github.com/Herbertofury/JetSetCraft/pull/44), [alpha.4 / PR #43](https://github.com/Herbertofury/JetSetCraft/pull/43), the shared activity/arena layer from [PR #41](https://github.com/Herbertofury/JetSetCraft/pull/41), Rival Rush from [PR #39](https://github.com/Herbertofury/JetSetCraft/pull/39), and crew houses from [PR #40](https://github.com/Herbertofury/JetSetCraft/pull/40). Stable **`v0.3.0` remains unchanged** and does not contain this mode.

The [original PR #46 receipt and 23-screen gallery](docs/verification/contracts/README.md) identify the exact source and independent local reproduction, whose images were visually reviewed. The new native sequence completes three accepted Paint patches, defeats actual owned rivals, clears the room through its normal gate and confirms Tune from rank one to two. Logged arrival, hit and active-clock fixtures shorten setup; this is not a human circuit playtest. ForgeGradle runs the source set in native Forge, with the distributable JAR built separately. Local images and JAR hashes do not establish downloaded CI pixels or identical CI binaries. Provider lanes check the pinned packs and admitted integration paths, not every provider mob, boss phase, native spell or foreign capability.

**Verified alpha.5 predecessor:** [base gate #290](https://github.com/Herbertofury/JetSetCraft/actions/runs/41388639847) and the [provider matrix](https://github.com/Herbertofury/JetSetCraft/actions/runs/41388639856) passed **all 44 real Forge GameTests in each of nine configurations — 396 executions**. Every configuration passed build, dedicated-server readiness, RCON stop and the normal saved-dimensions shutdown check. The base passed its **fifteen-capture native-client gate**. The [Style Library receipt and gallery](docs/verification/style-library/README.md) records the exact source/merge identity and an independent local reproduction of those fifteen screens. CI archive metadata is API-reported; the browser could not download the archive, so the local gallery is not presented as downloaded CI evidence or proof of identical binaries.

The [historical alpha.4 receipt](docs/verification/reward-journeys/README.md) retains its separate 35-test/nine-configuration checkpoint and thirteen reviewed captures.

The [historical alpha.3 receipt](docs/verification/arena-circuit/README.md) preserves its 28-test/nine-configuration checkpoint and ten client captures, with a dated correction: its provider dedicated-server smoke proved startup, while normal dedicated-server save/shutdown was established by the base workflow. [Alpha.2](docs/verification/crew-house/README.md) and [alpha.1](docs/verification/rival-rush/README.md) retain their separate evidence. These are machine integration checks; **human thirty-minute pacing, Endless balance, house progression balance and broad modded-AI playtesting remain outstanding**. [Read the verification scope](wiki/Testing-and-Verification.md).


### Connected activities and arena circuits

Use a tuned Boombox with an empty hand, or `/jetsetcraft activity`, to choose **Random, Tag, King of the Hill, Showdown, Rival Rush or Liberty Grind**. A random invitation shows its selected game before you accept; it considers Liberty Grind only when the optional loadout and current deployment requirements are available. Completed objectives contribute to the same crew supplies, house XP, member training and gang reputation; earnings are retained even before your first house is founded.

Rival Rush offers **Free streets** and a **six-arena circuit**. Pick one of three arena rewards, skate along the generated connection, complete its timed combat objective, and take the next route. Choices favor rooms you have not visited in the current set. The catalogue has six base profiles and four optional profiles for Aether, Iron's Spells, Cataclysm and AoA, enabled only when their providers are installed. Arena sizes, layouts, authored block maps and bounded rewards are editable through data packs, and a run keeps its chosen catalogue through reloads. See [Rival Rush](wiki/Rival-Rush.md) for the exact format.

The [three-arena example pack](examples/arena-data-pack/README.md) is ready to copy into a world's `datapacks` folder and edit: **Crossfade Yard** emphasizes critical hits and reputation, **Collector's Loop** emphasizes magnet range and supplies, and **Chorus Terrace** emphasizes ability area and training. Each room has a complete editable definition and separate run/shared rewards.

**Build** shows eleven upgradeable stats and three artifact slots. The sixteen four-rank artifact families include movement-charged tags, landing pulses, exposed targets, chaining, fireworks, control, rail pulses, tempo and recovery. Alpha.5 adds **Venom ink**, **Chill paint**, **Split chorus**, **Radiant groove**, **Beat ward** and **Soul burst**; their damage and recovery operate within Rush's virtual combat system. Each real arena clear earns one **Pit stop service**, or two if its optional contract was completed. Tune raises an owned rank; Patch restores missing vitality/ward; Rewind replenishes bounded draft controls; Remix retains the replaced artifact family's exact rank. Each service can be used once per stop, and unused choices close when you depart. Fifteen artifact families remain available without Iron's Spells. Its optional native Firebolt relay remains a scoped secondary hit; ordinary spellbooks, mana, summons and foreign capability snapshots are outside that adapter.

**Build paths** adds three precise combinations over those existing families. Both ingredients need rank three; a real headliner defeat earns the nonstacking opportunity to forge one evolution at an existing decision stop. **Cryo shatter** combines Venom ink and Chill paint, **Railstorm** combines Line battery and Rail shockwave, and **Radiant barrier** turns unused Radiant groove healing into Beat ward. The three-slot limit remains. These are original Rush mechanics inspired by the reference games, not an import of the complete Minecraft Dungeons I/II item catalogues.

**Build → Scout reward caches** lists the six physically placed cache destinations with directions, distance, reward effects and recharge state. Matching colored particles and floor markers distinguish magnet, damage, critical chance/power, ability area, recovery and artifact caches. New rooms require three defeats earned in that room to charge a claim; walls, another player's cache and repeated same-tick requests cannot yield rewards.

**Crew Charter → Progress → Style Library** offers sixteen permanent lessons using existing crew XP, from Line battery at 55 XP to the optional Firebolt relay at 2,000 XP. Learning and selection cost no currency. The selected available style replaces the single house preparation for that run; it does not add another starting rank or a fourth artifact slot. Leaving the selection at **House preparation** preserves the physically validated home-facility choice. An unavailable optional provider leaves its saved style dormant and falls back to the house. Shared progression remains separate from Minecraft XP and ordinary loot.

**Try the connected loop:** finish a Tag objective to earn the 55 crew XP for Line battery, select it in the Style Library, then choose Rival Rush at the Boombox, review the invitation and select **Start arena journey**. Follow the reward you want at each fork, scout the caches once you arrive, complete an optional room contract for a second Pit stop choice, then improve or reshape your build before riding onward. Returning banks the qualifying shared rewards, so another activity, a house upgrade or the next run can build on that session.

| Journey — actual first fork | Build — actual initial state |
|---|---|
| ![Actual Journey map with the first offered routes; no room has been cleared](docs/verification/reward-journeys/11-journey-map.png) | ![Actual initial Build view with baseline statistics and empty artifact slots](docs/verification/reward-journeys/12-run-build.png) |

These alpha.4 images show early client states. Completed-room settlement, artifact behavior and remix are exercised by GameTests; the captures do not depict a finished circuit or a developed three-artifact build.

| Alpha.5 Style Library | Alpha.5 reward-cache Scout |
|---|---|
| ![Native Style Library with three readable lessons and the actual fresh locked state](docs/verification/style-library/15-style-library.png) | ![Native cache Scout with real directions, distances and recharge state](docs/verification/style-library/14-cache-scout.png) |

These are native screenshots from the exact-source local reproduction. The library honestly shows **0 crew XP and 0/16 learned**, and the cache Scout shows **three defeats still needed**. The audit's logged arrival teleport reaches an actually generated arena; it does not demonstrate a naturally traversed connector or a completed room.

## Build a crew house — development alpha

Craft a **Crew Charter** from book + cyan dye + copper ingot, place a Boombox, then use the Charter on it to found a house. Build the rooms yourself. The house starts with two named apprentice profiles and grows from a **17 × 17** recognized footprint with **3 roster slots** to **49 × 49** with **12 slots**. Expansions do not construct or protect terrain; they enlarge the area that recognizes your facilities.

Placed crafting/smithing tables, targets, jukeboxes and brewing stands become **workshop, practice, studio and recovery** facilities. Add their supporting blocks, register upgrades and select one optional starting Rush rank. **Courier, Tagger, Technician and DJ** members gain XP from timed dispatch jobs or selected idle-member training earned through completed activities and Rush. Jobs earn separate **Crew Supplies**, crew XP and gang reputation; stronger home-gang reputation reduces recruitment costs. Ordinary Minecraft XP and inventory remain separate. Assignments progress while the server runs and ready rewards wait for an explicit claim.

One Tag, King of the Hill and Crew Showdown win before founding earns **34 Supplies** and **77 training XP**. Founding adds 24 Supplies and transfers that training to Riff, the initial training focus: **58 Supplies and a level-2 Riff**. At current prices, the first plot expansion costs 40 and workshop registration costs 8, leaving 10. You still place the valid workshop blocks yourself, and the expansion needs an eligible footprint. Rival Rush is optional throughout that early progression path.

The **Progress** tab retains sixteen actual reward receipts, including cap reductions, training recipients and level changes. It works before founding and transfers that history with the bank. Activity training uses the selected idle member; a mission trains its assigned member. Nonblocking notifications confirm earned progress, and the next home goal distinguishes having enough Supplies from having the required placed blocks.

![Actual Crew Progress view with empty reward history and a practical next home goal](docs/verification/reward-journeys/13-crew-progress.png)

The captured audit account has no reward-history entries yet and shows **16/40 Supplies** toward the next **25 × 25** house footprint. Persistent receipts, mission claims and level transitions are verified separately by server tests.

This first slice contains **persistent roster profiles, not visible residents or follower AI**. Full interaction steps, physical upgrade requirements and current limits are in [Player Chapters and Crew Houses](wiki/Player-Chapters-Posse-and-Mob-Archetypes.md). The [eight-game construction/member research](docs/CREW_HOUSE_DESIGN.md) develops the next layer of inhabited rooms, active missions and memorable crew activities.

The optional [soundtrack system](docs/MUSIC_INTEGRATION.md) supports local resource packs for the home and Rush, track credits, fades and `/jetsetmusic on|off|next|status`. The two supplied recording links are catalogued accurately; their audio masters are not yet bundled. The local importer creates streaming Ogg packs and source/encoded hash receipts from project-supplied files.

## In game

![Leg-baked inline skates, painted wall, and compact Boost/Flow HUD](art/screenshots/v0.3.0-ride-hud.png)

![Paged graffiti selector in persistent Free Paint mode](art/screenshots/v0.3.0-graffiti-selector.png)

![Custom 16 by 10 graffiti painter](art/screenshots/v0.3.0-custom-graffiti.png)

## Install

JetSetCraft targets Minecraft 1.20.1 and Java 17.

1. Install Forge 47.4.10 or newer for Minecraft 1.20.1; the release is built and verified with Forge 47.4.23.
2. Put the JetSetCraft JAR in the instance’s `mods` folder.
3. Install PlayerAnimator `1.0.2-rc1+1.20` on every client. It is not required on a dedicated server.
4. Launch with Java 17. Multiplayer servers and clients must use the same JetSetCraft version.

Create 6.0.8, TACZ 1.1.8-hotfix, Epic Fight, Better Combat, The Aether, Twilight Forest, and other Forge rail/block ecosystems are optional. Missing optional mods do not create registry or data-pack failures.

Official dependencies and compatibility targets: [Forge](https://files.minecraftforge.net/net/minecraftforge/forge/index_1.20.1.html), [PlayerAnimator](https://www.curseforge.com/minecraft/mc-mods/playeranimator/files/4587214), [TACZ](https://www.curseforge.com/minecraft/mc-mods/timeless-and-classics-zero/files/8141310), and [Create](https://www.curseforge.com/minecraft/mc-mods/create/files/7178761).

## Controls

All keys are ordinary Forge mappings and can be rebound.

| Input | Action |
| --- | --- |
| Use ride gear | Equip it to the hands-free ride slot |
| `K` / `Shift + K` | Toggle ride / return equipped gear |
| `H` | Show or hide the compact style HUD immediately |
| `Left Alt` | Boost |
| `R` | Contextual air, grind, or ground trick |
| `G` | Grind or wall-ride |
| `C` | Manual; Hip-Hop dance selector |
| `V` | Brake/powerslide; Breaking dance selector |
| `Space` while grinding | Hop or transfer |
| `Space` while wall-riding | Kick away; up to three wall kicks before touching ground |
| `Sprint + Space` with movement | Kickoff from the ground |
| `Space` at a clear ledge | Collision-checked ledge vault |
| `B` / `Shift + B` | Dance or change move / stop dancing |
| `W/S/A/D + B` | Select Toprock, House, Locking, or Popping |
| Use spray can in air | Open the paged graffiti selector |
| Shift + use spray can on a block | Open the selector without painting |
| Use spray can on a wall | Paint the selected decal |
| Tag / Free Paint selector toggle | Switch between full tags and quick six-face color splashes |
| Use a paint balloon | Throw a wide surface-aware color splash |
| Use ride gear on a mob | Equip persistent Street Gear |
| Sneak + empty-hand use on geared mob | Recover its gear |

## Build and verify

The repository includes the Gradle 8.8 wrapper. Use Java 17:

```text
python -m pip install -r tools/requirements.txt
python tools/generate_models.py
python tools/generate_animations.py
python tools/generate_brand.py
python tools/validate_assets.py
python tools/validate_gameplay_contract.py
python tools/validate_java_syntax.py
python tools/validate_premium_polish.py
python tools/validate_wiki.py
./gradlew clean build
./gradlew runGameTestServer
```

On Windows, use `gradlew.bat`. The JAR is written to `build/libs/`. CI regenerates deterministic assets, rejects dirty generator output, performs a clean build, requires every declared Forge GameTest, including activities, arena circuits, shared settlement and optional-provider boundaries, starts a real dedicated server, and publishes the JAR, checksums, and logs as one workflow artifact.

In a development world, `/jetsetcraft status` reports authoritative movement and style state. Operators can use `/jetsetcraft build_vanilla_lab` to create a compact acceptance course and `/jetsetcraft set_momentum <speed>` as a controlled test aid. Maintainers can run `gradlew.bat -Djetsetcraft.visualAudit=true runClient` for an unattended real-client inline-skate/HUD/painted-wall/Free-Paint-selector/editor capture.

## Architecture and compatibility

Clients send bounded input samples; the server owns movement, scoring, equipment, gang, and persistence state. Tracking clients receive sanitized snapshots. Optional APIs are isolated and optional data-pack entries use `required: false`; normal mob and Gang Form compatibility does not replace provider classes or use invasive injections. Three exact containment mixins are validated separately: the optional pinned TaCZ bullet guard and two Minecraft 1.20.1 contact guards for accepted home-defense actors. Ordinary actors retain their native callbacks, and home admission requires the actual installed guard markers. The [mode specification](docs/LIBERTY_GRIND.md) and [provider implementation map](docs/research/LIBERTY_CALLIN_IMPLEMENTATION.md#world-and-ownership-boundaries) document these boundaries. Event actors never force-load chunks, and fake/test players without a network channel remain safe.

See [Architecture](docs/ARCHITECTURE.md), [Upstream Merge](docs/UPSTREAM-MERGE.md), [Compatibility Covenant](docs/STANDALONE_COMPATIBILITY_COVENANT.md), [Asset Provenance](docs/ASSET_PROVENANCE.md), and [Changelog](CHANGELOG.md).

## Inspiration and research

Explore the **[visual Inspiration Atlas](https://herbertofury.github.io/JetSetCraft/research.html)** or the **[wiki research hub](https://github.com/Herbertofury/JetSetCraft/wiki/Inspiration-and-Research)**. The November 1, 2026 expansion adds **136 references**, bringing the collection to **505 entries**: **76 games, 160 music candidates, 40 art/culture references, 38 engineering references, 18 research papers**, and the preserved **173-entry earlier atlas**.

| Explore | What is inside |
|---|---|
| [Games, movement and rides](https://github.com/Herbertofury/JetSetCraft/wiki/Research-Street-Movement-and-Rides) | Official images/videos, specific design lessons, ten refinements and six course briefs. |
| [Music, graffiti and audio](https://github.com/Herbertofury/JetSetCraft/wiki/Research-Graffiti-Music-and-Audio) | A 16-track starting sequence, seven shelves, cover art and crew-specific audition candidates. |
| [Art, animation and creators](https://github.com/Herbertofury/JetSetCraft/wiki/Research-Animation-Models-and-Creators) | Street culture, readable visual direction and current choreography research. |
| [Engineering and performance](https://github.com/Herbertofury/JetSetCraft/wiki/Research-Frameworks-Java-and-Bedrock) | Code-aware improvement ideas, primary documentation and meaningful acceptance checks. |

Start with **[the latest additions](https://herbertofury.github.io/JetSetCraft/research.html?batch=2026-11-01)** or **[the new 12-track listening route](https://herbertofury.github.io/JetSetCraft/research.html?path=city-after-hours)**. The browser supports search, dated research passes, image/direct-video filters, two ordered listening paths, a local shortlist and CSV export. The wiki adds eight briefs for creator tools, replays and social play alongside the original six course studies. Research proposals and music candidates retain their evidence and audition status. The [source catalogue, validation and build instructions](docs/research/README.md) keep future updates reproducible.

## Rights

Project code and original JetSetCraft content are All Rights Reserved unless a file or ledger entry says otherwise. Third-party/reference inputs retain their own permissions and are not relicensed. See [LICENSE](LICENSE), [THIRD_PARTY_NOTICES.md](THIRD_PARTY_NOTICES.md), and the asset ledger before redistributing or adding external material.
