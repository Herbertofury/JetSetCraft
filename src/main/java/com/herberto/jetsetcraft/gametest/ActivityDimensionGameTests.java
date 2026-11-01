package com.herberto.jetsetcraft.gametest;

import com.herberto.jetsetcraft.JetSetCraft;
import com.herberto.jetsetcraft.activity.ActivityManager;
import com.herberto.jetsetcraft.data.JetSetData;
import com.herberto.jetsetcraft.data.JetSetDataProvider;
import com.herberto.jetsetcraft.movement.RideStyle;
import com.herberto.jetsetcraft.registry.ModItems;
import com.herberto.jetsetcraft.rush.RivalRushManager;
import com.herberto.jetsetcraft.rush.RushReturnState;
import com.herberto.jetsetcraft.rush.RushWorld;
import com.mojang.authlib.GameProfile;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestAssertException;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.common.util.FakePlayerFactory;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.gametest.GameTestHolder;
import net.minecraftforge.gametest.PrefixGameTestTemplate;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/** Exercises real provider dimension entry/return without serializing, rewriting or claiming to restore foreign capabilities. */
@GameTestHolder(JetSetCraft.MOD_ID)
@PrefixGameTestTemplate(false)
public final class ActivityDimensionGameTests {
    @GameTest(template = "hoverboard_empty", timeoutTicks = 300)
    public static void installedAdventureDimensionsRemainExactReturnOrigins(GameTestHelper helper) {
        String lane = System.getProperty("jetsetcraft.providerSelection", "");
        boolean all = lane.equals("adventure_all");
        verifyProvider("aether", "1.20.1-1.5.2-neoforge", all || lane.equals("aether"));
        verifyProvider("aoa3", "3.7.1", all || lane.equals("aoa"));
        verifyProvider("irons_spellbooks", "1.20.1-3.4.0.2", all || lane.equals("irons_spells"));
        verifyProvider("cataclysm", "3.31", all);
        List<String> dimensions = new ArrayList<>();
        if (ModList.get().isLoaded("aether")) dimensions.add("aether:the_aether");
        if (ModList.get().isLoaded("aoa3")) dimensions.add("aoa3:precasia");
        for (String dimension : dimensions) proveReturn(helper, dimension);
        System.out.println("JETSETCRAFT_GAMETEST_PASS activity_dimension_return dimensions="
                + (dimensions.isEmpty() ? "absent" : String.join(",", dimensions)));
        helper.succeed();
    }

    private static void verifyProvider(String mod, String expected, boolean required) {
        boolean present = ModList.get().isLoaded(mod);
        check(!required || present, "Selected provider lane did not load " + mod);
        if (!present) return;
        String actual = ModList.get().getModContainerById(mod).orElseThrow().getModInfo().getVersion().toString();
        check(actual.equals(expected), "Native provider proof expected " + mod + " " + expected + ", found " + actual);
        System.out.println("JETSETCRAFT_ADVENTURE_PROVIDER " + mod + " version=" + actual);
    }

    private static void proveReturn(GameTestHelper helper, String dimension) {
        ServerLevel source = helper.getLevel().getServer().getLevel(ResourceKey.create(Registries.DIMENSION,
                ResourceLocation.tryParse(dimension)));
        check(source != null, "Installed provider did not register its native dimension " + dimension);
        // This is a small test fixture, above native terrain and away from authored boss structures.
        BlockPos platform = new BlockPos(48, Math.min(source.getMaxBuildHeight() - 8, 240), 48);
        for (int x = -1; x <= 1; x++) for (int z = -1; z <= 1; z++)
            source.setBlockAndUpdate(platform.offset(x, -1, z), Blocks.STONE.defaultBlockState());
        ServerPlayer owner = FakePlayerFactory.get(helper.getLevel(), new GameProfile(UUID.randomUUID(), "JSC_realm_return"));
        owner.setGameMode(GameType.SURVIVAL); owner.setHealth(owner.getMaxHealth()); owner.getInventory().clearContent();
        owner.teleportTo(source, platform.getX() + .5, platform.getY(), platform.getZ() + .5, 17, 4);
        check(owner.serverLevel() == source, "Actual player teleport did not enter the selected provider dimension");
        owner.getInventory().setItem(0, new ItemStack(Items.DIAMOND, 7));
        owner.getInventory().getItem(0).getOrCreateTag().putString("ReturnProof", dimension);
        owner.getInventory().setItem(8, new ItemStack(Items.BREAD, 5)); owner.getInventory().selected = 8;
        ItemStack board = new ItemStack(ModItems.HOVERBOARD.get()); board.getOrCreateTag().putString("Origin", dimension);
        JetSetData ride = ride(owner); ride.load(new CompoundTag()); ride.setRideGear(board); ride.setStyle(RideStyle.HOVER);
        ride.setActive(true); ride.setBoost(37); ride.setFlow(12); ride.setComboScore(91);
        owner.experienceLevel = 23; owner.totalExperience = 587; owner.experienceProgress = .625F;
        owner.getPersistentData().putString("jetsetcraft:realm_return_proof", dimension);
        ListTag inventory = owner.getInventory().save(new ListTag()); CompoundTag savedRide = ride.save();
        Vec3 position = owner.position(); int forced = source.getForcedChunks().size();
        try {
            check(RivalRushManager.startCircuit(owner, ResourceLocation.tryParse("jetsetcraft:dead_beat"),
                    ResourceLocation.tryParse("minecraft:zombie"), false, null), "Arena circuit could not start from " + dimension);
            check(RushWorld.isRush(owner.level()) && RivalRushManager.active(owner) && !ActivityManager.active(owner)
                            && RushReturnState.journal(owner).getString("Dimension").equals(dimension),
                    "Circuit entry lost the provider's exact return dimension or kept conflicting activity ownership");
            check(RivalRushManager.leave(owner, "Native realm return proof"), "Circuit return could not restore " + dimension);
            check(owner.serverLevel() == source && owner.position().distanceToSqr(position) < .0001
                            && owner.getYRot() == 17 && owner.getXRot() == 4 && !RushReturnState.hasJournal(owner)
                            && !RivalRushManager.active(owner), "Return used the wrong dimension, position, orientation or retained a journal");
            check(inventory.equals(owner.getInventory().save(new ListTag())) && savedRide.equals(ride(owner).save())
                            && owner.experienceLevel == 23 && owner.totalExperience == 587 && owner.experienceProgress == .625F
                            && owner.getPersistentData().getString("jetsetcraft:realm_return_proof").equals(dimension),
                    "Provider-origin return changed vanilla inventory/XP, physical ride state or unrelated persistent data");
            check(source.getForcedChunks().size() == forced && source.getBlockState(platform.below()).is(Blocks.STONE),
                    "Arena entry/return acquired provider-world forced chunks or changed the source platform");
        } finally {
            if (RivalRushManager.active(owner) || RushReturnState.hasJournal(owner))
                check(RivalRushManager.leave(owner, "Native realm proof cleanup"), "Could not restore provider-origin test player");
        }
    }

    private static JetSetData ride(ServerPlayer player) {
        return player.getCapability(JetSetDataProvider.CAPABILITY).resolve()
                .orElseThrow(() -> new GameTestAssertException("The real Forge player's physical ride capability is unavailable"));
    }
    private static void check(boolean condition, String message) { if (!condition) throw new GameTestAssertException(message); }
    private ActivityDimensionGameTests() {}
}
