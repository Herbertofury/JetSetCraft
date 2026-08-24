package com.herberto.jetsetcraft.movement;

import com.herberto.jetsetcraft.JetSetCraft;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.fml.ModList;

import java.util.Optional;

/** Loads the Create-specific provider only when Create is actually present, keeping Create optional at runtime. */
final class CreateRailBridge {
    private static volatile GrindRailProvider provider;
    private static volatile boolean attempted;

    static Optional<GrindTarget> findBest(Player player, Vec3 preferredDirection, double radius, double verticalTolerance) {
        GrindRailProvider p = provider();
        return p == null ? Optional.empty() : p.findBest(player, preferredDirection, radius, verticalTolerance);
    }

    private static GrindRailProvider provider() {
        if (attempted) return provider;
        synchronized (CreateRailBridge.class) {
            if (attempted) return provider;
            attempted = true;
            if (!ModList.get().isLoaded("create")) return null;
            try {
                Class<?> clazz = Class.forName("com.herberto.jetsetcraft.compat.create.CreateRailProvider");
                provider = (GrindRailProvider) clazz.getDeclaredConstructor().newInstance();
                JetSetCraft.LOGGER.info("Create track grinding enabled via native ITrackBlock/Bezier track geometry");
            } catch (ReflectiveOperationException | RuntimeException | LinkageError t) {
                JetSetCraft.LOGGER.error("Create is loaded, but JetSetCraft could not initialize Create track grinding", t);
            }
            return provider;
        }
    }

    private CreateRailBridge() {}
}
