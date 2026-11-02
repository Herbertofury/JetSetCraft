package com.herberto.jetsetcraft.client.rush;

import com.herberto.jetsetcraft.gang.GangRegistry;
import com.herberto.jetsetcraft.network.JetSetNetwork;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.FormattedCharSequence;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/** Native focusable controls for explicit entry, choices, card collection and immediate extraction. */
final class RivalRushScreen extends Screen {
    private enum View { RUN, ADVANCED, ROSTER, ALBUM, CARD, UPGRADES, RESULT, ARENAS, ROUTE, JOURNEY, BUILD, REMIX, CACHES }
    private View view = View.RUN;
    private EditBox gangField, mobField;
    private String gang = "jetsetcraft:dead_beat";
    private String mob = "minecraft:zombie";
    private String draftGang = gang, draftMob = mob;
    private String gangLabel = "Dead Beat", gangLabelId = gang;
    private CompoundTag selectedCard;
    private int window;
    private int responseWindow;
    private int cardScroll;
    private int detailScroll;
    private String selectedJourney = "", remixFrom = "", remixTo = "";
    private String remixFeedback = "";
    private boolean debriefPending;
    private String resultSummary = "";
    private boolean pending;
    private int pendingTicks;
    private boolean circuit;
    private String firstArena = "";
    private Button timedButton, endlessButton, applyButton;

    boolean isAlbumView() { return view == View.ALBUM; }
    boolean isRouteView() { return view == View.ROUTE; }
    boolean isJourneyView() { return view == View.JOURNEY; }
    boolean isBuildView() { return view == View.BUILD; }
    boolean isCachesView() { return view == View.CACHES; }

    RivalRushScreen(CompoundTag opening) {
        super(Component.literal("Rival Rush"));
        if (!opening.getString("GangId").isBlank()) gang = opening.getString("GangId");
        if (!opening.getString("Mob").isBlank()) mob = opening.getString("Mob");
        if (!opening.getString("Gang").isBlank()) { gangLabel = opening.getString("Gang"); gangLabelId = gang; }
        selectView(opening);
    }

    void onSnapshot(CompoundTag incoming, CompoundTag previous) {
        boolean active = incoming.getBoolean("Active");
        boolean hasOffers = !RivalRushClient.offers().isEmpty();
        boolean choosingRoute = RivalRushClient.choosingRoute();
        CompoundTag journey = incoming.getCompound("Journey");
        CompoundTag priorJourney = previous.getCompound("Journey");
        if (active && journey.getInt("Set") == priorJourney.getInt("Set")
                && journey.getInt("Completed") > priorJourney.getInt("Completed")) {
            selectedJourney = "done:" + journey.getInt("Completed");
            detailScroll = 0; debriefPending = true;
        }
        if (active && !previous.getBoolean("Active") && !hasOffers && !incoming.getBoolean("Finale") && !choosingRoute) {
            minecraft.setScreen(null);
            return;
        }
        if (view == View.UPGRADES && active && !hasOffers && !incoming.getBoolean("Finale") && !choosingRoute) {
            minecraft.setScreen(null);
            return;
        }
        if (active && previous.getBoolean("Finale") && !incoming.getBoolean("Finale") && !hasOffers
                && incoming.getString("Summary").isBlank() && !choosingRoute) {
            minecraft.setScreen(null);
            return;
        }
        if ((view == View.ROUTE || view == View.JOURNEY) && active && "choosing".equals(previous.getCompound("Journey").getString("Phase"))
                && !choosingRoute && !hasOffers && !incoming.getBoolean("Finale")) {
            minecraft.setScreen(null);
            return;
        }
        boolean changed = incoming.getBoolean("Open") || active != previous.getBoolean("Active")
                || incoming.getInt("Version") != previous.getInt("Version")
                || incoming.getBoolean("Finale") != previous.getBoolean("Finale")
                || incoming.contains("Album", Tag.TAG_LIST) || incoming.contains("Roster", Tag.TAG_LIST)
                || incoming.contains("ArenaCatalogue", Tag.TAG_LIST)
                || incoming.getCompound("Journey").getInt("Version") != previous.getCompound("Journey").getInt("Version");
        if (changed) {
            rememberFields();
            if (view == View.REMIX && pending && !incoming.getString("Summary").isBlank()) remixFeedback = incoming.getString("Summary");
            if (view == View.REMIX && previous.getCompound("Build").getBoolean("RemixReady")
                    && !incoming.getCompound("Build").getBoolean("RemixReady")) {
                view = View.BUILD; remixFrom = remixTo = ""; detailScroll = 0;
            }
            if (incoming.getBoolean("Open")) selectView(incoming);
            pending = false;
            pendingTicks = 0;
            rebuildWidgets();
        }
    }

    private void selectView(CompoundTag incoming) {
        if (incoming.getBoolean("Active") && !RivalRushClient.offers().isEmpty()) view = View.UPGRADES;
        else if (incoming.getBoolean("Active") && debriefPending) { view = View.JOURNEY; debriefPending = false; }
        else if (RivalRushClient.choosingRoute()) {
            // A requested collection page remains usable while the next route waits for a decision.
            if (incoming.contains("Album", Tag.TAG_LIST)) { view = View.ALBUM; window = responseWindow; responseWindow = 0; }
            else if (incoming.contains("Roster", Tag.TAG_LIST)) { view = View.ROSTER; window = responseWindow; responseWindow = 0; }
            else if (view != View.ALBUM && view != View.CARD && view != View.ROSTER
                    && view != View.JOURNEY && view != View.BUILD && view != View.REMIX) view = View.ROUTE;
        }
        else if (!incoming.getString("Summary").isBlank()) { resultSummary = incoming.getString("Summary"); view = View.RESULT; }
        else if (incoming.getBoolean("Finale")) view = incoming.contains("Journey", Tag.TAG_COMPOUND) ? View.JOURNEY : View.RUN;
        else if (incoming.contains("Album", Tag.TAG_LIST)) { view = View.ALBUM; window = responseWindow; responseWindow = 0; }
        else if (incoming.contains("Roster", Tag.TAG_LIST)) { view = View.ROSTER; window = responseWindow; responseWindow = 0; }
        else if (!incoming.getBoolean("Active") && view != View.ARENAS) view = View.RUN;
        else if (incoming.contains("Journey", Tag.TAG_COMPOUND) && view == View.RUN) view = View.JOURNEY;
    }

    private void rememberFields() {
        if (gangField != null) draftGang = gangField.getValue();
        if (mobField != null) draftMob = mobField.getValue();
    }

    private void navigate(View target) {
        rememberFields();
        if (target == View.ADVANCED && view != View.ADVANCED) { draftGang = gang; draftMob = mob; }
        view = target;
        window = 0;
        if (target == View.CARD) cardScroll = 0;
        detailScroll = 0;
        if (target == View.REMIX) { remixFrom = ""; remixTo = ""; remixFeedback = ""; }
        rebuildWidgets();
    }

