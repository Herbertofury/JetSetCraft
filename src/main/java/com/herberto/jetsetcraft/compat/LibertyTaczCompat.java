package com.herberto.jetsetcraft.compat;

import com.herberto.jetsetcraft.JetSetCraft;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.registries.ForgeRegistries;

import java.lang.reflect.Method;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Optional public-API bridge for the real Helldivers gun pack. No TaCZ types enter JetSetCraft's
 * classpath. The caller owns mission admission, inventory escrow, session checks and loan cleanup.
 * Verified against TaCZ 1.1.8-hotfix2 and the merged Liberty Day pack, CurseForge file 9341504.
 */
public final class LibertyTaczCompat {
    public static final String LOAN_TAG = "jetsetcraft_liberty_loan";
    public static final String LOAN_OWNER = "jetsetcraft_liberty_loan_owner";
    public static final ResourceLocation LIBERATOR = new ResourceLocation("helldiver2", "ar23_liberator");
    public static final ResourceLocation PEACEMAKER = new ResourceLocation("helldiver2", "p2_peacemaker");
    public static final ResourceLocation RIFLE_AMMO = new ResourceLocation("zeta", "ar_ammo");
    public static final ResourceLocation PISTOL_AMMO = new ResourceLocation("zeta", "p113_ammo");
    public static final int RESERVE_MAGAZINES = 6;
    private static final ResourceLocation GUN_ITEM = new ResourceLocation("tacz", "modern_kinetic_gun");
    private static final ResourceLocation AMMO_ITEM = new ResourceLocation("tacz", "ammo");
    private static final String INSTALL = "Install TaCZ 1.1.8-hotfix2 and the merged Helldivers Liberty Day pack (file 9341504).";
    private static volatile Api cachedApi;
    private static volatile boolean checkedApi;
    private static volatile boolean warned;

    private record Api(Method gunIndex, Method ammoIndex, Method gunData, Method ammoId,
                       Method capacity, Method fireModes, Method reloadData, Method feedType, Method bolt,
                       Method createGun, Method gunId, Method gunAmmo, Method gunBarrel, Method gunFireMode,
                       Method buildGun, Method createAmmo, Method ammoBuilderId, Method ammoCount, Method buildAmmo,
                       Method readAmmoId) {}
    private record Gun(ResourceLocation id, ResourceLocation ammo, int capacity, Object fireMode) {}
    private record State(Api api, Gun primary, Gun secondary, String problem) {
        boolean ready() { return problem == null; }
    }

    /** Live index checks, not a cached availability flag: /tacz reload can add or remove the pack. */
    public static boolean available() {
        return state().ready();
    }

    /** Suitable for the war-map admission message; missing optional content never prevents startup. */
    public static String status() {
        State state = state();
        return state.ready() ? "Ready: Helldivers Liberator, Peacemaker and reload magazines detected."
                : state.problem();
    }

    /**
     * Stage the whole loadout before touching inventory. Slots 0/1 hold guns, 9/10 hold six magazines
     * each; slot 2 remains available for the mission spray can. Existing items are never overwritten.
     * The pack uses FUEL reloads: one reserve ammo item fills one magazine, not one bullet.
     */
    public static boolean provision(ServerPlayer player) {
        if (player == null || !player.isAlive()) return false;
        State state = state();
        if (!state.ready()) return false;
        int[] slots = {0, 1, 9, 10};
        for (int slot : slots) if (!player.getInventory().getItem(slot).isEmpty()) return false;
        try {
            List<ItemStack> items = List.of(gun(state.api(), state.primary()), gun(state.api(), state.secondary()),
                    ammo(state.api(), state.primary().ammo()), ammo(state.api(), state.secondary().ammo()));
            // A provider/build failure leaves the player's inventory completely unchanged.
            if (items.stream().anyMatch(ItemStack::isEmpty)) return false;
            for (ItemStack stack : items) markLoan(stack, player.getUUID());
            for (int i = 0; i < slots.length; i++) player.getInventory().setItem(slots[i], items.get(i));
            player.getInventory().setChanged();
            player.inventoryMenu.broadcastChanges();
            return true;
        } catch (ReflectiveOperationException | RuntimeException | LinkageError error) {
            warn(error);
            return false;
        }
    }

