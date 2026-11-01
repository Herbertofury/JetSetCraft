package com.herberto.jetsetcraft.gametest;

import com.herberto.jetsetcraft.JetSetCraft;
import com.herberto.jetsetcraft.crew.CrewHouseData;
import com.herberto.jetsetcraft.crew.CrewHouseManager;
import com.herberto.jetsetcraft.crew.CrewProfile;
import com.herberto.jetsetcraft.registry.ModBlocks;
import com.herberto.jetsetcraft.rush.RivalRushManager;
import com.herberto.jetsetcraft.rush.RushAlbum;
import com.herberto.jetsetcraft.rush.RushReturnState;
import com.herberto.jetsetcraft.rush.RushRun;
import com.mojang.authlib.GameProfile;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestAssertException;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.nbt.ListTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.block.Blocks;
import net.minecraftforge.common.util.FakePlayerFactory;
import net.minecraftforge.gametest.GameTestHolder;
import net.minecraftforge.gametest.PrefixGameTestTemplate;

import java.util.UUID;

/** Cross-system acceptance: a physically built house changes a real session and settles on its return path. */
@GameTestHolder(JetSetCraft.MOD_ID)
@PrefixGameTestTemplate(false)
public final class CrewRushGameTests {
    @GameTest(template = "hoverboard_empty", timeoutTicks = 400)
    public static void housePreparationAndGenuineRivalsSettleOnRushReturn(GameTestHelper helper) {
        BlockPos anchor = freeAnchor(helper);
        helper.getLevel().getChunkAt(anchor);
        helper.getLevel().setBlockAndUpdate(anchor.below(), Blocks.STONE.defaultBlockState());
        helper.getLevel().setBlockAndUpdate(anchor, ModBlocks.BOOMBOX.get().defaultBlockState());
        ServerPlayer player = FakePlayerFactory.get(helper.getLevel(), new GameProfile(UUID.randomUUID(), "JSC_crew_rush"));
        player.setGameMode(GameType.SURVIVAL);
        player.moveTo(anchor.getX() + .5, anchor.getY(), anchor.getZ() + 2.5, 0, 0);
        player.getInventory().clearContent(); player.containerMenu.setCarried(ItemStack.EMPTY);
        player.getInventory().setItem(0, new ItemStack(Items.DIAMOND, 7));
        player.experienceLevel = 23; player.totalExperience = 587; player.experienceProgress = .625F;
        ListTag inventory = player.getInventory().save(new ListTag());
        CrewHouseManager.openAt(player, anchor);
        CrewHouseManager.handleAction(player, "found", "Rush workshop", 0, null, null, 0);
        CrewProfile home = CrewHouseData.get(player.getServer()).profile(player.getUUID());
        check(home != null, "Could not found the real house fixture");
        BlockPos table = anchor.east(2);
        helper.getLevel().setBlockAndUpdate(table, Blocks.CRAFTING_TABLE.defaultBlockState());
        CrewHouseManager.openAt(player, table);
        action(player, home, "facility", "workshop", null);
        action(player, home, "preparation", "FORCE", null);
        action(player, home, "focus", "", home.members().get(0).id());
        ResourceLocation gang = ResourceLocation.tryParse("jetsetcraft:dead_beat");
        ResourceLocation zombie = ResourceLocation.tryParse("minecraft:zombie");
        // Album fixture establishes its independent, already-earned draft entitlement.
        for (int card = 0; card < 20; card++) RushAlbum.collect(player, zombie.toString(), gang.toString());
        try {
            check(RivalRushManager.start(player, gang, zombie, false), "House-to-Rush production entry failed");
            RushRun run = RivalRushManager.run(player);
            check(run.rank(RushRun.Upgrade.FORCE) == 1 && run.hasOffer(),
                    "House preparation displaced the independent album draft or failed to apply at entry");
            check(!run.grantStartingPerk(RushRun.Upgrade.CADENCE), "A second house rank was accepted");
            RivalRushManager.handleAction(player, "choose", run.offers().get(0).name(), 0, run.id(), run.offerVersion());
            check(!run.hasOffer(), "The earned album draft could not be chosen through the production action");
            int chunkX = player.blockPosition().getX() >> 4, chunkZ = player.blockPosition().getZ() >> 4;
            for (int x = -2; x <= 2; x++) for (int z = -2; z <= 2; z++)
                player.serverLevel().getChunk(chunkX + x, chunkZ + z);
            // Advance only this run's active clock, without changing the shared world/mission clock.
            // The director and tag handler below still create and defeat actual owned mobs.
            for (int tick = 0; tick < 1200; tick++) run.tickActive();
            for (int spawn = 0; spawn < 4; spawn++) {
                for (int tick = 0; tick < 36; tick++) run.tickActive();
                RivalRushManager.tick(player.getServer());
            }
        } catch (RuntimeException error) { cleanup(player); throw error; }
        helper.runAfterDelay(2, () -> {
            try {
                RushRun run = RivalRushManager.run(player);
                check(run != null, "Fixture session ended before its actual rival combat");
                for (Mob mob : player.serverLevel().getEntitiesOfClass(Mob.class, player.getBoundingBox().inflate(80),
                        target -> RivalRushManager.belongsTo(target, player))) {
                    for (int hit = 0; hit < 6 && !mob.isRemoved(); hit++)
                        RivalRushManager.tagEnemy(player, mob, 40);
                }
                check(run.defeats() >= 3, "Fewer than three genuine director-created rivals were defeated");
                int before = home.supplies();
                UUID receipt = run.id();
                check(RivalRushManager.leave(player, "Crew integration return"), "Crew progression blocked safe Rush return");
                check(!RivalRushManager.active(player) && !RushReturnState.hasJournal(player)
                                && player.serverLevel() == helper.getLevel(), "Crew integration retained active Rush state");
                check(home.supplies() > before && home.xp() > 0 && home.members().get(0).xp() > 0
                                && home.reputation(gang.toString()) > 0,
                        "Actual return failed to award crew supplies, XP, focused member training and reputation");
                int paid = home.supplies(); long xp = home.xp();
                CrewHouseManager.bankRush(player, receipt, gang, run.elapsedTicks(), 0, run.defeats(), run.score());
                check(home.supplies() == paid && home.xp() == xp, "The settled run UUID paid twice");
                check(inventory.equals(player.getInventory().save(new ListTag())) && player.experienceLevel == 23
                                && player.totalExperience == 587 && player.experienceProgress == .625F,
                        "House/Rush integration changed normal inventory or vanilla XP");
                System.out.println("JETSETCRAFT_GAMETEST_PASS crew_rush"); helper.succeed();
            } finally { cleanup(player); }
        });
    }

