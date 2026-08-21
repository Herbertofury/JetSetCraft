package com.herberto.jetsetcraft;

import com.mojang.logging.LogUtils;
import com.herberto.jetsetcraft.config.JetSetConfig;
import com.herberto.jetsetcraft.network.JetSetNetwork;
import com.herberto.jetsetcraft.event.CapabilityEvents;
import com.herberto.jetsetcraft.movement.EdgeRuntimeVerifier;
import com.herberto.jetsetcraft.registry.ModCreativeTabs;
import com.herberto.jetsetcraft.registry.ModEntities;
import com.herberto.jetsetcraft.registry.ModItems;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.server.ServerStartedEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import org.slf4j.Logger;

@Mod(JetSetCraft.MOD_ID)
public final class JetSetCraft {
    public static final String MOD_ID = "jetsetcraft";
    public static final Logger LOGGER = LogUtils.getLogger();

    public JetSetCraft() {
        var modBus = FMLJavaModLoadingContext.get().getModEventBus();
        ModItems.ITEMS.register(modBus);
        ModEntities.ENTITIES.register(modBus);
        ModCreativeTabs.TABS.register(modBus);
        modBus.addListener(CapabilityEvents::register);
        JetSetNetwork.init();
        ModLoadingContext.get().registerConfig(ModConfig.Type.SERVER, JetSetConfig.SERVER_SPEC);
        ModLoadingContext.get().registerConfig(ModConfig.Type.CLIENT, JetSetConfig.CLIENT_SPEC);
        MinecraftForge.EVENT_BUS.register(this);
    }

    @SubscribeEvent
    public void onServerStarted(ServerStartedEvent event) {
        if (Boolean.getBoolean("jetsetcraft.ci.edgeRuntime")) {
            EdgeRuntimeVerifier.run(event.getServer());
        }
    }
}
