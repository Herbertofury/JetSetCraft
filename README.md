# JetSetCraft

> **Official visual identity:** the project-owner-supplied JetSetCraft artwork is the canonical icon/cover source. The full-quality cover below is preserved byte-for-byte from the approved project asset; the smaller `src/main/resources/jetsetcraft.png` remains the runtime mod icon.

<p align="center">
  <img src="assets/jetsetcraft-cover.webp" alt="JetSetCraft official cover art" width="960">
</p>

**JetSetCraft** is a Forge 1.20.1 urban movement/combat mod built around momentum instead of scripted vehicle mode. The goal is to make Minecraft movement feel like a full street-sports game while remaining composable with normal Minecraft combat and third-party weapons.

## Current alpha foundation

- Inline skates, quad roller skates, skateboard, and BMX are separate ride styles with different acceleration, steering, air control, grind behavior, cruise caps, and boost caps.
- Server-authoritative momentum, boost meter, manuals, aerial tricks, combo scoring, powerslides, wall rides, and arbitrary block-edge grinding.
- Grinding supports arbitrary collision-shape block edges **and** first-class rail paths: vanilla/Forge rails, datapack-opted custom rails, and Create 6.0.8 tracks including diagonals, slopes, junctions, and long Bezier curves.
- Combat does **not** end a ride state. Vanilla attacks/items and third-party combat inputs keep running while JetSetCraft owns locomotion.
- TACZ detection uses TACZ's public `IGun` API when TACZ is installed.
- PlayerAnimator lower-body animation layer deliberately leaves arms/hands/head unclaimed so TACZ, Epic Fight, Better Combat, bows, spellbooks, and other weapon layers can compose above skating movement.
- Persistent wall graffiti entities with selectable decals and repaint-in-place behavior.
- Dense original OBJ ride meshes and a generated asset pipeline rather than vanilla cuboid placeholders.
- Dynamic FOV and camera roll are configurable and automatically reduced while aiming/using a weapon.

## Target stack

| Component | 1.20.1 target |
| --- | --- |
| Minecraft | 1.20.1 |
| Forge | 47.4.23 |
| Java | 17 |
| GeckoLib | 4.8.4 |
| PlayerAnimator | 1.0.2-rc1+1.20 |
| TACZ compat target | 1.1.8-hotfix |
| Epic Fight compat target | 20.14.17 |
| Create compat target | 6.0.8 |

Forge: https://files.minecraftforge.net/net/minecraftforge/forge/index_1.20.1.html
GeckoLib: https://www.curseforge.com/minecraft/mc-mods/geckolib/files/8285794
PlayerAnimator: https://www.curseforge.com/minecraft/mc-mods/playeranimator/files/4587214
TACZ: https://www.curseforge.com/minecraft/mc-mods/timeless-and-classics-zero/files/8141310
Create: https://www.curseforge.com/minecraft/mc-mods/create/files/7178761

## Controls

- **Right-click ride gear**: equip/toggle that ride style
- **Left Alt**: boost
- **R**: trick
- **G**: grind / wall ride (real rails/tracks are preferred over incidental edges)
- **C**: manual
- **V**: brake / powerslide
- **Space while grinding**: rail hop / transfer while preserving momentum
- **R while grinding**: rail-specific trick; stays attached to the rail and continues the combo
- **Shift + use spray can**: cycle graffiti
- **Use spray can on a wall**: spray selected graffiti

All key mappings are normal Forge keybinds and can be rebound in Controls.

## Build

JetSetCraft uses Java 17 and ForgeGradle 6. Generated high-detail meshes/textures are rebuilt from the checked-in generator before a clean build.

```text
python -m pip install -r tools/requirements.txt
python tools/generate_models.py
python tools/validate_assets.py
gradle --no-daemon build
```

The output is written to `build/libs/`.

## Animation architecture

JetSetCraft treats locomotion and action animation as separate channels. The ride layer supplies pelvis/body/leg movement and ride-gear presentation. It does not seize the weapon arms. This is essential for the core rule that a player can grind, boost, wall ride, or powerslide while aiming, firing, reloading, melee attacking, blocking, or using another mod's item.

The 1.20.1 implementation uses PlayerAnimator for player-layer composition and carries GeckoLib 4.8.4 as the Bedrock-format/high-fidelity animation foundation for the richer rig pipeline. Create compatibility is an optional native adapter: JetSetCraft reads Create's own `ITrackBlock` axes/normals and Bezier track graph rather than guessing from block names. Later Minecraft versions can swap animation or track adapters without replacing the server-authoritative movement state machine.

## Asset pipeline

`tools/generate_models.py` regenerates the original JetSetCraft equipment meshes and supporting textures. `tools/validate_assets.py` verifies JSON validity, OBJ face integrity, model coverage, the animation compatibility contract, and the minimum mesh-quality floor.

See `docs/ASSET_PROVENANCE.md` before adding third-party or game-derived assets.

## Status

This repository is an active alpha, not a finished public release. The current focus is making the 1.20.1 gameplay/build clean first, then expanding tricks, grind transfer logic, animated ride rigs, graffiti authoring, audio/VFX, combat integrations, and finally newer Minecraft versions without weakening 1.20.1.
