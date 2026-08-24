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
        double x = blockPos.getX() + 0.5 + face.getStepX() * 0.505;
        double y = blockPos.getY() + 0.62;
        double z = blockPos.getZ() + 0.5 + face.getStepZ() * 0.505;
        setPos(x, y, z);
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

    @Override
    protected void defineSynchedData() {
        entityData.define(VARIANT, 0);
        entityData.define(FACE, (byte) Direction.NORTH.get3DDataValue());
        entityData.define(CUSTOM, "");
    }

    @Override
    protected void readAdditionalSaveData(CompoundTag tag) {
        setVariant(tag.getInt("Variant"));
        setFace(Direction.from3DDataValue(tag.getByte("Face")));
        supportPos = tag.contains("SupportPos", Tag.TAG_LONG) ? BlockPos.of(tag.getLong("SupportPos")) : null;
        setCustomPattern(tag.getString("CustomPattern"));
    }

    @Override
    protected void addAdditionalSaveData(CompoundTag tag) {
        tag.putInt("Variant", getVariant());
        tag.putByte("Face", (byte) getFace().get3DDataValue());
        if (supportPos != null) tag.putLong("SupportPos", supportPos.asLong());
        if (!getCustomPattern().isEmpty()) tag.putString("CustomPattern", getCustomPattern());
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
