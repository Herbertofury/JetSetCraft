package com.herberto.jetsetcraft.gametest;

import com.herberto.jetsetcraft.JetSetCraft;
import com.herberto.jetsetcraft.crew.CrewHouseData;
import com.herberto.jetsetcraft.crew.CrewHouseManager;
import com.herberto.jetsetcraft.crew.CrewProfile;
import com.herberto.jetsetcraft.liberty.LibertyResearch;
import com.herberto.jetsetcraft.liberty.LibertyShipProgress;
import com.herberto.jetsetcraft.liberty.LibertyStratagemCatalog;
import com.mojang.authlib.GameProfile;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestAssertException;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.common.util.FakePlayerFactory;
import net.minecraftforge.gametest.GameTestHolder;
import net.minecraftforge.gametest.PrefixGameTestTemplate;

import java.util.UUID;

/** Server account fixtures exercise purchases/receipts; operation victory itself is covered by runtime tests. */
@GameTestHolder(JetSetCraft.MOD_ID)
@PrefixGameTestTemplate(false)
public final class LibertyShipProgressGameTests {
    @GameTest(template = "hoverboard_empty", timeoutTicks = 100)
    public static void shipRequisitionsAndFourSlotPurchasesAreVersionedAndExactlyOnce(GameTestHelper helper) {
        ServerPlayer player = owner(helper); CompoundTag original = player.getPersistentData().copy();
        CompoundTag fresh = LibertyShipProgress.snapshot(player, true);
        check(fresh.getList("Callins", Tag.TAG_COMPOUND).size() == LibertyStratagemCatalog.all().size()
                && LibertyShipProgress.equipped(player).equals(LibertyStratagemCatalog.starters())
                && original.equals(player.getPersistentData()), "Reading ship choices bootstrapped NBT or omitted the actual catalogue");
        UUID first = UUID.randomUUID();
        check(LibertyShipProgress.settle(player, first, 3, 1, true) && balance(player) == 33, "First operation did not pay the exact requisition reward");
        CompoundTag paid = saved(player);
        check(LibertyShipProgress.settle(player, first, 3, 1, true)
                && !LibertyShipProgress.settle(player, first, 3, 2, true) && paid.equals(saved(player)),
                "Settlement retry repaid or accepted a changed difficulty");
        check(LibertyShipProgress.settle(player, UUID.randomUUID(), 16, 10, true), "Second distinct operation could not fund hardware");
        String id = "orbital_gatling_barrage"; int cost = LibertyStratagemCatalog.find(id).unlockCost(); long revision = revision(player);
        CompoundTag before = saved(player); int funds = balance(player);
        check(!LibertyShipProgress.purchase(player, "unknown", revision)
                && !LibertyShipProgress.purchase(player, id, revision - 1) && !LibertyShipProgress.equip(player, id, 0, revision)
                && before.equals(saved(player)), "Unknown/stale/locked loadout requests mutated the ship wallet");
        check(LibertyShipProgress.purchase(player, id, revision) && balance(player) == funds - cost
                && !LibertyShipProgress.purchase(player, id, revision(player))
                && LibertyShipProgress.equip(player, id, 1, revision(player))
                && !LibertyShipProgress.equip(player, id, 2, revision(player)), "Purchasing or four-slot uniqueness diverged from displayed costs");
        var reference = LibertyStratagemCatalog.all().stream().filter(entry -> !entry.selectable()).findFirst().orElseThrow();
        check(!LibertyShipProgress.purchase(player, reference.id(), revision(player)), "A historical/shared reference became purchased equipment");
        CompoundTag checkpoint = saved(player); replace(player, checkpoint.copy());
        check(LibertyShipProgress.equipped(player).size() == 4 && LibertyShipProgress.equipped(player).get(1).equals(id)
                && LibertyShipProgress.settle(player, first, 3, 1, true) && checkpoint.equals(saved(player)),
                "Serialized ship selection or an old receipt replay changed the account");
        pass(helper, "liberty_ship_purchase");
    }

    @GameTest(template = "hoverboard_empty", timeoutTicks = 100)
    public static void shipModulesUseExistingResearchAndRealSharedCrewProgress(GameTestHelper helper) {
        ServerPlayer player = owner(helper); UUID research = UUID.randomUUID();
        check(LibertyResearch.begin(player, research) && LibertyResearch.settle(player, research, 1_000, 500, 100, 20)
                && LibertyShipProgress.settle(player, UUID.randomUUID(), 16, 10, true), "Module prerequisites could not be funded");
        CompoundTag unfunded = saved(player);
        check(!LibertyShipProgress.purchase(player, "paint_workshop", revision(player)) && unfunded.equals(saved(player)),
                "Ship hardware bypassed its actual research prerequisite");
        for (String project : new String[]{"pressure_cap", "signal_lacquer", "supply_skates"})
            check(LibertyResearch.purchase(player, project, LibertyResearch.snapshot(player).getLong("Revision")), "Could not purchase prerequisite " + project);
        CompoundTag researchBefore = player.getPersistentData().getCompound(Player.PERSISTED_NBT_TAG).getCompound(LibertyResearch.KEY).copy();
        for (String module : new String[]{"paint_workshop", "targeting_array", "eagle_hangar"})
            check(LibertyShipProgress.purchase(player, module, revision(player)), "Research did not enable hardware " + module);
        check(LibertyShipProgress.bonusResupplies(player) == 1 && LibertyShipProgress.cooldownMultiplier(player) == .9
                && LibertyShipProgress.bonusEagleCharges(player) == 1
                && !LibertyShipProgress.purchase(player, "sample_lab", revision(player)), "A module's accessor or crew prerequisite was incorrect");
        CrewProfile crew = new CrewProfile(player.getUUID(), UUID.randomUUID(), "Ship lab fixture", "jetsetcraft:dead_beat",
                player.serverLevel().dimension().location().toString(), new BlockPos(1_800_000, 100, -1_800_000));
        check(CrewHouseData.get(player.getServer()).add(crew), "Could not create the isolated crew prerequisite account");
        check(CrewHouseManager.bankLiberty(player, UUID.randomUUID(), ResourceLocation.tryParse("jetsetcraft:dead_beat"), 3, true).accepted()
                && crew.xp() == 125 && LibertyShipProgress.purchase(player, "sample_lab", revision(player)),
                "The existing once-only shared crew settlement did not enable its ship laboratory");
        check(LibertyShipProgress.bonusCommonSamples(player, 3, true) == 3
                && LibertyShipProgress.bonusCommonSamples(player, 3, false) == 0
                && LibertyShipProgress.bonusCommonSamples(player, 17, true) == 0
                && LibertyResearch.preparation(player).isEmpty()
                && researchBefore.equals(player.getPersistentData().getCompound(Player.PERSISTED_NBT_TAG).getCompound(LibertyResearch.KEY)),
                "Ship upgrades moved research currency, added a Rush preparation or paid unextracted samples");
        pass(helper, "liberty_ship_modules");
    }

