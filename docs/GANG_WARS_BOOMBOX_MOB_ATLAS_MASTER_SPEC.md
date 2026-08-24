# JetSetCraft — Gang Wars, Boombox, Mob Atlas & Universal Gangification Master Specification

**Project:** JetSetCraft  
**Primary target:** Minecraft Java Edition 1.20.1 / Forge  
**Document role:** Preserved design lineage for possible post-v0.3 expansions
**Status:** Reference direction; the README, changelog, runtime guide, and release evidence define shipped behavior

> **Not a v0.3.0 feature list.** This document preserves broader Gang Wars, territory, reputation, chapter, and
> atlas concepts for later releases. A concept here is not a release claim unless the runtime/release documents say so.

---

## 0. Mission

Continue development of **JetSetCraft** from the current project state. Do not restart the mod, replace working subsystems, regress existing movement, or reduce compatibility to make this expansion easier.

This expansion should make Minecraft feel as though a living underground street-sports culture has emerged inside the existing world. Vanilla creatures, modded creatures, dimensions, structures, rails, terrain, graffiti, music, movement, tricks, dance, rivalry, reputation, and multiplayer should all participate in one coherent system.

The defining fantasy is simple:

> **Minecraft owns the mob. JetSetCraft gives it skates, attitude, graffiti, music, rivalries, tricks, reputation, and a reason to throw down when the Boombox starts playing.**

A Bee remains a Bee. A Creeper remains a Creeper. An Aether creature remains an Aether creature. JetSetCraft never needs to replace those entities to make them part of its world.

The system is built around five connected pillars:

1. **The Boombox** — the universal challenge initializer, gang tuner, music source, and street-jam centerpiece.
2. **Universal Gangification** — persistent, equipment-bound JetSetCraft gang/rider state layered over untouched vanilla and mod-owned mobs; it remains until the actual JetSetCraft gear is removed, stolen, broken, or otherwise unequipped.
3. **Gang Reputation & Allegiance** — every crew can become friend, rival, enemy, or family through gameplay; players may ultimately join every gang.
4. **Street Competition Framework** — Turf War, graffiti, races, trick battles, tag, dance battles, spray combat, and other Jet Set-style contests.
5. **The Gang Atlas** — complete adult, junior/baby, and installed-mod creature registries that make the system understandable, collectible, expandable, and easy to customize.

The result should be replayable enough to become one of JetSetCraft's defining pillars rather than a side activity.

---

# 0A. STANDALONE COMPATIBILITY COVENANT — NEVER BREAK MINECRAFT TO ADD JETSETCRAFT

This covenant outranks implementation convenience. **JetSetCraft must remain a truly standalone, additive Forge mod.** The gang system, Boombox, natural encounters, trick vehicles, Mob Atlas, reputation, AI, graffiti, music, rewards, and optional-mod integrations must be implemented around Minecraft and other mods rather than by taking ownership of their code.

The release target is **zero known compatibility issues introduced by JetSetCraft**. Any reproducible conflict caused by this architecture is a release-blocking bug to fix at the root, not an acceptable tradeoff. It is impossible to mathematically guarantee behavior with every unknown future mod, so unknown integrations must **fail closed and degrade gracefully**: skip the optional enhancement, preserve the original mob/game behavior, log a useful diagnostic, and keep the world playable. Never patch globally first and hope compatibility survives.

## 0A.1 Absolutely forbidden for this system

Do **not** use any of the following as the normal implementation path for gangs or mob compatibility:

- replacing or registry-overriding vanilla entity types
- replacing another mod's entity types
- editing Mojang/vanilla source code
- copying vanilla mob classes into JetSetCraft and substituting them
- Mixins, ASM/coremods, bytecode rewriting, or access-transformer hacks that alter vanilla/third-party mob behavior merely to make Gang Form work
- globally deleting/replacing vanilla AI goals
- globally rewriting mob navigation
- globally changing vanilla attributes for a species
- replacing vanilla spawn tables or another mod's spawn tables
- taking over vanilla loot tables just to support gang selection
- canceling broad Forge events in ways that change unrelated vanilla/mod behavior
- hard class references to optional mods that can crash classloading when those mods are absent
- making Aether, Twilight Forest, Create, creature mods, combat mods, animation mods, or any other compatibility target a required dependency

If a desired feature appears to require one of these, **redesign the JetSetCraft layer first**. A narrowly scoped optional compatibility adapter may use a third-party mod's supported public API when that mod is present, but the adapter must be isolated, safely detected, and completely absent from base behavior when that mod is not installed.

## 0A.2 What JetSetCraft is allowed to own

JetSetCraft owns only its own additive state and content:

- its own items, such as Street Gear and the Boombox
- its own blocks/block entities/UI
- its own capabilities/data attachments/saved data
- its own render layers and equipment attachments
- its own high-level competition controller
- its own challenge AI state while an entity is participating
- its own gang/reputation/Atlas/music metadata
- its own ultra-rare encounter scheduler
- its own temporary paint/competition overlays
- its own recipes, tags, datapack definitions, and optional adapters

Normal Minecraft and mod behavior remains authoritative everywhere JetSetCraft is not explicitly active.

## 0A.3 Normal worlds must stay normal

Installing JetSetCraft should **not** noticeably rewrite ordinary Minecraft simulation. With no Street Gear equipped and no JetSetCraft event running:

- vanilla mobs behave normally
- modded mobs behave normally
- vanilla spawn distributions remain normal
- vanilla AI remains normal
- dimensions and structures remain owned by their source mods
- normal combat remains normal
- normal loot remains normal
- servers are not flooded with background gang AI

A player who ignores JetSetCraft's content should still have an ordinary compatible Minecraft/modpack world.

