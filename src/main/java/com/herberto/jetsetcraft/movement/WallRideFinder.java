package com.herberto.jetsetcraft.movement;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.core.Direction;
import java.util.Optional;

public final class WallRideFinder {
    public record Wall(Vec3 normal, Vec3 tangent, Direction face, int planeCoordinate) {
        public long planeKey() {
            return ((long) face.get3DDataValue() << 32) ^ (planeCoordinate & 0xFFFFFFFFL);
        }
    }

    public static Optional<Wall> find(ServerPlayer player, Vec3 horizontalVelocity) {
        Vec3 forward = EdgeFinder.horizontal(horizontalVelocity);
        if (forward.lengthSqr() < 1.0e-5) forward = EdgeFinder.horizontal(player.getLookAngle());
        if (forward.lengthSqr() < 1.0e-5) return Optional.empty();
        forward = forward.normalize();
        Vec3 left = new Vec3(-forward.z, 0, forward.x);
        Vec3 origin = player.position().add(0, Math.min(0.9, player.getBbHeight() * 0.48), 0);
        Wall a = cast(player, origin, left, forward);
        Wall b = cast(player, origin, left.scale(-1), forward);
        if (a == null) return Optional.ofNullable(b);
        if (b == null) return Optional.of(a);
        return Optional.of(a);
    }

    private static Wall cast(ServerPlayer player, Vec3 origin, Vec3 side, Vec3 forward) {
        Vec3 end = origin.add(side.scale(0.78));
        BlockHitResult hit = player.level().clip(new ClipContext(origin, end, ClipContext.Block.COLLIDER,
                ClipContext.Fluid.NONE, player));
        if (hit.getType() != HitResult.Type.BLOCK || hit.getDirection().getAxis().isVertical()) return null;
        Vec3 normal = new Vec3(hit.getDirection().getStepX(), 0, hit.getDirection().getStepZ()).normalize();
        Vec3 tangent = forward.subtract(normal.scale(forward.dot(normal)));
        if (tangent.lengthSqr() < 0.05) return null;
        Direction face = hit.getDirection();
        int plane = face.getAxis() == Direction.Axis.X ? hit.getBlockPos().getX() : hit.getBlockPos().getZ();
        return new Wall(normal, tangent.normalize(), face, plane);
    }

    private WallRideFinder() {}
}
