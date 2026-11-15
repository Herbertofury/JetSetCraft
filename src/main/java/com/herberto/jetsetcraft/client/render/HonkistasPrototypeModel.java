package com.herberto.jetsetcraft.client.render;

import com.herberto.jetsetcraft.entity.HonkistasPrototypeEntity;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;
import net.minecraft.util.Mth;

/**
 * Original theatrical courier study, authored in code; not a copied skin, named guest or accepted final NPC.
 * Seven color meshes share one articulated skeleton. No imported model/texture or secondary-physics claim.
 * Dimensions are model pixels (16 per block); feet terminate at the normal living-model ground y=24.
 */
public final class HonkistasPrototypeModel extends EntityModel<HonkistasPrototypeEntity> {
    private static final int INK=0, IVORY=1, TEAL=2, CORAL=3, GOLD=4, PALE=5, SLATE=6;
    private static final float[][] COLORS={
            {.055f,.105f,.135f}, {.95f,.91f,.79f}, {.06f,.49f,.48f},
            {.93f,.30f,.25f}, {.93f,.67f,.27f}, {.42f,.77f,.69f},
            {.10f,.28f,.30f}};
    private final ModelPart[] roots=new ModelPart[COLORS.length];

    public HonkistasPrototypeModel() {
        for(int i=0;i<roots.length;i++) roots[i]=mesh(i).getRoot().bake(64,64);
    }

    @Override public void setupAnim(HonkistasPrototypeEntity entity,float limbSwing,float limbSwingAmount,
                                    float ageInTicks,float netHeadYaw,float headPitch) {
        float stride=Mth.clamp(limbSwingAmount,0,1);
        float swing=Mth.cos(limbSwing*.6662f)*.65f*stride;
        for(ModelPart root:roots) {
            root.getAllParts().forEach(ModelPart::resetPose);
            ModelPart torso=root.getChild("torso"),head=torso.getChild("head");
            head.yRot=Mth.clamp(netHeadYaw,-65,65)*Mth.DEG_TO_RAD;
            head.xRot=Mth.clamp(headPitch,-30,35)*Mth.DEG_TO_RAD;
            for(int side:new int[]{-1,1}) {
                String n=side<0?"right":"left";
                ModelPart arm=torso.getChild(n+"_arm"),leg=root.getChild(n+"_leg");
                arm.xRot=-swing*side*.75f;arm.zRot=side*.055f;
                arm.getChild("forearm").xRot=-.10f-Math.max(0,-swing*side)*.35f;
                leg.xRot=swing*side;
                leg.getChild("shin").xRot=Math.max(0,-swing*side)*.65f;
                // Authored gait pose only, not movement-responsive cloth simulation.
                torso.getChild(n+"_tail").xRot=-.12f-Math.max(0,swing*side)*.13f;
            }
        }
    }

    @Override public void renderToBuffer(PoseStack pose,VertexConsumer buffer,int light,int overlay,
                                         float red,float green,float blue,float alpha) {
        for(int i=0;i<roots.length;i++) roots[i].render(pose,buffer,light,overlay,
                red*COLORS[i][0],green*COLORS[i][1],blue*COLORS[i][2],alpha);
    }

