package com.herberto.jetsetcraft.rush;

import com.herberto.jetsetcraft.JetSetCraft;
import com.herberto.jetsetcraft.activity.ActivityManager;
import com.herberto.jetsetcraft.compat.RushSpellCompat;
import com.herberto.jetsetcraft.config.JetSetConfig;
import com.herberto.jetsetcraft.crew.CrewHouseManager;
import com.herberto.jetsetcraft.data.JetSetData;
import com.herberto.jetsetcraft.data.JetSetDataProvider;
import com.herberto.jetsetcraft.entity.PaintBalloonEntity;
import com.herberto.jetsetcraft.gang.GangDefinition;
import com.herberto.jetsetcraft.gang.GangGearSelector;
import com.herberto.jetsetcraft.gang.GangMemberState;
import com.herberto.jetsetcraft.gang.GangRegistry;
import com.herberto.jetsetcraft.graffiti.PaintColor;
import com.herberto.jetsetcraft.graffiti.PaintSplash;
import com.herberto.jetsetcraft.item.RideGearItem;
import com.herberto.jetsetcraft.item.SprayCanItem;
import com.herberto.jetsetcraft.mob.MobStreetGear;
import com.herberto.jetsetcraft.mob.StreetGearAcquisition;
import com.herberto.jetsetcraft.network.JetSetNetwork;
import com.herberto.jetsetcraft.registry.ModBlocks;
import com.herberto.jetsetcraft.registry.ModItems;
import com.herberto.jetsetcraft.registry.ModSounds;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.Difficulty;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.GameType;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.event.ForgeEventFactory;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.WeakHashMap;

/** Server-owned opt-in sessions. Ordinary Minecraft actors, XP and progression never enter this simulation. */
public final class RivalRushManager {
    public static final String OWNED = "jetsetcraft:rush_actor";
    private static final Map<MinecraftServer, Map<UUID, Session>> SERVERS = new WeakHashMap<>();
    private static final Map<UUID, Long> LAST_MENU = new HashMap<>();
    private static final Map<UUID, Long> LAST_START = new HashMap<>();
    private static final int PAGE_SIZE = 20;

    public enum Role { SKATER, TAGGER, BRUISER, CAPTAIN }

    private static final class Actor {
        Mob mob;
        final Role role;
        final ResourceLocation species;
        double vitality;
        final double maximum;
        long lastAttack;
        long paintDue;
        long staggerUntil;
        long exposedUntil;
        long venomUntil, venomDue, chillUntil;
        int venomRank, chillRank;
        double exposureMultiplier = 1;
        Vec3 paintMark;
        boolean claimed;

        Actor(Mob mob, Role role, ResourceLocation species, double vitality) {
            this.mob = mob; this.role = role; this.species = species;
            this.vitality = vitality; this.maximum = vitality;
        }
    }

    private static final class Pickup {
        ItemEntity entity;
        final ResourceLocation species;
        int count = 1;
        long lastAddedActiveTick;
        Pickup(ItemEntity entity, ResourceLocation species, long activeTick) {
            this.entity = entity; this.species = species; this.lastAddedActiveTick = activeTick;
        }
    }

    private static final class Session {
        final ServerPlayer player;
        final RushRun run;
        final GangDefinition gang;
        final List<ResourceLocation> roster;
        final Map<UUID, Actor> actors = new LinkedHashMap<>();
        final Map<UUID, Pickup> pickups = new LinkedHashMap<>();
        final Map<UUID, Projectile> projectiles = new LinkedHashMap<>();
        final Map<String, Long> walls = new HashMap<>();
        final Map<Long, Long> musicStops = new HashMap<>();
        final RushPointsOfInterest points = new RushPointsOfInterest();
        final RushMotionTracker motion = new RushMotionTracker();
        RushCircuit circuit;
        long lastSpawn, lastShot, lastHurt, lastCaptain = -1, lastStyle;
        long lastHorn, lastSoul, lastShockwave, lastSpell;
        long lastAirBreak = -100, lastExpose = -140;
        long airBreakPulses, lineProcs, markApplications;
        long lastSoulBurst = -100;
        long multishotVolleys, radiantPulses, soulBursts;
        long cards;
        int sequence, announcedOffer, announcedJourney, musicCharge;
        boolean exiting, finaleSent;
        String objective = "Ride, tag rivals, collect cards. Find a lit Boombox or a new wall.";
        BlockPos musicStop;

        Session(ServerPlayer player, RushRun run, GangDefinition gang, List<ResourceLocation> roster) {
            this.player = player; this.run = run; this.gang = gang; this.roster = List.copyOf(roster);
        }
    }

    private static Map<UUID, Session> sessions(MinecraftServer server) {
        return SERVERS.computeIfAbsent(server, key -> new HashMap<>());
    }

    private static Session session(Player player) {
        if (!(player instanceof ServerPlayer server) || server.getServer() == null) return null;
        return sessions(server.getServer()).get(player.getUUID());
    }

    public static boolean active(Player player) { return session(player) != null; }
    public static RushRun run(ServerPlayer player) { Session s = session(player); return s == null ? null : s.run; }
    public static CompoundTag buildSnapshot(ServerPlayer player) {
        Session s = session(player); return s == null ? new CompoundTag() : buildSnapshot(s);
    }
    public static CompoundTag journeySnapshot(ServerPlayer player) {
        Session s = session(player); return s == null || s.circuit == null ? new CompoundTag() : s.circuit.snapshot(s.run, player.position());
    }
    public static CompoundTag cacheSnapshot(ServerPlayer player) {
        Session s = session(player); return s == null ? new CompoundTag() : s.points.snapshot(player, s.run, s.circuit);
    }
    public static BlockPos arenaDestination(ServerPlayer player) {
        Session s = session(player); return s == null || s.circuit == null ? null : s.circuit.center();
    }
    public static boolean owned(Entity entity) { return entity != null && entity.getPersistentData().contains(OWNED); }

    public static boolean belongsTo(Entity entity, ServerPlayer player) {
        Session s = session(player);
        if (s == null || !owned(entity)) return false;
        CompoundTag tag = entity.getPersistentData().getCompound(OWNED);
        return tag.hasUUID("Run") && tag.getUUID("Run").equals(s.run.id())
                && tag.hasUUID("Owner") && tag.getUUID("Owner").equals(player.getUUID());
    }

    private static void mark(Entity entity, Session s) {
        CompoundTag tag = new CompoundTag();
        tag.putUUID("Run", s.run.id()); tag.putUUID("Owner", s.player.getUUID());
        entity.getPersistentData().put(OWNED, tag);
        if (entity instanceof Projectile projectile) s.projectiles.put(entity.getUUID(), projectile);
    }

    /** Called by the actual packet/command boundary, never accepts score, XP, health or arbitrary entity NBT. */
    public static void handleAction(ServerPlayer player, String action, String value, int number, UUID runId, int version) {
        Session s = session(player);
        if (action == null || value == null || value.length() > 256) return;
        switch (action) {
            case "leave" -> { if (s != null && (runId == null || s.run.id().equals(runId))) leave(player, "Session banked. Your collected cards stay in the Rush album.");
                              else recover(player); }
            case "choose" -> {
                if (s != null && s.run.id().equals(runId) && number >= 0 && number < s.run.offers().size()
                        && (value.isEmpty() || s.run.offers().get(number).name().equalsIgnoreCase(value))
                        && s.run.choose(number, version)) send(player, true, "", -1, -1);
            }
            case "remix" -> {
                if (s == null || !s.run.id().equals(runId) || !betweenArenas(s) || s.run.hasOffer()
                        || version != s.run.offerVersion()) return;
                String[] selection = value.split("\\|", -1);
                if (selection.length != 2) return;
                RushRun.Upgrade from, to;
                try { from = RushRun.Upgrade.valueOf(selection[0]); to = RushRun.Upgrade.valueOf(selection[1]); }
                catch (IllegalArgumentException ignored) { return; }
                if (s.run.remix(from, to, version)) {
                    // Effects belong to the equipped family; a replacement cannot retain its old damage/control.
                    for (Actor actor : s.actors.values()) {
                        if (from == RushRun.Upgrade.VENOM) { actor.venomUntil = 0; actor.venomRank = 0; }
                        if (from == RushRun.Upgrade.CHILL) { actor.chillUntil = 0; actor.chillRank = 0; }
                        updateName(s, actor);
                    }
                    s.motion.reset(player.position());
                    send(player, true, "Remixed " + from.label() + " into " + to.label()
                            + " at rank " + s.run.rank(to) + ". Your other upgrades remain in place.", -1, -1);
                    player.serverLevel().playSound(null, player.blockPosition(), SoundEvents.NOTE_BLOCK_CHIME.value(), SoundSource.PLAYERS, .45f, 1.2f);
                }
            }
            case "encore" -> {
                if (s != null && s.run.id().equals(runId) && s.run.offerVersion() == version && s.run.continueEndless()) {
                    if (s.circuit != null && s.circuit.encore()) {
                        s.points.reset();
                        BlockPos origin = s.circuit.origin();
                        player.teleportTo(player.serverLevel(), origin.getX() + 0.5, RushWorld.FLOOR_Y + 1,
                                origin.getZ() + 0.5, 0, 0);
                        player.setDeltaMovement(Vec3.ZERO);
                    }
                    s.finaleSent = false; send(player, true, "", -1, -1);
                }
            }
            case "journey_choose" -> {
                if (s != null && s.circuit != null && s.run.id().equals(runId) && !s.run.hasOffer()
                        && s.circuit.choose(ResourceLocation.tryParse(value), version)) {
                    s.points.reset(); s.objective = s.circuit.objective(s.run);
                    send(player, false, "", -1, -1);
                }
            }
            case "start" -> {
                if (s != null || number < 0 || number > 3) return;
                String[] selection = value.split("\\|", -1);
                if (selection.length < 2 || selection.length > 3) return;
                ResourceLocation profile = selection.length == 3 && !selection[2].isEmpty() ? ResourceLocation.tryParse(selection[2]) : null;
                if (selection.length == 3 && !selection[2].isEmpty() && profile == null) return;
                if (number >= 2) startCircuit(player, ResourceLocation.tryParse(selection[0]), ResourceLocation.tryParse(selection[1]), number == 3, profile);
                else start(player, ResourceLocation.tryParse(selection[0]), ResourceLocation.tryParse(selection[1]), number == 1);
            }
            case "menu", "album", "roster", "arena_catalogue" -> {
                long now = player.serverLevel().getGameTime();
                if (now - LAST_MENU.getOrDefault(player.getUUID(), now - 10) < 4) return;
                LAST_MENU.put(player.getUUID(), now);
                send(player, true, "", action.equals("album") ? Math.max(0, number) : -1,
                        action.equals("roster") ? Math.max(0, number) : -1);
            }
            default -> { }
        }
    }

