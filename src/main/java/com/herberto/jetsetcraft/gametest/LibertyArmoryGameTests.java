package com.herberto.jetsetcraft.gametest;

import com.herberto.jetsetcraft.JetSetCraft;
import com.herberto.jetsetcraft.compat.LibertyHelldiversCompat;
import com.herberto.jetsetcraft.compat.LibertyTaczCompat;
import com.herberto.jetsetcraft.liberty.LibertyArmory;
import com.herberto.jetsetcraft.movement.VanillaWorldPhysics.MotionProfile;
import com.mojang.authlib.GameProfile;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestAssertException;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.GameType;
import net.minecraftforge.common.util.FakePlayerFactory;
import net.minecraftforge.gametest.GameTestHolder;
import net.minecraftforge.gametest.PrefixGameTestTemplate;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/** Live provider construction and inventory fixtures. These do not claim to simulate weapon combat or human pacing. */
@GameTestHolder(JetSetCraft.MOD_ID)
@PrefixGameTestTemplate(false)
public final class LibertyArmoryGameTests {
    @GameTest(template = "hoverboard_empty", timeoutTicks = 100)
    public static void armorySelectionIsVersionedAndRetainsMissingOrFutureDefinitions(GameTestHelper helper) {
        ServerPlayer player = owner(helper); CompoundTag original = player.getPersistentData().copy();
        check(LibertyArmory.selection(player).primary().equals(LibertyTaczCompat.LIBERATOR.toString())
                && LibertyArmory.snapshot(player, true).getList("Armors", Tag.TAG_COMPOUND).size() >= 3
                && original.equals(player.getPersistentData()), "Reading default armory choices wrote a new account or omitted actual armor kits");
        check(LibertyArmory.select(player, "armor", "light", 0), "The light field kit could not be selected");
        CompoundTag valid = saved(player), checkpoint = player.getPersistentData().copy();
        check(!LibertyArmory.select(player, "armor", "heavy", 0)
                && !LibertyArmory.select(player, "unknown", "light", 1)
                && !LibertyArmory.select(player, "primary", "minecraft:bow", 1)
                && !LibertyArmory.select(player, "primary", "zeta:not_a_real_pack_weapon", 1)
                && checkpoint.equals(player.getPersistentData()), "A stale, foreign or missing armory request changed the saved selection");
        check(LibertyArmory.select(player, "armor", "light", 1) && valid.equals(saved(player)), "An identical selection needlessly advanced the revision");
        CompoundTag missing = valid.copy(); missing.putString("Primary", "zeta:removed_by_pack_reload"); replace(player, missing);
        check(!LibertyArmory.snapshot(player, true).getBoolean("Blocked") && !LibertyArmory.ready(player)
                && LibertyArmory.selection(player).primary().equals("zeta:removed_by_pack_reload")
                && !LibertyArmory.provision(player) && missing.equals(saved(player)),
                "A missing live gun was silently substituted or its persistent choice was discarded");
        for (int variant = 0; variant < 6; variant++) {
            CompoundTag raw = valid.copy();
            switch (variant) {
                case 0 -> raw.putInt("Schema", 2);
                case 1 -> raw.putLong("Revision", -1);
                case 2 -> raw.putUUID("Owner", UUID.randomUUID());
                case 3 -> raw.putString("Armor", "future-kit-without-schema");
                case 4 -> raw.putString("FutureField", "retain me");
                default -> raw.putInt("Primary", 42);
            }
            replace(player, raw);
            check(LibertyArmory.snapshot(player, true).getBoolean("Blocked")
                    && !LibertyArmory.select(player, "armor", "medium", 1)
                    && !LibertyArmory.provision(player) && !LibertyArmory.resupply(player) && raw.equals(saved(player)),
                    "Malformed or future armory state was erased, changed or issued equipment");
        }
        replace(player, valid);
        check(LibertyArmory.selection(player).armor().equals("light") && LibertyArmory.snapshot(player).getLong("Revision") == 1,
                "A serialized armory checkpoint lost its exact choice or revision");
        pass(helper, "liberty_armory_schema_selection");
    }

