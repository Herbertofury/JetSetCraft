#!/usr/bin/env python3
from __future__ import annotations
import json
import re
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
dance_system = (ROOT / 'src/main/java/com/herberto/jetsetcraft/movement/DanceSystem.java').read_text(encoding='utf-8')
trick_combo = (ROOT / 'src/main/java/com/herberto/jetsetcraft/movement/TrickCombo.java').read_text(encoding='utf-8')
trick_catalog = (ROOT / 'src/main/java/com/herberto/jetsetcraft/movement/TrickCatalog.java').read_text(encoding='utf-8')
dance_catalog = (ROOT / 'src/main/java/com/herberto/jetsetcraft/movement/DanceCatalog.java').read_text(encoding='utf-8')
style_feedback = (ROOT / 'src/main/java/com/herberto/jetsetcraft/movement/StyleFeedback.java').read_text(encoding='utf-8')
input_flags = (ROOT / 'src/main/java/com/herberto/jetsetcraft/network/InputFlags.java').read_text(encoding='utf-8')
state_packet = (ROOT / 'src/main/java/com/herberto/jetsetcraft/network/S2CStatePacket.java').read_text(encoding='utf-8')
client_state = (ROOT / 'src/main/java/com/herberto/jetsetcraft/client/state/ClientRideState.java').read_text(encoding='utf-8')
mob_gear = (ROOT / 'src/main/java/com/herberto/jetsetcraft/mob/MobStreetGear.java').read_text(encoding='utf-8')
mob_rig = (ROOT / 'src/main/java/com/herberto/jetsetcraft/mob/MobRideRigResolver.java').read_text(encoding='utf-8')
mob_events = (ROOT / 'src/main/java/com/herberto/jetsetcraft/event/MobStreetGearEvents.java').read_text(encoding='utf-8')
mob_layer = (ROOT / 'src/main/java/com/herberto/jetsetcraft/client/render/MobRideGearLayer.java').read_text(encoding='utf-8')
mob_packet = (ROOT / 'src/main/java/com/herberto/jetsetcraft/network/S2CMobGearPacket.java').read_text(encoding='utf-8')
build_workflow = (ROOT / '.github/workflows/build.yml').read_text(encoding='utf-8')
style_flow_tests = (ROOT / 'src/main/java/com/herberto/jetsetcraft/gametest/StyleFlowGameTests.java').read_text(encoding='utf-8')
hoverboard_tests = (ROOT / 'src/main/java/com/herberto/jetsetcraft/gametest/HoverboardGameTests.java').read_text(encoding='utf-8')
street_gear_tests = (ROOT / 'src/main/java/com/herberto/jetsetcraft/gametest/StreetGearGameTests.java').read_text(encoding='utf-8')
model_generator = (ROOT / 'tools/generate_models.py').read_text(encoding='utf-8')
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
    values = {entry if isinstance(entry, str) else entry.get('id') for entry in data.get('values', [])}
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
    ('put("RideGear"', 'ride gear NBT persistence', data_java),
    ('equipFromHand', 'server-authoritative loadout equip', loadout),
    ('returnToPlayer', 'safe ride gear retrieval/swap', loadout),
    ('dropEquipped', 'normal death-drop semantics for ride gear', loadout),
    ('C2SRideLoadoutPacket', 'ride loadout network action', network),
    ('RIDE_TOGGLE', 'ride loadout key binding', client),
]:
    if needle not in source:
        errors.append(f'missing Red Skate-derived production contract: {label}')

# Persistent same-entity Street Gear is a compatibility covenant, not an event-only cosmetic.
for needle, label, source in [
    ('getPersistentData()', 'same-entity persistent storage', mob_gear),
    ('Stack', 'physical ItemStack persistence', mob_gear),
    ('StreetGearAcquisition', 'auditable acquisition source', mob_gear),
    ('LivingEquipmentChangeEvent', 'native pickup observer', mob_events),
    ('LivingConversionEvent.Post', 'conversion persistence bridge', mob_events),
    ('LivingDropsEvent', 'physical death-drop semantics', mob_events),
    ('PlayerEvent.StartTracking', 'late-join tracking synchronization', mob_events),
    ('ride_rig/biped', 'data-driven biped rig tag', mob_rig),
    ('ride_rig/quadruped', 'data-driven quadruped rig tag', mob_rig),
    ('MobRideGearLayer', 'renderer-agnostic mob gear visuals', client),
    ('S2CMobGearPacket', 'server-authoritative mob gear sync', network),
    ('writeItem', 'full physical gear stack network payload', mob_packet),
]:
    if needle not in source:
        errors.append(f'missing persistent Street Gear contract: {label}')
