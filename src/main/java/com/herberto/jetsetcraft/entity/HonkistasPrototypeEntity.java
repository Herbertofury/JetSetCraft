package com.herberto.jetsetcraft.entity;

import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import com.herberto.jetsetcraft.mob.MobStreetGear;
import com.herberto.jetsetcraft.movement.RideStyle;
import net.minecraft.world.phys.Vec3;
import java.util.EnumSet;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.ai.goal.WaterAvoidingRandomStrollGoal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraftforge.network.NetworkHooks;

/** Original theatrical visual prototype. No natural spawns, reward, gang substitution or named-character likeness. */
public final class HonkistasPrototypeEntity extends PathfinderMob {
    public static final int BEAT_CUE_TICKS=48;
    private static final EntityDataAccessor<Integer> BEAT_CUE=SynchedEntityData.defineId(
            HonkistasPrototypeEntity.class,EntityDataSerializers.INT);
    private static final EntityDataAccessor<Integer> SKATE_INPUT=SynchedEntityData.defineId(
            HonkistasPrototypeEntity.class,EntityDataSerializers.INT);
    @Override protected void defineSynchedData(){super.defineSynchedData();entityData.define(BEAT_CUE,0);entityData.define(SKATE_INPUT,0);}
    public enum SkateInput { PUSH, COAST, BRAKE }
    public enum SkateStatus { IDLE, RUNNING, COMPLETED, CANCELLED, FAILED_CONTACT, FAILED_OBSTACLE, FAILED_GEAR }
    private SkateStatus skateStatus=SkateStatus.IDLE;
    private Vec3 skateHeading=Vec3.ZERO;
    private long skateDeadline;
    public SkateStatus skateStatus(){return skateStatus;}
    /** Synced read-only input channel; null means the finite probe is not active. */
    public SkateInput skateInput(){int v=entityData.get(SKATE_INPUT);return v>=1&&v<=3?SkateInput.values()[v-1]:null;}
    private boolean hasInlineGear(){var gear=MobStreetGear.snapshot(this);return gear.equipped()&&gear.style()==RideStyle.INLINE;}
    public boolean startSkateControl(SkateInput input,Vec3 heading,int durationTicks){
        if(level().isClientSide || !isAlive() || isNoAi() || isPassenger() || isInWater() || isInLava()
                || !onGround() || !hasInlineGear() || skateStatus==SkateStatus.RUNNING || routeStatus==RouteStatus.RUNNING
                || input==null || heading==null || !Double.isFinite(heading.x) || !Double.isFinite(heading.y)
                || !Double.isFinite(heading.z) || durationTicks<1 || durationTicks>200)return false;
        Vec3 flat=new Vec3(heading.x,0,heading.z);
        if(flat.lengthSqr()<1.0e-8 || !Double.isFinite(flat.lengthSqr()))return false;
        skateHeading=flat.normalize();skateDeadline=level().getGameTime()+durationTicks;
        getNavigation().stop();skateStatus=SkateStatus.RUNNING;entityData.set(SKATE_INPUT,input.ordinal()+1);return true;
    }
    public boolean setSkateInput(SkateInput input){
        if(level().isClientSide || skateStatus!=SkateStatus.RUNNING || input==null)return false;
        entityData.set(SKATE_INPUT,input.ordinal()+1);return true; // Never renew the lease.
    }
    public void cancelSkateControl(){if(!level().isClientSide&&skateStatus==SkateStatus.RUNNING)finishSkate(SkateStatus.CANCELLED);}
    private void finishSkate(SkateStatus result){skateStatus=result;entityData.set(SKATE_INPUT,0);getNavigation().stop();}

