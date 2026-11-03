package com.herberto.jetsetcraft.liberty;

import com.herberto.jetsetcraft.data.SavedDataCheckpoint;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.saveddata.SavedData;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.HexFormat;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.regex.Pattern;

/** Persistent campaign decisions only. This class never creates entities or changes any world blocks. */
public final class LibertyCampaignData extends SavedData {
    public static final String NAME = "jetsetcraft_liberty_campaign";
    public static final int SCHEMA = 1, MAX_RECEIPTS = 65_536, MAX_HOME_OFFERS = 4_096;
    public static final int LIBERATION_MAX = 10_000, FRONTIER_THRESHOLD = 2_000, THREAT_TRIGGER = 60;
    public static final long DEFENSE_WINDOW = 40 * 1_200L;
    private static final long CLOCK_LIMIT = Long.MAX_VALUE - DEFENSE_WINDOW;
    private static final Pattern HASH = Pattern.compile("[0-9a-f]{64}");

    public record Sector(String id, String name, String theaterId, String factionId, String parentId, int requiredLiberation) { }
    private static final List<Sector> SECTORS = List.of(
            new Sector("neon_docks", "Neon Docks", "neon_docks", "terminid", "", 0),
            new Sector("overpass_uprising", "Overpass Uprising", "overpass_uprising", "automaton", "", 0),
            new Sector("blackout_borough", "Blackout Borough", "blackout_borough", "illuminate", "", 0),
            new Sector("neon_frontier", "Neon Frontier", "neon_docks", "terminid", "neon_docks", FRONTIER_THRESHOLD),
            new Sector("iron_horizon", "Iron Horizon", "overpass_uprising", "automaton", "overpass_uprising", FRONTIER_THRESHOLD),
            new Sector("prism_frontier", "Prism Frontier", "blackout_borough", "illuminate", "blackout_borough", FRONTIER_THRESHOLD));

    /** The manager constructs this from its real operation, never from client-supplied reward fields. */
    public record Result(UUID runId, UUID owner, String sectorId, String factionId, int difficulty,
                         int completedObjectives, int totalObjectives, int optionalObjectives,
                         int destroyedBases, boolean extracted, UUID defenseOffer) {
        public boolean valid() {
            Sector sector = sector(sectorId);
            return runId != null && owner != null && sector != null && sector.factionId.equals(factionId)
                    && difficulty >= 1 && difficulty <= 10 && totalObjectives >= 1 && totalObjectives <= 16
                    && completedObjectives >= 0 && completedObjectives <= totalObjectives
                    && optionalObjectives >= 0 && optionalObjectives <= 24 && destroyedBases >= 0 && destroyedBases <= 32
                    && (!extracted || completedObjectives == totalObjectives);
        }
        public boolean victory() { return valid() && extracted && completedObjectives == totalObjectives; }
        public CompoundTag save() {
            CompoundTag out = new CompoundTag(); out.putUUID("Run", runId); out.putUUID("Owner", owner);
            out.putString("Sector", sectorId); out.putString("Faction", factionId); out.putInt("Difficulty", difficulty);
            out.putInt("Completed", completedObjectives); out.putInt("Total", totalObjectives);
            out.putInt("Optional", optionalObjectives); out.putInt("Bases", destroyedBases); out.putBoolean("Extracted", extracted);
            if (defenseOffer != null) out.putUUID("Defense", defenseOffer); return out;
        }
        public static Optional<Result> load(CompoundTag raw) {
            if (raw == null || !raw.hasUUID("Run") || !raw.hasUUID("Owner") || !raw.contains("Sector", Tag.TAG_STRING)
                    || !raw.contains("Faction", Tag.TAG_STRING) || !bool(raw, "Extracted")
                    || !ints(raw, "Difficulty", "Completed", "Total", "Optional", "Bases")
                    || !knownKeys(raw, Set.of("Run", "Owner", "Sector", "Faction", "Difficulty", "Completed", "Total", "Optional", "Bases", "Extracted", "Defense"))
                    || raw.contains("Defense") && !raw.hasUUID("Defense")) return Optional.empty();
            Result result = new Result(raw.getUUID("Run"), raw.getUUID("Owner"), raw.getString("Sector"), raw.getString("Faction"),
                    raw.getInt("Difficulty"), raw.getInt("Completed"), raw.getInt("Total"), raw.getInt("Optional"),
                    raw.getInt("Bases"), raw.getBoolean("Extracted"), raw.hasUUID("Defense") ? raw.getUUID("Defense") : null);
            return result.valid() ? Optional.of(result) : Optional.empty();
        }
        private String fingerprint() {
            return digest(runId + "|" + owner + "|" + sectorId + "|" + factionId + "|" + difficulty + "|"
                    + completedObjectives + "|" + totalObjectives + "|" + optionalObjectives + "|" + destroyedBases
                    + "|" + extracted + "|" + defenseOffer);
        }
    }

