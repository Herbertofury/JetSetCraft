package com.herberto.jetsetcraft.client.rush;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.herberto.jetsetcraft.JetSetCraft;
import net.minecraftforge.fml.loading.FMLPaths;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

/** Local presentation preferences; never sent to or imposed by a server. */
final class RivalRushHudPreferences {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static RivalRushHudPreferences instance;
    float x = 0.015F;
    float y = 0.02F;
    float scale = 1.0F;
    boolean visible = true;

    static RivalRushHudPreferences get() {
        if (instance == null) {
            instance = new RivalRushHudPreferences();
            Path file = path();
            if (Files.isRegularFile(file)) {
                try {
                    RivalRushHudPreferences loaded = GSON.fromJson(Files.readString(file), RivalRushHudPreferences.class);
                    if (loaded != null) instance = loaded;
                } catch (IOException | RuntimeException error) {
                    JetSetCraft.LOGGER.warn("Could not read Rival Rush HUD preferences; using defaults", error);
                }
            }
            instance.sanitize();
        }
        return instance;
    }

    void reset() { x = 0.015F; y = 0.02F; scale = 1.0F; visible = true; }
    void sanitize() {
        x = clamp(x, 0, 1, 0.015F);
        y = clamp(y, 0, 1, 0.02F);
        scale = clamp(scale, 0.65F, 1.5F, 1.0F);
    }
    private static float clamp(float value, float min, float max, float fallback) {
        return Float.isFinite(value) ? Math.max(min, Math.min(max, value)) : fallback;
    }
    void save() {
        sanitize();
        try {
            Files.createDirectories(path().getParent());
            Files.writeString(path(), GSON.toJson(this));
        } catch (IOException | RuntimeException error) {
            JetSetCraft.LOGGER.warn("Could not save Rival Rush HUD preferences", error);
        }
    }
    private static Path path() { return FMLPaths.CONFIGDIR.get().resolve("jetsetcraft-rush-client.json"); }
}
