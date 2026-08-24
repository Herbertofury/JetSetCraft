package com.herberto.jetsetcraft.network;

import com.herberto.jetsetcraft.mob.MobRideRig;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

/** One compact server-authoritative snapshot for a tracked mob's physical Street Gear. */
public record S2CMobGearPacket(int entityId, ItemStack stack, MobRideRig rig) {
    public static void encode(S2CMobGearPacket packet, FriendlyByteBuf buffer) {
        buffer.writeVarInt(packet.entityId);
        buffer.writeItem(packet.stack);
        buffer.writeVarInt(packet.rig.ordinal());
    }

    public static S2CMobGearPacket decode(FriendlyByteBuf buffer) {
        return new S2CMobGearPacket(buffer.readVarInt(), buffer.readItem(),
                MobRideRig.byNetworkId(buffer.readVarInt()));
    }

    public static void handle(S2CMobGearPacket packet, Supplier<NetworkEvent.Context> supplier) {
        NetworkEvent.Context context = supplier.get();
        context.enqueueWork(() -> DistExecutor.unsafeRunWhenOn(Dist.CLIENT,
                () -> () -> com.herberto.jetsetcraft.client.ClientPacketHandlers.accept(packet)));
        context.setPacketHandled(true);
    }
}
