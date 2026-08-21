package com.herberto.jetsetcraft.movement;

import com.herberto.jetsetcraft.config.JetSetConfig;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;

import java.util.Optional;

/** Selects the best grind path, deliberately preferring real rails/tracks over incidental block edges. */
public final class GrindFinder {
    private static final VanillaRailFinder VANILLA_RAILS = new VanillaRailFinder();

    public static Optional<GrindTarget> findBest(Player player, Vec3 preferredDirection, GrindKind preferredKind) {
        double radius = JetSetConfig.SERVER.grindSnapRadius.get();
        double verticalTolerance = JetSetConfig.SERVER.grindVerticalTolerance.get();
        GrindTarget best = null;
        double bestScore = Double.POSITIVE_INFINITY;

        if (JetSetConfig.SERVER.allowRailGrinding.get()) {
            Optional<GrindTarget> vanilla = VANILLA_RAILS.findBest(player, preferredDirection, radius, verticalTolerance);
            if (vanilla.isPresent()) {
                best = vanilla.get();
                bestScore = GrindMath.score(best, preferredDirection, preferredKind);
            }
            Optional<GrindTarget> create = CreateRailBridge.findBest(player, preferredDirection, radius, verticalTolerance);
            if (create.isPresent()) {
                double score = GrindMath.score(create.get(), preferredDirection, preferredKind);
                if (score < bestScore) {
                    best = create.get();
                    bestScore = score;
                }
            }
        }

        if (JetSetConfig.SERVER.allowEdgeGrinding.get()) {
            Optional<EdgeFinder.Edge> edge = EdgeFinder.findBest(player, preferredDirection);
            if (edge.isPresent()) {
                EdgeFinder.Edge e = edge.get();
                GrindTarget target = new GrindTarget(e.point(), e.tangent(), new Vec3(0, 1, 0), e.distanceSq(), GrindKind.EDGE)
                        .orientedTo(preferredDirection);
                double score = GrindMath.score(target, preferredDirection, preferredKind);
                if (score < bestScore) best = target;
            }
        }
        return Optional.ofNullable(best);
    }

    private GrindFinder() {}
}
