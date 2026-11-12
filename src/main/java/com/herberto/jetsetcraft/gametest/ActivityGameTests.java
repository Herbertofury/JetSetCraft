package com.herberto.jetsetcraft.gametest;

import com.herberto.jetsetcraft.JetSetCraft;
import com.herberto.jetsetcraft.activity.ActivityManager;
import com.herberto.jetsetcraft.block.BoomboxBlock;
import com.herberto.jetsetcraft.blockentity.BoomboxBlockEntity;
import com.herberto.jetsetcraft.crew.CrewHouseData;
import com.herberto.jetsetcraft.crew.CrewHouseManager;
import com.herberto.jetsetcraft.crew.CrewProfile;
import com.herberto.jetsetcraft.gang.HeadGangTargetResolver;
import com.herberto.jetsetcraft.registry.ModBlocks;
import com.mojang.authlib.GameProfile;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestAssertException;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.level.TicketType;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.common.util.FakePlayerFactory;
import net.minecraftforge.gametest.GameTestHolder;
import net.minecraftforge.gametest.PrefixGameTestTemplate;

import java.util.List;
import java.util.UUID;

/** Actual Boombox invitations, source mobs, Forge attack hooks and the shared world-saved reward account. */
@GameTestHolder(JetSetCraft.MOD_ID)
@PrefixGameTestTemplate(false)
public final class ActivityGameTests {
    private static final ResourceLocation GANG = ResourceLocation.tryParse("jetsetcraft:dead_beat");
    private static final TicketType<BlockPos> FIXTURE_TICKET = TicketType.create("jetsetcraft_activity_gametest", BlockPos::compareTo, 2);

