package com.herberto.jetsetcraft.item;

import com.herberto.jetsetcraft.crew.CrewHouseManager;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;

import java.util.List;

/** A reusable, explicit interaction tool; opening it never builds, relocates or purchases anything. */
public final class CrewCharterItem extends Item {
    public CrewCharterItem(Properties properties) { super(properties); }

    @Override
    public InteractionResult onItemUseFirst(ItemStack stack, UseOnContext context) {
        // Inspect before the Boombox's existing head/empty-hand controls or a vanilla facility menu consumes use.
        if (context.getPlayer() instanceof ServerPlayer player) CrewHouseManager.openAt(player, context.getClickedPos());
        return InteractionResult.sidedSuccess(context.getLevel().isClientSide);
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        if (player instanceof ServerPlayer server) CrewHouseManager.open(server);
        return InteractionResultHolder.sidedSuccess(player.getItemInHand(hand), level.isClientSide);
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> lines, TooltipFlag flag) {
        lines.add(Component.translatable("tooltip.jetsetcraft.crew_charter.home").withStyle(ChatFormatting.AQUA));
        lines.add(Component.translatable("tooltip.jetsetcraft.crew_charter.facility").withStyle(ChatFormatting.GRAY));
        lines.add(Component.translatable("tooltip.jetsetcraft.crew_charter.open").withStyle(ChatFormatting.GRAY));
        lines.add(Component.translatable("tooltip.jetsetcraft.crew_charter.build").withStyle(ChatFormatting.GRAY));
        lines.add(Component.translatable("tooltip.jetsetcraft.crew_charter.profiles").withStyle(ChatFormatting.GRAY));
    }
}