    /** Original actor only: modify propulsion, then let vanilla perform its sole travel/collision/gravity step. */
    @Override public void travel(Vec3 input){
        boolean controlled=!level().isClientSide&&skateStatus==SkateStatus.RUNNING;
        if(controlled){
            if(level().getGameTime()>=skateDeadline)finishSkate(SkateStatus.COMPLETED);
            else if(!hasInlineGear())finishSkate(SkateStatus.FAILED_GEAR);
            else if(!onGround()||isPassenger()||isInWater()||isInLava()||isNoAi())finishSkate(SkateStatus.FAILED_CONTACT);
            else {
                Vec3 old=getDeltaMovement(),flat=new Vec3(old.x,0,old.z);
                double speed=flat.length();
                if(!Double.isFinite(speed)){finishSkate(SkateStatus.FAILED_CONTACT);}
                else {
                    SkateInput command=skateInput();Vec3 next=flat;
                    if(command==SkateInput.PUSH){
                        Vec3 direction=skateHeading;
                        if(speed>1.0e-6){
                            // Angular steering preserves velocity magnitude; no instant reversal.
                            double angle=Math.atan2(flat.x*skateHeading.z-flat.z*skateHeading.x,flat.dot(skateHeading));
                            double turn=Math.max(-RideStyle.INLINE.steering(),Math.min(RideStyle.INLINE.steering(),angle));
                            double c=Math.cos(turn),n=Math.sin(turn);
                            direction=new Vec3((flat.x*c-flat.z*n)/speed,0,(flat.z*c+flat.x*n)/speed);
                        }
                        // Do not clamp legitimate external impulses down to the propulsion cap.
                        double pushed=speed<RideStyle.INLINE.cruiseCap()?Math.min(RideStyle.INLINE.cruiseCap(),speed+RideStyle.INLINE.acceleration()):speed;
                        next=direction.scale(pushed);
                    }else if(command==SkateInput.BRAKE)next=flat.scale(.72);
                    // COAST leaves the real post-previous-travel velocity untouched. No momentum cache.
                    var sweep=getBoundingBox().expandTowards(next.x,0,next.z);
                    if(!supportedSkateSweep(sweep))finishSkate(SkateStatus.FAILED_CONTACT);
                    else if(!level().noCollision(this,sweep))finishSkate(SkateStatus.FAILED_OBSTACLE);
                    else setDeltaMovement(next.x,old.y,next.z);
                }
            }
        }
        // Zero walking acceleration only during this probe; native collisions and material drag remain intact.
        super.travel(controlled?Vec3.ZERO:input);
    }
    private boolean supportedSkateSweep(net.minecraft.world.phys.AABB sweep){
        // Cap query area even if a foreign mod supplies an extreme velocity; never load a chunk here.
        if(sweep.getXsize()>4||sweep.getZsize()>4)return false;
        int minX=net.minecraft.util.Mth.floor(sweep.minX+1.0e-7),maxX=net.minecraft.util.Mth.floor(sweep.maxX-1.0e-7);
        int minZ=net.minecraft.util.Mth.floor(sweep.minZ+1.0e-7),maxZ=net.minecraft.util.Mth.floor(sweep.maxZ-1.0e-7);
        int y=net.minecraft.util.Mth.floor(getY()-.05);
        for(int x=minX;x<=maxX;x++)for(int z=minZ;z<=maxZ;z++)if(!level().hasChunkAt(new BlockPos(x,y,z)))return false;
        for(int x=minX;x<=maxX;x++)for(int z=minZ;z<=maxZ;z++){
            BlockPos p=new BlockPos(x,y,z);
            if(!level().getBlockState(p).isFaceSturdy(level(),p,net.minecraft.core.Direction.UP))return false;
        }
        return true;
    }
    private final class SkateControlGoal extends Goal {
        SkateControlGoal(){setFlags(EnumSet.of(Flag.MOVE,Flag.LOOK,Flag.JUMP));}
        @Override public boolean canUse(){return skateStatus==SkateStatus.RUNNING;}
        @Override public boolean canContinueToUse(){return canUse();}
        @Override public void start(){getNavigation().stop();}
        @Override public void stop(){if(skateStatus==SkateStatus.RUNNING)finishSkate(SkateStatus.CANCELLED);}
    }
    /** A transient server-authoritative stage cue, never an attack or navigation replacement. */
    public boolean startBeatCue(){
        if(level().isClientSide || !isAlive() || beatCueTicksRemaining()!=0)return false;
        entityData.set(BEAT_CUE,BEAT_CUE_TICKS);return true;
    }
    public void cancelBeatCue(){if(!level().isClientSide)entityData.set(BEAT_CUE,0);}
    public int beatCueTicksRemaining(){return entityData.get(BEAT_CUE);}

    public enum RouteStatus { IDLE, RUNNING, COMPLETED, FAILED_PATH, FAILED_TIMEOUT, CANCELLED }
    private RouteStatus routeStatus=RouteStatus.IDLE;
    private List<BlockPos> route=List.of();
    private int completedWaypoints,remainingRepaths;
    private double routeSpeed;
    private long routeDeadline;
    private boolean routeNeedsNavigation;
    private String routeDiagnostic="not requested";

