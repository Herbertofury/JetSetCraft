package com.herberto.jetsetcraft.client.liberty;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.screens.ConfirmScreen;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.util.FormattedCharSequence;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/** Native campaign bridge, four-slot armory review and field radio; all eligibility and outcomes stay server-owned. */
public final class LibertyCampaignScreen extends Screen {
    private enum View { MAP, PLAN, REVIEW, DEFENSES, CALLINS, CALLIN, SHIP, MODULE, ARMORY, FIELD }
    private record Line(FormattedCharSequence text, int color) { }
    private static final int GOLD = 0xFFE5C35E, CYAN = 0xFF87E1EA, WHITE = 0xFFF1F3F5, DIM = 0xFFB4BCC7;
    private View view;
    private View armoryReturn = View.SHIP, callinsReturn = View.MAP;
    private String sector = "neon_docks", mission = "turf_liberation", defense = "", selected = "", armorySlot = "primary", query = "";
    private int difficulty = 1, minutes = 25, page, scroll, contentHeight, pendingTicks;
    private boolean pending, refreshRows;
    private EditBox filter;

    public LibertyCampaignScreen() {
        super(Component.literal("Liberty Grind · Campaign")); view = state().getBoolean("Active") ? View.FIELD : View.MAP;
    }
    public void onSnapshot(CompoundTag incoming, CompoundTag previous) {
        boolean started = incoming.getBoolean("Active") && !previous.getBoolean("Active");
        boolean reinforced = previous.getString("Phase").equals("DOWNED") && incoming.getString("Phase").equals("DROPPING");
        if (started || reinforced) { minecraft.setScreen(null); return; }
        boolean change = incoming.getBoolean("Open") || incoming.getLong("Revision") != previous.getLong("Revision")
                || !incoming.getCompound("Ship").equals(previous.getCompound("Ship"))
                || !incoming.getCompound("Armory").equals(previous.getCompound("Armory"))
                || incoming.getCompound("Campaign").getLong("Revision") != previous.getCompound("Campaign").getLong("Revision")
                || !incoming.getString("Status").equals(previous.getString("Status"));
        if (incoming.getBoolean("InShip") && !previous.getBoolean("InShip")) { view = View.SHIP; scroll = page = 0; change = true; }
        else if (!incoming.getBoolean("Active") && previous.getBoolean("Active")) { view = View.MAP; scroll = page = 0; change = true; }
        if (change) { pending = false; pendingTicks = 0; rebuildWidgets(); }
        else if (view == View.FIELD) rebuildWidgets();
    }

