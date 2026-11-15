package com.herberto.jetsetcraft.gametest;

import com.herberto.jetsetcraft.JetSetCraft;
import com.herberto.jetsetcraft.entity.HonkistasPrototypeEntity;
import com.herberto.jetsetcraft.entity.HonkistasPrototypeEntity.RouteStatus;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import net.minecraft.gametest.framework.GameTestAssertException;
import net.minecraft.server.level.TicketType;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import com.herberto.jetsetcraft.registry.ModEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraftforge.gametest.GameTestHolder;
import net.minecraftforge.gametest.PrefixGameTestTemplate;

@GameTestHolder(JetSetCraft.MOD_ID)
@PrefixGameTestTemplate(false)
public final class HonkistasPrototypeGameTests {
    @GameTest(template="hoverboard_empty",timeoutTicks=100)
    public static void originalActorRegistersAndRoundTripsWithoutGangSubstitution(GameTestHelper helper){
        var actor=ModEntities.HONKISTAS_PROTOTYPE.get().create(helper.getLevel());
        if(actor==null)throw new IllegalStateException("Original actor missing");
        BlockPos pos=helper.absolutePos(new BlockPos(1,2,1));
        actor.moveTo(pos.getX()+.5,pos.getY(),pos.getZ()+.5,0,0);
        try{
            if(!helper.getLevel().addFreshEntity(actor) || helper.getLevel().getEntity(actor.getUUID())!=actor)
                throw new IllegalStateException("No native original actor");
            var tag=new net.minecraft.nbt.CompoundTag();actor.save(tag);
            if(!tag.getString("id").equals("jetsetcraft:honkistas_prototype") || actor.getMaxHealth()!=24)
                throw new IllegalStateException("Original identity/attributes changed");
            var restored=net.minecraft.world.entity.EntityType.loadEntityRecursive(tag,helper.getLevel(),e->e);
            if(!(restored instanceof com.herberto.jetsetcraft.entity.HonkistasPrototypeEntity))
                throw new IllegalStateException("Native actor roundtrip changed class");
            restored.discard();helper.succeed();
        }finally{actor.discard();}
    }

    @GameTest(template="hoverboard_empty",timeoutTicks=360)
    public static void controlledRouteUsesNativeGroundedMovementAndTerminalStates(GameTestHelper helper){
        RouteFixture fixture=new RouteFixture(helper);
        helper.onEachTick(fixture::tick);
    }

    @GameTest(template="hoverboard_empty",timeoutTicks=360)
    public static void serverBeatCueSyncsAndExpiresWhileNativeRouteMoves(GameTestHelper helper){
        RouteFixture fixture=new RouteFixture(helper,true);helper.onEachTick(fixture::tick);
    }