    /** Home-world offers are untimed and require a new explicit deployment. A claim binds one real run. */
    public record DefenseOffer(UUID id, UUID owner, String sectorId, String factionId, boolean homeWorld,
                               long deadline, UUID claimedRun) {
        private CompoundTag save() {
            CompoundTag out = new CompoundTag(); out.putUUID("Id", id); out.putUUID("Owner", owner);
            out.putString("Sector", sectorId); out.putString("Faction", factionId); out.putBoolean("HomeWorld", homeWorld);
            out.putLong("Deadline", deadline); if (claimedRun != null) out.putUUID("ClaimedRun", claimedRun); return out;
        }
        private DefenseOffer claimed(UUID player, UUID run) { return new DefenseOffer(id, player, sectorId, factionId, homeWorld, deadline, run); }
    }
    private static final class Region {
        int liberation, threat;
        Region() { }
        Region(int liberation, int threat) { this.liberation = liberation; this.threat = threat; }
    }

    private final Map<String, Region> regions = new LinkedHashMap<>();
    private final Map<String, DefenseOffer> regionalDefenses = new LinkedHashMap<>();
    private final Map<UUID, DefenseOffer> homeDefenses = new LinkedHashMap<>();
    private final Map<UUID, String> receipts = new LinkedHashMap<>();
    private long revision, activeTicks, lastServerTick = Long.MIN_VALUE;
    private CompoundTag retained;

    public LibertyCampaignData() { SECTORS.forEach(sector -> regions.put(sector.id, new Region())); }
    public static LibertyCampaignData get(MinecraftServer server) {
        return server.overworld().getDataStorage().computeIfAbsent(LibertyCampaignData::load, LibertyCampaignData::new, NAME);
    }
    public static boolean saveAcknowledged(MinecraftServer server) {
        return server != null && SavedDataCheckpoint.saveAndAcknowledge(server, NAME, get(server));
    }
    public static List<Sector> sectors() { return SECTORS; }
    public static Sector sector(String id) { return SECTORS.stream().filter(row -> row.id.equals(id)).findFirst().orElse(null); }
    public long revision() { return retained == null ? revision : -1; }
    public long activeTicks() { return activeTicks; }
    public boolean blocked() { return retained != null; }
    public int receiptCount() { return receipts.size(); }
    public int liberation(String id) { Region row = regions.get(id); return retained == null && row != null ? row.liberation : 0; }
    public int threat(String id) { Region row = regions.get(id); return retained == null && row != null ? row.threat : 0; }
    public boolean frontierUnlocked() { return SECTORS.stream().anyMatch(row -> !row.parentId.isEmpty() && unlocked(row)); }
    public boolean bonusContracts(String id) { return liberation(id) >= 5_000; }
    public boolean canDeploy(String id, int difficulty) { return deployReason(id, difficulty).isEmpty(); }
    public String deployReason(String id, int difficulty) {
        if (retained != null) return "Campaign data needs recovery or a newer compatible version; the saved record is retained.";
        if (receipts.size() >= MAX_RECEIPTS || revision == Long.MAX_VALUE) return "Campaign receipt capacity is full; existing receipts are retained for recovery.";
        Sector row = sector(id);
        if (row == null) return "Unknown campaign sector.";
        if (difficulty < 1 || difficulty > 10) return "Choose a difficulty from 1 to 10.";
        if (!unlocked(row)) return "Liberate " + sector(row.parentId).name + " to 20% to open this route.";
        return "";
    }
    private boolean unlocked(Sector row) {
        return retained == null && (row.parentId.isEmpty() || regions.get(row.parentId).liberation >= row.requiredLiberation);
    }

