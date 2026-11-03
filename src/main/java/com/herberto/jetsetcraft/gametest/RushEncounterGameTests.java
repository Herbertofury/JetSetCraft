package com.herberto.jetsetcraft.gametest;

import com.herberto.jetsetcraft.JetSetCraft;
import com.herberto.jetsetcraft.data.JetSetDataProvider;
import com.herberto.jetsetcraft.rush.RivalRushManager;
import com.herberto.jetsetcraft.rush.RushAlbum;
import com.herberto.jetsetcraft.rush.RushDirector;
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
import net.minecraft.server.level.TicketType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.common.util.FakePlayerFactory;
import net.minecraftforge.gametest.GameTestHolder;
import net.minecraftforge.gametest.PrefixGameTestTemplate;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

/** Actual owned encounter bodies and manager settlements; compressed clocks are fixtures, not a balance playtest. */
@GameTestHolder(JetSetCraft.MOD_ID)
@PrefixGameTestTemplate(false)
public final class RushEncounterGameTests {
    private static final TicketType<UUID> TICKET = TicketType.create("jetsetcraft_encounter_test", UUID::compareTo, 900);

    @GameTest(template = "hoverboard_empty", timeoutTicks = 600)
    public static void nativeElitesTelegraphPauseDodgeAndOfferRecovery(GameTestHelper helper) {
        Fixture fixture = begin(helper, "JSC_elites", false, 700);
        helper.runAfterDelay(2, () -> {
            try {
                ServerPlayer player = fixture.player; RushRun run = RivalRushManager.run(player);
                for (int step = 0; step < 30 && eliteNames(player).size() < 3; step++) {
                    advance(run, 40); RivalRushManager.tick(player.getServer());
                }
                check(eliteNames(player).equals(Set.of("Linebreaker", "Bombardier", "Basswarden")),
                        "The real director did not produce all three distinct native elite variants");
                Vec3 base = clearCombatPatch(fixture);
                List<Mob> actors = actors(player); actors.forEach(mob -> place(mob, base, 36, 20));
                Mob linebreaker = actors.stream().filter(mob -> mob.getName().getString().contains("Linebreaker")).findFirst().orElseThrow();
                place(linebreaker, base, 8, 0);
                UUID originalId = linebreaker.getUUID(); var originalType = linebreaker.getType();
                float nativeHealth = linebreaker.getHealth(); double maximum = linebreaker.getAttributeValue(Attributes.MAX_HEALTH);
                RivalRushManager.tick(player.getServer());
                CompoundTag tell = RivalRushManager.encounterSnapshot(player);
                check(tell.getString("Name").equals("Linebreaker") && tell.getString("Attack").equals("Paint lane")
                                && tell.getInt("TicksUntilImpact") > 0 && tell.getInt("TellCount") <= RushDirector.MAX_TELLS
                                && tell.getInt("EliteCount") <= RushDirector.MAX_ELITES,
                        "The owned linebreaker did not expose a bounded, readable attack before impact");
                double vitality = run.vitality(); long active = run.elapsedTicks(); int remaining = tell.getInt("TicksUntilImpact");
                run.gainXp(24); check(run.hasOffer(), "The pause fixture did not earn an actual XP draft");
                for (int i = 0; i < 35; i++) RivalRushManager.tick(player.getServer());
                check(run.elapsedTicks() == active && near(run.vitality(), vitality)
                                && RivalRushManager.encounterSnapshot(player).getInt("TicksUntilImpact") == remaining,
                        "A pending draft advanced or resolved the elite's active-clock tell");
                player.moveTo(base.x, base.y, base.z + 3, 0, 0); player.setDeltaMovement(Vec3.ZERO);
                chooseDrafts(player);
                for (int i = 0; i <= remaining; i++) RivalRushManager.tick(player.getServer());
                check(run.vitality() >= vitality && RivalRushManager.encounterSnapshot(player).getString("Attack").equals("Recovery"),
                        "A locked lane followed a successful sideways dodge, or its recovery window was missing");
                double before = RivalRushManager.actorVitality(player, linebreaker);
                check(RivalRushManager.artifactHit(player, linebreaker, 2)
                                && near(before - RivalRushManager.actorVitality(player, linebreaker), 2 * run.damageMultiplier() * 1.25),
                        "An actual recovery counterattack did not receive its advertised 25 percent virtual bonus");
                check(linebreaker.getUUID().equals(originalId) && linebreaker.getType() == originalType
                                && near(linebreaker.getHealth(), nativeHealth) && near(linebreaker.getAttributeValue(Attributes.MAX_HEALTH), maximum),
                        "An elite replaced its native body or mutated native health/attributes");
                verifyGeometryBounds();
                returnNormally(fixture);
                pass(helper, "rush_elite_tells");
            } finally { cleanup(fixture); }
        });
    }

