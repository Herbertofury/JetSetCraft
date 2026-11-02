package com.herberto.jetsetcraft.client.liberty;

import com.herberto.jetsetcraft.JetSetCraft;
import com.herberto.jetsetcraft.client.activity.ActivityClient;
import com.herberto.jetsetcraft.entity.GraffitiEntity;
import com.herberto.jetsetcraft.liberty.LibertyManager;
import com.herberto.jetsetcraft.liberty.LibertyMissionCatalog;
import com.herberto.jetsetcraft.liberty.LibertyShipProgress;
import com.herberto.jetsetcraft.network.JetSetNetwork;
import net.minecraft.client.CameraType;
import net.minecraft.client.Minecraft;
import net.minecraft.client.Screenshot;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.BackupConfirmScreen;
import net.minecraft.client.gui.screens.ConfirmScreen;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.tutorial.TutorialSteps;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.contents.TranslatableContents;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.LevelResource;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.lwjgl.glfw.GLFW;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Opt-in native UI acceptance against only the disposable saves/LibertyGrindAudit world.
 * Uses production activity/liberty packets and real buttons. A logged vanilla setup fixture adds
 * return sentinels; no mission objective, credit, sample, research or completion is fabricated.
 * Provider-absent runs prove the dependency state. Provider-present runs additionally observe the
 * actual pod, loaded TaCZ weapon/HUD and mission screen, then prove an ordinary early return.
 * The separately gated campaign extension logs its real-settlement requisition fixture and camera/
 * invulnerable-flight staging. Choices, purchases, launch, CHROMAGEDDON and returns use production UI.
 */
@Mod.EventBusSubscriber(modid = JetSetCraft.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE, value = Dist.CLIENT)
public final class LibertyVisualAudit {
    private static final String WORLD = "LibertyGrindAudit", STAMP = Long.toString(System.currentTimeMillis());
    private static final byte[] PNG = {(byte) 137, 80, 78, 71, 13, 10, 26, 10};
    private static final Map<String, Path> CAPTURES = new ConcurrentHashMap<>();
    private static long started, failureStarted, fixtureObservedAt, phaseStartedAt;
    private static int phase, ticks, originalScale, originalWidth, originalHeight, fixtureSettledTicks;
    private static boolean configured, queuedFixture, complete, provider;
    private static volatile boolean fixtureReady;
    private static volatile String failure;
    private static Screen confirmedScreen;
    private static TutorialSteps originalTutorial;
    private static CameraType originalCamera;
    private static String originalDimension;
    private static ListTag originalInventory;
    private static int originalLevel, originalTotal;
    private static float originalProgress;
    private static int pointerPark;
    private static float originalHudX, originalHudY, originalHudScale;
    private static boolean originalHudVisible;
    private static boolean originalForward, campaignStageQueued;
    private static volatile boolean campaignProofQueued, campaignBaselineQueued;
    private static volatile boolean campaignStageReady, campaignBaselineReady, campaignPaintProved;
    private static boolean campaignCleanupQueued;
    private static volatile boolean campaignCleanupProved;
    private static volatile int campaignPaintEntities, campaignProgress;
    private static volatile UUID campaignMission;
    private static volatile BlockPos campaignTerritory;
    private static volatile String campaignSite;
    private static final Map<BlockPos, BlockState> CAMPAIGN_TERRAIN = new ConcurrentHashMap<>();
    private static final Set<UUID> CAMPAIGN_PAINT = ConcurrentHashMap.newKeySet();
    private static double shipWalkStartX, shipWalkStartZ;
    private static final String SELECTED_GUN = "helldiver2:sg225_breaker";

    @SubscribeEvent public static void tick(TickEvent.ClientTickEvent event) {
        if (event.phase != TickEvent.Phase.END || complete || !Boolean.getBoolean("jetsetcraft.libertyVisualAudit")) return;
        Minecraft minecraft = Minecraft.getInstance();
        if (failureStarted != 0) {
            if (CAPTURES.containsKey("99-failure") || System.nanoTime() - failureStarted > 10_000_000_000L) stop(minecraft);
            return;
        }
        if (started == 0) started = System.nanoTime();
        long limit = campaignAudit() ? 480_000_000_000L : 300_000_000_000L;
        if (System.nanoTime() - started > limit) { finish(minecraft, false, "Native audit deadline in phase " + phase); return; }
        if (failure != null) { finish(minecraft, false, failure); return; }
        try {
            if (!configured) {
                require(isDisposableLaunch(minecraft), "Audit requires explicit LibertyGrindAudit quick-play launch and an existing disposable save");
                originalScale = minecraft.options.guiScale().get(); originalCamera = minecraft.options.getCameraType();
                originalWidth = minecraft.getWindow().getScreenWidth(); originalHeight = minecraft.getWindow().getScreenHeight();
                originalTutorial = minecraft.options.tutorialStep; minecraft.getTutorial().setStep(TutorialSteps.NONE);
                originalForward = minecraft.options.keyUp.isDown();
                var hud = LibertyHudPreferences.get();
                originalHudX = hud.x; originalHudY = hud.y; originalHudScale = hud.scale; originalHudVisible = hud.visible;
                hud.reset(); // Verify the shipping default, then restore the local preference on every normal audit exit.
                viewport(minecraft, 1280, 720, 2); configured = true;
            }
            if (minecraft.player == null || minecraft.level == null || minecraft.getConnection() == null) { worldConfirmation(minecraft); return; }
            require(disposableServer(minecraft), "Audit lost its exact disposable integrated save");
            if (campaignAudit() && phase >= 3 && phaseStartedAt != 0 && System.nanoTime() - phaseStartedAt > 45_000_000_000L) {
                finish(minecraft, false, "45-second campaign phase deadline " + phase + "; server status=" + LibertyClient.stateSnapshot().getString("Status")); return;
            }
            if (minecraft.screen != null) {
                // A fixed native position need not deliver a new mouse event after a window resize.
                // Alternate nearby pixels, and never warp a grabbed gameplay mouse (that rotates the camera).
                GLFW.glfwFocusWindow(minecraft.getWindow().getWindow());
                int point = 1 + (pointerPark++ & 1);
                GLFW.glfwSetCursorPos(minecraft.getWindow().getWindow(), point, point);
            }
            advance(minecraft);
        } catch (RuntimeException error) {
            JetSetCraft.LOGGER.error("Liberty native UI audit failed", error);
            finish(minecraft, false, "phase " + phase + ": " + error.getMessage());
        }
    }

