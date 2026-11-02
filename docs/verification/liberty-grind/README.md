# Liberty Grind — local verification receipt

**Final local result, 2 November 2026: all 84 required Forge GameTests passed in each of three fresh-world configurations.** The standalone, TaCZ-without-pack and full-provider runs used the same frozen build. Native screenshots and the installed-server startup/save check also passed. Hosted CI, release and merge status are recorded separately below.

The feature implements the requested **Helldivers-inspired expeditions on skates**: choose a district, land in a pod, clear rival crews, paint actual walls, recover credits and samples, then extract aboard a ship. Research connects those expeditions to ordinary graffiti, Rival Rush preparation and the existing shared crew progression. See the [player guide](../../../wiki/Liberty-Grind.md), [implementation scope](../../LIBERTY_GRIND.md) and [pinned pack guide](../../LIBERTY_GRIND_PACK.md).

## Evidence identity and publication

| Gate | Recorded result |
| --- | --- |
| Final standalone / no TaCZ | **84/84 PASS**, process exit 0. Admission reported `provider=false pack=false`; all nine content-dependent Liberty cases recorded explicit skips. [Exact log](gametest-final-base.log). |
| Final TaCZ present / Helldivers pack absent | **84/84 PASS**, process exit 0. Admission reported `provider=true pack=false`; the same nine native cases recorded explicit skips. [Exact log](gametest-final-missing-pack.log). |
| Final full provider | **84/84 PASS**, process exit 0. All five Liberty runtime cases and all four TaCZ native cases executed and passed. [Exact log](gametest-final-full-provider.log). |
| Final local matrix identity | [Machine-readable receipt](gametest-final-matrix.json) and [SHA-256 checksums](GAMETEST_FINAL_SHA256SUMS). Every lane used an absent GameTest world, then preserved its resulting world separately. Source inputs and the built JAR were unchanged after each run; the original world and exact pack were restored afterward. |
| Optional content | TaCZ **1.1.8-hotfix2**, file **9037989**, plus the merged **Helldivers:Liberty Day** pack, file **9341504**. Exact archive hashes and installation instructions are in the pack guide. |
| Earlier full-provider GameTest5 | **84/84 PASS.** Its original [gametest-full-provider.log](gametest-full-provider.log) and [SHA-256 receipt](gametest-full-provider.sha256) remain preserved independently of the final matrix. |
| Dedicated-server readiness and graceful save/stop | **PASS.** The installed Forge server reached readiness in 37.59 seconds, accepted authenticated mod-command checks, saved all five dimensions and exited normally after RCON `stop`. See the [installed-server receipt](installed-smoke.json) and [checksums](installed-smoke.sha256). |
| Final native client gallery | **Native8 PASS; all fifteen PNGs individually reviewed.** [Audit receipt](native-audit.json), [exact source-input manifest](native-source-manifest.json), [log excerpt](native-audit-excerpt.log) and [checksums](NATIVE_SHA256SUMS). The moving pod visibly surrounds its rider. |
| Hosted CI and repository publication | [PR #48](https://github.com/Herbertofury/JetSetCraft/pull/48) records the published head, hosted check results and merge state. The code checkpoint and exact local input/artifact identities are recorded below; later evidence/documentation commits do not change those compiled inputs. This local receipt does not claim to contain downloaded CI binaries or images. |

The final compiled code checkpoint is [238e6458fd74cc1698dd6178c719de0b9b23ee56](https://github.com/Herbertofury/JetSetCraft/commit/238e6458fd74cc1698dd6178c719de0b9b23ee56). The 654 final Java/resource/build inputs have digest `c8715cd200a09416a7690a5f78edccfa785c4b0c0b240505a7853ac90c7de780`. All three runs excluded recompilation and resource processing after the clean build. The installed-server check used the same final `jetsetcraft-1.20.1-0.4.0-alpha.8.jar`, SHA-256 `a550f590cbcd8d80797d8c06a1970f285237295f2ba49fe918568a63aae15a47`.

The separately preserved GameTest5 log is **108,982 bytes**, SHA-256:

```text
82d86c612bdd03b0926782967bfa44331071701a3750d0d4c4d9345cd2825142
```

The [Liberty provider workflow](../../../.github/workflows/liberty-provider-compat.yml) independently defines full-provider and missing-pack jobs, aggregate required-test checks, exact native PASS/SKIP markers, a dedicated-server smoke check and a fifteen-capture graphical audit. This receipt does not invent results for those hosted jobs.

## What the passing server tests prove

The five [production lifecycle cases](../../../src/main/java/com/herberto/jetsetcraft/gametest/LibertyRuntimeGameTests.java) all emitted `JETSETCRAFT_LIBERTY_RUNTIME_PASS` with these suffixes:

| Marker | Exercised behavior |
| --- | --- |
| `liberty_runtime_abort_death_disconnect` | Real deployment, passenger pod and native director ticks; rejection of nested modes; independent Rush/Liberty journals; exact original-world position, inventory, XP, physical ride, mode and survival-state restoration after abort, defeat or disconnect. The source worlds include Overworld, Nether and End. |
| `liberty_runtime_action_identity` | Foreign mission IDs and stale revisions cannot abort, resupply, extract or board. Requests are separated by native ticks; a valid owner can then resupply and return. |
| `liberty_runtime_future_research_recovery` | Future-schema research restores belongings while retaining the earned outcome; a different pending mission nonce is not consumed. Repairing the account settles the retained credits once. |
| `liberty_runtime_serialized_rejoin` | Full player data survives native compressed-NBT serialization and loading into a new same-identity player instance. The production login subscriber restores belongings, retains earned credits, forfeits unextracted samples and rejects repeat payout. |
| `liberty_runtime_full_extraction` | The production director spawns actual owned rivals; native damage events credit them; real `SprayCanItem` placement paints designated walls; radio movement and ordered courier beacons complete objectives; physical caches are collected; the real extraction ship arrives, accepts boarding and returns the player with exact research and shared crew rewards. |

The [operation rules](../../../src/main/java/com/herberto/jetsetcraft/gametest/LibertyOperationGameTests.java) additionally check wall faces, reach, distinct panels, duplicate defeat identities, repeated-paint farming, idle/teleport rejection, ordered relays, cache identity, extraction/landing gates and once-only objective rewards. District allocation resumes monotonically after save/load, refuses invalid/future data and stops at its bound instead of recycling an earlier arena.

The [TaCZ loadout tests](../../../src/main/java/com/herberto/jetsetcraft/gametest/LibertyTaczGameTests.java) verify the actual Liberator/Peacemaker identities, chamber and magazine state, loan ownership, occupied-slot rejection, capped resupply and real provider reload consumption. The separate [native combat case](../../../src/main/java/com/herberto/jetsetcraft/gametest/LibertyGunCombatGameTests.java) calls TaCZ's real shoot API, observes its actual bullet entities, invokes their native collision tick, and checks ammunition consumption, owner attribution, foreign-mission exclusion and removal of an in-flight projectile on return. Its passing marker is `liberty_tacz_combat real_shoot_sweep_damage_credit_isolation_return`.

### Fixture boundaries

These are controlled server tests. They position players and targets, control target health and some operation clocks, and use FakePlayers. The full extraction case uses controlled native mob damage; the separate TaCZ case verifies actual provider gunfire with controlled trajectories. Drop and radio progress use native ticks. Defeat invokes the real Forge death event because FakePlayer ignores ordinary damage/death. Rejoin directly invokes the production Liberty login subscriber because Forge's unrelated tier-synchronization listener requires a connected Netty channel.

The recreated-player case exercises serialization and recovery without killing the process. The suite does **not** establish human aiming, skating feel, twenty-five-minute pacing, network latency or squad balance. Current deployments are individual missions in separate districts; shared squad operations are outside this implementation.

## Research and progression interconnection

The [research tests](../../../src/main/java/com/herberto/jetsetcraft/gametest/LibertyResearchGameTests.java) cover exact spending, stale/unknown requests, immutable staged payouts, replay rejection, bounds, malformed/future saves and the existing single Rival Rush preparation slot.

| Project | Functional connection |
| --- | --- |
| Pressure Saver | Every fourth successful graffiti placement saves durability, including ordinary-world painting. |
| Magnetic Pigment | Selectable Rival Rush preparation: +1.5-block pickup reach. |
| Blast Stencil | Selectable Rival Rush preparation: +12% paint/artifact area. |
| Prism Ink | Selectable Rival Rush preparation: +25% base damage on critical hits. |
| Signal Lacquer | The first Liberty mural needs two distinct panels instead of three. |
| Supply Skates | Later Liberty deployments start with one extra resupply charge. |

Research preparations replace the shared starting choice; they do not stack another starting perk. Successful extraction banks the exact earned credits and common/rare/super samples. Completed objectives also feed Crew Supplies, crew XP, member training and reputation, including rewards earned before founding a house. The full extracted mission test checks the actual once-only shared receipt, including **125 crew XP and 55 training XP**.

Research settlement saves its nonce and payout intent before return. Shared crew settlement retains the outcome until its vanilla SavedData file has been read back and matched against the complete expected payload and current `DataVersion`. Failed acknowledgement leaves the record dirty and recoverable. The district allocator uses the same acknowledgement. This verifies normal write/readback behavior; it is **not a power-loss or fsync guarantee**.

## Native gallery scope

**Native8 completed and all fifteen captures passed individual visual review.** The saved PNGs are exact native copies. The [audit receipt](native-audit.json) and [source-input manifest](native-source-manifest.json) identify the client-tested Java/resource/build inputs independently of the earlier GameTest5 log and later documentation commits. The input digest is `a102469ffdeb48ccf5e7b8e1cf296d9e9597e6583593bf3fb31153c4dc978970`; it covers the explicitly listed files, not the entire Git tree or CI workflows. The later Hill-fixture correction changes only `ActivityGameTests.java` among those inputs. The final local test matrix uses the rebuilt artifact and records its identity separately.

| Captures | Intended evidence boundary |
| --- | --- |
| [01 war map](01-war-map-default.png) – [05 HUD](05-hud-default.png) | Actual activity handoff, district selection, six-project research and HUD controls at the default layout. |
| [06 minimum war map](06-war-map-minimum.png) – [11 minimum district](11-district-minimum.png) | Minimum **320 × 240 logical** layout, scrolling and usable controls. |
| [12 live pod](12-live-pod.png) | Real mounted, descending pod with the surrounding model visibly reviewed; four consecutive moving frames also measured zero rider/seat interpolation error. |
| [13 live gun/HUD](13-live-gun-hud.png) | Actual loan Liberator and active mission HUD. |
| [14 live mission](14-live-mission-minimum.png) | Actual field-radio right-click opens the active mission panel. |
| [15 returned war map](15-returned-war-map.png) | Confirmed ordinary early return with original inventory, XP and dimension restored. |

The client audit does not manufacture research purchases, earned samples or completed objectives. Ship extraction is established by the server test above; the gallery is an early-return visual audit. Earlier pod captures that lacked a visible model are not final visual proof merely because the rider was mounted.

## Earlier attempts retained as failure history

| Local attempt | Result and correction |
| --- | --- |
| GameTest2 | **6 of 82 failed.** Pod/director fixtures exposed synchronous global-tick interference and FakePlayer movement/chunk assumptions. Two crew fixtures also collided with houses persisted by older runs. Native ticks, explicit fixture residency/relocation and unused plots replaced those assumptions; reward assertions were preserved. |
| GameTest3 | **Incomplete: harness crash.** Nested timer registration corrupted GameTest's timer-map iteration. Runtime fixtures now register one initial tick pump and schedule later steps in fixture state. Occupied-plot failures were also still present in that run. |
| GameTest4 | **82/84 passed.** Recreated-player login encountered Forge's connected-only listener, and the native bullet fixture needed target movement history to settle before its sweep. The recovery subscriber is now invoked explicitly; the gun test waits native ticks and retains real projectile/collision assertions. |
| GameTest5 | **84/84 passed.** This is the preserved full-provider log linked above, including all five runtime cases and the real TaCZ combat marker. |
| First local standalone run | **83/84 passed.** An existing Rival Rush fixture failed while settling ordinary room two, before its midpoint headliner. The subsequent fixture adds a strict real-defeat quota prerequisite and detailed objective, time, actor and position diagnostics. It passed all three final fresh-world lanes; the earlier failure was not reproduced in those isolated runs. |
| Initial hosted head `cea72a0` | **9/11 checks green**, including both Liberty provider jobs. Base and Cataclysm each passed **83/84**; both failed the Hill fixture's fixed-time check for three contenders, 35 ticks after launch. The fixture now observes native residency and the bounded production retry window, retains the exact three-contender and capture/reward assertions, and uses one initial native tick scheduler. It passed all three final local lanes. Final hosted results are recorded only after their actual completion. |
| Native pod probes before Native8 | A stationary pod could render while a moving pod separated from its rider because their interpolation histories differed. Per-frame pose/camera traces identified the mismatch. Client interpolation now advances in the transport tick before passenger positioning; Native8 verifies the moving model and seat alignment instead of relying on a renderer callback alone. |
| Final local matrix | **84/84 in each of base, missing-pack and full-provider**, all three process exits 0 and all required Liberty PASS/SKIP markers verified. The three exact logs, final matrix receipt and checksum file are linked above. |

Upstream optional Create-recipe and offline authentication diagnostics remain visible in the original log. They are documented in the pack guide and are distinct from the required GameTest outcome. Failed iterations are not counted as successful verification.
