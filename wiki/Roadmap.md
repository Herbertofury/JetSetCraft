# Roadmap

## Living pipe-rail area / dimension — requested to-do (2026-11-20)

**Owner vision, not implemented:** a Windows Pipes screensaver-inspired space where rails grow ahead of players as they grind, changing direction and branching unpredictably into a fun combat/roguelite playground. Use the same area system across **any JetSetCraft mode where it genuinely fits**, rather than forcing every mode to become the same game. The supplied visual reference shows glossy green, pink, blue and yellow pipe routes crossing at different heights against a dark void. Capture that playful, readable spatial spectacle using original assets; the screenshot is inspiration, not a bundled asset or evidence of an existing JetSetCraft feature.

- [ ] Audit existing rail/grind movement, procedural route builders, collision, encounter ownership and mode/session lifecycles before creating another system; study reusable existing solutions first.
- [ ] Build a bounded **living-rail vertical slice**: generate connected segments ahead of a grinding rider, with randomized bends, elevation changes, forks, transfers and occasional crossing routes. Preview upcoming changes early enough to react; never remove or redirect an occupied segment underneath a rider. Maintain a reachable baseline route, safe recovery and clear exit.
- [ ] Make route choices meaningful: risky shortcut versus safer line, collectible/resource detour, trick-chain opportunity, optional combat pocket or boss approach. Combine authored challenge modules with seeded variation so runs differ in decisions and encounters, not merely colors or random turns.
- [ ] Integrate enemies and gang encounters with on-rail fighting, jump/transfer counterplay and useful roguelite build rewards. Keep attack telegraphs readable at speed; separate one-time server-confirmed run rewards from vanilla loot/XP farming. Tune pacing so combat enhances skating rather than constantly stopping it.
- [ ] Define a reusable mode adapter: prioritize an Adventure Mode region in its existing dedicated dimension; evaluate a separate pipe-themed dimension only if justified by lifecycle needs. Consider optional Rival Rush/Coliseum combat variants and Liberty/free-skate or time-trial variants where appropriate, preserving each mode's rules, progression, distinct pacing and opt-in entry/return.
- [ ] Stream/recycle unoccupied distant segments with bounded active simulation, pooled geometry and measured frame/tick/memory budgets. Generate on demand from rider progress, not idle background polling. Keep world mutation server-authoritative and budgeted; parallelize only safe planning work. Preserve other worlds, player builds and existing session isolation; no forced chunks.
- [ ] Persist seed, generation version, route decisions, checkpoints and reward receipts for reproducibility and recovery. Test highest-speed travel, sharp turns, crossing collision, missed transfers, occupied-segment retention, backtracking, slow clients, simultaneous riders, reconnect/save/reload, death, cancellation and original-world return.
- [ ] Provide route cues beyond color alone, camera/motion comfort options and reduced-effects settings. Prove fun and readability through actual native-client playtests, plus dedicated-server correctness and long-run performance evidence. Do not mark this complete from a screenshot, static rail maze, design document or generator-only test.

