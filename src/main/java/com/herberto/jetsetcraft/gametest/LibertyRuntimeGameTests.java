package com.herberto.jetsetcraft.gametest;

import com.herberto.jetsetcraft.JetSetCraft;
import com.herberto.jetsetcraft.compat.LibertyTaczCompat;
import com.herberto.jetsetcraft.crew.CrewHouseData;
import com.herberto.jetsetcraft.data.JetSetDataProvider;
import com.herberto.jetsetcraft.entity.GraffitiEntity;
import com.herberto.jetsetcraft.entity.LibertyTransportEntity;
import com.herberto.jetsetcraft.item.RideGearItem;
import com.herberto.jetsetcraft.item.SprayCanItem;
import com.herberto.jetsetcraft.liberty.LibertyManager;
import com.herberto.jetsetcraft.liberty.LibertyEvents;
import com.herberto.jetsetcraft.liberty.LibertyOperation;
import com.herberto.jetsetcraft.liberty.LibertyResearch;
import com.herberto.jetsetcraft.liberty.LibertyWorld;
import com.herberto.jetsetcraft.network.C2SLibertyActionPacket;
import com.herberto.jetsetcraft.registry.ModItems;
import com.herberto.jetsetcraft.rush.RivalRushManager;
import com.herberto.jetsetcraft.rush.RushReturnState;
import com.mojang.authlib.GameProfile;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestAssertException;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.NbtIo;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.level.TicketType;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.util.FakePlayerFactory;
import net.minecraftforge.common.util.FakePlayer;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.gametest.GameTestHolder;
import net.minecraftforge.gametest.PrefixGameTestTemplate;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

/**
 * Provider-present native lifecycle proofs. Ordinary Forge ticks run the director and transports.
 * Positions, owned-mob damage and documented operation-clock compression are controlled fixtures;
 * this is not a human pacing, native gunfire, connected-client or crash-durability claim.
 */
@GameTestHolder(JetSetCraft.MOD_ID)
@PrefixGameTestTemplate(false)
public final class LibertyRuntimeGameTests {
    private static final TicketType<UUID> TICKET = TicketType.create("jetsetcraft_liberty_test", UUID::compareTo, 20);

    /** Register with GameTest once: adding delayed callbacks while its timer map is iterating corrupts that map. */
    private static final class Clock {
        final GameTestHelper helper;
        Fixture fixture;
        int ticks, due;
        Runnable pending;

        Clock(GameTestHelper helper) {
            this.helper = helper;
            helper.onEachTick(this::tick);
        }

        private void tick() {
            ticks++;
            if (fixture != null && !fixture.closed) for (ChunkPos chunk : fixture.chunks)
                fixture.district.getChunkSource().addRegionTicket(TICKET, chunk, 4, fixture.player.getUUID());
            if (pending == null || ticks < due) return;
            Runnable task = pending;
            pending = null;
            task.run();
        }
    }

    private static final class Fixture {
        final Clock clock;
        final GameTestHelper helper;
        ServerPlayer player;
        final ServerLevel source;
        final Vec3 position;
        final ListTag inventory;
        final CompoundTag ride;
        final Set<ChunkPos> chunks = new LinkedHashSet<>();
        ServerLevel district;
        LibertyOperation operation;
        boolean closed;

