# Comparable games, mods and design benchmarks

## Identity and existing research

[JetSetCraft's README](README.md) defines a Minecraft-native street-sports experience, not a replacement for Minecraft. Extend the substantial [Inspiration & Research Atlas](wiki/Inspiration-and-Research.md), [movement research](wiki/Research-Street-Movement-and-Rides.md) and [Liberty mission research](docs/research/LIBERTY_MISSION_RESEARCH.md), rather than starting another disconnected catalogue. Existing research dates and acceptance evidence remain intact.

| Reference and primary source | Relevant documented strength | JetSetCraft comparison goal, not a completed superiority claim |
|---|---|---|
| [Bomb Rush Cyberfunk, Team Reptile](https://team-reptile.com/press/sheet.php?p=Bomb_Rush_Cyberfunk) | Crew territory, graffiti and linked ground/air movement | Evaluate whether one uninterrupted Minecraft line communicates speed, landing, combo and ownership without menu friction. Preserve original art/music rights. |
| [Better Combat](https://github.com/ZsoltMolnarrr/BetterCombat) | Dedicated melee combat system and public integration source | Keep street movement compatible with attack ownership rather than duplicating the combat engine. Compare riding plus attacks with the same provider enabled and absent. |
| [The Aether](https://github.com/The-Aether-Team/The-Aether) | Established Minecraft adventure dimension project | Treat optional adventure integrations as versioned providers. Test leaving and returning from activities without lost inventory or foreign capability state. |

## Focused competitive backlog

- [ ] Movement feel study: identical rail, corner, wall transfer and recovery course; compare retry friction and camera readability with the street-sports reference. Human learning/flow observations supplement frame timings.
- [ ] Combat coexistence: vanilla, Better Combat and firearm-provider lanes; verify hands, animation ownership, first/third person and network correction while riding.
- [ ] Campaign-to-home continuity: invitation, decline, deploy, abort, disconnect, death and return must preserve belongings and clearly explain rewards.
- [ ] Use the existing research catalogue for additional references and attach findings to existing roadmap items, not duplicate speculative feature lists.

**Priority:** native movement correctness and full activity-return safety before adding more modes. Existing automated captures do not establish human pacing or balance.

## Evidence and maintenance contract

Reviewed 2026-11-12. This is a public-source design comparison, not a hands-on benchmark, exhaustive market ranking, feature acceptance, or permission to copy code/assets. Source pages may describe newer loaders or editions than our target. Recheck exact versions, licenses and integration boundaries before implementation. Preserve approved identity and existing progress; do not replace working systems merely because a competitor exists.

During each daily review, check linked official release notes/issues and update only material changes. Record source date, observation, relevance, proposed experiment and result separately. Retain failed comparisons and the last-good evidence. Community complaints are leads, not established product defects. New speculative features remain proposals until approved. Before claiming improvement, compare the same workload and retain latency/frame-time, memory, correctness and recovery results on the actual project build.

## Review log

- 2026-11-12: initial repository-scoped comparison index; official sources reviewed at documentation level. No gameplay, performance or compatibility tests run in this pass. No implementation or native GitHub Wiki publication claimed.

## Candidate foundations, concrete reuse and shared work

| Candidate | Exact useful technique / build-on boundary | Improvement experiment and interoperability |
|---|---|---|
| Existing JetSetCraft activity/provider architecture | Reuse current invitations, isolated mission state, return handling, provider adapters and verification receipts instead of introducing another campaign framework. | Define one versioned activity receipt containing provider versions, inventory/capability restoration outcome and timing; test it in existing native lanes before proposing reuse in Enderloom/TestGrid. |
| [GeckoLib](https://github.com/bernie-g/geckolib) | Evaluate keyframe/event animation, easing and concurrent animation for original ride/crew assets; do not replace already accepted animations without a demonstrated problem. | Prototype one ride plus one combat action while preserving arm ownership; compare pose correctness and client cost. A shared original animation-event fixture schema could serve Bloom & Boom and AoA. |
| [Better Combat integration API](https://github.com/ZsoltMolnarrr/BetterCombat) | Study provider-owned weapon attributes and readiness. Integrate through supported interfaces; do not vendor its implementation. | Share versioned provider-state scenarios with Combat Style Suite: mount, attack, gun takeover, dismount and reconnect. Each repo retains its own runtime adapter and tests. |

**Specific licensing evidence:** GeckoLib [LICENSE](https://github.com/bernie-g/geckolib/blob/main/LICENSE), blob `491454db53d5a2166d8a472d07cc8b60e6d016fb`, is MIT in this review. Better Combat [LICENSE](https://github.com/ZsoltMolnarrr/BetterCombat/blob/26.2/LICENSE), blob `05965515e8dbc57587c88dc70d5d9c480d89dd25`, is All Rights Reserved. Check exact supported integration terms and target release before shipping. BRC and other commercial references remain design inspiration only.

- [ ] Start with one native movement/combat fixture, not a wholesale animation migration.
- [ ] Compare shared receipt fields with existing Enderloom/Atlas formats before inventing a new schema.

### Reuse admission gates

These are **candidate foundations and experiments**, not selected dependencies or measured improvements. Before reuse: pin the exact version/commit and loader; inspect its source and dependency tree; record code, asset, trademark and redistribution terms separately; keep required notices; verify permission where terms are restrictive or unclear. Public visibility is not a license. A current default-branch license is evidence for that snapshot, not every historical release. Prefer an upstream dependency/API or a small original adapter over a fork when it preserves compatibility. Do not copy proprietary game assets or publish private fixtures.

For every admitted experiment retain baseline and candidate artifacts, representative native workloads, correctness/feature parity, cold/warm latency, p95 frame/tick time, memory, interrupted-save/recovery evidence and rollback. Promote only after the actual target improves without losing requirements. Cross-project sharing below is an interface proposal, not permission to change another project's implementation or declare integration complete.