    @Override
    protected void init() {
        rememberFields();
        gangField = mobField = null;
        timedButton = endlessButton = applyButton = null;
        int left = left();
        int span = span();
        boolean active = RivalRushClient.state().getBoolean("Active");
        int third = active ? (span - 12) / 4 : (span - 8) / 3;
        boolean journey = active && RivalRushClient.state().contains("Journey", Tag.TAG_COMPOUND);
        addRenderableWidget(Button.builder(Component.literal(journey ? "Journey" : "Run"), button -> navigate(
                !RivalRushClient.offers().isEmpty() ? View.UPGRADES : journey ? View.JOURNEY : View.RUN))
                .bounds(left, 45, third, 20).build());
        addRenderableWidget(Button.builder(Component.literal("Rival crews"), button -> {
            navigate(View.ROSTER);
            RivalRushClient.request("roster", "", 0);
        }).bounds(left + third + 4, 45, third, 20).build());
        addRenderableWidget(Button.builder(Component.literal("Mob cards"), button -> {
            navigate(View.ALBUM);
            RivalRushClient.request("album", "", 0);
        }).bounds(left + (third + 4) * 2, 45, active ? third : span - (third + 4) * 2, 20).build());
        if (active) addRenderableWidget(Button.builder(Component.literal("Build"), button -> navigate(View.BUILD))
                .bounds(left + (third + 4) * 3, 45, span - (third + 4) * 3, 20).build());

        switch (view) {
            case RUN -> buildRun(left, span);
            case ADVANCED -> buildAdvanced(left, span);
            case ROSTER, ALBUM -> buildList(left, span);
            case UPGRADES -> buildOffers(left, span);
            case ARENAS -> buildArenas(left, span);
            case ROUTE -> buildRoute(left, span);
            case JOURNEY -> buildJourney(left, span);
            case BUILD -> buildBuild(left, span);
            case CACHES -> addRenderableWidget(Button.builder(Component.literal("Back to build"), button -> navigate(View.BUILD))
                    .bounds(left, height - 53, span, 20).build());
            case REMIX -> buildRemix(left, span);
            case CARD -> addRenderableWidget(Button.builder(Component.literal("Back to cards"),
                    button -> navigate(View.ALBUM)).bounds(left, height - 53, span, 20).build());
            case RESULT -> addRenderableWidget(Button.builder(Component.literal(RivalRushClient.state().getBoolean("Active")
                            ? "Return to run menu" : "Set up another run"),
                    button -> navigate(RivalRushClient.offers().isEmpty() ? View.RUN : View.UPGRADES))
                    .bounds(left, height - 53, span, 20).build());
        }

        // Reserve enough space for the complete exit label even at Minecraft's 320-pixel GUI width.
        int closeWidth = Math.min(90, (span - 8) / 4);
        addRenderableWidget(Button.builder(Component.translatable("gui.done"), button -> onClose())
                .bounds(left, height - 26, closeWidth, 20).build());
        addRenderableWidget(Button.builder(Component.literal("HUD layout"), button -> {
            rememberFields();
            minecraft.setScreen(new RivalRushHudScreen(this));
        }).bounds(left + closeWidth + 4, height - 26, closeWidth, 20).build());
        Button returnButton = addRenderableWidget(Button.builder(Component.literal(active ? "Return to Minecraft" : "Crew House"), button -> {
            if (active) RivalRushClient.request("leave", "", 0);
            else JetSetNetwork.sendCrewAction("open", "", 0, null, null, 0L);
        }).bounds(left + (closeWidth + 4) * 2, height - 26, span - (closeWidth + 4) * 2, 20).build());
        returnButton.setTooltip(Tooltip.create(Component.literal(active
                ? "End this run and restore your Minecraft state. Collected mode cards stay in the album."
                : "Open your optional crew home, training and supply jobs. Done closes this menu.")));
    }

    private void buildRun(int left, int span) {
        CompoundTag state = RivalRushClient.state();
        if (state.getBoolean("Active")) {
            if (state.getBoolean("Finale")) {
                addRenderableWidget(Button.builder(Component.literal("Continue into Encore"), button -> {
                    pending = true;
                    button.active = false;
                    RivalRushClient.request("encore", "", 0);
                }).bounds(left, Math.min(height - 57, 156), span, 20).build()).active = !pending;
            } else if (!RivalRushClient.offers().isEmpty()) {
                addRenderableWidget(Button.builder(Component.literal("Choose an upgrade"), button -> navigate(View.UPGRADES))
                        .bounds(left, Math.min(height - 57, 156), span, 20).build());
            } else if (state.contains("Journey", Tag.TAG_COMPOUND)) {
                addRenderableWidget(Button.builder(Component.literal(RivalRushClient.choosingRoute()
                                ? "Choose the next arena" : "Journey map and rewards"), button -> navigate(
                                RivalRushClient.choosingRoute() ? View.ROUTE : View.JOURNEY))
                        .bounds(left, Math.min(height - 57, 156), span, 20).build());
            }
            return;
        }
        int crewWidth = Math.max(120, span - 92);
        Button crew = addRenderableWidget(Button.builder(Component.literal(RivalRushClient.clipped(
                crewName() + "  •  Change crew", crewWidth - 12)), button -> {
            navigate(View.ROSTER);
            RivalRushClient.request("roster", "", 0);
        }).bounds(left, 77, crewWidth, 20).build());
        crew.setTooltip(Tooltip.create(Component.literal(crewName() + "\nTheme: " + mobName(mob)
                + "\nChoose a named rival crew and its themed cast.")));
        int half = (span - 4) / 2;
        addRenderableWidget(Button.builder(Component.literal("Advanced"), button -> navigate(View.ADVANCED))
                .bounds(left + crewWidth + 4, 77, span - crewWidth - 4, 20).build())
                .setTooltip(Tooltip.create(Component.literal("Choose a specific crew or creature by registry ID.")));
        addRenderableWidget(Button.builder(Component.literal((circuit ? "" : "• ") + "Free streets"), button -> {
            circuit = false; rebuildWidgets();
        }).bounds(left, 117, half, 20).build());
        addRenderableWidget(Button.builder(Component.literal((circuit ? "• " : "") + "Arena circuit"), button -> {
            circuit = true; rebuildWidgets();
        }).bounds(left + half + 4, 117, span - half - 4, 20).build());
        if (circuit) {
            addRenderableWidget(Button.builder(Component.literal(RivalRushClient.clipped("First arena: " + firstArenaName(), span - 12)), button -> {
                navigate(View.ARENAS); RivalRushClient.request("arena_catalogue", "", 0);
            }).bounds(left, 144, span, 20).build());
        }
        timedButton = addRenderableWidget(Button.builder(Component.literal(circuit ? "Start 6-arena journey" : "Start 30-minute run"), button -> start(false))
                .bounds(left, height - 53, circuit ? span : half, 20).build());
        timedButton.setTooltip(Tooltip.create(Component.literal(circuit
                ? "Six arenas connected by actual riding. Each room shows its duration and reward; choose Return or Encore after the set."
                : "Thirty active minutes, then an explicit Return or Encore choice.")));
        if (!circuit) {
            endlessButton = addRenderableWidget(Button.builder(Component.literal("Start Endless"), button -> start(true))
                    .bounds(left + half + 4, height - 53, span - half - 4, 20).build());
            endlessButton.setTooltip(Tooltip.create(Component.literal("Keep the run going through escalating street sets. Return at any time.")));
        }
        updateStartButtons();
    }

    private String firstArenaName() {
        if (firstArena.isBlank()) return "Choose a route";
        for (var value : RivalRushClient.arenaCatalogue()) {
            if (value instanceof CompoundTag arena && firstArena.equals(arena.getString("Id"))) return arena.getString("Name");
        }
        return "Unavailable preset — choose again";
    }

    private void buildArenas(int left, int span) {
        ListTag catalogue = RivalRushClient.arenaCatalogue();
        int visible = rows();
        int total = catalogue.size() + 1;
        int windows = Math.max(1, (total + visible - 1) / visible);
        window = Math.max(0, Math.min(window, windows - 1));
        for (int i = 0; i < visible && window * visible + i < total; i++) {
            int index = window * visible + i - 1;
            CompoundTag arena = index < 0 ? null : catalogue.getCompound(index);
            String label = arena == null ? "Choose a route • preview rewards" : arena.getString("Name") + " • " + arena.getString("RewardLabel");
            Button choice = addRenderableWidget(Button.builder(Component.literal(RivalRushClient.clipped(label, span - 12)), button -> {
                firstArena = arena == null ? "" : arena.getString("Id");
                circuit = true; navigate(View.RUN);
            }).bounds(left, 77 + i * 23, span, 20).build());
            choice.setTooltip(Tooltip.create(Component.literal(arena == null
                    ? "Preview the available arena routes after entry. Select a reward, then ride to the chosen room."
                    : arena.getString("Name") + "\n" + arena.getString("Description") + "\n" + arenaDetails(arena)
                    + "\nThis selects the first arena only; later rooms offer branching choices.")));
        }
        Button previous = addRenderableWidget(Button.builder(Component.literal("‹ Previous"), button -> { window--; rebuildWidgets(); })
                .bounds(left, height - 53, 88, 20).build());
        Button next = addRenderableWidget(Button.builder(Component.literal("Next ›"), button -> { window++; rebuildWidgets(); })
                .bounds(left + span - 88, height - 53, 88, 20).build());
        previous.active = window > 0;
        next.active = window + 1 < windows;
    }