    @GameTest(template = "hoverboard_empty", timeoutTicks = 100)
    public static void pendingRewardsTransferReceiptsAndTrainingExactlyOnce(GameTestHelper helper) {
        BlockPos anchor = freeHomeAnchor(helper);
        ServerPlayer owner = player(helper, "wallet", anchor);
        CrewHouseData store = CrewHouseData.get(owner.getServer());
        UUID tag = UUID.randomUUID(), rush = UUID.randomUUID();
        check(!CrewHouseManager.bankActivity(owner, tag, GANG, "tag", 400, 5, 100).accepted()
                        && !CrewHouseManager.bankActivity(owner, tag, GANG, "unknown", 400, 99, 100).accepted()
                        && !CrewHouseManager.bankActivity(owner, tag, GANG, "rush_arena", 199, 3, 100).accepted(),
                "Incomplete, unknown or too-short activity results changed the shared account");
        check(CrewHouseManager.bankActivity(owner, tag, GANG, "tag", 400, 6, 600).pendingHouse(),
                "A completed game before founding did not create a pending reward");
        check(!CrewHouseManager.bankActivity(owner, tag, GANG, "tag", 400, 6, 600).accepted(),
                "The same completion receipt paid twice before founding");
        CrewHouseManager.bankActivity(owner, UUID.randomUUID(), GANG, "king_of_hill", 500, 3, 1000);
        CrewHouseManager.bankActivity(owner, UUID.randomUUID(), GANG, "showdown", 400, 4, 1000);
        CrewHouseManager.bankActivity(owner, UUID.randomUUID(), GANG, "rush_arena_supplies", 600, 3, 1000);
        CrewHouseManager.bankRush(owner, rush, GANG, 1200, 3, 3, 1000);
        check(store.pendingSupplies(owner.getUUID()) == 56 && store.pendingXp(owner.getUUID()) == 277
                        && store.pendingTraining(owner.getUUID()) == 110 && store.pendingReputation(owner.getUUID(), GANG.toString()) == 28,
                "Tag, hill, showdown, arena and Rush rewards did not share one bounded pending account");
        CompoundTag beforeFound = store.save(new CompoundTag());
        check(beforeFound.getInt("Schema") == 4 && beforeFound.equals(CrewHouseData.load(beforeFound).save(new CompoundTag())),
                "Pending amounts or receipt identities did not round-trip through world SavedData");
        helper.getLevel().setBlockAndUpdate(anchor.below(), Blocks.STONE.defaultBlockState());
        helper.getLevel().setBlockAndUpdate(anchor, ModBlocks.BOOMBOX.get().defaultBlockState());
        CrewHouseManager.openAt(owner, anchor);
        CompoundTag founding = CrewHouseManager.snapshot(owner);
        check(founding.getBoolean("CanFound"), "The real Boombox founding fixture was unavailable at " + anchor
                + ": " + founding.getString("FoundReason"));
        CrewHouseManager.handleAction(owner, "found", "Every mode crew", 0, null, null, 0);
        CrewProfile home = store.profile(owner.getUUID());
        check(home != null && home.supplies() == 80 && home.xp() == 277 && home.members().get(0).xp() == 110
                        && home.reputation(GANG.toString()) == 28 && store.pendingSupplies(owner.getUUID()) == 0
                        && store.pendingTraining(owner.getUUID()) == 0 && store.pendingXp(owner.getUUID()) == 0,
                "Founding failed to transfer pending supplies, crew/member XP and reputation exactly once: "
                        + (home == null ? CrewHouseManager.snapshot(owner) : home.save()));
        check(!CrewHouseManager.bankActivity(owner, tag, GANG, "tag", 400, 6, 600).accepted(),
                "A pre-house receipt became payable again after transfer");
        CrewHouseManager.bankRush(owner, rush, GANG, 1200, 3, 3, 1000);
        check(!store.add(home) && home.supplies() == 80 && home.members().get(0).xp() == 110,
                "Repeated Rush settlement or repeated founding replayed pending training");
        CrewProfile.Job job = home.assignMission(home.members().get(0).id(), "courier_run", 1000, 0);
        check(job != null, "A member trained by activity rewards could not accept a normal mission");
        var afterHouse = CrewHouseManager.bankActivity(owner, UUID.randomUUID(), GANG, "showdown", 400, 4, 1000);
        check(afterHouse.accepted() && !afterHouse.pendingHouse() && afterHouse.memberXp() == 0
                        && home.supplies() == 92 && home.xp() == 352 && home.members().get(0).xp() == 110,
                "Shared rewards failed to respect a training member who was away on a mission");
        // The actual persisted claim boundary is tested without advancing the shared server's time.
        check(!home.claimMission(job.id(), job.readyAt() - 1) && home.claimMission(job.id(), job.readyAt())
                        && !home.claimMission(job.id(), job.readyAt()) && home.supplies() == 92 + job.supplies()
                        && home.xp() == 352 + job.crewXp() && home.members().get(0).xp() == 110 + job.memberXp(),
                "The mission wallet or its once-only claim boundary diverged from activity progression");
        CompoundTag saved = store.save(new CompoundTag());
        check(saved.equals(CrewHouseData.load(saved).save(new CompoundTag())), "The combined mission/activity account failed persistence");
        CompoundTag legacy = saved.copy(); legacy.putInt("Schema", 1); legacy.remove("PendingProgress");
        check(CrewHouseData.load(legacy).profile(owner.getUUID()).save().equals(home.save()),
                "Schema-one houses lost existing progress during migration");
        checkVanilla(owner); pass(helper, "activity_progression");
    }

