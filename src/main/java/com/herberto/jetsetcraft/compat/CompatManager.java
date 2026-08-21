package com.herberto.jetsetcraft.compat;

import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.fml.ModList;

public final class CompatManager {
    public static final boolean TACZ = ModList.get().isLoaded("tacz");
    public static final boolean EPIC_FIGHT = ModList.get().isLoaded("epicfight");
    public static final boolean BETTER_COMBAT = ModList.get().isLoaded("bettercombat");
    public static final boolean YSM = ModList.get().isLoaded("yes_steve_model");

    public static boolean isGun(ItemStack stack) {
        return TACZ && TaczCompat.isGun(stack);
    }

    public static boolean hasWeaponOverlay(Player player) {
        return isGun(player.getMainHandItem()) || isGun(player.getOffhandItem()) || player.isUsingItem() || player.swinging;
    }

    private CompatManager() {}
}
