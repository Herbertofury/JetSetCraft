# JetSetCraft - Curated Modded Gang Atlas

Status: Accepted expansion contract for Forge 1.20.1
Purpose: Give popular creature mods premium JetSetCraft gang identities while preserving universal runtime fallback for every other installed Mob.

## 1. Non-negotiable model

JetSetCraft never replaces another mod's entity. Every curated record maps external EntityType IDs to JetSetCraft-owned social/presentation data only.

Each curated record uses:

- stable `gang_id`
- optional `crew_family_id`
- exact provider mod ID and supported entity IDs
- canonical display name plus rename-safe aliases
- starting disposition
- colors and bespoke graffiti art direction
- challenge preferences and signature technique hooks
- ride/contact profile hints, never hard class ownership
- reputation reward track
- one or more unlockable graffiti assets
- optional Junior/legendary/champion relationship
- provider version/source metadata

A missing provider or missing entity ID makes that record dormant. It must never crash JetSetCraft, delete history, or silently rebind to a different creature.

## 2. Per-gang graffiti progression

Every curated gang must have a fun art reward path. Do not ship empty reward tables.

Default ladder, configurable by datapack/server policy:

1. Spotted / Discovered - reveal the crew mark in the Atlas as a locked preview.
2. Recognized - unlock a small sticker/decal or color palette tied to the crew.
3. Respected / Friendly - unlock the canonical gang tag for use in the normal Graffiti Gallery/selector.
4. Member - unlock the full gang emblem/logo and one ride/Boombox/chapter decal where that surface supports decals.
5. Veteran - unlock an alternate tag, throw-up, stencil, or character mark with the same stable gang identity.
6. Legend / Family - unlock a prestige mural/masterpiece variant. This must be visually richer, not just a recolor.

Rules:

- reward identity keys from stable `gang_id`, never display name;
- renaming a crew cannot invalidate its unlocked art;
- art unlock receipts are server-authoritative where progression is server-owned;
- client cosmetics remain user-selectable after unlock;
- unlocked graffiti uses the existing JetSetCraft graffiti placement/persistence/cleanup pipeline;
- provider removal makes provider-specific rewards dormant when their resources are unavailable, but preserves the unlock receipt so reinstall restores them;
- no duplicate reward farming after reconnect, restart, alias change, challenge replay, or provider reload;
- Junior crews may inherit parent art plus a deliberately distinct cute remix;
- boss/legendary crews may use encounter victory as an additional unlock condition;
- every curated gang gets a bespoke art direction tied to its creature/world. No repeated template, repeated crown motif, generic AI-looking badge, or palette-swap-only art.

## 3. Vanilla coverage reconciliation baseline

The canonical vanilla hub currently contains:

- 30 friendly/passive adult rows;
- 14 neutral/conditional adult rows;
- 31 hostile adult rows;
- 75 adult rows, representing 74 unique adult mobs because Spider appears in both contextual disposition sections;
- 7 boss/legendary/special rows, bringing the unique adult/special entity total to 79;
- 34 named Junior crews;
- 3 juvenile-equivalent profiles: Tadpole cross-link, Small Slime/Goo Goos, Small Magma Cube/Hot Tots.

This modded atlas extends that system. It does not replace or renumber the vanilla gangs.

## 4. The Aether - curated 1.20.1 wave

Source authority: https://github.com/The-Aether-Team/The-Aether/tree/1.20.1-develop
Provider mod ID: `aether`

The 1.20.1 source registers 20 primary passive/hostile/dungeon mobs plus Cloud Minion as a special summoned/misc affiliate. All are explicitly accounted for below.

