package com.herberto.jetsetcraft.gang;

import net.minecraft.resources.ResourceLocation;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;

/**
 * Reload-safe exact item-id mappings for mob heads, emblems and compatibility tokens.
 * Datapacks can target optional-mod items without JetSetCraft linking their classes.
 */
public final class HeadTargetMappingRegistry {
    public record Mapping(ResourceLocation entityId, ResourceLocation gangId) {
        public Mapping {
            if (entityId == null) throw new IllegalArgumentException("Head target entity id is required");
        }
    }

    private static volatile Map<ResourceLocation, Mapping> mappings = Map.of();

    public static Optional<Mapping> find(ResourceLocation itemId) {
        if (itemId == null) return Optional.empty();
        return Optional.ofNullable(mappings.get(itemId));
    }

    public static int dataMappingCount() {
        return mappings.size();
    }

    public static void replaceFromData(Map<ResourceLocation, Mapping> loaded) {
        LinkedHashMap<ResourceLocation, Mapping> copy = new LinkedHashMap<>();
        if (loaded != null) {
            loaded.entrySet().stream().sorted(Map.Entry.comparingByKey())
                    .forEach(entry -> copy.put(entry.getKey(), entry.getValue()));
        }
        mappings = Map.copyOf(copy);
    }

    private HeadTargetMappingRegistry() {}
}
