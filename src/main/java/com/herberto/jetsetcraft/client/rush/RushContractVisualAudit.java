package com.herberto.jetsetcraft.client.rush;

import com.herberto.jetsetcraft.JetSetCraft;
import com.herberto.jetsetcraft.rush.RivalRushManager;
import com.herberto.jetsetcraft.rush.RushArenaCatalog;
import com.herberto.jetsetcraft.rush.RushRun;
import com.herberto.jetsetcraft.rush.RushWorld;
import net.minecraft.client.CameraType;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.Button;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
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
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;
import java.util.function.Consumer;
import java.util.function.Predicate;

/**
 * Appended native contract/service proof. Only the exact opt-in disposable world can run fixtures.
 * Teleports, loaded chunks, accepted wall-handler calls, owned-rival placement/hits and a room-clock
 * jump are explicitly logged. Completion, credits, ranks, receipts and client snapshots are never assigned.
 */
public final class RushContractVisualAudit {
    private static final String ARENA = "jetsetcraft:signal_yard";
    private static final Set<BlockPos> TAGGED_SUPPORTS = new HashSet<>();
    private static int phase, ticks, stableTicks, chosenVersion = Integer.MIN_VALUE;
    private static int tuneRank, serviceVersion;
    private static String tuneId, tuneName, originalDimension;
    private static UUID runId, chosenRun;
    private static Vec3 originalPosition;
    private static ListTag originalInventory;
    private static int originalLevel, originalTotalXp;
    private static float originalProgress;
    private static CameraType originalCamera;
    private static volatile boolean fixturePending, arrivalApplied, centerApplied, clockApplied;
    private static volatile int acceptedPatches, fixtureDefeats;
    private static volatile String fixtureFailure;
    private static long phaseStarted;
    private static boolean wallWaitLogged;

