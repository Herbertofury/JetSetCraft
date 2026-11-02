package com.herberto.jetsetcraft.rush;

import com.herberto.jetsetcraft.crew.CrewRewardReceipt;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceLocation;

import java.util.EnumSet;

/** One bounded display observation. It never authorizes a reward or restores a temporary run. */
public final class RushDebrief {
    public static final String KEY = "jetsetcraft:RivalRushDebrief";
    private static final long COUNTER_LIMIT = Long.MAX_VALUE / 4;
    private static final double METRIC_LIMIT = 1_000_000_000_000D;

    public enum Outcome {
        BANKED("Banked and returned"),
        DEFEATED("Run vitality exhausted"),
        HEADLINER_CLEAR("Headliner cleared"),
        INTERRUPTED("Interrupted and restored");
        private final String label;
        Outcome(String label) { this.label = label; }
        public String label() { return label; }
    }

    /** Capture after successful restoration/settlement, while the finished run's immutable identity remains available. */
    public static CompoundTag capture(RushRun run, Outcome outcome, long cards, CrewRewardReceipt acceptedReturnReward) {
        return capture(run, outcome, cards, acceptedReturnReward, "");
    }

    public static CompoundTag capture(RushRun run, Outcome outcome, long cards, CrewRewardReceipt acceptedReturnReward, String reason) {
        if (run == null || outcome == null) return new CompoundTag();
        CompoundTag result = new CompoundTag();
        result.putInt("Schema", 1); result.putUUID("Run", run.id()); result.putUUID("Owner", run.owner());
        result.putString("GangId", run.gangId()); result.putString("Outcome", outcome.name());
        result.putString("OutcomeLabel", outcome.label());
        result.putString("Reason", boundedReason(reason));
        result.putLong("Ticks", run.elapsedTicks()); result.putInt("Level", run.level());
        result.putLong("Score", run.score()); result.putLong("Defeats", run.defeats()); result.putLong("Cards", cards);
        result.putLong("ArenaClears", run.arenaClears()); result.putLong("Headliners", run.headliners());
        result.putBoolean("Circuit", run.circuit()); result.putBoolean("Endless", run.endless());
        result.putDouble("PrimaryDamage", run.primaryDamage()); result.putDouble("SecondaryDamage", run.secondaryDamage());
        result.putDouble("DamageTaken", run.damageTaken()); result.putDouble("VitalityRestored", run.vitalityRestored());
        result.putDouble("WardAbsorbed", run.wardAbsorbed()); result.putDouble("WardRestored", run.wardRestored());
        result.putLong("RerollsUsed", run.rerollsUsed()); result.putLong("BanishesUsed", run.banishesUsed());
        ListTag artifacts = new ListTag(), stats = new ListTag();
        for (RushRun.Upgrade upgrade : RushRun.Upgrade.values()) if (!upgrade.repeatable() && run.rank(upgrade) > 0) {
            (upgrade.artifact() ? artifacts : stats).add(upgradeRow(upgrade, run.rank(upgrade)));
        }
        result.put("Artifacts", artifacts); result.put("Stats", stats);
        RushBuildPaths.Evolution selected = run.selectedEvolution();
        CompoundTag evolution = new CompoundTag(); evolution.putString("Id", selected.name());
        evolution.putString("Name", selected.label()); evolution.putString("Description", selected.description());
        evolution.putBoolean("Active", run.evolutionActive(selected)); evolution.putLong("Procs", run.evolutionProcs(selected));
        result.put("Evolution", evolution);
        if (acceptedReturnReward != null && acceptedReturnReward.id().equals(run.id())
                && acceptedReturnReward.source().equals("rush_return") && acceptedReturnReward.gang().equals(run.gangId())) {
            result.putString("ReturnRewardStatus", "accepted"); result.put("ReturnReward", acceptedReturnReward.snapshot());
        } else result.putString("ReturnRewardStatus", "not_recorded");
        return sanitize(result);
    }

