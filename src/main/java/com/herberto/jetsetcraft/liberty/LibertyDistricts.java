package com.herberto.jetsetcraft.liberty;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.level.saveddata.SavedData;

/** Monotonic arena allocation avoids recycling another mission's painted walls or terrain layout. */
public final class LibertyDistricts extends SavedData {
    private int next;
    private CompoundTag retained;
    public static LibertyDistricts get(MinecraftServer server) {
        return server.overworld().getDataStorage().computeIfAbsent(LibertyDistricts::load, LibertyDistricts::new, "jetsetcraft_liberty_districts");
    }
    public int allocate() {
        if (retained != null || next >= 1_000_000) return -1;
        int result = next++; setDirty(); return result;
    }
    public static LibertyDistricts load(CompoundTag tag) {
        LibertyDistricts data = new LibertyDistricts();
        if (tag.getInt("Schema") != 1 || !tag.contains("Next", Tag.TAG_INT) || tag.getInt("Next") < 0 || tag.getInt("Next") > 1_000_000)
            data.retained = tag.copy();
        else data.next = tag.getInt("Next");
        return data;
    }
    @Override public CompoundTag save(CompoundTag tag) {
        if (retained != null) return retained.copy();
        tag.putInt("Schema", 1); tag.putInt("Next", next); return tag;
    }
}
