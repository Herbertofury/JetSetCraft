package com.herberto.jetsetcraft.crew;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceLocation;

import java.util.Objects;
import java.util.UUID;

/** An immutable account observation, written with the award. Display history never authorizes a payout. */
public record CrewRewardReceipt(UUID id, String source, String gang, long earnedAt,
                                int supplies, int crewXp, int memberXp, int reputation,
                                UUID memberId, String memberName, int memberLevelBefore, int memberLevelAfter,
                                int crewLevelBefore, int crewLevelAfter, Training training, boolean capped) {
    public enum Training { BANKED, APPLIED, BUSY, NO_FOCUS }

    public CrewRewardReceipt {
        Objects.requireNonNull(id);
        Objects.requireNonNull(training);
        source = clean(source, 80);
        gang = clean(gang, 256);
        memberName = clean(memberName, 36);
        if (ResourceLocation.tryParse(gang) == null || earnedAt < 0 || supplies < 0 || supplies > CrewProfile.SUPPLY_LIMIT
                || crewXp < 0 || crewXp > 1_000_000_000 || memberXp < 0 || memberXp > 1_000_000_000
                || reputation < 0 || reputation > 10_000 || memberLevelBefore < 0 || memberLevelAfter < memberLevelBefore
                || memberLevelAfter > 10 || crewLevelBefore < 1 || crewLevelAfter < crewLevelBefore || crewLevelAfter > 20)
            throw new IllegalArgumentException("Invalid crew reward observation");
    }

    public String name() {
        if (source.startsWith("mission:")) return CrewMissionCatalog.byId(source.substring(8))
                .map(mission -> mission.name() + " claimed").orElse("Mission claimed");
        return switch (source) {
            case "tag" -> "Tag completed";
            case "king_of_hill" -> "King of the Hill completed";
            case "showdown" -> "Crew Showdown completed";
            case "rush_arena" -> "Arena cleared";
            case "rush_arena_supplies" -> "Supplies arena cleared";
            case "rush_arena_training" -> "Training arena cleared";
            case "rush_arena_reputation" -> "Reputation arena cleared";
            case "rush_return" -> "Rival Rush banked";
            default -> "Crew progress earned";
        };
    }

    public boolean pending() { return training == Training.BANKED; }
    public boolean memberLeveledUp() { return memberId != null && memberLevelAfter > memberLevelBefore; }
    public String summary() { return delta(supplies) + " Supplies · " + delta(crewXp) + " crew XP · " + delta(reputation) + " reputation"; }
    public String trainingSummary() {
        return switch (training) {
            case BANKED -> delta(memberXp) + " training XP saved for your first home";
            case APPLIED -> delta(memberXp) + " training XP · " + memberName;
            case BUSY -> "0 training XP · " + memberName + " is away on a job";
            case NO_FOCUS -> "0 training XP · no training focus selected";
        };
    }
    public String levelSummary() {
        String member = memberLeveledUp() ? memberName + " reached level " + memberLevelAfter : "";
        String crew = crewLevelAfter > crewLevelBefore ? "Crew reached level " + crewLevelAfter : "";
        return member + (!member.isEmpty() && !crew.isEmpty() ? " · " : "") + crew;
    }

    public CompoundTag save() {
        CompoundTag tag = new CompoundTag(); tag.putInt("Schema", 1); tag.putUUID("Id", id);
        tag.putString("Source", source); tag.putString("GangId", gang); tag.putLong("EarnedAt", earnedAt);
        tag.putInt("Supplies", supplies); tag.putInt("CrewXp", crewXp); tag.putInt("MemberXp", memberXp); tag.putInt("Reputation", reputation);
        if (memberId != null) tag.putUUID("MemberId", memberId);
        tag.putString("MemberName", memberName); tag.putInt("MemberLevelBefore", memberLevelBefore); tag.putInt("MemberLevelAfter", memberLevelAfter);
        tag.putInt("CrewLevelBefore", crewLevelBefore); tag.putInt("CrewLevelAfter", crewLevelAfter);
        tag.putString("Training", training.name()); tag.putBoolean("Capped", capped);
        return tag;
    }

    public CompoundTag snapshot() {
        CompoundTag tag = save(); tag.putString("Name", name()); tag.putString("Summary", summary());
        tag.putString("TrainingSummary", trainingSummary()); tag.putString("LevelSummary", levelSummary());
        tag.putBoolean("Pending", pending()); return tag;
    }

    /** Malformed optional display rows are ignored without changing the authoritative balances or receipt guard. */
    public static CrewRewardReceipt load(CompoundTag tag) {
        if (tag == null || tag.getInt("Schema") != 1 || !tag.hasUUID("Id") || !tag.contains("EarnedAt", Tag.TAG_LONG)) return null;
        for (String key : new String[]{"Supplies", "CrewXp", "MemberXp", "Reputation", "MemberLevelBefore", "MemberLevelAfter", "CrewLevelBefore", "CrewLevelAfter"})
            if (!tag.contains(key, Tag.TAG_INT)) return null;
        try {
            return new CrewRewardReceipt(tag.getUUID("Id"), tag.getString("Source"), tag.getString("GangId"), tag.getLong("EarnedAt"),
                    tag.getInt("Supplies"), tag.getInt("CrewXp"), tag.getInt("MemberXp"), tag.getInt("Reputation"),
                    tag.hasUUID("MemberId") ? tag.getUUID("MemberId") : null, tag.getString("MemberName"),
                    tag.getInt("MemberLevelBefore"), tag.getInt("MemberLevelAfter"), tag.getInt("CrewLevelBefore"), tag.getInt("CrewLevelAfter"),
                    Training.valueOf(tag.getString("Training")), tag.getBoolean("Capped"));
        } catch (IllegalArgumentException error) { return null; }
    }

    private static String delta(int value) { return value == 0 ? "0" : "+" + value; }
    private static String clean(String value, int length) {
        String safe = value == null ? "" : value.replaceAll("[\\p{Cntrl}§]", "").trim();
        return safe.substring(0, Math.min(length, safe.length()));
    }
}
