package com.herberto.jetsetcraft.gametest;

import com.herberto.jetsetcraft.JetSetCraft;
import com.herberto.jetsetcraft.liberty.LibertyDifficulty;
import com.herberto.jetsetcraft.liberty.LibertyMissionCatalog;
import com.herberto.jetsetcraft.liberty.LibertyMissionPlan;
import com.herberto.jetsetcraft.liberty.LibertyMissionRuntime;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestAssertException;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.gametest.GameTestHolder;
import net.minecraftforge.gametest.PrefixGameTestTemplate;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import static com.herberto.jetsetcraft.liberty.LibertyMissionCatalog.ObjectiveKind;

/** Actual public mission actions exercise staging, ownership, timing and deterministic replay. */
@GameTestHolder(JetSetCraft.MOD_ID)
@PrefixGameTestTemplate(false)
public final class LibertyMissionGameTests {
    private static final Vec3 INVALID = new Vec3(Double.NaN, 64, 0);

    @GameTest(template = "hoverboard_empty", timeoutTicks = 100)
    public static void allMissionFamiliesReplayAcrossDifficultyTimerAndFaction(GameTestHelper helper) {
        int plans = 0;
        Set<LibertyMissionPlan.Point> firstSites = new HashSet<>();
        for (var faction : LibertyMissionCatalog.factions()) for (var template : LibertyMissionCatalog.templates(faction))
            for (int difficulty = 1; difficulty <= 10; difficulty++) for (int minutes : new int[]{20, 25, 30})
                for (long seed : new long[]{0, 19, -991}) {
                    var plan = LibertyMissionPlan.generate(seed, "neon_frontier", faction, template.id(), difficulty, minutes);
                    check(plan.equals(LibertyMissionPlan.generate(seed, "neon_frontier", faction, template.id(), difficulty, minutes)), "Replay changed for the same versioned seed");
                    check(plan.stages().size() == 3 && plan.totalTicks() == minutes * 1200 && plan.arenaRadius() >= 512
                            && plan.arenaRadius() <= 768 && plan.difficulty().residentCap() <= 96
                            && plan.difficulty().maxSpawnsPerTick() == 8, "A mission escaped its arena, timer or resident limits");
                    int previous = -1;
                    for (long time : new long[]{-100, 0, 1, 1200, 6000, minutes * 1200L, Long.MAX_VALUE}) {
                        int allowance = plan.difficulty().allowanceAt(time, minutes);
                        check(allowance >= previous && allowance <= plan.difficulty().budgetForMinutes(minutes), "Reinforcement allowance was unbounded or went backward");
                        previous = allowance;
                    }
                    var objectives = plan.allObjectives();
                    Set<String> ids = new HashSet<>();
                    for (int i = 0; i < objectives.size(); i++) {
                        var objective = objectives.get(i);
                        check(ids.add(objective.id()) && Math.abs(objective.center().x()) + objective.radius() < plan.arenaRadius()
                                && Math.abs(objective.center().z()) + objective.radius() < plan.arenaRadius(), "A site overlapped an ID or left its arena");
                        check(objective.center().distanceSquared(plan.drop()) >= 128 * 128
                                && objective.center().distanceSquared(plan.extraction()) >= 128 * 128, "A mission site blocked a landing area");
                        for (int j = 0; j < i; j++) check(objective.center().distanceSquared(objectives.get(j).center()) >= 96 * 96, "Mission site footprints collided");
                        for (var point : objective.checkpoints()) check(Math.abs(point.x()) < plan.arenaRadius() && Math.abs(point.z()) < plan.arenaRadius(), "A route left the playable arena");
                    }
                    check(new LibertyMissionRuntime(plan, BlockPos.ZERO, false).cores().size()
                                    == plan.strongholds().stream().mapToInt(LibertyMissionPlan.Stronghold::cores).sum(), "Base plan and physical core count disagreed");
                    firstSites.add(plan.primaryObjectives().get(0).center());
                    plans++;
                }
        int eligible = LibertyMissionCatalog.factions().stream().mapToInt(f -> LibertyMissionCatalog.templates(f).size()).sum();
        check(plans == eligible * 90 && firstSites.size() > plans * .9 && LibertyMissionCatalog.templates().size() == 34,
                "Replay catalogue collapsed or omitted a faction/template");
        rejected(() -> LibertyDifficulty.of(0)); rejected(() -> LibertyDifficulty.of(11));
        rejected(() -> LibertyMissionPlan.generate(1, "invalid/sector", LibertyMissionCatalog.Faction.AUTOMATON, "random", 1, 25));
        rejected(() -> LibertyMissionPlan.generate(1, "neon_docks", LibertyMissionCatalog.Faction.AUTOMATON, "nest_diving", 1, 25));
        rejected(() -> LibertyMissionPlan.generate(1, "neon_docks", LibertyMissionCatalog.Faction.AUTOMATON, "random", 1, 35));
        pass(helper, "liberty_mission_replay_catalogue");
    }

