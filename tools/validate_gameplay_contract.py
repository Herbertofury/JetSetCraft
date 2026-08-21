#!/usr/bin/env python3
from __future__ import annotations
import json
import sys
from pathlib import Path

ROOT = Path(__file__).resolve().parents[1]
errors: list[str] = []

movement_dir = ROOT / 'src/main/java/com/herberto/jetsetcraft/movement'
movement = '\n'.join(path.read_text(encoding='utf-8') for path in sorted(movement_dir.glob('*.java')))
world = '\n'.join(path.read_text(encoding='utf-8') for path in sorted(movement_dir.glob('Vanilla*.java')))
config = (ROOT / 'src/main/java/com/herberto/jetsetcraft/config/JetSetConfig.java').read_text(encoding='utf-8')
edge = (ROOT / 'src/main/java/com/herberto/jetsetcraft/movement/EdgeFinder.java').read_text(encoding='utf-8')
data_java = (ROOT / 'src/main/java/com/herberto/jetsetcraft/data/JetSetData.java').read_text(encoding='utf-8')
loadout = (ROOT / 'src/main/java/com/herberto/jetsetcraft/item/RideLoadout.java').read_text(encoding='utf-8')
network = (ROOT / 'src/main/java/com/herberto/jetsetcraft/network/JetSetNetwork.java').read_text(encoding='utf-8')
client = (ROOT / 'src/main/java/com/herberto/jetsetcraft/client/ClientEvents.java').read_text(encoding='utf-8')
ride_item = (ROOT / 'src/main/java/com/herberto/jetsetcraft/item/RideGearItem.java').read_text(encoding='utf-8')
commands = (ROOT / 'src/main/java/com/herberto/jetsetcraft/command/JetSetCommands.java').read_text(encoding='utf-8')
common_events = (ROOT / 'src/main/java/com/herberto/jetsetcraft/event/CommonEvents.java').read_text(encoding='utf-8')
ride_style = (ROOT / 'src/main/java/com/herberto/jetsetcraft/movement/RideStyle.java').read_text(encoding='utf-8')
mod_items = (ROOT / 'src/main/java/com/herberto/jetsetcraft/registry/ModItems.java').read_text(encoding='utf-8')
creative_tab = (ROOT / 'src/main/java/com/herberto/jetsetcraft/registry/ModCreativeTabs.java').read_text(encoding='utf-8')
ride_layer = (ROOT / 'src/main/java/com/herberto/jetsetcraft/client/render/RideGearLayer.java').read_text(encoding='utf-8')
ride_animation = (ROOT / 'src/main/java/com/herberto/jetsetcraft/client/animation/RideAnimationController.java').read_text(encoding='utf-8')
lang = json.loads((ROOT / 'src/main/resources/assets/jetsetcraft/lang/en_us.json').read_text(encoding='utf-8'))

# Core doctrine: riding must not be hard-disabled simply because the player is swimming,
# and external speed above normal caps must have explicit preservation paths.
if 'player.isSwimming()' in movement.split('if (!data.active()', 1)[1].split('{', 1)[0]:
    errors.append('movement hard-reset still disables JetSetCraft while swimming')
