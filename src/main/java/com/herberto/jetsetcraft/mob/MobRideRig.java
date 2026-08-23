package com.herberto.jetsetcraft.mob;

/**
 * Species-aware contact family used by both persistence and rendering.  It deliberately describes
 * where equipment meets the ground rather than assuming every mob has a humanoid skeleton.
 */
public enum MobRideRig {
    BIPED("biped", 2, 0.56f),
    QUADRUPED("quadruped", 4, 0.70f),
    MULTI_LEG("multi_leg", 6, 0.78f),
    BODY_CONTACT("body_contact", 1, 0.88f),
    AERIAL("aerial", 2, 0.64f),
    AQUATIC("aquatic", 1, 0.90f),
    GENERIC("generic", 2, 0.68f);

    private final String id;
    private final int contactCount;
    private final float footprintScale;

    MobRideRig(String id, int contactCount, float footprintScale) {
        this.id = id;
        this.contactCount = contactCount;
        this.footprintScale = footprintScale;
    }

    public String id() {
        return id;
    }

    public int contactCount() {
        return contactCount;
    }

    public float footprintScale() {
        return footprintScale;
    }

    public static MobRideRig byId(String id) {
        if (id != null) {
            for (MobRideRig rig : values()) {
                if (rig.id.equals(id)) return rig;
            }
        }
        return GENERIC;
    }

    public static MobRideRig byNetworkId(int id) {
        MobRideRig[] values = values();
        return id >= 0 && id < values.length ? values[id] : GENERIC;
    }
}
