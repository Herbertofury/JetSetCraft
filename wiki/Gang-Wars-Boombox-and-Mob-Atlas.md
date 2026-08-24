# JetSetCraft — Gang Wars, Boombox, Mob Atlas & Universal Gangification Master Specification

**Project:** JetSetCraft  
**Primary target:** Minecraft Java Edition 1.20.1 / Forge  
**Document role:** Canonical development specification, Pro Chat execution prompt, and GitHub Wiki source for the gang/minigame/mob-compatibility expansion  
**Status:** Approved direction — extend the existing project without replacing working systems

---

## Implemented runtime checkpoint — August 23, 2026

The first production Gang Wars runtime is now implemented on the Forge 1.20.1 line instead of remaining specification-only:

- a craftable/placeable physical **JetSetCraft Boombox**;
- a visible one-item **Gang Target** slot that accepts safe mob-head mappings;
- provider-agnostic target resolution through `HeadGangTargetResolver`;
- a central stable `GangRegistry` containing the approved vanilla adult gang atlas;
- original-source `EntityType` gang actors equipped through the existing persistent Street Gear system;
- reversible natural gangification: equip Street Gear → persistent gang state, remove Street Gear → original mob with JetSetCraft gang state removed;
- cinematic staggered event arrivals with species-aware ride rigs;
- session actor caps, loaded-terrain spawn safety, hard expiry, no forced chunks, and UUID tracking rather than global entity scans;
- event-cast anti-farm rules: ephemeral summoned actors provide no normal loot or experience and are removed when the event is cancelled/expires;
- no arbitrary real-world/player cooldown — a cancelled/completed Boombox can be used again immediately;
- per-gang registered music paths with valid Vorbis placeholders ready for owner-authored tracks;
- comparator states for automation: `0` idle, `7` tuned, `15` active.

The physical Boombox renders its inserted target above the unit. Empty-hand use starts/stops the tuned session; sneak + empty-hand removes the target. Breaking the block returns the physical target.

The current Forge GameTest suite keeps its six-test acceptance contract while extending the Street Gear test to prove the real placed Boombox, Zombie Head → Dead Beat targeting, session start/cancel/restart, physical target recovery, and persistent gangification/de-gangification. See [Testing and Verification](Testing-and-Verification) and `docs/BOOMBOX_GANG_RUNTIME.md`.

The remaining sections are the broader approved design and expansion contract. Features described below that are not in the implemented-runtime list remain design targets rather than silently being represented as finished.

---

## 0. Mission

Continue development of **JetSetCraft** from the current project state. Do not restart the mod, replace working subsystems, regress existing movement, or reduce compatibility to make this expansion easier.

This expansion should make Minecraft feel as though a living underground street-sports culture has emerged inside the existing world. Vanilla creatures, modded creatures, dimensions, structures, rails, terrain, graffiti, music, movement, tricks, dance, rivalry, reputation, and multiplayer should all participate in one coherent system.

The defining fantasy is simple:

> **Minecraft owns the mob. JetSetCraft gives it skates, attitude, graffiti, music, rivalries, tricks, reputation, and a reason to throw down when the Boombox starts playing.**

A Bee remains a Bee. A Creeper remains a Creeper. An Aether creature remains an Aether creature. JetSetCraft never needs to replace those entities to make them part of its world.

The system is built around five connected pillars:

1. **The Boombox** — the universal challenge initializer, gang tuner, music source, and street-jam centerpiece.
2. **Universal Gangification** — reversible JetSetCraft equipment/state layered over untouched vanilla and mod-owned mobs.
3. **Gang Reputation & Allegiance** — every crew can become friend, rival, enemy, or family through gameplay; players may ultimately join every gang.
4. **Street Competition Framework** — Turf War, graffiti, races, trick battles, tag, dance battles, spray combat, and other Jet Set-style contests.
5. **The Gang Atlas** — complete adult, junior/baby, and installed-mod creature registries that make the system understandable, collectible, expandable, and easy to customize.

The result should be replayable enough to become one of JetSetCraft's defining pillars rather than a side activity.

---

# 1. Non-Negotiable Architecture Rules

## 1.1 Never replace vanilla or another mod's mobs

JetSetCraft must **not** registry-override vanilla mobs, replace modded entity types, delete a creature and spawn a JetSetCraft copy in its place, or create a duplicate subclass for every supported species.

Ownership stays clean:

- Minecraft owns `minecraft:*` entities.
- The Aether owns `aether:*` entities.
- Twilight Forest owns `twilightforest:*` entities.
- Every other mod owns its own namespace/entities.
- JetSetCraft owns only its optional street-culture layer.

This is a foundational compatibility rule, not a temporary implementation shortcut.

## 1.2 Gangification is reversible augmentation

Conceptually:

**Original Mob + JetSetCraft Gang Equipment/State = Gang Form**

and:

**Remove JetSetCraft Gang Equipment/State = Original Mob**

Use the same entity instance wherever technically possible. Preserve:

- UUID
- health
- custom name
- tame/owner state
- age
- variant/genetics
- inventory
- original equipment
- persistence flags
- mod capabilities/data attachments
- mod-specific NBT
- original navigation and AI state where safe
- relationships established by the owning mod

JetSetCraft gang state should be attached through clean Forge-era mechanisms such as capabilities, events, saved data, render layers, tags, registries, and narrowly scoped compatibility adapters.

## 1.3 Gang Skates are the explicit transformation trigger

A compatible mob becomes a JetSetCraft gang member by receiving its Gang Skates or equivalent JetSetCraft gang equipment/state.

While equipped, the creature may gain:

- gang membership
- skate visuals
- skating locomotion
- grinding and transfer ability
- tricks
- graffiti/spray behavior
- competition AI
- gang role
- reputation/faction participation
- gang clothing/accessories
- challenge behavior
- cinematic entrance behavior
- music/beat metadata association

Remove that state/equipment and JetSetCraft relinquishes control cleanly.

Do not require a vanilla armor slot. JetSetCraft needs its own lightweight gang-equipment attachment so spiders, bees, slimes, quadrupeds, modded creatures, and other unusual models can participate.

## 1.4 Original AI remains the base personality

Outside a JetSetCraft event, the creature should remain recognizable as itself.

A gangified Bee should still feel like a Bee. A gangified Enderman should still feel like an Enderman. A gangified Aether creature should retain whatever makes that creature unique.

During a competition, JetSetCraft may temporarily take higher-level movement/objective control for skating, racing, painting, dancing, trick lines, spray combat, and entrances. When the event ends, it yields cleanly to the original behavior.

## 1.5 Stable IDs, mutable names

**Never use the visible gang name as the save identity.**

Each crew must have a stable namespaced `gang_id`, for example:

- `jetsetcraft:creepaku_gouji`
- `jetsetcraft:bone_drones`
- `jetsetcraft:arachnaphobia`
- `jetsetcraft:dead_beat`

Everything durable keys from that ID:

- reputation
- membership
- rewards
- leader state
- music
- gang relationships
- Boombox targeting
- achievements
- Atlas discovery
- server data
- datapack overrides

The display name is metadata and can be changed safely.

All approved names in this document are **canonical defaults**, not hardcoded strings scattered through Java. Put them in data/translation definitions so they can be changed later without code surgery.

---

# 2. The Boombox — Universal Street-Jam Initializer

The **JetSetCraft Boombox** replaces generic challenge beacons or abstract menu-only starters.

