package com.herberto.jetsetcraft.rush;

import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.Vec3;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/** Personal reward caches, paid only after owned defeats. No loot, vanilla XP or block entities. */
public final class RushPointsOfInterest {
    private record Cache(long key, BlockPos position, RushArenaCatalog.Reward reward) {}
    private final Map<Long, Cache> caches = new LinkedHashMap<>();
    // A fixed conservative cooldown ledger remembers visited regions without an unbounded Endless set.
    // A hash collision can delay another cache, but can never shorten a previously claimed cache's cooldown.
    private final long[] claimTimes = new long[2048];
    private long lastClaimDefeats;
    private boolean roomPlaced;

    public RushPointsOfInterest() { java.util.Arrays.fill(claimTimes, -12001L); }
    public void reset() { caches.clear(); java.util.Arrays.fill(claimTimes, -12001L); roomPlaced = false; }

    public String tick(ServerPlayer player, RushRun run, RushCircuit circuit) {
        if (run.elapsedTicks() % 10 != 0 || run.hasOffer()) return "";
        caches.values().removeIf(cache -> player.distanceToSqr(Vec3.atCenterOf(cache.position)) > 160 * 160);
        if (circuit != null) {
            if (circuit.phase() != RushCircuit.Phase.ARENA) return "";
            if (!roomPlaced) {
                int d = circuit.current().radius() - 8;
                BlockPos center = circuit.center();
                RushArenaCatalog.Reward[] rewards = RushArenaCatalog.Reward.values();
                int[][] locations = {{-d,-d},{d,-d},{-d,d},{d,d},{-d,6},{d,-6}};
                for (int i = 0; i < rewards.length; i++) {
                    BlockPos position = center.offset(locations[i][0], 1, locations[i][1]);
                    if (circuit.builder().columnReady(position)) add(player, position, rewards[i]);
                }
                roomPlaced = caches.size() == 6;
            }
        } else {
            int x = Math.floorDiv(player.blockPosition().getX(), 64);
            int z = Math.floorDiv(player.blockPosition().getZ(), 64);
            for (int dx = -1; dx <= 1; dx++) for (int dz = -1; dz <= 1; dz++) {
                BlockPos position = new BlockPos((x + dx) * 64 + 8, RushWorld.FLOOR_Y + 1, (z + dz) * 64 + 8);
                int index = Math.floorMod((x + dx) * 31 + z + dz, RushArenaCatalog.Reward.values().length);
                add(player, position, RushArenaCatalog.Reward.values()[index]);
            }
        }
        Cache nearest = caches.values().stream().filter(c -> run.elapsedTicks() - claimTimes[bucket(c.key)] >= 12000)
                .min(Comparator.comparingDouble(c -> player.distanceToSqr(Vec3.atCenterOf(c.position)))).orElse(null);
        if (nearest == null || player.distanceToSqr(Vec3.atCenterOf(nearest.position)) > 48 * 48) return "";
        BlockPos pos = nearest.position;
        player.serverLevel().sendParticles(ParticleTypes.ENCHANT, pos.getX() + .5, pos.getY() + .8, pos.getZ() + .5,
                6, .25, .5, .25, .02);
        long needed = Math.max(0, 3 - (run.defeats() - lastClaimDefeats));
        if (player.distanceToSqr(Vec3.atCenterOf(pos)) <= 6.25 && needed == 0) {
            claimTimes[bucket(nearest.key)] = run.elapsedTicks(); lastClaimDefeats = run.defeats();
            String result = grant(run, nearest.reward, 1);
            player.serverLevel().playSound(null, pos, SoundEvents.NOTE_BLOCK_CHIME.value(), SoundSource.PLAYERS, .7f, 1.2f);
            player.serverLevel().sendParticles(ParticleTypes.FIREWORK, pos.getX() + .5, pos.getY() + 1, pos.getZ() + .5, 18, .6, .6, .6, .03);
            return "Cache collected: " + result + ". Three more rivals charge the next cache.";
        }
        return nearest.reward.label() + " cache · " + (int) Math.sqrt(player.distanceToSqr(Vec3.atCenterOf(pos)))
                + "m" + (needed > 0 ? " · defeat " + needed + " rivals to charge" : " · skate through to collect");
    }

    private void add(ServerPlayer player, BlockPos position, RushArenaCatalog.Reward reward) {
        if (caches.size() >= 128 || caches.containsKey(position.asLong())
                || player.serverLevel().getChunkSource().getChunkNow(position.getX() >> 4, position.getZ() >> 4) == null) return;
        BlockPos standing = position;
        for (int height = 0; height < 8 && !player.serverLevel().getBlockState(standing).isAir(); height++) standing = standing.above();
        caches.put(position.asLong(), new Cache(position.asLong(), standing, reward));
        // A visible in-floor marker remains passable and carries no transferable loot.
        player.serverLevel().setBlock(standing.below(), Blocks.SEA_LANTERN.defaultBlockState(), 2);
    }

    private static int bucket(long key) {
        long hash = key ^ (key >>> 33);
        hash *= 0xFF51AFD7ED558CCDL;
        hash ^= hash >>> 33;
        hash *= 0xC4CEB9FE1A85EC53L;
        return (int) ((hash ^ (hash >>> 33)) & 2047);
    }

    public static String grant(RushRun run, RushArenaCatalog.Reward reward, int ranks) {
        String result = reward.label();
        for (int i = 0; i < Math.min(2, Math.max(1, ranks)); i++) {
            RushRun.Upgrade upgrade = reward.upgrade();
            if (reward == RushArenaCatalog.Reward.RELIC) {
                List<RushRun.Upgrade> available = new ArrayList<>();
                for (RushRun.Upgrade candidate : RushRun.Upgrade.values()) if (candidate.artifact() && run.canAdvance(candidate)) available.add(candidate);
                if (!available.isEmpty()) {
                    upgrade = available.get(Math.floorMod(Long.hashCode(run.seed() ^ run.defeats() ^ run.level()), available.size()));
                    result = upgrade.label();
                }
            }
            if (upgrade == null) { run.addScore(250); run.heal(5); result = "Full artifact build: +250 score and vitality"; }
            else if (!run.grantReward(upgrade)) result = "Mastered reward: +250 score and vitality";
            if (reward == RushArenaCatalog.Reward.CRITICAL) run.grantReward(RushRun.Upgrade.CRITICAL);
            if (reward == RushArenaCatalog.Reward.MAGNET) run.magnet(200);
            if (reward == RushArenaCatalog.Reward.RECOVERY) run.heal(25);
        }
        return result;
    }
}
