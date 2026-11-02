package com.herberto.jetsetcraft.rush;

import com.herberto.jetsetcraft.crew.CrewHouseManager;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.phys.Vec3;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;
import java.util.UUID;

/** Server-held six-room route. Client intent selects only one currently offered recipe/version. */
public final class RushCircuit {
    public enum Phase { CHOOSING, TRAVEL, ARENA, COMPLETE }
    private final UUID runId;
    private final long seed;
    private final BlockPos origin;
    private final int slot;
    private final List<RushArenaCatalog.Profile> catalogue;
    private int completed, set = 1, version = 1;
    private Phase phase = Phase.CHOOSING;
    private List<RushArenaCatalog.Profile> choices = List.of();
    private final List<CompoundTag> history = new ArrayList<>();
    private RushArenaCatalog.Profile current;
    private BlockPos center;
    private RushArenaBuilder builder;
    private long beganAt, startDefeats;

    public RushCircuit(UUID runId, long seed, BlockPos origin, int slot) {
        this.runId = runId; this.seed = seed; this.origin = origin; this.slot = slot; this.center = origin;
        this.catalogue = List.copyOf(RushArenaCatalog.all());
        offer();
    }
    public int slot() { return slot; }
    public int completed() { return completed; }
    public int set() { return set; }
    public int version() { return version; }
    public Phase phase() { return phase; }
    public BlockPos origin() { return origin; }
    public BlockPos center() { return center; }
    public RushArenaCatalog.Profile current() { return current; }
    public RushArenaBuilder builder() { return builder; }
    public List<RushArenaCatalog.Profile> choices() { return choices; }
    public long activeTicks(RushRun run) { return phase == Phase.ARENA ? Math.max(0, run.elapsedTicks() - beganAt) : 0; }
    public long defeats(RushRun run) { return Math.max(0, run.defeats() - startDefeats); }
    public boolean inCell(Vec3 pos) { return Math.abs(pos.x - origin.getX()) <= 1536 && Math.abs(pos.z - origin.getZ()) <= 1536; }
    public boolean inArena(Vec3 pos, int margin) {
        return current != null && Math.abs(pos.x - center.getX()) <= current.radius() + margin
                && Math.abs(pos.z - center.getZ()) <= current.radius() + margin;
    }

    public boolean choose(ResourceLocation id, int offeredVersion) {
        if (phase != Phase.CHOOSING || offeredVersion != version || id == null) return false;
        for (int i = 0; i < choices.size(); i++) if (choices.get(i).id().equals(id)) {
            select(choices.get(i), i - 1); return true;
        }
        return false;
    }
    public void firstArena(RushArenaCatalog.Profile profile) {
        if (completed != 0 || set != 1 || phase != Phase.CHOOSING) throw new IllegalStateException("First arena already chosen");
        select(profile, 0);
    }
    private void select(RushArenaCatalog.Profile profile, int branch) {
        BlockPos previous = center;
        current = profile;
        center = previous.offset(branch * 128, 0, 128);
        builder = new RushArenaBuilder(profile, previous, center);
        phase = Phase.TRAVEL;
        choices = List.of(); version++;
    }
    public boolean arrive(RushRun run, Vec3 position) {
        if (phase != Phase.TRAVEL || builder == null || !builder.readyForEntry() || !inArena(position, -6)) return false;
        phase = Phase.ARENA; beganAt = run.elapsedTicks(); startDefeats = run.defeats(); version++;
        return true;
    }
    public boolean objectiveMet(RushRun run) {
        return phase == Phase.ARENA && activeTicks(run) >= current.seconds() * 20L && defeats(run) >= current.defeats();
    }
    public UUID rewardId() {
        return UUID.nameUUIDFromBytes((runId + ":arena:" + set + ":" + completed).getBytes(StandardCharsets.UTF_8));
    }
    /** The manager first validates physical participation and banks one authoritative node reward. */
    public boolean complete(RushRun run) {
        if (!objectiveMet(run)) return false;
        CompoundTag entry = arenaSummary(current, center);
        entry.putInt("Stage", completed + 1); entry.putInt("Set", set); entry.putUUID("Receipt", rewardId());
        entry.putString("Reward", current.reward().label()); entry.putBoolean("SettlementRecorded", false);
        // Completion does not itself assert a banked payout. The manager records the actual settled deltas.
        entry.remove("SharedReward"); history.add(entry);
        completed++; version++;
        if (completed == 6) { phase = Phase.COMPLETE; choices = List.of(); run.pauseForEncore(); }
        else { phase = Phase.CHOOSING; offer(); }
        return true;
    }