        Fixture(Clock clock, ServerLevel source, String name) {
            this.clock = clock; this.helper = clock.helper; this.source = source;
            player = FakePlayerFactory.get(source, new GameProfile(UUID.randomUUID(), name));
            player.setGameMode(GameType.ADVENTURE); player.setHealth(17); player.setAbsorptionAmount(2);
            player.moveTo(24000.5, 96, 24000.5, 37, 12); player.setDeltaMovement(Vec3.ZERO);
            player.getInventory().clearContent(); player.containerMenu.setCarried(ItemStack.EMPTY);
            ItemStack original = new ItemStack(Items.EMERALD, 7);
            original.getOrCreateTag().putString("LibertyReturnOriginal", player.getUUID().toString());
            player.getInventory().setItem(5, original); player.getInventory().selected = 5;
            player.experienceLevel = 11; player.totalExperience = 117; player.experienceProgress = .375f;
            player.getFoodData().setFoodLevel(14); player.getFoodData().setSaturation(3);
            ItemStack skates = new ItemStack(ModItems.QUAD_SKATES.get());
            skates.getOrCreateTag().putString("LibertyPersonalSkates", "preserve this physical item");
            player.getCapability(JetSetDataProvider.CAPABILITY).ifPresent(data -> {
                data.setRideGear(skates); data.setStyle(((RideGearItem) skates.getItem()).style());
                data.setActive(false); data.setBoost(43);
            });
            CompoundTag persisted = new CompoundTag(); persisted.putString("LibertyUnrelatedFixture", "keep");
            player.getPersistentData().put(Player.PERSISTED_NBT_TAG, persisted);
            position = player.position(); inventory = player.getInventory().save(new ListTag());
            ride = player.getCapability(JetSetDataProvider.CAPABILITY).resolve().orElseThrow().save();
        }
    }

    @GameTest(template = "hoverboard_empty", timeoutTicks = 650)
    public static void fullPodDropsAbortDeathAndDisconnectRestoreOriginalWorldState(GameTestHelper helper) {
        if (skipAbsent(helper, "lifecycle")) return;
        lifecycle(new Clock(helper), 0);
    }

    private static void lifecycle(Clock clock, int exit) {
        GameTestHelper helper = clock.helper;
        ServerLevel source = exit == 1 ? helper.getLevel().getServer().getLevel(Level.NETHER)
                : exit == 2 ? helper.getLevel().getServer().getLevel(Level.END) : helper.getLevel();
        check(source != null, "The vanilla source dimension needed by the return test is unavailable");
        Fixture f = begin(clock, source, "JSC_liberty_exit" + exit);
        later(f, 90, () -> {
            landed(f);
            check(!LibertyManager.start(f.player, "neon_docks")
                            && !RivalRushManager.start(f.player, id("jetsetcraft:dead_beat"), id("minecraft:zombie"), false),
                    "An active Liberty deployment allowed another expedition or nested Rival Rush");
            check(RushReturnState.hasJournal(f.player, LibertyManager.RETURN_KEY) && !RushReturnState.hasJournal(f.player),
                    "Liberty and Rival Rush return journals were conflated");
            if (exit == 0) action(f, "abort", f.operation.id, f.operation.revision());
            else if (exit == 1) {
                // Forge FakePlayer makes native damage invulnerable and die() a no-op. Exercise the actual
                // event bus/subscriber explicitly, rather than falsely claiming a normal player death.
                LivingDeathEvent death = new LivingDeathEvent(f.player, f.player.damageSources().generic());
                MinecraftForge.EVENT_BUS.post(death);
                check(death.isCanceled(), "The production death subscriber did not intercept expedition defeat");
            } else MinecraftForge.EVENT_BUS.post(new PlayerEvent.PlayerLoggedOutEvent(f.player));
            restored(f);
            cleanup(f);
            if (exit < 2) later(f, 3, () -> lifecycle(clock, exit + 1));
            else pass(helper, "liberty_runtime_abort_death_disconnect");
        });
    }

    @GameTest(template = "hoverboard_empty", timeoutTicks = 300)
    public static void foreignAndStaleMissionActionsCannotSpendOrEndAnExpedition(GameTestHelper helper) {
        if (skipAbsent(helper, "action_identity")) return;
        Fixture f = begin(new Clock(helper), helper.getLevel(), "JSC_liberty_ids");
        later(f, 90, () -> { landed(f); rejectAction(f, 0); });
    }

