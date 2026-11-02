package com.herberto.jetsetcraft.liberty;

import com.herberto.jetsetcraft.JetSetCraft;
import com.herberto.jetsetcraft.activity.ActivityManager;
import com.herberto.jetsetcraft.compat.LibertyTaczCompat;
import com.herberto.jetsetcraft.config.JetSetConfig;
import com.herberto.jetsetcraft.crew.CrewHouseData;
import com.herberto.jetsetcraft.crew.CrewHouseManager;
import com.herberto.jetsetcraft.data.JetSetDataProvider;
import com.herberto.jetsetcraft.entity.GraffitiEntity;
import com.herberto.jetsetcraft.entity.LibertyTransportEntity;
import com.herberto.jetsetcraft.gang.GangGearSelector;
import com.herberto.jetsetcraft.gang.GangMemberState;
import com.herberto.jetsetcraft.item.RideGearItem;
import com.herberto.jetsetcraft.item.SprayCanItem;
import com.herberto.jetsetcraft.mob.MobStreetGear;
import com.herberto.jetsetcraft.mob.StreetGearAcquisition;
import com.herberto.jetsetcraft.network.JetSetNetwork;
import com.herberto.jetsetcraft.registry.ModEntities;
import com.herberto.jetsetcraft.registry.ModItems;
import com.herberto.jetsetcraft.rush.RivalRushManager;
import com.herberto.jetsetcraft.rush.RushPlayerStorage;
import com.herberto.jetsetcraft.rush.RushReturnState;
import com.herberto.jetsetcraft.rush.RushWorld;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.Difficulty;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.GameType;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.event.ForgeEventFactory;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.WeakHashMap;

/** Authoritative drop/objectives/extraction lifecycle. TaCZ retains normal aim, fire and reload ownership. */
public final class LibertyManager {
    public static final String RETURN_KEY = "jetsetcraft:LibertyReturn";
    public static final String OUTCOME_KEY = "jetsetcraft:LibertyOutcome";
    public static final String DEBRIEF_KEY = "jetsetcraft:LibertyDebrief";
    public static final String OWNED = "jetsetcraft:liberty_owned";
    private static final Map<MinecraftServer, State> SERVERS = new WeakHashMap<>();
    private static final int ACTOR_LIMIT = 24;
    private static final class State {
        final Map<UUID, Session> sessions = new HashMap<>();
        final Map<UUID, Menu> menus = new HashMap<>();
        final Map<UUID, Long> actions = new HashMap<>();
        long nextMenu = 1;
    }
    private record Menu(long revision, long expires) {}
    private static final class Session {
        final ServerPlayer player;
        final LibertyOperation operation;
        final Map<UUID, Entity> entities = new LinkedHashMap<>();
        final Map<UUID, Integer> actors = new LinkedHashMap<>();
        final Set<Integer> built = new HashSet<>(), spawnedCaches = new HashSet<>();
        final Set<UUID> paidDefeats = new HashSet<>();
        LibertyTransportEntity transport;
        Vec3 previous;
        long lastSpawn = -100, lastResupply = -600;
        boolean exiting, landingBuilt;
        String status = "Pod incoming. Drop hard. Paint loud. Skate home.";
        Session(ServerPlayer player, LibertyOperation operation) { this.player = player; this.operation = operation; }
    }
    private static State state(MinecraftServer server) { return SERVERS.computeIfAbsent(server, ignored -> new State()); }
    private static Session session(Player player) {
        return player instanceof ServerPlayer server && server.getServer() != null ? state(server.getServer()).sessions.get(player.getUUID()) : null;
    }
    public static boolean active(Player player) { return session(player) != null; }
    public static boolean hasReturn(ServerPlayer player) {
        return player != null && (RushReturnState.hasJournal(player, RETURN_KEY)
                || persisted(player).contains(OUTCOME_KEY) || LibertyResearch.pendingMission(player).isPresent());
    }
    public static boolean restoring(ServerPlayer player) { Session s = session(player); return s != null && s.exiting; }
    public static LibertyOperation operation(ServerPlayer player) { Session s = session(player); return s == null ? null : s.operation; }
    public static boolean inTransit(ServerPlayer player) {
        LibertyOperation op = operation(player); return op != null && (op.phase() == LibertyOperation.Phase.DROPPING
                || op.phase() == LibertyOperation.Phase.DEPARTING || op.phase() == LibertyOperation.Phase.RETURNING);
    }
    private static CompoundTag persisted(ServerPlayer player) {
        CompoundTag root = player.getPersistentData();
        if (!root.contains(Player.PERSISTED_NBT_TAG, Tag.TAG_COMPOUND)) root.put(Player.PERSISTED_NBT_TAG, new CompoundTag());
        return root.getCompound(Player.PERSISTED_NBT_TAG);
    }
    private static long now(ServerPlayer player) { return player.getServer().overworld().getGameTime(); }

    public static void open(ServerPlayer player) {
        if (player == null || player.getServer() == null) return;
        if (!active(player) && hasReturn(player)) {
            recover(player);
            if (hasReturn(player)) { send(player, true, "A saved return or reward checkpoint still needs recovery. Use Retry return after its account is available."); return; }
        }
        if (!active(player)) state(player.getServer()).menus.put(player.getUUID(), new Menu(state(player.getServer()).nextMenu++, now(player) + 2400));
        send(player, true, "");
    }

