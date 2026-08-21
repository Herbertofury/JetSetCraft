# Gang Hangouts, Soft Territory & Reputation

JetSetCraft should make rare gangs feel like they **live in the world** without turning the save into a permanent NPC simulator or changing vanilla/modded world generation.

The core model is:

> **Natural Hangout = tiny persistent site record + small resident crew + additive micro-dressing.**
>
> **Boombox/Event Crew = temporary competition cast that leaves after the event.**

This distinction is a performance and compatibility rule.

## Soft territory, not generated bases

A Natural Hangout is a gang adopting a place Minecraft already generated. JetSetCraft does not carve a base, reserve terrain, replace structures, or modify biome generation.

Each discovered hangout stores only a compact record:

- stable `site_id` / local chapter ID
- parent `gang_id`
- dimension and anchor position
- soft home/activity radius
- discovery state
- resident references
- tiny chapter mood/affinity state
- furniture layout seed/manifest
- a few bounded notable-history flags
- last meaningful interaction/visit
- validity state if the terrain later changes

The site record owns **no chunk ticket**. When its chunks are unloaded, it has no live gang AI and should cost essentially only saved-data/disk space.

A territory radius is an influence field, not land ownership. It must never prevent normal spawning, structures, player building, another mod's systems, or chunk unloading.

If a player or another mod changes the area substantially, JetSetCraft re-scores the site and gracefully shrinks, redresses, migrates, or retires it. It never restores old terrain over player/mod changes.

## Natural residents versus event-only actors

### Natural Hangout residents

Ultra-rare Natural Hangouts have a **small resident crew**, not a crowd. These residents are real persistent world actors:

- original vanilla/mod-owned entity type
- real JetSetCraft Street Gear
- persistent gangification while that gear remains equipped
- associated with one `site_id`
- soft-home behavior around the site
- cheap idle actions such as short skate loops, emotes, practice tricks, greeting/taunting players, or using hangout props
- no full strategic competition AI unless a challenge is active
- no force-loaded chunks

When their chunks unload, normal Minecraft entity/chunk serialization handles them. JetSetCraft must not keep the area loaded merely because a gang lives there.

The home rule is intentionally soft. A resident may path back when practical, but must never rubber-band teleport merely to preserve the hangout. If ordinary gameplay or another mod legitimately moves a resident far away, it can become a roaming member or detach from that local chapter rather than fighting the rest of the modpack.

### Boombox and scripted event actors

Boombox challenges and scripted street events may create an explicit ephemeral `event_actor` cast. They:

- exist for the challenge rather than as permanent residents
- inherit the target `gang_id`
- may optionally reference a relevant `site_id` for scoring/local memory
- never create a permanent hangout just because they spawned
- perform a stylish exit after results when possible: grind away, skate down a street, jump a ledge, enter a portal, etc.
- despawn only after leaving meaningful player view/interaction range, with a safe bounded cleanup timeout if a cinematic exit cannot finish
- do not leave ordinary mob loot or Street Gear merely because the event system removed its temporary cast unless that event explicitly awards it

This is **actor cleanup**, not a fake de-gangification transition. Existing normal mobs are never silently converted by an event.

## Adaptive micro-furniture

Hangout furniture is post-generation dressing applied to empty/safe space after JetSetCraft discovers the site. It should make the location recognizable without behaving like a structure generator.

Geometry-aware layout archetypes can include:

- rail-side hang
- alley/wall hang
- rooftop hang
- cave-mouth hang
- village-edge hang
- bridge/underpass hang
- forest clearing hang
- waterfront hang
- Nether ledge hang
- optional dimension/mod-specific safe archetypes

Possible dressing includes a Boombox, crates, folding seats, cones, boards, graffiti practice panels, small lights, banners, tool piles, or trick markers.

Placement rules are strict:

1. Never replace a solid vanilla/mod/player block merely to make the hangout fit.
2. Use only safe air/replaceable positions against detected floors/walls.
3. Never flatten terrain, carve blocks, cut trees, reroute fluids, or alter a source structure template.
4. If the full layout does not fit, use fewer props. If nothing fits, the crew can hang there with no furniture.
5. Prefer static non-ticking JetSetCraft props. Do not create one ticking block entity/entity per decorative object.
6. Interactive props are rare and bounded.
7. Keep a placement manifest so retirement removes only JetSetCraft-owned dressing.

