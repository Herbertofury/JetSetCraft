package com.herberto.jetsetcraft.movement;

import com.herberto.jetsetcraft.config.JetSetConfig;
import com.herberto.jetsetcraft.data.JetSetData;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.Mth;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.VoxelShape;

import com.herberto.jetsetcraft.movement.VanillaWorldPhysics.*;

final class VanillaImpulsePhysics {
    static void captureExternalImpulse(ServerPlayer player, JetSetData data) {
        if (!JetSetConfig.SERVER.enableVanillaWorldPhysics.get()) {
            data.setExternalImpulse(Vec3.ZERO);
            data.setExternalImpulseTicks(0);
            return;
        }

        Vec3 current = EdgeFinder.horizontal(player.getDeltaMovement());
        Vec3 previous = EdgeFinder.horizontal(data.lastSolverVelocity());
        Vec3 delta = current.subtract(previous);
        double currentSpeed = current.length();
        double previousSpeed = previous.length();
        double speedGain = currentSpeed - previousSpeed;
        double shock = delta.length();
        double alignment = currentSpeed > 0.08 && previousSpeed > 0.08
                ? current.normalize().dot(previous.normalize()) : 1.0;

        boolean hasRideInput = Math.abs(data.inputForward()) + Math.abs(data.inputStrafe()) > 0.14;
        // Ordinary Minecraft player travel runs before our END-phase solver. Give active WASD enough
        // allowance that it cannot masquerade as a piston/explosion boost every tick.
        double absoluteGainThreshold = hasRideInput ? 0.12 : 0.055;
        double relativeGainThreshold = hasRideInput ? 0.28 : 0.14;
        boolean launchedFromRest = previousSpeed < 0.055 && currentSpeed > (hasRideInput ? 0.285 : 0.105);
        boolean gainedSpeed = speedGain > Math.max(absoluteGainThreshold, previousSpeed * relativeGainThreshold);
        boolean redirectedHard = shock > 0.17 && currentSpeed > 0.12 && previousSpeed > 0.10 && alignment < 0.45;
        if (launchedFromRest || gainedSpeed || redirectedHard) {
            data.setExternalImpulse(delta);
            data.setExternalImpulseTicks(5);
            data.setMomentum(Math.max(data.momentum(), currentSpeed));
            data.setComboGrace(Math.max(data.comboGrace(), 42));
            return;
        }

        if (data.externalImpulseTicks() > 0) {
            data.setExternalImpulseTicks(data.externalImpulseTicks() - 1);
            data.setExternalImpulse(data.externalImpulse().scale(0.72));
        } else {
            data.setExternalImpulse(Vec3.ZERO);
        }
    }

