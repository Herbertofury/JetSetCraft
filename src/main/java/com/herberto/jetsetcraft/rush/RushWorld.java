package com.herberto.jetsetcraft.rush;

import com.herberto.jetsetcraft.JetSetCraft;
import com.herberto.jetsetcraft.registry.ModBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.StairBlock;
import net.minecraft.world.level.chunk.LevelChunk;

/** A separate, generated street district. Decorations never request a chunk or edit a source world. */
public final class RushWorld {
    public static final ResourceKey<Level> DIMENSION = ResourceKey.create(Registries.DIMENSION,
            ResourceLocation.fromNamespaceAndPath(JetSetCraft.MOD_ID, "rival_rush"));
    public static final int FLOOR_Y = 63;

    public static boolean isRush(Level level) { return level != null && level.dimension().equals(DIMENSION); }

    /** At most one already-loaded chunk per call; generation work is spread over ordinary server ticks. */
    public static void decorateNear(ServerLevel level, BlockPos center) {
        if (!isRush(level)) return;
        int cx = center.getX() >> 4, cz = center.getZ() >> 4;
        for (int radius = 0; radius <= 2; radius++) {
            for (int x = -radius; x <= radius; x++) for (int z = -radius; z <= radius; z++) {
                if (Math.max(Math.abs(x), Math.abs(z)) != radius) continue;
                LevelChunk chunk = level.getChunkSource().getChunkNow(cx + x, cz + z);
                if (chunk == null || decorated(chunk)) continue;
                decorate(level, chunk);
                return;
            }
        }
    }

    private static void decorate(ServerLevel level, LevelChunk chunk) {
        int x = chunk.getPos().getMinBlockX(), z = chunk.getPos().getMinBlockZ();
        int district = Math.floorMod(chunk.getPos().x / 8 + chunk.getPos().z / 8, 4);
        var accent = switch (district) {
            case 0 -> Blocks.CYAN_CONCRETE;
            case 1 -> Blocks.MAGENTA_CONCRETE;
            case 2 -> Blocks.LIME_CONCRETE;
            default -> Blocks.ORANGE_CONCRETE;
        };
        // Clear eight-block avenues continue through every chunk and every district.
        for (int i = 0; i < 16; i++) {
            if (i % 4 < 2) {
                place(level, x + 1, FLOOR_Y, z + i, Blocks.WHITE_CONCRETE.defaultBlockState());
                place(level, x + i, FLOOR_Y, z + 1, Blocks.WHITE_CONCRETE.defaultBlockState());
            }
            place(level, x + 3, FLOOR_Y, z + i, accent.defaultBlockState());
            place(level, x + i, FLOOR_Y, z + 3, accent.defaultBlockState());
        }
        // An approach, readable wall, rail, small ramp and an open recovery side.
        for (int i = 5; i <= 11; i++) for (int h = 1; h <= 4; h++)
            place(level, x + 13, FLOOR_Y + h, z + i,
                    (h == 4 ? accent : Blocks.LIGHT_GRAY_CONCRETE).defaultBlockState());
        for (int i = 5; i <= 10; i++)
            place(level, x + i, FLOOR_Y + 1, z + 12, Blocks.IRON_BARS.defaultBlockState());
        for (int i = 5; i <= 7; i++) {
            place(level, x + i, FLOOR_Y + 1, z + 5,
                    Blocks.SMOOTH_QUARTZ_STAIRS.defaultBlockState().setValue(StairBlock.FACING, Direction.SOUTH));
            place(level, x + i, FLOOR_Y + 1, z + 6, Blocks.SMOOTH_QUARTZ.defaultBlockState());
        }
        if (Math.floorMod(chunk.getPos().x + chunk.getPos().z, 3) == 0) {
            place(level, x + 9, FLOOR_Y, z + 8, Blocks.SEA_LANTERN.defaultBlockState());
            place(level, x + 9, FLOOR_Y + 1, z + 8, ModBlocks.BOOMBOX.get().defaultBlockState());
        }
        // Written last: the persistent in-world marker also survives a reload without a global chunk index.
        place(level, x + 15, FLOOR_Y, z + 15, Blocks.SEA_LANTERN.defaultBlockState());
    }

    public static BlockPos nearestBoombox(ServerLevel level, BlockPos center) {
        BlockPos best = null;
        double distance = Double.MAX_VALUE;
        int cx = center.getX() >> 4, cz = center.getZ() >> 4;
        for (int dx = -2; dx <= 2; dx++) for (int dz = -2; dz <= 2; dz++) {
            LevelChunk chunk = level.getChunkSource().getChunkNow(cx + dx, cz + dz);
            if (chunk == null || !decorated(chunk)) continue;
            BlockPos candidate = new BlockPos((cx + dx) * 16 + 9, FLOOR_Y + 1, (cz + dz) * 16 + 8);
            if (!level.getBlockState(candidate).is(ModBlocks.BOOMBOX.get())) continue;
            double squared = candidate.distSqr(center);
            if (squared < distance) { distance = squared; best = candidate; }
        }
        return best;
    }

    private static void place(ServerLevel level, int x, int y, int z,
                              net.minecraft.world.level.block.state.BlockState state) {
        level.setBlock(new BlockPos(x, y, z), state, 2);
    }

    private static boolean decorated(LevelChunk chunk) {
        return chunk.getBlockState(new BlockPos(chunk.getPos().getMinBlockX() + 15, FLOOR_Y,
                chunk.getPos().getMinBlockZ() + 15)).is(Blocks.SEA_LANTERN);
    }

    private RushWorld() {}
}