    /** Exactly one active tick per distinct server tick, and only while someone explicitly chooses Liberty. */
    public void tick(long serverGameTick, boolean libertyChosen) {
        if (retained != null || serverGameTick < 0 || lastServerTick == serverGameTick) return;
        lastServerTick = serverGameTick;
        if (!libertyChosen || activeTicks >= CLOCK_LIMIT || revision == Long.MAX_VALUE) return;
        activeTicks++; setDirty();
        for (DefenseOffer offer : List.copyOf(regionalDefenses.values())) {
            if (!mutable()) break;
            if (offer.claimedRun == null && activeTicks >= offer.deadline && canOfferHome(offer.owner)) {
                regionalDefenses.remove(offer.sectorId); offerHome(offer.owner, offer.sectorId);
                regions.get(offer.sectorId).threat = Math.max(75, regions.get(offer.sectorId).threat);
                changed();
            }
        }
    }

    public List<DefenseOffer> offers(UUID player) {
        if (retained != null || player == null) return List.of();
        List<DefenseOffer> out = new ArrayList<>();
        for (DefenseOffer offer : regionalDefenses.values())
            if (offer.claimedRun == null || player.equals(offer.owner)) out.add(offer);
        if (homeDefenses.containsKey(player)) out.add(homeDefenses.get(player)); return List.copyOf(out);
    }
    public Optional<DefenseOffer> offer(UUID player, UUID id) {
        return id == null ? Optional.empty() : offers(player).stream().filter(offer -> id.equals(offer.id)).findFirst();
    }
    public boolean claimDefense(UUID player, UUID id, UUID run, long expectedRevision) {
        if (player == null || run == null || retained != null) return false;
        DefenseOffer offer = offer(player, id).orElse(null);
        if (offer == null) return false;
        if (run.equals(offer.claimedRun) && player.equals(offer.owner)) return true;
        if (revision != expectedRevision || !mutable() || receipts.containsKey(run) || offer.claimedRun != null
                || !offer.homeWorld && activeTicks >= offer.deadline || !canDeploy(offer.sectorId, 1)) return false;
        if (allOffers().stream().anyMatch(other -> other.claimedRun != null && (run.equals(other.claimedRun) || player.equals(other.owner)))) return false;
        replaceOffer(offer.claimed(player, run)); changed(); return true;
    }
    /** Roll back an entry that never deployed. It does not turn an already settled loss into a victory. */
    public boolean releaseDefense(UUID player, UUID run) {
        if (retained != null || player == null || run == null) return false;
        for (DefenseOffer offer : offers(player)) if (run.equals(offer.claimedRun) && player.equals(offer.owner)) {
            if (!mutable()) return false;
            replaceOffer(offer.claimed(player, null)); changed(); return true;
        }
        return true;
    }

