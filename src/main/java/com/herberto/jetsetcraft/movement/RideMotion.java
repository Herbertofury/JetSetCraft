package com.herberto.jetsetcraft.movement;

import com.herberto.jetsetcraft.config.JetSetConfig;
import com.herberto.jetsetcraft.data.JetSetData;
import com.herberto.jetsetcraft.network.InputFlags;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.phys.Vec3;

final class RideMotion {
    static void applyGroundMovement(ServerPlayer player, JetSetData data, VanillaWorldPhysics.Surface surface) {
        RideStyle style = data.style();
        VanillaWorldPhysics.MotionProfile world = VanillaWorldPhysics.profile(player, data, surface);
        double scale = JetSetConfig.SERVER.speedScale.get();
        Vec3 current = EdgeFinder.horizontal(player.getDeltaMovement());
        double currentSpeed = current.length();
        Vec3 desired = MovementMath.desiredDirection(player, data);
        double inputMagnitude = Math.min(1.0, Math.hypot(data.inputForward(), data.inputStrafe()));
        double momentum = Math.max(currentSpeed, data.momentum());
        double cap = style.cruiseCap() * scale * world.cruiseCapMultiplier();
        double acceleration = style.acceleration() * scale * world.accelerationMultiplier();

        // With no directional input, Minecraft's own travel step has already applied the familiar
        // ground friction to `current`. Treat that real velocity as the entire source of truth instead
        // of reconstructing motion from stale ride momentum (or from the camera's look direction).
        // This makes releasing WASD settle exactly like vanilla and guarantees a stationary player
        // cannot be launched forward by an old momentum value.
        if (inputMagnitude <= 0.05 && !data.powersliding() && !data.manual()) {
            double speed = currentSpeed;
            if (data.pressed(InputFlags.BRAKE)) speed *= 0.72 * world.brakeMultiplier();
            if (speed < 0.025) speed = 0.0;
            data.setMomentum(speed);
            Vec3 old = player.getDeltaMovement();
            if (speed == 0.0 || currentSpeed <= 1.0e-6) {
                player.setDeltaMovement(0.0, old.y, 0.0);
            } else if (speed != currentSpeed) {
                Vec3 direction = current.normalize();
                player.setDeltaMovement(direction.x * speed, old.y, direction.z * speed);
            }
            player.hurtMarked = true;
            return;
        }

        if (inputMagnitude > 0.05 && !data.powersliding()) {
            if (momentum < cap) {
                momentum = Math.min(cap, momentum + acceleration * (0.45 + inputMagnitude * 0.55)
                        + world.passiveAssistPerTick());
            } else {
                // Momentum above the ordinary cap came from a boost, slope, explosion, piston, rail or other
                // legitimate source. Let friction bleed it off naturally instead of flattening it.
                momentum *= Math.max(world.coastingRetention(), 0.9970);
            }
        } else if (!data.powersliding()) {
            momentum *= world.coastingRetention();
        }
        if (data.powersliding()) {
            momentum *= Math.max(0.985, world.coastingRetention() - 0.0030);
            data.setComboGrace(MovementTuning.COMBO_GRACE_TICKS);
            if (player.tickCount % 6 == 0) TrickCombo.addStyle(data, 20, 0.012f);
        } else if (data.pressed(InputFlags.BRAKE)) {
            momentum *= 0.875 * world.brakeMultiplier();
        }
        if (data.manual()) momentum *= Math.max(0.994, world.coastingRetention());
        if (momentum < 0.025) momentum = 0.0;
        data.setMomentum(momentum);

        Vec3 direction;
        if (desired.lengthSqr() > 1.0e-5) {
            direction = desired.normalize();
            if (currentSpeed > 0.04) {
                double impulseSteeringScale = data.externalImpulseTicks() > 0 ? 0.28 : 1.0;
                double steering = (data.powersliding()
                        ? Math.min(0.68, (style.steering() * 1.85 + Math.abs(data.inputStrafe()) * 0.12)
                                * world.steeringMultiplier())
                        : Math.min(0.80, style.steering() * world.steeringMultiplier())) * impulseSteeringScale;
                direction = MovementMath.safeNormalize(current.normalize().scale(1.0 - steering)
                        .add(direction.scale(steering)), direction);
            }
        } else if (currentSpeed > 0.02) direction = current.normalize();
        else direction = Vec3.ZERO;

        Vec3 old = player.getDeltaMovement();
        if (direction.lengthSqr() <= 1.0e-7) momentum = 0.0;
        data.setMomentum(momentum);
        player.setDeltaMovement(direction.x * momentum, old.y, direction.z * momentum);
        player.hurtMarked = true;
    }

