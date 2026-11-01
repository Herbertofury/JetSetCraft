package com.herberto.jetsetcraft.crew;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.level.saveddata.SavedData;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;

/** One world-owned progression store; temporary Rush journals never serialize or restore it. */
public final class CrewHouseData extends SavedData {
    private static final String NAME = "jetsetcraft_crew_houses";
    private final Map<UUID, CrewProfile> profiles = new LinkedHashMap<>();
    private final Map<UUID, PendingCrewProgress> pending = new LinkedHashMap<>();
    private final ListTag quarantined = new ListTag();
    private CompoundTag newerSchema;

    public static CrewHouseData get(MinecraftServer server) {
        return server.overworld().getDataStorage().computeIfAbsent(CrewHouseData::load, CrewHouseData::new, NAME);
    }
    public CrewProfile profile(UUID owner) { return profiles.get(owner); }
    public Collection<CrewProfile> profiles() { return java.util.List.copyOf(profiles.values()); }
    public boolean readOnly() { return newerSchema != null; }
    public boolean blocked(UUID owner) {
        if (readOnly()) return true;
        for (int i = 0; i < quarantined.size(); i++) {
            CompoundTag row = quarantined.getCompound(i);
            if (row.hasUUID("Owner") && row.getUUID("Owner").equals(owner)) return true;
        }
        return false;
    }
    public boolean add(CrewProfile profile) {
        if (profile == null || blocked(profile.owner()) || profiles.containsKey(profile.owner())) return false;
        PendingCrewProgress earned = pending.get(profile.owner());
        if (earned != null) earned.transfer(profile);
        profiles.put(profile.owner(), profile); pending.remove(profile.owner()); setDirty(); return true;
    }
    public int pendingSupplies(UUID owner) { PendingCrewProgress p = pending.get(owner); return p == null ? 0 : p.supplies; }
    public long pendingXp(UUID owner) { PendingCrewProgress p = pending.get(owner); return p == null ? 0 : p.xp; }
    public long pendingTraining(UUID owner) { PendingCrewProgress p = pending.get(owner); return p == null ? 0 : p.training; }
    public int pendingReputation(UUID owner, String gang) {
        PendingCrewProgress p = pending.get(owner); return p == null ? 0 : p.reputations.getOrDefault(gang, 0);
    }
    /** Same save record owns receipt and reward; a first house absorbs its pending account exactly once. */
    boolean award(UUID owner, UUID receipt, String gang, int supplies, int xp, int training, int reputation) {
        if (owner == null || receipt == null || blocked(owner)) return false;
        CrewProfile profile = profiles.get(owner);
        if (profile == null) {
            boolean awarded = pending.computeIfAbsent(owner, PendingCrewProgress::new)
                    .award(receipt, gang, supplies, xp, training, reputation);
            if (awarded) setDirty();
            return awarded;
        }
        if (profile.settledRuns.contains(receipt)) return false;
        profile.settledRuns.add(receipt);
        while (profile.settledRuns.size() > 128) profile.settledRuns.remove(profile.settledRuns.iterator().next());
        profile.addSupplies(supplies); profile.addXp(xp); profile.addReputation(gang, reputation);
        CrewProfile.Member member = profile.members.get(profile.focus);
        if (member != null && !profile.busy(member.id)) member.xp = CrewProfile.clampXp(member.xp + training);
        profile.changed(); setDirty(); return true;
    }
    public static CrewHouseData load(CompoundTag tag) {
        CrewHouseData data = new CrewHouseData();
        if (tag.getInt("Schema") != 1 && tag.getInt("Schema") != 2) { data.newerSchema = tag.copy(); return data; }
        ListTag rows = tag.getList("Profiles", Tag.TAG_COMPOUND);
        for (int i = 0; i < rows.size(); i++) {
            CompoundTag row = rows.getCompound(i);
            CrewProfile profile = CrewProfile.load(row);
            if (profile == null || data.profiles.containsKey(profile.owner())) data.quarantined.add(row.copy());
            else data.profiles.put(profile.owner(), profile);
        }
        ListTag oldQuarantine = tag.getList("Quarantined", Tag.TAG_COMPOUND);
        for (int i = 0; i < oldQuarantine.size(); i++) data.quarantined.add(oldQuarantine.getCompound(i).copy());
        ListTag pendingRows = tag.getList("PendingProgress", Tag.TAG_COMPOUND);
        for (int i = 0; i < pendingRows.size(); i++) {
            CompoundTag row = pendingRows.getCompound(i);
            PendingCrewProgress progress = PendingCrewProgress.load(row);
            if (progress == null || data.pending.containsKey(progress.owner) || data.profiles.containsKey(progress.owner))
                data.quarantined.add(row.copy());
            else data.pending.put(progress.owner, progress);
        }
        return data;
    }
    @Override public CompoundTag save(CompoundTag tag) {
        if (newerSchema != null) return newerSchema.copy();
        tag.putInt("Schema", 2);
        ListTag rows = new ListTag(); for (CrewProfile profile : profiles.values()) rows.add(profile.save());
        tag.put("Profiles", rows); tag.put("Quarantined", quarantined.copy());
        ListTag unclaimed = new ListTag(); for (PendingCrewProgress progress : pending.values()) unclaimed.add(progress.save());
        tag.put("PendingProgress", unclaimed);
        return tag;
    }
}
