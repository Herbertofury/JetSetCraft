package com.herberto.jetsetcraft.client.render;

import com.herberto.jetsetcraft.blockentity.BoomboxBlockEntity;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;

/** Physical Gang Target presentation: the inserted head/emblem is visibly mounted on the Boombox. */
public final class BoomboxRenderer implements BlockEntityRenderer<BoomboxBlockEntity> {
    public BoomboxRenderer(BlockEntityRendererProvider.Context context) {}

    @Override
    public void render(BoomboxBlockEntity boombox, float partialTick, PoseStack poseStack,
                       MultiBufferSource buffers, int packedLight, int packedOverlay) {
        ItemStack target = boombox.targetStack();
        if (target.isEmpty()) return;
        long gameTime = boombox.getLevel() == null ? 0L : boombox.getLevel().getGameTime();
        float spin = ((gameTime + partialTick) * (boombox.isChallengeActive() ? 2.4F : 0.45F)) % 360.0F;
        float bob = boombox.isChallengeActive()
                ? (float) Math.sin((gameTime + partialTick) * 0.22D) * 0.035F : 0.0F;

        poseStack.pushPose();
        poseStack.translate(0.5D, 1.04D + bob, 0.5D);
        poseStack.mulPose(Axis.YP.rotationDegrees(spin));
        poseStack.scale(0.58F, 0.58F, 0.58F);
        Minecraft.getInstance().getItemRenderer().renderStatic(target, ItemDisplayContext.FIXED,
                packedLight, packedOverlay, poseStack, buffers, boombox.getLevel(), 0);
        poseStack.popPose();
    }
}
