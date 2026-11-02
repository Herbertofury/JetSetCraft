package com.herberto.jetsetcraft.crew;

import com.herberto.jetsetcraft.gang.GangRegistry;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceLocation;

import java.util.UUID;

/** Read-only, server-authored progress and affordable next steps. A suggestion never executes an action. */
final class CrewProgressView {
    static void write(CompoundTag state, CrewHouseData data, UUID owner) {
        ListTag history = new ListTag();
        for (CrewRewardReceipt receipt : data.recentRewards(owner)) {
            CompoundTag row = receipt.snapshot();
            row.putString("GangName", GangRegistry.byId(ResourceLocation.tryParse(receipt.gang()))
                    .map(gang -> gang.canonicalName()).orElse(receipt.gang()));
            history.add(row);
        }
        state.put("RecentRewards", history); state.putInt("RewardHistoryLimit", CrewRewardHistory.LIMIT);
        state.put("NextGoal", nextGoal(state));
    }

    private static CompoundTag nextGoal(CompoundTag state) {
        if (!state.getBoolean("Exists")) return goal(state, "Found your crew home", 0,
                "Use a Crew Charter on a placed Boombox, then confirm founding. Your saved progress transfers once; founding adds 24 Supplies and gives saved training to Riff.",
                state.getBoolean("CanFound"), "HOME", "");
        if (!state.getBoolean("AnchorValid")) return goal(state, "Restore access to your home", 0,
                state.getString("ManageReason"), false, "HOME", "");
        CompoundTag workshop = facility(state, "workshop");
        if (!workshop.getBoolean("Registered") || !workshop.getBoolean("Operational")) return facilityGoal(state, workshop);
        if (state.getInt("Tier") == 0) return expansion(state);
        CompoundTag training = facility(state, "training");
        if (!training.getBoolean("Registered") || !training.getBoolean("Operational")) return facilityGoal(state, training);
        ListTag members = state.getList("Members", Tag.TAG_COMPOUND), recruits = state.getList("Recruits", Tag.TAG_COMPOUND);
        if (members.size() < Math.min(3, state.getInt("Capacity")) && !recruits.isEmpty()) {
            CompoundTag recruit = recruits.getCompound(0);
            return goal(state, "Recruit another crew member", recruit.getInt("Cost"),
                    "A third profile can run a job while another stays available for activity training. Choose a species and role in Crew.",
                    recruit.getBoolean("CanRecruit"), "RECRUIT", "");
        }
        for (String id : new String[]{"studio", "recovery"}) {
            CompoundTag room = facility(state, id);
            if (!room.getBoolean("Registered") || !room.getBoolean("Operational")) return facilityGoal(state, room);
        }
        if (workshop.getInt("Level") < 3) return goal(state, "Upgrade your Paint workshop", workshop.getInt("Cost"),
                workshop.getString("Requires") + " Place the required blocks before buying the next tier.",
                workshop.getBoolean("CanUpgrade"), "FACILITY", "workshop");
        if (state.getInt("Tier") < 3) return expansion(state);
        return goal(state, "Keep your crew growing", 0,
                "Claim completed jobs and choose an idle training focus. Street games and optional Rush both advance your crew.",
                state.getBoolean("CanManage"), "JOBS", "");
    }

    private static CompoundTag facilityGoal(CompoundTag state, CompoundTag room) {
        boolean registered = room.getBoolean("Registered");
        String title = (registered ? "Restore your " : "Register your ") + room.getString("Name");
        int cost = !registered && room.getInt("Level") == 0 ? 8 : 0;
        String detail = registered ? room.getString("Status")
                : "Place the facility blocks inside your home plot, use the Charter on its anchor, then register it. " + room.getString("Requires");
        return goal(state, title, cost, detail, !registered && room.getBoolean("CanRegister"), "FACILITY", room.getString("Id"));
    }
    private static CompoundTag expansion(CompoundTag state) {
        int next = Math.min(3, state.getInt("Tier") + 1), side = CrewProfile.RADII[next] * 2 + 1;
        return goal(state, "Expand to " + side + " × " + side, state.getInt("ExpandCost"),
                "Recognize a larger build area and " + CrewProfile.CAPACITIES[next] + " roster slots. Build the extension yourself; the plot must clear overlap and border checks. "
                        + (state.getBoolean("CanExpand") ? "" : state.getString("ExpandReason")),
                state.getBoolean("CanExpand"), "HOME", "");
    }
    private static CompoundTag facility(CompoundTag state, String id) {
        for (Tag entry : state.getList("Facilities", Tag.TAG_COMPOUND))
            if (entry instanceof CompoundTag row && row.getString("Id").equals(id)) return row;
        return new CompoundTag();
    }
    private static CompoundTag goal(CompoundTag state, String title, int cost, String detail, boolean ready, String view, String target) {
        int available = state.getBoolean("Exists") ? state.getInt("Supplies") : state.getInt("PendingSupplies");
        CompoundTag goal = new CompoundTag(); goal.putString("Title", title); goal.putString("Detail", detail);
        goal.putInt("Cost", cost); goal.putInt("Available", available); goal.putInt("Shortfall", Math.max(0, cost - available));
        goal.putBoolean("Affordable", available >= cost); goal.putBoolean("Ready", ready);
        goal.putString("View", view); goal.putString("Target", target);
        goal.putString("Status", cost == 0 ? "No supply cost. Physical and home checks still apply."
                : available >= cost ? "Affordable: " + cost + " Supplies; " + available + " available."
                : "Save " + (cost - available) + " more Supplies: " + available + " / " + cost + ".");
        return goal;
    }
    private CrewProgressView() {}
}
