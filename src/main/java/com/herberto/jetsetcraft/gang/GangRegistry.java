package com.herberto.jetsetcraft.gang;

import com.herberto.jetsetcraft.JetSetCraft;
import net.minecraft.resources.ResourceLocation;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Stable built-in gang identity table. Unknown modded mobs receive deterministic generic IDs rather than guessed names.
 * The table is deliberately registry-ID based so optional mods never become hard classloading dependencies.
 */
public final class GangRegistry {
    private static final Map<ResourceLocation, GangDefinition> BUILTIN_BY_GANG = new LinkedHashMap<>();
    private static final Map<ResourceLocation, ResourceLocation> BUILTIN_ENTITY_TO_GANG = new LinkedHashMap<>();
    private static volatile Map<ResourceLocation, GangDefinition> BY_GANG = Map.of();
    private static volatile Map<ResourceLocation, ResourceLocation> ENTITY_TO_GANG = Map.of();
    private static volatile int dataOverrideCount;

    static {
        // Friendly / passive-start crews.
        crew("minecraft:allay", "blue_notes", "Blue Notes", GangDefinition.Disposition.FRIENDLY, 0x5ED8FF, 0xCCEFFF, 3, 5, true, false);
        crew("minecraft:axolotl", "gillty_pleasure", "Gillty Pleasure", GangDefinition.Disposition.FRIENDLY, 0xFF91C8, 0x8BE7FF, 3, 5, true, false);
        crew("minecraft:bat", "echo_chamber", "Echo Chamber", GangDefinition.Disposition.FRIENDLY, 0x504A65, 0xCABCE8, 3, 6, true, false);
        crew("minecraft:camel", "dune_cruisers", "Dune Cruisers", GangDefinition.Disposition.FRIENDLY, 0xDFA75E, 0xFFE1A3, 2, 4, true, false);
        crew("minecraft:cat", "nine_lives", "Nine Lives", GangDefinition.Disposition.FRIENDLY, 0xFF9A48, 0xFFF2C2, 3, 5, true, false);
        crew("minecraft:chicken", "pecking_order", "The Pecking Order", GangDefinition.Disposition.FRIENDLY, 0xF4F0E8, 0xE63E35, 4, 7, true, false);
        crew("minecraft:cod", "cod_frequency", "Cod Frequency", GangDefinition.Disposition.FRIENDLY, 0xB89D77, 0x86D8DF, 4, 7, true, false);
        crew("minecraft:cow", "milk_run", "The Milk Run", GangDefinition.Disposition.FRIENDLY, 0x5A4032, 0xF5EFE8, 3, 5, true, false);
        crew("minecraft:donkey", "kickback", "Kickback", GangDefinition.Disposition.FRIENDLY, 0x7A6757, 0xB9A08A, 2, 4, true, false);
        crew("minecraft:fox", "fox_trot", "Fox Trot", GangDefinition.Disposition.FRIENDLY, 0xF0782C, 0xFFF0D6, 3, 5, true, false);
        crew("minecraft:frog", "ribbit_riot", "Ribbit Riot", GangDefinition.Disposition.FRIENDLY, 0x6CA64F, 0xE8CF73, 3, 6, true, false);
        crew("minecraft:glow_squid", "neon_ink", "Neon Ink", GangDefinition.Disposition.FRIENDLY, 0x36CBD2, 0x173F5F, 3, 5, true, false);
        crew("minecraft:horse", "bridle_breakers", "Bridle Breakers", GangDefinition.Disposition.FRIENDLY, 0x916E4C, 0xE8D4AF, 2, 4, true, false);
        crew("minecraft:mooshroom", "spore_score", "Spore Score", GangDefinition.Disposition.FRIENDLY, 0xD94343, 0xFFF5DF, 3, 5, true, false);
        crew("minecraft:mule", "pack_attack", "Pack Attack", GangDefinition.Disposition.FRIENDLY, 0x725D50, 0xC8AD8F, 2, 4, true, false);
        crew("minecraft:ocelot", "spot_check", "Spot Check", GangDefinition.Disposition.FRIENDLY, 0xD9A640, 0x312B1F, 3, 5, true, false);
        crew("minecraft:parrot", "repeat_offenders", "Repeat Offenders", GangDefinition.Disposition.FRIENDLY, 0x4EDC7A, 0xF04E5E, 3, 6, true, false);
        crew("minecraft:pig", "hog_wild", "Hog Wild", GangDefinition.Disposition.FRIENDLY, 0xF5A0A8, 0x9E424D, 3, 6, true, false);
        crew("minecraft:rabbit", "hare_trigger", "Hare Trigger", GangDefinition.Disposition.FRIENDLY, 0xB69A7A, 0xF1E7D8, 4, 7, true, false);
        crew("minecraft:salmon", "upstream", "Upstream", GangDefinition.Disposition.FRIENDLY, 0xDE7258, 0x7EC8D4, 4, 7, true, false);
        crew("minecraft:sheep", "fleece_fleet", "Fleece Fleet", GangDefinition.Disposition.FRIENDLY, 0xEFEFEF, 0x84C5F4, 3, 6, true, false);
        crew("minecraft:skeleton_horse", "pale_riders", "Pale Riders", GangDefinition.Disposition.FRIENDLY, 0xDDE3DF, 0x728078, 2, 4, true, false);
        crew("minecraft:sniffer", "throwbacks", "The Throwbacks", GangDefinition.Disposition.FRIENDLY, 0x9B4B41, 0x70A77D, 2, 4, true, false);
        crew("minecraft:squid", "inkognito", "Inkognito", GangDefinition.Disposition.FRIENDLY, 0x2D3E56, 0x788AA3, 3, 5, true, false);
        crew("minecraft:strider", "lava_lanes", "Lava Lanes", GangDefinition.Disposition.FRIENDLY, 0xC84A3E, 0xFFD266, 3, 5, true, false);
        crew("minecraft:tadpole", "small_fry", "Small Fry", GangDefinition.Disposition.FRIENDLY, 0x5D5C50, 0xA8C67B, 4, 7, true, false);
        crew("minecraft:tropical_fish", "reef_riders", "Reef Riders", GangDefinition.Disposition.FRIENDLY, 0x42D9DF, 0xFF8A58, 4, 7, true, false);
        crew("minecraft:turtle", "shell_rollers", "Shell Rollers", GangDefinition.Disposition.FRIENDLY, 0x4E9F6C, 0xC9D96A, 3, 5, true, false);
        crew("minecraft:villager", "block_party", "Block Party", GangDefinition.Disposition.FRIENDLY, 0x8E6643, 0x45A1D9, 3, 6, true, false);
        crew("minecraft:wandering_trader", "roadshow", "The Roadshow", GangDefinition.Disposition.FRIENDLY, 0x4A78A8, 0xE7B75D, 2, 4, true, false);

        // Neutral / conditional-start crews.
        crew("minecraft:bee", "hive_five", "Hive Five", GangDefinition.Disposition.NEUTRAL, 0xF2C94C, 0x2C2C2C, 4, 7, true, false);
        crew("minecraft:dolphin", "wave_riders", "Wave Riders", GangDefinition.Disposition.NEUTRAL, 0x5FAFD6, 0xD6F4FF, 3, 5, true, false);
        crew("minecraft:enderman", "ender_the_influence", "Ender the Influence", GangDefinition.Disposition.NEUTRAL, 0x6A34A8, 0xD34BFF, 3, 5, true, false);
        crew("minecraft:goat", "high_ground", "High Ground", GangDefinition.Disposition.NEUTRAL, 0xD8D2C3, 0x7B6A54, 3, 5, true, false);
        crew("minecraft:iron_golem", "ironclad", "Ironclad", GangDefinition.Disposition.NEUTRAL, 0xB9B8AA, 0x5FA78A, 1, 3, true, false);
        crew("minecraft:llama", "spit_take", "Spit Take", GangDefinition.Disposition.NEUTRAL, 0xC49A70, 0xF3DDC1, 3, 5, true, false);
        crew("minecraft:panda", "bamboo_b_sides", "Bamboo B-Sides", GangDefinition.Disposition.NEUTRAL, 0xECECEC, 0x3C3C3C, 3, 5, true, false);
        crew("minecraft:piglin", "gold_rush", "Gold Rush", GangDefinition.Disposition.NEUTRAL, 0xF0B74A, 0x8A3F49, 3, 6, true, false);
        crew("minecraft:polar_bear", "ice_breakers", "Ice Breakers", GangDefinition.Disposition.NEUTRAL, 0xEEF5F6, 0x7ABFD4, 2, 4, true, false);
        crew("minecraft:snow_golem", "cold_front", "Cold Front", GangDefinition.Disposition.NEUTRAL, 0xF4F7F7, 0x54BFE6, 3, 5, true, false);
        crew("minecraft:spider", "arachnaphobia", "Arachnaphobia", GangDefinition.Disposition.NEUTRAL, 0x2E2028, 0xE83D70, 4, 7, true, false);
        crew("minecraft:trader_llama", "caravan_crew", "Caravan Crew", GangDefinition.Disposition.NEUTRAL, 0xB18962, 0x4A83B8, 3, 5, true, false);
        crew("minecraft:wolf", "pack_mentality", "Pack Mentality", GangDefinition.Disposition.NEUTRAL, 0x8B8B83, 0xDCE4E7, 3, 6, true, false);
        crew("minecraft:zombified_piglin", "dead_mint", "Dead Mint", GangDefinition.Disposition.NEUTRAL, 0x7AA65A, 0xE5B94F, 3, 6, true, false);

        // Hostile-start crews.
        crew("minecraft:blaze", "burnout_brigade", "Burnout Brigade", GangDefinition.Disposition.HOSTILE, 0xF59D2A, 0xFFE66A, 3, 5, true, false);
        crew("minecraft:cave_spider", "underweb", "Underweb", GangDefinition.Disposition.HOSTILE, 0x205D61, 0xC13B69, 4, 7, true, false);
        crew("minecraft:creeper", "creepaku_gouji", "Creepaku Gouji", GangDefinition.Disposition.HOSTILE, 0x57C84D, 0x141414, 3, 6, true, false);
        crew("minecraft:drowned", "dead_water", "Dead Water", GangDefinition.Disposition.HOSTILE, 0x3C7F78, 0x7ED1CC, 3, 6, true, false);
        crew("minecraft:elder_guardian", "ancient_current", "Ancient Current", GangDefinition.Disposition.HOSTILE, 0xB9AD81, 0x76668C, 1, 2, false, true);
        crew("minecraft:endermite", "static_noise", "Static Noise", GangDefinition.Disposition.HOSTILE, 0x77588C, 0xBBA0CF, 4, 7, true, false);
        crew("minecraft:evoker", "conjure_club", "Conjure Club", GangDefinition.Disposition.HOSTILE, 0x76636B, 0xD8C5E1, 2, 4, true, false);
        crew("minecraft:ghast", "wail_riders", "Wail Riders", GangDefinition.Disposition.HOSTILE, 0xE9E1DD, 0xA26F72, 2, 4, true, false);
        crew("minecraft:guardian", "current_affairs", "Current Affairs", GangDefinition.Disposition.HOSTILE, 0x5AAE9B, 0xE29B6F, 3, 5, true, false);
        crew("minecraft:hoglin", "razorbacks", "Razorbacks", GangDefinition.Disposition.HOSTILE, 0xB46A5A, 0x40282C, 3, 5, true, false);
        crew("minecraft:husk", "dry_spell", "Dry Spell", GangDefinition.Disposition.HOSTILE, 0xBFA86B, 0x6C6339, 3, 6, true, false);
        crew("minecraft:magma_cube", "hot_bounce", "Hot Bounce", GangDefinition.Disposition.HOSTILE, 0xE44934, 0xF7B845, 3, 6, true, false);
        crew("minecraft:phantom", "night_shift", "Night Shift", GangDefinition.Disposition.HOSTILE, 0x334A74, 0x8BC0D9, 3, 5, true, false);
        crew("minecraft:piglin_brute", "gold_standard", "Gold Standard", GangDefinition.Disposition.HOSTILE, 0xDAB04D, 0x5D343B, 2, 4, true, false);
        crew("minecraft:pillager", "raid_parade", "Raid Parade", GangDefinition.Disposition.HOSTILE, 0x657176, 0xA95555, 3, 6, true, false);
        crew("minecraft:pufferfish", "puff_piece", "Puff Piece", GangDefinition.Disposition.HOSTILE, 0xE1B958, 0x7D8B5B, 3, 6, true, false);
        crew("minecraft:ravager", "wrecking_crew", "Wrecking Crew", GangDefinition.Disposition.HOSTILE, 0x6B6260, 0xB64D46, 1, 3, true, false);
        crew("minecraft:shulker", "boxed_in", "Boxed In", GangDefinition.Disposition.HOSTILE, 0x9C6E9F, 0xD7B9D8, 3, 5, true, false);
        crew("minecraft:silverfish", "silver_static", "Silver Static", GangDefinition.Disposition.HOSTILE, 0x8E9493, 0xD3D7D6, 4, 7, true, false);
        crew("minecraft:skeleton", "bone_drones", "The Bone Drones", GangDefinition.Disposition.HOSTILE, 0xD9D5C7, 0x5A5860, 3, 6, true, false);
        crew("minecraft:slime", "goo_groove", "Goo Groove", GangDefinition.Disposition.HOSTILE, 0x6BCB5B, 0xB8F28E, 3, 6, true, false);
        crew("minecraft:stray", "cold_shots", "Cold Shots", GangDefinition.Disposition.HOSTILE, 0x93B8C5, 0xE0F3F7, 3, 6, true, false);
        crew("minecraft:vex", "bad_spirits", "Bad Spirits", GangDefinition.Disposition.HOSTILE, 0xA7B5C8, 0xE2E6EE, 3, 6, true, false);
        crew("minecraft:vindicator", "axe_to_grind", "Axe to Grind", GangDefinition.Disposition.HOSTILE, 0x6B7070, 0x4A342B, 3, 5, true, false);
        crew("minecraft:warden", "deep_cuts", "Deep Cuts", GangDefinition.Disposition.HOSTILE, 0x123F48, 0x45D7C7, 1, 1, false, true);
        crew("minecraft:witch", "hex_appeal", "Hex Appeal", GangDefinition.Disposition.HOSTILE, 0x52356A, 0xB8D65A, 2, 5, true, false);
        crew("minecraft:wither_skeleton", "blackout_bones", "Blackout Bones", GangDefinition.Disposition.HOSTILE, 0x252525, 0x727272, 3, 6, true, false);
        crew("minecraft:zoglin", "rotten_rush", "Rotten Rush", GangDefinition.Disposition.HOSTILE, 0x87675B, 0xB66361, 3, 5, true, false);
        crew("minecraft:zombie", "dead_beat", "Dead Beat", GangDefinition.Disposition.HOSTILE, 0x4F8A53, 0x5C68B1, 3, 6, true, false);
        crew("minecraft:zombie_villager", "dead_locals", "Dead Locals", GangDefinition.Disposition.HOSTILE, 0x688A55, 0x69506E, 3, 6, true, false);

        // Legendary/technical identities remain discoverable but are not random Boombox picks.
        crew("minecraft:ender_dragon", "final_flight", "Final Flight", GangDefinition.Disposition.HOSTILE, 0x281C37, 0xBD70FF, 1, 1, false, true);
        crew("minecraft:wither", "triple_threat", "Triple Threat", GangDefinition.Disposition.HOSTILE, 0x292929, 0xCECECE, 1, 1, false, true);
        crew("minecraft:zombie_horse", "night_mares", "Night Mares", GangDefinition.Disposition.NEUTRAL, 0x5B7562, 0x252A2A, 1, 2, false, true);
        crew("minecraft:illusioner", "smoke_and_mirrors", "Smoke & Mirrors", GangDefinition.Disposition.HOSTILE, 0x647080, 0xB8C7D8, 1, 2, false, true);
        crew("minecraft:giant", "dead_beat_titan", "Dead Beat Titan", GangDefinition.Disposition.HOSTILE, 0x4F8A53, 0x262F6A, 1, 1, false, true);
        replaceDataOverrides(Map.of(), Map.of());
    }

