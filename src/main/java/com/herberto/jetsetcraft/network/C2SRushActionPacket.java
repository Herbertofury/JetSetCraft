package com.herberto.jetsetcraft.network;

import com.herberto.jetsetcraft.rush.RivalRushManager;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkEvent;

import java.util.Set;
import java.util.UUID;
import java.util.function.Supplier;

/** Bounded intent only. Run identity, offers, rewards and elapsed time remain server-owned. */
public record C2SRushActionPacket(String action, String value, int number, UUID runId, int version) {
    private static final Set<String> ACTIONS = Set.of("menu", "start", "leave", "choose", "encore", "album", "roster",
            "journey_choose", "arena_catalogue");

    public static void encode(C2SRushActionPacket packet, FriendlyByteBuf buffer) {
        buffer.writeUtf(packet.action, 32);
        buffer.writeUtf(packet.value, 256);
        buffer.writeVarInt(packet.number);
        buffer.writeBoolean(packet.runId != null);
        if (packet.runId != null) buffer.writeUUID(packet.runId);
        buffer.writeVarInt(packet.version);
    }

    public static C2SRushActionPacket decode(FriendlyByteBuf buffer) {
        String action = buffer.readUtf(32);
        String value = buffer.readUtf(256);
        int number = buffer.readVarInt();
        UUID runId = buffer.readBoolean() ? buffer.readUUID() : null;
        return new C2SRushActionPacket(action, value, number, runId, buffer.readVarInt());
    }

    public static void handle(C2SRushActionPacket packet, Supplier<NetworkEvent.Context> supplier) {
        NetworkEvent.Context context = supplier.get();
        context.enqueueWork(() -> {
            ServerPlayer player = context.getSender();
            if (player == null || !ACTIONS.contains(packet.action)) return;
            RivalRushManager.handleAction(player, packet.action, packet.value, packet.number, packet.runId, packet.version);
        });
        context.setPacketHandled(true);
    }
}
