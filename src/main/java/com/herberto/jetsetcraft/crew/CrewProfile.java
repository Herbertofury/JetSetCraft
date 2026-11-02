package com.herberto.jetsetcraft.crew;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceLocation;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/** Durable roster identities and virtual progression. No Minecraft inventory, entities, or player XP live here. */
public final class CrewProfile {
    public static final int[] RADII = {8, 12, 16, 24};
    public static final int[] CAPACITIES = {3, 5, 8, 12};
    private static final long XP_LIMIT = 1_000_000_000L;
    public static final int SUPPLY_LIMIT = 1_000_000;
    final UUID owner;
    final UUID id;
    String name;
    String gang;
    String dimension;
    BlockPos anchor;
    int tier;
    int supplies = 24;
    long xp;
    long revision;
    UUID focus;
    String preparation = "NONE";
    final LinkedHashMap<UUID, Member> members = new LinkedHashMap<>();
    final LinkedHashMap<UUID, Job> jobs = new LinkedHashMap<>();
    final LinkedHashMap<String, Facility> facilities = new LinkedHashMap<>();
    final LinkedHashMap<String, Integer> reputations = new LinkedHashMap<>();
    final LinkedHashSet<UUID> settledRuns = new LinkedHashSet<>();
    final List<CrewRewardReceipt> rewards = new ArrayList<>();

    public static final class Member {
        final UUID id;
        String name;
        final String species;
        final String gang;
        final CrewMissionCatalog.Role role;
        long xp;
        Member(UUID id, String name, String species, String gang, CrewMissionCatalog.Role role, long xp) {
            this.id = id; this.name = name; this.species = species; this.gang = gang; this.role = role;
            this.xp = clampXp(xp);
        }
        public UUID id() { return id; }
        public String name() { return name; }
        public long xp() { return xp; }
        public int level() { return memberLevel(xp); }
    }

    public record Job(UUID id, UUID memberId, String missionId, String gang, long startedAt, long readyAt,
                      int supplies, int memberXp, int crewXp, int reputation) {
        public boolean ready(long now) { return now >= readyAt; }
    }
    static final class Facility {
        int level;
        BlockPos position;
        Facility(int level, BlockPos position) { this.level = level; this.position = position; }
    }

    public CrewProfile(UUID owner, UUID id, String name, String gang, String dimension, BlockPos anchor) {
        this.owner = owner; this.id = id; this.name = safeName(name, "My crew");
        this.gang = gang; this.dimension = dimension; this.anchor = anchor.immutable();
    }
    public UUID owner() { return owner; }
    public UUID id() { return id; }
    public long revision() { return revision; }
    public int supplies() { return supplies; }
    public long xp() { return xp; }
    public int tier() { return tier; }
    public int radius() { return RADII[tier]; }
    public int capacity() { return CAPACITIES[tier]; }
    public int missionSlots() { return tier >= 2 ? 3 : 2; }
    public List<Member> members() { return List.copyOf(members.values()); }
    public List<Job> jobs() { return List.copyOf(jobs.values()); }
    public List<CrewRewardReceipt> recentRewards() { return CrewRewardHistory.recent(rewards); }
    public int reputation(String gangId) { return reputations.getOrDefault(gangId, 0); }
    public boolean contains(BlockPos pos) {
        return Math.abs((long) pos.getX() - anchor.getX()) <= radius()
                && Math.abs((long) pos.getZ() - anchor.getZ()) <= radius();
    }
    public boolean busy(UUID member) { return jobs.values().stream().anyMatch(j -> j.memberId.equals(member)); }
    void changed() { if (revision < Long.MAX_VALUE) revision++; }
    void addSupplies(int amount) { supplies = (int) Math.max(0L, Math.min(SUPPLY_LIMIT, (long) supplies + amount)); }
    void addXp(long amount) { xp = clampXp(xp + Math.max(0L, Math.min(XP_LIMIT, amount))); }
    void addReputation(String gangId, int amount) {
        if (ResourceLocation.tryParse(gangId) == null || (!reputations.containsKey(gangId) && reputations.size() >= 512)) return;
        reputations.put(gangId, (int) Math.max(0L, Math.min(10_000L, (long) reputation(gangId) + amount)));
    }
    void addMember(String name, String species, String gangId, CrewMissionCatalog.Role role) {
        if (members.size() >= capacity()) return;
        UUID memberId = UUID.randomUUID();
        members.put(memberId, new Member(memberId, safeName(name, "Apprentice"), species, gangId, role, 0));
    }

