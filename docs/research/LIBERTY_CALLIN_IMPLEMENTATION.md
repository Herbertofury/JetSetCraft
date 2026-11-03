# Liberty Grind call-in research and implementation map

Research snapshot: **2026-11-02**. This document maps the source catalogue to executable behavior. Runtime receipts live separately under `docs/verification/`; a catalogue row or configured test is not evidence that a client/server run passed.

## What is included

The snapshot contains **94 Helldivers 2 selectable or campaign-unlocked stratagems**, plus **six JetSetCraft originals**, giving **100 selectable calls**. A separate **25 reference/conditional entries** contains four shared commands, sixteen objective commands and five unavailable, tutorial or temporary-event entries. Historical entries cannot occupy a loadout slot. Research did not mistake wiki disambiguation pages, equipment traits or an April Fools entry for usable equipment.

| Original category | Selectable entries |
| --- | ---: |
| Orbital | 12 |
| Eagle | 8 |
| Support weapon | 35 |
| Backpack | 13 |
| Vehicle | 8 |
| Sentry | 10 |
| Emplacement | 8 |
| JetSetCraft originals | 6 |

The source names/statuses are documented in [the dated research JSON](LIBERTY_CALLIN_CATALOGUE.json). Current category listings provide broad coverage; first-party announcements confirm recent additions including the Apex, Nova, Exo Experts equipment and Liberty Day content. Exact status of every historical event was not individually confirmed by a first-party announcement. Cooldowns, requisition prices, damage, capacities and controls in JetSetCraft are explicitly **adaptation values**, not a claim of original-game balance parity.

Primary announcements used:

