package com.herberto.jetsetcraft.client.state;

import com.herberto.jetsetcraft.movement.GrindKind;
import com.herberto.jetsetcraft.movement.RideStyle;
import com.herberto.jetsetcraft.network.S2CStatePacket;
import java.util.HashMap;
import java.util.Map;

public final class ClientRideState {
    public record Snapshot(RideStyle style, boolean active, float boost, int comboScore, float comboMultiplier,
                           boolean grinding, GrindKind grindKind, boolean wallRiding, boolean manual, boolean boosting,
                           boolean powersliding, float wallSide,
                           int trickIndex, int trickTicks, float momentum) {
        public static final Snapshot EMPTY = new Snapshot(RideStyle.NONE, false, 0f, 0, 1f,
                false, GrindKind.NONE, false, false, false, false, 0f, 0, 0, 0f);
    }
    private static final Map<Integer, Snapshot> STATES = new HashMap<>();
    public static void accept(S2CStatePacket p) {
        STATES.put(p.entityId(), new Snapshot(RideStyle.byId(p.styleId()), p.active(), p.boost(), p.comboScore(),
                p.comboMultiplier(), p.grinding(), GrindKind.byId(p.grindKindId()), p.wallRiding(), p.manual(),
                p.boosting(), p.powersliding(), p.wallSide(), p.trickIndex(), p.trickTicks(), p.momentum()));
    }
    public static Snapshot get(int id) { return STATES.getOrDefault(id, Snapshot.EMPTY); }
    public static void remove(int id) { STATES.remove(id); }
    public static void reset() { STATES.clear(); }
    private ClientRideState() {}
}
