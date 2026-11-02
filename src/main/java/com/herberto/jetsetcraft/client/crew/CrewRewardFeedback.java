package com.herberto.jetsetcraft.client.crew;

import com.herberto.jetsetcraft.config.JetSetConfig;
import com.herberto.jetsetcraft.crew.CrewRewardReceipt;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.toasts.Toast;
import net.minecraft.client.gui.components.toasts.ToastComponent;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.util.FormattedCharSequence;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.UUID;

/** Read-only feedback for committed server receipts. Never opens a screen or requests another award. */
public final class CrewRewardFeedback {
    private static final LinkedHashSet<UUID> SEEN = new LinkedHashSet<>();
    private static int generation;

    public static void accept(CompoundTag incoming) {
        Minecraft minecraft = Minecraft.getInstance();
        CrewRewardReceipt receipt = CrewRewardReceipt.load(incoming);
        if (minecraft.player == null || minecraft.getConnection() == null || receipt == null || !SEEN.add(receipt.id())) return;
        while (SEEN.size() > 64) SEEN.remove(SEEN.iterator().next());
        if (JetSetConfig.CLIENT.reducedMotion.get()) {
            // Keep the complete receipt accessible without the native toast's slide animation.
            minecraft.player.displayClientMessage(Component.literal(receipt.name() + ": " + receipt.summary()
                    + ". " + receipt.trainingSummary() + ". " + receipt.levelSummary()), false);
        } else minecraft.getToasts().addToast(new RewardToast(receipt, generation));
    }

    static void reset() { SEEN.clear(); generation++; }

    private static final class RewardToast implements Toast {
        private final CrewRewardReceipt receipt;
        private final int epoch, width, height;
        private final List<Line> lines = new ArrayList<>();
        private record Line(FormattedCharSequence text, int color) {}

        RewardToast(CrewRewardReceipt receipt, int epoch) {
            this.receipt = receipt; this.epoch = epoch;
            Minecraft minecraft = Minecraft.getInstance();
            width = Math.max(160, Math.min(330, minecraft.getWindow().getGuiScaledWidth() - 16));
            add(receipt.name(), 0xFFC5ED55);
            add(receipt.summary(), 0xFFE7E7E7);
            add(receipt.trainingSummary(), 0xFF87E1EA);
            if (!receipt.levelSummary().isBlank()) add(receipt.levelSummary(), 0xFFC5ED55);
            add("Crew Charter → Progress", 0xFFB8BEC5);
            height = 12 + lines.size() * 11;
        }
        private void add(String text, int color) {
            for (FormattedCharSequence line : Minecraft.getInstance().font.split(Component.literal(text), width - 16))
                lines.add(new Line(line, color));
        }
        @Override public Visibility render(GuiGraphics graphics, ToastComponent component, long elapsed) {
            if (epoch != generation || Minecraft.getInstance().player == null || elapsed >= 7000L) return Visibility.HIDE;
            graphics.fill(0, 0, width, height, 0xF2181C20);
            graphics.fill(0, 0, 3, height, receipt.memberLeveledUp() ? 0xFFC5ED55 : 0xFF87E1EA);
            for (int i = 0; i < lines.size(); i++) {
                Line line = lines.get(i);
                graphics.drawString(Minecraft.getInstance().font, line.text(), 8, 6 + i * 11, line.color(), true);
            }
            return Visibility.SHOW;
        }
        @Override public int width() { return width; }
        @Override public int height() { return height; }
        @Override public Object getToken() { return receipt.id(); }
    }
    private CrewRewardFeedback() {}
}
