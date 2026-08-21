# Standalone Compatibility Covenant

JetSetCraft must remain a **truly standalone, additive Forge mod**. This requirement outranks implementation convenience for gangs, the Boombox, natural encounters, the Mob Atlas, reputation, trick vehicles, AI, graffiti, music, rewards, and optional-mod integrations.

The target is **zero known compatibility issues introduced by JetSetCraft**. Any reproducible conflict caused by JetSetCraft is a release-blocking bug to fix at the root, not an acceptable tradeoff. Unknown future integrations must fail closed: skip the optional enhancement, preserve the original game/mod behavior, log a useful diagnostic, and keep the world playable.

## Never take ownership of vanilla or another mod

JetSetCraft must not normally:

- replace or registry-override vanilla entity types;
- replace another mod's entity types;
- edit Mojang/vanilla source code;
- copy vanilla mob classes and substitute JetSetCraft versions;
- use Mixins, ASM/coremods, bytecode rewriting, or access-transformer hacks merely to make Gang Form work;
- globally delete or replace vanilla AI goals/navigation;
- globally rewrite a species' attributes;
- replace vanilla or third-party spawn tables;
- take over vanilla loot tables just to support gang selection;
- hard-classload optional mods;
- make Aether, Twilight Forest, Create, creature mods, combat mods, animation mods, or any other compatibility target required dependencies.

If a desired feature appears to need an invasive patch, **redesign the JetSetCraft layer first**.

Minecraft owns Minecraft mobs. The Aether owns Aether mobs. Twilight Forest owns Twilight Forest mobs. Every mod owns its own entities and behavior. JetSetCraft owns only the optional street-culture layer it attaches through its own state, items, controllers, rendering, data, recipes, tags, and safe public Forge/API extension points.

## Normal worlds stay normal

Installing JetSetCraft should not noticeably rewrite ordinary Minecraft simulation. With no JetSetCraft Street Gear equipped and no JetSetCraft event active:

- vanilla mobs behave normally;
- modded mobs behave normally;
- vanilla spawn distributions remain normal;
- vanilla and modded AI remain source-owned;
- dimensions and structures remain source-owned;
- combat and loot remain normal;
- dormant mobs do not run JetSetCraft competition AI.

A player who ignores the gang system should still have an ordinary compatible Minecraft/modpack world.

## Ultra-rare natural gangs, not spawn-table takeover

Natural gang encounters are a separate JetSetCraft-owned encounter layer and should be **ultra rare**.

Do not turn every mob spawn into a gang roll and do not inject large gang weights into normal biome spawn lists. Use a lightweight encounter scheduler with a cheap rarity gate first. Only after the rarity gate succeeds should JetSetCraft consider terrain, space, biome, dimension, nearby rails, structures, and eligible entities.

When a natural encounter is created, either use safe existing eligible mobs or spawn the **original registered entity type** through normal public APIs, then attach JetSetCraft-owned Street/Gang state. The entity remains the same vanilla/mod-owned type.

Encounter caps must prevent population inflation and runaway AI costs. If a location, entity, optional mod, or adapter is unsafe or unsupported, simply skip the encounter.

Natural encounters are surprise flavor. The dependable gameplay entry point is the Boombox.

## Craftable Boombox is the reliable event initializer

JetSetCraft must ship a normal survival **Boombox crafting recipe using only vanilla ingredients by default**. The recipe should be data-driven so modpacks can override it, but base JetSetCraft must never require another mod's material to access the gang system.

Once crafted and placed:

- empty target slot → start an appropriate random available gang event;
- mob head/emblem/token inserted → target that mob's gang;
- absent optional mod → its gangs simply are not eligible;
- unsupported/unsafe target → fail cleanly with useful feedback and modify nothing unrelated.

The Boombox gives every player a deterministic, standalone way to access gang content while natural encounters can remain properly rare.

## Street Gear Activation: the same original mob gets the tricks

Reversible mob transformation is not limited to skates. One generalized **Street Gear Activation** contract should support:

- inline skates;
- quad skates;
- skateboards;
- BMX bikes;
- scooters;
- hoverboards;
- future JetSetCraft trick vehicles/equipment.

Giving/equipping/assigning supported Street Gear to a compatible mob may attach the minimum JetSetCraft-owned rider/gang state needed to ride, trick, grind, dance, spray, or compete.

Conceptually:

**Untouched source mob + Street Gear = temporary JetSetCraft Rider/Gang Form**

**Remove Street Gear = source-owned behavior restored**

This should be a state transition on the **same original entity** whenever technically possible, not a despawn-and-replace trick. Preserve UUID, health, age, owner/tame data, custom name, variants/genetics, inventories, existing equipment, persistent data, capabilities, attachments, and mod-specific state JetSetCraft does not own.

If a creature cannot safely use a specific ride because of anatomy/model/navigation constraints, use a safe adapter/fallback or reject that gear gracefully. Never mutate the source mob class just to force support.

## Optional adapters are capabilities, never dependencies

Generic compatibility should use stable Minecraft/Forge concepts such as registry IDs, `Mob`, tags, capabilities/data, events, rendering hooks, and JetSetCraft-owned controllers. Curated adapters may improve unusual models/behavior but remain isolated.

Every optional adapter must:

- detect its target mod/version before use;
- avoid loading target classes when absent;
- prefer supported public APIs;
- keep target-specific code isolated from the base mod;
- disable only itself when a capability/version mismatch occurs;
- never prevent JetSetCraft from starting;
- never prevent the source mod from behaving normally when JetSetCraft is inactive.

## Lightweight by default

Compatibility is also a performance rule:

- no per-tick full registry scans;
- no per-tick global mob scans;
- no expensive terrain analysis until a real challenge/rare encounter needs it;
- cache registry/tag/adapter capability results;
- dormant mobs do not run gang AI;
- active high-level gang AI exists only for actual challenge participants;
- transient event state is fully torn down when the event ends;
- event population, path searches, arena analysis, and paint tracking are bounded.

A large modpack that never starts a gang event should pay only tiny bookkeeping/detection cost.

## Release acceptance gate

Before a gang feature can be called complete, verify at minimum:

1. A vanilla mob with no Street Gear behaves normally.
2. Giving that same mob supported Street Gear activates JetSetCraft behavior.
3. Removing the gear restores the same entity's source behavior and preserves identity/data.
4. A base JetSetCraft install can craft, place, and use the Boombox without optional mods.
5. Natural encounters remain ultra rare and do not require spawn-table replacement or population inflation.
6. A supported optional-mod mob can be gangified without replacing its source entity.
7. Removing that optional mod from a separate test instance does not prevent JetSetCraft from starting.
8. An unknown creature mod degrades gracefully when details are unsupported.
9. A representative large mixed modpack has no known JetSetCraft-caused compatibility regression accepted for release.

**When compatibility and a flashy feature conflict, preserve compatibility and redesign the flashy feature without dropping the intended gameplay.**

See also [[Gang Wars, Boombox & Mob Atlas|Gang-Wars-Boombox-and-Mob-Atlas]] and [[Compatibility]].