## 0A.4 Natural gang encounters are deliberately ultra-rare and additive

Do **not** turn every ordinary mob spawn into a gang roll and do not inject large weights into vanilla biome spawn lists. Natural gangs are a special JetSetCraft encounter layer.

Preferred behavior:

1. A lightweight JetSetCraft encounter scheduler occasionally evaluates whether an ultra-rare **Natural Hangout** discovery is appropriate.
2. It performs cheap checks first and does expensive terrain analysis only after the rarity gate succeeds.
3. A successful discovery creates a tiny persistent **Hangout/Turf record** (`site_id`) and selects or spawns a small resident crew using the original registered entity types through normal public APIs, then equips those residents with real JetSetCraft Street Gear.
4. Natural residents are persistent world actors associated with that `site_id`; they do **not** disappear because a challenge ends. They remain gangified while their gear remains equipped and use a soft home-area rule to hang around the site without forced teleporting or chunk loading.
5. The original entity type remains untouched. The hangout never reserves terrain, takes a chunk ticket, rewrites a biome spawn table, or edits world generation.
6. Encounter and resident caps prevent population inflation or runaway AI cost.
7. If anything about the location/entity/optional mod is unsafe or unsupported, the hangout simply is not created.

This is intentionally different from **Boombox/event-only crews**. Event-only actors may be created as an explicit ephemeral competition cast and leave/despawn after the challenge; Natural Hangout residents are the rare persistent world population.

Default natural encounter frequency should be **ultra rare**—an exciting discovery, never ambient clutter. Expose configuration for modpacks/servers without changing the safe default.

The dependable way to play gang content is the **craftable Boombox**, not grinding random spawns.

## 0A.5 The Boombox is the reliable standalone entry point

Ship a normal survival **crafting recipe for the Boombox using only vanilla ingredients by default**. The recipe itself must be data-driven so modpacks can override it, but JetSetCraft must never require another mod's material just to access its core gang system.

Once crafted and placed:

- empty target slot → start an appropriate random available gang event
- mob head/emblem/token inserted → target that gang
- if an optional mod is absent → its gangs simply are not eligible
- if no safe gang/event can be resolved → fail cleanly with useful player feedback, never corrupt or modify the world

This makes natural gangs optional flavor while giving every player a deterministic, compatible way to start the actual gameplay.

## 0A.6 Street Gear Activation — persistent equipment-bound conversion, never an event-only buff

Do not limit Gang/Rider Form to skates. Create one generalized **Street Gear Activation** contract covering:

- inline skates
- quad skates
- skateboards
- BMX bikes
- scooters
- hoverboards
- future JetSetCraft trick vehicles/equipment

A compatible mob becomes gangified **only because actual JetSetCraft Street Gear has been equipped/attached/carried into the JetSetCraft equipment system**. Merely entering a Boombox challenge, being near a gang, loading a chunk, or being selected by event AI must never silently convert a normal mob.

Conceptually:

**Untouched source mob + equipped JetSetCraft Street Gear = persistent JetSetCraft Rider/Gang Form**

**Persistent Rider/Gang Form + gear still present = remain gangified across events, chunk unload/reload, save/restart, and ordinary idle time**

**Actual gear removed/stolen/broken/unequipped = same source mob returns to its source-owned non-gang behavior**

The end of a race, dance battle, Turf War, or Boombox event removes only **transient challenge state**. It must **not** strip Street Gear, erase the mob's gang affiliation, or revert the mob merely because the event is over.

This is a state transition on the original entity whenever technically possible—not a despawn/replacement trick. Preserve UUID, health, age, owner/tame data, custom name, variants/genetics, existing equipment, inventories, capabilities, attachments, mod-specific persistent data, and every state JetSetCraft does not explicitly own.

A gangified mob remains a member of its `gang_id` for as long as its gang gear remains equipped. Player-to-gang reputation is separate durable relationship data keyed by `gang_id`; it is never erased merely because one individual mob loses its gear.

Natural Hangout residents follow the same rule: JetSetCraft creates them by selecting/spawning the **original registered mob type and actually equipping JetSetCraft Street Gear on it**. Those residents remain real gang members associated with their `site_id` while the gear remains equipped. Challenge cleanup may remove scoreboards, paint overlays, temporary navigation objectives, and challenge controllers—but never strips a resident's persistent gear or affiliation.

**Event-only actors are a separate lifecycle.** A Boombox or scripted challenge may create an explicit `event_actor` cast whose entire purpose is that challenge. They spawn already as JetSetCraft-equipped competition actors, never masquerade as ordinary persistent world mobs, and may skate away/despawn after the event once they are safely out of player view. Their disappearance is actor cleanup, not a fake "de-gangify" transition. Any reputation they generate is credited to the gang and, when relevant, the associated Hangout/Turf record rather than to individual ephemeral mobs.

If the creature anatomy cannot safely use a particular vehicle, the compatibility layer should choose a safe adapter/fallback or reject that equipment gracefully. **Never crash and never mutate the source mob class to force support.**

## 0A.6a Street Gear must be equipable through natural mob behavior

The system should feel like Minecraft rather than a debug command. Prefer **species-appropriate acquisition** and JetSetCraft-owned item behavior over intrusive mob patches.

Supported acquisition paths should include:

