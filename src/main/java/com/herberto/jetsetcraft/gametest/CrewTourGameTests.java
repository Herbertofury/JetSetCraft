package com.herberto.jetsetcraft.gametest;

import com.herberto.jetsetcraft.JetSetCraft;
import com.herberto.jetsetcraft.crew.CrewHouseData;
import com.herberto.jetsetcraft.crew.CrewHouseManager;
import com.herberto.jetsetcraft.crew.CrewProfile;
import com.herberto.jetsetcraft.crew.CrewRewardReceipt;
import com.herberto.jetsetcraft.crew.CrewStyleLibrary;
import com.herberto.jetsetcraft.crew.CrewTour;
import com.herberto.jetsetcraft.network.C2SCrewActionPacket;
import com.herberto.jetsetcraft.registry.ModBlocks;
import com.herberto.jetsetcraft.rush.RivalRushManager;
import com.herberto.jetsetcraft.rush.RushReturnState;
import com.herberto.jetsetcraft.rush.RushRun;
import com.mojang.authlib.GameProfile;
import io.netty.buffer.Unpooled;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestAssertException;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.block.Blocks;
import net.minecraftforge.common.util.FakePlayerFactory;
import net.minecraftforge.gametest.GameTestHolder;
import net.minecraftforge.gametest.PrefixGameTestTemplate;

import java.util.UUID;

/** Production reward adapters, decoded player claims, persistence, and the actual single-preparation handoff. */
@GameTestHolder(JetSetCraft.MOD_ID)
@PrefixGameTestTemplate(false)
public final class CrewTourGameTests {
    private static final ResourceLocation GANG = ResourceLocation.tryParse("jetsetcraft:dead_beat");
    private static final ResourceLocation ZOMBIE = ResourceLocation.tryParse("minecraft:zombie");

    @GameTest(template = "hoverboard_empty", timeoutTicks = 120)
    public static void acceptedActivitiesUnlockOneClaimAndFeedTheExistingStyleLibrary(GameTestHelper helper) {
        ServerPlayer player = player(helper, "claims", freeAnchor(helper, 62000));
        CrewHouseData data = CrewHouseData.get(player.getServer()); UUID owner = player.getUUID();
        CompoundTag untouched = data.save(new CompoundTag());
        check(data.tourSnapshot(owner).getInt("Results") == 0 && data.tourRevision(owner) == 0
                        && data.save(new CompoundTag()).equals(untouched),
                "Opening an unearned Crew Tour created saved state or progress");
        UUID tag = UUID.randomUUID();
        check(!CrewHouseManager.bankActivity(player, tag, GANG, "tag", 400, 5, 600).accepted()
                        && !CrewHouseManager.bankActivity(player, tag, GANG, "invented", 400, 99, 600).accepted()
                        && data.tourRevision(owner) == 0,
                "An incomplete or unknown reward source supplied a tour stamp");
        check(CrewHouseManager.bankActivity(player, tag, GANG, "tag", 400, 6, 600).accepted(), "The real Tag award was rejected");
        check(!CrewHouseManager.bankActivity(player, tag, GANG, "tag", 400, 6, 600).accepted()
                        && data.tourRevision(owner) == 1 && track(data.tourSnapshot(owner), "TAG") == 1,
                "A repeated accepted receipt advanced the tour a second time");
        street(player, "king_of_hill"); street(player, "showdown");
        CompoundTag before = data.tourSnapshot(owner);
        check(before.getInt("Results") == 3 && before.getInt("Modes") == 3 && before.getInt("Ready") == 1
                        && milestone(before, "first_set").getBoolean("CanClaim") && data.pendingSupplies(owner) == 34
                        && data.styleXp(owner) == 220 && !CrewStyleLibrary.canSelect(data, owner, "WARD"),
                "The first mixed set changed activity payouts automatically or failed to expose its earned claim");
        packet(player, "tour_claim", "first_set", 2);
        packet(player, "tour_claim", "first_set", -1);
        packet(player, "tour_claim", "street_legend", 3);
        packet(player, "tour_claim", "unknown", 3);
        packet(player, "unknown_tour", "first_set", 3);
        check(data.tourSnapshot(owner).equals(before) && data.styleXp(owner) == 220,
                "Stale, negative, incomplete or unknown tour intents changed progress or balances");
        packet(player, "tour_claim", "first_set", 3);
        CrewRewardReceipt claim = data.recentRewards(owner).get(0);
        check(data.tourRevision(owner) == 4 && data.tourSnapshot(owner).getInt("Results") == 3
                        && data.tourSnapshot(owner).getInt("Claimed") == 1 && claim.source().equals("tour:first_set")
                        && claim.name().equals("First set claimed") && claim.supplies() == 6 && claim.crewXp() == 50
                        && claim.memberXp() == 15 && claim.reputation() == 3 && claim.pending()
                        && data.pendingSupplies(owner) == 40 && data.pendingTraining(owner) == 92
                        && data.styleXp(owner) == 270 && CrewStyleLibrary.canSelect(data, owner, "WARD"),
                "The decoded claim failed to pay the existing account or unlock Beat ward without self-stamping");
        packet(player, "tour_claim", "first_set", 3); packet(player, "tour_claim", "first_set", 4);
        check(data.tourRevision(owner) == 4 && data.pendingSupplies(owner) == 40 && data.recentRewards(owner).size() == 4,
                "An old or fresh duplicate claim replayed a completed milestone");
        CompoundTag snapshot = CrewHouseManager.snapshot(player).getCompound("CrewTour");
        check(snapshot.getInt("BaseTotal") == 9 && snapshot.getInt("OptionalTotal") == 3
                        && snapshot.getInt("BaseClaimed") == 1 && snapshot.getBoolean("CanManage"),
                "The player-facing tour snapshot did not expose authoritative core and optional progress");
        checkVanilla(player); pass(helper, "crew_tour_claims");
    }

