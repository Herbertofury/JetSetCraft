package com.herberto.jetsetcraft.liberty;

import com.herberto.jetsetcraft.JetSetCraft;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.StairBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.LevelChunk;

/** Wide, separate expedition districts. Only already loaded chunks are decorated, one per call. */
public final class LibertyWorld {
    public static final ResourceKey<Level> DIMENSION = ResourceKey.create(Registries.DIMENSION,
            ResourceLocation.fromNamespaceAndPath(JetSetCraft.MOD_ID, "liberty_grind"));
    public static final int FLOOR_Y = 63;
    public static boolean isLiberty(Level level) { return level != null && level.dimension().equals(DIMENSION); }
    public static boolean inside(LibertyOperation operation, BlockPos pos) {
        return Math.abs(pos.getX() - operation.origin.getX()) <= operation.theater.radius()
                && Math.abs(pos.getZ() - operation.origin.getZ()) <= operation.theater.radius();
    }
    public static void decorateNear(ServerLevel level, BlockPos player, LibertyOperation operation) {
        if (!isLiberty(level)) return;
        int cx = player.getX() >> 4, cz = player.getZ() >> 4;
        for (int radius = 0; radius <= 3; radius++) for (int dx = -radius; dx <= radius; dx++) for (int dz = -radius; dz <= radius; dz++) {
            if (Math.max(Math.abs(dx), Math.abs(dz)) != radius) continue;
            LevelChunk chunk = level.getChunkSource().getChunkNow(cx + dx, cz + dz);
            if (chunk == null) continue;
            int x = chunk.getPos().getMinBlockX(), z = chunk.getPos().getMinBlockZ();
            BlockPos marker = new BlockPos(x + 15, FLOOR_Y - 1, z + 15);
            if (chunk.getBlockState(marker).is(Blocks.SEA_LANTERN) || !inside(operation, marker)) continue;
            decorate(level, x, z, operation); level.setBlock(marker, Blocks.SEA_LANTERN.defaultBlockState(), 2); return;
        }
    }
    private static boolean reserved(LibertyOperation operation, int x, int z) {
        if (Math.abs(x - operation.drop.getX()) < 12 && Math.abs(z - operation.drop.getZ()) < 12) return true;
        if (Math.abs(x - operation.extraction.getX()) < 14 && Math.abs(z - operation.extraction.getZ()) < 14) return true;
        for (LibertyOperation.Objective objective : operation.objectives)
            if (Math.abs(x - objective.center().getX()) < 30 && Math.abs(z - objective.center().getZ()) < 36) return true;
        if (operation.mission != null) for (LibertyMissionRuntime.Site site : operation.mission.sites())
            if (Math.abs(x - site.center().getX()) < 36 && Math.abs(z - site.center().getZ()) < 36) return true;
        for (int i = 0; i < 9; i++) if (Math.abs(x - operation.cachePosition(i).getX()) < 5
                && Math.abs(z - operation.cachePosition(i).getZ()) < 5) return true;
        return false;
    }
    private static void decorate(ServerLevel level, int chunkX, int chunkZ, LibertyOperation operation) {
        BlockState accent = switch (operation.theater.id()) {
            case "overpass_uprising" -> Blocks.ORANGE_CONCRETE.defaultBlockState();
            case "blackout_borough" -> Blocks.MAGENTA_CONCRETE.defaultBlockState();
            default -> Blocks.CYAN_CONCRETE.defaultBlockState();
        };
        // Bounded 256-column pass; generous boulevards remain flat and continuous across chunks.
        for (int x = chunkX; x < chunkX + 16; x++) for (int z = chunkZ; z < chunkZ + 16; z++) {
            int lx = Math.floorMod(x - operation.origin.getX(), 48), lz = Math.floorMod(z - operation.origin.getZ(), 48);
            if (lx == 4 || lz == 4) put(level, x, FLOOR_Y, z, accent);
            if (reserved(operation, x, z)) continue;
            if ((lx == 1 && lz % 6 < 3) || (lz == 1 && lx % 6 < 3)) put(level, x, FLOOR_Y, z, Blocks.WHITE_CONCRETE.defaultBlockState());
            if (lx == 12 && lz >= 10 && lz <= 34) put(level, x, FLOOR_Y + 1, z, Blocks.IRON_BARS.defaultBlockState());
            if (lx >= 20 && lx <= 34 && lz == 32) for (int h = 1; h <= 5; h++)
                put(level, x, FLOOR_Y + h, z, h == 5 ? accent : Blocks.LIGHT_GRAY_CONCRETE.defaultBlockState());
            if (lx >= 20 && lx <= 24 && lz == 12) put(level, x, FLOOR_Y + 1, z,
                    Blocks.SMOOTH_QUARTZ_STAIRS.defaultBlockState().setValue(StairBlock.FACING, Direction.SOUTH));
            if (lx >= 20 && lx <= 24 && lz >= 13 && lz <= 16) put(level, x, FLOOR_Y + 1, z, Blocks.SMOOTH_QUARTZ.defaultBlockState());
            if (lx == 37 && lz == 16) { put(level, x, FLOOR_Y + 1, z, Blocks.SMOOTH_STONE.defaultBlockState());
                put(level, x, FLOOR_Y + 2, z, Blocks.SEA_LANTERN.defaultBlockState()); }
            if (operation.campaign()) {
                long blockSeed = operation.plan.seed() ^ Math.floorDiv(x - operation.origin.getX(), 48) * 73428767L
                        ^ Math.floorDiv(z - operation.origin.getZ(), 48) * 912931L;
                int height = 6 + (int) Math.floorMod(blockSeed, 4) * 4;
                boolean building = lx >= 20 && lx <= 35 && lz >= 20 && lz <= 30;
                if (building && (lx == 20 || lx == 35 || lz == 20 || lz == 30)) {
                    for (int y = 1; y <= height; y++) {
                        if (lz == 20 && lx >= 25 && lx <= 28 && y <= 3) continue;
                        BlockState material = y % 4 == 0 ? accent : y % 4 == 2 && (lx + lz) % 3 != 0
                                ? Blocks.TINTED_GLASS.defaultBlockState() : Blocks.GRAY_CONCRETE.defaultBlockState();
                        put(level, x, FLOOR_Y + y, z, material);
                    }
                }
                if (building) put(level, x, FLOOR_Y + height + 1, z, Blocks.SMOOTH_STONE.defaultBlockState());
                // Continuous street furniture, sloped transfers and a second rail line preserve skating routes.
                if (lx == 8 && lz >= 7 && lz <= 39) put(level, x, FLOOR_Y + 1, z, Blocks.IRON_BARS.defaultBlockState());
                if (lx >= 38 && lx <= 42 && lz >= 23 && lz <= 29)
                    put(level, x, FLOOR_Y + 1 + Math.min(2, (lz - 23) / 2), z, Blocks.SMOOTH_QUARTZ_STAIRS.defaultBlockState());
            }
        }
    }
    public static BlockPos campaignOrigin(int slot) {
        return new BlockPos(-10_000_008 + slot % 4096 * 2048, FLOOR_Y, -10_000_008 + slot / 4096 * 2048);
    }
    public static BlockPos shipOrigin(int slot) {
        return new BlockPos(-20_000_008 + slot % 4096 * 128, FLOOR_Y, -20_000_008 + slot / 4096 * 128);
    }
    public static boolean buildSite(ServerLevel level, LibertyMissionRuntime.Site site, LibertyMissionCatalog.Faction faction) {
        if (!isLiberty(level) || !loaded(level, site.center(), 24)) return false;
        BlockPos c = site.center();
        BlockState accent = (faction == LibertyMissionCatalog.Faction.AUTOMATON ? Blocks.RED_CONCRETE
                : faction == LibertyMissionCatalog.Faction.TERMINID ? Blocks.LIME_CONCRETE : Blocks.PURPLE_CONCRETE).defaultBlockState();
        for (int x=-20;x<=20;x++) for (int z=-20;z<=20;z++) {
            if (Math.abs(x)==20 || Math.abs(z)==20 || x%8==0 && z%8==0) level.setBlock(c.offset(x,0,z),accent,2);
            if ((Math.abs(x)==18 || Math.abs(z)==18) && Math.abs(x)>4 && Math.abs(z)>4)
                level.setBlock(c.offset(x,1,z),Blocks.POLISHED_DEEPSLATE_WALL.defaultBlockState(),2);
        }
        // Paintable floor and walls are both useful; territory is measured per cell by the mission rules.
        for (int x=-12;x<=12;x++) for (int y=1;y<=4;y++)
            level.setBlock(c.offset(x,y,-15), y==4 ? accent : Blocks.WHITE_CONCRETE.defaultBlockState(),2);
        if (site.kind()==LibertyMissionCatalog.ObjectiveKind.RELAY_HOLD || site.kind()==LibertyMissionCatalog.ObjectiveKind.SCAN) {
            for (int y=1;y<=10;y++) level.setBlock(c.offset(0,y,0),Blocks.IRON_BARS.defaultBlockState(),2);
            level.setBlock(c.above(11),Blocks.SEA_LANTERN.defaultBlockState(),2);
            level.setBlock(c.offset(2,1,2),Blocks.LODESTONE.defaultBlockState(),2);
        }
        return true;
    }
    /** A private walkable ship deck. No inventory-bearing blocks or third-party assets. */
    public static boolean buildShip(ServerLevel level, BlockPos c) {
        if (!isLiberty(level) || !loaded(level,c,24)) return false;
        for (int x=-20;x<=20;x++) for (int z=-24;z<=24;z++) {
            if (Math.abs(x)+Math.max(0,-z-12)>23) continue;
            level.setBlock(c.offset(x,0,z),(Math.abs(x)%8==0 ? Blocks.YELLOW_CONCRETE : Blocks.POLISHED_DEEPSLATE).defaultBlockState(),2);
            if (Math.abs(x)==20 || Math.abs(z)==24) for (int y=1;y<=8;y++)
                level.setBlock(c.offset(x,y,z),(y>=3&&y<=6 ? Blocks.CYAN_STAINED_GLASS : Blocks.GRAY_CONCRETE).defaultBlockState(),2);
            if (Math.abs(x)==20 || Math.abs(z)==24 || Math.abs(x)%10==0) level.setBlock(c.offset(x,9,z),Blocks.GRAY_CONCRETE.defaultBlockState(),2);
            if (x%8==0&&z%8==0) level.setBlock(c.offset(x,0,z),Blocks.SEA_LANTERN.defaultBlockState(),2);
        }
        for (int x=-4;x<=4;x++) for (int z=-8;z<=-3;z++)
            level.setBlock(c.offset(x,1,z),(Math.abs(x)==4||z==-8||z==-3 ? Blocks.IRON_BLOCK : Blocks.CYAN_STAINED_GLASS).defaultBlockState(),2);
        for (int x:new int[]{-12,12}) for (int z=2;z<=9;z++) {
            level.setBlock(c.offset(x,1,z),Blocks.IRON_BLOCK.defaultBlockState(),2);
            if (z%3==0) level.setBlock(c.offset(x,2,z),Blocks.SEA_LANTERN.defaultBlockState(),2);
        }
        return true;
    }
    /** Small objective builds wait for all affected chunks; these methods never request chunks. */
    public static boolean buildObjective(ServerLevel level, LibertyOperation.Objective objective) {
        if (!isLiberty(level) || !loaded(level, objective.center(), 30)) return false;
        BlockPos center = objective.center();
        for (int x = -8; x <= 8; x++) for (int z = -3; z <= 6; z++)
            put(level, center.getX() + x, FLOOR_Y, center.getZ() + z, Blocks.POLISHED_DEEPSLATE.defaultBlockState());
        for (int p = 0; p < objective.panels(); p++) {
            BlockPos panel = objective.panel(p);
            for (int x = -1; x <= 1; x++) for (int y = -1; y <= 1; y++)
                level.setBlock(panel.offset(x, y, 0), Blocks.WHITE_CONCRETE.defaultBlockState(), 2);
            level.setBlock(panel.above(2), Blocks.YELLOW_CONCRETE.defaultBlockState(), 2);
            level.setBlock(panel.below(2).south(), Blocks.SEA_LANTERN.defaultBlockState(), 2);
        }
        if (objective.index() == 1) {
            for (int y = 1; y <= 9; y++) level.setBlock(center.offset(6, y, -2), Blocks.IRON_BARS.defaultBlockState(), 2);
            level.setBlock(center.offset(6, 10, -2), Blocks.SEA_LANTERN.defaultBlockState(), 2);
        }
        if (objective.index() == 2) for (int i = 0; i < 3; i++) {
            BlockPos beacon = objective.beacon(i);
            for (int x = -2; x <= 2; x++) for (int z = -2; z <= 2; z++)
                level.setBlock(beacon.offset(x, 0, z), (i == 0 ? Blocks.LIME_CONCRETE : i == 1 ? Blocks.CYAN_CONCRETE : Blocks.MAGENTA_CONCRETE).defaultBlockState(), 2);
            level.setBlock(beacon, Blocks.SEA_LANTERN.defaultBlockState(), 2);
        }
        return true;
    }
    public static boolean buildLanding(ServerLevel level, BlockPos center) {
        if (!isLiberty(level) || !loaded(level, center, 8)) return false;
        for (int x = -7; x <= 7; x++) for (int z = -7; z <= 7; z++)
            level.setBlock(center.offset(x, 0, z), (Math.abs(x) == 7 || Math.abs(z) == 7 ? Blocks.YELLOW_CONCRETE : Blocks.POLISHED_DEEPSLATE).defaultBlockState(), 2);
        for (int x : new int[]{-6, 6}) for (int z : new int[]{-6, 6}) level.setBlock(center.offset(x, 0, z), Blocks.SEA_LANTERN.defaultBlockState(), 2);
        return true;
    }
    private static boolean loaded(ServerLevel level, BlockPos center, int radius) {
        for (int x = (center.getX() - radius) >> 4; x <= (center.getX() + radius) >> 4; x++)
            for (int z = (center.getZ() - radius) >> 4; z <= (center.getZ() + radius) >> 4; z++)
                if (level.getChunkSource().getChunkNow(x, z) == null) return false;
        return true;
    }
    private static void put(ServerLevel level, int x, int y, int z, BlockState state) { level.setBlock(new BlockPos(x, y, z), state, 2); }
    private LibertyWorld() {}
}
