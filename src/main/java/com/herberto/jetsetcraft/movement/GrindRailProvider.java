package com.herberto.jetsetcraft.movement;

import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;

import java.util.Optional;

/**
 * Optional integration point for rail/track mods. Implementations must return world-space rail-surface points.
 * Providers are queried server-side only and should cheaply reject distant geometry before doing expensive work.
 */
public interface GrindRailProvider {
    Optional<GrindTarget> findBest(Player player, Vec3 preferredDirection, double snapRadius, double verticalTolerance);
}