    private static String arenaDetails(CompoundTag arena) {
        int diameter = Math.max(1, arena.getInt("Radius") * 2 + 1);
        return arena.getString("RewardLabel") + " • " + RivalRushClient.clock((long) arena.getInt("Seconds") * 20L)
                + " • " + diameter + " × " + diameter;
    }

    private void buildRoute(int left, int span) {
        if (!RivalRushClient.choosingRoute()) return;
        ListTag choices = RivalRushClient.journey().getList("Choices", Tag.TAG_COMPOUND);
        int cardHeight = Math.min(60, Math.max(120, height - 113) / 3);
        for (int i = 0; i < Math.min(3, choices.size()); i++) {
            CompoundTag arena = choices.getCompound(i);
            Button choice = addRenderableWidget(Button.builder(Component.literal(RivalRushClient.clipped(
                    (char) ('A' + i) + ": " + arena.getString("Name") + " · "
                            + RivalRushClient.clock(arena.getInt("Seconds") * 20L), span - 12)), button -> {
                selectRoute(arena);
            }).bounds(left, 77 + i * cardHeight, span, 20).build());
            choice.active = !pending;
            choice.setTooltip(Tooltip.create(Component.literal(arena.getString("Name") + "\n" + arenaDetails(arena)
                    + "\nRun: " + arena.getString("RunReward") + "\n" + sharedInline(arena.getCompound("SharedReward"), true)
                    + "\n" + arena.getString("Description") + "\nSelecting opens a connected route. Ride into the next arena.")));
        }
    }

    private void selectRoute(CompoundTag arena) {
        pending = true; pendingTicks = 0;
        RivalRushClient.requestJourney(arena.getString("Id")); rebuildWidgets();
    }

    private record JourneyNode(String key, String kind, String label, CompoundTag data, int x, int z) {}

    private List<JourneyNode> journeyNodes() {
        CompoundTag journey = RivalRushClient.journey();
        List<JourneyNode> nodes = new ArrayList<>();
        nodes.add(new JourneyNode("start", "start", "S", new CompoundTag(), 0, 0));
        ListTag history = journey.getList("History", Tag.TAG_COMPOUND);
        for (int i = 0; i < Math.min(6, history.size()); i++) {
            CompoundTag room = history.getCompound(i);
            nodes.add(new JourneyNode("done:" + room.getInt("Stage"), "done", Integer.toString(room.getInt("Stage")),
                    room, room.getInt("MapX"), room.getInt("MapZ")));
        }
        if (journey.contains("Current", Tag.TAG_COMPOUND)) {
            CompoundTag room = journey.getCompound("Current");
            nodes.add(new JourneyNode("current", "current", Integer.toString(room.getInt("Stage")),
                    room, room.getInt("MapX"), room.getInt("MapZ")));
        }
        ListTag choices = journey.getList("Choices", Tag.TAG_COMPOUND);
        for (int i = 0; i < Math.min(3, choices.size()); i++) {
            CompoundTag room = choices.getCompound(i);
            nodes.add(new JourneyNode("offer:" + room.getString("Id"), "offer", Character.toString((char) ('A' + i)),
                    room, room.getInt("MapX"), room.getInt("MapZ")));
        }
        return nodes;
    }

    private JourneyNode selectedNode(List<JourneyNode> nodes) {
        for (JourneyNode node : nodes) if (node.key().equals(selectedJourney)) return node;
        JourneyNode selected = nodes.stream().filter(n -> n.kind().equals("current")).findFirst()
                .orElseGet(() -> nodes.stream().filter(n -> n.kind().equals("offer")).findFirst().orElse(nodes.get(nodes.size() - 1)));
        selectedJourney = selected.key(); detailScroll = 0;
        return selected;
    }

    private int mapWidth() { return Math.max(110, Math.min(180, span() * 2 / 5)); }

    private int[] mapPoint(JourneyNode node, List<JourneyNode> nodes) {
        int minX = 0, maxX = 0, maxZ = 1;
        for (JourneyNode value : nodes) { minX = Math.min(minX, value.x()); maxX = Math.max(maxX, value.x()); maxZ = Math.max(maxZ, value.z()); }
        int x = maxX == minX ? left() + mapWidth() / 2
                : left() + 8 + Math.round((node.x() - minX) * (mapWidth() - 16.0F) / (maxX - minX));
        int y = 82 + Math.round(node.z() * Math.max(24, height - 148.0F) / maxZ);
        return new int[]{x, y};
    }

    private void buildJourney(int left, int span) {
        List<JourneyNode> nodes = journeyNodes();
        JourneyNode selected = selectedNode(nodes);
        for (JourneyNode node : nodes) {
            int[] point = mapPoint(node, nodes);
            Button room = addRenderableWidget(Button.builder(Component.literal(node.label()), button -> {
                selectedJourney = node.key(); detailScroll = 0; rebuildWidgets();
            }).bounds(point[0] - 7, point[1] - 7, 14, 14).build());
            room.active = !node.key().equals(selected.key());
            String status = node.kind().equals("start") ? "Set start and banked totals" : node.kind().equals("done")
                    ? "Cleared room " + node.data().getInt("Stage") : node.kind().equals("offer") ? "Offered route " + node.label() : "Current destination";
            room.setTooltip(Tooltip.create(Component.literal(status + (node.kind().equals("start") ? "" : "\n" + node.data().getString("Name")))));
        }
        int half = (span - 4) / 2;
        addRenderableWidget(Button.builder(Component.literal(RivalRushClient.choosingRoute() ? "Compare routes" : "Run summary"),
                button -> navigate(RivalRushClient.choosingRoute() ? View.ROUTE : View.RUN)).bounds(left, height - 53, half, 20).build());
        boolean finale = RivalRushClient.state().getBoolean("Finale");
        String action = finale ? "Continue into Encore" : selected.kind().equals("offer") ? "Take this route"
                : RivalRushClient.choosingRoute() ? "Choose next arena" : "Resume skating";
        Button proceed = addRenderableWidget(Button.builder(Component.literal(action), button -> {
            if (finale) { pending = true; pendingTicks = 0; RivalRushClient.request("encore", "", 0); rebuildWidgets(); }
            else if (selected.kind().equals("offer")) selectRoute(selected.data());
            else if (RivalRushClient.choosingRoute()) navigate(View.ROUTE);
            else onClose();
        }).bounds(left + half + 4, height - 53, span - half - 4, 20).build());
        proceed.active = !pending;
    }

    private static String sharedInline(CompoundTag shared, boolean preview) {
        return (preview ? "Up to: " : "Banked: ") + shared.getInt("Supplies") + " supplies · "
                + shared.getInt("CrewXp") + " crew XP · " + shared.getInt("MemberXp") + " training · " + shared.getInt("Reputation") + " rep";
    }