    @GameTest(template = "hoverboard_empty", timeoutTicks = 360)
    public static void explicitBoomboxTagRequiresOwnersRealObjectiveHits(GameTestHelper helper) {
        BlockPos anchor = arena(helper, 26000);
        ServerPlayer owner = player(helper, "tag", anchor), outsider = player(helper, "other", anchor);
        ListTag inventory = owner.getInventory().save(new ListTag());
        int forced = helper.getLevel().getForcedChunks().size();
        var state = helper.getLevel().getBlockState(anchor);
        state.use(helper.getLevel(), owner, InteractionHand.MAIN_HAND,
                new BlockHitResult(Vec3.atCenterOf(anchor), Direction.UP, anchor, false));
        CompoundTag preview = ActivityManager.snapshot(owner);
        check(!ActivityManager.active(owner) && preview.getString("Mode").equals("tag")
                        && preview.getBoolean("FromBoombox") && actors(owner, anchor).isEmpty()
                        && !((BoomboxBlockEntity) helper.getLevel().getBlockEntity(anchor)).isChallengeActive(),
                "Empty-hand Boombox use did not open an explicit, inactive Tag invitation");
        UUID receipt = preview.getUUID("Context");
        helper.runAfterDelay(5, () -> {
            action(owner, "launch", ""); check(ActivityManager.active(owner), "The accepted explicit Tag game did not start: " + diagnostics(owner, anchor));
            helper.runAfterDelay(15, () -> tagLoop(helper, owner, outsider, anchor, inventory, forced, receipt, 0));
        });
    }

    private static void tagLoop(GameTestHelper helper, ServerPlayer owner, ServerPlayer outsider, BlockPos anchor,
                                ListTag inventory, int forced, UUID receipt, int attempt) {
        try {
            check(attempt < 50, "The real source-mob Tag round never reached its six owner objectives");
            for (Mob mob : actors(owner, anchor)) {
                approach(owner, mob.position()); approach(outsider, mob.position());
                int before = ActivityManager.snapshot(owner).getInt("Progress"); float health = mob.getHealth();
                outsider.attack(mob);
                check(!mob.isRemoved() && mob.getHealth() == health && ActivityManager.snapshot(owner).getInt("Progress") == before,
                        "A bystander's attack damaged or scored the owner's rival");
                owner.attack(mob);
                check(mob.isRemoved() && mob.getHealth() == health && ActivityManager.snapshot(owner).getInt("Progress") == before + 1,
                        "The actual owner melee hook did not advance exactly one Tag objective");
                check(!ActivityManager.tag(owner, mob, false), "An already removed actor scored a second time");
            }
            CompoundTag state = ActivityManager.snapshot(owner);
            if (ActivityManager.active(owner)) {
                helper.runAfterDelay(5, () -> tagLoop(helper, owner, outsider, anchor, inventory, forced, receipt, attempt + 1)); return;
            }
            check(state.getBoolean("Won") && state.getInt("Progress") == 6 && state.getInt("Supplies") == 8
                            && CrewHouseData.get(owner.getServer()).pendingXp(owner.getUUID()) == 55,
                    "Six genuine Tag objectives did not settle exactly the shared completion reward: " + state.getString("Summary"));
            check(!CrewHouseManager.bankActivity(owner, receipt, GANG, "tag", 600, 6, 600).accepted(),
                    "The real activity result could be settled twice");
            check(actors(owner, anchor).isEmpty() && !helper.getLevel().getBlockState(anchor).getValue(BoomboxBlock.ACTIVE)
                            && helper.getLevel().getForcedChunks().size() == forced
                            && helper.getLevel().getBlockState(anchor.offset(8, -1, 0)).is(Blocks.STONE),
                    "Tag retained actors, forced chunks, an active anchor or modified its source floor");
            check(inventory.equals(owner.getInventory().save(new ListTag())), "The mode changed ordinary inventory");
            checkVanilla(owner); ActivityManager.forget(owner); ActivityManager.forget(outsider); pass(helper, "activity_tag");
        } catch (RuntimeException error) { ActivityManager.forget(owner); ActivityManager.forget(outsider); throw error; }
    }

