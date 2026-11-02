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
            "journey_choose", "arena_catalogue", "remix", "reroll", "banish", "evolve");

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
        context.enqueueWork(() -> handleForPlayer(packet, context.getSender()));
        context.setPacketHandled(true);
    }

    /** Production intent boundary, also exercised directly by the dedicated-server regression tests. */
    public static void handleForPlayer(C2SRushActionPacket packet, ServerPlayer player) {
        if (player == null || packet == null || packet.action == null || !ACTIONS.contains(packet.action)
                || packet.value == null || packet.value.length() > 256 || packet.version < 0) return;
        RivalRushManager.handleAction(player, packet.action, packet.value, packet.number, packet.runId, packet.version);
    }
}
