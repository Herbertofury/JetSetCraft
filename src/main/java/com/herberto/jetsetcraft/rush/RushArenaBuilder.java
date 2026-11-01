package com.herberto.jetsetcraft.rush;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Mth;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import com.herberto.jetsetcraft.registry.ModBlocks;

import java.util.ArrayDeque;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/** Incremental construction exclusively inside the pre-registered Rush dimension and this run's reserved cell. */
public final class RushArenaBuilder {
    private final RushArenaCatalog.Profile profile;
    private final BlockPos center;
    private final ArrayDeque<BlockPos> pending = new ArrayDeque<>();
    private final Set<Long> connector = new HashSet<>();
    private final Map<Long, BlockState> authored = new HashMap<>();
    private final Set<Long> unfinished = new HashSet<>();

    public RushArenaBuilder(RushArenaCatalog.Profile profile, BlockPos from, BlockPos center) {
        this.profile = profile; this.center = center;
        Set<Long> columns = new HashSet<>();
        int length = Math.max(Math.abs(center.getX() - from.getX()), Math.abs(center.getZ() - from.getZ()));
        // A broad diagonal path preserves normal movement; grind edges and regular accent strips provide lines.
        for (int step = 0; step <= length; step++) {
            double fraction = length == 0 ? 0 : (double) step / length;
            int x = Mth.floor(Mth.lerp(fraction, from.getX(), center.getX()));
            int z = Mth.floor(Mth.lerp(fraction, from.getZ(), center.getZ()));
            for (int dx = -4; dx <= 4; dx++) for (int dz = -4; dz <= 4; dz++) {
                BlockPos pos = new BlockPos(x + dx, RushWorld.FLOOR_Y, z + dz);
                if (columns.add(pos.asLong())) pending.add(pos);
                connector.add(pos.asLong());
            }
        }
        int r = profile.radius();
        for (int radius = 0; radius <= r; radius++) {
            for (int x = -radius; x <= radius; x++) for (int z = -radius; z <= radius; z++) {
                if (Math.max(Math.abs(x), Math.abs(z)) != radius) continue;
                BlockPos pos = center.offset(x, 0, z);
                if (columns.add(pos.asLong())) pending.add(pos);
            }
        }
        for (RushArenaCatalog.MapBlock block : profile.blocks()) authored.put(center.offset(block.offset()).asLong(), block.state());
        pending.forEach(p -> unfinished.add(p.asLong()));
    }

    /** At most 192 columns/1728 block comparisons per tick; unloaded columns stay queued, without chunk requests. */
    public void step(ServerLevel level) {
        if (!RushWorld.isRush(level)) throw new IllegalArgumentException("Arena construction is isolated to Rival Rush");
        int attempts = Math.min(192, pending.size());
        for (int i = 0; i < attempts; i++) {
            BlockPos column = pending.removeFirst();
            if (level.getChunkSource().getChunkNow(column.getX() >> 4, column.getZ() >> 4) == null) {
                pending.addLast(column); continue;
            }
            for (int y = 0; y <= 8; y++) {
                BlockPos pos = column.above(y);
                BlockState state = desired(column.getX() - center.getX(), y, column.getZ() - center.getZ(), pos);
                if (!level.getBlockState(pos).equals(state)) level.setBlock(pos, state, 2);
            }
            unfinished.remove(column.asLong());
        }
    }

    public boolean ready() { return pending.isEmpty(); }
    /** Low simulation distances may not load every outer corner until the rider approaches it. */
    public boolean readyForEntry() {
        return pending.stream().noneMatch(p -> Math.abs(p.getX() - center.getX()) <= 12 && Math.abs(p.getZ() - center.getZ()) <= 12);
    }
    public int remainingColumns() { return pending.size(); }
    public boolean columnReady(BlockPos pos) { return !unfinished.contains(new BlockPos(pos.getX(), RushWorld.FLOOR_Y, pos.getZ()).asLong()); }
    public BlockPos center() { return center; }

    private BlockState desired(int x, int y, int z, BlockPos pos) {
        int r = profile.radius();
        boolean room = Math.abs(x) <= r && Math.abs(z) <= r;
        if (!room) {
            if (y == 0) return Math.floorMod(pos.getX() + pos.getZ(), 12) < 2 ? profile.accent() : profile.floor();
            boolean edge = !connector.contains(pos.below(y).east().asLong()) || !connector.contains(pos.below(y).west().asLong());
            if (y == 1 && edge && Math.floorMod(pos.getZ(), 12) < 8) return profile.rail();
            return Blocks.AIR.defaultBlockState();
        }
        if (y == 0) {
            if (x == 0 && z == r - 6) return Blocks.SEA_LANTERN.defaultBlockState();
            if (Math.abs(x) == r - 2 || Math.abs(z) == r - 2 || Math.abs(x) == 4 || Math.abs(z) == 4) return profile.accent();
            if (Math.abs(x) == r - 7 && Math.abs(z) == r - 7) return Blocks.SEA_LANTERN.defaultBlockState();
            return profile.floor();
        }
        // A diagonal branch must cut a real entrance through the perimeter, rails and authored blocks.
        if (connector.contains(pos.below(y).asLong())) return Blocks.AIR.defaultBlockState();
        BlockState custom = authored.get(pos.asLong());
        if (custom != null) return custom;
        if (x == 0 && z == r - 6 && y == 1) return ModBlocks.BOOMBOX.get().defaultBlockState();
        // Four broad open entrances and a central crossing stay clear in every authored map.
        if (Math.abs(x) <= 4 || Math.abs(z) <= 4) return Blocks.AIR.defaultBlockState();
        if ((Math.abs(x) == r || Math.abs(z) == r) && y <= 3) return y == 3 ? profile.accent() : profile.wall();
        if (profile.layout().equals("loop") && y == 1 && (Math.abs(x) == r - 8 || Math.abs(z) == r - 8)) return profile.rail();
        if (profile.layout().equals("cross") && y <= 3 && Math.abs(x) == r / 2 && Math.abs(z) > 8 && Math.abs(z) < r - 6)
            return y == 3 ? profile.accent() : profile.wall();
        if (profile.layout().equals("terraces") && x > 6 && x < r - 5 && z > 6 && z < r - 5) {
            if (y <= Math.min(3, (x - 6) / 3 + 1)) return profile.floor();
        }
        if (profile.layout().equals("plaza") && y == 1 && z == r / 2 && x > -r + 6 && x < -6) return profile.rail();
        return Blocks.AIR.defaultBlockState();
    }
}