- [Scope of Authority, PlayStation Blog, 2026-10-13](https://blog.playstation.com/2026/10/13/helldivers-2-scope-of-authority-premium-warbond-launches-oct-20/).
- [Exo Experts, PlayStation Blog, 2026-04-21](https://blog.playstation.com/2026/04/21/helldivers-2-the-exo-experts-warbond-drops-april-28/).
- [Liberty Day, PlayStation Blog, 2026-10-26](https://blog.playstation.com/2026/10/26/celebrate-helldivers-2-liberty-day-2026-with-in-game-gifts-today).
- [Official Steam patch announcement linked by the research snapshot](https://store.steampowered.com/news/app/553850/view/708913061799395773?l=english).

## Actual Minecraft providers

**MCDiver / MCDivers by shiroaki193, Forge 1.20.1 release 1.0.3**, is the native stratagem provider. The exact published JAR supplies planted beacons, real falling ordnance, Eagle jets, an orbital laser and two native sentry types. Nine matching catalogue entries invoke those actual provider actors in isolated Liberty districts. The author's extra `CARPET_BOMBING` enum is not presented as a researched Helldivers 2 stratagem. Native Guard Dog/Flag item workflows are not silently imported into mission inventories; their JetSetCraft versions have owned finite hardware and cleanup.

- [Official Forge release, file 9307472](https://www.curseforge.com/minecraft/mc-mods/mcdiver/files/9307472).
- [Official project/license](https://www.curseforge.com/minecraft/mc-mods/mcdiver/license): MIT; preserve upstream notices if distributing the upstream work. No upstream JAR or assets are embedded in JetSetCraft.
- Published metadata: mod ID `mcdivers`, version `1.0.3`, Forge range `[47.4.22,)`, Minecraft range `[1.20.1,1.21)`, no additional mandatory mod dependencies.
- SHA-256 `49c4ab153c50dab8766fca15ab7affcc14b1c9a8e7e52d58a9683c06ac4ceac0`, 2,160,981 bytes.

The other native provider is **TaCZ 1.1.8-hotfix2** with the merged **[eof]Helldivers:Liberty Day** pack. The pre-deployment armory enumerates all **68 live gun definitions** from `helldiver2` and `zeta`, and support calls use exact matching native IDs where verified. `zeta:ac7` really is the file/index ID of this release's AC-8. An attractive display name is never used to guess a registry ID. [Pack release](https://www.curseforge.com/minecraft/customization/tacz-helldivers-escalation-of-freedom/files/9341504), [install/provenance guide](../LIBERTY_GRIND_PACK.md).

SBW Helldivers 0.5.29 was also inspected as a possible base. Its [official Forge release](https://www.curseforge.com/minecraft/mc-mods/sbw-helldivers/files/9359255) has a larger mandatory dependency chain and custom Superb Warfare damage/explosion paths. This implementation uses MCDivers' smaller native lifecycle plus explicit JetSetCraft adaptations. No SBW dependency or source/assets are included.

## Scope and controls

Choose up to four distinct unlocked calls before deploying. The operation owns cooldowns, uses, Eagle ammunition and all actors; client buttons cannot invent resources. Calls validate the active phase, loaded target, operation bounds and a 128-block range. While downed, only available Reinforce is ready. Eagle calls share a turnaround, and a 120-second adapted rearm restores all equipped Eagle charges together. Targeting Array modifies cooldowns; Eagle Hangar adds a charge per Eagle call. A loadout cannot be changed after its first accepted call.

Support deliveries are destructible entities. Right-click an arrived delivery to equip it. The support slot is hotbar four, the field radio is hotbar five, the vehicle controller is hotbar seven and the backpack is hotbar eight. Mission primary/secondary weapons and spray equipment retain their own slots. Native TaCZ support weapons use TaCZ fire/reload inputs. Adapted support tools use right-click to fire and sneak-right-click for an alternate action. All supplied gear retains the operation loan owner. Hold the backpack in hotbar eight and right-click to activate its ability; rovers and shields also operate while carried. The field radio does not expose a separate equipment-action button.

Right-click your vehicle/emplacement to mount. Normal movement keys steer mobile hardware; right-click the vehicle controller to fire its main weapon and sneak-right-click for its secondary. Sneak dismounts. This is a **one-rider adaptation**, not original-game multi-seat vehicle parity. Weapons have finite ammunition/energy. Supply backpacks and the Supply FRV have finite onboard supply charges independent of the shared mission supply pool. Personal and relay shields absorb from finite energy; directional shields require the source to be in front. Rovers are real orbiting, destructible support entities with different bullets, beams, gas, flame, arc or paint effects.

The native MCDivers path is deliberately restricted to the dedicated district. Home defenses use the corresponding explicit JetSetCraft effect so that physical objectives, ordinary terrain and belongings are preserved. When an optional provider or exact gun definition is unavailable, the field row reports the functional adaptation actually used. The selected primary/secondary armory never replaces a missing chosen gun with a fake or differently named weapon.

## World and ownership boundaries

Every native actor is admitted by exact registered type, actual provider owner and the active operation, then tracked by object identity. Private MCDivers `owner`/`ownerId` fields are read only for exact version 1.0.3 actors whose public API does not expose ownership. Forged tags do not establish ownership. Native blast event lists are filtered at HIGHEST priority before MCDivers' own damage listener, and direct damage is filtered too. Native sentry temporary light is restored on close only if the current block is still the exact provider light placed into a recorded empty location.

TaCZ's native sweep does not use the ordinary Forge projectile-impact hook. Its early practice-target callback also precedes `EntityHurtByGunEvent.Pre`, and native ignition precedes normal damage cancellation. The optional `@Pseudo` mixin therefore guards owned native entity hits at the head of `onHitEntity`, before any of those effects. The Liberty admission check requires the exact supported TaCZ release, verified hit methods, private terrain fields and the injected marker interface. Missing/mismatched native safety fails closed for owned bullets and blocks expedition admission; ordinary JetSetCraft remains loadable.

Only admitted owned bullets have `igniteBlock` and `explosionDestroyBlock` set false. No global provider setting changes. Owned block hits skip projectile-reactive block callbacks and fire placement; explosive impacts invoke the real provider `ExplodeUtil.createExplosion` with block destruction disabled, retaining genuine native owned-enemy damage. Normal out-of-operation provider behavior is unchanged. Operation overlays do not replace blocks or remove existing tags. Closing an operation removes all hardware, provider actors, pending salvos, zones and temporary paint owned by that operation.

Vanilla mission projectiles are intercepted through Forge before native block callbacks. A bounded owned-potion adapter applies the actual payload only to living actors admitted by the operation's damage policy, retaining the Witch/projectile source and native radius/falloff. Healing, water effects and status effects receive the same ownership filter. Unusual custom payloads are bounded to sixteen effects, amplifier four and 3,600 ticks before falloff; owned lingering potions resolve as one bounded splash. The adapter creates no free-running area cloud and never invokes fire, candle, campfire or target-block callbacks. Ordinary out-of-operation potions and arrows are unchanged.

Two additional Minecraft 1.20.1 guards cover physical home contact: native block-trigger participation, inside-block dispatch, step callbacks and fall callbacks. They use exact development/production selectors with required injection counts, and home admission verifies the actual marker interfaces. These guards apply only to exact live home-operation participants/actors; ordinary actors retain native block interactions. Native mob initialization and Forge reinforcement/conversion policies contain foreign child creation and raids, with narrow home-only species substitutions for native behaviors without a sufficient cancellation hook.

## Complete selectable behavior map

**Native MCDivers** means the exact named provider call when the compatible provider is installed and the current district admits it; the listed JetSetCraft effect is also the bounded home/optional-provider adaptation. **Native TaCZ** means the exact live pack weapon, with its real data, ammo and reload behavior. **JetSetCraft** means an implemented custom effect/hardware/tool using the controls above.

### Orbital

| Call / stable ID | Backend | Implemented behavior |
| --- | --- | --- |
| Orbital Precision Strike<br>`orbital_precision_strike` | Native MCDivers `ORBITAL_PRECISION_STRIKE` | One delayed heavy point strike; a tight blast damages hostile mobs only. |
| Orbital Gatling Barrage<br>`orbital_gatling_barrage` | JetSetCraft | Rapid narrow sweeps repeatedly suppress a marked area. |
| Orbital Gas Strike<br>`orbital_gas_strike` | JetSetCraft | A lingering gas cloud poisons and disorients hostile mobs. |
| Orbital 120mm HE Barrage<br>`orbital_120mm_he_barrage` | Native MCDivers `ORBITAL_120MM_BARRAGE` | Repeated medium shells rain across the marked area. |
| Orbital Airburst Strike<br>`orbital_airburst_strike` | JetSetCraft | Three expanding fragment bursts hit a broad group above the mark. |
| Orbital Smoke Strike<br>`orbital_smoke_strike` | JetSetCraft | Dense smoke breaks hostile targeting while the skater relocates. |
| Orbital EMS Strike<br>`orbital_ems_strike` | JetSetCraft | A lingering electrical field arrests hostile movement without blast damage. |
| Orbital 380mm HE Barrage<br>`orbital_380mm_he_barrage` | Native MCDivers `ORBITAL_380MM_BARRAGE` | Slower heavy shells cover a wider area than the 120mm barrage. |
| Orbital Walking Barrage<br>`orbital_walking_barrage` | JetSetCraft | Successive shell lines advance away from the caller. |
| Orbital Laser<br>`orbital_laser` | Native MCDivers `ORBITAL_LASER` | A sustained beam follows the toughest nearby enemy; three uses. |
| Orbital Napalm Barrage<br>`orbital_napalm_barrage` | JetSetCraft | A wide barrage creates temporary burning damage zones without world fire. |
| Orbital Railcannon Strike<br>`orbital_railcannon_strike` | JetSetCraft | An armor-breaking precision shot selects the toughest target near the mark. |

### Eagle

| Call / stable ID | Backend | Implemented behavior |
| --- | --- | --- |
| Eagle Strafing Run<br>`eagle_strafing_run` | Native MCDivers `EAGLE_STRAFING_RUN` | A rapid narrow bullet run follows the caller's aim direction. |
| Eagle Airstrike<br>`eagle_airstrike` | Native MCDivers `EAGLE_AIRSTRIKE` | A perpendicular line of bombs clears a crossing lane. |
| Eagle Cluster Bomb<br>`eagle_cluster_bomb` | JetSetCraft | Many smaller fragment bursts cover a wide rectangle. |
| Eagle Smoke Strike<br>`eagle_smoke_strike` | JetSetCraft | A line of smoke breaks targeting across a crossing. |
| Eagle Napalm Airstrike<br>`eagle_napalm_airstrike` | JetSetCraft | A line of temporary burning zones controls a lane without igniting blocks. |
| Eagle 110mm Rocket Pods<br>`eagle_110mm_rocket_pods` | JetSetCraft | Three paired rockets concentrate on the toughest enemy near the mark. |
| Eagle 500kg Bomb<br>`eagle_500kg_bomb` | Native MCDivers `EAGLE_500KG_BOMB` | A delayed, concentrated large blast with a visible warning. |
| Eagle Gas Airstrike<br>`eagle_gas_airstrike` | JetSetCraft | A line of lingering toxic clouds disorients enemies. |

### Support Weapon

| Call / stable ID | Backend | Implemented behavior |
| --- | --- | --- |
| 40-K Meltagun<br>`40_k_meltagun` | JetSetCraft | Short-range thermal cone trades reach for very high armor damage. |
| AC-8 Autocannon<br>`ac_8_autocannon` | Native TaCZ `zeta:ac7` | Explosive autocannon rounds punch through groups and armor. |
| AMW-20 Apex<br>`amw_20_apex` | JetSetCraft | Slow, heavy armor-piercing rifle with finite reserve rounds. |
| APW-1 Anti-Materiel Rifle<br>`apw_1_anti_materiel_rifle` | Native TaCZ `zeta:apw1` | A long-range rifle rewards precise shots against tough enemies. |
| ARC-3 Arc Thrower<br>`arc_3_arc_thrower` | Native TaCZ `zeta:arc3` | Electrical shots chain between nearby hostile mobs. |
| B/FLAM-80 Cremator<br>`b_flam_80_cremator` | JetSetCraft | Long-duration backpack-fed flame cone with a large fuel reserve. |
| B/MD C4 Pack<br>`b_md_c4_pack` | JetSetCraft | Plant up to four remote charges; alternate use detonates only the caller's charges. |
| CQC-1 One True Flag<br>`cqc_1_one_true_flag` | JetSetCraft | A reach weapon; alternate use plants a rally flag that steadies nearby fighting. |
| CQC-20 Breaching Hammer<br>`cqc_20_breaching_hammer` | JetSetCraft | A slow melee shockwave staggers clustered hostile mobs; terrain is preserved. |
| CQC-9 Defoliation Tool<br>`cqc_9_defoliation_tool` | JetSetCraft | Fast close-range sustained cutting with finite battery charge. |
| EAT-17 Expendable Anti-Tank<br>`eat_17_expendable_anti_tank` | JetSetCraft | Two single-use anti-armor rockets per delivery. |
| EAT-411 Leveller<br>`eat_411_leveller` | Native TaCZ `zeta:eat411` | A disposable heavy area-demolition round with no terrain destruction. |
| EAT-700 Expendable Napalm<br>`eat_700_expendable_napalm` | JetSetCraft | A single-use rocket leaves a broad temporary burn zone. |
| FAF-14 Spear<br>`faf_14_spear` | Native TaCZ `zeta:spear` | A lock-on heavy rocket targets the toughest visible enemy. |
| FLAM-40 Flamethrower<br>`flam_40_flamethrower` | JetSetCraft | A short flame cone burns enemies while preserving blocks. |
| GL-21 Grenade Launcher<br>`gl_21_grenade_launcher` | Native TaCZ `zeta:gl21` | Lob explosive rounds into groups of hostile mobs. |
| GL-28 Belt-Fed Grenade Launcher<br>`gl_28_belt_fed_grenade_launcher` | Native TaCZ `zeta:gl28` | A larger grenade reserve supports sustained area suppression. |
| GL-52 De-Escalator<br>`gl_52_de_escalator` | JetSetCraft | Electrical grenades stun and slow rather than using a heavy blast. |
| GR-8 Recoilless Rifle<br>`gr_8_recoilless_rifle` | Native TaCZ `zeta:gr8` | Slow, heavy armor-piercing explosive rounds with limited ammunition. |
| LAS-44 Nova<br>`las_44_nova` | JetSetCraft | A charged, precise nonexplosive thermal shot recharges between bursts. |
| LAS-98 Laser Cannon<br>`las_98_laser_cannon` | Native TaCZ `zeta:las98` | A sustained heat-limited beam; allow cooling between shots. |
| LAS-99 Quasar Cannon<br>`las_99_quasar_cannon` | Native TaCZ `zeta:quasar` | An infinite-ammo, long-cooldown anti-armor energy pulse. |
| M-1000 Maxigun<br>`m_1000_maxigun` | Native TaCZ `zeta:m1000` | High-volume multi-shot bursts with a deep finite ammunition reserve. |
| M-105 Stalwart<br>`m_105_stalwart` | Native TaCZ `zeta:m105` | Fast light machine-gun bursts with broad handling and a large reserve. |
| MG-206 Heavy Machine Gun<br>`mg_206_heavy_machine_gun` | Native TaCZ `zeta:m206` | Slower heavy rounds trade reserve size for armor damage. |
| MG-43 Machine Gun<br>`mg_43_machine_gun` | JetSetCraft | Balanced medium machine-gun bursts with finite ammunition. |
| MGX-42 Bullet Storm<br>`mgx_42_bullet_storm` | Native TaCZ `zeta:mgx42` | An expendable extreme-rate burst weapon; spent units cannot refill. |
| MLS-4X Commando<br>`mls_4x_commando` | JetSetCraft | Four guided anti-armor rockets per delivery. |
| MS-11 Solo Silo<br>`ms_11_solo_silo` | JetSetCraft | A target designator schedules a descending missile on a visible enemy. |
| PLAS-45 Epoch<br>`plas_45_epoch` | Native TaCZ `zeta:plas45` | Charged plasma shots combine impact damage with a compact shock blast. |
| RL-77 Airburst Rocket Launcher<br>`rl_77_airburst_rocket_launcher` | JetSetCraft | A proximity burst scatters fragments across an enemy group. |
| RS-422 Railgun<br>`rs_422_railgun` | Native TaCZ `zeta:railgun` | Piercing shots reward aim; alternate fire trades delay for greater damage. |
| S-11 Speargun<br>`s_11_speargun` | Native TaCZ `zeta:s11` | A heavy piercing bolt damages and pulls an enemy toward the shooter. |
| StA-X3 W.A.S.P. Launcher<br>`sta_x3_w_a_s_p_launcher` | JetSetCraft | A multi-missile swarm locks to a target; alternate use concentrates the salvo. |
| TX-41 Sterilizer<br>`tx_41_sterilizer` | JetSetCraft | A short gas stream confuses and poisons hostile mobs. |

### Backpack

| Call / stable ID | Backend | Implemented behavior |
| --- | --- | --- |
| AX/AR-23 Guard Dog<br>`ax_ar_23_guard_dog` | JetSetCraft | An orbiting drone automatically fires at hostile mobs with a finite reserve. |
| AX/ARC-3 K-9<br>`ax_arc_3_k_9` | JetSetCraft | An orbiting drone chains electrical damage between nearby enemies. |
| AX/FLAM-75 Hot Dog<br>`ax_flam_75_hot_dog` | JetSetCraft | An orbiting drone burns close enemies with a short cone. |
| AX/LAS-5 Rover<br>`ax_las_5_rover` | JetSetCraft | An orbiting drone uses a heat-limited continuous beam. |
| AX/TX-13 Dog Breath<br>`ax_tx_13_dog_breath` | JetSetCraft | An orbiting drone disorients and poisons nearby enemies. |
| B-1 Supply Pack<br>`b_1_supply_pack` | JetSetCraft | Four personal resupply uses replenish support ammunition and invoke mission supply. |
| B-100 Portable Hellbomb<br>`b_100_portable_hellbomb` | JetSetCraft | Right-click the held backpack to arm it; its delayed blast affects hostile mobs only. |
| LIFT-182 Warp Pack<br>`lift_182_warp_pack` | JetSetCraft | Right-click the held backpack to blink to a clear landing point ahead. |
| LIFT-850 Jump Pack<br>`lift_850_jump_pack` | JetSetCraft | Right-click the held backpack for an upward and forward impulse; a recharge prevents spam. |
| LIFT-860 Hover Pack<br>`lift_860_hover_pack` | JetSetCraft | Right-click the held backpack to enable a short hover; descend safely while its charge recharges. |
| SH-20 Ballistic Shield Backpack<br>`sh_20_ballistic_shield_backpack` | JetSetCraft | A forward-facing ballistic guard absorbs frontal projectile damage. |
| SH-32 Shield Generator Pack<br>`sh_32_shield_generator_pack` | JetSetCraft | A rechargeable shield absorbs damage until its energy runs out. |
| SH-51 Directional Shield<br>`sh_51_directional_shield` | JetSetCraft | A wide forward energy guard intercepts hostile incoming projectiles. |

### Vehicle

| Call / stable ID | Backend | Implemented behavior |
| --- | --- | --- |
| EXO-45 Patriot Exosuit<br>`exo_45_patriot_exosuit` | JetSetCraft | A rideable armored walker with machine gun and alternate-fire rockets. |
| EXO-49 Emancipator Exosuit<br>`exo_49_emancipator_exosuit` | JetSetCraft | A rideable armored walker with alternating explosive autocannons. |
| EXO-51 Lumberer Exosuit<br>`exo_51_lumberer_exosuit` | JetSetCraft | A rideable walker with a heavy cannon and alternate-fire flame cone. |
| EXO-55 Breakthrough Exosuit<br>`exo_55_breakthrough_exosuit` | JetSetCraft | A close-range walker with scattergun and alternate shield bash. |
| M-102 Gunner FRV<br>`m_102_gunner_frv` | JetSetCraft | A fast rideable car with a mounted machine gun; one rider in this adaptation. |
| M-103 Supply FRV<br>`m_103_supply_frv` | JetSetCraft | A fast rideable supply car with four refills instead of a gun. |
| TD-110 Maelstrom<br>`td_110_maelstrom` | JetSetCraft | A heavily armored, slower vehicle firing paired Gatling bursts. |
| TD-220 Bastion MK XVI<br>`td_220_bastion_mk_xvi` | JetSetCraft | A slow armored vehicle with heavy main cannon and alternate coaxial machine gun. |

### Sentry

| Call / stable ID | Backend | Implemented behavior |
| --- | --- | --- |
| A/MG-43 Machine Gun Sentry<br>`a_mg_43_machine_gun_sentry` | Native MCDivers `SENTRY_MACHINE_GUN` | A finite-ammunition sentry picks visible hostile mobs and fires medium bursts. |
| A/G-16 Gatling Sentry<br>`a_g_16_gatling_sentry` | Native MCDivers `SENTRY_GATLING` | A fast sentry saturates nearby visible enemies with light rounds. |
| A/AC-8 Autocannon Sentry<br>`a_ac_8_autocannon_sentry` | JetSetCraft | A slow sentry prioritizes armored enemies with explosive shells. |
| A/M-12 Mortar Sentry<br>`a_m_12_mortar_sentry` | JetSetCraft | A mortar lofts delayed shells over cover into hostile groups. |
| A/MLS-4X Rocket Sentry<br>`a_mls_4x_rocket_sentry` | JetSetCraft | A rocket sentry selects the toughest visible target. |
| A/ARC-3 Tesla Tower<br>`a_arc_3_tesla_tower` | JetSetCraft | A close-range electrical tower chains between nearby hostile mobs. |
| A/M-23 EMS Mortar Sentry<br>`a_m_23_ems_mortar_sentry` | JetSetCraft | A mortar creates slowing electrical fields instead of lethal shell blasts. |
| A/LAS-98 Laser Sentry<br>`a_las_98_laser_sentry` | JetSetCraft | A heat-limited beam sentry tracks a visible target. |
| A/FLAM-40 Flame Sentry<br>`a_flam_40_flame_sentry` | JetSetCraft | A short-range flame sentry controls close approaches. |
| A/GM-17 Gas Mortar Sentry<br>`a_gm_17_gas_mortar_sentry` | JetSetCraft | A mortar places lingering poisonous, confusing clouds. |

### Emplacement

| Call / stable ID | Backend | Implemented behavior |
| --- | --- | --- |
| MD-6 Anti-Personnel Minefield<br>`md_6_anti_personnel_minefield` | JetSetCraft | A field of individually armed mines triggers only on hostile mobs. |
| MD-I4 Incendiary Mines<br>`md_i4_incendiary_mines` | JetSetCraft | Individual proximity mines leave short-lived burn zones. |
| MD-17 Anti-Tank Mines<br>`md_17_anti_tank_mines` | JetSetCraft | Fewer, heavier mines trigger on large or high-health hostile mobs. |
| FX-12 Shield Generator Relay<br>`fx_12_shield_generator_relay` | JetSetCraft | A destructible relay protects its owner inside a fixed-radius energy bubble. |
| E/MG-101 HMG Emplacement<br>`e_mg_101_hmg_emplacement` | JetSetCraft | A stationary rideable heavy machine-gun emplacement with finite ammunition. |
| E/GL-21 Grenadier Battlement<br>`e_gl_21_grenadier_battlement` | JetSetCraft | A stationary grenade emplacement grants frontal protection while occupied. |
| MD-8 Gas Mines<br>`md_8_gas_mines` | JetSetCraft | Individual proximity mines release gas that confuses and poisons hostiles. |
| E/AT-12 Anti-Tank Emplacement<br>`e_at_12_anti_tank_emplacement` | JetSetCraft | A stationary rideable cannon fires powerful anti-armor shells. |

### Signature

| Call / stable ID | Backend | Implemented behavior |
| --- | --- | --- |
| CHROMAGEDDON<br>`chromageddon` | JetSetCraft | Paints nearby streets and walls in expanding rings and blasts hostile mobs. |
| Tag Team<br>`tag_team` | JetSetCraft | A paint rover follows you, splashes nearby surfaces and pelts hostile mobs. |
| Bassquake<br>`bassquake` | JetSetCraft | Five timed bass pulses damage, stagger and push hostile mobs away from the marked center. |
| Inkstorm Curtain<br>`inkstorm_curtain` | JetSetCraft | A long paint-smoke screen breaks hostile targeting, slows pursuit and marks nearby real surfaces. |
| Fresh Tracks<br>`fresh_tracks` | JetSetCraft | A temporary luminous trail grants the caller speed and restores skating boost while inside it. |
| Graffiti Ghost<br>`graffiti_ghost` | JetSetCraft | A graffiti decoy draws hostile mobs away while you flank. |

## Shared and mission-only commands

Shared actions do not consume a loadout slot. Reinforce consumes the real operation reinforcement resource and drops a fresh pod after a downed state. Resupply consumes the shared supply resource. Eagle Rearm resets the actual equipped Eagle charge pool after its timer. SOS is disclosed as solo crew relief: temporary owned armed crew helpers, not network matchmaking or a message to other players.

Mission calls are exposed only when the current objective and actual resource state authorize them. A generic terminal interaction is not sufficient authority. SEAF Artillery gains six finite shells after an eligible optional scan/relay objective; its shell count is operation-owned. Hellbomb and breach drills show their warning marker and wait five seconds before affecting the actual owned target area. Camera/courier commands activate actual timed scans or ordered routes and supply their owned token. Flags/radio commands bind to the matching objective site. Dark Fluid remains conditional on the explicit Dark Ink operation; it is not a normal backpack.

| Reference / stable ID | Classification |
| --- | --- |
| Reinforce / `reinforce` | Shared; Shared operation command; it does not consume a loadout slot. |
| Resupply / `resupply` | Shared; Shared operation command; it does not consume a loadout slot. |
| SoS Beacon / `sos_beacon` | Shared; Shared operation command; it does not consume a loadout slot. |
| Eagle Rearm / `eagle_rearm` | Shared; Shared operation command; it does not consume a loadout slot. |
| Activate E-711 Extraction Drill / `activate_e_711_extraction_drill` | Mission; Available only when the active operation owns and requires this mission command. |
| Aquifer Drill / `aquifer_drill` | Mission; Available only when the active operation owns and requires this mission command. |
| Cargo Container / `cargo_container` | Mission; Available only when the active operation owns and requires this mission command. |
| Dark Fluid Vessel / `dark_fluid_vessel` | Mission; Historical Meridia objective equipment has also returned as temporary 2026 operational augmentation; do not mark permanently unobtainable or permanent loadout equipment. |
| Hive Breaker Drill / `hive_breaker_drill` | Mission; Available only when the active operation owns and requires this mission command. |
| NUX-223 Hellbomb / `nux_223_hellbomb` | Mission; Available only when the active operation owns and requires this mission command. |
| Portable Comms Relay / `portable_comms_relay` | Mission; Record evidence at the marked reconnaissance site. |
| Prospecting Drill / `prospecting_drill` | Mission; Available only when the active operation owns and requires this mission command. |
| Reinforcement Pods / `reinforcement_pods` | Mission; Available only when the active operation owns and requires this mission command. |
| SEAF Artillery / `seaf_artillery` | Mission; Conditional on completed artillery objective and loaded shells; ammunition payload variants are not separate loadout stratagems. |
| Seismic Probe / `seismic_probe` | Mission; Available only when the active operation owns and requires this mission command. |
| SSSD Delivery / `sssd_delivery` | Mission; Two input-code variants appear in current catalogue, representing the same named objective equipment rather than two distinct selectable stratagems. |
| Super Earth Flag / `super_earth_flag` | Mission; Available only when the active operation owns and requires this mission command. |
| Tactical Video Camera / `tactical_video_camera` | Mission; Record evidence at the marked reconnaissance site. |
| Tectonic Drill / `tectonic_drill` | Mission; Available only when the active operation owns and requires this mission command. |
| Upload Data / `upload_data` | Mission; Available only when the active operation owns and requires this mission command. |
| Call In Super Destroyer / `call_in_super_destroyer` | Historical; Listed among ship mission commands; not an ordinary four-slot loadout option. Verify exact tutorial trigger before implementation. |
| Orbital Illumination Flare / `orbital_illumination_flare` | Historical; Explicitly under Unavailable in the current catalogue. |
| M-104 Incinerator FRV / `m_104_incinerator_frv` | Historical; Previously available only as Sangis operational variation; Basquine VIII won Census Thunder, awarding Supply FRV instead; page explicitly says currently unavailable. |
| Orbital Confetti / `orbital_confetti` | Historical; Temporary Liberty Day 2026 provision on certain planets; not a permanent damage stratagem. |
| Eagle Fly By / `eagle_fly_by` | Historical; Temporary Liberty Day 2026 provision on certain planets; not a permanent damage stratagem. |

## Verification boundary

`LibertyStratagemGameTests` covers catalogue availability, four-slot admission, phase/readiness rules, real Eagle resources, physical sentry/mine/relay effects, CHROMAGEDDON callback radius with actual graffiti entities, collected tools, backpack impulses, rideable hardware, real MCDivers ordnance/sentry effects, foreign residents, protected terrain/chest NBT and native cleanup.

`LibertyNativeTerrainGameTests` uses a genuine home offer, real pod landing and the production director. It constructs actual TaCZ Torcher/GL-21 bullets with their live pack data, then lets the native collision tick run. Controlled fixture flags turn block ignition/destruction on before admission to prove the production guard turns them off; no global provider config is modified. A foreign cow, real TaCZ practice-target minecart, wood, air blocks and a chest remain unchanged, while the real grenade still damages the home operation's own enemy. The same home fixture selects an actual Illuminate defense, captures a director-spawned Witch and infantry, and exercises genuine block-hit ThrownPotion/Arrow ticks: owned effects remain real, an injured foreign cow is not healed/slowed, and the redstone target state is preserved. Existing native shooting tests separately cover `IGunOperator.shoot`, real ammo consumption and reload. `LibertyMobSafetyGameTests` adds actual native director actors, hard Zombie reinforcement, conversion/raid/Shulker checks, physical contact preservation and ordinary controls after real return. Its native conversion timer is accelerated explicitly; it does not post a fabricated conversion event.

A source test is not a release receipt. Consult the current verification document for the exact tested revision, provider matrix, screenshots and installed-package evidence. Test control of fixture positions, health or provider construction is not a claim of balanced weapon damage, original-game numerical fidelity, real-client animation fidelity or multiplayer latency behavior.
