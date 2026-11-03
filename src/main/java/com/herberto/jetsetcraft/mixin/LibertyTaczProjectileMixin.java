package com.herberto.jetsetcraft.mixin;

import com.herberto.jetsetcraft.compat.LibertyNativeBulletGuard;
import com.herberto.jetsetcraft.compat.LibertyNativeBulletSafety;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Coerce;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/** Optional target: no TaCZ class is linked when the provider is absent. Admission checks its exact API. */
@Pseudo
@Mixin(targets = "com.tacz.guns.entity.EntityKineticBullet", remap = false)
public abstract class LibertyTaczProjectileMixin implements LibertyNativeBulletGuard {
    @Inject(method = "onHitEntity", at = @At("HEAD"), cancellable = true, require = 0, remap = false)
    private void jetsetcraft$beforeNativeEntityHit(@Coerce EntityHitResult hit, Vec3 start, Vec3 end, CallbackInfo callback) {
        if (LibertyNativeBulletSafety.rejectEntity((Projectile) (Object) this, hit.getEntity())) callback.cancel();
    }
    @Inject(method = "onHitBlock", at = @At("HEAD"), cancellable = true, require = 0, remap = false)
    private void jetsetcraft$beforeNativeBlockHit(BlockHitResult hit, Vec3 start, Vec3 end, CallbackInfo callback) {
        if (LibertyNativeBulletSafety.handleBlock((Projectile) (Object) this, hit)) callback.cancel();
    }
}
