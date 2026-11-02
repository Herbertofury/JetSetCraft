package com.herberto.jetsetcraft.liberty;

import com.herberto.jetsetcraft.activity.ActivityManager;
import com.herberto.jetsetcraft.rush.RivalRushManager;
import com.herberto.jetsetcraft.rush.RushReturnState;
import com.herberto.jetsetcraft.rush.RushRun;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.nbt.Tag;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;

import java.util.EnumSet;
import java.util.Optional;
import java.util.UUID;

/**
 * Player-owned expedition research. A single armed mission is consumed in the same NBT replacement
 * as its payout, so a bounded display history never becomes the settlement replay guard.
 * The caller checkpoints prepared settlement before restoring the player's ordinary-world journal.
 */
public final class LibertyResearch {
    public static final String KEY = "jetsetcraft:LibertyResearch";
    public static final String NONE = "NONE";
    public static final int WALLET_LIMIT = 1_000_000;
    private static final int MISSION_LIMIT = 10_000;
    private static final int SCHEMA = 1;

    public enum Project {
        PRESSURE_CAP("pressure_cap", "Pressure Saver", 20, 8, 0, 0, null,
                "Every fourth successful graffiti placement uses no spray-can durability, in every mode."),
        MAGNETIC_PIGMENT("magnetic_pigment", "Magnetic Pigment", 20, 6, 0, 0, RushRun.Upgrade.PICKUP,
                "Unlock Magnetic Mixtape as your one Rival Rush starting preparation: +1.5-block pickup reach."),
        BLAST_STENCIL("blast_stencil", "Blast Stencil", 35, 12, 4, 0, RushRun.Upgrade.AMPLITUDE,
                "Unlock Bigger Chorus as your one Rival Rush starting preparation: +12% paint and artifact area."),
        PRISM_INK("prism_ink", "Prism Ink", 50, 16, 6, 1, RushRun.Upgrade.CRITICAL,
                "Unlock Finishing Touch as your one Rival Rush starting preparation: +25% base damage on critical hits."),
        SIGNAL_LACQUER("signal_lacquer", "Signal Lacquer", 30, 10, 2, 0, null,
                "Liberty Grind mural objectives require two distinct marked panels instead of three."),
        SUPPLY_SKATES("supply_skates", "Supply Skates", 40, 16, 4, 2, null,
                "Begin each Liberty Grind deployment with one additional resupply charge.");

        public final String id, name, effect;
        public final int credits, common, rare, superSamples;
        public final RushRun.Upgrade preparation;

        Project(String id, String name, int credits, int common, int rare, int superSamples,
                RushRun.Upgrade preparation, String effect) {
            this.id = id; this.name = name; this.credits = credits; this.common = common;
            this.rare = rare; this.superSamples = superSamples; this.preparation = preparation; this.effect = effect;
        }

        public static Optional<Project> byId(String id) {
            for (Project project : values()) if (project.id.equals(id)) return Optional.of(project);
            return Optional.empty();
        }
    }

    private static final class Account {
        final UUID owner;
        int credits, common, rare, superSamples, paintCounter;
        long revision;
        final EnumSet<Project> unlocked = EnumSet.noneOf(Project.class);
        String selected = NONE;
        UUID pendingRun, lastRun;
        boolean staged;
        int pendingCredits, pendingCommon, pendingRare, pendingSuper;

        Account(UUID owner) { this.owner = owner; }

        boolean affordable(Project project) {
            return credits >= project.credits && common >= project.common && rare >= project.rare
                    && superSamples >= project.superSamples;
        }

        CompoundTag save() {
            CompoundTag out = new CompoundTag();
            out.putInt("Schema", SCHEMA); out.putUUID("Owner", owner); out.putLong("Revision", revision);
            out.putInt("Credits", credits); out.putInt("Common", common); out.putInt("Rare", rare);
            out.putInt("Super", superSamples); out.putInt("PaintCounter", paintCounter);
            out.putString("SelectedPreparation", selected);
            ListTag choices = new ListTag();
            for (Project project : Project.values()) if (unlocked.contains(project)) choices.add(StringTag.valueOf(project.id));
            out.put("Unlocked", choices);
            if (lastRun != null) out.putUUID("LastRun", lastRun);
            if (pendingRun != null) {
                CompoundTag pending = new CompoundTag(); pending.putUUID("Run", pendingRun); pending.putBoolean("Staged", staged);
                pending.putInt("Credits", pendingCredits); pending.putInt("Common", pendingCommon);
                pending.putInt("Rare", pendingRare); pending.putInt("Super", pendingSuper); out.put("Pending", pending);
            }
            return out;
        }
    }

