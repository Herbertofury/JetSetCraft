package com.herberto.jetsetcraft.gametest;

import com.herberto.jetsetcraft.JetSetCraft;
import net.minecraft.server.level.ServerLevel;
import net.minecraftforge.event.server.ServerStartedEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.fml.common.Mod;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

/**
 * Verification-only production-server hook. It is inert unless the acceptance JVM property is explicitly enabled,
 * and it reflectively isolates the Create-linked runner so ordinary ForgeGradle GameTests still run without Create.
 */
@Mod.EventBusSubscriber(modid = JetSetCraft.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public final class CreateProductionAcceptanceBootstrap {
    private static final String ENABLE_PROPERTY = "jetsetcraft.createAcceptance";

    @SubscribeEvent
    public static void serverStarted(ServerStartedEvent event) {
        if (!Boolean.getBoolean(ENABLE_PROPERTY)) return;
        if (!ModList.get().isLoaded("create")) {
            fail("Create is not loaded in the production acceptance server", null);
            return;
        }

        try {
            ServerLevel overworld = event.getServer().overworld();
            Class<?> runner = Class.forName("com.herberto.jetsetcraft.gametest.CreateProductionAcceptanceRunner");
            Method run = runner.getDeclaredMethod("run", ServerLevel.class);
            run.invoke(null, overworld);
        } catch (InvocationTargetException error) {
            fail("Create production acceptance runner threw", error.getCause() == null ? error : error.getCause());
        } catch (Throwable error) {
            fail("Create production acceptance runner could not execute", error);
        }
    }

    private static void fail(String message, Throwable error) {
        if (error == null) JetSetCraft.LOGGER.error("JETSETCRAFT_CREATE_RUNTIME_FAIL {}", message);
        else JetSetCraft.LOGGER.error("JETSETCRAFT_CREATE_RUNTIME_FAIL {}", message, error);
    }

    private CreateProductionAcceptanceBootstrap() {}
}
