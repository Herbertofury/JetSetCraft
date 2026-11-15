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

    private static final class RouteFixture {
        private static final TicketType<BlockPos> TICKET=TicketType.create(
                "jetsetcraft_original_route_test",BlockPos::compareTo,5);
        private final GameTestHelper helper;
        private final BlockPos origin;
        private final ChunkPos chunk;
        private final Map<BlockPos,BlockState> saved=new LinkedHashMap<>();
        private HonkistasPrototypeEntity actor;
        private Vec3 initial;
        private int ticks,phase;
        private double displacement;
        private boolean grounded,finished;
        RouteFixture(GameTestHelper helper){
            this.helper=helper;
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
                    check(!actor.startControlledRoute(List.of(origin.offset(60,1,1)),1,120),"Accepted out-of-range route");
                    check(!actor.startControlledRoute(java.util.Collections.nCopies(17,origin.offset(4,1,1)),1,120),"Accepted oversized route");
                    check(actor.startControlledRoute(List.of(origin.offset(4,1,1),origin.offset(4,1,4)),1,180),"Grounded route rejected");
                    check(!actor.startControlledRoute(List.of(origin.offset(1,1,1)),1,120),"Running route overwritten");
                    phase=2;return;
                }
                if(phase==2){
                    displacement=Math.max(displacement,actor.position().distanceTo(initial));
                    grounded|=actor.onGround() && actor.position().distanceTo(initial)>1;
                    if(actor.controlledRouteStatus()==RouteStatus.RUNNING)return;
                    check(actor.controlledRouteStatus()==RouteStatus.COMPLETED
                            && actor.controlledRouteCompletedWaypoints()==2 && displacement>3 && grounded,
                            "Real grounded navigation did not finish both waypoints: "+actor.controlledRouteStatus()+" moved="+displacement+" diagnostic="+actor.controlledRouteDiagnostic());
                    check(actor.startControlledRoute(List.of(origin.offset(1,1,1)),1,120),"Cancellation route rejected");
                    actor.cancelControlledRoute();
                    check(actor.controlledRouteStatus()==RouteStatus.CANCELLED && actor.getNavigation().isDone(),"Cancellation retained navigation");
                    // AI disabled deliberately to verify a genuine server-time deadline, not simulated movement.
                    actor.setNoAi(true);
                    check(actor.startControlledRoute(List.of(origin.offset(1,1,1)),1,3),"Deadline route rejected");
                    phase=3;return;
                }
                if(phase==3){
                    if(actor.controlledRouteStatus()==RouteStatus.RUNNING)return;
                    check(actor.controlledRouteStatus()==RouteStatus.FAILED_TIMEOUT,"Disabled-AI route escaped native timeout");
                    actor.setNoAi(false);
                    helper.getLevel().setBlockAndUpdate(origin.offset(1,1,1),Blocks.STONE.defaultBlockState());
                    helper.getLevel().setBlockAndUpdate(origin.offset(1,2,1),Blocks.STONE.defaultBlockState());
                    check(actor.startControlledRoute(List.of(origin.offset(1,1,1)),1,40),"Blocked route request rejected before goal diagnosis");
                    phase=4;return;
                }
                if(actor.controlledRouteStatus()==RouteStatus.RUNNING)return;
                check(actor.controlledRouteStatus()==RouteStatus.FAILED_PATH,"Solid waypoint not reported as path failure");
                System.out.println("JETSETCRAFT_GAMETEST_PASS original_actor_native_route moved="+displacement);
                close();helper.succeed();
            }catch(RuntimeException|Error failure){close();throw failure;}
        }
        private static void check(boolean condition,String message){if(!condition)throw new GameTestAssertException(message);}
        private void close(){
            if(finished)return;finished=true;
            if(actor!=null){actor.cancelControlledRoute();actor.discard();}
            saved.forEach((pos,state)->helper.getLevel().setBlockAndUpdate(pos,state));
            helper.getLevel().getChunkSource().removeRegionTicket(TICKET,chunk,3,origin);
        }
    }
}
