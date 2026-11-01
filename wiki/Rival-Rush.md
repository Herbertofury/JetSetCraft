# Rival Rush — optional skating roguelite

**Status: optional `0.4.0-alpha.2` implementation with recorded machine verification.** The stable `v0.3.0` release is unchanged and does not include Rival Rush. First introduced through [merged PR #39](https://github.com/Herbertofury/JetSetCraft/pull/39). Rival Rush is one optional mode inside JetSetCraft; normal movement, graffiti, crews, Boombox challenges and Minecraft continue independently. This page describes the implemented alpha and its limits. The [18-game research and design study](https://github.com/Herbertofury/JetSetCraft/blob/main/docs/RIVAL_RUSH_DESIGN.md) separates further proposals from current code.

## Actual client captures

These images come from the passing alpha.2 real-client audit. The album capture remains the fresh audit player's genuine empty state. [Open all eight Rush/crew-house captures and their verification receipt](https://github.com/Herbertofury/JetSetCraft/tree/main/docs/verification/crew-house). The [alpha.1 captures and receipt](https://github.com/Herbertofury/JetSetCraft/tree/main/docs/verification/rival-rush) remain historical evidence.

| Choose a crew and session | Separate district and run HUD |
|---|---|
| ![Rival Rush setup](https://raw.githubusercontent.com/Herbertofury/JetSetCraft/main/docs/verification/crew-house/01-setup.png) | ![Rival Rush district](https://raw.githubusercontent.com/Herbertofury/JetSetCraft/main/docs/verification/crew-house/02-district-hud.png) |

## Start and return

Craft a **Rival Rush Flyer** from **paper + cyan dye**, then use it to open the menu. Reading the flyer does not start a session or consume it. Alternatively, enter `/jetsetcraft rush`, or bind **Rival Rush menu** in Minecraft's Controls settings; that key is unbound by default to avoid taking over another mod's control.

This alpha requires **Easy, Normal or Hard** difficulty. Peaceful removes the native hostile mobs used by the crews, so entry explains the limitation before moving the player or lending equipment. Rival Rush never changes the world's difficulty.

Choose a named rival crew using **Change crew**, review its creature theme, then choose **30-minute run** or **Endless**. Ordinary setup uses readable crew and creature names. **Advanced: custom crew or mob** lets experienced players enter registry IDs and select a specific installed creature instead of the crew's themed cast; **Apply selection** returns to setup without starting a run. The normal run counts active simulation time; choosing upgrades pauses your own run and its rivals, not the server. After thirty active minutes, choose **Return to Minecraft** or **Encore** to continue the same build indefinitely.

**Return to Minecraft** is always available in the run menu. `/jetsetcraft rush leave` is an independent exit. You return to your original dimension and position with the saved normal inventory, physical ride, Minecraft XP, survival state and effects restored. If the original dimension is no longer available, recovery uses the overworld spawn and explains the fallback. Invalid return journals are retained for recovery rather than silently discarded.

Collected cards update the separate album when picked up. Uncollected field cards and temporary run upgrades do not accompany the player into Minecraft. Disconnecting ends the run and restores the saved state; an interrupted-session journal is recovered on login.

### Return and persistence scope

The return journal covers the captured **vanilla player state and JetSetCraft ride state**. It does not snapshot arbitrary capabilities owned by other mods.

Album changes are persisted with **normal player saves**, not a disk flush for every card. An abrupt crash can lose changes since the last completed save. Successful entry and restored exit require their player-save checkpoints to be acknowledged.

If the entry checkpoint cannot be saved, the run stops before lending equipment or teleporting. If the restored exit cannot be saved, the player is disconnected with an explanation; reconnect after the server's storage problem is fixed.

## The first playable loop

1. Enter a separate generated street district with a temporary loadout.
2. Skate around crew-themed rivals. Automatic paint shots let movement remain the main task; manually aim and throw the loan paint balloons for deliberate splash attacks.
3. Ride over the paper mob-card pickups. They add to the album and grant **Rush XP** and mode score.
4. Choose one of three upgrades when the Rush level rises.
5. Tag fresh wall patches, use rails and tricks, and seek music stops for extra rewards.
6. Survive crew captains and changing five-minute encounter mixes. Bank the session whenever you want.

The loan inventory supplies paint balloons, a spray can, and all six existing ride styles. The equipped physical ride is preserved across the return snapshot. Run paint is supplied by the session; it is not a way to manufacture normal-world items.

## Three separate rewards

| Reward | Earned from | What happens after returning |
|---|---|---|
| **Rush XP** | Collected cards, eligible new graffiti patches, movement style, music stops and captains | Temporary level/build ends; vanilla XP is untouched |
| **Run score** | Rival defeats, collection mastery, walls, style, cyphers and terminal draft rewards | Shown in the result summary; never converted into vanilla currency |
| **Mob-card album** | Actually collected mode-owned card pickups | Persists by original mob registry ID and crew ID |

The run has its own **vitality**, separate from Minecraft hearts. Only the owning session's director resolves its rival attacks. Another player's run, native mob damage and unrelated projectiles cannot spend that vitality. Falling outside the playable district returns the player safely.

## Cards and increasing rewards

Every eligible rival defeat creates a collectible card pickup. Nearby cards of the same species can combine visually, retaining the exact count. Uncollected cards expire after five minutes of active run time or when left more than 160 blocks behind; upgrade and Encore decisions do not consume that lifetime. The director claims a defeat once; a discarded or unrelated world mob grants no card.

Duplicate collection reaches mastery milestones at **1, 5, 15, 40, 100, 250 and 600 cards** for that mob-and-crew identity. Higher milestones grant increasing one-time Rush XP and score bonuses; per-card rewards remain bounded. At **20, 60 and 150 total album cards**, future sessions begin with **one, two and three earned draft choices**, respectively. These advantages exist only inside Rival Rush.

The album shows the creature identity, crew, count, tier and next threshold. It uses pages rather than truncating the collection. Thresholds and combat numbers are initial tuning, not measured proof of a balanced thirty-minute session.

## Optional crew-house connection

A [player-built crew house](Player-Chapters-Posse-and-Mob-Archetypes) can prepare **one selected starting rank** when a run begins from its valid loaded plot. The album's earned starting drafts remain separate. Qualifying run results can also bank bounded Crew Supplies, crew XP, gang reputation and training for a selected idle roster member. House dispatch jobs provide a non-Rush progression route, and Rush remains playable without a house. These are persistent profiles; physical companions do not enter the run.

## Build choices

| Upgrade | Current effect |
|---|---|
| **Paint pressure** | Increased tag damage |
| **Quick hands** | Faster automatic firing, with a bounded minimum interval |
| **Wide spray** | Larger splash reach |
| **Magnetic mixtape** | Larger card collection radius |
| **Second wind** | More maximum run vitality and immediate recovery |
| **Easy breathing** | Recovery after avoiding damage |
| **Style dividends** | Stronger mode rewards for eligible walls and movement |

Stat ranks have defined ceilings. Once capped, drafts remain useful through **Fresh wheels** healing, **Victory lap** score, and temporary **Overdrive** damage. Choices validate the player, current run UUID, offer version and offered index. Replaying an old request cannot buy an upgrade in a new run.

## Optional crew-house preparation and training

The [player-built crew-house layer](Player-Chapters-Posse-and-Mob-Archetypes) can now support Rush without becoming a prerequisite. Register physical workshop, practice, studio or recovery facilities at home and choose **one** starting rank. Enter from inside your valid, loaded house plot to receive it; higher facility tiers do not stack more starting ranks. Album-earned draft choices remain separate.

A settled run with at least one minute of active time and three genuine rival defeats can earn virtual Crew Supplies, crew XP, reputation with that rival gang and training for one selected idle roster member. Those rewards never become vanilla items or XP. Dispatch missions provide a separate route to house progress while playing ordinary Minecraft. Crew members are currently persistent roster profiles, not physical followers.

Optional local-pack soundtrack playback now supports the home and Rush, with credits and fade-in/out. The two identified Sonic-related recordings have metadata entries; their audio files are not bundled yet. [Music integration and importer](https://github.com/Herbertofury/JetSetCraft/blob/main/docs/MUSIC_INTEGRATION.md) documents the exact recording versions and controls. This adds contextual playback, not beat-scored combat.

## Crews and enemy variety

Canonical gang names, colors and music identities come from the existing gang registry. Nine initial themed roster files provide multi-species casts where appropriate:

| Crew | Initial cast |
|---|---|
| Dead Beat | Zombie, husk, drowned, zombie villager |
| The Bone Drones | Skeleton, stray, wither skeleton |
| Arachnaphobia | Spider, cave spider, silverfish, endermite |
| Hex Appeal | Witch, evoker, vindicator, pillager |
| Goo Groove | Slime, magma cube |
| The Milk Run | Cow, mooshroom, goat, sheep |
| Hive Five | Bee, allay, bat, parrot |
| Creepaku Gouji | Creeper |
| Current Affairs | Guardian, drowned, squid, glow squid |

Other registered crews use their representative mob. Choosing an explicit different mob ID in **Advanced** uses that species. Installed modded AI mobs can be selected there by registry ID without a hard dependency on their mods. Creation, Street Gear admission, loaded terrain and clearance must succeed; this is not a claim that every third-party AI has been runtime-tested.

The existing Street Gear incompatibility tag currently excludes the **Ender Dragon and Wither** alongside non-mob/player entries. These scripted bosses need dedicated presentation/lifecycle adapters before they can safely join this mode. Their existing gang identities remain in the registry; selecting an excluded actor produces an explanation.

Rivals are freshly created from their real registered entity type and equipped through Street Gear. Natural residents are never recruited, replaced or stripped of AI state. Run-specific roles add skaters, ranged taggers, bruisers and captains. Tagger attacks mark a position, then resolve **0.8 seconds later**, allowing an actual dodge.

## Endless streets and objectives

The dedicated `jetsetcraft:rival_rush` dimension uses a flat generated street base. Small district pieces are decorated in already-loaded chunks: open avenues, accent lanes, graffiti walls, rails, a small ramp and lit Boombox stops. Decoration is spread across ticks, and no gang chunk is force-loaded. The normal Minecraft world is not rebuilt into an arena.

- **Fresh walls:** successful graffiti earns mode rewards. A four-block wall patch has a five-minute repeat cooldown, so repainting it continuously is not a scoring strategy.
- **Style:** moving grinds, wall rides and tricks can earn periodic Rush XP and score.
- **Music stops:** dance near a lit district Boombox for four seconds. A completed stop rewards XP, score and vitality, and plays the crew's existing entrance stinger through the Records sound category. A stop becomes eligible again after ten active minutes.
- **Captains:** stronger encounters reward additional Rush XP and recovery. Only one captain can be active in a run; an undefeated captain never creates a growing backlog of bosses in Endless.

Five-minute mixes rotate ordinary composition, tagger-heavy encounters, bruiser-heavy encounters, a recovery-oriented cypher mix, quicker telegraphed crossfire, and a relay mix. Enemy speed, health scaling and simultaneous pressure remain bounded; Endless continues through these mixes with the current build and album rewards. This first district is a reusable street grammar, not a completed hand-authored city campaign.

## Server and datapack controls

The server config exposes `[rivalRush] enabled = true`. Setting it false prevents entry and returns active participants. It does not disable ordinary JetSetCraft play.

Add a roster at `data/<namespace>/jetsetcraft_rush_rosters/<name>.json`:

```json
{
  "gang": "jetsetcraft:dead_beat",
  "entities": ["minecraft:zombie", "minecraft:husk", "minecraft:drowned"]
}
```

The mode filters absent entity IDs without requiring optional mods. A completely unavailable cast cannot start. Use the existing gang data system to define or map additional named crews. The client HUD has independent drag, scale, visibility and reset controls, persisted locally; those preferences cannot award gameplay state.

## Last recorded verification

The recorded **alpha.2** verification covers candidate [`d5ecadd`](https://github.com/Herbertofury/JetSetCraft/commit/d5ecadd0651c35fdffb505fa2790bda718e830db) on tested PR integration [`b9bcec8`](https://github.com/Herbertofury/JetSetCraft/commit/b9bcec8258e164aa41af5838a89df8aac04bd7c7). The [crew-house verification receipt](https://github.com/Herbertofury/JetSetCraft/tree/main/docs/verification/crew-house) records the exact tested source, binary checksum, workflow artifacts and eight captures. Later code changes need their own evidence.

| Check | Evidence and result |
|---|---|
| Main Forge/client gate | **[PASS — Verify JetSetCraft #274](https://github.com/Herbertofury/JetSetCraft/actions/runs/41350812751):** clean Forge build, **19 real Forge GameTests**, dedicated-server readiness, normal RCON shutdown, and an actual **1280 × 720 graphical client audit with eight captures**. |
| Optional-provider configurations | **[PASS — all four matrix configurations](https://github.com/Herbertofury/JetSetCraft/actions/runs/41350812645):** combined providers, Alex's Mobs, Alex's Caves, and Cataclysm each passed the same 19 GameTests and server smoke. |

The graphical audit captured setup, the district HUD, an **empty** album, the return result, crew home, roster, facilities and jobs. Its Rush fixture entered and left through the real packet flow with **7 diamonds, 5 bread, 23 XP levels and 17 XP points**. It compared the complete saved inventory and XP level/total, accepted an XP-progress difference of at most `0.000001`, and checked return to the original dimension and within one block of the saved position. The subsequent crew audit used the actual Charter, founded a home, registered an operational workshop and dispatched a still-running timed job.

The nineteen GameTests comprise nine original tests, six Rival Rush tests and four crew-house/integration tests. [Testing and Verification](Testing-and-Verification) records their scopes and the distinction between deterministic deadline checks and the short graphical audit. These are **machine integration smoke checks**. They do not establish thirty-minute enjoyment, Endless or house-economy balance, every modded mob's AI behavior, or every multiplayer interruption case. **No human thirty-minute or Endless playtest is claimed.** Stable `v0.3.0` retains its separate nine-test evidence.

### Historical alpha.1 checkpoint

The [alpha.1 verification receipt](https://github.com/Herbertofury/JetSetCraft/tree/main/docs/verification/rival-rush) preserves the fifteen-test checkpoint containing feature `88b2c75`, tested through PR integration `f2ac0c1`, with its original four captures, CI links and binary checksum. That historical evidence remains distinct from the current alpha.2 checks.

## What remains a design proposal

The implemented seven ranked upgrades and three fallback rewards form the first build system. The eight deeper families in the research study—such as tag chaining, rail-triggered pulses and painted drift trails—still need implementation and playtesting.

Captains currently add tougher priority targets and rewards. Bespoke boss attack patterns, paired bosses, crew-specific final headliners, and the proposed authored thirty-minute sequence remain future work. The current end is the timer boundary with a Return/Encore choice.

Music stops currently measure **four seconds of dancing** and play an existing crew stinger. They do not judge beats, analyze a soundtrack, or implement rhythm-scored combat. Tag relays, carried crates, crew murals, richer procedural neighborhoods, and coordinated co-op are also proposals. Human sessions should now test the existing skating, combat, collection and exit loop before expanding it.

**Continue:** [Controls](Controls) · [Graffiti](Graffiti) · [Mob Gangs](Mob-Gangs) · [Research and design](https://github.com/Herbertofury/JetSetCraft/blob/main/docs/RIVAL_RUSH_DESIGN.md)
