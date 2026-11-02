package com.herberto.jetsetcraft.client.rush;

import com.herberto.jetsetcraft.JetSetCraft;
import com.herberto.jetsetcraft.client.crew.CrewHouseVisualAudit;
import com.herberto.jetsetcraft.client.activity.ActivityJourneyVisualAudit;
import com.herberto.jetsetcraft.network.JetSetNetwork;
import net.minecraft.client.CameraType;
import net.minecraft.client.Minecraft;
import net.minecraft.client.Screenshot;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.BackupConfirmScreen;
import net.minecraft.client.gui.screens.ConfirmScreen;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.tutorial.TutorialSteps;
import net.minecraft.nbt.ListTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.contents.TranslatableContents;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.storage.LevelResource;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Opt-in acceptance through the real client, production network and integrated server.
 * Enable only with -Djetsetcraft.rushVisualAudit=true against the disposable RivalRushAudit save.
 * This never creates offers, cards, run state, entities or rewards through a test-only shortcut.
 */
@Mod.EventBusSubscriber(modid = JetSetCraft.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE, value = Dist.CLIENT)
public final class RivalRushVisualAudit {
    private static final long DEADLINE_NANOS = 180_000_000_000L;
    private static final byte[] PNG = {(byte) 137, 80, 78, 71, 13, 10, 26, 10};
    private static final String STAMP = Long.toString(System.currentTimeMillis());
    private static final Map<String, Path> CAPTURES = new ConcurrentHashMap<>();
    private static long started;
    private static long failureCaptureStarted;
    private static String failureCaptureLabel;
    private static String lastWaitingScreen = "";
    private static Screen confirmedWorldScreen;
    private static TutorialSteps originalTutorialStep;
    private static long fixtureObservedAt;
    private static int fixtureSettledTicks;
    private static int phase, ticks;
    private static int chosenVersion = Integer.MIN_VALUE;
    private static UUID chosenRun;
    private static boolean waitingChoice, complete, fixtureQueued;
    private static volatile boolean fixtureApplied;
    private static volatile String captureFailure;
    private static String originalDimension;
    private static Vec3 originalPosition;
    private static ListTag originalInventory;
    private static int originalLevel, originalTotalXp;
    private static float originalProgress;

    @SubscribeEvent
    public static void tick(TickEvent.ClientTickEvent event) {
        if (event.phase != TickEvent.Phase.END || complete || !Boolean.getBoolean("jetsetcraft.rushVisualAudit")) return;
        Minecraft minecraft = Minecraft.getInstance();
        if (failureCaptureStarted != 0L) {
            if (CAPTURES.containsKey(failureCaptureLabel) || captureFailure != null
                    || System.nanoTime() - failureCaptureStarted > 15_000_000_000L) {
                if (!CAPTURES.containsKey(failureCaptureLabel)) {
                    JetSetCraft.LOGGER.error("JETSETCRAFT_RUSH_VISUAL_AUDIT_FAILURE_CAPTURE_UNAVAILABLE {}", captureFailure);
                }
                complete = true;
                stopClient(minecraft);
            }
            return;
        }
        if (started == 0L) started = System.nanoTime();
        if (System.nanoTime() - started > DEADLINE_NANOS) {
            finish(minecraft, false, "180-second deadline exceeded in phase " + phase);
            return;
        }
        if (captureFailure != null) { finish(minecraft, false, captureFailure); return; }
        try {
            if (phase == 0 && originalTutorialStep == null && isDisposableQuickPlayLaunch(minecraft)) {
                originalTutorialStep = minecraft.options.tutorialStep;
                minecraft.getTutorial().setStep(TutorialSteps.NONE);
                JetSetCraft.LOGGER.info("JETSETCRAFT_RUSH_VISUAL_AUDIT_TUTORIAL_OPTION none (restored at shutdown)");
            }
            if (minecraft.player == null || minecraft.level == null || minecraft.getConnection() == null) {
                handleWorldLoadScreen(minecraft);
                return;
            }
            advance(minecraft);
        } catch (RuntimeException error) {
            JetSetCraft.LOGGER.error("Rival Rush real-client audit failed", error);
            finish(minecraft, false, "exception in phase " + phase + ": " + error.getClass().getSimpleName());
        }
    }