    public static ResourceLocation gangIdForEntity(ResourceLocation entityId) {
        if (entityId == null) return id("jetsetcraft:unknown");
        ResourceLocation curated = ENTITY_TO_GANG.get(entityId);
        if (curated != null) return curated;
        return id(JetSetCraft.MOD_ID + ":mob/" + entityId.getNamespace() + "/" + entityId.getPath());
    }

    public static GangDefinition definitionForEntity(ResourceLocation entityId) {
        ResourceLocation gangId = gangIdForEntity(entityId);
        GangDefinition curated = BY_GANG.get(gangId);
        if (curated != null) return curated;
        String sourceName = entityId == null ? "Unknown" : humanize(entityId.getPath());
        return new GangDefinition(gangId, sourceName + " Crew", GangDefinition.Disposition.NEUTRAL,
                id(JetSetCraft.MOD_ID + ":music/gangs/generic"), 0x5EE8E8, 0xE938A8, 2, 4, true, false);
    }

    public static Optional<GangDefinition> byId(ResourceLocation gangId) {
        if (gangId == null) return Optional.empty();
        GangDefinition curated = BY_GANG.get(gangId);
        if (curated != null) return Optional.of(curated);
        if (JetSetCraft.MOD_ID.equals(gangId.getNamespace()) && gangId.getPath().startsWith("mob/")) {
            return Optional.of(new GangDefinition(gangId, humanize(gangId.getPath().substring(4)) + " Crew",
                    GangDefinition.Disposition.NEUTRAL, id(JetSetCraft.MOD_ID + ":music/gangs/generic"),
                    0x5EE8E8, 0xE938A8, 2, 4, true, false));
        }
        return Optional.empty();
    }

