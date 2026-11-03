package com.herberto.jetsetcraft.liberty;

import net.minecraft.SharedConstants;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.SpawnGroupData;
import net.minecraft.world.entity.monster.Shulker;
import net.minecraft.world.entity.monster.Spider;
import net.minecraft.world.entity.monster.Zombie;
import net.minecraft.world.entity.projectile.ShulkerBullet;
import net.minecraft.world.entity.raid.Raider;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.BedBlock;
import net.minecraft.world.level.block.HayBlock;
import net.minecraft.world.level.block.HoneyBlock;
import net.minecraft.world.level.block.SlimeBlock;
import net.minecraftforge.event.ForgeEventFactory;

import java.util.List;

/** Native classes and combat AI, with child creation and ordinary-world side effects contained. */
public final class LibertyMobSafety {
    /** Silverfish alter infested blocks even with griefing denied; Skeleton has an unhooked Stray conversion. */
    public static EntityType<?> spawnType(EntityType<?> type, boolean home) {
        if (!home) return type;
        if (type == EntityType.SILVERFISH) return EntityType.CAVE_SPIDER;
        if (type == EntityType.SKELETON) return EntityType.STRAY;
        return type;
    }

    /** Runs genuine native initialization without allowing new or existing foreign jockeys. */
    public static void initialize(Mob mob, ServerLevel level, BlockPos ground) {
        SpawnGroupData group = mob instanceof Zombie ? new Zombie.ZombieGroupData(false, false) : null;
        ForgeEventFactory.onFinalizeSpawn(mob, level, level.getCurrentDifficultyAt(ground), MobSpawnType.EVENT, group, null);
        // Spider.finalizeSpawn can construct an unregistered Skeleton passenger. A normal entity add
        // does not admit that passenger, but keeping it would retain an unowned saved passenger tree.
        if (mob instanceof Spider) for (Entity passenger : List.copyOf(mob.getPassengers())) {
            passenger.stopRiding();
            if (!passenger.isAddedToWorld()) passenger.discard();
        }
        if (mob instanceof Raider raider) {
            raider.setCanJoinRaid(false);
            raider.setPatrolLeader(false);
        }
    }

    public static boolean blocksShulkerClone(Entity target, Entity direct) {
        return target instanceof Shulker && direct instanceof ShulkerBullet && LibertyManager.validOwned(target);
    }

    /** Authoritative session lookup, never a persistent ownership tag on its own. */
    public static boolean protectedHomeActor(Entity entity) {
        return entity != null && !entity.level().isClientSide && LibertyManager.protectedHomeActor(entity);
    }

    /** Home admission must not depend on a silently missing or differently mapped injection. */
    public static boolean homeGuardReady(Entity entity) {
        return "1.20.1".equals(SharedConstants.getCurrentVersion().getName())
                && entity instanceof LibertyHomeEntityGuard
                && (Object) Blocks.AIR.defaultBlockState() instanceof LibertyHomeBlockGuard;
    }

    /** Keep vanilla fall injury/cushioning while omitting callbacks that can mutate the home terrain. */
    public static void fallWithoutBlockCallbacks(Block block, Level level, Entity entity, float distance) {
        if (entity instanceof ServerPlayer player && LibertyManager.inTransit(player)) return;
        float adjustedDistance = block instanceof BedBlock ? distance * .5f : distance;
        float multiplier = block instanceof HayBlock || block instanceof HoneyBlock ? .2f
                : block instanceof SlimeBlock && !entity.isSuppressingBounce() ? 0 : 1;
        entity.causeFallDamage(adjustedDistance, multiplier, level.damageSources().fall());
    }

    private LibertyMobSafety() { }
}
