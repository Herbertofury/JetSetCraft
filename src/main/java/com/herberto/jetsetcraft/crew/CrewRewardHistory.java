package com.herberto.jetsetcraft.crew;

import net.minecraft.nbt.ListTag;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/** Small chronological presentation ledger, independent of the longer once-only settlement guard. */
final class CrewRewardHistory {
    static final int LIMIT = 16;
    static void add(List<CrewRewardReceipt> history, CrewRewardReceipt receipt) {
        if (receipt == null || history.stream().anyMatch(row -> row.id().equals(receipt.id()))) return;
        history.add(receipt);
        while (history.size() > LIMIT) history.remove(0);
    }
    static ListTag save(List<CrewRewardReceipt> history) {
        ListTag rows = new ListTag(); history.forEach(receipt -> rows.add(receipt.save())); return rows;
    }
    static void load(List<CrewRewardReceipt> history, ListTag rows) {
        for (int i = Math.max(0, rows.size() - LIMIT); i < rows.size(); i++) add(history, CrewRewardReceipt.load(rows.getCompound(i)));
    }
    static List<CrewRewardReceipt> recent(List<CrewRewardReceipt> history) {
        List<CrewRewardReceipt> recent = new ArrayList<>(history); Collections.reverse(recent); return List.copyOf(recent);
    }
    private CrewRewardHistory() {}
}