    @GameTest(template = "hoverboard_empty", timeoutTicks = 100)
    public static void shipFutureSchemasAndReceiptCapacityFailClosedWithoutEviction(GameTestHelper helper) {
        ServerPlayer player = owner(helper); UUID first = UUID.randomUUID();
        check(LibertyShipProgress.settle(player, first, 3, 1, true), "Could not create the first ship receipt"); CompoundTag valid = saved(player);
        for (int variant = 0; variant < 5; variant++) {
            CompoundTag invalid = valid.copy();
            switch (variant) {
                case 0 -> invalid.putInt("Schema", 2);
                case 1 -> invalid.putInt("Requisitions", -1);
                case 2 -> invalid.putUUID("Owner", UUID.randomUUID());
                case 3 -> invalid.putString("Equipped", "future loadout");
                default -> invalid.putString("FutureField", "retain me");
            }
            replace(player, invalid);
            check(LibertyShipProgress.snapshot(player, true).getBoolean("Blocked")
                    && !LibertyShipProgress.settle(player, UUID.randomUUID(), 3, 1, true)
                    && !LibertyShipProgress.purchase(player, "orbital_gatling_barrage", 1)
                    && LibertyShipProgress.cooldownMultiplier(player) == 1 && invalid.equals(saved(player)),
                    "An invalid/future ship account was erased, modified or granted hardware");
        }
        CompoundTag full = valid.copy(); ListTag ledger = full.getList("Receipts", Tag.TAG_COMPOUND);
        for (int i = ledger.size(); i < LibertyShipProgress.MAX_RECEIPTS; i++) {
            CompoundTag row = new CompoundTag(); row.putUUID("Run", new UUID(0, i)); row.putString("Hash", "0".repeat(64)); ledger.add(row);
        }
        full.putLong("Revision", LibertyShipProgress.MAX_RECEIPTS); replace(player, full);
        check(!LibertyShipProgress.snapshot(player).getBoolean("Blocked") && !LibertyShipProgress.canReceiveResult(player)
                && LibertyShipProgress.settle(player, first, 3, 1, true)
                && !LibertyShipProgress.settle(player, UUID.randomUUID(), 3, 1, true) && full.equals(saved(player)),
                "A full ship ledger evicted replay protection or lost its existing result acknowledgement");
        CompoundTag nearCap = valid.copy(); nearCap.putInt("Requisitions", LibertyShipProgress.WALLET_LIMIT - 1); replace(player, nearCap);
        check(LibertyShipProgress.settle(player, UUID.randomUUID(), 3, 1, true) && balance(player) == LibertyShipProgress.WALLET_LIMIT,
                "The requisition wallet exceeded its finite bound");
        pass(helper, "liberty_ship_schema_caps");
    }
    private static ServerPlayer owner(GameTestHelper helper) {
        ServerPlayer player = FakePlayerFactory.get(helper.getLevel(), new GameProfile(UUID.randomUUID(), "JSC_ship"));
        player.setHealth(player.getMaxHealth()); return player;
    }
    private static CompoundTag saved(ServerPlayer player) { return player.getPersistentData().getCompound(Player.PERSISTED_NBT_TAG).getCompound(LibertyShipProgress.KEY).copy(); }
    private static void replace(ServerPlayer player, CompoundTag value) {
        CompoundTag saved = player.getPersistentData().getCompound(Player.PERSISTED_NBT_TAG).copy(); saved.put(LibertyShipProgress.KEY, value.copy());
        player.getPersistentData().put(Player.PERSISTED_NBT_TAG, saved);
    }
    private static int balance(ServerPlayer player) { return LibertyShipProgress.snapshot(player).getInt("Requisitions"); }
    private static long revision(ServerPlayer player) { return LibertyShipProgress.snapshot(player).getLong("Revision"); }
    private static void check(boolean ok, String message) { if (!ok) throw new GameTestAssertException(message); }
    private static void pass(GameTestHelper helper, String name) { System.out.println("JETSETCRAFT_LIBERTY_SHIP_PASS " + name); helper.succeed(); }
    private LibertyShipProgressGameTests() { }
}
