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
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.block.Blocks;
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

    @GameTest(template = "hoverboard_empty", timeoutTicks = 100)
    public static void pendingRewardsTransferReceiptsAndTrainingExactlyOnce(GameTestHelper helper) {
        BlockPos anchor = new BlockPos(24000, 100, 16000);
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
        check(beforeFound.getInt("Schema") == 2 && beforeFound.equals(CrewHouseData.load(beforeFound).save(new CompoundTag())),
                "Pending amounts or receipt identities did not round-trip through world SavedData");
        helper.getLevel().setBlockAndUpdate(anchor.below(), Blocks.STONE.defaultBlockState());
        helper.getLevel().setBlockAndUpdate(anchor, ModBlocks.BOOMBOX.get().defaultBlockState());
        CrewHouseManager.openAt(owner, anchor);
        CrewHouseManager.handleAction(owner, "found", "Every mode crew", 0, null, null, 0);
        CrewProfile home = store.profile(owner.getUUID());
        check(home != null && home.supplies() == 80 && home.xp() == 277 && home.members().get(0).xp() == 110
                        && home.reputation(GANG.toString()) == 28 && store.pendingSupplies(owner.getUUID()) == 0
                        && store.pendingTraining(owner.getUUID()) == 0 && store.pendingXp(owner.getUUID()) == 0,
                "Founding failed to transfer pending supplies, crew/member XP and reputation exactly once");
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
            action(owner, "launch", ""); check(ActivityManager.active(owner), "The accepted explicit Tag game did not start");
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

    @GameTest(template = "hoverboard_empty", timeoutTicks = 260)
    public static void randomPreviewStaleActionsAndMissingActorsCannotCompleteAGame(GameTestHelper helper) {
        BlockPos anchor = arena(helper, 28000);
        ServerPlayer owner = player(helper, "safety", anchor);
        ActivityManager.open(owner);
        CompoundTag random = ActivityManager.snapshot(owner);
        check(random.getString("SelectedMode").equals("random") && !ActivityManager.active(owner)
                        && random.getList("Activities", 10).size() == 5,
                "The generic invitation did not put random and four actual modes in one optional catalogue");
        helper.runAfterDelay(5, () -> {
            action(owner, "refresh", "");
            check(ActivityManager.snapshot(owner).getString("Mode").equals(random.getString("Mode"))
                            && ActivityManager.snapshot(owner).getUUID("Context").equals(random.getUUID("Context")),
                    "Refreshing rerolled or implicitly launched the revealed random invitation");
            helper.runAfterDelay(5, () -> {
                ActivityManager.openAt(owner, anchor);
                CompoundTag stale = ActivityManager.snapshot(owner);
                helper.runAfterDelay(5, () -> {
                    action(owner, "select", "showdown");
                    check(ActivityManager.snapshot(owner).getString("Mode").equals("showdown"), "Explicit mode selection was ignored");
                    helper.runAfterDelay(5, () -> {
                        ActivityManager.handleAction(owner, "launch", "", stale.getUUID("Context"), stale.getInt("Version"));
                        check(!ActivityManager.active(owner), "A stale selection launched a different game than the reviewed mode");
                        helper.runAfterDelay(5, () -> {
                            action(owner, "launch", "");
                            check(ActivityManager.active(owner), "Fresh Showdown confirmation was not accepted");
                            helper.runAfterDelay(30, () -> showdownFirstHit(helper, owner, anchor));
                        });
                    });
                });
            });
        });
    }

    private static void showdownFirstHit(GameTestHelper helper, ServerPlayer owner, BlockPos anchor) {
        List<Mob> live = actors(owner, anchor); check(live.size() >= 2, "Showdown did not spawn its real protected source actors");
        Mob rival = live.get(0); approach(owner, rival.position()); owner.attack(rival);
        check(!rival.isRemoved() && ActivityManager.snapshot(owner).getInt("Progress") == 0,
                "A one-hit tap completed a three-tag Showdown rival");
        Mob orphan = EntityType.PIG.create(helper.getLevel()); check(orphan != null, "Missing vanilla test mob");
        orphan.moveTo(rival.position());
        orphan.getPersistentData().put(ActivityManager.OWNED, rival.getPersistentData().getCompound(ActivityManager.OWNED).copy());
        check(!helper.getLevel().addFreshEntity(orphan), "A copied/reloaded activity marker joined as a replacement objective");
        helper.runAfterDelay(9, () -> {
            approach(owner, rival.position()); owner.attack(rival);
            check(!rival.isRemoved(), "The second Showdown tag defeated a three-tag rival");
            helper.runAfterDelay(9, () -> {
                approach(owner, rival.position()); owner.attack(rival);
                check(rival.isRemoved() && ActivityManager.snapshot(owner).getInt("Progress") == 1,
                        "Three actual separated owner tags did not defeat exactly one Showdown rival");
                actors(owner, anchor).get(0).discard();
                helper.runAfterDelay(3, () -> {
                    check(!ActivityManager.active(owner) && !ActivityManager.snapshot(owner).getBoolean("Won")
                                    && CrewHouseData.get(owner.getServer()).pendingSupplies(owner.getUUID()) == 0,
                            "A missing/unloaded rival produced a false completion reward");
                    owner.moveTo(anchor.getX() + .5, anchor.getY() + 1, anchor.getZ() + .5, 0, 0);
                    action(owner, "select", "tag");
                    helper.runAfterDelay(5, () -> {
                        action(owner, "launch", ""); check(ActivityManager.active(owner), "A cancelled game prevented a later explicit game");
                        helper.runAfterDelay(15, () -> {
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
        helper.runAfterDelay(5, () -> {
            action(owner, "select", "king_of_hill");
            helper.runAfterDelay(5, () -> {
                action(owner, "launch", ""); check(ActivityManager.active(owner), "A clear three-site hill layout was rejected");
                helper.runAfterDelay(35, () -> {
                    List<Mob> rivals = actors(owner, anchor); check(rivals.size() == 3, "Hill mode did not create its three contenders");
                    Vec3 hill = hill(anchor, 0); owner.moveTo(hill);
                    for (Mob mob : rivals) mob.moveTo(hill.add(1, 0, 0));
                    int before = ActivityManager.snapshot(owner).getInt("Progress");
                    helper.runAfterDelay(5, () -> {
                        check(ActivityManager.snapshot(owner).getInt("Progress") == before,
                                "Contested hill presence incorrectly advanced capture time");
                        for (Mob mob : actors(owner, anchor)) owner.attack(mob);
                        helper.runAfterDelay(20, () -> {
                            int held = ActivityManager.snapshot(owner).getInt("Progress");
                            check(held > before, "Tagging contenders did not open a real capture window");
                            owner.moveTo(Vec3.atBottomCenterOf(anchor));
                            helper.runAfterDelay(20, () -> {
                                check(ActivityManager.snapshot(owner).getInt("Progress") == held,
                                        "Being outside the hill still earned capture time");
                                hillLoop(helper, owner, anchor, 0);
                            });
                        });
                    });
                });
            });
        });
    }

    private static void hillLoop(GameTestHelper helper, ServerPlayer owner, BlockPos anchor, int attempt) {
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
            helper.runAfterDelay(10, () -> hillLoop(helper, owner, anchor, attempt + 1));
        } catch (RuntimeException error) { ActivityManager.forget(owner); throw error; }
    }

    private static Vec3 hill(BlockPos anchor, int index) {
        BlockPos offset = switch (index) { case 0 -> anchor.east(7); case 1 -> anchor.south(7); default -> anchor.west(7); };
        return Vec3.atBottomCenterOf(offset);
    }
    private static BlockPos arena(GameTestHelper helper, int x) {
        BlockPos anchor = new BlockPos(x, 100, 16000);
        for (int dx = -12; dx <= 12; dx++) for (int dz = -12; dz <= 12; dz++)
            helper.getLevel().setBlockAndUpdate(anchor.offset(dx, -1, dz), Blocks.STONE.defaultBlockState());
        helper.getLevel().setBlockAndUpdate(anchor, ModBlocks.BOOMBOX.get().defaultBlockState());
        BoomboxBlockEntity box = (BoomboxBlockEntity) helper.getLevel().getBlockEntity(anchor);
        ItemStack head = new ItemStack(Items.PLAYER_HEAD);
        head.getOrCreateTag().putString(HeadGangTargetResolver.TARGET_ENTITY_KEY, "minecraft:pig");
        head.getOrCreateTag().putString(HeadGangTargetResolver.TARGET_GANG_KEY, GANG.toString());
        check(box.setTarget(head), "The real test Boombox rejected an explicit source-mob adapter head");
        return anchor;
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
    private static void checkVanilla(ServerPlayer player) {
        check(player.experienceLevel == 23 && player.totalExperience == 587 && player.experienceProgress == .625F
                        && player.getInventory().getItem(4).is(Items.DIAMOND) && player.getInventory().getItem(4).getCount() == 7,
                "A street game or shared reward changed Minecraft XP or ordinary inventory");
    }
    private static void check(boolean condition, String message) { if (!condition) throw new GameTestAssertException(message); }
    private static void pass(GameTestHelper helper, String marker) { System.out.println("JETSETCRAFT_GAMETEST_PASS " + marker); helper.succeed(); }
    private ActivityGameTests() {}
}
