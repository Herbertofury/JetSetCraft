# Liberty Grind — campaign implementation

## Connected Streets candidate — alpha.10

Six original operation templates extend the verified alpha.9 catalogue from 28 to **34**. See the [new operation table](../wiki/Liberty-Grind.md#connected-streets-candidate--six-original-operations). Neon Block Party and Prism Breakout exercise multiple required goal kinds within a stage; all goals must finish, in either order, before the next stage opens. Other operations combine two independent couriers or escorts with the existing native-world objective primitives. The protocol is **18**.

The production plan generator passed a 5,940-plan local JDK-only matrix across all eligible faction/template pairs, difficulties, time budgets and three seeds. A new native GameTest completes each original operation in both goal orders through `LibertyMissionRuntime` actions, verifies once-only objective rewards and confirms optional sites do not gate the primary completion. That native case is authored but not executed. [Full candidate status](verification/connected-streets/README.md) separates these results from previous Forge/provider/client verification below.

**Drop hard. Paint loud. Skate home.** Liberty Grind is JetSetCraft's selectable expedition mode: choose a war-map operation, review your equipment, land in a pod, fight and paint across a large skate district, then board the extraction ship. The ship hub connects another deployment to research, call-ins and the return to your original Minecraft world.

The retained campaign baseline below describes **0.4.0-alpha.9 / network protocol 17**. The alpha.10 candidate above uses protocol 18; matching clients and servers are required. **[PR #49](https://github.com/Herbertofury/JetSetCraft/pull/49) merged with all 11 PR checks passing**, including 122 required full-provider GameTests, all eight Wave 2 configurations, an independent exact-JAR smoke check and an automated nineteen-capture native-client audit. The [hosted continuation receipt](verification/liberty-campaign/README.md#hosted-continuation) records the evidence and failed attempts. The new captures were not individually reviewed; the earlier 118-case checkpoint and its reviewed images remain archived separately. The [alpha.8 receipt](verification/liberty-grind/README.md) retains the earlier expedition's evidence. Stable v0.3.0 does not include Liberty Grind.

Use the [player guide](../wiki/Liberty-Grind.md) for controls, [pack guide](LIBERTY_GRIND_PACK.md) for exact optional files, [mission research](research/LIBERTY_MISSION_RESEARCH.md) for the current/historical source-game mapping, and [call-in implementation map](research/LIBERTY_CALLIN_IMPLEMENTATION.md) for each call's actual provider or adaptation.

## Operation selection, scale and replay

Choose Liberty Grind from a tuned Boombox or `/jetsetcraft activity`, or open it directly with `/jetsetcraft liberty`. Opening the map does not deploy. Select a region, a mission, difficulty and duration; the final review shows the primary gun, secondary gun, armor and all four call-in slots before **Confirm loadout · Launch pod**.

| Parent region | Faction theme | Frontier at 20% parent liberation |
| --- | --- | --- |
| Neon Docks | Terminid Neon Swarm | Neon Frontier |
| Overpass Uprising | Automaton Machine Legion | Iron Horizon |
| Blackout Borough | Illuminate Prism Choir | Prism Frontier |

Every difficulty from **1, Trivial, through 10, Super Helldive**, is selectable on an unlocked route. Difficulty changes actor caps, reinforcement budgets, wave sizes, elites, objective requirements, optional bases and rewards. It does not change Minecraft's world difficulty. Admission requires Easy, Normal or Hard, enabled graffiti, supported gun content and no conflicting activity or unresolved return checkpoint.

All twenty-eight mission templates support **20, 25 or 30 minute** budgets. Twenty minutes compresses travel and objective duration; twenty-five is the baseline; thirty expands the operation. These are maximum operation clocks, not a promise that every player will take the full interval. Natural session length and difficulty still need human playtesting.

| Plan condition | Arena radius | Inclusive width |
| --- | ---: | ---: |
| 20 minutes and difficulty 1–3 | 512 blocks | 1,025 blocks |
| 25 minutes, or difficulty 4–7 without the largest condition | 640 blocks | 1,281 blocks |
| 30 minutes or difficulty 8–10 | 768 blocks | 1,537 blocks |

Each ordinary deployment occupies its own region of `jetsetcraft:liberty_grind`. Roads, cover, rails, objective structures and faction bases are built as nearby districts are explored. A server-generated seed fixes the plan's template, sites, routes and strongholds. Repeating a template generates another layout; identical plan inputs reproduce the same layout for verification. The [plan model](../src/main/java/com/herberto/jetsetcraft/liberty/LibertyMissionPlan.java) validates IDs, bounds, links and route shapes before a live operation is admitted.

## Twenty-eight mission adaptations

The catalogue combines three authored stages per template. Several sites can be active within a stage; every required site in that stage must finish before the next stage opens. Optional objectives are available independently and never block extraction.

| Theme | Selectable template IDs |
| --- | --- |
| Street liberation, broadcasting and logistics | turf_liberation, signal_blackout, chromatic_convoy, sample_heist, evacuation_rush, district_holdout |
| Automaton industrial operations | machine_shutdown, factory_uprising, machine_eradication, runway_revolt, commando_confidential |
| Terminid nest and swarm operations | nest_diving, hive_breaker, swarm_purge, hatchery_rampage |
| Illuminate disruption and reconnaissance | warp_blackout, prism_recon, fleet_eradication, lair_documentary |
| Shared strategic operations | mural_rocket, pigment_pipeline, blitz_blockbuster, asset_guard, seismic_session, raise_the_colors, headliner_hunt |
| Historical scenario remixes | dark_ink_protocol, termicide_remix |

The [complete research table](research/LIBERTY_MISSION_RESEARCH.md) maps source families including ICBM launch, fuel transfer, eradication, airbases, Blitz, asset defense, surveys, flag raising, commando intelligence, eggs, dark fluid, termicide, lair recording and high-value targets to these actual sequences. Current, conditional and historical source missions are distinguished there. JetSetCraft uses its own physical objectives and original presentation; it does not reproduce every source-game terminal minigame, underground biome, rocket mechanism or civilian AI.

### What actually advances an objective

| Mechanic | Required action and failure conditions |
| --- | --- |
| Territory | Apply real spray or a supported paint effect to distinct cells inside the site. Repainting one cell cannot multiply credit. Nearby rivals can reclaim unfinished progress. Bulk paint credits only cells covered by successfully placed overlays, and one effect cannot skip into a later stage. |
| Relay | Keep skating inside the active broadcast zone while it is clear of nearby rivals. Idle, teleporting or contested movement does not count; lost control decays unfinished progress. |
| Courier | Activate the pickup using the field radio's **Terminal** action near the marker, then skate through ordered checkpoints. Skipping a beacon or teleporting cannot deliver it. |
| Scan | Activate the nearby terminal, remain close and hold the scan. Leaving interrupts it; nearby rivals freeze progress. |
| Demolition | Damage the actual owned core entities until their native health reaches zero. Future-stage cores do not accept progress; decorative surrounding blocks are not the objective. |
| Sweep | Defeat distinct owned rivals near the active site. Foreign, duplicate or distant deaths do not fill the quota. |
| Priority hunt | Locate and defeat the specifically assigned native boss. Ordinary enemies cannot substitute for that actor. The target can be chased within the operation bounds. |
| Escort | Activate a moving objective rig and stay near it as it travels its route. Hostile pressure pauses movement. Escort/evacuation is a moving-rig adaptation, not a rescued-NPC population simulation. |
| Defense | Remain in the active zone and keep nearby hostiles out for the hold period. Standing is allowed here; losing ownership decays unfinished progress. |

These rules live in [LibertyMissionRuntime](../src/main/java/com/herberto/jetsetcraft/liberty/LibertyMissionRuntime.java). The runtime owns progress but cannot mutate a world or spawn an entity. The manager supplies validated player movement, actual applied surfaces, owned kills and physical core deaths.

## Factions, bases and bounded pressure

In isolated districts, Terminid infantry uses cave spiders and silverfish, with spiders as stronger bodies. Automaton infantry uses skeletons and pillagers, with iron golems and ravagers as elites. Illuminate infantry uses zombies and vindicators, with witches and shulkers as elites. Designated priority bodies are a spider, ravager or shulker according to faction. Native classes, health and weapon damage remain real; faction names and base forms give those Minecraft actors a mission identity. Home defense substitutes cave spiders for silverfish and native strays for skeletons: silverfish can rewrite infested blocks even with griefing denied, and skeleton powder-snow conversion has no cancellable conversion hook in the pinned version.

Fabricators, bug holes and warp ships have visible structures and actual damageable weak points. Optional strongholds provide additional objectives and reduce subsequent pressure when destroyed. The maximum-difficulty plan includes a larger stronghold. Cores and finite support hardware are tracked separately from mobile combat residents.

The [difficulty table](../src/main/java/com/herberto/jetsetcraft/liberty/LibertyDifficulty.java) caps each campaign operation at **24 resident combat actors on difficulty 1**, rising to **96 on difficulty 10**. A shared ceiling of **384** covers both campaign and legacy expeditions. Operations with the lowest resident count as a fraction of their own cap get first access to shared slots; ties rotate each tick. Campaign admission is bounded to **eight actors per operation per tick**; bosses consume the same per-tick, resident and cumulative budgets. Reinforcements become available over the mission clock, allowing high total pressure without keeping thousands of mobs resident. The highest thirty-minute lifetime ceiling is 2,700 admissions, an upper bound rather than a guaranteed spawn or kill count.

During campaign field play, ordinary infantry more than **160 blocks from its owner** can retire when every living player in that world is also more than **96 blocks away**. This frees resident slots for later waves near the current action. Assigned priority targets, objective cores and support hardware are preserved. Retirement gives no defeat or reward credit and does not refund an admission from the lifetime budget.

## Armory, four call-in slots and field controls

The pinned merged Helldivers pack supplies **68 live gun definitions** from its actual helldiver2 and zeta registries. Both armory slots can select any supported live definition; the displayed type, feed, capacity and ammo come from the pack. Native heat weapons retain their heat/cooling behavior and show cooling information in the armory. A missing selected definition blocks deployment with a reason. The game does not replace it with a differently named gun.

The fallback armor choices are real head, chest and leg items, preserving the physical ride:

| Kit | Physical items and attributes |
| --- | --- |
| Light | Leather pieces, 6 armor points, +10% walking and skating speed |
| Medium | Iron pieces, 13 armor points, normal movement speed |
| Heavy | Diamond pieces, 17 armor points, 6 toughness, +10% knockback resistance, −10% walking and skating speed |

Compatible complete native armor sets appear when their installed registry provides the actual pieces. These are equipment choices, not a claim that every Helldivers armor passive has been imported.

The call-in catalogue has **100 selectable entries: 94 researched source entries plus six JetSetCraft originals**. Another **25 entries** cover four shared commands, sixteen mission-specific commands and five historical/tutorial/temporary references. Those twenty-five do not enlarge the four-slot loadout. Historical entries cannot be equipped; mission commands are available only when the current operation authorizes them.

Four distinct unlocked calls are selected before launch. The starter loadout supplies Orbital Precision Strike, Eagle Airstrike, MG-43 Machine Gun and Machine Gun Sentry. Spend earned requisitions to unlock other calls, including campaign-gated choices after a frontier opens. **Nine** exact calls use the optional MCDivers 1.0.3 provider in isolated districts; **seventeen** support mappings use exact TaCZ guns when their definitions are live. Other entries execute documented JetSetCraft effects, equipment or hardware. The [behavior map](research/LIBERTY_CALLIN_IMPLEMENTATION.md) identifies every backend and fallback rather than treating catalogue names as native parity.

The original calls are **CHROMAGEDDON** (a large actual paint wave), **Tag Team** (paint rover), **Bassquake** (timed push/damage pulses), **Inkstorm Curtain** (paint smoke), **Fresh Tracks** (boosting route) and **Graffiti Ghost** (a destructible decoy). Their progress, damage, charges and lifetime remain owned by the operation.

Use the **field radio in hotbar slot five** or `/jetsetcraft liberty` to open **Field**. Aim at a valid nearby location before opening the radio to target support. The server checks range, loaded terrain, operation bounds, phase, resources and cooldown. Support equipment arrives physically; right-click the delivery to collect it. Support occupies slot four, vehicle control slot seven and a backpack slot eight. Native guns use TaCZ fire/reload controls; adapted tools/controllers use right-click and sneak-right-click. Vehicles/emplacements carry one rider, with normal movement controls and finite weapon resources. See each call's exact control text.

Eagle calls share turnaround and rearm. Shared Resupply uses mission charges and its displayed cooldown, replenishes the selected guns' correct ammunition, heals eight points and restores boost. Three starting charges become four with Supply Skates; Paint & Powder Workshop adds one more starting charge. Actual objective completion restores charges within the operation's cap. Native reload remains the player's action. A campaign can enter **Downed** with reinforcement drops remaining; use the explicit **Reinforce** action to return in a pod. Reinforcement is finite.

## Extraction, ship and connected rewards

All required stages must be complete to call extraction; optional sites can remain unfinished. Reach within twelve blocks of the pad, call the ship, then hold within forty-eight blocks for thirty seconds. The ship arrives and lands; approach within eight blocks and explicitly board through the radio or by right-clicking it. Leaving the hold zone cancels the call. An empty ship eventually leaves, and can be called again while mission time remains. Boarding accepted before the deadline, including the last valid tick, completes its departure even if the mission clock expires during the flight. Boarding at or after the deadline is rejected.

Successful campaign extraction settles the operation and brings the player to the physical ship hub. Walk around, use the command radio, buy hardware, review the next deployment or choose **Return to Minecraft**. The original source-world checkpoint remains the return destination across hub visits and redeployments. A controlled early return restores that ordinary-world state directly.

Super credits are earned in play. Nine exploration caches contain credits and three tiers of research samples; completed objectives also award field research and supplies. Earned credits and completed-objective progress survive a controlled loss/early return. **Samples require successful extraction.** Requisitions are separate: completed primary objectives and extraction pay the ship wallet, with difficulty scaling. Crew supplies, crew XP, member training and gang reputation use the existing shared reward account and continue to advance the Style Library, including rewards earned before founding a house.

### Retained research

| Project | Implemented effect | Credits | Common | Rare | Super |
| --- | --- | ---: | ---: | ---: | ---: |
| Pressure Saver | Every fourth successful graffiti placement spends no durability, including ordinary-world art; prerequisite for Paint & Powder Workshop | 20 | 8 | 0 | 0 |
| Magnetic Pigment | Unlocks Magnetic Mixtape as the single selected Rival Rush preparation: +1.5-block pickup reach | 20 | 6 | 0 | 0 |
| Blast Stencil | Unlocks Bigger Chorus as that preparation: +12% paint/artifact area | 35 | 12 | 4 | 0 |
| Prism Ink | Unlocks Finishing Touch as that preparation: +25% base damage on critical hits | 50 | 16 | 6 | 1 |
| Signal Lacquer | Prerequisite for Graffiti Targeting Array; retains its old three-to-two marked-panel effect in legacy expeditions, without reducing new territory quotas | 30 | 10 | 2 | 0 |
| Supply Skates | Adds a starting shared resupply charge; prerequisite for Skater Eagle Hangar | 40 | 16 | 4 | 2 |

The three Rush choices replace the existing Style Library/home preparation for the next run. They never grant a second starting rank or a fourth artifact slot. Selecting a crew style clears the research override. Purchases and loadout management happen outside an active deployment.

### Four ship modules

| Module | Requisition cost | Prerequisite and effect |
| --- | ---: | --- |
| Paint & Powder Workshop | 120 | Pressure Saver; one additional starting shared resupply charge |
| Graffiti Targeting Array | 180 | Signal Lacquer; call-in cooldowns reduced by 10% |
| Skater Eagle Hangar | 220 | Supply Skates; one additional charge per equipped Eagle call each rearm |
| Crew Sample Lab | 260 | Founded crew with 100 shared crew XP; successful extraction adds one common sample per completed primary objective |

One connected route is to earn crew XP through street games, recover samples in Liberty, research a Rush preparation, run an arena circuit with that starting choice, then use the combined crew growth to staff the ship laboratory. The currencies remain distinct, and purchases use their displayed costs.

## Shared campaign and safe home defense

Each deployment is solo. Several players can run separate districts concurrently, while their accepted results update one server-wide campaign. Liberation opens frontier routes; losses build threat and can produce regional defense offers. Regional deadlines advance only while a campaign operation is actively in the field. They do not count offline time or ordinary non-Liberty play.

Defense offers choose from faction-appropriate mission templates containing a defense objective. The offer identity and region fix the choice: retrying with the same settings reproduces the plan, and changing difficulty or duration retains its mission family. Different offers can therefore ask for different objective sequences without rerolling a failed defense.

An expired or lost regional defense can offer an owner-specific **home defense**. It has no deadline and does not automatically spawn an invasion. The player explicitly selects the offer, reviews equipment and deploys. Its objectives are compactly mapped into a 257-block-wide area of the existing world. The mode places temporary owned actors, weak points and graffiti overlays on suitable loaded surfaces; it does not generate a district over an existing build.

Home objectives begin only after valid placement. Courier checkpoints are grounded individually, so a beacon on a hill remains reachable at the surface. Escort routes follow clear ground with one-block climbs and bounded detours; the rig pauses if a new obstruction blocks its body. [LibertyHomeTerrain](../src/main/java/com/herberto/jetsetcraft/liberty/LibertyHomeTerrain.java) checks existing loaded terrain without changing blocks or requesting chunks. If a required route cannot be placed safely, deployment is refused and the offer remains available.

Ownership and interaction guards protect ordinary blocks, containers, unrelated mobs and existing graffiti. Native MCDivers attacks run only in isolated Liberty districts; home calls use documented safe JetSetCraft effects. The pinned TaCZ path has a narrow optional bullet guard for native terrain and entity-hit callbacks. Native mob initialization prevents zombie/chicken jockey creation, drops unregistered spider jockey passengers and disables ordinary-raid membership; owned zombie aid, conversions and shulker-bullet duplication are denied.

Home contact protection additionally uses two exact Minecraft 1.20.1 mixins. Actual home participants and operation-owned actors ignore block triggers, skip world-writing step/fall/entity-inside callbacks and suppress source-attributed vibration events. This prevents pressure plates, tripwires, ore, eggs and similar contact from altering the ordinary world or activating connected machinery. Native mob classes and AI remain intact. Normal fall injury and vanilla cushioning are retained; other block-contact effects are deliberately masked only during the accepted home operation. Ordinary entities and all non-home gameplay delegate to their original callbacks. Public Forge trample and block-attack hooks close the corresponding event paths. Admission checks both native marker interfaces and fails closed if the pinned contact guard is absent. [Exact provider boundaries](research/LIBERTY_CALLIN_IMPLEMENTATION.md#world-and-ownership-boundaries) document the separate optional weapon guard.

Closing the operation removes its actors, projectiles, hardware and temporary paint. An unsuccessful home attempt releases the same offer for retry; a successful claimed defense settles once. The ordinary inventory, nested item data, physical ride, Minecraft XP, source dimension/location and captured survival state are restored through the return journal. Disconnect, death or interrupted settlement use recovery; a saved unresolved record blocks another deployment until recovered.

## Architecture and verification boundary

The immutable catalogue/plan/difficulty classes describe a mission. LibertyMissionRuntime computes objective progress; LibertyHomeTerrain supplies grounded home sites and escort paths. LibertyOperation owns the clock, phases and field rewards. LibertyWorld builds isolated terrain; LibertyManager owns admission, real actors, director, transport, return and settlement; LibertyEvents enforces action/ownership boundaries. LibertyCampaignData, LibertyShipProgress, LibertyResearch and the shared crew account are separate persistent ledgers with once-only result identities. LibertyArmory, LibertyStratagemEngine and the optional compatibility adapters own equipment and support behavior.

The expansion adds model and native fixtures for replay, stage/action rules, campaign/ship transactions, real guns and armor, director limits, native core deaths, real spray, home preservation, provider safety and hub return. A positioned fake player, controlled target health, shortened draw delay or explicit failed-campaign result used to create a defense offer must remain disclosed in the receipt. Such fixtures are not evidence of natural player routing or human balance.

**Hosted verification passed for the merged continuation.** The 122-case full-provider suite includes grounded home routes and obstructions, deterministic defense variety, director retirement/replacement pressure and deadline boarding. The exact rebuilt JAR passed independent startup/save/shutdown, and the client passed its automated nineteen-capture walkthrough. See the [alpha.9 receipt](verification/liberty-campaign/README.md#hosted-continuation) for matching source/artifact identities and the other successful PR checks. Individual review of the new images was not performed; the archived nineteen-image review belongs to the earlier checkpoint. These controlled results do not establish a full human campaign extraction or naturally earned unlock pacing. Keep the [alpha.8 archive](verification/liberty-grind/README.md) attached to its original version. Human 20–30 minute pacing, sustained server load, the full equipment economy and broad mod combinations remain playtesting work. The mode does not claim shared squads, a full civilian evacuation simulator, original-game multiplayer vehicles or complete native Helldivers mechanic parity.
