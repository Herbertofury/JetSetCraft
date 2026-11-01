package com.herberto.jetsetcraft.network;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

/** Private, bounded per-player Rush state. Album and roster pages are emitted only on request. */
public record S2CRushStatePacket(CompoundTag state) {
    public S2CRushStatePacket {
        state = state == null ? new CompoundTag() : state.copy();
    }

    public static void encode(S2CRushStatePacket packet, FriendlyByteBuf buffer) {
        buffer.writeNbt(packet.state);
    }

    public static S2CRushStatePacket decode(FriendlyByteBuf buffer) {
        // FriendlyByteBuf's ordinary NBT reader applies its standard allocation limit.
        return new S2CRushStatePacket(buffer.readNbt());
    }

    public static void handle(S2CRushStatePacket packet, Supplier<NetworkEvent.Context> supplier) {
        NetworkEvent.Context context = supplier.get();
        context.enqueueWork(() -> DistExecutor.unsafeRunWhenOn(Dist.CLIENT,
                () -> () -> com.herberto.jetsetcraft.client.rush.RivalRushClient.accept(packet.state)));
        context.setPacketHandled(true);
    }
}
