package com.herberto.jetsetcraft.movement;

import com.herberto.jetsetcraft.JetSetCraft;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseRailBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.Property;
import net.minecraft.world.level.block.state.properties.RailShape;
import net.minecraft.world.phys.Vec3;

import java.util.Optional;

/** Handles every Forge rail that follows BaseRailBlock plus datapack-opted rail-like blocks. */
public final class VanillaRailFinder implements GrindRailProvider {
    public static final TagKey<Block> GRIND_RAILS = TagKey.create(Registries.BLOCK,
            ResourceLocation.fromNamespaceAndPath(JetSetCraft.MOD_ID, "grind_rails"));
    public static final TagKey<Block> GRIND_RAIL_BLACKLIST = TagKey.create(Registries.BLOCK,
            ResourceLocation.fromNamespaceAndPath(JetSetCraft.MOD_ID, "grind_rail_blacklist"));
    private static final TagKey<Block> VANILLA_RAILS = TagKey.create(Registries.BLOCK,
            ResourceLocation.fromNamespaceAndPath("minecraft", "rails"));
    private static final Vec3 UP = new Vec3(0, 1, 0);

    @Override
    public Optional<GrindTarget> findBest(Player player, Vec3 preferredDirection, double snapRadius, double verticalTolerance) {
        Level level = player.level();
        Vec3 feet = new Vec3(player.getX(), player.getY() + 0.08, player.getZ());
        int radius = Math.max(1, (int)Math.ceil(snapRadius + 0.85));
        BlockPos base = BlockPos.containing(feet);
        GrindTarget best = null;
        double bestScore = Double.POSITIVE_INFINITY;
        BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos();

        for (int dx = -radius; dx <= radius; dx++) {
            for (int dy = -2; dy <= 1; dy++) {
                for (int dz = -radius; dz <= radius; dz++) {
                    pos.set(base.getX() + dx, base.getY() + dy, base.getZ() + dz);
                    BlockState state = level.getBlockState(pos);
                    if (state.is(GRIND_RAIL_BLACKLIST)) continue;
                    boolean railLike = state.getBlock() instanceof BaseRailBlock || state.is(VANILLA_RAILS) || state.is(GRIND_RAILS);
                    if (!railLike) continue;

                    RailShape shape = railShape(level, pos, state);
                    if (shape == null) continue;
                    GrindTarget candidate = targetForShape(feet, preferredDirection, pos.immutable(), shape,
                            snapRadius, verticalTolerance, state.is(GRIND_RAILS) && !(state.getBlock() instanceof BaseRailBlock)
                                    ? GrindKind.CUSTOM_RAIL : GrindKind.VANILLA_RAIL);
                    if (candidate == null) continue;
                    double score = GrindMath.score(candidate, preferredDirection, GrindKind.NONE);
                    if (score < bestScore) {
                        best = candidate;
                        bestScore = score;
                    }
                }
            }
        }
        return Optional.ofNullable(best);
    }