    private static void advance(Minecraft minecraft) {
        var state = RivalRushClient.state();
        // A real pickup can produce a legitimate offer during the capture. Choose its actual first entry
        // through the same bounded packet as the UI, then wait for the authoritative response.
        if (phase >= 3 && phase < 8 && state.getBoolean("Active") && !RivalRushClient.offers().isEmpty()) {
            UUID run = RivalRushClient.runId();
            int version = state.getInt("Version");
            if (!java.util.Objects.equals(run, chosenRun) || chosenVersion != version) {
                var offer = RivalRushClient.offers().getCompound(0);
                request("choose", offer.getString("id"), 0);
                chosenRun = run;
                chosenVersion = version;
                waitingChoice = true;
                ticks = 0;
                JetSetCraft.LOGGER.info("JETSETCRAFT_RUSH_VISUAL_AUDIT_CHOICE {} version {}", offer.getString("id"), version);
            }
            return;
        }
        if (waitingChoice) {
            waitingChoice = false;
            ticks = 0;
            if (phase == 6) request("album", "", 0);
        }
        switch (phase) {
            case 0 -> {
                if (++ticks < 60) return; // Let ordinary spawn/connection/inventory synchronization settle.
                originalDimension = minecraft.level.dimension().location().toString();
                if (isDistrict(minecraft)) { finish(minecraft, false, "audit must start in normal Minecraft"); return; }
                if (!fixtureQueued) {
                    fixtureQueued = true;
                    queueVanillaFixture(minecraft);
                    return;
                }
                if (!fixtureApplied || minecraft.player.getInventory().countItem(Items.DIAMOND) < 7
                        || minecraft.player.getInventory().countItem(Items.BREAD) < 5
                        || minecraft.player.experienceLevel < 23 || minecraft.player.totalExperience < 17
                        || minecraft.player.experienceProgress <= 0.0F) return;
                if (fixtureObservedAt == 0L) {
                    fixtureObservedAt = System.nanoTime();
                    JetSetCraft.LOGGER.info("JETSETCRAFT_RUSH_VISUAL_AUDIT_FIXTURE_OBSERVED diamonds={} bread={} level={} totalXp={} progress={}",
                            minecraft.player.getInventory().countItem(Items.DIAMOND), minecraft.player.getInventory().countItem(Items.BREAD),
                            minecraft.player.experienceLevel, minecraft.player.totalExperience, minecraft.player.experienceProgress);
                }
                // Let the real recipe/advancement toasts and chat fade naturally after vanilla give commands.
                if (++fixtureSettledTicks < 240 || System.nanoTime() - fixtureObservedAt < 12_000_000_000L) return;
                request("menu", "", 0);
                next(1);
            }
            case 1 -> {
                if (!(minecraft.screen instanceof RivalRushScreen) || state.getBoolean("Active")
                        || minecraft.screen.children().stream().noneMatch(child -> child instanceof Button button
                        && button.getMessage().getString().equals("Start 30-minute run"))) { ticks = 0; return; }
                if (++ticks < 20) return;
                capture(minecraft, "01-setup");
                next(2);
            }
            case 2 -> {
                if (!CAPTURES.containsKey("01-setup")) return;
                // The setup screen intentionally does not pause Minecraft. Observe the source state now,
                // immediately before the request, rather than before a possible gravity/weather tick.
                originalDimension = minecraft.level.dimension().location().toString();
                originalPosition = minecraft.player.position();
                originalInventory = minecraft.player.getInventory().save(new ListTag()).copy();
                originalLevel = minecraft.player.experienceLevel;
                originalTotalXp = minecraft.player.totalExperience;
                originalProgress = minecraft.player.experienceProgress;
                request("start", "jetsetcraft:dead_beat|minecraft:zombie", 0);
                next(3);
            }
            case 3 -> {
                if (!state.getBoolean("Active")) {
                    if (!state.getString("Summary").isBlank()) finish(minecraft, false, "server rejected entry: " + state.getString("Summary"));
                    return;
                }
                if (!isDistrict(minecraft)) return;
                if (!state.hasUUID("Run") || state.getInt("Level") < 1 || state.getLong("NextXp") < 1
                        || !Double.isFinite(state.getDouble("Vitality")) || state.getDouble("Vitality") <= 0) {
                    finish(minecraft, false, "invalid authoritative run snapshot"); return;
                }
                minecraft.options.setCameraType(CameraType.THIRD_PERSON_BACK);
                minecraft.setScreen(null);
                JetSetCraft.LOGGER.info("JETSETCRAFT_RUSH_VISUAL_AUDIT_ENTERED {}", minecraft.level.dimension().location());
                next(4);
            }
            case 4 -> {
                if (!state.getBoolean("Active") || !isDistrict(minecraft)) { finish(minecraft, false, "run ended before district capture"); return; }
                if (minecraft.screen != null || state.getLong("Ticks") < 60L
                        || minecraft.level.getBlockState(minecraft.player.blockPosition().below()).isAir()) { ticks = 0; return; }
                if (++ticks < 30) return;
                capture(minecraft, "02-district-hud");
                next(5);
            }
            case 5 -> {
                if (!CAPTURES.containsKey("02-district-hud")) return;
                minecraft.options.setCameraType(CameraType.FIRST_PERSON);
                request("album", "", 0);
                next(6);
            }
            case 6 -> {
                if (!state.getBoolean("Active") || !isDistrict(minecraft)) { finish(minecraft, false, "run ended before album capture"); return; }
                // The view and album cache survive periodic snapshots that no longer contain Album.
                if (!(minecraft.screen instanceof RivalRushScreen screen) || !screen.isAlbumView()) { ticks = 0; return; }
                if (++ticks < 20) return;
                capture(minecraft, "03-album");
                next(7);
            }
            case 7 -> {
                if (!CAPTURES.containsKey("03-album")) return;
                request("leave", "", 0);
                next(8);
            }
            case 8 -> {
                if (state.getBoolean("Active") || !originalDimension.equals(minecraft.level.dimension().location().toString())
                        || !(minecraft.screen instanceof RivalRushScreen) || state.getString("Summary").isBlank()) {
                    ticks = 0; return;
                }
                if (++ticks < 20) return; // Wait for inventory/XP packets and the actual result frame.
                if (minecraft.player.experienceLevel != originalLevel || minecraft.player.totalExperience != originalTotalXp
                        || Math.abs(minecraft.player.experienceProgress - originalProgress) > 0.000001F) {
                    finish(minecraft, false, "vanilla XP did not round-trip"); return;
                }
                if (!originalInventory.equals(minecraft.player.getInventory().save(new ListTag()))) {
                    finish(minecraft, false, "vanilla inventory did not round-trip"); return;
                }
                if (minecraft.player.position().distanceToSqr(originalPosition) > 1.0D) {
                    finish(minecraft, false, "player did not return to the original position"); return;
                }
                capture(minecraft, "04-return-result");
                next(9);
            }
            case 9 -> {
                if (!CAPTURES.containsKey("04-return-result")) return;
                if (CAPTURES.size() != 4) { finish(minecraft, false, "expected four completed PNG captures"); return; }
                next(10);
            }
            case 10 -> {
                if (!CrewHouseVisualAudit.advance(minecraft, label -> capture(minecraft, label), CAPTURES::containsKey)) return;
                if (CAPTURES.size() != 8) { finish(minecraft, false, "expected eight completed Rush and Crew PNG captures"); return; }
                next(11);
            }
            case 11 -> {
                if (!ActivityJourneyVisualAudit.advance(minecraft, label -> capture(minecraft, label), CAPTURES::containsKey)) return;
                if (CAPTURES.size() != 13) { finish(minecraft, false, "expected thirteen completed Rush, Crew and Activity PNG captures"); return; }
                finish(minecraft, true, "thirteen real-client captures; street and arena inventory/XP/dimension restored; actual Charter, facilities, job, Boombox activity, first arena choice, journey map, build and crew progress verified");
            }
            default -> finish(minecraft, false, "unknown audit phase");
        }
    }

