package com.herberto.jetsetcraft.gang.data;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.herberto.jetsetcraft.JetSetCraft;
import com.herberto.jetsetcraft.gang.GangDefinition;
import com.herberto.jetsetcraft.gang.GangRegistry;
import com.herberto.jetsetcraft.mob.MobCompatibilityProfile;
import com.herberto.jetsetcraft.mob.MobCompatibilityRegistry;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.SimpleJsonResourceReloadListener;
import net.minecraft.util.GsonHelper;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

/**
 * Loads data/<namespace>/jetsetcraft_gangs/*.json as overlays over the stable built-in gang atlas.
 * Missing fields inherit built-in values, so a server can rename/recolor a gang without copying the whole definition.
 *
 * A resource may contain one legacy gang definition or an "entries" array for a provider bundle. Bundles keep
 * large optional-mod curation atomic and preserve rich compatibility metadata without hard-linking provider classes.
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
        LinkedHashMap<ResourceLocation, MobCompatibilityProfile> profiles = new LinkedHashMap<>();
        LinkedHashSet<ResourceLocation> hiddenEntities = new LinkedHashSet<>();
        LinkedHashMap<String, String> expectedVersions = new LinkedHashMap<>();

        resources.entrySet().stream().sorted(Map.Entry.comparingByKey()).forEach(entry -> {
            try {
                JsonObject json = GsonHelper.convertToJsonObject(entry.getValue(), "JetSetCraft gang definition");
                if (json.has("entries")) {
                    loadBundle(entry.getKey(), json, definitions, entityMappings, profiles, hiddenEntities,
                            expectedVersions);
                } else {
                    loadDefinition(entry.getKey(), json, false, definitions, entityMappings, profiles);
                }
            } catch (RuntimeException error) {
                JetSetCraft.LOGGER.error("Ignoring invalid JetSetCraft gang definition {}", entry.getKey(), error);
            }
        });

        GangRegistry.replaceDataOverrides(definitions, entityMappings);
        MobCompatibilityRegistry.replaceProfiles(profiles, hiddenEntities, expectedVersions);
        JetSetCraft.LOGGER.info("Loaded {} JetSetCraft gang override(s), {} installed entity mapping(s), {} compatibility profile(s), and {} hidden technical entity id(s)",
                definitions.size(), entityMappings.size(), profiles.size(), hiddenEntities.size());
        MobCompatibilityRegistry.providerStatuses().values().stream()
                .sorted(java.util.Comparator.comparing(MobCompatibilityRegistry.ProviderStatus::modId))
                .forEach(status -> {
                    if (status.state() == MobCompatibilityRegistry.ProviderState.ROSTER_DRIFT) {
                        JetSetCraft.LOGGER.warn("JetSetCraft compatibility roster drift for {} {}: resolved {}/{} known safe ids; missing {}",
                                status.modId(), status.installedVersion(), status.resolvedSafeCount(),
                                status.expectedSafeCount(), status.missingKnownIds());
                    } else if (status.state() == MobCompatibilityRegistry.ProviderState.VERSION_DRIFT) {
                        JetSetCraft.LOGGER.warn("JetSetCraft compatibility version drift for {}: curated {}, installed {}; known roster still resolves {}/{}",
                                status.modId(), status.expectedVersion(), status.installedVersion(),
                                status.resolvedSafeCount(), status.expectedSafeCount());
                    } else {
                        JetSetCraft.LOGGER.info("JetSetCraft compatibility {} -> {} ({}/{} known safe ids, {} hidden technical ids)",
                                status.modId(), status.state(), status.resolvedSafeCount(),
                                status.expectedSafeCount(), status.hiddenCount());
                    }
                });
    }

    private static void loadBundle(ResourceLocation resourceId, JsonObject root,
                                   Map<ResourceLocation, GangDefinition> definitions,
                                   Map<ResourceLocation, ResourceLocation> entityMappings,
                                   Map<ResourceLocation, MobCompatibilityProfile> profiles,
                                   Set<ResourceLocation> hiddenEntities,
                                   Map<String, String> expectedVersions) {
        String provider = GsonHelper.getAsString(root, "provider_mod_id", "").trim();
        String version = GsonHelper.getAsString(root, "provider_version", "").trim();
        if (!provider.isEmpty()) expectedVersions.put(provider, version);

        if (root.has("hidden_entities")) {
            for (JsonElement element : GsonHelper.getAsJsonArray(root, "hidden_entities")) {
                ResourceLocation id = ResourceLocation.tryParse(element.getAsString());
                if (id == null) throw new IllegalArgumentException("Invalid hidden entity id " + element);
                hiddenEntities.add(id);
            }
        }

        JsonArray entries = GsonHelper.getAsJsonArray(root, "entries");
        int index = 0;
        for (JsonElement element : entries) {
            JsonObject json = GsonHelper.convertToJsonObject(element, "JetSetCraft bundled gang definition");
            ResourceLocation syntheticId = ResourceLocation.fromNamespaceAndPath(resourceId.getNamespace(),
                    resourceId.getPath() + "/" + index++);
            loadDefinition(syntheticId, json, true, definitions, entityMappings, profiles);
        }
    }

    private static void loadDefinition(ResourceLocation fallbackId, JsonObject json, boolean requireGangId,
                                       Map<ResourceLocation, GangDefinition> definitions,
                                       Map<ResourceLocation, ResourceLocation> entityMappings,
                                       Map<ResourceLocation, MobCompatibilityProfile> profiles) {
        if (requireGangId && !json.has("gang_id")) {
            throw new IllegalArgumentException("Bundled gang entry must declare gang_id");
        }
        ResourceLocation gangId = json.has("gang_id") ? requiredId(json, "gang_id") : fallbackId;
        GangDefinition base = GangRegistry.builtInById(gangId).orElse(null);
        String name = GsonHelper.getAsString(json, "display_name",
                base == null ? humanize(gangId.getPath()) : base.canonicalName());
        GangDefinition.Disposition disposition = parseDisposition(GsonHelper.getAsString(json, "disposition",
                base == null ? "neutral" : base.disposition().name()));
        ResourceLocation music = json.has("music") ? requiredId(json, "music")
                : base == null ? ResourceLocation.fromNamespaceAndPath(JetSetCraft.MOD_ID, "music/gangs/generic") : base.musicId();
        int primary = parseColor(json.get("primary_color"), base == null ? 0x5EE8E8 : base.primaryColor());
        int secondary = parseColor(json.get("secondary_color"), base == null ? 0xE938A8 : base.secondaryColor());
        int minActors = GsonHelper.getAsInt(json, "min_actors", base == null ? 2 : base.minActors());
        int maxActors = GsonHelper.getAsInt(json, "max_actors", base == null ? 4 : base.maxActors());
        boolean eligible = GsonHelper.getAsBoolean(json, "boombox_eligible", base == null || base.boomboxEligible());
        boolean legendary = GsonHelper.getAsBoolean(json, "legendary", base != null && base.legendary());
        definitions.put(gangId, new GangDefinition(gangId, name, disposition, music, primary, secondary,
                minActors, maxActors, eligible, legendary));

        ResourceLocation family = json.has("crew_family_id") ? requiredId(json, "crew_family_id") : null;
        String rideProfile = GsonHelper.getAsString(json, "ride_profile", "generic");
        String challengeProfile = GsonHelper.getAsString(json, "challenge_profile", "default");
        String graffitiMotif = GsonHelper.getAsString(json, "graffiti_motif", "");

        for (ResourceLocation entityId : entityIds(json)) {
            addEntity(entityId, gangId, entityMappings);
            MobCompatibilityProfile profile = new MobCompatibilityProfile(entityId, gangId, family, rideProfile,
                    challengeProfile, graffitiMotif);
            MobCompatibilityProfile previous = profiles.put(entityId, profile);
            if (previous != null && !previous.equals(profile)) {
                throw new IllegalArgumentException("Conflicting compatibility profile for " + entityId);
            }
        }
    }

    private static Set<ResourceLocation> entityIds(JsonObject json) {
        LinkedHashSet<ResourceLocation> ids = new LinkedHashSet<>();
        if (json.has("entity")) ids.add(requiredId(json, "entity"));
        if (json.has("entities")) {
            for (JsonElement element : GsonHelper.getAsJsonArray(json, "entities")) {
                ResourceLocation id = ResourceLocation.tryParse(element.getAsString());
                if (id == null) throw new IllegalArgumentException("Invalid entity mapping " + element);
                ids.add(id);
            }
        }
        return ids;
    }

    private static void addEntity(ResourceLocation entityId, ResourceLocation gangId,
                                  Map<ResourceLocation, ResourceLocation> entityMappings) {
        if (ForgeRegistries.ENTITY_TYPES.containsKey(entityId)) {
            ResourceLocation previous = entityMappings.put(entityId, gangId);
            if (previous != null && !previous.equals(gangId)) {
                throw new IllegalArgumentException("Conflicting gang mapping for " + entityId + ": " + previous + " vs " + gangId);
            }
        } else {
            JetSetCraft.LOGGER.debug("Skipping gang entity mapping {} -> {} because the entity is not installed", entityId, gangId);
        }
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
        try { return Integer.parseInt(value, 16) & 0xFFFFFF;
        } catch (NumberFormatException error) { throw new IllegalArgumentException("Invalid RGB color " + value, error); }
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