    public static void handleAction(ServerPlayer player, String action, String value, UUID missionId, long revision) {
        if (player == null || player.getServer() == null || action == null || value == null || action.length() > 32 || value.length() > 128) return;
        State data = state(player.getServer()); long tick = now(player);
        if (tick < data.actions.getOrDefault(player.getUUID(), tick - 2) + 2) return;
        data.actions.put(player.getUUID(), tick);
        if (action.equals("open")) { open(player); return; }
        Session s = session(player);
        if (action.equals("research") || action.equals("research_select")) {
            boolean accepted = action.equals("research") ? LibertyResearch.purchase(player, value, revision)
                    : LibertyResearch.selectPreparation(player, value, revision);
            if (accepted && !RushPlayerStorage.flush(player)) { send(player, true, "Research is updated in memory; reconnect after the server can save player data."); return; }
            send(player, true, accepted ? action.equals("research") ? "Research completed. Your new technique is ready." : "Next Rush preparation selected."
                    : "That research choice is stale, locked, unaffordable or unavailable during an expedition. Review the refreshed ledger."); return;
        }
        if (action.equals("deploy")) {
            Menu menu = data.menus.get(player.getUUID());
            if (missionId != null || menu == null || revision != menu.revision || tick > menu.expires) {
                open(player); send(player, true, "Review the refreshed war map before deploying."); return;
            }
            start(player, value); return;
        }
        if (s == null) { if (action.equals("abort")) recover(player); else open(player); return; }
        if (!s.operation.matches(missionId, revision)) { send(player, true, "Mission state changed. Review the current operation."); return; }
        switch (action) {
            case "abort" -> leave(player, false, "Operation aborted. Earned super credits and objective crew progress were retained; field samples were left behind.");
            case "extract" -> {
                if (s.operation.extract(player.position())) { s.status = "Extraction called. Hold within 48 blocks of the pad for 30 seconds."; send(player, false, ""); }
                else send(player, true, "Finish all three objectives, then call extraction within 12 blocks of the landing pad.");
            }
            case "board" -> {
                if (s.transport != null && s.transport.isAlive() && s.operation.board(player.position())) {
                    s.status = "All aboard. Liberty is leaving the station.";
                    player.startRiding(s.transport, true); send(player, false, "");
                } else send(player, true, "Approach the landed ship within eight blocks to board.");
            }
            case "resupply" -> resupply(s);
            default -> { }
        }
    }

    public static String unavailable(ServerPlayer player) {
        if (!JetSetConfig.SERVER.enableLibertyGrind.get()) return "Liberty Grind is disabled by this server.";
        if (!JetSetConfig.SERVER.allowGraffiti.get()) return "Enable graffiti on this server to complete Liberty Grind objectives.";
        if (active(player) || hasReturn(player)) return "Return from your current Liberty operation first.";
        if (ActivityManager.active(player) || RivalRushManager.active(player) || RushReturnState.hasJournal(player)
                || RushWorld.isRush(player.level()) || LibertyWorld.isLiberty(player.level())) return "Finish your current activity and return to Minecraft first.";
        if (!player.isAlive() || player.isSleeping() || player.isPassenger()) return "Stand safely on your own before deploying.";
        if (!player.containerMenu.getCarried().isEmpty()) return "Put the item on your cursor away before deploying.";
        if (player.serverLevel().getDifficulty() == Difficulty.PEACEFUL) return "Liberty Grind needs Easy, Normal or Hard. World difficulty is unchanged.";
        if (player.getServer().getLevel(LibertyWorld.DIMENSION) == null) return "The Liberty Grind dimension is unavailable. Reopen the world with its bundled data pack enabled.";
        if (CrewHouseData.get(player.getServer()).blocked(player.getUUID()) || LibertyResearch.snapshot(player).getBoolean("Blocked"))
            return "Your progression record needs recovery or a newer compatible mod version. Existing data was retained.";
        return LibertyTaczCompat.available() ? "" : LibertyTaczCompat.status();
    }

