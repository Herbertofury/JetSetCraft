package com.herberto.jetsetcraft.gametest;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.herberto.jetsetcraft.JetSetCraft;
import com.herberto.jetsetcraft.rush.RushArenaCatalog;
import com.herberto.jetsetcraft.rush.RushCircuit;
import com.herberto.jetsetcraft.rush.RushPointsOfInterest;
import com.herberto.jetsetcraft.rush.RushRun;
import com.herberto.jetsetcraft.rush.RushWorld;
import com.mojang.authlib.GameProfile;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestAssertException;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.common.util.FakePlayerFactory;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.gametest.GameTestHolder;
import net.minecraftforge.gametest.PrefixGameTestTemplate;

import java.util.HashSet;
import java.util.UUID;

/** Real constructed cache surfaces and production claims; shortened clocks are fixture-only. */
@GameTestHolder(JetSetCraft.MOD_ID)
@PrefixGameTestTemplate(false)
public final class RushCacheGameTests {
    @GameTest(template = "hoverboard_empty", timeoutTicks = 200)
    public static void cacheChoicesDescribeDistinctRewardsAndOnlyChargeOwnedClaims(GameTestHelper helper) {
        Fixture fixture = fixture(helper, -4_250_000, false);
        RushPointsOfInterest points = new RushPointsOfInterest();
        fixture.player.getInventory().setItem(0, new ItemStack(Items.DIAMOND, 3));
        ListTag inventory = fixture.player.getInventory().save(new ListTag());
        pulse(points, fixture);
        CompoundTag preview = points.snapshot(fixture.player, fixture.run, fixture.route);
        ListTag caches = preview.getList("Caches", Tag.TAG_COMPOUND);
        check(caches.size() == 6 && preview.getInt("DefeatsNeeded") == 3 && !preview.getBoolean("Ready"),
                "The first room did not expose six real uncharged cache choices");
        HashSet<Integer> colors = new HashSet<>(); HashSet<String> families = new HashSet<>();
        for (int i = 0; i < caches.size(); i++) {
            CompoundTag cache = caches.getCompound(i); BlockPos pos = position(cache);
            colors.add(cache.getInt("Color")); families.add(cache.getString("Reward"));
            var reward = RushArenaCatalog.Reward.valueOf(cache.getString("Reward").toUpperCase(java.util.Locale.ROOT));
            check(!cache.getString("Label").isBlank() && !cache.getString("Benefit").isBlank()
                            && fixture.level.getBlockState(pos.below()).equals(reward.marker())
                            && fixture.level.getBlockState(pos).isAir() && fixture.level.getBlockState(pos.above()).isAir(),
                    "A cache preview disagreed with its actual safe colored floor marker");
        }
        check(colors.size() == 6 && families.size() == 6, "Distinct cache families lost their visual identities");
        CompoundTag damage = cache(preview, "damage");
        move(fixture.player, position(damage));
        pulse(points, fixture);
        check(fixture.run.rank(RushRun.Upgrade.FORCE) == 0, "An uncharged cache granted power");
        for (int defeat = 0; defeat < 3; defeat++) fixture.run.recordDefeat();
        pulse(points, fixture);
        check(fixture.run.rank(RushRun.Upgrade.FORCE) == 1
                        && cache(points.snapshot(fixture.player, fixture.run, fixture.route), "damage").getInt("CooldownTicks") > 0,
                "A charged physical cache did not grant and cool down its actual run reward");
        points.tick(fixture.player, fixture.run, fixture.route);
        for (int defeat = 0; defeat < 3; defeat++) fixture.run.recordDefeat();
        pulse(points, fixture);
        check(fixture.run.rank(RushRun.Upgrade.FORCE) == 1, "The same cache paid twice before cooldown");
        CompoundTag magnet = cache(points.snapshot(fixture.player, fixture.run, fixture.route), "magnet");
        move(fixture.player, position(magnet)); pulse(points, fixture);
        check(fixture.run.rank(RushRun.Upgrade.PICKUP) == 1 && fixture.run.pickupRadius() == 128
                        && inventory.equals(fixture.player.getInventory().save(new ListTag())),
                "A deliberate magnet choice failed to improve the run or changed ordinary inventory");
        // Mutating a returned map cannot change a real marker, cooldown or family.
        damage.putString("Reward", "relic"); preview.putInt("DefeatsNeeded", 0);
        check(cache(points.snapshot(fixture.player, fixture.run, fixture.route), "damage").getInt("CooldownTicks") > 0,
                "A returned cache preview mutated server state");
        ServerPlayer stranger = FakePlayerFactory.get(fixture.level, new GameProfile(UUID.randomUUID(), "JSC_cache_other"));
        move(stranger, position(magnet));
        check(points.tick(stranger, fixture.run, fixture.route).isEmpty()
                        && points.snapshot(stranger, fixture.run, fixture.route).getList("Caches", Tag.TAG_COMPOUND).isEmpty(),
                "Another owner read or claimed a private run's caches");
        RushRun partial = new RushRun(UUID.randomUUID(), fixture.player.getUUID(), 747, false, "jetsetcraft:dead_beat");
        for (int rank = 0; rank < RushRun.Upgrade.PRECISION.maxRank(); rank++) partial.grantReward(RushRun.Upgrade.PRECISION);
        String earned = RushPointsOfInterest.grant(partial, RushArenaCatalog.Reward.CRITICAL, 1);
        check(partial.rank(RushRun.Upgrade.CRITICAL) == 1 && partial.score() == 250
                        && earned.contains("Finishing touch +1") && earned.contains("+250 score"),
                "A partially mastered critical cache misreported its actual power rank and capped-chance payout");
        pass(helper, "rush_cache_options");
    }

