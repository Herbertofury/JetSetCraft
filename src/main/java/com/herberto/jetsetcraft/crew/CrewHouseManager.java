package com.herberto.jetsetcraft.crew;

import com.herberto.jetsetcraft.JetSetCraft;
import com.herberto.jetsetcraft.activity.ActivityManager;
import com.herberto.jetsetcraft.blockentity.BoomboxBlockEntity;
import com.herberto.jetsetcraft.config.JetSetConfig;
import com.herberto.jetsetcraft.gang.GangRegistry;
import com.herberto.jetsetcraft.network.JetSetNetwork;
import com.herberto.jetsetcraft.registry.ModBlocks;
import com.herberto.jetsetcraft.rush.RivalRushManager;
import com.herberto.jetsetcraft.rush.RushAlbum;
import com.herberto.jetsetcraft.rush.RushRun;
import com.herberto.jetsetcraft.rush.RushWorld;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.level.block.BedBlock;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.function.Predicate;

/** Optional player-built homes. All persistent changes are one owner-scoped SavedData transaction. */
public final class CrewHouseManager {
    private record Candidate(String dimension, BlockPos position, long expires) {}
    private static final Map<MinecraftServer, Map<UUID, Candidate>> CANDIDATES = new HashMap<>();
    private static final class ResponseBudget {
        long responseAt = -100, previewAt = -100, revision = -1, styleRevision = -1;
        UUID house;
        String candidateDimension = "";
        BlockPos candidatePosition;
    }
    private static final Map<MinecraftServer, Map<UUID, ResponseBudget>> RESPONSES = new HashMap<>();
    private static final Set<String> ACTIONS = Set.of("open", "refresh", "found", "relocate", "rename", "preview",
            "expand", "recruit", "rename_member", "focus", "focus_off", "assign", "claim", "cancel", "facility", "upgrade", "preparation", "style");
    private static final int[] EXPANSION_COSTS = {40, 100, 200, 0};

    private enum Kind {
        WORKSHOP("workshop", "Paint workshop", RushRun.Upgrade.FORCE,
                "Crafting or smithing table", "anvil", "grindstone"),
        TRAINING("training", "Practice yard", RushRun.Upgrade.CADENCE,
                "Target block", "iron bars", "smooth stone slab"),
        STUDIO("studio", "Mix studio", RushRun.Upgrade.FLOW,
                "Jukebox", "note block", "redstone lamp"),
        RECOVERY("recovery", "Recovery corner", RushRun.Upgrade.RECOVERY,
                "Brewing stand", "cauldron", "bed");
        final String id, label, anchorName, second, third;
        final RushRun.Upgrade perk;
        Kind(String id, String label, RushRun.Upgrade perk, String anchorName, String second, String third) {
            this.id = id; this.label = label; this.perk = perk; this.anchorName = anchorName; this.second = second; this.third = third;
        }
        boolean anchor(BlockState state) {
            return switch (this) {
                case WORKSHOP -> state.is(Blocks.CRAFTING_TABLE) || state.is(Blocks.SMITHING_TABLE);
                case TRAINING -> state.is(Blocks.TARGET);
                case STUDIO -> state.is(Blocks.JUKEBOX);
                case RECOVERY -> state.is(Blocks.BREWING_STAND);
            };
        }
        boolean support(BlockState state, int tier) {
            return switch (this) {
                case WORKSHOP -> tier == 2 ? state.is(Blocks.ANVIL) || state.is(Blocks.CHIPPED_ANVIL) || state.is(Blocks.DAMAGED_ANVIL) : state.is(Blocks.GRINDSTONE);
                case TRAINING -> tier == 2 ? state.is(Blocks.IRON_BARS) : state.is(Blocks.SMOOTH_STONE_SLAB);
                case STUDIO -> tier == 2 ? state.is(Blocks.NOTE_BLOCK) : state.is(Blocks.REDSTONE_LAMP);
                case RECOVERY -> tier == 2 ? state.is(Blocks.CAULDRON) || state.is(Blocks.WATER_CAULDRON)
                        : state.getBlock() instanceof BedBlock;
            };
        }
        String requirements() { return anchorName + "; tier 2: nearby " + second + "; tier 3: nearby " + third + ". Supports must be within 2 blocks and inside the plot."; }
        static Kind find(String id) { for (Kind kind : values()) if (kind.id.equals(id)) return kind; return null; }
    }

    public static void open(ServerPlayer player) { send(player, ""); }
    public static void openAt(ServerPlayer player, BlockPos position) {
        if (player == null || position == null) return;
        if (player.distanceToSqr(position.getX() + 0.5, position.getY() + 0.5, position.getZ() + 0.5) <= 64
                && player.serverLevel().hasChunkAt(position)) {
            CANDIDATES.computeIfAbsent(player.getServer(), ignored -> new HashMap<>()).put(player.getUUID(),
                    new Candidate(player.serverLevel().dimension().location().toString(), position.immutable(), now(player) + 1200));
            send(player, "Selected a placed block. Review the home or facility action before confirming.");
        } else send(player, "Stand within eight blocks of the placed anchor or facility.");
    }

