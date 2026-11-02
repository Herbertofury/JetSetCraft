package com.herberto.jetsetcraft.gametest;

import com.herberto.jetsetcraft.JetSetCraft;
import com.herberto.jetsetcraft.data.JetSetDataProvider;
import com.herberto.jetsetcraft.rush.RivalRushManager;
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

/** The director supplies real owned actors; compressed run clocks do not claim a human balance playtest. */
@GameTestHolder(JetSetCraft.MOD_ID)
@PrefixGameTestTemplate(false)
public final class RushSynergyGameTests {
    private static final TicketType<UUID> TICKET = TicketType.create("jetsetcraft_synergy_gametest", UUID::compareTo, 500);

    @GameTest(template = "hoverboard_empty", timeoutTicks = 100)
    public static void wardAndSoulBuildsKeepBoundedIndependentState(GameTestHelper helper) {
        RushRun run = new RushRun(UUID.randomUUID(), UUID.randomUUID(), 1809, false, "jetsetcraft:dead_beat");
        run.configureCircuit();
        check(run.grantStartingPerk(RushRun.Upgrade.WARD) && near(run.ward(), 12)
                        && !run.grantStartingPerk(RushRun.Upgrade.RADIANCE), "Starting ward was empty or a second preparation was accepted");
        check(run.grantReward(RushRun.Upgrade.SOUL) && run.grantReward(RushRun.Upgrade.SOUL_BURST)
                        && !run.grantReward(RushRun.Upgrade.VENOM) && run.artifactCount() == 3,
                "New families bypassed the three-slot boundary");
        for (int i = 0; i < 5; i++) run.recordDefeat();
        check(run.soulCharge() == 5 && run.soulBurstCharge() == 5 && run.spendSoulBurst()
                        && run.soulCharge() == 5 && run.soulBurstCharge() == 0 && !run.spendSoulBurst(),
                "Offensive and healing soul budgets consumed each other or spent twice");
        check(!run.hurt(7) && near(run.ward(), 5) && near(run.vitality(), 100), "Ward did not absorb only virtual damage");
        for (int tick = 0; tick < 119; tick++) run.tickActive();
        check(near(run.ward(), 5), "Ward recharged before six active seconds");
        check(!run.hurt(Double.NaN) && !run.hurt(-1), "Invalid incoming damage was accepted");
        run.tickActive();
        check(near(run.ward(), 8), "Rank-one ward did not regenerate three points after its quiet interval");
        run.pauseForEncore(); long paused = run.elapsedTicks();
        for (int tick = 0; tick < 80; tick++) run.tickActive();
        check(run.elapsedTicks() == paused && near(run.ward(), 8), "Paused time recharged the ward");
        check(run.continueEndless(), "Could not resume the ward fixture");
        run.grantRemix();
        check(run.remix(RushRun.Upgrade.WARD, RushRun.Upgrade.CHILL, run.offerVersion())
                        && run.ward() == 0 && run.maxWard() == 0, "A replaced ward kept protection");
        for (int i = 0; i < 40; i++) run.recordDefeat();
        check(run.soulBurstCharge() == 20 && run.soulCharge() == 20, "Soul reservoirs exceeded their bounds");
        run.grantRemix();
        check(run.remix(RushRun.Upgrade.SOUL_BURST, RushRun.Upgrade.VENOM, run.offerVersion())
                        && run.soulBurstCharge() == 0 && !run.spendSoulBurst(), "A replaced burst kept its stored charge");
        RushRun provider = new RushRun(UUID.randomUUID(), UUID.randomUUID(), 12, false, "jetsetcraft:dead_beat");
        check(!provider.grantStartingPerk(RushRun.Upgrade.SPELL_RELAY), "Absent optional provider was granted as preparation");
        provider.enableSpellRelay(true);
        check(provider.grantStartingPerk(RushRun.Upgrade.SPELL_RELAY), "Available optional preparation was incorrectly consumed by a failed request");
        pass(helper, "rush_synergy_rules");
    }

