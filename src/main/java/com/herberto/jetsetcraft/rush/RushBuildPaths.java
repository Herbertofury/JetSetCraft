package com.herberto.jetsetcraft.rush;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;

import java.util.List;
import java.util.Locale;

/** Small authored build paths, not imported native gear or a second reward economy. */
public final class RushBuildPaths {
    public enum Evolution {
        NONE("No evolution", "Defeat a headliner to earn one evolution opportunity."),
        CRYO_SHATTER("Cryo shatter", "A primary tag against an already inked and chilled rival consumes both effects. "
                + "Every 3 active seconds at most, shatter hits up to 4 visible owned rivals for 10 base damage "
                + "within 6 blocks, scaled by area and capped at 12.", RushRun.Upgrade.VENOM, RushRun.Upgrade.CHILL),
        RAILSTORM("Railstorm", "A primary tag that spends a real 24-block Line battery charge sends a storm forward. "
                + "Up to 5 other visible owned rivals take 8 base damage in a lane 14 blocks long and 2 blocks wide "
                + "on each side; area scales these to caps of 24 and 4.", RushRun.Upgrade.BATTERY, RushRun.Upgrade.SHOCKWAVE),
        RADIANT_BARRIER("Radiant barrier", "Every sixth primary tag, unused Radiant groove healing restores up to "
                + "6 Beat ward. This works during its quiet delay, cannot exceed shield capacity and cannot bank overheal.",
                RushRun.Upgrade.RADIANCE, RushRun.Upgrade.WARD);

        private final String label;
        private final String description;
        private final List<RushRun.Upgrade> requirements;

        Evolution(String label, String description, RushRun.Upgrade... requirements) {
            this.label = label; this.description = description; this.requirements = List.of(requirements);
        }
        public String label() { return label; }
        public String description() { return description; }
        public List<RushRun.Upgrade> requirements() { return requirements; }
        public boolean ready(RushRun run) {
            return this != NONE && run != null && requirements.stream().allMatch(upgrade -> run.rank(upgrade) >= 3);
        }
    }

    public static CompoundTag snapshot(RushRun run) {
        CompoundTag result = new CompoundTag();
        result.putBoolean("Opportunity", run.hasBuildOpportunity());
        result.putString("Selected", run.selectedEvolution().name());
        result.putBoolean("Active", run.evolutionActive(run.selectedEvolution()));
        ListTag recipes = new ListTag();
        for (Evolution evolution : Evolution.values()) {
            if (evolution == Evolution.NONE) continue;
            CompoundTag recipe = new CompoundTag();
            recipe.putString("Id", evolution.name()); recipe.putString("Name", evolution.label());
            recipe.putString("Description", evolution.description());
            recipe.putBoolean("Ready", evolution.ready(run));
            recipe.putBoolean("Selected", run.selectedEvolution() == evolution);
            recipe.putBoolean("Active", run.evolutionActive(evolution));
            recipe.putLong("ProcCount", run.evolutionProcs(evolution));
            ListTag requirements = new ListTag();
            for (RushRun.Upgrade upgrade : evolution.requirements()) {
                CompoundTag requirement = new CompoundTag(); requirement.putString("Id", upgrade.name());
                requirement.putString("Name", upgrade.label()); requirement.putInt("Rank", run.rank(upgrade));
                requirement.putInt("RequiredRank", 3); requirements.add(requirement);
            }
            recipe.put("Requirements", requirements); recipes.add(recipe);
        }
        result.put("Recipes", recipes);
        return result;
    }

    public static CompoundTag draftSnapshot(RushRun run) {
        CompoundTag result = new CompoundTag();
        result.putInt("Rerolls", run.rerollsRemaining()); result.putInt("RerollMax", RushRun.MAX_REROLLS);
        result.putInt("Banishes", run.banishesRemaining()); result.putInt("BanishMax", RushRun.MAX_BANISHES);
        result.putBoolean("CanReroll", run.canReroll());
        ListTag banished = new ListTag();
        for (RushRun.Upgrade upgrade : RushRun.Upgrade.values()) if (run.banishedDraftUpgrades().contains(upgrade)) {
            CompoundTag row = new CompoundTag(); row.putString("Id", upgrade.name()); row.putString("Name", upgrade.label());
            banished.add(row);
        }
        result.put("Banished", banished); return result;
    }

