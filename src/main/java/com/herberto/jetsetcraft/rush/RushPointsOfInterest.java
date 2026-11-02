package com.herberto.jetsetcraft.rush;

import com.herberto.jetsetcraft.network.JetSetNetwork;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import org.joml.Vector3f;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/** Personal reward caches, paid only after owned defeats. No loot, vanilla XP or block entities. */
public final class RushPointsOfInterest {
    public record Claim(RushArenaCatalog.Reward reward, String result) { }
    private static final int COOLDOWN_TICKS = 12000;
    private static final int[][] SAFE_OFFSETS = {{0,0},{2,0},{-2,0},{0,2},{0,-2},{2,2},{2,-2},{-2,2},{-2,-2}};
    private record Cache(long key, BlockPos position, RushArenaCatalog.Reward reward) {}
    private final Map<Long, Cache> caches = new LinkedHashMap<>();
    // A fixed conservative cooldown ledger remembers visited regions without an unbounded Endless set.
    // A hash collision can delay another cache, but can never shorten a previously claimed cache's cooldown.
    private final long[] claimTimes = new long[2048];
    private long lastClaimDefeats;
    private long processedTick = -1;
    private boolean roomPlaced;
    private boolean baselinePending = true;
    private Claim latestClaim;

    public RushPointsOfInterest() { java.util.Arrays.fill(claimTimes, -COOLDOWN_TICKS - 1L); }
    public void reset() {
        caches.clear(); java.util.Arrays.fill(claimTimes, -COOLDOWN_TICKS - 1L);
        lastClaimDefeats = 0; roomPlaced = false; baselinePending = true; processedTick = -1;
        latestClaim = null;
    }

    /** Only the owning session drains an actually accepted cache claim; nearby hints are not rewards. */
    public Claim pollClaim() {
        Claim claim = latestClaim;
        latestClaim = null;
        return claim;
    }

    public String tick(ServerPlayer player, RushRun run, RushCircuit circuit) {
        if (!eligible(player, run, circuit)) return "";
        if (baselinePending) {
            // Charge in a new room comes from that room. This retains defeats since arrival even if
            // its first cache tick is delayed, without spending leftovers from the previous arena.
            lastClaimDefeats = circuit == null ? 0 : Math.max(0, run.defeats() - circuit.defeats(run));
            baselinePending = false;
        }
        if (run.elapsedTicks() % 10 != 0 || run.hasOffer() || processedTick == run.elapsedTicks()) return "";
        processedTick = run.elapsedTicks();
        caches.values().removeIf(cache -> player.distanceToSqr(Vec3.atCenterOf(cache.position)) > 160 * 160);
        if (circuit != null) {
            if (!roomPlaced) {
                int d = circuit.current().radius() - 8;
                BlockPos center = circuit.center();
                RushArenaCatalog.Reward[] rewards = RushArenaCatalog.Reward.values();
                int[][] locations = {{-d,-d},{d,-d},{-d,d},{d,d},{-d,6},{d,-6}};
                for (int i = 0; i < rewards.length; i++) {
                    BlockPos position = center.offset(locations[i][0], 1, locations[i][1]);
                    if (circuit.builder().columnReady(position)) add(player, position, rewards[i], circuit);
                }
                roomPlaced = caches.size() == rewards.length;
            }
        } else {
            int x = Math.floorDiv(player.blockPosition().getX(), 64);
            int z = Math.floorDiv(player.blockPosition().getZ(), 64);
            for (int dx = -1; dx <= 1; dx++) for (int dz = -1; dz <= 1; dz++) {
                BlockPos position = new BlockPos((x + dx) * 64 + 8, RushWorld.FLOOR_Y + 1, (z + dz) * 64 + 8);
                int index = Math.floorMod((x + dx) * 31 + z + dz, RushArenaCatalog.Reward.values().length);
                add(player, position, RushArenaCatalog.Reward.values()[index], null);
            }
        }
        List<Cache> nearby = nearest(player);
        for (Cache cache : nearby) if (remainingCooldown(run, cache) == 0
                && player.distanceToSqr(Vec3.atCenterOf(cache.position)) <= 64 * 64) glow(player, cache);
        Cache nearest = nearby.stream().filter(c -> remainingCooldown(run, c) == 0).findFirst().orElse(null);
        if (nearest == null || player.distanceToSqr(Vec3.atCenterOf(nearest.position)) > 64 * 64) return "";
        BlockPos pos = nearest.position;
        int needed = defeatsNeeded(run, circuit);
        if (player.distanceToSqr(Vec3.atCenterOf(pos)) <= 6.25 && needed == 0 && clearApproach(player, pos)) {
            claimTimes[bucket(nearest.key)] = run.elapsedTicks(); lastClaimDefeats = run.defeats();
            String result = grant(run, nearest.reward, 1);
            latestClaim = new Claim(nearest.reward, result);
            player.serverLevel().playSound(null, pos, SoundEvents.NOTE_BLOCK_CHIME.value(), SoundSource.PLAYERS, .7f, 1.2f);
            if (JetSetNetwork.canSync(player)) player.serverLevel().sendParticles(player, ParticleTypes.FIREWORK, false,
                    pos.getX() + .5, pos.getY() + 1, pos.getZ() + .5, 12, .6, .6, .6, .03);
            return "Cache collected: " + result + ". Three more rivals charge the next cache.";
        }
        return nearest.reward.label() + " cache · " + (int) Math.sqrt(player.distanceToSqr(Vec3.atCenterOf(pos)))
                + "m" + (needed > 0 ? " · defeat " + needed + " rivals to charge" : " · skate through to collect");
    }

