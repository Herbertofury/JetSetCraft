package com.herberto.jetsetcraft.crew;

import java.util.Optional;

/** Named mission-roster identities, not registered Minecraft entity types or copied character assets. */
public final class NamedCrewRecruits {
    public static final String LUCY = "jetsetcraft:lucy_lacemaker";
    public static Optional<String> name(String id) {
        return LUCY.equals(id) ? Optional.of("Lucy Lacemaker") : Optional.empty();
    }
    public static Optional<String> affiliation(String id) {
        return LUCY.equals(id) ? Optional.of("jetsetcraft:amygdala") : Optional.empty();
    }
    private NamedCrewRecruits() {}
}
