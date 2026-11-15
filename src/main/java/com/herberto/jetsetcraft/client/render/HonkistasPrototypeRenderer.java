package com.herberto.jetsetcraft.client.render;

import com.herberto.jetsetcraft.JetSetCraft;
import com.herberto.jetsetcraft.entity.HonkistasPrototypeEntity;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.resources.ResourceLocation;

/** Native render path for the original study; does not install a production gang actor provider. */
public final class HonkistasPrototypeRenderer extends MobRenderer<HonkistasPrototypeEntity,HonkistasPrototypeModel> {
    private static final ResourceLocation TEXTURE=ResourceLocation.fromNamespaceAndPath(JetSetCraft.MOD_ID,
            "textures/entity/honkistas_prototype_white.png");
    public HonkistasPrototypeRenderer(EntityRendererProvider.Context context){
        super(context,new HonkistasPrototypeModel(),.38f);
    }
    @Override public ResourceLocation getTextureLocation(HonkistasPrototypeEntity entity){return TEXTURE;}
}
