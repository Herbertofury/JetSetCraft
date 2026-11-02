package com.herberto.jetsetcraft.liberty;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Random;
import java.util.Set;

import com.herberto.jetsetcraft.liberty.LibertyMissionCatalog.*;

/** Versioned, deterministic expedition geometry; no chunks, entities, global random or player data are touched. */
public record LibertyMissionPlan(int schema, long seed, String theaterId, Faction faction, MissionTemplate template,
                                 LibertyDifficulty difficulty, int durationMinutes, int arenaRadius,
                                 List<Stage> stages, List<Objective> optionalObjectives, List<Stronghold> strongholds,
                                 Point drop, Point extraction) {
    public static final int SCHEMA = 1;
    public static final List<String> THEATER_IDS = List.of("neon_docks", "overpass_uprising", "blackout_borough",
            "neon_frontier", "iron_horizon", "prism_frontier");
    public record Point(int x, int z) {
        public Point offset(int dx, int dz) { return new Point(x + dx, z + dz); }
        public long distanceSquared(Point other) { long dx = (long) x - other.x, dz = (long) z - other.z; return dx * dx + dz * dz; }
    }
    public record Objective(String id, int stage, ObjectiveKind kind, String name, String description,
                            Point center, int target, int durationSeconds, int radius, List<Point> checkpoints, boolean optional) {
        public Objective {
            checkpoints = List.copyOf(checkpoints);
            if (id == null || !id.matches("[a-z0-9_]{1,96}") || kind == null || name == null || description == null || center == null
                    || target < 1 || target > 128 || durationSeconds < 0 || durationSeconds > 1800
                    || radius < 1 || radius > 64 || (optional ? stage != -1 : stage < 0)
                    || ((kind == ObjectiveKind.COURIER || kind == ObjectiveKind.ESCORT) ? checkpoints.size() != target : !checkpoints.isEmpty())
                    || ((kind == ObjectiveKind.SCAN || kind == ObjectiveKind.RELAY_HOLD || kind == ObjectiveKind.DEFEND) && durationSeconds < 1))
                throw new IllegalArgumentException("Invalid objective plan");
        }
    }
    public record Stage(int index, String name, List<Objective> objectives) {
        public Stage { objectives = List.copyOf(objectives); if (index < 0 || name == null || objectives.isEmpty()) throw new IllegalArgumentException("Invalid stage"); }
    }
    public record Stronghold(String id, BaseKind kind, Point center, int tier, int cores, String objectiveId,
                             boolean required, int threatReduction) {
        public Stronghold {
            if (id == null || kind == null || center == null || tier < 1 || tier > 4 || cores < 1 || cores > 8
                    || objectiveId == null || threatReduction < 0 || threatReduction > 100)
                throw new IllegalArgumentException("Invalid stronghold plan");
        }
    }
    public LibertyMissionPlan {
        stages = List.copyOf(stages); optionalObjectives = List.copyOf(optionalObjectives); strongholds = List.copyOf(strongholds);
        Objects.requireNonNull(faction); Objects.requireNonNull(template); Objects.requireNonNull(difficulty);
        Objects.requireNonNull(drop); Objects.requireNonNull(extraction);
        LibertyDifficulty.checkMinutes(durationMinutes);
        if (schema != SCHEMA || !validSectorId(theaterId) || !template.supports(faction)
                || (arenaRadius != 512 && arenaRadius != 640 && arenaRadius != 768) || stages.isEmpty())
            throw new IllegalArgumentException("Invalid expedition plan");
        Set<String> objectiveIds = new HashSet<>();
        List<Objective> objectives = new ArrayList<>();
        for (int i = 0; i < stages.size(); i++) {
            Stage stage = stages.get(i);
            if (stage.index() != i) throw new IllegalArgumentException("Mission stages must be consecutive");
            for (Objective objective : stage.objectives()) {
                if (objective.optional() || objective.stage() != i) throw new IllegalArgumentException("Objective is in the wrong primary stage");
                objectives.add(objective);
            }
        }
        for (Objective objective : optionalObjectives) {
            if (!objective.optional()) throw new IllegalArgumentException("Optional objective is incorrectly mandatory");
            objectives.add(objective);
        }
        for (Objective objective : objectives) {
            if (!objectiveIds.add(objective.id()) || !within(objective.center(), arenaRadius - objective.radius())
                    || objective.checkpoints().stream().anyMatch(p -> !within(p, arenaRadius - 8)))
                throw new IllegalArgumentException("Duplicate or out-of-arena objective");
        }
        Set<String> baseIds = new HashSet<>(), basedObjectives = new HashSet<>();
        for (Stronghold base : strongholds) {
            Objective objective = objectives.stream().filter(o -> o.id().equals(base.objectiveId())).findFirst().orElse(null);
            if (!baseIds.add(base.id()) || !basedObjectives.add(base.objectiveId()) || objective == null
                    || objective.kind() != ObjectiveKind.DEMOLITION || !objective.center().equals(base.center())
                    || objective.target() != base.cores() || base.required() == objective.optional() || base.kind() != faction.baseKind())
                throw new IllegalArgumentException("Stronghold does not match its demolition objective");
        }
        if (objectives.stream().anyMatch(o -> o.kind() == ObjectiveKind.DEMOLITION && !basedObjectives.contains(o.id()))
                || !within(drop, arenaRadius - 8) || !within(extraction, arenaRadius - 8) || drop.equals(extraction))
            throw new IllegalArgumentException("Missing physical core plan or invalid landing sites");
    }
    public List<Objective> primaryObjectives() { return stages.stream().flatMap(s -> s.objectives().stream()).toList(); }
    public List<Objective> allObjectives() { var all = new ArrayList<>(primaryObjectives()); all.addAll(optionalObjectives); return List.copyOf(all); }
    public int totalTicks() { return durationMinutes * 1200; }
    public static LibertyMissionPlan generate(long seed, String theaterId, Faction faction, String templateId, int level, int minutes) {
        LibertyDifficulty difficulty = LibertyDifficulty.of(level); LibertyDifficulty.checkMinutes(minutes);
        if (!validSectorId(theaterId) || faction == null) throw new IllegalArgumentException("Invalid sector or faction");
        long salt = mix(seed, theaterId + "/" + faction.id() + "/" + level + "/" + minutes);
        Random random = new Random(salt);
        MissionTemplate template;
        if (templateId == null || templateId.isBlank() || templateId.equals("random")) {
            List<MissionTemplate> choices = LibertyMissionCatalog.templates(faction); template = choices.get(random.nextInt(choices.size()));
        } else template = LibertyMissionCatalog.template(templateId);
        if (template == null || !template.supports(faction)) throw new IllegalArgumentException("Mission is unavailable for this faction");
        random = new Random(mix(salt, template.id()));
        int radius = minutes == 30 || level >= 8 ? 768 : minutes == 25 || level >= 4 ? 640 : 512;
        Point drop = new Point(0, radius - 64), extraction = new Point(0, -radius + 64);
        List<Point> positions = positions(radius, drop, extraction, random); int cursor = 0;
        List<Stage> stages = new ArrayList<>(); List<Objective> optional = new ArrayList<>(); List<Stronghold> bases = new ArrayList<>();
        int stageIndex = 0;
        for (StageSpec spec : template.stages()) {
            List<Objective> objectives = new ArrayList<>();
            for (GoalSpec goal : spec.goals()) for (int i = 0; i < goal.sites(); i++) {
                String id = "stage_" + stageIndex + "_" + goal.id() + "_" + i;
                Objective objective = objective(id, stageIndex, goal, positions.get(cursor++), difficulty, minutes, random, false);
                objectives.add(objective);
                if (objective.kind() == ObjectiveKind.DEMOLITION) bases.add(base(objective, faction, difficulty, true, false));
            }
            stages.add(new Stage(stageIndex++, spec.name(), objectives));
        }
        List<GoalSpec> optionalPool = new ArrayList<>(LibertyMissionCatalog.optionalGoals()); shuffle(optionalPool, random);
        for (int i = 0; i < difficulty.optionalCount(); i++) {
            GoalSpec goal = optionalPool.get(i % optionalPool.size());
            optional.add(objective("optional_" + i + "_" + goal.id(), -1, goal, positions.get(cursor++), difficulty, minutes, random, true));
        }
        for (int i = 0; i < difficulty.outpostCount(); i++) {
            boolean giant = level == 10 && i == 0;
            GoalSpec goal = new GoalSpec("outpost", ObjectiveKind.DEMOLITION,
                    "Optional: " + (giant ? "giant " : "") + switch (faction.baseKind()) { case FABRICATOR -> "Automaton foundry"; case BUG_HOLE -> "Terminid nest"; case WARP_SHIP -> "Illuminate warp ship"; },
                    "Destroy its real marked cores to reduce local reinforcement pressure and earn research.", 1, giant ? 6 : 2 + level / 4, 0);
            Objective objective = objective("outpost_" + i, -1, goal, positions.get(cursor++), difficulty, minutes, random, true);
            optional.add(objective); bases.add(base(objective, faction, difficulty, false, giant));
        }
        return new LibertyMissionPlan(SCHEMA, seed, theaterId, faction, template, difficulty, minutes, radius, stages, optional, bases, drop, extraction);
    }
    private static Objective objective(String id, int stage, GoalSpec spec, Point center, LibertyDifficulty difficulty, int minutes, Random random, boolean optional) {
        double factor = (.85 + difficulty.level() * .055) * minutes / 25.0;
        int target = switch (spec.kind()) {
            case SCAN, RELAY_HOLD, DEFEND -> 1;
            case DEMOLITION -> Math.max(2, Math.min(8, (int) Math.round(spec.target() * factor)));
            case COURIER, ESCORT -> Math.max(3, Math.min(8, (int) Math.round(spec.target() * factor)));
            case SWEEP -> spec.target() == 1 ? 1 : Math.max(12, Math.min(96, (int) Math.round(spec.target() * factor)));
            default -> Math.max(12, Math.min(96, (int) Math.round(spec.target() * factor)));
        };
        int seconds = spec.seconds() == 0 ? 0 : Math.max(8, (int) Math.round(spec.seconds() * minutes / 25.0 * (1 + (difficulty.level() - 1) * .025)));
        int radius = spec.kind() == ObjectiveKind.TERRITORY ? 28 : spec.kind() == ObjectiveKind.SWEEP ? 40 : 24;
        List<Point> checkpoints = new ArrayList<>();
        if (spec.kind() == ObjectiveKind.COURIER || spec.kind() == ObjectiveKind.ESCORT) {
            double phase = random.nextDouble() * Math.PI * 2;
            for (int i = 0; i < target; i++) {
                double angle = phase + i * Math.PI * 2 / target;
                checkpoints.add(center.offset((int) Math.round(Math.cos(angle) * 44), (int) Math.round(Math.sin(angle) * 44)));
            }
        }
        return new Objective(id, stage, spec.kind(), spec.name(), spec.description(), center, target, seconds, radius, checkpoints, optional);
    }
    private static Stronghold base(Objective objective, Faction faction, LibertyDifficulty difficulty, boolean required, boolean giant) {
        int tier = giant ? 4 : difficulty.level() >= 8 ? 3 : difficulty.level() >= 4 ? 2 : 1;
        return new Stronghold("base_" + objective.id(), faction.baseKind(), objective.center(), tier, objective.target(), objective.id(), required, 5 + tier * 5);
    }
    private static List<Point> positions(int radius, Point drop, Point extraction, Random random) {
        List<Point> candidates = new ArrayList<>();
        // Cell pitch128 with +/-16 jitter guarantees at least96 blocks between site centers.
        for (int x = -radius + 96; x <= radius - 96; x += 128) for (int z = -radius + 96; z <= radius - 96; z += 128) {
            Point point = new Point(x + random.nextInt(33) - 16, z + random.nextInt(33) - 16);
            if (point.distanceSquared(drop) >= 128L * 128 && point.distanceSquared(extraction) >= 128L * 128) candidates.add(point);
        }
        shuffle(candidates, random);
        if (candidates.size() < 24) throw new IllegalStateException("Insufficient bounded mission sites");
        return candidates;
    }
    private static <T> void shuffle(List<T> list, Random random) {
        for (int i = list.size() - 1; i > 0; i--) { int j = random.nextInt(i + 1); T value = list.get(i); list.set(i, list.get(j)); list.set(j, value); }
    }
    private static long mix(long seed, String text) {
        long value = seed ^ 0x9e3779b97f4a7c15L;
        for (int i = 0; i < text.length(); i++) { value ^= text.charAt(i); value *= 0x100000001b3L; value ^= value >>> 29; }
        return value;
    }
    /** The campaign registry owns sector admission; this model only validates the bounded stable identifier. */
    public static boolean validSectorId(String id) { return id != null && id.matches("[a-z0-9_]{1,64}"); }
    private static boolean within(Point point, int radius) { return point != null && Math.abs((long) point.x()) <= radius && Math.abs((long) point.z()) <= radius; }
}
