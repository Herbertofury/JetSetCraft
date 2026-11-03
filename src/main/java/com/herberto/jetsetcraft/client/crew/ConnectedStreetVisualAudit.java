package com.herberto.jetsetcraft.client.crew;

import com.herberto.jetsetcraft.JetSetCraft;
import com.herberto.jetsetcraft.activity.ActivityManager;
import com.herberto.jetsetcraft.blockentity.BoomboxBlockEntity;
import com.herberto.jetsetcraft.client.activity.ActivityClient;
import com.herberto.jetsetcraft.client.rush.RivalRushClient;
import com.herberto.jetsetcraft.crew.CrewHouseData;
import com.herberto.jetsetcraft.crew.CrewHouseManager;
import com.herberto.jetsetcraft.liberty.LibertyManager;
import com.herberto.jetsetcraft.network.JetSetNetwork;
import com.herberto.jetsetcraft.registry.ModBlocks;
import com.herberto.jetsetcraft.rush.RivalRushManager;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.Button;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.level.storage.LevelResource;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import org.lwjgl.glfw.GLFW;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.function.Consumer;
import java.util.function.Predicate;

/**
 * Actual Crew Tour navigation, minimum-size scrolling and an unlaunched Street Mix preview.
 * This append-only audit reads the progress earned by preceding fixtures. It assigns no score,
 * reward, completion, unlock or account state, and never launches a street round.
 */
public final class ConnectedStreetVisualAudit {
    private static int phase, ticks, stableTicks, originalWidth, originalHeight, originalScale, originalSlot;
    private static boolean configured;
    private static long phaseStarted;
    private static UUID previewContext;
    private static ListTag inventory;
    private static int level, totalXp;
    private static float xpProgress;
    private static String dimension;
    private static Vec3 position;
    private static volatile boolean baselineReady, finalReady;
    private static volatile CompoundTag baselineTour, baselineAccount;
    private static volatile String readFailure;

