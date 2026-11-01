package com.herberto.jetsetcraft.rush;

import com.herberto.jetsetcraft.JetSetCraft;
import com.herberto.jetsetcraft.config.JetSetConfig;
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
        Vec3 paintMark;
        boolean claimed;

        Actor(Mob mob, Role role, ResourceLocation species, double vitality) {
            this.mob = mob; this.role = role; this.species = species;
            this.vitality = vitality; this.maximum = vitality;
        }
    }

    private static final class Pickup {
        final ItemEntity entity;
        final ResourceLocation species;
        int count = 1;
        Pickup(ItemEntity entity, ResourceLocation species) { this.entity = entity; this.species = species; }
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
        long lastSpawn, lastShot, lastHurt, lastCaptain = -1, lastStyle;
        long cards;
        int sequence, announcedOffer, musicCharge;
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
        if (action == null || value == null || value.length() > 128) return;
        switch (action) {
            case "leave" -> { if (s != null && (runId == null || s.run.id().equals(runId))) leave(player, "Session banked. Your collected cards stay in the Rush album.");
                              else recover(player); }
            case "choose" -> {
                if (s != null && s.run.id().equals(runId) && number >= 0 && number < s.run.offers().size()
                        && (value.isEmpty() || s.run.offers().get(number).name().equalsIgnoreCase(value))
                        && s.run.choose(number, version)) send(player, true, "", -1, -1);
            }
            case "encore" -> {
                if (s != null && s.run.id().equals(runId) && s.run.offerVersion() == version && s.run.continueEndless()) {
                    s.finaleSent = false; send(player, true, "", -1, -1);
                }
            }
            case "start" -> {
                if (s != null || (number != 0 && number != 1)) return;
                String[] selection = value.split("\\|", -1);
                if (selection.length != 2) return;
                start(player, ResourceLocation.tryParse(selection[0]), ResourceLocation.tryParse(selection[1]), number == 1);
            }
            case "menu", "album", "roster" -> {
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
        long now = player.getServer().overworld().getGameTime();
        if (now - LAST_START.getOrDefault(player.getUUID(), now - 20) < 20) return false;
        LAST_START.put(player.getUUID(), now);
        if (!JetSetConfig.SERVER.enableRivalRush.get()) return fail(player, "Rival Rush is disabled by this server.");
        if (active(player)) return fail(player, "Finish or leave your current Rush first.");
        if (RushReturnState.hasJournal(player)) { recover(player); return fail(player, "Recovered your previous session. Open Rival Rush again."); }
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
        if (!RushReturnState.capture(player)) return fail(player, "Could not safely preserve your return state; no run was started.");
        if (!RushPlayerStorage.flush(player)) {
            // No loan or teleport has happened. Keep the ordinary player state and let logout retry its save.
            player.getPersistentData().getCompound(Player.PERSISTED_NBT_TAG).remove(RushReturnState.KEY);
            player.connection.disconnect(Component.literal(
                    "Rival Rush could not save your return checkpoint. Your Minecraft belongings were not changed. Reconnect after the server can save player data."));
            return false;
        }
        RushRun run = new RushRun(UUID.randomUUID(), player.getUUID(), district.getRandom().nextLong(), endless, gangId.toString());
        Session s = new Session(player, run, gang, roster);
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
            player.teleportTo(district, x + 0.5, RushWorld.FLOOR_Y + 1.0, z + 0.5, 0, 0);
            player.setDeltaMovement(Vec3.ZERO);
            RushWorld.decorateNear(district, player.blockPosition());
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
        RushWorld.decorateNear(level, player.blockPosition());
        if (s.run.hasOffer() || s.run.awaitingEncore()) {
            freeze(s);
            if (s.announcedOffer != s.run.offerVersion() || (s.run.awaitingEncore() && !s.finaleSent)) {
                s.announcedOffer = s.run.offerVersion(); s.finaleSent = s.run.awaitingEncore();
                send(player, true, "", -1, -1);
            }
            return;
        }
        s.run.tickActive();
        if (s.run.awaitingEncore()) {
            freeze(s); s.finaleSent = true; send(player, true, "", -1, -1); return;
        }
        long time = s.run.elapsedTicks();
        if (player.getY() < 50 || !level.getWorldBorder().isWithinBounds(player.blockPosition())) {
            leave(player, "You left the playable district. Your collected cards were kept."); return;
        }
        if (time % 20 == 0) {
            player.getFoodData().setFoodLevel(20); player.getFoodData().setSaturation(5);
            player.clearFire(); player.setAirSupply(player.getMaxAirSupply());
            if (time - s.lastHurt > 100) s.run.heal(s.run.recoveryPerSecond());
            s.walls.entrySet().removeIf(entry -> time - entry.getValue() > 6000);
            s.musicStops.entrySet().removeIf(entry -> time - entry.getValue() > 12000);
            s.projectiles.values().removeIf(Entity::isRemoved);
        }
        collectPickups(s);
        if (s.run.hasOffer()) {
            freeze(s); s.announcedOffer = s.run.offerVersion(); send(player, true, "", -1, -1); return;
        }
        tickActors(s);
        if (s.run.isFinished()) return;
        autoFire(s);
        tickObjectives(s);
        if (time - s.lastSpawn >= Math.max(8, 36 - Math.min(20, (int) (time / 1800)))) {
            s.lastSpawn = time;
            int desiredPressure = 16 + (int) Math.min(64, time / 900);
            if (remix(s.run) == 3) desiredPressure = desiredPressure * 3 / 4;
            int pressure = s.actors.values().stream().mapToInt(a -> a.role == Role.CAPTAIN ? 8 : a.role == Role.BRUISER ? 3 : 1).sum();
            if (pressure < desiredPressure) spawnActor(s, nextRole(s), false);
        }
        long captainChapter = time / 6000;
        if (time >= 3000 && captainChapter != s.lastCaptain) {
            if (spawnActor(s, Role.CAPTAIN, true)) s.lastCaptain = captainChapter;
        }
        if (time % 10 == 0) send(player, false, "", -1, -1);
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
            double x = s.player.getX() + Math.cos(angle) * distance;
            double z = s.player.getZ() + Math.sin(angle) * distance;
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
            // Preserve the original mob/AI; stop this run-owned creeper's native fuse from destroying its card actor.
            if (mob instanceof net.minecraft.world.entity.monster.Creeper creeper) creeper.setSwellDir(-1);
            mob.setTarget(s.player);
            Vec3 toward = s.player.position().subtract(mob.position());
            double distance = toward.horizontalDistance();
            double speed = actor.role == Role.BRUISER ? 0.12 : actor.role == Role.TAGGER ? 0.15 : 0.19;
            // Endless changes composition and roles; skating opponents retain a readable, bounded top speed.
            speed += Math.min(0.05, s.run.elapsedTicks() / 180000.0);
            if (actor.role == Role.CAPTAIN) speed = 0.17;
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
        if (s.exiting || s.run.hasOffer() || s.run.awaitingEncore()) return true;
        if (hit instanceof EntityHitResult entityHit) {
            tagEnemy(player, entityHit.getEntity(), 7.0);
            Vec3 impact = hit.getLocation();
            for (Actor actor : List.copyOf(s.actors.values())) {
                if (actor.mob != entityHit.getEntity() && actor.mob.position().distanceToSqr(impact) < s.run.splashRadius() * s.run.splashRadius())
                    tagEnemy(player, actor.mob, 3.5);
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
        if (s == null || s.exiting || s.run.hasOffer() || s.run.awaitingEncore() || !belongsTo(target, player)) return false;
        Actor actor = s.actors.get(target.getUUID());
        if (actor == null || actor.claimed || target.isRemoved() || !Double.isFinite(baseDamage) || baseDamage <= 0) return false;
        actor.vitality -= Math.min(40, baseDamage) * s.run.damageMultiplier();
        player.serverLevel().sendParticles(ParticleTypes.WAX_OFF, target.getX(), target.getY() + target.getBbHeight() * 0.5,
                target.getZ(), 5, 0.3, 0.5, 0.3, 0.01);
        if (actor.vitality > 0) { updateName(s, actor); return true; }
        actor.claimed = true;
        s.actors.remove(target.getUUID());
        s.run.recordDefeat(); s.run.addScore(actor.role == Role.CAPTAIN ? 500 : 20);
        spawnCard(s, actor);
        if (actor.role == Role.CAPTAIN) { s.run.gainXp(36); s.run.heal(18); }
        target.discard();
        return true;
    }

    private static void updateName(Session s, Actor actor) {
        actor.mob.setCustomName(Component.literal(s.gang.canonicalName() + " · "
                + actor.role.name().toLowerCase(java.util.Locale.ROOT) + " · " + (int) Math.ceil(actor.vitality))
                .withStyle(actor.role == Role.CAPTAIN ? ChatFormatting.GOLD : ChatFormatting.AQUA));
        actor.mob.setCustomNameVisible(actor.role == Role.CAPTAIN || actor.vitality < actor.maximum);
    }

    private static void spawnCard(Session s, Actor actor) {
        for (Pickup pickup : s.pickups.values()) {
            if (!pickup.entity.isRemoved() && pickup.species.equals(actor.species) && pickup.count < 64
                    && pickup.entity.distanceToSqr(actor.mob) < 16) {
                pickup.count++; pickup.entity.getItem().setCount(pickup.count); return;
            }
        }
        ItemStack card = new ItemStack(Items.PAPER);
        card.setHoverName(Component.literal(actor.species.getPath().replace('_', ' ') + " · Rush card"));
        ItemEntity drop = new ItemEntity(s.player.serverLevel(), actor.mob.getX(), actor.mob.getY() + 0.3, actor.mob.getZ(), card);
        drop.setPickUpDelay(32767); drop.setInvulnerable(true);
        mark(drop, s);
        s.pickups.put(drop.getUUID(), new Pickup(drop, actor.species));
        if (!s.player.serverLevel().addFreshEntity(drop)) s.pickups.remove(drop.getUUID());
    }

    private static void collectPickups(Session s) {
        Iterator<Pickup> iterator = s.pickups.values().iterator();
        while (iterator.hasNext()) {
            Pickup pickup = iterator.next();
            if (pickup.entity.isRemoved() || pickup.entity.distanceToSqr(s.player) > 160 * 160) {
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
        if (s == null || s.exiting || s.run.hasOffer() || s.run.awaitingEncore() || !face.getAxis().isHorizontal()
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
        if (time % 20 == 0) s.musicStop = RushWorld.nearestBoombox(s.player.serverLevel(), s.player.blockPosition());
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
        if (s == null || s.exiting || s.run.hasOffer() || s.run.awaitingEncore()) return;
        long time = s.run.elapsedTicks();
        if (time - s.lastHurt < 12) return;
        s.lastHurt = time;
        if (s.run.hurt(Math.min(20, Math.max(0, amount))))
            s.objective = "Run finished — your collected cards are banked.";
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
        if (entity instanceof ItemEntity item) return s.pickups.containsKey(item.getUUID());
        return entity instanceof PaintBalloonEntity projectile && projectile.getOwner() == s.player;
    }

    public static void markPlayerProjectile(Projectile projectile) {
        if (projectile.getOwner() instanceof ServerPlayer player) {
            Session s = session(player); if (s != null && !s.exiting) mark(projectile, s);
        }
    }

    public static boolean restoring(ServerPlayer player) { Session s = session(player); return s != null && s.exiting; }

    public static boolean pausedActor(Entity entity) {
        if (!owned(entity) || !(entity.level() instanceof ServerLevel level)) return false;
        CompoundTag tag = entity.getPersistentData().getCompound(OWNED);
        if (!tag.hasUUID("Owner")) return false;
        Session s = sessions(level.getServer()).get(tag.getUUID("Owner"));
        return s != null && (s.exiting || s.run.hasOffer() || s.run.awaitingEncore());
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

    private static void send(ServerPlayer player, boolean open, String summary, int albumPage, int rosterPage) {
        CompoundTag state = new CompoundTag();
        state.putBoolean("Open", open); state.putString("Summary", summary);
        Session s = session(player); state.putBoolean("Active", s != null);
        state.putLong("Cards", s == null ? RushAlbum.total(player) : s.cards);
        state.putLong("AlbumTotal", RushAlbum.total(player));
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