    /**
     * A mission resupply tops owned reserve magazines up to six of each, preserving loaded rounds
     * and requiring normal TaCZ reload input. It does not replace weapons or overwrite unrelated items.
     * The manager must restrict this operation to the current mission and its resupply cooldown.
     */
    public static boolean resupply(ServerPlayer player) {
        if (player == null || !player.isAlive()) return false;
        State state = state();
        if (!state.ready()) return false;
        try {
            ItemStack rifle = ammo(state.api(), state.primary().ammo());
            ItemStack pistol = ammo(state.api(), state.secondary().ammo());
            if (rifle.isEmpty() || pistol.isEmpty()) return false;
            markLoan(rifle, player.getUUID());
            markLoan(pistol, player.getUUID());
            int rifleCount = ownedAmmoCount(state.api(), player, state.primary().ammo());
            int pistolCount = ownedAmmoCount(state.api(), player, state.secondary().ammo());
            rifle.setCount(Math.max(0, RESERVE_MAGAZINES - rifleCount));
            pistol.setCount(Math.max(0, RESERVE_MAGAZINES - pistolCount));
            int required = (rifle.isEmpty() ? 0 : 1) + (pistol.isEmpty() ? 0 : 1);
            int free = 0;
            for (int slot = 9; slot < 36; slot++) if (player.getInventory().getItem(slot).isEmpty()) free++;
            if (free < required) return false;
            for (ItemStack stack : List.of(rifle, pistol)) {
                if (stack.isEmpty()) continue;
                for (int slot = 9; slot < 36; slot++) {
                    if (player.getInventory().getItem(slot).isEmpty()) {
                        player.getInventory().setItem(slot, stack);
                        break;
                    }
                }
            }
            player.getInventory().setChanged();
            player.inventoryMenu.broadcastChanges();
            return true;
        } catch (ReflectiveOperationException | RuntimeException | LinkageError error) {
            warn(error);
            return false;
        }
    }

    public static boolean isLoan(ItemStack stack) {
        return stack != null && !stack.isEmpty() && stack.hasTag() && stack.getTag().getBoolean(LOAN_TAG);
    }

    public static boolean isLoan(ItemStack stack, UUID owner) {
        return owner != null && isLoan(stack) && stack.getTag().hasUUID(LOAN_OWNER)
                && owner.equals(stack.getTag().getUUID(LOAN_OWNER));
    }

    /** Also usable by the manager for its spray can and other temporary mission equipment. */
    public static void markLoan(ItemStack stack, UUID owner) {
        if (stack == null || stack.isEmpty() || owner == null) return;
        stack.getOrCreateTag().putBoolean(LOAN_TAG, true);
        stack.getOrCreateTag().putUUID(LOAN_OWNER, owner);
    }

    private static int ownedAmmoCount(Api api, ServerPlayer player, ResourceLocation ammoId)
            throws ReflectiveOperationException {
        int total = 0;
        for (int slot = 0; slot < player.getInventory().getContainerSize(); slot++) {
            ItemStack stack = player.getInventory().getItem(slot);
            if (isLoan(stack, player.getUUID()) && AMMO_ITEM.equals(ForgeRegistries.ITEMS.getKey(stack.getItem()))
                    && ammoId.equals(api.readAmmoId().invoke(stack.getItem(), stack)))
                total = Math.min(RESERVE_MAGAZINES, total + stack.getCount());
        }
        return total;
    }

    private static State state() {
        if (ModList.get() == null || !ModList.get().isLoaded("tacz")) return unavailable(INSTALL);
        if (!ForgeRegistries.ITEMS.containsKey(GUN_ITEM) || !ForgeRegistries.ITEMS.containsKey(AMMO_ITEM))
            return unavailable("TaCZ items are unavailable. " + INSTALL);
        Api api = api();
        if (api == null) return unavailable("TaCZ public API does not match the supported release. " + INSTALL);
        try {
            Gun primary = readGun(api, LIBERATOR, RIFLE_AMMO);
            Gun secondary = readGun(api, PEACEMAKER, PISTOL_AMMO);
            return new State(api, primary, secondary, null);
        } catch (Unavailable error) {
            return unavailable(error.getMessage());
        } catch (ReflectiveOperationException | RuntimeException | LinkageError error) {
            warn(error);
            return unavailable("Helldivers loadout could not be verified. Check the server log and reload the matched pack.");
        }
    }

    private static State unavailable(String message) { return new State(null, null, null, message); }

