# Rival Rush — optional skating roguelite

**Status: first implementation under verification.** Rival Rush is one optional mode inside JetSetCraft. The normal movement, graffiti, crews, Boombox challenges and Minecraft world continue to work independently. This page describes the implemented first slice and its current limits; the [research and longer design](https://github.com/Herbertofury/JetSetCraft/blob/main/docs/RIVAL_RUSH_DESIGN.md) separates further proposals from current code.

## Start and return

Craft a **Rival Rush Flyer** from **paper + cyan dye**, then use it to open the menu. Reading the flyer does not start a session or consume it. Alternatively, enter `/jetsetcraft rush`, or bind **Rival Rush menu** in Minecraft's Controls settings; that key is unbound by default to avoid taking over another mod's control.

Choose a named crew and a mob identity, then choose **30-minute run** or **Endless**. The normal run counts active simulation time; choosing upgrades pauses your own run and its rivals, not the server. After thirty active minutes, choose **Return to Minecraft** or **Encore** to continue the same build indefinitely.

**Return to Minecraft** is always available in the run menu. `/jetsetcraft rush leave` is an independent exit. You return to your original dimension and position with the saved normal inventory, physical ride, Minecraft XP, survival state and effects restored. If the original dimension is no longer available, recovery uses the overworld spawn and explains the fallback. Invalid return journals are retained for recovery rather than silently discarded.

Collected cards bank into the separate album immediately when picked up. Uncollected field cards and temporary run upgrades do not accompany the player into Minecraft. Disconnecting ends the run and restores the saved state; an interrupted-session journal is recovered on login.

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
| **Rush XP** | Collected cards, eligible new graffiti patches, movement challenges, music stops and captains | Temporary level/build ends; vanilla XP is untouched |
| **Run score** | Rival defeats, collection mastery, walls, style, cyphers and terminal draft rewards | Shown in the result summary; never converted into vanilla currency |
| **Mob-card album** | Actually collected mode-owned card pickups | Persists by original mob registry ID and crew ID |

The run has its own **vitality**, separate from Minecraft hearts. Only the owning session's director resolves its rival attacks. Another player's run, native mob damage and unrelated projectiles cannot spend that vitality. Falling outside the playable district returns the player safely.

## Cards and increasing rewards

Every eligible rival defeat creates a collectible card pickup. Nearby cards of the same species can combine visually, retaining the exact count. The director claims a defeat once; a discarded or unrelated world mob grants no card.

Duplicate collection reaches mastery milestones at **1, 5, 15, 40, 100, 250 and 600 cards** for that mob-and-crew identity. Higher milestones grant increasing one-time Rush XP and score bonuses; per-card rewards remain bounded. At **20, 60 and 150 total album cards**, future sessions begin with **one, two and three earned draft choices**, respectively. These advantages exist only inside Rival Rush.

The album shows the creature identity, crew, count, tier and next threshold. It uses pages rather than truncating the collection. Thresholds and combat numbers are initial tuning, not measured proof of a balanced thirty-minute session.

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

Other registered crews use their representative mob. Choosing an explicit different mob ID uses that species. Installed modded AI mobs can be selected by registry ID without a hard dependency on their mods. Creation, Street Gear admission, loaded terrain and clearance must succeed; this is not a claim that every third-party AI has been runtime-tested.

The existing Street Gear incompatibility tag currently excludes the **Ender Dragon and Wither** alongside non-mob/player entries. These scripted bosses need dedicated presentation/lifecycle adapters before they can safely join this mode. Their existing gang identities remain in the registry; selecting an excluded actor produces an explanation.

Rivals are freshly created from their real registered entity type and equipped through Street Gear. Natural residents are never recruited, replaced or stripped of AI state. Run-specific roles add skaters, ranged taggers, bruisers and captains. Tagger attacks mark a position, then resolve **0.8 seconds later**, allowing an actual dodge.

## Endless streets and objectives

The dedicated `jetsetcraft:rival_rush` dimension uses a flat generated street base. Small district pieces are decorated in already-loaded chunks: open avenues, accent lanes, graffiti walls, rails, a small ramp and lit Boombox stops. Decoration is spread across ticks, and no gang chunk is force-loaded. The normal Minecraft world is not rebuilt into an arena.

- **Fresh walls:** successful graffiti earns mode rewards. A four-block wall patch has a five-minute repeat cooldown, so repainting it continuously is not a scoring strategy.
- **Style:** moving grinds, wall rides and tricks can earn periodic Rush XP and score.
- **Music stops:** dance near a lit district Boombox for four seconds. A completed stop rewards XP, score and vitality, and plays the crew's existing entrance stinger through the Records sound category. A stop becomes eligible again after ten active minutes.
- **Captains:** stronger encounters reward additional Rush XP and recovery.

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

## Verification and next experiments

Six new Forge GameTests exercise run rules, album rewards, return journals, actual session entry/exit, actor/card ownership, and real graffiti/action dispatch. They join the nine existing tests; the CI gate requires **all fifteen**, a clean Forge build and dedicated-server readiness. Check the exact commit's workflow before describing a candidate as verified.

The larger research document covers additional weapon families, encounter directors, musical objectives, co-op, routes, world-space card presentation and more expressive city structure. Those remain experiments until implemented and tested. Client visual review and real thirty-minute playtesting remain essential for pacing, readability, terrain feel and mob-provider compatibility.

**Continue:** [Controls](Controls) · [Graffiti](Graffiti) · [Mob Gangs](Mob-Gangs) · [Research and design](https://github.com/Herbertofury/JetSetCraft/blob/main/docs/RIVAL_RUSH_DESIGN.md)
