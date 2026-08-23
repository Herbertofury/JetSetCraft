package com.herberto.jetsetcraft.item;

import com.herberto.jetsetcraft.data.JetSetDataProvider;
import com.herberto.jetsetcraft.movement.RideStyle;
import com.herberto.jetsetcraft.mob.MobStreetGear;
import com.herberto.jetsetcraft.mob.StreetGearAcquisition;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.Level;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.TooltipFlag;
import java.util.List;

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
    public InteractionResult interactLivingEntity(ItemStack stack, Player player, LivingEntity target, InteractionHand hand) {
        if (target instanceof Player || !MobStreetGear.eligible(target)) return InteractionResult.PASS;
        if (player.level().isClientSide) return InteractionResult.SUCCESS;

        boolean swap = player.isShiftKeyDown();
        MobStreetGear.EquipResult result = MobStreetGear.equip(target, stack, StreetGearAcquisition.PLAYER, swap);
        if (!result.equipped()) {
            player.displayClientMessage(Component.translatable("message.jetsetcraft.mob_gear_already_equipped")
                    .withStyle(ChatFormatting.GRAY), true);
            return InteractionResult.CONSUME;
        }
        if (!player.getAbilities().instabuild) stack.shrink(1);
        if (!result.previous().isEmpty() && !player.addItem(result.previous())) player.drop(result.previous(), false);
        player.displayClientMessage(Component.translatable("message.jetsetcraft.mob_gear_equipped",
                target.getDisplayName(), stack.getHoverName()).withStyle(ChatFormatting.AQUA), true);
        return InteractionResult.CONSUME;
    }

    @Override
    public boolean onEntityItemUpdate(ItemStack stack, net.minecraft.world.entity.item.ItemEntity entity) {
        if (!entity.level().isClientSide && entity.tickCount % 8 == 0 && !stack.isEmpty()) {
            LivingEntity target = entity.level().getEntitiesOfClass(LivingEntity.class,
                            entity.getBoundingBox().inflate(0.65D), candidate -> MobStreetGear.eligible(candidate)
                                    && !MobStreetGear.hasGear(candidate))
                    .stream().min(java.util.Comparator.comparingDouble(entity::distanceToSqr)).orElse(null);
            if (target != null && MobStreetGear.equip(target, stack, StreetGearAcquisition.DROPPED_CONTACT, false).equipped()) {
                stack.shrink(1);
                if (stack.isEmpty()) entity.discard();
                else entity.setItem(stack);
            }
        }
        return false;
    }

    @Override
    public void appendHoverText(ItemStack stack, Level level, List<Component> tooltip, TooltipFlag flag) {
        tooltip.add(Component.translatable("tooltip.jetsetcraft.ride_gear_player").withStyle(ChatFormatting.AQUA));
        tooltip.add(Component.translatable("tooltip.jetsetcraft.ride_gear_mob").withStyle(ChatFormatting.GRAY));
        tooltip.add(Component.translatable("tooltip.jetsetcraft.ride_gear_remove").withStyle(ChatFormatting.DARK_GRAY));
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
