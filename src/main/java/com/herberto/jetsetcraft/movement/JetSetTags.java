package com.herberto.jetsetcraft.movement;

import com.herberto.jetsetcraft.JetSetCraft;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.block.Block;

/**
 * Datapack-facing world interaction hooks. Vanilla defaults are built in, while modded blocks can opt in
 * without a JetSetCraft code patch.
 */
public final class JetSetTags {
    public static final TagKey<Block> BOOST_SURFACES = block("boost_surfaces");
    public static final TagKey<Block> BRAKE_SURFACES = block("brake_surfaces");
    public static final TagKey<Block> LOW_FRICTION_SURFACES = block("low_friction_surfaces");
    public static final TagKey<Block> BOUNCE_SURFACES = block("bounce_surfaces");
    public static final TagKey<Block> STICKY_SURFACES = block("sticky_surfaces");
    public static final TagKey<Block> HAZARD_SURFACES = block("hazard_surfaces");
    public static final TagKey<Block> FLUID_SKIMMABLE = block("fluid_skimmable");
    public static final TagKey<Block> GRINDABLE = block("grindable");
    public static final TagKey<Block> WALLRIDEABLE = block("wallrideable");
    public static final TagKey<Block> NO_GRIND = block("no_grind");

    private static TagKey<Block> block(String path) {
        return TagKey.create(Registries.BLOCK, ResourceLocation.fromNamespaceAndPath(JetSetCraft.MOD_ID, path));
    }

    private JetSetTags() {}
}
