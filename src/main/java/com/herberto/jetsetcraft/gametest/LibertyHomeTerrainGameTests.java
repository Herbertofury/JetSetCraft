package com.herberto.jetsetcraft.gametest;

import com.herberto.jetsetcraft.JetSetCraft;
import com.herberto.jetsetcraft.liberty.LibertyDifficulty;
import com.herberto.jetsetcraft.liberty.LibertyHomeTerrain;
import com.herberto.jetsetcraft.liberty.LibertyMissionCatalog;
import com.herberto.jetsetcraft.liberty.LibertyMissionPlan;
import com.herberto.jetsetcraft.liberty.LibertyMissionRuntime;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestAssertException;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.gametest.GameTestHolder;
import net.minecraftforge.gametest.PrefixGameTestTemplate;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

/** Real loaded terrain exercises the same route resolver and clearance predicate used by home deployment. */
@GameTestHolder(JetSetCraft.MOD_ID)
@PrefixGameTestTemplate(false)
public final class LibertyHomeTerrainGameTests {
    @GameTest(template = "hoverboard_empty", timeoutTicks = 100)
    public static void defenseOffersChooseVariedFactionAppropriatePlansAndKeepThemOnRetry(GameTestHelper helper) {
        for (var faction : LibertyMissionCatalog.factions()) {
            Set<String> families = new HashSet<>();
            for (int i = 0; i < 96; i++) {
                UUID offer = new UUID(0x53b774115L, i);
                var plan = LibertyMissionPlan.defense(offer, "neon_docks", faction, 5, 25);
                check(plan.equals(LibertyMissionPlan.defense(offer, "neon_docks", faction, 5, 25)),
                        "Retrying one defense offer rerolled its mission or geography");
                check(plan.template().supports(faction) && plan.primaryObjectives().stream()
                                .anyMatch(goal -> goal.kind() == LibertyMissionCatalog.ObjectiveKind.DEFEND),
                        "A defense offer selected an unsupported faction or a mission without a defended objective");
                for (int minutes : new int[]{20, 25, 30})
                    check(plan.template().id().equals(LibertyMissionPlan.defense(offer, "neon_docks", faction, 10, minutes).template().id()),
                            "Changing difficulty or time budget rerolled the defense offer's mission family");
                families.add(plan.template().id());
            }
            check(families.size() >= 6, "Distinct defense offers collapsed into one repeated mission family");
        }
        pass(helper, "liberty_defense_offer_variety");
    }

    @GameTest(template = "hoverboard_empty", timeoutTicks = 100)
    public static void homeCourierUsesEachRealWaypointElevationWithoutChangingTerrain(GameTestHelper helper) {
        ServerLevel level = helper.getLevel(); BlockPos origin = new BlockPos(52064, 180, 52064);
        BlockPos absent = new BlockPos(7_200_000, 180, 7_200_000);
        check(level.getChunkSource().getChunkNow(absent.getX() >> 4, absent.getZ() >> 4) == null
                        && LibertyHomeTerrain.resolve(level, route(absent, LibertyMissionCatalog.ObjectiveKind.COURIER).sites().get(0)) == null
                        && level.getChunkSource().getChunkNow(absent.getX() >> 4, absent.getZ() >> 4) == null,
                "Home route admission generated or loaded an absent chunk");
        terrain(level, origin);
        var run = route(origin, LibertyMissionCatalog.ObjectiveKind.COURIER);
        var site = run.sites().get(0);
        BlockPos oldFirst = site.checkpoints().get(0);
        check(!run.interact(site.marker()), "An ungrounded home courier accepted a pickup at its old flat coordinates");
        Map<BlockPos, BlockState> before = snapshot(level, origin);
        var ground = LibertyHomeTerrain.resolve(level, site);
        check(ground != null && ground.checkpoints().get(0).getY() - ground.center().getY() == 8
                        && ground.checkpoints().get(1).getY() == origin.getY() && run.relocateSite(site.id(), ground),
                "Actual hill and low-ground waypoints were flattened to the depot elevation");
        check(run.interact(site.marker()) && !run.relocateSite(site.id(), ground), "A grounded route could not start or was relocated after pickup");
        Vec3 obsolete = stand(oldFirst);
        run.tick(obsolete, obsolete.add(.1, 0, 0), true, point -> 0);
        check(site.progress() == 0, "The obsolete underground checkpoint still counted instead of the real hilltop");
        for (BlockPos checkpoint : site.checkpoints()) {
            Vec3 position = stand(checkpoint);
            run.tick(position, position.add(.1, 0, 0), true, point -> 0);
        }
        check(run.complete() && site.progress() == 3 && run.consumeRewards() == 1 && run.consumeRewards() == 0,
                "Ordered skating at the actual terrain elevations did not complete the home delivery once");
        check(before.equals(snapshot(level, origin)), "Home route admission or progress changed ordinary terrain");
        pass(helper, "liberty_home_courier_elevations");
    }

