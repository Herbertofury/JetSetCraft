package com.herberto.jetsetcraft.gametest;

import com.herberto.jetsetcraft.JetSetCraft;
import com.herberto.jetsetcraft.activity.ActivityManager;
import com.herberto.jetsetcraft.activity.ActivityMix;
import com.herberto.jetsetcraft.activity.ActivityMode;
import com.herberto.jetsetcraft.activity.ActivityRun;
import com.herberto.jetsetcraft.block.BoomboxBlock;
import com.herberto.jetsetcraft.blockentity.BoomboxBlockEntity;
import com.herberto.jetsetcraft.crew.CrewHouseData;
import com.herberto.jetsetcraft.crew.CrewHouseManager;
import com.herberto.jetsetcraft.gang.HeadGangTargetResolver;
import com.herberto.jetsetcraft.registry.ModBlocks;
import com.mojang.authlib.GameProfile;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestAssertException;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.level.TicketType;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.common.util.FakePlayerFactory;
import net.minecraftforge.gametest.GameTestHolder;
import net.minecraftforge.gametest.PrefixGameTestTemplate;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

/** Real invitations, physical owner tags, relay gates, five native hills and a captain finale. */
@GameTestHolder(JetSetCraft.MOD_ID)
@PrefixGameTestTemplate(false)
public final class ActivityMixGameTests {
    private static final ResourceLocation GANG = ResourceLocation.tryParse("jetsetcraft:dead_beat");
    private static final TicketType<BlockPos> TICKET = TicketType.create("jetsetcraft_mix_gametest", BlockPos::compareTo, 2);

    @GameTest(template = "hoverboard_empty", timeoutTicks = 40)
    public static void mixesHaveDistinctRoundsAndBoundedObjectiveClocks(GameTestHelper helper) {
        for (ActivityMode first : List.of(ActivityMode.TAG, ActivityMode.KING_OF_HILL, ActivityMode.SHOWDOWN)) {
            ActivityMix mix = new ActivityMix(UUID.randomUUID(), first);
            check(mix.mode() == first && new HashSet<>(mix.route()).size() == 3 && !mix.advance(),
                    "A playlist skipped its reviewed first round or advanced before a real win");
            Set<UUID> events = new HashSet<>();
            for (int round = 1; round <= 3; round++) {
                ActivityMode mode = mix.mode();
                check(events.add(mix.eventId()) && mix.round() == round, "Two rounds shared an actor/settlement identity");
                ActivityRun run = mix.newRun();
                if (mode == ActivityMode.KING_OF_HILL) {
                    check(run.goal() == 600 && run.objectiveGoal() == 5 && run.hillDuration() == 120,
                            "Mix hills did not use their five-site, six-second objective");
                    for (int tick = 0; tick < 600; tick++) {
                        run.tick();
                        check(!run.holdHill(false, false) && !run.holdHill(true, true), "Absent or contested capture advanced");
                        run.holdHill(true, false);
                    }
                } else for (int objective = 0; objective < mode.goal; objective++) run.tagged();
                check(run.won() && run.progress() == run.goal() && !run.tagged() && !run.holdHill(true, false),
                        "A completed objective could continue scoring");
                check(mix.wonRound(mode) && !mix.wonRound(mode), "One round advanced the playlist twice");
                check(round == 3 ? mix.complete() && !mix.advance() : mix.intermission() && mix.advance(),
                        "The playlist's explicit intermission boundary was lost");
            }
        }
        ActivityMix stopped = new ActivityMix(UUID.randomUUID(), ActivityMode.TAG);
        stopped.wonRound(ActivityMode.TAG); stopped.cancel();
        check(stopped.cleared() == 1 && !stopped.advance(), "Cancellation erased a win or started another round");
        ActivityRun quick = new ActivityRun(ActivityMode.KING_OF_HILL);
        check(quick.goal() == 480 && quick.hillDuration() == 160 && quick.remaining() == 2400,
                "The optional format changed a quick match");
        boolean rejected = false;
        try { new ActivityRun(ActivityMode.KING_OF_HILL, 999, 1, Integer.MAX_VALUE); }
        catch (IllegalArgumentException expected) { rejected = true; }
        check(rejected, "An unbounded street objective was admitted");
        pass(helper, "activity_mix_model");
    }

