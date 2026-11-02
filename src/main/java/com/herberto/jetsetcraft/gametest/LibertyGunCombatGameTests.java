package com.herberto.jetsetcraft.gametest;

import com.herberto.jetsetcraft.JetSetCraft;
import com.herberto.jetsetcraft.compat.LibertyTaczCompat;
import com.herberto.jetsetcraft.liberty.LibertyManager;
import com.herberto.jetsetcraft.liberty.LibertyOperation;
import com.herberto.jetsetcraft.liberty.LibertyWorld;
import com.mojang.authlib.GameProfile;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestAssertException;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.nbt.ListTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.level.TicketType;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.GameType;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.util.FakePlayerFactory;
import net.minecraftforge.event.entity.EntityJoinLevelEvent;
import net.minecraftforge.event.entity.living.LivingAttackEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.gametest.GameTestHolder;
import net.minecraftforge.gametest.PrefixGameTestTemplate;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.function.Supplier;

/** Real provider shooting -> native bullet sweep -> Forge damage -> owned mission defeat and cleanup. */
@GameTestHolder(JetSetCraft.MOD_ID)
@PrefixGameTestTemplate(false)
public final class LibertyGunCombatGameTests {
    private static final TicketType<UUID> TICKET = TicketType.create("jetsetcraft_liberty_gun_test", UUID::compareTo, 20);

    private record FixtureTicket(ServerLevel level, ChunkPos chunk, UUID owner) {}
    private static final class Fixture {
        final GameTestHelper helper;
        final ServerPlayer owner, other;
        final ListTag original;
        final Capture capture = new Capture();
        final List<FixtureTicket> tickets = new ArrayList<>();
        boolean closed;
        int elapsed, nextTick;
        Runnable next;
        Fixture(GameTestHelper helper) {
            this.helper = helper;
            owner = player(helper, "JSC_gun_owner"); other = player(helper, "JSC_gun_other");
            original = owner.getInventory().save(new ListTag());
        }
    }

    @GameTest(template = "hoverboard_empty", timeoutTicks = 350)
    public static void nativeGunfireCreditsOnlyItsOwnOperationAndIsRemovedOnReturn(GameTestHelper helper) {
        if (!LibertyTaczCompat.available()) {
            check(!Boolean.getBoolean("jetsetcraft.libertyPackExpected"),
                    "The selected native gunfire lane cannot skip its required Helldivers pack");
            System.out.println("JETSETCRAFT_GAMETEST_SKIP liberty_tacz_combat optional-provider-or-pack-absent");
            helper.succeed(); return;
        }
        Fixture f = new Fixture(helper);
        MinecraftForge.EVENT_BUS.register(f.capture);
        try {
            check(LibertyManager.start(f.owner, "neon_docks") && LibertyManager.start(f.other, "neon_docks"),
                    "Real provider loadout could not deploy the two isolated combat operations");
            loadNear(f, f.owner, f.owner.blockPosition()); loadNear(f, f.other, f.other.blockPosition());
            helper.onEachTick(() -> tickFixture(f));
            // Ordinary Forge ticks advance the production director and both pod passengers. Calling
            // LibertyManager.tick here would also advance every concurrently running test's session.
            later(f, 90, () -> {
                check(LibertyManager.operation(f.owner).phase() == LibertyOperation.Phase.ACTIVE
                                && LibertyManager.operation(f.other).phase() == LibertyOperation.Phase.ACTIVE
                                && !f.owner.isPassenger() && !f.other.isPassenger(),
                        "Native pod arrival did not reach the actual active mission phase");
                BlockPos center = LibertyManager.operation(f.owner).objectives.get(0).center();
                loadNear(f, f.owner, center);
                LibertyManager.movementDiscontinuity(f.owner);
                f.owner.moveTo(center.getX() + 4.5, LibertyWorld.FLOOR_Y + 1.1, center.getZ() + 10.5, 0, 0);
                f.owner.setDeltaMovement(Vec3.ZERO); f.owner.serverLevel().getChunkSource().move(f.owner);
                later(f, 5, () -> awaitRivals(f, 0));
            });
        } catch (RuntimeException | Error error) { cleanup(f); throw error; }
    }

