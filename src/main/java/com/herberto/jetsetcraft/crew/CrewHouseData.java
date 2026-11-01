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
        profiles.put(profile.owner(), profile); setDirty(); return true;
    }
    public static CrewHouseData load(CompoundTag tag) {
        CrewHouseData data = new CrewHouseData();
        if (tag.getInt("Schema") != 1) { data.newerSchema = tag.copy(); return data; }
        ListTag rows = tag.getList("Profiles", Tag.TAG_COMPOUND);
        for (int i = 0; i < rows.size(); i++) {
            CompoundTag row = rows.getCompound(i);
            CrewProfile profile = CrewProfile.load(row);
            if (profile == null || data.profiles.containsKey(profile.owner())) data.quarantined.add(row.copy());
            else data.profiles.put(profile.owner(), profile);
        }
        ListTag oldQuarantine = tag.getList("Quarantined", Tag.TAG_COMPOUND);
        for (int i = 0; i < oldQuarantine.size(); i++) data.quarantined.add(oldQuarantine.getCompound(i).copy());
        return data;
    }
    @Override public CompoundTag save(CompoundTag tag) {
        if (newerSchema != null) return newerSchema.copy();
        tag.putInt("Schema", 1);
        ListTag rows = new ListTag(); for (CrewProfile profile : profiles.values()) rows.add(profile.save());
        tag.put("Profiles", rows); tag.put("Quarantined", quarantined.copy());
        return tag;
    }
}
