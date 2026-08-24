package com.herberto.jetsetcraft.client;

import com.herberto.jetsetcraft.client.state.ClientMobGearState;
import com.herberto.jetsetcraft.client.state.ClientRideState;
import com.herberto.jetsetcraft.network.S2CMobGearPacket;
import com.herberto.jetsetcraft.network.S2CStatePacket;

/** Client-only packet destinations kept outside common networking classes for dedicated-server safety. */
public final class ClientPacketHandlers {
    public static void accept(S2CStatePacket packet) {
        ClientRideState.accept(packet);
    }

    public static void accept(S2CMobGearPacket packet) {
        ClientMobGearState.accept(packet);
    }

    private ClientPacketHandlers() {}
}
