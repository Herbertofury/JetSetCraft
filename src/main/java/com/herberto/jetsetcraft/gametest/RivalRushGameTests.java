package com.herberto.jetsetcraft.gametest;

import com.herberto.jetsetcraft.JetSetCraft;
import com.herberto.jetsetcraft.data.JetSetData;
import com.herberto.jetsetcraft.data.JetSetDataProvider;
import com.herberto.jetsetcraft.item.SprayCanItem;
import com.herberto.jetsetcraft.movement.RideStyle;
import com.herberto.jetsetcraft.registry.ModItems;
import com.herberto.jetsetcraft.rush.RivalRushManager;
import com.herberto.jetsetcraft.rush.RushAlbum;
import com.herberto.jetsetcraft.rush.RushReturnState;
import com.herberto.jetsetcraft.rush.RushRun;
import com.herberto.jetsetcraft.rush.RushWorld;
import com.mojang.authlib.GameProfile;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestAssertException;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.util.FakePlayerFactory;
import net.minecraftforge.event.entity.living.LivingExperienceDropEvent;
import net.minecraftforge.event.entity.living.LivingAttackEvent;
import net.minecraftforge.gametest.GameTestHolder;
import net.minecraftforge.gametest.PrefixGameTestTemplate;

import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.UUID;

/** Real Forge acceptance of the optional mode's production commands, item hooks, events and return boundary. */
@GameTestHolder(JetSetCraft.MOD_ID)
@PrefixGameTestTemplate(false)
public final class RivalRushGameTests {
    private static final ResourceLocation DEAD_BEAT = id("jetsetcraft:dead_beat");
    private static final ResourceLocation ZOMBIE = id("minecraft:zombie");

    @GameTest(template = "hoverboard_empty", timeoutTicks = 100)
    public static void rushRulesRemainDeterministicAndOfferExplicitEncore(GameTestHelper helper) {
        UUID owner = UUID.nameUUIDFromBytes("rush_rules".getBytes(StandardCharsets.UTF_8));
        RushRun first = new RushRun(UUID.randomUUID(), owner, 781L, false, DEAD_BEAT.toString());
        RushRun sameSeed = new RushRun(UUID.randomUUID(), owner, 781L, false, DEAD_BEAT.toString());
        first.gainXp(Integer.MAX_VALUE);
        sameSeed.gainXp(Integer.MAX_VALUE);
        for (int claim = 0; claim < 150; claim++) {
            check(first.offers().equals(sameSeed.offers()), "The same run seed and choices produced different offers");
            check(first.offers().size() == 3 && first.offers().stream().distinct().count() == 3L,
                    "An earned draft did not contain three distinct choices");
            int version = first.offerVersion();
            check(!first.choose(-1, version) && !first.choose(3, version) && !first.choose(0, version - 1),
                    "An invalid index or stale draft was accepted");
            check(first.choose(0, version) && sameSeed.choose(0, sameSeed.offerVersion()), "Valid draft could not be claimed");
            check(!first.choose(0, version), "One upgrade draft was claimed twice");
        }
        for (RushRun.Upgrade upgrade : RushRun.Upgrade.values()) {
            check(first.rank(upgrade) >= 0 && first.rank(upgrade) <= upgrade.maxRank(), "A stat rank overflowed its useful bound");
        }
        check(first.offers().stream().allMatch(RushRun.Upgrade::repeatable), "A completed build had no terminal rewards");
        first.addScore(Long.MAX_VALUE);
        first.addScore(Long.MAX_VALUE);
        check(first.score() > 0L && first.score() <= Long.MAX_VALUE / 4L, "Long-session score overflowed");
        check(Double.isFinite(first.damageMultiplier()) && first.cadenceMultiplier() > 0.0D
                && Double.isFinite(first.pickupRadius()), "Long-session combat statistics became invalid");
        RushRun timed = new RushRun(UUID.randomUUID(), owner, 1L, false, DEAD_BEAT.toString());
        for (long tick = 0; tick < RushRun.SESSION_TICKS; tick++) timed.tickActive();
        check(timed.awaitingEncore() && !timed.isFinished(), "Thirty minutes did not reach an explicit Encore decision");
        timed.tickActive();
        check(timed.elapsedTicks() == RushRun.SESSION_TICKS, "The Encore decision did not pause active run time");
        check(timed.continueEndless() && !timed.continueEndless(), "Encore was not an explicit one-time transition");
        timed.tickActive();
        check(timed.endless() && timed.elapsedTicks() == RushRun.SESSION_TICKS + 1L, "Encore lost the existing run");
        check(!timed.hurt(Double.NaN) && timed.vitality() == timed.maxVitality(), "Invalid damage changed run health");
        check(timed.hurt(10_000.0D) && timed.isFinished() && timed.vitality() == 0.0D, "Mode defeat did not terminate the run");
        pass(helper, "JETSETCRAFT_GAMETEST_PASS rush_rules");
    }

