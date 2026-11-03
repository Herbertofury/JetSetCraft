package com.herberto.jetsetcraft.entity;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.level.Level;
import net.minecraftforge.network.NetworkHooks;

/** A shootable stronghold weak point. It has real health and never changes the supporting terrain. */
public final class LibertyCoreEntity extends PathfinderMob {
    private static final EntityDataAccessor<String> KIND = SynchedEntityData.defineId(LibertyCoreEntity.class, EntityDataSerializers.STRING);
    public LibertyCoreEntity(EntityType<? extends LibertyCoreEntity> type, Level level) {
        super(type, level); setNoAi(true); setPersistenceRequired(); setNoGravity(true); xpReward = 0;
    }
    public static AttributeSupplier.Builder createAttributes() {
        return PathfinderMob.createMobAttributes().add(Attributes.MAX_HEALTH, 120).add(Attributes.MOVEMENT_SPEED, 0)
                .add(Attributes.KNOCKBACK_RESISTANCE, 1).add(Attributes.FOLLOW_RANGE, 0);
    }
    @Override protected void defineSynchedData() { super.defineSynchedData(); entityData.define(KIND, "FABRICATOR"); }
    public String kind() { return entityData.get(KIND); }
    public void kind(String kind) { entityData.set(KIND, kind); }
    @Override public boolean isPushable() { return false; }
    @Override public boolean canBeLeashed(net.minecraft.world.entity.player.Player player) { return false; }
    @Override public void addAdditionalSaveData(CompoundTag tag) { super.addAdditionalSaveData(tag); tag.putString("LibertyKind", kind()); }
    @Override public void readAdditionalSaveData(CompoundTag tag) { super.readAdditionalSaveData(tag); kind(tag.getString("LibertyKind")); setNoAi(true); }
    @Override public Packet<ClientGamePacketListener> getAddEntityPacket() { return NetworkHooks.getEntitySpawningPacket(this); }
}
