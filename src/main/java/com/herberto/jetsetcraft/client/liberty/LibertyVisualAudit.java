package com.herberto.jetsetcraft.client.liberty;

import com.herberto.jetsetcraft.JetSetCraft;
import com.herberto.jetsetcraft.client.activity.ActivityClient;
import com.herberto.jetsetcraft.network.JetSetNetwork;
import net.minecraft.client.CameraType;
import net.minecraft.client.Minecraft;
import net.minecraft.client.Screenshot;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.BackupConfirmScreen;
import net.minecraft.client.gui.screens.ConfirmScreen;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.tutorial.TutorialSteps;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.contents.TranslatableContents;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.storage.LevelResource;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.lwjgl.glfw.GLFW;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Opt-in native UI acceptance against only the disposable saves/LibertyGrindAudit world.
 * Uses production activity/liberty packets and real buttons. A logged vanilla setup fixture adds
 * return sentinels; no mission objective, credit, sample, research or completion is fabricated.
 * Provider-absent runs prove the dependency state. Provider-present runs additionally observe the
 * actual pod, loaded TaCZ weapon/HUD and mission screen, then prove an ordinary early return.
 */
@Mod.EventBusSubscriber(modid = JetSetCraft.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE, value = Dist.CLIENT)
public final class LibertyVisualAudit {
    private static final String WORLD = "LibertyGrindAudit", STAMP = Long.toString(System.currentTimeMillis());
    private static final byte[] PNG = {(byte) 137, 80, 78, 71, 13, 10, 26, 10};
    private static final Map<String, Path> CAPTURES = new ConcurrentHashMap<>();
    private static long started, failureStarted, fixtureObservedAt;
    private static int phase, ticks, originalScale, originalWidth, originalHeight, fixtureSettledTicks;
    private static boolean configured, queuedFixture, complete, provider;
    private static volatile boolean fixtureReady;
    private static volatile String failure;
    private static Screen confirmedScreen;
    private static TutorialSteps originalTutorial;
    private static CameraType originalCamera;
    private static String originalDimension;
    private static ListTag originalInventory;
    private static int originalLevel, originalTotal;
    private static float originalProgress;
    private static int pointerPark;
    private static float originalHudX, originalHudY, originalHudScale;
    private static boolean originalHudVisible;

    @SubscribeEvent public static void tick(TickEvent.ClientTickEvent event) {
        if (event.phase != TickEvent.Phase.END || complete || !Boolean.getBoolean("jetsetcraft.libertyVisualAudit")) return;
        Minecraft minecraft = Minecraft.getInstance();
        if (failureStarted != 0) {
            if (CAPTURES.containsKey("99-failure") || System.nanoTime() - failureStarted > 10_000_000_000L) stop(minecraft);
            return;
        }
        if (started == 0) started = System.nanoTime();
        if (System.nanoTime() - started > 300_000_000_000L) { finish(minecraft, false, "300-second deadline in phase " + phase); return; }
        if (failure != null) { finish(minecraft, false, failure); return; }
        try {
            if (!configured) {
                require(isDisposableLaunch(minecraft), "Audit requires explicit LibertyGrindAudit quick-play launch and an existing disposable save");
                originalScale = minecraft.options.guiScale().get(); originalCamera = minecraft.options.getCameraType();
                originalWidth = minecraft.getWindow().getScreenWidth(); originalHeight = minecraft.getWindow().getScreenHeight();
                originalTutorial = minecraft.options.tutorialStep; minecraft.getTutorial().setStep(TutorialSteps.NONE);
                var hud = LibertyHudPreferences.get();
                originalHudX = hud.x; originalHudY = hud.y; originalHudScale = hud.scale; originalHudVisible = hud.visible;
                hud.reset(); // Verify the shipping default, then restore the local preference on every normal audit exit.
                viewport(minecraft, 1280, 720, 2); configured = true;
            }
            if (minecraft.player == null || minecraft.level == null || minecraft.getConnection() == null) { worldConfirmation(minecraft); return; }
            require(disposableServer(minecraft), "Audit lost its exact disposable integrated save");
            if (minecraft.screen != null) {
                // A fixed native position need not deliver a new mouse event after a window resize.
                // Alternate nearby pixels, and never warp a grabbed gameplay mouse (that rotates the camera).
                GLFW.glfwFocusWindow(minecraft.getWindow().getWindow());
                int point = 1 + (pointerPark++ & 1);
                GLFW.glfwSetCursorPos(minecraft.getWindow().getWindow(), point, point);
            }
            advance(minecraft);
        } catch (RuntimeException error) {
            JetSetCraft.LOGGER.error("Liberty native UI audit failed", error);
            finish(minecraft, false, "phase " + phase + ": " + error.getMessage());
        }
    }