    @GameTest(template = "hoverboard_empty", timeoutTicks = 100)
    public static void cardTiersPersistWithoutTouchingVanillaProgression(GameTestHelper helper) {
        ServerPlayer player = player(helper, "album");
        player.experienceLevel = 17;
        player.totalExperience = 349;
        player.experienceProgress = 0.375F;
        int previousTier = 0;
        long previousTierReward = 0L;
        for (int count = 1; count <= 600; count++) {
            RushAlbum.CollectionReward reward = RushAlbum.collect(player, ZOMBIE.toString(), DEAD_BEAT.toString());
            check(reward.count() == count && reward.tier() >= previousTier, "Card count or tier regressed");
            if (RushAlbum.thresholds().contains((long) count)) {
                check(reward.newTier() && reward.tier() == previousTier + 1, "A collection threshold was skipped");
                check(reward.score() > previousTierReward, "Higher collection tiers did not yield increasing rewards");
                previousTierReward = reward.score();
                previousTier = reward.tier();
            } else check(!reward.newTier(), "A duplicate card incorrectly repeated the tier award");
            if (count == 20) check(RushAlbum.startingPerkChoices(player) == 1, "First starter draft did not unlock");
            if (count == 60) check(RushAlbum.startingPerkChoices(player) == 2, "Second starter draft did not unlock");
            if (count == 150) check(RushAlbum.startingPerkChoices(player) == 3, "Third starter draft did not unlock");
        }
        check(previousTier == 7 && RushAlbum.total(player) == 600L, "The complete seven-tier card ladder was not preserved");
        check(player.experienceLevel == 17 && player.totalExperience == 349 && player.experienceProgress == 0.375F,
                "Collecting Rush cards mutated Minecraft XP");
        check(RushAlbum.collect(player, "invalid registry id", DEAD_BEAT.toString()).runXp() == 0
                && RushAlbum.total(player) == 600L, "Malformed card identity earned a reward");
        RushAlbum.collect(player, ZOMBIE.toString(), "jetsetcraft:bone_drones");
        check(RushAlbum.uniqueCount(player) == 2, "Two crew identities for one mob were collapsed into one card");
        CompoundTag persisted = player.getPersistentData().getCompound(Player.PERSISTED_NBT_TAG).copy();
        CompoundTag roundTrip = new CompoundTag();
        roundTrip.merge(persisted);
        player.getPersistentData().put(Player.PERSISTED_NBT_TAG, roundTrip);
        check(RushAlbum.total(player) == 601L && RushAlbum.entries(player, 0, 1).size() == 1
                && RushAlbum.entries(player, 1, 1).size() == 1 && RushAlbum.entries(player, 2, 1).isEmpty(),
                "Persisted card album or pagination did not round-trip");
        CompoundTag beforeBrowse = player.getPersistentData().copy();
        RushAlbum.entries(player, Integer.MAX_VALUE, Integer.MAX_VALUE);
        check(beforeBrowse.equals(player.getPersistentData()), "Read-only album browsing changed saved player data");
        pass(helper, "JETSETCRAFT_GAMETEST_PASS rush_album");
    }

