package com.herberto.jetsetcraft.network;

import com.herberto.jetsetcraft.data.JetSetDataProvider;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkEvent;
import java.util.function.Supplier;

public record C2SInputPacket(int mask, float forward, float strafe) {
    public static void encode(C2SInputPacket p, FriendlyByteBuf b) { b.writeVarInt(p.mask); b.writeFloat(p.forward); b.writeFloat(p.strafe); }
    public static C2SInputPacket decode(FriendlyByteBuf b) { return new C2SInputPacket(b.readVarInt(), b.readFloat(), b.readFloat()); }
    public static void handle(C2SInputPacket p, Supplier<NetworkEvent.Context> supplier) {
        NetworkEvent.Context ctx = supplier.get();
        ctx.enqueueWork(() -> {
            ServerPlayer player = ctx.getSender();
            if (player != null) player.getCapability(JetSetDataProvider.CAPABILITY)
                    .ifPresent(data -> data.acceptInput(p.mask, p.forward, p.strafe));
        });
        ctx.setPacketHandled(true);
    }
}