    @GameTest(template = "hoverboard_empty", timeoutTicks = 100)
    public static void everyInstalledPackGunBuildsItsRealMagazineAndMatchingAmmunition(GameTestHelper helper) {
        if (!nativeReady(helper, "liberty_armory_all_pack_guns")) return;
        List<LibertyArmory.GunChoice> choices = LibertyArmory.guns(); UUID owner = UUID.randomUUID();
        check(choices.size() == 68 && choices.stream().map(LibertyArmory.GunChoice::id).distinct().count() == 68
                && choices.stream().filter(gun -> gun.id().startsWith("helldiver2:")).count() == 12
                && choices.stream().filter(gun -> gun.id().startsWith("zeta:")).count() == 56,
                "The pinned merged Helldivers pack did not expose all 12 + 56 genuine gun definitions: " + choices.size());
        try {
            Class<?> gunApi = type("api.item.IGun"), ammoApi = type("api.item.IAmmo"), indexApi = type("api.TimelessAPI");
            int heatDefinitions = 0, heatSentinels = 0;
            for (LibertyArmory.GunChoice choice : choices) {
                var built = LibertyHelldiversCompat.supportWeapon(choice.id(), owner);
                check(choice.ready() && LibertyHelldiversCompat.supportWeaponAvailable(choice.id()) && built.ready() && gunApi.isInstance(built.gun().getItem())
                        && choice.id().equals(gunApi.getMethod("getGunId", ItemStack.class).invoke(built.gun().getItem(), built.gun()).toString()),
                        "A catalogue choice could not build its real native gun: " + choice.id() + " " + built.problem());
                Object index = ((java.util.Optional<?>) indexApi.getMethod("getCommonGunIndex", ResourceLocation.class)
                        .invoke(null, new ResourceLocation(choice.id()))).orElseThrow();
                Object data = index.getClass().getMethod("getGunData").invoke(index);
                boolean nativeHeat = (Boolean) data.getClass().getMethod("hasHeatData").invoke(data);
                check(choice.heat() == nativeHeat
                        && (Boolean) gunApi.getMethod("hasHeatData", ItemStack.class).invoke(built.gun().getItem(), built.gun()) == nativeHeat
                        && ((Number) gunApi.getMethod("getHeatAmount", ItemStack.class).invoke(built.gun().getItem(), built.gun())).floatValue() == 0F
                        && !(Boolean) gunApi.getMethod("isOverheatLocked", ItemStack.class).invoke(built.gun().getItem(), built.gun()),
                        "Native initial heat state was missing, already hot, or invented for a non-heat gun: " + choice.id());
                if (nativeHeat) heatDefinitions++;
                if (choice.capacity() == Integer.MAX_VALUE) {
                    heatSentinels++;
                    check(nativeHeat && choice.feed().equals("FUEL") && choice.loadDescription().startsWith("Heat based")
                                    && !choice.loadDescription().contains(Integer.toString(Integer.MAX_VALUE)),
                            "A genuine native heat sentinel was changed or displayed as billions of physical rounds: " + choice.id());
                }
                int loaded = ((Number) gunApi.getMethod("getCurrentAmmoCount", ItemStack.class).invoke(built.gun().getItem(), built.gun())).intValue();
                boolean chamber = (Boolean) gunApi.getMethod("hasBulletInBarrel", ItemStack.class).invoke(built.gun().getItem(), built.gun());
                check((long) loaded + (chamber ? 1L : 0L) == choice.capacity() && LibertyTaczCompat.isLoan(built.gun(), owner),
                        "Native initial magazine/chamber count or loan identity disagreed with its live gun data: " + choice.id());
                int reserve = 0;
                for (ItemStack stack : built.reserve()) {
                    check(ammoApi.isInstance(stack.getItem()) && choice.ammo().equals(ammoId(stack)),
                            "Native reserve has the wrong provider item or ammo ID: " + choice.id());
                    // TaCZ's AmmoItemDataAccessor checks argument1 as IGun and argument2 as IAmmo.
                    check((Boolean) ammoApi.getMethod("isAmmoOfGun", ItemStack.class, ItemStack.class).invoke(stack.getItem(), built.gun(), stack),
                            "The actual native reload compatibility method rejected this gun/ammo pair: " + choice.id());
                    check(stack.getCount() > 0 && stack.getCount() <= stack.getMaxStackSize()
                            && LibertyTaczCompat.isLoan(stack, owner) && !LibertyTaczCompat.isLoan(stack, UUID.randomUUID()),
                            "Native reserve escaped its actual stack bound or exact loan owner: " + choice.id());
                    reserve += stack.getCount();
                }
                int expected = choice.feed().equals("FUEL") ? 4 : (int) Math.min(256L, (long) choice.capacity() * 3L);
                check(reserve == expected, "Native reserve budget disagreed with actual magazine/round feeding: " + choice.id());
            }
            check(heatDefinitions == 19 && heatSentinels == 7,
                    "The pinned pack's native heat definitions/sentinels changed: " + heatDefinitions + "/" + heatSentinels);
        } catch (ReflectiveOperationException error) { throw apiFailure(error); }
        pass(helper, "liberty_armory_all_pack_guns count=" + choices.size());
    }