    private static void rejectAction(Fixture f, int index) {
        String request = List.of("abort", "resupply", "extract", "board").get(index / 2);
        ListTag inventory = f.player.getInventory().save(new ListTag());
        int supplies = f.operation.resupplies();
        action(f, request, index % 2 == 0 ? UUID.randomUUID() : f.operation.id,
                index % 2 == 0 ? f.operation.revision() : f.operation.revision() - 1);
        check(LibertyManager.active(f.player) && LibertyManager.operation(f.player) == f.operation
                        && f.operation.phase() == LibertyOperation.Phase.ACTIVE && f.operation.resupplies() == supplies
                        && inventory.equals(f.player.getInventory().save(new ListTag())),
                "A foreign/stale " + request + " changed mission state or inventory");
        // Each request is separated by real server ticks, so rate throttling cannot mask a validation defect.
        if (index < 7) later(f, 3, () -> rejectAction(f, index + 1));
        else later(f, 3, () -> {
            action(f, "resupply", f.operation.id, f.operation.revision());
            check(f.operation.resupplies() == supplies - 1, "A current owner request could not spend a real resupply charge");
            later(f, 3, () -> {
                action(f, "abort", f.operation.id, f.operation.revision()); restored(f); cleanup(f);
                pass(f.helper, "liberty_runtime_action_identity");
            });
        });
    }

    @GameTest(template = "hoverboard_empty", timeoutTicks = 250)
    public static void unreadableResearchRestoresBelongingsAndRetainsEarnedRecoveryIntent(GameTestHelper helper) {
        if (skipAbsent(helper, "future_research_recovery")) return;
        Fixture f = begin(new Clock(helper), helper.getLevel(), "JSC_liberty_save");
        later(f, 90, () -> {
            landed(f);
            BlockPos credit = f.operation.cachePosition(0);
            load(f, credit); position(f, Vec3.atCenterOf(credit).add(0, .7, 0), true);
            later(f, 12, () -> {
                check(f.operation.cacheClaimed(0) && f.operation.credits() >= 10,
                        "The recovery fixture did not earn a real credit cache before its schema fault");
                UUID run = f.operation.id; int credits = f.operation.credits();
                CompoundTag persisted = f.player.getPersistentData().getCompound(Player.PERSISTED_NBT_TAG);
                CompoundTag validResearch = persisted.getCompound(LibertyResearch.KEY).copy();
                CompoundTag futureResearch = validResearch.copy(); futureResearch.putInt("Schema", 2);
                persisted.put(LibertyResearch.KEY, futureResearch);
                try {
                    check(LibertyResearch.snapshot(f.player).getBoolean("Blocked"), "The future-schema fault fixture was not read-only");
                    check(!LibertyManager.leave(f.player, false, "Future-schema recovery fixture"),
                            "Return reported reward settlement success despite an unreadable research account");
                    belongingsRestored(f);
                    check(!LibertyManager.active(f.player) && LibertyManager.hasReturn(f.player)
                                    && !RushReturnState.hasJournal(f.player, LibertyManager.RETURN_KEY)
                                    && persisted.contains(LibertyManager.OUTCOME_KEY)
                                    && persisted.getCompound(LibertyManager.OUTCOME_KEY).getUUID("Run").equals(run)
                                    && persisted.getCompound(LibertyManager.OUTCOME_KEY).getInt("Credits") == credits
                                    && futureResearch.equals(persisted.getCompound(LibertyResearch.KEY)),
                            "Future research was overwritten or its earned payout intent was removed instead of retained");
                } finally {
                    // Restore the exact pre-fault account so the real recovery path can settle the preserved intent.
                    f.player.getPersistentData().getCompound(Player.PERSISTED_NBT_TAG).put(LibertyResearch.KEY, validResearch);
                }
                CompoundTag mismatched = validResearch.copy(); UUID otherRun = UUID.randomUUID();
                mismatched.getCompound("Pending").putUUID("Run", otherRun);
                f.player.getPersistentData().getCompound(Player.PERSISTED_NBT_TAG).put(LibertyResearch.KEY, mismatched);
                try {
                    LibertyManager.recover(f.player);
                    check(LibertyManager.hasReturn(f.player)
                                    && f.player.getPersistentData().getCompound(Player.PERSISTED_NBT_TAG).contains(LibertyManager.OUTCOME_KEY)
                                    && LibertyResearch.pendingMission(f.player).orElseThrow().equals(otherRun)
                                    && LibertyResearch.snapshot(f.player).getInt("Credits") == 0,
                            "Recovery consumed a different mission's nonce or discarded the unmatched earned outcome");
                } finally {
                    f.player.getPersistentData().getCompound(Player.PERSISTED_NBT_TAG).put(LibertyResearch.KEY, validResearch);
                }
                LibertyManager.recover(f.player); restored(f);
                check(LibertyResearch.lastSettled(f.player, run)
                                && LibertyResearch.snapshot(f.player).getInt("Credits") == credits,
                        "A repaired account failed to settle the exact previously retained credit intent");
                cleanup(f); pass(helper, "liberty_runtime_future_research_recovery");
            });
        });
    }