It should become a flagship object for the mod: recognizable, useful, musical, physical, and fun even before a challenge begins.

## 2.1 Core behavior

The Boombox is:

- placeable
- portable
- multiplayer-safe
- visually animated
- a challenge initializer
- a gang selector
- a music source
- a dance/rhythm anchor
- a progression display surface
- customizable with stickers/colors/cosmetics
- capable of holding a mob head/emblem/token

Activating an empty Boombox starts a challenge against a context-appropriate random eligible gang.

Selection may consider:

- current dimension
- biome
- local structures
- available skating space
- terrain topology
- time/weather
- installed mods
- player progression
- discovered gangs
- gang reputation
- current alliances/rivalries
- enabled configuration
- nearby mob populations

There should be **no arbitrary real-world waiting timer** that blocks the player from using the core feature. Balance repeated challenges through gameplay, rewards, difficulty, resources, variety, and progression instead of boring cooldowns.

## 2.2 Mob Head / Gang Target slot

The Boombox includes a dedicated visible **Gang Target** slot.

Insert a mob head, skull, emblem, or JetSetCraft creature token to call out that creature's gang.

Examples:

| Target item | Gang |
|---|---|
| Creeper Head | Creepaku Gouji |
| Skeleton Skull | The Bone Drones |
| Zombie Head | Dead Beat |
| Spider gang emblem/token | Arachnaphobia |
| Witch gang emblem/token | Hex Appeal |
| Piglin gang emblem/token | Gold Rush |

For creatures without obtainable vanilla heads, JetSetCraft can provide an emblem/token/head-equivalent without invasively changing the source mob's normal loot table.

The target mapping must be data-driven and support:

- vanilla skulls/heads
- compatible modded head items when safely detectable
- JetSetCraft emblems/tokens
- datapack mappings
- compatibility-pack mappings
- server/modpack overrides

The simple player-facing rule is:

> **Put the creature's head or emblem into the Boombox → tune the Boombox to that creature's gang.**

## 2.3 Physical presentation

Do not make the Boombox a generic chest with a menu attached.

Give it physical personality through features such as:

- animated speaker cones
- bass vibration
- reactive equalizer bars
- emissive LEDs
- gang-color lighting
- visible target head/emblem
- cassette/CD/media animation
- spinning knobs or wheels
- record-scratch transitions
- earned gang stickers
- graffiti decals
- music-reactive particles
- dance interactions around it
- physical buttons/controls where practical

When a target is inserted, the Boombox should visibly retune to the gang: emblem, colors, equalizer theme, title card, and music identity all change.

---

# 3. Gang Music & Beat Infrastructure

Every curated gang receives a distinct musical identity.

The owner will create the final music later. Build the system now so those final tracks can be dropped in with minimal or no code changes.

## 3.1 Challenge sequence

A polished challenge should be able to flow like this:

1. Player activates the Boombox.
2. Boombox powers up.
3. Existing ambient music ducks/crossfades gracefully.
4. Gang colors/emblem animate onto the Boombox.
5. Gang title presentation appears.
6. Gang theme begins.
7. Opponents arrive cinematically, ideally on musical phrases/beats.
8. Challenge rules are introduced quickly.
9. Gameplay begins without excessive control stealing.
10. Music can react to score, final seconds, victory, defeat, sudden death, or leader phases.

## 3.2 Placeholder audio assets

Create resource paths and valid silent audio stubs where the game requires actual audio files. **Do not create zero-byte or malformed OGG files.**

Suggested path:

`assets/jetsetcraft/sounds/music/gangs/`

Initial track slots:

- `creepaku_gouji.ogg`
- `bone_drones.ogg`
- `arachnaphobia.ogg`
- `dead_beat.ogg`
- `hex_appeal.ogg`
- `dead_water.ogg`
- `goo_groove.ogg`
- `burnout_brigade.ogg`
- `night_shift.ogg`
- `gold_rush.ogg`

Use metadata such as:

- display title
- source resource location
- BPM when known
- downbeat offset
- time signature
- loop start/end
- intensity layers
- entrance cue
- victory cue
- defeat cue
- junior/baby mix resource

Until final songs exist, use titles like **“Creepaku Gouji — Placeholder Mix”** rather than broken resources.

## 3.3 Beat data is gameplay data

The music subsystem must be useful to:

- dance battles
- rhythm prompts
- synchronized gang entrances
- trick-on-beat bonuses
- synchronized group animation
- victory poses
- Boombox equalizers
- music-reactive graffiti/particles

Do not tightly couple gameplay timing to decoding raw audio every tick. Store/derive explicit beat metadata so multiplayer can synchronize it reliably.

---

# 4. Data-Driven Gang Definition Framework

A gang is a data definition, not a hardcoded mob class.

A gang definition should support at least:

- stable `gang_id`
- canonical/default display name key
- player/world alias support
- source mob IDs and/or entity tags
- supported baby/junior profile
- gang colors
- emblem/logo resource
- graffiti style
- clothing/accessory profile
- skate/board/BMX/hoverboard/scooter preferences
- role definitions
- personality
- base disposition
- AI profile
- difficulty bands
- entrance profile
- victory/defeat presentation
- music profile
- dance style
- signature tricks
- preferred minigames
- reputation rules
- membership ranks
- allies
- rivals
- biome affinity
- dimension affinity
- structure affinity
- natural encounter rarity
- reward pool
- leader/champion profile
- Boombox targeting tokens
- compatible mod IDs/entity IDs
- render attachment adapter
- movement adapter
- optional compatibility requirements

The architecture should be usable by:

- JetSetCraft itself
- datapacks
- modpacks
- servers
- compatibility modules
- future API consumers

Avoid giant chains such as `if (entity instanceof Zombie) ... else if ...`.

---

# 5. Gang Names: Approved Defaults, Easy Overrides, In-Game Renaming

All names listed as approved below are canonical JetSetCraft defaults.

They still must be **easy to change later**.

## 5.1 Developer/modpack customization

Names should be resolved through data/translation keys, for example:

- `gang.jetsetcraft.creepaku_gouji.name`
- `gang.jetsetcraft.creepaku_gouji.short_name`
- `gang.jetsetcraft.creepaku_gouji.junior_name`

A datapack/server pack/configured compatibility definition can override the display name without changing `gang_id`.

## 5.2 Player unlock: Crew Naming Rights

Once a player reaches the configured **Friendly** reputation threshold with a gang, unlock **Crew Naming Rights** in the Gang Atlas.

The player can:

- rename the gang in-game
- restore the canonical name
- view the original/canonical name
- optionally set a short tag/abbreviation

Renaming must never break:

- saves
- reputation
- achievements
- Boombox targeting
- music
- rewards
- relationships
- datapack mappings

### Singleplayer behavior

The alias can naturally act as the world's visible gang name.

### Multiplayer behavior

Prevent griefing by separating identity from presentation:

- **Personal alias mode (default):** each player may rename a Friendly gang for their own UI/Atlas presentation.
- **Shared world alias mode (server option):** authorized players/members can vote or obtain permission to set the server-visible crew name.
- Server operators can lock canonical names, permit aliases, or reset a shared alias.

The approved defaults remain discoverable so a renamed gang is never impossible to identify.

---

# 6. Reputation, Friendship, Rivalry & Joining Every Gang

The faction system should be dynamic, reversible, and built around JetSetCraft activities rather than generic RPG grinding.

## 6.1 Initial disposition follows the source creature

