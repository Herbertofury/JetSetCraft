package com.herberto.jetsetcraft.command;

import com.herberto.jetsetcraft.JetSetCraft;
import com.herberto.jetsetcraft.data.JetSetData;
import com.herberto.jetsetcraft.data.JetSetDataProvider;
import com.herberto.jetsetcraft.item.RideLoadout;
import com.herberto.jetsetcraft.movement.DanceCatalog;
import com.herberto.jetsetcraft.movement.GrindFinder;
import com.herberto.jetsetcraft.movement.JetSetMovement;
import com.herberto.jetsetcraft.movement.TrickCatalog;
import com.herberto.jetsetcraft.movement.VanillaWorldPhysics;
import com.herberto.jetsetcraft.network.JetSetNetwork;
import com.herberto.jetsetcraft.registry.ModItems;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.DoubleArgumentType;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.ChatFormatting;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.Locale;

/**
 * Runtime acceptance and diagnostics commands for the movement stack.
 *
 * These are intentionally useful in a normal dev/test world rather than being fake unit tests for
 * movement that only exists inside Minecraft. /jetsetcraft status exposes the authoritative server
 * state and /jetsetcraft build_vanilla_lab lays down a compact course made from ordinary Minecraft
 * blocks so the vanilla-synergy contract can be verified by actually riding it.
 */
