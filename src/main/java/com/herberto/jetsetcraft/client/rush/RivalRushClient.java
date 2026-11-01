package com.herberto.jetsetcraft.client.rush;

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

/** Client presentation only: no local XP, reward, damage or elapsed-time authority. */
public final class RivalRushClient {
    public static final KeyMapping MENU = new KeyMapping("key.jetsetcraft.rush_menu", KeyConflictContext.IN_GAME,
            InputConstants.Type.KEYSYM, GLFW.GLFW_KEY_UNKNOWN, "key.categories.jetsetcraft");
    static final int HUD_WIDTH = 194;
    static final int HUD_HEIGHT = 98;
    private static CompoundTag state = new CompoundTag();
    private static ListTag album = new ListTag();
    private static ListTag roster = new ListTag();
    private static ListTag arenaCatalogue = new ListTag();
    private static int albumPage, rosterPage;
    private static int albumPages = 1, rosterPages = 1;

    public static void accept(CompoundTag incoming) {
        Minecraft minecraft = Minecraft.getInstance();
        CompoundTag previous = state;
        state = incoming.copy();
        if (incoming.contains("Album", Tag.TAG_LIST)) {
            album = boundedPage(incoming.getList("Album", Tag.TAG_COMPOUND));
            albumPage = Math.max(0, incoming.getInt("Page"));
            albumPages = Math.max(1, incoming.getInt("TotalPages"));
        }
        if (incoming.contains("Roster", Tag.TAG_LIST)) {
            roster = boundedPage(incoming.getList("Roster", Tag.TAG_COMPOUND));
            rosterPage = Math.max(0, incoming.getInt("Page"));
            rosterPages = Math.max(1, incoming.getInt("TotalPages"));
        }
        if (incoming.contains("ArenaCatalogue", Tag.TAG_LIST)) {
            arenaCatalogue = new ListTag();
            ListTag source = incoming.getList("ArenaCatalogue", Tag.TAG_COMPOUND);
            for (int i = 0; i < Math.min(32, source.size()); i++) arenaCatalogue.add(source.getCompound(i).copy());
        }
        if (minecraft.player == null) return;
        if (minecraft.screen instanceof RivalRushScreen screen) {
            screen.onSnapshot(incoming, previous);
        } else if (incoming.getBoolean("Open")) {
            minecraft.setScreen(new RivalRushScreen(incoming));
        }
    }

    private static ListTag boundedPage(ListTag source) {
        ListTag result = new ListTag();
        for (int i = 0; i < Math.min(20, source.size()); i++) result.add(source.getCompound(i).copy());
        return result;
    }

    static CompoundTag state() { return state; }
    static ListTag album() { return album; }
    static ListTag roster() { return roster; }
    static ListTag arenaCatalogue() { return arenaCatalogue; }
    static CompoundTag journey() { return state.getCompound("Journey"); }
    static boolean choosingRoute() { return state.getBoolean("Active") && "choosing".equals(journey().getString("Phase")); }
    public static CompoundTag stateSnapshot() { return state.copy(); }
    public static boolean isJourneyScreenOpen() {
        return Minecraft.getInstance().screen instanceof RivalRushScreen screen && screen.isRouteView();
    }
    static int page(boolean cards) { return cards ? albumPage : rosterPage; }
    static int pages(boolean cards) { return cards ? albumPages : rosterPages; }
    static ListTag offers() { return state.getList("Offers", Tag.TAG_COMPOUND); }
    static UUID runId() { return state.hasUUID("Run") ? state.getUUID("Run") : null; }

    /** Presentation gating for optional music; callers do not receive mutable run NBT. */
    public static boolean active() {
        Minecraft minecraft = Minecraft.getInstance();
        return state.getBoolean("Active") && minecraft.player != null && minecraft.level != null
                && minecraft.getConnection() != null
                && "jetsetcraft:rival_rush".equals(minecraft.level.dimension().location().toString());
    }

    static void request(String action, String value, int number) {
        if (Minecraft.getInstance().getConnection() == null) return;
        JetSetNetwork.sendRushAction(action, value, number, runId(), state.getInt("Version"));
    }

    public static void requestJourney(String profileId) {
        if (Minecraft.getInstance().getConnection() == null) return;
        JetSetNetwork.sendRushAction("journey_choose", profileId, 0, runId(), journey().getInt("Version"));
    }

    static String clock(long ticks) {
        long seconds = Math.max(0L, ticks) / 20L;
        return String.format(Locale.ROOT, "%d:%02d", seconds / 60L, seconds % 60L);
    }

    static String clipped(String value, int pixels) {
        var font = Minecraft.getInstance().font;
        return font.width(value) <= pixels ? value : font.plainSubstrByWidth(value, Math.max(0, pixels - font.width("…"))) + "…";
    }

