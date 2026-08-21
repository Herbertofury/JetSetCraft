# JetSetCraft

> **Official visual identity:** the project-owner-supplied JetSetCraft artwork is the canonical icon and cover source. The verified full-quality PNG is preserved in the JetSetCraft Drive Assets folder at **1,254 × 1,254**, **1,702,879 bytes**, SHA-256 `6e60bcc90fd6b85209598b088b9d2b87fb6c3a36d9d68c6eb4e478f538a5894f`. [Open the canonical cover](https://drive.google.com/file/d/1km1kaSF6vqR5wBrMQr8J1FHCAFy8vp4W/view). `src/main/resources/jetsetcraft.png` is the deterministic runtime icon; `assets/jetsetcraft-cover.webp` remains a legacy repository thumbnail.

**JetSetCraft** is a Forge 1.20.1 street-movement, trick, dance, graffiti, and combat-composition mod. It makes ordinary Minecraft terrain feel like a full momentum sports game without putting the player inside a disconnected vehicle subsystem. Villages, rooftops, fences, walls, rails, Create tracks, ice highways, redstone builds, modded dimensions, pistons, slime launchers, fluids, mobs, and weapons all remain part of the same play space.

## Style Flow 0.2.0-alpha.1

The Style Flow release expands the verified hoverboard foundation into a complete playable style system:

- **Six ride styles:** inline skates, quad skates, skateboard, BMX, hoverboard, and street scooter. Each has distinct acceleration, steering, air control, grind multiplier, cruise cap, and boost cap.
- **Twenty-four named tricks:** eight aerial, eight grind, and eight ground-stunt slots with ride-specific names and presentation.
- **Twenty-eight named dance moves:** Toprock, Popping, House, Breaking, Hip-Hop, and Locking families, including Six-Step, Windmill, Flare, Swipe, Halo, Headspin, Airflare, Running Man, Tutting, and more.
- **Style Flow scoring:** combo multiplier, Flow meter, rank ladder, repeat penalties, variety rewards, boost tricks, graded landings, manuals, powerslides, and automatic dance chains.
- **Multiplayer cyphers:** nearby dancers contribute crew size, score, Flow, boost recovery, and synchronized street-session energy.
- **World-native grinding:** arbitrary exposed block edges, fences, walls, panes, vanilla and Forge rails, and optional Create 6.0.8 tracks including junctions, slopes, diagonals, and long Bezier curves.
- **Vanilla physics as movement tech:** powered rails boost, unpowered powered rails brake, detector rails pulse redstone, activator rails pop, ice accelerates, slime bounces, honey drags, Soul Speed creates specialist routes, and external impulses become legitimate momentum.
- **Hands-free ride loadout:** ride equipment persists in its own server-authoritative player slot so both hands and the vanilla boot slot remain available.
- **Combat composition:** riding never silently disables normal item use or third-party weapon input. Lower-body ride clips leave arms, hands, held items, and head free for TACZ, Epic Fight, Better Combat, spellbooks, bows, and vanilla actions.
- **High-detail generated equipment:** dedicated hoverboard and scooter geometry, rather than a hoverboard reusing the skateboard model.
- **Sixty-eight validated animation clips:** 28 individually addressable dance phrases, eight full-body ground stunts, 16 trick/grind variants, six ride/boost families, and core movement states.
- **Accessibility controls:** optional Style HUD, trick names, dynamic camera, speed FOV, and a reduced-motion mode that removes camera pulses and rapid equipment rotations.

## Controls

All inputs are normal Forge key mappings and can be rebound in Minecraft Controls.

| Input | Action |
| --- | --- |
| Right-click ride gear | Equip or activate that ride style |
| `K` | Toggle equipped ride gear |
| `Shift + K` | Return equipped ride gear to inventory |
| `Left Alt` | Boost; combine with `R` for a boost trick |
| `R` | Contextual air, grind, or low-speed ground trick |
| `G` | Grind or wall ride |
| `C` | Manual; also selects Hip-Hop while starting a dance |
| `V` | Brake or powerslide; also selects Breaking while starting a dance |
| `Space` while grinding | Rail hop or transfer while preserving momentum |
| `B` | Start or change dance family/move |
| `Shift + B` | Stop dancing |
| `W/S/A/D + B` | Select Toprock, House, Locking, or Popping |
| Shift + use spray can | Cycle graffiti |
| Use spray can on a wall | Spray the selected tag |

Dancing does not require ride equipment. While dancing, ride gear is visually hidden, momentum is safely settled, and movement or action inputs immediately return control to the player.

## Flow and combos

JetSetCraft rewards lines that stay varied rather than repeating one high-value action:

- New tricks and dance moves receive freshness bonuses.
- Immediate repeats receive diminishing score.
- Manuals, grinds, wall rides, powerslides, and successful landings hold the combo bridge.
- Boost tricks spend additional boost for higher score and Flow.
- Perfect, clean, and sketchy landings provide distinct feedback and recovery.
- Flow ranks progress from **WARM UP** to **ALL CITY**.

See the [complete wiki](https://github.com/Herbertofury/JetSetCraft/wiki) for move names, scoring behavior, controls, compatibility, server configuration, and testing commands.

## Optional compatibility

JetSetCraft has no hard dependency on these integrations:

- **Create 6.0.8:** native track axes, normals, graph junctions, and Bezier geometry.
- **The Aether:** Quicksoil speed routes, Blue Aercloud bounce routes, Aerogel/Holystone wall routes, and Quicksoil Glass grind lines through non-required datapack entries.
- **Twilight Forest:** Aurora block, pillar, slab, and glass routes through non-required datapack entries.
- **TACZ:** public `IGun` detection and reduced camera effects while using a weapon.
- **Epic Fight / Better Combat / vanilla combat:** preserved upper-body animation and input ownership.
- **Forge rail/block ecosystems:** native `BaseRailBlock` support plus datapack tags for unusual surfaces and geometry.

Missing optional mods do not create registry errors because optional datapack entries use `required: false`.

## Target stack

| Component | 1.20.1 target |
| --- | --- |
| Minecraft | 1.20.1 |
| Forge | 47.4.23 |
| Java | 17 |
| GeckoLib | 4.8.4 |
| PlayerAnimator | 1.0.2-rc1+1.20 |
| TACZ compatibility target | 1.1.8-hotfix |
| Epic Fight compatibility target | 20.14.17 |
| Create compatibility target | 6.0.8 |

Official project locations: [Forge 1.20.1](https://files.minecraftforge.net/net/minecraftforge/forge/index_1.20.1.html), [GeckoLib](https://www.curseforge.com/minecraft/mc-mods/geckolib/files/8285794), [PlayerAnimator](https://www.curseforge.com/minecraft/mc-mods/playeranimator/files/4587214), [TACZ](https://www.curseforge.com/minecraft/mc-mods/timeless-and-classics-zero/files/8141310), and [Create](https://www.curseforge.com/minecraft/mc-mods/create/files/7178761).

## Build and verification

JetSetCraft uses Java 17, ForgeGradle 6, deterministic generated assets, contract validators, real Forge GameTests, and a dedicated-server startup smoke test.

```text
python -m pip install -r tools/requirements.txt
python tools/generate_models.py
python tools/generate_animations.py
python tools/generate_brand.py
python tools/validate_assets.py
python tools/validate_gameplay_contract.py
python tools/validate_wiki.py
gradle --no-daemon build
gradle --no-daemon runGameTestServer
```

The distributable JAR is written to `build/libs/`. CI also publishes the exact tested source tree, GameTest log, server-smoke log, and built JAR as GitHub Actions artifacts.

In a development world, `/jetsetcraft status` prints the authoritative ride, surface, Flow, trick, dance, landing, enchantment, and compatibility state. `/jetsetcraft build_vanilla_lab` creates a compact acceptance course for rails, redstone, ice, slime, honey, fluids, hazards, and grind geometry.

## Architecture and asset policy

The server owns movement and style truth. Clients send inputs and render synchronized snapshots. Ride locomotion uses a lower-body PlayerAnimator layer; full-body dances and ground stunts use a separate higher-priority layer and yield when a weapon overlay is active. Gameplay state does not depend on an animation backend, so render adapters can evolve without replacing the movement state machine.

`tools/generate_models.py` creates original JetSetCraft equipment meshes and supporting textures. `tools/generate_animations.py` creates deterministic animation resources. `tools/validate_assets.py` enforces JSON validity, OBJ integrity, model quality floors, held-item safety, and the lower-body combat-composition contract. Review `docs/ASSET_PROVENANCE.md` before adding any external asset.

## Status

`0.2.0-alpha.1` is an active playable alpha. The current release is designed for immediate testing while preserving explicit future gates: real-client visual acceptance, large-modpack interoperability, multiplayer soak testing, and further authored VFX/audio/rig polish. Those gates are tracked openly rather than replaced by weaker claims.