    @GameTest(template = "hoverboard_empty", timeoutTicks = 100)
    public static void chosenGunsAndCollectedSupportResupplyAtomicallyWithoutReloading(GameTestHelper helper) {
        if (!nativeReady(helper, "liberty_armory_resupply")) return;
        ServerPlayer player = owner(helper); List<LibertyArmory.GunChoice> choices = LibertyArmory.guns();
        LibertyArmory.GunChoice primary = choices.stream().filter(gun -> gun.id().startsWith("zeta:") && !gun.feed().equals("FUEL")).findFirst().orElseThrow();
        LibertyArmory.GunChoice secondary = choices.stream().filter(gun -> gun.id().equals(LibertyTaczCompat.PEACEMAKER.toString())).findFirst().orElseThrow();
        LibertyArmory.GunChoice support = choices.stream().filter(gun -> gun.id().equals("zeta:las12")).findFirst().orElseThrow();
        check(LibertyArmory.select(player, "primary", primary.id(), 0) && LibertyArmory.select(player, "secondary", secondary.id(), 1),
                "Either weapon slot rejected a legitimate live pack choice");
        player.getInventory().setItem(10, new ItemStack(Items.DIAMOND, 7)); ListTag occupied = inventory(player);
        check(!LibertyArmory.provision(player) && occupied.equals(inventory(player)), "Provisioning partially wrote gear before detecting an occupied final reserve slot");
        player.getInventory().clearContent(); player.getInventory().setItem(2, new ItemStack(Items.ORANGE_DYE, 3));
        check(LibertyArmory.provision(player) && player.getInventory().getItem(2).is(Items.ORANGE_DYE), "Native chosen loadout failed or overwrote the paint slot");
        try {
            Class<?> gunApi = type("api.item.IGun");
            check(primary.id().equals(gunApi.getMethod("getGunId", ItemStack.class).invoke(player.getInventory().getItem(0).getItem(), player.getInventory().getItem(0)).toString())
                    && secondary.id().equals(gunApi.getMethod("getGunId", ItemStack.class).invoke(player.getInventory().getItem(1).getItem(), player.getInventory().getItem(1)).toString()),
                    "Provisioning ignored the player's saved native gun IDs");
            player.getInventory().setItem(3, LibertyHelldiversCompat.supportWeapon(support.id(), player.getUUID()).gun());
            for (int slot = 9; slot < 36; slot++) player.getInventory().setItem(slot, ItemStack.EMPTY);
            ItemStack foreign = LibertyHelldiversCompat.supportWeapon(primary.id(), UUID.randomUUID()).reserve().get(0).copy();
            player.getInventory().setItem(14, foreign.copy()); Map<String, Integer> expected = new HashMap<>();
            for (String id : List.of(primary.id(), secondary.id(), support.id())) {
                var loadout = LibertyHelldiversCompat.supportWeapon(id, player.getUUID());
                for (ItemStack ammo : loadout.reserve()) expected.merge(ammoId(ammo), ammo.getCount(), Integer::sum);
            }
            for (int slot : new int[]{0, 1, 3}) {
                ItemStack gun = player.getInventory().getItem(slot);
                gunApi.getMethod("setCurrentAmmoCount", ItemStack.class, int.class).invoke(gun.getItem(), gun, 1);
            }
            ItemStack heatGun = player.getInventory().getItem(3);
            gunApi.getMethod("setHeatAmount", ItemStack.class, float.class).invoke(heatGun.getItem(), heatGun, 17.25F);
            gunApi.getMethod("setOverheatLocked", ItemStack.class, boolean.class).invoke(heatGun.getItem(), heatGun, true);
            List<ItemStack> before = List.of(player.getInventory().getItem(0).copy(), player.getInventory().getItem(1).copy(), player.getInventory().getItem(3).copy());
            check(LibertyArmory.resupply(player), "Selected and collected support ammunition could not be resupplied");
            for (var entry : expected.entrySet()) check(countAmmo(player, entry.getKey()) == entry.getValue(),
                    "Native resupply omitted or duplicated selected/support ammunition " + entry.getKey());
            check(ItemStack.matches(foreign, player.getInventory().getItem(14)), "Resupply consumed or changed a foreign owner's loan ammunition");
            for (int i = 0; i < 3; i++) check(ItemStack.matches(before.get(i), player.getInventory().getItem(new int[]{0, 1, 3}[i])),
                    "Resupply bypassed native reload/cooling by changing a gun's ammunition or heat state");
            ListTag full = inventory(player);
            check(LibertyArmory.resupply(player) && full.equals(inventory(player)), "Repeated resupply multiplied full reserves");
            for (int slot = 9; slot < 36; slot++) player.getInventory().setItem(slot, new ItemStack(Items.COBBLESTONE, 64));
            ListTag blocked = inventory(player);
            check(!LibertyArmory.resupply(player) && blocked.equals(inventory(player)), "A full inventory caused partial ammo grants or replaced player items");
        } catch (ReflectiveOperationException error) { throw apiFailure(error); }
        finally { player.getInventory().clearContent(); }
        pass(helper, "liberty_armory_resupply");
    }

