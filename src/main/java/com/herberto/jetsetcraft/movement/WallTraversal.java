package com.herberto.jetsetcraft.movement;

import com.herberto.jetsetcraft.config.JetSetConfig;
import com.herberto.jetsetcraft.data.JetSetData;
import com.herberto.jetsetcraft.network.InputFlags;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;

final class WallTraversal {
    static boolean tryStartWallRide(ServerPlayer player, JetSetData data, Vec3 horizontal) {
        if (data.wallKicksRemaining() <= 0) return false;
        var wall = WallRideFinder.find(player, horizontal);
        if (wall.isEmpty()) return false;
        if (wall.get().planeKey() == data.lastWallPlane()) return false;
        data.setWallRiding(true);
        data.setGrinding(false);
        data.setWallNormal(wall.get().normal());
        data.setGrindDirection(wall.get().tangent());
        data.setWallRideTicks(0);
        data.setWallSide(MovementMath.wallSide(player, wall.get().normal()));
        data.setWallPlane(wall.get().planeKey());
        data.setMomentum(Math.max(MovementTuning.MIN_WALL_SPEED, Math.max(data.momentum(), horizontal.length())));
        TrickCombo.addStyle(data, 100, 0.08f);
        return continueWallRide(player, data);
    }

    static boolean continueWallRide(ServerPlayer player, JetSetData data) {
        if (player.onGround() || data.wallRideTicks() > 56) return false;
        if (!data.pressed(InputFlags.GRIND)) {
            Vec3 n = MovementMath.safeNormalize(data.wallNormal(), new Vec3(1,0,0));
            Vec3 t = MovementMath.safeNormalize(data.grindDirection(), MovementMath.horizontalLook(player));
            player.setDeltaMovement(t.scale(data.momentum()).add(n.scale(0.24)).add(0, 0.27 * VanillaWorldPhysics.jumpMultiplier(player), 0));
            player.hurtMarked = true;
            beginWindState(data, n);
            data.setLastWallPlane(data.wallPlane());
            data.setGrindReattachCooldown(7);
            return false;
        }
        var wall = WallRideFinder.find(player, data.grindDirection());
        if (wall.isEmpty()) return false;
        data.setWallNormal(wall.get().normal());
        data.setGrindDirection(wall.get().tangent());
        data.setWallPlane(wall.get().planeKey());
        data.setWallRideTicks(data.wallRideTicks() + 1);
        data.setWallSide(MovementMath.wallSide(player, wall.get().normal()));
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
        if (player.tickCount % 6 == 0) TrickCombo.addStyle(data, 16, 0.012f);
        return true;
    }

    /** Street Art-style parkour kick: three grounded-reset uses, outward impulse, then a short wind steering state. */
    static void kickFromWall(ServerPlayer player, JetSetData data) {
        Vec3 normal = MovementMath.safeNormalize(data.wallNormal(), new Vec3(1, 0, 0));
        Vec3 tangent = MovementMath.safeNormalize(data.grindDirection(), MovementMath.horizontalLook(player));
        double speed = Math.max(MovementTuning.MIN_WALL_SPEED,
                Math.max(data.momentum(), EdgeFinder.horizontal(player.getDeltaMovement()).length()));
        int uses = data.wallKicksRemaining();
        double outward = uses > 0 ? 0.25 + Math.min(0.20, speed * 0.25) : 0.12;
        double retained = uses > 0 ? 0.86 : 0.52;
        double jump = (uses > 0 ? 0.42 : 0.28) * VanillaWorldPhysics.jumpMultiplier(player);
        Vec3 kicked = tangent.scale(speed * retained).add(normal.scale(outward)).add(0, jump, 0);
        player.setDeltaMovement(kicked);
        player.fallDistance = 0;
        player.hurtMarked = true;
        data.setMomentum(EdgeFinder.horizontal(kicked).length());
        data.setWallKicksRemaining(Math.max(0, uses - 1));
        data.setLastWallPlane(data.wallPlane());
        data.setWallRiding(false);
        data.setGrindReattachCooldown(8);
        beginWindState(data, normal);
        data.setComboGrace(MovementTuning.COMBO_GRACE_TICKS);
        TrickCombo.addStyle(data, uses > 0 ? 125 : 45, uses > 0 ? 0.08f : 0.02f);
    }

    private static void beginWindState(JetSetData data, Vec3 wallNormal) {
        data.setWindTicks(20);
        data.setWindBias(MovementMath.safeNormalize(wallNormal, new Vec3(1, 0, 0)));
    }

    private WallTraversal() {}
}