    @Override protected void init() {
        filter = null; int x = left(), width = span(), quarter = (width - 12) / 4;
        button("War map", x, 38, quarter, () -> change(View.MAP));
        button("Call-ins", x + quarter + 4, 38, quarter, () -> change(View.CALLINS));
        button("Ship", x + 2 * (quarter + 4), 38, quarter, () -> change(View.SHIP));
        button("Field", x + 3 * (quarter + 4), 38, width - 3 * (quarter + 4), () -> change(View.FIELD));
        int fifth = (width - 16) / 5, y = height - 26;
        button("Back", x, y, fifth, this::back);
        button("Research", x + fifth + 4, y, fifth, () -> minecraft.setScreen(new LibertyScreen(true)));
        button("Refresh", x + 2 * (fifth + 4), y, fifth, () -> send("open", ""));
        button("HUD", x + 3 * (fifth + 4), y, fifth, () -> minecraft.setScreen(new LibertyHudScreen(this)));
        button("Done", x + 4 * (fifth + 4), y, width - 4 * (fifth + 4), this::onClose);
        switch (view) {
            case MAP -> initMap(); case PLAN -> initPlan(); case REVIEW -> initReview(); case DEFENSES -> initDefenses();
            case CALLINS -> initCallins(); case CALLIN -> initCallin(); case SHIP -> initShip(); case MODULE -> initModule();
            case ARMORY -> initArmory(); case FIELD -> initField();
        }
    }
    private void initMap() {
        ListTag regions = campaign().getList("Sectors", Tag.TAG_COMPOUND); int card = mapCardWidth(), gap = mapGap();
        for (int i = 0; i < Math.min(6, regions.size()); i++) {
            CompoundTag row = regions.getCompound(i); int column = i < 3 ? 0 : 1, lane = i % 3;
            Button region = addRenderableWidget(new RegionButton(left() + column * (card + gap), 74 + lane * mapLaneHeight(), card, mapLaneHeight() - 14, row, () -> {
                sector = row.getString("Id"); defense = ""; selectFirstMission(); change(View.PLAN);
            }));
            // Locked frontiers remain inspectable; launch is still gated by the authoritative route.
            region.active = !pending && !state().getBoolean("Active");
            tip(region, row.getString("Name") + " · " + factionName(row.getString("Faction")) + "\n"
                    + row.getInt("Liberation") / 100 + "% liberated · threat " + row.getInt("Threat") + "\n"
                    + (row.getBoolean("Unlocked") ? "Open region. Choose a mission and review your loadout." : row.getString("Reason")));
        }
        int halfFooter = (span() - 4) / 2;
        button("Defense offers", left(), actionY(), halfFooter, () -> change(View.DEFENSES));
        Button ship = button(state().getBoolean("InShip") ? "Walk around ship" : "Board your ship", left() + halfFooter + 4, actionY(), span() - halfFooter - 4,
                () -> { if (state().getBoolean("InShip")) onClose(); else send("ship", ""); });
        ship.active = !pending && !state().getBoolean("Active");
    }
    private void initPlan() {
        List<CompoundTag> missions = missions(); int narrow = 22;
        if (defense.isEmpty()) {
            button("<", left(), 84, narrow, () -> cycleMission(-1));
            button(">", left() + span() - narrow, 84, narrow, () -> cycleMission(1));
        }
        button("−", left(), 137, 22, () -> { difficulty = Math.max(1, difficulty - 1); rebuildWidgets(); });
        button("+", left() + 115, 137, 22, () -> { difficulty = Math.min(10, difficulty + 1); rebuildWidgets(); });
        button(minutes + " minute operation", left() + 145, 137, span() - 145,
                () -> { minutes = minutes == 20 ? 25 : minutes == 25 ? 30 : 20; rebuildWidgets(); });
        Button review = button("Review weapons, armor & call-ins", left(), actionY(), span(), () -> change(View.REVIEW));
        review.active = !pending && !state().getBoolean("Active") && (!defense.isEmpty() || !missions.isEmpty());
    }
    private void initReview() {
        int half = (span() - 4) / 2;
        button("Weapons & armor", left(), height - 73, half, () -> change(View.ARMORY));
        button("Four call-in slots", left() + half + 4, height - 73, span() - half - 4, () -> change(View.CALLINS));
        Button deploy = button(defense.isEmpty() ? "Confirm loadout · Launch pod" : "Confirm loadout · Deploy defense", left(), actionY(), span(), () -> {
            if (defense.isEmpty()) send("campaign_deploy", sector + "|" + mission + "|" + difficulty + "|" + minutes);
            else send("defense_deploy", defense + "|" + difficulty + "|" + minutes);
        });
        deploy.active = !pending && !state().getBoolean("Active") && armory().getBoolean("Ready")
                && state().getBoolean("CanDeploy") && routeAvailable();
        tip(deploy, !routeAvailable() ? routeReason() : armory().getBoolean("Ready")
                ? "Your normal world and belongings are checkpointed before the pod launches." : armory().getString("Reason"));
    }
    private void initDefenses() {
        ListTag offers = campaign().getList("Defenses", Tag.TAG_COMPOUND); int count = Math.max(1, (height - 126) / 32);
        normalizePage(offers.size(), count);
        for (int i = page * count; i < Math.min(offers.size(), (page + 1) * count); i++) {
            CompoundTag row = offers.getCompound(i);
            Button offer = button(row.getString("Name"), left(), 69 + (i - page * count) * 32, span(), () -> {
                defense = row.getUUID("Id").toString(); sector = row.getString("Sector"); change(View.PLAN);
            });
            offer.active = !pending && !state().getBoolean("Active") && row.getBoolean("CanDeploy"); tip(offer, row.getString("Description"));
        }
        pages(offers.size(), count);
    }
    private void initCallins() {
        search(64); List<CompoundTag> rows = filtered(ship().getList("Callins", Tag.TAG_COMPOUND)); int count = listCount(); normalizePage(rows.size(), count);
        for (int i = page * count; i < Math.min(rows.size(), (page + 1) * count); i++) {
            CompoundTag row = rows.get(i); String label = row.getString("Name") + (row.getBoolean("Unlocked") ? " · owned" : " · " + row.getInt("Cost") + " req");
            Button choice = button(clip(label, span() - 16), left(), 89 + (i - page * count) * 22, span(), () -> { selected = row.getString("Id"); change(View.CALLIN); });
            tip(choice, row.getString("Name") + "\n" + row.getString("Description") + "\n" + row.getString("Reason"));
        }
        pages(rows.size(), count);
    }
    private void initCallin() {
        CompoundTag row = find(ship().getList("Callins", Tag.TAG_COMPOUND), selected);
        if (row.getBoolean("Unlocked")) {
            int quarter = (span() - 12) / 4;
            for (int slot = 0; slot < 4; slot++) {
                final int target = slot;
                Button equip = button("Slot " + (slot + 1), left() + slot * (quarter + 4), actionY(), slot == 3 ? span() - 3 * (quarter + 4) : quarter,
                        () -> send("ship_equip", selected + "|" + target));
                equip.active = !pending && row.getBoolean("CanEquip") && (row.getInt("EquippedSlot") < 0 || row.getInt("EquippedSlot") == slot);
            }
        } else {
            Button buy = button("Unlock · " + row.getInt("Cost") + " requisitions", left(), actionY(), span(), () -> send("ship_purchase", selected));
            buy.active = !pending && row.getBoolean("CanPurchase");
        }
    }
    private void initShip() {
        ListTag modules = ship().getList("Modules", Tag.TAG_COMPOUND);
        for (int i = 0; i < Math.min(4, modules.size()); i++) {
            CompoundTag row = modules.getCompound(i);
            Button module = button(clip(row.getString("Name") + (row.getBoolean("Unlocked") ? " · installed" : " · " + row.getInt("Cost") + " req"), span() - 14),
                    left(), 83 + i * 23, span(), () -> { selected = row.getString("Id"); change(View.MODULE); });
            tip(module, row.getString("Description"));
        }
        int half = (span() - 4) / 2;
        button("Weapons & armor", left(), actionY(), half, () -> change(View.ARMORY));
        button(state().getBoolean("InShip") ? "Return to Minecraft" : "Board ship", left() + half + 4, actionY(), span() - half - 4,
                () -> { if (state().getBoolean("InShip")) send("abort", ""); else send("ship", ""); }).active = !pending && !state().getBoolean("Active");
    }
    private void initModule() {
        CompoundTag row = find(ship().getList("Modules", Tag.TAG_COMPOUND), selected);
        Button buy = button(row.getBoolean("Unlocked") ? "Installed" : "Install · " + row.getInt("Cost") + " requisitions", left(), actionY(), span(), () -> send("ship_purchase", selected));
        buy.active = !pending && row.getBoolean("CanPurchase");
    }
    private void initArmory() {
        int third = (span() - 8) / 3;
        for (int i = 0; i < 3; i++) {
            String slot = new String[]{"primary", "secondary", "armor"}[i];
            button(Character.toUpperCase(slot.charAt(0)) + slot.substring(1), left() + i * (third + 4), 63, i == 2 ? span() - 2 * (third + 4) : third,
                    () -> { armorySlot = slot; query = ""; page = 0; rebuildWidgets(); });
        }
        search(87); List<CompoundTag> rows = filtered(armory().getList(armorySlot.equals("armor") ? "Armors" : "Guns", Tag.TAG_COMPOUND));
        int count = Math.max(1, (height - 177) / 22); normalizePage(rows.size(), count);
        String current = armory().getString(Character.toUpperCase(armorySlot.charAt(0)) + armorySlot.substring(1));
        for (int i = page * count; i < Math.min(rows.size(), (page + 1) * count); i++) {
            CompoundTag row = rows.get(i); String id = row.getString("Id");
            Button choice = button(clip((id.equals(current) ? "[SET] " : "") + name(row), span() - 16), left(), 112 + (i - page * count) * 22, span(),
                    () -> send("armory_select", armorySlot + "|" + id));
            choice.active = !pending && armory().getBoolean("CanManage") && (armorySlot.equals("armor") || row.getBoolean("Ready"));
            tip(choice, name(row) + "\n" + (armorySlot.equals("armor") ? row.getString("Description")
                    : row.getString("Id") + " · " + row.getString("Type") + "\n" + row.getInt("Capacity") + " loaded · " + row.getString("Ammo") + "\n" + row.getString("Reason")));
        }
        pages(rows.size(), count);
    }
    private void initField() {
        List<CompoundTag> rows = new ArrayList<>();
        for (Tag value : state().getList("Stratagems", Tag.TAG_COMPOUND)) {
            CompoundTag row = (CompoundTag) value; String category = row.getString("Category").toLowerCase(Locale.ROOT);
            if (row.getBoolean("Equipped") || category.equals("shared") || category.equals("mission") && row.getBoolean("Ready")) rows.add(row);
        }
        int count = Math.max(1, (height - 174) / 22); normalizePage(rows.size(), count);
        for (int i = page * count; i < Math.min(rows.size(), (page + 1) * count); i++) {
            CompoundTag row = rows.get(i); String id = row.getString("Id");
            String status = row.getBoolean("Ready") ? "ready" : row.getLong("Cooldown") > 0 ? (row.getLong("Cooldown") + 19) / 20 + "s" : "unavailable";
            Button call = button(clip(row.getString("Name") + " · " + status, span() - 16), left(), 89 + (i - page * count) * 22, span(), () -> send("callin", id));
            call.active = !pending && state().getBoolean("Active") && !state().getString("Phase").equals("DOWNED") && row.getBoolean("Ready");
            tip(call, row.getString("Description") + "\n" + row.getString("Controls") + "\nUses: "
                    + (row.getInt("Uses") < 0 ? "unlimited" : row.getInt("Uses"))
                    + (row.getInt("Ammunition") >= 0 ? " · Eagle charges: " + row.getInt("Ammunition") : ""));
        }
        int third = (span() - 8) / 3;
        button("< Radio", left(), height - 73, third, () -> { page = Math.max(0, page - 1); rebuildWidgets(); });
        button("Radio >", left() + third + 4, height - 73, third, () -> { page++; normalizePage(rows.size(), count); rebuildWidgets(); });
        button("Terminal", left() + 2 * (third + 4), height - 73, span() - 2 * (third + 4), () -> send("terminal", ""))
                .active = !pending && state().getBoolean("Active") && !state().getString("Phase").equals("DOWNED");
        boolean downed = state().getString("Phase").equals("DOWNED"), board = state().getBoolean("CanBoard");
        Button objective = button(downed ? "Reinforce · " + state().getInt("Reinforcements") : board ? "Board extraction" : "Call extraction",
                left(), actionY(), (span() - 4) / 2, () -> send(downed ? "reinforce" : board ? "board" : "extract", ""));
        objective.active = !pending && (downed ? state().getInt("Reinforcements") > 0 : board || state().getBoolean("CanExtract"));
        button("Return home", left() + (span() - 4) / 2 + 4, actionY(), span() - (span() - 4) / 2 - 4, this::confirmReturn)
                .active = !pending && state().getBoolean("CanAbort");
    }

