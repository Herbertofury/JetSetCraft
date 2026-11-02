package com.herberto.jetsetcraft.gametest;

import com.herberto.jetsetcraft.JetSetCraft;
import com.herberto.jetsetcraft.crew.CrewHouseData;
import com.herberto.jetsetcraft.crew.CrewHouseManager;
import com.herberto.jetsetcraft.crew.CrewProfile;
import com.herberto.jetsetcraft.crew.CrewRewardReceipt;
import com.herberto.jetsetcraft.registry.ModBlocks;
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
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.block.Blocks;
import net.minecraftforge.common.util.FakePlayerFactory;
import net.minecraftforge.gametest.GameTestHolder;
import net.minecraftforge.gametest.PrefixGameTestTemplate;

import java.util.List;
import java.util.UUID;

/** Actual reward accounts and native saved records; fixture clocks never advance the shared server time. */
@GameTestHolder(JetSetCraft.MOD_ID)
@PrefixGameTestTemplate(false)
public final class CrewProgressGameTests {
    private static final ResourceLocation GANG = ResourceLocation.tryParse("jetsetcraft:dead_beat");

    @GameTest(template = "hoverboard_empty", timeoutTicks = 100)
    public static void earnedHistoryPersistsAndFollowsTheFirstHouseExactlyOnce(GameTestHelper helper) {
        BlockPos anchor = anchor(helper, 36000);
        ServerPlayer owner = owner(helper, anchor);
        CrewHouseData data = CrewHouseData.get(owner.getServer());
        UUID first = UUID.randomUUID();
        CrewHouseManager.bankActivity(owner, first, GANG, "tag", 400, 6, 600);
        CrewHouseManager.bankActivity(owner, UUID.randomUUID(), GANG, "king_of_hill", 500, 3, 600);
        CrewHouseManager.bankActivity(owner, UUID.randomUUID(), GANG, "showdown", 400, 4, 600);
        List<CrewRewardReceipt> earned = data.recentRewards(owner.getUUID());
        check(earned.size() == 3 && earned.get(0).source().equals("showdown") && earned.get(2).id().equals(first)
                        && earned.stream().allMatch(CrewRewardReceipt::pending)
                        && data.pendingSupplies(owner.getUUID()) == 34 && data.pendingTraining(owner.getUUID()) == 77,
                "Pre-house history did not contain the actual ordered shared awards");
        CompoundTag pendingSave = data.save(new CompoundTag());
        CrewHouseData reloadedPending = CrewHouseData.load(pendingSave);
        check(reloadedPending.recentRewards(owner.getUUID()).equals(earned)
                        && reloadedPending.save(new CompoundTag()).equals(pendingSave),
                "Pending reward amounts, identities or labels failed the real SavedData round trip");
        check(CrewHouseManager.snapshot(owner).getList("RecentRewards", Tag.TAG_COMPOUND).size() == 3
                        && CrewHouseManager.snapshot(owner).getCompound("NextGoal").getString("View").equals("HOME"),
                "A player without a house could not inspect saved progress and the founding goal");

        placeAnchor(owner, anchor);
        CrewHouseManager.openAt(owner, anchor);
        CrewHouseManager.handleAction(owner, "found", "Progress crew", 0, null, null, 0);
        CrewProfile house = data.profile(owner.getUUID());
        check(house != null && house.supplies() == 58 && house.members().get(0).xp() == 77
                        && house.members().get(0).level() == 2 && house.members().get(1).xp() == 0
                        && data.pendingSupplies(owner.getUUID()) == 0 && data.recentRewards(owner.getUUID()).equals(earned),
                "Founding replayed, lost or reassigned banked training/history instead of transferring it once");
        check(!data.add(house) && !CrewHouseManager.bankActivity(owner, first, GANG, "tag", 400, 6, 600).accepted()
                        && house.supplies() == 58 && data.recentRewards(owner.getUUID()).size() == 3,
                "Founding or repeated activity settlement produced another reward or history row");
        for (int i = 0; i < 20; i++) CrewHouseManager.bankActivity(owner, UUID.randomUUID(), GANG, "tag", 400, 6, 600);
        check(data.recentRewards(owner.getUUID()).size() == 16
                        && data.recentRewards(owner.getUUID()).stream().noneMatch(receipt -> receipt.id().equals(first)),
                "The human-readable history exceeded its sixteen-entry bound");
        int supplies = house.supplies();
        check(!CrewHouseManager.bankActivity(owner, first, GANG, "tag", 400, 6, 600).accepted() && house.supplies() == supplies,
                "Evicting an old display row weakened the separate settlement replay guard");
        CrewHouseData reloadedHouse = CrewHouseData.load(data.save(new CompoundTag()));
        check(reloadedHouse.recentRewards(owner.getUUID()).equals(data.recentRewards(owner.getUUID()))
                        && reloadedHouse.profile(owner.getUUID()).members().get(0).xp() == house.members().get(0).xp(),
                "Founded history or accumulated member progress failed persistence");
        checkVanilla(owner); pass(helper, "crew_progress_history");
    }

