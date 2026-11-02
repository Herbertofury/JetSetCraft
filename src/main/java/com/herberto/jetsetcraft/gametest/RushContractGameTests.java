package com.herberto.jetsetcraft.gametest;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.herberto.jetsetcraft.JetSetCraft;
import com.herberto.jetsetcraft.data.JetSetDataProvider;
import com.herberto.jetsetcraft.rush.RivalRushManager;
import com.herberto.jetsetcraft.rush.RushArenaCatalog;
import com.herberto.jetsetcraft.rush.RushDirector;
import com.herberto.jetsetcraft.rush.RushReturnState;
import com.herberto.jetsetcraft.rush.RushRun;
import com.herberto.jetsetcraft.rush.RushWorld;
import com.mojang.authlib.GameProfile;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestAssertException;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.level.TicketType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.util.FakePlayerFactory;
import net.minecraftforge.event.entity.EntityTeleportEvent;
import net.minecraftforge.gametest.GameTestHolder;
import net.minecraftforge.gametest.PrefixGameTestTemplate;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

/** Production contract hooks. Position segments, XP-pause setup and compressed clocks are explicit fixtures. */
@GameTestHolder(JetSetCraft.MOD_ID)
@PrefixGameTestTemplate(false)
public final class RushContractGameTests {
    private static final TicketType<UUID> TICKET = TicketType.create("jetsetcraft_contract_test", UUID::compareTo, 900);

    @GameTest(template = "hoverboard_empty", timeoutTicks = 100)
    public static void contractProfilesAreBoundedOptionalAndDetached(GameTestHelper helper) {
        RushArenaCatalog.Profile legacy = profile("{}");
        check(legacy.contract().type() == RushArenaCatalog.ContractType.NONE && legacy.contract().target() == 0
                        && legacy.seconds() == 120 && legacy.defeats() == 8,
                "An old arena without contract configuration changed its required objective or gained a side contract");
        for (String type : List.of("relay", "paint", "hunt")) {
            RushArenaCatalog.Profile parsed = profile("{\"contract\":{\"type\":\"" + type + "\",\"target\":3}}");
            CompoundTag contract = parsed.summary().getCompound("Contract");
            check(contract.getString("Type").equals(type) && contract.getInt("Target") == 3
                            && contract.getString("State").equals("preview") && !contract.getBoolean("Completed")
                            && contract.getInt("Progress") == 0 && !contract.getString("Reward").isBlank()
                            && parsed.seconds() == legacy.seconds() && parsed.defeats() == legacy.defeats(),
                    "The route preview omitted its bounded optional goal or changed the required room objective");
            contract.putInt("Progress", 99); contract.putBoolean("Completed", true);
            check(parsed.summary().getCompound("Contract").getInt("Progress") == 0
                            && !parsed.summary().getCompound("Contract").getBoolean("Completed"),
                    "A detached preview mutation granted authoritative contract progress");
        }
        for (String malformed : List.of(
                "{\"type\":\"relay\",\"target\":1}", "{\"type\":\"relay\",\"target\":5}",
                "{\"type\":\"paint\",\"target\":9}", "{\"type\":\"hunt\",\"target\":5}",
                "{\"type\":\"none\",\"target\":1}", "{\"type\":\"paint\",\"target\":1.5}",
                "{\"type\":\"unknown\",\"target\":1}", "{\"type\":\"paint\",\"payout\":20}")) {
            boolean rejected = false;
            try { profile("{\"contract\":" + malformed + "}"); } catch (RuntimeException expected) { rejected = true; }
            check(rejected, "An unbounded or unknown contract configuration was accepted: " + malformed);
        }
        Set<RushArenaCatalog.ContractType> supplied = RushArenaCatalog.all().stream()
                .map(p -> p.contract().type()).collect(Collectors.toSet());
        check(supplied.containsAll(Set.of(RushArenaCatalog.ContractType.RELAY, RushArenaCatalog.ContractType.PAINT,
                        RushArenaCatalog.ContractType.HUNT)), "The normal supplied catalogue does not offer all three contract types");
        pass(helper, "rush_contract_catalogue");
    }

