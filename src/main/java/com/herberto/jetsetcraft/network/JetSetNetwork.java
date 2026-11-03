package com.herberto.jetsetcraft.network;

import com.herberto.jetsetcraft.JetSetCraft;
import com.herberto.jetsetcraft.data.JetSetData;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.InteractionHand;
import net.minecraftforge.network.NetworkDirection;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.PacketDistributor;
import net.minecraftforge.network.simple.SimpleChannel;

import java.util.UUID;

public final class JetSetNetwork {
    private static final String PROTOCOL = "18";
    private static final SimpleChannel CHANNEL = NetworkRegistry.ChannelBuilder
            .named(ResourceLocation.fromNamespaceAndPath(JetSetCraft.MOD_ID, "main"))
            .networkProtocolVersion(() -> PROTOCOL).clientAcceptedVersions(PROTOCOL::equals).serverAcceptedVersions(PROTOCOL::equals)
            .simpleChannel();

    public static void init() {
        int id = 0;
        CHANNEL.messageBuilder(C2SInputPacket.class, id++, NetworkDirection.PLAY_TO_SERVER)
                .encoder(C2SInputPacket::encode).decoder(C2SInputPacket::decode).consumerMainThread(C2SInputPacket::handle).add();
        CHANNEL.messageBuilder(C2SRideLoadoutPacket.class, id++, NetworkDirection.PLAY_TO_SERVER)
                .encoder(C2SRideLoadoutPacket::encode).decoder(C2SRideLoadoutPacket::decode)
                .consumerMainThread(C2SRideLoadoutPacket::handle).add();
        CHANNEL.messageBuilder(S2CStatePacket.class, id++, NetworkDirection.PLAY_TO_CLIENT)
                .encoder(S2CStatePacket::encode).decoder(S2CStatePacket::decode).consumerMainThread(S2CStatePacket::handle).add();
        CHANNEL.messageBuilder(S2CMobGearPacket.class, id++, NetworkDirection.PLAY_TO_CLIENT)
                .encoder(S2CMobGearPacket::encode).decoder(S2CMobGearPacket::decode)
                .consumerMainThread(S2CMobGearPacket::handle).add();
        CHANNEL.messageBuilder(C2SGraffitiSelectionPacket.class, id++, NetworkDirection.PLAY_TO_SERVER)
                .encoder(C2SGraffitiSelectionPacket::encode).decoder(C2SGraffitiSelectionPacket::decode)
                .consumerMainThread(C2SGraffitiSelectionPacket::handle).add();
        CHANNEL.messageBuilder(C2SRushActionPacket.class, id++, NetworkDirection.PLAY_TO_SERVER)
                .encoder(C2SRushActionPacket::encode).decoder(C2SRushActionPacket::decode)
                .consumerMainThread(C2SRushActionPacket::handle).add();
        CHANNEL.messageBuilder(S2CRushStatePacket.class, id++, NetworkDirection.PLAY_TO_CLIENT)
                .encoder(S2CRushStatePacket::encode).decoder(S2CRushStatePacket::decode)
                .consumerMainThread(S2CRushStatePacket::handle).add();
        CHANNEL.messageBuilder(C2SCrewActionPacket.class, id++, NetworkDirection.PLAY_TO_SERVER)
                .encoder(C2SCrewActionPacket::encode).decoder(C2SCrewActionPacket::decode)
                .consumerMainThread(C2SCrewActionPacket::handle).add();
        CHANNEL.messageBuilder(S2CCrewStatePacket.class, id++, NetworkDirection.PLAY_TO_CLIENT)
                .encoder(S2CCrewStatePacket::encode).decoder(S2CCrewStatePacket::decode)
                .consumerMainThread(S2CCrewStatePacket::handle).add();
        CHANNEL.messageBuilder(C2SActivityActionPacket.class, id++, NetworkDirection.PLAY_TO_SERVER)
                .encoder(C2SActivityActionPacket::encode).decoder(C2SActivityActionPacket::decode)
                .consumerMainThread(C2SActivityActionPacket::handle).add();
        CHANNEL.messageBuilder(S2CActivityStatePacket.class, id++, NetworkDirection.PLAY_TO_CLIENT)
                .encoder(S2CActivityStatePacket::encode).decoder(S2CActivityStatePacket::decode)
                .consumerMainThread(S2CActivityStatePacket::handle).add();
        CHANNEL.messageBuilder(S2CCrewRewardPacket.class, id++, NetworkDirection.PLAY_TO_CLIENT)
                .encoder(S2CCrewRewardPacket::encode).decoder(S2CCrewRewardPacket::decode)
                .consumerMainThread(S2CCrewRewardPacket::handle).add();
        CHANNEL.messageBuilder(C2SLibertyActionPacket.class, id++, NetworkDirection.PLAY_TO_SERVER)
                .encoder(C2SLibertyActionPacket::encode).decoder(C2SLibertyActionPacket::decode)
                .consumerMainThread(C2SLibertyActionPacket::handle).add();
        CHANNEL.messageBuilder(S2CLibertyStatePacket.class, id, NetworkDirection.PLAY_TO_CLIENT)
                .encoder(S2CLibertyStatePacket::encode).decoder(S2CLibertyStatePacket::decode)
                .consumerMainThread(S2CLibertyStatePacket::handle).add();
    }
    public static void sendInput(int mask, float forward, float strafe) { CHANNEL.sendToServer(new C2SInputPacket(mask, forward, strafe)); }
    public static void sendRideLoadoutAction(boolean unequip) { CHANNEL.sendToServer(new C2SRideLoadoutPacket(unequip)); }
    public static void sendRushAction(String action, String value, int number, UUID runId, int version) {
        CHANNEL.sendToServer(new C2SRushActionPacket(action, value, number, runId, version));
    }

