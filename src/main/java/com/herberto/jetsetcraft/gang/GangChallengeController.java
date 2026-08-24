package com.herberto.jetsetcraft.gang;

import com.herberto.jetsetcraft.JetSetCraft;
import com.herberto.jetsetcraft.blockentity.BoomboxBlockEntity;
import com.herberto.jetsetcraft.config.JetSetConfig;
import com.herberto.jetsetcraft.registry.ModSounds;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/** Lightweight server-authoritative lifecycle for deliberate Boombox crews. No background world scan or chunk tickets. */
public final class GangChallengeController {
    private static final long ENTRANCE_DELAY = 16L;
    private static final long MEMBER_STAGGER = 12L;
    private static final long STATUS_PERIOD = 20L;

    public static boolean start(ServerLevel level, BlockPos pos, BoomboxBlockEntity boombox, ServerPlayer player) {
        if (boombox.isChallengeActive()) return false;
        Optional<HeadGangTargetResolver.Target> target = boombox.resolvedTarget();
        if (target.isEmpty()) target = chooseContextTarget(level, pos, player);
        if (target.isEmpty()) {
            player.displayClientMessage(Component.translatable("message.jetsetcraft.boombox_no_gang"), true);
            return false;
        }

        HeadGangTargetResolver.Target chosen = target.get();
        GangDefinition definition = GangRegistry.definitionForEntity(chosen.entityId());
        if (definition.legendary() || !definition.boomboxEligible()) {
            player.displayClientMessage(Component.translatable("message.jetsetcraft.boombox_legendary_locked",
                    definition.canonicalName()), true);
            return false;
        }

        int min = Math.max(1, definition.minActors());
        int max = Math.max(min, definition.maxActors());
        int configuredMax = JetSetConfig.SERVER.boomboxMaxActors.get();
        max = Math.min(max, Math.max(1, configuredMax));
        min = Math.min(min, max);
        int difficultyBonus = Math.max(0, level.getDifficulty().getId() - 1);
        int actorCount = Math.min(max, min + difficultyBonus);
        long now = level.getGameTime();
        long lifetime = JetSetConfig.SERVER.boomboxChallengeLifetimeTicks.get();
        boombox.beginChallenge(player, chosen, actorCount, now + ENTRANCE_DELAY, now + lifetime);

        ModSounds.music(definition.id()).ifPresent(sound ->
                level.playSound(null, pos, sound, SoundSource.RECORDS, 0.95F, 1.0F));
        level.playSound(null, pos, SoundEvents.NOTE_BLOCK_BASS.value(), SoundSource.RECORDS, 0.7F, 1.08F);
        level.sendParticles(ParticleTypes.NOTE, pos.getX() + 0.5D, pos.getY() + 1.15D, pos.getZ() + 0.5D,
                14, 0.55D, 0.35D, 0.55D, 0.05D);
        player.displayClientMessage(Component.translatable("message.jetsetcraft.boombox_challenge_started",
                definition.canonicalName(), actorCount), false);
        return true;
    }

    public static void tick(ServerLevel level, BlockPos pos, BoomboxBlockEntity boombox) {
        if (!boombox.isChallengeActive()) return;
        long now = level.getGameTime();
        if (now >= boombox.expiresAt()) {
            notifyOwner(level, boombox, Component.translatable("message.jetsetcraft.boombox_challenge_expired"));
            boombox.cancelChallenge(true);
            return;
        }

        if (boombox.spawnAttempts() < boombox.plannedActors() && now >= boombox.nextSpawnAt()) {
            HeadGangTargetResolver.Target target = boombox.activeTarget();
            double radius = JetSetConfig.SERVER.boomboxSpawnRadius.get();
            Optional<Mob> actor = GangActorFactory.spawn(level, pos, target, boombox.challengeId(),
                    boombox.spawnAttempts(), boombox.expiresAt(), radius);
            boombox.recordSpawnAttempt(actor.map(Entity::getUUID).orElse(null), now + MEMBER_STAGGER);
            if (actor.isPresent()) {
                Mob mob = actor.get();
                level.sendParticles(ParticleTypes.ELECTRIC_SPARK, mob.getX(), mob.getY() + mob.getBbHeight() * 0.5D,
                        mob.getZ(), 18, 0.35D, 0.45D, 0.35D, 0.04D);
                level.playSound(null, mob.blockPosition(), SoundEvents.NOTE_BLOCK_HAT.value(), SoundSource.RECORDS,
                        0.65F, 0.8F + (boombox.spawnAttempts() % 5) * 0.08F);
            }
            return;
        }

        if (boombox.spawnAttempts() < boombox.plannedActors() || now < boombox.nextStatusAt()) return;
        boombox.scheduleStatus(now + STATUS_PERIOD);
        int living = 0;
        for (UUID actorId : boombox.actorIds()) {
            Entity actor = level.getEntity(actorId);
            if (actor instanceof Mob mob && mob.isAlive() && GangMemberState.matchesChallenge(mob, boombox.challengeId())) {
                living++;
            }
        }
        if (living == 0 && now >= boombox.startedAt() + 50L) {
            GangDefinition definition = GangRegistry.byId(boombox.challengeGangId())
                    .orElse(GangRegistry.definitionForEntity(boombox.challengeEntityId()));
            notifyOwner(level, boombox, Component.translatable("message.jetsetcraft.boombox_challenge_complete",
                    definition.canonicalName()));
            level.playSound(null, pos, SoundEvents.PLAYER_LEVELUP, SoundSource.RECORDS, 0.7F, 1.35F);
            level.sendParticles(ParticleTypes.FIREWORK, pos.getX() + 0.5D, pos.getY() + 1.4D, pos.getZ() + 0.5D,
                    24, 0.7D, 0.5D, 0.7D, 0.08D);
            boombox.completeChallenge();
        }
    }