    @GameTest(template = "hoverboard_empty", timeoutTicks = 500)
    public static void primaryInkChillAndSplitTagsDoNotMutateNativeEffects(GameTestHelper helper) {
        Fixture fixture = begin(helper, "JSC_ink", 4);
        helper.runAfterDelay(2, () -> {
            try {
                ServerPlayer player = fixture.player(); RushRun run = RivalRushManager.run(player);
                List<Mob> actors = arrange(fixture); check(actors.size() >= 4, "Director produced too few real split-tag rivals");
                Mob primary = actors.get(0), secondary = actors.get(1), behind = actors.get(2), side = actors.get(3);
                Vec3 base = player.position();
                place(primary, base, 3, 0); place(secondary, base, 6, 1); place(behind, base, -6, 0); place(side, base, 0, 7);
                float health = primary.getHealth(); double speed = primary.getAttributeValue(Attributes.MOVEMENT_SPEED);
                ListTag effects = effects(primary);
                check(run.grantReward(RushRun.Upgrade.VENOM) && run.grantReward(RushRun.Upgrade.CHILL)
                                && run.grantReward(RushRun.Upgrade.MULTISHOT), "Could not equip the three control/split families");
                double secondaryBefore = RivalRushManager.actorVitality(player, secondary);
                double behindBefore = RivalRushManager.actorVitality(player, behind), sideBefore = RivalRushManager.actorVitality(player, side);
                for (int hit = 0; hit < 4; hit++) check(RivalRushManager.tagEnemy(player, primary, .01), "Owned primary tag failed");
                CompoundTag build = RivalRushManager.buildSnapshot(player);
                check(build.getInt("VenomRivals") == 1 && build.getInt("ChilledRivals") == 1
                                && build.getLong("MultishotVolleys") == 1 && run.hits() == 4
                                && near(secondaryBefore - RivalRushManager.actorVitality(player, secondary), 3)
                                && near(behindBefore, RivalRushManager.actorVitality(player, behind))
                                && near(sideBefore, RivalRushManager.actorVitality(player, side)),
                        "Split tags escaped their forward cone, recursively applied effects, or changed primary counters");
                Mob foreign = EntityType.ZOMBIE.create(player.serverLevel());
                check(foreign != null && !RivalRushManager.tagEnemy(player, foreign, 3)
                                && !RivalRushManager.artifactHit(player, foreign, 3) && run.hits() == 4,
                        "A foreign mob entered the run's primary or secondary effect path");
                foreign.discard();
                double beforeTick = RivalRushManager.actorVitality(player, primary);
                RivalRushManager.tick(player.getServer());
                check(near(primary.getDeltaMovement().horizontalDistance(), .17 * .75),
                        "Chill did not reduce the real captain's run-directed steering");
                for (int tick = 1; tick < 10; tick++) RivalRushManager.tick(player.getServer());
                check(near(beforeTick, RivalRushManager.actorVitality(player, primary)), "Venom damaged before its first active second");
                check(RivalRushManager.tagEnemy(player, primary, .01), "Could not refresh the owned status fixture");
                long appliedAt = run.elapsedTicks(); beforeTick = RivalRushManager.actorVitality(player, primary);
                for (int tick = 0; tick < 9; tick++) RivalRushManager.tick(player.getServer());
                check(near(beforeTick, RivalRushManager.actorVitality(player, primary)), "Refreshing venom added immediate stacked damage");
                RivalRushManager.tick(player.getServer());
                check(near(beforeTick - RivalRushManager.actorVitality(player, primary), 2) && run.hits() == 5,
                        "Refreshing venom delayed its existing tick, stacked damage, or recursively triggered primary effects");
                long beforePause = run.elapsedTicks(); double pausedVitality = RivalRushManager.actorVitality(player, primary);
                run.gainXp(run.xpToNextLevel());
                for (int tick = 0; tick < 5; tick++) RivalRushManager.tick(player.getServer());
                check(run.elapsedTicks() == beforePause && near(pausedVitality, RivalRushManager.actorVitality(player, primary)),
                        "Decision time advanced a status timer or damage-over-time tick");
                check(run.choose(0, run.offerVersion()), "Could not resume the earned real draft");
                for (int tick = 0; tick < 100 && run.elapsedTicks() <= appliedAt + 80; tick++) RivalRushManager.tick(player.getServer());
                check(run.elapsedTicks() > appliedAt + 80, "The status fixture did not progress through its bounded expiry window");
                check(RivalRushManager.buildSnapshot(player).getInt("VenomRivals") == 0
                                && RivalRushManager.buildSnapshot(player).getInt("ChilledRivals") == 0
                                && !primary.getCustomName().getString().contains("Inked")
                                && !primary.getCustomName().getString().contains("Chilled"), "Expired statuses kept their control or visible names");
                check(primary.getHealth() == health && near(primary.getAttributeValue(Attributes.MOVEMENT_SPEED), speed)
                                && effects.equals(effects(primary)), "Run control or damage changed native health, movement attributes or potion effects");
                restored(fixture); pass(helper, "rush_synergy_control");
            } finally { cleanup(fixture); }
        });
    }

