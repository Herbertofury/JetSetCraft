package com.herberto.jetsetcraft.gametest;

import com.herberto.jetsetcraft.JetSetCraft;
import com.herberto.jetsetcraft.compat.LibertyTaczCompat;
import com.herberto.jetsetcraft.liberty.LibertyManager;
import com.herberto.jetsetcraft.liberty.LibertyMobSafety;
import com.herberto.jetsetcraft.liberty.LibertyOperation;
import com.mojang.authlib.GameProfile;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestAssertException;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.nbt.ListTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.level.TicketType;
import net.minecraft.world.Difficulty;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.MoverType;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.item.PrimedTnt;
import net.minecraft.world.entity.monster.Drowned;
import net.minecraft.world.entity.monster.Shulker;
import net.minecraft.world.entity.monster.Witch;
import net.minecraft.world.entity.monster.Zombie;
import net.minecraft.world.entity.projectile.ShulkerBullet;
import net.minecraft.world.entity.raid.Raid;
import net.minecraft.world.entity.raid.Raids;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.PressurePlateBlock;
import net.minecraft.world.level.block.RedStoneOreBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.util.FakePlayerFactory;
import net.minecraftforge.event.entity.EntityJoinLevelEvent;
import net.minecraftforge.event.entity.living.LivingConversionEvent;
import net.minecraftforge.event.entity.living.ZombieEvent;
import net.minecraftforge.eventbus.api.Event;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.gametest.GameTestHolder;
import net.minecraftforge.gametest.PrefixGameTestTemplate;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/** Real home deployment and native mob/contact calls; no synthetic ownership or posted fake events. */
@GameTestHolder(JetSetCraft.MOD_ID)
@PrefixGameTestTemplate(false)
public final class LibertyMobSafetyGameTests {
    private static final TicketType<UUID> TICKET = TicketType.create("jsc_liberty_mob_safety", UUID::compareTo, 40);

    @GameTest(template = "hoverboard_empty", timeoutTicks = 320)
    public static void nativeHomeActorsCannotCreateForeignChildrenOrTriggerTerrainButOrdinaryControlsRemainVanilla(GameTestHelper helper) {
        if (!LibertyTaczCompat.available()) {
            check(!Boolean.getBoolean("jetsetcraft.libertyPackExpected"), "The full lane cannot skip native home mob safety");
            System.out.println("JETSETCRAFT_GAMETEST_SKIP liberty_mob_home_safety optional-provider-or-pack-absent");
            helper.succeed(); return;
        }
        Fixture f = new Fixture(helper);
        try {
            check(LibertyMobSafety.homeGuardReady(f.owner), "Actual Minecraft home contact guard was not installed");
            var offer = LibertyNativeTerrainGameTests.illuminateHomeOffer(f.owner);
            check(LibertyManager.startDefense(f.owner, offer.id() + "|10|20"), "Real hard home defense failed its admission/preflight");
            helper.onEachTick(() -> {
                if (f.closed) return;
                try {
                    f.ticks++;
                    f.level.getChunkSource().addRegionTicket(TICKET, f.chunk, 5, f.owner.getUUID());
                    if (f.ticks == 95) {
                        var operation = LibertyManager.operation(f.owner);
                        check(operation != null && operation.phase() == LibertyOperation.Phase.ACTIVE && !f.owner.isPassenger(),
                                "Native home pod did not finish before mob safety checks");
                        List<Mob> enemies = f.level.getEntitiesOfClass(Mob.class, f.bounds,
                                mob -> LibertyManager.belongsTo(mob, f.owner) && mob.isAlive());
                        enemies.forEach(mob -> { mob.setNoAi(true); mob.setTarget(null); mob.setNoGravity(true); mob.setDeltaMovement(Vec3.ZERO); });
                        f.zombie = enemies.stream().filter(mob -> mob.getClass() == Zombie.class).map(mob -> (Zombie) mob)
                                .findFirst().orElseThrow(() -> new GameTestAssertException("Native director did not create its real Zombie infantry"));
                        f.witch = enemies.stream().filter(mob -> mob instanceof Witch).map(mob -> (Witch) mob)
                                .findFirst().orElseThrow(() -> new GameTestAssertException("Native director did not create its real Witch"));
                        f.shulkers = enemies.stream().filter(mob -> mob instanceof Shulker).map(mob -> (Shulker) mob).toList();
                        check(f.shulkers.size() >= 2, "Native difficulty-10 first wave did not create two real owned Shulkers");
                        check(enemies.stream().noneMatch(Entity::isPassenger) && !f.zombie.isBaby(),
                                "Native mission initialization retained a foreign jockey/passenger tree");
                    }
                    if (f.ticks == 100) {
                        nativeMobPaths(f);
                        nativeOwnedContacts(f);
                        check(LibertyManager.leave(f.owner, false, "Native mob safety fixture return")
                                        && f.original.equals(f.owner.getInventory().save(new ListTag())) && !LibertyManager.hasReturn(f.owner),
                                "Actual home return did not restore original belongings and settle its journal");
                        check(f.zombie.isRemoved() && f.witch.isRemoved() && f.shulkers.stream().allMatch(Entity::isRemoved),
                                "Actual return left the mission's native mobs alive");
                        nativeOrdinaryControls(f);
                        f.close();
                        System.out.println("JETSETCRAFT_LIBERTY_MOB_SAFETY_PASS native_home_actors_callbacks_reinforcements_conversion_raid_return");
                        helper.succeed();
                    }
                } catch (ReflectiveOperationException error) {
                    f.close(); throw new GameTestAssertException("Pinned native conversion fixture failed: " + error);
                } catch (RuntimeException | Error error) { f.close(); throw error; }
            });
        } catch (RuntimeException | Error error) { f.close(); throw error; }
    }

