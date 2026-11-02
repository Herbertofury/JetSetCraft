package com.herberto.jetsetcraft.client.rush;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

/** A normalized-position editor that works across Minecraft GUI scales and window sizes. */
final class RivalRushHudScreen extends Screen {
    private final Screen parent;
    private boolean dragging;
    private double grabX, grabY;

    RivalRushHudScreen(Screen parent) {
        super(Component.literal("Rival Rush HUD layout"));
        this.parent = parent;
    }

    @Override
    protected void init() {
        int middle = width / 2;
        addRenderableWidget(Button.builder(Component.literal("Reset"), button -> {
            RivalRushHudPreferences.get().reset();
            rebuildWidgets();
        })
                .bounds(middle - 132, height - 26, 82, 20).build());
        addRenderableWidget(Button.builder(visibility(), button -> {
            var preferences = RivalRushHudPreferences.get();
            preferences.visible = !preferences.visible;
            button.setMessage(visibility());
        }).bounds(middle - 44, height - 26, 88, 20).build());
        addRenderableWidget(Button.builder(Component.translatable("gui.done"), button -> onClose())
                .bounds(middle + 50, height - 26, 82, 20).build());
        if (RivalRushClient.state().getBoolean("Active")) {
            addRenderableWidget(Button.builder(Component.literal("Return to Minecraft"), button -> {
                RivalRushHudPreferences.get().save();
                RivalRushClient.request("leave", "", 0);
                minecraft.setScreen(parent);
            }).bounds(width - 151, 6, 145, 20).build());
        }
    }

    private Component visibility() {
        return Component.literal(RivalRushHudPreferences.get().visible ? "HUD: shown" : "HUD: hidden");
    }

    private int previewHeight() { return Math.max(1, height - 65); }

    @Override
    public void render(GuiGraphics gui, int mouseX, int mouseY, float partialTick) {
        renderBackground(gui);
        RivalRushClient.drawHud(gui, width, previewHeight(), true);
        gui.fill(0, height - 61, width, height, 0xC0181C20);
        gui.drawCenteredString(font, title, width / 2, height - 56, 0xFFC5ED55);
        gui.drawCenteredString(font, "Drag to move • scroll to scale • arrow keys to nudge", width / 2,
                height - 43, 0xFFE7E7E7);
        super.render(gui, mouseX, mouseY, partialTick);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (super.mouseClicked(mouseX, mouseY, button)) return true;
        var preferences = RivalRushHudPreferences.get();
        float scale = RivalRushClient.hudScale(width, previewHeight(), true);
        int w = Math.round(RivalRushClient.HUD_WIDTH * scale);
        int h = Math.round(RivalRushClient.hudHeight(true) * scale);
        double x = preferences.x * Math.max(0, width - w);
        double y = preferences.y * Math.max(0, previewHeight() - h);
        if (button == 0 && mouseX >= x && mouseX <= x + w && mouseY >= y && mouseY <= y + h) {
            dragging = true;
            grabX = mouseX - x;
            grabY = mouseY - y;
            return true;
        }
        return false;
    }

    @Override
    public boolean mouseDragged(double mouseX, double mouseY, int button, double deltaX, double deltaY) {
        if (dragging && button == 0) {
            var preferences = RivalRushHudPreferences.get();
            float scale = RivalRushClient.hudScale(width, previewHeight(), true);
            preferences.x = (float) ((mouseX - grabX) / Math.max(1, width - Math.round(RivalRushClient.HUD_WIDTH * scale)));
            preferences.y = (float) ((mouseY - grabY) / Math.max(1, previewHeight() - Math.round(RivalRushClient.hudHeight(true) * scale)));
            preferences.sanitize();
            return true;
        }
        return super.mouseDragged(mouseX, mouseY, button, deltaX, deltaY);
    }

    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        dragging = false;
        return super.mouseReleased(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double amount) {
        var preferences = RivalRushHudPreferences.get();
        preferences.scale += (float) Math.signum(amount) * 0.05F;
        preferences.sanitize();
        return true;
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        var preferences = RivalRushHudPreferences.get();
        switch (keyCode) {
            case 263 -> preferences.x -= 0.01F;
            case 262 -> preferences.x += 0.01F;
            case 265 -> preferences.y -= 0.01F;
            case 264 -> preferences.y += 0.01F;
            case 61, 334 -> preferences.scale += 0.05F;
            case 45, 333 -> preferences.scale -= 0.05F;
            default -> { return super.keyPressed(keyCode, scanCode, modifiers); }
        }
        preferences.sanitize();
        return true;
    }

    @Override
    public void onClose() { RivalRushHudPreferences.get().save(); minecraft.setScreen(parent); }
    @Override
    public void removed() { RivalRushHudPreferences.get().save(); }
    @Override
    public boolean isPauseScreen() { return false; }
}
