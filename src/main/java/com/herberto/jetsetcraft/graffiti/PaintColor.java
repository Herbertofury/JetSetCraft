package com.herberto.jetsetcraft.graffiti;

import net.minecraft.world.item.DyeColor;

/** The complete vanilla dye palette shared by free spray paint, balloons, particles and splat decals. */
public enum PaintColor {
    WHITE(0, "white", DyeColor.WHITE, 0xF9FFFE, 15),
    ORANGE(1, "orange", DyeColor.ORANGE, 0xF9801D, 4),
    MAGENTA(2, "magenta", DyeColor.MAGENTA, 0xC74EBD, 11),
    LIGHT_BLUE(3, "light_blue", DyeColor.LIGHT_BLUE, 0x3AB3DA, 8),
    YELLOW(4, "yellow", DyeColor.YELLOW, 0xFED83D, 5),
    LIME(5, "lime", DyeColor.LIME, 0x80C71F, 6),
    PINK(6, "pink", DyeColor.PINK, 0xF38BAA, 11),
    GRAY(7, "gray", DyeColor.GRAY, 0x474F52, 13),
    LIGHT_GRAY(8, "light_gray", DyeColor.LIGHT_GRAY, 0x9D9D97, 14),
    CYAN(9, "cyan", DyeColor.CYAN, 0x169C9C, 7),
    PURPLE(10, "purple", DyeColor.PURPLE, 0x8932B8, 10),
    BLUE(11, "blue", DyeColor.BLUE, 0x3C44AA, 9),
    BROWN(12, "brown", DyeColor.BROWN, 0x835432, 12),
    GREEN(13, "green", DyeColor.GREEN, 0x5E7C16, 6),
    RED(14, "red", DyeColor.RED, 0xB02E26, 3),
    BLACK(15, "black", DyeColor.BLACK, 0x1D1D21, 1);

    private final int id;
    private final String serializedName;
    private final DyeColor dye;
    private final int rgb;
    private final int paletteIndex;

    PaintColor(int id, String serializedName, DyeColor dye, int rgb, int paletteIndex) {
        this.id = id;
        this.serializedName = serializedName;
        this.dye = dye;
        this.rgb = rgb;
        this.paletteIndex = paletteIndex;
    }

    public int id() { return id; }
    public String serializedName() { return serializedName; }
    public DyeColor dye() { return dye; }
    public int rgb() { return rgb; }
    public int argb() { return 0xFF000000 | rgb; }
    public int paletteIndex() { return paletteIndex; }

    public static PaintColor byId(int id) {
        PaintColor[] colors = values();
        return colors[Math.floorMod(id, colors.length)];
    }
}
