package com.herberto.jetsetcraft.liberty;

import com.herberto.jetsetcraft.JetSetCraft;
import com.herberto.jetsetcraft.compat.LibertyHelldiversCompat;
import com.herberto.jetsetcraft.compat.LibertyTaczCompat;
import net.minecraft.world.entity.Entity;
import net.minecraftforge.event.entity.living.LivingAttackEvent;
import net.minecraftforge.event.level.ExplosionEvent;
import net.minecraftforge.event.server.ServerStartedEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.ModList;

/** Filters the exact adopted native actors before MCDivers applies its own damage during Detonate. */
@Mod.EventBusSubscriber(modid=JetSetCraft.MOD_ID,bus=Mod.EventBusSubscriber.Bus.FORGE)
public final class LibertyProviderSafety {
    /** Read-only production-JAR diagnostic; no content, world or provider setting is changed. */
    @SubscribeEvent
    public static void providerStatus(ServerStartedEvent event){
        boolean tacz=ModList.get().isLoaded("tacz"),mcdivers=ModList.get().isLoaded("mcdivers");
        boolean homeGuard="1.20.1".equals(net.minecraft.SharedConstants.getCurrentVersion().getName())
                && LibertyHomeEntityGuard.class.isAssignableFrom(Entity.class)
                && (Object)net.minecraft.world.level.block.Blocks.AIR.defaultBlockState() instanceof LibertyHomeBlockGuard;
        JetSetCraft.LOGGER.info("JETSETCRAFT_LIBERTY_PROVIDER_STATUS tacz={} pack={} native_guard={} mcdivers_installed={} mcdivers_ready={} home_guard={}",
                tacz,LibertyTaczCompat.available(),LibertyHelldiversCompat.terrainSafeTaczApi(),mcdivers,LibertyHelldiversCompat.available(),homeGuard);
    }
    @SubscribeEvent(priority=EventPriority.HIGHEST)
    public static void blast(ExplosionEvent.Detonate event){
        Entity direct=event.getExplosion().getDirectSourceEntity();
        LibertyStratagemEngine engine=LibertyStratagemEngine.forActor(direct);
        if(engine==null)return;
        if(engine.context().terrainSafe())event.getAffectedBlocks().clear();
        event.getAffectedEntities().removeIf(entity->!engine.authorizedEnemy(entity));
    }
    @SubscribeEvent(priority=EventPriority.HIGHEST)
    public static void damage(LivingAttackEvent event){
        LibertyStratagemEngine engine=LibertyStratagemEngine.forActor(event.getSource().getDirectEntity());
        if(engine==null)engine=LibertyStratagemEngine.forActor(event.getSource().getEntity());
        if(engine!=null&&!engine.authorizedEnemy(event.getEntity()))event.setCanceled(true);
    }
    private LibertyProviderSafety(){}
}
