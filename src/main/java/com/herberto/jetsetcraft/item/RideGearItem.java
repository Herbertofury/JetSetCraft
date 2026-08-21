package com.herberto.jetsetcraft.item;

import com.herberto.jetsetcraft.data.JetSetDataProvider;
import com.herberto.jetsetcraft.movement.RideStyle;
import com.herberto.jetsetcraft.network.JetSetNetwork;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

public final class RideGearItem extends Item {
    private final RideStyle style;

    public RideGearItem(RideStyle style, Properties properties) {
        super(properties);
        this.style = style;
    }

    public RideStyle style() {
        return style;
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (!level.isClientSide && player instanceof ServerPlayer serverPlayer) {
            player.getCapability(JetSetDataProvider.CAPABILITY).ifPresent(data -> {
                boolean sameAndActive = data.active() && data.style() == style;
                data.setStyle(style);
                data.setActive(!sameAndActive);
                data.setGrinding(false);
                data.setGrindGrace(0);
                if (sameAndActive) {
                    player.displayClientMessage(Component.translatable("message.jetsetcraft.ride_off").withStyle(ChatFormatting.GRAY), true);
                } else {
                    player.displayClientMessage(Component.translatable("message.jetsetcraft.ride_on", style.serializedName()).withStyle(ChatFormatting.AQUA), true);
                }
                JetSetNetwork.sync(serverPlayer, data);
            });
        }
        return InteractionResultHolder.sidedSuccess(stack, level.isClientSide);
    }
}
