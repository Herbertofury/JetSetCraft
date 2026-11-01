package com.herberto.jetsetcraft.compat;

import com.herberto.jetsetcraft.JetSetCraft;
import com.herberto.jetsetcraft.entity.PaintBalloonEntity;
import com.herberto.jetsetcraft.rush.RivalRushManager;
import com.herberto.jetsetcraft.rush.RushWorld;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.registries.ForgeRegistries;

import java.lang.reflect.Method;

/**
 * Optional, narrow Iron's Firebolt relay. This launches the provider's actual projectile; it does not
 * cast a spell, copy a spellbook, or read/write another mod's mana, cooldowns, equipment or capabilities.
 * Verified API reference: iron431/irons-spells-n-spellbooks v1.20.1-3.4.0.2.
 */
public final class RushSpellCompat {
    public static final ResourceLocation FIREBOLT_ID = new ResourceLocation("irons_spellbooks", "firebolt");
    public static final float MAX_DAMAGE = 40.0F;
    public static final double MAX_RANGE = 64.0D;
    public static final int MAX_LIFETIME_TICKS = 100;
    public static final int MAX_ACTIVE_RELAYS = 8;
    private static final String RELAY = "jetsetcraft_rush_spell_relay";
    private static final String FIREBOLT_CLASS = "io.redspace.ironsspellbooks.entity.spells.firebolt.FireboltProjectile";
    private static volatile Api cachedApi;
    private static volatile boolean checkedApi;
    private static volatile boolean warned;

    private record Api(Class<?> type, Method setDamage, Method getDamage, Method shoot) {}

    /** Registry and public API availability only; no entity construction or registry scan per tick. */
    public static boolean fireboltAvailable() {
        return ModList.get().isLoaded("irons_spellbooks")
                && ForgeRegistries.ENTITY_TYPES.containsKey(FIREBOLT_ID) && api() != null;
    }

    /**
     * Admission/continuation allowlist for Rush projectiles. The manager remains responsible for
     * session ownership, room boundaries, tracking, and removing a projectile that fails this check.
     * Provider Firebolts are admitted only when created by this relay, never by arbitrary native gear.
     */
    public static boolean allowed(Projectile projectile) {
        if (projectile == null || projectile.isRemoved() || !(projectile.getOwner() instanceof ServerPlayer owner)
                || !RushWorld.isRush(projectile.level()) || owner.level() != projectile.level()
                || !RivalRushManager.active(owner) || RivalRushManager.restoring(owner)) return false;
        if (RivalRushManager.owned(projectile) && !RivalRushManager.belongsTo(projectile, owner)) return false;
        if (projectile instanceof PaintBalloonEntity) return true;
        if (!FIREBOLT_ID.equals(ForgeRegistries.ENTITY_TYPES.getKey(projectile.getType()))
                || !projectile.getPersistentData().getBoolean(RELAY)
                || projectile.tickCount > MAX_LIFETIME_TICKS
                || projectile.distanceToSqr(owner) > MAX_RANGE * MAX_RANGE
                || !finite(projectile.position()) || !finite(projectile.getDeltaMovement())
                || projectile.getDeltaMovement().lengthSqr() > 16.0D) return false;
        Api api = api();
        if (api == null || !api.type.isInstance(projectile)) return false;
        try {
            float damage = ((Number) api.getDamage.invoke(projectile)).floatValue();
            return Float.isFinite(damage) && damage > 0.0F && damage <= MAX_DAMAGE;
        } catch (ReflectiveOperationException | RuntimeException | LinkageError error) {
            warn(error);
            return false;
        }
    }

    /**
     * Aim and spawn one native Firebolt for a current run. All numbers and ownership are server-owned.
     * A failed provider API or rejected add returns false and leaves no untracked projectile behind.
     */
    public static boolean firebolt(ServerPlayer owner, Vec3 target, float damage) {
        if (owner == null || target == null || !finite(target) || !Float.isFinite(damage) || damage <= 0.0F
                || !RushWorld.isRush(owner.level()) || !RivalRushManager.active(owner)
                || RivalRushManager.restoring(owner) || !fireboltAvailable()) return false;
        var run = RivalRushManager.run(owner);
        if (run == null || run.isFinished() || run.hasOffer() || run.awaitingEncore()) return false;
        Vec3 aim = target.subtract(owner.getEyePosition());
        double distance = aim.lengthSqr();
        if (!Double.isFinite(distance) || distance < 0.0025D || distance > MAX_RANGE * MAX_RANGE) return false;
        long existing = owner.serverLevel().getEntitiesOfClass(Projectile.class,
                        owner.getBoundingBox().inflate(MAX_RANGE), p -> !p.isRemoved()
                                && p.getPersistentData().getBoolean(RELAY)
                                && RivalRushManager.belongsTo(p, owner)).size();
        if (existing >= MAX_ACTIVE_RELAYS) return false;
        Projectile projectile = null;
        try {
            EntityType<?> type = ForgeRegistries.ENTITY_TYPES.getValue(FIREBOLT_ID);
            if (type == null) return false;
            Entity created = type.create(owner.serverLevel());
            if (!(created instanceof Projectile shot)) {
                if (created != null) created.discard();
                return false;
            }
            projectile = shot;
            Api api = api();
            if (api == null || !api.type.isInstance(shot)) { shot.discard(); return false; }
            Vec3 direction = aim.normalize();
            Vec3 origin = owner.getEyePosition().add(direction.scale(0.35D));
            shot.setOwner(owner); // Provider attribution must exist before EntityJoinLevelEvent.
            shot.setPos(origin.x, origin.y, origin.z);
            api.setDamage.invoke(shot, Math.min(MAX_DAMAGE, damage));
            api.shoot.invoke(shot, direction);
            shot.getPersistentData().putBoolean(RELAY, true);
            if (!allowed(shot)) { shot.discard(); return false; }
            // The production join hook admits and records the shot; a missing hook fails closed.
            if (!owner.serverLevel().addFreshEntity(shot) || !RivalRushManager.belongsTo(shot, owner)) {
                shot.discard();
                return false;
            }
            return true;
        } catch (ReflectiveOperationException | RuntimeException | LinkageError error) {
            if (projectile != null) projectile.discard();
            warn(error);
            return false;
        }
    }

    private static Api api() {
        if (checkedApi) return cachedApi;
        if (!ModList.get().isLoaded("irons_spellbooks")) return null;
        synchronized (RushSpellCompat.class) {
            if (!checkedApi) {
                try {
                    Class<?> type = Class.forName(FIREBOLT_CLASS, false, RushSpellCompat.class.getClassLoader());
                    if (Projectile.class.isAssignableFrom(type)) {
                        cachedApi = new Api(type, type.getMethod("setDamage", float.class),
                                type.getMethod("getDamage"), type.getMethod("shoot", Vec3.class));
                    }
                } catch (ReflectiveOperationException | RuntimeException | LinkageError error) {
                    warn(error);
                }
                checkedApi = true;
            }
        }
        return cachedApi;
    }

    private static boolean finite(Vec3 value) {
        return Double.isFinite(value.x) && Double.isFinite(value.y) && Double.isFinite(value.z);
    }

    private static void warn(Throwable error) {
        if (!warned) {
            warned = true;
            JetSetCraft.LOGGER.warn("Rival Rush Firebolt relay unavailable: optional provider API did not match", error);
        }
    }

    private RushSpellCompat() {}
}
