package com.herberto.jetsetcraft.gametest;

import com.herberto.jetsetcraft.JetSetCraft;
import com.herberto.jetsetcraft.crew.CrewHouseData;
import com.herberto.jetsetcraft.crew.CrewHouseManager;
import com.herberto.jetsetcraft.liberty.LibertyResearch;
import com.herberto.jetsetcraft.rush.RivalRushManager;
import com.herberto.jetsetcraft.rush.RushReturnState;
import com.herberto.jetsetcraft.rush.RushRun;
import com.mojang.authlib.GameProfile;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestAssertException;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.GameType;
import net.minecraftforge.common.util.FakePlayerFactory;
import net.minecraftforge.gametest.GameTestHolder;
import net.minecraftforge.gametest.PrefixGameTestTemplate;

import java.util.UUID;

/** Actual player NBT, pending intents, shared reward account and production Rush entry. */
@GameTestHolder(JetSetCraft.MOD_ID)
@PrefixGameTestTemplate(false)
public final class LibertyResearchGameTests {
    private static final ResourceLocation GANG = ResourceLocation.tryParse("jetsetcraft:dead_beat");
    private static final ResourceLocation ZOMBIE = ResourceLocation.tryParse("minecraft:zombie");

    @GameTest(template = "hoverboard_empty", timeoutTicks = 100)
    public static void preparedMissionSettlementSurvivesReloadAndCannotReplay(GameTestHelper helper) {
        ServerPlayer player = owner(helper);
        UUID first = UUID.randomUUID(), second = UUID.randomUUID();
        CompoundTag untouched = player.getPersistentData().copy();
        check(!LibertyResearch.settle(player, first, 20, 5, 2, 1)
                        && untouched.equals(player.getPersistentData()),
                "An unarmed UUID received research currency or wrote an account");
        check(LibertyResearch.begin(player, first) && !LibertyResearch.begin(player, second)
                        && LibertyResearch.pendingMission(player).orElseThrow().equals(first),
                "The entry checkpoint permitted nested mission nonces");
        CompoundTag active = saved(player);
        check(!LibertyResearch.stageSettlement(player, first, -1, 2, 0, 0)
                        && !LibertyResearch.stageSettlement(player, first, 10_001, 2, 0, 0)
                        && active.equals(saved(player)),
                "Out-of-range settlement changed the pending transaction");
        check(LibertyResearch.stageSettlement(player, first, 20, 5, 2, 1)
                        && LibertyResearch.stageSettlement(player, first, 20, 5, 2, 1)
                        && !LibertyResearch.stageSettlement(player, first, 21, 5, 2, 1)
                        && LibertyResearch.staged(player),
                "A durable outcome was not idempotent or its earned amount could be rewritten");
        CompoundTag checkpoint = player.getPersistentData().getCompound(Player.PERSISTED_NBT_TAG).copy();
        player.getPersistentData().remove(Player.PERSISTED_NBT_TAG);
        player.getPersistentData().put(Player.PERSISTED_NBT_TAG, checkpoint.copy());
        check(LibertyResearch.settlePending(player) && balance(player, "Credits") == 20 && balance(player, "Common") == 5
                        && balance(player, "Rare") == 2 && balance(player, "Super") == 1
                        && LibertyResearch.lastSettled(player, first) && !LibertyResearch.lastSettled(player, second)
                        && !LibertyResearch.settlePending(player) && !LibertyResearch.settle(player, first, 20, 5, 2, 1)
                        && !LibertyResearch.begin(player, first),
                "Reload/retry duplicated, dropped or reopened the prepared payout");
        check(LibertyResearch.begin(player, second) && !LibertyResearch.settle(player, first, 20, 5, 2, 1)
                        && LibertyResearch.abandonPending(player) && balance(player, "Credits") == 20
                        && LibertyResearch.pendingMission(player).isEmpty(),
                "An old mission settled against a new nonce, or an unprepared abort invented a payout");
        checkVanilla(player); pass(helper, "liberty_research_settlement");
    }

