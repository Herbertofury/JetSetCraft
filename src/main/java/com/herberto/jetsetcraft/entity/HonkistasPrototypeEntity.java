package com.herberto.jetsetcraft.entity;

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
    public HonkistasPrototypeEntity(EntityType<? extends HonkistasPrototypeEntity> type, Level level) {
        super(type,level);xpReward=0;
    }
    public static AttributeSupplier.Builder createAttributes(){
        return PathfinderMob.createMobAttributes().add(Attributes.MAX_HEALTH,24)
                .add(Attributes.MOVEMENT_SPEED,.24).add(Attributes.FOLLOW_RANGE,12);
    }
    @Override protected void registerGoals(){
        goalSelector.addGoal(1,new WaterAvoidingRandomStrollGoal(this,.8));
        goalSelector.addGoal(2,new LookAtPlayerGoal(this,Player.class,8));
        goalSelector.addGoal(3,new RandomLookAroundGoal(this));
    }
    @Override public Packet<ClientGamePacketListener> getAddEntityPacket(){return NetworkHooks.getEntitySpawningPacket(this);}
}
