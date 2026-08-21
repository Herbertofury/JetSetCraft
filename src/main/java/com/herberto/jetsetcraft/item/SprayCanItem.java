package com.herberto.jetsetcraft.item;

import com.herberto.jetsetcraft.config.JetSetConfig;
import com.herberto.jetsetcraft.entity.GraffitiEntity;
import com.herberto.jetsetcraft.graffiti.GraffitiCatalog;
import com.herberto.jetsetcraft.registry.ModEntities;
import net.minecraft.ChatFormatting;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.phys.AABB;

public final class SprayCanItem extends Item {
    private static final String VARIANT_TAG = "JetSetCraftGraffitiVariant";

    public SprayCanItem(Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        Direction face = context.getClickedFace();
        if (!face.getAxis().isHorizontal()) return InteractionResult.FAIL;
        if (!JetSetConfig.SERVER.allowGraffiti.get()) return InteractionResult.FAIL;

        var player = context.getPlayer();
        var stack = context.getItemInHand();
        if (player != null && player.isShiftKeyDown()) {
            if (!context.getLevel().isClientSide) {
                int next = Math.floorMod(stack.getOrCreateTag().getInt(VARIANT_TAG) + 1, GraffitiCatalog.size());
                stack.getOrCreateTag().putInt(VARIANT_TAG, next);
                player.displayClientMessage(Component.translatable("message.jetsetcraft.graffiti_variant",
                                GraffitiCatalog.id(next))
                        .withStyle(ChatFormatting.LIGHT_PURPLE), true);
            }
            return InteractionResult.sidedSuccess(context.getLevel().isClientSide);
        }

        if (context.getLevel() instanceof ServerLevel serverLevel) {
            int variant = Math.floorMod(stack.getOrCreateTag().getInt(VARIANT_TAG), GraffitiCatalog.size());
            GraffitiEntity graffiti = new GraffitiEntity(ModEntities.GRAFFITI.get(), serverLevel);
            graffiti.configure(context.getClickedPos(), face, variant);

            // One tag per exact wall patch: repainting replaces the old decal instead of stacking z-fighting quads.
            var entry = GraffitiCatalog.get(variant);
            AABB patch = new AABB(graffiti.position(), graffiti.position())
                    .inflate(Math.max(0.14, entry.renderWidth() * 0.48), Math.max(0.55, entry.renderHeight() * 0.48),
                            Math.max(0.14, entry.renderWidth() * 0.48));
            serverLevel.getEntitiesOfClass(GraffitiEntity.class, patch, e -> e.getFace() == face)
                    .forEach(GraffitiEntity::discard);

            serverLevel.addFreshEntity(graffiti);
            if (player != null && !player.getAbilities().instabuild) {
                stack.hurtAndBreak(1, player, p -> p.broadcastBreakEvent(context.getHand()));
            }
        }
        return InteractionResult.sidedSuccess(context.getLevel().isClientSide);
    }
}
