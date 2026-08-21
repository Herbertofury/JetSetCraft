package com.herberto.jetsetcraft.movement;

import com.herberto.jetsetcraft.config.JetSetConfig;
import com.herberto.jetsetcraft.data.JetSetData;
import com.herberto.jetsetcraft.network.InputFlags;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;

final class GrindTraversal {
    static boolean tryStartGrinding(ServerPlayer player, JetSetData data, Vec3 horizontalVelocity) {
        Vec3 preferred = horizontalVelocity.lengthSqr() > 1.0e-6 ? horizontalVelocity : MovementMath.desiredDirection(player, data);
        if (preferred.lengthSqr() < 1.0e-6) preferred = MovementMath.horizontalLook(player);
        var target = GrindFinder.findBest(player, preferred, GrindKind.NONE);
        if (target.isEmpty()) return false;

        GrindTarget hit = target.get();
        data.setGrinding(true);
        data.setGrindKind(hit.kind());
        data.setWallRiding(false);
        data.setGrindDirection(hit.tangent());
        data.setGrindGrace(hit.kind() == GrindKind.CREATE_TRACK ? 14 : hit.kind().rail() ? 10 : 5);
        data.setGrindStuckTicks(0);
        data.setGrindCurveFactor(1.0);
        double speed = Math.max(MovementTuning.MIN_GRIND_SPEED, Math.max(horizontalVelocity.length(), data.momentum())) * data.style().grindMultiplier();
        data.setMomentum(speed);
        snapToGrind(player, hit, speed);
        TrickCombo.addStyle(data, hit.kind().rail() ? 155 : 120, hit.kind().rail() ? 0.13f : 0.10f);
        return true;
    }

    static boolean continueGrinding(ServerPlayer player, JetSetData data) {
        if (!data.pressed(InputFlags.GRIND)) {
            launchFromGrind(player, data, false);
            return false;
        }

        Vec3 preferred = data.grindDirection().lengthSqr() > 1.0e-5
                ? data.grindDirection() : EdgeFinder.horizontal(player.getDeltaMovement());
        Vec3 desired = MovementMath.desiredDirection(player, data);
        if (desired.lengthSqr() > 0.05) {
            if (data.grindKind().rail()) {
                // Rail input biases switches/junctions without snapping the rider away from the current track.
                preferred = MovementMath.safeNormalize(preferred.scale(0.82).add(desired.normalize().scale(0.44)), preferred);
            } else if (data.grindKind() == GrindKind.EDGE) {
                // Exposed world edges need stronger intentional steering so a fence/wall/ledge can take a
                // real 90-degree corner. With no input the current tangent still wins, so corners remain skillful.
                preferred = MovementMath.safeNormalize(preferred.scale(0.58).add(desired.normalize().scale(0.82)), preferred);
            }
        }

        var target = GrindFinder.findBest(player, preferred, data.grindKind());
        if (target.isEmpty()) {
            if (data.grindGrace() > 0) {
                data.setGrindGrace(data.grindGrace() - 1);
                double projected = Math.abs(player.getDeltaMovement().dot(MovementMath.safeNormalize(preferred, MovementMath.horizontalLook(player))));
                double speed = Math.max(data.momentum(), projected);
                Vec3 dir = MovementMath.safeNormalize(preferred, MovementMath.horizontalLook(player));
                player.setDeltaMovement(dir.scale(speed).add(0, 0.035, 0));
                player.fallDistance = 0;
                player.hurtMarked = true;
                return true;
            }
            launchFromGrind(player, data, true);
            return false;
        }

        GrindTarget hit = target.get();
        GrindKind previousKind = data.grindKind();
        data.setGrindKind(hit.kind());
        data.setGrindGrace(hit.kind() == GrindKind.CREATE_TRACK ? 14 : hit.kind().rail() ? 10 : 5);
        data.setGrindDirection(hit.tangent());

        VanillaWorldPhysics.GrindMaterialProfile material = VanillaWorldPhysics.grindMaterial(player, hit);
        double cap = data.style().boostCap() * JetSetConfig.SERVER.speedScale.get() * material.capMultiplier();
        double pathSpeed = Math.abs(player.getDeltaMovement().dot(hit.tangent()));
        double baseSpeed = Math.max(MovementTuning.MIN_GRIND_SPEED, Math.max(data.momentum(), pathSpeed));
        double speed = baseSpeed < cap
                ? Math.min(cap, baseSpeed + 0.0018 + material.passiveGain())
                : baseSpeed * material.retention();

        // Borrow the mature rail-mod idea of curve easing, but do not destroy momentum: the curve factor only
        // shapes instantaneous travel speed and smoothly returns to 1.0 on exit.
        double desiredCurveFactor = 1.0 - Math.min(0.25, hit.curvature() * 5.0);
        double ease = desiredCurveFactor < data.grindCurveFactor() ? 0.12 : 0.055;
        data.setGrindCurveFactor(Mth.lerp(ease, data.grindCurveFactor(), desiredCurveFactor));
        // Let actual rail slope influence momentum: downhill gains a little, uphill pays a little, without hard resets.
        if (hit.kind().rail()) speed += Mth.clamp(-hit.tangent().y * 0.012, -0.008, 0.014);
        if (data.boosting()) {
            if (speed < cap) speed = Math.min(cap, speed + (hit.kind().rail() ? 0.030 : 0.027));
            else speed *= 0.9995;
            data.setBoost(data.boost() - JetSetConfig.SERVER.boostDrainPerTick.get().floatValue() * 0.72f);
        }
        VanillaWorldPhysics.RailEffect railEffect = VanillaWorldPhysics.applyVanillaRail(player, data, hit, speed);
        speed = Math.max(MovementTuning.MIN_GRIND_SPEED, railEffect.speed());
        data.setMomentum(speed);
        if (railEffect.launch()) {
            Vec3 dir = MovementMath.safeNormalize(hit.tangent(), MovementMath.horizontalLook(player));
            player.setDeltaMovement(dir.scale(speed).add(0, 0.30 * VanillaWorldPhysics.jumpMultiplier(player), 0));
            player.hurtMarked = true;
            data.setGrinding(false);
            data.setGrindReattachCooldown(5);
            data.setComboGrace(Math.max(data.comboGrace(), 72));
            data.setTrickTicks(Math.max(data.trickTicks(), 10));
            TrickCombo.addStyle(data, 145, 0.10f);
            return false;
        }

        boolean obstructed = (player.horizontalCollision && Math.abs(hit.tangent().y) < 0.65)
                || (speed > 0.16 && player.getDeltaMovement().lengthSqr() < 0.0025);
        data.setGrindStuckTicks(obstructed ? data.grindStuckTicks() + 1 : Math.max(0, data.grindStuckTicks() - 2));
        if (data.grindStuckTicks() > 10) {
            launchFromGrind(player, data, true);
            data.setGrindReattachCooldown(8);
            return false;
        }

        VanillaWorldPhysics.emitGrindFeedback(player, hit, material);
        double travelSpeed = speed * data.grindCurveFactor();
        if (player.isInWater()) travelSpeed *= 0.65;
        else if (player.isInLava()) travelSpeed *= 0.35;
        snapToGrind(player, hit, Math.max(MovementTuning.MIN_GRIND_SPEED * 0.70, travelSpeed));

        if (previousKind != hit.kind() && previousKind != GrindKind.NONE)
            TrickCombo.addStyle(data, hit.kind().rail() ? 90 : 55, 0.055f); // rail/edge transfer
        if (player.tickCount % 5 == 0) TrickCombo.addStyle(data, hit.kind().rail() ? 23 : 18, hit.kind().rail() ? 0.018f : 0.015f);
        return true;
    }

