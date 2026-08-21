package com.herberto.jetsetcraft.movement;

import com.herberto.jetsetcraft.config.JetSetConfig;
import com.herberto.jetsetcraft.data.JetSetData;
import com.herberto.jetsetcraft.network.InputFlags;
import com.herberto.jetsetcraft.network.JetSetNetwork;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;

public final class JetSetMovement {
    private static final double MIN_GRIND_SPEED = 0.145;
    private static final double MIN_WALL_SPEED = 0.18;
    private static final int COMBO_GRACE_TICKS = 58;

    public static void tickServer(ServerPlayer player, JetSetData data) {
        if (!data.active() || data.style() == RideStyle.NONE || player.isPassenger() || player.isFallFlying()) {
            data.resetTransientRideState();
            data.setMomentum(0.0);
            data.setPreviousInputMask(data.inputMask());
            periodicSync(player, data, 8);
            return;
        }

        if (data.grindReattachCooldown() > 0) data.setGrindReattachCooldown(data.grindReattachCooldown() - 1);

        boolean grounded = player.onGround();
        VanillaWorldPhysics.Surface surface = VanillaWorldPhysics.ground(player);
        VanillaWorldPhysics.applyLanding(player, data, grounded, surface);
        if (grounded) VanillaWorldPhysics.applyGroundRailInteractions(player, data, surface);
        Vec3 velocity = player.getDeltaMovement();
        Vec3 horizontal = EdgeFinder.horizontal(velocity);
        double horizontalSpeed = horizontal.length();
        boolean wantsPowerslide = grounded && !data.grinding() && !data.wallRiding() && data.pressed(InputFlags.BRAKE)
                && Math.abs(data.inputStrafe()) > 0.20f && Math.max(horizontalSpeed, data.momentum()) > 0.18;
        data.setPowersliding(wantsPowerslide);
        data.setPowerslideTicks(wantsPowerslide ? data.powerslideTicks() + 1 : 0);
        data.setManual(grounded && !data.grinding() && !data.wallRiding() && !wantsPowerslide
                && data.pressed(InputFlags.MANUAL) && data.momentum() > 0.14);
        data.setBoosting(data.pressed(InputFlags.BOOST) && data.boost() > 0.0f);

        // Capture vanilla/modded impulses into JetSetCraft momentum. Do not clamp them back to ride caps here:
        // explosions, pistons, knockback, currents and other emergent movement are allowed to become tech.
        if (data.momentum() <= 0.0) data.setMomentum(horizontalSpeed);
        else data.setMomentum(Math.max(horizontalSpeed, data.momentum() * (grounded ? 0.9992 : 0.9985)));

        if (data.grinding() && data.justPressed(InputFlags.JUMP) && JetSetConfig.SERVER.allowRailTricks.get()) {
            hopFromGrind(player, data);
        }

        if (data.grinding()) {
            if (!continueGrinding(player, data)) data.setGrinding(false);
        } else if (data.wallRiding()) {
            if (!continueWallRide(player, data)) data.setWallRiding(false);
        } else if (data.pressed(InputFlags.GRIND)) {
            if (data.grindReattachCooldown() == 0 && Math.max(horizontalSpeed, data.momentum()) >= MIN_GRIND_SPEED) {
                tryStartGrinding(player, data, horizontal);
            }
            if (!data.grinding() && JetSetConfig.SERVER.allowWallRides.get() && !grounded
                    && data.airTicks() >= 2 && Math.max(horizontalSpeed, data.momentum()) >= MIN_WALL_SPEED) {
                tryStartWallRide(player, data, horizontal);
            }
        }

        if (!data.grinding() && !data.wallRiding()) {
            boolean surfaceHandled = !grounded && VanillaWorldPhysics.applyAirborneSurfaceInteractions(player, data);
            if (player.onClimbable() && !data.pressed(InputFlags.GRIND)) {
                // Yield vertical control to ladders/vines/scaffolding-like vanilla behavior; only retain a modest
                // horizontal memory for a clean jump/transfer away.
                data.setMomentum(Math.max(EdgeFinder.horizontal(player.getDeltaMovement()).length(), data.momentum() * 0.96));
            } else if (player.isInWater() || player.isInLava()) {
                applyFluidMovement(player, data);
            } else if (grounded) applyGroundMovement(player, data, surface);
            else if (!surfaceHandled) applyAirControl(player, data);
            if (!surfaceHandled) handleBoost(player, data, surface);
        }

        handleTricksAndCombo(player, data, grounded);
        data.setLastVerticalVelocity(player.getDeltaMovement().y);
        data.setPreviousInputMask(data.inputMask());
        periodicSync(player, data, 3);
    }

