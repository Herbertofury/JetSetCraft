# 🔬 Inspiration & Research Atlas

![References](https://img.shields.io/badge/references-173-16d6d1?style=for-the-badge)
![Categories](https://img.shields.io/badge/categories-12-f4d03f?style=for-the-badge)
![Scope](https://img.shields.io/badge/scope-Java%20%2B%20Bedrock%20%2B%20tools-8b5cf6?style=for-the-badge)

> [!IMPORTANT]
> **This is a research and inspiration catalogue, not a dependency list or a blanket reuse license.** An entry means “study this mechanic, asset pipeline, presentation choice, or implementation boundary.” Code/assets enter JetSetCraft only after license, author permission, provenance, technical fit, and regression checks are satisfied.

The canonical source is the [JetSetCraft Ultimate Research Atlas](https://docs.google.com/spreadsheets/d/1liWhRQL7rmPy8LLgSRVlkrLANqUIJE4UbFX3--KMJjg/edit?gid=1570163195#gid=1570163195), reconciled against the [project Drive](https://drive.google.com/drive/folders/1npHf1VwOj-tybm791XShz2IvY8LmjjUU). The current second scour contains **173 verified/compiled references** across Minecraft Java, Bedrock, Marketplace, source repositories, model sites, animation libraries, creators, tools, audio, and textures.

## 🧭 Browse by research lane

| Lane | References | What it covers |
|---|---:|---|
| [**Core Street Sports**](Research-Street-Movement-and-Rides) | 12 | Skating, boards, tricks, grinding, and street-sport culture |
| [**Movement & Parkour**](Research-Street-Movement-and-Rides) | 20 | Momentum, vaults, slides, wall actions, and input feel |
| [**Rides & Vehicle Physics**](Research-Street-Movement-and-Rides) | 12 | BMX, boards, scooters, hoverboards, mounts, and vehicle handling |
| [**Graffiti & Painting**](Research-Graffiti-Music-and-Audio) | 9 | Surface painting, custom art, spray UX, decals, and paint physics |
| [**Music, Radio & Boombox**](Research-Graffiti-Music-and-Audio) | 8 | Physical music players, radio, sequencing, and multiplayer audio |
| [**Audio & Texture Sources**](Research-Graffiti-Music-and-Audio) | 2 | Sound and texture libraries with explicit asset-rights checks |
| [**Dance & Animation**](Research-Animation-Models-and-Creators) | 18 | Emotes, cyphers, animation stacks, blending, and runtime synchronization |
| [**Mocap & Animation Assets**](Research-Animation-Models-and-Creators) | 8 | Motion capture libraries and retargetable performance references |
| [**Model & Asset Hubs**](Research-Animation-Models-and-Creators) | 35 | Modeling tools and models for skates, boards, bikes, props, and environments |
| [**Creator & Commission Sources**](Research-Animation-Models-and-Creators) | 17 | Artists, animators, modelers, and commissioning/contact routes |
| [**Code & Frameworks**](Research-Frameworks-Java-and-Bedrock) | 9 | Animation, geometry, data, networking, and integration architecture |
| [**Bedrock & Marketplace**](Research-Frameworks-Java-and-Bedrock) | 23 | Bedrock add-ons/worlds and cross-edition mechanic references |

## ⭐ Highest-priority deep dives

| Rank | Project | Lane | Score | Why it matters to JetSetCraft | Links |
|---:|---|---|---:|---|---|
| 1 | **Blockbench** | Model & Asset Hubs | **9.90** | Primary production tool and conversion hub for boards, skates, BMXs, hoverboards, boomboxes, props and animation. Direct native fit. | [Project](https://blockbench.net/) · [Source](https://blockbench.net/wiki/docs/bbmodel/) |
| 2 | **Emotecraft** | Dance & Animation | **9.75** | Top reference for user-extensible dances, emote serialization, networking, radial UX and mixed-server compatibility. | [Project](https://modrinth.com/plugin/emotecraft) · [Source](https://github.com/KosmX/emotes) |
| 3 | **Create: Train Track Rail Grinding** | Core Street Sports | **9.60** | Exceptional stress case for JetSetCraft continuous rail transforms, moving-world adapters, portal transitions and preserving velocity without desync. JetSetCraft should deliver the same continuity through optional additive adapters. | [Project](https://modrinth.com/mod/create-rail-grinding) · [Source](https://github.com/JuniKnytt/CreateRailGrinding) |
| 4 | **Dream Burst Spirit Vector** | Core Street Sports | **9.60** | One of the closest design references to JetSetCraft. Study skill-generated style economy, chained skate parkour, data tags and the way special abilities spend movement-earned Poise while JetSetCraft keeps its own grounded ride identities. | [Project](https://modrinth.com/mod/spirit-vector) · [Source](https://github.com/HamaIndustries/spirit-vector) |
| 5 | **GeckoLib** | Dance & Animation | **9.40** | Reference for data-driven event keyframes, easing vocabulary, asset pipelines and robust cross-loader animation tooling. | [Project](https://www.curseforge.com/minecraft/mc-mods/geckolib) · [Source](https://github.com/bernie-g/geckolib) |
| 6 | **ParCool!** | Movement & Parkour | **9.35** | Reference for clean action-state transitions, wall geometry detection, cancel windows and traversal interoperability around JetSetCraft riding. | [Project](https://www.curseforge.com/minecraft/mc-mods/parcool) · [Source](https://github.com/alRex-U/ParCool) |
| 7 | **Street Art** | Core Street Sports | **9.35** | Closest modern overlap with JetSetCraft. Study how one project connects paint, skating, movement and media without making them feel like unrelated minigames. | [Project](https://modrinth.com/mod/street-art) |
| 8 | **Emote - Server-Side Emotes** | Dance & Animation | **9.30** | Surprisingly strong cypher/dance-battle reference: server-governed emotes, collaborative sequences, hard limits and converters could inform scalable JetSetCraft dance packs without requiring every animation to be compiled into core. | [Project](https://modrinth.com/mod/emote) · [Source](https://github.com/hanhy06/emote) |
| 9 | **Momentum - Parkour Movement** | Movement & Parkour | **9.10** | Very strong modern action-state and latency reference. Mine its input responsiveness, wall-hang momentum preservation, water skipping and movement-to-animation synchronization. | [Project](https://modrinth.com/mod/momentum-parkour) · [Source](https://github.com/AkiraHane/Momentum) |
| 10 | **Player Animation Library (PAL)** | Dance & Animation | **9.10** | Important future-facing comparison because JetSetCraft currently uses legacy PlayerAnimator. Evaluate a compatibility bridge or migration path without regressions. | [Project](https://modrinth.com/mod/player-animation-library) |
| 11 | **I Wanna Skate** | Core Street Sports | **9.05** | Still one of the best Minecraft-native skating mechanic references. JetSetCraft should exceed its trick count, line continuity, animation layering and world integration. | [Project](https://modrinth.com/mod/i-wanna-skate) · [Source](https://github.com/AlexModGuy/IWannaSkate) |
| 12 | **Sable** | Code & Frameworks | **9.00** | High-value torture test for JetSetCraft riding/grinding on moving worlds. Study coordinate transforms and physics edge cases, but keep JetSetCraft additive and avoid Sable-style intrusive global ownership. | [Project](https://modrinth.com/mod/sable) · [Source](https://github.com/ryanhcode/sable) |
| 13 | **Automobility** | Rides & Vehicle Physics | **8.95** | Top architecture reference for modular ride parts, data-driven vehicle archetypes, track pieces and tactile driving feedback. | [Project](https://modrinth.com/mod/automobility) · [Source](https://github.com/FoundationGames/Automobility) |
| 14 | **BBS FS** | Dance & Animation | **8.95** | A major animation production gem. Study IK, per-limb editing, physics and editor UX for authoring polished ride tricks, dances, gang scenes and cinematic validation clips. | [Project](https://modrinth.com/mod/bbs-fs) · [Source](https://github.com/Wemppy4/bbs-fs) |
| 15 | **Locomotion** | Dance & Animation | **8.90** | High-end architecture research for procedural/blended locomotion and action layering if JetSetCraft eventually needs richer stance blending. | [Project](https://github.com/Trainguy9512/locomotion) · [Source](https://github.com/Trainguy9512/locomotion) |
| 16 | **Slide!** | Movement & Parkour | **8.85** | Excellent focused reference for readable slide timing, slope-driven acceleration and keeping animation/vanilla arm behavior coherent during high-speed locomotion. | [Project](https://modrinth.com/mod/slide%21) |
| 17 | **Create** | Code & Frameworks | **8.80** | JetSetCraft already supports Create track optionally; keep studying geometry continuity and moving-world interactions for advanced trick lines. | [Project](https://www.curseforge.com/minecraft/mc-mods/create) · [Source](https://github.com/Creators-of-Create/Create) |
| 18 | **Customizable Player Models (CPM)** | Dance & Animation | **8.75** | Reference for user-authored avatar compatibility, attachment transforms and keeping custom player rigs from clipping ride gear. | [Project](https://modrinth.com/plugin/custom-player-models) · [Source](https://github.com/tom5454/CustomPlayerModels) |
| 19 | **Momentum for Automobility** | Rides & Vehicle Physics | **8.75** | Excellent tuning reference for JetSetCraft ride feel, especially speed-dependent turning and preserving momentum while still allowing skillful braking. | [Project](https://modrinth.com/mod/momentum-for-automobility) |
| 20 | **Splinecart** | Code & Frameworks | **8.70** | Top geometric-path reference for smooth curves, deterministic acceleration zones and rail camera comfort. Valuable for JetSetCraft custom grind rails and spline-like creator blocks. | [Project](https://modrinth.com/mod/splinecart) · [Source](https://github.com/FoundationGames/Splinecart) |

## 🧬 Research already reflected in current work

| Reference | How it informed the current implementation | Boundary |
|---|---|---|
| [Street Art](https://modrinth.com/mod/street-art) · [source](https://github.com/BeeIsYou/street-art) | Surface-aware Free Paint, paint splashes/balloons, rollerblade presentation, and momentum ideas were studied and selectively merged. | Permission and MIT provenance are recorded; JetSetCraft's catalog/custom-tag paths and broader systems remain intact. |
| [Dream Burst Spirit Vector](https://modrinth.com/mod/spirit-vector) · [source](https://github.com/HamaIndustries/spirit-vector) | Sprint kickoff, chained parkour, ledge-vault, wall-action, and momentum-conservation ideas informed the responsive movement pass. | Useful ideas were adapted without inheriting a permanent movement-replacement model or replacing vanilla swimming. |
| Minecraft vanilla survival UI | The HUD is treated as a compact survival-status extension instead of a cinematic overlay. | Vanilla hearts, armor, food, air, mount health, and status-effect UI remain authoritative and unobstructed. |

## 🛡️ Research discipline

1. **Observe the mechanic or pipeline.** Record what problem it solves and where it fails.
2. **Verify provenance.** Confirm source, license, author, version, and whether assets are actually reusable.
3. **Ask when required.** A public project page is not permission to redistribute its models, textures, audio, or Marketplace content.
4. **Adapt, do not flatten.** Useful patterns must fit JetSetCraft's player-controlled, Minecraft-native design.
5. **Prove no regression.** Build, GameTest, client rendering, server startup, multiplayer synchronization, and asset validation remain release gates.

## 📚 Complete catalogue

Every one of the 173 atlas entries appears once in the four pages below:

- **[Street Sports, Movement & Rides](Research-Street-Movement-and-Rides)** — 44 references
- **[Graffiti, Music & Audio](Research-Graffiti-Music-and-Audio)** — 19 references
- **[Animation, Models & Creators](Research-Animation-Models-and-Creators)** — 78 references
- **[Frameworks, Java & Bedrock](Research-Frameworks-Java-and-Bedrock)** — 32 references

> [!NOTE]
> Version, availability, Marketplace purchase state, and project licensing can change. The Sheet remains the richer record for ranking, freshness, permission/contact notes, and implementation caveats; the wiki is the polished browsing layer.