    @Override public void render(GuiGraphics gui, int mouseX, int mouseY, float partialTick) {
        renderBackground(gui); gui.fill(left() - 4, 4, left() + span() + 4, height - 4, 0xEC141B24);
        gui.fill(left() - 4, 4, left() + span() + 4, 6, GOLD);
        gui.drawString(font, "LIBERTY GRIND", left(), 12, GOLD, false);
        String balance = ship().getInt("Requisitions") + " req";
        gui.drawString(font, balance, left() + span() - font.width(balance), 12, GOLD, false);
        String status = pending ? "Waiting for ship command…" : state().getBoolean("InShip") ? "Aboard your ship · deploy again or return home"
                : state().getBoolean("Active") ? state().getString("Phase") + " · " + LibertyClient.clock(state().getInt("Seconds")) + " · " + primaryProgress()
                : "Select a region, review your loadout, then drop";
        gui.drawString(font, clip(status, span()), left(), 25, DIM, false);
        switch (view) {
            case MAP -> renderMap(gui); case PLAN -> renderPlan(gui); case REVIEW -> drawLines(gui, reviewLines(), 64, height - 80);
            case DEFENSES -> renderDefenses(gui);
            case CALLINS -> pageLabel(gui, filtered(ship().getList("Callins", Tag.TAG_COMPOUND)).size(), listCount());
            case CALLIN -> drawLines(gui, callinLines(), 65, height - 57);
            case SHIP -> gui.drawString(font, "SHIP HARDWARE · " + ship().getInt("Requisitions") + " requisitions", left(), 65, CYAN, false);
            case MODULE -> { CompoundTag row = find(ship().getList("Modules", Tag.TAG_COMPOUND), selected); drawLines(gui, lines(row.getString("Name"), row.getString("Description"), row.getString("Reason")), 65, height - 57); }
            case ARMORY -> {
                pageLabel(gui, filtered(armory().getList(armorySlot.equals("armor") ? "Armors" : "Guns", Tag.TAG_COMPOUND)).size(), Math.max(1, (height - 177) / 22));
                if (armorySlot.equals("armor")) {
                    CompoundTag worn = find(armory().getList("Armors", Tag.TAG_COMPOUND), armory().getString("Armor"));
                    gui.drawString(font, clip(worn.getString("Name"), span()), left(), height - 82, CYAN, false);
                    int speed = (int) Math.round(worn.getDouble("WalkSpeed") * 100);
                    String stats = worn.getInt("ArmorPoints") + " armor · " + (int) worn.getDouble("Toughness") + " toughness · "
                            + (speed > 0 ? "+" : "") + speed + "% walk/skate";
                    gui.drawString(font, clip(stats, span()), left(), height - 69, DIM, false);
                }
            }
            case FIELD -> renderField(gui);
        }
        super.render(gui, mouseX, mouseY, partialTick);
        if (view == View.FIELD && mouseX >= left() && mouseX <= left() + span() && mouseY >= 63 && mouseY < 86) {
            CompoundTag next = currentObjective();
            if (!next.isEmpty()) gui.renderTooltip(font, Component.literal(next.getString("Description")), mouseX, mouseY);
        }
    }
    private void renderMap(GuiGraphics gui) {
        ListTag rows = campaign().getList("Sectors", Tag.TAG_COMPOUND);
        int card = mapCardWidth(), gap = mapGap(), lane = mapLaneHeight();
        for (int i = 0; i < Math.min(3, rows.size()); i++) {
            CompoundTag root = rows.getCompound(i), frontier = rows.size() > i + 3 ? rows.getCompound(i + 3) : new CompoundTag();
            int y = 62 + i * lane, color = factionColor(root.getString("Faction"));
            gui.drawString(font, factionName(root.getString("Faction")).toUpperCase(Locale.ROOT) + " FRONT", left(), y, color, false);
            int middle = left() + card + gap / 2, linkY = y + 12 + (lane - 14) / 2 + 5;
            int routeColor = frontier.getBoolean("Unlocked") ? color : 0xFF657080;
            for (int x = left() + card + 2; x < left() + card + gap - 5; x += 5)
                gui.fill(x, linkY, Math.min(x + (frontier.getBoolean("Unlocked") ? 5 : 3), left() + card + gap - 5), linkY + 2, routeColor);
            int arrow = left() + card + gap - 5;
            for (int part = 0; part < 4; part++) gui.fill(arrow - part, linkY - part, arrow - part + 1, linkY + part + 2, routeColor);
            gui.drawCenteredString(font, frontier.getBoolean("Unlocked") ? "OPEN" : "20%", middle, linkY - 12, routeColor);
        }
        if (rows.isEmpty()) drawLines(gui, lines("Campaign unavailable", state().getString("Status"), campaign().getString("Reason")), 66, height - 57);
    }
    private void renderPlan(GuiGraphics gui) {
        CompoundTag region = find(campaign().getList("Sectors", Tag.TAG_COMPOUND), sector), offer = defenseRow();
        gui.drawString(font, clip(defense.isEmpty() ? region.getString("Name") + " · " + factionName(region.getString("Faction")) : offer.getString("Name"), span()), left(), 65, GOLD, false);
        CompoundTag chosen = chosenMission(); String name = defense.isEmpty() ? chosen.getString("Name") : "Explicit home / regional defense";
        gui.drawCenteredString(font, clip(name, span() - 54), left() + span() / 2, 89, WHITE);
        List<FormattedCharSequence> description = font.split(Component.literal(defense.isEmpty() ? chosen.getString("Description") : offer.getString("Description")), span());
        for (int i = 0; i < Math.min(2, description.size()); i++) gui.drawString(font, description.get(i), left(), 110 + i * 10, DIM, false);
        gui.drawString(font, "Difficulty " + difficulty + "/10", left() + 29, 143, WHITE, false);
        gui.drawString(font, clip(routeAvailable() ? "Every difficulty is selectable. Review all four call-ins before launch." : routeReason(), span()), left(), 166, DIM, false);
    }
    private void renderDefenses(GuiGraphics gui) {
        ListTag rows = campaign().getList("Defenses", Tag.TAG_COMPOUND); int count = Math.max(1, (height - 126) / 32);
        for (int i = page * count; i < Math.min(rows.size(), (page + 1) * count); i++) {
            CompoundTag row = rows.getCompound(i); String time = row.getBoolean("HomeWorld") ? "Optional · no deadline or automatic invasion" : "Active campaign time left: " + LibertyClient.clock((int) Math.min(Integer.MAX_VALUE, row.getLong("TicksLeft") / 20));
            gui.drawString(font, clip(time, span()), left(), 91 + (i - page * count) * 32, DIM, false);
        }
        if (rows.isEmpty()) drawLines(gui, lines("No defense alerts", "Defense losses create optional operations. Your ordinary world is never invaded without an explicit deployment."), 69, height - 57);
        pageLabel(gui, rows.size(), count);
    }
    private void renderField(GuiGraphics gui) {
        CompoundTag next = currentObjective();
        String title = next.isEmpty() ? state().getString("Status") : next.getString("Name") + " · " + next.getInt("Progress") + "/" + Math.max(1, next.getInt("Goal"));
        String navigation = "Aim at a safe target before calling support.";
        if (!next.isEmpty()) navigation = LibertyClient.distance(next.getDouble("Distance")) + "m · X " + next.getInt("X") + " / Z " + next.getInt("Z") + " · hover for objective";
        else if (state().contains("ExtractionDistance")) navigation = "Extraction · " + LibertyClient.distance(state().getDouble("ExtractionDistance"))
                + "m · X " + state().getInt("ExtractionX") + " / Z " + state().getInt("ExtractionZ");
        gui.drawString(font, clip(title, span()), left(), 65, CYAN, false);
        gui.drawString(font, clip(navigation, span()), left(), 76, DIM, false);
    }
    private CompoundTag currentObjective() {
        CompoundTag best = new CompoundTag(); int bestRank = Integer.MAX_VALUE;
        for (Tag value : state().getList("Objectives", Tag.TAG_COMPOUND)) {
            CompoundTag row = (CompoundTag) value;
            if (row.getBoolean("Complete") || row.contains("Active") && !row.getBoolean("Active")) continue;
            int rank = row.getBoolean("Priority") ? 0 : row.getBoolean("Optional") ? 2 : 1;
            if (rank < bestRank || rank == bestRank && row.getDouble("Distance") < best.getDouble("Distance")) { best = row; bestRank = rank; }
        }
        return best;
    }
    private String primaryProgress() {
        int completed = 0, total = 0;
        for (Tag value : state().getList("Objectives", Tag.TAG_COMPOUND)) {
            CompoundTag row = (CompoundTag) value; if (row.getBoolean("Optional")) continue;
            total++; if (row.getBoolean("Complete")) completed++;
        }
        return completed + "/" + total + " secured";
    }
    private List<Line> reviewLines() {
        List<Line> lines = new ArrayList<>(); addCompact(lines, defense.isEmpty() ? chosenMission().getString("Name") : defenseRow().getString("Name"), GOLD);
        addCompact(lines, find(campaign().getList("Sectors", Tag.TAG_COMPOUND), sector).getString("Name") + " · difficulty " + difficulty + " · " + minutes + " minutes", DIM);
        addCompact(lines, "Primary: " + selectedGunName(armory().getString("Primary")), WHITE);
        addCompact(lines, "Secondary: " + selectedGunName(armory().getString("Secondary")), WHITE);
        addCompact(lines, "Armor: " + find(armory().getList("Armors", Tag.TAG_COMPOUND), armory().getString("Armor")).getString("Name"), WHITE);
        ListTag equipped = ship().getList("Equipped", Tag.TAG_STRING);
        for (int i = 0; i < equipped.size(); i++) addCompact(lines, "Call-in " + (i + 1) + ": " + find(ship().getList("Callins", Tag.TAG_COMPOUND), equipped.getString(i)).getString("Name"), CYAN);
        lines.add(new Line(Component.literal("").getVisualOrderText(), DIM));
        add(lines, "Weapons, ammunition and armor are mission loans. Your original inventory, XP and world are restored on return.", DIM);
        if (!routeAvailable()) add(lines, routeReason(), 0xFFFF9B85);
        if (!armory().getBoolean("Ready")) add(lines, armory().getString("Reason"), 0xFFFF9B85);
        return lines;
    }
    private List<Line> callinLines() {
        CompoundTag row = find(ship().getList("Callins", Tag.TAG_COMPOUND), selected); List<Line> lines = new ArrayList<>();
        add(lines, row.getString("Name"), GOLD); add(lines, row.getString("Description"), WHITE); add(lines, row.getString("Controls"), CYAN);
        add(lines, "Cooldown " + row.getInt("CooldownTicks") / 20 + "s · " + (row.getInt("MissionUses") == 0 ? "reusable" : row.getInt("MissionUses") + " mission uses")
                + (row.getInt("EagleCharges") > 0 ? " · " + row.getInt("EagleCharges") + " Eagle charges" : ""), DIM);
        add(lines, "Requisitions: " + ship().getInt("Requisitions") + " · cost " + row.getInt("Cost"), GOLD);
        if (row.getInt("EquippedSlot") >= 0) add(lines, "Currently equipped in slot " + (row.getInt("EquippedSlot") + 1), CYAN);
        if (!row.getString("Reason").isEmpty()) add(lines, row.getString("Reason"), 0xFFFF9B85);
        return lines;
    }
    private List<Line> lines(String... text) { List<Line> out = new ArrayList<>(); for (int i = 0; i < text.length; i++) add(out, text[i], i == 0 ? GOLD : WHITE); return out; }
    private void addCompact(List<Line> out, String text, int color) { for (FormattedCharSequence line : font.split(Component.literal(text), span() - 14)) out.add(new Line(line, color)); }
    private void add(List<Line> out, String text, int color) { if (!text.isEmpty()) { for (FormattedCharSequence line : font.split(Component.literal(text), span() - 14)) out.add(new Line(line, color)); out.add(new Line(Component.literal("").getVisualOrderText(), color)); } }
    private void drawLines(GuiGraphics gui, List<Line> lines, int top, int bottom) {
        int lineHeight = view == View.REVIEW ? 10 : 11;
        contentHeight = lines.size() * lineHeight; scroll = Math.max(0, Math.min(scroll, Math.max(0, contentHeight - (bottom - top))));
        gui.enableScissor(left(), top, left() + span(), bottom);
        for (int i = 0; i < lines.size(); i++) gui.drawString(font, lines.get(i).text, left() + 3, top + i * lineHeight - scroll, lines.get(i).color, false);
        gui.disableScissor();
        if (contentHeight > bottom - top) {
            int track = bottom - top, thumb = Math.max(10, track * track / contentHeight), y = top + (track - thumb) * scroll / Math.max(1, contentHeight - track);
            gui.fill(left() + span() - 3, top, left() + span(), bottom, 0xFF354050); gui.fill(left() + span() - 3, y, left() + span(), y + thumb, CYAN);
        }
    }
    private void search(int y) {
        filter = new EditBox(font, left(), y, span(), 18, Component.literal("Search names, categories or IDs"));
        filter.setMaxLength(80); filter.setValue(query); filter.setHint(Component.literal("Search names, categories or IDs"));
        filter.setSuggestion(query.isEmpty() ? "Search names, categories or IDs" : null);
        filter.setResponder(value -> { query = value; page = 0; refreshRows = true; }); addRenderableWidget(filter); setInitialFocus(filter);
    }
    private List<CompoundTag> filtered(ListTag rows) {
        List<CompoundTag> result = new ArrayList<>(); String needle = query.toLowerCase(Locale.ROOT).trim();
        for (Tag value : rows) { CompoundTag row = (CompoundTag) value; if (needle.isEmpty() || (name(row) + " " + row.getString("Id") + " " + row.getString("Category") + " " + row.getString("Type")).toLowerCase(Locale.ROOT).contains(needle)) result.add(row); }
        return result;
    }
    private String name(CompoundTag row) {
        String key = row.getString("NameKey");
        if (!key.isBlank()) { String translated = Component.translatable(key).getString(); if (!translated.equals(key)) return translated; }
        return row.getString("Name");
    }
    private String selectedGunName(String id) { CompoundTag row = find(armory().getList("Guns", Tag.TAG_COMPOUND), id); return row.isEmpty() ? id : name(row); }
    private List<CompoundTag> missions() {
        String faction = find(campaign().getList("Sectors", Tag.TAG_COMPOUND), sector).getString("Faction"); List<CompoundTag> result = new ArrayList<>();
        for (Tag value : state().getList("Missions", Tag.TAG_COMPOUND)) {
            CompoundTag row = (CompoundTag) value; ListTag factions = row.getList("Factions", Tag.TAG_STRING);
            for (int i = 0; i < factions.size(); i++) if (factions.getString(i).equalsIgnoreCase(faction)) { result.add(row); break; }
        }
        return result;
    }
    private void selectFirstMission() { List<CompoundTag> rows = missions(); if (rows.stream().noneMatch(row -> row.getString("Id").equals(mission))) mission = rows.isEmpty() ? "" : rows.get(0).getString("Id"); }
    private CompoundTag chosenMission() { return missions().stream().filter(row -> row.getString("Id").equals(mission)).findFirst().orElse(new CompoundTag()); }
    private void cycleMission(int delta) { List<CompoundTag> rows = missions(); if (rows.isEmpty()) return; int current = 0; for (int i = 0; i < rows.size(); i++) if (rows.get(i).getString("Id").equals(mission)) current = i; mission = rows.get(Math.floorMod(current + delta, rows.size())).getString("Id"); rebuildWidgets(); }
    private CompoundTag defenseRow() { for (Tag value : campaign().getList("Defenses", Tag.TAG_COMPOUND)) { CompoundTag row = (CompoundTag) value; if (row.hasUUID("Id") && row.getUUID("Id").toString().equals(defense)) return row; } return new CompoundTag(); }
    private boolean routeAvailable() { return defense.isEmpty() ? find(campaign().getList("Sectors", Tag.TAG_COMPOUND), sector).getBoolean("CanDeploy") && !chosenMission().isEmpty() : defenseRow().getBoolean("CanDeploy"); }
    private String routeReason() { return defense.isEmpty() ? find(campaign().getList("Sectors", Tag.TAG_COMPOUND), sector).getString("Reason") : "This defense offer is already assigned or no longer available."; }
    private void pages(int total, int count) { normalizePage(total, count); button("Previous", left(), actionY(), 82, () -> { page = Math.max(0, page - 1); rebuildWidgets(); }).active = page > 0; button("Next", left() + span() - 82, actionY(), 82, () -> { page++; normalizePage(total, count); rebuildWidgets(); }).active = (page + 1) * count < total; }
    private void pageLabel(GuiGraphics gui, int total, int count) { gui.drawCenteredString(font, (page + 1) + " / " + Math.max(1, (total + count - 1) / count) + " · " + total + " entries", left() + span() / 2, actionY() + 6, DIM); }
    private void normalizePage(int total, int count) { page = Math.max(0, Math.min(page, Math.max(0, (total - 1) / count))); }
    private int listCount() { return Math.max(1, (height - 153) / 22); }
    private void confirmReturn() {
        if (!state().getBoolean("Active")) { send("abort", ""); return; }
        minecraft.setScreen(new ConfirmScreen(answer -> { minecraft.setScreen(this); if (answer) send("abort", ""); },
                Component.literal("Return to Minecraft?"), Component.literal("Your original world and belongings will be restored. Unextracted samples stay behind.")));
    }
    private void change(View next) {
        if (next == View.ARMORY && view != View.ARMORY) armoryReturn = view == View.REVIEW ? View.REVIEW : View.SHIP;
        if (next == View.CALLINS && view != View.CALLIN && view != View.CALLINS) callinsReturn = view == View.REVIEW ? View.REVIEW : View.MAP;
        view = next; page = scroll = 0; query = ""; contentHeight = 0; rebuildWidgets();
    }
    private void back() { change(switch (view) {
        case REVIEW -> View.PLAN; case PLAN, DEFENSES -> View.MAP; case CALLIN -> View.CALLINS; case MODULE -> View.SHIP;
        case ARMORY -> armoryReturn; case CALLINS -> callinsReturn; default -> View.MAP;
    }); }
    private void send(String action, String value) { if (pending) return; pending = true; pendingTicks = 0; LibertyClient.request(action, value); rebuildWidgets(); }
    @Override public void tick() { if (filter != null) filter.tick(); if (refreshRows) { refreshRows = false; rebuildWidgets(); } if (pending && ++pendingTicks >= 100) { pending = false; rebuildWidgets(); } }
    @Override public boolean mouseScrolled(double x, double y, double delta) {
        if (view == View.REVIEW || view == View.CALLIN || view == View.MODULE) { scroll = Math.max(0, scroll - (int) (delta * 22)); return true; }
        if (view == View.CALLINS || view == View.ARMORY || view == View.DEFENSES || view == View.FIELD) { page = Math.max(0, page + (delta < 0 ? 1 : -1)); rebuildWidgets(); return true; }
        return super.mouseScrolled(x, y, delta);
    }
    @Override public boolean isPauseScreen() { return false; }
    /** The six real server regions are keyboard-accessible Buttons, drawn as the front-line map itself. */
    private final class RegionButton extends Button {
        private final CompoundTag region;
        RegionButton(int x, int y, int width, int height, CompoundTag region, Runnable action) {
            super(x, y, width, height, Component.literal(region.getString("Name")), ignored -> action.run(), DEFAULT_NARRATION);
            this.region = region;
        }
        @Override protected void renderWidget(GuiGraphics gui, int mouseX, int mouseY, float partialTick) {
            int x = getX(), y = getY(), width = getWidth(), height = getHeight();
            boolean open = region.getBoolean("Unlocked"), hover = isHoveredOrFocused() && active;
            int faction = factionColor(region.getString("Faction")), accent = open ? faction : shade(faction, .55);
            gui.fill(x, y, x + width, y + height, hover ? 0xFF2B3B4D : 0xFF1D2937);
            int border = hover ? WHITE : accent;
            gui.fill(x, y, x + width, y + 1, border); gui.fill(x, y + height - 1, x + width, y + height, border);
            gui.fill(x, y, x + 1, y + height, border); gui.fill(x + width - 1, y, x + width, y + height, border);
            boolean roomy = height >= 52 && width >= 190;
            int radius = roomy ? Math.min(19, height / 3) : 9;
            int centerX = x + (roomy ? 26 : 13), centerY = y + height / 2;
            planet(gui, centerX, centerY, radius, accent, open);
            int textX = x + (roomy ? 51 : 26), textWidth = x + width - textX - 5;
            gui.drawString(font, clip(region.getString("Name"), textWidth), textX, y + (roomy ? 8 : 3), open ? WHITE : DIM, false);
            String stats = region.getInt("Liberation") / 100 + "% · threat " + region.getInt("Threat");
            gui.drawString(font, clip(stats, textWidth), textX, y + (roomy ? 25 : 14), open ? faction : DIM, false);
            if (roomy) {
                CompoundTag parent = find(campaign().getList("Sectors", Tag.TAG_COMPOUND), region.getString("Parent"));
                String condition = open ? "Open region · choose operation" : "Need 20% in " + parent.getString("Name");
                gui.drawString(font, clip(condition, textWidth), textX, y + 40, DIM, false);
            }
            int barY = y + height - (roomy ? 10 : 3), barHeight = roomy ? 4 : 1;
            gui.fill(textX, barY, x + width - 5, barY + barHeight, 0xFF101923);
            int filled = textWidth * Math.min(10_000, Math.max(0, region.getInt("Liberation"))) / 10_000;
            if (filled > 0) gui.fill(textX, barY, textX + filled, barY + barHeight, faction);
        }
    }
    private static void planet(GuiGraphics gui, int x, int y, int radius, int color, boolean open) {
        for (int row = -radius; row <= radius; row++) {
            int half = (int) Math.sqrt(radius * radius - row * row);
            gui.fill(x - half, y + row, x + half + 1, y + row + 1, shade(color, row < 0 ? .8 : .55));
        }
        gui.fill(x - radius + 2, y - radius / 3, x + radius - 1, y - radius / 3 + 1, shade(color, 1.2));
        gui.fill(x - radius + 2, y + radius / 3, x + radius - 1, y + radius / 3 + 1, shade(color, .9));
        if (!open) {
            gui.fill(x - 3, y - 5, x + 4, y - 4, DIM); gui.fill(x - 4, y - 4, x - 2, y, DIM); gui.fill(x + 2, y - 4, x + 4, y, DIM);
            gui.fill(x - 5, y, x + 5, y + 6, DIM); gui.fill(x - 1, y + 2, x + 1, y + 5, 0xFF263345);
        } else {
            gui.fill(x - 2, y - 2, x + 3, y + 3, 0xFFEFF6FF);
            gui.fill(x - 4, y, x + 5, y + 1, 0xFFEFF6FF); gui.fill(x, y - 4, x + 1, y + 5, 0xFFEFF6FF);
        }
    }
    private static int shade(int color, double factor) {
        int r = Math.min(255, (int) ((color >> 16 & 255) * factor)), g = Math.min(255, (int) ((color >> 8 & 255) * factor)), b = Math.min(255, (int) ((color & 255) * factor));
        return 0xFF000000 | r << 16 | g << 8 | b;
    }
    private static int factionColor(String faction) { return switch (faction) { case "terminid" -> 0xFFBDDB62; case "automaton" -> 0xFFEE8B84; case "illuminate" -> 0xFFBF9BF5; default -> CYAN; }; }
    private static String factionName(String faction) { return faction.isBlank() ? "Campaign" : Character.toUpperCase(faction.charAt(0)) + faction.substring(1); }
    private int mapGap() { return Math.max(36, Math.min(56, span() / 12)); }
    private int mapCardWidth() { return (span() - mapGap()) / 2; }
    private int mapLaneHeight() { return Math.max(41, (actionY() - 66) / 3); }
    private Button button(String text, int x, int y, int width, Runnable action) { return addRenderableWidget(Button.builder(Component.literal(clip(text, Math.max(10, width - 10))), ignored -> action.run()).bounds(x, y, Math.max(20, width), 18).build()); }
    private static void tip(Button button, String text) { button.setTooltip(Tooltip.create(Component.literal(text))); }
    private String clip(String text, int width) { return LibertyClient.clipped(text, width); }
    private int span() { return Math.max(240, Math.min(760, width - 16)); }
    private int left() { return (width - span()) / 2; }
    private int actionY() { return height - 49; }
    private static CompoundTag state() { return LibertyClient.state(); }
    private static CompoundTag campaign() { return state().getCompound("Campaign"); }
    private static CompoundTag ship() { return state().getCompound("Ship"); }
    private static CompoundTag armory() { return state().getCompound("Armory"); }
    private static CompoundTag find(ListTag rows, String id) { for (Tag value : rows) { CompoundTag row = (CompoundTag) value; if (row.getString("Id").equals(id)) return row; } return new CompoundTag(); }
}
