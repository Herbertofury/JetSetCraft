package com.herberto.jetsetcraft;

import com.mojang.logging.LogUtils;
import com.herberto.jetsetcraft.config.JetSetConfig;
import com.herberto.jetsetcraft.network.JetSetNetwork;
import com.herberto.jetsetcraft.event.CapabilityEvents;
import com.herberto.jetsetcraft.registry.ModBlockEntities;
import com.herberto.jetsetcraft.registry.ModBlocks;
import com.herberto.jetsetcraft.registry.ModCreativeTabs;
import com.herberto.jetsetcraft.registry.ModEntities;
import com.herberto.jetsetcraft.registry.ModItems;
import com.herberto.jetsetcraft.registry.ModSounds;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import org.slf4j.Logger;

@Mod(JetSetCraft.MOD_ID)
public final class JetSetCraft {
    public static final String MOD_ID = "jetsetcraft";
    public static final Logger LOGGER = LogUtils.getLogger();

    public JetSetCraft() {
        var modBus = FMLJavaModLoadingContext.get().getModEventBus();
        ModBlocks.BLOCKS.register(modBus);
        ModItems.ITEMS.register(modBus);
        ModEntities.ENTITIES.register(modBus);
        ModBlockEntities.BLOCK_ENTITIES.register(modBus);
        ModSounds.SOUNDS.register(modBus);
        ModCreativeTabs.TABS.register(modBus);
        modBus.addListener(CapabilityEvents::register);
        modBus.addListener(this::commonSetup);
        JetSetNetwork.init();
        ModLoadingContext.get().registerConfig(ModConfig.Type.SERVER, JetSetConfig.SERVER_SPEC);
        ModLoadingContext.get().registerConfig(ModConfig.Type.CLIENT, JetSetConfig.CLIENT_SPEC);
        MinecraftForge.EVENT_BUS.register(this);
    }

    private void commonSetup(FMLCommonSetupEvent event) {
        event.enqueueWork(ModItems::registerDispenserBehaviors);
    }
}