    private static void awaitRivals(Fixture f, int attempts) {
        LibertyOperation operation = LibertyManager.operation(f.owner);
        check(operation != null && operation.field(), "Native combat operation ended before its defenders appeared");
        List<Mob> rivals = f.owner.serverLevel().getEntitiesOfClass(Mob.class, f.owner.getBoundingBox().inflate(65),
                mob -> mob.isAlive() && LibertyManager.belongsTo(mob, f.owner)
                        && mob.getPersistentData().getCompound(LibertyManager.OWNED).getInt("Objective") == 0);
        if (rivals.size() < 2) {
            check(attempts < 30, "The production director did not create two real mission rivals after native chunk/tick waits"
                    + "; ticks=" + operation.ticks() + "; rivals=" + rivals.size());
            later(f, 5, () -> awaitRivals(f, attempts + 1)); return;
        }
        BlockPos center = operation.objectives.get(0).center();
        Vec3 stage = Vec3.atCenterOf(center).add(8, 8, 10);
        for (int index = 0; index < 2; index++) {
            Mob rival = rivals.get(index);
            rival.setNoAi(true); rival.setNoGravity(true); rival.setTarget(null); rival.clearFire();
            rival.setPos(stage.x, stage.y, stage.z + index * 6); rival.setDeltaMovement(Vec3.ZERO);
        }
        LibertyManager.movementDiscontinuity(f.owner);
        f.owner.moveTo(stage.x - 4, stage.y, stage.z, 0, 0); f.owner.setDeltaMovement(Vec3.ZERO);
        f.owner.serverLevel().getChunkSource().move(f.owner);
        // TaCZ offsets every hitbox using current position minus xOld/yOld/zOld. A fixture
        // teleport followed immediately by a shot tests a displaced history box. Let actual
        // entity ticks settle those positions before firing; do not bypass provider collision.
        later(f, 3, () -> nativeCombat(f, operation, rivals));
    }

    private static void nativeCombat(Fixture f, LibertyOperation operation, List<Mob> rivals) {
        ServerPlayer owner = f.owner, other = f.other;
        Capture capture = f.capture;
        try {
            Mob victim = rivals.get(0), protectedRival = rivals.get(1);
            for (Mob rival : List.of(victim, protectedRival)) {
                check(rival.isAlive() && rival.position().distanceToSqr(new Vec3(rival.xOld, rival.yOld, rival.zOld)) < .000001,
                        "Native target history did not settle after the fixture placement: " + rival.position()
                                + "; old=" + new Vec3(rival.xOld, rival.yOld, rival.zOld));
                rival.clearFire();
            }
            victim.setHealth(1);
            int defeats = operation.objectives.get(0).defeats();
            int loaded = rounds(owner.getInventory().getItem(0));
            Projectile shot = shoot(owner, capture);
            check(shot.getOwner() == owner && LibertyManager.belongsTo(shot, owner)
                            && LibertyManager.validOwned(shot)
                            && !LibertyManager.belongsTo(shot, other)
                            && new ResourceLocation("tacz", "bullet").equals(ForgeRegistries.ENTITY_TYPES.getKey(shot.getType()))
                            && rounds(owner.getInventory().getItem(0)) == loaded - 1,
                    "Native gunfire did not retain its real bullet identity, admitted owner or ammunition consumption");
            check(LibertyTaczCompat.LIBERATOR.equals(shot.getClass().getMethod("getGunId").invoke(shot)),
                    "Actual gunfire produced a different gun identity");
            capture.target = victim;
            hit(shot, victim);
            check(capture.attacks.stream().anyMatch(hit -> hit.attacker() == owner && hit.direct() == shot && !hit.cancelled()),
                    "Native bullet sweep did not produce uncancelled owner/direct-bullet damage; attacks=" + capture.attacks
                            + "; targetHealth=" + victim.getHealth() + "; bulletRemoved=" + shot.isRemoved());
            check(!victim.isAlive(), "Native owner bullet reached its target but did not defeat the one-health fixture"
                    + "; targetHealth=" + victim.getHealth() + "; attacks=" + capture.attacks);
            check(operation.objectives.get(0).defeats() == defeats + 1,
                    "Native bullet death did not credit exactly one owned defender; before=" + defeats
                            + "; after=" + operation.objectives.get(0).defeats());

            // A real bullet fired in another valid operation keeps its source attribution when its
            // trajectory is placed across this fixture. The production damage hook must reject it.
            capture.target = protectedRival; capture.attacks.clear();
            float health = protectedRival.getHealth();
            Projectile foreign = shoot(other, capture);
            check(LibertyManager.belongsTo(foreign, other) && !LibertyManager.allowedDamage(protectedRival, other, foreign),
                    "A foreign mission's real bullet passed the damage admission guard");
            hit(foreign, protectedRival);
            check(protectedRival.getHealth() == health && operation.objectives.get(0).defeats() == defeats + 1
                            && capture.attacks.stream().anyMatch(hit -> hit.attacker() == other
                            && hit.direct() == foreign && hit.cancelled()),
                    "Native cross-session collision damaged a rival or earned another player's mission progress");
            foreign.discard();

            Projectile inFlight = shoot(owner, capture);
            check(!inFlight.isRemoved() && LibertyManager.belongsTo(inFlight, owner), "Cleanup bullet never entered its mission");
            check(LibertyManager.leave(owner, false, "Native gunfire GameTest return") && inFlight.isRemoved()
                            && !LibertyManager.active(owner) && !LibertyManager.hasReturn(owner)
                            && f.original.equals(owner.getInventory().save(new ListTag())),
                    "Returning retained a native bullet, session authority or loaned loadout");
            System.out.println("JETSETCRAFT_GAMETEST_PASS liberty_tacz_combat real_shoot_sweep_damage_credit_isolation_return");
            cleanup(f); f.helper.succeed();
        } catch (ReflectiveOperationException error) {
            throw new GameTestAssertException("Pinned TaCZ native combat API failed: " + error + "; cause=" + error.getCause());
        }
    }