    @GameTest(template = "hoverboard_empty", timeoutTicks = 500)
    public static void realDefeatsFuelOneSoulBurstAndRadianceKeepsNativeHealth(GameTestHelper helper) {
        Fixture fixture = begin(helper, "JSC_soul", 7);
        helper.runAfterDelay(2, () -> {
            try {
                ServerPlayer player = fixture.player(); RushRun run = RivalRushManager.run(player);
                List<Mob> actors = arrange(fixture); check(actors.size() >= 7, "Director produced too few real soul-charge rivals");
                Mob captain = actors.get(0), shielded = actors.get(1); Vec3 base = player.position();
                place(captain, base, 3, 0); place(shielded, base, -3, 0);
                for (int index = 2; index < actors.size(); index++) place(actors.get(index), base, 20 + index, 10);
                BlockPos floor = player.blockPosition().below();
                for (int y = 1; y <= 3; y++) for (int z = -2; z <= 2; z++)
                    player.serverLevel().setBlock(floor.offset(-1, y, z), Blocks.STONE.defaultBlockState(), 2);
                float nativePlayerHealth = player.getHealth(), nativeCaptainHealth = captain.getHealth();
                ListTag nativeEffects = effects(captain);
                check(run.grantReward(RushRun.Upgrade.WARD) && run.grantReward(RushRun.Upgrade.RADIANCE)
                                && run.grantReward(RushRun.Upgrade.SOUL_BURST), "Could not equip the recovery/soul build");
                RivalRushManager.hurt(player, 20);
                check(near(run.ward(), 0) && near(run.vitality(), 92) && player.getHealth() == nativePlayerHealth,
                        "A real director hit failed virtual shield absorption or changed native health");
                for (int hit = 0; hit < 5; hit++) check(RivalRushManager.tagEnemy(player, captain, .01), "Radiance primary tag failed");
                check(near(run.vitality(), 92), "Radiance healed before its sixth primary tag");
                check(RivalRushManager.tagEnemy(player, captain, .01) && near(run.vitality(), 96)
                                && RivalRushManager.buildSnapshot(player).getLong("RadiantPulses") == 1, "Sixth primary tag did not produce one recovery pulse");
                long defeats = run.defeats();
                for (int index = 2; index < 7; index++) {
                    Mob target = actors.get(index);
                    for (int hit = 0; hit < 4 && !target.isRemoved(); hit++)
                        check(RivalRushManager.artifactHit(player, target, 40), "An owned defeat fixture could not take secondary damage");
                    check(target.isRemoved(), "Director did not settle the actual defeated rival");
                    check(!RivalRushManager.artifactHit(player, target, 40), "A defeated actor paid a second time");
                }
                check(run.defeats() == defeats + 5 && run.soulBurstCharge() == 5 && run.hits() == 6,
                        "Five actual defeats did not charge one burst independently of primary tags");
                double captainBefore = RivalRushManager.actorVitality(player, captain), shieldedBefore = RivalRushManager.actorVitality(player, shielded);
                RivalRushManager.tick(player.getServer());
                check(near(captainBefore - RivalRushManager.actorVitality(player, captain), 7)
                                && near(shieldedBefore, RivalRushManager.actorVitality(player, shielded))
                                && run.soulBurstCharge() == 0 && run.hits() == 6
                                && RivalRushManager.buildSnapshot(player).getLong("SoulBursts") == 1,
                        "Charged burst crossed the real wall, spent incorrectly or recursively triggered tags");
                RivalRushManager.tick(player.getServer());
                check(RivalRushManager.buildSnapshot(player).getLong("SoulBursts") == 1
                                && RivalRushManager.buildSnapshot(player).getLong("RadiantPulses") == 1
                                && captain.getHealth() == nativeCaptainHealth && player.getHealth() == nativePlayerHealth
                                && nativeEffects.equals(effects(captain)), "Uncharged/replayed effects fired or changed native combat state");
                restored(fixture); pass(helper, "rush_synergy_recovery");
            } finally { cleanup(fixture); }
        });
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
        check(RivalRushManager.start(player, id("jetsetcraft:dead_beat"), id("minecraft:zombie"), false), "Could not start actual synergy session");
        ServerLevel district = player.serverLevel(); ChunkPos chunk = new ChunkPos(player.blockPosition());
        Fixture fixture = new Fixture(player, helper.getLevel(), position, inventory, ride, district, chunk);
        district.getChunkSource().addRegionTicket(TICKET, chunk, 4, player.getUUID());
        try {
            for (int x = -2; x <= 2; x++) for (int z = -2; z <= 2; z++) district.getChunk(chunk.x + x, chunk.z + z);
            RushRun run = RivalRushManager.run(player);
            for (int tick = 0; tick < 3001; tick++) run.tickActive();
            RivalRushManager.tick(player.getServer());
            // The real spawn director still checks pressure, provider eligibility, world geometry and ownership.
            for (int step = 0; step < 14 && actors(player).size() < requiredActors; step++) {
                for (int tick = 0; tick < 40; tick++) run.tickActive();
                RivalRushManager.tick(player.getServer());
            }
            return fixture;
        } catch (RuntimeException error) { cleanup(fixture); throw error; }
    }