    @GameTest(template = "hoverboard_empty", timeoutTicks = 250)
    public static void serializedInterruptionRecoversOnARecreatedPlayerExactlyOnce(GameTestHelper helper) {
        if (skipAbsent(helper, "serialized_rejoin")) return;
        Fixture f = begin(new Clock(helper), helper.getLevel(), "JSC_liberty_join");
        later(f, 90, () -> {
            landed(f);
            BlockPos credits = f.operation.cachePosition(0);
            load(f, credits); position(f, Vec3.atCenterOf(credits).add(0, .7, 0), true);
            later(f, 12, () -> {
                check(f.operation.cacheClaimed(0), "The serialized-rejoin fixture did not earn its actual credit cache");
                BlockPos samples = f.operation.cachePosition(3);
                load(f, samples); position(f, Vec3.atCenterOf(samples).add(0, .7, 0), true);
                later(f, 12, () -> {
                    check(f.operation.cacheClaimed(3) && f.operation.common() >= 3,
                            "The serialized-rejoin fixture did not carry provisional samples before interruption");
                    UUID run = f.operation.id; int earnedCredits = f.operation.credits();
                    CompoundTag saved = serializedPlayer(f.player);
                    JetSetCraft.LOGGER.info("JETSETCRAFT_LIBERTY_RUNTIME_FIXTURE recovery=compressed-player-NBT "
                            + "newPlayerInstance=true loginSubscriber=LibertyEvents directInvocation=true processKilled=false");
                    // Drop only volatile session state, as a stopped process would. No leave/settlement call runs here.
                    ServerPlayer disconnected = f.player;
                    LibertyManager.forget(disconnected);
                    f.district.removePlayerImmediately(disconnected, Entity.RemovalReason.UNLOADED_WITH_PLAYER);
                    f.player = new FakePlayer(f.district, disconnected.getGameProfile());
                    f.player.load(saved);
                    check(f.player != disconnected && LibertyManager.hasReturn(f.player)
                                    && RushReturnState.hasJournal(f.player, LibertyManager.RETURN_KEY)
                                    && LibertyResearch.pendingMission(f.player).orElseThrow().equals(run)
                                    && !LibertyManager.active(f.player),
                            "Full player serialization lost the interrupted return journal or armed mission identity");
                    // Forge's global tier-sorting login listener requires a real Netty channel; FakePlayer has none.
                    // Invoke the actual production Liberty subscriber without manufacturing a connected client.
                    LibertyEvents.login(new PlayerEvent.PlayerLoggedInEvent(f.player));
                    restored(f);
                    CompoundTag wallet = LibertyResearch.snapshot(f.player);
                    check(wallet.getInt("Credits") == earnedCredits && wallet.getInt("Common") == 0
                                    && wallet.getInt("Rare") == 0 && wallet.getInt("Super") == 0
                                    && LibertyResearch.lastSettled(f.player, run),
                            "Rejoin failed to retain earned credits or improperly banked unextracted samples");
                    LibertyEvents.login(new PlayerEvent.PlayerLoggedInEvent(f.player));
                    check(wallet.equals(LibertyResearch.snapshot(f.player)), "A repeated login replayed the serialized recovery payout");
                    cleanup(f); pass(helper, "liberty_runtime_serialized_rejoin");
                });
            });
        });
    }

