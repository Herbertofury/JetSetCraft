package com.herberto.jetsetcraft.network;

import com.herberto.jetsetcraft.graffiti.CustomGraffiti;
import com.herberto.jetsetcraft.graffiti.GraffitiCatalog;
import com.herberto.jetsetcraft.item.SprayCanItem;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

/** Server-authoritative spray-can selection; custom payloads are fixed-size, palette-only canvases. */
public record C2SGraffitiSelectionPacket(InteractionHand hand, int variant, String customPattern) {
    public static void encode(C2SGraffitiSelectionPacket packet, FriendlyByteBuf buf) {
        buf.writeEnum(packet.hand);
        buf.writeVarInt(packet.variant);
        buf.writeUtf(packet.customPattern == null ? "" : packet.customPattern, 128);
    }

    public static C2SGraffitiSelectionPacket decode(FriendlyByteBuf buf) {
        return new C2SGraffitiSelectionPacket(buf.readEnum(InteractionHand.class), buf.readVarInt(), buf.readUtf(128));
    }

    public static void handle(C2SGraffitiSelectionPacket packet, Supplier<NetworkEvent.Context> supplier) {
        NetworkEvent.Context context = supplier.get();
        context.enqueueWork(() -> {
            ServerPlayer player = context.getSender();
            if (player == null) return;
            ItemStack stack = player.getItemInHand(packet.hand);
            if (!(stack.getItem() instanceof SprayCanItem)) return;
            String custom = CustomGraffiti.normalize(packet.customPattern);
            if (!custom.isEmpty()) SprayCanItem.setCustomSelection(stack, custom);
            else SprayCanItem.setCatalogSelection(stack, Math.floorMod(packet.variant, GraffitiCatalog.size()));
        });
        context.setPacketHandled(true);
    }
}
