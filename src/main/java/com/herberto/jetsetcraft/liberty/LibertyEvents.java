package com.herberto.jetsetcraft.liberty;

import com.herberto.jetsetcraft.JetSetCraft;
import com.herberto.jetsetcraft.compat.LibertyTaczCompat;
import com.herberto.jetsetcraft.entity.GraffitiEntity;
import com.herberto.jetsetcraft.entity.LibertyTransportEntity;
import net.minecraft.commands.Commands;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.AreaEffectCloud;
import net.minecraft.world.entity.ExperienceOrb;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.VanillaGameEvent;
import net.minecraftforge.event.entity.EntityJoinLevelEvent;
import net.minecraftforge.event.entity.EntityMountEvent;
import net.minecraftforge.event.entity.EntityMobGriefingEvent;
import net.minecraftforge.event.entity.EntityTeleportEvent;
import net.minecraftforge.event.entity.living.LivingAttackEvent;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.event.entity.living.LivingDropsEvent;
import net.minecraftforge.event.entity.living.LivingExperienceDropEvent;
import net.minecraftforge.event.entity.living.LivingConversionEvent;
import net.minecraftforge.event.entity.living.ZombieEvent;
import net.minecraftforge.event.entity.item.ItemTossEvent;
import net.minecraftforge.event.entity.player.EntityItemPickupEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.event.entity.player.PlayerContainerEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.event.entity.player.PlayerXpEvent;
import net.minecraftforge.event.entity.player.AttackEntityEvent;
import net.minecraftforge.event.entity.ProjectileImpactEvent;
import net.minecraftforge.event.level.BlockEvent;
import net.minecraftforge.event.level.ExplosionEvent;
import net.minecraftforge.event.server.ServerStoppingEvent;
import net.minecraftforge.eventbus.api.Event;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

