package com.herberto.jetsetcraft.config;

import net.minecraftforge.common.ForgeConfigSpec;

public final class JetSetConfig {
    public static final ForgeConfigSpec SERVER_SPEC;
    public static final Server SERVER;
    public static final ForgeConfigSpec CLIENT_SPEC;
    public static final Client CLIENT;

    static {
        ForgeConfigSpec.Builder serverBuilder = new ForgeConfigSpec.Builder();
        SERVER = new Server(serverBuilder);
        SERVER_SPEC = serverBuilder.build();

        ForgeConfigSpec.Builder clientBuilder = new ForgeConfigSpec.Builder();
        CLIENT = new Client(clientBuilder);
        CLIENT_SPEC = clientBuilder.build();
    }

    public static final class Server {
        public final ForgeConfigSpec.DoubleValue speedScale;
        public final ForgeConfigSpec.DoubleValue boostDrainPerTick;
        public final ForgeConfigSpec.DoubleValue boostRechargePerTick;
        public final ForgeConfigSpec.DoubleValue grindSnapRadius;
        public final ForgeConfigSpec.DoubleValue grindVerticalTolerance;
        public final ForgeConfigSpec.BooleanValue allowEdgeGrinding;
        public final ForgeConfigSpec.BooleanValue allowRailGrinding;
        public final ForgeConfigSpec.BooleanValue allowRailTricks;
        public final ForgeConfigSpec.BooleanValue allowWallRides;
        public final ForgeConfigSpec.BooleanValue allowCombatWhileRiding;
        public final ForgeConfigSpec.BooleanValue enableVanillaWorldPhysics;
        public final ForgeConfigSpec.DoubleValue blueIceSpeedMultiplier;
        public final ForgeConfigSpec.DoubleValue slimeBounceMultiplier;
        public final ForgeConfigSpec.DoubleValue poweredRailBoostPerTick;
        public final ForgeConfigSpec.DoubleValue unpoweredRailRetention;
        public final ForgeConfigSpec.BooleanValue allowGraffiti;

        private Server(ForgeConfigSpec.Builder b) {
            b.push("movement");
            speedScale = b.comment("Global JetSetCraft speed multiplier.").defineInRange("speedScale", 1.0, 0.25, 3.0);
            boostDrainPerTick = b.defineInRange("boostDrainPerTick", 0.80, 0.05, 10.0);
            boostRechargePerTick = b.defineInRange("boostRechargePerTick", 0.18, 0.0, 5.0);
            grindSnapRadius = b.defineInRange("grindSnapRadius", 0.62, 0.20, 1.40);
            grindVerticalTolerance = b.defineInRange("grindVerticalTolerance", 0.76, 0.20, 1.60);
            allowEdgeGrinding = b.define("allowEdgeGrinding", true);
            allowRailGrinding = b.comment("Prefer real rail/track paths (vanilla/modded rails and supported track APIs) over generic block edges.")
                    .define("allowRailGrinding", true);
            allowRailTricks = b.comment("Allow trick inputs, hops and transfers while actively grinding a rail/track.")
                    .define("allowRailTricks", true);
            allowWallRides = b.define("allowWallRides", true);
            b.pop();
            b.push("vanillaWorldPhysics");
            enableVanillaWorldPhysics = b.comment("Compose JetSetCraft momentum with Minecraft block materials, redstone, fluids, effects and enchantments.")
                    .define("enableVanillaWorldPhysics", true);
            blueIceSpeedMultiplier = b.comment("Extreme blue-ice top-speed multiplier. Intentionally evokes boats on blue ice instead of normal ground.")
                    .defineInRange("blueIceSpeedMultiplier", 2.15, 1.0, 4.0);
            slimeBounceMultiplier = b.comment("Multiplier applied to preserved vertical impact speed when a rider lands on slime.")
                    .defineInRange("slimeBounceMultiplier", 0.92, 0.1, 2.0);
            poweredRailBoostPerTick = b.comment("Momentum added per tick while grinding a powered powered-rail.")
                    .defineInRange("poweredRailBoostPerTick", 0.030, 0.0, 0.20);
            unpoweredRailRetention = b.comment("Momentum retained per tick while grinding an unpowered powered-rail.")
                    .defineInRange("unpoweredRailRetention", 0.90, 0.20, 1.0);
            b.pop();
            b.push("compatibility");
            allowCombatWhileRiding = b.comment("Never suppress normal/third-party weapon input while a ride style is active.")
                    .define("allowCombatWhileRiding", true);
            b.pop();
            b.push("graffiti");
            allowGraffiti = b.define("allowGraffiti", true);
            b.pop();
        }
    }

    public static final class Client {
        public final ForgeConfigSpec.BooleanValue dynamicCamera;
        public final ForgeConfigSpec.BooleanValue dynamicFov;
        public final ForgeConfigSpec.DoubleValue cameraRollScale;
        public final ForgeConfigSpec.DoubleValue maxExtraFov;
        public final ForgeConfigSpec.DoubleValue boostExtraFov;

        private Client(ForgeConfigSpec.Builder b) {
            b.push("camera");
            dynamicCamera = b.comment("Lean the camera while grinding, powersliding and wall riding.")
                    .define("dynamicCamera", true);
            dynamicFov = b.comment("Add speed-sensitive field of view without overriding the player's configured FOV.")
                    .define("dynamicFov", true);
            cameraRollScale = b.defineInRange("cameraRollScale", 1.0, 0.0, 2.0);
            maxExtraFov = b.defineInRange("maxExtraFov", 7.0, 0.0, 24.0);
            boostExtraFov = b.defineInRange("boostExtraFov", 4.0, 0.0, 18.0);
            b.pop();
        }
    }

    private JetSetConfig() {}
}
