package com.herberto.jetsetcraft.liberty;

import com.herberto.jetsetcraft.crew.CrewHouseData;
import com.herberto.jetsetcraft.crew.CrewProfile;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.nbt.Tag;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.regex.Pattern;

/** Independent ship hardware account. Research samples/credits, crew balances and Rush preparation are never moved. */
public final class LibertyShipProgress {
    public static final String KEY = "jetsetcraft:LibertyShipProgress";
    public static final int SCHEMA = 1, WALLET_LIMIT = 1_000_000, MAX_RECEIPTS = 4_096, LOADOUT_SLOTS = 4;
    public static final String NONE = "NONE";
    private static final Pattern HASH = Pattern.compile("[0-9a-f]{64}");

    public enum Module {
        PAINT_WORKSHOP("paint_workshop", "Paint & Powder Workshop", 120, "pressure_cap",
                "Begin each deployment with one additional shared resupply charge. Requires Pressure Saver research."),
        TARGETING_ARRAY("targeting_array", "Graffiti Targeting Array", 180, "signal_lacquer",
                "Reduce call-in cooldowns by 10%. Requires Signal Lacquer research."),
        EAGLE_HANGAR("eagle_hangar", "Skater Eagle Hangar", 220, "supply_skates",
                "Each equipped Eagle call-in has one additional charge per rearm. Requires Supply Skates research."),
        SAMPLE_LAB("sample_lab", "Crew Sample Lab", 260, "",
                "Successful extraction adds one common research sample per completed primary objective. Requires a founded crew with 100 crew XP.");
        public final String id, name, research, effect;
        public final int cost;
        Module(String id, String name, int cost, String research, String effect) {
            this.id = id; this.name = name; this.cost = cost; this.research = research; this.effect = effect;
        }
        public static Module byId(String id) { for (Module module : values()) if (module.id.equals(id)) return module; return null; }
    }
    private static final class Account {
        final UUID owner;
        long revision;
        int requisitions;
        final Set<String> callins = new LinkedHashSet<>(LibertyStratagemCatalog.starters());
        final EnumSet<Module> modules = EnumSet.noneOf(Module.class);
        final List<String> equipped = new ArrayList<>(LibertyStratagemCatalog.starters());
        final Map<UUID, String> receipts = new LinkedHashMap<>();
        Account(UUID owner) { this.owner = owner; }
        CompoundTag save() {
            CompoundTag out = new CompoundTag(); out.putInt("Schema", SCHEMA); out.putUUID("Owner", owner);
            out.putLong("Revision", revision); out.putInt("Requisitions", requisitions);
            ListTag unlocked = new ListTag(), savedModules = new ListTag(), loadout = new ListTag(), paid = new ListTag();
            LibertyStratagemCatalog.selectable().forEach(entry -> { if (callins.contains(entry.id())) unlocked.add(StringTag.valueOf(entry.id())); });
            for (Module module : Module.values()) if (modules.contains(module)) savedModules.add(StringTag.valueOf(module.id));
            equipped.forEach(id -> loadout.add(StringTag.valueOf(id)));
            receipts.forEach((run, hash) -> { CompoundTag row = new CompoundTag(); row.putUUID("Run", run); row.putString("Hash", hash); paid.add(row); });
            out.put("Callins", unlocked); out.put("Modules", savedModules); out.put("Equipped", loadout); out.put("Receipts", paid); return out;
        }
    }

