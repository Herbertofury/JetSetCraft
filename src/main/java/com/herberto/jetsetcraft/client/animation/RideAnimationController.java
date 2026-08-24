package com.herberto.jetsetcraft.client.animation;

import com.herberto.jetsetcraft.JetSetCraft;
import com.herberto.jetsetcraft.client.state.ClientRideState;
import com.herberto.jetsetcraft.compat.CompatManager;
import com.herberto.jetsetcraft.movement.DanceCatalog;
import com.herberto.jetsetcraft.movement.RideStyle;
import com.herberto.jetsetcraft.movement.TrickCatalog;
import dev.kosmx.playerAnim.api.layered.IAnimation;
import dev.kosmx.playerAnim.api.layered.KeyframeAnimationPlayer;
import dev.kosmx.playerAnim.api.layered.ModifierLayer;
import dev.kosmx.playerAnim.minecraftApi.PlayerAnimationAccess;
import dev.kosmx.playerAnim.minecraftApi.PlayerAnimationFactory;
import dev.kosmx.playerAnim.minecraftApi.PlayerAnimationRegistry;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public final class RideAnimationController {
    private static final ResourceLocation RIDE_LAYER = ResourceLocation.fromNamespaceAndPath(JetSetCraft.MOD_ID, "ride_lower_body");
    private static final ResourceLocation ACTION_LAYER = ResourceLocation.fromNamespaceAndPath(JetSetCraft.MOD_ID, "style_full_body");
    private static final Map<Integer, ResourceLocation> ACTIVE_RIDE = new HashMap<>();
    private static final Map<Integer, ResourceLocation> ACTIVE_ACTION = new HashMap<>();

    @Mod.EventBusSubscriber(modid = JetSetCraft.MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
    public static final class ModBus {
        @SubscribeEvent
        public static void setup(FMLClientSetupEvent event) {
            PlayerAnimationFactory.ANIMATION_DATA_FACTORY.registerFactory(RIDE_LAYER, 24, player -> new ModifierLayer<>());
            PlayerAnimationFactory.ANIMATION_DATA_FACTORY.registerFactory(ACTION_LAYER, 32, player -> new ModifierLayer<>());
        }
    }

    @Mod.EventBusSubscriber(modid = JetSetCraft.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE, value = Dist.CLIENT)
    public static final class ForgeBus {
        @SubscribeEvent
        public static void tick(TickEvent.ClientTickEvent event) {
            if (event.phase != TickEvent.Phase.END) return;
            Minecraft mc = Minecraft.getInstance();
            if (mc.level == null) {
                reset();
                return;
            }
            for (AbstractClientPlayer player : mc.level.players()) update(player);
        }
    }

    private static void update(AbstractClientPlayer player) {
        ClientRideState.Snapshot state = ClientRideState.get(player.getId());
        boolean weaponOverlay = CompatManager.hasWeaponOverlay(player);
        apply(player, RIDE_LAYER, ACTIVE_RIDE, chooseRide(state));
        apply(player, ACTION_LAYER, ACTIVE_ACTION, chooseAction(state, weaponOverlay));
    }

    @SuppressWarnings("unchecked")
    private static void apply(AbstractClientPlayer player, ResourceLocation layerId,
                              Map<Integer, ResourceLocation> active, ResourceLocation next) {
        ResourceLocation previous = active.get(player.getId());
        Object raw = PlayerAnimationAccess.getPlayerAssociatedData(player).get(layerId);
        if (!(raw instanceof ModifierLayer<?> layerRaw)) {
            active.remove(player.getId());
            return;
        }
        ModifierLayer<IAnimation> layer = (ModifierLayer<IAnimation>) layerRaw;
        if (Objects.equals(previous, next)) return;
        if (next == null) {
            layer.setAnimation(null);
            active.remove(player.getId());
            return;
        }
        var animation = PlayerAnimationRegistry.getAnimation(next);
        if (animation == null) {
            layer.setAnimation(null);
            active.remove(player.getId());
            return;
        }
        layer.setAnimation(new KeyframeAnimationPlayer(animation));
        active.put(player.getId(), next);
    }


    public static void remove(int entityId) {
        ACTIVE_RIDE.remove(entityId);
        ACTIVE_ACTION.remove(entityId);
    }

    public static void reset() {
        ACTIVE_RIDE.clear();
        ACTIVE_ACTION.clear();
    }

    private static ResourceLocation chooseRide(ClientRideState.Snapshot state) {
        if (!state.active() || state.dancing() || state.groundStunt()) return null;
        int trickAnimation = TrickCatalog.byId(state.trickIndex()).animationIndex();
        if (state.grinding() && state.trickTicks() > 0) return id("grind_trick_" + trickAnimation);
        if (state.trickTicks() > 0) return id("trick_" + trickAnimation);
        if (state.grinding()) return id("grind");
        if (state.wallRiding()) return id("wallride");
        if (state.powersliding()) return id("powerslide");
        if (state.manual()) return id("manual");
        if (state.style() == RideStyle.BMX) return id(state.boosting() ? "bmx_boost" : "bmx_ride");
        if (state.style() == RideStyle.HOVER) return id(state.boosting() ? "hover_boost" : "hover_ride");
        if (state.style() == RideStyle.SCOOTER) return id(state.boosting() ? "scooter_boost" : "scooter_ride");
        if (state.style() == RideStyle.BOARD) return id(state.boosting() ? "board_boost" : "board_ride");
        if (state.style() == RideStyle.INLINE) return id(state.boosting() ? "inline_boost" : "inline_ride");
        if (state.style() == RideStyle.QUAD) return id(state.boosting() ? "quad_boost" : "quad_ride");
        return null;
    }

    private static ResourceLocation chooseAction(ClientRideState.Snapshot state, boolean weaponOverlay) {
        if (weaponOverlay) return null;
        if (state.dancing()) {
            int animation = DanceCatalog.byId(state.danceMoveId()).animationIndex();
            return id("dance_" + animation);
        }
        if (state.groundStunt() && state.trickTicks() > 0) {
            int animation = TrickCatalog.byId(state.trickIndex()).animationIndex();
            return id("stunt_" + animation);
        }
        return null;
    }

    private static ResourceLocation id(String name) { return ResourceLocation.fromNamespaceAndPath(JetSetCraft.MOD_ID, name); }
    private RideAnimationController() {}
}