    @GameTest(template = "hoverboard_empty", timeoutTicks = 100)
    public static void fieldArmorKeepsRealDefenseAndItsStatedMovementTradeoffs(GameTestHelper helper) {
        if (!nativeReady(helper, "liberty_armory_armor")) return;
        int index = 0;
        for (String armor : List.of("light", "medium", "heavy")) {
            ServerPlayer player = owner(helper); player.setItemSlot(EquipmentSlot.FEET, new ItemStack(Items.LEATHER_BOOTS));
            check(LibertyArmory.select(player, "armor", armor, 0), "Could not select armor kit " + armor);
            player.setItemSlot(EquipmentSlot.CHEST, new ItemStack(Items.GOLDEN_CHESTPLATE)); ListTag occupied = inventory(player);
            check(!LibertyArmory.provision(player) && occupied.equals(inventory(player)), "An occupied armor slot allowed partial weapon provisioning");
            player.setItemSlot(EquipmentSlot.CHEST, ItemStack.EMPTY);
            check(LibertyArmory.provision(player), "Could not issue actual field kit " + armor);
            double defense = 0, toughness = 0;
            for (EquipmentSlot slot : List.of(EquipmentSlot.HEAD, EquipmentSlot.CHEST, EquipmentSlot.LEGS)) {
                ItemStack piece = player.getItemBySlot(slot);
                check(LibertyTaczCompat.isLoan(piece, player.getUUID()), "A field armor piece escaped the owner's mission loan boundary");
                defense += piece.getAttributeModifiers(slot).get(Attributes.ARMOR).stream().mapToDouble(AttributeModifier::getAmount).sum();
                toughness += piece.getAttributeModifiers(slot).get(Attributes.ARMOR_TOUGHNESS).stream().mapToDouble(AttributeModifier::getAmount).sum();
            }
            ItemStack chest = player.getItemBySlot(EquipmentSlot.CHEST);
            double speed = chest.getAttributeModifiers(EquipmentSlot.CHEST).get(Attributes.MOVEMENT_SPEED).stream().mapToDouble(AttributeModifier::getAmount).sum();
            double stability = chest.getAttributeModifiers(EquipmentSlot.CHEST).get(Attributes.KNOCKBACK_RESISTANCE).stream().mapToDouble(AttributeModifier::getAmount).sum();
            check(defense == new int[]{6, 13, 17}[index] && toughness == (armor.equals("heavy") ? 6 : 0)
                    && speed == new double[]{.1, 0, -.1}[index] && stability == (armor.equals("heavy") ? .1 : 0)
                    && chest.getAttributeModifiers(EquipmentSlot.CHEST).get(Attributes.MOVEMENT_SPEED).stream().allMatch(modifier -> modifier.getOperation() == AttributeModifier.Operation.MULTIPLY_TOTAL)
                    && player.getItemBySlot(EquipmentSlot.FEET).is(Items.LEATHER_BOOTS),
                    "Field kit " + armor + " lost vanilla defense, misstated its actual movement modifiers, or replaced the feet slot");
            try {
                var apply = Class.forName("com.herberto.jetsetcraft.movement.VanillaSurfacePhysics")
                        .getDeclaredMethod("applyStatusEffects", ServerPlayer.class, MotionProfile.class);
                apply.setAccessible(true);
                MotionProfile base = new MotionProfile(.8, 1.3, 1.6, .998, .9, .002, .8);
                MotionProfile actual = (MotionProfile) apply.invoke(null, player, base);
                double factor = 1 + speed;
                check(Math.abs(actual.accelerationMultiplier() - base.accelerationMultiplier() * factor) < 1e-9
                        && Math.abs(actual.cruiseCapMultiplier() - base.cruiseCapMultiplier() * factor) < 1e-9
                        && Math.abs(actual.boostCapMultiplier() - base.boostCapMultiplier() * factor) < 1e-9
                        && actual.coastingRetention() == base.coastingRetention() && actual.steeringMultiplier() == base.steeringMultiplier(),
                        "Field kit " + armor + " did not affect real skating acceleration/caps, or changed preserved coasting momentum");
                ItemStack foreignChest = chest.copy(); LibertyTaczCompat.markLoan(foreignChest, UUID.randomUUID());
                player.setItemSlot(EquipmentSlot.CHEST, foreignChest);
                check(base.equals(apply.invoke(null, player, base)), "Another owner's armor loan granted a skating modifier");
            } catch (ReflectiveOperationException error) { throw apiFailure(error); }
            player.getInventory().clearContent(); index++;
        }
        pass(helper, "liberty_armory_armor");
    }

