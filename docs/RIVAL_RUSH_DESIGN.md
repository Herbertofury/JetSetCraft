# Rival Rush: implementation status and roguelite design research

**Status: optional `0.4.0-alpha.4`, merged and machine-verified through [PR #43](https://github.com/Herbertofury/JetSetCraft/pull/43).** This continuation adds movement-earned artifact power, a temporary rival vulnerability window, an optional between-room artifact remix, a journey map and visible shared reward receipts. [Base #286](https://github.com/Herbertofury/JetSetCraft/actions/runs/41379581968) and [provider matrix #59](https://github.com/Herbertofury/JetSetCraft/actions/runs/41379581891) passed **35 GameTests in each of nine configurations**, build, dedicated-server readiness, RCON stop and normal saved-dimensions shutdown. The base produced **thirteen reviewed 1280 × 720 client captures**. The [reward/journey receipt](verification/reward-journeys/README.md) records the identical tested/merged tree, artifacts and limits.

**Historical baseline:** alpha.3 merged through [PR #41](https://github.com/Herbertofury/JetSetCraft/pull/41). Its [verification receipt](verification/arena-circuit/README.md) records nine passing runtime configurations, 28 GameTests in each, and ten reviewed base-client captures. Its dated correction distinguishes provider dedicated-server startup from base normal save/shutdown evidence. Stable `v0.3.0` is unchanged. Rival Rush first arrived through [PR #39](https://github.com/Herbertofury/JetSetCraft/pull/39); alpha.2 added the optional crew-house connection. The [historical alpha.2 receipt](verification/crew-house/README.md) preserves its nineteen-test/eight-capture checkpoint, and the [alpha.1 receipt](verification/rival-rush/README.md) preserves the original fifteen-test checkpoint. Full human thirty-minute sessions, Endless/house progression balance, sustained performance and broad mob-provider behavior remain to be evaluated.

**Research:** the eighteen-game study was checked 1 November 2026; the focused Dungeons I/II movement, vulnerability and build-refinement sources were rechecked **2 November 2026**. Sources are primary developer, publisher and official store material.

Rival Rush is an optional JetSetCraft activity: a separate skating, paint-combat, and mob-card session. Free streets has a thirty-minute ending; the arena circuit ends after six room clears. Both offer voluntary continuation. Ordinary Minecraft and the rest of JetSetCraft remain the main game. This document records both the implemented alpha and the larger design direction; those are different scopes.

Source observations are separated from JetSetCraft recommendations. All locally chosen timings, thresholds, and rewards are **unvalidated tuning values**, except that approximately thirty minutes is the user's requested session target. No reference game was installed or played for this research, and linked videos were not treated as personally watched playtests.

## 0. What this change implements, and what remains a proposal

### Alpha.4: make the three-slot build reward riding

Alpha.4 adds **Line battery**, **Air break** and **Breakbeat mark** to the existing seven artifact families. The current catalogue therefore has **21 ranked choices with Iron's relay available, or 20 without it**: eleven stat choices and ten or nine artifact families. Each artifact has four ranks, and a run still holds at most **three different families**. Paint pressure, Quick hands and Second wind have eight ranks; the other eight stat choices have six. Fresh wheels, Victory lap and Overdrive remain repeatable fallback rewards.

| Implemented mechanic | Exact run rule | What the player gains |
|---|---|---|
| **Line battery** | Charge by **24 actual horizontal blocks** travelled while riding, including continuous travel along arena connectors. Reject observations moving more than four horizontal blocks or four vertical blocks; native passenger mounts, fall flight and swimming do not qualify. One full charge empowers the next validated **primary tag**, from paint or direct contact, by `1.35 + 0.15 × rank`: **1.50× at rank one, 1.95× at rank four**. | A useful line earns a finite stronger tag. Misses, splash, chains, landing pulses and native Firebolts do not spend the charge. |
| **Air break** | Observe at least **six airborne ticks**, **1.5 horizontal blocks** and **0.6 blocks of vertical travel**, then a real ground transition during combat. Its cooldown is `100 ticks × artifact interval multiplier`; radius is `(3 + 0.5 × rank) × area multiplier`; damage is `3 + 2 × rank`. Affect at most **12 visible session-owned rivals**; surviving targets receive `10 + 5 × rank` active ticks of directed stagger, **15–30 ticks**. | A completed moving jump or drop creates a nearby damage pulse and briefly delays directed rival attacks, while native mob health remains untouched. Holding the trick button or standing still does not manufacture a landing. |
| **Breakbeat mark** | If a visible owned target exists within **20 blocks**, choose a priority captain/bruiser at an interval of `140 ticks × artifact interval multiplier`. Mark duration is `40 + 10 × rank` active ticks. Its virtual-damage multiplier is `1.20 + 0.05 × rank`: **1.25×–1.40×**. | A readable short opportunity to focus a dangerous rival. The vulnerability belongs to the session actor, with no native potion or foreign attribute mutation. |
| **Artifact remix** | Each genuinely cleared arena grants **one nonstacking optional replacement**. Use it only between rooms, including after room six. Replace one owned family with a different available unowned family, carrying its **exact rank**. A rejected or stale-version request spends nothing; removing a family clears its associated charge. | Adapt an established three-slot build without discarding the ranks earned earlier or opening a fourth slot. Keeping the current build remains valid. |

All durations above use the existing active simulation clock. Movement observations reset after pauses and rejected discontinuities. The four-block observation limit rejects large position jumps; it is not a universal detector for every short teleport from arbitrary external systems. Real connector riding can charge Line battery, while Air break's airborne state is cleared outside combat. Secondary damage cannot recursively trigger paint chains or fireworks. Run identity, actor ownership and room validation remain authoritative; these additions do not import native equipment, apply vanilla damage, or create a new cross-mod casting interface.

The journey map shows **visited rooms, the current destination and up to three actual offers** for the current six-room set. Its S/start, numbered completed rooms and A/B/C offers represent known server state, not invented future branches. Room previews label possible shared rewards as **up to**; cleared-room details use the actual accepted settlement and contribute to the current set's banked totals. Unrecorded or unavailable settlement is shown explicitly rather than counted as earned. A new clear opens its debrief after any earned XP draft. Choosing a destination still means riding its real connector. Encore begins a fresh six-room map with the same run build. The Build page displays current statistics and artifact descriptions; remix requires an explicit source, replacement and confirmation showing the exact retained rank.

A separate **sixteen-entry persistent reward history** records actual account deltas from street activities, arena clears, qualifying Rush returns and claimed missions. It follows pre-house earnings into the founded house and is independent of the existing 128-entry replay guard. A nonblocking native toast names the earned contribution and any crew/member level change; reduced-motion mode uses a text notification. **Crew Charter → Progress** shows recent receipts, zero-training reasons and the next practical home goal, including the supply gap and physical placement requirements. Reading that goal does not buy an upgrade or build a house. Persistence still follows normal world saves.

**Alpha.4 acceptance is recorded for feature `b120db1b0a75870f5b9275f81e5e072f836a3893`, tested at integration `b0506b052e2e8d7b8f762bd5da3e6daea878a27f` and merged as [`e826694c3429394b2b264f153cd9ea34b1fb9051`](https://github.com/Herbertofury/JetSetCraft/commit/e826694c3429394b2b264f153cd9ea34b1fb9051), all sharing tree `1aab5454652554bffee78299a8f86a2c19013261`.** The 35-test suite covers actual credited receipts, capped/absent training, assigned-member mission level changes, honest route history, qualified movement, one-time charge consumption, exact-rank remix and stale-request rejection. The mark test retains the rank-one mark through **five paused manager calls**, observes it at active tick 49, and verifies expiry at **50 active ticks**, removal of its visible label and damage multiplier, and unchanged native health/effects. The [receipt](verification/reward-journeys/README.md) identifies compressed fixture clocks and supplied test movement. These checks establish mechanics, not a human skill or enjoyment result.

The client screenshots show the actual first Journey map, an initial Build with **zero artifacts**, and Progress with **empty reward history** and **16/40 Supplies** toward a **25 × 25** home goal. The album is empty when photographed; the later return result reports **score 95, one rival and one collected card**. No completed room, six-room client finish, pictured remix or claimed mission is inferred. Historical receipts below retain their original source identities and scope.

### Historical alpha.3 implementation inventory

The new activity hub implements Tag, King of the Hill, Showdown and a reviewed random invitation alongside Rival Rush. Completed Tag/KOTH/Showdown objectives, arena clears, qualifying Rush returns and claimed house missions feed one account. Founding adds pending supplies to the house's **24 starting Supplies**, transfers crew XP and reputation, and gives pending member training to **Riff, the initially focused starter member**. Pre-house reputation can already unlock a recruitment discount. After founding, activity training uses one selected idle member; mission claims train their assigned member. The [shared reward table](../wiki/Rival-Rush.md#shared-completion-rewards) records the exact amounts and eligibility rules.

Arena clears and the later qualifying Rush return are separate earned results. Their server-owned receipts settle supplies, crew XP, training and reputation together. The account retains its most recent 128 receipts, transfers those identities when a house is founded, and saves through ordinary world SavedData. It is a bounded replay ledger, not an unlimited transaction archive or a guarantee against progress loss since the last world save. Missing actors, cancelled street rounds and unloaded legacy Boombox challenges do not settle rewards.

A circuit is six arena clears. Each next room is selected from up to three server-held profiles with different reward families; the player skates along a nine-block-wide connector to the selected room. Six vanilla profiles and one conditional Aether profile cover plaza, crossing, loop and terrace layouts. Profiles specify bounded size, timed objective, genuine-defeat target, static block map and reward emphasis. Choosing upgrades/routes pauses that run; moving outside the active room pauses its objective. Encore starts another six-room set with the same build. The separate Free streets mode retains its thirty-minute boundary.

Alpha.3 had **18 ranked choices: eleven stat upgrades, six common artifact families and one provider-gated artifact**, plus three repeatable fallback rewards. Seventeen ranked choices remained when the optional Firebolt relay provider/API was absent. At most three artifact families could be held. Real paint chains, firework bursts, knockback/stagger, movement-triggered shockwaves, defeat-triggered firing boosts and charged recovery were implemented. Critical chance/power and area scale actual virtual combat. A cache can grant a run rank and invalidates any now-stale XP offer; old requests cannot fill a fourth artifact slot.

Reward caches require three additional owned defeats per claim. Arena completion requires both its active duration and genuine defeat target, then grants the advertised run reward, gathers owned cards and settles a unique shared reward receipt. This is a gameplay implementation, with tuning still subject to human playtesting. It does not import the complete Dungeons I/II item catalogues or their original assets.

The optional Iron's adapter creates the real `irons_spellbooks:firebolt` entity with its actual collision/damage path. Only server-created relays are admitted. It does not cast a player's spellbook or modify provider mana/cooldowns/capabilities. Provider tests distinguish loading, actual native collision, dimension return and curated mob behavior.


### Historical alpha.2 implementation baseline

The table below preserves the alpha.2 baseline. The alpha.3 inventory above supersedes entries that previously described circuits or artifact families as proposals. Its seven-choice upgrade count belongs to alpha.2; alpha.3 had eighteen ranked choices and alpha.4 has twenty-one when the optional relay is available. The recorded tests cover their stated checkpoint only; a feature appearing here is not evidence that every lifecycle or optional-mod combination has passed.

| Area | Recorded alpha.2 surface | Further design at that checkpoint |
|---|---|---|
| **Entry and return** | An opt-in mode menu and command path; a dedicated `jetsetcraft:rival_rush` dimension; temporary loaned ride/paint equipment; a persisted pre-entry return journal and restoration paths. | A polished in-world event host, authored entry sequence, and verified adapters for arbitrary foreign player capabilities. |
| **Run progression** | Separate run XP, score, vitality, seven ranked stat/reward upgrades, and three repeatable fallback rewards. Server-owned offers carry a run identity and offer version. | The eight interacting build families in section 6; family-aware offer steering, rerolls, evolutions, and run-specific equipment combinations. |
| **Session shape** | Thirty minutes of active server simulation time; offer and continuation decision pauses; a completion prompt with Return or Endless; direct Endless entry is also supported. | The authored minute-by-minute narrative in section 3 and a mandatory named final headliner. The current thirty-minute finish is a timer/decision boundary, not a completed bespoke boss finale. |
| **Combat and crews** | Run-owned source mobs, existing gang identities and physical street gear, skater/tagger/bruiser/captain roles, automatic paint fire plus existing aimed paint interactions, and a delayed positional tagger attack tell. | Unique authored behavior for every crew or species, aquatic districts, multipart boss adapters, and validated behavior across all installed provider mobs. |
| **Mob cards** | Defeated actors create mode-only card pickups. Collection records the registered entity ID **and gang ID as a pair**, grants run XP/score, and advances persistent album progress. Actual mastery milestones are **1 / 5 / 15 / 40 / 100 / 250 / 600**. | Illustrated card frames, foils, species-wide aggregated views, behavior notes, themed cosmetic prizes, and challenge unlocks. |
| **Persistent album benefit** | At **20 / 60 / 150 total collected cards**, subsequent runs receive **one / two / three** queued starting upgrade drafts. Album progress remains outside the temporary return journal. | A broad hub economy or permanent stat tree. No vanilla XP, item loot, or vanilla damage reward is intended. |
| **Movement and music** | Rewards for fresh wall patches, validated moving grind/wall-ride/trick states, and a four-second dance at a nearby generated Boombox. | Tag relays, carried crates, crew murals, beat-judged challenges, and the complete POI catalogue in section 8. |
| **World and Endless** | Generated local wall/rail/ramp/Boombox patterns; five-minute chapter labels with composition changes and a six-chapter rotation; bounded enemy movement speed and counters. | A rich procedural neighborhood graph, distinct authored route forks, boss duets, the full modifier catalogue, and measured long-duration performance guarantees. |
| **Optional crew-house bridge — alpha.2** | A player-built house can prepare one selected mode-only starting rank. Qualifying Rush results bank bounded crew supplies, XP, gang reputation and idle-profile training; dispatch jobs provide a non-Rush progression route. See the [Crew House design](CREW_HOUSE_DESIGN.md) and [runtime guide](../wiki/Player-Chapters-Posse-and-Mob-Archetypes.md). | Physical companions, playable expeditions, deep bonds, inhabited-room activities and music-timed house games. Current members are persistent profiles. |

### Current alpha.4 upgrade catalogue

The eleven stat choices are **Paint pressure** (damage), **Quick hands** (firing interval), **Wide spray** (splash radius), **Magnetic mixtape** (pickup reach), **Second wind** (maximum mode vitality), **Easy breathing** (recovery), **Style dividends** (mode style rewards), **Critical rhythm** (critical chance), **Finishing touch** (critical damage), **Bigger chorus** (paint/artifact area) and **Fast rewind** (artifact intervals).

The ten ranked artifact families are **Arc paint**, **Firework cap**, **Bass horn**, **Rail shockwave**, **Tempo drive**, **Soul mixtape**, optional **Firebolt relay**, **Line battery**, **Air break** and **Breakbeat mark**. Three family slots limit one run's build; further ranks strengthen an existing family. The relay enters the offer pool only when its optional Iron's provider/API is available. After applicable ranks or slots are exhausted, **Fresh wheels**, **Victory lap** and **Overdrive** provide healing, score or a temporary damage surge. Alpha.4's recorded suite adds the movement/mark/remix checks to the earlier artifact and provider scope. Broader interaction designs in section 6 remain proposals where explicitly described.

The chapter rotation currently changes tagger/bruiser frequency, lowers pressure and increases music-stop benefits in the cypher chapter, and shortens tagger cadence in the crossfire chapter. Its open-street and relay phases use the baseline mixture. These are concrete encounter changes, with further variety still to author and test.

### Compatibility boundary

This alpha refuses entry on Peaceful before saving a return checkpoint or installing loan equipment. Native hostile mobs are removed by Minecraft's Peaceful despawn path even when persistent. The mode does not change the world's difficulty; Easy, Normal and Hard are the supported entry settings for this slice. A future per-mode solution must preserve the ordinary world's setting and be verified across real mob lifecycles.

Selection is based on registered mob identities and existing JetSetCraft eligibility. This is a broad selection path, not a claim that every mob works well in a flat district. The existing `street_gear_incompatible` entity tag explicitly excludes the Ender Dragon and Wither, along with player/armor-stand entities. Respect those pre-existing exclusions: a multipart dragon encounter and a scripted, destructive boss lifecycle require deliberate encounter adapters, and this slice provides neither. Other provider ownership rules, aquatic needs, unusual geometry, native AI, and scripted transformations still require representative integration tests. Do not infer universal compatibility from a successful registry lookup.

### Historical verified alpha.3 checkpoint

[PR #41](https://github.com/Herbertofury/JetSetCraft/pull/41) merged as [`e57e9ab`](https://github.com/Herbertofury/JetSetCraft/commit/e57e9ab9732a84d90e6ac35a82439e54cd4461ab). The feature commit [`c733852`](https://github.com/Herbertofury/JetSetCraft/commit/c733852b260ef5dbf2cb54e7d41fc98ac10b8363) was tested at PR integration [`2626a8d`](https://github.com/Herbertofury/JetSetCraft/commit/2626a8d63c7c065a6ca5c241610993a28bdb58ca); the tested and merged tree is **`e5e127156d0335a9c6f2e33ba153e8ecfc75e757`**. The [arena-circuit receipt](verification/arena-circuit/README.md) preserves that identity, the binary checksum, workflow artifacts, provider markers and original captures.

| Evidence | Recorded result | Scope |
|---|---|---|
| [Verify JetSetCraft #280](https://github.com/Herbertofury/JetSetCraft/actions/runs/41356358215) | **PASS** | Clean Forge build, **28 real GameTests**, dedicated-server readiness and normal shutdown, plus ten reviewed **1280 × 720** base-client captures. |
| [Optional-provider matrix #56](https://github.com/Herbertofury/JetSetCraft/actions/runs/41356358302) | **PASS in all eight configurations** | Alex's Mobs, Alex's Caves, Cataclysm, their combined lane, Aether, Iron's, AoA and the Aether/Iron's/AoA/Cataclysm combined lane each passed the same **28 GameTests** and dedicated-server startup. Their SIGTERM/SIGKILL cleanup did not assert graceful save/shutdown. With base, this is nine tested configurations. |

The alpha.3 tests exercise actual owner-only street objectives, pending progress transfer, duplicate settlement rejection, bounded arena data, circuit/Encore rules, owned-rival combat and arena reward settlement. Provider markers establish native Aether and Precasia departure/return in their loaded configurations and actual admitted Iron's Firebolt collision in the Iron-containing lanes. These are specific integration proofs, not universal mob, boss, spell or cross-mod guarantees, and do not establish alpha.4's new behavior.

The real client used packet entry/exit for Free streets and the circuit, verified saved Minecraft inventory/XP and dimension/position restoration, founded a house with the Charter, registered a placed workshop, dispatched a job and accepted an explicit Boombox Rival Rush selection. Its arena capture shows **the first three room offers**; the audit then selected a route and entered connected travel. Actual arena combat and clear conditions are covered by server GameTests with shortened test clocks. The short Free streets result was **one defeat and zero collected cards**, the album was empty, and the dispatched job was still running. Crew captures 05–07 preserve native hover overlays. There is no pictured completed mission claim, populated album or six-room client finish.

**No human thirty-minute or Endless playtest is claimed.** Machine integration checks and screenshot review leave pacing, long-session economy, sustained performance and human enjoyment open for playtesting. The research and future acceptance cases below retain their stated scope.

### Historical alpha.2 verification

The following alpha.2 evidence covers candidate [`d5ecadd`](https://github.com/Herbertofury/JetSetCraft/commit/d5ecadd0651c35fdffb505fa2790bda718e830db) on tested PR integration [`b9bcec8`](https://github.com/Herbertofury/JetSetCraft/commit/b9bcec8258e164aa41af5838a89df8aac04bd7c7). The [crew-house verification receipt](verification/crew-house/README.md) records the exact source, binary and eight captures. The [historical alpha.1 receipt](verification/rival-rush/README.md) remains separate; neither checkpoint automatically verifies later code edits.

| Evidence | Recorded result | Scope |
|---|---|---|
| [Verify JetSetCraft #274](https://github.com/Herbertofury/JetSetCraft/actions/runs/41350812751) | **PASS** | Clean Forge build, all **19 real Forge GameTests**, dedicated-server readiness followed by a normal RCON stop, and an actual **1280 × 720 graphical Minecraft client audit with eight captures**. |
| [Optional-provider matrix #51](https://github.com/Herbertofury/JetSetCraft/actions/runs/41350812645) | **PASS in all four configurations** | Combined providers, Alex's Mobs, Alex's Caves, and Cataclysm each passed the same **19 GameTests** and dedicated-server smoke. This is configuration integration coverage, not an audit of every provider mob's AI. |

The client audit used actual packet entry and exit and captured setup, the district HUD, an empty album and the return result. Its fixture added **7 diamonds, 5 bread, 23 XP levels and 17 XP points**. It compared the complete pre-entry inventory data and XP level/total on return, checked XP progress within `0.000001`, required the original dimension, and accepted a return position within one block of the saved position. It then used the Charter, founded a home, displayed two crew profiles, registered an operational workshop and dispatched a still-running timed assignment, capturing the four crew tabs. The empty-album frame does not demonstrate a populated collection.

The nineteen GameTests comprise nine original tests, six Rival Rush tests and four crew-house/integration tests. The latter cover actual construction requirements, frozen mission rewards and claim boundaries, saved roster/job/receipt identity, and the production house-to-Rush-to-return reward path. [Testing and Verification](../wiki/Testing-and-Verification.md) lists the exact markers and fixture limits. A separate deterministic harness previously compiled the pure-Java `RushRun` class and checked timer, choice, overflow and Endless rules.

**Evidence limits:** no human thirty-minute or Endless playtest is claimed. The recorded smoke checks do not establish long-session pacing, animation quality across every mob, all multiplayer interruption combinations, universal optional-mod compatibility, or measured long-duration performance. The broader acceptance cases later in this document remain a verification plan unless matched to a specific recorded test. The source-study observations likewise remain research, not hands-on playtest evidence.

**Reading guide:** section 2 records reference-game evidence. Sections 1 and 3–13 describe the target experience and proposed next steps unless a subsection explicitly identifies implemented behavior. Section 5 gives the actual first-slice card thresholds and distinguishes them from future presentation rewards.

## 1. The experience to build

A player deliberately starts a rival-crew session, skates through a changing route, defeats or tags event-owned adversaries, gathers a separate run resource, makes increasingly consequential build choices, and collects cards for the mobs encountered. Music, graffiti, tricks, and route decisions all contribute. A thirty-minute run has a satisfying conclusion and an explicit invitation to continue in Endless. Leaving returns the player to normal Minecraft quickly and reliably.

This is one JetSetCraft activity. It should not become a compulsory progression gate for skating, painting, social play, or ordinary Minecraft survival.

### Four design promises

1. **Movement changes the fight.** Turning, grinding, jumping, lining up a tag shot, and choosing a route should produce different outcomes. The strongest strategy should involve riding well.
2. **Every short session leaves a memory.** A discovered mob card, a new build combination, a named captain, an unusual musical encounter, or a personal score supplies a story beyond a kill counter.
3. **The player can stop.** A visible exit remains available throughout the run. The normal ending offers closure; Endless is an active choice with further stopping points.
4. **Minecraft remains the player's home.** Ordinary mobs, blocks, inventory, vanilla XP, other players, and other mods do not acquire new obligations because someone starts this mode.

## 2. What the game research actually supports

The ordering below is editorial: it prioritizes usefulness to this particular JetSetCraft mode, not a universal quality ranking. Official store pages usually include screenshots and trailers for further visual study.

| Reference | Verified design evidence | Proposed JetSetCraft use | Trap to avoid |
|---|---|---|---|
| **[Vampire Survivors](https://store.steampowered.com/app/1794680/Vampire_Survivors/)** | Poncle presents a minimal-input survival game built around snowballing choices, run rewards, and upgrades between attempts. | Let riding occupy the player's attention while readable passive effects supplement aimed tagging. Make each early upgrade noticeable. | Removing all active decisions once the build becomes powerful; treating a wall of particles as sufficient feedback. |
| **[Megabonk](https://store.steampowered.com/app/3405340/Megabonk/Megabonk)** | Its official page describes randomly generated maps, enemy/boss hordes, XP pickups, and randomized upgrade offers with different rarities. Released September 18, 2025. | Build a legible three-dimensional pickup and combat loop around JetSetCraft's established mobility. | Copying numerical escalation without addressing Minecraft terrain, visibility, and ride control. |
| **[Risk of Rain 2](https://store.steampowered.com/app/632360/Risk_of_Rain_2/)** | The official description explicitly offers a final-boss escape or continued indefinite play and combines accumulating items with escalating opposition. | A real finale, followed by a voluntary continuation; combinations that change how a run behaves. | Making the only satisfying conclusion an eventual unavoidable loss. |
| **[Hand of Fate: Hordes](https://store.steampowered.com/app/3075410/Hordes_of_Fate__A_Hand_of_Fate_Adventure/)** | This is the current name of the user's Hordes of Fate reference. Early Access began July 22, 2026. Its official description connects cards, encounters, boons, and end-of-run token rewards. | Treat a discovered mob card as both an encounter memory and a clear progression record; add short encounter choices along the route. | Presenting the whole game as a deckbuilder or assuming every card mechanic is a collectible enemy system. |
| **[Death Must Die](https://store.steampowered.com/app/2334730/Death_Must_Die/)** | The current game describes god-granted powers, itemization, and interacting builds. Its developer update acknowledges that enlarged blessing pools weakened synergy; planned Act 4 changes, including infinite play, were delayed toward 2027. | Group offers around compatible mechanics and let route rewards help steer a build. | Claiming the announced overhaul is shipped; assuming more upgrade entries automatically produce more useful choices. |
| **[Night Swarm](https://store.steampowered.com/app/3668370/Night_Swarm/)** | Released December 4, 2025. Its official description includes companions, a castle progression hub, totems, and repeated runs. Developer updates discuss stale endgame currencies and specialized item sets. | Give the mob album useful long-term milestones and recognizable build families; keep a social place for reviewing progress. | Endless duplicate currency with nothing desirable to do with it, or mandatory grind before the mode feels expressive. |
| **[Roboquest](https://www.starbreeze.com/our-work/roboquest/)** | A movement-focused FPS roguelite. Its Endless update adds compact stages, mutators, and adapted combined-boss encounters. | Develop Endless as a sequence of changing situations, with movement and active aim still important. | Replaying an unchanged route order with only higher enemy health. |
| **[Deep Rock Galactic: Survivor](https://store.steampowered.com/app/2321470/Deep_Rock_Galactic__Survivor/)** | Its official description combines automatic shooting, procedural caves, mining, mission objectives, and extraction. It left Early Access on September 17, 2025. | Graffiti targets, route objectives, and musical POIs can compete with immediate combat attention and lead toward a finish. | A mode where every activity collapses into circling the same flat area. |
| **[Brotato](https://store.steampowered.com/app/1942280/Brotato/)** | The official page specifies short waves, shopping between them, default automatic fire with manual aiming, sub-thirty-minute runs, and enemy health/damage/speed accessibility settings. | Alternate pressure with brief decision windows; expose the precise consequence of an upgrade. | Mandatory long menus every few seconds or hidden difficulty tuning. |
| **[20 Minutes Till Dawn](https://store.steampowered.com/app/1966900/20_Minutes_Till_Dawn/)** | The official page combines active aiming, XP-driven upgrades, evolution paths, summons, boss rewards, and short-session pacing. | Support a deliberate tagging/shooting style alongside passive build effects. | Making aimed combat inferior in every situation to automatic area damage. |
| **[Hades II](https://www.supergiantgames.com/games/hades-ii/)** | Version 1.0 launched September 25, 2025. The developer's release coverage emphasizes character events, story resolution, and further unlocks alongside repeat runs. | Named rivals should acknowledge encounters; a new card or captain defeat can reveal a short piece of crew personality. | Adding a large mandatory narrative or dialogue queue to every quick session. |
| **[Gunfire Reborn](https://gfr.yijoys.com/en/)** | Its official site describes distinct heroes, randomized weapons, inscriptions, scrolls, and varying levels. | Define upgrade families by what they make the player do, and give them visible interactions. | A long list of statistically different upgrades that all feel identical to use. |
| **[Dead Cells](https://deadcells.com/)** | The developer describes pattern-based enemies and bosses, branching progression, and hidden paths. | Reward learning attack tells and selecting a route; a skilled player should be able to avoid a known threat. | Enemy acceleration that makes previously readable attacks cease to be avoidable. |
| **[Tiny Rogues](https://store.steampowered.com/app/2088570/tiny_rogues?l=english)** | Its Early Access page presents themed floors, room rewards, traits, equipment, and choices of the next reward. | Offer clear route forks such as recovery, an upgrade, a mob-card hunt, or a harder captain. | Forcing the player to know an external wiki before recognizing a useful reward. |
| **[Soulstone Survivors](https://www.soulstonesurvivors.com/)** | The official description combines active/passive skills, major bosses, curses, and several modes. Its Steam page records a June 17, 2025 full release. | Optional declared mutators can create new challenges after the normal mode has been learned. | Huge effects obscuring enemy tells, the ground, and the player's direction. |
| **[Slay the Spire 2](https://www.megacrit.com/news/2026-03-05-early-access-launch/)** | Mega Crit launched Early Access on March 5, 2026. It combines character-specific cards, encounters, relics, and a new cooperative mode. The [original game's press kit](https://www.megacrit.com/press-kits/slay-the-spire/) is also useful. | Make a card's identity, effect, and contribution to a build understandable; present meaningful alternatives with room to specialize. | Turning the mob album into an obligatory deck-management game that overwhelms the skating loop. |
| **[Crypt of the NecroDancer](https://braceyourselfgames.com/crypt-of-the-necrodancer/)** | Its developer describes movement and combat aligned to music and support for custom music. | Let a limited musical POI reward timing and movement while the main mode remains playable without beat-perfect inputs. | Enforcing a global rhythm lock on all movement or making essential information audio-only. |
| **[Ravenswatch](https://my.nacongaming.com/en-US/support/game/ravenswatch/faq)** | The publisher's FAQ describes activity selection under a chapter timer, bosses, cooperative tactics, and a custom modifier that removes the timer. | Make route objectives and difficulty options explicit. Preserve the difference between a scored challenge and relaxed experimentation. | Pretending the larger game's run length is a direct thirty-minute template or forcing all players to use the same pressure level. |

### Freshness and evidence qualifications

- The **current name is Hand of Fate: Hordes**; retaining the former title as a search alias is useful.
- **Death Must Die's planned infinite mode is future evidence**, not a verified current feature. The authoritative qualification is the August 17, 2026 announcement in its [official announcement feed](https://steamcommunity.com/app/2334730/announcements/).
- **Tiny Rogues, Hand of Fate: Hordes, and Slay the Spire 2** are identified here as Early Access. User-created store tags are not a reliable release-status signal; Soulstone Survivors still has an Early Access tag despite its recorded full release.
- **Roboquest cadence conflict:** the [publisher's release](https://corporate.starbreeze.com/en/press/press-releases/celebrate-roboquests-first-anniversary-with-the-endless-update-a-major-new-update-to-the-fast-paced-roguelite-shooter/) and a pre-release [developer feed](https://store.steampowered.com/news/posts/?appgroupname=roboquest&appids=692890&enddate=1732716138&feed=steam_community_announcements) give different combined-boss intervals. The report relies on the encounter-design principle, not either interval as a current balance fact.
- Gunfire Reborn's [October 2026 developer news](https://steamcommunity.com/app/1217060) announces music-oriented Lang Yao and the paint weapon Meltpaint for December. These are unusually relevant **announced concepts**, not shipped features or assets to import.
- Reference pages and their metadata were researched; this is not a playtest, frame-time measurement, or claim that any reference's complete mechanics have been audited.

## 3. A thirty-minute run with a readable shape

The session should be timed in server-owned active simulation time. A stalled server must not silently advance a run faster than the player can act. An upgrade or pause policy must be explicit, especially in multiplayer.

The following is an initial authoring proposal, not proven balance. The first slice implements the active timer and continuation prompt; it does not implement this complete authored sequence or a bespoke thirty-minute headliner:

| Active-play time | Player experience | Design purpose |
|---|---|---|
| **0–2 minutes: Drop-in** | See the chosen crew; learn the first attack tell; earn the first card; make a first useful upgrade choice. | Establish the mode's identity quickly. |
| **2–6: Find a line** | Follow one nearby graffiti target or musical landmark; encounter two contrasting enemy roles. | Teach that traveling and tagging matter. |
| **6–10: Build takes shape** | A captain or focused challenge presents a build-defining reward. | Give the run a recognizable direction. |
| **10–15: Route decision** | Choose a recovery route, card hunt, or riskier upgrade route. | Permit adaptation instead of pure reroll fishing. |
| **15–20: Crew variation** | The same gang introduces a different formation, elite role, or terrain relationship. | Keep the opponent's identity while changing the problem. |
| **20–25: Payoff** | Upgrades visibly interact; a short breathing space lets the player inspect progress and choose a final route. | Let the build feel powerful and the player plan. |
| **25–30: Headliner** | A named final encounter tests the abilities taught earlier; the route ends at a clear exit or celebration. | Deliver a proper ending. |
| **After the finish** | **Bank and return** or **continue into Endless**. Show precisely what is already secured. | Let the player own their time. |

The timer is not the entire objective. A final encounter can have a visible finish condition and an explicit timeout/extraction policy. Do not convert a successful thirty-minute session into an indefinite compulsory boss cleanup.

Early exit should retain already recorded album discoveries and the mode's earned persistent rewards. Victory can provide a separate completion bonus. The difference should be stated before starting, so leaving to answer a real-life interruption feels safe.

## 4. Three distinct progress tracks

| Track | Lifetime | Earned through | Spent or expressed as | Boundary |
|---|---|---|---|---|
| **Run XP** | Current attempt | Event adversaries and validated objectives | Run level and upgrade offers | Never vanilla experience orbs/levels, enchanting, mending, or Street Level by accident |
| **Run score** | Attempt result, then a record | Combat, route variety, successful tags, tricks, musical activities | Personal best, result breakdown, challenge medals | Does not become spendable power simply because a score is high |
| **Mob-card album / mode mastery** | Durable mode progress | Eligible defeated event actors and explicit mastery milestones | Cards, card ranks, mode unlocks, starting choices, presentation rewards | Remains separate from ordinary mob kills, loot tables, reputation, and normal survival progression |

Avoid a fourth currency until it solves a concrete problem. If a card milestone pays mode credits, explain exactly what the credits purchase and how a new player can reach the same basic gameplay without a grind.

### What run XP should feel like

The first meaningful choice should arrive quickly; later choices should support a build rather than interrupt it constantly. Present exact current and next values, interaction tags, and any prerequisite. Upgrade rewards can be banked as pending choices, but the player needs a safe, understandable way to make those choices.

For a first implementation, use a small authored pool with several interactions. Expanding a pool is useful only when offers remain understandable and relevant. At least one offer should be directly usable by the current loadout. A reroll should not immediately repeat the rejected choices when enough valid alternatives exist. Maxed upgrades leave the pool, and exhausted pools need a useful explicit fallback.

## 5. Mob cards with increasing rewards

The user's card idea should form a clear collectible loop, not another hidden RNG grind.

### Identity and eligibility

The first implementation identifies a collection entry by the **registered mob type and gang ID together** (`entityId|gangId`). Both identifiers retain their namespaces. A cow encountered with two different gangs therefore advances two distinct crew/species entries. A future species-wide album view can aggregate those entries while retaining crew-specific mastery; it must not silently merge or discard existing progress. Baby or visual variants are not separate mastery currencies in this slice.

An eligible defeated run actor creates a card pickup; collecting that pickup records the discovery or duplicate. The first collected card of an eligible entity/crew pair discovers that entry. Later collected cards add mastery progress. A mode-owned actor and a server-owned run ID are required; ordinary mobs, player-created farms, unloaded cleanup, administrative deletion, and environmental despawn must not produce cards. Replaying a death, pickup, or exit event must not duplicate the reward. Uncollected pickups are not already banked album discoveries.

In cooperative sessions, prefer shared encounter participation credit to a last-hit race. The exact contribution rule needs to include support and objectives, not only damage.

### Actual first-slice mastery thresholds and rewards

These are the chosen implementation values, **not playtest-validated balance**. They supersede the earlier illustrative research ladder of 1 / 5 / 20 / 50 / 150.

| Collected cards for one mob/crew pair | Mastery tier | Run XP on the milestone pickup | Run score on the milestone pickup |
|---:|---:|---:|---:|
| **1** | 1 | 18 | 75 |
| **5** | 2 | 56 | 260 |
| **15** | 3 | 118 | 565 |
| **40** | 4 | 204 | 990 |
| **100** | 5 | 314 | 1,535 |
| **250** | 6 | 448 | 2,200 |
| **600** | 7 | 606 | 2,985 |

For mastery tier `t`, an ordinary duplicate pays `4 + 2t` run XP and `10 + 5t` run score. Crossing into a new tier adds a one-time `12t²` XP bonus and `60t²` score bonus. The table shows the combined regular and milestone reward. The current collection count, tier, and next threshold are sent to the album UI. These rewards affect the active mode; they do not award vanilla XP or survival equipment.

Album-wide collection totals provide a second, separate benefit: **20 / 60 / 150 total collected cards unlock one / two / three starting upgrade drafts** for subsequent runs. The implementation queues the equivalent run XP at entry so the normal server-validated offer flow supplies those drafts. These totals count cards, not unique species, and are also unvalidated tuning. Core skating, painting, ordinary Minecraft, and starting the mode do not require these milestones.

Reward amounts increase with mastery without an ever-growing permanent vanilla damage bonus. Crossing a threshold is recorded automatically; there is no extra manual claim action to forget. After tier 7, the count and duplicate rewards continue, with no further mastery tier in this slice. Long-term prestige stamps, cosmetic identity, personal records, and optional challenges remain future design choices. They should offer reasons to continue beyond a health/damage treadmill.

The initial research suggested bronze/silver/gold/signature card treatments, behavior notes, cosmetic prizes, and challenge unlocks. Those remain presentation and content proposals, not currently implemented milestone rewards. Tune the actual thresholds and starting drafts only after observing first-card time, cards per thirty-minute run, time to each milestone, and how much veteran starting power affects a new player's experience.

### Proposed richer card presentation

The current slice records IDs, counts, tiers, and next thresholds. The following is a proposed expansion, not a list of completed album features:

- Mob portrait or render using the source entity's identity.
- Registered mob name and readable gang name.
- First encounter, total eligible defeats, highest chapter, and best run.
- A concise learned behavior hint.
- Current mastery tier and next reward.
- Explicit display of whether the entry is currently available with installed mods.

A removed optional mod should leave a readable historical entry rather than deleting the user's achievement or crashing the album. Do not render a live mob just to display every hidden card; use demand-driven previews and clear missing-content presentation.

## 6. Builds that make skating matter

These eight families describe broader interaction proposals, not effects attributed to the reference games. Alpha.3 implements bounded paint chaining, movement-triggered shockwaves, push/stagger and other artifact effects listed among its eighteen ranked choices in section 0. Alpha.4 adds a finite ridden-distance charge, a qualified landing pulse and priority-target vulnerability. The fuller rail-bank, aerial-tag, trail and combination mechanics below remain proposals; sharing a family name does not mean that complete design has shipped.

| Build family | Basic action | Example interaction | Player expression |
|---|---|---|---|
| **Tag chaining** | Aim paint at a rival | A tagged target carries a portion of a later paint burst to nearby tagged rivals | Prioritize targets and shape a pack |
| **Grind conductor** | Hold a readable rail line | Completing a qualifying grind banks a pulse released on a controlled landing | Choose a line and time a payoff |
| **Drift painter** | Turn through a pack | A slide leaves a short-lived mode-only painted trail; tagged targets react to it | Curve, position, and cross your own route |
| **Air mail** | Jump and aim | A successful aerial tag strengthens the next grounded shot | Alternate movement states deliberately |
| **Pickup courier** | Reach difficult pickups | A route objective briefly expands pickup reach or draws nearby run XP toward the player | Use momentum for an economy advantage |
| **Crowd control** | Redirect a dangerous group | Marked rivals become easier to stagger during a specific window | Make space rather than only maximizing damage |
| **Boombox breaker** | Visit a musical POI | Completing a short local challenge grants a temporary attack pattern or recovery benefit | Decide whether to defend, circle, or leave |
| **Captain hunter** | Pursue a durable priority target | A condition rewards accurate tagging or clean avoidance against elites | Trade safety and area clearing for a focused payoff |

A first pool could cover four of these families with two or three interacting upgrades each. Avoid inventing a huge untested catalogue whose combinations cannot be explained. The data model should support further families without requiring a new packet or bespoke screen for every effect.

Damage, radius, pickup attraction, and cooldown reduction should all show understandable limits. A powerful build must retain spatial information. Avoid recursive proc chains without an explicit depth/ownership rule.

## 7. Gang variety without flattening the mobs

JetSetCraft's existing gang atlas supplies names; reuse its stable IDs and naming/alias policy. These are illustrative authoring directions based on those existing names, not newly renamed crews.

| Existing crew | Authored combat role proposal | Route or objective relationship |
|---|---|---|
| **Dead Beat — zombies** | Endurance pressure, staggered packs, a slower durable captain | Long flowing lines with more than one exit |
| **The Bone Drones — skeletons** | Telegraphing ranged formations and crossfire lanes | Cover, approach choices, and aim under movement |
| **Creepaku Gouji — creepers** | Clearly signaled paint-burst zones and burst movement | Timed spacing and alternate lanes; no survival-world craters |
| **Arachnaphobia — spiders** | Vertical approaches and flanking pressure | Walls, ledges, and routes whose overhead clearance is readable |
| **Hex Appeal — witches** | Readable temporary denial zones and support priorities | Route switching and choosing which rival to tag first |
| **Hive Five — bees** | Coordinated aerial passes | Wide visibility and distinct attack/recovery phases |
| **Goo Groove — slimes** | Bounce rhythm and changing pack shape | Jump timing and landing choices |
| **The Milk Run — cows** | A playful competitive stampede or tagged relay | Broad turns and herd avoidance; preserve ordinary cows outside the mode |
| **Block Party — villagers** | Festival-themed competition roles | Social/music objectives and a readable event framing |
| **Ender the Influence — endermen** | Announced repositioning and line interruption | Route memory and anticipation, without random unavoidable hits |

### All mobs as a choice is a compatibility promise

The selection model should be registry-driven and default-inclusive for real mobs. A missing creature requires a useful reason: unavailable provider, unsuitable habitat, unsupported scripted lifecycle, insufficient safe space, or explicit provider exclusion. A fixed whitelist of convenient humanoids would miss the user's vision.

That does not mean spawning every entity into the same dry corridor. Aquatic, flying, huge, multipart, and scripted boss mobs need appropriate environment and lifecycle handling. Their card entries can exist before a safe encounter implementation does; the UI must distinguish **discoverable**, **installed but unavailable here**, and **not installed** honestly. Never silently substitute a zombie while displaying another mob's card.

Event adversaries are explicitly owned event actors. Existing persistent gang members and natural residents are not disposable spawn budget. Source mob identity, optional-mod ownership, and gear/drop rules must remain intact.

## 8. Use an endless world to offer routes, not empty distance

The value of endless terrain is that the next encounter can feel like a new place. A route still needs a coherent local shape.

### Author local encounter cells

Each cell should expose:

- One readable primary skating line and an alternate recovery line.
- A landmark visible before the player is committed.
- Appropriate spawn approaches, attack sightlines, and exits.
- One meaningful objective or encounter identity.
- A tested participant envelope for the included ride and mob types.
- A safe handoff to the next generated cell.

Cells can be assembled from authored patterns, varied by seed and crew, while physical validation rejects impossible approaches or landings. A compatible connection in a graph is not proof that a player can skate it. Do not generate or force-load a vast route merely to give the impression of infinity; build upcoming playable content on demand while preserving previous ownership and cleanup rules.

### Objective families

Fresh-wall, movement-style, and Boombox dance rewards have first-slice implementations. The larger authored objectives below are future directions; for example, the proposed mobile Boombox circle is different from the currently implemented four-second dance interaction.

| Objective | Player action | Reward direction | Anti-farm rule |
|---|---|---|---|
| **Fresh wall** | Reach and tag an indicated surface while under manageable pressure | Run XP, score, or a build offer | One award per objective ID; repainting the same surface is presentation only |
| **Tag relay** | Visit several visible targets along a line | A stronger choice or card hunt | Targets have server-validated order and route state |
| **Boombox circle** | Stay mobile within a broad music zone for a short encounter | Recovery, reroll, or temporary rhythmic effect | No reward from idle presence alone |
| **Moving record crate** | Intercept a visible route marker | A choice of music-related mode bonus | One owner/event identity and clear expiry |
| **Captain callout** | Accept and defeat a named priority target | Guaranteed targeted card progress and a defining upgrade | One summon/reward per encounter |
| **Recovery spot** | Reach a quieter location | Restore mode condition or inspect choices | Limited encounter use, not an endless invulnerability shelter |
| **Route trick challenge** | Perform a validated movement sequence | Score and a movement-oriented reward | Repeating a trivial motion in place does not compound reward |
| **Crew mural** | Complete different target sections across an area | End-of-chapter bonus and collective progress | Contributions tracked per target and participant |

Music should communicate place and phase, while visual signs communicate the same required information. Accurate beat interaction can be a bonus or a dedicated challenge. A player's soundtrack preferences, disabled music, latency, or reduced-motion settings should not prevent successful basic play.

## 9. Endless needs renewed decisions

An endless mode is healthy when the player periodically asks **which route, which rule, which risk, and when to leave**. Bigger numbers alone eventually stop asking useful questions.

### Proposed continuation structure

- Complete the normal finale and record the earned outcome.
- Show the next Endless chapter's crew variation and modifier before acceptance.
- Carry the current build forward with a controlled additional development opportunity.
- Change encounter patterns, route pieces, or objective combinations.
- Include a recovery/decision window and an explicit bank-and-exit option.
- Record chapter number and active modifiers in the final result.

### Candidate modifiers beyond the first slice

The implemented chapter rotation is described in section 0. These larger modifiers still need authoring and validation:

| Modifier | Meaningful change | Required counterplay |
|---|---|---|
| **Cross-town relay** | Rewards move farther along a visible route | Sufficient warning, an alternate path, and a fair travel allowance |
| **Captain duet** | Two complementary priority roles share the encounter | Compatible tells, recovery gaps, and no combined unavoidable attack |
| **Fresh paint** | New wall targets matter more than repeat combat | Objectives spawn where the selected ride can reach them |
| **Counter-rhythm** | A local pattern changes its timing | A visual signal; timing remains learnable and is not arbitrarily faster |
| **Rival formation** | Packs approach in a new composition | Space to read and redirect the group |
| **Courier chapter** | A carried objective changes route decisions | Clear drop/recovery rules and no inventory pollution |

Some modifiers can retire or rotate; indefinitely stacking every rule makes explanations and counterplay collapse. Cap or asymptotically approach mechanical speed, knockback, and cooldown limits that protect readability. Increase strategic pressure through combinations, roles, objectives, and opportunities to take risks.

The implementation needs bounded arithmetic and bounded work. Endless duration is not permission for integer overflow, infinite pending spawn debt, orphaned entities, ever-growing live packet payloads, or an unbounded log of every projectile. Durable summary statistics and current encounter state should remain distinct from temporary effects.

Do not make arbitrary global content cuts to mask performance problems. Profile the actual mode, schedule AI and spawning intentionally, aggregate suitable visual/XP representations, and preserve the full supported collection.

## 10. What makes this a good Minecraft mod

These are design judgments derived from the user's requested separation and JetSetCraft's existing compatibility covenant.

### Entry and exit are core gameplay

A normal player should be able to discover, start, understand, and leave the mode without operator permissions or external documentation. A Boombox or compact mode menu can expose crew, duration, difficulty, expected rewards, and Start. A command remains useful for accessibility, administration, and testing; it should not be the only comprehensible user flow.

Before entering, tell the player what carries in, what resets, what is retained, and how to leave. During the run, show a compact mode-only HUD: timer/chapter, run level, progress to the next choice, and current objective. Existing JetSetCraft HUD settings and Minecraft's survival HUD should remain coherent.

### Isolation should be observable

The user should return with the same ordinary inventory, vanilla XP, ride loadout, health/food policy, location policy, and other relevant pre-session state promised at entry. Temporary mode gear, effects, actors, projectiles, pickups, and audio leases need explicit ownership and teardown. Reward persistence must be idempotent.

Do not change global mob spawning or loot tables. Do not send nonparticipants into the mode. Do not force-load gang chunks. If a separate dimension or arena is used, describe that honestly and ensure it does not become a large compulsory generated world at install time.

Recovery must cover disconnect, server restart, death, administrative teleport, dimension change, stopped server, missing provider, failed spawn, and cancelled entry. If no safe location or creature adapter is available, the failure should leave the player's current Minecraft state intact.

### Fun requires iteration beyond compilation

An automated test can prove that a card rewards once, an XP pool is separate, and exit restores state. It cannot prove that the fourth encounter is exciting. The release notes must distinguish implementation tests from human playtesting and include a practical script for the latter.

## 11. Playtest questions and evidence to collect

Avoid an invented universal 'fun score.' Collect narrow observations that can change a design decision.

| Question | Observe | A useful failure signal |
|---|---|---|
| Can a new player start and leave? | Time and actions needed; points of confusion | Requires the wiki or loses ordinary state |
| Does skating improve play? | Compare stationary, simple circling, and purposeful route play | Staying still consistently wins without an intended specialized build |
| Is the first choice meaningful? | Can the player describe what changed? | Upgrade text changes but behavior is imperceptible |
| Are offers usable? | Offers skipped, repeated rerolls, unusable options | Most choices fail to interact with the build |
| Are cards rewarding? | First-card time, visible next milestone, expected reward | Duplicates feel like trash or progress is unclear |
| Do crews feel different? | Can players identify roles without reading a name? | A new crew is only a color and HP change |
| Is a defeat understandable? | Player can name the threat and counterplay | Offscreen, instantaneous, unavoidable, or visually hidden damage |
| Is thirty minutes enough? | Completion satisfaction and voluntary continuation | Finale has no closure or repeatedly overstays |
| Does Endless stay active? | New decisions, objective use, reason to cash out | Increasingly passive play with no meaningful route/upgrade decision |
| Does Minecraft remain normal? | Before/after state and nonparticipant behavior | XP/loot leakage, residual actors/effects, broken interactions |
| Does the server stay responsive? | Tick times by phase, spikes, allocation, entity lifecycle | Degradation grows with elapsed time even at equivalent encounter intensity |

### Minimum functional acceptance cases

1. No active session: ordinary kills grant ordinary behavior only, no mode cards or run XP.
2. Start then immediately leave: ordinary state matches the promised restoration contract.
3. A registered event actor defeats once: one reward, no vanilla XP or loot leakage.
4. Replay the death/cleanup notification: no additional reward.
5. A nonparticipant attacks an event actor: the isolation/credit policy is enforced.
6. Reach several XP thresholds in one update: all earned choices are accounted for without overflow.
7. Choose an old, invalid, unaffordable, or already-consumed offer: server rejects it safely.
8. Hit a card milestone more than once through reload/reconnect: reward remains one-time.
9. Duplicate card after the highest functional tier: still displays deliberate progression, no infinite stat loophole.
10. Disconnect or restart during a run: recovery follows the documented rule and no mode items leak.
11. Enter lava/water or attempt conflicting movement/item use: original ownership rules still hold.
12. Unsupported mob/habitat/provider: useful reason, no silent replacement or partial entry.
13. Finish thirty minutes: a definite success/extraction result; Endless requires explicit continuation.
14. Run an accelerated long-duration simulation: arithmetic, state size, and spawn lifecycle stay bounded.
15. Tag the same wall or replay a POI packet repeatedly: no repeat reward exploit.
16. Disable music/reduced motion: required cues and objectives remain understandable.

## 12. Recommended implementation order

**First: the trustworthy playable loop.** Session ownership, optional entry, separate XP, safe exit/recovery, repeated adversaries, a modest interacting upgrade pool, first-card/duplicate progression, and a genuine thirty-minute ending with explicit Endless continuation. Use the existing gang and ride systems rather than inventing replacement identities.

**Second: movement and place.** Validate wall-tag rewards, one musical POI, several enemy roles, a named captain, route choices, and the visible card album. These give the mode its JetSetCraft character.

**Third: breadth with evidence.** Expand mobs through the compatibility contract, author crew-specific patterns, add more build interactions, and tune Endless using observed sessions. Keep the working slice playable throughout; do not wait for every proposed card and boss before testing the core.

**Highest priority design decision:** the mode should reward riding toward something. Cards and damage numbers motivate another encounter, but a good line, a visible tag target, a rival's readable attack, and a musical destination make the current minute worth playing.

## 13. Further primary reading and media

- [Roboquest Endless developer feed](https://store.steampowered.com/news/posts/?appgroupname=roboquest&appids=692890&enddate=1732716138&feed=steam_community_announcements): explicit discussion of why simple looping did not fit its progression and route content.
- [Roboquest publisher release](https://corporate.starbreeze.com/en/press/press-releases/celebrate-roboquests-first-anniversary-with-the-endless-update-a-major-new-update-to-the-fast-paced-roguelite-shooter/): endless, mutators, and single-player save/resume.
- [Night Swarm official update history](https://steamcommunity.com/app/3668370/allnews/): reward economy, builds, readable effects, and exit/progression fixes.
- [Hand of Fate: Hordes official hub](https://steamcommunity.com/app/3075410/): card encounters, rewards, and navigation/collision iteration.
- [Death Must Die official announcements](https://steamcommunity.com/app/2334730/announcements/): synergy-pool and route-design reflection; observe the release qualification above.
- [Crypt of the NecroDancer music FAQ](https://braceyourselfgames.com/crypt-of-the-necrodancer/faq/): custom music and the practical limits of automatic beat detection.
- [Hades II gameplay showcase](https://www.youtube.com/watch?v=-SnaCUsUF3E): linked by Supergiant's official game page. Link attribution verified; video playback was not inspected.
- [Roboquest Endless update video](https://www.youtube-nocookie.com/embed/pfgkbM6JLzk?fs=1&modestbranding=1&rel=0): embedded by its official developer feed. Link attribution verified; video playback was not inspected.
- [Dead Cells official embedded trailer](https://www.youtube-nocookie.com/embed/02G3GUt6Nzo?rel=0&showinfo=0): linked from the developer's site. Link attribution verified; video playback was not inspected.

## 14. Dungeons I and II: a usable reference inventory

Research checked **November 1, 2026**. Minecraft Dungeons II is released: its [official launch article](https://www.minecraft.net/en-us/article/minecraft-dungeons-ii-is-live) and [October 22, 2026 hotfix](https://feedback.minecraft.net/hc/en-us/articles/49736028710541-Minecraft-Dungeons-II-1-1-3-0-Hotfix) establish that this is a current game, not an unannounced sequel. Older previews below are explicitly labeled as previews.

The tables inventory the **families and examples verified in the linked official material**. They are not a complete item database for either game. A marketing overview, historical patch note, or preview does not establish every launch item, numerical balance value, or possible combination. No game files, artwork, audio, or implementation code are imported. The JetSetCraft column proposes original mechanics; only section 0 and recorded verification establish what is implemented.

### 14.1 First-game artifacts and movement builds

The official [essential artifacts article](https://www.minecraft.net/en-us/article/minecraft-dungeons--9-essential-artifacts) supplies the first seven reference groups below. Its old durations and balance values are not adopted. The [September developer blog](https://www.minecraft.net/en-us/article/dungeons-september-dev-blog) supplies the remaining groups and describes the Acrobat, Summoner, and Soul build directions.

| Verified reference | Original JetSetCraft design direction | Useful decision it creates |
|---|---|---|
| Corrupted Beacon: beam | **Paint Projector** emits a short aiming beam that spends session charge and terminates at arena geometry. | Commit to a clear sightline or keep moving to safety. |
| Light Feather: tumble, knockback, stun | **Air Break** rewards a completed jump/landing with a brief, bounded space-making pulse. | Land near pressure deliberately rather than spam jumps in place. |
| Wonderful Wheat / Tasty Bone: summons | **Crew Call** brings a temporary session helper with a readable support role. | Choose collector, protector, or marker; do not copy a survival pet. |
| Gong of Weakening: attack/defense debuff | **Breakbeat Mark** opens a short vulnerability window on owned rivals. | Mark the captain or soften a dense group before a committed pass. |
| Love Medallion: temporary allies | **Crowd Turn** briefly distracts a limited number of session rivals. | Create a route through a pack without converting ordinary world mobs. |
| Soul Healer: resource-funded healing | **Encore Charge** spends a mode resource for virtual-vitality recovery. | Save charge for emergency recovery or spend it on offense. |
| Corrupted Seeds: entangle and poison | **Wet Paint** leaves a bounded slowing patch with session-only damage ticks. | Shape a corner or protect a tag wall; no terrain or potion contamination. |
| Boots of Swiftness / Ghost Cloak: mobility references | **Tempo Surge** briefly accelerates movement within the skating solver's safe bounds. | Use it to cross a dangerous opening or finish a route objective. |
| Soul Lantern: resource-funded ranged summon | **Signal Sprite** spends charge on a short-lived ranged helper. | Trade immediate burst for a dependable support interval. |
| Buzzy Nest: placed temporary summon source | **Portable Stage** creates a small defense objective with a strict helper cap. | Circle the stage for support or abandon it for a better route. |
| Dynamo / Burst Bowstring / Acrobat: roll and attack synergies | **Line Battery**, **Kickflip Volley**, and **Quick Reset** connect movement, attack, and recovery. | Establish a short route that charges a finite payoff; no unlimited idle stacking. |
| Busy Bee / Tumblebee / Bag of Souls: defeat, movement, resource triggers | **Hype Crew**, **Air Support**, and **Bigger Mixtape** expand a build through distinct trigger types. | Choose stronger actions, more frequent actions, or greater resource capacity. |
| Rapier / Burst Crossbow: different combo and burst rhythms | **Fine Liner** uses fast accurate tags; **Burst Can** commits to a short burst. | Pick precision against elites or coverage against moving packs. |
| Beenest Armor / Ghostly Armor: reactive and evasion identities | **Crowd Jacket** responds to real pressure; **Slipstream Pads** reward clean avoidance. | Choose a forgiving recovery style or an evasive execution style. |

Do not make movement upgrades activate merely because a button was pressed. A qualifying grind needs distance; an aerial trigger needs an actual airborne interval; a clean-dodge reward needs a relevant nearby threat. Each activation receives a session ID, owner, target limit, internal cooldown, and maximum chain depth. These are JetSetCraft implementation requirements, not claims about the reference games.

### 14.2 Broader first-game gear and enchantment coverage

The official [2022 developer update feed](https://steamcommunity.com/app/1672970/allnews/?l=english) names additional equipment and records changes to its behavior. Its evidence supports the concise reference list below; the proposed JetSetCraft actions deliberately use new rules and tuning.

| Reference family and verified examples | JetSetCraft application proposal |
|---|---|
| Heavy melee: Anchor, Boneclub, Mace, Obsidian Claymore | A slower **Paint Slam** creates a strong landing payoff, a narrow safe timing window, and readable recovery. Large damage must cost positioning or time. |
| Fast/continuous melee: Daggers, Gauntlets, Broken Sawblade | **Marker Rush** builds pressure through short accurate passes; sustained attacks have heat or a finite charge window. |
| Sweeping melee: Cutlass, Coral Blade, Katana, Soul Scythe | **Ribbon Sweep** uses a capped forward arc. Area upgrades widen coverage without reaching through arena walls. |
| Bow identities: Hunting Bow, Shortbow, Trickbow, Wind Bow | Separate **Long Shot**, **Quick Tag**, **Bounce Tag**, and **Push Shot** behaviors; preserve a reason to aim. |
| Artifacts: Fishing Rod, Eye of the Guardian, Ice Wand, Power Shaker | **Hook Mark**, **Spotlight Beam**, **Freeze Frame**, and **Impact Can** cover interrupt, sustained aim, brief control, and empowered-hit families. |
| Artifacts: Satchel of Elixirs, Satchel of Snacks, Scatter Mines, Spinblade | **Pit Kit**, **Snack Drop**, **Paint Pucks**, and **Return Disc** provide mode recovery, prepared territory, and outbound/return positioning. |
| Artifacts: Shadow Shifter, Vexing Chant, Totem of Casting | **Ghost Line**, **Hype Echoes**, and **Remix Station** explore temporary evasion, controlled helpers, and ability rhythm. |
| Enchantments: Ambush, Freezing, Gravity Pulse, Reckless | Offer conditional damage, brief slowing, local grouping, and an explicit virtual-vitality trade. Telegraphed risk should buy a visible benefit. |
| Enchantments: Anima Conduit, Beast Boss, Beast Burst, Soul Siphon | Build a coherent charge-and-helper branch with recovery, helper strength, a finite burst, and resource gain. |
| Enchantments: Dipping Poison, Lucky Explorer, Illager's Bane, Smiting | Explore prepared shots, movement rewards, and encounter-specialist bonuses. Avoid permanent advantages tied to farming harmless mobs. |

This table is a design vocabulary, not an instruction to add every row at once. Begin with a small set whose interactions are visible. Keep less mature families in the backlog until their target ownership, effect clarity, and resource loops are proven. The historical patch feed also records recursive splash/enchantment problems; JetSetCraft secondary effects should be unable to trigger themselves without a deliberate bounded rule.

### 14.3 What the sequel contributes

The [official Dungeons II systems overview](https://www.minecraft.net/en-us/article/minecraft-dungeons-ii-gameplay-systems) describes connected regions, settlements, fast-travel points, quests, variable cave/ruin layouts, rechargeable ranged ammunition, jumping/airborne attacks, passive talismans, gear effects, and an enchantment-book library. These support a richer **journey and build language**, not a requirement to add a full RPG inventory to Rival Rush.

The [June 10 Xbox preview](https://news.xbox.com/en-us/2026/06/10/minecraft-dungeons-2-arpg-details-demo-xbox-games-showcase-2026/) specifies one melee weapon, one ranged weapon, three artifacts, four armor pieces, and three talismans: twelve slots. It also demonstrates a dog-summoning talisman and a defeat-triggered healing-ground synergy. The [August 26 Xbox preview](https://news.xbox.com/en-us/2026/08/26/minecraft-dungeons-ii-new-features-gamescom-2026/amp/) describes gear improvement, effect rerolls, a roll-frequency/roll-shot/fire-arrow combination, quick inventory access, and a combined effects list.

| Confirmed sequel direction | Recommended JetSetCraft translation | Boundary |
|---|---|---|
| Gear defines a class; active and passive slots differ | Treat an equipped Rush ability and its support upgrades as a visible build. Present the current attack pattern, passive modifiers, and activation condition together. | Do not add twelve new menus simply to imitate twelve slots. |
| Independent armor pieces and talismans | Later, offer a small number of mode-only modules associated with pads, skates, paint gear, and a crew charm. | Never rewrite or duplicate another mod's equipped armor or Curios state. |
| Enchantment books and chosen gear slots | Unlock a **trick library** across activities. Discovering a trick adds future draft options or an alternative starting style. | Unlock variety first; raw starting power needs explicit limits. |
| Jumping and airborne attacks | Give air routes a real combat purpose: aerial accuracy, safe descent, and landing bursts. | Respect the existing movement solver and collision; no hidden flight. |
| Ranged ammunition recharges | Keep the core paint attack usable through a predictable session resource rhythm. | A survival ammo reserve must not become the entry fee for the mode. |
| Connected regions and travel points | Present an arena journey as a local route through distinct neighborhoods with nearby visible goals. | Load bounded rooms; do not generate an unlimited active corridor. |
| Gear refinement and effect rerolls | Add a workshop stop that offers a clear sidegrade, a focused reroll, or an upgrade to an already-loved build. | Never reroll silently; display the exact before/after change. |
| Aggregate effects display | Show damage, crit chance, crit multiplier, area radius, cooldown, pickup radius, and each trigger in one readable build panel. | Show actual capped values and conditions, not only vague rarity colors. |

The [official game page](https://www.minecraft.net/en-us/about-dungeons-ii) provides named reference examples: Battlestaff; Lightning Surge and Soul Blast enchantments; Powerbow, Shortbow, Longbow, Trickbow; War Hammer, Battle Hammer, Mob Mallet; Sword, Greatsword, and Double Daggers. The useful distinction is attack rhythm and reach. A JetSetCraft staff-like line pulse, hammer-like landing splash, ricochet shot, and quick precision combo can share one server-owned ability framework while playing differently. These official examples do not verify a complete launch roster or exact effect numbers.

The October hotfix above fixed rapid artifact activation and an arena that could block progress after leaving and rejoining. Make **rapid valid activation**, **duplicate input**, **route transition**, and **rejoin during an encounter** explicit JetSetCraft acceptance cases. A powerful build is enjoyable only if its controls and exits remain dependable.

### 14.4 Focused alpha.4 adaptation: four useful decisions

The **2 November 2026** follow-up selects four ideas that work with the existing three-artifact limit. The reference games supply mechanisms; the distance thresholds, multipliers, cooldowns, rank transfer and ownership rules in section 0 are original JetSetCraft choices with recorded machine acceptance and human balance tuning still outstanding.

| Primary-source mechanism | Alpha.4 adaptation | Why it adds a decision |
|---|---|---|
| The [Dungeons September developer blog](https://www.minecraft.net/en-us/article/dungeons-september-dev-blog) describes **Dynamo** adding next-attack power through rolls. | **Line battery** earns one finite primary-tag payoff from observed riding distance. The historical article's unbounded stacking is not adopted as a balance target. | Finish a productive line, then choose which rival receives the charged paint or contact tag. |
| The same blog makes **Light Feather count as a roll**; the [official artifacts guide](https://www.minecraft.net/en-us/article/minecraft-dungeons--9-essential-artifacts) describes its tumble and crowd control. | **Air break** connects a genuine airborne sequence and landing to a bounded pulse. It does not copy the feather's original movement or stun behavior. | Commit to a landing near a group or use the route to regain space. |
| The artifacts guide describes **Gong of Weakening** reducing enemy attack and defense. | **Breakbeat mark** uses a narrower, temporary virtual vulnerability on a priority owned rival. It does not apply native Weakness or claim the Gong's complete effects. | Concentrate a charged tag or a timed ability on a visible high-value target. |
| The [official Dungeons II Gamescom report](https://news.xbox.com/en-us/2026/08/26/minecraft-dungeons-ii-new-features-gamescom-2026/amp/) describes favorite-equipment upgrades, buff rerolls and a combined effects view. | **Artifact remix** preserves the replaced family's earned rank and asks for an explicit between-room choice. | Change build direction after seeing the next routes without throwing away earlier investment. |

The Dungeons II report also demonstrates a roll-to-arrows-to-fire combination. The useful lesson is that one understandable action can connect several build choices. JetSetCraft must keep secondary effects bounded and make the trigger, readiness and result visible. A stronger number alone does not explain why a player's route mattered.

Minecraft Dungeons II's [official launch article](https://www.minecraft.net/en-us/article/minecraft-dungeons-ii-is-live) records its release on **29 September 2026**. The August source remains a labeled hands-on preview, not a complete launch item database. This follow-up does not claim that every Dungeons I or II item has been catalogued, imported or reproduced.

### 14.5 Mechanical coverage and the remaining catalogue

**Continuation research checked 1 November 2026 (America/Denver).** The inventory above remains the reference list; this matrix connects those references to implementation work without creating a second list of the same items. **Existing** identifies alpha.4 behavior described in section 0. **Continuation** identifies the six new mode-native families and the limited permanent Style Library implemented in the working alpha.5 change; their runtime verification and merge receipt remain pending and must not inherit alpha.4's historical test result. **Design** identifies a remaining feature. A covered mechanical family does not mean every item in that family has been reproduced.

The source keys in the matrix refer to the primary sources below. They establish the examples and broad behavior, not JetSetCraft's triggers, numbers, names, or balance:

- **D1-A:** Mojang's [nine essential artifacts](https://www.minecraft.net/en-us/article/minecraft-dungeons--9-essential-artifacts), for beam, mobility, companions, weakening, conversion, healing, and poisonous entanglement.
- **D1-B:** Mojang's [September developer blog](https://www.minecraft.net/en-us/article/dungeons-september-dev-blog), for Acrobat/Summoner/Soul archetypes, movement and attack combinations, gear identity, and Blacksmith improvement.
- **D1-P:** Mojang's [official Steam update feed](https://steamcommunity.com/app/1672970/allnews/?l=english), specifically the **19 October and 30 November 2022** notes, for the additional equipment/enchantment names and the effects described in section 14.2. Historical balance values are not current tuning targets.
- **D1-T:** Mojang's [A towering challenge](https://www.minecraft.net/en-us/article/a-towering-challenge), for build combinations, Buzzy Nest/Golem Kit, support equipment, and Tower-earned equipment used outside the Tower.
- **D2-S:** Mojang's [Dungeons II gameplay systems](https://www.minecraft.net/en-us/article/minecraft-dungeons-ii-gameplay-systems), for equipment slots, passive talismans, variable effects, jumping, rechargeable ammunition, and an upgradeable enchantment-book collection.
- **D2-G:** The [official Dungeons II game page](https://www.minecraft.net/en-us/about-dungeons-ii), for named weapon examples and the Lightning Surge/Soul Blast references. It does not supply an exhaustive item manifest or verified effect coefficients.

| Mechanical family and verified examples | JetSetCraft coverage in this continuation | Remaining work and useful play decision |
|---|---|---|
| **Attack rhythm and reach:** Rapier/Burst Crossbow (D1-B); heavy, quick, and sweeping weapons (D1-P); Battlestaff (D2-G) | **Existing:** aimed/automatic paint, Quick hands and Paint pressure. | **Design:** distinct sustained, burst, sweeping, and precision patterns. A faster interval alone does not implement the corresponding weapon family. |
| **Splash and linked attacks:** Fireworks Arrow (D1-B); Exploding Crossbow (D1-P) | **Existing:** Firework cap and Arc paint give bounded secondary damage; area upgrades change coverage. | Keep a reason to place the first tag well. Neither artifact grants every projectile enchantment or unlimited chains. |
| **Several targets from one trigger:** Burst Bowstring (D1-B) | **Continuation — Split chorus:** repeated accepted primary tags produce a bounded forward volley against additional visible owned rivals. | Its trigger is a tag cadence, not a copied roll/arrow rule. Cone positioning distinguishes it from radial splash. |
| **Damage over time:** Corrupted Seeds (D1-A), Dipping Poison (D1-P) | **Continuation — Venom ink:** a primary tag applies nonstacking virtual damage over time; further tags refresh its duration. | Poisonous terrain, spreading clouds, and native potion effects remain **Design**. Tag several rivals and let the ticking effect support a moving route. |
| **Slowing and movement control:** Freezing (D1-P) | **Continuation — Chill paint:** primary tags briefly reduce Rush-directed steering and attack cadence. | A full freeze, rooting terrain, and foreign AI control remain **Design**. Choose a dangerous pursuer and create space without changing its native attributes. |
| **Displacement and landing control:** Light Feather (D1-A) | **Existing:** Bass horn pushes/staggers; Air break rewards a qualified moving landing. | Neither is a native mob-stun adapter. The player must still choose a useful approach and landing. |
| **Vulnerability windows:** Gong of Weakening (D1-A) | **Existing:** Breakbeat mark gives a temporary virtual-damage window on an owned priority rival. | Native enemy attack reduction and every defense interaction remain **Design**. Focus a charged tag during the visible window. |
| **Movement into an empowered hit:** Dynamo and Acrobat (D1-B) | **Existing:** Line battery charges a finite primary-tag payoff through measured riding; Rail shockwave rewards qualifying movement. | Roll-specific enchantments and unrestricted stacking are not adopted. Keep building a line while deciding where to spend the payoff. |
| **Temporary tempo:** Death Cap Mushroom (D1-A); Acrobat (D1-B) | **Existing:** Tempo drive, Quick hands and Fast rewind cover attack/ability cadence. | A temporary native movement-speed buff remains **Design**. Speed must remain compatible with the ride solver and readable attack tells. |
| **Earned resource into burst power:** Soul archetype and Bag of Souls (D1-B) | **Existing:** Soul mixtape uses its own healing charge. **Continuation — Soul burst:** owned defeats charge a separate short-range burst. | Do not imply native souls, mana, or soul-item support. A charged burst rewards close positioning, while a recovery build uses its own separate charge. |
| **Recovery tied to a build:** Soul Healer (D1-A), Anima Conduit (D1-P) | **Existing:** Soul mixtape and Easy breathing. **Continuation — Radiant groove:** a fixed cadence of accepted primary tags restores virtual vitality. | Party healing, native health, and healing-ground effects remain **Design**. The new primary-hit cadence is original, not a claim to reproduce Radiance or Soul Healer exactly. |
| **Defensive identity:** Ghostly Armor (D1-B); separate armor pieces (D2-S) | **Continuation — Beat ward:** a mode-only shield absorbs incoming run damage and recharges after a quiet interval. | This is an original shield mechanic; it does not claim Ghostly Armor's roll-through or miss-chance effects. Evasion, reflect, native armor and Curios integration remain **Design**. |
| **Companions and placed support:** Tasty Bone/Wonderful Wheat (D1-A), Soul Lantern/Buzzy Nest (D1-B), Golem Kit (D1-T) | **Design:** session-owned helpers and placed support have not been implemented by these six additions. Persistent crew profiles are a separate system. | Choose a clearly limited collector, protector, or ranged-support role; define summon count, ownership, navigation, expiry, and room cleanup before adding the family. |
| **Temporary allegiance changes:** Love Medallion (D1-A) | **Design:** Crowd Turn has no implemented counterpart in this continuation. | A distraction must preserve source ownership and restore the actor without survival taming, boss conversion, or reward multiplication. |
| **Aimed beams and channels:** Corrupted Beacon (D1-A), Eye of the Guardian (D1-P) | **Design:** Paint Projector/Spotlight Beam remain proposals. | An aimed line, a duration, obstruction tests and a resource cost make a beam different from another radial pulse. |
| **Prepared zones and returning attacks:** Scatter Mines/Spinblade (D1-P) | **Design:** Paint Pucks, Return Disc and persistent painted areas remain proposals. | Placement, arming, travel and cleanup need real behavior; an icon or a damage multiplier does not cover this family. |
| **Gear investment and changing a build:** Blacksmith (D1-B); enchantment books and upgrades (D2-S) | **Existing:** four artifact ranks and exact-rank between-room remix. **Continuation:** a permanent sixteen-lesson Style Library uses shared crew XP to learn freely selectable rank-one starting artifacts. | **Design:** a comprehensive equipment/enchantment collection, gear affixes, saved multi-item loadouts and a native gear-improvement workshop. One retained starting choice does not reproduce Dungeons equipment or its improvement economy. |
| **Passive equipment combinations:** independent armor, talismans and random effects (D2-S) | **Existing:** stat choices support three artifact families. | **Design:** mode-only equipment modules and saved loadout combinations. Ordinary armor and another mod's equipment stay under their own systems. |
| **Movement with combat payoff:** jumping/jump attacks (D2-S); named Lightning Surge/Soul Blast examples (D2-G) | **Existing:** Air break and Line battery make observed movement useful. | Dungeons II's precise enchantment coefficients and complete trigger catalogue are not established here. Add new combinations only when the cause and result remain clear to the player. |
| **Progress earned in one mode that helps another:** Tower rewards (D1-T) | **Existing:** accepted activity/arena/Rush/mission results contribute to shared crew progress; the mob album grants its documented Rush starting drafts. **Continuation:** the same cumulative crew XP learns sixteen starting styles at 55–2,000 XP, including before founding a house. | Current scope is one selected artifact rank for the next Rush, replacing the facility preparation. Full native item coverage and broader equipment collections remain **Design**. Ordinary drafts stay available without lesson unlocks; temporary run power does not become survival damage. |

Magnet reach, critical chance, critical damage, ability area and reward emphasis are **the user's requested JetSetCraft axes**. Their implementation and caps belong to sections 0 and 15.3; they are not presented as a verified universal Dungeons stat list. A desirable arena reward should advertise which axis or family it improves, then show the actual accepted change. It should never claim an unavailable provider spell or a not-yet-authored ability.

**Limited permanent-collection contract in alpha.5.** `CrewStyleLibrary` reads cumulative XP from the existing pre-house or founded account; no currency is spent and no extra reward settlement is created. A chosen, earned and available artifact supplies exactly one starting rank through the existing preparation path and occupies one of the three family slots. The default `HOME` choice uses an eligible operational house preparation. A saved provider-dependent choice remains stored when its provider disappears and falls back to that same house path; no eligible facility means the ordinary build. Changes validate the player's independent style revision and are blocked during active street games or Rush. Album starting drafts remain separate, and all available artifact families remain eligible in normal drafts.

The world crew store now writes **schema 3**, including per-owner style choices and revisions. It accepts **schemas 1 and 2**, preserving their existing XP, house/pending balances and receipts; absent style choices default to `HOME`, so old earned XP can immediately satisfy lessons without fabricated historical rewards. Unknown future schemas retain their original payload read-only, while malformed style rows are quarantined and block affected owners. This is implemented save handling, with the continuation's runtime verification still pending. The [crew guide's sixteen thresholds](../wiki/Player-Chapters-Posse-and-Mob-Archetypes.md#style-library-every-modes-xp-helps-the-next-run) and [Rush artifact rules](../wiki/Rival-Rush.md#six-new-artifact-rules--alpha5-continuation) describe the current bounded collection, not a complete native item catalogue.

#### Extending coverage without losing the implementation boundary

Continue the item audit in these existing tables by recording **game/version, exact source, family, activation condition, resource, target shape, rank behavior, interactions, and implementation status** for each newly verified example. Group unique variants with their parent family until a different behavior warrants its own row. Keep numerical coefficients unverified until a version-specific primary source or an authorized reproducible observation establishes them. An original JetSetCraft tuning value should always be identified as such.

The remaining catalogue work includes exact item manifests and unique variants for both games; DLC-specific and sequel artifact coverage beyond the examples above; complete enchantment/talisman interactions; and support, summon, trap, beam, and equipment-module implementations. No total-coverage percentage is given because a verified denominator is not available. This is systematic family coverage with an explicit remaining queue, not a complete native Dungeons item import.

## 15. Arena journeys, POIs, and rewards that connect the modes

### 15.1 Night Swarm evidence and the intended adaptation

Night Swarm's [official publisher launch page](https://www.madmushroom.gg/news/become-the-swarm-and-rebuild-your-legacy-in-night-swarm-available-now) verifies companion recruitment/upgrades through reputation, equipment blueprints, run growth, bosses at biome ends, and rebuilding a home. The [developer update feed](https://steamcommunity.com/app/3668370/allnews/) discusses late-game currency losing value, later currency sinks, more specialized builds, POI readability, and additional endgame challenges.

The developer feed's **14 December 2025** patch notes explicitly mention a **branching path screen**, precise upgrade previews, avoiding repeated companion choices on rerolls, and faster attraction of earned resources. Those are concrete references for readable route decisions and reward collection. Its **20 January 2026** update explains the need for useful late-game resource sinks and specialized set-item builds. The user's requested branching arena map remains a JetSetCraft design requirement: this evidence establishes that Night Swarm has branching choices, but not its complete node topology or exact reward algorithm. The [publisher-linked launch trailer](https://www.youtube.com/watch?v=Mc7IakyiVrc) is a useful visual reference to review; this research did not treat its playback as inspected evidence.

### 15.2 A route should answer “what do I want next?”

Offer a small set of next destinations with different purposes. The preview should show the arena's scale, movement emphasis, rival crew, expected reward family, and any rule modifier. Do not conceal the basic reward behind a mystery icon when the purpose is to let the player steer their build. A mystery stop can be one deliberate choice among known alternatives.

| Proposed destination | Movement/combat identity | Distinct reward purpose | Avoid |
|---|---|---|---|
| **Rail Yard** | Long grinds, changing cover, ranged rivals | Charge, cooldown, or precision upgrades | A long flat damage hallway with no useful rail decisions |
| **Wall Market** | Several approachable tag walls and looping alleys | Graffiti mastery, crew reputation, art unlock progress | Repeatedly tagging one safe wall for unlimited rewards |
| **Sky Court** | Broad landings, air routes, readable edges | Area, aerial, or mobility options | Unavoidable fall losses or launching opponents out of cleanup bounds |
| **Scrap Jam** | Short focused encounters around a supply objective | Gang-house construction progress | Survival resource duplication from spawned rivals |
| **Record Exchange** | A short musical challenge with visual timing cues | A themed draft, reroll, or track/cosmetic discovery | Requiring music volume or perfect external audio timing |
| **Crew Stop** | Recovery and a readable social beat | Crew training choices or virtual-vitality recovery | Mandatory downtime or an unskippable shopping session |
| **Captain Stage** | Telegraphed priority target plus supporting packs | Card mastery, an unusual modifier, or a chapter milestone | Arbitrary invulnerability and a health sponge without movement interaction |

A custom arena definition should declare bounds, spawn regions, safe arrivals, traversal pieces, tag surfaces, POI slots, supported encounter roles, completion condition, and reward category. Use stable arena IDs and data validation. “Large” should mean room to take faster lines, not merely more distance between enemies. An aquatic-only provider mob requires a real supported habitat; a flyer needs clearance; a boss needs an adapter that understands its native encounter contract.

Between encounters, give a short safe selection moment and retain the build. The route choice must be server-authorized, single-use, and tied to the current completed node. A late packet from the previous room cannot reopen rewards or teleport the player. If a destination cannot prepare safely, keep the player in a safe completed room and offer a valid alternative or extraction. Optional Endless should reintroduce meaningful route/build decisions and reward sinks without growing active room count, helper count, or saved entity history without bounds.

### 15.3 Magnet, crit, area, and ability choices

These are original tuning requirements requested for JetSetCraft, not copied Dungeons statistics.

| Upgrade or POI | Player-facing meaning | Server rule to make it reliable |
|---|---|---|
| **Magnet stop** | Sweep up the run rewards already earned in this room. | Attract only eligible pickups owned by this session; preserve their identity and one-time collection accounting. Do not pull vanilla items or XP. |
| **Pickup radius** | Collect nearby mode pickups without breaking a good line. | Cap radius and work per tick; a pickup cannot be credited to two sessions. |
| **Damage** | Improve the ordinary hit before special conditions. | Centralize damage calculation and expose the actual value. |
| **Crit chance** | More eligible hits can become critical. | Clamp probability; explicitly define which direct/secondary hits can crit. |
| **Crit power** | A critical hit has a larger multiplier. | Separate chance from multiplier and show both. Avoid multiplicative recursion. |
| **Ability area** | A splash, cone, beam width, or pulse covers more space. | Document radius versus area; clamp geometry and target count; do not query an entire dimension. |
| **Cooldown / tempo** | An ability becomes available more frequently. | Use a floor on cooldown and avoid granting multiple activations for one input. |
| **Reroll / focused draft** | A player can pursue an existing build or escape a dead offer. | Consume exactly one server-owned opportunity; never reroll into an unavailable or capped-only choice. |

A magnet is particularly satisfying after a hectic encounter, but it should recover earned opportunities rather than mint extra rewards. An area upgrade should visibly change an attack. A damage upgrade should give useful feedback in the next fight. Prefer a few changes that players can feel over a screen full of tiny percentages.

### 15.4 One crew, several reasons to play

The shared progression proposal has three layers: **session power**, **durable JetSetCraft progress**, and **ordinary Minecraft property**. Rush XP, temporary abilities, virtual health, and encounter charge end with the run. Eligible activity results can advance gang reputation, crew training, house development, discoveries, and cosmetic identity. The actual placed gang-house build remains the player's construction project.

Each mode should contribute something recognizable to the same crew without making all activities interchangeable. Tag can emphasize art and territory identity; racing can emphasize route mastery and mobility options; King of the Hill can emphasize crew coordination; Rush can emphasize cards, build discoveries, and expedition supplies. Players should see a concrete shared contribution after a legitimate result, including an honest partial result when the activity rules support one. Starting and immediately leaving a mode, idling, or replaying its completion event is not a legitimate result.

The house is a strong place to spend those rewards. Expand the permitted plot, then let the player place the upgraded workbench, record shelf, training equipment, mission board, or display themselves. A schematic unlock does not auto-build someone else's house. Each station should unlock a readable function, such as another loadout choice, better mission information, a focused reroll, a cosmetic display, or room for another crew member. An upgrade preview should say what changes now and what the player needs to place.

Crew missions should complement active play. Let a trained member return with a modest, bounded construction contribution or a choice of blueprint leads. Show duration, risk, relevant member strengths, and rewards before dispatch. Avoid compulsory real-time check-ins, unlimited offline stockpiles, permanent member loss without an explicit opt-in challenge, or a superior idle economy that makes skating feel inefficient. Member identity, friendship, and specialization should matter more than a growing list of anonymous resource timers.

At functional caps, provide a deliberate destination for further progress: cosmetic house variations, alternate stage styles, route challenge stamps, extra paint patterns, music displays, or optional prestige. Do not require ever larger permanent damage numbers simply to keep a currency spendable. The Night Swarm economy update is evidence that this concern deserves early attention; these specific sinks are JetSetCraft proposals.

## 16. Provider compatibility: concrete seams and exact test targets

### 16.1 Separate five different promises

An installed mod can work with JetSetCraft in one respect while remaining unverified in another. Report these separately: **co-loading**, **dimension entry/return**, **source-material traversal**, **eligible source-mob identity**, and **native combat abilities**. A registry manifest proves neither a boss encounter nor a spellbook workflow. Follow the [Standalone Compatibility Covenant](STANDALONE_COMPATIBILITY_COVENANT.md) and the existing [terrain and arena contract](../wiki/Terrain%E2%80%90Aware-Event-Circuits-and-Boss-Arenas.md).

| Provider / exact research target | Verified dependency or source evidence | Useful first integration proof | What that proof would not establish |
|---|---|---|---|
| **Aether 1.20.1-1.5.2-neoforge** | [Official release](https://modrinth.com/mod/aether/version/2411kUqF) lists Forge and NeoForge. Modrinth Maven `maven.modrinth:YhmgMVyu:2411kUqF`; CurseForge file `6134920`. | Co-load; confirm installed registry IDs; enter Rush from Aether and return safely; verify authored quicksoil/aercloud behavior remains provider-owned. | Every dungeon boss, accessory, mount, or source structure working inside a Rush arena. |
| **Iron's Spells 'n Spellbooks 1.20.1-3.4.0.2** | [Official tagged source](https://github.com/iron431/irons-spells-n-spellbooks/tree/v1.20.1-3.4.0.2), commit `ebd1972277d46be48170467b813d6cd8b04d15b8`. | A narrowly admitted, player-owned native projectile reaches an owned rival through the real provider hit path. | Native spell casting, mana rollback, status effects, summons, teleports, healing, or all spells. |
| **Advent of Ascension 1.20.1-3.7.1 Beta** | [Official release](https://modrinth.com/mod/adventofascension/version/3.7.1); Maven `maven.modrinth:9qn2AQBc:ADsBtG85`. Exact archive inspected as described below. | Co-load; fingerprint the actual installed entity/dimension registry; keep unavailable source IDs dormant. | The legacy dimension roster, every version's mobs, or native skills/guns/staves working in Rush. |
| **Cataclysm 3.31** | Existing repository Wave 2 lane pins CurseForge file `8246047`, Lionfish API 3.0, and Curios. See [Wave 2 ledger](JETSETCRAFT_WAVE_2_EXACT_MOD_COMPATIBILITY.md). | Preserve existing live registry/ownership gates and test an explicitly eligible ordinary rival. | Transporting a native boss, reconstructing its arena, or replacing its multipart/phase logic. |

Other authored ecosystems, including Twilight Forest, Undergarden, Alex's Mobs, Alex's Caves, and The Midnight, should use the same evidence levels. Already-listed candidates are not a blanket permission to spawn every registered entity into every arena. Resolve namespaced IDs once during startup/reload, gate boss/owner/helper cases, and make absent providers cheap dormant data. A room inspired by a dimension may use approved source materials without transporting its original mobs, containers, machines, or inventories.

**Aether source dependency detail.** Its [pinned 1.20.1 source properties](https://github.com/The-Aether-Team/The-Aether/blob/2f0be3a51bae2f434fbf5b5c0aecc56b50f921b7/gradle.properties), [manifest](https://github.com/The-Aether-Team/The-Aether/blob/2f0be3a51bae2f434fbf5b5c0aecc56b50f921b7/src/main/resources/META-INF/mods.toml), and [build](https://github.com/The-Aether-Team/The-Aether/blob/2f0be3a51bae2f434fbf5b5c0aecc56b50f921b7/build.gradle) declare Forge at least 47.1.0 and Curios at least 5.3.1+1.20.1. The build embeds Curios 5.6.1+1.20.1, Nitrogen 1.20.1-1.0.12-neoforge, Cumulus 1.20.1-1.0.1-neoforge, and MixinExtras 0.4.0. Caelus is compile-only in that build, not a mandatory manifest dependency. This is inspected source evidence, not a substitute for resolving the exact release archive in CI.

The same pinned [AetherBlocks registry](https://github.com/The-Aether-Team/The-Aether/blob/2f0be3a51bae2f434fbf5b5c0aecc56b50f921b7/src/main/java/com/aetherteam/aether/block/AetherBlocks.java) registers `aether:holystone_bricks`, `aether:quicksoil_glass`, `aether:quicksoil_glass_pane`, and `aether:aerogel`. These are the source-verified material choices for the optional `cloudline_circuit` arena palette, with vanilla fallback arrays and an Aether presence requirement. Registry confirmation is not a claim that the arena's movement feel has already been client-playtested.

**AoA archive dependency detail.** The official release links [AoA3-1.20.1-3.7.1-all.jar](https://cdn.modrinth.com/data/9qn2AQBc/versions/ADsBtG85/AoA3-1.20.1-3.7.1-all.jar), SHA-256 `f417ecb337d4a3b2aebec559a9262d8a86cc93ee7910f4eea64108bff063193a`. Reading its `META-INF/mods.toml` and `META-INF/jarjar/metadata.json` establishes Forge at least 47.1 and mandatory GeckoLib at least 4.3. Patchouli 1.20.1-83+ and TslatEntityStatus 1.2.1+ are optional. The archive embeds GeckoLib 4.3.1, HSQLDB 2.7.2, SmartBrainLib 1.13, and TslatEffectsLib 1.7. The current upstream `1.20` source branch also contains later work; do not use it as proof of the exact 1.20.1 roster. Resolve the real release in tests. Do not substitute AoA 3.7.15, which targets Minecraft 1.21.1.

**Iron's source dependency detail.** The tagged [properties](https://github.com/iron431/irons-spells-n-spellbooks/blob/v1.20.1-3.4.0.2/gradle.properties) and [build](https://github.com/iron431/irons-spells-n-spellbooks/blob/v1.20.1-3.4.0.2/build.gradle) target Minecraft 1.20.1 / Forge 47.2.0 and pin Caelus 3.1.0+1.20, Curios 5.4.7+1.20.1, GeckoLib 4.4.5, and PlayerAnimator 1.0.2-rc1+1.20. The combined provider lane must resolve one compatible version of each shared dependency rather than blindly adding duplicate embedded libraries. Source build pins and mandatory release ranges are different kinds of evidence.

### 16.2 A narrow native projectile bridge is useful

Iron's tagged [AbstractMagicProjectile](https://github.com/iron431/irons-spells-n-spellbooks/blob/v1.20.1-3.4.0.2/src/main/java/io/redspace/ironsspellbooks/entity/spells/AbstractMagicProjectile.java) extends vanilla `Projectile`. Its [FireboltProjectile](https://github.com/iron431/irons-spells-n-spellbooks/blob/v1.20.1-3.4.0.2/src/main/java/io/redspace/ironsspellbooks/entity/spells/firebolt/FireboltProjectile.java) sets an owner and supplies the direct projectile and causing owner to a spell damage source. These are suitable integration seams without copying provider code.

Alpha.3 implements this narrow bridge. The `rush_spell_relay` GameTest exercises the real registered Firebolt collision and an owned virtual defeat while preserving vanilla health, inventory, XP and provider mana. Both the individual Iron lane and the combined adventure lane passed in [provider matrix #56](https://github.com/Herbertofury/JetSetCraft/actions/runs/41356358302); the [receipt](verification/arena-circuit/README.md) records their provider-present markers. This proves projectile attribution/conversion, not a cast from a player's spellbook.

The admission adapter needs an explicit allowlist and rules: active session, same room, recognized participant owner, bounded lifetime and count, no terrain effect, correct cleanup, and no damage to nonparticipants. Resolve direct/causing entities consistently and reject an unknown owner. A provider identity alone is not sufficient approval. Keep foreign projectiles blocked until their side effects have been evaluated.

Required negative cases are a nonparticipant owner, another active room, an absent provider, a removed owner, repeated hit delivery, leaving during flight, and a stale actor loaded after restart. All must preserve isolation. Record both an individual Iron lane and a combined provider lane before broadening the claim.

### 16.3 Why full spell support needs explicit adapters

The tagged [SpellDamageSource](https://github.com/iron431/irons-spells-n-spellbooks/blob/v1.20.1-3.4.0.2/src/main/java/io/redspace/ironsspellbooks/damage/SpellDamageSource.java), [DamageSources](https://github.com/iron431/irons-spells-n-spellbooks/blob/v1.20.1-3.4.0.2/src/main/java/io/redspace/ironsspellbooks/damage/DamageSources.java), [AoeEntity](https://github.com/iron431/irons-spells-n-spellbooks/blob/v1.20.1-3.4.0.2/src/main/java/io/redspace/ironsspellbooks/entity/spells/AoeEntity.java), and [MagicSummon](https://github.com/iron431/irons-spells-n-spellbooks/blob/v1.20.1-3.4.0.2/src/main/java/io/redspace/ironsspellbooks/entity/mobs/MagicSummon.java) demonstrate distinct attribution, post-hit, area, and summoner semantics. For example, the tagged [PoisonCloud](https://github.com/iron431/irons-spells-n-spellbooks/blob/v1.20.1-3.4.0.2/src/main/java/io/redspace/ironsspellbooks/entity/spells/poison_cloud/PoisonCloud.java) applies a status effect separately from its damage call. Translating damage alone does not translate that effect.

The tagged [MagicData](https://github.com/iron431/irons-spells-n-spellbooks/blob/v1.20.1-3.4.0.2/src/main/java/io/redspace/ironsspellbooks/api/magic/MagicData.java) also tracks mana, casting, cooldowns, and recasts. Its persistence is not a generic exact rollback interface: mana is written as an integer and empty cooldown/recast payloads are not simply a universal reset. Do not clone arbitrary foreign capabilities, replace provider save data, or promise that restoring vanilla inventory restores all spell state.

Future native casting support needs a provider-specific contract for each admitted family: what resource is spent, what state persists, how effects target virtual vitality, which summons are session-owned, and what cleanup does. Healing, shields, teleportation, terrain changes, persistent statuses, and pets each require separate decisions. A mode-native ability inspired by an artifact can be implemented now without pretending to reproduce an entire installed magic mod.

### 16.4 Compatibility proof should be readable

For every shipped claim, record provider version, dependency resolution, test name, server/client coverage, and the narrow behavior observed. Useful acceptance milestones are: absent-provider boot; exact-provider boot; registry resolution; dimension return; representative safe mob; player-owned projectile hit; adversarial ownership cases; and client reconnect/visual playtest. Mark unrun milestones explicitly. This protects the user's central promise: join a session, enjoy an expressive build, leave promptly, and resume the same Minecraft life and provider progression.

The provider workflow retains `alexsmobs`, `alexscaves`, `cataclysm`, and their existing `all` lane. The added selections are `aether`, `irons_spells`, `aoa`, and `adventure_all` (Aether, Iron's, AoA, and Cataclysm together). They resolve shared Curios and GeckoLib declarations once. Iron-containing lanes must print the provider-present Firebolt marker; an absent-provider result cannot satisfy that gate. `ActivityDimensionGameTests` checks loaded versions and exercises native `aether:the_aether` and `aoa3:precasia` departure/return when installed; both IDs were verified in the exact release archives. Historical [matrix #56](https://github.com/Herbertofury/JetSetCraft/actions/runs/41356358302) passed eight lanes with 28 GameTests and dedicated-server startup; its cleanup did not assert normal shutdown, as the [corrected alpha.3 receipt](verification/arena-circuit/README.md) explains. Alpha.4 [matrix #59](https://github.com/Herbertofury/JetSetCraft/actions/runs/41379581891) passed all eight lanes with **35 GameTests and explicit normal dedicated-server save/shutdown**, alongside base #286. The [current receipt](verification/reward-journeys/README.md) records that separate evidence. Future versions and wider interaction families require their own checks.

### 16.5 Alpha.4 keeps movement and vulnerability inside the mode

The alpha.4 abilities do not expand the native-spell allowlist or copy provider player state. Their provider-relevant boundaries are concrete:

- Aether's pinned [AetherPlayerCapability](https://github.com/The-Aether-Team/The-Aether/blob/2f0be3a51bae2f434fbf5b5c0aecc56b50f921b7/src/main/java/com/aetherteam/aether/capability/player/AetherPlayerCapability.java) can deploy a carried parachute during a fast fall, mount the player and damage the item. Keep Rush's temporary inventory boundary and reject native passenger/fall-flight movement as ability input. This is a reason to observe actual qualified JetSetCraft riding, not to replace Aether's own movement behavior.
- Iron's pinned [PoisonCloud](https://github.com/iron431/irons-spells-n-spellbooks/blob/ebd1972277d46be48170467b813d6cd8b04d15b8/src/main/java/io/redspace/ironsspellbooks/entity/spells/poison_cloud/PoisonCloud.java) applies poison separately after its damage call. Its [MagicSummon](https://github.com/iron431/irons-spells-n-spellbooks/blob/ebd1972277d46be48170467b813d6cd8b04d15b8/src/main/java/io/redspace/ironsspellbooks/entity/mobs/MagicSummon.java) removal helper can change the summoner's native timer effect. Neither is a substitute for a session-owned mark or a generic safe helper lifecycle.
- Iron's [DamageSources](https://github.com/iron431/irons-spells-n-spellbooks/blob/ebd1972277d46be48170467b813d6cd8b04d15b8/src/main/java/io/redspace/ironsspellbooks/damage/DamageSources.java) exposes a spell event before native damage resolution. Apply Breakbeat's multiplier once in JetSetCraft's canonical virtual-damage path, not again through a provider event. Preserve the existing explicit Firebolt admission and cleanup contract.
- AoA and Cataclysm continue using the exact archive/registry and ownership boundaries above. A virtual mark does not authorize foreign skill XP, boss phases, multipart rewards, native cooldown manipulation or recreating a source encounter. No new universal compatibility claim follows from these four build mechanics.

These are source-backed design constraints. Alpha.4's [provider matrix #59](https://github.com/Herbertofury/JetSetCraft/actions/runs/41379581891) passed the native-return/projectile regressions and the new build checks within the exact [verified and merged source](verification/reward-journeys/README.md). This records the admitted contracts; it does not expand them to every native spell, status, summon or provider progression system. Alpha.3 remains separately identified historical evidence.

### 16.6 Provider freshness and the new ability families

**Source availability checked 1 November 2026 (America/Denver).** Available upstream releases and JetSetCraft's proven provider pins are separate records. The [official Iron's 1.20.1 files](https://www.curseforge.com/minecraft/mc-mods/irons-spells-n-spellbooks/files/all?page=1&version=1.20.1) list **Forge 3.16.3**, dated 18 August 2026; that does not extend the existing **3.4.0.2** test receipt to the newer release. The [official developer guide](https://iron.wiki/developers/) explicitly documents Forge dependencies for 1.20.1 and below and distinguishes its stable `api` package from internal implementation classes. Inspect and test the exact selected artifact before adding a newer native casting seam; a current documentation example may also describe 1.21 behavior.

The [official AoA 1.20.1 files](https://www.curseforge.com/minecraft/mc-mods/advent-of-ascension-nevermine/files/all?page=1&version=1.20.1) still identify **3.7.1 as Beta**. This project's exact-archive Precasia test is useful evidence for that dimension, not for the complete legacy dimension catalogue. Keep absent registry entries unavailable and preserve the exact source/version distinctions already documented in section 16.1. A provider-themed arena is an authored Rush room; it does not reproduce or transport the source dimension's complete world generation or progression.

The [official Aether 1.20.1 file list](https://www.curseforge.com/minecraft/mc-mods/aether/files/all?page=1&version=1.20.1) labels **1.5.2-neoforge** for Forge as well as NeoForge, consistent with the pinned release above. The [official Cataclysm project](https://www.curseforge.com/minecraft/mc-mods/lendercataclysm) lists **3.31 for Forge 1.20.1**, consistent with the existing lane. Matching a loader/version listing establishes availability; the local admitted palettes, actor behavior and provider matrix establish the narrower JetSetCraft behavior.

Iron's [official progression guide](https://iron.wiki/progression/) describes scrolls, spellbook advancement, an Arcane Anvil, armor upgrades and weapon imbuement. Those are useful future integration families. **Venom ink, Chill paint, Split chorus, Radiant groove, Beat ward and Soul burst are currently original Rush effects**, not wrappers around all those provider systems. Their damage, healing, shield and status state must remain scoped to the run; none authorizes changing another mod's mana, attributes, native health, equipment, progression, or ownership.

For this continuation, verify the new status/charge rules with an absent-provider configuration and the existing pinned provider lanes. Relevant assertions are expired/removed actor rejection, active-clock duration, no secondary-hit recursion, no native effect or health mutation, no stale charge after remix/room cleanup, and an honest unavailable-provider result. Broader native spell, summon, teleport, healing and boss support remains a distinct adapter task. Human movement feel and sustained combined-mod performance still require playtesting after machine acceptance.

