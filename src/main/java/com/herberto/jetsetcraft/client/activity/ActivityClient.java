package com.herberto.jetsetcraft.client.activity;

import com.herberto.jetsetcraft.JetSetCraft;
import com.herberto.jetsetcraft.client.rush.RivalRushClient;
import com.herberto.jetsetcraft.network.JetSetNetwork;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.ClientPlayerNetworkEvent;
import net.minecraftforge.client.event.RenderGuiOverlayEvent;
import net.minecraftforge.client.gui.overlay.VanillaGuiOverlay;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.Locale;
import java.util.UUID;

/** Activity choices and status presentation; all progress, outcomes and rewards belong to the server. */
@Mod.EventBusSubscriber(modid = JetSetCraft.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE, value = Dist.CLIENT)
public final class ActivityClient {
    private static CompoundTag state = new CompoundTag();

    public static void accept(CompoundTag incoming) {
        Minecraft minecraft = Minecraft.getInstance();
        CompoundTag previous = state;
        state = incoming == null ? new CompoundTag() : incoming.copy();
        // The server's five-entry catalogue is bounded again at the presentation boundary.
        ListTag rows = state.getList("Activities", Tag.TAG_COMPOUND);
        if (rows.size() > 8) {
            ListTag bounded = new ListTag();
            for (int i = 0; i < 8; i++) bounded.add(rows.getCompound(i).copy());
            state.put("Activities", bounded);
        }
        if (minecraft.player == null) return;
        if (minecraft.screen instanceof ActivityScreen screen) screen.onSnapshot(state, previous);
        else if (state.getBoolean("Open")) minecraft.setScreen(new ActivityScreen());
    }

    static CompoundTag state() { return state; }
    static ListTag activities() { return state.getList("Activities", Tag.TAG_COMPOUND); }
    public static CompoundTag stateSnapshot() { return state.copy(); }
    public static boolean isScreenOpen() { return Minecraft.getInstance().screen instanceof ActivityScreen; }

    public static void request(String action, String mode) {
        if (Minecraft.getInstance().getConnection() == null) return;
        UUID context = state.hasUUID("Context") ? state.getUUID("Context") : null;
        JetSetNetwork.sendActivityAction(action, mode == null ? "" : mode, context, state.getInt("Version"));
    }

    static String clipped(String value, int pixels) {
        var font = Minecraft.getInstance().font;
        return font.width(value) <= pixels ? value
                : font.plainSubstrByWidth(value, Math.max(0, pixels - font.width("…"))) + "…";
    }

    static String clock(long ticks) {
        long seconds = Math.max(0L, ticks) / 20L;
        return String.format(Locale.ROOT, "%d:%02d", seconds / 60L, seconds % 60L);
    }

    @SubscribeEvent
    public static void overlay(RenderGuiOverlayEvent.Post event) {
        Minecraft minecraft = Minecraft.getInstance();
        if (event.getOverlay() != VanillaGuiOverlay.HOTBAR.type() || minecraft.player == null
                || minecraft.options.hideGui || !state.getBoolean("Active") || RivalRushClient.active()
                || minecraft.screen instanceof ActivityScreen) return;
        int width = Math.min(230, minecraft.getWindow().getGuiScaledWidth() - 16);
        int x = minecraft.getWindow().getGuiScaledWidth() - width - 8;
        int y = 8;
        GuiGraphics gui = event.getGuiGraphics();
        gui.fill(x, y, x + width, y + 78, 0xCF181C20);
        gui.fill(x, y, x + 2, y + 78, 0xFF87E1EA);
        var font = minecraft.font;
        gui.drawString(font, clipped(state.getString("ModeName"), width - 58), x + 7, y + 6, 0xFFC5ED55, true);
        String timer = clock(state.getLong("RemainingTicks"));
        gui.drawString(font, timer, x + width - 7 - font.width(timer), y + 6, 0xFFFFFFFF, true);
        gui.drawString(font, clipped(state.getString("Gang"), width - 14), x + 7, y + 20, 0xFFE7E7E7, true);
        int progress = Math.max(0, state.getInt("Progress"));
        int goal = Math.max(1, state.getInt("Goal"));
        gui.drawString(font, "Progress " + progress + " / " + goal, x + 7, y + 34, 0xFF87E1EA, true);
        gui.fill(x + 7, y + 46, x + width - 7, y + 50, 0xFF333940);
        int filled = (int) Math.round((width - 14) * Math.min(1.0D, (double) progress / goal));
        gui.fill(x + 7, y + 46, x + 7 + filled, y + 50, 0xFF62CFDC);
        gui.drawString(font, clipped(state.getString("Objective"), width - 14), x + 7, y + 56, 0xFFE6DBA6, true);
        gui.drawString(font, "/jetsetcraft activity • menu / end", x + 7, y + 67, 0xFFBBBBBB, true);
    }

    @SubscribeEvent
    public static void logout(ClientPlayerNetworkEvent.LoggingOut event) { state = new CompoundTag(); }

    private ActivityClient() {}
}