Use the original mob's nature as the default starting point.

### Friendly/passive creature gangs

Begin friendly or welcoming.

They may:

- invite players to competitions
- teach tricks
- offer friendly jobs
- request help
- let the player build reputation naturally
- offer membership

The player can still turn them hostile through betrayal, attacks, sabotage, or repeated work for their rivals.

### Hostile creature gangs

Begin hostile or highly antagonistic.

They are not permanently locked as “evil.” The player can earn respect through gameplay.

A hostile crew may offer deals like:

- defeat a friendly gang in a Turf War
- cover a rival's tags
- beat a rival champion
- protect their territory
- win a race for them
- steal back an emblem
- embarrass an allied enemy in a dance battle

Their progression can move through states like:

**Hostile → Wary → Tolerated → Respected → Friendly → Member**

### Neutral/conditional creatures

Preserve their nuance. Piglins, Wolves, Endermen, Spiders, Bees, and other context-dependent creatures should not be flattened into a generic morality bucket.

## 6.2 No permanent faction exclusion

The player should be able to reach maximum reputation and membership with **every gang** over a long enough playthrough.

Joining one gang does not permanently lock out another.

However, relationships remain alive:

- helping Crew A against Crew B can raise A and lower B
- betrayal can turn a Friendly gang hostile again
- reparations can rebuild trust
- rival crews remember major choices
- temporary feuds may create special encounters

This creates consequences without save-file dead ends.

## 6.3 Suggested reputation ladder

A generic ladder may be:

1. Hated
2. Hostile
3. Wary
4. Neutral
5. Recognized
6. Tolerated
7. Respected
8. Friendly
9. Member
10. Veteran
11. Legend

Individual gangs may theme the visible rank names while sharing normalized internal thresholds.

## 6.4 Reputation sources

Award or remove reputation through meaningful gameplay:

- races
- Turf Wars
- graffiti battles
- dance battles
- Trick Attack
- Copycat/HORSE
- tag
- protecting gang members
- helping in gang-vs-gang events
- leader challenges
- covering rival graffiti
- restoring allied graffiti
- discovering hideouts
- returning gang items
- performing gang signature tricks
- accepting rival contracts
- betrayal
- unprovoked attacks
- helping a sworn enemy

Do not reduce reputation to “kill 100 mobs.”

---

# 7. Gang Relationship Graph

Gangs need relationships with one another, not only with the player.

Support values such as:

- allied
- friendly
- respectful
- neutral
- competitive
- rival
- hostile
- sworn rival

This graph can generate authored/systemic events:

- defend a friendly crew's Turf War
- sabotage a rival's tags
- join an allied tournament
- mediate a competition
- challenge a champion
- choose which crew to support in a territory dispute
- run a three-way paint battle

Prefer actual JetSetCraft activities over generic radiant-quest filler.

---

# 8. Rare Natural Gang Encounters

Natural gang encounters should be **ultra rare and memorable**.

A normal player should be able to spend long periods playing Minecraft without the world becoming crowded by gang spawns. When one appears, the reaction should be:

> **“WAIT — IS THAT A GANG?!”**

Natural encounter eligibility can consider:

- biome
- dimension
- structure
- time
- weather
- gang theme
- terrain usefulness
- nearby grindable geometry
- progression
- current reputation
- current rivalries
- installed mods

Gang members should arrive with style:

- grind down nearby rails
- jump from roofs
- wall-ride into view
- skate down hills
- drop from above
- emerge from portals
- bounce from slime
- teleport
- ride modded track geometry
- perform synchronized tricks
- spray an emblem on arrival

Presentation should be cinematic without forcing the player into a long unskippable cutscene.

---

# 9. Universal Street Competition / Minigame Framework

Do not implement each mode as an unrelated script.

Create one reusable challenge framework supporting:

- challenge definition ID
- participants
- teams
- AI teams
- gang affiliation
- score
- combo score
- territory ownership
- timers
- untimed modes
- checkpoints
- dynamic arena boundaries
- objective markers
- music/beat timeline
- difficulty
- modifiers
- gang-specific rules
- rewards
- rematches
- personal bests
- multiplayer
- spectators
- reconnect/recovery where practical
- server authority
- result summaries

The world itself should become the arena whenever possible.

---

# 10. Turf War / Paint War

Create an excellent territory-painting mode inspired by the readability and strategy of paint-control games while remaining distinctly JetSetCraft/Minecraft.

Players and gangs compete to paint/tag the highest-value amount of valid territory before time expires.

Track:

- valid surface area controlled
- percentage controlled
- reclaimed enemy territory
- high-risk surfaces
- vertical surfaces
- trick-to-paint bonuses
- combo chains
- team contribution
- late-game swings

Painting should default to a **temporary/non-destructive event overlay** so a challenge cannot permanently ruin a base.

Provide server controls for:

- temporary paint
- permanent graffiti where explicitly allowed
- protected claims
- valid surfaces
- arena boundaries
- cleanup
- persistence

AI must understand territory strategy:

- find valuable unpainted zones
- reclaim contested zones
- split into roles
- intercept opposing painters
- protect useful routes
- use high-value vertical areas
- react to current score/time
- take movement shortcuts

Do not let AI simply spray random blocks.

---

# 11. Graffiti Competition Modes

## 11.1 Tag Rush

Hit as many designated graffiti points as possible before the round ends.

## 11.2 Style Tag

Complexity, location, movement flow, and style matter more than raw quantity.

## 11.3 Risk Tag

Hard-to-reach and dangerous surfaces provide much larger score multipliers.

## 11.4 Gang Tag

Cover rival tags while defending your own crew's marks.

## 11.5 Moving Tag

Targets move/change while participants traverse the environment.

The optimal play style should involve movement and flow, never standing still clicking a wall repeatedly.

---

# 12. Trick Attack

Score as many style points as possible inside a time limit.

Reward:

- unique tricks
- combo duration
- rail transfers
- manuals
- wall interactions
- aerial tricks
- environmental interactions
- speed
- risky but successful landings
- equipment transitions
- maintaining flow

Use repeat penalties/diminishing returns so one dominant trick cannot be spammed forever.

Gang AI needs to understand trick lines and the scoring system, not just fire scripted animations.

---

# 13. Copycat / HORSE-Style Trick Battles

One competitor performs a sequence. The opponent reproduces it.

Failure earns a strike/letter/round loss.

Sequences can grow from simple to absurd:

**grind → transfer → wall ride → aerial trick → rail landing → manual → graffiti finish**

AI difficulty should affect the complexity it attempts and how reliably it executes, without impossible input cheating.

---

# 14. Street Run / Dynamic Racing

Generate races through existing Minecraft terrain:

- villages
- caves
- ravines
- forests
- rooftops
- strongholds
- mineshafts
- Nether structures
- End terrain
- Aether terrain
- Twilight Forest structures
- Create rail networks
- other modded dimensions

Routes should reward JetSetCraft traversal mastery rather than ordinary sprinting.

Include discoverable shortcuts and multiple valid lines.

AI should navigate and choose routes intelligently rather than follow a rigid breadcrumb train.

---

# 15. High-Speed Tag

Implement real playground Tag through JetSetCraft movement.

Variants can include:

- classic Tag
- infection Tag
- freeze Tag
- elimination Tag
- team Tag
- trick Tag, where a tag only counts after a valid trick/combo condition
- spray Tag, where a paint hit transfers “it” status