    private static void nativeMobPaths(Fixture f) throws ReflectiveOperationException {
        check(LibertyManager.validOwned(f.zombie) && LibertyManager.validOwned(f.witch), "Director actors lost their actual live ownership");
        check(LibertyMobSafety.spawnType(EntityType.SILVERFISH, true) == EntityType.CAVE_SPIDER
                        && LibertyMobSafety.spawnType(EntityType.SKELETON, true) == EntityType.STRAY
                        && LibertyMobSafety.spawnType(EntityType.SILVERFISH, false) == EntityType.SILVERFISH
                        && LibertyMobSafety.spawnType(EntityType.SKELETON, false) == EntityType.SKELETON,
                "Home spawn substitutions changed the ordinary/district species contract");
        var server = f.level.getServer(); Difficulty before = server.getWorldData().getDifficulty();
        try {
            server.setDifficulty(Difficulty.HARD, true);
            f.zombie.getAttribute(Attributes.SPAWN_REINFORCEMENTS_CHANCE).setBaseValue(1);
            f.zombie.invulnerableTime = 0;
            int joined = f.observer.added.size();
            check(f.level.getDifficulty() == Difficulty.HARD && f.zombie.hurt(f.owner.damageSources().playerAttack(f.owner), 1),
                    "The genuine hard-mode Zombie hurt path did not execute");
            check(f.observer.aid.get(f.zombie) == Event.Result.DENY
                            && f.observer.added.subList(joined, f.observer.added.size()).stream().noneMatch(entity -> entity instanceof Zombie),
                    "Owned hard-mode damage bypassed the native reinforcement denial or created a foreign child");
        } finally { server.setDifficulty(before, true); }

        Raid unregisteredRaid = new Raid(Integer.MAX_VALUE - 17, f.level, f.center);
        check(!f.witch.canJoinRaid() && !Raids.canJoinRaid(f.witch, unregisteredRaid),
                "Actual native Witch can join an unrelated vanilla raid");
        int joined = f.observer.added.size();
        beginNativeConversion(f.zombie);
        check(!f.zombie.isRemoved() && f.zombie.getType() == EntityType.ZOMBIE
                        && Boolean.TRUE.equals(f.observer.conversion.get(f.zombie))
                        && f.observer.added.subList(joined, f.observer.added.size()).stream().noneMatch(entity -> entity instanceof Drowned),
                "Native owned underwater conversion escaped its run identity into an unowned Drowned");

        Shulker shooter = f.shulkers.get(0), target = f.shulkers.get(1);
        ShulkerBullet shot = new ShulkerBullet(f.level, shooter, target, Direction.Axis.Y);
        f.transientEntities.add(shot);
        check(f.level.addFreshEntity(shot) && LibertyManager.belongsTo(shot, f.owner), "The actual native Shulker projectile was not admitted");
        float health = target.getHealth(); target.invulnerableTime = 0;
        joined = f.observer.added.size();
        check(!target.hurt(target.damageSources().mobProjectile(shot, shooter), 4) && target.getHealth() == health
                        && f.observer.added.subList(joined, f.observer.added.size()).stream().noneMatch(entity -> entity instanceof Shulker),
                "A native owned ShulkerBullet hit created an unowned clone");
        shot.discard();
    }

