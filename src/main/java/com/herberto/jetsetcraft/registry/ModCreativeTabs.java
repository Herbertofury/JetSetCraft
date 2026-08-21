package com.herberto.jetsetcraft.registry;

import com.herberto.jetsetcraft.JetSetCraft;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.RegistryObject;

public final class ModCreativeTabs {
    public static final DeferredRegister<CreativeModeTab> TABS = DeferredRegister.create(Registries.CREATIVE_MODE_TAB, JetSetCraft.MOD_ID);

    public static final RegistryObject<CreativeModeTab> MAIN = TABS.register("main", () -> CreativeModeTab.builder()
            .title(Component.translatable("itemGroup.jetsetcraft.main"))
            .icon(() -> ModItems.INLINE_SKATES.get().getDefaultInstance())
            .displayItems((parameters, output) -> {
                output.accept(ModItems.INLINE_SKATES.get());
                output.accept(ModItems.QUAD_SKATES.get());
                output.accept(ModItems.STREET_BOARD.get());
                output.accept(ModItems.HOVERBOARD.get());
                output.accept(ModItems.BMX.get());
                output.accept(ModItems.SPRAY_CAN.get());
            }).build());

    private ModCreativeTabs() {}
}
