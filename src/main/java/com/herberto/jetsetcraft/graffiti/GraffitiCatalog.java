package com.herberto.jetsetcraft.graffiti;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.herberto.jetsetcraft.JetSetCraft;
import net.minecraft.resources.ResourceLocation;

import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

/** One source of truth for bundled graffiti, preserving native aspect ratio. */
public final class GraffitiCatalog {
    public record Entry(String id, ResourceLocation texture, int pixelWidth, int pixelHeight) {
        public float aspectRatio() { return pixelHeight <= 0 ? 1.0f : pixelWidth / (float) pixelHeight; }
        public float renderWidth() {
            float a = Math.max(0.20f, aspectRatio());
            return a >= 1.0f ? Math.min(2.55f, 1.25f * a) : 1.25f * a;
        }
        public float renderHeight() {
            float a = Math.max(0.20f, aspectRatio());
            return a >= 1.0f ? renderWidth() / a : 1.25f;
        }
    }
    private static final List<Entry> ENTRIES = load();
    private static List<Entry> load() {
        String path = "/assets/" + JetSetCraft.MOD_ID + "/graffiti/catalog.json";
        try (var stream = GraffitiCatalog.class.getResourceAsStream(path)) {
            if (stream == null) throw new IllegalStateException("Missing " + path);
            JsonObject root = JsonParser.parseReader(new InputStreamReader(stream, StandardCharsets.UTF_8)).getAsJsonObject();
            JsonArray entries = root.getAsJsonArray("entries");
            List<Entry> out = new ArrayList<>(entries.size());
            for (var element : entries) {
                JsonObject e = element.getAsJsonObject();
                out.add(new Entry(e.get("id").getAsString(), new ResourceLocation(e.get("texture").getAsString()),
                        e.get("width").getAsInt(), e.get("height").getAsInt()));
            }
            if (out.isEmpty()) throw new IllegalStateException("Graffiti catalog is empty");
            return List.copyOf(out);
        } catch (Exception ex) {
            JetSetCraft.LOGGER.error("Failed to load generated graffiti catalog; using emergency JetSetCraft fallback", ex);
            return List.of(new Entry("jetsetcraft", new ResourceLocation(JetSetCraft.MOD_ID, "textures/graffiti/tag_3.png"), 512, 320));
        }
    }
    public static int size() { return ENTRIES.size(); }
    public static Entry get(int index) { return ENTRIES.get(Math.floorMod(index, ENTRIES.size())); }
    public static ResourceLocation texture(int index) { return get(index).texture(); }
    public static String id(int index) { return get(index).id(); }
    private GraffitiCatalog() { }
}