    public static boolean advance(Minecraft minecraft, Consumer<String> capture, Predicate<String> captured) {
        guardedServer(minecraft);
        require(minecraft.player != null && minecraft.level != null && minecraft.getConnection() != null,
                "Contract audit lost its connected player");
        if (fixtureFailure != null) throw new IllegalStateException(fixtureFailure);
        if (phaseStarted == 0) phaseStarted = System.nanoTime();
        require(System.nanoTime() - phaseStarted < 75_000_000_000L, "Contract audit stage timed out: " + phase);
        CompoundTag state = RivalRushClient.state(), journey = state.getCompound("Journey");
        if (phase >= 5 && phase <= 19) require(state.getBoolean("Active") && state.hasUUID("Run")
                && state.getUUID("Run").equals(runId), "Contract audit lost its live run in phase " + phase);
        if (phase >= 5 && phase <= 18 && chooseRealOffer(minecraft)) return false;

        switch (phase) {
            case 0 -> {
                require(!state.getBoolean("Active"), "Contract audit requires the preceding run to be returned");
                if (minecraft.screen != null) minecraft.screen.onClose();
                RivalRushClient.request("menu", "", 0); next(1);
            }
            case 1 -> {
                if (!(minecraft.screen instanceof RivalRushScreen) || !hasButton(minecraft, "Start 30-minute run", false)) return false;
                originalDimension = minecraft.level.dimension().location().toString();
                originalPosition = minecraft.player.position();
                originalInventory = minecraft.player.getInventory().save(new ListTag()).copy();
                originalLevel = minecraft.player.experienceLevel; originalTotalXp = minecraft.player.totalExperience;
                originalProgress = minecraft.player.experienceProgress; originalCamera = minecraft.options.getCameraType();
                click(minecraft, "Arena circuit", false);
                click(minecraft, "First arena:", true); next(2);
            }
            case 2 -> {
                if (!(minecraft.screen instanceof RivalRushScreen)) return false;
                if (hasButton(minecraft, "Signal Yard", true)) { click(minecraft, "Signal Yard", true); next(3); }
                else if (hasButton(minecraft, "Next ›", false)) click(minecraft, "Next ›", false);
                else if (++ticks >= 40) throw new IllegalStateException("The actual arena catalogue has no selectable Signal Yard");
            }
            case 3 -> {
                if (!hasButton(minecraft, "Start 6-arena journey", false)) return false;
                click(minecraft, "Start 6-arena journey", false); next(4);
            }
            case 4 -> {
                if (!state.getBoolean("Active") || !RivalRushClient.active()) return false;
                require(state.hasUUID("Run") && journey.getCompound("Current").getString("Id").equals(ARENA),
                        "Audit did not enter the selected real PAINT arena route");
                runId = state.getUUID("Run"); next(5);
            }
            case 5 -> {
                if (!openContract(minecraft)) return false;
                CompoundTag contract = journey.getCompound("Contract");
                require(contract.getString("Type").equals("paint") && contract.getInt("Target") == 3
                                && contract.getInt("Progress") == 0 && !contract.getBoolean("Completed") && journey.getInt("Completed") == 0,
                        "Initial briefing invented optional progress or a room clear");
                if (!captureReady(minecraft)) return false;
                JetSetCraft.LOGGER.info("JETSETCRAFT_CONTRACT_AUDIT_BRIEFING_OBSERVED run={} arena={} phase={} contract={} requiredDefeats={} requiredSeconds={} (real selected profile; no completion or bonus assigned)",
                        runId, ARENA, journey.getString("Phase"), contract, journey.getCompound("Current").getInt("Defeats"),
                        journey.getCompound("Current").getInt("Seconds"));
                capture.accept("20-contract-briefing"); next(6);
            }
            case 6 -> {
                if (!captured.test("20-contract-briefing")) return false;
                minecraft.setScreen(null); queueArrival(minecraft); next(7);
            }
            case 7 -> {
                if (!arrivalApplied || !journey.getString("Phase").equals("arena")) return false;
                minecraft.setScreen(null);
                if (!fixturePending && acceptedPatches == 0) queuePaint(minecraft);
                if (acceptedPatches == 1) next(8);
            }
            case 8 -> {
                CompoundTag contract = journey.getCompound("Contract");
                if (contract.getInt("Progress") != 1) return false;
                require(acceptedPatches == 1 && !contract.getBoolean("Completed") && journey.getInt("Completed") == 0,
                        "Progress frame has no actual first patch or invents completion");
                minecraft.options.setCameraType(CameraType.THIRD_PERSON_BACK);
                if (minecraft.screen != null) { minecraft.setScreen(null); stableTicks = 0; return false; }
                if (!captureReady(minecraft)) return false;
                JetSetCraft.LOGGER.info("JETSETCRAFT_CONTRACT_AUDIT_PROGRESS_OBSERVED run={} acceptedPatches={} contract={} requiredObjective={} (native HUD after one accepted wall-handler fixture, not human graffiti input)",
                        runId, acceptedPatches, contract, journey.getString("Objective"));
                capture.accept("21-contract-progress"); next(9);
            }
            case 9 -> {
                if (!captured.test("21-contract-progress")) return false;
                if (acceptedPatches < 3) {
                    if (!fixturePending && ++ticks % 10 == 0) queuePaint(minecraft);
                    return false;
                }
                CompoundTag contract = journey.getCompound("Contract");
                if (!contract.getBoolean("Completed")) return false;
                require(contract.getInt("Progress") == 3 && journey.getInt("Completed") == 0,
                        "A completed optional task incorrectly cleared the required room objective");
                if (!fixturePending) { queueCombatCenter(minecraft); next(10); }
            }
            case 10 -> {
                if (!centerApplied) return false;
                minecraft.setScreen(null); next(11);
            }
            case 11 -> {
                int required = journey.getCompound("Current").getInt("Defeats");
                require(required >= 3 && required <= 80, "Invalid live room defeat target");
                if (state.getInt("Kills") >= required) { next(12); return false; }
                require(fixtureDefeats <= 80, "Contract audit exceeded its real owned-defeat fixture bound");
                if (!fixturePending && ++ticks % 10 == 0) queueOwnedDefeat(minecraft);
            }
            case 12 -> {
                if (fixturePending) return false;
                minecraft.setScreen(null); queueRoomClock(minecraft); next(13);
            }
            case 13 -> {
                if (!clockApplied || journey.getInt("Completed") != 1 || !RivalRushClient.choosingRoute()) return false;
                CompoundTag stop = state.getCompound("PitStop");
                require(stop.getInt("Earned") == 2 && stop.getInt("Credits") == 2 && stop.hasUUID("Receipt")
                                && stop.getBoolean("Usable") && stop.getCompound("LastResult").isEmpty(),
                        "Normal room clear did not open the actual two-service contract stop");
                if (RivalRushClient.isPitStopScreenOpen()) { next(14); return false; }
                if (!(minecraft.screen instanceof RivalRushScreen)) { reopen(); return false; }
                if (!RivalRushClient.isJourneyMapScreenOpen()) click(minecraft, "Journey", false);
                else click(minecraft, "Pit stop (", true);
            }
            case 14 -> {
                if (!RivalRushClient.isPitStopScreenOpen()) return false;
                CompoundTag stop = state.getCompound("PitStop"), clear = journey.getCompound("LastCompleted");
                require(stop.getList("Options", Tag.TAG_COMPOUND).size() == 4 && stop.getInt("Credits") == 2
                                && clear.getBoolean("SettlementRecorded") && clear.getCompound("Contract").getBoolean("Completed"),
                        "Pit stop does not correspond to the real settled room and optional completion");
                if (!captureReady(minecraft)) return false;
                JetSetCraft.LOGGER.info("JETSETCRAFT_CONTRACT_AUDIT_PIT_STOP_OBSERVED run={} receipt={} earned={} credits={} acceptedPatches={} actualDefeats={} cards={} clear={} (normal manager clear after logged clock/hit fixtures)",
                        runId, stop.getUUID("Receipt"), stop.getInt("Earned"), stop.getInt("Credits"), acceptedPatches,
                        state.getInt("Kills"), state.getLong("Cards"), clear);
                capture.accept("22-pit-stop"); next(15);
            }
            case 15 -> {
                if (!captured.test("22-pit-stop")) return false;
                CompoundTag stop = state.getCompound("PitStop");
                ListTag choices = stop.getList("TuneChoices", Tag.TAG_COMPOUND);
                require(!choices.isEmpty(), "A genuine first clear has no owned upgrade to tune");
                CompoundTag choice = choices.getCompound(0);
                tuneId = choice.getString("Id"); tuneName = choice.getString("Name"); tuneRank = choice.getInt("Rank");
                require(tuneRank > 0 && choice.getInt("NextRank") == tuneRank + 1 && !choice.getString("NextDescription").isBlank(),
                        "Tune did not preview one actual eligible owned rank");
                click(minecraft, "Tune", false); next(16);
            }
            case 16 -> {
                if (!hasButton(minecraft, tuneName + " · ", true)) return false;
                click(minecraft, tuneName + " · ", true); next(17);
            }
            case 17 -> {
                if (!hasButton(minecraft, "Use 1 service", false)) return false;
                serviceVersion = state.getInt("Version");
                click(minecraft, "Use 1 service", false); next(18);
            }
            case 18 -> {
                CompoundTag stop = state.getCompound("PitStop"), result = stop.getCompound("LastResult");
                if (state.getInt("Version") == serviceVersion || !result.getBoolean("Accepted") || !RivalRushClient.isServiceResultScreenOpen()) return false;
                require(result.getString("Service").equals("TUNE") && result.getString("Upgrade").equals(tuneId)
                                && result.getInt("RankBefore") == tuneRank && result.getInt("RankAfter") == tuneRank + 1
                                && stop.getInt("Credits") == 1 && result.getInt("RemainingCredits") == 1
                                && !result.getString("Summary").isBlank(), "Service packet did not record the actual rank increase and single credit spend");
                if (!captureReady(minecraft)) return false;
                JetSetCraft.LOGGER.info("JETSETCRAFT_CONTRACT_AUDIT_SERVICE_OBSERVED run={} result={} credits={} version={} -> {} (real UI confirmation and production pit_stop packet, no rank/receipt assignment)",
                        runId, result, stop.getInt("Credits"), serviceVersion, state.getInt("Version"));
                capture.accept("23-service-result"); next(19);
            }
            case 19 -> {
                if (!captured.test("23-service-result")) return false;
                RivalRushClient.request("leave", "", 0); next(20);
            }
            case 20 -> {
                if (state.getBoolean("Active") || !originalDimension.equals(minecraft.level.dimension().location().toString())) return false;
                if (++ticks < 20) return false;
                require(originalInventory.equals(minecraft.player.getInventory().save(new ListTag()))
                                && minecraft.player.experienceLevel == originalLevel && minecraft.player.totalExperience == originalTotalXp
                                && Math.abs(minecraft.player.experienceProgress - originalProgress) <= .000001F
                                && minecraft.player.position().distanceToSqr(originalPosition) <= 1,
                        "Contract run did not restore ordinary inventory/XP/dimension/position");
                CompoundTag report = state.getCompound("Debrief");
                require(report.hasUUID("Run") && report.getUUID("Run").equals(runId)
                                && report.getLong("ArenaClears") == 1 && report.getLong("Headliners") == 0
                                && report.getLong("ContractsBanked") == 1 && report.getLong("ServicesUsed") == 1
                                && report.getLong("Defeats") >= 8 && report.getLong("Cards") > 0,
                        "Returned report lost the real room, optional bonus, service or pickup facts");
                minecraft.options.setCameraType(originalCamera);
                JetSetCraft.LOGGER.info("JETSETCRAFT_CONTRACT_VISUAL_AUDIT_PASS four real-client captures; run={} acceptedPatches={} defeats={} cards={} activeTicks={} arenas={} contractsBanked={} servicesUsed={} returnAward={} sourceDimension={}; authored-wall handler/arrival/owned-hit/clock fixtures explicitly logged; normal room gate and production service packet; inventory/XP/position restored; no human traversal, duration or boss victory claimed",
                        runId, acceptedPatches, report.getLong("Defeats"), report.getLong("Cards"), report.getLong("Ticks"),
                        report.getLong("ArenaClears"), report.getLong("ContractsBanked"), report.getLong("ServicesUsed"),
                        report.getString("ReturnRewardStatus"), originalDimension);
                next(21); return true;
            }
            case 21 -> { return true; }
            default -> throw new IllegalStateException("Unknown contract audit phase " + phase);
        }
        return false;
    }

