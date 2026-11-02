package com.herberto.jetsetcraft.gametest;

import com.herberto.jetsetcraft.JetSetCraft;
import com.herberto.jetsetcraft.compat.LibertyTaczCompat;
import com.mojang.authlib.GameProfile;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestAssertException;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.nbt.ListTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.GameType;
import net.minecraftforge.common.util.FakePlayerFactory;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.gametest.GameTestHolder;
import net.minecraftforge.gametest.PrefixGameTestTemplate;
import net.minecraftforge.registries.ForgeRegistries;

import java.lang.reflect.Method;
import java.util.UUID;
import java.util.function.Supplier;

/** Optional provider proofs use the shipped TaCZ code and the pinned real pack, never fake gun items. */
@GameTestHolder(JetSetCraft.MOD_ID)
@PrefixGameTestTemplate(false)
public final class LibertyTaczGameTests {
    @GameTest(template = "hoverboard_empty", timeoutTicks = 100)
    public static void providerAndPackAdmissionMatchTheSelectedLane(GameTestHelper helper) {
        boolean present = ModList.get().isLoaded("tacz");
        boolean expected = Boolean.getBoolean("jetsetcraft.libertyTacz");
        boolean packExpected = Boolean.getBoolean("jetsetcraft.libertyPackExpected");
        check(present == expected, "TaCZ presence disagrees with the selected optional verification lane");
        if (present) {
            String version = ModList.get().getModContainerById("tacz").orElseThrow().getModInfo().getVersion().toString();
            check(version.equals("1.1.8-hotfix2"), "Expected TaCZ 1.1.8-hotfix2, found " + version);
        }
        check(LibertyTaczCompat.available() == packExpected,
                "Helldivers availability disagrees with the pack fixture: " + LibertyTaczCompat.status());
        if (!packExpected) {
            ServerPlayer player = player(helper);
            player.getInventory().setItem(2, new ItemStack(Items.DIAMOND, 7));
            ListTag before = player.getInventory().save(new ListTag());
            check(!LibertyTaczCompat.provision(player) && !LibertyTaczCompat.resupply(player)
                            && before.equals(player.getInventory().save(new ListTag()))
                            && !LibertyTaczCompat.status().isBlank(),
                    "Missing optional content issued substitute gear, touched inventory or omitted its diagnostic");
            player.getInventory().clearContent();
        }
        pass(helper, "liberty_tacz_admission provider=" + present + " pack=" + packExpected);
    }

    @GameTest(template = "hoverboard_empty", timeoutTicks = 100)
    public static void realLoadoutHasCorrectIdsChambersMagazinesAndLoanOwner(GameTestHelper helper) {
        if (!nativeReady(helper, "liberty_tacz_loadout")) return;
        ServerPlayer player = player(helper);
        player.getInventory().setItem(2, new ItemStack(Items.ORANGE_DYE, 3));
        try {
            check(LibertyTaczCompat.provision(player), "Real Helldivers loadout was not provisioned");
            verifyGun(player.getInventory().getItem(0), LibertyTaczCompat.LIBERATOR, 44, "AUTO");
            verifyGun(player.getInventory().getItem(1), LibertyTaczCompat.PEACEMAKER, 14, "SEMI");
            verifyAmmo(player.getInventory().getItem(9), LibertyTaczCompat.RIFLE_AMMO, 6, 20);
            verifyAmmo(player.getInventory().getItem(10), LibertyTaczCompat.PISTOL_AMMO, 6, 8);
            for (int slot : new int[]{0, 1, 9, 10}) {
                ItemStack stack = player.getInventory().getItem(slot);
                check(LibertyTaczCompat.isLoan(stack, player.getUUID())
                                && !LibertyTaczCompat.isLoan(stack, UUID.randomUUID()),
                        "A native loadout item lacks its exact loan owner");
            }
            check(player.getInventory().getItem(2).is(Items.ORANGE_DYE)
                            && player.getInventory().getItem(2).getCount() == 3,
                    "Provisioning overwrote the spray-can slot");
            ListTag first = player.getInventory().save(new ListTag());
            check(!LibertyTaczCompat.provision(player) && first.equals(player.getInventory().save(new ListTag())),
                    "Repeated provisioning issued another loadout or overwrote existing gear");
            pass(helper, "liberty_tacz_loadout");
        } catch (ReflectiveOperationException error) {
            throw apiError(error);
        } finally { player.getInventory().clearContent(); }
    }

