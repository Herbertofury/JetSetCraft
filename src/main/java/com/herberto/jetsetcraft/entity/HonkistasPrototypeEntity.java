package com.herberto.jetsetcraft.entity;

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
    public enum RouteStatus { IDLE, RUNNING, COMPLETED, FAILED_PATH, FAILED_TIMEOUT, CANCELLED }
    private RouteStatus routeStatus=RouteStatus.IDLE;
    private List<BlockPos> route=List.of();
    private int completedWaypoints;
    private double routeSpeed;
    private long routeDeadline;
    private boolean routeNeedsNavigation;

    /** Server-only, transient choreography; no teleport, rewards, event admission or skating. */
    public boolean startControlledRoute(List<BlockPos> points,double speed,int timeoutTicks){
        if(level().isClientSide || !isAlive() || routeStatus==RouteStatus.RUNNING || points==null
                || points.isEmpty() || points.size()>16 || !Double.isFinite(speed) || speed<.1 || speed>1.5
                || timeoutTicks<1 || timeoutTicks>1200)return false;
        for(BlockPos point:points)if(point==null || point.distToCenterSqr(getX(),getY(),getZ())>32*32
                || level().isOutsideBuildHeight(point) || !level().hasChunkAt(point))return false;
        route=points.stream().map(BlockPos::immutable).toList();
        completedWaypoints=0;routeSpeed=speed;routeDeadline=level().getGameTime()+timeoutTicks;
        getNavigation().stop();routeNeedsNavigation=true;routeStatus=RouteStatus.RUNNING;return true;
    }
    public void cancelControlledRoute(){
        if(!level().isClientSide && routeStatus==RouteStatus.RUNNING)finishRoute(RouteStatus.CANCELLED);
    }
    public RouteStatus controlledRouteStatus(){return routeStatus;}
    public int controlledRouteCompletedWaypoints(){return completedWaypoints;}
    private void finishRoute(RouteStatus result){routeStatus=result;getNavigation().stop();route=List.of();}
    @Override public void tick(){
        super.tick();
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
            if(!level().hasChunkAt(p) || !level().getBlockState(p).getCollisionShape(level(),p).isEmpty()
                    || !level().getBlockState(p.above()).getCollisionShape(level(),p.above()).isEmpty()
                    || !getNavigation().moveTo(p.getX()+.5,p.getY(),p.getZ()+.5,routeSpeed))
                finishRoute(RouteStatus.FAILED_PATH);
        }
        @Override public void tick(){
            if(routeStatus!=RouteStatus.RUNNING)return;
            // A new command may arrive before the selector retires its previous finished goal.
            if(routeNeedsNavigation){navigate();if(routeStatus!=RouteStatus.RUNNING)return;}
            BlockPos p=route.get(completedWaypoints);
            double dx=getX()-p.getX()-.5,dz=getZ()-p.getZ()-.5;
            getLookControl().setLookAt(p.getX()+.5,getEyeY(),p.getZ()+.5);
            if(dx*dx+dz*dz<=.45*.45 && Math.abs(getY()-p.getY())<=.75){
                completedWaypoints++;
                if(completedWaypoints==route.size())finishRoute(RouteStatus.COMPLETED);
                else navigate();
            }else if(getNavigation().isDone())finishRoute(RouteStatus.FAILED_PATH);
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
        goalSelector.addGoal(0,new ControlledRouteGoal());
        goalSelector.addGoal(1,new WaterAvoidingRandomStrollGoal(this,.8));
        goalSelector.addGoal(2,new LookAtPlayerGoal(this,Player.class,8));
        goalSelector.addGoal(3,new RandomLookAroundGoal(this));
    }
    @Override public Packet<ClientGamePacketListener> getAddEntityPacket(){return NetworkHooks.getEntitySpawningPacket(this);}
}
