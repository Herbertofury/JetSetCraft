package com.herberto.jetsetcraft.network;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

/** Private server snapshot of a bounded crew house; never a source of client reward authority. */
public record S2CCrewStatePacket(CompoundTag state) {
    public S2CCrewStatePacket { state = state == null ? new CompoundTag() : state.copy(); }

    public static void encode(S2CCrewStatePacket packet, FriendlyByteBuf buffer) { buffer.writeNbt(packet.state); }
    public static S2CCrewStatePacket decode(FriendlyByteBuf buffer) { return new S2CCrewStatePacket(buffer.readNbt()); }

    public static void handle(S2CCrewStatePacket packet, Supplier<NetworkEvent.Context> supplier) {
        NetworkEvent.Context context = supplier.get();
        context.enqueueWork(() -> DistExecutor.unsafeRunWhenOn(Dist.CLIENT,
                () -> () -> com.herberto.jetsetcraft.client.crew.CrewHouseClient.accept(packet.state)));
        context.setPacketHandled(true);
    }
}
