package com.herberto.jetsetcraft.liberty;

import com.herberto.jetsetcraft.compat.LibertyHelldiversCompat;
import com.herberto.jetsetcraft.compat.LibertyTaczCompat;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.registries.ForgeRegistries;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

/** Predeployment selections from live common TaCZ indexes; no renamed substitutes or client gun metadata. */
public final class LibertyArmory {
    public static final String KEY = "jetsetcraft:LibertyArmory";
    public static final int SCHEMA = 1, MAX_GUNS = 512;
    private static final Set<String> PACK_NAMESPACES = Set.of("helldiver2", "zeta");
    private static final ResourceLocation AMMO_ITEM = new ResourceLocation("tacz", "ammo");
    private static final List<EquipmentSlot> ARMOR_SLOTS = List.of(EquipmentSlot.HEAD, EquipmentSlot.CHEST, EquipmentSlot.LEGS);
    public record Selection(String primary, String secondary, String armor) { }
    public record GunChoice(String id, String nameKey, String type, String ammo, int capacity, String feed, boolean heat, String reason) {
        public boolean ready() { return reason.isEmpty(); }
        public String loadDescription() {
            return heat && capacity == Integer.MAX_VALUE ? "Heat based · allow cooling between shots" : capacity + " loaded";
        }
    }
    private record ArmorChoice(String id, String name, String description, List<Item> items, double walkSpeed, double knockback) { }
    private record Account(long revision, Selection selection) { }

    /** The pinned merged pack has twelve helldiver2 and fifty-six zeta gun definitions. Re-read on every reload-aware query. */
    public static List<GunChoice> guns() {
        if (ModList.get() == null || !ModList.get().isLoaded("tacz")) return List.of();
        try {
            Class<?> api = tacz("api.TimelessAPI"); Object raw = api.getMethod("getAllCommonGunIndex").invoke(null);
            if (!(raw instanceof Set<?> entries)) return List.of();
            List<GunChoice> guns = new ArrayList<>();
            for (Object value : entries) {
                if (!(value instanceof Map.Entry<?, ?> entry) || !(entry.getKey() instanceof ResourceLocation id)
                        || !PACK_NAMESPACES.contains(id.getNamespace())) continue;
                if (guns.size() >= MAX_GUNS) return List.of();
                Object index = entry.getValue(); String name = id.toString(), type = "pack weapon", ammo = "", feed = "";
                int capacity = 0; boolean heat = false; String reason = "";
                try {
                    Object pojo = index.getClass().getMethod("getPojo").invoke(index);
                    name = String.valueOf(pojo.getClass().getMethod("getName").invoke(pojo));
                    type = String.valueOf(index.getClass().getMethod("getType").invoke(index));
                    Object data = index.getClass().getMethod("getGunData").invoke(index);
                    ResourceLocation ammoId = (ResourceLocation) data.getClass().getMethod("getAmmoId").invoke(data);
                    ammo = ammoId.toString(); capacity = ((Number) data.getClass().getMethod("getAmmoAmount").invoke(data)).intValue();
                    heat = (Boolean) data.getClass().getMethod("hasHeatData").invoke(data);
                    Object reload = data.getClass().getMethod("getReloadData").invoke(data);
                    feed = ((Enum<?>) reload.getClass().getMethod("getType").invoke(reload)).name();
                    Object modes = data.getClass().getMethod("getFireModeSet").invoke(data);
                    boolean ammoPresent = api.getMethod("getCommonAmmoIndex", ResourceLocation.class).invoke(null, ammoId) instanceof Optional<?> optional && optional.isPresent();
                    if (!ammoPresent || capacity < 1 || !(modes instanceof List<?> list) || list.isEmpty())
                        reason = "This live gun definition cannot provide a valid full load and matching ammunition.";
                } catch (ReflectiveOperationException | RuntimeException error) { reason = "This live gun definition could not be read safely."; }
                guns.add(new GunChoice(id.toString(), bounded(name), bounded(type), ammo, capacity, feed, heat, reason));
            }
            guns.sort(Comparator.comparing(GunChoice::id)); return List.copyOf(guns);
        } catch (ReflectiveOperationException | RuntimeException | LinkageError unavailable) { return List.of(); }
    }