    private static void queueArrival(Minecraft minecraft) {
        queue(minecraft, "arrival", player -> {
            BlockPos center = RivalRushManager.arenaDestination(player);
            RushArenaCatalog.Profile profile = RivalRushManager.currentArenaProfile(player);
            require(center != null && profile != null && profile.id().toString().equals(ARENA), "Arrival lost the selected arena");
            for (int x = -3; x <= 3; x++) for (int z = -3; z <= 3; z++)
                player.serverLevel().getChunk((center.getX() >> 4) + x, (center.getZ() >> 4) + z);
            Vec3 from = player.position();
            position(player, center.getX() + .5, RushWorld.FLOOR_Y + 1, center.getZ() + .5, 0);
            JetSetCraft.LOGGER.info("JETSETCRAFT_CONTRACT_AUDIT_ARRIVAL_FIXTURE run={} arena={} from={} to={} (teleport and nearby chunk loading only; normal builder/arrival gate, no progress assigned)",
                    runId, profile.id(), from, player.position());
            arrivalApplied = true;
        });
    }

    private static void queuePaint(Minecraft minecraft) {
        queue(minecraft, "paint", player -> {
            RushRun run = RivalRushManager.run(player);
            if (run.hasOffer()) return;
            require(RivalRushManager.journeySnapshot(player).getString("Phase").equals("arena"), "Wall fixture requires an active actual room");
            BlockPos center = RivalRushManager.arenaDestination(player);
            RushArenaCatalog.Profile profile = RivalRushManager.currentArenaProfile(player);
            require(center != null && profile != null && profile.id().toString().equals(ARENA), "Wall fixture lost its authored arena");
            for (int z = -profile.radius() + 6; z <= profile.radius() - 6; z += 4) {
                if (Math.abs(z) < 6) continue;
                BlockPos support = center.offset(profile.radius(), 1, z), stand = center.offset(profile.radius() - 2, 1, z);
                if (TAGGED_SUPPORTS.contains(support) || !player.serverLevel().hasChunkAt(support)
                        || player.serverLevel().getBlockState(support).isAir()
                        || player.serverLevel().getBlockState(support).getCollisionShape(player.serverLevel(), support).isEmpty()
                        || !player.serverLevel().getBlockState(stand).isAir() || !player.serverLevel().getBlockState(stand.above()).isAir()
                        || player.serverLevel().getBlockState(stand.below()).isAir()) continue;
                int before = RivalRushManager.contractSnapshot(player).getInt("Progress");
                Vec3 from = player.position(); position(player, stand.getX() + .5, stand.getY(), stand.getZ() + .5, -90);
                require(RivalRushManager.tagWall(player, support, Direction.WEST), "Fresh authored wall patch was rejected by the production handler");
                CompoundTag after = RivalRushManager.contractSnapshot(player);
                require(after.getInt("Progress") == before + 1 && after.getString("Type").equals("paint"),
                        "Accepted fresh patch did not advance the real PAINT contract exactly once");
                TAGGED_SUPPORTS.add(support.immutable()); acceptedPatches++;
                JetSetCraft.LOGGER.info("JETSETCRAFT_CONTRACT_AUDIT_PAINT_FIXTURE run={} support={} face=west from={} to={} progress={} -> {} draft={} (actual authored wall, scene placement and accepted production tagWall; not human graffiti input)",
                        runId, support, from, player.position(), before, after.getInt("Progress"), run.hasOffer());
                return;
            }
            // Safe entry can precede perimeter construction. Wait within the stage deadline; never create a support.
            if (!wallWaitLogged) {
                wallWaitLogged = true;
                JetSetCraft.LOGGER.info("JETSETCRAFT_CONTRACT_AUDIT_WAITING_FOR_AUTHORED_WALL run={} arena={} (normal builder continues; no fallback geometry added)", runId, ARENA);
            }
        });
    }