    @GameTest(template = "hoverboard_empty", timeoutTicks = 120)
    public static void standaloneTourCompletesInAnyClaimOrderWithoutLibertyAndRushCountsAsOneMode(GameTestHelper helper) {
        ServerPlayer player = player(helper, "core", freeAnchor(helper, 64000));
        CrewHouseData data = CrewHouseData.get(player.getServer()); UUID owner = player.getUUID();
        street(player, "tag"); street(player, "king_of_hill"); arena(player); rush(player);
        check(data.tourSnapshot(owner).getInt("Modes") == 3 && data.tourSnapshot(owner).getInt("Results") == 4,
                "Rush rooms and Rush returns were counted as two different modes");
        for (int i = 0; i < 12; i++) {
            if (i > 0) { street(player, "tag"); street(player, "king_of_hill"); }
            street(player, "showdown");
        }
        for (int i = 1; i < 6; i++) { arena(player); rush(player); }
        CompoundTag ready = data.tourSnapshot(owner);
        check(ready.getInt("Modes") == 4 && ready.getInt("Ready") == 9 && ready.getInt("Results") == 48
                        && track(ready, "LIBERTY") == 0 && milestone(ready, "street_legend").getBoolean("CanClaim"),
                "Standalone activities could not finish every core milestone without an optional provider");
        // Milestones are independent goals: a complete later one does not require earlier claims.
        packet(player, "tour_claim", "street_legend", data.tourRevision(owner));
        for (CrewTour.Milestone milestone : CrewTour.MILESTONES)
            if (!milestone.optional() && !milestone.id().equals("street_legend"))
                packet(player, "tour_claim", milestone.id(), data.tourRevision(owner));
        CompoundTag collected = data.tourSnapshot(owner);
        check(collected.getInt("BaseClaimed") == 9 && collected.getInt("OptionalClaimed") == 0
                        && collected.getInt("Results") == 48 && collected.getInt("Ready") == 0,
                "Out-of-order claims lost core progress, self-stamped or forced Liberty milestones");
        check(!CrewHouseManager.bankLiberty(player, UUID.randomUUID(), GANG, 0, true).accepted(),
                "An empty expedition supplied a shared reward or tour stamp");
        for (int i = 0; i < 2; i++)
            check(CrewHouseManager.bankLiberty(player, UUID.randomUUID(), GANG, 1, false).accepted(),
                    "A genuine objective debrief was incorrectly treated as requiring successful extraction");
        CompoundTag optional = data.tourSnapshot(owner);
        check(optional.getInt("Modes") == 5 && optional.getInt("BaseClaimed") == 9
                        && optional.getInt("OptionalClaimed") == 0 && milestone(optional, "expedition_exchange").getBoolean("CanClaim")
                        && milestone(optional, "all_city").getBoolean("CanClaim")
                        && !milestone(optional, "freedom_on_wheels").getBoolean("CanClaim"),
                "Qualifying Liberty objective debriefs did not advance their separate optional track accurately");
        packet(player, "tour_claim", "all_city", data.tourRevision(owner));
        check(data.tourSnapshot(owner).getInt("OptionalClaimed") == 1 && data.tourSnapshot(owner).getInt("BaseClaimed") == 9,
                "An optional claim changed core completion or required an unrelated optional claim first");
        checkVanilla(player); pass(helper, "crew_tour_standalone");
    }