    /** Identical journal retries acknowledge success without applying any liberation, threat or offer twice. */
    public boolean settle(Result result) {
        if (retained != null || result == null || !result.valid()) return false;
        String fingerprint = result.fingerprint(), recorded = receipts.get(result.runId);
        if (recorded != null) return recorded.equals(fingerprint);
        if (!mutable() || receipts.size() >= MAX_RECEIPTS || !unlocked(sector(result.sectorId))) return false;
        DefenseOffer defense = result.defenseOffer == null ? null : offer(result.owner, result.defenseOffer).orElse(null);
        if (defense == null && allOffers().stream().anyMatch(offer -> result.runId.equals(offer.claimedRun))) return false;
        if (result.defenseOffer != null && (defense == null || !result.runId.equals(defense.claimedRun)
                || !result.owner.equals(defense.owner) || !result.sectorId.equals(defense.sectorId)
                || !result.factionId.equals(defense.factionId))) return false;
        if (defense != null && !defense.homeWorld && !result.victory() && !canOfferHome(result.owner)) return false;
        Region region = regions.get(result.sectorId);
        if (result.victory()) {
            int gain = 400 + 100 * result.difficulty + 30 * result.optionalObjectives + 40 * result.destroyedBases;
            if (defense != null) gain += 200;
            region.liberation = Math.min(LIBERATION_MAX, region.liberation + gain);
            region.threat = Math.max(0, region.threat - 15 - result.difficulty * 2 - result.destroyedBases * 2);
        } else region.threat = Math.min(100, region.threat + 10 + result.difficulty * 2);
        if (defense != null) {
            if (defense.homeWorld) {
                if (result.victory()) homeDefenses.remove(result.owner);
                else replaceOffer(defense.claimed(result.owner, null));
            } else {
                regionalDefenses.remove(defense.sectorId);
                if (!result.victory()) offerHome(result.owner, defense.sectorId);
            }
        } else if (region.threat >= THREAT_TRIGGER && !regionalDefenses.containsKey(result.sectorId)) {
            Sector sector = sector(result.sectorId);
            regionalDefenses.put(sector.id, new DefenseOffer(UUID.randomUUID(), result.owner, sector.id, sector.factionId,
                    false, Math.min(CLOCK_LIMIT, activeTicks) + DEFENSE_WINDOW, null));
        }
        receipts.put(result.runId, fingerprint); changed(); return true;
    }
    public boolean settled(UUID run) { return retained == null && run != null && receipts.containsKey(run); }
    private boolean mutable() { return retained == null && revision < Long.MAX_VALUE; }
    private boolean canOfferHome(UUID owner) { return homeDefenses.containsKey(owner) || homeDefenses.size() < MAX_HOME_OFFERS; }
    private void offerHome(UUID owner, String id) {
        if (homeDefenses.containsKey(owner)) return;
        Sector sector = sector(id);
        homeDefenses.put(owner, new DefenseOffer(UUID.randomUUID(), owner, id, sector.factionId, true, 0, null));
    }
    private void replaceOffer(DefenseOffer offer) {
        if (offer.homeWorld) homeDefenses.put(offer.owner, offer); else regionalDefenses.put(offer.sectorId, offer);
    }
    private List<DefenseOffer> allOffers() {
        List<DefenseOffer> out = new ArrayList<>(regionalDefenses.values()); out.addAll(homeDefenses.values()); return out;
    }
    private void changed() { revision++; setDirty(); }

    public CompoundTag snapshot(ServerPlayer player) { return snapshot(player == null ? null : player.getUUID()); }
    public CompoundTag snapshot(UUID player) {
        CompoundTag out = new CompoundTag(); out.putInt("Schema", SCHEMA); out.putBoolean("Blocked", retained != null);
        out.putLong("Revision", revision()); out.putLong("ActiveTicks", activeTicks); out.putInt("Receipts", receipts.size());
        out.putString("Reason", retained == null ? receipts.size() >= MAX_RECEIPTS ? "Campaign receipt capacity is full." : ""
                : "Campaign data needs recovery or a newer compatible version; the saved record is retained.");
        out.putString("ClockRule", "Defense time passes only while players choose Liberty. Lost defenses offer optional, reversible home-world missions.");
        ListTag sectors = new ListTag();
        for (Sector sector : SECTORS) {
            CompoundTag row = new CompoundTag(); row.putString("Id", sector.id); row.putString("Name", sector.name);
            row.putString("Theater", sector.theaterId); row.putString("Faction", sector.factionId); row.putString("Parent", sector.parentId);
            row.putInt("Liberation", liberation(sector.id)); row.putInt("Threat", threat(sector.id));
            row.putBoolean("Unlocked", unlocked(sector)); row.putBoolean("CanDeploy", canDeploy(sector.id, 1));
            row.putString("Reason", deployReason(sector.id, 1)); row.putBoolean("BonusContracts", bonusContracts(sector.id));
            row.putInt("MinDifficulty", 1); row.putInt("MaxDifficulty", 10); sectors.add(row);
        }
        out.put("Sectors", sectors); ListTag offers = new ListTag();
        for (DefenseOffer offer : offers(player)) {
            CompoundTag row = offer.save(); row.putLong("TicksLeft", offer.homeWorld ? 0 : Math.max(0, offer.deadline - activeTicks));
            row.putBoolean("CanDeploy", offer.claimedRun == null && canDeploy(offer.sectorId, 1)
                    && (offer.homeWorld || activeTicks < offer.deadline));
            row.putString("Name", offer.homeWorld ? "Bring Liberty Home" : "Defend " + sector(offer.sectorId).name);
            row.putString("Description", offer.homeWorld
                    ? "An optional defense at a clear site in your normal world. Explicit deployment, temporary actors, full return restoration; no terrain destruction."
                    : "Hold the region against a counterattack. Claiming this operation pauses its campaign deadline."); offers.add(row);
        }
        out.put("Defenses", offers); return out;
    }