    private static void handleWorldLoadScreen(Minecraft minecraft) {
        logWaitingScreen(minecraft);
        Screen screen = minecraft.screen;
        if (phase != 0 || screen == null || screen == confirmedWorldScreen || minecraft.getSingleplayerServer() != null) return;
        String title = translationKey(screen.getTitle());
        String buttonKey;
        if (screen.getClass() == ConfirmScreen.class && title.equals("selectWorld.backupQuestion.experimental")) {
            // ForgeHooksClient.createWorldConfirmationScreen uses precisely this title and Proceed button.
            buttonKey = "gui.proceed";
        } else if (screen.getClass() == BackupConfirmScreen.class
                && (title.equals("selectWorld.backupQuestion.experimental") || title.equals("selectWorld.backupQuestion.customized"))) {
            buttonKey = "selectWorld.backupJoinSkipButton";
        } else return;
        if (!isDisposableQuickPlayLaunch(minecraft)) {
            finish(minecraft, false, "refused a world confirmation outside the exact RivalRushAudit quick-play launch");
            return;
        }
        for (var child : screen.children()) {
            if (child instanceof Button button && button.active && button.visible && translationKey(button.getMessage()).equals(buttonKey)) {
                confirmedWorldScreen = screen;
                JetSetCraft.LOGGER.info("JETSETCRAFT_RUSH_VISUAL_AUDIT_WORLD_CONFIRMATION {} {}", title, buttonKey);
                button.onPress();
                return;
            }
        }
    }

