package com.herberto.jetsetcraft.gametest;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.herberto.jetsetcraft.JetSetCraft;
import com.herberto.jetsetcraft.crew.CrewHouseData;
import com.herberto.jetsetcraft.crew.CrewHouseManager;
import com.herberto.jetsetcraft.data.JetSetDataProvider;
import com.herberto.jetsetcraft.rush.RivalRushManager;
import com.herberto.jetsetcraft.rush.RushArenaCatalog;
import com.herberto.jetsetcraft.rush.RushCircuit;
import com.herberto.jetsetcraft.rush.RushPointsOfInterest;
import com.herberto.jetsetcraft.rush.RushReturnState;
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
import net.minecraft.world.entity.Mob;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.common.util.FakePlayerFactory;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.gametest.GameTestHolder;
import net.minecraftforge.gametest.PrefixGameTestTemplate;

import java.util.HashSet;
import java.util.UUID;
import java.util.function.Consumer;

/** Production arena acceptance. Clock compression is confined to fixtures, never the real-client audit. */
@GameTestHolder(JetSetCraft.MOD_ID)
@PrefixGameTestTemplate(false)
public final class RushArenaGameTests {
    private static final ResourceLocation GANG = id("jetsetcraft:dead_beat");
    private static final ResourceLocation ZOMBIE = id("minecraft:zombie");

    @GameTest(template = "hoverboard_empty", timeoutTicks = 100)
    public static void arenaCatalogueRejectsUnsafeAndUnboundedMaps(GameTestHelper helper) {
        var profiles = RushArenaCatalog.all();
        check(profiles.size() >= 6 && profiles.size() <= 32, "Bundled six-arena catalogue did not load within its bound");
        check(profiles.stream().map(RushArenaCatalog.Profile::reward).distinct().count() == 6,
                "The bundled catalogue does not offer all six reward families");
        if (!ModList.get().isLoaded("aether")) check(RushArenaCatalog.find(id("jetsetcraft:cloudline_circuit")).isEmpty(),
                "An absent optional provider still exposed its gated arena");
        JsonObject valid = JsonParser.parseString("""
                {"name":"Bounded fixture","radius":16,"seconds":30,"defeats":3,"layout":"cross",
                 "reward":"area","shared_reward":"training","reward_ranks":1,
                 "palette":{"floor":["jetsetcraft:missing_fixture_material","minecraft:smooth_stone"]},
                 "blocks":[{"x":8,"y":1,"z":8,"block":"minecraft:stone"}]}
                """).getAsJsonObject();
        var parsed = RushArenaCatalog.parse(id("jetsetcraft:fixture"), valid);
        check(parsed.floor().is(Blocks.SMOOTH_STONE) && parsed.blocks().size() == 1,
                "A valid palette fallback or bounded authored map did not parse");
        rejects(valid, json -> json.addProperty("radius", 49), "Oversized radius");
        rejects(valid, json -> json.addProperty("seconds", 361), "Unbounded room duration");
        rejects(valid, json -> json.addProperty("reward_ranks", 3), "Inflated cache reward");
        rejects(valid, json -> json.addProperty("radius", 24.5), "Fractional dimensions");
        rejects(valid, json -> block(json).addProperty("x", 0), "Blocked central travel lane");
        rejects(valid, json -> block(json).addProperty("x", 13), "Out-of-bounds authored block");
        rejects(valid, json -> block(json).addProperty("nbt", "forged inventory"), "Authored block-entity data");
        rejects(valid, json -> block(json).addProperty("block", "minecraft:chest"), "Container block");
        rejects(valid, json -> block(json).addProperty("block", "minecraft:water"), "Fluid block");
        rejects(valid, json -> block(json).addProperty("block", "minecraft:tnt"), "Explosive block");
        rejects(valid, json -> {
            JsonArray blocks = new JsonArray();
            for (int i = 0; i < 4097; i++) blocks.add(block(json).deepCopy());
            json.add("blocks", blocks);
        }, "Excessive authored block count");
        pass(helper, "rush_arena_catalogue");
    }