    @GameTest(template = "hoverboard_empty", timeoutTicks = 120)
    public static void tourCountsAndClaimsSurviveFoundingAndLegacyMigrationWithoutInventedHistory(GameTestHelper helper) {
        BlockPos anchor = freeAnchor(helper, 66000); ServerPlayer player = player(helper, "save", anchor);
        CrewHouseData data = CrewHouseData.get(player.getServer()); UUID owner = player.getUUID();
        UUID first = UUID.randomUUID(); CrewHouseManager.bankActivity(player, first, GANG, "tag", 400, 6, 600);
        street(player, "king_of_hill"); street(player, "showdown");
        CompoundTag old = data.save(new CompoundTag()); old.putInt("Schema", 3); old.remove("TourProgress");
        CrewHouseData migrated = CrewHouseData.load(old);
        check(migrated.tourSnapshot(owner).getInt("Results") == 3 && migrated.tourRevision(owner) == 3
                        && migrated.pendingSupplies(owner) == 34 && migrated.tourSnapshot(owner).getInt("Claimed") == 0,
                "A schema-three migration lost retained activity receipts or invented previous claims");
        CompoundTag migratedSave = migrated.save(new CompoundTag());
        check(migratedSave.getInt("Schema") == 4 && CrewHouseData.load(migratedSave).save(new CompoundTag()).equals(migratedSave),
                "The tour migration reran or failed its actual SavedData round trip");
        packet(player, "tour_claim", "first_set", data.tourRevision(owner));
        UUID claimedReceipt = data.recentRewards(owner).get(0).id();
        CompoundTag beforeFound = data.tourSnapshot(owner); int supplies = data.pendingSupplies(owner);
        CrewProfile home = found(player, anchor);
        check(data.tourSnapshot(owner).getInt("Results") == beforeFound.getInt("Results")
                        && data.tourSnapshot(owner).getInt("Claimed") == 1 && home.supplies() == supplies + 24
                        && data.tourRevision(owner) == beforeFound.getLong("Revision") + 1 + home.revision()
                        && home.xp() == 270 && home.members().get(0).xp() == 92 && data.pendingSupplies(owner) == 0,
                "Founding reset/doubled tour credit or failed to transfer its existing crew rewards exactly once");
        check(!CrewHouseManager.bankActivity(player, first, GANG, "tag", 400, 6, 600).accepted(),
                "Founding made an already-stamped activity payable again");
        for (int i = 0; i < 130; i++) street(player, "tag");
        int beforeReplay = home.supplies(); long revision = data.tourRevision(owner);
        ListTag guardedReceipts = home.save().getList("SettledRuns", Tag.TAG_COMPOUND);
        check(data.recentRewards(owner).stream().noneMatch(receipt -> receipt.source().equals("tour:first_set")),
                "The display-history eviction fixture did not evict the old tour receipt");
        check(guardedReceipts.size() == 128 && guardedReceipts.stream().noneMatch(entry -> entry instanceof CompoundTag row
                        && row.hasUUID("Id") && row.getUUID("Id").equals(claimedReceipt)),
                "The receipt-guard eviction fixture did not evict the old tour claim from all 128 retained award IDs");
        packet(player, "tour_claim", "first_set", revision);
        check(home.supplies() == beforeReplay && data.tourRevision(owner) == revision
                        && data.tourSnapshot(owner).getInt("Claimed") == 1,
                "Evicting a receipt from display history and the award guard forgot its permanent milestone claim");
        CompoundTag saved = data.save(new CompoundTag()); CrewHouseData reloaded = CrewHouseData.load(saved);
        check(saved.equals(reloaded.save(new CompoundTag())) && reloaded.tourSnapshot(owner).equals(data.tourSnapshot(owner))
                        && reloaded.claimTour(owner, "first_set", revision, 5000) == null,
                "Reloading lost permanent claims or changed accumulated tour counts");
        CompoundTag retainedOnly = saved.copy(); retainedOnly.putInt("Schema", 3); retainedOnly.remove("TourProgress");
        CrewHouseData partialHistory = CrewHouseData.load(retainedOnly);
        check(partialHistory.tourSnapshot(owner).getInt("Results") == 16
                        && partialHistory.tourSnapshot(owner).getInt("Modes") == 1,
                "Legacy migration inferred lifetime wins from XP instead of its sixteen actual retained activity receipts");
        checkVanilla(player); pass(helper, "crew_tour_persistence");
    }