    private static RailShape railShape(Level level, BlockPos pos, BlockState state) {
        if (state.getBlock() instanceof BaseRailBlock rail)
            return rail.getRailDirection(state, level, pos, null);

        // Datapack-opted compatibility fallback for rail-like blocks that expose a vanilla RailShape property.
        for (Property<?> property : state.getProperties()) {
            Comparable<?> value = getValueUnchecked(state, property);
            if (value instanceof RailShape railShape) return railShape;
        }
        return null;
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    private static Comparable<?> getValueUnchecked(BlockState state, Property<?> property) {
        return state.getValue((Property) property);
    }

    private static GrindTarget targetForShape(Vec3 feet, Vec3 preferred, BlockPos pos, RailShape shape,
                                               double radius, double verticalTolerance, GrindKind kind) {
        double y = pos.getY() + 0.125;
        double x0 = pos.getX();
        double x1 = x0 + 1.0;
        double z0 = pos.getZ();
        double z1 = z0 + 1.0;
        double cx = x0 + 0.5;
        double cz = z0 + 0.5;
        Vec3 a;
        Vec3 b;

        return switch (shape) {
            case NORTH_SOUTH -> GrindMath.segment(feet, preferred, new Vec3(cx, y, z0), new Vec3(cx, y, z1), UP,
                    radius, verticalTolerance, kind);
            case EAST_WEST -> GrindMath.segment(feet, preferred, new Vec3(x0, y, cz), new Vec3(x1, y, cz), UP,
                    radius, verticalTolerance, kind);
            case ASCENDING_EAST -> GrindMath.segment(feet, preferred, new Vec3(x0, y, cz), new Vec3(x1, y + 1.0, cz),
                    slopeNormal(new Vec3(1, 1, 0)), radius, verticalTolerance, kind);
            case ASCENDING_WEST -> GrindMath.segment(feet, preferred, new Vec3(x0, y + 1.0, cz), new Vec3(x1, y, cz),
                    slopeNormal(new Vec3(-1, 1, 0)), radius, verticalTolerance, kind);
            case ASCENDING_NORTH -> GrindMath.segment(feet, preferred, new Vec3(cx, y + 1.0, z0), new Vec3(cx, y, z1),
                    slopeNormal(new Vec3(0, 1, -1)), radius, verticalTolerance, kind);
            case ASCENDING_SOUTH -> GrindMath.segment(feet, preferred, new Vec3(cx, y, z0), new Vec3(cx, y + 1.0, z1),
                    slopeNormal(new Vec3(0, 1, 1)), radius, verticalTolerance, kind);
            case SOUTH_EAST -> quadratic(feet, preferred, new Vec3(cx, y, z1), new Vec3(x1, y, z1),
                    new Vec3(x1, y, cz), radius, verticalTolerance, kind);
            case SOUTH_WEST -> quadratic(feet, preferred, new Vec3(cx, y, z1), new Vec3(x0, y, z1),
                    new Vec3(x0, y, cz), radius, verticalTolerance, kind);
            case NORTH_WEST -> quadratic(feet, preferred, new Vec3(cx, y, z0), new Vec3(x0, y, z0),
                    new Vec3(x0, y, cz), radius, verticalTolerance, kind);
            case NORTH_EAST -> quadratic(feet, preferred, new Vec3(cx, y, z0), new Vec3(x1, y, z0),
                    new Vec3(x1, y, cz), radius, verticalTolerance, kind);
        };
    }

    private static GrindTarget quadratic(Vec3 feet, Vec3 preferred, Vec3 a, Vec3 control, Vec3 b,
                                         double radius, double verticalTolerance, GrindKind kind) {
        GrindTarget best = null;
        double bestScore = Double.POSITIVE_INFINITY;
        final int samples = 14;
        Vec3 previous = a;
        for (int i = 1; i <= samples; i++) {
            double t = i / (double)samples;
            Vec3 current = GrindMath.quadratic(a, control, b, t);
            GrindTarget target = GrindMath.segment(feet, preferred, previous, current, UP,
                    radius, verticalTolerance, kind);
            if (target != null) {
                double score = GrindMath.score(target, preferred, GrindKind.NONE);
                if (score < bestScore) {
                    double segmentMidT = (i - 0.5) / samples;
                    Vec3 tangent = GrindMath.quadraticDerivative(a, control, b, segmentMidT);
                    Vec3 beforeTangent = GrindMath.quadraticDerivative(a, control, b, Math.max(0.0, segmentMidT - 0.08));
                    Vec3 afterTangent = GrindMath.quadraticDerivative(a, control, b, Math.min(1.0, segmentMidT + 0.08));
                    double curvature = curvature(beforeTangent, afterTangent);
                    best = new GrindTarget(target.point(), tangent, UP, target.distanceSq(), kind, curvature).orientedTo(preferred);
                    bestScore = score;
                }
            }
            previous = current;
        }
        return best;
    }

    private static double curvature(Vec3 a, Vec3 b) {
        if (a.lengthSqr() < 1.0e-8 || b.lengthSqr() < 1.0e-8) return 0.0;
        return Math.max(0.0, 1.0 - Math.max(-1.0, Math.min(1.0, a.normalize().dot(b.normalize()))));
    }

    private static Vec3 slopeNormal(Vec3 tangent) {
        Vec3 horizontalSide = new Vec3(-tangent.z, 0, tangent.x);
        Vec3 normal = horizontalSide.cross(tangent);
        return normal.y < 0 ? normal.scale(-1).normalize() : normal.normalize();
    }
}
