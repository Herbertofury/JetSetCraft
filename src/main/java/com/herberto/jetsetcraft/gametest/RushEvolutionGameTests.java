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

/** Actual owned targets and production packet/return boundaries; compressed setup is not a human playtest. */
@GameTestHolder(JetSetCraft.MOD_ID)
@PrefixGameTestTemplate(false)
public final class RushEvolutionGameTests {
    private static final TicketType<UUID> TICKET = TicketType.create("jetsetcraft_evolution_gametest", UUID::compareTo, 500);

    @GameTest(template = "hoverboard_empty", timeoutTicks = 500)
    public static void cryoConsumesPriorStatusesAndRespectsWallsCooldownAndOwnership(GameTestHelper helper) {
        Fixture fixture = begin(helper, "JSC_cryo", 4);
        awaitActors(helper, 100, () -> {
            try {
                ServerPlayer player = fixture.player(); RushRun run = RivalRushManager.run(player);
                List<Mob> actors = arrange(fixture); check(actors.size() >= 4, "Too few real Cryo rivals");
                Mob primary = actors.get(0), nearby = actors.get(1), blocked = actors.get(2), distant = actors.get(3);
                Vec3 base = player.position();
                place(primary, base, 3, 0); place(nearby, base, 5, 1); place(blocked, base, 5, -3); place(distant, base, -8, 7);
                wall(player, 3, 3, -4, -2);
                float health = primary.getHealth(); double speed = primary.getAttributeValue(Attributes.MOVEMENT_SPEED);
                ListTag effects = primary.saveWithoutId(new CompoundTag()).getList("ActiveEffects", Tag.TAG_COMPOUND).copy();
                equipAndEvolve(player, RushBuildPaths.Evolution.CRYO_SHATTER);
                double nearBefore = RivalRushManager.actorVitality(player, nearby);
                double blockedBefore = RivalRushManager.actorVitality(player, blocked), farBefore = RivalRushManager.actorVitality(player, distant);
                check(RivalRushManager.tagEnemy(player, primary, .01) && run.evolutionProcs(RushBuildPaths.Evolution.CRYO_SHATTER) == 0,
                        "Cryo triggered before both statuses existed");
                check(RivalRushManager.tagEnemy(player, primary, .01) && run.evolutionProcs(RushBuildPaths.Evolution.CRYO_SHATTER) == 1
                                && RivalRushManager.actorVitality(player, nearby) < nearBefore
                                && near(blockedBefore, RivalRushManager.actorVitality(player, blocked))
                                && near(farBefore, RivalRushManager.actorVitality(player, distant)),
                        "Cryo failed its real burst or crossed a wall/radius boundary");
                CompoundTag build = RivalRushManager.buildSnapshot(player);
                check(build.getInt("VenomRivals") == 0 && build.getInt("ChilledRivals") == 0 && run.hits() == 2,
                        "Cryo retained consumed statuses or recursively applied primary tags");
                nearBefore = RivalRushManager.actorVitality(player, nearby);
                check(RivalRushManager.tagEnemy(player, primary, .01) && RivalRushManager.tagEnemy(player, primary, .01)
                                && run.evolutionProcs(RushBuildPaths.Evolution.CRYO_SHATTER) == 1
                                && near(nearBefore, RivalRushManager.actorVitality(player, nearby)),
                        "Cryo bypassed its active-time cooldown");
                Mob foreign = EntityType.ZOMBIE.create(player.serverLevel());
                check(foreign != null && !RivalRushManager.tagEnemy(player, foreign, 3)
                                && !RivalRushManager.artifactHit(player, foreign, 3) && run.hits() == 4,
                        "An unowned mob entered an evolution's damage path");
                foreign.discard();
                check(primary.getHealth() == health && near(primary.getAttributeValue(Attributes.MOVEMENT_SPEED), speed)
                                && effects.equals(primary.saveWithoutId(new CompoundTag()).getList("ActiveEffects", Tag.TAG_COMPOUND)),
                        "Cryo changed native mob health, attributes or effects");
                restored(fixture); pass(helper, "rush_evolution_cryo");
            } finally { cleanup(fixture); }
        }, fixture);
    }

