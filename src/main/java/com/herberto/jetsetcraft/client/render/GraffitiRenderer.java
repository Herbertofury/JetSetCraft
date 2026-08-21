package com.herberto.jetsetcraft.client.render;

import com.herberto.jetsetcraft.entity.GraffitiEntity;
import com.herberto.jetsetcraft.graffiti.GraffitiCatalog;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import org.joml.Matrix3f;
import org.joml.Matrix4f;

public final class GraffitiRenderer extends EntityRenderer<GraffitiEntity> {
    public GraffitiRenderer(EntityRendererProvider.Context context) {
        super(context);
        this.shadowRadius = 0.0f;
    }

    @Override
    public void render(GraffitiEntity entity, float entityYaw, float partialTick, PoseStack poseStack,
                       MultiBufferSource buffer, int packedLight) {
        poseStack.pushPose();
        orientToFace(poseStack, entity.getFace());
        poseStack.translate(0.0, 0.0, 0.004);
        VertexConsumer consumer = buffer.getBuffer(RenderType.entityCutoutNoCull(getTextureLocation(entity)));
        PoseStack.Pose pose = poseStack.last();
        Matrix4f matrix = pose.pose();
        Matrix3f normal = pose.normal();

        GraffitiCatalog.Entry entry = GraffitiCatalog.get(entity.getVariant());
        float halfWidth = entry.renderWidth() * 0.5f;
        float halfHeight = entry.renderHeight() * 0.5f;
        vertex(consumer, matrix, normal, -halfWidth, -halfHeight, 0, 0, 1, packedLight);
        vertex(consumer, matrix, normal, halfWidth, -halfHeight, 0, 1, 1, packedLight);
        vertex(consumer, matrix, normal, halfWidth, halfHeight, 0, 1, 0, packedLight);
        vertex(consumer, matrix, normal, -halfWidth, halfHeight, 0, 0, 0, packedLight);
        poseStack.popPose();
        super.render(entity, entityYaw, partialTick, poseStack, buffer, packedLight);
    }

    private static void orientToFace(PoseStack poseStack, Direction face) {
        switch (face) {
            case NORTH -> poseStack.mulPose(Axis.YP.rotationDegrees(180.0f));
            case EAST -> poseStack.mulPose(Axis.YP.rotationDegrees(-90.0f));
            case WEST -> poseStack.mulPose(Axis.YP.rotationDegrees(90.0f));
            case SOUTH -> { }
            default -> { }
        }
    }

    private static void vertex(VertexConsumer consumer, Matrix4f matrix, Matrix3f normal,
                               float x, float y, float z, float u, float v, int light) {
        consumer.vertex(matrix, x, y, z)
                .color(255, 255, 255, 255)
                .uv(u, v)
                .overlayCoords(OverlayTexture.NO_OVERLAY)
                .uv2(light)
                .normal(normal, 0, 0, 1)
                .endVertex();
    }

    @Override
    public ResourceLocation getTextureLocation(GraffitiEntity entity) {
        return GraffitiCatalog.texture(entity.getVariant());
    }
}