    @GameTest(template = "hoverboard_empty", timeoutTicks = 500)
    public static void aNewWallBlocksAnAlreadyMarkedTaggerImpact(GameTestHelper helper) {
        Fixture fixture = begin(helper, "JSC_tagger_wall", false, 700);
        helper.runAfterDelay(2, () -> {
            try {
                ServerPlayer player = fixture.player; RushRun run = RivalRushManager.run(player);
                Vec3 base = clearCombatPatch(fixture); List<Mob> actors = actors(player);
                check(!actors.isEmpty(), "Actual tagger fixture failed to spawn");
                actors.forEach(mob -> place(mob, base, 36, 20));
                Mob tagger = actors.stream().filter(mob -> mob.getName().getString().contains("tagger")).findFirst().orElseThrow();
                place(tagger, base, 6, 0);
                double before = run.vitality(); float nativeHealth = player.getHealth();
                RivalRushManager.tick(player.getServer()); // Real target-position mark; no damage at cast time.
                check(near(run.vitality(), before), "Tagger hit before its visible mark could be dodged");
                wall(fixture, base, true);
                run.gainXp(24); chooseDrafts(player);
                before = run.vitality();
                for (int i = 0; i < 18; i++) RivalRushManager.tick(player.getServer());
                check(near(run.vitality(), before), "A tagger's old mark damaged the owner through newly placed solid cover");
                wall(fixture, base, false);
                for (int i = 0; i < 90 && near(run.vitality(), before); i++) RivalRushManager.tick(player.getServer());
                check(run.vitality() < before && near(player.getHealth(), nativeHealth),
                        "The uncovered subsequent mark did not spend only virtual vitality");
                returnNormally(fixture);
                pass(helper, "rush_tagger_cover");
            } finally { cleanup(fixture); }
        });
    }

    @GameTest(template = "hoverboard_empty", timeoutTicks = 500)
    public static void ownedHeadlinerPhasesAndVictorySettleExactlyOnce(GameTestHelper helper) {
        Fixture fixture = begin(helper, "JSC_headliner", false, 6000);
        helper.runAfterDelay(2, () -> {
            try {
                ServerPlayer player = fixture.player; RushRun run = RivalRushManager.run(player);
                Vec3 base = clearCombatPatch(fixture); Mob headliner = headliner(player);
                actors(player).forEach(mob -> place(mob, base, 36, 20)); place(headliner, base, 7, 0);
                CompoundTag first = RivalRushManager.encounterSnapshot(player);
                check(first.getString("Kind").equals("headliner") && first.getInt("Phase") == 1 && run.headliners() == 0,
                        "Five active minutes did not create the actual first-phase headliner");
                float nativeHealth = headliner.getHealth(); UUID nativeId = headliner.getUUID();
                ServerPlayer foreign = FakePlayerFactory.get(helper.getLevel(), new GameProfile(UUID.randomUUID(), "JSC_foreign"));
                double original = RivalRushManager.actorVitality(player, headliner);
                check(!RivalRushManager.tagEnemy(foreign, headliner, 40) && near(original, RivalRushManager.actorVitality(player, headliner)),
                        "A foreign owner spent another run's headliner vitality");
                damageToFraction(player, headliner, first.getDouble("MaxHealth") * .65);
                check(RivalRushManager.encounterSnapshot(player).getInt("Phase") == 2, "The two-thirds boundary did not enter phase two");
                damageToFraction(player, headliner, first.getDouble("MaxHealth") * .32);
                check(RivalRushManager.encounterSnapshot(player).getInt("Phase") == 3, "The one-third boundary did not enter phase three");
                check(headliner.getUUID().equals(nativeId) && near(headliner.getHealth(), nativeHealth),
                        "Headliner phases replaced the source body or consumed native health");
                defeat(player, headliner);
                CompoundTag victory = RivalRushManager.encounterSnapshot(player); long score = run.score(), defeats = run.defeats();
                check(victory.getString("HeadlinerState").equals("victory") && victory.getBoolean("ForgeEarned")
                                && victory.getInt("DefeatedCount") == 1 && run.headliners() == 1 && run.hasBuildOpportunity(),
                        "An actual owned defeat failed to earn exactly one headliner evolution opportunity");
                check(!RivalRushManager.artifactHit(player, headliner, 40) && run.headliners() == 1
                                && run.score() == score && run.defeats() == defeats,
                        "A removed headliner replayed its defeat, score or forge payout");
                verifyNoBacklog();
                returnNormally(fixture);
                pass(helper, "rush_headliner_victory");
            } finally { cleanup(fixture); }
        });
    }

