package com.herberto.jetsetcraft.compat.create;

import com.herberto.jetsetcraft.movement.GrindKind;
import com.herberto.jetsetcraft.movement.GrindRailProvider;
import com.herberto.jetsetcraft.movement.GrindTarget;
import com.simibubi.create.Create;
import com.simibubi.create.content.trains.graph.TrackGraph;
import com.simibubi.create.content.trains.graph.TrackGraphBounds;
import com.simibubi.create.content.trains.track.BezierConnection;
import com.simibubi.create.content.trains.track.ITrackBlock;
import com.simibubi.create.content.trains.track.TrackMaterial;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

import java.util.Optional;

/** Native Create 6.x track support. This class is reflectively loaded only when Create is present. */
public final class CreateRailProvider implements GrindRailProvider {
    private static final double TRACK_SURFACE_OFFSET = 3.0 / 16.0;

    @Override
    public Optional<GrindTarget> findBest(Player player, Vec3 preferredDirection, double snapRadius, double verticalTolerance) {
        Level level = player.level();
        Vec3 feet = new Vec3(player.getX(), player.getY() + 0.08, player.getZ());
        GrindTarget best = findBlockTrack(level, feet, preferredDirection, snapRadius, verticalTolerance);
        double bestScore = best == null ? Double.POSITIVE_INFINITY : score(best, preferredDirection);

        // Curves are stored globally in graph bounds. This covers long Bezier turns even when the rider is only
        // standing over Create's invisible FakeTrackBlock in the middle of the curve.
        if (Create.RAILWAYS.trackNetworks != null) {
            AABB playerQuery = new AABB(feet, feet).inflate(snapRadius + 0.75, verticalTolerance + 0.75, snapRadius + 0.75);
            for (TrackGraph graph : Create.RAILWAYS.trackNetworks.values()) {
                TrackGraphBounds bounds = graph.getBounds(level);
                if (bounds.box == null || !bounds.box.inflate(2.0).intersects(playerQuery)) continue;
                for (BezierConnection curve : bounds.beziers) {
                    if (!curve.getBounds().inflate(snapRadius + 0.75).intersects(playerQuery)) continue;
                    GrindTarget target = closestOnBezier(curve, feet, preferredDirection, snapRadius, verticalTolerance);
                    if (target == null) continue;
                    double score = score(target, preferredDirection);
                    if (score < bestScore) {
                        best = target;
                        bestScore = score;
                    }
                }
            }
        }
        return Optional.ofNullable(best);
    }

    private static GrindTarget findBlockTrack(Level level, Vec3 feet, Vec3 preferred, double radius, double verticalTolerance) {
        int search = Math.max(1, (int)Math.ceil(radius + 0.9));
        BlockPos base = BlockPos.containing(feet);
        BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos();
        GrindTarget best = null;
        double bestScore = Double.POSITIVE_INFINITY;

        for (int dx = -search; dx <= search; dx++) {
            for (int dy = -2; dy <= 1; dy++) {
                for (int dz = -search; dz <= search; dz++) {
                    pos.set(base.getX() + dx, base.getY() + dy, base.getZ() + dz);
                    BlockState state = level.getBlockState(pos);
                    if (!(state.getBlock() instanceof ITrackBlock track)) continue;
                    Vec3 normal = track.getUpNormal(level, pos, state).normalize();
                    Vec3 center = Vec3.atBottomCenterOf(pos)
                            .add(0, track.getElevationAtCenter(level, pos, state), 0)
                            .add(normal.scale(TRACK_SURFACE_OFFSET));
                    double lateral = lateralOffset(track.getMaterial());
                    for (Vec3 axis : track.getTrackAxes(level, pos, state)) {
                        if (axis.lengthSqr() < 1.0e-8) continue;
                        // Create axes are intentionally not normalized: slope/diagonal components describe the full block span.
                        Vec3 side = normal.cross(axis.normalize());
                        if (side.lengthSqr() < 1.0e-8) side = new Vec3(-axis.z, 0, axis.x);
                        side = side.normalize();
                        int variants = lateral <= 1.0e-4 ? 1 : 2;
                        for (int i = 0; i < variants; i++) {
                            double sign = variants == 1 ? 0.0 : (i == 0 ? -1.0 : 1.0);
                            Vec3 barCenter = center.add(side.scale(lateral * sign));
                            Vec3 a = barCenter.subtract(axis.scale(0.5));
                            Vec3 b = barCenter.add(axis.scale(0.5));
                            GrindTarget target = segment(feet, preferred, a, b, normal, radius, verticalTolerance);
                            if (target == null) continue;
                            double score = score(target, preferred);
                            if (score < bestScore) {
                                best = target;
                                bestScore = score;
                            }
                        }
                    }
                }
            }
        }
        return best;
    }

