# JetSetCraft Project Handoff

## Canonical release

- Version: 0.3.0
- Public source: <https://github.com/Herbertofury/JetSetCraft>
- Release: <https://github.com/Herbertofury/JetSetCraft/releases/tag/v0.3.0>
- Local canonical project: `C:\Users\Owner\Desktop\JetSetCraft`
- Google Drive canonical folder: complete 0.3.0 source, release artifact, checksum, screenshots, and verification evidence

## Continuation-safe state

Tag `v0.3.0` remains the stable release baseline. The development source advances through Rival Rush **alpha.7**, the initial Liberty Grind **alpha.8**, and the **0.4.0-alpha.9 Liberty Grind campaign expansion** described below. The stable release reconciles the valuable historical branch work in `docs/RECONCILIATION_0.3.0.md`. Do not revive an archive/export branch as a new base; compare its actual feature content against `main` first.

The physical ride item in `JetSetData` is the source of truth. The server owns movement, scoring, graffiti selection/custom payloads, Street Gear, gangs, and persistence. Clients send bounded input and render sanitized snapshots. PlayerAnimator is client-only; optional integrations remain isolated and optional.

Neutral directional input means neutral: stale momentum cannot manufacture camera-forward travel. Water, lava, and swimming give the entire velocity vector and animation pose back to Minecraft. Preserve those authority boundaries in future movement work.

The custom graffiti format is deliberately bounded to a 16 × 10, 4-bit palette payload. Do not replace it with arbitrary client texture/file uploads. Paid Marketplace and research-only references remain non-redistributable even when present in the private Desktop/Drive source-assets area.

## Current Liberty Grind campaign expansion — 0.4.0-alpha.9

Current source is **alpha.9 / protocol 17**. The mode has six persistent campaign regions, twenty-eight seeded mission templates, difficulty 1–10, 20/25/30 minute budgets and isolated arena radii of 512/640/768 blocks: inclusive widths of 1,025/1,281/1,537. The three frontier routes unlock at 20% liberation of their parent. All ten difficulties are selectable on unlocked routes. Solo operations contribute to shared server liberation, threat and defense offers; shared-squad combat is not implemented.

`LibertyMissionCatalog`, `LibertyMissionPlan` and `LibertyDifficulty` define immutable mission choices and bounded tuning. `LibertyMissionRuntime` owns stage gating and actual action progress for eight primitives: territory, relay, courier, demolition, sweep, scan, escort and defense. A designated-actor hunt is a distinct sweep form. `LibertyManager` performs actual world/entity actions and validates owned damage, surface placement, native core deaths and target identity before credit. Keep optional objectives out of the extraction gate. A bulk paint action captures one active stage and credits only cell centers covered by actually placed overlays.

The campaign director admits at most eight combat actors per operation per tick, caps each campaign at 24–96 residents and shares a 384-actor ceiling with legacy expeditions. Admission orders operations by resident count divided by their own cap, rotating ties. Finite lifetime budgets remain unchanged. Once a second during field play, ordinary campaign infantry more than 160 blocks from the owner can retire only if every living player in that world is more than 96 blocks away. Assigned priority targets, cores and support hardware are preserved; retirement grants no kill/reward credit or lifetime-budget refund. Priority bosses still consume the combat admission budgets. Faction actors retain native Minecraft classes; evoker summons and splitting descendants are excluded from the curated roster.

`LibertyHomeTerrain` resolves each home courier checkpoint against existing loaded ground and constructs bounded escort routes with one-block climbs, detours and body clearance. Runtime clearance checks pause an escort when its route becomes obstructed. Required sites must be grounded before admission, and no home site can progress before valid placement; physically unusable routes leave the offer available without terrain mutation or chunk requests. Terrain generation belongs only to isolated districts. Moving escort/evacuation rigs remain mission markers, not civilian-rescue AI.

The physical ship hub leads to a mandatory pre-pod review of primary, secondary, armor and four call-in slots. `LibertyArmory` discovers the pinned merged pack's 68 live gun definitions and supplies real light/medium/heavy kits; optional compatible native armor is discovered from the installed registry. The call-in catalogue contains 94 sourced selectable entries plus six originals, and 25 separate shared/mission/historical entries. Nine exact MCDivers calls and seventeen exact TaCZ support weapons use native providers when admitted. Other entries have explicit JetSetCraft effects/hardware, with one-rider vehicles. Preserve the provider mapping and fail-closed safety boundaries; never infer native parity from a display name.

`LibertyCampaignData` owns the shared front and once-only campaign results. `LibertyShipProgress` owns independent requisitions, call-in purchases, four equipped slots and four ship modules. The six retained `LibertyResearch` projects keep super credits/samples separate from ship requisitions and crew balances. Pressure Saver, Signal Lacquer and Supply Skates are prerequisites for three modules; the Crew Sample Lab requires a founded crew with 100 crew XP. Signal Lacquer's legacy three-to-two marked-panel effect does **not** reduce campaign territory quotas. Preserve one Rival Rush starting preparation and three Rush artifact slots.

