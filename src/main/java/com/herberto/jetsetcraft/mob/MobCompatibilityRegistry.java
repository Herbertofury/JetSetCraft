package com.herberto.jetsetcraft.mob;

import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

/**
 * Atomic, reload-time compatibility snapshot. The hot entity paths perform map lookups only.
 */
public final class MobCompatibilityRegistry {
    public enum ProviderState {
        ABSENT,
        READY,
        VERSION_DRIFT,
        ROSTER_DRIFT
    }

    public record ProviderStatus(
            String modId,
            String expectedVersion,
            String installedVersion,
            int expectedSafeCount,
            int resolvedSafeCount,
            int hiddenCount,
            List<ResourceLocation> missingKnownIds,
            ProviderState state
    ) {}

    private static volatile Map<ResourceLocation, MobCompatibilityProfile> BY_ENTITY = Map.of();
    private static volatile Set<ResourceLocation> HIDDEN = Set.of();
    private static volatile Map<String, ProviderStatus> PROVIDERS = Map.of();

    public static Optional<MobCompatibilityProfile> profile(ResourceLocation entityId) {
        return entityId == null ? Optional.empty() : Optional.ofNullable(BY_ENTITY.get(entityId));
    }

    public static MobRideRig broadRig(ResourceLocation entityId) {
        return profile(entityId).map(MobCompatibilityProfile::broadRig).orElse(MobRideRig.GENERIC);
    }

    public static boolean hidden(ResourceLocation entityId) {
        return entityId != null && HIDDEN.contains(entityId);
    }

    public static boolean boomboxAllowed(ResourceLocation entityId) {
        if (hidden(entityId)) return false;
        return profile(entityId).map(profile -> !profile.bossGated() && !profile.ownerGated()).orElse(true);
    }

    public static Map<String, ProviderStatus> providerStatuses() {
        return PROVIDERS;
    }

    public static synchronized void replaceProfiles(Map<ResourceLocation, MobCompatibilityProfile> profiles,
                                                    Set<ResourceLocation> hidden,
                                                    Map<String, String> expectedVersions) {
        LinkedHashMap<ResourceLocation, MobCompatibilityProfile> nextProfiles = new LinkedHashMap<>();
        if (profiles != null) profiles.entrySet().stream().sorted(Map.Entry.comparingByKey())
                .forEach(entry -> nextProfiles.put(entry.getKey(), entry.getValue()));

        LinkedHashSet<ResourceLocation> nextHidden = new LinkedHashSet<>();
        if (hidden != null) hidden.stream().sorted().forEach(nextHidden::add);

        LinkedHashMap<String, ProviderStatus> statuses = new LinkedHashMap<>();
        LinkedHashSet<String> providers = new LinkedHashSet<>();
        nextProfiles.keySet().forEach(id -> providers.add(id.getNamespace()));
        nextHidden.forEach(id -> providers.add(id.getNamespace()));

        for (String provider : providers) {
            String expected = expectedVersions == null ? "" : expectedVersions.getOrDefault(provider, "");
            boolean loaded = ModList.get().isLoaded(provider);
            String installed = ModList.get().getModContainerById(provider)
                    .map(container -> container.getModInfo().getVersion().toString()).orElse("");
            List<ResourceLocation> known = nextProfiles.keySet().stream()
                    .filter(id -> provider.equals(id.getNamespace())).toList();
            List<ResourceLocation> missing = new ArrayList<>();
            int resolved = 0;
            if (loaded) {
                for (ResourceLocation id : known) {
                    if (ForgeRegistries.ENTITY_TYPES.containsKey(id)) resolved++;
                    else missing.add(id);
                }
            } else {
                missing.addAll(known);
            }
            int hiddenCount = (int) nextHidden.stream().filter(id -> provider.equals(id.getNamespace())).count();
            ProviderState state;
            if (!loaded) state = ProviderState.ABSENT;
            else if (!missing.isEmpty()) state = ProviderState.ROSTER_DRIFT;
            else if (!expected.isBlank() && !installed.isBlank() && !expected.equals(installed)) state = ProviderState.VERSION_DRIFT;
            else state = ProviderState.READY;
            statuses.put(provider, new ProviderStatus(provider, expected, installed, known.size(), resolved,
                    hiddenCount, List.copyOf(missing), state));
        }

        BY_ENTITY = Map.copyOf(nextProfiles);
        HIDDEN = Set.copyOf(nextHidden);
        PROVIDERS = Map.copyOf(statuses);
    }

    private MobCompatibilityRegistry() {}
}
