package com.herberto.jetsetcraft.client.rush;

import com.herberto.jetsetcraft.JetSetCraft;
import com.herberto.jetsetcraft.network.JetSetNetwork;
import com.herberto.jetsetcraft.rush.RushRun;
import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.ClientPlayerNetworkEvent;
import net.minecraftforge.client.event.RegisterKeyMappingsEvent;
import net.minecraftforge.client.event.RenderGuiOverlayEvent;
import net.minecraftforge.client.gui.overlay.VanillaGuiOverlay;
import net.minecraftforge.client.settings.KeyConflictContext;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.lwjgl.glfw.GLFW;

import java.util.Locale;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/** Client presentation only: no local XP, reward, damage or elapsed-time authority. */
public final class RivalRushClient {
    public static final KeyMapping MENU = new KeyMapping("key.jetsetcraft.rush_menu", KeyConflictContext.IN_GAME,
            InputConstants.Type.KEYSYM, GLFW.GLFW_KEY_UNKNOWN, "key.categories.jetsetcraft");
    static final int HUD_WIDTH = 194;
    static final int HUD_HEIGHT = 98;
    private static CompoundTag state = new CompoundTag();
    private static ListTag album = new ListTag();
    private static ListTag roster = new ListTag();
    private static ListTag arenaCatalogue = new ListTag();
    private static CompoundTag debrief = new CompoundTag();
    private static List<String> buildResources = List.of();
    private static int albumPage, rosterPage;
    private static int albumPages = 1, rosterPages = 1;

    public static void accept(CompoundTag incoming) {
        Minecraft minecraft = Minecraft.getInstance();
        CompoundTag previous = state;
        state = incoming.copy();
        state.put("Offers", boundedList(state.getList("Offers", Tag.TAG_COMPOUND), 3));
        state.put("Feedback", boundedList(state.getList("Feedback", Tag.TAG_COMPOUND), 4));
        if (state.contains("PitStop", Tag.TAG_COMPOUND)) {
            CompoundTag stop = state.getCompound("PitStop");
            stop.put("Options", boundedList(stop.getList("Options", Tag.TAG_COMPOUND), 4));
            stop.put("TuneChoices", boundedList(stop.getList("TuneChoices", Tag.TAG_COMPOUND), 14));
        }
        if (state.contains("Draft", Tag.TAG_COMPOUND)) {
            CompoundTag draft = state.getCompound("Draft");
            // Rewind replenishes the two-use budget; lifetime exclusions can span the bounded upgrade pool.
            draft.put("Banished", boundedList(draft.getList("Banished", Tag.TAG_COMPOUND), RushRun.Upgrade.values().length));
        }
        if (state.contains("Build", Tag.TAG_COMPOUND)) {
            CompoundTag build = state.getCompound("Build");
            build.put("Artifacts", boundedList(build.getList("Artifacts", Tag.TAG_COMPOUND), 3));
            CompoundTag paths = build.getCompound("Paths");
            ListTag recipes = boundedList(paths.getList("Recipes", Tag.TAG_COMPOUND), 6);
            build.putInt("TempoTicks", Math.max(0, Math.min(160, build.getInt("TempoTicks"))));
            build.putInt("AftershockTicks", Math.max(0, Math.min(60, build.getInt("AftershockTicks"))));
            build.putInt("AftershockRemaining", Math.max(0, Math.min(3, build.getInt("AftershockRemaining"))));
            build.putLong("AftershockPulses", Math.max(0L, build.getLong("AftershockPulses")));
            for (var raw : recipes) {
                CompoundTag recipe = (CompoundTag) raw;
                recipe.put("Requirements", boundedList(recipe.getList("Requirements", Tag.TAG_COMPOUND), 2));
            }
            paths.put("Recipes", recipes);
        }
        if (state.contains("Debrief", Tag.TAG_COMPOUND)) {
            CompoundTag saved = state.getCompound("Debrief");
            debrief = saved.getInt("Schema") == 1 && saved.hasUUID("Run") ? saved.copy() : new CompoundTag();
            if (!debrief.isEmpty()) {
                debrief.put("Artifacts", boundedList(debrief.getList("Artifacts", Tag.TAG_COMPOUND), 3));
                debrief.put("Stats", boundedList(debrief.getList("Stats", Tag.TAG_COMPOUND), 11));
            }
        }
        buildResources = buildResourceLines();
        if (state.contains("Caches", Tag.TAG_COMPOUND)) {
            CompoundTag caches = state.getCompound("Caches");
            caches.put("Caches", boundedList(caches.getList("Caches", Tag.TAG_COMPOUND), 6));
        }
        if (state.contains("Journey", Tag.TAG_COMPOUND)) {
            CompoundTag journey = state.getCompound("Journey");
            journey.put("History", boundedList(journey.getList("History", Tag.TAG_COMPOUND), 6));
            journey.put("Choices", boundedList(journey.getList("Choices", Tag.TAG_COMPOUND), 3));
        }
        if (incoming.contains("Album", Tag.TAG_LIST)) {
            album = boundedPage(incoming.getList("Album", Tag.TAG_COMPOUND));
            albumPage = Math.max(0, incoming.getInt("Page"));
            albumPages = Math.max(1, incoming.getInt("TotalPages"));
        }
        if (incoming.contains("Roster", Tag.TAG_LIST)) {
            roster = boundedPage(incoming.getList("Roster", Tag.TAG_COMPOUND));
            rosterPage = Math.max(0, incoming.getInt("Page"));
            rosterPages = Math.max(1, incoming.getInt("TotalPages"));
        }
        if (incoming.contains("ArenaCatalogue", Tag.TAG_LIST)) {
            arenaCatalogue = new ListTag();
            ListTag source = incoming.getList("ArenaCatalogue", Tag.TAG_COMPOUND);
            for (int i = 0; i < Math.min(32, source.size()); i++) arenaCatalogue.add(source.getCompound(i).copy());
        }
        if (minecraft.player == null) return;
        RushFeedback.accept(state);
        if (minecraft.screen instanceof RivalRushScreen screen) {
            screen.onSnapshot(incoming, previous);
        } else if (incoming.getBoolean("Open")) {
            minecraft.setScreen(new RivalRushScreen(incoming, endedRun(incoming, previous)));
        }
    }