    private static void queueCombatCenter(Minecraft minecraft) {
        queue(minecraft, "combat center", player -> {
            BlockPos center = RivalRushManager.arenaDestination(player);
            require(center != null, "Combat fixture lost its real arena center");
            Vec3 from = player.position(); position(player, center.getX() + .5, RushWorld.FLOOR_Y + 1, center.getZ() + .5, 0);
            JetSetCraft.LOGGER.info("JETSETCRAFT_CONTRACT_AUDIT_POSITION_FIXTURE run={} from={} to={} (safe center placement only)", runId, from, player.position());
            centerApplied = true;
        });
    }

    private static void queueOwnedDefeat(Minecraft minecraft) {
        queue(minecraft, "owned hit", player -> {
            RushRun run = RivalRushManager.run(player);
            RushArenaCatalog.Profile profile = RivalRushManager.currentArenaProfile(player);
            if (run.hasOffer() || profile == null || run.defeats() >= profile.defeats()) return;
            var rivals = player.serverLevel().getEntitiesOfClass(Mob.class, player.getBoundingBox().inflate(64),
                            mob -> RivalRushManager.belongsTo(mob, player) && RivalRushManager.actorVitality(player, mob) > 0)
                    .stream().sorted(Comparator.comparingDouble(mob -> mob.distanceToSqr(player))).toList();
            if (rivals.isEmpty()) return;
            Mob target = rivals.get(0); Vec3 from = target.position(), nearby = player.position().add(2, 0, 1);
            target.moveTo(nearby.x, nearby.y, nearby.z, target.getYRot(), 0); target.setDeltaMovement(Vec3.ZERO);
            require(player.hasLineOfSight(target), "Owned rival fixture has no actual tag sightline");
            long before = run.defeats(); int hits = 0;
            while (!target.isRemoved() && !run.hasOffer() && hits < 32) {
                require(RivalRushManager.tagEnemy(player, target, 7), "Production tag rejected an actual owned rival"); hits++;
            }
            require(target.isRemoved() || run.hasOffer(), "Bounded production hits failed to settle or open a real draft");
            if (target.isRemoved()) require(run.defeats() > before, "Removed fixture target did not settle an owned defeat");
            fixtureDefeats += (int) Math.max(0, run.defeats() - before);
            JetSetCraft.LOGGER.info("JETSETCRAFT_CONTRACT_AUDIT_OWNED_HIT_FIXTURE run={} actor={} from={} to={} hits={} defeats={} -> {} draft={} (real primary handler; any secondary defeats and pickup XP follow production rules)",
                    runId, target.getUUID(), from, nearby, hits, before, run.defeats(), run.hasOffer());
        });
    }

