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
    private static final double[] EXPOSURE_SAMPLES = {0.20, 0.50, 0.80};

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
                        best = choose(player, feet, preferred, new Vec3(x0, topY, z0), new Vec3(x1, topY, z0), radius, best, bestScore);
                        if (best != null) bestScore = score(best, preferred);
                        best = choose(player, feet, preferred, new Vec3(x0, topY, z1), new Vec3(x1, topY, z1), radius, best, bestScore);
                        if (best != null) bestScore = score(best, preferred);
                        best = choose(player, feet, preferred, new Vec3(x0, topY, z0), new Vec3(x0, topY, z1), radius, best, bestScore);
                        if (best != null) bestScore = score(best, preferred);
                        best = choose(player, feet, preferred, new Vec3(x1, topY, z0), new Vec3(x1, topY, z1), radius, best, bestScore);
                        if (best != null) bestScore = score(best, preferred);
                    }
                }
            }
        }
        return Optional.ofNullable(best);
    }

    private static Edge choose(Player player, Vec3 feet, Vec3 preferred, Vec3 a, Vec3 b, double radius, Edge current, double currentScore) {
        Vec3 ab = b.subtract(a);
        double lenSq = ab.lengthSqr();
        if (lenSq < 0.04) return current;
        Vec3 tangent = ab.normalize();
        double alignment = Math.abs(tangent.dot(preferred));
        if (alignment < 0.42) return current;
        double t = feet.subtract(a).dot(ab) / lenSq;
        t = Math.max(0.0, Math.min(1.0, t));
        Vec3 point = a.add(ab.scale(t));
        double distanceSq = point.distanceToSqr(feet);
        if (distanceSq > radius * radius) return current;
        // Collision exposure probes are the expensive part, so only run them for edges inside snap range.
        if (!usableExposedEdge(player, a, b)) return current;
        if (tangent.dot(preferred) < 0) tangent = tangent.scale(-1);
        Edge candidate = new Edge(point, tangent, distanceSq);
        double score = score(candidate, preferred);
        return score < currentScore ? candidate : current;
    }

    /**
     * Reject fake top seams while retaining real ledges and narrow world geometry. A collision-box top edge
     * is grindable only when one horizontal side is genuinely open and there is rider clearance above it.
     * This makes adjacent full-block floor seams disappear while iron bars, fences, walls, panes, roof edges
     * and the exposed sides of complex voxel shapes remain discoverable without a giant block whitelist.
     */
    private static boolean usableExposedEdge(Player player, Vec3 a, Vec3 b) {
        Vec3 tangent = horizontal(b.subtract(a));
        if (tangent.lengthSqr() < 1.0e-6) return false;
        tangent = tangent.normalize();
        Vec3 side = new Vec3(-tangent.z, 0.0, tangent.x);

        // Sample more than the midpoint so a candidate that merely crosses an internal shape seam does not
        // masquerade as a continuous ledge. At each sample, at least one side just below the top must be air.
        for (double t : EXPOSURE_SAMPLES) {
            Vec3 p = a.add(b.subtract(a).scale(t));
            boolean sideAFree = sideProbeFree(player, p.add(side.scale(0.045)), tangent);
            boolean sideBFree = sideProbeFree(player, p.add(side.scale(-0.045)), tangent);
            if (!sideAFree && !sideBFree) return false;
        }

        Vec3 mid = a.add(b.subtract(a).scale(0.50));
        double halfAlong = Math.min(0.16, Math.max(0.08, horizontal(b.subtract(a)).length() * 0.20));
        double halfSide = 0.12;
        AABB clearance = orientedAabb(mid.add(0.0, 0.04, 0.0), tangent, side, halfAlong, halfSide, 1.62);
        return player.level().noCollision(player, clearance);
    }

    private static boolean sideProbeFree(Player player, Vec3 center, Vec3 tangent) {
        Vec3 side = new Vec3(-tangent.z, 0.0, tangent.x);
        AABB probe = orientedAabb(center.add(0.0, -0.035, 0.0), tangent, side, 0.045, 0.018, 0.040);
        return player.level().noCollision(player, probe);
    }

    /** Build a conservative world-axis AABB around a tiny oriented rectangle. */
    private static AABB orientedAabb(Vec3 center, Vec3 tangent, Vec3 side, double halfAlong, double halfSide, double height) {
        double hx = Math.abs(tangent.x) * halfAlong + Math.abs(side.x) * halfSide;
        double hz = Math.abs(tangent.z) * halfAlong + Math.abs(side.z) * halfSide;
        double minY = height >= 0.10 ? center.y : center.y - height;
        double maxY = height >= 0.10 ? center.y + height : center.y;
        return new AABB(center.x - hx, minY, center.z - hz, center.x + hx, maxY, center.z + hz);
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
