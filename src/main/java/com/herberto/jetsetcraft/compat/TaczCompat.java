package com.herberto.jetsetcraft.compat;

import net.minecraft.world.item.ItemStack;

/** Loaded only when TacZ is present. Keeps TacZ classes and its large distribution out of the build classpath. */
public final class TaczCompat {
    private static final Class<?> GUN_TYPE = loadGunType();

    public static boolean isGun(ItemStack stack) {
        return GUN_TYPE != null && !stack.isEmpty() && GUN_TYPE.isInstance(stack.getItem());
    }

    private static Class<?> loadGunType() {
        try {
            return Class.forName("com.tacz.guns.api.item.IGun", false, TaczCompat.class.getClassLoader());
        } catch (ClassNotFoundException | LinkageError ignored) {
            return null;
        }
    }

    private TaczCompat() {}
}
