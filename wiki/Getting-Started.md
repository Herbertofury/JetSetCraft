# Getting Started

## Requirements

JetSetCraft `0.2.0-alpha.1` targets:

| Component | Required or tested target |
| --- | --- |
| Minecraft | 1.20.1 |
| Forge | 47.4.23 |
| Java | 17 |
| PlayerAnimator | 1.0.2-rc1+1.20 |
| GeckoLib | 4.8.4 |

Create and TACZ are optional. The Aether, Twilight Forest, Epic Fight, Better Combat, and other dimension/combat mods are also optional.

## Installation

1. Install Forge 47.4.23 for Minecraft 1.20.1.
2. Place the verified JetSetCraft JAR in the instance `mods` folder.
3. Install the required PlayerAnimator and GeckoLib dependencies.
4. Launch once so Forge creates the common/server and client configuration files.
5. In multiplayer, install the same JetSetCraft build and required dependencies on both client and server.

## Your first ride

Open the JetSetCraft creative tab or craft a piece of ride gear. Right-click the gear to move the actual item stack into JetSetCraft's dedicated loadout slot. This keeps both hands and the vanilla boots slot available for weapons, tools, shields, spellbooks, or armor.

Press **K** to toggle the equipped ride on or off. Press **Shift + K** to return the gear to your inventory. The item keeps its NBT, enchantments, and future customization data while equipped.

Start with these actions:

- Hold a movement key to accelerate.
- Hold **Left Alt** to boost.
- Press **R** in the air for a trick.
- Hold **G** near a rail, fence, wall top, ledge, or supported track to grind.
- Hold **C** on the ground to manual and preserve a combo.
- Hold **V** while steering to powerslide.
- Press **B** while standing to begin dancing, even with no ride gear equipped.

## Quick practice course

Operators can run `/jetsetcraft build_vanilla_lab` in a test world. The command creates lanes for ice, powered rails, detector/activator rails, water and bubble columns, slime, honey, hazards, micro-terrain, fences, walls, panes, and other world-geometry acceptance cases.

Use `/jetsetcraft status` while riding to see the authoritative server state: momentum, velocity, surface profile, boost, Flow, combo, rank, active trick, landing grade, dance move, cypher size, enchantment composition, and grind material.
