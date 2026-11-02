package com.herberto.jetsetcraft.gametest;

import com.herberto.jetsetcraft.JetSetCraft;
import com.herberto.jetsetcraft.data.JetSetDataProvider;
import com.herberto.jetsetcraft.rush.RivalRushManager;
import com.herberto.jetsetcraft.rush.RushMotionTracker;
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
import java.util.UUID;

/** Real server movement observations and owned actors; shortened fixture clocks are not a human playtest. */
@GameTestHolder(JetSetCraft.MOD_ID)
@PrefixGameTestTemplate(false)
public final class RushBuildGameTests {
    private static final TicketType<UUID> FIXTURE_TICKET = TicketType.create("jetsetcraft_build_gametest", UUID::compareTo, 500);

    @GameTest(template = "hoverboard_empty", timeoutTicks = 100)
    public static void movementChargesAndRankPreservingRemixesRejectStaleAndFalseActions(GameTestHelper helper) {
        RushRun run = new RushRun(UUID.randomUUID(), UUID.randomUUID(), 9901, false, "jetsetcraft:dead_beat");
        run.configureCircuit();
        check(run.grantReward(RushRun.Upgrade.BATTERY) && run.grantReward(RushRun.Upgrade.BATTERY)
                        && run.grantReward(RushRun.Upgrade.AIR_BREAK) && run.grantReward(RushRun.Upgrade.EXPOSE),
                "Could not earn the three movement/priority artifact families");
        check(!run.grantReward(RushRun.Upgrade.CHAIN) && run.artifactCount() == 3,
                "The new artifact families bypassed the three-slot limit");
        check(!run.chargeLine(Double.NaN) && !run.chargeLine(Double.POSITIVE_INFINITY) && !run.chargeLine(4.01)
                        && !run.chargeLine(0) && run.lineCharge() == 0,
                "Invalid, idle or discontinuous movement produced line charge");
        for (int step = 0; step < 6; step++) run.chargeLine(4);
        check(run.lineCharge() == 24 && !run.chargeLine(4) && near(run.consumeLineMultiplier(), 1.65)
                        && run.lineCharge() == 0 && run.consumeLineMultiplier() == 1,
                "A bounded rank-two charge was lost, overfilled or consumed twice");
        for (int step = 0; step < 6; step++) run.chargeLine(4);
        check(!run.remix(RushRun.Upgrade.BATTERY, RushRun.Upgrade.CHAIN, run.offerVersion()), "A remix was available before an arena clear");
        run.grantRemix(); int oldRoom = run.offerVersion(); run.grantRemix();
        check(!run.remix(RushRun.Upgrade.BATTERY, RushRun.Upgrade.CHAIN, oldRoom) && run.remixReady(),
                "A queued earlier-room remix spent a new room's permission");
        int version = run.offerVersion();
        check(!run.remix(RushRun.Upgrade.BATTERY, RushRun.Upgrade.BATTERY, version)
                        && !run.remix(RushRun.Upgrade.BATTERY, RushRun.Upgrade.AIR_BREAK, version)
                        && !run.remix(RushRun.Upgrade.FORCE, RushRun.Upgrade.CHAIN, version) && run.remixReady(),
                "A no-op, duplicate family or invalid source spent the remix");
        check(run.remix(RushRun.Upgrade.BATTERY, RushRun.Upgrade.CHAIN, version)
                        && run.rank(RushRun.Upgrade.CHAIN) == 2 && run.rank(RushRun.Upgrade.BATTERY) == 0
                        && run.lineCharge() == 0 && run.artifactCount() == 3 && !run.remixReady()
                        && !run.remix(RushRun.Upgrade.CHAIN, RushRun.Upgrade.BATTERY, version),
                "A remix lost invested ranks, retained charge, added a slot or paid twice");
        run.grantRemix();
        check(!run.remix(RushRun.Upgrade.CHAIN, RushRun.Upgrade.SPELL_RELAY, run.offerVersion()) && run.remixReady(),
                "An unavailable native provider became a remix target");
        run.enableSpellRelay(true);
        check(run.remix(RushRun.Upgrade.CHAIN, RushRun.Upgrade.SPELL_RELAY, run.offerVersion())
                        && run.rank(RushRun.Upgrade.SPELL_RELAY) == 2, "An available relay lost transferred ranks");
        run.grantRemix(); run.gainXp(run.xpToNextLevel());
        check(!run.remix(RushRun.Upgrade.SPELL_RELAY, RushRun.Upgrade.BATTERY, run.offerVersion()) && run.remixReady(),
                "A remix bypassed an outstanding earned XP draft");
        run.finish();
        check(!run.remixReady() && !run.remix(RushRun.Upgrade.SPELL_RELAY, RushRun.Upgrade.BATTERY, run.offerVersion()),
                "A finished run retained remix authority");

        RushMotionTracker motion = new RushMotionTracker();
        motion.reset(Vec3.ZERO);
        check(motion.sample(Vec3.ZERO, true, true, true).ridingDistance() == 0, "Standing still counted as riding");
        for (int tick = 1; tick <= 6; tick++) motion.sample(new Vec3(tick * .3, .8, 0), false, true, true);
        check(motion.airborneReady() && motion.sample(new Vec3(2.1, 0, 0), true, true, true).landed()
                        && !motion.sample(new Vec3(2.1, 0, 0), true, true, true).landed(),
                "A genuine moving airborne interval did not create exactly one landing");
        motion.sample(new Vec3(100, 30, 100), false, true, true);
        check(!motion.airborneReady() && !motion.sample(new Vec3(100, 30, 100), true, true, true).landed(),
                "A teleport manufactured a landing");
        motion.reset(Vec3.ZERO);
        for (int tick = 1; tick <= 8; tick++) motion.sample(new Vec3(tick * .3, 0, 0), false, true, true);
        check(!motion.sample(new Vec3(2.7, 0, 0), true, true, true).landed(), "Ground-height flag jitter armed a landing");
        motion.reset(Vec3.ZERO);
        for (int tick = 1; tick <= 8; tick++) motion.sample(new Vec3(tick * .3, .8, 0), false, true, false);
        check(!motion.sample(new Vec3(2.7, 0, 0), true, true, true).landed(), "Travel-phase air time armed a combat landing");
        motion.reset(Vec3.ZERO);
        for (int tick = 1; tick <= 8; tick++) motion.sample(new Vec3(tick * .3, .8, 0), false, false, true);
        check(!motion.sample(new Vec3(2.7, 0, 0), true, true, true).landed(), "Non-riding movement armed a landing");
        pass(helper, "rush_build_rules");
    }

