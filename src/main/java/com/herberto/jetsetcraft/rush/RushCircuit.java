package com.herberto.jetsetcraft.rush;

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
        CompoundTag entry = new CompoundTag(); entry.putInt("Stage", completed + 1);
        entry.putString("Name", current.name()); entry.putString("Reward", current.reward().label()); history.add(entry);
        completed++; version++;
        if (completed == 6) { phase = Phase.COMPLETE; choices = List.of(); run.pauseForEncore(); }
        else { phase = Phase.CHOOSING; offer(); }
        return true;
    }
    public boolean encore() {
        if (phase != Phase.COMPLETE || set == Integer.MAX_VALUE) return false;
        set++; completed = 0; phase = Phase.CHOOSING; history.clear(); center = origin; builder = null; version++;
        offer(); return true;
    }
    private void offer() {
        List<RushArenaCatalog.Profile> available = new ArrayList<>(RushArenaCatalog.all());
        Collections.shuffle(available, new Random(seed ^ ((long) set << 32) ^ completed * 7919L));
        // Prefer different reward families before filling the remaining slots.
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
        ListTag choiceTags = new ListTag(); choices.forEach(p -> choiceTags.add(p.summary())); tag.put("Choices", choiceTags);
        ListTag historyTags = new ListTag(); history.forEach(h -> historyTags.add(h.copy())); tag.put("History", historyTags);
        return tag;
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