    /** A bounded owner-only preview. Reading a map never places a cache or spends a charge. */
    public CompoundTag snapshot(ServerPlayer player, RushRun run, RushCircuit circuit) {
        CompoundTag state = new CompoundTag();
        ListTag entries = new ListTag(); state.put("Caches", entries);
        if (!eligible(player, run, circuit)) return state;
        int needed = defeatsNeeded(run, circuit);
        state.putInt("DefeatsNeeded", needed); state.putBoolean("Ready", needed == 0 && !run.hasOffer());
        for (Cache cache : nearest(player)) {
            CompoundTag tag = new CompoundTag();
            int cooldown = remainingCooldown(run, cache);
            tag.putLong("Key", cache.key); tag.putString("Reward", cache.reward.name().toLowerCase(Locale.ROOT));
            tag.putString("Label", cache.reward.label()); tag.putString("Benefit", benefit(run, cache.reward));
            tag.putInt("Color", cache.reward.color());
            tag.putInt("X", cache.position.getX()); tag.putInt("Y", cache.position.getY()); tag.putInt("Z", cache.position.getZ());
            tag.putInt("Distance", (int) Math.ceil(Math.sqrt(player.distanceToSqr(Vec3.atCenterOf(cache.position)))));
            tag.putInt("CooldownTicks", cooldown); tag.putBoolean("Ready", needed == 0 && cooldown == 0 && !run.hasOffer());
            entries.add(tag);
        }
        return state;
    }

    private static boolean eligible(ServerPlayer player, RushRun run, RushCircuit circuit) {
        return player != null && run != null && player.getUUID().equals(run.owner()) && RushWorld.isRush(player.level())
                && !run.isFinished() && !run.awaitingEncore()
                && (circuit == null || circuit.phase() == RushCircuit.Phase.ARENA);
    }
    private int defeatsNeeded(RushRun run, RushCircuit circuit) {
        long baseline = baselinePending ? circuit == null ? 0 : run.defeats() - circuit.defeats(run) : lastClaimDefeats;
        return (int) Math.max(0, Math.min(3, 3 - Math.max(0, run.defeats() - baseline)));
    }
    private int remainingCooldown(RushRun run, Cache cache) {
        return (int) Math.max(0, COOLDOWN_TICKS - (run.elapsedTicks() - claimTimes[bucket(cache.key)]));
    }
    private List<Cache> nearest(ServerPlayer player) {
        return caches.values().stream()
                .sorted(Comparator.comparingDouble(c -> player.distanceToSqr(Vec3.atCenterOf(c.position))))
                .limit(6).toList();
    }
    private static String benefit(RushRun run, RushArenaCatalog.Reward reward) {
        if (reward == RushArenaCatalog.Reward.RELIC) {
            boolean available = java.util.Arrays.stream(RushRun.Upgrade.values()).anyMatch(u -> u.artifact() && run.canAdvance(u));
            return available ? "One available artifact rank; at most three families" : "Mastered artifacts: +250 score and up to 5 vitality";
        }
        String primary = run.canAdvance(reward.upgrade()) ? reward.upgrade().label() + " +1 rank"
                : "Mastered rank: +250 score and up to 5 vitality";
        return switch (reward) {
            case CRITICAL -> primary + (run.canAdvance(RushRun.Upgrade.CRITICAL) ? "; Finishing touch +1 rank"
                    : "; mastered power: +250 score and up to 5 vitality");
            case MAGNET -> primary + "; 10-second owned-card magnet";
            case RECOVERY -> primary + "; recover up to 25 vitality";
            default -> primary;
        };
    }