    public static void removeActors(ServerLevel level, Collection<UUID> actorIds, UUID challengeId) {
        if (level == null || actorIds == null || challengeId == null) return;
        for (UUID id : actorIds) {
            Entity entity = level.getEntity(id);
            if (entity instanceof Mob mob && GangMemberState.matchesChallenge(mob, challengeId)
                    && GangMemberState.snapshot(mob).ephemeral()) {
                mob.discard();
            }
        }
    }

    private static Optional<HeadGangTargetResolver.Target> chooseContextTarget(ServerLevel level, BlockPos pos,
                                                                               ServerPlayer player) {
        // Nearby source mobs are the strongest context signal and are only scanned when the player explicitly activates.
        List<ResourceLocation> candidates = new ArrayList<>();
        AABB nearby = new AABB(pos).inflate(18.0D, 8.0D, 18.0D);
        for (Mob mob : level.getEntitiesOfClass(Mob.class, nearby, mob -> mob.isAlive() && !GangMemberState.snapshot(mob).ephemeral())) {
            ResourceLocation entityId = ForgeRegistries.ENTITY_TYPES.getKey(mob.getType());
            if (entityId != null && isBoomboxEligible(entityId)) candidates.add(entityId);
        }
        if (candidates.isEmpty()) {
            for (ResourceLocation entityId : GangRegistry.curatedEntityIds()) {
                if (dimensionAllows(level.dimension(), entityId) && isBoomboxEligible(entityId)) candidates.add(entityId);
            }
        }
        if (candidates.isEmpty()) return Optional.empty();
        Collections.shuffle(candidates, new java.util.Random(level.getSeed() ^ pos.asLong() ^ level.getGameTime() ^ player.getUUID().getLeastSignificantBits()));
        ResourceLocation entityId = candidates.get(0);
        return Optional.of(new HeadGangTargetResolver.Target(entityId, GangRegistry.gangIdForEntity(entityId),
                HeadGangTargetResolver.ResolutionSource.REGISTRY_CONVENTION));
    }

    private static boolean isBoomboxEligible(ResourceLocation entityId) {
        EntityType<?> type = ForgeRegistries.ENTITY_TYPES.getValue(entityId);
        if (type == null) return false;
        GangDefinition definition = GangRegistry.definitionForEntity(entityId);
        if (!definition.boomboxEligible() || definition.legendary()) return false;
        return type.getCategory() != net.minecraft.world.entity.MobCategory.MISC;
    }

    private static boolean dimensionAllows(ResourceKey<Level> dimension, ResourceLocation entityId) {
        String path = entityId.getPath();
        if (dimension == Level.NETHER) {
            return List.of("piglin", "piglin_brute", "zombified_piglin", "hoglin", "zoglin", "blaze",
                    "ghast", "magma_cube", "strider", "wither_skeleton").contains(path);
        }
        if (dimension == Level.END) {
            return List.of("enderman", "shulker").contains(path);
        }
        return !List.of("piglin", "piglin_brute", "zombified_piglin", "hoglin", "zoglin", "blaze", "ghast",
                "magma_cube", "strider", "wither_skeleton", "shulker").contains(path);
    }

    private static void notifyOwner(ServerLevel level, BoomboxBlockEntity boombox, Component message) {
        UUID owner = boombox.ownerId();
        if (owner == null) return;
        ServerPlayer player = level.getServer().getPlayerList().getPlayer(owner);
        if (player != null) player.displayClientMessage(message, false);
    }

    private GangChallengeController() {}
}
