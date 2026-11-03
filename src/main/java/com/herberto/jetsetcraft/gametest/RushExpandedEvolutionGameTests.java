package com.herberto.jetsetcraft.gametest;

import com.herberto.jetsetcraft.JetSetCraft;
import com.herberto.jetsetcraft.data.JetSetDataProvider;
import com.herberto.jetsetcraft.network.C2SRushActionPacket;
import com.herberto.jetsetcraft.rush.RivalRushManager;
import com.herberto.jetsetcraft.rush.RushBuildPaths;
import com.herberto.jetsetcraft.rush.RushDebrief;
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
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.common.util.FakePlayerFactory;
import net.minecraftforge.gametest.GameTestHolder;
import net.minecraftforge.gametest.PrefixGameTestTemplate;

import java.util.Comparator;
import java.util.List;
import java.util.UUID;

/** Production targets, observed movement and accepted resource spending; setup clocks/ranks are controlled fixtures. */
@GameTestHolder(JetSetCraft.MOD_ID)
@PrefixGameTestTemplate(false)
public final class RushExpandedEvolutionGameTests {
    private static final TicketType<UUID> TICKET = TicketType.create("jetsetcraft_expanded_evolution_gametest", UUID::compareTo, 500);

    @GameTest(template = "hoverboard_empty", timeoutTicks = 100)
    public static void sixBuildPathsKeepOnePreparationThreeSlotsAndBoundedSoulReprise(GameTestHelper helper) {
        for (RushBuildPaths.Evolution evolution : List.of(RushBuildPaths.Evolution.CHROMATIC_FINALE,
                RushBuildPaths.Evolution.BASS_DROP, RushBuildPaths.Evolution.SOUL_REPRISE)) {
            RushRun run = new RushRun(UUID.randomUUID(), UUID.randomUUID(), 4401, false, "jetsetcraft:dead_beat");
            List<RushRun.Upgrade> pair = evolution.requirements();
            check(run.grantStartingPerk(pair.get(0)) && !run.grantStartingPerk(pair.get(1)),
                    "An expanded recipe manufactured a second starting preparation");
            grant(run, pair.get(0), 2); grant(run, pair.get(1), 2);
            run.grantHeadlinerReward();
            check(!evolution.ready(run) && !run.evolve(evolution, run.offerVersion()) && run.hasBuildOpportunity(),
                    "A rank-two recipe spent the earned opportunity");
            grant(run, pair.get(1), 1); grant(run, RushRun.Upgrade.WARD, 1);
            check(!run.grantReward(RushRun.Upgrade.VENOM) && run.artifactCount() == 3,
                    "An expanded recipe admitted a fourth artifact");
            int version = run.offerVersion();
            check(!run.evolve(evolution, version - 1) && run.evolve(evolution, version)
                            && run.evolutionActive(evolution) && !run.hasBuildOpportunity()
                            && RushBuildPaths.snapshot(run).getList("Recipes", Tag.TAG_COMPOUND).size() == 6,
                    "Expanded build selection lost rank/version authority or its six visible recipes");
        }
        RushRun run = new RushRun(UUID.randomUUID(), UUID.randomUUID(), 4402, false, "jetsetcraft:dead_beat");
        run.configureCircuit(); grant(run, RushRun.Upgrade.SOUL, 3); grant(run, RushRun.Upgrade.SOUL_BURST, 3);
        run.grantHeadlinerReward(); check(run.evolve(RushBuildPaths.Evolution.SOUL_REPRISE, run.offerVersion()), "Could not forge the soul rule fixture");
        run.hurt(4); for (int i = 0; i < 5; i++) run.recordDefeat();
        check(run.spendSoulBurst() && near(run.vitality(), 100) && near(run.vitalityRestored(), 4)
                        && run.soulCharge() == 5 && run.soulBurstCharge() == 0 && run.tempoTicks() == 60
                        && near(run.cadenceMultiplier(), .7) && !run.spendSoulBurst(),
                "Soul reprise banked overheal, consumed healing souls, or replayed an empty offensive reservoir");
        for (int tick = 0; tick < 30; tick++) run.tickActive();
        run.hurt(12); for (int i = 0; i < 5; i++) run.recordDefeat();
        check(run.spendSoulBurst() && near(run.vitality(), 94) && near(run.vitalityRestored(), 10)
                        && run.tempoTicks() == 60 && run.soulCharge() == 10
                        && run.evolutionProcs(RushBuildPaths.Evolution.SOUL_REPRISE) == 2,
                "Soul reprise exceeded six recovery or stacked its tempo duration");
        for (int i = 0; i < 5; i++) run.recordDefeat();
        run.pauseForEncore(); long paused = run.elapsedTicks();
        for (int tick = 0; tick < 80; tick++) run.tickActive();
        check(run.elapsedTicks() == paused && run.tempoTicks() == 60 && !run.spendSoulBurst()
                        && run.soulBurstCharge() == 5 && run.continueEndless(), "A paused burst spent souls or advanced tempo");
        run.grantRemix();
        check(run.remix(RushRun.Upgrade.SOUL_BURST, RushRun.Upgrade.CHAIN, run.offerVersion())
                        && !run.evolutionActive(RushBuildPaths.Evolution.SOUL_REPRISE)
                        && run.soulBurstCharge() == 0 && run.tempoTicks() == 0 && near(run.cadenceMultiplier(), 1),
                "A removed recipe retained its soul reservoir or temporary cadence power");
        pass(helper, "rush_expanded_evolution_rules");
    }

