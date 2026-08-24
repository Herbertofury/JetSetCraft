package com.herberto.jetsetcraft.gametest;

import com.herberto.jetsetcraft.JetSetCraft;
import com.herberto.jetsetcraft.data.JetSetData;
import com.herberto.jetsetcraft.item.RideGearItem;
import com.herberto.jetsetcraft.item.RideLoadout;
import com.herberto.jetsetcraft.movement.JetSetMovement;
import com.herberto.jetsetcraft.movement.RideStyle;
import com.herberto.jetsetcraft.movement.VanillaWorldPhysics;
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

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.nio.charset.StandardCharsets;
import java.util.UUID;

/** Real Forge/Minecraft acceptance for the recovered hoverboard ride style. */
@GameTestHolder(JetSetCraft.MOD_ID)
@PrefixGameTestTemplate(false)
public final class HoverboardGameTests {
    @GameTest(template = "hoverboard_empty", timeoutTicks = 80)
    public static void hoverboardUsesPersistentUniversalRideSolver(GameTestHelper helper) {
        if (!(ModItems.HOVERBOARD.get() instanceof RideGearItem gear)) {
            throw new GameTestAssertException("Hoverboard registry entry is not RideGearItem");
        }
        ResourceLocation registryName = ForgeRegistries.ITEMS.getKey(gear);
        ResourceLocation expected = ResourceLocation.fromNamespaceAndPath(JetSetCraft.MOD_ID, "hoverboard");
        if (!expected.equals(registryName)) {
            throw new GameTestAssertException("Unexpected hoverboard registry key: " + registryName);
        }
        if (gear.style() != RideStyle.HOVER || RideStyle.byId(5) != RideStyle.HOVER) {
            throw new GameTestAssertException("Hoverboard style/serialized ID is not stable");
        }

        // Prove the new style survives JetSetCraft's real player-data NBT contract and dedicated loadout slot.
        JetSetData saved = new JetSetData();
        saved.setRideGear(new ItemStack(gear));
        saved.setStyle(RideStyle.HOVER);
        saved.setActive(true);
        JetSetData data = new JetSetData();
        data.load(saved.save());
        if (!data.active() || data.style() != RideStyle.HOVER || RideLoadout.equippedStyle(data) != RideStyle.HOVER) {
            throw new GameTestAssertException("Hoverboard did not survive real JetSetCraft persistence/loadout state");
        }

        // Exercise JetSetCraft's actual production ground-motion solver against the real GameTest ServerLevel and
        // Forge's purpose-built server FakePlayer. Unlike GameTest's login-oriented mock player, FakePlayer does not
        // require a synthetic Netty channel, so this remains a real Minecraft/Forge movement test without mixing in
        // an unrelated client transport failure.
        helper.setBlock(new BlockPos(1, 0, 1), Blocks.STONE);
        UUID uuid = UUID.nameUUIDFromBytes((JetSetCraft.MOD_ID + ":gametest:hoverboard")
                .getBytes(StandardCharsets.UTF_8));
        ServerPlayer player = FakePlayerFactory.get(helper.getLevel(), new GameProfile(uuid, "JSC_hoverboard"));
        BlockPos feet = helper.absolutePos(new BlockPos(1, 1, 1));
        player.moveTo(feet.getX() + 0.5, feet.getY(), feet.getZ() + 0.5, 0.0F, 0.0F);
        player.setOnGround(true);
        player.setDeltaMovement(Vec3.ZERO);
        data.setInputForward(1.0F);
        data.setMomentum(0.0);
        VanillaWorldPhysics.Surface surface = VanillaWorldPhysics.ground(player);
        invokeProductionGroundSolver(player, data, surface);

        double horizontalSpeed = player.getDeltaMovement().horizontalDistance();
        if (!(horizontalSpeed > 0.0) || !(data.momentum() > 0.0)) {
            throw new GameTestAssertException("Hoverboard did not accelerate through the real ride solver: " + horizontalSpeed);
        }
        if (RideStyle.HOVER.grindMultiplier() <= 1.0 || RideStyle.HOVER.airControl() <= RideStyle.BOARD.airControl()) {
            throw new GameTestAssertException("Hoverboard tuning no longer preserves its grind/air-control identity");
        }

        System.out.println("JETSETCRAFT_GAMETEST_PASS hoverboard");
        helper.succeed();
    }

