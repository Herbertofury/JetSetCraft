# 🛼 JetSetCraft

![Release](https://img.shields.io/badge/release-0.3.0-16d6d1?style=for-the-badge)
![Minecraft](https://img.shields.io/badge/Minecraft-Forge%201.20.1-62b347?style=for-the-badge)
![Movement](https://img.shields.io/badge/movement-momentum%20first-f4d03f?style=for-the-badge)
![Stable verification](https://img.shields.io/badge/stable%200.3.0-9%20GameTests-e84393?style=for-the-badge)
[![Alpha verification](https://img.shields.io/badge/development%20alpha.2-19%20GameTests-f4d03f?style=for-the-badge)](https://github.com/Herbertofury/JetSetCraft/actions/runs/41350812751)

> [!IMPORTANT]
> **JetSetCraft turns ordinary Minecraft worlds into a momentum-first street-sports playground without replacing the player, the world, or vanilla control.** Inline skates, quad skates, a street board, BMX, hoverboard, and scooter share one expressive movement language built around tricks, grinding, flow, dance, graffiti, and emergent Minecraft physics.

## ✨ Start here

| I want to… | Go here |
|---|---|
| Install the verified release and take a first ride | **[Getting Started](Getting-Started)** |
| See exactly what is shipped and tested | **[Release & Capability Status](Release-Status)** |
| Understand the complete feature set | **[Feature Overview](Feature-Overview)** |
| Learn every input without permanent forward motion | **[Controls](Controls)** |
| Pick skates, a board, BMX, hoverboard, or scooter | **[Ride Styles](Ride-Styles)** |
| Build tricks, combos, transfers, and Flow | **[Tricks, Combos & Flow](Tricks-Combos-and-Flow)** |
| Paint tags, create custom art, or use Free Paint | **[Graffiti](Graffiti)** |
| Choose an isolated skating roguelite session | **[Rival Rush](Rival-Rush)** — optional `0.4.0-alpha.2` development mode |
| Build a house, develop crew profiles and send dispatch jobs | **[Player Chapters and Crew Houses](Player-Chapters-Posse-and-Mob-Archetypes)** |
| Browse games, music, art and engineering inspiration | **[Inspiration & Research](Inspiration-and-Research)** |
| Build a compatible modpack or server | **[Modpack Maker Guide](Modpack-Maker-Guide)** |
| Inspect architecture and verification evidence | **[Developer Architecture](Developer-Architecture)** · **[Testing](Testing-and-Verification)** |

## 🧪 Optional Rush and crew-house alpha

**[Rival Rush](Rival-Rush)** adds a separate thirty-minute or Endless skating survival session: named rival crews, paint combat, temporary upgrade drafts, independent Rush XP, persistent mob cards, wall rewards and music stops. A reusable paper + cyan-dye flyer opens its menu. Players opt in and can return to their saved Minecraft state; the alpha requires Easy, Normal or Hard.

The development version is **`0.4.0-alpha.2`**. Rival Rush first arrived through [merged PR #39](https://github.com/Herbertofury/JetSetCraft/pull/39); [merged PR #40](https://github.com/Herbertofury/JetSetCraft/pull/40) adds the alpha.2 [player-built crew house](Player-Chapters-Posse-and-Mob-Archetypes), named roster profiles, physical facilities, dispatch jobs and optional Rush preparation. **Visible residents and follower AI remain future work.** Stable **`v0.3.0` is unchanged**, with its existing **nine** GameTests. The current development suite has **nineteen**: those nine, six Rival Rush tests and four crew-house/integration tests.

**Recorded alpha.2 verification:** [the main Forge/client gate](https://github.com/Herbertofury/JetSetCraft/actions/runs/41350812751) and [all four optional-provider configurations](https://github.com/Herbertofury/JetSetCraft/actions/runs/41350812645) passed. The main gate includes nineteen GameTests, dedicated-server readiness and normal shutdown, plus a real graphical client audit with **eight captures** and an inventory/XP return check. Every provider configuration passed nineteen GameTests and server smoke. [The crew-house receipt](https://github.com/Herbertofury/JetSetCraft/tree/main/docs/verification/crew-house) identifies the tested source, binary and captures; the [fifteen-test alpha.1 receipt](https://github.com/Herbertofury/JetSetCraft/tree/main/docs/verification/rival-rush) remains historical. These are machine integration checks. Human thirty-minute/Endless enjoyment, house progression balance and universal modded-mob behavior remain to be evaluated; [Testing and Verification](Testing-and-Verification) explains the scope.

[![Rival Rush district and mode HUD in the alpha.2 client audit](https://raw.githubusercontent.com/Herbertofury/JetSetCraft/main/docs/verification/crew-house/02-district-hud.png)](https://github.com/Herbertofury/JetSetCraft/tree/main/docs/verification/crew-house)

[View all eight real-client captures and the build receipt](https://github.com/Herbertofury/JetSetCraft/tree/main/docs/verification/crew-house) · [View the four crew-house screens](Player-Chapters-Posse-and-Mob-Archetypes#actual-crew-house-captures).

## 🎧 New research collection

**[Open the visual Inspiration Atlas](https://herbertofury.github.io/JetSetCraft/research.html)** — **505 entries**, including **76 games, 160 music candidates, 40 art/culture references, 38 engineering references and 18 research papers**, alongside the preserved 173-entry earlier catalogue. The **[November 1 expansion adds 136 references](https://herbertofury.github.io/JetSetCraft/research.html?batch=2026-11-01)**, eight creator/replay/social briefs and a **[new 12-track listening route](https://herbertofury.github.io/JetSetCraft/research.html?path=city-after-hours)**. Search by topic or research pass, filter images and direct videos, follow either listening sequence, save a shortlist and export matching entries. The four [research lanes](Inspiration-and-Research) contain the detailed applications, credited media and prototype questions.

## 🎮 The current experience

| Ride, world physics, and compact HUD | Tag selector and custom painting |
|---|---|
| <img src="https://raw.githubusercontent.com/Herbertofury/JetSetCraft/main/art/screenshots/v0.3.0-ride-hud.png" alt="JetSetCraft ride, world painting, and HUD" width="100%"> | <img src="https://raw.githubusercontent.com/Herbertofury/JetSetCraft/main/art/screenshots/v0.3.0-graffiti-selector.png" alt="JetSetCraft graffiti selector" width="100%"> |

## 🟢 Stable v0.3.0 capabilities

| System | Shipped surface |
|---|---|
| **Street movement** | Player-controlled stride acceleration, neutral stopping, powerslides, manuals, ledge vaults, wall kicks, wind redirects, world-geometry grinding, transfers, and preserved vanilla swimming. |
| **Ride identities** | Six ride styles with attached player-following models, distinct tuning, 24 tricks, and 72 validated animation clips. |
| **Flow & presentation** | Combo/Flow scoring, landing grades, a compact vanilla-aligned HUD, reduced-motion controls, and 28 dance moves with multiplayer cyphers. |
| **Graffiti** | 139 persistent aspect-aware tags, custom tag creation, Tag/Free Paint modes, 16 colors, six-face splashes, and throwable paint balloons. |
| **Minecraft physics** | Ice, slime, honey, Soul Speed, water, bubbles, redstone rails, pistons, explosions, currents, knockback, and optional Create geometry remain useful movement tech. |
| **Street Gear & gangs** | Reversible gear on compatible source-owned mobs, physical Boombox tuning, data-driven gang identity, bounded actors, anti-farm cleanup, and original entrance stingers. |
| **Stable verification** | Deterministic asset generation, nine real Forge GameTests, clean client/server builds, dedicated-server smoke, real-client visual audit, and TestGrid protocol proof. The current Rival Rush/crew-house development suite and its nineteen tests are recorded separately above. |

## 🧭 What makes JetSetCraft different

**The player stays the player.** Weapons, tools, inventory, enchantments, dimensions, mobs, rails, terrain, redstone, fluids, and optional mods remain part of the same play space.

**Neutral means neutral.** Releasing directional input settles through Minecraft friction to a complete stop. Sprint kickoffs, vaults, wall kicks, and redirects require deliberate input—there is no permanent camera-forward motion.

**The world is the skatepark.** JetSetCraft follows real rail shapes, collision-shape ledges, walls, and optional Create tracks rather than requiring a separate course made from special blocks.

**Research is additive.** The project studies excellent work across Java, Bedrock, tools, models, animation, graffiti, music, and vehicles while preserving JetSetCraft's identity and recording permission/provenance boundaries.

```mermaid
flowchart LR
    I[Player Input] --> M[Momentum Core]
    W[Minecraft World Physics] --> M
    M --> R[Ride Style]
    R --> T[Tricks + Transfers]
    T --> F[Flow + Landing]
    F --> H[Vanilla-aligned HUD]
    R --> G[Grinding]
    R --> P[Graffiti + Free Paint]
    R --> D[Dance + Cyphers]
    X[Optional Mods] -. additive adapters .-> W
```

## 🚦 Status legend

| Badge | Meaning |
|---|---|
| ✅ **Verified** | Runtime or packaged-artifact evidence exists for the exact release. |
| 🟢 **Implemented** | Present in current source and documented as a current capability. |
| 🧪 **Experimental** | Implemented behind a deliberate compatibility or stability boundary. |
| 📚 **Research** | Inspiration or an implementation reference—not bundled code or a shipped promise. |
| 🚧 **In development** | Active work that is not safe to call complete. |
| 📋 **Design lineage** | Preserved future design, explicitly separated from `0.3.0`. |

## 🔗 Project links

- **Repository:** https://github.com/Herbertofury/JetSetCraft
- **Verified 0.3.0 release:** https://github.com/Herbertofury/JetSetCraft/releases/tag/v0.3.0
- **Research atlas:** https://docs.google.com/spreadsheets/d/1liWhRQL7rmPy8LLgSRVlkrLANqUIJE4UbFX3--KMJjg/edit?gid=1570163195#gid=1570163195
- **Canonical project Drive:** https://drive.google.com/drive/folders/1npHf1VwOj-tybm791XShz2IvY8LmjjUU
- **Roadmap:** [Roadmap](Roadmap)

> [!NOTE]
> Natural gang hangouts, story chapters, physical posse systems, Junior Atlas and other future concepts remain design lineage unless **[Release Status](Release-Status)** marks them shipped. The optional [Rival Rush](Rival-Rush) and [profile-based crew-house alpha](Player-Chapters-Posse-and-Mob-Archetypes) have their own implementation boundaries and recorded evidence.

---

**New here?** Continue with **[Getting Started →](Getting-Started)**.
