package com.herberto.jetsetcraft.crew;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceLocation;

import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/** Earned before founding a home; moved into the first house in the same world-saved transaction. */
final class PendingCrewProgress {
    final UUID owner;
    int supplies;
    long xp, training;
    final LinkedHashMap<String, Integer> reputations = new LinkedHashMap<>();
    final LinkedHashSet<UUID> receipts = new LinkedHashSet<>();
    final List<CrewRewardReceipt> rewards = new ArrayList<>();
    PendingCrewProgress(UUID owner) { this.owner = owner; }
    boolean award(UUID receipt, String gang, int supplies, int xp, int training, int reputation) {
        if (receipt == null || receipts.contains(receipt) || ResourceLocation.tryParse(gang) == null) return false;
        receipts.add(receipt);
        while (receipts.size() > 128) receipts.remove(receipts.iterator().next());
        this.supplies = (int) Math.min(CrewProfile.SUPPLY_LIMIT, (long) this.supplies + Math.max(0, supplies));
        this.xp = CrewProfile.clampXp(this.xp + Math.max(0, xp));
        this.training = CrewProfile.clampXp(this.training + Math.max(0, training));
        if (reputations.containsKey(gang) || reputations.size() < 512)
            reputations.put(gang, (int) Math.min(10_000L, (long) reputations.getOrDefault(gang, 0) + Math.max(0, reputation)));
        return true;
    }
    void transfer(CrewProfile profile) {
        profile.addSupplies(supplies); profile.addXp(xp);
        reputations.forEach(profile::addReputation);
        profile.settledRuns.addAll(receipts);
        while (profile.settledRuns.size() > 128) profile.settledRuns.remove(profile.settledRuns.iterator().next());
        CrewProfile.Member member = profile.members.get(profile.focus);
        if (member != null) member.xp = CrewProfile.clampXp(member.xp + training);
        rewards.forEach(receipt -> CrewRewardHistory.add(profile.rewards, receipt));
        profile.changed();
    }
    CompoundTag save() {
        CompoundTag tag = new CompoundTag();
        tag.putInt("Schema", 1); tag.putUUID("Owner", owner); tag.putInt("Supplies", supplies);
        tag.putLong("Xp", xp); tag.putLong("Training", training);
        CompoundTag relations = new CompoundTag(); reputations.forEach(relations::putInt); tag.put("Reputations", relations);
        ListTag list = new ListTag(); for (UUID id : receipts) { CompoundTag row = new CompoundTag(); row.putUUID("Id", id); list.add(row); }
        tag.put("Receipts", list); tag.put("Rewards", CrewRewardHistory.save(rewards)); return tag;
    }
    static PendingCrewProgress load(CompoundTag tag) {
        if (tag.getInt("Schema") != 1 || !tag.hasUUID("Owner")) return null;
        PendingCrewProgress progress = new PendingCrewProgress(tag.getUUID("Owner"));
        progress.supplies = Math.max(0, Math.min(CrewProfile.SUPPLY_LIMIT, tag.getInt("Supplies")));
        progress.xp = CrewProfile.clampXp(tag.getLong("Xp")); progress.training = CrewProfile.clampXp(tag.getLong("Training"));
        CompoundTag relations = tag.getCompound("Reputations");
        for (String gang : relations.getAllKeys()) {
            if (progress.reputations.size() >= 512) break;
            if (ResourceLocation.tryParse(gang) != null)
                progress.reputations.put(gang, Math.max(0, Math.min(10_000, relations.getInt(gang))));
        }
        ListTag receipts = tag.getList("Receipts", Tag.TAG_COMPOUND);
        for (int i = Math.max(0, receipts.size() - 128); i < receipts.size(); i++)
            if (receipts.getCompound(i).hasUUID("Id")) progress.receipts.add(receipts.getCompound(i).getUUID("Id"));
        CrewRewardHistory.load(progress.rewards, tag.getList("Rewards", Tag.TAG_COMPOUND));
        return progress;
    }
}
