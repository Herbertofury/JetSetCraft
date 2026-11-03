package com.herberto.jetsetcraft.client.rush;

import com.herberto.jetsetcraft.JetSetCraft;
import com.herberto.jetsetcraft.rush.RivalRushManager;
import com.herberto.jetsetcraft.rush.RushDirector;
import com.herberto.jetsetcraft.rush.RushRun;
import com.herberto.jetsetcraft.rush.RushWorld;
import net.minecraft.client.CameraType;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.Button;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.level.storage.LevelResource;
import net.minecraft.world.phys.Vec3;
import org.lwjgl.glfw.GLFW;

import java.nio.file.Path;
import java.util.Comparator;
import java.util.UUID;
import java.util.function.Consumer;
import java.util.function.Predicate;

/**
 * Native progression views, using production packets and real owned-rival rewards.
 * The exact disposable save and JVM opt-in guard every fixture. Rival placement/primary-hit
 * calls accelerate earning the first draft; only the active clock is advanced to schedule a
 * headliner. No XP, ranks, evolution, boss victory, receipt or client snapshot is fabricated.
 */
public final class RushProgressionVisualAudit {
    private static int phase, ticks, stableTicks, previousVersion, draftLevel;
    private static volatile int fixtureDefeats;
    private static volatile boolean fixturePending, clockApplied, facingApplied;
    private static volatile String fixtureFailure;
    private static String banishedId, originalDimension;
    private static UUID runId, chosenRun;
    private static int chosenVersion = Integer.MIN_VALUE;
    private static Vec3 originalPosition;
    private static ListTag originalInventory;
    private static int originalLevel, originalTotalXp;
    private static float originalProgress;
    private static CameraType originalCamera;