    @GameTest(template = "hoverboard_empty", timeoutTicks = 1800)
    public static void physicalStreetMixBanksEachModeOnceAndRequiresItsRealModifiers(GameTestHelper helper) {
        Fixture f = new Fixture(helper, new BlockPos(34000, 100, 20000));
        awaitLoaded(f, 100, () -> {
            ActivityManager.openAt(f.owner, f.anchor);
            f.clock.later(5, () -> {
                action(f.owner, "format", ActivityMix.FORMAT);
                CompoundTag preview = state(f);
                check(preview.getBoolean("Mix") && preview.getInt("MixRound") == 1
                                && preview.getString("MixRoute").contains("King of the Hill") && !ActivityManager.active(f.owner),
                        "Choosing Street Mix did not expose an inactive, complete route preview");
                f.clock.later(5, () -> {
                    action(f.owner, "launch", "");
                    check(ActivityManager.active(f.owner), "The reviewed Street Mix failed admission: " + state(f));
                    f.events.add(state(f).getUUID("Event"));
                    f.clock.later(10, () -> tagLoop(f, 0));
                });
            });
        });
    }

    private static void tagLoop(Fixture f, int attempt) {
        check(attempt < 100, "The physical relay never completed: " + state(f));
        CompoundTag state = state(f);
        if (!ActivityManager.active(f.owner)) {
            check(state.getBoolean("Won") && state.getBoolean("Intermission") && state.getInt("MixCleared") == 1
                            && f.handoffs.size() == 2 && actors(f).isEmpty() && pendingSupplies(f) == f.expectedTagSupplies,
                    "The six real relay tags did not bank exactly one Tag reward at an explicit break: " + state);
            if (f.afterTag != null) { f.afterTag.run(); return; }
            f.owner.moveTo(Vec3.atBottomCenterOf(f.anchor).add(0, 1, 0));
            f.clock.later(20, () -> {
                check(!ActivityManager.active(f.owner) && actors(f).isEmpty() && pendingSupplies(f) == 8,
                        "An intermission auto-started the next round or duplicated its reward");
                CompoundTag stale = state(f);
                ActivityManager.handleAction(f.owner, "next", "", stale.getUUID("Context"), stale.getInt("Version") - 1);
                check(!ActivityManager.active(f.owner), "A stale next-round action launched unreviewed content");
                f.clock.later(5, () -> blockedNextPreservesEarnedRound(f));
            });
            return;
        }
        if (state.getBoolean("RelayHandoff")) {
            int progress = state.getInt("Progress");
            check(f.handoffs.add(progress) && actors(f).isEmpty(), "A handoff repeated or admitted the next relay pair early");
            f.owner.moveTo(Vec3.atBottomCenterOf(f.anchor.east(15)));
            f.outsider.moveTo(Vec3.atBottomCenterOf(f.anchor).add(0, 1, 0));
            f.clock.later(20, () -> {
                check(state(f).getBoolean("RelayHandoff") && state(f).getInt("Progress") == progress && actors(f).isEmpty(),
                        "A bystander at the handoff ring released the owner's next pair");
                f.owner.moveTo(Vec3.atBottomCenterOf(f.anchor).add(0, 1, 0));
                f.clock.later(10, () -> tagLoop(f, attempt + 1));
            });
            return;
        }
        for (Mob mob : actors(f)) {
            placeForHit(f, mob);
            float health = mob.getHealth();
            f.outsider.moveTo(f.owner.position()); f.outsider.attack(mob);
            check(!mob.isRemoved() && mob.getHealth() == health, "A bystander damaged a relay target");
            f.owner.attack(mob);
            check(mob.isRemoved() && mob.getHealth() == health, "A physical owner relay tag did not score non-lethally");
        }
        f.clock.later(10, () -> tagLoop(f, attempt + 1));
    }

