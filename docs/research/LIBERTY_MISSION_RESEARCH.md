# Liberty Grind: mission, faction and pressure research

Research checked **2 November 2026**. Implementation owner: JetSetCraft. This document explains the source material, the actual adaptation, and the limits of each comparison. It accompanies `LibertyDifficulty`, `LibertyMissionCatalog`, `LibertyMissionPlan`, and `LibertyMissionRuntime`; provider equipment research lives separately in the call-in catalogue.

## Design decision

Liberty Grind expands to **28 selectable mission templates**, three enemy factions, ten difficulty settings and **20-, 25- or 30-minute operation budgets**. Each operation has three ordered stages, parallel sites inside a stage, optional jobs and optional enemy strongholds. Layout, site selection and route geometry replay from a versioned seed. The player performs actual server-validated actions: painting distinct turf, holding contested zones, carrying a courier route, escorting a moving rig, activating and protecting scans, destroying physical cores, defeating local rivals, or killing one designated priority actor.

The intended session contains a pod arrival, several trips across a large skateable district, staged objectives, optional research detours, increasing combat pressure and a defended ship extraction. The chosen timer is an upper budget. Skilled players can finish earlier; the implementation does not pad a short mission with an invisible minimum-duration lock. Human pacing at each difficulty needs playtesting in addition to the deterministic and native runtime checks.

Terminology matters: one `LibertyOperation` is currently **one deployment with three objective stages**. Helldivers 2 uses “operation” for a group of up to three separately deployed missions. Returning to the Liberty ship and selecting another mission provides the repeat-deployment loop; the three stages are not evidence of an implemented three-mission operation contract. [P 1]

## What the primary sources establish

Arrowhead's launch explanation places the War Table aboard the ship and describes planetary operations containing up to three missions. Its objective families include infestation clearance, supplies, intelligence, priority targets and strategic launches. Resources and intelligence feed research and the wider war. This supports a connected campaign loop with varied jobs, rather than making every location repeat one checklist. [P 1]

The Escalation of Freedom announcement introduced **Combat Rating 10 / Super Helldive**, larger fortified bot and bug outposts, new objectives and valuable samples. Its significance here is stronger optional bases and another pressure tier. It does not supply Minecraft entity caps, terrain dimensions or our difficulty budgets. [P 2]

The 2025 Hive World expansion introduced underground Hive Lung destruction and a mobile oil-rig escort, with restricted support underground. The urban Illuminate update emphasized gaining and losing districts, activating defense cannons, support squads and protecting civilians. These supply distinct demolition, payload, ownership and defense families. [P 3] [P 4]

The 2026 Cyberstan update added factory-city operations involving production facilities, infrastructure and symbolic structures. The later Void update describes multiple randomized primary objectives, evidence recording, reconnaissance, structure destruction and additional extraction options. The campaign's actual availability evolves, so an indexed mission list is not proof that every mission is offered on every planet at once. [P 5] [P 6]

Arrowhead's 7.1.0 changes measure pressure separately for each player using enemy count and distance. At the global performance cap, spawning favors players with lower nearby pressure, and distant enemies are removed when the enemy budget is exceeded. Its high-value-asset defense update also increases waves and encounter strength, reduces downtime, and exposes generator health. These are separate ideas: a resident limit alone does not establish pressure near a moving player, and faster waves alone do not reproduce destructible defense machinery. **The numeric caps and rates below are original JetSetCraft tuning**, not reverse-engineered Helldivers internals. [P 7]

Nintendo describes distinct Turf War, Splat Zones, Tower Control, Rainmaker and Clam Blitz rules. Liberty uses the useful actions—cover territory, hold a contested zone, move a payload and carry an objective—in a PvE skating campaign. It does not reproduce Nintendo's competitive scoring, movement physics, art or multiplayer rules. [P 8]

## Source clocks and the 20–30-minute adaptation

The source game does not assign one universal length to every mission. The following are published mission time limits, **not measured average completion times**. The community mission pages supply the clock labels; official announcements establish the corresponding operation and objective families. [P 1] [P 7]

| Source mission example | Published source limit | Liberty comparison |
|---|---:|---|
| Launch ICBM | 40 minutes [M 2] | `mural_rocket` uses the selected 20 / 25 / 30-minute budget |
| Blitz: Search and Destroy / Terminid | 12 minutes [M 4] | `blitz_blockbuster` is a longer multi-stage raid; its compressed option is 20 minutes |
| Eradicate Terminid Swarm | 15 minutes [M 9] | `swarm_purge` adds nest destruction and a staged combat route |
| Evacuate High-Value Assets | 20 minutes [M 10] | `asset_guard` adapts defended sites and delivery, with all three Liberty timer options |

Liberty's deadline includes the drop, field objectives, extraction defense and boarding. New extraction calls and boarding are rejected at or after the cutoff. Boarding accepted before the cutoff commits departure: the automatic 100-tick flight can finish even if the clock passes the deadline during it. An unboarded mission instead expires and returns the player; it does not switch to Helldivers' separate loss-of-orbital-support/emergency-extraction sequence. [M 11] Consequently, travel back to the pad and boarding must fit inside the chosen budget. Timer, route-length and human-playtest results must be evaluated together before claiming a reliably paced 20–30-minute session.

