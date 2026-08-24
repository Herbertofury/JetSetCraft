# JetSetCraft Mob Head → Gang Compatibility

Status: active compatibility foundation on `automation/head-gang-compat-0.3`.

## Goal

Any trustworthy mob head, skull, emblem, or compatible head-equivalent should be usable as the Boombox Gang Target without JetSetCraft taking ownership of the source mod. The compatibility contract is provider-agnostic and fail-closed:

1. Vanilla skull/head identity wins when present.
2. Explicit JetSetCraft adapter metadata can map any head item to a concrete `entity_type` and optional `gang_id`.
3. Common `*_head`, `*_skull`, `head_*`, and `skull_*` registry conventions are resolved conservatively against the live entity registry.
4. Ambiguous player-head texture payloads are never guessed from skin pixels or display names. They require an explicit compatibility mapping.
5. Missing optional mods never create classloading failures.

The implementation lives in `HeadGangTargetResolver`.

## Current canonical mappings

- `minecraft:zombie` → `jetsetcraft:dead_beat`
- `minecraft:skeleton` / `minecraft:wither_skeleton` → `jetsetcraft:bone_drones`
- `minecraft:creeper` → `jetsetcraft:creepaku_gouji`
- `minecraft:spider` / `minecraft:cave_spider` → `jetsetcraft:arachnaphobia`
- `minecraft:witch` → `jetsetcraft:hex_appeal`
- `minecraft:piglin`, `minecraft:piglin_brute`, `minecraft:zombified_piglin` → `jetsetcraft:gold_rush`
- every other safely-resolved entity gets a stable generated gang identity under `jetsetcraft:mob/<namespace>/<entity>` until a curated gang overrides it.

## Compatibility targets researched 2026-08-23

### Tier S — build/test first

- **All The Heads** — https://www.curseforge.com/minecraft/mc-mods/all-the-heads
  - Current project is Fabric/NeoForge and advertises 450+ fully data-driven mob/variant heads.
  - The supplied `AllTheHeads-v26.2.2-mc26.2.x-NeoForge.jar` was inspected directly.
  - Current builds use one `alltheheads:mob_head` item plus an `alltheheads:head_type` data component, e.g. `alltheheads:minecraft/bee`.
  - This is an excellent future-version adapter target, but the supplied 26.2 NeoForge binary cannot be loaded into JetSetCraft's current Forge 1.20.1 runtime. Keep the adapter isolated and version-aware rather than introducing a hard dependency.

- **Heads** by Mrbysco — https://www.curseforge.com/minecraft/mc-mods/heads
  - One of the largest relevant Forge projects; CurseForge reports millions of downloads.
  - Has a dedicated Forge 1.20.1 build (`Heads-1.20.1-1.1.3.jar`).
  - Priority live-modpack compatibility target for the current JetSetCraft platform.

- **Mobs Heads** — https://www.curseforge.com/minecraft/mc-mods/mobs-heads
  - Provides a Forge 1.20.1 build (`mobs_heads_forge_1.20.1_v1.8.jar`).
  - Priority compatibility target for current Forge packs.

- **More Mob Heads Mod** — https://www.curseforge.com/minecraft/mc-mods/moremobheads
  - Forge 1.20.1; roughly 463 vanilla mob/variant heads.
  - Strong current-platform test target because every vanilla mob and variants are represented.

### Tier A — player-head/datapack bridge

- **Mob Heads** — https://modrinth.com/datapack/mob-heads
  - 500+ unique heads, including babies and variants, with a 1.20.1-compatible datapack line.
  - Uses custom/player-head style content, so JetSetCraft should consume explicit adapter metadata rather than infer identity from textures.

- **Better Mob Heads** — https://modrinth.com/datapack/better-mob-heads
  - 1.20.x support and custom `player_head` textures.
  - Same explicit-mapping bridge applies.

- **More Heads** — https://www.curseforge.com/minecraft/mc-mods/more-heads
  - Modern multi-loader/server-side head collection ecosystem.
  - Useful future-version compatibility and datapack reference even where the exact current file differs from Forge 1.20.1.

## All The Heads 26.2 reference findings

Direct inspection of the user-supplied JAR showed:

- item id: `alltheheads:mob_head`
- identity component: `alltheheads:head_type`
- sample Bee identity: `alltheheads:minecraft/bee`
- advancement/data paths expose species and variant IDs such as `minecraft/bee`, `minecraft/bee_angry`, `minecraft/axolotl_blue`, etc.
- the mod is built around data-driven head definitions and has broad variant coverage.

JetSetCraft should preserve the exact variant token for Atlas/discovery/cosmetics while resolving the base source entity (`minecraft:bee`, `minecraft:axolotl`, etc.) for gang spawning. Variant identity must never be discarded if the provider exposes it.

## Premium Boombox behavior contract

When the physical Boombox target slot is implemented/connected to this resolver:

1. Insert head.
2. Resolver produces source entity + stable gang ID.
3. Boombox visibly retunes to the gang (name, emblem, color, music slot).
4. Challenge builder validates that the source entity type is currently registered and allowed.
5. Gang actors are created using the original source entity type, then receive JetSetCraft Street Gear and gang state.
6. Variant metadata is preserved where the provider exposes it.
7. Unsupported/ambiguous heads remain in the slot and show a useful `No safe gang mapping` state; they are never consumed or guessed.
8. Removing the head clears targeting cleanly.

## Next verification matrix

- Vanilla Zombie, Skeleton, Wither Skeleton, Creeper, Piglin and Dragon heads.
- Heads 1.20.1 live JAR.
- Mobs Heads 1.20.1 live JAR.
- More Mob Heads 1.20.1 live JAR.
- Player-head datapack sample using explicit `jetsetcraft:target_entity` metadata.
- All The Heads 26.2 adapter on a future NeoForge/current-Minecraft JetSetCraft port; keep its current data-component identity contract recorded now.
- Unknown head mod with `*_head` item path to prove generic registry fallback.
- Ambiguous player head to prove fail-closed behavior.

## Compatibility principle

A head provider supplies identity; JetSetCraft supplies gang gameplay. JetSetCraft must never copy or replace the provider's entity classes, rewrite its loot tables, or require it as a dependency merely to support the Boombox.