    private static void blockedNextPreservesEarnedRound(Fixture f) {
        CompoundTag earned = state(f);
        BlockPos obstruction = f.anchor.north(9).above(6);
        f.helper.getLevel().setBlockAndUpdate(obstruction, Blocks.STONE.defaultBlockState());
        action(f.owner, "next", "");
        CompoundTag rejected = state(f);
        check(rejected.getBoolean("Intermission") && rejected.getInt("MixRound") == 1 && rejected.getInt("MixCleared") == 1
                        && rejected.getString("Mode").equals("tag") && rejected.getUUID("Event").equals(earned.getUUID("Event"))
                        && rejected.getInt("Supplies") == 8 && !rejected.getBoolean("CanChooseFormat") && pendingSupplies(f) == 8,
                "A failed next-round admission erased or reset the paid round: " + rejected);
        f.clock.later(5, () -> {
            action(f.owner, "format", "quick");
            check(state(f).getBoolean("Intermission") && state(f).getString("Format").equals(ActivityMix.FORMAT)
                            && state(f).getUUID("Context").equals(earned.getUUID("Context")),
                    "A blocked transition let format selection reuse a settled round identity");
            f.helper.getLevel().setBlockAndUpdate(obstruction, Blocks.AIR.defaultBlockState());
            f.clock.later(5, () -> {
                action(f.owner, "next", "");
                check(ActivityManager.active(f.owner) && state(f).getString("Mode").equals("king_of_hill")
                                && state(f).getInt("Goal") == 600 && f.events.add(state(f).getUUID("Event")),
                        "Repaired next round did not launch a distinct five-site hill event: " + state(f));
                ActivityManager.handleAction(f.owner, "cancel", "", earned.getUUID("Context"), earned.getInt("Version"));
                check(ActivityManager.active(f.owner), "A delayed prior-round cancel stopped the newly accepted round");
                f.clock.later(10, () -> awaitContenders(f, 100));
            });
        });
    }

    private static void awaitContenders(Fixture f, int remaining) {
        check(remaining > 0 && ActivityManager.active(f.owner), "The five-site contest lost its native contenders: " + state(f));
        if (actors(f).size() < 3) { f.clock.later(5, () -> awaitContenders(f, remaining - 5)); return; }
        Vec3 hill = hill(state(f)); f.owner.moveTo(hill);
        for (Mob mob : actors(f)) mob.moveTo(hill.add(1, 0, 0));
        f.clock.later(10, () -> {
            check(state(f).getInt("Progress") == 0, "A contested Mix hill advanced before actual owner tags");
            hillLoop(f, 0);
        });
    }

    private static void hillLoop(Fixture f, int attempt) {
        check(attempt < 100, "The five real hill locations did not complete: " + state(f));
        CompoundTag state = state(f);
        if (!ActivityManager.active(f.owner)) {
            check(state.getBoolean("Won") && state.getBoolean("Intermission") && state.getInt("MixCleared") == 2
                            && state.getInt("Progress") == 600 && f.hills.size() == 5 && pendingSupplies(f) == 22,
                    "Five physical uncontested captures did not bank the distinct hill reward: " + state);
            f.owner.moveTo(Vec3.atBottomCenterOf(f.anchor).add(0, 1, 0));
            f.clock.later(5, () -> {
                action(f.owner, "next", "");
                check(ActivityManager.active(f.owner) && state(f).getString("Mode").equals("showdown")
                                && f.events.add(state(f).getUUID("Event")), "The captain round reused a prior event identity");
                f.clock.later(10, () -> showdownLoop(f, 0));
            });
            return;
        }
        Vec3 hill = hill(state); f.hills.add(BlockPos.containing(hill)); f.owner.moveTo(hill);
        for (Mob mob : actors(f)) { mob.moveTo(hill.add(1, 0, 0)); f.owner.attack(mob); }
        f.clock.later(10, () -> hillLoop(f, attempt + 1));
    }

