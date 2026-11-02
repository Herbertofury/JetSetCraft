package com.herberto.jetsetcraft.gametest;

import com.herberto.jetsetcraft.JetSetCraft;
import com.herberto.jetsetcraft.data.JetSetDataProvider;
import com.herberto.jetsetcraft.network.C2SRushActionPacket;
import com.herberto.jetsetcraft.rush.RivalRushManager;
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
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.level.TicketType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.common.util.FakePlayerFactory;
import net.minecraftforge.gametest.GameTestHolder;
import net.minecraftforge.gametest.PrefixGameTestTemplate;

import java.util.UUID;

/** Real room settlement and production packets. Only positioning and the room clock are compressed. */
@GameTestHolder(JetSetCraft.MOD_ID)
@PrefixGameTestTemplate(false)
public final class RushPitStopRuntimeGameTests {
    private static final TicketType<UUID> TICKET = TicketType.create("jetsetcraft_pit_stop_test", UUID::compareTo, 900);

    @GameTest(template = "hoverboard_empty", timeoutTicks = 700)
    public static void actualContractClearEarnsSharedServicesAndRecordsOnlyAcceptedResults(GameTestHelper helper) {
        ServerPlayer player = FakePlayerFactory.get(helper.getLevel(), new GameProfile(UUID.randomUUID(), "JSC_pit_stop"));
        player.setGameMode(GameType.SURVIVAL); player.getInventory().clearContent(); player.containerMenu.setCarried(ItemStack.EMPTY);
        BlockPos source = helper.absolutePos(new BlockPos(1, 2, 1));
        player.moveTo(source.getX() + .5, source.getY(), source.getZ() + .5, 0, 0);
        ItemStack ordinary = new ItemStack(Items.EMERALD, 5); ordinary.getOrCreateTag().putString("PitStopReturn", "original");
        player.getInventory().setItem(0, ordinary); player.experienceLevel = 8; player.totalExperience = 57; player.experienceProgress = .25f;
        ListTag inventory = player.getInventory().save(new ListTag());
        CompoundTag ride = player.getCapability(JetSetDataProvider.CAPABILITY).resolve().orElseThrow().save();
        Vec3 returnPosition = player.position();
        check(RivalRushManager.startCircuit(player, id("jetsetcraft:dead_beat"), id("minecraft:zombie"), false,
                id("jetsetcraft:signal_yard")), "Could not start the actual PAINT arena session");
        ServerLevel district = player.serverLevel(); BlockPos destination = RivalRushManager.arenaDestination(player);
        ChunkPos chunk = new ChunkPos(destination);
        district.getChunkSource().addRegionTicket(TICKET, chunk, 4, player.getUUID());
        try {
            for (int x = -2; x <= 2; x++) for (int z = -2; z <= 2; z++) district.getChunk(chunk.x + x, chunk.z + z);
            player.moveTo(destination.getX() + .5, RushWorld.FLOOR_Y + 1, destination.getZ() + .5, 0, 0);
            player.setDeltaMovement(Vec3.ZERO); district.getChunkSource().move(player);
            for (int step = 0; step < 128; step++) RivalRushManager.tick(player.getServer());
        } catch (RuntimeException error) { cleanup(player, district, chunk); throw error; }
        helper.runAfterDelay(2, () -> {
            try {
                RushRun run = RivalRushManager.run(player);
                check(RivalRushManager.journeySnapshot(player).getString("Phase").equals("arena"), "The actual builder did not admit the arena");
                check(RivalRushManager.contractSnapshot(player).getString("Type").equals("paint"), "Supplied Signal Yard lost its graffiti contract");
                action(player, "pit_stop", "TUNE|FORCE", run.id(), run.offerVersion());
                check(run.pitStopCredits() == 0 && run.pitStopServicesUsed() == 0 && run.rank(RushRun.Upgrade.FORCE) == 0,
                        "A combat-phase service manufactured a rank or an unearned stop");

                for (int patch = 0; patch < 3; patch++) {
                    chooseDrafts(player);
                    BlockPos support = destination.offset(-8 + patch * 8, 2, 8);
                    district.setBlock(support, Blocks.STONE.defaultBlockState(), 2);
                    player.moveTo(support.getX() + .5, RushWorld.FLOOR_Y + 1, support.getZ() - 2, 0, 0);
                    player.setDeltaMovement(Vec3.ZERO);
                    check(RivalRushManager.tagWall(player, support, Direction.NORTH), "A real fresh in-room patch was rejected");
                }
                check(RivalRushManager.contractSnapshot(player).getBoolean("Completed") && run.contractClears() == 0
                                && run.pitStopCredits() == 0, "Achieving the optional goal paid services before its room was cleared");
                ListTag contractFeedback = RivalRushManager.feedbackSnapshot(player);
                check(count(contractFeedback, "contract") == 1 && contractFeedback.equals(RivalRushManager.feedbackSnapshot(player)),
                        "A genuine contract completion was missing or a snapshot read replayed its notice");
                contractFeedback.getCompound(contractFeedback.size() - 1).putString("Title", "forged client title");
                check(!RivalRushManager.feedbackSnapshot(player).getCompound(contractFeedback.size() - 1).getString("Title").equals("forged client title"),
                        "A returned notice mutated the authoritative feedback history");

                chooseDrafts(player);
                check(run.rerollsRemaining() == 2 && run.rerollsUsed() == 1, "The real earned draft did not spend exactly one reroll");
                player.moveTo(destination.getX() + .5, RushWorld.FLOOR_Y + 1, destination.getZ() + .5, 0, 0);
                // Clock-only fixture compression: rival defeats, XP choices and settlement use production paths.
                CompoundTag entered = RivalRushManager.journeySnapshot(player);
                advance(run, entered.getLong("TicksLeft") + 1);
                int quota = entered.getCompound("Current").getInt("Defeats"); long startingDefeats = run.defeats();
                for (int wave = 0; wave < quota + 30 && run.defeats() - startingDefeats < quota; wave++) {
                    chooseDrafts(player); advance(run, 40); RivalRushManager.tick(player.getServer());
                    for (Mob actor : district.getEntitiesOfClass(Mob.class, player.getBoundingBox().inflate(100), mob -> RivalRushManager.belongsTo(mob, player))) {
                        defeat(player, actor);
                        if (run.defeats() - startingDefeats >= quota) break;
                    }
                }
                chooseDrafts(player); RivalRushManager.tick(player.getServer()); chooseDrafts(player);
                CompoundTag journey = RivalRushManager.journeySnapshot(player), stop = RivalRushManager.pitStopSnapshot(player);
                check(journey.getInt("Completed") == 1 && journey.getString("Phase").equals("choosing")
                                && journey.getCompound("LastCompleted").getCompound("Contract").getBoolean("Completed")
                                && run.arenaClears() == 1 && run.contractClears() == 1 && run.pitStopCredits() == 2
                                && stop.getBoolean("Usable") && stop.getUUID("Receipt").equals(journey.getCompound("LastCompleted").getUUID("Receipt")),
                        "A real contracted clear did not open exactly two services against its actual room receipt");
                long score = run.score(), xp = run.xp(); int level = run.level(), before = run.rank(RushRun.Upgrade.FORCE), revision = run.offerVersion();
                check(before > 0 && before < RushRun.Upgrade.FORCE.maxRank(), "The actual room reward did not leave a tunable owned stat");
                action(player, "pit_stop", "TUNE|FORCE", UUID.randomUUID(), revision);
                action(player, "pit_stop", "TUNE|FORCE", run.id(), revision - 1);
                action(player, "pit_stop", "TUNE|NOT_AN_UPGRADE", run.id(), revision);
                check(run.pitStopCredits() == 2 && run.rank(RushRun.Upgrade.FORCE) == before && run.pitStopServicesUsed() == 0,
                        "A foreign, stale or malformed request spent an earned service");
                action(player, "pit_stop", "TUNE|FORCE", run.id(), revision);
                CompoundTag tuned = RivalRushManager.pitStopSnapshot(player).getCompound("LastResult");
                check(run.pitStopCredits() == 1 && run.pitStopServicesUsed() == 1 && run.rank(RushRun.Upgrade.FORCE) == before + 1
                                && tuned.getBoolean("Accepted") && tuned.getInt("RankBefore") == before && tuned.getInt("RankAfter") == before + 1
                                && !tuned.getString("Summary").isBlank(), "The accepted Tune result did not describe its actual single-rank change");
                ListTag accepted = RivalRushManager.feedbackSnapshot(player);
                action(player, "pit_stop", "TUNE|FORCE", run.id(), revision);
                action(player, "pit_stop", "TUNE|FORCE", run.id(), run.offerVersion());
                check(run.pitStopCredits() == 1 && run.pitStopServicesUsed() == 1 && accepted.equals(RivalRushManager.feedbackSnapshot(player)),
                        "A replay or second Tune consumed the bonus choice or emitted another success notice");
                action(player, "pit_stop", "REWIND", run.id(), run.offerVersion());
                CompoundTag rewound = RivalRushManager.pitStopSnapshot(player).getCompound("LastResult");
                check(run.rerollsRemaining() == 3 && run.banishesRemaining() == 2 && run.pitStopCredits() == 0 && run.pitStopServicesUsed() == 2
                                && rewound.getBoolean("Accepted") && rewound.getString("Service").equals("REWIND")
                                && rewound.getInt("RemainingCredits") == 0 && run.score() == score && run.xp() == xp && run.level() == level,
                        "Rewind failed to replenish its bounded control or minted unrelated XP/score");
                long clears = run.arenaClears(), contracts = run.contractClears();
                action(player, "pit_stop", "PATCH", run.id(), run.offerVersion());
                check(run.pitStopServicesUsed() == 2, "An exhausted stop accepted a third service");
                String next = journey.getList("Choices", Tag.TAG_COMPOUND).getCompound(0).getString("Id");
                action(player, "journey_choose", next, run.id(), journey.getInt("Version"));
                check(RivalRushManager.journeySnapshot(player).getString("Phase").equals("travel")
                                && !RivalRushManager.pitStopSnapshot(player).getBoolean("Usable"), "Departing did not close the actual stop");
                action(player, "pit_stop", "TUNE|FORCE", run.id(), run.offerVersion());
                check(run.pitStopServicesUsed() == 2 && run.arenaClears() == clears && run.contractClears() == contracts,
                        "Travel reopened a consumed room reward");
                check(RivalRushManager.leave(player, "Pit stop acceptance return") && player.serverLevel() == helper.getLevel()
                                && player.position().distanceToSqr(returnPosition) < .0001 && inventory.equals(player.getInventory().save(new ListTag()))
                                && ride.equals(player.getCapability(JetSetDataProvider.CAPABILITY).resolve().orElseThrow().save())
                                && player.experienceLevel == 8 && player.totalExperience == 57 && player.experienceProgress == .25f
                                && !RushReturnState.hasJournal(player), "Services changed ordinary belongings, XP, ride gear or the return checkpoint");
                CompoundTag report = RivalRushManager.lastDebrief(player);
                check(report.getLong("ContractsBanked") == 1 && report.getLong("ServicesUsed") == 2 && report.getLong("ArenaClears") == 1,
                        "The restored Last run report lost actual banked contracts or accepted services");
                System.out.println("JETSETCRAFT_GAMETEST_PASS rush_pit_stop_runtime"); helper.succeed();
            } finally { cleanup(player, district, chunk); }
        });
    }

