# Crew House: Construction, Members and Missions

**Status: `0.4.0-alpha.3` shared-progression candidate; new verification pending.** The crew-house foundation introduced in alpha.2 has recorded machine verification; those historical checks are preserved below and do not verify later changes. This document separates the runtime candidate from future design proposals. It also records eight focused primary-source references checked on 1 November 2026. Source observations are not claims of hands-on playtesting, and proposed mechanics are not implemented features.

## House foundation and alpha.3 progression candidate

The current runtime has a deliberately smaller scope than the inhabited-house vision below:

- One owner has a persistent world-saved chapter/crew house, founded around an existing placed Boombox with a reusable Charter. The player constructs the house manually.
- An expandable **soft plot** defines where the house recognizes its registered facilities. It neither protects Minecraft land nor creates a structure, replaces blocks or keeps chunks loaded. This is an explicit player-founded feature; natural hangouts retain their separate non-invasive rules.
- **Workshop, training, studio and recovery** facilities use registered, physically placed vanilla blocks. Registering and upgrading facilities require those physical blocks plus a saved **Crew Supplies** balance. Plot expansion spends supplies to enlarge the recognized footprint and roster allowance; the player still constructs the extension.
- Named roster profiles persist, gain levels and have a training focus. The first slice has a small starting roster and additional recruiting. **It does not spawn visible residents, skating followers or a physical posse.** A profile is not evidence that a mob AI has been implemented or tested.
- Dispatch assignments use the overworld's game time, require no remote entities or loaded mission chunks, and have explicit claimable results. Time stops when the server stops; on a running multiplayer server it may advance while an individual player is offline. Ready rewards do not expire, decay or auto-repeat.
- Claimed missions and completed Tag, King of the Hill and Showdown objectives provide non-Rush routes to Crew Supplies, crew/member progress and canonical gang reputation. Arena clears and qualifying Rush returns feed the same account through distinct server-owned receipts. Home-gang reputation grants bounded recruitment discounts without gating expansion; unloading a legacy Boombox is not a completed objective.
- Activity earnings can be saved before the first house. Founding transfers supplies, crew XP, reputation and recent receipt IDs alongside the **24 starting Supplies**; pending training goes to **Riff, the initially focused starter member**. Pre-house reputation can already qualify a new home for recruitment discounts. Later activity training goes only to the selected idle member; a mission trains its assigned member when claimed.
- The Rush connection prepares **one selected starting perk**. It does not turn every house investment into another stacking permanent combat bonus or award vanilla experience.

