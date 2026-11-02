package com.herberto.jetsetcraft.rush;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;

import java.util.Locale;

/** Bounded, server-calculated service previews. The run owns every budget and mutation. */
public final class RushPitStop {
    public enum Service {
        TUNE("Tune", "Raise one owned stat or artifact by one rank, within its existing cap."),
        PATCH("Patch", "Recover up to 40% maximum vitality and 50% equipped ward capacity."),
        REWIND("Rewind", "Restore one reroll and one banish, capped at three and two. Draft exclusions stay."),
        REMIX("Remix", "Replace one artifact family with another, keeping its exact rank and three-slot limit.");

        private final String label;
        private final String description;
        Service(String label, String description) { this.label = label; this.description = description; }
        public String label() { return label; }
        public String description() { return description; }
    }

    /** A caller can inspect or copy a result but cannot mutate the run's last accepted service. */
    public static final class Result {
        private final CompoundTag data;
        private Result(CompoundTag data) { this.data = data.copy(); }
        public boolean accepted() { return data.getBoolean("Accepted"); }
        public String summary() { return data.getString("Summary"); }
        public CompoundTag snapshot() { return data.copy(); }
        static Result empty() { return new Result(new CompoundTag()); }
        static Result rejected(Service service, String reason) {
            CompoundTag result = new CompoundTag(); result.putBoolean("Accepted", false);
            if (service != null) { result.putString("Service", service.name()); result.putString("Name", service.label()); }
            result.putString("Summary", reason); return new Result(result);
        }
    }

    record Before(int rank, double vitality, double maxVitality, double ward, double maxWard, int rerolls, int banishes) {}

    static Before observe(RushRun run, RushRun.Upgrade upgrade) {
        return new Before(upgrade == null ? 0 : run.rank(upgrade), run.vitality(), run.maxVitality(), run.ward(), run.maxWard(),
                run.rerollsRemaining(), run.banishesRemaining());
    }

    static String unavailableReason(RushRun run, Service service) {
        if (service == null) return "Unknown Pit Stop service.";
        if (!run.circuit()) return "Pit Stops are earned in Arena circuits.";
        if (run.isFinished()) return "The run has ended.";
        if (!run.pitStopAvailable()) return "Clear an arena to earn another Pit Stop service.";
        if (run.pitStopUsed(service)) return service.label() + " was already used at this stop.";
        return switch (service) {
            case TUNE -> tuneCount(run) > 0 ? "" : "No owned upgrade has another available rank.";
            case PATCH -> patchVitality(run) > 0 || patchWard(run) > 0 ? ""
                    : run.maxWard() > 0 ? "Vitality and ward are already full." : "Vitality is full; no ward is equipped.";
            case REWIND -> run.rerollsRemaining() < RushRun.MAX_REROLLS || run.banishesRemaining() < RushRun.MAX_BANISHES
                    ? "" : "Rerolls and banishes are already at their caps.";
            case REMIX -> run.artifactCount() > 0 ? "" : "Equip an artifact family before remixing.";
        };
    }

    /** Session phase and ownership are deliberately added by the manager, not inferred from a display tag. */
    public static CompoundTag snapshot(RushRun run) {
        CompoundTag result = new CompoundTag();
        result.putBoolean("Available", run.pitStopAvailable()); result.putInt("Credits", run.pitStopCredits());
        result.putInt("Earned", run.pitStopEarned()); result.putInt("Version", run.offerVersion());
        if (run.pitStopReceipt() != null) result.putUUID("Receipt", run.pitStopReceipt());
        ListTag options = new ListTag();
        for (Service service : Service.values()) {
            CompoundTag option = new CompoundTag(); String reason = unavailableReason(run, service);
            option.putString("Id", service.name()); option.putString("Name", service.label());
            option.putString("Description", service.description()); option.putBoolean("Enabled", reason.isEmpty());
            option.putString("Reason", reason); option.putString("Preview", preview(run, service)); options.add(option);
        }
        result.put("Options", options);
        ListTag choices = new ListTag();
        for (RushRun.Upgrade upgrade : RushRun.Upgrade.values()) if (run.canTune(upgrade)) {
            CompoundTag choice = new CompoundTag(); choice.putString("Id", upgrade.name()); choice.putString("Name", upgrade.label());
            choice.putInt("Rank", run.rank(upgrade)); choice.putInt("NextRank", run.rank(upgrade) + 1);
            choice.putString("NextDescription", RushBuildPaths.nextRankDescription(run, upgrade)); choices.add(choice);
        }
        result.put("TuneChoices", choices); result.put("LastResult", run.lastPitStopResult().snapshot());
        return result;
    }

