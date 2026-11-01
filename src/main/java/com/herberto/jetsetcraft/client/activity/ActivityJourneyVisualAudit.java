package com.herberto.jetsetcraft.client.activity;

import com.herberto.jetsetcraft.JetSetCraft;
import com.herberto.jetsetcraft.blockentity.BoomboxBlockEntity;
import com.herberto.jetsetcraft.client.rush.RivalRushClient;
import com.herberto.jetsetcraft.network.JetSetNetwork;
import com.herberto.jetsetcraft.registry.ModBlocks;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.Button;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.storage.LevelResource;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;

import java.nio.file.Path;
import java.util.UUID;
import java.util.function.Consumer;
import java.util.function.Predicate;

/** Actual Boombox selection and a short arena-journey round trip in the guarded disposable audit world. */
public final class ActivityJourneyVisualAudit {
    private static int phase, ticks;
    private static BlockPos anchor;
    private static volatile boolean fixtureApplied;
    private static volatile String fixtureFailure;
    private static String originalDimension;
    private static String previousRushSummary = "";
    private static Vec3 originalPosition;
    private static ListTag originalInventory;
    private static int originalLevel, originalTotalXp;
    private static float originalProgress;
    private static UUID runId, chosenRun;
    private static int chosenVersion = Integer.MIN_VALUE;

