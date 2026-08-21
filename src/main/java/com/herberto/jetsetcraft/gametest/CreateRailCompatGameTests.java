package com.herberto.jetsetcraft.gametest;

import com.herberto.jetsetcraft.JetSetCraft;
import com.herberto.jetsetcraft.compat.create.CreateRailProvider;
import com.herberto.jetsetcraft.movement.GrindFinder;
import com.herberto.jetsetcraft.movement.GrindKind;
import com.herberto.jetsetcraft.movement.GrindTarget;
import com.simibubi.create.AllBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestAssertException;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.common.util.FakePlayerFactory;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.gametest.GameTestHolder;
import net.minecraftforge.gametest.PrefixGameTestTemplate;

import java.util.Optional;

/**
 * Verification-only acceptance against the real Create 6.0.8 runtime.
 * Production JetSetCraft keeps Create optional; this test branch deliberately loads Create so the adapter has to
 * survive real mod initialization and resolve a real TrackBlock through both the provider and universal finder.
 */
@GameTestHolder(JetSetCraft.MOD_ID)
@PrefixGameTestTemplate(false)
public final class CreateRailCompatGameTests {
    @GameTest(template = "hoverboard_empty", timeoutTicks = 120)
    public static void create608TrackUsesPhysicalRailBarGeometry(GameTestHelper helper) {
        if (!ModList.get().isLoaded("create")) {
            throw new GameTestAssertException("Create 6.0.8 is not loaded in the compatibility runtime");
        }

        BlockPos trackRelative = new BlockPos(1, 1, 1);
        helper.setBlock(trackRelative.below(), Blocks.STONE);
        helper.setBlock(trackRelative, AllBlocks.TRACK.getDefaultState());

        BlockPos track = helper.absolutePos(trackRelative);
        ServerPlayer player = FakePlayerFactory.getMinecraft(helper.getLevel());

        // Create's default TrackShape.ZO runs north/south. The physical rail bars are roughly +/-0.46 blocks
        // from the block center. Stand above the east bar so a centerline implementation will fail this test.
        double expectedRailX = track.getX() + 0.96;
        player.moveTo(expectedRailX, track.getY() + 0.11, track.getZ() + 0.50, 0.0F, 0.0F);
        player.setOnGround(true);
        player.setDeltaMovement(new Vec3(0.0, 0.0, 0.34));
        Vec3 preferred = new Vec3(0.0, 0.0, 1.0);

        CreateRailProvider provider = new CreateRailProvider();
        GrindTarget direct = provider.findBest(player, preferred, 0.70, 0.82)
                .orElseThrow(() -> new GameTestAssertException("Native CreateRailProvider did not resolve a real Create track"));
        assertCreateRailBar(track, expectedRailX, direct, "direct provider");

        Optional<GrindTarget> universalResult = GrindFinder.findBest(player, preferred, GrindKind.CREATE_TRACK);
        GrindTarget universal = universalResult
                .orElseThrow(() -> new GameTestAssertException("Universal GrindFinder did not resolve the loaded Create track"));
        assertCreateRailBar(track, expectedRailX, universal, "universal finder");

        System.out.println("JETSETCRAFT_CREATE_RUNTIME_PASS createLoaded=true directKind=" + direct.kind()
                + " universalKind=" + universal.kind() + " railX=" + universal.point().x);
        helper.succeed();
    }

    private static void assertCreateRailBar(BlockPos track, double expectedRailX, GrindTarget target, String stage) {
        if (target.kind() != GrindKind.CREATE_TRACK) {
            throw new GameTestAssertException(stage + " selected " + target.kind() + " instead of CREATE_TRACK");
        }
        if (Math.abs(target.tangent().normalize().z) < 0.90) {
            throw new GameTestAssertException(stage + " returned the wrong default Z-axis tangent: " + target.tangent());
        }
        double centerX = track.getX() + 0.50;
        if (Math.abs(target.point().x - centerX) < 0.30) {
            throw new GameTestAssertException(stage + " snapped to Create's invisible centerline instead of a physical rail bar: "
                    + target.point());
        }
        if (Math.abs(target.point().x - expectedRailX) > 0.12) {
            throw new GameTestAssertException(stage + " missed the physical Create rail bar: expected x~"
                    + expectedRailX + " got " + target.point().x);
        }
    }

    private CreateRailCompatGameTests() {}
}
