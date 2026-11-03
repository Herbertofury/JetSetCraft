package com.herberto.jetsetcraft.network;

import com.herberto.jetsetcraft.liberty.LibertyManager;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkEvent;

import java.util.Set;
import java.util.UUID;
import java.util.function.Supplier;

/** Bounded choices only. Mission locations, objective state, costs and awards are server-owned. */
public record C2SLibertyActionPacket(String action, String value, UUID missionId, long revision) {
    private static final Set<String> ACTIONS = Set.of("open", "deploy", "extract", "board", "abort",
            "resupply", "research", "research_select", "armory_select", "ship_purchase", "ship_equip",
            "campaign_deploy", "defense_deploy", "ship", "callin", "terminal", "reinforce");

    public static void encode(C2SLibertyActionPacket packet, FriendlyByteBuf buffer) {
        buffer.writeUtf(packet.action, 32);
        buffer.writeUtf(packet.value, 128);
        buffer.writeBoolean(packet.missionId != null);
        if (packet.missionId != null) buffer.writeUUID(packet.missionId);
        buffer.writeVarLong(packet.revision);
    }

    public static C2SLibertyActionPacket decode(FriendlyByteBuf buffer) {
        String action = buffer.readUtf(32), value = buffer.readUtf(128);
        UUID mission = buffer.readBoolean() ? buffer.readUUID() : null;
        return new C2SLibertyActionPacket(action, value, mission, buffer.readVarLong());
    }

    public static void handle(C2SLibertyActionPacket packet, Supplier<NetworkEvent.Context> supplier) {
        NetworkEvent.Context context = supplier.get();
        context.enqueueWork(() -> handleForPlayer(packet, context.getSender()));
        context.setPacketHandled(true);
    }

    /** Same validated production boundary used by dedicated-server regression tests. */
    public static void handleForPlayer(C2SLibertyActionPacket packet, ServerPlayer player) {
        if (player == null || packet == null || packet.action == null || !ACTIONS.contains(packet.action)
                || packet.value == null || packet.value.length() > 128 || packet.revision < 0) return;
        LibertyManager.handleAction(player, packet.action, packet.value, packet.missionId, packet.revision);
    }
}