    private static MeshDefinition mesh(int palette) {
        Builder b=new Builder(palette);PartDefinition root=b.mesh.getRoot();
        PartDefinition torso=b.bone(root,"torso",0,6,0);
        // Tapered waistcoat, stepped shoulder line and tailored rear panel.
        b.box(torso,TEAL,-3.1f,0,-1.8f,6.2f,3.6f,3.6f);
        b.box(torso,TEAL,-2.6f,3.6f,-1.6f,5.2f,3.6f,3.2f);
        b.box(torso,INK,-2.7f,7.0f,-1.65f,5.4f,.8f,3.3f);
        b.box(torso,GOLD,-.6f,7.05f,-1.85f,1.2f,.65f,.25f);
        b.box(torso,IVORY,-1.25f,.2f,-1.96f,2.5f,4.5f,.24f);
        b.rotBox(torso,GOLD,-1.45f,1.7f,-2.08f,0,0,-.28f,-.18f,-1.8f,-.08f,.36f,3.6f,.16f);
        b.rotBox(torso,GOLD,1.45f,1.7f,-2.08f,0,0,.28f,-.18f,-1.8f,-.08f,.36f,3.6f,.16f);
        for(int y=3;y<=6;y++) b.box(torso,GOLD,-.2f,y,-2.02f,.4f,.4f,.2f);
        // Local tailoring, not a uniform noise overlay: center-back seam and two waist darts.
        b.box(torso,SLATE,-.10f,.25f,1.81f,.20f,3.3f,.08f);
        b.box(torso,SLATE,-.10f,3.6f,1.61f,.20f,3.35f,.08f);
        for(int side:new int[]{-1,1}) {
            b.rotBox(torso,SLATE,side*1.9f,2.9f,1.84f,0,0,side*.15f,
                    -.13f,-1.2f,-.04f,.26f,2.4f,.12f);
            // Recess-colored pocket mouth below the existing coral welt.
            b.box(torso,INK,side<0?-2.65f:1.55f,4.55f,-1.85f,1.1f,.16f,.12f);
            b.rotBox(torso,SLATE,side*2.1f,5.4f,-1.66f,0,0,-side*.20f,
                    -.13f,-.5f,-.05f,.26f,1.0f,.12f);
        }
        // Split pointed ruff instead of a generic player neckline.
        for(int side:new int[]{-1,1}) {
            b.rotBox(torso,IVORY,side*1.6f,.05f,-1.5f,-.18f,0,side*.24f,-1.2f,-.65f,-.55f,2.4f,.8f,2.0f);
            b.box(torso,CORAL,side<0?-2.7f:1.5f,4.3f,-1.83f,1.2f,.25f,.2f);
            PartDefinition tail=b.bone(torso,side<0?"right_tail":"left_tail",side*1.45f,7.2f,1.45f);
            float length=side<0?6.2f:5.3f;
            // Authored six-step taper preserves the split-tail silhouette at gameplay distance.
            for(int tier=0;tier<6;tier++) {
                float width=2.3f-tier*.32f, y=tier*length/6;
                float height=length/6+.025f;
                b.box(tail,TEAL,-width/2,y,-.15f,width,height,.55f);
                b.box(tail,CORAL,-width/2+.14f,y+.05f,.43f,width-.28f,height-.10f,.16f);
                b.box(tail,GOLD,-width/2,y,.42f,.12f,height,.2f);
                b.box(tail,GOLD,width/2-.12f,y,.42f,.12f,height,.2f);
            }
            b.box(tail,GOLD,-.35f,length-.25f,-.17f,.7f,.35f,.75f);
        }
        PartDefinition head=b.bone(torso,"head",0,-.4f,0);
        b.box(head,IVORY,-2.8f,-5.4f,-2.35f,5.6f,5.4f,4.5f);
        b.box(head,IVORY,-2.25f,-.2f,-2.2f,4.5f,.65f,3.8f);
        b.box(head,PALE,-3.2f,-3.35f,-.5f,.65f,1.7f,1.25f);
        b.box(head,PALE,2.55f,-3.35f,-.5f,.65f,1.7f,1.25f);
        // Different eye frames and brows make the mask deliberately asymmetric.
        b.box(head,INK,-2.1f,-3.65f,-2.53f,1.65f,1.15f,.2f);
        b.box(head,INK,.4f,-3.4f,-2.53f,1.65f,1.0f,.2f);
        b.box(head,PALE,-1.72f,-3.42f,-2.76f,.68f,.66f,.23f);
        b.box(head,GOLD,.9f,-3.23f,-2.76f,.6f,.6f,.23f);
        b.box(head,INK,-1.4f,-3.36f,-2.99f,.22f,.5f,.08f);
        b.box(head,INK,1.1f,-3.19f,-2.99f,.2f,.48f,.08f);
        b.rotBox(head,TEAL,-1.35f,-4.0f,-2.62f,0,0,-.16f,-.9f,-.14f,-.08f,1.8f,.28f,.16f);
        b.rotBox(head,INK,1.25f,-3.9f,-2.62f,0,0,.22f,-.8f,-.14f,-.08f,1.6f,.28f,.16f);
        b.box(head,CORAL,-.35f,-2.45f,-2.82f,.7f,.65f,.55f);
        b.box(head,INK,-.9f,-1.05f,-2.54f,1.8f,.25f,.2f);
        b.box(head,CORAL,-1.2f,-1.35f,-2.53f,.3f,.45f,.18f);
        b.box(head,CORAL,.9f,-1.35f,-2.53f,.3f,.45f,.18f);
        b.rotBox(head,TEAL,-2.0f,-1.95f,-2.5f,0,0,.5f,-.22f,-.5f,-.08f,.44f,1,.16f);
        // Short sculpted swept hair: separately placed locks, not copied reference hair.
        b.box(head,INK,-2.95f,-5.65f,-1.55f,5.9f,1.2f,3.9f);
        for(int side:new int[]{-1,1}) for(int lock=0;lock<3;lock++) {
            b.rotBox(head,lock==1?TEAL:INK,side*2.73f,-4.3f+lock*.9f,.45f+lock*.45f,
                    .12f,side*.12f,side*.15f,-.35f,-.65f,-.7f,.7f,1.5f,1.4f);
            // Narrow raised facet on the outward surface, following each lock's own angle.
            b.rotBox(head,SLATE,side*2.73f,-4.3f+lock*.9f,.45f+lock*.45f,
                    .12f,side*.12f,side*.15f,side<0?-.40f:.31f,-.48f,-.4f,.09f,1.12f,.65f);
        }
        // Layered rear locks cover the skull without copying the benchmark's long white hair.
        b.box(head,INK,-2.65f,-5.15f,2.08f,5.3f,2.5f,.5f);
        for(int lock=0;lock<5;lock++) {
            float x=-2.15f+lock*1.075f;
            float length=3.6f-Math.abs(lock-2)*.3f;
            b.rotBox(head,lock==1||lock==3?TEAL:INK,x,-3.5f,2.35f,
                    -.07f,(lock-2)*.025f,(lock-2)*-.045f,
                    -.54f,-1.1f,-.25f,1.08f,length,.55f);
            b.rotBox(head,SLATE,x,-3.5f,2.35f,
                    -.07f,(lock-2)*.025f,(lock-2)*-.045f,
                    -.30f,-.95f,.31f,.24f,length-.35f,.07f);
            b.box(head,INK,x-.38f,-3.5f+length-1.1f,2.27f,.76f,.45f,.45f);
        }
        // Off-center tilted parade cap with stacked crown and coral ribbon fan.
        PartDefinition cap=b.rotBone(head,"cap",-.45f,-5.45f,.2f,.03f,0,-.16f);
        b.box(cap,INK,-3.45f,-.4f,-2.6f,6.9f,.6f,5.2f);
        b.box(cap,TEAL,-2.65f,-2.2f,-1.95f,5.3f,1.9f,3.9f);
        b.box(cap,TEAL,-2.2f,-3.6f,-1.6f,4.4f,1.5f,3.2f);
        b.box(cap,GOLD,-2.7f,-1.15f,-2.0f,5.4f,.5f,4f);
        // Two intentional compressed crown creases, with no added animated joint.
        for(int side:new int[]{-1,1}) b.rotBox(cap,SLATE,side*1.65f,-2.7f,-1.63f,
                0,0,-side*.16f,-.12f,-.55f,-.05f,.24f,1.1f,.10f);
        b.box(cap,CORAL,-.9f,-3.85f,-1.7f,1.8f,.35f,3.4f);
        for(int i=0;i<3;i++) b.rotBox(cap,CORAL,2.2f,-2.4f,.2f,0,0,.25f+i*.18f,-.25f,-2.0f,-.18f,.5f,2.6f,.36f);
        for(int side:new int[]{-1,1}) {
            String name=side<0?"right":"left";
            PartDefinition arm=b.bone(torso,name+"_arm",side*3.55f,.65f,0);
            b.box(arm,side<0?CORAL:TEAL,-1.7f,-.55f,-1.7f,3.4f,2.4f,3.4f);
            b.box(arm,GOLD,-1.8f,.2f,-1.8f,3.6f,.45f,3.6f);
            b.box(arm,TEAL,-1.1f,1.7f,-1.1f,2.2f,3.5f,2.2f);
            // A shadowed elbow fold plus cuff stitch distinguishes cloth from polished trim.
            b.rotBox(arm,SLATE,0,4.35f,-1.14f,0,0,side*.13f,
                    -.85f,-.12f,-.04f,1.7f,.24f,.10f);
            PartDefinition forearm=b.bone(arm,"forearm",0,5.0f,0);
            b.box(forearm,side<0?TEAL:CORAL,-1.05f,0,-1.05f,2.1f,3.3f,2.1f);
            b.box(forearm,IVORY,-1.45f,2.7f,-1.45f,2.9f,.7f,2.9f);
            b.box(forearm,PALE,-1.18f,3.15f,-1.48f,2.36f,.12f,.06f);
            b.box(forearm,GOLD,-1.4f,3.4f,-1.4f,2.8f,.25f,2.8f);
            // Shaped palm, four separated fingers and angled thumb, all following the elbow rig.
            b.box(forearm,IVORY,-.92f,3.65f,-.72f,1.84f,.85f,1.44f);
            b.box(forearm,IVORY,-.8f,4.45f,-.66f,1.6f,.4f,1.32f);
            for(int digit=0;digit<4;digit++) {
                float length=digit==0||digit==3?.65f:.85f;
                b.rotBox(forearm,IVORY,-.66f+digit*.44f,4.75f,-.12f,
                        -.18f,0,0,-.16f,0,-.50f,.32f,length,1.0f);
                b.box(forearm,PALE,-.76f+digit*.44f,4.25f,-.76f,.20f,.3f,.09f);
            }
            b.rotBox(forearm,IVORY,side<0?.86f:-.86f,3.95f,-.12f,
                    -.14f,0,-side*.32f,-.24f,0,-.45f,.48f,1.0f,.9f);
            PartDefinition leg=b.bone(root,name+"_leg",side*1.45f,13.6f,0);
            b.box(leg,INK,-1.3f,0,-1.2f,2.6f,5.1f,2.4f);
            b.box(leg,side<0?CORAL:PALE,side<0?-1.4f:1.1f,.3f,-1.25f,.3f,4.6f,2.5f);
            PartDefinition shin=b.bone(leg,"shin",0,5,0);
            b.box(shin,INK,-1.2f,0,-1.1f,2.4f,4.8f,2.2f);
            b.box(shin,TEAL,-1.4f,2.9f,-1.35f,2.8f,1.6f,2.7f);
            b.box(shin,GOLD,-1.35f,2.8f,-1.45f,2.7f,.4f,2.9f);
            b.box(shin,INK,-1.35f,4.1f,-2.7f,2.7f,1.0f,4.2f);
            b.box(shin,CORAL,-1.25f,4.0f,-3.3f,2.5f,.65f,1.2f);
            b.box(shin,IVORY,-1.35f,5.1f,-2.7f,2.7f,.3f,4.2f);
        }
        return b.mesh;
    }

