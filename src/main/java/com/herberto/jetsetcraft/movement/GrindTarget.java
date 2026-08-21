package com.herberto.jetsetcraft.movement;

import net.minecraft.world.phys.Vec3;

/** A sampled grind surface plus local path geometry. Curvature is 0 for straight paths and rises on tighter turns. */
public record GrindTarget(Vec3 point, Vec3 tangent, Vec3 normal, double distanceSq, GrindKind kind, double curvature) {
    public GrindTarget(Vec3 point, Vec3 tangent, Vec3 normal, double distanceSq, GrindKind kind) {
        this(point, tangent, normal, distanceSq, kind, 0.0);
    }

    public GrindTarget {
        tangent = normalizeOr(tangent, new Vec3(0, 0, 1));
        normal = normalizeOr(normal, new Vec3(0, 1, 0));
        kind = kind == null ? GrindKind.NONE : kind;
        distanceSq = Math.max(0.0, distanceSq);
        curvature = Math.max(0.0, Math.min(1.0, curvature));
    }

    public GrindTarget orientedTo(Vec3 preferred) {
        Vec3 p = preferred == null ? Vec3.ZERO : preferred;
        if (p.lengthSqr() > 1.0e-7 && tangent.dot(p) < 0.0)
            return new GrindTarget(point, tangent.scale(-1), normal, distanceSq, kind, curvature);
        return this;
    }

    public double alignment(Vec3 preferred) {
        if (preferred == null || preferred.lengthSqr() < 1.0e-7) return 1.0;
        return Math.abs(tangent.dot(preferred.normalize()));
    }

    private static Vec3 normalizeOr(Vec3 value, Vec3 fallback) {
        return value != null && value.lengthSqr() > 1.0e-9 ? value.normalize() : fallback;
    }
}