    public static boolean advance(Minecraft minecraft, Consumer<String> capture, Predicate<String> captured) {
        guardedServer(minecraft);
        require(minecraft.player != null && minecraft.level != null && minecraft.getConnection() != null,
                "Progression audit lost its connected player");
        if (fixtureFailure != null) throw new IllegalStateException(fixtureFailure);
        CompoundTag state = RivalRushClient.state(), draft = state.getCompound("Draft");
        if (phase >= 3 && phase <= 14)
            require(state.getBoolean("Active") && state.hasUUID("Run") && state.getUUID("Run").equals(runId),
                    "Progression audit lost its actual run during phase " + phase);
        switch (phase) {
            case 0 -> {
                require(!state.getBoolean("Active") && !RivalRushClient.active(), "Progression audit requires normal Minecraft first");
                if (minecraft.screen != null) minecraft.screen.onClose();
                RivalRushClient.request("menu", "", 0); next(1);
            }
            case 1 -> {
                if (!(minecraft.screen instanceof RivalRushScreen) || state.getBoolean("Active")) return false;
                originalDimension = minecraft.level.dimension().location().toString();
                originalPosition = minecraft.player.position();
                originalInventory = minecraft.player.getInventory().save(new ListTag()).copy();
                originalLevel = minecraft.player.experienceLevel; originalTotalXp = minecraft.player.totalExperience;
                originalProgress = minecraft.player.experienceProgress;
                originalCamera = minecraft.options.getCameraType();
                click(minecraft, "Start 30-minute run"); next(2);
            }
            case 2 -> {
                if (!state.getBoolean("Active") || !RivalRushClient.active()) return false;
                require(state.hasUUID("Run"), "Started run has no identity");
                runId = state.getUUID("Run");
                RivalRushClient.request("menu", "", 0); next(3);
            }
            case 3 -> {
                if (!(minecraft.screen instanceof RivalRushScreen)) { reopen(minecraft); return false; }
                click(minecraft, "Build"); next(4);
            }
            case 4 -> {
                if (!RivalRushClient.isBuildScreenOpen()) return false;
                click(minecraft, "Build paths"); next(5);
            }
            case 5 -> {
                if (!RivalRushClient.isBuildPathsScreenOpen()) return false;
                CompoundTag paths = state.getCompound("Build").getCompound("Paths");
                ListTag recipes = paths.getList("Recipes", Tag.TAG_COMPOUND);
                require(recipes.size() == 6 && paths.getString("Selected").equals("NONE") && !paths.getBoolean("Opportunity"),
                        "Initial build-path view invented an evolution or headliner award");
                for (var raw : recipes) {
                    CompoundTag recipe = (CompoundTag) raw;
                    require(!recipe.getString("Name").isBlank() && !recipe.getString("Description").isBlank()
                            && recipe.getList("Requirements", Tag.TAG_COMPOUND).size() == 2, "Recipe has no real description or requirements");
                    for (var requirement : recipe.getList("Requirements", Tag.TAG_COMPOUND))
                        require(((CompoundTag) requirement).getInt("RequiredRank") == 3, "Recipe requirement differs from the authored rank");
                }
                if (!captureReady(minecraft)) return false;
                JetSetCraft.LOGGER.info("JETSETCRAFT_PROGRESSION_AUDIT_RECIPES_OBSERVED run={} recipes={} forge={} selected={} requirements={}",
                        runId, recipes.size(), paths.getBoolean("Opportunity"), paths.getString("Selected"),
                        recipes.getCompound(0).getList("Requirements", Tag.TAG_COMPOUND));
                capture.accept("16-build-paths"); next(6);
            }
            case 6 -> {
                if (!captured.test("16-build-paths")) return false;
                minecraft.setScreen(null); next(7);
            }
            case 7 -> {
                if (!RivalRushClient.offers().isEmpty() && state.getLong("Cards") > 0) {
                    require(state.getInt("Level") >= 2 && state.getInt("Kills") > 0, "Draft was not earned from owned rivals and pickups");
                    RivalRushClient.request("menu", "", 0); next(8); return false;
                }
                if (chooseRealOffer(minecraft)) return false; // A genuine album starter choice is not pickup proof.
                require(fixtureDefeats < 8, "Eight owned rivals did not produce the expected genuine XP draft");
                if (++ticks % 10 == 0 && !fixturePending) queueOwnedRivalDefeat(minecraft);
            }
            case 8 -> {
                if (!RivalRushClient.isUpgradeScreenOpen()) { reopen(minecraft); return false; }
                require(draft.getInt("Rerolls") == 3 && draft.getInt("Banishes") == 2 && draft.getBoolean("CanReroll"),
                        "New run's real draft budgets are incorrect");
                for (var raw : RivalRushClient.offers()) require(!((CompoundTag) raw).getString("nextDescription").isBlank(),
                        "A genuine draft choice has no authoritative next-rank description");
                previousVersion = state.getInt("Version"); draftLevel = state.getInt("Level");
                clickStarting(minecraft, "Reroll "); next(9);
            }
            case 9 -> {
                if (state.getInt("Version") == previousVersion || draft.getInt("Rerolls") != 2) return false;
                require(state.getInt("Level") == draftLevel && !RivalRushClient.offers().isEmpty(), "Reroll consumed the pending level choice");
                banishedId = "";
                for (var raw : RivalRushClient.offers()) if (((CompoundTag) raw).getBoolean("banishable")) {
                    banishedId = ((CompoundTag) raw).getString("id"); break;
                }
                require(!banishedId.isBlank(), "The early draft has no banishable choice");
                previousVersion = state.getInt("Version");
                click(minecraft, "Banish"); next(10);
            }
            case 10 -> {
                if (state.getInt("Version") == previousVersion || draft.getInt("Banishes") != 1) return false;
                require(draft.getInt("Rerolls") == 2 && state.getInt("Level") == draftLevel
                        && draft.getList("Banished", Tag.TAG_COMPOUND).stream().anyMatch(raw -> ((CompoundTag) raw).getString("Id").equals(banishedId))
                        && RivalRushClient.offers().stream().noneMatch(raw -> ((CompoundTag) raw).getString("id").equals(banishedId)),
                        "Banish was not reflected in the actual budget, exclusion and offers");
                if (!RivalRushClient.isUpgradeScreenOpen() || !captureReady(minecraft)) return false;
                JetSetCraft.LOGGER.info("JETSETCRAFT_PROGRESSION_AUDIT_DRAFT_OBSERVED run={} level={} rerolls={} banishes={} excluded={} actualDefeats={} cards={}",
                        runId, draftLevel, draft.getInt("Rerolls"), draft.getInt("Banishes"), banishedId, state.getInt("Kills"), state.getLong("Cards"));
                capture.accept("17-draft-agency"); next(11);
            }
            case 11 -> {
                if (!captured.test("17-draft-agency")) return false;
                if (chooseRealOffer(minecraft)) return false;
                minecraft.setScreen(null);
                queueHeadlinerClock(minecraft); next(12);
            }
            case 12 -> {
                if (!clockApplied) return false;
                if (chooseRealOffer(minecraft)) return false;
                CompoundTag encounter = state.getCompound("Encounter");
                if (!encounter.getBoolean("Active") || !encounter.getString("Kind").equals("headliner")) return false;
                require(encounter.hasUUID("Actor") && encounter.getInt("DefeatedCount") == 0 && !encounter.getBoolean("ForgeEarned"),
                        "Headliner was not a live, undefeated real actor");
                queueFaceHeadliner(minecraft, encounter.getUUID("Actor"));
                minecraft.options.setCameraType(CameraType.THIRD_PERSON_BACK); minecraft.setScreen(null); next(13);
            }
            case 13 -> {
                if (!facingApplied) return false;
                if (chooseRealOffer(minecraft)) { stableTicks = 0; return false; }
                if (minecraft.screen != null) { minecraft.setScreen(null); stableTicks = 0; return false; }
                CompoundTag encounter = state.getCompound("Encounter");
                if (!encounter.getBoolean("Active") || !encounter.getString("Kind").equals("headliner")
                        || encounter.getInt("TicksUntilImpact") < 12 || encounter.getString("Attack").isBlank()
                        || encounter.getString("Counterplay").isBlank()) { stableTicks = 0; return false; }
                if (++stableTicks < 2) return false; // Render at least one genuine tell frame before grabbing the framebuffer.
                require(encounter.getDouble("Health") > 0 && encounter.getDouble("Health") <= encounter.getDouble("MaxHealth")
                        && encounter.getInt("Phase") >= 1 && encounter.getInt("Phase") <= 3, "Headliner HUD lacks real health/phase");
                JetSetCraft.LOGGER.info("JETSETCRAFT_PROGRESSION_AUDIT_HEADLINER_OBSERVED run={} name={} phase={} health={}/{} attack={} counterplay={} activeTicksToImpact={} (clock-compressed schedule; no victory or forge fabricated)",
                        runId, encounter.getString("Name"), encounter.getInt("Phase"), encounter.getDouble("Health"), encounter.getDouble("MaxHealth"),
                        encounter.getString("Attack"), encounter.getString("Counterplay"), encounter.getInt("TicksUntilImpact"));
                capture.accept("18-headliner-tell"); next(14);
            }
            case 14 -> {
                if (!captured.test("18-headliner-tell")) return false;
                RivalRushClient.request("leave", "", 0); next(15);
            }
            case 15 -> {
                if (state.getBoolean("Active") || !originalDimension.equals(minecraft.level.dimension().location().toString())) return false;
                if (++ticks < 20) return false;
                require(originalInventory.equals(minecraft.player.getInventory().save(new ListTag()))
                        && minecraft.player.experienceLevel == originalLevel && minecraft.player.totalExperience == originalTotalXp
                        && Math.abs(minecraft.player.experienceProgress - originalProgress) <= .000001F
                        && minecraft.player.position().distanceToSqr(originalPosition) <= 1,
                        "Progression run did not restore ordinary inventory/XP/position");
                minecraft.options.setCameraType(originalCamera);
                RivalRushClient.request("menu", "", 0); next(16);
            }
            case 16 -> {
                // Wait for the requested menu response, not the still-visible immediate debrief.
                if (!(minecraft.screen instanceof RivalRushScreen) || minecraft.screen.children().stream().noneMatch(child ->
                        child instanceof Button button && button.getMessage().getString().equals("Start 30-minute run"))) return false;
                click(minecraft, "Last run"); next(17);
            }
            case 17 -> {
                if (!RivalRushClient.isDebriefScreenOpen()) return false;
                CompoundTag result = state.getCompound("Debrief");
                require(result.getInt("Schema") == 1 && result.hasUUID("Run") && result.getUUID("Run").equals(runId)
                        && result.getLong("Defeats") > 0 && result.getLong("Cards") > 0 && result.getDouble("PrimaryDamage") > 0
                        && result.getLong("RerollsUsed") == 1 && result.getLong("BanishesUsed") == 1
                        && result.getLong("Headliners") == 0 && result.getLong("ArenaClears") == 0,
                        "Reopened last-run report lost real metrics or invented a clear");
                if (!captureReady(minecraft)) return false;
                JetSetCraft.LOGGER.info("JETSETCRAFT_PROGRESSION_AUDIT_DEBRIEF_OBSERVED run={} outcome={} activeTicks={} defeats={} cards={} primaryDamage={} returnAward={} receipt={} (real settlement after a logged clock/owned-hit fixture; not human-duration or boss-clear proof)",
                        runId, result.getString("Outcome"), result.getLong("Ticks"), result.getLong("Defeats"), result.getLong("Cards"),
                        result.getDouble("PrimaryDamage"), result.getString("ReturnRewardStatus"), result.getCompound("ReturnReward"));
                capture.accept("19-run-debrief"); next(18);
            }
            case 18 -> {
                if (!captured.test("19-run-debrief")) return false;
                JetSetCraft.LOGGER.info("JETSETCRAFT_PROGRESSION_VISUAL_AUDIT_PASS four real-client views; genuine recipe requirements; production owned-hit/pickup XP; actual reroll/banish packets; clock-compressed live headliner tell without victory; persisted report reopened; ordinary inventory/XP/position restored");
                next(19); return true;
            }
            case 19 -> { return true; }
            default -> throw new IllegalStateException("Unknown progression audit phase " + phase);
        }
        return false;
    }

