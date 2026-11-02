package com.herberto.jetsetcraft.rush;

import net.minecraft.world.phys.Vec3;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/** Bounded, active-clock encounter rules. Only the manager may spawn, damage or settle an actor. */
public final class RushDirector {
    public static final int MAX_ELITES = 4;
    public static final int MAX_TELLS = 8;
    public static final long FREE_STREET_INTERVAL = 6000;
    private static final int MAX_SPAWN_ATTEMPTS = 8;

    public enum Elite {
        NONE(""), LINEBREAKER("Linebreaker"), BOMBARDIER("Bombardier"), BASSWARDEN("Basswarden");
        private final String label;
        Elite(String label) { this.label = label; }
        public String label() { return label; }
        public Pattern pattern() { return switch (this) {
            case LINEBREAKER -> Pattern.LINE;
            case BOMBARDIER -> Pattern.DISK;
            case BASSWARDEN -> Pattern.RING;
            case NONE -> throw new IllegalStateException("Ordinary rivals have no elite attack");
        }; }
    }

    public enum Pattern {
        LINE("Paint lane", "Step sideways out of the lane", 30, 9),
        DISK("Paint drop", "Leave the marked circle", 32, 9),
        RING("Bass ring", "Move inside or outside the ring", 36, 10);
        private final String label, counterplay;
        private final int windup;
        private final double damage;
        Pattern(String label, String counterplay, int windup, double damage) {
            this.label = label; this.counterplay = counterplay; this.windup = windup; this.damage = damage;
        }
        public String label() { return label; }
        public String counterplay() { return counterplay; }
        public int windup() { return windup; }
        public double damage() { return damage; }
    }

    /** Geometry is locked when a tell starts; it never follows a player after they dodge. */
    public record Tell(UUID actor, Pattern pattern, Vec3 source, Vec3 anchor, Vec3 direction,
                       long startsAt, long resolvesAt, boolean headliner) {
        public boolean covers(Vec3 point) {
            if (Math.abs(point.y - anchor.y) > 1.65) return false;
            Vec3 offset = new Vec3(point.x - anchor.x, 0, point.z - anchor.z);
            if (pattern == Pattern.LINE) {
                double along = offset.dot(direction);
                return along >= 0 && along <= 10 && offset.subtract(direction.scale(along)).lengthSqr() <= 1;
            }
            double distance = offset.lengthSqr();
            return pattern == Pattern.DISK ? distance <= 2.5 * 2.5 : distance >= 2.25 * 2.25 && distance <= 7 * 7;
        }
    }

    private final List<Tell> tells = new ArrayList<>(MAX_TELLS);
    private String state = "scheduled", abortReason = "";
    private UUID headliner;
    private long encounterKey = Long.MIN_VALUE, lastFreeWindow, nextAttempt;
    private int scheduledStage, attempts, defeatedCount;
    private boolean circuitEncounter, forgeEarned, returnRequired;

    /** Missed free-street windows are consumed while a headliner is alive, never queued for later. */
    public void schedule(long activeTick, boolean circuit, int set, int stage) {
        if (circuit) {
            scheduledStage = stage <= 3 ? 3 : 6;
            if (stage != 3 && stage != 6) return;
            long key = ((long) set << 32) | stage;
            if (key != encounterKey) prepare(key, stage, true, activeTick);
        } else {
            scheduledStage = 0;
            long window = activeTick / FREE_STREET_INTERVAL;
            if (window <= lastFreeWindow) return;
            lastFreeWindow = window;
            if (headliner == null && !spawnPending() && !returnRequired) prepare(window, 0, false, activeTick);
        }
    }

    private void prepare(long key, int stage, boolean circuit, long activeTick) {
        encounterKey = key; scheduledStage = stage; circuitEncounter = circuit;
        state = "scheduled"; headliner = null; attempts = 0; nextAttempt = activeTick;
        abortReason = ""; forgeEarned = false; returnRequired = false;
    }

    public boolean spawnPending() { return encounterKey != Long.MIN_VALUE && state.equals("scheduled"); }
    public boolean attemptDue(long activeTick) { return spawnPending() && activeTick >= nextAttempt; }
    public void spawnFailed(long activeTick) {
        if (!spawnPending()) return;
        attempts++; nextAttempt = activeTick + 20;
        if (attempts >= MAX_SPAWN_ATTEMPTS) abort("The headliner could not safely enter this arena.", true);
    }
    public boolean spawned(UUID actor) {
        if (actor == null || !spawnPending()) return false;
        headliner = actor; state = "active"; return true;
    }
    public boolean defeated(UUID actor) {
        if (actor == null || !actor.equals(headliner) || !state.equals("active")) return false;
        cancel(actor); headliner = null; state = "victory"; forgeEarned = true;
        if (defeatedCount < Integer.MAX_VALUE) defeatedCount++;
        return true;
    }
    public void abort(String reason, boolean mustReturn) {
        if (!state.equals("active") && !spawnPending()) return;
        if (headliner != null) cancel(headliner);
        headliner = null; state = "aborted"; abortReason = reason; returnRequired = mustReturn;
    }
    public boolean permitsRoomCompletion(int set, int stage) {
        if (stage != 3 && stage != 6) return true;
        return circuitEncounter && encounterKey == (((long) set << 32) | stage) && state.equals("victory");
    }

    public boolean tell(UUID actor, Pattern pattern, Vec3 source, Vec3 target, long activeTick,
                        int delay, boolean isHeadliner) {
        if (actor == null || pattern == null || tells.size() >= MAX_TELLS) return false;
        Vec3 direction = new Vec3(target.x - source.x, 0, target.z - source.z);
        direction = direction.lengthSqr() < .0001 ? new Vec3(0, 0, 1) : direction.normalize();
        Vec3 anchor = pattern == Pattern.DISK ? target : source;
        long starts = activeTick + Math.max(0, Math.min(40, delay));
        tells.add(new Tell(actor, pattern, source, anchor, direction, starts, starts + pattern.windup(), isHeadliner));
        return true;
    }
    public List<Tell> tells() { return List.copyOf(tells); }
    public void remove(Tell tell) { tells.remove(tell); }
    public void cancel(UUID actor) { tells.removeIf(tell -> tell.actor().equals(actor)); }
    public void clearTells() { tells.clear(); }
    public void skipFreeWindow(long activeTick) { lastFreeWindow = Math.max(lastFreeWindow, activeTick / FREE_STREET_INTERVAL); }
    public UUID headliner() { return headliner; }
    public String state() { return state; }
    public String abortReason() { return abortReason; }
    public int scheduledStage() { return scheduledStage; }
    public int defeatedCount() { return defeatedCount; }
    public boolean forgeEarned() { return forgeEarned; }
    public boolean returnRequired() { return returnRequired; }
}