    private static void advance(Minecraft minecraft) {
        CompoundTag state = LibertyClient.stateSnapshot();
        switch (phase) {
            case 0 -> {
                if (++ticks < 40) return;
                if (!queuedFixture) { queuedFixture = true; fixture(minecraft); return; }
                if (!fixtureReady || minecraft.player.getInventory().countItem(Items.DIAMOND) < 7) return;
                if (fixtureObservedAt == 0) fixtureObservedAt = System.nanoTime();
                // Let the genuine recipe/advancement toasts and vanilla chat fade; never suppress them.
                if (++fixtureSettledTicks < 240 || System.nanoTime() - fixtureObservedAt < 12_000_000_000L) return;
                JetSetNetwork.sendActivityAction("open", "", null, 0); next(1);
            }
            case 1 -> {
                if (!ActivityClient.isScreenOpen() || ++ticks < 12) return;
                clickContains(minecraft, "Liberty Grind"); next(2);
            }
            case 2 -> {
                CompoundTag activity = ActivityClient.stateSnapshot();
                if (!activity.getString("SelectedMode").equals("liberty_grind") || !activity.getBoolean("CanLaunch") || ++ticks < 12) return;
                click(minecraft, "Open war map"); next(3);
            }
            case 3 -> {
                if (!LibertyClient.isScreenOpen() || state.getList("Theaters", Tag.TAG_COMPOUND).size() != 3 || ++ticks < 20) return;
                capture(minecraft, "01-war-map-default"); next(4);
            }
            case 4 -> {
                if (!captured("01-war-map-default")) return;
                click(minecraft, "Neon Docks"); next(5);
            }
            case 5 -> {
                if (++ticks < 20) return;
                capture(minecraft, "02-district-default"); next(6);
            }
            case 6 -> {
                if (!captured("02-district-default")) return;
                click(minecraft, "Research"); next(7);
            }
            case 7 -> {
                require(state.getCompound("Research").getList("Projects", Tag.TAG_COMPOUND).size() == 6, "Actual research catalogue must contain six projects");
                if (++ticks < 20) return;
                capture(minecraft, "03-research-default"); next(8);
            }
            case 8 -> {
                if (!captured("03-research-default")) return;
                clickContains(minecraft, firstProject()); next(9);
            }
            case 9 -> {
                if (++ticks < 20) return;
                capture(minecraft, "04-project-default"); next(10);
            }
            case 10 -> {
                if (!captured("04-project-default")) return;
                click(minecraft, "HUD"); next(11);
            }
            case 11 -> {
                require(minecraft.screen instanceof LibertyHudScreen, "HUD editor did not open");
                if (++ticks < 20) return;
                capture(minecraft, "05-hud-default"); next(12);
            }
            case 12 -> {
                if (!captured("05-hud-default")) return;
                click(minecraft, "Done"); viewport(minecraft, 960, 720, 3); next(13);
            }
            case 13 -> {
                if (minecraft.getWindow().getGuiScaledWidth() != 320 || minecraft.getWindow().getGuiScaledHeight() != 240 || ++ticks < 20) return;
                click(minecraft, "War map"); next(131);
            }
            case 131 -> {
                if (++ticks < 20) return;
                capture(minecraft, "06-war-map-minimum"); next(14);
            }
            case 14 -> {
                if (!captured("06-war-map-minimum")) return;
                click(minecraft, "Research"); next(15);
            }
            case 15 -> {
                if (++ticks < 20) return;
                capture(minecraft, "07-research-minimum"); next(16);
            }
            case 16 -> {
                if (!captured("07-research-minimum")) return;
                clickContains(minecraft, firstProject()); next(17);
            }
            case 17 -> {
                if (++ticks < 20) return;
                capture(minecraft, "08-project-minimum"); next(18);
            }
            case 18 -> {
                if (!captured("08-project-minimum")) return;
                minecraft.screen.keyPressed(GLFW.GLFW_KEY_END, 0, 0); next(19);
            }
            case 19 -> {
                if (++ticks < 12) return;
                capture(minecraft, "09-project-scroll-minimum"); next(20);
            }
            case 20 -> {
                if (!captured("09-project-scroll-minimum")) return;
                click(minecraft, "HUD"); next(21);
            }
            case 21 -> {
                if (++ticks < 20) return;
                capture(minecraft, "10-hud-minimum"); next(22);
            }
            case 22 -> {
                if (!captured("10-hud-minimum")) return;
                click(minecraft, "Done"); click(minecraft, "War map"); click(minecraft, "Neon Docks"); next(23);
            }
            case 23 -> {
                if (++ticks < 20) return;
                provider = state.getList("Theaters", Tag.TAG_COMPOUND).getCompound(0).getBoolean("Available");
                require(button(minecraft, "Deploy in pod").active == provider, "Deploy widget disagrees with server availability");
                capture(minecraft, "11-district-minimum"); next(24);
            }
            case 24 -> {
                if (!captured("11-district-minimum")) return;
                if (!provider) { finish(minecraft, true, "11 native captures; actual activity handoff, dependency-blocked deployment, six-project research and user HUD at default and 320x240 logical size; no simulated mission state"); return; }
                originalDimension = minecraft.level.dimension().location().toString();
                originalInventory = minecraft.player.getInventory().save(new ListTag()).copy();
                originalLevel = minecraft.player.experienceLevel; originalTotal = minecraft.player.totalExperience; originalProgress = minecraft.player.experienceProgress;
                minecraft.options.setCameraType(CameraType.THIRD_PERSON_BACK);
                click(minecraft, "Deploy in pod"); next(25);
            }
            case 25 -> {
                require(!state.getBoolean("Active") || state.getString("Phase").equals("DROPPING"),
                        "Pod drop ended before its current vehicle had a genuine rendered frame");
                if (!state.getBoolean("Active") || !LibertyClient.active() || minecraft.player.getVehicle() == null) return;
                if (minecraft.screen != null) { ticks = 0; return; } // A terrain loading screen is not visual proof of a pod.
                minecraft.player.getInventory().selected = 4; // Actual field radio: TaCZ may force first-person while holding a gun.
                require(minecraft.player.getMainHandItem().is(Items.COMPASS), "Pod camera did not select the actual field radio");
                minecraft.options.setCameraType(CameraType.THIRD_PERSON_FRONT);
                minecraft.player.setYRot(0); minecraft.player.yRotO = 0;
                minecraft.player.setXRot(0); minecraft.player.xRotO = 0;
                minecraft.player.setYHeadRot(0); minecraft.player.yHeadRotO = 0;
                if (++ticks < 5) return;
                var vehicle = minecraft.player.getVehicle();
                var riderPos = minecraft.player.position(); var vehiclePos = vehicle.position();
                require(Double.isFinite(riderPos.x) && Double.isFinite(riderPos.y) && Double.isFinite(riderPos.z)
                        && Double.isFinite(vehiclePos.x) && Double.isFinite(vehiclePos.y) && Double.isFinite(vehiclePos.z),
                        "Non-finite native transport or rider position");
                long renderCalls = com.herberto.jetsetcraft.client.render.LibertyTransportRenderer.auditRenderCalls(vehicle.getId());
                boolean compiled = minecraft.levelRenderer.isChunkCompiled(vehicle.blockPosition());
                if (ticks == 5 || compiled && renderCalls > 0) JetSetCraft.LOGGER.info("JETSETCRAFT_LIBERTY_VISUAL_AUDIT_TRANSPORT type={} class={} renderer={} player={} vehicle={} ticks={} chunk={} compiled={} riderCompiled={} old=({},{},{}) removed={} controlledLocal={} invisible={} renderCalls={} bounds={}",
                        net.minecraft.core.registries.BuiltInRegistries.ENTITY_TYPE.getKey(vehicle.getType()), vehicle.getClass().getName(),
                        minecraft.getEntityRenderDispatcher().getRenderer(vehicle).getClass().getName(), riderPos, vehiclePos,
                        vehicle.tickCount, minecraft.level.hasChunkAt(vehicle.blockPosition()), compiled,
                        minecraft.levelRenderer.isChunkCompiled(minecraft.player.blockPosition()), vehicle.xOld, vehicle.yOld, vehicle.zOld, vehicle.isRemoved(),
                        vehicle.isControlledByLocalInstance(), vehicle.isInvisible(),
                        renderCalls, vehicle.getBoundingBox());
                if (!compiled || renderCalls == 0) return;
                capture(minecraft, "12-live-pod"); next(26);
            }
            case 26 -> {
                if (!captured("12-live-pod") || !state.getString("Phase").equals("ACTIVE")) return;
                require(!minecraft.player.isPassenger(), "Player is still attached to discarded drop pod");
                minecraft.player.getInventory().selected = 0;
                minecraft.options.setCameraType(CameraType.FIRST_PERSON); next(27);
            }
            case 27 -> {
                if (++ticks < 240) return; // New loan armor legitimately earns recipe/advancement toasts; let them finish.
                require(minecraft.player.getMainHandItem().hasTag()
                        && minecraft.player.getMainHandItem().getTag().getString("GunId").equals("helldiver2:ar23_liberator"), "Native mission did not lend the verified Liberator");
                capture(minecraft, "13-live-gun-hud"); next(28);
            }
            case 28 -> {
                if (!captured("13-live-gun-hud")) return;
                minecraft.player.getInventory().selected = 4;
                require(minecraft.gameMode != null && minecraft.player.getMainHandItem().is(Items.COMPASS)
                        && minecraft.player.getMainHandItem().hasTag()
                        && minecraft.player.getMainHandItem().getTag().getBoolean("LibertyFieldRadio"), "Missing actual field-radio control");
                minecraft.gameMode.useItem(minecraft.player, net.minecraft.world.InteractionHand.MAIN_HAND);
                next(29);
            }
            case 29 -> {
                if (!LibertyClient.isScreenOpen() || ++ticks < 20) return;
                require(state.getList("Objectives", Tag.TAG_COMPOUND).size() == 3 && state.getBoolean("CanAbort"), "Actual mission objectives / return action missing");
                capture(minecraft, "14-live-mission-minimum"); next(30);
            }
            case 30 -> {
                if (!captured("14-live-mission-minimum")) return;
                click(minecraft, "Return early");
                require(minecraft.screen instanceof ConfirmScreen, "Early return requires the ordinary confirmation screen");
                clickTranslation(minecraft, "gui.yes"); next(31);
            }
            case 31 -> {
                if (state.getBoolean("Active") || !minecraft.level.dimension().location().toString().equals(originalDimension) || ++ticks < 20) return;
                require(minecraft.player.getInventory().save(new ListTag()).equals(originalInventory), "Native early return changed original inventory");
                require(minecraft.player.experienceLevel == originalLevel && minecraft.player.totalExperience == originalTotal
                        && Math.abs(minecraft.player.experienceProgress - originalProgress) < .0001F, "Native early return changed XP");
                capture(minecraft, "15-returned-war-map"); next(32);
            }
            case 32 -> {
                if (!captured("15-returned-war-map")) return;
                finish(minecraft, true, "15 native captures; actual activity handoff, default and 320x240 research/HUD, live TaCZ pod/weapon/mission, ordinary early return restored original inventory/XP/dimension; no objective, reward or extraction completion fixture");
            }
            default -> throw new IllegalStateException("Unknown audit phase " + phase);
        }
    }

