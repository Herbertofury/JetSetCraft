package com.herberto.jetsetcraft.compat;

import com.herberto.jetsetcraft.liberty.LibertyManager;
import com.herberto.jetsetcraft.liberty.LibertyStratagemEngine;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;

/** Head-of-native-hit guards, before TaCZ's practice-target callback, ignition, knockback or block callbacks. */
public final class LibertyNativeBulletSafety {
    private static boolean owned(Projectile bullet) {
        Entity owner=bullet.getOwner();
        return LibertyManager.owned(bullet) || LibertyStratagemEngine.forActor(bullet) != null
                || owner instanceof Player player&&LibertyManager.active(player) || LibertyManager.owned(owner);
    }
    public static boolean rejectEntity(Projectile bullet, Entity target) {
        if (bullet.level().isClientSide || !owned(bullet)) return false;
        LibertyStratagemEngine engine = LibertyStratagemEngine.forActor(bullet);
        boolean allowed = engine == null ? LibertyManager.validOwned(bullet)
                && LibertyManager.allowedDamage(target, bullet.getOwner(), bullet) : engine.authorizedEnemy(target);
        if (allowed && LibertyHelldiversCompat.protectTaczTerrain(bullet)) return false;
        bullet.discard(); return true;
    }
    /**
     * An owned round may damage owned enemies through its real native explosion. It must never call
     * a world's projectile-reactive block (target, bell, custom machine), or place fire in that world.
     */
    public static boolean handleBlock(Projectile bullet, BlockHitResult hit) {
        if (bullet.level().isClientSide || hit.getType() == HitResult.Type.MISS || !owned(bullet)) return false;
        LibertyStratagemEngine engine = LibertyStratagemEngine.forActor(bullet);
        if ((engine != null || LibertyManager.validOwned(bullet)) && LibertyHelldiversCompat.protectTaczTerrain(bullet)) {
            LibertyHelldiversCompat.nativeSafeBlockImpact(bullet, hit.getLocation());
            if (bullet.level() instanceof ServerLevel level) level.sendParticles(ParticleTypes.CRIT,
                    hit.getLocation().x, hit.getLocation().y, hit.getLocation().z, 4, .05, .05, .05, .02);
        }
        bullet.discard(); return true;
    }
    private LibertyNativeBulletSafety() { }
}