    private static void queueRoomClock(Minecraft minecraft) {
        queue(minecraft, "room clock", player -> {
            RushRun run = RivalRushManager.run(player);
            CompoundTag journey = RivalRushManager.journeySnapshot(player), contract = RivalRushManager.contractSnapshot(player);
            RushArenaCatalog.Profile profile = RivalRushManager.currentArenaProfile(player);
            require(profile != null && !run.hasOffer() && journey.getString("Phase").equals("arena")
                            && contract.getBoolean("Completed") && run.defeats() >= profile.defeats(),
                    "Clock fixture requires actual completed optional task and owned defeats, with no unresolved draft");
            long before = run.elapsedTicks(), remaining = journey.getLong("TicksLeft");
            require(remaining >= 0 && remaining <= 7200, "Room clock jump exceeded its authored bound");
            for (long tick = 0; tick < remaining; tick++) run.tickActive();
            JetSetCraft.LOGGER.info("JETSETCRAFT_CONTRACT_AUDIT_CLOCK_FIXTURE run={} activeTicks={} -> {} remaining={} actualDefeats={} actualContract={} (clock only; next normal manager tick must earn and settle the room)",
                    runId, before, run.elapsedTicks(), remaining, run.defeats(), contract);
            clockApplied = true;
        });
    }

