package com.herberto.jetsetcraft.registry;

import com.herberto.jetsetcraft.JetSetCraft;
import com.herberto.jetsetcraft.entity.GraffitiEntity;
import com.herberto.jetsetcraft.entity.PaintBalloonEntity;
import com.herberto.jetsetcraft.entity.LibertyTransportEntity;
import com.herberto.jetsetcraft.entity.LibertyCoreEntity;
import com.herberto.jetsetcraft.entity.LibertySupportEntity;
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

    public static final RegistryObject<EntityType<PaintBalloonEntity>> PAINT_BALLOON = ENTITIES.register("paint_balloon",
            () -> EntityType.Builder.<PaintBalloonEntity>of(PaintBalloonEntity::new, MobCategory.MISC)
                    .sized(0.25F, 0.25F)
                    .clientTrackingRange(8)
                    .updateInterval(10)
                    .build("jetsetcraft:paint_balloon"));

    public static final RegistryObject<EntityType<LibertyTransportEntity>> LIBERTY_TRANSPORT = ENTITIES.register("liberty_transport",
            () -> EntityType.Builder.<LibertyTransportEntity>of(LibertyTransportEntity::new, MobCategory.MISC)
                    .sized(2.4F, 3.2F).clientTrackingRange(12).updateInterval(1).fireImmune()
                    .build("jetsetcraft:liberty_transport"));

    public static final RegistryObject<EntityType<LibertyCoreEntity>> LIBERTY_CORE = ENTITIES.register("liberty_core",
            () -> EntityType.Builder.<LibertyCoreEntity>of(LibertyCoreEntity::new, MobCategory.MISC)
                    .sized(3F, 3F).clientTrackingRange(12).updateInterval(3).fireImmune().build("jetsetcraft:liberty_core"));
    public static final RegistryObject<EntityType<LibertySupportEntity>> LIBERTY_SUPPORT = ENTITIES.register("liberty_support",
            () -> EntityType.Builder.<LibertySupportEntity>of(LibertySupportEntity::new, MobCategory.MISC)
                    .sized(1.5F, 1.7F).clientTrackingRange(12).updateInterval(1).fireImmune().build("jetsetcraft:liberty_support"));

    @net.minecraftforge.fml.common.Mod.EventBusSubscriber(modid = JetSetCraft.MOD_ID, bus = net.minecraftforge.fml.common.Mod.EventBusSubscriber.Bus.MOD)
    public static final class Attributes {
        @net.minecraftforge.eventbus.api.SubscribeEvent
        public static void register(net.minecraftforge.event.entity.EntityAttributeCreationEvent event) {
            event.put(LIBERTY_CORE.get(), LibertyCoreEntity.createAttributes().build());
            event.put(LIBERTY_SUPPORT.get(), LibertySupportEntity.createAttributes().build());
        }
    }

    private ModEntities() {}
}