World generation therefore remains source-owned: **Minecraft/the installed mod generates the place; JetSetCraft notices it and lightly dresses available empty space afterward.**

## Reputation: gang first, local chapter second

Do not track reputation independently for every spawned gang member.

### Canonical gang reputation

The primary durable progression remains:

`player UUID + gang_id -> GangReputation`

This controls broad hostility/friendliness, membership, unlocks, betrayal/reconciliation, rename rights, rewards, and how that gang treats the player across the world.

### Tiny local chapter memory

A Natural Hangout may additionally store a small `site_id` memory so a particular crew/place can remember important local history without becoming a second giant RPG reputation system.

Examples:

- discovered
- first challenge won/lost
- defended from a rival
- repeatedly helped
- vandalized/betrayed
- local champion defeated
- currently welcoming/annoyed/hostile

Keep this bounded: one small affinity bias plus a handful of notable flags. Never store an unbounded interaction transcript.

### Shared reputation transactions

One Natural Hangout or one event cast is one reputation target. Five members do **not** award reputation five times.

- event away from a hangout -> update `gang_id`
- event tied to a hangout -> update normal `gang_id` reputation plus a small optional `site_id` memory
- helping/attacking several crew members in one encounter -> resolve one balanced interaction/challenge transaction

This makes it feel like **the crew remembers you** while keeping saves and balance sane.

## Shared Hangout Brain

Idle residents should not each run expensive gang strategy. A loaded hangout uses one low-frequency site-level coordinator that assigns cheap intents such as:

- stand/sit at a prop anchor
- skate a short cached loop
- practice one trick
- watch another member
- dance/emote
- use a graffiti practice surface
- greet/taunt a nearby player based on reputation
- return toward the soft home radius

Individual mobs keep ordinary local steering/navigation. Full route planning, team roles, paint strategy, intercept logic, and other competitive intelligence activate only when a real challenge begins.

## Performance invariants

- **No force-loaded gang chunks.**
- No global per-tick scan for hangouts.
- Activate sites only near players, with a farther deactivation radius to prevent boundary thrashing.
- Small resident cap per Natural Hangout.
- Bounded simultaneously active hangouts per dimension/server.
- Zero strategic gang AI for unloaded sites.
- No ticking decorative furniture.
- Cache route/anchor analysis rather than rescanning terrain every tick.
- If budget is exceeded, reduce ambient animation/decision frequency before reducing gameplay correctness.

The goal is that hundreds or thousands of discovered hangout records in distant unloaded areas are cheap because they are **records, not active simulations**.

## Lifecycle

1. **Candidate** — ultra-rare cheap rarity gate succeeds.
2. **Validate** — score existing terrain/space without modifying it.
3. **Create** — assign `site_id`, gang, anchor, radius, roster budget, and dressing seed.
4. **Dress** — add only safe JetSetCraft-owned props in empty/replaceable space.
5. **Residents arrive** — small original-type crew gets real Street Gear.
6. **Dormant** — chunks unload; no JetSetCraft live simulation.
7. **Active idle** — player nearby; low-frequency Hangout Brain.
8. **Challenge** — temporary high-level controller drives participating residents/event actors.
9. **Post-event** — residents return to cheap hangout life; event actors skate away/despawn.
10. **Revalidate** — terrain changes can shrink, migrate, or retire the site.
11. **Retire** — remove only JetSetCraft dressing and leave source/player terrain untouched.

## Acceptance tests

1. Natural Hangout discovery does not change biome/structure generation or replace source blocks.
2. A hangout in an unloaded chunk never keeps that chunk loaded.
3. Natural residents remain associated with `site_id` after event end and save/reload while their gear remains equipped.
4. Idle loaded hangouts stay inside the configured resident/AI budget.
5. A Boombox event away from a hangout creates an ephemeral cast that exits/despawns and does not create a permanent site.
6. One event resolves one balanced gang/chapter reputation transaction, not one per spawned actor.
7. A specific hangout can remember bounded local history while `gang_id` reputation remains canonical.
8. Dressing never replaces solid source/player blocks and safely reduces to fewer/no props when space is constrained.
9. Building through a site triggers revalidation/migration/retirement rather than terrain restoration.
10. Many discovered but unloaded hangouts have negligible live tick cost.

See also [[Gang Wars, Boombox & Mob Atlas|Gang-Wars-Boombox-and-Mob-Atlas]], [[Standalone Compatibility Covenant|Standalone-Compatibility-Covenant]], and [[Compatibility]].