AI should predict interception points and route choices rather than simply chase the player's current coordinates.

---

# 16. Dance Battles

Dance battles must happen visibly in the Minecraft world, not as a detached menu minigame.

Core flow:

1. Boombox starts the track.
2. Beat timeline becomes active.
3. Direction/action prompts appear using configurable keybinds.
4. Player hits inputs in rhythm.
5. Character performs chained breakdance/dance animations.
6. Accuracy, combo, variation, and difficulty build score.
7. Opponent responds simultaneously or by turns depending on mode.

Grades can include:

- Perfect
- Great
- Good
- Miss

Score can consider:

- timing accuracy
- combo
- style variety
- difficult patterns
- freestyle sections
- gang signature moves

Dead Beat should be especially memorable in rhythm/dance modes.

Never assume literal arrow keys; use configurable controls with arrow-key style prompts as one default presentation.

---

# 17. Spray-Paint Combat

Spray paint should be a playful JetSetCraft combat/competition mechanic, not a generic firearm reskin.

Possible mechanics:

- direct spray damage where appropriate
- paint buildup
- marking targets
- temporary visual obstruction
- movement disruption
- gang-color weaknesses/resistances
- combo multipliers
- tagging stunned opponents
- territory buffs
- spray clashes/parries
- graffiti finishers
- paint resource management

Balance PvE and PvP independently where needed.

AI must understand:

- spray range
- line of sight
- dodging
- resource usage
- positioning
- counterattacks
- retreat
- team cover

---

# 18. Competition AI Quality Bar

This expansion succeeds only if gang AI is fun to play against.

Do not create mobs that merely use vanilla pathfinding and walk toward the player while a skateboard model is attached.

Build specialized high-level competition AI that understands:

- skating
- acceleration/braking
- jumping
- grinding
- rail switching
- transfers
- wall riding
- wall skating
- trick opportunities
- shortcuts
- objective scoring
- territory control
- opponent interception
- spray range
- retreating
- teamwork
- role assignment
- hazards
- dynamic arena boundaries
- catch-up strategy
- protecting a lead
- desperation behavior
- gang personality

## 18.1 Difficulty without cheating

Increase difficulty primarily through:

- better decision quality
- faster but believable reaction
- stronger route selection
- better trick selection
- better teamwork
- smarter risk management

Avoid:

- impossible acceleration
- perfect omniscience
- infinite spray resources
- impossible input rates
- teleporting solely to catch up
- rubber-banding that invalidates player skill

An expert AI should be good enough that a player can watch it and learn a better route.

## 18.2 Team roles

Support roles such as:

- leader
- racer
- trick specialist
- territory painter
- defender
- interceptor
- disruptor
- support
- wildcard

Different gangs should use roles differently.

---

# 19. Flagship Gang Identities

These starting crews should be polished enough to demonstrate the full architecture.

## 19.1 Creepaku Gouji — Creepers

**Identity:** explosive, reckless speed freaks.

Gameplay characteristics:

- sudden acceleration
- risky close passes
- explosive boost effects
- chaotic trick lines
- evasive movement
- blast-launch traversal
- aggressive territory pushes

Explosions should be visually exciting without unnecessarily destroying the world during normal competitions.

## 19.2 The Bone Drones — Skeletons

**Identity:** unnervingly precise technical skaters.

Gameplay characteristics:

- accurate line choice
- synchronized formation grinding
- ranged spray harassment
- precision trick challenges
- coordinated roles
- efficient rail routing

## 19.3 Arachnaphobia — Spiders

**Identity:** vertical-movement specialists.

Gameplay characteristics:

- wall skating
- vertical grinding
- wall-to-wall transfers
- ceiling transitions where practical
- web traps
- sudden leaps
- ambush routes
- extreme three-dimensional arena use

Arachnaphobia is an excellent first vertical-slice gang because it forces wall/vertical AI and movement architecture to prove itself.

## 19.4 Dead Beat — Zombies

**Identity:** relentless rhythm-and-numbers street crew.

Gameplay characteristics:

- big groups
- persistent pursuit
- synchronized movement
- endurance contests
- dance battles
- territory swarms
- deceptively strong coordination

They should look shambling until the beat drops.

---

# 20. Approved Vanilla Gang Atlas — Adult/Main Crews

The following names are approved canonical **default display names**. They remain data-driven and renameable; the stable `gang_id` is what matters internally.

## 20.1 Friendly / passive-start crews

| Vanilla mob | Default gang name | Design hook |
|---|---|---|
| Allay | **Blue Notes** | musical support, retrieval relay, aerial lines |
| Axolotl | **Gillty Pleasure** | aquatic trick crew, rescue/support |
| Bat | **Echo Chamber** | cave routes, sonar-style navigation |
| Camel | **Dune Cruisers** | long-stride desert flow and two-rider antics |
| Cat | **Nine Lives** | precision landings and rooftop routes |
| Chicken | **The Pecking Order** | tiny chaos, flutter drops, pecking hierarchy |
| Cod | **Cod Frequency** | schooling water routes |
| Cow | **The Milk Run** | friendly endurance and herd challenges |
| Donkey | **Kickback** | cargo/utility street crew |
| Fox | **Fox Trot** | speed, night routes, theft/retrieval games |
| Frog | **Ribbit Riot** | bounce chains and lily-pad lines |
| Glow Squid | **Neon Ink** | luminous underwater graffiti |
| Horse | **Bridle Breakers** | speed lines and mounted race culture |
| Mooshroom | **Spore Score** | mushroom-island style battles |
| Mule | **Pack Attack** | cargo relay challenges |
| Ocelot | **Spot Check** | jungle agility and evasive lines |
| Parrot | **Repeat Offenders** | mimicry, rhythm, Copycat battles |
| Pig | **Hog Wild** | chaotic friendly races |
| Rabbit | **Hare Trigger** | explosive starts and jump precision |
| Salmon | **Upstream** | current fighting and vertical water routes |
| Sheep | **Fleece Fleet** | friendly herd formation events |
| Skeleton Horse | **Pale Riders** | rare eerie racing crew |
| Sniffer | **The Throwbacks** | ancient/trail-discovery competitions |
| Squid | **Inkognito** | stealthy underwater paint play |
| Strider | **Lava Lanes** | Nether lava-course specialists |
| Tadpole | **Small Fry** | tiny aquatic junior-style events |
| Tropical Fish | **Reef Riders** | colorful group routes |
| Turtle | **Shell Rollers** | endurance and shoreline lines |
| Villager | **Block Party** | village street festivals and community jams |
| Wandering Trader | **The Roadshow** | traveling challenge/events crew |

## 20.2 Neutral / conditional-start crews

| Vanilla mob | Default gang name | Design hook |
|---|---|---|
| Bee | **Hive Five** | coordinated swarm teamwork |
| Dolphin | **Wave Riders** | speed swimming and water-launch tricks |
| Enderman | **Ender the Influence** | teleport/glitch route identity |
| Goat | **High Ground** | mountain lines and knockback games |
| Iron Golem | **Ironclad** | village defense, heavy trick style |
| Llama | **Spit Take** | ranged disruption and caravan contests |
| Panda | **Bamboo B-Sides** | playful rolling/dance style |
| Piglin | **Gold Rush** | gold-fueled Nether rivalry and bartering flavor |
| Polar Bear | **Ice Breakers** | frozen terrain specialists |
| Snow Golem | **Cold Front** | snow trails and ranged disruption |
| Spider | **Arachnaphobia** | vertical movement; preserve source mob day/night nuance outside events |
| Trader Llama | **Caravan Crew** | traveling support/race crew |
| Wolf | **Pack Mentality** | coordinated pursuit and pack tactics |
| Zombified Piglin | **Dead Mint** | conditional Nether mob behavior plus undead gold style |