## 2026 source refresh and coverage boundaries

The dated catalogue already covers the major 2026 mission families. This follow-up distinguishes newly evidenced source behavior from what the current adaptation actually supplies.

| Source change | Existing Liberty coverage | Boundary retained |
|---|---|---|
| Cyberstan production and factory-city assault [P 5] | `factory_uprising`, `machine_shutdown`, optional fabricator cores | These are registered weak-point objectives, not full city-building demolition or copied Cyborg encounters |
| Galactic Campaigns and Control Centre context/history [P 14] | Persistent sector outcomes, ship progression and repeat deployment | Personal/server progression does not imply an Arrowhead-style global Major Order service or campaign archive |
| Void randomized primary objectives and secondary extraction zones [P 6] | `prism_recon` and `lair_documentary`, with seeded site and route selection | Primary stage composition remains authored; alternate-extraction unlocking is not implemented by those templates |
| Recon Craft retrieval enabled on the other two fronts in 7.1.0 [P 7] | `sample_heist` is already available to all factions | This is coverage of an existing retrieval family, not a reason to add a duplicate template |
| 7.2.0 recording/relay, Gazer Spire Hellbomb and terrain-pathing fixes [P 13] | Evidence-recording and structure-destruction families already have mappings | A patch fixing an objective is not evidence that the mission was introduced in that patch, or that its source minigame is reproduced |

The official patch index lists 7.2.0 at this research cutoff. Its support article was **updated 1 November 2026**; that timestamp is not asserted as the patch's original release date. Later event rotations or additions require another source check rather than extending this snapshot's completeness claim. [P 13] [P 15]

## Actual mission mechanics

| Primitive | What the server really measures | Failure/contest behavior | Physical representation |
|---|---|---|---|
| `TERRITORY` | Unique horizontal ground cells reached by successful real spray; face and height aliases share a cell | An unfinished zone loses one covered cell every four seconds while nearby mission rivals contest it; completed zones remain secured | Real graffiti decals, objective marker and coverage count |
| `RELAY_HOLD` | Real skating movement within the zone while no mission enemy contests it | Idling, walking and teleport-sized movement do not advance it; contested or abandoned progress decays | Signal zone/beacon and held seconds |
| `DEFEND` | Actual owner presence in an uncontested zone for its stated duration | Leaving or contesting the zone decays progress; skating is optional | Defense plaza and held seconds |
| `SCAN` | Nearby objective interaction, followed by remaining near the activated terminal | Rival contest freezes the scan; leaving cancels it and requires another activation | Terminal/recorder marker and scan clock |
| `COURIER` | Nearby canister pickup, followed by ordered checkpoints during plausible skating | Wrong-order, repeated, stationary, walking and teleport arrivals give no credit | Depot and the current route beacon |
| `ESCORT` | A marker physically advances along its route only while the owner stays near and the path is uncontested | An abandoned or contested rig stops moving | Moving paint-rig/evacuation marker; this is not a simulated rescued civilian |
| `DEMOLITION` | Every linked native core must actually die from an authorized mission source | Future-stage cores are unavailable; partial health damage does not count as destruction | Shootable faction weak points with native health and ownership |
| `SWEEP` | Unique mission-owned actor deaths within that site's district | Foreign, repeated and distant quota kills do not count | Actual native mobs, never a client-supplied kill total |
| Priority form of `SWEEP` | Death of the exact assigned native actor UUID | Ordinary enemies and paint cannot substitute; the target may be chased within its operation bounds | Named, marked native priority enemy |

Ordinary territory cells are four blocks wide. Home defense uses smaller cells and compact site radii so its targets remain reachable without modifying the player's terrain. The call-in area-paint path uses the same cell set and accepts an applied-surface predicate, so CHROMAGEDDON credits only cells covered by an effect actually placed on loaded terrain. One area action or movement tick cannot advance two consecutive primary stages. Optional jobs remain available after the main sequence and never gate extraction.

These mechanics give each kind a different player action. A scan is not completed by killing a quota. A core cannot be destroyed by standing next to it. A moving payload is not a stationary timer. A priority target cannot be replaced by any convenient rival. The shared runtime performs no world access, chunk loading, block edits, entity spawning or persistent payouts; the manager performs those effects after validating the operation and owner.

## The 28 selectable templates

Counts below describe the authored base composition. The generator scales targets and held durations with difficulty and the selected operation budget. Each row is implemented as three ordered stages, with multiple sites inside one stage available together.

