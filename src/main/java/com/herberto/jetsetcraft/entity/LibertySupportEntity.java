package com.herberto.jetsetcraft.entity;

import com.herberto.jetsetcraft.liberty.LibertyStratagemEngine;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraftforge.network.NetworkHooks;

import java.util.Optional;
import java.util.UUID;

/** Destructible, operation-owned hardware. Functional simulation lives in the server's stratagem engine. */
public final class LibertySupportEntity extends PathfinderMob {
    private static final EntityDataAccessor<String> ROLE = SynchedEntityData.defineId(LibertySupportEntity.class, EntityDataSerializers.STRING);
    private static final EntityDataAccessor<Optional<UUID>> OWNER = SynchedEntityData.defineId(LibertySupportEntity.class, EntityDataSerializers.OPTIONAL_UUID);
    private static final EntityDataAccessor<Optional<UUID>> OPERATION = SynchedEntityData.defineId(LibertySupportEntity.class, EntityDataSerializers.OPTIONAL_UUID);
    private static final EntityDataAccessor<Integer> AMMO = SynchedEntityData.defineId(LibertySupportEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Float> ENERGY = SynchedEntityData.defineId(LibertySupportEntity.class, EntityDataSerializers.FLOAT);
    private static final EntityDataAccessor<Boolean> FIRING = SynchedEntityData.defineId(LibertySupportEntity.class, EntityDataSerializers.BOOLEAN);
    private int life = 2400;
    private int lerpSteps;
    private double targetX, targetY, targetZ;
    private float targetYaw, targetPitch;

    public LibertySupportEntity(EntityType<? extends PathfinderMob> type, Level level) {
        super(type, level);
        setNoAi(true);
        setPersistenceRequired();
        setNoGravity(true);
    }
    public static AttributeSupplier.Builder createAttributes() {
        return LivingEntity.createLivingAttributes().add(Attributes.MAX_HEALTH, 70)
                .add(Attributes.KNOCKBACK_RESISTANCE, 1).add(Attributes.ARMOR, 6)
                .add(Attributes.MOVEMENT_SPEED, .28).add(Attributes.FOLLOW_RANGE, 64);
    }
    @Override protected void defineSynchedData() {
        super.defineSynchedData();
        entityData.define(ROLE, "SENTRY_MG"); entityData.define(OWNER, Optional.empty());
        entityData.define(OPERATION, Optional.empty()); entityData.define(AMMO, 0);
        entityData.define(ENERGY, 0f); entityData.define(FIRING, false);
    }
    public void configure(String role, UUID owner, UUID operation, int ammo, float health, int ticks) {
        entityData.set(ROLE, role); entityData.set(OWNER, Optional.of(owner));
        entityData.set(OPERATION, Optional.of(operation)); setAmmo(ammo); setEnergy(100);
        getAttribute(Attributes.MAX_HEALTH).setBaseValue(Math.max(10, Math.min(500, health)));
        setHealth(getMaxHealth()); life = Math.max(20, Math.min(7200, ticks));
        getPersistentData().putBoolean("jetsetcraft_liberty_support", true);
    }
    public String role() { return entityData.get(ROLE); }
    public UUID ownerId() { return entityData.get(OWNER).orElse(null); }
    public UUID operationId() { return entityData.get(OPERATION).orElse(null); }
    public int ammo() { return entityData.get(AMMO); }
    public void setAmmo(int amount) { entityData.set(AMMO, Math.max(0, Math.min(5000, amount))); }
    public float energy() { return entityData.get(ENERGY); }
    public void setEnergy(float amount) { entityData.set(ENERGY, Float.isFinite(amount) ? Math.max(0, Math.min(100, amount)) : 0); }
    public boolean firing() { return entityData.get(FIRING); }
    public void setFiring(boolean value) { entityData.set(FIRING, value); }
    public boolean vehicle() { return switch (role()) {
        case "PATRIOT", "EMANCIPATOR", "LUMBERER", "BREAKTHROUGH", "GUNNER_FRV", "SUPPLY_FRV", "MAELSTROM", "BASTION" -> true;
        default -> false;
    }; }
    public boolean emplacement() { return role().startsWith("EMPLACEMENT_"); }
    public boolean drone() { return role().startsWith("DOG_"); }
    public boolean mine() { return role().startsWith("MINE_") || role().equals("C4"); }
    @Override public void tick() {
        super.tick();
        if (level().isClientSide) {
            if (lerpSteps > 0) {
                double f = 1.0 / lerpSteps--;
                setPos(net.minecraft.util.Mth.lerp(f, getX(), targetX), net.minecraft.util.Mth.lerp(f, getY(), targetY), net.minecraft.util.Mth.lerp(f, getZ(), targetZ));
                setRot(getYRot() + net.minecraft.util.Mth.wrapDegrees(targetYaw - getYRot()) * (float) f,
                        net.minecraft.util.Mth.lerp((float) f, getXRot(), targetPitch));
            }
            return;
        }
        LibertyStratagemEngine engine = LibertyStratagemEngine.forOperation(operationId());
        if (--life <= 0 || engine == null || !engine.owns(this)) { discard(); return; }
        engine.tickSupport(this);
    }
    @Override public void lerpTo(double x, double y, double z, float yaw, float pitch, int steps, boolean teleport) {
        if (!level().isClientSide) { super.lerpTo(x, y, z, yaw, pitch, steps, teleport); return; }
        targetX=x;targetY=y;targetZ=z;targetYaw=yaw;targetPitch=pitch;lerpSteps=Math.max(1,Math.min(3,steps));
    }
    @Override public InteractionResult mobInteract(Player player, InteractionHand hand) {
        if (level().isClientSide) return InteractionResult.SUCCESS;
        if (player instanceof ServerPlayer server && server.getUUID().equals(ownerId())) {
            LibertyStratagemEngine supportEngine=LibertyStratagemEngine.forOperation(operationId());
            if (role().equals("SUPPLY") && supportEngine != null) {
                var result=supportEngine.collectDelivery(this);server.displayClientMessage(net.minecraft.network.chat.Component.literal(result.message()),true);
                return result.accepted()?InteractionResult.CONSUME:InteractionResult.FAIL;
            }
            if (!vehicle() && !emplacement()) return InteractionResult.PASS;
            LibertyStratagemEngine engine=LibertyStratagemEngine.forOperation(operationId());
            if (engine != null && engine.owns(this) && server.distanceToSqr(this) <= 25 && !isVehicle()) {
                if (server.startRiding(this,true)) { engine.mounted(this); return InteractionResult.CONSUME; }
                return InteractionResult.FAIL;
            }
        }
        return InteractionResult.PASS;
    }
    @Override protected boolean canAddPassenger(Entity passenger) {
        return (vehicle() || emplacement()) && getPassengers().isEmpty() && ownerId() != null && ownerId().equals(passenger.getUUID());
    }
    @Override public LivingEntity getControllingPassenger() { return getFirstPassenger() instanceof LivingEntity living ? living : null; }
    @Override public double getPassengersRidingOffset() { return vehicle() ? 1.0 : .45; }
    @Override public boolean isPushable() { return false; }
    @Override public boolean canBeLeashed(Player player) { return false; }
    @Override public boolean hurt(DamageSource source, float amount) {
        if (source.getEntity() != null && ownerId() != null && ownerId().equals(source.getEntity().getUUID())) return false;
        return super.hurt(source, amount);
    }
    @Override protected void dropCustomDeathLoot(DamageSource source, int looting, boolean recentlyHit) {}
    @Override public void addAdditionalSaveData(CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        tag.putString("SupportRole", role()); if(ownerId()!=null)tag.putUUID("SupportOwner",ownerId());
        if(operationId()!=null)tag.putUUID("SupportOperation",operationId());
        tag.putInt("SupportAmmo",ammo());tag.putFloat("SupportEnergy",energy());tag.putInt("SupportLife",life);
    }
    @Override public void readAdditionalSaveData(CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        entityData.set(ROLE,tag.getString("SupportRole"));
        entityData.set(OWNER,tag.hasUUID("SupportOwner")?Optional.of(tag.getUUID("SupportOwner")):Optional.empty());
        entityData.set(OPERATION,tag.hasUUID("SupportOperation")?Optional.of(tag.getUUID("SupportOperation")):Optional.empty());
        setAmmo(tag.getInt("SupportAmmo"));setEnergy(tag.getFloat("SupportEnergy"));life=Math.max(0,Math.min(7200,tag.getInt("SupportLife")));
    }
    @Override public Packet<ClientGamePacketListener> getAddEntityPacket() { return NetworkHooks.getEntitySpawningPacket(this); }
}
