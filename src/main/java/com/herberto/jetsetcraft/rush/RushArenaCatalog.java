package com.herberto.jetsetcraft.rush;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.herberto.jetsetcraft.JetSetCraft;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.SimpleJsonResourceReloadListener;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.Property;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;

/** Bounded, server-owned arena recipes. A map contains blocks, never entities, inventories or block-entity NBT. */
public final class RushArenaCatalog extends SimpleJsonResourceReloadListener {
    public enum Reward {
        DAMAGE("Paint damage", RushRun.Upgrade.FORCE),
        CRITICAL("Critical chance and power", RushRun.Upgrade.PRECISION),
        AREA("Ability and paint area", RushRun.Upgrade.AMPLITUDE),
        RELIC("An artifact for your build", null),
        MAGNET("Magnet and pickup reach", RushRun.Upgrade.PICKUP),
        RECOVERY("Vitality and recovery", RushRun.Upgrade.RECOVERY);
        private final String label;
        private final RushRun.Upgrade upgrade;
        Reward(String label, RushRun.Upgrade upgrade) { this.label = label; this.upgrade = upgrade; }
        public String label() { return label; }
        public RushRun.Upgrade upgrade() { return upgrade; }
    }
    public record MapBlock(BlockPos offset, BlockState state) {}
    public record Profile(ResourceLocation id, String name, String description, int radius, int seconds,
                          int defeats, String layout, Reward reward, int rewardRanks, String sharedReward,
                          BlockState floor, BlockState accent, BlockState wall, BlockState rail,
                          List<MapBlock> blocks) {
        public Profile { blocks = List.copyOf(blocks); }
        public CompoundTag summary() {
            CompoundTag tag = new CompoundTag();
            tag.putString("Id", id.toString()); tag.putString("Name", name); tag.putString("Description", description);
            tag.putInt("Radius", radius); tag.putInt("Seconds", seconds);
            tag.putString("RewardLabel", reward.label() + " + " + sharedReward + " crew progress");
            return tag;
        }
    }
    private static final Profile FALLBACK = new Profile(id("jetsetcraft:street_plaza"), "Street plaza",
            "Open skating lanes, a clear circuit and a stronger paint build.", 24, 120, 8, "plaza",
            Reward.DAMAGE, 1, "balanced", Blocks.STONE.defaultBlockState(), Blocks.CYAN_CONCRETE.defaultBlockState(),
            Blocks.LIGHT_GRAY_CONCRETE.defaultBlockState(), Blocks.IRON_BARS.defaultBlockState(), List.of());
    private static Map<ResourceLocation, Profile> profiles = Map.of(FALLBACK.id(), FALLBACK);

    public RushArenaCatalog() { super(new Gson(), "rush_arenas"); }
    public static Optional<Profile> find(ResourceLocation id) { return Optional.ofNullable(profiles.get(id)); }
    public static List<Profile> all() { return profiles.values().stream().sorted(Comparator.comparing(p -> p.id().toString())).toList(); }
    public static ListTag summaries() { ListTag list = new ListTag(); all().forEach(p -> list.add(p.summary())); return list; }

    @Override protected void apply(Map<ResourceLocation, JsonElement> input, ResourceManager manager, ProfilerFiller profiler) {
        Map<ResourceLocation, Profile> loaded = new LinkedHashMap<>();
        input.entrySet().stream().sorted(Map.Entry.comparingByKey()).forEach(entry -> {
            if (loaded.size() >= 32) return;
            try {
                JsonObject json = entry.getValue().getAsJsonObject();
                String provider = text(json, "requires_mod", "", 64);
                if (!provider.isEmpty() && !ModList.get().isLoaded(provider)) return;
                loaded.put(entry.getKey(), parse(entry.getKey(), json));
            } catch (RuntimeException error) {
                JetSetCraft.LOGGER.warn("Ignoring invalid Rush arena {}: {}", entry.getKey(), error.getMessage());
            }
        });
        if (loaded.isEmpty()) loaded.put(FALLBACK.id(), FALLBACK);
        profiles = Map.copyOf(loaded);
        JetSetCraft.LOGGER.info("Loaded {} bounded Rival Rush arena profiles", profiles.size());
    }