    private static ListTag boundedPage(ListTag source) {
        return boundedList(source, 20);
    }

    private static ListTag boundedList(ListTag source, int limit) {
        ListTag result = new ListTag();
        for (int i = 0; i < Math.min(limit, source.size()); i++) result.add(source.getCompound(i).copy());
        return result;
    }

    static CompoundTag state() { return state; }
    static ListTag album() { return album; }
    static ListTag roster() { return roster; }
    static ListTag arenaCatalogue() { return arenaCatalogue; }
    static CompoundTag debrief() { return debrief; }
    static CompoundTag journey() { return state.getCompound("Journey"); }
    static CompoundTag contract() { return journey().getCompound("Contract"); }
    static CompoundTag pitStop() { return state.getCompound("PitStop"); }
    static boolean choosingRoute() { return state.getBoolean("Active") && "choosing".equals(journey().getString("Phase")); }
    public static CompoundTag stateSnapshot() { return state.copy(); }
    public static boolean isJourneyScreenOpen() {
        return Minecraft.getInstance().screen instanceof RivalRushScreen screen && screen.isRouteView();
    }
    public static boolean isJourneyMapScreenOpen() {
        return Minecraft.getInstance().screen instanceof RivalRushScreen screen && screen.isJourneyView();
    }
    public static boolean isBuildScreenOpen() {
        return Minecraft.getInstance().screen instanceof RivalRushScreen screen && screen.isBuildView();
    }
    public static boolean isCacheScreenOpen() {
        return Minecraft.getInstance().screen instanceof RivalRushScreen screen && screen.isCachesView();
    }
    public static boolean isBuildPathsScreenOpen() {
        return Minecraft.getInstance().screen instanceof RivalRushScreen screen && screen.isPathsView();
    }
    public static boolean isUpgradeScreenOpen() {
        return Minecraft.getInstance().screen instanceof RivalRushScreen screen && screen.isUpgradeView();
    }
    public static boolean isDebriefScreenOpen() {
        return Minecraft.getInstance().screen instanceof RivalRushScreen screen && screen.isDebriefView();
    }
    public static boolean isContractScreenOpen() {
        return Minecraft.getInstance().screen instanceof RivalRushScreen screen && screen.isContractView();
    }
    public static boolean isPitStopScreenOpen() {
        return Minecraft.getInstance().screen instanceof RivalRushScreen screen && screen.isPitStopView();
    }
    public static boolean isServiceResultScreenOpen() {
        return Minecraft.getInstance().screen instanceof RivalRushScreen screen && screen.isServiceResultView();
    }
    static int page(boolean cards) { return cards ? albumPage : rosterPage; }
    static int pages(boolean cards) { return cards ? albumPages : rosterPages; }
    static ListTag offers() { return state.getList("Offers", Tag.TAG_COMPOUND); }
    static UUID runId() { return state.hasUUID("Run") ? state.getUUID("Run") : null; }

