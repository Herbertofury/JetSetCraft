package com.herberto.jetsetcraft.registry;

import com.herberto.jetsetcraft.JetSetCraft;
import com.herberto.jetsetcraft.block.BoomboxBlock;
import net.minecraft.world.level.block.Block;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public final class ModBlocks {
    public static final DeferredRegister<Block> BLOCKS = DeferredRegister.create(ForgeRegistries.BLOCKS, JetSetCraft.MOD_ID);

    public static final RegistryObject<Block> BOOMBOX = BLOCKS.register("boombox", BoomboxBlock::new);

    private ModBlocks() {}
}
