# Connected Streets — verification receipt

## 3 November continuation

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
