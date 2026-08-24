package com.herberto.jetsetcraft.gang.data;

import com.herberto.jetsetcraft.JetSetCraft;
import net.minecraftforge.event.AddReloadListenerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

/** Registers server/datapack reload listeners without adding a client or optional-mod dependency. */
@Mod.EventBusSubscriber(modid = JetSetCraft.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public final class GangDataEvents {
    @SubscribeEvent
    public static void addReloadListeners(AddReloadListenerEvent event) {
        event.addListener(new GangDefinitionReloadListener());
        event.addListener(new HeadTargetMappingReloadListener());
    }

    private GangDataEvents() {}
}
