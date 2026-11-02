package com.herberto.jetsetcraft.client.liberty;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

/** Local layout editor with drag, keyboard nudge, scale and visibility controls. */
final class LibertyHudScreen extends Screen {
    private final Screen parent;
    private boolean dragging;
    private double grabX, grabY;

    LibertyHudScreen(Screen parent) { super(Component.literal("Liberty Grind HUD")); this.parent = parent; }
    @Override protected void init() {
        int span = Math.min(280, width - 16), left = (width - span) / 2, third = (span - 8) / 3;
        addRenderableWidget(Button.builder(Component.literal("Reset"), button -> {
            LibertyHudPreferences.get().reset(); rebuildWidgets();
        }).bounds(left, height - 26, third, 20).build());
        addRenderableWidget(Button.builder(visibility(), button -> {
            var preferences = LibertyHudPreferences.get(); preferences.visible = !preferences.visible; button.setMessage(visibility());
        }).bounds(left + third + 4, height - 26, third, 20).build());
        addRenderableWidget(Button.builder(Component.translatable("gui.done"), button -> onClose())
                .bounds(left + 2 * (third + 4), height - 26, span - 2 * (third + 4), 20).build());
    }
    private Component visibility() { return Component.literal(LibertyHudPreferences.get().visible ? "HUD: shown" : "HUD: hidden"); }
    private int previewHeight() { return Math.max(1, height - 70); }
    @Override public void render(GuiGraphics gui, int mouseX, int mouseY, float partialTick) {
        renderBackground(gui);
        LibertyClient.drawHud(gui, width, previewHeight(), true);
        gui.fill(0, height - 67, width, height, 0xE0181C20);
        gui.drawCenteredString(font, title, width / 2, height - 61, 0xFFE5C35E);
        gui.drawCenteredString(font, "Drag to move · scroll or +/- to scale", width / 2, height - 49, 0xFFE7E7E7);
        gui.drawCenteredString(font, "Arrow keys nudge · only your layout changes", width / 2, height - 38, 0xFFBAC8CC);
        super.render(gui, mouseX, mouseY, partialTick);
    }
    @Override public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (super.mouseClicked(mouseX, mouseY, button)) return true;
        var preferences = LibertyHudPreferences.get();
        float scale = LibertyClient.hudScale(width, previewHeight());
        int w = Math.round(LibertyClient.HUD_WIDTH * scale), h = Math.round(LibertyClient.HUD_HEIGHT * scale);
        double x = preferences.x * Math.max(0, width - w), y = preferences.y * Math.max(0, previewHeight() - h);
        if (button == 0 && mouseX >= x && mouseX <= x + w && mouseY >= y && mouseY <= y + h) {
            dragging = true; grabX = mouseX - x; grabY = mouseY - y; return true;
        }
        return false;
    }
    @Override public boolean mouseDragged(double mouseX, double mouseY, int button, double deltaX, double deltaY) {
        if (dragging && button == 0) {
            var preferences = LibertyHudPreferences.get();
            float scale = LibertyClient.hudScale(width, previewHeight());
            preferences.x = (float) ((mouseX - grabX) / Math.max(1, width - Math.round(LibertyClient.HUD_WIDTH * scale)));
            preferences.y = (float) ((mouseY - grabY) / Math.max(1, previewHeight() - Math.round(LibertyClient.HUD_HEIGHT * scale)));
            preferences.sanitize(); return true;
        }
        return super.mouseDragged(mouseX, mouseY, button, deltaX, deltaY);
    }
    @Override public boolean mouseReleased(double mouseX, double mouseY, int button) {
        dragging = false; return super.mouseReleased(mouseX, mouseY, button);
    }
    @Override public boolean mouseScrolled(double mouseX, double mouseY, double amount) {
        var preferences = LibertyHudPreferences.get(); preferences.scale += (float) Math.signum(amount) * .05F; preferences.sanitize(); return true;
    }
    @Override public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        var preferences = LibertyHudPreferences.get();
        switch (keyCode) {
            case 263 -> preferences.x -= .01F;
            case 262 -> preferences.x += .01F;
            case 265 -> preferences.y -= .01F;
            case 264 -> preferences.y += .01F;
            case 61, 334 -> preferences.scale += .05F;
            case 45, 333 -> preferences.scale -= .05F;
            default -> { return super.keyPressed(keyCode, scanCode, modifiers); }
        }
        preferences.sanitize(); return true;
    }
    @Override public void onClose() { LibertyHudPreferences.get().save(); minecraft.setScreen(parent); }
    @Override public void removed() { LibertyHudPreferences.get().save(); }
    @Override public boolean isPauseScreen() { return false; }
}