    private static void queue(Minecraft minecraft, String label, Consumer<ServerPlayer> operation) {
        MinecraftServer server = guardedServer(minecraft); Path expected = expectedWorld(minecraft);
        UUID playerId = minecraft.player.getUUID(); fixturePending = true;
        server.execute(() -> {
            try {
                require(Boolean.getBoolean("jetsetcraft.rushVisualAudit")
                                && server.getWorldPath(LevelResource.ROOT).toAbsolutePath().normalize().equals(expected),
                        "Contract fixture lost its exact disposable-world guard");
                ServerPlayer player = server.getPlayerList().getPlayer(playerId);
                require(player != null && RushWorld.isRush(player.level()) && RivalRushManager.run(player) != null
                                && RivalRushManager.run(player).id().equals(runId), "Contract fixture lost its actual owner/run");
                operation.accept(player);
            } catch (RuntimeException error) {
                fixtureFailure = label + " fixture failed: " + error.getMessage();
                JetSetCraft.LOGGER.error("Contract visual audit fixture failed: " + label, error);
            } finally { fixturePending = false; }
        });
    }

    private static void position(ServerPlayer player, double x, double y, double z, float yaw) {
        player.teleportTo(player.serverLevel(), x, y, z, yaw, 0); player.setDeltaMovement(Vec3.ZERO); player.fallDistance = 0;
    }

    private static boolean openContract(Minecraft minecraft) {
        if (RivalRushClient.isContractScreenOpen()) return true;
        if (!(minecraft.screen instanceof RivalRushScreen)) { reopen(); return false; }
        if (RivalRushClient.isBuildScreenOpen()) click(minecraft, "Room contract", false);
        else click(minecraft, "Build", false);
        return false;
    }

    private static boolean chooseRealOffer(Minecraft minecraft) {
        if (RivalRushClient.offers().isEmpty()) return false;
        int version = RivalRushClient.state().getInt("Version");
        if (runId.equals(chosenRun) && version == chosenVersion) return true;
        if (!RivalRushClient.isUpgradeScreenOpen()) { reopen(); return true; }
        String upgrade = RivalRushClient.offers().getCompound(0).getString("id");
        click(minecraft, "1. ", true); chosenRun = runId; chosenVersion = version;
        JetSetCraft.LOGGER.info("JETSETCRAFT_CONTRACT_AUDIT_CHOICE run={} upgrade={} version={} (actual earned offer)", runId, upgrade, version);
        return true;
    }

    private static MinecraftServer guardedServer(Minecraft minecraft) {
        MinecraftServer server = minecraft.getSingleplayerServer();
        require(Boolean.getBoolean("jetsetcraft.rushVisualAudit") && server != null
                        && server.getWorldPath(LevelResource.ROOT).toAbsolutePath().normalize().equals(expectedWorld(minecraft)),
                "Contract visual audit requires the exact disposable saves/RivalRushAudit integrated world");
        return server;
    }
    private static Path expectedWorld(Minecraft minecraft) {
        return minecraft.gameDirectory.toPath().resolve("saves/RivalRushAudit").toAbsolutePath().normalize();
    }
    private static void reopen() { if (++ticks % 20 == 0) RivalRushClient.request("menu", "", 0); }
    private static boolean hasButton(Minecraft minecraft, String label, boolean prefix) {
        return minecraft.screen != null && minecraft.screen.children().stream().anyMatch(child -> child instanceof Button button
                && button.visible && button.active && (prefix ? button.getMessage().getString().startsWith(label) : button.getMessage().getString().equals(label)));
    }
    private static void click(Minecraft minecraft, String label, boolean prefix) {
        require(minecraft.screen != null, "Missing contract screen for " + label);
        for (var child : minecraft.screen.children()) if (child instanceof Button button && button.visible && button.active
                && (prefix ? button.getMessage().getString().startsWith(label) : button.getMessage().getString().equals(label))) {
            button.onPress(); return;
        }
        throw new IllegalStateException("Missing real contract control: " + label);
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
            JetSetCraft.LOGGER.warn("JETSETCRAFT_CONTRACT_AUDIT_CURSOR_UNCONFIRMED phase={} focused={} observed=({}, {}); preserving actual capture", phase, focused, x, y);
            return true;
        }
        return false;
    }
    private static void next(int value) { phase = value; ticks = 0; stableTicks = 0; phaseStarted = System.nanoTime(); }
    private static void require(boolean condition, String reason) { if (!condition) throw new IllegalStateException(reason); }
    private RushContractVisualAudit() {}
}