    @GameTest(template = "hoverboard_empty", timeoutTicks = 360)
    public static void randomPreviewStaleActionsAndMissingActorsCannotCompleteAGame(GameTestHelper helper) {
        BlockPos anchor = arena(helper, 28000);
        ServerPlayer owner = player(helper, "safety", anchor);
        NativeClock clock = new NativeClock(helper, () -> ActivityManager.forget(owner));
        ActivityManager.open(owner);
        CompoundTag random = ActivityManager.snapshot(owner);
        check(random.getString("SelectedMode").equals("random") && !ActivityManager.active(owner)
                        && random.getList("Activities", 10).size() == 6
                        && random.getList("Activities", 10).stream().anyMatch(value -> value instanceof CompoundTag row && row.getString("Id").equals("liberty_grind")),
                "The generic invitation did not put random and all five actual modes, including Liberty Grind, in one optional catalogue");
        clock.later(5, () -> {
            action(owner, "refresh", "");
            check(ActivityManager.snapshot(owner).getString("Mode").equals(random.getString("Mode"))
                            && ActivityManager.snapshot(owner).getUUID("Context").equals(random.getUUID("Context")),
                    "Refreshing rerolled or implicitly launched the revealed random invitation");
            clock.later(5, () -> {
                ActivityManager.openAt(owner, anchor);
                CompoundTag stale = ActivityManager.snapshot(owner);
                clock.later(5, () -> {
                    action(owner, "select", "showdown");
                    check(ActivityManager.snapshot(owner).getString("Mode").equals("showdown"), "Explicit mode selection was ignored");
                    clock.later(5, () -> {
                        ActivityManager.handleAction(owner, "launch", "", stale.getUUID("Context"), stale.getInt("Version"));
                        check(!ActivityManager.active(owner), "A stale selection launched a different game than the reviewed mode");
                        clock.later(5, () -> prepareShowdown(clock, helper, owner, anchor, 80));
                    });
                });
            });
        });
    }

    private static void prepareShowdown(NativeClock clock, GameTestHelper helper, ServerPlayer owner, BlockPos anchor, int remaining) {
        int ticking = 0;
        for (int x : new int[]{-12, 12}) for (int z : new int[]{-12, 12})
            if (owner.serverLevel().isPositionEntityTicking(anchor.offset(x, 0, z))) ticking++;
        if (ticking != 4) {
            check(remaining > 0, "The Showdown fixture never obtained native entity-ticking residency: tickingCorners="
                    + ticking + "/4, " + diagnostics(owner, anchor));
            clock.later(1, () -> prepareShowdown(clock, helper, owner, anchor, remaining - 1)); return;
        }
        action(owner, "launch", "");
        check(ActivityManager.active(owner), "Fresh Showdown confirmation was not accepted: " + diagnostics(owner, anchor));
        // Production retries every ten ticks, up to sixteen attempts for four Showdown rivals.
        // Observe that native window instead of assuming two first-try actors after thirty ticks.
        awaitShowdownActors(clock, helper, owner, anchor, 160);
    }

    private static void awaitShowdownActors(NativeClock clock, GameTestHelper helper, ServerPlayer owner, BlockPos anchor, int remaining) {
        check(ActivityManager.active(owner), "Showdown ended before its real protected actors arrived: " + diagnostics(owner, anchor));
        if (actors(owner, anchor).size() < 2) {
            check(remaining > 0, "Showdown did not spawn its real protected source actors within the native spawn window: " + diagnostics(owner, anchor));
            clock.later(1, () -> awaitShowdownActors(clock, helper, owner, anchor, remaining - 1)); return;
        }
        showdownFirstHit(clock, helper, owner, anchor);
    }