    private static void glow(ServerPlayer player, Cache cache) {
        if (!JetSetNetwork.canSync(player)) return;
        int color = cache.reward.color();
        DustParticleOptions dust = new DustParticleOptions(new Vector3f(((color >> 16) & 255) / 255f,
                ((color >> 8) & 255) / 255f, (color & 255) / 255f), 1.2f);
        BlockPos pos = cache.position;
        player.serverLevel().sendParticles(player, dust, false, pos.getX() + .5, pos.getY() + .8,
                pos.getZ() + .5, 3, .25, .45, .25, .01);
    }
    private static boolean clearApproach(ServerPlayer player, BlockPos position) {
        return player.serverLevel().clip(new ClipContext(player.getEyePosition(), Vec3.atCenterOf(position),
                ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, player)).getType() == HitResult.Type.MISS;
    }

    private void add(ServerPlayer player, BlockPos position, RushArenaCatalog.Reward reward, RushCircuit circuit) {
        if (caches.size() >= 128 || caches.containsKey(position.asLong())) return;
        // Check complete built columns only, then choose a real safe standing surface. A solid authored
        // pillar is never replaced with a cache inside it, and optional provider geometry is respected.
        for (int[] offset : SAFE_OFFSETS) {
            BlockPos column = position.offset(offset[0], 0, offset[1]);
            if (player.serverLevel().getChunkSource().getChunkNow(column.getX() >> 4, column.getZ() >> 4) == null
                    || circuit != null && !circuit.builder().columnReady(column)) continue;
            for (int height = 0; height <= 3; height++) {
                BlockPos standing = column.above(height);
                var level = player.serverLevel();
                var support = level.getBlockState(standing.below());
                if (!RushArenaCatalog.safe(support) || !support.isFaceSturdy(level, standing.below(), Direction.UP)
                        || !level.getBlockState(standing).getFluidState().isEmpty()
                        || !level.getBlockState(standing.above()).getFluidState().isEmpty()
                        || !level.noCollision(player, player.getBoundingBox().move(Vec3.atBottomCenterOf(standing).subtract(player.position())))
                        || caches.values().stream().anyMatch(c -> c.position.equals(standing))) continue;
                caches.put(position.asLong(), new Cache(position.asLong(), standing, reward));
                // A flat colored pattern remains passable. It has no inventory, loot or ticking entity.
                level.setBlock(standing.below(), reward.marker(), 2);
                return;
            }
        }
    }

    private static int bucket(long key) {
        long hash = key ^ (key >>> 33);
        hash *= 0xFF51AFD7ED558CCDL;
        hash ^= hash >>> 33;
        hash *= 0xC4CEB9FE1A85EC53L;
        return (int) ((hash ^ (hash >>> 33)) & 2047);
    }

    public static String grant(RushRun run, RushArenaCatalog.Reward reward, int ranks) {
        if (run == null || reward == null || run.isFinished() || run.awaitingEncore() || ranks < 1 || ranks > 2)
            return "Reward unavailable";
        var beforeRanks = new java.util.EnumMap<RushRun.Upgrade, Integer>(RushRun.Upgrade.class);
        for (RushRun.Upgrade upgrade : RushRun.Upgrade.values()) beforeRanks.put(upgrade, run.rank(upgrade));
        long beforeScore = run.score(); double beforeVitality = run.vitality();
        for (int i = 0; i < ranks; i++) {
            RushRun.Upgrade upgrade = reward.upgrade();
            if (reward == RushArenaCatalog.Reward.RELIC) {
                List<RushRun.Upgrade> available = new ArrayList<>();
                for (RushRun.Upgrade candidate : RushRun.Upgrade.values())
                    if (candidate.artifact() && run.canAdvance(candidate)) available.add(candidate);
                if (!available.isEmpty())
                    upgrade = available.get(Math.floorMod(Long.hashCode(run.seed() ^ run.defeats() ^ run.level()), available.size()));
            }
            if (upgrade == null) { run.addScore(250); run.heal(5); }
            else run.grantReward(upgrade);
            if (reward == RushArenaCatalog.Reward.CRITICAL) run.grantReward(RushRun.Upgrade.CRITICAL);
            if (reward == RushArenaCatalog.Reward.MAGNET) run.magnet(200);
            if (reward == RushArenaCatalog.Reward.RECOVERY) run.heal(25);
        }
        List<String> accepted = new ArrayList<>();
        for (RushRun.Upgrade upgrade : RushRun.Upgrade.values()) {
            int gained = run.rank(upgrade) - beforeRanks.get(upgrade);
            if (gained > 0) accepted.add(upgrade.label() + " +" + gained + " (rank " + run.rank(upgrade) + ")");
        }
        if (run.score() > beforeScore) accepted.add("+" + (run.score() - beforeScore) + " score");
        if (run.vitality() > beforeVitality)
            accepted.add(String.format(Locale.ROOT, "+%.1f vitality", run.vitality() - beforeVitality));
        if (reward == RushArenaCatalog.Reward.MAGNET) accepted.add("10s owned-card magnet");
        return accepted.isEmpty() ? "Build and recovery already full" : String.join(" · ", accepted);
    }
}