    static boolean endedRun(CompoundTag incoming, CompoundTag previous) {
        CompoundTag result = incoming.getCompound("Debrief");
        return previous.getBoolean("Active") && !incoming.getBoolean("Active")
                && previous.hasUUID("Run") && result.getInt("Schema") == 1 && result.hasUUID("Run")
                && previous.getUUID("Run").equals(result.getUUID("Run"));
    }

    /** Presentation gating for optional music; callers do not receive mutable run NBT. */
    public static boolean active() {
        Minecraft minecraft = Minecraft.getInstance();
        return state.getBoolean("Active") && minecraft.player != null && minecraft.level != null
                && minecraft.getConnection() != null
                && "jetsetcraft:rival_rush".equals(minecraft.level.dimension().location().toString());
    }

    static void request(String action, String value, int number) {
        if (Minecraft.getInstance().getConnection() == null) return;
        JetSetNetwork.sendRushAction(action, value, number, runId(), state.getInt("Version"));
    }

    public static void requestJourney(String profileId) {
        if (Minecraft.getInstance().getConnection() == null) return;
        JetSetNetwork.sendRushAction("journey_choose", profileId, 0, runId(), journey().getInt("Version"));
    }

    static String clock(long ticks) {
        long seconds = Math.max(0L, ticks) / 20L;
        return String.format(Locale.ROOT, "%d:%02d", seconds / 60L, seconds % 60L);
    }

    static String clipped(String value, int pixels) {
        var font = Minecraft.getInstance().font;
        return font.width(value) <= pixels ? value : font.plainSubstrByWidth(value, Math.max(0, pixels - font.width("…"))) + "…";
    }

    private static List<String> buildResourceLines() {
        CompoundTag build = state.getCompound("Build");
        List<String> lines = new ArrayList<>(3);
        for (var raw : build.getList("Artifacts", Tag.TAG_COMPOUND)) {
            CompoundTag artifact = (CompoundTag) raw;
            switch (artifact.getString("Id")) {
                case "WARD" -> lines.add("Ward " + (int) Math.ceil(finite(build.getDouble("Ward")))
                        + "/" + (int) Math.ceil(finite(build.getDouble("WardMax"))));
                case "SOUL_BURST" -> lines.add("Souls " + Math.max(0, build.getInt("SoulBurstCharge"))
                        + "/" + Math.max(1, build.getInt("SoulBurstChargeMax")));
                case "BATTERY" -> lines.add("Line charge " + Math.max(0, build.getInt("LineCharge"))
                        + "/" + Math.max(1, build.getInt("LineChargeMax")));
                case "AIR_BREAK" -> lines.add(build.getInt("AftershockRemaining") > 0
                        ? "Bass drop: " + build.getInt("AftershockRemaining") + " pulses left"
                        : build.getBoolean("AirborneReady") ? "Landing pulse ready" : "Landing pulse: build airtime");
                default -> { }
            }
        }
        CompoundTag paths = build.getCompound("Paths");
        String selected = paths.getString("Selected");
        if (!selected.isBlank() && !selected.equals("NONE")) {
            for (var raw : paths.getList("Recipes", Tag.TAG_COMPOUND)) {
                CompoundTag recipe = (CompoundTag) raw;
                if (selected.equals(recipe.getString("Id"))) {
                    lines.add(recipe.getString("Name") + (paths.getBoolean("Active") ? " · active" : " · dormant"));
                    break;
                }
            }
        }
        return List.copyOf(lines.subList(0, Math.min(3, lines.size())));
    }