    private static void showdownLoop(Fixture f, int attempt) {
        check(attempt < 80, "The captain finale never completed: " + state(f));
        if (!ActivityManager.active(f.owner)) {
            CompoundTag state = state(f); CrewHouseData data = CrewHouseData.get(f.owner.getServer());
            check(state.getBoolean("MixComplete") && state.getInt("MixCleared") == 3 && !state.getBoolean("CanNextRound")
                            && f.captainHits == 5 && f.events.size() == 3 && actors(f).isEmpty(),
                    "The final round omitted its five-tag captain or left another payable round: " + state);
            check(pendingSupplies(f) == 34 && data.pendingXp(f.owner.getUUID()) == 220
                            && data.pendingTraining(f.owner.getUUID()) == 77,
                    "The physical three-mode mix failed to settle the existing shared supplies/XP/training");
            check(!CrewHouseManager.bankActivity(f.owner, state.getUUID("Event"), GANG, "showdown", 600, 4, 400).accepted(),
                    "The last round's receipt could be paid twice");
            CompoundTag saved = data.save(new CompoundTag());
            check(saved.equals(CrewHouseData.load(saved).save(new CompoundTag())), "The earned Mix rewards did not survive SavedData round-trip");
            check(f.inventory.equals(f.owner.getInventory().save(new ListTag())) && f.owner.totalExperience == 587
                            && f.owner.experienceLevel == 23 && f.owner.experienceProgress == .625F
                            && f.helper.getLevel().getBlockState(f.anchor.east(8).below()).is(Blocks.STONE)
                            && !f.helper.getLevel().getBlockState(f.anchor).getValue(BoomboxBlock.ACTIVE)
                            && f.helper.getLevel().getForcedChunks().size() == f.forced,
                    "A Mix changed ordinary inventory/XP/terrain, left the Boombox active, or forced a chunk");
            f.cleanup(); pass(f.helper, "activity_mix_runtime"); return;
        }
        for (Mob mob : actors(f)) {
            boolean captain = mob.getName().getString().startsWith("Crew captain");
            if (captain) check(state(f).getInt("Progress") == 3, "The captain entered before all three regular rivals were cleared");
            placeForHit(f, mob); f.owner.attack(mob);
            int hits = f.hits.merge(mob.getUUID(), 1, Integer::sum);
            if (captain) {
                f.captainHits = hits;
                check(hits < 5 ? !mob.isRemoved() : mob.isRemoved(), "The captain ignored its five actual separated tags");
            } else check(hits < 3 ? !mob.isRemoved() : mob.isRemoved(), "A regular rival changed its three-tag rule");
        }
        f.clock.later(10, () -> showdownLoop(f, attempt + 1));
    }

    @GameTest(template = "hoverboard_empty", timeoutTicks = 300)
    public static void mixHillAdmissionAndCancellationPreserveOrdinaryTerrain(GameTestHelper helper) {
        Fixture f = new Fixture(helper, new BlockPos(35000, 100, 20000));
        awaitLoaded(f, 100, () -> {
            ActivityManager.openAt(f.owner, f.anchor);
            f.clock.later(5, () -> {
                action(f.owner, "select", "king_of_hill");
                f.clock.later(5, () -> {
                    action(f.owner, "format", ActivityMix.FORMAT);
                    // A test-only high obstruction makes a required location unsafe without fluid updates.
                    BlockPos obstruction = f.anchor.north(9).above(6);
                    helper.getLevel().setBlockAndUpdate(obstruction, Blocks.STONE.defaultBlockState());
                    f.clock.later(5, () -> {
                        action(f.owner, "launch", "");
                        check(!ActivityManager.active(f.owner) && actors(f).isEmpty()
                                        && state(f).getString("Summary").contains("five clear") && pendingSupplies(f) == 0
                                        && helper.getLevel().getBlockState(obstruction).is(Blocks.STONE),
                                "An unsafe five-site layout launched, paid, or changed the source terrain: " + state(f));
                        helper.getLevel().setBlockAndUpdate(obstruction, Blocks.AIR.defaultBlockState());
                        f.clock.later(10, () -> {
                            action(f.owner, "launch", "");
                            check(ActivityManager.active(f.owner), "A repaired loaded layout could not accept its original invitation: " + state(f));
                            f.clock.later(20, () -> {
                                action(f.owner, "cancel", "");
                                check(!ActivityManager.active(f.owner) && !state(f).getBoolean("Intermission")
                                                && !state(f).getBoolean("Won") && pendingSupplies(f) == 0 && actors(f).isEmpty(),
                                        "Cancellation paid or advanced an unfinished Mix round");
                                f.cleanup(); pass(helper, "activity_mix_safety");
                            });
                        });
                    });
                });
            });
        });
    }

