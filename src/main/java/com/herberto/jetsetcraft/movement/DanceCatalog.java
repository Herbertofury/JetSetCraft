package com.herberto.jetsetcraft.movement;

/**
 * Stable street-dance vocabulary. A dance family controls the movement language while individual
 * move IDs drive scoring, HUD callouts and twenty-eight individually addressable full-body clips.
 */
public final class DanceCatalog {
    public record Move(int id, DanceStyle style, int animationIndex, int duration,
                       int basePoints, float multiplierGain, float boostReward) {}

    private static final String[][] NAMES = {
            {"Indian Step", "Cross Step", "Salsa Step", "Kick Step"},
            {"Fresno", "Robot", "Tutting", "Body Wave"},
            {"Jack", "Shuffle", "Loose Legs", "Skate Step"},
            {"Six-Step", "Backspin", "Windmill", "Flare", "Swipe", "Halo", "Headspin", "Airflare"},
            {"Running Man", "Bart Simpson", "Criss Cross", "Reebok"},
            {"Lock", "Scooby Doo", "Uncle Sam", "Skeeter Rabbit"}
    };
    private static final int[] OFFSETS = {0, 4, 8, 12, 20, 24};
    public static final int MOVE_COUNT = 28;

    public static Move select(DanceStyle style, int chain, float forward, float strafe) {
        int count = NAMES[style.id()].length;
        int direction = directionSlot(forward, strafe);
        int slot = Math.floorMod(chain + direction, count);
        return byId(OFFSETS[style.id()] + slot);
    }

    public static Move byId(int id) {
        int safe = Math.floorMod(id, MOVE_COUNT);
        DanceStyle style = DanceStyle.TOPROCK;
        int slot = safe;
        for (DanceStyle candidate : DanceStyle.values()) {
            int offset = OFFSETS[candidate.id()];
            int count = NAMES[candidate.id()].length;
            if (safe >= offset && safe < offset + count) {
                style = candidate;
                slot = safe - offset;
                break;
            }
        }
        int animation = safe;
        int duration = 28 + (slot % 4) * 4 + (style == DanceStyle.BREAKING ? 4 : 0);
        int points = 120 + style.id() * 12 + slot * 18 + (style == DanceStyle.BREAKING ? 55 : 0);
        float multiplier = 0.055f + slot * 0.012f + (style == DanceStyle.BREAKING ? 0.035f : 0.0f);
        float boost = 1.2f + slot * 0.32f + (style == DanceStyle.BREAKING ? 1.2f : 0.0f);
        return new Move(safe, style, animation, duration, points, multiplier, boost);
    }

    public static String name(int id) {
        Move move = byId(id);
        return NAMES[move.style().id()][move.id() - OFFSETS[move.style().id()]];
    }

    public static int countFor(DanceStyle style) {
        return NAMES[style.id()].length;
    }


    private static int directionSlot(float forward, float strafe) {
        if (Math.abs(strafe) > Math.abs(forward) && Math.abs(strafe) > 0.20f) return strafe > 0.0f ? 1 : 3;
        if (forward > 0.20f) return 0;
        if (forward < -0.20f) return 2;
        return 0;
    }

    private DanceCatalog() {}
}
