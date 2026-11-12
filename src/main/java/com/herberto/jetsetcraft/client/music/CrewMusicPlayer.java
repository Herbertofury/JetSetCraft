package com.herberto.jetsetcraft.client.music;

import com.mojang.brigadier.arguments.IntegerArgumentType;
import net.minecraft.network.chat.ClickEvent;
import net.minecraft.ChatFormatting;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.herberto.jetsetcraft.JetSetCraft;
import com.herberto.jetsetcraft.client.crew.CrewHouseClient;
import com.herberto.jetsetcraft.client.rush.RivalRushClient;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.sounds.AbstractTickableSoundInstance;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RegisterClientCommandsEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.loading.FMLPaths;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.List;

/** Owns only its own optional RECORDS sound. It neither streams websites nor alters other audio channels. */
@Mod.EventBusSubscriber(modid = JetSetCraft.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE, value = Dist.CLIENT)
public final class CrewMusicPlayer {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final RandomSource RANDOM = RandomSource.create();
    private static boolean preferencesLoaded, enabled = true;
    private static long generation = -1;
    private static int cooldown, playingTicks;
    private static CrewTrackSound current;
    private static JetSetMusicCatalog.Track track;
    private static ResourceLocation lastTrack;

    @SubscribeEvent public static void commands(RegisterClientCommandsEvent event) {
        event.getDispatcher().register(Commands.literal("jetsetmusic")
                .executes(context -> status())
                .then(Commands.literal("status").executes(context -> status()))
                .then(Commands.literal("credits").executes(context -> credits(1))
                        .then(Commands.argument("page", IntegerArgumentType.integer(1))
                                .executes(context -> credits(IntegerArgumentType.getInteger(context, "page")))))
                .then(Commands.literal("on").executes(context -> setEnabled(true)))
                .then(Commands.literal("off").executes(context -> setEnabled(false)))
                .then(Commands.literal("next").executes(context -> {
                    if (current != null) current.fadeOut();
                    cooldown = 0;
                    tell("Next available track will start in your house or Rival Rush.");
                    return 1;
                })));
    }

    @SubscribeEvent public static void tick(TickEvent.ClientTickEvent event) {
        if (event.phase != TickEvent.Phase.END) return;
        Minecraft minecraft = Minecraft.getInstance();
        loadPreferences();
        if (minecraft.player == null || minecraft.level == null || minecraft.getConnection() == null) {
            stopImmediately(minecraft);
            lastTrack = null;
            return;
        }
        if (generation != JetSetMusicCatalog.generation()) {
            stopImmediately(minecraft);
            generation = JetSetMusicCatalog.generation();
        }
        if (minecraft.isPaused()) return;
        String context = RivalRushClient.active() ? "rush" : CrewHouseClient.inHomePlot() ? "home" : "";
        if (current != null) {
            playingTicks++;
            if (!enabled || context.isEmpty() || track == null || !track.contexts().contains(context)) current.fadeOut();
            if (current.isStopped() || (playingTicks > 20 && !minecraft.getSoundManager().isActive(current))) {
                minecraft.getSoundManager().stop(current);
                current = null; track = null; cooldown = 40;
            }
            return;
        }
        if (cooldown > 0) { cooldown--; return; }
        // A missing optional pack stays silent and is checked at most once every five seconds.
        cooldown = 100;
        if (!enabled || context.isEmpty() || minecraft.options.getSoundSourceVolume(SoundSource.RECORDS) <= 0
                || minecraft.options.getSoundSourceVolume(SoundSource.MASTER) <= 0) return;
        List<JetSetMusicCatalog.Track> available = new ArrayList<>(JetSetMusicCatalog.tracks().stream()
                .filter(candidate -> candidate.contexts().contains(context)
                        && candidate.available(minecraft.getResourceManager(), minecraft.getSoundManager())).toList());
        if (available.size() > 1) available.removeIf(candidate -> candidate.id().equals(lastTrack));
        if (available.isEmpty()) return;
        track = available.get(RANDOM.nextInt(available.size()));
        lastTrack = track.id();
        current = new CrewTrackSound(track);
        playingTicks = 0;
        minecraft.getSoundManager().play(current);
        tell("Now playing: " + track.credit());
    }

