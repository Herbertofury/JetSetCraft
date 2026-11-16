package com.herberto.jetsetcraft.gametest;

import com.herberto.jetsetcraft.JetSetCraft;
import com.herberto.jetsetcraft.crew.ColiseumLineup;
import com.herberto.jetsetcraft.crew.ColiseumLineup.Reason;
import com.herberto.jetsetcraft.crew.CrewProfile;
import com.herberto.jetsetcraft.rush.RushAlbum;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraftforge.gametest.GameTestHolder;
import net.minecraftforge.gametest.PrefixGameTestTemplate;
import java.util.*;

/** Fixture-only roster/defeat data: not real earned conquests or spectator battle evidence. */
@GameTestHolder(JetSetCraft.MOD_ID)
@PrefixGameTestTemplate(false)
public final class ColiseumLineupGameTests {
    @GameTest(template="hoverboard_empty",timeoutTicks=40)
    public static void coliseumPreviewRejectsForeignBusyStaleAndUnearnedRosters(GameTestHelper helper){
        UUID owner=UUID.randomUUID(),a=UUID.randomUUID(),b=UUID.randomUUID(),c=UUID.randomUUID();
        CompoundTag tag=new CrewProfile(owner,UUID.randomUUID(),"Fixture","jetsetcraft:dead_beat","minecraft:overworld",BlockPos.ZERO).save();
        ListTag members=new ListTag();members.add(member(a,"minecraft:zombie","jetsetcraft:dead_beat"));
        members.add(member(b,"minecraft:skeleton","jetsetcraft:bone_beat"));
        members.add(member(c,"minecraft:zombie","jetsetcraft:dead_beat"));tag.put("Members",members);
        CrewProfile crew=CrewProfile.load(tag);check(crew!=null,"Fixture failed native NBT load");
        List<RushAlbum.Entry> defeats=List.of(new RushAlbum.Entry("minecraft:zombie","jetsetcraft:dead_beat",1,1,5),
                new RushAlbum.Entry("minecraft:skeleton","jetsetcraft:bone_beat",1,1,5));
        CompoundTag before=crew.save();
        var preview=ColiseumLineup.evaluate(crew,owner,crew.revision(),List.of(a),List.of(b),defeats);
        check(preview.eligibleForPreview()&&preview.left().get(0).memberId().equals(a),"Owned earned lineup not previewable");
        check(crew.save().equals(before),"Preview mutated crew data");
        boolean immutable=false;try{preview.left().clear();}catch(UnsupportedOperationException expected){immutable=true;}
        check(immutable,"Preview exposed mutable roster");
        expect(Reason.WRONG_OWNER,ColiseumLineup.evaluate(crew,UUID.randomUUID(),crew.revision(),List.of(a),List.of(b),defeats));
        expect(Reason.STALE,ColiseumLineup.evaluate(crew,owner,crew.revision()+1,List.of(a),List.of(b),defeats));
        expect(Reason.UNKNOWN_MEMBER,ColiseumLineup.evaluate(crew,owner,crew.revision(),List.of(UUID.randomUUID()),List.of(b),defeats));
        expect(Reason.DUPLICATE_MEMBER,ColiseumLineup.evaluate(crew,owner,crew.revision(),List.of(a),List.of(a),defeats));
        expect(Reason.INVALID_TEAM_SIZE,ColiseumLineup.evaluate(crew,owner,crew.revision(),List.of(),List.of(b),defeats));
        expect(Reason.DEFEAT_NOT_RECORDED,ColiseumLineup.evaluate(crew,owner,crew.revision(),List.of(a),List.of(b),List.of()));
        expect(Reason.SAME_GANG,ColiseumLineup.evaluate(crew,owner,crew.revision(),List.of(a),List.of(c),defeats));
        expect(Reason.MIXED_GANG,ColiseumLineup.evaluate(crew,owner,crew.revision(),List.of(a,b),List.of(c),defeats));
        check(crew.save().equals(before),"Rejected preview mutated crew data");
        check(crew.assignMission(a,"courier_run",0,0)!=null,"Fixture mission assignment failed");
        CompoundTag busy=crew.save();
        expect(Reason.BUSY_MEMBER,ColiseumLineup.evaluate(crew,owner,crew.revision(),List.of(a),List.of(b),defeats));
        check(crew.save().equals(busy),"Busy rejection changed assignment");
        System.out.println("JETSETCRAFT_GAMETEST_PASS coliseum_lineup_preview_policy");helper.succeed();
    }
    @GameTest(template="hoverboard_empty",timeoutTicks=40)
    public static void coliseumDefeatLookupUsesExactPersistedKeysWithoutReadMutation(GameTestHelper helper){
        var player=net.minecraftforge.common.util.FakePlayerFactory.get(helper.getLevel(),
                new com.mojang.authlib.GameProfile(UUID.randomUUID(),"JSC_coliseum"));
        CompoundTag original=player.getPersistentData().copy();
        try {
            check(!RushAlbum.hasDefeated(player,"minecraft:zombie","jetsetcraft:dead_beat"),"Unrecorded defeat accepted");
            check(original.equals(player.getPersistentData()),"Read-only lookup created player data");
            // Explicit fixture collection only; not evidence of a real player battle.
            RushAlbum.collect(player,"minecraft:zombie","jetsetcraft:dead_beat");
            CompoundTag earned=player.getPersistentData().copy();
            check(RushAlbum.hasDefeated(player,"minecraft:zombie","jetsetcraft:dead_beat"),"Exact earned pair missing");
            check(!RushAlbum.hasDefeated(player,"minecraft:zombie","jetsetcraft:other")
                    &&!RushAlbum.hasDefeated(player,"minecraft:skeleton","jetsetcraft:dead_beat")
                    &&!RushAlbum.hasDefeated(player,null,"jetsetcraft:dead_beat"),"Lookup broadened evidence to another actor/gang");
            check(earned.equals(player.getPersistentData()),"Exact lookup mutated collection");
            System.out.println("JETSETCRAFT_GAMETEST_PASS coliseum_exact_defeat_lookup");helper.succeed();
        } finally {
            for(String key:new HashSet<>(player.getPersistentData().getAllKeys()))player.getPersistentData().remove(key);
            player.getPersistentData().merge(original);
        }
    }
    private static CompoundTag member(UUID id,String species,String gang){
        CompoundTag row=new CompoundTag();row.putUUID("Id",id);row.putString("Name","Fixture recruit");
        row.putString("Species",species);row.putString("Gang",gang);row.putString("Role","courier");row.putLong("Xp",0);return row;
    }
    private static void expect(Reason reason,ColiseumLineup.Preview preview){check(preview.reason()==reason,"Expected "+reason+", got "+preview.reason());}
    private static void check(boolean condition,String message){if(!condition)throw new IllegalStateException(message);}
    private ColiseumLineupGameTests(){}
}