    @GameTest(template = "hoverboard_empty", timeoutTicks = 100)
    public static void turfUsesDistinctReachableCellsAndCanBeReclaimedUntilSecured(GameTestHelper helper) {
        var run = fixture(false, List.of(ObjectiveKind.TERRITORY, ObjectiveKind.SCAN), null);
        var turf = run.sites().get(0);
        BlockPos support = turf.center().above();
        Vec3 near = Vec3.atCenterOf(support).add(0, 0, 2);
        check(run.paint(support, Direction.UP, near) && !run.paint(support, Direction.NORTH, near)
                && !run.paint(support.above(), Direction.SOUTH, near)
                && !run.paint(support.east(), Direction.WEST, near), "Face, height or adjacent block aliases manufactured new turf cells");
        check(!run.paint(support.east(4), Direction.UP, near.add(20, 0, 0)) && !run.paint(support, null, near)
                && !run.paint(null, Direction.UP, near) && !run.paint(support, Direction.UP, INVALID), "Remote or invalid graffiti counted");
        BlockPos second = support.east(4);
        check(run.paint(second, Direction.UP, Vec3.atCenterOf(second)), "A genuinely distinct reachable turf cell was rejected");
        check(!run.interact(run.sites().get(1).marker()), "A future stage terminal activated early");
        for (int i = 0; i < 80; i++) run.tick(near, near, false, p -> 1);
        check(turf.progress() == 1 && turf.paintedCells().size() == 1, "A contesting rival did not reclaim one unfinished cell in four seconds");
        check(run.paintArea(turf.marker(), 14) == 3 && turf.complete() && run.stage() == 1 && run.consumeRewards() == 1
                && run.consumeRewards() == 0 && run.paintArea(turf.marker(), 14) == 0, "Real area paint did not secure exactly the remaining cells and one reward");
        for (int i = 0; i < 160; i++) run.tick(near, near, false, p -> 1);
        check(turf.progress() == turf.goal() && turf.complete(), "Secured turf reverted after its stage advanced");
        pass(helper, "liberty_mission_real_turf");
    }