    private static ServerPlayer owner(GameTestHelper helper) {
        ServerPlayer player = FakePlayerFactory.get(helper.getLevel(), new GameProfile(UUID.randomUUID(), "JSC_armory"));
        player.setHealth(player.getMaxHealth()); player.setGameMode(GameType.SURVIVAL); player.getInventory().clearContent(); return player;
    }
    private static boolean nativeReady(GameTestHelper helper, String name) {
        boolean ready = LibertyTaczCompat.available();
        check(!Boolean.getBoolean("jetsetcraft.libertyPackExpected") || ready, "Required native armory lane cannot skip its Helldivers pack: " + LibertyTaczCompat.status());
        if (!ready) { System.out.println("JETSETCRAFT_GAMETEST_SKIP " + name + " optional-provider-or-pack-absent"); helper.succeed(); }
        return ready;
    }
    private static String ammoId(ItemStack stack) throws ReflectiveOperationException {
        Class<?> ammo = type("api.item.IAmmo"); return ammo.isInstance(stack.getItem()) ? ammo.getMethod("getAmmoId", ItemStack.class).invoke(stack.getItem(), stack).toString() : "";
    }
    private static int countAmmo(ServerPlayer player, String id) throws ReflectiveOperationException {
        int count = 0; for (int slot = 0; slot < 36; slot++) {
            ItemStack stack = player.getInventory().getItem(slot);
            if (!stack.isEmpty() && LibertyTaczCompat.isLoan(stack, player.getUUID()) && id.equals(ammoId(stack))) count += stack.getCount();
        }
        return count;
    }
    private static ListTag inventory(ServerPlayer player) { return player.getInventory().save(new ListTag()); }
    private static CompoundTag saved(ServerPlayer player) { return player.getPersistentData().getCompound(Player.PERSISTED_NBT_TAG).getCompound(LibertyArmory.KEY).copy(); }
    private static void replace(ServerPlayer player, CompoundTag raw) {
        CompoundTag saved = player.getPersistentData().getCompound(Player.PERSISTED_NBT_TAG).copy(); saved.put(LibertyArmory.KEY, raw.copy());
        player.getPersistentData().put(Player.PERSISTED_NBT_TAG, saved);
    }
    private static Class<?> type(String name) throws ClassNotFoundException { return Class.forName("com.tacz.guns." + name, false, LibertyArmoryGameTests.class.getClassLoader()); }
    private static GameTestAssertException apiFailure(ReflectiveOperationException error) { return new GameTestAssertException("Pinned native armory API failed: " + error); }
    private static void check(boolean ok, String message) { if (!ok) throw new GameTestAssertException(message); }
    private static void pass(GameTestHelper helper, String name) { System.out.println("JETSETCRAFT_LIBERTY_ARMORY_PASS " + name); helper.succeed(); }
    private LibertyArmoryGameTests() { }
}