    /** Attaches exactly one real manager settlement to the latest completed room; never accepts a client amount. */
    public boolean recordSettlement(UUID receipt, String runReward, CompoundTag sharedReward) {
        if (receipt == null || runReward == null || sharedReward == null || history.isEmpty() || history.size() > 6) return false;
        CompoundTag entry = history.get(history.size() - 1);
        if (!entry.hasUUID("Receipt") || !receipt.equals(entry.getUUID("Receipt")) || entry.getBoolean("SettlementRecorded")) return false;
        int[] limits = {60, 300, 180, 40};
        String[] amounts = {"Supplies", "CrewXp", "MemberXp", "Reputation"};
        CompoundTag settled = new CompoundTag();
        settled.putBoolean("Accepted", sharedReward.getBoolean("Accepted"));
        settled.putBoolean("PendingHouse", sharedReward.getBoolean("PendingHouse"));
        for (int i = 0; i < amounts.length; i++) {
            int value = sharedReward.getInt(amounts[i]);
            if (value < 0 || value > limits[i] || !settled.getBoolean("Accepted") && value != 0) return false;
            settled.putInt(amounts[i], value);
        }
        entry.putString("RunReward", runReward.substring(0, Math.min(240, runReward.length())));
        entry.put("SharedReward", settled); entry.putBoolean("SettlementRecorded", true); version++;
        return true;
    }
    public boolean encore() {
        if (phase != Phase.COMPLETE || set == Integer.MAX_VALUE) return false;
        set++; completed = 0; phase = Phase.CHOOSING; history.clear(); current = null; center = origin; builder = null; version++;
        offer(); return true;
    }
    private void offer() {
        List<RushArenaCatalog.Profile> available = new ArrayList<>(catalogue);
        Collections.shuffle(available, new Random(seed ^ ((long) set << 32) ^ completed * 7919L));
        // Stable sorting retains seeded variety among equally visited rooms. Offer new places first,
        // then retain a choice of reward families when a small pack needs to repeat earlier rooms.
        available.sort(java.util.Comparator.comparingLong(this::visits));
        List<RushArenaCatalog.Profile> selected = new ArrayList<>();
        for (RushArenaCatalog.Profile candidate : available) {
            if (selected.size() == 3) break;
            if (selected.stream().noneMatch(p -> p.reward() == candidate.reward())) selected.add(candidate);
        }
        for (RushArenaCatalog.Profile candidate : available) {
            if (selected.size() == 3) break;
            if (!selected.contains(candidate)) selected.add(candidate);
        }
        choices = List.copyOf(selected);
    }
    private long visits(RushArenaCatalog.Profile profile) {
        return history.stream().filter(room -> room.getString("Id").equals(profile.id().toString())).count();
    }
    public CompoundTag snapshot(RushRun run, Vec3 position) {
        CompoundTag tag = new CompoundTag();
        tag.putString("Phase", phase.name().toLowerCase(java.util.Locale.ROOT));
        tag.putInt("Stage", Math.min(6, completed + 1)); tag.putInt("Completed", completed);
        tag.putInt("Set", set); tag.putInt("StagesPerSet", 6); tag.putInt("Version", version);
        tag.putString("CurrentName", current == null ? "Choose your first arena" : current.name());
        tag.putString("Reward", current == null ? "" : current.reward().label());
        tag.putLong("TicksLeft", current == null ? 0 : Math.max(0, current.seconds() * 20L - activeTicks(run)));
        tag.putInt("Distance", (int) Math.sqrt(position.distanceToSqr(Vec3.atCenterOf(center))));
        tag.putString("Objective", objective(run));
        tag.putInt("OriginX", 0); tag.putInt("OriginZ", 0); tag.putInt("MapScaleBlocks", 128);
        // A cleared room already exists in History. It must not appear again as a new/current destination.
        if (current != null && (phase == Phase.TRAVEL || phase == Phase.ARENA)) {
            CompoundTag destination = arenaSummary(current, center);
            destination.putInt("Stage", completed + 1); destination.putInt("Set", set);
            tag.put("Current", destination);
        }
        ListTag choiceTags = new ListTag();
        for (int i = 0; i < Math.min(3, choices.size()); i++) {
            CompoundTag choice = arenaSummary(choices.get(i), center.offset((i - 1) * 128, 0, 128));
            choice.putInt("Stage", completed + 1); choice.putInt("Set", set); choiceTags.add(choice);
        }
        tag.put("Choices", choiceTags);
        ListTag historyTags = new ListTag(); history.forEach(h -> historyTags.add(h.copy())); tag.put("History", historyTags);
        if (!history.isEmpty()) tag.put("LastCompleted", history.get(history.size() - 1).copy());
        return tag;
    }