| Entity ID | Canonical gang | Role / identity | Art direction |
|---|---|---|---|
| `aether:phyg` | **Sky Hogs** | friendly aerial pork trick crew | sky-blue aerosol, wing arcs, pig snout throw-up |
| `aether:flying_cow` | **High Steaks** | friendly high-altitude endurance crew | cloud-white block letters, brown speed slashes, milk-trail tags |
| `aether:sheepuff` | **Cloud Nine** | fluffy aerial formation crew | soft cloud wildstyle with sharp wind calligraphy, no crown motifs |
| `aether:moa` | **Sky Striders** | speed and mount-line specialists | long-leg motion strokes, feathered calligraphy, track-line tags |
| `aether:aerbunny` | **Cloud Hoppers** | aerial jump-chain crew | compact bubble letters, spring arcs, tiny wing marks |
| `aether:aerwhale` | **Blue Horizon** | huge peaceful sky-route crew | broad panoramic blue mural language, whale silhouette negative space |
| `aether:blue_swet` | **Blue Bounce** | blue Swet bounce crew | gel drips, rounded impact marks, cobalt throw-ups |
| `aether:golden_swet` | **Gold Bounce** | golden Swet bounce crew | metallic-gold aerosol illusion, elastic loops, amber splatter |
| `aether:whirlwind` | **Spin Cycle** | neutral spinning route-disruption crew | circular one-line tags, cyclone arrows, white/gray wind overspray |
| `aether:evil_whirlwind` | **Bad Draft** | hostile whirlwind crew | torn black/red strokes, reversed wind arrows, dirty aerosol haze |
| `aether:aechor_plant` | **Needlepoint** | rooted poison-control crew | thorn-calligraphy, acidic green/purple stencil forms |
| `aether:cockatrice` | **Stone Gaze** | hostile precision/glare crew | angular bird-eye mark, petrified gray texture, violet accents |
| `aether:zephyr` | **Crosswinds** | hostile sky-control crew | huge lateral wind lettering, snowball dots, storm-blue overspray |
| `aether:mimic` | **False Bottom** | dungeon ambush crew | chest-lid letterforms, fake depth shadows, gold-to-black reveal |
| `aether:sentry` | **Watchtower** | dungeon guard/control crew | geometric eye/sensor stencil, stone block letters, sentry rings |
| `aether:slider` | **Hard Slide** | dungeon heavy-slide crew | scraped stone typography, horizontal gouges, impact sparks |
| `aether:valkyrie` | **Winged Order** | technical aerial duel crew | elegant spear-line calligraphy and wing geometry, no regal crown motif |
| `aether:valkyrie_queen` | **First Flight** | legendary Valkyrie leader encounter | refined gold/white mural, long wing strokes, victory-only prestige tag |
| `aether:fire_minion` | **Ember Crew** | Sun Spirit affiliate | charred handstyle, ember dots, short flame strokes |
| `aether:sun_spirit` | **Daybreak** | legendary fire boss identity | radiant orange/yellow sunburst mural, heat-distorted lettering |
| `aether:cloud_minion` | **Cloud Hands** | special summoned affiliate, not ordinary progression | small cloud-hand stencil; dormant unless safe as an Atlas participant |

## 5. Twilight Forest - complete active 1.20.1 mob mapping

Source authority: https://github.com/TeamTwilight/twilightforest/tree/1.20.1
Provider mod ID: `twilightforest`

The 1.20.1 registry contains 59 active creature/monster registrations after excluding the commented-out Boggard declaration and non-Mob projectiles/misc entities. Every active creature/monster is mapped below. Related species may share a family crew, but each EntityType still receives its own stable mapping record and Atlas role.