    @GameTest(template = "hoverboard_empty", timeoutTicks = 100)
    public static void relayScanAndDefenseUseDifferentActualOwnershipActions(GameTestHelper helper) {
        var run = fixture(false, List.of(ObjectiveKind.RELAY_HOLD, ObjectiveKind.SCAN, ObjectiveKind.DEFEND), null);
        var relay = run.sites().get(0);
        Vec3 at = relay.marker();
        for (int i = 0; i < 80; i++) {
            run.tick(at, at, true, p -> 0);
            run.tick(at, at.add(1, 0, 0), false, p -> 0);
            run.tick(at, at.add(20, 0, 0), true, p -> 0);
        }
        check(relay.progress() == 0, "Idle, walking or teleported player advanced a skating relay");
        skate(run, at, 20, 0);
        check(relay.progress() == 1, "Real local skating failed to broadcast");
        skate(run, at, 20, 1);
        check(relay.progress() == 0 && relay.contested(), "Contested ownership did not decay");
        skate(run, at, 40, 0);
        check(relay.complete() && run.stage() == 1, "Uncontested skating did not finish the relay");
        var scan = run.sites().get(1);
        at = scan.marker();
        for (int i = 0; i < 40; i++) run.tick(at, at, false, p -> 0);
        check(scan.progress() == 0 && !run.interact(at.add(6, 0, 0)) && run.interact(at) && !run.interact(at), "A scan began without one nearby terminal interaction");
        for (int i = 0; i < 20; i++) run.tick(at, at, false, p -> 0);
        for (int i = 0; i < 20; i++) run.tick(at, at, false, p -> 1);
        check(scan.progress() == 1 && !scan.complete(), "A contested scan gained time or completed early");
        run.tick(at.add(7, 0, 0), at, false, p -> 0);
        check(!scan.activated() && scan.progress() == 0 && run.interact(at), "Leaving a scan did not require a fresh local activation");
        for (int i = 0; i < 40; i++) run.tick(at, at, false, p -> 0);
        var defend = run.sites().get(2);
        at = defend.marker();
        for (int i = 0; i < 39; i++) run.tick(at, at, false, p -> 0);
        check(!defend.complete(), "A defense completed before its full held duration");
        run.tick(at, at, false, p -> 0);
        check(run.complete() && run.completedPrimary() == 3 && run.consumeRewards() == 3 && run.consumeRewards() == 0,
                "Stationary defense or once-only staged reward accounting failed");
        pass(helper, "liberty_mission_timed_actions");
    }

    @GameTest(template = "hoverboard_empty", timeoutTicks = 100)
    public static void courierNeedsPickupOrderedSkatingAndFutureCoresStayLocked(GameTestHelper helper) {
        var run = fixture(false, List.of(ObjectiveKind.COURIER, ObjectiveKind.DEMOLITION), null);
        var courier = run.sites().get(0);
        List<Vec3> route = courier.checkpoints().stream().map(LibertyMissionGameTests::stand).toList();
        Vec3 first = route.get(0), second = route.get(1), third = route.get(2);
        check(!run.damageCore(run.cores().get(0).id(), Float.MAX_VALUE), "A future-stage native core could complete early");
        run.tick(first, first.add(1, 0, 0), true, p -> 0);
        check(courier.progress() == 0 && run.interact(courier.marker()), "Courier route advanced before its actual pickup");
        run.tick(second, second.add(1, 0, 0), true, p -> 0);
        run.tick(first, first, true, p -> 0);
        run.tick(first, first.add(20, 0, 0), true, p -> 0);
        run.tick(first, first.add(1, 0, 0), false, p -> 0);
        check(courier.progress() == 0, "A skipped, stationary, teleported or walking checkpoint counted");
        run.tick(first, first.add(1, 0, 0), true, p -> 0);
        run.tick(first, first.add(1, 0, 0), true, p -> 0);
        run.tick(third, third.add(1, 0, 0), true, p -> 0);
        check(courier.progress() == 1, "Repeating/skipping route beacons advanced the ordered courier");
        run.tick(second, second.add(1, 0, 0), true, p -> 0);
        run.tick(third, third.add(1, 0, 0), true, p -> 0);
        check(courier.complete() && run.stage() == 1, "Valid ordered skating failed to deliver the canister");
        var core = run.cores().get(0);
        check(!run.damageCore(core.id(), Float.NaN) && !run.damageCore(core.id(), Float.POSITIVE_INFINITY)
                && !run.damageCore(core.id(), -1) && !run.damageCore("unknown", 1), "Invalid core damage was accepted");
        check(run.damageCore(core.id(), core.maximumHealth() / 2) && core.alive() && run.sites().get(1).progress() == 0,
                "A partially damaged core counted as destroyed");
        check(run.damageCore(core.id(), Float.MAX_VALUE) && !run.damageCore(core.id(), Float.MAX_VALUE), "Physical core death was missing or double-counted");
        check(run.damageCore(run.cores().get(1).id(), Float.MAX_VALUE) && run.complete() && run.consumeRewards() == 2,
                "Destroyed active cores failed to finish their stage once");
        pass(helper, "liberty_mission_courier_core_gate");
    }

