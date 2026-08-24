package com.herberto.jetsetcraft.client.screen;

import com.herberto.jetsetcraft.graffiti.GraffitiCatalog;
import com.herberto.jetsetcraft.item.SprayCanItem;
import com.herberto.jetsetcraft.network.JetSetNetwork;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;

/** Mouse-first gallery for every bundled tag plus entry into the custom pixel editor. */
public final class GraffitiSelectorScreen extends Screen {
    private static final int COLUMNS = 4;
    private static final int ROWS = 3;
    private static final int PER_PAGE = COLUMNS * ROWS;
    private final InteractionHand hand;
    private int page;

    public GraffitiSelectorScreen(InteractionHand hand) {
        super(Component.translatable("screen.jetsetcraft.graffiti_selector"));
        this.hand = hand;
    }

    @Override
    protected void init() {
        ItemStack stack = minecraft.player == null ? ItemStack.EMPTY : minecraft.player.getItemInHand(hand);
        page = Math.max(0, SprayCanItem.getCatalogSelection(stack) / PER_PAGE);
        int buttonY = height - 27;
        addRenderableWidget(Button.builder(Component.literal("‹"), button -> changePage(-1))
                .bounds(width / 2 - 104, buttonY, 24, 20).build());
        addRenderableWidget(Button.builder(Component.translatable("screen.jetsetcraft.graffiti_custom"),
                        button -> minecraft.setScreen(new GraffitiEditorScreen(this, hand)))
                .bounds(width / 2 - 76, buttonY, 102, 20).build());
        addRenderableWidget(Button.builder(Component.translatable("gui.done"), button -> onClose())
                .bounds(width / 2 + 30, buttonY, 74, 20).build());
        addRenderableWidget(Button.builder(Component.literal("›"), button -> changePage(1))
                .bounds(width / 2 + 108, buttonY, 24, 20).build());
    }

    private void changePage(int delta) {
        int pages = Math.max(1, (GraffitiCatalog.size() + PER_PAGE - 1) / PER_PAGE);
        page = Math.floorMod(page + delta, pages);
    }

    @Override
    public void render(GuiGraphics gui, int mouseX, int mouseY, float partialTick) {
        renderBackground(gui);
        gui.drawCenteredString(font, title, width / 2, 12, 0xFFFFFFFF);
        int pages = Math.max(1, (GraffitiCatalog.size() + PER_PAGE - 1) / PER_PAGE);
        gui.drawCenteredString(font, Component.literal((page + 1) + " / " + pages), width / 2, 25, 0xFFB8B8B8);
        int cardWidth = 78;
        int cardHeight = 49;
        int gap = 5;
        int gridWidth = COLUMNS * cardWidth + (COLUMNS - 1) * gap;
        int gridX = (width - gridWidth) / 2;
        int gridY = 40;
        ItemStack stack = minecraft.player == null ? ItemStack.EMPTY : minecraft.player.getItemInHand(hand);
        int selected = SprayCanItem.getCatalogSelection(stack);
        for (int slot = 0; slot < PER_PAGE; slot++) {
            int index = page * PER_PAGE + slot;
            if (index >= GraffitiCatalog.size()) break;
            int column = slot % COLUMNS;
            int row = slot / COLUMNS;
            int x = gridX + column * (cardWidth + gap);
            int y = gridY + row * (cardHeight + gap);
            boolean hover = mouseX >= x && mouseX < x + cardWidth && mouseY >= y && mouseY < y + cardHeight;
            gui.fill(x, y, x + cardWidth, y + cardHeight,
                    index == selected && !SprayCanItem.hasCustomSelection(stack) ? 0xD0648539 : hover ? 0xC0444444 : 0xB0222222);
            var entry = GraffitiCatalog.get(index);
            int imageWidth = Math.min(cardWidth - 8, Math.round((cardHeight - 17) * entry.aspectRatio()));
            int imageHeight = Math.min(cardHeight - 17, Math.round(imageWidth / Math.max(0.05f, entry.aspectRatio())));
            int imageX = x + (cardWidth - imageWidth) / 2;
            gui.blit(entry.texture(), imageX, y + 3, 0, 0, imageWidth, imageHeight,
                    entry.pixelWidth(), entry.pixelHeight());
            String label = entry.id();
            if (label.startsWith("jsr_")) label = label.substring(4);
            if (label.length() > 13) label = label.substring(0, 6) + "…" + label.substring(label.length() - 6);
            gui.drawCenteredString(font, label, x + cardWidth / 2, y + cardHeight - 11, 0xFFE8E8E8);
        }
        super.render(gui, mouseX, mouseY, partialTick);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (button == 0) {
            int cardWidth = 78;
            int cardHeight = 49;
            int gap = 5;
            int gridWidth = COLUMNS * cardWidth + (COLUMNS - 1) * gap;
            int gridX = (width - gridWidth) / 2;
            int gridY = 40;
            int column = (int) (mouseX - gridX) / (cardWidth + gap);
            int row = (int) (mouseY - gridY) / (cardHeight + gap);
            if (column >= 0 && column < COLUMNS && row >= 0 && row < ROWS) {
                int x = gridX + column * (cardWidth + gap);
                int y = gridY + row * (cardHeight + gap);
                if (mouseX >= x && mouseX < x + cardWidth && mouseY >= y && mouseY < y + cardHeight) {
                    int index = page * PER_PAGE + row * COLUMNS + column;
                    if (index < GraffitiCatalog.size() && minecraft.player != null) {
                        ItemStack stack = minecraft.player.getItemInHand(hand);
                        SprayCanItem.setCatalogSelection(stack, index);
                        JetSetNetwork.sendGraffitiSelection(hand, index, "");
                        minecraft.player.displayClientMessage(Component.translatable(
                                "message.jetsetcraft.graffiti_variant", GraffitiCatalog.id(index)), true);
                        return true;
                    }
                }
            }
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public boolean isPauseScreen() { return false; }
}
