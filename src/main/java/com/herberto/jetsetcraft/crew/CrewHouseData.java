package com.herberto.jetsetcraft.crew;

import com.herberto.jetsetcraft.data.SavedDataCheckpoint;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.level.saveddata.SavedData;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.List;
import java.util.Set;
import java.util.UUID;

/** One world-owned progression store; temporary Rush journals never serialize or restore it. */
public final class CrewHouseData extends SavedData {
    private static final String NAME = "jetsetcraft_crew_houses";
    private final Map<UUID, CrewProfile> profiles = new LinkedHashMap<>();
    private final Map<UUID, PendingCrewProgress> pending = new LinkedHashMap<>();
    private record StyleChoice(String id, long revision) {}
    private final Map<UUID, StyleChoice> styleChoices = new LinkedHashMap<>();
    private final Map<UUID, CrewTour.Progress> tours = new LinkedHashMap<>();
    private final ListTag quarantined = new ListTag();
    private CompoundTag newerSchema;

    public static CrewHouseData get(MinecraftServer server) {
        return server.overworld().getDataStorage().computeIfAbsent(CrewHouseData::load, CrewHouseData::new, NAME);
    }
    public static boolean saveAcknowledged(MinecraftServer server) {
        return server != null && SavedDataCheckpoint.saveAndAcknowledge(server, NAME, get(server));
    }
    public CrewProfile profile(UUID owner) { return profiles.get(owner); }
    public Collection<CrewProfile> profiles() { return java.util.List.copyOf(profiles.values()); }
    public boolean readOnly() { return newerSchema != null; }
    public boolean blocked(UUID owner) {
        if (readOnly()) return true;
        for (int i = 0; i < quarantined.size(); i++) {
            CompoundTag row = quarantined.getCompound(i);
            if (row.hasUUID("Owner") && row.getUUID("Owner").equals(owner)) return true;
        }
        return false;
    }
    public boolean add(CrewProfile profile) {
        if (profile == null || blocked(profile.owner()) || profiles.containsKey(profile.owner())) return false;
        PendingCrewProgress earned = pending.get(profile.owner());
        if (earned != null) earned.transfer(profile);
        CrewTour.Progress tour = tours.get(profile.owner());
        // Founding changes the reward's account, training recipient and reputation destination.
        if (tour != null && tour.revision < Long.MAX_VALUE) tour.revision++;
        profiles.put(profile.owner(), profile); pending.remove(profile.owner()); setDirty(); return true;
    }
    public int pendingSupplies(UUID owner) { PendingCrewProgress p = pending.get(owner); return p == null ? 0 : p.supplies; }
    public long pendingXp(UUID owner) { PendingCrewProgress p = pending.get(owner); return p == null ? 0 : p.xp; }
    public long pendingTraining(UUID owner) { PendingCrewProgress p = pending.get(owner); return p == null ? 0 : p.training; }
    public long styleXp(UUID owner) {
        CrewProfile profile = profiles.get(owner);
        return profile == null ? pendingXp(owner) : profile.xp();
    }
    public String selectedStyle(UUID owner) {
        StyleChoice choice = styleChoices.get(owner); return choice == null ? CrewStyleLibrary.HOME : choice.id();
    }
    public long styleRevision(UUID owner) {
        StyleChoice choice = styleChoices.get(owner); return choice == null ? 0 : choice.revision();
    }
    public boolean selectStyle(UUID owner, String id, long expectedRevision) {
        if (owner == null || expectedRevision != styleRevision(owner) || expectedRevision == Long.MAX_VALUE
                || !CrewStyleLibrary.canSelect(this, owner, id)) return false;
        styleChoices.put(owner, new StyleChoice(id, expectedRevision + 1)); setDirty(); return true;
    }
    public int pendingReputation(UUID owner, String gang) {
        PendingCrewProgress p = pending.get(owner); return p == null ? 0 : p.reputations.getOrDefault(gang, 0);
    }
    public List<CrewRewardReceipt> recentRewards(UUID owner) {
        CrewProfile profile = profiles.get(owner);
        if (profile != null) return profile.recentRewards();
        PendingCrewProgress progress = pending.get(owner);
        return progress == null ? List.of() : CrewRewardHistory.recent(progress.rewards);
    }

