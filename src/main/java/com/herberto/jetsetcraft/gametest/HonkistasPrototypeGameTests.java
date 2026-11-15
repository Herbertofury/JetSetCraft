package com.herberto.jetsetcraft.gametest;

import com.herberto.jetsetcraft.JetSetCraft;
import com.herberto.jetsetcraft.registry.ModEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraftforge.gametest.GameTestHolder;
import net.minecraftforge.gametest.PrefixGameTestTemplate;

@GameTestHolder(JetSetCraft.MOD_ID)
@PrefixGameTestTemplate(false)
public final class HonkistasPrototypeGameTests {
    @GameTest(template="hoverboard_empty",timeoutTicks=100)
    public static void originalActorRegistersAndRoundTripsWithoutGangSubstitution(GameTestHelper helper){
        var actor=ModEntities.HONKISTAS_PROTOTYPE.get().create(helper.getLevel());
        if(actor==null)throw new IllegalStateException("Original actor missing");
        BlockPos pos=helper.absolutePos(new BlockPos(1,2,1));
        actor.moveTo(pos.getX()+.5,pos.getY(),pos.getZ()+.5,0,0);
        try{
            if(!helper.getLevel().addFreshEntity(actor) || helper.getLevel().getEntity(actor.getUUID())!=actor)
                throw new IllegalStateException("No native original actor");
            var tag=new net.minecraft.nbt.CompoundTag();actor.save(tag);
            if(!tag.getString("id").equals("jetsetcraft:honkistas_prototype") || actor.getMaxHealth()!=24)
                throw new IllegalStateException("Original identity/attributes changed");
            var restored=net.minecraft.world.entity.EntityType.loadEntityRecursive(tag,helper.getLevel(),e->e);
            if(!(restored instanceof com.herberto.jetsetcraft.entity.HonkistasPrototypeEntity))
                throw new IllegalStateException("Native actor roundtrip changed class");
            restored.discard();helper.succeed();
        }finally{actor.discard();}
    }
}