    @GameTest(template = "hoverboard_empty", timeoutTicks = 200)
    public static void authoredObstaclesWallsAndRoomResetCannotManufactureRewards(GameTestHelper helper) {
        Fixture fixture = fixture(helper, -4_500_000, true);
        RushPointsOfInterest points = new RushPointsOfInterest();
        pulse(points, fixture);
        CompoundTag damage = cache(points.snapshot(fixture.player, fixture.run, fixture.route), "damage");
        BlockPos actual = position(damage), authored = fixture.route.center().offset(-8, 1, -8);
        check(!actual.equals(authored) && actual.getY() <= RushWorld.FLOOR_Y + 4
                        && fixture.level.getBlockState(authored).is(Blocks.STONE),
                "A cache replaced an authored pillar or chose its inaccessible high roof");
        for (int defeat = 0; defeat < 15; defeat++) fixture.run.recordDefeat();
        fixture.level.setBlock(actual, Blocks.STONE.defaultBlockState(), 2);
        move(fixture.player, actual.east(2)); pulse(points, fixture);
        check(fixture.run.rank(RushRun.Upgrade.FORCE) == 0, "A charged cache was collected through a solid obstruction");
        fixture.level.setBlock(actual, Blocks.AIR.defaultBlockState(), 2);
        move(fixture.player, actual); pulse(points, fixture);
        check(fixture.run.rank(RushRun.Upgrade.FORCE) == 1, "Clearing the actual approach did not restore cache collection");
        for (int defeat = 0; defeat < 15; defeat++) fixture.run.recordDefeat();
        for (int tick = 0; tick < fixture.route.current().seconds() * 20; tick++) fixture.run.tickActive();
        check(fixture.route.complete(fixture.run), "The first physical arena did not complete in the fixture");
        check(points.snapshot(fixture.player, fixture.run, fixture.route).getList("Caches", Tag.TAG_COMPOUND).isEmpty(),
                "An old room's caches remained claimable in the next-route decision");
        check(fixture.route.choose(fixture.route.choices().get(0).id(), fixture.route.version()), "The next offered room could not be chosen");
        enter(fixture); points.reset(); pulse(points, fixture);
        CompoundTag next = points.snapshot(fixture.player, fixture.run, fixture.route);
        check(next.getInt("DefeatsNeeded") == 3 && !next.getBoolean("Ready"),
                "Leftover defeats from an earlier room charged a fresh room's cache");
        CompoundTag nextDamage = cache(next, "damage"); move(fixture.player, position(nextDamage));
        for (int defeat = 0; defeat < 2; defeat++) fixture.run.recordDefeat();
        pulse(points, fixture);
        check(fixture.run.rank(RushRun.Upgrade.FORCE) == 1
                        && points.snapshot(fixture.player, fixture.run, fixture.route).getInt("DefeatsNeeded") == 1,
                "Fewer than three new-room defeats spent a charge");
        fixture.run.recordDefeat(); pulse(points, fixture);
        check(fixture.run.rank(RushRun.Upgrade.FORCE) == 2, "The new room's three real defeats did not charge its own cache");
        fixture.run.finish();
        long score = fixture.run.score();
        check(points.tick(fixture.player, fixture.run, fixture.route).isEmpty()
                        && RushPointsOfInterest.grant(fixture.run, RushArenaCatalog.Reward.DAMAGE, 1).equals("Reward unavailable")
                        && fixture.run.score() == score, "A finished run retained cache reward authority");
        pass(helper, "rush_cache_boundaries");
    }