    public static boolean advance(Minecraft minecraft, Consumer<String> capture, Predicate<String> captured) {
        guardedServer(minecraft);
        require(minecraft.player != null && minecraft.level != null && minecraft.gameMode != null
                        && minecraft.getConnection() != null, "Connected-street audit lost its connected player");
        if (readFailure != null) throw new IllegalStateException(readFailure);
        if (phaseStarted == 0) phaseStarted = System.nanoTime();
        require(System.nanoTime() - phaseStarted < 60_000_000_000L, "Connected-street audit stage timed out: " + phase);
        CompoundTag crew = CrewHouseClient.stateSnapshot(), tour = crew.getCompound("CrewTour");
        CompoundTag activity = ActivityClient.stateSnapshot();
        switch (phase) {
            case 0 -> {
                require(!RivalRushClient.active(), "Connected-street audit requires a returned Rush player");
                originalWidth = minecraft.getWindow().getWidth(); originalHeight = minecraft.getWindow().getHeight();
                originalScale = minecraft.options.guiScale().get(); originalSlot = minecraft.player.getInventory().selected;
                configured = true;
                inventory = minecraft.player.getInventory().save(new ListTag()).copy();
                level = minecraft.player.experienceLevel; totalXp = minecraft.player.totalExperience;
                xpProgress = minecraft.player.experienceProgress; dimension = minecraft.level.dimension().location().toString();
                position = minecraft.player.position();
                viewport(minecraft, 1280, 720, 2);
                if (minecraft.screen != null) minecraft.screen.onClose();
                queueRead(minecraft, false);
                JetSetNetwork.sendCrewAction("open", "", 0, null, null, 0); next(1);
            }
            case 1 -> {
                if (!baselineReady || !(minecraft.screen instanceof CrewHouseScreen)) return false;
                click(minecraft, "Progress", false); next(2);
            }
            case 2 -> {
                if (!(minecraft.screen instanceof CrewHouseScreen screen) || !screen.isView("PROGRESS")) return false;
                click(minecraft, "Crew Tour", true); next(3);
            }
            case 3 -> {
                if (!(minecraft.screen instanceof CrewHouseScreen screen) || !screen.isView("TOUR")) return false;
                validateTour(tour); validateButtons(minecraft);
                require(screen.width >= 600 && screen.height >= 350, "Crew Tour standard viewport did not settle");
                if (!captureReady(minecraft)) return false;
                JetSetCraft.LOGGER.info("JETSETCRAFT_CONNECTED_AUDIT_TOUR_OBSERVED revision={} results={} modes={} core={}/{} optional={}/{} ready={} (actual prior activity receipts; read-only navigation, no award or unlock assigned)",
                        tour.getLong("Revision"), tour.getInt("Results"), tour.getInt("Modes"), tour.getInt("BaseClaimed"),
                        tour.getInt("BaseTotal"), tour.getInt("OptionalClaimed"), tour.getInt("OptionalTotal"), tour.getInt("Ready"));
                capture.accept("24-crew-tour"); next(4);
            }
            case 4 -> {
                if (!captured.test("24-crew-tour")) return false;
                viewport(minecraft, 640, 480, 2); next(5);
            }
            case 5 -> {
                if (!(minecraft.screen instanceof CrewHouseScreen screen) || !screen.isView("TOUR")
                        || screen.width != 320 || screen.height != 240) return false;
                require(screen.mouseScrolled(160, 120, -100), "The minimum-size tour did not accept real detail scrolling");
                require(screen.detailScrollForAudit() > 0, "The actual Crew Tour detail pane did not scroll"); next(6);
            }
            case 6 -> {
                if (!(minecraft.screen instanceof CrewHouseScreen screen) || !screen.isView("TOUR")) return false;
                require(screen.width == 320 && screen.height == 240 && screen.detailScrollForAudit() > 0,
                        "Minimum-size Crew Tour lost its real viewport or scroll position");
                validateTour(tour); validateButtons(minecraft);
                if (!captureReady(minecraft)) return false;
                JetSetCraft.LOGGER.info("JETSETCRAFT_CONNECTED_AUDIT_MINIMUM_OBSERVED width={} height={} scroll={} revision={} (actual native detail scroll and bounded nonoverlapping controls)",
                        screen.width, screen.height, screen.detailScrollForAudit(), tour.getLong("Revision"));
                capture.accept("25-crew-tour-minimum"); next(7);
            }
            case 7 -> {
                if (!captured.test("25-crew-tour-minimum")) return false;
                viewport(minecraft, 1280, 720, 2); minecraft.setScreen(null); useBoombox(minecraft); next(8);
            }
            case 8 -> {
                if (!ActivityClient.isScreenOpen() || !activity.getBoolean("FromBoombox") || !activity.hasUUID("Context")) return false;
                require(!activity.getBoolean("Active"), "Reading the Boombox invitation started a round");
                if (++ticks < 10) return false;
                clickChoice(minecraft, "Tag"); next(9);
            }
            case 9 -> {
                if (!ActivityClient.isScreenOpen() || !activity.getString("SelectedMode").equals("tag")
                        || !activity.getBoolean("CanChooseFormat")) return false;
                require(!activity.getBoolean("Active") && activity.getString("Mode").equals("tag"),
                        "Explicit Tag preview started or randomized the activity");
                if (++ticks < 10) return false;
                click(minecraft, "Street Mix", true); next(10);
            }
            case 10 -> {
                if (!ActivityClient.isScreenOpen() || !activity.getString("Format").equals("street_mix")) return false;
                require(activity.getBoolean("Mix") && !activity.getBoolean("Active") && !activity.getBoolean("Completed")
                                && activity.getInt("MixTotal") == 3 && activity.getInt("MixCleared") == 0
                                && activity.getInt("Progress") == 0 && activity.getBoolean("CanLaunch")
                                && activity.getString("Mode").equals("tag") && !activity.getString("MixRoute").isBlank()
                                && !activity.getString("MixModifier").isBlank(),
                        "Street Mix preview lacks its real three-round route or invents active/earned progress");
                validateButtons(minecraft);
                if (!captureReady(minecraft)) return false;
                previewContext = activity.getUUID("Context");
                JetSetCraft.LOGGER.info("JETSETCRAFT_CONNECTED_AUDIT_MIX_PREVIEW_OBSERVED context={} version={} route={} modifier={} active={} cleared={} (actual UI selection; no launch, objectives or reward assigned)",
                        previewContext, activity.getInt("Version"), activity.getString("MixRoute"), activity.getString("MixModifier"),
                        activity.getBoolean("Active"), activity.getInt("MixCleared"));
                capture.accept("26-street-mix-preview"); next(11);
            }
            case 11 -> {
                if (!captured.test("26-street-mix-preview")) return false;
                ActivityClient.request("cancel", ""); next(12);
            }
            case 12 -> {
                if (!ActivityClient.isScreenOpen() || !activity.hasUUID("Context") || activity.getUUID("Context").equals(previewContext)) return false;
                require(!activity.getBoolean("Active") && !activity.getBoolean("Mix"), "Cancelling the unlaunched mix retained or started it");
                click(minecraft, "Done", false); queueRead(minecraft, true); next(13);
            }
            case 13 -> {
                if (!finalReady) return false;
                require(inventory.equals(minecraft.player.getInventory().save(new ListTag()))
                                && minecraft.player.experienceLevel == level && minecraft.player.totalExperience == totalXp
                                && Math.abs(minecraft.player.experienceProgress - xpProgress) < .000001F
                                && dimension.equals(minecraft.level.dimension().location().toString())
                                && minecraft.player.position().distanceToSqr(position) <= 1,
                        "Read-only tour/mix preview changed normal inventory, XP, dimension or position");
                restoreViewport(minecraft);
                JetSetCraft.LOGGER.info("JETSETCRAFT_CONNECTED_STREET_VISUAL_AUDIT_PASS three appended real-client captures; actual Crew Tour snapshot and 320x240 scrolling; explicit Tag Street Mix preview cancelled through production packet before launch; exact tour/account/inventory/XP/dimension preserved; no reward, unlock, victory or human gameplay fabricated");
                next(14); return true;
            }
            case 14 -> { return true; }
            default -> throw new IllegalStateException("Unknown connected-street audit phase " + phase);
        }
        return false;
    }

