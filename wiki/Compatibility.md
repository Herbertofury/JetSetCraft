# Compatibility

JetSetCraft is designed to enter large adventure/combat modpacks without making those mods dependencies.

<!-- JETSETCRAFT_WAVE2_EXACT_CURATED_2026-10-01 -->
## Wave 2 exact creature compatibility

The first premium installed-mod curation wave now has an exact data contract for three major Forge 1.20.1 creature ecosystems. **Curated does not mean copied or replaced**: each mob remains its provider's original registered entity and JetSetCraft only layers reversible Street Gear, social/challenge identity, reputation, stingers, and graffiti rewards.

| Provider | Pinned target | Exact safe curated mobs | Technical/helper records excluded from independent gangs |
|---|---:|---:|---:|
| **Alex's Mobs** (`alexsmobs`) | 1.22.9 | **90** | **7** |
| **Alex's Caves** (`alexscaves`) | 2.0.2 | **43** | **2** |
| **L_Ender's Cataclysm** (`cataclysm`) | 3.31 | **39** candidate records | **0 living multipart records** |
| **Total** | | **172** | **9** |

The full mob-by-mob ledger is in **[Wave 2 Exact Mod Compatibility](../docs/JETSETCRAFT_WAVE_2_EXACT_MOD_COMPATIBILITY.md)** and includes each namespaced EntityType, stable gang ID, crew family, ride/contact profile, challenge safety profile, and bespoke graffiti direction.

### What “supreme compatibility” means here

- **No optional-mod hard dependency:** provider classes are not required for JetSetCraft to boot. Missing providers leave dormant curated data.
- **No source AI takeover:** provider goals, navigation, combat, variants, tame/owner state, boss controllers, loot, structures, animations, and renderers stay provider-owned.
- **Anatomy-aware rigs:** quadrupeds, bipeds, aquatic creatures, flyers, multi-leg mobs, serpentine bodies, slimes/contact-plane creatures, tiny mobs, and massive mobs receive explicit safe contact/ride profiles rather than a two-feet assumption.
- **Multipart/helper safety:** implementation segments/helpers never gain independent gear, gang state, rewards, or challenge targeting.
- **Boss and pet safety:** active Cataclysm boss/arena logic always wins, and owned/tamed creatures retain owner UUID and provider commands across equip, challenge, unload/reload, restart, and unequip.
- **Forward-compatible by default:** unknown safe future `Mob` IDs receive generic Mob Atlas behavior immediately; known curated IDs keep premium identity. Roster drift warns once and fails open instead of crashing or disabling the provider.
- **No hot-path tax:** provider discovery/fingerprinting is bounded to startup/data reload and cached. JetSetCraft does not rescan the entity registry every tick.



<!-- JETSETCRAFT_WAVE2_RUNTIME_IMPLEMENTATION_2026-10-01 -->
### Wave 2 runtime implementation

The Wave 2A curation is now wired into the actual mod, not only documented:

- three bundled datapack manifests live under `data/jetsetcraft/jetsetcraft_gangs/wave2_*.json` and carry all **172** exact entity IDs plus the **9** hard-hidden helper/segment IDs;
- the gang reload listener loads provider bundles atomically at server data reload, maps only installed EntityTypes into active gang identities, and keeps absent-provider profiles dormant without optional class links;
- `MobCompatibilityRegistry` caches provider version/known-roster state at reload time, so entity interaction does not enumerate registries every tick;
- `MobRideRigResolver` consumes the curated anatomy profile before geometry heuristics, while explicit datapack tags remain the highest-priority override;
- hidden multipart/helper IDs are rejected by the normal Street Gear eligibility path instead of falling through to generic compatibility;
- boss-gated and owner-gated curated records are not Boombox-spawn eligible; provider-owned boss/pet behavior remains authoritative;
- CI runs `tools/validate_wave2_compat.py`, enforcing 90/43/39 safe records, 7/2/0 hidden records, stable unique gang/entity IDs, complete rig/challenge/graffiti metadata, and boss/owner gating;
- the existing real Forge Street Gear GameTest now asserts that Wave 2 profiles and hidden technical IDs are actually present after a real resource reload.

Provider-present baseline runtime verification is now implemented and passing for the pinned releases, both individually and with all three providers loaded together. The verification lane runs the real Forge 1.20.1 GameTest server and dedicated server with the third-party JARs present, requires exact provider fingerprints, exhaustively proves that every live provider `Mob` is either curated or explicitly hidden, and rejects uncatalogued live mobs.

### Version drift and verification

Alex's Mobs 1.22.9 and Alex's Caves 2.0.2 were reconciled against their 1.20.1 source registries, living-attribute registration, and spawn-egg surfaces. Cataclysm is pinned to 3.31, while the public 1.20.1 source lineage inspected for its exact entity roster is older; JetSetCraft therefore treats the 39 Cataclysm records as the curated candidate set and uses the **live Forge registry fingerprint** as runtime authority.

A fingerprint mismatch never rebinds by display name. Missing known IDs go dormant with history preserved; added safe mobs get generic compatibility until curated; absent mods simply produce an `ABSENT` adapter state.

Wave 2 curation and the provider-present baseline are verified. On the pinned releases, the live registry proof resolved **116 Alex's Mobs EntityTypes / 90 curated mobs**, **83 Alex's Caves EntityTypes / 43 curated mobs**, and **103 Cataclysm EntityTypes / 39 curated mobs**, with **zero uncatalogued live Mob IDs** in each individual lane and again with all three providers loaded together. The hard-hidden manifest remains **7 + 2 + 0 = 9 technical/helper IDs**; only **3 + 1 + 0** of those instantiate as `Mob` objects because the remainder are non-Mob multipart/helper entities. All nine JetSetCraft GameTests and dedicated-server readiness passed in every individual provider lane and in the combined lane. Deeper client-facing persistence/reconnect, multiplayer synchronization, and representative boss/pet/anatomy interaction checks remain separate gameplay-proof tasks rather than being implied by the registry/server baseline.


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

The lower-body animation contract improves composition with player-model and action systems, but extreme skeleton replacements may still need a dedicated adapter. JetSetCraft keeps gameplay state independent from PlayerAnimator, Epic Fight, TACZ, or YSM internals so adapters can be added without replacing movement.