1. **Direct player equip** — use the Street Gear item on an eligible mob. JetSetCraft handles the interaction through its own item/event logic and writes only its own attachment state.
2. **Native pickup/steal behavior** — if a mob already has vanilla/mod-owned item pickup behavior, let that behavior be the trigger. Observe the successful pickup/held-item transition and, when the item is compatible JetSetCraft Street Gear, move/attach it into JetSetCraft's own equipment slot without rewriting the mob's pickup code.
3. **Foxes** — preserve their native ability to steal/pick up items. A fox that naturally acquires compatible Street Gear can gangify through that pickup. Do not replace fox AI to make this happen.
4. **Allays** — preserve their normal held-item/matching-item rules. Handing an Allay compatible Street Gear, or allowing it to pick up matching dropped gear through its normal mechanics, can activate JetSetCraft equipment state. Do not make Allays vacuum arbitrary items just for JetSetCraft.
5. **Other naturally item-capable mobs** — Zombies, Skeletons, Piglins, or modded mobs that already support pickup/equipment should use their source-owned mechanics as the trigger when possible.
6. **Dropped-gear walk-over** — for mobs that do not normally pick up items, a dropped **JetSetCraft Street Gear item itself** may perform a tiny, bounded nearby-collision/eligibility check. If an eligible unequipped mob physically runs into/touches that dropped gear, the gear can auto-equip. Keep this logic attached to the rare JetSetCraft gear item entity, not a global per-tick scan of every mob.
7. **Redstone/dispenser equip** — register normal dispenser behavior for JetSetCraft's own gear items. A dispenser facing an eligible mob should equip the gear just as Minecraft redstone equipment interactions feel like they should, without patching `DispenserBlock`, vanilla mob classes, or global armor code. A dropper can also place the item into the mob's path for normal walk-over acquisition.
8. **Optional automation adapters** — when Create or another automation mod is present, a narrowly scoped adapter may let its normal deploy/use-item mechanism equip JetSetCraft gear through the same public interaction contract. The automation mod remains optional.

Removal must be equally physical and truthful. If Street Gear is actually removed, stolen by another entity, broken (when that gear type has durability), admin-cleared, or otherwise leaves JetSetCraft's equipment attachment, the source mob immediately exits Gang/Rider Form and its original source-owned behavior becomes fully authoritative again.

A theft can therefore produce a real systemic moment: **Gang Mob A loses its skates → A de-gangifies; compatible Mob/Player B gains those skates → B can become gangified/equipped.** The `gang_id` and reputation systems remain stable identities independent of the individual item stack.

## 0A.6b Species-aware ground-contact and animal skating is first-class

JetSetCraft must not assume every rider is a two-legged humanoid. Build a reusable **Ground Contact / Ride Rig** abstraction that answers a simple question for every mob:

> **What parts of this creature actually contact the ground or support locomotion, and what ride presentation would look natural there?**

The generic system plus curated adapters should support:

- bipeds
- quadrupeds / four-legged animals
- six-legged insects
- eight-legged spiders/arachnids
- tiny many-legged creatures
- slimes/magma cubes and other body-contact locomotion
- legless creatures
- flying creatures when they land/use ground gear
- aquatic/amphibious creatures where a ground/contact presentation makes sense
- baby/juvenile versions with correctly scaled anchors
- unusual modded skeletons/models

A ride/contact profile should be able to define:

- locomotor/contact anchor count
- model-part or bone anchors when safely available
- generic fallback contact points derived from bounding box/pose when no model API exists
- which contacts receive individual skates/wheels versus paired trucks/bogies/platforms
- wheel orientation and spin axis
- stance width/length
- suspension/compression
- body lean and turn banking
- gait-to-wheel synchronization
- jump/airborne contact release
- grind contact points
- wall-ride/vertical contact behavior
- baby scale/offset rules
- per-vehicle compatibility

Use common sense instead of forcing one visual rule on every species:

- a biped can naturally use two foot skates;
- a horse/wolf/cow/fox and other quadrupeds can use four appropriately scaled skate contacts or a species-specific four-contact rig;
- a Spider/Cave Spider can use an eight-contact or intelligently grouped multi-leg skate rig that preserves its arachnid movement silhouette rather than pretending it has two feet;
- Bees and other tiny/insect-like mobs can use compact multi-contact gear, tiny wheel pods, or a board/hover solution when individual skates would be unreadable;
- Slimes/Magma Cubes should use their actual underside/contact plane, a springy skate platform, board, or hover contact rather than invented fake legs;
- a legless or anatomically incompatible creature should use a board/hover/contact-platform solution or reject foot-skates gracefully rather than clipping two shoes onto nowhere.

Animation and movement must respect the creature's existing silhouette. Wheels should stay close to contact surfaces, legs should not visibly pass through boards/skates, body lean should follow speed/turns, airborne tricks should release ground constraints, and grind/wall transitions should use the correct contact rig. Curated adapters can raise fidelity for popular vanilla/modded mobs, but the generic fallback must remain crash-safe and source-code-free.

The same Ground Contact / Ride Rig is also the compatibility seam for modded animals. An Aether quadruped, Twilight Forest creature, or unknown mod animal should first receive a generic contact solution from geometry/pose metadata; only then should an optional curated adapter improve exact bone anchors. If safe anchors cannot be determined, use a non-invasive fallback or disable that gear type for that mob—never mutate the source renderer/model to force it.

## 0A.7 Compatibility adapters are optional capabilities, never dependencies

Generic compatibility should operate against stable Minecraft/Forge concepts such as registry IDs, `Mob`, capabilities/data, tags, events, and rendering hooks. Curated support can improve unusual models/behaviors through isolated adapters.

An adapter must satisfy all of these:

- detect its target mod by ID/version before use
- avoid loading target classes when absent
- use supported public APIs where available
- keep all target-specific logic isolated from the base mod
- catch unsupported-version capability mismatches and disable only the adapter
- never prevent JetSetCraft from starting because an optional integration failed
- never prevent the source mod from behaving normally when JetSetCraft functionality is inactive

## 0A.8 Lightweight by default

Compatibility and rarity are also performance requirements.

