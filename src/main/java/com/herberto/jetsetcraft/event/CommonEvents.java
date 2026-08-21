package com.herberto.jetsetcraft.event;

import com.herberto.jetsetcraft.JetSetCraft;
import com.herberto.jetsetcraft.data.JetSetDataProvider;
import com.herberto.jetsetcraft.movement.JetSetMovement;
import com.herberto.jetsetcraft.network.JetSetNetwork;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.event.AttachCapabilitiesEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = JetSetCraft.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public final class CommonEvents {
    @SubscribeEvent public static void attachCapabilities(AttachCapabilitiesEvent<Entity> event) {
        if (event.getObject() instanceof Player) {
            JetSetDataProvider provider = new JetSetDataProvider();
            event.addCapability(JetSetDataProvider.ID, provider);
            event.addListener(provider::invalidate);
        }
    }
    @SubscribeEvent public static void clonePlayer(PlayerEvent.Clone event) {
        event.getOriginal().reviveCaps();
        event.getOriginal().getCapability(JetSetDataProvider.CAPABILITY).ifPresent(oldData ->
                event.getEntity().getCapability(JetSetDataProvider.CAPABILITY).ifPresent(newData -> newData.copyFrom(oldData)));
        event.getOriginal().invalidateCaps();
    }
    @SubscribeEvent public static void playerTick(TickEvent.PlayerTickEvent event) {
        if (event.phase != TickEvent.Phase.END || event.player.level().isClientSide) return;
        if (event.player instanceof ServerPlayer serverPlayer)
            serverPlayer.getCapability(JetSetDataProvider.CAPABILITY).ifPresent(data -> JetSetMovement.tickServer(serverPlayer, data));
    }
    @SubscribeEvent public static void login(PlayerEvent.PlayerLoggedInEvent event) { sync(event.getEntity()); }
    @SubscribeEvent public static void respawn(PlayerEvent.PlayerRespawnEvent event) { sync(event.getEntity()); }
    @SubscribeEvent public static void dimensionChange(PlayerEvent.PlayerChangedDimensionEvent event) {
        if (event.getEntity() instanceof ServerPlayer serverPlayer) {
            serverPlayer.getCapability(JetSetDataProvider.CAPABILITY).ifPresent(data -> {
                if (data.grinding()) {
                    data.setGrindGrace(Math.max(data.grindGrace(), 40));
                    data.setGrindReattachCooldown(0);
                    data.setGrindStuckTicks(0);
                    data.setGrindCurveFactor(1.0);
                }
            });
        }
        sync(event.getEntity());
    }
    private static void sync(Player player) {
        if (player instanceof ServerPlayer serverPlayer)
            serverPlayer.getCapability(JetSetDataProvider.CAPABILITY).ifPresent(data -> JetSetNetwork.sync(serverPlayer, data));
    }
    private CommonEvents() {}
}