    /** Explicit server command and war-map deployment share this entry path. */
    public static boolean start(ServerPlayer player, String theaterId) {
        if (player == null || player.getServer() == null) return false;
        String blocked = unavailable(player);
        if (!blocked.isEmpty()) return fail(player, blocked);
        LibertyOperation.Theater theater = LibertyOperation.theater(theaterId);
        if (theater == null) return fail(player, "Choose an available war-map district.");
        State data = state(player.getServer()); int slot = data.sessions.size() >= 32 ? -1 : LibertyDistricts.get(player.getServer()).allocate();
        if (slot < 0) return fail(player, "All expedition districts are occupied. Wait for a crew to return.");
        if (!LibertyDistricts.saveAcknowledged(player.getServer()))
            return fail(player, "The server could not save your district allocation. No gear or location was changed.");
        UUID mission = UUID.randomUUID();
        if (!RushReturnState.capture(player, RETURN_KEY)) return fail(player, "Could not preserve your return checkpoint. No gear or location was changed.");
        if (!LibertyResearch.begin(player, mission)) {
            persisted(player).remove(RETURN_KEY); return fail(player, "Research settlement needs recovery before another expedition.");
        }
        BlockPos origin = new BlockPos(1_000_008 + slot % 512 * 1024, LibertyWorld.FLOOR_Y, 1_000_008 + slot / 512 * 1024);
        LibertyOperation op = new LibertyOperation(mission, player.getUUID(), theater, origin, slot,
                LibertyResearch.unlocked(player, "signal_lacquer"), LibertyResearch.unlocked(player, "supply_skates"));
        Session s = new Session(player, op); writeOutcome(s, false);
        // The ordinary inventory is durable before any item is lent or dimension transfer occurs.
        if (!RushPlayerStorage.flush(player)) {
            LibertyResearch.abandonPending(player); persisted(player).remove(RETURN_KEY); persisted(player).remove(OUTCOME_KEY);
            return fail(player, "The server could not save your checkpoint. Your Minecraft belongings were not changed.");
        }
        data.sessions.put(player.getUUID(), s); data.menus.remove(player.getUUID());
        try {
            ItemStack ride = player.getCapability(JetSetDataProvider.CAPABILITY).map(value -> value.rideGear().copy()).orElse(ItemStack.EMPTY);
            if (ride.isEmpty()) ride = new ItemStack(ModItems.INLINE_SKATES.get());
            LibertyTaczCompat.markLoan(ride, player.getUUID());
            player.getInventory().clearContent(); player.containerMenu.setCarried(ItemStack.EMPTY);
            if (!LibertyTaczCompat.provision(player)) throw new IllegalStateException("Helldivers loadout changed while deploying");
            ItemStack spray = new ItemStack(ModItems.SPRAY_CAN.get()); spray.getOrCreateTag().putBoolean("Unbreakable", true);
            LibertyTaczCompat.markLoan(spray, player.getUUID()); player.getInventory().setItem(2, spray);
            ItemStack food = new ItemStack(Items.COOKED_BEEF, 16); LibertyTaczCompat.markLoan(food, player.getUUID()); player.getInventory().setItem(3, food);
            ItemStack radio = new ItemStack(Items.COMPASS); radio.setHoverName(Component.literal("Liberty field radio"));
            LibertyTaczCompat.markLoan(radio, player.getUUID()); radio.getOrCreateTag().putBoolean("LibertyFieldRadio", true);
            player.getInventory().setItem(4, radio);
            ItemStack helmet = new ItemStack(Items.IRON_HELMET), chest = new ItemStack(Items.IRON_CHESTPLATE), legs = new ItemStack(Items.IRON_LEGGINGS);
            for (ItemStack stack : List.of(helmet, chest, legs)) LibertyTaczCompat.markLoan(stack, player.getUUID());
            player.setItemSlot(EquipmentSlot.HEAD, helmet); player.setItemSlot(EquipmentSlot.CHEST, chest); player.setItemSlot(EquipmentSlot.LEGS, legs);
            ItemStack equipped = ride;
            player.getCapability(JetSetDataProvider.CAPABILITY).ifPresent(value -> {
                value.load(new CompoundTag()); value.setRideGear(equipped);
                if (equipped.getItem() instanceof RideGearItem gear) value.setStyle(gear.style());
                value.setActive(true); value.setBoost(100);
            });
            player.setGameMode(GameType.SURVIVAL); player.removeAllEffects(); player.setHealth(player.getMaxHealth());
            player.setAbsorptionAmount(0); player.clearFire(); player.getFoodData().setFoodLevel(20); player.getFoodData().setSaturation(6);
            player.fallDistance = 0; player.getInventory().selected = 0;
            ServerLevel district = player.getServer().getLevel(LibertyWorld.DIMENSION);
            player.teleportTo(district, origin.getX() + .5, LibertyWorld.FLOOR_Y + 66, origin.getZ() + .5, 0, 0);
            s.transport = transport(s, false, player.position()); player.startRiding(s.transport, true);
            syncRide(player); player.inventoryMenu.broadcastChanges(); send(player, false, ""); return true;
        } catch (RuntimeException error) {
            JetSetCraft.LOGGER.error("Liberty Grind deployment interrupted for {}; restoring checkpoint", player.getUUID(), error);
            leave(player, false, "Deployment interrupted. Your ordinary Minecraft state was restored."); return false;
        }
    }

    private static LibertyTransportEntity transport(Session s, boolean ship, Vec3 position) {
        LibertyTransportEntity transport = new LibertyTransportEntity(ModEntities.LIBERTY_TRANSPORT.get(), s.player.serverLevel());
        transport.setShip(ship); transport.moveTo(position.x, position.y, position.z, 0, 0); mark(transport, s, -1);
        if (!s.player.serverLevel().addFreshEntity(transport)) throw new IllegalStateException("Transport spawn rejected");
        return transport;
    }