- No per-tick full registry scans.
- No per-tick global mob scans.
- No expensive terrain analysis until a real challenge/rare encounter needs it.
- Cache registry/tag/adapter capability results.
- Keep dormant mobs free of JetSetCraft competition AI work.
- Activate high-level gang AI only for active participants.
- Tear down transient **challenge-only** state completely when the challenge ends, while preserving persistent Street Gear, gang affiliation, and equipment-bound Rider/Gang Form until the gear itself is actually removed.
- Bound event population, path searches, paint tracking, and arena analysis.

A large modpack that never starts a gang event should pay essentially only tiny bookkeeping/detection cost.

## 0A.9 Compatibility is an acceptance gate

Before considering any gang feature complete, verify at minimum:

1. Vanilla mob with no gear behaves exactly as before.
2. Give that same mob supported Street Gear → JetSetCraft behavior activates and the mob remains gangified after the event ends.
3. Save/restart and chunk-unload/reload while the gear remains equipped → the same mob remains in Rider/Gang Form with its stable `gang_id` and JetSetCraft-owned state intact.
4. Actually remove/steal/break/unequip the gear → the same UUID/entity returns to source behavior with preserved non-JetSetCraft data.
5. Verify at least one **native acquisition** path (for example Fox/Allay/source-owned pickup), one **dropped-gear walk-over** path, one **direct player equip** path, and one **dispenser/redstone equip** path without replacing source AI.
6. Verify a biped, a quadruped, and a non-biped/multi-leg or body-contact creature render/move with sensible species-aware contact rigs and no forced humanoid assumptions.
7. Craft/place/use the Boombox in a base-only JetSetCraft installation.
8. Natural encounters remain rare and do not alter ordinary spawn tables/population behavior; any surviving naturally gangified mob keeps its gear/affiliation after event cleanup.
9. Install a supported optional mod → its mob can be gangified without replacing the source entity.
10. Remove that optional mod from a separate test instance → JetSetCraft still starts normally.
11. Install an unknown creature mod → unsupported details degrade gracefully rather than crashing.
12. Run a large mixed modpack → no known JetSetCraft-caused compatibility regression is accepted for release.

**When compatibility and a flashy feature conflict, preserve compatibility and redesign the flashy feature without dropping the intended gameplay.**

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

## 1.3 Street Gear is the only transformation trigger, and the result persists

A compatible mob becomes a JetSetCraft rider/gang participant only after receiving actual supported **Street Gear** (inline/quad skates, skateboard, BMX, scooter, hoverboard, or future trick equipment) through JetSetCraft's equipment attachment. **Challenge selection alone must never gangify a normal mob.**

Gang Skates are the flagship mob-friendly form, but the architecture must not hardcode the transformation system to skates alone.

While the gear remains equipped, the creature may persistently have:

- gang membership / stable `gang_id`
- species-aware ride gear visuals
- skating/riding locomotion support
- grinding and transfer ability
- tricks
- graffiti/spray behavior
- competition AI capability when an event is active
- gang role
- reputation/faction participation
- gang clothing/accessories
- cinematic entrance capability
- music/beat metadata association

**Ending an event does not remove any of the above persistent equipment-owned identity.** It only stops the temporary event controller/objective state.

Only actual gear removal/stolen gear/breakage/unequip clears the equipment-bound Rider/Gang Form. JetSetCraft then relinquishes its persistent gang/rider layer cleanly and the same source entity resumes ordinary source-owned behavior.

Do not require a vanilla armor slot. JetSetCraft needs its own lightweight gang-equipment attachment so spiders, bees, slimes, quadrupeds, modded creatures, and other unusual models can participate without altering their source classes.

Acquisition should feel native: observe source-owned pickup/held-item behavior where it already exists, let eligible mobs physically walk over JetSetCraft dropped gear, support direct player equip, and register additive dispenser behavior for JetSetCraft gear. See **0A.6a**.

Rendering/movement must use a species-aware Ground Contact / Ride Rig rather than a humanoid-foot assumption. See **0A.6b**.

## 1.4 Original AI remains the base personality

Outside a JetSetCraft event, the creature should remain recognizable as itself.

A gangified Bee should still feel like a Bee. A gangified Enderman should still feel like an Enderman. A gangified Aether creature should retain whatever makes that creature unique.

During a competition, JetSetCraft may temporarily take higher-level movement/objective control for skating, racing, painting, dancing, trick lines, spray combat, and entrances. When the event ends, it yields the **challenge controller** cleanly to the original behavior, but the mob stays gangified/equipped if its Street Gear is still present. Persistent gang equipment and transient event control are deliberately separate states.

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

## 3.2 Shipped entrance stingers

v0.3.0 packages eighty distinct original 3.2-second entrance stingers. `tools/generate_audio.py` synthesizes them
without third-party samples, `tools/audio_manifest.json` records exact hashes/duration/PCM measurements, and asset
validation rejects silence, corruption, truncation, or undeclared files.

Suggested path:

`assets/jetsetcraft/sounds/music/gangs/`

Example stable sound paths:

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

Future full-length music systems can extend the stable IDs with metadata such as:

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

Resource packs can replace the original stingers while retaining the same gang and sound IDs.

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

## 8.1 Natural Hangouts are soft territories, not generated bases

A Natural Hangout should feel like a gang has **adopted a place that Minecraft already generated**, not like JetSetCraft carved a new structure into the world.

Each discovered hangout creates a compact persistent record such as:

- stable `site_id` / chapter ID
- parent `gang_id`
- dimension + anchor position
- soft home radius / activity radius
- discovery state
- resident roster references
- lightweight chapter mood/affinity
- furniture/dressing layout seed
- a few bounded notable-history flags
- last meaningful interaction / last visit
- validity state if the terrain later changes

