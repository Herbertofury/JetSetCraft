package com.herberto.jetsetcraft.client.state;

import com.herberto.jetsetcraft.movement.DanceStyle;
import com.herberto.jetsetcraft.movement.GrindKind;
import com.herberto.jetsetcraft.movement.RideStyle;
import com.herberto.jetsetcraft.network.S2CStatePacket;

import java.util.HashMap;
import java.util.Map;

public final class ClientRideState {
    public record Snapshot(RideStyle style, boolean active, float boost, int comboScore, float comboMultiplier,
                           boolean grinding, GrindKind grindKind, boolean wallRiding, boolean manual, boolean boosting,
                           boolean powersliding, float wallSide, int trickIndex, int trickTicks, boolean boostTrick,
                           float momentum, float flow, boolean groundStunt, int landingGrade, int landingTicks,
                           boolean dancing, DanceStyle danceStyle, int danceMoveId, int danceTicks,
                           int danceChain, int cypherSize) {
        public static final Snapshot EMPTY = new Snapshot(RideStyle.NONE, false, 0f, 0, 1f,
                false, GrindKind.NONE, false, false, false, false, 0f, 0, 0, false,
                0f, 0f, false, 0, 0, false, DanceStyle.BREAKING, 0, 0, 0, 0);
    }

    private static final Map<Integer, Snapshot> STATES = new HashMap<>();

    public static void accept(S2CStatePacket packet) {
        STATES.put(packet.entityId(), new Snapshot(RideStyle.byId(packet.styleId()), packet.active(), packet.boost(),
                packet.comboScore(), packet.comboMultiplier(), packet.grinding(), GrindKind.byId(packet.grindKindId()),
                packet.wallRiding(), packet.manual(), packet.boosting(), packet.powersliding(), packet.wallSide(),
                packet.trickIndex(), packet.trickTicks(), packet.boostTrick(), packet.momentum(), packet.flow(),
                packet.groundStunt(), packet.landingGrade(), packet.landingTicks(), packet.dancing(),
                DanceStyle.byId(packet.danceStyle()), packet.danceMoveId(), packet.danceTicks(), packet.danceChain(),
                packet.cypherSize()));
    }

    public static Snapshot get(int entityId) { return STATES.getOrDefault(entityId, Snapshot.EMPTY); }
    public static void remove(int entityId) { STATES.remove(entityId); }
    public static void reset() { STATES.clear(); }
    private ClientRideState() {}
}