| Entity ID | Canonical gang / family | Role |
|---|---|---|
| `twilightforest:adherent` | **Last Rites** | dark magic street cult crew |
| `twilightforest:alpha_yeti` | **Whiteout** | legendary Whiteout leader |
| `twilightforest:armored_giant` | **Iron Skyline** | armored giant heavy crew |
| `twilightforest:bighorn_sheep` | **Ram Jam** | colorful horn/formation crew |
| `twilightforest:blockchain_goblin` | **Chain Reaction** | chained goblin family |
| `twilightforest:boar` | **Bristle Beat** | forest charge/race crew |
| `twilightforest:carminite_broodling` | **Red Static** | tower brood affiliate |
| `twilightforest:carminite_ghastguard` | **Red Static** | tower aerial guard |
| `twilightforest:carminite_ghastling` | **Red Static** | junior aerial affiliate |
| `twilightforest:carminite_golem` | **Red Machinery** | tower heavy enforcer |
| `twilightforest:death_tome` | **Dead Letters** | flying book/spell crew |
| `twilightforest:deer` | **Velvet Lines** | graceful forest route crew |
| `twilightforest:dwarf_rabbit` | **Burrow Beat** | tiny hop crew |
| `twilightforest:fire_beetle` | **Hot Shells** | fire beetle battle crew |
| `twilightforest:giant_miner` | **Tall Order** | giant mining/traversal crew |
| `twilightforest:harbinger_cube` | **Black Box** | ominous cube-control crew |
| `twilightforest:hedge_spider` | **Webwork** | hedge web family |
| `twilightforest:helmet_crab` | **Shell Shock** | armored crab crew |
| `twilightforest:hostile_wolf` | **Moon Pack** | hostile wolf family |
| `twilightforest:hydra` | **Many Heads** | legendary Hydra encounter |
| `twilightforest:ice_crystal` | **Cold Core** | ice-core family |
| `twilightforest:king_spider` | **Webwork** | elite Webwork leader |
| `twilightforest:knight_phantom` | **Ghost Riders** | phantom knight crew |
| `twilightforest:kobold` | **Little Trouble** | frantic small-mob trick crew |
| `twilightforest:lich` | **Dead Royalty** | legendary spell duel leader |
| `twilightforest:lich_minion` | **Dead Royalty** | Lich affiliate |
| `twilightforest:lower_goblin_knight` | **Chain Reaction** | goblin knight lower half/member |
| `twilightforest:loyal_zombie` | **Grave Detail** | loyal undead guard crew |
| `twilightforest:maze_slime` | **Maze Melt** | labyrinth bounce crew |
| `twilightforest:minoshroom` | **Spore Bull** | elite maze mushroom brawler |
| `twilightforest:minotaur` | **Maze Breakers** | labyrinth charge crew |
| `twilightforest:mist_wolf` | **Moon Pack** | fog-line wolf affiliate |
| `twilightforest:mosquito_swarm` | **Blood Buzz** | swarm disruption crew |
| `twilightforest:naga` | **Coil Culture** | legendary serpentine route encounter |
| `twilightforest:penguin` | **Slide Society** | ice-slide friendly crew |
| `twilightforest:pinch_beetle` | **Pinch Point** | grapple/control beetle crew |
| `twilightforest:plateau_boss` | **Last Horizon** | technical/unfinished special; hidden unless safe/enabled |
| `twilightforest:quest_ram` | **Spectrum Run** | rare color-collection crew/legend |
| `twilightforest:raven` | **Black Signal** | aerial scout/rhythm crew |
| `twilightforest:redcap` | **Red Shift** | Redcap mining/ambush crew |
| `twilightforest:redcap_sapper` | **Fuse Caps** | explosive Redcap specialist |
| `twilightforest:rising_zombie` | **Grave Rising** | underground emergence crew |
| `twilightforest:roving_cube` | **Box Step** | roaming geometric crew |
| `twilightforest:skeleton_druid` | **Bone Garden** | nature-magic skeleton crew |
| `twilightforest:slime_beetle` | **Goo Shells** | slime beetle crew |
| `twilightforest:snow_guardian` | **White Court** | Snow Queen guard family |
| `twilightforest:snow_queen` | **White Court** | legendary White Court leader |
| `twilightforest:squirrel` | **Acorn Alley** | tiny woodland sprint crew |
| `twilightforest:stable_ice_core` | **Cold Core** | controlled core member |
| `twilightforest:swarm_spider` | **Webwork** | swarm Webwork member |
| `twilightforest:tiny_bird` | **Pocket Flight** | tiny aerial line crew |
| `twilightforest:towerwood_borer` | **Woodwork** | tower infestation crew |
| `twilightforest:troll` | **Stone Throwers** | cave heavy crew |
| `twilightforest:unstable_ice_core` | **Cold Core** | volatile core member |
| `twilightforest:upper_goblin_knight` | **Chain Reaction** | goblin knight upper member |
| `twilightforest:ur_ghast` | **Tower Wail** | legendary tower aerial boss identity |
| `twilightforest:winter_wolf` | **Moon Pack** | frozen wolf affiliate |
| `twilightforest:wraith` | **Fade Out** | spectral movement crew |
| `twilightforest:yeti` | **Whiteout** | Whiteout standard member |

Twilight art direction must reflect each family: forest handstyles, labyrinth geometry, Carminite industrial red, glacier aerosol, spectral dry-brush, etc. Do not collapse them into one generic fantasy logo template.

## 6. The Undergarden - complete 1.20 branch creature mapping

Source authority: https://github.com/quek04/undergarden/tree/1.20
Provider mod ID: `undergarden`

