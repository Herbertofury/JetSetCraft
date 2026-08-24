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
        if (worldTicks == 340) finish(minecraft, true, "three real-client captures completed");
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