    /** Dispatch freezes all reward inputs. No later facility or level change alters a job in progress. */
    public Job assignMission(UUID memberId, String missionId, long now, int facilityBonus) {
        Member member = members.get(memberId);
        CrewMissionCatalog.Mission mission = CrewMissionCatalog.byId(missionId).orElse(null);
        if (member == null || mission == null || busy(memberId) || jobs.size() >= missionSlots()
                || now < 0 || now > Long.MAX_VALUE - mission.durationTicks()) return null;
        int bonus = (member.role == mission.role() ? 25 : 0) + Math.min(20, (member.level() - 1) * 2)
                + Math.max(0, Math.min(30, facilityBonus));
        Job job = new Job(UUID.randomUUID(), memberId, mission.id(), member.gang, now,
                now + mission.durationTicks(), mission.supplies() * (100 + bonus) / 100,
                mission.memberXp(), mission.crewXp(), mission.reputation());
        jobs.put(job.id, job);
        changed();
        return job;
    }

    /** One same-record settlement; caller validates the owner and the expected revision before invoking it. */
    public boolean claimMission(UUID jobId, long now) {
        return claimMissionReward(jobId, now) != null;
    }

    /** Claims once and records the actual capped deltas for the assigned member, regardless of training focus. */
    public CrewRewardReceipt claimMissionReward(UUID jobId, long now) {
        Job job = jobs.get(jobId);
        if (job == null || !job.ready(now) || !members.containsKey(job.memberId)) return null;
        Member member = members.get(job.memberId);
        int beforeSupplies = supplies, beforeReputation = reputation(job.gang), beforeMemberLevel = member.level();
        long beforeXp = xp, beforeTraining = member.xp;
        jobs.remove(jobId);
        addSupplies(job.supplies); addXp(job.crewXp); addReputation(job.gang, job.reputation);
        member.xp = clampXp(member.xp + job.memberXp);
        CrewRewardReceipt receipt = new CrewRewardReceipt(job.id, "mission:" + job.missionId, job.gang, Math.max(0, now),
                supplies - beforeSupplies, (int) (xp - beforeXp), (int) (member.xp - beforeTraining), reputation(job.gang) - beforeReputation,
                member.id, member.name, beforeMemberLevel, member.level(), crewLevel(beforeXp), crewLevel(xp), CrewRewardReceipt.Training.APPLIED,
                supplies - beforeSupplies < job.supplies || xp - beforeXp < job.crewXp
                        || member.xp - beforeTraining < job.memberXp || reputation(job.gang) - beforeReputation < job.reputation);
        CrewRewardHistory.add(rewards, receipt);
        changed();
        return receipt;
    }
    public boolean cancelMission(UUID jobId) {
        if (jobs.remove(jobId) == null) return false;
        changed(); return true;
    }