| Template ID | Name / faction | Stage 1 | Stage 2 | Stage 3 |
|---|---|---|---|---|
| `turf_liberation` | Turf Liberation / any | Paint two districts | Hold two skating signal zones | Defend the street party |
| `signal_blackout` | Signal Blackout / any | Scan two uplinks | Destroy their power cores | Carry the stolen broadcast |
| `chromatic_convoy` | Chromatic Convoy / any | Paint the launch lane | Escort a moving paint rig | Hold its final transmission |
| `sample_heist` | Sample Heist / any | Scan three research vaults | Carry the sample route | Defend the handoff |
| `machine_shutdown` | Machine Shutdown / Automatons | Break two fabrication lines | Copy the production ledger | Carry it along the escape route |
| `factory_uprising` | Factory Uprising / Automatons | Seize two factory broadcasts | Demolish two factories | Defeat the local response force |
| `nest_diving` | Nest Diving / Terminids | Trace two spore sites | Seal three nests | Escape with the survey |
| `hive_breaker` | Hive Breaker / Terminids | Clear two swarm sectors | Destroy a central brood base | Paint two hive approaches |
| `warp_blackout` | Warp Blackout / Illuminate | Paint two shield plazas | Destroy two warp-ship bases | Hold the fleet jammer |
| `prism_recon` | Prism Recon / Illuminate | Record three anomalies | Carry the evidence | Destroy portal-station cores |
| `evacuation_rush` | Evacuation Rush / any | Raise the rescue signal | Escort two evacuation markers | Defend the departure plaza |
| `district_holdout` | District Holdout / any | Paint three defense sites | Defend two plazas | Defeat the final strike force |
| `mural_rocket` | Mural Rocket / any | Recover launch controls | Power the skating launch grid | Defend the launch countdown |
| `pigment_pipeline` | Pigment Pipeline / any | Survey pump controls | Hold two pump zones | Protect the transfer yard |
| `swarm_purge` | Swarm Purge / Terminids | Break two swarm fronts | Destroy breeding-route cores | Defeat the final swarm surge |
| `machine_eradication` | Machine Eradication / Automatons | Defeat a machine formation | Hold the counterstrike beacon | Defeat the machine response |
| `fleet_eradication` | Fleet Eradication / Illuminate | Decode two landing signals | Defeat two invasion groups | Defend the reclaimed plaza |
| `runway_revolt` | Runway Revolt / Automatons | Copy the air-base manifest | Destroy three grounded launch sites | Carry the manifest out |
| `blitz_blockbuster` | Blitz Blockbuster / any | Crack two forward bases | Break the rapid response | Destroy the command base |
| `asset_guard` | Asset Guard / any | Power two security relays | Defend two artifact bays | Dispatch the secure courier |
| `seismic_session` | Seismic Session / any | Survey three mineral sites | Defend the deep scan | Recover its research readout |
| `raise_the_colors` | Raise the Colors / any | Paint two flag plazas | Broadcast from three crew relays | Defend the celebration |
| `commando_confidential` | Commando Confidential / Automatons | Recover three black-box records | Carry the intelligence route | Protect the final upload |
| `hatchery_rampage` | Hatchery Rampage / Terminids | Destroy three egg-cluster bases | Clear two brood-response sectors | Paint the emptied hatchery |
| `dark_ink_protocol` | Dark Ink Protocol / Terminids | Survey two dark-ink sites | Protect two drill cycles | Collapse a corrupted nest |
| `termicide_remix` | Termicide Remix / Terminids | Activate three treatment relays | Defend the treatment cycle | Carry its research out |
| `lair_documentary` | Lair Documentary / Illuminate | Break lair-entry cores | Record three experiments | Carry the documentary evidence |
| `headliner_hunt` | Headliner Hunt / any | Scan two tracking sites | Defeat one actual assigned priority actor | Deliver proof of the takedown |

Every template supports **20 / 25 / 30 minutes**. Twenty minutes is the compressed variant; the raid-like `blitz_blockbuster` and research-site adaptations are especially suited to it. Twenty-five minutes is the standard expedition budget; thirty minutes provides the longest exploration window. Original source-game mission clocks are not copied into this system. Two named historical remixes are retained intentionally as repeatable scenarios.

## Source mission coverage and adaptation status

The following is a coverage inventory, including primary families, renamed variants and event/legacy scenarios found during research. Official sources establish the families and mechanics; the community index is used only to discover and distinguish additional game labels, then mapped to our original implementation. The index mixes currently rotating and conditional missions with historical entries. **“Adapted” means a playable JetSetCraft sequence with the stated related actions. It does not mean an identical mission, model, minigame, planet event or source-game encounter.** [M 1]

All rows use the selectable **20 / 25 / 30-minute** budget. Rows marked historical are explicitly replayable remixes, without implying the source scenario is currently available. A many-to-one mapping is intentional when several source mission labels share the same meaningful action family.

### Shared mission families

