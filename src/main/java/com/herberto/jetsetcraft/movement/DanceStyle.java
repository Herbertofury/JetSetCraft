package com.herberto.jetsetcraft.movement;

/** Selectable street-dance families used by the server-authoritative cypher system. */
public enum DanceStyle {
    TOPROCK(0, "toprock", "Toprock"),
    POPPING(1, "popping", "Popping"),
    HOUSE(2, "house", "House"),
    BREAKING(3, "breaking", "Breaking"),
    HIP_HOP(4, "hip_hop", "Hip-Hop"),
    LOCKING(5, "locking", "Locking");

    private final int id;
    private final String serializedName;
    private final String displayName;

    DanceStyle(int id, String serializedName, String displayName) {
        this.id = id;
        this.serializedName = serializedName;
        this.displayName = displayName;
    }

    public int id() { return id; }
    public String serializedName() { return serializedName; }
    public String displayName() { return displayName; }

    public static DanceStyle byId(int id) {
        for (DanceStyle style : values()) if (style.id == id) return style;
        return TOPROCK;
    }

    /** Directional selection keeps the action immediate while neutral presses cycle the full roster. */
    public static DanceStyle select(DanceStyle current, float forward, float strafe, boolean manual, boolean brake) {
        if (manual) return HIP_HOP;
        if (brake) return BREAKING;
        if (Math.abs(strafe) > Math.abs(forward) && Math.abs(strafe) > 0.25f) {
            return strafe > 0.0f ? POPPING : LOCKING;
        }
        if (forward > 0.25f) return TOPROCK;
        if (forward < -0.25f) return HOUSE;
        return current;
    }
}
