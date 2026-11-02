package com.herberto.jetsetcraft.client.render;

import com.herberto.jetsetcraft.entity.LibertyTransportEntity;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;

/** Original block-built hard-surface silhouettes, using only Minecraft's existing block materials. */
public final class LibertyTransportRenderer extends EntityRenderer<LibertyTransportEntity> {
    private static final java.util.Map<Integer, Long> AUDIT_RENDER_CALLS = Boolean.getBoolean("jetsetcraft.libertyVisualAudit")
            ? new java.util.HashMap<>() : null;
    private static final java.util.Map<Integer, Long> AUDIT_MOVING_RENDER_CALLS = AUDIT_RENDER_CALLS == null ? null : new java.util.HashMap<>();
    private record AuditAlignment(int consecutiveFrames, double error) {}
    private static final java.util.Map<Integer, AuditAlignment> AUDIT_ALIGNMENT = AUDIT_RENDER_CALLS == null ? null : new java.util.HashMap<>();
    public static long auditRenderCalls(int entityId) { return AUDIT_RENDER_CALLS == null ? 0 : AUDIT_RENDER_CALLS.getOrDefault(entityId, 0L); }
    public static long auditMovingRenderCalls(int entityId) { return AUDIT_MOVING_RENDER_CALLS == null ? 0 : AUDIT_MOVING_RENDER_CALLS.getOrDefault(entityId, 0L); }
    public static int auditAlignedMovingFrames(int entityId) { return AUDIT_ALIGNMENT == null ? 0 : AUDIT_ALIGNMENT.getOrDefault(entityId, new AuditAlignment(0, Double.POSITIVE_INFINITY)).consecutiveFrames(); }
    public static double auditSeatError(int entityId) { return AUDIT_ALIGNMENT == null ? Double.POSITIVE_INFINITY : AUDIT_ALIGNMENT.getOrDefault(entityId, new AuditAlignment(0, Double.POSITIVE_INFINITY)).error(); }
    public LibertyTransportRenderer(EntityRendererProvider.Context context) { super(context); shadowRadius = 1.5f; }
    @Override public void render(LibertyTransportEntity entity, float yaw, float partialTick, PoseStack pose,
                                 MultiBufferSource buffer, int light) {
        if (AUDIT_RENDER_CALLS != null) {
            long calls = AUDIT_RENDER_CALLS.merge(entity.getId(), 1L, Long::sum);
            var minecraft = Minecraft.getInstance(); var player = minecraft.player;
            boolean moving = !entity.ship() && entity.getY() < com.herberto.jetsetcraft.liberty.LibertyWorld.FLOOR_Y + 65;
            if (moving) {
                AUDIT_MOVING_RENDER_CALLS.merge(entity.getId(), 1L, Long::sum);
                if (player != null && player.getVehicle() == entity) {
                    double interpolatedGap = net.minecraft.util.Mth.lerp(partialTick, player.yOld, player.getY())
                            - net.minecraft.util.Mth.lerp(partialTick, entity.yOld, entity.getY());
                    double error = Math.abs(interpolatedGap - (player.getY() - entity.getY()));
                    int consecutive = Double.isFinite(error) && error <= .05 ? auditAlignedMovingFrames(entity.getId()) + 1 : 0;
                    AUDIT_ALIGNMENT.put(entity.getId(), new AuditAlignment(consecutive, error));
                }
            }
            if (calls <= 20) {
                var matrix = pose.last().pose();
                com.herberto.jetsetcraft.JetSetCraft.LOGGER.info("JETSETCRAFT_LIBERTY_VISUAL_AUDIT_RENDER frame={} partial={} pose=({},{},{}) camera={} entity={} old=({},{},{}) tickOld=({},{},{}) player={} playerOld=({},{},{}) playerTickOld=({},{},{}) light={} seatError={}",
                        calls, partialTick, matrix.m30(), matrix.m31(), matrix.m32(), minecraft.gameRenderer.getMainCamera().getPosition(),
                        entity.position(), entity.xOld, entity.yOld, entity.zOld, entity.xo, entity.yo, entity.zo,
                        player == null ? null : player.position(), player == null ? 0 : player.xOld, player == null ? 0 : player.yOld, player == null ? 0 : player.zOld,
                        player == null ? 0 : player.xo, player == null ? 0 : player.yo, player == null ? 0 : player.zo, light, auditSeatError(entity.getId()));
            }
        }
        pose.pushPose(); pose.mulPose(Axis.YP.rotationDegrees(-yaw));
        if (entity.ship()) {
            box(pose, buffer, light, Blocks.GRAY_CONCRETE, -1.7, -.35, -3, 3.4f, .65f, 6);
            box(pose, buffer, light, Blocks.LIGHT_GRAY_CONCRETE, -1.5, .3, -2.8, .4f, 1.7f, 5.6f);
            box(pose, buffer, light, Blocks.LIGHT_GRAY_CONCRETE, 1.1, .3, -2.8, .4f, 1.7f, 5.6f);
            box(pose, buffer, light, Blocks.GRAY_CONCRETE, -1.5, 2, -2.8, 3, .35f, 5.6f);
            box(pose, buffer, light, Blocks.CYAN_CONCRETE, -1.1, .35, -3.6, 2.2f, 1.2f, .65f);
            box(pose, buffer, light, Blocks.LIGHT_BLUE_STAINED_GLASS, -.95, 1.35, -3.5, 1.9f, .65f, .3f);
            for (int side : new int[]{-1, 1}) {
                box(pose, buffer, light, Blocks.GRAY_CONCRETE, side < 0 ? -4.6 : 1.5, .1, -.7, 3.1f, .3f, 2.8f);
                box(pose, buffer, light, Blocks.BLACK_CONCRETE, side < 0 ? -4.5 : 3.4, -.2, -.9, 1.1f, .8f, 3.3f);
                box(pose, buffer, light, Blocks.SEA_LANTERN, side < 0 ? -4.3 : 3.6, -.1, 2.35, .7f, .6f, .2f);
            }
            box(pose, buffer, light, Blocks.YELLOW_CONCRETE, -.65, -.35, 3, 1.3f, .2f, 1.8f);
        } else {
            box(pose, buffer, light, Blocks.GRAY_CONCRETE, -1.15, -.3, -1.15, 2.3f, .45f, 2.3f);
            for (int side : new int[]{-1, 1}) {
                box(pose, buffer, light, Blocks.DEEPSLATE_TILES, side < 0 ? -1.15 : .85, .1, -.95, .3f, 2.6f, 1.9f);
                box(pose, buffer, light, Blocks.YELLOW_CONCRETE, side < 0 ? -1.2 : 1, 1.6, -.9, .2f, .65f, 1.8f);
            }
            box(pose, buffer, light, Blocks.GRAY_CONCRETE, -1.15, 2.7, -1.15, 2.3f, .4f, 2.3f);
            box(pose, buffer, light, Blocks.BLACK_CONCRETE, -.85, .1, -.95, 1.7f, 2.6f, .2f);
            box(pose, buffer, light, Blocks.SEA_LANTERN, -.7, -.4, -.7, 1.4f, .15f, 1.4f);
        }
        pose.popPose(); super.render(entity, yaw, partialTick, pose, buffer, light);
    }
    private void box(PoseStack pose, MultiBufferSource buffer, int light, Block material,
                     double x, double y, double z, float sx, float sy, float sz) {
        pose.pushPose(); pose.translate(x, y, z); pose.scale(sx, sy, sz);
        Minecraft.getInstance().getBlockRenderer().renderSingleBlock(material.defaultBlockState(), pose, buffer, light, OverlayTexture.NO_OVERLAY);
        pose.popPose();
    }
    @Override public ResourceLocation getTextureLocation(LibertyTransportEntity entity) {
        return ResourceLocation.withDefaultNamespace("textures/atlas/blocks.png");
    }
}