    private static void queueOwnedRivalDefeat(Minecraft minecraft) {
        MinecraftServer server = guardedServer(minecraft); Path expected = expectedWorld(minecraft);
        UUID playerId = minecraft.player.getUUID(); fixturePending = true;
        server.execute(() -> {
            try {
                ServerPlayer player = fixturePlayer(server, expected, playerId); RushRun run = RivalRushManager.run(player);
                if (run.hasOffer()) return;
                var rivals = player.serverLevel().getEntitiesOfClass(Mob.class, player.getBoundingBox().inflate(48),
                                mob -> RivalRushManager.belongsTo(mob, player) && RivalRushManager.actorVitality(player, mob) > 0)
                        .stream().sorted(Comparator.comparingDouble(mob -> mob.distanceToSqr(player))).toList();
                if (rivals.isEmpty()) return;
                Mob target = rivals.get(0); long before = run.defeats();
                Vec3 original = target.position(), nearby = player.position().add(2, 0, 1);
                target.moveTo(nearby.x, nearby.y, nearby.z, target.getYRot(), 0); target.setDeltaMovement(Vec3.ZERO);
                require(player.hasLineOfSight(target), "Owned rival placement has no genuine tag sightline");
                int hits = 0;
                while (!target.isRemoved() && !run.hasOffer() && hits < 16) {
                    require(RivalRushManager.tagEnemy(player, target, 7), "Production primary tag rejected the owned rival"); hits++;
                }
                require(target.isRemoved() && run.defeats() == before + 1, "Production tag fixture did not settle exactly one actual rival");
                fixtureDefeats++;
                JetSetCraft.LOGGER.info("JETSETCRAFT_PROGRESSION_AUDIT_OWNED_HIT_FIXTURE run={} actor={} from={} to={} hits={} baseDamage=7 defeats={} (scene placement and normal damage handler only; pickup/XP earned by normal server loop)",
                        runId, target.getUUID(), original, nearby, hits, run.defeats());
            } catch (RuntimeException error) { failFixture("owned-hit fixture", error); }
            finally { fixturePending = false; }
        });
    }

