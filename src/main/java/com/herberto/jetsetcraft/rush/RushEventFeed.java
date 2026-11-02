package com.herberto.jetsetcraft.rush;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.Locale;

/** Small, run-owned display history. Only accepted server events are appended; it never grants rewards. */
public final class RushEventFeed {
    public enum Kind { CACHE, MASTERY, HEADLINER, EVOLUTION, SERVICE, CONTRACT, ARENA }
    private record Notice(long id, Kind kind, String title, String detail) { }
    private final Deque<Notice> recent = new ArrayDeque<>();
    private long sequence;

    public long emit(Kind kind, String title, String detail) {
        if (kind == null || sequence == Long.MAX_VALUE) return 0;
        String heading = clean(title, 64);
        if (heading.isEmpty()) return 0;
        Notice notice = new Notice(++sequence, kind, heading, clean(detail, 240));
        if (recent.size() == 4) recent.removeFirst();
        recent.addLast(notice);
        return notice.id();
    }

    /** Fresh detached tags keep snapshots and clients from changing the authoritative event history. */
    public ListTag snapshot() {
        ListTag result = new ListTag();
        for (Notice notice : recent) {
            CompoundTag row = new CompoundTag();
            row.putLong("Id", notice.id()); row.putString("Kind", notice.kind().name().toLowerCase(Locale.ROOT));
            row.putString("Title", notice.title()); row.putString("Detail", notice.detail());
            result.add(row);
        }
        return result;
    }

    private static String clean(String value, int limit) {
        String text = value == null ? "" : value.replaceAll("[\\p{Cntrl}§]", " ").trim();
        return text.substring(0, Math.min(limit, text.length()));
    }
}