    @GameTest(template = "hoverboard_empty", timeoutTicks = 120)
    public static void capsBusyTrainingAndQuarantinePreserveHonestOnceOnlyClaims(GameTestHelper helper) {
        BlockPos anchor = freeAnchor(helper, 68000); ServerPlayer player = player(helper, "caps", anchor);
        CrewHouseData data = CrewHouseData.get(player.getServer()); UUID owner = player.getUUID();
        street(player, "tag"); street(player, "king_of_hill"); street(player, "showdown");
        CompoundTag seeded = data.save(new CompoundTag());
        CompoundTag bank = owned(seeded.getList("PendingProgress", Tag.TAG_COMPOUND), owner);
        bank.putInt("Supplies", CrewProfile.SUPPLY_LIMIT - 2); bank.putLong("Xp", 999_999_997L);
        bank.putLong("Training", 999_999_996L); bank.getCompound("Reputations").putInt(GANG.toString(), 9999);
        CrewHouseData capped = CrewHouseData.load(seeded);
        CrewRewardReceipt receipt = capped.claimTour(owner, "first_set", capped.tourRevision(owner), 5000);
        check(receipt != null && receipt.supplies() == 2 && receipt.crewXp() == 3 && receipt.memberXp() == 4
                        && receipt.reputation() == 1 && receipt.capped() && capped.tourSnapshot(owner).getInt("Claimed") == 1
                        && capped.claimTour(owner, "first_set", capped.tourRevision(owner), 5000) == null,
                "A capped milestone advertised requested rewards or allowed the remainder to be claimed twice");
        long beforeFounding = data.tourRevision(owner);
        CrewProfile home = found(player, anchor);
        packet(player, "tour_claim", "first_set", beforeFounding);
        check(data.tourSnapshot(owner).getInt("Claimed") == 0 && home.xp() == 220
                        && data.tourRevision(owner) == beforeFounding + 1 + home.revision(),
                "Founding accepted an old claim prepared for a different account/training destination");
        CrewProfile.Job job = home.assignMission(home.members().get(0).id(), "courier_run", 1000, 0);
        check(job != null, "The real busy-training fixture could not dispatch its member");
        packet(player, "tour_claim", "first_set", data.tourRevision(owner));
        CrewRewardReceipt busy = data.recentRewards(owner).get(0);
        check(busy.source().equals("tour:first_set") && busy.training() == CrewRewardReceipt.Training.BUSY
                        && busy.memberXp() == 0 && home.members().get(0).xp() == 77,
                "A tour claim trained a crew member already away on a job");
        long tourRevision = data.tourRevision(owner);
        check(home.claimMissionReward(job.id(), job.readyAt()) != null && data.tourRevision(owner) == tourRevision + 1
                        && data.tourSnapshot(owner).getInt("Results") == 3,
                "A timed roster job was counted as a played activity or a tour payout recursively advanced itself");
        CompoundTag invalid = data.save(new CompoundTag());
        CompoundTag brokenRow = owned(invalid.getList("TourProgress", Tag.TAG_COMPOUND), owner);
        brokenRow.getCompound("Counts").putInt("TAG", -1); CompoundTag retained = brokenRow.copy();
        CrewHouseData quarantined = CrewHouseData.load(invalid);
        check(quarantined.blocked(owner) && quarantined.tourSnapshot(owner).getBoolean("Blocked")
                        && quarantined.claimTour(owner, "first_set", 0, 5000) == null
                        && containsExact(quarantined.save(new CompoundTag()).getList("Quarantined", Tag.TAG_COMPOUND), retained),
                "Malformed tour progress was silently reset, paid, or discarded instead of retained for recovery");
        CompoundTag unknown = data.save(new CompoundTag());
        CompoundTag futureClaim = owned(unknown.getList("TourProgress", Tag.TAG_COMPOUND), owner);
        futureClaim.getList("Claimed", Tag.TAG_STRING).add(StringTag.valueOf("future_milestone"));
        CrewHouseData unknownClaim = CrewHouseData.load(unknown);
        check(unknownClaim.blocked(owner) && unknownClaim.claimTour(owner, "first_set", 0, 5000) == null
                        && containsExact(unknownClaim.save(new CompoundTag()).getList("Quarantined", Tag.TAG_COMPOUND), futureClaim),
                "An unknown saved claim was forgotten instead of preserved with its blocked owner");
        CompoundTag blankGang = data.save(new CompoundTag());
        CompoundTag blankRow = owned(blankGang.getList("TourProgress", Tag.TAG_COMPOUND), owner);
        blankRow.putString("LastGang", ""); CrewHouseData noDestination = CrewHouseData.load(blankGang);
        check(noDestination.blocked(owner)
                        && containsExact(noDestination.save(new CompoundTag()).getList("Quarantined", Tag.TAG_COMPOUND), blankRow),
                "Earned progress with a blank reward destination was accepted or silently discarded");
        CompoundTag wrongList = data.save(new CompoundTag()); ListTag strings = new ListTag(); strings.add(StringTag.valueOf("retain me"));
        wrongList.put("TourProgress", strings); CrewHouseData wrongType = CrewHouseData.load(wrongList);
        check(wrongType.readOnly() && wrongType.save(new CompoundTag()).equals(wrongList),
                "A malformed top-level tour list was silently dropped");
        CompoundTag future = data.save(new CompoundTag()); future.putInt("Schema", 999); future.putString("FutureTour", "retain me");
        CrewHouseData futureData = CrewHouseData.load(future);
        check(futureData.readOnly() && futureData.claimTour(owner, "first_set", 0, 5000) == null
                        && futureData.save(new CompoundTag()).equals(future),
                "An unknown future crew schema changed or admitted a tour claim");
        checkVanilla(player); pass(helper, "crew_tour_safety");
    }