    public static boolean advance(Minecraft minecraft, Consumer<String> capture, Predicate<String> captured) {
        require(Boolean.getBoolean("jetsetcraft.rushVisualAudit"), "Activity visual audit is not enabled");
        if (fixtureFailure != null) throw new IllegalStateException(fixtureFailure);
        require(minecraft.player != null && minecraft.level != null && minecraft.gameMode != null,
                "Activity audit lost its connected player");
        CompoundTag activity = ActivityClient.stateSnapshot();
        CompoundTag rush = RivalRushClient.stateSnapshot();
        // Resolve only genuine pending upgrade offers through the normal action packet.
        ListTag offers = rush.getList("Offers", Tag.TAG_COMPOUND);
        if (phase >= 7 && phase <= 10 && rush.getBoolean("Active") && !offers.isEmpty()) {
            UUID currentRun = rush.hasUUID("Run") ? rush.getUUID("Run") : null;
            int version = rush.getInt("Version");
            if (!java.util.Objects.equals(currentRun, chosenRun) || chosenVersion != version) {
                JetSetNetwork.sendRushAction("choose", offers.getCompound(0).getString("id"), 0, currentRun, version);
                chosenRun = currentRun; chosenVersion = version; ticks = 0;
            }
            return false;
        }
        switch (phase) {
            case 0 -> { queueFixture(minecraft); next(1); }
            case 1 -> {
                if (!fixtureApplied || hotbarSlot(minecraft, false) < 0) return false;
                click(minecraft, "Done");
                use(minecraft, hotbarSlot(minecraft, false)); next(2);
            }
            case 2 -> {
                if (!(minecraft.level.getBlockEntity(anchor) instanceof BoomboxBlockEntity box)
                        || box.targetStack().isEmpty()) return false;
                int empty = hotbarSlot(minecraft, true);
                require(empty >= 0, "No real empty-hand slot remains in the audit hotbar");
                use(minecraft, empty); next(3);
            }
            case 3 -> {
                if (!ActivityClient.isScreenOpen() || !activity.getBoolean("FromBoombox") || !activity.hasUUID("Context")) return false;
                require(!activity.getBoolean("Active") && activity.getBoolean("CanLaunch"),
                        "Real Boombox did not provide a launchable preview: " + activity.getString("Summary"));
                require(activity.getList("Activities", Tag.TAG_COMPOUND).size() >= 4, "Shared activity catalogue is incomplete");
                if (++ticks < 20) return false;
                capture.accept("09-activities"); next(4);
            }
            case 4 -> {
                if (!captured.test("09-activities")) return false;
                clickContaining(minecraft, "Rival Rush"); next(5);
            }
            case 5 -> {
                if (!activity.getString("SelectedMode").equals("rival_rush") || !activity.getBoolean("CanLaunch")) return false;
                require(activity.getString("Mode").equals("rival_rush"), "Explicit mode selection was randomized");
                // The native chooser deliberately budgets consecutive actions. Let its real preview settle
                // before accepting; an automation click must not race the server's four-tick request budget.
                if (++ticks < 10) return false;
                // Capture the source state after the real tuner item was consumed, immediately before launch.
                originalDimension = minecraft.level.dimension().location().toString();
                originalPosition = minecraft.player.position();
                originalInventory = minecraft.player.getInventory().save(new ListTag()).copy();
                originalLevel = minecraft.player.experienceLevel;
                originalTotalXp = minecraft.player.totalExperience;
                originalProgress = minecraft.player.experienceProgress;
                previousRushSummary = rush.getString("Summary");
                click(minecraft, activity.getString("LaunchLabel")); next(7);
            }
            case 7 -> {
                if (!rush.getBoolean("Active")) {
                    require(rush.getString("Summary").isBlank() || rush.getString("Summary").equals(previousRushSummary),
                            "Activity-to-Rush launch failed: " + rush.getString("Summary"));
                    return false;
                }
                CompoundTag journey = rush.getCompound("Journey");
                if (!RivalRushClient.active() || !RivalRushClient.isJourneyScreenOpen()
                        || !journey.getString("Phase").equals("choosing")) return false;
                require(rush.hasUUID("Run") && journey.getList("Choices", Tag.TAG_COMPOUND).size() == 3,
                        "First arena decision is not a real three-choice server offer");
                runId = rush.getUUID("Run");
                if (++ticks < 20) return false;
                capture.accept("10-arena-route"); next(8);
            }
            case 8 -> {
                if (!captured.test("10-arena-route")) return false;
                CompoundTag journey = rush.getCompound("Journey");
                CompoundTag choice = journey.getList("Choices", Tag.TAG_COMPOUND).getCompound(0);
                require(!choice.getString("Id").isBlank(), "First route has no authoritative profile identity");
                RivalRushClient.requestJourney(choice.getString("Id")); next(9);
            }
            case 9 -> {
                CompoundTag journey = rush.getCompound("Journey");
                String journeyPhase = journey.getString("Phase");
                if (!journeyPhase.equals("travel") && !journeyPhase.equals("arena")) return false;
                require(rush.hasUUID("Run") && rush.getUUID("Run").equals(runId)
                        && !journey.getString("CurrentName").isBlank(), "Selecting a route lost the live run or arena");
                if (++ticks < 20) return false;
                // No room completion is fabricated: this verifies choosing a real connected arena and the normal exit.
                JetSetNetwork.sendRushAction("leave", "", 0, runId, rush.getInt("Version")); next(10);
            }
            case 10 -> {
                if (rush.getBoolean("Active") || !minecraft.level.dimension().location().toString().equals(originalDimension)) return false;
                if (++ticks < 20) return false;
                require(originalInventory.equals(minecraft.player.getInventory().save(new ListTag())), "Circuit return changed ordinary inventory");
                require(minecraft.player.experienceLevel == originalLevel && minecraft.player.totalExperience == originalTotalXp
                        && Math.abs(minecraft.player.experienceProgress - originalProgress) <= 0.000001F,
                        "Circuit return changed ordinary Minecraft XP");
                require(minecraft.player.position().distanceToSqr(originalPosition) <= 1.0D, "Circuit return lost its source position");
                JetSetCraft.LOGGER.info("JETSETCRAFT_ACTIVITY_VISUAL_AUDIT_PASS real Boombox preview; explicit Rival Rush; genuine first route choice; entered connected arena travel; inventory/XP/dimension restored");
                next(11); return true;
            }
            case 11 -> { return true; }
            default -> throw new IllegalStateException("Unknown activity audit phase " + phase);
        }
        return false;
    }