    public static boolean start(ServerPlayer player, ResourceLocation gangId, ResourceLocation species, boolean endless) {
        return startInternal(player, gangId, species, endless, false, null);
    }

    public static boolean startCircuit(ServerPlayer player, ResourceLocation gangId, ResourceLocation species,
                                       boolean endless, ResourceLocation arenaProfile) {
        return startInternal(player, gangId, species, endless, true, arenaProfile);
    }

    private static boolean startInternal(ServerPlayer player, ResourceLocation gangId, ResourceLocation species,
                                         boolean endless, boolean circuit, ResourceLocation arenaProfile) {
        long now = player.getServer().overworld().getGameTime();
        if (now - LAST_START.getOrDefault(player.getUUID(), now - 20) < 20) return false;
        LAST_START.put(player.getUUID(), now);
        if (!JetSetConfig.SERVER.enableRivalRush.get()) return fail(player, "Rival Rush is disabled by this server.");
        if (active(player)) return fail(player, "Finish or leave your current Rush first.");
        if (ActivityManager.active(player)) return fail(player, "Finish or cancel your street activity before starting Rival Rush.");
        RushArenaCatalog.Profile firstArena = arenaProfile == null ? null : RushArenaCatalog.find(arenaProfile).orElse(null);
        if (arenaProfile != null && firstArena == null) return fail(player, "That arena is unavailable. Refresh the arena catalogue.");
        int slot = circuit ? freeCircuitSlot(player.getServer()) : -1;
        if (circuit && slot < 0) return fail(player, "All arena circuits are occupied. Try again after another rider returns.");
        if (RushReturnState.hasJournal(player)) { recover(player); return fail(player, "Recovered your previous session. Open Rival Rush again."); }
        if (player.serverLevel().getDifficulty() == Difficulty.PEACEFUL)
            return fail(player, "This Rival Rush alpha requires Easy, Normal or Hard because Peaceful removes hostile rivals. Your world difficulty is unchanged.");
        if (!player.isAlive() || player.isPassenger() || player.isSleeping())
            return fail(player, "Stand safely on your own before starting Rival Rush.");
        if (!player.containerMenu.getCarried().isEmpty())
            return fail(player, "Put the item on your cursor away before entering Rival Rush.");
        if (RushWorld.isRush(player.level())) return fail(player, "Use Return to Minecraft before beginning another run.");
        if (gangId == null || species == null) return fail(player, "Choose a valid crew and mob registry ID.");
        GangDefinition gang = GangRegistry.byId(gangId).orElse(null);
        if (gang == null) return fail(player, "Unknown crew. Select a crew from the roster.");
        ServerLevel district = player.getServer().getLevel(RushWorld.DIMENSION);
        if (district == null) return fail(player, "The Rival Rush dimension is unavailable. Reopen the world with its bundled datapack enabled.");
        EntityType<?> type = ForgeRegistries.ENTITY_TYPES.getValue(species);
        Entity probe;
        try { probe = type == null ? null : type.create(district); }
        catch (RuntimeException error) { return fail(player, "This mob provider cannot create an event actor: " + species); }
        if (!(probe instanceof Mob mob)) {
            if (probe != null) probe.discard();
            return fail(player, "Choose an installed AI mob; that registry entry is not an available Mob.");
        }
        boolean compatible = MobStreetGear.eligible(mob);
        mob.discard();
        if (!compatible) return fail(player, "This mob's Street Gear provider explicitly excludes it. Choose another mob or update its compatibility datapack.");
        List<ResourceLocation> roster = new ArrayList<>();
        for (ResourceLocation candidate : RushRosters.forGang(gangId, species)) {
            Entity check = null;
            try {
                EntityType<?> candidateType = ForgeRegistries.ENTITY_TYPES.getValue(candidate);
                check = candidateType == null ? null : candidateType.create(district);
                if (check instanceof Mob candidateMob && MobStreetGear.eligible(candidateMob)) roster.add(candidate);
            } catch (RuntimeException error) {
                JetSetCraft.LOGGER.warn("Rival Rush roster {} cannot create {}: {}", gangId, candidate, error.getMessage());
            } finally { if (check != null) check.discard(); }
        }
        if (roster.isEmpty()) return fail(player, "This crew's roster has no installed, Street Gear-compatible AI mobs.");
        Optional<RushRun.Upgrade> preparation = Optional.empty();
        try { preparation = CrewHouseManager.rushPreparation(player); }
        catch (RuntimeException error) {
            JetSetCraft.LOGGER.error("Could not read optional house preparation for {}; starting without it", player.getUUID(), error);
        }
        if (!RushReturnState.capture(player)) return fail(player, "Could not safely preserve your return state; no run was started.");
        if (!RushPlayerStorage.flush(player)) {
            // No loan or teleport has happened. Keep the ordinary player state and let logout retry its save.
            player.getPersistentData().getCompound(Player.PERSISTED_NBT_TAG).remove(RushReturnState.KEY);
            player.connection.disconnect(Component.literal(
                    "Rival Rush could not save your return checkpoint. Your Minecraft belongings were not changed. Reconnect after the server can save player data."));
            return false;
        }
        RushRun run = new RushRun(UUID.randomUUID(), player.getUUID(), district.getRandom().nextLong(), endless, gangId.toString());
        run.enableSpellRelay(RushSpellCompat.fireboltAvailable());
        if (circuit) run.configureCircuit();
        preparation.ifPresent(run::grantStartingPerk);
        Session s = new Session(player, run, gang, roster);
        if (circuit) {
            BlockPos origin = new BlockPos(2_000_000 + (slot % 32) * 4096, RushWorld.FLOOR_Y, (slot / 32) * 4096);
            s.circuit = new RushCircuit(run.id(), run.seed(), origin, slot);
            if (firstArena != null) s.circuit.firstArena(firstArena);
        }
        sessions(player.getServer()).put(player.getUUID(), s);
        try {
            ItemStack ride = player.getCapability(JetSetDataProvider.CAPABILITY)
                    .map(data -> data.rideGear().copy()).orElse(ItemStack.EMPTY);
            if (ride.isEmpty()) ride = new ItemStack(ModItems.INLINE_SKATES.get());
            player.getInventory().clearContent();
            player.containerMenu.setCarried(ItemStack.EMPTY);
            ItemStack spray = new ItemStack(ModItems.SPRAY_CAN.get());
            spray.getOrCreateTag().putBoolean("Unbreakable", true);
            SprayCanItem.setPaintColor(spray, PaintColor.CYAN);
            ItemStack balloons = new ItemStack(ModItems.PAINT_BALLOONS.get(PaintColor.CYAN).get(), 16);
            balloons.getOrCreateTag().putBoolean("JetSetCraftRushLoan", true);
            player.getInventory().setItem(0, balloons);
            player.getInventory().setItem(1, spray);
            player.getInventory().selected = 0;
            // All six original physical ride styles remain available in the isolated loan loadout.
            List<ItemStack> rides = List.of(new ItemStack(ModItems.INLINE_SKATES.get()),
                    new ItemStack(ModItems.QUAD_SKATES.get()), new ItemStack(ModItems.STREET_BOARD.get()),
                    new ItemStack(ModItems.BMX.get()), new ItemStack(ModItems.HOVERBOARD.get()), new ItemStack(ModItems.SCOOTER.get()));
            for (int i = 0; i < rides.size(); i++) player.getInventory().setItem(i + 2, rides.get(i));
            ItemStack equipped = ride;
            player.getCapability(JetSetDataProvider.CAPABILITY).ifPresent(data -> {
                data.load(new CompoundTag()); data.setRideGear(equipped);
                if (equipped.getItem() instanceof RideGearItem gear) data.setStyle(gear.style());
                data.setActive(true); data.setBoost(100);
            });
            player.setGameMode(GameType.SURVIVAL);
            player.removeAllEffects(); player.getFoodData().setFoodLevel(20); player.getFoodData().setSaturation(5);
            player.clearFire(); player.fallDistance = 0;
            int x = Math.floorMod(player.getUUID().hashCode(), 4096) * 512 - 1_048_576;
            int z = Math.floorMod(Long.hashCode(player.getUUID().getMostSignificantBits()), 4096) * 512 - 1_048_576;
            if (s.circuit != null) { x = s.circuit.origin().getX(); z = s.circuit.origin().getZ(); }
            player.teleportTo(district, x + 0.5, RushWorld.FLOOR_Y + 1.0, z + 0.5, 0, 0);
            player.setDeltaMovement(Vec3.ZERO);
            if (s.circuit == null) RushWorld.decorateNear(district, player.blockPosition());
            int drafts = RushAlbum.startingPerkChoices(player);
            if (drafts > 0) run.gainXp(drafts * 24 + drafts * (drafts - 1) * 6);
            syncRide(player);
            player.inventoryMenu.broadcastChanges();
            send(player, true, "", -1, -1);
            return true;
        } catch (RuntimeException error) {
            JetSetCraft.LOGGER.error("Rival Rush entry failed; restoring {}", player.getGameProfile().getName(), error);
            leave(player, "Entry interrupted. Your Minecraft state has been restored.");
            return false;
        }
    }

