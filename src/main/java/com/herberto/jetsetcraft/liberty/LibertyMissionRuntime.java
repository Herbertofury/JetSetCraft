package com.herberto.jetsetcraft.liberty;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.world.phys.Vec3;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import java.util.function.ToIntFunction;
import java.util.function.Predicate;

import static com.herberto.jetsetcraft.liberty.LibertyMissionCatalog.ObjectiveKind;

/**
 * Server-owned objective state, with no world access or entity/block mutations. Callers supply actual
 * successful graffiti, owned actor deaths, native core damage and server player positions. This class
 * never accepts a client score and never pays a persistent reward itself.
 */
public final class LibertyMissionRuntime {
    public final class Site {
        private final LibertyMissionPlan.Objective spec;
        private BlockPos center;
        private final List<BlockPos> checkpoints;
        private final Set<Long> cells = new HashSet<>();
        private final List<Core> cores = new ArrayList<>();
        private final int radius, cellSize;
        private int count, heldTicks;
        private boolean complete, activated, contested, relocated;
        private Vec3 escort;
        private UUID priorityTarget;

        private Site(LibertyMissionPlan.Objective spec) {
            this.spec = spec;
            this.center = map(spec.center());
            this.checkpoints = new ArrayList<>(spec.checkpoints().stream().map(LibertyMissionRuntime.this::map).toList());
            this.radius = home ? Math.max(spec.kind() == ObjectiveKind.TERRITORY ? 6
                    : spec.kind() == ObjectiveKind.SWEEP ? 8 : 5, (int) Math.round(spec.radius() * scale)) : spec.radius();
            this.cellSize = home ? 1 : 4;
            this.escort = stand(center);
        }
        public String id() { return spec.id(); }
        public ObjectiveKind kind() { return spec.kind(); }
        /** Fixed world build anchor. Use marker() for the moving payload or current courier checkpoint. */
        public BlockPos center() { return center; }
        public int radius() { return radius; }
        public boolean optional() { return spec.optional(); }
        public boolean active() { return !complete && (optional() || spec.stage() == stage); }
        public boolean complete() { return complete; }
        public boolean activated() { return activated; }
        public boolean contested() { return contested; }
        public boolean priorityTarget() { return kind() == ObjectiveKind.SWEEP && spec.target() == 1; }
        public UUID priorityTargetId() { return priorityTarget; }
        public LibertyMissionPlan.Objective spec() { return spec; }
        public List<BlockPos> checkpoints() { return List.copyOf(checkpoints); }
        public int progress() { return timed() ? heldTicks / 20 : count; }
        public int goal() { return timed() ? spec.durationSeconds() : spec.target(); }
        public Vec3 marker() {
            if (kind() == ObjectiveKind.ESCORT) return escort;
            if (kind() == ObjectiveKind.COURIER && activated && count < checkpoints.size()) return stand(checkpoints.get(count));
            return stand(center);
        }
        /** Distinct covered ground cells; height and face aliases never manufacture extra territory. */
        public List<BlockPos> paintedCells() {
            return cells.stream().sorted().map(key -> center.offset(cellX(key) * cellSize, 0, cellZ(key) * cellSize)).toList();
        }
        private boolean timed() { return kind() == ObjectiveKind.SCAN || kind() == ObjectiveKind.RELAY_HOLD || kind() == ObjectiveKind.DEFEND; }
        private boolean started() { return activated || heldTicks > 0 || count > 0 || !cells.isEmpty() || cores.stream().anyMatch(c -> c.health < c.maximumHealth); }
        private boolean inside(Vec3 point, double distance) { return close(point, stand(center), distance, 8); }
        private boolean addCell(int x, int z) {
            if (!active() || kind() != ObjectiveKind.TERRITORY || count >= goal()) return false;
            double dx = x * cellSize + cellSize * .5, dz = z * cellSize + cellSize * .5;
            if (dx * dx + dz * dz > radius * radius || !cells.add(cell(x, z))) return false;
            count++;
            update(this);
            return true;
        }
        private String status() {
            if (complete) return optional() ? "Optional objective secured." : "Objective secured.";
            if (!active()) return "Complete the earlier stage to activate this objective.";
            String progress = progress() + "/" + goal();
            return switch (kind()) {
                case TERRITORY -> "Paint distinct ground cells in this zone: " + progress + ". Rivals reclaim unfinished turf every four seconds.";
                case RELAY_HOLD -> (contested ? "Contested: clear nearby rivals. " : "Skate within the marked zone to broadcast. ") + progress + "s.";
                case DEFEND -> (contested ? "Contested: clear nearby rivals. " : "Stay near the defense beacon. ") + progress + "s; abandoning it loses progress.";
                case SCAN -> !activated ? "Use Objective interact beside the survey terminal, then remain nearby."
                        : (contested ? "Scan contested: clear nearby rivals. " : "Remain beside the terminal to scan. ") + progress + "s.";
                case COURIER -> !activated ? "Use Objective interact beside the courier depot to collect its canister."
                        : "Skate through the marked checkpoints in order: " + progress + ".";
                case ESCORT -> !activated ? "Use Objective interact beside the paint rig to power it up."
                        : (contested ? "Payload contested: clear its path. " : "Stay near the moving paint rig to escort it. ") + progress + " waypoints.";
                case DEMOLITION -> "Destroy the marked physical base cores: " + progress + ".";
                case SWEEP -> priorityTarget() ? "Defeat the actual marked priority enforcer in this district; ordinary rivals do not count."
                        : "Defeat mission-owned rivals in this district: " + progress + ".";
            };
        }
    }