    @GameTest(template = "hoverboard_empty", timeoutTicks = 500)
    public static void chromaticFinaleRespectsSixthPrimaryThreeTargetCapWallsAndNativeState(GameTestHelper helper) {
        Fixture fixture = begin(helper, "JSC_finale", 8);
        helper.runAfterDelay(2, () -> {
            Mob foreign = null;
            try {
                ServerPlayer player = fixture.player(); RushRun run = RivalRushManager.run(player);
                List<Mob> actors = arrange(fixture); check(actors.size() >= 8, "Too few real finale targets");
                Vec3 base = player.position(); Mob primary = actors.get(0);
                place(primary, base, 3, 0);
                for (int i = 1; i <= 4; i++) place(actors.get(i), base, 11 + i * .2, .2);
                place(actors.get(5), base, 10, -4); place(actors.get(6), base, 16, 7);
                wall(player, 5, 5, -4, -1);
                forge(player, RushBuildPaths.Evolution.CHROMATIC_FINALE);
                foreign = EntityType.ZOMBIE.create(player.serverLevel()); check(foreign != null, "No foreign fixture mob");
                place(foreign, base, 11, .5); check(player.serverLevel().addFreshEntity(foreign), "Could not add foreign control mob");
                float foreignHealth = foreign.getHealth(), nativeHealth = primary.getHealth();
                double speed = primary.getAttributeValue(Attributes.MOVEMENT_SPEED);
                double[] before = new double[7];
                for (int i = 1; i <= 6; i++) before[i] = RivalRushManager.actorVitality(player, actors.get(i));
                for (int hit = 0; hit < 5; hit++) check(RivalRushManager.tagEnemy(player, primary, .01), "Accepted primary failed");
                check(run.evolutionProcs(RushBuildPaths.Evolution.CHROMATIC_FINALE) == 0,
                        "Chromatic finale triggered before the sixth primary");
                for (int i = 1; i <= 6; i++) check(near(before[i], RivalRushManager.actorVitality(player, actors.get(i))),
                        "The ordinary chain/blast fixture reached an outer-ring control before the finale");
                check(RivalRushManager.tagEnemy(player, primary, .01) && run.hits() == 6
                                && run.evolutionProcs(RushBuildPaths.Evolution.CHROMATIC_FINALE) == 1,
                        "Sixth primary failed to produce exactly one nonrecursive finale");
                for (int i = 1; i <= 3; i++) check(near(before[i] - RivalRushManager.actorVitality(player, actors.get(i)), 8),
                        "A visible finale target did not receive its bounded secondary hit");
                for (int i = 4; i <= 6; i++) check(near(before[i], RivalRushManager.actorVitality(player, actors.get(i))),
                        "Finale exceeded three targets or crossed its wall/radius boundary");
                check(RivalRushManager.artifactHit(player, primary, .01) && run.hits() == 6
                                && run.evolutionProcs(RushBuildPaths.Evolution.CHROMATIC_FINALE) == 1
                                && foreign.getHealth() == foreignHealth && primary.getHealth() == nativeHealth
                                && near(primary.getAttributeValue(Attributes.MOVEMENT_SPEED), speed),
                        "A secondary spark replayed the finale or changed foreign/native state");
                restored(fixture); pass(helper, "rush_expanded_evolution_finale");
            } finally { if (foreign != null) foreign.discard(); cleanup(fixture); }
        });
    }