    @GameTest(template = "hoverboard_empty", timeoutTicks = 240)
    public static void seededRoutesPreferUnvisitedRoomsAndRespectOptionalProviders(GameTestHelper helper) {
        ServerLevel level = helper.getLevel().getServer().getLevel(RushWorld.DIMENSION);
        check(level != null, "Rush dimension unavailable");
        RushRun run = new RushRun(UUID.randomUUID(), UUID.randomUUID(), 6539, false, "jetsetcraft:dead_beat"); run.configureCircuit();
        RushCircuit route = new RushCircuit(run.id(), run.seed(), new BlockPos(-4_750_000, RushWorld.FLOOR_Y, -4_000_000), 0);
        HashSet<ResourceLocation> visited = new HashSet<>();
        for (int stage = 0; stage < 6; stage++) {
            var choices = route.choices();
            check(choices.size() == 3 && choices.stream().map(RushArenaCatalog.Profile::reward).distinct().count() == 3,
                    "Exploration preference removed a branch or distinct reward family");
            var selected = choices.get(0);
            check(visited.add(selected.id()), "The first branch repeated a room while unvisited bundled rooms were available");
            var snapshot = route.snapshot(run, Vec3.atCenterOf(route.center()));
            check(snapshot.getList("Choices", Tag.TAG_COMPOUND).getCompound(0).getInt("VisitsThisSet") == 0,
                    "A fresh room's preview claimed a previous visit");
            check(route.choose(selected.id(), route.version()), "An actual exploratory branch could not be selected");
            build(level, route);
            check(route.arrive(run, Vec3.atCenterOf(route.center()).add(0, .5, 0)), "Constructed route did not arrive");
            for (int tick = 0; tick < selected.seconds() * 20; tick++) run.tickActive();
            for (int defeat = 0; defeat < selected.defeats(); defeat++) run.recordDefeat();
            check(route.complete(run), "Exploratory room did not complete");
        }
        check(visited.size() == 6, "A bundled six-room journey failed to explore six different rooms");
        String[][] providers = {{"aether", "cloudline_circuit"}, {"irons_spellbooks", "runecast_crossing"},
                {"cataclysm", "embervault_loop"}, {"aoa3", "expedition_steps"}};
        for (String[] provider : providers) {
            var profile = RushArenaCatalog.find(id("jetsetcraft:" + provider[1]));
            check(profile.isPresent() == ModList.get().isLoaded(provider[0]), "A provider-gated arena disagrees with installed mods: " + provider[0]);
            if (profile.isPresent()) check(RushArenaCatalog.safe(profile.get().floor()) && RushArenaCatalog.safe(profile.get().wall()),
                    "An optional arena resolved an unsafe provider material");
        }
        pass(helper, "rush_route_exploration");
    }