    private static void showdownFirstHit(NativeClock clock, GameTestHelper helper, ServerPlayer owner, BlockPos anchor) {
        List<Mob> live = actors(owner, anchor); check(live.size() >= 2, "Showdown did not spawn its real protected source actors: " + diagnostics(owner, anchor));
        Mob rival = live.get(0); approach(owner, rival.position()); owner.attack(rival);
        check(!rival.isRemoved() && ActivityManager.snapshot(owner).getInt("Progress") == 0,
                "A one-hit tap completed a three-tag Showdown rival");
        Mob orphan = EntityType.PIG.create(helper.getLevel()); check(orphan != null, "Missing vanilla test mob");
        orphan.moveTo(rival.position());
        orphan.getPersistentData().put(ActivityManager.OWNED, rival.getPersistentData().getCompound(ActivityManager.OWNED).copy());
        check(!helper.getLevel().addFreshEntity(orphan), "A copied/reloaded activity marker joined as a replacement objective");
        clock.later(9, () -> {
            approach(owner, rival.position()); owner.attack(rival);
            check(!rival.isRemoved(), "The second Showdown tag defeated a three-tag rival");
            clock.later(9, () -> {
                approach(owner, rival.position()); owner.attack(rival);
                check(rival.isRemoved() && ActivityManager.snapshot(owner).getInt("Progress") == 1,
                        "Three actual separated owner tags did not defeat exactly one Showdown rival");
                actors(owner, anchor).get(0).discard();
                clock.later(3, () -> {
                    check(!ActivityManager.active(owner) && !ActivityManager.snapshot(owner).getBoolean("Won")
                                    && CrewHouseData.get(owner.getServer()).pendingSupplies(owner.getUUID()) == 0,
                            "A missing/unloaded rival produced a false completion reward");
                    owner.moveTo(anchor.getX() + .5, anchor.getY() + 1, anchor.getZ() + .5, 0, 0);
                    action(owner, "select", "tag");
                    clock.later(5, () -> {
                        action(owner, "launch", ""); check(ActivityManager.active(owner), "A cancelled game prevented a later explicit game: " + diagnostics(owner, anchor));
                        clock.later(15, () -> {
                            helper.getLevel().setBlockAndUpdate(anchor, Blocks.AIR.defaultBlockState());
                            check(helper.getLevel().getBlockState(anchor).isAir() && !ActivityManager.active(owner)
                                            && actors(owner, anchor).isEmpty()
                                            && CrewHouseData.get(owner.getServer()).pendingSupplies(owner.getUUID()) == 0,
                                    "Removing the Boombox revived it, retained actors or paid cancellation rewards");
                            checkVanilla(owner); ActivityManager.forget(owner); pass(helper, "activity_safety");
                        });
                    });
                });
            });
        });
    }

    @GameTest(template = "hoverboard_empty", timeoutTicks = 900)
    public static void hillsRequirePresenceUncontestedTimeAndThreePhysicalLocations(GameTestHelper helper) {
        BlockPos anchor = arena(helper, 30000);
        ServerPlayer owner = player(helper, "hill", anchor);
        ActivityManager.openAt(owner, anchor);
        NativeClock clock = new NativeClock(helper, () -> ActivityManager.forget(owner));
        clock.later(5, () -> prepareHill(clock, helper, owner, anchor, 80));
    }

    private static void prepareHill(NativeClock clock, GameTestHelper helper, ServerPlayer owner, BlockPos anchor, int remaining) {
        boolean ticking = true;
        for (int x : new int[]{-12, 12}) for (int z : new int[]{-12, 12})
            ticking &= owner.serverLevel().isPositionEntityTicking(anchor.offset(x, 0, z));
        if (!ticking) {
            check(remaining > 0, "The hill fixture never obtained native entity-ticking residency: " + diagnostics(owner, anchor));
            clock.later(1, () -> prepareHill(clock, helper, owner, anchor, remaining - 1)); return;
        }
        action(owner, "select", "king_of_hill");
        clock.later(5, () -> {
            action(owner, "launch", "");
            check(ActivityManager.active(owner), "A clear three-site hill layout was rejected: " + diagnostics(owner, anchor));
            // Production retries safe native spawns every ten ticks, up to twelve attempts for three
            // contenders. Observe that bounded real window instead of assuming three first-try spawns.
            clock.later(1, () -> awaitHillContenders(clock, helper, owner, anchor, 120));
        });
    }