    public static void tick(MinecraftServer server) {
        for (Session s : List.copyOf(state(server).sessions.values())) if (!s.exiting) {
            try { tick(s); }
            catch (RuntimeException error) { JetSetCraft.LOGGER.error("Liberty Grind interrupted for {}", s.player.getUUID(), error);
                leave(s.player, false, "Operation interrupted; your return checkpoint was preserved."); }
        }
    }
    private static void tick(Session s) {
        ServerPlayer player = s.player; LibertyOperation op = s.operation; ServerLevel level = player.serverLevel();
        if (!LibertyWorld.isLiberty(level)) { leave(player, false, "Left the expedition district. Your Minecraft state was restored."); return; }
        if (op.phase() == LibertyOperation.Phase.RETURNING) { leave(player, false, "Retrying your saved return."); return; }
        op.tick();
        if (op.expired()) { leave(player, false, "Mission time expired. Earned credits and crew progress retained; field samples lost."); return; }
        LibertyWorld.decorateNear(level, player.blockPosition(), op);
        if (!s.landingBuilt) s.landingBuilt = LibertyWorld.buildLanding(level, op.extraction);
        s.entities.entrySet().removeIf(entry -> entry.getValue().isRemoved());
        s.actors.entrySet().removeIf(entry -> !s.entities.containsKey(entry.getKey()));
        switch (op.phase()) {
            case DROPPING -> {
                double t = Math.min(1, op.phaseTicks() / 80.0), y = LibertyWorld.FLOOR_Y + 1 + 65 * (1 - t) * (1 - t);
                moveTransport(s, op.origin, y, true);
                if (op.phaseTicks() >= 80) {
                    op.phase(LibertyOperation.Phase.ACTIVE);
                    player.stopRiding(); relocate(player, op.origin.getX() + .5, LibertyWorld.FLOOR_Y + 1.1, op.origin.getZ() + 2.5);
                    s.transport.discard(); s.transport = null; player.setDeltaMovement(Vec3.ZERO); player.fallDistance = 0;
                    level.sendParticles(ParticleTypes.CLOUD, player.getX(), LibertyWorld.FLOOR_Y + 1, player.getZ(), 32, 2, .2, 2, .02);
                    level.playSound(null, player.blockPosition(), SoundEvents.GENERIC_EXPLODE, SoundSource.PLAYERS, .65f, .8f);
                    s.status = "Find the yellow mural panels. Clear rivals, tag the walls and keep moving."; send(player, false, "");
                }
            }
            case EXTRACTING -> {
                double distance = player.position().distanceToSqr(Vec3.atCenterOf(op.extraction));
                if (distance > 48 * 48) { op.phase(LibertyOperation.Phase.ACTIVE); s.status = "Extraction cancelled: you left the landing zone. Call again at the pad."; }
                else if (op.phaseTicks() >= 600) {
                    s.transport = transport(s, true, Vec3.atCenterOf(op.extraction).add(0, 16, 0));
                    op.phase(LibertyOperation.Phase.BOARDING); s.status = "Ship arriving. Approach the rear ramp and choose Board ship.";
                }
            }
            case BOARDING -> {
                moveTransport(s, op.extraction, LibertyWorld.FLOOR_Y + 1 + 16 * (1 - Math.min(1, op.phaseTicks() / 60.0)), false);
                if (op.phaseTicks() >= 1800) { s.transport.discard(); s.transport = null; op.phase(LibertyOperation.Phase.ACTIVE); s.status = "Ship departed empty. Your objectives remain complete; call again."; }
            }
            case DEPARTING -> {
                moveTransport(s, op.extraction, LibertyWorld.FLOOR_Y + 1 + op.phaseTicks() * .6, true);
                if (op.phaseTicks() >= 100) { leave(player, true, "Extraction complete. Samples delivered, research stocked and your crew rewarded."); return; }
            }
            default -> { }
        }
        if (op.field()) {
            if (!LibertyWorld.inside(op, player.blockPosition()) || player.getY() < 50 || player.getY() > 180) {
                relocate(player, op.origin.getX() + .5, LibertyWorld.FLOOR_Y + 1.1, op.origin.getZ() + .5);
                player.setDeltaMovement(Vec3.ZERO); player.fallDistance = 0; s.previous = null; s.status = "Stay inside the expedition district. Returned to the drop zone.";
            }
            boolean skating = player.getCapability(JetSetDataProvider.CAPABILITY).map(value -> value.active() && !value.rideGear().isEmpty()).orElse(false)
                    && !player.isPassenger() && !player.getAbilities().flying;
            for (LibertyOperation.Objective objective : op.objectives) {
                if (!s.built.contains(objective.index()) && player.distanceToSqr(Vec3.atCenterOf(objective.center())) < 100 * 100
                        && LibertyWorld.buildObjective(level, objective)) s.built.add(objective.index());
                objective.advance(player.position(), s.previous, skating);
            }
            s.previous = player.position();
            if (op.awardObjectives()) { s.status = op.complete() ? "All objectives complete. Head to the landing pad to extract your samples." : "Objective liberated. Research gathered and one resupply charge restored."; checkpoint(s); }
            if (session(player) != s || !op.field()) return;
            if (op.ticks() - s.lastSpawn >= 20) { s.lastSpawn = op.ticks(); spawnRival(s); }
            if (op.ticks() % 10 == 0) {
                caches(s);
                if (session(player) != s || !op.field() || !LibertyWorld.isLiberty(player.level())) return;
                guidance(s);
            }
            if (op.ticks() % 20 == 0) { player.getFoodData().setFoodLevel(20); player.getFoodData().setSaturation(6); }
        }
        if (op.ticks() % 10 == 0) send(player, false, "");
    }
    private static void moveTransport(Session s, BlockPos destination, double y, boolean passenger) {
        if (s.transport == null || !s.transport.isAlive()) throw new IllegalStateException("Mission transport is missing");
        s.transport.setPos(destination.getX() + .5, y, destination.getZ() + .5); s.transport.setDeltaMovement(Vec3.ZERO);
        s.transport.hasImpulse = true;
        if (passenger && s.player.getVehicle() != s.transport) s.player.startRiding(s.transport, true);
        s.player.fallDistance = 0;
    }
    /** Server position is canonical even for actors without a network connection, as in return-journal tests. */
    private static void relocate(ServerPlayer player, double x, double y, double z) {
        player.moveTo(x, y, z, player.getYRot(), player.getXRot());
        if (JetSetNetwork.canSync(player)) player.teleportTo(x, y, z);
        movementDiscontinuity(player);
    }

