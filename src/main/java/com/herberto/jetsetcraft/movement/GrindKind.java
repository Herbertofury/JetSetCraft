package com.herberto.jetsetcraft.movement;

public enum GrindKind {
    NONE(0, "none", false),
    EDGE(1, "edge", false),
    VANILLA_RAIL(2, "rail", true),
    CREATE_TRACK(3, "create_track", true),
    CUSTOM_RAIL(4, "custom_rail", true);

    private final int id;
    private final String serializedName;
    private final boolean rail;

    GrindKind(int id, String serializedName, boolean rail) {
        this.id = id;
        this.serializedName = serializedName;
        this.rail = rail;
    }

    public int id() { return id; }
    public String serializedName() { return serializedName; }
    public boolean rail() { return rail; }

    public static GrindKind byId(int id) {
        for (GrindKind kind : values()) if (kind.id == id) return kind;
        return NONE;
    }
}