    public static LibertyCampaignData load(CompoundTag raw) {
        LibertyCampaignData data = new LibertyCampaignData();
        try {
            if (raw == null || !raw.contains("Schema", Tag.TAG_INT) || raw.getInt("Schema") != SCHEMA
                    || !raw.contains("Revision", Tag.TAG_LONG) || raw.getLong("Revision") < 0
                    || !raw.contains("ActiveTicks", Tag.TAG_LONG) || raw.getLong("ActiveTicks") < 0 || raw.getLong("ActiveTicks") > CLOCK_LIMIT
                    || !knownKeys(raw, Set.of("Schema", "Revision", "ActiveTicks", "Regions", "RegionalDefenses", "HomeDefenses", "Receipts")))
                throw new IllegalArgumentException("Campaign schema");
            ListTag regions = compounds(raw, "Regions", SECTORS.size());
            if (regions.size() != SECTORS.size()) throw new IllegalArgumentException("Campaign regions");
            data.regions.clear();
            for (Tag value : regions) {
                CompoundTag row = (CompoundTag) value; String id = row.getString("Id");
                if (!row.contains("Id", Tag.TAG_STRING) || sector(id) == null || !ints(row, "Liberation", "Threat")
                        || row.getInt("Liberation") < 0 || row.getInt("Liberation") > LIBERATION_MAX
                        || row.getInt("Threat") < 0 || row.getInt("Threat") > 100
                        || !knownKeys(row, Set.of("Id", "Liberation", "Threat"))
                        || data.regions.putIfAbsent(id, new Region(row.getInt("Liberation"), row.getInt("Threat"))) != null)
                    throw new IllegalArgumentException("Campaign region");
            }
            Set<UUID> offerIds = new java.util.HashSet<>(); Set<UUID> claimed = new java.util.HashSet<>();
            for (Tag value : compounds(raw, "RegionalDefenses", SECTORS.size())) {
                DefenseOffer offer = readOffer((CompoundTag) value, false);
                if (!offerIds.add(offer.id) || offer.claimedRun != null && !claimed.add(offer.claimedRun)
                        || data.regionalDefenses.putIfAbsent(offer.sectorId, offer) != null) throw new IllegalArgumentException("Duplicate regional defense");
            }
            for (Tag value : compounds(raw, "HomeDefenses", MAX_HOME_OFFERS)) {
                DefenseOffer offer = readOffer((CompoundTag) value, true);
                if (!offerIds.add(offer.id) || offer.claimedRun != null && !claimed.add(offer.claimedRun)
                        || data.homeDefenses.putIfAbsent(offer.owner, offer) != null) throw new IllegalArgumentException("Duplicate home defense");
            }
            for (Tag value : compounds(raw, "Receipts", MAX_RECEIPTS)) {
                CompoundTag row = (CompoundTag) value;
                if (!row.hasUUID("Run") || !row.contains("Hash", Tag.TAG_STRING) || !HASH.matcher(row.getString("Hash")).matches()
                        || !knownKeys(row, Set.of("Run", "Hash")) || claimed.contains(row.getUUID("Run"))
                        || data.receipts.putIfAbsent(row.getUUID("Run"), row.getString("Hash")) != null)
                    throw new IllegalArgumentException("Campaign receipt");
            }
            data.revision = raw.getLong("Revision"); data.activeTicks = raw.getLong("ActiveTicks"); return data;
        } catch (RuntimeException invalid) {
            LibertyCampaignData retained = new LibertyCampaignData(); retained.retained = raw == null ? new CompoundTag() : raw.copy(); return retained;
        }
    }
    @Override public CompoundTag save(CompoundTag out) {
        if (retained != null) return retained.copy();
        out.putInt("Schema", SCHEMA); out.putLong("Revision", revision); out.putLong("ActiveTicks", activeTicks);
        ListTag savedRegions = new ListTag();
        for (Sector sector : SECTORS) {
            Region region = regions.get(sector.id); CompoundTag row = new CompoundTag(); row.putString("Id", sector.id);
            row.putInt("Liberation", region.liberation); row.putInt("Threat", region.threat); savedRegions.add(row);
        }
        out.put("Regions", savedRegions); ListTag defenses = new ListTag(), homes = new ListTag(), paid = new ListTag();
        regionalDefenses.values().forEach(offer -> defenses.add(offer.save())); homeDefenses.values().forEach(offer -> homes.add(offer.save()));
        receipts.forEach((run, hash) -> { CompoundTag row = new CompoundTag(); row.putUUID("Run", run); row.putString("Hash", hash); paid.add(row); });
        out.put("RegionalDefenses", defenses); out.put("HomeDefenses", homes); out.put("Receipts", paid); return out;
    }
    private static DefenseOffer readOffer(CompoundTag raw, boolean home) {
        Sector sector = sector(raw.getString("Sector"));
        if (!raw.hasUUID("Id") || !raw.hasUUID("Owner") || sector == null || !raw.contains("Faction", Tag.TAG_STRING)
                || !raw.contains("Sector", Tag.TAG_STRING) || !sector.factionId.equals(raw.getString("Faction"))
                || !bool(raw, "HomeWorld") || raw.getBoolean("HomeWorld") != home || !raw.contains("Deadline", Tag.TAG_LONG)
                || raw.getLong("Deadline") < 0 || home && raw.getLong("Deadline") != 0 || !home && raw.getLong("Deadline") == 0
                || raw.contains("ClaimedRun") && !raw.hasUUID("ClaimedRun")
                || !knownKeys(raw, Set.of("Id", "Owner", "Sector", "Faction", "HomeWorld", "Deadline", "ClaimedRun")))
            throw new IllegalArgumentException("Defense offer");
        return new DefenseOffer(raw.getUUID("Id"), raw.getUUID("Owner"), sector.id, sector.factionId, home,
                raw.getLong("Deadline"), raw.hasUUID("ClaimedRun") ? raw.getUUID("ClaimedRun") : null);
    }
    private static ListTag compounds(CompoundTag raw, String key, int max) {
        if (!raw.contains(key, Tag.TAG_LIST)) throw new IllegalArgumentException(key);
        ListTag list = (ListTag) raw.get(key);
        if (list.size() > max || !list.isEmpty() && list.getElementType() != Tag.TAG_COMPOUND) throw new IllegalArgumentException(key);
        return list;
    }
    private static boolean knownKeys(CompoundTag tag, Set<String> allowed) { return allowed.containsAll(tag.getAllKeys()); }
    private static boolean ints(CompoundTag tag, String... keys) { for (String key : keys) if (!tag.contains(key, Tag.TAG_INT)) return false; return true; }
    private static boolean bool(CompoundTag tag, String key) { return tag.contains(key, Tag.TAG_BYTE) && (tag.getByte(key) == 0 || tag.getByte(key) == 1); }
    static String digest(String value) {
        try { return HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256").digest(value.getBytes(StandardCharsets.UTF_8))); }
        catch (NoSuchAlgorithmException impossible) { throw new IllegalStateException(impossible); }
    }
}
