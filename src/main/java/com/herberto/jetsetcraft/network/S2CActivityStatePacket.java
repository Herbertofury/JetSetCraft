package com.herberto.jetsetcraft.network;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

/** Private authoritative activity preview, progress and result for the requesting player. */
public record S2CActivityStatePacket(CompoundTag state) {
    public S2CActivityStatePacket {
        state = state == null ? new CompoundTag() : state.copy();
    }

    public static void encode(S2CActivityStatePacket packet, FriendlyByteBuf buffer) {
        buffer.writeNbt(packet.state);
    }

    public static S2CActivityStatePacket decode(FriendlyByteBuf buffer) {
        return new S2CActivityStatePacket(buffer.readNbt());
    }

    public static void handle(S2CActivityStatePacket packet, Supplier<NetworkEvent.Context> supplier) {
        NetworkEvent.Context context = supplier.get();
        context.enqueueWork(() -> DistExecutor.unsafeRunWhenOn(Dist.CLIENT,
                () -> () -> com.herberto.jetsetcraft.client.activity.ActivityClient.accept(packet.state)));
        context.setPacketHandled(true);
    }
}