    private static int freeCircuitSlot(MinecraftServer server) {
        java.util.Set<Integer> occupied = new java.util.HashSet<>();
        for (Session existing : sessions(server).values()) if (existing.circuit != null) occupied.add(existing.circuit.slot());
        for (int slot = 0; slot < 2048; slot++) if (!occupied.contains(slot)) return slot;
        return -1;
    }

    public static boolean leave(ServerPlayer player, String reason) {
        Session s = session(player);
        if (s != null && s.exiting) return false;
        String summary = reason;
        if (s != null) {
            s.exiting = true;
            summary += "  Score " + s.run.score() + " · rivals " + s.run.defeats() + " · cards " + s.cards;
            s.run.finish();
            for (Actor actor : s.actors.values()) actor.mob.discard();
            for (Pickup pickup : s.pickups.values()) pickup.entity.discard();
            for (Projectile projectile : s.projectiles.values()) projectile.discard();
            s.actors.clear(); s.pickups.clear(); s.projectiles.clear();
        }
        boolean restored = !RushReturnState.hasJournal(player) || RushReturnState.restore(player);
        if (!restored) {
            if (s != null) s.exiting = false;
            return fail(player, "Your return snapshot is retained. Use Return again or reconnect to recover safely.");
        }
        if (s != null) {
            try {
                CrewHouseManager.bankRush(player, s.run.id(), s.gang.id(), s.run.elapsedTicks(),
                        s.cards, s.run.defeats(), s.run.score());
            } catch (RuntimeException error) {
                // Optional progression must never prevent the player returning to their ordinary world.
                JetSetCraft.LOGGER.error("Could not settle crew progress for Rush {} owned by {}; Minecraft state was restored",
                        s.run.id(), player.getUUID(), error);
            }
        }
        sessions(player.getServer()).remove(player.getUUID());
        LAST_MENU.remove(player.getUUID());
        if (!RushPlayerStorage.flush(player)) {
            // Restoration already succeeded in memory. Do not reinsert/replay a stale journal or permit
            // container transactions before the restored state is saved; normal logout retries the save.
            player.connection.disconnect(Component.literal(
                    "Rival Rush restored your Minecraft state, but the server could not save it. Reconnect after the server can save player data."));
            return false;
        }
        syncRide(player);
        send(player, true, summary, -1, -1);
        return true;
    }

    public static void recover(ServerPlayer player) {
        Session existing = session(player);
        if (existing != null && existing.player != player) playerCloned(existing.player);
        else if (existing != null) return;
        if (RushReturnState.hasJournal(player)) leave(player, "Recovered an interrupted Rival Rush. Collected cards were preserved.");
        else if (RushWorld.isRush(player.level())) {
            ServerLevel home = player.getServer().overworld();
            BlockPos spawn = home.getSharedSpawnPos();
            player.teleportTo(home, spawn.getX() + 0.5, spawn.getY(), spawn.getZ() + 0.5, 0, 0);
            send(player, true, "Returned from the Rush district; there was no active run.", -1, -1);
        }
    }

    public static void dimensionChanged(ServerPlayer player) {
        Session s = session(player);
        if (s != null && !s.exiting && !RushWorld.isRush(player.level()))
            leave(player, "You left the Rush district. Your Minecraft state was restored.");
    }

    public static void stopServer(MinecraftServer server) {
        for (Session s : List.copyOf(sessions(server).values())) leave(s.player, "Server stopped; your run was safely banked.");
        SERVERS.remove(server); LAST_MENU.clear(); LAST_START.clear();
    }

    public static void tick(MinecraftServer server) {
        for (Session s : List.copyOf(sessions(server).values())) {
            if (s.exiting) continue;
            try { tick(s); }
            catch (RuntimeException error) {
                JetSetCraft.LOGGER.error("Rival Rush interrupted; returning {} safely", s.player.getGameProfile().getName(), error);
                leave(s.player, "The session was interrupted. Your collected cards were kept.");
            }
        }
    }

    private static void tick(Session s) {
        ServerPlayer player = s.player;
        if (!RushWorld.isRush(player.level())) { dimensionChanged(player); return; }
        if (!JetSetConfig.SERVER.enableRivalRush.get()) { leave(player, "Rival Rush was disabled by the server."); return; }
        if (s.run.isFinished()) { leave(player, "Run finished. Your collected cards were kept."); return; }
        ServerLevel level = player.serverLevel();
        if (s.circuit == null) RushWorld.decorateNear(level, player.blockPosition());
        else if (s.circuit.builder() != null) s.circuit.builder().step(level);
        // Removed shots must not accumulate while an offer or Encore pauses the run clock.
        s.projectiles.values().removeIf(projectile -> {
            boolean remove = projectile.isRemoved() || projectile.tickCount > 200 || !RushSpellCompat.allowed(projectile)
                    || (s.circuit != null && !s.circuit.inArena(projectile.position(), 8));
            if (remove) projectile.discard();
            return remove;
        });
        if (player.getY() < 50 || !level.getWorldBorder().isWithinBounds(player.blockPosition())
                || (s.circuit != null && !s.circuit.inCell(player.position()))) {
            leave(player, "You left the playable district. Your collected cards were kept."); return;
        }
        if (s.run.hasOffer() || s.run.awaitingEncore()) {
            s.motion.reset(player.position());
            freeze(s);
            if (s.announcedOffer != s.run.offerVersion() || (s.run.awaitingEncore() && !s.finaleSent)) {
                s.announcedOffer = s.run.offerVersion(); s.finaleSent = s.run.awaitingEncore();
                send(player, true, "", -1, -1);
            }
            return;
        }
        if (s.circuit != null) {
            s.objective = s.circuit.objective(s.run);
            if (s.circuit.phase() == RushCircuit.Phase.CHOOSING || s.circuit.phase() == RushCircuit.Phase.COMPLETE) {
                s.motion.reset(player.position());
                freeze(s);
                if (s.announcedJourney != s.circuit.version()) {
                    s.announcedJourney = s.circuit.version(); send(player, true, "", -1, -1);
                }
                return;
            }
            if (s.circuit.phase() == RushCircuit.Phase.TRAVEL) {
                s.run.tickActive(); freeze(s);
                tickMovementArtifacts(s, false);
                if (s.circuit.arrive(s.run, player.position())) {
                    s.lastSpawn = s.run.elapsedTicks(); s.lastHurt = s.run.elapsedTicks();
                    s.musicStop = s.circuit.center().offset(0, 1, s.circuit.current().radius() - 6);
                    if (level.getChunkSource().getChunkNow(s.musicStop.getX() >> 4, s.musicStop.getZ() >> 4) != null) {
                        level.setBlock(s.musicStop.below(), net.minecraft.world.level.block.Blocks.SEA_LANTERN.defaultBlockState(), 2);
                        level.setBlock(s.musicStop, ModBlocks.BOOMBOX.get().defaultBlockState(), 2);
                    }
                    level.playSound(null, player.blockPosition(), SoundEvents.NOTE_BLOCK_BELL.value(), SoundSource.PLAYERS, .8f, 1);
                    send(player, false, "Arena entered: " + s.circuit.current().name(), -1, -1);
                } else if (s.run.elapsedTicks() % 10 == 0) {
                    Vec3 direction = Vec3.atCenterOf(s.circuit.center()).subtract(player.position()).normalize();
                    Vec3 beacon = player.position().add(direction.scale(5)).add(0, 1, 0);
                    level.sendParticles(ParticleTypes.END_ROD, beacon.x, beacon.y, beacon.z, 5, .2, .4, .2, .01);
                    send(player, false, "", -1, -1);
                }
                return;
            }
            if (!s.circuit.inArena(player.position(), 4)) {
                s.motion.reset(player.position());
                freeze(s); s.objective = "Return to " + s.circuit.current().name() + " — arena time is paused.";
                if (level.getGameTime() % 10 == 0) send(player, false, "", -1, -1);
                return;
            }
        }
        s.run.tickActive();
        if (s.run.awaitingEncore()) {
            freeze(s); s.finaleSent = true; send(player, true, "", -1, -1); return;
        }
        long time = s.run.elapsedTicks();
        if (time % 20 == 0) {
            player.getFoodData().setFoodLevel(20); player.getFoodData().setSaturation(5);
            player.clearFire(); player.setAirSupply(player.getMaxAirSupply());
            if (time - s.lastHurt > 100) s.run.heal(s.run.recoveryPerSecond());
            s.walls.entrySet().removeIf(entry -> time - entry.getValue() > 6000);
            s.musicStops.entrySet().removeIf(entry -> time - entry.getValue() > 12000);
        }
        collectPickups(s);
        if (s.run.hasOffer()) {
            s.motion.reset(player.position());
            freeze(s); s.announcedOffer = s.run.offerVersion(); send(player, true, "", -1, -1); return;
        }
        tickActors(s);
        if (s.run.isFinished()) return;
        tickMovementArtifacts(s, true);
        autoFire(s);
        tickArtifacts(s);
        tickObjectives(s);
        String cache = s.points.tick(player, s.run, s.circuit);
        if (!cache.isEmpty()) s.objective = cache;
        if (s.circuit != null && s.circuit.objectiveMet(s.run) && !s.run.hasOffer()) {
            finishArena(s); return;
        }
        if (time - s.lastSpawn >= Math.max(8, 36 - Math.min(20, (int) (time / 1800)))) {
            s.lastSpawn = time;
            int desiredPressure = 16 + (int) Math.min(64, time / 900);
            if (remix(s.run) == 3) desiredPressure = desiredPressure * 3 / 4;
            int pressure = s.actors.values().stream().mapToInt(a -> a.role == Role.CAPTAIN ? 8 : a.role == Role.BRUISER ? 3 : 1).sum();
            if (pressure < desiredPressure) spawnActor(s, nextRole(s), false);
        }
        long captainChapter = time / 6000;
        if (time >= 3000 && captainChapter != s.lastCaptain
                && s.actors.values().stream().noneMatch(actor -> actor.role == Role.CAPTAIN)) {
            if (spawnActor(s, Role.CAPTAIN, true)) s.lastCaptain = captainChapter;
        }
        if (time % 10 == 0) send(player, false, "", -1, -1);
    }

