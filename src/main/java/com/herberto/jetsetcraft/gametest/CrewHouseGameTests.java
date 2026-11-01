package com.herberto.jetsetcraft.gametest;

import com.herberto.jetsetcraft.JetSetCraft;
import com.herberto.jetsetcraft.crew.CrewHouseData;
import com.herberto.jetsetcraft.crew.CrewHouseManager;
import com.herberto.jetsetcraft.crew.CrewProfile;
import com.herberto.jetsetcraft.registry.ModBlocks;
import com.herberto.jetsetcraft.rush.RushRun;
import com.mojang.authlib.GameProfile;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestAssertException;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.block.Blocks;
import net.minecraftforge.common.util.FakePlayerFactory;
import net.minecraftforge.gametest.GameTestHolder;
import net.minecraftforge.gametest.PrefixGameTestTemplate;

import java.util.UUID;

/** Real placed-house/action acceptance plus deterministic saved-time boundaries for long missions. */
@GameTestHolder(JetSetCraft.MOD_ID)
@PrefixGameTestTemplate(false)
public final class CrewHouseGameTests {
    private static final ResourceLocation DEAD_BEAT = ResourceLocation.tryParse("jetsetcraft:dead_beat");

    @GameTest(template = "hoverboard_empty", timeoutTicks = 100)
    public static void manualHouseFacilitiesAndExpansionKeepWorldOwnership(GameTestHelper helper) {
        BlockPos anchor = freeAnchor(helper, 12000);
        ServerPlayer owner = player(helper, "house", anchor);
        ServerLevel level = helper.getLevel();
        int forced = level.getForcedChunks().size();
        CrewProfile home = found(owner, anchor);
        check(home.members().size() == 2 && home.capacity() == 3 && home.radius() == 8,
                "Founding did not create exactly two apprentices and the small manual plot");
        BlockPos table = anchor.offset(2, 0, 0);
        level.setBlockAndUpdate(table.below(), Blocks.STONE.defaultBlockState());
        level.setBlockAndUpdate(table, Blocks.CRAFTING_TABLE.defaultBlockState());
        CrewHouseManager.openAt(owner, table);
        action(owner, home, "facility", "workshop", null);
        check(home.supplies() == 16 && facility(owner, "workshop").getBoolean("Operational"),
                "An actual placed workshop was not registered for its exact cost");
        action(owner, home, "preparation", "FORCE", null);
        check(CrewHouseManager.rushPreparation(owner).orElse(null) == RushRun.Upgrade.FORCE,
                "The operational physical workshop did not offer its optional mode preparation");
        level.setBlockAndUpdate(table, Blocks.AIR.defaultBlockState());
        check(CrewHouseManager.rushPreparation(owner).isEmpty(), "A missing physical facility still granted preparation");
        level.setBlockAndUpdate(table, Blocks.CRAFTING_TABLE.defaultBlockState());
        int beforeFailed = home.supplies(); long beforeRevision = home.revision();
        action(owner, home, "upgrade", "workshop", null);
        check(home.supplies() == beforeFailed && home.revision() == beforeRevision,
                "A facility upgrade charged supplies without its placed support block");
        level.setBlockAndUpdate(table.east().below(), Blocks.STONE.defaultBlockState());
        level.setBlockAndUpdate(table.east(), Blocks.ANVIL.defaultBlockState());
        action(owner, home, "upgrade", "workshop", null);
        check(facility(owner, "workshop").getInt("Level") == 2 && home.supplies() == 0,
                "The real anvil-supported tier upgrade was not applied exactly once");
        // Seed supplies through the same settled-result API used by the run integration; its actual run wiring
        // is independently exercised by crew_rush. No fixture mutates the production wallet directly.
        reward(owner);
        BlockPos builtWall = anchor.offset(11, 0, 0);
        level.setBlockAndUpdate(builtWall, Blocks.BRICKS.defaultBlockState());
        action(owner, home, "expand", "", null);
        check(home.tier() == 1 && home.radius() == 12 && home.capacity() == 5
                        && level.getBlockState(builtWall).is(Blocks.BRICKS),
                "Plot expansion failed or replaced the player's manually built blocks");
        BlockPos neighborAnchor = anchor.offset(24, 0, 0);
        ServerPlayer neighbor = player(helper, "neighbor", neighborAnchor);
        found(neighbor, neighborAnchor);
        for (int i = 0; i < 3; i++) reward(owner);
        int beforeOverlap = home.supplies();
        check(CrewHouseManager.snapshot(owner).getString("ExpandReason").contains("overlaps"),
                "The next plot tier did not detect the neighboring owner's actual registered footprint");
        action(owner, home, "expand", "", null);
        check(home.tier() == 1 && home.supplies() == beforeOverlap,
                "An overlapping expansion changed the plot or charged supplies");
        check(level.getBlockState(anchor).is(ModBlocks.BOOMBOX.get())
                        && level.getBlockState(table).is(Blocks.CRAFTING_TABLE)
                        && level.getForcedChunks().size() == forced,
                "House management replaced source blocks or acquired forced chunks");
        pass(helper, "crew_house");
    }