    public static Selection selection(ServerPlayer player) { Account account = read(player); return account == null ? null : account.selection; }
    public static boolean ready(ServerPlayer player) { return readyReason(player).isEmpty(); }
    public static String readyReason(ServerPlayer player) {
        Account account = read(player);
        if (account == null) return "Armory data needs recovery or a newer compatible version; the saved selection is retained.";
        List<GunChoice> guns = guns();
        for (String id : List.of(account.selection.primary, account.selection.secondary)) {
            GunChoice gun = gun(guns, id);
            if (gun == null) return "Selected weapon " + id + " is absent from the live Helldivers pack. Choose an installed weapon before deployment.";
            if (!gun.ready()) return gun.reason;
        }
        return armor(account.selection.armor) == null ? "The selected armor kit is not currently installed." : "";
    }
    public static CompoundTag snapshot(ServerPlayer player) { return snapshot(player, false); }
    public static CompoundTag snapshot(ServerPlayer player, boolean canManage) {
        Account account = read(player); CompoundTag out = new CompoundTag(); boolean blocked = account == null;
        out.putInt("Schema", SCHEMA); out.putLong("Revision", blocked ? -1 : account.revision);
        out.putBoolean("Blocked", blocked); out.putBoolean("CanManage", canManage && !blocked && account.revision < Long.MAX_VALUE);
        out.putString("Primary", blocked ? "" : account.selection.primary); out.putString("Secondary", blocked ? "" : account.selection.secondary);
        out.putString("Armor", blocked ? "" : account.selection.armor); String reason = readyReason(player);
        out.putBoolean("Ready", reason.isEmpty()); out.putString("Reason", reason);
        out.putString("WeaponRule", "Both weapon slots can choose any live Helldivers pack gun. Displayed type is the pack's actual classification.");
        ListTag guns = new ListTag();
        for (GunChoice gun : guns()) {
            CompoundTag row = new CompoundTag(); row.putString("Id", gun.id); row.putString("NameKey", gun.nameKey);
            row.putString("Name", friendly(gun.id.substring(gun.id.indexOf(':') + 1))); row.putString("Type", gun.type);
            row.putString("Ammo", gun.ammo); row.putInt("Capacity", gun.capacity); row.putString("Feed", gun.feed);
            row.putBoolean("Heat", gun.heat); row.putString("LoadDescription", gun.loadDescription());
            row.putBoolean("Ready", gun.ready()); row.putString("Reason", gun.reason); guns.add(row);
        }
        out.put("Guns", guns); ListTag armor = new ListTag();
        for (ArmorChoice kit : armors()) {
            CompoundTag row = new CompoundTag(); row.putString("Id", kit.id); row.putString("Name", kit.name);
            row.putString("Description", kit.description); row.putDouble("WalkSpeed", kit.walkSpeed); row.putDouble("SkateSpeed", kit.walkSpeed);
            int protection = 0; double toughness = 0;
            for (Item item : kit.items) if (item instanceof ArmorItem piece) { protection += piece.getDefense(); toughness += piece.getToughness(); }
            row.putInt("ArmorPoints", protection); row.putDouble("Toughness", toughness); row.putBoolean("Selected", !blocked && account.selection.armor.equals(kit.id)); armor.add(row);
        }
        out.put("Armors", armor); return out;
    }
    /** The manager verifies alive/in-ship context before forwarding a versioned selection. */
    public static boolean select(ServerPlayer player, String slot, String id, long expectedRevision) {
        Account account = read(player);
        if (account == null || player == null || !player.isAlive() || player.isSpectator()
                || account.revision != expectedRevision || account.revision == Long.MAX_VALUE || id == null) return false;
        Selection old = account.selection, next;
        if ("armor".equals(slot)) {
            if (armor(id) == null) return false;
            next = new Selection(old.primary, old.secondary, id);
        } else if ("primary".equals(slot) || "secondary".equals(slot)) {
            GunChoice gun = gun(guns(), id); if (gun == null || !gun.ready()) return false;
            next = "primary".equals(slot) ? new Selection(id, old.secondary, old.armor) : new Selection(old.primary, id, old.armor);
        } else return false;
        if (next.equals(old)) return true;
        write(player, new Account(account.revision + 1, next)); return true;
    }

