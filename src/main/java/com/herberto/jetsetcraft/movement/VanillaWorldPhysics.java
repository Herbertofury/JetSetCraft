package com.herberto.jetsetcraft.movement;

import com.herberto.jetsetcraft.config.JetSetConfig;
import com.herberto.jetsetcraft.data.JetSetData;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.Mth;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.SnowLayerBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.phys.AABB;
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

    public static Surface ground(ServerPlayer player) {
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

    public static Surface at(ServerPlayer player, Vec3 point) {
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

    private static Surface classify(ServerPlayer player, BlockPos pos, BlockState state) {
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

    private static float safeFriction(ServerPlayer player, BlockPos pos, BlockState state) {
        try {
            return state.getFriction(player.level(), pos, player);
        } catch (Throwable ignored) {
            return state.getBlock().getFriction();
        }
    }

    public static MotionProfile profile(ServerPlayer player, Surface surface) {
        if (!JetSetConfig.SERVER.enableVanillaWorldPhysics.get()) return MotionProfile.DEFAULT;
        MotionProfile base = switch (surface.kind()) {
            case ICE -> new MotionProfile(0.90, 1.34, 1.40, 0.9987, 0.82, 0.0025, 1.0);
            case PACKED_ICE -> new MotionProfile(0.96, 1.62, 1.72, 0.99925, 0.69, 0.0045, 1.0);
            case BLUE_ICE -> new MotionProfile(1.02, JetSetConfig.SERVER.blueIceSpeedMultiplier.get(),
                    JetSetConfig.SERVER.blueIceSpeedMultiplier.get() * 1.08, 0.99962, 0.54, 0.0075, 1.0);
            case FROSTED_ICE -> new MotionProfile(0.88, 1.30, 1.36, 0.9985, 0.78, 0.0020, 1.0);
            case HONEY -> new MotionProfile(0.48, 0.48, 0.50, 0.875, 0.72, 0.0, 0.72);
            case SOUL -> soulProfile(player);
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


    private static MotionProfile snowProfile(Surface surface) {
        int layers = surface.state().hasProperty(SnowLayerBlock.LAYERS)
                ? surface.state().getValue(SnowLayerBlock.LAYERS) : 8;
        double depth = Mth.clamp(layers / 8.0, 0.125, 1.0);
        return new MotionProfile(0.91 - 0.22 * depth, 0.96 - 0.25 * depth, 0.98 - 0.24 * depth,
                0.989 - 0.040 * depth, 0.96 - 0.17 * depth, 0.0, 0.97 - 0.12 * depth);
    }

    private static MotionProfile lowFrictionProfile(float friction) {
        double t = Mth.clamp((friction - 0.82) / 0.18, 0.0, 1.0);
        return new MotionProfile(0.92 + 0.08 * t,
                1.18 + 0.52 * t, 1.24 + 0.58 * t,
                0.9975 + 0.0020 * t, 0.86 - 0.25 * t,
                0.0015 + 0.0040 * t, 1.0);
    }

    private static MotionProfile soulProfile(ServerPlayer player) {
        int level = EnchantmentHelper.getEnchantmentLevel(Enchantments.SOUL_SPEED, player);
        if (level <= 0) return new MotionProfile(0.55, 0.62, 0.66, 0.91, 0.76, 0.0, 0.82);
        double bonus = 1.0 + level * 0.12;
        return new MotionProfile(0.98 + level * 0.08, bonus, bonus * 1.04,
                0.993 + Math.min(0.004, level * 0.0012), 0.96, 0.0015 * level, 1.0);
    }

    private static MotionProfile applyStatusEffects(ServerPlayer player, MotionProfile profile) {
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

    public static double jumpMultiplier(ServerPlayer player) {
        MobEffectInstance jump = player.getEffect(MobEffects.JUMP);
        return jump == null ? 1.0 : 1.0 + 0.12 * (jump.getAmplifier() + 1);
    }

    public static double waterRetention(ServerPlayer player) {
        int depth = EnchantmentHelper.getEnchantmentLevel(Enchantments.DEPTH_STRIDER, player);
        boolean dolphins = player.hasEffect(MobEffects.DOLPHINS_GRACE);
        double base = player.isUnderWater() ? 0.88 : 0.94;
        base += Math.min(0.075, depth * 0.025);
        if (dolphins) base = Math.max(base, 0.985);
        return Math.min(0.995, base);
    }

    /**
     * Preserve Minecraft's side-contact language while airborne. Honey becomes a controlled sticky
     * wall stall/slide and slime can rebound a rider without deleting the incoming combo/momentum.
     * These are deliberately contact driven instead of global movement modes.
     */
    public static boolean applyAirborneSurfaceInteractions(ServerPlayer player, JetSetData data) {
        if (!JetSetConfig.SERVER.enableVanillaWorldPhysics.get()) return false;
        boolean handled = false;
        if (data.surfaceInteractionCooldown() > 0) {
            data.setSurfaceInteractionCooldown(data.surfaceInteractionCooldown() - 1);
        }

        SurfaceContact honey = findSideContact(player, SurfaceKind.HONEY);
        if (honey != null) {
            Vec3 velocity = player.getDeltaMovement();
            Vec3 horizontal = new Vec3(velocity.x, 0, velocity.z);
            if (data.justPressed(com.herberto.jetsetcraft.network.InputFlags.JUMP)) {
                double kick = Math.max(0.22, Math.min(0.52, data.momentum() * 0.35));
                player.setDeltaMovement(horizontal.scale(0.72).add(honey.normal().scale(kick))
                        .add(0, 0.29 * jumpMultiplier(player), 0));
                data.setMomentum(Math.max(horizontal.length() * 0.78, data.momentum() * 0.84));
                data.setComboGrace(Math.max(data.comboGrace(), 68));
                data.setSurfaceInteractionCooldown(5);
            } else if (velocity.y < -0.035) {
                double retained = Math.max(horizontal.length(), data.momentum()) * 0.90;
                Vec3 along = horizontal.lengthSqr() > 1.0e-6 ? horizontal.normalize().scale(retained) : Vec3.ZERO;
                player.setDeltaMovement(along.x, Math.max(velocity.y, -0.075), along.z);
                data.setMomentum(retained);
                data.setComboGrace(Math.max(data.comboGrace(), 36));
                player.fallDistance = 0;
            }
            player.hurtMarked = true;
            handled = true;
        }

        if (player.horizontalCollision && data.surfaceInteractionCooldown() == 0) {
            SurfaceContact slime = findSideContact(player, SurfaceKind.SLIME);
            if (slime != null) {
                long key = slime.pos().asLong();
                if (data.lastSideBouncePos() != key) {
                    Vec3 velocity = player.getDeltaMovement();
                    Vec3 horizontal = new Vec3(velocity.x, 0, velocity.z);
                    Vec3 incoming = horizontal.lengthSqr() > 1.0e-5 ? horizontal.normalize()
                            : JetSetMovement.desiredDirection(player, data);
                    if (incoming.lengthSqr() < 1.0e-5) incoming = slime.normal().scale(-1);
                    else incoming = incoming.normalize();
                    Vec3 normal = slime.normal();
                    double dot = incoming.dot(normal);
                    Vec3 reflected = dot < -0.02 ? incoming.subtract(normal.scale(2.0 * dot)) : normal;
                    reflected = reflected.lengthSqr() > 1.0e-6 ? reflected.normalize() : normal;
                    double speed = Math.max(0.20, Math.max(horizontal.length(), data.momentum()) * 0.92);
                    player.setDeltaMovement(reflected.x * speed, Math.max(velocity.y, 0.085), reflected.z * speed);
                    data.setMomentum(speed);
                    data.setComboGrace(Math.max(data.comboGrace(), 72));
                    data.setLastSideBouncePos(key);
                    data.setSurfaceInteractionCooldown(5);
                    player.hurtMarked = true;
                    handled = true;
                }
            } else {
                data.setLastSideBouncePos(Long.MIN_VALUE);
            }
        } else if (!player.horizontalCollision) {
            data.setLastSideBouncePos(Long.MIN_VALUE);
        }
        return handled;
    }

    private record SurfaceContact(BlockPos pos, Vec3 normal) {}

    private static SurfaceContact findSideContact(ServerPlayer player, SurfaceKind wanted) {
        AABB box = player.getBoundingBox();
        double y = box.minY + Math.min(0.85, Math.max(0.20, box.getYsize() * 0.45));
        BlockPos center = BlockPos.containing(player.getX(), y, player.getZ());
        for (Direction direction : Direction.Plane.HORIZONTAL) {
            BlockPos pos = center.relative(direction);
            BlockState state = player.level().getBlockState(pos);
            SurfaceKind kind = classify(player, pos, state).kind();
            if (kind != wanted) continue;
            AABB blockBox = new AABB(pos);
            if (!box.inflate(0.045, 0.0, 0.045).intersects(blockBox)) continue;
            Vec3 normal = new Vec3(-direction.getStepX(), 0, -direction.getStepZ());
            return new SurfaceContact(pos, normal);
        }
        return null;
    }

    /** Apply rail block-state semantics while rolling across rails, not only while grind-locked. */
    public static void applyGroundRailInteractions(ServerPlayer player, JetSetData data, Surface surface) {
        if (!JetSetConfig.SERVER.enableVanillaWorldPhysics.get()) return;
        BlockState state = surface.state();
        Block block = state.getBlock();
        long posKey = surface.pos().asLong();
        boolean powered = state.hasProperty(BlockStateProperties.POWERED) && state.getValue(BlockStateProperties.POWERED);

        if (block == Blocks.DETECTOR_RAIL) {
            if (data.lastDetectorRailPos() != posKey) {
                data.setLastDetectorRailPos(posKey);
                pulseDetector(player, surface.pos(), state);
            }
        } else {
            data.setLastDetectorRailPos(Long.MIN_VALUE);
        }

        if (block == Blocks.ACTIVATOR_RAIL && powered) {
            if (data.lastActivatorRailPos() != posKey) {
                data.setLastActivatorRailPos(posKey);
                Vec3 v = player.getDeltaMovement();
                player.setDeltaMovement(v.x, Math.max(v.y, 0.27 * jumpMultiplier(player)), v.z);
                player.hurtMarked = true;
                data.setComboGrace(Math.max(data.comboGrace(), 58));
            }
        } else {
            data.setLastActivatorRailPos(Long.MIN_VALUE);
        }
    }

    /** Apply block-specific landing behavior while preserving incoming horizontal momentum. */
    public static void applyLanding(ServerPlayer player, JetSetData data, boolean grounded, Surface surface) {
        if (!JetSetConfig.SERVER.enableVanillaWorldPhysics.get() || !grounded || data.wasGrounded()) return;
        if (surface.kind() == SurfaceKind.SLIME && !player.isShiftKeyDown() && data.lastVerticalVelocity() < -0.16) {
            double bounce = Math.min(1.10, -data.lastVerticalVelocity() * JetSetConfig.SERVER.slimeBounceMultiplier.get());
            Vec3 v = player.getDeltaMovement();
            player.setDeltaMovement(v.x, Math.max(v.y, bounce), v.z);
            player.fallDistance = 0;
            player.hurtMarked = true;
            data.setComboGrace(Math.max(data.comboGrace(), 72));
        }
    }

    /**
     * Powered rails boost, unpowered powered rails brake, detector rails emit a short redstone pulse,
     * and powered activator rails become a one-shot action/launch pulse per crossed block.
     */
    public static RailEffect applyVanillaRail(ServerPlayer player, JetSetData data, GrindTarget target, double speed) {
        if (!JetSetConfig.SERVER.enableVanillaWorldPhysics.get() || !target.kind().rail()) return new RailEffect(speed, false);
        Surface surface = at(player, target.point());
        BlockState state = surface.state();
        Block block = state.getBlock();
        boolean powered = state.hasProperty(BlockStateProperties.POWERED) && state.getValue(BlockStateProperties.POWERED);

        if (block == Blocks.POWERED_RAIL) {
            if (powered) {
                double cap = data.style().boostCap() * JetSetConfig.SERVER.speedScale.get() * 1.32;
                if (speed < cap) speed = Math.min(cap, speed + JetSetConfig.SERVER.poweredRailBoostPerTick.get());
            } else {
                speed *= JetSetConfig.SERVER.unpoweredRailRetention.get();
            }
        }

        long posKey = surface.pos().asLong();
        if (block == Blocks.DETECTOR_RAIL) {
            if (data.lastDetectorRailPos() != posKey) {
                data.setLastDetectorRailPos(posKey);
                pulseDetector(player, surface.pos(), state);
            }
        } else {
            data.setLastDetectorRailPos(Long.MIN_VALUE);
        }

        if (block == Blocks.ACTIVATOR_RAIL && powered) {
            if (data.lastActivatorRailPos() != posKey) {
                data.setLastActivatorRailPos(posKey);
                return new RailEffect(speed, true);
            }
        } else {
            data.setLastActivatorRailPos(Long.MIN_VALUE);
        }
        return new RailEffect(speed, false);
    }

    private static void pulseDetector(ServerPlayer player, BlockPos pos, BlockState state) {
        if (!(player.level() instanceof ServerLevel level) || !state.hasProperty(BlockStateProperties.POWERED)) return;
        if (!state.getValue(BlockStateProperties.POWERED)) {
            BlockState powered = state.setValue(BlockStateProperties.POWERED, true);
            level.setBlock(pos, powered, 3);
            level.updateNeighborsAt(pos, powered.getBlock());
            level.updateNeighborsAt(pos.below(), powered.getBlock());
            level.scheduleTick(pos, powered.getBlock(), 8);
        }
    }

    private VanillaWorldPhysics() {}
}