    @GameTest(template = "hoverboard_empty", timeoutTicks = 200)
    public static void sixRoomRoutesAndArtifactSlotsKeepEarnedChoicesAuthoritative(GameTestHelper helper) {
        verifyArtifactDraftRefresh();
        RushRun run = new RushRun(UUID.randomUUID(), UUID.randomUUID(), 884L, false, GANG.toString());
        run.configureCircuit(); run.grantReward(RushRun.Upgrade.FORCE);
        // Pure run clock fixture proves circuits are governed by rooms, not the free-street 30-minute deadline.
        for (long tick = 0; tick <= RushRun.SESSION_TICKS; tick++) run.tickActive();
        check(!run.awaitingEncore(), "A finite circuit inherited the unrelated free-street deadline");
        ServerLevel district = helper.getLevel().getServer().getLevel(RushWorld.DIMENSION);
        check(district != null, "Rush dimension is absent");
        RushCircuit route = new RushCircuit(run.id(), run.seed(), new BlockPos(-2_500_000, RushWorld.FLOOR_Y, 3_000_000), 0);
        var receipts = new HashSet<UUID>();
        for (int room = 0; room < 6; room++) {
            check(route.choices().size() == 3 && route.choices().stream().map(RushArenaCatalog.Profile::reward).distinct().count() == 3,
                    "A route did not provide three distinct reward families");
            var choice = route.choices().get(0); int version = route.version();
            check(!route.choose(choice.id(), version - 1) && !route.choose(id("jetsetcraft:forged_arena"), version),
                    "A stale or unoffered route was accepted");
            check(route.choose(choice.id(), version) && !route.choose(choice.id(), version), "One offered route could be selected twice");
            // Only this isolated fixture's nearby chunks are loaded; production builders never request tickets.
            loadChunks(district, route.center(), 1);
            for (int step = 0; step < 160 && !route.builder().readyForEntry(); step++) route.builder().step(district);
            check(route.builder().readyForEntry() && route.arrive(run, Vec3.atCenterOf(route.center()).add(0, .5, 0)),
                    "A constructed central arrival zone did not admit its route");
            check(!route.complete(run), "An unplayed room paid a completion");
            for (int tick = 1; tick < choice.seconds() * 20; tick++) run.tickActive();
            for (int defeat = 0; defeat < choice.defeats(); defeat++) run.recordDefeat();
            check(!route.complete(run), "Defeats bypassed the room's remaining active time");
            run.tickActive();
            check(receipts.add(route.rewardId()), "Two completed rooms shared a reward receipt");
            check(route.complete(run) && !route.complete(run) && route.completed() == room + 1,
                    "Room completion was lost or repeated");
            check(route.snapshot(run, Vec3.atCenterOf(route.center())).getList("History", Tag.TAG_COMPOUND).size() == room + 1,
                    "Route history omitted or invented a completed room");
        }
        check(route.phase() == RushCircuit.Phase.COMPLETE && run.awaitingEncore(), "Six completed rooms did not offer an explicit Encore");
        long elapsed = run.elapsedTicks(); run.tickActive();
        check(run.elapsedTicks() == elapsed, "The set-end decision did not pause the circuit clock");
        check(run.continueEndless() && route.encore() && !route.encore(), "Encore was not a one-time route transition");
        CompoundTag nextSet = route.snapshot(run, Vec3.atCenterOf(route.origin()));
        check(route.set() == 2 && route.completed() == 0 && nextSet.getList("History", Tag.TAG_COMPOUND).isEmpty()
                        && run.rank(RushRun.Upgrade.FORCE) == 1 && run.elapsedTicks() == elapsed && run.endless(),
                "Encore lost the build, clock or fresh six-room route");
        pass(helper, "rush_arena_rules");
    }