    @GameTest(template = "hoverboard_empty", timeoutTicks = 100)
    public static void provisioningAndResupplyAreBoundedAndAtomic(GameTestHelper helper) {
        if (!nativeReady(helper, "liberty_tacz_resupply")) return;
        ServerPlayer player = player(helper);
        try {
            player.getInventory().setItem(10, new ItemStack(Items.DIAMOND, 7));
            ListTag occupied = player.getInventory().save(new ListTag());
            check(!LibertyTaczCompat.provision(player) && occupied.equals(player.getInventory().save(new ListTag())),
                    "A conflict in the last provision slot partially wrote weapons or removed normal items");
            player.getInventory().clearContent();
            check(LibertyTaczCompat.provision(player), "Could not provision the resupply fixture");
            // Spend two magazine items and move the remaining stack; the server counts the entire inventory.
            ItemStack moved = player.getInventory().getItem(9).copy();
            moved.setCount(4); moved.getOrCreateTag().putString("SessionExtension", "extra manager metadata");
            player.getInventory().setItem(9, ItemStack.EMPTY); player.getInventory().setItem(15, moved);
            player.getInventory().setItem(10, ItemStack.EMPTY);
            ItemStack primaryBefore = player.getInventory().getItem(0).copy();
            ItemStack secondaryBefore = player.getInventory().getItem(1).copy();
            check(LibertyTaczCompat.resupply(player)
                            && countAmmo(player, LibertyTaczCompat.RIFLE_AMMO) == 6
                            && countAmmo(player, LibertyTaczCompat.PISTOL_AMMO) == 6,
                    "Resupply did not top up the moved/partially spent reserve to six magazines");
            check(ItemStack.matches(primaryBefore, player.getInventory().getItem(0))
                            && ItemStack.matches(secondaryBefore, player.getInventory().getItem(1)),
                    "Resupply skipped reload input by modifying loaded rounds or replacing guns");
            ListTag full = player.getInventory().save(new ListTag());
            check(LibertyTaczCompat.resupply(player) && full.equals(player.getInventory().save(new ListTag())),
                    "Repeated resupply multiplied full reserves");
            for (int slot = 9; slot < 36; slot++) player.getInventory().setItem(slot, new ItemStack(Items.COBBLESTONE, 64));
            ListTag blocked = player.getInventory().save(new ListTag());
            check(!LibertyTaczCompat.resupply(player) && blocked.equals(player.getInventory().save(new ListTag())),
                    "A full inventory caused partial resupply, overflow drops or item replacement");
            pass(helper, "liberty_tacz_resupply");
        } catch (ReflectiveOperationException error) {
            throw apiError(error);
        } finally { player.getInventory().clearContent(); }
    }

    @GameTest(template = "hoverboard_empty", timeoutTicks = 100)
    public static void actualProviderFuelReloadConsumesOneMatchingMagazine(GameTestHelper helper) {
        if (!nativeReady(helper, "liberty_tacz_reload")) return;
        ServerPlayer player = player(helper);
        try {
            check(LibertyTaczCompat.provision(player), "Could not provision the native reload fixture");
            for (int slot : new int[]{0, 1}) {
                player.getInventory().selected = slot;
                ItemStack gun = player.getMainHandItem();
                ResourceLocation ammo = slot == 0 ? LibertyTaczCompat.RIFLE_AMMO : LibertyTaczCompat.PISTOL_AMMO;
                int expectedRounds = slot == 0 ? 44 : 14;
                Class<?> gunApi = type("api.item.IGun");
                gunApi.getMethod("setCurrentAmmoCount", ItemStack.class, int.class).invoke(gun.getItem(), gun, 0);
                gunApi.getMethod("setBulletInBarrel", ItemStack.class, boolean.class).invoke(gun.getItem(), gun, false);
                Class<?> operatorApi = type("api.entity.IGunOperator");
                Object operator = operatorApi.getMethod("fromLivingEntity", LivingEntity.class).invoke(null, player);
                operatorApi.getMethod("initialData").invoke(operator);
                operatorApi.getMethod("draw", Supplier.class).invoke(operator, (Supplier<ItemStack>) () -> gun);
                check((Boolean) operatorApi.getMethod("consumesAmmoOrNot").invoke(operator),
                        "The native reload fixture incorrectly uses creative/infinite ammunition");
                Class<?> holderType = type("entity.shooter.ShooterDataHolder");
                Object holder = operatorApi.getMethod("getDataHolder").invoke(operator);
                Class<?> gunItemType = type("api.item.gun.AbstractGunItem");
                check((Boolean) gunItemType.getMethod("canReload", LivingEntity.class, ItemStack.class)
                        .invoke(gun.getItem(), player, gun), "Issued magazines cannot reload the real TaCZ gun");
                check((Boolean) gunItemType.getMethod("startReload", holderType, ItemStack.class, LivingEntity.class)
                        .invoke(gun.getItem(), holder, gun, player), "TaCZ rejected its actual reload start");
                // Advance only this test player's public provider timer past its feeding stage. The
                // shipped tickReload method performs consumption and chambering; no ammo is patched in.
                Class<?> states = type("api.entity.ReloadState$StateType");
                Object emptyFeeding = states.getField("EMPTY_RELOAD_FEEDING").get(null);
                holderType.getField("reloadStateType").set(holder, emptyFeeding);
                holderType.getField("reloadTimestamp").setLong(holder, System.currentTimeMillis() - 10_000L);
                int before = countAmmo(player, ammo);
                Object outcome = gunItemType.getMethod("tickReload", holderType, ItemStack.class, LivingEntity.class)
                        .invoke(gun.getItem(), holder, gun, player);
                Object state = type("api.entity.ReloadState").getMethod("getStateType").invoke(outcome);
                holderType.getField("reloadStateType").set(holder, state);
                check(countAmmo(player, ammo) == before - 1
                                && ((Number) gunApi.getMethod("getCurrentAmmoCount", ItemStack.class)
                                .invoke(gun.getItem(), gun)).intValue() == expectedRounds
                                && (Boolean) gunApi.getMethod("hasBulletInBarrel", ItemStack.class).invoke(gun.getItem(), gun),
                        "Native fuel reload did not consume exactly one matching magazine and chamber the full load");
                operatorApi.getMethod("initialData").invoke(operator);
            }
            pass(helper, "liberty_tacz_reload");
        } catch (ReflectiveOperationException error) {
            throw apiError(error);
        } finally { player.getInventory().clearContent(); }
    }

