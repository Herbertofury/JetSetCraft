package com.herberto.jetsetcraft.client.rush;

import com.herberto.jetsetcraft.JetSetCraft;
import com.herberto.jetsetcraft.entity.HonkistasPrototypeEntity;
import com.herberto.jetsetcraft.registry.ModEntities;
import net.minecraft.client.Minecraft;
import net.minecraft.client.CameraType;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.level.storage.LevelResource;
import java.util.UUID;
import java.util.function.Consumer;
import java.util.function.Predicate;

/** Real-client original-model views in the existing disposable audit world only; not encounter acceptance. */
public final class HonkistasPrototypeVisualAudit {
    private static int phase,ticks;
    private static volatile UUID actorId;
    private static volatile boolean queued,applied;
    private static volatile String error;
    private static Vec3 original,center;
    private static float yaw,pitch;
    private static boolean gravity,hideGui;
    private static long originalDayTime;
    private static CameraType camera;
    public static boolean advance(Minecraft mc,Consumer<String> capture,Predicate<String> captured){
        var server=mc.getSingleplayerServer();
        if(server==null || mc.player==null || mc.level==null
                || !server.getWorldPath(LevelResource.ROOT).toAbsolutePath().normalize().getFileName().toString().equals("RivalRushAudit"))
            throw new IllegalStateException("Original actor audit refused non-disposable world");
        if(error!=null)throw new IllegalStateException(error);
        if(phase==0){
            if(!queued){
                queued=true;hideGui=mc.options.hideGui;mc.options.hideGui=true;camera=mc.options.getCameraType();mc.options.setCameraType(CameraType.FIRST_PERSON);mc.setScreen(null);
                UUID owner=mc.player.getUUID();
                server.execute(()->{
                    try{
                        var p=server.getPlayerList().getPlayer(owner);
                        original=p.position();yaw=p.getYRot();pitch=p.getXRot();gravity=p.isNoGravity();
                        // Prior preview inherited an underwater fixture; use open, daylight airspace
                        // in this disposable world, never modify terrain or player inventory.
                        int surface=p.serverLevel().getHeight(net.minecraft.world.level.levelgen.Heightmap.Types.MOTION_BLOCKING,
                                p.blockPosition().getX()+8,p.blockPosition().getZ());
                        center=new Vec3(original.x+8,Math.max(original.y,surface)+16,original.z);
                        originalDayTime=p.serverLevel().getDayTime();p.serverLevel().setDayTime(6000);
                        HonkistasPrototypeEntity actor=ModEntities.HONKISTAS_PROTOTYPE.get().create(p.serverLevel());
                        if(actor==null)throw new IllegalStateException("Original actor registry missing");
                        actor.moveTo(center.x,center.y,center.z,0,0);actor.setNoAi(true);actor.setNoGravity(true);
                        if(!p.serverLevel().addFreshEntity(actor))throw new IllegalStateException("Original actor insertion rejected");
                        actorId=actor.getUUID();p.setNoGravity(true);
                        p.teleportTo(p.serverLevel(),center.x,center.y+.1,center.z+3,180,12);
                        applied=true;
                    }catch(RuntimeException e){error=e.toString();}
                });
            }
            if(!applied)return false;
            boolean visible=false;
            for(var candidate:mc.level.entitiesForRendering())
                if(candidate.getUUID().equals(actorId) && candidate instanceof HonkistasPrototypeEntity prototype
                        && mc.getEntityRenderDispatcher().getRenderer(prototype) instanceof com.herberto.jetsetcraft.client.render.HonkistasPrototypeRenderer)visible=true;
            if(!visible)return false;
            phase=1;ticks=0;return false;
        }
        String label=phase==1?"24-honkistas-front":phase==2?"25-honkistas-side":"26-honkistas-back";
        if(phase<=3){
            if(!applied)return false;
            if(++ticks==25)capture.accept(label);
            if(!captured.test(label))return false;
            if(phase<3){
                int next=phase+1;UUID owner=mc.player.getUUID();applied=false;
                server.execute(()->{
                    var p=server.getPlayerList().getPlayer(owner);
                    double x=next==2?center.x+3:center.x,z=next==2?center.z:center.z-3;
                    p.teleportTo(p.serverLevel(),x,center.y+.1,z,next==2?90:0,12);applied=true;
                });
                phase=next;ticks=0;return false;
            }
            UUID owner=mc.player.getUUID();applied=false;
            server.execute(()->{
                var p=server.getPlayerList().getPlayer(owner);var actor=p.serverLevel().getEntity(actorId);
                if(actor!=null)actor.discard();p.setNoGravity(gravity);p.serverLevel().setDayTime(originalDayTime);
                p.teleportTo(p.serverLevel(),original.x,original.y,original.z,yaw,pitch);applied=true;
            });
            phase=4;return false;
        }
        if(!applied)return false;
        mc.options.setCameraType(camera);mc.options.hideGui=hideGui;
        JetSetCraft.LOGGER.info("JETSETCRAFT_HONKISTAS_MODEL_PREVIEW_PASS original articulated model; three native still views only; no skating/secondary-physics/event acceptance");
        return true;
    }
    private HonkistasPrototypeVisualAudit(){}
}
