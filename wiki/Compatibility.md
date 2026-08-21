# Compatibility

JetSetCraft is designed to enter large adventure/combat modpacks without making those mods dependencies.

## Gangification and the installed-mod Mob Atlas

The gang system follows a strict ownership boundary: **JetSetCraft does not replace vanilla mobs or another mod's entities.** A normal mob remains the original registered entity. Actual JetSetCraft Street Gear is the persistent transformation trigger: once compatible gear is equipped, that same source mob remains a JetSetCraft rider/gang member across events, chunk unload/reload, and save/restart for as long as the gear remains equipped. Ending a race, Turf War, dance battle, or Boombox event removes only transient challenge state. Only actual gear removal, theft, breakage, or unequip restores the mob's ordinary non-gang state.

Challenge selection alone never silently gangifies a normal mob. Ultra-rare natural gang encounters must select/spawn the original registered entity type and actually equip JetSetCraft Street Gear on it. A surviving mob that keeps its gear keeps its gang affiliation after the event.

Street Gear acquisition should use additive, Minecraft-like paths rather than replaced AI: observe native pickup/steal behavior for mobs such as Foxes and item-capable mobs; preserve Allay held-item/matching-item behavior; allow eligible mobs to physically run into dropped JetSetCraft gear through a tiny gear-item-local contact check; support direct player equip; and register JetSetCraft-owned dispenser behavior so redstone can equip compatible mobs without patching vanilla dispenser or mob classes.

Animal riding must also be anatomy-aware. The shared **Ground Contact / Ride Rig** maps equipment to the creature's actual locomotion/contact points instead of assuming two humanoid feet: bipeds can use two contacts, quadrupeds can use four/species-specific contacts, spiders can use multi-leg/grouped contacts, tiny insects can use compact wheel pods, Slimes/Magma Cubes can use their underside/contact plane or a platform/board/hover solution, and babies scale their anchors correctly. Unknown modded creatures receive safe geometry/pose fallbacks first; curated adapters may improve exact bone anchors later. If a gear type cannot be represented safely, JetSetCraft falls back or refuses that gear for that mob rather than modifying its source renderer/model.

The runtime Mod Mob Atlas is designed to enumerate safe registered `Mob` entity types from installed namespaces. Unknown mods receive generic compatibility records; curated adapters can add premium names, model anchors, movement, rewards, entrances, baby/junior profiles, and music without turning the source mod into a required dependency. See [[Gang Wars, Boombox & Mob Atlas|Gang-Wars-Boombox-and-Mob-Atlas]] and the [[Standalone Compatibility Covenant|Standalone-Compatibility-Covenant]] for the complete architecture.

## Create 6.0.8

When Create is present, JetSetCraft uses Create's own `ITrackBlock` geometry and track graph/Bezier data. This supports real slopes, diagonals, junctions, and long curves instead of guessing from registry names. Create remains compile-optional/runtime-optional.

## TACZ and combat mods

TACZ detection uses the public `IGun` API. Dynamic camera effects are reduced while a weapon overlay is active. Ride animation is lower-body only, leaving arms, hands, head, and held-item bones free for TACZ, Epic Fight, Better Combat, vanilla items, bows, shields, spellbooks, and similar systems.

Full-body dance/ground-stunt animation runs on a separate layer and is suppressed when weapon activity is detected. Movement never uses “weapon equipped” as an implicit ride-state exit.

## The Aether

All entries are non-required datapack references:

- `aether:quicksoil`, `quicksoil_glass`, and `quicksoil_glass_pane` are boost/low-friction routes.
- `aether:blue_aercloud` participates in the bounce language while retaining its native launch behavior.
- `aether:aerogel` and `aether:holystone_bricks` can be wall-ride surfaces.
- Quicksoil glass panes can be discovered as grind geometry.

The Aether can be absent without missing-registry errors. Its mobs can also be represented in the Mod Mob Atlas and gangified through persistent JetSetCraft Street Gear state while remaining Aether-owned entities. Removing the gear returns control to normal Aether behavior; simply finishing an event does not.

## Twilight Forest

Non-required entries make Aurora Palace materials especially expressive:

- Aurora blocks, pillars, and slabs can become low-friction route pieces.
- Aurora pillars, slabs, and auroralized glass can be grind targets.
- Aurora blocks, pillars, and glass can be wall-ride surfaces.

All normal dimension travel, progression, mob combat, structure rules, and entity ownership remain owned by Twilight Forest. Curated gang adapters should layer on top rather than replace its creatures.

## Other rails, creatures, and dimensions

Vanilla/Forge rail subclasses work through `BaseRailBlock` behavior. Modded blocks can opt into surface/grind/wall tags through datapacks. Because the movement state lives on the player and does not assume the Overworld, Aether, Twilight Forest, Nether, End, and custom dimensions use the same solver.

For creatures, namespaced registry IDs and optional adapters are preferred over direct class assumptions. Missing optional namespaces must remain dormant data rather than startup failures. Species-aware Ground Contact / Ride Rigs are the common compatibility seam for animal/multi-leg movement without source-code takeover.

## Visual model mods

The lower-body animation contract improves composition with player-model and action systems, but extreme skeleton replacements may still need a dedicated adapter. JetSetCraft keeps gameplay state independent from PlayerAnimator, GeckoLib, Epic Fight, TACZ, or YSM internals so adapters can be added without replacing movement.
