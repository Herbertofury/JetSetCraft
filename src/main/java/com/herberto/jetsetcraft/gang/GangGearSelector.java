package com.herberto.jetsetcraft.gang;

import com.herberto.jetsetcraft.mob.MobRideRig;
import com.herberto.jetsetcraft.mob.MobRideRigResolver;
import com.herberto.jetsetcraft.registry.ModItems;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

/** Deterministic species-aware Street Gear selection for Boombox casts. */
public final class GangGearSelector {
    public static ItemStack forActor(LivingEntity entity, int memberIndex) {
        MobRideRig rig = MobRideRigResolver.resolve(entity);
        Item item = switch (rig) {
            case BODY_CONTACT, AQUATIC -> (memberIndex & 1) == 0 ? ModItems.HOVERBOARD.get() : ModItems.STREET_BOARD.get();
            case MULTI_LEG -> (memberIndex % 3) == 0 ? ModItems.HOVERBOARD.get() : ModItems.QUAD_SKATES.get();
            case AERIAL -> (memberIndex & 1) == 0 ? ModItems.HOVERBOARD.get() : ModItems.SCOOTER.get();
            case QUADRUPED -> (memberIndex % 3) == 0 ? ModItems.STREET_BOARD.get() : ModItems.QUAD_SKATES.get();
            case BIPED -> switch (Math.floorMod(memberIndex, 4)) {
                case 0 -> ModItems.INLINE_SKATES.get();
                case 1 -> ModItems.QUAD_SKATES.get();
                case 2 -> ModItems.STREET_BOARD.get();
                default -> ModItems.SCOOTER.get();
            };
            case GENERIC -> (memberIndex & 1) == 0 ? ModItems.INLINE_SKATES.get() : ModItems.HOVERBOARD.get();
        };
        return new ItemStack(item);
    }

    private GangGearSelector() {}
}
