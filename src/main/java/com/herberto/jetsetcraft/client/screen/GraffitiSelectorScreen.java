package com.herberto.jetsetcraft.client.screen;

import com.herberto.jetsetcraft.graffiti.GraffitiCatalog;
import com.herberto.jetsetcraft.graffiti.PaintColor;
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
    private Button modeButton;

    public GraffitiSelectorScreen(InteractionHand hand) {
        super(Component.translatable("screen.jetsetcraft.graffiti_selector"));
        this.hand = hand;
    }

    @Override
    protected void init() {
        ItemStack stack = minecraft.player == null ? ItemStack.EMPTY : minecraft.player.getItemInHand(hand);
        page = Math.max(0, SprayCanItem.getCatalogSelection(stack) / PER_PAGE);
        int buttonY = height - 24;
        addRenderableWidget(Button.builder(Component.literal("‹"), button -> changePage(-1))
                .bounds(width / 2 - 162, buttonY, 24, 20).build());
        modeButton = addRenderableWidget(Button.builder(modeLabel(stack), button -> toggleMode())
                .bounds(width / 2 - 134, buttonY, 92, 20).build());
        addRenderableWidget(Button.builder(Component.translatable("screen.jetsetcraft.graffiti_custom"),
                        button -> minecraft.setScreen(new GraffitiEditorScreen(this, hand)))
                .bounds(width / 2 - 38, buttonY, 88, 20).build());
        addRenderableWidget(Button.builder(Component.translatable("gui.done"), button -> onClose())
                .bounds(width / 2 + 54, buttonY, 80, 20).build());
        addRenderableWidget(Button.builder(Component.literal("›"), button -> changePage(1))
                .bounds(width / 2 + 138, buttonY, 24, 20).build());
    }

    private void changePage(int delta) {
        int pages = Math.max(1, (GraffitiCatalog.size() + PER_PAGE - 1) / PER_PAGE);
        page = Math.floorMod(page + delta, pages);
    }

    private void toggleMode() {
        if (minecraft.player == null) return;
        ItemStack stack = minecraft.player.getItemInHand(hand);
        SprayCanItem.setFreePaint(stack, !SprayCanItem.isFreePaint(stack));
        modeButton.setMessage(modeLabel(stack));
        sync(stack);
    }

    private static Component modeLabel(ItemStack stack) {
        return Component.translatable(SprayCanItem.isFreePaint(stack)
                ? "screen.jetsetcraft.mode_free_paint" : "screen.jetsetcraft.mode_tag");
    }

    private void sync(ItemStack stack) {
        JetSetNetwork.sendGraffitiSelection(hand, SprayCanItem.getCatalogSelection(stack),
                SprayCanItem.getCustomSelection(stack), SprayCanItem.isFreePaint(stack),
                SprayCanItem.getPaintColor(stack).id());
    }

    @Override
    public void render(GuiGraphics gui, int mouseX, int mouseY, float partialTick) {
        renderBackground(gui);
        gui.drawCenteredString(font, title, width / 2, 12, 0xFFFFFFFF);
        int pages = Math.max(1, (GraffitiCatalog.size() + PER_PAGE - 1) / PER_PAGE);
        gui.drawCenteredString(font, Component.literal((page + 1) + " / " + pages), width / 2, 25, 0xFFB8B8B8);
        int cardWidth = 78;
        int cardHeight = 42;
        int gap = 4;
        int gridWidth = COLUMNS * cardWidth + (COLUMNS - 1) * gap;
        int gridX = (width - gridWidth) / 2;
        int gridY = 34;
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
                    !SprayCanItem.isFreePaint(stack) && index == selected && !SprayCanItem.hasCustomSelection(stack)
                            ? 0xD0648539 : hover ? 0xC0444444 : 0xB0222222);
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
        int paletteY = height - 42;
        int paletteX = (width - PaintColor.values().length * 11) / 2;
        PaintColor paintColor = SprayCanItem.getPaintColor(stack);
        for (PaintColor color : PaintColor.values()) {
            int x = paletteX + color.id() * 11;
            gui.fill(x - 1, paletteY - 1, x + 10, paletteY + 10,
                    color == paintColor ? 0xFFFFFFFF : 0xFF282828);
            gui.fill(x + 1, paletteY + 1, x + 8, paletteY + 8, color.argb());
        }
        super.render(gui, mouseX, mouseY, partialTick);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (button == 0) {
            int cardWidth = 78;
            int cardHeight = 42;
            int gap = 4;
            int gridWidth = COLUMNS * cardWidth + (COLUMNS - 1) * gap;
            int gridX = (width - gridWidth) / 2;
            int gridY = 34;
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
                        SprayCanItem.setFreePaint(stack, false);
                        modeButton.setMessage(modeLabel(stack));
                        sync(stack);
                        minecraft.player.displayClientMessage(Component.translatable(
                                "message.jetsetcraft.graffiti_variant", GraffitiCatalog.id(index)), true);
                        return true;
                    }
                }
            }
            int paletteY = height - 42;
            int paletteX = (width - PaintColor.values().length * 11) / 2;
            if (minecraft.player != null && mouseY >= paletteY - 1 && mouseY < paletteY + 11
                    && mouseX >= paletteX - 1 && mouseX < paletteX + PaintColor.values().length * 11) {
                int colorId = Math.max(0, Math.min(PaintColor.values().length - 1,
                        (int) (mouseX - paletteX) / 11));
                ItemStack stack = minecraft.player.getItemInHand(hand);
                SprayCanItem.setPaintColor(stack, PaintColor.byId(colorId));
                SprayCanItem.setFreePaint(stack, true);
                modeButton.setMessage(modeLabel(stack));
                sync(stack);
                minecraft.player.displayClientMessage(Component.translatable(
                        "message.jetsetcraft.free_paint_color", PaintColor.byId(colorId).serializedName()), true);
                return true;
            }
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public boolean isPauseScreen() { return false; }
}
