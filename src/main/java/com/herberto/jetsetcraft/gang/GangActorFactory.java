package com.herberto.jetsetcraft.gang;

import com.herberto.jetsetcraft.JetSetCraft;
import com.herberto.jetsetcraft.mob.MobStreetGear;
import com.herberto.jetsetcraft.mob.StreetGearAcquisition;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraftforge.event.ForgeEventFactory;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.Optional;
import java.util.UUID;

/** Creates event-only casts from the original registered EntityType and equips them through the normal Street Gear API. */
public final class GangActorFactory {
    public static Optional<Mob> spawn(ServerLevel level, BlockPos anchor, HeadGangTargetResolver.Target target,
                                      UUID challengeId, int memberIndex, long expiresAt, double radius) {
        if (level == null || target == null || challengeId == null) return Optional.empty();
        EntityType<?> type = ForgeRegistries.ENTITY_TYPES.getValue(target.entityId());
        if (type == null) return Optional.empty();

        Entity raw = type.create(level);
        if (!(raw instanceof Mob mob)) return Optional.empty();
        BlockPos spawn = findSafeSpawn(level, mob, anchor, memberIndex, radius);
        if (spawn == null) {
            mob.discard();
            return Optional.empty();
        }

        mob.moveTo(spawn.getX() + 0.5D, spawn.getY(), spawn.getZ() + 0.5D,
                Mth.wrapDegrees(memberIndex * 57.0F), 0.0F);
        try {
            ForgeEventFactory.onFinalizeSpawn(mob, level, level.getCurrentDifficultyAt(spawn),
                    MobSpawnType.EVENT, null, null);
        } catch (RuntimeException error) {
            JetSetCraft.LOGGER.warn("Skipping Boombox actor {} because its spawn finalization failed",
                    target.entityId(), error);
            mob.discard();
            return Optional.empty();
        }
        mob.setPersistenceRequired();
        if (!level.addFreshEntity(mob)) {
            mob.discard();
            return Optional.empty();
        }

        MobStreetGear.EquipResult gear = MobStreetGear.equip(mob, GangGearSelector.forActor(mob, memberIndex),
                StreetGearAcquisition.GANG_EVENT, false);
        if (!gear.equipped()) {
            mob.discard();
            return Optional.empty();
        }
        GangMemberState.attach(mob, target.gangId(), roleFor(memberIndex), challengeId, true, expiresAt);
        return Optional.of(mob);
    }

    private static BlockPos findSafeSpawn(ServerLevel level, Mob mob, BlockPos anchor, int memberIndex, double radius) {
        int attempts = 12;
        double base = (memberIndex * 2.399963229728653D) + (level.getGameTime() % 31L) * 0.13D;
        double maxRadius = Math.max(4.0D, Math.min(24.0D, radius));
        for (int i = 0; i < attempts; i++) {
            double angle = base + (Math.PI * 2.0D * i / attempts);
            double distance = 4.5D + ((i + memberIndex) % 5) * Math.max(0.75D, (maxRadius - 4.5D) / 5.0D);
            int x = anchor.getX() + Mth.floor(Math.cos(angle) * distance);
            int z = anchor.getZ() + Mth.floor(Math.sin(angle) * distance);
            if (!level.getChunkSource().hasChunk(x >> 4, z >> 4)) continue;
            int y = level.getHeight(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, x, z);
            BlockPos feet = new BlockPos(x, y, z);
            if (Math.abs(y - anchor.getY()) > 10) continue;
            BlockPos below = feet.below();
            if (!level.getBlockState(below).isFaceSturdy(level, below, Direction.UP)) continue;
            mob.moveTo(x + 0.5D, y, z + 0.5D, 0.0F, 0.0F);
            if (!level.noCollision(mob) || level.containsAnyLiquid(mob.getBoundingBox())) continue;
            return feet;
        }
        return null;
    }

    private static String roleFor(int memberIndex) {
        return switch (Math.floorMod(memberIndex, 5)) {
            case 0 -> "leader";
            case 1 -> "racer";
            case 2 -> "trickster";
            case 3 -> "tagger";
            default -> "support";
        };
    }

    private GangActorFactory() {}
}