    public static void handleAction(ServerPlayer player, String action, String value, int number,
                                    UUID houseId, UUID targetId, long revision) {
        if (player == null || !ACTIONS.contains(action)) return;
        if (action.equals("open") || action.equals("refresh")) { send(player, ""); return; }
        String blocked = normalModeReason(player);
        if (!blocked.isEmpty()) { send(player, blocked); return; }
        CrewHouseData data = CrewHouseData.get(player.getServer());
        CrewProfile profile = data.profile(player.getUUID());
        if (data.blocked(player.getUUID())) { send(player, "This crew record needs recovery or a newer compatible mod version; existing data was retained."); return; }
        if (action.equals("style")) {
            boolean selected = data.selectStyle(player.getUUID(), value, revision);
            send(player, !selected ? "That style is locked, unavailable or the page is stale. Refresh the Style Library."
                    : CrewStyleLibrary.HOME.equals(value) ? "Your next Rush uses the selected operational house preparation, if available."
                    : "Starting style selected. Your next Rush begins with one rank of "
                        + CrewStyleLibrary.preparation(data, player.getUUID()).orElseThrow().label()
                        + ". This uses the single preparation slot; your crew XP is kept.");
            return;
        }
        if (action.equals("found")) {
            if (profile != null || houseId != null || revision != 0) { send(player, "Your home already exists or the page is stale. Refresh the ledger."); return; }
            send(player, found(player, data, value)); return;
        }
        if (profile == null || !profile.id.equals(houseId) || profile.revision != revision) {
            send(player, "That home page is out of date. Review the refreshed state and try again."); return;
        }
        if (action.equals("relocate")) {
            String result = relocate(player, profile, data);
            send(player, result); return;
        }
        String unavailable = managementReason(player, profile);
        if (!unavailable.isEmpty()) { send(player, unavailable); return; }
        long before = profile.revision;
        String message = act(player, profile, action, value == null ? "" : value, number, targetId);
        if (before != profile.revision) data.setDirty();
        send(player, message);
    }

    private static String found(ServerPlayer player, CrewHouseData data, String name) {
        String reason = foundingReason(player, data, null);
        if (!reason.isEmpty()) return reason;
        Candidate candidate = candidate(player);
        BoomboxBlockEntity boombox = (BoomboxBlockEntity) player.serverLevel().getBlockEntity(candidate.position);
        String gang = boombox.resolvedTarget().map(t -> t.gangId().toString()).orElse("jetsetcraft:dead_beat");
        CrewProfile profile = new CrewProfile(player.getUUID(), UUID.randomUUID(), name, gang, candidate.dimension, candidate.position);
        String species = representative(gang);
        profile.addMember("Riff", species, gang, CrewMissionCatalog.Role.COURIER);
        profile.addMember("Patches", species, gang, CrewMissionCatalog.Role.TAGGER);
        profile.focus = profile.members.keySet().iterator().next();
        if (!data.add(profile)) return "Your existing crew record was preserved; a second home was not created.";
        CrewProfile.Member first = profile.members.get(profile.focus);
        return "Home founded with " + profile.supplies + " Crew Supplies. "
                + (first != null && first.xp > 0 ? "Saved training applied to " + first.name + ": level " + first.level() + ". " : "")
                + "Build your own house; the Progress tab shows your next practical goal.";
    }

    private static String relocate(ServerPlayer player, CrewProfile profile, CrewHouseData data) {
        String reason = foundingReason(player, data, profile);
        if (!reason.isEmpty()) return reason;
        Candidate candidate = candidate(player);
        if (profile.dimension.equals(candidate.dimension) && profile.anchor.equals(candidate.position)) return "This is already your home anchor.";
        profile.dimension = candidate.dimension; profile.anchor = candidate.position;
        for (CrewProfile.Facility room : profile.facilities.values()) room.position = null;
        profile.changed(); data.setDirty();
        return "Home relocated. Crew identities, jobs and purchased facility tiers remain; register the new physical facilities.";
    }