    private static void awaitHillContenders(NativeClock clock, GameTestHelper helper, ServerPlayer owner, BlockPos anchor, int remaining) {
        List<Mob> rivals = actors(owner, anchor);
        check(ActivityManager.active(owner), "The hill round ended before its three contenders arrived: " + diagnostics(owner, anchor));
        check(rivals.size() <= 3, "Hill mode exceeded its three real contenders: " + diagnostics(owner, anchor));
        if (rivals.size() < 3) {
            check(remaining > 0, "Hill mode did not create its three contenders within the native spawn window: " + diagnostics(owner, anchor));
            clock.later(1, () -> awaitHillContenders(clock, helper, owner, anchor, remaining - 1)); return;
        }
        check(rivals.size() == 3, "Hill mode did not create exactly three native contenders");
        Vec3 hill = hill(anchor, 0); owner.moveTo(hill);
        for (Mob mob : rivals) mob.moveTo(hill.add(1, 0, 0));
        int before = ActivityManager.snapshot(owner).getInt("Progress");
        clock.later(5, () -> {
            check(ActivityManager.snapshot(owner).getInt("Progress") == before,
                    "Contested hill presence incorrectly advanced capture time");
            for (Mob mob : actors(owner, anchor)) owner.attack(mob);
            clock.later(20, () -> {
                int held = ActivityManager.snapshot(owner).getInt("Progress");
                check(held > before, "Tagging contenders did not open a real capture window");
                owner.moveTo(Vec3.atBottomCenterOf(anchor));
                clock.later(20, () -> {
                    check(ActivityManager.snapshot(owner).getInt("Progress") == held,
                            "Being outside the hill still earned capture time");
                    hillLoop(clock, helper, owner, anchor, 0);
                });
            });
        });
    }

    private static void hillLoop(NativeClock clock, GameTestHelper helper, ServerPlayer owner, BlockPos anchor, int attempt) {
        try {
            check(attempt < 75, "Three real hill captures failed to complete within the bounded session");
            CompoundTag state = ActivityManager.snapshot(owner);
            if (!ActivityManager.active(owner)) {
                check(state.getBoolean("Won") && state.getInt("Progress") == 480 && state.getInt("Supplies") == 14
                                && CrewHouseData.get(owner.getServer()).pendingXp(owner.getUUID()) == 90,
                        "Hill completion did not bank its distinct shared reward: " + state.getString("Summary"));
                checkVanilla(owner); ActivityManager.forget(owner); pass(helper, "activity_hill"); return;
            }
            Vec3 hill = hill(anchor, Math.min(2, state.getInt("Progress") / 160)); owner.moveTo(hill);
            for (Mob mob : actors(owner, anchor)) { mob.moveTo(hill.add(1, 0, 0)); owner.attack(mob); }
            clock.later(10, () -> hillLoop(clock, helper, owner, anchor, attempt + 1));
        } catch (RuntimeException error) { ActivityManager.forget(owner); throw error; }
    }

    /** One initial native tick registration; continuations never mutate GameTest's scheduled-task map. */
    private static final class NativeClock {
        private int ticks, due;
        private Runnable pending;
        NativeClock(GameTestHelper helper, Runnable cleanup) {
            helper.onEachTick(() -> {
                ticks++;
                if (pending == null || ticks < due) return;
                Runnable action = pending; pending = null;
                try { action.run(); }
                catch (RuntimeException error) { cleanup.run(); throw error; }
            });
        }
        void later(int delay, Runnable action) {
            check(pending == null && delay > 0, "The native activity fixture overwrote a scheduled continuation");
            due = ticks + delay; pending = action;
        }
    }

