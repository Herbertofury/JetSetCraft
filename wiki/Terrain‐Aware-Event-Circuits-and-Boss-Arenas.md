# Terrain-Aware Event Circuits and Boss Arenas

**Research/design checkpoint — 12 October 2026. Minecraft Java 1.20.1 / Forge. Not an implemented-feature or performance claim.** Exact Forge dependency, movement source and installed protection integrations must be pinned before implementation.

## Owner-approved experience

Temporary, biome- and terrain-aware courses should make the actual world an excellent competitive playground: gang races, skating/platform/trick chains, obstacle dodging, branching Metroidvania-style traversal and racing to tag more valid areas. Jet Set Radio and Bomb Rush Cyberfunk are inspiration, with a higher quality ambition. Keep builds and terrain intact; remove event obstacles afterward so normal Minecraft continues. Provide teleported boss arenas where more room or isolation is necessary, and genuine underwater skating/tricks against water gangs.

The owner explicitly approved a **layered event course** rather than replacing terrain. Night events are a flagship quality target: **as exciting as the best rave on Earth**, with goosebump-inducing music, synchronized lights/lasers, choreography and entertaining reactions to player and rival trick chains. This is a playtest target, not a claim that it can be guaranteed by an algorithm.

## Recommended architecture

Use two complementary systems:

1. **Live-world circuits:** server-owned, temporary course geometry, tag surfaces and hazards layered over real terrain. The default implementation must never replace source-world blocks, block entities, inventories, fluids or biomes.
2. **Boss arenas:** one pre-registered arena dimension with reusable session-owned rooms and a tested return protocol. Use it for encounters needing extensive space, destructive-looking changes or strong isolation. It is an optional separate place, not a disguised claim that a copied arena is the live Overworld.

A course is an accepted **manifest**: session UUID, revision, terrain/protection snapshot, directed route graph, collision primitives, objectives, hazard timing, visual palette, participant rules and cleanup ownership. Generate and validate that plan before activation.

Preserve existing safeguards documented in the [Gang Wars master](Gang-Wars-Boombox-and-Mob-Atlas): loaded-terrain spawn safety, no forced chunks, actor limits, expiry, UUID tracking, no ordinary loot/XP from ephemeral actors, no arbitrary real-world cooldown, and original mob/provider identity. Resource budgets protect server health; do not misrepresent deleting gameplay capabilities or shrinking the content catalogue as optimization.

## Why this is better than block restoration

| Approach | Recommendation |
|---|---|
| Natural terrain plus visual gates/targets | First playable milestone; simplest no-terrain-write route. |
| Virtual geometry integrated with skate collision | Preferred complete system for temporary ramps, rails, platforms and hazards. Bespoke physics integration must be proven. |
| Fixed isolated arena | Preferred boss alternative where safe live-world space is unavailable. Requires loading/return/recovery engineering. |
| Air-only temporary blocks | Not the default: empty space may be a doorway, fluid channel or machine path, and placement affects neighbors. |
| Replace region, restore snapshots | Reject as normal survival-world strategy: can erase later work, restore old inventories and miss fluid/tick effects. |

Forge's [BlockSnapshot implementation](https://raw.githubusercontent.com/MinecraftForge/MinecraftForge/1.20.1/src/main/java/net/minecraftforge/common/util/BlockSnapshot.java) captures block state and block-entity metadata, not a transactional snapshot of all world simulation. Neither forced restoration nor a same-state comparison establishes event ownership or preserves concurrent edits. The comparison is not a universal compare-and-restore facility.

**Important limit:** no course terrain writes does not freeze the world. Players can still step on pressure plates, mobs can act, and farms can tick. Exclude fragile mechanisms and protected builds, guard supported event interactions, and use isolated arenas when strict separation is required. Promise tested non-destructive behavior, not byte-identical world files after ordinary survival time passes.

## Generation: topology first, obstacles second

### 1. Capture a bounded, immutable terrain snapshot

On the logical server, sample only available chunks. Start coarse, refine candidate corridors. Record actual collision shapes, headroom, support, slopes, materials, fluids, useful rail/wall affordances, biome tags and protection exclusions.