    public static final class Core {
        private final String id, siteId;
        private BlockPos position;
        private final float maximumHealth;
        private float health;
        private Core(String id, String siteId, BlockPos position, float health) {
            this.id = id; this.siteId = siteId; this.position = position.immutable();
            this.health = health; this.maximumHealth = health;
        }
        public String id() { return id; }
        public String siteId() { return siteId; }
        public BlockPos position() { return position; }
        public float health() { return health; }
        public float maximumHealth() { return maximumHealth; }
        public boolean alive() { return health > 0; }
    }

    private final LibertyMissionPlan plan;
    private final BlockPos origin;
    private final boolean home;
    private final double scale;
    private final List<Site> sites;
    private final List<Core> cores;
    private final Map<String, Site> byId = new LinkedHashMap<>();
    private final Map<String, Core> coreById = new LinkedHashMap<>();
    private final Set<UUID> creditedDefeats = new HashSet<>();
    private int stage, rewards;
    private long ticks;

    public LibertyMissionRuntime(LibertyMissionPlan plan, BlockPos origin, boolean home) {
        this.plan = Objects.requireNonNull(plan);
        this.origin = Objects.requireNonNull(origin).immutable();
        this.home = home;
        this.scale = home ? 96.0 / plan.arenaRadius() : 1;
        for (LibertyMissionPlan.Objective objective : plan.allObjectives()) {
            Site site = new Site(objective);
            if (byId.putIfAbsent(site.id(), site) != null) throw new IllegalArgumentException("Duplicate objective ID");
        }
        this.sites = List.copyOf(byId.values());
        for (LibertyMissionPlan.Stronghold base : plan.strongholds()) {
            Site site = byId.get(base.objectiveId());
            if (site == null || site.kind() != ObjectiveKind.DEMOLITION || base.cores() != site.goal())
                throw new IllegalArgumentException("Stronghold has no matching demolition objective");
            for (int i = 0; i < base.cores(); i++) {
                double angle = i * Math.PI * 2 / base.cores();
                int spread = home ? 3 : 8;
                BlockPos position = site.center.offset((int) Math.round(Math.cos(angle) * spread), 1,
                        (int) Math.round(Math.sin(angle) * spread));
                Core core = new Core(site.id() + "_core_" + i, site.id(), position, 40 + 20 * base.tier());
                if (coreById.putIfAbsent(core.id(), core) != null) throw new IllegalArgumentException("Duplicate base core ID");
                site.cores.add(core);
            }
        }
        for (Site site : sites) if (site.kind() == ObjectiveKind.DEMOLITION && site.cores.size() != site.goal())
            throw new IllegalArgumentException("Demolition objective has no physical cores");
        this.cores = List.copyOf(coreById.values());
    }

