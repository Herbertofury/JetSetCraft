package com.herberto.jetsetcraft.movement;

import com.herberto.jetsetcraft.data.JetSetData;
import com.herberto.jetsetcraft.network.InputFlags;
import net.minecraft.core.particles.BlockParticleOption;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

/** Intentional kickoff and ledge-vault parkour adapted from Spirit Vector's movement-state design. */
public final class ParkourTraversal {
    static void tick(ServerPlayer player, JetSetData data, boolean grounded) {
        if (data.parkourCooldown() > 0) data.setParkourCooldown(data.parkourCooldown() - 1);
        if (grounded) {
            data.setLastWallPlane(Long.MIN_VALUE);
            if (data.justPressed(InputFlags.JUMP) && data.pressed(InputFlags.SPRINT)) tryKickoff(player, data);
            return;
        }
        if (data.parkourCooldown() == 0 && !data.grinding() && !data.wallRiding()
                && data.justPressed(InputFlags.JUMP)) {
            tryLedgeVault(player, data);
        }
    }

    public static boolean tryKickoff(ServerPlayer player, JetSetData data) {
        Vec3 desired = MovementMath.desiredDirection(player, data);
        if (desired.lengthSqr() < 1.0e-5) return false;
        Vec3 old = player.getDeltaMovement();
        Vec3 horizontal = EdgeFinder.horizontal(old);
        double speed = Math.max(horizontal.length(), data.momentum());
        double resultSpeed = Math.max(0.26D, speed + 0.18D);
        Vec3 direction = desired.normalize();
        player.setDeltaMovement(direction.x * resultSpeed, Math.max(old.y, 0.44D), direction.z * resultSpeed);
        player.hurtMarked = true;
        data.setMomentum(resultSpeed);
        data.setParkourCooldown(8);
        data.setComboGrace(MovementTuning.COMBO_GRACE_TICKS);
        TrickCombo.addStyle(data, 70, 0.045F);
        return true;
    }

    public static boolean tryLedgeVault(ServerPlayer player, JetSetData data) {
        Vec3 desired = MovementMath.desiredDirection(player, data);
        if (desired.lengthSqr() < 1.0e-5) return false;
        Vec3 direction = desired.normalize();
        Vec3 feet = player.position();
        Vec3 lowerStart = feet.add(0, Math.min(0.48D, player.getBbHeight() * 0.28D), 0);
        Vec3 upperStart = feet.add(0, Math.min(1.42D, player.getBbHeight() * 0.78D), 0);
        BlockHitResult lower = player.level().clip(new ClipContext(lowerStart, lowerStart.add(direction.scale(0.82D)),
                ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, player));
        BlockHitResult upper = player.level().clip(new ClipContext(upperStart, upperStart.add(direction.scale(0.82D)),
                ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, player));
        if (lower.getType() != HitResult.Type.BLOCK || upper.getType() == HitResult.Type.BLOCK) return false;

        // Clear the top plane of a full one-block ledge before accepting the vault. A sub-block probe here makes
        // the destination AABB overlap the very obstacle we just identified, silently rejecting every real ledge.
        Vec3 lift = direction.scale(0.42D).add(0, 1.08D, 0);
        AABB destination = player.getBoundingBox().move(lift);
        if (!player.level().noCollision(player, destination)) return false;
        Vec3 old = player.getDeltaMovement();
        double speed = Math.max(0.28D, Math.max(EdgeFinder.horizontal(old).length(), data.momentum()));
        player.setDeltaMovement(direction.x * speed, Math.max(0.54D, old.y + 0.22D), direction.z * speed);
        player.fallDistance = 0;
        player.hurtMarked = true;
        data.setMomentum(speed);
        data.setParkourCooldown(12);
        data.setWindTicks(8);
        data.setWindBias(direction);
        data.setComboGrace(MovementTuning.COMBO_GRACE_TICKS);
        TrickCombo.addStyle(data, 115, 0.07F);

        if (player.level() instanceof ServerLevel server) {
            var state = server.getBlockState(lower.getBlockPos());
            server.sendParticles(new BlockParticleOption(ParticleTypes.BLOCK, state),
                    lower.getLocation().x, lower.getLocation().y, lower.getLocation().z,
                    10, 0.18D, 0.22D, 0.18D, 0.08D);
        }
        return true;
    }

    private ParkourTraversal() { }
}