    @GameTest(template = "hoverboard_empty", timeoutTicks = 100)
    public static void missionsFreezeRewardsAndRejectStaleOrRepeatedClaims(GameTestHelper helper) {
        BlockPos anchor = freeAnchor(helper, 14000);
        ServerPlayer owner = player(helper, "missions", anchor);
        CrewProfile home = found(owner, anchor);
        UUID member = home.members().get(0).id();
        long stale = home.revision();
        action(owner, home, "assign", "courier_run", member);
        check(home.jobs().size() == 1, "The production action did not dispatch a member");
        CrewProfile.Job job = home.jobs().get(0);
        check(job.readyAt() - job.startedAt() == 3600L && job.supplies() == 15,
                "The three-minute job or frozen matching-role reward changed");
        CrewHouseManager.handleAction(owner, "assign", "paint_run", 0, home.id(), home.members().get(1).id(), stale);
        check(home.jobs().size() == 1, "A stale ledger revision dispatched another member");
        int before = home.supplies();
        action(owner, home, "claim", "", job.id());
        check(home.jobs().size() == 1 && home.supplies() == before, "A mission was claimed before its saved ready time");
        ServerPlayer outsider = player(helper, "outsider", anchor.offset(2, 0, 2));
        CrewHouseManager.handleAction(outsider, "claim", "", 0, home.id(), job.id(), home.revision());
        check(home.jobs().size() == 1 && home.supplies() == before, "Another player claimed the owner's job");

        CompoundTag serialized = CrewHouseData.get(owner.getServer()).save(new CompoundTag());
        CrewHouseData roundTrip = CrewHouseData.load(serialized);
        CrewProfile saved = roundTrip.profile(owner.getUUID());
        check(saved != null && saved.jobs().equals(home.jobs()) && saved.members().get(0).id().equals(member),
                "Saving and loading lost a mission identity, member identity, deadline or frozen reward");
        // Exercise the real deterministic claim operation at its exact persisted time boundary. Advancing the
        // shared server clock would corrupt concurrently running GameTests; no fake scheduler is substituted.
        check(!saved.claimMission(job.id(), job.readyAt() - 1) && saved.claimMission(job.id(), job.readyAt())
                        && !saved.claimMission(job.id(), job.readyAt()),
                "The mission boundary or once-only settlement contract failed");
        check(saved.supplies() == before + 15 && saved.xp() == 40 && saved.members().get(0).xp() == 35
                        && saved.reputation("jetsetcraft:dead_beat") == 6 && saved.jobs().isEmpty(),
                "Claim did not atomically award exactly the frozen supplies, crew/member XP and reputation");
        action(owner, home, "cancel", "", job.id());
        action(owner, home, "cancel", "", job.id());
        check(home.jobs().isEmpty() && home.supplies() == before && home.members().get(0).xp() == 0,
                "Cancellation or cancellation replay granted partial rewards");
        checkVanilla(owner);
        pass(helper, "crew_missions");
    }