    public List<Site> sites() { return sites; }
    public List<Core> cores() { return cores; }
    /** Zero-based active stage, or stages().size() after every primary stage is complete. */
    public int stage() { return stage; }
    public int completedPrimary() { return (int) sites.stream().filter(s -> !s.optional() && s.complete()).count(); }
    public int totalPrimary() { return (int) sites.stream().filter(s -> !s.optional()).count(); }
    public boolean complete() { return stage >= plan.stages().size(); }

    /** Successful ordinary graffiti only. The game-facing caller has already validated the real paint action. */
    public boolean paint(BlockPos support, Direction face, Vec3 playerPosition) {
        if (support == null || face == null || !finite(playerPosition)
                || playerPosition.distanceToSqr(Vec3.atCenterOf(support)) > 36) return false;
        Vec3 at = Vec3.atCenterOf(support);
        Site site = sites.stream().filter(s -> s.active() && s.kind() == ObjectiveKind.TERRITORY && s.inside(at, s.radius))
                .min(Comparator.comparingDouble(s -> stand(s.center).distanceToSqr(at))).orElse(null);
        return site != null && site.addCell(Math.floorDiv(support.getX() - site.center.getX(), site.cellSize),
                Math.floorDiv(support.getZ() - site.center.getZ(), site.cellSize));
    }

    /** Trusted server call-in area. Bounded work and cell deduplication make CHROMAGEDDON a real turf action. */
    public int paintArea(Vec3 center, double radius) {
        return paintArea(center, radius, support -> true);
    }

    /** Credit only cells where the caller actually placed an effect on a loaded surface. */
    public int paintArea(Vec3 center, double radius, Predicate<BlockPos> appliedSurface) {
        if (!finite(center) || !Double.isFinite(radius) || radius <= 0 || radius > 128 || appliedSurface == null) return 0;
        int painted = 0;
        int activeStage = stage;
        for (Site site : sites) {
            if (!site.active() || !site.optional() && site.spec.stage() != activeStage
                    || site.kind() != ObjectiveKind.TERRITORY || Math.abs(center.y - stand(site.center).y) > 12) continue;
            int limit = (int) Math.ceil((double) site.radius / site.cellSize);
            for (int x = -limit; x <= limit && site.active(); x++) for (int z = -limit; z <= limit && site.active(); z++) {
                double dx = site.center.getX() + x * site.cellSize + site.cellSize * .5 - center.x;
                double dz = site.center.getZ() + z * site.cellSize + site.cellSize * .5 - center.z;
                BlockPos actualCell = site.center.offset(x * site.cellSize + site.cellSize / 2, 0,
                        z * site.cellSize + site.cellSize / 2);
                if (dx * dx + dz * dz <= radius * radius && appliedSurface.test(actualCell) && site.addCell(x, z)) painted++;
            }
        }
        return painted;
    }