    private static List<Mob> arrange(Fixture fixture) {
        ServerPlayer player = fixture.player();
        Vec3 base = new Vec3(player.getX(), RushWorld.FLOOR_Y + 1, player.getZ());
        BlockPos floor = BlockPos.containing(base).below();
        for (int x = -10; x <= 10; x++) for (int z = -10; z <= 10; z++) {
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
    private static void place(Mob actor, Vec3 base, double x, double z) {
        actor.moveTo(base.x + x, base.y, base.z + z, 0, 0); actor.setDeltaMovement(Vec3.ZERO);
    }
    private static ListTag effects(Mob mob) { return mob.saveWithoutId(new CompoundTag()).getList("ActiveEffects", Tag.TAG_COMPOUND).copy(); }
    private static void restored(Fixture fixture) {
        ServerPlayer player = fixture.player();
        check(RivalRushManager.leave(player, "Synergy acceptance return") && player.serverLevel() == fixture.source()
                        && player.position().distanceToSqr(fixture.returnPosition()) < .0001
                        && fixture.inventory().equals(player.getInventory().save(new ListTag()))
                        && fixture.ride().equals(player.getCapability(JetSetDataProvider.CAPABILITY).resolve().orElseThrow().save())
                        && player.experienceLevel == 9 && player.totalExperience == 41 && !RushReturnState.hasJournal(player),
                "Synergy return changed ordinary inventory, ride gear, XP, dimension or position");
    }
    private static void cleanup(Fixture fixture) {
        try {
            if (RivalRushManager.active(fixture.player()) || RushReturnState.hasJournal(fixture.player()))
                check(RivalRushManager.leave(fixture.player(), "Synergy GameTest cleanup"), "Synergy cleanup failed to restore player");
        } finally { fixture.district().getChunkSource().removeRegionTicket(TICKET, fixture.chunk(), 4, fixture.player().getUUID()); }
    }
    private static ResourceLocation id(String value) { return ResourceLocation.tryParse(value); }
    private static boolean near(double left, double right) { return Math.abs(left - right) < .000001; }
    private static void check(boolean condition, String message) { if (!condition) throw new GameTestAssertException(message); }
    private static void pass(GameTestHelper helper, String marker) { System.out.println("JETSETCRAFT_GAMETEST_PASS " + marker); helper.succeed(); }
    private RushSynergyGameTests() { }
}