    @GameTest(template = "hoverboard_empty", timeoutTicks = 100)
    public static void researchPurchasesAreAtomicVersionedAndPowerTheAdvertisedEffects(GameTestHelper helper) {
        ServerPlayer player = owner(helper);
        check(!LibertyResearch.purchase(player, "pressure_cap", 0), "Unfunded research was purchased");
        fund(player, 500, 200, 100, 20);
        long originalRevision = revision(player);
        CompoundTag before = saved(player);
        check(!LibertyResearch.purchase(player, "unknown", originalRevision)
                        && !LibertyResearch.purchase(player, "pressure_cap", originalRevision - 1)
                        && !LibertyResearch.selectPreparation(player, "magnetic_pigment", originalRevision)
                        && before.equals(saved(player)),
                "Unknown, stale or locked choices mutated research state");
        check(LibertyResearch.purchase(player, "pressure_cap", originalRevision)
                        && balance(player, "Credits") == 480 && balance(player, "Common") == 192,
                "The first real purchase did not consume the displayed exact costs");
        CompoundTag purchased = saved(player);
        check(!LibertyResearch.purchase(player, "pressure_cap", revision(player)) && purchased.equals(saved(player)),
                "A duplicate project was charged twice");
        for (int i = 1; i <= 3; i++) check(LibertyResearch.shouldConsumePaint(player), "Pressure Saver fired before its fourth successful paint");
        CompoundTag paintCheckpoint = saved(player).copy();
        replace(player, paintCheckpoint);
        check(!LibertyResearch.shouldConsumePaint(player) && LibertyResearch.shouldConsumePaint(player),
                "Pressure Saver lost its saved cadence or stopped consuming ordinary paint");
        check(LibertyResearch.purchase(player, "signal_lacquer", revision(player))
                        && LibertyResearch.muralPanels(player) == 2
                        && LibertyResearch.purchase(player, "supply_skates", revision(player))
                        && LibertyResearch.bonusResupplies(player) == 1,
                "The selected practical research effects did not match their project descriptions");
        check(!LibertyResearch.selectPreparation(player, "pressure_cap", revision(player)),
                "A passive project entered the preparation slot");
        CompoundTag view = LibertyResearch.snapshot(player);
        check(view.getList("Projects", Tag.TAG_COMPOUND).size() == 6 && view.getBoolean("CanManage")
                        && !view.getBoolean("Blocked") && view.getString("SelectedPreparation").equals(LibertyResearch.NONE),
                "Research presentation omitted the real project catalogue or account state");
        checkVanilla(player); pass(helper, "liberty_research_purchase");
    }

    @GameTest(template = "hoverboard_empty", timeoutTicks = 100)
    public static void expeditionResearchUsesExactlyOneExistingRushPreparationSlot(GameTestHelper helper) {
        ServerPlayer player = owner(helper); fund(player, 100, 40, 10, 2);
        check(LibertyResearch.purchase(player, "magnetic_pigment", revision(player))
                        && LibertyResearch.selectPreparation(player, "magnetic_pigment", revision(player)),
                "Earned Magnetic Pigment could not be selected");
        UUID mission = UUID.randomUUID();
        var reward = CrewHouseManager.bankLiberty(player, mission, GANG, 3, true);
        CrewHouseData crew = CrewHouseData.get(player.getServer());
        check(reward.accepted() && reward.supplies() == 20 && reward.crewXp() == 125 && reward.memberXp() == 55
                        && reward.reputation() == 14 && crew.pendingXp(player.getUUID()) == 125
                        && crew.recentRewards(player.getUUID()).get(0).source().equals("liberty_grind")
                        && !CrewHouseManager.bankLiberty(player, mission, GANG, 3, true).accepted()
                        && !CrewHouseManager.bankLiberty(player, UUID.randomUUID(), GANG, 0, true).accepted(),
                "Liberty did not use the genuine shared crew account or duplicated an objective award");
        check(CrewHouseManager.rushPreparation(player).orElse(null) == RushRun.Upgrade.PICKUP,
                "The crew preparation path omitted the selected research");
        ListTag inventory = player.getInventory().save(new ListTag());
        try {
            check(RivalRushManager.start(player, GANG, ZOMBIE, false), "Production Rival Rush rejected a researched preparation");
            RushRun run = RivalRushManager.run(player); int ranks = 0;
            for (RushRun.Upgrade upgrade : RushRun.Upgrade.values()) ranks += run.rank(upgrade);
            check(ranks == 1 && run.rank(RushRun.Upgrade.PICKUP) == 1 && !run.grantStartingPerk(RushRun.Upgrade.FORCE)
                            && !LibertyResearch.selectPreparation(player, LibertyResearch.NONE, revision(player))
                            && !LibertyResearch.purchase(player, "signal_lacquer", revision(player)),
                    "Research stacked another rank or changed while a temporary Rush was active");
            check(RivalRushManager.leave(player, "Research integration return"), "Research prevented the production safe return");
        } finally {
            if (RivalRushManager.active(player) || RushReturnState.hasJournal(player))
                check(RivalRushManager.leave(player, "Research fixture cleanup"), "Could not restore the research fixture player");
        }
        CrewHouseManager.handleAction(player, "style", "BATTERY", 0, null, null, crew.styleRevision(player.getUUID()));
        check(CrewHouseManager.rushPreparation(player).orElse(null) == RushRun.Upgrade.BATTERY
                        && LibertyResearch.preparation(player).isEmpty()
                        && inventory.equals(player.getInventory().save(new ListTag())),
                "Explicitly selecting an earned crew style retained a hidden research override or changed normal items");
        checkVanilla(player); pass(helper, "liberty_research_rush");
    }