    public CompoundTag save() {
        CompoundTag tag = new CompoundTag();
        tag.putInt("Schema", 1); tag.putUUID("Owner", owner); tag.putUUID("Id", id);
        tag.putString("Name", name); tag.putString("Gang", gang); tag.putString("Dimension", dimension);
        tag.putLong("Anchor", anchor.asLong()); tag.putInt("Tier", tier); tag.putInt("Supplies", supplies);
        tag.putLong("Xp", xp); tag.putLong("Revision", revision); tag.putString("Preparation", preparation);
        if (focus != null) tag.putUUID("Focus", focus);
        ListTag roster = new ListTag();
        for (Member member : members.values()) {
            CompoundTag row = new CompoundTag(); row.putUUID("Id", member.id); row.putString("Name", member.name);
            row.putString("Species", member.species); row.putString("Gang", member.gang);
            row.putString("Role", member.role.id); row.putLong("Xp", member.xp); roster.add(row);
        }
        tag.put("Members", roster);
        ListTag assignments = new ListTag();
        for (Job job : jobs.values()) {
            CompoundTag row = new CompoundTag(); row.putUUID("Id", job.id); row.putUUID("Member", job.memberId);
            row.putString("Mission", job.missionId); row.putString("Gang", job.gang);
            row.putLong("Start", job.startedAt); row.putLong("Ready", job.readyAt);
            row.putInt("Supplies", job.supplies); row.putInt("MemberXp", job.memberXp);
            row.putInt("CrewXp", job.crewXp); row.putInt("Reputation", job.reputation); assignments.add(row);
        }
        tag.put("Jobs", assignments);
        ListTag rooms = new ListTag();
        facilities.forEach((key, value) -> {
            CompoundTag row = new CompoundTag(); row.putString("Id", key); row.putInt("Level", value.level);
            if (value.position != null) row.putLong("Pos", value.position.asLong()); rooms.add(row);
        });
        tag.put("Facilities", rooms);
        CompoundTag relations = new CompoundTag(); reputations.forEach(relations::putInt); tag.put("Reputations", relations);
        ListTag receipts = new ListTag();
        for (UUID run : settledRuns) { CompoundTag row = new CompoundTag(); row.putUUID("Id", run); receipts.add(row); }
        tag.put("SettledRuns", receipts);
        tag.put("Rewards", CrewRewardHistory.save(rewards));
        return tag;
    }