    private static void fixture(Minecraft minecraft) {
        require(disposableServer(minecraft), "Vanilla fixture requires the exact disposable integrated save");
        var server = minecraft.getSingleplayerServer(); UUID owner = minecraft.player.getUUID();
        server.execute(() -> {
            try {
                require(Boolean.getBoolean("jetsetcraft.libertyVisualAudit") && server.getWorldPath(LevelResource.ROOT).toAbsolutePath().normalize().equals(expected(minecraft)), "Fixture lost its explicit save guard");
                var player = server.getPlayerList().getPlayer(owner); require(player != null, "Fixture player disconnected");
                var source = server.createCommandSourceStack().withPermission(4).withEntity(player).withSuppressedOutput();
                if (server.getWorldData().getDifficulty() != net.minecraft.world.Difficulty.NORMAL)
                    server.getCommands().performPrefixedCommand(source, "difficulty normal");
                require(server.getWorldData().getDifficulty() == net.minecraft.world.Difficulty.NORMAL
                        && player.serverLevel().getDifficulty() == net.minecraft.world.Difficulty.NORMAL,
                        "Could not configure disposable difficulty");
                if (!player.isCreative()) server.getCommands().performPrefixedCommand(source, "gamemode creative @s");
                require(player.isCreative(), "Could not configure disposable creative origin");
                for (String command : new String[]{"give @s minecraft:diamond 7", "experience add @s 17 points"})
                    require(server.getCommands().performPrefixedCommand(source, command) > 0, "Vanilla audit setup command failed: " + command);
                JetSetCraft.LOGGER.info("JETSETCRAFT_LIBERTY_VISUAL_AUDIT_FIXTURE vanilla difficulty/creative/sentinel inventory and XP only; no mission or research awards");
                fixtureReady = true;
            } catch (RuntimeException error) { failure = "Vanilla fixture: " + error.getMessage(); }
        });
    }
    private static Path expected(Minecraft minecraft) { return minecraft.gameDirectory.toPath().resolve("saves").resolve(WORLD).toAbsolutePath().normalize(); }
    private static boolean disposableServer(Minecraft minecraft) {
        var server = minecraft.getSingleplayerServer();
        return server != null && server.getWorldPath(LevelResource.ROOT).toAbsolutePath().normalize().equals(expected(minecraft));
    }
    private static boolean isDisposableLaunch(Minecraft minecraft) {
        if (!Files.isRegularFile(expected(minecraft).resolve("level.dat"))) return false;
        String[] arguments = ProcessHandle.current().info().arguments().orElse(new String[0]);
        int process = target(arguments), command = target(System.getProperty("sun.java.command", "").trim().split("\\s+"));
        return process >= 0 && command >= 0 && (process == 1 || process == 0 && command == 1);
    }
    private static int target(String[] arguments) {
        int matches = 0;
        for (int i = 0; i < arguments.length; i++) {
            if (arguments[i].equals("--quickPlaySingleplayer")) { if (++i >= arguments.length || !arguments[i].equals(WORLD)) return -1; matches++; }
            else if (arguments[i].startsWith("--quickPlaySingleplayer=")) { if (!arguments[i].equals("--quickPlaySingleplayer=" + WORLD)) return -1; matches++; }
        }
        return matches > 1 ? -1 : matches;
    }
    private static void worldConfirmation(Minecraft minecraft) {
        Screen screen = minecraft.screen;
        if (phase != 0 || screen == null || screen == confirmedScreen || minecraft.getSingleplayerServer() != null) return;
        String title = translation(screen.getTitle()), key;
        if (screen.getClass() == ConfirmScreen.class && title.equals("selectWorld.backupQuestion.experimental")) key = "gui.proceed";
        else if (screen.getClass() == BackupConfirmScreen.class && (title.equals("selectWorld.backupQuestion.experimental") || title.equals("selectWorld.backupQuestion.customized"))) key = "selectWorld.backupJoinSkipButton";
        else return;
        require(isDisposableLaunch(minecraft), "Refused confirmation outside the named disposable audit world");
        confirmedScreen = screen; clickTranslation(minecraft, key);
    }
    private static String firstProject() { return LibertyClient.research().getList("Projects", Tag.TAG_COMPOUND).getCompound(0).getString("Name"); }
    private static String translation(Component text) { return text.getContents() instanceof TranslatableContents value ? value.getKey() : ""; }
    private static Button button(Minecraft minecraft, String label) {
        require(minecraft.screen != null, "No screen for button " + label);
        for (var child : minecraft.screen.children()) if (child instanceof Button button && button.visible && button.getMessage().getString().equals(label)) return button;
        throw new IllegalStateException("Missing real button: " + label);
    }
    private static void click(Minecraft minecraft, String label) { Button button = button(minecraft, label); require(button.active, "Disabled real button: " + label); button.onPress(); }
    private static void clickContains(Minecraft minecraft, String label) {
        require(minecraft.screen != null, "No screen for choice " + label);
        for (var child : minecraft.screen.children()) if (child instanceof Button button && button.visible && button.active && button.getMessage().getString().contains(label)) { button.onPress(); return; }
        throw new IllegalStateException("Missing real choice: " + label);
    }
    private static void clickTranslation(Minecraft minecraft, String key) {
        require(minecraft.screen != null, "No screen for translated button " + key);
        for (var child : minecraft.screen.children()) if (child instanceof Button button && button.visible && button.active && translation(button.getMessage()).equals(key)) { button.onPress(); return; }
        throw new IllegalStateException("Missing translated button: " + key);
    }
    private static void viewport(Minecraft minecraft, int width, int height, int scale) {
        minecraft.getWindow().setWindowed(width, height); minecraft.options.guiScale().set(scale); minecraft.resizeDisplay();
    }
    private static void next(int value) { phase = value; ticks = 0; }
    private static boolean captured(String label) { return CAPTURES.containsKey(label); }
    private static void capture(Minecraft minecraft, String label) {
        String filename = "liberty-grind-" + STAMP + "-" + label + ".png";
        Path path = minecraft.gameDirectory.toPath().resolve("screenshots").resolve(filename);
        Screenshot.grab(minecraft.gameDirectory, filename, minecraft.getMainRenderTarget(), response -> {
            try {
                require(Files.isRegularFile(path) && Files.size(path) > 1024, "Screenshot not written: " + label);
                try (var bytes = Files.newInputStream(path)) { require(Arrays.equals(PNG, bytes.readNBytes(PNG.length)), "Invalid screenshot PNG: " + label); }
                CAPTURES.put(label, path);
                JetSetCraft.LOGGER.info("JETSETCRAFT_LIBERTY_VISUAL_AUDIT_CAPTURE {} {}", label, path.toAbsolutePath());
            } catch (IOException | RuntimeException error) { failure = "Capture: " + error.getMessage(); }
        });
    }
    private static void finish(Minecraft minecraft, boolean passed, String message) {
        if (complete || failureStarted != 0) return;
        JetSetCraft.LOGGER.info("JETSETCRAFT_LIBERTY_VISUAL_AUDIT_{} {}", passed ? "PASS" : "FAIL", message);
        if (!passed) {
            failureStarted = System.nanoTime();
            if (minecraft.screen != null) JetSetCraft.LOGGER.error("JETSETCRAFT_LIBERTY_VISUAL_AUDIT_SCREEN {} {}", minecraft.screen.getClass().getName(), minecraft.screen.getTitle().getString());
            capture(minecraft, "99-failure"); return;
        }
        stop(minecraft);
    }
    private static void stop(Minecraft minecraft) {
        complete = true;
        if (configured) {
            minecraft.options.guiScale().set(originalScale); minecraft.options.setCameraType(originalCamera);
            minecraft.getWindow().setWindowed(originalWidth, originalHeight); minecraft.resizeDisplay();
            minecraft.getTutorial().setStep(originalTutorial);
            var hud = LibertyHudPreferences.get(); hud.x = originalHudX; hud.y = originalHudY;
            hud.scale = originalHudScale; hud.visible = originalHudVisible; hud.save();
        }
        minecraft.stop();
    }
    private static void require(boolean condition, String message) { if (!condition) throw new IllegalStateException(message); }
    private LibertyVisualAudit() {}
}
