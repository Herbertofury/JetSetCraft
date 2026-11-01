package com.herberto.jetsetcraft.client.activity;

import com.herberto.jetsetcraft.network.JetSetNetwork;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.screens.ConfirmScreen;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.network.chat.Component;

/** A shared, readable front door for explicit Boombox choices and previewed random invitations. */
final class ActivityScreen extends Screen {
    private boolean pending;
    private int pendingTicks;

    ActivityScreen() { super(Component.literal("JetSetCraft Activities")); }

    void onSnapshot(CompoundTag incoming, CompoundTag previous) {
        pending = false;
        pendingTicks = 0;
        if (incoming.getBoolean("Active") && !previous.getBoolean("Active")) {
            minecraft.setScreen(null);
            return;
        }
        boolean changed = incoming.getBoolean("Open") || incoming.getBoolean("Active") != previous.getBoolean("Active")
                || incoming.getInt("Version") != previous.getInt("Version")
                || !incoming.getString("Summary").equals(previous.getString("Summary"))
                || incoming.getBoolean("CanLaunch") != previous.getBoolean("CanLaunch");
        if (changed) rebuildWidgets();
    }

    @Override
    protected void init() {
        int left = left(), span = span(), half = (span - 4) / 2;
        CompoundTag state = ActivityClient.state();
        if (!state.getBoolean("Active")) {
            ListTag activities = ActivityClient.activities();
            for (int i = 0; i < Math.min(6, activities.size()); i++) {
                CompoundTag mode = activities.getCompound(i);
                String id = mode.getString("Id");
                boolean selected = id.equals(state.getString("SelectedMode"));
                String label = (selected ? "• " : "") + mode.getString("Name");
                int x = left + (i % 2) * (half + 4);
                int rowWidth = i % 2 == 0 ? half : span - half - 4;
                Button button = addRenderableWidget(Button.builder(Component.literal(ActivityClient.clipped(label, rowWidth - 10)),
                        chosen -> send("select", id)).bounds(x, 52 + (i / 2) * 25, rowWidth, 20).build());
                button.active = !pending && mode.getBoolean("Available");
                String reason = mode.getString("Reason");
                button.setTooltip(Tooltip.create(Component.literal(mode.getString("Name") + "\n"
                        + mode.getString("Description") + (reason.isBlank() ? "" : "\n" + reason))));
            }
            String launchLabel = state.getString("LaunchLabel");
            if (launchLabel.isBlank()) launchLabel = "Confirm launch";
            Button launch = addRenderableWidget(Button.builder(Component.literal(ActivityClient.clipped(launchLabel, span - 12)),
                    button -> send("launch", state.getString("SelectedMode")))
                    .bounds(left, height - 53, span, 20).build());
            launch.active = !pending && state.getBoolean("CanLaunch");
            launch.setTooltip(Tooltip.create(Component.literal((state.getString("Summary").isBlank() ? "" : state.getString("Summary") + "\n")
                    + (state.getString("Objective").isBlank() ? "" : state.getString("Objective") + "\n")
                    + (state.getString("RewardSummary").isBlank() ? "" : state.getString("RewardSummary") + "\n")
                    + "Start the previewed activity. Reading this menu never starts or teleports you.")));
        } else {
            Button cancel = addRenderableWidget(Button.builder(Component.literal("End activity"), button ->
                    minecraft.setScreen(new ConfirmScreen(confirmed -> {
                        minecraft.setScreen(this);
                        if (confirmed) send("cancel", "");
                    }, Component.literal("End this activity?"),
                            Component.literal("The current challenge stops. Completed rewards stay saved; unfinished objectives do not earn a completion reward."))))
                    .bounds(left, height - 53, span, 20).build());
            cancel.active = state.getBoolean("CanCancel");
        }

        int third = (span - 8) / 3;
        addRenderableWidget(Button.builder(Component.translatable("gui.done"), button -> onClose())
                .bounds(left, height - 26, third, 20).build());
        addRenderableWidget(Button.builder(Component.literal("Refresh"), button -> send("refresh", ""))
                .bounds(left + third + 4, height - 26, third, 20).build()).active = !pending;
        addRenderableWidget(Button.builder(Component.literal("Crew House"), button ->
                JetSetNetwork.sendCrewAction("open", "", 0, null, null, 0L))
                .bounds(left + (third + 4) * 2, height - 26, span - (third + 4) * 2, 20).build());
    }