    /** Stable built-in definition before datapack/server overlays are applied. */
    public static Optional<GangDefinition> builtInById(ResourceLocation gangId) {
        return gangId == null ? Optional.empty() : Optional.ofNullable(BUILTIN_BY_GANG.get(gangId));
    }

    public static int dataOverrideCount() {
        return dataOverrideCount;
    }

    /**
     * Atomically replaces the current datapack/server overlays while preserving all built-in fallback identities.
     * Entity mappings may point at a newly defined gang or override the built-in gang for an installed source mob.
     */
    public static synchronized void replaceDataOverrides(Map<ResourceLocation, GangDefinition> definitions,
                                                         Map<ResourceLocation, ResourceLocation> entityMappings) {
        LinkedHashMap<ResourceLocation, GangDefinition> mergedDefinitions = new LinkedHashMap<>(BUILTIN_BY_GANG);
        if (definitions != null) {
            definitions.entrySet().stream().sorted(Map.Entry.comparingByKey())
                    .forEach(entry -> mergedDefinitions.put(entry.getKey(), entry.getValue()));
        }
        LinkedHashMap<ResourceLocation, ResourceLocation> mergedEntities = new LinkedHashMap<>(BUILTIN_ENTITY_TO_GANG);
        if (entityMappings != null) {
            entityMappings.entrySet().stream().sorted(Map.Entry.comparingByKey()).forEach(entry -> {
                if (mergedDefinitions.containsKey(entry.getValue())) mergedEntities.put(entry.getKey(), entry.getValue());
            });
        }
        BY_GANG = Map.copyOf(mergedDefinitions);
        ENTITY_TO_GANG = Map.copyOf(mergedEntities);
        dataOverrideCount = definitions == null ? 0 : definitions.size();
    }