    private static boolean isDisposableQuickPlayLaunch(Minecraft minecraft) {
        if (!Boolean.getBoolean("jetsetcraft.rushVisualAudit")
                || !Files.isRegularFile(minecraft.gameDirectory.toPath().resolve("saves/RivalRushAudit/level.dat"))) return false;
        String[] arguments = ProcessHandle.current().info().arguments().orElse(new String[0]);
        int processTarget = auditQuickPlayTarget(arguments);
        // Java @argfiles (or unavailable process metadata) can hide the expanded application arguments.
        // Java 17 exposes those in sun.java.command; the audit world name contains no whitespace.
        String command = System.getProperty("sun.java.command", "").trim();
        int commandTarget = auditQuickPlayTarget(command.isEmpty() ? new String[0] : command.split("\\s+"));
        if (processTarget < 0 || commandTarget < 0) return false;
        return processTarget == 1 || processTarget == 0 && commandTarget == 1;
    }

    private static int auditQuickPlayTarget(String[] arguments) {
        int targets = 0;
        for (int i = 0; i < arguments.length; i++) {
            if (arguments[i].equals("--quickPlaySingleplayer")) {
                if (++i >= arguments.length || !arguments[i].equals("RivalRushAudit")) return -1;
                targets++;
            } else if (arguments[i].startsWith("--quickPlaySingleplayer=")) {
                if (!arguments[i].equals("--quickPlaySingleplayer=RivalRushAudit")) return -1;
                targets++;
            }
        }
        return targets > 1 ? -1 : targets;
    }

    private static String translationKey(Component component) {
        return component.getContents() instanceof TranslatableContents contents ? contents.getKey() : "";
    }

    private static void logWaitingScreen(Minecraft minecraft) {
        Screen screen = minecraft.screen;
        StringBuilder detail = new StringBuilder(screen == null ? "<none>" : screen.getClass().getName());
        if (screen != null) {
            detail.append(" title=").append(translationKey(screen.getTitle())).append(" / ").append(screen.getTitle().getString());
            for (var child : screen.children()) {
                if (child instanceof Button button) {
                    detail.append(" | button=").append(translationKey(button.getMessage())).append(" / ")
                            .append(button.getMessage().getString()).append(" active=").append(button.active);
                }
            }
        }
        if (!detail.toString().equals(lastWaitingScreen)) {
            lastWaitingScreen = detail.toString();
            JetSetCraft.LOGGER.info("JETSETCRAFT_RUSH_VISUAL_AUDIT_WAITING_SCREEN {}", lastWaitingScreen);
        }
    }