    private List<CardLine> journeyLines(int availableWidth) {
        List<CardLine> lines = new ArrayList<>();
        CompoundTag journey = RivalRushClient.journey();
        JourneyNode node = selectedNode(journeyNodes());
        CompoundTag room = node.data();
        if (node.kind().equals("start")) {
            detail(lines, "Set " + journey.getInt("Set") + " · " + journey.getInt("Completed") + "/6 rooms cleared", availableWidth, 0xFFC5ED55);
            detail(lines, "Only known rooms and offered routes appear on this map.", availableWidth, 0xFFDDDDDD);
            detail(lines, "You ride each connection. Select A, B or C to inspect a current offer.", availableWidth, 0xFF87E1EA);
        } else {
            detail(lines, room.getString("Name"), availableWidth, 0xFFC5ED55);
            String status = node.kind().equals("done") ? "Room " + room.getInt("Stage") + " cleared"
                    : node.kind().equals("offer") ? "Route " + node.label() + " · room " + room.getInt("Stage")
                    : "travel".equals(journey.getString("Phase")) ? "Ride there · " + journey.getInt("Distance") + " blocks"
                    : "In progress · " + RivalRushClient.clock(journey.getLong("TicksLeft"));
            detail(lines, status, availableWidth, 0xFF87E1EA);
            detail(lines, "Run: " + room.getString("RunReward"), availableWidth, 0xFFE7E7E7);
            boolean recorded = room.getBoolean("SettlementRecorded");
            CompoundTag shared = room.getCompound("SharedReward");
            if (node.kind().equals("done") && (!recorded || !shared.getBoolean("Accepted"))) {
                detail(lines, recorded ? "Shared reward unavailable; no banked payout is claimed." : "Waiting for the actual shared settlement.", availableWidth, 0xFFE6DBA6);
            } else {
                detail(lines, node.kind().equals("done") ? "Already banked" : "Completion, up to · " + room.getString("Emphasis"), availableWidth, 0xFFC5ED55);
                rewardLines(lines, shared, availableWidth);
                if (shared.getBoolean("PendingHouse")) detail(lines, "Saved until you found a crew house.", availableWidth, 0xFFE6DBA6);
            }
            if (!node.kind().equals("done")) {
                detail(lines, RivalRushClient.clock(room.getInt("Seconds") * 20L) + " + " + room.getInt("Defeats")
                        + " owned rivals · " + (room.getInt("Radius") * 2 + 1) + " × " + (room.getInt("Radius") * 2 + 1), availableWidth, 0xFFB8B8B8);
                detail(lines, "Full run ranks pay score and recovery instead.", availableWidth, 0xFFB8B8B8);
            }
            detail(lines, room.getString("Description"), availableWidth, 0xFFB8B8B8);
        }
        CompoundTag totals = new CompoundTag();
        for (var raw : journey.getList("History", Tag.TAG_COMPOUND)) {
            CompoundTag entry = (CompoundTag) raw;
            CompoundTag earned = entry.getCompound("SharedReward");
            if (entry.getBoolean("SettlementRecorded") && earned.getBoolean("Accepted")) {
                for (String key : List.of("Supplies", "CrewXp", "MemberXp", "Reputation")) totals.putInt(key, totals.getInt(key) + earned.getInt(key));
            }
        }
        detail(lines, "Banked this set", availableWidth, 0xFFC5ED55);
        rewardLines(lines, totals, availableWidth);
        return lines;
    }

    private void rewardLines(List<CardLine> lines, CompoundTag amounts, int availableWidth) {
        detail(lines, amounts.getInt("Supplies") + " supplies · " + amounts.getInt("CrewXp") + " crew XP", availableWidth, 0xFFE7E7E7);
        detail(lines, amounts.getInt("MemberXp") + " member XP · " + amounts.getInt("Reputation") + " reputation", availableWidth, 0xFFE7E7E7);
    }

    private void detail(List<CardLine> lines, String text, int availableWidth, int color) {
        if (text.isBlank()) return;
        for (FormattedCharSequence line : font.split(Component.literal(text), Math.max(20, availableWidth))) lines.add(new CardLine(line, color));
    }

    private void buildBuild(int left, int span) {
        CompoundTag build = RivalRushClient.state().getCompound("Build");
        boolean ready = build.getBoolean("RemixReady") && !build.getList("Artifacts", Tag.TAG_COMPOUND).isEmpty();
        Button remix = addRenderableWidget(Button.builder(Component.literal(ready ? "Remix an artifact"
                        : "Artifact remix · earned between arenas"), button -> navigate(View.REMIX))
                .bounds(left, height - 53, (span - 4) / 2, 20).build());
        remix.active = ready;
        remix.setTooltip(Tooltip.create(Component.literal("A completed arena offers one optional replacement. Keep the replaced artifact's rank."
                + "\nThe opportunity does not stack; it is usable only between rooms, outside upgrade drafts.")));
        addRenderableWidget(Button.builder(Component.literal("Scout reward caches"), button -> navigate(View.CACHES))
                .bounds(left + (span - 4) / 2 + 4, height - 53, span - (span - 4) / 2 - 4, 20).build());
    }

    private List<CardLine> cacheLines() {
        List<CardLine> lines = new ArrayList<>();
        CompoundTag scouting = RivalRushClient.state().getCompound("Caches");
        ListTag caches = scouting.getList("Caches", Tag.TAG_COMPOUND);
        if (!caches.isEmpty()) detail(lines, scouting.getBoolean("Ready") ? "A cache is charged. Ride through the reward you want."
                : "Defeat " + scouting.getLong("DefeatsNeeded") + " more rivals to charge your next cache.", span(), 0xFFC5ED55);
        detail(lines, "Each marker has its own color and floor pattern. Three new defeats charge another choice.", span(), 0xFFB8B8B8);
        if (caches.isEmpty()) detail(lines, "Caches appear inside the active arena or nearby free streets. Follow your route to reach them.", span(), 0xFFE7E7E7);
        for (int i = 0; i < Math.min(6, caches.size()); i++) {
            CompoundTag cache = caches.getCompound(i);
            double dx = minecraft.player == null ? 0 : cache.getInt("X") + .5 - minecraft.player.getX();
            double dz = minecraft.player == null ? 0 : cache.getInt("Z") + .5 - minecraft.player.getZ();
            String direction = Math.abs(dx) < 3 && Math.abs(dz) < 3 ? "Here" : (dz < -3 ? "N" : dz > 3 ? "S" : "")
                    + (dx < -3 ? "W" : dx > 3 ? "E" : "");
            detail(lines, cache.getString("Label") + " • " + direction + " • " + cache.getInt("Distance") + "m", span(), 0xFF000000 | cache.getInt("Color"));
            detail(lines, cache.getString("Benefit"), span(), 0xFFE7E7E7);
            if (cache.getLong("CooldownTicks") > 0) detail(lines, "Recharges in " + RivalRushClient.clock(cache.getLong("CooldownTicks"))
                    + " active time", span(), 0xFFB8B8B8);
        }
        return lines;
    }

