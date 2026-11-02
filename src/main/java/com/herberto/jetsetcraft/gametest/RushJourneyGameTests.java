package com.herberto.jetsetcraft.gametest;

import com.herberto.jetsetcraft.JetSetCraft;
import com.herberto.jetsetcraft.crew.CrewHouseManager;
import com.herberto.jetsetcraft.rush.RushArenaCatalog;
import com.herberto.jetsetcraft.rush.RushCircuit;
import com.herberto.jetsetcraft.rush.RushRun;
import com.herberto.jetsetcraft.rush.RushWorld;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestAssertException;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.gametest.GameTestHolder;
import net.minecraftforge.gametest.PrefixGameTestTemplate;

import java.util.UUID;

/** Real server rules/geometry fixtures; no claim of human skating or real-time six-room survival. */
@GameTestHolder(JetSetCraft.MOD_ID)
@PrefixGameTestTemplate(false)
public final class RushJourneyGameTests {
    @GameTest(template = "hoverboard_empty", timeoutTicks = 100)
    public static void journeyPreviewNamesOnlyActualOfferedBranches(GameTestHelper helper) {
        RushRun run = run(791L);
        BlockPos origin = new BlockPos(-3_500_000, RushWorld.FLOOR_Y, 3_500_000);
        RushCircuit route = new RushCircuit(run.id(), run.seed(), origin, 0);
        CompoundTag preview = route.snapshot(run, Vec3.atCenterOf(origin));
        verifyChoosing(preview, 0, 0);
        check(preview.getInt("Set") == 1 && preview.getInt("Stage") == 1 && preview.getInt("MapScaleBlocks") == 128,
                "Initial journey changed its set or physical map scale");
        ListTag choices = preview.getList("Choices", Tag.TAG_COMPOUND);
        for (int i = 0; i < choices.size(); i++) {
            CompoundTag choice = choices.getCompound(i);
            var profile = RushArenaCatalog.find(id(choice.getString("Id"))).orElseThrow();
            String mode = profile.sharedReward().equals("balanced") ? "rush_arena" : "rush_arena_" + profile.sharedReward();
            var expected = CrewHouseManager.activityReward(mode);
            CompoundTag shared = choice.getCompound("SharedReward");
            check(choice.getString("Name").equals(profile.name()) && choice.getInt("Seconds") == profile.seconds()
                            && choice.getInt("Defeats") == profile.defeats() && choice.getInt("Radius") == profile.radius()
                            && !choice.getString("RunReward").isBlank(), "An offered room omitted or invented its authored objective/reward");
            check(shared.getInt("Supplies") == expected.supplies() && shared.getInt("CrewXp") == expected.crewXp()
                            && shared.getInt("MemberXp") == expected.memberXp() && shared.getInt("Reputation") == expected.reputation()
                            && !shared.getBoolean("Accepted"), "A preview disagrees with the real shared reward or claims it was banked");
        }
        ResourceLocation selected = id(choices.getCompound(2).getString("Id"));
        int offeredVersion = preview.getInt("Version");
        check(!route.choose(selected, offeredVersion - 1) && !route.choose(id("jetsetcraft:unoffered_room"), offeredVersion)
                        && route.version() == offeredVersion, "A rejected route intent altered the real offer");
        check(route.choose(selected, offeredVersion) && !route.choose(selected, offeredVersion), "An actual choice was lost or replayed");
        CompoundTag travel = route.snapshot(run, Vec3.atCenterOf(origin));
        check(travel.getString("Phase").equals("travel") && travel.getList("History", Tag.TAG_COMPOUND).isEmpty()
                        && travel.getList("Choices", Tag.TAG_COMPOUND).isEmpty()
                        && travel.getCompound("Current").getString("Id").equals(selected.toString())
                        && travel.getCompound("Current").getInt("MapX") == 1 && travel.getCompound("Current").getInt("MapZ") == 1,
                "The map duplicated an unchosen branch or lost the physical chosen destination");
        travel.getCompound("Current").putString("Name", "forged client copy");
        choices.getCompound(0).getCompound("SharedReward").putInt("Supplies", 999);
        check(!route.snapshot(run, Vec3.atCenterOf(origin)).getCompound("Current").getString("Name").equals("forged client copy"),
                "A returned NBT map mutated the server's route");
        pass(helper, "rush_journey_preview");
    }

