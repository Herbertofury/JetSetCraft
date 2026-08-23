package com.herberto.jetsetcraft.mob;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.BlockSource;
import net.minecraft.core.dispenser.OptionalDispenseItemBehavior;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.DispenserBlock;
import net.minecraft.world.phys.AABB;

import java.util.Comparator;

/** Redstone-native equipment path that leaves ordinary dispenser behavior intact when no mob is present. */
public final class StreetGearDispenserBehavior extends OptionalDispenseItemBehavior {
    @Override
    protected ItemStack execute(BlockSource source, ItemStack stack) {
        Direction direction = source.getBlockState().getValue(DispenserBlock.FACING);
        BlockPos targetPos = source.getPos().relative(direction);
        AABB area = new AABB(targetPos).inflate(0.45D);
        LivingEntity target = source.getLevel().getEntitiesOfClass(LivingEntity.class, area,
                        entity -> MobStreetGear.eligible(entity) && !MobStreetGear.hasGear(entity))
                .stream()
                .min(Comparator.<LivingEntity>comparingDouble(entity -> entity.distanceToSqr(
                        targetPos.getX() + 0.5D, targetPos.getY() + 0.5D, targetPos.getZ() + 0.5D)))
                .orElse(null);
        if (target != null && MobStreetGear.equip(target, stack, StreetGearAcquisition.DISPENSER, false).equipped()) {
            stack.shrink(1);
            setSuccess(true);
            return stack;
        }
        setSuccess(true);
        return super.execute(source, stack);
    }
}