| Source label or family | JetSetCraft template | Status and exact boundary |
|---|---|---|
| Start Fuel Pumps | `pigment_pipeline` | Adapted pump controls, powered zones and defended transfer |
| Upload Escape Pod Data | `sample_heist` | Adapted local scan and protected data delivery |
| Retrieve Recon Craft Intel | `sample_heist` | Adapted intelligence retrieval, courier and handoff |
| Terminate Illegal Broadcast | `signal_blackout` | Adapted uplink survey, real core demolition and broadcast delivery |
| Conduct Geological Survey | `seismic_session` | Adapted probe scans and defended deep-research site; no copied terminal minigame |
| Spread Democracy | `raise_the_colors` | Adapted crew-flag plazas and contested relay holds |
| Launch ICBM | `mural_rocket` | Adapted launch preparation and defended countdown; not a nuclear terrain-destruction system |
| Retrieve Valuable Data | `sample_heist` | Adapted scan, carried route and defended handoff |
| Emergency Evacuation | `evacuation_rush` | Adapted moving evacuation markers and departure defense; no civilian rescue simulation claim |
| Evacuate High-Value Assets | `asset_guard` | Adapted security zones, defended artifact bays and final delivery; no cloned gate/rocket-launch machinery |
| Retrieve Essential Personnel | `evacuation_rush` | Historical family. Arrowhead explicitly disabled the old defense-event variant in patch 01.000.400. [P 9] |

The strategic-launch family is directly described at launch; geological objectives and high-value defense are also identified in Arrowhead's patches. [P 1] [P 7] [P 10] The detailed launch-code/power-chain distinction is useful comparison material, not a claim that Liberty duplicates its original terminal sequence. [M 2]

### Terminid mission families

| Source label or family | JetSetCraft template | Status and exact boundary |
|---|---|---|
| Restart Pumps; Activate Oil Pumps; Enable Oil Extraction | `pigment_pipeline` | Adapted industrial-control and protected-transfer sequence |
| Extract E-711; Collect Gloom-Infused Oil | `pigment_pipeline` | Adapted material recovery; no fuel-hose vehicle interface. The former has also appeared under an older mysterious-substance label. [M 3] |
| Conduct Mobile E-711 Extraction | `chromatic_convoy` | Adapted moving paint rig; the rig truly travels its route. Official mobile-oil-rig inspiration: [P 3] |
| Collect Meteorological Data; Collect Gloom Spore Readings; Extract Research Probe Data | `seismic_session` | Adapted environmental scans and defended research |
| Blitz: Secure Research Site | `sample_heist` | Adapted research raid; choose 20 minutes for its compressed version |
| Activate TCS+ Station | `termicide_remix` | Adapted treatment relays and defended cycle; distinct from claiming a live original TCS event |
| Restore Air Quality | `signal_blackout` | Adapted survey, contamination-source demolition and data delivery |
| Eliminate Brood Commanders; Eliminate Chargers; Eliminate Bile Titans; Eliminate Impaler | `headliner_hunt` | Adapted actual designated Minecraft priority enemy; no copied Terminid body or armor anatomy |
| Eradicate Terminid Swarm | `swarm_purge` | Adapted sustained local kill pressure and nest destruction |
| Blitz: Search and Destroy | `blitz_blockbuster` | Adapted multi-base raid with actual cores; 20-minute compressed option. [M 4] |
| Purge Hatcheries | `hatchery_rampage` | Adapted egg-cluster cores, brood response and reclamation |
| Destroy Spore Lung / Hive Lung | `hive_breaker` | Adapted central brood-core demolition; no subterranean biome or burrowing simulation claim. [P 3] |
| Chart Terminid Tunnels | `nest_diving` | Adapted scans, nesting sites and survey escape |
| Nuke Nursery | `hatchery_rampage` | Adapted marked nursery-core destruction; no player-world crater |
| Cleanse Infested District | `turf_liberation` | Adapted reclaimed turf, contested signals and defense |
| Activate Terminid Control System | `termicide_remix` | Historical scenario remix of activation and defense |
| Deactivate Terminid Control System | `signal_blackout` | Historical shutdown remix through scan/core destruction. [M 5] |
| Deploy Dark Fluid | `dark_ink_protocol` | Historical survey/drill-defense/nest-collapse remix; no planet-destruction event. [M 6] |

### Automaton mission families