    @GameTest(template = "hoverboard_empty", timeoutTicks = 120)
    public static void returnJournalRecoversOrphanedRunsAndRejectsCorruption(GameTestHelper helper) {
        ServerPlayer player = player(helper, "return");
        prepareBelongings(player);
        CompoundTag beforeRide = ride(player).save();
        ListTag beforeInventory = player.getInventory().save(new ListTag());
        player.setHealth(13.0F);
        player.setAbsorptionAmount(2.0F);
        player.getFoodData().setFoodLevel(11);
        player.getFoodData().setSaturation(2.0F);
        player.addEffect(new MobEffectInstance(MobEffects.NIGHT_VISION, 600, 0));
        check(RushReturnState.capture(player), "A valid player return snapshot could not be captured");
        CompoundTag journal = RushReturnState.journal(player);
        check(!RushReturnState.capture(player), "A pending return journal was overwritten");
        player.getInventory().clearContent();
        player.getInventory().setItem(0, new ItemStack(Items.DIRT, 9));
        ride(player).load(new CompoundTag());
        player.experienceLevel = 0; player.totalExperience = 0; player.experienceProgress = 0.0F;
        player.setHealth(4.0F); player.setAbsorptionAmount(0.0F);
        player.removeAllEffects(); player.getFoodData().setFoodLevel(20);
        RushAlbum.collect(player, ZOMBIE.toString(), DEAD_BEAT.toString());

        CompoundTag corrupted = journal.copy();
        corrupted.remove("Food");
        player.getPersistentData().getCompound(Player.PERSISTED_NBT_TAG).put(RushReturnState.KEY, corrupted);
        ListTag untouched = player.getInventory().save(new ListTag());
        check(!RushReturnState.restore(player) && RushReturnState.hasJournal(player)
                && untouched.equals(player.getInventory().save(new ListTag())), "Incomplete journal mutated inventory or was discarded");
        corrupted = journal.copy();
        corrupted.putUUID("Owner", UUID.randomUUID());
        player.getPersistentData().getCompound(Player.PERSISTED_NBT_TAG).put(RushReturnState.KEY, corrupted);
        check(!RushReturnState.restore(player) && RushReturnState.hasJournal(player), "Another player's return journal was accepted");
        corrupted = journal.copy(); corrupted.putDouble("Y", Double.NaN);
        player.getPersistentData().getCompound(Player.PERSISTED_NBT_TAG).put(RushReturnState.KEY, corrupted);
        check(!RushReturnState.restore(player), "Nonfinite return position was accepted");
        corrupted = journal.copy();
        ListTag duplicateInventory = corrupted.getList("Inventory", net.minecraft.nbt.Tag.TAG_COMPOUND);
        duplicateInventory.add(duplicateInventory.getCompound(0).copy());
        player.getPersistentData().getCompound(Player.PERSISTED_NBT_TAG).put(RushReturnState.KEY, corrupted);
        check(!RushReturnState.restore(player) && untouched.equals(player.getInventory().save(new ListTag())),
                "Ambiguous duplicate inventory slots destroyed an existing return snapshot");
        player.getPersistentData().getCompound(Player.PERSISTED_NBT_TAG).put(RushReturnState.KEY, journal.copy());

        // This is the same production recovery entry invoked on login after a server interruption.
        RivalRushManager.recover(player);
        check(!RushReturnState.hasJournal(player), "Orphaned run recovery retained a completed journal");
        check(beforeInventory.equals(player.getInventory().save(new ListTag())) && beforeRide.equals(ride(player).save()),
                "Recovery did not restore the exact physical inventory and JetSetCraft ride NBT");
        check(player.experienceLevel == 23 && player.totalExperience == 587 && player.experienceProgress == 0.625F,
                "Recovery changed vanilla XP");
        check(player.getHealth() == 13.0F && player.getAbsorptionAmount() == 2.0F
                && player.getFoodData().getFoodLevel() == 11 && player.hasEffect(MobEffects.NIGHT_VISION),
                "Recovery lost survival state or active effects");
        check(RushAlbum.total(player) == 1L, "Restoring the return journal erased a banked card");
        pass(helper, "JETSETCRAFT_GAMETEST_PASS rush_return");
    }

