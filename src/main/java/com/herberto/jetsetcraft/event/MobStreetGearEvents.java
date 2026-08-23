package com.herberto.jetsetcraft.event;

import com.herberto.jetsetcraft.JetSetCraft;
import com.herberto.jetsetcraft.item.RideGearItem;
import com.herberto.jetsetcraft.mob.MobStreetGear;
import com.herberto.jetsetcraft.mob.StreetGearAcquisition;
import com.herberto.jetsetcraft.network.JetSetNetwork;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.event.entity.EntityJoinLevelEvent;
import net.minecraftforge.event.entity.living.LivingConversionEvent;
import net.minecraftforge.event.entity.living.LivingDropsEvent;
import net.minecraftforge.event.entity.living.LivingEquipmentChangeEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/** Additive lifecycle bridge for same-entity Street Gear. Source mob AI and ownership remain untouched. */
@Mod.EventBusSubscriber(modid = JetSetCraft.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public final class MobStreetGearEvents {
    private static final Set<UUID> EQUIPMENT_GUARD = ConcurrentHashMap.newKeySet();

    @SubscribeEvent
    public static void unequipWithEmptyHand(PlayerInteractEvent.EntityInteract event) {
        Player player = event.getEntity();
        if (player.level().isClientSide || !player.isShiftKeyDown() || !player.getItemInHand(event.getHand()).isEmpty()
                || !(event.getTarget() instanceof LivingEntity target) || !MobStreetGear.hasGear(target)) return;

        ItemStack recovered = MobStreetGear.unequip(target);
        if (!recovered.isEmpty()) {
            if (!player.addItem(recovered)) player.drop(recovered, false);
            player.swing(event.getHand(), true);
        }
        event.setCancellationResult(InteractionResult.SUCCESS);
        event.setCanceled(true);
    }

    @SubscribeEvent
    public static void observeNativeEquipment(LivingEquipmentChangeEvent event) {
        LivingEntity entity = event.getEntity();
        ItemStack incoming = event.getTo();
        if (entity.level().isClientSide || incoming.isEmpty() || !(incoming.getItem() instanceof RideGearItem)
                || !MobStreetGear.eligible(entity) || MobStreetGear.hasGear(entity)
                || !EQUIPMENT_GUARD.add(entity.getUUID())) return;
        try {
            if (MobStreetGear.equip(entity, incoming, StreetGearAcquisition.NATIVE_PICKUP, false).equipped()) {
                // Consume exactly one physical item. Never mutate the mob's persistent drop-chance rules, because
                // another mod may later reuse this slot for its own equipment.
                ItemStack remainder = incoming.copy();
                remainder.shrink(1);
                entity.setItemSlot(event.getSlot(), remainder);
            }
        } finally {
            EQUIPMENT_GUARD.remove(entity.getUUID());
        }
    }

    @SubscribeEvent
    public static void dropPhysicalGear(LivingDropsEvent event) {
        LivingEntity entity = event.getEntity();
        if (entity.level().isClientSide || entity instanceof Player || !MobStreetGear.hasGear(entity)) return;
        ItemStack gear = MobStreetGear.unequip(entity);
        if (!gear.isEmpty()) {
            event.getDrops().add(new ItemEntity(entity.level(), entity.getX(), entity.getY() + 0.25D,
                    entity.getZ(), gear));
        }
    }

    @SubscribeEvent
    public static void preserveConversion(LivingConversionEvent.Post event) {
        LivingEntity original = event.getEntity();
        LivingEntity outcome = event.getOutcome();
        if (original.level().isClientSide || !MobStreetGear.hasGear(original)) return;

        ItemStack gear = MobStreetGear.snapshot(original).stack().copy();
        MobStreetGear.EquipResult transfer = MobStreetGear.hasGear(outcome)
                ? MobStreetGear.EquipResult.rejected()
                : MobStreetGear.equip(outcome, gear, StreetGearAcquisition.RESTORED, false);
        // The old body is removed immediately after Forge's Post event. Clear its copy in every path, then
        // materialize the item if the destination is incompatible/already equipped so conversion never deletes gear.
        original.getPersistentData().remove(MobStreetGear.ROOT_KEY);
        if (!transfer.equipped() && !gear.isEmpty()) {
            outcome.level().addFreshEntity(new ItemEntity(outcome.level(), outcome.getX(), outcome.getY() + 0.25D,
                    outcome.getZ(), gear));
        }
    }

    @SubscribeEvent
    public static void beginTracking(PlayerEvent.StartTracking event) {
        if (event.getEntity() instanceof ServerPlayer player && event.getTarget() instanceof LivingEntity target
                && !(target instanceof Player)) {
            // Empty snapshots matter too: they clear stale client entries when an entity ID is reused.
            JetSetNetwork.syncMobGear(player, target);
        }
    }

    @SubscribeEvent
    public static void entityJoin(EntityJoinLevelEvent event) {
        if (!event.getLevel().isClientSide && event.getEntity() instanceof LivingEntity living
                && !(living instanceof Player) && MobStreetGear.hasStoredState(living)) {
            MobStreetGear.refreshRig(living);
        }
    }

    private MobStreetGearEvents() {}
}
