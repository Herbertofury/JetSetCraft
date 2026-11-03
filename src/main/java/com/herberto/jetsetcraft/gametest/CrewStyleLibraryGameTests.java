package com.herberto.jetsetcraft.gametest;

import com.herberto.jetsetcraft.JetSetCraft;
import com.herberto.jetsetcraft.compat.RushSpellCompat;
import com.herberto.jetsetcraft.crew.CrewHouseData;
import com.herberto.jetsetcraft.crew.CrewHouseManager;
import com.herberto.jetsetcraft.crew.CrewProfile;
import com.herberto.jetsetcraft.crew.CrewRewardReceipt;
import com.herberto.jetsetcraft.crew.CrewStyleLibrary;
import com.herberto.jetsetcraft.network.C2SCrewActionPacket;
import com.herberto.jetsetcraft.registry.ModBlocks;
import com.herberto.jetsetcraft.rush.RivalRushManager;
import com.herberto.jetsetcraft.rush.RushReturnState;
import com.herberto.jetsetcraft.rush.RushRun;
import com.mojang.authlib.GameProfile;
import io.netty.buffer.Unpooled;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestAssertException;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.block.Blocks;
import net.minecraftforge.common.util.FakePlayerFactory;
import net.minecraftforge.gametest.GameTestHolder;
import net.minecraftforge.gametest.PrefixGameTestTemplate;

import java.util.UUID;

/** Production shared settlements, saved style choices, and the actual house-to-Rush preparation handoff. */
@GameTestHolder(JetSetCraft.MOD_ID)
@PrefixGameTestTemplate(false)
public final class CrewStyleLibraryGameTests {
    private static final ResourceLocation GANG = ResourceLocation.tryParse("jetsetcraft:dead_beat");
    private static final ResourceLocation ZOMBIE = ResourceLocation.tryParse("minecraft:zombie");

    @GameTest(template = "hoverboard_empty", timeoutTicks = 100)
    public static void everySharedRewardSourceUnlocksLessonsBeforeAndAfterFoundingWithoutReplay(GameTestHelper helper) {
        BlockPos anchor = freeAnchor(helper, 46000);
        ServerPlayer player = player(helper, "growth", anchor);
        CrewHouseData data = CrewHouseData.get(player.getServer());
        UUID tagReceipt = UUID.randomUUID(), rushReceipt = UUID.randomUUID();
        check(!CrewStyleLibrary.canSelect(data, player.getUUID(), "BATTERY") && unlocked(data, player) == 0,
                "An unearned lesson was already selectable before any shared result");
        check(CrewHouseManager.bankActivity(player, tagReceipt, GANG, "tag", 400, 6, 600).accepted()
                        && data.styleXp(player.getUUID()) == 55 && unlocked(data, player) == 1
                        && CrewStyleLibrary.canSelect(data, player.getUUID(), "BATTERY"),
                "A real Tag settlement did not unlock the first starting style before founding");
        choose(player, "BATTERY", 0);
        check(data.selectedStyle(player.getUUID()).equals("BATTERY") && data.styleXp(player.getUUID()) == 55,
                "Selecting an earned style spent shared XP or required a house");
        check(CrewHouseManager.bankActivity(player, UUID.randomUUID(), GANG, "king_of_hill", 500, 3, 600).accepted()
                        && data.styleXp(player.getUUID()) == 145 && unlocked(data, player) == 2,
                "King of the Hill did not contribute to the same permanent lesson progress");
        check(CrewHouseManager.bankActivity(player, UUID.randomUUID(), GANG, "showdown", 400, 4, 600).accepted()
                        && data.styleXp(player.getUUID()) == 220 && unlocked(data, player) == 3,
                "Showdown did not unlock the next lesson in the shared account");
        CrewHouseManager.bankRush(player, rushReceipt, GANG, 1200, 3, 3, 600);
        check(data.styleXp(player.getUUID()) == 252 && unlocked(data, player) == 3
                        && !CrewStyleLibrary.canSelect(data, player.getUUID(), "WARD"),
                "A qualifying Rush return was lost or unlocked a lesson before its actual threshold");
        CompoundTag pending = data.save(new CompoundTag());
        CrewHouseData pendingReload = CrewHouseData.load(pending);
        check(pendingReload.styleXp(player.getUUID()) == 252
                        && pendingReload.selectedStyle(player.getUUID()).equals("BATTERY")
                        && pendingReload.save(new CompoundTag()).equals(pending),
                "Pre-house lessons or their selected style failed the real SavedData round trip");

        CrewProfile home = found(player, anchor);
        check(home.xp() == 252 && data.pendingXp(player.getUUID()) == 0
                        && data.styleXp(player.getUUID()) == 252 && unlocked(data, player) == 3
                        && data.selectedStyle(player.getUUID()).equals("BATTERY"),
                "Founding reset or doubled lesson progress, or discarded the pre-house selection");
        check(!CrewHouseManager.bankActivity(player, tagReceipt, GANG, "tag", 400, 6, 600).accepted(),
                "A pre-house Tag receipt became payable again after founding");
        CrewHouseManager.bankRush(player, rushReceipt, GANG, 1200, 3, 3, 600);
        check(data.styleXp(player.getUUID()) == 252 && unlocked(data, player) == 3 && data.recentRewards(player.getUUID()).size() == 4,
                "Replaying settled activity/Rush receipts changed the lesson count or reward history");

        CrewProfile.Job job = home.assignMission(home.members().get(0).id(), "courier_run", 1000, 0);
        check(job != null && home.claimMissionReward(job.id(), job.readyAt() - 1) == null,
                "The production mission could not dispatch or paid before its persisted deadline");
        // Use the real claim at its saved deadline; never advance the shared clock of concurrent GameTests.
        CrewRewardReceipt mission = home.claimMissionReward(job.id(), job.readyAt());
        check(mission != null && mission.crewXp() == 40 && mission.source().equals("mission:courier_run")
                        && data.styleXp(player.getUUID()) == 292 && unlocked(data, player) == 4
                        && CrewStyleLibrary.canSelect(data, player.getUUID(), "WARD"),
                "A real mission claim did not advance the same XP balance across the Beat ward threshold");
        check(home.claimMissionReward(job.id(), job.readyAt()) == null && data.styleXp(player.getUUID()) == 292
                        && unlocked(data, player) == 4,
                "A repeated mission claim produced another lesson or additional XP");
        CompoundTag saved = data.save(new CompoundTag());
        CrewHouseData reloaded = CrewHouseData.load(saved);
        check(reloaded.styleXp(player.getUUID()) == 292 && unlocked(reloaded, player) == 4
                        && reloaded.selectedStyle(player.getUUID()).equals("BATTERY")
                        && saved.equals(reloaded.save(new CompoundTag())),
                "Founded lesson progress diverged from its saved shared reward account");
        checkVanilla(player); pass(helper, "crew_style_shared_rewards");
    }