    @GameTest(template = "hoverboard_empty", timeoutTicks = 500)
    public static void bassDropUsesRealLandingFixedOriginAndExactlyThreeActiveTimePulses(GameTestHelper helper) {
        Fixture fixture = begin(helper, "JSC_bass", 3);
        helper.runAfterDelay(2, () -> {
            try {
                ServerPlayer player = fixture.player(); RushRun run = RivalRushManager.run(player);
                List<Mob> actors = arrange(fixture); check(actors.size() >= 3, "Too few real aftershock targets");
                forge(player, RushBuildPaths.Evolution.BASS_DROP);
                Vec3 base = player.position();
                RivalRushManager.tick(player.getServer());
                check(run.evolutionProcs(RushBuildPaths.Evolution.BASS_DROP) == 0, "Standing still created a Bass drop");
                for (int tick = 1; tick <= 6; tick++) {
                    player.moveTo(base.x + tick * .3, base.y + .8, base.z, 0, 0); player.setOnGround(false);
                    RivalRushManager.tick(player.getServer());
                }
                check(RivalRushManager.buildSnapshot(player).getBoolean("AirborneReady"), "Observed moving airtime did not arm the landing");
                player.moveTo(base.x + 2.1, base.y, base.z, 0, 0); player.setOnGround(true);
                RivalRushManager.tick(player.getServer());
                CompoundTag cast = RivalRushManager.buildSnapshot(player);
                check(run.evolutionProcs(RushBuildPaths.Evolution.BASS_DROP) == 1 && cast.getInt("AftershockTicks") == 60
                                && cast.getInt("AftershockRemaining") == 3 && cast.getLong("AftershockPulses") == 0,
                        "The real landing failed to create one finite aftershock circle");
                Vec3 anchor = player.position(); Mob captain = actors.get(0), blocked = actors.get(1), distant = actors.get(2);
                place(captain, anchor, 4, 0); place(blocked, anchor, -3, 0); place(distant, anchor, 11, 0);
                wall(player, -1, -1, -2, 2);
                float nativeHealth = captain.getHealth();
                double before = RivalRushManager.actorVitality(player, captain);
                double blockedBefore = RivalRushManager.actorVitality(player, blocked), distantBefore = RivalRushManager.actorVitality(player, distant);
                // Moving away must not drag the saved circle onto a different rival.
                player.moveTo(anchor.x + 10, anchor.y, anchor.z, 0, 0); player.setOnGround(true);
                for (int tick = 0; tick < 19; tick++) RivalRushManager.tick(player.getServer());
                check(near(before, RivalRushManager.actorVitality(player, captain)), "Aftershock fired before one active second");
                RivalRushManager.tick(player.getServer());
                check(near(before - RivalRushManager.actorVitality(player, captain), 6)
                                && near(blockedBefore, RivalRushManager.actorVitality(player, blocked))
                                && near(distantBefore, RivalRushManager.actorVitality(player, distant))
                                && RivalRushManager.buildSnapshot(player).getLong("AftershockPulses") == 1,
                        "Aftershock moved with the rider or crossed its visible fixed-circle boundary");
                run.pauseForEncore(); long paused = run.elapsedTicks();
                for (int tick = 0; tick < 6; tick++) RivalRushManager.tick(player.getServer());
                check(run.elapsedTicks() == paused && RivalRushManager.buildSnapshot(player).getInt("AftershockTicks") == 40
                                && RivalRushManager.buildSnapshot(player).getLong("AftershockPulses") == 1
                                && run.continueEndless(), "A paused menu spent or erased the aftershock schedule");
                for (int tick = 0; tick < 40; tick++) RivalRushManager.tick(player.getServer());
                check(near(before - RivalRushManager.actorVitality(player, captain), 18)
                                && RivalRushManager.buildSnapshot(player).getLong("AftershockPulses") == 3
                                && RivalRushManager.buildSnapshot(player).getInt("AftershockRemaining") == 0,
                        "The circle did not finish exactly three scheduled pulses");
                for (int tick = 0; tick < 20; tick++) RivalRushManager.tick(player.getServer());
                check(near(before - RivalRushManager.actorVitality(player, captain), 18) && captain.getHealth() == nativeHealth
                                && run.evolutionProcs(RushBuildPaths.Evolution.BASS_DROP) == 1
                                && RivalRushManager.buildSnapshot(player).getInt("AftershockTicks") == 0,
                        "A grounded rider repeated the landing, the expired circle kept damage, or native health changed");
                restored(fixture); pass(helper, "rush_expanded_evolution_bass");
            } finally { cleanup(fixture); }
        });
    }