    static void applyAirControl(ServerPlayer player, JetSetData data) {
        Vec3 current = player.getDeltaMovement();
        Vec3 horizontal = EdgeFinder.horizontal(current);
        Vec3 desired = MovementMath.desiredDirection(player, data);
        if (desired.lengthSqr() < 1.0e-5) return;
        double speed = Math.max(horizontal.length(), data.momentum());
        Vec3 currentDir = horizontal.lengthSqr() > 1.0e-5 ? horizontal.normalize() : desired.normalize();
        double control = data.style().airControl() * (data.externalImpulseTicks() > 0 ? 0.30 : 1.0);
        Vec3 blended = MovementMath.safeNormalize(currentDir.scale(1.0 - control)
                .add(desired.normalize().scale(control)), desired.normalize());
        player.setDeltaMovement(blended.x * speed, current.y, blended.z * speed);
        player.hurtMarked = true;
    }

    static void handleBoost(ServerPlayer player, JetSetData data, VanillaWorldPhysics.Surface surface) {
        if (!data.boosting()) {
            if (player.onGround() && !data.grinding()) data.setBoost(data.boost() + JetSetConfig.SERVER.boostRechargePerTick.get().floatValue());
            return;
        }
        RideStyle style = data.style();
        VanillaWorldPhysics.MotionProfile world = VanillaWorldPhysics.profile(player, data, surface);
        double scale = JetSetConfig.SERVER.speedScale.get();
        Vec3 velocity = player.getDeltaMovement();
        Vec3 horizontal = EdgeFinder.horizontal(velocity);
        Vec3 direction = MovementMath.desiredDirection(player, data);
        if (direction.lengthSqr() < 1.0e-5) direction = horizontal.lengthSqr() > 1.0e-5 ? horizontal.normalize() : Vec3.ZERO;
        else {
            direction = direction.normalize();
            if (data.externalImpulseTicks() > 0 && horizontal.lengthSqr() > 1.0e-5) {
                direction = MovementMath.safeNormalize(horizontal.normalize().scale(0.82).add(direction.scale(0.18)), horizontal.normalize());
            }
        }
        if (direction.lengthSqr() <= 1.0e-7) {
            data.setBoosting(false);
            data.setMomentum(0.0);
            return;
        }
        double fluidScale = player.isInLava() ? 0.34 : (player.isInWater() ? (player.isUnderWater() ? 0.48 : 0.68) : 1.0);
        double cap = style.boostCap() * scale * world.boostCapMultiplier() * fluidScale;
        double baseSpeed = Math.max(horizontal.length(), data.momentum());
        double speed = baseSpeed < cap ? Math.min(cap, baseSpeed + 0.036 * scale * world.accelerationMultiplier() * fluidScale)
                : baseSpeed * Math.max(0.998, world.coastingRetention());
        data.setMomentum(speed);
        data.setBoost(data.boost() - JetSetConfig.SERVER.boostDrainPerTick.get().floatValue());
        player.setDeltaMovement(direction.x * speed, velocity.y, direction.z * speed);
        player.hurtMarked = true;
    }

    private RideMotion() {}
}