    private static void applyGroundMovement(ServerPlayer player, JetSetData data, VanillaWorldPhysics.Surface surface) {
        RideStyle style = data.style();
        VanillaWorldPhysics.MotionProfile world = VanillaWorldPhysics.profile(player, surface);
        double scale = JetSetConfig.SERVER.speedScale.get();
        Vec3 current = EdgeFinder.horizontal(player.getDeltaMovement());
        double currentSpeed = current.length();
        Vec3 desired = desiredDirection(player, data);
        double inputMagnitude = Math.min(1.0, Math.hypot(data.inputForward(), data.inputStrafe()));
        double momentum = Math.max(currentSpeed, data.momentum());
        double cap = style.cruiseCap() * scale * world.cruiseCapMultiplier();
        double acceleration = style.acceleration() * scale * world.accelerationMultiplier();

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
            data.setComboGrace(COMBO_GRACE_TICKS);
            if (player.tickCount % 6 == 0) addStyle(data, 20, 0.012f);
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
                double steering = data.powersliding()
                        ? Math.min(0.68, (style.steering() * 1.85 + Math.abs(data.inputStrafe()) * 0.12)
                                * world.steeringMultiplier())
                        : Math.min(0.80, style.steering() * world.steeringMultiplier());
                direction = safeNormalize(current.normalize().scale(1.0 - steering)
                        .add(direction.scale(steering)), direction);
            }
        } else if (currentSpeed > 0.02) direction = current.normalize();
        else direction = horizontalLook(player);

        Vec3 old = player.getDeltaMovement();
        player.setDeltaMovement(direction.x * momentum, old.y, direction.z * momentum);
        player.hurtMarked = true;
    }

    private static void applyAirControl(ServerPlayer player, JetSetData data) {
        Vec3 current = player.getDeltaMovement();
        Vec3 horizontal = EdgeFinder.horizontal(current);
        Vec3 desired = desiredDirection(player, data);
        if (desired.lengthSqr() < 1.0e-5) return;
        double speed = Math.max(horizontal.length(), data.momentum());
        Vec3 currentDir = horizontal.lengthSqr() > 1.0e-5 ? horizontal.normalize() : desired.normalize();
        Vec3 blended = safeNormalize(currentDir.scale(1.0 - data.style().airControl())
                .add(desired.normalize().scale(data.style().airControl())), desired.normalize());
        player.setDeltaMovement(blended.x * speed, current.y, blended.z * speed);
        player.hurtMarked = true;
    }


    private static void applyFluidMovement(ServerPlayer player, JetSetData data) {
        Vec3 current = player.getDeltaMovement();
        Vec3 horizontal = EdgeFinder.horizontal(current);
        Vec3 desired = desiredDirection(player, data);
        double retention = player.isInLava() ? 0.55 : VanillaWorldPhysics.waterRetention(player);
        double speed = Math.max(horizontal.length(), data.momentum()) * retention;
        if (speed < 0.015) speed = 0.0;
        Vec3 currentDir = horizontal.lengthSqr() > 1.0e-6 ? horizontal.normalize() : horizontalLook(player);
        Vec3 direction = currentDir;
        if (desired.lengthSqr() > 1.0e-5) {
            double control = player.isInLava() ? 0.045 : (player.isUnderWater() ? 0.08 : 0.14);
            direction = safeNormalize(currentDir.scale(1.0 - control).add(desired.normalize().scale(control)), currentDir);
        }
        data.setMomentum(speed);
        // Vertical velocity is intentionally untouched: water currents and bubble columns remain Minecraft-native.
        player.setDeltaMovement(direction.x * speed, current.y, direction.z * speed);
        player.hurtMarked = true;
    }

    private static void handleBoost(ServerPlayer player, JetSetData data, VanillaWorldPhysics.Surface surface) {
        if (!data.boosting()) {
            if (player.onGround() && !data.grinding()) data.setBoost(data.boost() + JetSetConfig.SERVER.boostRechargePerTick.get().floatValue());
            return;
        }
        RideStyle style = data.style();
        VanillaWorldPhysics.MotionProfile world = VanillaWorldPhysics.profile(player, surface);
        double scale = JetSetConfig.SERVER.speedScale.get();
        Vec3 velocity = player.getDeltaMovement();
        Vec3 horizontal = EdgeFinder.horizontal(velocity);
        Vec3 direction = desiredDirection(player, data);
        if (direction.lengthSqr() < 1.0e-5) direction = horizontal.lengthSqr() > 1.0e-5 ? horizontal.normalize() : horizontalLook(player);
        else direction = direction.normalize();
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

    private static boolean tryStartGrinding(ServerPlayer player, JetSetData data, Vec3 horizontalVelocity) {
        Vec3 preferred = horizontalVelocity.lengthSqr() > 1.0e-6 ? horizontalVelocity : desiredDirection(player, data);
        if (preferred.lengthSqr() < 1.0e-6) preferred = horizontalLook(player);
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
        double speed = Math.max(MIN_GRIND_SPEED, Math.max(horizontalVelocity.length(), data.momentum())) * data.style().grindMultiplier();
        data.setMomentum(speed);
        snapToGrind(player, hit, speed);
        addStyle(data, hit.kind().rail() ? 155 : 120, hit.kind().rail() ? 0.13f : 0.10f);
        return true;
    }

    private static boolean continueGrinding(ServerPlayer player, JetSetData data) {
        if (!data.pressed(InputFlags.GRIND)) {
            launchFromGrind(player, data, false);
            return false;
        }

        Vec3 preferred = data.grindDirection().lengthSqr() > 1.0e-5
                ? data.grindDirection() : EdgeFinder.horizontal(player.getDeltaMovement());
        if (data.grindKind().rail()) {
            Vec3 desired = desiredDirection(player, data);
            if (desired.lengthSqr() > 0.05) {
                // Steering input biases junction selection without instantly rotating away from the current rail.
                preferred = safeNormalize(preferred.scale(0.82).add(desired.normalize().scale(0.44)), preferred);
            }
        }

        var target = GrindFinder.findBest(player, preferred, data.grindKind());
        if (target.isEmpty()) {
            if (data.grindGrace() > 0) {
                data.setGrindGrace(data.grindGrace() - 1);
                double projected = Math.abs(player.getDeltaMovement().dot(safeNormalize(preferred, horizontalLook(player))));
                double speed = Math.max(data.momentum(), projected);
                Vec3 dir = safeNormalize(preferred, horizontalLook(player));
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

        double cap = data.style().boostCap() * JetSetConfig.SERVER.speedScale.get();
        double pathSpeed = Math.abs(player.getDeltaMovement().dot(hit.tangent()));
        double baseSpeed = Math.max(MIN_GRIND_SPEED, Math.max(data.momentum(), pathSpeed));
        double speed = baseSpeed < cap ? Math.min(cap, baseSpeed + 0.0018) : baseSpeed * 0.99935;

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
        speed = Math.max(MIN_GRIND_SPEED, railEffect.speed());
        data.setMomentum(speed);
        if (railEffect.launch()) {
            Vec3 dir = safeNormalize(hit.tangent(), horizontalLook(player));
            player.setDeltaMovement(dir.scale(speed).add(0, 0.30 * VanillaWorldPhysics.jumpMultiplier(player), 0));
            player.hurtMarked = true;
            data.setGrinding(false);
            data.setGrindReattachCooldown(5);
            data.setComboGrace(Math.max(data.comboGrace(), 72));
            data.setTrickTicks(Math.max(data.trickTicks(), 10));
            addStyle(data, 145, 0.10f);
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

        double travelSpeed = speed * data.grindCurveFactor();
        if (player.isInWater()) travelSpeed *= 0.65;
        else if (player.isInLava()) travelSpeed *= 0.35;
        snapToGrind(player, hit, Math.max(MIN_GRIND_SPEED * 0.70, travelSpeed));

        if (previousKind != hit.kind() && previousKind != GrindKind.NONE)
            addStyle(data, hit.kind().rail() ? 90 : 55, 0.055f); // rail/edge transfer
        if (player.tickCount % 5 == 0) addStyle(data, hit.kind().rail() ? 23 : 18, hit.kind().rail() ? 0.018f : 0.015f);
        return true;
    }

    private static boolean tryStartWallRide(ServerPlayer player, JetSetData data, Vec3 horizontal) {
        var wall = WallRideFinder.find(player, horizontal);
        if (wall.isEmpty()) return false;
        data.setWallRiding(true);
        data.setGrinding(false);
        data.setWallNormal(wall.get().normal());
        data.setGrindDirection(wall.get().tangent());
        data.setWallRideTicks(0);
        data.setWallSide(wallSide(player, wall.get().normal()));
        data.setMomentum(Math.max(MIN_WALL_SPEED, Math.max(data.momentum(), horizontal.length())));
        addStyle(data, 100, 0.08f);
        return continueWallRide(player, data);
    }

    private static boolean continueWallRide(ServerPlayer player, JetSetData data) {
        if (player.onGround() || data.wallRideTicks() > 42) return false;
        if (!data.pressed(InputFlags.GRIND)) {
            Vec3 n = safeNormalize(data.wallNormal(), new Vec3(1,0,0));
            Vec3 t = safeNormalize(data.grindDirection(), horizontalLook(player));
            player.setDeltaMovement(t.scale(data.momentum()).add(n.scale(0.24)).add(0, 0.27 * VanillaWorldPhysics.jumpMultiplier(player), 0));
            player.hurtMarked = true;
            return false;
        }
        var wall = WallRideFinder.find(player, data.grindDirection());
        if (wall.isEmpty()) return false;
        data.setWallNormal(wall.get().normal());
        data.setGrindDirection(wall.get().tangent());
        data.setWallRideTicks(data.wallRideTicks() + 1);
        data.setWallSide(wallSide(player, wall.get().normal()));
        double cap = data.style().boostCap() * JetSetConfig.SERVER.speedScale.get();
        double baseSpeed = Math.max(data.momentum(), EdgeFinder.horizontal(player.getDeltaMovement()).length());
        double speed = baseSpeed < cap ? Math.min(cap, baseSpeed + (data.boosting() ? 0.018 : 0.001))
                : baseSpeed * 0.9990;
        if (data.boosting()) data.setBoost(data.boost() - JetSetConfig.SERVER.boostDrainPerTick.get().floatValue() * 0.60f);
        data.setMomentum(speed);
        Vec3 tangent = wall.get().tangent();
        Vec3 normal = wall.get().normal();
        double y = Mth.clamp(player.getDeltaMovement().y, -0.075, 0.075);
        player.setDeltaMovement(tangent.x * speed - normal.x * 0.026, y, tangent.z * speed - normal.z * 0.026);
        player.fallDistance = 0;
        player.hurtMarked = true;
        if (player.tickCount % 6 == 0) addStyle(data, 16, 0.012f);
        return true;
    }

    private static void snapToGrind(ServerPlayer player, GrindTarget target, double speed) {
        Vec3 normal = safeNormalize(target.normal(), new Vec3(0, 1, 0));
        Vec3 surface = target.point().add(normal.scale(target.kind().rail() ? 0.035 : 0.045));
        double correction = target.kind().rail() ? 0.72 : 0.52;
        player.setPos(Mth.lerp(correction, player.getX(), surface.x),
                Mth.lerp(correction, player.getY(), surface.y),
                Mth.lerp(correction, player.getZ(), surface.z));
        Vec3 dir = safeNormalize(target.tangent(), horizontalLook(player));
        player.setDeltaMovement(dir.scale(speed).add(normal.scale(target.kind().rail() ? 0.012 : 0.025)));
        player.fallDistance = 0;
        player.hurtMarked = true;
    }

    private static void hopFromGrind(ServerPlayer player, JetSetData data) {
        GrindKind kind = data.grindKind();
        Vec3 dir = safeNormalize(data.grindDirection(), horizontalLook(player));
        double speed = Math.max(MIN_GRIND_SPEED, data.momentum());
        double lift = (kind.rail() ? 0.34 : 0.29) * VanillaWorldPhysics.jumpMultiplier(player);
        player.setDeltaMovement(dir.scale(speed).add(0, lift, 0));
        player.hurtMarked = true;
        data.setGrinding(false);
        data.setGrindReattachCooldown(5);
        data.setComboGrace(Math.max(data.comboGrace(), 72));
        data.setTrickTicks(Math.max(data.trickTicks(), 10));
        addStyle(data, kind.rail() ? 135 : 90, 0.09f);
    }

    private static void launchFromGrind(ServerPlayer player, JetSetData data, boolean auto) {
        Vec3 dir = safeNormalize(data.grindDirection(), horizontalLook(player));
        double speed = Math.max(MIN_GRIND_SPEED, data.momentum());
        player.setDeltaMovement(dir.scale(speed).add(0, (auto ? 0.14 : 0.22) * VanillaWorldPhysics.jumpMultiplier(player), 0));
        player.hurtMarked = true;
        data.setGrinding(false);
        if (!auto) data.setGrindReattachCooldown(3);
    }

    private static void handleTricksAndCombo(ServerPlayer player, JetSetData data, boolean grounded) {
        if (data.justPressed(InputFlags.TRICK) && data.grinding() && JetSetConfig.SERVER.allowRailTricks.get()) {
            int next = (data.trickIndex() + trickFromInput(data.inputForward(), data.inputStrafe()) + 1) % 12;
            data.setTrickIndex(next);
            data.setTrickTicks(14);
            int base = data.grindKind().rail() ? 245 : 190;
            addStyle(data, base + next * 13, data.grindKind().rail() ? 0.22f : 0.17f);
            if (data.grindKind().rail()) data.setBoost(data.boost() + 2.5f);
        } else if (data.justPressed(InputFlags.TRICK) && !grounded && !data.grinding() && data.airTicks() >= 2) {
            int next = (data.trickIndex() + trickFromInput(data.inputForward(), data.inputStrafe()) + 1) % 12;
            data.setTrickIndex(next);
            data.setTrickTicks(18);
            addStyle(data, 180 + next * 18, 0.18f);
            Vec3 v = player.getDeltaMovement();
            player.setDeltaMovement(v.x, Math.max(v.y, -0.08) + 0.035, v.z);
            player.hurtMarked = true;
        }
        if (data.trickTicks() > 0) data.setTrickTicks(data.trickTicks() - 1);
        if (!grounded) data.setAirTicks(data.airTicks() + 1);
        else if (!data.wasGrounded()) {
            if (data.airTicks() >= 5 && data.comboScore() > 0) {
                data.setBoost(data.boost() + Math.min(24f, 4f + data.comboScore() / 350f));
                data.setComboGrace(Math.max(data.comboGrace(), 70));
            }
            data.setAirTicks(0);
        }
        if (data.manual()) {
            data.setComboGrace(COMBO_GRACE_TICKS);
            if (player.tickCount % 6 == 0) addStyle(data, 14, 0.01f);
        } else if (!data.grinding() && !data.wallRiding() && data.trickTicks() == 0 && data.comboGrace() > 0) {
            data.setComboGrace(data.comboGrace() - 1);
        }
        if (grounded && data.comboGrace() == 0 && data.comboScore() > 0 && !data.manual() && player.tickCount % 20 == 0) {
            data.setComboScore(0);
            data.setComboMultiplier(1f);
        }
        data.setWasGrounded(grounded);
    }

    private static void addStyle(JetSetData data, int base, float multi) {
        data.setComboScore(data.comboScore() + Math.max(1, Math.round(base * data.comboMultiplier())));
        data.setComboMultiplier(data.comboMultiplier() + multi);
        data.setComboGrace(COMBO_GRACE_TICKS);
    }

    private static int trickFromInput(float f, float s) {
        if (Math.abs(s) > Math.abs(f)) return s > 0 ? 2 : 4;
        if (f < -0.25f) return 5;
        if (f > 0.25f) return 1;
        return 0;
    }

    public static Vec3 desiredDirection(ServerPlayer p, JetSetData d) {
        double yaw = Math.toRadians(p.getYRot());
        Vec3 f = new Vec3(-Math.sin(yaw),0,Math.cos(yaw));
        Vec3 r = new Vec3(f.z,0,-f.x);
        return f.scale(d.inputForward()).add(r.scale(-d.inputStrafe()));
    }

    private static Vec3 horizontalLook(ServerPlayer p) {
        return safeNormalize(EdgeFinder.horizontal(p.getLookAngle()), new Vec3(0,0,1));
    }

    private static float wallSide(ServerPlayer p, Vec3 normal) {
        Vec3 look = horizontalLook(p);
        Vec3 right = new Vec3(look.z, 0, -look.x);
        double dot = safeNormalize(normal, new Vec3(1,0,0)).dot(right);
        return dot >= 0.0 ? 1.0f : -1.0f;
    }

    private static Vec3 safeNormalize(Vec3 v, Vec3 fallback) {
        return v != null && v.lengthSqr() > 1e-7 ? v.normalize() : fallback;
    }

    private static void periodicSync(ServerPlayer p, JetSetData d, int interval) {
        long t = p.level().getGameTime();
        if (t - d.lastSyncTick() >= interval) {
            d.setLastSyncTick(t);
            JetSetNetwork.sync(p,d);
        }
    }

    private JetSetMovement() {}
}