## 20.3 Hostile-start crews

| Vanilla mob | Default gang name | Design hook |
|---|---|---|
| Blaze | **Burnout Brigade** | fiery aerial boosts and heat lines |
| Cave Spider | **Underweb** | compact cave ambushes and verticality |
| Creeper | **Creepaku Gouji** | explosive speed and blast-launch style |
| Drowned | **Dead Water** | amphibious pursuit and water territory |
| Elder Guardian | **Ancient Current** | elite aquatic encounter |
| Endermite | **Static Noise** | tiny teleport-adjacent disruption |
| Evoker | **Conjure Club** | magical control and summoned pressure |
| Ghast | **Wail Riders** | aerial space control and huge arenas |
| Guardian | **Current Affairs** | precision aquatic control |
| Hoglin | **Razorbacks** | heavy charge lines and knockback |
| Husk | **Dry Spell** | desert attrition/endurance |
| Magma Cube | **Hot Bounce** | lava-zone bounce chains |
| Phantom | **Night Shift** | aerial night raids and drop-ins |
| Piglin Brute | **Gold Standard** | elite heavy Gold Rush-adjacent crew |
| Pillager | **Raid Parade** | ranged formation pressure |
| Pufferfish | **Puff Piece** | proposed full-atlas addition; defensive inflation and close-range denial |
| Ravager | **Wrecking Crew** | heavyweight obstacle smashing/charge play |
| Shulker | **Boxed In** | vertical levitation routes and arena control |
| Silverfish | **Silver Static** | tiny swarm disruption |
| Skeleton | **The Bone Drones** | precision and synchronized technical lines |
| Slime | **Goo Groove** | bounce-based movement and rhythm |
| Spider | **Arachnaphobia** | vertical movement; source mob nuance still respected outside events |
| Stray | **Cold Shots** | icy ranged precision |
| Vex | **Bad Spirits** | aerial harassment and phase-like route pressure |
| Vindicator | **Axe to Grind** | aggressive close-range crew |
| Warden | **Deep Cuts** | legendary Deep Dark elite encounter |
| Witch | **Hex Appeal** | potion/magic disruption with stylish trick play |
| Wither Skeleton | **Blackout Bones** | hardcore Nether technical crew |
| Zoglin | **Rotten Rush** | relentless charge crew |
| Zombie | **Dead Beat** | rhythm, endurance, numbers |
| Zombie Villager | **Dead Locals** | corrupted village street crew |

## 20.4 Boss / legendary / special entries

These should not behave like common street spawns.

| Entity | Default encounter identity | Treatment |
|---|---|---|
| Ender Dragon | **Final Flight** | legendary End challenge/event |
| Wither | **Triple Threat** | legendary multi-head boss challenge |
| Warden | **Deep Cuts** | ultra-rare elite crew/leader encounter |
| Elder Guardian | **Ancient Current** | elite monument crew |
| Zombie Horse | **Night Mares** | proposed special/technical crew; hidden unless available/enabled |
| Illusioner | **Smoke & Mirrors** | hidden/technical entry unless explicitly enabled |
| Giant | **Dead Beat Titan** | hidden/technical Dead Beat boss variant |

If an entity exists in the registry but is normally unused/unobtainable in survival, keep it out of ordinary progression unless explicitly enabled.

---

# 21. Junior / Baby Gang Atlas

Baby-capable creatures deserve their own absurd subculture rather than merely being scaled-down adult models.

The Junior Atlas is a **sub-atlas under the parent gang system**, not a separate incompatible faction system.

## 21.1 Tone

Junior crews should be:

- painfully cute
- tiny
- squeaky
- overconfident
- wildly energetic
- hilariously serious about their turf
- comically aggressive once a challenge starts
- slapstick rather than gory

The joke is that they look adorable while treating a three-block patch of dirt like the most important gang war in history.

Friendly junior crews begin friendly just like their parent species. Hostile junior mobs inherit hostile/wary disposition. Relationship changes still come from the same reputation system.

## 21.2 Audio identity

Each junior crew receives a **cute/squeaky remix slot** of the adult gang theme.

The final owner-made music can later replace these resources. For now, define valid silent audio placeholders and metadata for:

- higher-pitched/squeaky instrumentation profile
- toy percussion profile
- tiny record-scratch cue
- baby vocal/sound cue hooks where legal/appropriate
- same BPM grid as parent where useful for synchronized battles

Do not alter copyrighted third-party music. These are JetSetCraft-owned/custom track slots.

## 21.3 Junior behavior modifier

A Junior profile can emphasize:

- smaller hitboxes
- faster direction changes
- shorter stride but frantic cadence
- lower raw damage than adult equivalents where balance requires
- stronger swarm behavior
- exaggerated knockback/recoil
- toy-sized spray cans
- tiny custom skates
- over-the-top taunts/emotes
- chaotic team tactics
- special Junior challenge badges/rewards

Avoid making babies simply stat-superior to adults.

## 21.4 Vanilla 1.20.1 baby/junior crew naming atlas

The table covers naturally or technically baby-capable vanilla 1.20.1 creature families. Where a species has no normal baby form, do not invent one merely for completeness; it stays in the adult Atlas unless JetSetCraft later adds an explicit cosmetic Junior variant.

| Baby / juvenile creature | Parent gang | Junior default name | Cute hook |
|---|---|---|---|
| Baby Axolotl | Gillty Pleasure | **Gillty Giggles** | chirpy aquatic trick swarm |
| Baby Bee | Hive Five | **Hive Five-Lings** | tiny buzz-squad teamwork |
| Camel Calf | Dune Cruisers | **Dune Snoozers** | sleepy-looking desert troublemakers |
| Kitten | Nine Lives | **Mew Lives** | meowing rooftop tiny crew |
| Chick | The Pecking Order | **The Peeping Order** | peep-peep hierarchy with absurd seriousness |
| Calf | The Milk Run | **The Moo Run** | tiny stampede relay |
| Donkey Foal | Kickback | **Hee-Haw Kickback** | squeaky cargo chaos |
| Fox Kit | Fox Trot | **Fox Trot Tots** | yipping night sprinters |
| Goat Kid | High Ground | **Bleat Ground** | tiny headbutt mountain gang |
| Baby Hoglin | Razorbacks | **Rattlebacks** | baby-rattle pun, miniature heavy charges |
| Horse Foal | Bridle Breakers | **Whinny Breakers** | squeaky race prodigies |
| Baby Husk | Dry Spell | **Dry Squeak** | desert baby-zombie terror |
| Llama Cria | Spit Take | **Spit-Take Tots** | tiny spit-disruption crew |
| Mooshroom Calf | Spore Score | **Spore Snore** | sleepy mushroom-island chaos |
| Mule Foal | Pack Attack | **Pack-a-Snack** | tiny cargo-relay bandits |
| Ocelot Kitten | Spot Check | **Spot Meow** | jungle pounce crew |
| Panda Cub | Bamboo B-Sides | **Bamboo Babbles** | rolling, babbling dance crew |
| Piglet | Hog Wild | **Hog Mild** | adorable until the challenge begins |
| Baby Piglin | Gold Rush | **Gold Hush** | tiny gold-obsessed troublemakers |
| Polar Bear Cub | Ice Breakers | **Ice Squeakers** | miniature ice crew |
| Rabbit Kit | Hare Trigger | **Hare Tickle** | hyperactive jump-combo crew |
| Lamb | Fleece Fleet | **Fleece Peep** | tiny formation flock |
| Skeleton Horse Foal / technical baby | Pale Riders | **Pale Trotters** | rare ghostly junior race crew |
| Snifflet | The Throwbacks | **The Throwbabies** | ancient but somehow baby-sized |
| Baby Strider | Lava Lanes | **Lava Lullabies** | tiny Nether lava cruisers |
| Trader Llama Cria | Caravan Crew | **Cria Caravan** | traveling junior support crew |
| Baby Turtle | Shell Rollers | **Shell Rollies** | tiny shoreline rollers |
| Baby Villager | Block Party | **Block Potty** | hilariously serious village toddlers |
| Wolf Pup | Pack Mentality | **Yap Mentality** | yapping pursuit squad |
| Baby Drowned | Dead Water | **Dead Puddle** | tiny amphibious menace |
| Baby Zombie | Dead Beat | **Dead Beep** | squeaky rhythm swarm |
| Baby Zombie Villager | Dead Locals | **Dead Little Locals** | tiny corrupted block-party rivals |
| Baby Zombified Piglin | Dead Mint | **Dead Mint Minis** | tiny neutral/hostile Nether posse |
| Baby Zoglin / technical baby | Rotten Rush | **Rattle Rush** | frantic miniature charge crew |