    private static void nativeOwnedContacts(Fixture f) {
        check(f.owner.isIgnoringBlockTriggers() && f.zombie.isIgnoringBlockTriggers(),
                "Actual protected player/mob contact guard did not suppress native trigger participation");
        walkOn(f.zombie, f.ore);
        check(!f.level.getBlockState(f.ore).getValue(RedStoneOreBlock.LIT), "Native owned mob movement lit a home ore block");
        walkOn(f.owner, f.ore);
        check(!f.level.getBlockState(f.ore).getValue(RedStoneOreBlock.LIT), "Native mission player movement lit a home ore block");
        // Invoke the actual BlockState dispatch used by movement, then the real trigger scan. These
        // are vanilla calls, not a manually posted Forge cancellation event.
        standOnPlate(f.zombie, f.plate);
        f.level.getBlockState(f.plate).entityInside(f.level, f.plate, f.zombie);
        standOnPlate(f.owner, f.plate);
        f.level.getBlockState(f.plate).entityInside(f.level, f.plate, f.owner);
        // An unrelated nonliving projectile can request the normal stone plate scan. Its scan
        // must still exclude the real owned living mob already standing in the trigger volume.
        var probe = EntityType.ARROW.create(f.level);
        check(probe != null, "Could not construct the ordinary native pressure-plate scan probe");
        probe.setNoGravity(true); standOnPlate(probe, f.plate); f.transientEntities.add(probe);
        check(f.level.addFreshEntity(probe) && !LibertyManager.owned(probe), "Ordinary native scan probe was incorrectly claimed");
        f.level.getBlockState(f.plate).entityInside(f.level, f.plate, probe);
        check(!f.level.getBlockState(f.plate).getValue(PressurePlateBlock.POWERED)
                        && f.level.getBlockState(f.tnt).is(Blocks.TNT) && f.level.getEntitiesOfClass(PrimedTnt.class, f.bounds).isEmpty(),
                "Owned home contact powered a pressure plate or primed neighboring TNT");
        probe.discard();
        f.level.random.setSeed(0);
        fallOn(f.owner, f.egg);
        check(f.level.getBlockState(f.egg).equals(f.eggState), "Native home-player falling crushed an ordinary turtle egg");
    }