    @GameTest(template = "hoverboard_empty", timeoutTicks = 100)
    public static void styleActionsRejectLockedStaleAndUnknownChoicesAndMigrateSavedSchemas(GameTestHelper helper) {
        BlockPos anchor = freeAnchor(helper, 48000);
        ServerPlayer player = player(helper, "saved", anchor);
        CrewHouseData data = CrewHouseData.get(player.getServer());
        choose(player, "WARD", 0);
        check(data.selectedStyle(player.getUUID()).equals(CrewStyleLibrary.HOME) && data.styleRevision(player.getUUID()) == 0,
                "The production action selected a lesson without its earned XP");
        CrewHouseManager.bankActivity(player, UUID.randomUUID(), GANG, "tag", 400, 6, 600);
        CrewHouseManager.bankActivity(player, UUID.randomUUID(), GANG, "king_of_hill", 500, 3, 600);
        CrewProfile home = found(player, anchor);
        choose(player, "BATTERY", 0);
        check(data.selectedStyle(player.getUUID()).equals("BATTERY") && data.styleRevision(player.getUUID()) == 1,
                "A fresh earned style action did not record exactly one revision");
        choose(player, "AIR_BREAK", -1);
        packet(player, "unknown_style", "AIR_BREAK", 1);
        check(data.selectedStyle(player.getUUID()).equals("BATTERY") && data.styleRevision(player.getUUID()) == 1
                        && data.styleXp(player.getUUID()) == 145,
                "The decoded packet boundary admitted a negative revision or unknown action");
        choose(player, "AIR_BREAK", 0);
        choose(player, "NOT_A_STYLE", 1);
        choose(player, "WARD", 1);
        check(data.selectedStyle(player.getUUID()).equals("BATTERY") && data.styleRevision(player.getUUID()) == 1
                        && data.styleXp(player.getUUID()) == 145,
                "A stale, unknown or locked style changed the selection, revision or shared XP");
        choose(player, "AIR_BREAK", 1);
        check(data.selectedStyle(player.getUUID()).equals("AIR_BREAK") && data.styleRevision(player.getUUID()) == 2
                        && home.xp() == 145,
                "The fresh second choice was rejected or charged the persistent XP balance");
        CompoundTag view = CrewHouseManager.snapshot(player).getCompound("StyleLibrary");
        check(view.getString("Selected").equals("AIR_BREAK") && view.getLong("Revision") == 2
                        && view.getInt("Unlocked") == 2 && view.getBoolean("CanManage"),
                "The authoritative library snapshot did not expose the accepted selection and revision");
        CompoundTag saved = data.save(new CompoundTag());
        CrewHouseData reloaded = CrewHouseData.load(saved);
        check(saved.getInt("Schema") == 4 && saved.equals(reloaded.save(new CompoundTag()))
                        && reloaded.styleRevision(player.getUUID()) == 2
                        && CrewStyleLibrary.preparation(reloaded, player.getUUID()).orElse(null) == RushRun.Upgrade.AIR_BREAK,
                "Schema four lost the selected style, revision or resulting preparation");

        for (int schema = 1; schema <= 2; schema++) {
            CompoundTag legacy = saved.copy(); legacy.putInt("Schema", schema); legacy.remove("StyleChoices");
            if (schema == 1) legacy.remove("PendingProgress");
            CrewHouseData migrated = CrewHouseData.load(legacy);
            check(!migrated.readOnly() && !migrated.blocked(player.getUUID())
                            && migrated.profile(player.getUUID()).save().equals(home.save())
                            && migrated.styleXp(player.getUUID()) == 145 && unlocked(migrated, player) == 2
                            && migrated.selectedStyle(player.getUUID()).equals(CrewStyleLibrary.HOME)
                            && migrated.styleRevision(player.getUUID()) == 0 && migrated.save(new CompoundTag()).getInt("Schema") == 4,
                    "Schema " + schema + " migration lost existing XP or invented a prior style selection");
        }
        CompoundTag future = saved.copy(); future.putInt("Schema", 999); future.putString("FutureLibraryField", "preserve me");
        CrewHouseData readOnly = CrewHouseData.load(future);
        check(readOnly.readOnly() && readOnly.blocked(player.getUUID())
                        && !readOnly.selectStyle(player.getUUID(), CrewStyleLibrary.HOME, 0)
                        && !readOnly.add(new CrewProfile(UUID.randomUUID(), UUID.randomUUID(), "Future fixture", GANG.toString(),
                            player.level().dimension().location().toString(), anchor.offset(128, 0, 0)))
                        && readOnly.save(new CompoundTag()).equals(future),
                "An unknown future schema permitted mutation or discarded its exact payload");
        checkVanilla(player); pass(helper, "crew_style_persistence");
    }