for needle, label, source in [
    ('entity instanceof Mob', 'real-mob-only eligibility boundary', mob_gear),
    ('hasStoredState', 'corrupt-state repair entry point', mob_gear),
    ('entity.spawnAtLocation(stack.copy())', 'physical fallback when a datapack later rejects a rig', mob_gear),
    ('remainder.shrink(1)', 'exactly-one native equipment consumption', mob_events),
    ('Never mutate the mob\'s persistent drop-chance rules', 'third-party drop-rule sovereignty', mob_events),
    ('JETSETCRAFT_GAMETEST_PASS street_gear', 'real Forge same-entity Street Gear acceptance', street_gear_tests),
]:
    if needle not in source:
        errors.append(f'missing compatibility-safe Street Gear contract: {label}')

for path in (
    ROOT / 'src/main/resources/data/jetsetcraft/tags/entity_types/ride_rig/biped.json',
    ROOT / 'src/main/resources/data/jetsetcraft/tags/entity_types/ride_rig/quadruped.json',
    ROOT / 'src/main/resources/data/jetsetcraft/tags/entity_types/street_gear_incompatible.json',
):
    if not path.exists(): errors.append(f'missing Street Gear resource: {path.relative_to(ROOT)}')

for needle, label, source in [
    ('mc.screen == null', 'menu-safe client input gate', client),
    ('Float.floatToIntBits(forward)', 'immediate analog-change transmission', client),
    ('acceptInput(~0, Float.NaN, Float.POSITIVE_INFINITY)', 'non-finite/unknown-bit GameTest', style_flow_tests),
    ('tickInputWatchdog()', 'lost-input watchdog wiring', movement),
    ('outcome.level().addFreshEntity(new ItemEntity', 'conversion-safe physical gear fallback', mob_events),
    ('copied.copyFrom(sanitizedSave)', 'capability clone hostile-data acceptance', style_flow_tests),
    ('finiteUnit(mc.player.input.leftImpulse)', 'non-finite-safe camera input', client),
]:
    if needle not in source:
        errors.append(f'missing premium reliability contract: {label}')

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

# Style Flow is one server-authoritative system, not disconnected cosmetic emotes.
for needle, label, source in [
    ('SCOOTER(6, "scooter"', 'stable scooter RideStyle ID', ride_style),
    ('ITEMS.register("scooter"', 'scooter item registration', mod_items),
    ('new RideGearItem(RideStyle.SCOOTER', 'scooter loadout binding', mod_items),
    ('case SCOOTER -> renderScooter', 'scooter third-person render path', ride_layer),
    ('RideStyle.SCOOTER', 'scooter lower-body animation composition', ride_animation),
    ('DANCE = 1 << 6', 'dedicated dance input', input_flags),
    ('getEntitiesOfClass(ServerPlayer.class', 'nearby-player cypher detection', dance_system),
    ('enableCyphers', 'server-configurable cypher support', config),
    ('allowGroundStunts', 'server-configurable ground stunts', config),
    ('styleBoostScale', 'style-derived boost control', config),
    ('allowBoostTricks', 'server-configurable boost tricks', config),
    ('cypherRadius', 'server-configurable cypher radius', config),
    ('reducedMotion', 'client reduced-motion mode', config),
    ('repetitionScale', 'repetition-aware scoring', trick_combo),
    ('uniqueTrickMask', 'trick-variety tracking', data_java),
    ('uniqueDanceMask', 'dance-variety tracking', data_java),
    ('scoreLanding', 'landing grade scoring', trick_combo),
    ('boostTrick', 'boost-trick state', data_java),
    ('rankName', 'style rank vocabulary', trick_catalog),
    ('MOVE_COUNT = 28', 'twenty-eight named dance moves', dance_catalog),
    ('DanceCatalog.byId(state.danceMoveId()).animationIndex()', 'dynamic dance animation selection', ride_animation),
    ('weaponOverlay', 'weapon-safe full-body dance suppression', ride_animation),
    ('ParticleTypes.ELECTRIC_SPARK', 'style action particle feedback', style_feedback),
    ('player.isUsingItem() || player.swinging', 'immediate combat/input sovereignty while dancing', dance_system),
]:
    if needle not in source:
        errors.append(f'missing Style Flow contract: {label}')
for path in (
    ROOT / 'src/main/resources/assets/jetsetcraft/models/item/scooter.json',
    ROOT / 'src/main/resources/assets/jetsetcraft/models/item/hoverboard.json',
    ROOT / 'src/main/resources/data/jetsetcraft/recipes/scooter.json',
    ROOT / 'src/main/resources/data/jetsetcraft/recipes/hoverboard.json',
):
    if not path.exists(): errors.append(f'missing Style Flow resource: {path.relative_to(ROOT)}')
if not (ROOT / 'src/main/java/com/herberto/jetsetcraft/gametest/StyleFlowGameTests.java').exists():
    errors.append('missing real Forge Style Flow GameTests')
if not (ROOT / 'tools/generate_animations.py').exists():
    errors.append('missing deterministic animation generator')