    @GameTest(template = "hoverboard_empty", timeoutTicks = 300)
    public static void realSessionEntryAndExitPreserveMinecraftState(GameTestHelper helper) {
        ServerPlayer player = player(helper, "session");
        prepareBelongings(player);
        ListTag inventory = player.getInventory().save(new ListTag());
        CompoundTag savedRide = ride(player).save();
        Vec3 origin = player.position();
        check(player.getServer().getLevel(RushWorld.DIMENSION) != null, "Bundled Rival Rush dimension was not loaded by Forge");
        try {
            check(RivalRushManager.start(player, DEAD_BEAT, ZOMBIE, false), "Production session entry failed");
            check(RivalRushManager.active(player) && RushWorld.isRush(player.level()) && RushReturnState.hasJournal(player),
                    "Entry did not establish a recoverable session in the separate dimension");
            check(player.getInventory().getItem(0).getItem() != Items.DIAMOND
                    && player.getInventory().getItem(1).getItem() == ModItems.SPRAY_CAN.get(),
                    "The isolated loan loadout was not installed");
            check(player.experienceLevel == 23 && player.totalExperience == 587 && player.experienceProgress == 0.625F,
                    "Entering a run repurposed vanilla XP");
            check(!RivalRushManager.start(player, DEAD_BEAT, ZOMBIE, true), "Nested sessions were accepted");
            RivalRushManager.run(player).gainXp(100);
            check(RivalRushManager.leave(player, "GameTest return"), "Production session exit failed");
            check(!RivalRushManager.active(player) && !RushReturnState.hasJournal(player) && player.serverLevel() == helper.getLevel(),
                    "Exit left run ownership, a journal, or the wrong world behind");
            check(player.position().distanceToSqr(origin) < 0.0001D
                    && inventory.equals(player.getInventory().save(new ListTag())) && savedRide.equals(ride(player).save()),
                    "Entry/exit did not restore position, physical items and original ride state");
            check(player.experienceLevel == 23 && player.totalExperience == 587 && player.experienceProgress == 0.625F,
                    "Mode XP escaped into Minecraft XP at exit");
        } finally { cleanup(player); }
        pass(helper, "JETSETCRAFT_GAMETEST_PASS rush_session");
    }