The record itself owns **no chunk ticket** and has no ticking controller while its chunks are unloaded. A thousand discovered sites in distant unloaded chunks should be mostly disk/state cost, not simulation cost.

A hangout radius is an influence field, **not land ownership**. It must never:

- prevent vanilla/modded spawning
- prevent structure or terrain generation
- block player building
- claim/protect chunks unless a server explicitly enables a separate optional rule
- force-load chunks
- reserve biome space
- stop another mod from using the area

If a player or another mod substantially changes the terrain, JetSetCraft should re-score the site and gracefully shrink, redress, migrate, or retire it rather than restoring old terrain.

## 8.2 Natural resident crews persist; event casts do not

There are two deliberately different actor lifecycles.

### Natural residents

Ultra-rare Natural Hangouts have a tiny resident crew—normally a small handful, not a crowd. Residents:

- are the original vanilla/mod-owned entity type
- wear real JetSetCraft Street Gear
- stay gangified while that gear remains equipped
- are associated with one `site_id`
- use a **soft home** behavior to hang around the site
- can idle, skate short loops, emote, practice tricks, interact with props, or react to nearby players
- do not require the chunk to remain loaded
- do not run full competition AI when no challenge is active
- return toward home through ordinary pathing when practical, never rubber-band teleport merely to protect the site

Do not create a permanent high-cost "town NPC" simulation. When a resident's chunk unloads, normal Minecraft chunk/entity serialization should make it cost no live AI time. JetSetCraft must not hold a chunk ticket merely because a gang lives there.

If a resident is legitimately transported far away by gameplay or another mod, prefer a clean state transition such as becoming a roaming member or detaching from the chapter rather than repeatedly forcing it back and fighting other mods.

### Event-only cast

Boombox challenges and scripted street events may spawn an explicit ephemeral cast. These mobs:

- are marked as `event_actor` from creation
- exist for competition presentation/gameplay, not as permanent residents
- inherit the target `gang_id` and optionally a relevant `site_id` for scoring/reputation
- do not create a new permanent hangout simply because they appeared
- after results/rewards, perform a stylish exit where possible—grind away, skate down a street, jump a ledge, enter a portal, etc.
- despawn only after leaving the player's meaningful view/interaction range or after a safe bounded cleanup timeout
- never remain forever because pathfinding could not find a cinematic exit
- should not leave normal mob loot/Street Gear behind merely because event cleanup removed the cast, unless that challenge explicitly awards it

This keeps Boombox events spectacular without steadily filling the save with permanent AI entities.

## 8.3 Adaptive micro-furniture: decorate what exists, never rewrite generation

Hangout dressing should be **post-generation, additive, tiny, reversible, and geometry-aware**. JetSetCraft should score the already-generated environment and choose a small layout archetype such as:

- rail-side hang
- alley/wall hang
- rooftop hang
- cave-mouth hang
- village-edge hang
- bridge/underpass hang
- forest clearing hang
- beach/waterfront hang
- Nether ledge hang
- dimension/mod-specific safe archetypes supplied by optional adapters

Furniture/props can include things like a Boombox, crates, folding seats, cones, boards, a graffiti practice panel, small lights, banners, a tool pile, or a trick marker—but the placement solver must obey strict rules:

1. **Never replace a solid vanilla/mod block just to make the hangout fit.**
2. Place only in safe air/replaceable positions and against detected floors/walls.
3. Never carve terrain, flatten ground, cut trees, reroute fluids, or modify a structure template.
4. If the full dressing does not fit, degrade to a smaller layout; if nothing fits, the gang can simply hang there with no furniture.
5. Prefer non-ticking static JetSetCraft prop blocks/models. Avoid one block entity or entity per decorative object.
6. Interactive props should be rare and bounded; static visual dressing should cost essentially nothing while loaded.
7. Keep a placement manifest so JetSetCraft can remove only **its own** dressing if a site retires, without touching player/vanilla/mod blocks.

This means world generation remains exactly source-owned: JetSetCraft discovers a finished place and lightly dresses empty space afterward.

## 8.4 Reputation belongs to the gang/chapter, never individual disposable mobs

Do **not** create a separate reputation table for every spawned gang mob. That is noisy for gameplay and wasteful for saves.

Use a hierarchy:

### Canonical gang reputation

`player UUID + gang_id -> GangReputation` remains the primary durable progression. It controls Friendly/Member/Veteran/etc., unlocks, betrayal/reconciliation, gang naming rights, and the broad way that gang treats the player everywhere.

### Lightweight local chapter memory

A Natural Hangout may optionally maintain a tiny `site_id`/chapter affinity record so a specific hangout can remember major local history without becoming a second full RPG reputation system. Examples:

- discovered
- first challenge won/lost
- defended from rival
- vandalized/betrayed
- helped repeatedly
- local champion defeated
- currently welcoming / annoyed / hostile

Keep this bounded: a small numeric bias plus a handful of notable flags is enough. Do not store an unbounded interaction log. The parent gang reputation remains the source of truth.

### Shared reputation transaction

All members of one event cast or one Natural Hangout chapter share the same reputation target. Beating, helping, betraying, or impressing five members should resolve into one balanced **challenge/interaction transaction**, not five duplicated per-NPC reputation awards.

For an event away from any Natural Hangout, reputation goes directly to `gang_id`. For an event associated with a hangout, apply the normal gang reputation result plus an optional small chapter-memory update.

This produces the feeling that **the crew remembers you** without tracking friendship meters on disposable mobs.

## 8.5 Shared Hangout Brain keeps idle crews cheap

Idle residents do not each need expensive strategic gang AI. Use one lightweight site-level coordinator while the hangout is active/loaded.

The Hangout Brain can run at low frequency and assign cheap intents such as:

