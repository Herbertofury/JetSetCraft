package com.herberto.jetsetcraft.network;

public final class InputFlags {
    public static final int BOOST = 1;
    public static final int TRICK = 1 << 1;
    public static final int GRIND = 1 << 2;
    public static final int MANUAL = 1 << 3;
    public static final int BRAKE = 1 << 4;
    public static final int JUMP = 1 << 5;
    private InputFlags() {}
}
