package com.herberto.jetsetcraft.gametest;

import com.herberto.jetsetcraft.JetSetCraft;
import com.herberto.jetsetcraft.data.JetSetData;
import com.herberto.jetsetcraft.item.RideGearItem;
import com.herberto.jetsetcraft.item.RideLoadout;
import com.herberto.jetsetcraft.movement.DanceCatalog;
import com.herberto.jetsetcraft.movement.JetSetMovement;
import com.herberto.jetsetcraft.movement.RideStyle;
import com.herberto.jetsetcraft.movement.TrickCatalog;
import com.herberto.jetsetcraft.network.InputFlags;
import com.herberto.jetsetcraft.registry.ModItems;
import com.mojang.authlib.GameProfile;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestAssertException;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.common.util.FakePlayerFactory;
import net.minecraftforge.gametest.GameTestHolder;
import net.minecraftforge.gametest.PrefixGameTestTemplate;
import net.minecraftforge.registries.ForgeRegistries;

import java.nio.charset.StandardCharsets;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

/** Real Forge/Minecraft acceptance for scooter and no-gear breakdance Style Flow. */
@GameTestHolder(JetSetCraft.MOD_ID)
@PrefixGameTestTemplate(false)
public final class StyleFlowGameTests {
    @GameTest(template = "hoverboard_empty", timeoutTicks = 80)
    public static void scooterUsesUniversalPersistentRideSolver(GameTestHelper helper) {
        if (!(ModItems.SCOOTER.get() instanceof RideGearItem gear)) {
            throw new GameTestAssertException("Scooter registry entry is not RideGearItem");
        }
        ResourceLocation actual = ForgeRegistries.ITEMS.getKey(gear);
        ResourceLocation expected = new ResourceLocation(JetSetCraft.MOD_ID, "scooter");
        if (!expected.equals(actual) || gear.style() != RideStyle.SCOOTER || RideStyle.byId(6) != RideStyle.SCOOTER) {
            throw new GameTestAssertException("Scooter registry/style identity is unstable: " + actual);
        }

        JetSetData saved = new JetSetData();
        saved.setRideGear(new ItemStack(gear));
        saved.setStyle(RideStyle.SCOOTER);
        saved.setActive(true);
        JetSetData data = new JetSetData();
        data.load(saved.save());
        if (!data.active() || RideLoadout.equippedStyle(data) != RideStyle.SCOOTER) {
            throw new GameTestAssertException("Scooter did not survive the persistent loadout contract");
        }

        helper.setBlock(new BlockPos(1, 0, 1), Blocks.STONE);
        ServerPlayer player = fakePlayer(helper, "scooter");
        BlockPos feet = helper.absolutePos(new BlockPos(1, 1, 1));
        player.moveTo(feet.getX() + 0.5, feet.getY(), feet.getZ() + 0.5, 0.0F, 0.0F);
        player.setOnGround(true);
        player.setDeltaMovement(Vec3.ZERO);
        data.setInputForward(1.0F);
        JetSetMovement.tickServer(player, data);
        if (!(player.getDeltaMovement().horizontalDistance() > 0.0) || !(data.momentum() > 0.0)) {
            throw new GameTestAssertException("Scooter did not accelerate through JetSetMovement");
        }
        System.out.println("JETSETCRAFT_GAMETEST_PASS scooter");
        helper.succeed();
    }

    @GameTest(template = "hoverboard_empty", timeoutTicks = 80)
    public static void breakdanceStartsWithoutRideGearAndBuildsStyle(GameTestHelper helper) {
        helper.setBlock(new BlockPos(1, 0, 1), Blocks.SMOOTH_STONE);
        ServerPlayer player = fakePlayer(helper, "dance_flow");
        prepareGroundedPlayer(helper, player, new BlockPos(1, 1, 1));

        JetSetData data = new JetSetData();
        data.setInputMask(InputFlags.DANCE);
        JetSetMovement.tickServer(player, data);
        if (!data.dancing() || data.active() || data.danceTicks() <= 0) {
            throw new GameTestAssertException("No-gear server-authoritative dance did not start");
        }
        if (data.danceMoveId() < 0 || data.danceMoveId() >= DanceCatalog.MOVE_COUNT) {
            throw new GameTestAssertException("Dance did not select a valid named Style Flow move");
        }

        // Scoring is awarded when a complete authored phrase finishes, not when the key is pressed.
        for (int tick = 0; tick < 48 && data.comboScore() <= 0; tick++) {
            player.setOnGround(true);
            player.setDeltaMovement(Vec3.ZERO);
            JetSetMovement.tickServer(player, data);
        }
        if (!data.dancing() || data.comboScore() <= 0 || data.flow() <= 0.0f || data.danceChain() <= 0) {
            throw new GameTestAssertException("Completed dance move did not award Style Flow score/flow");
        }
        System.out.println("JETSETCRAFT_GAMETEST_PASS dance_flow");
        helper.succeed();
    }

