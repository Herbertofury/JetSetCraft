package com.herberto.jetsetcraft.network;

import com.herberto.jetsetcraft.client.state.ClientRideState;
import com.herberto.jetsetcraft.data.JetSetData;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;
import java.util.function.Supplier;

public record S2CStatePacket(int entityId, int styleId, boolean active, float boost, int comboScore,
                             float comboMultiplier, boolean grinding, int grindKindId, boolean wallRiding, boolean manual,
                             boolean boosting, boolean powersliding, float wallSide,
                             int trickIndex, int trickTicks, float momentum) {
    public S2CStatePacket(int id, JetSetData d) {
        this(id, d.style().id(), d.active(), d.boost(), d.comboScore(), d.comboMultiplier(), d.grinding(),
                d.grindKind().id(), d.wallRiding(), d.manual(), d.boosting(), d.powersliding(), d.wallSide(),
                d.trickIndex(), d.trickTicks(), (float)d.momentum());
    }
    public static void encode(S2CStatePacket p, FriendlyByteBuf b) {
        b.writeVarInt(p.entityId); b.writeVarInt(p.styleId); b.writeBoolean(p.active); b.writeFloat(p.boost);
        b.writeVarInt(p.comboScore); b.writeFloat(p.comboMultiplier); b.writeBoolean(p.grinding); b.writeVarInt(p.grindKindId);
        b.writeBoolean(p.wallRiding); b.writeBoolean(p.manual); b.writeBoolean(p.boosting);
        b.writeBoolean(p.powersliding); b.writeFloat(p.wallSide);
        b.writeVarInt(p.trickIndex); b.writeVarInt(p.trickTicks); b.writeFloat(p.momentum);
    }
    public static S2CStatePacket decode(FriendlyByteBuf b) {
        return new S2CStatePacket(b.readVarInt(), b.readVarInt(), b.readBoolean(), b.readFloat(), b.readVarInt(),
                b.readFloat(), b.readBoolean(), b.readVarInt(), b.readBoolean(), b.readBoolean(), b.readBoolean(),
                b.readBoolean(), b.readFloat(), b.readVarInt(), b.readVarInt(), b.readFloat());
    }
    public static void handle(S2CStatePacket p, Supplier<NetworkEvent.Context> supplier) {
        NetworkEvent.Context ctx = supplier.get();
        ctx.enqueueWork(() -> ClientRideState.accept(p));
        ctx.setPacketHandled(true);
    }
}