| Source label or family | JetSetCraft template | Status and exact boundary |
|---|---|---|
| Eliminate Devastators; Eliminate Automaton Hulks; Eliminate Automaton Factory Strider | `headliner_hunt` | Adapted real marked native enforcer; designated UUID must die. The source Hulk target is separately documented in the comparison inventory. [M 7] |
| Commando: Secure Black Box; Commando: Acquire Evidence; Commando: Extract Intel | `commando_confidential` | Adapted intelligence infiltration, route and upload. Source Commando operations have support restrictions; this template does not claim to duplicate that restriction schedule. [P 11] [M 8] |
| Sabotage Orgo-Plasma Synthesis | `machine_shutdown` | Adapted production-core shutdown and ledger theft |
| Sabotage Supply Bases | `blitz_blockbuster` | Adapted multiple real strongholds and response force |
| Annex Untapped Mineral Sites | `seismic_session` | Adapted mineral-site survey and defended deep scan |
| Confiscate Assets; Rapid Acquisition | `sample_heist` | Adapted research raid and secure carried delivery |
| Seize Industrial Complex | `factory_uprising` | Adapted signal takeover, factories and response force |
| Destroy Transmission Network | `signal_blackout` | Adapted uplinks and real power-core destruction |
| Eradicate Automaton Forces | `machine_eradication` | Adapted formation, counterstrike defense and final quota |
| Blitz: Search and Destroy | `blitz_blockbuster` | Adapted multi-base raid with optional 20-minute pacing |
| Sabotage Air Base | `runway_revolt` | Adapted manifest, grounded launch cores and escape; not flyable enemy aircraft |
| Halt Cyborg Production | `factory_uprising` | Adapted fortified production complex. Official Cyberstan family: [P 5] |
| Neutralize Ground-to-Orbit Defenses | `machine_shutdown` | Adapted defended installation cores and intelligence recovery |
| Blitz: Destroy Bio-Processors | `blitz_blockbuster` | Adapted physical processing-base cores; no copied environmental scene |
| Destroy Command Bunkers | `machine_shutdown` | Adapted multiple fortified core sites and ledger extraction. Arrowhead's bunker rebalance confirms the multi-site family. [P 12] |

### Illuminate mission families

| Source label or family | JetSetCraft template | Status and exact boundary |
|---|---|---|
| Blitz: Destroy Illuminate Warp Ships | `warp_blackout` | Adapted grounded warp-ship cores and a fleet jammer |
| Blitz: Destroy Illuminate Warp Gateways | `warp_blackout` | Adapted portal/ship power cores; no traversable alien dimension |
| Evacuate Colonists | `evacuation_rush` | Adapted moving rescue markers; no claim of native colonist pathfinding |
| Take Down Overship; Destroy Harvesters | `headliner_hunt` | Adapted actual marked priority native enemy, not the original flying ship or walker |
| Free Colony | `turf_liberation` | Adapted ownership and protection of a reclaimed neighborhood |
| Democratize the Void | `prism_recon` | Adapted evidence, route and destructive objective family; layouts replay from a seed, while this template's stage order stays explicit. [P 6] |
| Infiltrate Illuminate Lair | `lair_documentary` | Adapted entry destruction, actual recording scans and carried evidence. [P 13] |
| Blitz: Suppress Toxic Pollination | `signal_blackout` | Adapted survey, hostile infrastructure destruction and proof delivery |
| Repel Invasion Fleet | `fleet_eradication` | Adapted landing intelligence, invasion groups and district defense. [P 4] |
| Destroy Exospire; Destroy Gazer Spire | `warp_blackout` | Adapted marked structure cores and signal suppression |
| Extract Anomalous Material | `prism_recon` | Adapted anomalous-site scans and research delivery. [P 13] |
| Eradicate Illuminate Forces | `fleet_eradication` | Adapted sustained local combat and final defense; the source mission is mentioned in current fixes. [P 7] |

The catalogue therefore preserves separate playable launch, pump, eradication, air-base, blitz, high-value defense, survey, flag, commando, hatchery, historical treatment/drill, evidence-recording and priority-hunt families. It also retains the original graffiti-first combinations. Exact source-game models, architecture, civilian AI, procedural caves, input minigames, alternate-extraction unlocking and global narrative events are outside the parity claim.

## Secondary objectives and strongholds

Each plan shuffles an independent pool of community mural, pirate uplink, survey cache, pigment delivery, stranded rig and rival ambush jobs. Higher difficulty adds jobs and optional outposts. A failed optional job never prevents an otherwise complete mission from extracting.

| Source secondary family | Liberty behavior | Boundary |
|---|---|---|
| Radar, survey and research sites | Optional scanner; research and route choice | The current objective does not claim to reproduce a full map-reveal radar minigame |
| Illegal broadcasts and propaganda | Optional pirate relay / Signal Blackout | Actual local skating hold or core shutdown |
| Artillery, SAM and support installations | Related ship/call-in progression, plus defended objectives | Capturing a scanner is not advertised as instantly granting every source support stratagem |
| Spore sources, stalking/flying nests and infestation structures | Optional bug-hole cores / Hive Breaker | Native bodies and cores; no copied flying nest AI |
| Jammers, detector towers, mortars, anti-air and gunship bases | Optional fabricator cores / Machine Shutdown | Removing a base reduces pressure; unimplemented source-specific jamming effects are not implied |
| Bio-processors, industrial infrastructure and memorial destruction | Factory Uprising / optional machine bases | Physical core destruction rather than arbitrary block griefing |
| Cognitive interference, anomalous research and evidence recording | Warp Blackout / Prism Recon / Lair Documentary | Actual scans, turf and cores; source camera minigames are adapted |
| Mutant larva and carried scientific specimens | Courier and research-delivery jobs | The inventory/route fantasy is adapted; a living larva pet is not claimed |
| Giant fortresses and heavily fortified nests | Difficulty 10's first optional stronghold uses tier 4 cores | A dangerous optional detour with a bounded resident budget |

