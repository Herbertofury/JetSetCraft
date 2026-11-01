package com.herberto.jetsetcraft.crew;

import java.util.List;
import java.util.Optional;

/** Small, deterministic first mission catalogue. Durations use saved server game time, never wall-clock time. */
public final class CrewMissionCatalog {
    public enum Role {
        COURIER("courier", "Courier"), TAGGER("tagger", "Tagger"), TECHNICIAN("technician", "Technician"), DJ("dj", "DJ");
        public final String id;
        public final String label;
        Role(String id, String label) { this.id = id; this.label = label; }
        public static Role from(String value) {
            for (Role role : values()) if (role.id.equals(value)) return role;
            return COURIER;
        }
    }
    public record Mission(String id, String name, Role role, long durationTicks,
                          int supplies, int memberXp, int crewXp, int reputation) {}
    public static final List<Mission> ALL = List.of(
            new Mission("courier_run", "Neighborhood delivery", Role.COURIER, 3L * 60L * 20L, 12, 35, 40, 6),
            new Mission("paint_run", "Community mural commission", Role.TAGGER, 8L * 60L * 20L, 30, 90, 100, 14),
            new Mission("soundcheck_run", "Soundcheck circuit", Role.DJ, 8L * 60L * 20L, 30, 90, 100, 14),
            new Mission("salvage_run", "Workshop salvage circuit", Role.TECHNICIAN, 15L * 60L * 20L, 55, 160, 180, 25));
    public static Optional<Mission> byId(String id) { return ALL.stream().filter(m -> m.id.equals(id)).findFirst(); }
    private CrewMissionCatalog() {}
}