### Juvenile-equivalent special cases

Some vanilla creatures use a distinct entity or size mechanic instead of a conventional baby form. The Atlas should understand those relationships rather than forcing fake age data.

| Juvenile-like case | Parent relationship | Junior treatment |
|---|---|---|
| Tadpole | grows into Frog / Ribbit Riot family | **Small Fry** already functions as its own approved crew identity; cross-link it as the Frog junior life stage |
| Small Slime | size-based, not age-based | expose **Goo Goos** as an optional Junior-style Goo Groove subcrew/profile without pretending the source entity has baby age data |
| Small Magma Cube | size-based, not age-based | optional **Hot Tots** junior profile linked to Hot Bounce |

The Junior system must detect actual source mechanics. Never write bogus age data into entities that do not support it.

---

# 22. Gang Atlas — In-Game Black Book

Create a polished **Gang Atlas / Black Book** as the player's collection, relationship, and customization hub.

For every discovered crew, show where available:

- mob portrait/model
- source mod icon/name
- gang emblem
- canonical default name
- player's current alias
- colors
- adult/junior status
- initial/current disposition
- reputation meter
- reputation rank
- membership status
- rename unlock status
- allies
- rivals
- leader/champion
- preferred equipment
- signature movement
- signature minigames
- signature tricks
- signature reward
- dimension/biome affinity
- encounter rarity
- music title
- junior music title
- discovered graffiti
- wins/losses
- best score
- contracts completed
- betrayal/reconciliation history where useful

Unknown gangs should initially appear mysterious rather than dumping every secret immediately.

## 22.1 Discovery

Possible discovery sources:

- natural encounter
- seeing a gangified mob
- inserting a valid head/emblem into a Boombox
- finding gang graffiti
- obtaining gang equipment
- receiving a contract referencing the crew
- meeting an allied/rival crew that mentions them

## 22.2 Editing

After Friendly reputation:

- enable Rename
- allow Reset to Canonical
- show safe preview before saving
- keep the stable ID visible in advanced/debug views

---

# 23. Universal Installed-Mod Mob Atlas

“Mod support” means more than a few hand-authored compatibility statements.

JetSetCraft should build an **Atlas of mobs from installed mods** at runtime.

## 23.1 Runtime enumeration

At an appropriate lifecycle stage, enumerate registered compatible mob entity types by namespaced registry ID.

For each detected namespace/mod, build a Mod Atlas section:

- mod display name
- mod ID
- detected version where safely available
- mod icon when safely accessible
- total detected mob types
- gang-compatible count
- curated adapter status
- baby/juvenile support status
- gang definitions
- generic fallback status

Every safe `EntityType<? extends Mob>` should receive a gang-compatibility record even if JetSetCraft has never seen that mod before.

## 23.2 Two-tier compatibility

### Tier A — Generic universal compatibility

Unknown mod mobs can still receive:

- JetSetCraft gang state
- generic gang equipment attachment
- safe generic skate rendering
- reputation
- minigame participation where movement permits
- player/server-assigned gang name
- Boombox token mapping when configured

If a model is too unusual for generic feet rendering, fail visually gracefully and mark it for a render adapter rather than crashing.

### Tier B — Curated premium compatibility

Important mods receive authored:

- crew names
- emblems
- colors
- movement adapters
- model attachment anchors
- custom skates
- animation profiles
- entrances
- rewards
- AI styles
- biome/dimension affinity
- baby crew names where the mod supports juveniles
- music slots
- rivalries

## 23.3 Never force weak autogenerated names

The Atlas must list every detected creature, but JetSetCraft does not need to ship a bad pun for every unknown mod entity.

For uncurated entries:

- show the creature and source mod
- give it a stable generated gang slot/ID
- allow player/server naming after the appropriate unlock or through modpack data
- optionally use a neutral fallback label such as “Unclaimed Crew” in developer/undiscovered states

Curated packs can later supply a premium default name.

This keeps universal compatibility broad without sacrificing writing quality.

## 23.4 Dynamic removal safety

If a mod is removed from a test instance/world:

- JetSetCraft itself still loads
- missing external IDs resolve to dormant/unavailable Atlas records instead of classloading crashes
- world data keeps namespaced references safely
- no hard class reference is loaded when the optional mod is absent
- stale compatibility data can be retained for history but not instantiated

---

# 24. Curated Mod Compatibility Atlas Targets

Build first-class Atlas packs/adapters for popular creature/dimension ecosystems relevant to Forge 1.20.1 where an actual compatible version is available.

Priority candidates include:

- **The Aether**
- **Twilight Forest**
- **Blue Skies**
- **The Bumblezone**
- **Deeper and Darker**
- **Alex's Mobs**
- **Alex's Caves**
- **Naturalist**
- **Mowzie's Mobs**
- **Ice and Fire** where target-version compatibility is available
- **L_Ender's Cataclysm** where target-version compatibility is available
- **Friends & Foes**
- **Creeper Overhaul**
- **Born in Chaos** where appropriate
- **Aquamirae** where appropriate
- other widely installed mob/dimension mods discovered during current ecosystem research

Do not make any of them required dependencies.

A curated Mod Atlas should be able to show every detected mob from that mod, not merely the handful that have unique rewards.

For example, if The Aether is installed:

1. The Aether registers its entities normally.
2. JetSetCraft detects the `aether:*` mob entries.
3. The Aether section appears in the Mod Gang Atlas.
4. Generic gangification works where safe.
5. Curated definitions improve names, attachment points, movement, AI, rewards, and presentation.
6. Giving an Aether mob appropriate Gang Skates activates JetSetCraft state on the same Aether-owned entity.
7. Removing the skates returns that creature to normal Aether behavior.
8. Removing The Aether from another test instance does not prevent JetSetCraft from loading.