    @GameTest(template = "hoverboard_empty", timeoutTicks = 100)
    public static void persistentCrewSurvivesDormancyRelocationAndRushReceipts(GameTestHelper helper) {
        BlockPos anchor = freeAnchor(helper, 16000);
        ServerPlayer owner = player(helper, "persistence", anchor);
        CrewProfile home = found(owner, anchor);
        UUID houseId = home.id(), away = home.members().get(0).id(), idle = home.members().get(1).id();
        action(owner, home, "assign", "paint_run", away);
        UUID receipt = UUID.randomUUID();
        CrewHouseManager.bankRush(owner, receipt, DEAD_BEAT, 1200, 3, 3, 100);
        int supplies = home.supplies(); long crewXp = home.xp();
        CrewHouseManager.bankRush(owner, receipt, DEAD_BEAT, 1200, 3, 3, 100);
        check(home.supplies() == supplies && home.xp() == crewXp && home.members().get(0).xp() == 0,
                "Rush settlement replayed or trained a member who was away on a mission");
        action(owner, home, "focus", "", idle);
        reward(owner);
        check(home.members().get(1).xp() > 0 && home.members().get(0).xp() == 0,
                "The chosen idle member did not receive isolated Rush training");
        BlockPos table = anchor.offset(2, 0, 0);
        helper.getLevel().setBlockAndUpdate(table, Blocks.CRAFTING_TABLE.defaultBlockState());
        CrewHouseManager.openAt(owner, table); action(owner, home, "facility", "workshop", null);
        CompoundTag savedHome = home.save();
        CrewHouseData store = CrewHouseData.get(owner.getServer());
        CompoundTag savedWorld = store.save(new CompoundTag());
        CrewHouseData loaded = CrewHouseData.load(savedWorld);
        check(savedWorld.equals(loaded.save(new CompoundTag())) && savedHome.equals(loaded.profile(owner.getUUID()).save()),
                "World SavedData did not round-trip exact roster, mission, progression, facility and reward-receipt state");
        CrewProfile loadedHome = loaded.profile(owner.getUUID());
        check(loadedHome.jobs().size() == 1 && loadedHome.members().size() == 2, "Reload discarded active jobs or roster identities");
        CompoundTag unknown = savedWorld.copy(); unknown.putInt("Schema", 999);
        check(CrewHouseData.load(unknown).readOnly() && CrewHouseData.load(unknown).save(new CompoundTag()).equals(unknown),
                "A future save schema was overwritten instead of retained read-only");

        helper.getLevel().setBlockAndUpdate(anchor, Blocks.AIR.defaultBlockState());
        check(!CrewHouseManager.snapshot(owner).getBoolean("CanManage") && home.id().equals(houseId)
                        && home.jobs().size() == 1, "Breaking the home anchor erased the crew or continued live management");
        int dormantBalance = home.supplies(); action(owner, home, "expand", "", null);
        check(home.supplies() == dormantBalance, "A dormant home charged an expansion");
        BlockPos relocated = anchor.offset(70, 0, 0);
        helper.getLevel().setBlockAndUpdate(relocated.below(), Blocks.STONE.defaultBlockState());
        helper.getLevel().setBlockAndUpdate(relocated, ModBlocks.BOOMBOX.get().defaultBlockState());
        owner.moveTo(relocated.getX() + .5, relocated.getY() + 1, relocated.getZ() + .5, 0, 0);
        CrewHouseManager.openAt(owner, relocated); action(owner, home, "relocate", "", null);
        CompoundTag relocatedState = CrewHouseManager.snapshot(owner);
        check(home.id().equals(houseId) && relocatedState.getLong("HomeAnchor") == relocated.asLong()
                        && home.jobs().size() == 1 && home.members().get(0).id().equals(away)
                        && !facility(owner, "workshop").getBoolean("Registered")
                        && facility(owner, "workshop").getInt("Level") == 1,
                "Relocation duplicated/lost crew identities or reused old physical facility bindings");
        checkVanilla(owner);
        pass(helper, "crew_persistence");
    }