    @GameTest(template = "hoverboard_empty", timeoutTicks = 900)
    public static void midpointArenaCannotSettleUntilItsRealHeadlinerIsDefeated(GameTestHelper helper) {
        Fixture fixture = begin(helper, "JSC_boss_gate", true, 0);
        prepareCircuitRoom(helper, fixture, 1);
    }

    @GameTest(template = "hoverboard_empty", timeoutTicks = 600)
    public static void lostHeadlinersReturnSafelyAndTheStreetDeadlineStillOffersEncore(GameTestHelper helper) {
        HeadlinerRecoveryFixture fixture = new HeadlinerRecoveryFixture(helper);
        // Register once. Later phases update only fixture fields, never GameTest's scheduled-task map.
        helper.onEachTick(fixture::tick);
    }

    /** Native chunk/actor readiness precedes clock compression; it is not a human pacing fixture. */
    private static final class HeadlinerRecoveryFixture {
        private final GameTestHelper helper;
        private Fixture current;
        private int phase, waited;
        private boolean finished;

        private HeadlinerRecoveryFixture(GameTestHelper helper) {
            this.helper = helper;
            current = beginBranch("JSC_boss_lost");
        }

        private Fixture beginBranch(String name) {
            Fixture fixture = begin(helper, name, false, 0);
            try {
                // Supply the outer neighbors around the existing radius-four ticket before
                // starting the native readiness deadline. Terrain preparation alone still
                // does not satisfy the unchanged 25-chunk entity-ticking gate.
                ChunkPos center = fixture.held.get(0);
                for (int x = -4; x <= 4; x++) for (int z = -4; z <= 4; z++)
                    if (Math.max(Math.abs(x), Math.abs(z)) > 2)
                        fixture.district.getChunk(center.x + x, center.z + z);
                return fixture;
            } catch (RuntimeException | Error error) { cleanup(fixture); throw error; }
        }

        private void tick() {
            if (finished) return;
            try {
                ServerPlayer player = current.player; RushRun run = RivalRushManager.run(player);
                check(run != null && RivalRushManager.active(player),
                        "Headliner recovery fixture ended before its intended assertion: " + diagnostic());
                check(++waited <= 120, "Native headliner recovery readiness timed out: " + diagnostic());
                if (phase == 0 || phase == 2) {
                    // getChunk() only supplies terrain; entity UUID lookup remains unavailable until
                    // the ticket's full spawn area becomes entity-ticking in actual server ticks.
                    if (residentChunks() != 25) return;
                    chooseDrafts(player);
                    advance(run, Math.max(0, (phase == 0 ? 6000 : 30000) - run.elapsedTicks()));
                    RivalRushManager.tick(player.getServer());
                    phase++; waited = 0; return;
                }
                CompoundTag encounter = RivalRushManager.encounterSnapshot(player);
                if (!encounter.getString("Kind").equals("headliner") || !encounter.hasUUID("Actor")) return;
                var actor = player.serverLevel().getEntity(encounter.getUUID("Actor"));
                if (!(actor instanceof Mob)) return;
                Mob boss = headliner(player);
                check(!boss.isRemoved() && boss.isAlive() && boss.level() == current.district
                                && RivalRushManager.belongsTo(boss, player),
                        "The real headliner failed native identity/ownership checks: " + diagnostic());
                System.out.println("JETSETCRAFT_RUSH_HEADLINER_RESIDENCY branch=" + (phase == 1 ? "lost" : "deadline")
                        + " actor=" + boss.getUUID() + " observedAfterNativeTicks=" + waited
                        + " entityTicking=" + current.district.isPositionEntityTicking(boss.blockPosition())
                        + " elapsed=" + run.elapsedTicks());
                if (phase == 1) {
                    boss.discard(); RivalRushManager.tick(player.getServer());
                    check(!RivalRushManager.active(player) && run.headliners() == 0,
                            "A missing headliner bypassed the gate or manufactured a victory");
                    assertRestored(current); cleanup(current);
                    current = beginBranch("JSC_boss_limit");
                    phase = 2; waited = 0; return;
                }
                advance(run, RushRun.SESSION_TICKS - 1 - run.elapsedTicks());
                RivalRushManager.tick(player.getServer());
                check(RivalRushManager.active(player) && run.awaitingEncore() && boss.isRemoved()
                                && run.headliners() == 0 && RivalRushManager.encounterSnapshot(player).getString("State").equals("aborted"),
                        "The finite deadline blocked bank/Encore, retained its boss, or invented a victory");
                RivalRushManager.handleAction(player, "encore", "", 0, run.id(), run.offerVersion()); chooseDrafts(player);
                RivalRushManager.tick(player.getServer());
                check(run.endless() && !RivalRushManager.encounterSnapshot(player).getString("Kind").equals("headliner"),
                        "Encore replayed a missed headliner window immediately instead of continuing normally");
                returnNormally(current); cleanup(current); finished = true;
                pass(helper, "rush_headliner_recovery");
            } catch (RuntimeException error) {
                finished = true; cleanup(current); throw error;
            }
        }

