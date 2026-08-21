package com.herberto.jetsetcraft.movement;

import com.herberto.jetsetcraft.JetSetCraft;
import com.mojang.authlib.GameProfile;
import net.minecraft.core.BlockPos;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.Vec3;

import java.util.Optional;
import java.util.UUID;

/**
 * CI-only real-world verifier for the collision-shape edge solver.
 *
 * This is dormant in normal gameplay and is invoked only when the dedicated-server smoke sets
 * {@code -Djetsetcraft.ci.edgeRuntime=true}. It uses an actual ServerLevel and ServerPlayer so
 * exposed-edge behavior is proved against Minecraft collision geometry rather than a mock shape.
 */
public final class EdgeRuntimeVerifier {
    private static final UUID PROBE_UUID = UUID.fromString("0c229e0f-383f-4d6f-8be6-531488c35ec0");

    public static void run(MinecraftServer server) {
        ServerLevel level = server.overworld();
        BlockPos spawn = level.getSharedSpawnPos();
        int baseY = Math.min(level.getMaxBuildHeight() - 10,
                Math.max(level.getMinBuildHeight() + 10, spawn.getY() + 16));
        BlockPos origin = new BlockPos(spawn.getX(), baseY, spawn.getZ());
        ServerPlayer probe = new ServerPlayer(server, level,
                new GameProfile(PROBE_UUID, "JetSetCraftEdgeProbe"));

        try {
            verifyInternalSeamsRejected(level, probe, origin);
            verifyExposedLedgeDetected(level, probe, origin);
            verifyBlockedClearanceRejected(level, probe, origin);
            JetSetCraft.LOGGER.info("JETSETCRAFT_EDGE_RUNTIME_PASS internalSeamRejected=true exposedLedgeDetected=true clearanceRejected=true");
        } finally {
            clear(level, origin);
        }
    }

    private static void verifyInternalSeamsRejected(ServerLevel level, ServerPlayer probe, BlockPos origin) {
        clear(level, origin);
        for (int x = -2; x <= 2; x++) {
            for (int z = -2; z <= 2; z++) {
                level.setBlockAndUpdate(origin.offset(x, 0, z), Blocks.SMOOTH_STONE.defaultBlockState());
            }
        }
        probe.setPos(origin.getX() + 0.5, origin.getY() + 1.0, origin.getZ() + 0.5);
        Optional<EdgeFinder.Edge> seam = EdgeFinder.findBest(probe, new Vec3(0.0, 0.0, 1.0));
        if (seam.isPresent()) {
            throw new IllegalStateException("Internal full-block floor seam was incorrectly grindable: " + seam.get());
        }
    }

    private static void verifyExposedLedgeDetected(ServerLevel level, ServerPlayer probe, BlockPos origin) {
        clear(level, origin);
        for (int z = -2; z <= 2; z++) {
            level.setBlockAndUpdate(origin.offset(0, 0, z), Blocks.SMOOTH_STONE.defaultBlockState());
        }
        probe.setPos(origin.getX() + 1.14, origin.getY() + 1.0, origin.getZ() + 0.5);
        Optional<EdgeFinder.Edge> edge = EdgeFinder.findBest(probe, new Vec3(0.0, 0.0, 1.0));
        if (edge.isEmpty()) {
            throw new IllegalStateException("Real exposed full-block ledge was not discovered");
        }
        if (Math.abs(edge.get().tangent().z) < 0.90) {
            throw new IllegalStateException("Exposed ledge tangent is not aligned with the real ledge: " + edge.get());
        }
    }

    private static void verifyBlockedClearanceRejected(ServerLevel level, ServerPlayer probe, BlockPos origin) {
        clear(level, origin);
        for (int z = -2; z <= 2; z++) {
            level.setBlockAndUpdate(origin.offset(0, 0, z), Blocks.SMOOTH_STONE.defaultBlockState());
            level.setBlockAndUpdate(origin.offset(0, 2, z), Blocks.SMOOTH_STONE.defaultBlockState());
        }
        probe.setPos(origin.getX() + 1.14, origin.getY() + 1.0, origin.getZ() + 0.5);
        Optional<EdgeFinder.Edge> blocked = EdgeFinder.findBest(probe, new Vec3(0.0, 0.0, 1.0));
        if (blocked.isPresent()) {
            throw new IllegalStateException("Ledge with insufficient rider clearance was incorrectly grindable: " + blocked.get());
        }
    }

    private static void clear(ServerLevel level, BlockPos origin) {
        for (int x = -4; x <= 4; x++) {
            for (int y = 0; y <= 4; y++) {
                for (int z = -4; z <= 4; z++) {
                    level.setBlockAndUpdate(origin.offset(x, y, z), Blocks.AIR.defaultBlockState());
                }
            }
        }
    }

    private EdgeRuntimeVerifier() {}
}