    @GameTest(template = "hoverboard_empty", timeoutTicks = 500)
    public static void soulRepriseUsesFiveRealDefeatsAndPreservesHealingSoulsAndReturnState(GameTestHelper helper) {
        Fixture fixture = begin(helper, "JSC_reprise", 7);
        helper.runAfterDelay(2, () -> {
            try {
                ServerPlayer player = fixture.player(); RushRun run = RivalRushManager.run(player);
                List<Mob> actors = arrange(fixture); check(actors.size() >= 7, "Too few real soul reprise rivals");
                forge(player, RushBuildPaths.Evolution.SOUL_REPRISE);
                Vec3 base = player.position(); Mob captain = actors.get(0), blocked = actors.get(1);
                place(captain, base, 3, 0); place(blocked, base, -3, 0); wall(player, -1, -1, -2, 2);
                float nativePlayerHealth = player.getHealth(), nativeCaptainHealth = captain.getHealth();
                for (int index = 2; index < 7; index++) {
                    Mob target = actors.get(index);
                    for (int hit = 0; hit < 5 && !target.isRemoved(); hit++)
                        check(RivalRushManager.artifactHit(player, target, 40), "Could not defeat the actual soul-charge target");
                    check(target.isRemoved() && !RivalRushManager.artifactHit(player, target, 40), "An owned defeat settled twice");
                }
                check(run.soulBurstCharge() == 5 && run.soulCharge() == 5 && !run.hasOffer(),
                        "Five real defeats did not create independent offensive/healing reservoirs");
                RivalRushManager.hurt(player, 4);
                double before = RivalRushManager.actorVitality(player, captain), blockedBefore = RivalRushManager.actorVitality(player, blocked);
                double restoredBefore = run.vitalityRestored();
                RivalRushManager.tick(player.getServer());
                check(near(before - RivalRushManager.actorVitality(player, captain), 13)
                                && near(blockedBefore, RivalRushManager.actorVitality(player, blocked))
                                && run.soulBurstCharge() == 0 && run.soulCharge() == 5 && run.tempoTicks() == 60
                                && near(run.vitalityRestored() - restoredBefore, 4) && near(run.vitality(), run.maxVitality())
                                && run.evolutionProcs(RushBuildPaths.Evolution.SOUL_REPRISE) == 1,
                        "Accepted Soul reprise crossed a wall, spent healing souls, over-healed or missed its tempo");
                RivalRushManager.tick(player.getServer());
                check(run.evolutionProcs(RushBuildPaths.Evolution.SOUL_REPRISE) == 1 && run.tempoTicks() == 59
                                && player.getHealth() == nativePlayerHealth && captain.getHealth() == nativeCaptainHealth,
                        "An empty reservoir replayed the evolution or altered native health");
                UUID runId = run.id(); restored(fixture);
                CompoundTag report = RivalRushManager.lastDebrief(player);
                check(report.getUUID("Run").equals(runId) && report.getCompound("Evolution").getString("Id").equals("SOUL_REPRISE")
                                && report.getCompound("Evolution").getLong("Procs") == 1,
                        "Returning lost the accepted expanded evolution's debrief");
                pass(helper, "rush_expanded_evolution_reprise");
            } finally { cleanup(fixture); }
        });
    }

