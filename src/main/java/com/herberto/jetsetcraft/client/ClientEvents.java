package com.herberto.jetsetcraft.client;

import com.herberto.jetsetcraft.JetSetCraft;
import com.herberto.jetsetcraft.client.render.GraffitiRenderer;
import com.herberto.jetsetcraft.client.render.RideGearLayer;
import com.herberto.jetsetcraft.client.state.ClientRideState;
import com.herberto.jetsetcraft.network.InputFlags;
import com.herberto.jetsetcraft.network.JetSetNetwork;
import com.herberto.jetsetcraft.registry.ModEntities;
import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.ChatFormatting;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.entity.player.PlayerRenderer;
import net.minecraft.network.chat.Component;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.EntityRenderersEvent;
import net.minecraftforge.client.event.RegisterKeyMappingsEvent;
import net.minecraftforge.client.event.RenderGuiOverlayEvent;
import net.minecraftforge.client.event.ViewportEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.lwjgl.glfw.GLFW;

public final class ClientEvents {
    private static final String CATEGORY = "key.categories.jetsetcraft";
    public static final KeyMapping BOOST = new KeyMapping("key.jetsetcraft.boost", InputConstants.Type.KEYSYM, GLFW.GLFW_KEY_LEFT_ALT, CATEGORY);
    public static final KeyMapping TRICK = new KeyMapping("key.jetsetcraft.trick", InputConstants.Type.KEYSYM, GLFW.GLFW_KEY_R, CATEGORY);
    public static final KeyMapping GRIND = new KeyMapping("key.jetsetcraft.grind", InputConstants.Type.KEYSYM, GLFW.GLFW_KEY_G, CATEGORY);
    public static final KeyMapping MANUAL = new KeyMapping("key.jetsetcraft.manual", InputConstants.Type.KEYSYM, GLFW.GLFW_KEY_C, CATEGORY);
    public static final KeyMapping BRAKE = new KeyMapping("key.jetsetcraft.brake", InputConstants.Type.KEYSYM, GLFW.GLFW_KEY_V, CATEGORY);
    public static final KeyMapping RIDE_TOGGLE = new KeyMapping("key.jetsetcraft.ride_toggle", InputConstants.Type.KEYSYM, GLFW.GLFW_KEY_K, CATEGORY);
    private static int lastMask = -1;
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
            event.register(RIDE_TOGGLE);
        }

        @SubscribeEvent
        public static void registerRenderers(EntityRenderersEvent.RegisterRenderers event) {
            event.registerEntityRenderer(ModEntities.GRAFFITI.get(), GraffitiRenderer::new);
        }

        @SubscribeEvent
        public static void addPlayerLayers(EntityRenderersEvent.AddLayers event) {
            for (String skin : event.getSkins()) {
                var renderer = event.getSkin(skin);
                if (renderer instanceof PlayerRenderer playerRenderer) {
                    playerRenderer.addLayer(new RideGearLayer(playerRenderer));
                }
            }
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
                heartbeat = 0;
                ClientRideState.reset();
                return;
            }
            while (RIDE_TOGGLE.consumeClick()) {
                JetSetNetwork.sendRideLoadoutAction(mc.options.keyShift.isDown());
            }
            int mask = 0;
            if (BOOST.isDown()) mask |= InputFlags.BOOST;
            if (TRICK.isDown()) mask |= InputFlags.TRICK;
            if (GRIND.isDown()) mask |= InputFlags.GRIND;
            if (MANUAL.isDown()) mask |= InputFlags.MANUAL;
            if (BRAKE.isDown()) mask |= InputFlags.BRAKE;
            if (mc.options.keyJump.isDown()) mask |= InputFlags.JUMP;

            float forward = mc.player.input.forwardImpulse;
            float strafe = mc.player.input.leftImpulse;
            ClientRideState.Snapshot snapshot = ClientRideState.get(mc.player.getId());
            heartbeat++;
            if (mask != lastMask || snapshot.active() || heartbeat >= 5) {
                JetSetNetwork.sendInput(mask, forward, strafe);
                lastMask = mask;
                heartbeat = 0;
            }
        }

        @SubscribeEvent
        public static void cameraAngles(ViewportEvent.ComputeCameraAngles event) {
            Minecraft mc = Minecraft.getInstance();
            if (mc.player == null) return;
            ClientRideState.Snapshot state = ClientRideState.get(mc.player.getId());
            if (!state.active() || !com.herberto.jetsetcraft.config.JetSetConfig.CLIENT.dynamicCamera.get()) return;
            float roll = 0.0f;
            if (state.wallRiding()) roll = state.wallSide() * 8.0f;
            else if (state.powersliding()) roll = -mc.player.input.leftImpulse * 5.0f;
            else if (state.grinding()) roll = -mc.player.input.leftImpulse * 2.5f;
            if (com.herberto.jetsetcraft.compat.CompatManager.hasWeaponOverlay(mc.player)) roll *= 0.35f;
            roll *= com.herberto.jetsetcraft.config.JetSetConfig.CLIENT.cameraRollScale.get().floatValue();
            event.setRoll(event.getRoll() + roll);
        }

        @SubscribeEvent
        public static void fov(ViewportEvent.ComputeFov event) {
            Minecraft mc = Minecraft.getInstance();
            if (mc.player == null) return;
            ClientRideState.Snapshot state = ClientRideState.get(mc.player.getId());
            if (!state.active() || !com.herberto.jetsetcraft.config.JetSetConfig.CLIENT.dynamicFov.get()) return;
            double speedEffect = Math.min(1.0, Math.max(0.0, state.momentum() / 0.90f));
            double extra = com.herberto.jetsetcraft.config.JetSetConfig.CLIENT.maxExtraFov.get() * speedEffect;
            if (state.boosting()) extra += com.herberto.jetsetcraft.config.JetSetConfig.CLIENT.boostExtraFov.get();
            if (com.herberto.jetsetcraft.compat.CompatManager.hasWeaponOverlay(mc.player)) extra *= 0.45;
            event.setFOV(event.getFOV() + extra);
        }

        @SubscribeEvent
        public static void hud(RenderGuiOverlayEvent.Post event) {
            Minecraft mc = Minecraft.getInstance();
            if (mc.player == null || mc.options.hideGui) return;
            ClientRideState.Snapshot state = ClientRideState.get(mc.player.getId());
            if (!state.active()) return;
            GuiGraphics gui = event.getGuiGraphics();
            int width = mc.getWindow().getGuiScaledWidth();
            int x = width / 2 - 70;
            int y = mc.getWindow().getGuiScaledHeight() - 54;

            gui.fill(x - 4, y - 4, x + 144, y + 34, 0x88000000);
            String modeLabel = state.style().serializedName().toUpperCase();
            if (state.grinding()) modeLabel += "  •  " + state.grindKind().serializedName().toUpperCase();
            Component mode = Component.literal(modeLabel)
                    .withStyle(state.grinding() ? ChatFormatting.GOLD : ChatFormatting.AQUA);
            gui.drawString(mc.font, mode, x, y, 0xFFFFFF, true);
            int barWidth = Math.round(120.0f * Math.max(0.0f, Math.min(100.0f, state.boost())) / 100.0f);
            gui.fill(x, y + 12, x + 120, y + 19, 0xFF202020);
            gui.fill(x, y + 12, x + barWidth, y + 19, 0xFFE8F23A);
            gui.drawString(mc.font, Component.literal("BOOST " + Math.round(state.boost()) + "%"), x + 2, y + 22, 0xFFF5F5F5, false);
            if (state.comboScore() > 0) {
                String combo = state.comboScore() + "  x" + String.format(java.util.Locale.ROOT, "%.2f", state.comboMultiplier());
                gui.drawString(mc.font, combo, x + 74, y + 22, 0xFFFF78D7, true);
            }
        }
    }

    private ClientEvents() {}
}
