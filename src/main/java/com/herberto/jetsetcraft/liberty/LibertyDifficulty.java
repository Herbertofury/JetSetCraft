package com.herberto.jetsetcraft.liberty;

import java.util.List;

/** Original JetSetCraft pressure budgets. These numbers are not claims about Helldivers' internal AI. */
public record LibertyDifficulty(int level, String name, int residentCap, int waveMin, int waveMax,
                                int waveIntervalTicks, int reinforcementBudget, int outpostCount,
                                int optionalCount, int elitePercent, int rewardPercent) {
    private static final List<LibertyDifficulty> LEVELS = List.of(
            new LibertyDifficulty(1, "Trivial", 24, 3, 5, 320, 220, 2, 2, 0, 100),
            new LibertyDifficulty(2, "Easy", 30, 4, 6, 300, 320, 2, 2, 3, 115),
            new LibertyDifficulty(3, "Medium", 36, 4, 8, 280, 450, 3, 2, 5, 130),
            new LibertyDifficulty(4, "Challenging", 42, 5, 10, 250, 600, 3, 3, 8, 150),
            new LibertyDifficulty(5, "Hard", 48, 6, 12, 220, 800, 4, 3, 10, 175),
            new LibertyDifficulty(6, "Extreme", 56, 7, 14, 200, 1020, 4, 3, 13, 200),
            new LibertyDifficulty(7, "Suicide Mission", 64, 8, 16, 180, 1280, 5, 4, 16, 230),
            new LibertyDifficulty(8, "Impossible", 72, 10, 18, 160, 1580, 5, 4, 20, 260),
            new LibertyDifficulty(9, "Helldive", 84, 12, 22, 140, 1900, 6, 5, 24, 300),
            new LibertyDifficulty(10, "Super Helldive", 96, 14, 24, 120, 2250, 6, 5, 28, 340));

    public LibertyDifficulty {
        if (level < 1 || level > 10 || name == null || name.isBlank() || residentCap < 1 || residentCap > 96
                || waveMin < 1 || waveMax < waveMin || waveMax > residentCap || waveIntervalTicks < 20
                || reinforcementBudget < residentCap || outpostCount < 0 || optionalCount < 0
                || elitePercent < 0 || elitePercent > 100 || rewardPercent < 100)
            throw new IllegalArgumentException("Invalid Liberty difficulty budget");
    }
    public static LibertyDifficulty of(int level) {
        if (level < 1 || level > 10) throw new IllegalArgumentException("Liberty difficulty must be 1 through 10");
        return LEVELS.get(level - 1);
    }
    public static List<LibertyDifficulty> all() { return LEVELS; }
    public int maxSpawnsPerTick() { return 8; }
    public int budgetForMinutes(int minutes) { checkMinutes(minutes); return reinforcementBudget * minutes / 25; }
    /** Prevent a fast-clearing squad from consuming the whole expedition budget in its first minutes. */
    public int allowanceAt(long elapsedTicks, int minutes) {
        int total = budgetForMinutes(minutes);
        long elapsed = Math.max(0, Math.min(elapsedTicks, minutes * 1200L));
        return Math.min(total, residentCap + (int) ((total - residentCap) * elapsed / (minutes * 1200L)));
    }
    static void checkMinutes(int minutes) {
        if (minutes != 20 && minutes != 25 && minutes != 30)
            throw new IllegalArgumentException("Liberty expeditions last 20, 25 or 30 minutes");
    }
}