    private static GrindTarget closestOnBezier(BezierConnection curve, Vec3 feet, Vec3 preferred,
                                                double radius, double verticalTolerance) {
        int samples = Math.max(16, Math.min(128, curve.getSegmentCount() * 3));
        double bestT = 0.0;
        double bestDistance = Double.POSITIVE_INFINITY;
        for (int i = 0; i <= samples; i++) {
            double t = i / (double)samples;
            Vec3 normal = curve.getNormal(t).normalize();
            Vec3 point = curve.getPosition(t).add(normal.scale(TRACK_SURFACE_OFFSET));
            double dy = Math.abs(feet.y - point.y);
            if (dy > verticalTolerance + 0.30) continue;
            double horizontalSq = sq(feet.x - point.x) + sq(feet.z - point.z);
            if (horizontalSq > (radius + 0.30) * (radius + 0.30)) continue;
            double d = point.distanceToSqr(feet);
            if (d < bestDistance) {
                bestDistance = d;
                bestT = t;
            }
        }
        if (!Double.isFinite(bestDistance)) return null;

        double window = 1.5 / samples;
        double lo = Math.max(0.0, bestT - window);
        double hi = Math.min(1.0, bestT + window);
        for (int i = 0; i < 7; i++) {
            double m1 = lo + (hi - lo) / 3.0;
            double m2 = hi - (hi - lo) / 3.0;
            if (distanceSq(curve, feet, m1) < distanceSq(curve, feet, m2)) hi = m2;
            else lo = m1;
        }
        double t = (lo + hi) * 0.5;
        Vec3 normal = curve.getNormal(t).normalize();
        Vec3 point = curve.getPosition(t).add(normal.scale(TRACK_SURFACE_OFFSET));
        if (Math.abs(feet.y - point.y) > verticalTolerance) return null;
        double horizontalSq = sq(feet.x - point.x) + sq(feet.z - point.z);
        if (horizontalSq > radius * radius) return null;

        double epsilon = Math.max(1.0 / (samples * 2.0), 1.0e-3);
        Vec3 before = curve.getPosition(Math.max(0.0, t - epsilon));
        Vec3 after = curve.getPosition(Math.min(1.0, t + epsilon));
        Vec3 tangent = after.subtract(before);
        if (tangent.lengthSqr() < 1.0e-8) return null;
        Vec3 side = normal.cross(tangent.normalize());
        if (side.lengthSqr() > 1.0e-8) {
            double lateral = lateralOffset(curve.getMaterial());
            if (lateral > 1.0e-4) {
                side = side.normalize();
                Vec3 left = point.add(side.scale(-lateral));
                Vec3 right = point.add(side.scale(lateral));
                point = left.distanceToSqr(feet) <= right.distanceToSqr(feet) ? left : right;
            }
        }
        double wide = Math.max(epsilon, 0.05);
        Vec3 tangentBefore = curve.getPosition(t).subtract(curve.getPosition(Math.max(0.0, t - wide)));
        Vec3 tangentAfter = curve.getPosition(Math.min(1.0, t + wide)).subtract(curve.getPosition(t));
        double curvature = curvature(tangentBefore, tangentAfter);
        GrindTarget target = new GrindTarget(point, tangent, normal, point.distanceToSqr(feet), GrindKind.CREATE_TRACK, curvature)
                .orientedTo(preferred);
        return preferred != null && preferred.lengthSqr() > 1.0e-7 && target.alignment(preferred) < 0.12 ? null : target;
    }

    private static GrindTarget segment(Vec3 feet, Vec3 preferred, Vec3 a, Vec3 b, Vec3 normal,
                                       double radius, double verticalTolerance) {
        Vec3 ab = b.subtract(a);
        double lenSq = ab.lengthSqr();
        if (lenSq < 1.0e-8) return null;
        double t = Math.max(0.0, Math.min(1.0, feet.subtract(a).dot(ab) / lenSq));
        Vec3 point = a.add(ab.scale(t));
        if (Math.abs(feet.y - point.y) > verticalTolerance) return null;
        double horizontalSq = sq(feet.x - point.x) + sq(feet.z - point.z);
        if (horizontalSq > radius * radius) return null;
        GrindTarget target = new GrindTarget(point, ab, normal, point.distanceToSqr(feet), GrindKind.CREATE_TRACK)
                .orientedTo(preferred);
        return preferred != null && preferred.lengthSqr() > 1.0e-7 && target.alignment(preferred) < 0.12 ? null : target;
    }

    private static double distanceSq(BezierConnection curve, Vec3 feet, double t) {
        Vec3 normal = curve.getNormal(t).normalize();
        return curve.getPosition(t).add(normal.scale(TRACK_SURFACE_OFFSET)).distanceToSqr(feet);
    }

    private static double score(GrindTarget target, Vec3 preferred) {
        double alignment = target.alignment(preferred);
        return target.distanceSq() + (1.0 - alignment) * 0.16;
    }

    private static double lateralOffset(TrackMaterial material) {
        if (material == null || material.trackType == null || material.trackType.id == null) return 0.46;
        String path = material.trackType.id.getPath().toLowerCase();
        if (path.contains("monorail")) return 0.0;
        if (path.contains("narrow")) return 0.26;
        if (path.contains("wide")) return 0.72;
        return 0.46;
    }

    private static double curvature(Vec3 a, Vec3 b) {
        if (a.lengthSqr() < 1.0e-8 || b.lengthSqr() < 1.0e-8) return 0.0;
        double dot = Math.max(-1.0, Math.min(1.0, a.normalize().dot(b.normalize())));
        return Math.max(0.0, 1.0 - dot);
    }

    private static double sq(double v) { return v * v; }
}
