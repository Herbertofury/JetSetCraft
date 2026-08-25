package com.herberto.jetsetcraft.movement;

import com.herberto.jetsetcraft.data.JetSetData;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.phys.Vec3;

final class MovementMath {
    static Vec3 desiredDirection(ServerPlayer p, JetSetData d) {
        double yaw = Math.toRadians(p.getYRot());
        Vec3 f = new Vec3(-Math.sin(yaw),0,Math.cos(yaw));
        Vec3 r = new Vec3(f.z,0,-f.x);
        return f.scale(d.inputForward()).add(r.scale(-d.inputStrafe()));
    }

    static Vec3 horizontalLook(ServerPlayer p) {
        return safeNormalize(EdgeFinder.horizontal(p.getLookAngle()), new Vec3(0,0,1));
    }

    static float wallSide(ServerPlayer p, Vec3 normal) {
        Vec3 look = horizontalLook(p);
        Vec3 right = new Vec3(look.z, 0, -look.x);
        double dot = safeNormalize(normal, new Vec3(1,0,0)).dot(right);
        return dot >= 0.0 ? 1.0f : -1.0f;
    }

    static Vec3 safeNormalize(Vec3 v, Vec3 fallback) {
        return v != null && v.lengthSqr() > 1e-7 ? v.normalize() : fallback;
    }

    static double signedHorizontalAngle(Vec3 from, Vec3 to) {
        Vec3 a = safeNormalize(new Vec3(from.x, 0, from.z), new Vec3(0, 0, 1));
        Vec3 b = safeNormalize(new Vec3(to.x, 0, to.z), a);
        return Math.atan2(a.x * b.z - a.z * b.x, a.dot(b));
    }

    static Vec3 rotateHorizontal(Vec3 vector, double radians) {
        double cos = Math.cos(radians);
        double sin = Math.sin(radians);
        return new Vec3(vector.x * cos - vector.z * sin, 0,
                vector.z * cos + vector.x * sin);
    }

    private MovementMath() {}
}