    private static String preview(RushRun run, Service service) {
        return switch (service) {
            case TUNE -> tuneCount(run) + " owned upgrades can gain one rank; each target shows its exact change.";
            case PATCH -> "Vitality " + change(run.vitality(), run.vitality() + patchVitality(run))
                    + (run.maxWard() > 0 ? "; ward " + change(run.ward(), run.ward() + patchWard(run)) : "; no ward equipped") + ".";
            case REWIND -> "Rerolls " + change(run.rerollsRemaining(), Math.min(RushRun.MAX_REROLLS, run.rerollsRemaining() + 1))
                    + "; banishes " + change(run.banishesRemaining(), Math.min(RushRun.MAX_BANISHES, run.banishesRemaining() + 1))
                    + ". Excluded draft upgrades stay excluded.";
            case REMIX -> "Choose an owned artifact and an available replacement; keep its exact rank. Removed-family charge is cleared.";
        };
    }

    static Result accepted(RushRun run, Service service, RushRun.Upgrade target, RushRun.Upgrade source, Before before, String effect) {
        CompoundTag result = new CompoundTag(); result.putBoolean("Accepted", true);
        result.putString("Service", service.name()); result.putString("Name", service.label());
        result.putUUID("Receipt", run.pitStopReceipt()); result.putInt("Version", run.offerVersion());
        result.putInt("RemainingCredits", run.pitStopCredits());
        if (target != null) {
            result.putString("Upgrade", target.name()); result.putInt("RankBefore", source == null ? before.rank() : 0);
            result.putInt("RankAfter", run.rank(target));
        }
        if (source != null) { result.putString("Source", source.name()); result.putInt("SourceRankBefore", before.rank()); }
        result.putDouble("VitalityBefore", before.vitality()); result.putDouble("VitalityAfter", run.vitality());
        result.putDouble("MaxVitalityBefore", before.maxVitality()); result.putDouble("MaxVitalityAfter", run.maxVitality());
        result.putDouble("WardBefore", before.ward()); result.putDouble("WardAfter", run.ward());
        result.putDouble("MaxWardBefore", before.maxWard()); result.putDouble("MaxWardAfter", run.maxWard());
        result.putDouble("VitalityRestored", Math.max(0, run.vitality() - before.vitality()));
        // Added/remixed shield capacity is visible above but does not masquerade as repaired damage.
        result.putDouble("WardRestored", service == Service.PATCH ? Math.max(0, run.ward() - before.ward()) : 0);
        result.putInt("RerollsBefore", before.rerolls()); result.putInt("RerollsAfter", run.rerollsRemaining());
        result.putInt("BanishesBefore", before.banishes()); result.putInt("BanishesAfter", run.banishesRemaining());
        String summary = switch (service) {
            case TUNE -> "Tuned " + target.label() + ": rank " + before.rank() + " → " + run.rank(target) + ". " + effect;
            case PATCH -> "Patched vitality " + change(before.vitality(), run.vitality()) + "; ward " + change(before.ward(), run.ward()) + ".";
            case REWIND -> "Rewound rerolls " + change(before.rerolls(), run.rerollsRemaining())
                    + "; banishes " + change(before.banishes(), run.banishesRemaining()) + ". Draft exclusions kept.";
            case REMIX -> "Remixed " + source.label() + " rank " + before.rank() + " → " + target.label() + " rank " + run.rank(target) + ".";
        };
        result.putString("Summary", summary); return new Result(result);
    }

    private static int tuneCount(RushRun run) {
        int count = 0; for (RushRun.Upgrade upgrade : RushRun.Upgrade.values()) if (run.canTune(upgrade)) count++;
        return count;
    }
    private static double patchVitality(RushRun run) { return Math.min(run.maxVitality() * .4D, Math.max(0, run.maxVitality() - run.vitality())); }
    private static double patchWard(RushRun run) { return Math.min(run.maxWard() * .5D, Math.max(0, run.maxWard() - run.ward())); }
    private static String change(double before, double after) { return number(before) + " → " + number(after) + " (+" + number(after - before) + ")"; }
    private static String number(double value) {
        if (value > 0 && value < .005D) return "<0.01";
        return String.format(Locale.ROOT, "%.2f", value).replaceAll("0+$", "").replaceAll("\\.$", "");
    }
    private RushPitStop() {}
}
