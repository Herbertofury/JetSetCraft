package com.herberto.jetsetcraft.client.liberty;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.screens.ConfirmScreen;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.util.FormattedCharSequence;
import org.lwjgl.glfw.GLFW;

import java.util.ArrayList;
import java.util.List;

/** Native Minecraft war map and research bench. Small windows keep all details reachable by scrolling. */
final class LibertyScreen extends Screen {
    private enum View { WAR_MAP, THEATER, MISSION, RESEARCH, PROJECT }
    private record TextLine(FormattedCharSequence text, int color) {}
    private View view;
    private String theater = "", project = "";
    private int page, scroll, contentHeight, pendingTicks;
    private boolean pending;
    private String waiting = "";

    LibertyScreen() {
        super(Component.literal("Liberty Grind"));
        view = LibertyClient.state().getBoolean("Active") ? View.MISSION : View.WAR_MAP;
    }

    void onSnapshot(CompoundTag incoming, CompoundTag previous) {
        boolean started = incoming.getBoolean("Active") && !previous.getBoolean("Active");
        boolean changed = incoming.getBoolean("Open") || incoming.getLong("Revision") != previous.getLong("Revision")
                || incoming.getCompound("Research").getLong("Revision") != previous.getCompound("Research").getLong("Revision")
                || incoming.getBoolean("Active") != previous.getBoolean("Active")
                || !incoming.getString("Status").equals(previous.getString("Status"));
        if (changed) { pending = false; pendingTicks = 0; waiting = ""; }
        if (started) { minecraft.setScreen(null); return; }
        if (!incoming.getBoolean("Active") && previous.getBoolean("Active")) { view = View.WAR_MAP; scroll = 0; }
        if (changed || incoming.getBoolean("CanExtract") != previous.getBoolean("CanExtract")
                || incoming.getBoolean("CanBoard") != previous.getBoolean("CanBoard")
                || incoming.getBoolean("CanResupply") != previous.getBoolean("CanResupply")) rebuildWidgets();
    }

    @Override protected void init() {
        int left = left(), span = span(), half = (span - 4) / 2;
        boolean active = LibertyClient.state().getBoolean("Active");
        button(active ? "Mission" : "War map", left, 39, half, () -> change(active ? View.MISSION : View.WAR_MAP));
        button("Research", left + half + 4, 39, span - half - 4, () -> change(View.RESEARCH));
        int quarter = (span - 12) / 4;
        button("Back", left, height - 26, quarter, this::back);
        button("Refresh", left + quarter + 4, height - 26, quarter, () -> send("open", "")).active = !pending;
        button("HUD", left + 2 * (quarter + 4), height - 26, quarter, () -> minecraft.setScreen(new LibertyHudScreen(this)));
        button("Done", left + 3 * (quarter + 4), height - 26, span - 3 * (quarter + 4), this::onClose);
        switch (view) {
            case WAR_MAP -> initMap();
            case THEATER -> {
                CompoundTag selected = find(LibertyClient.theaters(), theater);
                Button deploy = button("Deploy in pod", left, height - 51, span, () -> send("deploy", theater));
                deploy.active = !pending && !active && !selected.isEmpty() && selected.getBoolean("Available");
                deploy.setTooltip(Tooltip.create(Component.literal(selected.getBoolean("Available")
                        ? "Save your normal-world state, borrow the mission loadout and drop into this district."
                        : reason(selected, "This district is unavailable. Refresh to check its dependencies."))));
            }
            case MISSION -> initMission();
            case RESEARCH -> initResearch();
            case PROJECT -> initProject();
        }
    }

    private void initMap() {
        ListTag rows = LibertyClient.theaters();
        int start = page * 3;
        for (int i = start; i < Math.min(rows.size(), start + 3); i++) {
            CompoundTag row = rows.getCompound(i);
            String id = row.getString("Id");
            Button district = button(row.getString("Name"), left() + 18, 70 + (i - start) * 35, span() - 18,
                    () -> { theater = id; change(View.THEATER); });
            district.setTooltip(Tooltip.create(Component.literal(row.getString("Name") + "\n" + row.getString("Description")
                    + (row.getString("Reason").isBlank() ? "" : "\n" + row.getString("Reason")))));
        }
        if (LibertyClient.state().getBoolean("CanAbort")) {
            Button recover = button("Recover return to Minecraft", left(), height - 51, span(), () -> send("abort", ""));
            recover.active = !pending;
            recover.setTooltip(Tooltip.create(Component.literal("Retry the retained return checkpoint. Your saved original world and belongings are kept until restoration succeeds.")));
        } else if (rows.size() > 3) pages(rows.size());
    }

