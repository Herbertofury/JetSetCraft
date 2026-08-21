package com.herberto.jetsetcraft.client.render;

import com.herberto.jetsetcraft.client.state.ClientRideState;
import com.herberto.jetsetcraft.movement.RideStyle;
import com.herberto.jetsetcraft.registry.ModItems;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.PlayerModel;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;

public final class RideGearLayer extends RenderLayer<AbstractClientPlayer, PlayerModel<AbstractClientPlayer>> {
    public RideGearLayer(RenderLayerParent<AbstractClientPlayer, PlayerModel<AbstractClientPlayer>> parent) {
        super(parent);
    }

    @Override
    public void render(PoseStack poseStack, MultiBufferSource buffer, int packedLight, AbstractClientPlayer player,
                       float limbSwing, float limbSwingAmount, float partialTick, float ageInTicks,
                       float netHeadYaw, float headPitch) {
        ClientRideState.Snapshot state = ClientRideState.get(player.getId());
        if (!state.active()) return;
        switch (state.style()) {
            case INLINE -> renderSkates(poseStack, buffer, packedLight, player, state, partialTick, ModItems.INLINE_SKATES.get().getDefaultInstance());
            case QUAD -> renderSkates(poseStack, buffer, packedLight, player, state, partialTick, ModItems.QUAD_SKATES.get().getDefaultInstance());
            case BOARD -> renderBoard(poseStack, buffer, packedLight, player, state, partialTick);
            case BMX -> renderBmx(poseStack, buffer, packedLight, player, state, partialTick);
            default -> { }
        }
    }

    private void renderSkates(PoseStack poseStack, MultiBufferSource buffer, int light,
                              AbstractClientPlayer player, ClientRideState.Snapshot state, float partialTick, ItemStack stack) {
        poseStack.pushPose();
        getParentModel().leftLeg.translateAndRotate(poseStack);
        poseStack.translate(0.0, 0.72, -0.04);
        poseStack.scale(0.72f, 0.72f, 0.72f);
        renderItem(stack, poseStack, buffer, light, player);
        poseStack.popPose();

        poseStack.pushPose();
        getParentModel().rightLeg.translateAndRotate(poseStack);
        poseStack.translate(0.0, 0.72, -0.04);
        poseStack.mulPose(Axis.YP.rotationDegrees(180.0f));
        poseStack.scale(0.72f, 0.72f, 0.72f);
        renderItem(stack, poseStack, buffer, light, player);
        poseStack.popPose();
    }

    private void renderBoard(PoseStack poseStack, MultiBufferSource buffer, int light,
                             AbstractClientPlayer player, ClientRideState.Snapshot state, float partialTick) {
        poseStack.pushPose();
        poseStack.translate(0.0, -0.035, 0.0);
        if (state.trickTicks() > 0) {
            float progress = (16.0f - state.trickTicks() + partialTick) / 16.0f;
            poseStack.mulPose(Axis.YP.rotationDegrees(progress * 360.0f));
            poseStack.mulPose(Axis.ZP.rotationDegrees((float) Math.sin(progress * Math.PI) * 80.0f));
        }
        poseStack.scale(0.82f, 0.82f, 0.82f);
        renderItem(ModItems.STREET_BOARD.get().getDefaultInstance(), poseStack, buffer, light, player);
        poseStack.popPose();
    }

    private void renderBmx(PoseStack poseStack, MultiBufferSource buffer, int light,
                           AbstractClientPlayer player, ClientRideState.Snapshot state, float partialTick) {
        poseStack.pushPose();
        poseStack.translate(0.0, -0.02, 0.12);
        if (state.trickTicks() > 0) {
            float progress = (16.0f - state.trickTicks() + partialTick) / 16.0f;
            poseStack.mulPose(Axis.ZP.rotationDegrees((float) Math.sin(progress * Math.PI) * 28.0f));
        }
        poseStack.scale(0.88f, 0.88f, 0.88f);
        renderItem(ModItems.BMX.get().getDefaultInstance(), poseStack, buffer, light, player);
        poseStack.popPose();
    }

    private static void renderItem(ItemStack stack, PoseStack poseStack, MultiBufferSource buffer,
                                   int light, AbstractClientPlayer player) {
        Minecraft.getInstance().getItemRenderer().renderStatic(stack, ItemDisplayContext.FIXED, light,
                OverlayTexture.NO_OVERLAY, poseStack, buffer, player.level(), player.getId());
    }
}
