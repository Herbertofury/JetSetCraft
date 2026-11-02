package com.herberto.jetsetcraft.entity;

import com.herberto.jetsetcraft.liberty.LibertyManager;
import com.herberto.jetsetcraft.liberty.LibertyWorld;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.Level;
import net.minecraftforge.network.NetworkHooks;

/** A visible, server-driven passenger pod / extraction ship, never a usable overworld vehicle. */
public final class LibertyTransportEntity extends Entity {
    private static final EntityDataAccessor<Boolean> SHIP = SynchedEntityData.defineId(LibertyTransportEntity.class, EntityDataSerializers.BOOLEAN);
    public LibertyTransportEntity(EntityType<? extends LibertyTransportEntity> type, Level level) {
        super(type, level); noPhysics = true; setNoGravity(true);
    }
    @Override protected void defineSynchedData() { entityData.define(SHIP, false); }
    public boolean ship() { return entityData.get(SHIP); }
    public void setShip(boolean ship) { entityData.set(SHIP, ship); }
    @Override public void tick() {
        super.tick();
        if (!level().isClientSide && (!LibertyWorld.isLiberty(level()) || !LibertyManager.validOwned(this))) discard();
    }
    @Override public boolean hurt(DamageSource source, float amount) { return false; }
    @Override public boolean isPickable() { return ship(); }
    @Override public boolean isPushable() { return false; }
    @Override protected boolean canAddPassenger(Entity passenger) {
        return getPassengers().isEmpty() && passenger instanceof ServerPlayer player && LibertyManager.belongsTo(this, player);
    }
    @Override public double getPassengersRidingOffset() { return ship() ? .55 : .15; }
    @Override protected void readAdditionalSaveData(CompoundTag tag) { setShip(tag.getBoolean("Ship")); }
    @Override protected void addAdditionalSaveData(CompoundTag tag) { tag.putBoolean("Ship", ship()); }
    @Override public Packet<ClientGamePacketListener> getAddEntityPacket() { return NetworkHooks.getEntitySpawningPacket(this); }
}
