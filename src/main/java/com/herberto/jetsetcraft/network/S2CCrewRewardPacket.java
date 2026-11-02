package com.herberto.jetsetcraft.network;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

/** Owner-only notification of an already committed server award; carries no request or payout authority. */
public record S2CCrewRewardPacket(CompoundTag receipt) {
    public S2CCrewRewardPacket { receipt = receipt == null ? new CompoundTag() : receipt.copy(); }
    public static void encode(S2CCrewRewardPacket packet, FriendlyByteBuf buffer) { buffer.writeNbt(packet.receipt); }
    public static S2CCrewRewardPacket decode(FriendlyByteBuf buffer) { return new S2CCrewRewardPacket(buffer.readNbt()); }
    public static void handle(S2CCrewRewardPacket packet, Supplier<NetworkEvent.Context> supplier) {
        NetworkEvent.Context context = supplier.get();
        context.enqueueWork(() -> DistExecutor.unsafeRunWhenOn(Dist.CLIENT,
                () -> () -> com.herberto.jetsetcraft.client.crew.CrewRewardFeedback.accept(packet.receipt)));
        context.setPacketHandled(true);
    }
}
