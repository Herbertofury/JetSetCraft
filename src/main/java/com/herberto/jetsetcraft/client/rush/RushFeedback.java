package com.herberto.jetsetcraft.client.rush;

import com.herberto.jetsetcraft.config.JetSetConfig;
import net.minecraft.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.toasts.SystemToast;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.UUID;

/** Presentation of server-authored events only. A toast never infers or requests an award. */
final class RushFeedback {
    private record Notice(long id, String title, String detail) {}
    private static final ArrayDeque<Notice> QUEUED = new ArrayDeque<>();
    private static UUID run;
    private static long lastId, nextToastAt;

    static void accept(CompoundTag snapshot) {
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.player == null || minecraft.getConnection() == null || !snapshot.hasUUID("Run")) return;
        UUID incomingRun = snapshot.getUUID("Run");
        if (!incomingRun.equals(run)) {
            reset();
            run = incomingRun;
        }
        var incoming = snapshot.getList("Feedback", Tag.TAG_COMPOUND);
        ArrayList<Notice> notices = new ArrayList<>(4);
        for (int i = 0; i < Math.min(4, incoming.size()); i++) {
            CompoundTag entry = incoming.getCompound(i);
            long id = entry.getLong("Id");
            String title = clean(entry.getString("Title"), 64);
            if (id > lastId && !title.isBlank()) notices.add(new Notice(id, title, clean(entry.getString("Detail"), 240)));
        }
        notices.sort(Comparator.comparingLong(Notice::id));
        for (Notice notice : notices) {
            if (notice.id() <= lastId) continue;
            lastId = notice.id();
            if (JetSetConfig.CLIENT.reducedMotion.get()) chat(notice);
            else {
                while (QUEUED.size() >= 4) chat(QUEUED.removeFirst());
                QUEUED.addLast(notice);
            }
        }
        tick();
    }

    static void tick() {
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.player == null || minecraft.getConnection() == null || QUEUED.isEmpty()) return;
        if (JetSetConfig.CLIENT.reducedMotion.get()) {
            while (!QUEUED.isEmpty()) chat(QUEUED.removeFirst());
            return;
        }
        long now = Util.getMillis();
        if (now < nextToastAt) return;
        Notice notice = QUEUED.removeFirst();
        String title = RivalRushClient.clipped(notice.title(), Math.max(80,
                Math.min(220, minecraft.getWindow().getGuiScaledWidth() - 36)));
        minecraft.getToasts().addToast(SystemToast.multiline(minecraft, SystemToast.SystemToastIds.PERIODIC_NOTIFICATION,
                Component.literal(title), Component.literal(notice.detail())));
        // Bound our queue and leave one native toast's normal display interval between entries.
        nextToastAt = now + 6_000L;
    }

    private static void chat(Notice notice) {
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.player != null) minecraft.player.displayClientMessage(Component.literal(notice.title()
                + (notice.detail().isBlank() ? "" : ": " + notice.detail())), false);
    }

    private static String clean(String text, int limit) {
        String cleaned = text.replaceAll("§.", "").replaceAll("[\\p{Cntrl}]", " ").strip();
        return cleaned.substring(0, Math.min(limit, cleaned.length()));
    }

    static void reset() { run = null; lastId = 0; nextToastAt = 0; QUEUED.clear(); }
    /** Native audit waits for real notices to finish instead of deleting or hiding their pixels. */
    static boolean isIdle() {
        return QUEUED.isEmpty() && Minecraft.getInstance().getToasts().getToast(SystemToast.class,
                SystemToast.SystemToastIds.PERIODIC_NOTIFICATION) == null;
    }
    private RushFeedback() {}
}
