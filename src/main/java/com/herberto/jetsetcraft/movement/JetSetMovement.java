package com.herberto.jetsetcraft.movement;

import com.herberto.jetsetcraft.config.JetSetConfig;
import com.herberto.jetsetcraft.data.JetSetData;
import com.herberto.jetsetcraft.network.InputFlags;
import com.herberto.jetsetcraft.network.JetSetNetwork;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.phys.Vec3;

/** Server-authoritative ride movement orchestrator. Focused solvers live in sibling modules. */
public final class JetSetMovement {

    public static void tickServer(ServerPlayer player, JetSetData data) {
        data.tickInputWatchdog();
        if (DanceSystem.tick(player, data)) {
            data.setLastVerticalVelocity(player.getDeltaMovement().y);
            data.setLastSolverVelocity(player.getDeltaMovement());
            data.setPreviousInputMask(data.inputMask());
            periodicSync(player, data, 2);
            return;
        }
        if (!data.active() || data.style() == RideStyle.NONE || player.isPassenger() || player.isFallFlying()) {
            data.resetTransientRideState();
            data.setGroundStunt(false);
            data.setBoostTrick(false);
            data.setTrickTicks(0);
            data.setMomentum(0.0);
            data.setPreviousInputMask(data.inputMask());
            periodicSync(player, data, 8);
            return;
        }
        data.setDancing(false);

        // Active ride gear is equipment, not a replacement swimming controller. Yield the complete
        // velocity vector and pose back to Minecraft in fluids so swimming, diving, currents, bubble
        // columns and other mods retain full authority. Zeroing the visual momentum mirror also lets
        // Player Animator return to Minecraft's native swim animation immediately.
        if (player.isInWater() || player.isInLava() || player.isSwimming()) {
            data.resetTransientRideState();
            data.setGroundStunt(false);
            data.setBoostTrick(false);
            data.setTrickTicks(0);
            data.setMomentum(0.0);
            data.setLastVerticalVelocity(player.getDeltaMovement().y);
            data.setLastSolverVelocity(player.getDeltaMovement());
            data.setPreviousInputMask(data.inputMask());
            periodicSync(player, data, 3);
            return;
        }

        if (data.groundStunt() && data.trickTicks() > 0) {
            boolean cancel = !player.onGround() || player.isUsingItem() || player.swinging
                    || data.pressed(InputFlags.JUMP) || data.pressed(InputFlags.GRIND);
            if (cancel) {
                data.setGroundStunt(false);
                data.setBoostTrick(false);
                data.setTrickTicks(0);
            } else {
                Vec3 velocity = player.getDeltaMovement();
                player.setDeltaMovement(velocity.x * 0.68, velocity.y, velocity.z * 0.68);
                player.hurtMarked = true;
                data.setMomentum(data.momentum() * 0.68);
                data.setBoosting(false);
                data.setManual(false);
                data.setPowersliding(false);
                TrickCombo.handleTricksAndCombo(player, data, true);
                data.setLastVerticalVelocity(player.getDeltaMovement().y);
                data.setLastSolverVelocity(player.getDeltaMovement());
                data.setPreviousInputMask(data.inputMask());
                periodicSync(player, data, 2);
                return;
            }
        }

        if (data.grindReattachCooldown() > 0) data.setGrindReattachCooldown(data.grindReattachCooldown() - 1);

        boolean grounded = player.onGround();
        VanillaWorldPhysics.captureExternalImpulse(player, data);
        VanillaWorldPhysics.applyRideEnchantments(player, data);
        VanillaWorldPhysics.Surface surface = VanillaWorldPhysics.ground(player);
        VanillaWorldPhysics.applyLanding(player, data, grounded, surface);
        if (grounded) {
            VanillaWorldPhysics.applyGroundRailInteractions(player, data, surface);
            VanillaWorldPhysics.applyMicroTerrainContinuity(player, data);
        }
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
            GrindTraversal.hopFromGrind(player, data);
        }

        if (data.grinding()) {
            if (!GrindTraversal.continueGrinding(player, data)) data.setGrinding(false);
        } else if (data.wallRiding()) {
            if (!WallTraversal.continueWallRide(player, data)) data.setWallRiding(false);
        } else if (data.pressed(InputFlags.GRIND)) {
            if (data.grindReattachCooldown() == 0 && Math.max(horizontalSpeed, data.momentum()) >= MovementTuning.MIN_GRIND_SPEED) {
                GrindTraversal.tryStartGrinding(player, data, horizontal);
            }
            if (!data.grinding() && JetSetConfig.SERVER.allowWallRides.get() && !grounded
                    && data.airTicks() >= 2 && Math.max(horizontalSpeed, data.momentum()) >= MovementTuning.MIN_WALL_SPEED) {
                WallTraversal.tryStartWallRide(player, data, horizontal);
            }
        }

        if (!data.grinding() && !data.wallRiding()) {
            boolean surfaceHandled = !grounded && VanillaWorldPhysics.applyAirborneSurfaceInteractions(player, data);
            if (player.onClimbable() && !data.pressed(InputFlags.GRIND)) {
                // Yield vertical control to ladders/vines/scaffolding-like vanilla behavior; only retain a modest
                // horizontal memory for a clean jump/transfer away.
                data.setMomentum(Math.max(EdgeFinder.horizontal(player.getDeltaMovement()).length(), data.momentum() * 0.96));
            } else if (grounded) RideMotion.applyGroundMovement(player, data, surface);
            else if (!surfaceHandled) RideMotion.applyAirControl(player, data);
            if (!surfaceHandled) RideMotion.handleBoost(player, data, surface);
        }

        TrickCombo.handleTricksAndCombo(player, data, grounded);
        data.setLastVerticalVelocity(player.getDeltaMovement().y);
        data.setLastSolverVelocity(player.getDeltaMovement());
        data.setPreviousInputMask(data.inputMask());
        periodicSync(player, data, 3);
    }

    public static Vec3 desiredDirection(ServerPlayer player, JetSetData data) {
        return MovementMath.desiredDirection(player, data);
    }

    public static Vec3 horizontalLook(ServerPlayer player) {
        return MovementMath.horizontalLook(player);
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