    @GameTest(template = "hoverboard_empty", timeoutTicks = 100)
    public static void sweepCountsUniqueLocalOwnedDeathsAndOptionalBasesNeverGateExtraction(GameTestHelper helper) {
        var run = fixture(false, List.of(ObjectiveKind.SWEEP), ObjectiveKind.DEMOLITION);
        var sweep = run.sites().get(0);
        UUID first = UUID.randomUUID();
        check(!run.defeated(null, sweep.marker()) && !run.defeated(first, INVALID)
                && !run.defeated(first, sweep.marker().add(50, 0, 0)) && run.defeated(first, sweep.marker())
                && !run.defeated(first, sweep.marker()), "A non-local, invalid or repeated actor death manufactured a sweep kill");
        check(run.defeated(UUID.randomUUID(), sweep.marker()) && run.complete() && !run.sites().get(1).complete()
                && run.consumeRewards() == 1, "An unfinished optional stronghold blocked primary extraction");
        for (var core : run.cores()) check(run.damageCore(core.id(), Float.MAX_VALUE), "An optional base stopped being available after primary completion");
        check(run.complete() && run.sites().get(1).complete() && run.consumeRewards() == 1 && run.consumeRewards() == 0,
                "Optional base rewards repeated or altered primary completion");
        pass(helper, "liberty_mission_optional_and_owned_sweep");
    }

    @GameTest(template = "hoverboard_empty", timeoutTicks = 100)
    public static void escortMovesThroughActualRouteOnlyWithNearbyOwnerAndClearPath(GameTestHelper helper) {
        var run = fixture(false, List.of(ObjectiveKind.ESCORT), null);
        var rig = run.sites().get(0);
        Vec3 initial = rig.marker();
        run.tick(initial, initial, false, p -> 0);
        check(rig.marker().equals(initial) && run.interact(initial), "Payload moved before it was powered up");
        run.tick(initial.add(30, 0, 0), initial, false, p -> 0);
        run.tick(initial, initial, false, p -> 1);
        check(rig.marker().equals(initial) && rig.progress() == 0, "An abandoned or contested escort moved");
        run.tick(initial, initial, false, p -> 0);
        check(rig.marker().distanceTo(initial) > 0 && rig.marker().distanceTo(initial) <= .161 && rig.progress() == 0,
                "Escort progress was a fake timer or teleported directly to a checkpoint");
        int ticks = 1;
        while (!run.complete() && ticks++ < 1000) {
            Vec3 near = rig.marker();
            run.tick(near, near, false, p -> 0);
        }
        check(run.complete() && ticks > 200 && rig.progress() == 3 && rig.marker().distanceTo(stand(rig.checkpoints().get(2))) < .001
                && run.consumeRewards() == 1, "The escorted marker did not physically traverse and complete all waypoints");
        pass(helper, "liberty_mission_moving_escort");
    }

    @GameTest(template = "hoverboard_empty", timeoutTicks = 100)
    public static void homeTerrainRelocationMovesWholeUntouchedSiteOnceWithoutWorldAccess(GameTestHelper helper) {
        var run = fixture(true, List.of(ObjectiveKind.DEMOLITION), ObjectiveKind.COURIER);
        var base = run.sites().get(0);
        BlockPos oldCore = run.cores().get(0).position();
        check(Math.abs(base.center().getX()) <= 96 && Math.abs(base.center().getZ()) <= 96, "Home site escaped compact coordinates");
        check(run.relocateSite(base.id(), base.center().above(12)) && run.cores().get(0).position().equals(oldCore.above(12))
                && !run.relocateSite(base.id(), base.center().below(12)), "Loaded-ground relocation did not move the core or repeated later");
        var courier = run.sites().get(1);
        var before = courier.checkpoints();
        check(run.relocateSite(courier.id(), courier.center().above(3)) && courier.checkpoints().get(0).equals(before.get(0).above(3)), "Terrain remap failed to move an ordered route");
        var progressed = fixture(true, List.of(ObjectiveKind.COURIER), null);
        var site = progressed.sites().get(0);
        check(!progressed.interact(site.marker()) && progressed.relocateSite(site.id(), site.center())
                && progressed.interact(site.marker()) && !progressed.relocateSite(site.id(), site.center().above(8)),
                "An ungrounded route accepted pickup or a live carried route moved mid-objective");
        var away = fixture(false, List.of(ObjectiveKind.SCAN), null);
        check(!away.relocateSite(away.sites().get(0).id(), BlockPos.ZERO), "Expedition geometry admitted a home-world remap");
        checkHomeProgressRequiresGrounding();
        pass(helper, "liberty_mission_home_ground_remap");
    }

