package com.herberto.jetsetcraft.mob;

import net.minecraft.resources.ResourceLocation;

/**
 * Data-driven compatibility metadata for one source-owned mob. No optional provider class is linked here.
 */
public record MobCompatibilityProfile(
        ResourceLocation entityId,
        ResourceLocation gangId,
        ResourceLocation crewFamilyId,
        String rideProfile,
        String challengeProfile,
        String graffitiMotif
) {
    public MobCompatibilityProfile {
        if (entityId == null) throw new IllegalArgumentException("entityId is required");
        if (gangId == null) throw new IllegalArgumentException("gangId is required");
        rideProfile = normalize(rideProfile, "generic");
        challengeProfile = normalize(challengeProfile, "default");
        graffitiMotif = graffitiMotif == null ? "" : graffitiMotif.trim();
    }

    public MobRideRig broadRig() {
        String value = rideProfile;
        if (value.contains("aquatic")) return MobRideRig.AQUATIC;
        if (value.contains("flight") || value.contains("hover") || value.contains("aerial")) return MobRideRig.AERIAL;
        if (value.contains("multi_leg")) return MobRideRig.MULTI_LEG;
        if (value.contains("serpentine") || value.contains("contact_plane") || value.contains("body_contact")) {
            return MobRideRig.BODY_CONTACT;
        }
        if (value.contains("quadruped") || value.contains("amphibious")) return MobRideRig.QUADRUPED;
        if (value.contains("biped") || value.contains("humanoid")) return MobRideRig.BIPED;
        return MobRideRig.GENERIC;
    }

    public boolean bossGated() {
        return challengeProfile.contains("boss-gated");
    }

    public boolean ownerGated() {
        return challengeProfile.contains("owner-gated");
    }

    private static String normalize(String value, String fallback) {
        if (value == null || value.isBlank()) return fallback;
        return value.trim().toLowerCase(java.util.Locale.ROOT);
    }
}