    private static String act(ServerPlayer player, CrewProfile profile, String action, String value, int number, UUID target) {
        switch (action) {
            case "rename" -> { profile.name = CrewProfile.safeName(value, profile.name); profile.changed(); return "Crew name updated."; }
            case "preview" -> {
                ResponseBudget budget = responseBudget(player);
                if (now(player) < budget.previewAt + 20) return "The current boundary preview is already visible.";
                budget.previewAt = now(player); preview(player, profile);
                return "Plot boundary shown. This is a crew activity area; normal building permissions are unchanged.";
            }
            case "expand" -> {
                String reason = expansionReason(player, profile);
                if (!reason.isEmpty()) return reason;
                profile.addSupplies(-EXPANSION_COSTS[profile.tier]); profile.tier++; profile.changed();
                return "Plot expanded to " + (profile.radius() * 2 + 1) + " × " + (profile.radius() * 2 + 1) + ". Member capacity: " + profile.capacity() + ". Build the extension yourself.";
            }
            case "recruit" -> {
                if (profile.members.size() >= profile.capacity()) return "Expand your plot before recruiting another member.";
                int cost = recruitmentCost(profile);
                if (profile.supplies < cost) return "Recruitment requires " + cost + " Crew Supplies.";
                if (number < 0 || number >= CrewMissionCatalog.Role.values().length || !recruitPool(player, profile).contains(value))
                    return "Choose a currently available species and role from the ledger.";
                CrewMissionCatalog.Role role = CrewMissionCatalog.Role.values()[number];
                profile.addMember(role.label + " " + (profile.members.size() + 1), value,
                        GangRegistry.gangIdForEntity(id(value)).toString(), role);
                profile.addSupplies(-cost); profile.changed(); return "A new roster member joined. Members are mission profiles in this first slice, not follower mobs.";
            }
            case "rename_member" -> {
                CrewProfile.Member member = profile.members.get(target);
                if (member == null) return "That member no longer belongs to this roster.";
                member.name = CrewProfile.safeName(value, member.name); profile.changed(); return "Member name updated.";
            }
            case "focus" -> {
                if (!profile.members.containsKey(target) || profile.busy(target)) return "Choose an idle member for activity training.";
                profile.focus = target; profile.changed(); return "Training focus selected. Completed street games, arenas and earned Rush participation train this member while they are idle.";
            }
            case "focus_off" -> { profile.focus = null; profile.changed(); return "Activity member training disabled."; }
            case "assign" -> {
                int bonus = missionBonus(player.getServer(), profile);
                CrewProfile.Member member = profile.members.get(target);
                if (member != null && member.role == CrewMissionCatalog.Role.DJ && operationalLevel(player.getServer(), profile, Kind.STUDIO) > 0) bonus += 10;
                CrewProfile.Job job = profile.assignMission(target, value, now(player), bonus);
                return job == null ? "Choose an idle member and an available mission slot."
                        : "Member dispatched. Rewards are fixed now; the assignment progresses while this server runs and waits for your claim.";
            }
            case "claim" -> {
                CrewRewardReceipt receipt = profile.claimMissionReward(target, now(player));
                if (receipt == null) return "That mission is not ready, is not yours, or has already been claimed.";
                CrewHouseData.get(player.getServer()).setDirty();
                publishReward(player, receipt);
                return receipt.summary() + ". " + receipt.trainingSummary() + ". " + receipt.levelSummary();
            }
            case "cancel" -> { return profile.cancelMission(target) ? "Assignment cancelled. The member is idle; no rewards were granted." : "That assignment is no longer active."; }
            case "facility" -> { return registerFacility(player, profile, Kind.find(value)); }
            case "upgrade" -> { return upgradeFacility(player, profile, Kind.find(value)); }
            case "preparation" -> {
                if (value.equals("NONE")) { profile.preparation = "NONE"; profile.changed(); return "House preparation disabled. Rival Rush keeps its ordinary starting build."; }
                for (Kind kind : Kind.values()) {
                    if (kind.perk.name().equals(value) && operationalLevel(player.getServer(), profile, kind) > 0) {
                        profile.preparation = value; profile.changed(); return "Selected " + kind.perk.label() + ". It grants one mode-only rank when entering Rush from this loaded home.";
                    }
                }
                return "Build and register the matching physical facility before choosing that preparation.";
            }
            default -> { return "Choose an available crew action."; }
        }
    }

    private static String registerFacility(ServerPlayer player, CrewProfile profile, Kind kind) {
        if (kind == null) return "Choose a supported facility.";
        Candidate candidate = candidate(player);
        if (candidate == null || !profile.dimension.equals(candidate.dimension)) return "Use the Crew Charter on the placed facility block first.";
        CrewProfile.Facility existing = profile.facilities.get(kind.id);
        int level = existing == null ? 1 : Math.max(1, existing.level);
        String reason = physicalReason(player.getServer(), profile, kind, candidate.position, level);
        if (!reason.isEmpty()) return reason;
        if (existing != null && candidate.position.equals(existing.position)) return "This facility is already registered.";
        int cost = existing == null || existing.level == 0 ? 8 : 0;
        if (profile.supplies < cost) return "Registration requires " + cost + " Crew Supplies.";
        profile.addSupplies(-cost); profile.facilities.put(kind.id, new CrewProfile.Facility(level, candidate.position));
        profile.changed(); return kind.label + " registered. Its blocks remain ordinary Minecraft blocks.";
    }

    private static String upgradeFacility(ServerPlayer player, CrewProfile profile, Kind kind) {
        if (kind == null) return "Choose a supported facility.";
        CrewProfile.Facility room = profile.facilities.get(kind.id);
        if (room == null || room.position == null) return "Register the physical facility before upgrading it.";
        if (room.level >= 3) return "This facility is already fully upgraded.";
        String reason = physicalReason(player.getServer(), profile, kind, room.position, room.level + 1);
        if (!reason.isEmpty()) return reason;
        int cost = room.level * 16;
        if (profile.supplies < cost) return "The next tier requires " + cost + " Crew Supplies.";
        profile.addSupplies(-cost); room.level++; profile.changed();
        return kind.label + " tier " + room.level + " unlocked. Upgraded operational facilities improve frozen mission supply rewards; preparations still grant only one rank.";
    }

    /** One starting rank: a learned style anywhere, otherwise the physically validated house preparation. */
    public static Optional<RushRun.Upgrade> rushPreparation(ServerPlayer player) {
        if (player == null || !normalModeReason(player).isEmpty()) return Optional.empty();
        CrewHouseData data = CrewHouseData.get(player.getServer());
        if (data.blocked(player.getUUID())) return Optional.empty();
        Optional<RushRun.Upgrade> style = CrewStyleLibrary.preparation(data, player.getUUID());
        if (style.isPresent()) return style;
        CrewProfile profile = data.profile(player.getUUID());
        if (profile == null || !managementReason(player, profile).isEmpty()) return Optional.empty();
        for (Kind kind : Kind.values()) if (kind.perk.name().equals(profile.preparation)
                && operationalLevel(player.getServer(), profile, kind) > 0) return Optional.of(kind.perk);
        return Optional.empty();
    }

    public record ActivityReward(boolean accepted, int supplies, int crewXp, int memberXp, int reputation,
                                 boolean pendingHouse) {
        public static final ActivityReward NONE = new ActivityReward(false, 0, 0, 0, 0, false);
    }