    private static void advance(Minecraft minecraft) {
        if (campaignAudit() && phase >= 3 && phase != 25) { advanceCampaign(minecraft); return; }
        CompoundTag state = LibertyClient.stateSnapshot();
        switch (phase) {
            case 0 -> {
                if (++ticks < 40) return;
                if (!queuedFixture) { queuedFixture = true; fixture(minecraft); return; }
                if (!fixtureReady || minecraft.player.getInventory().countItem(Items.DIAMOND) < 7) return;
                if (fixtureObservedAt == 0) fixtureObservedAt = System.nanoTime();
                // Let the genuine recipe/advancement toasts and vanilla chat fade; never suppress them.
                if (++fixtureSettledTicks < 240 || System.nanoTime() - fixtureObservedAt < 12_000_000_000L) return;
                JetSetNetwork.sendActivityAction("open", "", null, 0); next(1);
            }
            case 1 -> {
                if (!ActivityClient.isScreenOpen() || ++ticks < 12) return;
                clickContains(minecraft, "Liberty Grind"); next(2);
            }
            case 2 -> {
                CompoundTag activity = ActivityClient.stateSnapshot();
                if (!activity.getString("SelectedMode").equals("liberty_grind") || !activity.getBoolean("CanLaunch") || ++ticks < 12) return;
                click(minecraft, "Open war map"); next(3);
            }
            case 3 -> {
                if (!LibertyClient.isScreenOpen() || state.getList("Theaters", Tag.TAG_COMPOUND).size() != 3 || ++ticks < 20) return;
                capture(minecraft, "01-war-map-default"); next(4);
            }
            case 4 -> {
                if (!captured("01-war-map-default")) return;
                click(minecraft, "Neon Docks"); next(5);
            }
            case 5 -> {
                if (++ticks < 20) return;
                capture(minecraft, "02-district-default"); next(6);
            }
            case 6 -> {
                if (!captured("02-district-default")) return;
                click(minecraft, "Research"); next(7);
            }
            case 7 -> {
                require(state.getCompound("Research").getList("Projects", Tag.TAG_COMPOUND).size() == 6, "Actual research catalogue must contain six projects");
                if (++ticks < 20) return;
                capture(minecraft, "03-research-default"); next(8);
            }
            case 8 -> {
                if (!captured("03-research-default")) return;
                clickContains(minecraft, firstProject()); next(9);
            }
            case 9 -> {
                if (++ticks < 20) return;
                capture(minecraft, "04-project-default"); next(10);
            }
            case 10 -> {
                if (!captured("04-project-default")) return;
                click(minecraft, "HUD"); next(11);
            }
            case 11 -> {
                require(minecraft.screen instanceof LibertyHudScreen, "HUD editor did not open");
                if (++ticks < 20) return;
                capture(minecraft, "05-hud-default"); next(12);
            }
            case 12 -> {
                if (!captured("05-hud-default")) return;
                click(minecraft, "Done"); viewport(minecraft, 960, 720, 3); next(13);
            }
            case 13 -> {
                if (minecraft.getWindow().getGuiScaledWidth() != 320 || minecraft.getWindow().getGuiScaledHeight() != 240 || ++ticks < 20) return;
                click(minecraft, "War map"); next(131);
            }
            case 131 -> {
                if (++ticks < 20) return;
                capture(minecraft, "06-war-map-minimum"); next(14);
            }
            case 14 -> {
                if (!captured("06-war-map-minimum")) return;
                click(minecraft, "Research"); next(15);
            }
            case 15 -> {
                if (++ticks < 20) return;
                capture(minecraft, "07-research-minimum"); next(16);
            }
            case 16 -> {
                if (!captured("07-research-minimum")) return;
                clickContains(minecraft, firstProject()); next(17);
            }
            case 17 -> {
                if (++ticks < 20) return;
                capture(minecraft, "08-project-minimum"); next(18);
            }
            case 18 -> {
                if (!captured("08-project-minimum")) return;
                minecraft.screen.keyPressed(GLFW.GLFW_KEY_END, 0, 0); next(19);
            }
            case 19 -> {
                if (++ticks < 12) return;
                capture(minecraft, "09-project-scroll-minimum"); next(20);
            }
            case 20 -> {
                if (!captured("09-project-scroll-minimum")) return;
                click(minecraft, "HUD"); next(21);
            }
            case 21 -> {
                if (++ticks < 20) return;
                capture(minecraft, "10-hud-minimum"); next(22);
            }
            case 22 -> {
                if (!captured("10-hud-minimum")) return;
                click(minecraft, "Done"); click(minecraft, "War map"); click(minecraft, "Neon Docks"); next(23);
            }
            case 23 -> {
                if (++ticks < 20) return;
                provider = state.getList("Theaters", Tag.TAG_COMPOUND).getCompound(0).getBoolean("Available");
                require(button(minecraft, "Deploy in pod").active == provider, "Deploy widget disagrees with server availability");
                capture(minecraft, "11-district-minimum"); next(24);
            }
            case 24 -> {
                if (!captured("11-district-minimum")) return;
                if (!provider) { finish(minecraft, true, "11 native captures; actual activity handoff, dependency-blocked deployment, six-project research and user HUD at default and 320x240 logical size; no simulated mission state"); return; }
                originalDimension = minecraft.level.dimension().location().toString();
                originalInventory = minecraft.player.getInventory().save(new ListTag()).copy();
                originalLevel = minecraft.player.experienceLevel; originalTotal = minecraft.player.totalExperience; originalProgress = minecraft.player.experienceProgress;
                minecraft.options.setCameraType(CameraType.THIRD_PERSON_BACK);
                click(minecraft, "Deploy in pod"); next(25);
            }
            case 25 -> {
                require(!state.getBoolean("Active") || state.getString("Phase").equals("DROPPING"),
                        "Pod drop ended before three aligned moving render frames were observed");
                if (!state.getBoolean("Active") || !LibertyClient.active() || minecraft.player.getVehicle() == null) return;
                if (minecraft.screen != null) { ticks = 0; return; } // A terrain loading screen is not visual proof of a pod.
                minecraft.player.getInventory().selected = 4; // Actual field radio: TaCZ may force first-person while holding a gun.
                require(minecraft.player.getMainHandItem().is(Items.COMPASS), "Pod camera did not select the actual field radio");
                minecraft.options.setCameraType(CameraType.THIRD_PERSON_FRONT);
                minecraft.player.setYRot(0); minecraft.player.yRotO = 0;
                minecraft.player.setXRot(0); minecraft.player.xRotO = 0;
                minecraft.player.setYHeadRot(0); minecraft.player.yHeadRotO = 0;
                if (++ticks < 5) return;
                var vehicle = minecraft.player.getVehicle();
                var riderPos = minecraft.player.position(); var vehiclePos = vehicle.position();
                require(Double.isFinite(riderPos.x) && Double.isFinite(riderPos.y) && Double.isFinite(riderPos.z)
                        && Double.isFinite(vehiclePos.x) && Double.isFinite(vehiclePos.y) && Double.isFinite(vehiclePos.z),
                        "Non-finite native transport or rider position");
                long renderCalls = com.herberto.jetsetcraft.client.render.LibertyTransportRenderer.auditRenderCalls(vehicle.getId());
                long movingRenderCalls = com.herberto.jetsetcraft.client.render.LibertyTransportRenderer.auditMovingRenderCalls(vehicle.getId());
                int alignedFrames = com.herberto.jetsetcraft.client.render.LibertyTransportRenderer.auditAlignedMovingFrames(vehicle.getId());
                boolean compiled = minecraft.levelRenderer.isChunkCompiled(vehicle.blockPosition());
                if (ticks == 5 || compiled && renderCalls > 0) JetSetCraft.LOGGER.info("JETSETCRAFT_LIBERTY_VISUAL_AUDIT_TRANSPORT type={} class={} renderer={} player={} vehicle={} ticks={} chunk={} compiled={} riderCompiled={} old=({},{},{}) removed={} controlledLocal={} invisible={} renderCalls={} bounds={}",
                        net.minecraft.core.registries.BuiltInRegistries.ENTITY_TYPE.getKey(vehicle.getType()), vehicle.getClass().getName(),
                        minecraft.getEntityRenderDispatcher().getRenderer(vehicle).getClass().getName(), riderPos, vehiclePos,
                        vehicle.tickCount, minecraft.level.hasChunkAt(vehicle.blockPosition()), compiled,
                        minecraft.levelRenderer.isChunkCompiled(minecraft.player.blockPosition()), vehicle.xOld, vehicle.yOld, vehicle.zOld, vehicle.isRemoved(),
                        vehicle.isControlledByLocalInstance(), vehicle.isInvisible(),
                        renderCalls, vehicle.getBoundingBox());
                if (!compiled || renderCalls == 0 || movingRenderCalls == 0 || alignedFrames < 3) return;
                JetSetCraft.LOGGER.info("JETSETCRAFT_LIBERTY_VISUAL_AUDIT_SEAT movingFrames={} consecutiveAlignedFrames={} interpolationError={} tolerance=0.05",
                        movingRenderCalls, alignedFrames, com.herberto.jetsetcraft.client.render.LibertyTransportRenderer.auditSeatError(vehicle.getId()));
                capture(minecraft, "12-live-pod"); next(26);
            }
            case 26 -> {
                if (!captured("12-live-pod") || !state.getString("Phase").equals("ACTIVE")) return;
                require(!minecraft.player.isPassenger(), "Player is still attached to discarded drop pod");
                minecraft.player.getInventory().selected = 0;
                minecraft.options.setCameraType(CameraType.FIRST_PERSON); next(27);
            }
            case 27 -> {
                if (++ticks < 240) return; // New loan armor legitimately earns recipe/advancement toasts; let them finish.
                require(minecraft.player.getMainHandItem().hasTag()
                        && minecraft.player.getMainHandItem().getTag().getString("GunId").equals("helldiver2:ar23_liberator"), "Native mission did not lend the verified Liberator");
                capture(minecraft, "13-live-gun-hud"); next(28);
            }
            case 28 -> {
                if (!captured("13-live-gun-hud")) return;
                minecraft.player.getInventory().selected = 4;
                require(minecraft.gameMode != null && minecraft.player.getMainHandItem().is(Items.COMPASS)
                        && minecraft.player.getMainHandItem().hasTag()
                        && minecraft.player.getMainHandItem().getTag().getBoolean("LibertyFieldRadio"), "Missing actual field-radio control");
                minecraft.gameMode.useItem(minecraft.player, net.minecraft.world.InteractionHand.MAIN_HAND);
                next(29);
            }
            case 29 -> {
                if (!LibertyClient.isScreenOpen() || ++ticks < 20) return;
                require(state.getList("Objectives", Tag.TAG_COMPOUND).size() == 3 && state.getBoolean("CanAbort"), "Actual mission objectives / return action missing");
                capture(minecraft, "14-live-mission-minimum"); next(30);
            }
            case 30 -> {
                if (!captured("14-live-mission-minimum")) return;
                click(minecraft, "Return early");
                require(minecraft.screen instanceof ConfirmScreen, "Early return requires the ordinary confirmation screen");
                clickTranslation(minecraft, "gui.yes"); next(31);
            }
            case 31 -> {
                if (state.getBoolean("Active") || !minecraft.level.dimension().location().toString().equals(originalDimension) || ++ticks < 20) return;
                require(minecraft.player.getInventory().save(new ListTag()).equals(originalInventory), "Native early return changed original inventory");
                require(minecraft.player.experienceLevel == originalLevel && minecraft.player.totalExperience == originalTotal
                        && Math.abs(minecraft.player.experienceProgress - originalProgress) < .0001F, "Native early return changed XP");
                capture(minecraft, "15-returned-war-map"); next(32);
            }
            case 32 -> {
                if (!captured("15-returned-war-map")) return;
                finish(minecraft, true, "15 native captures; actual activity handoff, default and 320x240 research/HUD, live TaCZ pod/weapon/mission, ordinary early return restored original inventory/XP/dimension; no objective, reward or extraction completion fixture");
            }
            default -> throw new IllegalStateException("Unknown audit phase " + phase);
        }
    }

