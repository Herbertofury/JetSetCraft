# SCP roster candidate regression evidence

Initial candidate `d1ab998b05d8448e54bac445d91497237174aac9` compiled, but native CI failed in the existing `huntCountsOnlyAcceptedOwnedCaptainAndEliteDefeats` fixture across multiple provider lanes. This is reproducible suite evidence, not a dismissed flaky run.

- [Alex's Mobs job](https://github.com/Herbertofury/JetSetCraft/actions/runs/43698969643/job/132765628508): bounded director did not spawn an owned elite for the hunt fixture; one required test failed.
- Base Java17 and full-provider jobs reported the same assertion. The new SCP/Lucy tests were not reported as the failing case.
- A retry request was rejected because the workflow was still running; no successful retry is claimed.

Inspection found the runtime elite cadence counted attempted spawns: `sequence` advances before entity creation/placement/equipment admission, then `% 5` selected elites. Rejected admissions could therefore consume elite positions. The repair keeps placement/species attempt rotation unchanged but advances a separate admission counter only after successful world insertion and equipment/director acceptance. Elite scheduling uses the next accepted slot. With no rejected admissions this retains the existing cadence; failed attempts no longer silently consume it. This is a diagnosed mechanism and candidate repair, not a proven universal cause until final native comparison succeeds.

The original failed runs remain retained. Re-run final native CI and the existing hunt fixture; do not weaken its assertion, raise its attempt limit or claim rendered character acceptance from a roster/encounter counter test.
