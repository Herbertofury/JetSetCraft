package com.herberto.jetsetcraft.gametest;

import com.herberto.jetsetcraft.JetSetCraft;
import com.herberto.jetsetcraft.gang.HonkistasEncounterData;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraftforge.gametest.GameTestHolder;
import net.minecraftforge.gametest.PrefixGameTestTemplate;
import java.util.UUID;

@GameTestHolder(JetSetCraft.MOD_ID)
@PrefixGameTestTemplate(false)
public final class HonkistasEncounterGameTests {
    @GameTest(template="hoverboard_empty",timeoutTicks=100)
    public static void persistedSequenceRejectsReplayAndOutOfOrder(GameTestHelper helper) {
        var data=new HonkistasEncounterData();
        UUID owner=UUID.randomUUID(),source=UUID.randomUUID(),rival=UUID.randomUUID(),honk=UUID.randomUUID();
        check(!data.rivalWon(owner,rival),"Win admitted without source/rival");
        check(data.sourceSettled(owner,source,"jetsetcraft:dead_beat"),"Source not recorded");
        check(!data.honkistasAdmitted(owner,rival,honk,true),"Honkistas skipped rival victory");
        check(!data.sourceSettled(owner,UUID.randomUUID(),"jetsetcraft:dead_beat"),"Unresolved chain overwritten");
        check(!data.rivalAdmitted(owner,UUID.randomUUID(),rival),"Wrong source admitted rival");
        check(data.rivalAdmitted(owner,source,rival),"Rival not admitted");
        check(!data.rivalWon(UUID.randomUUID(),rival),"Other owner advanced chain");
        data=roundTrip(data);check(data.rivalWon(owner,rival),"Rival victory lost after reload");
        check(!data.rivalWon(owner,rival),"Rival victory replayed");
        check(!data.honkistasAdmitted(owner,rival,honk,false),"Ineligible admission");
        check(data.honkistasAdmitted(owner,rival,honk,true),"Eligible admission failed");
        data=roundTrip(data);check(!data.honkistasAdmitted(owner,rival,UUID.randomUUID(),true),"Reload allowed reroll/duplicate admission");
        check(data.close(owner,source),"Close failed");check(!data.sourceSettled(owner,source,"jetsetcraft:dead_beat"),"Closed source replayed");
        helper.succeed();
    }
    @GameTest(template="hoverboard_empty",timeoutTicks=100)
    public static void unknownAndMalformedEncounterDataIsRetained(GameTestHelper helper) {
        CompoundTag future=new CompoundTag();future.putInt("Schema",99);future.putString("Unknown","preserve");
        var readonly=HonkistasEncounterData.load(future);
        check(readonly.save(new CompoundTag()).equals(future),"Future data lost");
        check(!readonly.sourceSettled(UUID.randomUUID(),UUID.randomUUID(),"jetsetcraft:dead_beat"),"Future data overwritten");
        UUID owner=UUID.randomUUID();CompoundTag root=new CompoundTag();root.putInt("Schema",1);
        ListTag rows=new ListTag();CompoundTag invalid=new CompoundTag();invalid.putUUID("Owner",owner);invalid.putString("Stage","NEW_STAGE");rows.add(invalid);root.put("Entries",rows);
        var quarantined=HonkistasEncounterData.load(root);
        check(quarantined.save(new CompoundTag()).equals(root),"Malformed row discarded");
        check(!quarantined.sourceSettled(owner,UUID.randomUUID(),"jetsetcraft:dead_beat"),"Quarantined owner overwritten");
        CompoundTag wrongShape=new CompoundTag();wrongShape.putInt("Schema",1);
        ListTag strings=new ListTag();strings.add(net.minecraft.nbt.StringTag.valueOf("must not disappear"));
        wrongShape.put("Entries",strings);var badList=HonkistasEncounterData.load(wrongShape);
        check(badList.save(new CompoundTag()).equals(wrongShape),"Wrong-type entry list discarded");
        check(!badList.sourceSettled(UUID.randomUUID(),UUID.randomUUID(),"jetsetcraft:dead_beat"),"Wrong-type data overwritten");
        helper.succeed();
    }
    private static HonkistasEncounterData roundTrip(HonkistasEncounterData data) {
        CompoundTag tag=data.save(new CompoundTag());var loaded=HonkistasEncounterData.load(tag);
        check(loaded.save(new CompoundTag()).equals(tag),"Exact NBT round trip failed");return loaded;
    }
    private static void check(boolean ok,String message){if(!ok)throw new IllegalStateException(message);}
}