    /** Read-only snapshots never create, repair, truncate, or replace a saved account. */
    public static CompoundTag snapshot(ServerPlayer player) {
        Account account = read(player);
        CompoundTag out = new CompoundTag(); out.putInt("Schema", SCHEMA);
        boolean blocked = account == null;
        String reason = blocked ? "Research data needs recovery or a newer compatible mod version; the saved record is retained."
                : managementReason(player, account);
        out.putBoolean("Blocked", blocked); out.putBoolean("CanManage", reason.isEmpty()); out.putString("Reason", reason);
        out.putLong("Revision", blocked ? -1 : account.revision);
        out.putInt("Credits", blocked ? 0 : account.credits); out.putInt("Common", blocked ? 0 : account.common);
        out.putInt("Rare", blocked ? 0 : account.rare); out.putInt("Super", blocked ? 0 : account.superSamples);
        out.putString("SelectedPreparation", blocked ? NONE : account.selected);
        out.putBoolean("Pending", !blocked && account.pendingRun != null);
        ListTag projects = new ListTag();
        for (Project project : Project.values()) {
            CompoundTag row = new CompoundTag();
            boolean unlocked = !blocked && account.unlocked.contains(project);
            row.putString("Id", project.id); row.putString("Name", project.name);
            row.putString("Description", project.effect); row.putString("Effect", project.effect);
            row.putInt("Credits", project.credits); row.putInt("Common", project.common);
            row.putInt("Rare", project.rare); row.putInt("Super", project.superSamples);
            row.putBoolean("Unlocked", unlocked); row.putBoolean("Affordable", !blocked && account.affordable(project));
            row.putBoolean("CanPurchase", reason.isEmpty() && !unlocked && account.affordable(project));
            row.putBoolean("Preparation", project.preparation != null);
            row.putBoolean("Selected", !blocked && project.id.equals(account.selected));
            projects.add(row);
        }
        out.put("Projects", projects); return out;
    }

    /** Arms the exact server-created mission before the caller durably saves the return checkpoint. */
    public static boolean begin(ServerPlayer player, UUID runId) {
        Account account = read(player);
        if (account == null || runId == null || account.pendingRun != null || runId.equals(account.lastRun)
                || account.revision > Long.MAX_VALUE - 3) return false;
        account.pendingRun = runId; account.revision++;
        write(player, account); return true;
    }

    public static Optional<UUID> pendingMission(ServerPlayer player) {
        Account account = read(player); return account == null ? Optional.empty() : Optional.ofNullable(account.pendingRun);
    }

    /** Recovery may recognize the just-consumed nonce while its separate crew outcome is still pending. */
    public static boolean lastSettled(ServerPlayer player, UUID runId) {
        Account account = read(player);
        return account != null && runId != null && runId.equals(account.lastRun);
    }

    public static boolean staged(ServerPlayer player) {
        Account account = read(player); return account != null && account.pendingRun != null && account.staged;
    }

    /**
     * Saves a write-once settlement intent. Repeating the exact intent is harmless; changing it is rejected.
     * These amounts are accepted only from the authoritative mission manager, never a client payload.
     */
    public static boolean stageSettlement(ServerPlayer player, UUID runId, int credits, int common, int rare, int superSamples) {
        Account account = read(player);
        if (account == null || runId == null || !runId.equals(account.pendingRun)
                || !missionAmount(credits) || !missionAmount(common) || !missionAmount(rare) || !missionAmount(superSamples)) return false;
        if (account.staged) return account.pendingCredits == credits && account.pendingCommon == common
                && account.pendingRare == rare && account.pendingSuper == superSamples;
        if (account.revision == Long.MAX_VALUE) return false;
        account.pendingCredits = credits; account.pendingCommon = common; account.pendingRare = rare; account.pendingSuper = superSamples;
        account.staged = true; account.revision++;
        write(player, account); return true;
    }

