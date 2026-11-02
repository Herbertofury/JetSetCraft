package com.herberto.jetsetcraft.client.liberty;

import com.herberto.jetsetcraft.JetSetCraft;
import com.herberto.jetsetcraft.network.JetSetNetwork;
import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.ClientPlayerNetworkEvent;
import net.minecraftforge.client.event.RegisterKeyMappingsEvent;
import net.minecraftforge.client.event.RenderGuiOverlayEvent;
import net.minecraftforge.client.gui.overlay.VanillaGuiOverlay;
import net.minecraftforge.client.settings.KeyConflictContext;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.lwjgl.glfw.GLFW;

import java.util.Locale;
import java.util.UUID;

/** Presentation only: every clock, objective, affordability check and extraction stage comes from the server. */
public final class LibertyClient {
    public static final KeyMapping MENU = new KeyMapping("Liberty Grind war map", KeyConflictContext.IN_GAME,
            InputConstants.Type.KEYSYM, GLFW.GLFW_KEY_UNKNOWN, "key.categories.jetsetcraft");
    static final int HUD_WIDTH = 204, HUD_HEIGHT = 98;
    private static CompoundTag state = new CompoundTag();

    public static void accept(CompoundTag incoming) {
        CompoundTag previous = state;
        if(incoming!=null&&incoming.getBoolean("Partial")) {
            state=previous.copy(); for(String key:incoming.getAllKeys())state.put(key,incoming.get(key).copy());
        } else state = incoming == null ? new CompoundTag() : incoming.copy();
        state.put("Theaters", bounded(state.getList("Theaters", Tag.TAG_COMPOUND), 12));
        state.put("Objectives", bounded(state.getList("Objectives", Tag.TAG_COMPOUND), 64));
        CompoundTag research = state.getCompound("Research");
        research.put("Projects", bounded(research.getList("Projects", Tag.TAG_COMPOUND), 32));
        state.put("Research", research);
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.player == null) return;
        if (minecraft.screen instanceof LibertyCampaignScreen screen) screen.onSnapshot(state,previous);
        else if (minecraft.screen instanceof LibertyScreen screen) screen.onSnapshot(state, previous);
        else if (state.getBoolean("Open")) minecraft.setScreen(new LibertyCampaignScreen());
    }

    private static ListTag bounded(ListTag source, int limit) {
        ListTag result = new ListTag();
        for (int i = 0; i < Math.min(limit, source.size()); i++) result.add(source.getCompound(i).copy());
        return result;
    }

    static CompoundTag state() { return state; }
    static ListTag theaters() { return state.getList("Theaters", Tag.TAG_COMPOUND); }
    static ListTag objectives() { return state.getList("Objectives", Tag.TAG_COMPOUND); }
    static CompoundTag research() { return state.getCompound("Research"); }
    public static CompoundTag stateSnapshot() { return state.copy(); }
    public static boolean isScreenOpen() { return Minecraft.getInstance().screen instanceof LibertyScreen || Minecraft.getInstance().screen instanceof LibertyCampaignScreen; }
    public static boolean active() {
        Minecraft minecraft = Minecraft.getInstance();
        return state.getBoolean("Active") && minecraft.player != null && minecraft.level != null
                && minecraft.getConnection() != null
                && (state.getBoolean("HomeDefense") || minecraft.level.dimension().location().toString().equals("jetsetcraft:liberty_grind"));
    }

    public static void request(String action, String value) {
        if (Minecraft.getInstance().getConnection() == null) return;
        UUID mission = state.hasUUID("Mission") ? state.getUUID("Mission") : null;
        long revision = action.equals("research") || action.equals("research_select")
                ? research().getLong("Revision") : action.startsWith("ship_") ? state.getCompound("Ship").getLong("Revision")
                : action.equals("armory_select") ? state.getCompound("Armory").getLong("Revision") : state.getLong("Revision");
        JetSetNetwork.sendLibertyAction(action, value == null ? "" : value, mission, Math.max(0, revision));
    }

    static String clipped(String text, int pixels) {
        var font = Minecraft.getInstance().font;
        return font.width(text) <= pixels ? text
                : font.plainSubstrByWidth(text, Math.max(0, pixels - font.width("…"))) + "…";
    }
    static String clock(int seconds) {
        int value = Math.max(0, seconds);
        return String.format(Locale.ROOT, "%d:%02d", value / 60, value % 60);
    }
    static String samples(CompoundTag source, boolean wallet) {
        String common = wallet ? "Common" : "CommonSamples", rare = wallet ? "Rare" : "RareSamples", special = wallet ? "Super" : "SuperSamples";
        return Math.max(0, source.getInt(common)) + " common · " + Math.max(0, source.getInt(rare))
                + " rare · " + Math.max(0, source.getInt(special)) + " super";
    }

    static float hudScale(int width, int height) {
        return Math.max(0.2F, Math.min(LibertyHudPreferences.get().scale,
                Math.min((float) Math.max(1, width - 8) / HUD_WIDTH, (float) Math.max(1, height - 8) / HUD_HEIGHT)));
    }

    static void drawHud(GuiGraphics gui, int width, int height, boolean preview) {
        var preferences = LibertyHudPreferences.get();
        if (!preview && (!active() || !preferences.visible)) return;
        float scale = hudScale(width, height);
        int x = Math.round(preferences.x * Math.max(0, width - HUD_WIDTH * scale));
        int y = Math.round(preferences.y * Math.max(0, height - HUD_HEIGHT * scale));
        gui.pose().pushPose();
        gui.pose().translate(x, y, 0); gui.pose().scale(scale, scale, 1);
        gui.fill(0, 0, HUD_WIDTH, HUD_HEIGHT, 0xDB181C20);
        gui.fill(0, 0, 2, HUD_HEIGHT, 0xFFE5C35E);
        var font = Minecraft.getInstance().font;
        gui.drawString(font, "Liberty Grind", 7, 6, 0xFFE5C35E, true);
        String timer = active() ? clock(state.getInt("Seconds")) : "Mission clock";
        gui.drawString(font, timer, HUD_WIDTH - 7 - font.width(timer), 6, 0xFFFFFFFF, true);
        String phase = active() ? state.getString("Phase").replace('_', ' ') : "Mission stage";
        if (active() && state.getString("Phase").equals("EXTRACTING")) phase += " · " + Math.max(0, state.getInt("ExtractionSeconds")) + "s";
        gui.drawString(font, clipped(phase, HUD_WIDTH - 14), 7, 20, 0xFF87E1EA, true);
        CompoundTag next = new CompoundTag();
        for (var raw : objectives()) if (raw instanceof CompoundTag row && !row.getBoolean("Complete")
                && (!row.contains("Active") || row.getBoolean("Active"))) {
            int rank=row.getBoolean("Priority")?0:row.getBoolean("Optional")?2:1;
            int previousRank=next.getBoolean("Priority")?0:next.getBoolean("Optional")?2:1;
            if(next.isEmpty()||rank<previousRank||rank==previousRank&&row.getDouble("Distance")<next.getDouble("Distance"))next=row;
        }
        String objective = !active() ? "Next objective · progress · distance" : next.isEmpty()
                ? state.getString("Status") : next.getString("Name") + " " + Math.max(0, next.getInt("Progress"))
                        + "/" + Math.max(1, next.getInt("Goal"));
        gui.drawString(font, clipped(objective, HUD_WIDTH - 14), 7, 34, 0xFFE7E7E7, true);
        String navigation = "Open the war map for mission details";
        if (active() && !next.isEmpty() && next.contains("Distance")) {
            navigation = distance(next.getDouble("Distance")) + "m";
            if (next.contains("X") && next.contains("Z")) navigation += " · X " + next.getInt("X") + " / Z " + next.getInt("Z");
        } else if (active() && state.contains("ExtractionDistance")) {
            navigation = "Extraction · " + distance(state.getDouble("ExtractionDistance")) + "m";
            if (state.contains("ExtractionX") && state.contains("ExtractionZ")) navigation += " · " + state.getInt("ExtractionX") + ", " + state.getInt("ExtractionZ");
        }
        gui.drawString(font, clipped(navigation, HUD_WIDTH - 14), 7, 47, 0xFFB9CDD0, true);
        gui.drawString(font, active() ? Math.max(0, state.getInt("Credits")) + " super credits recovered" : "Recovered super credits", 7, 61, 0xFFE5C35E, true);
        gui.drawString(font, clipped(active() ? samples(state, false) : "Common · rare · super samples", HUD_WIDTH - 14), 7, 74, 0xFFD7B7FF, true);
        gui.drawString(font, "/jetsetcraft liberty menu", 7, 87, 0xFFBBBBBB, true);
        gui.pose().popPose();
    }

    static int distance(double value) { return Double.isFinite(value) ? (int) Math.min(999999, Math.ceil(Math.max(0, value))) : 0; }

    @Mod.EventBusSubscriber(modid = JetSetCraft.MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
    public static final class ModEvents {
        @SubscribeEvent public static void keys(RegisterKeyMappingsEvent event) { event.register(MENU); }
    }

    @Mod.EventBusSubscriber(modid = JetSetCraft.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE, value = Dist.CLIENT)
    public static final class ForgeEvents {
        @SubscribeEvent public static void tick(TickEvent.ClientTickEvent event) {
            if (event.phase != TickEvent.Phase.END) return;
            while (MENU.consumeClick()) if (Minecraft.getInstance().player != null && Minecraft.getInstance().screen == null) request("open", "");
        }
        @SubscribeEvent public static void overlay(RenderGuiOverlayEvent.Post event) {
            Minecraft minecraft = Minecraft.getInstance();
            if (event.getOverlay() != VanillaGuiOverlay.HOTBAR.type() || minecraft.player == null || minecraft.options.hideGui
                    || minecraft.screen instanceof LibertyScreen || minecraft.screen instanceof LibertyCampaignScreen || minecraft.screen instanceof LibertyHudScreen) return;
            drawHud(event.getGuiGraphics(), minecraft.getWindow().getGuiScaledWidth(), minecraft.getWindow().getGuiScaledHeight(), false);
        }
        @SubscribeEvent public static void logout(ClientPlayerNetworkEvent.LoggingOut event) { state = new CompoundTag(); }
    }
    private LibertyClient() {}
}