The source exposes 16 normal living mobs plus Forgotten Guardian as a boss, and two relevant special/misc actors (`boomgourd`, `minion`). All are accounted for below.

| Entity ID | Canonical gang / family | Role |
|---|---|---|
| `undergarden:rotling` | **Rot Riot** | small Rotspawn member |
| `undergarden:rotwalker` | **Rot Riot** | standard Rotspawn member |
| `undergarden:rotbeast` | **Rot Riot** | heavy Rotspawn member |
| `undergarden:dweller` | **Down Below** | riding/traversal-friendly deep crew |
| `undergarden:gwibling` | **Glow Current** | juvenile/small aquatic affiliate |
| `undergarden:brute` | **Heavy Roots** | heavy creature crew |
| `undergarden:scintling` | **Shimmerline** | luminous ambient crew |
| `undergarden:gloomper` | **Gloom Bounce** | springy dark-biome crew |
| `undergarden:stoneborn` | **Deep Mason** | Stoneborn society/technical crew |
| `undergarden:nargoyle` | **Night Carvers** | cavern aerial/stone crew |
| `undergarden:muncher` | **Mouth Off** | cavern bite/control crew |
| `undergarden:sploogie` | **Goo Mood** | cavern splatter crew |
| `undergarden:gwib` | **Glow Current** | aquatic parent crew |
| `undergarden:mog` | **Mog Fog** | creature crew |
| `undergarden:smog_mog` | **Smog Alley** | smoky Mog variant crew |
| `undergarden:forgotten` | **Forgotten Frequency** | forgotten undead/ancient crew |
| `undergarden:forgotten_guardian` | **Deep Memory** | legendary Forgotten leader encounter |
| `undergarden:minion` | **Forgotten Frequency** | special summoned affiliate; no ordinary roaming progression |
| `undergarden:boomgourd` | **Blast Bloom** | special event/prop identity only if runtime confirms safe Mob participation |

## 7. Advent of Ascension - dimension/faction curation wave

Source authority used for planning: https://github.com/Tslat/Advent-Of-Ascension/tree/1.20
Provider mod ID: `aoa3`
Exact Forge 1.20.1 release reality must be verified from the installed mod/JAR at runtime before enabling a record.

Important: the public `1.20` source line is a useful candidate superset, not proof that every listed ID exists in every 1.20.1 build. JetSetCraft must condition curated records on actual registered IDs. Missing IDs remain dormant; they are never treated as errors or proof of removal.

The source scan produced 358 candidate Mob/Animal/NPC registrations. AoA is too large for weak one-off pun generation. The first premium layer therefore gives each dimension/faction a strong umbrella crew and maps every detected member into it. Individual species can later receive subcrew names without changing the stable umbrella relationship.

### Dimension/faction crews