    private static boolean nativeReady(GameTestHelper helper, String name) {
        boolean expected = Boolean.getBoolean("jetsetcraft.libertyPackExpected");
        boolean ready = LibertyTaczCompat.available();
        check(!expected || ready, "Selected native provider lane cannot skip its required pack: " + LibertyTaczCompat.status());
        if (!ready) {
            System.out.println("JETSETCRAFT_GAMETEST_SKIP " + name + " optional-provider-or-pack-absent");
            helper.succeed();
        }
        return ready;
    }

    private static void verifyGun(ItemStack stack, ResourceLocation id, int magazineRounds, String fireMode)
            throws ReflectiveOperationException {
        check(!stack.isEmpty() && new ResourceLocation("tacz", "modern_kinetic_gun")
                .equals(ForgeRegistries.ITEMS.getKey(stack.getItem())), "Loadout contains a substitute gun");
        Class<?> gun = type("api.item.IGun");
        check(gun.isInstance(stack.getItem())
                        && id.equals(gun.getMethod("getGunId", ItemStack.class).invoke(stack.getItem(), stack))
                        && ((Number) gun.getMethod("getCurrentAmmoCount", ItemStack.class).invoke(stack.getItem(), stack)).intValue() == magazineRounds
                        && (Boolean) gun.getMethod("hasBulletInBarrel", ItemStack.class).invoke(stack.getItem(), stack)
                        && ((Enum<?>) gun.getMethod("getFireMode", ItemStack.class).invoke(stack.getItem(), stack)).name().equals(fireMode),
                "Native gun ID, magazine/chamber or initial fire mode disagrees with the pinned loadout");
    }

    private static void verifyAmmo(ItemStack stack, ResourceLocation id, int count, int stackLimit)
            throws ReflectiveOperationException {
        check(!stack.isEmpty() && new ResourceLocation("tacz", "ammo").equals(ForgeRegistries.ITEMS.getKey(stack.getItem()))
                        && id.equals(type("api.item.IAmmo").getMethod("getAmmoId", ItemStack.class).invoke(stack.getItem(), stack))
                        && stack.getCount() == count && stack.getMaxStackSize() == stackLimit,
                "Native reserve magazines have the wrong item, ID, amount or stack limit");
    }

    private static int countAmmo(ServerPlayer player, ResourceLocation id) throws ReflectiveOperationException {
        Class<?> ammo = type("api.item.IAmmo");
        Method getId = ammo.getMethod("getAmmoId", ItemStack.class);
        int count = 0;
        for (int slot = 0; slot < player.getInventory().getContainerSize(); slot++) {
            ItemStack stack = player.getInventory().getItem(slot);
            if (!stack.isEmpty() && ammo.isInstance(stack.getItem()) && id.equals(getId.invoke(stack.getItem(), stack)))
                count += stack.getCount();
        }
        return count;
    }

    private static ServerPlayer player(GameTestHelper helper) {
        ServerPlayer player = FakePlayerFactory.get(helper.getLevel(), new GameProfile(UUID.randomUUID(), "JSC_liberty_tacz"));
        player.getInventory().clearContent(); player.setGameMode(GameType.SURVIVAL); player.setHealth(player.getMaxHealth());
        BlockPos pos = helper.absolutePos(new BlockPos(1, 2, 1));
        player.moveTo(pos.getX() + .5, pos.getY(), pos.getZ() + .5, 0, 0);
        return player;
    }

    private static Class<?> type(String name) throws ClassNotFoundException { return Class.forName("com.tacz.guns." + name); }
    private static GameTestAssertException apiError(ReflectiveOperationException error) {
        return new GameTestAssertException("Pinned TaCZ API proof failed: " + error + "; cause=" + error.getCause());
    }
    private static void check(boolean condition, String message) { if (!condition) throw new GameTestAssertException(message); }
    private static void pass(GameTestHelper helper, String name) {
        System.out.println("JETSETCRAFT_GAMETEST_PASS " + name); helper.succeed();
    }
    private LibertyTaczGameTests() {}
}
