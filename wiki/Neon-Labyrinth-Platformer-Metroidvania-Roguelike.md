# Neon Labyrinth — Platformer × Metroidvania × Roguelike (Working Title)

> **OWNER-APPROVED MODE REQUIREMENT · 2026-11-15 · DESIGN / BACKLOG ONLY. NOT PLAYABLE OR SHIPPED.**
>
> Create a dedicated JetSetCraft mode with **genuinely fun platforming levels, interconnected Metroidvania exploration, roguelike run variation, and outstanding end-of-level and final bosses**. It should feel like its own brilliantly polished action-platformer while preserving the movement, tricks, music, graffiti and unique crew personality of JetSetCraft.

**Neon Labyrinth is a proposed working title, not a final name selected by the owner.** The proposed ID `jetsetcraft:neon_labyrinth` is **not registered in game**. The target is **a distinct selectable mode**, not replacing an existing activity or claiming something is implemented.

**Track implementation:** [Issue #65](https://github.com/Herbertofury/JetSetCraft/issues/65) · [Technical specification](https://github.com/Herbertofury/JetSetCraft/blob/main/docs/modes/NEON_LABYRINTH_DESIGN.md) · [Encounter North Star](Encounter-North-Star-Beyond-Minecraft) · [Current Rival Rush](Rival-Rush) · [Terrain platforming ideas](Terrain‐Aware-Event-Circuits-and-Boss-Arenas)

## Was it already added? Partly — but not as this complete mode

| Current JetSetCraft direction | Confirmed from source | What's missing for this request |
| --- | --- | --- |
| **Rival Rush** | Current development alpha has six-room randomized arena circuits, build rewards, Pit stops, and required headliners in room 3 and 6 | These are primarily combat/arena rooms, not a hand-designed interconnected platforming adventure with traversal unlocks, revisitable map and a proper authored final-boss progression |
| **Terrain-aware platformer/runner proposals** | Runner routes, crate-and-coin platforming, secret paths, Metroidvania-like optional traversal and safe boss arenas appear in the design wiki | A separate integrated platformer/Metroidvania/roguelike mode with its own entry, state, course topology, checkpoints, run powers and ending is not demonstrated as playable |
| **Encounter North Star** | Aiming for breathtaking, original Jet Set Radio/HYPERFUNK-class polish, with actual responsive gameplay and restored worlds | A production mode and native test receipts proving the new adventure experience |

**Core decision:** reuse proven shared services where suitable, but preserve **Rival Rush (Free streets/Arena circuit)** and **Liberty Grind** as separate experiences. This must not be a rename of the six-room Rush circuit or a single timed arena with a boss at its end.

## A real platformer, a real Metroidvania, and a real roguelike

### 1. Platforming should be ridiculously fun

- **Carefully authored levels**, mixed with controlled remixable modules. Make each stage memorable: vertical rail-to-rail transfers, jumps, wall kicks and ledge vaults, grind and air routes, tight landings, quick-turn chases, moving hazards, secret passages, rhythm-aware set pieces, purposeful collectibles and optional mastery lines.
- **Momentum and control come first.** The game needs original JetSetCraft skating and trick physics rather than generic Minecraft sprint-jumping or forced forward movement. Players need legitimate opportunities to learn techniques and improve their line. Stages should not require a traversal action unless that action is truly implemented and verified on the target ride.
- **Fun level pacing**, not identical corridors: exploration and discovery → playful traversal teaching → engaging challenge → satisfying reward → rising tension → headlining spectacle → breathing space. Variety from layout, movement grammar, biome, rivals and music, not shuffled block palettes.
- **Fair checkpoints and retries.** A death or failed jump must show why it occurred, reset event-owned props/collectibles correctly, and support a replay/continue flow that matches the selected difficulty. Do not rely on loss of the player's Minecraft inventory.

### 2. Metroidvania exploration must change the map

- A **coherent network of interconnected districts/rooms**: hubs, vertical loops, optional branches, locked shortcuts, secret areas, spatial landmarks and meaningful connections back to familiar places.
- **Earn traversal capabilities inside the adventure.** Example *proposals* only: a temporary rail-switch tool, an air redirect module, a wall-launch upgrade or a paint-powered gate key. They should introduce genuinely different movement decisions, not just numerical damage bonuses. Validate them against real movement/controller APIs.
- **Backtracking has a purpose:** an early inaccessible route becomes an exciting secret or shortcut after gaining a skill. Mark discovered obstacles, opened doors and unfinished optional routes on a usable map; make the player feel clever on revisiting them.
- **Reachability is proven for each ability state.** Required abilities must be acquired before their mandatory gates; there must be a valid exit and recovery route for every committed room. Optional secrets may be locked; the only path to the finish may **never** be unknowingly impossible.

### 3. Roguelike runs must offer surprising variety

- **Seeded, replayable adventures** choose from *validated authored modules* and legal connection layouts. Enemies, rewards, route choices, some hazards and secrets vary so runs stay fresh while retaining the feel of deliberately made levels.
- **Run-only powers and combinations** affect movement/style/creativity and some encounter choices. Resets are explicit on defeat/completion; permanent progression can unlock routes, modes, characters, gear aesthetics or starting options but should not erase the need to master movement.
- **Choices matter:** safe vs dangerous detours, new traversal power vs a different build, bonus-room reward vs harder boss route, crew assistance vs a solo mastery objective. No manipulative near-miss grind or forced random reward loop.
- **Real finish and replay:** a final boss can be defeated and recorded as a persistent run outcome; optional encore, higher difficulty/alternate routes and postgame secrets must never replace the feeling of an earned ending.

## One illustrative full run — proposed level beats, not approved canon

| Level/segment | Gameplay identity | Exploration/replay reward | Boss |
| --- | --- | --- | --- |
| **Arrival: The Rail Garden** | Expressive beginner-to-intermediate skating, vaults and joyful collectible trails around a recognizable hub | First routes and a visibly locked optional shortcut | No required boss; teaches movement |
| **Vertical District: Crosswire Towers** | Ascending and descending rail/parkour puzzles with alternate momentum lines | Acquire a **proposed run-local traversal key**, then revisit the Rail Garden's shortcut | A small skill-check miniboss (design proposed) |
| **Hidden Loop: Chromatic Caves** | Nonlinear exploration, moving platforms, grind-to-wall transfers, secret side rooms, skill-dependent risk/reward | Open a looping junction, optional upgrade and a new escape route | A **real stage-ending boss** teaching traversal and counterplay |
| **Branch: Skybridge Works / Flooded Venue** | Different course archetypes chosen by seed and available movement support | Optional detours, distinct gang-specific challenges and earned power combinations | Route-specific miniboss or challenge |
| **Final District: Signal Spire** | A culmination of everything learned, visually evolving and musically reactive | A final checkpoint, meaningfully changed environment, completion and replay options | **A cinematic-feeling but fully playable multi-phase FINAL BOSS** |

This sequence is **an example, not a fixed owner-approved stage count, required boss names, release plan or current game content**. Procedural remix is accepted only after the authored vertical slice is genuinely enjoyable.

## Boss quality bar: end bosses must earn the finale

**Never call a high-health normal mob a complete stage boss.** Each major level should build toward a recognizable, original boss with a distinctive silhouette, setup, mechanic, and victory transition.

- **Stage bosses:** teach or test a movement skill from their environment, with changing attack patterns and readable telegraphs, meaningful counterattacks, safe landing zones, dance/trick interactions and a clear defeat state.
- **Final boss:** real multiphase escalation, distinctive arena reconfiguration, memorable original music/crew choreography, skilful routes, reasonable checkpoints, victory/resolution and safe return. No fake playable cinematic or arbitrarily huge HP bar.
- **Variations:** different legal phases/assistants/hazards per seed or difficulty; all must be rehearsable, fair and accessible. Optional modifiers must not invalidate a run's selected traversal keys.
- **Gang crossover:** invited factions can alter boss moves or venue identity while keeping the existing independent gang lore, abilities and persistent defeats intact. Original licensed music only; Spotify must-add references are **not downloadable game assets**.
- **Multiplayer:** boss ownership, phase state, confirmed hits and awards are authoritative and once-only; all players can understand attacks and alternate cues without flash/music.

## Progression contract: no accidental loss of Minecraft

| Lifecycle | Non-negotiable behavior |
| --- | --- |
| **Enter** | Explicit opt-in at the activity interface; preview difficulty, solo/co-op rules, selected seed/variant and the event-only inventory policy. Do not force unknown mods or override other active activities. |
| **Explore** | Visible minimap/world map shows explored rooms, route connections, known locks, unlocked traversal powers, earned shortcuts and checkpoints. No spoilers for undiscovered branches. |
| **Choose** | Decisions and powers are run-scoped; no partial credit for failed rooms or unverified boss victory. |
| **Fail/retry** | Meaningful checkpoint and attempt states, no duplicated cards/XP/collectibles/rewards, and no vanilla inventory/equipment deletion. Exit path always exists. |
| **Win** | Beat the actual final boss; see a unique finale, score/rewards report and proper end credits/replay option where appropriate; persist completion and selected permanent unlocks exactly once. |
| **Return** | Restore original safe location, inventory ownership and modded mounts/entities via proven session recovery; unload/clean only event-owned room content. Reconnect, death, cancellation, restart and resource reload cannot strand or duplicate players. |

**Co-op and accessibility:** solo play must be complete and fun. Proposed co-op should avoid locking physical route geometry to the highest-skilled player or silently skipping the party's traversal goals; spectators/rejoin, configurable graphics, optional camera behavior, reduced flash/motion and mute cues are design acceptance gates.

## First production slice: what should actually be built first

1. **Entry and lifecycle:** a distinct prospective activity-menu selection and isolated run/session state, safely forked from current Rush flows.
2. **One great level:** a short, visually clear, hand-authored, real controller-tested platforming area with a hub, branching routes, checkpoint, secrets and meaningful finish.
3. **One actual traversal upgrade:** a previously visible gated shortcut becomes reachable after earning the upgrade, and backtracking feels fun.
4. **One true multi-phase boss:** attacks tied to movement, fair defeat/retry, unique music/visual identity and actual victory award—no fake HP sponge.
5. **Seed-safe remix:** vary 2–3 verified challenge/secret modules without breaking ability-state reachability; use replay testing.
6. **Complete native-game QA:** first-ever real player run, direct screen recording, stage completion, boss victory, voluntary exit, crash/rejoin and clean vanilla world return, dedicated server, performance and accessibility checks.
7. **Expand only after the slice sings:** more stage families, a real final boss, other gangs and multiple endings/encore paths once the first level is demonstrably fun.

**Separate requirements vs status:** The idea is **owner-approved**. The working title, sample stage ideas, powers, bosses, count and architecture are **proposals pending design/playtest**. **Nothing above is claimed as a current playable game mode.**

### Related project systems

[Rival Rush](Rival-Rush) (existing roguelite circuit) · [Runner and coin platforming](Terrain‐Aware-Event-Circuits-and-Boss-Arenas#runner-and-crate-collecting-platformer-modes) (earlier individual design) · [Encounter North Star](Encounter-North-Star-Beyond-Minecraft) · [Bosses and reactive music](Music‐Driven-Dances-and-Mob‐Themed-Bosses) · [Current release status](Release-Status) · [Issue #65](https://github.com/Herbertofury/JetSetCraft/issues/65).