    @GameTest(template = "hoverboard_empty", timeoutTicks = 100)
    public static void oneAreaActionCannotLeapSuccessiveStagesAndSnapshotsStayFinite(GameTestHelper helper) {
        var run = fixture(true, List.of(ObjectiveKind.TERRITORY, ObjectiveKind.TERRITORY), null);
        for (var site : run.sites()) check(run.relocateSite(site.id(), site.center()),
                "The explicit flat home-terrain fixture could not ground its sites");
        check(run.paintArea(INVALID, 20) == 0 && run.paintArea(Vec3.ZERO, Double.NaN) == 0 && run.paintArea(Vec3.ZERO, 129) == 0,
                "An invalid or unbounded call-in area painted turf");
        Vec3 center = new Vec3(0, 65, 0);
        check(run.paintArea(center, 128, cell -> false) == 0 && run.sites().get(0).progress() == 0 && run.stage() == 0,
                "An area with no successfully applied surface effect credited objective turf");
        BlockPos appliedCell = run.sites().get(0).center();
        check(run.paintArea(center, 128, appliedCell::equals) == 1 && run.sites().get(0).progress() == 1 && run.stage() == 0,
                "A partial loaded effect credited more than its one actually applied cell");
        check(run.paintArea(center, 128) == 3 && run.stage() == 1 && run.sites().get(1).progress() == 0,
                "One area action completed more than one successive primary stage");
        run.tick(INVALID, null, true, p -> 0);
        check(!run.interact(INVALID) && run.snapshot(INVALID).getCompound(0).getDouble("Distance") == -1,
                "Invalid positions entered objective state or a non-finite snapshot");
        check(run.paintArea(center, 128) == 4 && run.complete() && run.consumeRewards() == 2 && run.consumeRewards() == 0,
                "A second genuine call-in failed to complete the now-active stage once");
        pass(helper, "liberty_mission_stage_atomic_area");
    }