Larger outposts and sample incentives are directly supported by the Escalation announcement. Current fixes confirm scientific specimen, recording and shield-generator objective families. [P 2] [P 13]

## Minecraft faction casting

The server deliberately uses actual Minecraft entity behavior with operation ownership and native damage. It does not replace the TaCZ pack's weapons with renamed vanilla items. The district roster avoids evoker summons and splitting mobs. Native initialization also disables zombie/chicken jockey creation, removes newly constructed unregistered spider passengers and prevents ordinary-raid membership. Owned zombie reinforcement, supported conversion and shulker-bullet duplication paths are denied so descendants do not bypass the director or cleanup.

| Faction | Gang association | Actual baseline runtime bodies | Actual elite/priority bodies | Stronghold style |
|---|---|---|---|---|
| Automaton Machine Legion | Bone Drones | Skeleton, pillager | Iron golem, ravager; priority ravager | Fabricator weak points |
| Terminid Neon Swarm | Dead Beat | Cave spider, silverfish | Spider; priority spider | Bug-hole weak points |
| Illuminate Prism Choir | Hex Appeal | Zombie, vindicator | Witch, shulker; priority shulker | Warp-ship weak points and an absorption shield |

These are original casting decisions. A spider playing a priority Terminid role does not claim the original Bile Titan skeleton, hitboxes or attacks. Strongholds are shootable mission entities with native health. The mission runtime receives their authorized death; it never treats a painted wall, nearby player or arbitrary explosion count as a destroyed base. The manager protects owner/world boundaries and removes its entities on return.

## Difficulty and long-operation pressure

All numbers in this table are **original tuning**. The source establishes the tenth tier and a capped, pressure-aware spawning approach; it does not publish these Minecraft parameters. [P 2] [P 7]

| Difficulty | Name | Resident actors | Wave size | Baseline interval | Total 25-minute admission budget | Optional bases | Other optional jobs | Elite share target |
|---:|---|---:|---:|---:|---:|---:|---:|---:|
| 1 | Trivial | 24 | 3–5 | 16s | 220 | 2 | 2 | 0% |
| 2 | Easy | 30 | 4–6 | 15s | 320 | 2 | 2 | 3% |
| 3 | Medium | 36 | 4–8 | 14s | 450 | 3 | 2 | 5% |
| 4 | Challenging | 42 | 5–10 | 12.5s | 600 | 3 | 3 | 8% |
| 5 | Hard | 48 | 6–12 | 11s | 800 | 4 | 3 | 10% |
| 6 | Extreme | 56 | 7–14 | 10s | 1, 020 | 4 | 3 | 13% |
| 7 | Suicide Mission | 64 | 8–16 | 9s | 1, 280 | 5 | 4 | 16% |
| 8 | Impossible | 72 | 10–18 | 8s | 1, 580 | 5 | 4 | 20% |
| 9 | Helldive | 84 | 12–22 | 7s | 1, 900 | 6 | 5 | 24% |
| 10 | Super Helldive | 96 | 14–24 | 6s | 2, 250 | 6 | 5 | 28% |

The twenty- and thirty-minute admission budgets scale by 20/25 and 30/25. A thirty-minute difficulty 10 operation can therefore admit up to 2, 700 enemies over time while retaining at most 96 mission combat actors at once. That is a ceiling, not a promise to spawn or require killing every actor. Loaded safe spawn positions, remaining actors, optional base destruction and time-based allowance all constrain admission. The global combat-actor cap is 384 across simultaneous sessions, and admission is bounded to eight actors per operation per tick, including priority enemies. Stationary cores and harmless objective markers are tracked separately and remain bounded by the finite site plan.

The allowance begins with one resident-cap budget and releases the remaining admission budget over the chosen operation clock. This prevents a fast-clearing player from consuming all reinforcements at the beginning. Clearing bases slows subsequent waves, with a bounded slowdown. Difficulty therefore changes density, renewal speed, elite mix, target requirements, detours and rewards; it is not only a health multiplier.

**Pressure adaptation:** the continuation retires ordinary campaign combatants more than 160 blocks from their owner, checked once per second, while preserving designated priority targets and actors within 96 blocks of another living player. Retirement frees resident slots without awarding defeats, progressing objectives or refunding lifetime admissions. Sessions with a lower fraction of their resident allowance tick first, with rotating ties; legacy expeditions also observe the shared 384-actor ceiling. This is a bounded JetSetCraft policy informed by the 7.1.0 problem, not a reproduction of Arrowhead's scheduler or evidence of sustained multiplayer scale. The native director regression moves into another district with the former front still loaded and checks actual retirement, renewed nearby admissions, unchanged rewards and preserved budgets. See the current verification receipt for its observed result. [P 7]

