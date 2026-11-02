package com.herberto.jetsetcraft.rush;

import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumMap;
import java.util.EnumSet;
import java.util.List;
import java.util.Random;
import java.util.Set;
import java.util.UUID;

/** Mode-only, deterministic run rules. No vanilla XP, inventory, world, or client authority lives here. */
public final class RushRun {
    public static final long SESSION_TICKS = 30L * 60L * 20L;
    private static final long COUNTER_LIMIT = Long.MAX_VALUE / 4L;
    private static final double METRIC_LIMIT = 1_000_000_000_000D;
    public static final int MAX_REROLLS = 3;
    public static final int MAX_BANISHES = 2;

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
        BATTERY("Line battery", "Ride 24 actual blocks to empower your next primary tag, by paint or contact. One artifact slot.", 4),
        AIR_BREAK("Air break", "A moving jump or drop followed by a real landing pulses nearby rivals. One artifact slot.", 4),
        EXPOSE("Breakbeat mark", "Briefly expose a visible priority rival to increased run damage. One artifact slot.", 4),
        VENOM("Venom ink", "Primary tags apply nonstacking run damage each second for 4–7 seconds. One artifact slot.", 4),
        CHILL("Chill paint", "Primary tags briefly slow rival run steering and directed attacks. One artifact slot.", 4),
        MULTISHOT("Split chorus", "Every fourth primary tag hits 1–3 extra visible rivals in its forward cone. One artifact slot.", 4),
        RADIANCE("Radiant groove", "Every sixth primary tag restores 4–10 run vitality. One artifact slot.", 4),
        WARD("Beat ward", "Absorb 12–24 run damage; recharge after six active seconds without damage. One artifact slot.", 4),
        SOUL_BURST("Soul burst", "Five owned defeats charge a burst against nearby visible rivals. One artifact slot.", 4),
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
                case CHAIN, FIREWORK, WIND, SHOCKWAVE, TEMPO, SOUL, SPELL_RELAY, BATTERY, AIR_BREAK, EXPOSE,
                        VENOM, CHILL, MULTISHOT, RADIANCE, WARD, SOUL_BURST -> true;
                default -> false;
            };
        }
    }

    private final UUID id;
    private final UUID owner;
    private final long seed;
    private final String gangId;
    private final EnumMap<Upgrade, Integer> ranks = new EnumMap<>(Upgrade.class);
    private final EnumSet<Upgrade> banishedDrafts = EnumSet.noneOf(Upgrade.class);
    private final EnumMap<RushBuildPaths.Evolution, Long> evolutionProcs = new EnumMap<>(RushBuildPaths.Evolution.class);
    private boolean endless;
    private boolean awaitingEncore;
    private boolean finished;
    private boolean startingPerkGranted;
    private boolean circuit;
    private boolean spellRelayAvailable;
    private boolean remixReady;
    private boolean buildOpportunity;
    private RushBuildPaths.Evolution selectedEvolution = RushBuildPaths.Evolution.NONE;
    private int rerolls = MAX_REROLLS;
    private int banishes = MAX_BANISHES;
    private long rerollsUsed;
    private long banishesUsed;
    private long arenaClears;
    private long headliners;
    private long nextCryoShatterTick;
    private double primaryDamage;
    private double secondaryDamage;
    private double damageTaken;
    private double vitalityRestored;
    private double wardAbsorbed;
    private double wardRestored;
    private double lineCharge;
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
    private int soulBurstCharge;
    private double ward;
    private long wardLastHit;
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
    public int soulBurstCharge() { return soulBurstCharge; }
    public double ward() { return ward; }
    public double maxWard() { return rank(Upgrade.WARD) == 0 ? 0 : 8 + rank(Upgrade.WARD) * 4; }
    public long hits() { return hits; }
    public boolean circuit() { return circuit; }
    public int artifactCount() { return (int) ranks.keySet().stream().filter(u -> u.artifact() && rank(u) > 0).count(); }
    public double lineCharge() { return lineCharge; }
    public double lineChargeMax() { return 24.0D; }
    public boolean remixReady() { return remixReady && !finished; }
    public int rerollsRemaining() { return rerolls; }
    public int banishesRemaining() { return banishes; }
    public Set<Upgrade> banishedDraftUpgrades() { return Set.copyOf(banishedDrafts); }
    public long rerollsUsed() { return rerollsUsed; }
    public long banishesUsed() { return banishesUsed; }
    public long arenaClears() { return arenaClears; }
    public long headliners() { return headliners; }
    public double primaryDamage() { return primaryDamage; }
    public double secondaryDamage() { return secondaryDamage; }
    public double damageTaken() { return damageTaken; }
    public double vitalityRestored() { return vitalityRestored; }
    public double wardAbsorbed() { return wardAbsorbed; }
    public double wardRestored() { return wardRestored; }
    public boolean hasBuildOpportunity() { return buildOpportunity && !finished; }
    public RushBuildPaths.Evolution selectedEvolution() { return selectedEvolution; }
    /** Activity is derived from the current three-slot build, including after a rank-preserving remix. */
    public boolean evolutionActive(RushBuildPaths.Evolution evolution) {
        return evolution != null && evolution != RushBuildPaths.Evolution.NONE
                && selectedEvolution == evolution && evolution.ready(this);
    }
    public long evolutionProcs(RushBuildPaths.Evolution evolution) { return evolutionProcs.getOrDefault(evolution, 0L); }
    public boolean artifactAvailable(Upgrade upgrade) {
        return upgrade != null && upgrade.artifact() && (upgrade != Upgrade.SPELL_RELAY || spellRelayAvailable);
    }
    public void configureCircuit() { if (elapsedTicks == 0L) circuit = true; }
    public void enableSpellRelay(boolean available) { spellRelayAvailable = available; }
    public boolean canAdvance(Upgrade upgrade) {
        return upgrade != null && !upgrade.repeatable() && rank(upgrade) < upgrade.maxRank()
                && (upgrade != Upgrade.SPELL_RELAY || spellRelayAvailable)
                && (!upgrade.artifact() || rank(upgrade) > 0 || artifactCount() < 3);
    }

    /** Draft-only exclusions never remove earned ranks or prevent physical cache/remix rewards. */
    public boolean canDraft(Upgrade upgrade) { return canAdvance(upgrade) && !banishedDrafts.contains(upgrade); }

    public boolean canBanish(int offerIndex) {
        return !finished && !awaitingEncore && banishes > 0 && offerIndex >= 0 && offerIndex < offers.size()
                && !offers.get(offerIndex).repeatable() && !banishedDrafts.contains(offers.get(offerIndex));
    }

    public boolean canReroll() {
        if (finished || awaitingEncore || rerolls <= 0 || offers.isEmpty()) return false;
        return !sameChoices(offers, drawOffers(nextVersion(), offers));
    }

    /** Keeps the earned draft/level. A no-change shuffle does not spend the player's finite control. */
    public boolean reroll(int version) {
        if (version != offerVersion || !canReroll()) return false;
        List<Upgrade> replacement = drawOffers(nextVersion(), offers);
        rerolls--; rerollsUsed = saturatingAdd(rerollsUsed, 1);
        offerVersion = nextVersion(); offers = replacement;
        return true;
    }

    /** Replaces only the rejected row; stale requests cannot spend another banish or alter the kept rows. */
    public boolean banish(int offerIndex, int version) {
        if (version != offerVersion || !canBanish(offerIndex)) return false;
        Upgrade rejected = offers.get(offerIndex);
        List<Upgrade> pool = draftPool(nextVersion());
        pool.removeIf(offers::contains);
        for (Upgrade terminal : terminalRewards()) if (!offers.contains(terminal)) pool.add(terminal);
        if (pool.isEmpty()) return false;
        List<Upgrade> replacement = new ArrayList<>(offers);
        replacement.set(offerIndex, pool.get(0));
        banishedDrafts.add(rejected); banishes--; banishesUsed = saturatingAdd(banishesUsed, 1);
        offerVersion = nextVersion(); offers = List.copyOf(replacement);
        return true;
    }

    /** The director must call exactly once after an owned headliner settlement; this is not a payout guard. */
    public void grantHeadlinerReward() {
        if (finished) return;
        headliners = saturatingAdd(headliners, 1);
        buildOpportunity = true; rerolls = Math.min(MAX_REROLLS, rerolls + 1);
        offerVersion = nextVersion();
    }

    /** The manager additionally validates the owner/run and a draft, between-room or Encore decision window. */
    public boolean evolve(RushBuildPaths.Evolution evolution, int version) {
        if (finished || !buildOpportunity || version != offerVersion || evolution == null
                || evolution == RushBuildPaths.Evolution.NONE || evolution == selectedEvolution || !evolution.ready(this)) return false;
        selectedEvolution = evolution; buildOpportunity = false; nextCryoShatterTick = elapsedTicks;
        offerVersion = nextVersion();
        return true;
    }

    /** Call only after selecting actual owned targets and validating the recipe's physical trigger. */
    public boolean tryEvolutionProc(RushBuildPaths.Evolution evolution) {
        if (finished || awaitingEncore || hasOffer() || !evolutionActive(evolution)) return false;
        if (evolution == RushBuildPaths.Evolution.CRYO_SHATTER) {
            if (elapsedTicks < nextCryoShatterTick) return false;
            nextCryoShatterTick = saturatingAdd(elapsedTicks, 60);
        }
        evolutionProcs.put(evolution, saturatingAdd(evolutionProcs(evolution), 1));
        return true;
    }

    /** Counts accepted damage capped by the actor's pre-hit virtual vitality, never attempted/overkill damage. */
    public void recordVirtualDamage(double actualApplied, boolean primary) {
        if (finished || !Double.isFinite(actualApplied) || actualApplied <= 0) return;
        if (primary) primaryDamage = addMetric(primaryDamage, actualApplied);
        else secondaryDamage = addMetric(secondaryDamage, actualApplied);
    }

    /** Called by the existing once-only room settlement, including clears before the final Encore prompt. */
    public void recordArenaClear() { if (!finished) arenaClears = saturatingAdd(arenaClears, 1); }

    public record Recovery(double vitalityRestored, double wardRestored) {}

    /** Sixth-primary Radiance only; ordinary healing cannot manufacture offensive or shield procs. */
    public Recovery applyRadiance() {
        if (finished || awaitingEncore || hasOffer() || rank(Upgrade.RADIANCE) == 0) return new Recovery(0, 0);
        double amount = 2 + rank(Upgrade.RADIANCE) * 2;
        double before = vitality; heal(amount); double restored = vitality - before;
        double shield = 0;
        if (evolutionActive(RushBuildPaths.Evolution.RADIANT_BARRIER)) {
            double overflow = Math.min(6, Math.max(0, amount - restored));
            if (overflow > 0 && ward < maxWard() && tryEvolutionProc(RushBuildPaths.Evolution.RADIANT_BARRIER))
                shield = restoreWard(overflow);
        }
        return new Recovery(restored, shield);
    }

    /** The manager supplies observed, continuous movement while actually riding; no input-button charge. */
    public boolean chargeLine(double distance) {
        if (finished || awaitingEncore || rank(Upgrade.BATTERY) == 0 || !Double.isFinite(distance)
                || distance <= 0 || distance > 4) return false;
        boolean before = lineCharge >= lineChargeMax();
        lineCharge = Math.min(lineChargeMax(), lineCharge + distance);
        return !before && lineCharge >= lineChargeMax();
    }

    /** Called only after validating an owned primary tag. Splash, relays and chained hits never spend it. */
    public double consumeLineMultiplier() {
        if (finished || awaitingEncore || hasOffer() || rank(Upgrade.BATTERY) == 0 || lineCharge < lineChargeMax()) return 1;
        lineCharge = 0;
        return 1.35D + rank(Upgrade.BATTERY) * .15D;
    }

    /** A completed circuit room supplies one optional replacement, never a stockpile of future rerolls. */
    public void grantRemix() {
        if (circuit && !finished) {
            remixReady = true;
            // Also invalidates a queued remix from an earlier room, even if no XP draft intervened.
            offerVersion = nextVersion();
        }
    }

    public boolean remix(Upgrade source, Upgrade replacement, int version) {
        if (!remixReady() || hasOffer() || version != offerVersion || source == null || !source.artifact()
                || rank(source) <= 0 || !artifactAvailable(replacement) || source == replacement || rank(replacement) > 0
                || rank(source) > replacement.maxRank()) return false;
        int preserved = rank(source);
        ranks.remove(source); ranks.put(replacement, preserved);
        if (source == Upgrade.BATTERY) lineCharge = 0;
        if (source == Upgrade.SOUL) soulCharge = 0;
        if (source == Upgrade.SOUL_BURST) soulBurstCharge = 0;
        if (source == Upgrade.WARD) ward = 0;
        if (source == Upgrade.TEMPO) tempoTicks = 0;
        if (replacement == Upgrade.WARD) { ward = maxWard(); wardLastHit = elapsedTicks; }
        remixReady = false;
        offerVersion = nextVersion();
        return true;
    }

    /** Server-earned arena/POI reward. Full builds receive useful score instead of an unusable rank. */
    public boolean grantReward(Upgrade upgrade) {
        if (finished || awaitingEncore || upgrade == null) return false;
        if (!canAdvance(upgrade)) { addScore(250); heal(5); return false; }
        double oldWardMax = maxWard();
        ranks.put(upgrade, rank(upgrade) + 1);
        if (upgrade == Upgrade.VITALITY) heal(12);
        if (upgrade == Upgrade.WARD) ward = Math.min(maxWard(), ward + maxWard() - oldWardMax);
        if (hasOffer()) refreshOffer();
        return true;
    }
    public void magnet(int ticks) { if (!finished) magnetTicks = Math.max(magnetTicks, Math.min(200, Math.max(0, ticks))); }
    public boolean spendSouls() { if (soulCharge < 5 || rank(Upgrade.SOUL) == 0) return false; soulCharge -= 5; return true; }
    public boolean spendSoulBurst() {
        if (finished || awaitingEncore || hasOffer() || soulBurstCharge < 5 || rank(Upgrade.SOUL_BURST) == 0) return false;
        soulBurstCharge -= 5; return true;
    }
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
        if (rank(Upgrade.WARD) > 0 && elapsedTicks - wardLastHit >= 120 && elapsedTicks % 20 == 0)
            restoreWard(2 + rank(Upgrade.WARD));
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
        if (!upgrade.repeatable() && !canDraft(upgrade)) { refreshOffer(); return false; }
        offers = List.of();
        if (upgrade.repeatable()) {
            switch (upgrade) {
                case REPAIR -> heal(maxVitality() * 0.5D);
                case CACHE -> addScore(Math.min(1_000_000L, 500L * Math.min(2000L, chapter())));
                case SURGE -> surgeTicks = 300;
                default -> throw new IllegalStateException("Unknown repeatable reward");
            }
        } else {
            double oldWardMax = maxWard();
            ranks.put(upgrade, Math.min(upgrade.maxRank(), rank(upgrade) + 1));
            if (upgrade == Upgrade.VITALITY) heal(12.0D);
            if (upgrade == Upgrade.WARD) ward = Math.min(maxWard(), ward + maxWard() - oldWardMax);
        }
        prepareOffer();
        return true;
    }

    /** A loaded, physical crew facility is validated by the manager; at most one starting rank is accepted. */
    public boolean grantStartingPerk(Upgrade upgrade) {
        if (upgrade == null || startingPerkGranted || elapsedTicks != 0L || finished
                || !canAdvance(upgrade) || rank(upgrade) != 0) return false;
        startingPerkGranted = true;
        ranks.put(upgrade, 1);
        if (upgrade == Upgrade.VITALITY) vitality = maxVitality();
        if (upgrade == Upgrade.WARD) ward = maxWard();
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
        remixReady = false;
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
            if (rank(Upgrade.SOUL_BURST) > 0) soulBurstCharge = Math.min(20, soulBurstCharge + 1);
            if (rank(Upgrade.TEMPO) > 0 && defeats % 8 == 0) tempoTicks = 80 + rank(Upgrade.TEMPO) * 20;
        }
    }

    /** Returns true exactly when this hit exhausts run vitality. It never changes a Player's real health. */
    public boolean hurt(double amount) {
        if (finished || awaitingEncore || !Double.isFinite(amount) || amount <= 0.0D) return false;
        wardLastHit = elapsedTicks;
        double absorbed = Math.min(ward, amount);
        ward -= absorbed; amount -= absorbed;
        wardAbsorbed = addMetric(wardAbsorbed, absorbed);
        double previousVitality = vitality;
        vitality = Math.max(0.0D, vitality - amount);
        damageTaken = addMetric(damageTaken, previousVitality - vitality);
        if (vitality <= 0.0D) {
            finish();
            return true;
        }
        return false;
    }

    public void heal(double amount) {
        if (!finished && Double.isFinite(amount) && amount > 0.0D) {
            double before = vitality; vitality = Math.min(maxVitality(), vitality + amount);
            vitalityRestored = addMetric(vitalityRestored, vitality - before);
        }
    }

    private void prepareOffer() {
        if (finished || awaitingEncore || !offers.isEmpty() || xp < xpToNextLevel()) return;
        xp -= xpToNextLevel();
        if (level < Integer.MAX_VALUE) level++;
        refreshOffer();
    }

    /** A reward cache can change slot/rank eligibility while an XP draft is already open. */
    private void refreshOffer() {
        offerVersion = nextVersion();
        offers = drawOffers(offerVersion, List.of());
    }

    private int nextVersion() { return offerVersion == Integer.MAX_VALUE ? 1 : offerVersion + 1; }

    private List<Upgrade> draftPool(int version) {
        List<Upgrade> available = new ArrayList<>();
        for (Upgrade upgrade : Upgrade.values()) {
            if (canDraft(upgrade)) available.add(upgrade);
        }
        Collections.shuffle(available, new Random(seed ^ ((long) version << 32) ^ level));
        // An equipped artifact gets an actionable rank-up when one remains eligible. During a reroll,
        // excluded choices yield to genuinely new choices instead of forcing the old family back in.
        return available;
    }

    private List<Upgrade> drawOffers(int version, List<Upgrade> excluded) {
        List<Upgrade> pool = draftPool(version);
        List<Upgrade> fresh = new ArrayList<>();
        for (Upgrade upgrade : pool) if (!excluded.contains(upgrade)) fresh.add(upgrade);
        List<Upgrade> result = new ArrayList<>(3);
        for (Upgrade upgrade : fresh) if (upgrade.artifact() && rank(upgrade) > 0) {
            result.add(upgrade); break;
        }
        for (Upgrade upgrade : fresh) if (result.size() < 3 && !result.contains(upgrade)) result.add(upgrade);
        for (Upgrade terminal : terminalRewards()) if (result.size() < 3 && !excluded.contains(terminal)) result.add(terminal);
        for (Upgrade upgrade : pool) if (result.size() < 3 && !result.contains(upgrade)) result.add(upgrade);
        for (Upgrade terminal : terminalRewards()) if (result.size() < 3 && !result.contains(terminal)) result.add(terminal);
        return List.copyOf(result);
    }

    private static List<Upgrade> terminalRewards() { return List.of(Upgrade.REPAIR, Upgrade.CACHE, Upgrade.SURGE); }

    private static boolean sameChoices(List<Upgrade> a, List<Upgrade> b) { return a.size() == b.size() && a.containsAll(b); }

    private double restoreWard(double amount) {
        double before = ward; ward = Math.min(maxWard(), ward + Math.max(0, amount));
        double restored = ward - before; wardRestored = addMetric(wardRestored, restored);
        return restored;
    }

    private static double addMetric(double value, double amount) {
        return !Double.isFinite(amount) || amount <= 0 ? value : Math.min(METRIC_LIMIT, value + Math.min(METRIC_LIMIT, amount));
    }

    private static long saturatingAdd(long value, long amount) {
        return Math.min(COUNTER_LIMIT, value + Math.min(amount, COUNTER_LIMIT - value));
    }
}