    @GameTest(template = "hoverboard_empty", timeoutTicks = 500)
    public static void realRidingLandingMarksAndPrimaryHitsPreserveNativeState(GameTestHelper helper) {
        ServerPlayer player = FakePlayerFactory.get(helper.getLevel(), new GameProfile(UUID.randomUUID(), "JSC_build"));
        player.setGameMode(GameType.SURVIVAL); player.getInventory().clearContent(); player.containerMenu.setCarried(ItemStack.EMPTY);
        BlockPos source = helper.absolutePos(new BlockPos(1, 2, 1));
        player.moveTo(source.getX() + .5, source.getY(), source.getZ() + .5, 0, 0);
        player.getInventory().setItem(0, new ItemStack(Items.DIAMOND, 3)); player.experienceLevel = 11; player.totalExperience = 47;
        ListTag inventory = player.getInventory().save(new ListTag());
        CompoundTag ride = player.getCapability(JetSetDataProvider.CAPABILITY).resolve().orElseThrow().save();
        Vec3 original = player.position();
        check(RivalRushManager.start(player, id("jetsetcraft:dead_beat"), id("minecraft:zombie"), false), "Could not start actual build fixture");
        ServerLevel district = player.serverLevel(); ChunkPos center = new ChunkPos(player.blockPosition());
        district.getChunkSource().addRegionTicket(FIXTURE_TICKET, center, 4, player.getUUID());
        try {
            for (int x = -2; x <= 2; x++) for (int z = -2; z <= 2; z++) district.getChunk(center.x + x, center.z + z);
            RushRun run = RivalRushManager.run(player);
            // Only the fixture clock is compressed to obtain a real high-vitality director captain.
            for (int tick = 0; tick < 3001; tick++) run.tickActive();
            RivalRushManager.tick(player.getServer());
            helper.runAfterDelay(2, () -> {
                try {
                    verifyActualBuild(player);
                    check(RivalRushManager.leave(player, "Build acceptance return"), "Build fixture could not return");
                    check(player.serverLevel() == helper.getLevel() && player.position().distanceToSqr(original) < .0001
                                    && inventory.equals(player.getInventory().save(new ListTag()))
                                    && ride.equals(player.getCapability(JetSetDataProvider.CAPABILITY).resolve().orElseThrow().save())
                                    && player.experienceLevel == 11 && player.totalExperience == 47 && !RushReturnState.hasJournal(player),
                            "Build effects changed the restored source dimension, position, inventory, XP or ride state");
                    pass(helper, "rush_build_runtime");
                } finally { cleanup(player, district, center); }
            });
        } catch (RuntimeException error) { cleanup(player, district, center); throw error; }
    }

