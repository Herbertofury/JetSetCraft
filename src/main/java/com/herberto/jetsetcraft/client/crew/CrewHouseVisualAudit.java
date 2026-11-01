package com.herberto.jetsetcraft.client.crew;

import com.herberto.jetsetcraft.JetSetCraft;
import com.herberto.jetsetcraft.client.rush.RivalRushClient;
import com.herberto.jetsetcraft.registry.ModBlocks;
import com.herberto.jetsetcraft.registry.ModItems;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.Button;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.storage.LevelResource;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;

import java.nio.file.Path;
import java.util.UUID;
import java.util.function.Consumer;
import java.util.function.Predicate;

/** Continues the opt-in real-client audit after the Rush inventory/XP restoration checks have passed. */
public final class CrewHouseVisualAudit {
    private static int phase, ticks;
    private static BlockPos anchor, workshop;
    private static UUID assignedMember;
    private static long foundedRevision, registeredRevision;
    private static volatile boolean fixtureApplied;
    private static volatile String fixtureFailure;

    public static boolean advance(Minecraft minecraft, Consumer<String> capture, Predicate<String> captured) {
        if (!Boolean.getBoolean("jetsetcraft.rushVisualAudit")) throw new IllegalStateException("Crew visual audit is not enabled");
        if (fixtureFailure != null) throw new IllegalStateException(fixtureFailure);
        if (minecraft.player == null || minecraft.level == null || minecraft.gameMode == null || RivalRushClient.active())
            throw new IllegalStateException("Crew audit requires the restored Minecraft player");
        CompoundTag state = CrewHouseClient.state();
        switch (phase) {
            case 0 -> { queueFixture(minecraft); next(1); }
            case 1 -> {
                if (!fixtureApplied || !minecraft.level.getBlockState(anchor).is(ModBlocks.BOOMBOX.get())
                        || !minecraft.level.getBlockState(workshop).is(Blocks.CRAFTING_TABLE) || charterSlot(minecraft) < 0) return false;
                click(minecraft, "Done");
                useCharter(minecraft, anchor);
                next(2);
            }
            case 2 -> {
                if (!(minecraft.screen instanceof CrewHouseScreen) || !state.getBoolean("HasCandidate")) return false;
                if (state.getBoolean("Exists")) throw new IllegalStateException("Disposable audit player already owns a house");
                if (!state.getBoolean("CanFound")) throw new IllegalStateException("Crew founding rejected: " + state.getString("FoundReason"));
                CrewHouseClient.request("found", "Audit Crew", 0, null);
                next(3);
            }
            case 3 -> {
                if (!state.getBoolean("Exists")) return false;
                require(state.hasUUID("HomeId") && state.contains("Revision", Tag.TAG_LONG) && state.getLong("Revision") >= 0
                        && state.getString("Name").equals("Audit Crew"), "Missing founded crew identity");
                foundedRevision = state.getLong("Revision");
                require(state.getLong("HomeAnchor") == anchor.asLong() && state.getBoolean("AnchorValid")
                        && state.getBoolean("CanManage") && CrewHouseClient.list("Members").size() == 2, "Invalid real home or apprentice roster");
                if (!(minecraft.screen instanceof CrewHouseScreen screen) || !screen.isView("HOME")) return false;
                if (++ticks < 20) return false;
                capture.accept("05-crew-home"); next(4);
            }
            case 4 -> {
                if (!captured.test("05-crew-home")) return false;
                click(minecraft, "Crew"); next(5);
            }
            case 5 -> {
                if (!(minecraft.screen instanceof CrewHouseScreen screen) || !screen.isView("CREW")) return false;
                if (++ticks < 20) return false;
                capture.accept("06-crew-roster"); next(6);
            }
            case 6 -> {
                if (!captured.test("06-crew-roster")) return false;
                click(minecraft, "Done");
                useCharter(minecraft, workshop); next(7);
            }
            case 7 -> {
                CompoundTag facility = workshop();
                if (!(minecraft.screen instanceof CrewHouseScreen) || !facility.getBoolean("CanRegister")) return false;
                CrewHouseClient.request("facility", "workshop", 0, null); next(8);
            }
            case 8 -> {
                CompoundTag facility = workshop();
                if (!facility.getBoolean("Registered")) return false;
                require(facility.getBoolean("Operational") && facility.getInt("Level") == 1, "Physical workshop did not become operational");
                require(state.getLong("Revision") > foundedRevision, "Workshop registration did not advance the crew revision");
                registeredRevision = state.getLong("Revision");
                click(minecraft, "Facilities"); next(9);
            }
            case 9 -> {
                if (!(minecraft.screen instanceof CrewHouseScreen screen) || !screen.isView("FACILITIES")) return false;
                if (++ticks < 20) return false;
                capture.accept("07-crew-facilities"); next(10);
            }
            case 10 -> {
                if (!captured.test("07-crew-facilities")) return false;
                CompoundTag member = CrewHouseClient.list("Members").getCompound(0);
                require(member.hasUUID("Id") && member.getBoolean("Idle") && member.getBoolean("CanAssign"), "No real idle apprentice to assign");
                assignedMember = member.getUUID("Id");
                CrewHouseClient.request("assign", "courier_run", 0, assignedMember); next(11);
            }
            case 11 -> {
                if (CrewHouseClient.list("Jobs").isEmpty()) return false;
                CompoundTag job = CrewHouseClient.list("Jobs").getCompound(0);
                require(CrewHouseClient.list("Jobs").size() == 1 && state.getInt("ActiveJobs") == 1
                        && state.getLong("Revision") > registeredRevision
                        && job.hasUUID("Id") && job.hasUUID("MemberId") && job.getUUID("MemberId").equals(assignedMember)
                        && job.getInt("Supplies") > 0 && job.getLong("RemainingTicks") > 0 && !job.getBoolean("Ready"),
                        "Production crew dispatch did not create one real timed assignment");
                click(minecraft, "Jobs"); next(12);
            }
            case 12 -> {
                if (!(minecraft.screen instanceof CrewHouseScreen screen) || !screen.isView("JOBS")) return false;
                if (++ticks < 20) return false;
                capture.accept("08-crew-jobs"); next(13);
            }
            case 13 -> {
                if (!captured.test("08-crew-jobs")) return false;
                JetSetCraft.LOGGER.info("JETSETCRAFT_CREW_VISUAL_AUDIT_PASS real charter use; founded home; two profiles; physical workshop; real timed job");
                next(14); return true;
            }
            case 14 -> { return true; }
            default -> throw new IllegalStateException("Unknown crew audit phase " + phase);
        }
        return false;
    }