    @GameTest(template = "hoverboard_empty", timeoutTicks = 500)
    public static void soulBurstFinishesItsAcceptedPulseBeforePausingFurtherArtifacts(GameTestHelper helper) {
        Fixture fixture = begin(helper, "JSC_burst_pause", 8);
        helper.runAfterDelay(2, () -> {
            try {
                ServerPlayer player = fixture.player(); RushRun run = RivalRushManager.run(player);
                List<Mob> actors = arrange(fixture); check(actors.size() >= 8, "Too few real pulse-pause targets");
                forge(player, RushBuildPaths.Evolution.SOUL_REPRISE);
                grant(run, RushRun.Upgrade.EXPOSE, 1);
                Vec3 base = player.position();
                Mob captain = actors.get(0), receiver = actors.get(1), control = actors.get(2);
                // Five actual ordinary defeats charge the burst without granting a draft or touching its later targets.
                for (int index = 3; index < 8; index++) {
                    Mob target = actors.get(index);
                    for (int hit = 0; hit < 5 && !target.isRemoved(); hit++)
                        check(RivalRushManager.artifactHit(player, target, 40), "Could not charge the pulse from an owned defeat");
                    check(target.isRemoved(), "The pulse fixture did not earn its fifth soul");
                }
                check(run.soulBurstCharge() == 5 && !run.hasOffer(), "The pulse fixture did not earn exactly five souls");
                for (int hit = 0; hit < 16 && RivalRushManager.actorVitality(player, captain) > 8.000001; hit++) {
                    double damage = Math.min(40, (RivalRushManager.actorVitality(player, captain) - 8) / run.damageMultiplier());
                    check(RivalRushManager.artifactHit(player, captain, damage), "Could not prime the real captain for a final pulse");
                }
                check(near(RivalRushManager.actorVitality(player, captain), 8), "The bounded fixture did not leave its captain alive for the pulse");
                place(captain, base, 1.5, 0); place(receiver, base, 4, 0); place(control, base, 11, 0);
                double receiverBefore = RivalRushManager.actorVitality(player, receiver);
                double controlBefore = RivalRushManager.actorVitality(player, control);
                check(receiverBefore > 13, "The pulse receiver cannot survive its bounded damage");
                float nativeHealth = receiver.getHealth();
                int liveBefore = actors(player).size();
                // Make ordinary director admission due; the newly earned choice must stop it in the same tick.
                for (int tick = 0; tick < 40; tick++) run.tickActive();
                // Explicit XP-threshold fixture: the real captain defeat must earn the paused draft.
                run.gainXp((int) Math.max(0, run.xpToNextLevel() - run.xp() - 1));
                check(!run.hasOffer(), "The fixture itself opened the XP draft");
                RivalRushManager.tick(player.getServer());
                CompoundTag build = RivalRushManager.buildSnapshot(player);
                check(captain.isRemoved() && run.hasOffer()
                                && near(receiverBefore - RivalRushManager.actorVitality(player, receiver), 13)
                                && near(controlBefore, RivalRushManager.actorVitality(player, control))
                                && build.getLong("SoulBursts") == 1 && build.getLong("MarkApplications") == 0
                                && actors(player).size() == liveBefore - 1
                                && run.evolutionProcs(RushBuildPaths.Evolution.SOUL_REPRISE) == 1,
                        "A captain XP draft clipped its accepted pulse or allowed later artifact/spawn work during the pause");
                long pausedAt = run.elapsedTicks(); double pausedVitality = RivalRushManager.actorVitality(player, receiver);
                RivalRushManager.tick(player.getServer());
                check(run.elapsedTicks() == pausedAt && near(pausedVitality, RivalRushManager.actorVitality(player, receiver))
                                && RivalRushManager.buildSnapshot(player).getLong("MarkApplications") == 0
                                && receiver.getHealth() == nativeHealth,
                        "An unresolved earned draft advanced combat or changed native mob health");
                restored(fixture); pass(helper, "rush_expanded_evolution_pulse_pause");
            } finally { cleanup(fixture); }
        });
    }

