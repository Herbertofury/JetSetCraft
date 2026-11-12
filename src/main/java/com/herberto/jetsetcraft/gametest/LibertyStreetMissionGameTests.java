package com.herberto.jetsetcraft.gametest;

import com.herberto.jetsetcraft.JetSetCraft;
import com.herberto.jetsetcraft.liberty.LibertyMissionCatalog;
import com.herberto.jetsetcraft.liberty.LibertyMissionPlan;
import com.herberto.jetsetcraft.liberty.LibertyMissionRuntime;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestAssertException;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.gametest.GameTestHolder;
import net.minecraftforge.gametest.PrefixGameTestTemplate;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

/** Exercise each new authored operation through the same staged actions used by the world manager. */
@GameTestHolder(JetSetCraft.MOD_ID)
@PrefixGameTestTemplate(false)
public final class LibertyStreetMissionGameTests {
    private static final List<String> OPERATIONS = List.of("neon_block_party", "last_train_home", "pirate_radio_takeover",
            "supply_line_shuffle", "swarm_street_cleanup", "prism_breakout");

    @GameTest(template = "hoverboard_empty", timeoutTicks = 120)
    public static void originalStreetOperationsCompleteInEitherOrderWithoutOptionalGates(GameTestHelper helper) {
        int completed = 0;
        for (String id : OPERATIONS) {
            var template = LibertyMissionCatalog.template(id);
            check(template != null, "Missing original street operation " + id);
            for (boolean reverse : new boolean[]{false, true}) {
                var faction = template.factions().stream().sorted().findFirst().orElseThrow();
                var plan = LibertyMissionPlan.generate(209L, "neon_docks", faction, id, 4, 20);
                var run = new LibertyMissionRuntime(plan, new BlockPos(0, 64, 0), false);
                check(!run.complete() && run.consumeRewards() == 0, "An operation paid or completed before gameplay");
                for (int stage = 0; stage < plan.stages().size(); stage++) {
                    final int current = stage;
                    List<LibertyMissionRuntime.Site> sites = new ArrayList<>(run.sites().stream()
                            .filter(site -> !site.optional() && site.spec().stage() == current).toList());
                    if (reverse) Collections.reverse(sites);
                    check(!sites.isEmpty() && run.stage() == stage, "The operation skipped or stalled a stage");
                    for (int i = 0; i < sites.size(); i++) {
                        check(run.stage() == stage, "A parallel objective was skipped");
                        complete(run, sites.get(i));
                        check(sites.get(i).complete(), "An authored goal cannot finish through its real action: " + sites.get(i).id());
                        check(run.stage() == (i + 1 == sites.size() ? stage + 1 : stage),
                                "Parallel objectives did not gate their next stage together");
                    }
                }
                check(run.complete() && run.completedPrimary() == run.totalPrimary(), "Not every primary objective was earned");
                check(run.sites().stream().filter(LibertyMissionRuntime.Site::optional).noneMatch(LibertyMissionRuntime.Site::complete),
                        "Primary actions accidentally completed optional sites");
                check(run.consumeRewards() == run.totalPrimary() && run.consumeRewards() == 0,
                        "Operation objective rewards were lost or repeated");
                for (var site : run.sites().stream().filter(site -> !site.optional()).toList()) {
                    check(!run.interact(site.marker()), "Completed operation accepted another terminal activation");
                    check(run.paintArea(site.marker(), 24) == 0, "Completed operation accepted another turf reward");
                }
                check(run.consumeRewards() == 0, "Repeated completed actions manufactured rewards");
                completed++;
            }
        }
        check(completed == 12, "The operation/order matrix is incomplete");
        JetSetCraft.LOGGER.info("JETSETCRAFT_GAMETEST_PASS liberty_street_operations operations=6 orders=2 completed={}", completed);
        helper.succeed();
    }

    private static void complete(LibertyMissionRuntime run, LibertyMissionRuntime.Site site) {
        switch (site.kind()) {
            case TERRITORY -> {
                check(run.paintArea(site.marker(), site.radius() + 4) > 0, "Real area paint failed at an active turf site");
            }
            case DEMOLITION -> {
                for (var core : run.cores()) if (core.siteId().equals(site.id())) {
                    check(run.damageCore(core.id(), Float.MAX_VALUE), "Active physical core rejected damage");
                    check(!run.damageCore(core.id(), Float.MAX_VALUE), "The same destroyed core counted twice");
                }
            }
            case SWEEP -> {
                for (int i = 0; i < site.goal(); i++) {
                    UUID actor = UUID.randomUUID();
                    check(run.defeated(actor, site.marker()) && !run.defeated(actor, site.marker()),
                            "Distinct local owned deaths were rejected or repeated");
                }
            }
            case COURIER -> {
                check(run.interact(site.marker()), "An active courier pickup could not be collected");
                for (BlockPos checkpoint : site.checkpoints()) {
                    Vec3 position = Vec3.atBottomCenterOf(checkpoint).add(0, 1, 0);
                    run.tick(position, position.add(.35, 0, 0), true, ignored -> 0);
                }
            }
            case ESCORT -> {
                check(run.interact(site.marker()), "An active escort could not be started");
                int ticks = 0;
                while (!site.complete() && ++ticks <= 20_000) {
                    Vec3 position = site.marker();
                    run.tick(position, position, false, ignored -> 0);
                }
                check(ticks > 1 && ticks <= 20_000, "The real moving escort stalled or teleported to completion");
            }
            case SCAN, RELAY_HOLD, DEFEND -> {
                if (site.kind() == LibertyMissionCatalog.ObjectiveKind.SCAN)
                    check(run.interact(site.marker()), "An active scan could not be started");
                Vec3 position = site.marker();
                for (int tick = 0; tick < site.goal() * 20; tick++)
                    run.tick(position, position.add(.35, 0, 0), true, ignored -> 0);
            }
        }
    }

    private static void check(boolean condition, String message) { if (!condition) throw new GameTestAssertException(message); }
    private LibertyStreetMissionGameTests() {}
}
