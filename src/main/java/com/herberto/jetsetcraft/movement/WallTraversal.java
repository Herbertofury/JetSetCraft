package com.herberto.jetsetcraft.movement;

import com.herberto.jetsetcraft.config.JetSetConfig;
import com.herberto.jetsetcraft.data.JetSetData;
import com.herberto.jetsetcraft.network.InputFlags;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;

final class WallTraversal {
    static boolean tryStartWallRide(ServerPlayer player, JetSetData data, Vec3 horizontal) {
        var wall = WallRideFinder.find(player, horizontal);
        if (wall.isEmpty()) return false;
        data.setWallRiding(true);
        data.setGrinding(false);
        data.setWallNormal(wall.get().normal());
        data.setGrindDirection(wall.get().tangent());
        data.setWallRideTicks(0);
        data.setWallSide(MovementMath.wallSide(player, wall.get().normal()));
        data.setMomentum(Math.max(MovementTuning.MIN_WALL_SPEED, Math.max(data.momentum(), horizontal.length())));
        TrickCombo.addStyle(data, 100, 0.08f);
        return continueWallRide(player, data);
    }

    static boolean continueWallRide(ServerPlayer player, JetSetData data) {
        if (player.onGround() || data.wallRideTicks() > 42) return false;
        if (!data.pressed(InputFlags.GRIND)) {
            Vec3 n = MovementMath.safeNormalize(data.wallNormal(), new Vec3(1,0,0));
            Vec3 t = MovementMath.safeNormalize(data.grindDirection(), MovementMath.horizontalLook(player));
            player.setDeltaMovement(t.scale(data.momentum()).add(n.scale(0.24)).add(0, 0.27 * VanillaWorldPhysics.jumpMultiplier(player), 0));
            player.hurtMarked = true;
            return false;
        }
        var wall = WallRideFinder.find(player, data.grindDirection());
        if (wall.isEmpty()) return false;
        data.setWallNormal(wall.get().normal());
        data.setGrindDirection(wall.get().tangent());
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

    private WallTraversal() {}
}
