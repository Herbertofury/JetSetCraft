package com.herberto.jetsetcraft.gametest;

import com.herberto.jetsetcraft.JetSetCraft;
import com.herberto.jetsetcraft.entity.HonkistasPrototypeEntity;
import com.herberto.jetsetcraft.entity.HonkistasPrototypeEntity.SkateInput;
import com.herberto.jetsetcraft.entity.HonkistasPrototypeEntity.SkateStatus;
import com.herberto.jetsetcraft.mob.MobStreetGear;
import com.herberto.jetsetcraft.mob.StreetGearAcquisition;
import com.herberto.jetsetcraft.registry.ModEntities;
import com.herberto.jetsetcraft.registry.ModItems;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.gametest.framework.GameTestAssertException;
import net.minecraft.server.level.TicketType;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.gametest.GameTestHolder;
import net.minecraftforge.gametest.PrefixGameTestTemplate;
import java.util.*;

/** Native finite-propulsion probes, not visual skating or production encounter acceptance. */
@GameTestHolder(JetSetCraft.MOD_ID)
@PrefixGameTestTemplate(false)
public final class HonkistasSkateControlGameTests {
    @GameTest(template="hoverboard_empty",timeoutTicks=240)
    public static void nativeInlineCoastBrakeAndMaterialFrictionUseActualVelocity(GameTestHelper helper){
        new Fixture(helper,false).run();
    }
    @GameTest(template="hoverboard_empty",timeoutTicks=240)
    public static void nativeInlineBoundsContactObstacleAndLeaseFailClosed(GameTestHelper helper){
        new Fixture(helper,true).run();
    }
    private static final class Fixture {
        private static final TicketType<BlockPos> TICKET=TicketType.create("jetsetcraft_inline_probe",BlockPos::compareTo,5);
        final GameTestHelper helper;final boolean safety;
        final List<HonkistasPrototypeEntity> actors=new ArrayList<>();
        final Map<BlockPos,BlockState> saved=new LinkedHashMap<>();
        final BlockPos floor;final ChunkPos chunk;
        final double[] originalWalkingSpeed=new double[4];
        final double[] releaseX=new double[3],releaseSpeed=new double[3];
        int ticks,phase,started;boolean closed;
        Fixture(GameTestHelper helper,boolean safety){
            this.helper=helper;this.safety=safety;
            int x=safety?62208:62000,z=62000;
            chunk=new ChunkPos(new BlockPos(x,0,z));
            int y=helper.getLevel().getMinBuildHeight();
            for(int dx=0;dx<=20;dx++)for(int dz=0;dz<=13;dz++)
                y=Math.max(y,helper.getLevel().getHeight(Heightmap.Types.WORLD_SURFACE,x+dx,z+dz));
            floor=new BlockPos(x,y+8,z);
            check(!helper.getLevel().isOutsideBuildHeight(floor.above(4)),"No room for reversible inline test lane");
            try{
                keepTicking();
                for(int cx=-3;cx<=3;cx++)for(int cz=-3;cz<=3;cz++)helper.getLevel().getChunk(chunk.x+cx,chunk.z+cz);
                for(int dx=0;dx<=20;dx++)for(int dz=0;dz<=13;dz++)for(int dy=0;dy<=3;dy++){
                    BlockPos p=floor.offset(dx,dy,dz);saved.put(p,helper.getLevel().getBlockState(p));
                    BlockState state=dy>0?Blocks.AIR.defaultBlockState():(!safety&&dz>=7&&dz<=9?Blocks.ICE.defaultBlockState():Blocks.STONE.defaultBlockState());
                    helper.getLevel().setBlockAndUpdate(p,state);
                }
                for(int i=0;i<4;i++){
                    var actor=ModEntities.HONKISTAS_PROTOTYPE.get().create(helper.getLevel());check(actor!=null,"Original inline actor missing");
                    actors.add(actor);BlockPos at=floor.offset(2,1,2+3*i);
                    actor.moveTo(at.getX()+.5,at.getY(),at.getZ()+.5,270,0); // Initial spawn only.
                    originalWalkingSpeed[i]=actor.getAttribute(net.minecraft.world.entity.ai.attributes.Attributes.MOVEMENT_SPEED).getBaseValue();
                    actor.getAttribute(net.minecraft.world.entity.ai.attributes.Attributes.MOVEMENT_SPEED).setBaseValue(0);
                    actor.setPersistenceRequired();check(helper.getLevel().addFreshEntity(actor),"Native inline actor admission rejected");
                    if(i<3)equip(actor);
                }
            }catch(RuntimeException|Error error){close();throw error;}
        }
        void equip(HonkistasPrototypeEntity actor){
            check(MobStreetGear.equip(actor,new ItemStack(ModItems.INLINE_SKATES.get()),StreetGearAcquisition.COMMAND,false).equipped(),"Physical inline gear refused");
        }
        void keepTicking(){helper.getLevel().getChunkSource().addRegionTicket(TICKET,chunk,4,floor);}
        void run(){helper.onEachTick(this::tick);}
        void tick(){
            if(closed)return;
            try{
                keepTicking();check(++ticks<230,"Inline fixture exceeded bounded native deadline phase="+phase);
                if(phase>0)for(var actor:actors)check(actor.isAlive()&&helper.getLevel().getEntity(actor.getUUID())==actor,"Original actor lost from native world registry");
                if(phase==0){
                    check(ticks<=120,"Inline actors did not reach native grounded/ticking readiness: "+actors.stream().map(a->"ticks="+a.tickCount+" ground="+a.onGround()+" pos="+a.position()+" velocity="+a.getDeltaMovement()+" noAI="+a.isNoAi()+" resident="+helper.getLevel().isPositionEntityTicking(a.blockPosition())+" registered="+(helper.getLevel().getEntity(a.getUUID())==a)).toList());
                    if(actors.stream().anyMatch(a->!a.onGround()||!helper.getLevel().isPositionEntityTicking(a.blockPosition())
                            ||helper.getLevel().getEntity(a.getUUID())!=a))return;
                    for(int i=0;i<actors.size();i++)actors.get(i).getAttribute(net.minecraft.world.entity.ai.attributes.Attributes.MOVEMENT_SPEED).setBaseValue(originalWalkingSpeed[i]); // Restore owned fixture walking attribute after native contact.
                    var a=actors.get(0);var ungeared=actors.get(3);
                    check(!ungeared.startSkateControl(SkateInput.PUSH,new Vec3(1,0,0),60),"Ungeared actor accepted inline input");
                    check(!a.startSkateControl(SkateInput.PUSH,new Vec3(Double.NaN,0,0),60)
                            && !a.startSkateControl(SkateInput.PUSH,Vec3.ZERO,60)
                            && !a.startSkateControl(SkateInput.PUSH,new Vec3(1,0,0),201),"Invalid inline command accepted");
                    for(int i=0;i<3;i++)check(actors.get(i).startSkateControl(SkateInput.PUSH,new Vec3(1,0,0),100),"Native push command refused");
                    check(!a.startControlledRoute(List.of(floor.offset(4,1,2)),1,60),"Walking route overlapped inline control");
                    check(!a.startSkateControl(SkateInput.COAST,new Vec3(1,0,0),100),"New command replaced finite lease");
                    check(a.startBeatCue(),"Independent upper-body cue refused");
                    if(safety){
                        helper.getLevel().setBlockAndUpdate(floor.offset(3,1,2),Blocks.STONE.defaultBlockState());
                        helper.getLevel().setBlockAndUpdate(floor.offset(3,2,2),Blocks.STONE.defaultBlockState());
                        helper.getLevel().setBlockAndUpdate(floor.offset(2,0,5),Blocks.AIR.defaultBlockState());
                        actors.get(2).cancelSkateControl();
                        check(actors.get(2).startSkateControl(SkateInput.COAST,new Vec3(1,0,0),4),"Short lease refused");
                        equip(ungeared);check(ungeared.startSkateControl(SkateInput.PUSH,new Vec3(1,0,0),60),"Fresh gear not accepted");
                        ungeared.cancelSkateControl();check(ungeared.skateStatus()==SkateStatus.CANCELLED&&ungeared.skateInput()==null,"Cancel did not clear sync channel");
                    }
                    started=ticks;phase=1;return;
                }
                if(safety){
                    // Observe terminal mechanics first, then suppress unrelated random-stroll AI only.
                    // Gravity/collision and the cue clock remain native; no position/ground flags are assigned.
                    for(int i=0;i<3;i++)if(actors.get(i).skateStatus()!=SkateStatus.RUNNING)actors.get(i).getAttribute(net.minecraft.world.entity.ai.attributes.Attributes.MOVEMENT_SPEED).setBaseValue(0);
                    if(ticks-started<20)return;
                    check(actors.get(0).skateStatus()==SkateStatus.FAILED_OBSTACLE,"Native obstruction failed to stop propulsion: "+actors.get(0).skateStatus());
                    check(actors.get(0).getBoundingBox().maxX<=floor.getX()+3+.001,"Original actor crossed native blocking wall");
                    check(actors.get(1).skateStatus()==SkateStatus.FAILED_CONTACT&&actors.get(1).getY()<floor.getY()+1,"Missing floor did not yield to native falling");
                    check(actors.get(2).skateStatus()==SkateStatus.COMPLETED&&actors.get(2).skateInput()==null,"Short finite lease did not reset");
                    check(actors.get(0).beatCueTicksRemaining()>0,"Independent cue was cancelled by a locomotion failure");
                    pass("native_inline_contact_bounds");return;
                }
                if(phase==1){
                    if(ticks-started<35)return;
                    for(int i=0;i<3;i++){
                        var actor=actors.get(i);check(actor.skateStatus()==SkateStatus.RUNNING&&actor.onGround(),"Push lost supported control");
                        releaseX[i]=actor.getX();releaseSpeed[i]=actor.getDeltaMovement().horizontalDistance();
                        check(releaseX[i]>floor.getX()+2.6&&releaseSpeed[i]>0,"Native propulsion did not produce measured motion");
                    }
                    check(Math.abs(releaseSpeed[0]-releaseSpeed[1])<1.0e-5,"Paired stone actors reached unequal native release velocities");
                    actors.get(0).setSkateInput(SkateInput.COAST);actors.get(1).setSkateInput(SkateInput.BRAKE);actors.get(2).setSkateInput(SkateInput.COAST);
                    started=ticks;phase=2;return;
                }
                if(ticks-started<20)return;
                for(int i=0;i<3;i++)check(actors.get(i).skateStatus()==SkateStatus.RUNNING&&actors.get(i).onGround(),"Release phase lost native supported control");
                double coast=actors.get(0).getX()-releaseX[0],brake=actors.get(1).getX()-releaseX[1],ice=actors.get(2).getX()-releaseX[2];
                check(coast>0&&brake>=0&&brake<coast,"Actual braking did not shorten same-surface coast: "+coast+" / "+brake);
                check(ice/releaseSpeed[2]>coast/releaseSpeed[0],"Native ice retention did not exceed stone after normalizing measured release speed");
                System.out.println("JETSETCRAFT_INLINE_MEASURE stoneCoast="+coast+" stoneBrake="+brake+" iceCoast="+ice+" release="+Arrays.toString(releaseSpeed));
                pass("native_inline_friction_braking");
            }catch(RuntimeException|Error error){close();throw error;}
        }
        void pass(String name){close();System.out.println("JETSETCRAFT_GAMETEST_PASS "+name);helper.succeed();}
        void close(){if(closed)return;closed=true;for(var actor:actors){actor.cancelSkateControl();actor.cancelBeatCue();actor.discard();}saved.forEach((p,s)->helper.getLevel().setBlockAndUpdate(p,s));helper.getLevel().getChunkSource().removeRegionTicket(TICKET,chunk,4,floor);}
        static void check(boolean ok,String message){if(!ok)throw new GameTestAssertException(message);}
    }
}