    private static Projectile shoot(ServerPlayer player, Capture capture) throws ReflectiveOperationException {
        player.getInventory().selected = 0;
        ItemStack gun = player.getMainHandItem();
        Class<?> api = Class.forName("com.tacz.guns.api.entity.IGunOperator");
        Object operator = api.getMethod("fromLivingEntity", LivingEntity.class).invoke(null, player);
        api.getMethod("initialData").invoke(operator);
        api.getMethod("draw", Supplier.class).invoke(operator, (Supplier<ItemStack>) () -> gun);
        Object holder = api.getMethod("getDataHolder").invoke(operator);
        // Skip only the fixture's draw animation delay; shoot still performs the provider's real
        // ammo checks, event, projectile construction and inventory consumption.
        holder.getClass().getField("drawTimestamp").setLong(holder, -1L);
        int before = capture.shots.size();
        Object result = api.getMethod("shoot", Supplier.class, Supplier.class).invoke(operator,
                (Supplier<Float>) () -> 0F, (Supplier<Float>) () -> -90F);
        check(result instanceof Enum<?> value && value.name().equals("SUCCESS"), "Native shooting failed: " + result);
        List<Projectile> spawned = capture.shots.subList(before, capture.shots.size()).stream()
                .filter(shot -> shot.getOwner() == player).toList();
        check(spawned.size() == 1, "One actual Liberator shot did not create exactly one native projectile");
        return spawned.get(0);
    }

    private static int rounds(ItemStack stack) throws ReflectiveOperationException {
        Class<?> gun = Class.forName("com.tacz.guns.api.item.IGun");
        int magazine = ((Number) gun.getMethod("getCurrentAmmoCount", ItemStack.class).invoke(stack.getItem(), stack)).intValue();
        boolean chambered = (Boolean) gun.getMethod("hasBulletInBarrel", ItemStack.class).invoke(stack.getItem(), stack);
        return magazine + (chambered ? 1 : 0);
    }