    static void drawHud(GuiGraphics gui, int screenWidth, int screenHeight, boolean preview) {
        if (!preview && (!state.getBoolean("Active") || !RivalRushHudPreferences.get().visible)) return;
        var preferences = RivalRushHudPreferences.get();
        float scale = preferences.scale;
        int x = Math.round(preferences.x * Math.max(0, screenWidth - Math.round(HUD_WIDTH * scale)));
        int y = Math.round(preferences.y * Math.max(0, screenHeight - Math.round(HUD_HEIGHT * scale)));
        var font = Minecraft.getInstance().font;
        gui.pose().pushPose();
        gui.pose().translate(x, y, 0);
        gui.pose().scale(scale, scale, 1.0F);
        gui.fill(0, 0, HUD_WIDTH, HUD_HEIGHT, 0xB0181C20);
        gui.fill(0, 0, 2, HUD_HEIGHT, 0xFFC5ED55);
        gui.drawString(font, "RIVAL RUSH", 7, 6, 0xFFC5ED55, true);
        boolean active = state.getBoolean("Active");
        boolean circuit = active && state.contains("Journey", Tag.TAG_COMPOUND);
        CompoundTag journey = journey();
        String timer = circuit ? switch (journey.getString("Phase")) {
            case "choosing" -> "Choose";
            case "travel" -> Math.max(0, (int) Math.ceil(journey.getDouble("Distance"))) + "m";
            case "complete" -> "Set done";
            default -> clock(journey.getLong("TicksLeft"));
        } : active ? (state.getBoolean("Endless") ? "+ " + clock(state.getLong("Ticks"))
                : clock(Math.max(0L, 36_000L - state.getLong("Ticks")))) : "HUD preview";
        gui.drawString(font, timer, HUD_WIDTH - 7 - font.width(timer), 6, 0xFFFFFFFF, true);
        String gang = circuit ? "Arena " + journey.getInt("Stage") + "/" + Math.max(1, journey.getInt("StagesPerSet"))
                + " • " + journey.getString("CurrentName") : active ? state.getString("Gang") : "Your rival crew";
        gui.drawString(font, clipped(gang, HUD_WIDTH - 14), 7, 20, 0xFFE7E7E7, true);
        long xp = Math.max(0L, state.getLong("Xp"));
        long next = Math.max(1L, state.getLong("NextXp"));
        String xpText = active ? "Rush XP  " + xp + " / " + next + "  •  Lv " + Math.max(1, state.getInt("Level"))
                : "Rush XP — separate from Minecraft";
        gui.drawString(font, clipped(xpText, HUD_WIDTH - 14), 7, 33, 0xFF87E1EA, true);
        bar(gui, 7, 45, HUD_WIDTH - 14, ratio(xp, next), 0xFF62CFDC);
        double vitality = finite(state.getDouble("Vitality"));
        double maxVitality = Math.max(1.0D, finite(state.getDouble("MaxVitality")));
        String health = active ? "Vitality " + (int) Math.ceil(vitality) + " / " + (int) Math.ceil(maxVitality)
                : "Run vitality";
        gui.drawString(font, health, 7, 53, 0xFFF3C1CC, true);
        bar(gui, 7, 65, HUD_WIDTH - 14, ratio(vitality, maxVitality), 0xFFE9869C);
        String progress = active ? "Score " + Math.max(0L, state.getLong("Score")) + "  •  Cards "
                + Math.max(0L, state.getLong("Cards")) : "Score and card progress";
        gui.drawString(font, clipped(progress, HUD_WIDTH - 14), 7, 73, 0xFFE7E7E7, true);
        String objective = !active ? "Objective / upgrade reminder" : !offers().isEmpty() ? "Choose an upgrade — open Rush menu"
                : choosingRoute() ? "Choose the next arena — open Rush menu"
                : state.getBoolean("Finale") ? "Set complete — extract or play Encore" : state.getString("Objective");
        gui.drawString(font, clipped(objective, HUD_WIDTH - 14), 7, 86, 0xFFE6DBA6, true);
        gui.pose().popPose();
    }

    private static double finite(double value) { return Double.isFinite(value) ? Math.max(0.0D, value) : 0.0D; }
    private static float ratio(double current, double maximum) {
        return (float) Math.max(0.0D, Math.min(1.0D, finite(current) / Math.max(1.0D, finite(maximum))));
    }
    private static void bar(GuiGraphics gui, int x, int y, int width, float fraction, int color) {
        gui.fill(x, y, x + width, y + 4, 0xFF333940);
        gui.fill(x, y, x + Math.round(width * fraction), y + 4, color);
    }

    @Mod.EventBusSubscriber(modid = JetSetCraft.MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
    public static final class ModEvents {
        @SubscribeEvent
        public static void keys(RegisterKeyMappingsEvent event) { event.register(MENU); }
    }

    @Mod.EventBusSubscriber(modid = JetSetCraft.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE, value = Dist.CLIENT)
    public static final class ForgeEvents {
        @SubscribeEvent
        public static void tick(TickEvent.ClientTickEvent event) {
            if (event.phase != TickEvent.Phase.END) return;
            Minecraft minecraft = Minecraft.getInstance();
            while (MENU.consumeClick()) {
                if (minecraft.player != null && minecraft.screen == null) request("menu", "", 0);
            }
        }

        @SubscribeEvent
        public static void overlay(RenderGuiOverlayEvent.Post event) {
            Minecraft minecraft = Minecraft.getInstance();
            if (event.getOverlay() != VanillaGuiOverlay.HOTBAR.type() || minecraft.player == null
                    || minecraft.options.hideGui || minecraft.screen instanceof RivalRushScreen
                    || minecraft.screen instanceof RivalRushHudScreen) return;
            drawHud(event.getGuiGraphics(), minecraft.getWindow().getGuiScaledWidth(),
                    minecraft.getWindow().getGuiScaledHeight(), false);
        }

        @SubscribeEvent
        public static void logout(ClientPlayerNetworkEvent.LoggingOut event) {
            state = new CompoundTag();
            album = new ListTag();
            roster = new ListTag();
            arenaCatalogue = new ListTag();
            albumPage = rosterPage = 0;
            albumPages = rosterPages = 1;
        }
    }

    private RivalRushClient() {}
}
