package com.herberto.jetsetcraft.client;

import com.herberto.jetsetcraft.JetSetCraft;
import com.herberto.jetsetcraft.client.animation.RideAnimationController;
import com.herberto.jetsetcraft.client.render.BoomboxRenderer;
import com.herberto.jetsetcraft.client.render.GraffitiRenderer;
import com.herberto.jetsetcraft.client.render.RideGearLayer;
import com.herberto.jetsetcraft.client.render.MobRideGearLayer;
import com.herberto.jetsetcraft.client.state.ClientRideState;
import com.herberto.jetsetcraft.client.state.ClientMobGearState;
import com.herberto.jetsetcraft.config.JetSetConfig;
import com.herberto.jetsetcraft.movement.DanceCatalog;
import com.herberto.jetsetcraft.movement.TrickCatalog;
import com.herberto.jetsetcraft.network.InputFlags;
import com.herberto.jetsetcraft.network.JetSetNetwork;
import com.herberto.jetsetcraft.registry.ModBlockEntities;
import com.herberto.jetsetcraft.registry.ModEntities;
import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.ChatFormatting;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.entity.player.PlayerRenderer;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.world.entity.EntityType;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.event.entity.EntityLeaveLevelEvent;
import net.minecraft.network.chat.Component;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.EntityRenderersEvent;
import net.minecraftforge.client.event.RegisterKeyMappingsEvent;
import net.minecraftforge.client.event.RenderGuiOverlayEvent;
import net.minecraftforge.client.event.ViewportEvent;
import net.minecraftforge.client.settings.KeyConflictContext;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.lwjgl.glfw.GLFW;

import java.util.Locale;

public final class ClientEvents {
    private static final String CATEGORY = "key.categories.jetsetcraft";
    public static final KeyMapping BOOST = new KeyMapping("key.jetsetcraft.boost", KeyConflictContext.IN_GAME, InputConstants.Type.KEYSYM, GLFW.GLFW_KEY_LEFT_ALT, CATEGORY);
    public static final KeyMapping TRICK = new KeyMapping("key.jetsetcraft.trick", KeyConflictContext.IN_GAME, InputConstants.Type.KEYSYM, GLFW.GLFW_KEY_R, CATEGORY);
    public static final KeyMapping GRIND = new KeyMapping("key.jetsetcraft.grind", KeyConflictContext.IN_GAME, InputConstants.Type.KEYSYM, GLFW.GLFW_KEY_G, CATEGORY);
    public static final KeyMapping MANUAL = new KeyMapping("key.jetsetcraft.manual", KeyConflictContext.IN_GAME, InputConstants.Type.KEYSYM, GLFW.GLFW_KEY_C, CATEGORY);
    public static final KeyMapping BRAKE = new KeyMapping("key.jetsetcraft.brake", KeyConflictContext.IN_GAME, InputConstants.Type.KEYSYM, GLFW.GLFW_KEY_V, CATEGORY);
    public static final KeyMapping DANCE = new KeyMapping("key.jetsetcraft.dance", KeyConflictContext.IN_GAME, InputConstants.Type.KEYSYM, GLFW.GLFW_KEY_B, CATEGORY);
    public static final KeyMapping RIDE_TOGGLE = new KeyMapping("key.jetsetcraft.ride_toggle", KeyConflictContext.IN_GAME, InputConstants.Type.KEYSYM, GLFW.GLFW_KEY_K, CATEGORY);
    private static int lastMask = -1;
    private static float lastForward = Float.NaN;
    private static float lastStrafe = Float.NaN;
    private static int heartbeat;

