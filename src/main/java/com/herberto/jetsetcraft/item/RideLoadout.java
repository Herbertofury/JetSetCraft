package com.herberto.jetsetcraft.item;

import com.herberto.jetsetcraft.data.JetSetData;
import com.herberto.jetsetcraft.movement.RideStyle;
import com.herberto.jetsetcraft.network.JetSetNetwork;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;

/**
 * Server-authoritative dedicated ride equipment slot.
 *
 * Red Skate Rebellion demonstrated that street-sport gear is much cleaner as a player loadout than
 * as a hand-held pseudo-vehicle. JetSetCraft keeps that good idea while retaining its own physics,
 * animation and combat systems. The slot stores the actual ItemStack, so custom NBT survives.
 */
public final class RideLoadout {
    public static void equipFromHand(ServerPlayer player, JetSetData data, InteractionHand hand, RideGearItem item) {
        ItemStack held = player.getItemInHand(hand);
        if (held.isEmpty() || held.getItem() != item) return;

        ItemStack equipped = held.copy();
        equipped.setCount(1);
        if (!player.getAbilities().instabuild) held.shrink(1);

        returnToPlayer(player, data.takeRideGear());
        data.setRideGear(equipped);
        data.setStyle(item.style());
        data.setActive(true);
        resetMotionState(data);
        player.displayClientMessage(Component.translatable("message.jetsetcraft.ride_equipped", item.style().serializedName())
                .withStyle(ChatFormatting.AQUA), true);
        JetSetNetwork.sync(player, data);
    }

    public static void toggle(ServerPlayer player, JetSetData data) {
        RideStyle equippedStyle = equippedStyle(data);
        if (equippedStyle == RideStyle.NONE) {
            data.setActive(false);
            data.setStyle(RideStyle.NONE);
            player.displayClientMessage(Component.translatable("message.jetsetcraft.no_ride_equipped")
                    .withStyle(ChatFormatting.GRAY), true);
            JetSetNetwork.sync(player, data);
            return;
        }

        boolean next = !data.active();
        data.setStyle(equippedStyle);
        data.setActive(next);
        resetMotionState(data);
        player.displayClientMessage(Component.translatable(next
                        ? "message.jetsetcraft.ride_on" : "message.jetsetcraft.ride_off", equippedStyle.serializedName())
                .withStyle(next ? ChatFormatting.AQUA : ChatFormatting.GRAY), true);
        JetSetNetwork.sync(player, data);
    }

    public static void unequip(ServerPlayer player, JetSetData data) {
        ItemStack stack = data.takeRideGear();
        if (stack.isEmpty()) {
            player.displayClientMessage(Component.translatable("message.jetsetcraft.no_ride_equipped")
                    .withStyle(ChatFormatting.GRAY), true);
            return;
        }
        data.setActive(false);
        data.setStyle(RideStyle.NONE);
        data.setMomentum(0.0);
        resetMotionState(data);
        returnToPlayer(player, stack);
        player.displayClientMessage(Component.translatable("message.jetsetcraft.ride_unequipped")
                .withStyle(ChatFormatting.GRAY), true);
        JetSetNetwork.sync(player, data);
    }

    public static RideStyle equippedStyle(JetSetData data) {
        ItemStack stack = data.rideGear();
        return !stack.isEmpty() && stack.getItem() instanceof RideGearItem gear ? gear.style() : RideStyle.NONE;
    }

    public static void dropEquipped(ServerPlayer player, JetSetData data) {
        ItemStack stack = data.takeRideGear();
        if (!stack.isEmpty()) player.spawnAtLocation(stack);
        data.setActive(false);
        data.setStyle(RideStyle.NONE);
        data.setMomentum(0.0);
        resetMotionState(data);
    }

    private static void returnToPlayer(ServerPlayer player, ItemStack stack) {
        if (stack == null || stack.isEmpty()) return;
        if (!player.getInventory().add(stack)) player.drop(stack, false);
    }

    private static void resetMotionState(JetSetData data) {
        data.setGrinding(false);
        data.setGrindGrace(0);
        data.setWallRiding(false);
        data.setManual(false);
        data.setPowersliding(false);
        data.setDancing(false);
        data.setGroundStunt(false);
        data.setBoostTrick(false);
        data.setTrickTicks(0);
    }

    private RideLoadout() {}
}