    @GameTest(template = "hoverboard_empty", timeoutTicks = 120)
    public static void trainingChangesInvalidateClaimsAndReadOnlyPreviewsKeepTheirRevision(GameTestHelper helper) {
        BlockPos anchor = freeAnchor(helper, 72000); ServerPlayer player = player(helper, "recipient", anchor);
        CrewHouseData data = CrewHouseData.get(player.getServer()); UUID owner = player.getUUID();
        street(player, "tag"); street(player, "king_of_hill"); street(player, "showdown");
        check(data.tourSnapshot(owner).getBoolean("TrainingEligible")
                        && data.tourSnapshot(owner).getString("TrainingSummary").contains("first home"),
                "The unclaimed tour did not explain its pending training destination");
        CrewProfile home = found(player, anchor);
        CrewProfile.Member first = home.members().get(0), second = home.members().get(1);
        long idleRevision = data.tourRevision(owner);
        CompoundTag untouched = data.save(new CompoundTag());
        check(data.tourSnapshot(owner).getLong("Revision") == idleRevision
                        && data.tourSnapshot(owner).getString("TrainingSummary").contains(first.name())
                        && CrewHouseManager.snapshot(player).getCompound("CrewTour").getLong("Revision") == idleRevision
                        && data.save(new CompoundTag()).equals(untouched),
                "A read-only preview changed the reward account or its claim revision");
        CrewHouseManager.handleAction(player, "focus", "", 0, home.id(), second.id(), home.revision());
        packet(player, "tour_claim", "first_set", idleRevision);
        check(data.tourRevision(owner) > idleRevision && data.tourSnapshot(owner).getInt("Claimed") == 0
                        && data.tourSnapshot(owner).getString("TrainingSummary").contains(second.name())
                        && first.xp() == 77 && second.xp() == 0,
                "An old claim silently trained a different member after the training focus changed");

        long focusedRevision = data.tourRevision(owner);
        CrewHouseManager.handleAction(player, "assign", "courier_run", 0, home.id(), second.id(), home.revision());
        check(home.jobs().size() == 1, "The training-recipient fixture could not dispatch its selected member");
        packet(player, "tour_claim", "first_set", focusedRevision);
        check(data.tourSnapshot(owner).getInt("Claimed") == 0 && home.xp() == 220 && second.xp() == 0
                        && !data.tourSnapshot(owner).getBoolean("TrainingEligible")
                        && data.tourSnapshot(owner).getString("TrainingSummary").contains("away on a job"),
                "A claim prepared for an idle member was accepted after dispatch and silently lost its training");

        long busyRevision = data.tourRevision(owner);
        CrewHouseManager.handleAction(player, "cancel", "", 0, home.id(), home.jobs().get(0).id(), home.revision());
        packet(player, "tour_claim", "first_set", busyRevision);
        check(data.tourSnapshot(owner).getInt("Claimed") == 0 && data.tourSnapshot(owner).getBoolean("TrainingEligible")
                        && data.tourSnapshot(owner).getInt("Results") == 3,
                "Returning a member to idle accepted a stale claim or manufactured activity stamps");
        long beforeDisable = data.tourRevision(owner);
        CrewHouseManager.handleAction(player, "focus_off", "", 0, home.id(), null, home.revision());
        packet(player, "tour_claim", "first_set", beforeDisable);
        check(data.tourSnapshot(owner).getInt("Claimed") == 0 && !data.tourSnapshot(owner).getBoolean("TrainingEligible")
                        && data.tourSnapshot(owner).getString("TrainingSummary").contains("no training focus"),
                "Disabling training did not invalidate its prior claim or explain the unavailable training");
        CrewHouseManager.handleAction(player, "focus", "", 0, home.id(), second.id(), home.revision());
        CompoundTag saved = data.save(new CompoundTag()); CrewHouseData reloaded = CrewHouseData.load(saved);
        check(saved.equals(reloaded.save(new CompoundTag())) && reloaded.tourRevision(owner) == data.tourRevision(owner)
                        && reloaded.tourSnapshot(owner).equals(data.tourSnapshot(owner)),
                "Reloading changed the compound claim revision or current training preview");
        packet(player, "tour_claim", "first_set", data.tourRevision(owner));
        CrewRewardReceipt claim = data.recentRewards(owner).get(0);
        check(claim.source().equals("tour:first_set") && second.id().equals(claim.memberId())
                        && claim.training() == CrewRewardReceipt.Training.APPLIED && claim.memberXp() == 15
                        && first.xp() == 77 && second.xp() == 15 && data.tourSnapshot(owner).getInt("Results") == 3,
                "The refreshed claim did not apply exactly once to the current idle member");
        checkVanilla(player); pass(helper, "crew_tour_training_revision");
    }