    /** Also used by acceptance fixtures; rejects malformed limits rather than silently raising reward authority. */
    public static Profile parse(ResourceLocation id, JsonObject json) {
        int radius = integer(json, "radius", 24, 16, 48);
        int seconds = integer(json, "seconds", 120, 30, 360);
        int defeats = integer(json, "defeats", 8, 3, 80);
        String layout = text(json, "layout", "plaza", 24);
        if (!List.of("plaza", "cross", "loop", "terraces").contains(layout)) throw new IllegalArgumentException("Unknown layout");
        Reward reward = Reward.valueOf(text(json, "reward", "damage", 24).toUpperCase(Locale.ROOT));
        String shared = text(json, "shared_reward", "balanced", 24);
        if (!List.of("balanced", "supplies", "training", "reputation").contains(shared)) throw new IllegalArgumentException("Unknown shared reward");
        JsonObject palette = json.has("palette") ? json.getAsJsonObject("palette") : new JsonObject();
        List<MapBlock> blocks = new ArrayList<>();
        if (json.has("blocks")) {
            if (json.getAsJsonArray("blocks").size() > 4096) throw new IllegalArgumentException("At most 4096 authored map blocks");
            for (JsonElement raw : json.getAsJsonArray("blocks")) {
                JsonObject block = raw.getAsJsonObject();
                int x = integer(block, "x", 0, -radius + 4, radius - 4);
                int z = integer(block, "z", 0, -radius + 4, radius - 4);
                int y = integer(block, "y", 1, 1, 8);
                if (Math.abs(x) <= 4 || Math.abs(z) <= 4) throw new IllegalArgumentException("Keep the central crossing and entrance lanes clear");
                if (block.has("nbt") || block.has("entity") || block.has("items")) throw new IllegalArgumentException("Arena maps never carry entity or inventory data");
                BlockState state = blockState(block.get("block"), Blocks.STONE.defaultBlockState());
                if (block.has("properties")) for (Map.Entry<String, JsonElement> property : block.getAsJsonObject("properties").entrySet())
                    state = property(state, property.getKey(), property.getValue().getAsString());
                blocks.add(new MapBlock(new BlockPos(x, y, z), state));
            }
        }
        return new Profile(id, text(json, "name", id.getPath().replace('_', ' '), 64),
                text(json, "description", "A custom skating arena.", 200), radius, seconds, defeats, layout, reward,
                integer(json, "reward_ranks", 1, 1, 2), shared,
                blockState(palette.get("floor"), FALLBACK.floor()), blockState(palette.get("accent"), FALLBACK.accent()),
                blockState(palette.get("wall"), FALLBACK.wall()), blockState(palette.get("rail"), FALLBACK.rail()), blocks);
    }

    private static BlockState blockState(JsonElement raw, BlockState fallback) {
        if (raw == null) return fallback;
        List<JsonElement> candidates = new ArrayList<>();
        if (raw.isJsonArray()) raw.getAsJsonArray().forEach(candidates::add); else candidates.add(raw);
        if (candidates.isEmpty() || candidates.size() > 8) throw new IllegalArgumentException("Palette needs 1–8 block candidates");
        for (JsonElement candidate : candidates) {
            ResourceLocation key = ResourceLocation.tryParse(candidate.getAsString());
            if (key == null) throw new IllegalArgumentException("Invalid block ID");
            if (!ForgeRegistries.BLOCKS.containsKey(key)) continue;
            BlockState state = ForgeRegistries.BLOCKS.getValue(key).defaultBlockState();
            if (!safe(state)) throw new IllegalArgumentException("Unsafe arena material: " + key);
            return state;
        }
        return fallback;
    }
    public static boolean safe(BlockState state) {
        if (state.hasBlockEntity() || !state.getFluidState().isEmpty() || state.isAir()) return false;
        var block = state.getBlock();
        return block != Blocks.TNT && block != Blocks.FIRE && block != Blocks.SOUL_FIRE
                && block != Blocks.NETHER_PORTAL && block != Blocks.END_PORTAL && block != Blocks.END_GATEWAY
                && block != Blocks.CACTUS && block != Blocks.MAGMA_BLOCK && block != Blocks.RESPAWN_ANCHOR
                && block != Blocks.PISTON && block != Blocks.STICKY_PISTON && block != Blocks.MOVING_PISTON;
    }
    private static BlockState property(BlockState state, String name, String value) {
        Property<?> property = state.getBlock().getStateDefinition().getProperty(name);
        if (property == null) throw new IllegalArgumentException("Unknown block property " + name);
        return propertyValue(state, property, value);
    }
    private static <T extends Comparable<T>> BlockState propertyValue(BlockState state, Property<T> property, String value) {
        T parsed = property.getValue(value).orElseThrow(() -> new IllegalArgumentException("Invalid block property value"));
        BlockState result = state.setValue(property, parsed);
        if (!safe(result)) throw new IllegalArgumentException("Unsafe block state");
        return result;
    }
    private static int integer(JsonObject json, String key, int fallback, int low, int high) {
        if (!json.has(key)) return fallback;
        double raw = json.get(key).getAsDouble();
        if (!Double.isFinite(raw) || raw != Math.rint(raw) || raw < low || raw > high) throw new IllegalArgumentException(key + " outside " + low + ".." + high);
        return (int) raw;
    }
    private static String text(JsonObject json, String key, String fallback, int max) {
        String value = json.has(key) ? json.get(key).getAsString().strip() : fallback;
        if (value.length() > max || (value.isEmpty() && !key.equals("requires_mod"))) throw new IllegalArgumentException("Invalid " + key);
        return value;
    }
    private static ResourceLocation id(String text) { return ResourceLocation.tryParse(text); }
}