    /** Tick once per server tick while the owner is in the active field. Enemy counts must be mission-owned. */
    public void tick(Vec3 position, Vec3 previous, boolean skating, ToIntFunction<BlockPos> nearbyEnemies) {
        if (!finite(position) || nearbyEnemies == null) return;
        ticks++;
        double step = finite(previous) ? position.distanceToSqr(previous) : Double.POSITIVE_INFINITY;
        boolean riding = skating && step >= .0025 && step <= 16;
        int activeStage = stage; // A single movement/hold tick cannot advance two successive stages.
        for (Site site : sites) {
            if (!site.active() || !site.optional() && site.spec.stage() != activeStage) continue;
            site.contested = nearbyEnemies.applyAsInt(BlockPos.containing(site.marker())) > 0;
            switch (site.kind()) {
                case TERRITORY -> {
                    if (site.contested && ticks % 80 == 0 && !site.cells.isEmpty()) {
                        long reclaimed = site.cells.stream().mapToLong(Long::longValue).min().orElseThrow();
                        site.cells.remove(reclaimed);
                        site.count--;
                    }
                }
                case RELAY_HOLD, DEFEND -> {
                    boolean eligible = site.inside(position, site.radius) && !site.contested
                            && (site.kind() == ObjectiveKind.DEFEND || riding);
                    if (eligible) site.heldTicks++;
                    else if (ticks % 4 == 0) site.heldTicks = Math.max(0, site.heldTicks - 1);
                    update(site);
                }
                case SCAN -> {
                    if (!site.activated) break;
                    if (!site.inside(position, home ? 4 : 6)) { site.activated = false; site.heldTicks = 0; }
                    else if (!site.contested) site.heldTicks++;
                    update(site);
                }
                case COURIER -> {
                    if (site.activated && riding && site.count < site.checkpoints.size()
                            && close(position, stand(site.checkpoints.get(site.count)), home ? 1.5 : 5, 6)) {
                        site.count++;
                        update(site);
                    }
                }
                case ESCORT -> {
                    if (!site.activated || site.contested || site.count >= site.checkpoints.size()
                            || !close(position, site.escort, home ? 6 : 14, 8)) break;
                    Vec3 target = stand(site.checkpoints.get(site.count));
                    Vec3 difference = target.subtract(site.escort);
                    double distance = difference.length();
                    double speed = home ? .055 : .16;
                    if (distance <= speed) { site.escort = target; site.count++; update(site); }
                    else site.escort = site.escort.add(difference.scale(speed / distance));
                }
                default -> { }
            }
        }
    }

    public boolean interact(Vec3 playerPosition) {
        if (!finite(playerPosition)) return false;
        Site site = sites.stream().filter(s -> s.active() && !s.activated && (s.kind() == ObjectiveKind.SCAN
                        || s.kind() == ObjectiveKind.COURIER || s.kind() == ObjectiveKind.ESCORT)
                        && s.inside(playerPosition, home ? 3 : 5))
                .min(Comparator.comparingDouble(s -> s.marker().distanceToSqr(playerPosition))).orElse(null);
        if (site == null) return false;
        site.activated = true;
        return true;
    }

    /** Only deaths that the caller has proved belong to this mission are eligible. */
    public boolean defeated(UUID actor, Vec3 position) {
        if (actor == null || !finite(position) || creditedDefeats.contains(actor)) return false;
        Site site = sites.stream().filter(s -> s.active() && s.kind() == ObjectiveKind.SWEEP
                        && (s.priorityTarget() ? actor.equals(s.priorityTarget) && withinArena(position) : s.inside(position, s.radius)))
                .min(Comparator.comparingDouble(s -> stand(s.center).distanceToSqr(position))).orElse(null);
        if (site == null) return false;
        creditedDefeats.add(actor); // Bounded by the total configured sweep goals, never every spawn in the mission.
        site.count++;
        update(site);
        return true;
    }

    /** Bind a real server-spawned priority actor. Rebinding a lost/despawned actor is the manager's responsibility. */
    public boolean assignPriorityTarget(String siteId, UUID actor) {
        Site site = byId.get(siteId);
        if (site == null || !site.active() || !site.priorityTarget() || actor == null || creditedDefeats.contains(actor)) return false;
        site.priorityTarget = actor;
        return true;
    }

    /** Future-stage cores must be invulnerable in the entity layer; early destruction is not credited here. */
    public boolean damageCore(String id, float damage) {
        Core core = coreById.get(id);
        if (core == null || !core.alive() || !Float.isFinite(damage) || damage <= 0) return false;
        Site site = byId.get(core.siteId);
        if (!site.active()) return false;
        core.health = Math.max(0, core.health - damage);
        if (!core.alive()) { site.count++; update(site); }
        return true;
    }

