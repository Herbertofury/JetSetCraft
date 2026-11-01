# 🚦 Release & Capability Status

![Release](https://img.shields.io/badge/JetSetCraft-0.3.0-16d6d1?style=for-the-badge)
![Commit](https://img.shields.io/badge/commit-d512258-24292f?style=for-the-badge)
![Forge](https://img.shields.io/badge/Forge-47.4.23-f16436?style=for-the-badge)

> [!IMPORTANT]
> This page distinguishes the published stable `0.3.0` release, the separately verified `0.4.0-alpha.3` development checkpoint, outside research, and preserved future design. Development verification does not replace the stable release's identity or artifact.

## ✅ Verified release

| Field | Value |
|---|---|
| Minecraft | Java Edition 1.20.1 |
| Loader | Forge 47.4.23 |
| Release tag | [`v0.3.0`](https://github.com/Herbertofury/JetSetCraft/releases/tag/v0.3.0) |
| Source commit | `d512258381b7a17579a371bf929f232b3c107e0e` |
| Release JAR | `JetSetCraft-0.3.0-forge-1.20.1-d512258.jar` |
| JAR SHA-256 | `9639199cab05ef60fc0207fc107c556c49744ee7a275ff639259ea875618d671` |
| Required side | Client and server |

## 🧪 Stable v0.3.0 acceptance evidence

- Deterministic generation of authored models, animations, and brand assets.
- Asset, gameplay-contract, Java-source, premium-polish, and wiki validation.
- Clean Forge build and **nine real Forge GameTests**.
- Real dedicated-server startup and clean shutdown on Forge 47.4.23.
- Real-client visual audit of riding, the HUD, Tag/Free Paint selection, and the custom editor.
- Minecraft Mod Vault TestGrid Java server-list probe against the exact release JAR.
- Green GitHub Actions verification at the release commit.

See **[Testing & Verification](Testing-and-Verification)** for the complete matrix.

## 🟢 Shipped capability

Street movement, six ride styles, trick/Flow scoring, grinding/transfers, world-physics composition, vanilla swimming boundaries, graffiti/custom tags/Free Paint, paint balloons, dance/cyphers, reversible Street Gear, Boombox tuning, data-driven gangs, configuration, multiplayer synchronization, and accessibility controls are current `0.3.0` surfaces.

## ✅ Verified development: 0.4.0-alpha.3

[PR #41](https://github.com/Herbertofury/JetSetCraft/pull/41) merged the shared-activity and arena-circuit implementation on **2026-11-01**. It builds on the earlier Rival Rush and crew-house checkpoints. The published stable release above remains `v0.3.0`; this section records the tested development source and its own evidence.

| Development identity | Verified value |
|---|---|
| Checkpoint | `0.4.0-alpha.3` — activity progression and arena circuits |
| Feature source | [`c733852b260ef5dbf2cb54e7d41fc98ac10b8363`](https://github.com/Herbertofury/JetSetCraft/commit/c733852b260ef5dbf2cb54e7d41fc98ac10b8363) |
| Tested PR integration | [`2626a8d63c7c065a6ca5c241610993a28bdb58ca`](https://github.com/Herbertofury/JetSetCraft/commit/2626a8d63c7c065a6ca5c241610993a28bdb58ca) |
| Actual merge | [`e57e9ab9732a84d90e6ac35a82439e54cd4461ab`](https://github.com/Herbertofury/JetSetCraft/commit/e57e9ab9732a84d90e6ac35a82439e54cd4461ab) |
| Identical feature/tested/merged tree | `e5e127156d0335a9c6f2e33ba153e8ecfc75e757` |
| Completed gates | **28 real Forge GameTests in each of nine configurations**, with build and dedicated-server readiness/save/shutdown; **ten reviewed real-client captures** in base |
| Exact artifact and scope | [Arena-circuit verification receipt, machine-readable record and gallery](https://github.com/Herbertofury/JetSetCraft/tree/main/docs/verification/arena-circuit) |

[Base verification #280](https://github.com/Herbertofury/JetSetCraft/actions/runs/41356358215) and [provider matrix #56](https://github.com/Herbertofury/JetSetCraft/actions/runs/41356358302) passed. The nine configurations are base; Alex's Mobs; Alex's Caves; Cataclysm; those three together; Aether; Iron's Spells; AoA; and Aether/Iron/AoA/Cataclysm together. [Testing and Verification](Testing-and-Verification) links each job and identifies its actual assertions.

### Implemented in the verified development checkpoint

| System | Current implementation |
|---|---|
| **Shared street activities** | A Boombox chooser for Tag, King of the Hill, Crew Showdown and Rival Rush, plus a revealed random invitation; explicit launch, server-owned objectives, ownership/cancellation checks and reward previews. |
| **Shared progression** | Completed street games, arena clears and eligible Rush settlement contribute Supplies, crew XP, focused-member training and gang reputation. Earnings can be banked before founding a home; persisted receipts reject duplicate settlement. |
| **Rival Rush and circuits** | Optional Free streets with thirty-minute/Endless continuation; six-room arena routes with reward choices and Encore; bounded custom maps, named rival crews, independent run XP, persistent mob cards, graffiti/music objectives and ordinary Minecraft return. |
| **Temporary run builds** | Magnet caches, critical chance/power, ability area and capped upgrades; three artifact slots with JetSetCraft's chain, firework, wind, shockwave, tempo and soul-inspired effects; an optional native Iron Firebolt relay. |
| **Crew profiles and player-built houses** | Persistent named roster profiles, levels/training focus, gang reputation, house radius/capacity expansion, manually placed operational facilities, optional Rush preparation and timed resource/training dispatch jobs. Players build the house themselves. |
| **Scoped provider integration** | Actual Aether and AoA Precasia departure → Rush circuit → source-return checks, plus the provider's real Iron Firebolt collision/ownership/cleanup path. Existing Wave 2 registry and representative gameplay checks remain in the suite. |

Crew members are persistent roster/mission profiles; visible resident and follower AI is not implemented. House expansion increases the space and capacity recognized by the crew system and is not general plot-protection software. The provider checks do not establish universal mob/boss behavior, full native spellbooks, arbitrary foreign-capability restoration or every mod combination. [Compatibility](Compatibility) records the exact pins, tested contracts and the two nonfatal upstream messages retained in the passing logs.

The ten-capture client audit shows the Boombox preview and first arena fork, enters travel and checks return. Arena combat, clears and shared settlement are covered by server GameTests. The album is empty and the dispatched job is still running in their captures; this short audit does not establish a full human circuit, mission completion, thirty-minute pacing or prolonged Endless performance. [View the actual captures and their limitations](https://github.com/Herbertofury/JetSetCraft/tree/main/docs/verification/arena-circuit#what-the-graphical-client-actually-did).

The [nineteen-test, eight-capture alpha.2 crew-house receipt](https://github.com/Herbertofury/JetSetCraft/tree/main/docs/verification/crew-house) and [fifteen-test alpha.1 Rival Rush receipt](https://github.com/Herbertofury/JetSetCraft/tree/main/docs/verification/rival-rush) remain historical checkpoints with their own source identities.

## 📚 Research—not bundled capability

The **[Inspiration & Research Atlas](Inspiration-and-Research)** catalogs external mods, Marketplace worlds, source repositories, creator portfolios, models, tools, and asset libraries. Inclusion means “worth studying,” not “bundled,” “required,” “licensed for redistribution,” or “already implemented.”

## 📋 Preserved design lineage

Expanded territory simulation, authored story chapters, physical posse/resident/follower AI, Junior Atlas, further competitive formats and other long-horizon concepts remain design lineage. Crew-profile progression, gang reputation, player-built houses, facilities, dispatch jobs and the street activities described above are already implemented in the verified development checkpoint; they are separate from these future physical/story systems and from stable `v0.3.0`.

## 🔗 Canonical records

- [GitHub release](https://github.com/Herbertofury/JetSetCraft/releases/tag/v0.3.0)
- [GitHub Actions verification](https://github.com/Herbertofury/JetSetCraft/actions)
- [Canonical Drive folder](https://drive.google.com/drive/folders/1npHf1VwOj-tybm791XShz2IvY8LmjjUU)
- [Repository release evidence](https://github.com/Herbertofury/JetSetCraft/blob/main/docs/RELEASE_EVIDENCE_0.3.0.md)
- [Verified alpha.3 development receipt and ten-client-capture gallery](https://github.com/Herbertofury/JetSetCraft/tree/main/docs/verification/arena-circuit)
- [Merged activity and arena implementation — PR #41](https://github.com/Herbertofury/JetSetCraft/pull/41)