**Integration dependencies remain open:** [Adventure dimension architecture](https://github.com/Herbertofury/JetSetCraft/blob/main/docs/modes/ADVENTURE_DIMENSION_ARCHITECTURE.md), [Adventure implementation checklist #65](https://github.com/Herbertofury/JetSetCraft/issues/65), and the existing run-unique discoveries/resource progression goals below. This addition records a playable-area goal, not an implementation or release claim.

## Newly requested martial-arts skating gang — name pending (2026-11-15)

The owner wants a new high-energy **Chinese wuxia-inspired / Japanese shinobi-ninja-inspired** martial-arts skating gang (cultural/art direction still to choose), with the exact song [**“USAIN” — Moreart & Asylllum**](https://open.spotify.com/track/5lViQePieL6gzg9ajLBUUN?si=6431d65f32ad47e2) as its **first confirmed must-add**. The proposed name **Wuxistas** and alternatives **Shinobistas, Shaolistas, Shadowlotus, Jade Strikers** are **not yet owner-approved**. This is an original fictional crew and a new soundtrack assignment, **not a shipped NPC group** or proof of playable/licensed music.

[New crew concept and naming shortlist](Martial-Arts-Crew-Naming-Shortlist) · [Owner-selection metadata](https://github.com/Herbertofury/JetSetCraft/blob/main/docs/gangs/unnamed_martial_arts_crew.json).

- [x] Preserve new crew concept and first owner-selected Spotify recording separately from all existing gangs.
- [ ] Get owner confirmation of the final name and choose an internally stable gang ID.
- [ ] Research the specific chosen Chinese/Japanese-inspired art/movement tradition rather than conflating them; author original character rigging, graffiti, skating, on-foot dance and signature challenges.
- [ ] Verify exact recording/master, content rating and synchronization/redistribution permissions; implement gang-specific music routing and actual boss/choreography timing.
- [ ] Native-game and dedicated-server QA for realistic movement, fair action, safe source-world state and premium encounter quality.

## Owner-approved Adventure Mode: a separate platformer dimension + Metroidvania + roguelike (2026-11-15)

**A separate JetSetCraft adventure mode is required:** truly fun skating/platforming levels, meaningful exploration and backtracking after unlocking traversal techniques, variable roguelike runs and unforgettable end-of-level and final bosses. **Adventure Mode** is the owner's confirmed name; **Neon Labyrinth** was the prior working codename. This is distinct from Rival Rush's existing **six-stop combat Arena circuit with room-three and room-six headliners**, and from earlier runner/coin-platformer mode proposals. **Not implemented or verified playable.**

**[Canonical Adventure Mode + dimension architecture](Adventure-Mode)** · [Full level/boss design](Neon-Labyrinth-Platformer-Metroidvania-Roguelike) · **[Technical design](https://github.com/Herbertofury/JetSetCraft/blob/main/docs/modes/NEON_LABYRINTH_DESIGN.md)** · **[Implementation checklist and tracking #65](https://github.com/Herbertofury/JetSetCraft/issues/65)**.

- [x] Add dedicated **`jetsetcraft:adventure_mode` dimension/type resource definitions**, typed identity helper and two **unloaded authored area blueprints** (Rail Garden / Crosswire Towers); no verified runtime entry or playable map yet.
- [ ] Implement Adventure-only region/session allocation, map generation, safe entry/return and crash/rejoin recovery without touching Overworld/Nether/End/provider worlds.
- [ ] Add separate, clearly labelled optional game-mode entry and safe return; preserve Rival Rush, Liberty Grind and ordinary Minecraft state.
- [ ] Make one extraordinary hand-authored 3D platforming vertical slice, not random block jumps; prove movement, checkpoint, collectibles, secrets and replay are fun in a native client.
- [ ] Implement real connected Metroidvania regions, acquired traversal abilities, an initially visible gate, useful backtracking and a graph-level guarantee that no run gets stuck.
- [ ] Add distinct level-end bosses with legible multi-phase mechanics and a full final-boss climax; victory must be server-confirmed and earn a real event result.
- [ ] Add deterministic procedural variation and run-scoped build choices **without** losing authored level design, permanent legitimate crew progression or original-world safety.
- [ ] Test full run, loss/retry, accessibility, solo/multiplayer, optional providers absent, real frame/network performance and crash/rejoin on the exact client and dedicated server before claiming "playable."

## Ultimate owner-approved encounter vision — beyond the feeling of Minecraft (2026-11-15)

**Highest-level experiential target:** make major JetSetCraft events so outrageously beautiful, kinetic, musical, fun, responsive and replayable that the player feels transported into an original premium **Jet Set Radio / HYPERFUNK-class** action-and-street-culture game. The **goal is to surpass those reference feelings through original work**, not just sprinkle effects over a Minecraft minigame. This is **aspiration, not shipped quality**. Minecraft remains physically compatible and intact before/after the event.

**Canonical experience specification:** [Encounter North Star — Beyond Minecraft](Encounter-North-Star-Beyond-Minecraft) · [Evidence-gated QA scorecard](https://github.com/Herbertofury/JetSetCraft/blob/main/docs/ENCOUNTER_NORTH_STAR_ACCEPTANCE.md).

- [ ] Build one genuinely playable, deeply polished signature vertical slice with responsive skating, original crew body animation, satisfying score, camera consent, meaningful challenge and fair replay.
- [ ] Create a fully reversible live-world-to-event staging system with original graffiti, lighting, signage and biome/terrain-aware routes. Confirm cleanup, protection and no mod/world-state loss.
- [ ] Integrate soundtrack-reactive choreography, hit/trick/Flow musical payoffs, crew responses and legal event-scoped music routing. Never slow input or claim a Spotify reference is shipped audio.
- [ ] Create multiple radically different authentic gang encounters with unique choreography, identity, mechanics and adaptive challenge; avoid generic reskinning or repeating choreographies.
- [ ] Prove first-run awe, in-hand controls, readability, replay impulse, performance and accessibility using actual blind outside playtests, footage, timing traces and hardware/server profiles.
- [ ] Achieve competitive evidence against reference game experiences while preserving world compatibility, all approved rides, vanilla/modded mobs, network correctness and multi-version direction.
- [ ] Only graduate from **design/experimental** to **shipped AAA-level claim** after recorded native client, dedicated server, asset-rights and regression evidence demonstrates the outcome.

## Required version priority and rapid-port architecture

**1.20.1 → 1.21.1 → latest stable Minecraft → eventually all Minecraft versions.** This is the owner-approved order. The latest stable target must be resolved and pinned when its port begins. Broader coverage is a long-term goal, not an existing support claim.

- [ ] Preserve and finish the Forge 1.20.1 baseline first.
- [ ] Build a Stonecutter-style shared-code/version-adapter workflow and prove the 1.21.1 port next.
- [ ] Reuse core gameplay, assets and behavioral tests; pin target-specific loaders, mappings, Java/build toolchains and optional integrations.
- [ ] Add the latest stable release, then widen the tested matrix without silent feature/quality loss or performance regressions.

Canonical design and implementation checklist: [Multi-version architecture](Developer-Architecture#owner-approved-multi-version-architecture).


## Latest approved rival roster: six crews

The owner selected **[Krumpaz](Krumpaz)** for krump and **[Footworkas](Footworkas)** for Chicago footwork. They join Gabberina's, Cumbias, Phonkistas and Hardtekkas; this six-crew roster supersedes earlier four-rival expansion counts without deleting prior identities or requirements. [Implementation and music-audition backlog #33](https://github.com/Herbertofury/JetSetCraft/issues/33).

The new pages contain six Krumpaz and seven Footworkas researched listening candidates. Current owner-selection and review statuses are maintained on each faction page; reference approval is distinct from production licensing and implemented playback. Shared control/power removal, independent defeats, surprise arbitration, choreography, music/rave, customization and safe-course systems must support all six. Final rosters and new unlock thresholds remain open; no implementation claimed.


JetSetCraft `0.3.0` is the completed Forge 1.20.1 release surface documented in the README and changelog. Everything below is possible post-v0.3 direction, not a promise that the feature already exists.


## Owner-approved expansion: terrain-aware circuits and flagship night events

Full researched architecture and primary sources: [Terrain-Aware Event Circuits and Boss Arenas](Terrain‐Aware-Event-Circuits-and-Boss-Arenas). Actionable implementation and test backlog: [issue #32](https://github.com/Herbertofury/JetSetCraft/issues/32). This is research/design, not implemented gameplay.

- [ ] Build non-destructive live-world course overlays fitted to real biome/terrain for gang races, tag-area battles, platforms, tricks and branching traversal. Validate actual movement and multi-racer fairness; no source-region replacement/rollback.
- [ ] Prototype real temporary collision separately from visuals, safe boss-room teleport/return, new opt-in underwater skating and complete cancellation/unload/crash recovery. Preserve source mobs, normal vanilla behavior outside events, no forced chunks and current safety budgets.
- [ ] Deliver the flagship nighttime quality goal: as fun and goosebump-inducing as the best rave ever on Earth, with coherent music-synced lights/lasers and player/rival combo-reactive crew spectacle; reduced-flash/readability and complete transient cleanup are required.
- [ ] Integrate stylish title/artist reveals, in-game radio, capability-scoped authorized Spotify support and reputation-unlocked mob music/dance choices through the [shared music specification](Music‐Driven-Dances-and-Mob‐Themed-Bosses) and [issue #30](https://github.com/Herbertofury/JetSetCraft/issues/30). Final API capabilities and new perk thresholds need qualification.
- [ ] Preserve the extendable [Hardtekkas track register](Hardtekkas#owner-music-reference-register) and its per-entry review status.

## Major requirement: music-driven dances and themed bosses

High-priority owner requirement: every mob needs amazing music-generated/driven dances with meaningful, fun variety and goosebump-level presentation. Cumbias, Phonkistas, Gabberina's and Hardtekkas each need exceptional signature dances. When a major faction owns a mob gang, its boss becomes a music-driven, mob-themed human leader tied to the controller; the explicit example is **Hakken Horse**, a human dressed like a horse for a Gabberina-controlled horse gang. Preserve source mobs and existing takeover/power-removal, surprise-challenge and independent-defeat rules.

**Shared choreography scope:** players and enemies can emote solo, have teammates join synchronized crew routines, and go gang-versus-gang head-to-head both while skating and while dancing on foot. Preserve music-generated variety, player agency, safe moving formations, multiplayer timing and the no-content/no-population-cap performance direction. Join/leave timing, formations and interruption details are proposed design criteria in the linked canonical specification. Gabberina's explicit music/dance blend is jumpstyle, gabber and hardtekk; both newly selected songs are preserved in its reference register.

See the [canonical requirement and extendable reference register](https://github.com/Herbertofury/JetSetCraft/wiki/Music%E2%80%90Driven-Dances-and-Mob%E2%80%90Themed-Bosses) and [shared high-priority issue #30](https://github.com/Herbertofury/JetSetCraft/issues/30). Generation technology, detailed timing/rig criteria and final boss roster remain design work. This is a required quality target and backlog, not an implementation claim.

- [ ] Build the shared player/enemy solo-to-crew and rival head-to-head choreography system for skating and on-foot dance; verify voluntary joining/leaving, continuity, formations and multiplayer behavior.
- [ ] Deliver meaningful music-generated dance variety for every mob and exceptional Cumbias, Phonkistas, Gabberina and Hardtekkas signature performances.
- [ ] Build ownership-aware mob-themed human bosses, beginning with Gabberina-controlled horses / Hakken Horse, while preserving the source mobs.
- [ ] Review the growing video/song register, including the owner-selected Danny Ores Gabberina track; define generation, rig adaptation and synchronization before implementation.
- [ ] Verify campaign invariants, native single/multiplayer quality, accessibility, rights provenance and performance/startup with no regressions or arbitrary coverage caps.

## Major requirement: reactive fights, trick chains and combo payoffs

**Owner goal:** Killer Instinct-level music integration for fights and chained tricks, including the excitement of the “ULTRAAAAA COMBO” example and music responding to hits. Build JetSetCraft's own reactive musical identity and memorable original announcer-style payoffs. This is a quality/inspiration target, not a claim about Killer Instinct's implementation or a shipped JetSetCraft feature.

Full [reactive-music design and acceptance backlog](Music‐Driven-Dances-and-Mob‐Themed-Bosses#major-requirement-reactive-fight-trick-chain-and-combo-music) is tracked in [shared issue #30](https://github.com/Herbertofury/JetSetCraft/issues/30).

- [ ] Prototype immediate confirmed-hit/trick/landing feedback plus coherent phrase-aware musical escalation, combo peaks, breaks and recovery. Never delay controls, hit registration or physics to quantize music.
- [ ] Reconcile existing Flow/combat/trick scoring, faction sonic identity, solo/crew choreography, multiplayer authority and significant musical timing.
- [ ] Create original/commissioned or appropriately licensed musical layers and announcer identity; no copied Killer Instinct recordings or voices.
- [ ] Verify clean mixing and event arbitration, separate music/effects/announcer controls, muted visual equivalents, interruption cleanup and native single/multiplayer playtests.
- [ ] Profile audio, event/network cost, memory, startup and loading without capability loss, arbitrary content/participation caps or regressions; attach exact-build evidence before completion claims.

## Owner-approved fourth rival: Hardtekkas

**Hardtekkas** is the separate hardtekk rival alongside Gabberina's, Cumbias and Phonkistas. **Tekkistas** is the owner-approved optional name at the appropriate reputation/good-standing level. Use existing configured Friendly → Crew Naming Rights, stable faction identity, personal aliases by default in multiplayer, authorized shared aliases and canonical reset. Numeric thresholds and any extra unlock/rank-loss semantics remain unresolved. Full [Hardtekkas design, all three music references and acceptance checklist](Hardtekkas) · [Issue #31](https://github.com/Herbertofury/JetSetCraft/issues/31).

- [ ] Resolve reputation/naming UX and permissions without replacing the faction, resetting progress or reviving defeated rivals.
- [ ] Review Drucktherapie, Wake Up and Break It Down; develop original Hardtekkas art/music, exceptional music-driven choreography, signature challenges and ownership-aware mob-themed human bosses.
- [ ] Extend control/power removal, surprise-event arbitration and independent persistent defeats to all four rivals; integrate shared solo-to-crew/head-to-head and reactive combo music through #30.
- [ ] Verify native gameplay, saves, multiplayer, accessibility, rights and performance/startup without feature loss or arbitrary caps. Preserve source mobs and existing temporary-event safety.

Gabberina's retains its jumpstyle/gabber/hardtekk blend and Hardtekk Tutorial reference. This update is design/backlog only.

## Possible later work

1. Expand live modpack compatibility coverage across more creature models, head providers, dimensions, combat systems, and rail ecosystems.
2. Add bounded territory, reputation, aliases, relationships, and adopted hangouts on top of stable `gang_id` data.
3. Add complete scored challenge modes—Turf War, timed lines, races, dance battles, graffiti contests, and Tag—using the existing bounded actor lifecycle.
4. Extend presentation with articulated equipment parts, more anatomy-specific choreography, richer Boombox visuals, and opt-in full-length resource-pack music.
5. Consider newer Minecraft versions or additional loaders only after behavior and evidence can be carried forward without feature reduction.

## Permanent guardrails

- Preserve legitimate momentum and ordinary Minecraft mechanics.
- Keep optional adventure, combat, head, model, and rail mods non-required.
- Keep ordinary ride animation off weapon arms/hands/head.
- Never replace source mobs to create gang members.
- Never guess ambiguous player-head identity from texture or display-name heuristics.
- Keep event casts bounded, chunk-safe, temporary, and ineligible for loot/XP/Street-Gear farming.
- Require a fresh JAR, validators, real Forge GameTests, dedicated-server readiness, and exact artifact identity for every release claim.

## Requested design: Gabberina's and gang-control campaign

**To-do / design only; not shipped.** Full specification and acceptance checklist: [Gabberina's and Gang War Campaign](https://github.com/Herbertofury/JetSetCraft/wiki/Gabberinas%E2%80%90and%E2%80%90Gang%E2%80%90War%E2%80%90Campaign) · [Issue #27](https://github.com/Herbertofury/JetSetCraft/issues/27).

- [ ] Create an elite human female gabber skate/dance crew with spectacular outfits, seamless dancing while skating, and boss-level fair challenge.
- [ ] Blend a wild mix of mob-inspired abilities into distinct crew kits, coordinated combos, readable telegraphs and counterplay; preserve provider-owned mobs and safe fallbacks.
- [ ] Build player-versus-Gabberina gang capture, defense, recapture and counterattack progression through a persistent final defeat.
- [ ] Give every mob gang goosebump-worthy entrances, music intros and challenges; Gabberina's is the flagship treatment.
- [ ] Complete original/licensed art/audio, accessibility, native single/multiplayer tests, save persistence and performance/startup profiling without capability loss or regressions.

- [ ] Make full player takeover of a Gabberina-controlled mob gang remove its corresponding ability from their arsenal, visibly breaking related combos; verify ownership transitions, recapture rules, save/reload and multiplayer synchronization.

- [ ] Add intense surprise Gabberina appearances and follow-on challenges after a normal mob event is defeated/completed when they retain some control of that gang. Preserve the original event result/rewards; test current influence, stripped abilities, pacing, duplicate/recursive triggers and persistent final defeat.

## Requested design: the Cumbias and multi-rival gang war

**Design/backlog, not shipped.** Add the Cumbias as another major competing gang alongside Gabberina's, with a distinctive Mexican cumbia music, dance/skate and visual identity. [Full Cumbias design, owner references and checklist](https://github.com/Herbertofury/JetSetCraft/wiki/Cumbias%E2%80%90and%E2%80%90Multi%E2%80%90Rival%E2%80%90Gang%E2%80%90War).

- [ ] Research owner music references and finalize original crew identity, choreography, spectacular entrances and challenging cumbia events.
- [ ] Extend shared gang-control, borrowed-ability removal and surprise post-event challenges to a coherent multi-rival campaign; finalize influence sharing, independent defeat states and competing-trigger arbitration.
- [ ] Preserve earned event rewards, existing capabilities, provider-owned mobs, persistence, accessibility and performance/startup quality; attach native evidence before marking implementation complete.

## Requested design: Phonkistas

Owner-selected name for the phonk competing gang. [Full design and to-do list](https://github.com/Herbertofury/JetSetCraft/wiki/Phonkistas) · [Issue #29](https://github.com/Herbertofury/JetSetCraft/issues/29). Design/backlog only.

- [ ] Develop a distinctive phonk crew, skate/dance challenges and goosebump-worthy entrance/music/finale from the owner reference.
- [ ] Integrate with Gabberina/Cumbias multi-rival control, borrowed-power removal, surprise challenges and independent persistent defeat; finalize contested influence and event arbitration.
- [ ] Verify native gameplay, persistence, accessibility, licensing and performance/startup without regressions before release claims.

## Major approved goal: Trick Trains and flagship Create integration

[Full train design and installed-Denshattack research handoff](Trick-Trains-and-Create) · [Implementation checklist #37](https://github.com/Herbertofury/JetSetCraft/issues/37). Approved development goal; not shipped.

- [ ] Deliver reversible Trick Train activation for vanilla minecarts and full player-built Create trains, then tested adapters for other train mods.
- [ ] Build satisfying Denshattack-inspired whole-train handling, tricks, customizable skins/liveries and crew/mob train battles, with measured installed-game comparisons for future Codex/Claude work.
- [ ] Integrate music, biome-safe temporary courses, train-to-ride/wingsuit transitions, progression and multiplayer.
- [ ] Preserve actual construction, inventories, passengers, identity, schedules and railway behavior across events, failure and reload; prove performance and compatibility before release.


## Roaming creature reward POIs — approved goal, not implemented (2026-11-19)

Add optional moving points of interest on large creatures: airborne birds and eventually colossal roaming giants. Reach an attached, clearly marked object by platforming/climbing, then **hit that object to collect a substantial in-game XP/points reward**. The intended feel mixes playful traversal with roguelite run decisions, especially Megabonk-like experiences; Shadow of the Colossus is a scale/climbing inspiration, not permission to copy characters, art or code. This records a requested goal, not verified capabilities of either reference game.

- [ ] Reconcile existing movement, moving-platform collision, creature rigs, reward ledgers and run lifecycle before implementation; research reusable existing solutions first.
- [ ] Prototype one optional slow ground giant with a marked reward object, two approach routes, safe exit and seed-varied route placement. Reuse suitable authorized assets/data for variation without treating reuse as a ceiling on quality or new features.
- [ ] Make boarding, movement-relative footing, climbing/jumping, hit registration and payoff legible and fun; provide reachable alternatives for differing mobility/builds and accessibility needs. Missing an optional encounter must not block a run.
- [ ] Define meaningful high rewards balanced against time, traversal difficulty and combat risk. Commit one reward per stable run/creature/object identity; repeated hits, reconnect, reload and overlapping collection must not duplicate it.
- [ ] Test missed jumps, safe recovery, camera/readability, creature bounds/despawn, pause/death/restart, simultaneous collection where multiplayer exists, and low/high mobility. Preserve saves, unrelated world/player state and prior progression.
- [ ] Record exact-build performance and actual player enjoyment/playtests; expand to flying creatures and larger multi-stage giants only after the bounded first slice works. Do not mark the goal complete from a mockup, menu, static giant or simulated reward alone.

**JetSetCraft scope:** prioritize Adventure Mode's own custom dimension and platformer/Metroidvania/roguelike traversal. Preserve skating/grind/trick identity, validated traversal unlocks and original-world return safety. Evaluate Rival Rush separately for fit rather than silently imposing the same system on every mode. Event rewards must be server-confirmed, run-scoped and non-farmable; this does not relax the no-vanilla-loot/XP farming guardrail, replace provider mobs or force chunks. Existing Adventure Mode implementation gaps remain open.


## Long-horizon progression, resources and themed bases — goal only (2026-11-19)

**Owner direction, not implemented or measured playtime:** collect spendable resources such as gold, make between-run upgrades and home/castle building meaningful, and treat roughly the first 100 hours as the beginning rather than the end. Later progression should reveal genuinely new abilities, characters, maps, enemy families and bosses. Support multiple themed bases with resource storage and an eventually scalable open-world mode. Challenge and satisfying new decisions must continue; procedural reshuffling and inflated statistics alone do not meet this goal. The 100-hour figure expresses ambition, not a mandatory wait before fun features or a promise of existing content.

**Reference evidence:** the [official Night Swarm Steam page](https://store.steampowered.com/app/3668370/Night_Swarm/), checked 2026-11-19, describes a castle hub with NPC talent/item shops, gold exchanged at totems, companions and large bosses. This is publisher description, not hands-on verification. It does **not** establish the requested multi-base construction/storage, open world or 100-hour-plus depth; those are the owner's proposals.

- [ ] Audit existing resource/currency, persistence, crafting, base/home and unlock systems; reuse compatible code and authorized assets without reducing quality or preventing genuinely new mechanics.
- [ ] Define distinct run resources and retained resources, explicit earn/spend rules, visible costs and upgrade effects, refunds/respec where suitable, and fair death/abandonment handling. Make one end-to-end earn → return → spend → visibly improved next-run loop playable first.
- [ ] Design home/castle construction as meaningful choices and usable facilities, not a decorative menu or a wall of percentage bonuses. Define ownership, building costs, resource storage and clear progression purpose.
- [ ] Plan staged discoveries with new verbs/build synergies, characters, authored map features, enemy behaviors and boss mechanics. Preserve early accessibility and optional paths; do not gate basic enjoyment behind 100 hours of grind.
- [ ] Extend to multiple themed bases with distinct functions, safe travel/return and explicit storage/transfer rules. Test transaction interruption, concurrent actions, duplication, capacity/overflow, migration and backup/recovery without losing items or existing saves.
- [ ] Prototype a scalable open-world mode separately, with streaming/persistence, encounter scheduling, bounded active simulation and measured frame/memory/save budgets. Preserve distinct run-based modes; do not claim an infinite world or unlimited performance from procedural generation.
- [ ] Prove sustained challenge, meaningful upgrade tradeoffs, late-game novelty and player enjoyment through exact-build playtests across early/mid/late progression. Use accelerated progression tests for correctness but not as proof of 100 hours of authored enjoyment. Keep all milestones open until implemented and accepted.

**JetSetCraft adaptation:** plan Adventure Mode progression and themed crew/home facilities around skating, tricks, movement unlocks and its dedicated custom dimension. Preserve Rival Rush/Liberty as distinct modes; any wider open-world extension needs explicit mode-specific design, source-world protection and safe return. Do not replace provider mobs, mutate player builds or bypass existing event anti-farming/server-authority rules.


## Run-unique world discoveries and faction warzones — open goals (2026-11-19)

**Owner direction, not implemented:** make each run richly explorable, with meaningful discoveries rather than empty procedural repetition. As players gain movement capabilities and travel faster or farther, reveal new biomes, distinct enemies, caves, homes, companions and unexpected bosses. Include faction warzones where enemies fight each other and the player can choose to enter. Support satisfying **30-, 60-, 90-minute and endless** experiences without deleting previously requested duration choices. Seed changes or higher enemy statistics alone do not satisfy this goal.

- [ ] Audit existing world generation, authored encounters, movement unlocks, faction AI, run lifecycle and save systems before adding parallel systems. Reuse authorized assets creatively while adding genuinely different interactions, routes and decisions.
- [ ] Define biome identities through traversal, hazards, enemy behavior, resources and discoverable stories, not palette swaps alone. Connect increased speed/range to reachable discoveries, with clear route clues and alternatives for different builds; avoid long empty travel or making low-mobility builds nonviable.
- [ ] Build a first playable discovery chain spanning contrasting regions: an explorable cave or home, a companion opportunity, and a surprising but readable optional boss. Give discoveries consequences, useful choices and safe return/exit rather than empty scenery or unavoidable ambushes.
- [ ] Add faction-versus-faction encounters with readable allegiances, distinct tactics and meaningful player choices to observe, intervene or leave. Specify targeting, aggro transfer, victory consequences, rewards and persistence; prevent offscreen combat, repeated entry or faction kills from duplicating player rewards.
- [ ] Design separate pacing arcs for 30/60/90-minute and endless runs: early discoveries, intermediate changes, later biome/encounter combinations and satisfying conclusions or voluntary exits. Do not simply stretch the same wave schedule. Preserve earlier duration options and portal side-run return continuity where applicable.
- [ ] Combine authored encounter families with seed-aware placement, recent-discovery tracking and variation that changes gameplay. Avoid promises of literally limitless unique content; endless mode needs meaningful ongoing choices and transparent repeat behavior.
- [ ] Test unreachable layouts, high-speed streaming, cave/home entry and return, encounter overlap, faction targeting, companion persistence, unexpected-boss fairness, save/reload and duplicate rewards. Measure frame time, memory and active simulation at long distances and long run durations.
- [ ] Playtest actual short/medium/long/endless builds for discovery density, repeated-content fatigue, travel downtime, risk/reward and enjoyment. Automated seed sweeps or accelerated timers prove correctness only, not satisfying hours of gameplay. Keep implementation and acceptance milestones open until evidenced.

**JetSetCraft adaptation:** Prioritize Adventure's dedicated dimension and skating/grind/trick exploration. Reconcile existing multi-gang control, independent defeats, surprise challenges and Coliseum opt-in fights rather than introducing contradictory faction rules; Rival Rush and Liberty retain distinct pacing. Preserve source mobs, player builds, safe original-world return, server authority and no vanilla loot/XP farming or forced chunks.
