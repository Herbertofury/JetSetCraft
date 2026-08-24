package com.herberto.jetsetcraft.gang.data;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.herberto.jetsetcraft.JetSetCraft;
import com.herberto.jetsetcraft.gang.GangRegistry;
import com.herberto.jetsetcraft.gang.HeadTargetMappingRegistry;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.SimpleJsonResourceReloadListener;
import net.minecraft.util.GsonHelper;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.LinkedHashMap;
import java.util.Map;

/** Loads data/<namespace>/jetsetcraft_head_targets/*.json exact item -> source mob/gang mappings. */
public final class HeadTargetMappingReloadListener extends SimpleJsonResourceReloadListener {
    private static final Gson GSON = new GsonBuilder().setLenient().create();

    public HeadTargetMappingReloadListener() {
        super(GSON, "jetsetcraft_head_targets");
    }

    @Override
    protected void apply(Map<ResourceLocation, JsonElement> resources, ResourceManager manager, ProfilerFiller profiler) {
        LinkedHashMap<ResourceLocation, HeadTargetMappingRegistry.Mapping> loaded = new LinkedHashMap<>();
        resources.entrySet().stream().sorted(Map.Entry.comparingByKey()).forEach(entry -> {
            try {
                JsonObject json = GsonHelper.convertToJsonObject(entry.getValue(), "JetSetCraft head target mapping");
                ResourceLocation itemId = requiredId(json, "item");
                ResourceLocation entityId = requiredId(json, "entity");
                if (!ForgeRegistries.ITEMS.containsKey(itemId)) {
                    JetSetCraft.LOGGER.debug("Skipping head target {} because item {} is not installed", entry.getKey(), itemId);
                    return;
                }
                if (!ForgeRegistries.ENTITY_TYPES.containsKey(entityId)) {
                    JetSetCraft.LOGGER.debug("Skipping head target {} because entity {} is not installed", entry.getKey(), entityId);
                    return;
                }
                ResourceLocation gangId = json.has("gang") ? requiredId(json, "gang") : GangRegistry.gangIdForEntity(entityId);
                loaded.put(itemId, new HeadTargetMappingRegistry.Mapping(entityId, gangId));
            } catch (RuntimeException error) {
                JetSetCraft.LOGGER.error("Ignoring invalid JetSetCraft head target mapping {}", entry.getKey(), error);
            }
        });
        HeadTargetMappingRegistry.replaceFromData(loaded);
        JetSetCraft.LOGGER.info("Loaded {} exact JetSetCraft head/emblem target mapping(s)", loaded.size());
    }

    private static ResourceLocation requiredId(JsonObject json, String key) {
        ResourceLocation id = ResourceLocation.tryParse(GsonHelper.getAsString(json, key));
        if (id == null) throw new IllegalArgumentException("Invalid resource location for " + key);
        return id;
    }
}