Repeat that ownership pattern for every optional mod.

---

# 25. World / Dimension / Block Compatibility

Continue expanding compatibility with popular dimensions, structures, terrain, movement systems, rails, and block ecosystems.

JetSetCraft should detect/use, where safe:

- rails
- Create tracks
- fences
- walls
- ledges
- pipes
- beams
- chains
- cables
- decorative edges
- grindable collision geometry
- slippery blocks
- bouncy blocks
- sticky blocks
- boost surfaces
- hazards
- dimension-specific terrain

Preserve the “Minecraft itself is the skatepark” philosophy.

Examples:

- ice = strong speed preservation/boost interaction
- packed ice = stronger interaction
- blue ice = extreme high-skill speed
- slime = bounce tech
- honey = sticky/slow tech
- powered rails = boost opportunities
- vanilla rails = grind lines
- Create tracks = shape-following grind lines
- soul sand = slowdown/line planning
- fluids/currents = momentum interactions
- pistons/explosions = legitimate launch tech where existing architecture allows
- wind/updraft mechanics = aerial opportunities

Use tags, capabilities, collision-shape analysis, registries, and compatibility adapters instead of hardcoding hundreds of block IDs.

---

# 26. Dynamic Arenas — The World Is the Course

Before a challenge, analyze nearby terrain and identify:

- grindable paths
- rails
- walls
- vertical surfaces
- rooftops
- open paint territory
- slopes
- drops
- trick lines
- hazards
- safe spawn/entrance points
- checkpoints
- shortcuts
- graffiti surfaces

Then adapt the selected mode to the location.

Examples:

- village → rooftop race / Block Party jam
- mineshaft → rail grind challenge
- Nether fortress → vertical trick attack
- Aether terrain → huge aerial line competition
- Twilight Forest structure → obstacle race
- Create factory → dense rail-routing battle

Do not destroy or substantially rewrite player builds just to create an arena.

---

# 27. Cinematic Gang Entrances

The Boombox theme should begin before or during arrival.

Distinct entrance concepts:

### Creepaku Gouji
Explosive boost chains, reckless canyon launches, smoke/paint bursts.

### The Bone Drones
Perfectly synchronized rail grind into formation.

### Arachnaphobia
Wall descent, vertical grinding, rooftop/ceiling arrival.

### Dead Beat
Large synchronized procession that suddenly locks perfectly to the beat.

### Hex Appeal
Smoke, particles, potion/magic flourish blended with skating.

### Dead Water
Emerge from nearby water, canals, flooded caves, or wet routes.

### Goo Groove
Bounce into the scene using slime physics.

### Burnout Brigade
Fiery high-speed boost/grind entrance.

### Night Shift
Aerial drop-in under night conditions.

### Gold Rush
Polished gold street swagger, coordinated Nether entrance, bartering/gold motifs.

Junior variants should receive comically miniature versions of these presentations with their own squeaky theme mixes.

---

# 28. Themed Rewards

Gang rewards should be memorable mechanics, cosmetics, music, tricks, or equipment—not generic resource bundles.

## 28.1 Arachnaphobia — Spider Skates

Signature reward direction:

- wall skating
- vertical skating
- vertical grinding
- temporary wall adhesion
- wall-to-wall transfers
- ceiling interactions where technically reasonable

Balance them so they expand traversal without making every other ride style obsolete.

## 28.2 Creepaku Gouji

Reward direction:

- explosive boost equipment
- blast-launch tricks
- volatile momentum mechanics with safe world-damage defaults

## 28.3 The Bone Drones

Reward direction:

- precision landing tools
- enhanced rail control
- ranged graffiti accuracy
- technical combo perks

## 28.4 Dead Beat

Reward direction:

- rhythm/combo equipment
- beat-synced bonuses
- dance/trick chain benefits

Other gangs should reach the same quality bar.

Possible reward categories:

- signature skates
- board/BMX/hoverboard/scooter cosmetics
- traversal ability modifiers
- graffiti
- emblems
- music tracks
- Boombox skins/stickers
- dances
- trick animations
- victory poses
- clothing
- leader variants
- challenge modifiers

---

# 29. Gang Progression & Memory

Repeated encounters should evolve.

Track where useful:

- gang discovery
- player reputation
- membership rank
- rivalry heat
- wins/losses
- leader defeats
- special contracts
- betrayals
- reconciliations
- unlocked rewards
- signature challenges

Beating a crew once must not exhaust all content.

Possible long-term progression:

**Unknown → Spotted → Rival/Friend → Respected → Member → Veteran → Legend**

Keep this optional to normal Minecraft progression. JetSetCraft should enrich the world, not turn every survival save into a mandatory quest campaign.

---

# 30. Multiplayer Architecture

Design the gang/minigame framework as server-authoritative from the beginning.

Support:

- player vs gang
- players vs gang
- player vs player
- team vs team
- mixed player + gang teams where appropriate
- spectators
- synchronized scoring
- synchronized music timeline
- synchronized Boombox state
- synchronized dynamic arena boundaries
- reconnect/recovery where practical

Important gameplay decisions must not live only on the client.

Use the server as authority for:

- challenge lifecycle
- participants
- score
- territory
- reputation changes
- rewards
- gang state
- movement decisions that affect fairness
- target/head selection
- match results

---

# 31. Performance

The system may involve multiple high-mobility AI opponents in large modpacks, so performance must be designed rather than patched later.

Use approaches such as:

- hierarchical AI decisions
- cached terrain analysis
- shared gang route knowledge
- spatial indexing
- bounded path/line searches
- event-driven updates
- reusable route graphs
- safe asynchronous computation only where Minecraft permits it
- AI LOD that does not change active-match fairness
- lower-frequency strategic decisions with high-frequency local steering

Avoid every gang member doing a full environment scan every tick.

---

# 32. Configuration & Modpack Extensibility

Provide excellent defaults, then expose meaningful controls for:

- natural gang rarity
- enabled gangs
- enabled mod atlases
- challenge types
- AI difficulty
- reputation gain/loss multipliers
- gang damage
- PvP
- paint persistence
- terrain protection
- arena limits
- music volume/behavior
- reduced-motion presentation
- cinematic intensity
- junior crew behavior
- shared gang aliases
- datapack-defined crews
- dimension restrictions
- compatibility adapter toggles

A player should not need to spend an hour configuring JetSetCraft before it becomes fun.

---

# 33. Proposed Data Model

A clean implementation could separate immutable/stable identity from mutable presentation and runtime state.

## 33.1 GangDefinition

Conceptual fields:

```text
gang_id
canonical_name_key
short_name_key
source_entity_ids[]
source_entity_tags[]
base_disposition
colors
emblem
music_profile
junior_profile
ai_profile
movement_profile
render_profile
entrance_profile
reward_profile
minigames[]
allies[]
rivals[]
biome_affinity[]
dimension_affinity[]
rarity
boombox_targets[]
compatibility_requirements[]
```

## 33.2 GangMemberAttachment

Conceptual runtime state:

```text
gang_id
role
equipment
is_gangified
active_challenge_id
original_state_snapshot_if_needed
ai_mode
movement_adapter
render_adapter
```

Only store original state that JetSetCraft actually changes and needs to restore. Do not serialize giant copies of another mod's entity NBT unnecessarily.

## 33.3 PlayerGangRelationship

```text
gang_id
reputation
rank
member
personal_alias
wins
losses
leader_wins
contracts_completed
betrayal_count
last_major_relationship_event
unlocks
```