    /** The manager supplies in-ship permission. A read never bootstraps, repairs or truncates player NBT. */
    public static CompoundTag snapshot(ServerPlayer player) { return snapshot(player, false); }
    public static CompoundTag snapshot(ServerPlayer player, boolean canManage) {
        Account account = read(player); boolean blocked = account == null;
        boolean manage = canManage && account != null && account.revision < Long.MAX_VALUE && alive(player);
        CompoundTag out = new CompoundTag(); out.putInt("Schema", SCHEMA); out.putBoolean("Blocked", blocked);
        out.putLong("Revision", blocked ? -1 : account.revision); out.putInt("Requisitions", blocked ? 0 : account.requisitions);
        out.putInt("ReceiptCount", blocked ? 0 : account.receipts.size()); out.putBoolean("CanManage", manage);
        out.putString("Reason", blocked ? "Ship progress needs recovery or a newer compatible version; the saved record is retained."
                : account.revision == Long.MAX_VALUE ? "Ship account revision is exhausted; the saved record is retained."
                : !canManage ? "Dock at your ship to change hardware and the four-slot call-in loadout." : "");
        ListTag loadout = new ListTag(); if (!blocked) account.equipped.forEach(id -> loadout.add(StringTag.valueOf(id))); out.put("Equipped", loadout);
        ListTag catalogue = new ListTag();
        for (LibertyStratagemCatalog.Entry entry : LibertyStratagemCatalog.all()) {
            CompoundTag row = new CompoundTag(); boolean unlocked = !blocked && account.callins.contains(entry.id());
            String requirement = callinReason(player, entry);
            row.putString("Id", entry.id()); row.putString("Name", entry.name()); row.putString("Category", entry.category().name().toLowerCase(java.util.Locale.ROOT));
            row.putString("Availability", entry.availability().name().toLowerCase(java.util.Locale.ROOT)); row.putString("Description", entry.description());
            row.putString("Controls", entry.controls()); row.putString("Backend", entry.backend()); row.putString("Source", entry.source());
            row.putInt("Cost", entry.unlockCost()); row.putInt("CooldownTicks", entry.cooldownTicks());
            row.putInt("MissionUses", entry.missionUses()); row.putInt("EagleCharges", entry.eagleCharges());
            row.putBoolean("Selectable", entry.selectable()); row.putBoolean("Unlocked", unlocked);
            row.putBoolean("Affordable", !blocked && account.requisitions >= entry.unlockCost());
            row.putBoolean("CanPurchase", manage && !unlocked && requirement.isEmpty() && account.requisitions >= entry.unlockCost());
            row.putBoolean("CanEquip", manage && unlocked && entry.selectable());
            row.putInt("EquippedSlot", blocked ? -1 : account.equipped.indexOf(entry.id())); row.putString("Reason", requirement); catalogue.add(row);
        }
        out.put("Callins", catalogue); ListTag modules = new ListTag();
        for (Module module : Module.values()) {
            CompoundTag row = new CompoundTag(); String requirement = moduleReason(player, module);
            boolean unlocked = !blocked && account.modules.contains(module);
            row.putString("Id", module.id); row.putString("Name", module.name); row.putString("Description", module.effect);
            row.putInt("Cost", module.cost); row.putBoolean("Unlocked", unlocked); row.putString("Reason", requirement);
            row.putBoolean("Affordable", !blocked && account.requisitions >= module.cost);
            row.putBoolean("CanPurchase", manage && !unlocked && requirement.isEmpty() && account.requisitions >= module.cost); modules.add(row);
        }
        out.put("Modules", modules); return out;
    }

    /** Root's journal calls this before consuming its outcome. Exact retries return true without paying again. */
    public static boolean settle(ServerPlayer player, UUID run, int completed, int difficulty, boolean extracted) {
        Account account = read(player);
        if (account == null || run == null || !validResult(completed, difficulty, extracted)) return false;
        String hash = resultHash(player.getUUID(), run, completed, difficulty, extracted);
        String recorded = account.receipts.get(run);
        if (recorded != null) return recorded.equals(hash);
        if (account.receipts.size() >= MAX_RECEIPTS || account.revision == Long.MAX_VALUE) return false;
        account.requisitions = (int) Math.min(WALLET_LIMIT, (long) account.requisitions + requisitionReward(completed, difficulty, extracted));
        account.receipts.put(run, hash); account.revision++; write(player, account); return true;
    }
    public static int requisitionReward(int completed, int difficulty, boolean extracted) {
        return validResult(completed, difficulty, extracted) ? difficulty * (6 * completed + (extracted ? 15 : 0)) : 0;
    }
    public static boolean settled(ServerPlayer player, UUID run) {
        Account account = read(player); return account != null && run != null && account.receipts.containsKey(run);
    }
    public static boolean canReceiveResult(ServerPlayer player) {
        Account account = read(player); return account != null && account.revision < Long.MAX_VALUE && account.receipts.size() < MAX_RECEIPTS;
    }
    private static boolean validResult(int completed, int difficulty, boolean extracted) {
        return completed >= 0 && completed <= 16 && difficulty >= 1 && difficulty <= 10 && (!extracted || completed > 0);
    }
    private static String resultHash(UUID owner, UUID run, int completed, int difficulty, boolean extracted) {
        return LibertyCampaignData.digest(owner + "|" + run + "|" + completed + "|" + difficulty + "|" + extracted);
    }