    @GameTest(template = "hoverboard_empty", timeoutTicks = 1100)
    public static void nativeRivalsActualGraffitiAndShipExtractionSettleSharedProgress(GameTestHelper helper) {
        if (skipAbsent(helper, "extraction")) return;
        Fixture f = begin(new Clock(helper), helper.getLevel(), "JSC_liberty_full");
        JetSetCraft.LOGGER.info("JETSETCRAFT_LIBERTY_RUNTIME_FIXTURE positions=controlled nativeMobDamage=owned-player-source "
                + "spawnExtractionDepartureClocks=compressed radioAndDrop=native-ticks humanPacing=false");
        later(f, 90, () -> { landed(f); enterObjective(f, 0); });
    }

    private static void enterObjective(Fixture f, int index) {
        var objective = f.operation.objectives.get(index);
        load(f, objective.center());
        position(f, Vec3.atCenterOf(objective.center()).add(0, 1, 6), true);
        later(f, 3, () -> fight(f, index, 0));
    }

    private static void fight(Fixture f, int index, int attempts) {
        var objective = f.operation.objectives.get(index);
        check(attempts < 30, "Production director failed to supply the remaining objective rivals");
        for (Mob mob : f.district.getEntitiesOfClass(Mob.class, new AABB(objective.center()).inflate(45),
                candidate -> LibertyManager.belongsTo(candidate, f.player) && candidate.isAlive())) {
            CompoundTag marker = mob.getPersistentData().getCompound(LibertyManager.OWNED);
            if (marker.getInt("Objective") != index) continue;
            check(mob.getType() == EntityType.ZOMBIE && mob.getHealth() > 0,
                    "Neon Docks replaced its authored native zombie source body");
            int before = objective.defeats();
            check(mob.hurt(f.player.damageSources().playerAttack(f.player), 1000), "An owned native rival rejected actual player-source damage");
            check(mob.isDeadOrDying() && objective.defeats() == before + 1,
                    "The real mob death event failed to credit its exact owned objective");
        }
        if (objective.defeats() < objective.defenders()) {
            advanceClock(f.operation, 20);
            later(f, 3, () -> fight(f, index, attempts + 1)); return;
        }
        for (int panel = 0; panel < objective.panels(); panel++) paint(f, objective, panel);
        if (index == 1) radio(f, 0);
        else if (index == 2) beacon(f, 0);
        else {
            check(objective.complete() && f.operation.completed() == 1, "Real mural painting failed to liberate the first district");
            enterObjective(f, 1);
        }
    }

    private static void paint(Fixture f, LibertyOperation.Objective objective, int index) {
        BlockPos support = objective.panel(index);
        check(!f.district.getBlockState(support).isAir(), "The production objective builder did not supply its marked wall");
        position(f, Vec3.atCenterOf(support).add(0, -.4, 2), true);
        f.player.getInventory().selected = 2;
        ItemStack spray = f.player.getMainHandItem();
        check(spray.getItem() instanceof SprayCanItem, "The deployed loadout did not contain the actual spray can");
        UseOnContext context = new UseOnContext(f.player, InteractionHand.MAIN_HAND,
                new BlockHitResult(Vec3.atCenterOf(support).add(0, 0, .5), Direction.SOUTH, support, false));
        int before = objective.paints();
        check(spray.getItem().useOn(context).consumesAction() && objective.paints() == before + 1,
                "Successful native SprayCanItem placement failed to advance the designated mural panel");
        check(!f.district.getEntitiesOfClass(GraffitiEntity.class, new AABB(support).inflate(4),
                decal -> LibertyManager.belongsTo(decal, f.player) && support.equals(decal.getSupportPos())
                        && decal.getFace() == Direction.SOUTH).isEmpty(),
                "Objective credit was granted without an actual owned graffiti entity");
        spray.getItem().useOn(context);
        check(objective.paints() == before + 1, "Repainting the same actual wall farmed another objective panel");
    }

    private static void radio(Fixture f, int elapsed) {
        var objective = f.operation.objectives.get(1);
        if (objective.complete()) { enterObjective(f, 2); return; }
        check(elapsed < 240, "Two hundred native moving radio ticks did not complete the broadcast");
        position(f, Vec3.atCenterOf(objective.center()).add(elapsed % 2 + 1, .6, 6), false);
        later(f, 1, () -> radio(f, elapsed + 1));
    }

