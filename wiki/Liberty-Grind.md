# Liberty Grind — drop, tag, extract

**Helldivers-inspired expeditions on skates, connected to the rest of JetSetCraft.** Choose a front, review your guns and four call-ins, land in a pod, paint territory, break rival bases and extract to your ship. Research and ship upgrades turn those expeditions into new options for street art, crew progression and Rival Rush.

This guide describes **0.4.0-alpha.9 / protocol 17**. Both client and server need the matching version. Stable v0.3.0 does not contain this mode. **Hosted verification of this continuation is pending.** The archived alpha.9 checkpoint recorded 118 required GameTests in each of three configurations, nineteen reviewed native-client captures and an installed-JAR startup/save/shutdown check. Those results belong to the earlier source/JAR in the [campaign receipt and gallery](https://github.com/Herbertofury/JetSetCraft/blob/main/docs/verification/liberty-campaign/README.md); they do not verify the current changes. Earlier [alpha.8 evidence](https://github.com/Herbertofury/JetSetCraft/blob/main/docs/verification/liberty-grind/README.md) remains archived separately.

## Install and prepare

Install supported **TaCZ 1.1.8-hotfix2** and the merged **Helldivers:Liberty Day** gun pack on the server and participating clients. Put the intact pack ZIP in the instance's `tacz/` folder and remove the superseded standalone Helldivers ZIP. Optional **MCDivers 1.0.3** supplies native versions of nine supported calls. The [exact pack/provider guide](https://github.com/Herbertofury/JetSetCraft/blob/main/docs/LIBERTY_GRIND_PACK.md) lists official files, hashes and setup.

JetSetCraft remains usable without these optional mods. The war map explains missing gun content before deployment. Keep graffiti enabled and use Minecraft Easy, Normal or Hard; the operation's separate 1–10 difficulty setting does not change world difficulty. Finish another active game, leave vehicles and clear any carried cursor item before deploying.

## Choose a mission and review the loadout

Use a tuned Boombox or `/jetsetcraft activity`, choose **Liberty Grind**, or open its controls directly with `/jetsetcraft liberty`.

The six-region campaign map starts with three fronts. Completing expeditions contributes to the server's shared liberation and unlocks each frontier at 20% of its parent.

| Starting region | Faction | Unlockable frontier |
| --- | --- | --- |
| Neon Docks | Terminid Neon Swarm | Neon Frontier |
| Overpass Uprising | Automaton Machine Legion | Iron Horizon |
| Blackout Borough | Illuminate Prism Choir | Prism Frontier |

Choose among the region's available mission templates, **difficulty 1–10** and **20, 25 or 30 minutes**. All difficulties are selectable on open routes. The twenty-eight mission adaptations have different three-stage sequences: free turf and raise crew colors, shut down factories, purge hatcheries, scan mineral sites, defend assets, disrupt airbases, run intelligence, record a lair or hunt an actual priority target. Historical termicide/dark-fluid scenarios are explicit remixes. The [complete mission map](https://github.com/Herbertofury/JetSetCraft/blob/main/docs/research/LIBERTY_MISSION_RESEARCH.md) lists all twenty-eight and their source families.

The twenty-minute option compresses the plan; twenty-five is standard and thirty gives a longer operation budget. Arenas span **1,025, 1,281 or 1,537 blocks**, depending on duration and difficulty. The map is regenerated from a new seed for replay variety. These are operation time limits; a quick clear can finish sooner.

Press **Review weapons, armor & call-ins**. The review shows your actual two guns, armor and four slots. **Weapons & armor** searches the pinned pack's **68 live guns**; either weapon slot can choose a supported definition. TaCZ keeps the real gun's aiming, fire mode, ammo, reload and heat/cooling behavior. Heat weapons show their cooling information in the armory.

| Armor kit | Tradeoff |
| --- | --- |
| Light | Leather protection, +10% walking/skating speed |
| Medium | Iron protection, normal speed |
| Heavy | Diamond protection and toughness, +10% knockback resistance, −10% walking/skating speed |

Your physical ride remains equipped. Compatible native armor sets appear only when their real items are installed. Use **Four call-in slots** to choose four distinct unlocked calls, then **Back** to the same review and **Confirm loadout · Launch pod**. The starting calls are Orbital Precision Strike, Eagle Airstrike, MG-43 Machine Gun and Machine Gun Sentry.

## Fight, skate and paint

The pod carries you into your own district with temporary equipment and a copy of your ride, or inline skates if you had none. Native mobs are faction-themed Minecraft bodies: spiders/silverfish, skeletons/pillagers, and zombies/vindicators with stronger native elites. Patrol pressure replenishes over time; difficulty caps the active combat population between 24 and 96 per operation. Destroying optional fabricators, bug holes or warp ships helps reduce later pressure.

Campaign and legacy expeditions share a 384-enemy ceiling. New waves favor operations with the largest share of their enemy slots free, with equal priorities taking turns. Ordinary campaign infantry can retire once more than 160 blocks from its owner and more than 96 from every living player in that world; assigned priority targets remain. Retirement makes room for later waves but awards no kills or rewards and does not replenish the mission's finite reinforcement budget.

Follow the HUD's **active** objective and world markers. Required stages unlock in order; optional sites can be completed for extra rewards but never hold extraction closed.

| Goal | How to complete it |
| --- | --- |
| Paint territory | Spray distinct floor/wall cells in the marked zone, or use a paint call that actually reaches those surfaces. Repainting one spot does not count twice. Nearby rivals can reclaim unfinished turf. |
| Broadcast a relay | Skate inside the zone while rivals are cleared away. Standing still cannot build a skating relay. |
| Courier | Approach the pickup, use **Field → Terminal**, then skate through its checkpoints in order. |
| Scan or survey | Use **Terminal** near the marker and stay close while the scan runs. Rivals contest it; leaving interrupts it. |
| Break a base | Shoot or damage the actual marked weak-point entities until they are destroyed. Painting the decoration cannot substitute. |
| Sweep or priority hunt | Defeat your operation's local enemies, or the specifically marked boss when required. Another player's kills and ordinary enemies do not substitute for a designated target. |
| Escort | Activate the moving rig with **Terminal**, stay nearby and clear its path. Enemies pause its movement. |
| Defend | Hold the zone and keep nearby rivals out until its timer fills. Standing is allowed for this goal. |

Escort and evacuation use **moving objective rigs**, not a full crowd of rescued civilian NPCs. Mission names describe adaptations built for JetSetCraft's skating and paint systems; the [implementation guide](https://github.com/Herbertofury/JetSetCraft/blob/main/docs/LIBERTY_GRIND.md) explains exact boundaries.

Explore for glowing super-credit capsules and common, rare and super research samples. Credits are earned through play, with no real-money purchase. Keep enough operation time to reach the extraction pad after finishing required stages.

## Use the field radio and call-ins

Select the **Liberty field radio in hotbar slot five** and right-click, or use `/jetsetcraft liberty`. **Field** shows equipped calls, shared commands, eligible mission commands, cooldowns and remaining uses. Aim at the desired location before opening it; the server validates the actual target and range. **Terminal** interacts with a nearby active objective.

The catalogue has **100 selectable calls: 94 sourced Helldivers entries and six originals**. Another **25 shared, mission-specific and historical references** sit outside the four equipped slots; historical references are not usable loadout equipment. Nine calls can use native MCDivers actors and seventeen support calls can supply exact TaCZ weapons. Other calls use the documented JetSetCraft effect or physical hardware. Check each entry's description and the [complete behavior/provider map](https://github.com/Herbertofury/JetSetCraft/blob/main/docs/research/LIBERTY_CALLIN_IMPLEMENTATION.md).

Try the original street-art options as you unlock them:

- **CHROMAGEDDON:** a large paint wave damages owned enemies and colors actual surfaces.
- **Tag Team:** a paint rover helps tag targets.
- **Bassquake:** timed bass pulses push and damage rivals.
- **Inkstorm Curtain:** paint smoke disrupts pursuit.
- **Fresh Tracks:** a temporary route improves speed and boost recovery.
- **Graffiti Ghost:** a destructible decoy draws rivals away.

Right-click a physical support delivery to collect it. Support equipment uses hotbar four, the vehicle controller seven and backpack eight. Native guns use TaCZ's controls. Adapted tools/controllers use right-click, with sneak-right-click for an alternate action where available. Mount a vehicle/emplacement by right-clicking it; normal movement keys steer mobile hardware and sneak dismounts. These are one-rider adaptations with finite ammunition or energy.

**Resupply** uses shared mission charges and its displayed cooldown. It replenishes the selected guns' proper ammunition, heals eight health points and restores boost; reload your guns normally afterward. Supply Skates research and the workshop module improve starting supply. Eagle calls share turnaround/rearm and have finite charges. If you are **Downed** and reinforcement drops remain, choose **Reinforce** from the radio to land in another pod.

## Extract, visit the ship, return home

After every required stage is complete:

1. Reach the extraction pad and call the ship from within twelve blocks.
2. Stay within forty-eight blocks for thirty seconds while rivals pressure the zone.
3. Let the ship land. Approach within eight blocks and right-click it, or choose **Board extraction** in Field.
4. Ride the departure to the ship hub, where the successful result and samples are banked.

Leaving the landing zone cancels the call. A ship left waiting eventually departs empty, but another call is possible while mission time remains. Optional objectives do not need to be finished first. Board before the deadline: once boarding is accepted, even on the last valid tick, the flight finishes and banks the successful extraction after the clock runs out. New boarding closes when time expires.

The physical ship is a place to review the next expedition, unlock calls and install modules. Close the menu to walk around; the command radio opens it again. Choose another operation or **Ship → Return to Minecraft** to restore your original world, position and belongings. From an active field operation, **Return home** or `/jetsetcraft liberty return` leaves early and restores the original state.

**Samples require successful extraction.** An early return or loss retains earned credits and completed-objective progress while leaving field samples behind. Loan weapons, armor, support equipment and temporary paint belong to the operation. Disconnect/recovery uses the saved return checkpoint; an unresolved recovery blocks another deployment until it is handled.

## Research and ship upgrades that connect modes

Six research projects spend super credits and samples. Open **Research** outside an active deployment; the panel shows exact costs and your balance.

| Research | What it enables |
| --- | --- |
| Pressure Saver | Every fourth successful graffiti placement costs no spray durability, including ordinary-world art; prerequisite for the ship workshop. |
| Magnetic Pigment | Magnetic Mixtape as the selected Rival Rush starting preparation: +1.5-block pickup reach. |
| Blast Stencil | Bigger Chorus as that preparation: +12% paint/artifact area. |
| Prism Ink | Finishing Touch as that preparation: +25% base damage on critical hits. |
| Signal Lacquer | Prerequisite for the Graffiti Targeting Array. Its old marked-panel discount remains in legacy expeditions; new territory quotas are unchanged. |
| Supply Skates | One additional starting shared resupply charge and the Eagle Hangar prerequisite. |

Use **Select for Rival Rush** for a researched preparation. It occupies the same **one starting preparation** as the Style Library/home choice. Selecting a crew style replaces that override; it does not add an extra artifact slot or starting rank.

Primary progress and extraction also earn separate **requisitions**. Spend those in **Call-ins** or **Ship**:

| Ship module | Cost | Requirement and effect |
| --- | ---: | --- |
| Paint & Powder Workshop | 120 req | Pressure Saver; an extra starting shared resupply charge |
| Graffiti Targeting Array | 180 req | Signal Lacquer; 10% shorter call-in cooldowns |
| Skater Eagle Hangar | 220 req | Supply Skates; one more charge per equipped Eagle call each rearm |
| Crew Sample Lab | 260 req | Founded crew with 100 shared crew XP; one extra common sample per completed primary objective on successful extraction |

Liberty's accepted results also contribute **Crew Supplies, crew XP, member training and gang reputation** through the same account used by street games, crew houses and Rival Rush. Crew XP keeps unlocking Style Library lessons. The sample laboratory makes growing your crew useful aboard the ship; ordinary graffiti research and Rush preparations make expedition discoveries useful back on the streets.

## Defense offers and your normal world

The campaign is shared, but each deployment is **solo**. Losses can raise regional threat and create a timed defense offer. Its clock advances only while campaign players are actively in the field, not while the server is offline or everyone is doing ordinary Minecraft activities.

Defense offers draw varied missions suited to their faction, each including a defense objective. Retrying an offer keeps its mission choice. You can change difficulty or duration without changing that mission family.

Losing or missing a regional defense can produce a **home-defense offer**. It has no deadline and never invades automatically. Choose it deliberately, review the loadout and deploy when you want to defend. Home objectives are compact and use the existing landscape; temporary owned mobs, weak points and paint overlays are removed when the operation ends. Existing builds, containers, unrelated mobs and old graffiti are protected by the operation's ownership and interaction guards. Native effects that cannot honor those boundaries use documented safe adaptations or block admission with a reason.

Home objectives begin only after valid placement. Courier beacons follow each checkpoint's actual ground height. Escort rigs can climb one-block slopes and take short clear detours; a new obstruction pauses the rig. Routes use loaded terrain without rebuilding it. If no suitable required route fits, deployment stays closed and the offer remains available for another attempt.

The **HUD** button lets you move, scale or hide the mission display. Its layout is local to your client. The campaign and equipment menus use search, pages and scrolling. The archived alpha.9 native audit reviewed controls at the default size and **320 × 240 logical pixels**, including the full loadout review and its scrolled explanation. Its controlled walkthrough showed actual deployment, CHROMAGEDDON paint, the ship and original-world returns; fresh hosted results for this continuation are pending. Human 20–30 minute pacing, difficulty, equipment economy and sustained server load still require playtesting. Shared combat squads and full source-game NPC/vehicle parity are outside the current implementation.

Continue with [Rival Rush](Rival-Rush), [Crew Houses and Progress](Player-Chapters-Posse-and-Mob-Archetypes), [Graffiti](Graffiti), or [Testing and Verification](Testing-and-Verification).
