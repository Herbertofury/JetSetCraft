# Connected Streets — verification receipt

## Hosted acceptance checkpoints — PR remains draft

[PR #50](https://github.com/Herbertofury/JetSetCraft/pull/50) carries the current source and check status. The records below identify exact tested candidates; a result never transfers automatically to a later source revision. Required CI remains incomplete, and no release or merge is claimed.

### Fifth candidate: every executable native suite passes; client ship transition times out

Candidate `8d19826d5e7d78d5046707d667a6cea9f6d1d12b` has tracked tree `f697380c64947b494cb6e00f9ab7c87f41ef9c85`. Its observed merge checkout `8a56fc3c58a8238cf6906904fa8a29af12b1bac0` was independently verified to have the identical tree. All six configurations that compiled passed their **139 required native cases**: base, Alex's Mobs, combined adventure, Iron's Spells, full Liberty and intentional missing-pack Liberty. The generalized delayed-probe fixture now passes in Alex's Mobs and both other executable Wave 2 configurations; all three also passed their server gates. The same five Create dependency TLS failures remain, with 15 bounded failed attempts. [Base](hosted-attempts/05-base.json) and [Wave 2](hosted-attempts/05-wave2.json) records preserve these exact outcomes.

The [base workflow](https://github.com/Herbertofury/JetSetCraft/actions/runs/41678802091) also passed dedicated startup/save and all **26 native captures**, including the real settled-feedback marker. The downloaded base archive matched its GitHub digest; its alpha.10 JAR matched the independently downloaded full-provider JAR and the successful installed-server report: **6,634,631 bytes**, SHA-256 `cd6e862bc392f8ecaa3d25c07eead9921af93cc8efc77caf8b6a3e9275dee43e`. Root individually inspected the three new-page images from this exact candidate. The [selected-image record](hosted-attempts/05-root-image-review.json) preserves their hashes, dimensions and scope: only three of 26, after genuine notification completion; no active-toast-at-minimum or human-playthrough claim.

Full-provider [Liberty](https://github.com/Herbertofury/JetSetCraft/actions/runs/41678801956) passed its native suite, dedicated smoke and independent installed-JAR audit, then **failed the real-client gate**. After genuine early return and cleanup of all 14 tracked paint entities, phase 241 waited for ship arrival until its original 45-second deadline. The screen remained the returned campaign map with the prior “Operation aborted” status and a usable Board button. The artifact contains **17 PNGs: 16 completed required captures plus the failure image**. Ship hardware, native deck walking and the final home-return captures (16–18) did not run. The downloaded client archive digest and every existing PNG metadata entry were verified; only the failure image received individual pixel review. The [complete Liberty record](hosted-attempts/05-liberty.json) retains this failed gate alongside the earlier successful gates.

The source follow-up corrects a demonstrated audit timing assumption. The old 20-client-tick wait did not prove the production two-server-tick action interval had elapsed after asynchronous return cleanup. The exact rejected guard was not logged, so the silent throttle remains a supported diagnosis rather than an established historical fact. The audit now records actual overworld game time after cleanup, waits for a read-only observation at least two server ticks later and a matching current menu revision, then presses the existing Board button exactly once. One server observation may be outstanding, sampled at most once per 20 client ticks; diagnostics record clock, state, dimension and revision. The 45-second limits, real arrival/deck/return assertions and all 19 required captures remain. Production input handling and gameplay are unchanged. The [inspection rationale](hosted-attempts/06-boarding-rationale.json) and [candidate-6 input delta](hosted-attempts/06-source-delta.json) identify this correction separately; fresh hosted acceptance belongs to the resulting candidate and is recorded in PR #50.

### Fourth candidate: native provider repair passes; delayed fallback probe exposes the same setup gap

Candidate `f2b021bea8780f54a540d0042d31d12db0300811` has tracked tree `a5d8356ef23f76277f6fffc619aa25a7d4efc744`. Its observed PR merge checkout, `3f6e0a08d2fd359e08c160f009eb148c28e0621e`, was independently verified to have the identical tree. The [base run](https://github.com/Herbertofury/JetSetCraft/actions/runs/41676734842) again passed all 139 required cases, all 17 Connected Streets markers, dedicated-server smoke and 26 native captures, including genuine feedback completion. [Both Liberty configurations](https://github.com/Herbertofury/JetSetCraft/actions/runs/41676734880) passed all 139 cases. Full-provider Liberty additionally passed dedicated startup/save, the independent installed-JAR audit and all 19 native client captures.

The actual native MCDivers pass marker appeared at **16:46:49 UTC**; its unchanged damage and world-preservation requirements passed after the native fixture elevation. The installed candidate-4 JAR was **6,634,009 bytes**, SHA-256 `f415b677f5cb63a32eca5c5b1fd10ad90b528996ac0ece82f22315ec54ef9989`. That is the hosted installation record; candidate-4 archives were not independently downloaded or individually viewed locally. Complete [base](hosted-attempts/04-base.json), [Wave 2](hosted-attempts/04-wave2.json) and [Liberty](hosted-attempts/04-liberty.json) records retain exact gate outcomes and artifact metadata.

In Wave 2, combined adventure and Iron's Spells again passed all 139 cases and dedicated-server gates. Alex's Mobs completed the suite with one required fixture failure: the delayed CHROMAGEDDON target at `point + (45, 0, 0)` had obstructed body space when spawned. Its body cube was prepared before effects began and was correctly not cleared again. The log does not identify the obstructing block or entity. Five other configurations remained blocked by the same official Create TLS failure, with all 15 bounded failed attempts preserved.

The following source revision extends the already-reviewed above-surface platform to all four call-in fixtures, including the fallback hardware case. Native-provider geometry remains unchanged from candidate 4. Every relative position, delayed spawn, effect/damage assertion, foreign control, protected terrain/chest NBT, cleanup check and deadline remains; no terrain is rewritten after effects begin. Read-only diagnostics report nearby blocks and entity bounds if another body obstruction occurs. The [candidate-5 source delta](hosted-attempts/05-source-delta.json) records this single test-input change against the 703-input baseline; production gameplay and UI remain unchanged from candidate 3. Local syntax passed for all 247 sources and independent reviews found no blocker. Fresh hosted results belong to the resulting candidate and are recorded in PR #50.

### Third candidate: base and five configurations reach native execution

Candidate `89357025e515503c70ecf43cf90bf433b96623d2` has tracked tree `d4136930d9b5db431bc9c3fe2b8a5cc8da727b8d`. GitHub ran the merge checkout `97d523abe7aa4538315bda35aa3e779e5b1e1044`, whose tree was independently compared with the candidate. The [703-input local source manifest](hosted-attempts/03-source-manifest.json) identifies the production, native-test, resource, Gradle and workflow inputs separately from both the Git tree and the compiled JAR.

| Configuration | Observed result on candidate 3 |
| --- | --- |
| [Base](https://github.com/Herbertofury/JetSetCraft/actions/runs/41674056219) | Clean Forge build; all 139 required native cases; all 17 new feature markers; dedicated startup/save; all 26 native client captures; artifact checksums. |
| [Alex's Mobs, Iron's Spells, combined adventure providers](https://github.com/Herbertofury/JetSetCraft/actions/runs/41674056189) | Each passed all 139 required cases and its dedicated-server smoke gate. The Radiant fixture repair passed in every configuration that reached native execution. |
| [Intentional missing-pack Liberty](https://github.com/Herbertofury/JetSetCraft/actions/runs/41674056101) | Clean build and all 139 required cases. Full-provider installation and client gates are intentionally inapplicable to this configuration. |
| Full-provider Liberty | Clean build; 139 cases completed with one required failure in the existing native MCDivers ordnance/sentry fixture. Real provider actors appeared, but the target's health remained 1000. Installed-JAR and native-client execution were skipped. Its 618-byte client artifact contains empty metadata, not screenshots. |
| Aether, Alex's Caves, all Wave 2, Advent of Ascension, Cataclysm | Each exhausted three bounded build attempts at the official pinned Create slim-JAR URL with an SSL certificate-chain error. These jobs did not reach native execution. |

The complete [base](hosted-attempts/03-base.json), [Wave 2](hosted-attempts/03-wave2.json) and [Liberty](hosted-attempts/03-liberty.json) records preserve successful gates and failures. Optional-provider cases explicitly skip when the corresponding provider is absent; 139 completed required cases do not imply every optional mod was loaded. Successful cached builds do not establish recovery of the Create endpoint. Its pinned version, official endpoint and TLS verification remain unchanged.

The candidate-3 base artifact is **8,984,296 bytes**, archive SHA-256 `b995cf2564b230e5919e38574eddd6090223b8ad4b194abd9bffb5e340ef599d`. The extracted alpha.10 JAR is **6,632,153 bytes**, SHA-256 `35ea1d71ee9e170acdfaf28a6ffe54154d47f4da34be19378cc4407e76f00a5f`; its included checksum was independently verified. This JAR did not receive an independent full-provider installed-artifact audit in candidate 3 because the prerequisite native test failed.

The three new-page images were individually inspected: Crew Tour headings, milestone requirements, training destination and controls are unobstructed; its scrolled minimum 320 × 240 GUI remains navigable; the Street Mix preview states “This round, up to” above the launch action. The actual `FEEDBACK_SETTLED` marker preceded all three captures. This is a three-image review of the 26-image audit, not a human playthrough or a captured active notification at minimum size. [Image metadata and review scope](hosted-attempts/03-root-image-review.json) record exactly what was inspected.

Candidate 3 keeps native Rush fixtures pristine during admission, without changing production combat. It bounds reward notifications to 304 GUI pixels and waits for their genuine completion before page captures. The fourth candidate's test-only fixture follow-up places the native MCDivers platform above the highest surface across its already-loaded approach chunks. The previous fixed Y=100 left firing paths and the orbital approach exposed to generated terrain; [pinned-provider inspection](hosted-attempts/04-fixture-rationale.json) confirms real block-ray visibility and vanilla explosion exposure. The follow-up asserts open landing columns and a clear firing path, requires genuine sentry age and residency, and preserves the original 320-tick effect deadline, real damage, foreign-entity protection, terrain/chest preservation and light cleanup. Detailed failure diagnostics report actual actor positions and authorization. The old failure's exact impact path cannot be reconstructed from its log. The [fourth-candidate source delta](hosted-attempts/04-source-delta.json) identifies its single changed native-test input among 703 inputs; production gameplay is unchanged from candidate 3. Its acceptance and the later fallback-fixture follow-up are described above.

### Second candidate: first complete base and full-provider acceptance

Candidate `23976383cbf8a3f49d753c1e3795ca2df6738544` (tracked tree `09969da2a5b15543a6a0ba50dc1b1f47a1ba9852`, runner checkout `0d42296e0a6e2147c835a8134657e66dbc48a5c3`) passed the complete [base workflow](https://github.com/Herbertofury/JetSetCraft/actions/runs/41670932195): **139 required native cases**, dedicated startup/save and **26 client captures**. The [full Liberty configuration](https://github.com/Herbertofury/JetSetCraft/actions/runs/41670931894) also passed **139 cases**, real difficulty-10 home safety, an independent installed-JAR audit and **19 native captures**. The combined adventure and Iron's Spells configurations passed all their native and server gates.

Five Wave 2 configurations failed on the same Create TLS error. Alex's Mobs and the intentional missing-pack configuration each exposed a remaining Radiant fixture failure, at admission readiness and overheal conversion respectively. Exact failures and successful gates remain in the [base](hosted-attempts/02-base.json), [Wave 2](hosted-attempts/02-wave2.json) and [Liberty](hosted-attempts/02-liberty.json) records. The later third candidate repaired both Radiant failures, while a different existing full-provider case then failed.

The downloaded candidate-2 base and full-provider JARs match the independently installed artifact: **6,628,611 bytes**, SHA-256 `ee582ce2abf740a1a53fb98877086f7e642d21407cbc27c6606729688d24893c`. That identity belongs to candidate 2 only. The [selected four-frame base review](hosted-attempts/02-selected-base-qa.json), [three new-page review](hosted-attempts/02-root-image-review.json) and [six-frame Liberty review](hosted-attempts/02-selected-liberty-qa.json) distinguish actual pixel inspection from automated capture counts. The new-page images revealed a genuine reward notification covering page headings and cropping after resize; candidate 3 corrected the width and capture timing. Human pacing and multiplayer load remain separate playtesting work.

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
