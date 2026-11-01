package com.herberto.jetsetcraft.rush;

import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumMap;
import java.util.List;
import java.util.Random;
import java.util.UUID;

/** Mode-only, deterministic run rules. No vanilla XP, inventory, world, or client authority lives here. */
public final class RushRun {
    public static final long SESSION_TICKS = 30L * 60L * 20L;
    private static final long COUNTER_LIMIT = Long.MAX_VALUE / 4L;

    public enum Upgrade {
        FORCE("Paint pressure", "Increase tag damage by 15% per rank.", 8),
        CADENCE("Quick hands", "Fire more frequently; the firing interval stays bounded.", 8),
        SPLASH("Wide spray", "Increase your splash radius by 0.3 blocks per rank.", 6),
        PICKUP("Magnetic mixtape", "Collect run pickups from 1.5 blocks farther away per rank.", 6),
        VITALITY("Second wind", "Add 12 maximum run vitality and recover that amount.", 8),
        RECOVERY("Easy breathing", "Recover more run vitality between threats.", 6),
        FLOW("Style dividends", "Increase mode-only style rewards by 10% per rank.", 6),
        REPAIR("Fresh wheels", "Recover half your maximum run vitality now.", 0),
        CACHE("Victory lap", "Bank a chapter-scaled score bonus; no vanilla reward.", 0),
        SURGE("Overdrive", "Refresh a 15-second, 25% tag-damage surge.", 0);

        private final String label;
        private final String description;
        private final int maxRank;

        Upgrade(String label, String description, int maxRank) {
            this.label = label;
            this.description = description;
            this.maxRank = maxRank;
        }

        public String label() { return label; }
        public String description() { return description; }
        public int maxRank() { return maxRank; }
        public boolean repeatable() { return maxRank == 0; }
    }

    private final UUID id;
    private final UUID owner;
    private final long seed;
    private final String gangId;
    private final EnumMap<Upgrade, Integer> ranks = new EnumMap<>(Upgrade.class);
    private boolean endless;
    private boolean awaitingEncore;
    private boolean finished;
    private boolean startingPerkGranted;
    private long elapsedTicks;
    private long xp;
    private long score;
    private long defeats;
    private int level = 1;
    private int offerVersion;
    private int surgeTicks;
    private double vitality = 100.0D;
    private List<Upgrade> offers = List.of();

    public RushRun(UUID id, UUID owner, long seed, boolean endless, String gangId) {
        if (id == null || owner == null) throw new IllegalArgumentException("A run and its owner need stable IDs");
        this.id = id;
        this.owner = owner;
        this.seed = seed;
        this.endless = endless;
        this.gangId = gangId == null ? "" : gangId;
    }

    public UUID id() { return id; }
    public UUID owner() { return owner; }
    public long seed() { return seed; }
    public String gangId() { return gangId; }
    public boolean endless() { return endless; }
    public long elapsedTicks() { return elapsedTicks; }
    public int level() { return level; }
    public long xp() { return xp; }
    public int xpToNextLevel() { return (int) Math.min(5000L, 24L + (long) (level - 1) * 12L); }
    public long score() { return score; }
    public long defeats() { return defeats; }
    public double vitality() { return vitality; }
    public double maxVitality() { return 100.0D + rank(Upgrade.VITALITY) * 12.0D; }
    public boolean awaitingEncore() { return awaitingEncore; }
    public boolean isFinished() { return finished; }
    public boolean hasOffer() { return !offers.isEmpty(); }
    public int offerVersion() { return offerVersion; }
    public List<Upgrade> offers() { return offers; }
    public int rank(Upgrade upgrade) { return ranks.getOrDefault(upgrade, 0); }
    public long chapter() { return elapsedTicks / 6000L + 1L; }
    public double damageMultiplier() { return (1.0D + rank(Upgrade.FORCE) * 0.15D) * (surgeTicks > 0 ? 1.25D : 1.0D); }
    public double cadenceMultiplier() { return Math.max(0.35D, 1.0D - rank(Upgrade.CADENCE) * 0.07D); }
    public double splashRadius() { return 1.5D + rank(Upgrade.SPLASH) * 0.3D; }
    public double pickupRadius() { return 3.0D + rank(Upgrade.PICKUP) * 1.5D; }
    public double recoveryPerSecond() { return rank(Upgrade.RECOVERY) * 0.35D; }
    public double flowMultiplier() { return 1.0D + rank(Upgrade.FLOW) * 0.1D; }