    /** Campaign-only branch; phase 25 deliberately reuses the original moving native-pod proof. */
    private static void advanceCampaign(Minecraft minecraft) {
        CompoundTag state = LibertyClient.stateSnapshot();
        switch (phase) {
            case 3 -> {
                if (!(minecraft.screen instanceof LibertyCampaignScreen) || ++ticks < 20) return;
                require(state.getCompound("Campaign").getList("Sectors", Tag.TAG_COMPOUND).size() == 6, "Campaign map omitted one of six regions");
                require(state.getCompound("Armory").getList("Guns", Tag.TAG_COMPOUND).size() == 68, "The live armory does not contain the pinned pack's 68 actual guns");
                require(state.getCompound("Ship").getList("Callins", Tag.TAG_COMPOUND).size() >= 100, "The actual call-in catalogue is incomplete");
                require(state.getBoolean("CanDeploy"), "Campaign deployment unavailable: " + state.getString("Status"));
                capture(minecraft, "01-campaign-war-map-default"); next(200);
            }
            case 200 -> { if (captured("01-campaign-war-map-default")) { click(minecraft, "Neon Docks"); next(201); } }
            case 201 -> {
                if (++ticks < 12) return;
                for (int i = 0; i < 9; i++) click(minecraft, "+");
                click(minecraft, "25 minute operation"); next(202);
            }
            case 202 -> { if (++ticks >= 20) { require(button(minecraft, "30 minute operation").active, "Selected duration was not 30 minutes"); capture(minecraft, "02-campaign-plan-default"); next(203); } }
            case 203 -> { if (captured("02-campaign-plan-default")) { click(minecraft, "Call-ins"); next(204); } }
            case 204 -> { if (++ticks >= 20) { capture(minecraft, "03-callin-catalog-default"); next(205); } }
            case 205 -> { if (captured("03-callin-catalog-default")) { search(minecraft, "chromageddon"); next(206); } }
            case 206 -> { if (++ticks >= 12) { clickContains(minecraft, "CHROMAGEDDON"); next(207); } }
            case 207 -> {
                if (++ticks < 12) return;
                CompoundTag row = row(state.getCompound("Ship").getList("Callins", Tag.TAG_COMPOUND), "chromageddon");
                require(!row.getBoolean("Unlocked"), "Campaign audit needs its clean disposable baseline to prove the real CHROMAGEDDON purchase");
                require(row.getBoolean("CanPurchase"), "Real CHROMAGEDDON purchase was not available despite the logged requisition fixture");
                clickContains(minecraft, "Unlock"); next(208);
            }
            case 208 -> {
                if (!row(state.getCompound("Ship").getList("Callins", Tag.TAG_COMPOUND), "chromageddon").getBoolean("Unlocked") || ++ticks < 6) return;
                click(minecraft, "Slot 4"); next(209);
            }
            case 209 -> {
                ListTag equipped = state.getCompound("Ship").getList("Equipped", Tag.TAG_STRING);
                if (equipped.size() != 4 || !equipped.getString(3).equals("chromageddon") || ++ticks < 20) return;
                capture(minecraft, "04-chromageddon-equipped-default"); next(210);
            }
            case 210 -> {
                if (!captured("04-chromageddon-equipped-default")) return;
                click(minecraft, "War map"); click(minecraft, "Neon Docks"); clickContains(minecraft, "Review weapons"); click(minecraft, "Weapons & armor"); next(211);
            }
            case 211 -> { if (++ticks >= 20) { capture(minecraft, "05-armory-all-weapons-default"); next(212); } }
            case 212 -> { if (captured("05-armory-all-weapons-default")) { viewport(minecraft, 960, 720, 3); next(213); } }
            case 213 -> { if (minimum(minecraft) && ++ticks >= 20) { click(minecraft, "Armor"); next(214); } }
            case 214 -> { if (++ticks >= 12) { search(minecraft, "heavy"); next(215); } }
            case 215 -> { if (++ticks >= 12) { clickContains(minecraft, "Heavy field kit"); next(216); } }
            case 216 -> {
                if (!state.getCompound("Armory").getString("Armor").equals("heavy") || ++ticks < 20) return;
                capture(minecraft, "06-armor-heavy-minimum"); next(217);
            }
            case 217 -> { if (captured("06-armor-heavy-minimum")) { click(minecraft, "Primary"); search(minecraft, SELECTED_GUN); next(218); } }
            case 218 -> {
                if (++ticks < 12) return;
                CompoundTag gun = row(state.getCompound("Armory").getList("Guns", Tag.TAG_COMPOUND), SELECTED_GUN);
                require(!gun.isEmpty() && gun.getBoolean("Ready"), "The actual Breaker index is missing");
                String key = gun.getString("NameKey"), label = Component.translatable(key).getString();
                clickContains(minecraft, label.equals(key) ? gun.getString("Name") : label); next(219);
            }
            case 219 -> {
                if (!state.getCompound("Armory").getString("Primary").equals(SELECTED_GUN) || ++ticks < 20) return;
                capture(minecraft, "07-armory-weapon-search-minimum"); next(220);
            }
            case 220 -> { if (captured("07-armory-weapon-search-minimum")) { click(minecraft, "Back"); click(minecraft, "War map"); next(221); } }
            case 221 -> { if (++ticks >= 20) { capture(minecraft, "08-campaign-war-map-minimum"); next(222); } }
            case 222 -> { if (captured("08-campaign-war-map-minimum")) { click(minecraft, "Neon Docks"); next(223); } }
            case 223 -> { if (++ticks >= 20) { capture(minecraft, "09-campaign-plan-minimum"); next(224); } }
            case 224 -> { if (captured("09-campaign-plan-minimum")) { clickContains(minecraft, "Review weapons"); next(225); } }
            case 225 -> { if (++ticks >= 20) { capture(minecraft, "10-loadout-review-minimum"); next(226); } }
            case 226 -> { if (captured("10-loadout-review-minimum")) { minecraft.screen.mouseScrolled(160, 120, -100); next(227); } }
            case 227 -> { if (++ticks >= 12) { capture(minecraft, "10b-loadout-review-scroll-minimum"); next(228); } }
            case 228 -> {
                if (!captured("10b-loadout-review-scroll-minimum")) return;
                viewport(minecraft, 1280, 720, 2); minecraft.screen.mouseScrolled(320, 160, 100); next(229);
            }
            case 229 -> { if (++ticks >= 20) { capture(minecraft, "11-loadout-review-default"); next(230); } }
            case 230 -> {
                if (!captured("11-loadout-review-default")) return;
                originalDimension = minecraft.level.dimension().location().toString(); originalInventory = minecraft.player.getInventory().save(new ListTag()).copy();
                originalLevel = minecraft.player.experienceLevel; originalTotal = minecraft.player.totalExperience; originalProgress = minecraft.player.experienceProgress;
                minecraft.options.setCameraType(CameraType.THIRD_PERSON_BACK);
                clickContains(minecraft, "Confirm loadout"); next(25);
            }
            case 26 -> {
                if (!captured("12-live-pod") || !state.getString("Phase").equals("ACTIVE")) return;
                require(!minecraft.player.isPassenger(), "Selected campaign loadout failed to dismount its actual pod");
                if (!campaignStageQueued) { campaignStageQueued = true; stageCampaignCamera(minecraft); }
                next(231);
            }
            case 231 -> {
                if (!campaignStageReady || !minecraft.player.getAbilities().mayfly || ++ticks < 240) return;
                require(state.getInt("Difficulty") == 10 && state.getInt("ArenaRadius") == 768,
                        "The UI-selected campaign did not start its actual difficulty 10 arena");
                minecraft.player.getInventory().selected = 0; minecraft.options.setCameraType(CameraType.FIRST_PERSON);
                require(minecraft.player.getMainHandItem().hasTag() && minecraft.player.getMainHandItem().getTag().getString("GunId").equals(SELECTED_GUN),
                        "Deployment replaced the chosen native Breaker");
                next(2311);
            }
            case 2311 -> { if (++ticks >= 40) { capture(minecraft, "13-selected-native-gun-hud"); next(232); } }
            case 232 -> { if (captured("13-selected-native-gun-hud")) { openRadio(minecraft); next(233); } }
            case 233 -> {
                if (!(minecraft.screen instanceof LibertyCampaignScreen) || ++ticks < 12) return;
                if (!campaignBaselineReady) { if (!campaignBaselineQueued) { campaignBaselineQueued = true; campaignBaseline(minecraft); } return; }
                next(234);
            }
            case 234 -> {
                if (++ticks % 6 != 0) return;
                require(ticks < 120, "The equipped CHROMAGEDDON never appeared in the actual field radio");
                Button call = containsButton(minecraft, "CHROMAGEDDON");
                if (call != null) { require(call.active, "Equipped CHROMAGEDDON is not ready at its valid loaded target"); call.onPress(); next(235); }
                else click(minecraft, "Radio >");
            }
            case 235 -> {
                CompoundTag call = row(state.getList("Stratagems", Tag.TAG_COMPOUND), "chromageddon");
                if (call.isEmpty() || call.getInt("Uses") != 0 || call.getLong("Cooldown") <= 0) return;
                if (minecraft.screen != null) minecraft.screen.onClose(); next(236);
            }
            case 236 -> {
                if (minecraft.screen != null) minecraft.screen.onClose();
                if (++ticks % 10 == 0 && !campaignProofQueued) { campaignProofQueued = true; campaignPaintProof(minecraft); }
                if (!campaignPaintProved || ticks < 75 || clientPaint(minecraft) < 20) return;
                require(campaignProgress > 0 && campaignPaintEntities > 0, "CHROMAGEDDON had no genuine territory or entity result");
                capture(minecraft, "14-chromageddon-paint-blast"); next(237);
            }
            case 237 -> { if (captured("14-chromageddon-paint-blast")) { viewport(minecraft, 960, 720, 3); openRadio(minecraft); next(238); } }
            case 238 -> { if (minimum(minecraft) && minecraft.screen instanceof LibertyCampaignScreen && ++ticks >= 20) { capture(minecraft, "15-field-progress-minimum"); next(239); } }
            case 239 -> {
                if (!captured("15-field-progress-minimum")) return;
                click(minecraft, "Return home"); require(minecraft.screen instanceof ConfirmScreen, "Real early-return confirmation was missing");
                clickTranslation(minecraft, "gui.yes"); next(240);
            }
            case 240 -> {
                if (state.getBoolean("Active") || state.getBoolean("InShip") || !minecraft.level.dimension().location().toString().equals(originalDimension) || ++ticks < 20) return;
                requireOriginal(minecraft, "campaign early return");
                if (!campaignCleanupQueued) { campaignCleanupQueued = true; campaignCleanup(minecraft); return; }
                if (!campaignCleanupProved) return;
                click(minecraft, "Board your ship"); next(241);
            }
            case 241 -> { if (state.getBoolean("InShip") && minecraft.screen instanceof LibertyCampaignScreen && ++ticks >= 20) { click(minecraft, "Ship"); next(242); } }
            case 242 -> { if (++ticks >= 20) { capture(minecraft, "16-ship-hardware-minimum"); next(2421); } }
            case 2421 -> {
                if (!captured("16-ship-hardware-minimum")) return;
                click(minecraft, "Done"); viewport(minecraft, 1280, 720, 2); minecraft.options.setCameraType(CameraType.FIRST_PERSON);
                minecraft.player.getInventory().selected = 4; minecraft.player.setYRot(180); minecraft.player.yRotO = 180;
                minecraft.player.setXRot(8); minecraft.player.xRotO = 8;
                shipWalkStartX = minecraft.player.getX(); shipWalkStartZ = minecraft.player.getZ(); minecraft.options.keyUp.setDown(true); next(243);
            }
            case 243 -> {
                if (++ticks < 25) return;
                minecraft.options.keyUp.setDown(false);
                double dx = minecraft.player.getX() - shipWalkStartX, dz = minecraft.player.getZ() - shipWalkStartZ;
                require(dx * dx + dz * dz >= 1, "Ordinary forward input did not walk across the real ship deck");
                JetSetCraft.LOGGER.info("JETSETCRAFT_LIBERTY_CAMPAIGN_VISUAL_SHIP_WALK distance={} nativeForwardTicks=25", Math.sqrt(dx * dx + dz * dz)); next(244);
            }
            case 244 -> {
                if (++ticks < 20 || !minecraft.levelRenderer.isChunkCompiled(minecraft.player.blockPosition())) return;
                require(state.getBoolean("InShip") && !minecraft.level.getBlockState(minecraft.player.blockPosition().below()).isAir(), "Native ship deck failed its floor/residency check");
                capture(minecraft, "17-walkable-ship-deck"); next(245);
            }
            case 245 -> { if (captured("17-walkable-ship-deck")) { openRadio(minecraft); next(246); } }
            case 246 -> {
                if (!(minecraft.screen instanceof LibertyCampaignScreen) || ++ticks < 12) return;
                click(minecraft, "Ship"); click(minecraft, "Return to Minecraft"); next(247);
            }
            case 247 -> {
                if (state.getBoolean("Active") || state.getBoolean("InShip") || !minecraft.level.dimension().location().toString().equals(originalDimension) || ++ticks < 20) return;
                requireOriginal(minecraft, "ship return"); capture(minecraft, "18-returned-home-map"); next(248);
            }
            case 248 -> {
                if (!captured("18-returned-home-map")) return;
                JetSetCraft.LOGGER.info("JETSETCRAFT_LIBERTY_CAMPAIGN_VISUAL_PASS captures={} gun={} armor=heavy callin=chromageddon slot=4 difficulty=10 durationMinutes=30 paintEntities={} territoryProgress={} sourceInventoryXPAndDimensionRestoredTwice=true",
                        CAPTURES.size(), SELECTED_GUN, campaignPaintEntities, campaignProgress);
                finish(minecraft, true, "19 campaign native captures; real UI purchase/equip/weapon/armor selection and launch, moving pod, native Breaker, actual CHROMAGEDDON paint/progress, early return and walkable ship/home return. Explicit disposable requisition and camera/flight staging; no full-extraction, earned-unlock pacing or human-play claim.");
            }
            default -> throw new IllegalStateException("Unknown campaign native audit phase " + phase);
        }
    }