Heightmaps alone are inadequate for trees, bridges, cave floors or Nether roofs. Store multiple support/free-space spans per column; explore local accessible volume around the start elevation. Underwater routes need a 3D free-space graph. Never treat an unavailable chunk as air or load distant terrain to satisfy untrusted client coordinates.

Copy bounded value data for background planning; worker threads must not retain live Level/chunk/entity references. Revalidate occupancy, permissions and configuration on the server before commit. Forge separates the [logical server](https://docs.minecraftforge.net/en/1.20.1/concepts/sides/) from client rendering; its [networking guide](https://docs.minecraftforge.net/en/1.20.1/networking/simpleimpl/) specifically warns about client positions causing unwanted chunk generation.

### 2. Build a directed movement graph

Nodes are useful landing, passing, turning, tagging and departure regions. Edges describe real actions: roll, turn, step, jump, grind, transfer, supported wall action or aquatic motion. Each edge declares entry speed/heading, required abilities, swept clearance, landing/recovery space and exit-state range.

Calibrate these envelopes against the actual JetSetCraft controller. Two independently possible jumps can form an impossible chain if the first landing leaves the wrong speed or direction. Validate sequences, not only pieces. Maintain real-world collision and external impulses.

### 3. Choose a competitive topology

For a race, construct an ordered checkpoint loop with a valid last-to-first connection. Add visibly distinct optional technical lines and readable, wide merges. For tag competition, use interconnected loops and target clusters with several approaches, rather than a forced linear race path.

A graph-first bounded beam search with local backtracking is a strong first choice. A* can connect anchors, but independently shortest segments do not necessarily create a fun course. Evaluate route overlap, curvature, pacing, verticality, passing room and meaningful choices. Safety, reachability and permissions are hard constraints, never negotiable terms in a weighted “fun” score.

### 4. Fit authored, parameterized challenge modules

Examples: acceleration→turn→recovery; gap→wide landing→optional trick; grind→transfer→safe dismount; forgiving route versus technical shortcut; telegraphed hazard→dodge window→recovery; low-risk tag patch versus exposed bonus detour.

Each module needs validated connectors, legal rotations, collider/support geometry, ability and speed bounds, biome compatibility, timing, fallback and cost. Do not simply scatter random ramps or reskin a dry-land jump as ice. Required routes must fit the advertised participant abilities; Metroidvania-like shortcuts can expose aspirational abilities without trapping an unqualified player behind the only exit.

### 5. Validate and optimize

Run protection/availability checks, collision/clearance tests, graph reachability, movement-state propagation, difficult-chain simulation, multi-racer congestion and objective fairness. Fail with specific reasons. Search can improve a valid candidate; it cannot trade away safety. If a site is unsuitable, relocate within available permitted terrain, offer a simpler disclosed course, or decline safely.

Keep accepted competitive geometry stable. If another player builds into it, their world edit wins: invalidate the segment and safely pause, reroute under declared rules, or cancel. Never delete their new block or secretly move obstacles to disadvantage the leader.

### Research basis

- [Dormans: Adventures in Level Design](https://www.pcgworkshop.com/archive/dormans2010adventures.pdf) separates challenge logic from spatial layout. Adapt this to embedding a course in existing terrain rather than carving terrain.
- [Tanagra](https://ojs.aaai.org/index.php/AIIDE/article/view/12379) combines movement models, patterns and constraints. Its 2D platformer results motivate, but do not validate, a Minecraft skating implementation.
- [GDMC](https://arxiv.org/abs/1803.09853) studies adaptation to existing Minecraft maps. Settlement generation is not a ready-made real-time race system.
- [Search-based PCG](https://www.um.edu.mt/library/oar/handle/123456789/29609) supports explicit candidate representations and evaluation; use hard safety gates before quality scoring.
- [WFC reference implementation](https://github.com/mxgmn/WaveFunctionCollapse) is useful for local variety. Local adjacency alone does not prove a complete playable circuit. [DeBroglie](https://boristhebrave.github.io/DeBroglie/articles/constraints.html) supports global/path constraints, but still needs controller-level validation.
- [Expressive-range analysis](https://www.pcgworkshop.com/archive/smith2010analyzing.pdf) motivates measuring actual variation. Many seeds producing nearly identical ovals do not establish replayability.

## Real collision, not decorative platforms

Mojang's [display-entity release notes](https://www.minecraft.net/en-us/article/minecraft-java-edition-1-19-4) establish that displays have no collision/physics. A visible block display is not a skateable ramp; interaction entities do not solve that.

Prototype spatially indexed event primitives integrated into both server-authoritative movement and client prediction. Start with static platforms and explicit rail paths, then ramps and moving hazards. Check swept collision at high speed, seams, support, normals, jumping, falling, dismount, real block collision and server movement validation. Never globally disable flight/anticheat checks to hide desynchronization.

Decorations should be batched rather than one ticking entity per rendered block. [Forge entity synchronization](https://docs.minecraftforge.net/en/1.20.1/networking/entities/) supplies transport tools, not a complete collision or recovery system. Missing/stale geometry revisions must prevent competitive admission.

## Biome-specific gameplay

| Environment | Course identity | World-preserving implementation |
|---|---|---|
| Forest/plains/village | Contour-following rails, natural gaps, alternate lines | Bend around builds and trees; respect claims and keep ordinary access open. |
| Ice/snow | Fast coasting, drifting, long slides, technical braking | Measure actual grip/stopping distance; widen landings and provide readable recovery. Do not change global block friction. |
| Nether | Heat lanes, timed fire jets, lava-side leaps | Event-owned hazard volumes/visual fire; never place spreading fire or lava in the source world. Lava-crossing support needs proven collision and recovery. |
| Caves | Layered ledges, low-clearance dodge choices, falling obstacles | Telegraph debris; use owned actors that never remove ceilings, settle as blocks or drop resources. |
| Underwater | Seafloor lines, 3D hoops, current-assisted tricks and tags | New opted-in aquatic controller; preserve real water and provide tested oxygen/escape rules. |
| Modded biomes | Provider-aware palettes and affordances | Data-driven tags plus actual collision/behavior validation; unsupported semantics fail safely. |

Real existing lava, fire and other native hazards remain real unless explicitly stated event rules provide participant-only recovery. No hidden global immunity, weather changes or biome rewrites. Claim adapters need allowed/denied/unknown results; unknown protection cannot be guessed away from block palettes.

## Biome-native materials and visual palettes

**Explicit owner requirement:** generated rails, fences, platforms, ramps, supports, trim and arena scenery must use textures and material families that belong to the surrounding biome and local terrain. The result should look natural, varied and exciting rather than like the same generic course pasted into every biome. This applies to vanilla and modded biomes and to boss-arena presentation derived from the source area.

- **Warped Nether areas:** use warped-wood/fence-style rail supports and matching local Nether stone accents. Crimson areas should receive their own crimson palette rather than the warped palette by default.
- **Ocean areas:** use ocean-appropriate local stone, sand, gravel and suitable reef-themed visual accents according to the actual site; do not assume every ocean has coral or prismarine. Keep underwater routes and grind surfaces readable.
- **Other vanilla and modded areas:** prefer the local wood, stone and related fence/slab/wall material families actually associated with that biome. Support provider-specific block textures and models where compatible. Do not substitute generic oak merely because a biome comes from another mod.

### Proposed palette resolver and safeguards

Use data-driven biome/tag-to-material-family mappings plus bounded sampling of natural nearby terrain. Allow modpack/provider overrides and validate registry IDs and available variants. Sampling must not treat a player build, machine, container, rare ore or unrelated structure as permission to copy its palette or behavior. A nearby block alone does not establish biome provenance.

Select coherent roles: structural wood/stone, grindable rail surface, supports, platform faces, trim and readable course accents. Match textures through the active resource pack where supported; validate unusual connected textures, custom models and block-entity renderers rather than assuming universal compatibility. Blend deliberately across biome boundaries while maintaining a readable course identity.

These are **event-owned visual material representations**, not harvesting or placing those source blocks. Keep collision/grind geometry explicit and movement-tested independently of the decorative fence/block model; cosmetic selection must not silently change friction, damage, fluids, loot, ticking or provider behavior. Material-dependent movement must be an explicit advertised and tested course rule. Never instantiate a functional machine/block entity just to borrow its appearance.

For missing providers, unsupported models or absent fence/slab variants, fall back through a validated same-family or biome-compatible palette. Record the fallback for debugging and preserve fair geometry. Revalidate resources and the accepted manifest before activation; avoid disruptive mid-event palette/geometry changes. Cache resolved palettes with resource/configuration invalidation rather than scanning registries or terrain every frame.

### Acceptance requirements

- [ ] Verify visually distinct, locally coherent warped/crimson, forest, ice, cave, ocean and representative modded-biome courses and source-themed boss arenas.
- [ ] Verify rails, fences, supports and platforms use compatible local material families, including biome-boundary transitions and missing-provider fallbacks.
- [ ] Test resource-pack reloads, unusual/custom block models, multiplayer consistency, underwater visibility and nighttime light effects.
- [ ] Prove material styling causes no source-world edits, block harvesting, functional block side effects, resource drops or cleanup residue.
- [ ] Compare generation/startup/rendering costs and preserve collision, grindability, readable hazards and the full feature set.

## Fair gang racing and tag battles

AI uses legal movement modes and executable graph edges, with skill expressed through route choice, consistency, reaction, teamwork and risk. Preserve the existing prohibition on impossible acceleration, omniscient input reading and teleport catch-up.

Use ordered directional checkpoint gates and swept crossing tests. Tag areas have stable target IDs, legal backing surfaces, range/line-of-sight validation and clear capture rules. Count meaningful areas rather than rewarding repeated clicks or every tiny subdivision of one wall. Roles can include racer, painter, defender and interceptor. Compare time-adjusted target access and opening routes; equal tag counts do not prove fair starting positions.

Keep the basic goal understandable: reach the finish or tag more areas. Optional style/technical bonuses must be visible rather than silently replacing that goal. Validate several actors together; a solo bot reaching the finish does not prove a race is playable or fun.

## Boss arenas and safe teleporting

Use a fixed registered arena dimension with separated reusable slots. Runtime dimension creation/deletion per fight is not a turnkey Forge 1.20.1 service. [Registry documentation](https://docs.minecraftforge.net/en/1.20.1/concepts/registries/) and [datapack generation](https://docs.minecraftforge.net/en/1.20.1/datagen/server/datapackregistries/) support the data-defined approach; room allocation remains custom engineering.

Reserve a slot with session UUID and generation. Prevalidate a real floor, clearance, breathable staging area and exit; record recoverable return intent before transferring. [ITeleporter](https://raw.githubusercontent.com/MinecraftForge/MinecraftForge/1.20.1/src/main/java/net/minecraftforge/common/util/ITeleporter.java) supplies placement hooks, not a complete return transaction. Respect cancellation. Start attacks/scoring only after confirmed arrival.

Return to the original location only after revalidation, then try a safe nearby source location, a valid configured fallback and a safe Overworld refuge. Preserve pending recovery if none is available. Never reset beds or restore whole old inventories/health/effect snapshots as a return strategy. Remove only event-owned modifiers. Offline returns must survive room reuse; never delete a dimension folder as normal cleanup.

Keep original gang mobs in their world. Ordinary non-player dimension transfer can recreate entities, conflicting with instance/provider preservation; use session-owned arena opponents until identity-preserving transport is explicitly proven. See the [Forge entity patch](https://raw.githubusercontent.com/MinecraftForge/MinecraftForge/1.20.1/patches/minecraft/net/minecraft/world/entity/Entity.java.patch). Initially require safe dismount from unrelated mounts rather than silently cloning or deleting them.

Preserve no mod-forced tickets. Admission to a separate dimension still needs ordinary destination loading and a bounded readiness policy; do not claim zero load. Do not acquire persistent tickets merely to make cleanup easier. A copied arena, if later offered, must omit inventories, live machines, loot and source entities; rewards must not duplicate copied valuables.

## Underwater skating is a new feature

[Current World Physics](World-Physics) deliberately yields water movement/pose to vanilla, including currents, bubble columns, boots and effects. Preserve that default outside explicit aquatic activation.

Proposed states: supported seafloor skating; free-water glide with ascend/descend; validated aquatic rail/trick; stable surface transition; scoring-disabled recovery. Ground support cannot be faked everywhere, and infinite free-volume spins must not farm tricks. Preserve external impulses rather than overwriting currents/knockback every tick. Water mode must not imply lava skating or immunity.

The [1.20.1 LivingEntity patch](https://raw.githubusercontent.com/MinecraftForge/MinecraftForge/1.20.1/patches/minecraft/net/minecraft/world/entity/LivingEntity.java.patch) short-circuits vanilla water before the custom-fluid movement hook: overriding moveInFluid alone is not a general water-controller replacement. Prefer a carefully integrated movement state; a custom vehicle adds mount, camera, oxygen and transfer complexity.

Either design breathable checkpoints around vanilla oxygen or explicitly scope a temporary event breathing feature. [LivingBreatheEvent](https://raw.githubusercontent.com/MinecraftForge/MinecraftForge/1.20.1/src/main/java/net/minecraftforge/event/entity/living/LivingBreatheEvent.java) is a possible hook, not automatic compatibility. Move participants to breathable safety before withdrawing it. Preserve their own potion/enchantment effects.

Water gangs can express distinct challenges: Dead Water seafloor pursuit, Current Affairs beam-dodge lanes, Ancient Current boss phases, Neon Ink luminous tag trails and Reef Riders formation slalom. These are proposed uses of existing gang names. Vanilla mobs' effects do not become participant-only merely because they have a session tag; isolate or specifically control event-owned attacks, including bystander effects.

## Night rave and combo spectacle

A generated night course should feel like a deliberately staged festival, not random flashing decoration. Generate lighting positions along the same visibility, clearance and terrain plan: start reveal, route accents, dramatic overlooks, synchronized crew zones and a finish climax.

Use temporary client-rendered beams, emissive stage elements and appropriately budgeted effects. Dynamic illumination of real surfaces is a separate renderer/compatibility task; an emissive mesh is not proof it lights terrain. Do not place/remove light blocks, alter world time or weather, create damaging lasers, or require shader packs for basic readability.

Share the musical timeline with [reactive music and choreography](Music‐Driven-Dances-and-Mob‐Themed-Bosses): buildup, beat/phrase accents, combo escalation, landing/hit responses, rival call-and-response, original announcer payoffs and crowd reactions. Immediate gameplay feedback stays immediate; no delayed controls to fit a beat. A giant combo can earn a dramatic coordinated climax, while smaller actions stay legible and avoid constant overload.

Provide low-flash/static-light, reduced motion, brightness/effect density and audio controls, plus visible hazard cues when muted. Avoid rapid high-contrast full-screen flashes and preserve opponent/obstacle visibility. Effects quality can scale without changing gameplay, scoring or the catalogue of available routines. Synchronize meaningful cues and deterministic seeds rather than sending every particle.


### Nether and End: rave staging at any time

**Explicit owner requirement:** Nether and End courses and arena events always qualify for the full music-synchronized rave presentation, independent of the Overworld day/night cycle. Do not gate their lights, lasers, musical reveals or combo-reactive crew spectacle on a night-time check. This means throughout an active event, not permanent effects after it finishes.

Use dimension identity/event context for eligibility rather than assuming a low local block-light value: lava, luminous blocks and player lighting must not accidentally disable the show. Preserve Nether/End-specific material palettes, lighting contrast and readable routes/hazards. Keep the same reduced-flash/no-strobe, brightness and effect controls, no real light-block/time/weather changes, event-owned cleanup and performance requirements. Source-themed isolated boss rooms inherit this presentation policy through the event manifest.

- [ ] Verify Nether and End event rave staging remains eligible at every Overworld time, in bright and dark local areas, and after teleport/reconnect.
- [ ] Verify finish/cancel/restart removes only event-owned effects and preserves vanilla surroundings; accessibility controls remain effective.

## Lifecycle and recovery: despawn safely, not merely quickly

Session states: PREPARING → READY → ACTIVE → ENDING → CLOSED; cancellation uses the same idempotent cleanup path. Only ACTIVE scores. Every collider, marker, decal, actor and modifier has one owner UUID/revision.

Close scoring first, stop hazards, put riders on safe real support or return them, then remove colliders/visuals and release reservations. Expiry cannot depend on an unloaded entity ticking. Stale actors must validate their session when naturally loaded; no global entity scan or forced world loading to find them.

Use cross-dimensional [SavedData](https://docs.minecraftforge.net/en/1.20.1/datastorage/saveddata/) for versioned session/recovery records, but setDirty is not an immediate crash-atomic transaction with player/chunk saves. Fault-test durable entry intent, returns and reward reconciliation. Default stale sessions to abort-and-recover on restart until resume semantics are separately qualified.

Forge [chunk](https://raw.githubusercontent.com/MinecraftForge/MinecraftForge/1.20.1/src/main/java/net/minecraftforge/event/level/ChunkEvent.java) and [entity-load](https://raw.githubusercontent.com/MinecraftForge/MinecraftForge/1.20.1/src/main/java/net/minecraftforge/event/entity/EntityJoinLevelEvent.java) callbacks may occur before full chunk readiness. Queue world-sensitive validation safely. Player death/clone/login/dimension transitions need explicit handling; [capability persistence/synchronization](https://docs.minecraftforge.net/en/1.20.1/datastorage/capabilities/) is not automatic.

## Implementation backlog and release gates

- [ ] Pin actual Forge 1.20.1 dependency and inspect existing controller/session/claims APIs; do not copy generic 1.20.x code that targets newer Minecraft.
- [ ] Build bounded read-only terrain/protection snapshots and debug route previews.
- [ ] Build graph/module schema, ability-state propagation and deterministic validation/failure reasons.
- [ ] Demonstrate one natural-terrain circuit with real player and gang racer, optional line and fair checkpoints.
- [ ] Prototype true virtual platforms/rails with prediction, real terrain collision and safe support removal.
- [ ] Add tag-area objectives, strategic gang roles, anti-farming and multi-actor fairness tests.
- [ ] Prove cancellation, expiry, deferred cleanup, reconnect and process-kill recovery before expanding content.
- [ ] Build one registered boss room and durable safe entry/return before a room pool.
- [ ] Implement opt-in seafloor skating, then free-water transitions; retain vanilla off-mode parity.
- [ ] Add ice, cave and Nether modules with tested hazard/recovery semantics.
- [ ] Integrate musical course staging, night rave and player/rival combo spectacle with accessibility.
- [ ] Profile snapshot/commit tick tails, generation latency, heap, packets, rendering and cleanup under simultaneous events; use cancellation/backpressure rather than unsafe shortcuts.
- [ ] Conduct human playtests for flow, meaningful shortcuts, readability, competition, variety and the flagship rave experience.

Use [Forge GameTests](https://docs.minecraftforge.net/en/1.20.1/misc/gametest/) for deterministic fixtures, plus dedicated-server/multi-client and process-kill tests. Required matrix: claims/unknown permissions; farms/containers/redstone/fluids; concurrent construction; maximum-speed seams/landings; chunk unload; cancel during every phase; death/mount/disconnect/return; stale/wrong-dimension packets; overlapping sessions; loot/reward duplication; existing underwater equipment/effects; bystander safety; repeated startup/shutdown and extended sessions.

For live worlds, compare event-caused changes against a control simulation, not raw region-file equality while normal time advances. Keep native test evidence, failure cases and performance uncertainty. No production code, Minecraft runtime tests or release certification resulted from this research pass.


## Major requirement: replayable skating and gunplay modes

**Owner direction:** give JetSetCraft enough meaningful activities and replayability to avoid the owner’s concern about running out of things to do in skating games. Capture the flag, king of the hill, familiar Halo-style objective/battle modes and escalating enemy-wave survival are explicit requests. Integrate intended TaCZ gunplay with skating and tricks. This records an experience target, not a claim about another game’s commercial performance or a claim that TaCZ integration is already implemented.

### Mode catalogue

| Mode | Skating-first objective and proposed variations |
|---|---|
| Capture the flag | Steal and return flags through branching grind, aerial and ground routes; teammate escorts and interceptions. Specify carry/drop/return, scoring, overtime and anti-stall rules. Prototype two-base and neutral-flag variants. |
| King of the hill | Contest readable control zones with several viable entry/escape lines. Proposed rotating hills keep traversal important; capture progress and contested rules must be clear. |
| Team battle / free-for-all | TaCZ-enabled combat while skating, grinding, jumping and chaining tricks; movement mastery, aim and route choice all matter. Clear score/time limits, safe spawns and anti-camping layouts. |
| Territories / domination | Control several distributed objectives, creating decisions between defending, rotating, tagging and intercepting. |
| Moving-object possession | A proposed Oddball-style carrier/escort objective with explicit carrier restrictions, drop/recovery and anti-hide rules. |
| Elimination / limited lives | Proposed round-based high-stakes variants with short rounds, useful spectator flow and fair resets. |
| Infection / chase | Proposed asymmetric pursuer-versus-survivor rounds; readable role changes and mobility-balanced escape routes. |
| Race / checkpoint rally / tag rush | Retain pure movement and graffiti-focused modes alongside combat: gang races, time trials, relay variants and competition over taggable areas. |
| Survival / onslaught | Fight pursuing enemy waves while skating and performing tricks; growing pressure, speed and encounter complexity with solo and cooperative play as design targets. |

Beyond the explicitly requested CTF, hill and survival modes, these are proposed familiar-mode interpretations to validate, not a locked shipping catalogue. Prefer mechanically distinct objectives over many near-identical labels. All major gangs should contribute distinct opponents, styles and encounter variations through shared systems.

### Survival: escalating speed, pursuit and mastery

The explicit fantasy is enemies trying to catch and kill the player while the player skates, tricks and shoots through increasingly intense waves. Build an encounter director around routes, flanks, readable attack windows and recovery space. Escalate enemy composition, coordination, pursuit pace, moving objectives and occasional elite/boss waves rather than relying only on health inflation or unlimited spawning.

Make speed escalation legible and movement-tested. Introduce higher-speed routes and pressure progressively; do not silently overwrite player controls, momentum, weapon behavior or impose impossible AI acceleration/teleport catch-up. Exact speed scaling and whether optional boosts are involved remain design decisions. Always retain achievable exits, landing/braking space and response windows.

Proposed structure: preparation → wave → short resupply/reposition window → tougher wave, with optional endless continuation and clear voluntary extraction. Define ammo/resupply, recovery/revive, failure, rewards and anti-farming. Do not assume specific TaCZ pickup or reload APIs exist. Reward skillful mobility and combat without making a forced trick animation interrupt aiming or dodge input.

### TaCZ integration and competitive rules

- Audit the installed TaCZ version and existing compatibility contract before choosing hooks. Verify shooting, aiming, reloads, recoil, weapon switching, hitboxes and projectile/hitscan behavior across ground skating, grinds, aerial tricks and landings. No integration or native validation is claimed here.
- Keep gameplay and weapon state server-authoritative with responsive prediction; never delay shots or tricks to musical beats. Coordinate confirmed combat/trick chains with the shared music, announcer, lights and crew reactions.
- Separate event participation and damage from ordinary vanilla bystanders. Live-world gun modes require proven terrain/explosion/fire and bystander protection; otherwise use isolated arenas. Do not globally change weapon or PvP rules outside the event.
- Explicitly choose normalized event loadouts versus bring-your-own equipment, friendly fire, team balance, respawn, ammo, death/inventory and rewards. Preserve player-owned equipment and prevent reward/resource duplication.
- Keep non-gun racing/tag/dance modes usable when TaCZ is absent. TaCZ-required modes should clearly declare that dependency and become unavailable with an explanatory fallback rather than silently misbehaving.

### Generation must fit the selected mode

Generate and validate objective topology first: CTF needs balanced bases and multiple return/interception paths; hill needs contest/re-entry space; territories need fair travel costs; survival needs tested spawn approach corridors, escape loops and resupply positions. Avoid unavoidable spawn kills, sightlines straight into respawn zones, unreachable camping ledges and objectives dominated by a single weapon angle.

Use biome-native materials, layered temporary geometry, underwater capability checks and Nether/End all-time rave eligibility already specified here. Safety, claims, cleanup and real collision remain mandatory. Variety comes from objective layouts, faction tactics, course modules, biome mechanics, team roles and optional declared rule modifiers. Seed variation alone is insufficient.

### Mode implementation and acceptance backlog

- [ ] Inspect existing event, gang, movement, damage and TaCZ compatibility systems; document exact versions and supported hooks.
- [ ] Build a shared mode lifecycle with objectives, teams, joins/leaves, scoring, victory/failure, overtime, rewards and idempotent cleanup.
- [ ] Prototype and human-playtest CTF, hill and survival end to end before broadening the catalogue.
- [ ] Validate mode-specific course generation, multi-route fairness, spawn safety, anti-stalling and AI navigation under real skating speeds.
- [ ] Test TaCZ shooting/aiming/reloading and tricks together across movement states, multiplayer latency and weapon packs; prevent control/animation conflicts.
- [ ] Test survival escalation, co-op joins/disconnects, ammo exhaustion, extraction, downed players, boss waves and long-session performance.
- [ ] Define transparent progression/reputation rewards without replacing existing gang-control and independent-defeat rules or incentivizing farming.
- [ ] Test no terrain/bystander damage, inventory loss/duplication, orphaned objectives or effects across death, cancel, unload, restart and mod absence.
- [ ] Measure repeat-play enjoyment, objective clarity, meaningful variation and gunplay/skating flow; optimize startup/frame/tick costs without removing capabilities or disguising arbitrary content caps as optimization.

All entries are requirements/proposals until implementation and exact-build native evidence exist.


## Runner and crate-collecting platformer modes

**Explicit owner direction:** add modes inspired by Temple Run and Subway Surfers, plus Crash Bandicoot-style platforming that combines destroying boxes and collecting a substantial amount of coins. Adapt these ideas into original JetSetCraft skating challenges, alongside the other objective and combat modes. No copied characters, levels or assets are implied.

### Runner challenges

- Design readable sequences of dodges, jumps, low-clearance maneuvers, rail transfers, branching paths and collectible trails. Movement stays responsive and uses tested JetSetCraft controller capabilities; unsupported actions need implementation before modules can require them.
- Offer finite authored/generated challenge runs and a proposed endless variant with increasing pace, obstacle combinations and risk/reward routes. Lane-guided movement or automatic forward travel may be opt-in mode rules, not a global change to free skating.
- Telegraph obstacles far enough ahead for actual speed, camera and network conditions. Validate complete sequences, safe landing/dismount space and routes through every required obstacle; never generate unavoidable failure to end a run.
- Coin trails should teach a useful line or advertise a deliberate optional risk. Trick multipliers, rival ghost runs and chase pressure are proposed variations, not substitutes for a clear finish/distance goal.

### Crate and coin platforming

- Build courses around jumping, grinding, precision landings, breakable **event-owned crates**, coin collection and satisfying finish goals. Include optional secret routes, bonus rooms and harder completion objectives as proposed replay variations.
- Clearly distinguish the normal completion target from optional all-crates/all-coins mastery. A required coin quota must be achievable on validated routes; show progress and what remains. Exact quotas and reward tiers are configurable design decisions, not invented fixed numbers.
- Give crates readable interactions: validated contact/trick/melee actions, with TaCZ shooting only in a mode that explicitly supports it. Distinguish bonus, hazard and ordinary crates visually and through fair telegraphs; do not turn arbitrary world chests or blocks into targets.
- Coins and destroyed-crate state use stable event IDs and server-authoritative once-only credit. Decide personal versus shared/team collections explicitly; prevent duplicate credit after respawn, reconnect or simultaneous hits. Use event score/collectibles by default, not uncontrolled vanilla-item drops or assumed permanent currency.
- Design checkpoints, retries and missed-collectible recovery so a failed jump does not require needless repetition; higher-stakes variants can declare stricter rules. Preserve inventory and normal survival state.

### Shared generation and acceptance

Use the existing layered, biome-material-aware course system: ice lines, warped/crimson rails, cave routes and ocean stages only when their movement requirements are implemented and tested. Nether/End events retain always-eligible rave staging. Crates, coins, hazards and lighting clear with the event and must never damage or harvest the source world.

- [ ] Prototype one finite runner and one crate/coin platform stage with complete scoring, finish/failure, checkpoint/retry and cleanup loops.
- [ ] Verify coin quotas and crate objectives remain achievable; optional branches reconnect and required collectibles cannot spawn inside blocked geometry.
- [ ] Test increasing-speed reaction windows, swept collision/collection, camera readability, multiplayer authority and accessibility.
- [ ] Test missing/duplicate collectibles, simultaneous collection/destruction, disconnects, checkpoint resets, unload and crash recovery.
- [ ] Human-playtest flow, variety, satisfying crate feedback, collectible placement and meaningful mastery; profile generation/render/tick/startup costs without feature regression.

These are recorded requirements and design proposals; implementation and native tests remain pending.