## 33.4 WorldGangState

```text
gang_id
shared_alias_optional
relationship_overrides
territory_state_optional
leader_state
world_events
```

## 33.5 ModMobAtlasEntry

```text
entity_type_id
source_mod_id
source_mod_version_optional
gang_id_optional
generic_compatible
curated_adapter
baby_or_juvenile_mode
render_adapter_id
movement_adapter_id
boombox_target_items[]
```

---

# 34. Major Acceptance Tests

The architecture is not successful until the following workflows work in real runtime tests.

## 34.1 Vanilla reversible transformation

Normal vanilla mob  
→ apply Gang Skates  
→ gang equipment renders  
→ gang behavior activates  
→ mob participates in a minigame  
→ remove Gang Skates  
→ same entity returns to normal source behavior/data.

Verify UUID and important state are preserved.

## 34.2 Hostile-to-friendly reputation

Start hostile with a hostile-start gang  
→ accept appropriate work/challenges  
→ gain reputation  
→ become tolerated  
→ become respected  
→ become friendly  
→ unlock Crew Naming Rights  
→ join the gang.

## 34.3 Betrayal and recovery

Start Friendly/Member with Gang A  
→ help rival Gang B against Gang A  
→ lose A reputation  
→ see behavior/dialogue/Atlas state change  
→ later repair A relationship  
→ regain friendliness/membership benefits according to rules.

## 34.4 Rename safety

Reach Friendly  
→ rename gang in Atlas  
→ close/reopen world  
→ alias persists  
→ Boombox still targets same stable gang  
→ music/rewards/reputation remain correct  
→ reset to canonical name succeeds.

## 34.5 Boombox random challenge

Place Boombox  
→ leave target slot empty  
→ activate  
→ context-appropriate random gang selected  
→ correct music profile starts  
→ cinematic entrance occurs  
→ valid challenge begins.

## 34.6 Boombox targeted challenge

Insert mob head/emblem  
→ matching gang resolves  
→ Boombox retunes visually  
→ gang theme starts  
→ correct gang enters  
→ challenge begins.

## 34.7 Junior crew challenge

Encounter or deliberately create a valid baby/junior gang member  
→ Junior Atlas entry links to parent gang  
→ tiny equipment/render profile works  
→ junior music profile resolves  
→ challenge AI is distinct but balanced  
→ reputation interactions correctly affect parent/subcrew rules.

## 34.8 Optional mod compatibility

Install a supported mod such as The Aether  
→ its detected mobs populate that mod's Atlas section  
→ a compatible Aether mob remains an Aether-owned entity  
→ apply JetSetCraft Gang Skates  
→ Gang Form works  
→ remove skates  
→ original Aether behavior resumes.

Then test JetSetCraft without The Aether installed and verify JetSetCraft loads normally.

## 34.9 Unknown-mod generic compatibility

Install a creature mod with no curated JetSetCraft adapter  
→ safe mobs still appear in the Mod Mob Atlas  
→ generic gang state can attach where supported  
→ missing special render anchors degrade gracefully  
→ no classloading crash occurs.

## 34.10 Multiplayer synchronization

Two or more clients  
→ activate same Boombox  
→ hear/see synchronized challenge timeline  
→ share authoritative score/territory  
→ gang AI state remains server-authoritative  
→ match result/reputation persists correctly after restart.

---

# 35. Implementation Order — Vertical Slices, Not Placeholder Sprawl

Do not stop at interfaces, stubs, mock menus, or documentation.

A strong development order is:

1. Finalize stable `gang_id` / GangDefinition / relationship data model.
2. Implement reversible GangMemberAttachment on existing mobs.
3. Implement Gang Skates/equipment attachment and safe render anchors.
4. Implement Boombox block/entity, empty random targeting, and target slot.
5. Implement music metadata + valid silent placeholder assets.
6. Implement one complete flagship gang end-to-end; Arachnaphobia is a strong architecture stress test.
7. Implement generic challenge lifecycle/scoring.
8. Implement Turf War plus one movement-focused mode and Dance Battle.
9. Implement specialized competition AI and team roles.
10. Implement reputation, membership, betrayal/recovery, and Crew Naming Rights.
11. Implement Gang Atlas adult registry.
12. Implement Junior/Baby Atlas and tiny equipment/audio profiles.
13. Implement runtime Installed-Mod Mob Atlas.
14. Implement generic unknown-mod gangification.
15. Add curated mod adapters for the highest-value ecosystems.
16. Expand all approved vanilla gangs and rewards.
17. Expand dynamic arenas and natural rare encounters.
18. Polish cinematic entrances, UI, audio, animation, and multiplayer.
19. Performance profile in large modpacks and optimize hot paths without degrading behavior.
20. Perform an independent outside-the-box improvement pass before calling the system mature.

After each meaningful stage:

- compile
- run targeted tests
- run broader regression tests when risk warrants
- launch the real mod where possible
- exercise the exact gameplay path
- inspect logs
- fix root causes rather than hiding errors
- preserve a usable test build
- update project documentation/wiki alongside verified behavior

---

# 36. Outside-the-Box Expansion Hooks

Design now so future high-value additions do not require a rewrite.

Potential extensions:

- gang headquarters/hideouts discovered naturally in existing structures
- gang radio stations selectable from Boombox after high reputation
- collaborative murals unlocked by allied crews
- cross-gang tournaments
- three-way Turf Wars
- gang leaders who remember signature losses
- “street legends” generated from high-scoring player ghosts/replays where technically feasible
- modpack-authored gangs using datapacks only
- server seasons that reset territory but not cosmetic discovery
- player-created crews that can enter the same relationship graph
- gang sticker layers physically accumulating on a player's Boombox
- junior-vs-adult exhibition matches
- rare “all gangs jam” festivals once the player reaches broad respect

These are expansion hooks, not excuses to delay the core vertical slice.

---

# 37. Final Design Standard

Every feature should reinforce:

**movement + music + expression + graffiti + rivalry + exploration**

Avoid generic RPG mechanics simply because they are easy to implement.

Ask:

> **“Would this create an amazing moment while skating through Minecraft?”**

If the answer is no, redesign it.

The desired memories are things like:

- peacefully exploring for hours, hearing unfamiliar music, cresting a hill, and discovering Arachnaphobia grinding vertically across a ruined structure;
- placing the Boombox beside a huge Create railway, inserting a Skeleton Skull, and watching The Bone Drones arrive in perfect formation;
- seeing Creepaku Gouji blast-launch across a canyon without turning the world into crater soup;
- getting challenged by Dead Beat to a midnight village breakdance battle;
- befriending Gold Rush after beginning as enemies, joining them, then later betraying them for Block Party and having to rebuild the relationship;
- renaming a beloved Friendly gang in the Atlas without breaking any of its identity or progression;
- seeing an installed Aether creature appear in the Mod Mob Atlas, giving it JetSetCraft skates, and watching it become a gang competitor without JetSetCraft ever replacing the Aether entity;
- getting jumped by a tiny **Dead Beep** baby-zombie crew while their squeaky gang mix plays with absolute cinematic seriousness.

That is the quality bar.

The target is not “gang mobs added.”

The target is a **living, data-driven, cross-mod street-culture ecosystem** where almost any Minecraft creature can become part of JetSetCraft without JetSetCraft fighting the original game or other mods for ownership.

> **Cool mob. Now give it skates.**