if 'pinned JSR graffiti pack unavailable; using offline originals' not in model_generator:
    errors.append('asset generation does not preserve an offline-original graffiti fallback')

for generated_function in ('make_hoverboard', 'make_scooter'):
    if model_generator.count(f'def {generated_function}(') != 1:
        errors.append(f'asset generator must define {generated_function} exactly once')
if "'shell':'hover_deck'" not in model_generator:
    errors.append('hoverboard generator is not bound to its dedicated deck texture')

# Optional dimension-mod integrations must never make those mods required.
for tag_name, optional_ids in {
    'boost_surfaces.json': {'aether:quicksoil', 'aether:quicksoil_glass'},
    'low_friction_surfaces.json': {'aether:quicksoil', 'twilightforest:aurora_block'},
    'bounce_surfaces.json': {'aether:blue_aercloud'},
}.items():
    path = tag_dir / tag_name
    if not path.exists():
        errors.append(f'missing optional compatibility tag {tag_name}')
        continue
    entries = json.loads(path.read_text(encoding='utf-8')).get('values', [])
    optional = {entry.get('id') for entry in entries if isinstance(entry, dict) and entry.get('required') is False}
    missing = optional_ids - optional
    if missing:
        errors.append(f'{tag_name}: missing non-required compatibility entries {sorted(missing)}')

# Network and CI state must evolve atomically. A packet-order mismatch here would silently corrupt every HUD and
# animation decision even though both sides still compile, so validate the wire contract from source order.
record_match = re.search(r'public record S2CStatePacket\((.*?)\) \{', state_packet, re.S)
if not record_match:
    errors.append('could not parse S2CStatePacket record components')
else:
    record_fields = re.findall(r'\b(?:int|boolean|float)\s+(\w+)', record_match.group(1))
    encode_match = re.search(r'public static void encode\(.*?\) \{(.*?)\n    \}', state_packet, re.S)
    decode_match = re.search(r'public static S2CStatePacket decode\(.*?\) \{(.*?)\n    \}', state_packet, re.S)
    if not encode_match or not decode_match:
        errors.append('could not parse S2CStatePacket encode/decode methods')
    else:
        encoded = re.findall(r'buffer\.write(?:VarInt|Boolean|Float)\(packet\.(\w+)\)', encode_match.group(1))
        decoded = re.findall(r'buffer\.(readVarInt|readBoolean|readFloat)\(\)', decode_match.group(1))
        writes = re.findall(r'buffer\.(writeVarInt|writeBoolean|writeFloat)\(packet\.(\w+)\)', encode_match.group(1))
        expected_reads = {
            'writeVarInt': 'readVarInt',
            'writeBoolean': 'readBoolean',
            'writeFloat': 'readFloat',
        }
        if encoded != record_fields:
            errors.append(f'S2C encode order differs from record order: {encoded} != {record_fields}')
        if len(decoded) != len(record_fields):
            errors.append(f'S2C decode field count differs from record: {len(decoded)} != {len(record_fields)}')
        elif [expected_reads[write] for write, _ in writes] != decoded:
            errors.append('S2C decode primitive order differs from encode primitive order')
        for field in record_fields:
            if field != 'entityId' and f'packet.{field}()' not in client_state:
                errors.append(f'client snapshot does not consume S2C field {field}')
if 'private static final String PROTOCOL = "7"' not in network:
    errors.append('network protocol was not bumped for the expanded Style Flow packet')

required_gametest_markers = {
    'JETSETCRAFT_GAMETEST_PASS hoverboard': hoverboard_tests,
    'JETSETCRAFT_GAMETEST_PASS scooter': style_flow_tests,
    'JETSETCRAFT_GAMETEST_PASS dance_flow': style_flow_tests,
    'JETSETCRAFT_GAMETEST_PASS combat_sovereignty': style_flow_tests,
    'JETSETCRAFT_GAMETEST_PASS catalogs': style_flow_tests,
    'JETSETCRAFT_GAMETEST_PASS street_gear': street_gear_tests,
}
for pass_marker, source in required_gametest_markers.items():
    if pass_marker not in source:
        errors.append(f'missing explicit real-Forge acceptance marker: {pass_marker}')
    if pass_marker not in build_workflow:
        errors.append(f'CI does not require real-Forge acceptance marker: {pass_marker}')
for workflow_token, label in [
    ('build/verification/style-flow-gametest.log', 'ignored GameTest evidence path'),
    ('build/verification/server-smoke.log', 'ignored server-smoke evidence path'),
    ('tools/finalize_ci_checkpoint.py', 'verified project-memory finalizer'),
    ('git write-tree', 'exact staged source tree packaging'),
    ('style-flow-pending.ready.json', 'complete-payload ready gate'),
]:
    if workflow_token not in build_workflow:
        errors.append(f'missing CI durability contract: {label}')
if not (ROOT / 'tools/finalize_ci_checkpoint.py').exists():
    errors.append('missing Style Flow CI finalizer')

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