    private static void spawnRival(Session s) {
        if (!LibertyWorld.isLiberty(s.player.level()) || !s.operation.field() || session(s.player) != s) return;
        if (s.actors.size() >= ACTOR_LIMIT) return;
        LibertyOperation.Objective target = null;
        for (LibertyOperation.Objective objective : s.operation.objectives) {
            long alive = s.actors.values().stream().filter(index -> index == objective.index()).count();
            if (s.built.contains(objective.index()) && objective.defeats() + alive < objective.defenders()
                    && s.player.distanceToSqr(Vec3.atCenterOf(objective.center())) < 75 * 75) { target = objective; break; }
        }
        boolean extraction = s.operation.phase() == LibertyOperation.Phase.EXTRACTING || s.operation.phase() == LibertyOperation.Phase.BOARDING;
        if (target == null && (s.operation.ticks() % (extraction ? 60 : 240) != 0 || s.actors.size() >= (extraction ? 12 : 6))) return;
        BlockPos center = target == null ? extraction ? s.operation.extraction : s.player.blockPosition().atY(LibertyWorld.FLOOR_Y) : target.center();
        int number = (int) (s.operation.ticks() / 20), objectiveIndex = target == null ? -1 : target.index();
        double angle = number * 2.399963229728653;
        BlockPos pos = center.offset((int) (Math.cos(angle) * 13), 1, (int) (Math.sin(angle) * 13));
        ServerLevel level = s.player.serverLevel();
        if (level.getChunkSource().getChunkNow(pos.getX() >> 4, pos.getZ() >> 4) == null) return;
        EntityType<?> type = ForgeRegistries.ENTITY_TYPES.getValue(s.operation.theater.speciesId());
        Entity raw = type == null ? null : type.create(level);
        if (!(raw instanceof Mob mob)) { if (raw != null) raw.discard(); return; }
        mob.moveTo(pos.getX() + .5, pos.getY(), pos.getZ() + .5, 0, 0);
        if (!level.noCollision(mob)) { mob.discard(); return; }
        ForgeEventFactory.onFinalizeSpawn(mob, level, level.getCurrentDifficultyAt(pos), MobSpawnType.EVENT, null, null);
        mob.setPersistenceRequired(); mob.setCanPickUpLoot(false);
        for (EquipmentSlot slot : EquipmentSlot.values()) mob.setDropChance(slot, 0);
        if (mob.getAttribute(Attributes.MAX_HEALTH) != null) mob.getAttribute(Attributes.MAX_HEALTH).setBaseValue(28 + s.operation.theater.defenders() * 3);
        mob.setHealth(mob.getMaxHealth()); mob.setTarget(s.player);
        mark(mob, s, objectiveIndex);
        if (!level.addFreshEntity(mob)) { s.entities.remove(mob.getUUID()); mob.discard(); return; }
        s.actors.put(mob.getUUID(), objectiveIndex);
        MobStreetGear.equip(mob, GangGearSelector.forActor(mob, number), StreetGearAcquisition.GANG_EVENT, false);
        GangMemberState.attach(mob, s.operation.theater.gangId(), "liberty_rival", s.operation.id, true, level.getGameTime() + 50_000);
    }

    public static void defeated(Mob mob, Entity killer) {
        Session s = ownedSession(mob);
        if (s == null || !s.operation.field() || !s.paidDefeats.add(mob.getUUID())) return;
        Integer objective = s.actors.remove(mob.getUUID());
        boolean owner = killer instanceof ServerPlayer player && player.getUUID().equals(s.player.getUUID());
        if (objective != null && objective >= 0 && owner) s.operation.objectives.get(objective).defeated(mob.getUUID());
        // Uncredited environmental/foreign deaths are replaced; they never complete an objective.
    }

    public static boolean tagWall(ServerPlayer player, BlockPos support, Direction face) {
        Session s = session(player); if (s == null || !s.operation.field() || support == null) return false;
        for (LibertyOperation.Objective objective : s.operation.objectives) {
            if (s.built.contains(objective.index()) && objective.paint(support, face, player.position())) {
                s.status = objective.description(); s.operation.awardObjectives();
                if (checkpoint(s)) send(player, false, ""); return true;
            }
        }
        return false;
    }