- stand/sit at a prop anchor
- skate a short cached loop
- practice one trick
- watch another member
- dance/emote
- graffiti on the JetSetCraft practice surface
- greet/taunt a nearby player based on reputation
- return toward the soft home radius

Individual mobs then use ordinary local steering/navigation. Full route planning, team roles, paint strategy, intercept logic, and competition intelligence activate **only when a real challenge begins**.

Performance invariants:

- no force-loaded gang chunks
- no global per-tick scan for hangouts
- activate a site only when a player enters a bounded activation radius
- use hysteresis: deactivate farther away than the activation distance so borders do not thrash
- small resident cap per Natural Hangout
- bounded number of simultaneously active hangouts per dimension/server
- zero strategic AI for unloaded sites
- no ticking decorative furniture
- cache local route/anchor analysis instead of rescanning every tick
- if the performance budget is exceeded, reduce ambient animation/decision frequency before reducing gameplay correctness

## 8.6 Hangout lifecycle

A clean lifecycle is:

1. **Candidate** — cheap rarity gate succeeds.
2. **Validate** — score existing terrain/space without modifying it.
3. **Create record** — assign `site_id`, parent `gang_id`, anchor, radius, roster budget, and dressing seed.
4. **Dress** — place only safe JetSetCraft-owned additive props in empty/replaceable space.
5. **Resident arrival** — create/select a tiny original-type crew and equip real Street Gear.
6. **Dormant** — chunks unloaded; no JetSetCraft live simulation.
7. **Active idle** — player nearby; low-frequency Hangout Brain only.
8. **Challenge** — temporary high-level competition controller takes over participating residents/event actors.
9. **Post-event** — residents return to cheap hangout life; event-only actors perform exits and despawn.
10. **Revalidate** — if terrain/player construction makes the site invalid, shrink, migrate, or retire it without restoring/replacing source blocks.
11. **Retire** — remove only JetSetCraft-owned dressing, release site state, and leave Minecraft/other-mod terrain untouched.

## 8.7 High-reputation reward: Found Your Own Chapter

At a high configurable reputation tier with a gang—default target should be **Member/Veteran-level trust, not a trivial early unlock**—the player earns that gang's **Chapter Boombox**. This is a prestige reward that lets the player invite a real branch of the gang to live at a location the player chooses, including their own base.

The reward must be tied to the stable `gang_id`, not a hardcoded visible gang name. Each gang can provide its own themed item name, model, textures, particles, sound set, stickers, lights, antenna/equalizer behavior, and idle animations while sharing the same safe base implementation. Examples should be playful and species-aware: a Mooshroom gang can award a mushroom-covered **Shroom Box**, aquatic gangs can use water/bubble motifs, Bone Drones can use skeletal speaker cages, and so on. These are data-driven skins/models over one compatible Chapter Boombox contract rather than bespoke code forks.

Placing the Chapter Boombox creates a **player-founded chapter site** with a stable `site_id` and that gang's `gang_id`. The Boombox itself is the home anchor. It does **not** create a world-generation structure, reserve chunks, replace blocks, or force-load the area. The player chooses the home by choosing where to place the earned Boombox.

Default behavior:

- create or invite a small permanent roster, approximately **five members by default** (configurable and server-budgeted);
- use original vanilla/mod-owned entity types selected through the Mob Archetype Resolver;
- equip actual JetSetCraft Street Gear so normal persistent gangification rules apply;
- give each resident a stable JetSetCraft `crew_member_id` independent from its current runtime entity UUID;
- bind each roster member to the Chapter Boombox `site_id` as its home;
- let residents idle, practice, interact, greet, pose, skate, dance, and use the same cheap Hangout Brain when not following the player;
- never keep the base chunk loaded merely because the player owns a chapter there.

The Chapter Boombox should be movable without erasing the crew. When the owner intentionally picks it up, preserve the `site_id`, roster, gang identity, customizations, and home data in durable JetSetCraft saved state/item data. Re-placing it safely relocates the chapter home. Never duplicate roster members during move/reload/recovery.

## 8.8 Named permanent crew, followers, and player posse

The five-or-so chapter residents are not disposable event actors. They are persistent named gang members the player can get to know.

Each roster entry may persist JetSetCraft-owned data such as:

- stable `crew_member_id`
- current/preferred source `entity_type` provider
- stable semantic `mob_archetype_id`
- display/custom name chosen by the player
- gang role / personality seed
- Street Gear/cosmetic loadout
- home `site_id` / Chapter Boombox
- current state: home, following, participating, recovering, missing-provider, roaming
- posse assignment
- bounded memorable interaction flags
- optional progression such as favorite trick/signature move, without becoming a giant RPG stat sheet

At sufficient trust/membership, the player can invite chapter residents into their **posse**. Posse members may follow the player through normal gameplay, ride/skate with them, participate in JetSetCraft events, pose/dance, help during allowed gang activities, and return home when dismissed.

Performance rules still win:

- no follower force-loading behind the player;
- no global per-tick search for missing posse members;
- use normal loaded-entity navigation first;
- use bounded, compatibility-safe catch-up/rejoin only when a follower becomes hopelessly separated;
- allow all chapter members to be eligible followers, but keep an adjustable active-posse budget for servers/modpacks that need it;
- members left at home use the low-frequency shared Hangout Brain rather than expensive follower AI.

The player's relationship is still primarily with the gang's `gang_id`. Naming a specific member or adding them to the posse personalizes the crew but does not create five separate global reputation tracks.

## 8.9 Home-anchor recovery: defeated members come back to their Boombox

The placed Chapter Boombox acts as the crew's **home/spawn anchor**, conceptually similar to a player's bed while remaining entirely JetSetCraft-owned. A permanent chapter resident or posse member should not be lost forever because it died to combat, lava, a boss, another mod, or an accident.

