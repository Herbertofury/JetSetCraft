package com.herberto.jetsetcraft.adventure;

import com.herberto.jetsetcraft.JetSetCraft;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.Level;

/**
 * Adventure Mode's own registered custom platformer dimension.
 * Datapack JSON defines the level; this key does NOT implement entry, slots, maps or gameplay.
 * Never redirect this key to Rival Rush, Liberty Grind, or a player's source world.
 */
public final class AdventureWorld {
    public static final ResourceKey<Level> DIMENSION = ResourceKey.create(
            Registries.DIMENSION,
            ResourceLocation.fromNamespaceAndPath(JetSetCraft.MOD_ID, "adventure_mode"));

    public static final int BASE_SURFACE_Y = 63;

    public static boolean isAdventure(Level level) {
        return level != null && DIMENSION.equals(level.dimension());
    }

    private AdventureWorld() {}
}