    @GameTest(template = "hoverboard_empty", timeoutTicks = 240)
    public static void completedJourneyRecordsOnlyActualOnceOnlySettlementsAndResetsOnEncore(GameTestHelper helper) {
        RushRun run = run(887L); run.grantReward(RushRun.Upgrade.FORCE);
        BlockPos origin = new BlockPos(-3_750_000, RushWorld.FLOOR_Y, 3_750_000);
        RushCircuit route = new RushCircuit(run.id(), run.seed(), origin, 0);
        ServerLevel district = helper.getLevel().getServer().getLevel(RushWorld.DIMENSION);
        check(district != null, "Rush dimension is unavailable for the route fixture");
        UUID firstReceipt = null;
        int previousX = 0, firstOfferVersion = route.version();
        for (int stage = 1; stage <= 6; stage++) {
            int choiceIndex = stage % 2 == 0 ? 2 : 0;
            var profile = route.choices().get(choiceIndex);
            check(route.choose(profile.id(), route.version()), "An offered fixture room could not be chosen");
            previousX += choiceIndex - 1;
            loadChunks(district, route.center());
            for (int step = 0; step < 160 && !route.builder().readyForEntry(); step++) route.builder().step(district);
            check(route.builder().readyForEntry() && route.arrive(run, Vec3.atCenterOf(route.center()).add(0, .5, 0)),
                    "The actual constructed room did not admit its route");
            // Advance only this pure rules fixture's run clock/counters, never the world or visual audit.
            for (int tick = 0; tick < profile.seconds() * 20; tick++) run.tickActive();
            for (int defeat = 0; defeat < profile.defeats(); defeat++) run.recordDefeat();
            UUID receipt = route.rewardId();
            if (firstReceipt == null) firstReceipt = receipt;
            check(route.complete(run) && !route.complete(run), "Room completion was missing or repeated");
            CompoundTag before = route.snapshot(run, Vec3.atCenterOf(route.center()));
            CompoundTag cleared = before.getCompound("LastCompleted");
            check(!before.contains("Current", Tag.TAG_COMPOUND) && before.getList("History", Tag.TAG_COMPOUND).size() == stage
                            && cleared.getInt("Stage") == stage && cleared.getInt("MapX") == previousX && cleared.getInt("MapZ") == stage
                            && cleared.getUUID("Receipt").equals(receipt) && !cleared.getBoolean("SettlementRecorded")
                            && cleared.getCompound("SharedReward").isEmpty(),
                    "Completing a room invented banked rewards, future rooms or a duplicate current node");
            CompoundTag amounts = settled(stage != 3);
            int version = route.version();
            check(!route.recordSettlement(UUID.randomUUID(), "unowned receipt", amounts) && route.version() == version,
                    "An unrelated receipt mutated the debrief");
            if (stage > 1) check(!route.recordSettlement(firstReceipt, "old room replay", amounts), "A previous room receipt was accepted again");
            CompoundTag invalid = settled(false); invalid.putInt("Supplies", 1);
            check(!route.recordSettlement(receipt, "unbanked value", invalid), "An unavailable settlement claimed a positive payout");
            check(route.recordSettlement(receipt, "Actual fixture run grant " + stage, amounts)
                            && !route.recordSettlement(receipt, "overwrite", amounts), "A real latest settlement was lost or overwritten");
            amounts.putInt("Supplies", 999);
            CompoundTag snapshot = route.snapshot(run, Vec3.atCenterOf(route.center()));
            CompoundTag last = snapshot.getCompound("LastCompleted"), shared = last.getCompound("SharedReward");
            check(last.getBoolean("SettlementRecorded") && last.getString("RunReward").equals("Actual fixture run grant " + stage)
                            && shared.getBoolean("Accepted") == (stage != 3) && shared.getInt("Supplies") == (stage == 3 ? 0 : 5)
                            && shared.getInt("CrewXp") == (stage == 3 ? 0 : 7) && shared.getInt("MemberXp") == (stage == 3 ? 0 : 3)
                            && shared.getInt("Reputation") == (stage == 3 ? 0 : 1),
                    "The debrief substituted preview rewards for actual deltas or retained mutable caller data");
            snapshot.getList("History", Tag.TAG_COMPOUND).getCompound(stage - 1).getCompound("SharedReward").putInt("Supplies", 999);
            check(route.snapshot(run, Vec3.atCenterOf(route.center())).getCompound("LastCompleted").getCompound("SharedReward")
                            .getInt("Supplies") == (stage == 3 ? 0 : 5), "A map consumer mutated a retained settlement");
            if (stage < 6) verifyChoosing(snapshot, stage, previousX);
            else check(snapshot.getString("Phase").equals("complete") && snapshot.getList("Choices", Tag.TAG_COMPOUND).isEmpty(),
                    "The six-room ending invented another offer before Encore");
        }
        long elapsed = run.elapsedTicks();
        check(run.continueEndless() && route.encore() && !route.encore(), "Encore could not transition exactly once");
        CompoundTag encore = route.snapshot(run, Vec3.atCenterOf(origin));
        verifyChoosing(encore, 0, 0);
        check(encore.getInt("Set") == 2 && !encore.contains("LastCompleted", Tag.TAG_COMPOUND)
                        && run.rank(RushRun.Upgrade.FORCE) == 1 && run.elapsedTicks() == elapsed
                        && !route.choose(route.choices().get(0).id(), firstOfferVersion)
                        && !route.recordSettlement(firstReceipt, "old-set receipt", settled(true)),
                "Encore retained old map rewards, accepted an old version or lost the earned build");
        pass(helper, "rush_journey_history");
    }

