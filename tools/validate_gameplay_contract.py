#!/usr/bin/env python3
from __future__ import annotations
import json
import sys
from pathlib import Path

ROOT = Path(__file__).resolve().parents[1]
errors: list[str] = []

movement = (ROOT / 'src/main/java/com/herberto/jetsetcraft/movement/JetSetMovement.java').read_text(encoding='utf-8')
world = (ROOT / 'src/main/java/com/herberto/jetsetcraft/movement/VanillaWorldPhysics.java').read_text(encoding='utf-8')
config = (ROOT / 'src/main/java/com/herberto/jetsetcraft/config/JetSetConfig.java').read_text(encoding='utf-8')
edge = (ROOT / 'src/main/java/com/herberto/jetsetcraft/movement/EdgeFinder.java').read_text(encoding='utf-8')

# Core doctrine: riding must not be hard-disabled simply because the player is swimming,
# and external speed above normal caps must have explicit preservation paths.
if 'player.isSwimming()' in movement.split('if (!data.active()', 1)[1].split('{', 1)[0]:
    errors.append('movement hard-reset still disables JetSetCraft while swimming')
for needle, label in [
    ('explosions, pistons, knockback, currents', 'external impulse preservation'),
    ('baseSpeed < cap', 'above-cap momentum preservation'),
    ('applyFluidMovement', 'fluid composition'),
    ('applyAirborneSurfaceInteractions', 'honey/slime side-contact composition'),
    ('applyVanillaRail', 'vanilla rail semantics'),
    ('jumpMultiplier', 'Jump Boost composition'),
]:
    if needle not in movement and needle not in world:
        errors.append(f'missing gameplay contract: {label}')

required_world_tokens = {
    'Blocks.BLUE_ICE': 'Blue Ice extreme speed',
    'Blocks.PACKED_ICE': 'Packed Ice progression',
    'Blocks.FROSTED_ICE': 'Frost Walker/frosted ice synergy',
    'Blocks.SLIME_BLOCK': 'slime bounce',
    'Blocks.HONEY_BLOCK': 'honey drag/wall interaction',
    'Enchantments.SOUL_SPEED': 'Soul Speed route',
    'Enchantments.DEPTH_STRIDER': 'Depth Strider water composition',
    'MobEffects.DOLPHINS_GRACE': "Dolphin's Grace composition",
    'Blocks.POWERED_RAIL': 'powered/unpowered rail semantics',
    'Blocks.DETECTOR_RAIL': 'detector rail redstone',
    'Blocks.ACTIVATOR_RAIL': 'activator rail action pulse',
    'SnowLayerBlock.LAYERS': 'snow-depth drag',
    'Blocks.COBWEB': 'cobweb trap behavior',
    'Blocks.POWDER_SNOW': 'powder snow trap behavior',
}
for token, label in required_world_tokens.items():
    if token not in world:
        errors.append(f'missing vanilla-world mechanic: {label}')

for key in ('enableVanillaWorldPhysics', 'blueIceSpeedMultiplier', 'slimeBounceMultiplier',
            'poweredRailBoostPerTick', 'unpoweredRailRetention'):
    if key not in config:
        errors.append(f'missing server config: {key}')

# The blue-ice default should remain deliberately beyond ordinary ground; this is a feel contract,
# not a generic safe-speed cap.
if 'defineInRange("blueIceSpeedMultiplier", 2.15' not in config:
    errors.append('Blue Ice default no longer preserves the deliberate 2.15x extreme-speed target')

# Data-driven surface hooks and their important vanilla defaults.
tag_dir = ROOT / 'src/main/resources/data/jetsetcraft/tags/blocks'
required_tags = {
    'low_friction_surfaces.json': {'minecraft:ice', 'minecraft:packed_ice', 'minecraft:blue_ice', 'minecraft:frosted_ice'},
    'bounce_surfaces.json': {'minecraft:slime_block'},
    'sticky_surfaces.json': {'minecraft:honey_block'},
    'brake_surfaces.json': {'minecraft:honey_block', 'minecraft:soul_sand', 'minecraft:soul_soil', 'minecraft:cobweb'},
    'hazard_surfaces.json': {'minecraft:magma_block', 'minecraft:cactus', 'minecraft:campfire'},
    'grindable.json': {'minecraft:iron_bars', '#minecraft:fences', '#minecraft:walls', '#minecraft:rails'},
}
for filename, expected in required_tags.items():
    path = tag_dir / filename
    if not path.exists():
        errors.append(f'missing interaction tag {filename}')
        continue
    try:
        data = json.loads(path.read_text(encoding='utf-8'))
    except Exception as exc:
        errors.append(f'{filename}: invalid JSON: {exc}')
        continue
    values = set(data.get('values', []))
    missing = expected - values
    if missing:
        errors.append(f'{filename}: missing defaults {sorted(missing)}')

if 'JetSetTags.NO_GRIND' not in edge:
    errors.append('generic edge solver does not respect no_grind tag')
if 'JetSetTags.HAZARD_SURFACES' not in edge:
    errors.append('generic edge solver does not guard hazard surfaces')

for doc in ('docs/VANILLA_WORLD_MECHANICS_SYNERGY.md', 'docs/RED_SKATE_REBELLION_AUDIT.md'):
    if not (ROOT / doc).exists():
        errors.append(f'missing durable project doctrine: {doc}')

if errors:
    print('JetSetCraft gameplay contract validation FAILED')
    for error in errors:
        print(' -', error)
    sys.exit(1)
print('JetSetCraft gameplay contract validation OK')