    @GameTest(template = "hoverboard_empty", timeoutTicks = 100)
    public static void malformedAndFutureResearchIsRetainedWithoutSpendingOrRepair(GameTestHelper helper) {
        ServerPlayer player = owner(helper); fund(player, 60, 30, 8, 2);
        CompoundTag valid = saved(player);
        for (int variant = 0; variant < 4; variant++) {
            CompoundTag invalid = valid.copy();
            switch (variant) {
                case 0 -> invalid.putInt("Schema", 2);
                case 1 -> invalid.putInt("Credits", -1);
                case 2 -> invalid.putUUID("Owner", UUID.randomUUID());
                default -> invalid.putString("Unlocked", "not-a-list");
            }
            replace(player, invalid);
            check(LibertyResearch.snapshot(player).getBoolean("Blocked")
                            && !LibertyResearch.begin(player, UUID.randomUUID())
                            && !LibertyResearch.purchase(player, "pressure_cap", revision(player))
                            && !LibertyResearch.settlePending(player) && LibertyResearch.shouldConsumePaint(player)
                            && invalid.equals(saved(player)),
                    "Malformed/future research was silently reset, changed, or granted an effect");
        }
        replace(player, valid);
        check(!LibertyResearch.snapshot(player).getBoolean("Blocked") && balance(player, "Credits") == 60,
                "Replacing a preserved record with its valid checkpoint did not restore access");
        checkVanilla(player); pass(helper, "liberty_research_schema");
    }

    @GameTest(template = "hoverboard_empty", timeoutTicks = 100)
    public static void walletCapsRemainBoundedAndPreparedPayoutsBlockPurchases(GameTestHelper helper) {
        ServerPlayer player = owner(helper); fund(player, 1, 1, 1, 1);
        CompoundTag capped = saved(player);
        for (String key : new String[]{"Credits", "Common", "Rare", "Super"}) capped.putInt(key, LibertyResearch.WALLET_LIMIT - 1);
        replace(player, capped);
        UUID run = UUID.randomUUID();
        check(LibertyResearch.begin(player, run) && LibertyResearch.stageSettlement(player, run, 10, 10, 10, 10)
                        && !LibertyResearch.purchase(player, "pressure_cap", revision(player))
                        && !LibertyResearch.abandonPending(player) && LibertyResearch.settlePending(player),
                "A prepared outcome was spent before settling, abandoned, or not consumed");
        for (String key : new String[]{"Credits", "Common", "Rare", "Super"})
            check(balance(player, key) == LibertyResearch.WALLET_LIMIT, "A research balance escaped its documented cap");
        checkVanilla(player); pass(helper, "liberty_research_caps");
    }

    private static ServerPlayer owner(GameTestHelper helper) {
        ServerPlayer player = FakePlayerFactory.get(helper.getLevel(), new GameProfile(UUID.randomUUID(), "JSC_research"));
        player.setGameMode(GameType.SURVIVAL); player.setHealth(player.getMaxHealth());
        var position = helper.absolutePos(new net.minecraft.core.BlockPos(1, 2, 1));
        player.moveTo(position.getX() + .5, position.getY(), position.getZ() + .5, 0, 0);
        player.getInventory().clearContent(); player.getInventory().setItem(0, new ItemStack(Items.DIAMOND, 7));
        player.experienceLevel = 23; player.totalExperience = 587; player.experienceProgress = .625F;
        return player;
    }

    private static void fund(ServerPlayer player, int credits, int common, int rare, int supers) {
        UUID run = UUID.randomUUID();
        check(LibertyResearch.begin(player, run) && LibertyResearch.settle(player, run, credits, common, rare, supers),
                "The earned-wallet fixture failed the real nonce/settlement path");
    }

    private static long revision(ServerPlayer player) { return LibertyResearch.snapshot(player).getLong("Revision"); }
    private static int balance(ServerPlayer player, String key) { return LibertyResearch.snapshot(player).getInt(key); }
    private static CompoundTag saved(ServerPlayer player) {
        return player.getPersistentData().getCompound(Player.PERSISTED_NBT_TAG).getCompound(LibertyResearch.KEY).copy();
    }
    private static void replace(ServerPlayer player, CompoundTag saved) {
        CompoundTag persisted = player.getPersistentData().getCompound(Player.PERSISTED_NBT_TAG);
        persisted.put(LibertyResearch.KEY, saved.copy()); player.getPersistentData().put(Player.PERSISTED_NBT_TAG, persisted);
    }
    private static void checkVanilla(ServerPlayer player) {
        check(player.experienceLevel == 23 && player.totalExperience == 587 && player.experienceProgress == .625F
                        && player.getInventory().getItem(0).is(Items.DIAMOND) && player.getInventory().getItem(0).getCount() == 7,
                "Research modified ordinary Minecraft inventory or XP");
    }
    private static void check(boolean condition, String message) { if (!condition) throw new GameTestAssertException(message); }
    private static void pass(GameTestHelper helper, String name) {
        JetSetCraft.LOGGER.info("JETSETCRAFT_LIBERTY_RESEARCH_PASS {}", name); helper.succeed();
    }
    private LibertyResearchGameTests() {}
}