    private static void grant(RushRun run, RushRun.Upgrade upgrade, int ranks) {
        for (int rank = 0; rank < ranks; rank++) check(run.grantReward(upgrade), "Could not prepare " + upgrade.name());
    }

    private static void forge(ServerPlayer player, RushBuildPaths.Evolution evolution) {
        RushRun run = RivalRushManager.run(player);
        for (RushRun.Upgrade upgrade : evolution.requirements()) grant(run, upgrade, 3);
        // Earlier encounter tests establish headliner earning; this fixture isolates new recipe packets and real effects.
        run.grantHeadlinerReward();
        int version = run.offerVersion();
        C2SRushActionPacket.handleForPlayer(new C2SRushActionPacket("evolve", evolution.name(), 0, run.id(), version), player);
        check(run.selectedEvolution() == RushBuildPaths.Evolution.NONE, "Combat accepted a recipe outside a decision window");
        run.pauseForEncore();
        C2SRushActionPacket.handleForPlayer(new C2SRushActionPacket("evolve", evolution.name(), 0, UUID.randomUUID(), version), player);
        check(run.selectedEvolution() == RushBuildPaths.Evolution.NONE, "A foreign run forged a recipe");
        C2SRushActionPacket.handleForPlayer(new C2SRushActionPacket("evolve", evolution.name(), 0, run.id(), version), player);
        check(run.evolutionActive(evolution) && !run.hasBuildOpportunity(), "The valid new recipe packet was rejected");
        C2SRushActionPacket.handleForPlayer(new C2SRushActionPacket("encore", "", 0, run.id(), run.offerVersion()), player);
        check(!run.awaitingEncore() && !run.hasOffer(), "Could not resume the forged recipe fixture");
    }

    private record Fixture(ServerPlayer player, ServerLevel source, Vec3 returnPosition, ListTag inventory,
                           CompoundTag ride, ServerLevel district, ChunkPos chunk) { }

    private static Fixture begin(GameTestHelper helper, String name, int requiredActors) {
        ServerPlayer player = FakePlayerFactory.get(helper.getLevel(), new GameProfile(UUID.randomUUID(), name));
        player.setGameMode(GameType.SURVIVAL); player.getInventory().clearContent(); player.containerMenu.setCarried(ItemStack.EMPTY);
        BlockPos source = helper.absolutePos(new BlockPos(1, 2, 1));
        player.moveTo(source.getX() + .5, source.getY(), source.getZ() + .5, 0, 0);
        player.getInventory().setItem(0, new ItemStack(Items.EMERALD, 4)); player.experienceLevel = 9; player.totalExperience = 41;
        ListTag inventory = player.getInventory().save(new ListTag());
        CompoundTag ride = player.getCapability(JetSetDataProvider.CAPABILITY).resolve().orElseThrow().save();
        Vec3 position = player.position();
        check(RivalRushManager.start(player, ResourceLocation.tryParse("jetsetcraft:dead_beat"), ResourceLocation.tryParse("minecraft:zombie"), false),
                "Could not start actual evolution session");
        ServerLevel district = player.serverLevel(); ChunkPos chunk = new ChunkPos(player.blockPosition());
        Fixture fixture = new Fixture(player, helper.getLevel(), position, inventory, ride, district, chunk);
        district.getChunkSource().addRegionTicket(TICKET, chunk, 4, player.getUUID());
        try {
            for (int x = -2; x <= 2; x++) for (int z = -2; z <= 2; z++) district.getChunk(chunk.x + x, chunk.z + z);
            RushRun run = RivalRushManager.run(player);
            for (int tick = 0; tick < 3001; tick++) run.tickActive();
            RivalRushManager.tick(player.getServer());
            for (int step = 0; step < 14 && actors(player).size() < requiredActors; step++) {
                for (int tick = 0; tick < 40; tick++) run.tickActive();
                RivalRushManager.tick(player.getServer());
            }
            return fixture;
        } catch (RuntimeException error) { cleanup(fixture); throw error; }
    }