    private static void verifyChoosing(CompoundTag state, int completed, int previousX) {
        check(state.getString("Phase").equals("choosing") && state.getInt("Completed") == completed
                        && state.getList("History", Tag.TAG_COMPOUND).size() == completed && !state.contains("Current", Tag.TAG_COMPOUND),
                "Choosing state duplicated a destination or invented a completion");
        ListTag choices = state.getList("Choices", Tag.TAG_COMPOUND);
        check(choices.size() == 3, "The map omitted an actual offered branch");
        for (int i = 0; i < choices.size(); i++) {
            CompoundTag choice = choices.getCompound(i);
            check(choice.getInt("Stage") == completed + 1 && choice.getInt("MapX") == previousX + i - 1
                            && choice.getInt("MapZ") == completed + 1, "An offered map branch disagrees with physical connector placement");
        }
    }

    private static CompoundTag settled(boolean accepted) {
        CompoundTag tag = new CompoundTag(); tag.putBoolean("Accepted", accepted); tag.putBoolean("PendingHouse", accepted);
        tag.putInt("Supplies", accepted ? 5 : 0); tag.putInt("CrewXp", accepted ? 7 : 0);
        tag.putInt("MemberXp", accepted ? 3 : 0); tag.putInt("Reputation", accepted ? 1 : 0);
        return tag;
    }

    private static RushRun run(long seed) {
        RushRun run = new RushRun(UUID.randomUUID(), UUID.randomUUID(), seed, false, "jetsetcraft:dead_beat");
        run.configureCircuit(); return run;
    }

    private static void loadChunks(ServerLevel level, BlockPos center) {
        ChunkPos chunk = new ChunkPos(center);
        for (int x = -1; x <= 1; x++) for (int z = -1; z <= 1; z++) level.getChunk(chunk.x + x, chunk.z + z);
    }

    private static ResourceLocation id(String value) { return ResourceLocation.tryParse(value); }
    private static void check(boolean condition, String message) { if (!condition) throw new GameTestAssertException(message); }
    private static void pass(GameTestHelper helper, String marker) { JetSetCraft.LOGGER.info("JETSETCRAFT_GAMETEST_PASS {}", marker); helper.succeed(); }
    private RushJourneyGameTests() {}
}