        private int residentChunks() {
            ChunkPos center = current.held.get(0); int ready = 0;
            for (int x = -2; x <= 2; x++) for (int z = -2; z <= 2; z++)
                if (current.district.isPositionEntityTicking(new BlockPos((center.x + x) << 4,
                        RushWorld.FLOOR_Y + 1, (center.z + z) << 4))) ready++;
            return ready;
        }

        private String diagnostic() {
            ServerPlayer player = current.player; RushRun run = RivalRushManager.run(player);
            CompoundTag encounter = RivalRushManager.encounterSnapshot(player);
            UUID actor = encounter.hasUUID("Actor") ? encounter.getUUID("Actor") : null;
            var body = actor == null ? null : player.serverLevel().getEntity(actor);
            return "branch=" + (phase < 2 ? "lost" : "deadline") + ", phase=" + phase + ", waited=" + waited
                    + ", active=" + RivalRushManager.active(player) + ", kind=" + encounter.getString("Kind")
                    + ", state=" + encounter.getString("HeadlinerState") + ", actor=" + actor
                    + ", body=" + (body == null ? "not visible" : body.getClass().getSimpleName())
                    + ", residentChunks=" + residentChunks() + "/25, player=" + player.position()
                    + ", dimension=" + player.serverLevel().dimension().location()
                    + ", elapsed=" + (run == null ? -1 : run.elapsedTicks());
        }
    }

    private static void prepareCircuitRoom(GameTestHelper helper, Fixture fixture, int stage) {
        try {
            ServerPlayer player = fixture.player; RushRun run = RivalRushManager.run(player); chooseDrafts(player);
            CompoundTag journey = RivalRushManager.journeySnapshot(player);
            check(journey.getString("Phase").equals("choosing") && journey.getInt("Stage") == stage, "Circuit fixture lost its real route decision");
            String choice = journey.getList("Choices", Tag.TAG_COMPOUND).getCompound(0).getString("Id");
            RivalRushManager.handleAction(player, "journey_choose", choice, 0, run.id(), journey.getInt("Version"));
            BlockPos destination = RivalRushManager.arenaDestination(player); hold(fixture, destination);
            // Fixture arrival only: the authored map still must pass the actual builder/arrival gate.
            // FakePlayer has no movement connection, so use the same explicit positioning as the
            // existing arena GameTests. This does not claim human traversal or an arena clear.
            player.moveTo(destination.getX() + .5, RushWorld.FLOOR_Y + 1, destination.getZ() + .5, 0, 0);
            player.setDeltaMovement(Vec3.ZERO); fixture.district.getChunkSource().move(player);
            for (int i = 0; i < 128; i++) RivalRushManager.tick(player.getServer());
            helper.runAfterDelay(2, () -> finishCircuitRoomFixture(helper, fixture, stage));
        } catch (RuntimeException error) { cleanup(fixture); throw error; }
    }