for needle, label in [
    ('explosions, pistons, knockback, currents', 'external impulse preservation'),
    ('captureExternalImpulse', 'external impulse bridge'),
    ('applyMicroTerrainContinuity', 'micro-height terrain continuity'),
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
for needle, label in [
    ('usableExposedEdge', 'shape-driven exposed-edge validation'),
    ('sideProbeFree', 'shared/internal seam rejection'),
    ('noCollision(player, clearance)', 'rider clearance above grind edges'),
]:
    if needle not in edge:
        errors.append(f'missing world-geometry grind contract: {label}')
if 'data.grindKind() == GrindKind.EDGE' not in movement or 'preferred.scale(0.58)' not in movement:
    errors.append('edge grinding lacks intentional 90-degree corner steering bias')

# Red Skate's useful dedicated-gear concept is now a permanent compatibility contract: actual ride
# equipment lives in a persistent player loadout so weapons/items remain free in both hands.
for needle, label, source in [
    ('private ItemStack rideGear = ItemStack.EMPTY', 'persistent dedicated ride gear slot', data_java),
    ('t.put("RideGear"', 'ride gear NBT persistence', data_java),
    ('equipFromHand', 'server-authoritative loadout equip', loadout),
    ('returnToPlayer', 'safe ride gear retrieval/swap', loadout),
    ('dropEquipped', 'normal death-drop semantics for ride gear', loadout),
    ('C2SRideLoadoutPacket', 'ride loadout network action', network),
    ('RIDE_TOGGLE', 'ride loadout key binding', client),
]:
    if needle not in source:
        errors.append(f'missing Red Skate-derived production contract: {label}')

# Hoverboards are a first-class ride style inside the same persistent, server-authoritative system.
# They must never regress into an isolated pseudo-vehicle that bypasses vanilla-world/grind/trick logic.
for needle, label, source in [
    ('HOVER(5, "hover"', 'stable hoverboard RideStyle ID', ride_style),
    ('ITEMS.register("hoverboard"', 'hoverboard item registration', mod_items),
    ('new RideGearItem(RideStyle.HOVER', 'hoverboard loadout style binding', mod_items),
    ('ModItems.HOVERBOARD.get()', 'hoverboard creative inventory exposure', creative_tab),
    ('case HOVER -> renderBoard', 'hoverboard third-person render path', ride_layer),
    ('RideStyle.HOVER', 'hoverboard lower-body animation composition', ride_animation),
]:
    if needle not in source:
        errors.append(f'missing hoverboard production contract: {label}')
if not lang.get('item.jetsetcraft.hoverboard'):
    errors.append('missing hoverboard localization')
if not (ROOT / 'src/main/resources/assets/jetsetcraft/models/item/hoverboard.json').exists():
    errors.append('missing hoverboard item model')
if not (ROOT / 'src/main/java/com/herberto/jetsetcraft/gametest/HoverboardGameTests.java').exists():
    errors.append('missing real Forge hoverboard GameTest')
if not (ROOT / 'src/main/resources/data/jetsetcraft/structures/hoverboard_empty.nbt').exists():
    errors.append('missing hoverboard GameTest structure')

# Dedicated skate footwear must still participate in Minecraft's movement enchantment language.
for needle, label, source in [
    ('Enchantments.FROST_WALKER', 'Frost Walker allowed on skate footwear', ride_item),
    ('Enchantments.SOUL_SPEED', 'Soul Speed allowed on skate footwear', ride_item),
    ('Enchantments.DEPTH_STRIDER', 'Depth Strider allowed on skate footwear', ride_item),
    ('FrostWalkerEnchantment.onEntityMoved', 'vanilla Frost Walker implementation reused', world),
    ('Math.max(vanilla, jetSet)', 'loadout enchantments use max-not-sum composition', world),
    ('applyRideEnchantments(player, data)', 'ride enchantments tick before surface sampling', movement),
    ('augmentFallDamageProtection', 'dedicated-slot Feather Falling composition', world),
    ('getDamageProtection(player.getArmorSlots()', 'vanilla EPF used to avoid Feather Falling double-stack', world),
]:
    if needle not in source:
        errors.append(f'missing vanilla enchantment/loadout contract: {label}')

for needle, label in [
    ('GrindMaterialProfile', 'material-specific grind profile'),
    ('SoundType.METAL', 'metal grind language'),
    ('SoundType.COPPER', 'copper grind language'),
    ('SoundType.GLASS', 'glass grind language'),
    ('SoundType.WOOD', 'wood grind language'),
    ('SoundType.STONE', 'stone grind language'),
    ('ParticleTypes.ELECTRIC_SPARK', 'metal/copper grind sparks'),
    ('getStepSound()', 'Minecraft-native material grind audio'),
]:
    if needle not in world:
        errors.append(f'missing material grind contract: {label}')
if 'grindMaterial(player, hit)' not in movement or 'emitGrindFeedback(player, hit, material)' not in movement:
    errors.append('grind solver is not consuming the material/feedback profile')

# Runtime acceptance tooling must stay available so the doctrine can be verified in a real Minecraft world.
for needle, label in [
    ('literal("status")', 'authoritative runtime diagnostics'),
    ('literal("build_vanilla_lab")', 'vanilla acceptance lab builder'),
    ('Blocks.BLUE_ICE', 'Blue Ice test segment'),
    ('Blocks.POWERED_RAIL', 'powered/unpowered rail test segment'),
    ('Blocks.DETECTOR_RAIL', 'detector rail redstone test'),
    ('Blocks.ACTIVATOR_RAIL', 'activator rail action test'),
    ('Blocks.SLIME_BLOCK', 'slime test segment'),
    ('Blocks.HONEY_BLOCK', 'honey test segment'),
    ('Blocks.SOUL_SAND', 'Soul Speed/bubble test segment'),
    ('Blocks.MAGMA_BLOCK', 'magma/bubble hazard test segment'),
    ('Blocks.COBWEB', 'cobweb trap test'),
    ('Blocks.POWDER_SNOW', 'powder-snow trap test'),
    ('Blocks.STONE_SLAB', 'slab continuity test'),
    ('Blocks.STONE_STAIRS', 'stair continuity test'),
    ('Blocks.IRON_BARS', 'iron-bar geometry grind test'),
    ('Blocks.OAK_FENCE', 'fence geometry grind test'),
    ('Blocks.COBBLESTONE_WALL', 'wall geometry grind test'),
    ('Blocks.GLASS_PANE', 'glass-pane geometry grind test'),
    ('Blocks.OAK_LOG', 'natural log/clearance geometry test'),
]:
    if needle not in commands:
        errors.append(f'missing runtime vanilla acceptance tool: {label}')
if 'JetSetCommands.register(event.getDispatcher())' not in common_events:
    errors.append('JetSetCraft runtime commands are not registered on the Forge command event')
for needle, label in [
    ('lastSolverVelocity', 'previous solver velocity tracking'),
    ('externalImpulseTicks', 'external impulse preservation window'),
    ('terrainAssistCooldown', 'safe terrain assist cooldown'),
]:
    if needle not in data_java:
        errors.append(f'missing movement-composition state: {label}')

if 'augmentFallDamageProtection' not in common_events:
    errors.append('custom-slot Feather Falling is not wired into Forge damage handling')

for doc in ('docs/VANILLA_WORLD_MECHANICS_SYNERGY.md', 'docs/RED_SKATE_REBELLION_AUDIT.md'):
    if not (ROOT / doc).exists():
        errors.append(f'missing durable project doctrine: {doc}')

if errors:
    print('JetSetCraft gameplay contract validation FAILED')
    for error in errors:
        print(' -', error)
    sys.exit(1)
print('JetSetCraft gameplay contract validation OK')