    private static void verifyArtifactDraftRefresh() {
        RushRun build = new RushRun(UUID.randomUUID(), UUID.randomUUID(), 91L, false, GANG.toString());
        build.gainXp(24); int oldVersion = build.offerVersion();
        check(build.hasOffer(), "The XP fixture did not earn a genuine draft");
        check(build.grantReward(RushRun.Upgrade.CHAIN) && build.grantReward(RushRun.Upgrade.FIREWORK)
                        && build.grantReward(RushRun.Upgrade.WIND), "The first three artifact families could not be earned");
        check(build.artifactCount() == 3 && !build.canAdvance(RushRun.Upgrade.SOUL)
                        && !build.grantReward(RushRun.Upgrade.SOUL), "A fourth artifact family bypassed the slot limit");
        int beforeCache = build.offerVersion();
        RushPointsOfInterest.grant(build, RushArenaCatalog.Reward.RELIC, 1);
        check(build.hasOffer() && build.offerVersion() != beforeCache && !build.choose(0, oldVersion),
                "A cache failed to refresh the pending XP draft or accepted its old version");
        check(build.offers().stream().allMatch(upgrade -> !upgrade.artifact() || build.rank(upgrade) > 0),
                "A refreshed draft offered a fourth artifact family");
        check(build.choose(0, build.offerVersion()), "The refreshed earned draft could not be claimed");
        for (int reward = 0; reward < 40; reward++) RushPointsOfInterest.grant(build, RushArenaCatalog.Reward.RELIC, 1);
        check(build.artifactCount() == 3 && build.score() > 0, "Full artifact caches stopped rewarding play or exceeded the slot limit");
        for (RushRun.Upgrade upgrade : RushRun.Upgrade.values()) if (upgrade.artifact())
            check(build.rank(upgrade) <= upgrade.maxRank(), "Repeated artifact caches exceeded a rank cap");
    }

    @GameTest(template = "hoverboard_empty", timeoutTicks = 600)
    public static void realArenaTravelCombatSettlementAndReturnPreserveMinecraft(GameTestHelper helper) {
        ServerPlayer player = FakePlayerFactory.get(helper.getLevel(), new GameProfile(UUID.randomUUID(), "JSC_arena"));
        player.setGameMode(GameType.SURVIVAL); player.getInventory().clearContent(); player.containerMenu.setCarried(ItemStack.EMPTY);
        BlockPos source = helper.absolutePos(new BlockPos(1, 2, 1));
        player.moveTo(source.getX() + .5, source.getY(), source.getZ() + .5, 0, 0);
        ItemStack diamonds = new ItemStack(Items.DIAMOND, 7); diamonds.getOrCreateTag().putString("ArenaReturnProof", "original belongings");
        player.getInventory().setItem(0, diamonds); player.getInventory().setItem(8, new ItemStack(Items.BREAD, 5));
        player.getInventory().selected = 8;
        player.experienceLevel = 23; player.totalExperience = 587; player.experienceProgress = .625F;
        ListTag inventory = player.getInventory().save(new ListTag());
        CompoundTag ride = player.getCapability(JetSetDataProvider.CAPABILITY).resolve().orElseThrow().save();
        Vec3 sourcePosition = player.position();
        try {
            check(RivalRushManager.startCircuit(player, GANG, ZOMBIE, false, null), "Real circuit entry failed");
            RushRun run = RivalRushManager.run(player);
            CompoundTag journey = RivalRushManager.journeySnapshot(player);
            ListTag choices = journey.getList("Choices", Tag.TAG_COMPOUND);
            check(RushWorld.isRush(player.level()) && RushReturnState.hasJournal(player) && choices.size() == 3,
                    "Circuit entry lost its separate dimension, return journal or genuine route offers");
            String selected = choices.getCompound(0).getString("Id"); int version = journey.getInt("Version");
            Vec3 departure = player.position();
            RivalRushManager.handleAction(player, "journey_choose", selected, 0, UUID.randomUUID(), version);
            RivalRushManager.handleAction(player, "journey_choose", selected, 0, run.id(), version - 1);
            check(RivalRushManager.journeySnapshot(player).getString("Phase").equals("choosing"),
                    "A foreign or stale route action changed the session");
            RivalRushManager.handleAction(player, "journey_choose", selected, 0, run.id(), version);
            check(RivalRushManager.journeySnapshot(player).getString("Phase").equals("travel")
                            && player.position().distanceToSqr(departure) < .0001,
                    "Selecting an arena teleported the rider or failed to open travel");
            var profile = RushArenaCatalog.find(id(selected)).orElseThrow();
            BlockPos destination = RivalRushManager.arenaDestination(player);
            check(destination != null && Vec3.atCenterOf(destination).distanceToSqr(departure) > 64 * 64,
                    "The offered arena did not create a distinct destination");
            // A bounded fake-player fixture supplies chunks along the real connection. No production tickets are added.
            loadChunks(player.serverLevel(), BlockPos.containing(departure), 2);
            loadChunks(player.serverLevel(), BlockPos.containing(departure.lerp(Vec3.atCenterOf(destination), .5)), 2);
            loadChunks(player.serverLevel(), destination, 2);
            helper.runAfterDelay(2, () -> enterGeneratedArena(helper, player, departure, destination, profile,
                    inventory, ride, sourcePosition));
        } catch (RuntimeException error) { cleanup(player); throw error; }
    }

