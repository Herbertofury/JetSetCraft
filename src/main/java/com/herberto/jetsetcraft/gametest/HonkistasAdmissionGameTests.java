package com.herberto.jetsetcraft.gametest;

import com.herberto.jetsetcraft.JetSetCraft;
import com.herberto.jetsetcraft.gang.HonkistasActorAdmission;
import com.herberto.jetsetcraft.gang.HonkistasEncounterData;
import com.mojang.authlib.GameProfile;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraftforge.common.util.FakePlayerFactory;
import net.minecraftforge.gametest.GameTestHolder;
import net.minecraftforge.gametest.PrefixGameTestTemplate;
import java.util.List;
import java.util.UUID;

@GameTestHolder(JetSetCraft.MOD_ID)
@PrefixGameTestTemplate(false)
public final class HonkistasAdmissionGameTests {
    @GameTest(template="hoverboard_empty",timeoutTicks=100)
    public static void nativeAdmissionRequiresCheckpointAndRejectsReplay(GameTestHelper helper) {
        var level=helper.getLevel();
        var owner=FakePlayerFactory.get(level,new GameProfile(UUID.randomUUID(),"JSC_admission"));
        BlockPos pos=helper.absolutePos(new BlockPos(1,2,1));
        owner.moveTo(pos.getX()+.5,pos.getY(),pos.getZ()+.5,0,0);
        var data=HonkistasEncounterData.get(level.getServer());UUID source=UUID.randomUUID();
        check(data.sourceSettled(owner.getUUID(),source,"jetsetcraft:dead_beat"),"Source not recorded");
        check(HonkistasActorAdmission.rival(owner,source,null).state()==HonkistasActorAdmission.State.NO_PROVIDER,"Missing provider admitted");
        check(data.entry(owner.getUUID()).stage()==HonkistasEncounterData.Stage.SOURCE_RECORDED,"Missing provider consumed source");
        helper.runAfterDelay(2,()->{
            Mob mob=EntityType.PIG.create(level);
            check(mob!=null,"Native fixture actor unavailable");
            mob.moveTo(pos.getX()+2.5,pos.getY(),pos.getZ()+.5,0,0);
            try {
                var result=HonkistasActorAdmission.rival(owner,source,()->List.of(mob));
                check(result.state()==HonkistasActorAdmission.State.ADMITTED,"Native admission failed: "+result.state());
                check(level.getEntity(mob.getUUID())==mob && mob.isAlive(),"No actual world actor");
                var saved=HonkistasEncounterData.load(data.save(new CompoundTag()));
                check(saved.entry(owner.getUUID()).rivalEvent().equals(result.event()),"Durable event identity mismatch");
                check(HonkistasEncounterData.saveAcknowledged(level.getServer()),"Checkpoint not acknowledged");
                check(HonkistasActorAdmission.rival(owner,source,()->{throw new AssertionError("Replay invoked provider");}).state()
                        ==HonkistasActorAdmission.State.STALE_SOURCE,"Replay admitted");
                helper.succeed();
            } finally {mob.discard();data.close(owner.getUUID(),source);}
        });
    }
    @GameTest(template="hoverboard_empty",timeoutTicks=100)
    public static void preexistingBodyCannotBecomeEncounterActor(GameTestHelper helper) {
        var owner=FakePlayerFactory.get(helper.getLevel(),new GameProfile(UUID.randomUUID(),"JSC_existing"));
        BlockPos pos=helper.absolutePos(new BlockPos(1,2,1));
        owner.moveTo(pos.getX()+.5,pos.getY(),pos.getZ()+.5,0,0);
        Mob existing=helper.spawn(EntityType.PIG,new BlockPos(2,2,2));
        var data=HonkistasEncounterData.get(owner.getServer());UUID source=UUID.randomUUID();
        data.sourceSettled(owner.getUUID(),source,"jetsetcraft:dead_beat");
        try {
            check(HonkistasActorAdmission.rival(owner,source,()->List.of(existing)).state()
                    ==HonkistasActorAdmission.State.INVALID_ACTORS,"Existing entity appropriated");
            check(!existing.isRemoved(),"Rejected provider deleted a pre-existing entity");
            check(data.entry(owner.getUUID()).stage()==HonkistasEncounterData.Stage.SOURCE_RECORDED,"Invalid provider consumed source");
            helper.succeed();
        }finally{existing.discard();data.close(owner.getUUID(),source);}
    }
    private static void check(boolean value,String message){if(!value)throw new IllegalStateException(message);}
}