    private static void hit(Projectile shot, Mob target) {
        Vec3 center = target.getBoundingBox().getCenter();
        shot.setPos(center.x - 2, center.y, center.z);
        shot.setDeltaMovement(3, 0, 0);
        // This is the native EntityKineticBullet tick/sweep, not a synthetic damage event or hurt call.
        shot.tick();
    }

    public record Attack(Entity attacker, Entity direct, boolean cancelled) {}
    public static final class Capture {
        final List<Projectile> shots = new ArrayList<>();
        final List<Attack> attacks = new ArrayList<>();
        LivingEntity target;
        @SubscribeEvent(priority = EventPriority.LOWEST)
        public void join(EntityJoinLevelEvent event) {
            if (event.getEntity() instanceof Projectile projectile
                    && projectile.getClass().getName().equals("com.tacz.guns.entity.EntityKineticBullet")) shots.add(projectile);
        }
        @SubscribeEvent(priority = EventPriority.LOWEST, receiveCanceled = true)
        public void attack(LivingAttackEvent event) {
            if (event.getEntity() == target) attacks.add(new Attack(event.getSource().getEntity(),
                    event.getSource().getDirectEntity(), event.isCanceled()));
        }
    }

    private static void loadNear(Fixture f, ServerPlayer player, BlockPos center) {
        ChunkPos ticket = new ChunkPos(center);
        f.tickets.add(new FixtureTicket(player.serverLevel(), ticket, player.getUUID()));
        player.serverLevel().getChunkSource().addRegionTicket(TICKET, ticket, 4, player.getUUID());
        int x = center.getX() >> 4, z = center.getZ() >> 4;
        for (int dx = -3; dx <= 3; dx++) for (int dz = -3; dz <= 3; dz++) player.serverLevel().getChunk(x + dx, z + dz);
    }
    private static ServerPlayer player(GameTestHelper helper, String name) {
        ServerPlayer player = FakePlayerFactory.get(helper.getLevel(), new GameProfile(UUID.randomUUID(), name));
        player.getInventory().clearContent(); player.setGameMode(GameType.SURVIVAL); player.setHealth(player.getMaxHealth());
        player.getInventory().setItem(0, new ItemStack(Items.DIAMOND, 7));
        BlockPos pos = helper.absolutePos(new BlockPos(1, 2, 1));
        player.moveTo(pos.getX() + .5, pos.getY(), pos.getZ() + .5, 0, 0);
        return player;
    }
    private static void later(Fixture f, int ticks, Runnable task) {
        check(f.next == null, "The native combat fixture already has a pending transition");
        f.nextTick = f.elapsed + ticks; f.next = task;
    }
    private static void tickFixture(Fixture f) {
        if (f.closed) return;
        try {
            for (FixtureTicket ticket : f.tickets)
                ticket.level().getChunkSource().addRegionTicket(TICKET, ticket.chunk(), 4, ticket.owner());
            f.elapsed++;
            if (f.next != null && f.elapsed >= f.nextTick) {
                Runnable task = f.next; f.next = null;
                // Chained waits mutate only fixture fields. Adding runAfterDelay callbacks while
                // GameTestInfo iterates its scheduled map can corrupt that iterator in 1.20.1.
                task.run();
            }
        } catch (RuntimeException | Error error) { cleanup(f); throw error; }
    }
    private static void cleanup(Fixture f) {
        if (f.closed) return;
        f.closed = true;
        MinecraftForge.EVENT_BUS.unregister(f.capture);
        try {
            for (ServerPlayer player : List.of(f.owner, f.other)) {
                if (LibertyManager.active(player) || LibertyManager.hasReturn(player))
                    check(LibertyManager.leave(player, false, "Native combat fixture cleanup"), "Could not restore a native combat fixture player");
                LibertyManager.forget(player);
            }
        } finally {
            for (FixtureTicket ticket : f.tickets)
                ticket.level().getChunkSource().removeRegionTicket(TICKET, ticket.chunk(), 4, ticket.owner());
        }
    }
    private static void check(boolean condition, String message) { if (!condition) throw new GameTestAssertException(message); }
    private LibertyGunCombatGameTests() {}
}