    /**
     * Server-authoritative micro-step continuation for slabs, stair collision boxes, snow layers and
     * similarly small vanilla/modded height changes. It only runs after Minecraft reports a horizontal
     * collision, only accepts a <= 5/8 block rise at the actual probe point, and verifies the destination
     * bounding box with Minecraft collision before moving. Full blocks, fences, walls and other real
     * obstacles therefore remain obstacles.
     */
    static boolean applyMicroTerrainContinuity(ServerPlayer player, JetSetData data) {
        if (!JetSetConfig.SERVER.enableVanillaWorldPhysics.get() || !JetSetConfig.SERVER.enableMicroTerrainAssist.get()
                || !player.onGround() || !player.horizontalCollision
                || data.grinding() || data.wallRiding() || data.powersliding()
                || data.pressed(com.herberto.jetsetcraft.network.InputFlags.BRAKE) || data.momentum() < 0.15) {
            if (data.terrainAssistCooldown() > 0) data.setTerrainAssistCooldown(data.terrainAssistCooldown() - 1);
            return false;
        }
        if (data.terrainAssistCooldown() > 0) {
            data.setTerrainAssistCooldown(data.terrainAssistCooldown() - 1);
            return false;
        }

        Vec3 horizontal = EdgeFinder.horizontal(player.getDeltaMovement());
        Vec3 direction = horizontal.lengthSqr() > 1.0e-5 ? horizontal.normalize() : JetSetMovement.desiredDirection(player, data);
        if (direction.lengthSqr() < 1.0e-5) direction = JetSetMovement.horizontalLook(player);
        direction = new Vec3(direction.x, 0, direction.z).normalize();

        double probeDistance = Mth.clamp(0.28 + data.momentum() * 0.42, 0.30, 0.58);
        double probeX = player.getX() + direction.x * probeDistance;
        double probeZ = player.getZ() + direction.z * probeDistance;
        BlockPos pos = BlockPos.containing(probeX, player.getY() + 0.04, probeZ);
        BlockState state = player.level().getBlockState(pos);
        VoxelShape shape = state.getCollisionShape(player.level(), pos);
        if (shape.isEmpty()) {
            BlockPos below = pos.below();
            BlockState belowState = player.level().getBlockState(below);
            VoxelShape belowShape = belowState.getCollisionShape(player.level(), below);
            if (!belowShape.isEmpty()) {
                pos = below;
                state = belowState;
                shape = belowShape;
            }
        }
        if (shape.isEmpty()) return false;

        double localX = probeX - pos.getX();
        double localZ = probeZ - pos.getZ();
        double top = Double.NEGATIVE_INFINITY;
        for (AABB part : shape.toAabbs()) {
            if (localX + 0.055 < part.minX || localX - 0.055 > part.maxX
                    || localZ + 0.055 < part.minZ || localZ - 0.055 > part.maxZ) continue;
            top = Math.max(top, pos.getY() + part.maxY);
        }
        if (!Double.isFinite(top)) return false;

        double step = top - player.getY();
        if (step < 0.045 || step > JetSetConfig.SERVER.microTerrainMaxStep.get()) return false;
        double advance = Mth.clamp(0.035 + data.momentum() * 0.12, 0.04, 0.13);
        double dy = step + 0.018;
        AABB destination = player.getBoundingBox().move(direction.x * advance, dy, direction.z * advance);
        if (!player.level().noCollision(player, destination)) return false;

        player.setPos(player.getX() + direction.x * advance, player.getY() + dy, player.getZ() + direction.z * advance);
        Vec3 velocity = player.getDeltaMovement();
        player.setDeltaMovement(velocity.x, Math.max(0.0, velocity.y), velocity.z);
        player.fallDistance = 0;
        player.hurtMarked = true;
        data.setMomentum(Math.max(data.momentum(), horizontal.length()));
        data.setTerrainAssistCooldown(2);
        data.setComboGrace(Math.max(data.comboGrace(), 18));
        return true;
    }

    static double jumpMultiplier(ServerPlayer player) {
        MobEffectInstance jump = player.getEffect(MobEffects.JUMP);
        return jump == null ? 1.0 : 1.0 + 0.12 * (jump.getAmplifier() + 1);
    }

    static double waterRetention(ServerPlayer player, JetSetData data) {
        int depth = VanillaEnchantments.effectiveEnchantmentLevel(player, data, Enchantments.DEPTH_STRIDER);
        boolean dolphins = player.hasEffect(MobEffects.DOLPHINS_GRACE);
        double base = player.isUnderWater() ? 0.88 : 0.94;
        base += Math.min(0.075, depth * 0.025);
        if (dolphins) base = Math.max(base, 0.985);
        return Math.min(0.995, base);
    }

    static boolean applyAirborneSurfaceInteractions(ServerPlayer player, JetSetData data) {
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

    static SurfaceContact findSideContact(ServerPlayer player, SurfaceKind wanted) {
        AABB box = player.getBoundingBox();
        double y = box.minY + Math.min(0.85, Math.max(0.20, box.getYsize() * 0.45));
        BlockPos center = BlockPos.containing(player.getX(), y, player.getZ());
        for (Direction direction : Direction.Plane.HORIZONTAL) {
            BlockPos pos = center.relative(direction);
            BlockState state = player.level().getBlockState(pos);
            SurfaceKind kind = VanillaSurfacePhysics.classify(player, pos, state).kind();
            if (kind != wanted) continue;
            AABB blockBox = new AABB(pos);
            if (!box.inflate(0.045, 0.0, 0.045).intersects(blockBox)) continue;
            Vec3 normal = new Vec3(-direction.getStepX(), 0, -direction.getStepZ());
            return new SurfaceContact(pos, normal);
        }
        return null;
    }

    /** Apply rail block-state semantics while rolling across rails, not only while grind-locked. */
    static void applyLanding(ServerPlayer player, JetSetData data, boolean grounded, Surface surface) {
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

    private VanillaImpulsePhysics() {}
}