    /** Invalid required identity is quarantined by the containing SavedData, not silently replaced by a new crew. */
    public static CrewProfile load(CompoundTag tag) {
        if (tag.getInt("Schema") != 1 || !tag.hasUUID("Owner") || !tag.hasUUID("Id")
                || ResourceLocation.tryParse(tag.getString("Gang")) == null
                || ResourceLocation.tryParse(tag.getString("Dimension")) == null
                || !tag.contains("Anchor", Tag.TAG_LONG)) return null;
        CrewProfile profile = new CrewProfile(tag.getUUID("Owner"), tag.getUUID("Id"), tag.getString("Name"),
                tag.getString("Gang"), tag.getString("Dimension"), BlockPos.of(tag.getLong("Anchor")));
        profile.tier = Math.max(0, Math.min(3, tag.getInt("Tier")));
        profile.supplies = Math.max(0, Math.min(SUPPLY_LIMIT, tag.getInt("Supplies")));
        profile.xp = clampXp(tag.getLong("Xp")); profile.revision = Math.max(0, tag.getLong("Revision"));
        if (List.of("NONE", "FORCE", "CADENCE", "FLOW", "RECOVERY").contains(tag.getString("Preparation")))
            profile.preparation = tag.getString("Preparation");
        ListTag roster = tag.getList("Members", Tag.TAG_COMPOUND);
        for (int i = 0; i < Math.min(12, roster.size()); i++) {
            CompoundTag row = roster.getCompound(i);
            if (!row.hasUUID("Id") || ResourceLocation.tryParse(row.getString("Species")) == null
                    || ResourceLocation.tryParse(row.getString("Gang")) == null) continue;
            UUID member = row.getUUID("Id");
            profile.members.putIfAbsent(member, new Member(member, safeName(row.getString("Name"), "Apprentice"),
                    row.getString("Species"), row.getString("Gang"), CrewMissionCatalog.Role.from(row.getString("Role")), row.getLong("Xp")));
        }
        if (tag.hasUUID("Focus") && profile.members.containsKey(tag.getUUID("Focus"))) profile.focus = tag.getUUID("Focus");
        ListTag assignments = tag.getList("Jobs", Tag.TAG_COMPOUND);
        for (int i = 0; i < Math.min(profile.missionSlots(), assignments.size()); i++) {
            CompoundTag row = assignments.getCompound(i);
            if (!row.hasUUID("Id") || !row.hasUUID("Member") || !profile.members.containsKey(row.getUUID("Member"))
                    || profile.busy(row.getUUID("Member")) || CrewMissionCatalog.byId(row.getString("Mission")).isEmpty()
                    || ResourceLocation.tryParse(row.getString("Gang")) == null
                    || !row.contains("Start", Tag.TAG_LONG) || !row.contains("Ready", Tag.TAG_LONG)
                    || row.getLong("Start") < 0 || row.getLong("Ready") < row.getLong("Start")) continue;
            int supplies = boundedReward(row, "Supplies", 200), memberXp = boundedReward(row, "MemberXp", 500);
            int crewXp = boundedReward(row, "CrewXp", 500), reputation = boundedReward(row, "Reputation", 100);
            if (supplies < 0 || memberXp < 0 || crewXp < 0 || reputation < 0) continue;
            Job job = new Job(row.getUUID("Id"), row.getUUID("Member"), row.getString("Mission"), row.getString("Gang"),
                    row.getLong("Start"), row.getLong("Ready"), supplies, memberXp, crewXp, reputation);
            profile.jobs.putIfAbsent(job.id, job);
        }
        ListTag rooms = tag.getList("Facilities", Tag.TAG_COMPOUND);
        for (int i = 0; i < Math.min(4, rooms.size()); i++) {
            CompoundTag row = rooms.getCompound(i);
            if (!List.of("workshop", "training", "studio", "recovery").contains(row.getString("Id"))) continue;
            profile.facilities.putIfAbsent(row.getString("Id"), new Facility(Math.max(0, Math.min(3, row.getInt("Level"))),
                    row.contains("Pos", Tag.TAG_LONG) ? BlockPos.of(row.getLong("Pos")) : null));
        }
        CompoundTag relations = tag.getCompound("Reputations");
        for (String key : relations.getAllKeys()) {
            if (profile.reputations.size() >= 512) break;
            if (ResourceLocation.tryParse(key) != null) profile.addReputation(key, Math.max(0, relations.getInt(key)));
        }
        ListTag receipts = tag.getList("SettledRuns", Tag.TAG_COMPOUND);
        for (int i = Math.max(0, receipts.size() - 128); i < receipts.size(); i++) {
            if (receipts.getCompound(i).hasUUID("Id")) profile.settledRuns.add(receipts.getCompound(i).getUUID("Id"));
        }
        CrewRewardHistory.load(profile.rewards, tag.getList("Rewards", Tag.TAG_COMPOUND));
        return profile;
    }
    private static int boundedReward(CompoundTag tag, String key, int max) {
        return tag.contains(key, Tag.TAG_INT) && tag.getInt(key) >= 0 && tag.getInt(key) <= max ? tag.getInt(key) : -1;
    }
    public static int crewLevel(long xp) { int level = 1; while (level < 20 && xp >= crewThreshold(level + 1)) level++; return level; }
    public static long crewThreshold(int level) { return 50L * (level - 1L) * level; }
    public static int memberLevel(long xp) { int level = 1; while (level < 10 && xp >= memberThreshold(level + 1)) level++; return level; }
    public static long memberThreshold(int level) { return 60L * (level - 1L) * (level - 1L); }
    static long clampXp(long value) { return Math.max(0, Math.min(XP_LIMIT, value)); }
    static String safeName(String value, String fallback) {
        if (value == null) return fallback;
        String safe = value.replaceAll("[\\p{Cntrl}§]", "").trim();
        return safe.isEmpty() ? fallback : safe.substring(0, Math.min(36, safe.length()));
    }
}