    /** The UI displays a server-calculated change; temporary buffs are not mistaken for permanent ranks. */
    public static String nextRankDescription(RushRun run, RushRun.Upgrade upgrade) {
        if (upgrade == null) return "Unavailable upgrade";
        if (upgrade.repeatable()) return switch (upgrade) {
            case REPAIR -> "Recover up to " + number(run.maxVitality() * .5) + " run vitality now.";
            case CACHE -> "Bank " + Math.min(1_000_000L, 500L * Math.min(2000L, run.chapter())) + " run score now.";
            case SURGE -> "Refresh 15 active seconds of 1.25x tag damage; duration does not stack.";
            default -> upgrade.description();
        };
        int rank = run.rank(upgrade), next = Math.min(upgrade.maxRank(), rank + 1);
        if (rank >= upgrade.maxRank()) return "Maximum rank: " + rankEffect(upgrade, rank);
        String change = (upgrade.artifact() && rank == 0 ? "Acquire: " : rankEffect(upgrade, rank) + " → ")
                + rankEffect(upgrade, next);
        if (upgrade == RushRun.Upgrade.VITALITY) change += "; recover the 12 added vitality now";
        if (upgrade == RushRun.Upgrade.WARD) change += "; fill only the added shield capacity";
        return change + ".";
    }

    private static String rankEffect(RushRun.Upgrade upgrade, int rank) {
        return switch (upgrade) {
            case FORCE -> number(1 + rank * .15) + "x base tag damage";
            case CADENCE -> number(Math.max(.25, 1 - rank * .07)) + "x base firing interval";
            case SPLASH -> number(1.5 + rank * .3) + "-block base splash";
            case PICKUP -> number(3 + rank * 1.5) + "-block ordinary pickup reach";
            case VITALITY -> (100 + rank * 12) + " maximum vitality";
            case RECOVERY -> number(rank * .35) + " vitality per active second";
            case FLOW -> number(1 + rank * .1) + "x mode style rewards";
            case PRECISION -> (5 + rank * 5) + "% primary critical chance";
            case CRITICAL -> number(1.5 + rank * .25) + "x critical damage";
            case AMPLITUDE -> number(1 + rank * .12) + "x paint and artifact area";
            case RESONANCE -> number(Math.max(.5, 1 - rank * .08)) + "x artifact intervals";
            case CHAIN -> rank + " extra targets, " + (2 + rank) + " base damage, " + (4 + rank) + "-block base radius";
            case FIREWORK -> (5 + rank * 2) + " base burst damage / sixth tag, " + number(3 + rank * .5) + "-block base radius";
            case WIND -> (4 + rank) + "-block base horn radius, " + number((15 + rank * 5) / 20.0) + "s directed stagger";
            case SHOCKWAVE -> (6 + rank * 3) + " base moving-wave damage, " + (6 + rank) + "-block base reach";
            case TEMPO -> (4 + rank) + "s tempo after every eighth defeat";
            case SOUL -> (6 + rank * 3) + " vitality per five healing souls";
            case SPELL_RELAY -> (6 + rank * 3) + " base Firebolt damage / " + number(Math.max(40, 150 - rank * 20) / 20.0) + "s base interval";
            case BATTERY -> number(1.35 + rank * .15) + "x charged primary tag after 24 ridden blocks";
            case AIR_BREAK -> (3 + rank * 2) + " base landing damage, " + number(3 + rank * .5) + "-block base radius";
            case EXPOSE -> number(1.20 + rank * .05) + "x exposed-target damage for " + number((40 + rank * 10) / 20.0) + "s";
            case VENOM -> (1 + rank) + " base damage / second for " + (3 + rank) + " active seconds";
            case CHILL -> number((.8 - rank * .05) * 100) + "% directed steering, " + number(1 + rank * .15)
                    + "x directed attack interval for " + number((40 + rank * 10) / 20.0) + "s";
            case MULTISHOT -> (1 + rank / 2) + " extra forward targets / fourth tag, " + (2 + rank) + " base damage";
            case RADIANCE -> (2 + rank * 2) + " vitality / sixth primary tag";
            case WARD -> (8 + rank * 4) + " ward capacity, " + (2 + rank) + "/second after 6 quiet active seconds";
            case SOUL_BURST -> (4 + rank * 3) + " base burst damage / five offensive souls, " + number(4 + rank * .5) + "-block base radius";
            default -> upgrade.description();
        };
    }

    private static String number(double value) {
        return String.format(Locale.ROOT, "%.2f", value).replaceAll("0+$", "").replaceAll("\\.$", "");
    }
    private RushBuildPaths() {}
}
