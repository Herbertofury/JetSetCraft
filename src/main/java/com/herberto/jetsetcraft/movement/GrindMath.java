package com.herberto.jetsetcraft.movement;

import net.minecraft.world.phys.Vec3;

final class GrindMath {
    static GrindTarget segment(Vec3 feet, Vec3 preferred, Vec3 a, Vec3 b, Vec3 normal,
                               double radius, double verticalTolerance, GrindKind kind) {
        Vec3 ab = b.subtract(a);
        double lenSq = ab.lengthSqr();
        if (lenSq < 1.0e-8) return null;
        double t = clamp(feet.subtract(a).dot(ab) / lenSq, 0.0, 1.0);
        Vec3 point = a.add(ab.scale(t));
        double dy = Math.abs(feet.y - point.y);
        if (dy > verticalTolerance) return null;
        double horizontalSq = sq(feet.x - point.x) + sq(feet.z - point.z);
        if (horizontalSq > radius * radius) return null;
        GrindTarget target = new GrindTarget(point, ab, normal, point.distanceToSqr(feet), kind).orientedTo(preferred);
        if (preferred != null && preferred.lengthSqr() > 1.0e-7 && target.alignment(preferred) < 0.15) return null;
        return target;
    }

    static double score(GrindTarget target, Vec3 preferred, GrindKind preferredKind) {
        double alignmentPenalty = (1.0 - target.alignment(preferred)) * (target.kind().rail() ? 0.18 : 0.34);
        double kindBias = target.kind().rail() ? -0.16 : 0.0;
        if (preferredKind != null && preferredKind != GrindKind.NONE) {
            if (target.kind() == preferredKind) kindBias -= 0.20;
            else if (preferredKind.rail() && !target.kind().rail()) kindBias += 0.42;
            else if (!preferredKind.rail() && target.kind().rail()) kindBias += 0.06;
        }
        return target.distanceSq() + alignmentPenalty + kindBias;
    }

    static Vec3 quadratic(Vec3 a, Vec3 control, Vec3 b, double t) {
        double u = 1.0 - t;
        return a.scale(u * u).add(control.scale(2.0 * u * t)).add(b.scale(t * t));
    }

    static Vec3 quadraticDerivative(Vec3 a, Vec3 control, Vec3 b, double t) {
        return control.subtract(a).scale(2.0 * (1.0 - t)).add(b.subtract(control).scale(2.0 * t));
    }

    static double clamp(double value, double min, double max) { return Math.max(min, Math.min(max, value)); }
    private static double sq(double v) { return v * v; }
    private GrindMath() {}
}