    @GameTest(template = "hoverboard_empty", timeoutTicks = 600)
    public static void relayUsesOrderedRidingSegmentsAndRejectsPauseTeleportAndUnsafeSpace(GameTestHelper helper) {
        Fixture fixture = begin(helper, "JSC_relay", "copper_current");
        helper.runAfterDelay(2, () -> {
            try {
                ServerPlayer player = fixture.player; RushRun run = RivalRushManager.run(player);
                CompoundTag first = contract(player);
                check(first.getString("Type").equals("relay") && first.getInt("Target") == 3
                                && first.getString("State").equals("active") && first.getInt("Progress") == 0,
                        "The actual generated relay did not expose its first safe ordered beacon");
                Vec3 beacon = beacon(first);
                // Direct fixture jumps are deliberately adversarial observations, never claimed human traversal.
                sample(fixture, beacon); sample(fixture, beacon);
                check(contract(player).getInt("Progress") == 0, "A greater-than-four-block discontinuity claimed a relay point");
                Vec3 outside = beacon.add(3.5, 0, 0);
                sample(fixture, outside);
                MinecraftForge.EVENT_BUS.post(new EntityTeleportEvent.TeleportCommand(player, beacon.x, beacon.y, beacon.z));
                sample(fixture, beacon);
                check(contract(player).getInt("Progress") == 0,
                        "A standard Forge teleport event did not discard its short apparent riding segment");
                EntityTeleportEvent.TeleportCommand cancelled = new EntityTeleportEvent.TeleportCommand(player, outside.x, outside.y, outside.z);
                cancelled.setCanceled(true); MinecraftForge.EVENT_BUS.post(cancelled);
                sample(fixture, beacon);
                check(contract(player).getInt("Progress") == 0, "A cancelled teleport manufactured a relay claim");

                sample(fixture, outside);
                run.gainXp(run.xpToNextLevel());
                check(run.hasOffer(), "The explicit XP fixture did not open a real paused draft");
                long pausedClock = run.elapsedTicks();
                sample(fixture, beacon);
                check(run.elapsedTicks() == pausedClock && contract(player).getInt("Progress") == 0,
                        "Paused repositioning consumed active time or advanced the relay");
                chooseDrafts(player); sample(fixture, beacon);
                check(contract(player).getInt("Progress") == 0, "Resuming a draft reused a paused movement segment");
                // A draft can also open and close between two manager ticks; its action still resets the baseline.
                sample(fixture, outside); run.gainXp(run.xpToNextLevel());
                move(player, beacon); chooseDrafts(player); sample(fixture, beacon);
                check(contract(player).getInt("Progress") == 0, "A same-tick draft choice carried an old relay segment across the pause");

                for (int checkpoint = 0; checkpoint < 3; checkpoint++) {
                    CompoundTag next = contract(player);
                    check(next.getInt("Checkpoint") == checkpoint + 1, "The relay skipped its required checkpoint order");
                    // Real manager samples observe <=1.25-block steps along the protected crossing lanes.
                    rideTo(fixture, Vec3.atBottomCenterOf(fixture.center.above()));
                    rideTo(fixture, beacon(next));
                    check(contract(player).getInt("Progress") == checkpoint + 1,
                            "An observed ordered riding approach did not claim exactly one beacon");
                }
                check(contract(player).getBoolean("Completed") && run.pitStopCredits() == 0
                                && RivalRushManager.journeySnapshot(player).getString("Phase").equals("arena")
                                && RivalRushManager.journeySnapshot(player).getInt("Completed") == 0,
                        "A relay goal paid a service or completed the required arena early");
                returnNormally(fixture);
            } finally { cleanup(fixture); }

            Fixture obstructed = begin(helper, "JSC_bad_relay", "copper_current");
            try {
                CompoundTag next = contract(obstructed.player);
                BlockPos point = new BlockPos(next.getInt("X"), next.getInt("Y"), next.getInt("Z"));
                obstructed.district.setBlock(point, Blocks.STONE.defaultBlockState(), 2);
                sample(obstructed, obstructed.player.position());
                CompoundTag disabled = contract(obstructed.player);
                check(disabled.getString("State").equals("disabled") && !disabled.getString("Reason").isBlank()
                                && !disabled.getBoolean("Completed") && disabled.getInt("Progress") == 0
                                && obstructed.district.getBlockState(point).is(Blocks.STONE)
                                && RivalRushManager.journeySnapshot(obstructed.player).getString("Phase").equals("arena"),
                        "An unsafe relay overwrote authored space, faked completion or removed the ordinary room objective");
                returnNormally(obstructed); pass(helper, "rush_contract_relay");
            } finally { cleanup(obstructed); }
        });
    }

