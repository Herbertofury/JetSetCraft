package com.herberto.jetsetcraft.client.state;

import com.herberto.jetsetcraft.movement.DanceStyle;
import com.herberto.jetsetcraft.movement.GrindKind;
import com.herberto.jetsetcraft.movement.RideStyle;
import com.herberto.jetsetcraft.network.S2CStatePacket;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/** Sanitized client mirror of server-authoritative ride state. */
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

    private static final Map<Integer, Snapshot> STATES = new ConcurrentHashMap<>();

    public static void accept(S2CStatePacket packet) {
        if (packet == null || packet.entityId() < 0) return;
        RideStyle style = RideStyle.byId(packet.styleId());
        boolean grinding = packet.grinding();
        STATES.put(packet.entityId(), new Snapshot(style, packet.active() && style != RideStyle.NONE,
                finiteClamp(packet.boost(), 0.0F, 100.0F), Math.max(0, packet.comboScore()),
                finiteClamp(packet.comboMultiplier(), 1.0F, 20.0F), grinding,
                grinding ? GrindKind.byId(packet.grindKindId()) : GrindKind.NONE,
                packet.wallRiding(), packet.manual(), packet.boosting(), packet.powersliding(),
                finiteClamp(packet.wallSide(), -1.0F, 1.0F), Math.max(0, packet.trickIndex()),
                Math.max(0, packet.trickTicks()), packet.boostTrick(), finiteNonNegative(packet.momentum()),
                finiteClamp(packet.flow(), 0.0F, 100.0F), packet.groundStunt(),
                Math.max(0, Math.min(3, packet.landingGrade())), Math.max(0, packet.landingTicks()),
                packet.dancing(), DanceStyle.byId(packet.danceStyle()), Math.max(0, packet.danceMoveId()),
                Math.max(0, packet.danceTicks()), Math.max(0, packet.danceChain()),
                Math.max(0, Math.min(16, packet.cypherSize()))));
    }

    public static Snapshot get(int entityId) { return STATES.getOrDefault(entityId, Snapshot.EMPTY); }
    public static void remove(int entityId) { STATES.remove(entityId); }
    public static void reset() { STATES.clear(); }

    private static float finiteNonNegative(float value) {
        return Float.isFinite(value) ? Math.max(0.0F, value) : 0.0F;
    }

    private static float finiteClamp(float value, float min, float max) {
        if (!Float.isFinite(value)) return min <= 0.0F && max >= 0.0F ? 0.0F : min;
        return Math.max(min, Math.min(max, value));
    }

    private ClientRideState() {}
}
