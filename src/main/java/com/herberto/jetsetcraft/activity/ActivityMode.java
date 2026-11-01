package com.herberto.jetsetcraft.activity;

import java.util.List;
import java.util.Optional;

/** One player-facing catalogue for short street games and the optional arena roguelite. */
public enum ActivityMode {
    RANDOM("random", "Surprise me", "A random available game is revealed before you accept it.", 0, 0),
    TAG("tag", "Tag", "Catch six rival skaters with a close tag. Chase, turn and transfer around the local circuit.", 1800, 6),
    KING_OF_HILL("king_of_hill", "King of the Hill", "Capture three moving hill locations. Tag nearby contenders to open a scoring window.", 2400, 3),
    SHOWDOWN("showdown", "Crew showdown", "Land three clean paint tags on each of four rivals. Defeats stay inside this non-lethal contest.", 2400, 4),
    RIVAL_RUSH("rival_rush", "Rival Rush", "Choose a branching arena journey: temporary run builds and shared crew rewards.", 0, 0);

    public final String id, label, description;
    public final int durationTicks, goal;
    ActivityMode(String id, String label, String description, int durationTicks, int goal) {
        this.id = id; this.label = label; this.description = description;
        this.durationTicks = durationTicks; this.goal = goal;
    }
    public boolean local() { return this != RANDOM && this != RIVAL_RUSH; }
    public static Optional<ActivityMode> byId(String id) {
        for (ActivityMode mode : values()) if (mode.id.equals(id)) return Optional.of(mode);
        return Optional.empty();
    }
    public static List<ActivityMode> games() { return List.of(TAG, KING_OF_HILL, SHOWDOWN, RIVAL_RUSH); }
}