    private static void queueFixture(Minecraft minecraft) {
        var server = minecraft.getSingleplayerServer();
        Path expected = minecraft.gameDirectory.toPath().resolve("saves/RivalRushAudit").toAbsolutePath().normalize();
        require(server != null && server.getWorldPath(LevelResource.ROOT).toAbsolutePath().normalize().equals(expected),
                "Crew fixture requires the exact disposable RivalRushAudit integrated save");
        anchor = minecraft.player.blockPosition().offset(2, 0, 0);
        workshop = anchor.offset(2, 0, 0);
        UUID playerId = minecraft.player.getUUID();
        server.execute(() -> {
            try {
                require(Boolean.getBoolean("jetsetcraft.rushVisualAudit")
                        && server.getWorldPath(LevelResource.ROOT).toAbsolutePath().normalize().equals(expected), "Crew fixture lost its world guard");
                var player = server.getPlayerList().getPlayer(playerId);
                require(player != null, "Crew fixture player disconnected");
                var source = server.createCommandSourceStack().withPermission(4).withEntity(player).withSuppressedOutput();
                for (String command : new String[]{setBlock(anchor, "jetsetcraft:boombox"), setBlock(workshop, "minecraft:crafting_table"),
                        "give @s jetsetcraft:crew_charter 1"}) {
                    require(server.getCommands().performPrefixedCommand(source, command) > 0, "Crew fixture vanilla command failed: " + command);
                }
                fixtureApplied = true;
            } catch (RuntimeException error) {
                fixtureFailure = "Crew fixture failed: " + error.getMessage();
                JetSetCraft.LOGGER.error("Crew real-client fixture failed", error);
            }
        });
    }

    private static String setBlock(BlockPos pos, String block) { return "setblock " + pos.getX() + " " + pos.getY() + " " + pos.getZ() + " " + block; }
    private static int charterSlot(Minecraft minecraft) {
        for (int slot = 0; slot < 9; slot++) if (minecraft.player.getInventory().getItem(slot).is(ModItems.CREW_CHARTER.get())) return slot;
        return -1;
    }
    private static void useCharter(Minecraft minecraft, BlockPos target) {
        int slot = charterSlot(minecraft);
        require(slot >= 0 && minecraft.gameMode != null, "Actual Charter is missing from the client hotbar");
        // Select the real given item; useItemOn sends ordinary carried-slot/use packets and invokes the Charter hook.
        minecraft.player.getInventory().selected = slot;
        minecraft.gameMode.useItemOn(minecraft.player, InteractionHand.MAIN_HAND,
                new BlockHitResult(Vec3.atCenterOf(target).add(0, 0.5, 0), Direction.UP, target, false));
        JetSetCraft.LOGGER.info("JETSETCRAFT_CREW_VISUAL_AUDIT_CHARTER_USE {}", target.toShortString());
    }
    private static void click(Minecraft minecraft, String label) {
        require(minecraft.screen != null, "Missing screen for actual button " + label);
        for (var child : minecraft.screen.children()) {
            if (child instanceof Button button && button.active && button.visible && button.getMessage().getString().equals(label)) {
                button.onPress(); return;
            }
        }
        throw new IllegalStateException("Missing actual crew UI button: " + label);
    }
    private static CompoundTag workshop() {
        for (var tag : CrewHouseClient.list("Facilities")) if (tag instanceof CompoundTag row && row.getString("Id").equals("workshop")) return row;
        return new CompoundTag();
    }
    private static void require(boolean condition, String reason) { if (!condition) throw new IllegalStateException(reason); }
    private static void next(int value) { phase = value; ticks = 0; JetSetCraft.LOGGER.info("JETSETCRAFT_CREW_VISUAL_AUDIT_PHASE {}", value); }
    private CrewHouseVisualAudit() {}
}