    private static void caches(Session s) {
        if (!LibertyWorld.isLiberty(s.player.level()) || !s.operation.field() || session(s.player) != s) return;
        s.spawnedCaches.removeIf(index -> !s.operation.cacheClaimed(index) && s.entities.values().stream().noneMatch(entity ->
                entity instanceof ItemEntity && !entity.isRemoved() && entity.getPersistentData().getCompound(OWNED).getInt("Cache") == index));
        for (int i = 0; i < 9; i++) {
            if (s.operation.cacheClaimed(i) || s.spawnedCaches.contains(i)) continue;
            BlockPos pos = s.operation.cachePosition(i);
            if (s.player.distanceToSqr(Vec3.atCenterOf(pos)) > 70 * 70
                    || s.player.serverLevel().getChunkSource().getChunkNow(pos.getX() >> 4, pos.getZ() >> 4) == null) continue;
            ItemStack icon = new ItemStack(i < 3 ? Items.GOLD_INGOT : i < 6 ? Items.SLIME_BALL : i < 8 ? Items.PRISMARINE_CRYSTALS : Items.AMETHYST_SHARD);
            ItemEntity cache = new ItemEntity(s.player.serverLevel(), pos.getX() + .5, pos.getY() + 1.2, pos.getZ() + .5, icon);
            cache.setNoGravity(true); cache.setDeltaMovement(Vec3.ZERO); cache.setGlowingTag(true); cache.setUnlimitedLifetime();
            cache.setCustomName(Component.literal(i < 3 ? "Super-credit capsule" : i < 6 ? "Common pigment samples" : i < 8 ? "Rare resonant samples" : "Super chromatic sample"));
            cache.setCustomNameVisible(true); mark(cache, s, -1); cache.getPersistentData().getCompound(OWNED).putInt("Cache", i);
            if (s.player.serverLevel().addFreshEntity(cache)) s.spawnedCaches.add(i); else s.entities.remove(cache.getUUID());
        }
        for (Entity entity : List.copyOf(s.entities.values())) if (entity instanceof ItemEntity item && item.distanceToSqr(s.player) < 3)
            collect(s.player, item);
    }
    public static boolean collect(ServerPlayer player, ItemEntity item) {
        Session s = session(player);
        if (s == null || !belongsTo(item, player) || item.distanceToSqr(player) > 9) return false;
        CompoundTag marker = item.getPersistentData().getCompound(OWNED);
        if (!marker.contains("Cache", Tag.TAG_INT) || !s.operation.cache(marker.getInt("Cache"))) return false;
        item.discard(); if (!checkpoint(s)) return true;
        player.serverLevel().playSound(null, player.blockPosition(), SoundEvents.EXPERIENCE_ORB_PICKUP, SoundSource.PLAYERS, .4f, 1.2f);
        s.status = "Cache secured. Super credits stay earned; samples must reach the extraction ship."; return true;
    }
    private static void resupply(Session s) {
        if (!s.operation.field() || s.operation.resupplies() <= 0 || s.operation.ticks() < s.lastResupply + 600) {
            send(s.player, true, "Resupply needs a charge and a 30-second cooldown. Complete objectives to recover charges."); return;
        }
        if (!LibertyTaczCompat.resupply(s.player)) { send(s.player, true, "Make room for magazine supplies, and check the installed Helldivers pack."); return; }
        s.operation.spendResupply(); s.lastResupply = s.operation.ticks(); s.player.heal(8);
        s.player.getCapability(JetSetDataProvider.CAPABILITY).ifPresent(value -> value.setBoost(100));
        s.status = "Supply signal answered: reserve magazines topped up, health +8 and boost refilled. Reload normally."; send(s.player, false, "");
    }
    private static void guidance(Session s) {
        for (LibertyOperation.Objective objective : s.operation.objectives) if (!objective.complete()) {
            BlockPos target = objective.index() == 2 && objective.paints() >= objective.panels() && objective.relay() < 3 ? objective.beacon(objective.relay()) : objective.center();
            if (s.player.distanceToSqr(Vec3.atCenterOf(target)) < 100 * 100)
                s.player.serverLevel().sendParticles(s.player, ParticleTypes.END_ROD, true, target.getX() + .5, target.getY() + 4, target.getZ() + .5, 5, .3, 1.5, .3, .01);
        }
        if (s.operation.complete()) s.player.serverLevel().sendParticles(s.player, ParticleTypes.HAPPY_VILLAGER, true,
                s.operation.extraction.getX() + .5, LibertyWorld.FLOOR_Y + 2, s.operation.extraction.getZ() + .5, 8, 3, .5, 3, .02);
    }

    private static boolean checkpoint(Session s) {
        writeOutcome(s, false);
        if (!RushPlayerStorage.flush(s.player)) {
            s.status = "Player saves failed. Your return checkpoint is retained; the operation is ending safely.";
            leave(s.player, false, s.status);
        }
        return session(s.player) == s && s.operation.field() && LibertyWorld.isLiberty(s.player.level());
    }
    private static void writeOutcome(Session s, boolean extracted) {
        CompoundTag tag = new CompoundTag(); LibertyOperation op = s.operation;
        tag.putInt("Schema", 1); tag.putUUID("Owner", s.player.getUUID()); tag.putUUID("Run", op.id);
        tag.putString("Gang", op.theater.gang()); tag.putString("Theater", op.theater.name());
        tag.putInt("Completed", op.completed()); tag.putInt("Credits", op.credits());
        tag.putInt("Common", op.common()); tag.putInt("Rare", op.rare()); tag.putInt("Super", op.supers()); tag.putBoolean("Extracted", extracted);
        persisted(s.player).put(OUTCOME_KEY, tag);
    }
    private static boolean validOutcome(ServerPlayer player, CompoundTag tag) {
        if (tag.getInt("Schema") != 1 || !tag.hasUUID("Owner") || !tag.getUUID("Owner").equals(player.getUUID()) || !tag.hasUUID("Run")
                || ResourceLocation.tryParse(tag.getString("Gang")) == null) return false;
        for (String key : List.of("Completed", "Credits", "Common", "Rare", "Super"))
            if (!tag.contains(key, Tag.TAG_INT) || tag.getInt(key) < 0 || tag.getInt(key) > 1000) return false;
        return tag.getInt("Completed") <= 3 && (!tag.getBoolean("Extracted") || tag.getInt("Completed") == 3);
    }