    /** Server-authored preview, shared by the invitation and completion paths; this never awards anything. */
    public static ActivityReward activityReward(String mode) {
        return switch (mode == null ? "" : mode) {
            case "tag" -> new ActivityReward(false, 8, 55, 20, 6, false);
            case "king_of_hill" -> new ActivityReward(false, 14, 90, 32, 8, false);
            case "showdown" -> new ActivityReward(false, 12, 75, 25, 8, false);
            case "rush_arena" -> new ActivityReward(false, 6, 35, 15, 4, false);
            case "rush_arena_supplies" -> new ActivityReward(false, 12, 25, 10, 3, false);
            case "rush_arena_training" -> new ActivityReward(false, 4, 45, 35, 3, false);
            case "rush_arena_reputation" -> new ActivityReward(false, 4, 30, 10, 9, false);
            default -> ActivityReward.NONE;
        };
    }

    /** Called only by completed server-owned objectives. No packet accepts reward amounts or objective counts. */
    public static ActivityReward bankActivity(ServerPlayer player, UUID eventId, ResourceLocation gang,
                                              String mode, long activeTicks, int objectives, long score) {
        if (mode == null || activeTicks < 0 || objectives < 0 || score < 0) return ActivityReward.NONE;
        boolean eligible = switch (mode) {
            case "tag" -> activeTicks >= 40 && objectives >= 6;
            case "showdown" -> activeTicks >= 40 && objectives >= 4;
            case "king_of_hill" -> activeTicks >= 480 && objectives >= 3;
            case "rush_arena", "rush_arena_supplies", "rush_arena_training", "rush_arena_reputation" -> activeTicks >= 200 && objectives >= 1;
            default -> false;
        };
        if (!eligible) return ActivityReward.NONE;
        ActivityReward reward = activityReward(mode);
        return settle(player, eventId, gang, reward.supplies, reward.crewXp, reward.memberXp, reward.reputation, mode);
    }

    /** Settles genuine mode participation into the same account, including earnings before founding a home. */
    public static void bankRush(ServerPlayer player, UUID runId, ResourceLocation rivalGang, long activeTicks,
                                long cards, long defeats, long score) {
        if (player == null || player.getServer() == null || runId == null || rivalGang == null || !JetSetConfig.SERVER.enableCrewHouse.get()
                || activeTicks < 1200 || defeats < 3 || cards < 0 || score < 0) return;
        int minutes = (int) Math.min(30, activeTicks / 1200);
        int cardBonus = (int) Math.min(20, cards);
        CrewProfile profile = CrewHouseData.get(player.getServer()).profile(player.getUUID());
        int training = profile == null ? 0 : operationalLevel(player.getServer(), profile, Kind.TRAINING);
        settle(player, runId, rivalGang, Math.min(60, 8 + minutes + cardBonus / 3),
                Math.min(300, 20 + minutes * 6 + cardBonus * 2), Math.min(180, 20 + minutes * 3 + training * 10),
                Math.min(40, 2 + minutes), "rush_return");
    }

    private static ActivityReward settle(ServerPlayer player, UUID receipt, ResourceLocation gang,
                                         int supplies, int xp, int training, int reputation, String source) {
        if (player == null || player.getServer() == null || receipt == null || gang == null
                || !JetSetConfig.SERVER.enableCrewHouse.get()) return ActivityReward.NONE;
        CrewHouseData data = CrewHouseData.get(player.getServer());
        if (data.blocked(player.getUUID())) return ActivityReward.NONE;
        CrewRewardReceipt awarded = data.award(player.getUUID(), receipt, gang.toString(), Math.max(0, Math.min(60, supplies)),
                Math.max(0, Math.min(300, xp)), Math.max(0, Math.min(180, training)), Math.max(0, Math.min(40, reputation)), source, now(player));
        if (awarded == null) return ActivityReward.NONE;
        publishReward(player, awarded);
        return new ActivityReward(true, awarded.supplies(), awarded.crewXp(), awarded.memberXp(), awarded.reputation(), awarded.pending());
    }

    /** Presentation follows the committed result. A disconnected client cannot undo or repeat an award. */
    private static void publishReward(ServerPlayer player, CrewRewardReceipt receipt) {
        try {
            CompoundTag notification = receipt.snapshot();
            long xp = CrewHouseData.get(player.getServer()).styleXp(player.getUUID());
            ListTag learned = new ListTag();
            for (CrewStyleLibrary.Lesson lesson : CrewStyleLibrary.LESSONS) {
                if (lesson.requiredXp() > xp - receipt.crewXp() && lesson.requiredXp() <= xp) {
                    CompoundTag row = new CompoundTag(); row.putString("Name", lesson.upgrade().label()); learned.add(row);
                }
            }
            notification.put("StylesLearned", learned);
            JetSetNetwork.syncCrewReward(player, notification);
            CompoundTag state = snapshot(player); state.putBoolean("Open", false);
            JetSetNetwork.syncCrew(player, state);
        } catch (RuntimeException error) {
            JetSetCraft.LOGGER.warn("Crew reward {} was saved but its notification could not be sent to {}", receipt.id(), player.getUUID(), error);
        }
    }

