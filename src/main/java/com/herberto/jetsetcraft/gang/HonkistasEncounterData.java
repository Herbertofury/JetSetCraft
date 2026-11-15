package com.herberto.jetsetcraft.gang;

import com.herberto.jetsetcraft.data.SavedDataCheckpoint;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.level.saveddata.SavedData;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;

/** Server-only chain journal. No actors, rewards, RNG, client calls or model substitution. */
public final class HonkistasEncounterData extends SavedData {
    private static final String NAME = "jetsetcraft_honkistas_encounters";
    public enum Stage { SOURCE_RECORDED, RIVAL_ADMITTED, RIVAL_WON, HONKISTAS_ADMITTED, CLOSED }
    public record Entry(UUID sourceEvent, String sourceGang, UUID rivalEvent, UUID honkistasEvent, Stage stage) {}
    private final Map<UUID, Entry> entries = new LinkedHashMap<>();
    private final ListTag quarantined = new ListTag();
    private CompoundTag unknownSchema;
    public static HonkistasEncounterData get(MinecraftServer server) {
        return server.overworld().getDataStorage().computeIfAbsent(HonkistasEncounterData::load, HonkistasEncounterData::new, NAME);
    }
    /** Future actor admission must await this acknowledgement after recording its event decision. */
    public static boolean saveAcknowledged(MinecraftServer server) {
        return server != null && SavedDataCheckpoint.saveAndAcknowledge(server, NAME, get(server));
    }
    public Entry entry(UUID owner) { return entries.get(owner); }
    private boolean blocked(UUID owner) {
        if (owner == null || unknownSchema != null) return true;
        for (int i=0;i<quarantined.size();i++) {
            CompoundTag row=quarantined.getCompound(i);
            if (row.hasUUID("Owner") && row.getUUID("Owner").equals(owner)) return true;
        }
        return false;
    }
    public boolean sourceSettled(UUID owner, UUID event, String gang) {
        if (blocked(owner) || event == null || gang == null || ResourceLocation.tryParse(gang) == null) return false;
        Entry previous=entries.get(owner);
        // Never overwrite an unresolved chain or replay its closed source. Owner-scoped one outstanding chain.
        if (previous != null && (previous.stage()!=Stage.CLOSED || previous.sourceEvent().equals(event))) return false;
        entries.put(owner,new Entry(event,gang,null,null,Stage.SOURCE_RECORDED));setDirty();return true;
    }
    public boolean rivalAdmitted(UUID owner, UUID source, UUID rival) {
        Entry e=entries.get(owner);
        if (blocked(owner) || e==null || e.stage()!=Stage.SOURCE_RECORDED || !e.sourceEvent().equals(source)
                || rival==null || rival.equals(source)) return false;
        entries.put(owner,new Entry(source,e.sourceGang(),rival,null,Stage.RIVAL_ADMITTED));setDirty();return true;
    }
    public boolean rivalWon(UUID owner, UUID rival) {
        Entry e=entries.get(owner);
        if (blocked(owner) || e==null || e.stage()!=Stage.RIVAL_ADMITTED || !e.rivalEvent().equals(rival)) return false;
        entries.put(owner,new Entry(e.sourceEvent(),e.sourceGang(),rival,null,Stage.RIVAL_WON));setDirty();return true;
    }
    public boolean honkistasAdmitted(UUID owner, UUID rival, UUID event, boolean eligible) {
        Entry e=entries.get(owner);
        if (blocked(owner) || !eligible || e==null || e.stage()!=Stage.RIVAL_WON || !e.rivalEvent().equals(rival)
                || event==null || event.equals(rival) || event.equals(e.sourceEvent())) return false;
        entries.put(owner,new Entry(e.sourceEvent(),e.sourceGang(),rival,event,Stage.HONKISTAS_ADMITTED));setDirty();return true;
    }
    public boolean close(UUID owner, UUID source) {
        Entry e=entries.get(owner);
        if (blocked(owner)||e==null||e.stage()==Stage.CLOSED||!e.sourceEvent().equals(source)) return false;
        entries.put(owner,new Entry(e.sourceEvent(),e.sourceGang(),e.rivalEvent(),e.honkistasEvent(),Stage.CLOSED));setDirty();return true;
    }
    @Override public CompoundTag save(CompoundTag tag) {
        if (unknownSchema!=null) return unknownSchema.copy();
        tag.putInt("Schema",1);ListTag rows=quarantined.copy();
        entries.forEach((owner,e)->{ CompoundTag row=new CompoundTag();row.putUUID("Owner",owner);
            row.putUUID("Source",e.sourceEvent());row.putString("Gang",e.sourceGang());row.putString("Stage",e.stage().name());
            if(e.rivalEvent()!=null)row.putUUID("Rival",e.rivalEvent());if(e.honkistasEvent()!=null)row.putUUID("Honkistas",e.honkistasEvent());rows.add(row);});
        tag.put("Entries",rows);return tag;
    }
    public static HonkistasEncounterData load(CompoundTag tag) {
        HonkistasEncounterData data=new HonkistasEncounterData();
        if(tag.getInt("Schema")!=1 || !tag.contains("Entries",Tag.TAG_LIST)){data.unknownSchema=tag.copy();return data;}
        ListTag rows=(ListTag)tag.get("Entries");
        // getList(name, COMPOUND) silently returns an empty list on a type mismatch.
        // Preserve unexpected list shapes read-only rather than erasing their contents.
        if (!rows.isEmpty() && rows.getElementType()!=Tag.TAG_COMPOUND) {
            data.unknownSchema=tag.copy();return data;
        }
        for(int i=0;i<rows.size();i++){
            CompoundTag row=rows.getCompound(i);
            try {
                if(!row.hasUUID("Owner")||!row.hasUUID("Source")||ResourceLocation.tryParse(row.getString("Gang"))==null)throw new IllegalArgumentException();
                Stage stage=Stage.valueOf(row.getString("Stage"));
                UUID owner=row.getUUID("Owner"),source=row.getUUID("Source");
                UUID rival=row.hasUUID("Rival")?row.getUUID("Rival"):null, honk=row.hasUUID("Honkistas")?row.getUUID("Honkistas"):null;
                if ((stage==Stage.RIVAL_ADMITTED||stage==Stage.RIVAL_WON||stage==Stage.HONKISTAS_ADMITTED)&&rival==null
                        ||stage==Stage.HONKISTAS_ADMITTED&&honk==null||source.equals(rival)||source.equals(honk)
                        ||rival!=null&&rival.equals(honk)||honk!=null&&rival==null
                        ||stage==Stage.SOURCE_RECORDED&&(rival!=null||honk!=null)
                        ||(stage==Stage.RIVAL_ADMITTED||stage==Stage.RIVAL_WON)&&honk!=null||data.entries.containsKey(owner))throw new IllegalArgumentException();
                data.entries.put(owner,new Entry(source,row.getString("Gang"),rival,honk,stage));
            }catch(RuntimeException invalid){data.quarantined.add(row.copy());}
        }
        return data;
    }
}
