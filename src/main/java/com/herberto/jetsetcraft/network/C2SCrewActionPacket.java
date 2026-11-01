package com.herberto.jetsetcraft.network;

import com.herberto.jetsetcraft.crew.CrewHouseManager;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkEvent;

import java.util.Set;
import java.util.UUID;
import java.util.function.Supplier;

/** Bounded player intent; the server owns home candidates, members, costs, jobs and rewards. */
public record C2SCrewActionPacket(String action, String value, int number, UUID houseId, UUID targetId, long revision) {
    private static final Set<String> ACTIONS = Set.of("open", "refresh", "found", "relocate", "rename", "preview",
            "expand", "recruit", "rename_member", "focus", "focus_off", "assign", "claim", "cancel", "facility",
            "upgrade", "preparation");

    public static void encode(C2SCrewActionPacket packet, FriendlyByteBuf buffer) {
        buffer.writeUtf(packet.action, 32);
        buffer.writeUtf(packet.value, 128);
        buffer.writeVarInt(packet.number);
        buffer.writeBoolean(packet.houseId != null);
        if (packet.houseId != null) buffer.writeUUID(packet.houseId);
        buffer.writeBoolean(packet.targetId != null);
        if (packet.targetId != null) buffer.writeUUID(packet.targetId);
        buffer.writeVarLong(packet.revision);
    }

    public static C2SCrewActionPacket decode(FriendlyByteBuf buffer) {
        String action = buffer.readUtf(32);
        String value = buffer.readUtf(128);
        int number = buffer.readVarInt();
        UUID house = buffer.readBoolean() ? buffer.readUUID() : null;
        UUID target = buffer.readBoolean() ? buffer.readUUID() : null;
        return new C2SCrewActionPacket(action, value, number, house, target, buffer.readVarLong());
    }

    public static void handle(C2SCrewActionPacket packet, Supplier<NetworkEvent.Context> supplier) {
        NetworkEvent.Context context = supplier.get();
        context.enqueueWork(() -> {
            ServerPlayer player = context.getSender();
            if (player == null || !ACTIONS.contains(packet.action) || packet.revision < 0L) return;
            CrewHouseManager.handleAction(player, packet.action, packet.value, packet.number,
                    packet.houseId, packet.targetId, packet.revision);
        });
        context.setPacketHandled(true);
    }
}