    public static CompoundTag snapshot(ServerPlayer player) {
        CompoundTag out = new CompoundTag(); out.putBoolean("Open", true); out.putString("Message", "");
        boolean inRush = RivalRushManager.active(player) || RushWorld.isRush(player.level()); out.putBoolean("InRush", inRush);
        CrewHouseData data = CrewHouseData.get(player.getServer());
        CrewProfile profile = data.profile(player.getUUID());
        Candidate candidate = candidate(player);
        out.putBoolean("HasCandidate", candidate != null);
        out.putString("CandidateLabel", candidate == null ? "Use the Crew Charter on a placed Boombox or facility." : positionLabel(candidate.dimension, candidate.position));
        out.putBoolean("Exists", profile != null);
        out.putInt("PendingSupplies", data.pendingSupplies(player.getUUID()));
        out.putLong("PendingCrewXp", data.pendingXp(player.getUUID()));
        out.putLong("PendingTraining", data.pendingTraining(player.getUUID()));
        out.put("StyleLibrary", CrewStyleLibrary.snapshot(data, player.getUUID(), normalModeReason(player).isEmpty()));
        String founding = foundingReason(player, data, profile);
        out.putBoolean("CanFound", profile == null && founding.isEmpty());
        out.putBoolean("CanRelocate", profile != null && founding.isEmpty()); out.putString("FoundReason", founding);
        String manage = profile == null ? "Found your home at a placed Boombox first." : managementReason(player, profile);
        if (data.blocked(player.getUUID())) manage = "Saved crew data was retained for recovery; management is unavailable.";
        out.putBoolean("CanManage", manage.isEmpty()); out.putString("ManageReason", manage);
        if (profile == null) { CrewProgressView.write(out, data, player.getUUID()); return out; }
        boolean canManage = manage.isEmpty();
        out.putUUID("HomeId", profile.id); out.putLong("Revision", profile.revision); out.putString("Name", profile.name);
        out.putString("GangId", profile.gang); out.putString("GangName", gangName(profile.gang));
        out.putString("HomeLabel", positionLabel(profile.dimension, profile.anchor));
        out.putString("HomeDimension", profile.dimension); out.putLong("HomeAnchor", profile.anchor.asLong());
        ServerLevel home = homeLevel(player.getServer(), profile);
        boolean anchorLoaded = home != null && home.hasChunkAt(profile.anchor);
        out.putBoolean("AnchorLoaded", anchorLoaded);
        out.putBoolean("AnchorValid", anchorLoaded && home.getBlockState(profile.anchor).is(ModBlocks.BOOMBOX.get()));
        out.putInt("Tier", profile.tier); out.putInt("Radius", profile.radius()); out.putInt("Capacity", profile.capacity());
        out.putLong("CrewXp", profile.xp); int crewLevel = CrewProfile.crewLevel(profile.xp);
        out.putInt("CrewLevel", crewLevel); out.putLong("CrewNextXp", crewLevel >= 20 ? 0 : CrewProfile.crewThreshold(crewLevel + 1));
        out.putInt("Supplies", profile.supplies); int reputation = profile.reputation(profile.gang);
        out.putInt("RecruitDiscount", recruitmentDiscount(profile));
        out.putInt("Reputation", reputation); out.putString("ReputationTier", reputation >= 1000 ? "Family" : reputation >= 400 ? "Veteran" : reputation >= 150 ? "Trusted" : reputation >= 50 ? "Friendly" : "New neighbors");
        out.putInt("ExpandCost", EXPANSION_COSTS[profile.tier]); String expansion = expansionReason(player, profile);
        out.putBoolean("CanExpand", canManage && expansion.isEmpty()); out.putString("ExpandReason", canManage ? expansion : manage);
        out.putString("SelectedPreparation", profile.preparation); if (profile.focus != null) out.putUUID("FocusId", profile.focus);
        out.putInt("MissionSlots", profile.missionSlots()); out.putInt("ActiveJobs", profile.jobs.size());
        long now = now(player);
        ListTag roster = new ListTag();
        for (CrewProfile.Member member : profile.members.values()) {
            CompoundTag row = new CompoundTag(); row.putUUID("Id", member.id); row.putString("Name", member.name);
            row.putString("EntityId", member.species); row.putString("SpeciesName", speciesName(member.species));
            row.putString("Role", member.role.id); row.putString("RoleName", member.role.label);
            row.putInt("Level", member.level()); row.putLong("Xp", member.xp);
            row.putLong("NextXp", member.level() >= 10 ? 0 : CrewProfile.memberThreshold(member.level() + 1));
            CrewProfile.Job job = profile.jobs.values().stream().filter(j -> j.memberId().equals(member.id)).findFirst().orElse(null);
            boolean idle = job == null;
            row.putString("Status", idle ? "Idle roster member" : job.ready(now) ? "Mission ready to claim" : "Away on assignment");
            row.putBoolean("Idle", idle); row.putBoolean("CanAssign", canManage && idle && profile.jobs.size() < profile.missionSlots());
            row.putBoolean("Training", member.id.equals(profile.focus)); row.putBoolean("CanTrain", canManage && idle); roster.add(row);
        }
        out.put("Members", roster);
        ListTag facilities = new ListTag(); ListTag preparations = new ListTag();
        CompoundTag none = new CompoundTag(); none.putString("Id", "NONE"); none.putString("Name", "No house preparation");
        none.putBoolean("Available", canManage); none.putString("Reason", canManage ? "Rival Rush remains fully playable without a house bonus." : manage); preparations.add(none);
        for (Kind kind : Kind.values()) {
            CrewProfile.Facility room = profile.facilities.get(kind.id); int level = room == null ? 0 : room.level;
            String physical = room == null || room.position == null ? "Use the Crew Charter on a placed " + kind.anchorName.toLowerCase() + "." : physicalReason(player.getServer(), profile, kind, room.position, level);
            boolean operational = level > 0 && physical.isEmpty();
            int cost = level == 0 ? 8 : level >= 3 ? 0 : level * 16;
            String registerReason = candidate == null ? "Select the physical facility with the Crew Charter." : physicalReason(player.getServer(), profile, kind, candidate.position, Math.max(1, level));
            boolean candidateMatchesDimension = candidate != null && profile.dimension.equals(candidate.dimension);
            String upgradeReason = level == 0 || room.position == null ? "Register this physical facility first." : level >= 3 ? "Fully upgraded." : physicalReason(player.getServer(), profile, kind, room.position, level + 1);
            CompoundTag row = new CompoundTag(); row.putString("Id", kind.id); row.putString("Name", kind.label); row.putInt("Level", level);
            row.putBoolean("Registered", room != null && room.position != null); row.putBoolean("Operational", operational);
            row.putString("Status", operational ? "Operational" : physical); row.putString("Requires", kind.requirements()); row.putInt("Cost", cost);
            row.putBoolean("CanRegister", canManage && candidateMatchesDimension && registerReason.isEmpty() && profile.supplies >= (level == 0 ? 8 : 0));
            row.putBoolean("CanUpgrade", canManage && level > 0 && level < 3 && upgradeReason.isEmpty() && profile.supplies >= cost);
            row.putString("Reason", !canManage ? manage : profile.supplies < cost ? "Needs " + cost + " Crew Supplies." : level == 0 ? registerReason : upgradeReason);
            row.putString("Perk", kind.perk.name()); row.putString("PerkName", kind.perk.label()); facilities.add(row);
            CompoundTag prep = new CompoundTag(); prep.putString("Id", kind.perk.name()); prep.putString("Name", kind.perk.label());
            prep.putBoolean("Available", canManage && operational); prep.putString("Reason", !canManage ? manage : operational ? "One mode-only starting rank from this home." : physical); preparations.add(prep);
        }
        out.put("Facilities", facilities); out.put("Preparations", preparations);
        ListTag missions = new ListTag();
        for (CrewMissionCatalog.Mission mission : CrewMissionCatalog.ALL) {
            CompoundTag row = new CompoundTag(); row.putString("Id", mission.id()); row.putString("Name", mission.name()); row.putString("RoleName", mission.role().label);
            row.putLong("DurationTicks", mission.durationTicks()); row.putInt("RewardSupplies", mission.supplies()); row.putInt("MemberXp", mission.memberXp());
            row.putInt("CrewXp", mission.crewXp()); row.putInt("Reputation", mission.reputation());
            boolean slot = profile.jobs.size() < profile.missionSlots(); row.putBoolean("CanAssign", canManage && slot && profile.members.keySet().stream().anyMatch(m -> !profile.busy(m)));
            row.putString("Reason", !canManage ? manage : !slot ? "All mission slots are occupied; claim or cancel an assignment." : "Role, level and operational facilities can increase supplies at dispatch."); missions.add(row);
        }
        out.put("Missions", missions);
        ListTag jobs = new ListTag();
        for (CrewProfile.Job job : profile.jobs.values()) {
            CompoundTag row = new CompoundTag(); row.putUUID("Id", job.id()); row.putUUID("MemberId", job.memberId());
            row.putString("MemberName", profile.members.get(job.memberId()).name); row.putString("MissionId", job.missionId());
            row.putString("Name", CrewMissionCatalog.byId(job.missionId()).map(CrewMissionCatalog.Mission::name).orElse("Assignment"));
            row.putBoolean("Ready", job.ready(now)); row.putLong("RemainingTicks", Math.max(0, job.readyAt() - now));
            row.putInt("Supplies", job.supplies()); row.putInt("MemberXp", job.memberXp()); row.putInt("CrewXp", job.crewXp()); row.putInt("Reputation", job.reputation());
            row.putBoolean("CanClaim", canManage && job.ready(now)); row.putBoolean("CanCancel", canManage); jobs.add(row);
        }
        out.put("Jobs", jobs);
        ListTag recruits = new ListTag(); int recruitCost = recruitmentCost(profile);
        for (String entity : recruitPool(player, profile)) {
            CompoundTag row = new CompoundTag(); row.putString("Id", entity); row.putString("Name", speciesName(entity)); row.putInt("Cost", recruitCost);
            boolean room = profile.members.size() < profile.capacity(); row.putBoolean("CanRecruit", canManage && room && profile.supplies >= recruitCost);
            row.putString("Reason", !canManage ? manage : !room ? "Expand your plot for more member capacity." : profile.supplies < recruitCost ? "Needs " + recruitCost + " Crew Supplies." : "A mission roster profile; no world mob is captured or replaced."); recruits.add(row);
        }
        out.put("Recruits", recruits);
        CrewProgressView.write(out, data, player.getUUID());
        return out;
    }

