package com.herberto.jetsetcraft.gametest;

import com.herberto.jetsetcraft.JetSetCraft;
import com.herberto.jetsetcraft.liberty.LibertyArmory;
import com.herberto.jetsetcraft.liberty.LibertyManager;
import com.herberto.jetsetcraft.liberty.LibertyShipProgress;
import com.herberto.jetsetcraft.liberty.LibertyStratagemCatalog;
import com.herberto.jetsetcraft.network.C2SLibertyActionPacket;
import com.mojang.authlib.GameProfile;
import io.netty.buffer.Unpooled;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestAssertException;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.common.util.FakePlayerFactory;
import net.minecraftforge.gametest.GameTestHolder;
import net.minecraftforge.gametest.PrefixGameTestTemplate;

import java.util.UUID;

/** Real codec and production dispatch regression for the pre-pod choices exposed by the native UI. */
@GameTestHolder(JetSetCraft.MOD_ID)
@PrefixGameTestTemplate(false)
public final class LibertyPacketGameTests {
    @GameTest(template = "hoverboard_empty", timeoutTicks = 80)
    public static void prePodPacketsActuallyChangeVersionedArmoryAndPurchasedFourSlotLoadouts(GameTestHelper helper) {
        ServerPlayer player = FakePlayerFactory.get(helper.getLevel(), new GameProfile(UUID.randomUUID(), "JSC_loadoutpacket"));
        player.setHealth(player.getMaxHealth());
        // A genuine once-only settlement funds this isolated packet fixture; no wallet NBT is injected.
        check(LibertyShipProgress.settle(player, UUID.randomUUID(), 6, 10, true), "Could not fund the isolated ship packet fixture");
        int[] tick = {0}; boolean[] finished = {false};
        helper.onEachTick(() -> {
            if (finished[0]) return;
            try {
                switch (++tick[0]) {
                    case 1 -> {
                        deliver(player, "armory_select", "armor|heavy", 0);
                        check(LibertyArmory.selection(player).armor().equals("heavy")
                                        && LibertyArmory.snapshot(player).getLong("Revision") == 1,
                                "The actual armory_select packet was discarded before the pre-pod choice reached the server");
                    }
                    case 4 -> {
                        CompoundTag before = player.getPersistentData().copy();
                        deliver(player, "armory_select", "armor|light", 0);
                        deliver(player, "invent_credits", "999999", 1);
                        C2SLibertyActionPacket.handleForPlayer(new C2SLibertyActionPacket("armory_select", "x".repeat(129), null, 1), player);
                        C2SLibertyActionPacket.handleForPlayer(new C2SLibertyActionPacket("armory_select", "armor|light", null, -1), player);
                        check(before.equals(player.getPersistentData()), "Stale, oversized, negative-revision or unknown packet changed saved accounts");
                    }
                    case 7 -> {
                        int before = ship(player).getInt("Requisitions");
                        deliver(player, "ship_purchase", "chromageddon", ship(player).getLong("Revision"));
                        check(LibertyShipProgress.unlocked(player, "chromageddon")
                                        && ship(player).getInt("Requisitions") == before - LibertyStratagemCatalog.find("chromageddon").unlockCost(),
                                "The actual ship_purchase packet failed to unlock and charge the selected call-in exactly once");
                    }
                    case 10 -> {
                        deliver(player, "ship_equip", "chromageddon|3", ship(player).getLong("Revision"));
                        check(LibertyShipProgress.equipped(player).size() == 4
                                        && LibertyShipProgress.equipped(player).get(3).equals("chromageddon"),
                                "The actual ship_equip packet did not update the fourth pre-launch stratagem slot");
                    }
                    case 13 -> {
                        CompoundTag before = player.getPersistentData().copy();
                        deliver(player, "ship_purchase", "chromageddon", ship(player).getLong("Revision"));
                        check(before.equals(player.getPersistentData()), "Replaying an already purchased call-in spent the wallet again");
                    }
                    case 16 -> {
                        finished[0] = true; LibertyManager.forget(player);
                        JetSetCraft.LOGGER.info("JETSETCRAFT_LIBERTY_PACKET_PASS liberty_loadout_native_codec_actions");
                        helper.succeed();
                    }
                    default -> { }
                }
            } catch (RuntimeException | Error error) { finished[0] = true; LibertyManager.forget(player); throw error; }
        });
    }
    private static CompoundTag ship(ServerPlayer player) { return LibertyShipProgress.snapshot(player, true); }
    private static void deliver(ServerPlayer player, String action, String value, long revision) {
        FriendlyByteBuf bytes = new FriendlyByteBuf(Unpooled.buffer());
        try {
            C2SLibertyActionPacket expected = new C2SLibertyActionPacket(action, value, null, revision);
            C2SLibertyActionPacket.encode(expected, bytes);
            C2SLibertyActionPacket decoded = C2SLibertyActionPacket.decode(bytes);
            check(expected.equals(decoded) && !bytes.isReadable(), "The real pre-pod action codec lost fields or left unread bytes");
            C2SLibertyActionPacket.handleForPlayer(decoded, player);
        } finally { bytes.release(); }
    }
    private static void check(boolean condition, String message) { if (!condition) throw new GameTestAssertException(message); }
    private LibertyPacketGameTests() { }
}
