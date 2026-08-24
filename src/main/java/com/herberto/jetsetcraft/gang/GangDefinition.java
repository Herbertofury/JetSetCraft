package com.herberto.jetsetcraft.gang;

import net.minecraft.resources.ResourceLocation;

/** Immutable stable identity/presentation metadata for one gang. Runtime member state lives on source-owned mobs. */
public record GangDefinition(
        ResourceLocation id,
        String canonicalName,
        Disposition disposition,
        ResourceLocation musicId,
        int primaryColor,
        int secondaryColor,
        int minActors,
        int maxActors,
        boolean boomboxEligible,
        boolean legendary
) {
    public enum Disposition {
        FRIENDLY,
        NEUTRAL,
        HOSTILE
    }

    public GangDefinition {
        if (id == null) throw new IllegalArgumentException("Gang id is required");
        canonicalName = canonicalName == null || canonicalName.isBlank() ? humanize(id.getPath()) : canonicalName;
        disposition = disposition == null ? Disposition.NEUTRAL : disposition;
        musicId = musicId == null ? ResourceLocation.fromNamespaceAndPath(id.getNamespace(), "music/gangs/" + id.getPath()) : musicId;
        minActors = Math.max(1, minActors);
        maxActors = Math.max(minActors, maxActors);
    }

    private static String humanize(String path) {
        String[] parts = path.replace('/', '_').split("_");
        StringBuilder out = new StringBuilder();
        for (String part : parts) {
            if (part.isBlank()) continue;
            if (!out.isEmpty()) out.append(' ');
            out.append(Character.toUpperCase(part.charAt(0))).append(part.substring(1));
        }
        return out.isEmpty() ? "Unknown Crew" : out.toString();
    }
}
