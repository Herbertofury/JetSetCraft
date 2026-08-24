package com.herberto.jetsetcraft.gang.data;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.herberto.jetsetcraft.JetSetCraft;
import com.herberto.jetsetcraft.gang.GangDefinition;
import com.herberto.jetsetcraft.gang.GangRegistry;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.SimpleJsonResourceReloadListener;
import net.minecraft.util.GsonHelper;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Loads data/<namespace>/jetsetcraft_gangs/*.json as overlays over the stable built-in gang atlas.
 * Missing fields inherit built-in values, so a server can rename/recolor a gang without copying the whole definition.
 */
public final class GangDefinitionReloadListener extends SimpleJsonResourceReloadListener {
    private static final Gson GSON = new GsonBuilder().setLenient().create();

    public GangDefinitionReloadListener() {
        super(GSON, "jetsetcraft_gangs");
    }

    @Override
    protected void apply(Map<ResourceLocation, JsonElement> resources, ResourceManager manager, ProfilerFiller profiler) {
        LinkedHashMap<ResourceLocation, GangDefinition> definitions = new LinkedHashMap<>();
        LinkedHashMap<ResourceLocation, ResourceLocation> entityMappings = new LinkedHashMap<>();
        resources.entrySet().stream().sorted(Map.Entry.comparingByKey()).forEach(entry -> {
            try {
                JsonObject json = GsonHelper.convertToJsonObject(entry.getValue(), "JetSetCraft gang definition");
                ResourceLocation gangId = json.has("gang_id") ? requiredId(json, "gang_id") : entry.getKey();
                GangDefinition base = GangRegistry.builtInById(gangId).orElse(null);
                String name = GsonHelper.getAsString(json, "display_name",
                        base == null ? humanize(gangId.getPath()) : base.canonicalName());
                GangDefinition.Disposition disposition = parseDisposition(GsonHelper.getAsString(json, "disposition",
                        base == null ? "neutral" : base.disposition().name()));
                ResourceLocation music = json.has("music") ? requiredId(json, "music")
                        : base == null ? new ResourceLocation(JetSetCraft.MOD_ID, "music/gangs/generic") : base.musicId();
                int primary = parseColor(json.get("primary_color"), base == null ? 0x5EE8E8 : base.primaryColor());
                int secondary = parseColor(json.get("secondary_color"), base == null ? 0xE938A8 : base.secondaryColor());
                int minActors = GsonHelper.getAsInt(json, "min_actors", base == null ? 2 : base.minActors());
                int maxActors = GsonHelper.getAsInt(json, "max_actors", base == null ? 4 : base.maxActors());
                boolean eligible = GsonHelper.getAsBoolean(json, "boombox_eligible", base == null || base.boomboxEligible());
                boolean legendary = GsonHelper.getAsBoolean(json, "legendary", base != null && base.legendary());
                definitions.put(gangId, new GangDefinition(gangId, name, disposition, music, primary, secondary,
                        minActors, maxActors, eligible, legendary));
                loadEntityMappings(json, gangId, entityMappings);
            } catch (RuntimeException error) {
                JetSetCraft.LOGGER.error("Ignoring invalid JetSetCraft gang definition {}", entry.getKey(), error);
            }
        });
        GangRegistry.replaceDataOverrides(definitions, entityMappings);
        JetSetCraft.LOGGER.info("Loaded {} JetSetCraft gang override(s) with {} explicit entity mapping(s)",
                definitions.size(), entityMappings.size());
    }

    private static void loadEntityMappings(JsonObject json, ResourceLocation gangId,
                                           Map<ResourceLocation, ResourceLocation> entityMappings) {
        if (json.has("entity")) addEntity(requiredId(json, "entity"), gangId, entityMappings);
        if (!json.has("entities")) return;
        JsonArray array = GsonHelper.getAsJsonArray(json, "entities");
        for (JsonElement element : array) {
            ResourceLocation entityId = ResourceLocation.tryParse(element.getAsString());
            if (entityId == null) throw new IllegalArgumentException("Invalid entity mapping " + element);
            addEntity(entityId, gangId, entityMappings);
        }
    }

    private static void addEntity(ResourceLocation entityId, ResourceLocation gangId,
                                  Map<ResourceLocation, ResourceLocation> entityMappings) {
        if (ForgeRegistries.ENTITY_TYPES.containsKey(entityId)) entityMappings.put(entityId, gangId);
        else JetSetCraft.LOGGER.debug("Skipping gang entity mapping {} -> {} because the entity is not installed", entityId, gangId);
    }

    private static GangDefinition.Disposition parseDisposition(String value) {
        try { return GangDefinition.Disposition.valueOf(value.trim().toUpperCase(java.util.Locale.ROOT)); }
        catch (IllegalArgumentException error) { throw new IllegalArgumentException("Invalid disposition " + value, error); }
    }

    private static int parseColor(JsonElement element, int fallback) {
        if (element == null || element.isJsonNull()) return fallback;
        if (element.isJsonPrimitive() && element.getAsJsonPrimitive().isNumber()) return element.getAsInt() & 0xFFFFFF;
        String value = element.getAsString().trim();
        if (value.startsWith("#")) value = value.substring(1);
        try { return Integer.parseInt(value, 16) & 0xFFFFFF; }
        catch (NumberFormatException error) { throw new IllegalArgumentException("Invalid RGB color " + value, error); }
    }

    private static ResourceLocation requiredId(JsonObject json, String key) {
        ResourceLocation id = ResourceLocation.tryParse(GsonHelper.getAsString(json, key));
        if (id == null) throw new IllegalArgumentException("Invalid resource location for " + key);
        return id;
    }

    private static String humanize(String path) {
        StringBuilder out = new StringBuilder();
        for (String part : path.replace('/', '_').split("_")) {
            if (part.isBlank()) continue;
            if (!out.isEmpty()) out.append(' ');
            out.append(Character.toUpperCase(part.charAt(0))).append(part.substring(1));
        }
        return out.isEmpty() ? "Unknown Crew" : out.toString();
    }
}
