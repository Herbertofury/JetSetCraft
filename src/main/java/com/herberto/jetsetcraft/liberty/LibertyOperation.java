package com.herberto.jetsetcraft.liberty;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.phys.Vec3;

import java.util.List;
import java.util.Set;
import java.util.HashSet;
import java.util.UUID;

/** Server-owned expedition rules. No client score, position, currency or completion is accepted. */
public final class LibertyOperation {
    public enum Phase { DROPPING, ACTIVE, DOWNED, EXTRACTING, BOARDING, DEPARTING, RETURNING }
    public record Theater(String id, String name, String description, String gang, String species,
                          int radius, int defenders, int minutes, int reward) {
        public ResourceLocation gangId() { return ResourceLocation.tryParse(gang); }
        public ResourceLocation speciesId() { return ResourceLocation.tryParse(species); }
    }
    public static final List<Theater> THEATERS = List.of(
            new Theater("neon_docks", "Neon Docks", "Take the waterfront from Dead Beat. Wide avenues, warehouse walls and long rail lines.",
                    "jetsetcraft:dead_beat", "minecraft:zombie", 160, 5, 25, 25),
            new Theater("overpass_uprising", "Overpass Uprising", "Break the Bone Drones' blockade. Exposed crossings reward quick transfers and cover.",
                    "jetsetcraft:bone_drones", "minecraft:skeleton", 224, 7, 30, 40),
            new Theater("blackout_borough", "Blackout Borough", "Jam Hex Appeal's broadcast and steal its pigment research. The longest, richest expedition.",
                    "jetsetcraft:hex_appeal", "minecraft:witch", 288, 9, 35, 60));
    public static Theater theater(String id) { return THEATERS.stream().filter(t -> t.id.equals(id)).findFirst().orElse(null); }

    public static final class Objective {
        private final int index, defenders, panels;
        private final BlockPos center;
        private final Set<UUID> defeated = new HashSet<>();
        private final Set<BlockPos> painted = new HashSet<>();
        private int relay;
        private long broadcastTicks;
        private boolean complete;
        public Objective(int index, BlockPos center, int defenders, int panels) {
            this.index = index; this.center = center.immutable(); this.defenders = defenders;
            this.panels = index == 0 ? panels : 1;
        }
        public int index() { return index; }
        public BlockPos center() { return center; }
        public int defenders() { return defenders; }
        public int defeats() { return defeated.size(); }
        public int paints() { return painted.size(); }
        public int panels() { return panels; }
        public int relay() { return relay; }
        public long broadcastTicks() { return broadcastTicks; }
        public boolean complete() { return complete; }
        public String name() { return switch (index) { case 0 -> "Paint the town free"; case 1 -> "Pirate radio"; default -> "Grindline courier"; }; }
        public BlockPos panel(int number) { return center.offset((number - 1) * 4, 2, 0); }
        public BlockPos beacon(int number) {
            return switch (number) { case 0 -> center.offset(-14, 0, 12); case 1 -> center.offset(0, 0, 27); default -> center.offset(14, 0, 12); };
        }
        public boolean defeated(UUID actor) {
            if (complete || actor == null || defeated.size() >= defenders) return false;
            boolean accepted = defeated.add(actor); update(); return accepted;
        }
        /** Called only after the ordinary spray can actually paints the designated face. */
        public boolean paint(BlockPos support, Direction face, Vec3 position) {
            if (complete || defeats() < defenders || face != Direction.SOUTH || support == null || !finite(position)
                    || position.distanceToSqr(Vec3.atCenterOf(support)) > 36) return false;
            for (int i = 0; i < panels; i++) if (panel(i).getZ() == support.getZ()
                    && Math.abs(panel(i).getX() - support.getX()) <= 1 && Math.abs(panel(i).getY() - support.getY()) <= 1) {
                boolean accepted = painted.add(panel(i)); update(); return accepted;
            }
            return false;
        }
        /** Broadcast requires actual skating; relay progress is ordered and grounded in server positions. */
        public void advance(Vec3 position, Vec3 previous, boolean skating) {
            if (complete || paints() < panels || defeats() < defenders || !skating || !finite(position) || !finite(previous)) return;
            double step = position.distanceToSqr(previous);
            if (step < .0025 || step > 16) return; // standing still, teleporting and leaps cannot fabricate a route
            if (index == 1 && position.distanceToSqr(Vec3.atCenterOf(center)) <= 24 * 24) broadcastTicks++;
            if (index == 2 && relay < 3 && position.distanceToSqr(Vec3.atCenterOf(beacon(relay))) <= 5 * 5) relay++;
            update();
        }
        private void update() {
            complete = defeats() >= defenders && paints() >= panels
                    && (index != 1 || broadcastTicks >= 200) && (index != 2 || relay >= 3);
        }
        public String description() {
            if (complete) return "District liberated. Research secured.";
            if (defeats() < defenders) return "Clear the rival crew: " + defeats() + "/" + defenders + ".";
            if (paints() < panels) return "Spray the marked SOUTH-facing mural panels: " + paints() + "/" + panels + ".";
            return index == 1 ? "Keep skating within 24 blocks to broadcast: " + (broadcastTicks / 20) + "/10s."
                    : "Ride the lit courier beacons in order: " + relay + "/3.";
        }
        public CompoundTag snapshot(Vec3 player) {
            CompoundTag tag = new CompoundTag(); tag.putString("Name", name()); tag.putString("Description", description());
            tag.putInt("Progress", defeats() + paints() + (index == 1 ? (int) Math.min(10, broadcastTicks / 20) : index == 2 ? relay : 0));
            tag.putInt("Goal", defenders + panels + (index == 1 ? 10 : index == 2 ? 3 : 0)); tag.putBoolean("Complete", complete);
            BlockPos target = index == 2 && paints() >= panels && relay < 3 ? beacon(relay) : center;
            tag.putInt("X", target.getX()); tag.putInt("Z", target.getZ());
            tag.putDouble("Distance", Math.sqrt(player.distanceToSqr(Vec3.atCenterOf(target)))); return tag;
        }
    }