    @GameTest(template = "hoverboard_empty", timeoutTicks = 500)
    public static void acceptedBoundaryPaintCountsOnceAndBeforeItsEarnedDraft(GameTestHelper helper) {
        Fixture fixture = begin(helper, "JSC_paint", "signal_yard");
        helper.runAfterDelay(2, () -> {
            try {
                ServerPlayer player = fixture.player; RushRun run = RivalRushManager.run(player);
                check(contract(player).getString("Type").equals("paint") && contract(player).getInt("Target") == 3,
                        "The supplied Signal Yard did not start its PAINT3 contract");
                int radius = RivalRushManager.currentArenaProfile(player).radius();
                BlockPos outside = fixture.center.offset(radius + 1, 1, -16);
                fixture.district.setBlock(outside, Blocks.STONE.defaultBlockState(), 2);
                move(player, Vec3.atBottomCenterOf(outside.west(2)));
                check(RivalRushManager.tagWall(player, outside, Direction.WEST) && contract(player).getInt("Progress") == 0,
                        "An accepted ordinary patch outside the room's exact column boundary earned contract credit");
                int[] offsets = {-12, -8, 8};
                for (int patch = 0; patch < offsets.length; patch++) {
                    chooseDrafts(player);
                    BlockPos wall = fixture.center.offset(radius, 1, offsets[patch]);
                    check(!fixture.district.getBlockState(wall).isAir(), "The actual authored east perimeter wall was not built");
                    move(player, Vec3.atBottomCenterOf(wall.west(2)));
                    check(RivalRushManager.tagWall(player, wall, Direction.WEST)
                                    && contract(player).getInt("Progress") == patch + 1,
                            "A genuine fresh east boundary-wall patch failed to advance its exact goal once");
                    check(!RivalRushManager.tagWall(player, wall, Direction.WEST)
                                    && contract(player).getInt("Progress") == patch + 1,
                            "A repeated wall patch replayed its paint goal");
                    if (run.hasOffer()) {
                        BlockPos other = fixture.center.offset(radius, 1, 16);
                        move(player, Vec3.atBottomCenterOf(other.west(2)));
                        check(!RivalRushManager.tagWall(player, other, Direction.WEST)
                                        && contract(player).getInt("Progress") == patch + 1,
                                "A paused draft accepted a new wall patch");
                    }
                }
                CompoundTag completed = contract(player);
                check(completed.getBoolean("Completed") && completed.getString("State").equals("complete")
                                && run.pitStopCredits() == 0 && run.contractClears() == 0
                                && RivalRushManager.journeySnapshot(player).getInt("Completed") == 0,
                        "Optional paint completion paid or cleared the required room before actual settlement");
                completed.putInt("Progress", 99); completed.putBoolean("Completed", false);
                check(contract(player).getInt("Progress") == 3 && contract(player).getBoolean("Completed"),
                        "A returned progress snapshot modified the authoritative goal");
                returnNormally(fixture); pass(helper, "rush_contract_paint");
            } finally { cleanup(fixture); }
        });
    }