    /** Payout and nonce consumption replace one persisted subtree together. No old nonce can settle again. */
    public static boolean settlePending(ServerPlayer player) {
        Account account = read(player);
        if (account == null || account.pendingRun == null || !account.staged || account.revision == Long.MAX_VALUE) return false;
        account.credits = add(account.credits, account.pendingCredits); account.common = add(account.common, account.pendingCommon);
        account.rare = add(account.rare, account.pendingRare); account.superSamples = add(account.superSamples, account.pendingSuper);
        account.lastRun = account.pendingRun; account.pendingRun = null; account.staged = false;
        account.pendingCredits = 0; account.pendingCommon = 0; account.pendingRare = 0; account.pendingSuper = 0;
        account.revision++; write(player, account); return true;
    }

    public static boolean settle(ServerPlayer player, UUID runId, int credits, int common, int rare, int superSamples) {
        return stageSettlement(player, runId, credits, common, rare, superSamples) && settlePending(player);
    }

    /** A mission without a prepared result was interrupted before earning a durable payout. */
    public static boolean abandonPending(ServerPlayer player) {
        Account account = read(player);
        if (account == null || account.pendingRun == null || account.staged) return false;
        return settle(player, account.pendingRun, 0, 0, 0, 0);
    }

    public static boolean purchase(ServerPlayer player, String id, long expectedRevision) {
        Account account = read(player); Project project = Project.byId(id).orElse(null);
        if (account == null || project == null || !managementReason(player, account).isEmpty()
                || expectedRevision != account.revision || account.revision == Long.MAX_VALUE
                || account.unlocked.contains(project) || !account.affordable(project)) return false;
        account.credits -= project.credits; account.common -= project.common;
        account.rare -= project.rare; account.superSamples -= project.superSamples;
        account.unlocked.add(project); account.revision++;
        write(player, account); return true;
    }

    /** Research preparations occupy the existing single starting-rank slot; NONE returns that slot to crew styles. */
    public static boolean selectPreparation(ServerPlayer player, String id, long expectedRevision) {
        Account account = read(player); Project project = Project.byId(id).orElse(null);
        if (account == null || !managementReason(player, account).isEmpty() || expectedRevision != account.revision
                || account.revision == Long.MAX_VALUE || (!NONE.equals(id)
                && (project == null || project.preparation == null || !account.unlocked.contains(project)))) return false;
        account.selected = id; account.revision++;
        write(player, account); return true;
    }

    public static boolean unlocked(ServerPlayer player, String id) {
        Account account = read(player); Project project = Project.byId(id).orElse(null);
        return account != null && project != null && account.unlocked.contains(project);
    }

    public static Optional<RushRun.Upgrade> preparation(ServerPlayer player) {
        Account account = read(player);
        if (account == null) return Optional.empty();
        return Project.byId(account.selected).filter(account.unlocked::contains).map(project -> project.preparation);
    }

    /** Call once only after an authorized successful placement, at the existing durability-spend boundary. */
    public static boolean shouldConsumePaint(ServerPlayer player) {
        Account account = read(player);
        if (account == null || !account.unlocked.contains(Project.PRESSURE_CAP)) return true;
        account.paintCounter = (account.paintCounter + 1) % 4;
        // A paint-use counter does not invalidate a research purchase page: it never changes balances or selections.
        write(player, account); return account.paintCounter != 0;
    }

    public static int muralPanels(ServerPlayer player) { return unlocked(player, "signal_lacquer") ? 2 : 3; }
    public static int bonusResupplies(ServerPlayer player) { return unlocked(player, "supply_skates") ? 1 : 0; }

    private static String managementReason(ServerPlayer player, Account account) {
        if (player == null || player.getServer() == null || !player.isAlive() || player.isSpectator())
            return "Research while alive and outside Spectator mode.";
        if (account.pendingRun != null || RivalRushManager.active(player) || RushReturnState.hasJournal(player)
                || LibertyManager.active(player) || LibertyManager.hasReturn(player)
                || ActivityManager.active(player)) return "Return from your current activity before changing research.";
        String dimension = player.serverLevel().dimension().location().toString();
        if (dimension.equals("jetsetcraft:liberty_grind") || dimension.equals("jetsetcraft:rival_rush"))
            return "Return to your normal world before changing research.";
        return "";
    }

