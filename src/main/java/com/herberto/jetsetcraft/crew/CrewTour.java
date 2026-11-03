package com.herberto.jetsetcraft.crew;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceLocation;

import java.nio.charset.StandardCharsets;
import java.util.EnumMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

/**
 * Evergreen connections between modes. Accepted shared settlements supply the stamps; finite
 * milestones pay the existing crew account. Neither calendar time nor installed providers gates
 * the nine core milestones, and a tour payout never supplies another stamp.
 */
public final class CrewTour {
    public static final int COUNT_LIMIT = 1_000_000;

    public enum Track {
        TAG("Tag wins"), HILL("Hill wins"), SHOWDOWN("Showdown wins"),
        ARENA("Rush rooms cleared"), RETURN("Qualifying Rush returns"), LIBERTY("Qualifying Liberty debriefs"),
        RESULTS("Activity results"), MODES("Different modes");
        public final String label;
        Track(String label) { this.label = label; }
    }

    public record Requirement(Track track, int target) {}
    public record Milestone(String id, String name, String description, boolean optional,
                            int supplies, int crewXp, int memberXp, int reputation, List<Requirement> requirements) {}

    public static final List<Milestone> MILESTONES = List.of(
            milestone("first_set", "First set", "Find your rhythm across a few different activities.", false, 6, 50, 15, 3,
                    need(Track.RESULTS, 3), need(Track.MODES, 2)),
            milestone("street_circuit", "Street circuit", "Connect tag chases, territory and crew battles.", false, 12, 100, 30, 8,
                    need(Track.TAG, 2), need(Track.HILL, 2), need(Track.SHOWDOWN, 2)),
            milestone("tag_and_pressure", "Tag and pressure", "Bring your tag-chasing rhythm into the arena journey.", false, 10, 80, 25, 6,
                    need(Track.TAG, 3), need(Track.ARENA, 3)),
            milestone("hill_to_headliner", "Hill to headliner", "Hold the streets, then bank a qualifying Rush run.", false, 12, 100, 30, 8,
                    need(Track.HILL, 3), need(Track.RETURN, 1)),
            milestone("showdown_to_skyline", "Showdown to skyline", "Carry your fighting confidence through six arena clears.", false, 14, 120, 40, 8,
                    need(Track.SHOWDOWN, 3), need(Track.ARENA, 6)),
            milestone("homecoming", "Homecoming", "Bring three Rush runs home to a crew with varied experience.", false, 18, 150, 50, 12,
                    need(Track.RETURN, 3), need(Track.MODES, 3)),
            milestone("block_party", "Block party", "Make the whole neighborhood part of your progress.", false, 24, 200, 60, 16,
                    need(Track.TAG, 5), need(Track.HILL, 5), need(Track.SHOWDOWN, 5), need(Track.RETURN, 2)),
            milestone("mixtape_master", "Mixtape master", "Keep a varied set of activities in your rotation.", false, 30, 250, 90, 20,
                    need(Track.RESULTS, 30), need(Track.MODES, 4)),
            milestone("street_legend", "Street legend", "Build a lasting reputation across the standalone modes.", false, 40, 300, 100, 25,
                    need(Track.TAG, 12), need(Track.HILL, 12), need(Track.SHOWDOWN, 12), need(Track.RETURN, 6)),
            milestone("expedition_exchange", "Expedition exchange", "Bring expedition experience back into the neighborhood.", true, 16, 120, 35, 10,
                    need(Track.LIBERTY, 2), need(Track.MODES, 3)),
            milestone("freedom_on_wheels", "Freedom on wheels", "Link completed expedition objectives to tag wins on your own streets.", true, 20, 180, 60, 14,
                    need(Track.LIBERTY, 4), need(Track.TAG, 4)),
            milestone("all_city", "All city", "Celebrate progress in every activity family, including Liberty Grind.", true, 24, 200, 60, 16,
                    need(Track.TAG, 3), need(Track.HILL, 3), need(Track.SHOWDOWN, 3), need(Track.RETURN, 2), need(Track.LIBERTY, 2)));

    static final class Progress {
        final UUID owner;
        final EnumMap<Track, Integer> counts = new EnumMap<>(Track.class);
        final Set<String> claimed = new LinkedHashSet<>();
        long revision;
        String lastGang = "";
        Progress(UUID owner) { this.owner = owner; }