    private record Fixture(ServerLevel level, ServerPlayer player, RushRun run, RushCircuit route) {}
    private static Fixture fixture(GameTestHelper helper, int coordinate, boolean obstacle) {
        ServerLevel level = helper.getLevel().getServer().getLevel(RushWorld.DIMENSION);
        check(level != null, "Rush dimension unavailable");
        ServerPlayer player = FakePlayerFactory.get(level, new GameProfile(UUID.randomUUID(), "JSC_cache"));
        RushRun run = new RushRun(UUID.randomUUID(), player.getUUID(), coordinate, false, "jetsetcraft:dead_beat"); run.configureCircuit();
        JsonObject json = JsonParser.parseString("""
                {"name":"Cache fixture","radius":16,"seconds":30,"defeats":3,"layout":"plaza","reward":"damage"}
                """).getAsJsonObject();
        if (obstacle) {
            JsonArray blocks = new JsonArray();
            for (int y = 1; y <= 8; y++) {
                JsonObject block = new JsonObject(); block.addProperty("x", -8); block.addProperty("z", -8);
                block.addProperty("y", y); block.addProperty("block", "minecraft:stone"); blocks.add(block);
            }
            json.add("blocks", blocks);
        }
        RushCircuit route = new RushCircuit(run.id(), run.seed(), new BlockPos(coordinate, RushWorld.FLOOR_Y, -4_000_000), 0);
        route.firstArena(RushArenaCatalog.parse(id("jetsetcraft:cache_fixture"), json));
        Fixture fixture = new Fixture(level, player, run, route); enter(fixture); return fixture;
    }
    private static void enter(Fixture fixture) {
        build(fixture.level, fixture.route);
        move(fixture.player, fixture.route.center().above());
        check(fixture.route.arrive(fixture.run, fixture.player.position()), "Could not enter the actual built cache arena");
    }
    private static void build(ServerLevel level, RushCircuit route) {
        ChunkPos center = new ChunkPos(route.center());
        for (int x = -3; x <= 3; x++) for (int z = -3; z <= 3; z++) level.getChunk(center.x + x, center.z + z);
        for (int step = 0; step < 160; step++) route.builder().step(level);
        check(route.builder().readyForEntry(), "Actual cache arena did not finish its entrance");
    }
    private static void pulse(RushPointsOfInterest points, Fixture fixture) {
        do { fixture.run.tickActive(); } while (fixture.run.elapsedTicks() % 10 != 0);
        points.tick(fixture.player, fixture.run, fixture.route);
    }
    private static void move(ServerPlayer player, BlockPos position) { player.moveTo(position.getX() + .5, position.getY(), position.getZ() + .5, 0, 0); }
    private static BlockPos position(CompoundTag tag) { return new BlockPos(tag.getInt("X"), tag.getInt("Y"), tag.getInt("Z")); }
    private static CompoundTag cache(CompoundTag state, String reward) {
        ListTag caches = state.getList("Caches", Tag.TAG_COMPOUND);
        for (int i = 0; i < caches.size(); i++) if (caches.getCompound(i).getString("Reward").equals(reward)) return caches.getCompound(i);
        throw new GameTestAssertException("No actual cache for " + reward);
    }
    private static ResourceLocation id(String value) { return ResourceLocation.tryParse(value); }
    private static void check(boolean condition, String message) { if (!condition) throw new GameTestAssertException(message); }
    private static void pass(GameTestHelper helper, String marker) { JetSetCraft.LOGGER.info("JETSETCRAFT_GAMETEST_PASS {}", marker); helper.succeed(); }
    private RushCacheGameTests() {}
}