    private static List<Mob> arrange(Fixture fixture) {
        ServerPlayer player = fixture.player(); Vec3 base = new Vec3(player.getX(), RushWorld.FLOOR_Y + 1, player.getZ());
        BlockPos floor = BlockPos.containing(base).below();
        for (int x = -11; x <= 11; x++) for (int z = -11; z <= 11; z++) {
            fixture.district().setBlock(floor.offset(x, 0, z), Blocks.STONE.defaultBlockState(), 2);
            for (int y = 1; y <= 5; y++) fixture.district().setBlock(floor.offset(x, y, z), Blocks.AIR.defaultBlockState(), 2);
        }
        player.moveTo(base.x, base.y, base.z, 0, 0); player.setOnGround(true); player.setDeltaMovement(Vec3.ZERO);
        for (Projectile shot : fixture.district().getEntitiesOfClass(Projectile.class, player.getBoundingBox().inflate(100),
                entity -> RivalRushManager.belongsTo(entity, player))) shot.discard();
        List<Mob> actors = actors(player);
        check(!actors.isEmpty() && RivalRushManager.actorVitality(player, actors.get(0)) >= 200, "Actual director captain fixture is missing");
        for (Mob actor : actors) place(actor, base, 40, 20);
        return actors;
    }

    private static List<Mob> actors(ServerPlayer player) {
        return player.serverLevel().getEntitiesOfClass(Mob.class, player.getBoundingBox().inflate(100),
                        mob -> RivalRushManager.belongsTo(mob, player)).stream()
                .sorted(Comparator.comparingDouble((Mob mob) -> RivalRushManager.actorVitality(player, mob)).reversed()).toList();
    }
    private static void place(Mob actor, Vec3 base, double x, double z) { actor.moveTo(base.x + x, base.y, base.z + z, 0, 0); actor.setDeltaMovement(Vec3.ZERO); }
    private static void wall(ServerPlayer player, int xFrom, int xTo, int zFrom, int zTo) {
        BlockPos floor = player.blockPosition().below();
        for (int x = Math.min(xFrom, xTo); x <= Math.max(xFrom, xTo); x++)
            for (int z = zFrom; z <= zTo; z++) for (int y = 1; y <= 3; y++)
                player.serverLevel().setBlock(floor.offset(x, y, z), Blocks.STONE.defaultBlockState(), 2);
    }
    private static void restored(Fixture fixture) {
        ServerPlayer player = fixture.player();
        check(RivalRushManager.leave(player, "Evolution acceptance return") && player.serverLevel() == fixture.source()
                        && player.position().distanceToSqr(fixture.returnPosition()) < .0001
                        && fixture.inventory().equals(player.getInventory().save(new ListTag()))
                        && fixture.ride().equals(player.getCapability(JetSetDataProvider.CAPABILITY).resolve().orElseThrow().save())
                        && player.experienceLevel == 9 && player.totalExperience == 41 && !RushReturnState.hasJournal(player),
                "Evolution return changed ordinary inventory, ride gear, XP, dimension or position");
    }
    private static void cleanup(Fixture fixture) {
        try {
            if (RivalRushManager.active(fixture.player()) || RushReturnState.hasJournal(fixture.player()))
                check(RivalRushManager.leave(fixture.player(), "Evolution GameTest cleanup"), "Evolution cleanup failed to restore player");
        } finally { fixture.district().getChunkSource().removeRegionTicket(TICKET, fixture.chunk(), 4, fixture.player().getUUID()); }
    }
    private static boolean near(double left, double right) { return Math.abs(left - right) < .000001; }
    private static void check(boolean condition, String message) { if (!condition) throw new GameTestAssertException(message); }
    private static void pass(GameTestHelper helper, String marker) { System.out.println("JETSETCRAFT_GAMETEST_PASS " + marker); helper.succeed(); }
    private RushExpandedEvolutionGameTests() { }
}
