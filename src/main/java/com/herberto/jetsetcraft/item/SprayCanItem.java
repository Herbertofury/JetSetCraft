package com.herberto.jetsetcraft.item;

import com.herberto.jetsetcraft.config.JetSetConfig;
import com.herberto.jetsetcraft.client.ClientPacketHandlers;
import com.herberto.jetsetcraft.entity.GraffitiEntity;
import com.herberto.jetsetcraft.graffiti.CustomGraffiti;
import com.herberto.jetsetcraft.graffiti.GraffitiCatalog;
import com.herberto.jetsetcraft.graffiti.PaintColor;
import com.herberto.jetsetcraft.graffiti.PaintSplash;
import com.herberto.jetsetcraft.registry.ModEntities;
import net.minecraft.ChatFormatting;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;

public final class SprayCanItem extends Item {
    public static final String VARIANT_TAG = "JetSetCraftGraffitiVariant";
    public static final String CUSTOM_TAG = "JetSetCraftGraffitiCustom";
    public static final String FREE_PAINT_TAG = "JetSetCraftFreePaint";
    public static final String PAINT_COLOR_TAG = "JetSetCraftPaintColor";

    public SprayCanItem(Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        if (level.isClientSide) openSelector(hand);
        return InteractionResultHolder.sidedSuccess(player.getItemInHand(hand), level.isClientSide);
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        Direction face = context.getClickedFace();
        if (!JetSetConfig.SERVER.allowGraffiti.get()) return InteractionResult.FAIL;

        var player = context.getPlayer();
        var stack = context.getItemInHand();
        if (player != null && player.isShiftKeyDown()) {
            if (context.getLevel().isClientSide) openSelector(context.getHand());
            return InteractionResult.sidedSuccess(context.getLevel().isClientSide);
        }

        if (isFreePaint(stack)) {
            if (context.getLevel() instanceof ServerLevel serverLevel && player != null) {
                BlockHitResult hit = new BlockHitResult(context.getClickLocation(), face,
                        context.getClickedPos(), false);
                int painted = PaintSplash.freeSpray(serverLevel, player, hit, getPaintColor(stack), stack);
                if (painted > 0 && !player.getAbilities().instabuild) {
                    stack.hurtAndBreak(1, player, p -> p.broadcastBreakEvent(context.getHand()));
                }
            }
            return InteractionResult.sidedSuccess(context.getLevel().isClientSide);
        }

        if (!face.getAxis().isHorizontal()) return InteractionResult.FAIL;

        if (context.getLevel() instanceof ServerLevel serverLevel) {
            int variant = getCatalogSelection(stack);
            String custom = getCustomSelection(stack);
            GraffitiEntity graffiti = new GraffitiEntity(ModEntities.GRAFFITI.get(), serverLevel);
            graffiti.configure(context.getClickedPos(), face, variant, custom);

            // One tag per exact wall patch: repainting replaces the old decal instead of stacking z-fighting quads.
            float renderWidth = custom.isEmpty() ? GraffitiCatalog.get(variant).renderWidth() : 1.6f;
            float renderHeight = custom.isEmpty() ? GraffitiCatalog.get(variant).renderHeight() : 1.0f;
            AABB patch = new AABB(graffiti.position(), graffiti.position())
                    .inflate(Math.max(0.14, renderWidth * 0.48), Math.max(0.55, renderHeight * 0.48),
                            Math.max(0.14, renderWidth * 0.48));
            var replaced = serverLevel.getEntitiesOfClass(GraffitiEntity.class, patch, e -> e.getFace() == face);

            ChunkPos chunk = new ChunkPos(graffiti.blockPosition());
            AABB chunkBounds = new AABB(chunk.getMinBlockX(), serverLevel.getMinBuildHeight(), chunk.getMinBlockZ(),
                    chunk.getMaxBlockX() + 1, serverLevel.getMaxBuildHeight(), chunk.getMaxBlockZ() + 1);
            int existingOutsidePatch = serverLevel.getEntitiesOfClass(GraffitiEntity.class, chunkBounds).size()
                    - replaced.size();
            if (existingOutsidePatch >= JetSetConfig.SERVER.maxGraffitiPerChunk.get()) {
                if (player != null) {
                    player.displayClientMessage(Component.translatable("message.jetsetcraft.graffiti_chunk_full")
                            .withStyle(ChatFormatting.RED), true);
                }
                return InteractionResult.CONSUME;
            }

            if (!serverLevel.addFreshEntity(graffiti)) return InteractionResult.FAIL;
            replaced.forEach(GraffitiEntity::discard);
            if (player != null && !player.getAbilities().instabuild) {
                stack.hurtAndBreak(1, player, p -> p.broadcastBreakEvent(context.getHand()));
            }
        }
        return InteractionResult.sidedSuccess(context.getLevel().isClientSide);
    }

    public static int getCatalogSelection(ItemStack stack) {
        return Math.floorMod(stack.getOrCreateTag().getInt(VARIANT_TAG), GraffitiCatalog.size());
    }

    public static String getCustomSelection(ItemStack stack) {
        return CustomGraffiti.normalize(stack.getOrCreateTag().getString(CUSTOM_TAG));
    }

    public static boolean hasCustomSelection(ItemStack stack) { return !getCustomSelection(stack).isEmpty(); }

    public static boolean isFreePaint(ItemStack stack) {
        return stack.getOrCreateTag().getBoolean(FREE_PAINT_TAG);
    }

    public static void setFreePaint(ItemStack stack, boolean enabled) {
        stack.getOrCreateTag().putBoolean(FREE_PAINT_TAG, enabled);
    }

    public static PaintColor getPaintColor(ItemStack stack) {
        return PaintColor.byId(stack.getOrCreateTag().getInt(PAINT_COLOR_TAG));
    }

    public static void setPaintColor(ItemStack stack, PaintColor color) {
        stack.getOrCreateTag().putInt(PAINT_COLOR_TAG, color == null ? PaintColor.WHITE.id() : color.id());
    }

    public static void setCatalogSelection(ItemStack stack, int variant) {
        stack.getOrCreateTag().putInt(VARIANT_TAG, Math.floorMod(variant, GraffitiCatalog.size()));
        stack.getOrCreateTag().remove(CUSTOM_TAG);
    }

    public static void setCustomSelection(ItemStack stack, String custom) {
        String normalized = CustomGraffiti.normalize(custom);
        if (normalized.isEmpty()) stack.getOrCreateTag().remove(CUSTOM_TAG);
        else stack.getOrCreateTag().putString(CUSTOM_TAG, normalized);
    }

    @SuppressWarnings("deprecation")
    private static void openSelector(InteractionHand hand) {
        DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () -> ClientPacketHandlers.openGraffitiSelector(hand));
    }
}
