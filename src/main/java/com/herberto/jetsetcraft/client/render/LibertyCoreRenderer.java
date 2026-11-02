package com.herberto.jetsetcraft.client.render;

import com.herberto.jetsetcraft.entity.LibertyCoreEntity;
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

/** Original faction silhouettes: furnace stacks, organic nest mouths and hovering warp vessels. */
public final class LibertyCoreRenderer extends EntityRenderer<LibertyCoreEntity> {
    public LibertyCoreRenderer(EntityRendererProvider.Context context) { super(context); shadowRadius = 2; }
    @Override public void render(LibertyCoreEntity e, float yaw, float partial, PoseStack p, MultiBufferSource b, int light) {
        p.pushPose(); p.mulPose(Axis.YP.rotationDegrees(-yaw));
        if (e.kind().equals("BUG_HOLE")) {
            box(p,b,light,Blocks.BLACK_CONCRETE,-1.5,.03,-1.5,3,.15f,3);
            for (int i=0;i<8;i++) { p.pushPose(); p.mulPose(Axis.YP.rotationDegrees(i*45));
                box(p,b,light,Blocks.MOSS_BLOCK,-1.4,0,-1.4,2.8f,.65f,.55f);
                box(p,b,light,Blocks.ORANGE_CONCRETE,-.9,.2,-1.1,1.8f,.35f,.35f); p.popPose(); }
            box(p,b,light,Blocks.SHROOMLIGHT,-.6,.1,-.6,1.2f,.1f,1.2f);
        } else if (e.kind().equals("WARP_SHIP")) {
            box(p,b,light,Blocks.PURPUR_BLOCK,-2.8,.9,-1.6,5.6f,.5f,3.2f);
            box(p,b,light,Blocks.OBSIDIAN,-1.4,1.4,-1.1,2.8f,.7f,2.2f);
            box(p,b,light,Blocks.AMETHYST_BLOCK,-.9,2.1,-.7,1.8f,.5f,1.4f);
            for (int side:new int[]{-1,1}) box(p,b,light,Blocks.SEA_LANTERN,side<0?-2.7:2,.6,-1,.7f,.35f,2);
            box(p,b,light,Blocks.MAGENTA_STAINED_GLASS,-.7,.2,-.7,1.4f,.7f,1.4f);
        } else {
            box(p,b,light,Blocks.POLISHED_DEEPSLATE,-1.5,0,-1.4,3,2.3f,2.8f);
            box(p,b,light,Blocks.RED_CONCRETE,-1.6,1.9,-1.5,3.2f,.35f,3);
            box(p,b,light,Blocks.BLACK_CONCRETE,-.9,.4,-1.5,1.8f,1.1f,.2f);
            box(p,b,light,Blocks.SHROOMLIGHT,-.7,.5,-1.65,1.4f,.8f,.15f);
            for (int side:new int[]{-1,1}) box(p,b,light,Blocks.IRON_BLOCK,side<0?-1.2:.6,2.3,-.5,.6f,1.6f,.7f);
        }
        p.popPose(); super.render(e,yaw,partial,p,b,light);
    }
    private static void box(PoseStack p, MultiBufferSource b, int light, Block block, double x,double y,double z,float sx,float sy,float sz) {
        p.pushPose(); p.translate(x,y,z); p.scale(sx,sy,sz);
        Minecraft.getInstance().getBlockRenderer().renderSingleBlock(block.defaultBlockState(),p,b,light,OverlayTexture.NO_OVERLAY); p.popPose();
    }
    @Override public ResourceLocation getTextureLocation(LibertyCoreEntity e) { return ResourceLocation.withDefaultNamespace("textures/atlas/blocks.png"); }
}