    /** The existing manager authorizes the ship context before these versioned server mutations. */
    public static boolean purchase(ServerPlayer player, String id, long expectedRevision) {
        Account account = read(player);
        if (account == null || !alive(player) || account.revision != expectedRevision || account.revision == Long.MAX_VALUE) return false;
        Module module = Module.byId(id);
        if (module != null) {
            if (account.modules.contains(module) || !moduleReason(player, module).isEmpty() || account.requisitions < module.cost) return false;
            account.requisitions -= module.cost; account.modules.add(module);
        } else {
            LibertyStratagemCatalog.Entry entry = LibertyStratagemCatalog.find(id);
            if (entry == null || account.callins.contains(id) || !callinReason(player, entry).isEmpty()
                    || account.requisitions < entry.unlockCost()) return false;
            account.requisitions -= entry.unlockCost(); account.callins.add(id);
        }
        account.revision++; write(player, account); return true;
    }
    public static boolean equip(ServerPlayer player, String id, int slot, long expectedRevision) {
        Account account = read(player); LibertyStratagemCatalog.Entry entry = LibertyStratagemCatalog.find(id);
        if (account == null || !alive(player) || slot < 0 || slot >= LOADOUT_SLOTS || account.revision != expectedRevision
                || account.revision == Long.MAX_VALUE || (!NONE.equals(id) && (entry == null || !entry.selectable() || !account.callins.contains(id)))) return false;
        int current = account.equipped.indexOf(id);
        if (!NONE.equals(id) && current >= 0 && current != slot) return false;
        if (id.equals(account.equipped.get(slot))) return true;
        account.equipped.set(slot, id); account.revision++; write(player, account); return true;
    }
    public static List<String> equipped(ServerPlayer player) {
        Account account = read(player); return account == null ? List.of() : List.copyOf(account.equipped);
    }
    public static boolean unlocked(ServerPlayer player, String id) {
        Account account = read(player); Module module = Module.byId(id);
        return account != null && (module != null ? account.modules.contains(module) : account.callins.contains(id));
    }
    public static int bonusResupplies(ServerPlayer player) { return unlocked(player, Module.PAINT_WORKSHOP.id) ? 1 : 0; }
    public static double cooldownMultiplier(ServerPlayer player) { return unlocked(player, Module.TARGETING_ARRAY.id) ? .9 : 1; }
    public static int bonusEagleCharges(ServerPlayer player) { return unlocked(player, Module.EAGLE_HANGAR.id) ? 1 : 0; }
    public static int bonusCommonSamples(ServerPlayer player, int completed, boolean extracted) {
        return extracted && completed >= 0 && completed <= 16 && unlocked(player, Module.SAMPLE_LAB.id) ? completed : 0;
    }
    private static String callinReason(ServerPlayer player, LibertyStratagemCatalog.Entry entry) {
        if (!entry.selectable()) return "This is a shared, mission-specific or historical reference; it is not a permanent loadout slot.";
        if (entry.availability() == LibertyStratagemCatalog.Availability.CAMPAIGN
                && (player == null || player.getServer() == null || !LibertyCampaignData.get(player.getServer()).frontierUnlocked()))
            return "Liberate any parent region to 20% and open its frontier route to unlock campaign call-ins.";
        return "";
    }
    private static String moduleReason(ServerPlayer player, Module module) {
        if (!module.research.isEmpty() && !LibertyResearch.unlocked(player, module.research))
            return "Complete " + LibertyResearch.Project.byId(module.research).orElseThrow().name + " research first.";
        if (module == Module.SAMPLE_LAB) {
            CrewProfile crew = player == null || player.getServer() == null ? null : CrewHouseData.get(player.getServer()).profile(player.getUUID());
            if (crew == null || crew.xp() < 100) return "Found a crew house and earn 100 shared crew XP to staff this laboratory.";
        }
        return "";
    }
    private static boolean alive(ServerPlayer player) { return player != null && player.getServer() != null && player.isAlive() && !player.isSpectator(); }