    private static void finishArena(Session s) {
        RushArenaCatalog.Profile profile = s.circuit.current();
        long activeTicks = s.circuit.activeTicks(s.run);
        int defeats = (int) Math.min(Integer.MAX_VALUE, s.circuit.defeats(s.run));
        UUID receipt = s.circuit.rewardId();
        String reward = RushPointsOfInterest.grant(s.run, profile.reward(), profile.rewardRanks());
        s.run.magnet(200); collectPickups(s);
        for (Actor actor : s.actors.values()) actor.mob.discard();
        for (Projectile projectile : s.projectiles.values()) projectile.discard();
        s.actors.clear(); s.projectiles.clear();
        if (!s.circuit.complete(s.run)) return;
        s.run.grantRemix();
        String mode = profile.sharedReward().equals("balanced") ? "rush_arena" : "rush_arena_" + profile.sharedReward();
        String settlement = "";
        CompoundTag shared = new CompoundTag();
        try {
            var earned = CrewHouseManager.bankActivity(s.player, receipt, s.gang.id(), mode, activeTicks, defeats, s.run.score());
            shared.putBoolean("Accepted", earned.accepted()); shared.putBoolean("PendingHouse", earned.pendingHouse());
            shared.putInt("Supplies", earned.supplies()); shared.putInt("CrewXp", earned.crewXp());
            shared.putInt("MemberXp", earned.memberXp()); shared.putInt("Reputation", earned.reputation());
            if (earned.accepted()) settlement = " · supplies +" + earned.supplies() + " · crew XP +" + earned.crewXp()
                    + " · member XP +" + earned.memberXp() + " · reputation +" + earned.reputation();
        } catch (RuntimeException error) {
            JetSetCraft.LOGGER.error("Could not settle completed Rush arena {} for {}", receipt, s.player.getUUID(), error);
        }
        s.circuit.recordSettlement(receipt, reward, shared);
        s.player.sendSystemMessage(Component.literal(profile.name() + " cleared! " + reward + settlement).withStyle(ChatFormatting.GOLD));
        s.player.serverLevel().playSound(null, s.player.blockPosition(), SoundEvents.PLAYER_LEVELUP, SoundSource.PLAYERS, .5f, 1.1f);
        s.objective = s.circuit.objective(s.run); send(s.player, true, reward + settlement, -1, -1);
    }

    private static boolean combatPaused(Session s) {
        return s.exiting || s.run.isFinished() || s.run.hasOffer() || s.run.awaitingEncore()
                || (s.circuit != null && (s.circuit.phase() != RushCircuit.Phase.ARENA || !s.circuit.inArena(s.player.position(), 4)));
    }

    private static boolean betweenArenas(Session s) {
        return s.circuit != null && !s.exiting && !s.run.isFinished()
                && (s.circuit.phase() == RushCircuit.Phase.CHOOSING || s.circuit.phase() == RushCircuit.Phase.COMPLETE);
    }

    private static Role nextRole(Session s) {
        int sequence = s.sequence;
        int remix = remix(s.run);
        int bruiserEvery = remix == 2 ? 3 : 7;
        int taggerEvery = remix == 1 || remix == 4 ? 2 : 4;
        if (s.run.elapsedTicks() > 1800 && sequence % bruiserEvery == 0) return Role.BRUISER;
        if (s.run.elapsedTicks() > 600 && sequence % taggerEvery == 0) return Role.TAGGER;
        return Role.SKATER;
    }

    private static int remix(RushRun run) { return (int) ((run.chapter() - 1) % 6); }

    private static boolean spawnActor(Session s, Role role, boolean captain) {
        ServerLevel level = s.player.serverLevel();
        ResourceLocation species = s.roster.get(Math.floorMod(s.sequence++, s.roster.size()));
        EntityType<?> type = ForgeRegistries.ENTITY_TYPES.getValue(species);
        Entity raw;
        try { raw = type == null ? null : type.create(level); }
        catch (RuntimeException error) { return false; }
        if (!(raw instanceof Mob mob)) { if (raw != null) raw.discard(); return false; }
        if (!MobStreetGear.eligible(mob)) { mob.discard(); return false; }
        boolean placed = false;
        for (int attempt = 0; attempt < 12; attempt++) {
            double angle = (s.sequence + attempt * 5) * 2.399963229728653;
            double distance = 18 + attempt % 3 * 4 + mob.getBbWidth();
            if (s.circuit != null) distance = Math.min(distance, Math.max(6, s.circuit.current().radius() - 6));
            double x = s.player.getX() + Math.cos(angle) * distance;
            double z = s.player.getZ() + Math.sin(angle) * distance;
            if (s.circuit != null && !s.circuit.inArena(new Vec3(x, RushWorld.FLOOR_Y + 1, z), -4)) continue;
            if (!level.getChunkSource().hasChunk(Mth.floor(x) >> 4, Mth.floor(z) >> 4)) continue;
            mob.moveTo(x, RushWorld.FLOOR_Y + 1, z, 0, 0);
            if (!level.getWorldBorder().isWithinBounds(mob.getBoundingBox()) || !level.noCollision(mob)) continue;
            placed = true; break;
        }
        if (!placed) { mob.discard(); return false; }
        try {
            ForgeEventFactory.onFinalizeSpawn(mob, level, level.getCurrentDifficultyAt(mob.blockPosition()), MobSpawnType.EVENT, null, null);
        } catch (RuntimeException error) { mob.discard(); return false; }
        mark(mob, s);
        mob.setPersistenceRequired(); mob.setInvulnerable(true); mob.setCanPickUpLoot(false);
        double hp = (captain ? 220 : role == Role.BRUISER ? 42 : role == Role.TAGGER ? 18 : 14)
                * (1.0 + Math.min(2.0, s.run.elapsedTicks() / 18000.0));
        Actor actor = new Actor(mob, role, species, hp);
        s.actors.put(mob.getUUID(), actor);
        if (!level.addFreshEntity(mob)) { s.actors.remove(mob.getUUID()); mob.discard(); return false; }
        if (!MobStreetGear.equip(mob, GangGearSelector.forActor(mob, s.sequence), StreetGearAcquisition.GANG_EVENT, false).equipped()) {
            s.actors.remove(mob.getUUID()); mob.discard(); return false;
        }
        GangMemberState.attach(mob, s.gang.id(), role.name().toLowerCase(java.util.Locale.ROOT), s.run.id(), true, 0);
        updateName(s, actor);
        level.sendParticles(ParticleTypes.POOF, mob.getX(), mob.getY() + 1, mob.getZ(), 8, 0.5, 0.8, 0.5, 0.02);
        if (captain) {
            s.objective = "Crew captain arrived — keep moving and tag its escort!";
            level.playSound(null, s.player.blockPosition(), SoundEvents.NOTE_BLOCK_BELL.value(), SoundSource.RECORDS, 0.7f, 0.7f);
        }
        return true;
    }

