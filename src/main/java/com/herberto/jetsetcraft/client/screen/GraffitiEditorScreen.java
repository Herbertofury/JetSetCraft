package com.herberto.jetsetcraft.client.screen;

import com.herberto.jetsetcraft.graffiti.CustomGraffiti;
import com.herberto.jetsetcraft.item.SprayCanItem;
import com.herberto.jetsetcraft.network.JetSetNetwork;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;

import java.util.Arrays;

/** Bounded palette editor: no files, commands, or arbitrary texture uploads are accepted from a client. */
public final class GraffitiEditorScreen extends Screen {
    private static final int CELL = 12;
    private final Screen parent;
    private final InteractionHand hand;
    private byte[] pixels;
    private int color = 1;

    public GraffitiEditorScreen(Screen parent, InteractionHand hand) {
        super(Component.translatable("screen.jetsetcraft.graffiti_editor"));
        this.parent = parent;
        this.hand = hand;
    }

    @Override
    protected void init() {
        ItemStack stack = minecraft.player == null ? ItemStack.EMPTY : minecraft.player.getItemInHand(hand);
        pixels = CustomGraffiti.decode(SprayCanItem.getCustomSelection(stack));
        int y = height - 28;
        addRenderableWidget(Button.builder(Component.translatable("screen.jetsetcraft.graffiti_clear"), button -> Arrays.fill(pixels, (byte) 0))
                .bounds(width / 2 - 92, y, 56, 20).build());
        addRenderableWidget(Button.builder(Component.translatable("gui.cancel"), button -> minecraft.setScreen(parent))
                .bounds(width / 2 - 32, y, 60, 20).build());
        addRenderableWidget(Button.builder(Component.translatable("gui.done"), button -> save())
                .bounds(width / 2 + 32, y, 60, 20).build());
    }

    private void save() {
        String encoded = CustomGraffiti.encode(pixels);
        if (minecraft.player != null) {
            SprayCanItem.setCustomSelection(minecraft.player.getItemInHand(hand), encoded);
            SprayCanItem.setFreePaint(minecraft.player.getItemInHand(hand), false);
            JetSetNetwork.sendGraffitiSelection(hand, 0, encoded, false,
                    SprayCanItem.getPaintColor(minecraft.player.getItemInHand(hand)).id());
            minecraft.player.displayClientMessage(Component.translatable("message.jetsetcraft.graffiti_custom_saved"), true);
        }
        minecraft.setScreen(parent);
    }

    @Override
    public void render(GuiGraphics gui, int mouseX, int mouseY, float partialTick) {
        renderBackground(gui);
        gui.drawCenteredString(font, title, width / 2, 12, 0xFFFFFFFF);
        gui.drawCenteredString(font, Component.translatable("screen.jetsetcraft.graffiti_editor_hint"), width / 2, 25, 0xFFB8B8B8);
        int canvasX = (width - CustomGraffiti.WIDTH * CELL) / 2;
        int canvasY = 42;
        gui.fill(canvasX - 3, canvasY - 3, canvasX + CustomGraffiti.WIDTH * CELL + 3,
                canvasY + CustomGraffiti.HEIGHT * CELL + 3, 0xFF101010);
        for (int py = 0; py < CustomGraffiti.HEIGHT; py++) {
            for (int px = 0; px < CustomGraffiti.WIDTH; px++) {
                int value = pixels[py * CustomGraffiti.WIDTH + px] & 15;
                int x = canvasX + px * CELL;
                int y = canvasY + py * CELL;
                int argb = value == 0 ? (((px + py) & 1) == 0 ? 0xFF555555 : 0xFF777777) : CustomGraffiti.PALETTE[value];
                gui.fill(x, y, x + CELL - 1, y + CELL - 1, argb);
            }
        }
        int paletteY = canvasY + CustomGraffiti.HEIGHT * CELL + 10;
        int paletteX = (width - 16 * 13) / 2;
        for (int i = 0; i < CustomGraffiti.PALETTE.length; i++) {
            int x = paletteX + i * 13;
            gui.fill(x - 1, paletteY - 1, x + 12, paletteY + 12, i == color ? 0xFFFFFFFF : 0xFF333333);
            int argb = i == 0 ? 0xFF202020 : CustomGraffiti.PALETTE[i];
            gui.fill(x + 1, paletteY + 1, x + 10, paletteY + 10, argb);
        }
        super.render(gui, mouseX, mouseY, partialTick);
    }

    private boolean paint(double mouseX, double mouseY) {
        int canvasX = (width - CustomGraffiti.WIDTH * CELL) / 2;
        int canvasY = 42;
        int px = (int) (mouseX - canvasX) / CELL;
        int py = (int) (mouseY - canvasY) / CELL;
        if (mouseX >= canvasX && mouseY >= canvasY && px >= 0 && px < CustomGraffiti.WIDTH
                && py >= 0 && py < CustomGraffiti.HEIGHT) {
            pixels[py * CustomGraffiti.WIDTH + px] = (byte) color;
            return true;
        }
        return false;
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (button == 0 && paint(mouseX, mouseY)) return true;
        int paletteY = 42 + CustomGraffiti.HEIGHT * CELL + 10;
        int paletteX = (width - 16 * 13) / 2;
        if (button == 0 && mouseY >= paletteY && mouseY < paletteY + 13 && mouseX >= paletteX && mouseX < paletteX + 16 * 13) {
            color = Math.max(0, Math.min(15, (int) (mouseX - paletteX) / 13));
            return true;
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseDragged(double mouseX, double mouseY, int button, double dragX, double dragY) {
        return button == 0 && paint(mouseX, mouseY) || super.mouseDragged(mouseX, mouseY, button, dragX, dragY);
    }

    @Override
    public void onClose() { minecraft.setScreen(parent); }

    @Override
    public boolean isPauseScreen() { return false; }
}
