package com.herberto.jetsetcraft.activity;

import com.herberto.jetsetcraft.JetSetCraft;
import com.herberto.jetsetcraft.entity.PaintBalloonEntity;
import net.minecraft.commands.Commands;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.EntityJoinLevelEvent;
import net.minecraftforge.event.entity.EntityMobGriefingEvent;
import net.minecraftforge.event.entity.ProjectileImpactEvent;
import net.minecraftforge.event.entity.living.LivingAttackEvent;
import net.minecraftforge.event.entity.living.LivingConversionEvent;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.event.entity.living.LivingDropsEvent;
import net.minecraftforge.event.entity.living.LivingExperienceDropEvent;
import net.minecraftforge.event.entity.living.MobEffectEvent;
import net.minecraftforge.event.entity.player.AttackEntityEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.event.level.ExplosionEvent;
import net.minecraftforge.event.server.ServerStoppingEvent;
import net.minecraftforge.eventbus.api.Event;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

/** Rules apply only to marked activity actors. Ordinary mobs, players, inventory and XP retain vanilla rules. */
@Mod.EventBusSubscriber(modid = JetSetCraft.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public final class ActivityEvents {
    @SubscribeEvent
    public static void commands(RegisterCommandsEvent event) {
        var activity = Commands.literal("activity").executes(context -> {
            ActivityManager.open(context.getSource().getPlayerOrException()); return 1;
        });
        for (ActivityMode mode : ActivityMode.values())
            activity.then(Commands.literal(mode.id).executes(context -> {
                ActivityManager.invite(context.getSource().getPlayerOrException(), mode.id); return 1;
            }));
        activity.then(Commands.literal("cancel").executes(context -> {
            ActivityManager.cancel(context.getSource().getPlayerOrException(), "Activity cancelled. No completion reward was claimed."); return 1;
        }));
        event.getDispatcher().register(Commands.literal("jetsetcraft").then(activity));
    }

    @SubscribeEvent
    public static void tick(TickEvent.ServerTickEvent event) {
        if (event.phase == TickEvent.Phase.END) ActivityManager.tick(event.getServer());
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public static void melee(AttackEntityEvent event) {
        if (!ActivityManager.owned(event.getTarget())) return;
        event.setCanceled(true);
        if (event.getEntity() instanceof ServerPlayer player) ActivityManager.tag(player, event.getTarget(), false);
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public static void damage(LivingAttackEvent event) {
        if (ActivityManager.owned(event.getEntity())) {
            event.setCanceled(true);
            if (event.getSource().getEntity() instanceof ServerPlayer player)
                ActivityManager.tag(player, event.getEntity(), event.getSource().getDirectEntity() instanceof Projectile);
        } else if (ActivityManager.owned(event.getSource().getEntity())
                || ActivityManager.owned(event.getSource().getDirectEntity())) event.setCanceled(true);
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public static void balloon(ProjectileImpactEvent event) {
        if (event.getProjectile() instanceof PaintBalloonEntity balloon && balloon.getOwner() instanceof ServerPlayer player
                && ActivityManager.balloonHit(player, event.getRayTraceResult())) {
            event.setCanceled(true); balloon.discard();
        }
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public static void death(LivingDeathEvent event) {
        if (ActivityManager.owned(event.getEntity())) { event.setCanceled(true); event.getEntity().discard(); }
        else if (event.getEntity() instanceof ServerPlayer player)
            ActivityManager.cancel(player, "Street activity ended on death. No completion reward was claimed.");
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public static void effect(MobEffectEvent.Applicable event) {
        if (ActivityManager.owned(event.getEntity())) event.setResult(Event.Result.DENY);
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public static void conversion(LivingConversionEvent.Pre event) {
        if (ActivityManager.owned(event.getEntity())) event.setCanceled(true);
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public static void join(EntityJoinLevelEvent event) {
        if (event.getLevel().isClientSide()) return;
        if (ActivityManager.owned(event.getEntity()) && !ActivityManager.validateJoin(event.getEntity())) {
            event.setCanceled(true); event.getEntity().discard();
        } else if (event.getEntity() instanceof Projectile projectile && ActivityManager.owned(projectile.getOwner()))
            event.setCanceled(true);
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public static void interact(PlayerInteractEvent.EntityInteract event) {
        if (ActivityManager.owned(event.getTarget())) { event.setCanceled(true); event.setCancellationResult(InteractionResult.FAIL); }
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public static void interactSpecific(PlayerInteractEvent.EntityInteractSpecific event) {
        if (ActivityManager.owned(event.getTarget())) { event.setCanceled(true); event.setCancellationResult(InteractionResult.FAIL); }
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public static void drops(LivingDropsEvent event) {
        if (ActivityManager.owned(event.getEntity())) { event.getDrops().clear(); event.setCanceled(true); }
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public static void xpDrops(LivingExperienceDropEvent event) {
        if (ActivityManager.owned(event.getEntity())) event.setDroppedExperience(0);
    }

    @SubscribeEvent
    public static void grief(EntityMobGriefingEvent event) {
        if (ActivityManager.owned(event.getEntity())) event.setResult(Event.Result.DENY);
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public static void explosion(ExplosionEvent.Detonate event) {
        if (ActivityManager.owned(event.getExplosion().getDirectSourceEntity())) {
            event.getAffectedBlocks().clear(); event.getAffectedEntities().clear();
        }
    }

    @SubscribeEvent
    public static void logout(PlayerEvent.PlayerLoggedOutEvent event) {
        if (event.getEntity() instanceof ServerPlayer player) ActivityManager.forget(player);
    }

    @SubscribeEvent
    public static void dimension(PlayerEvent.PlayerChangedDimensionEvent event) {
        if (event.getEntity() instanceof ServerPlayer player)
            ActivityManager.cancel(player, "Street activity ended when you changed dimension. No completion reward was claimed.");
    }

    @SubscribeEvent
    public static void clone(PlayerEvent.Clone event) {
        if (event.getOriginal() instanceof ServerPlayer player) ActivityManager.forget(player);
    }

    @SubscribeEvent
    public static void stop(ServerStoppingEvent event) { ActivityManager.stop(event.getServer()); }
    private ActivityEvents() {}
}