    private static boolean threatVisible(boolean preview) {
        CompoundTag encounter = state.getCompound("Encounter");
        return preview || encounter.getBoolean("Active") && "active".equals(encounter.getString("State"));
    }

    private static List<String> hudResources(boolean preview) {
        return preview && buildResources.isEmpty()
                ? List.of("Artifact charge and ward", "Your active evolution") : buildResources;
    }

    private static boolean contractVisible(boolean preview) {
        String phase = journey().getString("Phase"), type = contract().getString("Type");
        return preview || state.getBoolean("Active") && (phase.equals("arena") || phase.equals("travel"))
                && !type.isBlank() && !type.equals("none");
    }

    private static String requiredObjective() {
        CompoundTag journey = journey();
        String required = journey.getString("Objective"), prefix = journey.getString("CurrentName") + ": ";
        return required.startsWith(prefix) ? required.substring(prefix.length()) : required;
    }

    static int hudHeight(boolean preview) {
        return HUD_HEIGHT + (threatVisible(preview) ? 47 : 0)
                + (contractVisible(preview) ? 24 : 0)
                + (hudResources(preview).isEmpty() ? 0 : hudResources(preview).size() * 11 + 4);
    }

    static float hudScale(int screenWidth, int screenHeight, boolean preview) {
        return Math.max(0.1F, Math.min(RivalRushHudPreferences.get().scale,
                Math.min(Math.max(1, screenWidth - 4) / (float) HUD_WIDTH,
                        Math.max(1, screenHeight - 4) / (float) hudHeight(preview))));
    }