    private static void beacon(Fixture f, int index) {
        var objective = f.operation.objectives.get(2);
        Vec3 target = Vec3.atCenterOf(objective.beacon(index)).add(0, .6, 0);
        position(f, target.add(6, 0, 0), true);
        later(f, 2, () -> {
            position(f, target.add(4, 0, 0), false);
            later(f, 2, () -> {
                check(objective.relay() == index + 1, "An ordered, locally skated courier arrival was not recorded");
                if (index < 2) beacon(f, index + 1);
                else {
                    check(f.operation.complete() && f.operation.completed() == 3, "The three real production objectives did not complete");
                    cache(f, 0);
                }
            });
        });
    }

    private static void cache(Fixture f, int step) {
        int index = new int[]{0, 3, 6, 8}[step];
        BlockPos target = f.operation.cachePosition(index);
        load(f, target); position(f, Vec3.atCenterOf(target).add(0, .7, 0), true);
        later(f, 12, () -> {
            check(f.operation.cacheClaimed(index), "Approaching the real owned cache did not collect its credit/sample payload");
            if (step < 3) cache(f, step + 1);
            else {
                check(f.operation.credits() >= 34 && f.operation.common() >= 12 && f.operation.rare() >= 5 && f.operation.supers() >= 1
                                && LibertyResearch.snapshot(f.player).getInt("Common") == 0,
                        "Mission cargo omitted an earned type or banked provisional samples before extraction");
                beginExtraction(f);
            }
        });
    }

    private static void beginExtraction(Fixture f) {
        load(f, f.operation.extraction);
        position(f, Vec3.atCenterOf(f.operation.extraction).add(0, .6, 0), true);
        action(f, "extract", f.operation.id, f.operation.revision());
        check(f.operation.phase() == LibertyOperation.Phase.EXTRACTING, "The completed operation could not call extraction at the real pad");
        advanceClock(f.operation, 599);
        later(f, 3, () -> {
            check(f.operation.phase() == LibertyOperation.Phase.BOARDING && f.operation.phaseTicks() < 60,
                    "The production extraction countdown did not spawn its arriving ship");
            action(f, "board", f.operation.id, f.operation.revision());
            check(f.operation.phase() == LibertyOperation.Phase.BOARDING && !f.player.isPassenger(),
                    "A forged early board request entered the ship before it landed");
            later(f, 65, () -> {
                check(LibertyManager.snapshot(f.player).getBoolean("CanBoard"), "The landed ship did not offer boarding");
                check(!f.district.getEntitiesOfClass(LibertyTransportEntity.class,
                                new AABB(f.operation.extraction).inflate(12), entity -> entity.ship()
                                        && entity.isPickable() && LibertyManager.belongsTo(entity, f.player)).isEmpty(),
                        "The physical landed ship could not be targeted for its boarding interaction");
                action(f, "board", UUID.randomUUID(), f.operation.revision());
                check(f.operation.phase() == LibertyOperation.Phase.BOARDING && !f.player.isPassenger(), "A foreign mission boarded the landed ship");
                later(f, 3, () -> {
                    action(f, "board", f.operation.id, f.operation.revision() - 1);
                    check(f.operation.phase() == LibertyOperation.Phase.BOARDING, "A stale board request departed the ship");
                    later(f, 3, () -> finishExtraction(f));
                });
            });
        });
    }