    public static void sendCrewAction(String action, String value, int number, UUID houseId, UUID targetId, long revision) {
        CHANNEL.sendToServer(new C2SCrewActionPacket(action, value, number, houseId, targetId, revision));
    }

    public static void sendActivityAction(String action, String mode, UUID contextId, int version) {
        CHANNEL.sendToServer(new C2SActivityActionPacket(action, mode, contextId, version));
    }

    public static void sendLibertyAction(String action, String value, UUID missionId, long revision) {
        CHANNEL.sendToServer(new C2SLibertyActionPacket(action, value, missionId, revision));
    }

    public static void syncLiberty(ServerPlayer player, CompoundTag state) {
        if (player == null || !canSync(player)) return;
        CHANNEL.send(PacketDistributor.PLAYER.with(() -> player), new S2CLibertyStatePacket(state));
    }

    public static void syncActivity(ServerPlayer player, CompoundTag state) {
        if (player == null || !canSync(player)) return;
        CHANNEL.send(PacketDistributor.PLAYER.with(() -> player), new S2CActivityStatePacket(state));
    }

    public static void syncCrew(ServerPlayer player, CompoundTag state) {
        if (player == null || !canSync(player)) return;
        CHANNEL.send(PacketDistributor.PLAYER.with(() -> player), new S2CCrewStatePacket(state));
    }

    public static void syncCrewReward(ServerPlayer player, CompoundTag receipt) {
        if (player == null || !canSync(player)) return;
        CHANNEL.send(PacketDistributor.PLAYER.with(() -> player), new S2CCrewRewardPacket(receipt));
    }

    public static void syncRush(ServerPlayer player, CompoundTag state) {
        if (player == null || !canSync(player)) return;
        CHANNEL.send(PacketDistributor.PLAYER.with(() -> player), new S2CRushStatePacket(state));
    }
    public static void sendGraffitiSelection(InteractionHand hand, int variant, String customPattern,
                                             boolean freePaint, int colorId) {
        CHANNEL.sendToServer(new C2SGraffitiSelectionPacket(hand, variant, customPattern, freePaint, colorId));
    }

    /**
     * Fake/test players and a few server-side automation players can legitimately exist without a Netty channel.
     * They still participate in JetSetCraft's server-authoritative movement/capability logic, but there is no client
     * endpoint to receive S2C state. Treat that as "nothing to sync" instead of crashing Forge's login/tick path.
     */
    public static boolean canSync(ServerPlayer player) {
        return player.connection != null
                && player.connection.connection != null
                && player.connection.connection.channel() != null;
    }

    public static void sync(ServerPlayer player, JetSetData data) {
        if (!canSync(player)) return;
        S2CStatePacket p = new S2CStatePacket(player.getId(), data);
        CHANNEL.send(PacketDistributor.PLAYER.with(() -> player), p);
        CHANNEL.send(PacketDistributor.TRACKING_ENTITY.with(() -> player), p);
    }

    public static void syncMobGear(LivingEntity entity) {
        if (entity == null || entity.level().isClientSide) return;
        var snapshot = com.herberto.jetsetcraft.mob.MobStreetGear.snapshot(entity);
        CHANNEL.send(PacketDistributor.TRACKING_ENTITY.with(() -> entity),
                new S2CMobGearPacket(entity.getId(), snapshot.stack(), snapshot.rig()));
    }

    public static void syncMobGear(ServerPlayer receiver, LivingEntity entity) {
        if (receiver == null || entity == null || !canSync(receiver)) return;
        var snapshot = com.herberto.jetsetcraft.mob.MobStreetGear.snapshot(entity);
        CHANNEL.send(PacketDistributor.PLAYER.with(() -> receiver),
                new S2CMobGearPacket(entity.getId(), snapshot.stack(), snapshot.rig()));
    }
    private JetSetNetwork() {}
}
