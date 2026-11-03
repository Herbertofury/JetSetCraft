# Connected Streets — verification receipt

## Current hosted status — partial acceptance, PR remains draft

Candidate `23976383cbf8a3f49d753c1e3795ca2df6738544` (tracked tree `09969da2a5b15543a6a0ba50dc1b1f47a1ba9852`, runner checkout `0d42296e0a6e2147c835a8134657e66dbc48a5c3`) passed the complete [base workflow](https://github.com/Herbertofury/JetSetCraft/actions/runs/41670932195): **139 required native tests**, normal dedicated-server startup/save and **26 client captures**. The [full Liberty configuration](https://github.com/Herbertofury/JetSetCraft/actions/runs/41670931894) also passed **139 tests**, the corrected real difficulty-10 home safety case, an independent installed-JAR audit and **19 native captures**. The [combined adventure and Iron's Spells configurations](https://github.com/Herbertofury/JetSetCraft/actions/runs/41670931836) passed all their native and server gates.

Overall required CI is still failing. Five Wave 2 configurations exhausted three build attempts each with the same TLS certificate-chain failure while resolving the pinned Create slim JAR. The successful cached configurations do not establish endpoint recovery; a focused official-source search found no supported fix. Alex's Mobs and the intentional missing-pack configuration each exposed a remaining Radiant fixture failure, at admission readiness and overheal conversion respectively. Exact failures and all successful gates remain in the [base](hosted-attempts/02-base.json), [Wave 2](hosted-attempts/02-wave2.json) and [Liberty](hosted-attempts/02-liberty.json) records.

The downloaded base and full-provider JARs match the independently installed artifact: **6,628,611 bytes**, SHA-256 `ee582ce2abf740a1a53fb98877086f7e642d21407cbc27c6606729688d24893c`. That identity belongs to candidate 2, not to later edits. The [selected four-frame review](hosted-attempts/02-selected-base-qa.json) and [three new-page review](hosted-attempts/02-root-image-review.json) distinguish actual pixel inspection from the automated capture count. The new-page images revealed a genuine reward notification covering page headings; resizing while it was visible also cropped its fixed-width layout.

The next source revision isolates fixture preparation from ordinary automatic combat, retains the captain and effect requirements, and reports individual readiness/state conditions on failure. It also bounds reward notifications to the supported minimum viewport, waits for real notifications to finish before page captures, and labels a Street Mix preview's payment as the current round's estimate. These changes need fresh native acceptance; neither screenshots nor a merge are implied by the earlier successful jobs. Human pacing and multiplayer load remain separate playtesting work.

## First hosted PR #50 attempt

[PR #50](https://github.com/Herbertofury/JetSetCraft/pull/50) publishes the reviewed contribution as head `d7fcb3ddc14a3b4dcd6aa0a5fd7aca9e505008fd`. The first runner checkout, `fadf597da0c25d7a03e2bb40ff07fd9c8a1ed3a7`, and the reviewed local candidate all have tracked tree `9fa57ea133e0f5f8eaf2b9324fbd49549c37e557`; a public git fetch and tree comparison confirmed that identity. The earlier publication rejection described below is historical: verified repository ownership and the existing branch allowed the continuation to publish through the authenticated repository API.

The first [base run](https://github.com/Herbertofury/JetSetCraft/actions/runs/41668876133) passed the clean Forge build and repository checks, then completed all **139** native cases with **two required failures**. Its downstream dedicated-server and client gates were skipped. The [Wave 2 run](https://github.com/Herbertofury/JetSetCraft/actions/runs/41668876015) had five Create dependency TLS failures; the other three configurations compiled and reached native tests. [Both Liberty provider builds](https://github.com/Herbertofury/JetSetCraft/actions/runs/41668876186) passed, then failed the same two Rush fixtures; the full-provider configuration additionally rejected the native mob-safety fixture's home-defense preflight. Its exact rejected preflight condition was not exposed in that log. These failed runs do not establish full native acceptance or a verified playable artifact.

The follow-up preserves the observed failures and repairs their causes without changing production gameplay or relaxing the required gates:

- Rush correctly rejects an unowned zombie entering its dimension. The finale fixture now asserts that rejection, then obtains a foreign-owner control from a second genuine run and checks its native health and virtual vitality through the real effect.
- The evolution fixtures require both the intended actor count and a successfully admitted captain. They wait at most 100 native ticks for resident actors before running the original combat and return assertions.
- In the combined adventure-provider lane, falling gravel refilled the graffiti fixture's removed support. A small roofed fixture cavity keeps the support column controlled; the test still requires real native support checks, actual support removal and discard within 40 entity ticks.
- The native home-mob fixture now builds above the highest existing surface and keeps its contact controls outside seeded objective routes, with a separate ticket for their real entity-ticking sections. It still deploys the exact offer-selected difficulty-10 plan through production preflight and checks its admitted origin and plan. Failure-only read-only diagnostics expose the previously missing preflight context. These changes correct observable fixture assumptions; the first rejection's exact cause remains unproven until the next run.
- A failed `compileJava` task can indicate dependency resolution before the compiler runs. The build retry classifier now stops immediately on actual compiler diagnostics while retaining the existing three-attempt bound for dependency/service failures. The pinned Create version, official endpoint and TLS verification are unchanged. Successful cached builds do not prove the upstream certificate chain recovered.

The complete first-attempt observations are preserved under `hosted-attempts/`. A later source revision must pass fresh hosted gates before this candidate is treated as accepted. Stable v0.3.0 remains separate.

## 3 November local continuation

The user requested continued JetSetCraft contributions. The earlier Connected Streets patch was recovered on the same verified `48894787914076c050ff9635478cc637e666842b` baseline and received a separate correctness review before native acceptance. Its original checkpoint, failures and source manifests remain below and under `evidence/`; they are not evidence for the revised source.

The continuation adds the following repairs:

- Crew Tour claim revisions include the existing crew-account revision as well as the tour revision. Changing training focus, assigning or cancelling a job, or changing account balances invalidates an old claim preview. The combined revision saturates at `Long.MAX_VALUE` and disables claims rather than wrapping. Read-only inspection does not mutate it.
- Tour previews name the current training recipient, explain pre-house banking, and warn when no focus is selected or the member is away. Refreshed claims keep the existing capped reward and training behavior.
- Soul Burst selects only owned, visible actors admitted by the current arena. A paid, accepted multi-target pulse finishes before its earned XP opens a draft; subsequent artifact effects respect that pause.
- Three additional regression cases exercise recipient changes and save/reload, revision saturation, and a pulse whose first defeat earns a draft. The current suite declares **139 required GameTests**: **17** added since alpha.9. `CrewTourGameTests` now has seven cases and `RushExpandedEvolutionGameTests` five. The native client gate still requires **26 captures**, including real Tour navigation, a 320 × 240 viewport and an unlaunched Street Mix preview.

The reviewed gameplay checkpoint is `752ee7fac8fe5817dcd1b4e33a88c5719889ac06`. [Fresh local evidence](continuation-local/verification.json) records nine passing static/source checks, Java syntax parsing for **247 files**, the production street-round model, **5,940** Liberty plans and repeated determinism checks, and shell syntax checks for **28 workflow steps**. The [703-input source manifest](continuation-local/source-manifest.json) and [checksums](continuation-local/SHA256SUMS) identify that local evidence separately from the original candidate.

The configured environment network path downloaded official Gradle 8.8, but the official Forge Maven metadata request returned **HTTP 403**. The dependency-resolution attempt was stopped, with exit 130; **Forge compilation, native GameTests, native client captures and installed-JAR verification did not run locally**. This is an environment limitation, not a passing or failing Forge compile. Native acceptance and publication identities must be recorded before treating the revised source as verified. The original receipt below records only the earlier prepared candidate.

## Original prepared-candidate identity and acceptance status

This receipt describes the **0.4.0-alpha.10 / protocol 18 / crew schema 4** source contribution on `feature/connected-streets`, based on main commit `48894787914076c050ff9635478cc637e666842b` (baseline tree `a75bcdd877cc530d36c3791d7be845c785e9e0c6`). It is a local candidate, not a verified release. Stable v0.3.0 and the earlier alpha.9 acceptance receipts retain their original identities.

**Passing evidence is limited to the local checks below.** This candidate has not passed a full Forge compilation, native GameTests, dedicated-server loading, the native client audit, installed-JAR verification, hosted provider lanes or human playtesting. There is no new playable JAR or native screenshot set in this receipt. Existing successful receipts elsewhere in this repository do not validate these new source changes.

An empty remote feature branch was created from the baseline, but automatic approval review rejected the first implementation commit because the user's broad improvement request did not explicitly authorize publishing repository changes. The upload was not committed, no implementation PR was opened, and no hosted run was started for this candidate. Publishing the prepared contribution requires explicit user approval; that approval must not be inferred from the existence of the empty branch.

## Implemented contribution

| Area | Behavior in this source |
| --- | --- |
| Street Mix | An optional three-round Boombox format rotates Tag, King of the Hill and Showdown, starting with the selected mode. Relay Tag uses three two-skater waves and physical handoffs; Hill uses five six-second capture sites; Showdown ends with a faster five-hit captain. Each round has its own authoritative actor/reward identity and explicit next-round admission. Quick matches remain available. |
| Shared Crew Tour | Nine core milestones are completable without Liberty or optional providers. Three independent optional milestones recognize qualifying Liberty debriefs. Real accepted results advance permanent counters; explicit once-only claims pay the existing crew account. No new currency or recurring calendar is introduced. |
| Rival Rush | Three additional two-ingredient evolutions bring Build Paths to six: Chromatic finale adds bounded secondary fireworks every sixth accepted primary hit; Bass drop creates three timed landing aftershocks; Soul reprise turns a paid offensive soul burst into vitality recovery and a short firing-interval reduction. One selected evolution, three artifact slots and one preparation remain enforced. |
| Liberty Grind | Six original three-stage operations bring the catalog from 28 to 34 templates. Neon Block Party and Prism Breakout include stages with multiple required objectives that can be completed in either order. Existing planning, difficulty, time, deployment, extraction and reward systems are reused. |
| Player guidance | Activity previews, intermissions, the Crew Tour page, Rush build details, README, wiki and design/handoff notes describe the candidate and separate its evidence from older verified releases. |

### A concrete reward connection

An uncapped complete Street Mix settles **34 Supplies, 220 crew XP, 77 training XP and 22 reputation** across its three wins. Those wins also qualify a fresh account for **First set**, whose explicit once-only claim adds up to **6 Supplies, 50 crew XP, 15 training XP and 3 reputation**. The combined maximum is **40 Supplies, 270 crew XP, 92 training XP and 25 reputation**.

Shared crew XP continues to unlock the Style Library's single Rush preparation; Supplies support the home and its existing facilities. Before founding, training is banked. After founding, training targets the selected idle crew member and can be reduced if no eligible recipient exists. Account caps reduce actual payments, which remain visible in the existing accepted reward receipts. Ordinary Minecraft XP, Liberty requisitions and research samples keep their existing roles.

### Persistence and authority boundaries

- Only accepted activity, arena, qualifying Rush-return and qualifying Liberty-debrief settlements advance the tour. Menu views, cancellation, timed crew-job payments and tour payouts do not supply progress.
- Permanent claim flags are independent of the 16-entry display history and 128-entry settlement guard. An evicted receipt cannot make a milestone claimable again.
- Claims require the current tour revision. Founding changes the reward destination and invalidates pending claims. Unknown, malformed, future or invalid-destination saved progress is preserved in quarantine rather than guessed or overwritten.
- Schema-3 and older migration uses only retained actual receipts; current balances are not converted into invented lifetime history.
- Street Mix checks the next round's terrain, Boombox, eligibility and budget before replacing the paid intermission or allocating its next reward identity. A failed admission leaves that intermission intact. Stale prior-round actions cannot cancel a newer round.
- New evolution secondary effects stay within the current run's owned, visible targets and existing area caps. They do not recursively trigger another evolution effect. Active-time effects pause with draft decisions.

## Completed local evidence

The accompanying `evidence/` directory records the validation commands and exit statuses. Production-input hashes identify the exact dependency-free model code exercised; these checks do not establish Minecraft runtime behavior.

| Check | Result and scope |
| --- | --- |
| Java 17 syntax parser | Passed all **247 Java source files**. Parses source; does not resolve Minecraft/Forge symbols or type-check the complete mod. |
| Asset validation | Passed: ten literal plus sixteen generated item registrations and 72 animation clips. Model/animation generators were run and produced no tracked asset changes. |
| Gameplay contract | Passed with protocol expectation updated to 18. Existing authority, compatibility and return contracts remain required. |
| Premium polish | Passed: seven authored meshes, 72 animation seam checks and compatibility boundaries. |
| Wave 2 static compatibility validation | Passed: 172 curated safe entries and nine hidden technical entries. This is not a loaded provider lane. |
| Wiki and research browser | Wiki validation passed for 53 pages. Research-browser generation check and its 34 JavaScript checks passed. |
| Patch whitespace | `git diff --check` passed. A separate delivery manifest records complete-patch applicability and identity. |
| Actual street-round Java code | Compiled and executed the production `ActivityMode` and `ActivityRun` classes with no Minecraft stubs. Quick and expanded capture thresholds, contested/absent holds, wins, timeouts, cancellation, terminal immutability and eight invalid bounds passed. |
| Actual Liberty planning Java code | Compiled and executed production catalog, difficulty and plan classes with no Minecraft stubs. **5,940 plans** across 66 eligible faction/template pairs, ten difficulties, three durations and three seeds passed deterministic regeneration, bounds, spacing, landing clearance and reinforcement limits. The six seed-209 native-test fixtures also passed the optional-mural overlap check. |
| Objective-count review | The expanded maximum is seven primary plus eleven optional objectives. Shared crew settlement receives normalized primary completion in 0–3; optional sites are not added to that input. No settlement limit was raised. |

The local Gradle bootstrap attempt failed while downloading Gradle 8.8 from `services.gradle.org`, with `java.net.SocketException: Network is unreachable`. There was no usable local Gradle distribution, mapped Forge/Minecraft dependency cache or compiled project output. This is an environment limitation, not a successful or failed Forge compilation. No alternate remote write or network workaround was used after the publication rejection.

## Native acceptance prepared, not executed

Four new native GameTest classes add **14 tests**, bringing the declared suite from **122 to 136**. All remain required. The hosted workflow keeps its dynamic declared-test count and now also requires each new success marker.

| New class | Count | Principal assertions |
| --- | ---: | --- |
| `ActivityMixGameTests` | 4 | Mode rotation, real relay handoffs, hill contests, captain hits, per-round settlement, stale actions, rejected-next recovery, return/cancellation and world/account cleanup. |
| `CrewTourGameTests` | 5 | Decoded claim intents, standalone core completion, independent Liberty claims, save migration/quarantine, cap-limited rewards, 130-award receipt eviction, founding revision changes, Rush entry and unchanged preparation/slot limits. |
| `RushExpandedEvolutionGameTests` | 4 | Recipe constraints, owned target selection and line of sight, bounded finale pulses, actual moving landings and paused aftershock timing, paid soul bursts, vitality/tempo behavior and normal return/debrief. |
| `LibertyStreetMissionGameTests` | 1 | All six new operation state machines, both orders of paired stage goals, accepted objective actions, stage gates, required primary completion, optional-site separation and no repeat reward. This controlled-input state-machine test does not replace real movement/manager integration tests. |

The native client audit retains its existing 23 captures and adds **24-crew-tour**, **25-crew-tour-minimum** and **26-street-mix-preview**. It visits the real Crew Tour, checks scrolling and control bounds at an actual **320 × 240** GUI viewport, opens a Street Mix preview and cancels it before launch. It compares actual before/after server progression, vanilla inventory, XP and location, and restores the viewport. These are authored assertions; no candidate screenshots have been produced.

## Original prepared-candidate completion gate

1. Publish this exact reviewed contribution to the feature branch and open its pull request.
2. Run the clean Forge build, all 136 required GameTests, dedicated-server smoke, native client capture audit, installed-artifact checks and existing compatibility/provider lanes. Repair concrete failures without weakening the gates.
3. Inspect the new native captures and record exact source, workflow and artifact identities. Preserve failed attempts and distinguish automated checks from human playtesting.
4. Merge only after all required checks pass and merge authorization is explicit. Leave the stable release untouched unless separately requested.

Human combat balance, full 20–30 minute mission pacing, long-session replayability and multiplayer load remain playtesting work even after automated gates pass.

## Reproduce the dependency-free checks

From the repository root, use Java 17 and a temporary output directory. The source files under `evidence/` are the harnesses used for this receipt; no game classes are substituted.

```sh
mkdir -p /tmp/jetsetcraft-connected-models
java --module jdk.compiler/com.sun.tools.javac.Main --release 17 \
  -d /tmp/jetsetcraft-connected-models \
  src/main/java/com/herberto/jetsetcraft/activity/ActivityMode.java \
  src/main/java/com/herberto/jetsetcraft/activity/ActivityRun.java \
  src/main/java/com/herberto/jetsetcraft/liberty/LibertyMissionCatalog.java \
  src/main/java/com/herberto/jetsetcraft/liberty/LibertyDifficulty.java \
  src/main/java/com/herberto/jetsetcraft/liberty/LibertyMissionPlan.java \
  docs/verification/connected-streets/evidence/StreetRoundSmoke.java \
  docs/verification/connected-streets/evidence/LibertyPlanCheck.java \
  docs/verification/connected-streets/evidence/ObjectiveCountCheck.java
java -cp /tmp/jetsetcraft-connected-models StreetRoundSmoke
java -cp /tmp/jetsetcraft-connected-models LibertyPlanCheck
java -cp /tmp/jetsetcraft-connected-models ObjectiveCountCheck
```
