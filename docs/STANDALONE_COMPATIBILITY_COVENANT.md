# JetSetCraft Standalone Compatibility Covenant

JetSetCraft must remain a **truly standalone, additive Forge mod**. This requirement outranks implementation convenience for gangs, the Boombox, natural encounters, the Mob Atlas, reputation, trick vehicles, AI, graffiti, music, rewards, and optional-mod integrations.

The target is **zero known compatibility issues introduced by JetSetCraft**. Any reproducible conflict caused by JetSetCraft is a release-blocking bug to fix at the root, not an acceptable tradeoff. Unknown future integrations must fail closed: skip the optional enhancement, preserve the original game/mod behavior, log a useful diagnostic, and keep the world playable.

## Hard ownership boundary

Minecraft owns Minecraft mobs. The Aether owns Aether mobs. Twilight Forest owns Twilight Forest mobs. Every mod owns its own entities and behavior. JetSetCraft owns only the optional street-culture layer it attaches through its own state, items, controllers, rendering, data, recipes, tags, and safe public Forge/API extension points.

Do not replace or registry-override vanilla/modded entity types, copy-substitute source mob classes, globally replace AI/navigation/attributes, replace spawn tables, or make compatibility targets required dependencies merely to implement gang features. Do not use Mixins, ASM/coremods, or bytecode rewriting as the normal mob-compatibility path. If a feature appears to require invasive ownership, redesign the JetSetCraft layer first.

## Normal worlds stay normal

With no JetSetCraft Street Gear equipped and no JetSetCraft event active, ordinary vanilla/modded mobs, spawning, AI, dimensions, structures, combat, loot, navigation, and third-party behavior remain source-owned and unchanged. Dormant mobs do not run JetSetCraft competition AI.

## Ultra-rare natural encounters

Natural gangs are a separate lightweight JetSetCraft encounter layer and remain **ultra rare**. Do not turn every normal spawn into a gang roll or broadly modify biome spawn tables. Apply a cheap rarity gate first, then evaluate terrain/context only when a real event may happen. Use safe existing mobs or original registered entity types and attach JetSetCraft-owned gang state. Cap encounter populations and skip unsupported contexts without side effects.

## Craftable Boombox is the reliable entry point

Base JetSetCraft ships a survival **Boombox recipe using vanilla ingredients only by default**. The recipe is data-driven for modpack overrides. Empty target slot starts an appropriate random available event; a mob head/emblem/token targets that gang. Missing optional mods simply make their gangs ineligible. Natural encounters can therefore stay rare without making gang gameplay inaccessible.

## Street Gear Activation

Use one reversible Street Gear contract for inline skates, quad skates, skateboards, BMX bikes, scooters, hoverboards, and future trick equipment.

**Untouched source mob + Street Gear = temporary JetSetCraft Rider/Gang Form**

**Remove Street Gear = source-owned behavior restored**

Perform this transition on the **same original entity** whenever technically possible. Preserve UUID, health, age, tame/owner data, custom name, variants/genetics, inventories, existing equipment, persistent data, capabilities, attachments, and mod-specific state JetSetCraft does not own. If a creature cannot safely support specific gear, use a safe adapter/fallback or reject that equipment gracefully—never mutate the source mob class to force it.

## Optional adapters fail closed

Generic compatibility uses stable Minecraft/Forge concepts such as namespaced registry IDs, `Mob`, tags, capabilities/data, events, rendering hooks, and JetSetCraft-owned controllers. Curated adapters must detect their target mod/version, avoid classloading when absent, prefer public APIs, remain isolated, and disable only themselves on mismatch. An optional integration failure must never stop JetSetCraft or the source mod from loading normally.

## Lightweight by default

No per-tick full registry scans, no per-tick global mob scans, no expensive terrain analysis until a real event needs it, and no high-level gang AI for dormant mobs. Cache capability results, bound route/arena/paint work, cap event populations, and tear down transient challenge state completely after each event.

## Release gate

Before a gang feature is complete, verify:

1. vanilla mob without Street Gear behaves normally;
2. supported Street Gear activates JetSetCraft behavior on that same entity;
3. removing gear restores source behavior and preserves identity/data;
4. base JetSetCraft can craft/place/use the Boombox without optional mods;
5. natural encounters stay ultra rare without spawn-table takeover/population inflation;
6. supported optional-mod mobs can be gangified without entity replacement;
7. removing the optional mod from a separate test instance does not break JetSetCraft startup;
8. unknown creature mods degrade gracefully;
9. representative large modpacks ship with **zero known JetSetCraft-caused compatibility regressions**.

**When compatibility and a flashy feature conflict, preserve compatibility and redesign the feature without dropping the intended gameplay.**
