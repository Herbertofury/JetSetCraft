# Rival Rush: implementation status and roguelite design research

**Status: optional alpha candidate with recorded machine verification.** Rival Rush targets `0.4.0-alpha.1`; the stable `v0.3.0` release is unchanged. The last recorded PR verification checkpoint is merge commit [`520591e`](https://github.com/Herbertofury/JetSetCraft/commit/520591e049e38fcd7592679788d97b28cd27d4ba), containing feature commit [`7283dd8`](https://github.com/Herbertofury/JetSetCraft/commit/7283dd8f912c2c6903e156d468fd1326ee8966b7), with the evidence below. Follow [PR #39](https://github.com/Herbertofury/JetSetCraft/pull/39) for the current candidate and any newer checks. Full human thirty-minute sessions, Endless balance, sustained performance, and broad mob-provider behavior remain to be evaluated.

**Research checked 1 November 2026. Eighteen game studies; primary developer, publisher, and official store sources.**

Rival Rush is an optional JetSetCraft activity: a separate skating, paint-combat, and mob-card session with a thirty-minute ending and voluntary Endless continuation. Ordinary Minecraft and the rest of JetSetCraft remain the main game. This document records both the implementation being verified and the larger design direction; those are different scopes.

Source observations are separated from JetSetCraft recommendations. All locally chosen timings, thresholds, and rewards are **unvalidated tuning values**, except that approximately thirty minutes is the user's requested session target. No reference game was installed or played for this research, and linked videos were not treated as personally watched playtests.

## 0. What this change implements, and what remains a proposal

The following inventory describes code present in this alpha. The recorded tests cover the cases stated below; a feature appearing here is not evidence that every lifecycle or optional-mod combination has passed.

| Area | Implemented alpha surface | Further design, not implemented by this slice |
|---|---|---|
| **Entry and return** | An opt-in mode menu and command path; a dedicated `jetsetcraft:rival_rush` dimension; temporary loaned ride/paint equipment; a persisted pre-entry return journal and restoration paths. | A polished in-world event host, authored entry sequence, and verified adapters for arbitrary foreign player capabilities. |
| **Run progression** | Separate run XP, score, vitality, seven ranked stat/reward upgrades, and three repeatable fallback rewards. Server-owned offers carry a run identity and offer version. | The eight interacting build families in section 6; family-aware offer steering, rerolls, evolutions, and run-specific equipment combinations. |
| **Session shape** | Thirty minutes of active server simulation time; offer and continuation decision pauses; a completion prompt with Return or Endless; direct Endless entry is also supported. | The authored minute-by-minute narrative in section 3 and a mandatory named final headliner. The current thirty-minute finish is a timer/decision boundary, not a completed bespoke boss finale. |
| **Combat and crews** | Run-owned source mobs, existing gang identities and physical street gear, skater/tagger/bruiser/captain roles, automatic paint fire plus existing aimed paint interactions, and a delayed positional tagger attack tell. | Unique authored behavior for every crew or species, aquatic districts, multipart boss adapters, and validated behavior across all installed provider mobs. |
| **Mob cards** | Defeated actors create mode-only card pickups. Collection records the registered entity ID **and gang ID as a pair**, grants run XP/score, and advances persistent album progress. Actual mastery milestones are **1 / 5 / 15 / 40 / 100 / 250 / 600**. | Illustrated card frames, foils, species-wide aggregated views, behavior notes, themed cosmetic prizes, and challenge unlocks. |
| **Persistent album benefit** | At **20 / 60 / 150 total collected cards**, subsequent runs receive **one / two / three** queued starting upgrade drafts. Album progress remains outside the temporary return journal. | A broad hub economy or permanent stat tree. No vanilla XP, item loot, or vanilla damage reward is intended. |
| **Movement and music** | Rewards for fresh wall patches, validated moving grind/wall-ride/trick states, and a four-second dance at a nearby generated Boombox. | Tag relays, carried crates, crew murals, beat-judged challenges, and the complete POI catalogue in section 8. |
| **World and Endless** | Generated local wall/rail/ramp/Boombox patterns; five-minute chapter labels with composition changes and a six-chapter rotation; bounded enemy movement speed and counters. | A rich procedural neighborhood graph, distinct authored route forks, boss duets, the full modifier catalogue, and measured long-duration performance guarantees. |

### First-slice upgrade vocabulary

The seven ranked choices are **Paint pressure** (damage), **Quick hands** (firing interval), **Wide spray** (splash radius), **Magnetic mixtape** (pickup reach), **Second wind** (maximum mode vitality), **Easy breathing** (recovery), and **Style dividends** (mode style rewards). After the ranked pool becomes exhausted, **Fresh wheels**, **Victory lap**, and **Overdrive** provide healing, score, or a temporary damage surge. These are a foundation for build design; they do not yet implement tag chaining, rail-triggered pulses, painted trails, or other interaction families proposed below.

The chapter rotation currently changes tagger/bruiser frequency, lowers pressure and increases music-stop benefits in the cypher chapter, and shortens tagger cadence in the crossfire chapter. Its open-street and relay phases use the baseline mixture. These are concrete encounter changes, with further variety still to author and test.

### Compatibility boundary

This alpha refuses entry on Peaceful before saving a return checkpoint or installing loan equipment. Native hostile mobs are removed by Minecraft's Peaceful despawn path even when persistent. The mode does not change the world's difficulty; Easy, Normal and Hard are the supported entry settings for this slice. A future per-mode solution must preserve the ordinary world's setting and be verified across real mob lifecycles.

Selection is based on registered mob identities and existing JetSetCraft eligibility. This is a broad selection path, not a claim that every mob works well in a flat district. The existing `street_gear_incompatible` entity tag explicitly excludes the Ender Dragon and Wither, along with player/armor-stand entities. Respect those pre-existing exclusions: a multipart dragon encounter and a scripted, destructive boss lifecycle require deliberate encounter adapters, and this slice provides neither. Other provider ownership rules, aquatic needs, unusual geometry, native AI, and scripted transformations still require representative integration tests. Do not infer universal compatibility from a successful registry lookup.

### Last recorded verification

The following evidence applies to the PR merge checkpoint [`520591e`](https://github.com/Herbertofury/JetSetCraft/commit/520591e049e38fcd7592679788d97b28cd27d4ba), containing feature commit [`7283dd8`](https://github.com/Herbertofury/JetSetCraft/commit/7283dd8f912c2c6903e156d468fd1326ee8966b7). Later candidates and their own checks are linked from [PR #39](https://github.com/Herbertofury/JetSetCraft/pull/39); this record does not automatically verify later edits.

| Evidence | Recorded result | Scope |
|---|---|---|
| [Verify JetSetCraft #268](https://github.com/Herbertofury/JetSetCraft/actions/runs/41346460726) | **PASS** | Clean Forge build, all **15 real Forge GameTests**, dedicated-server readiness followed by a normal RCON stop, and an actual **1280 × 720 graphical Minecraft client audit**. |
| [Optional-provider matrix](https://github.com/Herbertofury/JetSetCraft/actions/runs/41346460787) | **PASS in all four configurations** | Combined providers, Alex's Mobs, Alex's Caves, and Cataclysm each passed the same **15 GameTests** and dedicated-server smoke. This is integration coverage for those configurations, not an audit of every provider mob's AI. |

The client audit used actual packet entry and exit and captured setup, the district HUD, an empty album, and the return result. Its fixture added **7 diamonds, 5 bread, 23 XP levels, and 17 XP points**. It compared the complete pre-entry inventory data and XP level/total on return, checked XP progress within `0.000001`, required the original dimension, and accepted a return position within one block of the saved position. These are real machine integration and visual-capture checks; the empty-album frame does not demonstrate a populated collection.

The fifteen GameTests consist of the nine existing tests plus six Rival Rush tests covering run rules, album rewards, return journals, actual session entry/exit, actor/card ownership, and graffiti/action dispatch. A separate deterministic harness also compiled the pure-Java `RushRun` class and checked timer, choice, overflow, and Endless rules.

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

These eight families are proposed mechanics, not a claim that the reference games contain these exact effects and not features implemented by this slice. The current seven ranked upgrades are listed in section 0.

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