/** Containment is limited to expedition participants, owned entities and the dedicated dimension. */
@Mod.EventBusSubscriber(modid = JetSetCraft.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public final class LibertyEvents {
    @SubscribeEvent public static void commands(RegisterCommandsEvent event) {
        var liberty = Commands.literal("liberty")
                .executes(context -> { LibertyManager.open(context.getSource().getPlayerOrException()); return 1; })
                .then(Commands.literal("menu").executes(context -> { LibertyManager.open(context.getSource().getPlayerOrException()); return 1; }))
                .then(Commands.literal("return").executes(context -> LibertyManager.leave(context.getSource().getPlayerOrException(), false, "Returned from Liberty Grind. Field samples left behind.") ? 1 : 0));
        for (LibertyOperation.Theater theater : LibertyOperation.THEATERS)
            liberty.then(Commands.literal(theater.id()).executes(context -> LibertyManager.startCampaign(context.getSource().getPlayerOrException(), theater.id()+"|random|3|25") ? 1 : 0));
        event.getDispatcher().register(Commands.literal("jetsetcraft").then(liberty));
    }
    @SubscribeEvent public static void tick(TickEvent.ServerTickEvent event) {
        if (event.phase == TickEvent.Phase.END) LibertyManager.tick(event.getServer());
    }
    @SubscribeEvent(priority = EventPriority.LOWEST, receiveCanceled = true)
    public static void teleport(EntityTeleportEvent event) {
        if (event.getEntity() instanceof ServerPlayer player) LibertyManager.movementDiscontinuity(player);
    }
    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public static void damage(LivingAttackEvent event) {
        if (LibertyMobSafety.blocksShulkerClone(event.getEntity(), event.getSource().getDirectEntity())
                || !LibertyManager.allowedDamage(event.getEntity(), event.getSource().getEntity(), event.getSource().getDirectEntity())) event.setCanceled(true);
    }
    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public static void zombieAid(ZombieEvent.SummonAidEvent event) {
        if (LibertyManager.validOwned(event.getEntity())) event.setResult(Event.Result.DENY);
    }
    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public static void conversion(LivingConversionEvent.Pre event) {
        if (LibertyManager.validOwned(event.getEntity())) event.setCanceled(true);
    }
    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public static void homeVibration(VanillaGameEvent event) {
        if (LibertyMobSafety.protectedHomeActor(event.getCause())) event.setCanceled(true);
    }
    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public static void homeTrample(BlockEvent.FarmlandTrampleEvent event) {
        if (LibertyMobSafety.protectedHomeActor(event.getEntity())) event.setCanceled(true);
    }
    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public static void homeBlockAttack(PlayerInteractEvent.LeftClickBlock event) {
        if (LibertyMobSafety.protectedHomeActor(event.getEntity())) event.setCanceled(true);
    }
    @SubscribeEvent(priority = EventPriority.LOWEST)
    public static void protection(LivingHurtEvent event) {
        if (event.getEntity() instanceof ServerPlayer player)
            event.setAmount(LibertyManager.protectedDamage(player,event.getSource(),event.getAmount()));
    }
    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public static void death(LivingDeathEvent event) {
        if (event.getEntity() instanceof ServerPlayer player && LibertyManager.active(player)) {
            event.setCanceled(true); player.setHealth(1);
            if (LibertyManager.downed(player)) return;
            LibertyManager.leave(player, false, "Operation lost. Earned credits and completed-objective crew progress retained; field samples left behind.");
        } else if (event.getEntity() instanceof Mob mob && LibertyManager.owned(mob)) {
            LibertyManager.defeated(mob, event.getSource().getEntity());
        }
    }
    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public static void join(EntityJoinLevelEvent event) {
        if (event.getLevel().isClientSide) return;
        var entity = event.getEntity();
        if (LibertyManager.owned(entity)) {
            if (!LibertyManager.validOwned(entity) || entity instanceof Projectile
                    && !com.herberto.jetsetcraft.compat.LibertyHelldiversCompat.protectTaczTerrain(entity)) event.setCanceled(true);
            return;
        }
        if (LibertyManager.adoptProviderEntity(entity)) return;
        if (entity instanceof ItemEntity item && LibertyTaczCompat.isLoan(item.getItem())) { event.setCanceled(true); return; }
        if (entity instanceof Projectile projectile) {
            LibertyManager.markProjectile(projectile);
            var shooter = projectile.getOwner();
            boolean expeditionSource = shooter instanceof Player player && LibertyManager.active(player)
                    || LibertyManager.owned(shooter);
            // Hitting the owned-projectile cap must reject the new shot. In a home world it must
            // never fall through as an ordinary unowned projectile with native side effects.
            if ((expeditionSource || LibertyManager.owned(projectile) || LibertyWorld.isLiberty(event.getLevel()))
                    && !LibertyManager.validOwned(projectile)) { event.setCanceled(true); return; }
            if (LibertyManager.owned(projectile) && !com.herberto.jetsetcraft.compat.LibertyHelldiversCompat.protectTaczTerrain(projectile)) {
                event.setCanceled(true); return;
            }
            return;
        }
        if (!LibertyWorld.isLiberty(event.getLevel())) return;
        if (entity instanceof GraffitiEntity graffiti) LibertyManager.markGraffiti(graffiti);
        else if (entity instanceof Mob || entity instanceof ItemEntity || entity instanceof ExperienceOrb
                || entity instanceof AreaEffectCloud || entity instanceof LibertyTransportEntity) event.setCanceled(true);
    }
    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public static void toss(ItemTossEvent event) {
        if (LibertyManager.active(event.getPlayer()) && LibertyTaczCompat.isLoan(event.getEntity().getItem(), event.getPlayer().getUUID())) {
            event.setCanceled(true);
            // Forge has already removed the tossed stack. Return it to the vacated inventory space.
            ItemStack stack = event.getEntity().getItem();
            if (!event.getPlayer().getInventory().add(stack))
                event.getPlayer().getInventory().placeItemBackInInventory(stack);
            event.getPlayer().inventoryMenu.broadcastChanges();
        }
    }
    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public static void pickup(EntityItemPickupEvent event) {
        if (LibertyManager.active(event.getEntity()) || LibertyManager.owned(event.getItem()) || LibertyTaczCompat.isLoan(event.getItem().getItem())) {
            event.setCanceled(true);
            if (event.getEntity() instanceof ServerPlayer player) LibertyManager.collect(player, event.getItem());
        }
    }
    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public static void drops(LivingDropsEvent event) {
        if (LibertyManager.owned(event.getEntity()) || LibertyWorld.isLiberty(event.getEntity().level())) { event.getDrops().clear(); event.setCanceled(true); }
    }
    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public static void xpDrops(LivingExperienceDropEvent event) {
        if (LibertyManager.owned(event.getEntity()) || LibertyWorld.isLiberty(event.getEntity().level())) event.setDroppedExperience(0);
    }
    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public static void xp(PlayerXpEvent.PickupXp event) { if (LibertyManager.active(event.getEntity())) event.setCanceled(true); }
    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public static void xpChange(PlayerXpEvent.XpChange event) { if (LibertyManager.active(event.getEntity())) event.setCanceled(true); }
    @SubscribeEvent public static void grief(EntityMobGriefingEvent event) {
        if (LibertyWorld.isLiberty(event.getEntity().level()) || LibertyManager.owned(event.getEntity())) event.setResult(Event.Result.DENY);
    }
    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public static void explosion(ExplosionEvent.Detonate event) {
        var source=event.getExplosion().getIndirectSourceEntity(); var direct=event.getExplosion().getDirectSourceEntity();
        if (LibertyWorld.isLiberty(event.getLevel()) || LibertyManager.owned(source) || LibertyManager.owned(direct)
                || source instanceof Player player && LibertyManager.active(player)) {
            event.getAffectedBlocks().clear();
            event.getAffectedEntities().removeIf(entity -> !LibertyManager.allowedDamage(entity,
                    event.getExplosion().getIndirectSourceEntity(), event.getExplosion().getDirectSourceEntity()));
        }
    }
    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public static void breakBlock(BlockEvent.BreakEvent event) {
        if (event.getPlayer() != null && (LibertyWorld.isLiberty(event.getPlayer().level()) || LibertyManager.active(event.getPlayer()))) event.setCanceled(true);
    }
    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public static void placeBlock(BlockEvent.EntityPlaceEvent event) {
        if (event.getEntity() instanceof Player player && (LibertyWorld.isLiberty(player.level()) || LibertyManager.active(player))) event.setCanceled(true);
    }
    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public static void attackEntity(AttackEntityEvent event) {
        if (LibertyManager.active(event.getEntity()) && (!(event.getEntity() instanceof ServerPlayer player) || !LibertyManager.belongsTo(event.getTarget(),player))) event.setCanceled(true);
    }
    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public static void projectileImpact(ProjectileImpactEvent event) {
        if (LibertyProjectileSafety.intercept(event.getProjectile(), event.getRayTraceResult())) event.setCanceled(true);
    }
    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public static void interactEntity(PlayerInteractEvent.EntityInteract event) {
        if (LibertyManager.active(event.getEntity())) {
            if (event.getTarget() instanceof com.herberto.jetsetcraft.entity.LibertySupportEntity support
                    && event.getEntity() instanceof ServerPlayer player && LibertyManager.belongsTo(support,player)) return;
            if (event.getEntity() instanceof ServerPlayer player && event.getTarget() instanceof LibertyTransportEntity
                    && LibertyManager.belongsTo(event.getTarget(), player) && event.getHand() == net.minecraft.world.InteractionHand.MAIN_HAND) {
                LibertyOperation op = LibertyManager.operation(player);
                if (op!=null) LibertyManager.handleAction(player, "board", "", op.id, op.revision());
            }
            event.setCanceled(true); event.setCancellationResult(InteractionResult.SUCCESS);
        }
    }
    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public static void interactSpecific(PlayerInteractEvent.EntityInteractSpecific event) {
        if (event.getTarget() instanceof com.herberto.jetsetcraft.entity.LibertySupportEntity && event.getEntity() instanceof ServerPlayer player
                && LibertyManager.belongsTo(event.getTarget(),player)) return;
        if (LibertyManager.active(event.getEntity())) { event.setCanceled(true); event.setCancellationResult(InteractionResult.FAIL); }
    }
    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public static void interactBlock(PlayerInteractEvent.RightClickBlock event) {
        if (event.getEntity() instanceof ServerPlayer player && LibertyManager.homeDefense(player)
                && event.getItemStack().getItem() instanceof com.herberto.jetsetcraft.item.SprayCanItem && !player.isShiftKeyDown()) {
            boolean painted=LibertyManager.safeHomeSpray(player,event.getPos(),event.getFace());
            event.setCanceled(true); event.setCancellationResult(painted?InteractionResult.SUCCESS:InteractionResult.FAIL); return;
        }
        if (LibertyManager.active(event.getEntity())) event.setUseBlock(Event.Result.DENY); // item spraying still uses real successful placement
    }
    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public static void radio(PlayerInteractEvent.RightClickItem event) {
        if (LibertyManager.active(event.getEntity()) && event.getItemStack().hasTag()
                && event.getItemStack().getTag().contains("jetsetcraft_liberty_equipment")) {
            if (event.getEntity() instanceof ServerPlayer player) LibertyManager.useEquipment(player,event.getHand(),player.isShiftKeyDown());
            event.setCanceled(true); event.setCancellationResult(InteractionResult.SUCCESS); return;
        }
        if (LibertyManager.active(event.getEntity()) && LibertyTaczCompat.isLoan(event.getItemStack(), event.getEntity().getUUID())
                && event.getItemStack().getOrCreateTag().getBoolean("LibertyFieldRadio")) {
            if (event.getEntity() instanceof ServerPlayer player) LibertyManager.open(player);
            event.setCanceled(true); event.setCancellationResult(InteractionResult.SUCCESS);
        }
    }
    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public static void container(PlayerContainerEvent.Open event) {
        if (event.getEntity() instanceof ServerPlayer player && LibertyManager.active(player)
                && player.containerMenu != player.inventoryMenu) player.closeContainer();
    }
    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public static void mount(EntityMountEvent event) {
        if (event.getEntityMounting() instanceof ServerPlayer player && LibertyManager.inTransit(player)
                && !LibertyManager.restoring(player) && event.isDismounting()) event.setCanceled(true);
    }
    @SubscribeEvent(priority = EventPriority.LOWEST)
    public static void login(PlayerEvent.PlayerLoggedInEvent event) { if (event.getEntity() instanceof ServerPlayer player) LibertyManager.recover(player); }
    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public static void logout(PlayerEvent.PlayerLoggedOutEvent event) {
        if (event.getEntity() instanceof ServerPlayer player) {
            if (LibertyManager.active(player) || LibertyManager.hasReturn(player)) LibertyManager.leave(player, false, "Disconnected. Earned super credits and crew progress retained; field samples lost.");
            LibertyManager.forget(player);
        }
    }
    @SubscribeEvent(priority = EventPriority.LOWEST)
    public static void clonePlayer(PlayerEvent.Clone event) {
        CompoundTag before = event.getOriginal().getPersistentData().getCompound(Player.PERSISTED_NBT_TAG);
        CompoundTag after = event.getEntity().getPersistentData().getCompound(Player.PERSISTED_NBT_TAG);
        for (String key : new String[]{LibertyManager.RETURN_KEY, LibertyManager.OUTCOME_KEY, LibertyManager.DEBRIEF_KEY, LibertyResearch.KEY, LibertyShipProgress.KEY, LibertyArmory.KEY})
            if (before.contains(key)) after.put(key, before.get(key).copy());
        event.getEntity().getPersistentData().put(Player.PERSISTED_NBT_TAG, after);
        if (event.getOriginal() instanceof ServerPlayer player) LibertyManager.forget(player);
    }
    @SubscribeEvent(priority = EventPriority.LOWEST)
    public static void respawn(PlayerEvent.PlayerRespawnEvent event) { if (event.getEntity() instanceof ServerPlayer player) LibertyManager.recover(player); }
    @SubscribeEvent(priority = EventPriority.LOWEST)
    public static void dimension(PlayerEvent.PlayerChangedDimensionEvent event) { if (event.getEntity() instanceof ServerPlayer player) LibertyManager.dimensionChanged(player); }
    @SubscribeEvent public static void stop(ServerStoppingEvent event) { LibertyManager.stop(event.getServer()); }
    private LibertyEvents() {}
}
