package com.herberto.jetsetcraft.item;

import com.herberto.jetsetcraft.data.JetSetDataProvider;
import com.herberto.jetsetcraft.movement.RideStyle;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.Enchantments;
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

    public boolean isFootwearStyle() {
        return style == RideStyle.INLINE || style == RideStyle.QUAD;
    }

    /**
     * Skates deliberately compose with Minecraft's movement enchantments even though JetSetCraft
     * stores them in its own hands-free loadout instead of consuming the vanilla boots slot.
     * Boards/BMX keep their own future upgrade language instead of inheriting footwear magic.
     */
    @Override
    public boolean canApplyAtEnchantingTable(ItemStack stack, Enchantment enchantment) {
        if (!isFootwearStyle()) return false;
        return enchantment == Enchantments.FROST_WALKER
                || enchantment == Enchantments.SOUL_SPEED
                || enchantment == Enchantments.DEPTH_STRIDER
                || enchantment == Enchantments.FALL_PROTECTION;
    }

    @Override
    public boolean isEnchantable(ItemStack stack) {
        return isFootwearStyle() && stack.getCount() == 1;
    }

    @Override
    public int getEnchantmentValue(ItemStack stack) {
        return isFootwearStyle() ? 14 : 0;
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (!level.isClientSide && player instanceof ServerPlayer serverPlayer) {
            player.getCapability(JetSetDataProvider.CAPABILITY)
                    .ifPresent(data -> RideLoadout.equipFromHand(serverPlayer, data, hand, this));
        }
        return InteractionResultHolder.sidedSuccess(stack, level.isClientSide);
    }
}