    public static Optional<ResourceLocation> representativeEntity(ResourceLocation gangId) {
        return ENTITY_TO_GANG.entrySet().stream().filter(entry -> entry.getValue().equals(gangId))
                .map(Map.Entry::getKey).findFirst();
    }

    public static Collection<GangDefinition> curated() {
        return List.copyOf(BY_GANG.values());
    }

    public static Collection<ResourceLocation> curatedEntityIds() {
        return List.copyOf(ENTITY_TO_GANG.keySet());
    }

    private static void crew(String entity, String path, String name, GangDefinition.Disposition disposition,
                             int primary, int secondary, int minActors, int maxActors,
                             boolean boomboxEligible, boolean legendary) {
        ResourceLocation entityId = id(entity);
        ResourceLocation gangId = id(JetSetCraft.MOD_ID + ":" + path);
        ResourceLocation music = id(JetSetCraft.MOD_ID + ":music/gangs/" + path);
        GangDefinition definition = new GangDefinition(gangId, name, disposition, music, primary, secondary,
                minActors, maxActors, boomboxEligible, legendary);
        BUILTIN_ENTITY_TO_GANG.put(entityId, gangId);
        BUILTIN_BY_GANG.putIfAbsent(gangId, definition);
    }

    private static ResourceLocation id(String value) {
        ResourceLocation id = ResourceLocation.tryParse(value);
        if (id == null) throw new IllegalArgumentException("Invalid JetSetCraft gang resource location: " + value);
        return id;
    }

    private static String humanize(String path) {
        String[] parts = path.replace('/', '_').split("_");
        StringBuilder out = new StringBuilder();
        for (String part : parts) {
            if (part.isBlank()) continue;
            if (!out.isEmpty()) out.append(' ');
            out.append(Character.toUpperCase(part.charAt(0))).append(part.substring(1));
        }
        return out.toString();
    }

    private GangRegistry() {}
}