    private static void queueVanillaFixture(Minecraft minecraft) {
        var server = minecraft.getSingleplayerServer();
        Path expected = minecraft.gameDirectory.toPath().resolve("saves").resolve("RivalRushAudit").toAbsolutePath().normalize();
        if (server == null || !server.getWorldPath(LevelResource.ROOT).toAbsolutePath().normalize().equals(expected)) {
            finish(minecraft, false, "vanilla fixture requires the disposable saves/RivalRushAudit integrated world");
            return;
        }
        UUID playerId = minecraft.player.getUUID();
        server.execute(() -> {
            try {
                // Ordinary vanilla commands run on the actual server thread and target only this client.
                // Recheck both opt-in and the exact save before adding anything to the disposable world.
                if (!Boolean.getBoolean("jetsetcraft.rushVisualAudit")
                        || !server.getWorldPath(LevelResource.ROOT).toAbsolutePath().normalize().equals(expected)) {
                    captureFailure = "vanilla fixture lost its audit-world guard";
                    return;
                }
                var player = server.getPlayerList().getPlayer(playerId);
                if (player == null) { captureFailure = "vanilla fixture player disconnected"; return; }
                var source = server.createCommandSourceStack().withPermission(4).withEntity(player).withSuppressedOutput();
                for (String command : new String[]{"give @s minecraft:diamond 7", "give @s minecraft:bread 5",
                        "experience add @s 23 levels", "experience add @s 17 points"}) {
                    if (server.getCommands().performPrefixedCommand(source, command) <= 0) {
                        captureFailure = "vanilla fixture command failed: " + command;
                        return;
                    }
                }
                fixtureApplied = true;
            } catch (RuntimeException error) {
                JetSetCraft.LOGGER.error("Rival Rush vanilla audit fixture failed", error);
                captureFailure = "vanilla fixture exception: " + error.getClass().getSimpleName();
            }
        });
    }

    private static boolean isDistrict(Minecraft minecraft) {
        return minecraft.level != null && "jetsetcraft:rival_rush".equals(minecraft.level.dimension().location().toString());
    }

    private static void request(String action, String value, int number) {
        JetSetNetwork.sendRushAction(action, value, number, RivalRushClient.runId(), RivalRushClient.state().getInt("Version"));
    }

    private static void next(int value) { phase = value; ticks = 0; }

    private static void capture(Minecraft minecraft, String label) {
        String filename = "rival-rush-" + STAMP + "-" + label + ".png";
        Path destination = minecraft.gameDirectory.toPath().resolve("screenshots").resolve(filename);
        Screenshot.grab(minecraft.gameDirectory, filename, minecraft.getMainRenderTarget(), component -> {
            try {
                if (!Files.isRegularFile(destination) || Files.size(destination) < 1024L) {
                    captureFailure = "screenshot was not written: " + label + " " + component.getString();
                    return;
                }
                try (var stream = Files.newInputStream(destination)) {
                    if (!Arrays.equals(PNG, stream.readNBytes(PNG.length))) {
                        captureFailure = "screenshot is not a PNG: " + label;
                        return;
                    }
                }
                CAPTURES.put(label, destination);
                JetSetCraft.LOGGER.info("JETSETCRAFT_RUSH_VISUAL_AUDIT_CAPTURE {} {}", label, destination.toAbsolutePath());
            } catch (IOException error) {
                captureFailure = "cannot verify screenshot " + label + ": " + error.getMessage();
            }
        });
    }

    private static void finish(Minecraft minecraft, boolean passed, String detail) {
        if (complete || failureCaptureStarted != 0L) return;
        JetSetCraft.LOGGER.info("JETSETCRAFT_RUSH_VISUAL_AUDIT_{} {}", passed ? "PASS" : "FAIL", detail);
        if (!passed) {
            logWaitingScreen(minecraft);
            failureCaptureStarted = System.nanoTime();
            failureCaptureLabel = "99-failure-phase-" + phase;
            captureFailure = null;
            try {
                capture(minecraft, failureCaptureLabel);
                return; // Keep the client alive until the real screenshot has been written and verified.
            } catch (RuntimeException error) {
                JetSetCraft.LOGGER.error("JETSETCRAFT_RUSH_VISUAL_AUDIT_FAILURE_CAPTURE_UNAVAILABLE", error);
            }
        }
        complete = true;
        stopClient(minecraft);
    }

    private static void stopClient(Minecraft minecraft) {
        if (originalTutorialStep != null) {
            minecraft.getTutorial().setStep(originalTutorialStep);
            originalTutorialStep = null;
        }
        minecraft.stop();
    }

    private RivalRushVisualAudit() {}
}
