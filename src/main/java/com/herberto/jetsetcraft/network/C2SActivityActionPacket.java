package com.herberto.jetsetcraft.network;

import com.herberto.jetsetcraft.activity.ActivityManager;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkEvent;

import java.util.Set;
import java.util.UUID;
import java.util.function.Supplier;

/** A mode choice and server-issued context only; never accepts positions, progress or rewards. */
public record C2SActivityActionPacket(String action, String mode, UUID contextId, int version) {
    private static final Set<String> ACTIONS = Set.of("open", "refresh", "select", "launch", "cancel");

    public static void encode(C2SActivityActionPacket packet, FriendlyByteBuf buffer) {
        buffer.writeUtf(packet.action, 32);
        buffer.writeUtf(packet.mode, 64);
        buffer.writeBoolean(packet.contextId != null);
        if (packet.contextId != null) buffer.writeUUID(packet.contextId);
        buffer.writeVarInt(packet.version);
    }

    public static C2SActivityActionPacket decode(FriendlyByteBuf buffer) {
        String action = buffer.readUtf(32);
        String mode = buffer.readUtf(64);
        UUID context = buffer.readBoolean() ? buffer.readUUID() : null;
        return new C2SActivityActionPacket(action, mode, context, buffer.readVarInt());
    }

    public static void handle(C2SActivityActionPacket packet, Supplier<NetworkEvent.Context> supplier) {
        NetworkEvent.Context context = supplier.get();
        context.enqueueWork(() -> {
            ServerPlayer player = context.getSender();
            if (player == null || !ACTIONS.contains(packet.action)) return;
            ActivityManager.handleAction(player, packet.action, packet.mode, packet.contextId, packet.version);
        });
        context.setPacketHandled(true);
    }
}