    private static void stageCampaignCamera(Minecraft minecraft) {
        campaignServer(minecraft, player -> {
            var operation = LibertyManager.operation(player);
            require(operation != null && operation.campaign() && operation.plan.difficulty().level() == 10
                    && operation.plan.durationMinutes() == 30, "The reviewed campaign plan did not reach the real server");
            var site = operation.mission.sites().stream().filter(s -> s.active() && !s.optional() && s.kind() == LibertyMissionCatalog.ObjectiveKind.TERRITORY).findFirst().orElseThrow();
            campaignMission = operation.id; campaignTerritory = site.center(); campaignSite = site.id();
            Vec3 position = Vec3.atCenterOf(site.center()).add(0, 20, 26), target = Vec3.atCenterOf(site.center()).add(0, .55, 0);
            Vec3 aim = target.subtract(position.add(0, player.getEyeHeight(), 0));
            float yaw = (float) Math.toDegrees(Math.atan2(-aim.x, aim.z));
            float pitch = (float) -Math.toDegrees(Math.atan2(aim.y, Math.sqrt(aim.x * aim.x + aim.z * aim.z)));
            player.getAbilities().invulnerable = true; player.getAbilities().mayfly = true; player.getAbilities().flying = true; player.onUpdateAbilities();
            player.teleportTo(player.serverLevel(), position.x, position.y, position.z, yaw, pitch); player.setDeltaMovement(Vec3.ZERO); player.fallDistance = 0;
            JetSetCraft.LOGGER.info("JETSETCRAFT_LIBERTY_CAMPAIGN_VISUAL_STAGING mission={} site={} target={} camera={} invulnerable=true flight=true; explicit disposable visual fixture, no objectives or timers patched",
                    operation.id, site.id(), target, position); campaignStageReady = true;
        });
    }
    private static void campaignBaseline(Minecraft minecraft) {
        campaignServer(minecraft, player -> {
            try {
                var operation = LibertyManager.operation(player); require(operation != null && operation.id.equals(campaignMission), "Visual fixture mission changed");
                var site = operation.mission.sites().stream().filter(s -> s.id().equals(campaignSite)).findFirst().orElseThrow();
                if (!player.serverLevel().getBlockState(site.center().north(15).above(2)).is(Blocks.WHITE_CONCRETE)) return;
                require(site.active() && site.progress() == 0, "Visual blast must start at an actual untouched active territory objective");
                for (int x = -12; x <= 12; x += 6) for (int z = -12; z <= 12; z += 6) {
                    BlockPos point = site.center().offset(x, 0, z); CAMPAIGN_TERRAIN.put(point, player.serverLevel().getBlockState(point));
                }
                campaignBaselineReady = true;
                JetSetCraft.LOGGER.info("JETSETCRAFT_LIBERTY_CAMPAIGN_VISUAL_BASELINE site={} progress=0 originalSurfaceSamples={}", site.id(), CAMPAIGN_TERRAIN.size());
            } finally { campaignBaselineQueued = false; }
        });
    }
    private static void campaignPaintProof(Minecraft minecraft) {
        campaignServer(minecraft, player -> {
            try {
                var operation = LibertyManager.operation(player); require(operation != null && operation.id.equals(campaignMission), "Paint proof lost its actual mission");
                var site = operation.mission.sites().stream().filter(s -> s.id().equals(campaignSite)).findFirst().orElseThrow();
                var entities = player.serverLevel().getEntitiesOfClass(GraffitiEntity.class, new AABB(campaignTerritory).inflate(70), entity -> LibertyManager.belongsTo(entity, player));
                if (site.progress() <= 0 || entities.isEmpty()) return;
                for (var entry : CAMPAIGN_TERRAIN.entrySet()) require(entry.getValue().equals(player.serverLevel().getBlockState(entry.getKey())), "CHROMAGEDDON changed an original terrain block");
                entities.forEach(entity -> CAMPAIGN_PAINT.add(entity.getUUID())); campaignProgress = site.progress(); campaignPaintEntities = entities.size();
                if (!campaignPaintProved) JetSetCraft.LOGGER.info("JETSETCRAFT_LIBERTY_CAMPAIGN_VISUAL_PAINT mission={} site={} actualProgress={} goal={} actualOwnedGraffiti={} originalSurfaceSamplesUnchanged={}; produced by the real UI call-in, not injected completion",
                        operation.id, site.id(), site.progress(), site.goal(), entities.size(), CAMPAIGN_TERRAIN.size());
                campaignPaintProved = true;
            } finally { campaignProofQueued = false; }
        });
    }
    private static void campaignCleanup(Minecraft minecraft) {
        campaignServer(minecraft, player -> {
            require(LibertyManager.operation(player) == null && !LibertyManager.hasReturn(player), "Native early return retained an active operation or return journal");
            var district = player.getServer().getLevel(com.herberto.jetsetcraft.liberty.LibertyWorld.DIMENSION);
            for (UUID id : CAMPAIGN_PAINT) { var entity = district.getEntity(id); require(entity == null || entity.isRemoved(), "A temporary mission paint entity survived the real return"); }
            JetSetCraft.LOGGER.info("JETSETCRAFT_LIBERTY_CAMPAIGN_VISUAL_CLEANUP verifiedRemovedPaintEntities={}", CAMPAIGN_PAINT.size()); campaignCleanupProved = true;
        });
    }
    private static void campaignServer(Minecraft minecraft, java.util.function.Consumer<net.minecraft.server.level.ServerPlayer> task) {
        require(campaignAudit() && disposableServer(minecraft), "Campaign fixture left its explicit disposable world");
        var server = minecraft.getSingleplayerServer(); UUID owner = minecraft.player.getUUID();
        server.execute(() -> {
            try {
                require(campaignAudit() && server.getWorldPath(LevelResource.ROOT).toAbsolutePath().normalize().equals(expected(minecraft)), "Campaign server fixture lost its save guard");
                var player = server.getPlayerList().getPlayer(owner); require(player != null, "Campaign fixture player disconnected"); task.accept(player);
            } catch (RuntimeException error) { failure = "Campaign native fixture: " + error.getMessage(); }
        });
    }
    private static int clientPaint(Minecraft minecraft) {
        int count = 0; for (var entity : minecraft.level.entitiesForRendering()) if (entity instanceof GraffitiEntity && entity.position().distanceToSqr(Vec3.atCenterOf(campaignTerritory)) < 70 * 70) count++; return count;
    }
    private static void requireOriginal(Minecraft minecraft, String label) {
        require(minecraft.level.dimension().location().toString().equals(originalDimension) && minecraft.player.getInventory().save(new ListTag()).equals(originalInventory), label + " changed the source dimension or deep original inventory NBT");
        require(minecraft.player.experienceLevel == originalLevel && minecraft.player.totalExperience == originalTotal
                && Math.abs(minecraft.player.experienceProgress - originalProgress) < .0001F, label + " changed original XP");
        JetSetCraft.LOGGER.info("JETSETCRAFT_LIBERTY_CAMPAIGN_VISUAL_RETURN {} originalInventoryNBT=true originalXP=true originalDimension={}", label, originalDimension);
    }
    private static void openRadio(Minecraft minecraft) {
        minecraft.player.getInventory().selected = 4;
        require(minecraft.gameMode != null && minecraft.player.getMainHandItem().is(Items.COMPASS) && minecraft.player.getMainHandItem().hasTag()
                && minecraft.player.getMainHandItem().getTag().getBoolean("LibertyFieldRadio"), "Actual field / ship radio is missing");
        minecraft.gameMode.useItem(minecraft.player, net.minecraft.world.InteractionHand.MAIN_HAND);
    }
    private static void search(Minecraft minecraft, String text) {
        require(minecraft.screen != null, "No native search screen");
        for (var child : minecraft.screen.children()) if (child instanceof EditBox box && box.visible) { box.setValue(text); return; }
        throw new IllegalStateException("Actual searchable catalogue has no EditBox");
    }
    private static Button containsButton(Minecraft minecraft, String text) {
        if (minecraft.screen != null) for (var child : minecraft.screen.children()) if (child instanceof Button button && button.visible && button.getMessage().getString().contains(text)) return button;
        return null;
    }
    private static CompoundTag row(ListTag rows, String id) { for (Tag value : rows) { CompoundTag row = (CompoundTag) value; if (row.getString("Id").equals(id)) return row; } return new CompoundTag(); }
    private static boolean minimum(Minecraft minecraft) { return minecraft.getWindow().getGuiScaledWidth() == 320 && minecraft.getWindow().getGuiScaledHeight() == 240; }
    private static boolean campaignAudit() { return Boolean.getBoolean("jetsetcraft.libertyCampaignVisualAudit"); }