    @GameTest(template = "hoverboard_empty", timeoutTicks = 100)
    public static void homeEscortFollowsGroundDetoursAndPausesForNewObstacles(GameTestHelper helper) {
        ServerLevel level = helper.getLevel(); BlockPos origin = new BlockPos(52464, 180, 52464);
        terrain(level, origin);
        for (int z = -1; z <= 1; z++) for (int y = 4; y <= 7; y++)
            level.setBlock(origin.offset(3, y, z), Blocks.STONE.defaultBlockState(), 3);
        var run = route(origin, LibertyMissionCatalog.ObjectiveKind.ESCORT); var site = run.sites().get(0);
        Map<BlockPos, BlockState> before = snapshot(level, origin);
        var ground = LibertyHomeTerrain.resolve(level, site);
        check(ground != null && ground.escortPaths().get(0).stream().anyMatch(point -> Math.abs(point.getZ() - origin.getZ()) >= 2)
                        && run.relocateSite(site.id(), ground) && run.interact(site.marker()),
                "A walkable hill route was rejected or its rig path crossed the solid obstacle");
        Vec3 start = site.marker(); BlockPos temporaryBlock = BlockPos.containing(start);
        BlockState original = level.getBlockState(temporaryBlock);
        level.setBlock(temporaryBlock, Blocks.STONE.defaultBlockState(), 3);
        run.tick(start, start, false, point -> 0, feet -> LibertyHomeTerrain.clear(level, feet));
        check(site.marker().equals(start) && site.progress() == 0, "A live home escort moved through a newly placed solid block");
        level.setBlock(temporaryBlock, original, 3);
        double high = start.y; int steps = 0;
        while (!run.complete() && steps++ < 4000) {
            Vec3 near = site.marker();
            run.tick(near, near, false, point -> 0, feet -> LibertyHomeTerrain.clear(level, feet));
            check(LibertyHomeTerrain.clear(level, site.marker()) && site.marker().distanceTo(near) <= .05501,
                    "Terrain-following escort clipped a block or teleported between route points");
            high = Math.max(high, site.marker().y);
        }
        check(run.complete() && high >= origin.getY() + 9 && site.progress() == 3 && run.consumeRewards() == 1,
                "The actual ground route did not ascend, descend and finish all escorted waypoints");
        check(before.equals(snapshot(level, origin)), "Escort navigation excavated, built on or otherwise changed the home terrain");
        // An impassable cliff spans the bounded route corridor; admission must fail without editing it.
        for (int z = -12; z <= 12; z++) for (int y = 5; y <= 20; y++)
            level.setBlock(origin.offset(4, y, z), Blocks.STONE.defaultBlockState(), 3);
        var blocked = route(origin, LibertyMissionCatalog.ObjectiveKind.ESCORT);
        Map<BlockPos, BlockState> cliff = snapshot(level, origin);
        check(LibertyHomeTerrain.resolve(level, blocked.sites().get(0)) == null && cliff.equals(snapshot(level, origin)),
                "An impossible home escort route was admitted or terrain was changed to manufacture a route");
        pass(helper, "liberty_home_escort_ground_path");
    }

    private static LibertyMissionRuntime route(BlockPos origin, LibertyMissionCatalog.ObjectiveKind kind) {
        var point = new LibertyMissionPlan.Point(0, 0);
        var objective = new LibertyMissionPlan.Objective("home_route", 0, kind, "Home route", "Actual terrain route",
                point, 3, 0, 24, List.of(point.offset(44, 0), point.offset(0, 44), point.offset(-44, 0)), false);
        var plan = new LibertyMissionPlan(1, 123, "neon_docks", LibertyMissionCatalog.Faction.TERMINID,
                LibertyMissionCatalog.template("evacuation_rush"), LibertyDifficulty.of(1), 20, 512,
                List.of(new LibertyMissionPlan.Stage(0, "Route", List.of(objective))), List.of(), List.of(),
                point.offset(0, 450), point.offset(0, -450));
        return new LibertyMissionRuntime(plan, origin, true);
    }
    private static void terrain(ServerLevel level, BlockPos origin) {
        // Only the explicit disposable fixture requests chunks or writes blocks; the production resolver does neither.
        for (int x = -1; x <= 1; x++) for (int z = -1; z <= 1; z++)
            level.getChunk((origin.getX() >> 4) + x, (origin.getZ() >> 4) + z);
        for (int x = -12; x <= 12; x++) for (int z = -12; z <= 12; z++) {
            int rise = Math.max(0, Math.min(8, x));
            for (int y = 0; y <= 22; y++) level.setBlock(origin.offset(x, y, z),
                    (y <= rise ? Blocks.STONE : Blocks.AIR).defaultBlockState(), 3);
        }
    }
    private static Map<BlockPos, BlockState> snapshot(ServerLevel level, BlockPos origin) {
        Map<BlockPos, BlockState> result = new HashMap<>();
        for (int x = -12; x <= 12; x++) for (int z = -12; z <= 12; z++) for (int y = 0; y <= 22; y++) {
            BlockPos at = origin.offset(x, y, z); result.put(at, level.getBlockState(at));
        }
        return result;
    }
    private static Vec3 stand(BlockPos ground) { return Vec3.atCenterOf(ground).add(0, .5, 0); }
    private static void check(boolean condition, String message) { if (!condition) throw new GameTestAssertException(message); }
    private static void pass(GameTestHelper helper, String marker) { JetSetCraft.LOGGER.info("JETSETCRAFT_LIBERTY_HOME_TERRAIN_PASS {}", marker); helper.succeed(); }
    private LibertyHomeTerrainGameTests() { }
}