    private static void finishExtraction(Fixture f) {
        action(f, "board", f.operation.id, f.operation.revision());
        check(f.operation.phase() == LibertyOperation.Phase.DEPARTING
                        && f.player.getVehicle() instanceof LibertyTransportEntity ship && ship.ship() && ship.tickCount > 0,
                "A current boarding request did not mount the real natively ticking extraction ship");
        int credits = f.operation.credits(), common = f.operation.common(), rare = f.operation.rare(), supers = f.operation.supers();
        UUID run = f.operation.id;
        advanceClock(f.operation, 99);
        later(f, 3, () -> {
            restored(f);
            CompoundTag wallet = LibertyResearch.snapshot(f.player);
            check(wallet.getInt("Credits") == credits && wallet.getInt("Common") == common
                            && wallet.getInt("Rare") == rare && wallet.getInt("Super") == supers,
                    "The extraction ship failed to bank the exact earned credits and all three sample tiers");
            var rewards = CrewHouseData.get(f.player.getServer()).recentRewards(f.player.getUUID());
            check(rewards.size() == 1 && rewards.get(0).id().equals(run) && rewards.get(0).source().equals("liberty_grind")
                            && rewards.get(0).crewXp() == 125 && rewards.get(0).memberXp() == 55,
                    "A full extracted mission did not contribute its once-only shared crew/Style Library rewards");
            CompoundTag debrief = f.player.getPersistentData().getCompound(Player.PERSISTED_NBT_TAG).getCompound(LibertyManager.DEBRIEF_KEY);
            check(debrief.getBoolean("Extracted") && debrief.getInt("Completed") == 3, "The saved debrief lost the actual extraction outcome");
            LibertyManager.recover(f.player); LibertyManager.recover(f.player);
            check(wallet.equals(LibertyResearch.snapshot(f.player))
                            && rewards.equals(CrewHouseData.get(f.player.getServer()).recentRewards(f.player.getUUID())),
                    "Repeated login recovery replayed a completed expedition payout");
            cleanup(f); pass(f.helper, "liberty_runtime_full_extraction");
        });
    }

    private static Fixture begin(Clock clock, ServerLevel source, String name) {
        Fixture f = new Fixture(clock, source, name);
        clock.fixture = f;
        try {
            check(LibertyManager.start(f.player, "neon_docks"), "Production entry failed: " + LibertyManager.unavailable(f.player));
            f.district = f.player.serverLevel(); f.operation = LibertyManager.operation(f.player);
            check(f.operation != null && f.operation.phase() == LibertyOperation.Phase.DROPPING
                            && LibertyWorld.isLiberty(f.district) && f.player.getVehicle() instanceof LibertyTransportEntity pod && !pod.ship()
                            && LibertyTaczCompat.isLoan(f.player.getInventory().getItem(0), f.player.getUUID())
                            && LibertyTaczCompat.isLoan(f.player.getInventory().getItem(1), f.player.getUUID()),
                    "Entry did not create a genuine passenger pod and owned Helldivers loan loadout");
            load(f, f.operation.origin);
            return f;
        } catch (RuntimeException | Error error) { cleanup(f); throw error; }
    }

