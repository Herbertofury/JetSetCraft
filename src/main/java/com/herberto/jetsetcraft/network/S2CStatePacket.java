package com.herberto.jetsetcraft.network;

import com.herberto.jetsetcraft.data.JetSetData;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public record S2CStatePacket(int entityId, int styleId, boolean active, float boost, int comboScore,
                             float comboMultiplier, boolean grinding, int grindKindId, boolean wallRiding, boolean manual,
                             boolean boosting, boolean powersliding, float wallSide, int trickIndex, int trickTicks,
                             boolean boostTrick, float momentum, float flow, boolean groundStunt,
                             int landingGrade, int landingTicks, boolean dancing, int danceStyle, int danceMoveId,
                             int danceTicks, int danceChain, int cypherSize) {
    public S2CStatePacket(int id, JetSetData data) {
        this(id, data.style().id(), data.active(), data.boost(), data.comboScore(), data.comboMultiplier(),
                data.grinding(), data.grindKind().id(), data.wallRiding(), data.manual(), data.boosting(),
                data.powersliding(), data.wallSide(), data.trickIndex(), data.trickTicks(), data.boostTrick(),
                (float)data.momentum(), data.flow(), data.groundStunt(), data.landingGrade(), data.landingTicks(),
                data.dancing(), data.danceStyleId(), data.danceMoveId(), data.danceTicks(), data.danceChain(),
                data.cypherSize());
    }

    public static void encode(S2CStatePacket packet, FriendlyByteBuf buffer) {
        buffer.writeVarInt(packet.entityId);
        buffer.writeVarInt(packet.styleId);
        buffer.writeBoolean(packet.active);
        buffer.writeFloat(packet.boost);
        buffer.writeVarInt(packet.comboScore);
        buffer.writeFloat(packet.comboMultiplier);
        buffer.writeBoolean(packet.grinding);
        buffer.writeVarInt(packet.grindKindId);
        buffer.writeBoolean(packet.wallRiding);
        buffer.writeBoolean(packet.manual);
        buffer.writeBoolean(packet.boosting);
        buffer.writeBoolean(packet.powersliding);
        buffer.writeFloat(packet.wallSide);
        buffer.writeVarInt(packet.trickIndex);
        buffer.writeVarInt(packet.trickTicks);
        buffer.writeBoolean(packet.boostTrick);
        buffer.writeFloat(packet.momentum);
        buffer.writeFloat(packet.flow);
        buffer.writeBoolean(packet.groundStunt);
        buffer.writeVarInt(packet.landingGrade);
        buffer.writeVarInt(packet.landingTicks);
        buffer.writeBoolean(packet.dancing);
        buffer.writeVarInt(packet.danceStyle);
        buffer.writeVarInt(packet.danceMoveId);
        buffer.writeVarInt(packet.danceTicks);
        buffer.writeVarInt(packet.danceChain);
        buffer.writeVarInt(packet.cypherSize);
    }

    public static S2CStatePacket decode(FriendlyByteBuf buffer) {
        return new S2CStatePacket(buffer.readVarInt(), buffer.readVarInt(), buffer.readBoolean(), buffer.readFloat(),
                buffer.readVarInt(), buffer.readFloat(), buffer.readBoolean(), buffer.readVarInt(), buffer.readBoolean(),
                buffer.readBoolean(), buffer.readBoolean(), buffer.readBoolean(), buffer.readFloat(), buffer.readVarInt(),
                buffer.readVarInt(), buffer.readBoolean(), buffer.readFloat(), buffer.readFloat(), buffer.readBoolean(),
                buffer.readVarInt(), buffer.readVarInt(), buffer.readBoolean(), buffer.readVarInt(), buffer.readVarInt(),
                buffer.readVarInt(), buffer.readVarInt(), buffer.readVarInt());
    }

    public static void handle(S2CStatePacket packet, Supplier<NetworkEvent.Context> supplier) {
        NetworkEvent.Context context = supplier.get();
        context.enqueueWork(() -> DistExecutor.unsafeRunWhenOn(Dist.CLIENT,
                () -> () -> com.herberto.jetsetcraft.client.ClientPacketHandlers.accept(packet)));
        context.setPacketHandled(true);
    }
}
