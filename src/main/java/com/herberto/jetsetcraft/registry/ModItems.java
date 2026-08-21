package com.herberto.jetsetcraft.registry;

import com.herberto.jetsetcraft.JetSetCraft;
import com.herberto.jetsetcraft.item.RideGearItem;
import com.herberto.jetsetcraft.item.SprayCanItem;
import com.herberto.jetsetcraft.movement.RideStyle;
import net.minecraft.world.item.Item;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public final class ModItems {
    public static final DeferredRegister<Item> ITEMS = DeferredRegister.create(ForgeRegistries.ITEMS, JetSetCraft.MOD_ID);

    public static final RegistryObject<Item> INLINE_SKATES = ITEMS.register("inline_skates",
            () -> new RideGearItem(RideStyle.INLINE, new Item.Properties().stacksTo(1)));
    public static final RegistryObject<Item> QUAD_SKATES = ITEMS.register("quad_skates",
            () -> new RideGearItem(RideStyle.QUAD, new Item.Properties().stacksTo(1)));
    public static final RegistryObject<Item> STREET_BOARD = ITEMS.register("street_board",
            () -> new RideGearItem(RideStyle.BOARD, new Item.Properties().stacksTo(1)));
    public static final RegistryObject<Item> BMX = ITEMS.register("bmx",
            () -> new RideGearItem(RideStyle.BMX, new Item.Properties().stacksTo(1)));
    public static final RegistryObject<Item> SPRAY_CAN = ITEMS.register("spray_can",
            () -> new SprayCanItem(new Item.Properties().stacksTo(1).durability(256)));

    private ModItems() {}
}
