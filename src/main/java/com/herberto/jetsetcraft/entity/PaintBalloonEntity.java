package com.herberto.jetsetcraft.entity;

import com.herberto.jetsetcraft.graffiti.PaintColor;
import com.herberto.jetsetcraft.graffiti.PaintSplash;
import com.herberto.jetsetcraft.item.PaintBalloonItem;
import com.herberto.jetsetcraft.registry.ModEntities;
import com.herberto.jetsetcraft.registry.ModItems;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.ThrowableItemProjectile;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

public final class PaintBalloonEntity extends ThrowableItemProjectile {
    public PaintBalloonEntity(EntityType<? extends PaintBalloonEntity> type, Level level) {
        super(type, level);
    }

    public PaintBalloonEntity(Level level, LivingEntity owner, ItemStack stack) {
        super(ModEntities.PAINT_BALLOON.get(), owner, level);
        setItem(stack.copyWithCount(1));
    }

    public PaintBalloonEntity(Level level, double x, double y, double z, ItemStack stack) {
        super(ModEntities.PAINT_BALLOON.get(), x, y, z, level);
        setItem(stack.copyWithCount(1));
    }

    @Override
    protected Item getDefaultItem() {
        return ModItems.PAINT_BALLOONS.get(PaintColor.RED).get();
    }

    @Override
    protected void onHitBlock(BlockHitResult hit) {
        if (level() instanceof ServerLevel server) {
            Vec3 origin = hit.getLocation().add(Vec3.atLowerCornerOf(hit.getDirection().getNormal()).scale(0.30D))
                    .subtract(getDeltaMovement().scale(0.30D));
            PaintColor color = getItem().getItem() instanceof PaintBalloonItem balloon
                    ? balloon.color() : PaintColor.RED;
            Player player = getOwner() instanceof Player owner ? owner : null;
            PaintSplash.throwBalloon(server, this, player, origin, color);
        }
        super.onHitBlock(hit);
    }

    @Override
    protected void onHit(HitResult hit) {
        super.onHit(hit);
        if (level() instanceof ServerLevel server && hit instanceof EntityHitResult entityHit) {
            PaintColor color = getItem().getItem() instanceof PaintBalloonItem balloon
                    ? balloon.color() : PaintColor.RED;
            Player player = getOwner() instanceof Player owner ? owner : null;
            Vec3 origin = entityHit.getLocation().subtract(getDeltaMovement().scale(0.20D));
            PaintSplash.throwBalloon(server, this, player, origin, color);
        }
        if (!level().isClientSide) discard();
    }
}