    private static void action(ServerPlayer player, CrewProfile home, String action, String value, UUID target) {
        CrewHouseManager.handleAction(player, action, value, 0, home.id(), target, home.revision());
    }
    private static BlockPos freeAnchor(GameTestHelper helper) {
        for (int attempt = 0; attempt < 100; attempt++) {
            BlockPos candidate = new BlockPos(18000 + attempt * 100, 100, 12000);
            boolean clear = CrewHouseData.get(helper.getLevel().getServer()).profiles().stream()
                    .noneMatch(profile -> profile.contains(candidate)
                            || profile.contains(candidate.offset(-8, 0, -8)) || profile.contains(candidate.offset(8, 0, 8))
                            || profile.contains(candidate.offset(-8, 0, 8)) || profile.contains(candidate.offset(8, 0, -8)));
            if (clear) return candidate;
        }
        throw new GameTestAssertException("No unused crew integration fixture plot is available");
    }
    private static void cleanup(ServerPlayer player) {
        if (RivalRushManager.active(player) || RushReturnState.hasJournal(player))
            check(RivalRushManager.leave(player, "Crew integration cleanup"), "Could not restore test player");
    }
    private static void check(boolean condition, String message) { if (!condition) throw new GameTestAssertException(message); }
    private CrewRushGameTests() {}
}
