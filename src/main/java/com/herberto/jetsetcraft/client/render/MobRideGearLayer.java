package com.herberto.jetsetcraft.client.render;

import com.herberto.jetsetcraft.client.state.ClientMobGearState;
import com.herberto.jetsetcraft.config.JetSetConfig;
import com.herberto.jetsetcraft.item.RideGearItem;
import com.herberto.jetsetcraft.mob.MobRideRig;
import com.herberto.jetsetcraft.mob.MobRideRigResolver;
import com.herberto.jetsetcraft.movement.RideStyle;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;

/**
 * Renderer-agnostic ground-contact rig. It never edits a third-party model or assumes humanoid bones;
 * the actual physical item follows conservative contact points derived from the mob's body footprint.
 */
public final class MobRideGearLayer<T extends LivingEntity, M extends EntityModel<T>> extends RenderLayer<T, M> {
    public MobRideGearLayer(RenderLayerParent<T, M> parent) {
        super(parent);
    }

    @Override
    public void render(PoseStack poseStack, MultiBufferSource buffer, int packedLight, T entity,
                       float limbSwing, float limbSwingAmount, float partialTick, float ageInTicks,
                       float netHeadYaw, float headPitch) {
        if (entity instanceof Player || entity.isInvisible()) return;
        ClientMobGearState.Snapshot snapshot = ClientMobGearState.get(entity.getId());
        ItemStack stack = snapshot.stack();
        if (!snapshot.equipped() || !(stack.getItem() instanceof RideGearItem gear)) return;

        MobRideRig rig = snapshot.rig() == null ? MobRideRig.GENERIC : snapshot.rig();
        float ageScale = MobRideRigResolver.ageScale(entity);
        float width = Mth.clamp(entity.getBbWidth(), 0.35f, 3.5f);
        double horizontal = entity.getDeltaMovement().horizontalDistance();
        float motion = Double.isFinite(horizontal) ? (float) Math.min(1.0D, horizontal * 2.8D) : 0.0F;
        boolean reduced = JetSetConfig.CLIENT.reducedMotion.get();
        float bob = reduced ? 0.0f : Mth.sin(ageInTicks * 0.22f) * 0.012f * motion;
        double vertical = entity.getDeltaMovement().y;
        float lean = reduced || !Double.isFinite(vertical) ? 0.0f
                : Mth.clamp((float) vertical * 7.0f, -6.0f, 6.0f);

        RideStyle style = gear.style();
        if (style == RideStyle.INLINE || style == RideStyle.QUAD) {
            renderContactGear(poseStack, buffer, packedLight, entity, stack, rig, width, ageScale, bob, motion);
        } else {
            renderPlatformGear(poseStack, buffer, packedLight, entity, stack, rig, style,
                    width, ageScale, bob, lean);
        }
    }

    private static void renderContactGear(PoseStack poseStack, MultiBufferSource buffer, int light,
                                          LivingEntity entity, ItemStack stack, MobRideRig rig, float width,
                                          float ageScale, float bob, float motion) {
        float lateral = width * 0.29f;
        float longitudinal = width * 0.31f;
        float rigScale = 0.78f + rig.footprintScale() * 0.32f;
        float scale = Mth.clamp(width * 0.58f * ageScale * rigScale, 0.26f, 1.10f);
        switch (rig) {
            case BIPED, GENERIC -> {
                renderAt(poseStack, buffer, light, entity, stack, -lateral, bob, 0.02f, scale, motion);
                renderAt(poseStack, buffer, light, entity, stack, lateral, bob, 0.02f, scale, motion);
            }
            case QUADRUPED -> {
                renderAt(poseStack, buffer, light, entity, stack, -lateral, bob, -longitudinal, scale, motion);
                renderAt(poseStack, buffer, light, entity, stack, lateral, bob, -longitudinal, scale, motion);
                renderAt(poseStack, buffer, light, entity, stack, -lateral, bob, longitudinal, scale, motion);
                renderAt(poseStack, buffer, light, entity, stack, lateral, bob, longitudinal, scale, motion);
            }
            case MULTI_LEG -> {
                for (int row = -1; row <= 1; row++) {
                    renderAt(poseStack, buffer, light, entity, stack, -lateral, bob, row * longitudinal,
                            scale * 0.78f, motion);
                    renderAt(poseStack, buffer, light, entity, stack, lateral, bob, row * longitudinal,
                            scale * 0.78f, motion);
                }
            }
            case BODY_CONTACT, AERIAL, AQUATIC -> {
                // No fake humanoid feet: form a stable paired under-body carriage for unusual anatomy.
                float carriage = lateral * 0.48f;
                float carriageScale = Mth.clamp(scale * 0.92f, 0.28f, 1.18f);
                renderAt(poseStack, buffer, light, entity, stack, -carriage, bob, 0.0f,
                        carriageScale, motion);
                renderAt(poseStack, buffer, light, entity, stack, carriage, bob, 0.0f,
                        carriageScale, motion);
            }
        }
    }

    private static void renderPlatformGear(PoseStack poseStack, MultiBufferSource buffer, int light,
                                           LivingEntity entity, ItemStack stack, MobRideRig rig, RideStyle style,
                                           float width, float ageScale, float bob, float lean) {
        poseStack.pushPose();
        float baseScale = switch (style) {
            case BMX -> 0.68f;
            case SCOOTER -> 0.66f;
            case HOVER -> 0.78f;
            default -> 0.73f;
        };
        float anatomyScale = 0.90f + rig.footprintScale() * 0.18f;
        float scale = Mth.clamp(width * baseScale * anatomyScale * ageScale, 0.32f, 1.75f);
        float hoverLift = style == RideStyle.HOVER ? 0.04f : 0.0f;
        poseStack.translate(0.0D, entity.getBbHeight() * 0.36D - 0.035D + hoverLift + bob, 0.0D);
        poseStack.mulPose(Axis.XP.rotationDegrees(180.0f));
        poseStack.mulPose(Axis.ZP.rotationDegrees(lean));
        poseStack.scale(scale, scale, scale);
        renderItem(stack, poseStack, buffer, light, entity);
        poseStack.popPose();
    }

    private static void renderAt(PoseStack poseStack, MultiBufferSource buffer, int light, LivingEntity entity,
                                 ItemStack stack, float x, float y, float z, float scale, float motion) {
        poseStack.pushPose();
        poseStack.translate(x, entity.getBbHeight() * 0.36D - 0.015f + y, z);
        poseStack.mulPose(Axis.XP.rotationDegrees(180.0f));
        // Both sides point in the travel direction. Rotating one skate 180 degrees made half the rig face backward.
        if (!JetSetConfig.CLIENT.reducedMotion.get()) {
            poseStack.mulPose(Axis.XP.rotationDegrees(motion * Mth.sin((entity.tickCount + x * 17.0f + z * 13.0f)
                    * 0.55f) * 5.0f));
        }
        poseStack.scale(scale, scale, scale);
        renderItem(stack, poseStack, buffer, light, entity);
        poseStack.popPose();
    }

    private static void renderItem(ItemStack stack, PoseStack poseStack, MultiBufferSource buffer,
                                   int light, LivingEntity entity) {
        Minecraft.getInstance().getItemRenderer().renderStatic(stack, ItemDisplayContext.NONE, light,
                OverlayTexture.NO_OVERLAY, poseStack, buffer, entity.level(), entity.getId());
    }
}