    private static void validateTour(CompoundTag tour) {
        require(baselineReady && tour.getLong("Revision") == baselineTour.getLong("Revision")
                        && tour.getInt("Results") == baselineTour.getInt("Results")
                        && tour.getInt("Modes") == baselineTour.getInt("Modes")
                        && tour.getInt("Claimed") == baselineTour.getInt("Claimed")
                        && tour.getString("TrainingSummary").equals(baselineTour.getString("TrainingSummary"))
                        && tour.getBoolean("TrainingEligible") == baselineTour.getBoolean("TrainingEligible")
                        && tour.getInt("BaseTotal") == 9 && tour.getInt("OptionalTotal") == 3
                        && tour.getList("Milestones", Tag.TAG_COMPOUND).size() == 12
                        && tour.getList("Tracks", Tag.TAG_COMPOUND).equals(baselineTour.getList("Tracks", Tag.TAG_COMPOUND)),
                "Displayed tour totals diverge from the actual read-only server account");
        ListTag rows = tour.getList("Milestones", Tag.TAG_COMPOUND), baseline = baselineTour.getList("Milestones", Tag.TAG_COMPOUND);
        for (int i = 0; i < rows.size(); i++) {
            CompoundTag row = rows.getCompound(i), original = baseline.getCompound(i);
            boolean complete = true;
            for (Tag raw : row.getList("Requirements", Tag.TAG_COMPOUND)) {
                CompoundTag requirement = (CompoundTag) raw;
                require(requirement.getBoolean("Complete") == (requirement.getInt("Current") >= requirement.getInt("Target")),
                        "A displayed milestone requirement invented completion");
                complete &= requirement.getBoolean("Complete");
            }
            require(row.getString("Id").equals(original.getString("Id"))
                            && row.getList("Requirements", Tag.TAG_COMPOUND).equals(original.getList("Requirements", Tag.TAG_COMPOUND))
                            && row.getBoolean("Claimed") == original.getBoolean("Claimed")
                            && row.getBoolean("Ready") == (complete && !row.getBoolean("Claimed"))
                            && (!row.getBoolean("CanClaim") || row.getBoolean("Ready")),
                    "A tour screenshot would advertise an unearned claim or modified requirement");
        }
    }