    private static void defeat(ServerPlayer player, Mob actor) {
        if (actor.isRemoved()) return;
        actor.moveTo(player.getX() + 5, player.getY(), player.getZ(), 0, 0); actor.setDeltaMovement(Vec3.ZERO);
        for (int hit = 0; hit < 200 && RivalRushManager.actorVitality(player, actor) > 0; hit++) {
            chooseDrafts(player); check(RivalRushManager.artifactHit(player, actor, 40), "A valid owned rival could not be defeated");
        }
        check(actor.isRemoved(), "Actual bounded damage did not defeat its owned body"); chooseDrafts(player);
    }
    private static void chooseDrafts(ServerPlayer player) {
        RushRun run = RivalRushManager.run(player);
        for (int pick = 0; run != null && run.hasOffer() && pick < 100; pick++) {
            if (run.rerollsUsed() == 0 && run.canReroll()) action(player, "reroll", "", run.id(), run.offerVersion());
            int index = 0;
            for (int i = 0; i < run.offers().size(); i++) if (!run.offers().get(i).artifact() && run.offers().get(i) != RushRun.Upgrade.FORCE) { index = i; break; }
            C2SRushActionPacket.handleForPlayer(new C2SRushActionPacket("choose", "", index, run.id(), run.offerVersion()), player);
        }
        check(run != null && !run.hasOffer(), "Could not resolve the earned XP offers");
    }
    private static int count(ListTag notices, String kind) {
        int count = 0; for (Tag raw : notices) if (((CompoundTag) raw).getString("Kind").equals(kind)) count++; return count;
    }
    private static void action(ServerPlayer player, String action, String value, UUID run, int version) {
        C2SRushActionPacket.handleForPlayer(new C2SRushActionPacket(action, value, 0, run, version), player);
    }
    private static void advance(RushRun run, long ticks) { for (long tick = 0; tick < ticks; tick++) run.tickActive(); }
    private static ResourceLocation id(String value) { return ResourceLocation.tryParse(value); }
    private static void check(boolean condition, String message) { if (!condition) throw new GameTestAssertException(message); }
    private static void cleanup(ServerPlayer player, ServerLevel district, ChunkPos chunk) {
        try { if (RivalRushManager.active(player) || RushReturnState.hasJournal(player))
            check(RivalRushManager.leave(player, "Pit stop GameTest cleanup"), "Pit stop fixture could not restore the owner"); }
        finally { district.getChunkSource().removeRegionTicket(TICKET, chunk, 4, player.getUUID()); }
    }
    private RushPitStopRuntimeGameTests() {}
}