    @Mod.EventBusSubscriber(modid = JetSetCraft.MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
    public static final class ModBus {
        @SubscribeEvent
        public static void registerKeys(RegisterKeyMappingsEvent event) {
            event.register(BOOST);
            event.register(TRICK);
            event.register(GRIND);
            event.register(MANUAL);
            event.register(BRAKE);
            event.register(DANCE);
            event.register(RIDE_TOGGLE);
        }

        @SubscribeEvent
        public static void registerRenderers(EntityRenderersEvent.RegisterRenderers event) {
            event.registerEntityRenderer(ModEntities.GRAFFITI.get(), GraffitiRenderer::new);
            event.registerBlockEntityRenderer(ModBlockEntities.BOOMBOX.get(), BoomboxRenderer::new);
        }

        @SubscribeEvent
        public static void addPlayerLayers(EntityRenderersEvent.AddLayers event) {
            for (String skin : event.getSkins()) {
                var renderer = event.getSkin(skin);
                if (renderer instanceof PlayerRenderer playerRenderer) {
                    playerRenderer.addLayer(new RideGearLayer(playerRenderer));
                }
            }
            for (EntityType<?> type : ForgeRegistries.ENTITY_TYPES.getValues()) {
                if (type == EntityType.PLAYER) continue;
                addMobLayer(event, type);
            }
        }

        @SuppressWarnings({"rawtypes", "unchecked"})
        private static void addMobLayer(EntityRenderersEvent.AddLayers event, EntityType<?> type) {
            LivingEntityRenderer renderer = event.getRenderer((EntityType) type);
            if (renderer != null) renderer.addLayer(new MobRideGearLayer(renderer));
        }
    }

    @Mod.EventBusSubscriber(modid = JetSetCraft.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE, value = Dist.CLIENT)
    public static final class ForgeBus {
        @SubscribeEvent
        public static void clientTick(TickEvent.ClientTickEvent event) {
            if (event.phase != TickEvent.Phase.END) return;
            Minecraft mc = Minecraft.getInstance();
            if (mc.player == null || mc.level == null) {
                lastMask = -1;
                lastForward = Float.NaN;
                lastStrafe = Float.NaN;
                heartbeat = 0;
                ClientRideState.reset();
                ClientMobGearState.reset();
                RideAnimationController.reset();
                return;
            }
            boolean gameplayInput = mc.screen == null;
            while (RIDE_TOGGLE.consumeClick()) {
                // Always drain queued clicks so a key pressed in chat/inventory cannot fire after the screen closes.
                if (gameplayInput) JetSetNetwork.sendRideLoadoutAction(mc.options.keyShift.isDown());
            }

            int mask = 0;
            float forward = 0.0F;
            float strafe = 0.0F;
            if (gameplayInput) {
                if (BOOST.isDown()) mask |= InputFlags.BOOST;
                if (TRICK.isDown()) mask |= InputFlags.TRICK;
                if (GRIND.isDown()) mask |= InputFlags.GRIND;
                if (MANUAL.isDown()) mask |= InputFlags.MANUAL;
                if (BRAKE.isDown()) mask |= InputFlags.BRAKE;
                if (DANCE.isDown()) mask |= InputFlags.DANCE;
                if (mc.options.keyJump.isDown()) mask |= InputFlags.JUMP;
                if (mc.options.keyShift.isDown()) mask |= InputFlags.SNEAK;
                forward = finiteUnit(mc.player.input.forwardImpulse);
                strafe = finiteUnit(mc.player.input.leftImpulse);
            }

            ClientRideState.Snapshot snapshot = ClientRideState.get(mc.player.getId());
            boolean analogChanged = Float.floatToIntBits(forward) != Float.floatToIntBits(lastForward)
                    || Float.floatToIntBits(strafe) != Float.floatToIntBits(lastStrafe);
            heartbeat++;
            if (mask != lastMask || analogChanged || snapshot.active() || snapshot.dancing() || heartbeat >= 5) {
                JetSetNetwork.sendInput(mask, forward, strafe);
                lastMask = mask;
                lastForward = forward;
                lastStrafe = strafe;
                heartbeat = 0;
            }
        }

        private static float finiteUnit(float value) {
            if (!Float.isFinite(value)) return 0.0F;
            return Math.max(-1.0F, Math.min(1.0F, value));
        }

        @SubscribeEvent
        public static void entityLeave(EntityLeaveLevelEvent event) {
            if (!event.getLevel().isClientSide) return;
            int entityId = event.getEntity().getId();
            ClientRideState.remove(entityId);
            ClientMobGearState.remove(entityId);
            RideAnimationController.remove(entityId);
        }

        @SubscribeEvent
        public static void cameraAngles(ViewportEvent.ComputeCameraAngles event) {
            Minecraft mc = Minecraft.getInstance();
            if (mc.player == null) return;
            ClientRideState.Snapshot state = ClientRideState.get(mc.player.getId());
            if (!state.active() || state.dancing() || JetSetConfig.CLIENT.reducedMotion.get()
                    || !JetSetConfig.CLIENT.dynamicCamera.get()) return;
            float roll = 0.0f;
            float strafe = finiteUnit(mc.player.input.leftImpulse);
            if (state.wallRiding()) roll = state.wallSide() * 8.0f;
            else if (state.powersliding()) roll = -strafe * 5.0f;
            else if (state.grinding()) roll = -strafe * 2.5f;
            if (com.herberto.jetsetcraft.compat.CompatManager.hasWeaponOverlay(mc.player)) roll *= 0.35f;
            roll *= JetSetConfig.CLIENT.cameraRollScale.get().floatValue();
            event.setRoll(event.getRoll() + roll);
        }

        @SubscribeEvent
        public static void fov(ViewportEvent.ComputeFov event) {
            Minecraft mc = Minecraft.getInstance();
            if (mc.player == null) return;
            ClientRideState.Snapshot state = ClientRideState.get(mc.player.getId());
            if (!state.active() || state.dancing() || JetSetConfig.CLIENT.reducedMotion.get()
                    || !JetSetConfig.CLIENT.dynamicFov.get()) return;
            double speedEffect = Math.min(1.0, Math.max(0.0, state.momentum() / 0.90f));
            double extra = JetSetConfig.CLIENT.maxExtraFov.get() * speedEffect;
            if (state.boosting()) extra += JetSetConfig.CLIENT.boostExtraFov.get();
            if (com.herberto.jetsetcraft.compat.CompatManager.hasWeaponOverlay(mc.player)) extra *= 0.45;
            event.setFOV(event.getFOV() + extra);
        }

        @SubscribeEvent
        public static void hud(RenderGuiOverlayEvent.Post event) {
            Minecraft mc = Minecraft.getInstance();
            if (mc.player == null || mc.options.hideGui || !JetSetConfig.CLIENT.showStyleHud.get()) return;
            ClientRideState.Snapshot state = ClientRideState.get(mc.player.getId());
            if (!state.active() && !state.dancing() && state.comboScore() == 0 && state.landingTicks() == 0) return;

            GuiGraphics gui = event.getGuiGraphics();
            int width = mc.getWindow().getGuiScaledWidth();
            int height = mc.getWindow().getGuiScaledHeight();
            int panelWidth = 196;
            int x = width / 2 - panelWidth / 2;
            int y = height - 76;
            gui.fill(x - 5, y - 5, x + panelWidth + 5, y + 57, 0xB0101018);
            gui.fill(x - 5, y - 5, x + 3, y + 57, 0xFFE938A8);

            String modeLabel;
            if (state.dancing()) {
                modeLabel = state.danceStyle().displayName().toUpperCase(Locale.ROOT) + "  •  "
                        + DanceCatalog.name(state.danceMoveId()).toUpperCase(Locale.ROOT);
                if (state.cypherSize() > 1) modeLabel += "  CYPHER x" + state.cypherSize();
            } else if (state.active()) {
                modeLabel = state.style().serializedName().toUpperCase(Locale.ROOT);
                if (state.grinding()) modeLabel += "  •  " + state.grindKind().serializedName().toUpperCase(Locale.ROOT);
            } else {
                modeLabel = "STYLE CHAIN";
            }
            Component mode = Component.literal(modeLabel).withStyle(state.dancing() ? ChatFormatting.LIGHT_PURPLE
                    : state.grinding() ? ChatFormatting.GOLD : ChatFormatting.AQUA);
            gui.drawString(mc.font, mode, x, y, 0xFFFFFF, true);
            String rank = TrickCatalog.rankName(state.comboScore(), state.comboMultiplier(), state.flow());
            gui.drawString(mc.font, Component.literal(rank), x + panelWidth - mc.font.width(rank), y, 0xFFFFD33D, true);

            int boostWidth = Math.round(88.0f * clamp01(state.boost() / 100.0f));
            int flowWidth = Math.round(88.0f * clamp01(state.flow() / 100.0f));
            gui.fill(x, y + 13, x + 88, y + 19, 0xFF262630);
            gui.fill(x, y + 13, x + boostWidth, y + 19, 0xFFEAF23A);
            gui.fill(x + 104, y + 13, x + 192, y + 19, 0xFF262630);
            gui.fill(x + 104, y + 13, x + 104 + flowWidth, y + 19, 0xFF34E1E5);
            gui.drawString(mc.font, Component.literal("BOOST " + Math.round(state.boost())), x, y + 22, 0xFFF3F3F3, false);
            gui.drawString(mc.font, Component.literal("FLOW " + Math.round(state.flow())), x + 104, y + 22, 0xFFF3F3F3, false);

            if (state.comboScore() > 0) {
                String combo = state.comboScore() + "  x" + String.format(Locale.ROOT, "%.2f", state.comboMultiplier());
                gui.drawString(mc.font, Component.literal(combo), x, y + 34, 0xFFFF78D7, true);
            }
            if (JetSetConfig.CLIENT.showTrickNames.get() && state.dancing()) {
                String dance = DanceCatalog.name(state.danceMoveId());
                gui.drawString(mc.font, Component.literal(dance), x + panelWidth - mc.font.width(dance), y + 34,
                        0xFFFFA7E3, true);
            } else if (JetSetConfig.CLIENT.showTrickNames.get() && state.trickTicks() > 0) {
                String trick = (state.boostTrick() ? "BOOST • " : "")
                        + TrickCatalog.name(state.trickIndex(), state.style());
                gui.drawString(mc.font, Component.literal(trick), x + panelWidth - mc.font.width(trick), y + 34,
                        state.boostTrick() ? 0xFFEAF23A : state.groundStunt() ? 0xFFFFA633 : 0xFFFAFAFA, true);
            } else if (JetSetConfig.CLIENT.showTrickNames.get() && state.landingTicks() > 0) {
                String landing = TrickCatalog.landingName(state.landingGrade());
                gui.drawString(mc.font, Component.literal(landing), x + panelWidth - mc.font.width(landing), y + 34,
                        state.landingGrade() == 3 ? 0xFF65FF8F : 0xFFFFFFFF, true);
            }
        }

        private static float clamp01(float value) { return Math.max(0.0f, Math.min(1.0f, value)); }
    }

    private ClientEvents() {}
}