    @GameTest(template = "hoverboard_empty", timeoutTicks = 400)
    public static void actualActorsAndPickupsEnforceSessionOwnership(GameTestHelper helper) {
        ServerPlayer first = player(helper, "owner_a");
        ServerPlayer second = player(helper, "owner_b");
        prepareBelongings(first); prepareBelongings(second);
        try {
            check(RivalRushManager.start(first, DEAD_BEAT, ZOMBIE, false)
                    && RivalRushManager.start(second, DEAD_BEAT, ZOMBIE, false), "Could not start two independent production sessions");
            loadPracticeChunks(first);
            for (int tick = 0; tick < 45; tick++) RivalRushManager.tick(first.getServer());
        } catch (RuntimeException error) { cleanup(first); cleanup(second); throw error; }
        helper.runAfterDelay(2, () -> {
            try {
                List<Mob> actors = first.serverLevel().getEntitiesOfClass(Mob.class,
                        first.getBoundingBox().inflate(80.0D), mob -> RivalRushManager.belongsTo(mob, first));
                check(!actors.isEmpty(), "Production director did not spawn a real owned actor in loaded playable chunks");
                Mob target = actors.get(0);
                check(!RivalRushManager.belongsTo(target, second) && !RivalRushManager.tagEnemy(second, target, 40),
                        "A second run could damage the first run's opponent");
                double virtualHealth = RivalRushManager.run(first).vitality();
                LivingAttackEvent crossRunHit = new LivingAttackEvent(first, second.damageSources().playerAttack(second), 8.0F);
                MinecraftForge.EVENT_BUS.post(crossRunHit);
                check(crossRunHit.isCanceled() && RivalRushManager.run(first).vitality() == virtualHealth,
                        "Cross-session player damage reached run vitality");
                var ordinary = EntityType.ZOMBIE.create(helper.getLevel());
                check(ordinary != null && !RivalRushManager.tagEnemy(first, ordinary, 40), "An unowned Minecraft mob entered run combat");
                LivingExperienceDropEvent normalXp = new LivingExperienceDropEvent(ordinary, first, 20);
                MinecraftForge.EVENT_BUS.post(normalXp);
                check(normalXp.getDroppedExperience() == 20, "Rush changed unrelated source-world mob XP");
                ordinary.discard();
                long defeats = RivalRushManager.run(first).defeats();
                check(RivalRushManager.tagEnemy(first, target, 40), "The owner could not tag a real director-created actor");
                check(target.isRemoved() && RivalRushManager.run(first).defeats() == defeats + 1
                        && !RivalRushManager.tagEnemy(first, target, 40), "A defeated actor was claimed twice");
                LivingExperienceDropEvent runXp = new LivingExperienceDropEvent(target, first, 20);
                MinecraftForge.EVENT_BUS.post(runXp);
                check(runXp.getDroppedExperience() == 0, "Event actors leaked vanilla experience");
            } catch (RuntimeException error) { cleanup(first); cleanup(second); throw error; }
            helper.runAfterDelay(2, () -> {
                try {
                    List<ItemEntity> pickups = first.serverLevel().getEntitiesOfClass(ItemEntity.class,
                            first.getBoundingBox().inflate(80), item -> RivalRushManager.belongsTo(item, first));
                    check(pickups.size() == 1, "One defeated actor did not produce exactly one owned card pickup");
                    ItemEntity card = pickups.get(0);
                    first.moveTo(card.getX(), card.getY(), card.getZ(), 0.0F, 0.0F);
                    RivalRushManager.tick(first.getServer());
                    check(card.isRemoved() && RushAlbum.total(first) == 1L && RushAlbum.total(second) == 0L,
                            "Card collection ignored ownership or failed to bank the claimed card");
                    RivalRushManager.tick(first.getServer());
                    check(RushAlbum.total(first) == 1L, "A removed pickup granted its card twice");
                    check(first.experienceLevel == 23 && first.totalExperience == 587 && first.experienceProgress == 0.625F,
                            "Real combat or pickup changed Minecraft XP");
                    cleanup(first); cleanup(second);
                    pass(helper, "JETSETCRAFT_GAMETEST_PASS rush_ownership");
                } finally { cleanup(first); cleanup(second); }
            });
        });
    }

    @GameTest(template = "hoverboard_empty", timeoutTicks = 300)
    public static void realGraffitiAndDraftActionsRejectRepeatFarming(GameTestHelper helper) {
        ServerPlayer player = player(helper, "objectives");
        prepareBelongings(player);
        try {
            check(RivalRushManager.start(player, DEAD_BEAT, ZOMBIE, false), "Objective session entry failed");
            ServerLevel district = player.serverLevel();
            BlockPos support = player.blockPosition().offset(2, 1, 0);
            district.setBlockAndUpdate(support, Blocks.LIGHT_GRAY_CONCRETE.defaultBlockState());
            player.getInventory().selected = 1;
            ItemStack spray = player.getMainHandItem();
            check(spray.getItem() instanceof SprayCanItem, "The loan spray can was not available");
            Vec3 contact = Vec3.atCenterOf(support).add(-0.5D, 0.0D, 0.0D);
            UseOnContext context = new UseOnContext(player, InteractionHand.MAIN_HAND,
                    new BlockHitResult(contact, Direction.WEST, support, false));
            long before = RivalRushManager.run(player).score();
            check(spray.getItem().useOn(context).consumesAction(), "Real spray-can placement failed on the test wall");
            long scored = RivalRushManager.run(player).score();
            check(scored > before, "Successful production graffiti placement did not award run points");
            spray.getItem().useOn(context);
            check(RivalRushManager.run(player).score() == scored, "Repainting one wall repeatedly farmed run points");
            RushRun run = RivalRushManager.run(player);
            run.gainXp(100);
            check(run.hasOffer(), "Earned mode XP did not offer an upgrade");
            int version = run.offerVersion();
            RushRun.Upgrade choice = run.offers().get(0);
            int rank = run.rank(choice);
            RivalRushManager.handleAction(player, "choose", choice.name(), 0, UUID.randomUUID(), version);
            check(run.rank(choice) == rank && run.offerVersion() == version, "A stale run UUID could claim a current draft");
            RivalRushManager.handleAction(player, "choose", choice.name(), 0, run.id(), version - 1);
            check(run.rank(choice) == rank, "A stale offer version could claim a current draft");
            RivalRushManager.handleAction(player, "choose", choice.name(), 0, run.id(), version);
            check(run.rank(choice) == rank + 1, "Valid authoritative draft choice was not applied");
            RivalRushManager.handleAction(player, "choose", choice.name(), 0, run.id(), version);
            check(run.rank(choice) == rank + 1, "A replayed draft action applied twice");
            check(player.experienceLevel == 23 && player.totalExperience == 587 && player.experienceProgress == 0.625F,
                    "Graffiti or draft rewards altered Minecraft XP");
        } finally { cleanup(player); }
        pass(helper, "JETSETCRAFT_GAMETEST_PASS rush_objectives");
    }

