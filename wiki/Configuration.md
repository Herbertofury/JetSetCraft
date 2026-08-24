# Configuration

JetSetCraft uses separate Forge server/common and client specifications. Exact filenames follow the normal Forge config location for the installed instance/server.

## Server/common options

### Movement

| Key | Default | Purpose |
| --- | ---: | --- |
| `speedScale` | 1.0 | Global ride speed multiplier |
| `boostDrainPerTick` | 0.80 | Boost consumed while boosting |
| `boostRechargePerTick` | 0.18 | Ground recharge outside grinding |
| `grindSnapRadius` | 0.62 | Horizontal candidate snap radius |
| `grindVerticalTolerance` | 0.76 | Vertical candidate tolerance |
| `allowEdgeGrinding` | true | Collision-shape ledge grinding |
| `allowRailGrinding` | true | Vanilla/modded/Create rail paths |
| `allowRailTricks` | true | Tricks, hops, and transfers on rails |
| `allowWallRides` | true | Wall traversal |

### Style Flow

| Key | Default | Purpose |
| --- | ---: | --- |
| `allowGroundStunts` | true | Contextual breakdance power moves |
| `allowBoostTricks` | true | Alt+R high-value boost tricks |
| `allowDancing` | true | No-gear synchronized dance system |
| `enableCyphers` | true | Nearby dancer group bonuses |
| `cypherRadius` | 8.0 | Dancer detection radius in blocks |
| `styleBoostScale` | 1.0 | Boost earned from style actions |

### Vanilla world physics

| Key | Default | Purpose |
| --- | ---: | --- |
| `enableVanillaWorldPhysics` | true | Master surface/effect/enchantment composition |
| `blueIceSpeedMultiplier` | 2.15 | Extreme blue-ice route multiplier |
| `slimeBounceMultiplier` | 0.92 | Preserved landing impact on slime |
| `poweredRailBoostPerTick` | 0.030 | Momentum gained on powered rails |
| `unpoweredRailRetention` | 0.90 | Momentum retained on unpowered powered rails |
| `enableMicroTerrainAssist` | true | Safe slab/stair/snow continuity |
| `microTerrainMaxStep` | 0.625 | Maximum collision-verified rise |

### Graffiti and gangs

| Key | Default | Purpose |
| --- | ---: | --- |
| `allowCombatWhileRiding` | true | Preserve ordinary/third-party combat input while riding |
| `allowGraffiti` | true | Permit new graffiti placement |
| `maxGraffitiPerChunk` | 128 | Persistent decal limit per chunk; repainting does not consume another slot |
| `boomboxMaxActors` | 7 | Maximum event-only actors from one Boombox |
| `boomboxChallengeLifetimeTicks` | 3600 | Hard event-cast cleanup lifetime (three minutes) |
| `boomboxSpawnRadius` | 12.0 | Loaded-terrain search radius for safe entrances |

## Client options

| Key | Default | Purpose |
| --- | ---: | --- |
| `dynamicCamera` | true | Camera lean on grind, wall ride, powerslide |
| `dynamicFov` | true | Speed-sensitive FOV extension |
| `cameraRollScale` | 1.0 | Camera-roll intensity |
| `maxExtraFov` | 7.0 | Maximum normal speed FOV bonus |
| `boostExtraFov` | 4.0 | Additional boost FOV |
| `reducedMotion` | false | Disable roll/FOV pulses and rapid equipment rotations |
| `showStyleHud` | true | Show Boost/Flow/combo/rank panel |
| `showTrickNames` | true | Show trick, dance, and landing callouts |
