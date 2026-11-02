package com.herberto.jetsetcraft.network;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

/** A private mission and research snapshot for its owner; ordinary NBT allocation limits apply. */
public record S2CLibertyStatePacket(CompoundTag state) {
    public S2CLibertyStatePacket {
        state = state == null ? new CompoundTag() : state.copy();
    }

    public static void encode(S2CLibertyStatePacket packet, FriendlyByteBuf buffer) { buffer.writeNbt(packet.state); }
    public static S2CLibertyStatePacket decode(FriendlyByteBuf buffer) { return new S2CLibertyStatePacket(buffer.readNbt()); }

    public static void handle(S2CLibertyStatePacket packet, Supplier<NetworkEvent.Context> supplier) {
        NetworkEvent.Context context = supplier.get();
        context.enqueueWork(() -> DistExecutor.unsafeRunWhenOn(Dist.CLIENT,
                () -> () -> com.herberto.jetsetcraft.client.liberty.LibertyClient.accept(packet.state)));
        context.setPacketHandled(true);
    }
}