The largest arena has a **768-block radius**, making it 1, 536 blocks across. Other plans use 512 or 640. Thirty-minute or difficulty 8–10 plans use 768; otherwise 25-minute or difficulty 4–7 plans use 640; twenty-minute difficulty 1–3 uses 512. Mission regions are spaced by 2, 048 blocks. Sites are separated by at least 96 blocks, and every route stays within its arena. The implementation decorates only loaded chunks instead of materializing an entire enormous arena in one server tick.

## Replay generation and progression connections

The seed is salted with the bounded sector ID, faction, difficulty, timer and selected template. Fixed seeded shuffling chooses separated site centers, optional jobs and checkpoint orientation. Replaying the same schema and inputs produces the same immutable plan; unrelated world randomness cannot change its layout. Primary stages are consecutive, site IDs are unique, demolition bases match their core goals, and the public constructor rejects invalid plans before deployment.

The model accepts stable sector identifiers, including `neon_docks`, `overpass_uprising`, `blackout_borough`, `neon_frontier`, `iron_horizon` and `prism_frontier`. Campaign admission owns whether a route is unlocked. The generator itself does not unlock a frontier or accept a client-provided outcome.

Objective completion queues one reward event per objective. The manager converts those events into its actual field rewards; extraction and persistent settlement remain separate transactions. Research uses the existing Pressure Saver, Magnetic Pigment, Blast Stencil, Prism Ink, Signal Lacquer and Supply Skates projects. Selected preparations contribute a compatible starting rank to Rival Rush; they do not stack a second free rank. Ship hardware connects research to resupply capacity, targeting cooldowns, Eagle charges and additional extracted common samples. Thus painting and returning with research can improve both future expeditions and the shared roguelite loop.

Campaign failure may produce a defense opportunity. A later owner-specific home defense requires an explicit deployment choice; elapsed offline wall time does not attack or rewrite a player's world. Home geometry uses a compact coordinate remap and can align once to already loaded terrain before objective progress. Existing blocks and containers are not used as destructible mission cores. Failed home defense unclaims its existing offer for retry; successful accepted outcomes use their mission nonce to settle once.

Home rosters have two explicit native substitutions: cave spiders replace silverfish, whose wake-up goal changes infested blocks even when mob griefing is denied; strays replace skeletons, whose powder-snow conversion bypasses Forge's cancellable conversion event in Minecraft 1.20.1. Two narrowly scoped contact mixins preserve the existing home world: actual participants/owned actors ignore block triggers and skip step/fall/entity-inside block callbacks, with ordinary fall injury/cushioning retained. Public Forge hooks suppress their vibration, trample and block-attack events. Ordinary entities retain native callbacks, and no custom mob subclass replaces the original combat AI. Home admission requires the exact native guard markers. These safety boundaries are implementation choices, not a claim about Helldivers mechanics.

## Verification and honest claim boundaries

`LibertyMissionGameTests` contains nine focused production-action fixtures. The expanded deterministic corpus covers **4, 860 plans**: every eligible faction/template pair, all ten difficulties, all three timers and three seeds. It produced 4, 749 distinct first-site positions while preserving exact replay for repeated inputs. Checks cover resident/tick/allowance limits, arena and route bounds, site separation, real core links, immutable structure and invalid input rejection.

The action fixtures cover turf aliases and reclamation, held relay/scan/defense behavior, courier pickup/order/anti-teleport conditions, core stage gates, optional extraction independence, unique local kills, moving escorts, one-time home remapping, atomic area-paint staging and exact priority actor identity. They pass against the actual mapped Minecraft value types in the focused harness. This establishes rule behavior, not a human play session.

`LibertyCampaignRuntimeGameTests` supplies the separate native tests for real campaign deployment, ordinary Forge director ticks, high-difficulty budgets, actual TaCZ ammunition/projectile/core death, actual free-spray decals, accepted home-defense preservation, and ship-to-campaign return. The home fixture compares an exact 257×16×257 block-state volume, chest NBT, old graffiti and an unrelated native mob, as well as original nested inventory, armor, XP and physical ride state. Its defense offer is established through explicit public failed-result fixture transactions; it does not claim those setup transactions were played missions. Native pass/fail and final artifact provenance belong to the current verification receipt, not to the earlier Liberty Grind screenshots.

The native fixtures deliberately disclose loaded-region tickets, controlled player placement, a one-health target for the provider attribution check and a skipped draw-animation delay. They do not set objective completion fields, manufacture a successful core death event, grant arbitrary progress or call a fake rendering path. A real screenshot audit is still needed for readability and visual behavior, and human playtesting is still needed to tune a 20–30-minute experience.

## Sources

Primary sources carry the gameplay and architecture rationale. Community entries below are comparison/discovery aids for game labels, renamed and historical scenarios; their internal AI, rewards, clocks and exact balance tables are not imported into JetSetCraft.