    private static Account read(ServerPlayer player) {
        if (player == null) return null;
        CompoundTag persistent = player.getPersistentData();
        if (persistent.contains(Player.PERSISTED_NBT_TAG) && !persistent.contains(Player.PERSISTED_NBT_TAG, Tag.TAG_COMPOUND)) return null;
        CompoundTag data = persistent.getCompound(Player.PERSISTED_NBT_TAG);
        if (!data.contains(KEY)) return new Account(player.getUUID());
        if (!data.contains(KEY, Tag.TAG_COMPOUND)) return null;
        CompoundTag raw = data.getCompound(KEY);
        if (!raw.contains("Schema", Tag.TAG_INT) || raw.getInt("Schema") != SCHEMA || !raw.hasUUID("Owner")
                || !player.getUUID().equals(raw.getUUID("Owner")) || !raw.contains("Revision", Tag.TAG_LONG)
                || raw.getLong("Revision") < 0 || !raw.contains("SelectedPreparation", Tag.TAG_STRING)
                || !raw.contains("Unlocked", Tag.TAG_LIST)) return null;
        for (String key : new String[]{"Credits", "Common", "Rare", "Super"})
            if (!raw.contains(key, Tag.TAG_INT) || raw.getInt(key) < 0 || raw.getInt(key) > WALLET_LIMIT) return null;
        if (!raw.contains("PaintCounter", Tag.TAG_INT) || raw.getInt("PaintCounter") < 0 || raw.getInt("PaintCounter") > 3) return null;
        ListTag saved = (ListTag) raw.get("Unlocked");
        if (saved.size() > Project.values().length || (!saved.isEmpty() && saved.getElementType() != Tag.TAG_STRING)) return null;
        Account account = new Account(player.getUUID());
        for (int i = 0; i < saved.size(); i++) {
            Project project = Project.byId(saved.getString(i)).orElse(null);
            if (project == null || !account.unlocked.add(project)) return null;
        }
        account.selected = raw.getString("SelectedPreparation");
        Project selected = Project.byId(account.selected).orElse(null);
        if (!NONE.equals(account.selected) && (selected == null || selected.preparation == null || !account.unlocked.contains(selected))) return null;
        account.credits = raw.getInt("Credits"); account.common = raw.getInt("Common"); account.rare = raw.getInt("Rare");
        account.superSamples = raw.getInt("Super"); account.paintCounter = raw.getInt("PaintCounter"); account.revision = raw.getLong("Revision");
        if (raw.contains("LastRun")) {
            if (!raw.hasUUID("LastRun")) return null;
            account.lastRun = raw.getUUID("LastRun");
        }
        if (raw.contains("Pending")) {
            if (!raw.contains("Pending", Tag.TAG_COMPOUND)) return null;
            CompoundTag pending = raw.getCompound("Pending");
            if (!pending.hasUUID("Run") || !pending.contains("Staged", Tag.TAG_BYTE)) return null;
            for (String key : new String[]{"Credits", "Common", "Rare", "Super"})
                if (!pending.contains(key, Tag.TAG_INT) || !missionAmount(pending.getInt(key))) return null;
            account.pendingRun = pending.getUUID("Run"); account.staged = pending.getBoolean("Staged");
            if (account.pendingRun.equals(account.lastRun)) return null;
            account.pendingCredits = pending.getInt("Credits"); account.pendingCommon = pending.getInt("Common");
            account.pendingRare = pending.getInt("Rare"); account.pendingSuper = pending.getInt("Super");
            if (!account.staged && (account.pendingCredits != 0 || account.pendingCommon != 0
                    || account.pendingRare != 0 || account.pendingSuper != 0)) return null;
        }
        return account;
    }

    private static void write(ServerPlayer player, Account account) {
        CompoundTag persisted = player.getPersistentData().getCompound(Player.PERSISTED_NBT_TAG);
        persisted.put(KEY, account.save()); player.getPersistentData().put(Player.PERSISTED_NBT_TAG, persisted);
    }

    private static boolean missionAmount(int amount) { return amount >= 0 && amount <= MISSION_LIMIT; }
    private static int add(int amount, int earned) { return (int) Math.min(WALLET_LIMIT, (long) amount + earned); }
    private LibertyResearch() {}
}