    private static void verifyActualBuild(ServerPlayer player) {
        RushRun run = RivalRushManager.run(player);
        check(run != null && !run.hasOffer(), "Build fixture lost its active run before validation");
        var actors = player.serverLevel().getEntitiesOfClass(Mob.class, player.getBoundingBox().inflate(80),
                mob -> RivalRushManager.belongsTo(mob, player));
        Mob target = actors.stream().max(Comparator.comparingDouble(mob -> RivalRushManager.actorVitality(player, mob))).orElseThrow();
        check(RivalRushManager.actorVitality(player, target) >= 200, "Director did not create the real captain fixture");
        Vec3 base = new Vec3(player.getX(), RushWorld.FLOOR_Y + 1, player.getZ());
        BlockPos floor = BlockPos.containing(base).below(); ServerLevel level = player.serverLevel();
        // Test-owned clear surface and headroom, independent of the optional provider's world seed.
        for (int x = -5; x <= 5; x++) for (int z = -5; z <= 5; z++) {
            level.setBlock(floor.offset(x, 0, z), Blocks.STONE.defaultBlockState(), 2);
            for (int y = 1; y <= 6; y++) level.setBlock(floor.offset(x, y, z), Blocks.AIR.defaultBlockState(), 2);
        }
        for (Mob other : actors) if (other != target) other.moveTo(base.x + 40, base.y, base.z + 20, 0, 0);
        target.moveTo(base.x + 3, base.y, base.z, 0, 0);
        player.moveTo(base.x, base.y, base.z, 0, 0); player.setOnGround(true);
        for (Projectile shot : level.getEntitiesOfClass(Projectile.class, player.getBoundingBox().inflate(80),
                entity -> RivalRushManager.belongsTo(entity, player))) shot.discard();
        check(run.grantReward(RushRun.Upgrade.BATTERY) && run.grantReward(RushRun.Upgrade.AIR_BREAK)
                        && run.grantReward(RushRun.Upgrade.EXPOSE), "Could not grant server-earned build fixture artifacts");
        float nativeHealth = target.getHealth();
        ListTag nativeEffects = target.saveWithoutId(new CompoundTag()).getList("ActiveEffects", Tag.TAG_COMPOUND).copy();
        for (int y = 1; y <= 3; y++) for (int z = -1; z <= 1; z++)
            level.setBlock(floor.offset(1, y, z), Blocks.STONE.defaultBlockState(), 2);
        RivalRushManager.tick(player.getServer());
        check(RivalRushManager.buildSnapshot(player).getInt("MarkedRivals") == 0,
                "Breakbeat mark crossed the real intervening wall");
        for (int y = 1; y <= 3; y++) for (int z = -1; z <= 1; z++)
            level.setBlock(floor.offset(1, y, z), Blocks.AIR.defaultBlockState(), 2);
        RivalRushManager.tick(player.getServer());
        check(RivalRushManager.buildSnapshot(player).getInt("MarkedRivals") == 1
                        && target.getCustomName().getString().contains("Exposed"), "Visible priority rival did not receive a readable mark");
        double before = RivalRushManager.actorVitality(player, target);
        check(RivalRushManager.artifactHit(player, target, 1)
                        && near(before - RivalRushManager.actorVitality(player, target), 1.25),
                "The mark did not multiply virtual damage exactly once");
        long pulses = RivalRushManager.buildSnapshot(player).getLong("AirBreakPulses");
        for (int tick = 1; tick <= 6; tick++) {
            player.moveTo(base.x + tick * .3, base.y + .8, base.z, 0, 0); player.setOnGround(false);
            RivalRushManager.tick(player.getServer());
        }
        check(RivalRushManager.buildSnapshot(player).getBoolean("AirborneReady"), "Actual observed moving air interval did not arm Air break");
        before = RivalRushManager.actorVitality(player, target);
        player.moveTo(base.x + 2.1, base.y, base.z, 0, 0); player.setOnGround(true);
        RivalRushManager.tick(player.getServer());
        check(RivalRushManager.buildSnapshot(player).getLong("AirBreakPulses") == pulses + 1
                        && RivalRushManager.actorVitality(player, target) < before,
                "Actual landing did not create one owned-rival pulse");
        RivalRushManager.tick(player.getServer());
        check(RivalRushManager.buildSnapshot(player).getLong("AirBreakPulses") == pulses + 1,
                "Remaining grounded repeated the landing reward");
        double charge = run.lineCharge();
        player.moveTo(base.x + 32, base.y + 10, base.z, 0, 0); player.setOnGround(false);
        RivalRushManager.tick(player.getServer());
        check(run.lineCharge() == charge && !RivalRushManager.buildSnapshot(player).getBoolean("AirborneReady"),
                "A discontinuous relocation generated charge or an armed landing");
        player.moveTo(base.x, base.y, base.z, 0, 0); player.setOnGround(true);
        RivalRushManager.tick(player.getServer());
        for (int step = 0; step < 30 && run.lineCharge() < run.lineChargeMax(); step++) {
            player.moveTo(base.x, base.y, base.z + (step % 2 == 0 ? 1 : 0), 0, 0); player.setOnGround(true);
            RivalRushManager.tick(player.getServer());
        }
        check(run.lineCharge() == run.lineChargeMax(), "Actual short riding observations did not fill the bounded line battery");
        Mob foreign = EntityType.ZOMBIE.create(level);
        check(foreign != null && !RivalRushManager.tagEnemy(player, foreign, 1) && run.lineCharge() == 24,
                "A foreign target consumed an earned charge");
        foreign.discard(); long hits = run.hits();
        check(RivalRushManager.artifactHit(player, target, 1) && run.lineCharge() == 24 && run.hits() == hits,
                "A secondary effect consumed paint charge or triggered primary hit counters");
        long procs = RivalRushManager.buildSnapshot(player).getLong("LineProcs");
        check(RivalRushManager.tagEnemy(player, target, 1) && run.lineCharge() == 0
                        && RivalRushManager.buildSnapshot(player).getLong("LineProcs") == procs + 1,
                "A valid primary tag did not spend exactly one ready charge");
        check(target.getHealth() == nativeHealth
                        && nativeEffects.equals(target.saveWithoutId(new CompoundTag()).getList("ActiveEffects", Tag.TAG_COMPOUND)),
                "Run-only marks or pulses mutated native mob health or potion effects");
        run.gainXp(run.xpToNextLevel());
        player.moveTo(base.x + 1, base.y, base.z, 0, 0); RivalRushManager.tick(player.getServer());
        check(run.hasOffer() && run.lineCharge() == 0, "An upgrade decision pause generated movement power");
    }

    private static void cleanup(ServerPlayer player, ServerLevel level, ChunkPos center) {
        try {
            if (RivalRushManager.active(player) || RushReturnState.hasJournal(player))
                check(RivalRushManager.leave(player, "Build GameTest cleanup"), "Build cleanup failed to restore player");
        } finally { level.getChunkSource().removeRegionTicket(FIXTURE_TICKET, center, 4, player.getUUID()); }
    }
    private static ResourceLocation id(String value) { return ResourceLocation.tryParse(value); }
    private static boolean near(double left, double right) { return Math.abs(left - right) < .000001; }
    private static void check(boolean condition, String message) { if (!condition) throw new GameTestAssertException(message); }
    private static void pass(GameTestHelper helper, String marker) { System.out.println("JETSETCRAFT_GAMETEST_PASS " + marker); helper.succeed(); }
    private RushBuildGameTests() { }
}
