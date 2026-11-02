# Your custom Rival Rush arena trio

This is an editable **Minecraft 1.20.1 data pack for JetSetCraft 0.4.0-alpha.5 (introduced in alpha.3)**. It adds **Crossfade Yard**, **Collector's Loop** and **Chorus Terrace** to the server's arena catalogue. It requires no optional dimension or magic mod.

## Install and select

1. Copy this whole `arena-data-pack` folder into the world's `datapacks` directory. `pack.mcmeta` must sit directly inside the copied folder. For a ZIP, put `pack.mcmeta` and `data/` at the archive root.
2. Reload data packs with `/reload`, or restart the world/server. Operators can inspect `/datapack list` to confirm it is enabled.
3. Open `/jetsetcraft rush`, select **Arena circuit**, then use the starting-arena selector to choose **Crossfade Yard**. Start the six-arena journey. That first profile is deliberate; subsequent route forks use the server catalogue.

A run keeps the arena definitions it started with. Leave and begin another run after editing/reloading to use the updated definitions. This pack adds three arenas; it does not replace existing arenas, make a dimension, or construct anything in ordinary Minecraft.

## Three different reasons to take a route

| Arena | Geometry and objective | Reward for this run | Shared crew emphasis |
|---|---|---|---|
| Crossfade Yard | Radius 28 crossing, 150 seconds and 10 defeats | One critical chance and critical power rank | Reputation |
| Collector's Loop | Radius 24 loop, 120 seconds and 8 defeats | Pickup reach plus a temporary owned-card magnet | Supplies |
| Chorus Terrace | Radius 32 terraces, 180 seconds and 14 defeats | Two ability-area ranks | Member training |

All three join the existing route pool; installing this pack does not force a fixed three-room order. Choose the starting profile deliberately, then compare the actual offered forks. Routes prioritize unvisited profiles and keep different reward families available when the installed catalogue allows it. The six colored cache patterns inside each arena provide another deliberate choice: orange damage, magenta critical power, cyan area, purple artifact, yellow magnet or lime recovery. The cache list includes names as well as colors.

## Make it yours

Edit `data/jetsetcraft_examples/rush_arenas/crossfade_yard.json`:

| Field | Meaning | Accepted values |
|---|---|---|
| `name`, `description` | Route preview text | Short readable text |
| `radius` | Half-width of the room | 16–48 blocks |
| `seconds` | Active combat time needed | 30–360 |
| `defeats` | Owned rival defeats needed as well as time | 3–80 |
| `layout` | Starting street geometry | `plaza`, `cross`, `loop`, `terraces` |
| `reward` | Temporary run reward | `damage`, `critical`, `area`, `relic`, `magnet`, `recovery` |
| `reward_ranks` | Reward strength within the allowed range | 1–2 |
| `shared_reward` | Persistent crew reward emphasis | `balanced`, `supplies`, `training`, `reputation` |
| `palette` | Floor, accent, wall and rail materials | Block ID, or ordered fallback array of up to eight IDs |
| `blocks` | Extra authored pieces | Up to 4,096 relative block positions |

The example has four two-block corner markers and clear crossing lanes. Authored blocks use offsets from the room's floor center: `y=1..8`, inside the radius minus four, with both `abs(x)>4` and `abs(z)>4`. The incoming skating connection stays clear even when it passes through authored pieces. Block-state options can use a valid `properties` object. Entity data, inventories, block-entity NBT, fluids, portals and explicitly forbidden dangerous blocks are rejected.

You can add more JSON files using distinct filenames. The server loads at most 32 profiles in total, including the supplied arenas. Provider-specific packs can add `requires_mod` and fallback palette IDs; see the [full arena format](https://github.com/Herbertofury/JetSetCraft/wiki/Rival-Rush#custom-arena-data-packs) and [compatibility scope](https://github.com/Herbertofury/JetSetCraft/wiki/Compatibility).

The example deliberately separates its two rewards: **critical chance/power for this run**, and **reputation-focused shared crew progress**. Change those fields to build a different reason to visit your room. Numbers are starting tuning, not a claim of measured balance.