    private static int credits(int page) {
        Minecraft minecraft = Minecraft.getInstance();
        var catalogue = JetSetMusicCatalog.tracks();
        int pages = Math.max(1, (catalogue.size() + 4) / 5);
        if (page > pages) { tell("Credits page must be between 1 and " + pages + "."); return 0; }
        tell("Soundtrack credits " + page + "/" + pages + " (catalogued recordings, not the whole recommendation library)");
        if (catalogue.isEmpty()) tell("No soundtrack metadata is loaded.");
        catalogue.stream().skip((long) (page - 1) * 5).limit(5).forEach(candidate ->
                tell(candidate.credit() + " [" + (candidate.available(minecraft.getResourceManager(), minecraft.getSoundManager())
                        ? "installed" : "metadata only; recording not installed") + "]"));
        if (minecraft.player != null) minecraft.player.displayClientMessage(
                Component.literal("Music Library: artists, albums, game/gang recommendations and purchase links")
                        .withStyle(style -> style.withColor(ChatFormatting.AQUA).withUnderlined(true)
                                .withClickEvent(new ClickEvent(ClickEvent.Action.OPEN_URL,
                                        "https://github.com/Herbertofury/JetSetCraft/wiki/Music-Library"))), false);
        if (page < pages) tell("Next page: /jetsetmusic credits " + (page + 1));
        return 1;
    }

    private static int status() {
        loadPreferences();
        Minecraft minecraft = Minecraft.getInstance();
        long installed = JetSetMusicCatalog.tracks().stream()
                .filter(candidate -> candidate.available(minecraft.getResourceManager(), minecraft.getSoundManager())).count();
        tell("Soundtrack " + (enabled ? "on" : "off") + ": " + installed + "/" + JetSetMusicCatalog.tracks().size()
                + " recordings installed. Volume: Music & Sounds → Jukebox/Note Blocks.");
        tell("View credits anytime: /jetsetmusic credits [page].");
        if (installed == 0) tell("The catalogue is ready. Enable a resource pack containing your project audio to hear these tracks.");
        return 1;
    }

    private static int setEnabled(boolean value) {
        loadPreferences(); enabled = value; savePreferences(); cooldown = 0;
        if (!enabled && current != null) current.fadeOut();
        tell("House and Rival Rush soundtrack " + (enabled ? "on." : "off."));
        return 1;
    }

    private static void stopImmediately(Minecraft minecraft) {
        if (current != null) minecraft.getSoundManager().stop(current);
        current = null; track = null; playingTicks = 0; cooldown = 0;
    }

    private static Path preferencePath() { return FMLPaths.CONFIGDIR.get().resolve("jetsetcraft-music.json"); }

    private static void loadPreferences() {
        if (preferencesLoaded) return;
        preferencesLoaded = true;
        Path path = preferencePath();
        if (!Files.isRegularFile(path)) return;
        try {
            if (Files.size(path) > 4096) throw new IOException("preference file exceeds 4 KiB");
            JsonObject json = GSON.fromJson(Files.readString(path), JsonObject.class);
            if (json != null && json.has("enabled")) enabled = json.get("enabled").getAsBoolean();
        } catch (IOException | RuntimeException error) {
            JetSetCraft.LOGGER.warn("Could not read soundtrack preference; using default: {}", error.getMessage());
        }
    }

    private static void savePreferences() {
        Path path = preferencePath();
        Path temporary = path.resolveSibling(path.getFileName() + ".tmp");
        try {
            Files.createDirectories(path.getParent());
            JsonObject json = new JsonObject(); json.addProperty("enabled", enabled);
            Files.writeString(temporary, GSON.toJson(json) + "\n");
            Files.move(temporary, path, StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException error) {
            JetSetCraft.LOGGER.warn("Soundtrack preference changed for this session but could not be saved: {}", error.getMessage());
        }
    }

    private static void tell(String text) {
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.player != null) minecraft.player.displayClientMessage(Component.literal(text), false);
    }

    private static final class CrewTrackSound extends AbstractTickableSoundInstance {
        private final float maximum;
        private boolean fading;
        CrewTrackSound(JetSetMusicCatalog.Track track) {
            super(SoundEvent.createVariableRangeEvent(track.event()), SoundSource.RECORDS, RandomSource.create());
            maximum = track.volume();
            volume = 0; pitch = 1; relative = true; attenuation = Attenuation.NONE; looping = false;
        }
        @Override public boolean canStartSilent() { return true; }
        void fadeOut() { fading = true; }
        @Override public void tick() {
            volume = fading ? Math.max(0, volume - maximum / 20) : Math.min(maximum, volume + maximum / 40);
            if (fading && volume <= 0) stop();
        }
    }

    private CrewMusicPlayer() {}
}
