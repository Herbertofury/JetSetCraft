package com.herberto.jetsetcraft.client.music;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.herberto.jetsetcraft.JetSetCraft;
import net.minecraft.client.sounds.SoundManager;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.SimpleJsonResourceReloadListener;
import net.minecraft.util.GsonHelper;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RegisterClientReloadListenersEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/** Client resource-pack catalogue. A metadata entry never implies that its recording is installed. */
@Mod.EventBusSubscriber(modid = JetSetCraft.MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public final class JetSetMusicCatalog extends SimpleJsonResourceReloadListener {
    public record Track(ResourceLocation id, String title, String artist, ResourceLocation event,
                        ResourceLocation audio, Set<String> contexts, float volume) {
        public boolean available(ResourceManager resources, SoundManager sounds) {
            var definition = sounds.getSoundEvent(event);
            return resources.getResource(audio).isPresent() && definition != null && definition.getWeight() > 0;
        }
    }

    private static volatile List<Track> tracks = List.of();
    private static volatile long generation;

    public JetSetMusicCatalog() { super(new Gson(), "jetsetcraft_music"); }
    public static List<Track> tracks() { return tracks; }
    public static long generation() { return generation; }

    @SubscribeEvent public static void register(RegisterClientReloadListenersEvent event) {
        event.registerReloadListener(new JetSetMusicCatalog());
    }

    @Override protected void apply(Map<ResourceLocation, JsonElement> entries, ResourceManager resources,
                                   ProfilerFiller profiler) {
        List<Track> loaded = new ArrayList<>();
        entries.entrySet().stream().sorted(Map.Entry.comparingByKey()).limit(256).forEach(entry -> {
            try {
                JsonObject json = GsonHelper.convertToJsonObject(entry.getValue(), "music track");
                ResourceLocation event = resource(json, "sound_event");
                ResourceLocation audio = resource(json, "audio_resource");
                if (!audio.getPath().startsWith("sounds/") || !audio.getPath().endsWith(".ogg"))
                    throw new IllegalArgumentException("audio_resource must name a local sounds/*.ogg resource");
                Set<String> contexts = new LinkedHashSet<>();
                for (JsonElement context : GsonHelper.getAsJsonArray(json, "contexts")) {
                    String value = context.getAsString();
                    if (!Set.of("home", "rush").contains(value)) throw new IllegalArgumentException("unknown music context");
                    contexts.add(value);
                }
                if (contexts.isEmpty()) throw new IllegalArgumentException("at least one context is required");
                float volume = GsonHelper.getAsFloat(json, "volume", 0.65F);
                if (!Float.isFinite(volume) || volume <= 0 || volume > 1) throw new IllegalArgumentException("volume must be in (0,1]");
                loaded.add(new Track(entry.getKey(), label(json, "title"), label(json, "artist"), event,
                        audio, Set.copyOf(contexts), volume));
            } catch (RuntimeException error) {
                JetSetCraft.LOGGER.warn("Ignoring invalid JetSetCraft music metadata {}: {}", entry.getKey(), error.getMessage());
            }
        });
        tracks = List.copyOf(loaded);
        generation++;
        JetSetCraft.LOGGER.info("Loaded {} optional JetSetCraft soundtrack metadata entries; recordings are checked separately", tracks.size());
    }

    private static ResourceLocation resource(JsonObject json, String key) {
        String value = GsonHelper.getAsString(json, key);
        ResourceLocation id = value.length() <= 256 ? ResourceLocation.tryParse(value) : null;
        if (id == null || id.getPath().contains("..")) throw new IllegalArgumentException("invalid " + key);
        return id;
    }

    private static String label(JsonObject json, String key) {
        String value = GsonHelper.getAsString(json, key).replaceAll("[\\p{Cntrl}§]", "").trim();
        if (value.isEmpty() || value.length() > 160) throw new IllegalArgumentException("invalid " + key);
        return value;
    }
}