    /**
     * A claim observes both tour progress and its reward account. Each revision is monotonic;
     * their bounded sum also invalidates previews when training focus, jobs or balances change.
     * Saturation is terminal for claims, rather than wrapping into a previously valid token.
     */
    public long tourRevision(UUID owner) {
        CrewTour.Progress progress = tours.get(owner);
        CrewProfile profile = profiles.get(owner);
        long tour = progress == null ? 0 : progress.revision;
        long account = profile == null ? 0 : profile.revision;
        return account > Long.MAX_VALUE - tour ? Long.MAX_VALUE : tour + account;
    }

    /** Read-only tour previews do not create accounts or award progress. */
    public CompoundTag tourSnapshot(UUID owner) {
        CrewTour.Progress progress = tours.get(owner);
        if (progress == null) progress = new CrewTour.Progress(owner);
        CompoundTag snapshot = CrewTour.snapshot(progress, tourGang(owner, progress), blocked(owner), tourRevision(owner));
        CrewProfile profile = profiles.get(owner);
        CrewProfile.Member member = profile == null ? null : profile.members.get(profile.focus);
        boolean busy = member != null && profile.busy(member.id);
        snapshot.putString("TrainingSummary", profile == null ? "Training XP will be saved for your first home."
                : member == null ? "No training XP will be applied: no training focus is selected."
                : busy ? "No training XP will be applied: " + member.name + " is away on a job."
                : "Training XP goes to " + member.name + " (level " + member.level() + "), up to their XP limit.");
        snapshot.putBoolean("TrainingEligible", profile == null || member != null && !busy);
        return snapshot;
    }

    /** Caller validates the living owner and normal-world action boundary, as with style selection. */
    public CrewRewardReceipt claimTour(UUID owner, String id, long expectedRevision, long gameTime) {
        CrewTour.Progress progress = tours.get(owner);
        CrewTour.Milestone milestone = CrewTour.byId(id);
        long currentRevision = tourRevision(owner);
        if (owner == null || blocked(owner) || progress == null || milestone == null || gameTime < 0
                || expectedRevision != currentRevision || currentRevision == Long.MAX_VALUE
                || progress.claimed.contains(id) || !progress.complete(milestone)) return null;
        String gang = tourGang(owner, progress);
        if (!CrewTour.validGang(gang)) return null;
        // Both the permanent claim flag and the exact capped reward live in this same SavedData record.
        CrewRewardReceipt receipt = award(owner, CrewTour.receiptId(owner, id), gang, milestone.supplies(),
                milestone.crewXp(), milestone.memberXp(), milestone.reputation(), "tour:" + id, gameTime);
        if (receipt == null) return null;
        progress.claimed.add(id); progress.revision++; setDirty(); return receipt;
    }

    private String tourGang(UUID owner, CrewTour.Progress progress) {
        CrewProfile profile = profiles.get(owner);
        return profile == null ? progress.lastGang : profile.gang;
    }

