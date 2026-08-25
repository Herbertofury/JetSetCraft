package com.herberto.jetsetcraft.entity;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraftforge.network.NetworkHooks;
import com.herberto.jetsetcraft.graffiti.GraffitiCatalog;
import com.herberto.jetsetcraft.graffiti.CustomGraffiti;

public final class GraffitiEntity extends Entity {
    private static final EntityDataAccessor<Integer> VARIANT = SynchedEntityData.defineId(GraffitiEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Byte> FACE = SynchedEntityData.defineId(GraffitiEntity.class, EntityDataSerializers.BYTE);
    private static final EntityDataAccessor<String> CUSTOM = SynchedEntityData.defineId(GraffitiEntity.class, EntityDataSerializers.STRING);
    private static final EntityDataAccessor<Float> RENDER_WIDTH = SynchedEntityData.defineId(GraffitiEntity.class, EntityDataSerializers.FLOAT);
    private static final EntityDataAccessor<Float> RENDER_HEIGHT = SynchedEntityData.defineId(GraffitiEntity.class, EntityDataSerializers.FLOAT);
    private BlockPos supportPos;

    public GraffitiEntity(EntityType<? extends GraffitiEntity> type, Level level) {
        super(type, level);
        this.noPhysics = true;
        this.setNoGravity(true);
    }

    public void configure(BlockPos blockPos, Direction face, int variant) {
        configure(blockPos, face, variant, "");
    }

    public void configure(BlockPos blockPos, Direction face, int variant, String customPattern) {
        supportPos = blockPos.immutable();
        setVariant(variant);
        setFace(face);
        setCustomPattern(customPattern);
        setRenderSize(customPattern == null || customPattern.isEmpty() ? 0.0F : 1.6F,
                customPattern == null || customPattern.isEmpty() ? 0.0F : 1.0F);
        double x = blockPos.getX() + 0.5 + face.getStepX() * 0.505;
        double y = blockPos.getY() + 0.62;
        double z = blockPos.getZ() + 0.5 + face.getStepZ() * 0.505;
        setPos(x, y, z);
    }

    /** Configure a compact paint patch on any of Minecraft's six block faces. */
    public void configureSplash(BlockPos blockPos, Direction face, String customPattern, float size) {
        supportPos = blockPos.immutable();
        setVariant(0);
        setFace(face);
        setCustomPattern(customPattern);
        setRenderSize(size, size);
        setPos(blockPos.getX() + 0.5 + face.getStepX() * 0.505,
                blockPos.getY() + 0.5 + face.getStepY() * 0.505,
                blockPos.getZ() + 0.5 + face.getStepZ() * 0.505);
    }

    public int getVariant() {
        return entityData.get(VARIANT);
    }

    public void setVariant(int variant) {
        entityData.set(VARIANT, Math.floorMod(variant, GraffitiCatalog.size()));
    }

    public Direction getFace() {
        return Direction.from3DDataValue(entityData.get(FACE));
    }

    public void setFace(Direction face) {
        entityData.set(FACE, (byte) face.get3DDataValue());
    }

    public String getCustomPattern() { return entityData.get(CUSTOM); }

    public void setCustomPattern(String pattern) { entityData.set(CUSTOM, CustomGraffiti.normalize(pattern)); }

    public float getRenderWidth() { return entityData.get(RENDER_WIDTH); }
    public float getRenderHeight() { return entityData.get(RENDER_HEIGHT); }
    public BlockPos getSupportPos() { return supportPos; }

    public void setRenderSize(float width, float height) {
        entityData.set(RENDER_WIDTH, Float.isFinite(width) ? Math.max(0.0F, Math.min(2.5F, width)) : 0.0F);
        entityData.set(RENDER_HEIGHT, Float.isFinite(height) ? Math.max(0.0F, Math.min(2.5F, height)) : 0.0F);
    }

    @Override
    protected void defineSynchedData() {
        entityData.define(VARIANT, 0);
        entityData.define(FACE, (byte) Direction.NORTH.get3DDataValue());
        entityData.define(CUSTOM, "");
        entityData.define(RENDER_WIDTH, 0.0F);
        entityData.define(RENDER_HEIGHT, 0.0F);
    }

    @Override
    protected void readAdditionalSaveData(CompoundTag tag) {
        setVariant(tag.getInt("Variant"));
        setFace(Direction.from3DDataValue(tag.getByte("Face")));
        supportPos = tag.contains("SupportPos", Tag.TAG_LONG) ? BlockPos.of(tag.getLong("SupportPos")) : null;
        setCustomPattern(tag.getString("CustomPattern"));
        setRenderSize(tag.getFloat("RenderWidth"), tag.getFloat("RenderHeight"));
    }

    @Override
    protected void addAdditionalSaveData(CompoundTag tag) {
        tag.putInt("Variant", getVariant());
        tag.putByte("Face", (byte) getFace().get3DDataValue());
        if (supportPos != null) tag.putLong("SupportPos", supportPos.asLong());
        if (!getCustomPattern().isEmpty()) tag.putString("CustomPattern", getCustomPattern());
        if (getRenderWidth() > 0.0F) tag.putFloat("RenderWidth", getRenderWidth());
        if (getRenderHeight() > 0.0F) tag.putFloat("RenderHeight", getRenderHeight());
    }

    @Override
    public void tick() {
        super.tick();
        setDeltaMovement(0, 0, 0);
        if (!level().isClientSide && tickCount % 40 == 0 && supportPos != null
                && !level().getBlockState(supportPos).isFaceSturdy(level(), supportPos, getFace())) {
            discard();
        }
    }


    @Override
    public boolean hurt(DamageSource source, float amount) {
        if (!level().isClientSide && source.getEntity() instanceof Player) {
            discard();
            return true;
        }
        return false;
    }

    @Override
    public boolean isPickable() {
        return true;
    }

    @Override
    public Packet<ClientGamePacketListener> getAddEntityPacket() {
        return NetworkHooks.getEntitySpawningPacket(this);
    }
}