    private static void nativeOrdinaryControls(Fixture f) throws ReflectiveOperationException {
        check(!LibertyMobSafety.protectedHomeActor(f.owner) && !f.owner.isIgnoringBlockTriggers(),
                "Normal return retained home-only contact immunity");
        walkOn(f.owner, f.ore);
        check(f.level.getBlockState(f.ore).getValue(RedStoneOreBlock.LIT),
                "The same returned player's real movement no longer invokes ordinary redstone-ore stepping");
        f.level.random.setSeed(0);
        for (int i = 0; i < 16 && f.level.getBlockState(f.egg).is(Blocks.TURTLE_EGG); i++) fallOn(f.owner, f.egg);
        check(!f.level.getBlockState(f.egg).is(Blocks.TURTLE_EGG), "The same returned player's native falls cannot break an ordinary egg");

        Witch witch = EntityType.WITCH.create(f.level); Zombie zombie = EntityType.ZOMBIE.create(f.level);
        check(witch != null && zombie != null, "Ordinary native controls were not constructible");
        f.transientEntities.add(witch); f.transientEntities.add(zombie);
        witch.moveTo(Vec3.atCenterOf(f.center).add(20, 1, 20)); zombie.moveTo(Vec3.atCenterOf(f.center).add(24, 1, 20));
        witch.setNoAi(true); zombie.setNoAi(true);
        check(f.level.addFreshEntity(witch) && f.level.addFreshEntity(zombie), "Ordinary native controls could not join the home world");
        witch.setCanJoinRaid(true);
        check(Raids.canJoinRaid(witch, new Raid(Integer.MAX_VALUE - 18, f.level, f.center)),
                "Home defense changed ordinary native raid eligibility after return");
        zombie.getAttribute(Attributes.SPAWN_REINFORCEMENTS_CHANCE).setBaseValue(0);
        check(zombie.hurt(zombie.damageSources().mobAttack(witch), 1) && f.observer.aid.get(zombie) == Event.Result.DEFAULT,
                "Ordinary native Zombie damage no longer reaches its normal SummonAid policy");
        beginNativeConversion(zombie);
        List<Drowned> drowned = f.observer.added.stream().filter(entity -> entity instanceof Drowned)
                .map(entity -> (Drowned) entity).filter(entity -> !entity.isRemoved()).toList();
        f.transientEntities.addAll(drowned);
        check(zombie.isRemoved() && Boolean.FALSE.equals(f.observer.conversion.get(zombie)) && !drowned.isEmpty(),
                "Ordinary native underwater conversion stopped working outside an operation");

        standOnPlate(f.owner, f.plate);
        // Fake players are not in the world's entity section index, so use a real ordinary mob for
        // the native plate's entity lookup; the same-state player callback itself stays vanilla.
        standOnPlate(witch, f.plate);
        f.level.getBlockState(f.plate).entityInside(f.level, f.plate, witch);
        List<PrimedTnt> tnt = f.level.getEntitiesOfClass(PrimedTnt.class, f.bounds);
        f.transientEntities.addAll(tnt);
        check(f.level.getBlockState(f.plate).getValue(PressurePlateBlock.POWERED)
                        && f.level.getBlockState(f.tnt).isAir() && !tnt.isEmpty(),
                "Ordinary native pressure-plate contact no longer powers its actual TNT circuit");
        tnt.forEach(Entity::discard);
    }

    private static void beginNativeConversion(Zombie zombie) throws ReflectiveOperationException {
        // Only the native conversion timer is accelerated; the real Zombie.tick/Forge conversion
        // event and entity replacement perform the behavior under test.
        Method start = Zombie.class.getDeclaredMethod("startUnderWaterConversion", int.class);
        start.setAccessible(true); start.invoke(zombie, 0);
        zombie.setNoAi(false); zombie.tick(); zombie.setNoAi(true); zombie.clearFire();
    }
    private static void walkOn(Entity actor, BlockPos ground) {
        actor.setPos(ground.getX() + .5, ground.getY() + 1.001, ground.getZ() + .5);
        actor.setDeltaMovement(Vec3.ZERO); actor.setShiftKeyDown(false);
        actor.move(MoverType.SELF, new Vec3(.04, -.12, 0));
    }
    private static void fallOn(Entity actor, BlockPos egg) {
        actor.setPos(egg.getX() + .5, egg.getY() + 2, egg.getZ() + .5);
        actor.setDeltaMovement(Vec3.ZERO); actor.setShiftKeyDown(false); actor.fallDistance = 2;
        Vec3 before = actor.position();
        actor.move(MoverType.SELF, new Vec3(0, -3, 0));
        check(actor.onGround() && actor.getOnPosLegacy().equals(egg),
                "Native fall fixture did not actually land on the turtle egg: " + actor.position() + " support=" + actor.getOnPosLegacy());
        // ServerPlayer.checkFallDamage is deliberately a no-op during Entity.move. The real
        // movement-packet path separately invokes this public method with the actual movement.
        if (actor instanceof ServerPlayer player) {
            Vec3 moved = actor.position().subtract(before);
            player.doCheckFallDamage(moved.x, moved.y, moved.z, player.onGround());
        }
    }
    private static void standOnPlate(Entity actor, BlockPos plate) {
        actor.setPos(plate.getX() + .5, plate.getY() + .0625, plate.getZ() + .5);
        actor.setDeltaMovement(Vec3.ZERO); actor.setShiftKeyDown(false);
    }

