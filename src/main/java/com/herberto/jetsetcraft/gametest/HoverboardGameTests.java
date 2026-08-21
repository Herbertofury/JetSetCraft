package com.herberto.jetsetcraft.gametest;

import com.herberto.jetsetcraft.JetSetCraft;
import com.herberto.jetsetcraft.data.JetSetData;
import com.herberto.jetsetcraft.item.RideGearItem;
import com.herberto.jetsetcraft.item.RideLoadout;
import com.herberto.jetsetcraft.movement.JetSetMovement;
import com.herberto.jetsetcraft.movement.RideStyle;
import com.herberto.jetsetcraft.registry.ModItems;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestAssertException;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.gametest.GameTestHolder;
import net.minecraftforge.gametest.PrefixGameTestTemplate;
import net.minecraftforge.registries.ForgeRegistries;

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
        ResourceLocation expected = new ResourceLocation(JetSetCraft.MOD_ID, "hoverboard");
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

        // Exercise the actual server-authoritative ride solver in a real ServerLevel rather than a math mock.
        helper.setBlock(new BlockPos(1, 0, 1), Blocks.STONE);
        ServerPlayer player = helper.makeMockServerPlayerInLevel();
        BlockPos feet = helper.absolutePos(new BlockPos(1, 1, 1));
        player.moveTo(feet.getX() + 0.5, feet.getY(), feet.getZ() + 0.5, 0.0F, 0.0F);
        player.setOnGround(true);
        player.setDeltaMovement(Vec3.ZERO);
        data.setInputForward(1.0F);
        // Avoid an unrelated network synchronization packet during the single-tick solver assertion.
        data.setLastSyncTick(player.level().getGameTime());
        JetSetMovement.tickServer(player, data);

        double horizontalSpeed = player.getDeltaMovement().horizontalDistance();
        if (!(horizontalSpeed > 0.0) || !(data.momentum() > 0.0)) {
            throw new GameTestAssertException("Hoverboard did not accelerate through the real ride solver: " + horizontalSpeed);
        }
        if (RideStyle.HOVER.grindMultiplier() <= 1.0 || RideStyle.HOVER.airControl() <= RideStyle.BOARD.airControl()) {
            throw new GameTestAssertException("Hoverboard tuning no longer preserves its grind/air-control identity");
        }

        helper.succeed();
    }

    private HoverboardGameTests() {}
}
