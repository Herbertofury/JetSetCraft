package com.herberto.jetsetcraft.gametest;

import com.herberto.jetsetcraft.JetSetCraft;
import com.herberto.jetsetcraft.compat.create.CreateRailProvider;
import com.herberto.jetsetcraft.movement.GrindFinder;
import com.herberto.jetsetcraft.movement.GrindKind;
import com.herberto.jetsetcraft.movement.GrindTarget;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.common.util.FakePlayerFactory;
import net.minecraftforge.registries.ForgeRegistries;

/**
 * Runs only in the dedicated production-Forge/Create acceptance server. The normal product keeps Create optional.
 */
public final class CreateProductionAcceptanceRunner {
    public static void run(ServerLevel level) {
        Block createTrack = ForgeRegistries.BLOCKS.getValue(new ResourceLocation("create", "track"));
        require(createTrack != null && createTrack != Blocks.AIR,
                "Create is loaded but create:track is absent from the production Forge registry");

        BlockPos spawn = level.getSharedSpawnPos();
        int x = spawn.getX() + 24;
        int z = spawn.getZ() + 24;
        int y = level.getHeight(Heightmap.Types.WORLD_SURFACE, x, z);
        BlockPos support = new BlockPos(x, y, z);
        BlockPos track = support.above();
        BlockState oldSupport = level.getBlockState(support);
        BlockState oldTrack = level.getBlockState(track);

        try {
            level.setBlockAndUpdate(support, Blocks.STONE.defaultBlockState());
            level.setBlockAndUpdate(track, createTrack.defaultBlockState());

            ServerPlayer player = FakePlayerFactory.getMinecraft(level);
            // Create's default TrackShape.ZO is north/south. TrackMaterial's standard gauge places each visible
            // rail bar about 0.46 blocks from the center. Stand over the east bar to detect centerline regressions.
            double expectedRailX = track.getX() + 0.96;
            player.moveTo(expectedRailX, track.getY() + 0.11, track.getZ() + 0.50, 0.0F, 0.0F);
            player.setOnGround(true);
            player.setDeltaMovement(new Vec3(0.0, 0.0, 0.34));
            Vec3 preferred = new Vec3(0.0, 0.0, 1.0);

            CreateRailProvider provider = new CreateRailProvider();
            GrindTarget direct = provider.findBest(player, preferred, 0.70, 0.82)
                    .orElseThrow(() -> new IllegalStateException(
                            "Native CreateRailProvider did not resolve the real production Create track"));
            assertRailBar(track, expectedRailX, direct, "direct provider");

            GrindTarget universal = GrindFinder.findBest(player, preferred, GrindKind.CREATE_TRACK)
                    .orElseThrow(() -> new IllegalStateException(
                            "Universal GrindFinder did not resolve the real production Create track"));
            assertRailBar(track, expectedRailX, universal, "universal finder");

            JetSetCraft.LOGGER.info(
                    "JETSETCRAFT_CREATE_RUNTIME_PASS createLoaded=true registryTrack=true directKind={} universalKind={} railX={} centerlineRejected=true",
                    direct.kind(), universal.kind(), universal.point().x);
        } finally {
            level.setBlockAndUpdate(track, oldTrack);
            level.setBlockAndUpdate(support, oldSupport);
        }
    }

    private static void assertRailBar(BlockPos track, double expectedRailX, GrindTarget target, String stage) {
        require(target.kind() == GrindKind.CREATE_TRACK,
                stage + " selected " + target.kind() + " instead of CREATE_TRACK");
        Vec3 tangent = target.tangent().normalize();
        require(Math.abs(tangent.z) > 0.90,
                stage + " returned the wrong default Z-axis tangent: " + target.tangent());

        double centerX = track.getX() + 0.50;
        require(Math.abs(target.point().x - centerX) >= 0.30,
                stage + " snapped to Create's invisible centerline instead of a physical rail bar: " + target.point());
        require(Math.abs(target.point().x - expectedRailX) <= 0.12,
                stage + " missed the physical Create rail bar: expected x~" + expectedRailX + " got " + target.point().x);
    }

    private static void require(boolean condition, String message) {
        if (!condition) throw new IllegalStateException(message);
    }

    private CreateProductionAcceptanceRunner() {}
}