    @GameTest(template = "hoverboard_empty", timeoutTicks = 500)
    public static void railstormRequiresChargedPrimaryAndKeepsForwardVisibleTargets(GameTestHelper helper) {
        Fixture fixture = begin(helper, "JSC_rail", 5);
        awaitActors(helper, 100, () -> {
            try {
                ServerPlayer player = fixture.player(); RushRun run = RivalRushManager.run(player);
                List<Mob> actors = arrange(fixture); check(actors.size() >= 5, "Too few real Railstorm rivals");
                Mob primary = actors.get(0), forward = actors.get(1), blocked = actors.get(2), behind = actors.get(3), side = actors.get(4);
                Vec3 base = player.position();
                place(primary, base, 3, 0); place(forward, base, 6, 1); place(blocked, base, 9, 0);
                place(behind, base, -5, 0); place(side, base, 6, 5); wall(player, 8, 8, -1, 1);
                equipAndEvolve(player, RushBuildPaths.Evolution.RAILSTORM);
                check(RivalRushManager.tagEnemy(player, primary, .01) && run.evolutionProcs(RushBuildPaths.Evolution.RAILSTORM) == 0,
                        "An uncharged primary manufactured Railstorm");
                // The real movement observer is tested separately; this fixture supplies six valid bounded distances.
                for (int step = 0; step < 6; step++) run.chargeLine(4);
                check(run.lineCharge() == run.lineChargeMax() && RivalRushManager.artifactHit(player, primary, .01)
                                && run.lineCharge() == run.lineChargeMax() && run.evolutionProcs(RushBuildPaths.Evolution.RAILSTORM) == 0,
                        "A secondary hit consumed battery charge or fired Railstorm");
                double forwardBefore = RivalRushManager.actorVitality(player, forward);
                double blockedBefore = RivalRushManager.actorVitality(player, blocked), behindBefore = RivalRushManager.actorVitality(player, behind);
                double sideBefore = RivalRushManager.actorVitality(player, side);
                check(RivalRushManager.tagEnemy(player, primary, .01) && run.lineCharge() == 0
                                && run.evolutionProcs(RushBuildPaths.Evolution.RAILSTORM) == 1 && run.hits() == 2
                                && RivalRushManager.actorVitality(player, forward) < forwardBefore
                                && near(blockedBefore, RivalRushManager.actorVitality(player, blocked))
                                && near(behindBefore, RivalRushManager.actorVitality(player, behind))
                                && near(sideBefore, RivalRushManager.actorVitality(player, side)),
                        "Railstorm missed its real target, escaped the forward visible lane, or recursively spent charge");
                forwardBefore = RivalRushManager.actorVitality(player, forward);
                check(RivalRushManager.tagEnemy(player, primary, .01) && run.evolutionProcs(RushBuildPaths.Evolution.RAILSTORM) == 1
                                && near(forwardBefore, RivalRushManager.actorVitality(player, forward)), "A spent battery replayed Railstorm");
                // A captain's real XP must pause only after the already accepted charged hit finishes its effects.
                for (int hit = 0; hit < 16 && RivalRushManager.actorVitality(player, primary) > 1.000001; hit++) {
                    double baseDamage = Math.min(40, (RivalRushManager.actorVitality(player, primary) - 1) / run.damageMultiplier());
                    check(RivalRushManager.artifactHit(player, primary, baseDamage), "Could not prepare the captain's last hit");
                }
                run.gainXp((int) Math.max(0, run.xpToNextLevel() - run.xp() - 1));
                check(!run.hasOffer(), "The pre-kill fixture opened its draft early");
                for (int step = 0; step < 6; step++) run.chargeLine(4);
                check(RivalRushManager.tagEnemy(player, primary, 7) && primary.isRemoved() && run.hasOffer()
                                && run.evolutionProcs(RushBuildPaths.Evolution.RAILSTORM) == 2
                                && RivalRushManager.actorVitality(player, forward) < forwardBefore,
                        "Captain XP paused midway through an accepted hit and suppressed its charged evolution");
                restored(fixture); pass(helper, "rush_evolution_railstorm");
            } finally { cleanup(fixture); }
        }, fixture);
    }