| AoA family | Premium umbrella crew | Candidate source IDs |
|---|---|---|
| Candyland | **Sugar Rush** | airhead, candy_corny, cane_bug, cherry_blaster, gingerbird, gingerbread_man, lollypopper |
| Vox Ponds | **Circuit Tide** | alarmo, centinel, destructor, fischer, gadgetoid, nightwing, slimer, toxxulous |
| L'Borean | **Blue Current** | amphibior, amphibiyte, angler, mermage, muncher, neptuno, sea_viper |
| Abyss | **Dead Air** | anemia, apparition, bloodsucker, distorter, fiend, flesh_eater, jawe, occulent, web_reaper |
| Shyrelands | **Shyre Style** | arc_flower, arc_wizard, arcbeast, arcworm, axiolight, lightwalker, luxocron, omnilight, shyre_knight, soulscorne, soulvyre, stimulo, stimulosus, sysker |
| Gardencia | **Green Scene** | archvine, blue_flower, broccohead, carrotop, corny, daysee, flowerface, green_flower, orange_flower, pod_plant, purple_flower, squasher, sunny, vine_wizard, yellow_flower |
| Runandor | **Rune Runners** | ariel, blue_rune_templar, blue_runic_lifeform, bouncer, green_rune_templar, green_runic_lifeform, paladin, red_rune_templar, red_runic_lifeform, runic_guardian, runicorn, runicorn_rider, spectral_wizard, yellow_rune_templar, yellow_runic_lifeform |
| Barathos | **Dust Crownless** | arkback, cryptid, echodar, emperor_beast, keeler, nospike, parasect, ramradon, squiggler, tharafly |
| Dustopia | **Dust Devils** | arkzyne, basilisk, crusilisk, devourer, dust_strider, dusteiva, duston, lost_soul, lurker, merkyre, stalker |
| Deeplands | **Bedrock Beat** | arocknid, case_construct, cave_creep, doubler, dweller, nipper, rock_crawler, rock_critter, rockbiter |
| Precasia | **Fossil Fuel** | attercopus, dunkleosteus, skeletal_abomination, smilodon, spinoledon, veloraptor |
| Greckon | **Static Shade** | banshee, faceless_floater, grillface, hunter, nightmare_spider, shifter, silencer, skull_creature, sugarface, undead_troll, valkyrie |
| Lunalus | **Moonwalkers** | baumba, explodot, fake_zorp, inmate_x, inmate_y, lunarcher, modulo, refluct, terrestrial, visular, visulon, zarg, zorp |
| Celeve | **Big Top Breakers** | bobo, chocko, happy, jumbo, koko, kranky, snappy, sticky, stitches, tipsy |
| Creeponia | **Fusebox Family** | bone_creeper, cave_creepoid, creeperlock, creepird, creepuple, host, king_creeper, magical_creeper, winged_creeper |
| Crystevia | **Seven Signals** | construct_of_flight, construct_of_mind, construct_of_range, construct_of_resistance, construct_of_speed, construct_of_strength, construct_of_terror |
| Nether | **Ash Runners** | embrake, flamewalker, infernal, little_bam, nethengeic_beast |
| Iromine | **Circuit Breakers** | enforcer, mechachron, mechamaton, mechyon, polytom, voltron |
| Lelyetia | **Lelyetian Line** | exohead, flye, grobbler, lelyetian_caster, lelyetian_warrior, paravite, rawbone, tracker, zhinx |
| Mysterium | **Sporecore** | fungat, fungback, fungik, fungock, fungung, mushroom_spider, runic_golem |
| Haven | **High Spirits** | angelica, dawnlight, rainicorn, spirit_guardian, spirit_protector |

### AoA overworld subfamilies

Do not force all AoA overworld creatures into one crew.

| Subfamily | Crew | Candidate IDs |
|---|---|---|
| Giants | **Tall Tales** | ice_giant, leafy_giant, sand_giant, stone_giant, wood_giant |
| Charger line | **Charge Line** | charger, king_charger |
| Forest/nature | **Root Runners** | bush_baby, chomper, tree_spirit |
| Void/ghost | **Null Street** | ghost, void_walker |
| Goblin | **Small Change** | goblin |
| Heavy/ancient | **Old Guard** | ancient_golem, cyclops, sasquatch, yeti |
| Explosive | **Payload** | bomb_carrier |

### AoA animal/ambient families

- **Prism Traps**: blue_gemtrap, green_gemtrap, purple_gemtrap, red_gemtrap, white_gemtrap, yellow_gemtrap.
- **Deep Frequency**: candlefish, charred_char, crimson_skipper, crimson_stripefish, dark_hatchetfish, jamfish, parapiranha, pearl_stripefish, rainbowfish, razorfish, reeftooth, rocketfish, sailback, sapphire_strider, skelecanth, turquoise_stripefish, violet_skipper.
- **Ancient Motion**: deinotherium, meganeuropsis, opteryx.
- **Open Range**: chocaw, coratee, creep_cow, eeo, halycon, horndron, hydrone, ironback, night_watcher, shik, shiny_squid, trotter, urka, voliant.
- **Mint Condition**: peppermint_snail, spearmint_snail.

### AoA NPC social crews

NPCs remain source-owned NPCs with their original trades/dialogue. JetSetCraft adds social/challenge identity only when safe.

- **Gorb Garage**: gorb_arms_dealer, gorb_citizen, gorb_engineer.
- **First Edition**: primordial_banker, primordial_guide, primordial_merchant, primordial_spellbinder, primordial_wizard.
- **Zal Market**: zal_banker, zal_child, zal_citizen, zal_grocer, zal_herbalist, zal_spellbinder, zal_vendor.
- **Shyre Style**: shyre_archer, shyre_banker.
- **Lelyetian Line**: lelyetian_banker, lelyetian_trader.
- **Fusebox Family**: creep_banker.
- **Road Scholars**: professor, skill_master, naturalist.
- **Night Market**: corrupted_traveller, crystal_trader, dungeon_keeper, explosives_expert, lottoman, metalloid, store_keeper, token_collector, toy_merchant, troll_trader, undead_herald, dryad_sprite.