    private void initMission() {
        CompoundTag state = LibertyClient.state();
        int third = (span() - 8) / 3;
        boolean board = state.getBoolean("CanBoard");
        Button extract = button(board ? "Board ship" : "Call extraction", left(), height - 51, third,
                () -> send(board ? "board" : "extract", ""));
        extract.active = !pending && (board || state.getBoolean("CanExtract"));
        extract.setTooltip(Tooltip.create(Component.literal(board ? "Board the arrived ship to return to your original world."
                : "Finish the objectives, reach the extraction pad, then call the ship and hold the landing zone.")));
        Button supply = button("Resupply", left() + third + 4, height - 51, third, () -> send("resupply", ""));
        supply.active = !pending && state.getBoolean("CanResupply");
        supply.setTooltip(Tooltip.create(Component.literal("Use an available mission resupply. Supply Skates research adds one charge.")));
        boolean retryReturn = state.getString("Phase").equals("RETURNING");
        Button abort = button(retryReturn ? "Retry return" : "Return early", left() + 2 * (third + 4), height - 51, span() - 2 * (third + 4), () -> {
            if (retryReturn) { send("abort", ""); return; }
            minecraft.setScreen(new ConfirmScreen(confirmed -> {
                    minecraft.setScreen(this);
                    if (confirmed) send("abort", "");
                }, Component.literal("Return before extraction?"),
                        Component.literal("Your original world and belongings will be restored. Unextracted samples and completion rewards will not be earned.")));
        });
        abort.active = !pending && state.getBoolean("CanAbort");
    }

    private void initResearch() {
        ListTag projects = projects();
        int start = page * 3;
        for (int i = start; i < Math.min(projects.size(), start + 3); i++) {
            CompoundTag row = projects.getCompound(i);
            String id = row.getString("Id");
            String label = (row.getBoolean("Selected") ? "Selected · " : row.getBoolean("Unlocked") ? "Learned · " : "") + row.getString("Name");
            Button entry = button(label, left(), 70 + (i - start) * 35, span(), () -> { project = id; change(View.PROJECT); });
            entry.setTooltip(Tooltip.create(Component.literal(row.getString("Name") + "\n" + row.getString("Effect") + "\n" + cost(row))));
        }
        pages(projects.size());
    }

    private void initProject() {
        CompoundTag row = find(projects(), project), research = LibertyClient.research();
        String label = !row.getBoolean("Unlocked") ? "Research upgrade"
                : row.getBoolean("Preparation") ? row.getBoolean("Selected") ? "Use Style Library / home instead" : "Select for Rival Rush"
                : "Learned · active automatically";
        Button purchase = button(label, left(), height - 51, span(), () -> {
            if (!row.getBoolean("Unlocked")) send("research", project);
            else send("research_select", row.getBoolean("Selected") ? "NONE" : project);
        });
        purchase.active = !pending && !row.isEmpty() && (!row.getBoolean("Unlocked") ? row.getBoolean("CanPurchase")
                : row.getBoolean("Preparation") && research.getBoolean("CanManage"));
        purchase.setTooltip(Tooltip.create(Component.literal(row.getBoolean("Unlocked") && row.getBoolean("Preparation")
                ? "Choose exactly one starting preparation. This replaces your selected Style Library or home preparation for Rival Rush."
                : row.getString("Effect") + "\n" + cost(row))));
    }

    private void pages(int count) {
        int total = Math.max(1, (count + 2) / 3);
        page = Math.max(0, Math.min(total - 1, page));
        int half = (span() - 4) / 2;
        button("Previous", left(), height - 51, half, () -> { page--; rebuildWidgets(); }).active = page > 0;
        button("Next · " + (page + 1) + "/" + total, left() + half + 4, height - 51, span() - half - 4,
                () -> { page++; rebuildWidgets(); }).active = page + 1 < total;
    }

    private Button button(String label, int x, int y, int width, Runnable action) {
        Button result = addRenderableWidget(Button.builder(Component.literal(LibertyClient.clipped(label, width - 10)), ignored -> action.run())
                .bounds(x, y, width, 20).build());
        result.setTooltip(Tooltip.create(Component.literal(label)));
        return result;
    }