    static void drawHud(GuiGraphics gui, int screenWidth, int screenHeight, boolean preview) {
        if (!preview && (!state.getBoolean("Active") || !RivalRushHudPreferences.get().visible)) return;
        var preferences = RivalRushHudPreferences.get();
        float scale = hudScale(screenWidth, screenHeight, preview);
        int panelHeight = hudHeight(preview);
        int x = Math.round(preferences.x * Math.max(0, screenWidth - Math.round(HUD_WIDTH * scale)));
        int y = Math.round(preferences.y * Math.max(0, screenHeight - Math.round(panelHeight * scale)));
        var font = Minecraft.getInstance().font;
        gui.pose().pushPose();
        gui.pose().translate(x, y, 0);
        gui.pose().scale(scale, scale, 1.0F);
        gui.fill(0, 0, HUD_WIDTH, panelHeight, 0xB0181C20);
        gui.fill(0, 0, 2, panelHeight, 0xFFC5ED55);
        gui.drawString(font, "RIVAL RUSH", 7, 6, 0xFFC5ED55, true);
        boolean active = state.getBoolean("Active");
        boolean circuit = active && state.contains("Journey", Tag.TAG_COMPOUND);
        CompoundTag journey = journey();
        String timer = circuit ? switch (journey.getString("Phase")) {
            case "choosing" -> "Choose";
            case "travel" -> Math.max(0, (int) Math.ceil(journey.getDouble("Distance"))) + "m";
            case "complete" -> "Set done";
            default -> clock(journey.getLong("TicksLeft"));
        } : active ? (state.getBoolean("Endless") ? "+ " + clock(state.getLong("Ticks"))
                : clock(Math.max(0L, 36_000L - state.getLong("Ticks")))) : "HUD preview";
        gui.drawString(font, timer, HUD_WIDTH - 7 - font.width(timer), 6, 0xFFFFFFFF, true);
        String gang = circuit ? switch (journey.getString("Phase")) {
            case "choosing" -> "Arena " + journey.getInt("Stage") + "/6 • choose a reward";
            case "complete" -> "Set " + journey.getInt("Set") + " • six arenas cleared";
            default -> "Arena " + journey.getInt("Stage") + "/" + Math.max(1, journey.getInt("StagesPerSet")) + " • " + journey.getString("CurrentName");
        } : active ? state.getString("Gang") : "Your rival crew";
        gui.drawString(font, clipped(gang, HUD_WIDTH - 14), 7, 20, 0xFFE7E7E7, true);
        long xp = Math.max(0L, state.getLong("Xp"));
        long next = Math.max(1L, state.getLong("NextXp"));
        String xpText = active ? "Rush XP  " + xp + " / " + next + "  •  Lv " + Math.max(1, state.getInt("Level"))
                : "Rush XP — separate from Minecraft";
        gui.drawString(font, clipped(xpText, HUD_WIDTH - 14), 7, 33, 0xFF87E1EA, true);
        bar(gui, 7, 45, HUD_WIDTH - 14, ratio(xp, next), 0xFF62CFDC);
        double vitality = finite(state.getDouble("Vitality"));
        double maxVitality = Math.max(1.0D, finite(state.getDouble("MaxVitality")));
        String health = active ? "Vitality " + (int) Math.ceil(vitality) + " / " + (int) Math.ceil(maxVitality)
                : "Run vitality";
        gui.drawString(font, health, 7, 53, 0xFFF3C1CC, true);
        bar(gui, 7, 65, HUD_WIDTH - 14, ratio(vitality, maxVitality), 0xFFE9869C);
        String progress = active ? "Score " + Math.max(0L, state.getLong("Score")) + "  •  Cards "
                + Math.max(0L, state.getLong("Cards")) : "Score and card progress";
        gui.drawString(font, clipped(progress, HUD_WIDTH - 14), 7, 73, 0xFFE7E7E7, true);
        String objective = !active ? "Objective / upgrade reminder" : !offers().isEmpty() ? "Choose an upgrade — open Rush menu"
                : choosingRoute() && pitStop().getBoolean("Available") ? "Pit stop: " + Math.max(0, pitStop().getInt("Credits")) + " services · Rush menu"
                : choosingRoute() ? "Choose the next arena — open Rush menu"
                : state.getBoolean("Finale") ? "Set complete — extract or play Encore" : circuit ? requiredObjective() : state.getString("Objective");
        gui.drawString(font, clipped(objective, HUD_WIDTH - 14), 7, 86, 0xFFE6DBA6, true);
        int detailY = HUD_HEIGHT;
        if (threatVisible(preview)) {
            CompoundTag encounter = state.getCompound("Encounter");
            boolean actual = encounter.getBoolean("Active") && "active".equals(encounter.getString("State"));
            gui.fill(7, detailY, HUD_WIDTH - 7, detailY + 1, 0xFF67584D);
            String healthText = actual ? (int) Math.ceil(finite(encounter.getDouble("Health"))) + "/"
                    + (int) Math.ceil(finite(encounter.getDouble("MaxHealth"))) : "HP";
            int healthWidth = font.width(healthText);
            gui.drawString(font, clipped(actual ? encounter.getString("Name") : "Encounter preview",
                    HUD_WIDTH - 25 - healthWidth), 7, detailY + 4, 0xFFF1BB79, true);
            gui.drawString(font, healthText, HUD_WIDTH - 7 - healthWidth, detailY + 4, 0xFFF1BB79, true);
            bar(gui, 7, detailY + 16, HUD_WIDTH - 14, actual ? ratio(encounter.getDouble("Health"),
                    encounter.getDouble("MaxHealth")) : 1, 0xFFEAA16D);
            String attack = actual ? encounter.getString("Attack") : "Attack · phase · time";
            int phase = Math.max(0, Math.min(3, encounter.getInt("Phase")));
            String tell = actual ? (phase > 0 ? "Phase " + phase + " · " : "")
                    + (attack.isBlank() ? "Watch for the next tell" : attack) : attack;
            int ticks = Math.max(0, encounter.getInt("TicksUntilImpact"));
            String countdown = actual && !attack.isBlank() && ticks > 0
                    ? String.format(Locale.ROOT, "%.1fs", ticks / 20.0D) : "";
            gui.drawString(font, clipped(tell, HUD_WIDTH - 21 - font.width(countdown)), 7, detailY + 23, 0xFFF1BB79, true);
            gui.drawString(font, countdown, HUD_WIDTH - 7 - font.width(countdown), detailY + 23, 0xFFFFFFFF, true);
            String counterplay = actual ? encounter.getString("Counterplay") : "Read the marked ground";
            gui.drawString(font, clipped(counterplay, HUD_WIDTH - 14), 7, detailY + 35, 0xFFFFFFFF, true);
            detailY += 47;
        }
        if (contractVisible(preview)) {
            CompoundTag contract = contract();
            boolean actual = active && !contract.isEmpty();
            String contractTitle = actual ? "Optional · " + contract.getString("Name") : "Optional room contract";
            gui.drawString(font, clipped(contractTitle, HUD_WIDTH - 14), 7, detailY + 1, 0xFFD7B7FF, true);
            String hint = !actual ? "Progress and next target" : switch (contract.getString("State")) {
                case "complete" -> "Complete · +1 service on room clear";
                case "disabled" -> "Unavailable · open Room contract";
                case "missed" -> "Unfinished · room clear still counts";
                case "pending" -> journey.getString("Phase").equals("travel") ? "Starts on arrival"
                        : contract.getString("Reason").isBlank() ? "Waiting for the next beacon" : contract.getString("Reason");
                default -> Math.max(0, contract.getInt("Progress")) + "/" + Math.max(1, contract.getInt("Target"))
                        + (contract.contains("Distance") ? " · " + contract.getString("Direction") + " "
                        + Math.max(0, contract.getInt("Distance")) + "m" : " · optional bonus");
            };
            gui.drawString(font, clipped(hint, HUD_WIDTH - 14), 7, detailY + 12, 0xFFDDDDDD, true);
            detailY += 24;
        }
        for (String resource : hudResources(preview)) {
            gui.drawString(font, clipped(resource, HUD_WIDTH - 14), 7, detailY + 2, 0xFF87E1EA, true);
            detailY += 11;
        }
        gui.pose().popPose();
    }

