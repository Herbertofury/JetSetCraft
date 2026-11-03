package com.herberto.jetsetcraft.mixin;

import com.herberto.jetsetcraft.liberty.LibertyHomeEntityGuard;
import com.herberto.jetsetcraft.liberty.LibertyMobSafety;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Group;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Minecraft 1.20.1 contact boundary for explicitly accepted home defenses only. Named and SRG
 * selectors are paired because the ForgeGradle source runtime and installed Forge use different
 * method names. Each redirect group must bind exactly one native call; marker admission fails closed.
 */
@Mixin(value = Entity.class, remap = false)
public abstract class LibertyHomeEntityMixin implements LibertyHomeEntityGuard {
    @Inject(method = {"isIgnoringBlockTriggers()Z", "m_6090_()Z"}, at = @At("HEAD"), cancellable = true, require = 1)
    private void jetsetcraft$ignoreHomeTriggers(CallbackInfoReturnable<Boolean> result) {
        if (LibertyMobSafety.protectedHomeActor((Entity) (Object) this)) result.setReturnValue(true);
    }

    @Group(name = "jetsetcraft_home_step", min = 1, max = 1)
    @Redirect(method = {"move(Lnet/minecraft/world/entity/MoverType;Lnet/minecraft/world/phys/Vec3;)V", "m_6478_(Lnet/minecraft/world/entity/MoverType;Lnet/minecraft/world/phys/Vec3;)V"},
            at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/block/Block;stepOn(Lnet/minecraft/world/level/Level;Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/level/block/state/BlockState;Lnet/minecraft/world/entity/Entity;)V"), require = 0)
    private void jetsetcraft$homeStepNamed(Block block, Level level, BlockPos position, BlockState state, Entity actor) {
        if (!LibertyMobSafety.protectedHomeActor(actor)) block.stepOn(level, position, state, actor);
    }

    @Group(name = "jetsetcraft_home_step", min = 1, max = 1)
    @Redirect(method = {"move(Lnet/minecraft/world/entity/MoverType;Lnet/minecraft/world/phys/Vec3;)V", "m_6478_(Lnet/minecraft/world/entity/MoverType;Lnet/minecraft/world/phys/Vec3;)V"},
            at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/block/Block;m_141947_(Lnet/minecraft/world/level/Level;Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/level/block/state/BlockState;Lnet/minecraft/world/entity/Entity;)V"), require = 0)
    private void jetsetcraft$homeStepSrg(Block block, Level level, BlockPos position, BlockState state, Entity actor) {
        if (!LibertyMobSafety.protectedHomeActor(actor)) block.stepOn(level, position, state, actor);
    }

    @Group(name = "jetsetcraft_home_fall", min = 1, max = 1)
    @Redirect(method = {"checkFallDamage(DZLnet/minecraft/world/level/block/state/BlockState;Lnet/minecraft/core/BlockPos;)V", "m_7840_(DZLnet/minecraft/world/level/block/state/BlockState;Lnet/minecraft/core/BlockPos;)V"},
            at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/block/Block;fallOn(Lnet/minecraft/world/level/Level;Lnet/minecraft/world/level/block/state/BlockState;Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/entity/Entity;F)V"), require = 0)
    private void jetsetcraft$homeFallNamed(Block block, Level level, BlockState state, BlockPos position, Entity actor, float distance) {
        if (LibertyMobSafety.protectedHomeActor(actor)) LibertyMobSafety.fallWithoutBlockCallbacks(block, level, actor, distance);
        else block.fallOn(level, state, position, actor, distance);
    }

    @Group(name = "jetsetcraft_home_fall", min = 1, max = 1)
    @Redirect(method = {"checkFallDamage(DZLnet/minecraft/world/level/block/state/BlockState;Lnet/minecraft/core/BlockPos;)V", "m_7840_(DZLnet/minecraft/world/level/block/state/BlockState;Lnet/minecraft/core/BlockPos;)V"},
            at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/block/Block;m_142072_(Lnet/minecraft/world/level/Level;Lnet/minecraft/world/level/block/state/BlockState;Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/entity/Entity;F)V"), require = 0)
    private void jetsetcraft$homeFallSrg(Block block, Level level, BlockState state, BlockPos position, Entity actor, float distance) {
        if (LibertyMobSafety.protectedHomeActor(actor)) LibertyMobSafety.fallWithoutBlockCallbacks(block, level, actor, distance);
        else block.fallOn(level, state, position, actor, distance);
    }
}
