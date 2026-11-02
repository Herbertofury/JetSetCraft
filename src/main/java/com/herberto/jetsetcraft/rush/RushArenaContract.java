package com.herberto.jetsetcraft.rush;

import com.herberto.jetsetcraft.network.JetSetNetwork;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.joml.Vector3f;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

/** One optional room goal. It observes accepted server events and never grants a reward or clears a room. */
public final class RushArenaContract {
    private static final double RELAY_RADIUS = 2.25;
    private static final DustParticleOptions RELAY_COLOR = new DustParticleOptions(new Vector3f(.35f, .95f, 1), 1.1f);
    private final RushArenaCatalog.ContractSpec spec;
    private final List<BlockPos> beacons;
    private final Set<String> painted = new HashSet<>();
    private String state, reason = "";
    private int progress;
    private long sampledAt = Long.MIN_VALUE;
    private Vec3 previous;
    private boolean armed, finished;

    RushArenaContract(RushArenaCatalog.Profile profile, BlockPos center, UUID room) {
        spec = profile.contract(); state = spec.type() == RushArenaCatalog.ContractType.NONE ? "none" : "preview";
        int distance = Math.min(profile.radius() - 10, 24);
        List<BlockPos> cross = List.of(center.offset(0, 1, -distance), center.offset(distance, 1, 0),
                center.offset(0, 1, distance), center.offset(-distance, 1, 0));
        int rotation = Math.floorMod(room.hashCode(), 4);
        beacons = java.util.stream.IntStream.range(0, spec.type() == RushArenaCatalog.ContractType.RELAY ? spec.target() : 0)
                .mapToObj(index -> cross.get((rotation + index) % 4)).toList();
    }

    void begin() {
        if (state.equals("preview")) state = spec.type() == RushArenaCatalog.ContractType.RELAY ? "pending" : "active";
        pause();
    }

    /** Only the manager's existing single physical-motion sample may advance a relay. */
    boolean tick(ServerPlayer player, RushMotionTracker.Step motion, RushArenaBuilder builder, long activeTick) {
        if (finished || completed() || state.equals("disabled") || spec.type() != RushArenaCatalog.ContractType.RELAY
                || state.equals("preview") || activeTick == sampledAt) return false;
        sampledAt = activeTick;
        BlockPos beacon = beacons.get(progress);
        if (!ready(player.serverLevel(), builder, beacon)) { pause(); return false; }
        if (state.equals("pending")) { state = "active"; reason = ""; pause(); }
        Vec3 position = player.position(), last = previous; previous = position;
        if (activeTick % 8 == 0) marker(player, beacon);
        if (!finite(position) || last == null || !finite(last)) {
            armed = finite(position) && outside(position, beacon); return false;
        }
        Vec3 delta = position.subtract(last);
        if (delta.horizontalDistance() > 4 || Math.abs(delta.y) > 4) { armed = false; return false; }
        // Paused baselines, vehicles, swimming, flight and discontinuities supply no riding distance.
        if (!Double.isFinite(motion.ridingDistance()) || motion.ridingDistance() <= .005 || motion.ridingDistance() > 4) {
            armed = outside(position, beacon); return false;
        }
        if (!armed) { armed = outside(position, beacon); return false; }
        if (!crosses(last, position, beacon)) return false;
        progress++; pause();
        if (progress >= spec.target()) { state = "complete"; reason = ""; return true; }
        state = "pending"; reason = "Ride toward the next ordered beacon.";
        return false;
    }

    /** A temporarily unloaded point waits; a built but unsafe point explicitly disables this optional goal. */
    private boolean ready(ServerLevel level, RushArenaBuilder builder, BlockPos beacon) {
        boolean loaded = true;
        for (int x = -1; x <= 1; x++) for (int z = -1; z <= 1; z++)
            loaded &= level.getChunkSource().hasChunk((beacon.getX() + x) >> 4, (beacon.getZ() + z) >> 4);
        if (builder == null || !builder.columnReady(beacon) || !loaded) {
            state = "pending"; reason = "The next beacon is waiting for its arena blocks to load."; return false;
        }
        AABB standing = new AABB(beacon.getX() + .15, beacon.getY(), beacon.getZ() + .15,
                beacon.getX() + .85, beacon.getY() + 1.85, beacon.getZ() + .85);
        if (!level.getBlockState(beacon.below()).isCollisionShapeFullBlock(level, beacon.below())
                || level.getBlockCollisions(null, standing).iterator().hasNext()) {
            state = "disabled"; reason = "Relay unavailable: a beacon has no clear standing space. The normal room reward is unchanged.";
            return false;
        }
        return true;
    }

