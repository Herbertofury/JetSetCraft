package com.herberto.jetsetcraft.registry;

import com.herberto.jetsetcraft.JetSetCraft;
import com.herberto.jetsetcraft.gang.GangDefinition;
import com.herberto.jetsetcraft.gang.GangRegistry;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;

/** Data-addressable gang music slots. Current packaged audio is valid silence, ready for owner-authored OGG replacement. */
public final class ModSounds {
    public static final DeferredRegister<SoundEvent> SOUNDS = DeferredRegister.create(ForgeRegistries.SOUND_EVENTS, JetSetCraft.MOD_ID);
    private static final Map<ResourceLocation, RegistryObject<SoundEvent>> GANG_MUSIC = new LinkedHashMap<>();

    static {
        for (GangDefinition definition : GangRegistry.curated()) {
            ResourceLocation soundId = definition.musicId();
            if (!JetSetCraft.MOD_ID.equals(soundId.getNamespace())) continue;
            GANG_MUSIC.put(definition.id(), SOUNDS.register(soundId.getPath(),
                    () -> SoundEvent.createVariableRangeEvent(soundId)));
        }
        ResourceLocation generic = new ResourceLocation(JetSetCraft.MOD_ID, "music/gangs/generic");
        GANG_MUSIC.put(new ResourceLocation(JetSetCraft.MOD_ID, "mob/generic"),
                SOUNDS.register(generic.getPath(), () -> SoundEvent.createVariableRangeEvent(generic)));
    }

    public static Optional<SoundEvent> music(ResourceLocation gangId) {
        RegistryObject<SoundEvent> direct = GANG_MUSIC.get(gangId);
        if (direct != null && direct.isPresent()) return Optional.of(direct.get());
        RegistryObject<SoundEvent> generic = GANG_MUSIC.get(new ResourceLocation(JetSetCraft.MOD_ID, "mob/generic"));
        return generic != null && generic.isPresent() ? Optional.of(generic.get()) : Optional.empty();
    }

    private ModSounds() {}
}
