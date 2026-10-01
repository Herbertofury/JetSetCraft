package com.herberto.jetsetcraft.client;

import com.herberto.jetsetcraft.JetSetCraft;
import com.herberto.jetsetcraft.client.screen.GraffitiEditorScreen;
import com.herberto.jetsetcraft.client.screen.GraffitiSelectorScreen;
import net.minecraft.client.CameraType;
import net.minecraft.client.Minecraft;
import net.minecraft.client.Screenshot;
import net.minecraft.world.InteractionHand;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

/** Opt-in real-client screenshot acceptance. Enable only with -Djetsetcraft.visualAudit=true. */
@Mod.EventBusSubscriber(modid = JetSetCraft.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE, value = Dist.CLIENT)
public final class ClientVisualAudit {
    private static int globalTicks;
    private static int worldTicks;
    private static boolean complete;

    @SubscribeEvent
    public static void tick(TickEvent.ClientTickEvent event) {
        if (event.phase != TickEvent.Phase.END || complete || !Boolean.getBoolean("jetsetcraft.visualAudit")) return;
        Minecraft minecraft = Minecraft.getInstance();
        globalTicks++;
        if (minecraft.player == null || minecraft.level == null) {
            if (globalTicks > 2400) finish(minecraft, false, "world did not load");
            return;
        }
        worldTicks++;
        if (worldTicks == 40) minecraft.player.connection.sendCommand("jetsetcraft visual_audit");
        if (worldTicks == 100) {
            minecraft.options.setCameraType(CameraType.THIRD_PERSON_BACK);
            minecraft.setScreen(null);
        }
        if (worldTicks == 150) capture(minecraft, "ride-hud");
        if (worldTicks == 190) {
            minecraft.options.setCameraType(CameraType.FIRST_PERSON);
            minecraft.player.getInventory().selected = 8;
            minecraft.setScreen(new GraffitiSelectorScreen(InteractionHand.MAIN_HAND));
        }
        if (worldTicks == 235) capture(minecraft, "graffiti-selector");
        if (worldTicks == 265) minecraft.setScreen(new GraffitiEditorScreen(
                new GraffitiSelectorScreen(InteractionHand.MAIN_HAND), InteractionHand.MAIN_HAND));
        if (worldTicks == 310) capture(minecraft, "graffiti-editor");

        String wave2Provider = System.getProperty("jetsetcraft.wave2Provider", "").trim();
        if (wave2Provider.isEmpty()) {
            if (worldTicks == 340) finish(minecraft, true, "three real-client captures completed");
            return;
        }

        if (worldTicks == 340) {
            minecraft.setScreen(null);
            minecraft.options.hideGui = true;
            minecraft.options.setCameraType(CameraType.FIRST_PERSON);
            minecraft.player.setYRot(180.0F);
            minecraft.player.setXRot(4.0F);
            minecraft.player.connection.sendCommand("jetsetcraft visual_audit_wave2 "
                    + ("all".equals(wave2Provider) ? "alexsmobs" : wave2Provider));
        }

        if ("all".equals(wave2Provider)) {
            if (worldTicks == 400) capture(minecraft, "wave2-alexsmobs-rigs");
            if (worldTicks == 420) minecraft.player.connection.sendCommand("jetsetcraft visual_audit_wave2 alexscaves");
            if (worldTicks == 480) capture(minecraft, "wave2-alexscaves-rigs");
            if (worldTicks == 500) minecraft.player.connection.sendCommand("jetsetcraft visual_audit_wave2 cataclysm");
            if (worldTicks == 560) capture(minecraft, "wave2-cataclysm-rigs");
            if (worldTicks == 590) finish(minecraft, true,
                    "six real-client captures completed including all Wave 2 provider rig scenes");
        } else {
            if (worldTicks == 400) capture(minecraft, "wave2-" + wave2Provider + "-rigs");
            if (worldTicks == 430) finish(minecraft, true,
                    "four real-client captures completed including Wave 2 provider rig scene");
        }
    }

    private static void capture(Minecraft minecraft, String label) {
        Screenshot.grab(minecraft.gameDirectory, minecraft.getMainRenderTarget(), component ->
                JetSetCraft.LOGGER.info("JETSETCRAFT_VISUAL_AUDIT_CAPTURE {} {}", label, component.getString()));
    }

    private static void finish(Minecraft minecraft, boolean passed, String detail) {
        complete = true;
        JetSetCraft.LOGGER.info("JETSETCRAFT_VISUAL_AUDIT_{} {}", passed ? "PASS" : "FAIL", detail);
        minecraft.stop();
    }

    private ClientVisualAudit() { }
}
