package com.herberto.jetsetcraft.movement;

import com.herberto.jetsetcraft.config.JetSetConfig;
import com.herberto.jetsetcraft.data.JetSetData;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.Mth;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.SnowLayerBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.phys.Vec3;

import com.herberto.jetsetcraft.movement.VanillaWorldPhysics.*;

final class VanillaSurfacePhysics {
    static Surface ground(ServerPlayer player) {
        // Body-contact traps must win over the floor under them. Cobweb and powder snow commonly have
        // nonstandard collision, so sampling only the block under the feet would let JetSet momentum
        // incorrectly power straight through them.
        BlockPos bodyPos = BlockPos.containing(player.getX(), player.getBoundingBox().minY + 0.18, player.getZ());
        BlockState bodyState = player.level().getBlockState(bodyPos);
        if (bodyState.getBlock() == Blocks.COBWEB || bodyState.getBlock() == Blocks.POWDER_SNOW) {
            return classify(player, bodyPos, bodyState);
        }

        BlockPos pos = BlockPos.containing(player.getX(), player.getBoundingBox().minY - 0.08, player.getZ());
        BlockState state = player.level().getBlockState(pos);
        if (state.isAir() || state.getCollisionShape(player.level(), pos).isEmpty()) {
            BlockPos below = pos.below();
            BlockState belowState = player.level().getBlockState(below);
            if (!belowState.isAir()) {
                pos = below;
                state = belowState;
            }
        }
        return classify(player, pos, state);
    }

    static Surface at(ServerPlayer player, Vec3 point) {
        BlockPos pos = BlockPos.containing(point.x, point.y - 0.06, point.z);
        BlockState state = player.level().getBlockState(pos);
        if (state.isAir()) {
            BlockPos below = pos.below();
            BlockState belowState = player.level().getBlockState(below);
            if (!belowState.isAir()) {
                pos = below;
                state = belowState;
            }
        }
        return classify(player, pos, state);
    }

    static Surface classify(ServerPlayer player, BlockPos pos, BlockState state) {
        Block block = state.getBlock();
        SurfaceKind kind;
        if (block == Blocks.BLUE_ICE) kind = SurfaceKind.BLUE_ICE;
        else if (block == Blocks.PACKED_ICE) kind = SurfaceKind.PACKED_ICE;
        else if (block == Blocks.ICE) kind = SurfaceKind.ICE;
        else if (block == Blocks.FROSTED_ICE) kind = SurfaceKind.FROSTED_ICE;
        else if (block == Blocks.SLIME_BLOCK || state.is(JetSetTags.BOUNCE_SURFACES)) kind = SurfaceKind.SLIME;
        else if (block == Blocks.HONEY_BLOCK || state.is(JetSetTags.STICKY_SURFACES)) kind = SurfaceKind.HONEY;
        else if (block == Blocks.SOUL_SAND || block == Blocks.SOUL_SOIL) kind = SurfaceKind.SOUL;
        else if (block == Blocks.POWDER_SNOW) kind = SurfaceKind.POWDER_SNOW;
        else if (block == Blocks.SNOW || block == Blocks.SNOW_BLOCK) kind = SurfaceKind.SNOW;
        else if (block == Blocks.COBWEB) kind = SurfaceKind.COBWEB;
        else if (block == Blocks.MUD) kind = SurfaceKind.MUD;
        else {
            float friction = safeFriction(player, pos, state);
            kind = state.is(JetSetTags.LOW_FRICTION_SURFACES) || friction >= 0.90f
                    ? SurfaceKind.MODDED_LOW_FRICTION : SurfaceKind.DEFAULT;
            return new Surface(pos, state, kind, friction);
        }
        return new Surface(pos, state, kind, safeFriction(player, pos, state));
    }

    static float safeFriction(ServerPlayer player, BlockPos pos, BlockState state) {
        try {
            return state.getFriction(player.level(), pos, player);
        } catch (Throwable ignored) {
            return state.getBlock().getFriction();
        }
    }

