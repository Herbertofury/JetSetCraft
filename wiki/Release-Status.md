# 🚦 Release & Capability Status

![Release](https://img.shields.io/badge/JetSetCraft-0.3.0-16d6d1?style=for-the-badge)
![Commit](https://img.shields.io/badge/commit-d512258-24292f?style=for-the-badge)
![Forge](https://img.shields.io/badge/Forge-47.4.23-f16436?style=for-the-badge)

> [!IMPORTANT]
> The published stable release remains `0.3.0`. The current verified development alpha is `0.4.0-alpha.7`, merged through [PR #46](https://github.com/Herbertofury/JetSetCraft/pull/46). Its 63-test/nine-configuration and 23-capture results identify the development source; they do not replace the stable artifact. Alpha.6 and earlier receipts retain their own historical identities.

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

## ✅ Current verified development: 0.4.0-alpha.7

**Merged and verified through [PR #46](https://github.com/Herbertofury/JetSetCraft/pull/46).** This continuation adds optional room contracts and a shared Pit stop choice to the existing arena circuits, headliners, build evolutions and shared progression. It does not add a separate permanent currency.

| System | Implemented behavior |
|---|---|
| **Optional arena contracts** | Relay follows ordered beacons through observed riding; Paint counts accepted fresh in-room wall patches; Hunt counts owned elite/captain defeats. Missing contract data defaults to none. Required room time, defeats and headliner gates still determine the clear. |
| **One shared Pit stop budget** | An actual room clear earns one service; completing its optional contract earns two total. Each service kind is usable once per stop. Unused choices expire on route departure or Encore. |
| **Tune / Patch / Rewind / Remix** | Tune adds one owned stat/artifact rank within its cap. Patch recovers up to 40% maximum vitality and 50% equipped ward capacity. Rewind restores one reroll and one banish within the 3/2 caps, retaining draft exclusions. Remix retains the exchanged artifact's exact rank and spends the same budget. |
| **Accepted feedback and report** | Server-calculated previews show capped effects; successful services have actual before/after results. A bounded event feed reports accepted caches, mastery, contracts, clears, headliners, evolutions and services. Optional Schema 1 Last run counters record contracts banked on clears and accepted services; older reports read these as zero. |

The server validates the owner, run UUID, current revision and between-arena window. A pending XP draft must be resolved first; room-six services work at the Encore decision. Invalid, stale, repeated and no-effect requests do not spend credits. Clear receipts guard the service opening, while the existing crew settlement remains the only permanent room payout.

[Base verification](https://github.com/Herbertofury/JetSetCraft/actions/runs/41397720888) and the [provider matrix](https://github.com/Herbertofury/JetSetCraft/actions/runs/41397720946) passed **63 required Forge GameTests in each of nine configurations — 567 executions of 63 distinct tests**, all builds and normal dedicated-server save/shutdown. Base also passed its **23-capture native-client gate**. An independent local replay passed all repository validators, clean build, the 63-test suite, normal dedicated-server smoke and the native audit; all 23 local PNGs were visually reviewed. The [contract/Pit stop receipt](https://github.com/Herbertofury/JetSetCraft/blob/main/docs/verification/contracts/README.md) records the source identities, local JAR hash, actual Paint clear and Tune result, and explicit fixture limitations. Native ForgeGradle source-set execution is separate from an independently installed distributable-JAR runtime.

| Development identity | Verified value |
|---|---|
| Local tested checkpoint | `3e211313f8b2fd4898abc05c9836099817f6a249` |
| Feature source | `56b27b6b42ad55ac668e40ae294cadecf1f3948c` |
| Tested PR integration | `e8115bf2339318273618846a26e1bdb40434aa8b` |
| Actual merge | [`af8e7f1b7a41b0e491f04d3f3f8ca4288ac5eed7`](https://github.com/Herbertofury/JetSetCraft/commit/af8e7f1b7a41b0e491f04d3f3f8ca4288ac5eed7) |
| Identical tested/merged tree | `e33c221bb02df4c949c28766007893215d7ca2ed` |
| Network protocol | **15**; matching alpha clients and servers required |

## ✅ Historical development: 0.4.0-alpha.6

The [headliner/evolution/debrief receipt](https://github.com/Herbertofury/JetSetCraft/tree/main/docs/verification/headliners) records [PR #45](https://github.com/Herbertofury/JetSetCraft/pull/45), merge `6e85fb3015620a19a6bd4a69b0154cbe8d248527`, **55 passing tests in each of nine configurations**, builds and normal dedicated-server save/shutdown. Base passed its nineteen-capture audit gate; nineteen independently reproduced local PNGs were reviewed. CI archive bytes and CI pixels were not independently inspected. These results belong to alpha.6, not alpha.7.

The [alpha.5 Style Library receipt](https://github.com/Herbertofury/JetSetCraft/tree/main/docs/verification/style-library) and [alpha.4 reward/journey receipt](https://github.com/Herbertofury/JetSetCraft/tree/main/docs/verification/reward-journeys) preserve their respective 44-test/fifteen-capture and 35-test/thirteen-capture checkpoints.

## ✅ Historical development: 0.4.0-alpha.3

[PR #41](https://github.com/Herbertofury/JetSetCraft/pull/41) merged the shared-activity and arena-circuit implementation on **2026-11-01**. It builds on the earlier Rival Rush and crew-house checkpoints. The published stable release above remains `v0.3.0`; this section records the tested development source and its own evidence.

| Development identity | Verified value |
|---|---|
| Checkpoint | `0.4.0-alpha.3` — activity progression and arena circuits |
| Feature source | [`c733852b260ef5dbf2cb54e7d41fc98ac10b8363`](https://github.com/Herbertofury/JetSetCraft/commit/c733852b260ef5dbf2cb54e7d41fc98ac10b8363) |
| Tested PR integration | [`2626a8d63c7c065a6ca5c241610993a28bdb58ca`](https://github.com/Herbertofury/JetSetCraft/commit/2626a8d63c7c065a6ca5c241610993a28bdb58ca) |
| Actual merge | [`e57e9ab9732a84d90e6ac35a82439e54cd4461ab`](https://github.com/Herbertofury/JetSetCraft/commit/e57e9ab9732a84d90e6ac35a82439e54cd4461ab) |
| Identical feature/tested/merged tree | `e5e127156d0335a9c6f2e33ba153e8ecfc75e757` |
| Completed gates | **28 real Forge GameTests in each of nine configurations**, builds and dedicated-server readiness; normal server save/shutdown and **ten reviewed real-client captures** in base. The eight provider smoke jobs asserted startup only. |
| Exact artifact and scope | [Arena-circuit verification receipt, machine-readable record and gallery](https://github.com/Herbertofury/JetSetCraft/tree/main/docs/verification/arena-circuit) |

[Base verification #280](https://github.com/Herbertofury/JetSetCraft/actions/runs/41356358215) and [provider matrix #56](https://github.com/Herbertofury/JetSetCraft/actions/runs/41356358302) passed. The nine configurations are base; Alex's Mobs; Alex's Caves; Cataclysm; those three together; Aether; Iron's Spells; AoA; and Aether/Iron/AoA/Cataclysm together. [Testing and Verification](Testing-and-Verification) links each job and identifies its actual assertions.

### Implemented in the historical alpha.3 checkpoint

| System | Alpha.3 implementation |
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

Expanded territory simulation, authored story chapters, physical posse/resident/follower AI, Junior Atlas, further competitive formats and other long-horizon concepts remain design lineage. Crew-profile progression, gang reputation, player-built houses, facilities, dispatch jobs and the street activities described above are implemented in the historical development checkpoints. Alpha.7 adds the verified contract and Pit stop systems listed above; human pacing, long Endless sessions, broad native spellbooks and the complete Dungeons-inspired catalogue remain further work.

## 🔗 Canonical records

- [GitHub release](https://github.com/Herbertofury/JetSetCraft/releases/tag/v0.3.0)
- [GitHub Actions verification](https://github.com/Herbertofury/JetSetCraft/actions)
- [Canonical Drive folder](https://drive.google.com/drive/folders/1npHf1VwOj-tybm791XShz2IvY8LmjjUU)
- [Repository release evidence](https://github.com/Herbertofury/JetSetCraft/blob/main/docs/RELEASE_EVIDENCE_0.3.0.md)
- [Current alpha.7 contract/Pit stop receipt and 23-capture local gallery](https://github.com/Herbertofury/JetSetCraft/blob/main/docs/verification/contracts/README.md)
- [Historical alpha.6 headliner, evolution and debrief receipt](https://github.com/Herbertofury/JetSetCraft/tree/main/docs/verification/headliners)
- [Historical alpha.5 Style Library receipt](https://github.com/Herbertofury/JetSetCraft/tree/main/docs/verification/style-library)
- [Historical alpha.3 development receipt and ten-client-capture gallery](https://github.com/Herbertofury/JetSetCraft/tree/main/docs/verification/arena-circuit)
- [Merged activity and arena implementation — PR #41](https://github.com/Herbertofury/JetSetCraft/pull/41)
