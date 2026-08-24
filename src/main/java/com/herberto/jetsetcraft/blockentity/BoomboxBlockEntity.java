package com.herberto.jetsetcraft.blockentity;

import com.herberto.jetsetcraft.block.BoomboxBlock;
import com.herberto.jetsetcraft.gang.GangChallengeController;
import com.herberto.jetsetcraft.gang.GangDefinition;
import com.herberto.jetsetcraft.gang.GangRegistry;
import com.herberto.jetsetcraft.gang.HeadGangTargetResolver;
import com.herberto.jetsetcraft.registry.ModBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/** One-slot physical gang tuner plus compact authoritative challenge state. No menu/container takeover is required. */
public final class BoomboxBlockEntity extends BlockEntity {
    private static final int SCHEMA = 1;

    private ItemStack targetStack = ItemStack.EMPTY;
    private UUID challengeId;
    private UUID ownerId;
    private ResourceLocation challengeEntityId;
    private ResourceLocation challengeGangId;
    private long startedAt;
    private long nextSpawnAt;
    private long expiresAt;
    private long nextStatusAt;
    private int plannedActors;
    private int spawnAttempts;
    private final List<UUID> actorIds = new ArrayList<>();

    public BoomboxBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.BOOMBOX.get(), pos, state);
    }

    public ItemStack targetStack() {
        return targetStack.copy();
    }

    public Optional<HeadGangTargetResolver.Target> resolvedTarget() {
        return HeadGangTargetResolver.resolve(targetStack);
    }

    public boolean setTarget(ItemStack stack) {
        if (level == null || level.isClientSide || stack == null || stack.isEmpty() || !targetStack.isEmpty()) return false;
        Optional<HeadGangTargetResolver.Target> resolved = HeadGangTargetResolver.resolve(stack);
        if (resolved.isEmpty()) return false;
        ItemStack physical = stack.copy();
        physical.setCount(1);
        targetStack = physical;
        sync();
        return true;
    }

    public ItemStack removeTarget() {
        if (level == null || level.isClientSide || targetStack.isEmpty()) return ItemStack.EMPTY;
        ItemStack removed = targetStack.copy();
        targetStack = ItemStack.EMPTY;
        sync();
        return removed;
    }

    public boolean isChallengeActive() {
        return challengeId != null && challengeGangId != null && challengeEntityId != null;
    }

    public UUID challengeId() {
        return challengeId;
    }

    public UUID ownerId() {
        return ownerId;
    }

    public ResourceLocation challengeEntityId() {
        return challengeEntityId;
    }

    public ResourceLocation challengeGangId() {
        return challengeGangId;
    }

    public long startedAt() {
        return startedAt;
    }

    public long nextSpawnAt() {
        return nextSpawnAt;
    }

    public long expiresAt() {
        return expiresAt;
    }

    public long nextStatusAt() {
        return nextStatusAt;
    }

    public int plannedActors() {
        return plannedActors;
    }

    public int spawnAttempts() {
        return spawnAttempts;
    }

    public List<UUID> actorIds() {
        return List.copyOf(actorIds);
    }

    public Component displayGangName(ResourceLocation gangId) {
        return GangRegistry.byId(gangId).<Component>map(def -> Component.literal(def.canonicalName()))
                .orElseGet(() -> Component.literal(gangId == null ? "Unknown Crew" : gangId.toString()));
    }

    public void toggleChallenge(ServerPlayer player) {
        if (level == null || level.isClientSide) return;
        if (isChallengeActive()) {
            cancelChallenge(true);
            player.displayClientMessage(Component.translatable("message.jetsetcraft.boombox_cancelled"), true);
            return;
        }
        GangChallengeController.start((ServerLevel) level, worldPosition, this, player);
    }

    public void beginChallenge(ServerPlayer player, HeadGangTargetResolver.Target target, int actorCount,
                               long nextSpawnAt, long expiresAt) {
        if (level == null || level.isClientSide || target == null || player == null) return;
        this.challengeId = UUID.randomUUID();
        this.ownerId = player.getUUID();
        this.challengeEntityId = target.entityId();
        this.challengeGangId = target.gangId();
        this.startedAt = level.getGameTime();
        this.nextSpawnAt = Math.max(this.startedAt + 1L, nextSpawnAt);
        this.expiresAt = Math.max(this.nextSpawnAt + 20L, expiresAt);
        this.nextStatusAt = this.startedAt + 20L;
        this.plannedActors = Math.max(1, actorCount);
        this.spawnAttempts = 0;
        this.actorIds.clear();
        setBlockActive(true);
        sync();
    }

    public HeadGangTargetResolver.Target activeTarget() {
        if (!isChallengeActive()) return null;
        return new HeadGangTargetResolver.Target(challengeEntityId, challengeGangId,
                HeadGangTargetResolver.ResolutionSource.EXPLICIT_METADATA);
    }

    public void recordSpawnAttempt(@Nullable UUID actorId, long nextSpawnAt) {
        spawnAttempts++;
        if (actorId != null) actorIds.add(actorId);
        this.nextSpawnAt = nextSpawnAt;
        sync();
    }

    public void scheduleStatus(long gameTime) {
        nextStatusAt = gameTime;
        setChanged();
    }

    public void completeChallenge() {
        clearChallengeState(true);
    }

    public void cancelChallenge(boolean removeActors) {
        if (level instanceof ServerLevel server && removeActors) {
            GangChallengeController.removeActors(server, actorIds, challengeId);
        }
        clearChallengeState(true);
    }

    /**
     * Tear down an active session while the block itself is being removed. This deliberately avoids
     * mutating the block state or broadcasting a block-entity update: doing either from Block#onRemove
     * can race the replacement state and make an active Boombox appear to survive its own break.
     */
    public void cancelChallengeForRemoval() {
        if (level instanceof ServerLevel server) {
            GangChallengeController.removeActors(server, actorIds, challengeId);
        }
        clearChallengeState(false);
    }

    private void clearChallengeState(boolean updateWorldState) {
        challengeId = null;
        ownerId = null;
        challengeEntityId = null;
        challengeGangId = null;
        startedAt = 0L;
        nextSpawnAt = 0L;
        expiresAt = 0L;
        nextStatusAt = 0L;
        plannedActors = 0;
        spawnAttempts = 0;
        actorIds.clear();
        if (updateWorldState) {
            setBlockActive(false);
            sync();
        } else {
            setChanged();
        }
    }

    private void setBlockActive(boolean active) {
        if (level == null) return;
        BlockState state = getBlockState();
        if (state.hasProperty(BoomboxBlock.ACTIVE) && state.getValue(BoomboxBlock.ACTIVE) != active) {
            level.setBlock(worldPosition, state.setValue(BoomboxBlock.ACTIVE, active), Block.UPDATE_ALL);
        }
    }

    private void sync() {
        setChanged();
        if (level == null) return;
        BlockState state = getBlockState();
        level.sendBlockUpdated(worldPosition, state, state, Block.UPDATE_ALL);
        level.updateNeighbourForOutputSignal(worldPosition, state.getBlock());
    }

    @Override
    protected void saveAdditional(CompoundTag tag) {
        super.saveAdditional(tag);
        tag.putInt("Schema", SCHEMA);
        if (!targetStack.isEmpty()) tag.put("Target", targetStack.save(new CompoundTag()));
        if (challengeId != null) tag.putUUID("Challenge", challengeId);
        if (ownerId != null) tag.putUUID("Owner", ownerId);
        if (challengeEntityId != null) tag.putString("Entity", challengeEntityId.toString());
        if (challengeGangId != null) tag.putString("Gang", challengeGangId.toString());
        if (startedAt > 0L) tag.putLong("StartedAt", startedAt);
        if (nextSpawnAt > 0L) tag.putLong("NextSpawnAt", nextSpawnAt);
        if (expiresAt > 0L) tag.putLong("ExpiresAt", expiresAt);
        if (nextStatusAt > 0L) tag.putLong("NextStatusAt", nextStatusAt);
        tag.putInt("PlannedActors", plannedActors);
        tag.putInt("SpawnAttempts", spawnAttempts);
        if (!actorIds.isEmpty()) {
            ListTag actors = new ListTag();
            for (UUID id : actorIds) {
                CompoundTag actor = new CompoundTag();
                actor.putUUID("Id", id);
                actors.add(actor);
            }
            tag.put("Actors", actors);
        }
    }

    @Override
    public void load(CompoundTag tag) {
        super.load(tag);
        targetStack = tag.contains("Target", Tag.TAG_COMPOUND) ? ItemStack.of(tag.getCompound("Target")) : ItemStack.EMPTY;
        challengeId = tag.hasUUID("Challenge") ? tag.getUUID("Challenge") : null;
        ownerId = tag.hasUUID("Owner") ? tag.getUUID("Owner") : null;
        challengeEntityId = ResourceLocation.tryParse(tag.getString("Entity"));
        challengeGangId = ResourceLocation.tryParse(tag.getString("Gang"));
        startedAt = tag.getLong("StartedAt");
        nextSpawnAt = tag.getLong("NextSpawnAt");
        expiresAt = tag.getLong("ExpiresAt");
        nextStatusAt = tag.getLong("NextStatusAt");
        plannedActors = Math.max(0, tag.getInt("PlannedActors"));
        spawnAttempts = Math.max(0, tag.getInt("SpawnAttempts"));
        actorIds.clear();
        if (tag.contains("Actors", Tag.TAG_LIST)) {
            ListTag actors = tag.getList("Actors", Tag.TAG_COMPOUND);
            for (Tag entry : actors) {
                if (entry instanceof CompoundTag actor && actor.hasUUID("Id")) actorIds.add(actor.getUUID("Id"));
            }
        }
        if (challengeId == null || challengeEntityId == null || challengeGangId == null) {
            challengeId = null;
            ownerId = null;
            challengeEntityId = null;
            challengeGangId = null;
            plannedActors = 0;
            spawnAttempts = 0;
            actorIds.clear();
        }
    }

    @Override
    public CompoundTag getUpdateTag() {
        CompoundTag tag = new CompoundTag();
        saveAdditional(tag);
        return tag;
    }

    @Nullable
    @Override
    public ClientboundBlockEntityDataPacket getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }

    public static void serverTick(Level level, BlockPos pos, BlockState state, BoomboxBlockEntity boombox) {
        if (level instanceof ServerLevel server) GangChallengeController.tick(server, pos, boombox);
    }
}