    private List<CardLine> buildLines() {
        List<CardLine> lines = new ArrayList<>();
        CompoundTag state = RivalRushClient.state(), build = state.getCompound("Build");
        if (build.isEmpty()) {
            detail(lines, "Waiting for the server's actual build details.", span(), 0xFFDDDDDD); return lines;
        }
        detail(lines, "Damage " + multiplier(build.getDouble("DamageMultiplier")) + " · critical "
                + String.format(Locale.ROOT, "%.0f%% at ", finite(build.getDouble("CritChance")) * 100)
                + multiplier(build.getDouble("CritMultiplier")), span(), 0xFF87E1EA);
        detail(lines, "Area " + multiplier(build.getDouble("AreaMultiplier")) + " · vitality "
                + (int) Math.ceil(state.getDouble("Vitality")) + "/" + (int) Math.ceil(state.getDouble("MaxVitality")), span(), 0xFFE7E7E7);
        if (build.contains("CadenceMultiplier")) detail(lines, "Primary attack interval " + multiplier(build.getDouble("CadenceMultiplier"))
                + " · artifact interval " + multiplier(build.getDouble("ArtifactIntervalMultiplier")), span(), 0xFFE7E7E7);
        if (build.contains("PickupRadius")) detail(lines, String.format(Locale.ROOT, "Pickup %.1f blocks · recovery %.2f vitality/s",
                finite(build.getDouble("PickupRadius")), finite(build.getDouble("RecoveryPerSecond"))), span(), 0xFFE7E7E7);
        ListTag artifacts = build.getList("Artifacts", Tag.TAG_COMPOUND);
        detail(lines, "Artifact families " + Math.min(3, artifacts.size()) + "/" + Math.max(1, build.getInt("ArtifactSlots")), span(), 0xFFC5ED55);
        if (artifacts.isEmpty()) detail(lines, "No artifacts yet. Earn a run upgrade or clear an artifact-reward arena.", span(), 0xFFB8B8B8);
        for (int i = 0; i < Math.min(3, artifacts.size()); i++) {
            CompoundTag artifact = artifacts.getCompound(i);
            detail(lines, artifact.getString("Name") + " · rank " + artifact.getInt("Rank"), span(), 0xFFC5ED55);
            detail(lines, artifact.getString("Description"), span(), 0xFFDDDDDD);
            switch (artifact.getString("Id")) {
                case "BATTERY" -> detail(lines, "Line charge " + build.getInt("LineCharge") + "/" + build.getInt("LineChargeMax"), span(), 0xFF87E1EA);
                case "AIR_BREAK" -> detail(lines, build.getBoolean("AirborneReady") ? "Landing pulse ready: land from your current airborne move."
                        : "Build an airborne move, then land to trigger the pulse.", span(), 0xFF87E1EA);
                case "EXPOSE" -> detail(lines, "Marked rivals: " + build.getInt("MarkedRivals"), span(), 0xFF87E1EA);
                case "VENOM" -> detail(lines, "Rivals carrying venom: " + build.getInt("VenomRivals"), span(), 0xFF87E1EA);
                case "CHILL" -> detail(lines, "Chilled rivals: " + build.getInt("ChilledRivals"), span(), 0xFF87E1EA);
                case "MULTISHOT" -> detail(lines, "Split volleys: " + build.getLong("MultishotVolleys"), span(), 0xFF87E1EA);
                case "RADIANCE" -> detail(lines, "Recovery pulses: " + build.getLong("RadiantPulses"), span(), 0xFF87E1EA);
                case "WARD" -> detail(lines, "Ward " + (int) Math.ceil(build.getDouble("Ward")) + "/"
                        + (int) Math.ceil(build.getDouble("WardMax")) + " • recharges after six active seconds without damage", span(), 0xFF87E1EA);
                case "SOUL_BURST" -> detail(lines, "Soul charge " + build.getInt("SoulBurstCharge") + "/"
                        + build.getInt("SoulBurstChargeMax") + " • " + build.getInt("SoulBurstCost")
                        + " per burst • " + build.getLong("SoulBursts") + " bursts", span(), 0xFF87E1EA);
                default -> { }
            }
        }
        return lines;
    }

    private CompoundTag artifact(String id, String list) {
        for (var raw : RivalRushClient.state().getCompound("Build").getList(list, Tag.TAG_COMPOUND)) {
            CompoundTag value = (CompoundTag) raw;
            if (value.getString("Id").equals(id)) return value;
        }
        return new CompoundTag();
    }

    private List<CompoundTag> remixTargets() {
        int rank = artifact(remixFrom, "Artifacts").getInt("Rank");
        List<CompoundTag> choices = new ArrayList<>();
        for (var raw : RivalRushClient.state().getCompound("Build").getList("RemixChoices", Tag.TAG_COMPOUND)) {
            CompoundTag value = (CompoundTag) raw;
            if (value.getInt("Rank") == 0 && value.getInt("MaxRank") >= rank && !value.getString("Id").equals(remixFrom)) choices.add(value);
        }
        return choices;
    }

    private int remixRows() { return Math.max(1, (height - 162) / 23); }

    private void buildRemix(int left, int span) {
        CompoundTag build = RivalRushClient.state().getCompound("Build");
        if (!build.getBoolean("RemixReady")) {
            addRenderableWidget(Button.builder(Component.literal("Back to build"), button -> navigate(View.BUILD))
                    .bounds(left, height - 53, span, 20).build()); return;
        }
        if (!remixFrom.isBlank() && artifact(remixFrom, "Artifacts").isEmpty()) { remixFrom = remixTo = ""; window = 0; }
        if (remixFrom.isBlank()) {
            ListTag owned = build.getList("Artifacts", Tag.TAG_COMPOUND);
            for (int i = 0; i < Math.min(3, owned.size()); i++) {
                CompoundTag source = owned.getCompound(i);
                addRenderableWidget(Button.builder(Component.literal(RivalRushClient.clipped(source.getString("Name")
                                + " · rank " + source.getInt("Rank"), span - 12)), button -> {
                    remixFrom = source.getString("Id"); remixTo = ""; window = 0; rebuildWidgets();
                }).bounds(left, 94 + i * 24, span, 20).build())
                        .setTooltip(Tooltip.create(Component.literal(source.getString("Description"))));
            }
            addRenderableWidget(Button.builder(Component.literal("Back to build"), button -> navigate(View.BUILD))
                    .bounds(left, height - 53, span, 20).build());
            return;
        } else if (remixTo.isBlank()) {
            List<CompoundTag> targets = remixTargets();
            int rows = remixRows(); window = Math.max(0, Math.min(window, Math.max(0, (targets.size() - 1) / rows)));
            for (int i = 0; i < rows && window * rows + i < targets.size(); i++) {
                CompoundTag target = targets.get(window * rows + i);
                addRenderableWidget(Button.builder(Component.literal(RivalRushClient.clipped(target.getString("Name"), span - 12)), button -> {
                    remixTo = target.getString("Id"); detailScroll = 0; rebuildWidgets();
                }).bounds(left, 94 + i * 23, span, 20).build())
                        .setTooltip(Tooltip.create(Component.literal(target.getString("Description") + "\nKeeps rank " + artifact(remixFrom, "Artifacts").getInt("Rank"))));
            }
            Button previous = addRenderableWidget(Button.builder(Component.literal("‹ Previous"), button -> { window--; rebuildWidgets(); })
                    .bounds(left, height - 77, 88, 20).build());
            Button next = addRenderableWidget(Button.builder(Component.literal("Next ›"), button -> { window++; rebuildWidgets(); })
                    .bounds(left + span - 88, height - 77, 88, 20).build());
            previous.active = window > 0; next.active = (window + 1) * rows < targets.size();
        }
        int half = (span - 4) / 2;
        addRenderableWidget(Button.builder(Component.literal(remixFrom.isBlank() ? "Back to build" : remixTo.isBlank() ? "Change source" : "Change target"), button -> {
            if (remixFrom.isBlank()) navigate(View.BUILD);
            else if (remixTo.isBlank()) { remixFrom = ""; window = 0; rebuildWidgets(); }
            else { remixTo = ""; detailScroll = 0; rebuildWidgets(); }
        }).bounds(left, height - 53, half, 20).build());
        Button confirm = addRenderableWidget(Button.builder(Component.literal(remixTo.isBlank() ? "Back to build" : "Confirm remix"), button -> {
            if (remixTo.isBlank()) navigate(View.BUILD);
            else {
                pending = true; pendingTicks = 0;
                RivalRushClient.request("remix", remixFrom + "|" + remixTo, 0); rebuildWidgets();
            }
        }).bounds(left + half + 4, height - 53, span - half - 4, 20).build());
        confirm.active = !pending;
    }

    private List<CardLine> remixLines() {
        List<CardLine> lines = new ArrayList<>();
        CompoundTag build = RivalRushClient.state().getCompound("Build");
        if (!build.getBoolean("RemixReady")) detail(lines, "No remix is available now. Clear an arena to earn one opportunity, then use it between rooms.", span(), 0xFFDDDDDD);
        else if (!remixTo.isBlank()) {
            CompoundTag source = artifact(remixFrom, "Artifacts"), target = artifact(remixTo, "RemixChoices");
            detail(lines, source.getString("Name") + " → " + target.getString("Name"), span(), 0xFFC5ED55);
            detail(lines, "Keep rank " + source.getInt("Rank") + ". Replace this family; the other artifact slots stay the same.", span(), 0xFF87E1EA);
            detail(lines, target.getString("Description"), span(), 0xFFDDDDDD);
            detail(lines, "Uses this arena's one remix opportunity. Continue without a remix whenever you prefer.", span(), 0xFFE6DBA6);
            detail(lines, remixFeedback, span(), 0xFFE6DBA6);
        }
        return lines;
    }

