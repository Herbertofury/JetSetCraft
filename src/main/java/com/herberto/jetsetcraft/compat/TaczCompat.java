package com.herberto.jetsetcraft.compat;

import com.tacz.guns.api.item.IGun;
import net.minecraft.world.item.ItemStack;

/** Loaded only when TacZ is present. Keeps TacZ classes out of the standalone hot path. */
public final class TaczCompat {
    public static boolean isGun(ItemStack stack) { return !stack.isEmpty() && stack.getItem() instanceof IGun; }
    private TaczCompat() {}
}
