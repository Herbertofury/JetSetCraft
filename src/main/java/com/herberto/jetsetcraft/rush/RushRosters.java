package com.herberto.jetsetcraft.rush;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.herberto.jetsetcraft.JetSetCraft;
import com.herberto.jetsetcraft.gang.GangRegistry;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.SimpleJsonResourceReloadListener;
import net.minecraft.util.profiling.ProfilerFiller;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/** Datapacks add themed casts without replacing canonical crew identities or optional mob registries. */
public final class RushRosters extends SimpleJsonResourceReloadListener {
    private static volatile Map<ResourceLocation, List<ResourceLocation>> rosters = Map.of();

    public RushRosters() { super(new Gson(), "jetsetcraft_rush_rosters"); }

    @Override
    protected void apply(Map<ResourceLocation, JsonElement> files, ResourceManager resources, ProfilerFiller profiler) {
        Map<ResourceLocation, List<ResourceLocation>> next = new LinkedHashMap<>();
        files.entrySet().stream().sorted(Map.Entry.comparingByKey()).forEach(entry -> {
            try {
                var object = entry.getValue().getAsJsonObject();
                ResourceLocation gang = ResourceLocation.tryParse(object.get("gang").getAsString());
                if (gang == null) throw new IllegalArgumentException("Invalid gang ID");
                List<ResourceLocation> entities = new ArrayList<>();
                for (JsonElement element : object.getAsJsonArray("entities")) {
                    ResourceLocation entity = ResourceLocation.tryParse(element.getAsString());
                    if (entity == null) throw new IllegalArgumentException("Invalid entity ID");
                    if (!entities.contains(entity)) entities.add(entity);
                }
                if (!entities.isEmpty()) next.put(gang, List.copyOf(entities));
            } catch (RuntimeException error) {
                JetSetCraft.LOGGER.warn("Ignoring malformed Rival Rush roster {}: {}", entry.getKey(), error.getMessage());
            }
        });
        rosters = Map.copyOf(next);
    }

    public static List<ResourceLocation> forGang(ResourceLocation gang, ResourceLocation selected) {
        // An explicit species override is respected; the representative selects the crew's full themed cast.
        ResourceLocation representative = GangRegistry.representativeEntity(gang).orElse(selected);
        if (selected != null && !selected.equals(representative)) return List.of(selected);
        List<ResourceLocation> configured = rosters.get(gang);
        return configured == null ? List.of(selected == null ? representative : selected) : configured;
    }

}
