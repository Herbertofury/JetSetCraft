package com.herberto.jetsetcraft.client.liberty;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.herberto.jetsetcraft.JetSetCraft;
import net.minecraftforge.fml.loading.FMLPaths;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

/** The player owns this local layout; no server packet changes it. */
final class LibertyHudPreferences {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static LibertyHudPreferences instance;
    float x = 0.015F, y = 0.02F, scale = 1.0F;
    boolean visible = true;
    static LibertyHudPreferences get() {
        if (instance == null) {
            instance = new LibertyHudPreferences();
            if (Files.isRegularFile(path())) try {
                LibertyHudPreferences saved = GSON.fromJson(Files.readString(path()), LibertyHudPreferences.class);
                if (saved != null) instance = saved;
            } catch (IOException | RuntimeException error) {
                JetSetCraft.LOGGER.warn("Could not read Liberty Grind HUD layout; using defaults", error);
            }
            instance.sanitize();
        }
        return instance;
    }
    void reset() { x = .015F; y = .02F; scale = 1; visible = true; }
    void sanitize() { x = clamp(x, 0, 1, .015F); y = clamp(y, 0, 1, .02F); scale = clamp(scale, .65F, 1.5F, 1); }
    private static float clamp(float value, float min, float max, float fallback) {
        return Float.isFinite(value) ? Math.max(min, Math.min(max, value)) : fallback;
    }
    void save() {
        sanitize();
        try { Files.createDirectories(path().getParent()); Files.writeString(path(), GSON.toJson(this)); }
        catch (IOException | RuntimeException error) { JetSetCraft.LOGGER.warn("Could not save Liberty Grind HUD layout", error); }
    }
    private static Path path() { return FMLPaths.CONFIGDIR.get().resolve("jetsetcraft-liberty-client.json"); }
}
