package com.herberto.jetsetcraft.network;

import com.herberto.jetsetcraft.JetSetCraft;
import com.herberto.jetsetcraft.data.JetSetData;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkDirection;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.PacketDistributor;
import net.minecraftforge.network.simple.SimpleChannel;

public final class JetSetNetwork {
    private static final String PROTOCOL = "4";
    private static final SimpleChannel CHANNEL = NetworkRegistry.ChannelBuilder
            .named(new ResourceLocation(JetSetCraft.MOD_ID, "main"))
            .networkProtocolVersion(() -> PROTOCOL).clientAcceptedVersions(PROTOCOL::equals).serverAcceptedVersions(PROTOCOL::equals)
            .simpleChannel();

    public static void init() {
        int id = 0;
        CHANNEL.messageBuilder(C2SInputPacket.class, id++, NetworkDirection.PLAY_TO_SERVER)
                .encoder(C2SInputPacket::encode).decoder(C2SInputPacket::decode).consumerMainThread(C2SInputPacket::handle).add();
        CHANNEL.messageBuilder(C2SRideLoadoutPacket.class, id++, NetworkDirection.PLAY_TO_SERVER)
                .encoder(C2SRideLoadoutPacket::encode).decoder(C2SRideLoadoutPacket::decode)
                .consumerMainThread(C2SRideLoadoutPacket::handle).add();
        CHANNEL.messageBuilder(S2CStatePacket.class, id, NetworkDirection.PLAY_TO_CLIENT)
                .encoder(S2CStatePacket::encode).decoder(S2CStatePacket::decode).consumerMainThread(S2CStatePacket::handle).add();
    }
    public static void sendInput(int mask, float forward, float strafe) { CHANNEL.sendToServer(new C2SInputPacket(mask, forward, strafe)); }
    public static void sendRideLoadoutAction(boolean unequip) { CHANNEL.sendToServer(new C2SRideLoadoutPacket(unequip)); }
    public static void sync(ServerPlayer player, JetSetData data) {
        S2CStatePacket p = new S2CStatePacket(player.getId(), data);
        CHANNEL.send(PacketDistributor.PLAYER.with(() -> player), p);
        CHANNEL.send(PacketDistributor.TRACKING_ENTITY.with(() -> player), p);
    }
    private JetSetNetwork() {}
}