        int count(Track track) {
            if (track == Track.RESULTS) {
                long sum = 0;
                for (Track source : sources()) sum += counts.getOrDefault(source, 0);
                return (int) Math.min(COUNT_LIMIT, sum);
            }
            if (track == Track.MODES) {
                int modes = 0;
                for (Track source : new Track[]{Track.TAG, Track.HILL, Track.SHOWDOWN, Track.LIBERTY})
                    if (counts.getOrDefault(source, 0) > 0) modes++;
                if (counts.getOrDefault(Track.ARENA, 0) > 0 || counts.getOrDefault(Track.RETURN, 0) > 0) modes++;
                return modes;
            }
            return counts.getOrDefault(track, 0);
        }

        void accepted(CrewRewardReceipt receipt) {
            Track track = source(receipt.source());
            if (track == null || !validGang(receipt.gang()) || revision == Long.MAX_VALUE) return;
            counts.put(track, Math.min(COUNT_LIMIT, count(track) + 1));
            lastGang = receipt.gang(); revision++;
        }

        boolean complete(Milestone milestone) {
            return milestone.requirements().stream().allMatch(requirement -> count(requirement.track()) >= requirement.target());
        }

        CompoundTag save() {
            CompoundTag tag = new CompoundTag(); tag.putInt("Schema", 1); tag.putUUID("Owner", owner);
            tag.putLong("Revision", revision); tag.putString("LastGang", lastGang);
            CompoundTag tracks = new CompoundTag();
            for (Track source : sources()) tracks.putInt(source.name(), count(source));
            tag.put("Counts", tracks);
            ListTag collected = new ListTag();
            for (Milestone milestone : MILESTONES) if (claimed.contains(milestone.id())) collected.add(StringTag.valueOf(milestone.id()));
            tag.put("Claimed", collected); return tag;
        }
    }

    static Track source(String source) {
        return switch (source) {
            case "tag" -> Track.TAG;
            case "king_of_hill" -> Track.HILL;
            case "showdown" -> Track.SHOWDOWN;
            case "rush_arena", "rush_arena_supplies", "rush_arena_training", "rush_arena_reputation" -> Track.ARENA;
            case "rush_return" -> Track.RETURN;
            case "liberty_grind" -> Track.LIBERTY;
            default -> null;
        };
    }

    static Milestone byId(String id) {
        return MILESTONES.stream().filter(milestone -> milestone.id().equals(id)).findFirst().orElse(null);
    }

    public static String name(String id) {
        Milestone milestone = byId(id); return milestone == null ? "Crew Tour" : milestone.name();
    }

    static UUID receiptId(UUID owner, String id) {
        return UUID.nameUUIDFromBytes(("jetsetcraft:crew_tour:" + owner + ":" + id).getBytes(StandardCharsets.UTF_8));
    }

    static boolean validGang(String gang) {
        return gang != null && !gang.isBlank() && ResourceLocation.tryParse(gang) != null;
    }