    @GameTest(template = "hoverboard_empty", timeoutTicks = 80)
    public static void rideControlsStopCleanlyAndYieldToSwimming(GameTestHelper helper) {
        helper.setBlock(new BlockPos(1, 0, 1), Blocks.STONE);
        UUID uuid = UUID.nameUUIDFromBytes((JetSetCraft.MOD_ID + ":gametest:ride_controls")
                .getBytes(StandardCharsets.UTF_8));
        ServerPlayer player = FakePlayerFactory.get(helper.getLevel(), new GameProfile(uuid, "JSC_ride_controls"));
        BlockPos feet = helper.absolutePos(new BlockPos(1, 1, 1));
        player.moveTo(feet.getX() + 0.5, feet.getY(), feet.getZ() + 0.5, 0.0F, 0.0F);
        player.setOnGround(true);

        JetSetData data = new JetSetData();
        data.setRideGear(new ItemStack(ModItems.HOVERBOARD.get()));
        data.setStyle(RideStyle.HOVER);
        data.setActive(true);
        data.setMomentum(0.55);
        data.setInputForward(0.0F);
        data.setInputStrafe(0.0F);
        player.setDeltaMovement(Vec3.ZERO);
        invokeProductionGroundSolver(player, data, VanillaWorldPhysics.ground(player));
        if (player.getDeltaMovement().horizontalDistanceSqr() != 0.0 || data.momentum() != 0.0) {
            throw new GameTestAssertException("Neutral controls relaunched stale momentum toward the camera");
        }

        // A deliberate input still accelerates through the authored ride solver.
        data.setInputForward(1.0F);
        invokeProductionGroundSolver(player, data, VanillaWorldPhysics.ground(player));
        if (player.getDeltaMovement().horizontalDistanceSqr() <= 0.0 || data.momentum() <= 0.0) {
            throw new GameTestAssertException("Directional ride input no longer accelerates after a complete stop");
        }

        // Swimming is an explicit authority boundary: not one component of vanilla fluid velocity may be replaced.
        Vec3 vanillaSwimVelocity = new Vec3(0.13, 0.08, -0.07);
        player.setOnGround(false);
        player.setSwimming(true);
        player.setDeltaMovement(vanillaSwimVelocity);
        data.setMomentum(0.70);
        data.setInputMask(InputFlags.BOOST);
        data.setLastSyncTick(helper.getLevel().getGameTime());
        JetSetMovement.tickServer(player, data);
        if (!player.getDeltaMovement().equals(vanillaSwimVelocity) || data.momentum() != 0.0
                || data.boosting() || data.grinding() || data.wallRiding()) {
            throw new GameTestAssertException("Ride solver did not yield complete movement authority while swimming");
        }

        System.out.println("JETSETCRAFT_GAMETEST_PASS ride_controls");
        helper.succeed();
    }

    private static void invokeProductionGroundSolver(ServerPlayer player, JetSetData data,
                                                     VanillaWorldPhysics.Surface surface) {
        try {
            Class<?> rideMotion = Class.forName("com.herberto.jetsetcraft.movement.RideMotion");
            Method method = rideMotion.getDeclaredMethod("applyGroundMovement", ServerPlayer.class,
                    JetSetData.class, VanillaWorldPhysics.Surface.class);
            method.setAccessible(true);
            method.invoke(null, player, data, surface);
        } catch (InvocationTargetException error) {
            Throwable cause = error.getCause() == null ? error : error.getCause();
            throw new GameTestAssertException("Production RideMotion solver threw: " + cause);
        } catch (ReflectiveOperationException error) {
            throw new GameTestAssertException("Could not invoke production RideMotion solver: " + error);
        }
    }

    private HoverboardGameTests() {}
}
