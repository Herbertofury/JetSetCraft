package com.herberto.jetsetcraft.activity;

/** Bounded authoritative objective clock. Actor ownership and physical interaction are checked by the manager. */
public final class ActivityRun {
    public static final int HILL_TICKS = 160;
    private final ActivityMode mode;
    private int ticks, objectives, hillTicks;
    private boolean ended, won;

    public ActivityRun(ActivityMode mode) {
        if (mode == null || !mode.local()) throw new IllegalArgumentException("A local game is required");
        this.mode = mode;
    }
    public ActivityMode mode() { return mode; }
    public int ticks() { return ticks; }
    public int objectives() { return objectives; }
    public int hillTicks() { return hillTicks; }
    public int progress() { return mode == ActivityMode.KING_OF_HILL ? objectives * HILL_TICKS + hillTicks : objectives; }
    public int goal() { return mode == ActivityMode.KING_OF_HILL ? mode.goal * HILL_TICKS : mode.goal; }
    public int remaining() { return Math.max(0, mode.durationTicks - ticks); }
    public boolean ended() { return ended; }
    public boolean won() { return won; }
    public void tick() {
        if (ended) return;
        ticks++;
        if (ticks >= mode.durationTicks) ended = true;
    }
    public boolean tagged() {
        if (ended || mode == ActivityMode.KING_OF_HILL) return false;
        objectives++;
        finishIfReady(); return true;
    }
    public boolean holdHill(boolean inside, boolean contested) {
        if (ended || mode != ActivityMode.KING_OF_HILL || !inside || contested) return false;
        hillTicks++;
        if (hillTicks >= HILL_TICKS) { hillTicks = 0; objectives++; finishIfReady(); }
        return true;
    }
    public void cancel() { ended = true; }
    private void finishIfReady() {
        if (objectives >= mode.goal) { objectives = mode.goal; ended = true; won = true; }
    }
}