    private void recordTour(UUID owner, CrewRewardReceipt receipt) {
        if (CrewTour.source(receipt.source()) != null)
            tours.computeIfAbsent(owner, CrewTour.Progress::new).accepted(receipt);
    }
    /** Same save record owns receipt and reward; a first house absorbs its pending account exactly once. */
    boolean award(UUID owner, UUID receipt, String gang, int supplies, int xp, int training, int reputation) {
        return award(owner, receipt, gang, supplies, xp, training, reputation, "activity", 0) != null;
    }
    CrewRewardReceipt award(UUID owner, UUID receipt, String gang, int supplies, int xp, int training, int reputation,
                            String source, long earnedAt) {
        if (owner == null || receipt == null || blocked(owner) || gang == null || ResourceLocation.tryParse(gang) == null) return null;
        supplies = Math.max(0, supplies); xp = Math.max(0, xp); training = Math.max(0, training); reputation = Math.max(0, reputation);
        CrewProfile profile = profiles.get(owner);
        if (profile == null) {
            PendingCrewProgress progress = pending.computeIfAbsent(owner, PendingCrewProgress::new);
            int beforeSupplies = progress.supplies, beforeReputation = progress.reputations.getOrDefault(gang, 0);
            long beforeXp = progress.xp, beforeTraining = progress.training;
            if (!progress.award(receipt, gang, supplies, xp, training, reputation)) return null;
            CrewRewardReceipt awarded = new CrewRewardReceipt(receipt, source, gang, Math.max(0, earnedAt),
                    progress.supplies - beforeSupplies, (int) (progress.xp - beforeXp), (int) (progress.training - beforeTraining),
                    progress.reputations.getOrDefault(gang, 0) - beforeReputation, null, "", 0, 0, 1, 1, CrewRewardReceipt.Training.BANKED,
                    progress.supplies - beforeSupplies < supplies || progress.xp - beforeXp < xp
                            || progress.training - beforeTraining < training || progress.reputations.getOrDefault(gang, 0) - beforeReputation < reputation);
            CrewRewardHistory.add(progress.rewards, awarded); recordTour(owner, awarded); setDirty(); return awarded;
        }
        if (profile.settledRuns.contains(receipt)) return null;
        CrewProfile.Member member = profile.members.get(profile.focus);
        boolean busy = member != null && profile.busy(member.id);
        int beforeSupplies = profile.supplies, beforeReputation = profile.reputation(gang), beforeMemberLevel = member == null ? 0 : member.level();
        long beforeXp = profile.xp, beforeTraining = member == null ? 0 : member.xp;
        profile.settledRuns.add(receipt);
        while (profile.settledRuns.size() > 128) profile.settledRuns.remove(profile.settledRuns.iterator().next());
        profile.addSupplies(supplies); profile.addXp(xp); profile.addReputation(gang, reputation);
        if (member != null && !busy) member.xp = CrewProfile.clampXp(member.xp + training);
        int awardedTraining = member == null ? 0 : (int) (member.xp - beforeTraining);
        CrewRewardReceipt awarded = new CrewRewardReceipt(receipt, source, gang, Math.max(0, earnedAt),
                profile.supplies - beforeSupplies, (int) (profile.xp - beforeXp), awardedTraining, profile.reputation(gang) - beforeReputation,
                member == null ? null : member.id, member == null ? "" : member.name, beforeMemberLevel, member == null ? 0 : member.level(),
                CrewProfile.crewLevel(beforeXp), CrewProfile.crewLevel(profile.xp), member == null ? CrewRewardReceipt.Training.NO_FOCUS
                    : busy ? CrewRewardReceipt.Training.BUSY : CrewRewardReceipt.Training.APPLIED,
                profile.supplies - beforeSupplies < supplies || profile.xp - beforeXp < xp || profile.reputation(gang) - beforeReputation < reputation
                        || (member != null && !busy && awardedTraining < training));
        CrewRewardHistory.add(profile.rewards, awarded); recordTour(owner, awarded); profile.changed(); setDirty(); return awarded;
    }
    public static CrewHouseData load(CompoundTag tag) {
        CrewHouseData data = new CrewHouseData();
        Tag rawTours = tag.get("TourProgress");
        if (tag.getInt("Schema") < 1 || tag.getInt("Schema") > 4
                || tag.getInt("Schema") == 4 && rawTours == null
                || rawTours != null && (!(rawTours instanceof ListTag)
                    || !((ListTag) rawTours).isEmpty() && ((ListTag) rawTours).getElementType() != Tag.TAG_COMPOUND)) {
            data.newerSchema = tag.copy(); return data;
        }
        ListTag rows = tag.getList("Profiles", Tag.TAG_COMPOUND);
        for (int i = 0; i < rows.size(); i++) {
            CompoundTag row = rows.getCompound(i);
            CrewProfile profile = CrewProfile.load(row);
            if (profile == null || data.profiles.containsKey(profile.owner())) data.quarantined.add(row.copy());
            else data.profiles.put(profile.owner(), profile);
        }
        ListTag oldQuarantine = tag.getList("Quarantined", Tag.TAG_COMPOUND);
        for (int i = 0; i < oldQuarantine.size(); i++) data.quarantined.add(oldQuarantine.getCompound(i).copy());
        ListTag pendingRows = tag.getList("PendingProgress", Tag.TAG_COMPOUND);
        for (int i = 0; i < pendingRows.size(); i++) {
            CompoundTag row = pendingRows.getCompound(i);
            PendingCrewProgress progress = PendingCrewProgress.load(row);
            if (progress == null || data.pending.containsKey(progress.owner) || data.profiles.containsKey(progress.owner))
                data.quarantined.add(row.copy());
            else data.pending.put(progress.owner, progress);
        }
        ListTag choices = tag.getList("StyleChoices", Tag.TAG_COMPOUND);
        for (int i = 0; i < choices.size(); i++) {
            CompoundTag row = choices.getCompound(i);
            if (!row.hasUUID("Owner") || !row.contains("Revision", Tag.TAG_LONG) || row.getLong("Revision") < 1
                    || !CrewStyleLibrary.known(row.getString("Style")) || data.styleChoices.containsKey(row.getUUID("Owner"))) {
                data.quarantined.add(row.copy());
            } else data.styleChoices.put(row.getUUID("Owner"), new StyleChoice(row.getString("Style"), row.getLong("Revision")));
        }
        ListTag tourRows = tag.getList("TourProgress", Tag.TAG_COMPOUND);
        for (int i = 0; i < tourRows.size(); i++) {
            CompoundTag row = tourRows.getCompound(i);
            CrewTour.Progress progress = CrewTour.load(row);
            if (progress == null || data.tours.containsKey(progress.owner)) data.quarantined.add(row.copy());
            else data.tours.put(progress.owner, progress);
        }
        if (tag.getInt("Schema") < 4) {
            // Old saves retain only sixteen display receipts: credit those actual retained results,
            // never infer a lifetime total from XP, supplies, or jobs that were not played activities.
            Set<UUID> owners = new java.util.LinkedHashSet<>(data.profiles.keySet()); owners.addAll(data.pending.keySet());
            for (UUID owner : owners) if (!data.tours.containsKey(owner) && !data.blocked(owner)) {
                Set<UUID> imported = new java.util.HashSet<>();
                List<CrewRewardReceipt> history = data.recentRewards(owner);
                for (int i = history.size() - 1; i >= 0; i--) {
                    CrewRewardReceipt receipt = history.get(i);
                    if (imported.add(receipt.id())) data.recordTour(owner, receipt);
                }
            }
            data.setDirty();
        }
        return data;
    }
    @Override public CompoundTag save(CompoundTag tag) {
        if (newerSchema != null) return newerSchema.copy();
        tag.putInt("Schema", 4);
        ListTag rows = new ListTag(); for (CrewProfile profile : profiles.values()) rows.add(profile.save());
        tag.put("Profiles", rows); tag.put("Quarantined", quarantined.copy());
        ListTag unclaimed = new ListTag(); for (PendingCrewProgress progress : pending.values()) unclaimed.add(progress.save());
        tag.put("PendingProgress", unclaimed);
        ListTag choices = new ListTag();
        styleChoices.forEach((owner, choice) -> {
            CompoundTag row = new CompoundTag(); row.putUUID("Owner", owner); row.putString("Style", choice.id());
            row.putLong("Revision", choice.revision()); choices.add(row);
        });
        tag.put("StyleChoices", choices);
        ListTag tourRows = new ListTag(); for (CrewTour.Progress progress : tours.values()) tourRows.add(progress.save());
        tag.put("TourProgress", tourRows);
        return tag;
    }
}
