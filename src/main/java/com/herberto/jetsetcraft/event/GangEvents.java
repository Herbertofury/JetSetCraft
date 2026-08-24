package com.herberto.jetsetcraft.event;

import com.herberto.jetsetcraft.JetSetCraft;
import com.herberto.jetsetcraft.gang.GangMemberState;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraftforge.event.entity.living.LivingDropsEvent;
import net.minecraftforge.event.entity.living.LivingEvent;
import net.minecraftforge.event.entity.living.LivingExperienceDropEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

/** Safety/lifecycle rules for explicit event-only casts. Natural/source-owned mobs are unaffected. */
@Mod.EventBusSubscriber(modid = JetSetCraft.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public final class GangEvents {
    @SubscribeEvent(priority = EventPriority.LOWEST)
    public static void preventEventCastLootFarm(LivingDropsEvent event) {
        if (GangMemberState.snapshot(event.getEntity()).ephemeral()) event.getDrops().clear();
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public static void preventEventCastXpFarm(LivingExperienceDropEvent event) {
        if (GangMemberState.snapshot(event.getEntity()).ephemeral()) event.setDroppedExperience(0);
    }

    @SubscribeEvent
    public static void expireEventCast(LivingEvent.LivingTickEvent event) {
        LivingEntity living = event.getEntity();
        if (living.level().isClientSide || !(living instanceof Mob mob)) return;
        if (GangMemberState.expired(mob, mob.level().getGameTime())) mob.discard();
    }

    private GangEvents() {}
}
