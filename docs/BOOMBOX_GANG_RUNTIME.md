# JetSetCraft Boombox & Gang Runtime

Status: implemented runtime checkpoint for Forge 1.20.1 / Java 17 on the `automation/head-gang-compat-0.3` line.

## What is real now

JetSetCraft's Boombox is no longer only a design document. It is a craftable, placeable block with a physical one-item gang target slot and a server-authoritative gang-session lifecycle.

- Use a supported mob head on the Boombox to insert exactly one physical target item.
- The target resolves to the original registered source `EntityType` plus a stable JetSetCraft gang identity.
- Empty-hand use starts/stops a gang session. There is no artificial player cooldown; safety comes from one active session per Boombox, actor caps, loaded-terrain spawn checks, and hard actor expiry.
- Sneak + empty-hand removes the physical target item. Breaking the Boombox also returns the target.
- The inserted head is rendered on the Boombox so tuning is visible in-world.
- Comparator output is `0` idle/untuned, `7` tuned, `15` active.

## Source ownership is preserved

Gang actors are instantiated from the source mob's live registered `EntityType`; JetSetCraft does not replace vanilla or optional-mod entity registrations. After creation, the same source-owned entity receives physical JetSetCraft Street Gear and a JetSetCraft-owned gang attachment.

Ordinary mobs follow the same contract: equipping Street Gear makes them a persistent member of the stable gang mapped to their source entity, while removing Street Gear removes that gang attachment. This means gangification is additive and reversible.

## Event actors and anti-farm safety

Deliberately summoned Boombox actors are marked as ephemeral challenge cast members. They:

- carry a unique challenge UUID and role;
- expire at the Boombox session deadline;
- are tracked by UUID instead of requiring a continuous global entity scan;
- do not drop normal loot or experience when killed during the event;
- are discarded when the session is cancelled, expires, or their event lifetime ends.

Natural/source mobs are not converted into ephemeral event actors and are not globally modified.

## Species-aware Street Gear

`GangGearSelector` chooses JetSetCraft gear through the existing `MobRideRig` anatomy resolver. Biped, quadruped, multi-leg, aquatic, winged, floating, and body-contact shapes remain source-owned mobs while receiving a compatible JetSetCraft presentation.

## Gang identity

`GangRegistry` is now the central stable identity catalog. The approved vanilla atlas from the canonical Drive master specification is represented by stable IDs and canonical default names, including Dead Beat, Bone Drones, Creepaku Gouji, Hex Appeal, Burnout Brigade, Dead Water, Goo Groove, Gold Rush, Blackout Bones, Arachnaphobia, Underweb, and the broader passive/neutral/hostile roster.

Unknown modded entities use deterministic `jetsetcraft:mob/<namespace>/<entity>` IDs and safe generated crew names until a curated datapack/adapter overrides them; JetSetCraft does not invent a destructive entity replacement.

### Datapack/server gang overrides

Gang identity is now reloadable server data rather than a hardcoded-only catalog. Files under `data/<namespace>/jetsetcraft_gangs/*.json` overlay the stable built-in atlas atomically. A definition may override `display_name`, `disposition`, `music`, `primary_color`, `secondary_color`, actor bounds, Boombox eligibility, legendary status, and one or more source `entity`/`entities` mappings. Missing fields inherit the built-in definition when the `gang_id` matches a stock gang, so a server can rename or recolor Dead Beat without copying every field. Optional-mod entity mappings are ignored cleanly when their entity is not installed.

The bundled `dead_beat.json` is an acceptance fixture as well as an example. The Forge GameTest requires at least one gang override to have been loaded, so a broken reload-listener registration fails CI instead of silently falling back forever.

## Mob-head compatibility

The Boombox consumes `HeadGangTargetResolver`, so the same provider-agnostic compatibility rules apply to vanilla and optional head ecosystems. Exact item mappings can now be supplied by `data/<namespace>/jetsetcraft_head_targets/*.json` with `item`, `entity`, and optional `gang` fields. The resolver priority is explicit per-stack JetSetCraft metadata → exact server/datapack mapping → vanilla identity → conservative registry-name convention. That lets a pack support unusual player-head/emblem items without texture guessing or source-mod Java linkage.

Exact known runtime targets and the All The Heads future-version identity contract are documented in `MOB_HEAD_GANG_COMPATIBILITY.md`.

## Entrance stingers

Every curated gang has a dedicated stable sound path registered through `ModSounds`. The repository ships eighty distinct original 3.2-second procedural entrance stingers with exact hashes, loudness floors, and duration recorded in `tools/audio_manifest.json`. Servers or resource packs can replace a stinger without changing gang IDs or runtime code.

## Vanilla recipe

The standalone Boombox recipe deliberately uses vanilla ingredients only: iron, a note block, redstone, and copper. Optional compatibility never becomes a hard dependency.

## Verification contract

The existing six-test Forge GameTest suite remains six tests so CI's runtime acceptance count stays stable. The `street_gear` test now also proves that:

- Street Gear creates and removal clears persistent gang identity;
- a placed physical Boombox accepts and retains a Zombie Head;
- the head tunes to `minecraft:zombie` / `jetsetcraft:dead_beat`;
- a real server FakePlayer can start and cancel the session;
- a second session can start immediately after cancellation;
- the target slot returns its physical head intact.

GitHub Actions remains the authoritative Forge compile, real GameTest, and dedicated-server smoke environment.
