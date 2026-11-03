package com.herberto.jetsetcraft.gametest;

import com.herberto.jetsetcraft.JetSetCraft;
import com.herberto.jetsetcraft.liberty.LibertyOperation;
import com.herberto.jetsetcraft.liberty.LibertyDistricts;
import com.herberto.jetsetcraft.data.SavedDataCheckpoint;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestAssertException;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.gametest.GameTestHolder;
import net.minecraftforge.gametest.PrefixGameTestTemplate;

import java.util.UUID;
import java.nio.file.Path;

/** Exercise server mission rules through their public production entry points; no reflected state or score injection. */
@GameTestHolder(JetSetCraft.MOD_ID)
@PrefixGameTestTemplate(false)
public final class LibertyOperationGameTests {
    private static final Vec3 NON_FINITE = new Vec3(Double.NaN, 64, 0);

    @GameTest(template = "hoverboard_empty", timeoutTicks = 100)
    public static void muralNeedsOwnedDefeatsAndDistinctDesignatedReachableFaces(GameTestHelper helper) {
        LibertyOperation run = operation(false, false);
        run.phase(LibertyOperation.Phase.ACTIVE);
        var objective = run.objectives.get(0);
        BlockPos panel = objective.panel(0);
        Vec3 close = Vec3.atCenterOf(panel).add(0, 0, 2);
        check(!objective.paint(panel, Direction.SOUTH, close), "A guarded mural could be tagged before clearing its crew");
        UUID actor = UUID.randomUUID();
        check(objective.defeated(actor) && !objective.defeated(actor) && !objective.defeated(null) && objective.defeats() == 1,
                "A repeated or missing actor identity manufactured a second defeat");
        while (objective.defeats() < objective.defenders()) check(objective.defeated(UUID.randomUUID()), "A distinct rival defeat was rejected");
        check(!objective.defeated(UUID.randomUUID()) && !objective.complete() && !run.awardObjectives(),
                "Rival clearance alone completed or paid the graffiti objective");
        check(!objective.paint(panel, Direction.NORTH, close)
                        && !objective.paint(panel.east(2), Direction.SOUTH, close)
                        && !objective.paint(panel, Direction.SOUTH, Vec3.atCenterOf(panel).add(0, 0, 6.01))
                        && !objective.paint(null, Direction.SOUTH, close)
                        && !objective.paint(panel, Direction.SOUTH, null)
                        && !objective.paint(panel, Direction.SOUTH, NON_FINITE),
                "Wrong, unmarked, remote or non-finite paint qualified for a mural");
        check(objective.paint(panel.offset(-1, 1, 0), Direction.SOUTH, close)
                        && !objective.paint(new BlockPos(panel.getX(), panel.getY(), panel.getZ()), Direction.SOUTH, close)
                        && !objective.paint(panel.offset(1, -1, 0), Direction.SOUTH, close)
                        && !objective.paint(panel, Direction.EAST, close) && objective.paints() == 1,
                "A corner cell was rejected or another cell/coordinate alias/face farmed the same mural panel");
        for (int index = 1; index < objective.panels(); index++) {
            BlockPos next = objective.panel(index);
            check(objective.paint(next, Direction.SOUTH, Vec3.atCenterOf(next).add(0, 0, 2)), "A distinct marked face did not count");
        }
        check(objective.complete() && !objective.paint(panel, Direction.SOUTH, close) && run.completed() == 1
                        && run.awardObjectives() && run.credits() == run.theater.reward() / 3
                        && run.common() == 3 && run.rare() == 1 && !run.awardObjectives()
                        && run.credits() == run.theater.reward() / 3,
                "A completed mural was uncounted or repeated paint/render settlement paid it again");
        pass(helper, "liberty_mural_rules");
    }