    @GameTest(template = "hoverboard_empty", timeoutTicks = 650)
    public static void intermissionCommandCancelAndAnchorRemovalRetainEarnedRewards(GameTestHelper helper) {
        Fixture f = new Fixture(helper, new BlockPos(35500, 100, 20000));
        f.afterTag = () -> {
            ActivityManager.cancel(f.owner, "Street Mix ended by the activity cancel command.");
            check(!state(f).getBoolean("Intermission") && !state(f).getBoolean("CanNextRound") && pendingSupplies(f) == 8,
                    "The cancel command did not end an intermission while retaining its earned reward");
            f.handoffs.clear(); f.expectedTagSupplies = 16;
            f.afterTag = () -> {
                helper.getLevel().setBlockAndUpdate(f.anchor, Blocks.AIR.defaultBlockState());
                check(!state(f).getBoolean("Intermission") && !state(f).getBoolean("CanNextRound")
                                && helper.getLevel().getBlockState(f.anchor).isAir() && pendingSupplies(f) == 16 && actors(f).isEmpty(),
                        "Boombox removal retained an intermission, rebuilt the anchor, or lost earlier rewards");
                f.cleanup(); pass(helper, "activity_mix_intermission");
            };
            f.clock.later(5, () -> launchTagMix(f));
        };
        awaitLoaded(f, 100, () -> launchTagMix(f));
    }

    private static void launchTagMix(Fixture f) {
        f.owner.moveTo(Vec3.atBottomCenterOf(f.anchor).add(0, 1, 0)); ActivityManager.openAt(f.owner, f.anchor);
        f.clock.later(5, () -> {
            action(f.owner, "format", ActivityMix.FORMAT);
            f.clock.later(5, () -> {
                action(f.owner, "launch", "");
                check(ActivityManager.active(f.owner), "The intermission fixture could not launch a new Tag Mix: " + state(f));
                f.clock.later(10, () -> tagLoop(f, 0));
            });
        });
    }

    private static final class Fixture {
        final GameTestHelper helper;
        final BlockPos anchor;
        final ServerPlayer owner, outsider;
        final ListTag inventory;
        final int forced;
        final NativeClock clock;
        final Set<Integer> handoffs = new HashSet<>();
        final Set<BlockPos> hills = new HashSet<>();
        final Set<UUID> events = new HashSet<>();
        final Map<UUID, Integer> hits = new HashMap<>();
        int captainHits, expectedTagSupplies = 8;
        Runnable afterTag;
        Fixture(GameTestHelper helper, BlockPos anchor) {
            this.helper = helper; this.anchor = anchor;
            Runnable ticket = () -> helper.getLevel().getChunkSource().addRegionTicket(TICKET, new ChunkPos(anchor), 3, anchor);
            ticket.run(); helper.onEachTick(ticket);
            for (int dx = -13; dx <= 13; dx++) for (int dz = -13; dz <= 13; dz++) {
                BlockPos column = anchor.offset(dx, 0, dz);
                int top = helper.getLevel().getHeight(Heightmap.Types.WORLD_SURFACE, column.getX(), column.getZ());
                for (int y = Math.min(top, helper.getLevel().getMaxBuildHeight()) - 1; y >= anchor.getY(); y--)
                    helper.getLevel().setBlockAndUpdate(new BlockPos(column.getX(), y, column.getZ()), Blocks.AIR.defaultBlockState());
                helper.getLevel().setBlockAndUpdate(column.below(), Blocks.STONE.defaultBlockState());
            }
            helper.getLevel().setBlockAndUpdate(anchor, ModBlocks.BOOMBOX.get().defaultBlockState());
            ItemStack head = new ItemStack(Items.PLAYER_HEAD);
            head.getOrCreateTag().putString(HeadGangTargetResolver.TARGET_ENTITY_KEY, "minecraft:pig");
            head.getOrCreateTag().putString(HeadGangTargetResolver.TARGET_GANG_KEY, GANG.toString());
            check(((BoomboxBlockEntity) helper.getLevel().getBlockEntity(anchor)).setTarget(head), "The physical test Boombox rejected its source crew");
            owner = player(helper, anchor, "owner"); outsider = player(helper, anchor, "guest");
            inventory = owner.getInventory().save(new ListTag()); forced = helper.getLevel().getForcedChunks().size();
            clock = new NativeClock(helper, this::cleanup);
        }
        void cleanup() { ActivityManager.forget(owner); ActivityManager.forget(outsider); }
    }