    @GameTest(template = "hoverboard_empty", timeoutTicks = 240)
    public static void providerGatedStyleUsesOnePreparationAndCannotChangeInsideAnActualRun(GameTestHelper helper) {
        BlockPos anchor = freeAnchor(helper, 50000);
        ServerPlayer player = player(helper, "entry", anchor);
        CrewHouseData data = CrewHouseData.get(player.getServer());
        // These are real capped settlements with distinct receipt IDs, not a direct XP fixture mutation.
        for (int result = 0; result < 9; result++)
            CrewHouseManager.bankRush(player, UUID.randomUUID(), GANG, 36000, 20, 100, 5000);
        check(data.styleXp(player.getUUID()) == 2160 && unlocked(data, player) == 16,
                "Earned shared results did not reach the optional relay lesson");
        boolean relayAvailable = RushSpellCompat.fireboltAvailable();
        CompoundTag relay = lesson(CrewHouseManager.snapshot(player).getCompound("StyleLibrary"), "SPELL_RELAY");
        check(relay.getBoolean("Unlocked") && relay.getBoolean("Available") == relayAvailable,
                "The library conflated earning the relay lesson with having its provider API installed");
        choose(player, "SPELL_RELAY", 0);
        if (relayAvailable) {
            check(data.selectedStyle(player.getUUID()).equals("SPELL_RELAY") && data.styleRevision(player.getUUID()) == 1
                            && CrewHouseManager.rushPreparation(player).orElse(null) == RushRun.Upgrade.SPELL_RELAY,
                    "An installed and API-compatible relay could not be selected after earning it");
        } else {
            check(data.selectedStyle(player.getUUID()).equals(CrewStyleLibrary.HOME) && data.styleRevision(player.getUUID()) == 0
                            && CrewHouseManager.rushPreparation(player).isEmpty(),
                    "An absent optional relay changed the selection or created a phantom preparation");
        }
        choose(player, "BATTERY", data.styleRevision(player.getUUID()));
        long selectedRevision = data.styleRevision(player.getUUID());
        CrewProfile home = found(player, anchor);
        BlockPos workshop = anchor.east(2);
        player.serverLevel().setBlockAndUpdate(workshop.below(), Blocks.STONE.defaultBlockState());
        player.serverLevel().setBlockAndUpdate(workshop, Blocks.CRAFTING_TABLE.defaultBlockState());
        CrewHouseManager.openAt(player, workshop);
        CrewHouseManager.handleAction(player, "facility", "workshop", 0, home.id(), null, home.revision());
        CrewHouseManager.handleAction(player, "preparation", "FORCE", 0, home.id(), null, home.revision());
        check(CrewHouseManager.rushPreparation(player).orElse(null) == RushRun.Upgrade.BATTERY,
                "An operational house preparation displaced the selected earned artifact");
        ListTag inventory = player.getInventory().save(new ListTag());
        try {
            check(RivalRushManager.start(player, GANG, ZOMBIE, false), "The normal Rush entry rejected the earned-style preparation");
            RushRun run = RivalRushManager.run(player);
            int totalRanks = 0;
            for (RushRun.Upgrade upgrade : RushRun.Upgrade.values()) totalRanks += run.rank(upgrade);
            check(run.rank(RushRun.Upgrade.BATTERY) == 1 && run.rank(RushRun.Upgrade.FORCE) == 0
                            && run.artifactCount() == 1 && totalRanks == 1 && !run.hasOffer()
                            && !run.grantStartingPerk(RushRun.Upgrade.WARD),
                    "Production entry stacked house and style ranks, manufactured a draft, or admitted a second preparation");
            choose(player, "AIR_BREAK", selectedRevision);
            check(data.selectedStyle(player.getUUID()).equals("BATTERY") && data.styleRevision(player.getUUID()) == selectedRevision
                            && !CrewHouseManager.snapshot(player).getCompound("StyleLibrary").getBoolean("CanManage")
                            && CrewHouseManager.rushPreparation(player).isEmpty() && run.rank(RushRun.Upgrade.AIR_BREAK) == 0,
                    "An active Rush allowed the style action to alter saved or live build state");
            check(RivalRushManager.leave(player, "Style library integration return") && !RivalRushManager.active(player)
                            && !RushReturnState.hasJournal(player) && player.serverLevel() == helper.getLevel(),
                    "The earned style prevented the ordinary production return path");
        } finally {
            if (RivalRushManager.active(player) || RushReturnState.hasJournal(player))
                check(RivalRushManager.leave(player, "Style library fixture cleanup"), "Could not restore the style fixture player");
        }
        check(data.styleXp(player.getUUID()) == 2160 && data.selectedStyle(player.getUUID()).equals("BATTERY")
                        && data.styleRevision(player.getUUID()) == selectedRevision
                        && inventory.equals(player.getInventory().save(new ListTag())),
                "Entering or returning spent shared XP, replayed a reward, or changed the persisted choice or normal inventory");
        choose(player, CrewStyleLibrary.HOME, selectedRevision);
        check(CrewHouseManager.rushPreparation(player).orElse(null) == RushRun.Upgrade.FORCE,
                "Choosing the house option did not restore the single operational workshop preparation");
        player.serverLevel().setBlockAndUpdate(workshop, Blocks.AIR.defaultBlockState());
        check(CrewHouseManager.rushPreparation(player).isEmpty(),
                "A removed workshop still supplied a house preparation after clearing the learned style");
        checkVanilla(player);
        System.out.println("JETSETCRAFT_GAMETEST_STYLE_RELAY " + (relayAvailable ? "provider_present" : "provider_absent"));
        pass(helper, "crew_style_entry");
    }

