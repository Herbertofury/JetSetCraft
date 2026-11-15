# SCP roster candidate regression evidence

Initial candidate `d1ab998b05d8448e54bac445d91497237174aac9` compiled, but native CI failed in the existing `huntCountsOnlyAcceptedOwnedCaptainAndEliteDefeats` fixture across multiple provider lanes. This is reproducible suite evidence, not a dismissed flaky run.

- [Alex's Mobs job](https://github.com/Herbertofury/JetSetCraft/actions/runs/43698969643/job/132765628508): bounded director did not spawn an owned elite for the hunt fixture; one required test failed.
- Base Java17 and full-provider jobs reported the same assertion. The new SCP/Lucy tests were not reported as the failing case.
- A retry request was rejected because the workflow was still running; no successful retry is claimed.

Inspection found the runtime elite cadence counted attempted spawns: `sequence` advances before entity creation/placement/equipment admission, then `% 5` selected elites. Rejected admissions could therefore consume elite positions. The repair keeps placement/species attempt rotation unchanged but advances a separate admission counter only after successful world insertion and equipment/director acceptance. Elite scheduling uses the next accepted slot. With no rejected admissions this retains the existing cadence; failed attempts no longer silently consume it. This is a diagnosed mechanism and candidate repair, not a proven universal cause until final native comparison succeeds.

The original failed runs remain retained. Re-run final native CI and the existing hunt fixture; do not weaken its assertion, raise its attempt limit or claim rendered character acceptance from a roster/encounter counter test.


Candidate repair6144277 compiled but the base native GameTest still failed the same bounded elite assertion. It is not a verified fix. Added bounded encounter counters and failure-only fixture snapshots to identify whether the blocked step is admission, selection, paused progression or actor observation, without weakening the assertion or increasing its bounds.


Diagnostic c4cc9ff Alex's Mobs job132769371959 showed an active admitted Linebreaker,22 admissions,18 tracked bodies, but fixture world query visible=[] in a synchronous30-wave loop. This contradicts the initial starvation hypothesis. The cadence candidate is reverted; production spawn cadence stays unchanged. The fixture now yields a real server tick between observations, retaining30 director advances and the requirement to defeat an actual owned elite. Await native results before calling this resolved.


Real-tick scheduling e249d93 still reports an active elite but empty area-query results. The next candidate also resolves the reported elite UUID through ServerLevel's actual entity registry and verifies alive/native Mob/run ownership/elite label before accepting it. It never accepts an internal actor record alone or creates an entity. Failure evidence now includes native registry lookup and player query bounds to distinguish section-query visibility from missing registration.
