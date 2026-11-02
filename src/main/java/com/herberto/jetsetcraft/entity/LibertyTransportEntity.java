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
import net.minecraft.util.Mth;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.Level;
import net.minecraftforge.network.NetworkHooks;

/** A visible, server-driven passenger pod / extraction ship, never a usable overworld vehicle. */
public final class LibertyTransportEntity extends Entity {
    private static final EntityDataAccessor<Boolean> SHIP = SynchedEntityData.defineId(LibertyTransportEntity.class, EntityDataSerializers.BOOLEAN);
    private int interpolationSteps;
    private double targetX, targetY, targetZ;
    private float targetYaw, targetPitch;
    public LibertyTransportEntity(EntityType<? extends LibertyTransportEntity> type, Level level) {
        super(type, level); noPhysics = true; setNoGravity(true);
    }
    @Override protected void defineSynchedData() { entityData.define(SHIP, false); }
    public boolean ship() { return entityData.get(SHIP); }
    public void setShip(boolean ship) { entityData.set(SHIP, ship); }
    @Override public void tick() {
        super.tick();
        if (level().isClientSide) {
            if (interpolationSteps > 0) {
                double fraction = 1.0 / interpolationSteps--;
                setPos(Mth.lerp(fraction, getX(), targetX), Mth.lerp(fraction, getY(), targetY), Mth.lerp(fraction, getZ(), targetZ));
                setRot(getYRot() + Mth.wrapDegrees(targetYaw - getYRot()) * (float) fraction,
                        Mth.lerp((float) fraction, getXRot(), targetPitch));
            }
        } else if (!LibertyWorld.isLiberty(level()) || !LibertyManager.validOwned(this)) discard();
    }
    @Override public void lerpTo(double x, double y, double z, float yaw, float pitch, int steps, boolean teleport) {
        if (!level().isClientSide) { super.lerpTo(x, y, z, yaw, pitch, steps, teleport); return; }
        // Apply network targets inside the vehicle tick, before its passengers tick. Base Entity's immediate
        // packet move gives the pod and rider different old positions, making the camera separate in descent.
        targetX = x; targetY = y; targetZ = z; targetYaw = yaw; targetPitch = pitch;
        interpolationSteps = Math.max(1, Math.min(3, steps));
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
