package com.herberto.jetsetcraft.movement;

import com.herberto.jetsetcraft.data.JetSetData;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;

/**
 * Bridges JetSetCraft's momentum model with Minecraft's existing material, block-state, fluid,
 * redstone, enchantment and status-effect language. It deliberately modifies momentum instead of
 * replacing vanilla impulses, so explosions, pistons, currents, slime and other emergent mechanics
 * can become legitimate movement tech.
 */
public final class VanillaWorldPhysics {
    public enum SurfaceKind {
        DEFAULT, ICE, PACKED_ICE, BLUE_ICE, FROSTED_ICE, SLIME, HONEY,
        SOUL, SNOW, POWDER_SNOW, COBWEB, MUD, MODDED_LOW_FRICTION
    }

    public record Surface(BlockPos pos, BlockState state, SurfaceKind kind, float vanillaFriction) {}

    public record MotionProfile(double accelerationMultiplier,
                                double cruiseCapMultiplier,
                                double boostCapMultiplier,
                                double coastingRetention,
                                double steeringMultiplier,
                                double passiveAssistPerTick,
                                double brakeMultiplier) {
        public static final MotionProfile DEFAULT = new MotionProfile(1.0, 1.0, 1.0, 0.993, 1.0, 0.0, 1.0);
    }

    public record RailEffect(double speed, boolean launch) {}

    public enum GrindMaterialKind {
        METAL, COPPER, GLASS, WOOD, STONE, ICE, SLIME, HONEY, GENERIC
    }

    /**
     * Material and geometry are intentionally separate. The path solver decides where a grind line
     * exists; this profile decides how that line feels. Modded blocks can therefore inherit useful
     * behavior through Minecraft's own material/sound language instead of hard-coded IDs.
     */
    public record GrindMaterialProfile(GrindMaterialKind kind, double capMultiplier, double retention,
                                       double passiveGain, boolean sparks, float soundPitch, SoundType sound) {}

    public static Surface ground(ServerPlayer player) { return VanillaSurfacePhysics.ground(player); }
    public static Surface at(ServerPlayer player, Vec3 point) { return VanillaSurfacePhysics.at(player, point); }
    public static void captureExternalImpulse(ServerPlayer player, JetSetData data) { VanillaImpulsePhysics.captureExternalImpulse(player, data); }
    public static boolean applyMicroTerrainContinuity(ServerPlayer player, JetSetData data) { return VanillaImpulsePhysics.applyMicroTerrainContinuity(player, data); }
    public static MotionProfile profile(ServerPlayer player, JetSetData data, Surface surface) { return VanillaSurfacePhysics.profile(player, data, surface); }
    public static int effectiveEnchantmentLevel(ServerPlayer player, JetSetData data, Enchantment enchantment) { return VanillaEnchantments.effectiveEnchantmentLevel(player, data, enchantment); }
    public static void applyRideEnchantments(ServerPlayer player, JetSetData data) { VanillaEnchantments.applyRideEnchantments(player, data); }
    public static float augmentFallDamageProtection(ServerPlayer player, JetSetData data, DamageSource source, float amount) { return VanillaEnchantments.augmentFallDamageProtection(player, data, source, amount); }
    public static double jumpMultiplier(ServerPlayer player) { return VanillaImpulsePhysics.jumpMultiplier(player); }
    public static double waterRetention(ServerPlayer player, JetSetData data) { return VanillaImpulsePhysics.waterRetention(player, data); }
    public static GrindMaterialProfile grindMaterial(ServerPlayer player, GrindTarget target) { return VanillaGrindPhysics.grindMaterial(player, target); }
    public static void emitGrindFeedback(ServerPlayer player, GrindTarget target, GrindMaterialProfile material) { VanillaGrindPhysics.emitGrindFeedback(player, target, material); }
    public static boolean applyAirborneSurfaceInteractions(ServerPlayer player, JetSetData data) { return VanillaImpulsePhysics.applyAirborneSurfaceInteractions(player, data); }
    public static void applyGroundRailInteractions(ServerPlayer player, JetSetData data, Surface surface) { VanillaRailPhysics.applyGroundRailInteractions(player, data, surface); }
    public static void applyLanding(ServerPlayer player, JetSetData data, boolean grounded, Surface surface) { VanillaImpulsePhysics.applyLanding(player, data, grounded, surface); }
    public static RailEffect applyVanillaRail(ServerPlayer player, JetSetData data, GrindTarget target, double speed) { return VanillaRailPhysics.applyVanillaRail(player, data, target, speed); }

    private VanillaWorldPhysics() {}
}