Use a durable **roster/body split**:

- `crew_member_id` is the persistent character identity;
- the currently spawned Minecraft/mod entity is that member's current body;
- when the body dies, JetSetCraft records the member as `recovering` and allows the source entity death lifecycle to complete;
- after a configurable recovery delay, the member may re-form/return near its bound Chapter Boombox;
- respawn/recovery occurs **only when the home chunk is naturally loaded**. Never issue a chunk ticket simply to resurrect a gang member;
- a recovered body uses the original registered entity type when it is still available and safe; otherwise the Mob Archetype Resolver may choose an equivalent provider as described below;
- the member keeps its player-facing name, `crew_member_id`, gang, role, posse assignment, Street Gear/cosmetics, and JetSetCraft memories after recovery;
- a new runtime entity UUID is acceptable after a true death because the stable `crew_member_id` is the character identity. Do not illegally reuse a dead entity UUID if doing so risks vanilla/mod compatibility.

Defeat does not always need literal death. JetSetCraft minigames can use a non-lethal `downed/resting` result and send the member home after the activity. Vanilla/mod combat deaths can use the full recovery path.

### No death-farm or duplication exploit

Home recovery must not duplicate Street Gear, inventory, or source-mob loot. For Chapter residents specifically:

- JetSetCraft-owned bound Street Gear/cosmetics belong to the roster and are retained for recovery rather than duplicated as death drops;
- source-mod/vanilla loot behavior should be preserved where reasonable, but repeated Chapter-member resurrection must not become an infinite source-mob farm;
- use narrowly scoped Forge death/loot events or a chapter-specific cooldown/accounting rule rather than modifying global loot tables or source mob classes;
- never suppress or rewrite unrelated mobs' drops.

If the Chapter Boombox is temporarily picked up/moved, recovering members wait in roster state until the same chapter anchor is safely placed again. If the provider for a member's mob archetype is missing, the member waits in a non-corrupt `missing-provider` state rather than crashing or being silently deleted.

## 8.10 Super-intelligent Mob Archetype Resolver: concepts survive provider/mod changes

JetSetCraft must not equate a creature concept with one specific mod namespace. This is essential for creatures such as **Mooblooms**, where the available implementation may come from one backport mod today, a different mod tomorrow, a replacement fork, a future official/backported implementation, or multiple providers installed at once.

Introduce a stable semantic identity separate from registry identity:

- `mob_archetype_id` = what the creature **is** to JetSetCraft, e.g. `jetsetcraft:moobloom`;
- `entity_type_id` = which installed mod currently provides the physical creature, e.g. some provider's `namespace:moobloom`;
- `gang_id` = which gang/crew definition the archetype maps to.

The resolver should use a layered confidence system instead of brittle one-mod hardcoding. Recommended resolution order:

1. **Explicit datapack/Forge tags and server overrides** — strongest source of truth, e.g. providers opt into `#jetsetcraft:archetypes/moobloom`.
2. **Curated provider aliases/adapters** — known registry IDs from supported mods/backports mapped to the semantic archetype, version-gated and optional.
3. **Normalized registry/translation-key/name matching** — case/underscore/hyphen/plural normalization plus synonym tables such as `moobloom`, `moo_bloom`, and known localized-independent registry aliases.
4. **Safe trait/family evidence** — base family, tags, dimensions/age capability, passive/hostile disposition, cow-like family markers, public capabilities, or other server-safe metadata that strengthens/weakens a candidate without instantiating arbitrary mobs or touching renderer internals.
5. **Modpack/user alias definitions** — allow creators to declare that a completely differently named entity should satisfy an archetype.
6. If confidence is insufficient, mark the archetype unresolved instead of guessing and corrupting a save.

Do not rely on one Java class, one namespace, one mod filename, or a texture/model inspection to identify an archetype. Do not hard-load provider classes when absent. Keep the resolver server-safe and cache results after registry load; it must not scan the whole registry every tick.

### Multiple providers installed

If more than one installed entity satisfies an archetype:

- choose deterministically using explicit server priority first, then curated confidence/compatibility score;
- expose the selected provider in the Mob Atlas/debug UI;
- allow the server/modpack to pin a preferred provider;
- never randomly swap an already-living member's entity type.

### Provider changes across saves

Persist both the stable `mob_archetype_id` and the last concrete provider. If that provider later disappears:

- living entities from that missing provider are naturally handled by Minecraft/the source mod removal process; JetSetCraft must not fabricate unsafe NBT conversions;
- roster/hangout records remain valid because they key on the stable archetype, not just the missing registry ID;
- on the next safe spawn/recovery, the resolver may bind the member to another installed provider satisfying the same archetype;
- preserve JetSetCraft-owned identity (`crew_member_id`, display name, gang, gear, role, memories), while never pretending provider-specific state from an absent mod can be reconstructed;
- if no safe provider exists, keep the member/archetype dormant and recoverable until one is installed.

This same system should generalize beyond Mooblooms to any creature concept with multiple implementations/backports: similar butterflies, sharks, fireflies, copper golems, rascal-style mobs, old-vote mobs, variants, or future mobs from competing creature packs.

## 8.11 Acceptance tests for territories, player chapters, roster recovery, and archetype resolution

At minimum verify:

