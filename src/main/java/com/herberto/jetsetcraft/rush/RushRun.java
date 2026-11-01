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
        PRECISION("Critical rhythm", "Add 5 percentage points to paint critical chance per rank; capped at 35%.", 6),
        CRITICAL("Finishing touch", "Critical hits deal an additional 25% base damage per rank.", 6),
        AMPLITUDE("Bigger chorus", "Increase paint and artifact area by 12% per rank.", 6),
        RESONANCE("Fast rewind", "Reduce artifact intervals by 8% per rank, with a bounded minimum.", 6),
        CHAIN("Arc paint", "Paint hits chain to nearby rivals. One of three artifact slots.", 4),
        FIREWORK("Firework cap", "Every sixth paint hit bursts around its target. One of three artifact slots.", 4),
        WIND("Bass horn", "Periodically push nearby rivals away and stagger them. One of three artifact slots.", 4),
        SHOCKWAVE("Rail shockwave", "Moving tricks, grinds and wall rides send a damaging forward pulse. One of three artifact slots.", 4),
        TEMPO("Tempo drive", "Every eighth defeat gives a short firing-rate burst. One of three artifact slots.", 4),
        SOUL("Soul mixtape", "Defeats charge a small automatic recovery pulse. One of three artifact slots.", 4),
        SPELL_RELAY("Firebolt relay", "Launch a real Iron's Spells Firebolt at intervals. Optional provider; one artifact slot.", 4),
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
        public boolean artifact() {
            return switch (this) {
                case CHAIN, FIREWORK, WIND, SHOCKWAVE, TEMPO, SOUL, SPELL_RELAY -> true;
                default -> false;
            };
        }
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
    private boolean circuit;
    private boolean spellRelayAvailable;
    private long elapsedTicks;
    private long xp;
    private long score;
    private long defeats;
    private int level = 1;
    private int offerVersion;
    private int surgeTicks;
    private int magnetTicks;
    private int tempoTicks;
    private int soulCharge;
    private long hits;
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
    public double cadenceMultiplier() { return Math.max(0.25D, (1.0D - rank(Upgrade.CADENCE) * 0.07D) * (tempoTicks > 0 ? 0.7D : 1.0D)); }
    public double splashRadius() { return (1.5D + rank(Upgrade.SPLASH) * 0.3D) * areaMultiplier(); }
    public double pickupRadius() { return magnetTicks > 0 ? 128.0D : 3.0D + rank(Upgrade.PICKUP) * 1.5D; }
    public double recoveryPerSecond() { return rank(Upgrade.RECOVERY) * 0.35D; }
    public double flowMultiplier() { return 1.0D + rank(Upgrade.FLOW) * 0.1D; }
    public double criticalChance() { return 0.05D + rank(Upgrade.PRECISION) * 0.05D; }
    public double criticalMultiplier() { return 1.5D + rank(Upgrade.CRITICAL) * 0.25D; }
    public double areaMultiplier() { return 1.0D + rank(Upgrade.AMPLITUDE) * 0.12D; }
    public double artifactIntervalMultiplier() { return Math.max(0.5D, 1.0D - rank(Upgrade.RESONANCE) * 0.08D); }
    public int soulCharge() { return soulCharge; }
    public long hits() { return hits; }
    public boolean circuit() { return circuit; }
    public int artifactCount() { return (int) ranks.keySet().stream().filter(u -> u.artifact() && rank(u) > 0).count(); }
    public void configureCircuit() { if (elapsedTicks == 0L) circuit = true; }
    public void enableSpellRelay(boolean available) { spellRelayAvailable = available; }
    public boolean canAdvance(Upgrade upgrade) {
        return upgrade != null && !upgrade.repeatable() && rank(upgrade) < upgrade.maxRank()
                && (upgrade != Upgrade.SPELL_RELAY || spellRelayAvailable)
                && (!upgrade.artifact() || rank(upgrade) > 0 || artifactCount() < 3);
    }

    /** Server-earned arena/POI reward. Full builds receive useful score instead of an unusable rank. */
    public boolean grantReward(Upgrade upgrade) {
        if (finished || awaitingEncore || upgrade == null) return false;
        if (!canAdvance(upgrade)) { addScore(250); heal(5); return false; }
        ranks.put(upgrade, rank(upgrade) + 1);
        if (upgrade == Upgrade.VITALITY) heal(12);
        if (hasOffer()) refreshOffer();
        return true;
    }
    public void magnet(int ticks) { if (!finished) magnetTicks = Math.max(magnetTicks, Math.min(200, Math.max(0, ticks))); }
    public boolean spendSouls() { if (soulCharge < 5 || rank(Upgrade.SOUL) == 0) return false; soulCharge -= 5; return true; }
    public void pauseForEncore() { if (!finished) awaitingEncore = true; }

    /** Deterministic per-run critical sequence, called only after an owned valid hit is accepted. */
    public boolean recordHitAndRollCritical() {
        hits = saturatingAdd(hits, 1);
        long value = seed + hits * 0x9E3779B97F4A7C15L;
        value = (value ^ (value >>> 30)) * 0xBF58476D1CE4E5B9L;
        value = (value ^ (value >>> 27)) * 0x94D049BB133111EBL;
        value ^= value >>> 31;
        return (value >>> 11) * 0x1.0p-53 < criticalChance();
    }

    /** Only active simulation time advances; the manager decides when menu/transition pauses apply. */
    public void tickActive() {
        if (finished || awaitingEncore) return;
        elapsedTicks = saturatingAdd(elapsedTicks, 1L);
        if (surgeTicks > 0) surgeTicks--;
        if (magnetTicks > 0) magnetTicks--;
        if (tempoTicks > 0) tempoTicks--;
        if (!circuit && !endless && elapsedTicks >= SESSION_TICKS) awaitingEncore = true;
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
        if (!upgrade.repeatable() && !canAdvance(upgrade)) { refreshOffer(); return false; }
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
        if (!finished && !awaitingEncore) {
            defeats = saturatingAdd(defeats, 1L);
            if (rank(Upgrade.SOUL) > 0) soulCharge = Math.min(20, soulCharge + 1);
            if (rank(Upgrade.TEMPO) > 0 && defeats % 8 == 0) tempoTicks = 80 + rank(Upgrade.TEMPO) * 20;
        }
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
        refreshOffer();
    }

    /** A reward cache can change slot/rank eligibility while an XP draft is already open. */
    private void refreshOffer() {
        offerVersion = offerVersion == Integer.MAX_VALUE ? 1 : offerVersion + 1;
        List<Upgrade> available = new ArrayList<>();
        for (Upgrade upgrade : Upgrade.values()) {
            if (canAdvance(upgrade)) available.add(upgrade);
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