    private static Vec3 hill(BlockPos anchor, int index) {
        BlockPos offset = switch (index) { case 0 -> anchor.east(7); case 1 -> anchor.south(7); default -> anchor.west(7); };
        return Vec3.atBottomCenterOf(offset);
    }
    private static BlockPos arena(GameTestHelper helper, int x) {
        BlockPos anchor = new BlockPos(x, 100, 16000);
        // FakePlayer.moveTo does not register a connected player with the chunk distance manager. These
        // deliberately remote fixtures therefore need their own short-lived native loading/ticking ticket.
        // The helper stops these renewals when its test ends; timeout2 also cleans up failed/timed-out tests.
        // Production ActivityManager never acquires a ticket, and the world's persistent forced-chunk set
        // remains untouched, so its ordinary loaded-area admission/cancellation rules are still exercised.
        Runnable keepFixtureLoaded = () -> helper.getLevel().getChunkSource()
                .addRegionTicket(FIXTURE_TICKET, new ChunkPos(anchor), 3, anchor);
        keepFixtureLoaded.run(); helper.onEachTick(keepFixtureLoaded);
        for (int dx = -12; dx <= 12; dx++) for (int dz = -12; dz <= 12; dz++) {
            BlockPos column = anchor.offset(dx, 0, dz);
            // Native world generation is seed/provider dependent. A floor alone can leave tree trunks,
            // terrain or fluids above it, changing both the heightmap and the production clearance check.
            // Clear this test column to its highest existing block, then author the intended dry floor.
            int top = helper.getLevel().getHeight(Heightmap.Types.WORLD_SURFACE, column.getX(), column.getZ());
            for (int y = Math.min(top, helper.getLevel().getMaxBuildHeight()) - 1; y >= anchor.getY(); y--) {
                BlockPos existing = new BlockPos(column.getX(), y, column.getZ());
                if (!helper.getLevel().getBlockState(existing).isAir())
                    helper.getLevel().setBlockAndUpdate(existing, Blocks.AIR.defaultBlockState());
            }
            helper.getLevel().setBlockAndUpdate(column.below(), Blocks.STONE.defaultBlockState());
        }
        for (int index = 0; index < 3; index++) {
            BlockPos site = BlockPos.containing(hill(anchor, index));
            check(helper.getLevel().getHeight(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, site.getX(), site.getZ()) == anchor.getY(),
                    "Authored activity fixture did not leave its hill at the intended floor height: " + hillDiagnostics(helper, anchor));
        }
        helper.getLevel().setBlockAndUpdate(anchor, ModBlocks.BOOMBOX.get().defaultBlockState());
        BoomboxBlockEntity box = (BoomboxBlockEntity) helper.getLevel().getBlockEntity(anchor);
        ItemStack head = new ItemStack(Items.PLAYER_HEAD);
        head.getOrCreateTag().putString(HeadGangTargetResolver.TARGET_ENTITY_KEY, "minecraft:pig");
        head.getOrCreateTag().putString(HeadGangTargetResolver.TARGET_GANG_KEY, GANG.toString());
        check(box.setTarget(head), "The real test Boombox rejected an explicit source-mob adapter head");
        return anchor;
    }
    private static BlockPos freeHomeAnchor(GameTestHelper helper) {
        CrewHouseData data = CrewHouseData.get(helper.getLevel().getServer());
        for (int attempt = 0; attempt < 128; attempt++) {
            BlockPos anchor = new BlockPos(24000 + attempt * 128, 100, 16000);
            boolean occupied = data.profiles().stream().anyMatch(profile -> {
                BlockPos existing = BlockPos.of(profile.save().getLong("Anchor"));
                return Math.abs((long) existing.getX() - anchor.getX()) < 100
                        && Math.abs((long) existing.getZ() - anchor.getZ()) < 100;
            });
            if (!occupied) return anchor;
        }
        throw new GameTestAssertException("Could not reserve an isolated plot for the earned-reward founding fixture");
    }

