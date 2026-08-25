package com.herberto.jetsetcraft.client.render;

import com.herberto.jetsetcraft.client.state.ClientRideState;
import com.herberto.jetsetcraft.JetSetCraft;
import com.herberto.jetsetcraft.config.JetSetConfig;
import com.herberto.jetsetcraft.registry.ModItems;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.PlayerModel;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.resources.ResourceLocation;

public final class RideGearLayer extends RenderLayer<AbstractClientPlayer, PlayerModel<AbstractClientPlayer>> {
    private static final ResourceLocation INLINE_TEXTURE = ResourceLocation.fromNamespaceAndPath(JetSetCraft.MOD_ID,
            "textures/entity/ride/street_art/inline_rollerblades.png");
    private static final ResourceLocation QUAD_TEXTURE = ResourceLocation.fromNamespaceAndPath(JetSetCraft.MOD_ID,
            "textures/entity/ride/street_art/quad_rollerblades.png");
    private final RollerbladeModel rollerblades = new RollerbladeModel();

    public RideGearLayer(RenderLayerParent<AbstractClientPlayer, PlayerModel<AbstractClientPlayer>> parent) {
        super(parent);
    }

    @Override
    public void render(PoseStack poseStack, MultiBufferSource buffer, int packedLight, AbstractClientPlayer player,
                       float limbSwing, float limbSwingAmount, float partialTick, float ageInTicks,
                       float netHeadYaw, float headPitch) {
        ClientRideState.Snapshot state = ClientRideState.get(player.getId());
        // Dancing and breakdance power moves deliberately return the rider to their feet. Equipment remains safely
        // stored in the dedicated loadout and reappears as soon as the action ends.
        if (!state.active() || state.dancing() || state.groundStunt()) return;
        switch (state.style()) {
            case INLINE -> renderSkates(poseStack, buffer, packedLight, INLINE_TEXTURE);
            case QUAD -> renderSkates(poseStack, buffer, packedLight, QUAD_TEXTURE);
            case BOARD -> renderBoard(poseStack, buffer, packedLight, player, state, partialTick,
                    ModItems.STREET_BOARD.get().getDefaultInstance(), false);
            case HOVER -> renderBoard(poseStack, buffer, packedLight, player, state, partialTick,
                    ModItems.HOVERBOARD.get().getDefaultInstance(), true);
            case BMX -> renderBmx(poseStack, buffer, packedLight, player, state, partialTick);
            case SCOOTER -> renderScooter(poseStack, buffer, packedLight, player, state, partialTick);
            default -> { }
        }
    }

    private void renderSkates(PoseStack poseStack, MultiBufferSource buffer, int light,
                              ResourceLocation texture) {
        rollerblades.copyPose(getParentModel());
        rollerblades.render(poseStack, buffer.getBuffer(RenderType.entityCutoutNoCull(texture)), light);
    }

    private void renderBoard(PoseStack poseStack, MultiBufferSource buffer, int light,
                             AbstractClientPlayer player, ClientRideState.Snapshot state, float partialTick,
                             ItemStack stack, boolean hover) {
        poseStack.pushPose();
        boolean reduced = JetSetConfig.CLIENT.reducedMotion.get();
        double bob = hover && !reduced ? Math.sin((player.tickCount + partialTick) * 0.24) * 0.012 : 0.0;
        poseStack.translate(0.0, (hover ? 0.59 : 0.64) + bob, 0.0);
        poseStack.mulPose(Axis.XP.rotationDegrees(180.0f));
        if (state.trickTicks() > 0 && !reduced) {
            float progress = (22.0f - state.trickTicks() + partialTick) / 22.0f;
            poseStack.mulPose(Axis.YP.rotationDegrees(progress * 360.0f));
            poseStack.mulPose(Axis.ZP.rotationDegrees((float) Math.sin(progress * Math.PI) * (hover ? 62.0f : 80.0f)));
        } else if (hover && !reduced) {
            poseStack.mulPose(Axis.ZP.rotationDegrees((float) Math.sin((player.tickCount + partialTick) * 0.12) * 2.0f));
        }
        float scale = hover ? 0.88f : 0.82f;
        poseStack.scale(scale, scale, scale);
        renderItem(stack, poseStack, buffer, light, player);
        poseStack.popPose();
    }

    private void renderBmx(PoseStack poseStack, MultiBufferSource buffer, int light,
                           AbstractClientPlayer player, ClientRideState.Snapshot state, float partialTick) {
        poseStack.pushPose();
        poseStack.translate(0.0, 0.65, 0.12);
        poseStack.mulPose(Axis.XP.rotationDegrees(180.0f));
        if (state.trickTicks() > 0 && !JetSetConfig.CLIENT.reducedMotion.get()) {
            float progress = (22.0f - state.trickTicks() + partialTick) / 22.0f;
            poseStack.mulPose(Axis.ZP.rotationDegrees((float) Math.sin(progress * Math.PI) * 28.0f));
        }
        poseStack.scale(0.88f, 0.88f, 0.88f);
        renderItem(ModItems.BMX.get().getDefaultInstance(), poseStack, buffer, light, player);
        poseStack.popPose();
    }

    private void renderScooter(PoseStack poseStack, MultiBufferSource buffer, int light,
                               AbstractClientPlayer player, ClientRideState.Snapshot state, float partialTick) {
        poseStack.pushPose();
        poseStack.translate(0.0, 0.65, 0.12);
        poseStack.mulPose(Axis.XP.rotationDegrees(180.0f));
        if (state.trickTicks() > 0 && !JetSetConfig.CLIENT.reducedMotion.get()) {
            float progress = (22.0f - state.trickTicks() + partialTick) / 22.0f;
            poseStack.mulPose(Axis.YP.rotationDegrees(progress * 360.0f));
            poseStack.mulPose(Axis.ZP.rotationDegrees((float) Math.sin(progress * Math.PI) * 24.0f));
        }
        poseStack.scale(0.86f, 0.86f, 0.86f);
        renderItem(ModItems.SCOOTER.get().getDefaultInstance(), poseStack, buffer, light, player);
        poseStack.popPose();
    }

    private static void renderItem(ItemStack stack, PoseStack poseStack, MultiBufferSource buffer,
                                   int light, AbstractClientPlayer player) {
        // Render the authored mesh in model space. FIXED applies item-frame transforms a second time here,
        // which offsets and enlarges ride gear instead of keeping it attached to the rider.
        Minecraft.getInstance().getItemRenderer().renderStatic(stack, ItemDisplayContext.NONE, light,
                OverlayTexture.NO_OVERLAY, poseStack, buffer, player.level(), player.getId());
    }
}