    private void send(String action, String value) {
        if (pending) return;
        pending = true; pendingTicks = 0; waiting = "Waiting for the server…";
        LibertyClient.request(action, value);
        rebuildWidgets();
    }
    private void change(View next) { view = next; page = 0; scroll = 0; rebuildWidgets(); }
    private void back() {
        if (view == View.THEATER) change(View.WAR_MAP);
        else if (view == View.PROJECT) change(View.RESEARCH);
        else change(LibertyClient.state().getBoolean("Active") ? View.MISSION : View.WAR_MAP);
    }

    @Override public void tick() {
        super.tick();
        if (pending && ++pendingTicks >= 60) {
            pending = false;
            waiting = "No reply yet. Refresh to read the server state.";
            rebuildWidgets();
        }
    }

    @Override public void render(GuiGraphics gui, int mouseX, int mouseY, float partialTick) {
        renderBackground(gui);
        gui.fill(left() - 6, 4, left() + span() + 6, height - 4, 0xF2181C20);
        gui.drawCenteredString(font, title, width / 2, 10, 0xFFE5C35E);
        String subtitle = subtitle();
        gui.drawCenteredString(font, LibertyClient.clipped(subtitle, span()), width / 2, 25, 0xFFD8DEE2);
        if (view == View.WAR_MAP) drawMap(gui);
        else if (view == View.RESEARCH) drawResearch(gui);
        else drawDetails(gui);
        String status = waiting.isBlank() ? LibertyClient.state().getString("Status") : waiting;
        if (view == View.WAR_MAP && !LibertyClient.state().getBoolean("CanAbort") && LibertyClient.theaters().size() <= 3 && !status.isBlank())
            drawWrappedFooter(gui, status);
        super.render(gui, mouseX, mouseY, partialTick);
        if (mouseY >= 23 && mouseY <= 35 && mouseX >= left() && mouseX <= left() + span() && !subtitle.isBlank())
            gui.renderTooltip(font, font.split(Component.literal(subtitle), Math.max(100, span() - 16)), mouseX, mouseY);
        else if (view == View.WAR_MAP && !LibertyClient.state().getBoolean("CanAbort") && LibertyClient.theaters().size() <= 3 && !status.isBlank()
                && mouseY >= height - 61 && mouseY < height - 28 && mouseX >= left() && mouseX <= left() + span())
            gui.renderTooltip(font, font.split(Component.literal(status), Math.max(100, span() - 16)), mouseX, mouseY);
    }

    private String subtitle() {
        if (pending || !waiting.isBlank()) return waiting;
        CompoundTag research = LibertyClient.research();
        if (view == View.RESEARCH || view == View.PROJECT)
            return Math.max(0, research.getInt("Credits")) + " credits · " + LibertyClient.samples(research, true);
        if (LibertyClient.state().getBoolean("Active"))
            return LibertyClient.state().getString("Phase").replace('_', ' ') + " · " + LibertyClient.clock(LibertyClient.state().getInt("Seconds"));
        return "Drop on skates. Reclaim the streets. Bring everyone home.";
    }

    private void drawMap(GuiGraphics gui) {
        ListTag rows = LibertyClient.theaters();
        if (rows.isEmpty()) { drawMessageBody(gui, "No districts are available. Refresh to read the war map."); return; }
        int start = page * 3, visible = Math.min(3, rows.size() - start);
        if (visible > 1) gui.fill(left() + 6, 80, left() + 8, 80 + (visible - 1) * 35, 0xFF586C71);
        for (int i = 0; i < visible; i++) {
            CompoundTag row = rows.getCompound(start + i);
            int y = 70 + i * 35;
            gui.fill(left() + 2, y + 6, left() + 12, y + 16, row.getBoolean("Available") ? 0xFFE5C35E : 0xFF787B83);
            String detail = row.getString("Gang") + " · " + (Math.max(0, row.getInt("Radius")) * 2 + 1) + " blocks across";
            gui.drawString(font, LibertyClient.clipped(detail, span() - 18), left() + 18, y + 23, 0xFFBAC8CC, true);
        }
    }