    private static double finite(double value) { return Double.isFinite(value) ? Math.max(0.0D, value) : 0.0D; }
    private static float ratio(double current, double maximum) {
        return (float) Math.max(0.0D, Math.min(1.0D, finite(current) / Math.max(1.0D, finite(maximum))));
    }
    private static void bar(GuiGraphics gui, int x, int y, int width, float fraction, int color) {
        gui.fill(x, y, x + width, y + 4, 0xFF333940);
        gui.fill(x, y, x + Math.round(width * fraction), y + 4, color);
    }

    @Mod.EventBusSubscriber(modid = JetSetCraft.MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
    public static final class ModEvents {
        @SubscribeEvent
        public static void keys(RegisterKeyMappingsEvent event) { event.register(MENU); }
    }

    @Mod.EventBusSubscriber(modid = JetSetCraft.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE, value = Dist.CLIENT)
    public static final class ForgeEvents {
        @SubscribeEvent
        public static void tick(TickEvent.ClientTickEvent event) {
            if (event.phase != TickEvent.Phase.END) return;
            Minecraft minecraft = Minecraft.getInstance();
            RushFeedback.tick();
            while (MENU.consumeClick()) {
                if (minecraft.player != null && minecraft.screen == null) request("menu", "", 0);
            }
        }

        @SubscribeEvent
        public static void overlay(RenderGuiOverlayEvent.Post event) {
            Minecraft minecraft = Minecraft.getInstance();
            if (event.getOverlay() != VanillaGuiOverlay.HOTBAR.type() || minecraft.player == null
                    || minecraft.options.hideGui || minecraft.screen instanceof RivalRushScreen
                    || minecraft.screen instanceof RivalRushHudScreen) return;
            drawHud(event.getGuiGraphics(), minecraft.getWindow().getGuiScaledWidth(),
                    minecraft.getWindow().getGuiScaledHeight(), false);
        }

        @SubscribeEvent
        public static void logout(ClientPlayerNetworkEvent.LoggingOut event) {
            state = new CompoundTag();
            album = new ListTag();
            roster = new ListTag();
            arenaCatalogue = new ListTag();
            debrief = new CompoundTag();
            buildResources = List.of();
            RushFeedback.reset();
            albumPage = rosterPage = 0;
            albumPages = rosterPages = 1;
        }
    }

    private RivalRushClient() {}
}
