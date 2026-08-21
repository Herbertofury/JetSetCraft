package com.herberto.jetsetcraft.network;

import com.herberto.jetsetcraft.data.JetSetDataProvider;
import com.herberto.jetsetcraft.item.RideLoadout;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

/** Toggle the dedicated ride slot, or retrieve the equipped item with Shift+Ride Toggle. */
public record C2SRideLoadoutPacket(boolean unequip) {
    public static void encode(C2SRideLoadoutPacket packet, FriendlyByteBuf buf) { buf.writeBoolean(packet.unequip); }
    public static C2SRideLoadoutPacket decode(FriendlyByteBuf buf) { return new C2SRideLoadoutPacket(buf.readBoolean()); }

    public static void handle(C2SRideLoadoutPacket packet, Supplier<NetworkEvent.Context> supplier) {
        NetworkEvent.Context ctx = supplier.get();
        ctx.enqueueWork(() -> {
            ServerPlayer player = ctx.getSender();
            if (player == null) return;
            player.getCapability(JetSetDataProvider.CAPABILITY).ifPresent(data -> {
                if (packet.unequip) RideLoadout.unequip(player, data);
                else RideLoadout.toggle(player, data);
            });
        });
        ctx.setPacketHandled(true);
    }
}
