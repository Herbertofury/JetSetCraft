package com.herberto.jetsetcraft.rush;

import com.herberto.jetsetcraft.JetSetCraft;
import com.herberto.jetsetcraft.compat.RushSpellCompat;
import com.herberto.jetsetcraft.entity.PaintBalloonEntity;
import com.herberto.jetsetcraft.registry.ModBlocks;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.AreaEffectCloud;
import net.minecraft.world.entity.ExperienceOrb;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraftforge.event.AddReloadListenerEvent;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.EntityJoinLevelEvent;
import net.minecraftforge.event.entity.EntityMobGriefingEvent;
import net.minecraftforge.event.entity.living.LivingAttackEvent;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.event.entity.living.LivingDropsEvent;
import net.minecraftforge.event.entity.living.LivingExperienceDropEvent;
import net.minecraftforge.event.entity.living.LivingEvent;
import net.minecraftforge.event.entity.living.MobEffectEvent;
import net.minecraftforge.event.entity.player.EntityItemPickupEvent;
import net.minecraftforge.event.entity.player.AttackEntityEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.event.entity.player.PlayerXpEvent;
import net.minecraftforge.event.level.BlockEvent;
import net.minecraftforge.event.level.ExplosionEvent;
import net.minecraftforge.event.server.ServerStoppingEvent;
import net.minecraftforge.eventbus.api.Event;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