    private static void finishCircuitRoomFixture(GameTestHelper helper, Fixture fixture, int stage) {
        try {
            ServerPlayer player = fixture.player; RushRun run = RivalRushManager.run(player); chooseDrafts(player);
            CompoundTag entered = RivalRushManager.journeySnapshot(player);
            check(entered.getString("Phase").equals("arena"), "The real generated room did not admit the fixture's arrival: stage="
                    + stage + ", phase=" + entered.getString("Phase") + ", player=" + player.position()
                    + ", destination=" + RivalRushManager.arenaDestination(player) + ", offer=" + run.hasOffer());
            advance(run, entered.getLong("TicksLeft") + 1);
            int quota = entered.getCompound("Current").getInt("Defeats"); long start = run.defeats();
            UUID bossId = stage == 3 ? headliner(player).getUUID() : null;
            for (int wave = 0; wave < quota + 30 && run.defeats() - start < quota; wave++) {
                chooseDrafts(player); advance(run, 40); RivalRushManager.tick(player.getServer());
                for (Mob actor : actors(player)) {
                    if (!actor.getUUID().equals(bossId)) defeat(player, actor);
                    if (run.defeats() - start >= quota) break;
                }
            }
            check(run.defeats() - start >= quota || RivalRushManager.journeySnapshot(player).getInt("Completed") == stage,
                    "The bounded native combat fixture did not earn its rival quota: stage=" + stage
                            + ", fixtureDefeats=" + (run.defeats() - start) + "/" + quota + ", " + circuitState(player));
            if (stage < 3) {
                settleCircuitRoom(player, stage, "Actual native rival defeats did not settle an ordinary room");
                prepareCircuitRoom(helper, fixture, stage + 1); return;
            }
            chooseDrafts(player); RivalRushManager.tick(player.getServer());
            CompoundTag blocked = RivalRushManager.journeySnapshot(player);
            check(blocked.getString("Phase").equals("arena") && blocked.getInt("Completed") == 2
                            && blocked.getLong("TicksLeft") == 0 && run.defeats() - start >= quota
                            && RivalRushManager.encounterSnapshot(player).getBoolean("RoomVictoryRequired") && run.headliners() == 0,
                    "Time and ordinary defeats cleared room three while its headliner was alive");
            Mob boss = headliner(player);
            // The protected north lane is built by readyForEntry in every profile. Keep the boss's
            // actual card separate from ordinary fixture drops at center + (5, 0), without deleting
            // or pre-collecting those piles. Even maximum ordinary pickup reach is only 12 blocks.
            Vec3 dropPosition = Vec3.atBottomCenterOf(RivalRushManager.arenaDestination(player).offset(0, 1, -12));
            defeat(player, boss, dropPosition); chooseDrafts(player);
            ItemEntity bossCard = fixture.district.getEntitiesOfClass(ItemEntity.class, player.getBoundingBox().inflate(32),
                    item -> RivalRushManager.belongsTo(item, player) && item.position().distanceToSqr(dropPosition) < 1
                            && item.getItem().hasTag() && item.getItem().getTag().hasUUID("JetSetCraftRushPickup")
                            && item.getItem().getTag().getUUID("JetSetCraftRushPickup").equals(boss.getUUID()))
                    .stream().findFirst().orElseThrow(() -> new GameTestAssertException("The actual owned headliner card was not present at its isolated drop point"));
            check(bossCard.getItem().getCount() == 1 && run.headliners() == 1 && run.hasBuildOpportunity()
                            && RivalRushManager.encounterSnapshot(player).getString("HeadlinerState").equals("victory"),
                    "The actual headliner defeat failed to settle its owned card and forge before pickup: " + circuitState(player));
            // Explicit XP-threshold fixture only. The next level must be earned by the real boss
            // card's production pickup, after its separate synchronous defeat XP has been resolved.
            long xpGap = run.xpToNextLevel() - run.xp() - 1;
            check(xpGap >= 0 && xpGap <= Integer.MAX_VALUE, "The fixture did not finish its genuine defeat-XP drafts: " + circuitState(player));
            run.gainXp((int) xpGap);
            check(!run.hasOffer() && run.xp() == run.xpToNextLevel() - 1,
                    "XP prerequisite setup itself opened a draft: " + circuitState(player));
            long cardsBefore = RushAlbum.total(player);
            player.moveTo(dropPosition.x, dropPosition.y, dropPosition.z, 0, 0); player.setDeltaMovement(Vec3.ZERO);
            RivalRushManager.tick(player.getServer());
            check(bossCard.isRemoved() && RushAlbum.total(player) > cardsBefore && run.hasOffer()
                            && RivalRushManager.journeySnapshot(player).getInt("Completed") == 2
                            && run.headliners() == 1 && run.hasBuildOpportunity()
                            && RivalRushManager.encounterSnapshot(player).getString("HeadlinerState").equals("victory"),
                    "An actual boss-card XP draft failed to pause room settlement while retaining victory: " + circuitState(player));
            settleCircuitRoom(player, stage, "A real midpoint headliner victory did not settle after its earned pickup draft");
            check(RivalRushManager.journeySnapshot(player).getInt("Completed") == 3 && run.headliners() == 1 && run.hasBuildOpportunity(),
                    "A real midpoint headliner victory failed to release exactly one room settlement and forge opportunity: " + circuitState(player));
            RushDirector finalGate = new RushDirector(); finalGate.schedule(100, true, 1, 6);
            check(!finalGate.permitsRoomCompletion(1, 6), "The final-room gate permitted an unspawned headliner");
            UUID finalId = UUID.randomUUID(); check(finalGate.spawned(finalId), "Final gate could not register its actor");
            finalGate.abort("Fixture actor was lost", true);
            check(!finalGate.permitsRoomCompletion(1, 6) && !finalGate.defeated(finalId), "An aborted final encounter granted a room clear");
            returnNormally(fixture); pass(helper, "rush_headliner_arena_gate");
        } catch (RuntimeException error) { cleanup(fixture); throw error; }
        if (stage == 3) cleanup(fixture);
    }

