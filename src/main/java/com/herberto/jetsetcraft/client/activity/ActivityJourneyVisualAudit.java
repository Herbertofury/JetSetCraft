package com.herberto.jetsetcraft.client.activity;

import com.herberto.jetsetcraft.JetSetCraft;
import com.herberto.jetsetcraft.blockentity.BoomboxBlockEntity;
import com.herberto.jetsetcraft.client.crew.CrewHouseVisualAudit;
import com.herberto.jetsetcraft.client.crew.CrewHouseClient;
import com.herberto.jetsetcraft.client.rush.RivalRushClient;
import com.herberto.jetsetcraft.network.JetSetNetwork;
import com.herberto.jetsetcraft.registry.ModBlocks;
import com.herberto.jetsetcraft.rush.RivalRushManager;
import com.herberto.jetsetcraft.rush.RushWorld;
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
import org.lwjgl.glfw.GLFW;

import java.nio.file.Path;
import java.util.UUID;
import java.util.function.Consumer;
import java.util.function.Predicate;

/** Actual mode/UI round trip; cache inspection uses an explicitly logged disposable-world arrival fixture. */
public final class ActivityJourneyVisualAudit {
    private static int phase, ticks, captureStableTicks;
    private static BlockPos anchor;
    private static volatile boolean fixtureApplied;
    private static volatile boolean arenaFixtureApplied;
    private static volatile String fixtureFailure;
    private static String fixtureDimension, originalDimension;
    private static String previousRushSummary = "";
    private static Vec3 originalPosition;
    private static ListTag originalInventory;
    private static int originalLevel, originalTotalXp;
    private static float originalProgress;
    private static UUID runId, chosenRun;
    private static String selectedRoute;
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
        if (phase >= 7 && rush.getBoolean("Active") && !offers.isEmpty()) {
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
                if (!captureReady(minecraft)) return false;
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
                if (!captureReady(minecraft)) return false;
                capture.accept("10-arena-route"); next(8);
            }
            case 8 -> {
                if (!captured.test("10-arena-route")) return false;
                click(minecraft, "Journey"); next(11);
            }
            case 11 -> {
                if (!RivalRushClient.isJourneyMapScreenOpen()) return false;
                CompoundTag journey = rush.getCompound("Journey");
                require(journey.getString("Phase").equals("choosing") && journey.getInt("Completed") == 0
                        && journey.getList("History", Tag.TAG_COMPOUND).isEmpty() && !journey.contains("Current", Tag.TAG_COMPOUND)
                        && journey.getList("Choices", Tag.TAG_COMPOUND).size() == 3,
                        "Initial journey map invented a completed or unoffered room");
                for (var raw : journey.getList("Choices", Tag.TAG_COMPOUND)) {
                    CompoundTag room = (CompoundTag) raw;
                    require(room.contains("MapX") && room.getInt("MapZ") == 1 && !room.getString("RunReward").isBlank()
                            && room.getCompound("SharedReward").getInt("Supplies") > 0,
                            "Actual map offer lacks its route coordinate or server reward preview");
                }
                if (!captureReady(minecraft)) return false;
                capture.accept("11-journey-map"); next(12);
            }
            case 12 -> {
                if (!captured.test("11-journey-map")) return false;
                click(minecraft, "Build"); next(13);
            }
            case 13 -> {
                if (!RivalRushClient.isBuildScreenOpen()) return false;
                CompoundTag build = rush.getCompound("Build");
                require(build.getInt("ArtifactSlots") == 3 && build.getList("Artifacts", Tag.TAG_COMPOUND).size() <= 3
                        && Double.isFinite(build.getDouble("DamageMultiplier")) && build.getDouble("DamageMultiplier") >= 1
                        && build.getDouble("AreaMultiplier") >= 1, "Build screen lacks a genuine bounded server build");
                if (!captureReady(minecraft)) return false;
                capture.accept("12-run-build"); next(14);
            }
            case 14 -> {
                if (!captured.test("12-run-build")) return false;
                click(minecraft, "Journey"); next(15);
            }
            case 15 -> {
                if (!RivalRushClient.isJourneyMapScreenOpen()) return false;
                click(minecraft, "Compare routes"); next(16);
            }
            case 16 -> {
                if (!RivalRushClient.isJourneyScreenOpen()) return false;
                CompoundTag choice = rush.getCompound("Journey").getList("Choices", Tag.TAG_COMPOUND).getCompound(0);
                require(!choice.getString("Id").isBlank(), "First route has no authoritative profile identity");
                selectedRoute = choice.getString("Id");
                clickContaining(minecraft, choice.getString("Name")); next(9);
            }
            case 9 -> {
                CompoundTag journey = rush.getCompound("Journey");
                String journeyPhase = journey.getString("Phase");
                if (!journeyPhase.equals("travel") && !journeyPhase.equals("arena")) return false;
                require(rush.hasUUID("Run") && rush.getUUID("Run").equals(runId)
                        && journey.getCompound("Current").getString("Id").equals(selectedRoute),
                        "Selecting a route lost the live run or the actual chosen arena");
                if (++ticks < 20) return false;
                // Arrival is an explicit disposable-world fixture, not evidence of human traversal or a clear.
                // The normal arena builder, entry rules and cache loop still create the actual captured state.
                queueArenaArrival(minecraft, runId, selectedRoute); next(21);
            }
            case 21 -> {
                if (!arenaFixtureApplied) return false;
                require(rush.getBoolean("Active") && rush.hasUUID("Run") && rush.getUUID("Run").equals(runId),
                        "Cache audit ended or changed its live run");
                if (!rush.getCompound("Journey").getString("Phase").equals("arena")
                        || rush.getCompound("Caches").getList("Caches", Tag.TAG_COMPOUND).size() != 6) return false;
                require(rush.getCompound("Journey").getInt("Completed") == 0,
                        "The cache inspection unexpectedly cleared a room before its capture");
                JetSetNetwork.sendRushAction("menu", "", 0, runId, rush.getInt("Version")); next(22);
            }
            case 22 -> {
                require(rush.getBoolean("Active") && rush.hasUUID("Run") && rush.getUUID("Run").equals(runId),
                        "The run ended before cache scouting could open");
                if (RivalRushClient.isBuildScreenOpen() || tryClick(minecraft, "Build")) { next(23); return false; }
                // A genuine upgrade choice can close its menu. Reopen the ordinary UI after that response.
                if (++ticks % 20 == 0) JetSetNetwork.sendRushAction("menu", "", 0, runId, rush.getInt("Version"));
            }
            case 23 -> {
                if (!RivalRushClient.isBuildScreenOpen()) { next(22); return false; }
                click(minecraft, "Scout reward caches"); next(24);
            }
            case 24 -> {
                if (!RivalRushClient.isCacheScreenOpen()) { next(22); return false; }
                CompoundTag scouting = rush.getCompound("Caches");
                ListTag caches = scouting.getList("Caches", Tag.TAG_COMPOUND);
                require(rush.getBoolean("Active") && rush.hasUUID("Run") && rush.getUUID("Run").equals(runId)
                                && rush.getCompound("Journey").getString("Phase").equals("arena") && caches.size() == 6,
                        "The scout screen lost its real arena or six placed caches");
                for (var raw : caches) {
                    CompoundTag cache = (CompoundTag) raw;
                    require(!cache.getString("Label").isBlank() && !cache.getString("Benefit").isBlank()
                                    && cache.contains("X") && cache.contains("Y") && cache.contains("Z")
                                    && cache.getInt("Color") != 0 && cache.getInt("Distance") >= 0,
                            "A captured cache lacks a real destination, reward or marker identity");
                }
                if (!captureReady(minecraft)) return false;
                JetSetCraft.LOGGER.info("JETSETCRAFT_ACTIVITY_AUDIT_CACHE_SCOUT_OBSERVED run={} caches={} defeatsNeeded={} completed={}",
                        runId, caches.size(), scouting.getInt("DefeatsNeeded"), rush.getCompound("Journey").getInt("Completed"));
                capture.accept("14-cache-scout"); next(25);
            }
            case 25 -> {
                if (!captured.test("14-cache-scout")) return false;
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
                JetSetCraft.LOGGER.info("JETSETCRAFT_ACTIVITY_AUDIT_RETURN_RESTORED inventory/XP/dimension and source position verified before opening crew progress");
                click(minecraft, "Crew House"); next(17);
            }
            case 17 -> {
                if (!CrewHouseClient.isScreenOpen() || !CrewHouseClient.stateSnapshot().getBoolean("Exists")) return false;
                click(minecraft, "Progress"); next(18);
            }
            case 18 -> {
                if (!CrewHouseClient.isProgressScreenOpen()) return false;
                CompoundTag crew = CrewHouseClient.stateSnapshot();
                require(crew.hasUUID("HomeId") && crew.getList("RecentRewards", Tag.TAG_COMPOUND).size() <= 16
                        && !crew.getCompound("NextGoal").getString("Title").isBlank(),
                        "Progress view lost the actual home, bounded reward history or practical next goal");
                if (!captureReady(minecraft)) return false;
                capture.accept("13-crew-progress"); next(19);
            }
            case 19 -> {
                if (!captured.test("13-crew-progress")) return false;
                click(minecraft, "Style Library"); next(26);
            }
            case 26 -> {
                if (!CrewHouseClient.isStyleLibraryOpen()) return false;
                CompoundTag crew = CrewHouseClient.stateSnapshot();
                CompoundTag library = crew.getCompound("StyleLibrary");
                ListTag lessons = library.getList("Lessons", Tag.TAG_COMPOUND);
                require(crew.hasUUID("HomeId") && library.getInt("Total") == 16 && lessons.size() == 16
                                && library.getLong("Xp") >= 0 && library.getLong("Revision") >= 0
                                && !library.getString("Selected").isBlank(),
                        "Style Library screen lacks actual saved progression and server-owned choices");
                for (var raw : lessons) {
                    CompoundTag lesson = (CompoundTag) raw;
                    require(!lesson.getString("Id").isBlank() && !lesson.getString("Name").isBlank()
                                    && lesson.getBoolean("Unlocked") == (library.getLong("Xp") >= lesson.getInt("RequiredXp")),
                            "A photographed lesson disagrees with the real shared crew XP");
                }
                if (!captureReady(minecraft)) return false;
                JetSetCraft.LOGGER.info("JETSETCRAFT_ACTIVITY_AUDIT_STYLE_LIBRARY_OBSERVED xp={} learned={} selected={} revision={}",
                        library.getLong("Xp"), library.getInt("Unlocked"), library.getString("Selected"), library.getLong("Revision"));
                capture.accept("15-style-library"); next(27);
            }
            case 27 -> {
                if (!captured.test("15-style-library")) return false;
                JetSetCraft.LOGGER.info("JETSETCRAFT_ACTIVITY_VISUAL_AUDIT_PASS real Boombox preview; explicit Rival Rush; genuine Journey and Build; actual first route; logged fixture arrival (not human traversal); six live cache previews; inventory/XP/dimension restored; actual Progress and Style Library with no fabricated rewards");
                next(20); return true;
            }
            case 20 -> { return true; }
            default -> throw new IllegalStateException("Unknown activity audit phase " + phase);
        }
        return false;
    }

    private static void queueArenaArrival(Minecraft minecraft, UUID expectedRun, String expectedArena) {
        var server = minecraft.getSingleplayerServer();
        Path expected = minecraft.gameDirectory.toPath().resolve("saves/RivalRushAudit").toAbsolutePath().normalize();
        require(Boolean.getBoolean("jetsetcraft.rushVisualAudit") && server != null
                        && server.getWorldPath(LevelResource.ROOT).toAbsolutePath().normalize().equals(expected),
                "Arena arrival fixture requires the exact disposable RivalRushAudit save");
        UUID playerId = minecraft.player.getUUID();
        server.execute(() -> {
            try {
                require(Boolean.getBoolean("jetsetcraft.rushVisualAudit")
                                && server.getWorldPath(LevelResource.ROOT).toAbsolutePath().normalize().equals(expected),
                        "Arena arrival fixture lost its disposable-world guard");
                var player = server.getPlayerList().getPlayer(playerId);
                require(player != null && RushWorld.isRush(player.level()), "Arena fixture lost its player or Rush dimension");
                var run = RivalRushManager.run(player);
                CompoundTag journey = RivalRushManager.journeySnapshot(player);
                BlockPos destination = RivalRushManager.arenaDestination(player);
                require(run != null && run.id().equals(expectedRun) && destination != null
                                && (journey.getString("Phase").equals("travel") || journey.getString("Phase").equals("arena"))
                                && journey.getCompound("Current").getString("Id").equals(expectedArena)
                                && journey.getInt("Completed") == 0,
                        "Arena arrival fixture no longer matches the selected live run and destination");
                // Fixture-only chunk access. Production arena construction never adds tickets or force-loads.
                for (int x = -3; x <= 3; x++) for (int z = -3; z <= 3; z++)
                    player.serverLevel().getChunk((destination.getX() >> 4) + x, (destination.getZ() >> 4) + z);
                Vec3 from = player.position();
                player.teleportTo(player.serverLevel(), destination.getX() + .5, RushWorld.FLOOR_Y + 1,
                        destination.getZ() + .5, player.getYRot(), player.getXRot());
                player.setDeltaMovement(Vec3.ZERO); player.fallDistance = 0;
                JetSetCraft.LOGGER.info("JETSETCRAFT_ACTIVITY_AUDIT_ARENA_ARRIVAL_FIXTURE run={} arena={} from={} to={} (teleport and nearby chunk loading only; no clock, defeats, rewards or clear fabricated)",
                        expectedRun, expectedArena, from, player.position());
                arenaFixtureApplied = true;
            } catch (RuntimeException error) {
                fixtureFailure = "Arena arrival fixture failed: " + error.getMessage();
                JetSetCraft.LOGGER.error("Activity arena arrival fixture failed", error);
            }
        });
    }

    private static void queueFixture(Minecraft minecraft) {
        var server = minecraft.getSingleplayerServer();
        Path expected = minecraft.gameDirectory.toPath().resolve("saves/RivalRushAudit").toAbsolutePath().normalize();
        require(server != null && server.getWorldPath(LevelResource.ROOT).toAbsolutePath().normalize().equals(expected),
                "Activity fixture requires the exact disposable RivalRushAudit save");
        anchor = CrewHouseVisualAudit.completedHomeAnchor(minecraft);
        fixtureDimension = minecraft.level.dimension().location().toString();
        requireBoombox(minecraft);
        UUID playerId = minecraft.player.getUUID();
        server.execute(() -> {
            try {
                require(Boolean.getBoolean("jetsetcraft.rushVisualAudit")
                        && server.getWorldPath(LevelResource.ROOT).toAbsolutePath().normalize().equals(expected), "Activity fixture lost its world guard");
                var player = server.getPlayerList().getPlayer(playerId);
                require(player != null, "Activity fixture player disconnected");
                require(player.level().dimension().location().toString().equals(fixtureDimension)
                        && player.serverLevel().hasChunkAt(anchor)
                        && player.serverLevel().getBlockState(anchor).is(ModBlocks.BOOMBOX.get())
                        && player.serverLevel().getBlockEntity(anchor) instanceof BoomboxBlockEntity
                        && player.getEyePosition().distanceToSqr(Vec3.atCenterOf(anchor).add(0, 0.5, 0)) <= 4.5 * 4.5,
                        "The exact founded Boombox is unavailable or outside genuine server interaction range");
                var source = player.createCommandSourceStack().withPermission(4).withSuppressedOutput();
                require(server.getCommands().performPrefixedCommand(source, "give @s minecraft:zombie_head 1") > 0,
                        "Vanilla head fixture command failed");
                JetSetCraft.LOGGER.info("JETSETCRAFT_ACTIVITY_AUDIT_ANCHOR_VERIFIED dimension={} anchor={} player={} grounded={} distance={}",
                        fixtureDimension, anchor.toShortString(), player.position(), player.onGround(),
                        player.getEyePosition().distanceTo(Vec3.atCenterOf(anchor).add(0, 0.5, 0)));
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
        requireBoombox(minecraft);
        minecraft.player.getInventory().selected = slot;
        minecraft.gameMode.useItemOn(minecraft.player, InteractionHand.MAIN_HAND,
                new BlockHitResult(Vec3.atCenterOf(anchor).add(0, 0.5, 0), Direction.UP, anchor, false));
    }

    private static void requireBoombox(Minecraft minecraft) {
        double distance = minecraft.player == null || anchor == null ? Double.NaN
                : minecraft.player.getEyePosition().distanceTo(Vec3.atCenterOf(anchor).add(0, 0.5, 0));
        require(minecraft.level != null && minecraft.player != null && anchor != null
                && minecraft.level.dimension().location().toString().equals(fixtureDimension)
                && minecraft.level.hasChunkAt(anchor) && minecraft.level.getBlockState(anchor).is(ModBlocks.BOOMBOX.get())
                && minecraft.level.getBlockEntity(anchor) instanceof BoomboxBlockEntity
                && minecraft.player.onGround() && !minecraft.player.isInWaterOrBubble()
                && distance <= 4.5,
                "The exact founded Boombox is missing, unloaded or outside the supported client interaction position: anchor="
                        + anchor + ", player=" + (minecraft.player == null ? "disconnected" : minecraft.player.position())
                        + ", grounded=" + (minecraft.player != null && minecraft.player.onGround()) + ", distance=" + distance);
    }

    private static void click(Minecraft minecraft, String label) {
        require(minecraft.screen != null && !label.isBlank(), "Missing screen or label for an activity button");
        if (tryClick(minecraft, label)) return;
        throw new IllegalStateException("Missing real activity button: " + label);
    }

    private static boolean tryClick(Minecraft minecraft, String label) {
        if (minecraft.screen == null || label.isBlank()) return false;
        for (var child : minecraft.screen.children()) {
            if (child instanceof Button button && button.active && button.visible && button.getMessage().getString().equals(label)) {
                button.onPress(); return true;
            }
        }
        return false;
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

    private static boolean captureReady(Minecraft minecraft) {
        ticks++;
        long window = minecraft.getWindow().getWindow();
        boolean focused = GLFW.glfwGetWindowAttrib(window, GLFW.GLFW_FOCUSED) == GLFW.GLFW_TRUE;
        double x = minecraft.mouseHandler.xpos(), y = minecraft.mouseHandler.ypos();
        boolean corner = x >= 0 && x <= 24 && y >= 0 && y <= 24;
        if (focused && corner) {
            if (++captureStableTicks >= 20) {
                JetSetCraft.LOGGER.info("JETSETCRAFT_ACTIVITY_AUDIT_CURSOR_SETTLED phase={} observed=({}, {})", phase, x, y);
                return true;
            }
        } else {
            captureStableTicks = 0;
            if (!focused) {
                if (ticks == 1 || ticks % 10 == 0) GLFW.glfwFocusWindow(window);
            } else {
                // X11 suppresses the event matching GLFW's last warp. Two genuine native moves allow
                // the first event through; alternating coordinates also avoids a cached-position no-op.
                double offset = ticks & 1;
                GLFW.glfwSetCursorPos(window, 8 + offset, 8);
                GLFW.glfwSetCursorPos(window, 12 + offset, 12);
            }
        }
        if (ticks >= 60) {
            JetSetCraft.LOGGER.warn("JETSETCRAFT_ACTIVITY_AUDIT_CURSOR_UNCONFIRMED phase={} focused={} observed=({}, {}); preserving the actual capture",
                    phase, focused, x, y);
            return true;
        }
        return false;
    }

    private static void require(boolean condition, String reason) { if (!condition) throw new IllegalStateException(reason); }
    private static void next(int value) { phase = value; ticks = 0; captureStableTicks = 0; JetSetCraft.LOGGER.info("JETSETCRAFT_ACTIVITY_VISUAL_AUDIT_PHASE {}", value); }
    private ActivityJourneyVisualAudit() {}
}