### AoA bosses and technical entries

Bosses should normally inherit their dimension/faction crew and appear as legendary/champion profiles instead of becoming a disconnected faction for every boss. `elite_*` versions inherit the same identity with an elite encounter profile.

Candidate boss IDs include: bane, baroness, blue_guardian, clunkhead, corallus, cotton_candor, craexxeus, creep, crystocore, dracyon, elite_king_bambambam, elite_nethengeic_wither, elite_skeletron, elite_smash, elite_tyrosaur, elusive, flash, graw, green_guardian, gyro, harkos, hive_king, kajaros, king_bambambam, king_shroomus, klobber, kror, mechbot, mirage, miskel, nethengeic_wither, okazor, proshield, raxxan, red_guardian, rock_rider, shadowlord, skeletron, smash, tyrosaur, vinocorne, visualent, voxxulon, xxeus, yellow_guardian.

The public branch also contains candidate `unused` registrations: boneback, bugeye, fenix, ghastus, goalby, goldum, goldus, muckopede, night_reaper, nightfly, reaver, sea_troll, shade, shadow, shavo, skeledon, skelekyte, urioh, urv, visage. These must stay hidden/dormant unless the exact installed AoA build registers and safely enables them. Never expose technical/unused entities merely because source code contains a registration candidate.

Special candidates `doppelganger`, `elusive_clone`, and `hive_worker` require runtime ownership/safety classification before ordinary progression.

## 8. Wave 2 popular Forge 1.20.1 targets

The curated system should next pin exact 1.20.1 source/release rosters for these high-value creature ecosystems, then create the same premium one-to-one coverage ledger:

1. Alex's Mobs
2. Mowzie's Mobs
3. L_Ender's Cataclysm
4. Alex's Caves
5. Ice and Fire: Dragons
6. Blue Skies
7. Born in Chaos
8. Critters and Companions
9. Naturalist
10. Deeper and Darker
11. The Bumblezone
12. Aquamirae
13. Unusual Prehistory
14. Friends & Foes where the exact Forge 1.20.1 provider is present
15. other installed creature mods discovered by the runtime Atlas, prioritized by player usage and safe API availability

Do not call this list exhaustive forever. The Installed-Mod Atlas remains the safety net: every safe unknown Mob gets a compatibility record even before premium curation arrives.

## 9. Validation and completeness ledger

For each curated provider version/build, record:

- discovered safe Mob count;
- curated exact-ID count;
- generic fallback count;
- dormant/missing-provider count;
- intentionally hidden technical/boss-only count;
- unresolved count.

A provider is not "fully curated" while unresolved > 0 without a written safety reason.

Required tests:

1. Provider present: all safe detected mobs appear in the Mod Atlas and resolve to either curated or generic records.
2. Provider absent: JetSetCraft loads and preserved records remain dormant without hard imports/classloading crashes.
3. Provider upgrade/downgrade: removed IDs become dormant, new IDs appear generic until curated, unchanged IDs preserve progression.
4. Multiplayer: gang identity/reputation/rewards remain server-authoritative.
5. Graffiti progression: earn a gang tag, use it in the existing graffiti selector, place it, save/reload/reconnect, and verify the unlock remains exactly once.
6. Rename safety: canonical -> curated alternate -> custom alias -> restore canonical never changes the art unlock receipt or gang identity.
7. Resource failure: missing optional art fails gracefully with a useful Atlas state, never a crash or deleted reward.
8. Performance: registry enumeration occurs at lifecycle/cache invalidation points, not every tick/frame.
9. Boss safety: source boss ownership, arena logic, progression, and loot remain provider-owned.
10. NPC safety: normal trade/dialogue/use interactions remain provider-owned unless the explicit JetSetCraft Challenge modifier is active.

## 10. Art quality contract

Curated gang art should look like a real elite street artist designed it for that creature and world:

- deliberate handstyle and composition;
- strong readable silhouette at icon size;
- authentic spray/marker/stencil/wildstyle language where appropriate;
- distinct visual grammar per gang instead of one repeated logo template;
- creature/world motifs integrated into letter construction, negative space, throw-ups, stickers, characters, and mural composition;
- no generic crown motif as a default prestige shortcut;
- no photoreal creature pasted next to generated text;
- no glossy generic esports-logo look;
- no repeated AI-looking symmetry or meaningless decorative clutter;
- premium variants should be genuinely new compositions, not a recolor.

The gameplay implementation must treat these as normal authored resources with stable IDs and provenance, regardless of how the final art is produced.

<!-- JETSETCRAFT_WAVE2_EXACT_CURATED_2026-10-01 -->
## Wave 2A — exact mob-by-mob curation: Alex's Mobs + Alex's Caves + Cataclysm

**Status: curated data contract complete; runtime/provider-present verification is still required before calling the adapters runtime-verified.**

Wave 2A converts the first three high-value creature ecosystems from broad family planning into an exact, namespaced compatibility ledger. The canonical row-by-row roster lives in [JETSETCRAFT_WAVE_2_EXACT_MOD_COMPATIBILITY.md](JETSETCRAFT_WAVE_2_EXACT_MOD_COMPATIBILITY.md).

| Provider | Pinned 1.20.1 target | Exact safe curated mobs | Hard-hidden technical/helper entities | Compatibility stance |
|---|---:|---:|---:|---|
| Alex's Mobs (`alexsmobs`) | 1.22.9 | **90** | **7** | Exact curated records plus generic fail-open support for future safe mobs |
| Alex's Caves (`alexscaves`) | 2.0.2 | **43** | **2** | Exact curated records across all five cave ecosystems plus generic fail-open support |
| L_Ender's Cataclysm (`cataclysm`) | 3.31 | **39** candidate records | **0 living multipart records** | Boss/pet aware; 3.31 live-registry fingerprint is authoritative |
| **Wave 2A total** |  | **172** | **9** | No provider becomes a hard dependency |

### Required behavior for every Wave 2 record

- Keep the provider entity, AI, navigation, combat, ownership, variants, structures, loot, animation controller, and renderer provider-owned. JetSetCraft only layers reversible Street Gear, gang identity, challenge presentation, reputation, stingers, and graffiti rewards.
- Resolve providers by mod ID + namespaced entity ID, never by direct optional-provider class references in unconditional code.
- Enumerate/calculate compatibility at startup or data reload, cache the result, and keep the movement hot path free of registry scans or optional-provider reflection.
- Technical multipart/helper entities never receive independent gang state, rewards, Street Gear, drops, or challenge targeting; they resolve to the parent or are ignored.
- Tamed/owned mobs preserve owner UUID, sit/follow state, variant/inventory state, and provider commands across equip, challenge, chunk unload, restart, dimension transfer, and unequip.
- Cataclysm boss/arena state always wins. Boss-gated records may have Atlas identity and rewards, but JetSetCraft cannot seize movement/control during combat, invulnerability/cutscene phases, scripted arena logic, or death sequences.
- Anatomy is data, not an assumption: aquatic, flying, tiny, giant, serpentine, multi-leg, slime/contact-plane, and unusual mobs use an explicit Ground Contact / Ride Rig profile. If a safe visual rig does not exist, fall back to a non-invasive board/hover presentation or refuse that gear type instead of mutating the provider model.
- Unknown safe future `Mob` entity IDs remain playable through the universal generic Mob Atlas path. A provider roster drift is a maintenance signal, never a crash or reason to disable the whole provider.

### Wave 2 graffiti/reward quality bar

Every curated mob record has a stable `gang_id`, a provider-scoped `crew_family_id`, a distinct display identity, an explicit ride/contact profile, a challenge/safety profile, and a bespoke graffiti motif. Each record must resolve a three-step **Mark → Throwie → Masterpiece** reward chain. Shared family art direction is allowed; recolor-only “unique” art is not.

### Runtime acceptance gate

Wave 2A is not considered runtime-verified until the pinned releases pass provider-present and provider-absent boots, all **172** safe IDs resolve exactly once, all **9** technical/helper IDs remain hidden, unknown safe injected mobs fall back generically, roster drift fails open, owner/boss state survives untouched, multiplayer persistence works after restart, representative anatomy rigs render safely, and no per-tick registry enumeration is introduced.
