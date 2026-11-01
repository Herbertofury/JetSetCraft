package com.herberto.jetsetcraft.client.rush;

import com.herberto.jetsetcraft.JetSetCraft;
import com.herberto.jetsetcraft.network.JetSetNetwork;
import net.minecraft.client.CameraType;
import net.minecraft.client.Minecraft;
import net.minecraft.client.Screenshot;
import net.minecraft.client.gui.components.Button;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
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
    private static int phase, ticks;
    private static int chosenVersion = Integer.MIN_VALUE;
    private static UUID chosenRun;
    private static boolean waitingChoice, albumObserved, complete;
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
        if (started == 0L) started = System.nanoTime();
        if (System.nanoTime() - started > DEADLINE_NANOS) {
            finish(minecraft, false, "180-second deadline exceeded in phase " + phase);
            return;
        }
        if (captureFailure != null) { finish(minecraft, false, captureFailure); return; }
        if (minecraft.player == null || minecraft.level == null || minecraft.getConnection() == null) return;
        try {
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
            if (phase == 6) { albumObserved = false; request("album", "", 0); }
        }
        switch (phase) {
            case 0 -> {
                if (++ticks < 60) return; // Let ordinary spawn/connection/inventory synchronization settle.
                originalDimension = minecraft.level.dimension().location().toString();
                if (isDistrict(minecraft)) { finish(minecraft, false, "audit must start in normal Minecraft"); return; }
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
                albumObserved |= state.contains("Album", Tag.TAG_LIST);
                if (!state.getBoolean("Active") || !isDistrict(minecraft)) { finish(minecraft, false, "run ended before album capture"); return; }
                if (!albumObserved || !(minecraft.screen instanceof RivalRushScreen)) { ticks = 0; return; }
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
                finish(minecraft, true, "four real-client captures; actual packet entry/exit; dimension, inventory and vanilla XP restored");
            }
            default -> finish(minecraft, false, "unknown audit phase");
        }
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
        if (complete) return;
        complete = true;
        JetSetCraft.LOGGER.info("JETSETCRAFT_RUSH_VISUAL_AUDIT_{} {}", passed ? "PASS" : "FAIL", detail);
        minecraft.stop();
    }

    private RivalRushVisualAudit() {}
}
