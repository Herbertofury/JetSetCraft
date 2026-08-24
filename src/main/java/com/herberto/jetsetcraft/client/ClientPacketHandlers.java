package com.herberto.jetsetcraft.client;

import com.herberto.jetsetcraft.client.state.ClientMobGearState;
import com.herberto.jetsetcraft.client.state.ClientRideState;
import com.herberto.jetsetcraft.network.S2CMobGearPacket;
import com.herberto.jetsetcraft.network.S2CStatePacket;
import com.herberto.jetsetcraft.client.screen.GraffitiSelectorScreen;
import net.minecraft.client.Minecraft;
import net.minecraft.world.InteractionHand;

/** Client-only packet destinations kept outside common networking classes for dedicated-server safety. */
public final class ClientPacketHandlers {
    public static void accept(S2CStatePacket packet) {
        ClientRideState.accept(packet);
    }

    public static void accept(S2CMobGearPacket packet) {
        ClientMobGearState.accept(packet);
    }

    public static void openGraffitiSelector(InteractionHand hand) {
        Minecraft.getInstance().setScreen(new GraffitiSelectorScreen(hand));
    }

    private ClientPacketHandlers() {}
}
