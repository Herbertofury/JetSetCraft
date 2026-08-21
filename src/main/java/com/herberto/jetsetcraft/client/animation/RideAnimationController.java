package com.herberto.jetsetcraft.client.animation;

import com.herberto.jetsetcraft.JetSetCraft;
import com.herberto.jetsetcraft.client.state.ClientRideState;
import com.herberto.jetsetcraft.compat.CompatManager;
import com.herberto.jetsetcraft.movement.RideStyle;
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

public final class RideAnimationController {
    private static final ResourceLocation LAYER = new ResourceLocation(JetSetCraft.MOD_ID, "ride_lower_body");
    private static final Map<Integer, ResourceLocation> ACTIVE = new HashMap<>();

    @Mod.EventBusSubscriber(modid = JetSetCraft.MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
    public static final class ModBus {
        @SubscribeEvent
        public static void setup(FMLClientSetupEvent event) {
            PlayerAnimationFactory.ANIMATION_DATA_FACTORY.registerFactory(LAYER, 24, player -> new ModifierLayer<>());
        }
    }

    @Mod.EventBusSubscriber(modid = JetSetCraft.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE, value = Dist.CLIENT)
    public static final class ForgeBus {
        @SubscribeEvent
        public static void tick(TickEvent.ClientTickEvent event) {
            if (event.phase != TickEvent.Phase.END) return;
            Minecraft mc = Minecraft.getInstance();
            if (mc.level == null) { ACTIVE.clear(); return; }
            for (var player : mc.level.players()) if (player instanceof AbstractClientPlayer clientPlayer) update(clientPlayer);
        }
    }

    @SuppressWarnings("unchecked")
    private static void update(AbstractClientPlayer player) {
        ClientRideState.Snapshot state = ClientRideState.get(player.getId());
        ResourceLocation next = choose(state);
        ResourceLocation previous = ACTIVE.get(player.getId());
        if (java.util.Objects.equals(previous, next)) return;

        Object raw = PlayerAnimationAccess.getPlayerAssociatedData(player).get(LAYER);
        if (!(raw instanceof ModifierLayer<?> layerRaw)) return;
        ModifierLayer<IAnimation> layer = (ModifierLayer<IAnimation>) layerRaw;
        if (next == null) {
            layer.setAnimation(null);
            ACTIVE.remove(player.getId());
            return;
        }
        var anim = PlayerAnimationRegistry.getAnimation(next);
        if (anim != null) {
            layer.setAnimation(new KeyframeAnimationPlayer(anim));
            ACTIVE.put(player.getId(), next);
        }
    }

    private static ResourceLocation choose(ClientRideState.Snapshot s) {
        if (!s.active()) return null;
        // Upper-body weapon animation is intentionally not selected here. Our authored clips only animate body/legs,
        // leaving TacZ/Epic Fight/Better Combat/vanilla hand and arm layers free to compose above this layer.
        if (s.grinding() && s.trickTicks() > 0) return id("grind_trick_" + Math.floorMod(s.trickIndex(), 4));
        if (s.trickTicks() > 0) return id("trick_" + Math.floorMod(s.trickIndex(), 4));
        if (s.grinding()) return id("grind");
        if (s.wallRiding()) return id("wallride");
        if (s.powersliding()) return id("powerslide");
        if (s.manual()) return id("manual");
        if (s.style() == RideStyle.BMX) return id(s.boosting() ? "bmx_boost" : "bmx_ride");
        if (s.style() == RideStyle.BOARD) return id(s.boosting() ? "board_boost" : "board_ride");
        if (s.style() == RideStyle.INLINE) return id(s.boosting() ? "inline_boost" : "inline_ride");
        if (s.style() == RideStyle.QUAD) return id(s.boosting() ? "quad_boost" : "quad_ride");
        return null;
    }

    private static ResourceLocation id(String name) { return new ResourceLocation(JetSetCraft.MOD_ID, name); }
    private RideAnimationController() {}
}