    private static void queueHeadlinerClock(Minecraft minecraft) {
        MinecraftServer server = guardedServer(minecraft); Path expected = expectedWorld(minecraft);
        UUID playerId = minecraft.player.getUUID();
        server.execute(() -> {
            try {
                ServerPlayer player = fixturePlayer(server, expected, playerId); RushRun run = RivalRushManager.run(player);
                require(!run.hasOffer() && !run.awaitingEncore(), "Clock fixture requires a resolved genuine draft");
                long before = run.elapsedTicks();
                require(before < RushDirector.FREE_STREET_INTERVAL, "Unexpected headliner interval before clock fixture");
                for (long tick = before; tick < RushDirector.FREE_STREET_INTERVAL; tick++) run.tickActive();
                JetSetCraft.LOGGER.info("JETSETCRAFT_PROGRESSION_AUDIT_CLOCK_FIXTURE run={} activeTicks={} -> {} (model clock only; normal manager schedules/spawns headliner, no health/XP/defeats/forge/receipt assignment)",
                        runId, before, run.elapsedTicks());
                clockApplied = true;
            } catch (RuntimeException error) { failFixture("headliner clock fixture", error); }
        });
    }

    private static void queueFaceHeadliner(Minecraft minecraft, UUID actorId) {
        MinecraftServer server = guardedServer(minecraft); Path expected = expectedWorld(minecraft);
        UUID playerId = minecraft.player.getUUID();
        server.execute(() -> {
            try {
                ServerPlayer player = fixturePlayer(server, expected, playerId);
                var actor = player.serverLevel().getEntity(actorId);
                require(actor != null && RivalRushManager.belongsTo(actor, player), "Headliner facing fixture lost its real actor");
                Vec3 direction = actor.position().subtract(player.position());
                float yaw = (float) (Math.toDegrees(Math.atan2(direction.z, direction.x)) - 90);
                player.teleportTo(player.serverLevel(), player.getX(), player.getY(), player.getZ(), yaw, 0);
                JetSetCraft.LOGGER.info("JETSETCRAFT_PROGRESSION_AUDIT_CAMERA_FIXTURE run={} actor={} yaw={} (orientation only; player position and threat unchanged)", runId, actorId, yaw);
                facingApplied = true;
            } catch (RuntimeException error) { failFixture("headliner camera fixture", error); }
        });
    }