    private static void queueRead(Minecraft minecraft, boolean finalCheck) {
        MinecraftServer server = guardedServer(minecraft); UUID owner = minecraft.player.getUUID();
        server.execute(() -> {
            try {
                ServerPlayer player = server.getPlayerList().getPlayer(owner);
                require(player != null && !RivalRushManager.active(player) && !LibertyManager.active(player)
                                && !ActivityManager.active(player), "Read-only connected audit found an active game");
                CrewHouseData data = CrewHouseData.get(server);
                CompoundTag tour = data.tourSnapshot(owner), account = account(CrewHouseManager.snapshot(player));
                if (finalCheck) {
                    require(tour.equals(baselineTour) && account.equals(baselineAccount),
                            "Inspecting the tour or cancelling an unlaunched mix mutated actual shared progression");
                    finalReady = true;
                } else { baselineTour = tour.copy(); baselineAccount = account; baselineReady = true; }
            } catch (RuntimeException error) {
                readFailure = "Connected-street server observation failed: " + error.getMessage();
                JetSetCraft.LOGGER.error("Connected-street read-only observation failed", error);
            }
        });
    }

    private static CompoundTag account(CompoundTag state) {
        CompoundTag account = new CompoundTag();
        for (String key : new String[]{"Supplies", "CrewXp", "PendingSupplies", "PendingCrewXp", "PendingTraining", "RecentRewards", "StyleLibrary"})
            if (state.contains(key)) account.put(key, state.get(key).copy());
        return account;
    }

    private static void useBoombox(Minecraft minecraft) {
        BlockPos anchor = CrewHouseVisualAudit.completedHomeAnchor(minecraft);
        require(minecraft.level.hasChunkAt(anchor) && minecraft.level.getBlockState(anchor).is(ModBlocks.BOOMBOX.get())
                        && minecraft.level.getBlockEntity(anchor) instanceof BoomboxBlockEntity box && !box.targetStack().isEmpty()
                        && minecraft.player.onGround() && !minecraft.player.isInWaterOrBubble()
                        && minecraft.player.getEyePosition().distanceToSqr(Vec3.atCenterOf(anchor).add(0, .5, 0)) <= 4.5 * 4.5,
                "The existing tuned Boombox is outside genuine loaded interaction range");
        int empty = -1;
        for (int slot = 0; slot < 9; slot++) if (minecraft.player.getInventory().getItem(slot).isEmpty()) { empty = slot; break; }
        require(empty >= 0, "The preview audit has no actual empty-hand hotbar slot");
        minecraft.player.getInventory().selected = empty;
        minecraft.gameMode.useItemOn(minecraft.player, InteractionHand.MAIN_HAND,
                new BlockHitResult(Vec3.atCenterOf(anchor).add(0, .5, 0), Direction.UP, anchor, false));
    }

