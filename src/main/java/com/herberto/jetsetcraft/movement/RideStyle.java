package com.herberto.jetsetcraft.movement;

public enum RideStyle {
    NONE(0, "none", 0.0, 0.0, 0.0, 0.0, 0.0, 0.0),
    INLINE(1, "inline", 0.027, 0.59, 0.92, 0.024, 1.05, 0.20),
    BOARD(2, "board", 0.024, 0.56, 0.90, 0.020, 1.12, 0.17),
    BMX(3, "bmx", 0.030, 0.65, 0.98, 0.026, 0.90, 0.23),
    QUAD(4, "quad", 0.025, 0.55, 0.88, 0.022, 1.08, 0.25),
    /**
     * Hoverboard keeps JetSetCraft's universal trick/grind/world-physics contract instead of becoming
     * a separate vehicle subsystem. Its tuning favors smooth coasting, air correction and rail flow
     * while remaining below BMX's raw boost ceiling so vanilla-world impulses still matter.
     */
    HOVER(5, "hover", 0.028, 0.62, 0.96, 0.031, 1.16, 0.24);

    private final int id;
    private final String serializedName;
    private final double acceleration;
    private final double cruiseCap;
    private final double boostCap;
    private final double airControl;
    private final double grindMultiplier;
    private final double steering;

    RideStyle(int id, String serializedName, double acceleration, double cruiseCap, double boostCap,
              double airControl, double grindMultiplier, double steering) {
        this.id = id;
        this.serializedName = serializedName;
        this.acceleration = acceleration;
        this.cruiseCap = cruiseCap;
        this.boostCap = boostCap;
        this.airControl = airControl;
        this.grindMultiplier = grindMultiplier;
        this.steering = steering;
    }

    public int id() { return id; }
    public String serializedName() { return serializedName; }
    public double acceleration() { return acceleration; }
    public double cruiseCap() { return cruiseCap; }
    public double boostCap() { return boostCap; }
    public double airControl() { return airControl; }
    public double grindMultiplier() { return grindMultiplier; }
    public double steering() { return steering; }

    public static RideStyle byId(int id) {
        for (RideStyle v : values()) if (v.id == id) return v;
        return NONE;
    }
}
