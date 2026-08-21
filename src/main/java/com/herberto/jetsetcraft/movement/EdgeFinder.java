package com.herberto.jetsetcraft.movement;

import com.herberto.jetsetcraft.config.JetSetConfig;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.VoxelShape;

import java.util.Optional;

public final class EdgeFinder {
    public record Edge(Vec3 point, Vec3 tangent, double distanceSq) {}

    public static Optional<Edge> findBest(Player player, Vec3 preferredDirection) {
        Level level = player.level();
        double radius = JetSetConfig.SERVER.grindSnapRadius.get();
        double yTolerance = JetSetConfig.SERVER.grindVerticalTolerance.get();
        Vec3 feet = new Vec3(player.getX(), player.getY() + 0.08, player.getZ());
        Vec3 preferred = horizontal(preferredDirection);
        if (preferred.lengthSqr() < 1.0e-5) preferred = horizontal(player.getLookAngle());
        if (preferred.lengthSqr() < 1.0e-5) preferred = new Vec3(0, 0, 1);
        preferred = preferred.normalize();

        int minX = (int) Math.floor(feet.x - 1.25);
        int maxX = (int) Math.floor(feet.x + 1.25);
        int minY = (int) Math.floor(feet.y - 1.1);
        int maxY = (int) Math.floor(feet.y + 0.8);
        int minZ = (int) Math.floor(feet.z - 1.25);
        int maxZ = (int) Math.floor(feet.z + 1.25);

        Edge best = null;
        double bestScore = Double.POSITIVE_INFINITY;
        BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos();
        for (int x = minX; x <= maxX; x++) {
            for (int y = minY; y <= maxY; y++) {
                for (int z = minZ; z <= maxZ; z++) {
                    pos.set(x, y, z);
                    BlockState state = level.getBlockState(pos);
                    if (state.isAir()) continue;
                    if (state.is(JetSetTags.NO_GRIND)) continue;
                    if (state.is(JetSetTags.HAZARD_SURFACES) && !state.is(JetSetTags.GRINDABLE)) continue;
                    VoxelShape shape = state.getCollisionShape(level, pos);
                    if (shape.isEmpty()) continue;
                    for (AABB box : shape.toAabbs()) {
                        double topY = y + box.maxY;
                        if (Math.abs(feet.y - topY) > yTolerance) continue;
                        double x0 = x + box.minX;
                        double x1 = x + box.maxX;
                        double z0 = z + box.minZ;
                        double z1 = z + box.maxZ;
                        best = choose(feet, preferred, new Vec3(x0, topY, z0), new Vec3(x1, topY, z0), radius, best, bestScore);
                        if (best != null) bestScore = score(best, preferred);
                        best = choose(feet, preferred, new Vec3(x0, topY, z1), new Vec3(x1, topY, z1), radius, best, bestScore);
                        if (best != null) bestScore = score(best, preferred);
                        best = choose(feet, preferred, new Vec3(x0, topY, z0), new Vec3(x0, topY, z1), radius, best, bestScore);
                        if (best != null) bestScore = score(best, preferred);
                        best = choose(feet, preferred, new Vec3(x1, topY, z0), new Vec3(x1, topY, z1), radius, best, bestScore);
                        if (best != null) bestScore = score(best, preferred);
                    }
                }
            }
        }
        return Optional.ofNullable(best);
    }

    private static Edge choose(Vec3 feet, Vec3 preferred, Vec3 a, Vec3 b, double radius, Edge current, double currentScore) {
        Vec3 ab = b.subtract(a);
        double lenSq = ab.lengthSqr();
        if (lenSq < 0.04) return current;
        Vec3 tangent = ab.normalize();
        double alignment = Math.abs(tangent.dot(preferred));
        if (alignment < 0.42) return current;
        if (tangent.dot(preferred) < 0) tangent = tangent.scale(-1);
        double t = feet.subtract(a).dot(ab) / lenSq;
        t = Math.max(0.0, Math.min(1.0, t));
        Vec3 point = a.add(ab.scale(t));
        double distanceSq = point.distanceToSqr(feet);
        if (distanceSq > radius * radius) return current;
        Edge candidate = new Edge(point, tangent, distanceSq);
        double score = score(candidate, preferred);
        return score < currentScore ? candidate : current;
    }

    private static double score(Edge edge, Vec3 preferred) {
        double alignment = Math.abs(edge.tangent.dot(preferred));
        return edge.distanceSq + (1.0 - alignment) * 0.35;
    }

    public static Vec3 horizontal(Vec3 vec) {
        return new Vec3(vec.x, 0.0, vec.z);
    }

    private EdgeFinder() {}
}