    /** Number of newly completed objectives, drained once; persistent settlement remains with the manager. */
    public int consumeRewards() { int result = rewards; rewards = 0; return result; }

    /** Home objectives may be aligned to loaded terrain once, before any objective interaction or damage. */
    public boolean relocateSite(String id, BlockPos groundCenter) {
        Site site = byId.get(id);
        if (!home || site == null || groundCenter == null || site.relocated || site.started() || site.complete) return false;
        if (Math.abs((long) groundCenter.getX() - origin.getX()) > 128
                || Math.abs((long) groundCenter.getZ() - origin.getZ()) > 128) return false;
        BlockPos difference = groundCenter.subtract(site.center);
        site.center = groundCenter.immutable();
        for (int i = 0; i < site.checkpoints.size(); i++) site.checkpoints.set(i, site.checkpoints.get(i).offset(difference));
        for (Core core : site.cores) core.position = core.position.offset(difference);
        site.escort = site.escort.add(difference.getX(), difference.getY(), difference.getZ());
        site.relocated = true;
        return true;
    }

    public ListTag snapshot(Vec3 playerPosition) {
        ListTag result = new ListTag();
        for (Site site : sites) {
            CompoundTag tag = new CompoundTag();
            tag.putString("Id", site.id()); tag.putString("Kind", site.kind().name());
            tag.putString("Name", site.spec.name()); tag.putString("Description", site.status());
            tag.putInt("Progress", site.progress()); tag.putInt("Goal", site.goal());
            tag.putInt("Stage", site.spec.stage()); tag.putInt("Radius", site.radius());
            tag.putBoolean("Complete", site.complete()); tag.putBoolean("Optional", site.optional());
            tag.putBoolean("Active", site.active()); tag.putBoolean("Activated", site.activated());
            tag.putBoolean("Contested", site.contested());
            tag.putBoolean("Priority", site.priorityTarget());
            Vec3 marker = site.marker();
            tag.putInt("X", (int) Math.floor(marker.x)); tag.putInt("Y", (int) Math.floor(marker.y)); tag.putInt("Z", (int) Math.floor(marker.z));
            tag.putDouble("Distance", finite(playerPosition) ? Math.sqrt(playerPosition.distanceToSqr(marker)) : -1);
            result.add(tag);
        }
        return result;
    }

    private void update(Site site) {
        if (site.complete || (site.timed() ? site.heldTicks < site.goal() * 20 : site.count < site.goal())) return;
        site.complete = true;
        site.contested = false;
        rewards++;
        while (stage < plan.stages().size()) {
            int current = stage;
            if (sites.stream().anyMatch(s -> !s.optional() && s.spec.stage() == current && !s.complete)) break;
            stage++;
        }
    }
    private BlockPos map(LibertyMissionPlan.Point point) {
        return origin.offset((int) Math.round(point.x() * scale), 0, (int) Math.round(point.z() * scale));
    }
    private boolean withinArena(Vec3 point) {
        int radius = home ? 128 : plan.arenaRadius();
        return Math.abs(point.x - origin.getX()) <= radius && Math.abs(point.z - origin.getZ()) <= radius;
    }
    private static Vec3 stand(BlockPos ground) { return Vec3.atCenterOf(ground).add(0, .5, 0); }
    private static boolean close(Vec3 a, Vec3 b, double radius, double vertical) {
        double x = a.x - b.x, z = a.z - b.z;
        return Math.abs(a.y - b.y) <= vertical && x * x + z * z <= radius * radius;
    }
    private static boolean finite(Vec3 point) { return point != null && Double.isFinite(point.x) && Double.isFinite(point.y) && Double.isFinite(point.z); }
    private static long cell(int x, int z) { return (long) x << 32 | z & 0xffffffffL; }
    private static int cellX(long key) { return (int) (key >> 32); }
    private static int cellZ(long key) { return (int) key; }
}
