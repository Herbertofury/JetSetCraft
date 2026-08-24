package com.herberto.jetsetcraft.registry;

import com.herberto.jetsetcraft.JetSetCraft;
import com.herberto.jetsetcraft.blockentity.BoomboxBlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public final class ModBlockEntities {
    public static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITIES =
            DeferredRegister.create(ForgeRegistries.BLOCK_ENTITY_TYPES, JetSetCraft.MOD_ID);

    public static final RegistryObject<BlockEntityType<BoomboxBlockEntity>> BOOMBOX = BLOCK_ENTITIES.register("boombox",
            () -> BlockEntityType.Builder.of(BoomboxBlockEntity::new, ModBlocks.BOOMBOX.get()).build(null));

    private ModBlockEntities() {}
}