    @GameTest(template = "hoverboard_empty", timeoutTicks = 600)
    public static void huntCountsOnlyAcceptedOwnedCaptainAndEliteDefeats(GameTestHelper helper) {
        Fixture fixture = begin(helper, "JSC_hunt", "needlepoint_crossing");
        helper.runAfterDelay(2, () -> {
            try {
                ServerPlayer player = fixture.player; RushRun run = RivalRushManager.run(player);
                check(contract(player).getString("Type").equals("hunt") && contract(player).getInt("Target") == 2,
                        "The actual hunt room lost its two-elite-or-captain goal");
                // Only elapsed time is compressed; every credited defeat below resolves a real owned native body.
                advance(run, Math.max(0, 3001 - run.elapsedTicks())); RivalRushManager.tick(player.getServer());
                Mob ordinary = actors(player).stream().filter(mob -> !captain(mob) && !elite(mob)).findFirst().orElseThrow();
                defeat(player, ordinary);
                check(contract(player).getInt("Progress") == 0, "An ordinary rival was incorrectly treated as a hunt elite");
                Mob captain = actors(player).stream().filter(RushContractGameTests::captain).findFirst().orElseThrow();
                ServerPlayer foreign = FakePlayerFactory.get(helper.getLevel(), new GameProfile(UUID.randomUUID(), "JSC_hunt_other"));
                double vitality = RivalRushManager.actorVitality(player, captain); float nativeHealth = captain.getHealth();
                check(!RivalRushManager.artifactHit(foreign, captain, 40)
                                && RivalRushManager.actorVitality(player, captain) == vitality && contract(player).getInt("Progress") == 0,
                        "A foreign player consumed another run's body or claimed its hunt progress");
                defeat(player, captain);
                check(contract(player).getInt("Progress") == 1 && captain.getHealth() == nativeHealth
                                && !RivalRushManager.artifactHit(player, captain, 40) && contract(player).getInt("Progress") == 1,
                        "A real captain defeat did not count once, or it consumed native health/replayed after removal");
                Mob removed = nextElite(player);
                removed.discard(); RivalRushManager.tick(player.getServer());
                check(contract(player).getInt("Progress") == 1, "Discarding an owned elite falsely credited a defeat");
                Mob defeated = nextElite(player); defeat(player, defeated);
                check(contract(player).getInt("Progress") == 2 && contract(player).getBoolean("Completed")
                                && run.defeats() == 3 && run.pitStopCredits() == 0 && run.contractClears() == 0
                                && RivalRushManager.journeySnapshot(player).getString("Phase").equals("arena"),
                        "A real elite did not finish the bounded hunt, or optional completion bypassed the required defeat quota");
                returnNormally(fixture); pass(helper, "rush_contract_hunt");
            } finally { cleanup(fixture); }
        });
    }

    private static final class Fixture {
        final ServerPlayer player;
        final ServerLevel source, district;
        final Vec3 returnPosition;
        final ListTag inventory;
        final CompoundTag ride;
        final BlockPos center;
        final List<ChunkPos> held = new ArrayList<>();
        Fixture(ServerPlayer player, ServerLevel source, Vec3 position, ListTag inventory, CompoundTag ride) {
            this.player = player; this.source = source; this.returnPosition = position; this.inventory = inventory; this.ride = ride;
            district = player.serverLevel(); center = RivalRushManager.arenaDestination(player);
        }
    }