    static void snapToGrind(ServerPlayer player, GrindTarget target, double speed) {
        Vec3 normal = MovementMath.safeNormalize(target.normal(), new Vec3(0, 1, 0));
        Vec3 surface = target.point().add(normal.scale(target.kind().rail() ? 0.035 : 0.045));
        double correction = target.kind().rail() ? 0.72 : 0.52;
        player.setPos(Mth.lerp(correction, player.getX(), surface.x),
                Mth.lerp(correction, player.getY(), surface.y),
                Mth.lerp(correction, player.getZ(), surface.z));
        Vec3 dir = MovementMath.safeNormalize(target.tangent(), MovementMath.horizontalLook(player));
        player.setDeltaMovement(dir.scale(speed).add(normal.scale(target.kind().rail() ? 0.012 : 0.025)));
        player.fallDistance = 0;
        player.hurtMarked = true;
    }

    static void hopFromGrind(ServerPlayer player, JetSetData data) {
        GrindKind kind = data.grindKind();
        Vec3 dir = MovementMath.safeNormalize(data.grindDirection(), MovementMath.horizontalLook(player));
        double speed = Math.max(MovementTuning.MIN_GRIND_SPEED, data.momentum());
        double lift = (kind.rail() ? 0.34 : 0.29) * VanillaWorldPhysics.jumpMultiplier(player);
        player.setDeltaMovement(dir.scale(speed).add(0, lift, 0));
        player.hurtMarked = true;
        data.setGrinding(false);
        data.setGrindReattachCooldown(5);
        data.setComboGrace(Math.max(data.comboGrace(), 72));
        data.setTrickTicks(Math.max(data.trickTicks(), 10));
        TrickCombo.addStyle(data, kind.rail() ? 135 : 90, 0.09f);
    }

    static void launchFromGrind(ServerPlayer player, JetSetData data, boolean auto) {
        Vec3 dir = MovementMath.safeNormalize(data.grindDirection(), MovementMath.horizontalLook(player));
        double speed = Math.max(MovementTuning.MIN_GRIND_SPEED, data.momentum());
        player.setDeltaMovement(dir.scale(speed).add(0, (auto ? 0.14 : 0.22) * VanillaWorldPhysics.jumpMultiplier(player), 0));
        player.hurtMarked = true;
        data.setGrinding(false);
        if (!auto) data.setGrindReattachCooldown(3);
    }

    private GrindTraversal() {}
}