    private static void checkHomeProgressRequiresGrounding() {
        for (ObjectiveKind kind : ObjectiveKind.values()) {
            var run = fixture(true, List.of(kind), kind);
            for (var site : run.sites()) {
                Vec3 provisional = site.marker();
                check(!site.grounded() && !site.active() && !run.interact(provisional)
                                && !run.paint(site.center(), Direction.UP, provisional)
                                && run.paintArea(provisional, 12) == 0
                                && !run.defeated(UUID.randomUUID(), provisional),
                        "An unresolved home objective accepted an action at provisional coordinates: " + kind);
                for (var core : run.cores()) if (core.siteId().equals(site.id()))
                    check(!run.damageCore(core.id(), Float.MAX_VALUE) && core.health() == core.maximumHealth(),
                            "An unresolved home demolition objective accepted core damage");
                // Cross the manager's full terrain-retry interval using ordinary public ticks.
                for (int tick = 0; tick < 120; tick++)
                    run.tick(provisional, provisional.add(.1, 0, 0), true, point -> 0);
                check(site.progress() == 0 && !site.activated() && site.marker().equals(provisional),
                        "An unresolved home objective accumulated time, route movement or progress: " + kind);
            }
            check(run.completedPrimary() == 0 && !run.complete() && run.consumeRewards() == 0,
                    "Unresolved home objectives manufactured completion or rewards: " + kind);
            // Explicit flat terrain is sufficient for this state-machine regression; the separate
            // native terrain GameTests exercise the actual world resolver and escort clearance.
            for (var site : run.sites()) check(run.relocateSite(site.id(), site.center().above(3)) && site.grounded(),
                    "A rejected provisional action made the later terrain remap impossible: " + kind);
            for (var site : run.sites()) {
                Vec3 at = site.marker();
                switch (kind) {
                    case TERRITORY -> check(run.paintArea(at, 12) == site.goal(), "Grounded home turf could not be claimed");
                    case RELAY_HOLD -> skate(run, at, site.goal() * 20, 0);
                    case DEFEND -> {
                        for (int tick = 0; tick < site.goal() * 20; tick++) run.tick(at, at, false, point -> 0);
                    }
                    case SCAN -> {
                        check(run.interact(at), "A grounded home scan could not activate");
                        for (int tick = 0; tick < site.goal() * 20; tick++) run.tick(at, at, false, point -> 0);
                    }
                    case COURIER -> {
                        check(run.interact(at), "A grounded home courier could not accept pickup");
                        for (BlockPos checkpoint : site.checkpoints()) {
                            Vec3 waypoint = stand(checkpoint);
                            run.tick(waypoint, waypoint.add(.1, 0, 0), true, point -> 0);
                        }
                    }
                    case ESCORT -> {
                        check(run.interact(at), "A grounded home escort could not activate");
                        for (int tick = 0; tick < 1000 && !site.complete(); tick++) {
                            Vec3 near = site.marker(); run.tick(near, near, false, point -> 0);
                        }
                    }
                    case DEMOLITION -> {
                        for (var core : run.cores()) if (core.siteId().equals(site.id()))
                            check(run.damageCore(core.id(), Float.MAX_VALUE), "A grounded home core rejected its actual damage");
                    }
                    case SWEEP -> {
                        for (int defeat = 0; defeat < site.goal(); defeat++)
                            check(run.defeated(UUID.randomUUID(), at), "A grounded home sweep rejected an owned local defeat");
                    }
                }
                check(site.complete(), "Grounding failed to restore normal home objective progress: " + kind);
            }
            check(run.complete() && run.completedPrimary() == 1 && run.consumeRewards() == 2 && run.consumeRewards() == 0,
                    "Grounded primary/optional home objectives did not settle exactly once: " + kind);
        }
    }

    @GameTest(template = "hoverboard_empty", timeoutTicks = 100)
    public static void priorityHuntRequiresTheActualAssignedActorAndNeverAnOrdinaryQuotaKill(GameTestHelper helper) {
        var plan = LibertyMissionPlan.generate(405, "neon_docks", LibertyMissionCatalog.Faction.AUTOMATON, "headliner_hunt", 1, 20);
        var run = new LibertyMissionRuntime(plan, new BlockPos(0, 64, 0), false);
        var boss = run.sites().stream().filter(LibertyMissionRuntime.Site::priorityTarget).findFirst().orElseThrow();
        UUID actor = UUID.randomUUID();
        check(!run.assignPriorityTarget(boss.id(), actor) && !run.defeated(actor, boss.marker()), "A future priority stage accepted a target or a death");
        for (var scan : run.sites()) if (!scan.optional() && scan.spec().stage() == 0) {
            check(run.interact(scan.marker()), "Tracking terminal could not be activated");
            for (int i = 0; i < scan.goal() * 20; i++) run.tick(scan.marker(), scan.marker(), false, p -> 0);
        }
        check(run.stage() == 1 && boss.goal() == 1 && !run.defeated(UUID.randomUUID(), boss.marker())
                && !run.assignPriorityTarget(boss.id(), null) && run.assignPriorityTarget(boss.id(), actor)
                && actor.equals(boss.priorityTargetId()), "Priority target assignment was missing or ordinary kills qualified");
        check(!run.defeated(actor, new Vec3(10_000, 65, 10_000)) && !run.defeated(UUID.randomUUID(), boss.marker())
                && !run.paint(boss.center(), Direction.UP, boss.marker()), "Remote kills, a different actor or graffiti completed the hunt");
        check(run.defeated(actor, new Vec3(0, 65, 0)) && boss.complete() && run.stage() == 2 && !run.complete()
                && !run.defeated(actor, boss.marker()) && run.consumeRewards() == 3 && run.consumeRewards() == 0,
                "The actual assigned target chased across its mission failed to count exactly once");
        pass(helper, "liberty_mission_actual_priority_target");
    }

