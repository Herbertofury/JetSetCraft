# Testing and Verification

JetSetCraft treats a passing Java compile as intermediate evidence, not final proof. The repository CI regenerates assets, validates contracts, builds the reobfuscated JAR, runs real Forge GameTests, and starts a real dedicated server.

## Alpha.8 Liberty Grind verification

Liberty Grind adds **21 required cases**, bringing the suite to **84**. They cover mission rules, saved research, real TaCZ loadout and reload behavior, native bullet attribution, pod arrival, accepted graffiti/skating objectives, ship extraction and original-world recovery. The [Liberty verification receipt](https://github.com/Herbertofury/JetSetCraft/blob/main/docs/verification/liberty-grind/README.md) records the exact full-provider, intentionally absent-content and hosted CI results, including failed iterations and their corrections.

The receipt also preserves **fifteen independently reviewed native client captures**, including the visibly seated moving pod, real Liberator, field-radio controls and normal early return at default and minimum UI sizes. A separate official Forge installation ran the final reobfuscated alpha.8 JAR with the pinned TaCZ/Helldivers content, verified registered commands, and completed normal save/shutdown with exit zero. Those installed-package checks and their JAR hash are distinct from ForgeGradle source-set tests.

Client captures demonstrate deployment and early return; full objective completion and ship extraction are exercised by controlled server fixtures. Current missions are individual deployments. Human aiming, session pacing, long-term research balance and shared-squad play are not established by these automated checks. [Play Liberty Grind](Liberty-Grind).

## Alpha.7 original acceptance and fixture follow-up

**The original `0.4.0-alpha.7` gameplay checkpoint merged through [PR #46](https://github.com/Herbertofury/JetSetCraft/pull/46) as [`af8e7f1b7a41b0e491f04d3f3f8ca4288ac5eed7`](https://github.com/Herbertofury/JetSetCraft/commit/af8e7f1b7a41b0e491f04d3f3f8ca4288ac5eed7).** [Base verification](https://github.com/Herbertofury/JetSetCraft/actions/runs/41397720888) and the [provider matrix](https://github.com/Herbertofury/JetSetCraft/actions/runs/41397720946) passed **63 required Forge GameTests in each of nine configurations — 567 executions of 63 distinct tests**, all builds and normal dedicated-server save/shutdown. Base also passed its **23-capture native-client gate**. The suite contains 55 retained alpha.6 cases, four optional-contract cases, three Pit stop model cases and one Pit stop runtime case. Local checkpoint `3e211313f8b2fd4898abc05c9836099817f6a249`, feature `56b27b6b42ad55ac668e40ae294cadecf1f3948c` and tested integration `e8115bf2339318273618846a26e1bdb40434aa8b` share verified tree `e33c221bb02df4c949c28766007893215d7ca2ed` with the merge. The [alpha.7 receipt](https://github.com/Herbertofury/JetSetCraft/blob/main/docs/verification/contracts/README.md) separates CI gate results from the independently reproduced local source runtime, artifact hashes and 23 reviewed local PNGs.

### Post-merge fixture follow-up

[PR #47](https://github.com/Herbertofury/JetSetCraft/pull/47), merge [`1fcb78852f893f083749aac2e566addc3acca78b`](https://github.com/Herbertofury/JetSetCraft/commit/1fcb78852f893f083749aac2e566addc3acca78b), strengthens the existing `rush_headliner_arena_gate` case; the suite remains **63 distinct tests**. A real boss-card pickup must open its earned draft and retain headliner victory before a bounded helper resolves actual offers and advances the manager to exactly one room settlement. Production gameplay is unchanged. The [central addendum](https://github.com/Herbertofury/JetSetCraft/blob/main/docs/verification/contracts/README.md#post-merge-verification-follow-up) records the three failed provider jobs, confirmed midpoint reproduction, separate missing-catalogue local setup failure, and corrected local/base/provider results. Original PR #46 gate results and pixel reviews below are not relabeled as follow-up evidence.

### Original PR #46 gates

| Gate | Observed evidence | Status |
|---|---|---|
| Source and build | Asset generators, all repository validators and a clean Forge build | [PASS — base](https://github.com/Herbertofury/JetSetCraft/actions/runs/41397720888) |
| Base and provider runtime | All 63 required tests in each of nine configurations; build, dedicated-server readiness, RCON stop and normal save/shutdown | [PASS — base](https://github.com/Herbertofury/JetSetCraft/actions/runs/41397720888) · [eight provider lanes](https://github.com/Herbertofury/JetSetCraft/actions/runs/41397720946) |
| Native client | All five audit PASS markers, contract fixture/return observations and **23 named captures**; 23 independent local PNGs reviewed | [PASS — base gate](https://github.com/Herbertofury/JetSetCraft/actions/runs/41397720888) · [local gallery](https://github.com/Herbertofury/JetSetCraft/blob/main/docs/verification/contracts/README.md) |

### Eight added acceptance cases

| Required alpha.7 marker | Behavior under test |
|---|---|
| `rush_contract_catalogue` | Optional contract parsing, legacy no-contract defaults, bounded targets and malformed-data rejection. |
| `rush_contract_relay` | Ordered manager-observed riding; pause, same-tick draft, standard teleport and greater-than-four-block discontinuity resets; blocked standing space. |
| `rush_contract_paint` | Actual east perimeter acceptance, outside-column/duplicate/paused rejection, accepted patch credit before its XP draft and no independent room clear. |
| `rush_contract_hunt` | Actual owned elite/captain defeats; ordinary, foreign and repeated defeats cannot satisfy the goal. |
| `rush_pit_stop_rules` | Canonical clear receipts, one/two shared credits, each service kind once, stale/duplicate rejection, departure invalidation and old Schema 1 report defaults. |
| `rush_pit_stop_effects` | Room-six Encore services, owned capped ranks, actual partial recovery, no-op/no-spend behavior and provider-unavailable targets. |
| `rush_pit_stop_snapshots` | Draft-only exclusions, bounded Rewind refills, precise previews, pending-draft blocking and detached results. |
| `rush_pit_stop_runtime` | Production action handler, actual contracted room settlement and receipt, accepted services/feedback, travel closure, ordinary-state return and persisted counters. |

These eight cases passed within the original PR #46 63-test local suite and each original CI configuration. The model cases call production rules with explicit clear inputs; runtime cases exercise actual server sessions and accepted events. Positioning, prerequisite state and clocks may be supplied to reach a bounded scenario. Calling the production packet handler directly is not a network-socket or codec test. Existing room time/defeat/headliner gates remain required, and completing a side contract alone must not grant a service or permanent award.

The base client gate adds `20-contract-briefing`, `21-contract-progress`, `22-pit-stop` and `23-service-result` after the retained nineteen views. All 23 independent local images were reviewed for readable, truthful state. The new sequence establishes a Paint contract, a normal actual room clear and a confirmed Tune, not native-client Relay/Hunt completion or all service types. Neither this shortened audit nor the provider matrix establishes human pacing, prolonged Endless play, every foreign mob/spell or every mod combination. ForgeGradle runtime tasks execute the source set while separately building the distributable JAR; independently installing that JAR remains a separate acceptance boundary. Local images and local JAR hashes do not establish CI archive or binary equality.

## Historical alpha.6 headliner, evolution and debrief acceptance

[PR #45](https://github.com/Herbertofury/JetSetCraft/pull/45) merged alpha.6 as `6e85fb3015620a19a6bd4a69b0154cbe8d248527`, with the identical tested tree `b2618bbabd94649a88039eefdd2625f9f0839127`. Its [base run](https://github.com/Herbertofury/JetSetCraft/actions/runs/41392665091) and [eight-provider run](https://github.com/Herbertofury/JetSetCraft/actions/runs/41392665154) passed **55 tests in each of nine configurations: 495 executions of 55 distinct tests**, plus builds and normal dedicated-server save/shutdown. Base passed its nineteen-capture native audit gate.

The [headliner receipt](https://github.com/Herbertofury/JetSetCraft/tree/main/docs/verification/headliners) preserves the eleven added encounter/evolution/draft/debrief cases, exact source identities and nineteen independently reproduced, reviewed local PNGs. It distinguishes source runtime from execution of an installed reobfuscated JAR, and local pixels from uninspected CI archive bytes/pixels. Its public runtime record is a redacted summary with original-log hashes, not a reconstructed complete transcript. These results verify alpha.6 only.

## Historical alpha.5 Style Library and build acceptance

The **`0.4.0-alpha.5` implementation merged through [PR #44](https://github.com/Herbertofury/JetSetCraft/pull/44)** passed **all 44 real Forge GameTests in each of nine configurations**, each build and dedicated-server readiness followed by RCON stop and normal saved-dimensions shutdown. This is **396 successful test executions of 44 distinct tests**. The base also passed the **fifteen-screen native client audit gate**. The [Style Library receipt](https://github.com/Herbertofury/JetSetCraft/blob/main/docs/verification/style-library/README.md) records the exact merge and separates CI results from the independent exact-source local visual reproduction.

The feature is [`8bb7109`](https://github.com/Herbertofury/JetSetCraft/commit/8bb7109042decfe636ae0b621b8af88169bf61ce), tested through PR integration [`6701db2`](https://github.com/Herbertofury/JetSetCraft/commit/6701db2997395f5df90861965a081f8c7708df5e) and squash-merged as [`5bad458`](https://github.com/Herbertofury/JetSetCraft/commit/5bad45891b6fe7a5c7c133ed0a19ac0556dd5af8). All three have Git tree **`878476f60413bfe47c18864a99674c363432ce87`**, with an empty feature-to-merge tree diff.

| Final CI evidence | Result and scope |
|---|---|
| [Base run 41388639847](https://github.com/Herbertofury/JetSetCraft/actions/runs/41388639847) | **PASS:** clean build, 44 GameTests, dedicated-server readiness and normal save/shutdown, fifteen-screen native audit gate |
| [Provider run 41388639856](https://github.com/Herbertofury/JetSetCraft/actions/runs/41388639856) | **PASS in all eight lanes:** Alex's Mobs, Alex's Caves, Cataclysm, their combined lane, Aether, Iron's, AoA and the combined adventure lane; each passed build, 44 tests and normal server save/shutdown |

**Artifact inspection boundary:** browser URL policy blocked downloading the CI archives. Artifact metadata is API-reported; CI ZIP/JAR bytes were not independently downloaded or hashed, and CI screenshot pixels were not independently reviewed. Identical CI JAR hashes are not claimed. The fifteen-PNG gallery is a separate local reproduction from the exact source, after the required asset generators and rebuild. It passed all three native audit markers and normal save/shutdown. Its local JAR SHA-256 is **`a5cea430f3b2930b7bae1d479bfda5ac6bf6e08263d3f01e1858835a26fbd847`**; this is not a hash of a downloaded CI binary. Source/log/image provenance belongs to the new receipt, not the older alpha.4 gallery.

### Nine additional acceptance cases

| Alpha.5 marker | Production behavior or model boundary exercised |
|---|---|
| `rush_synergy_rules` | One preparation, three-family cap, bounded independent soul pools, shield timing and pause, remix cleanup and relay admission |
| `rush_synergy_control` | Real owned actors, refreshing Venom, directed Chill, forward Split, foreign-target exclusion, paused/expired effects and native-state preservation |
| `rush_synergy_recovery` | Actual owned-rival defeats, shield absorption, sixth-hit recovery, wall-limited soul burst, nonrecursive secondary damage and normal return |
| `rush_cache_options` | Built floor markers, distinct private previews, actual claims/cooldown/magnet, immutable snapshots and truthful partially capped rewards |
| `rush_cache_boundaries` | Authored obstacles, blocked approaches, current-room charge, between-room hiding and finished-run rejection |
| `rush_route_exploration` | Six built rooms, distinct offered reward families, unvisited profiles, honest history and optional-provider/material resolution |
| `crew_style_shared_rewards` | Production shared settlements, pre-house learning, founding transfer, replay denial, deadline-based mission claim and saved-data round trips |
| `crew_style_persistence` | Real packet codec/handler, stale/negative/unknown/locked rejection, schema 3, schemas 1/2 migration and exact future-payload preservation |
| `crew_style_entry` | Earned provider-gated lessons, actual entry with exactly one starting rank, active-run rejection, unchanged return state and operational-house fallback |

Cache and route cases inject `recordDefeat()` counters and compress clocks; the synergy cases separately settle real session-owned kills through production damage handling. Crew cases supply qualifying transaction inputs and saved mission deadlines rather than replaying complete street games or waiting real mission minutes. Codec/handler checks do not constitute remote-network tests. The [receipt](https://github.com/Herbertofury/JetSetCraft/blob/main/docs/verification/style-library/README.md) preserves these limits.

## Historical alpha.4 reward, journey and build acceptance

The **`0.4.0-alpha.4` implementation merged through [PR #43](https://github.com/Herbertofury/JetSetCraft/pull/43)** passed **all 35 real Forge GameTests in each of nine configurations**, each configuration's build, and dedicated-server readiness followed by an RCON stop and verified normal saved-dimensions shutdown. The base also completed the graphical audit and produced **thirteen reviewed 1280 × 720 native captures**. This is **315 successful test executions**, not 315 different tests. The [reward/journey receipt, machine-readable evidence and gallery](https://github.com/Herbertofury/JetSetCraft/tree/main/docs/verification/reward-journeys) record the exact artifacts and limits.

The verified feature is [`b120db1`](https://github.com/Herbertofury/JetSetCraft/commit/b120db1b0a75870f5b9275f81e5e072f836a3893), tested through PR integration [`b0506b0`](https://github.com/Herbertofury/JetSetCraft/commit/b0506b052e2e8d7b8f762bd5da3e6daea878a27f) and merged as [`e826694`](https://github.com/Herbertofury/JetSetCraft/commit/e826694c3429394b2b264f153cd9ea34b1fb9051). All three have Git tree **`1aab5454652554bffee78299a8f86a2c19013261`**. All nine artifacts contain the same **5,644,758-byte JAR**, SHA-256 **`182e4b589191c288472b059e9be71b89374687768f079580301153d97498e24c`**; their downloaded ZIP digests matched GitHub's artifact records.

| Configuration | Completed alpha.4 job |
|---|---|
| Base Forge 1.20.1 / Java 17 | [Verify JetSetCraft #286](https://github.com/Herbertofury/JetSetCraft/actions/runs/41379581968/job/125067211271): build, 35 tests, normal server save/shutdown and thirteen reviewed client captures |
| Alex's Mobs 1.22.9 | [Provider #59 — alexsmobs](https://github.com/Herbertofury/JetSetCraft/actions/runs/41379581891/job/125067210841): build, 35 tests and normal server save/shutdown |
| Alex's Caves 2.0.2 | [Provider #59 — alexscaves](https://github.com/Herbertofury/JetSetCraft/actions/runs/41379581891/job/125067210692): build, 35 tests and normal server save/shutdown |
| Cataclysm 3.31 | [Provider #59 — cataclysm](https://github.com/Herbertofury/JetSetCraft/actions/runs/41379581891/job/125067210466): build, 35 tests and normal server save/shutdown |
| Alex's Mobs + Alex's Caves + Cataclysm | [Provider #59 — all](https://github.com/Herbertofury/JetSetCraft/actions/runs/41379581891/job/125067211000): build, 35 tests and normal server save/shutdown |
| Aether 1.20.1-1.5.2-neoforge | [Provider #59 — aether](https://github.com/Herbertofury/JetSetCraft/actions/runs/41379581891/job/125067210745): build, 35 tests and normal server save/shutdown |
| Iron's Spells 1.20.1-3.4.0.2 | [Provider #59 — irons_spells](https://github.com/Herbertofury/JetSetCraft/actions/runs/41379581891/job/125067210739): build, 35 tests and normal server save/shutdown |
| AoA 1.20.1-3.7.1 Beta | [Provider #59 — aoa](https://github.com/Herbertofury/JetSetCraft/actions/runs/41379581891/job/125067210702): build, 35 tests and normal server save/shutdown |
| Aether + Iron's Spells + AoA + Cataclysm | [Provider #59 — adventure_all](https://github.com/Herbertofury/JetSetCraft/actions/runs/41379581891/job/125067210795): build, 35 tests and normal server save/shutdown |

### Seven additional acceptance cases

| Alpha.4 marker | Production behavior exercised |
|---|---|
| `crew_progress_history` | Actual pre-house rewards, sixteen-row history, saved identity, once-only founding transfer and a separate longer replay guard |
| `crew_progress_caps` | Credited amounts after caps, explicit busy/no-focus training results and affordable goals that still require physical readiness |
| `crew_progress_mission` | Mission reward goes to its assigned member, with the actual saved level transition, consumed job and replay rejection |
| `rush_journey_preview` | Actual room offers, objectives and reward previews; stale/unoffered selection rejection and immutable snapshots |
| `rush_journey_history` | Real constructed-room admission, bounded six-room history, supplied settlement records without invented payouts, stale receipt rejection and Encore reset |
| `rush_build_rules` | Finite distance charge, genuine aerial observations, exact-rank remix, unavailable-provider and stale-version rejection, and the three-slot limit |
| `rush_build_runtime` | Real manager/owned-rival movement and combat paths, visible-target mark, secondary-hit isolation, pause/expiry behavior and exact source-state return |

The rank-one Breakbeat test preserves its mark and active clock through **five paused manager calls**, then proves the mark remains at active tick 49 and expires at **exactly 50 active ticks**. It checks removal of the visible Exposed label and damage multiplier, no replacement mark hiding the result, and unchanged native mob health/effects. Every configuration recorded `JETSETCRAFT_RUSH_MARK_EXPIRY active_ticks=50 paused_calls=5`. These fixture-driven production calls do not claim a fifty-tick human maneuver or a human balance result.

The new client frames show the first **Journey** map, initial **Build** with empty artifact slots, and **Progress** with empty reward history and a real next house goal. No completed room, remix, claimed mission, displayed earned receipt or developed build is pictured. Server GameTests supply their respective mechanic and settlement evidence.

## Historical alpha.3 activity and arena acceptance

The activity and arena implementation merged in [PR #41](https://github.com/Herbertofury/JetSetCraft/pull/41) passed **all 28 real Forge GameTests in each of nine configurations**, plus every configuration's build and dedicated-server startup checks. The base also verified normal dedicated-server save/shutdown and produced **ten reviewed 1280 × 720 real-client captures**. The [historical receipt and dated scope correction](https://github.com/Herbertofury/JetSetCraft/tree/main/docs/verification/arena-circuit#scope-correction--2026-11-02) preserve exact identities and explain that the eight provider smoke jobs used SIGTERM/SIGKILL cleanup without a normal-shutdown assertion. GameTest-server normal-save output remains separate valid evidence; alpha.4's stronger gate does not retroactively change those jobs.

The verified feature commit is [`c733852`](https://github.com/Herbertofury/JetSetCraft/commit/c733852b260ef5dbf2cb54e7d41fc98ac10b8363), tested through PR integration [`2626a8d`](https://github.com/Herbertofury/JetSetCraft/commit/2626a8d63c7c065a6ca5c241610993a28bdb58ca). PR #41 merged as [`e57e9ab`](https://github.com/Herbertofury/JetSetCraft/commit/e57e9ab9732a84d90e6ac35a82439e54cd4461ab). All three commits have Git tree `e5e127156d0335a9c6f2e33ba153e8ecfc75e757`, tying the merged implementation to the tested source. These results were recorded on **2026-11-01**.

| Configuration | Completed evidence |
|---|---|
| Base Forge 1.20.1 / Java 17 | [Verify JetSetCraft #280](https://github.com/Herbertofury/JetSetCraft/actions/runs/41356358215/job/124997134020): build, 28 GameTests, dedicated-server readiness/save/shutdown and ten real-client captures |
| Alex's Mobs 1.22.9 | [Provider #56 — `alexsmobs`](https://github.com/Herbertofury/JetSetCraft/actions/runs/41356358302/job/124997134620): build, 28 GameTests and dedicated-server startup smoke (normal shutdown not asserted) |
| Alex's Caves 2.0.2 | [Provider #56 — `alexscaves`](https://github.com/Herbertofury/JetSetCraft/actions/runs/41356358302/job/124997134535): build, 28 GameTests and dedicated-server startup smoke (normal shutdown not asserted) |
| Cataclysm 3.31 | [Provider #56 — `cataclysm`](https://github.com/Herbertofury/JetSetCraft/actions/runs/41356358302/job/124997134668): build, 28 GameTests and dedicated-server startup smoke (normal shutdown not asserted) |
| Alex's Mobs + Alex's Caves + Cataclysm | [Provider #56 — `all`](https://github.com/Herbertofury/JetSetCraft/actions/runs/41356358302/job/124997134415): build, 28 GameTests and dedicated-server startup smoke (normal shutdown not asserted) |
| Aether 1.20.1-1.5.2-neoforge | [Provider #56 — `aether`](https://github.com/Herbertofury/JetSetCraft/actions/runs/41356358302/job/124997134603): build, 28 GameTests and dedicated-server startup smoke (normal shutdown not asserted) |
| Iron's Spells 1.20.1-3.4.0.2 | [Provider #56 — `irons_spells`](https://github.com/Herbertofury/JetSetCraft/actions/runs/41356358302/job/124997134635): build, 28 GameTests and dedicated-server startup smoke (normal shutdown not asserted) |
| AoA 1.20.1-3.7.1 Beta | [Provider #56 — `aoa`](https://github.com/Herbertofury/JetSetCraft/actions/runs/41356358302/job/124997134536): build, 28 GameTests and dedicated-server startup smoke (normal shutdown not asserted) |
| Aether + Iron's Spells + AoA + Cataclysm | [Provider #56 — `adventure_all`](https://github.com/Herbertofury/JetSetCraft/actions/runs/41356358302/job/124997134611): build, 28 GameTests and dedicated-server startup smoke (normal shutdown not asserted) |

### Client-audit follow-up

A post-merge repeat passed build, all 28 GameTests and dedicated-server smoke but stopped after eight client captures when the disposable house fixture's activity handoff could not locate its Boombox. [PR #42](https://github.com/Herbertofury/JetSetCraft/pull/42) supplies a real dry supported room, observes grounded client state and hands over the completed home's authoritative anchor. It preserves ordinary item-use packets, native screenshots and return assertions; only two opt-in audit Java classes changed under `src`.

[Verify JetSetCraft #282](https://github.com/Herbertofury/JetSetCraft/actions/runs/41357798877/job/125001513156) then passed its build, all 28 GameTests, server smoke, ten captures and all three visual-audit PASS markers. The feature `fe679cf`, tested integration `c0b2028` and actual merge `76f44fe` share tree `ffc85769b8d50143cb7c2e1c66f240ed01515bf8`. The [complete follow-up receipt](https://github.com/Herbertofury/JetSetCraft/tree/main/docs/verification/arena-circuit#follow-up-repeatable-client-fixture-and-final-guides) records the initial failure and observed room/anchor checks.

The historical alpha.3 JAR remains the exact original PR #41 artifact tested in all nine configurations. PR #42 has its own passing base result; ordinary gameplay sources and resources are identical. That ten-image gallery remains the original inspected capture set, separate from alpha.4.

### Activity and arena test coverage

| Alpha.3 marker | Scope |
|---|---|
| `activity_progression` | Cross-mode pre-house earnings, once-only transfer, training, receipts, account persistence and schema migration |
| `activity_tag` | Actual Boombox preview and launch, source actors, owner attack hooks, six objectives and bystander/replay denial |
| `activity_safety` | Stable random invitation, stale context rejection, three-hit Showdown, orphan/missing actor and anchor removal handling |
| `activity_hill` | Contested capture rejection, physical presence at three hills and 480 real uncontested ticks |
| `activity_dimension_return` | Selected provider versions and actual Aether/Precasia → circuit → exact source return, with unchanged Minecraft/ride state |
| `rush_arena_catalogue` | Live profile loading, optional gating, fallback materials, authored map bounds and forbidden payload rejection |
| `rush_arena_rules` | Six-room completion/Encore, three artifact slots, cache refresh and stale draft denial |
| `rush_arena_session` | Real manager route choices, generated passable connection, actual owned rivals, shared settlement and exact return |
| `rush_spell_relay` | Provider-absent behavior, or the real native Iron Firebolt collision, bounded damage, ownership, virtual defeat and cleanup |

The route rules/session tests use explicit fixture-clock compression for long arena durations. They do not speed up the real-client audit. The graphical audit shows a real Boombox activity preview and a genuine three-choice first route, selects that route, enters the connected-arena travel phase and returns normally. Arena combat, objective completion and settlement are established by GameTests; the client audit does not show a cleared room or a reward screen.

The provider-present proofs passed with the actual source mods loaded. The Iron's and `adventure_all` jobs report native Firebolt admission, collision, bounded virtual damage, foreign-owner rejection and cleanup. The Aether/AoA jobs report actual `aether:the_aether` and `aoa3:precasia` departure and exact source return; the combined job reports both dimensions. Provider-absent configurations separately pass the clean unavailable paths. Full native spellbooks, arbitrary foreign capabilities, every modded mob and long human sessions remain outside these results. See [Compatibility](https://github.com/Herbertofury/JetSetCraft/wiki/Compatibility#rival-rush-dimensions-rivals-and-the-firebolt-relay) for the precise adapter contracts and known upstream messages.

## Versioned verification evidence

The published **v0.3.0** release has **nine** GameTests, as recorded in its [release evidence](https://github.com/Herbertofury/JetSetCraft/blob/main/docs/RELEASE_EVIDENCE_0.3.0.md). Historical **alpha.1** added six Rival Rush tests, reaching **15**; its [receipt and original captures](https://github.com/Herbertofury/JetSetCraft/tree/main/docs/verification/rival-rush) are preserved. **Alpha.2** added four crew-house/integration tests, reaching **19**; **alpha.3** added nine activity/arena tests, reaching **28**; **alpha.4** added seven reward/journey/build tests, reaching **35**; **alpha.5** added nine Style Library, synergy and cache/route cases, reaching **44**; and **alpha.6** added eleven encounter/evolution/draft/debrief cases, reaching **55**. Their receipts retain each checkpoint's actual results. Current **alpha.7 adds eight cases, reaching 63**, which passed in each of nine configurations; the [alpha.7 receipt](https://github.com/Herbertofury/JetSetCraft/blob/main/docs/verification/contracts/README.md) records its own source and evidence. Development evidence does not change the published stable release's version or history.

### Historical alpha.2 checkpoint

The recorded alpha.2 candidate is [`d5ecadd`](https://github.com/Herbertofury/JetSetCraft/commit/d5ecadd0651c35fdffb505fa2790bda718e830db), tested through PR integration [`b9bcec8`](https://github.com/Herbertofury/JetSetCraft/commit/b9bcec8258e164aa41af5838a89df8aac04bd7c7). [PR #40](https://github.com/Herbertofury/JetSetCraft/pull/40) merged as [`b852dfc`](https://github.com/Herbertofury/JetSetCraft/commit/b852dfce9b17eadbb29699ee8d5d50e2c6ab8101), whose Git tree is identical to that tested integration. The [crew-house verification receipt](https://github.com/Herbertofury/JetSetCraft/tree/main/docs/verification/crew-house) records those source identities, the binary, workflow artifacts and captures.

| Configuration | Completed evidence |
| --- | --- |
| Base Forge 1.20.1 / Java 17 | [Verify JetSetCraft #274: clean build, all nineteen GameTests, dedicated-server readiness and normal RCON shutdown, and the eight-screen real Minecraft client audit](https://github.com/Herbertofury/JetSetCraft/actions/runs/41350812751) |
| Alex's Mobs, Alex's Caves, Cataclysm, and all three together | [Provider matrix #51: four separate jobs, each passing its build, all nineteen GameTests and dedicated-server smoke](https://github.com/Herbertofury/JetSetCraft/actions/runs/41350812645) |

These links identify that checkpoint's evidence. Later code changes require their own passing run. Provider jobs establish the documented registry and gameplay regression baseline; they do not prove every optional creature's behavior or every combination of other mods.

## Local source checks

```text
python -m pip install -r tools/requirements.txt
python tools/generate_models.py
python tools/generate_animations.py
python tools/generate_brand.py
python tools/validate_assets.py
python tools/validate_gameplay_contract.py
python tools/validate_premium_polish.py
python tools/validate_java_syntax.py
python tools/validate_wiki.py
./gradlew clean build --no-daemon
./gradlew runGameTestServer --no-daemon
```

The model generator is offline-safe: when the pinned optional graffiti archive cannot be fetched, it removes any partial download and produces the original JetSetCraft fallback catalog.

## Forge GameTests

The nine original tests verify:

1. Hoverboard registry identity, persistent loadout NBT, and the production ground movement solver.
2. Neutral ride input cannot relaunch stale momentum, deliberate input restarts correctly, directional sprint kickoff works while neutral kickoff cannot manufacture motion, and swimming retains the exact vanilla velocity vector.
3. Scooter registry identity, persistent loadout NBT, and the universal `JetSetMovement` path.
4. Breakdance starting with no ride gear, server-owned move selection, completed-phrase scoring, and Flow.
5. Immediate dance cancellation when a combat swing takes presentation/input authority.
6. Stable one-to-one identity for all 28 dance moves, complete 24-trick catalog coverage, and hostile input/NBT sanitization.
7. Same-entity Street Gear identity, physical-item persistence, real entity NBT save/load, clean removal, spider/slime anatomy routing, persistent gangification/de-gangification, and a real placed Boombox proving Zombie Head → Dead Beat tuning plus start/cancel/immediate-restart/target-return behavior.
8. All sixteen registered/craftable paint-balloon colors, deterministic bounded splat encoding, Tag/Free Paint item state, real 1,000-ray surface placement, exact decal materialization, and persistent six-face sizing.
9. Built-in and custom graffiti NBT persistence, including the support position, plus automatic removal after the supporting wall is broken. The current fixture observes a complete supported cycle first, then requires native discard within 40 additional entity ticks; all nine configurations recorded `native_support_ticks=40`.

The six Rival Rush tests introduced in alpha.1 remain part of the current suite:

10. Deterministic three-choice upgrades, rejection of stale or repeated choices, bounded ranks and counters, the thirty-minute decision pause, and explicit Endless continuation.
11. All seven mob-card mastery thresholds, increasing milestone rewards, three starting-draft unlocks, distinct gang/species identities, saved album data, and unchanged Minecraft XP.
12. Recovery through the production return-journal path, exact original inventory and ride NBT, survival state and effects, preserved album progress, and rejection of incomplete, foreign-owner, nonfinite, or duplicate-slot journals before inventory mutation.
13. Actual session entry and exit across dimensions, loan equipment, exact original position/items/ride/XP restoration, and Forge capability invalidation/revival with the same durable ride data.
14. Real director-created actors and card pickups, independent player ownership, rejection of cross-session damage and duplicate claims, and suppression of event XP without changing unrelated Minecraft mob XP.
15. The real spray-can placement hook and points for a fresh wall, rejection of repaint farming, and authoritative upgrade actions that reject stale run IDs, stale offer versions, and replays.

The four alpha.2 crew-house/integration tests require these exact CI pass-marker IDs:

| Test marker | Production behavior exercised |
|---|---|
| **`crew_house`** | A real placed Boombox founds one house; physical facility registration and support-dependent upgrades charge the correct supplies; expansion preserves placed blocks, checks overlap before spending and acquires no forced chunks. |
| **`crew_missions`** | Dispatch freezes role-adjusted rewards and a saved deadline; stale, early, repeated and cross-player claims are rejected; the exact ready-time boundary settles supplies, member/crew XP and reputation once; cancellation grants no reward. |
| **`crew_persistence`** | World SavedData round-trips member/job identity, facilities, progress and run receipts; unknown schemas remain read-only; missing anchors retain the crew; relocation clears physical bindings while keeping purchased tiers; duplicate Rush settlement and training of busy members are rejected. |
| **`crew_rush`** | Actual house-to-Rush entry grants one chosen preparation alongside an independent earned album draft; the director creates real owned rivals, production tagging defeats them, and the real return path awards bounded crew progress once while preserving ordinary inventory/XP. |

Mission deadline checks use the persisted ready-time boundary deterministically. The integration test advances only its run's active clock before exercising actual actors and return; it does not claim a one-minute human play session or alter the shared world clock.

The player tests use uniquely identified Forge fake players and a real `ServerLevel` rather than mocked movement classes. Unique profiles prevent parallel GameTests from moving or mutating the same automation player. Isolated activity, arena and graffiti fixtures explicitly supply their required terrain and bounded temporary loading/ticking tickets; those fixture tickets do not add production chunk-loading behavior.

FakePlayer tests establish in-memory lifecycle behavior; they do not establish player-file durability. The real-client audit exercises the actual save acknowledgement and network entry/return paths. Forced process termination, storage failure, arbitrary third-party player capabilities, and a human thirty-minute or long Endless playtest are separate acceptance work; no passing gate above implies those have been exercised.

## Input transport acceptance

Client controls remain change-driven: any digital mask or analog movement change is transmitted immediately. While riding, dancing, or holding live movement input, unchanged state is refreshed every 5 client ticks instead of being resent every tick. A neutral inactive client sends its release state once and then stays quiet. The server independently sanitizes unknown bits/non-finite axes and clears stale armed input after 20 ticks, so the active heartbeat retains a four-times safety margin without needless 20-packets-per-second steady-state traffic.

## Dedicated-server smoke

CI launches Forge with `eula=true`, waits for the real ready line, and fails on lifecycle/mod-loading/fatal JetSetCraft errors. The base and all eight provider jobs issue an RCON stop and require normal saved-dimensions shutdown output and successful process completion. Alpha.7 passed this gate in [base](https://github.com/Herbertofury/JetSetCraft/actions/runs/41397720888) and [all eight provider lanes](https://github.com/Herbertofury/JetSetCraft/actions/runs/41397720946), retaining the explicit normal-shutdown gate introduced in alpha.4. Historical alpha.5 and alpha.6 results remain separately identified. JARs and logs are workflow artifacts whose inspection status belongs to the corresponding receipt. Alpha.5/alpha.6 CI archive bytes were not independently inspected. Older provider startup-only smoke results retain their historical scope.

### Known upstream messages retained in passing logs

The historical alpha.3 [`adventure_all` log](https://github.com/Herbertofury/JetSetCraft/actions/runs/41356358302/job/124997134611) contains two nonfatal upstream issues alongside its successful tests and dedicated-server startup smoke:

- **AoA's remote update response:** Forge's version checker could not parse the response from the URL declared in AoA's `META-INF/mods.toml` (`Expected ':' at line 120 column 13`). This affected update information, not the tested arena or dimension data.
- **Iron's concentration-amulet recipe:** `data/irons_spellbooks/recipes/curios/concentration_amulet.json` referenced unregistered `irons_spellbooks:mithril_ingot`, so Minecraft rejected recipe `irons_spellbooks:curios/concentration_amulet`. That crafting recipe did not load.

The final alpha.4 [combined adventure job](https://github.com/Herbertofury/JetSetCraft/actions/runs/41379581891/job/125067210795) still records the rejected Iron recipe alongside all 35 passing tests and normal dedicated-server save/shutdown. The AoA checker parse failure was not observed in that final job's GameTest or dedicated-server logs; it remains a historical observation.

The [Compatibility warning ledger](https://github.com/Herbertofury/JetSetCraft/wiki/Compatibility#known-upstream-messages-in-passing-provider-logs) identifies the exact resources and evidence. Passing integration results do not certify all provider recipes or features, and the upstream resources and version pins were not modified to conceal these messages.

## Real-client visual audit

Run `gradlew.bat -Djetsetcraft.visualAudit=true runClient --no-daemon` on Windows (or the wrapper equivalent elsewhere). The opt-in harness quick-loads its development world, creates a controlled inline-skate/HUD/painted-wall scene, captures the selector in persistent Free Paint mode and the custom painter, prints `JETSETCRAFT_VISUAL_AUDIT_PASS`, and exits cleanly. It is inert in ordinary launches.

The development CI also runs `./gradlew runClient -Djetsetcraft.rushVisualAudit=true --no-daemon` with a disposable `RivalRushAudit` world prepared by the [build workflow](https://github.com/Herbertofury/JetSetCraft/blob/main/.github/workflows/build.yml). It enters and leaves Rush through real network actions, compares the returned dimension, inventory and vanilla XP with its observed pre-entry fixture, and writes the setup, district HUD, empty-album and return-result captures.

The original PR #46 checkpoint passed its **23-capture gate**, including `JETSETCRAFT_CONTRACT_VISUAL_AUDIT_PASS` and the existing Rush, crew, activity and progression audit markers. The [independent local gallery and receipt](https://github.com/Herbertofury/JetSetCraft/blob/main/docs/verification/contracts/README.md) preserve all 23 reviewed images and the actual fixture/return/shutdown observations. The appended sequence uses three accepted Paint patches and eight actual owned defeats, then compresses only the remaining room clock. The normal manager clear opens two services; a real UI confirmation applies Tune from rank one to two and leaves one choice. The returned report records one arena clear, one contract banked and one service used. These are scripted fixture interactions, not human graffiti controls, a complete six-room journey or evidence that the downloaded CI screenshots/binary were inspected. Earlier Free-streets progression captures belong to a separate run and do not describe this Paint settlement.

### Historical native audit checkpoints

Alpha.6 expanded the base gate to nineteen views by adding **Build paths, Draft agency, Headliner tell and Run debrief**. Its nineteen reviewed local PNGs show the documented early build and actual draft/headliner state, with explicit clock compression to reach the scheduled boss. They do not show a headliner victory or a human five-minute run. The [alpha.6 receipt](https://github.com/Herbertofury/JetSetCraft/tree/main/docs/verification/headliners) records the exact state and return-reward limits.

In the historical alpha.5 audit, the crew sequence used an actual Charter on placed fixture blocks, founded a home through production actions, checked two real roster profiles, registered an operational workshop and dispatched a real timed job. It captured **Home, Crew, Facilities and Jobs**. The activity sequence added **Activities**, **Arena route**, **Journey map**, **Build** and **Crew Progress**; alpha.5 added **Cache Scout** and **Style Library**, bringing that checkpoint to **fifteen 1280 × 720 captures**. Its Rush, crew and activity `VISUAL_AUDIT_PASS` markers, observed fixtures and all fifteen capture markers/files passed the CI gate; direct CI-pixel inspection was unavailable.

For Cache Scout, the harness chooses an actual offered route, then explicitly logs a disposable-world fixture that teleports to its selected arena and loads nearby chunks. The production builder, arrival rules and cache loop supply the displayed state; the fixture does not fabricate clocks, defeats, rewards or clears. The audit requires **zero completed arenas** and records the six live caches. The empty-progression Style Library fixture shows **0 crew XP, 0/16 learned and HOME selected**, with three lessons on page 1 of 6. These are initial-state inspections, not human traversal, an earned cache, unlocked-style or completed-circuit demonstrations. The [alpha.5 receipt and local gallery](https://github.com/Herbertofury/JetSetCraft/tree/main/docs/verification/style-library) identify this independent exact-source reproduction.

**Historical alpha.4 pixel review:** the [thirteen-screen reward/journey gallery](https://github.com/Herbertofury/JetSetCraft/tree/main/docs/verification/reward-journeys) remains that checkpoint's original reviewed evidence. The [ten-screen alpha.3 gallery](https://github.com/Herbertofury/JetSetCraft/tree/main/docs/verification/arena-circuit) and [eight-screen alpha.2 gallery](https://github.com/Herbertofury/JetSetCraft/tree/main/docs/verification/crew-house) retain their own source identities.

In those **alpha.4** captures, the album is empty when capture 03 is taken; the later return result in capture 04 shows **score 95, one rival and one collected card**. Captures 05–07 retain native hover overlays, and the job is still running in capture 08. Captures 09–11 show the Boombox chooser and first route/map; the audit enters connected travel and returns. Capture 12 has **zero artifacts**. Capture 13 has **empty reward history**, with **16/40 Supplies** toward the **25 × 25** house goal. Forge tests cover actual combat/clear/settlement, receipts, artifact behavior and job claims. That visual audit does not complete a room, claim a mission or demonstrate a populated history. Short automated audits do not measure house progression balance, thirty-minute/Endless enjoyment, sustained performance, physical follower behavior or a complete human session.

## Runtime acceptance world

Run:

- `/jetsetcraft build_vanilla_lab`
- `/jetsetcraft status`
- `/jetsetcraft set_momentum <speed>` as an operator-only diagnostic

Then exercise every ride style, dance family, surface lane, rail type, wall, hazard, transfer, combat overlay, dimension transition, death/respawn, reconnect, and multiplayer tracking path.

## Wiki verification

`tools/validate_wiki.py` ensures the complete page set exists, sidebar destinations resolve, internal wiki links are valid, required technical terms are present, and no unfinished-marker language ships. The validated `wiki/` directory is the canonical source published to the GitHub wiki.

## Premium offline gates

The premium gate additionally rejects malformed or degenerate OBJ geometry, unresolved materials/textures, non-finite or visibly unclosed animation loops, hard optional-mod class imports, invasive mixin/ASM paths, stale client-cache regressions, and Java syntax failures before ForgeGradle begins. These checks are strong preflight evidence, but they never replace the real Forge build, GameTests, dedicated-server smoke, or client acceptance.