    private static void tickActors(Session s) {
        long time = s.run.elapsedTicks();
        Iterator<Actor> iterator = s.actors.values().iterator();
        while (iterator.hasNext()) {
            Actor actor = iterator.next(); Mob mob = actor.mob;
            if (mob.isRemoved() || !mob.isAlive() || mob.distanceToSqr(s.player) > 160 * 160) {
                mob.discard(); iterator.remove(); continue;
            }
            mob.clearFire(); mob.setAirSupply(mob.getMaxAirSupply());
            if (actor.exposedUntil > 0 && time >= actor.exposedUntil) {
                actor.exposedUntil = 0; actor.exposureMultiplier = 1; updateName(s, actor);
            }
            if (actor.chillUntil > 0 && time >= actor.chillUntil) {
                actor.chillUntil = 0; actor.chillRank = 0; updateName(s, actor);
            }
            if (actor.venomUntil > 0 && time > actor.venomUntil) {
                actor.venomUntil = 0; actor.venomRank = 0; updateName(s, actor);
            }
            if (s.circuit != null && !s.circuit.inArena(mob.position(), 6)) { mob.discard(); iterator.remove(); continue; }
            // Preserve the original mob/AI; stop this run-owned creeper's native fuse from destroying its card actor.
            if (mob instanceof net.minecraft.world.entity.monster.Creeper creeper) creeper.setSwellDir(-1);
            if (time < actor.staggerUntil) { mob.setTarget(null); mob.getNavigation().stop(); continue; }
            mob.setTarget(s.player);
            Vec3 toward = s.player.position().subtract(mob.position());
            double distance = toward.horizontalDistance();
            double speed = actor.role == Role.BRUISER ? 0.12 : actor.role == Role.TAGGER ? 0.15 : 0.19;
            // Endless changes composition and roles; skating opponents retain a readable, bounded top speed.
            speed += Math.min(0.05, s.run.elapsedTicks() / 180000.0);
            if (actor.role == Role.CAPTAIN) speed = 0.17;
            boolean chilled = actor.chillUntil > time && s.run.rank(RushRun.Upgrade.CHILL) > 0;
            if (chilled) speed *= .8 - actor.chillRank * .05;
            double orbit = actor.role == Role.TAGGER && distance < 9 ? 1.1 : 0;
            if (distance > 0.1) {
                Vec3 horizontal = new Vec3(toward.x, 0, toward.z).normalize();
                Vec3 steering = orbit > 0 ? new Vec3(-horizontal.z, 0, horizontal.x) : horizontal;
                double vertical = mob.getDeltaMovement().y;
                if (mob.horizontalCollision && mob.onGround()) vertical = 0.42;
                mob.setDeltaMovement(steering.x * speed, vertical, steering.z * speed);
                mob.getLookControl().setLookAt(s.player, 30, 30);
            }
            if (actor.paintMark != null && time >= actor.paintDue) {
                Vec3 mark = actor.paintMark; actor.paintMark = null;
                s.player.serverLevel().sendParticles(ParticleTypes.WAX_OFF, mark.x, mark.y + 0.2, mark.z, 18, 1, 0.2, 1, 0.02);
                if (s.player.position().distanceToSqr(mark) < 1.5 * 1.5) hurt(s.player, 6);
            }
            long cadence = actor.role == Role.TAGGER ? (remix(s.run) == 4 ? 50 : 70) : actor.role == Role.CAPTAIN ? 40 : 30;
            if (chilled) cadence = Math.round(cadence * (1 + actor.chillRank * .15));
            double reach = actor.role == Role.TAGGER ? 12 : Math.max(1.5, mob.getBbWidth() * 0.5 + 0.7);
            if (distance < reach && time - actor.lastAttack >= cadence && mob.hasLineOfSight(s.player)) {
                actor.lastAttack = time;
                if (actor.role == Role.TAGGER) {
                    actor.paintMark = s.player.position(); actor.paintDue = time + 16;
                    // The marked position resolves 0.8 seconds later, allowing a real dodge even from rest.
                    s.player.serverLevel().sendParticles(ParticleTypes.ELECTRIC_SPARK,
                            s.player.getX(), s.player.getY() + 0.15, s.player.getZ(), 12, 0.8, 0.1, 0.8, 0);
                } else hurt(s.player, actor.role == Role.CAPTAIN ? 12 : actor.role == Role.BRUISER ? 10 : 5);
                if (s.run.isFinished()) return;
            }
        }
    }

    private static void freeze(Session s) {
        for (Actor actor : s.actors.values()) {
            actor.mob.setTarget(null); actor.mob.getNavigation().stop(); actor.mob.setDeltaMovement(Vec3.ZERO);
        }
    }

    private static void autoFire(Session s) {
        long time = s.run.elapsedTicks();
        if (time - s.lastShot < Math.max(6, Math.round(22 * s.run.cadenceMultiplier()))) return;
        Actor target = s.actors.values().stream().filter(a -> !a.claimed && a.mob.isAlive()
                        && a.mob.distanceToSqr(s.player) < 24 * 24 && s.player.hasLineOfSight(a.mob))
                .min(Comparator.comparingDouble(a -> a.mob.distanceToSqr(s.player))).orElse(null);
        if (target == null) return;
        s.lastShot = time;
        ItemStack paint = new ItemStack(ModItems.PAINT_BALLOONS.get(PaintColor.CYAN).get());
        PaintBalloonEntity shot = new PaintBalloonEntity(s.player.level(), s.player, paint);
        mark(shot, s);
        Vec3 aim = target.mob.getBoundingBox().getCenter().subtract(shot.position());
        shot.shoot(aim.x, aim.y + aim.horizontalDistance() * 0.07, aim.z, 1.7f, 0);
        s.player.serverLevel().addFreshEntity(shot);
    }

    public static boolean balloonHit(ServerPlayer player, HitResult hit) {
        Session s = session(player);
        if (s == null) return false;
        if (combatPaused(s)) return true;
        if (hit instanceof EntityHitResult entityHit) {
            tagEnemy(player, entityHit.getEntity(), 7.0);
            Vec3 impact = hit.getLocation();
            for (Actor actor : List.copyOf(s.actors.values())) {
                if (actor.mob != entityHit.getEntity() && actor.mob.position().distanceToSqr(impact) < s.run.splashRadius() * s.run.splashRadius())
                    damageActor(s, actor.mob, 3.5, false);
            }
        } else if (hit instanceof BlockHitResult blockHit && blockHit.getDirection().getAxis().isHorizontal()) {
            int painted = PaintSplash.throwBalloon(player.serverLevel(), player, player,
                    hit.getLocation().add(Vec3.atLowerCornerOf(blockHit.getDirection().getNormal()).scale(0.3)), PaintColor.CYAN);
            if (painted > 0) tagWall(player, blockHit.getBlockPos(), blockHit.getDirection());
        }
        return true;
    }

    public static boolean tagEnemy(ServerPlayer player, Entity target, double baseDamage) {
        Session s = session(player);
        if (s == null) return false;
        return damageActor(s, target, baseDamage, true);
    }

    /** Native spell relays and secondary effects do not consume primary-tag charges or recursively trigger artifacts. */
    public static boolean artifactHit(ServerPlayer player, Entity target, double baseDamage) {
        Session s = session(player);
        return s != null && damageActor(s, target, baseDamage, false);
    }

    /** Read-only diagnostic used by provider acceptance tests and overlay integrations. */
    public static double actorVitality(ServerPlayer player, Entity target) {
        Session s = session(player); Actor actor = s == null || target == null ? null : s.actors.get(target.getUUID());
        return actor == null ? -1 : actor.vitality;
    }

    private static boolean damageActor(Session s, Entity target, double baseDamage, boolean primary) {
        ServerPlayer player = s.player;
        if (target == null || combatPaused(s) || !belongsTo(target, player)) return false;
        Actor actor = s.actors.get(target.getUUID());
        if (actor == null || actor.claimed || target.isRemoved() || !Double.isFinite(baseDamage) || baseDamage <= 0) return false;
        if (s.circuit != null && !s.circuit.inArena(target.position(), 6)) return false;
        boolean critical = primary && s.run.recordHitAndRollCritical();
        double line = primary ? s.run.consumeLineMultiplier() : 1;
        double exposure = actor.exposedUntil > s.run.elapsedTicks() ? actor.exposureMultiplier : 1;
        actor.vitality -= Math.min(40, baseDamage) * s.run.damageMultiplier()
                * (critical ? s.run.criticalMultiplier() : 1) * line * exposure;
        if (line > 1) {
            s.lineProcs++;
            player.serverLevel().sendParticles(ParticleTypes.ELECTRIC_SPARK, target.getX(), target.getY() + .7,
                    target.getZ(), 14, .4, .5, .4, .05);
            player.serverLevel().playSound(null, target.blockPosition(), SoundEvents.NOTE_BLOCK_BELL.value(), SoundSource.PLAYERS, .35f, 1.6f);
        }
        player.serverLevel().sendParticles(ParticleTypes.WAX_OFF, target.getX(), target.getY() + target.getBbHeight() * 0.5,
                target.getZ(), 5, 0.3, 0.5, 0.3, 0.01);
        if (critical) player.serverLevel().sendParticles(ParticleTypes.CRIT, target.getX(), target.getY() + 1, target.getZ(), 12, .4, .4, .4, .05);
        Vec3 impact = target.position();
        if (actor.vitality > 0) updateName(s, actor);
        else {
            actor.claimed = true;
            s.actors.remove(target.getUUID());
            s.run.recordDefeat(); s.run.addScore(actor.role == Role.CAPTAIN ? 500 : 20);
            spawnCard(s, actor);
            if (actor.role == Role.CAPTAIN) { s.run.gainXp(36); s.run.heal(18); }
            target.discard();
        }
        if (primary) onPaintHit(s, target, impact);
        return true;
    }

    private static void onPaintHit(Session s, Entity original, Vec3 impact) {
        long time = s.run.elapsedTicks();
        Actor tagged = s.actors.get(original.getUUID());
        if (tagged != null && !tagged.claimed) {
            int venom = s.run.rank(RushRun.Upgrade.VENOM), chill = s.run.rank(RushRun.Upgrade.CHILL);
            if (venom > 0) {
                if (tagged.venomRank == 0 || tagged.venomUntil < time) tagged.venomDue = time + 20;
                tagged.venomRank = venom; tagged.venomUntil = time + 60 + venom * 20;
            }
            if (chill > 0) { tagged.chillRank = chill; tagged.chillUntil = time + 40 + chill * 10; }
            if (venom > 0 || chill > 0) updateName(s, tagged);
        }
        int radiance = s.run.rank(RushRun.Upgrade.RADIANCE);
        if (radiance > 0 && s.run.hits() % 6 == 0) {
            s.run.heal(2 + radiance * 2); s.radiantPulses++;
            s.player.serverLevel().sendParticles(ParticleTypes.HEART, s.player.getX(), s.player.getY() + 1,
                    s.player.getZ(), 4, .3, .4, .3, .01);
        }
        int multishot = s.run.rank(RushRun.Upgrade.MULTISHOT);
        if (multishot > 0 && s.run.hits() % 4 == 0) {
            Vec3 aim = impact.subtract(s.player.position()).normalize();
            double range = Math.min(28, (10 + multishot * 2) * s.run.areaMultiplier());
            List<Actor> targets = s.actors.values().stream().filter(a -> a.mob != original && !a.claimed && a.mob.isAlive()
                            && a.mob.distanceToSqr(s.player) <= range * range && s.player.hasLineOfSight(a.mob)
                            && a.mob.position().subtract(s.player.position()).normalize().dot(aim) >= .8)
                    .sorted(Comparator.comparingDouble(a -> a.mob.distanceToSqr(s.player)))
                    .limit(1 + multishot / 2).toList();
            boolean hit = false;
            for (Actor target : targets) {
                hit |= damageActor(s, target.mob, 2 + multishot, false);
                s.player.serverLevel().sendParticles(ParticleTypes.SPLASH, target.mob.getX(), target.mob.getY() + .7,
                        target.mob.getZ(), 8, .3, .4, .3, .03);
            }
            if (hit) s.multishotVolleys++;
        }
        int chain = s.run.rank(RushRun.Upgrade.CHAIN);
        if (chain > 0) {
            double radius = (4 + chain) * s.run.areaMultiplier();
            List<Actor> targets = s.actors.values().stream().filter(a -> a.mob != original && !a.claimed
                            && a.mob.position().distanceToSqr(impact) < radius * radius && s.player.hasLineOfSight(a.mob))
                    .sorted(Comparator.comparingDouble(a -> a.mob.position().distanceToSqr(impact))).limit(Math.min(4, chain)).toList();
            for (Actor actor : targets) {
                s.player.serverLevel().sendParticles(ParticleTypes.ELECTRIC_SPARK, actor.mob.getX(), actor.mob.getY() + .7,
                        actor.mob.getZ(), 6, .3, .4, .3, .03);
                damageActor(s, actor.mob, 2 + chain, false);
            }
        }
        int firework = s.run.rank(RushRun.Upgrade.FIREWORK);
        if (firework > 0 && s.run.hits() % 6 == 0) {
            double radius = (3 + firework * .5) * s.run.areaMultiplier();
            s.player.serverLevel().sendParticles(ParticleTypes.FIREWORK, impact.x, impact.y + 1, impact.z, 25, radius / 3, .5, radius / 3, .07);
            for (Actor actor : List.copyOf(s.actors.values())) if (actor.mob.position().distanceToSqr(impact) < radius * radius)
                damageActor(s, actor.mob, 5 + firework * 2, false);
        }
    }