    /** A tick may legitimately collect cards and open a fresh draft before the room-clear check. */
    private static void settleCircuitRoom(ServerPlayer player, int stage, String failure) {
        RushRun run = RivalRushManager.run(player);
        for (int attempt = 0; attempt < 8 && RivalRushManager.journeySnapshot(player).getInt("Completed") < stage; attempt++) {
            chooseDrafts(player);
            RivalRushManager.tick(player.getServer());
        }
        check(RivalRushManager.journeySnapshot(player).getInt("Completed") == stage && run != null && run.arenaClears() == stage,
                failure + ": " + circuitState(player));
    }

    private static String circuitState(ServerPlayer player) {
        RushRun run = RivalRushManager.run(player); CompoundTag journey = RivalRushManager.journeySnapshot(player);
        return "phase=" + journey.getString("Phase") + ", stage=" + journey.getInt("Stage")
                + ", completed=" + journey.getInt("Completed") + ", room=" + journey.getString("CurrentName")
                + ", objective=" + journey.getString("Objective") + ", ticksLeft=" + journey.getLong("TicksLeft")
                + ", elapsed=" + (run == null ? -1 : run.elapsedTicks()) + ", defeats=" + (run == null ? -1 : run.defeats())
                + ", actors=" + actors(player).size() + ", player=" + player.position()
                + ", destination=" + RivalRushManager.arenaDestination(player)
                + ", finished=" + (run != null && run.isFinished()) + ", encore=" + (run != null && run.awaitingEncore())
                + ", offer=" + (run != null && run.hasOffer()) + ", cards=" + RushAlbum.total(player)
                + ", headliners=" + (run == null ? -1 : run.headliners())
                + ", opportunity=" + (run != null && run.hasBuildOpportunity())
                + ", headlinerState=" + RivalRushManager.encounterSnapshot(player).getString("HeadlinerState");
    }

    private static void verifyGeometryBounds() {
        RushDirector rules = new RushDirector(); UUID owner = UUID.randomUUID(); Vec3 origin = Vec3.ZERO;
        check(rules.tell(owner, RushDirector.Pattern.RING, origin, new Vec3(0, 0, 5), 0, 0, false), "Ring fixture was rejected");
        RushDirector.Tell ring = rules.tells().get(0);
        check(!ring.covers(new Vec3(1, 0, 0)) && ring.covers(new Vec3(4, 0, 0))
                        && !ring.covers(new Vec3(8, 0, 0)) && !ring.covers(new Vec3(4, 3, 0)),
                "Bass ring lost its inner/outer or vertical dodge space");
        for (int i = 1; i < RushDirector.MAX_TELLS; i++) check(rules.tell(owner, RushDirector.Pattern.DISK, origin, origin, 0, 0, false), "Tell budget rejected a permitted entry");
        check(!rules.tell(owner, RushDirector.Pattern.LINE, origin, origin, 0, 0, false)
                        && rules.tells().size() == RushDirector.MAX_TELLS, "Encounter tells exceeded their strict server bound");
    }

    private static void verifyNoBacklog() {
        RushDirector rules = new RushDirector(); UUID id = UUID.randomUUID();
        rules.schedule(6000, false, 0, 0); check(rules.spawned(id), "First five-minute window did not schedule");
        rules.schedule(12000, false, 0, 0); rules.schedule(18000, false, 0, 0);
        check(rules.defeated(id) && !rules.defeated(id), "Director terminal victory replayed");
        rules.schedule(18001, false, 0, 0); check(!rules.spawnPending(), "Missed windows queued a burst of replacement headliners");
        rules.schedule(24000, false, 0, 0); check(rules.spawnPending(), "Next fresh headliner window failed to schedule");
    }