    /** Builds and validates both real weapons, matching reserves and armor before touching empty loan slots. */
    public static boolean provision(ServerPlayer player) {
        Selection choice = selection(player);
        if (choice == null || player == null || !player.isAlive() || !ready(player)) return false;
        var primary = LibertyHelldiversCompat.supportWeapon(choice.primary, player.getUUID());
        var secondary = LibertyHelldiversCompat.supportWeapon(choice.secondary, player.getUUID());
        ArmorChoice kit = armor(choice.armor);
        if (!primary.ready() || !secondary.ready() || primary.reserve().isEmpty() || secondary.reserve().isEmpty() || kit == null) return false;
        Map<Integer, ItemStack> staged = new LinkedHashMap<>(); staged.put(0, primary.gun()); staged.put(1, secondary.gun());
        staged.put(9, primary.reserve().get(0)); staged.put(10, secondary.reserve().get(0));
        List<ItemStack> extra = new ArrayList<>(primary.reserve().subList(1, primary.reserve().size()));
        extra.addAll(secondary.reserve().subList(1, secondary.reserve().size()));
        int next = 11;
        for (ItemStack stack : extra) {
            while (next < 36 && !player.getInventory().getItem(next).isEmpty()) next++;
            if (next >= 36) return false; staged.put(next++, stack);
        }
        for (int slot : staged.keySet()) if (!player.getInventory().getItem(slot).isEmpty()) return false;
        for (EquipmentSlot slot : ARMOR_SLOTS) if (!player.getItemBySlot(slot).isEmpty()) return false;
        List<ItemStack> armor = armorStacks(kit, player.getUUID());
        if (armor.size() != ARMOR_SLOTS.size()) return false;
        for (var entry : staged.entrySet()) { LibertyTaczCompat.markLoan(entry.getValue(), player.getUUID()); player.getInventory().setItem(entry.getKey(), entry.getValue()); }
        for (int i = 0; i < ARMOR_SLOTS.size(); i++) player.setItemSlot(ARMOR_SLOTS.get(i), armor.get(i));
        player.getInventory().setChanged(); player.inventoryMenu.broadcastChanges(); return true;
    }

    /** Restore the selected ammunition budget using real ammo items; loaded guns and unrelated stacks are untouched. */
    public static boolean resupply(ServerPlayer player) {
        Selection choice = selection(player); if (choice == null || player == null || !player.isAlive() || !ready(player)) return false;
        Map<String, ItemStack> examples = new LinkedHashMap<>(); Map<String, Integer> desired = new LinkedHashMap<>();
        List<String> issued = new ArrayList<>(List.of(choice.primary, choice.secondary));
        String support = nativeSupportId(player.getInventory().getItem(3), player.getUUID());
        if (!support.isEmpty()) issued.add(support);
        for (String id : issued) {
            var loadout = LibertyHelldiversCompat.supportWeapon(id, player.getUUID()); if (!loadout.ready()) return false;
            for (ItemStack stack : loadout.reserve()) {
                String ammo = ammoId(stack); if (ammo.isEmpty()) return false;
                examples.putIfAbsent(ammo, stack); desired.merge(ammo, stack.getCount(), Integer::sum);
            }
        }
        List<ItemStack> staged = new ArrayList<>(); for (int i = 0; i < 36; i++) staged.add(player.getInventory().getItem(i).copy());
        for (var entry : desired.entrySet()) {
            int have = 0;
            for (ItemStack stack : staged) if (LibertyTaczCompat.isLoan(stack, player.getUUID()) && entry.getKey().equals(ammoId(stack)))
                have = (int) Math.min(entry.getValue(), (long) have + Math.max(0, stack.getCount()));
            int needed = entry.getValue() - have;
            for (int i = 0; needed > 0 && i < 36; i++) {
                ItemStack stack = staged.get(i);
                if (LibertyTaczCompat.isLoan(stack, player.getUUID()) && entry.getKey().equals(ammoId(stack))) {
                    int add = Math.min(needed, Math.max(0, stack.getMaxStackSize() - stack.getCount())); stack.grow(add); needed -= add;
                }
            }
            for (int i = 9; needed > 0 && i < 36; i++) if (staged.get(i).isEmpty()) {
                ItemStack stack = examples.get(entry.getKey()).copy(); int add = Math.min(needed, stack.getMaxStackSize());
                stack.setCount(add); staged.set(i, stack); needed -= add;
            }
            if (needed > 0) return false;
        }
        for (int i = 0; i < 36; i++) if (!ItemStack.matches(staged.get(i), player.getInventory().getItem(i))) player.getInventory().setItem(i, staged.get(i));
        player.getInventory().setChanged(); player.inventoryMenu.broadcastChanges(); return true;
    }
    private static String ammoId(ItemStack stack) {
        if (stack.isEmpty() || !AMMO_ITEM.equals(ForgeRegistries.ITEMS.getKey(stack.getItem()))) return "";
        try { Object id = tacz("api.item.IAmmo").getMethod("getAmmoId", ItemStack.class).invoke(stack.getItem(), stack); return id instanceof ResourceLocation resource ? resource.toString() : ""; }
        catch (ReflectiveOperationException | RuntimeException | LinkageError failure) { return ""; }
    }
    private static String nativeSupportId(ItemStack stack, UUID owner) {
        if (!LibertyTaczCompat.isLoan(stack, owner) || !stack.hasTag()) return "";
        String marked = stack.getTag().getString("jetsetcraft_liberty_native_support");
        if (!validGunId(marked)) return "";
        try {
            Class<?> gun = tacz("api.item.IGun");
            if (!gun.isInstance(stack.getItem())) return "";
            Object actual = gun.getMethod("getGunId", ItemStack.class).invoke(stack.getItem(), stack);
            return actual instanceof ResourceLocation id && id.toString().equals(marked) ? marked : "";
        } catch (ReflectiveOperationException | RuntimeException | LinkageError failure) { return ""; }
    }