    private static Gun readGun(Api api, ResourceLocation id, ResourceLocation expectedAmmo)
            throws ReflectiveOperationException {
        Object index = optional(api.gunIndex().invoke(null, id));
        if (index == null) throw new Unavailable("Missing gun " + id + ". " + INSTALL + " Put its ZIP in the game tacz folder and reload.");
        Object data = api.gunData().invoke(index);
        Object ammo = api.ammoId().invoke(data);
        if (!expectedAmmo.equals(ammo) || optional(api.ammoIndex().invoke(null, expectedAmmo)) == null)
            throw new Unavailable("Missing or mismatched magazines for " + id + ". Remove the old standalone pack and install the merged Liberty Day ZIP.");
        int capacity = ((Number) api.capacity().invoke(data)).intValue();
        Object modes = api.fireModes().invoke(data);
        Object feed = api.feedType().invoke(api.reloadData().invoke(data));
        Object bolt = api.bolt().invoke(data);
        if (capacity < 1 || capacity > 128 || !(modes instanceof List<?> list) || list.isEmpty()
                || !(feed instanceof Enum<?> feedEnum) || !feedEnum.name().equals("FUEL")
                || !(bolt instanceof Enum<?> boltEnum) || !boltEnum.name().equals("CLOSED_BOLT"))
            throw new Unavailable("Unsupported Helldivers reload data for " + id + ". " + INSTALL);
        return new Gun(id, expectedAmmo, capacity, list.get(0));
    }

    private static Object optional(Object value) {
        if (!(value instanceof Optional<?> optional)) throw new IllegalStateException("Unexpected TaCZ index result");
        return optional.orElse(null);
    }

    private static ItemStack gun(Api api, Gun gun) throws ReflectiveOperationException {
        Object builder = api.createGun().invoke(null);
        api.gunId().invoke(builder, gun.id());
        // A closed-bolt full load contains one chambered round and the remaining rounds in the magazine.
        api.gunAmmo().invoke(builder, gun.capacity() - 1);
        api.gunBarrel().invoke(builder, true);
        api.gunFireMode().invoke(builder, gun.fireMode());
        Object result = api.buildGun().invoke(builder);
        return result instanceof ItemStack stack && GUN_ITEM.equals(ForgeRegistries.ITEMS.getKey(stack.getItem()))
                ? stack : ItemStack.EMPTY;
    }

    private static ItemStack ammo(Api api, ResourceLocation id) throws ReflectiveOperationException {
        Object builder = api.createAmmo().invoke(null);
        api.ammoBuilderId().invoke(builder, id);
        api.ammoCount().invoke(builder, RESERVE_MAGAZINES);
        Object result = api.buildAmmo().invoke(builder);
        return result instanceof ItemStack stack && !stack.isEmpty()
                && AMMO_ITEM.equals(ForgeRegistries.ITEMS.getKey(stack.getItem()))
                && stack.getCount() <= stack.getMaxStackSize() ? stack : ItemStack.EMPTY;
    }

    private static Api api() {
        if (checkedApi) return cachedApi;
        synchronized (LibertyTaczCompat.class) {
            if (!checkedApi) {
                try {
                    Class<?> timeless = type("api.TimelessAPI");
                    Class<?> index = type("resource.index.CommonGunIndex");
                    Class<?> data = type("resource.pojo.data.gun.GunData");
                    Class<?> reload = type("resource.pojo.data.gun.GunReloadData");
                    Class<?> guns = type("api.item.builder.GunItemBuilder");
                    Class<?> ammo = type("api.item.builder.AmmoItemBuilder");
                    cachedApi = new Api(timeless.getMethod("getCommonGunIndex", ResourceLocation.class),
                            timeless.getMethod("getCommonAmmoIndex", ResourceLocation.class), index.getMethod("getGunData"),
                            data.getMethod("getAmmoId"), data.getMethod("getAmmoAmount"), data.getMethod("getFireModeSet"),
                            data.getMethod("getReloadData"), reload.getMethod("getType"), data.getMethod("getBolt"),
                            guns.getMethod("create"), guns.getMethod("setId", ResourceLocation.class),
                            guns.getMethod("setAmmoCount", int.class), guns.getMethod("setAmmoInBarrel", boolean.class),
                            guns.getMethod("setFireMode", type("api.item.gun.FireMode")), guns.getMethod("build"),
                            ammo.getMethod("create"), ammo.getMethod("setId", ResourceLocation.class),
                            ammo.getMethod("setCount", int.class), ammo.getMethod("build"),
                            type("api.item.IAmmo").getMethod("getAmmoId", ItemStack.class));
                } catch (ReflectiveOperationException | RuntimeException | LinkageError error) {
                    warn(error);
                }
                checkedApi = true;
            }
        }
        return cachedApi;
    }

    private static Class<?> type(String suffix) throws ClassNotFoundException {
        return Class.forName("com.tacz.guns." + suffix, false, LibertyTaczCompat.class.getClassLoader());
    }

    private static void warn(Throwable error) {
        if (!warned) {
            warned = true;
            JetSetCraft.LOGGER.warn("Liberty Grind TaCZ loadout unavailable: optional provider API did not match", error);
        }
    }

    private static final class Unavailable extends RuntimeException {
        Unavailable(String message) { super(message); }
    }

    private LibertyTaczCompat() {}
}
