package com.herberto.jetsetcraft.gang;

import com.herberto.jetsetcraft.JetSetCraft;
import com.herberto.jetsetcraft.mob.MobStreetGear;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.LivingEntity;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.Optional;
import java.util.UUID;

/** JetSetCraft-owned gang/challenge attachment layered onto the untouched source entity. */
public final class GangMemberState {
    public static final String ROOT_KEY = JetSetCraft.MOD_ID + ":gang_member";
    private static final int SCHEMA = 1;

    public record Snapshot(ResourceLocation gangId, String role, UUID challengeId, boolean ephemeral,
                           long expiresAt, boolean present) {
        public boolean inChallenge() {
            return challengeId != null;
        }
    }

    public static Snapshot snapshot(LivingEntity entity) {
        if (entity == null || !entity.getPersistentData().contains(ROOT_KEY, Tag.TAG_COMPOUND)) return empty();
        CompoundTag root = entity.getPersistentData().getCompound(ROOT_KEY);
        ResourceLocation gangId = ResourceLocation.tryParse(root.getString("Gang"));
        if (gangId == null) return empty();
        UUID challenge = root.hasUUID("Challenge") ? root.getUUID("Challenge") : null;
        return new Snapshot(gangId, root.getString("Role"), challenge, root.getBoolean("Ephemeral"),
                root.contains("ExpiresAt", Tag.TAG_LONG) ? root.getLong("ExpiresAt") : 0L, true);
    }

    /** Ensure ordinary Street Gear produces durable gang identity without adding transient challenge state. */
    public static Snapshot ensureForGear(LivingEntity entity) {
        Snapshot current = snapshot(entity);
        if (current.present()) return current;
        ResourceLocation entityId = ForgeRegistries.ENTITY_TYPES.getKey(entity.getType());
        ResourceLocation gangId = GangRegistry.gangIdForEntity(entityId);
        attach(entity, gangId, "member", null, false, 0L);
        return snapshot(entity);
    }

    public static void attach(LivingEntity entity, ResourceLocation gangId, String role, UUID challengeId,
                              boolean ephemeral, long expiresAt) {
        if (entity == null || entity.level().isClientSide || gangId == null || !MobStreetGear.hasGear(entity)) return;
        CompoundTag root = new CompoundTag();
        root.putInt("Schema", SCHEMA);
        root.putString("Gang", gangId.toString());
        root.putString("Role", role == null || role.isBlank() ? "member" : role);
        if (challengeId != null) root.putUUID("Challenge", challengeId);
        root.putBoolean("Ephemeral", ephemeral);
        if (expiresAt > 0L) root.putLong("ExpiresAt", expiresAt);
        entity.getPersistentData().put(ROOT_KEY, root);
    }

    public static void restore(LivingEntity entity, Snapshot snapshot) {
        if (snapshot == null || !snapshot.present()) return;
        attach(entity, snapshot.gangId(), snapshot.role(), snapshot.challengeId(), snapshot.ephemeral(), snapshot.expiresAt());
    }

    public static void clearChallenge(LivingEntity entity) {
        Snapshot current = snapshot(entity);
        if (!current.present()) return;
        if (current.ephemeral()) {
            clear(entity);
            return;
        }
        attach(entity, current.gangId(), current.role(), null, false, 0L);
    }

    public static void clear(LivingEntity entity) {
        if (entity != null && !entity.level().isClientSide) entity.getPersistentData().remove(ROOT_KEY);
    }

    public static boolean matchesChallenge(LivingEntity entity, UUID challengeId) {
        if (challengeId == null) return false;
        Snapshot snapshot = snapshot(entity);
        return snapshot.present() && challengeId.equals(snapshot.challengeId());
    }

    public static boolean expired(LivingEntity entity, long now) {
        Snapshot snapshot = snapshot(entity);
        return snapshot.present() && snapshot.ephemeral() && snapshot.expiresAt() > 0L && now >= snapshot.expiresAt();
    }

    public static Optional<ResourceLocation> gangId(LivingEntity entity) {
        Snapshot snapshot = snapshot(entity);
        return snapshot.present() ? Optional.of(snapshot.gangId()) : Optional.empty();
    }

    private static Snapshot empty() {
        return new Snapshot(null, "", null, false, 0L, false);
    }

    private GangMemberState() {}
}