    private static ServerPlayer player(GameTestHelper helper, String name, BlockPos anchor) {
        ServerPlayer player = FakePlayerFactory.get(helper.getLevel(), new GameProfile(UUID.randomUUID(), "JSC_activity_" + name));
        player.setGameMode(GameType.SURVIVAL); player.setHealth(player.getMaxHealth());
        player.moveTo(anchor.getX() + .5, anchor.getY() + 1, anchor.getZ() + .5, 0, 0);
        player.getInventory().clearContent(); player.getInventory().selected = 0;
        player.getInventory().setItem(4, new ItemStack(Items.DIAMOND, 7));
        player.experienceLevel = 23; player.totalExperience = 587; player.experienceProgress = .625F;
        return player;
    }
    private static List<Mob> actors(ServerPlayer player, BlockPos anchor) {
        return player.serverLevel().getEntitiesOfClass(Mob.class, new AABB(anchor).inflate(40), mob ->
                ActivityManager.owned(mob) && mob.getPersistentData().getCompound(ActivityManager.OWNED).hasUUID("Owner")
                        && mob.getPersistentData().getCompound(ActivityManager.OWNED).getUUID("Owner").equals(player.getUUID()));
    }
    private static void approach(ServerPlayer player, Vec3 target) { player.moveTo(target.add(0, 0, 1)); }
    private static void action(ServerPlayer player, String action, String mode) {
        CompoundTag state = ActivityManager.snapshot(player);
        ActivityManager.handleAction(player, action, mode, state.getUUID("Context"), state.getInt("Version"));
    }
    private static String diagnostics(ServerPlayer player, BlockPos anchor) {
        CompoundTag state = ActivityManager.snapshot(player);
        return "summary=" + state.getString("Summary") + ", mode=" + state.getString("Mode")
                + ", canLaunch=" + state.getBoolean("CanLaunch") + ", version=" + state.getInt("Version")
                + ", active=" + state.getBoolean("Active") + ", remainingTicks=" + state.getLong("RemainingTicks")
                + ", actors=" + actors(player, anchor).stream().map(mob -> mob.getType() + "@" + mob.position()).toList()
                + ", anchorLoaded=" + player.serverLevel().hasChunkAt(anchor)
                + ", entityTicking=" + player.serverLevel().isPositionEntityTicking(anchor)
                + ", distanceSquared=" + player.distanceToSqr(Vec3.atCenterOf(anchor))
                + ", hills=" + hillDiagnostics(player.serverLevel(), anchor);
    }
    private static String hillDiagnostics(GameTestHelper helper, BlockPos anchor) {
        return hillDiagnostics(helper.getLevel(), anchor);
    }
    private static String hillDiagnostics(net.minecraft.server.level.ServerLevel level, BlockPos anchor) {
        StringBuilder out = new StringBuilder();
        for (int index = 0; index < 3; index++) {
            BlockPos site = BlockPos.containing(hill(anchor, index));
            if (index > 0) out.append("; ");
            out.append(index + 1).append(":");
            if (!level.hasChunksAt(site.offset(-2, 0, -2), site.offset(2, 2, 2))) { out.append("unloaded"); continue; }
            int y = level.getHeight(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, site.getX(), site.getZ());
            BlockPos feet = new BlockPos(site.getX(), y, site.getZ());
            AABB space = new AABB(feet).inflate(1.0, 0, 1.0).expandTowards(0, 1, 0);
            out.append("y=").append(y).append(", floor=").append(level.getBlockState(feet.below()))
                    .append(", clear=").append(level.noCollision(space))
                    .append(", liquid=").append(level.containsAnyLiquid(space));
        }
        return out.toString();
    }
    private static void checkVanilla(ServerPlayer player) {
        check(player.experienceLevel == 23 && player.totalExperience == 587 && player.experienceProgress == .625F
                        && player.getInventory().getItem(4).is(Items.DIAMOND) && player.getInventory().getItem(4).getCount() == 7,
                "A street game or shared reward changed Minecraft XP or ordinary inventory");
    }
    private static void check(boolean condition, String message) { if (!condition) throw new GameTestAssertException(message); }
    private static void pass(GameTestHelper helper, String marker) { System.out.println("JETSETCRAFT_GAMETEST_PASS " + marker); helper.succeed(); }
    private ActivityGameTests() {}
}