    private static void tickMovementArtifacts(Session s, boolean combat) {
        JetSetData ride = s.player.getCapability(JetSetDataProvider.CAPABILITY).resolve().orElse(null);
        boolean riding = ride != null && ride.active() && !s.player.isPassenger() && !s.player.isFallFlying()
                && !s.player.isInWaterOrBubble() && !s.player.isSwimming();
        RushMotionTracker.Step motion = s.motion.sample(s.player.position(), s.player.onGround(), riding, combat);
        if (s.run.chargeLine(motion.ridingDistance())) {
            s.player.serverLevel().sendParticles(ParticleTypes.ELECTRIC_SPARK, s.player.getX(), s.player.getY() + .3,
                    s.player.getZ(), 8, .35, .2, .35, .02);
            s.player.serverLevel().playSound(null, s.player.blockPosition(), SoundEvents.NOTE_BLOCK_CHIME.value(), SoundSource.PLAYERS, .3f, 1.7f);
        }
        int rank = s.run.rank(RushRun.Upgrade.AIR_BREAK);
        long time = s.run.elapsedTicks();
        if (!combat || rank == 0 || !motion.landed() || combatPaused(s)
                || time - s.lastAirBreak < Math.round(100 * s.run.artifactIntervalMultiplier())) return;
        s.lastAirBreak = time; s.airBreakPulses++;
        double radius = (3 + rank * .5) * s.run.areaMultiplier();
        List<Actor> targets = s.actors.values().stream().filter(a -> !a.claimed && a.mob.isAlive()
                        && a.mob.distanceToSqr(s.player) <= radius * radius && s.player.hasLineOfSight(a.mob))
                .sorted(Comparator.comparingDouble(a -> a.mob.distanceToSqr(s.player))).limit(12).toList();
        for (Actor actor : targets) if (damageActor(s, actor.mob, 3 + rank * 2, false) && !actor.mob.isRemoved())
            actor.staggerUntil = Math.max(actor.staggerUntil, time + 10 + rank * 5);
        s.player.serverLevel().sendParticles(ParticleTypes.CLOUD, s.player.getX(), s.player.getY() + .15,
                s.player.getZ(), 18, radius / 3, .08, radius / 3, .035);
        s.player.serverLevel().playSound(null, s.player.blockPosition(), SoundEvents.PLAYER_ATTACK_SWEEP, SoundSource.PLAYERS, .45f, .85f);
    }

    private static void tickArtifacts(Session s) {
        if (combatPaused(s)) return;
        long time = s.run.elapsedTicks();
        double interval = s.run.artifactIntervalMultiplier();
        if (s.run.rank(RushRun.Upgrade.VENOM) > 0) {
            // Iterate a bounded session snapshot: a damage tick may remove an actor and settle one defeat.
            for (Actor actor : List.copyOf(s.actors.values())) {
                if (combatPaused(s)) break;
                if (actor.venomRank <= 0 || time > actor.venomUntil || time < actor.venomDue) continue;
                actor.venomDue = time + 20;
                damageActor(s, actor.mob, 1 + actor.venomRank, false);
                s.player.serverLevel().sendParticles(ParticleTypes.SPORE_BLOSSOM_AIR, actor.mob.getX(), actor.mob.getY() + .7,
                        actor.mob.getZ(), 5, .3, .4, .3, .01);
            }
        }
        int burst = s.run.rank(RushRun.Upgrade.SOUL_BURST);
        if (burst > 0 && s.run.soulBurstCharge() >= 5 && time - s.lastSoulBurst >= Math.round(100 * interval)) {
            double range = (4 + burst * .5) * s.run.areaMultiplier();
            List<Actor> targets = s.actors.values().stream().filter(a -> !a.claimed && a.mob.isAlive()
                            && a.mob.distanceToSqr(s.player) <= range * range && s.player.hasLineOfSight(a.mob))
                    .sorted(Comparator.comparingDouble(a -> a.mob.distanceToSqr(s.player))).limit(12).toList();
            if (!targets.isEmpty() && s.run.spendSoulBurst()) {
                s.lastSoulBurst = time; s.soulBursts++;
                for (Actor target : targets) damageActor(s, target.mob, 4 + burst * 3, false);
                s.player.serverLevel().sendParticles(ParticleTypes.SOUL_FIRE_FLAME, s.player.getX(), s.player.getY() + .7,
                        s.player.getZ(), 20, range / 3, .4, range / 3, .04);
                s.player.serverLevel().playSound(null, s.player.blockPosition(), SoundEvents.SOUL_ESCAPE, SoundSource.PLAYERS, .5f, 1.1f);
            }
        }
        int expose = s.run.rank(RushRun.Upgrade.EXPOSE);
        if (expose > 0 && time - s.lastExpose >= Math.round(140 * interval)) {
            Actor target = s.actors.values().stream().filter(a -> !a.claimed && a.mob.isAlive()
                            && a.mob.distanceToSqr(s.player) <= 400 && s.player.hasLineOfSight(a.mob))
                    .sorted(Comparator.comparingInt((Actor a) -> a.role == Role.CAPTAIN ? 0 : a.role == Role.BRUISER ? 1 : 2)
                            .thenComparingDouble(a -> a.mob.distanceToSqr(s.player))).findFirst().orElse(null);
            if (target != null) {
                for (Actor previous : s.actors.values()) if (previous != target && previous.exposedUntil > 0) {
                    previous.exposedUntil = 0; previous.exposureMultiplier = 1; updateName(s, previous);
                }
                s.lastExpose = time; s.markApplications++;
                target.exposedUntil = time + 40 + expose * 10;
                target.exposureMultiplier = 1.20 + expose * .05;
                updateName(s, target);
                s.player.serverLevel().sendParticles(ParticleTypes.ENCHANT, target.mob.getX(), target.mob.getY() + 1.2,
                        target.mob.getZ(), 12, .3, .5, .3, .1);
                s.player.serverLevel().playSound(null, target.mob.blockPosition(), SoundEvents.NOTE_BLOCK_PLING.value(), SoundSource.PLAYERS, .35f, .9f);
            }
        }
        int wind = s.run.rank(RushRun.Upgrade.WIND);
        if (wind > 0 && time - s.lastHorn >= Math.round(180 * interval)) {
            s.lastHorn = time;
            double range = (4 + wind) * s.run.areaMultiplier();
            for (Actor actor : s.actors.values()) if (actor.mob.distanceToSqr(s.player) < range * range) {
                Vec3 outward = actor.mob.position().subtract(s.player.position()).multiply(1, 0, 1).normalize();
                actor.mob.setDeltaMovement(outward.scale(.7 + .1 * wind).add(0, .2, 0));
                actor.staggerUntil = time + 15 + wind * 5;
            }
            s.player.serverLevel().sendParticles(ParticleTypes.CLOUD, s.player.getX(), s.player.getY() + .3, s.player.getZ(), 24, range / 3, .1, range / 3, .08);
            s.player.serverLevel().playSound(null, s.player.blockPosition(), SoundEvents.NOTE_BLOCK_DIDGERIDOO.value(), SoundSource.PLAYERS, .5f, .7f);
        }
        int soul = s.run.rank(RushRun.Upgrade.SOUL);
        if (soul > 0 && time - s.lastSoul >= Math.round(100 * interval) && s.run.vitality() < s.run.maxVitality() && s.run.spendSouls()) {
            s.lastSoul = time; s.run.heal(6 + soul * 3);
            s.player.serverLevel().sendParticles(ParticleTypes.SOUL, s.player.getX(), s.player.getY() + 1, s.player.getZ(), 10, .4, .6, .4, .02);
        }
        int wave = s.run.rank(RushRun.Upgrade.SHOCKWAVE);
        JetSetData ride = s.player.getCapability(JetSetDataProvider.CAPABILITY).resolve().orElse(null);
        if (wave > 0 && ride != null && time - s.lastShockwave >= Math.round(70 * interval)
                && s.player.getDeltaMovement().horizontalDistanceSqr() > .025 && (ride.grinding() || ride.wallRiding() || ride.trickTicks() > 0)) {
            s.lastShockwave = time;
            Vec3 direction = s.player.getDeltaMovement().multiply(1, 0, 1).normalize();
            double range = (6 + wave) * s.run.areaMultiplier();
            for (Actor actor : List.copyOf(s.actors.values())) {
                Vec3 delta = actor.mob.position().subtract(s.player.position());
                if (delta.lengthSqr() < range * range && delta.normalize().dot(direction) > .45)
                    damageActor(s, actor.mob, 6 + wave * 3, false);
            }
            Vec3 end = s.player.position().add(direction.scale(range * .5));
            s.player.serverLevel().sendParticles(ParticleTypes.SWEEP_ATTACK, end.x, end.y + .5, end.z, 5, 1, .2, 1, 0);
        }
        int spell = s.run.rank(RushRun.Upgrade.SPELL_RELAY);
        if (spell > 0 && time - s.lastSpell >= Math.round(Math.max(40, 150 - spell * 20) * interval)) {
            Actor target = s.actors.values().stream().filter(a -> a.mob.distanceToSqr(s.player) < 28 * 28 && s.player.hasLineOfSight(a.mob))
                    .min(Comparator.comparingDouble(a -> a.mob.distanceToSqr(s.player))).orElse(null);
            if (target != null) { s.lastSpell = time; RushSpellCompat.firebolt(s.player, target.mob.getBoundingBox().getCenter(), 6 + spell * 3); }
        }
    }