    private static final class Fixture {
        final ServerPlayer player;
        final ServerLevel source, district;
        final Vec3 returnPosition;
        final ListTag inventory;
        final CompoundTag ride;
        final List<ChunkPos> held = new ArrayList<>();
        Fixture(ServerPlayer player, ServerLevel source, Vec3 returnPosition, ListTag inventory, CompoundTag ride) {
            this.player = player; this.source = source; this.returnPosition = returnPosition;
            this.inventory = inventory; this.ride = ride; this.district = player.serverLevel();
        }
    }

    private static Fixture begin(GameTestHelper helper, String name, boolean circuit, long clock) {
        ServerPlayer player = FakePlayerFactory.get(helper.getLevel(), new GameProfile(UUID.randomUUID(), name));
        player.setGameMode(GameType.SURVIVAL); player.getInventory().clearContent(); player.containerMenu.setCarried(ItemStack.EMPTY);
        BlockPos source = helper.absolutePos(new BlockPos(1, 2, 1));
        player.moveTo(source.getX() + .5, source.getY(), source.getZ() + .5, 0, 0);
        ItemStack ordinary = new ItemStack(Items.EMERALD, 7); ordinary.getOrCreateTag().putString("EncounterReturn", "original");
        player.getInventory().setItem(0, ordinary); player.experienceLevel = 12; player.totalExperience = 93; player.experienceProgress = .25f;
        ListTag inventory = player.getInventory().save(new ListTag());
        CompoundTag ride = player.getCapability(JetSetDataProvider.CAPABILITY).resolve().orElseThrow().save(); Vec3 position = player.position();
        check(circuit ? RivalRushManager.startCircuit(player, id("jetsetcraft:dead_beat"), id("minecraft:zombie"), false, null)
                        : RivalRushManager.start(player, id("jetsetcraft:dead_beat"), id("minecraft:zombie"), false),
                "Could not start the production encounter session");
        Fixture fixture = new Fixture(player, helper.getLevel(), position, inventory, ride);
        try {
            hold(fixture, player.blockPosition());
            if (!circuit) { advance(RivalRushManager.run(player), clock); RivalRushManager.tick(player.getServer()); }
            return fixture;
        } catch (RuntimeException error) { cleanup(fixture); throw error; }
    }