    @GameTest(template = "hoverboard_empty", timeoutTicks = 120)
    public static void combinedClaimRevisionsSaturateWithoutWrappingOrMutatingBlockedClaims(GameTestHelper helper) {
        BlockPos anchor = freeAnchor(helper, 74000); ServerPlayer player = player(helper, "revision", anchor);
        CrewHouseData data = CrewHouseData.get(player.getServer()); UUID owner = player.getUUID();
        street(player, "tag"); street(player, "king_of_hill"); street(player, "showdown"); found(player, anchor);
        CompoundTag saved = data.save(new CompoundTag());
        long rawTourRevision = owned(saved.getList("TourProgress", Tag.TAG_COMPOUND), owner).getLong("Revision");
        for (long accountRevision : new long[]{Long.MAX_VALUE - rawTourRevision, Long.MAX_VALUE - 1, Long.MAX_VALUE}) {
            CompoundTag terminal = saved.copy();
            owned(terminal.getList("Profiles", Tag.TAG_COMPOUND), owner).putLong("Revision", accountRevision);
            CrewHouseData bounded = CrewHouseData.load(terminal); CompoundTag before = bounded.save(new CompoundTag());
            check(bounded.tourRevision(owner) == Long.MAX_VALUE
                            && !milestone(bounded.tourSnapshot(owner), "first_set").getBoolean("CanClaim")
                            && bounded.claimTour(owner, "first_set", Long.MAX_VALUE, 5000) == null
                            && bounded.claimTour(owner, "first_set", Long.MIN_VALUE, 5000) == null
                            && bounded.save(new CompoundTag()).equals(before),
                    "A saturated account/tour revision wrapped or changed a claim's permanent state");
        }
        CompoundTag nearLimit = saved.copy();
        owned(nearLimit.getList("Profiles", Tag.TAG_COMPOUND), owner).putLong("Revision", Long.MAX_VALUE - rawTourRevision - 1);
        CrewHouseData lastClaim = CrewHouseData.load(nearLimit);
        CrewRewardReceipt receipt = lastClaim.claimTour(owner, "first_set", Long.MAX_VALUE - 1, 5000);
        check(receipt != null && lastClaim.tourRevision(owner) == Long.MAX_VALUE
                        && lastClaim.tourSnapshot(owner).getInt("Claimed") == 1
                        && CrewHouseData.load(lastClaim.save(new CompoundTag())).tourRevision(owner) == Long.MAX_VALUE,
                "The final valid claim failed or wrapped its persistent revision on save/reload");
        checkVanilla(player); pass(helper, "crew_tour_revision_saturation");
    }

