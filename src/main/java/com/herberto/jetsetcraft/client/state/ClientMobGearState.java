package com.herberto.jetsetcraft.client.state;

import com.herberto.jetsetcraft.item.RideGearItem;
import com.herberto.jetsetcraft.mob.MobRideRig;
import com.herberto.jetsetcraft.network.S2CMobGearPacket;
import net.minecraft.world.item.ItemStack;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/** Client-only cache populated exclusively by server-authoritative tracking packets. */
public final class ClientMobGearState {
    public record Snapshot(ItemStack stack, MobRideRig rig) {
        public boolean equipped() {
            return !stack.isEmpty() && stack.getItem() instanceof RideGearItem;
        }
    }

    private static final Snapshot EMPTY = new Snapshot(ItemStack.EMPTY, MobRideRig.GENERIC);
    private static final Map<Integer, Snapshot> BY_ENTITY = new ConcurrentHashMap<>();

    public static void accept(S2CMobGearPacket packet) {
        if (packet == null || packet.entityId() < 0 || packet.stack().isEmpty()
                || !(packet.stack().getItem() instanceof RideGearItem)) {
            if (packet != null) BY_ENTITY.remove(packet.entityId());
            return;
        }
        ItemStack physical = packet.stack().copy();
        physical.setCount(1);
        BY_ENTITY.put(packet.entityId(), new Snapshot(physical,
                packet.rig() == null ? MobRideRig.GENERIC : packet.rig()));
    }

    public static Snapshot get(int entityId) {
        return BY_ENTITY.getOrDefault(entityId, EMPTY);
    }

    public static void remove(int entityId) {
        BY_ENTITY.remove(entityId);
    }

    public static void reset() {
        BY_ENTITY.clear();
    }

    private ClientMobGearState() {}
}