    @GameTest(template = "hoverboard_empty", timeoutTicks = 100)
    public static void pirateRadioRequiresTenSecondsOfLocalActualSkating(GameTestHelper helper) {
        LibertyOperation run = operation(false, false); run.phase(LibertyOperation.Phase.ACTIVE);
        var objective = run.objectives.get(1);
        Vec3 center = Vec3.atCenterOf(objective.center());
        for (int i = 0; i < 220; i++) objective.advance(center, center.add(1, 0, 0), true);
        check(objective.broadcastTicks() == 0, "The broadcast started before the crew and mural were secured");
        clearAndPaint(objective);
        for (int i = 0; i < 220; i++) {
            objective.advance(center, center, true);
            objective.advance(center, center.add(1, 0, 0), false);
            objective.advance(center.add(25, 0, 0), center.add(26, 0, 0), true);
            objective.advance(center, center.add(100, 0, 0), true);
            objective.advance(center, null, true);
            objective.advance(null, center, true);
            objective.advance(NON_FINITE, center, true);
        }
        check(objective.broadcastTicks() == 0 && !objective.complete(),
                "Idling, walking, remote movement, teleports or invalid positions advanced the radio");
        for (int i = 0; i < 199; i++) skateInPlaceArea(objective, center, i);
        check(objective.broadcastTicks() == 199 && !objective.complete(), "Radio completed before ten seconds of eligible skating");
        skateInPlaceArea(objective, center, 199);
        check(objective.complete() && objective.broadcastTicks() == 200 && run.awardObjectives()
                        && !run.awardObjectives(), "The full broadcast did not complete exactly once");
        skateInPlaceArea(objective, center, 200);
        check(objective.broadcastTicks() == 200, "A completed broadcast kept generating progress");
        pass(helper, "liberty_radio_rules");
    }

    @GameTest(template = "hoverboard_empty", timeoutTicks = 100)
    public static void courierBeaconsAreOrderedAndRejectIdleAndTeleportProgress(GameTestHelper helper) {
        LibertyOperation run = operation(false, false); run.phase(LibertyOperation.Phase.ACTIVE);
        var objective = run.objectives.get(2); clearAndPaint(objective);
        Vec3 first = Vec3.atCenterOf(objective.beacon(0));
        Vec3 second = Vec3.atCenterOf(objective.beacon(1));
        Vec3 third = Vec3.atCenterOf(objective.beacon(2));
        objective.advance(third, third.add(1, 0, 0), true);
        objective.advance(second, second.add(1, 0, 0), true);
        objective.advance(first, first, true);
        objective.advance(first, first.add(100, 0, 0), true);
        check(objective.relay() == 0, "The courier skipped its first beacon or counted an idle/teleport arrival");
        objective.advance(first, first.add(1, 0, 0), true);
        objective.advance(first, first.add(1, 0, 0), true);
        objective.advance(third, third.add(1, 0, 0), true);
        check(objective.relay() == 1 && !objective.complete(), "Repeating or skipping a beacon advanced the ordered relay");
        objective.advance(second, second.add(1, 0, 0), false);
        check(objective.relay() == 1, "Unskated courier movement counted");
        objective.advance(second, second.add(1, 0, 0), true);
        objective.advance(third, third.add(1, 0, 0), true);
        check(objective.relay() == 3 && objective.complete() && run.awardObjectives() && !run.awardObjectives(),
                "All three ordered skating beacons failed to secure one objective reward");
        pass(helper, "liberty_courier_rules");
    }

    @GameTest(template = "hoverboard_empty", timeoutTicks = 100)
    public static void cachesAndResupplyChargesAreDistinctFiniteAndConfinedToTheField(GameTestHelper helper) {
        LibertyOperation run = operation(false, false);
        check(!run.cache(0) && !run.spendResupply(), "Cargo or supply charges could be used while dropping");
        run.phase(LibertyOperation.Phase.ACTIVE);
        check(!run.cache(-1) && !run.cache(9), "An unknown cache index created cargo");
        for (int index = 0; index < 9; index++) {
            check(run.cache(index) && run.cacheClaimed(index) && !run.cache(index), "A cache was unclaimable or paid twice");
            for (int other = 0; other < index; other++)
                check(!run.cachePosition(index).equals(run.cachePosition(other)), "Different cache IDs overlapped the same authored site");
        }
        check(run.credits() == 30 && run.common() == 9 && run.rare() == 4 && run.supers() == 1,
                "The nine cache types did not produce their displayed credit/sample quantities");
        long revision = run.revision();
        check(run.matches(run.id, revision) && run.spendResupply() && !run.matches(run.id, revision)
                        && run.spendResupply() && run.spendResupply() && !run.spendResupply() && run.resupplies() == 0,
                "Supply charge spending was not bounded or did not invalidate a stale action");
        LibertyOperation researched = operation(true, true);
        check(researched.objectives.get(0).panels() == 2 && researched.objectives.get(1).panels() == 1
                        && researched.resupplies() == 4 && !researched.spendResupply(),
                "Research altered the wrong objective or supplied charges outside the deployment lifecycle");
        researched.phase(LibertyOperation.Phase.RETURNING);
        check(!researched.cache(0) && !researched.spendResupply(), "A returning session could collect new cargo");
        pass(helper, "liberty_cache_supply_rules");
    }