1. Discovering a Natural Hangout does not change biome/structure generation or replace any source block.
2. A Natural Hangout can exist in an unloaded chunk without keeping that chunk loaded.
3. Natural resident members remain associated with their `site_id` after a challenge and after save/reload while their gear remains equipped.
4. An idle loaded hangout stays within the configured resident/AI performance budget.
5. A Boombox event away from a hangout spawns an ephemeral event cast that exits/despawns cleanly after the event and does not create a permanent site.
6. One event produces one balanced gang/chapter reputation transaction, not multiplied per spawned actor.
7. A specific Natural Hangout can remember a small amount of local history while global `gang_id` reputation remains canonical.
8. Furniture placement never replaces solid source/player blocks; a constrained site gracefully uses fewer/no props.
9. Destroying/building through a hangout causes revalidation/migration/retirement rather than terrain restoration.
10. Many discovered but unloaded hangouts have negligible live tick cost.
11. Reaching the configured high reputation tier awards/unlocks that gang's Chapter Boombox without changing the stable `gang_id`.
12. Placing the Chapter Boombox at a player-selected base creates one bounded player-founded chapter with roughly five persistent roster members and no structure/worldgen/chunk-ticket side effects.
13. Picking up and relocating the Chapter Boombox preserves `site_id`, roster identities, names, and posse assignments without duplicating members.
14. The player can name roster members and recruit/dismiss them as posse followers while home residents stay on the cheap Hangout Brain.
15. Killing a permanent chapter/posse member marks its roster entry recovering; after the delay it returns at its home Boombox when that chunk is naturally loaded, with name/role/gear/posse identity intact.
16. Recovery does not duplicate Street Gear, inventory, or create an infinite repeatable vanilla/modded mob loot exploit.
17. Breaking/moving the home Boombox while a member is recovering leaves the roster safely pending until the chapter anchor is placed again.
18. A `jetsetcraft:moobloom` archetype resolves correctly when provider A is installed, remains save-stable when that provider disappears, and can safely rebind on a later spawn/recovery when provider B supplies an equivalent Moobloom.
19. Two simultaneous Moobloom providers resolve deterministically according to tag/server-priority/curated-confidence rules and never cause random provider swapping for a living member.
20. An unresolved archetype becomes dormant with a useful diagnostic rather than guessing, crashing, or deleting gang/roster state.

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

If a later release adds junior-specific arrangements, it can extend the adult stinger IDs with metadata for:

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
6. Giving an Aether mob appropriate Street Gear activates **persistent equipment-bound** JetSetCraft state on the same Aether-owned entity; finishing an event does not remove it.
7. Removing/stealing/unequipping the Street Gear returns that creature to normal Aether behavior.
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
street_gear_slots[]
is_gangified_persistent
street_gear_acquisition_source
active_challenge_id_optional
original_state_snapshot_if_needed
ai_mode
movement_adapter
ground_contact_ride_rig_id
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

## 34.1 Persistent equipment-bound vanilla transformation

Normal vanilla mob  
→ acquire/equip JetSetCraft Street Gear  
→ species-appropriate gang equipment renders  
→ gang/rider behavior activates  
→ mob participates in a minigame  
→ minigame ends  
→ **mob remains gangified because the gear is still equipped**  
→ unload/reload the chunk and save/restart  
→ same entity still has the same `gang_id` / JetSetCraft gear state  
→ actually remove/steal/break/unequip the Street Gear  
→ same entity returns to normal source behavior/data.

Verify UUID and all important source-owned state are preserved. Verify event cleanup never strips persistent gang gear/state.

## 34.1a Natural Street Gear acquisition

Verify multiple additive equipment paths:

- Fox acquires compatible JetSetCraft gear through its normal pickup/steal behavior → JetSetCraft observes the pickup and equips without replacing Fox AI.
- Allay receives or naturally picks up compatible gear according to its normal held-item/matching-item behavior → JetSetCraft equips without broadening Allay pickup rules.
- A mob with no normal pickup ability physically runs into a dropped JetSetCraft Street Gear item → the **gear item** performs the bounded eligibility/contact check and equips it, without a global mob scan.
- Player directly uses compatible gear on a mob → equip succeeds.
- Dispenser points at an eligible mob and dispenses JetSetCraft gear → additive registered dispenser behavior equips it; no vanilla block/class patch is used.

Then steal/remove the gear and verify the original mob immediately returns to source-owned behavior.

## 34.1b Species-aware skating and ride-contact rigs

Verify at minimum:

- one biped with two-contact skating;
- one quadruped (for example Fox/Wolf/Cow/Horse) with a natural four-contact or species-specific rig;
- Spider/Cave Spider or another multi-leg mob with a sensible multi-contact/grouped rig preserving its silhouette;
- Slime/Magma Cube or another body-contact creature using underside/platform/board/hover logic rather than fake feet;
- one baby/juvenile with scaled contact anchors;
- one optional-mod animal using the generic fallback plus a curated adapter when available.

Check wheel/contact placement, no obvious clipping, turning/body lean, jump release, grind contact, wall/vertical transitions where supported, and graceful fallback/rejection when anatomy cannot safely use a gear type.

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
→ naturally/directly equip compatible JetSetCraft Street Gear  
→ persistent Gang Form works on the same Aether-owned entity  
→ finish a challenge and verify the mob remains gangified while gear remains equipped  
→ remove/steal/unequip the gear  
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

# 35. Reference implementation order — complete vertical slices

Each release slice must include its runtime, persistence, tests, assets, and truthful documentation.

A strong development order is:

1. Finalize stable `gang_id` / GangDefinition / relationship data model.
2. Implement **persistent equipment-bound** GangMemberAttachment on existing mobs; event lifecycle must never clear gang state while Street Gear remains equipped.
3. Implement Street Gear attachment, natural acquisition/removal paths (source-owned pickup, Fox/Allay handling, dropped-gear collision, direct interaction, dispenser/redstone), and species-aware Ground Contact / Ride Rig anchors for biped, quadruped, multi-leg, body-contact, juvenile, and modded creatures.
4. Implement Boombox block/entity, empty random targeting, and target slot.
5. Implement original entrance audio plus exact validation metadata.
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