    private static BlockPos freeAnchor(GameTestHelper helper, int initialX) {
        int y = helper.absolutePos(new BlockPos(0, 1, 0)).getY();
        CrewHouseData store = CrewHouseData.get(helper.getLevel().getServer());
        for (int attempt = 0; attempt < 128; attempt++) {
            BlockPos anchor = new BlockPos(initialX + attempt * 128, y, 12000);
            boolean close = store.profiles().stream().anyMatch(profile -> {
                BlockPos other = BlockPos.of(profile.save().getLong("Anchor"));
                return Math.abs((long) other.getX() - anchor.getX()) < 100 && Math.abs((long) other.getZ() - anchor.getZ()) < 100;
            });
            if (!close) return anchor;
        }
        throw new GameTestAssertException("Could not reserve isolated fixture space for a manual crew home");
    }
    private static ServerPlayer player(GameTestHelper helper, String label, BlockPos anchor) {
        ServerPlayer player = FakePlayerFactory.get(helper.getLevel(), new GameProfile(UUID.randomUUID(), "JSC_crew_" + label));
        player.setGameMode(GameType.SURVIVAL); player.setHealth(player.getMaxHealth());
        player.moveTo(anchor.getX() + .5, anchor.getY() + 1, anchor.getZ() + .5, 0, 0);
        player.getInventory().clearContent(); player.getInventory().setItem(0, new ItemStack(Items.DIAMOND, 7));
        player.experienceLevel = 23; player.totalExperience = 587; player.experienceProgress = .625F;
        return player;
    }
    private static CrewProfile found(ServerPlayer owner, BlockPos anchor) {
        owner.serverLevel().setBlockAndUpdate(anchor.below(), Blocks.STONE.defaultBlockState());
        owner.serverLevel().setBlockAndUpdate(anchor, ModBlocks.BOOMBOX.get().defaultBlockState());
        CrewHouseManager.openAt(owner, anchor);
        CrewHouseManager.handleAction(owner, "found", "GameTest crew", 0, null, null, 0);
        CrewProfile profile = CrewHouseData.get(owner.getServer()).profile(owner.getUUID());
        check(profile != null, "Production crew founding failed at a real placed Boombox");
        return profile;
    }
    private static void action(ServerPlayer owner, CrewProfile profile, String action, String value, UUID target) {
        CrewHouseManager.handleAction(owner, action, value, 0, profile.id(), target, profile.revision());
    }
    private static CompoundTag facility(ServerPlayer player, String id) {
        ListTag rooms = CrewHouseManager.snapshot(player).getList("Facilities", Tag.TAG_COMPOUND);
        for (int i = 0; i < rooms.size(); i++) if (rooms.getCompound(i).getString("Id").equals(id)) return rooms.getCompound(i);
        throw new GameTestAssertException("Expected facility missing from the authoritative snapshot");
    }
    private static void reward(ServerPlayer owner) { CrewHouseManager.bankRush(owner, UUID.randomUUID(), DEAD_BEAT, 36000, 20, 100, 5000); }
    private static void checkVanilla(ServerPlayer player) {
        check(player.experienceLevel == 23 && player.totalExperience == 587 && player.experienceProgress == .625F
                        && player.getInventory().getItem(0).getItem() == Items.DIAMOND && player.getInventory().getItem(0).getCount() == 7,
                "Crew progression changed Minecraft XP or ordinary inventory");
    }
    private static void check(boolean condition, String message) { if (!condition) throw new GameTestAssertException(message); }
    private static void pass(GameTestHelper helper, String marker) { System.out.println("JETSETCRAFT_GAMETEST_PASS " + marker); helper.succeed(); }
    private CrewHouseGameTests() {}
}
