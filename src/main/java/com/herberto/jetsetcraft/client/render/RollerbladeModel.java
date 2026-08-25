package com.herberto.jetsetcraft.client.render;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.model.PlayerModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeDeformation;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;
import net.minecraft.client.player.AbstractClientPlayer;

/**
 * Leg-baked rollerblade mesh ported from BeeIsYou/Street Art (MIT). Unlike an item transform, every boot,
 * chassis and wheel follows the corresponding player leg through ride, drift, wallrun and trick poses.
 */
public final class RollerbladeModel {
    private final ModelPart root;
    private final ModelPart rightLeg;
    private final ModelPart leftLeg;

    public RollerbladeModel() {
        root = createMesh().getRoot().bake(32, 32);
        rightLeg = root.getChild("right_leg");
        leftLeg = root.getChild("left_leg");
    }

    public void copyPose(PlayerModel<AbstractClientPlayer> playerModel) {
        rightLeg.copyFrom(playerModel.rightLeg);
        leftLeg.copyFrom(playerModel.leftLeg);
    }

    public void render(PoseStack poseStack, VertexConsumer consumer, int light) {
        root.render(poseStack, consumer, light, 0, 1.0F, 1.0F, 1.0F, 1.0F);
    }

    private static MeshDefinition createMesh() {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition root = mesh.getRoot();
        CubeDeformation none = new CubeDeformation(0.0F);
        CubeListBuilder skate = CubeListBuilder.create()
                .texOffs(16, 5).addBox(-2.0F, 9.0F, 2.25F, 4.0F, 2.0F, 0.0F, none)
                .texOffs(8, 2).addBox(-1.25F, 12.5F, -4.5F, 0.0F, 2.0F, 7.0F, none)
                .texOffs(8, 2).addBox(1.25F, 12.5F, -4.5F, 0.0F, 2.0F, 7.0F, none)
                .texOffs(0, 14).addBox(-1.0F, 13.0F, -5.0F, 2.0F, 2.0F, 8.0F, none)
                .texOffs(0, 9).addBox(-1.5F, 10.0F, -4.0F, 3.0F, 3.0F, 2.0F, none)
                .texOffs(0, 0).addBox(-2.0F, 8.0F, -2.0F, 4.0F, 5.0F, 4.0F,
                        new CubeDeformation(0.3F));
        CubeListBuilder tongue = CubeListBuilder.create()
                .texOffs(4, 6).addBox(-1.0F, -3.0F, -1.0F, 2.0F, 3.0F, 0.0F, none);
        PartPose tonguePose = PartPose.offsetAndRotation(0.0F, 10.0F, -1.25F,
                0.0873F, 0.0F, 0.0F);
        PartDefinition right = root.addOrReplaceChild("right_leg", skate, PartPose.ZERO);
        right.addOrReplaceChild("right_tongue", tongue, tonguePose);
        PartDefinition left = root.addOrReplaceChild("left_leg", skate, PartPose.ZERO);
        left.addOrReplaceChild("left_tongue", tongue, tonguePose);
        return mesh;
    }
}
