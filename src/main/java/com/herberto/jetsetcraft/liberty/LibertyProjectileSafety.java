package com.herberto.jetsetcraft.liberty;

import net.minecraft.core.BlockPos;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.animal.axolotl.Axolotl;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.entity.projectile.ThrownPotion;
import net.minecraft.world.item.alchemy.PotionUtils;
import net.minecraft.world.item.alchemy.Potions;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

/** Keeps native mission projectile effects inside the exact live operation that owns the shot. */
public final class LibertyProjectileSafety {
    /** True means the Forge impact must be cancelled before native block or entity callbacks. */
    public static boolean intercept(Projectile projectile, HitResult hit) {
        if (projectile.level().isClientSide || !LibertyManager.owned(projectile)
                || hit.getType() == HitResult.Type.MISS) return false;
        if (!LibertyManager.validOwned(projectile)) { projectile.discard(); return true; }
        if (projectile instanceof ThrownPotion potion) {
            splash(potion, hit);
            projectile.discard();
            return true;
        }
        if (hit.getType() == HitResult.Type.BLOCK || hit instanceof EntityHitResult entityHit
                && !LibertyManager.allowedDamage(entityHit.getEntity(), projectile.getOwner(), projectile)) {
            projectile.discard();
            return true;
        }
        return false;
    }

    private static void splash(ThrownPotion potion, HitResult hit) {
        Vec3 center = hit.getLocation();
        Entity owner = potion.getOwner();
        var stack = potion.getItem();
        var effects = PotionUtils.getMobEffects(stack);
        boolean water = PotionUtils.getPotion(stack) == Potions.WATER && effects.isEmpty();
        Entity directHit = hit instanceof EntityHitResult entityHit ? entityHit.getEntity() : null;
        // Match vanilla's four-block falloff and two-block vertical reach, but never create an
        // unfiltered lingering cloud or invoke a block's water/projectile callback. Mission potion
        // payloads remain finite even if another mod supplies unusually long/custom effect data.
        for (LivingEntity target : potion.level().getEntitiesOfClass(LivingEntity.class,
                new AABB(center, center).inflate(4, 2, 4), entity -> entity.isAlive()
                        && LibertyManager.allowedDamage(entity, owner, potion))) {
            double distance = target.position().distanceToSqr(center);
            if (distance >= 16) continue;
            if (water) {
                if (target.isSensitiveToWater()) target.hurt(potion.damageSources().indirectMagic(potion, owner), 1);
                if (target.isOnFire()) target.extinguishFire();
                if (target instanceof Axolotl axolotl) axolotl.rehydrate();
                continue;
            }
            if (!target.isAffectedByPotions()) continue;
            double intensity = target == directHit ? 1 : 1 - Math.sqrt(distance) / 4;
            for (int i = 0; i < Math.min(16, effects.size()); i++) {
                MobEffectInstance value = effects.get(i);
                var effect = value.getEffect();
                int amplifier = Math.max(0, Math.min(4, value.getAmplifier()));
                if (effect.isInstantenous()) {
                    effect.applyInstantenousEffect(potion, owner, target, amplifier, intensity);
                } else {
                    int duration = value.getDuration() < 0 ? 3_600 : Math.min(3_600, value.getDuration());
                    duration = (int) (duration * intensity + .5);
                    if (duration > 20) target.addEffect(new MobEffectInstance(effect, duration, amplifier,
                            value.isAmbient(), value.isVisible(), value.showIcon()), owner == null ? potion : owner);
                }
            }
        }
        potion.level().levelEvent(PotionUtils.getPotion(stack).hasInstantEffects() ? 2007 : 2002,
                BlockPos.containing(center), PotionUtils.getColor(stack));
    }

    private LibertyProjectileSafety() { }
}
