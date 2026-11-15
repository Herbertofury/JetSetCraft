package com.herberto.jetsetcraft.client.rush;

import com.herberto.jetsetcraft.JetSetCraft;
import com.herberto.jetsetcraft.entity.HonkistasPrototypeEntity;
import com.herberto.jetsetcraft.registry.ModEntities;
import com.herberto.jetsetcraft.client.render.HonkistasPrototypeRenderer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.CameraType;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.LevelResource;
import net.minecraft.world.phys.Vec3;
import java.util.*;
import java.util.function.*;

/** Actual navigation on a reversible lane in the disposable audit world; not skating or an encounter. */
public final class HonkistasGroundedVisualAudit {
    private static int phase,ticks;
    private static volatile boolean queued,applied,routeDone;
    private static volatile String error,serverDiagnostic="not sampled";
    private static UUID actorId;
    private static Vec3 original,start,cameraPosition;
    private static volatile Vec3 cueStart;
    private static float yaw,pitch;
    private static boolean gravity,hideGui,requested;
    private static CameraType camera;
    private static long day,readyAt;
    private static final Map<BlockPos,BlockState> saved=new LinkedHashMap<>();
    public static boolean advance(Minecraft mc,Consumer<String> capture,Predicate<String> captured){
        var server=mc.getSingleplayerServer();
        if(server==null||mc.player==null||mc.level==null||!server.getWorldPath(LevelResource.ROOT)
                .toAbsolutePath().normalize().getFileName().toString().equals("RivalRushAudit"))
            throw new IllegalStateException("Grounded actor audit refused non-disposable world");
        if(error!=null)throw new IllegalStateException(error);
        if(phase==0){
            if(!queued){
                queued=true;hideGui=mc.options.hideGui;camera=mc.options.getCameraType();
                mc.options.hideGui=true;mc.options.setCameraType(CameraType.FIRST_PERSON);mc.setScreen(null);
                UUID owner=mc.player.getUUID();
                server.execute(()->{try{
                    var p=server.getPlayerList().getPlayer(owner);var level=p.serverLevel();
                    original=p.position();yaw=p.getYRot();pitch=p.getXRot();gravity=p.isNoGravity();day=level.getDayTime();
                    int x=p.blockPosition().getX()+12,z=p.blockPosition().getZ();
                    int surface=p.blockPosition().getY();
                    for(int dx=-2;dx<=9;dx++)for(int dz=-3;dz<=3;dz++)
                        surface=Math.max(surface,level.getHeight(net.minecraft.world.level.levelgen.Heightmap.Types.MOTION_BLOCKING,x+dx,z+dz));
                    int y=surface+12;
                    if(y+4>=level.getMaxBuildHeight())throw new IllegalStateException("No room for safe audit lane");
                    start=new Vec3(x+.5,y+1,z+.5);
                    for(int dx=-2;dx<=9;dx++)for(int dz=-3;dz<=3;dz++){
                        BlockPos pos=new BlockPos(x+dx,y,z+dz);saved.put(pos,level.getBlockState(pos));
                        level.setBlockAndUpdate(pos,Blocks.SMOOTH_STONE.defaultBlockState());
                    }
                    var actor=ModEntities.HONKISTAS_PROTOTYPE.get().create(level);
                    if(actor==null)throw new IllegalStateException("Original actor missing");
                    // Keep native AI/physics active so floor contact is genuinely observed.
                    actor.moveTo(start.x,start.y,start.z,270,0);
                    if(!level.addFreshEntity(actor))throw new IllegalStateException("Grounded actor insertion failed");
                    actorId=actor.getUUID();cameraPosition=start.add(3,.4,7);
                    level.setDayTime(6000);p.setNoGravity(true);
                    p.teleportTo(level,cameraPosition.x,cameraPosition.y,cameraPosition.z,180,8);applied=true;
                }catch(RuntimeException failure){error=failure.toString();}});
            }
            if(!applied)return false;
            phase=1;readyAt=System.nanoTime();ticks=0;return false;
        }
        if(phase<=5){
            ++ticks;
            if(ticks%100==0){UUID owner=mc.player.getUUID();server.execute(()->{
                var p=server.getPlayerList().getPlayer(owner);var a=p.serverLevel().getEntity(actorId);
                serverDiagnostic="player="+p.position()+" velocity="+p.getDeltaMovement()+" actor="+(a==null?"absent":a.position()+" ground="+a.onGround());
            });}
            if(ticks>500){
                net.minecraft.world.entity.Entity visible=null;
                for(var e:mc.level.entitiesForRendering())if(e.getUUID().equals(actorId)){visible=e;break;}
                throw new IllegalStateException("Grounded navigation/capture timeout phase "+phase+" client="+mc.player.position()
                        +" expected="+cameraPosition+" velocity="+mc.player.getDeltaMovement()+" yaw="+mc.player.getYRot()
                        +" actorClient="+(visible==null?"absent":visible.position()+" ground="+visible.onGround())
                        +" rendered="+HonkistasPrototypeRenderer.renderedSince(actorId,readyAt)+" server="+serverDiagnostic);
            }
            net.minecraft.world.entity.Entity candidate=null;
            for(var e:mc.level.entitiesForRendering())if(e.getUUID().equals(actorId)){candidate=e;break;}
            if(!(candidate instanceof HonkistasPrototypeEntity actor))return false;
            if(mc.player.position().distanceToSqr(cameraPosition)>.04||Math.abs(net.minecraft.util.Mth.wrapDegrees(mc.player.getYRot()-180))>2)return false;
            if(!actor.onGround()||!HonkistasPrototypeRenderer.renderedSince(actorId,readyAt))return false;
            String label=phase==1?"27-honkistas-grounded-start":phase==2?"28-honkistas-walking":phase==3?"29-honkistas-route-complete":"30-honkistas-synced-beat-cue";
            if(phase==2 && actor.getX()<start.x+2)return false;
            if(phase==4 && (cueStart==null || actor.beatCueTicksRemaining()<=8
                    || actor.beatCueTicksRemaining()>=HonkistasPrototypeEntity.BEAT_CUE_TICKS-6
                    || actor.position().distanceTo(cueStart)<.5))return false;
            if(phase==3 || phase==5){
                if(!queued){queued=true;server.execute(()->{try{
                    var p=server.getPlayerList().getPlayer(mc.player.getUUID());
                    var actual=(HonkistasPrototypeEntity)p.serverLevel().getEntity(actorId);
                    routeDone=actual!=null && actual.controlledRouteStatus()==HonkistasPrototypeEntity.RouteStatus.COMPLETED;
                    queued=false;
                }catch(RuntimeException e){error=e.toString();}});}
                if(!routeDone || phase==3 && actor.getX()<start.x+5
                        || phase==5 && (actor.position().distanceTo(start)>.9 || actor.beatCueTicksRemaining()!=0))return false;
            }
            if(ticks<(phase==4?6:25))return false;
            if(phase!=5){
                if(!requested){requested=true;capture.accept(label);}
                if(!captured.test(label))return false;
            }
            if(phase==1){
                queued=true;UUID owner=mc.player.getUUID();server.execute(()->{try{
                    var p=server.getPlayerList().getPlayer(owner);var actorServer=(HonkistasPrototypeEntity)p.serverLevel().getEntity(actorId);
                    actorServer.setNoAi(false);
                    if(!actorServer.startControlledRoute(List.of(BlockPos.containing(start.add(6,0,0))),.8,400))throw new IllegalStateException("Grounded route refused");
                    queued=false;
                }catch(RuntimeException e){error=e.toString();}});
            }
            if(phase<3){phase++;ticks=0;requested=false;readyAt=System.nanoTime();return false;}
            if(phase==3){
                queued=true;routeDone=false;UUID owner=mc.player.getUUID();
                server.execute(()->{try{
                    var p=server.getPlayerList().getPlayer(owner);
                    var actual=(HonkistasPrototypeEntity)p.serverLevel().getEntity(actorId);
                    cueStart=actual.position();
                    if(!actual.startControlledRoute(List.of(BlockPos.containing(start)),.6,400) || !actual.startBeatCue())
                        throw new IllegalStateException("Server return-route beat cue refused");
                    queued=false;
                }catch(RuntimeException failure){error=failure.toString();}});
                phase=4;ticks=0;requested=false;readyAt=System.nanoTime();return false;
            }
            if(phase==4){phase=5;ticks=0;requested=false;readyAt=System.nanoTime();return false;}
            UUID owner=mc.player.getUUID();applied=false;
            server.execute(()->{var p=server.getPlayerList().getPlayer(owner);var level=p.serverLevel();var actorServer=level.getEntity(actorId);
                if(actorServer!=null)actorServer.discard();saved.forEach(level::setBlockAndUpdate);saved.clear();
                level.setDayTime(day);p.setNoGravity(gravity);p.teleportTo(level,original.x,original.y,original.z,yaw,pitch);applied=true;});
            phase=6;return false;
        }
        if(!applied)return false;
        mc.options.hideGui=hideGui;mc.options.setCameraType(camera);original=null;
        JetSetCraft.LOGGER.info("JETSETCRAFT_HONKISTAS_GROUNDED_PASS actual original actor navigation, grounded client displacement and server-synced finite upper-body cue; not skating/attack/event acceptance");
        return true;
    }
    public static void restore(Minecraft mc){
        var server=mc.getSingleplayerServer();
        if(original==null||server==null||mc.player==null)return;
        UUID owner=mc.player.getUUID();
        server.submit(()->{
            var p=server.getPlayerList().getPlayer(owner);if(p==null)return;
            var level=p.serverLevel();var actor=actorId==null?null:level.getEntity(actorId);
            if(actor!=null)actor.discard();saved.forEach(level::setBlockAndUpdate);saved.clear();
            level.setDayTime(day);p.setNoGravity(gravity);
            p.teleportTo(level,original.x,original.y,original.z,yaw,pitch);
        }).join();
        mc.options.hideGui=hideGui;mc.options.setCameraType(camera);original=null;
    }
    private HonkistasGroundedVisualAudit(){}
}
