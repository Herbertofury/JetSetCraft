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
        public final ForgeConfigSpec.BooleanValue allowGroundStunts;
        public final ForgeConfigSpec.BooleanValue allowBoostTricks;
        public final ForgeConfigSpec.BooleanValue allowDancing;
        public final ForgeConfigSpec.BooleanValue enableCyphers;
        public final ForgeConfigSpec.DoubleValue cypherRadius;
        public final ForgeConfigSpec.DoubleValue styleBoostScale;
        public final ForgeConfigSpec.BooleanValue allowCombatWhileRiding;
        public final ForgeConfigSpec.BooleanValue enableVanillaWorldPhysics;
        public final ForgeConfigSpec.DoubleValue blueIceSpeedMultiplier;
        public final ForgeConfigSpec.DoubleValue slimeBounceMultiplier;
        public final ForgeConfigSpec.DoubleValue poweredRailBoostPerTick;
        public final ForgeConfigSpec.DoubleValue unpoweredRailRetention;
        public final ForgeConfigSpec.BooleanValue enableMicroTerrainAssist;
        public final ForgeConfigSpec.DoubleValue microTerrainMaxStep;
        public final ForgeConfigSpec.BooleanValue allowGraffiti;
        public final ForgeConfigSpec.IntValue maxGraffitiPerChunk;
        public final ForgeConfigSpec.IntValue boomboxMaxActors;
        public final ForgeConfigSpec.IntValue boomboxChallengeLifetimeTicks;
        public final ForgeConfigSpec.DoubleValue boomboxSpawnRadius;

        private Server(ForgeConfigSpec.Builder b) {
            b.push("movement");
            speedScale = b.comment("Global JetSetCraft speed multiplier.").defineInRange("speedScale", 1.0, 0.25, 3.0);
            boostDrainPerTick = b.defineInRange("boostDrainPerTick", 0.80, 0.05, 10.0);
            boostRechargePerTick = b.defineInRange("boostRechargePerTick", 0.18, 0.0, 5.0);
            grindSnapRadius = b.defineInRange("grindSnapRadius", 0.62, 0.20, 1.40);
            grindVerticalTolerance = b.defineInRange("grindVerticalTolerance", 0.76, 0.20, 1.60);
            allowEdgeGrinding = b.define("allowEdgeGrinding", true);
            allowRailGrinding = b.comment("Prefer real rail/track paths over incidental block edges.")
                    .define("allowRailGrinding", true);
            allowRailTricks = b.comment("Allow tricks, hops and transfers while actively grinding.")
                    .define("allowRailTricks", true);
            allowWallRides = b.define("allowWallRides", true);
            b.pop();

            b.push("styleFlow");
            allowGroundStunts = b.comment("Allow contextual breakdance power moves and freezes on the ground.")
                    .define("allowGroundStunts", true);
            allowBoostTricks = b.comment("Allow holding Boost while pressing Trick for higher-risk, higher-value boost tricks.")
                    .define("allowBoostTricks", true);
            allowDancing = b.comment("Allow the six selectable street-dance styles even without ride gear equipped.")
                    .define("allowDancing", true);
            enableCyphers = b.comment("Nearby dancing players form cyphers that improve style and boost rewards.")
                    .define("enableCyphers", true);
            cypherRadius = b.comment("Maximum block radius used to detect other dancers in a cypher.")
                    .defineInRange("cypherRadius", 8.0, 2.0, 24.0);
            styleBoostScale = b.comment("Multiplier for boost earned from tricks, clean landings and cyphers.")
                    .defineInRange("styleBoostScale", 1.0, 0.0, 4.0);
            b.pop();

            b.push("vanillaWorldPhysics");
            enableVanillaWorldPhysics = b.comment("Compose momentum with Minecraft materials, redstone, fluids, effects and enchantments.")
                    .define("enableVanillaWorldPhysics", true);
            blueIceSpeedMultiplier = b.comment("Extreme blue-ice top-speed multiplier, intentionally evoking boats on blue ice.")
                    .defineInRange("blueIceSpeedMultiplier", 2.15, 1.0, 4.0);
            slimeBounceMultiplier = b.comment("Preserved vertical impact multiplier when landing on slime.")
                    .defineInRange("slimeBounceMultiplier", 0.92, 0.1, 2.0);
            poweredRailBoostPerTick = b.comment("Momentum added each tick on a powered powered-rail.")
                    .defineInRange("poweredRailBoostPerTick", 0.030, 0.0, 0.20);
            unpoweredRailRetention = b.comment("Momentum retained on an unpowered powered-rail.")
                    .defineInRange("unpoweredRailRetention", 0.90, 0.20, 1.0);
            enableMicroTerrainAssist = b.comment("Continue across collision-verified slabs, stairs and other small rises.")
                    .define("enableMicroTerrainAssist", true);
            microTerrainMaxStep = b.comment("Maximum rise eligible for micro-terrain continuation.")
                    .defineInRange("microTerrainMaxStep", 0.625, 0.25, 0.75);
            b.pop();

            b.push("compatibility");
            allowCombatWhileRiding = b.comment("Never suppress normal or third-party weapon input while riding.")
                    .define("allowCombatWhileRiding", true);
            b.pop();
            b.push("graffiti");
            allowGraffiti = b.define("allowGraffiti", true);
            maxGraffitiPerChunk = b.comment("Maximum persistent JetSetCraft decals in one chunk. Repainting an existing patch does not consume another slot.")
                    .defineInRange("maxGraffitiPerChunk", 128, 1, 1024);
            b.pop();

            b.push("gangs");
            boomboxMaxActors = b.comment("Maximum event-only gang actors one Boombox challenge may create. No chunk loading is forced.")
                    .defineInRange("boomboxMaxActors", 7, 1, 24);
            boomboxChallengeLifetimeTicks = b.comment("Hard cleanup lifetime for Boombox event casts in ticks. This is not a player cooldown.")
                    .defineInRange("boomboxChallengeLifetimeTicks", 20 * 180, 20 * 30, 20 * 900);
            boomboxSpawnRadius = b.comment("Maximum radius used to search loaded terrain for safe cinematic entrance points.")
                    .defineInRange("boomboxSpawnRadius", 12.0, 4.0, 24.0);
            b.pop();
        }
    }

    public static final class Client {
        public final ForgeConfigSpec.BooleanValue dynamicCamera;
        public final ForgeConfigSpec.BooleanValue dynamicFov;
        public final ForgeConfigSpec.DoubleValue cameraRollScale;
        public final ForgeConfigSpec.DoubleValue maxExtraFov;
        public final ForgeConfigSpec.DoubleValue boostExtraFov;
        public final ForgeConfigSpec.BooleanValue showStyleHud;
        public final ForgeConfigSpec.BooleanValue showTrickNames;
        public final ForgeConfigSpec.BooleanValue reducedMotion;

        private Client(ForgeConfigSpec.Builder b) {
            b.push("camera");
            dynamicCamera = b.comment("Lean the camera while grinding, powersliding and wall riding.")
                    .define("dynamicCamera", true);
            dynamicFov = b.comment("Add speed-sensitive field of view without overriding configured FOV.")
                    .define("dynamicFov", true);
            cameraRollScale = b.defineInRange("cameraRollScale", 1.0, 0.0, 2.0);
            maxExtraFov = b.defineInRange("maxExtraFov", 7.0, 0.0, 24.0);
            boostExtraFov = b.defineInRange("boostExtraFov", 4.0, 0.0, 18.0);
            reducedMotion = b.comment("Disable camera roll/FOV pulses and reduce rapid ride-gear stunt rotations.")
                    .define("reducedMotion", false);
            b.pop();
            b.push("hud");
            showStyleHud = b.comment("Show the JetSetCraft boost, flow, combo, trick and cypher HUD.")
                    .define("showStyleHud", true);
            showTrickNames = b.comment("Show contextual named tricks and landing grades.")
                    .define("showTrickNames", true);
            b.pop();
        }
    }

    private JetSetConfig() {}
}
