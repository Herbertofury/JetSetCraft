package com.herberto.jetsetcraft.mob;

import com.herberto.jetsetcraft.JetSetCraft;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.AgeableMob;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.FlyingMob;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.entity.animal.AbstractFish;
import net.minecraft.world.entity.animal.Squid;
import net.minecraft.world.entity.animal.WaterAnimal;
import net.minecraft.world.entity.monster.Slime;

/** Data-first rig classification with conservative geometry fallbacks for unknown modded mobs. */
public final class MobRideRigResolver {
    public static final TagKey<EntityType<?>> BIPED = tag("ride_rig/biped");
    public static final TagKey<EntityType<?>> QUADRUPED = tag("ride_rig/quadruped");
    public static final TagKey<EntityType<?>> MULTI_LEG = tag("ride_rig/multi_leg");
    public static final TagKey<EntityType<?>> BODY_CONTACT = tag("ride_rig/body_contact");
    public static final TagKey<EntityType<?>> AERIAL = tag("ride_rig/aerial");
    public static final TagKey<EntityType<?>> AQUATIC = tag("ride_rig/aquatic");
    public static final TagKey<EntityType<?>> INCOMPATIBLE = tag("street_gear_incompatible");

    public static MobRideRig resolve(LivingEntity entity) {
        EntityType<?> type = entity.getType();
        if (type.is(BIPED)) return MobRideRig.BIPED;
        if (type.is(QUADRUPED)) return MobRideRig.QUADRUPED;
        if (type.is(MULTI_LEG)) return MobRideRig.MULTI_LEG;
        if (type.is(BODY_CONTACT)) return MobRideRig.BODY_CONTACT;
        if (type.is(AERIAL)) return MobRideRig.AERIAL;
        if (type.is(AQUATIC)) return MobRideRig.AQUATIC;

        if (entity instanceof Slime) return MobRideRig.BODY_CONTACT;
        if (entity instanceof AbstractFish || entity instanceof Squid || entity instanceof WaterAnimal) {
            return MobRideRig.AQUATIC;
        }
        if (entity instanceof FlyingMob || entity.getType().getCategory() == MobCategory.AMBIENT) {
            return MobRideRig.AERIAL;
        }

        float width = entity.getBbWidth();
        float height = entity.getBbHeight();
        if (height >= width * 1.42f) return MobRideRig.BIPED;
        if (width >= height * 1.28f) return MobRideRig.BODY_CONTACT;
        return MobRideRig.QUADRUPED;
    }

    public static float ageScale(LivingEntity entity) {
        // Entity dimensions already shrink for juveniles; this is only a gentle equipment-specific refinement.
        return entity instanceof AgeableMob ageable && ageable.isBaby() ? 0.82f : 1.0f;
    }

    public static boolean incompatible(LivingEntity entity) {
        return entity.getType().is(INCOMPATIBLE);
    }

    private static TagKey<EntityType<?>> tag(String path) {
        return TagKey.create(Registries.ENTITY_TYPE, new ResourceLocation(JetSetCraft.MOD_ID, path));
    }

    private MobRideRigResolver() {}
}
