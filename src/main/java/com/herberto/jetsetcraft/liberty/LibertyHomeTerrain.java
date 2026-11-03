package com.herberto.jetsetcraft.liberty;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.PriorityQueue;
import java.util.Set;

/** Read-only home-world route admission. Every sample belongs to an already resident chunk. */
public final class LibertyHomeTerrain {
    private static final int MAX_VISITS = 1024, MAX_LEG_LENGTH = 128, DETOUR_MARGIN = 6;
    private static final List<Direction> STEPS = List.of(Direction.NORTH, Direction.EAST, Direction.SOUTH, Direction.WEST);
    private record Candidate(BlockPos position, int cost, int estimate, long order) { }

    public record GroundedSite(BlockPos center, List<BlockPos> checkpoints, List<List<BlockPos>> escortPaths) {
        public GroundedSite {
            center = center.immutable(); checkpoints = List.copyOf(checkpoints);
            escortPaths = escortPaths.stream().map(List::copyOf).toList();
        }
    }

    public static BlockPos footing(ServerLevel level, BlockPos point) {
        if (level.getChunkSource().getChunkNow(point.getX() >> 4, point.getZ() >> 4) == null) return null;
        BlockPos ground = point.atY(level.getHeight(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES,
                point.getX(), point.getZ()) - 1);
        return level.getBlockState(ground).isFaceSturdy(level, ground, Direction.UP)
                && clear(level, stand(ground)) ? ground : null;
    }

    /** Resolve every waypoint independently; an escort also needs a traversable, bounded ground path. */
    public static GroundedSite resolve(ServerLevel level, LibertyMissionRuntime.Site site) {
        BlockPos center = footing(level, site.center());
        if (center == null) return null;
        List<BlockPos> checkpoints = new ArrayList<>();
        List<List<BlockPos>> paths = new ArrayList<>();
        BlockPos previous = center;
        for (BlockPos checkpoint : site.checkpoints()) {
            BlockPos ground = footing(level, checkpoint);
            if (ground == null) return null;
            checkpoints.add(ground);
            if (site.kind() == LibertyMissionCatalog.ObjectiveKind.ESCORT) {
                List<BlockPos> leg = route(level, previous, ground);
                if (leg.isEmpty()) return null;
                paths.add(leg);
            }
            previous = ground;
        }
        return new GroundedSite(center, checkpoints, paths);
    }

    /** Body clearance is rechecked before each live escort step, so a new obstacle pauses the rig. */
    public static boolean clear(ServerLevel level, Vec3 feet) {
        if (feet == null || !Double.isFinite(feet.x) || !Double.isFinite(feet.y) || !Double.isFinite(feet.z)) return false;
        AABB body = new AABB(feet.x - .35, feet.y, feet.z - .35, feet.x + .35, feet.y + 2, feet.z + .35);
        for (int cx = (int) Math.floor(body.minX) >> 4; cx <= (int) Math.floor(body.maxX) >> 4; cx++)
            for (int cz = (int) Math.floor(body.minZ) >> 4; cz <= (int) Math.floor(body.maxZ) >> 4; cz++)
                if (level.getChunkSource().getChunkNow(cx, cz) == null) return false;
        return level.noCollision(body);
    }

    private static List<BlockPos> route(ServerLevel level, BlockPos start, BlockPos destination) {
        if (start.equals(destination)) return List.of(destination);
        int minX = Math.min(start.getX(), destination.getX()) - DETOUR_MARGIN;
        int maxX = Math.max(start.getX(), destination.getX()) + DETOUR_MARGIN;
        int minZ = Math.min(start.getZ(), destination.getZ()) - DETOUR_MARGIN;
        int maxZ = Math.max(start.getZ(), destination.getZ()) + DETOUR_MARGIN;
        PriorityQueue<Candidate> frontier = new PriorityQueue<>(Comparator.comparingInt(Candidate::estimate)
                .thenComparing(Comparator.comparingInt(Candidate::cost).reversed()).thenComparingLong(Candidate::order));
        Map<BlockPos, BlockPos> previous = new HashMap<>();
        Map<BlockPos, Integer> costs = new HashMap<>();
        Map<Long, BlockPos> samples = new HashMap<>();
        Set<BlockPos> visited = new HashSet<>(); long order = 0;
        frontier.add(new Candidate(start, 0, distance(start, destination), order++));
        previous.put(start, start); costs.put(start, 0);
        while (!frontier.isEmpty() && visited.size() < MAX_VISITS) {
            Candidate candidate = frontier.remove(); BlockPos from = candidate.position();
            if (!visited.add(from)) continue;
            if (from.equals(destination)) {
                List<BlockPos> result = new ArrayList<>();
                for (BlockPos at = from; !at.equals(start); at = previous.get(at)) {
                    if (result.size() >= MAX_LEG_LENGTH) return List.of();
                    result.add(at);
                }
                Collections.reverse(result); return List.copyOf(result);
            }
            for (Direction direction : STEPS) {
                BlockPos column = from.relative(direction);
                if (column.getX() < minX || column.getX() > maxX || column.getZ() < minZ || column.getZ() > maxZ) continue;
                long key = (long) column.getX() << 32 | column.getZ() & 0xffffffffL;
                BlockPos next;
                if (samples.containsKey(key)) next = samples.get(key);
                else { next = footing(level, column); samples.put(key, next); }
                if (next == null || visited.contains(next) || Math.abs(next.getY() - from.getY()) > 1
                        || !clearStep(level, from, next)) continue;
                int cost = candidate.cost() + 1 + Math.abs(next.getY() - from.getY());
                if (cost >= costs.getOrDefault(next, Integer.MAX_VALUE)) continue;
                previous.put(next, from);
                costs.put(next, cost);
                frontier.add(new Candidate(next, cost, cost + distance(next, destination), order++));
            }
        }
        return List.of();
    }

    private static int distance(BlockPos from, BlockPos to) {
        return Math.abs(from.getX() - to.getX()) + Math.abs(from.getY() - to.getY()) + Math.abs(from.getZ() - to.getZ());
    }

    private static boolean clearStep(ServerLevel level, BlockPos from, BlockPos to) {
        // Ascend before crossing a one-block step; descend after crossing its edge.
        double y = Math.max(from.getY(), to.getY()) + 1;
        for (int i = 0; i <= 4; i++) {
            double fraction = i / 4.0;
            if (!clear(level, new Vec3(from.getX() + .5 + (to.getX() - from.getX()) * fraction,
                    y, from.getZ() + .5 + (to.getZ() - from.getZ()) * fraction))) return false;
        }
        return true;
    }

    private static Vec3 stand(BlockPos ground) { return Vec3.atCenterOf(ground).add(0, .5, 0); }
    private LibertyHomeTerrain() { }
}
