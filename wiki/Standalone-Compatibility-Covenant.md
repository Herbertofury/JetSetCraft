# Standalone Compatibility Covenant — Persistent Gear, Natural Equipping & Species-Aware Riding

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

1. A lightweight JetSetCraft encounter scheduler occasionally evaluates whether a rare event is appropriate.
2. It performs cheap checks first and does expensive terrain analysis only after the rarity gate succeeds.
3. The event either selects safe existing eligible mobs or spawns the original registered entity types through normal public APIs, then actually equips JetSetCraft Street Gear on those mobs through JetSetCraft-owned equipment state.
4. The original entity type remains untouched.
5. Encounter caps prevent population inflation or runaway AI cost.
6. If anything about the location/entity/optional mod is unsafe or unsupported, the encounter simply does not occur.

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

Natural encounter mobs follow the same rule: JetSetCraft may create an ultra-rare encounter by spawning/selecting the **original registered mob type and actually equipping JetSetCraft Street Gear on it**. If that mob survives the event and keeps its gear, it remains gangified afterward. Event cleanup may remove scoreboards, paint overlays, temporary navigation objectives, and challenge controllers—but not the mob's persistent gear or affiliation. Normal source-mob death/despawn rules still apply unless JetSetCraft has an explicit, narrowly scoped reason to opt that individual gang member into persistence.

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

See also [[Gang Wars, Boombox & Mob Atlas|Gang-Wars-Boombox-and-Mob-Atlas]] and [[Compatibility]].