    private static final class Builder {
        final int palette;final MeshDefinition mesh=new MeshDefinition();int number;
        Builder(int palette){this.palette=palette;}
        PartDefinition bone(PartDefinition parent,String name,float x,float y,float z){
            return parent.addOrReplaceChild(name,CubeListBuilder.create(),PartPose.offset(x,y,z));
        }
        PartDefinition rotBone(PartDefinition parent,String name,float x,float y,float z,float rx,float ry,float rz){
            return parent.addOrReplaceChild(name,CubeListBuilder.create(),PartPose.offsetAndRotation(x,y,z,rx,ry,rz));
        }
        void box(PartDefinition parent,int color,float x,float y,float z,float w,float h,float d){
            if(color==palette) parent.addOrReplaceChild("shape_"+number++,CubeListBuilder.create().texOffs(0,0).addBox(x,y,z,w,h,d),PartPose.ZERO);
        }
        void rotBox(PartDefinition parent,int color,float px,float py,float pz,float rx,float ry,float rz,
                    float x,float y,float z,float w,float h,float d){
            if(color==palette) parent.addOrReplaceChild("shape_"+number++,CubeListBuilder.create().texOffs(0,0).addBox(x,y,z,w,h,d),PartPose.offsetAndRotation(px,py,pz,rx,ry,rz));
        }
    }
}
