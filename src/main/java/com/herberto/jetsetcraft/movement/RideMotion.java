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
            data.setStrideTicks(0);
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
            data.setStrideTicks(data.strideTicks() + 1);
            // Street Art's grounded controller expresses acceleration as alternating skate pushes. Keep that
            // physical rhythm, but retain a non-zero floor so keyboard/analogue control never feels unresponsive.
            double stride = Math.sin(data.strideTicks() * Math.PI * 2.0 * 0.04);
            // The first push must always clear the solver's tiny-velocity dead zone; subsequent ticks pulse.
            double stridePulse = data.strideTicks() <= 2 ? 1.0 : 0.55 + 0.45 * stride * stride;
            if (momentum < cap) {
                momentum = Math.min(cap, momentum + acceleration * stridePulse * (0.45 + inputMagnitude * 0.55)
                        + world.passiveAssistPerTick());
            } else {
                // Momentum above the ordinary cap came from a boost, slope, explosion, piston, rail or other
                // legitimate source. Let friction bleed it off naturally instead of flattening it.
                momentum *= Math.max(world.coastingRetention(), 0.9970);
            }
        } else if (!data.powersliding()) {
            data.setStrideTicks(0);
            momentum *= world.coastingRetention();
        }
        if (data.powersliding()) {
            data.setStrideTicks(0);
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
        if (data.powersliding() && currentSpeed > 0.05 && desired.lengthSqr() > 1.0e-5) {
            // Drift steering is angular rather than a direct velocity replacement. Holding the slide longer
            // permits a tighter line, while accumulated turn determines the small release kick.
            double maxTurn = 0.045 + Math.min(1.0, data.powerslideTicks() / 40.0) * 0.045;
            double turn = Math.max(-maxTurn, Math.min(maxTurn,
                    MovementMath.signedHorizontalAngle(current, desired)));
            data.setDriftTurn(data.driftTurn() + turn);
            data.setBestDriftTurn(Math.max(data.bestDriftTurn(), Math.abs(data.driftTurn())));
            direction = MovementMath.safeNormalize(MovementMath.rotateHorizontal(current.normalize(), turn),
                    current.normalize());
        } else if (desired.lengthSqr() > 1.0e-5) {
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
        if (desired.lengthSqr() < 1.0e-5) {
            if (data.windTicks() > 0) data.setWindTicks(data.windTicks() - 1);
            return;
        }
        double speed = Math.max(horizontal.length(), data.momentum());
        Vec3 currentDir = horizontal.lengthSqr() > 1.0e-5 ? horizontal.normalize() : desired.normalize();
        if (data.windTicks() > 0 && horizontal.length() >= 0.25) {
            double decay = Math.max(0.0, Math.min(1.0, data.windTicks() / 10.0 - 1.0));
            Vec3 bias = MovementMath.safeNormalize(data.windBias(), currentDir);
            double alignment = Math.max(0.0, Math.min(1.0, currentDir.dot(desired.normalize()) * 2.0));
            alignment *= Math.max(0.0, Math.min(1.0, currentDir.dot(bias) * 2.0));
            double maxTurn = 0.25 * decay * alignment;
            double turn = Math.max(-maxTurn, Math.min(maxTurn,
                    MovementMath.signedHorizontalAngle(currentDir, desired)));
            Vec3 redirected = MovementMath.rotateHorizontal(currentDir, turn);
            player.setDeltaMovement(redirected.x * speed, current.y * 0.985, redirected.z * speed);
            player.hurtMarked = true;
            data.setWindTicks(data.windTicks() - 1);
            return;
        }
        if (data.windTicks() > 0) data.setWindTicks(data.windTicks() - 1);
        double control = data.style().airControl() * (data.externalImpulseTicks() > 0 ? 0.30 : 1.0);
        Vec3 blended = MovementMath.safeNormalize(currentDir.scale(1.0 - control)
                .add(desired.normalize().scale(control)), desired.normalize());
        player.setDeltaMovement(blended.x * speed, current.y, blended.z * speed);
        player.hurtMarked = true;
    }

    static void beginPowerslide(JetSetData data) {
        data.setDriftTurn(0.0);
        data.setBestDriftTurn(0.0);
    }

    static void finishPowerslide(ServerPlayer player, JetSetData data) {
        double strength = Math.max(0.0, Math.min(1.0, data.bestDriftTurn() / Math.PI * 4.0 - 1.0));
        Vec3 horizontal = EdgeFinder.horizontal(player.getDeltaMovement());
        if (strength > 0.0 && horizontal.lengthSqr() > 1.0e-5
                && Math.hypot(data.inputForward(), data.inputStrafe()) > 0.05) {
            double speed = horizontal.length() + strength * 0.10;
            Vec3 direction = horizontal.normalize();
            Vec3 old = player.getDeltaMovement();
            player.setDeltaMovement(direction.x * speed, old.y, direction.z * speed);
            player.hurtMarked = true;
            data.setMomentum(Math.max(data.momentum(), speed));
            data.setComboGrace(MovementTuning.COMBO_GRACE_TICKS);
            TrickCombo.addStyle(data, 45 + (int) Math.round(strength * 55.0), 0.04f);
        }
        data.setDriftTurn(0.0);
        data.setBestDriftTurn(0.0);
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