public final class JetSetCommands {
    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        var root = Commands.literal("jetsetcraft")
                .then(Commands.literal("status")
                        .executes(context -> status(context.getSource())))
                .then(Commands.literal("set_momentum")
                        .requires(source -> source.hasPermission(2))
                        .then(Commands.argument("speed", DoubleArgumentType.doubleArg(0.0, 8.0))
                                .executes(context -> setMomentum(context.getSource(),
                                        DoubleArgumentType.getDouble(context, "speed")))))
                .then(Commands.literal("build_vanilla_lab")
                        .requires(source -> source.hasPermission(2))
                        .executes(context -> buildVanillaLab(context.getSource())));
        if (Boolean.getBoolean("jetsetcraft.visualAudit")) {
            root.then(Commands.literal("visual_audit")
                        .requires(source -> source.hasPermission(2))
                        .executes(context -> visualAudit(context.getSource())));
        }
        dispatcher.register(root);
    }

    private static int status(CommandSourceStack source) {
        ServerPlayer player;
        try {
            player = source.getPlayerOrException();
        } catch (CommandSyntaxException exception) {
            source.sendFailure(Component.literal("JetSetCraft status requires a player."));
            return 0;
        }

        JetSetData data = player.getCapability(JetSetDataProvider.CAPABILITY).resolve().orElse(null);
        if (data == null) {
            source.sendFailure(Component.literal("JetSetCraft capability is not attached to this player."));
            return 0;
        }

            VanillaWorldPhysics.Surface surface = VanillaWorldPhysics.ground(player);
            VanillaWorldPhysics.MotionProfile profile = VanillaWorldPhysics.profile(player, data, surface);
            String block = String.valueOf(ForgeRegistries.BLOCKS.getKey(surface.state().getBlock()));
            Vec3 horizontal = new Vec3(player.getDeltaMovement().x, 0.0, player.getDeltaMovement().z);

            source.sendSuccess(() -> Component.literal("JetSetCraft server diagnostics").withStyle(ChatFormatting.AQUA), false);
            source.sendSuccess(() -> Component.literal(String.format(Locale.ROOT,
                    "ride=%s equipped=%s active=%s momentum=%.3f velocity=%.3f boost=%.1f flow=%.1f combo=%d x%.2f rank=%s",
                    data.style().serializedName(), RideLoadout.equippedStyle(data).serializedName(), data.active(),
                    data.momentum(), horizontal.length(), data.boost(), data.flow(), data.comboScore(),
                    data.comboMultiplier(), TrickCatalog.rankName(data.comboScore(), data.comboMultiplier(), data.flow()))), false);
            source.sendSuccess(() -> Component.literal(String.format(Locale.ROOT,
                    "surface=%s block=%s friction=%.3f cap=%.2fx boostCap=%.2fx retention=%.5f steering=%.2fx",
                    surface.kind().name().toLowerCase(Locale.ROOT), block, surface.vanillaFriction(),
                    profile.cruiseCapMultiplier(), profile.boostCapMultiplier(), profile.coastingRetention(),
                    profile.steeringMultiplier())), false);
            source.sendSuccess(() -> Component.literal(String.format(Locale.ROOT,
                    "enchants frost=%d soul=%d depth=%d feather=%d water=%s underwater=%s",
                    VanillaWorldPhysics.effectiveEnchantmentLevel(player, data, Enchantments.FROST_WALKER),
                    VanillaWorldPhysics.effectiveEnchantmentLevel(player, data, Enchantments.SOUL_SPEED),
                    VanillaWorldPhysics.effectiveEnchantmentLevel(player, data, Enchantments.DEPTH_STRIDER),
                    VanillaWorldPhysics.effectiveEnchantmentLevel(player, data, Enchantments.FALL_PROTECTION),
                    player.isInWater(), player.isUnderWater())), false);
            source.sendSuccess(() -> Component.literal(String.format(Locale.ROOT,
                    "state grind=%s/%s wall=%s manual=%s powerslide=%s boost=%s curve=%.3f impulseTicks=%d impulse=%.3f terrainAssist=%d",
                    data.grinding(), data.grindKind().serializedName(), data.wallRiding(), data.manual(),
                    data.powersliding(), data.boosting(), data.grindCurveFactor(), data.externalImpulseTicks(),
                    data.externalImpulse().length(), data.terrainAssistCooldown())), false);
            source.sendSuccess(() -> Component.literal(String.format(Locale.ROOT,
                    "style trick=%s boostTrick=%s landing=%s dance=%s/%s chain=%d cypher=%d",
                    TrickCatalog.debugName(data.trickIndex(), data.style()), data.boostTrick(),
                    TrickCatalog.landingName(data.landingGrade()), data.danceStyle().serializedName(),
                    DanceCatalog.name(data.danceMoveId()), data.danceChain(), data.cypherSize())), false);

            if (data.grinding()) {
                Vec3 preferred = data.grindDirection().lengthSqr() > 1.0e-6
                        ? data.grindDirection() : JetSetMovement.horizontalLook(player);
                GrindFinder.findBest(player, preferred, data.grindKind()).ifPresent(target -> {
                    VanillaWorldPhysics.GrindMaterialProfile material = VanillaWorldPhysics.grindMaterial(player, target);
                    source.sendSuccess(() -> Component.literal(String.format(Locale.ROOT,
                            "grindTarget=%s material=%s cap=%.3fx retention=%.5f curvature=%.4f",
                            target.kind().serializedName(), material.kind().name().toLowerCase(Locale.ROOT),
                            material.capMultiplier(), material.retention(), target.curvature())), false);
                });
            }
        return 1;
    }

    private static int setMomentum(CommandSourceStack source, double speed) {
        ServerPlayer player;
        try {
            player = source.getPlayerOrException();
        } catch (CommandSyntaxException exception) {
            source.sendFailure(Component.literal("JetSetCraft set_momentum requires a player."));
            return 0;
        }
        JetSetData data = player.getCapability(JetSetDataProvider.CAPABILITY).resolve().orElse(null);
        if (data == null) {
            source.sendFailure(Component.literal("JetSetCraft capability is not attached to this player."));
            return 0;
        }
        Vec3 direction = JetSetMovement.desiredDirection(player, data);
        if (direction.lengthSqr() < 1.0e-6) direction = JetSetMovement.horizontalLook(player);
        direction = direction.normalize();
        Vec3 current = player.getDeltaMovement();
        data.setMomentum(speed);
        player.setDeltaMovement(direction.x * speed, current.y, direction.z * speed);
        player.hurtMarked = true;
        source.sendSuccess(() -> Component.literal(String.format(Locale.ROOT,
                "JetSetCraft momentum set to %.3f for runtime testing.", speed)).withStyle(ChatFormatting.YELLOW), false);
        return 1;
    }

    /** Deterministic maintainer scene used by the opt-in client screenshot audit; never runs during normal play. */
    private static int visualAudit(CommandSourceStack source) {
        ServerPlayer player;
        try {
            player = source.getPlayerOrException();
        } catch (CommandSyntaxException exception) {
            source.sendFailure(Component.literal("JetSetCraft visual audit requires a player."));
            return 0;
        }
        JetSetData data = player.getCapability(JetSetDataProvider.CAPABILITY).resolve().orElse(null);
        if (data == null) return 0;
        data.setRideGear(new net.minecraft.world.item.ItemStack(ModItems.BMX.get()));
        data.setStyle(com.herberto.jetsetcraft.movement.RideStyle.BMX);
        data.setActive(true);
        data.setBoost(72.0f);
        data.setFlow(46.0f);
        data.setComboScore(1840);
        data.setComboMultiplier(2.25f);
        data.setComboGrace(1200);
        player.getInventory().setItem(8, new net.minecraft.world.item.ItemStack(ModItems.SPRAY_CAN.get()));
        player.getInventory().setItem(player.getInventory().selected, net.minecraft.world.item.ItemStack.EMPTY);
        ServerLevel level = player.serverLevel();
        BlockPos stage = player.blockPosition().offset(0, 12, 0);
        for (int x = -8; x <= 8; x++) {
            for (int z = -8; z <= 8; z++) {
                level.setBlockAndUpdate(stage.offset(x, -1, z), Blocks.SMOOTH_STONE.defaultBlockState());
                for (int y = 0; y <= 7; y++) level.setBlockAndUpdate(stage.offset(x, y, z), Blocks.AIR.defaultBlockState());
            }
        }
        player.teleportTo(stage.getX() + 0.5D, stage.getY(), stage.getZ() + 0.5D);
        player.setDeltaMovement(Vec3.ZERO);
        player.setYRot(180.0f);
        player.setXRot(8.0f);
        JetSetNetwork.sync(player, data);
        JetSetCraft.LOGGER.info("JetSetCraft visual audit scene ready for {}", player.getGameProfile().getName());
        return 1;
    }

    private static int buildVanillaLab(CommandSourceStack source) {
        ServerPlayer player;
        try {
            player = source.getPlayerOrException();
        } catch (CommandSyntaxException exception) {
            source.sendFailure(Component.literal("JetSetCraft build_vanilla_lab requires a player."));
            return 0;
        }

        ServerLevel level = player.serverLevel();
        BlockPos origin = player.blockPosition().offset(4, -1, 4);
        clearLabVolume(level, origin);
        buildSurfaceLane(level, origin);
        buildRailLane(level, origin.offset(3, 0, 0));
        buildFluidLane(level, origin.offset(6, 0, 0));
        buildImpulseLane(level, origin.offset(9, 0, 0));
        buildGeometryLane(level, origin.offset(12, 0, 0));

        source.sendSuccess(() -> Component.literal("Built JetSetCraft vanilla-physics acceptance lab at "
                + origin.toShortString() + " (lanes run south/+Z).").withStyle(ChatFormatting.GREEN), false);
        source.sendSuccess(() -> Component.literal(
                "Lanes: X+0 surfaces | X+3 rails/redstone | X+6 water/bubbles | X+9 slime/honey/impulse hazards | X+12 world geometry."), false);
        source.sendSuccess(() -> Component.literal(
                "Use /jetsetcraft status while riding each section; /jetsetcraft set_momentum <speed> is an operator-only test aid."), false);
        return 1;
    }

    private static void clearLabVolume(ServerLevel level, BlockPos origin) {
        for (int x = -1; x <= 17; x++) {
            for (int z = -1; z <= 70; z++) {
                for (int y = 1; y <= 4; y++) {
                    level.setBlockAndUpdate(origin.offset(x, y, z), Blocks.AIR.defaultBlockState());
                }
            }
        }
    }

    private static void buildSurfaceLane(ServerLevel level, BlockPos origin) {
        for (int z = 0; z <= 67; z++) set(level, origin.offset(0, 0, z), Blocks.SMOOTH_STONE);
        fill(level, origin, 0, 0, 5, Blocks.SMOOTH_STONE);
        fill(level, origin, 0, 6, 11, Blocks.ICE);
        fill(level, origin, 0, 12, 17, Blocks.PACKED_ICE);
        fill(level, origin, 0, 18, 29, Blocks.BLUE_ICE);
        fill(level, origin, 0, 30, 33, Blocks.HONEY_BLOCK);
        fill(level, origin, 0, 34, 39, Blocks.SOUL_SAND);
        fill(level, origin, 0, 40, 43, Blocks.MUD);
        fill(level, origin, 0, 44, 47, Blocks.SNOW_BLOCK);
        fill(level, origin, 0, 48, 50, Blocks.SLIME_BLOCK);
        set(level, origin.offset(0, 1, 51), Blocks.COBWEB);
        fill(level, origin, 0, 52, 54, Blocks.POWDER_SNOW);
        // Micro-height continuity: half-height slab down/up, then real stair collision and thin snow.
        set(level, origin.offset(0, 0, 56), Blocks.STONE_SLAB);
        set(level, origin.offset(0, 0, 57), Blocks.SMOOTH_STONE);
        set(level, origin.offset(0, 0, 58), Blocks.STONE_SLAB);
        set(level, origin.offset(0, 0, 59), Blocks.STONE_STAIRS);
        set(level, origin.offset(0, 0, 60), Blocks.STONE_STAIRS);
        set(level, origin.offset(0, 0, 61), Blocks.SMOOTH_STONE);
        set(level, origin.offset(0, 1, 62), Blocks.SNOW);
        set(level, origin.offset(0, 1, 63), Blocks.SNOW);
        set(level, origin.offset(1, 0, 18), Blocks.LIME_CONCRETE);
        set(level, origin.offset(1, 0, 30), Blocks.YELLOW_CONCRETE);
        set(level, origin.offset(1, 0, 48), Blocks.GREEN_CONCRETE);
    }

    private static void buildRailLane(ServerLevel level, BlockPos origin) {
        for (int z = 0; z <= 27; z++) {
            Block support = (z >= 6 && z <= 11) || z == 17 ? Blocks.REDSTONE_BLOCK : Blocks.SMOOTH_STONE;
            set(level, origin.offset(0, 0, z), support);
            Block rail = z <= 5 || z >= 18 ? Blocks.RAIL
                    : z <= 15 ? Blocks.POWERED_RAIL
                    : z == 16 ? Blocks.DETECTOR_RAIL : Blocks.ACTIVATOR_RAIL;
            set(level, origin.offset(0, 1, z), rail);
        }
        // Detector feedback: ordinary redstone components should react to a JetSetCraft rider.
        set(level, origin.offset(1, 0, 16), Blocks.SMOOTH_STONE);
        set(level, origin.offset(1, 1, 16), Blocks.REDSTONE_LAMP);
        set(level, origin.offset(2, 0, 16), Blocks.SMOOTH_STONE);
        set(level, origin.offset(2, 1, 16), Blocks.NOTE_BLOCK);

        // A small rising rail proves slopes are part of the same continuous rail path.
        for (int z = 28; z <= 31; z++) set(level, origin.offset(0, 0, z), Blocks.SMOOTH_STONE);
        set(level, origin.offset(0, 1, 28), Blocks.RAIL);
        set(level, origin.offset(0, 1, 29), Blocks.SMOOTH_STONE);
        set(level, origin.offset(0, 2, 29), Blocks.RAIL);
        set(level, origin.offset(0, 1, 30), Blocks.SMOOTH_STONE);
        set(level, origin.offset(0, 2, 30), Blocks.RAIL);
        set(level, origin.offset(0, 1, 31), Blocks.SMOOTH_STONE);
        set(level, origin.offset(0, 2, 31), Blocks.RAIL);
    }

    private static void buildFluidLane(ServerLevel level, BlockPos origin) {
        // Contained one-block-deep channel: source water must not flood the rest of the acceptance lab.
        for (int z = -1; z <= 12; z++) {
            set(level, origin.offset(-1, 0, z), Blocks.SMOOTH_STONE);
            set(level, origin.offset(0, 0, z), Blocks.SMOOTH_STONE);
            set(level, origin.offset(1, 0, z), Blocks.SMOOTH_STONE);
            set(level, origin.offset(-1, 1, z), Blocks.GLASS);
            set(level, origin.offset(1, 1, z), Blocks.GLASS);
        }
        set(level, origin.offset(0, 1, -1), Blocks.GLASS);
        set(level, origin.offset(0, 1, 12), Blocks.GLASS);
        for (int z = 0; z <= 11; z++) set(level, origin.offset(0, 1, z), Blocks.WATER);

        // Vanilla Soul Sand/Magma + source-water columns are intentionally used rather than a custom launcher block.
        for (int z : new int[]{14, 18}) {
            set(level, origin.offset(0, 0, z), z == 14 ? Blocks.SOUL_SAND : Blocks.MAGMA_BLOCK);
            for (int y = 1; y <= 4; y++) {
                set(level, origin.offset(0, y, z), Blocks.WATER);
                set(level, origin.offset(-1, y, z), Blocks.GLASS);
                set(level, origin.offset(1, y, z), Blocks.GLASS);
                set(level, origin.offset(0, y, z - 1), Blocks.GLASS);
                set(level, origin.offset(0, y, z + 1), Blocks.GLASS);
            }
        }
    }

    private static void buildImpulseLane(ServerLevel level, BlockPos origin) {
        for (int z = 0; z <= 24; z++) set(level, origin.offset(0, 0, z), Blocks.SMOOTH_STONE);
        fill(level, origin, 0, 4, 7, Blocks.SLIME_BLOCK);
        fill(level, origin, 0, 10, 13, Blocks.HONEY_BLOCK);
        set(level, origin.offset(0, 0, 16), Blocks.MAGMA_BLOCK);
        set(level, origin.offset(0, 0, 18), Blocks.SOUL_CAMPFIRE);
        set(level, origin.offset(0, 0, 20), Blocks.CACTUS);
        set(level, origin.offset(0, -1, 20), Blocks.SAND);
        // This lane deliberately leaves vanilla hazards dangerous. The purpose is to verify that
        // tricks and momentum do not silently grant immunity.
    }

    private static void buildGeometryLane(ServerLevel level, BlockPos origin) {
        // This lane proves that the generic edge solver follows Minecraft collision geometry rather than a
        // tiny whitelist. Connected narrow blocks get real corner lines; broad roofs expose only their outer
        // ledges, not the invisible seams between adjacent full blocks.
        for (int x = -1; x <= 5; x++) {
            for (int z = 0; z <= 66; z++) set(level, origin.offset(x, 0, z), Blocks.SMOOTH_STONE);
        }

        for (int z = 2; z <= 8; z++) set(level, origin.offset(0, 1, z), Blocks.IRON_BARS);
        for (int x = 1; x <= 5; x++) set(level, origin.offset(x, 1, 8), Blocks.IRON_BARS);

        for (int z = 12; z <= 18; z++) set(level, origin.offset(0, 1, z), Blocks.OAK_FENCE);
        for (int x = 1; x <= 4; x++) set(level, origin.offset(x, 1, 18), Blocks.OAK_FENCE);

        for (int z = 22; z <= 28; z++) set(level, origin.offset(0, 1, z), Blocks.COBBLESTONE_WALL);
        for (int x = 1; x <= 4; x++) set(level, origin.offset(x, 1, 28), Blocks.COBBLESTONE_WALL);

        for (int z = 32; z <= 38; z++) set(level, origin.offset(0, 1, z), Blocks.GLASS_PANE);
        for (int x = 1; x <= 4; x++) set(level, origin.offset(x, 1, 38), Blocks.GLASS_PANE);

        // Three-wide rooftop: perimeter edges are valid, block-to-block top seams are intentionally invalid.
        for (int x = 0; x <= 2; x++) {
            for (int z = 43; z <= 50; z++) set(level, origin.offset(x, 1, z), Blocks.SMOOTH_STONE);
        }

        // Natural full-block geometry still participates. The vertical stack specifically regression-tests
        // clearance: lower block tops must not become hidden grind paths through the log above them.
        for (int z = 54; z <= 61; z++) set(level, origin.offset(0, 1, z), Blocks.OAK_LOG);
        set(level, origin.offset(4, 1, 56), Blocks.OAK_LOG);
        set(level, origin.offset(4, 2, 56), Blocks.OAK_LOG);
        set(level, origin.offset(4, 3, 56), Blocks.OAK_LOG);
    }

    private static void fill(ServerLevel level, BlockPos origin, int x, int z0, int z1, Block block) {
        for (int z = z0; z <= z1; z++) set(level, origin.offset(x, 0, z), block);
    }

    private static void set(ServerLevel level, BlockPos pos, Block block) {
        level.setBlockAndUpdate(pos, block.defaultBlockState());
    }

    private JetSetCommands() {}
}