    private static LibertyMissionRuntime fixture(boolean home, List<ObjectiveKind> primary, ObjectiveKind optional) {
        List<LibertyMissionPlan.Stage> stages = new ArrayList<>();
        List<LibertyMissionPlan.Objective> extras = new ArrayList<>();
        List<LibertyMissionPlan.Stronghold> bases = new ArrayList<>();
        for (int i = 0; i < primary.size(); i++) {
            var objective = objective("fixture_" + i, i, primary.get(i), new LibertyMissionPlan.Point(-300 + i * 150, 0), false);
            stages.add(new LibertyMissionPlan.Stage(i, "Fixture stage " + i, List.of(objective)));
            addBase(bases, objective);
        }
        if (optional != null) {
            var objective = objective("optional_fixture", -1, optional, new LibertyMissionPlan.Point(300, 0), true);
            extras.add(objective); addBase(bases, objective);
        }
        var plan = new LibertyMissionPlan(1, 1234, "neon_docks", LibertyMissionCatalog.Faction.AUTOMATON,
                LibertyMissionCatalog.template("turf_liberation"), LibertyDifficulty.of(1), 20, 512, stages, extras, bases,
                new LibertyMissionPlan.Point(0, 450), new LibertyMissionPlan.Point(0, -450));
        return new LibertyMissionRuntime(plan, new BlockPos(0, 64, 0), home);
    }
    private static LibertyMissionPlan.Objective objective(String id, int stage, ObjectiveKind kind, LibertyMissionPlan.Point center, boolean optional) {
        boolean route = kind == ObjectiveKind.COURIER || kind == ObjectiveKind.ESCORT;
        List<LibertyMissionPlan.Point> checkpoints = route ? List.of(center.offset(12, 0), center.offset(0, 12), center.offset(-12, 0)) : List.of();
        int target = route ? 3 : kind == ObjectiveKind.TERRITORY ? 4 : kind == ObjectiveKind.SWEEP || kind == ObjectiveKind.DEMOLITION ? 2 : 1;
        return new LibertyMissionPlan.Objective(id, stage, kind, "Fixture", "Test actual actions", center, target, 2, 12, checkpoints, optional);
    }
    private static void addBase(List<LibertyMissionPlan.Stronghold> bases, LibertyMissionPlan.Objective objective) {
        if (objective.kind() == ObjectiveKind.DEMOLITION) bases.add(new LibertyMissionPlan.Stronghold("base_" + objective.id(),
                LibertyMissionCatalog.BaseKind.FABRICATOR, objective.center(), 1, objective.target(), objective.id(), !objective.optional(), 10));
    }
    private static void skate(LibertyMissionRuntime run, Vec3 center, int ticks, int enemies) {
        for (int i = 0; i < ticks; i++) {
            Vec3 at = center.add(i % 2, 0, 0), previous = center.add((i + 1) % 2, 0, 0);
            run.tick(at, previous, true, p -> enemies);
        }
    }
    private static Vec3 stand(BlockPos ground) { return Vec3.atCenterOf(ground).add(0, .5, 0); }
    private static void rejected(Runnable action) { try { action.run(); } catch (IllegalArgumentException expected) { return; } throw new GameTestAssertException("Invalid mission input was accepted"); }
    private static void check(boolean condition, String message) { if (!condition) throw new GameTestAssertException(message); }
    private static void pass(GameTestHelper helper, String name) { JetSetCraft.LOGGER.info("[JetSetCraft][GameTest] PASS {}", name); helper.succeed(); }
}