    private void buildAdvanced(int left, int span) {
        gangField = new EditBox(font, left, 87, span, 18, Component.literal("Rival gang registry ID"));
        gangField.setMaxLength(128);
        gangField.setValue(draftGang);
        gangField.setResponder(value -> updateStartButtons());
        addRenderableWidget(gangField);
        mobField = new EditBox(font, left, 122, span, 18, Component.literal("Rival mob registry ID"));
        mobField.setMaxLength(128);
        mobField.setValue(draftMob);
        mobField.setResponder(value -> updateStartButtons());
        addRenderableWidget(mobField);
        int half = (span - 4) / 2;
        applyButton = addRenderableWidget(Button.builder(Component.literal("Apply selection"), button -> {
            rememberFields();
            if (!validIds(draftGang, draftMob)) return;
            gang = draftGang.trim(); mob = draftMob.trim();
            navigate(View.RUN);
        }).bounds(left, 153, half, 20).build());
        addRenderableWidget(Button.builder(Component.literal("Back"), button -> navigate(View.RUN))
                .bounds(left + half + 4, 153, span - half - 4, 20).build());
        updateStartButtons();
    }

    private void updateStartButtons() {
        if (timedButton != null) timedButton.active = !pending && validIds(gang, mob);
        if (endlessButton != null) endlessButton.active = !pending && validIds(gang, mob);
        if (applyButton != null && gangField != null && mobField != null)
            applyButton.active = validIds(gangField.getValue(), mobField.getValue());
    }

    private static boolean validIds(String gangId, String mobId) {
        return gangId != null && mobId != null && gangId.trim().length() + mobId.trim().length() + 1 <= 256
                && ResourceLocation.tryParse(gangId.trim()) != null && ResourceLocation.tryParse(mobId.trim()) != null;
    }

    private void start(boolean endless) {
        rememberFields();
        if (!validIds(gang, mob)) return;
        pending = true;
        pendingTicks = 0;
        updateStartButtons();
        String selection = gang.trim() + "|" + mob.trim() + (circuit ? "|" + firstArena : "");
        if (selection.length() > 256) { pending = false; updateStartButtons(); return; }
        RivalRushClient.request("start", selection, (circuit ? 2 : 0) + (endless ? 1 : 0));
    }

    private void buildOffers(int left, int span) {
        ListTag offers = RivalRushClient.offers();
        int available = Math.max(72, height - 112);
        int cardHeight = Math.min(60, available / 3);
        for (int i = 0; i < Math.min(3, offers.size()); i++) {
            CompoundTag offer = offers.getCompound(i);
            int index = i;
            String label = (i + 1) + ". " + offer.getString("name")
                    + (offer.getInt("rank") > 0 ? "  •  current rank " + offer.getInt("rank") : "");
            Button choice = addRenderableWidget(Button.builder(Component.literal(RivalRushClient.clipped(label, span - 12)), button -> {
                pending = true;
                RivalRushClient.request("choose", offer.getString("id"), index);
                rebuildWidgets();
            }).bounds(left, 77 + i * cardHeight, span, 20).build());
            choice.setTooltip(Tooltip.create(Component.literal(offer.getString("name") + "\n" + offer.getString("description"))));
            choice.active = !pending;
        }
    }

    private void buildList(int left, int span) {
        boolean cards = view == View.ALBUM;
        ListTag list = cards ? RivalRushClient.album() : RivalRushClient.roster();
        int visible = rows();
        window = Math.min(Math.max(0, window), Math.max(0, (list.size() - 1) / visible));
        int start = window * visible;
        for (int i = 0; i < visible && start + i < list.size(); i++) {
            CompoundTag entry = list.getCompound(start + i);
            String label = cards ? cardName(entry) + "  •  " + entry.getLong("count") + " cards  •  T" + entry.getInt("tier")
                    : entry.getString("name") + "  —  " + mobName(entry.getString("mob"));
            Button row = addRenderableWidget(Button.builder(Component.literal(RivalRushClient.clipped(label, span - 12)), button -> {
                if (cards) {
                    selectedCard = entry.copy();
                    navigate(View.CARD);
                } else {
                    gang = entry.getString("id");
                    mob = entry.getString("mob");
                    gangLabel = entry.getString("name"); gangLabelId = gang;
                    navigate(View.RUN);
                }
            }).bounds(left, 77 + i * 23, span, 20).build());
            row.setTooltip(Tooltip.create(Component.literal(label + (cards ? "\nOpen collection details" : "\nSelect this crew and source mob"))));
        }
        Button previous = addRenderableWidget(Button.builder(Component.literal("‹ Previous"), button -> changeListPage(-1))
                .bounds(left, height - 53, 88, 20).build());
        Button next = addRenderableWidget(Button.builder(Component.literal("Next ›"), button -> changeListPage(1))
                .bounds(left + span - 88, height - 53, 88, 20).build());
        previous.active = window > 0 || RivalRushClient.page(cards) > 0;
        next.active = (window + 1) * visible < list.size() || RivalRushClient.page(cards) + 1 < RivalRushClient.pages(cards);
    }

    private void changeListPage(int direction) {
        boolean cards = view == View.ALBUM;
        ListTag list = cards ? RivalRushClient.album() : RivalRushClient.roster();
        int windows = Math.max(1, (list.size() + rows() - 1) / rows());
        if (window + direction >= 0 && window + direction < windows) {
            window += direction;
            rebuildWidgets();
        } else {
            int page = RivalRushClient.page(cards) + direction;
            if (page >= 0 && page < RivalRushClient.pages(cards)) {
                responseWindow = direction < 0 ? Integer.MAX_VALUE : 0;
                RivalRushClient.request(cards ? "album" : "roster", "", page);
            }
        }
    }

    private int span() { return Math.min(460, width - 20); }
    private int left() { return (width - span()) / 2; }
    private int rows() { return Math.max(1, Math.min(20, (height - 137) / 23)); }

    private String crewName() {
        if (gang.equals(gangLabelId) && !gangLabel.isBlank()) return gangLabel;
        ListTag roster = RivalRushClient.roster();
        for (int i = 0; i < roster.size(); i++) {
            CompoundTag entry = roster.getCompound(i);
            if (gang.equals(entry.getString("id")) && !entry.getString("name").isBlank()) return entry.getString("name");
        }
        ResourceLocation id = ResourceLocation.tryParse(gang);
        return GangRegistry.builtInById(id).map(definition -> definition.canonicalName()).orElse("Custom crew");
    }

    private static String mobName(String entityId) {
        ResourceLocation id = ResourceLocation.tryParse(entityId);
        if (id == null || !ForgeRegistries.ENTITY_TYPES.containsKey(id)) return "Unavailable creature";
        var type = ForgeRegistries.ENTITY_TYPES.getValue(id);
        if (type == null) return "Unavailable creature";
        String name = type.getDescription().getString();
        return name.equals(type.getDescriptionId()) ? "Custom creature" : name;
    }

