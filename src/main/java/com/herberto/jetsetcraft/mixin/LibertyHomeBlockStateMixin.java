package com.herberto.jetsetcraft.mixin;

import com.herberto.jetsetcraft.liberty.LibertyHomeBlockGuard;
import com.herberto.jetsetcraft.liberty.LibertyMobSafety;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockBehaviour;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/** Block contact can mutate plates, dripleaves and portals before a Forge game-event hook exists. */
@Mixin(value = BlockBehaviour.BlockStateBase.class, remap = false)
public abstract class LibertyHomeBlockStateMixin implements LibertyHomeBlockGuard {
    @Inject(method = {"entityInside(Lnet/minecraft/world/level/Level;Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/entity/Entity;)V", "m_60682_(Lnet/minecraft/world/level/Level;Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/entity/Entity;)V"},
            at = @At("HEAD"), cancellable = true, require = 1)
    private void jetsetcraft$homeInside(Level level, BlockPos position, Entity actor, CallbackInfo callback) {
        if (LibertyMobSafety.protectedHomeActor(actor)) callback.cancel();
    }
}