    @GameTest(template = "hoverboard_empty", timeoutTicks = 500)
    public static void radiantRecoveryAndReturnedDebriefUseActualAcceptedState(GameTestHelper helper) {
        Fixture fixture = begin(helper, "JSC_report", 4);
        awaitActors(helper, 100, () -> {
            try {
                ServerPlayer player = fixture.player(); RushRun run = RivalRushManager.run(player);
                List<Mob> actors = arrange(fixture); check(actors.size() >= 4, "Too few real debrief rivals");
                Mob primary = actors.get(0); place(primary, player.position(), 3, 0);
                equipAndEvolve(player, RushBuildPaths.Evolution.RADIANT_BARRIER);
                check(near(run.vitality(), run.maxVitality()) && near(run.ward(), run.maxWard()) && run.hits() == 0
                                && !run.hasOffer() && !run.awaitingEncore(),
                        "Radiance requires full vitality and ward before the accepted damage: " + preparationState(fixture));
                float nativeHealth = player.getHealth(); RivalRushManager.hurt(player, 10);
                double before = run.ward(), restoredBefore = run.wardRestored();
                for (int hit = 0; hit < 6; hit++) check(RivalRushManager.tagEnemy(player, primary, .01), "Radiance primary tag failed");
                check(near(run.ward() - before, 6) && near(run.wardRestored() - restoredBefore, 6)
                                && run.evolutionProcs(RushBuildPaths.Evolution.RADIANT_BARRIER) == 1
                                && player.getHealth() == nativeHealth && near(run.vitality(), run.maxVitality()),
                        "Real Radiance did not convert only actual overheal to bounded virtual ward; wardDelta="
                                + (run.ward() - before) + ", restoredDelta=" + (run.wardRestored() - restoredBefore)
                                + ", procs=" + run.evolutionProcs(RushBuildPaths.Evolution.RADIANT_BARRIER)
                                + ", nativeHealth=" + player.getHealth() + "/" + nativeHealth + "; " + preparationState(fixture));
                double damageBefore = run.secondaryDamage(), available = 0;
                for (int i = 1; i <= 3; i++) {
                    Mob target = actors.get(i); available += RivalRushManager.actorVitality(player, target);
                    for (int hit = 0; hit < 8 && !target.isRemoved(); hit++)
                        check(RivalRushManager.artifactHit(player, target, 40), "Could not defeat the actual owned report target");
                    check(target.isRemoved() && !RivalRushManager.artifactHit(player, target, 40), "An owned defeat settled twice");
                }
                check(near(run.secondaryDamage() - damageBefore, available), "Debrief counted attempted overkill as actual damage");
                UUID runId = run.id(); long defeats = run.defeats(); double primaryDamage = run.primaryDamage();
                restored(fixture);
                CompoundTag report = RivalRushManager.lastDebrief(player);
                check(report.hasUUID("Run") && report.getUUID("Run").equals(runId) && report.getUUID("Owner").equals(player.getUUID())
                                && report.getLong("Defeats") == defeats && near(report.getDouble("PrimaryDamage"), primaryDamage)
                                && near(report.getDouble("SecondaryDamage"), run.secondaryDamage())
                                && report.getCompound("Evolution").getString("Id").equals("RADIANT_BARRIER")
                                && report.getString("ReturnRewardStatus").equals("accepted")
                                && report.getCompound("ReturnReward").getUUID("Id").equals(runId),
                        "Returned debrief lost actual build/combat state or accepted shared settlement identity");
                report.putLong("Score", Long.MAX_VALUE);
                check(RivalRushManager.lastDebrief(player).getLong("Score") == run.score(), "Read-only results exposed mutable persisted state");
                C2SRushActionPacket.handleForPlayer(new C2SRushActionPacket("evolve", "CRYO_SHATTER", 0, runId, run.offerVersion()), player);
                check(!RivalRushManager.active(player) && RivalRushManager.lastDebrief(player).getUUID("Run").equals(runId),
                        "A post-return action created a run or altered its settled report");
                CompoundTag reloaded = player.getPersistentData().getCompound(Player.PERSISTED_NBT_TAG).getCompound(RushDebrief.KEY).copy();
                check(RushDebrief.sanitize(reloaded).equals(RivalRushManager.lastDebrief(player)), "Saved results failed bounded reload");
                pass(helper, "rush_evolution_return");
            } finally { cleanup(fixture); }
        }, fixture);
    }