/** Boundaries apply only to an opted-in player, a run-owned entity, or the dedicated Rush dimension. */
@Mod.EventBusSubscriber(modid = JetSetCraft.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public final class RivalRushEvents {
    @SubscribeEvent
    public static void commands(RegisterCommandsEvent event) { RivalRushCommands.register(event.getDispatcher()); }

    @SubscribeEvent
    public static void reload(AddReloadListenerEvent event) { event.addListener(new RushRosters()); event.addListener(new RushArenaCatalog()); }

    @SubscribeEvent
    public static void tick(TickEvent.ServerTickEvent event) {
        if (event.phase == TickEvent.Phase.END) RivalRushManager.tick(event.getServer());
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public static void damage(LivingAttackEvent event) {
        if (event.getEntity() instanceof ServerPlayer player && RivalRushManager.active(player)) {
            event.setCanceled(true);
            // Only the owning run director spends virtual vitality. PvP, foreign projectiles, effects and
            // native mob damage cannot cross sessions or accidentally double an authored attack.
            return;
        }
        if (RivalRushManager.owned(event.getEntity())) {
            event.setCanceled(true);
            if (event.getSource().getEntity() instanceof ServerPlayer player) {
                var direct = event.getSource().getDirectEntity();
                if (direct == player || (direct instanceof Projectile shot && RushSpellCompat.allowed(shot)
                        && RivalRushManager.belongsTo(shot, player)))
                    RivalRushManager.tagEnemy(player, event.getEntity(), event.getAmount());
            }
        } else if (RivalRushManager.owned(event.getSource().getEntity())
                || RivalRushManager.owned(event.getSource().getDirectEntity())) event.setCanceled(true);
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public static void melee(AttackEntityEvent event) {
        if (event.getEntity() instanceof ServerPlayer player && RivalRushManager.active(player)) {
            event.setCanceled(true);
            RivalRushManager.tagEnemy(player, event.getTarget(), 4);
        }
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public static void pauseActor(LivingEvent.LivingTickEvent event) {
        if (!event.getEntity().level().isClientSide && RivalRushManager.pausedActor(event.getEntity())) event.setCanceled(true);
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public static void death(LivingDeathEvent event) {
        if (event.getEntity() instanceof ServerPlayer player && RivalRushManager.active(player)) {
            event.setCanceled(true);
            RivalRushManager.leave(player, "Run ended. Your Minecraft inventory and collected cards were preserved.");
        } else if (RivalRushManager.owned(event.getEntity())) {
            event.setCanceled(true); event.getEntity().discard();
        }
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public static void effect(MobEffectEvent.Applicable event) {
        if (event.getEntity() instanceof ServerPlayer player && RivalRushManager.active(player)
                && !RivalRushManager.restoring(player)) event.setResult(Event.Result.DENY);
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public static void join(EntityJoinLevelEvent event) {
        if (event.getLevel().isClientSide()) return;
        var entity = event.getEntity();
        if (RivalRushManager.owned(entity)) {
            if (!RivalRushManager.validateJoin(entity)) event.setCanceled(true);
            return;
        }
        if (!RushWorld.isRush(event.getLevel())) return;
        if (entity instanceof Projectile projectile && projectile.getOwner() instanceof ServerPlayer player
                && RushSpellCompat.allowed(projectile)) {
            RivalRushManager.markPlayerProjectile(projectile);
            if (!RivalRushManager.belongsTo(projectile, player)) event.setCanceled(true);
            return;
        }
        // Natural spawns, summoned children, dropped loan items, XP and native hostile projectiles are not run actors.
        if (entity instanceof Mob || entity instanceof ItemEntity || entity instanceof ExperienceOrb
                || entity instanceof Projectile || entity instanceof AreaEffectCloud) event.setCanceled(true);
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public static void pickup(EntityItemPickupEvent event) {
        if (RivalRushManager.active(event.getEntity()) || RivalRushManager.owned(event.getItem())) event.setCanceled(true);
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public static void xp(PlayerXpEvent.PickupXp event) {
        if (RivalRushManager.active(event.getEntity())) event.setCanceled(true);
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public static void xpChange(PlayerXpEvent.XpChange event) {
        if (RivalRushManager.active(event.getEntity())) event.setCanceled(true);
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public static void drops(LivingDropsEvent event) {
        if (RivalRushManager.owned(event.getEntity()) || RushWorld.isRush(event.getEntity().level())) {
            event.getDrops().clear(); event.setCanceled(true);
        }
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public static void xpDrops(LivingExperienceDropEvent event) {
        if (RivalRushManager.owned(event.getEntity()) || RushWorld.isRush(event.getEntity().level())) event.setDroppedExperience(0);
    }

    @SubscribeEvent
    public static void grief(EntityMobGriefingEvent event) {
        if (RushWorld.isRush(event.getEntity().level()) || RivalRushManager.owned(event.getEntity())) event.setResult(Event.Result.DENY);
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public static void explosion(ExplosionEvent.Detonate event) {
        if (RushWorld.isRush(event.getLevel())) { event.getAffectedBlocks().clear(); event.getAffectedEntities().clear(); }
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public static void breakBlock(BlockEvent.BreakEvent event) {
        if (event.getPlayer() != null && RivalRushManager.active(event.getPlayer())) event.setCanceled(true);
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public static void placeBlock(BlockEvent.EntityPlaceEvent event) {
        if (event.getEntity() instanceof Player player && RivalRushManager.active(player)) event.setCanceled(true);
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public static void interactEntity(PlayerInteractEvent.EntityInteract event) {
        if (RivalRushManager.active(event.getEntity())) { event.setCanceled(true); event.setCancellationResult(InteractionResult.FAIL); }
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public static void interactSpecific(PlayerInteractEvent.EntityInteractSpecific event) {
        if (RivalRushManager.active(event.getEntity())) { event.setCanceled(true); event.setCancellationResult(InteractionResult.FAIL); }
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public static void interactBlock(PlayerInteractEvent.RightClickBlock event) {
        if (!RivalRushManager.active(event.getEntity())) return;
        // Run Boomboxes are musical objectives, never the ordinary gang-summoning interface.
        if (event.getLevel().getBlockState(event.getPos()).is(ModBlocks.BOOMBOX.get())) {
            event.setCanceled(true); event.setCancellationResult(InteractionResult.SUCCESS);
        } else event.setUseBlock(Event.Result.DENY); // Item paint is still allowed on real wall faces.
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public static void login(PlayerEvent.PlayerLoggedInEvent event) {
        if (event.getEntity() instanceof ServerPlayer player) RivalRushManager.recover(player);
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public static void logout(PlayerEvent.PlayerLoggedOutEvent event) {
        if (event.getEntity() instanceof ServerPlayer player && (RivalRushManager.active(player) || RushReturnState.hasJournal(player))) {
            RivalRushManager.leave(player, "Session banked on disconnect.");
            // A failed restore retains its journal, but must not retain the disconnected ServerPlayer object.
            RivalRushManager.playerCloned(player);
        }
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public static void clonePlayer(PlayerEvent.Clone event) {
        // Forge's persisted subtree is also copied explicitly for clone providers with different ordering.
        CompoundTag persisted = event.getOriginal().getPersistentData().getCompound(Player.PERSISTED_NBT_TAG);
        CompoundTag destination = event.getEntity().getPersistentData().getCompound(Player.PERSISTED_NBT_TAG);
        if (persisted.contains(RushAlbum.KEY)) destination.put(RushAlbum.KEY, persisted.get(RushAlbum.KEY).copy());
        if (persisted.contains(RushReturnState.KEY)) destination.put(RushReturnState.KEY, persisted.get(RushReturnState.KEY).copy());
        event.getEntity().getPersistentData().put(Player.PERSISTED_NBT_TAG, destination);
        if (event.getOriginal() instanceof ServerPlayer original) RivalRushManager.playerCloned(original);
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public static void respawn(PlayerEvent.PlayerRespawnEvent event) {
        if (event.getEntity() instanceof ServerPlayer player) RivalRushManager.recover(player);
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public static void dimension(PlayerEvent.PlayerChangedDimensionEvent event) {
        if (event.getEntity() instanceof ServerPlayer player) RivalRushManager.dimensionChanged(player);
    }

    @SubscribeEvent
    public static void stop(ServerStoppingEvent event) { RivalRushManager.stopServer(event.getServer()); }

    private RivalRushEvents() {}
}