    private CompoundTag arenaSummary(RushArenaCatalog.Profile profile, BlockPos destination) {
        CompoundTag tag = profile.summary();
        tag.putInt("Defeats", profile.defeats()); tag.putString("Emphasis", profile.sharedReward());
        tag.putInt("VisitsThisSet", (int) visits(profile));
        tag.putString("RunReward", previewRunReward(profile));
        tag.putInt("MapX", (destination.getX() - origin.getX()) / 128);
        tag.putInt("MapZ", (destination.getZ() - origin.getZ()) / 128);
        String mode = profile.sharedReward().equals("balanced") ? "rush_arena" : "rush_arena_" + profile.sharedReward();
        var reward = CrewHouseManager.activityReward(mode);
        CompoundTag shared = new CompoundTag();
        shared.putInt("Supplies", reward.supplies()); shared.putInt("CrewXp", reward.crewXp());
        shared.putInt("MemberXp", reward.memberXp()); shared.putInt("Reputation", reward.reputation());
        tag.put("SharedReward", shared);
        return tag;
    }

    private static String previewRunReward(RushArenaCatalog.Profile profile) {
        String ranks = "+" + profile.rewardRanks() + (profile.rewardRanks() == 1 ? " rank" : " ranks");
        return switch (profile.reward()) {
            case DAMAGE -> "Paint pressure " + ranks + " · 15% damage per rank";
            case CRITICAL -> "Critical chance and power " + ranks;
            case AREA -> "Bigger chorus " + ranks + " · 12% area per rank";
            case RELIC -> "Artifact " + ranks + " · within your three family slots";
            case MAGNET -> "Magnetic mixtape " + ranks + " + 10s pickup magnet";
            case RECOVERY -> "Easy breathing " + ranks + " + " + (25 * profile.rewardRanks()) + " vitality";
        };
    }
    public String objective(RushRun run) {
        return switch (phase) {
            case CHOOSING -> "Choose the next arena and its reward.";
            case TRAVEL -> "Ride the lit connection to " + current.name() + ".";
            case ARENA -> current.name() + ": " + defeats(run) + "/" + current.defeats() + " rivals · "
                    + Math.max(0, current.seconds() - activeTicks(run) / 20) + "s · " + current.reward().label();
            case COMPLETE -> "Six arenas cleared! Return with your crew progress or take an Encore.";
        };
    }
}