    private static void equipAndEvolve(ServerPlayer player, RushBuildPaths.Evolution evolution) {
        RushRun run = RivalRushManager.run(player);
        for (RushRun.Upgrade requirement : evolution.requirements())
            for (int rank = 0; rank < 3; rank++) check(run.grantReward(requirement), "Could not prepare required evolution ranks");
        check(run.grantReward(RushRun.Upgrade.WIND), "Could not occupy the third artifact slot");
        // Encounter tests establish the real once-only victory; this fixture isolates packet and proc behavior.
        run.grantHeadlinerReward(); int version = run.offerVersion();
        C2SRushActionPacket.handleForPlayer(new C2SRushActionPacket("evolve", evolution.name(), 0, run.id(), version), player);
        check(run.selectedEvolution() == RushBuildPaths.Evolution.NONE && run.hasBuildOpportunity(), "Combat accepted an evolution outside a decision window");
        run.gainXp(run.xpToNextLevel()); version = run.offerVersion();
        long xp = run.xp(); int level = run.level();
        C2SRushActionPacket.handleForPlayer(new C2SRushActionPacket("reroll", "", 0, UUID.randomUUID(), version), player);
        check(run.rerollsRemaining() == 3 && run.offerVersion() == version, "A foreign run spent a reroll");
        C2SRushActionPacket.handleForPlayer(new C2SRushActionPacket("reroll", "", 0, run.id(), version), player);
        check(run.rerollsRemaining() == 2 && run.offerVersion() != version && run.xp() == xp && run.level() == level,
                "The production reroll action spent XP/level or failed to replace the offer");
        C2SRushActionPacket.handleForPlayer(new C2SRushActionPacket("reroll", "", 0, run.id(), version), player);
        check(run.rerollsRemaining() == 2, "A stale reroll spent twice");
        version = run.offerVersion(); RushRun.Upgrade banished = run.offers().get(0);
        C2SRushActionPacket.handleForPlayer(new C2SRushActionPacket("banish", banished.name(), 0, run.id(), version), player);
        check(run.banishesRemaining() == 1 && run.banishedDraftUpgrades().contains(banished) && !run.offers().contains(banished)
                        && run.xp() == xp && run.level() == level, "The production banish action lost earned progress or kept its excluded option");
        C2SRushActionPacket.handleForPlayer(new C2SRushActionPacket("evolve", evolution.name(), 0, run.id(), version), player);
        check(run.selectedEvolution() == RushBuildPaths.Evolution.NONE, "A stale build selection was accepted");
        version = run.offerVersion();
        C2SRushActionPacket.handleForPlayer(new C2SRushActionPacket("evolve", evolution.name(), 0, run.id(), version), player);
        check(run.evolutionActive(evolution) && !run.hasBuildOpportunity() && run.hasOffer(), "Valid evolution did not preserve the earned XP draft");
        C2SRushActionPacket.handleForPlayer(new C2SRushActionPacket("choose", run.offers().get(0).name(), 0, run.id(), run.offerVersion()), player);
        check(!run.hasOffer() && run.evolutionActive(evolution), "Could not resume the real evolved run");
    }

