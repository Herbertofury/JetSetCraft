package com.herberto.jetsetcraft.crew;

import com.herberto.jetsetcraft.JetSetCraft;
import com.herberto.jetsetcraft.network.JetSetNetwork;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.event.server.ServerStoppedEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

/** World SavedData owns progression; only short-lived clicked-block contexts need player/server cleanup. */
@Mod.EventBusSubscriber(modid = JetSetCraft.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public final class CrewHouseEvents {
    @SubscribeEvent public static void login(PlayerEvent.PlayerLoggedInEvent event) {
        if (event.getEntity() instanceof ServerPlayer player) passive(player);
    }
    @SubscribeEvent public static void dimension(PlayerEvent.PlayerChangedDimensionEvent event) {
        if (event.getEntity() instanceof ServerPlayer player) passive(player);
    }
    @SubscribeEvent public static void logout(PlayerEvent.PlayerLoggedOutEvent event) {
        if (event.getEntity() instanceof ServerPlayer player) CrewHouseManager.forget(player);
    }
    @SubscribeEvent public static void stop(ServerStoppedEvent event) { CrewHouseManager.stop(event.getServer()); }
    private static void passive(ServerPlayer player) {
        CompoundTag state = CrewHouseManager.snapshot(player);
        state.putBoolean("Open", false);
        JetSetNetwork.syncCrew(player, state);
    }
    private CrewHouseEvents() {}
}