    private static final class NativeClock {
        int ticks, due;
        Runnable pending;
        NativeClock(GameTestHelper helper, Runnable cleanup) {
            helper.onEachTick(() -> {
                ticks++;
                if (pending == null || ticks < due) return;
                Runnable task = pending; pending = null;
                try { task.run(); } catch (RuntimeException error) { cleanup.run(); throw error; }
            });
        }
        void later(int delay, Runnable task) {
            check(pending == null && delay > 0, "The Mix fixture replaced an unexecuted native continuation");
            due = ticks + delay; pending = task;
        }
    }
    private static void awaitLoaded(Fixture f, int remaining, Runnable ready) {
        boolean loaded = true;
        for (int x : new int[]{-12, 12}) for (int z : new int[]{-12, 12}) loaded &= f.helper.getLevel().isPositionEntityTicking(f.anchor.offset(x, 0, z));
        if (loaded) { ready.run(); return; }
        check(remaining > 0, "The Mix fixture never acquired native ticking residency");
        f.clock.later(5, () -> awaitLoaded(f, remaining - 5, ready));
    }
    private static ServerPlayer player(GameTestHelper helper, BlockPos anchor, String name) {
        ServerPlayer player = FakePlayerFactory.get(helper.getLevel(), new GameProfile(UUID.randomUUID(), "JSC_mix_" + name));
        player.setGameMode(GameType.SURVIVAL); player.setHealth(player.getMaxHealth());
        player.moveTo(Vec3.atBottomCenterOf(anchor).add(0, 1, 0));
        player.getInventory().clearContent(); player.getInventory().selected = 0;
        player.getInventory().setItem(4, new ItemStack(Items.DIAMOND, 7));
        player.experienceLevel = 23; player.totalExperience = 587; player.experienceProgress = .625F;
        return player;
    }
    private static CompoundTag state(Fixture f) { return ActivityManager.snapshot(f.owner); }
    private static int pendingSupplies(Fixture f) { return CrewHouseData.get(f.owner.getServer()).pendingSupplies(f.owner.getUUID()); }
    private static Vec3 hill(CompoundTag state) { return new Vec3(state.getDouble("HillX"), state.getDouble("HillY"), state.getDouble("HillZ")); }
    private static List<Mob> actors(Fixture f) {
        return f.helper.getLevel().getEntitiesOfClass(Mob.class, new AABB(f.anchor).inflate(40), mob -> ActivityManager.owned(mob)
                && mob.getPersistentData().getCompound(ActivityManager.OWNED).hasUUID("Owner")
                && mob.getPersistentData().getCompound(ActivityManager.OWNED).getUUID("Owner").equals(f.owner.getUUID()));
    }
    private static void placeForHit(Fixture f, Mob mob) {
        mob.moveTo(Vec3.atBottomCenterOf(f.anchor.east(8))); f.owner.moveTo(mob.position().add(0, 0, 1));
    }
    private static void action(ServerPlayer owner, String action, String mode) {
        CompoundTag state = ActivityManager.snapshot(owner);
        ActivityManager.handleAction(owner, action, mode, state.getUUID("Context"), state.getInt("Version"));
    }
    private static void check(boolean condition, String message) { if (!condition) throw new GameTestAssertException(message); }
    private static void pass(GameTestHelper helper, String marker) { System.out.println("JETSETCRAFT_GAMETEST_PASS " + marker); helper.succeed(); }
    private ActivityMixGameTests() {}
}