    @GameTest(template = "hoverboard_empty", timeoutTicks = 60)
    public static void danceYieldsToCombatImmediately(GameTestHelper helper) {
        helper.setBlock(new BlockPos(1, 0, 1), Blocks.SMOOTH_STONE);
        ServerPlayer player = fakePlayer(helper, "dance_combat");
        prepareGroundedPlayer(helper, player, new BlockPos(1, 1, 1));

        JetSetData data = new JetSetData();
        data.setInputMask(InputFlags.DANCE);
        JetSetMovement.tickServer(player, data);
        if (!data.dancing()) throw new GameTestAssertException("Dance setup failed before combat-sovereignty check");

        player.swinging = true;
        JetSetMovement.tickServer(player, data);
        player.swinging = false;
        if (data.dancing()) {
            throw new GameTestAssertException("Dance did not yield immediately when a weapon/action swing took authority");
        }
        System.out.println("JETSETCRAFT_GAMETEST_PASS combat_sovereignty");
        helper.succeed();
    }

    @GameTest(template = "hoverboard_empty", timeoutTicks = 40)
    public static void styleFlowCatalogsRemainStableAndComplete(GameTestHelper helper) {
        if (DanceCatalog.MOVE_COUNT != 28 || TrickCatalog.TRICK_COUNT != 24) {
            throw new GameTestAssertException("Style Flow catalog cardinality changed unexpectedly");
        }

        Set<String> danceNames = new HashSet<>();
        for (int id = 0; id < DanceCatalog.MOVE_COUNT; id++) {
            DanceCatalog.Move move = DanceCatalog.byId(id);
            if (move.id() != id || move.animationIndex() != id || !danceNames.add(DanceCatalog.name(id))) {
                throw new GameTestAssertException("Dance catalog ID/name/animation is not one-to-one at " + id);
            }
        }

        Set<String> trickNames = new HashSet<>();
        for (int id = 0; id < TrickCatalog.TRICK_COUNT; id++) {
            TrickCatalog.Trick trick = TrickCatalog.byId(id);
            String key = TrickCatalog.contextName(id) + ":" + TrickCatalog.name(id, RideStyle.INLINE);
            if (trick.id() != id || trick.animationIndex() < 0 || trick.animationIndex() >= 8
                    || !trickNames.add(key)) {
                throw new GameTestAssertException("Trick catalog identity is invalid at " + id + " (" + key + ")");
            }
        }
        System.out.println("JETSETCRAFT_GAMETEST_PASS catalogs");
        helper.succeed();
    }

    private static ServerPlayer fakePlayer(GameTestHelper helper, String identity) {
        UUID uuid = UUID.nameUUIDFromBytes((JetSetCraft.MOD_ID + ":gametest:" + identity)
                .getBytes(StandardCharsets.UTF_8));
        return FakePlayerFactory.get(helper.getLevel(), new GameProfile(uuid, "JSC_" + identity));
    }

    private static void prepareGroundedPlayer(GameTestHelper helper, ServerPlayer player, BlockPos relativeFeet) {
        BlockPos feet = helper.absolutePos(relativeFeet);
        player.moveTo(feet.getX() + 0.5, feet.getY(), feet.getZ() + 0.5, 0.0F, 0.0F);
        player.setOnGround(true);
        player.setDeltaMovement(Vec3.ZERO);
        player.stopUsingItem();
        player.swinging = false;
    }

    private StyleFlowGameTests() {}
}