    @Override
    public void render(GuiGraphics gui, int mouseX, int mouseY, float partialTick) {
        renderBackground(gui);
        int left = left();
        int span = span();
        gui.fill(left - 6, 4, left + span + 6, height - 4, 0xF2181C20);
        gui.drawCenteredString(font, title, width / 2, 10, 0xFFC5ED55);
        String subtitle = view == View.JOURNEY ? "Set " + RivalRushClient.journey().getInt("Set") + " · "
                + RivalRushClient.journey().getInt("Completed") + "/6 cleared · select a known room"
                : view == View.ROUTE ? "Compare rewards · Journey opens the map"
                : view == View.ARENAS ? "Choose the first arena • later routes branch"
                : view == View.BUILD ? "Your current run effects • three artifact families"
                : view == View.CACHES ? "Scout caches • choose where to ride"
                : view == View.REMIX ? "Replace one artifact family · keep its rank"
                : "An optional skating survival set • your own run XP";
        gui.drawCenteredString(font, subtitle, width / 2, 26, 0xFFDDDDDD);
        CompoundTag state = RivalRushClient.state();
        if (state.contains("Journey", Tag.TAG_COMPOUND) && view == View.ROUTE) drawJourneyStrip(gui, state.getCompound("Journey"), left, span);
        if (view == View.RUN) {
            if (!state.getBoolean("Active")) {
                gui.drawString(font, RivalRushClient.clipped("Theme: " + mobName(mob), span), left, 102, 0xFF87E1EA);
                if (circuit) wrap(gui, "Six arenas, then Return or Encore with your build.", left, 170, span, 1, 0xFFDDDDDD);
                else wrap(gui, "Explore endless streets, tag rivals and collect cards. Return whenever you choose.",
                        left, 146, span, Math.max(1, (height - 200) / 10), 0xFFDDDDDD);
            } else {
                gui.drawString(font, RivalRushClient.clipped(state.getString("Gang"), span), left, 78, 0xFFE7E7E7);
                gui.drawString(font, (state.contains("Journey", Tag.TAG_COMPOUND) ? "Arena journey • "
                        : state.getBoolean("Endless") ? "Endless • " : "30-minute set • ")
                        + RivalRushClient.clock(state.getLong("Ticks")) + " elapsed", left, 94, 0xFF87E1EA);
                gui.drawString(font, "Level " + state.getInt("Level") + "  •  Score " + state.getLong("Score")
                        + "  •  Cards " + state.getLong("Cards"), left, 110, 0xFFE7E7E7);
                wrap(gui, state.getBoolean("Finale") ? "Set complete! Keep your collected cards and return, or choose an Encore."
                        : state.getString("Objective"), left, 128, span, 2, 0xFFE6DBA6);
                wrap(gui, "Return ends the session. Your normal Minecraft state is restored.", left,
                        Math.min(height - 48, 187), span, 2, 0xFFB8B8B8);
            }
        } else if (view == View.ADVANCED) {
            gui.drawString(font, "Crew registry ID", left, 75, 0xFFDDDDDD);
            gui.drawString(font, "Mob registry ID", left, 110, 0xFFDDDDDD);
            wrap(gui, "A different mob overrides the crew's themed cast. Use this for a specific Minecraft or installed mod creature.",
                    left, 181, span, Math.max(2, (height - 214) / 10), 0xFFB8B8B8);
        } else if (view == View.UPGRADES) {
            int cardHeight = Math.min(60, Math.max(72, height - 112) / 3);
            ListTag offers = RivalRushClient.offers();
            for (int i = 0; i < Math.min(3, offers.size()); i++) {
                wrap(gui, offers.getCompound(i).getString("description"), left + 4, 100 + i * cardHeight,
                        span - 8, Math.max(0, (cardHeight - 25) / 10), 0xFFDDDDDD);
            }
        } else if (view == View.ARENAS) {
            int pages = Math.max(1, (RivalRushClient.arenaCatalogue().size() + 1 + rows() - 1) / rows());
            gui.drawCenteredString(font, "Presets " + (window + 1) + "/" + pages, width / 2, height - 47, 0xFFDDDDDD);
        } else if (view == View.ROUTE) {
            CompoundTag journey = RivalRushClient.journey();
            if (RivalRushClient.choosingRoute()) {
                int cardHeight = Math.min(60, Math.max(120, height - 113) / 3);
                ListTag choices = journey.getList("Choices", Tag.TAG_COMPOUND);
                for (int i = 0; i < Math.min(3, choices.size()); i++) {
                    CompoundTag arena = choices.getCompound(i);
                    gui.drawString(font, RivalRushClient.clipped("Run: " + arena.getString("RunReward"), span - 8), left + 4,
                            100 + i * cardHeight, 0xFF87E1EA);
                    gui.drawString(font, RivalRushClient.clipped(sharedInline(arena.getCompound("SharedReward"), true), span - 8), left + 4,
                            110 + i * cardHeight, 0xFFC5ED55);
                }
                if (choices.isEmpty()) wrap(gui, "Waiting for the server's next route choices…", left, 84, span, 3, 0xFFDDDDDD);
            } else {
                gui.drawString(font, RivalRushClient.clipped("Arena " + journey.getInt("Stage") + "/"
                        + Math.max(1, journey.getInt("StagesPerSet")) + " • " + journey.getString("CurrentName"), span), left, 78, 0xFF87E1EA);
                String timing = "travel".equals(journey.getString("Phase"))
                        ? "Ride to the arena • " + Math.max(0, (int) Math.ceil(journey.getDouble("Distance"))) + " blocks"
                        : "Arena time remaining • " + RivalRushClient.clock(journey.getLong("TicksLeft"));
                gui.drawString(font, RivalRushClient.clipped(timing, span), left, 94, 0xFFE7E7E7);
                wrap(gui, journey.getString("Objective"), left, 111, span, 2, 0xFFE6DBA6);
                gui.drawString(font, RivalRushClient.clipped("Reward: " + journey.getString("Reward"), span), left, 137, 0xFFC5ED55);
                ListTag history = journey.getList("History", Tag.TAG_COMPOUND);
                int visible = Math.max(0, Math.min(4, (height - 190) / 11));
                for (int i = Math.max(0, history.size() - visible), row = 0; i < history.size(); i++, row++) {
                    CompoundTag entry = history.getCompound(i);
                    gui.drawString(font, RivalRushClient.clipped(entry.getInt("Stage") + ". " + entry.getString("Name")
                            + " • " + entry.getString("Reward"), span), left, 157 + row * 11, 0xFFB8B8B8);
                }
            }
        } else if (view == View.JOURNEY) {
            drawJourney(gui, left, span);
        } else if (view == View.BUILD) {
            drawDetails(gui, buildLines(), left, span);
        } else if (view == View.CACHES) {
            drawDetails(gui, cacheLines(), left, span);
        } else if (view == View.REMIX) {
            if (!state.getCompound("Build").getBoolean("RemixReady") || !remixTo.isBlank()) drawDetails(gui, remixLines(), left, span);
            else {
                String heading = remixFrom.isBlank() ? "Choose the owned artifact to replace"
                        : "Replace " + artifact(remixFrom, "Artifacts").getString("Name") + " · keep rank " + artifact(remixFrom, "Artifacts").getInt("Rank");
                gui.drawString(font, RivalRushClient.clipped(heading, span), left, 77, 0xFF87E1EA);
                if (!remixFrom.isBlank()) {
                    int pages = Math.max(1, (remixTargets().size() + remixRows() - 1) / remixRows());
                    gui.drawCenteredString(font, (window + 1) + "/" + pages, width / 2, height - 71, 0xFFB8B8B8);
                    if (remixTargets().isEmpty()) wrap(gui, "No unowned available artifact can retain this rank.", left, 96, span, 3, 0xFFE6DBA6);
                }
            }
        } else if (view == View.ROSTER || view == View.ALBUM) {
            boolean cards = view == View.ALBUM;
            ListTag list = cards ? RivalRushClient.album() : RivalRushClient.roster();
            if (list.isEmpty()) {
                if (cards) {
                    gui.drawString(font, "No cards collected yet", left, 81, 0xFF87E1EA);
                    wrap(gui, "Defeat rivals and pick up their dropped cards to fill your album.",
                            left, 99, Math.min(span, 280), 4, 0xFFDDDDDD);
                } else {
                    wrap(gui, "Choose Rival crews above to request the server's current roster.", left, 81, span, 4, 0xFFDDDDDD);
                }
            }
            String page = (RivalRushClient.page(cards) + 1) + "/" + RivalRushClient.pages(cards)
                    + " • " + (list.isEmpty() ? "0" : (window * rows() + 1) + "–" + Math.min(list.size(), (window + 1) * rows()));
            gui.drawCenteredString(font, page, width / 2, height - 47, 0xFFDDDDDD);
        } else if (view == View.RESULT) {
            gui.drawString(font, state.getBoolean("Active") ? "Session update" : "Session result", left, 79, 0xFFC5ED55);
            wrap(gui, resultSummary, left, 100, span, Math.max(2, (height - 164) / 10), 0xFFE7E7E7);
        } else if (view == View.CARD && selectedCard != null) {
            List<CardLine> lines = cardLines();
            cardScroll = Math.max(0, Math.min(cardScroll, maxCardScroll(lines)));
            gui.enableScissor(left, 75, left + span, height - 65);
            for (int i = 0; i < lines.size(); i++) {
                CardLine line = lines.get(i);
                gui.drawString(font, line.text(), left, 75 + i * 11 - cardScroll, line.color(), true);
            }
            gui.disableScissor();
            if (maxCardScroll(lines) > 0) gui.drawCenteredString(font, "Scroll or Page Up / Down for details",
                    width / 2, height - 64, 0xFFB8B8B8);
        }
        super.render(gui, mouseX, mouseY, partialTick);
    }