- **P 1:** Arrowhead / PlayStation, [Galactic War gameplay detailed](https://blog.playstation.com/2024/01/23/helldivers-2-galactic-war-gameplay-detailed-complete-missions-reclaim-planets-rescue-the-galaxy/), 23 January 2024.
- **P 2:** Arrowhead / PlayStation, [Escalation of Freedom](https://blog.playstation.com/2024/07/23/helldivers-2s-biggest-update-yet-escalation-of-freedom-drops-august-6/), 23 July 2024.
- **P 3:** Arrowhead / PlayStation, [Into the Unjust](https://blog.playstation.com/?p=408849), 26 August 2025.
- **P 4:** Arrowhead / PlayStation, [Heart of Democracy](https://blog.playstation.com/?p=404945), 20 May 2025.
- **P 5:** Arrowhead / PlayStation, [Machinery of Oppression / Cyberstan](https://blog.playstation.com/2026/02/10/new-helldivers-2-update-out-today/), 10 February 2026.
- **P 6:** Arrowhead / PlayStation, [A deep dive into the Illuminate Void](https://blog.playstation.com/2026/08/18/helldivers-2-a-deep-dive-into-the-illuminate-void/), 18 August 2026.
- **P 7:** Arrowhead, [Devoid of Liberty 7.1.0](https://arrowhead.zendesk.com/hc/en-us/articles/30492249897756-Devoid-of-Liberty-7-1-0), particularly Enemy Spawning and Evacuate High-Value Assets. The support-page update timestamp is not treated as the original patch release date.
- **P 8:** Nintendo, [Splatoon 3 online modes: beginner basics](https://splatoon.nintendo.com/ca/news/beginner-basics-for-splatoon-3-the-ins-and-outs-of-playing-online/) and [official gameplay overview](https://splatoon.nintendo.com/en/gameplay/).
- **P 9:** Arrowhead, [Patch 01.000.400](https://arrowhead.zendesk.com/hc/en-us/articles/14444588747676-PATCH-01-000-400), including the disabled personnel-defense variant.
- **P 10:** Arrowhead, [Patch 01.001.00](https://arrowhead.zendesk.com/hc/en-us/articles/15938362444444--PATCH-01-001-00), including geological-survey fixes.
- **P 11:** Arrowhead, [Into the Unjust 6.0.1](https://arrowhead.zendesk.com/hc/en-us/articles/25259642976156--Into-the-Unjust-6-0-1), including the extension of Commando operations to Automaton planets.
- **P 12:** Arrowhead, [Patch 01.000.200](https://arrowhead.zendesk.com/hc/en-us/articles/13343297312412--PATCH-01-000-200), including bunker and personnel mission changes.
- **P 13:** Arrowhead, [Devoid of Liberty 7.2.0](https://arrowhead.zendesk.com/hc/en-us/articles/31176395885596-Devoid-of-Liberty-7-2-0), including recording, anomaly, shield-generator and specimen objective fixes; support article updated 1 November 2026.
- **P 14:** Arrowhead, [Machinery of Oppression 6.3.0](https://arrowhead.zendesk.com/hc/en-us/articles/28203563503132-Machinery-of-Oppression-6-3-0), including Galactic Campaigns and the Control Centre. Its support-page modification date is not used as a release-date claim.
- **P 15:** Arrowhead, [latest patch-note index](https://arrowhead.zendesk.com/hc/en-us/sections/12541983411100-Latest-patch-notes), checked 2 November 2026; a rolling index, not an immutable version manifest.
- **M 1:** Community-maintained [mission inventory](https://helldivers.wiki.gg/wiki/Missions), used for additional labels and historical/conditional coverage. No completeness guarantee about future rotations is inferred.
- **M 2:** Community comparison, [Launch ICBM](https://helldivers.wiki.gg/wiki/Launch_ICBM).
- **M 3:** Community comparison, [Extract E-711](https://helldivers.wiki.gg/wiki/Extract_E-711).
- **M 4:** Community comparison, [Terminid search-and-destroy](https://helldivers.wiki.gg/wiki/Blitz_Search_And_Destroy/Terminid).
- **M 5:** Community history, [Deactivate Terminid Control System](https://helldivers.wiki.gg/wiki/Deactivate_Terminid_Control_System).
- **M 6:** Community history, [Deploy Dark Fluid](https://helldivers.wiki.gg/wiki/Deploy_Dark_Fluid).
- **M 7:** Community comparison, [Eliminate Automaton Hulks](https://helldivers.wiki.gg/wiki/Eliminate_Automaton_Hulks).
- **M 8:** Community comparison, [Commando missions](https://helldivers.wiki.gg/wiki/Commando_Missions).
- **M 9:** Community clock comparison, [Eradicate Terminid Swarm](https://helldivers.wiki.gg/wiki/Eradicate_Terminid_Swarm).
- **M 10:** Community clock comparison, [Evacuate High-Value Assets](https://helldivers.wiki.gg/wiki/Defend_Evacuation_Site).
- **M 11:** Community extraction comparison, [Extraction](https://helldivers.wiki.gg/wiki/Extraction).

Community clock/roster details in this refresh were available through indexed search excerpts; direct wiki page retrieval returned HTTP 403. They remain comparison aids with that retrieval limitation, rather than first-party timing measurements or a claim that every rotating mission was inspected in-game.
