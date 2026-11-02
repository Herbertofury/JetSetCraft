package com.herberto.jetsetcraft.rush;

import net.minecraft.world.phys.Vec3;

/** Observed server movement for run-only abilities. Pauses and discontinuities never manufacture a landing. */
public final class RushMotionTracker {
    public record Step(double ridingDistance, boolean landed, boolean airborneReady) { }

    private Vec3 previous;
    private int airborneTicks;
    private double airborneDistance, departureY, verticalTravel;
    private boolean ready;

    public boolean airborneReady() { return ready; }

    public void reset(Vec3 position) {
        previous = finite(position) ? position : null;
        clearAir();
    }

    public Step sample(Vec3 position, boolean grounded, boolean riding, boolean combat) {
        if (!finite(position)) { reset(null); return new Step(0, false, false); }
        Vec3 last = previous;
        previous = position;
        if (last == null) { clearAir(); return new Step(0, false, false); }
        Vec3 delta = position.subtract(last);
        double distance = delta.horizontalDistance();
        if (!riding || distance > 4 || Math.abs(delta.y) > 4) {
            clearAir(); return new Step(0, false, false);
        }
        if (!combat) { clearAir(); return new Step(distance, false, false); }
        if (!grounded) {
            if (airborneTicks == 0) { departureY = last.y; airborneDistance = 0; verticalTravel = 0; }
            airborneTicks = Math.min(1200, airborneTicks + 1);
            airborneDistance = Math.min(64, airborneDistance + distance);
            verticalTravel = Math.max(verticalTravel, Math.abs(position.y - departureY));
            ready = airborneTicks >= 6 && airborneDistance >= 1.5 && verticalTravel >= .6;
            return new Step(distance, false, ready);
        }
        boolean landed = ready;
        clearAir();
        return new Step(distance, landed, false);
    }

    private void clearAir() {
        airborneTicks = 0; airborneDistance = 0; verticalTravel = 0; ready = false;
    }

    private static boolean finite(Vec3 position) {
        return position != null && Double.isFinite(position.x) && Double.isFinite(position.y) && Double.isFinite(position.z);
    }
}
