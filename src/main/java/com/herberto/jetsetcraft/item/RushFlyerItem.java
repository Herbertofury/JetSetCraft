package com.herberto.jetsetcraft.item;

import com.herberto.jetsetcraft.rush.RivalRushManager;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;

import java.util.List;

/** An optional, reusable invitation. Reading the flyer never starts a run or consumes an item. */
public final class RushFlyerItem extends Item {
    public RushFlyerItem(Properties properties) { super(properties); }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        if (player instanceof ServerPlayer server) RivalRushManager.handleAction(server, "menu", "", 0, null, 0);
        return InteractionResultHolder.sidedSuccess(player.getItemInHand(hand), level.isClientSide);
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> lines, TooltipFlag flag) {
        lines.add(Component.literal("Optional 30-minute skating survival sessions").withStyle(ChatFormatting.AQUA));
        lines.add(Component.literal("Separate XP, crew rivals, collectible mob cards").withStyle(ChatFormatting.GRAY));
        lines.add(Component.literal("Open the menu to join or return to Minecraft").withStyle(ChatFormatting.GRAY));
    }
}