    private static void landed(Fixture f) {
        check(LibertyManager.active(f.player) && f.operation.phase() == LibertyOperation.Phase.ACTIVE
                        && !f.player.isPassenger() && f.player.getY() >= LibertyWorld.FLOOR_Y + 1
                        && f.player.getY() <= LibertyWorld.FLOOR_Y + 1.6,
                "The full native drop did not safely dismount at street level: active=" + LibertyManager.active(f.player)
                        + " phase=" + f.operation.phase() + " phaseTicks=" + f.operation.phaseTicks()
                        + " operationTicks=" + f.operation.ticks() + " y=" + f.player.getY()
                        + " passenger=" + f.player.isPassenger() + " vehicle=" + f.player.getVehicle()
                        + " mission=" + f.operation.id + " owner=" + f.player.getUUID());
    }
    private static void restored(Fixture f) {
        check(!LibertyManager.active(f.player) && !LibertyManager.hasReturn(f.player)
                        && !RushReturnState.hasJournal(f.player), "A completed return retained an active session or recovery journal");
        belongingsRestored(f);
    }
    private static void belongingsRestored(Fixture f) {
        check(f.player.serverLevel() == f.source && f.player.position().distanceToSqr(f.position) < .0001 && !f.player.isPassenger()
                        && f.inventory.equals(f.player.getInventory().save(new ListTag()))
                        && f.ride.equals(f.player.getCapability(JetSetDataProvider.CAPABILITY).resolve().orElseThrow().save())
                        && f.player.gameMode.getGameModeForPlayer() == GameType.ADVENTURE
                        && f.player.experienceLevel == 11 && f.player.totalExperience == 117 && f.player.experienceProgress == .375f
                        && f.player.getHealth() == 17 && f.player.getAbsorptionAmount() == 2 && f.player.getFoodData().getFoodLevel() == 14
                        && f.player.getPersistentData().getCompound(Player.PERSISTED_NBT_TAG).getString("LibertyUnrelatedFixture").equals("keep"),
                "An expedition exit changed source world, inventory, XP, physical ride, survival state, mode or unrelated saved data");
    }
    private static void load(Fixture f, BlockPos center) {
        ChunkPos chunk = new ChunkPos(center);
        f.chunks.add(chunk); f.district.getChunkSource().addRegionTicket(TICKET, chunk, 4, f.player.getUUID());
        // Disposable fixture tickets make loaded-chunk-only production generation observable for FakePlayers.
        for (int x = -2; x <= 2; x++) for (int z = -2; z <= 2; z++) f.district.getChunk(chunk.x + x, chunk.z + z);
    }
    private static void position(Fixture f, Vec3 position, boolean discontinuity) {
        if (discontinuity) LibertyManager.movementDiscontinuity(f.player);
        f.player.moveTo(position.x, position.y, position.z, 0, 0); f.player.setDeltaMovement(Vec3.ZERO);
        f.district.getChunkSource().move(f.player);
    }
    private static void advanceClock(LibertyOperation operation, int ticks) { for (int i = 0; i < ticks; i++) operation.tick(); }
    private static CompoundTag serializedPlayer(ServerPlayer player) {
        try (ByteArrayOutputStream bytes = new ByteArrayOutputStream()) {
            NbtIo.writeCompressed(player.saveWithoutId(new CompoundTag()), bytes);
            return NbtIo.readCompressed(new ByteArrayInputStream(bytes.toByteArray()));
        } catch (IOException error) {
            throw new GameTestAssertException("Native compressed player-data serialization failed: " + error.getMessage());
        }
    }
    private static void action(Fixture f, String action, UUID mission, long revision) {
        C2SLibertyActionPacket.handleForPlayer(new C2SLibertyActionPacket(action, "", mission, revision), f.player);
    }
    private static void later(Fixture f, int ticks, Runnable task) {
        check(f.clock.pending == null, "The fixture attempted to replace an unfinished scheduled assertion");
        f.clock.due = f.clock.ticks + Math.max(1, ticks);
        f.clock.pending = () -> {
            try { task.run(); }
            catch (RuntimeException | Error error) { cleanup(f); throw error; }
        };
    }
    private static void cleanup(Fixture f) {
        if (f.closed) return;
        f.closed = true;
        try {
            if (LibertyManager.active(f.player) || LibertyManager.hasReturn(f.player))
                check(LibertyManager.leave(f.player, false, "Liberty runtime fixture cleanup"), "Could not restore the Liberty fixture player");
            LibertyManager.forget(f.player);
        } finally {
            if (f.district != null) for (ChunkPos chunk : f.chunks)
                f.district.getChunkSource().removeRegionTicket(TICKET, chunk, 4, f.player.getUUID());
        }
    }
    private static boolean skipAbsent(GameTestHelper helper, String test) {
        if (LibertyTaczCompat.available()) return false;
        check(!Boolean.getBoolean("jetsetcraft.libertyPackExpected"), "The required native Helldivers lane is unavailable: " + LibertyTaczCompat.status());
        JetSetCraft.LOGGER.info("JETSETCRAFT_LIBERTY_RUNTIME_SKIP {} reason={} expectedPack=false", test, LibertyTaczCompat.status());
        helper.succeed(); return true;
    }
    private static ResourceLocation id(String value) { return ResourceLocation.tryParse(value); }
    private static void check(boolean condition, String message) { if (!condition) throw new GameTestAssertException(message); }
    private static void pass(GameTestHelper helper, String name) {
        JetSetCraft.LOGGER.info("JETSETCRAFT_LIBERTY_RUNTIME_PASS {}", name); helper.succeed();
    }
    private LibertyRuntimeGameTests() {}
}
