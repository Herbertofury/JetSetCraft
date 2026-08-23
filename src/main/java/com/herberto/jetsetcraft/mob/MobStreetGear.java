package com.herberto.jetsetcraft.mob;

import com.herberto.jetsetcraft.JetSetCraft;
import com.herberto.jetsetcraft.item.RideGearItem;
import com.herberto.jetsetcraft.movement.RideStyle;
import com.herberto.jetsetcraft.network.JetSetNetwork;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.decoration.ArmorStand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

/**
 * Persistent, physical Street Gear attached to the original mob.
 * No vanilla or third-party entity class is replaced, subclassed, or registry-overridden.
 */
public final class MobStreetGear {
    public static final String ROOT_KEY = JetSetCraft.MOD_ID + ":street_gear";
    private static final int SCHEMA = 1;

    public record Snapshot(ItemStack stack, MobRideRig rig, StreetGearAcquisition acquisition) {
        public boolean equipped() {
            return !stack.isEmpty() && stack.getItem() instanceof RideGearItem;
        }

        public RideStyle style() {
            return stack.getItem() instanceof RideGearItem gear ? gear.style() : RideStyle.NONE;
        }
    }

    public record EquipResult(boolean equipped, ItemStack previous) {
        public static EquipResult rejected() {
            return new EquipResult(false, ItemStack.EMPTY);
        }
    }

    /** Only real source-owned mobs participate. Players, display entities, and unusual living helpers stay untouched. */
    public static boolean eligible(LivingEntity entity) {
        return entity instanceof Mob && !(entity instanceof Player) && !(entity instanceof ArmorStand)
                && entity.isAlive() && !entity.isRemoved() && !MobRideRigResolver.incompatible(entity);
    }

    public static boolean hasStoredState(LivingEntity entity) {
        return entity != null && entity.getPersistentData().contains(ROOT_KEY, Tag.TAG_COMPOUND);
    }

    public static boolean hasGear(LivingEntity entity) {
        return snapshot(entity).equipped();
    }

    /** Read-only view. Corrupt or foreign payloads fail closed instead of being interpreted as equipment. */
    public static Snapshot snapshot(LivingEntity entity) {
        if (!hasStoredState(entity)) return emptySnapshot();
        CompoundTag tag = entity.getPersistentData().getCompound(ROOT_KEY);
        ItemStack stack = readPhysicalStack(tag);
        if (stack.isEmpty()) return emptySnapshot();
        return new Snapshot(stack, MobRideRig.byId(tag.getString("Rig")),
                StreetGearAcquisition.byId(tag.getString("Acquisition")));
    }

    public static EquipResult equip(LivingEntity entity, ItemStack offered, StreetGearAcquisition acquisition,
                                    boolean allowSwap) {
        if (entity == null || entity.level().isClientSide || !eligible(entity) || offered == null
                || offered.isEmpty() || !(offered.getItem() instanceof RideGearItem)) {
            return EquipResult.rejected();
        }
        Snapshot current = snapshot(entity);
        if (current.equipped() && !allowSwap) return EquipResult.rejected();

        ItemStack physical = offered.copy();
        physical.setCount(1);
        StreetGearAcquisition source = acquisition == null ? StreetGearAcquisition.RESTORED : acquisition;
        CompoundTag tag = createTag(physical, MobRideRigResolver.resolve(entity), source,
                entity.level().getGameTime());
        entity.getPersistentData().put(ROOT_KEY, tag);
        JetSetNetwork.syncMobGear(entity);
        return new EquipResult(true, current.stack().copy());
    }

    public static ItemStack unequip(LivingEntity entity) {
        if (entity == null || entity.level().isClientSide) return ItemStack.EMPTY;
        ItemStack stack = snapshot(entity).stack().copy();
        entity.getPersistentData().remove(ROOT_KEY);
        JetSetNetwork.syncMobGear(entity);
        return stack;
    }

    /**
     * Normalize data loaded from disk/datapacks and refresh the anatomy rig. A valid physical gear stack is never
     * silently deleted: if a datapack later marks the source mob incompatible, the item is materialized in-world.
     */
    public static void refreshRig(LivingEntity entity) {
        if (entity == null || entity.level().isClientSide || !hasStoredState(entity)) return;
        CompoundTag current = entity.getPersistentData().getCompound(ROOT_KEY);
        ItemStack stack = readPhysicalStack(current);
        if (stack.isEmpty()) {
            entity.getPersistentData().remove(ROOT_KEY);
            JetSetNetwork.syncMobGear(entity);
            return;
        }
        if (!eligible(entity)) {
            entity.getPersistentData().remove(ROOT_KEY);
            entity.spawnAtLocation(stack.copy());
            JetSetNetwork.syncMobGear(entity);
            return;
        }

        long equippedAt = current.contains("EquippedAt", Tag.TAG_LONG)
                ? current.getLong("EquippedAt") : entity.level().getGameTime();
        CompoundTag normalized = createTag(stack, MobRideRigResolver.resolve(entity),
                StreetGearAcquisition.byId(current.getString("Acquisition")), equippedAt);
        if (!normalized.equals(current)) {
            entity.getPersistentData().put(ROOT_KEY, normalized);
            JetSetNetwork.syncMobGear(entity);
        }
    }

    public static Snapshot emptySnapshot() {
        return new Snapshot(ItemStack.EMPTY, MobRideRig.GENERIC, StreetGearAcquisition.RESTORED);
    }

    private static ItemStack readPhysicalStack(CompoundTag tag) {
        if (tag == null || !tag.contains("Stack", Tag.TAG_COMPOUND)) return ItemStack.EMPTY;
        ItemStack stack = ItemStack.of(tag.getCompound("Stack"));
        if (stack.isEmpty() || !(stack.getItem() instanceof RideGearItem)) return ItemStack.EMPTY;
        ItemStack physical = stack.copy();
        physical.setCount(1);
        return physical;
    }

    private static CompoundTag createTag(ItemStack stack, MobRideRig rig, StreetGearAcquisition acquisition,
                                         long equippedAt) {
        ItemStack physical = stack.copy();
        physical.setCount(1);
        CompoundTag tag = new CompoundTag();
        tag.putInt("Schema", SCHEMA);
        tag.put("Stack", physical.save(new CompoundTag()));
        tag.putString("Rig", (rig == null ? MobRideRig.GENERIC : rig).id());
        tag.putString("Acquisition", (acquisition == null ? StreetGearAcquisition.RESTORED : acquisition).id());
        tag.putLong("EquippedAt", Math.max(0L, equippedAt));
        return tag;
    }

    private MobStreetGear() {}
}