    private static void updateName(Session s, Actor actor) {
        boolean exposed = actor.exposedUntil > s.run.elapsedTicks();
        actor.mob.setCustomName(Component.literal(s.gang.canonicalName() + " · "
                + actor.role.name().toLowerCase(java.util.Locale.ROOT) + " · " + (int) Math.ceil(actor.vitality)
                + (exposed ? " · Exposed" : "")
                + (actor.venomUntil >= s.run.elapsedTicks() && actor.venomRank > 0 ? " · Inked" : "")
                + (actor.chillUntil > s.run.elapsedTicks() ? " · Chilled" : ""))
                .withStyle(exposed ? ChatFormatting.YELLOW : actor.role == Role.CAPTAIN ? ChatFormatting.GOLD : ChatFormatting.AQUA));
        actor.mob.setCustomNameVisible(exposed || actor.role == Role.CAPTAIN || actor.vitality < actor.maximum);
    }

    private static void spawnCard(Session s, Actor actor) {
        long time = s.run.elapsedTicks();
        for (Pickup pickup : s.pickups.values()) {
            if (!pickup.entity.isRemoved() && pickup.species.equals(actor.species) && pickup.count < 64
                    && pickup.entity.distanceToSqr(actor.mob) < 16) {
                pickup.count++; pickup.entity.getItem().setCount(pickup.count);
                pickup.lastAddedActiveTick = time; return;
            }
        }
        ItemStack card = new ItemStack(Items.PAPER);
        // Only this director may merge cards. Native item merging cannot reconcile virtual counts or run owners.
        card.getOrCreateTag().putUUID("JetSetCraftRushPickup", actor.mob.getUUID());
        card.setHoverName(Component.literal(actor.species.getPath().replace('_', ' ') + " · Rush card"));
        ItemEntity drop = new ItemEntity(s.player.serverLevel(), actor.mob.getX(), actor.mob.getY() + 0.3, actor.mob.getZ(), card);
        drop.setPickUpDelay(32767); drop.setInvulnerable(true);
        // The director expires cards in active run time, so decision pauses cannot age them away.
        drop.setUnlimitedLifetime();
        mark(drop, s);
        s.pickups.put(drop.getUUID(), new Pickup(drop, actor.species, time));
        if (!s.player.serverLevel().addFreshEntity(drop)) s.pickups.remove(drop.getUUID());
    }

    private static void collectPickups(Session s) {
        long time = s.run.elapsedTicks();
        Iterator<Pickup> iterator = s.pickups.values().iterator();
        while (iterator.hasNext()) {
            Pickup pickup = iterator.next();
            if (pickup.entity.isRemoved() || pickup.entity.distanceToSqr(s.player) > 160 * 160
                    || time - pickup.lastAddedActiveTick >= 6000L) {
                pickup.entity.discard(); iterator.remove(); continue;
            }
            if (pickup.entity.distanceToSqr(s.player) > s.run.pickupRadius() * s.run.pickupRadius()) continue;
            iterator.remove(); pickup.entity.discard(); // Claim before any reward; two event paths cannot collect twice.
            for (int i = 0; i < pickup.count; i++) {
                RushAlbum.CollectionReward reward = RushAlbum.collect(s.player, pickup.species.toString(), s.gang.id().toString());
                s.run.gainXp(reward.runXp()); s.run.addScore(reward.score()); s.cards++;
                if (reward.newTier()) s.objective = "Card mastery " + reward.tier() + ": " + pickup.species.getPath().replace('_', ' ') + "!";
            }
            s.player.serverLevel().playSound(null, s.player.blockPosition(), SoundEvents.NOTE_BLOCK_CHIME.value(), SoundSource.PLAYERS, 0.25f, 1.5f);
        }
    }

    public static boolean tagWall(ServerPlayer player, BlockPos support, Direction face) {
        Session s = session(player);
        if (s == null || combatPaused(s) || !face.getAxis().isHorizontal()
                || player.distanceToSqr(Vec3.atCenterOf(support)) > 36 || player.serverLevel().getBlockState(support).isAir()) return false;
        String patch = Math.floorDiv(support.getX(), 4) + ":" + Math.floorDiv(support.getY(), 4) + ":"
                + Math.floorDiv(support.getZ(), 4) + ":" + face.getName();
        long time = s.run.elapsedTicks();
        if (time - s.walls.getOrDefault(patch, time - 6001) <= 6000) return false;
        s.walls.put(patch, time);
        s.run.gainXp((int) Math.round(8 * s.run.flowMultiplier()));
        s.run.addScore(Math.round(60 * s.run.flowMultiplier()));
        s.objective = "Fresh wall tagged! Find another line, wall, or music stop.";
        return true;
    }

    private static void tickObjectives(Session s) {
        long time = s.run.elapsedTicks();
        JetSetData ride = s.player.getCapability(JetSetDataProvider.CAPABILITY).resolve().orElse(null);
        if (ride != null && time - s.lastStyle >= 100 && s.player.getDeltaMovement().horizontalDistanceSqr() > 0.025
                && (ride.grinding() || ride.wallRiding() || ride.trickTicks() > 0)) {
            s.lastStyle = time; s.run.addScore(Math.round(30 * s.run.flowMultiplier()));
            s.run.gainXp((int) Math.round(4 * s.run.flowMultiplier()));
        }
        if (s.circuit == null && time % 20 == 0) s.musicStop = RushWorld.nearestBoombox(s.player.serverLevel(), s.player.blockPosition());
        if (s.musicStop == null) { s.musicCharge = 0; return; }
        double distance = s.player.distanceToSqr(Vec3.atCenterOf(s.musicStop));
        boolean eligible = time - s.musicStops.getOrDefault(s.musicStop.asLong(), time - 12001) > 12000;
        if (distance < 25 && eligible) {
            s.objective = "Music stop: dance here for 4 seconds (" + s.musicCharge / 20 + "/4).";
            if (ride != null && ride.dancing()) s.musicCharge++;
            else s.musicCharge = Math.max(0, s.musicCharge - 1);
            if (s.musicCharge >= 80) {
                s.musicStops.put(s.musicStop.asLong(), time); s.musicCharge = 0;
                s.run.gainXp(remix(s.run) == 3 ? 36 : 24); s.run.addScore(250); s.run.heal(remix(s.run) == 3 ? 35 : 20);
                ModSounds.music(s.gang.id()).ifPresent(sound -> s.player.serverLevel().playSound(null, s.musicStop, sound, SoundSource.RECORDS, 0.7f, 1));
                s.objective = "Cypher complete: vitality restored. Keep the city moving!";
            }
        } else {
            s.musicCharge = 0;
            if (time % 100 == 0) s.objective = "Music stop " + (int) Math.sqrt(distance) + "m · " + modifier(s.run.chapter());
        }
    }

    private static String modifier(long chapter) {
        return switch ((int) ((chapter - 1) % 6)) {
            case 0 -> "Open streets: follow a fresh line";
            case 1 -> "Tagger mix: moving evades their paint pulses";
            case 2 -> "Heavy mix: lure bruisers through the rails";
            case 3 -> "Cypher mix: fewer rivals, stronger music-stop rewards";
            case 4 -> "Crossfire: quicker tagger volleys, keep dodging";
            default -> "Relay mix: skaters lead the next lap";
        };
    }

    public static void hurt(ServerPlayer player, double amount) {
        Session s = session(player);
        if (s == null || combatPaused(s) || !Double.isFinite(amount) || amount <= 0) return;
        long time = s.run.elapsedTicks();
        if (time - s.lastHurt < 12) return;
        s.lastHurt = time;
        double wardBefore = s.run.ward();
        if (s.run.hurt(Math.min(20, Math.max(0, amount))))
            s.objective = "Run finished — your collected cards are banked.";
        if (s.run.ward() < wardBefore) {
            player.serverLevel().sendParticles(ParticleTypes.ENCHANTED_HIT, player.getX(), player.getY() + .7,
                    player.getZ(), 10, .4, .5, .4, .02);
            player.serverLevel().playSound(null, player.blockPosition(), SoundEvents.SHIELD_BLOCK, SoundSource.PLAYERS, .35f, 1.3f);
        }
    }