    private static ServerPlayer fixturePlayer(MinecraftServer server, Path expected, UUID playerId) {
        require(Boolean.getBoolean("jetsetcraft.rushVisualAudit")
                        && server.getWorldPath(LevelResource.ROOT).toAbsolutePath().normalize().equals(expected),
                "Progression fixture lost its exact disposable-world guard");
        ServerPlayer player = server.getPlayerList().getPlayer(playerId);
        require(player != null && RushWorld.isRush(player.level()) && RivalRushManager.run(player) != null
                        && RivalRushManager.run(player).id().equals(runId), "Progression fixture lost its actual player/run");
        return player;
    }

    private static MinecraftServer guardedServer(Minecraft minecraft) {
        MinecraftServer server = minecraft.getSingleplayerServer();
        require(Boolean.getBoolean("jetsetcraft.rushVisualAudit") && server != null
                        && server.getWorldPath(LevelResource.ROOT).toAbsolutePath().normalize().equals(expectedWorld(minecraft)),
                "Progression visual audit requires the exact disposable saves/RivalRushAudit integrated world");
        return server;
    }

    private static Path expectedWorld(Minecraft minecraft) {
        return minecraft.gameDirectory.toPath().resolve("saves/RivalRushAudit").toAbsolutePath().normalize();
    }

    private static void failFixture(String label, RuntimeException error) {
        fixtureFailure = label + " failed: " + error.getMessage();
        JetSetCraft.LOGGER.error("Progression visual audit fixture failed: " + label, error);
    }

    private static boolean chooseRealOffer(Minecraft minecraft) {
        if (RivalRushClient.offers().isEmpty()) return false;
        CompoundTag state = RivalRushClient.state();
        int version = state.getInt("Version");
        if (runId.equals(chosenRun) && chosenVersion == version) return true;
        if (!RivalRushClient.isUpgradeScreenOpen()) { reopen(minecraft); return true; }
        String id = RivalRushClient.offers().getCompound(0).getString("id");
        clickStarting(minecraft, "1. "); chosenRun = runId; chosenVersion = version;
        JetSetCraft.LOGGER.info("JETSETCRAFT_PROGRESSION_AUDIT_CHOICE run={} upgrade={} version={}", runId, id, version);
        return true;
    }

    private static void reopen(Minecraft minecraft) {
        if (++ticks % 20 == 0) RivalRushClient.request("menu", "", 0);
    }

    private static void click(Minecraft minecraft, String label) { clickButton(minecraft, label, false); }
    private static void clickStarting(Minecraft minecraft, String label) { clickButton(minecraft, label, true); }
    private static void clickButton(Minecraft minecraft, String label, boolean prefix) {
        require(minecraft.screen != null, "Missing progression screen for " + label);
        for (var child : minecraft.screen.children()) if (child instanceof Button button && button.active && button.visible
                && (prefix ? button.getMessage().getString().startsWith(label) : button.getMessage().getString().equals(label))) {
            button.onPress(); return;
        }
        throw new IllegalStateException("Missing real progression button: " + label);
    }

    private static boolean captureReady(Minecraft minecraft) {
        if (!RushFeedback.isIdle()) { stableTicks = 0; return false; }
        ticks++;
        long window = minecraft.getWindow().getWindow();
        boolean focused = GLFW.glfwGetWindowAttrib(window, GLFW.GLFW_FOCUSED) == GLFW.GLFW_TRUE;
        double x = minecraft.mouseHandler.xpos(), y = minecraft.mouseHandler.ypos();
        if (focused && x >= 0 && x <= 24 && y >= 0 && y <= 24) {
            if (++stableTicks >= 12) return true;
        } else {
            stableTicks = 0;
            if (!focused) GLFW.glfwFocusWindow(window);
            else { double offset = ticks & 1; GLFW.glfwSetCursorPos(window, 8 + offset, 8); GLFW.glfwSetCursorPos(window, 12 + offset, 12); }
        }
        if (ticks >= 60) {
            JetSetCraft.LOGGER.warn("JETSETCRAFT_PROGRESSION_AUDIT_CURSOR_UNCONFIRMED phase={} focused={} observed=({}, {}); preserving actual capture", phase, focused, x, y);
            return true;
        }
        return false;
    }

    private static void next(int next) { phase = next; ticks = 0; stableTicks = 0; }
    private static void require(boolean condition, String reason) { if (!condition) throw new IllegalStateException(reason); }
    private RushProgressionVisualAudit() { }
}