    private static Fixture begin(GameTestHelper helper, String name, String arena) {
        ServerPlayer player = FakePlayerFactory.get(helper.getLevel(), new GameProfile(UUID.randomUUID(), name));
        player.setGameMode(GameType.SURVIVAL); player.getInventory().clearContent(); player.containerMenu.setCarried(ItemStack.EMPTY);
        move(player, Vec3.atBottomCenterOf(helper.absolutePos(new BlockPos(1, 2, 1))));
        ItemStack ordinary = new ItemStack(Items.EMERALD, 7); ordinary.getOrCreateTag().putString("ContractReturn", "original");
        player.getInventory().setItem(0, ordinary); player.experienceLevel = 12; player.totalExperience = 93; player.experienceProgress = .25f;
        Vec3 position = player.position(); ListTag inventory = player.getInventory().save(new ListTag());
        CompoundTag ride = player.getCapability(JetSetDataProvider.CAPABILITY).resolve().orElseThrow().save();
        check(RivalRushManager.startCircuit(player, id("jetsetcraft:dead_beat"), id("minecraft:zombie"), false,
                id("jetsetcraft:" + arena)), "Could not begin the actual selected contract arena");
        Fixture fixture = new Fixture(player, helper.getLevel(), position, inventory, ride);
        try {
            hold(fixture, player.blockPosition()); hold(fixture, fixture.center);
            // A disposable fixture arrival lets the actual bounded builder and manager admit this selected room.
            move(player, Vec3.atBottomCenterOf(fixture.center.above())); fixture.district.getChunkSource().move(player);
            for (int step = 0; step < 128; step++) RivalRushManager.tick(player.getServer());
            check(RivalRushManager.journeySnapshot(player).getString("Phase").equals("arena"),
                    "The real arena builder did not admit the fixture arrival");
            return fixture;
        } catch (RuntimeException error) { cleanup(fixture); throw error; }
    }
    private static void hold(Fixture fixture, BlockPos position) {
        ChunkPos chunk = new ChunkPos(position); fixture.held.add(chunk);
        fixture.district.getChunkSource().addRegionTicket(TICKET, chunk, 4, fixture.player.getUUID());
        for (int x = -2; x <= 2; x++) for (int z = -2; z <= 2; z++) fixture.district.getChunk(chunk.x + x, chunk.z + z);
    }
    private static void sample(Fixture fixture, Vec3 position) {
        // Rival removal keeps this movement fixture focused; removal must never supply defeat/contract credit.
        actors(fixture.player).forEach(Mob::discard);
        move(fixture.player, position); RivalRushManager.tick(fixture.player.getServer());
    }
    private static void rideTo(Fixture fixture, Vec3 target) {
        chooseDrafts(fixture.player);
        for (int step = 0; step < 100 && fixture.player.position().distanceToSqr(target) > .000001; step++) {
            Vec3 delta = target.subtract(fixture.player.position());
            sample(fixture, delta.length() <= 1.25 ? target : fixture.player.position().add(delta.normalize().scale(1.25)));
        }
        check(fixture.player.position().distanceToSqr(target) < .000001, "The bounded observed riding path did not reach its fixture endpoint");
    }
    private static void move(ServerPlayer player, Vec3 position) {
        player.moveTo(position.x, position.y, position.z, 0, 0); player.setOnGround(true); player.setDeltaMovement(Vec3.ZERO);
    }
    private static Vec3 beacon(CompoundTag tag) { return Vec3.atBottomCenterOf(new BlockPos(tag.getInt("X"), tag.getInt("Y"), tag.getInt("Z"))); }
    private static CompoundTag contract(ServerPlayer player) { return RivalRushManager.contractSnapshot(player); }
    private static List<Mob> actors(ServerPlayer player) {
        return player.serverLevel().getEntitiesOfClass(Mob.class, player.getBoundingBox().inflate(100), mob -> RivalRushManager.belongsTo(mob, player));
    }
    private static boolean captain(Mob mob) { return mob.getName().getString().contains("captain"); }
    private static boolean elite(Mob mob) {
        for (RushDirector.Elite variant : RushDirector.Elite.values())
            if (variant != RushDirector.Elite.NONE && mob.getName().getString().contains(variant.label())) return true;
        return false;
    }
    private static Mob nextElite(ServerPlayer player) {
        RushRun run = RivalRushManager.run(player);
        for (int wave = 0; wave < 30; wave++) {
            for (Mob mob : actors(player)) if (elite(mob)) return mob;
            actors(player).forEach(Mob::discard); chooseDrafts(player); advance(run, 40); RivalRushManager.tick(player.getServer());
        }
        throw new GameTestAssertException("The bounded real director did not spawn an owned elite for its hunt fixture");
    }
    private static void defeat(ServerPlayer player, Mob mob) {
        chooseDrafts(player); mob.moveTo(player.getX() + 5, player.getY(), player.getZ(), 0, 0); mob.setDeltaMovement(Vec3.ZERO);
        for (int hit = 0; hit < 200 && RivalRushManager.actorVitality(player, mob) > 0; hit++) {
            chooseDrafts(player); check(RivalRushManager.artifactHit(player, mob, 40), "A valid owned native body rejected the contract fixture's virtual hit");
        }
        check(mob.isRemoved(), "Bounded actual virtual hits did not defeat the owned native body"); chooseDrafts(player);
    }
    private static void chooseDrafts(ServerPlayer player) {
        RushRun run = RivalRushManager.run(player);
        for (int choice = 0; run != null && run.hasOffer() && choice < 100; choice++) {
            int index = -1;
            for (int i = 0; i < run.offers().size(); i++) if (!run.offers().get(i).artifact()) { index = i; break; }
            if (index < 0) for (int i = 0; i < run.offers().size(); i++)
                if (run.offers().get(i) != RushRun.Upgrade.EXPOSE && run.offers().get(i) != RushRun.Upgrade.WARD) { index = i; break; }
            check(index >= 0, "The genuine earned draft had no safe fixture choice");
            RivalRushManager.handleAction(player, "choose", "", index, run.id(), run.offerVersion());
        }
        check(run != null && !run.hasOffer(), "Could not resolve the genuine contract fixture draft");
    }
    private static void advance(RushRun run, long ticks) { for (long tick = 0; tick < ticks; tick++) run.tickActive(); }
    private static RushArenaCatalog.Profile profile(String json) {
        JsonObject object = JsonParser.parseString(json).getAsJsonObject();
        return RushArenaCatalog.parse(id("jetsetcraft:contract_fixture"), object);
    }
    private static void returnNormally(Fixture fixture) {
        ServerPlayer player = fixture.player;
        check(RivalRushManager.leave(player, "Contract acceptance return") && player.serverLevel() == fixture.source
                        && player.position().distanceToSqr(fixture.returnPosition) < .0001
                        && fixture.inventory.equals(player.getInventory().save(new ListTag()))
                        && fixture.ride.equals(player.getCapability(JetSetDataProvider.CAPABILITY).resolve().orElseThrow().save())
                        && player.experienceLevel == 12 && player.totalExperience == 93 && player.experienceProgress == .25f
                        && !RushReturnState.hasJournal(player),
                "Contract return changed normal inventory NBT, physical ride gear, XP, position or recovery journal");
    }
    private static void cleanup(Fixture fixture) {
        try {
            if (RivalRushManager.active(fixture.player) || RushReturnState.hasJournal(fixture.player))
                check(RivalRushManager.leave(fixture.player, "Contract GameTest cleanup"), "Contract fixture could not restore its owner");
        } finally {
            for (ChunkPos chunk : fixture.held) fixture.district.getChunkSource().removeRegionTicket(TICKET, chunk, 4, fixture.player.getUUID());
            fixture.held.clear();
        }
    }
    private static ResourceLocation id(String value) { return ResourceLocation.tryParse(value); }
    private static void check(boolean condition, String message) { if (!condition) throw new GameTestAssertException(message); }
    private static void pass(GameTestHelper helper, String marker) { System.out.println("JETSETCRAFT_GAMETEST_PASS " + marker); helper.succeed(); }
    private RushContractGameTests() { }
}
