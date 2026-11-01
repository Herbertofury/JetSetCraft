package com.herberto.jetsetcraft.client.rush;

import com.herberto.jetsetcraft.gang.GangRegistry;
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

/** Native focusable controls for explicit entry, choices, card collection and immediate extraction. */
final class RivalRushScreen extends Screen {
    private enum View { RUN, ADVANCED, ROSTER, ALBUM, CARD, UPGRADES, RESULT }
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
    private String resultSummary = "";
    private boolean pending;
    private Button timedButton, endlessButton, applyButton;

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
        if (active && !previous.getBoolean("Active") && !hasOffers && !incoming.getBoolean("Finale")) {
            minecraft.setScreen(null);
            return;
        }
        if (view == View.UPGRADES && active && !hasOffers && !incoming.getBoolean("Finale")) {
            minecraft.setScreen(null);
            return;
        }
        if (active && previous.getBoolean("Finale") && !incoming.getBoolean("Finale") && !hasOffers
                && incoming.getString("Summary").isBlank()) {
            minecraft.setScreen(null);
            return;
        }
        boolean changed = incoming.getBoolean("Open") || active != previous.getBoolean("Active")
                || incoming.getInt("Version") != previous.getInt("Version")
                || incoming.getBoolean("Finale") != previous.getBoolean("Finale")
                || incoming.contains("Album", Tag.TAG_LIST) || incoming.contains("Roster", Tag.TAG_LIST);
        if (changed) {
            rememberFields();
            if (incoming.getBoolean("Open")) selectView(incoming);
            pending = false;
            rebuildWidgets();
        }
    }

    private void selectView(CompoundTag incoming) {
        if (!incoming.getString("Summary").isBlank()) { resultSummary = incoming.getString("Summary"); view = View.RESULT; }
        else if (incoming.getBoolean("Active") && !RivalRushClient.offers().isEmpty()) view = View.UPGRADES;
        else if (incoming.getBoolean("Finale")) view = View.RUN;
        else if (incoming.contains("Album", Tag.TAG_LIST)) { view = View.ALBUM; window = responseWindow; responseWindow = 0; }
        else if (incoming.contains("Roster", Tag.TAG_LIST)) { view = View.ROSTER; window = responseWindow; responseWindow = 0; }
        else if (!incoming.getBoolean("Active")) view = View.RUN;
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
        rebuildWidgets();
    }

    @Override
    protected void init() {
        rememberFields();
        gangField = mobField = null;
        timedButton = endlessButton = applyButton = null;
        int left = left();
        int span = span();
        int third = (span - 8) / 3;
        addRenderableWidget(Button.builder(Component.literal("Run"), button -> navigate(
                RivalRushClient.offers().isEmpty() ? View.RUN : View.UPGRADES))
                .bounds(left, 45, third, 20).build());
        addRenderableWidget(Button.builder(Component.literal("Rival crews"), button -> {
            navigate(View.ROSTER);
            RivalRushClient.request("roster", "", 0);
        }).bounds(left + third + 4, 45, third, 20).build());
        addRenderableWidget(Button.builder(Component.literal("Mob cards"), button -> {
            navigate(View.ALBUM);
            RivalRushClient.request("album", "", 0);
        }).bounds(left + (third + 4) * 2, 45, span - (third + 4) * 2, 20).build());

        switch (view) {
            case RUN -> buildRun(left, span);
            case ADVANCED -> buildAdvanced(left, span);
            case ROSTER, ALBUM -> buildList(left, span);
            case UPGRADES -> buildOffers(left, span);
            case CARD -> addRenderableWidget(Button.builder(Component.literal("Back to cards"),
                    button -> navigate(View.ALBUM)).bounds(left, height - 53, span, 20).build());
            case RESULT -> addRenderableWidget(Button.builder(Component.literal(RivalRushClient.state().getBoolean("Active")
                            ? "Return to run menu" : "Set up another run"),
                    button -> navigate(RivalRushClient.offers().isEmpty() ? View.RUN : View.UPGRADES))
                    .bounds(left, height - 53, span, 20).build());
        }

        boolean active = RivalRushClient.state().getBoolean("Active");
        int closeWidth = (span - 8) / 3;
        addRenderableWidget(Button.builder(Component.translatable("gui.done"), button -> onClose())
                .bounds(left, height - 26, closeWidth, 20).build());
        addRenderableWidget(Button.builder(Component.literal("HUD layout"), button -> {
            rememberFields();
            minecraft.setScreen(new RivalRushHudScreen(this));
        }).bounds(left + closeWidth + 4, height - 26, closeWidth, 20).build());
        Button returnButton = addRenderableWidget(Button.builder(Component.literal(active ? "Return to Minecraft" : "Close"), button -> {
            if (active) RivalRushClient.request("leave", "", 0);
            else onClose();
        }).bounds(left + (closeWidth + 4) * 2, height - 26, span - (closeWidth + 4) * 2, 20).build());
        returnButton.setTooltip(Tooltip.create(Component.literal(active
                ? "End this run and restore your Minecraft state. Collected mode cards stay in the album."
                : "Return to normal play. A run starts only when you choose Start.")));
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
            }
            return;
        }
        Button crew = addRenderableWidget(Button.builder(Component.literal(RivalRushClient.clipped(
                crewName() + "  •  Change crew", span - 12)), button -> {
            navigate(View.ROSTER);
            RivalRushClient.request("roster", "", 0);
        }).bounds(left, 88, span, 20).build());
        crew.setTooltip(Tooltip.create(Component.literal(crewName() + "\nTheme: " + mobName(mob)
                + "\nChoose a named rival crew and its themed cast.")));
        int half = (span - 4) / 2;
        timedButton = addRenderableWidget(Button.builder(Component.literal("Start 30-minute run"), button -> start(false))
                .bounds(left, 153, half, 20).build());
        endlessButton = addRenderableWidget(Button.builder(Component.literal("Start Endless"), button -> start(true))
                .bounds(left + half + 4, 153, span - half - 4, 20).build());
        timedButton.setTooltip(Tooltip.create(Component.literal("A timed session with an explicit Encore choice after the finale.")));
        endlessButton.setTooltip(Tooltip.create(Component.literal("Keep the run going through escalating sets. Return at any time.")));
        addRenderableWidget(Button.builder(Component.literal("Advanced: custom crew or mob"), button -> navigate(View.ADVANCED))
                .bounds(left, 183, span, 20).build());
        updateStartButtons();
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
        if (timedButton != null && endlessButton != null)
            timedButton.active = endlessButton.active = !pending && validIds(gang, mob);
        if (applyButton != null && gangField != null && mobField != null)
            applyButton.active = validIds(gangField.getValue(), mobField.getValue());
    }

    private static boolean validIds(String gangId, String mobId) {
        return gangId != null && mobId != null && gangId.trim().length() + mobId.trim().length() + 1 <= 128
                && ResourceLocation.tryParse(gangId.trim()) != null && ResourceLocation.tryParse(mobId.trim()) != null;
    }

    private void start(boolean endless) {
        rememberFields();
        if (!validIds(gang, mob)) return;
        pending = true;
        updateStartButtons();
        RivalRushClient.request("start", gang.trim() + "|" + mob.trim(), endless ? 1 : 0);
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
        gui.drawCenteredString(font, title, width / 2, 10, 0xFFC5ED55);
        gui.drawCenteredString(font, "An optional skating survival set • your own run XP", width / 2, 26, 0xFFDDDDDD);
        CompoundTag state = RivalRushClient.state();
        int left = left();
        int span = span();
        if (view == View.RUN) {
            if (!state.getBoolean("Active")) {
                gui.drawString(font, "Your rival crew", left, 75, 0xFFDDDDDD);
                gui.drawString(font, RivalRushClient.clipped("Theme: " + mobName(mob), span), left, 116, 0xFF87E1EA);
                wrap(gui, "Skate, tag, collect cards. Return at any time.", left, 131, span, 1, 0xFFDDDDDD);
            } else {
                gui.drawString(font, RivalRushClient.clipped(state.getString("Gang"), span), left, 78, 0xFFE7E7E7);
                gui.drawString(font, (state.getBoolean("Endless") ? "Endless • " : "30-minute set • ")
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
        } else if (view == View.ROSTER || view == View.ALBUM) {
            boolean cards = view == View.ALBUM;
            ListTag list = cards ? RivalRushClient.album() : RivalRushClient.roster();
            if (list.isEmpty()) wrap(gui, cards ? "Your album is empty. Defeat rivals in a Rush to collect their mob cards."
                    : "Choose Rival crews above to request the server's current roster.", left, 81, span, 4, 0xFFDDDDDD);
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
        addCardLines(lines, "Card rewards belong to Rival Rush. Your Minecraft XP and progression stay separate.", 0xFFB8B8B8);
        return lines;
    }

    private void addCardLines(List<CardLine> lines, String text, int color) {
        for (FormattedCharSequence line : font.split(Component.literal(text), Math.max(20, span()))) lines.add(new CardLine(line, color));
        lines.add(new CardLine(FormattedCharSequence.EMPTY, color));
    }

    private int maxCardScroll(List<CardLine> lines) { return Math.max(0, lines.size() * 11 - Math.max(1, height - 140)); }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double amount) {
        if (view == View.CARD && selectedCard != null) {
            cardScroll = Math.max(0, Math.min(maxCardScroll(cardLines()), cardScroll - (int) (amount * 22)));
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
        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    @Override
    public boolean isPauseScreen() { return false; }
}