    private void wrap(GuiGraphics gui, String text, int x, int y, int availableWidth, int lines, int color) {
        var wrapped = font.split(Component.literal(text), Math.max(20, availableWidth));
        for (int i = 0; i < Math.min(lines, wrapped.size()); i++) gui.drawString(font, wrapped.get(i), x, y + i * 10, color, true);
    }

    private void drawJourney(GuiGraphics gui, int left, int span) {
        List<JourneyNode> nodes = journeyNodes();
        JourneyNode selected = selectedNode(nodes), previous = nodes.get(0);
        gui.fill(left - 2, 73, left + mapWidth() + 2, height - 57, 0xFF222A32);
        for (int i = 1; i < nodes.size(); i++) {
            JourneyNode node = nodes.get(i);
            int color = node.kind().equals("done") ? 0xFFC5ED55 : node.key().equals(selected.key()) ? 0xFF87E1EA : 0xFF687683;
            int[] from = mapPoint(previous, nodes), to = mapPoint(node, nodes);
            line(gui, from[0], from[1], to[0], to[1], color);
            if (node.kind().equals("done")) previous = node;
        }
        int[] selectedPoint = mapPoint(selected, nodes);
        gui.fill(selectedPoint[0] - 9, selectedPoint[1] - 9, selectedPoint[0] + 9, selectedPoint[1] + 9, 0xFF87E1EA);
        int detailLeft = left + mapWidth() + 12, detailWidth = span - mapWidth() - 12;
        drawDetails(gui, journeyLines(detailWidth), detailLeft, detailWidth);
    }

    private static void line(GuiGraphics gui, int x1, int y1, int x2, int y2, int color) {
        int dx = Math.abs(x2 - x1), dy = Math.abs(y2 - y1), sx = x1 < x2 ? 1 : -1, sy = y1 < y2 ? 1 : -1;
        int error = dx - dy;
        while (true) {
            gui.fill(x1, y1, x1 + 2, y1 + 2, color);
            if (x1 == x2 && y1 == y2) break;
            int twice = 2 * error;
            if (twice > -dy) { error -= dy; x1 += sx; }
            if (twice < dx) { error += dx; y1 += sy; }
        }
    }

    private int maxDetailScroll(List<CardLine> lines) { return Math.max(0, lines.size() * 11 - Math.max(22, height - 145)); }

    private List<CardLine> currentDetails() {
        return view == View.JOURNEY ? journeyLines(span() - mapWidth() - 12) : view == View.BUILD ? buildLines()
                : view == View.CACHES ? cacheLines() : remixLines();
    }

    private void drawDetails(GuiGraphics gui, List<CardLine> lines, int left, int availableWidth) {
        detailScroll = Math.max(0, Math.min(detailScroll, maxDetailScroll(lines)));
        gui.enableScissor(left, 75, left + availableWidth, height - 69);
        for (int i = 0; i < lines.size(); i++) {
            CardLine value = lines.get(i);
            gui.drawString(font, value.text(), left, 77 + i * 11 - detailScroll, value.color(), true);
        }
        gui.disableScissor();
        if (maxDetailScroll(lines) > 0) gui.drawString(font, RivalRushClient.clipped("Scroll / Page Up–Down", availableWidth), left, height - 64, 0xFFB8B8B8);
    }

    private void drawJourneyStrip(GuiGraphics gui, CompoundTag journey, int left, int span) {
        int stages = Math.max(1, Math.min(6, journey.getInt("StagesPerSet")));
        int completed = Math.max(0, Math.min(stages, journey.getInt("Completed")));
        int spacing = Math.min(42, (span - 40) / stages);
        int first = left + (span - (stages - 1) * spacing) / 2;
        gui.fill(first, 38, first + (stages - 1) * spacing, 40, 0xFF47505B);
        for (int i = 0; i < stages; i++) {
            int color = i < completed ? 0xFFC5ED55 : i == completed ? 0xFF87E1EA : 0xFF59616B;
            int x = first + i * spacing;
            gui.fill(x - 3, 36, x + 4, 43, color);
        }
    }

    private static double finite(double value) { return Double.isFinite(value) ? Math.max(0.0D, value) : 0.0D; }
    private static String multiplier(double value) { return String.format(Locale.ROOT, "%.2f×", finite(value)); }

    private static String cardName(CompoundTag card) {
        return card.getString("name").isBlank() ? card.getString("mob") : card.getString("name");
    }

    private record CardLine(FormattedCharSequence text, int color) {}

    private List<CardLine> cardLines() {
        List<CardLine> lines = new ArrayList<>();
        addCardLines(lines, cardName(selectedCard), 0xFFC5ED55);
        if (!cardName(selectedCard).equals(selectedCard.getString("mob")))
            addCardLines(lines, selectedCard.getString("mob"), 0xFFB8B8B8);
        addCardLines(lines, "Crew: " + selectedCard.getString("gang"), 0xFFDDDDDD);
        addCardLines(lines, "Collected: " + selectedCard.getLong("count") + "  •  Mastery tier: " + selectedCard.getInt("tier"), 0xFF87E1EA);
        if (selectedCard.contains("next") && selectedCard.getLong("next") > selectedCard.getLong("count"))
            addCardLines(lines, "Next mastery at " + selectedCard.getLong("next") + " cards ("
                    + (selectedCard.getLong("next") - selectedCard.getLong("count")) + " to go)", 0xFFE6DBA6);
        else if (selectedCard.contains("next") && selectedCard.getLong("next") == 0L)
            addCardLines(lines, "Current mastery track complete.", 0xFFE6DBA6);
        if (!selectedCard.getString("reward").isBlank()) addCardLines(lines, selectedCard.getString("reward"), 0xFFE7E7E7);
        addCardLines(lines, "Card mastery powers Rival Rush. Earned crew rewards connect activities; Minecraft XP stays separate.", 0xFFB8B8B8);
        return lines;
    }

    private void addCardLines(List<CardLine> lines, String text, int color) {
        for (FormattedCharSequence line : font.split(Component.literal(text), Math.max(20, span()))) lines.add(new CardLine(line, color));
        lines.add(new CardLine(FormattedCharSequence.EMPTY, color));
    }

    private int maxCardScroll(List<CardLine> lines) { return Math.max(0, lines.size() * 11 - Math.max(1, height - 140)); }

    @Override
    public void tick() {
        super.tick();
        if (pending && ++pendingTicks >= 30) {
            pending = false;
            pendingTicks = 0;
            rebuildWidgets();
        }
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double amount) {
        if (view == View.CARD && selectedCard != null) {
            cardScroll = Math.max(0, Math.min(maxCardScroll(cardLines()), cardScroll - (int) (amount * 22)));
            return true;
        }
        if (view == View.JOURNEY || view == View.BUILD || view == View.CACHES || view == View.REMIX && !remixTo.isBlank()) {
            detailScroll = Math.max(0, Math.min(maxDetailScroll(currentDetails()), detailScroll - (int) (amount * 22)));
            return true;
        }
        return super.mouseScrolled(mouseX, mouseY, amount);
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (view == View.CARD && selectedCard != null && (keyCode == 266 || keyCode == 267)) {
            cardScroll = Math.max(0, Math.min(maxCardScroll(cardLines()), cardScroll + (keyCode == 266 ? -66 : 66)));
            return true;
        }
        if ((view == View.JOURNEY || view == View.BUILD || view == View.CACHES || view == View.REMIX && !remixTo.isBlank()) && (keyCode == 266 || keyCode == 267)) {
            detailScroll = Math.max(0, Math.min(maxDetailScroll(currentDetails()), detailScroll + (keyCode == 266 ? -66 : 66)));
            return true;
        }
        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    @Override
    public boolean isPauseScreen() { return false; }
}
