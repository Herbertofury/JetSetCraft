# Roadmap

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