    private static void fixture(Minecraft minecraft) {
        require(disposableServer(minecraft), "Vanilla fixture requires the exact disposable integrated save");
        var server = minecraft.getSingleplayerServer(); UUID owner = minecraft.player.getUUID();
        server.execute(() -> {
            try {
                require(Boolean.getBoolean("jetsetcraft.libertyVisualAudit") && server.getWorldPath(LevelResource.ROOT).toAbsolutePath().normalize().equals(expected(minecraft)), "Fixture lost its explicit save guard");
                var player = server.getPlayerList().getPlayer(owner); require(player != null, "Fixture player disconnected");
                var source = server.createCommandSourceStack().withPermission(4).withEntity(player).withSuppressedOutput();
                if (server.getWorldData().getDifficulty() != net.minecraft.world.Difficulty.NORMAL)
                    server.getCommands().performPrefixedCommand(source, "difficulty normal");
                require(server.getWorldData().getDifficulty() == net.minecraft.world.Difficulty.NORMAL
                        && player.serverLevel().getDifficulty() == net.minecraft.world.Difficulty.NORMAL,
                        "Could not configure disposable difficulty");
                if (!player.isCreative()) server.getCommands().performPrefixedCommand(source, "gamemode creative @s");
                require(player.isCreative(), "Could not configure disposable creative origin");
                for (String command : new String[]{"give @s minecraft:diamond 7", "experience add @s 17 points"})
                    require(server.getCommands().performPrefixedCommand(source, command) > 0, "Vanilla audit setup command failed: " + command);
                if (campaignAudit()) {
                    ItemStack container = new ItemStack(Items.SHULKER_BOX); CompoundTag contents = new CompoundTag(); ListTag nested = new ListTag();
                    ItemStack emeralds = new ItemStack(Items.EMERALD, 5); emeralds.getOrCreateTag().putString("LibertyAuditNestedSentinel", STAMP);
                    CompoundTag item = emeralds.save(new CompoundTag()); item.putByte("Slot", (byte) 3); nested.add(item); contents.put("Items", nested);
                    container.getOrCreateTag().put("BlockEntityTag", contents); container.getOrCreateTag().putString("LibertyAuditReturnSentinel", STAMP);
                    require(player.getInventory().add(container), "Disposable source inventory has no room for its deep return sentinel");
                    UUID receipt = UUID.randomUUID();
                    int before = LibertyShipProgress.snapshot(player).getInt("Requisitions");
                    require(LibertyShipProgress.settle(player, receipt, 6, 10, true), "The explicit ship settlement fixture was rejected");
                    int after = LibertyShipProgress.snapshot(player).getInt("Requisitions");
                    require(after - before == 510, "Ship fixture did not grant its documented 510 requisitions");
                    player.getInventory().setChanged(); player.inventoryMenu.broadcastChanges();
                    JetSetCraft.LOGGER.info("JETSETCRAFT_LIBERTY_CAMPAIGN_VISUAL_FIXTURE receipt={} requisitionsBefore={} requisitionsAfter={} grant=510 via=LibertyShipProgress.settle completedFixture=6 difficultyFixture=10 extractedFixture=true; disposable UI funding, NOT earned gameplay or extraction evidence; nested source inventory/XP sentinels added", receipt, before, after);
                } else JetSetCraft.LOGGER.info("JETSETCRAFT_LIBERTY_VISUAL_AUDIT_FIXTURE vanilla difficulty/creative/sentinel inventory and XP only; no mission or research awards");
                fixtureReady = true;
            } catch (RuntimeException error) { failure = "Vanilla fixture: " + error.getMessage(); }
        });
    }
    private static Path expected(Minecraft minecraft) { return minecraft.gameDirectory.toPath().resolve("saves").resolve(WORLD).toAbsolutePath().normalize(); }
    private static boolean disposableServer(Minecraft minecraft) {
        var server = minecraft.getSingleplayerServer();
        return server != null && server.getWorldPath(LevelResource.ROOT).toAbsolutePath().normalize().equals(expected(minecraft));
    }
    private static boolean isDisposableLaunch(Minecraft minecraft) {
        if (!Files.isRegularFile(expected(minecraft).resolve("level.dat"))) return false;
        String[] arguments = ProcessHandle.current().info().arguments().orElse(new String[0]);
        int process = target(arguments), command = target(System.getProperty("sun.java.command", "").trim().split("\\s+"));
        return process >= 0 && command >= 0 && (process == 1 || process == 0 && command == 1);
    }
    private static int target(String[] arguments) {
        int matches = 0;
        for (int i = 0; i < arguments.length; i++) {
            if (arguments[i].equals("--quickPlaySingleplayer")) { if (++i >= arguments.length || !arguments[i].equals(WORLD)) return -1; matches++; }
            else if (arguments[i].startsWith("--quickPlaySingleplayer=")) { if (!arguments[i].equals("--quickPlaySingleplayer=" + WORLD)) return -1; matches++; }
        }
        return matches > 1 ? -1 : matches;
    }
    private static void worldConfirmation(Minecraft minecraft) {
        Screen screen = minecraft.screen;
        if (phase != 0 || screen == null || screen == confirmedScreen || minecraft.getSingleplayerServer() != null) return;
        String title = translation(screen.getTitle()), key;
        if (screen.getClass() == ConfirmScreen.class && title.equals("selectWorld.backupQuestion.experimental")) key = "gui.proceed";
        else if (screen.getClass() == BackupConfirmScreen.class && (title.equals("selectWorld.backupQuestion.experimental") || title.equals("selectWorld.backupQuestion.customized"))) key = "selectWorld.backupJoinSkipButton";
        else return;
        require(isDisposableLaunch(minecraft), "Refused confirmation outside the named disposable audit world");
        confirmedScreen = screen; clickTranslation(minecraft, key);
    }
    private static String firstProject() { return LibertyClient.research().getList("Projects", Tag.TAG_COMPOUND).getCompound(0).getString("Name"); }
    private static String translation(Component text) { return text.getContents() instanceof TranslatableContents value ? value.getKey() : ""; }
    private static Button button(Minecraft minecraft, String label) {
        require(minecraft.screen != null, "No screen for button " + label);
        for (var child : minecraft.screen.children()) if (child instanceof Button button && button.visible && button.getMessage().getString().equals(label)) return button;
        throw new IllegalStateException("Missing real button: " + label);
    }
    private static void click(Minecraft minecraft, String label) { Button button = button(minecraft, label); require(button.active, "Disabled real button: " + label); button.onPress(); }
    private static void clickContains(Minecraft minecraft, String label) {
        require(minecraft.screen != null, "No screen for choice " + label);
        for (var child : minecraft.screen.children()) if (child instanceof Button button && button.visible && button.active && button.getMessage().getString().contains(label)) { button.onPress(); return; }
        throw new IllegalStateException("Missing real choice: " + label);
    }
    private static void clickTranslation(Minecraft minecraft, String key) {
        require(minecraft.screen != null, "No screen for translated button " + key);
        for (var child : minecraft.screen.children()) if (child instanceof Button button && button.visible && button.active && translation(button.getMessage()).equals(key)) { button.onPress(); return; }
        throw new IllegalStateException("Missing translated button: " + key);
    }
    private static void viewport(Minecraft minecraft, int width, int height, int scale) {
        minecraft.getWindow().setWindowed(width, height); minecraft.options.guiScale().set(scale); minecraft.resizeDisplay();
    }
    private static void next(int value) { phase = value; ticks = 0; phaseStartedAt = System.nanoTime(); }
    private static boolean captured(String label) { return CAPTURES.containsKey(label); }
    private static void capture(Minecraft minecraft, String label) {
        String filename = "liberty-grind-" + STAMP + "-" + label + ".png";
        Path path = minecraft.gameDirectory.toPath().resolve("screenshots").resolve(filename);
        Screenshot.grab(minecraft.gameDirectory, filename, minecraft.getMainRenderTarget(), response -> {
            try {
                require(Files.isRegularFile(path) && Files.size(path) > 1024, "Screenshot not written: " + label);
                try (var bytes = Files.newInputStream(path)) { require(Arrays.equals(PNG, bytes.readNBytes(PNG.length)), "Invalid screenshot PNG: " + label); }
                CAPTURES.put(label, path);
                JetSetCraft.LOGGER.info("JETSETCRAFT_LIBERTY_VISUAL_AUDIT_CAPTURE {} {}", label, path.toAbsolutePath());
            } catch (IOException | RuntimeException error) { failure = "Capture: " + error.getMessage(); }
        });
    }
    private static void finish(Minecraft minecraft, boolean passed, String message) {
        if (complete || failureStarted != 0) return;
        JetSetCraft.LOGGER.info("JETSETCRAFT_LIBERTY_VISUAL_AUDIT_{} {}", passed ? "PASS" : "FAIL", message);
        if (!passed) {
            failureStarted = System.nanoTime();
            if (minecraft.screen != null) JetSetCraft.LOGGER.error("JETSETCRAFT_LIBERTY_VISUAL_AUDIT_SCREEN {} {}", minecraft.screen.getClass().getName(), minecraft.screen.getTitle().getString());
            capture(minecraft, "99-failure"); return;
        }
        stop(minecraft);
    }
    private static void stop(Minecraft minecraft) {
        complete = true;
        if (configured) {
            minecraft.options.guiScale().set(originalScale); minecraft.options.setCameraType(originalCamera);
            minecraft.options.keyUp.setDown(originalForward);
            minecraft.getWindow().setWindowed(originalWidth, originalHeight); minecraft.resizeDisplay();
            minecraft.getTutorial().setStep(originalTutorial);
            var hud = LibertyHudPreferences.get(); hud.x = originalHudX; hud.y = originalHudY;
            hud.scale = originalHudScale; hud.visible = originalHudVisible; hud.save();
        }
        minecraft.stop();
    }
    private static void require(boolean condition, String message) { if (!condition) throw new IllegalStateException(message); }
    private LibertyVisualAudit() {}
}
