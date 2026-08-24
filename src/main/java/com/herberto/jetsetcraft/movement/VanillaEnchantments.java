package com.herberto.jetsetcraft.movement;

import com.herberto.jetsetcraft.config.JetSetConfig;
import com.herberto.jetsetcraft.data.JetSetData;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.Mth;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.item.enchantment.FrostWalkerEnchantment;

import com.herberto.jetsetcraft.movement.VanillaWorldPhysics.*;

final class VanillaEnchantments {
    static int effectiveEnchantmentLevel(ServerPlayer player, JetSetData data, Enchantment enchantment) {
        int vanilla = EnchantmentHelper.getEnchantmentLevel(enchantment, player);
        ItemStack ride = data.rideGear();
        int jetSet = 0;
        if (!ride.isEmpty() && ride.getItem() instanceof com.herberto.jetsetcraft.item.RideGearItem gear && gear.isFootwearStyle()) {
            jetSet = EnchantmentHelper.getTagEnchantmentLevel(enchantment, ride);
        }
        return Math.max(vanilla, jetSet);
    }

    /**
     * Reuses vanilla Frost Walker itself instead of inventing a fake ice trail. JetSetCraft only
     * supplies the enchantment level from its hands-free skate slot; Minecraft still owns the
     * actual frosted-ice placement rules, light checks, water checks and melt scheduling.
     */
    static void applyRideEnchantments(ServerPlayer player, JetSetData data) {
        if (!data.active() || !JetSetConfig.SERVER.enableVanillaWorldPhysics.get() || !player.onGround()) return;
        ItemStack ride = data.rideGear();
        if (ride.isEmpty() || !(ride.getItem() instanceof com.herberto.jetsetcraft.item.RideGearItem gear) || !gear.isFootwearStyle()) return;

        int rideFrost = EnchantmentHelper.getTagEnchantmentLevel(Enchantments.FROST_WALKER, ride);
        int vanillaFrost = EnchantmentHelper.getEnchantmentLevel(Enchantments.FROST_WALKER, player);
        if (rideFrost > vanillaFrost) {
            FrostWalkerEnchantment.onEntityMoved(player, player.level(), player.blockPosition(), rideFrost);
        }
    }

    /**
     * Makes Feather Falling on JetSetCraft's dedicated skate slot behave like real Minecraft
     * protection without double-counting a vanilla boots enchantment. LivingHurtEvent runs before
     * armor/enchantment reduction in Forge 1.20.1, so scaling by the ratio between the vanilla EPF
     * and target EPF produces the same final protection curve Minecraft already uses.
     */
    static float augmentFallDamageProtection(ServerPlayer player, JetSetData data, DamageSource source, float amount) {
        if (amount <= 0.0f || !JetSetConfig.SERVER.enableVanillaWorldPhysics.get()) return amount;
        ItemStack ride = data.rideGear();
        if (ride.isEmpty() || !(ride.getItem() instanceof com.herberto.jetsetcraft.item.RideGearItem gear) || !gear.isFootwearStyle()) {
            return amount;
        }

        int rideLevel = EnchantmentHelper.getTagEnchantmentLevel(Enchantments.FALL_PROTECTION, ride);
        int vanillaLevel = EnchantmentHelper.getEnchantmentLevel(Enchantments.FALL_PROTECTION, player);
        if (rideLevel <= vanillaLevel) return amount;

        int rideProtection = Enchantments.FALL_PROTECTION.getDamageProtection(rideLevel, source);
        int vanillaFeatherProtection = Enchantments.FALL_PROTECTION.getDamageProtection(vanillaLevel, source);
        int extra = Math.max(0, rideProtection - vanillaFeatherProtection);
        if (extra <= 0) return amount; // Non-fall damage: Feather Falling contributes zero by vanilla rules.

        int vanillaProtection = Mth.clamp(EnchantmentHelper.getDamageProtection(player.getArmorSlots(), source), 0, 20);
        int targetProtection = Mth.clamp(vanillaProtection + extra, 0, 20);
        if (targetProtection <= vanillaProtection || vanillaProtection >= 20) return amount;

        double ratio = (25.0 - targetProtection) / (25.0 - vanillaProtection);
        return (float)(amount * ratio);
    }

    private VanillaEnchantments() {}
}