    @GameTest(template = "hoverboard_empty", timeoutTicks = 240)
    public static void tourClaimsAreBlockedDuringRushAndNeverAddASecondStartingPreparation(GameTestHelper helper) {
        ServerPlayer player = player(helper, "entry", freeAnchor(helper, 70000));
        CrewHouseData data = CrewHouseData.get(player.getServer()); UUID owner = player.getUUID();
        street(player, "tag"); street(player, "king_of_hill"); street(player, "showdown");
        packet(player, "style", "BATTERY", data.styleRevision(owner));
        long revision = data.tourRevision(owner); ListTag inventory = player.getInventory().save(new ListTag());
        try {
            check(RivalRushManager.start(player, GANG, ZOMBIE, false), "The tour integration fixture could not enter a real Rush run");
            RushRun run = RivalRushManager.run(player); int totalRanks = 0;
            for (RushRun.Upgrade upgrade : RushRun.Upgrade.values()) totalRanks += run.rank(upgrade);
            check(totalRanks == 1 && run.rank(RushRun.Upgrade.BATTERY) == 1 && run.artifactCount() == 1
                            && !run.grantStartingPerk(RushRun.Upgrade.WARD),
                    "Earned tour progress added a second starting preparation or changed the normal artifact limit");
            packet(player, "tour_claim", "first_set", revision);
            CompoundTag during = CrewHouseManager.snapshot(player).getCompound("CrewTour");
            check(data.tourRevision(owner) == revision && data.styleXp(owner) == 220 && !during.getBoolean("CanManage")
                            && !milestone(during, "first_set").getBoolean("CanClaim"),
                    "An active run accepted a tour claim or presented a claimable player action");
            check(RivalRushManager.leave(player, "Crew Tour test return") && !RushReturnState.hasJournal(player),
                    "The tour-aware player could not use the normal Rush return journal");
        } finally {
            if (RivalRushManager.active(player) || RushReturnState.hasJournal(player))
                check(RivalRushManager.leave(player, "Crew Tour fixture cleanup"), "Could not restore the tour fixture player");
        }
        packet(player, "tour_claim", "first_set", revision);
        check(data.styleXp(owner) == 270 && data.selectedStyle(owner).equals("BATTERY")
                        && inventory.equals(player.getInventory().save(new ListTag())),
                "Returning failed to restore the claim or changed the selected style/ordinary inventory");
        checkVanilla(player); pass(helper, "crew_tour_entry");
    }