Defense deadlines use active campaign time only. `LibertyMissionPlan.defense` uses the offer UUID and region to select among faction-appropriate templates containing a defense objective. The same offer and settings reproduce its plan; changing difficulty or duration retains its mission family. Losing or missing a regional defense can create an untimed home offer; accepting it is a separate explicit action. Home events use temporary owned actors/overlays and preserve ordinary blocks, chest data, unrelated mobs and existing graffiti. Native MCDivers effects are restricted to isolated districts; safe documented adaptations run at home.

An extraction boarded before the deadline completes departure after the clock runs out; new boarding at or after the deadline is rejected. Preserve once-only successful settlement and sample banking. The return journal must keep the original source world, location, deep inventory, physical ride and captured vanilla state through ship visits, redeployments, disconnects and rewards. Unknown or unacknowledged saved data remains recoverable rather than being overwritten.

**[PR #49](https://github.com/Herbertofury/JetSetCraft/pull/49) merged as `c79b925` with all 11 PR checks passing:** base verification, all eight Wave 2 configurations and both Liberty provider jobs. The full-provider native suite passed **122 required cases**; the exact rebuilt JAR passed a separate installed-server smoke check; and the native client passed its **automated nineteen-capture audit**. The [hosted continuation receipt](docs/verification/liberty-campaign/README.md#hosted-continuation) identifies the matching feature/PR/merge trees, the local 691-input manifest, exact JAR and failed attempts. That local manifest aggregate was not recomputed by CI.

The new client images have **no fresh individual pixel review** because browser automatic approval review blocked their download. Keep the archived 2 November 118-case/689-input checkpoint and its nineteen individually reviewed images attached to their original source and JAR, alongside the alpha.8 archive. Source-set gameplay, installed-package smoke and automated client checks remain distinct evidence. Controlled fixtures do not establish a full human campaign extraction, naturally earned unlock pacing or sustained multiplayer load. Human 20–30 minute pacing, economy and aiming remain playtesting work.

Read [current implementation notes](docs/LIBERTY_GRIND.md), [the player guide](wiki/Liberty-Grind.md), [mission research and historical/current mapping](docs/research/LIBERTY_MISSION_RESEARCH.md), [call-in implementation mapping](docs/research/LIBERTY_CALLIN_IMPLEMENTATION.md), [the dated call-in inventory](docs/research/LIBERTY_CALLIN_CATALOGUE.json), and [exact optional pack/provider setup](docs/LIBERTY_GRIND_PACK.md). These are playable adaptations with explicit limits, not imports of original game assets or every native game mechanic.

## Archived Liberty Grind continuation — 0.4.0-alpha.8

Liberty Grind adds a sixth selectable activity: a server-owned war map, three isolated expedition districts, passenger pod arrival, native TaCZ Helldivers gun combat, rival-clearing/graffiti/skating objectives, field research and a passenger extraction ship. The shared return journal restores the player's source dimension, location, inventory, physical ride, vanilla XP and other captured state. Earned super credits survive an interrupted expedition; field samples require successful extraction. Reward intent and settlement IDs protect recovery from duplicate payouts.

The six projects connect expeditions to ordinary graffiti, Liberty operations and the existing single Rival Rush preparation slot. Completed objectives also bank shared crew supplies, XP, training and gang reputation, including before founding a house. Preserve that single preparation and the three Rush artifact slots. Protocol is **16**; client/server versions must match.

Read [the mode guide](docs/LIBERTY_GRIND.md), [the exact pack and verification guide](docs/LIBERTY_GRIND_PACK.md), [the player wiki](wiki/Liberty-Grind.md), and [the Liberty verification receipt with native gallery](docs/verification/liberty-grind/README.md). The optional native lane pins TaCZ **1.1.8-hotfix2** and verifies the creator's merged Helldivers ZIP by hash; upstream binaries are not committed or redistributed. Missing TaCZ or gun-pack content produces a deployment reason while ordinary JetSetCraft remains usable.

Initial scope is individual operations, with a bounded director and original block-built districts/transports. Shared squads, a persistent galactic campaign, a full stratagem/weapon catalogue and human session/economy balance remain future work. Historical alpha.6/alpha.7 receipts below describe their original source, not verification of this continuation.

## Verified predecessor — 0.4.0-alpha.6

[PR #45](https://github.com/Herbertofury/JetSetCraft/pull/45) merged as `6e85fb3015620a19a6bd4a69b0154cbe8d248527`, with tree `b2618bbabd94649a88039eefdd2625f9f0839127` identical to local checkpoint `f438241e32f8d753cab095ff8f70a03c1302116c`, feature `1e8da0f47b2a9622a9ed454cbe8ac10fafdbad0a` and tested integration `988362f3dc4159df2ded4889059f67c8ed9406b0`. It adds bounded elite/headliner encounters, three rank-three build evolutions, versioned rerolls/banishes, exact next-rank previews, a movable threat/resource HUD and a persistent optional run debrief including its saved exit reason. Protocol is 14. Headliners gate circuit rooms three and six, while Free streets preserves its thirty-minute extraction and Encore. Evolution opportunities are earned once per actual owned victory and selected only at an existing decision stop.

**All nine CI configurations passed on their first attempt:** [base run 41392665091](https://github.com/Herbertofury/JetSetCraft/actions/runs/41392665091) and [provider run 41392665154](https://github.com/Herbertofury/JetSetCraft/actions/runs/41392665154) completed every required step, including **55 GameTests each — 495 executions**, builds and normal dedicated-server save/shutdown. Base also passed its nineteen-frame native gate. The fresh local replay separately passed all five validators and the same base gates; all nineteen local images were reviewed. Preserve the evidence distinction: the new native audit uses logged owned-rival hit/placement and clock-compression fixtures; it observes a live headliner without defeating it, then returns with two defeats, two cards, no arena/headliner clears and no qualifying return award. These are controlled integration/visual results, not a completed human session or proof of CI artifact identity.

Use the [alpha.6 source and verification receipt](docs/verification/headliners/README.md) for exact identities and limits. ForgeGradle executes `sourceSets.main` in native Forge; separately building the reobfuscated JAR does not establish the finish contract's final installed-package gate. New encounter/evolution fixtures use the Dead Beat zombie-family cast. Provider lanes demonstrate the pinned modpack's coexistence and exercised paths, including native Aether/AoA origin return and the scoped Iron's projectile bridge where available; arbitrary foreign capabilities, every native mob phase and broad spellbook/mana coverage remain unclaimed.

## Verified predecessor — 0.4.0-alpha.5

[PR #44](https://github.com/Herbertofury/JetSetCraft/pull/44) merged as `5bad45891b6fe7a5c7c133ed0a19ac0556dd5af8`, with the exact tested tree `878476f60413bfe47c18864a99674c363432ce87`. The [Style Library receipt](docs/verification/style-library/README.md) identifies the nine successful CI configurations, 44 GameTests per configuration, normal dedicated-server save/shutdown gates, and the independently reproduced fifteen-screen local gallery. CI archive metadata and local evidence are explicitly distinguished.

The continuation connects all activity rewards to sixteen permanent Rush starting lessons, adds six artifact families, improves cache visibility and scouting, favors unvisited arena routes, and extends optional provider arenas and editable data-pack examples. Existing crew houses, shared reward receipts, six-room circuits, optional remixes, normal inventory/XP return and mode selection remain part of the current implementation. Use the current [Rival Rush guide](wiki/Rival-Rush.md), [crew-house guide](wiki/Player-Chapters-Posse-and-Mob-Archetypes.md), and [Dungeons I/II implementation matrix](docs/RIVAL_RUSH_DESIGN.md) when planning the next slice.

Human pacing, full-session economy/Endless balance, multiplayer scale, visible crew residents, the complete native Dungeons equipment catalogue, companion/beam systems and broad spellbook/mana integration remain open work. The current custom arena format is a bounded data pack, not an in-game map editor. Preserve the once-only reward transactions, one starting preparation, three artifact slots, optional-provider boundaries and saved return journal.

## Verification entry points

- `python tools/validate_assets.py`
- `python tools/validate_gameplay_contract.py`
- `python tools/validate_java_syntax.py`
- `python tools/validate_premium_polish.py`
- `python tools/validate_wiki.py`
- `gradlew.bat clean build --no-daemon`
- `gradlew.bat runGameTestServer --no-daemon`
- `gradlew.bat -Djetsetcraft.visualAudit=true runClient --no-daemon`
- `gradlew.bat -Djetsetcraft.rushVisualAudit=true runClient --no-daemon` — Rush/crew/activity/progression audit, only with the workflow's disposable `saves/RivalRushAudit` setup.
- `python tools/prepare_liberty_pack.py` — hash-checked optional Helldivers pack setup; see the pack guide before running.
- `gradlew.bat -PlibertyTacz=true runGameTestServer --no-daemon` — real guns, native mission lifecycle and all base regressions.
- `python tools/prepare_liberty_helldivers.py` — hash-checked optional MCDivers provider setup; follow the current pack guide.
- `gradlew.bat -PlibertyTacz=true -PlibertyHelldivers=true runGameTestServer --no-daemon` — full optional-provider campaign lane; the receipt separates the archived alpha.9 checkpoint from current continuation results.
- `gradlew.bat -PlibertyTacz=true -Djetsetcraft.libertyVisualAudit=true runClient --no-daemon` — Liberty audit with the workflow's disposable `saves/LibertyGrindAudit` setup.

Release evidence and the exact artifact checksum are recorded in `docs/RELEASE_EVIDENCE_0.3.0.md`.


## Current expansion execution authority

For ongoing post-v0.3 implementation, do not rely on this short release handoff alone. Use:

- `docs/JETSETCRAFT_CLAUDE_FINISH_EXECUTION.md` — the executable finish contract, including HUD, movement, progression, universal challenges, curated modded gangs, and runtime/release gates.
- `docs/CURATED_MODDED_GANG_ATLAS.md` — the version-aware curated gang map for popular mod mobs plus per-gang collectible graffiti/tag/logo/mural progression.

Those documents preserve stable task IDs and must be resumed rather than rewritten from scratch. The universal runtime Mob Atlas remains the fallback for any installed creature not yet premium-curated.