    /** Only active simulation time advances; the manager decides when menu/transition pauses apply. */
    public void tickActive() {
        if (finished || awaitingEncore) return;
        elapsedTicks = saturatingAdd(elapsedTicks, 1L);
        if (surgeTicks > 0) surgeTicks--;
        if (!endless && elapsedTicks >= SESSION_TICKS) awaitingEncore = true;
    }

    public void gainXp(int amount) {
        if (amount <= 0 || finished || awaitingEncore) return;
        xp = saturatingAdd(xp, amount);
        prepareOffer();
    }

    /** One atomic offer claim. Callers must additionally match the current run UUID and player ownership. */
    public boolean choose(int offerIndex, int version) {
        if (finished || version != offerVersion || offerIndex < 0 || offerIndex >= offers.size()) return false;
        Upgrade upgrade = offers.get(offerIndex);
        offers = List.of();
        if (upgrade.repeatable()) {
            switch (upgrade) {
                case REPAIR -> heal(maxVitality() * 0.5D);
                case CACHE -> addScore(Math.min(1_000_000L, 500L * Math.min(2000L, chapter())));
                case SURGE -> surgeTicks = 300;
                default -> throw new IllegalStateException("Unknown repeatable reward");
            }
        } else {
            ranks.put(upgrade, Math.min(upgrade.maxRank(), rank(upgrade) + 1));
            if (upgrade == Upgrade.VITALITY) heal(12.0D);
        }
        prepareOffer();
        return true;
    }

    /** A loaded, physical crew facility is validated by the manager; at most one starting rank is accepted. */
    public boolean grantStartingPerk(Upgrade upgrade) {
        if (upgrade == null || upgrade.repeatable() || startingPerkGranted || elapsedTicks != 0L || finished) return false;
        startingPerkGranted = true;
        ranks.put(upgrade, 1);
        if (upgrade == Upgrade.VITALITY) vitality = maxVitality();
        return true;
    }

    public boolean continueEndless() {
        if (finished || !awaitingEncore) return false;
        endless = true;
        awaitingEncore = false;
        prepareOffer();
        return true;
    }

    public void finish() {
        finished = true;
        awaitingEncore = false;
        offers = List.of();
    }

    public void addScore(long amount) {
        if (amount > 0 && !finished) score = saturatingAdd(score, amount);
    }

    public void recordDefeat() {
        if (!finished && !awaitingEncore) defeats = saturatingAdd(defeats, 1L);
    }

    /** Returns true exactly when this hit exhausts run vitality. It never changes a Player's real health. */
    public boolean hurt(double amount) {
        if (finished || awaitingEncore || !Double.isFinite(amount) || amount <= 0.0D) return false;
        vitality = Math.max(0.0D, vitality - amount);
        if (vitality <= 0.0D) {
            finish();
            return true;
        }
        return false;
    }

    public void heal(double amount) {
        if (!finished && Double.isFinite(amount) && amount > 0.0D) vitality = Math.min(maxVitality(), vitality + amount);
    }

    private void prepareOffer() {
        if (finished || awaitingEncore || !offers.isEmpty() || xp < xpToNextLevel()) return;
        xp -= xpToNextLevel();
        if (level < Integer.MAX_VALUE) level++;
        offerVersion = offerVersion == Integer.MAX_VALUE ? 1 : offerVersion + 1;
        List<Upgrade> available = new ArrayList<>();
        for (Upgrade upgrade : Upgrade.values()) {
            if (!upgrade.repeatable() && rank(upgrade) < upgrade.maxRank()) available.add(upgrade);
        }
        Collections.shuffle(available, new Random(seed ^ ((long) offerVersion << 32) ^ level));
        for (Upgrade terminal : List.of(Upgrade.REPAIR, Upgrade.CACHE, Upgrade.SURGE)) {
            if (available.size() >= 3) break;
            available.add(terminal);
        }
        offers = List.copyOf(available.subList(0, Math.min(3, available.size())));
    }

    private static long saturatingAdd(long value, long amount) {
        return Math.min(COUNTER_LIMIT, value + Math.min(amount, COUNTER_LIMIT - value));
    }
}
