# JetSetCraft Hangouts, Soft Territory & Reputation

This document records the performance/compatibility architecture for persistent natural gang locations. The full gang master specification remains authoritative; the live wiki page is `Hangouts-Territory-and-Reputation`.

## Core split

- **Natural Hangout:** ultra-rare persistent `site_id`, small resident crew, additive micro-dressing, no chunk ticket.
- **Natural resident:** original vanilla/mod-owned mob with real JetSetCraft Street Gear; remains gangified while gear remains equipped and keeps a soft home around the site.
- **Boombox/event actor:** explicit ephemeral challenge cast; leaves/despawns after the event and does not create permanent world population.
- **Reputation:** canonical progression is per `gang_id`; a hangout may keep only a tiny bounded `site_id` chapter-memory bias/flags. Never track full reputation per disposable mob.

## Soft territory

A site record contains only a stable ID, parent gang, dimension/anchor/radius, resident references, discovery state, tiny chapter memory, dressing seed/manifest, bounded notable-history flags, and terrain validity state.

The site does not force-load chunks, reserve terrain, alter biome/structure generation, block player building, or stop other mods from using the area. When unloaded it has no live gang AI.

## Adaptive furniture

Hangouts lightly dress already-generated geometry after discovery. Use safe air/replaceable positions only; never replace solid source/player blocks, carve terrain, cut trees, reroute fluids, or modify source structure templates. Prefer static non-ticking props and degrade to fewer/no props when the location is constrained. Keep a placement manifest so retirement removes only JetSetCraft-owned dressing.

## Performance

Use a low-frequency shared Hangout Brain for loaded idle residents. Full strategic AI exists only during actual challenges. Enforce small resident caps, bounded active sites, no global per-tick scans, activation/deactivation hysteresis, cached local route/anchor analysis, zero strategic AI for unloaded sites, and no ticking decorative furniture.

## Reputation transactions

One event cast or one Natural Hangout chapter is one reputation transaction target. Five spawned actors must not multiply rewards/penalties five times. Events away from sites update `gang_id`; site-linked events update `gang_id` plus a small optional local chapter-memory change.

## Lifecycle

Candidate -> Validate existing terrain -> Create `site_id` -> Add safe micro-dressing -> Resident arrival -> Dormant/unloaded -> Active idle -> Challenge -> Residents return/event cast exits -> Revalidate changed terrain -> Migrate/shrink/retire without restoring source terrain.

## Release gates

- no generation replacement;
- no chunk tickets;
- natural residents survive event end/save-reload while geared;
- event actors cleanly exit/despawn;
- one balanced reputation transaction per encounter;
- dressing never overwrites source/player blocks;
- large numbers of unloaded hangout records have negligible live tick cost.