    private static void street(ServerPlayer player, String mode) {
        int objectives = mode.equals("tag") ? 6 : mode.equals("showdown") ? 4 : 3;
        check(CrewHouseManager.bankActivity(player, UUID.randomUUID(), GANG, mode, 600, objectives, 1000).accepted(),
                "The production " + mode + " settlement was rejected");
    }
    private static void arena(ServerPlayer player) {
        check(CrewHouseManager.bankActivity(player, UUID.randomUUID(), GANG, "rush_arena", 600, 1, 1000).accepted(),
                "The production room-clear settlement was rejected");
    }
    private static void rush(ServerPlayer player) { CrewHouseManager.bankRush(player, UUID.randomUUID(), GANG, 1200, 3, 3, 1000); }
    private static CompoundTag milestone(CompoundTag tour, String id) {
        for (Tag entry : tour.getList("Milestones", Tag.TAG_COMPOUND))
            if (entry instanceof CompoundTag row && row.getString("Id").equals(id)) return row;
        throw new GameTestAssertException("Missing Crew Tour milestone " + id);
    }
    private static int track(CompoundTag tour, String id) {
        for (Tag entry : tour.getList("Tracks", Tag.TAG_COMPOUND))
            if (entry instanceof CompoundTag row && row.getString("Id").equals(id)) return row.getInt("Count");
        throw new GameTestAssertException("Missing Crew Tour track " + id);
    }
    private static CompoundTag owned(ListTag rows, UUID owner) {
        for (Tag entry : rows) if (entry instanceof CompoundTag row && row.hasUUID("Owner") && row.getUUID("Owner").equals(owner)) return row;
        throw new GameTestAssertException("Missing saved owner fixture " + owner);
    }
    private static boolean containsExact(ListTag rows, CompoundTag target) {
        for (Tag row : rows) if (row.equals(target)) return true;
        return false;
    }
    private static void packet(ServerPlayer player, String action, String value, long revision) {
        FriendlyByteBuf buffer = new FriendlyByteBuf(Unpooled.buffer());
        try {
            C2SCrewActionPacket intent = new C2SCrewActionPacket(action, value, 0, null, null, revision);
            C2SCrewActionPacket.encode(intent, buffer); C2SCrewActionPacket received = C2SCrewActionPacket.decode(buffer);
            check(received.equals(intent) && buffer.readableBytes() == 0, "The real crew packet changed a tour intent");
            C2SCrewActionPacket.handleForPlayer(received, player);
        } finally { buffer.release(); }
    }
    private static BlockPos freeAnchor(GameTestHelper helper, int initialX) {
        CrewHouseData data = CrewHouseData.get(helper.getLevel().getServer());
        for (int attempt = 0; attempt < 128; attempt++) {
            BlockPos anchor = new BlockPos(initialX + attempt * 128, 100, 30000);
            if (data.profiles().stream().noneMatch(profile -> {
                BlockPos existing = BlockPos.of(profile.save().getLong("Anchor"));
                return Math.abs((long) existing.getX() - anchor.getX()) < 100 && Math.abs((long) existing.getZ() - anchor.getZ()) < 100;
            })) return anchor;
        }
        throw new GameTestAssertException("No isolated plot is available for the tour fixture");
    }
    private static ServerPlayer player(GameTestHelper helper, String name, BlockPos anchor) {
        helper.getLevel().getChunkAt(anchor);
        for (int x = -2; x <= 2; x++) for (int z = -1; z <= 3; z++) {
            BlockPos floor = anchor.offset(x, -1, z); helper.getLevel().setBlockAndUpdate(floor, Blocks.STONE.defaultBlockState());
            helper.getLevel().setBlockAndUpdate(floor.above(), Blocks.AIR.defaultBlockState());
            helper.getLevel().setBlockAndUpdate(floor.above(2), Blocks.AIR.defaultBlockState());
        }
        ServerPlayer player = FakePlayerFactory.get(helper.getLevel(), new GameProfile(UUID.randomUUID(), "JSC_tour_" + name));
        player.setGameMode(GameType.SURVIVAL); player.setHealth(player.getMaxHealth());
        player.moveTo(anchor.getX() + .5, anchor.getY(), anchor.getZ() + 2.5, 0, 0);
        player.getInventory().clearContent(); player.containerMenu.setCarried(ItemStack.EMPTY);
        player.getInventory().setItem(0, new ItemStack(Items.DIAMOND, 7));
        player.experienceLevel = 23; player.totalExperience = 587; player.experienceProgress = .625F;
        return player;
    }
    private static CrewProfile found(ServerPlayer player, BlockPos anchor) {
        player.serverLevel().setBlockAndUpdate(anchor, ModBlocks.BOOMBOX.get().defaultBlockState());
        CrewHouseManager.openAt(player, anchor);
        CrewHouseManager.handleAction(player, "found", "Tour crew", 0, null, null, 0);
        CrewProfile profile = CrewHouseData.get(player.getServer()).profile(player.getUUID());
        check(profile != null, "Production crew founding failed at the tour fixture's real Boombox"); return profile;
    }
    private static void checkVanilla(ServerPlayer player) {
        check(player.experienceLevel == 23 && player.totalExperience == 587 && player.experienceProgress == .625F
                        && player.getInventory().getItem(0).is(Items.DIAMOND) && player.getInventory().getItem(0).getCount() == 7,
                "Tour progression changed ordinary Minecraft inventory or XP");
    }
    private static void check(boolean condition, String message) { if (!condition) throw new GameTestAssertException(message); }
    private static void pass(GameTestHelper helper, String marker) { System.out.println("JETSETCRAFT_GAMETEST_PASS " + marker); helper.succeed(); }
    private CrewTourGameTests() {}
}
