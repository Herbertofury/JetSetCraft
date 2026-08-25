package com.herberto.jetsetcraft.client.render;

import com.herberto.jetsetcraft.entity.GraffitiEntity;
import com.herberto.jetsetcraft.graffiti.GraffitiCatalog;
import com.herberto.jetsetcraft.graffiti.CustomGraffiti;
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
    private static final ResourceLocation CUSTOM_PIXEL = ResourceLocation.withDefaultNamespace("textures/block/white_concrete.png");
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
        String custom = entity.getCustomPattern();
        VertexConsumer consumer = buffer.getBuffer(RenderType.entityCutoutNoCull(custom.isEmpty()
                ? getTextureLocation(entity) : CUSTOM_PIXEL));
        PoseStack.Pose pose = poseStack.last();
        Matrix4f matrix = pose.pose();
        Matrix3f normal = pose.normal();

        if (custom.isEmpty()) {
            GraffitiCatalog.Entry entry = GraffitiCatalog.get(entity.getVariant());
            float halfWidth = entry.renderWidth() * 0.5f;
            float halfHeight = entry.renderHeight() * 0.5f;
            vertex(consumer, matrix, normal, -halfWidth, -halfHeight, 0, 0, 1, packedLight, 255, 255, 255);
            vertex(consumer, matrix, normal, halfWidth, -halfHeight, 0, 1, 1, packedLight, 255, 255, 255);
            vertex(consumer, matrix, normal, halfWidth, halfHeight, 0, 1, 0, packedLight, 255, 255, 255);
            vertex(consumer, matrix, normal, -halfWidth, halfHeight, 0, 0, 0, packedLight, 255, 255, 255);
        } else {
            float width = entity.getRenderWidth() > 0.0F ? entity.getRenderWidth() : 1.6F;
            float height = entity.getRenderHeight() > 0.0F ? entity.getRenderHeight() : 1.0F;
            renderCustom(consumer, matrix, normal, custom, packedLight, width, height);
        }
        poseStack.popPose();
        super.render(entity, entityYaw, partialTick, poseStack, buffer, packedLight);
    }

    private static void orientToFace(PoseStack poseStack, Direction face) {
        switch (face) {
            case NORTH -> poseStack.mulPose(Axis.YP.rotationDegrees(180.0f));
            case EAST -> poseStack.mulPose(Axis.YP.rotationDegrees(-90.0f));
            case WEST -> poseStack.mulPose(Axis.YP.rotationDegrees(90.0f));
            case SOUTH -> { }
            case UP -> poseStack.mulPose(Axis.XP.rotationDegrees(-90.0F));
            case DOWN -> poseStack.mulPose(Axis.XP.rotationDegrees(90.0F));
        }
    }

    private static void vertex(VertexConsumer consumer, Matrix4f matrix, Matrix3f normal,
                               float x, float y, float z, float u, float v, int light, int red, int green, int blue) {
        consumer.vertex(matrix, x, y, z)
                .color(red, green, blue, 255)
                .uv(u, v)
                .overlayCoords(OverlayTexture.NO_OVERLAY)
                .uv2(light)
                .normal(normal, 0, 0, 1)
                .endVertex();
    }

    private static void renderCustom(VertexConsumer consumer, Matrix4f matrix, Matrix3f normal,
                                     String encoded, int light, float renderWidth, float renderHeight) {
        byte[] pixels = CustomGraffiti.decode(encoded);
        float pixelWidth = renderWidth / CustomGraffiti.WIDTH;
        float pixelHeight = renderHeight / CustomGraffiti.HEIGHT;
        for (int py = 0; py < CustomGraffiti.HEIGHT; py++) {
            for (int px = 0; px < CustomGraffiti.WIDTH; px++) {
                int palette = pixels[py * CustomGraffiti.WIDTH + px] & 15;
                if (palette == 0) continue;
                int argb = CustomGraffiti.PALETTE[palette];
                int red = (argb >>> 16) & 255;
                int green = (argb >>> 8) & 255;
                int blue = argb & 255;
                float left = -renderWidth * 0.5F + px * pixelWidth;
                float right = left + pixelWidth + 0.0005f;
                float top = renderHeight * 0.5F - py * pixelHeight;
                float bottom = top - pixelHeight - 0.0005f;
                vertex(consumer, matrix, normal, left, bottom, 0, 0, 1, light, red, green, blue);
                vertex(consumer, matrix, normal, right, bottom, 0, 1, 1, light, red, green, blue);
                vertex(consumer, matrix, normal, right, top, 0, 1, 0, light, red, green, blue);
                vertex(consumer, matrix, normal, left, top, 0, 0, 0, light, red, green, blue);
            }
        }
    }

    @Override
    public ResourceLocation getTextureLocation(GraffitiEntity entity) {
        return GraffitiCatalog.texture(entity.getVariant());
    }
}