    private static String normalModeReason(ServerPlayer player) {
        if (!JetSetConfig.SERVER.enableCrewHouse.get()) return "Crew homes are disabled by this server; saved progression is retained.";
        if (RivalRushManager.active(player) || RushWorld.isRush(player.level())) return "Return to Minecraft before managing your home.";
        if (ActivityManager.active(player)) return "Finish or cancel your street activity before managing your home.";
        if (!player.isAlive() || player.isSpectator()) return "Manage your crew while alive and outside Spectator mode.";
        return "";
    }
    private static String managementReason(ServerPlayer player, CrewProfile profile) {
        String normal = normalModeReason(player); if (!normal.isEmpty()) return normal;
        String anchor = homeReason(player.getServer(), profile); if (!anchor.isEmpty()) return anchor;
        if (!player.serverLevel().dimension().location().toString().equals(profile.dimension) || !profile.contains(player.blockPosition()))
            return "Visit your home plot to manage members, jobs and facilities.";
        return "";
    }
    private static String homeReason(MinecraftServer server, CrewProfile profile) {
        ServerLevel level = homeLevel(server, profile);
        if (level == null) return "Your home dimension is unavailable. The crew is retained; relocate to an available Boombox.";
        if (!level.hasChunkAt(profile.anchor)) return "Your home is unloaded. Visit it to check its facilities; no chunks are force-loaded.";
        if (!level.getBlockState(profile.anchor).is(ModBlocks.BOOMBOX.get())) return "The home Boombox is missing. Replace it here or relocate; your crew and jobs are retained.";
        return "";
    }
    private static String foundingReason(ServerPlayer player, CrewHouseData data, CrewProfile existing) {
        String normal = normalModeReason(player); if (!normal.isEmpty()) return normal;
        if (data.blocked(player.getUUID())) return "Existing saved crew data needs recovery; it will not be overwritten.";
        Candidate candidate = candidate(player);
        if (candidate == null || !candidate.dimension.equals(player.serverLevel().dimension().location().toString())
                || player.distanceToSqr(candidate.position.getX() + .5, candidate.position.getY() + .5, candidate.position.getZ() + .5) > 64)
            return "Use the Crew Charter on a nearby placed Boombox first.";
        ServerLevel level = player.serverLevel();
        if (!level.hasChunkAt(candidate.position) || !level.getBlockState(candidate.position).is(ModBlocks.BOOMBOX.get())
                || !(level.getBlockEntity(candidate.position) instanceof BoomboxBlockEntity boombox)) return "The selected block must be a placed Boombox.";
        if (boombox.isChallengeActive() || ActivityManager.anchorActive(level, candidate.position))
            return "Finish the Boombox challenge before using this home anchor.";
        return plotReason(level, candidate.position, existing == null ? 8 : existing.radius(), data, existing);
    }
    private static String expansionReason(ServerPlayer player, CrewProfile profile) {
        if (profile.tier >= 3) return "This plot is fully expanded.";
        if (profile.supplies < EXPANSION_COSTS[profile.tier]) return "Needs " + EXPANSION_COSTS[profile.tier] + " Crew Supplies.";
        ServerLevel level = homeLevel(player.getServer(), profile);
        if (level == null) return "The home dimension is unavailable.";
        return plotReason(level, profile.anchor, CrewProfile.RADII[profile.tier + 1], CrewHouseData.get(player.getServer()), profile);
    }
    private static String plotReason(ServerLevel level, BlockPos anchor, int radius, CrewHouseData data, CrewProfile self) {
        if (anchor.getY() < level.getMinBuildHeight() || anchor.getY() >= level.getMaxBuildHeight()) return "Choose an anchor inside the world's build height.";
        for (int dx : new int[]{-radius, radius}) for (int dz : new int[]{-radius, radius})
            if (!level.getWorldBorder().isWithinBounds(anchor.offset(dx, 0, dz))) return "That plot would extend beyond the world border.";
        String dimension = level.dimension().location().toString();
        for (CrewProfile other : data.profiles()) {
            if (other == self || !other.dimension.equals(dimension)) continue;
            if (Math.abs((long) other.anchor.getX() - anchor.getX()) <= other.radius() + radius
                    && Math.abs((long) other.anchor.getZ() - anchor.getZ()) <= other.radius() + radius)
                return "That footprint overlaps another registered crew plot. Choose a different location or expansion space.";
        }
        return "";
    }
    private static String physicalReason(MinecraftServer server, CrewProfile profile, Kind kind, BlockPos position, int level) {
        String home = homeReason(server, profile); if (!home.isEmpty()) return home;
        if (position == null || !profile.contains(position)) return "Place the facility inside your registered home plot.";
        ServerLevel world = homeLevel(server, profile);
        if (!world.hasChunkAt(position)) return "The facility is unloaded; visit it before registering or using it.";
        if (!kind.anchor(world.getBlockState(position))) return "Needs an actual placed " + kind.anchorName.toLowerCase() + ".";
        for (int tier = 2; tier <= level; tier++) {
            int required = tier;
            if (!nearby(world, profile, position, state -> kind.support(state, required)))
                return "Tier " + tier + " needs a placed " + (tier == 2 ? kind.second : kind.third) + " within two blocks, inside the loaded home plot.";
        }
        return "";
    }
    private static boolean nearby(ServerLevel level, CrewProfile profile, BlockPos origin, Predicate<BlockState> match) {
        for (BlockPos pos : BlockPos.betweenClosed(origin.offset(-2, -2, -2), origin.offset(2, 2, 2))) {
            if (profile.contains(pos) && level.hasChunkAt(pos) && match.test(level.getBlockState(pos))) return true;
        }
        return false;
    }
    private static int operationalLevel(MinecraftServer server, CrewProfile profile, Kind kind) {
        CrewProfile.Facility room = profile.facilities.get(kind.id);
        return room != null && room.position != null && room.level > 0
                && physicalReason(server, profile, kind, room.position, room.level).isEmpty() ? room.level : 0;
    }
    private static int missionBonus(MinecraftServer server, CrewProfile profile) {
        int bonus = 0;
        for (Kind kind : Kind.values()) bonus += Math.max(0, operationalLevel(server, profile, kind) - 1) * 5;
        return Math.min(30, bonus);
    }
    private static int recruitmentDiscount(CrewProfile profile) {
        int reputation = profile.reputation(profile.gang);
        return reputation >= 1000 ? 8 : reputation >= 400 ? 6 : reputation >= 150 ? 4 : reputation >= 50 ? 2 : 0;
    }
    private static int recruitmentCost(CrewProfile profile) { return Math.max(20, 20 + profile.members.size() * 5 - recruitmentDiscount(profile)); }
    private static LinkedHashSet<String> recruitPool(ServerPlayer player, CrewProfile profile) {
        LinkedHashSet<String> result = new LinkedHashSet<>();
        String representative = representative(profile.gang); if (availableSpecies(representative)) result.add(representative);
        for (int offset = 0; offset < 400 && result.size() < 64; offset += 100) {
            List<RushAlbum.Entry> page = RushAlbum.entries(player, offset, 100);
            for (RushAlbum.Entry card : page) { if (result.size() < 64 && availableSpecies(card.entityId())) result.add(card.entityId()); }
            if (page.size() < 100) break;
        }
        return result;
    }
    private static boolean availableSpecies(String value) {
        ResourceLocation key = id(value);
        if (key == null || !ForgeRegistries.ENTITY_TYPES.containsKey(key)) return false;
        EntityType<?> type = ForgeRegistries.ENTITY_TYPES.getValue(key);
        return type != null && type.getCategory() != MobCategory.MISC;
    }
    private static String representative(String gang) {
        return GangRegistry.curatedEntityIds().stream().filter(entity -> GangRegistry.gangIdForEntity(entity).toString().equals(gang))
                .map(ResourceLocation::toString).filter(CrewHouseManager::availableSpecies).sorted().findFirst().orElse("minecraft:villager");
    }
    private static String speciesName(String entity) {
        EntityType<?> type = id(entity) == null ? null : ForgeRegistries.ENTITY_TYPES.getValue(id(entity));
        return type == null ? "Unavailable creature (profile retained)" : type.getDescription().getString();
    }
    private static String gangName(String gang) { return GangRegistry.byId(id(gang)).map(d -> d.canonicalName()).orElse("Unavailable crew (reputation retained)"); }
    private static ServerLevel homeLevel(MinecraftServer server, CrewProfile profile) { return server.getLevel(ResourceKey.create(Registries.DIMENSION, id(profile.dimension))); }
    private static String positionLabel(String dimension, BlockPos pos) { return dimension + " · " + pos.getX() + ", " + pos.getY() + ", " + pos.getZ(); }
    private static Candidate candidate(ServerPlayer player) {
        Candidate result = CANDIDATES.getOrDefault(player.getServer(), Map.of()).get(player.getUUID());
        return result != null && result.expires >= now(player) ? result : null;
    }
    private static long now(ServerPlayer player) { return player.getServer().overworld().getGameTime(); }
    private static ResourceLocation id(String value) { return ResourceLocation.tryParse(value); }
    private static void send(ServerPlayer player, String message) {
        if (player == null || !JetSetNetwork.canSync(player)) return;
        // Bound read/rejected-action responses before performing the expensive world/album snapshot. Successful
        // transactions change revision, and selecting a different physical block changes context; both respond
        // immediately. Domain mutations and in-memory GameTests never depend on this presentation budget.
        CrewHouseData data = CrewHouseData.get(player.getServer());
        CrewProfile profile = data.profile(player.getUUID());
        UUID house = profile == null ? null : profile.id;
        long revision = profile == null ? 0 : profile.revision;
        long styleRevision = data.styleRevision(player.getUUID());
        Candidate candidate = candidate(player);
        String dimension = candidate == null ? "" : candidate.dimension;
        BlockPos position = candidate == null ? null : candidate.position;
        ResponseBudget budget = responseBudget(player);
        boolean unchanged = java.util.Objects.equals(house, budget.house) && revision == budget.revision
                && styleRevision == budget.styleRevision
                && dimension.equals(budget.candidateDimension) && java.util.Objects.equals(position, budget.candidatePosition);
        if (unchanged && now(player) < budget.responseAt + 4) return;
        budget.responseAt = now(player); budget.house = house; budget.revision = revision; budget.styleRevision = styleRevision;
        budget.candidateDimension = dimension; budget.candidatePosition = position;
        CompoundTag state = snapshot(player); state.putString("Message", message); JetSetNetwork.syncCrew(player, state);
    }
    private static ResponseBudget responseBudget(ServerPlayer player) {
        return RESPONSES.computeIfAbsent(player.getServer(), ignored -> new HashMap<>())
                .computeIfAbsent(player.getUUID(), ignored -> new ResponseBudget());
    }
    private static void preview(ServerPlayer player, CrewProfile profile) {
        ServerLevel level = player.serverLevel(); int r = profile.radius();
        double y = profile.anchor.getY() + 1.25;
        for (int offset = -r; offset <= r; offset += 4) {
            for (int sign : new int[]{-1, 1}) {
                level.sendParticles(ParticleTypes.END_ROD, profile.anchor.getX() + offset + .5, y, profile.anchor.getZ() + sign * r + .5, 1, 0, 0, 0, 0);
                level.sendParticles(ParticleTypes.END_ROD, profile.anchor.getX() + sign * r + .5, y, profile.anchor.getZ() + offset + .5, 1, 0, 0, 0, 0);
            }
        }
    }
    public static void forget(ServerPlayer player) {
        Map<UUID, Candidate> map = CANDIDATES.get(player.getServer()); if (map != null) map.remove(player.getUUID());
        Map<UUID, ResponseBudget> responses = RESPONSES.get(player.getServer()); if (responses != null) responses.remove(player.getUUID());
    }
    public static void stop(MinecraftServer server) { CANDIDATES.remove(server); RESPONSES.remove(server); }
    private CrewHouseManager() {}
}