    /** Only registered actors/pickups and owned player projectiles may survive a chunk/entity reload. */
    public static boolean validateJoin(Entity entity) {
        if (!owned(entity) || !(entity.level() instanceof ServerLevel level)) return false;
        CompoundTag tag = entity.getPersistentData().getCompound(OWNED);
        if (!tag.hasUUID("Owner") || !tag.hasUUID("Run")) return false;
        Session s = sessions(level.getServer()).get(tag.getUUID("Owner"));
        if (s == null || s.exiting || !s.run.id().equals(tag.getUUID("Run")) || !RushWorld.isRush(level)) return false;
        if (entity instanceof Mob mob) {
            Actor a = s.actors.get(mob.getUUID());
            if (a == null || a.claimed) return false;
            a.mob = mob; return true;
        }
        if (entity instanceof ItemEntity item) {
            Pickup pickup = s.pickups.get(item.getUUID());
            if (pickup == null) return false;
            pickup.entity = item;
            return true;
        }
        if (entity instanceof Projectile projectile && projectile.getOwner() == s.player && RushSpellCompat.allowed(projectile)
                && s.projectiles.size() < 256) {
            s.projectiles.put(projectile.getUUID(), projectile);
            return true;
        }
        return false;
    }

    public static void markPlayerProjectile(Projectile projectile) {
        if (projectile.getOwner() instanceof ServerPlayer player) {
            Session s = session(player);
            if (s != null && !combatPaused(s) && s.projectiles.size() < 256 && RushSpellCompat.allowed(projectile)) mark(projectile, s);
        }
    }

    public static boolean restoring(ServerPlayer player) { Session s = session(player); return s != null && s.exiting; }

    public static boolean pausedActor(Entity entity) {
        if (!owned(entity) || !(entity.level() instanceof ServerLevel level)) return false;
        CompoundTag tag = entity.getPersistentData().getCompound(OWNED);
        if (!tag.hasUUID("Owner")) return false;
        Session s = sessions(level.getServer()).get(tag.getUUID("Owner"));
        return s != null && combatPaused(s);
    }

    /** Cloning replaces the ServerPlayer object. Recover through the new player's persisted return journal. */
    public static void playerCloned(ServerPlayer original) {
        Session s = sessions(original.getServer()).remove(original.getUUID());
        if (s == null) return;
        for (Actor actor : s.actors.values()) actor.mob.discard();
        for (Pickup pickup : s.pickups.values()) pickup.entity.discard();
        for (Projectile projectile : s.projectiles.values()) projectile.discard();
        s.run.finish();
    }

    private static CompoundTag buildSnapshot(Session s) {
        RushRun run = s.run;
        CompoundTag build = new CompoundTag();
        build.putDouble("DamageMultiplier", run.damageMultiplier()); build.putDouble("CritChance", run.criticalChance());
        build.putDouble("CritMultiplier", run.criticalMultiplier()); build.putDouble("AreaMultiplier", run.areaMultiplier());
        build.putDouble("CadenceMultiplier", run.cadenceMultiplier()); build.putDouble("PickupRadius", run.pickupRadius());
        build.putDouble("RecoveryPerSecond", run.recoveryPerSecond()); build.putDouble("ArtifactIntervalMultiplier", run.artifactIntervalMultiplier());
        build.putDouble("LineCharge", run.lineCharge()); build.putDouble("LineChargeMax", run.lineChargeMax());
        build.putBoolean("AirborneReady", run.rank(RushRun.Upgrade.AIR_BREAK) > 0 && s.motion.airborneReady());
        build.putInt("MarkedRivals", (int) s.actors.values().stream().filter(a -> a.exposedUntil > run.elapsedTicks()).count());
        build.putLong("AirBreakPulses", s.airBreakPulses); build.putLong("LineProcs", s.lineProcs);
        build.putLong("MarkApplications", s.markApplications);
        build.putDouble("Ward", run.ward()); build.putDouble("WardMax", run.maxWard());
        build.putInt("SoulBurstCharge", run.soulBurstCharge()); build.putInt("SoulBurstChargeMax", 20);
        build.putInt("SoulBurstCost", 5);
        build.putInt("VenomRivals", (int) s.actors.values().stream().filter(a -> a.venomRank > 0 && a.venomUntil >= run.elapsedTicks()).count());
        build.putInt("ChilledRivals", (int) s.actors.values().stream().filter(a -> a.chillUntil > run.elapsedTicks()).count());
        build.putLong("MultishotVolleys", s.multishotVolleys); build.putLong("RadiantPulses", s.radiantPulses);
        build.putLong("SoulBursts", s.soulBursts);
        build.putInt("ArtifactSlots", 3);
        build.putBoolean("RemixReady", run.remixReady() && betweenArenas(s) && !run.hasOffer());
        ListTag artifacts = new ListTag(), replacements = new ListTag();
        for (RushRun.Upgrade upgrade : RushRun.Upgrade.values()) {
            if (!run.artifactAvailable(upgrade)) continue;
            CompoundTag artifact = new CompoundTag(); artifact.putString("Id", upgrade.name());
            artifact.putString("Name", upgrade.label()); artifact.putString("Description", upgrade.description());
            artifact.putInt("Rank", run.rank(upgrade)); artifact.putInt("MaxRank", upgrade.maxRank());
            replacements.add(artifact.copy());
            if (run.rank(upgrade) > 0) artifacts.add(artifact);
        }
        build.put("Artifacts", artifacts); build.put("RemixChoices", replacements);
        return build;
    }

    private static void send(ServerPlayer player, boolean open, String summary, int albumPage, int rosterPage) {
        CompoundTag state = new CompoundTag();
        state.putBoolean("Open", open); state.putString("Summary", summary);
        Session s = session(player); state.putBoolean("Active", s != null);
        state.putLong("Cards", s == null ? RushAlbum.total(player) : s.cards);
        state.putLong("AlbumTotal", RushAlbum.total(player));
        if (s == null) state.put("ArenaCatalogue", RushArenaCatalog.summaries());
        if (s != null) {
            RushRun run = s.run;
            state.putUUID("Run", run.id()); state.putInt("Version", run.offerVersion());
            state.putString("Gang", s.gang.canonicalName()); state.putString("GangId", s.gang.id().toString());
            state.putString("Mob", s.roster.get(0).toString()); state.putBoolean("Endless", run.endless());
            state.putLong("Ticks", run.elapsedTicks()); state.putInt("Level", run.level());
            state.putLong("Xp", run.xp()); state.putLong("NextXp", run.xpToNextLevel());
            state.putDouble("Vitality", run.vitality()); state.putDouble("MaxVitality", run.maxVitality());
            state.putLong("Score", run.score()); state.putInt("Kills", (int) Math.min(Integer.MAX_VALUE, run.defeats()));
            state.putString("Objective", s.objective); state.putBoolean("Finale", run.awaitingEncore());
            if (s.circuit != null) state.put("Journey", s.circuit.snapshot(run, player.position()));
            state.put("Build", buildSnapshot(s));
            state.put("Caches", s.points.snapshot(player, run, s.circuit));
            ListTag offers = new ListTag();
            for (RushRun.Upgrade upgrade : run.offers()) {
                CompoundTag offer = new CompoundTag(); offer.putString("id", upgrade.name());
                offer.putString("name", upgrade.label()); offer.putString("description", upgrade.description());
                offer.putInt("rank", run.rank(upgrade)); offers.add(offer);
            }
            state.put("Offers", offers);
        }
        if (albumPage >= 0) {
            int pages = Math.max(1, (RushAlbum.uniqueCount(player) + PAGE_SIZE - 1) / PAGE_SIZE);
            int page = Math.min(albumPage, pages - 1);
            ListTag album = new ListTag();
            for (RushAlbum.Entry entry : RushAlbum.entries(player, page * PAGE_SIZE, PAGE_SIZE)) {
                CompoundTag card = new CompoundTag(); card.putString("mob", entry.entityId());
                card.putString("gang", entry.gangId()); card.putLong("count", entry.count()); card.putInt("tier", entry.tier());
                card.putLong("next", entry.nextThreshold());
                card.putString("name", entry.entityId().substring(entry.entityId().indexOf(':') + 1).replace('_', ' '));
                card.putString("reward", "Mastery increases Rush XP and score. Album drafts unlock at 20 / 60 / 150 cards.");
                album.add(card);
            }
            state.put("Album", album); state.putInt("Page", page); state.putInt("TotalPages", pages);
        }
        if (rosterPage >= 0) {
            var gangs = GangRegistry.curated().stream().sorted(Comparator.comparing(GangDefinition::canonicalName)).toList();
            int pages = Math.max(1, (gangs.size() + PAGE_SIZE - 1) / PAGE_SIZE);
            int page = Math.min(rosterPage, pages - 1);
            ListTag roster = new ListTag();
            for (int i = page * PAGE_SIZE; i < Math.min(gangs.size(), (page + 1) * PAGE_SIZE); i++) {
                GangDefinition gang = gangs.get(i); CompoundTag entry = new CompoundTag();
                entry.putString("id", gang.id().toString()); entry.putString("name", gang.canonicalName());
                entry.putString("mob", GangRegistry.representativeEntity(gang.id()).orElse(ResourceLocation.tryParse("minecraft:zombie")).toString());
                roster.add(entry);
            }
            state.put("Roster", roster); state.putInt("Page", page); state.putInt("TotalPages", pages);
        }
        JetSetNetwork.syncRush(player, state);
    }

    private static boolean fail(ServerPlayer player, String message) {
        player.sendSystemMessage(Component.literal(message).withStyle(ChatFormatting.YELLOW));
        send(player, true, message, -1, -1);
        return false;
    }

    private static void syncRide(ServerPlayer player) {
        player.getCapability(JetSetDataProvider.CAPABILITY).ifPresent(data -> JetSetNetwork.sync(player, data));
    }

    private RivalRushManager() {}
}