    @GameTest(template = "hoverboard_empty", timeoutTicks = 100)
    public static void receiptsReportCapsBusyMembersAndMissingFocusWithoutInventedAwards(GameTestHelper helper) {
        BlockPos anchor = anchor(helper, 38000);
        ServerPlayer owner = owner(helper, anchor);
        CrewProfile house = seededHouse(owner, anchor, CrewProfile.SUPPLY_LIMIT - 2, 999_999_998L, 999_999_997L, 9999, false);
        CrewHouseData data = CrewHouseData.get(owner.getServer());
        UUID event = UUID.randomUUID();
        var actual = CrewHouseManager.bankActivity(owner, event, GANG, "tag", 400, 6, 600);
        CrewRewardReceipt receipt = data.recentRewards(owner.getUUID()).get(0);
        check(actual.accepted() && actual.supplies() == 2 && actual.crewXp() == 2 && actual.memberXp() == 3 && actual.reputation() == 1
                        && receipt.supplies() == 2 && receipt.crewXp() == 2 && receipt.memberXp() == 3 && receipt.reputation() == 1
                        && receipt.capped() && !receipt.memberLeveledUp() && receipt.training() == CrewRewardReceipt.Training.APPLIED,
                "The receipt advertised requested values instead of actual cap-limited wallet/member deltas");
        check(!CrewHouseManager.bankActivity(owner, event, GANG, "tag", 400, 6, 600).accepted()
                        && data.recentRewards(owner.getUUID()).size() == 1,
                "A duplicate capped result emitted a second reward receipt");
        UUID focused = house.members().get(0).id();
        check(house.assignMission(focused, "courier_run", 1000, 0) != null, "Busy-member fixture could not dispatch its real saved job");
        CrewHouseManager.bankActivity(owner, UUID.randomUUID(), GANG, "showdown", 400, 4, 600);
        CrewRewardReceipt busy = data.recentRewards(owner.getUUID()).get(0);
        check(busy.training() == CrewRewardReceipt.Training.BUSY && busy.memberXp() == 0 && focused.equals(busy.memberId())
                        && busy.memberLevelBefore() == busy.memberLevelAfter(),
                "A busy training focus received XP or an invented level-up in the receipt");
        CrewHouseManager.handleAction(owner, "focus_off", "", 0, house.id(), null, house.revision());
        CrewHouseManager.bankActivity(owner, UUID.randomUUID(), GANG, "tag", 400, 6, 600);
        CrewRewardReceipt noFocus = data.recentRewards(owner.getUUID()).get(0);
        check(noFocus.training() == CrewRewardReceipt.Training.NO_FOCUS && noFocus.memberXp() == 0 && noFocus.memberId() == null,
                "Clearing the training focus left a phantom recipient in the shared receipt");
        CompoundTag goal = CrewHouseManager.snapshot(owner).getCompound("NextGoal");
        check(goal.getInt("Cost") == 8 && goal.getBoolean("Affordable") && !goal.getBoolean("Ready")
                        && goal.getInt("Shortfall") == 0 && goal.getString("Target").equals("workshop"),
                "An affordable workshop goal pretended that the required physical blocks were already registered");
        check(CrewHouseData.load(data.save(new CompoundTag())).recentRewards(owner.getUUID()).equals(data.recentRewards(owner.getUUID())),
                "Capped/busy/no-focus receipts failed persistence");
        checkVanilla(owner); pass(helper, "crew_progress_caps");
    }