    private static int unlocked(CrewHouseData data, ServerPlayer player) {
        return CrewStyleLibrary.snapshot(data, player.getUUID(), true).getInt("Unlocked");
    }
    private static CompoundTag lesson(CompoundTag library, String id) {
        ListTag lessons = library.getList("Lessons", Tag.TAG_COMPOUND);
        for (int i = 0; i < lessons.size(); i++) if (lessons.getCompound(i).getString("Id").equals(id)) return lessons.getCompound(i);
        throw new GameTestAssertException("Missing style lesson " + id);
    }
    private static void choose(ServerPlayer player, String id, long revision) {
        packet(player, "style", id, revision);
    }
    private static void packet(ServerPlayer player, String action, String value, long revision) {
        FriendlyByteBuf buffer = new FriendlyByteBuf(Unpooled.buffer());
        try {
            C2SCrewActionPacket intent = new C2SCrewActionPacket(action, value, 0, null, null, revision);
            C2SCrewActionPacket.encode(intent, buffer);
            C2SCrewActionPacket received = C2SCrewActionPacket.decode(buffer);
            check(received.equals(intent) && buffer.readableBytes() == 0,
                    "The real crew packet codec changed the style intent or left unread payload");
            C2SCrewActionPacket.handleForPlayer(received, player);
        } finally {
            buffer.release();
        }
    }
    private static BlockPos freeAnchor(GameTestHelper helper, int initialX) {
        CrewHouseData data = CrewHouseData.get(helper.getLevel().getServer());
        for (int attempt = 0; attempt < 128; attempt++) {
            BlockPos anchor = new BlockPos(initialX + attempt * 128, 100, 28000);
            if (data.profiles().stream().noneMatch(profile -> {
                BlockPos existing = BlockPos.of(profile.save().getLong("Anchor"));
                return Math.abs((long) existing.getX() - anchor.getX()) < 100 && Math.abs((long) existing.getZ() - anchor.getZ()) < 100;
            })) return anchor;
        }
        throw new GameTestAssertException("No isolated plot is available for the style library fixture");
    }
    private static ServerPlayer player(GameTestHelper helper, String name, BlockPos anchor) {
        helper.getLevel().getChunkAt(anchor);
        for (int x = -2; x <= 2; x++) for (int z = -1; z <= 3; z++) {
            BlockPos floor = anchor.offset(x, -1, z);
            helper.getLevel().setBlockAndUpdate(floor, Blocks.STONE.defaultBlockState());
            helper.getLevel().setBlockAndUpdate(floor.above(), Blocks.AIR.defaultBlockState());
            helper.getLevel().setBlockAndUpdate(floor.above(2), Blocks.AIR.defaultBlockState());
        }
        ServerPlayer player = FakePlayerFactory.get(helper.getLevel(), new GameProfile(UUID.randomUUID(), "JSC_style_" + name));
        player.setGameMode(GameType.SURVIVAL); player.setHealth(player.getMaxHealth());
        player.moveTo(anchor.getX() + .5, anchor.getY(), anchor.getZ() + 2.5, 0, 0);
        player.getInventory().clearContent(); player.containerMenu.setCarried(ItemStack.EMPTY);
        player.getInventory().setItem(0, new ItemStack(Items.DIAMOND, 7));
        player.experienceLevel = 23; player.totalExperience = 587; player.experienceProgress = .625F;
        return player;
    }
    private static CrewProfile found(ServerPlayer player, BlockPos anchor) {
        player.serverLevel().setBlockAndUpdate(anchor.below(), Blocks.STONE.defaultBlockState());
        player.serverLevel().setBlockAndUpdate(anchor, ModBlocks.BOOMBOX.get().defaultBlockState());
        CrewHouseManager.openAt(player, anchor);
        CrewHouseManager.handleAction(player, "found", "Style library crew", 0, null, null, 0);
        CrewProfile home = CrewHouseData.get(player.getServer()).profile(player.getUUID());
        check(home != null, "Production founding failed at the isolated placed Boombox");
        return home;
    }
    private static void checkVanilla(ServerPlayer player) {
        check(player.experienceLevel == 23 && player.totalExperience == 587 && player.experienceProgress == .625F
                        && player.getInventory().getItem(0).is(Items.DIAMOND) && player.getInventory().getItem(0).getCount() == 7,
                "The permanent style library changed ordinary Minecraft XP or inventory");
    }
    private static void check(boolean condition, String message) { if (!condition) throw new GameTestAssertException(message); }
    private static void pass(GameTestHelper helper, String marker) { System.out.println("JETSETCRAFT_GAMETEST_PASS " + marker); helper.succeed(); }
    private CrewStyleLibraryGameTests() {}
}
