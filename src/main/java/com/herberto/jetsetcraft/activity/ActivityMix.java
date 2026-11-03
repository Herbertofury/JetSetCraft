package com.herberto.jetsetcraft.activity;

import net.minecraft.core.BlockPos;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/** One bounded, explicit three-round street playlist. Earned rounds never depend on finishing the set. */
public final class ActivityMix {
    public static final String FORMAT = "street_mix";
    private static final List<ActivityMode> STREET = List.of(ActivityMode.TAG, ActivityMode.KING_OF_HILL, ActivityMode.SHOWDOWN);
    private final UUID id;
    private final List<ActivityMode> route;
    private int round, cleared;
    private boolean cancelled;

    public ActivityMix(UUID id, ActivityMode first) {
        if (id == null || first == null || !first.local()) throw new IllegalArgumentException("A local first round is required");
        this.id = id;
        int start = STREET.indexOf(first);
        List<ActivityMode> ordered = new ArrayList<>();
        for (int index = 0; index < STREET.size(); index++) ordered.add(STREET.get((start + index) % STREET.size()));
        route = List.copyOf(ordered);
    }

    public ActivityMode mode() { return route.get(round); }
    public List<ActivityMode> route() { return route; }
    public int round() { return round + 1; }
    public int cleared() { return cleared; }
    public int total() { return route.size(); }
    public boolean complete() { return cleared == route.size(); }
    public boolean intermission() { return !cancelled && cleared == round + 1 && !complete(); }
    public ActivityMode next() { return intermission() ? route.get(round + 1) : mode(); }

    /** This ID identifies both physical actors and the existing per-mode settlement receipt. */
    public UUID eventId() {
        return UUID.nameUUIDFromBytes(("jetsetcraft:street_mix:" + id + ":" + round).getBytes(StandardCharsets.UTF_8));
    }

    public boolean wonRound(ActivityMode actual) {
        if (cancelled || complete() || cleared != round || actual != mode()) return false;
        cleared++; return true;
    }

    public boolean advance() {
        if (!intermission()) return false;
        round++; return true;
    }

    public void cancel() { cancelled = true; }

    public ActivityRun newRun() {
        return mode() == ActivityMode.KING_OF_HILL
                ? new ActivityRun(mode(), 5, 120, 3600) : new ActivityRun(mode());
    }

    public static String modifier(ActivityMode mode) {
        return switch (mode) {
            case TAG -> "Relay chase: catch three pairs of skaters. Return to the marked handoff ring between pairs.";
            case KING_OF_HILL -> "Five-site takeover: hold each marked hill uncontested for 6 seconds; tag contenders to clear it.";
            case SHOWDOWN -> "Captain finale: three rivals need 3 tags each. The final, faster captain needs 5 clean tags.";
            default -> "";
        };
    }

    public static List<BlockPos> hillOffsets() {
        return List.of(new BlockPos(9, 0, 0), new BlockPos(0, 0, 9), new BlockPos(-9, 0, 0),
                new BlockPos(0, 0, -9), new BlockPos(7, 0, 7));
    }
}