    static CompoundTag snapshot(Progress progress, String gang, boolean blocked, long revision) {
        CompoundTag out = new CompoundTag(); out.putLong("Revision", revision); out.putBoolean("Blocked", blocked);
        out.putString("GangId", gang); out.putInt("Total", MILESTONES.size());
        int ready = 0, claimed = 0, baseTotal = 0, baseClaimed = 0, optionalClaimed = 0;
        ListTag milestones = new ListTag(), tracks = new ListTag();
        for (Track track : sources()) {
            CompoundTag row = new CompoundTag(); row.putString("Id", track.name()); row.putString("Name", track.label);
            row.putInt("Count", progress.count(track)); tracks.add(row);
        }
        out.put("Tracks", tracks); out.putInt("Results", progress.count(Track.RESULTS)); out.putInt("Modes", progress.count(Track.MODES));
        for (Milestone milestone : MILESTONES) {
            boolean collected = progress.claimed.contains(milestone.id()), complete = progress.complete(milestone);
            if (collected) claimed++;
            if (!milestone.optional()) { baseTotal++; if (collected) baseClaimed++; }
            else if (collected) optionalClaimed++;
            if (!collected && complete) ready++;
            CompoundTag row = new CompoundTag(); row.putString("Id", milestone.id()); row.putString("Name", milestone.name());
            row.putString("Description", milestone.description()); row.putBoolean("Optional", milestone.optional());
            row.putBoolean("Claimed", collected); row.putBoolean("Ready", complete && !collected);
            row.putBoolean("CanClaim", !blocked && !collected && complete && revision < Long.MAX_VALUE
                    && validGang(gang));
            row.putInt("Supplies", milestone.supplies()); row.putInt("CrewXp", milestone.crewXp());
            row.putInt("MemberXp", milestone.memberXp()); row.putInt("Reputation", milestone.reputation());
            StringBuilder summary = new StringBuilder(); ListTag requirements = new ListTag();
            for (Requirement requirement : milestone.requirements()) {
                int count = progress.count(requirement.track());
                CompoundTag goal = new CompoundTag(); goal.putString("Name", requirement.track().label);
                goal.putInt("Current", count); goal.putInt("Target", requirement.target());
                goal.putBoolean("Complete", count >= requirement.target()); requirements.add(goal);
                if (!summary.isEmpty()) summary.append(" · ");
                summary.append(requirement.track().label).append(" ").append(Math.min(count, requirement.target()))
                        .append("/").append(requirement.target());
            }
            row.put("Requirements", requirements); row.putString("ProgressText", summary.toString()); milestones.add(row);
        }
        out.put("Milestones", milestones); out.putInt("Claimed", claimed); out.putInt("Ready", ready);
        out.putInt("BaseTotal", baseTotal); out.putInt("BaseClaimed", baseClaimed);
        out.putInt("OptionalTotal", MILESTONES.size() - baseTotal); out.putInt("OptionalClaimed", optionalClaimed);
        return out;
    }

    /** Unknown, malformed or internally impossible progress remains quarantined by its world store. */
    static Progress load(CompoundTag tag) {
        if (!tag.contains("Schema", Tag.TAG_INT) || tag.getInt("Schema") != 1 || !tag.hasUUID("Owner")
                || !tag.contains("Revision", Tag.TAG_LONG) || tag.getLong("Revision") < 0
                || !tag.contains("LastGang", Tag.TAG_STRING) || !tag.contains("Counts", Tag.TAG_COMPOUND)
                || !tag.contains("Claimed", Tag.TAG_LIST)) return null;
        Progress progress = new Progress(tag.getUUID("Owner")); progress.revision = tag.getLong("Revision");
        progress.lastGang = tag.getString("LastGang");
        CompoundTag counts = tag.getCompound("Counts");
        if (counts.getAllKeys().size() != sources().length) return null;
        long resultCount = 0;
        for (Track source : sources()) {
            if (!counts.contains(source.name(), Tag.TAG_INT)) return null;
            int count = counts.getInt(source.name());
            if (count < 0 || count > COUNT_LIMIT) return null;
            progress.counts.put(source, count); resultCount += count;
        }
        if (resultCount > 0 && !validGang(progress.lastGang)
                || resultCount == 0 && !progress.lastGang.isEmpty()) return null;
        ListTag claimed = (ListTag) tag.get("Claimed");
        if (claimed.size() > MILESTONES.size() || !claimed.isEmpty() && claimed.getElementType() != Tag.TAG_STRING) return null;
        for (int i = 0; i < claimed.size(); i++) {
            Milestone milestone = byId(claimed.getString(i));
            if (milestone == null || !progress.complete(milestone) || !progress.claimed.add(milestone.id())) return null;
        }
        if (progress.revision < resultCount + claimed.size()) return null;
        return progress;
    }

    private static Track[] sources() { return new Track[]{Track.TAG, Track.HILL, Track.SHOWDOWN, Track.ARENA, Track.RETURN, Track.LIBERTY}; }
    private static Requirement need(Track track, int count) { return new Requirement(track, count); }
    private static Milestone milestone(String id, String name, String description, boolean optional,
                                       int supplies, int crewXp, int training, int reputation, Requirement... requirements) {
        return new Milestone(id, name, description, optional, supplies, crewXp, training, reputation, List.of(requirements));
    }
    private CrewTour() {}
}
