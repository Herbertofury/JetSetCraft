package com.herberto.jetsetcraft.mob;

import java.util.Locale;

/** Auditable origin for physical Street Gear without changing the source mob's AI or registry identity. */
public enum StreetGearAcquisition {
    PLAYER("player"),
    NATIVE_PICKUP("native_pickup"),
    DROPPED_CONTACT("dropped_contact"),
    DISPENSER("dispenser"),
    COMMAND("command"),
    RESTORED("restored");

    private final String id;

    StreetGearAcquisition(String id) {
        this.id = id;
    }

    public String id() {
        return id;
    }

    public static StreetGearAcquisition byId(String id) {
        if (id != null && !id.isBlank()) {
            String normalized = id.trim().toLowerCase(Locale.ROOT);
            for (StreetGearAcquisition value : values()) {
                if (value.id.equals(normalized) || value.name().toLowerCase(Locale.ROOT).equals(normalized)) {
                    return value;
                }
            }
        }
        return RESTORED;
    }
}