    @GameTest(template = "hoverboard_empty", timeoutTicks = 100)
    public static void extractionRequiresAllObjectivesPhysicalBeaconAndBoardingWindow(GameTestHelper helper) {
        LibertyOperation run = operation(false, false);
        Vec3 extraction = Vec3.atCenterOf(run.extraction);
        check(!run.extract(extraction) && !run.board(extraction), "Transport could be called or boarded during insertion");
        run.phase(LibertyOperation.Phase.ACTIVE);
        check(!run.extract(extraction), "Extraction was available before completing the operation");
        finishObjectives(run);
        check(run.complete() && run.completed() == 3 && run.awardObjectives()
                        && run.credits() == (run.theater.reward() / 3) * 3 && run.common() == 9 && run.rare() == 3
                        && !run.awardObjectives(), "Full mission objectives did not pay exactly once each");
        check(!run.extract(extraction.add(12.01, 0, 0)) && !run.extract(null) && !run.extract(NON_FINITE),
                "The extraction call accepted a remote, missing or invalid player position");
        long before = run.revision();
        check(run.extract(extraction.add(12, 0, 0)) && run.phase() == LibertyOperation.Phase.EXTRACTING
                        && !run.matches(run.id, before) && !run.extract(extraction) && !run.board(extraction),
                "Extraction did not transition once or boarding opened before the ship arrived");
        run.tick(); run.tick();
        check(run.phaseTicks() == 2, "Transport phase time did not advance");
        run.phase(LibertyOperation.Phase.BOARDING);
        check(run.phaseTicks() == 0 && !run.board(extraction), "A ship still arriving could be boarded before landing");
        for (int tick = 0; tick < 59; tick++) run.tick();
        check(!run.board(extraction), "Boarding opened one tick before touchdown");
        run.tick();
        check(!run.board(extraction.add(8.01, 0, 0))
                        && !run.board(null) && !run.board(NON_FINITE),
                "Boarding did not reset its phase clock or accepted a remote/invalid passenger");
        check(run.board(extraction.add(8, 0, 0)) && run.phase() == LibertyOperation.Phase.DEPARTING
                        && !run.board(extraction) && !run.extract(extraction) && !run.spendResupply() && !run.cache(0),
                "A departing ship allowed another board, restart, supply spend or cache claim");
        pass(helper, "liberty_extraction_rules");
    }