    private record Fixture(ServerPlayer player, ServerLevel source, Vec3 returnPosition, ListTag inventory,
                           CompoundTag ride, ServerLevel district, ChunkPos chunk, int requiredActors) { }

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
        Fixture fixture = new Fixture(player, helper.getLevel(), position, inventory, ride, district, chunk, requiredActors);
        district.getChunkSource().addRegionTicket(TICKET, chunk, 4, player.getUUID());
        try {
            for (int x = -2; x <= 2; x++) for (int z = -2; z <= 2; z++) district.getChunk(chunk.x + x, chunk.z + z);
            RushRun run = RivalRushManager.run(player);
            for (int tick = 0; tick < 3001; tick++) run.tickActive();
            RivalRushManager.tick(player.getServer());
            parkPreparingActors(fixture);
            for (int step = 0; step < 14 && !actorsReady(fixture); step++) {
                for (int tick = 0; tick < 40; tick++) run.tickActive();
                RivalRushManager.tick(player.getServer());
                parkPreparingActors(fixture);
            }
            return fixture;
        } catch (RuntimeException error) { cleanup(fixture); throw error; }
    }

    private static boolean actorsReady(Fixture fixture) {
        List<Mob> actors = actors(fixture.player());
        return actors.size() >= fixture.requiredActors() && !actors.isEmpty()
                && RivalRushManager.actorVitality(fixture.player(), actors.get(0)) >= 200;
    }

    /** Keep real admitted targets outside automatic combat while the chunk tickets become resident. */
    private static void parkPreparingActors(Fixture fixture) {
        ServerPlayer player = fixture.player();
        for (Projectile shot : fixture.district().getEntitiesOfClass(Projectile.class, player.getBoundingBox().inflate(100),
                entity -> RivalRushManager.belongsTo(entity, player))) shot.discard();
        for (Mob actor : actors(player)) {
            place(actor, player.position(), 28, 20);
            actor.setTarget(null); actor.getNavigation().stop();
        }
    }

    /** Chunk tickets and a mob count alone do not establish native actor residency or captain admission. */
    private static void awaitActors(GameTestHelper helper, int remaining, Runnable ready, Fixture... fixtures) {
        try {
            boolean available = true;
            for (Fixture fixture : fixtures) {
                parkPreparingActors(fixture);
                BlockPos center = fixture.player().blockPosition();
                for (int x : new int[]{-32, 32}) for (int z : new int[]{-32, 32})
                    available &= fixture.district().isPositionEntityTicking(center.offset(x, 0, z));
                available &= actorsReady(fixture);
            }
            if (available) {
                for (Fixture fixture : fixtures) {
                    RushRun run = RivalRushManager.run(fixture.player());
                    check(run != null && !run.hasOffer() && !run.awaitingEncore() && !run.isFinished()
                                    && run.hits() == 0 && run.defeats() == 0 && near(run.vitality(), run.maxVitality()),
                            "Automatic combat changed the prepared evolution fixture: " + preparationState(fixture));
                }
                ready.run(); return;
            }
            if (remaining <= 0) check(false, "The real director did not admit its required resident actors and captain: "
                    + preparationState(fixtures));
            helper.runAfterDelay(1, () -> awaitActors(helper, remaining - 1, ready, fixtures));
        } catch (RuntimeException error) {
            for (Fixture fixture : fixtures) cleanup(fixture);
            throw error;
        }
    }

    private static String preparationState(Fixture... fixtures) {
        StringBuilder state = new StringBuilder();
        for (Fixture fixture : fixtures) {
            ServerPlayer player = fixture.player(); RushRun run = RivalRushManager.run(player);
            state.append('[').append(player.getGameProfile().getName()).append(" active=").append(run != null);
            if (run != null) state.append(" elapsed=").append(run.elapsedTicks()).append(" offer=").append(run.offers())
                    .append(" encore=").append(run.awaitingEncore()).append(" finished=").append(run.isFinished())
                    .append(" xp=").append(run.xp()).append(" hits=").append(run.hits()).append(" defeats=").append(run.defeats())
                    .append(" vitality=").append(run.vitality()).append('/').append(run.maxVitality())
                    .append(" ward=").append(run.ward()).append('/').append(run.maxWard());
            List<Mob> actors = actors(player);
            state.append(" actors=").append(actors.size()).append('/').append(fixture.requiredActors()).append(" vitalities=")
                    .append(actors.stream().map(actor -> RivalRushManager.actorVitality(player, actor)).toList());
            BlockPos center = player.blockPosition();
            for (int x : new int[]{-32, 32}) for (int z : new int[]{-32, 32})
                state.append(" corner(").append(x).append(',').append(z).append(")=")
                        .append(fixture.district().isPositionEntityTicking(center.offset(x, 0, z)));
            state.append(']');
        }
        return state.toString();
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
    private RushEvolutionGameTests() { }
}