    private static void queueFixture(Minecraft minecraft) {
        var server = minecraft.getSingleplayerServer();
        Path expected = minecraft.gameDirectory.toPath().resolve("saves/RivalRushAudit").toAbsolutePath().normalize();
        require(server != null && server.getWorldPath(LevelResource.ROOT).toAbsolutePath().normalize().equals(expected),
                "Activity fixture requires the exact disposable RivalRushAudit save");
        BlockPos playerPos = minecraft.player.blockPosition();
        for (BlockPos pos : BlockPos.betweenClosed(playerPos.offset(-4, -1, -4), playerPos.offset(4, 1, 4))) {
            if (minecraft.level.hasChunkAt(pos) && minecraft.level.getBlockState(pos).is(ModBlocks.BOOMBOX.get())) {
                anchor = pos.immutable(); break;
            }
        }
        require(anchor != null, "Placed house Boombox is missing from the actual audit world");
        UUID playerId = minecraft.player.getUUID();
        server.execute(() -> {
            try {
                require(Boolean.getBoolean("jetsetcraft.rushVisualAudit")
                        && server.getWorldPath(LevelResource.ROOT).toAbsolutePath().normalize().equals(expected), "Activity fixture lost its world guard");
                var player = server.getPlayerList().getPlayer(playerId);
                require(player != null, "Activity fixture player disconnected");
                var source = server.createCommandSourceStack().withPermission(4).withEntity(player).withSuppressedOutput();
                require(server.getCommands().performPrefixedCommand(source, "give @s minecraft:zombie_head 1") > 0,
                        "Vanilla head fixture command failed");
                fixtureApplied = true;
            } catch (RuntimeException error) {
                fixtureFailure = "Activity fixture failed: " + error.getMessage();
                JetSetCraft.LOGGER.error("Activity real-client fixture failed", error);
            }
        });
    }

    private static int hotbarSlot(Minecraft minecraft, boolean empty) {
        for (int slot = 0; slot < 9; slot++) {
            var stack = minecraft.player.getInventory().getItem(slot);
            if (empty ? stack.isEmpty() : stack.is(Items.ZOMBIE_HEAD)) return slot;
        }
        return -1;
    }

    private static void use(Minecraft minecraft, int slot) {
        require(slot >= 0 && minecraft.gameMode != null, "Missing actual tuner item or empty-hand slot");
        minecraft.player.getInventory().selected = slot;
        minecraft.gameMode.useItemOn(minecraft.player, InteractionHand.MAIN_HAND,
                new BlockHitResult(Vec3.atCenterOf(anchor).add(0, 0.5, 0), Direction.UP, anchor, false));
    }

    private static void click(Minecraft minecraft, String label) {
        require(minecraft.screen != null && !label.isBlank(), "Missing screen or label for an activity button");
        for (var child : minecraft.screen.children()) {
            if (child instanceof Button button && button.active && button.visible && button.getMessage().getString().equals(label)) {
                button.onPress(); return;
            }
        }
        throw new IllegalStateException("Missing real activity button: " + label);
    }

    private static void clickContaining(Minecraft minecraft, String label) {
        require(minecraft.screen != null, "Missing activity chooser");
        for (var child : minecraft.screen.children()) {
            if (child instanceof Button button && button.active && button.visible && button.getMessage().getString().contains(label)) {
                button.onPress(); return;
            }
        }
        throw new IllegalStateException("Missing real activity choice: " + label);
    }

    private static void require(boolean condition, String reason) { if (!condition) throw new IllegalStateException(reason); }
    private static void next(int value) { phase = value; ticks = 0; JetSetCraft.LOGGER.info("JETSETCRAFT_ACTIVITY_VISUAL_AUDIT_PHASE {}", value); }
    private ActivityJourneyVisualAudit() {}
}