    private void drawResearch(GuiGraphics gui) {
        ListTag rows = projects();
        if (rows.isEmpty()) { drawMessageBody(gui, reason(LibertyClient.research(), "No research projects are available.")); return; }
        int start = page * 3;
        for (int i = start; i < Math.min(start + 3, rows.size()); i++) {
            CompoundTag row = rows.getCompound(i);
            String detail = row.getBoolean("Unlocked") ? row.getBoolean("Preparation") ? "One starting rank · choose in details" : "Permanent research · active automatically" : cost(row);
            gui.drawString(font, LibertyClient.clipped(detail, span()), left(), 93 + (i - start) * 35, 0xFFD7B7FF, true);
        }
    }

    private void drawDetails(GuiGraphics gui) {
        List<TextLine> lines = new ArrayList<>();
        CompoundTag state = LibertyClient.state();
        if (view == View.THEATER) {
            CompoundTag row = find(LibertyClient.theaters(), theater);
            add(lines, row.getString("Name"), 0xFFE5C35E);
            add(lines, row.getString("Description"), 0xFFE7E7E7);
            add(lines, "Rival crew: " + row.getString("Gang"), 0xFF87E1EA);
            add(lines, "District: " + (Math.max(0, row.getInt("Radius")) * 2 + 1) + " blocks across", 0xFFBAC8CC);
            add(lines, "Rewards: " + row.getString("Reward"), 0xFFD7B7FF);
            add(lines, "Complete the marked objectives, reach extraction, hold for the ship and board it. Your normal-world belongings return with you.", 0xFFE7E7E7);
            if (!row.getString("Reason").isBlank()) add(lines, row.getString("Reason"), 0xFFFFC591);
        } else if (view == View.PROJECT) {
            CompoundTag row = find(projects(), project);
            add(lines, row.getString("Name"), 0xFFE5C35E);
            add(lines, row.getString("Description"), 0xFFE7E7E7);
            if (!row.getString("Effect").equals(row.getString("Description"))) add(lines, row.getString("Effect"), 0xFF87E1EA);
            add(lines, "Cost: " + cost(row), 0xFFD7B7FF);
            if (row.getBoolean("Preparation")) add(lines, "Select one researched preparation for Rival Rush. It uses the existing single starting rank; selecting NONE returns to your Style Library or crew-house choice.", 0xFFBAC8CC);
            if (row.getBoolean("Unlocked")) add(lines, row.getBoolean("Selected") ? "Learned and selected for your next Rival Rush." : "Research learned permanently.", 0xFFC5ED55);
            else if (!row.getBoolean("Affordable")) add(lines, "More super credits or research samples are needed. Check the exact costs above.", 0xFFFFC591);
            if (!LibertyClient.research().getString("Reason").isBlank()) add(lines, LibertyClient.research().getString("Reason"), 0xFFFFC591);
        } else if (view == View.MISSION) {
            add(lines, state.getString("Status"), 0xFFE5C35E);
            add(lines, "Recovered: " + Math.max(0, state.getInt("Credits")) + " credits · " + LibertyClient.samples(state, false), 0xFFD7B7FF);
            add(lines, "Resupply charges: " + Math.max(0, state.getInt("Resupplies")), 0xFFBAC8CC);
            if (state.getString("Phase").equals("EXTRACTING")) add(lines, "Ship arrival: " + Math.max(0, state.getInt("ExtractionSeconds")) + " seconds. Hold within 48 blocks of the pad.", 0xFFE5C35E);
            for (var raw : LibertyClient.objectives()) {
                CompoundTag row = (CompoundTag) raw;
                add(lines, (row.getBoolean("Complete") ? "Complete · " : "Objective · ") + row.getString("Name") + " · "
                        + Math.max(0, row.getInt("Progress")) + "/" + Math.max(1, row.getInt("Goal")), row.getBoolean("Complete") ? 0xFFC5ED55 : 0xFF87E1EA);
                add(lines, row.getString("Description"), 0xFFE7E7E7);
                if (row.contains("Distance")) add(lines, LibertyClient.distance(row.getDouble("Distance")) + "m away"
                        + (row.contains("X") && row.contains("Z") ? " · X " + row.getInt("X") + " / Z " + row.getInt("Z") : ""), 0xFFBAC8CC);
            }
            if (state.contains("ExtractionDistance")) add(lines, "Extraction pad: " + LibertyClient.distance(state.getDouble("ExtractionDistance")) + "m away"
                    + (state.contains("ExtractionX") && state.contains("ExtractionZ") ? " · X " + state.getInt("ExtractionX") + " / Z " + state.getInt("ExtractionZ") : ""), 0xFFE5C35E);
            add(lines, "Call extraction at the pad after objectives are complete. Defend the landing zone, then board the arrived ship to bank your samples.", 0xFFE7E7E7);
        }
        if (view != View.MISSION && !state.getString("Status").isBlank()
                && (view != View.THEATER || !state.getString("Status").equals(find(LibertyClient.theaters(), theater).getString("Reason"))))
            add(lines, state.getString("Status"), 0xFFFFC591);
        drawBody(gui, lines);
    }