    private static List<ArmorChoice> armors() {
        List<ArmorChoice> out = new ArrayList<>();
        out.add(new ArmorChoice("light", "Light field kit", "Leather head/chest/legs: 6 armor points and +10% walking and skating speed. Physical skates remain equipped.",
                List.of(Items.LEATHER_HELMET, Items.LEATHER_CHESTPLATE, Items.LEATHER_LEGGINGS), .1, 0));
        out.add(new ArmorChoice("medium", "Medium field kit", "Iron head/chest/legs: 13 armor points and normal walking and skating speed. Physical skates remain equipped.",
                List.of(Items.IRON_HELMET, Items.IRON_CHESTPLATE, Items.IRON_LEGGINGS), 0, 0));
        out.add(new ArmorChoice("heavy", "Heavy field kit", "Diamond head/chest/legs: 17 armor points, 6 toughness, +10% knockback resistance and -10% walking and skating speed. Physical skates remain equipped.",
                List.of(Items.DIAMOND_HELMET, Items.DIAMOND_CHESTPLATE, Items.DIAMOND_LEGGINGS), -.1, .1));
        Map<String, Map<EquipmentSlot, Item>> nativeKits = new LinkedHashMap<>();
        for (Item item : ForgeRegistries.ITEMS.getValues()) {
            ResourceLocation id = ForgeRegistries.ITEMS.getKey(item);
            if (!(item instanceof ArmorItem piece) || id == null || !(id.getNamespace().equals("mcdivers") || id.getNamespace().contains("helldiver"))
                    || !ARMOR_SLOTS.contains(piece.getEquipmentSlot())) continue;
            String stem = id.getPath().replaceFirst("_(helmet|chestplate|leggings)$", "");
            if (stem.equals(id.getPath())) continue;
            nativeKits.computeIfAbsent(id.getNamespace() + ":" + stem, ignored -> new LinkedHashMap<>()).put(piece.getEquipmentSlot(), item);
        }
        nativeKits.forEach((id, pieces) -> {
            if (out.size() < 35 && pieces.keySet().containsAll(ARMOR_SLOTS)) out.add(new ArmorChoice("native:" + id, "Native " + friendly(id.substring(id.indexOf(':') + 1)),
                    "Actual installed " + id + " armor, preserving its own equipment attributes. Physical skates remain equipped.", ARMOR_SLOTS.stream().map(pieces::get).toList(), 0, 0));
        });
        return List.copyOf(out);
    }
    private static ArmorChoice armor(String id) { return armors().stream().filter(kit -> kit.id.equals(id)).findFirst().orElse(null); }
    private static List<ItemStack> armorStacks(ArmorChoice kit, UUID owner) {
        List<ItemStack> out = new ArrayList<>();
        for (int i = 0; i < kit.items.size(); i++) {
            ItemStack stack = new ItemStack(kit.items.get(i)); EquipmentSlot slot = ARMOR_SLOTS.get(i);
            if (slot == EquipmentSlot.CHEST && (kit.walkSpeed != 0 || kit.knockback != 0)) {
                // An explicit modifier list replaces item defaults, so copy every original armor attribute first.
                List<Map.Entry<Attribute, AttributeModifier>> base = new ArrayList<>(stack.getAttributeModifiers(slot).entries());
                for (var modifier : base) stack.addAttributeModifier(modifier.getKey(), modifier.getValue(), slot);
                if (kit.walkSpeed != 0) stack.addAttributeModifier(Attributes.MOVEMENT_SPEED,
                        new AttributeModifier(UUID.nameUUIDFromBytes("jetsetcraft:liberty_armor_speed".getBytes(StandardCharsets.UTF_8)), "Liberty field kit movement speed", kit.walkSpeed, AttributeModifier.Operation.MULTIPLY_TOTAL), slot);
                if (kit.knockback != 0) stack.addAttributeModifier(Attributes.KNOCKBACK_RESISTANCE,
                        new AttributeModifier(UUID.nameUUIDFromBytes("jetsetcraft:liberty_armor_stability".getBytes(StandardCharsets.UTF_8)), "Liberty field kit stability", kit.knockback, AttributeModifier.Operation.ADDITION), slot);
            }
            LibertyTaczCompat.markLoan(stack, owner); out.add(stack);
        }
        return List.copyOf(out);
    }
    private static GunChoice gun(List<GunChoice> guns, String id) { return guns.stream().filter(gun -> gun.id.equals(id)).findFirst().orElse(null); }
    private static Account read(ServerPlayer player) {
        if (player == null) return null; CompoundTag persistent = player.getPersistentData();
        if (persistent.contains(Player.PERSISTED_NBT_TAG) && !persistent.contains(Player.PERSISTED_NBT_TAG, Tag.TAG_COMPOUND)) return null;
        CompoundTag data = persistent.getCompound(Player.PERSISTED_NBT_TAG);
        if (!data.contains(KEY)) return new Account(0, new Selection(LibertyTaczCompat.LIBERATOR.toString(), LibertyTaczCompat.PEACEMAKER.toString(), "medium"));
        if (!data.contains(KEY, Tag.TAG_COMPOUND)) return null;
        CompoundTag raw = data.getCompound(KEY);
        if (!raw.contains("Schema", Tag.TAG_INT) || raw.getInt("Schema") != SCHEMA || !raw.hasUUID("Owner")
                || !player.getUUID().equals(raw.getUUID("Owner")) || !raw.contains("Revision", Tag.TAG_LONG) || raw.getLong("Revision") < 0
                || !raw.contains("Primary", Tag.TAG_STRING) || !raw.contains("Secondary", Tag.TAG_STRING) || !raw.contains("Armor", Tag.TAG_STRING)
                || !Set.of("Schema", "Owner", "Revision", "Primary", "Secondary", "Armor").containsAll(raw.getAllKeys())
                || !validGunId(raw.getString("Primary")) || !validGunId(raw.getString("Secondary"))) return null;
        String armor = raw.getString("Armor");
        if (!Set.of("light", "medium", "heavy").contains(armor) && (!armor.startsWith("native:") || armor.length() > 160
                || ResourceLocation.tryParse(armor.substring(7)) == null)) return null;
        return new Account(raw.getLong("Revision"), new Selection(raw.getString("Primary"), raw.getString("Secondary"), armor));
    }
    private static boolean validGunId(String text) { ResourceLocation id = text.length() > 160 ? null : ResourceLocation.tryParse(text); return id != null && PACK_NAMESPACES.contains(id.getNamespace()); }
    private static void write(ServerPlayer player, Account account) {
        CompoundTag raw = new CompoundTag(); raw.putInt("Schema", SCHEMA); raw.putUUID("Owner", player.getUUID()); raw.putLong("Revision", account.revision);
        raw.putString("Primary", account.selection.primary); raw.putString("Secondary", account.selection.secondary); raw.putString("Armor", account.selection.armor);
        CompoundTag saved = player.getPersistentData().getCompound(Player.PERSISTED_NBT_TAG).copy(); saved.put(KEY, raw); player.getPersistentData().put(Player.PERSISTED_NBT_TAG, saved);
    }
    private static String bounded(String text) { return text == null ? "" : text.substring(0, Math.min(256, text.length())); }
    private static String friendly(String value) { return value.replace('_', ' '); }
    private static Class<?> tacz(String name) throws ClassNotFoundException { return Class.forName("com.tacz.guns." + name, false, LibertyArmory.class.getClassLoader()); }
    private LibertyArmory() { }
}
