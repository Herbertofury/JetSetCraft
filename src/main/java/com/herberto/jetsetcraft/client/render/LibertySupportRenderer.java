package com.herberto.jetsetcraft.client.render;

import com.herberto.jetsetcraft.entity.LibertySupportEntity;
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

/** Original mechanical meshes with visible magazines, barrels, cockpits, running gear and paint reservoirs. */
public final class LibertySupportRenderer extends EntityRenderer<LibertySupportEntity> {
    public LibertySupportRenderer(EntityRendererProvider.Context context) { super(context);shadowRadius=.8f; }
    @Override public void render(LibertySupportEntity e,float yaw,float partial,PoseStack p,MultiBufferSource b,int light) {
        String role=e.role(); p.pushPose();p.mulPose(Axis.YP.rotationDegrees(-yaw));
        Block accent=role.contains("FIRE")||role.contains("FLAME")?Blocks.ORANGE_CONCRETE:role.contains("GAS")?Blocks.LIME_CONCRETE
                :role.contains("PAINT")||role.contains("TAG")?Blocks.MAGENTA_CONCRETE:role.contains("LASER")||role.contains("EMS")?Blocks.CYAN_CONCRETE:Blocks.YELLOW_CONCRETE;
        boolean mech=role.equals("PATRIOT")||role.equals("EMANCIPATOR")||role.equals("LUMBERER")||role.equals("BREAKTHROUGH");
        if (e.vehicle() && mech) {
            for(int side:new int[]{-1,1}) {
                double x=side<0?-.95:.35;
                box(p,b,light,Blocks.BLACK_CONCRETE,x,0,-.7,.6f,.25f,1.2f);
                box(p,b,light,Blocks.IRON_BLOCK,x+.12,.25,-.3,.36f,1.2f,.55f);
                box(p,b,light,accent,x-.05,.75,-.5,.7f,.5f,.7f);
                box(p,b,light,Blocks.GRAY_CONCRETE,side<0?-1.6:1.05,1.65,-.45,.55f,.8f,1.2f);
                int barrels=role.equals("EMANCIPATOR")?2:side<0?3:1;
                for(int n=0;n<barrels;n++) box(p,b,light,Blocks.BLACK_CONCRETE,side<0?-1.48:1.17,1.8+n*.2,-1.6,.28f,.14f,1.2f);
            }
            box(p,b,light,Blocks.DEEPSLATE_TILES,-.85,1.35,-.7,1.7f,1.65f,1.6f);
            box(p,b,light,accent,-.9,2.85,-.7,1.8f,.3f,1.7f);
            box(p,b,light,Blocks.CYAN_STAINED_GLASS,-.6,2.05,-.76,1.2f,.65f,.1f);
            box(p,b,light,Blocks.IRON_BLOCK,-.7,1.65,.9,1.4f,1.1f,.4f);
        } else if(e.vehicle()) {
            box(p,b,light,Blocks.DEEPSLATE_TILES,-1.1,.4,-1.9,2.2f,.65f,3.8f);
            for(int side:new int[]{-1,1}) for(int z:new int[]{-1,1}) {
                box(p,b,light,Blocks.BLACK_CONCRETE,side<0?-1.35:.95,.12,z*1.2-.4,.4f,.8f,.8f);
                box(p,b,light,Blocks.IRON_BLOCK,side<0?-1.4:1.3,.35,z*1.2-.2,.1f,.35f,.4f);
            }
            box(p,b,light,accent,-1.15,1,-1.7,2.3f,.25f,.8f);
            box(p,b,light,Blocks.CYAN_STAINED_GLASS,-.9,1.25,-1.25,1.8f,.75f,.12f);
            box(p,b,light,Blocks.GRAY_CONCRETE,-1,2,-1.25,2,.12f,1.7f);
            for(int side:new int[]{-1,1}) box(p,b,light,Blocks.IRON_BLOCK,side<0?-1:.85,1,-1.25,.15f,1,.15f);
            box(p,b,light,Blocks.GRAY_CONCRETE,-.45,1,.8,.9f,.55f,.9f);
            barrel(p,b,light,accent,0,1.8,.8,role.equals("SUPPLY_FRV")?.7f:2.3f);
        } else if(e.drone() || role.contains("TAG_TEAM")) {
            box(p,b,light,accent,-.4,.4,-.35,.8f,.35f,.7f);
            box(p,b,light,Blocks.OBSERVER,-.22,.12,-.45,.44f,.3f,.5f);
            for(int side:new int[]{-1,1}) {
                box(p,b,light,Blocks.IRON_BLOCK,side<0?-.85:.35,.55,-.08,.5f,.1f,.16f);
                p.pushPose();p.translate(side*.85,.7,0);p.mulPose(Axis.YP.rotationDegrees((e.tickCount+partial)*30));
                box(p,b,light,Blocks.BLACK_CONCRETE,-.55,0,-.06,1.1f,.06f,.12f);p.popPose();
            }
            barrel(p,b,light,accent,0,.25,-.3,.8f);
        } else if(e.mine()) {
            box(p,b,light,Blocks.POLISHED_DEEPSLATE,-.35,.02,-.35,.7f,.12f,.7f);
            box(p,b,light,accent,-.22,.14,-.22,.44f,.07f,.44f);
            box(p,b,light,Blocks.REDSTONE_BLOCK,-.06,.22,-.06,.12f,.08f,.12f);
        } else if(role.contains("SHIELD")||role.equals("TESLA")||role.contains("GHOST")) {
            box(p,b,light,Blocks.DEEPSLATE_TILES,-.6,0,-.6,1.2f,.25f,1.2f);
            box(p,b,light,Blocks.IRON_BLOCK,-.16,.25,-.16,.32f,1.6f,.32f);
            for(int y=0;y<3;y++) box(p,b,light,Blocks.COPPER_BLOCK,-.4,.65+y*.35,-.4,.8f,.12f,.8f);
            box(p,b,light,Blocks.SEA_LANTERN,-.3,1.9,-.3,.6f,.4f,.6f);
            box(p,b,light,Blocks.CYAN_STAINED_GLASS,-.7,1,-.7,1.4f,1.5f,1.4f);
        } else {
            for(int i=0;i<3;i++) {p.pushPose();p.mulPose(Axis.YP.rotationDegrees(i*120));
                box(p,b,light,Blocks.IRON_BLOCK,-.12,0,-.9,.24f,.18f,1.2f);p.popPose();}
            box(p,b,light,Blocks.DEEPSLATE_TILES,-.4,.15,-.35,.8f,.6f,.7f);
            box(p,b,light,accent,-.45,.75,-.4,.9f,.45f,.8f);
            box(p,b,light,Blocks.IRON_BLOCK,-.65,.55,0,.25f,.55f,.55f);
            barrel(p,b,light,accent,0,1,-.3,role.contains("MORTAR")?.55f:1.7f);
            if(role.contains("ROCKET")||role.contains("AUTOCANNON")) barrel(p,b,light,accent,.3,1.2,-.3,1.5f);
        }
        if(e.firing()) box(p,b,15728880,Blocks.SHROOMLIGHT,-.12,.95,-2,.24f,.24f,.24f);
        p.popPose();super.render(e,yaw,partial,p,b,light);
    }
    private static void barrel(PoseStack p,MultiBufferSource b,int light,Block accent,double x,double y,double z,float length) {
        box(p,b,light,Blocks.BLACK_CONCRETE,x-.13,y,z-length,.26f,.26f,length);
        box(p,b,light,accent,x-.18,y-.05,z-length+.15,.36f,.36f,.22f);
    }
    private static void box(PoseStack p,MultiBufferSource b,int light,Block block,double x,double y,double z,float sx,float sy,float sz) {
        p.pushPose();p.translate(x,y,z);p.scale(sx,sy,sz);
        Minecraft.getInstance().getBlockRenderer().renderSingleBlock(block.defaultBlockState(),p,b,light,OverlayTexture.NO_OVERLAY);p.popPose();
    }
    @Override public ResourceLocation getTextureLocation(LibertySupportEntity e) {return ResourceLocation.withDefaultNamespace("textures/atlas/blocks.png");}
}
