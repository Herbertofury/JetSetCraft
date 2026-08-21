package com.herberto.jetsetcraft.movement;

import java.util.Locale;

/**
 * Stable, data-light trick vocabulary shared by server scoring and client presentation.
 * IDs are serialized over the network; names vary by ride style without changing gameplay identity.
 */
public final class TrickCatalog {
    public static final int AIR = 0;
    public static final int GRIND = 1;
    public static final int GROUND = 2;
    public static final int TRICKS_PER_CONTEXT = 8;
    public static final int TRICK_COUNT = TRICKS_PER_CONTEXT * 3;

    public record Trick(int id, int context, int animationIndex, int basePoints,
                        float multiplierGain, float boostReward) {}

    private static final int[] AIR_POINTS = {180, 205, 225, 250, 275, 305, 335, 370};
    private static final int[] GRIND_POINTS = {190, 215, 240, 270, 300, 330, 365, 405};
    private static final int[] GROUND_POINTS = {210, 240, 275, 315, 360, 410, 465, 530};

    private static final String[][] AIR_NAMES = {
            common("Aerial"),
            {"Mute 360", "Rocket Spin", "Corkscrew", "Heel Click", "Cross Grab", "Unity Twist", "Bio Flip", "Skyline Spin"},
            {"Kickflip", "Heelflip", "Pop Shove-It", "Hardflip", "Varial Flip", "Impossible", "Indy Grab", "Method"},
            {"Barspin", "Tailwhip", "No-Hander", "Tabletop", "Can-Can", "Superman", "Toboggan", "Backflip"},
            {"Disco Spin", "Toe-Stop Flip", "Butterfly Kick", "Side Surf", "Moon Kick", "Orbit", "Star Split", "Roller Cork"},
            {"Flux Flip", "Phase Shift", "Ion Roll", "Zero-G Grab", "Vector Twist", "Pulse Cork", "Orbit Break", "Neon Eclipse"},
            {"Barspin", "Tailwhip", "Bri Flip", "No-Hander", "Heelwhip", "Fingerwhip", "Tuck No-Hander", "Corkscrew"}
    };

    private static final String[][] GRIND_NAMES = {
            common("Grind Shift"),
            {"Soul Slide", "Unity Grind", "Torque Soul", "Backslide", "Acid Soul", "Royale", "Savannah", "Fastslide"},
            {"Boardslide", "Lipslide", "Feeble", "Smith", "Crooked", "Noseslide", "Darkslide", "Bluntslide"},
            {"Double Peg", "Feeble", "Smith", "Ice Pick", "Toothpick", "Crooked", "Luc-E", "Over Smith"},
            {"Disco Grind", "Toe-Stop Slide", "Side Surf", "Royal Roll", "Sunset Slide", "Cross-Step Grind", "Orbit Grind", "Crown Slide"},
            {"Flux Slide", "Phase Rail", "Mag-Lock", "Vector Grind", "Ion Drift", "Pulse Switch", "Zero-G Rail", "Neon Lock"},
            {"Deck Grind", "Feeble", "Smith", "Front Board", "Back Board", "Hurricane", "Over Crook", "Rotor Slide"}
    };

    private static final String[] GROUND_NAMES = {
            "Backspin", "Windmill", "Flare", "Headspin", "Air Freeze", "Handplant", "Halo", "1990 Spin"
    };

    public static Trick select(int context, RideStyle style, float forward, float strafe, int chainSeed) {
        int directional = directionSlot(forward, strafe);
        int styleSalt = Math.max(0, style.id()) * 3;
        int slot = Math.floorMod(directional + chainSeed + styleSalt, TRICKS_PER_CONTEXT);
        return byId(context * TRICKS_PER_CONTEXT + slot);
    }

    public static Trick byId(int id) {
        int safe = Math.floorMod(id, TRICK_COUNT);
        int context = safe / TRICKS_PER_CONTEXT;
        int slot = safe % TRICKS_PER_CONTEXT;
        int points = switch (context) {
            case GRIND -> GRIND_POINTS[slot];
            case GROUND -> GROUND_POINTS[slot];
            default -> AIR_POINTS[slot];
        };
        float multiplier = 0.14f + slot * 0.016f + (context == GROUND ? 0.05f : context == GRIND ? 0.025f : 0.0f);
        float boost = 1.5f + slot * 0.32f + (context == GROUND ? 1.5f : context == GRIND ? 0.8f : 0.0f);
        return new Trick(safe, context, slot, points, multiplier, boost);
    }

    public static String name(int id, RideStyle style) {
        Trick trick = byId(id);
        int slot = trick.animationIndex();
        if (trick.context() == GROUND) return GROUND_NAMES[slot];
        int styleId = Math.max(0, Math.min(AIR_NAMES.length - 1, style.id()));
        return (trick.context() == GRIND ? GRIND_NAMES : AIR_NAMES)[styleId][slot];
    }

    public static String contextName(int id) {
        return switch (byId(id).context()) {
            case GRIND -> "GRIND";
            case GROUND -> "BREAK";
            default -> "AIR";
        };
    }

    public static String rankName(int score, float multiplier, float flow) {
        double heat = score / 650.0 + Math.max(0.0, multiplier - 1.0) * 1.4 + flow / 18.0;
        if (heat >= 28.0) return "ALL CITY";
        if (heat >= 18.0) return "LEGEND";
        if (heat >= 11.0) return "WILD";
        if (heat >= 6.0) return "HYPE";
        if (heat >= 2.5) return "FRESH";
        return "WARM UP";
    }

    public static String landingName(int grade) {
        return switch (grade) {
            case 3 -> "PERFECT LANDING";
            case 2 -> "CLEAN LANDING";
            case 1 -> "SKETCHY LANDING";
            default -> "";
        };
    }

    public static String debugName(int id, RideStyle style) {
        return String.format(Locale.ROOT, "%s/%s#%d", contextName(id), name(id, style), byId(id).animationIndex());
    }

    private static int directionSlot(float forward, float strafe) {
        if (Math.abs(strafe) > Math.abs(forward)) return strafe > 0.20f ? 2 : 6;
        if (forward > 0.20f) return 1;
        if (forward < -0.20f) return 5;
        return 0;
    }

    private static String[] common(String base) {
        return new String[]{base + " I", base + " II", base + " III", base + " IV",
                base + " V", base + " VI", base + " VII", base + " VIII"};
    }

    private TrickCatalog() {}
}
