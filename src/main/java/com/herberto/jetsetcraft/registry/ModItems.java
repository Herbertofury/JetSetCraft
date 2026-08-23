package com.herberto.jetsetcraft.registry;

import com.herberto.jetsetcraft.JetSetCraft;
import com.herberto.jetsetcraft.item.RideGearItem;
import com.herberto.jetsetcraft.item.SprayCanItem;
import com.herberto.jetsetcraft.movement.RideStyle;
import com.herberto.jetsetcraft.mob.StreetGearDispenserBehavior;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.DispenserBlock;
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
    public static final RegistryObject<Item> HOVERBOARD = ITEMS.register("hoverboard",
            () -> new RideGearItem(RideStyle.HOVER, new Item.Properties().stacksTo(1)));
    public static final RegistryObject<Item> BMX = ITEMS.register("bmx",
            () -> new RideGearItem(RideStyle.BMX, new Item.Properties().stacksTo(1)));
    public static final RegistryObject<Item> SCOOTER = ITEMS.register("scooter",
            () -> new RideGearItem(RideStyle.SCOOTER, new Item.Properties().stacksTo(1)));
    public static final RegistryObject<Item> SPRAY_CAN = ITEMS.register("spray_can",
            () -> new SprayCanItem(new Item.Properties().stacksTo(1).durability(256)));


    public static void registerDispenserBehaviors() {
        StreetGearDispenserBehavior behavior = new StreetGearDispenserBehavior();
        DispenserBlock.registerBehavior(INLINE_SKATES.get(), behavior);
        DispenserBlock.registerBehavior(QUAD_SKATES.get(), behavior);
        DispenserBlock.registerBehavior(STREET_BOARD.get(), behavior);
        DispenserBlock.registerBehavior(HOVERBOARD.get(), behavior);
        DispenserBlock.registerBehavior(BMX.get(), behavior);
        DispenserBlock.registerBehavior(SCOOTER.get(), behavior);
    }

    private ModItems() {}
}