    @GameTest(template = "hoverboard_empty", timeoutTicks = 100)
    public static void lastTickBoardingCommitsDepartureButDeadlineRejectsNewActions(GameTestHelper helper) {
        LibertyOperation departing = operation(false, false);
        prepareBoarding(departing);
        long deadline = departing.theater.minutes() * 1200L;
        advanceUntil(departing, deadline - 1);
        Vec3 pad = Vec3.atCenterOf(departing.extraction);
        check(!departing.expired() && departing.board(pad)
                        && departing.phase() == LibertyOperation.Phase.DEPARTING,
                "Boarding during the last valid mission tick did not commit departure");
        long revision = departing.revision();
        check(!departing.board(pad) && !departing.extract(pad) && departing.phaseTicks() == 0
                        && departing.revision() == revision,
                "Duplicate boarding or extraction restarted the accepted departure");
        // The manager's automatic flight lasts 100 ticks, crossing the deadline on its first tick.
        for (int tick = 1; tick <= 100; tick++) {
            departing.tick();
            check(!departing.expired() && departing.phaseTicks() == tick
                            && !departing.board(pad) && !departing.extract(pad)
                            && departing.revision() == revision,
                    "The mission timer revoked or restarted an already accepted departure");
        }
        check(departing.ticks() == deadline + 99 && departing.phaseTicks() == 100,
                "The complete departure animation did not finish beyond the mission cutoff");

        LibertyOperation lateBoard = operation(false, false);
        prepareBoarding(lateBoard);
        advanceUntil(lateBoard, deadline);
        long boardingRevision = lateBoard.revision(), boardingTicks = lateBoard.phaseTicks();
        check(lateBoard.expired() && !lateBoard.board(Vec3.atCenterOf(lateBoard.extraction))
                        && lateBoard.phase() == LibertyOperation.Phase.BOARDING
                        && lateBoard.revision() == boardingRevision && lateBoard.phaseTicks() == boardingTicks,
                "Boarding at the exact cutoff bypassed expiry or changed the rejected session");
        lateBoard.tick();
        check(lateBoard.expired() && !lateBoard.board(Vec3.atCenterOf(lateBoard.extraction)),
                "Boarding became available again after the mission deadline");

        LibertyOperation lateCall = operation(false, false);
        lateCall.phase(LibertyOperation.Phase.ACTIVE);
        finishObjectives(lateCall);
        advanceUntil(lateCall, deadline);
        long activeRevision = lateCall.revision();
        check(lateCall.complete() && lateCall.expired() && !lateCall.extract(Vec3.atCenterOf(lateCall.extraction))
                        && lateCall.phase() == LibertyOperation.Phase.ACTIVE && lateCall.revision() == activeRevision,
                "Completed objectives allowed a new extraction call at the exact cutoff");
        lateCall.tick();
        check(lateCall.expired() && !lateCall.extract(Vec3.atCenterOf(lateCall.extraction)),
                "An extraction call became available after the mission deadline");

        LibertyOperation lateExtract = operation(false, false);
        lateExtract.phase(LibertyOperation.Phase.ACTIVE);
        finishObjectives(lateExtract);
        advanceUntil(lateExtract, deadline - 1);
        check(lateExtract.extract(Vec3.atCenterOf(lateExtract.extraction)),
                "An extraction call was rejected before the advertised mission cutoff");
        lateExtract.tick();
        check(lateExtract.expired() && lateExtract.phase() == LibertyOperation.Phase.EXTRACTING,
                "Calling extraction without boarding granted extra mission time");

        LibertyOperation downed = operation(false, false);
        downed.phase(LibertyOperation.Phase.DOWNED);
        advanceUntil(downed, deadline - 1);
        check(!downed.expired(), "A downed mission expired before its cutoff");
        downed.tick();
        check(downed.expired() && !downed.board(Vec3.atCenterOf(downed.extraction)),
                "Being downed bypassed the mission clock or allowed boarding");
        pass(helper, "liberty_extraction_deadline_rules");
    }