    private void drawBody(GuiGraphics gui, List<TextLine> lines) {
        int top = 73, bottom = height - 60, available = Math.max(10, bottom - top);
        contentHeight = lines.size() * 11;
        scroll = Math.max(0, Math.min(scroll, Math.max(0, contentHeight - available)));
        gui.enableScissor(left(), top, left() + span(), bottom);
        for (int i = 0; i < lines.size(); i++) {
            int y = top + i * 11 - scroll;
            if (y >= top && y + font.lineHeight <= bottom) gui.drawString(font, lines.get(i).text(), left(), y, lines.get(i).color(), true);
        }
        gui.disableScissor();
        if (contentHeight > available) {
            int track = available, thumb = Math.max(12, track * available / contentHeight);
            int y = top + (track - thumb) * scroll / Math.max(1, contentHeight - available);
            gui.fill(left() + span() - 3, top, left() + span(), bottom, 0xFF343D45);
            gui.fill(left() + span() - 3, y, left() + span(), y + thumb, 0xFFE5C35E);
            gui.drawString(font, "Scroll / PgUp / PgDn", left(), 61, 0xFFBAC8CC, true);
        }
    }

    private void drawMessageBody(GuiGraphics gui, String text) {
        List<TextLine> lines = new ArrayList<>(); add(lines, text, 0xFFE6DBA6); drawBody(gui, lines);
    }

    private TextLine line(String text, int color) { return new TextLine(Component.literal(text).getVisualOrderText(), color); }
    private void add(List<TextLine> lines, String text, int color) {
        if (text == null || text.isBlank()) return;
        for (FormattedCharSequence wrapped : font.split(Component.literal(text), Math.max(20, span() - 10))) lines.add(new TextLine(wrapped, color));
        lines.add(line("", color));
    }
    private void drawWrappedFooter(GuiGraphics gui, String text) {
        var wrapped = font.split(Component.literal(text), span());
        for (int i = 0; i < Math.min(3, wrapped.size()); i++) gui.drawString(font, wrapped.get(i), left(), height - 61 + i * 10, 0xFFFFC591, true);
    }
    private ListTag projects() { return LibertyClient.research().getList("Projects", Tag.TAG_COMPOUND); }
    private static CompoundTag find(ListTag list, String id) {
        for (var raw : list) if (raw instanceof CompoundTag row && row.getString("Id").equals(id)) return row;
        return new CompoundTag();
    }
    private static String reason(CompoundTag row, String fallback) { return row.getString("Reason").isBlank() ? fallback : row.getString("Reason"); }
    private static String cost(CompoundTag row) {
        return Math.max(0, row.getInt("Credits")) + " credits · " + Math.max(0, row.getInt("Common")) + " common · "
                + Math.max(0, row.getInt("Rare")) + " rare · " + Math.max(0, row.getInt("Super")) + " super";
    }

    @Override public boolean mouseScrolled(double mouseX, double mouseY, double amount) {
        if (view == View.WAR_MAP || view == View.RESEARCH) return super.mouseScrolled(mouseX, mouseY, amount);
        scroll = Math.max(0, scroll - (int) Math.signum(amount) * 22);
        return true;
    }
    @Override public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (view != View.WAR_MAP && view != View.RESEARCH) {
            if (keyCode == GLFW.GLFW_KEY_PAGE_DOWN) { scroll += Math.max(22, height - 140); return true; }
            if (keyCode == GLFW.GLFW_KEY_PAGE_UP) { scroll = Math.max(0, scroll - Math.max(22, height - 140)); return true; }
            if (keyCode == GLFW.GLFW_KEY_HOME) { scroll = 0; return true; }
            if (keyCode == GLFW.GLFW_KEY_END) { scroll = contentHeight; return true; }
        }
        return super.keyPressed(keyCode, scanCode, modifiers);
    }
    private int span() { return Math.max(200, Math.min(480, width - 20)); }
    private int left() { return (width - span()) / 2; }
    @Override public boolean isPauseScreen() { return false; }
}