    private static void enterGeneratedArena(GameTestHelper helper, ServerPlayer player, Vec3 departure, BlockPos destination,
                                             RushArenaCatalog.Profile profile, ListTag inventory, CompoundTag ride, Vec3 sourcePosition) {
        try {
            for (int step = 0; step < 96; step++) RivalRushManager.tick(player.getServer());
            Vec3 arrival = new Vec3(destination.getX() + .5, RushWorld.FLOOR_Y + 1, destination.getZ() + .5);
            // Advance the test actor in short position steps over generated blocks. This is a geometry/manager
            // fixture, not a claim that the visual audit completed a room or recorded human skating controls.
            for (int point = 1; point <= 16; point++) {
                Vec3 position = departure.lerp(arrival, point / 16.0);
                BlockPos floor = BlockPos.containing(position).below();
                var material = player.serverLevel().getBlockState(floor);
                check((material.equals(profile.floor()) || material.equals(profile.accent()))
                                && player.serverLevel().getBlockState(floor.above()).isAir()
                                && player.serverLevel().getBlockState(floor.above(2)).isAir(),
                        "Generated arena connection has missing floor or obstructed rider headroom at " + floor);
                player.moveTo(position.x, position.y, position.z, 0, 0); player.setDeltaMovement(Vec3.ZERO);
                RivalRushManager.tick(player.getServer());
            }
            check(RivalRushManager.journeySnapshot(player).getString("Phase").equals("arena"),
                    "Actual arrival in the constructed room did not start its objective");
            RushRun run = RivalRushManager.run(player);
            // Compress only this run's active clock. Defeats below still come from real director-owned actors.
            for (int tick = 0; tick < profile.seconds() * 20; tick++) run.tickActive();
            RivalRushManager.tick(player.getServer());
            helper.runAfterDelay(2, () -> fightRoom(helper, player, profile, inventory, ride, sourcePosition, 0));
        } catch (RuntimeException error) { cleanup(player); throw error; }
    }