Exact costs, timers, capacities and interaction steps are recorded in the [chapter wiki reference](../wiki/Player-Chapters-Posse-and-Mob-Archetypes.md); the [shared reward table](../wiki/Rival-Rush.md#shared-completion-rewards) records the activity amounts and Rush-return formula. Settlement retains the most recent 128 receipt IDs, and founding moves those IDs with the pending account. Persistence follows normal world saves. The earlier chapter design's approximately five physical residents is a future design example, not the starting roster contract for this slice. Music assets and permissions are separate work; this document does not claim that a soundtrack is bundled.

### Recorded alpha.2 evidence

[Verify JetSetCraft #274](https://github.com/Herbertofury/JetSetCraft/actions/runs/41350812751) passed the clean Forge build, **19 real GameTests**, dedicated-server readiness and normal RCON shutdown, and an actual **1280 × 720 graphical client audit with eight captures**. [All four optional-provider configurations](https://github.com/Herbertofury/JetSetCraft/actions/runs/41350812645) passed the same 19 tests and server smoke. The [crew-house verification receipt](verification/crew-house/README.md) records the tested source, binary and captures; the [alpha.1 receipt](verification/rival-rush/README.md) remains a separate historical record.

The real client used the Charter, founded a home, displayed two roster profiles, registered an operational placed workshop and dispatched a timed job. The captured job was still running; persisted deadline/claim boundaries and duplicate rejection are covered by the Forge tests. This is machine integration evidence. Human progression balance, thirty-minute/Endless enjoyment, physical residents and musical house activities are not established by these checks.

The following sections propose the longer-term direction. They should guide later additions without making the first slice appear to contain physical NPC life, playable dispatch missions, musical minigames or deep personal bonds already.

## 1. Recommended direction

Make the gang house a place the player builds and enjoys returning to. Its facilities should change what the player and their crew can do. A menu can explain and assign work, but buying an invisible upgrade should not substitute for constructing the workshop, practice line, rest area or stage.

The desired loop is:

**Found a chapter → build useful spaces → invite and develop members → choose a crew job → ride, build or create → bring back chapter resources and memories → improve or expand the house → unlock another way to play.**

Rival Rush is one possible outing in this loop. House ownership, member development, normal riding, graffiti and social play must remain achievable without a Rush run. House investments can unlock optional Rush preparation choices, but should not make an undeveloped house a handicap that must be fixed before the first run is enjoyable.

The strongest design decision is to make each upgrade visible: another resident uses a space the player built; a member practices a learned trick; a wall records a completed mural; a new room opens a new activity. The result should look and behave differently, not merely add a percentage in a menu.

## 2. Fit the work into JetSetCraft's existing plans

Read together:

- `docs/HANGOUTS_TERRITORY_REPUTATION.md`
- `docs/GANG_WARS_BOOMBOX_MOB_ATLAS_MASTER_SPEC.md`, especially reputation and player-chapter sections
- `wiki/Hangouts-Territory-and-Reputation.md`
- `wiki/Player-Chapters-Posse-and-Mob-Archetypes.md`
- `docs/RIVAL_RUSH_DESIGN.md`

Those documents establish the longer-term identities and compatibility contract: a Chapter Boombox/home anchor, stable chapter/site and gang identity, persistent member identity independent of a runtime entity UUID, moving home without duplication, recoverable members, optional providers, bounded active-posse budgets and no chunk tickets. They are not proof that physical residents or the complete archetype resolver have shipped. The crew-house runtime should extend the same concept rather than create a competing faction or disposable roster.

The existing reputation policy also matters: one completed encounter is one reputation transaction for its gang, not one reward for each spawned mob. Canonical reputation remains per player and `gang_id`. Personal member bonds can be small permanent-member memories and unlock conditions; they should not become competing global faction meters. The long-term policy supports befriending every gang, reversible rivalry and reconciliation.

The existing [player-chapter wiki page](../wiki/Player-Chapters-Posse-and-Mob-Archetypes.md) remains the feature's documentation home. It must distinguish verified roster/facility/mission behavior from proposed physical residents, followers and provider recovery. The [Rush design](RIVAL_RUSH_DESIGN.md) stays focused on its optional activity and links outward for house progression.

## 3. Eight references that answer specific design questions

| Reference and primary source | What the source establishes | JetSetCraft proposal | What to avoid copying |
|---|---|---|---|
| **Dragon Quest Builders 2** — [Square Enix product page](https://na.store.square-enix-games.com/dragon-quest-builders-2) | Player-built structures, new recipes/blueprints, cooperative construction, and villagers working with the player to cultivate crops. | Reward a house the player physically constructs and makes usable. Have residents visibly contribute to its activities. | Reducing construction to purchasing a prebuilt house from a screen. Exact room-recognition rules are not established by this source. |
| **V Rising** — [Level Infinite castle-design guide](https://www.levelinfinite.com/news/grand-designs-constructing-the-perfect-castle-in-v-rising/) | Enclosed rooms and appropriate floors affect workstations; castle expansion creates room for stations; servants have job-relevant stats and can be dispatched for resources. | Give facilities readable physical requirements and match members to assignments. Let a workshop's layout and crew affect its available projects. | Carrying over a survival upkeep obligation, coercive recruitment theme, or passive resource factory. The source is a dated 2023 guide, not current numerical balance documentation. |
| **Cult of the Lamb** — [official store description](https://store.steampowered.com/app/1313140/Cult_of_the_Lamb/) and [Sins of the Flesh announcement](https://steamcommunity.com/games/1313140/announcements/detail/6915906760354008792) | The base has structures, follower traits/quests and progression; the official update adds a player-performed Drum Circle whose performance affects a follower-related reward. | Make member personality and social facilities playable. A stage, workshop or mural wall should host a small activity with the crew. | Mandatory hunger/sickness chores, repeated check-ins or copying its punishment tone. A rhythm minigame is a separate designed activity, not automatic analysis of any soundtrack. |
| **Metal Gear Solid V: The Phantom Pain** — [Konami's official manual](https://mgstpp-app.konamionline.com/manual/pc/na/en/pc_07.html) | Adding staff, resources and facilities expands Mother Base functionality; personnel suit different units; dispatch missions use formations and return results after time, with potential losses. | Let recruiting a distinctive member and building the right station open a new mission approach. Show how the selected crew changes the job. | Reproducing the wait-to-collect loop, compulsory full assignment queues or permanent member loss. The manual explicitly says dispatched units are not directly controlled; our optional playable mission approach is an original proposal. |
| **Ni no Kuni II: Revenant Kingdom** — [Level-5's official manual](https://ssl.level5.co.jp/game/ninokuni2/pc/manual/en/kingdom01.html) | Facilities occupy plots, can be upgraded, use assigned citizens and enable research; different expertise unlocks particular research/materials. Its coffers and stores fill over time and stop accumulating at capacity. | Connect facility function, member skill and a visible research project. Explain the missing prerequisite directly: person, place or completed activity. | Full storage that pressures frequent logins, collection chores, or general economy multiplication. JetSetCraft construction rewards should remain in its own progression system. |
| **Against the Storm** — [Eremite's housing-upgrade design note](https://eremitegames.com/upgraded-living-update/) and [specialization/UI update](https://eremitegames.com/ats-quality-of-life-update-2/) | The developer added selected housing upgrades to make a static system more interactive, with alternate improvements and visible building changes; specialization information is shown in building/species UI. | Let a residence or facility upgrade choose an identity, such as communal practice versus quiet recovery. Preview the functional change and show eligible roles in the world. | Upgrading every decorative block, a giant blueprint pool, or species-locked jobs that make a beloved member useless. The housing article is a historical 2023 developer design record, not a claim about current numerical balance. |
| **Darkest Dungeon** — [Red Hook's game description](https://www.darkestdungeon.com/darkest-dungeon/about/) and [official store page](https://store.steampowered.com/app/262060/Darkest_Dungeon/) | Named classes/quirks, training, risky expeditions and town recovery make individual heroes matter across outings. | Give a member a recognizable specialty, a memorable incident and a useful recovery activity. Rotate crews because combinations are interesting. | Permanent loss, punitive exhaustion or forced waiting. JetSetCraft's existing recoverable roster policy should remain authoritative. |
| **Hades** — [Supergiant's Welcome to Hell design/update notes](https://www.supergiantgames.com/blog/hades-welcome-to-hell-update-patch-notes/) | House renovations, character bonds, keepsakes, narrative events and a list of goals link the return home to future outings. | Let a short return visit reveal a new member interaction, a shared keepsake or a visible house improvement. Give the player one meaningful next project. | Long compulsory dialogue queues or buying identical filler gifts to grind affection. This is a historical 2019 design record, not a claim about current patch balance. |

The recommendations in the last two columns are inferences for this mod. These eight are selected for complementary mechanics, not presented as a universal ranking of base-building games.

## 4. A house that grows through construction

### The anchor and the plot

The Chapter Boombox remains the home anchor. A plot is the area where the chapter recognizes its facilities and resident spaces. It does not reserve terrain against other Minecraft systems or silently build over blocks. Expanding it adds functional building area; the player still places the building, furniture and course pieces.

Use an inspectable outline and a clear project preview. The first slice uses tiered footprints. A later extension could support connected sections or deliberate annexes, allowing a courtyard or long practice lane without requiring one prescribed house shape. Keep feature authorization tied to the chapter owner; ordinary building protection remains the server's responsibility. A larger plot does not load its chunks.

### Capacity follows a useful house

The first slice connects roster allowance to purchased plot expansions and gives physically constructed facilities their own upgrade requirements. When real residents exist, add visibly useful resting/gathering spaces so expansion means more than a larger number. A member might use a bed corner, perch, pool or shaded nook; habitat must follow the real creature rather than assume that every mob sleeps in a human bed.

Do not make a missing bed or damaged wall erase a character. If a future habitat check fails, the member can remain safely unassigned or pending until space is restored. Stored roster capacity and fully active bodies remain different budgets: retain server-controlled active caps and cheap loaded idle behavior. This is future resident design, not a hidden extra rule for the current profile-only roster.

### Validate the function, allow the style

A facility should check a few legible conditions: its placed controller/prop, useful clearance, access, declared work area and any real species requirement. Give an in-world explanation such as “needs a clear landing zone” or “one rest space is obstructed.” Do not demand a single official aesthetic, exact decorative blocks, constant whole-base scans or invisible building scores. Recheck on relevant changes and interaction, with bounded cached local work.

The player chooses where facilities go. A facility upgrade should change its model/state or allow an additional purpose; the mod must not replace an entire hand-built room with a prefab.

## 5. Facilities with something fun to do

All interactive activities below are proposed extensions. The current workshop, training, studio and recovery registrations establish a simpler construction/progression foundation; a registered block does not already perform every activity in this catalogue.

| Facility | What the player builds or places | Playable purpose | Progression payoff |
|---|---|---|---|
| **Crew Board** | A placed board with a readable gathering spot | Brief a small crew, choose a contract and compare approaches | Opens assignments and records completed neighborhood jobs |
| **Rest Corner** | Suitable rest spaces with clear access; species variants may use pools or perches | Welcome a member, assign their space and display a personal keepsake | Makes later resident capacity visible and usable; recovery never deletes the roster |
| **Gear Garage** | Workbench, display rack and a small testing lane | Tune a member's role loadout, then complete a short test ride together | Role choices, crew-specific cosmetics and optional Rush sidegrade blueprints |
| **Practice Line** | A player-made ramp/rail/landing route registered from a start prop | Teach or learn a signature move through a short follow-the-line challenge | Member skill XP and route-specific support abilities |
| **Tag Studio** | A player-selected wall and paint station | Create a crew mural in stages; members contribute their unlocked marks | Blueprint progress, member bond events and mural presentation options |
| **Cypher Stage** | Clear dance area, speakers and a placed stage control | Call-and-response dance, short rhythm patterns or cooperative pose chains | Member bonds, original performance variations and stage presentation upgrades |
| **Dispatch Corner** | Map table and a departure/return marker | Select the crew and approach, then act on the lead they send back | Chapter supplies, new local contracts and expansion projects |
| **Common Room** | Seating or species-appropriate gathering space | A brief celebration, story exchange or recovery challenge after an outing | Persistent shared memories and optional crew combinations |
| **Archive Wall** | Album display or trophy board | Review collected mob/crew cards, memorable runs and a member's history | Target a future hunt or commission; display rewards stay optional |
| **Habitat Annex** | A deliberately built pool, shaded nook, broad doorway or perch route | Make a new creature genuinely comfortable and able to use the house | Broadens usable residents through verified habitat support, not a generic all-mobs checkbox |

A future movable facility should preserve its identity and unlocks. Repeated placement, breaking or moving must never generate supplies. Real construction uses the player's chosen Minecraft materials; mode rewards do not turn into free vanilla stone, iron, diamonds, food or enchantment XP.

## 6. Members: skill, personality and relationship

Keep **crew reputation**, **individual skill** and **personal bonds** distinct.

- **Gang reputation** answers whether a gang trusts the player and which chapter privileges it offers. Resolve it once per meaningful event, with the existing reconciliation/naming policy.
- **Member skill** answers what this permanent character has learned. The alpha.3 candidate awards progress through claimed missions, completed Tag/KOTH/Showdown objectives, arena clears and qualifying Rush returns. Activity training uses the selected idle member; mission XP uses its assigned member. Later add teaching and concrete shared accomplishments; repeatedly opening a menu or moving a facility must not become a training exploit.
- **Bond memories** answer what the player and member have done together. Use a few memorable unlock conditions instead of another infinite affection bar or a daily chore list.

A small role roster can support many creature types:

| Role | Distinct contribution | Example skill milestone |
|---|---|---|
| **Courier** | Maintains a delivery chain or brings a dropped mission parcel back into reach | Learns a handoff move after completing a relay with the player |
| **Writer** | Reveals a useful mural target or adds a crew mark after the player earns it | Learns a second pattern through the Tag Studio |
| **Scout** | Reveals route options and identifies habitat/clearance problems | Offers a safer alternate approach on a surveyed course |
| **Mechanic** | Supports mission-owned course devices and prepares mode gear | Unlocks a different support attachment after a garage test |
| **Soundcaller** | Coordinates a visible musical cue, crew entrance or group move | Learns a new call-and-response sequence |
| **Mentor** | Helps another member gain a useful first specialty | Teaches a junior member through a completed practice session |

Roles should remain trainable choices, with gentle affinities rather than mandatory species jobs. A favorite cow should not become obsolete because a rarer mob has a larger hidden stat total. A rare member can offer another personality, route preference or presentation, while ordinary members remain useful.

An illustrative five-level arc is: **rookie → chosen specialty → signature action → team interaction → veteran teaching option**. These labels and pacing are hypotheses. Prefer a new behavior at a milestone to five indistinguishable percentage increases. Respecialization should be possible through a short activity, without erasing the character.

Small bond events can make the same roster feel personal: a member asks for a spot on the mural; another remembers a clean relay rescue; two members create a shared victory pose; a veteran offers to teach someone who struggled. These can be bounded flags associated with stable member IDs, not unlimited per-tick histories. Identity and achievements survive provider removal in the existing dormant/missing-provider state.

## 7. Missions worth sending a crew on

### Use a briefing, a field lead and a debrief

A dispatched crew should create a decision the player cares about. A mission card shows the objective, selected members, role interactions, promised reward, possible complication and whether the player can join an active segment. A clear guaranteed baseline reward is more legible than opaque success percentages that merely punish a new roster.

The agreed first slice uses short, server-game-time dispatch assignments. These let the player keep building or playing ordinary Minecraft while a roster job is in progress. They are not simulated mobs walking across unloaded terrain, and they do not currently contain playable mission segments. Claims should show how a role and level contributed, with a bounded bonus and a useful baseline.

A strong later addition is an **optional active resolution**: complete a short survey, delivery, mural or friendly challenge to resolve an assignment through play. Another experiment is a dispatch that advances through a few meaningful completed activities. Neither should count every enemy kill, idle minute, menu opening or repeated repaint as progress. These alternatives should add agency without making the quiet building route feel punitive.

No real-world deadline is needed. There is no missed login, storage collection tax, streak or penalty for leaving. A ready report remains ready. If recall is added, explain its partial-progress result before confirmation. Dispatch should not make an eventual household feel empty: use small teams and leave useful activities at home. The current system represents the job as saved roster state and requires no follower pathfinding or mission chunk loading.

### Concrete mission briefs

| Mission | Active player experience | Member/house connection | Reward direction |
|---|---|---|---|
| **Next Block Survey** | Skate to several authored viewpoints and select an accessible approach | Scout identifies a good expansion edge; player still builds there | Chapter expansion project and supplies |
| **Neon Courier** | Deliver a mission-owned parcel across a short route with optional handoffs | Courier learns a relay action; garage offers loadout choices | Supplies and courier training |
| **Community Mural** | Reach and tag different authorized sections of a mural | Writer contributes a learned mark; Tag Studio records the finished work | Blueprint, crew reputation and visible mural history |
| **Find Our Sound** | Visit a few themed landmarks and complete a brief performance prompt | Soundcaller brings back a new cue or stage arrangement | Music presentation options and bond progress; audio sourcing remains a separate rights task |
| **Friendly Face-off** | Complete a small trick, dance or tag contest with another crew | Mentor/host offers a cooperative variation | One balanced reputation transaction and a social unlock |
| **Lights for the Block Party** | Route between mission-owned beacons, solving a simple order/position problem | Mechanic prepares the festival devices; player completes the course | Facility project and a startable house celebration |
| **Bring Our Friend Home** | Follow a short trail and finish a recovery/escort activity | A recovering member remains a persistent character | Recovery and a shared memory; no permanent-loss roulette |
| **Crew Showcase** | Choose either a short ordinary gang challenge or an explicit Rush contract | Showcase unlocks a garage/stage sidegrade through either route | Comparable house progress, keeping Rush optional |

Initial active missions might target **three to eight minutes**, with at least one useful result in a short session. This needs playtesting. Avoid making every mission a reskinned kill quota; delivery, painting, route planning, building validation, performance and a friendly contest should ask for different actions.

Failure should produce a readable partial result or an optional follow-up, not erase a named member. A bad route choice can cost a bonus or create a rescue lead. It must not steal normal Minecraft inventory or create an offline household crisis.

## 8. An economy that stays inside the feature

Use one common chapter construction resource initially, such as **Crew Supplies**, plus distinct earned blueprints. Reputation, member skill, bond memories, the existing card album and Rush XP are progression records, not five interchangeable shop currencies.

Supply awards come from explicit completed activities with stable transaction IDs. They are a saved ledger balance or bound feature resource, not normal mob drops. Mission success should not manufacture vanilla minerals, survival food, trade goods or XP orbs. Native creatures outside the feature retain their ordinary behavior and loot.

Useful sinks are plot sections, facility tiers, role specializations, cosmetic commissions and optional Rush preparation research. Costs and expected progress are shown before the job. No endless rent, spoilage, repair tax or housekeeping demand is needed to keep a player returning.

The agreed Rush bridge is **one selected starting perk**, bounded separately from the existing card album's starting-draft rewards. Later garage research can offer alternative preparations rather than an unlimited stack of stronger starts. A stage might instead unlock an optional musical challenge or presentation. Keep the album's existing 20/60/150 milestone ladder legible; do not silently turn house upgrades into another unbounded source of free drafts. No house reward should accidentally increase ordinary Minecraft damage, maximum health, enchanting XP or item yield.

Every essential house tier and member role needs a non-Rush route of comparable effort. A player who loves building and dancing should not have to endure thirty-minute combat sessions to house another member; a Rush player should still need to place and use the relevant house facility rather than buy an invisible settlement from the run menu.

## 9. Music and celebrations as playable house life

The stage is the natural place to make musical interaction deeper. Three original prototypes are worth testing:

1. **Call-and-response cypher:** a member demonstrates a short visible sequence; the player answers with a dance, turn or trick. Provide an equally readable visual cue and an adjustable timing window.
2. **Mural jam:** completing different parts of a wall adds declared musical layers or presentation changes during that activity. The art and soundtrack state become a record of collaboration, without editing an external streaming service.
3. **Crew entrance rehearsal:** choose which members lead, choose a route through the house/practice line, and perform an entrance that can later play before a voluntary event.

A player starts these activities. They do not occur on a real-world schedule. Use explicit beat/cue metadata where rhythm is part of gameplay; do not claim that an arbitrary song or a streaming integration has exposed usable beat data. Keep audio-provider permissions, Spotify capabilities and rights decisions with the separate music work. Silent/visual play and untimed participation should still advance essential house progression.

## 10. Extend the first slice through a visible result

Once the agreed roster/facility/dispatch loop is verified, test one addition from beginning to end rather than introducing every facility at once:

1. A member asks for a signature mark or a usable practice space.
2. The player registers a studio wall or builds a short practice line in the existing house plot.
3. A brief optional activity gives that member a concrete milestone.
4. The house records the result visibly, such as a mural layer, personal keepsake or course marker.
5. The next assignment acknowledges that specific member's contribution.

This can make the current profile roster feel personal before a complete physical-NPC system exists. A menu portrait, persistent name and visible achievement display can carry an early interaction honestly; do not present a nonexistent companion as following the player.

Later prioritize physical resident reliability and one species-appropriate activity before a large posse catalogue. Add stronger bonds, music games, authored active missions and habitat annexes only when their individual loops justify their complexity.

### Acceptance questions that matter

- Can a player build a personally designed house and understand why each facility works?
- Does a house upgrade clearly support another roster member or a useful new option?
- Can the same core progression be completed without Rush?
- Can the player identify how a training focus or crew assignment changes an outcome?
- When physical residents arrive, will a favorite ordinary mob remain useful after rare recruits appear?
- Can the player leave for a week without decay, missed rewards or punishment?
- Does a save/reload or moved Chapter Boombox preserve one roster and one reward transaction?
- Before physical provider-backed residents ship, does provider removal preserve character history without generating fake replacements?
- Are normal items, vanilla XP, unrelated mobs and unloaded chunks unaffected?

The next design test should be a short session ending with **one newly useful space and one newly memorable member**. If that moment does not feel rewarding, more facilities and currencies will not fix it.
