package com.herberto.jetsetcraft.registry;

import com.herberto.jetsetcraft.JetSetCraft;
import com.herberto.jetsetcraft.entity.GraffitiEntity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public final class ModEntities {
    public static final DeferredRegister<EntityType<?>> ENTITIES = DeferredRegister.create(ForgeRegistries.ENTITY_TYPES, JetSetCraft.MOD_ID);

    public static final RegistryObject<EntityType<GraffitiEntity>> GRAFFITI = ENTITIES.register("graffiti",
            () -> EntityType.Builder.<GraffitiEntity>of(GraffitiEntity::new, MobCategory.MISC)
                    .sized(2.6f, 1.3f)
                    .clientTrackingRange(8)
                    .updateInterval(20)
                    .build("jetsetcraft:graffiti"));

    private ModEntities() {}
}
