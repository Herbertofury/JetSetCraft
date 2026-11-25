package com.herberto.jetsetcraft.gang;

/** Pure selection boundary for the pending server-owned Honkistas event director.
 * This does not spawn actors, grant rewards, clear defeat state, or request media.
 * The caller must persist its one event decision before spawning and must not reroll on reload.
 */
public final class HonkistasVariantPolicy {
    public static final String HONKISTAS = "jetsetcraft:honkistas";
    public static final String GRINIONS = "jetsetcraft:grinions";
    public static final String AMYGDALA = "jetsetcraft:amygdala";

    public record Context(String eventGangId, boolean serverAuthoritative,
                          boolean honkistasEventAdmitted, boolean eventDecisionAlreadyRecorded,
                          boolean parentDefeated, boolean grinionsDefeated,
                          boolean grinionsAssetsReady) {}

    /** Rate is an explicit director input, not an invented owner-approved balance value.
     * Invalid inputs fail closed; there is no random source or recursive event creation here.
     */
    public static String select(Context context, double probability, double roll) {
        if (context == null) return HONKISTAS;
        String original = context.eventGangId();
        if (!HONKISTAS.equals(original)) return original;
        if (!context.serverAuthoritative() || !context.honkistasEventAdmitted()
                || context.eventDecisionAlreadyRecorded() || context.parentDefeated()
                || context.grinionsDefeated() || !context.grinionsAssetsReady()
                || !Double.isFinite(probability) || probability <= 0 || probability >= 0.5
                || !Double.isFinite(roll) || roll < 0 || roll >= 1) return original;
        return roll < probability ? GRINIONS : original;
    }

    private HonkistasVariantPolicy() {}
}
