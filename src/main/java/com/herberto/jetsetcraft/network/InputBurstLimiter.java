package com.herberto.jetsetcraft.network;

/**
 * Per-player transport guard for JetSetCraft client input samples.
 *
 * <p>The stock client emits at most one changed sample per client tick plus sparse heartbeats. The
 * four-sample budget intentionally leaves headroom for jitter/reordering while preventing packet
 * bursts from repeatedly mutating server-authoritative movement state in one tick. Once that budget
 * is exhausted, one fully neutral sample is still accepted so a release can never be starved and
 * leave boost/grind/manual/dance controls latched.</p>
 */
public final class InputBurstLimiter {
    public static final int MAX_SAMPLES_PER_TICK = 4;
    private static final float NEUTRAL_EPSILON = 1.0e-4F;

    private long windowTick = Long.MIN_VALUE;
    private int acceptedSamples;
    private boolean safetyReleaseAccepted;

    public boolean allow(long serverTick, int mask, float forward, float strafe) {
        if (serverTick != windowTick) {
            windowTick = serverTick;
            acceptedSamples = 0;
            safetyReleaseAccepted = false;
        }

        if (acceptedSamples < MAX_SAMPLES_PER_TICK) {
            acceptedSamples++;
            return true;
        }

        if (!safetyReleaseAccepted && isNeutralRelease(mask, forward, strafe)) {
            safetyReleaseAccepted = true;
            return true;
        }
        return false;
    }

    static boolean isNeutralRelease(int mask, float forward, float strafe) {
        return (mask & InputFlags.ALL) == 0
                && Float.isFinite(forward)
                && Float.isFinite(strafe)
                && Math.abs(forward) <= NEUTRAL_EPSILON
                && Math.abs(strafe) <= NEUTRAL_EPSILON;
    }
}