    private static ServerPlayer player(GameTestHelper helper, String suffix) {
        UUID uuid = UUID.nameUUIDFromBytes((JetSetCraft.MOD_ID + ":gametest:rush:" + suffix).getBytes(StandardCharsets.UTF_8));
        ServerPlayer player = FakePlayerFactory.get(helper.getLevel(), new GameProfile(uuid, "JSC_" + suffix));
        if (RivalRushManager.active(player)) RivalRushManager.leave(player, "Reset GameTest player");
        player.getPersistentData().remove(Player.PERSISTED_NBT_TAG);
        player.getInventory().clearContent();
        player.containerMenu.setCarried(ItemStack.EMPTY);
        player.setGameMode(GameType.SURVIVAL);
        player.removeAllEffects(); player.setHealth(player.getMaxHealth()); player.setAbsorptionAmount(0.0F);
        BlockPos origin = helper.absolutePos(new BlockPos(1, 2, 1));
        player.moveTo(origin.getX() + 0.5D, origin.getY(), origin.getZ() + 0.5D, 17.0F, 4.0F);
        return player;
    }

    private static void prepareBelongings(ServerPlayer player) {
        ItemStack diamonds = new ItemStack(Items.DIAMOND, 7);
        diamonds.getOrCreateTag().putString("ReturnProof", "original inventory, not a loan");
        player.getInventory().setItem(0, diamonds);
        player.getInventory().setItem(8, new ItemStack(Items.BREAD, 5));
        player.getInventory().selected = 8;
        ItemStack physicalRide = new ItemStack(ModItems.HOVERBOARD.get());
        physicalRide.getOrCreateTag().putString("ReturnProof", "original physical ride");
        JetSetData ride = ride(player);
        ride.load(new CompoundTag()); ride.setRideGear(physicalRide); ride.setStyle(RideStyle.HOVER);
        ride.setActive(true); ride.setBoost(37.0F); ride.setFlow(12.0F); ride.setComboScore(91);
        player.experienceLevel = 23; player.totalExperience = 587; player.experienceProgress = 0.625F;
    }

    private static JetSetData ride(ServerPlayer player) {
        return player.getCapability(JetSetDataProvider.CAPABILITY).resolve()
                .orElseThrow(() -> new GameTestAssertException("Real Forge player capability is missing"));
    }

    private static void loadPracticeChunks(ServerPlayer player) {
        // Test setup supplies genuinely loaded terrain; production continues to use hasChunk without tickets.
        int x = player.blockPosition().getX() >> 4, z = player.blockPosition().getZ() >> 4;
        for (int dx = -2; dx <= 2; dx++) for (int dz = -2; dz <= 2; dz++)
            player.serverLevel().getChunk(x + dx, z + dz);
    }

    private static void cleanup(ServerPlayer player) {
        if (RivalRushManager.active(player) || RushReturnState.hasJournal(player)) RivalRushManager.leave(player, "GameTest cleanup");
    }

    private static ResourceLocation id(String id) { return ResourceLocation.tryParse(id); }
    private static void check(boolean condition, String message) { if (!condition) throw new GameTestAssertException(message); }
    private static void pass(GameTestHelper helper, String marker) {
        System.out.println(marker);
        helper.succeed();
    }
    private RivalRushGameTests() {}
}