    boolean paint(String patch) {
        if (finished || !state.equals("active") || spec.type() != RushArenaCatalog.ContractType.PAINT
                || patch == null || patch.length() > 96 || painted.size() >= spec.target() || !painted.add(patch)) return false;
        return advance();
    }

    boolean hunt() {
        if (finished || !state.equals("active") || spec.type() != RushArenaCatalog.ContractType.HUNT) return false;
        return advance();
    }

    private boolean advance() {
        progress = Math.min(spec.target(), progress + 1);
        if (progress == spec.target()) { state = "complete"; return true; }
        return false;
    }

    void pause() { previous = null; armed = false; }
    boolean completed() { return state.equals("complete"); }
    void finish() {
        finished = true; pause();
        if (state.equals("active") || state.equals("pending")) {
            state = "missed"; reason = "The room cleared before this optional goal was completed.";
        }
    }

    CompoundTag snapshot(Vec3 viewer) {
        CompoundTag tag = spec.summary(); tag.putInt("Progress", progress); tag.putString("State", state);
        tag.putString("Reason", reason); tag.putBoolean("Completed", completed());
        if (spec.type() == RushArenaCatalog.ContractType.RELAY && progress < beacons.size()) {
            BlockPos next = beacons.get(progress); Vec3 point = Vec3.atBottomCenterOf(next);
            tag.putInt("X", next.getX()); tag.putInt("Y", next.getY()); tag.putInt("Z", next.getZ());
            tag.putInt("Checkpoint", progress + 1); tag.putInt("Checkpoints", beacons.size());
            if (finite(viewer)) {
                tag.putInt("Distance", (int) Math.min(Integer.MAX_VALUE, Math.sqrt(viewer.distanceToSqr(point))));
                String[] directions = {"N", "NE", "E", "SE", "S", "SW", "W", "NW"};
                int direction = Math.floorMod((int) Math.round(Math.atan2(point.x - viewer.x, viewer.z - point.z) / (Math.PI / 4)), 8);
                tag.putString("Direction", directions[direction]);
            }
        }
        return tag;
    }

    private static boolean outside(Vec3 point, BlockPos beacon) {
        Vec3 center = Vec3.atBottomCenterOf(beacon);
        double x = point.x - center.x, z = point.z - center.z;
        return x * x + z * z > (RELAY_RADIUS + .5) * (RELAY_RADIUS + .5);
    }

    private static boolean crosses(Vec3 from, Vec3 to, BlockPos beacon) {
        Vec3 center = Vec3.atBottomCenterOf(beacon), segment = to.subtract(from);
        double length = segment.x * segment.x + segment.z * segment.z;
        if (length < .000001) return false;
        double fraction = ((center.x - from.x) * segment.x + (center.z - from.z) * segment.z) / length;
        fraction = Math.max(0, Math.min(1, fraction)); Vec3 nearest = from.add(segment.scale(fraction));
        double x = nearest.x - center.x, z = nearest.z - center.z;
        return x * x + z * z <= RELAY_RADIUS * RELAY_RADIUS && Math.abs(nearest.y - center.y) <= 2;
    }

    private static void marker(ServerPlayer player, BlockPos beacon) {
        if (!JetSetNetwork.canSync(player)) return;
        for (int index = 0; index < 12; index++) {
            double angle = index * Math.PI / 6;
            player.serverLevel().sendParticles(player, RELAY_COLOR, false,
                    beacon.getX() + .5 + Math.cos(angle) * RELAY_RADIUS, beacon.getY() + .12,
                    beacon.getZ() + .5 + Math.sin(angle) * RELAY_RADIUS, 1, 0, 0, 0, 0);
        }
    }

    private static boolean finite(Vec3 point) {
        return point != null && Double.isFinite(point.x) && Double.isFinite(point.y) && Double.isFinite(point.z);
    }
}