    @GameTest(template = "hoverboard_empty", timeoutTicks = 100)
    public static void authoredTheatersKeepDistinctScaleAndBoundedMissionClocks(GameTestHelper helper) {
        check(LibertyOperation.THEATERS.size() == 3 && LibertyOperation.theater("unknown") == null,
                "The war map omitted authored theaters or accepted an unknown one");
        int previousRadius = 0;
        for (var theater : LibertyOperation.THEATERS) {
            check(theater.radius() > previousRadius && theater.gangId() != null && theater.speciesId() != null,
                    "The war-map tiers lost their increasing scale or source mob identities");
            previousRadius = theater.radius();
            LibertyOperation run = new LibertyOperation(UUID.randomUUID(), UUID.randomUUID(), theater,
                    new BlockPos(0, 64, 0), 0, false, false);
            for (long tick = 0; tick < theater.minutes() * 1200L - 1; tick++) run.tick();
            check(!run.expired(), "A mission expired before its advertised duration");
            run.tick(); check(run.expired(), "A mission exceeded its advertised duration without expiry");
        }
        LibertyDistricts districts = new LibertyDistricts();
        check(districts.allocate() == 0 && districts.allocate() == 1, "The district allocator reused a live district");
        CompoundTag saved = districts.save(new CompoundTag());
        check(saved.getInt("Schema") == 1 && saved.getInt("Next") == 2
                        && LibertyDistricts.load(saved).allocate() == 2 && saved.getInt("Next") == 2,
                "Saved district allocation did not resume monotonically or mutated its load source");
        for (int next : new int[]{-1, 1_000_001}) {
            CompoundTag invalid = saved.copy(); invalid.putInt("Next", next);
            LibertyDistricts retained = LibertyDistricts.load(invalid);
            check(retained.allocate() == -1 && retained.save(new CompoundTag()).equals(invalid),
                    "An invalid saved district counter was reset or overwritten");
        }
        CompoundTag future = saved.copy(); future.putInt("Schema", 2); future.putString("FutureLayout", "retain");
        LibertyDistricts retained = LibertyDistricts.load(future);
        check(retained.allocate() == -1 && retained.save(new CompoundTag()).equals(future),
                "A future district schema was allocated over or rewritten");
        CompoundTag exhausted = saved.copy(); exhausted.putInt("Next", 999_999);
        LibertyDistricts last = LibertyDistricts.load(exhausted);
        check(last.allocate() == 999_999 && last.allocate() == -1
                        && LibertyDistricts.load(last.save(new CompoundTag())).allocate() == -1,
                "Exhausted district allocation wrapped or reused an earlier arena after reload");
        // Missing-file acknowledgement is a real read failure without editing any server world file.
        districts.setDirty(false);
        Path absent = Path.of(System.getProperty("java.io.tmpdir"), "jetsetcraft-missing-" + UUID.randomUUID() + ".dat");
        check(!SavedDataCheckpoint.acknowledge(absent, districts, saved) && districts.isDirty(),
                "An unacknowledged world-data checkpoint was reported saved or failed to preserve its retry flag");
        pass(helper, "liberty_war_map_rules");
    }

    private static LibertyOperation operation(boolean signal, boolean supply) {
        return new LibertyOperation(UUID.randomUUID(), UUID.randomUUID(), LibertyOperation.THEATERS.get(0),
                new BlockPos(0, 64, 0), 0, signal, supply);
    }
    private static void prepareBoarding(LibertyOperation run) {
        run.phase(LibertyOperation.Phase.ACTIVE);
        finishObjectives(run);
        check(run.extract(Vec3.atCenterOf(run.extraction)), "A completed fixture could not call extraction");
        for (int tick = 0; tick < 600; tick++) run.tick();
        run.phase(LibertyOperation.Phase.BOARDING);
        for (int tick = 0; tick < 60; tick++) run.tick();
    }
    private static void advanceUntil(LibertyOperation run, long targetTick) {
        while (run.ticks() < targetTick) run.tick();
    }
    private static void clearAndPaint(LibertyOperation.Objective objective) {
        while (objective.defeats() < objective.defenders())
            check(objective.defeated(UUID.randomUUID()), "A real distinct fixture defeat was rejected");
        for (int i = 0; i < objective.panels(); i++) {
            BlockPos panel = objective.panel(i);
            check(objective.paint(panel, Direction.SOUTH, Vec3.atCenterOf(panel).add(0, 0, 2)),
                    "The authored panel rejected its valid cleared-crew paint event");
        }
    }
    private static void finishObjectives(LibertyOperation run) {
        for (var objective : run.objectives) {
            clearAndPaint(objective);
            if (objective.index() == 1) for (int tick = 0; tick < 200; tick++)
                skateInPlaceArea(objective, Vec3.atCenterOf(objective.center()), tick);
            if (objective.index() == 2) for (int i = 0; i < 3; i++) {
                Vec3 beacon = Vec3.atCenterOf(objective.beacon(i));
                objective.advance(beacon, beacon.add(1, 0, 0), true);
            }
        }
    }
    private static void skateInPlaceArea(LibertyOperation.Objective objective, Vec3 center, int tick) {
        objective.advance(center.add(tick % 2, 0, 0), center.add((tick + 1) % 2, 0, 0), true);
    }
    private static void check(boolean condition, String message) { if (!condition) throw new GameTestAssertException(message); }
    private static void pass(GameTestHelper helper, String name) {
        JetSetCraft.LOGGER.info("JETSETCRAFT_LIBERTY_OPERATION_PASS {}", name); helper.succeed();
    }
    private LibertyOperationGameTests() {}
}