    /** Server-only, transient choreography; no teleport, rewards, event admission or skating. */
    public boolean startControlledRoute(List<BlockPos> points,double speed,int timeoutTicks){
        if(level().isClientSide || !isAlive() || routeStatus==RouteStatus.RUNNING || points==null
                || skateStatus==SkateStatus.RUNNING || points.isEmpty() || points.size()>16 || !Double.isFinite(speed) || speed<.1 || speed>1.5
                || timeoutTicks<1 || timeoutTicks>1200)return false;
        for(BlockPos point:points)if(point==null || point.distToCenterSqr(getX(),getY(),getZ())>32*32
                || level().isOutsideBuildHeight(point) || !level().hasChunkAt(point))return false;
        route=points.stream().map(BlockPos::immutable).toList();
        completedWaypoints=0;remainingRepaths=3;routeSpeed=speed;routeDeadline=level().getGameTime()+timeoutTicks;
        getNavigation().stop();routeNeedsNavigation=true;routeDiagnostic="requested";routeStatus=RouteStatus.RUNNING;return true;
    }
    public void cancelControlledRoute(){
        if(!level().isClientSide && routeStatus==RouteStatus.RUNNING)finishRoute(RouteStatus.CANCELLED);
    }
    public RouteStatus controlledRouteStatus(){return routeStatus;}
    public int controlledRouteCompletedWaypoints(){return completedWaypoints;}
    /** Bounded latest snapshot, retained after failure; no history list or world mutation. */
    public String controlledRouteDiagnostic(){return routeDiagnostic;}
    private void diagnoseRoute(String reason,BlockPos target){
        var path=getNavigation().getPath();
        routeDiagnostic=reason+" position="+position()+" onGround="+onGround()+" entityTicks="+tickCount
                +" target="+target+" targetBlock="+level().getBlockState(target)
                +" above="+level().getBlockState(target.above())+" support="+level().getBlockState(target.below())
                +" actorSupport="+level().getBlockState(blockPosition().below())
                +" navigationDone="+getNavigation().isDone()
                +" path="+(path==null?"null":("nodes="+path.getNodeCount()+" next="+path.getNextNodeIndex()
                    +" canReach="+path.canReach()+" target="+path.getTarget()));
    }
    /** Read-only conservative clearance check for the bounded endpoint approach. */
    public boolean canFinishGroundedApproach(BlockPos target){
        if(target==null || level().isClientSide || !level().hasChunkAt(target))return false;
        double dx=target.getX()+.5-getX(),dz=target.getZ()+.5-getZ();
        if(!onGround() || dx*dx+dz*dz>4 || Math.abs(getY()-target.getY())>.75)return false;
        // A conservative swept rectangle validates every touched floor cell first.
        // This deliberately rejects corner overhangs rather than promising step/cliff handling.
        var sweep=getBoundingBox().expandTowards(dx,0,dz);
        int minX=net.minecraft.util.Mth.floor(sweep.minX+1.0e-7),maxX=net.minecraft.util.Mth.floor(sweep.maxX-1.0e-7);
        int minZ=net.minecraft.util.Mth.floor(sweep.minZ+1.0e-7),maxZ=net.minecraft.util.Mth.floor(sweep.maxZ-1.0e-7);
        int floorY=net.minecraft.util.Mth.floor(getY()-.05);
        for(int x=minX;x<=maxX;x++)for(int z=minZ;z<=maxZ;z++){
            BlockPos floor=new BlockPos(x,floorY,z);
            if(!level().hasChunkAt(floor))return false;
        }
        for(int x=minX;x<=maxX;x++)for(int z=minZ;z<=maxZ;z++){
            BlockPos floor=new BlockPos(x,floorY,z);
            if(!level().getBlockState(floor).isFaceSturdy(level(),floor,net.minecraft.core.Direction.UP))return false;
        }
        if(!level().noCollision(this,sweep))return false;
        return true;
    }
    private void finishRoute(RouteStatus result){routeStatus=result;getNavigation().stop();route=List.of();}
    @Override public void tick(){
        super.tick();
        if(!level().isClientSide&&skateStatus==SkateStatus.RUNNING&&level().getGameTime()>=skateDeadline)finishSkate(SkateStatus.COMPLETED);
        if(!level().isClientSide && beatCueTicksRemaining()>0)
            entityData.set(BEAT_CUE,isAlive()?beatCueTicksRemaining()-1:0);
        // Real server time also bounds disabled-AI routes. No manually advanced fixture clock.
        if(!level().isClientSide && routeStatus==RouteStatus.RUNNING && level().getGameTime()>=routeDeadline)
            finishRoute(RouteStatus.FAILED_TIMEOUT);
    }
    private final class ControlledRouteGoal extends Goal {
        ControlledRouteGoal(){setFlags(EnumSet.of(Flag.MOVE,Flag.LOOK));}
        @Override public boolean canUse(){return routeStatus==RouteStatus.RUNNING;}
        @Override public boolean canContinueToUse(){return canUse();}
        @Override public boolean requiresUpdateEveryTick(){return true;}
        @Override public void start(){navigate();}
        @Override public void stop(){
            if(routeStatus==RouteStatus.RUNNING)finishRoute(RouteStatus.CANCELLED);
        }
        private void navigate(){
            if(routeStatus!=RouteStatus.RUNNING)return;
            routeNeedsNavigation=false;
            BlockPos p=route.get(completedWaypoints);
            String reason=null;
            if(!level().hasChunkAt(p))reason="target_chunk_not_loaded";
            else if(!level().getBlockState(p).getCollisionShape(level(),p).isEmpty())reason="target_collision";
            else if(!level().getBlockState(p.above()).getCollisionShape(level(),p.above()).isEmpty())reason="target_head_collision";
            else {
                // Discard a consumed native path before requesting the same endpoint again.
                // Otherwise native moveTo can retain the equivalent path's exhausted node cursor.
                getNavigation().stop();
                if(!getNavigation().moveTo(p.getX()+.5,p.getY(),p.getZ()+.5,routeSpeed))reason="moveTo_returned_false";
            }
            diagnoseRoute(reason==null?"navigation_started":reason,p);
            if(reason!=null)finishRoute(RouteStatus.FAILED_PATH);
        }
        @Override public void tick(){
            if(routeStatus!=RouteStatus.RUNNING)return;
            // A new command may arrive before the selector retires its previous finished goal.
            if(routeNeedsNavigation){navigate();if(routeStatus!=RouteStatus.RUNNING)return;}
            BlockPos p=route.get(completedWaypoints);
            double dx=getX()-p.getX()-.5,dz=getZ()-p.getZ()-.5;
            getLookControl().setLookAt(p.getX()+.5,getEyeY(),p.getZ()+.5);
            if(dx*dx+dz*dz<=.45*.45 && Math.abs(getY()-p.getY())<=.75 && onGround()){
                completedWaypoints++;remainingRepaths=3;
                if(completedWaypoints==route.size())finishRoute(RouteStatus.COMPLETED);
                else navigate();
            }else if(getNavigation().isDone()){
                // Native path completion has a wider waypoint tolerance than this controller.
                // Finish the final short segment through normal collision-aware MoveControl,
                // never by teleporting or weakening the route's arrival threshold.
                var path=getNavigation().getPath();
                if(path!=null && path.canReach() && dx*dx+dz*dz<=2*2
                        && Math.abs(getY()-p.getY())<=1.5){
                    // A reachable native path can finish during its normal hop/landing arc.
                    // Wait for actual physics contact, with the unchanged real route deadline.
                    if(!onGround()){diagnoseRoute("awaiting_native_landing",p);return;}
                    if(!canFinishGroundedApproach(p)){
                        diagnoseRoute("unsafe_grounded_final_approach",p);finishRoute(RouteStatus.FAILED_PATH);return;
                    }
                    getMoveControl().setWantedPosition(p.getX()+.5,p.getY(),p.getZ()+.5,routeSpeed);
                    diagnoseRoute("native_final_approach",p);
                }else if(path!=null && path.canReach() && remainingRepaths>0){
                    // Native navigation may consume its last node before our stricter endpoint.
                    // Recompute a real collision-aware path a bounded number of times; never
                    // enlarge arrival tolerance, force position, or extend the original deadline.
                    remainingRepaths--;navigate();
                }else{
                    diagnoseRoute("navigation_done_before_waypoint_repaths="+remainingRepaths,p);finishRoute(RouteStatus.FAILED_PATH);
                }
            }
        }
    }

    public HonkistasPrototypeEntity(EntityType<? extends HonkistasPrototypeEntity> type, Level level) {
        super(type,level);xpReward=0;
    }
    public static AttributeSupplier.Builder createAttributes(){
        return PathfinderMob.createMobAttributes().add(Attributes.MAX_HEALTH,24)
                .add(Attributes.MOVEMENT_SPEED,.24).add(Attributes.FOLLOW_RANGE,12);
    }
    @Override protected void registerGoals(){
        goalSelector.addGoal(0,new SkateControlGoal());
        goalSelector.addGoal(0,new ControlledRouteGoal());
        goalSelector.addGoal(1,new WaterAvoidingRandomStrollGoal(this,.8));
        goalSelector.addGoal(2,new LookAtPlayerGoal(this,Player.class,8));
        goalSelector.addGoal(3,new RandomLookAroundGoal(this));
    }
    @Override public Packet<ClientGamePacketListener> getAddEntityPacket(){return NetworkHooks.getEntitySpawningPacket(this);}
}
