package com.herberto.jetsetcraft.activity;

/** Bounded authoritative objective clock. Actor ownership and physical interaction are checked by the manager. */
public final class ActivityRun {
    public static final int HILL_TICKS = 160;
    private final ActivityMode mode;
    private final int objectiveGoal, hillDuration, duration;
    private int ticks, objectives, hillTicks;
    private boolean ended, won;

    public ActivityRun(ActivityMode mode) {
        this(mode, mode == null ? 0 : mode.goal, HILL_TICKS, mode == null ? 0 : mode.durationTicks);
    }
    public ActivityRun(ActivityMode mode, int objectiveGoal, int hillDuration, int duration) {
        if (mode == null || !mode.local()) throw new IllegalArgumentException("A local game is required");
        if (objectiveGoal < 1 || objectiveGoal > 12 || hillDuration < 100 || hillDuration > 240
                || duration < 1200 || duration > 6000) throw new IllegalArgumentException("Street round bounds exceeded");
        this.mode = mode;
        this.objectiveGoal = objectiveGoal; this.hillDuration = hillDuration; this.duration = duration;
    }
    public ActivityMode mode() { return mode; }
    public int ticks() { return ticks; }
    public int objectives() { return objectives; }
    public int hillTicks() { return hillTicks; }
    public int objectiveGoal() { return objectiveGoal; }
    public int hillDuration() { return hillDuration; }
    public int progress() { return mode == ActivityMode.KING_OF_HILL ? objectives * hillDuration + hillTicks : objectives; }
    public int goal() { return mode == ActivityMode.KING_OF_HILL ? objectiveGoal * hillDuration : objectiveGoal; }
    public int remaining() { return Math.max(0, duration - ticks); }
    public boolean ended() { return ended; }
    public boolean won() { return won; }
    public void tick() {
        if (ended) return;
        ticks++;
        if (ticks >= duration) ended = true;
    }
    public boolean tagged() {
        if (ended || mode == ActivityMode.KING_OF_HILL) return false;
        objectives++;
        finishIfReady(); return true;
    }
    public boolean holdHill(boolean inside, boolean contested) {
        if (ended || mode != ActivityMode.KING_OF_HILL || !inside || contested) return false;
        hillTicks++;
        if (hillTicks >= hillDuration) { hillTicks = 0; objectives++; finishIfReady(); }
        return true;
    }
    public void cancel() { ended = true; }
    private void finishIfReady() {
        if (objectives >= objectiveGoal) { objectives = objectiveGoal; ended = true; won = true; }
    }
}