    private static final class RouteFixture {
        private static final TicketType<BlockPos> TICKET=TicketType.create(
                "jetsetcraft_original_route_test",BlockPos::compareTo,5);
        private final GameTestHelper helper;
        private final BlockPos origin;
        private final ChunkPos chunk;
        private final Map<BlockPos,BlockState> saved=new LinkedHashMap<>();
        private HonkistasPrototypeEntity actor,metadataReplica;
        private Vec3 initial;
        private int ticks,phase;
        private double displacement;
        private boolean grounded,finished;
        private final boolean cueTest;
        private int cueStartTick,noAiCueStartTick;
        private boolean movedDuringCue,expiredCue;
        private Vec3 cueStartPosition;
        RouteFixture(GameTestHelper helper){this(helper,false);}
        RouteFixture(GameTestHelper helper,boolean cueTest){
            this.cueTest=cueTest;this.helper=helper;
            BlockPos anchor=helper.absolutePos(BlockPos.ZERO);
            chunk=new ChunkPos(anchor);
            // A natural underground fixture can receive falling gravel from above.
            // Place the reversible lane above the highest real surface, not under terrain.
            int surface=anchor.getY();
            for(int x=0;x<=5;x++)for(int z=0;z<=5;z++)
                surface=Math.max(surface,helper.getLevel().getHeight(
                        net.minecraft.world.level.levelgen.Heightmap.Types.MOTION_BLOCKING,anchor.getX()+x,anchor.getZ()+z));
            origin=new BlockPos(anchor.getX(),surface+16,anchor.getZ());
            check(origin.getY()+4<helper.getLevel().getMaxBuildHeight(),"No clear space for reversible route lane");
            try{
                keepTicking();
                for(int x=-3;x<=3;x++)for(int z=-3;z<=3;z++)helper.getLevel().getChunk(chunk.x+x,chunk.z+z);
                for(int x=0;x<=5;x++)for(int z=0;z<=5;z++)for(int y=0;y<=4;y++){
                    BlockPos p=origin.offset(x,y,z);saved.put(p,helper.getLevel().getBlockState(p));
                    helper.getLevel().setBlockAndUpdate(p,y==0?Blocks.STONE.defaultBlockState():Blocks.AIR.defaultBlockState());
                }
            }catch(RuntimeException|Error failure){close();throw failure;}
        }
        private void keepTicking(){helper.getLevel().getChunkSource().addRegionTicket(TICKET,chunk,3,origin);}
        private boolean ready(){
            for(int x:new int[]{0,5})for(int z:new int[]{0,5})
                if(!helper.getLevel().isPositionEntityTicking(origin.offset(x,1,z)))return false;
            return true;
        }
        void tick(){
            if(finished)return;
            try{
                keepTicking();check(++ticks<340,"Native route fixture timed out; phase="+phase
                        +" status="+(actor==null?"not spawned":actor.controlledRouteStatus())+" displacement="+displacement);
                if(phase==0){
                    check(ticks<=120,"Route arena failed native entity-ticking readiness");
                    if(!ready())return;
                    actor=ModEntities.HONKISTAS_PROTOTYPE.get().create(helper.getLevel());
                    check(actor!=null,"Original route actor missing");
                    BlockPos p=origin.offset(1,1,1);
                    actor.moveTo(p.getX()+.5,p.getY(),p.getZ()+.5,0,0); // Initial spawn placement only.
                    actor.setPersistenceRequired();check(helper.getLevel().addFreshEntity(actor),"Native route admission failed");
                    initial=actor.position();phase=1;return;
                }
                check(helper.getLevel().getEntity(actor.getUUID())==actor && actor.isAlive(),"Real route actor lost from world registry");
                check(ready(),"Native route arena stopped entity-ticking");
                if(phase==1){
                    // Native GroundPathNavigation cannot create a route before physics ground contact.
                    // The initial spawn has zero vertical speed; the first ticks must actually settle.
                    if(!actor.onGround())return; // Overall native-tick deadline remains enforced.
                    BlockPos approach=origin.offset(2,1,1);
                    check(actor.canFinishGroundedApproach(approach),"Clear supported short approach rejected");
                    helper.getLevel().setBlockAndUpdate(approach.below(),Blocks.AIR.defaultBlockState());
                    check(!actor.canFinishGroundedApproach(approach),"Short approach accepted unsupported floor");
                    helper.getLevel().setBlockAndUpdate(approach.below(),Blocks.STONE.defaultBlockState());
                    helper.getLevel().setBlockAndUpdate(approach.above(),Blocks.STONE.defaultBlockState());
                    check(!actor.canFinishGroundedApproach(approach),"Short approach crossed headroom obstruction");
                    helper.getLevel().setBlockAndUpdate(approach.above(),Blocks.AIR.defaultBlockState());
                    BlockPos diagonal=origin.offset(2,1,2),corner=origin.offset(1,0,2);
                    check(actor.canFinishGroundedApproach(diagonal),"Supported diagonal short approach rejected");
                    helper.getLevel().setBlockAndUpdate(corner,Blocks.AIR.defaultBlockState());
                    check(!actor.canFinishGroundedApproach(diagonal),"Diagonal approach accepted unsupported corner");
                    helper.getLevel().setBlockAndUpdate(corner,Blocks.STONE.defaultBlockState());
                    check(!actor.canFinishGroundedApproach(origin.offset(5,1,1)),"Endpoint approach exceeded two-block bound");
                    check(!actor.startControlledRoute(List.of(origin.offset(60,1,1)),1,120),"Accepted out-of-range route");
                    check(!actor.startControlledRoute(java.util.Collections.nCopies(17,origin.offset(4,1,1)),1,120),"Accepted oversized route");
                    check(actor.startControlledRoute(cueTest?List.of(origin.offset(4,1,1),origin.offset(4,1,4),origin.offset(1,1,1)):List.of(origin.offset(4,1,1),origin.offset(4,1,4)),cueTest?.6:1,cueTest?260:180),"Grounded route rejected");
                    check(!actor.startControlledRoute(List.of(origin.offset(1,1,1)),1,120),"Running route overwritten");
                    if(cueTest){
                        check(actor.startBeatCue(),"Server beat cue refused");cueStartTick=actor.tickCount;cueStartPosition=actor.position();
                        check(!actor.startBeatCue() && actor.beatCueTicksRemaining()==HonkistasPrototypeEntity.BEAT_CUE_TICKS,
                                "Overlapping cue replaced the active bounded gesture");
                        verifyCueMetadata();
                        var savedActor=new net.minecraft.nbt.CompoundTag();actor.save(savedActor);
                        var restored=net.minecraft.world.entity.EntityType.loadEntityRecursive(savedActor,helper.getLevel(),e->e);
                        check(restored instanceof HonkistasPrototypeEntity
                                        && ((HonkistasPrototypeEntity)restored).beatCueTicksRemaining()==0,
                                "Transient gesture resumed from a saved actor");
                        restored.discard();
                    }
                    phase=2;return;
                }
                if(cueTest && phase>=2 && !expiredCue){
                    int elapsed=actor.tickCount-cueStartTick;
                    check(actor.beatCueTicksRemaining()==Math.max(0,HonkistasPrototypeEntity.BEAT_CUE_TICKS-elapsed),
                            "Server cue failed exact native tick countdown");
                    if(actor.beatCueTicksRemaining()>0 && actor.position().distanceTo(cueStartPosition)>.5
                            && actor.controlledRouteStatus()==RouteStatus.RUNNING)movedDuringCue=true;
                    if(elapsed>=HonkistasPrototypeEntity.BEAT_CUE_TICKS){
                        check(actor.beatCueTicksRemaining()==0 && movedDuringCue,"Cue failed automatic reset or prevented real walking");
                        expiredCue=true;
                    }
                }
                if(phase==2){
                    displacement=Math.max(displacement,actor.position().distanceTo(initial));
                    grounded|=actor.onGround() && actor.position().distanceTo(initial)>1;
                    if(actor.controlledRouteStatus()==RouteStatus.RUNNING)return;
                    check(actor.controlledRouteStatus()==RouteStatus.COMPLETED
                            && actor.controlledRouteCompletedWaypoints()==(cueTest?3:2) && displacement>3 && grounded,
                            "Real grounded navigation did not finish both waypoints: "+actor.controlledRouteStatus()+" moved="+displacement+" diagnostic="+actor.controlledRouteDiagnostic());
                    if(cueTest && !expiredCue)return;
                    if(cueTest){
                        check(actor.startBeatCue(),"Completed cue could not restart");actor.cancelBeatCue();
                        check(actor.beatCueTicksRemaining()==0,"Explicit cue cancellation did not reset metadata");
                        verifyCueMetadata();
                    }
                    check(actor.startControlledRoute(List.of(origin.offset(1,1,1)),1,120),"Cancellation route rejected");
                    actor.cancelControlledRoute();
                    check(actor.controlledRouteStatus()==RouteStatus.CANCELLED && actor.getNavigation().isDone(),"Cancellation retained navigation");
                    // AI disabled deliberately to verify a genuine server-time deadline, not simulated movement.
                    actor.setNoAi(true);
                    if(cueTest){check(actor.startBeatCue(),"No-AI cue refused");noAiCueStartTick=actor.tickCount;}
                    check(actor.startControlledRoute(List.of(origin.offset(1,1,1)),1,3),"Deadline route rejected");
                    phase=3;return;
                }
                if(phase==3){
                    if(actor.controlledRouteStatus()==RouteStatus.RUNNING)return;
                    check(actor.controlledRouteStatus()==RouteStatus.FAILED_TIMEOUT,"Disabled-AI route escaped native timeout");
                    if(cueTest){
                        check(actor.beatCueTicksRemaining()==HonkistasPrototypeEntity.BEAT_CUE_TICKS-(actor.tickCount-noAiCueStartTick)
                                        && actor.beatCueTicksRemaining()<HonkistasPrototypeEntity.BEAT_CUE_TICKS,
                                "No-AI actor did not tick its server cue independently of failed navigation");
                        actor.cancelBeatCue();check(actor.beatCueTicksRemaining()==0,"No-AI cue cancellation failed");
                    }
                    actor.setNoAi(false);
                    helper.getLevel().setBlockAndUpdate(origin.offset(1,1,1),Blocks.STONE.defaultBlockState());
                    helper.getLevel().setBlockAndUpdate(origin.offset(1,2,1),Blocks.STONE.defaultBlockState());
                    check(actor.startControlledRoute(List.of(origin.offset(1,1,1)),1,40),"Blocked route request rejected before goal diagnosis");
                    phase=4;return;
                }
                if(actor.controlledRouteStatus()==RouteStatus.RUNNING)return;
                check(actor.controlledRouteStatus()==RouteStatus.FAILED_PATH,"Solid waypoint not reported as path failure");
                System.out.println("JETSETCRAFT_GAMETEST_PASS "+(cueTest?"original_actor_synced_beat_cue":"original_actor_native_route")+" moved="+displacement);
                close();helper.succeed();
            }catch(RuntimeException|Error failure){close();throw failure;}
        }
        private void verifyCueMetadata(){
            // Actual vanilla metadata packet codec, not a client progress assignment in the visual audit.
            // Consumes fixture-only dirty updates to exercise both active and default/reset packets.
            var values=actor.getEntityData().packDirty();
            if(metadataReplica==null)metadataReplica=ModEntities.HONKISTAS_PROTOTYPE.get().create(helper.getLevel());
            check(metadataReplica!=null,"Metadata receiver unavailable");
            var bytes=new net.minecraft.network.FriendlyByteBuf(io.netty.buffer.Unpooled.buffer());
            try{
                var packet=new net.minecraft.network.protocol.game.ClientboundSetEntityDataPacket(actor.getId(),values==null?List.of():values);
                packet.write(bytes);
                var decoded=new net.minecraft.network.protocol.game.ClientboundSetEntityDataPacket(bytes);
                metadataReplica.getEntityData().assignValues(decoded.packedItems());
                check(bytes.readableBytes()==0 && metadataReplica.beatCueTicksRemaining()==actor.beatCueTicksRemaining(),
                        "Native entity metadata lost the exact cue countdown");
            }finally{bytes.release();}
        }
        private static void check(boolean condition,String message){if(!condition)throw new GameTestAssertException(message);}
        private void close(){
            if(finished)return;finished=true;
            if(actor!=null){actor.cancelBeatCue();actor.cancelControlledRoute();actor.discard();}
            if(metadataReplica!=null)metadataReplica.discard();
            saved.forEach((pos,state)->helper.getLevel().setBlockAndUpdate(pos,state));
            helper.getLevel().getChunkSource().removeRegionTicket(TICKET,chunk,3,origin);
        }
    }
}