    public final UUID id, owner;
    public final Theater theater;
    public final BlockPos origin;
    public final BlockPos drop;
    public BlockPos extraction;
    public final int slot;
    public final List<Objective> objectives;
    public final LibertyMissionPlan plan;
    public final LibertyMissionRuntime mission;
    public final boolean homeDefense;
    private Phase phase = Phase.DROPPING;
    private long phaseTicks, ticks, revision = 1;
    private int credits, common, rare, supers, resupplies;
    private final Set<Integer> caches = new HashSet<>();
    private final Set<Integer> awarded = new HashSet<>();
    public LibertyOperation(UUID id, UUID owner, Theater theater, BlockPos origin, int slot, boolean signalLacquer, boolean supplySkates) {
        this.id = id; this.owner = owner; this.theater = theater; this.origin = origin.immutable(); this.slot = slot;
        this.plan = null; this.mission = null; this.homeDefense = false;
        this.drop=origin.immutable();
        int r = theater.radius;
        extraction = origin.offset(0, 0, r - 20);
        objectives = List.of(new Objective(0, origin.offset(-r / 2, 0, -r / 3), theater.defenders, signalLacquer ? 2 : 3),
                new Objective(1, origin.offset(r / 2, 0, -r / 3), theater.defenders, 1),
                new Objective(2, origin.offset(0, 0, r / 2), theater.defenders, 1));
        resupplies = supplySkates ? 4 : 3;
    }
    public LibertyOperation(UUID id, UUID owner, Theater theater, BlockPos origin, int slot,
                            LibertyMissionPlan plan, boolean homeDefense, boolean supplySkates) {
        this.id = id; this.owner = owner; this.origin = origin.immutable(); this.slot = slot;
        this.plan = plan; this.homeDefense = homeDefense;
        this.drop=homeDefense?origin.immutable():origin.offset(plan.drop().x(),0,plan.drop().z());
        this.theater = new Theater(theater.id(), theater.name(), theater.description(), theater.gang(), theater.species(),
                homeDefense ? 128 : plan.arenaRadius(), plan.difficulty().level() + 4, plan.durationMinutes(), 45 + 9 * plan.difficulty().level());
        this.objectives = List.of(); this.mission = new LibertyMissionRuntime(plan, origin, homeDefense);
        double scale = homeDefense ? 96.0 / plan.arenaRadius() : 1;
        this.extraction = origin.offset((int) (plan.extraction().x() * scale), 0, (int) (plan.extraction().z() * scale));
        this.resupplies = supplySkates ? 4 : 3;
    }
    public boolean campaign() { return plan != null; }
    public Phase phase() { return phase; }
    public long phaseTicks() { return phaseTicks; }
    public long ticks() { return ticks; }
    public long revision() { return revision; }
    public int credits() { return credits; }
    public int common() { return common; }
    public int rare() { return rare; }
    public int supers() { return supers; }
    public int resupplies() { return resupplies; }
    public void addResupplies(int amount) { resupplies=Math.max(0,Math.min(5,resupplies+Math.max(0,amount))); }
    public int completed() { return mission == null ? (int) objectives.stream().filter(Objective::complete).count()
            : Math.min(3, mission.completedPrimary() * 3 / Math.max(1, mission.totalPrimary())); }
    public boolean complete() { return mission == null ? completed() == objectives.size() : mission.complete(); }
    public boolean field() { return phase == Phase.ACTIVE || phase == Phase.EXTRACTING || phase == Phase.BOARDING; }
    /** The deadline closes boarding; an accepted departure finishes without revoking extraction. */
    public boolean expired() { return phase != Phase.DEPARTING && ticks >= theater.minutes * 1200L; }
    public void tick() { ticks++; phaseTicks++; }
    public void phase(Phase next) { phase = next; phaseTicks = 0; revision++; }
    public boolean matches(UUID mission, long version) { return id.equals(mission) && revision == version; }
    public boolean extract(Vec3 position) {
        if (phase != Phase.ACTIVE || expired() || !complete() || !finite(position) || position.distanceToSqr(Vec3.atCenterOf(extraction)) > 12 * 12) return false;
        phase(Phase.EXTRACTING); return true;
    }
    public boolean board(Vec3 position) {
        if (phase != Phase.BOARDING || expired() || phaseTicks < 60 || !finite(position) || position.distanceToSqr(Vec3.atCenterOf(extraction)) > 8 * 8) return false;
        phase(Phase.DEPARTING); return true;
    }
    public boolean spendResupply() { if (!field() || resupplies <= 0) return false; resupplies--; revision++; return true; }
    public boolean cache(int index) {
        if (!field() || index < 0 || index >= 9 || !caches.add(index)) return false;
        if (index < 3) credits += 10; else if (index < 6) common += 3; else if (index < 8) rare += 2; else supers++;
        return true;
    }
    public boolean cacheClaimed(int index) { return caches.contains(index); }
    public BlockPos cachePosition(int index) {
        double angle = index * Math.PI * 2 / 9;
        int distance = (int) (theater.radius * (index == 8 ? .83 : .64));
        return origin.offset((int) (Math.cos(angle) * distance), 0, (int) (Math.sin(angle) * distance));
    }
    /** Each completed objective contributes once, independently of rendering or repeated packets. */
    public boolean awardObjectives() {
        if (mission != null) {
            int rewards = mission.consumeRewards();
            if (rewards <= 0) return false;
            credits = Math.min(1000, credits + rewards * (6 + plan.difficulty().level()));
            common = Math.min(1000, common + rewards * 3);
            rare = Math.min(1000, rare + rewards * (plan.difficulty().level() >= 4 ? 2 : 1));
            if (plan.difficulty().level() >= 7 && complete() && awarded.add(-1)) supers = Math.min(1000, supers + 1);
            resupplies = Math.min(4, resupplies + rewards); return true;
        }
        boolean changed = false;
        for (Objective objective : objectives) if (objective.complete && awarded.add(objective.index)) {
            credits += theater.reward / 3; common += 3; rare++; resupplies = Math.min(4, resupplies + 1); changed = true;
        }
        return changed;
    }
    public ListTag objectivesSnapshot(Vec3 player) {
        if (mission != null) return mission.snapshot(player);
        ListTag list = new ListTag(); objectives.forEach(o -> list.add(o.snapshot(player))); return list;
    }
    private static boolean finite(Vec3 point) { return point != null && Double.isFinite(point.x) && Double.isFinite(point.y) && Double.isFinite(point.z); }
}