    public static final class HomeMobSafetyObserver {
        private final Fixture fixture;
        final List<Entity> added = new ArrayList<>();
        final Map<Entity, Event.Result> aid = new HashMap<>();
        final Map<Entity, Boolean> conversion = new HashMap<>();
        HomeMobSafetyObserver(Fixture fixture) { this.fixture = fixture; }
        @SubscribeEvent(priority = EventPriority.LOWEST, receiveCanceled = true)
        public void joined(EntityJoinLevelEvent event) {
            if (event.getLevel() == fixture.level && !event.isCanceled() && fixture.bounds.contains(event.getEntity().position())) added.add(event.getEntity());
        }
        @SubscribeEvent(priority = EventPriority.LOWEST, receiveCanceled = true)
        public void aid(ZombieEvent.SummonAidEvent event) { if (event.getEntity().level() == fixture.level) aid.put(event.getEntity(), event.getResult()); }
        @SubscribeEvent(priority = EventPriority.LOWEST, receiveCanceled = true)
        public void conversion(LivingConversionEvent.Pre event) { if (event.getEntity().level() == fixture.level) conversion.put(event.getEntity(), event.isCanceled()); }
    }

    private static final class Fixture {
        final ServerLevel level;
        final BlockPos center = new BlockPos(39000, 100, -39000), ore = center.offset(12, 0, 0),
                egg = center.offset(12, 1, 6), plate = center.offset(18, 1, 0), tnt = plate.east();
        final ChunkPos chunk = new ChunkPos(center);
        final AABB bounds = new AABB(center).inflate(150);
        final BlockState eggState = Blocks.TURTLE_EGG.defaultBlockState();
        final ServerPlayer owner;
        final ListTag original;
        final HomeMobSafetyObserver observer;
        final List<Entity> transientEntities = new ArrayList<>();
        Zombie zombie; Witch witch; List<Shulker> shulkers = List.of();
        int ticks; boolean closed;
        Fixture(GameTestHelper helper) {
            level = helper.getLevel();
            for (int x = -9; x <= 9; x++) for (int z = -9; z <= 9; z++) level.getChunk(chunk.x + x, chunk.z + z);
            for (int x = -128; x <= 128; x++) for (int z = -128; z <= 128; z++) level.setBlock(center.offset(x, 0, z), Blocks.STONE.defaultBlockState(), 3);
            level.setBlock(ore, Blocks.REDSTONE_ORE.defaultBlockState(), 3);
            level.setBlock(egg, eggState, 3);
            level.setBlock(plate, Blocks.STONE_PRESSURE_PLATE.defaultBlockState(), 3);
            level.setBlock(tnt, Blocks.TNT.defaultBlockState(), 3);
            owner = FakePlayerFactory.get(level, new GameProfile(UUID.randomUUID(), "JSC_home_mobs"));
            owner.setGameMode(GameType.ADVENTURE); owner.getInventory().clearContent();
            owner.getInventory().setItem(7, new ItemStack(Items.EMERALD, 17));
            owner.moveTo(center.getX() + .5, center.getY() + 1.1, center.getZ() + .5, 0, 0);
            original = owner.getInventory().save(new ListTag());
            observer = new HomeMobSafetyObserver(this); MinecraftForge.EVENT_BUS.register(observer);
            level.getChunkSource().addRegionTicket(TICKET, chunk, 5, owner.getUUID());
        }
        void close() {
            if (closed) return; closed = true;
            try {
                if (LibertyManager.active(owner) || LibertyManager.hasReturn(owner)) LibertyManager.leave(owner, false, "Native mob safety cleanup");
                LibertyManager.forget(owner);
            } finally {
                transientEntities.forEach(Entity::discard);
                level.getEntitiesOfClass(PrimedTnt.class, bounds).forEach(Entity::discard);
                level.setBlock(plate, Blocks.AIR.defaultBlockState(), 3); level.setBlock(tnt, Blocks.AIR.defaultBlockState(), 3);
                MinecraftForge.EVENT_BUS.unregister(observer);
                level.getChunkSource().removeRegionTicket(TICKET, chunk, 5, owner.getUUID());
            }
        }
    }
    private static void check(boolean condition, String message) { if (!condition) throw new GameTestAssertException(message); }
    private LibertyMobSafetyGameTests() { }
}