    public static boolean leave(ServerPlayer player, boolean extracted, String message) {
        if (player == null || player.getServer() == null) return false;
        Session s = session(player);
        if (s != null && s.exiting) return false;
        if (s != null) {
            s.exiting = true;
            if (s.operation.phase() != LibertyOperation.Phase.RETURNING) {
                extracted = extracted && s.operation.phase() == LibertyOperation.Phase.DEPARTING && s.operation.complete();
                writeOutcome(s, extracted); s.operation.phase(LibertyOperation.Phase.RETURNING);
            }
            player.stopRiding(); s.entities.values().forEach(Entity::discard); s.entities.clear(); s.actors.clear();
        }
        CompoundTag outcome = persisted(player).getCompound(OUTCOME_KEY);
        boolean hasOutcome = persisted(player).contains(OUTCOME_KEY);
        if (hasOutcome && !validOutcome(player, outcome)) {
            // Restore belongings even when optional rewards need manual recovery; do not overwrite unknown data.
            if (RushReturnState.hasJournal(player, RETURN_KEY)) RushReturnState.restore(player, RETURN_KEY);
            if (s != null) state(player.getServer()).sessions.remove(player.getUUID());
            RushPlayerStorage.flush(player); return fail(player, "Minecraft state restored. Your expedition reward record was retained for recovery.");
        }
        if (hasOutcome && LibertyResearch.pendingMission(player).filter(outcome.getUUID("Run")::equals).isPresent() && !LibertyResearch.staged(player)) {
            boolean success = outcome.getBoolean("Extracted");
            if (!LibertyResearch.stageSettlement(player, outcome.getUUID("Run"), outcome.getInt("Credits"),
                    success ? outcome.getInt("Common") : 0, success ? outcome.getInt("Rare") : 0, success ? outcome.getInt("Super") : 0)) {
                if (s != null) s.exiting = false; return fail(player, "Your reward checkpoint is retained. Retry Return after progression recovery.");
            }
        }
        if (!RushPlayerStorage.flush(player)) { if (s != null) s.exiting = false; return fail(player, "The server cannot save the return checkpoint yet. Your recovery journal is retained."); }
        boolean restored = !RushReturnState.hasJournal(player, RETURN_KEY) || RushReturnState.restore(player, RETURN_KEY);
        if (!restored) { if (s != null) s.exiting = false; return fail(player, "Your Minecraft return journal is retained. Retry Return or reconnect."); }
        if (hasOutcome && (LibertyResearch.snapshot(player).getBoolean("Blocked")
                || !LibertyResearch.pendingMission(player).map(pending -> pending.equals(outcome.getUUID("Run")))
                        .orElseGet(() -> LibertyResearch.lastSettled(player, outcome.getUUID("Run"))))) {
            state(player.getServer()).sessions.remove(player.getUUID()); RushPlayerStorage.flush(player);
            return fail(player, "Minecraft state restored. Your research payout intent is retained until its account can be recovered.");
        }
        try {
            if (LibertyResearch.staged(player) && !LibertyResearch.settlePending(player))
                throw new IllegalStateException("Prepared research settlement was not accepted");
            else if (!hasOutcome) LibertyResearch.abandonPending(player);
            if (hasOutcome) {
                if (CrewHouseData.get(player.getServer()).blocked(player.getUUID()))
                    throw new IllegalStateException("Shared crew progression is retained for a compatible recovery");
                CrewHouseManager.bankLiberty(player, outcome.getUUID("Run"), ResourceLocation.tryParse(outcome.getString("Gang")),
                        outcome.getInt("Completed"), outcome.getBoolean("Extracted"));
                if (!CrewHouseData.saveAcknowledged(player.getServer()))
                    throw new IllegalStateException("Shared crew reward checkpoint was not acknowledged on disk");
                outcome.putString("Summary", message == null ? "Returned from Liberty Grind." : message);
                persisted(player).put(DEBRIEF_KEY, outcome.copy()); persisted(player).remove(OUTCOME_KEY);
            }
        } catch (RuntimeException error) {
            JetSetCraft.LOGGER.error("Liberty Grind optional reward settlement retained for {}", player.getUUID(), error);
            if (s != null) state(player.getServer()).sessions.remove(player.getUUID());
            RushPlayerStorage.flush(player); return fail(player, "Minecraft state restored; your reward intent is retained for the next recovery attempt.");
        }
        state(player.getServer()).sessions.remove(player.getUUID());
        if (!RushPlayerStorage.flush(player)) {
            if (JetSetNetwork.canSync(player)) player.connection.disconnect(Component.literal("Liberty Grind restored your belongings, but the server cannot save them yet. Reconnect after player saves are available."));
            return false;
        }
        syncRide(player); open(player); send(player, true, message == null ? "Returned from Liberty Grind." : message); return true;
    }
    public static void recover(ServerPlayer player) {
        Session existing = session(player);
        if (existing != null && existing.player != player) forget(existing.player);
        else if (existing != null) return;
        if (hasReturn(player)) leave(player, false, "Recovered an interrupted expedition. Earned credits and completed objective rewards were retained.");
        else if (LibertyWorld.isLiberty(player.level())) {
            ServerLevel home = player.getServer().overworld(); BlockPos spawn = home.getSharedSpawnPos();
            player.teleportTo(home, spawn.getX() + .5, spawn.getY(), spawn.getZ() + .5, 0, 0);
        }
    }
    public static void dimensionChanged(ServerPlayer player) {
        Session s = session(player);
        if (s != null && !s.exiting && !LibertyWorld.isLiberty(player.level())) leave(player, false, "Returned after leaving the expedition district.");
    }
    public static void movementDiscontinuity(ServerPlayer player) { Session s = session(player); if (s != null) s.previous = null; }
    public static void forget(ServerPlayer player) {
        if (player.getServer() == null) return;
        Session s = state(player.getServer()).sessions.remove(player.getUUID());
        if (s != null) s.entities.values().forEach(Entity::discard);
        state(player.getServer()).menus.remove(player.getUUID()); state(player.getServer()).actions.remove(player.getUUID());
    }
    public static void stop(MinecraftServer server) {
        for (Session s : List.copyOf(state(server).sessions.values())) leave(s.player, false, "Server stopped. Your original world and earned credits were preserved.");
        SERVERS.remove(server);
    }