    private static void fightRoom(GameTestHelper helper, ServerPlayer player, RushArenaCatalog.Profile profile,
                                  ListTag inventory, CompoundTag ride, Vec3 sourcePosition, int wave) {
        try {
            RushRun run = RivalRushManager.run(player);
            check(run != null, "Arena fixture ended before owned rival combat");
            for (int choice = 0; choice < 40 && run.hasOffer(); choice++)
                RivalRushManager.handleAction(player, "choose", run.offers().get(0).name(), 0, run.id(), run.offerVersion());
            var actors = player.serverLevel().getEntitiesOfClass(Mob.class, player.getBoundingBox().inflate(80),
                    mob -> RivalRushManager.belongsTo(mob, player));
            for (Mob mob : actors) for (int hit = 0; hit < 16 && !mob.isRemoved() && !run.hasOffer(); hit++)
                RivalRushManager.tagEnemy(player, mob, 40);
            RivalRushManager.tick(player.getServer());
            CompoundTag journey = RivalRushManager.journeySnapshot(player);
            if (journey.getInt("Completed") != 1) {
                check(wave < 80, "Arena combat exhausted its bounded fixture: defeats=" + run.defeats()
                        + ", actors=" + actors.size() + ", phase=" + journey.getString("Phase"));
                for (int tick = 0; tick < 36; tick++) run.tickActive();
                RivalRushManager.tick(player.getServer());
                helper.runAfterDelay(2, () -> fightRoom(helper, player, profile, inventory, ride, sourcePosition, wave + 1));
                return;
            }
            check(run.defeats() >= profile.defeats() && journey.getString("Phase").equals("choosing")
                            && journey.getList("History", Tag.TAG_COMPOUND).size() == 1,
                    "Room completion did not come from its real objective or open the next route");
            CrewHouseData data = CrewHouseData.get(player.getServer());
            int supplies = data.pendingSupplies(player.getUUID()); long xp = data.pendingXp(player.getUUID());
            check(supplies > 0 && xp > 0 && data.pendingTraining(player.getUUID()) > 0
                            && data.pendingReputation(player.getUUID(), GANG.toString()) > 0,
                    "A completed arena did not bank shared progression before a house was founded");
            UUID receipt = pendingReceipt(data, player.getUUID());
            String mode = profile.sharedReward().equals("balanced") ? "rush_arena" : "rush_arena_" + profile.sharedReward();
            check(!CrewHouseManager.bankActivity(player, receipt, GANG, mode, profile.seconds() * 20L, profile.defeats(), run.score()).accepted()
                            && data.pendingSupplies(player.getUUID()) == supplies && data.pendingXp(player.getUUID()) == xp,
                    "An actual room receipt paid twice");
            check(RivalRushManager.leave(player, "Arena acceptance return"), "Arena return failed");
            check(!RivalRushManager.active(player) && !RushReturnState.hasJournal(player) && player.serverLevel() == helper.getLevel()
                            && player.position().distanceToSqr(sourcePosition) < .0001
                            && inventory.equals(player.getInventory().save(new ListTag()))
                            && ride.equals(player.getCapability(JetSetDataProvider.CAPABILITY).resolve().orElseThrow().save())
                            && player.experienceLevel == 23 && player.totalExperience == 587 && player.experienceProgress == .625F,
                    "Arena return failed to restore exact Minecraft position, inventory, ride data or XP");
            check(data.pendingSupplies(player.getUUID()) >= supplies && data.pendingXp(player.getUUID()) >= xp,
                    "Returning from a run erased already banked arena progress");
            pass(helper, "rush_arena_session");
        } catch (RuntimeException error) { cleanup(player); throw error; }
    }

    private static UUID pendingReceipt(CrewHouseData data, UUID owner) {
        ListTag accounts = data.save(new CompoundTag()).getList("PendingProgress", Tag.TAG_COMPOUND);
        for (var value : accounts) if (value instanceof CompoundTag account && account.hasUUID("Owner") && account.getUUID("Owner").equals(owner)) {
            ListTag receipts = account.getList("Receipts", Tag.TAG_COMPOUND);
            check(receipts.size() == 1 && receipts.getCompound(0).hasUUID("Id"), "One completed room did not persist exactly one receipt");
            return receipts.getCompound(0).getUUID("Id");
        }
        throw new GameTestAssertException("Arena's shared pending account was not saved");
    }

    private static void loadChunks(ServerLevel level, BlockPos center, int radius) {
        for (int x = -radius; x <= radius; x++) for (int z = -radius; z <= radius; z++)
            level.getChunk((center.getX() >> 4) + x, (center.getZ() >> 4) + z);
    }
    private static JsonObject block(JsonObject json) { return json.getAsJsonArray("blocks").get(0).getAsJsonObject(); }
    private static void rejects(JsonObject valid, Consumer<JsonObject> mutation, String scenario) {
        JsonObject candidate = valid.deepCopy(); mutation.accept(candidate); boolean rejected = false;
        try { RushArenaCatalog.parse(id("jetsetcraft:rejected_fixture"), candidate); }
        catch (RuntimeException expected) { rejected = true; }
        check(rejected, scenario + " was accepted by the production arena parser");
    }
    private static void cleanup(ServerPlayer player) {
        if (RivalRushManager.active(player) || RushReturnState.hasJournal(player))
            check(RivalRushManager.leave(player, "Arena GameTest cleanup"), "Arena fixture could not restore its player");
    }
    private static ResourceLocation id(String value) { return ResourceLocation.tryParse(value); }
    private static void check(boolean condition, String message) { if (!condition) throw new GameTestAssertException(message); }
    private static void pass(GameTestHelper helper, String marker) { System.out.println("JETSETCRAFT_GAMETEST_PASS " + marker); helper.succeed(); }
    private RushArenaGameTests() {}
}