    private void send(String action, String mode) {
        pending = true;
        pendingTicks = 0;
        ActivityClient.request(action, mode);
        rebuildWidgets();
    }

    @Override
    public void tick() {
        super.tick();
        // A dropped/rate-limited request must not leave an ordinary screen permanently disabled.
        if (pending && ++pendingTicks >= 30) { pending = false; pendingTicks = 0; rebuildWidgets(); }
    }

    @Override
    public void render(GuiGraphics gui, int mouseX, int mouseY, float partialTick) {
        renderBackground(gui);
        int left = left(), span = span();
        CompoundTag state = ActivityClient.state();
        gui.fill(left - 6, 4, left + span + 6, height - 4, 0xF2181C20);
        gui.drawCenteredString(font, title, width / 2, 10, 0xFFC5ED55);
        String subtitle = state.getBoolean("FromBoombox") ? "Boombox • choose your activity" : "Street invitation • preview before playing";
        gui.drawCenteredString(font, subtitle, width / 2, 26, 0xFFDDDDDD);
        if (state.getBoolean("Active")) {
            gui.drawString(font, ActivityClient.clipped(state.getString("ModeName") + " • " + state.getString("Gang"), span), left, 57, 0xFF87E1EA);
            gui.drawString(font, "Progress " + Math.max(0, state.getInt("Progress")) + " / " + Math.max(1, state.getInt("Goal"))
                    + " • " + ActivityClient.clock(state.getLong("RemainingTicks")) + " remaining", left, 75, 0xFFE7E7E7);
            wrap(gui, state.getString("Objective"), left, 96, span, 3, 0xFFE6DBA6);
            wrap(gui, state.getString("AnchorLabel"), left, 132, span, 2, 0xFFB8B8B8);
        } else {
            String selection = state.getString("SelectedMode").equals("random") ? "Random pick: " : "Selected: ";
            gui.drawString(font, ActivityClient.clipped(selection + state.getString("ModeName"), span), left, 130, 0xFF87E1EA);
            String summary = state.getString("Summary");
            String description = summary.isBlank() ? selectedDescription(state.getString("Mode")) : summary;
            wrap(gui, description, left, 145, span, Math.max(1, (height - 215) / 10), 0xFFE7E7E7);
        }
        String reward = state.getString("RewardSummary");
        if (reward.isBlank()) reward = state.getString("Mode").equals("rival_rush")
                ? "Arena rewards and run results contribute to crew progress"
                : "Completion: " + Math.max(0, state.getInt("Supplies")) + " supplies • "
                    + Math.max(0, state.getInt("CrewXp")) + " crew XP • " + Math.max(0, state.getInt("Reputation")) + " reputation";
        gui.drawString(font, ActivityClient.clipped(reward, span), left, height - 66, 0xFFE6DBA6);
        if (pending) gui.drawCenteredString(font, "Waiting for the server…", width / 2, 39, 0xFFB8B8B8);
        super.render(gui, mouseX, mouseY, partialTick);
    }

    private String selectedDescription(String id) {
        for (var item : ActivityClient.activities()) {
            if (item instanceof CompoundTag row && row.getString("Id").equals(id)) return row.getString("Description");
        }
        return "Choose an activity above. Your crew progression connects the different games.";
    }

    private void wrap(GuiGraphics gui, String text, int x, int y, int availableWidth, int lines, int color) {
        var wrapped = font.split(Component.literal(text), Math.max(20, availableWidth));
        for (int i = 0; i < Math.min(lines, wrapped.size()); i++) gui.drawString(font, wrapped.get(i), x, y + i * 10, color, true);
    }

    private int span() { return Math.min(460, width - 20); }
    private int left() { return (width - span()) / 2; }
    @Override public boolean isPauseScreen() { return false; }
}