    private static void hold(Fixture fixture, BlockPos center) {
        ChunkPos chunk = new ChunkPos(center); fixture.held.add(chunk);
        fixture.district.getChunkSource().addRegionTicket(TICKET, chunk, 4, fixture.player.getUUID());
        for (int x = -2; x <= 2; x++) for (int z = -2; z <= 2; z++) fixture.district.getChunk(chunk.x + x, chunk.z + z);
    }
    private static Vec3 clearCombatPatch(Fixture fixture) {
        Vec3 base = new Vec3(fixture.player.getX(), RushWorld.FLOOR_Y + 1, fixture.player.getZ()); BlockPos floor = BlockPos.containing(base).below();
        for (int x = -10; x <= 10; x++) for (int z = -10; z <= 10; z++) {
            fixture.district.setBlock(floor.offset(x, 0, z), Blocks.STONE.defaultBlockState(), 2);
            for (int y = 1; y <= 5; y++) fixture.district.setBlock(floor.offset(x, y, z), Blocks.AIR.defaultBlockState(), 2);
        }
        fixture.player.moveTo(base.x, base.y, base.z, 0, 0); fixture.player.setOnGround(true); fixture.player.setDeltaMovement(Vec3.ZERO);
        return base;
    }
    private static void wall(Fixture fixture, Vec3 base, boolean solid) {
        BlockPos origin = BlockPos.containing(base).offset(3, 0, 0);
        for (int y = 0; y < 4; y++) for (int z = -2; z <= 2; z++)
            fixture.district.setBlock(origin.offset(0, y, z), (solid ? Blocks.STONE : Blocks.AIR).defaultBlockState(), 2);
    }
    private static Set<String> eliteNames(ServerPlayer player) {
        Set<String> names = new HashSet<>();
        for (Mob mob : actors(player)) for (RushDirector.Elite elite : RushDirector.Elite.values())
            if (elite != RushDirector.Elite.NONE && mob.getName().getString().contains(elite.label())) names.add(elite.label());
        return names;
    }
    private static List<Mob> actors(ServerPlayer player) {
        return player.serverLevel().getEntitiesOfClass(Mob.class, player.getBoundingBox().inflate(100), mob -> RivalRushManager.belongsTo(mob, player));
    }
    private static Mob headliner(ServerPlayer player) {
        CompoundTag encounter = RivalRushManager.encounterSnapshot(player);
        check(encounter.getString("Kind").equals("headliner") && encounter.hasUUID("Actor"), "Actual owned headliner body is missing");
        var entity = player.serverLevel().getEntity(encounter.getUUID("Actor"));
        check(entity instanceof Mob, "Headliner snapshot does not point to a native Mob"); return (Mob) entity;
    }
    private static void place(Mob mob, Vec3 base, double x, double z) { mob.moveTo(base.x + x, base.y, base.z + z, 0, 0); mob.setDeltaMovement(Vec3.ZERO); }
    private static void damageToFraction(ServerPlayer player, Mob mob, double health) {
        for (int hit = 0; hit < 100 && RivalRushManager.actorVitality(player, mob) > health; hit++)
            check(RivalRushManager.artifactHit(player, mob, 20), "A valid owned virtual hit was refused");
    }
    private static void defeat(ServerPlayer player, Mob mob) {
        defeat(player, mob, player.position().add(5, 0, 0));
    }
    private static void defeat(ServerPlayer player, Mob mob, Vec3 position) {
        chooseDrafts(player);
        if (mob.isRemoved() || RivalRushManager.actorVitality(player, mob) < 0) return;
        mob.moveTo(position.x, position.y, position.z, 0, 0); mob.setDeltaMovement(Vec3.ZERO);
        for (int hit = 0; hit < 200 && RivalRushManager.actorVitality(player, mob) > 0; hit++) {
            chooseDrafts(player); check(RivalRushManager.artifactHit(player, mob, 40), "An actual owned fixture rival could not be defeated");
        }
        check(mob.isRemoved(), "Bounded virtual hits failed to defeat the actual owned body"); chooseDrafts(player);
    }
    private static void chooseDrafts(ServerPlayer player) {
        RushRun run = RivalRushManager.run(player); if (run == null) return;
        for (int choice = 0; choice < 100 && run.hasOffer(); choice++) {
            int index = -1;
            // A neutral stat keeps damage assertions independent from a newly rolled Ward or Breakbeat
            // artifact. Force is safe: the exact recovery assertion already includes damageMultiplier().
            for (int i = 0; i < run.offers().size(); i++)
                if (!run.offers().get(i).artifact()) { index = i; break; }
            if (index < 0) for (int i = 0; i < run.offers().size(); i++)
                if (run.offers().get(i) != RushRun.Upgrade.EXPOSE && run.offers().get(i) != RushRun.Upgrade.WARD) { index = i; break; }
            check(index >= 0, "Fixture draft has no neutral stat or safe non-ward/non-mark alternative");
            RivalRushManager.handleAction(player, "choose", "", index, run.id(), run.offerVersion());
        }
        check(!run.hasOffer(), "Fixture could not resolve a genuine earned XP draft");
    }
    private static void advance(RushRun run, long ticks) {
        for (long tick = 0; tick < ticks; tick++) run.tickActive();
    }
    private static void returnNormally(Fixture fixture) {
        check(RivalRushManager.leave(fixture.player, "Encounter acceptance return"), "Normal encounter return failed"); assertRestored(fixture);
    }
    private static void assertRestored(Fixture fixture) {
        ServerPlayer player = fixture.player;
        check(player.serverLevel() == fixture.source && player.position().distanceToSqr(fixture.returnPosition) < .0001
                        && fixture.inventory.equals(player.getInventory().save(new ListTag()))
                        && fixture.ride.equals(player.getCapability(JetSetDataProvider.CAPABILITY).resolve().orElseThrow().save())
                        && player.experienceLevel == 12 && player.totalExperience == 93 && player.experienceProgress == .25f
                        && !RushReturnState.hasJournal(player), "Encounter return changed ordinary gear, inventory NBT, XP, position or return journal");
    }
    private static void cleanup(Fixture fixture) {
        try {
            if (RivalRushManager.active(fixture.player) || RushReturnState.hasJournal(fixture.player))
                check(RivalRushManager.leave(fixture.player, "Encounter GameTest cleanup"), "Encounter fixture failed to return its player safely");
        } finally {
            for (ChunkPos chunk : fixture.held) fixture.district.getChunkSource().removeRegionTicket(TICKET, chunk, 4, fixture.player.getUUID());
            fixture.held.clear();
        }
    }
    private static ResourceLocation id(String text) { return ResourceLocation.tryParse(text); }
    private static boolean near(double left, double right) { return Math.abs(left - right) < .000001; }
    private static void check(boolean valid, String reason) { if (!valid) throw new GameTestAssertException(reason); }
    private static void pass(GameTestHelper helper, String marker) { System.out.println("JETSETCRAFT_GAMETEST_PASS " + marker); helper.succeed(); }
    private RushEncounterGameTests() { }
}
