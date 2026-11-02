package com.herberto.jetsetcraft.client.crew;

import com.herberto.jetsetcraft.JetSetCraft;
import com.herberto.jetsetcraft.client.rush.RivalRushClient;
import com.herberto.jetsetcraft.network.JetSetNetwork;
import com.herberto.jetsetcraft.registry.ModBlocks;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.ClientPlayerNetworkEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.UUID;

/** Client presentation cache. All home eligibility, member progression and rewards come from the server. */
public final class CrewHouseClient {
    private static CompoundTag state = new CompoundTag();

    public static void accept(CompoundTag incoming) {
        CompoundTag previous = state;
        state = incoming.copy();
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.player == null) return;
        if (minecraft.screen instanceof CrewHouseScreen screen) screen.onSnapshot(previous);
        else if (state.getBoolean("Open")) minecraft.setScreen(new CrewHouseScreen());
    }

    static CompoundTag state() { return state; }
    public static CompoundTag stateSnapshot() { return state.copy(); }
    public static boolean isScreenOpen() { return Minecraft.getInstance().screen instanceof CrewHouseScreen; }
    public static boolean isProgressScreenOpen() {
        return Minecraft.getInstance().screen instanceof CrewHouseScreen screen && screen.isView("PROGRESS");
    }
    public static boolean isStyleLibraryOpen() {
        return Minecraft.getInstance().screen instanceof CrewHouseScreen screen && screen.isView("STYLES");
    }
    static ListTag list(String key) { return state.getList(key, Tag.TAG_COMPOUND); }
    static UUID homeId() { return state.hasUUID("HomeId") ? state.getUUID("HomeId") : null; }

    static void request(String action, String value, int number, UUID target) {
        if (Minecraft.getInstance().getConnection() == null) return;
        long revision = action.equals("style") ? state.getCompound("StyleLibrary").getLong("Revision") : state.getLong("Revision");
        JetSetNetwork.sendCrewAction(action, value, number, homeId(), target, revision);
    }

    /** A read-only, loaded-client-world check for optional home music; never fetches or loads a chunk. */
    public static boolean inHomePlot() {
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.player == null || minecraft.level == null || minecraft.getConnection() == null
                || !state.getBoolean("Exists") || RivalRushClient.active()
                || !state.contains("HomeAnchor", Tag.TAG_LONG)
                || "jetsetcraft:rival_rush".equals(minecraft.level.dimension().location().toString())
                || !minecraft.level.dimension().location().toString().equals(state.getString("HomeDimension"))) return false;
        BlockPos anchor = BlockPos.of(state.getLong("HomeAnchor"));
        int radius = state.getInt("Radius");
        if (radius < 1 || radius > 24 || !minecraft.level.hasChunkAt(anchor)
                || !minecraft.level.getBlockState(anchor).is(ModBlocks.BOOMBOX.get())) return false;
        BlockPos player = minecraft.player.blockPosition();
        return Math.abs((long) player.getX() - anchor.getX()) <= radius
                && Math.abs((long) player.getZ() - anchor.getZ()) <= radius;
    }

    @Mod.EventBusSubscriber(modid = JetSetCraft.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE, value = Dist.CLIENT)
    public static final class Events {
        @SubscribeEvent
        public static void logout(ClientPlayerNetworkEvent.LoggingOut event) { state = new CompoundTag(); CrewRewardFeedback.reset(); }
    }

    private CrewHouseClient() {}
}