    @GameTest(template = "hoverboard_empty", timeoutTicks = 100)
    public static void missionReceiptNamesItsAssignedMemberAndPersistsTheRealLevelTransition(GameTestHelper helper) {
        BlockPos anchor = anchor(helper, 40000);
        ServerPlayer owner = owner(helper, anchor);
        CrewProfile house = seededHouse(owner, anchor, 24, 0, 50, 0, true);
        CrewHouseData data = CrewHouseData.get(owner.getServer());
        UUID assigned = house.members().get(0).id(), idleFocus = house.members().get(1).id();
        CrewProfile.Job job = house.assignMission(assigned, "courier_run", 1000, 0);
        check(job != null && house.claimMissionReward(job.id(), job.readyAt() - 1) == null,
                "The progress ledger allowed a reward before the mission's persisted deadline");
        CrewRewardReceipt receipt = house.claimMissionReward(job.id(), job.readyAt());
        check(receipt != null && receipt.source().equals("mission:courier_run") && receipt.memberId().equals(assigned)
                        && receipt.memberName().equals("Riff") && receipt.memberXp() == 35 && receipt.memberLevelBefore() == 1
                        && receipt.memberLevelAfter() == 2 && receipt.memberLeveledUp() && receipt.levelSummary().contains("Riff reached level 2")
                        && receipt.supplies() == 15 && receipt.crewXp() == 40 && receipt.reputation() == 6,
                "The mission receipt lost its actual frozen rewards or the assigned member's 50→85 XP level transition");
        check(house.members().get(0).xp() == 85 && house.members().get(1).id().equals(idleFocus) && house.members().get(1).xp() == 0
                        && house.claimMissionReward(job.id(), job.readyAt()) == null && house.recentRewards().size() == 1,
                "Mission feedback trained the activity focus, replayed the claim, or duplicated its history row");
        CrewProfile reloaded = CrewHouseData.load(data.save(new CompoundTag())).profile(owner.getUUID());
        check(reloaded != null && reloaded.recentRewards().equals(List.of(receipt)) && reloaded.members().get(0).level() == 2
                        && reloaded.jobs().isEmpty() && reloaded.claimMissionReward(job.id(), job.readyAt()) == null,
                "The saved level-up receipt or consumed job changed after reloading");
        checkVanilla(owner); pass(helper, "crew_progress_mission");
    }

    private static CrewProfile seededHouse(ServerPlayer owner, BlockPos anchor, int supplies, long crewXp, long firstXp, int reputation, boolean focusSecond) {
        placeAnchor(owner, anchor);
        CompoundTag tag = new CrewProfile(owner.getUUID(), UUID.randomUUID(), "Receipt fixture", GANG.toString(),
                owner.level().dimension().location().toString(), anchor).save();
        tag.putInt("Supplies", supplies); tag.putLong("Xp", crewXp);
        ListTag members = new ListTag(); UUID first = UUID.randomUUID(), second = UUID.randomUUID();
        for (int i = 0; i < 2; i++) {
            CompoundTag member = new CompoundTag(); member.putUUID("Id", i == 0 ? first : second); member.putString("Name", i == 0 ? "Riff" : "Patches");
            member.putString("Species", "minecraft:zombie"); member.putString("Gang", GANG.toString());
            member.putString("Role", i == 0 ? "courier" : "tagger"); member.putLong("Xp", i == 0 ? firstXp : 0); members.add(member);
        }
        tag.put("Members", members); tag.putUUID("Focus", focusSecond ? second : first);
        CompoundTag relations = new CompoundTag(); relations.putInt(GANG.toString(), reputation); tag.put("Reputations", relations);
        CrewProfile profile = CrewProfile.load(tag);
        check(profile != null && CrewHouseData.get(owner.getServer()).add(profile), "The valid saved-account fixture was rejected");
        return profile;
    }
    private static BlockPos anchor(GameTestHelper helper, int x) { return new BlockPos(x, helper.absolutePos(new BlockPos(0, 1, 0)).getY(), 24000); }
    private static ServerPlayer owner(GameTestHelper helper, BlockPos anchor) {
        ServerPlayer owner = FakePlayerFactory.get(helper.getLevel(), new GameProfile(UUID.randomUUID(), "JSC_progress"));
        owner.setGameMode(GameType.SURVIVAL); owner.setHealth(owner.getMaxHealth());
        owner.moveTo(anchor.getX() + .5, anchor.getY() + 1, anchor.getZ() + .5, 0, 0);
        owner.getInventory().clearContent(); owner.getInventory().setItem(0, new ItemStack(Items.DIAMOND, 7));
        owner.experienceLevel = 23; owner.totalExperience = 587; owner.experienceProgress = .625F;
        return owner;
    }
    private static void placeAnchor(ServerPlayer owner, BlockPos anchor) {
        owner.serverLevel().setBlockAndUpdate(anchor.below(), Blocks.STONE.defaultBlockState());
        owner.serverLevel().setBlockAndUpdate(anchor, ModBlocks.BOOMBOX.get().defaultBlockState());
    }
    private static void checkVanilla(ServerPlayer player) {
        check(player.experienceLevel == 23 && player.totalExperience == 587 && player.experienceProgress == .625F
                        && player.getInventory().getItem(0).is(Items.DIAMOND) && player.getInventory().getItem(0).getCount() == 7,
                "Reward feedback or history changed ordinary Minecraft inventory or XP");
    }
    private static void check(boolean condition, String message) { if (!condition) throw new GameTestAssertException(message); }
    private static void pass(GameTestHelper helper, String marker) { System.out.println("JETSETCRAFT_GAMETEST_PASS " + marker); helper.succeed(); }
    private CrewProgressGameTests() {}
}