    private static void validateButtons(Minecraft minecraft) {
        require(minecraft.screen != null, "No native screen to inspect"); List<Button> buttons = new ArrayList<>();
        for (var child : minecraft.screen.children()) if (child instanceof Button button && button.visible) {
            require(button.getX() >= 0 && button.getY() >= 0 && button.getWidth() > 0 && button.getHeight() > 0
                            && button.getX() + button.getWidth() <= minecraft.screen.width
                            && button.getY() + button.getHeight() <= minecraft.screen.height,
                    "A native connected-mode button lies outside its viewport: " + button.getMessage().getString());
            for (Button other : buttons) require(button.getX() >= other.getX() + other.getWidth()
                            || other.getX() >= button.getX() + button.getWidth()
                            || button.getY() >= other.getY() + other.getHeight()
                            || other.getY() >= button.getY() + button.getHeight(),
                    "Connected-mode controls overlap: " + button.getMessage().getString() + " / " + other.getMessage().getString());
            buttons.add(button);
        }
    }

    private static void clickChoice(Minecraft minecraft, String label) {
        require(minecraft.screen != null, "Missing native activity choice");
        for (var child : minecraft.screen.children()) if (child instanceof Button button && button.visible && button.active
                && button.getMessage().getString().replaceFirst("^• ", "").equals(label)) { button.onPress(); return; }
        throw new IllegalStateException("Missing actual activity choice " + label);
    }
    private static void click(Minecraft minecraft, String label, boolean prefix) {
        require(minecraft.screen != null, "Missing native connected-mode screen");
        for (var child : minecraft.screen.children()) if (child instanceof Button button && button.visible && button.active
                && (prefix ? button.getMessage().getString().startsWith(label) : button.getMessage().getString().equals(label))) {
            button.onPress(); return;
        }
        throw new IllegalStateException("Missing actual connected-mode button " + label);
    }
    private static boolean captureReady(Minecraft minecraft) {
        ticks++;
        long window = minecraft.getWindow().getWindow();
        boolean focused = GLFW.glfwGetWindowAttrib(window, GLFW.GLFW_FOCUSED) == GLFW.GLFW_TRUE;
        double x = minecraft.mouseHandler.xpos(), y = minecraft.mouseHandler.ypos();
        if (focused && x >= 0 && x <= 24 && y >= 0 && y <= 24) { if (++stableTicks >= 12) return true; }
        else {
            stableTicks = 0;
            if (!focused) GLFW.glfwFocusWindow(window);
            else { double offset = ticks & 1; GLFW.glfwSetCursorPos(window, 8 + offset, 8); GLFW.glfwSetCursorPos(window, 12 + offset, 12); }
        }
        if (ticks >= 60) {
            JetSetCraft.LOGGER.warn("JETSETCRAFT_CONNECTED_AUDIT_CURSOR_UNCONFIRMED phase={} focused={} observed=({}, {}); preserving actual capture", phase, focused, x, y);
            return true;
        }
        return false;
    }
    private static MinecraftServer guardedServer(Minecraft minecraft) {
        MinecraftServer server = minecraft.getSingleplayerServer();
        Path expected = minecraft.gameDirectory.toPath().resolve("saves/RivalRushAudit").toAbsolutePath().normalize();
        require(Boolean.getBoolean("jetsetcraft.rushVisualAudit") && server != null
                        && server.getWorldPath(LevelResource.ROOT).toAbsolutePath().normalize().equals(expected),
                "Connected-street audit requires the exact disposable RivalRushAudit world");
        return server;
    }
    private static void viewport(Minecraft minecraft, int width, int height, int scale) {
        minecraft.getWindow().setWindowed(width, height); minecraft.options.guiScale().set(scale); minecraft.resizeDisplay();
    }
    /** Also called on audit failure so a verification run cannot leave the user's UI settings changed. */
    public static void restoreViewport(Minecraft minecraft) {
        if (!configured) return;
        viewport(minecraft, originalWidth, originalHeight, originalScale);
        if (minecraft.player != null) minecraft.player.getInventory().selected = originalSlot;
        configured = false;
    }
    private static void next(int value) { phase = value; ticks = 0; stableTicks = 0; phaseStarted = System.nanoTime(); }
    private static void require(boolean condition, String message) { if (!condition) throw new IllegalStateException(message); }
    private ConnectedStreetVisualAudit() {}
}