    private static Account read(ServerPlayer player) {
        if (player == null) return null;
        CompoundTag persistent = player.getPersistentData();
        if (persistent.contains(Player.PERSISTED_NBT_TAG) && !persistent.contains(Player.PERSISTED_NBT_TAG, Tag.TAG_COMPOUND)) return null;
        CompoundTag data = persistent.getCompound(Player.PERSISTED_NBT_TAG);
        if (!data.contains(KEY)) return new Account(player.getUUID());
        if (!data.contains(KEY, Tag.TAG_COMPOUND)) return null;
        CompoundTag raw = data.getCompound(KEY);
        try {
            if (!raw.contains("Schema", Tag.TAG_INT) || raw.getInt("Schema") != SCHEMA || !raw.hasUUID("Owner")
                    || !player.getUUID().equals(raw.getUUID("Owner")) || !raw.contains("Revision", Tag.TAG_LONG) || raw.getLong("Revision") < 0
                    || !raw.contains("Requisitions", Tag.TAG_INT) || raw.getInt("Requisitions") < 0 || raw.getInt("Requisitions") > WALLET_LIMIT
                    || !Set.of("Schema", "Owner", "Revision", "Requisitions", "Callins", "Modules", "Equipped", "Receipts").containsAll(raw.getAllKeys())) return null;
            Account account = new Account(player.getUUID()); account.callins.clear(); account.equipped.clear();
            for (Tag value : list(raw, "Callins", Tag.TAG_STRING, LibertyStratagemCatalog.selectable().size())) {
                String id = value.getAsString(); LibertyStratagemCatalog.Entry entry = LibertyStratagemCatalog.find(id);
                if (entry == null || !entry.selectable() || !account.callins.add(id)) return null;
            }
            if (!account.callins.containsAll(LibertyStratagemCatalog.starters())) return null;
            for (Tag value : list(raw, "Modules", Tag.TAG_STRING, Module.values().length)) {
                Module module = Module.byId(value.getAsString()); if (module == null || !account.modules.add(module)) return null;
            }
            Set<String> assigned = new LinkedHashSet<>();
            for (Tag value : list(raw, "Equipped", Tag.TAG_STRING, LOADOUT_SLOTS)) {
                String id = value.getAsString();
                if (!NONE.equals(id) && (!account.callins.contains(id) || !assigned.add(id))) return null;
                account.equipped.add(id);
            }
            if (account.equipped.size() != LOADOUT_SLOTS) return null;
            for (Tag value : list(raw, "Receipts", Tag.TAG_COMPOUND, MAX_RECEIPTS)) {
                CompoundTag row = (CompoundTag) value;
                if (!row.hasUUID("Run") || !row.contains("Hash", Tag.TAG_STRING) || !HASH.matcher(row.getString("Hash")).matches()
                        || !Set.of("Run", "Hash").containsAll(row.getAllKeys())
                        || account.receipts.putIfAbsent(row.getUUID("Run"), row.getString("Hash")) != null) return null;
            }
            account.revision = raw.getLong("Revision"); account.requisitions = raw.getInt("Requisitions"); return account;
        } catch (RuntimeException invalid) { return null; }
    }
    private static ListTag list(CompoundTag raw, String key, int type, int max) {
        if (!raw.contains(key, Tag.TAG_LIST)) throw new IllegalArgumentException(key);
        ListTag values = (ListTag) raw.get(key);
        if (values.size() > max || !values.isEmpty() && values.getElementType() != type) throw new IllegalArgumentException(key);
        return values;
    }
    private static void write(ServerPlayer player, Account account) {
        CompoundTag persistent = player.getPersistentData();
        CompoundTag saved = persistent.getCompound(Player.PERSISTED_NBT_TAG).copy(); saved.put(KEY, account.save());
        persistent.put(Player.PERSISTED_NBT_TAG, saved);
    }
    private LibertyShipProgress() { }
}