    /**
     * Detects momentum injected after JetSetCraft's previous server-tick solve. Minecraft player travel
     * and normal friction can perturb velocity slightly, so only a meaningful speed gain or a strong
     * directional shock is classified as an external impulse. The short preservation window lets the
     * rider steer out of an explosion/piston/knockback launch instead of instantly snapping back to the
     * input direction.
     */
    static MotionProfile profile(ServerPlayer player, JetSetData data, Surface surface) {
        if (!JetSetConfig.SERVER.enableVanillaWorldPhysics.get()) return MotionProfile.DEFAULT;
        MotionProfile base = switch (surface.kind()) {
            case ICE -> new MotionProfile(0.90, 1.34, 1.40, 0.9987, 0.82, 0.0025, 1.0);
            case PACKED_ICE -> new MotionProfile(0.96, 1.62, 1.72, 0.99925, 0.69, 0.0045, 1.0);
            case BLUE_ICE -> new MotionProfile(1.02, JetSetConfig.SERVER.blueIceSpeedMultiplier.get(),
                    JetSetConfig.SERVER.blueIceSpeedMultiplier.get() * 1.08, 0.99962, 0.54, 0.0075, 1.0);
            case FROSTED_ICE -> new MotionProfile(0.88, 1.30, 1.36, 0.9985, 0.78, 0.0020, 1.0);
            case HONEY -> new MotionProfile(0.48, 0.48, 0.50, 0.875, 0.72, 0.0, 0.72);
            case SOUL -> soulProfile(player, data);
            case SNOW -> snowProfile(surface);
            case POWDER_SNOW -> new MotionProfile(0.40, 0.46, 0.48, 0.82, 0.62, 0.0, 0.72);
            case COBWEB -> new MotionProfile(0.12, 0.18, 0.20, 0.54, 0.48, 0.0, 0.50);
            case MUD -> new MotionProfile(0.65, 0.70, 0.74, 0.94, 0.78, 0.0, 0.86);
            case MODDED_LOW_FRICTION -> lowFrictionProfile(surface.vanillaFriction());
            case SLIME, DEFAULT -> MotionProfile.DEFAULT;
        };

        if (surface.state().getBlock() == Blocks.POWERED_RAIL && surface.state().hasProperty(BlockStateProperties.POWERED)) {
            if (surface.state().getValue(BlockStateProperties.POWERED)) {
                base = new MotionProfile(1.18, Math.max(base.cruiseCapMultiplier(), 1.28),
                        Math.max(base.boostCapMultiplier(), 1.36), Math.max(base.coastingRetention(), 0.9975),
                        base.steeringMultiplier(), base.passiveAssistPerTick() + JetSetConfig.SERVER.poweredRailBoostPerTick.get(), 1.0);
            } else {
                base = new MotionProfile(0.54, 0.62, 0.66, JetSetConfig.SERVER.unpoweredRailRetention.get(),
                        0.88, 0.0, 0.74);
            }
        }

        boolean genericSurface = surface.kind() == SurfaceKind.DEFAULT || surface.kind() == SurfaceKind.MODDED_LOW_FRICTION;
        if (genericSurface && surface.state().is(JetSetTags.BOOST_SURFACES)) {
            base = new MotionProfile(base.accelerationMultiplier() * 1.10,
                    Math.max(base.cruiseCapMultiplier(), 1.18), Math.max(base.boostCapMultiplier(), 1.22),
                    Math.max(base.coastingRetention(), 0.996), base.steeringMultiplier(),
                    base.passiveAssistPerTick() + 0.0045, base.brakeMultiplier());
        }
        if (genericSurface && surface.state().is(JetSetTags.BRAKE_SURFACES)) {
            base = new MotionProfile(base.accelerationMultiplier() * 0.72,
                    base.cruiseCapMultiplier() * 0.78, base.boostCapMultiplier() * 0.80,
                    Math.min(base.coastingRetention(), 0.93), base.steeringMultiplier() * 0.92,
                    0.0, Math.min(base.brakeMultiplier(), 0.82));
        }
        return applyStatusEffects(player, base);
    }

    static MotionProfile snowProfile(Surface surface) {
        int layers = surface.state().hasProperty(SnowLayerBlock.LAYERS)
                ? surface.state().getValue(SnowLayerBlock.LAYERS) : 8;
        double depth = Mth.clamp(layers / 8.0, 0.125, 1.0);
        return new MotionProfile(0.91 - 0.22 * depth, 0.96 - 0.25 * depth, 0.98 - 0.24 * depth,
                0.989 - 0.040 * depth, 0.96 - 0.17 * depth, 0.0, 0.97 - 0.12 * depth);
    }

    static MotionProfile lowFrictionProfile(float friction) {
        double t = Mth.clamp((friction - 0.82) / 0.18, 0.0, 1.0);
        return new MotionProfile(0.92 + 0.08 * t,
                1.18 + 0.52 * t, 1.24 + 0.58 * t,
                0.9975 + 0.0020 * t, 0.86 - 0.25 * t,
                0.0015 + 0.0040 * t, 1.0);
    }

    static MotionProfile soulProfile(ServerPlayer player, JetSetData data) {
        int level = VanillaEnchantments.effectiveEnchantmentLevel(player, data, Enchantments.SOUL_SPEED);
        if (level <= 0) return new MotionProfile(0.55, 0.62, 0.66, 0.91, 0.76, 0.0, 0.82);
        double bonus = 1.0 + level * 0.12;
        return new MotionProfile(0.98 + level * 0.08, bonus, bonus * 1.04,
                0.993 + Math.min(0.004, level * 0.0012), 0.96, 0.0015 * level, 1.0);
    }

    static MotionProfile applyStatusEffects(ServerPlayer player, MotionProfile profile) {
        double accel = profile.accelerationMultiplier();
        double cruise = profile.cruiseCapMultiplier();
        double boost = profile.boostCapMultiplier();

        MobEffectInstance speed = player.getEffect(MobEffects.MOVEMENT_SPEED);
        if (speed != null) {
            int amp = speed.getAmplifier() + 1;
            accel *= 1.0 + 0.16 * amp;
            cruise *= 1.0 + 0.10 * amp;
            boost *= 1.0 + 0.08 * amp;
        }
        MobEffectInstance slow = player.getEffect(MobEffects.MOVEMENT_SLOWDOWN);
        if (slow != null) {
            int amp = slow.getAmplifier() + 1;
            double factor = Math.max(0.28, 1.0 - 0.14 * amp);
            accel *= factor;
            cruise *= Math.max(0.42, 1.0 - 0.10 * amp);
            boost *= Math.max(0.50, 1.0 - 0.08 * amp);
        }
        return new MotionProfile(accel, cruise, boost, profile.coastingRetention(),
                profile.steeringMultiplier(), profile.passiveAssistPerTick(), profile.brakeMultiplier());
    }

    /**
     * Vanilla enchantment lookup plus JetSetCraft's dedicated footwear slot. The maximum wins rather
     * than summing levels, so putting the same enchantment on boots and skates cannot create an
     * exponential/double-application exploit.
     */

    private VanillaSurfacePhysics() {}
}