    private static void mark(Entity entity, Session s, int objective) {
        CompoundTag tag = new CompoundTag(); tag.putUUID("Owner", s.player.getUUID()); tag.putUUID("Run", s.operation.id); tag.putInt("Objective", objective);
        entity.getPersistentData().put(OWNED, tag); s.entities.put(entity.getUUID(), entity);
    }
    public static boolean owned(Entity entity) { return entity != null && entity.getPersistentData().contains(OWNED, Tag.TAG_COMPOUND); }
    private static Session ownedSession(Entity entity) {
        if (!owned(entity) || entity.level().isClientSide || !(entity.level() instanceof ServerLevel level)) return null;
        CompoundTag tag = entity.getPersistentData().getCompound(OWNED);
        if (!tag.hasUUID("Owner") || !tag.hasUUID("Run")) return null;
        Session s = state(level.getServer()).sessions.get(tag.getUUID("Owner"));
        return s != null && s.operation.id.equals(tag.getUUID("Run")) && !s.exiting
                && s.entities.get(entity.getUUID()) == entity ? s : null;
    }
    public static boolean validOwned(Entity entity) { return LibertyWorld.isLiberty(entity.level()) && ownedSession(entity) != null; }
    public static boolean belongsTo(Entity entity, ServerPlayer player) {
        Session s = ownedSession(entity); return s != null && s.player.getUUID().equals(player.getUUID());
    }
    public static void markProjectile(Projectile projectile) {
        Entity owner = projectile.getOwner(); Session s = owner instanceof ServerPlayer player ? session(player) : ownedSession(owner);
        if (s != null && !s.exiting && s.entities.values().stream().filter(entity -> entity instanceof Projectile && !entity.isRemoved()).count() < 128)
            mark(projectile, s, -1);
    }
    public static void markGraffiti(GraffitiEntity graffiti) {
        if (!(graffiti.level() instanceof ServerLevel level)) return;
        for (Session s : state(level.getServer()).sessions.values()) if (!s.exiting && LibertyWorld.inside(s.operation, graffiti.blockPosition())) { mark(graffiti, s, -1); return; }
    }
    public static boolean allowedDamage(Entity target, Entity attacker, Entity direct) {
        Session receiver = target instanceof ServerPlayer player ? session(player) : ownedSession(target);
        Session source = attacker instanceof ServerPlayer player ? session(player) : ownedSession(attacker);
        if (source == null) source = ownedSession(direct);
        if (receiver != null) {
            if (receiver.exiting || !receiver.operation.field()) return false;
            if (target instanceof ServerPlayer && attacker instanceof Player) return false;
            return attacker == null && direct == null || source == receiver;
        }
        return source == null; // expedition actors/bullets cannot damage anyone outside their mission
    }

    public static CompoundTag snapshot(ServerPlayer player) {
        CompoundTag tag = new CompoundTag(); Session s = session(player); tag.putBoolean("Active", s != null);
        tag.put("Research", LibertyResearch.snapshot(player));
        if (s == null) {
            Menu menu = state(player.getServer()).menus.get(player.getUUID()); tag.putLong("Revision", menu == null ? 0 : menu.revision);
            String blocked = unavailable(player); tag.putString("Status", blocked.isEmpty() ? "Choose your operation. Super credits are earned in play; samples need extraction." : blocked);
            ListTag theaters = new ListTag();
            for (LibertyOperation.Theater theater : LibertyOperation.THEATERS) {
                CompoundTag row = new CompoundTag(); row.putString("Id", theater.id()); row.putString("Name", theater.name());
                row.putString("Description", theater.description()); row.putString("Gang", theater.gang().replace("jetsetcraft:", "").replace('_', ' '));
                row.putString("Reward", "~" + theater.reward() + " objective super credits + caches; three sample tiers and shared crew training");
                row.putInt("Radius", theater.radius()); row.putBoolean("Available", blocked.isEmpty()); row.putString("Reason", blocked); theaters.add(row);
            }
            tag.put("Theaters", theaters); tag.put("Debrief", persisted(player).getCompound(DEBRIEF_KEY).copy());
            tag.putBoolean("CanAbort", hasReturn(player)); return tag;
        }
        LibertyOperation op = s.operation; tag.putUUID("Mission", op.id); tag.putLong("Revision", op.revision()); tag.putString("Phase", op.phase().name());
        tag.putString("Status", s.status); tag.putString("Theater", op.theater.name()); tag.putInt("Seconds", (int) Math.max(0, op.theater.minutes() * 60L - op.ticks() / 20));
        tag.putInt("Credits", op.credits()); tag.putInt("CommonSamples", op.common()); tag.putInt("RareSamples", op.rare()); tag.putInt("SuperSamples", op.supers());
        tag.putInt("Samples", op.common() + op.rare() + op.supers()); tag.putInt("Resupplies", op.resupplies()); tag.put("Objectives", op.objectivesSnapshot(player.position()));
        double distance = Math.sqrt(player.position().distanceToSqr(Vec3.atCenterOf(op.extraction))); tag.putDouble("ExtractionDistance", distance);
        tag.putInt("ExtractionX", op.extraction.getX()); tag.putInt("ExtractionZ", op.extraction.getZ());
        tag.putInt("ExtractionSeconds", op.phase() == LibertyOperation.Phase.EXTRACTING ? (int) Math.max(0, 30 - op.phaseTicks() / 20) : 0);
        tag.putBoolean("CanExtract", op.phase() == LibertyOperation.Phase.ACTIVE && op.complete() && distance <= 12);
        tag.putBoolean("CanBoard", op.phase() == LibertyOperation.Phase.BOARDING && op.phaseTicks() >= 60 && distance <= 8);
        tag.putBoolean("CanResupply", op.field() && op.resupplies() > 0 && op.ticks() >= s.lastResupply + 600);
        tag.putBoolean("CanAbort", !s.exiting); return tag;
    }
    private static void send(ServerPlayer player, boolean open, String message) {
        CompoundTag snapshot = snapshot(player); snapshot.putBoolean("Open", open);
        if (message != null && !message.isBlank()) snapshot.putString("Status", message);
        JetSetNetwork.syncLiberty(player, snapshot);
    }
    private static boolean fail(ServerPlayer player, String message) { send(player, true, message); return false; }
    private static void syncRide(ServerPlayer player) { player.getCapability(JetSetDataProvider.CAPABILITY).ifPresent(value -> JetSetNetwork.sync(player, value)); }
    private LibertyManager() {}
}