    /** Read into a fresh bounded display tag. Unknown schemas and invalid identities remain unavailable. */
    public static CompoundTag sanitize(CompoundTag source) {
        if (source == null || source.getInt("Schema") != 1 || !source.hasUUID("Run") || !source.hasUUID("Owner"))
            return new CompoundTag();
        String gang = source.getString("GangId");
        if (gang.length() > 256 || ResourceLocation.tryParse(gang) == null) return new CompoundTag();
        Outcome outcome;
        try { outcome = Outcome.valueOf(source.getString("Outcome")); }
        catch (IllegalArgumentException error) { return new CompoundTag(); }
        ListTag sourceArtifacts = source.getList("Artifacts", Tag.TAG_COMPOUND), sourceStats = source.getList("Stats", Tag.TAG_COMPOUND);
        if (sourceArtifacts.size() > 3 || sourceStats.size() > 11) return new CompoundTag();
        CompoundTag result = new CompoundTag();
        result.putInt("Schema", 1); result.putUUID("Run", source.getUUID("Run")); result.putUUID("Owner", source.getUUID("Owner"));
        result.putString("GangId", gang); result.putString("Outcome", outcome.name()); result.putString("OutcomeLabel", outcome.label());
        result.putString("Reason", boundedReason(source.getString("Reason")));
        for (String key : new String[]{"Ticks", "Score", "Defeats", "Cards", "ArenaClears", "Headliners", "RerollsUsed", "BanishesUsed"})
            result.putLong(key, Math.max(0, Math.min(COUNTER_LIMIT, source.getLong(key))));
        result.putInt("Level", Math.max(1, source.getInt("Level")));
        result.putBoolean("Circuit", source.getBoolean("Circuit")); result.putBoolean("Endless", source.getBoolean("Endless"));
        for (String key : new String[]{"PrimaryDamage", "SecondaryDamage", "DamageTaken", "VitalityRestored", "WardAbsorbed", "WardRestored"}) {
            double value = source.getDouble(key);
            result.putDouble(key, Double.isFinite(value) ? Math.max(0, Math.min(METRIC_LIMIT, value)) : 0);
        }
        ListTag artifacts = sanitizeUpgrades(sourceArtifacts, true), stats = sanitizeUpgrades(sourceStats, false);
        result.put("Artifacts", artifacts); result.put("Stats", stats);
        CompoundTag evolutionSource = source.getCompound("Evolution"), evolution = new CompoundTag();
        RushBuildPaths.Evolution selected;
        try { selected = RushBuildPaths.Evolution.valueOf(evolutionSource.getString("Id")); }
        catch (IllegalArgumentException error) { selected = RushBuildPaths.Evolution.NONE; }
        boolean active = selected != RushBuildPaths.Evolution.NONE;
        for (RushRun.Upgrade requirement : selected.requirements()) {
            boolean found = false;
            for (Tag raw : artifacts) {
                CompoundTag row = (CompoundTag) raw;
                if (row.getString("Id").equals(requirement.name()) && row.getInt("Rank") >= 3) found = true;
            }
            active &= found;
        }
        evolution.putString("Id", selected.name()); evolution.putString("Name", selected.label());
        evolution.putString("Description", selected.description()); evolution.putBoolean("Active", active);
        evolution.putLong("Procs", selected == RushBuildPaths.Evolution.NONE ? 0
                : Math.max(0, Math.min(COUNTER_LIMIT, evolutionSource.getLong("Procs"))));
        result.put("Evolution", evolution);
        CrewRewardReceipt reward = source.getString("ReturnRewardStatus").equals("accepted")
                ? CrewRewardReceipt.load(source.getCompound("ReturnReward")) : null;
        if (reward != null && reward.id().equals(result.getUUID("Run")) && reward.source().equals("rush_return")
                && reward.gang().equals(gang)) {
            result.putString("ReturnRewardStatus", "accepted"); result.put("ReturnReward", reward.snapshot());
        } else result.putString("ReturnRewardStatus", "not_recorded");
        return result;
    }

    private static ListTag sanitizeUpgrades(ListTag source, boolean artifacts) {
        ListTag result = new ListTag(); EnumSet<RushRun.Upgrade> seen = EnumSet.noneOf(RushRun.Upgrade.class);
        for (Tag raw : source) {
            CompoundTag row = (CompoundTag) raw;
            RushRun.Upgrade upgrade;
            try { upgrade = RushRun.Upgrade.valueOf(row.getString("Id")); }
            catch (IllegalArgumentException error) { continue; }
            int rank = row.getInt("Rank");
            if (upgrade.repeatable() || upgrade.artifact() != artifacts || rank < 1 || rank > upgrade.maxRank() || !seen.add(upgrade)) continue;
            result.add(upgradeRow(upgrade, rank));
        }
        return result;
    }

    private static CompoundTag upgradeRow(RushRun.Upgrade upgrade, int rank) {
        CompoundTag result = new CompoundTag(); result.putString("Id", upgrade.name()); result.putString("Name", upgrade.label());
        result.putString("Description", upgrade.description()); result.putInt("Rank", rank); result.putInt("MaxRank", upgrade.maxRank());
        return result;
    }

    private static String boundedReason(String value) {
        String clean = value == null ? "" : value.replaceAll("[\\p{Cntrl}§]", " ").trim();
        return clean.substring(0, Math.min(240, clean.length()));
    }
    private RushDebrief() {}
}
