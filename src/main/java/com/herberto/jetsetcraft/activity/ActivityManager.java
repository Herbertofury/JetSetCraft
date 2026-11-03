package com.herberto.jetsetcraft.activity;

import com.herberto.jetsetcraft.block.BoomboxBlock;
import com.herberto.jetsetcraft.blockentity.BoomboxBlockEntity;
import com.herberto.jetsetcraft.config.JetSetConfig;
import com.herberto.jetsetcraft.crew.CrewHouseData;
import com.herberto.jetsetcraft.crew.CrewHouseManager;
import com.herberto.jetsetcraft.gang.GangActorFactory;
import com.herberto.jetsetcraft.gang.GangChallengeController;
import com.herberto.jetsetcraft.gang.GangRegistry;
import com.herberto.jetsetcraft.gang.HeadGangTargetResolver;
import com.herberto.jetsetcraft.mob.MobCompatibilityRegistry;
import com.herberto.jetsetcraft.liberty.LibertyManager;
import com.herberto.jetsetcraft.liberty.LibertyWorld;
import com.herberto.jetsetcraft.network.JetSetNetwork;
import com.herberto.jetsetcraft.registry.ModBlocks;
import com.herberto.jetsetcraft.rush.RivalRushManager;
import com.herberto.jetsetcraft.rush.RushReturnState;
import com.herberto.jetsetcraft.rush.RushWorld;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.Difficulty;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;

/** Owner-scoped street games. A random invitation is revealed and accepted before any activity begins. */
public final class ActivityManager {
    public static final String OWNED = "jetsetcraft:street_activity";
    private static final int RADIUS = 32;
    private static final Set<String> ACTIONS = Set.of("open", "refresh", "select", "launch", "cancel", "format", "next");
    private static final Map<MinecraftServer, Map<UUID, Context>> CONTEXTS = new HashMap<>();
    private static final Map<MinecraftServer, Map<UUID, Long>> REQUESTS = new HashMap<>();

    private static final class Actor {
        final Mob mob;
        final int index;
        int hits;
        int hitAt = -100, stunnedUntil;
        Actor(Mob mob, int index) { this.mob = mob; this.index = index; }
    }
    private static final class Context {
        final UUID id = UUID.randomUUID();
        final ServerPlayer player;
        final ResourceKey<Level> dimension;
        final BlockPos anchor;
        final boolean boombox;
        final HeadGangTargetResolver.Target target;
        final HeadGangTargetResolver.Target tuning;
        final Map<UUID, Actor> actors = new LinkedHashMap<>();
        final List<Vec3> hills = new ArrayList<>();
        ActivityMode selected, mode;
        ActivityRun run;
        ActivityMix mix;
        UUID eventId;
        String format = "quick";
        long expires;
        int version, spawned, attempts, nextSpawn, relayDelivered;
        String summary = "";
        boolean contested, anchorRemoving, finalized;
        CrewHouseManager.ActivityReward reward = CrewHouseManager.ActivityReward.NONE;
        Context(ServerPlayer player, BlockPos anchor, boolean boombox, HeadGangTargetResolver.Target target) {
            this.player = player; this.dimension = player.serverLevel().dimension(); this.anchor = anchor.immutable();
            this.eventId = id;
            this.boombox = boombox; this.target = target; this.expires = clock(player) + 1200;
            this.tuning = boombox && player.serverLevel().getBlockEntity(anchor) instanceof BoomboxBlockEntity box
                    ? box.resolvedTarget().orElse(null) : null;
        }
        boolean active() { return run != null && !run.ended(); }
    }

    private static Map<UUID, Context> contexts(MinecraftServer server) {
        return CONTEXTS.computeIfAbsent(server, ignored -> new HashMap<>());
    }
    private static Context context(Player player) {
        if (player == null || player.getServer() == null) return null;
        return contexts(player.getServer()).get(player.getUUID());
    }
    public static boolean active(Player player) { Context c = context(player); return c != null && c.active(); }
    public static boolean owned(Entity entity) {
        return entity != null && entity.getPersistentData().contains(OWNED);
    }
    /** A reloaded or copied event actor never becomes a replacement objective. */
    public static boolean validateJoin(Entity entity) {
        if (!owned(entity) || entity.getServer() == null) return false;
        CompoundTag marker = entity.getPersistentData().getCompound(OWNED);
        if (!marker.hasUUID("Owner") || !marker.hasUUID("Event")) return false;
        Context c = contexts(entity.getServer()).get(marker.getUUID("Owner"));
        Actor actor = c == null ? null : c.actors.get(entity.getUUID());
        return c != null && c.active() && c.eventId.equals(marker.getUUID("Event"))
                && actor != null && actor.mob == entity;
    }
    public static boolean anchorActive(ServerLevel level, BlockPos pos) {
        if (level == null || pos == null) return false;
        return contexts(level.getServer()).values().stream().anyMatch(c -> c.active() && c.boombox
                && c.dimension.equals(level.dimension()) && c.anchor.equals(pos));
    }

    public static void open(ServerPlayer player) { openAt(player, null); }
    public static void openAt(ServerPlayer player, BlockPos clicked) {
        if (player == null || !budget(player)) return;
        if (LibertyManager.active(player) || LibertyManager.hasReturn(player) || LibertyWorld.isLiberty(player.level())) {
            LibertyManager.open(player); return;
        }
        if (RivalRushManager.active(player) || RushReturnState.hasJournal(player)) {
            RivalRushManager.handleAction(player, "menu", "", 0, null, 0); return;
        }
        Context old = context(player);
        if (old != null && (old.active() || old.mix != null && old.mix.intermission())) { send(old, true); return; }
        boolean boombox = clicked != null;
        BlockPos anchor = boombox ? clicked : player.blockPosition();
        if (boombox && (!player.serverLevel().hasChunkAt(anchor)
                || !player.serverLevel().getBlockState(anchor).is(ModBlocks.BOOMBOX.get())
                || player.distanceToSqr(Vec3.atCenterOf(anchor)) > 64)) return;
        Context created = invitation(player, anchor, boombox, boombox ? ActivityMode.TAG : ActivityMode.RANDOM);
        contexts(player.getServer()).put(player.getUUID(), created); send(created, true);
    }

    /** Used by commands: the chosen mode is still an invitation, never an implicit teleport or launch. */
    public static void invite(ServerPlayer player, String modeId) {
        if (player == null || !budget(player)) return;
        if (LibertyManager.active(player) || LibertyManager.hasReturn(player) || LibertyWorld.isLiberty(player.level())) {
            LibertyManager.open(player); return;
        }
        Context old = context(player);
        if (old != null && (old.active() || old.mix != null && old.mix.intermission())) { send(old, true); return; }
        ActivityMode selected = ActivityMode.byId(modeId).orElse(ActivityMode.RANDOM);
        Context c = invitation(player, player.blockPosition(), false, selected);
        contexts(player.getServer()).put(player.getUUID(), c); send(c, true);
    }

    public static void handleAction(ServerPlayer player, String action, String mode, UUID contextId, int version) {
        if (player == null || !ACTIONS.contains(action)) return;
        if (LibertyManager.active(player) || LibertyManager.hasReturn(player) || LibertyWorld.isLiberty(player.level())) {
            if (budget(player)) LibertyManager.open(player);
            return;
        }
        if (action.equals("open")) { open(player); return; }
        Context c = context(player);
        if (c == null) { open(player); return; }
        if (!c.id.equals(contextId)) { if (budget(player)) send(c, true); return; }
        if (action.equals("cancel")) {
            if (version != c.version) { if (budget(player)) send(c, true); return; }
            if (c.active()) finish(c, false, "Activity cancelled. Your existing crew progress is unchanged.");
            else if (c.mix != null && c.mix.intermission()) endIntermission(c, "Street Mix ended.");
            else { contexts(player.getServer()).remove(player.getUUID()); open(player); }
            return;
        }
        if (!budget(player)) return;
        if (action.equals("refresh")) { send(c, true); return; }
        if (c.active()) { send(c, true); return; }
        if (version != c.version || clock(player) >= c.expires) {
            c.summary = "That invitation is out of date. Reopen the activity menu to choose again."; send(c, true); return;
        }
        if (action.equals("format")) {
            if (c.run != null || !c.mode.local() || !(mode.equals("quick") || mode.equals(ActivityMix.FORMAT))) {
                send(c, true); return;
            }
            c.format = mode;
            c.mix = mode.equals(ActivityMix.FORMAT) ? new ActivityMix(c.id, c.mode) : null;
            c.summary = c.mix == null ? c.mode.description : ActivityMix.modifier(c.mode)
                    + " Each cleared round banks its own crew reward. Continue between rounds when ready.";
            c.version++; send(c, true); return;
        }
        if (action.equals("next")) {
            if (c.mix == null || !c.mix.intermission()) { send(c, true); return; }
            LaunchPlan plan = prepareLaunch(c, c.mix.next());
            if (!plan.reason.isEmpty()) { c.summary = plan.reason; send(c, true); return; }
            c.mix.advance(); c.mode = c.mix.mode(); c.run = null; c.reward = CrewHouseManager.ActivityReward.NONE;
            c.version++; launch(c, plan); return;
        }
        if (action.equals("select")) {
            ActivityMode chosen = ActivityMode.byId(mode).orElse(null);
            if (chosen == null) return;
            if (c.run != null) {
                c = invitation(player, c.anchor, c.boombox, chosen);
                contexts(player.getServer()).put(player.getUUID(), c);
            } else { choose(c, chosen); c.version++; }
            send(c, true); return;
        }
        if (action.equals("launch") && c.run == null) launch(c);
    }

    private static Context invitation(ServerPlayer player, BlockPos anchor, boolean boombox, ActivityMode selected) {
        HeadGangTargetResolver.Target target = null;
        if (boombox && player.serverLevel().getBlockEntity(anchor) instanceof BoomboxBlockEntity box)
            target = box.resolvedTarget().orElse(null);
        if (target == null) target = GangChallengeController.chooseContextTarget(player.serverLevel(), anchor, player).orElse(null);
        Context c = new Context(player, anchor, boombox, target);
        choose(c, selected); return c;
    }
    private static void choose(Context c, ActivityMode selected) {
        c.selected = selected;
        if (selected == ActivityMode.RANDOM) {
            List<ActivityMode> allowed = ActivityMode.games().stream().filter(mode -> unavailable(c, mode).isEmpty()
                    && (mode != ActivityMode.LIBERTY_GRIND || LibertyManager.unavailable(c.player).isEmpty())).toList();
            c.mode = allowed.isEmpty() ? ActivityMode.TAG : allowed.get(c.player.getRandom().nextInt(allowed.size()));
            c.summary = "Random invitation: " + c.mode.label + ". Review the objective, then accept to start.";
        } else {
            c.mode = selected;
            c.summary = selected == ActivityMode.LIBERTY_GRIND
                    ? "Open the war map to review a district and its loadout before deploying. Research connects graffiti, crew progress and Rival Rush."
                    : selected == ActivityMode.RIVAL_RUSH
                    ? "Enter a separate arena journey. Your normal Minecraft inventory and XP are saved for return."
                    : "Play on the nearby loaded terrain. Your normal inventory and Minecraft XP stay with you.";
        }
        c.expires = clock(c.player) + 1200;
        if (!c.mode.local()) c.format = "quick";
        c.mix = c.format.equals(ActivityMix.FORMAT) ? new ActivityMix(c.id, c.mode) : null;
    }

    private static String unavailable(Context c, ActivityMode mode) {
        ServerPlayer player = c.player;
        if (!JetSetConfig.SERVER.enableStreetActivities.get()) return "Street activities are disabled by this server.";
        if (!player.isAlive() || player.isSpectator() || player.isPassenger() || player.isSleeping()) return "Stand safely, alive and outside Spectator mode.";
        if (LibertyManager.active(player) || LibertyManager.hasReturn(player) || LibertyWorld.isLiberty(player.level()))
            return "Return from Liberty Grind before choosing another activity.";
        if (RivalRushManager.active(player) || RushReturnState.hasJournal(player) || RushWorld.isRush(player.level())) return "Return from your current Rush before another activity.";
        // A war-map preview and research do not need a local mob or installed weapon pack.
        // Deployment has its own server-side dependency and mission admission checks.
        if (mode == ActivityMode.LIBERTY_GRIND) return "";
        if (mode == ActivityMode.RANDOM) return ActivityMode.games().stream().anyMatch(game -> unavailable(c, game).isEmpty()
                && (game != ActivityMode.LIBERTY_GRIND || LibertyManager.unavailable(player).isEmpty()))
                ? "" : "No activity is currently available here.";
        if (c.target == null) return "No eligible local crew is available here. Tune a Boombox with an eligible mob head.";
        var definition = GangRegistry.definitionForEntity(c.target.entityId());
        if (!MobCompatibilityRegistry.boomboxAllowed(c.target.entityId()) || definition.legendary() || !definition.boomboxEligible())
            return "This source creature is boss-gated or reserved by its provider and cannot enter a street activity.";
        if (mode == ActivityMode.RIVAL_RUSH) {
            if (!JetSetConfig.SERVER.enableRivalRush.get()) return "Rival Rush is disabled by this server.";
            if (GangRegistry.byId(c.target.gangId()).isEmpty()) return "Register this crew in the gang catalogue before choosing its Rival Rush roster.";
            if (player.serverLevel().getDifficulty() == Difficulty.PEACEFUL) return "Rival Rush requires Easy, Normal or Hard.";
        }
        EntityType<?> type = ForgeRegistries.ENTITY_TYPES.getValue(c.target.entityId());
        if (type == null) return "The selected crew's creature provider is unavailable.";
        if (type.getCategory() == MobCategory.MONSTER && player.serverLevel().getDifficulty() == Difficulty.PEACEFUL)
            return "This hostile crew requires Easy, Normal or Hard; the world difficulty is unchanged.";
        return "";
    }

    private static String launchReason(Context c, ActivityMode mode) {
        ServerPlayer player = c.player;
        String reason = unavailable(c, mode);
        if (reason.isEmpty() && (!player.serverLevel().dimension().equals(c.dimension)
                || !player.serverLevel().hasChunkAt(c.anchor) || player.distanceToSqr(Vec3.atCenterOf(c.anchor)) > 64))
            reason = "Return to the invitation's nearby anchor before accepting.";
        if (reason.isEmpty() && c.boombox) {
            if (!(player.serverLevel().getBlockEntity(c.anchor) instanceof BoomboxBlockEntity box)) reason = "The selected Boombox is no longer present.";
            else if (box.isChallengeActive() || anchorActive(player.serverLevel(), c.anchor)) reason = "This Boombox already has an activity.";
            else if (!Objects.equals(box.resolvedTarget().orElse(null), c.tuning))
                reason = "The Boombox target changed. Reopen its menu to review the new crew.";
        }
        return reason;
    }

    private record LaunchPlan(String reason, List<Vec3> hills) {}

    /** Preflight is read-only: a rejected next round retains its intermission and paid round identity. */
    private static LaunchPlan prepareLaunch(Context c, ActivityMode mode) {
        String reason = launchReason(c, mode);
        if (!reason.isEmpty() || !mode.local()) return new LaunchPlan(reason, List.of());
        long live = contexts(c.player.getServer()).values().stream().filter(Context::active).count();
        if (live >= JetSetConfig.SERVER.maxStreetActivities.get())
            return new LaunchPlan("The server's active street-game budget is full. Try again after another game finishes.", List.of());
        List<Vec3> hills = new ArrayList<>();
        if (mode == ActivityMode.KING_OF_HILL) {
            List<BlockPos> offsets = c.mix == null
                    ? List.of(new BlockPos(7, 0, 0), new BlockPos(0, 0, 7), new BlockPos(-7, 0, 0)) : ActivityMix.hillOffsets();
            for (BlockPos offset : offsets) {
                Vec3 center = ground(c.player.serverLevel(), c.anchor.offset(offset), c.anchor.getY());
                if (center != null) hills.add(center);
            }
            if (hills.size() != offsets.size()) return new LaunchPlan(c.mix == null
                    ? "King of the Hill needs three clear, dry locations seven blocks east, south and west of the anchor. No terrain was changed."
                    : "Street Mix needs five clear, dry hills: nine blocks east, south, west and north, plus seven east and south. Move the anchor or clear the area yourself.", List.of());
        }
        return new LaunchPlan("", List.copyOf(hills));
    }

    private static void launch(Context c) { launch(c, prepareLaunch(c, c.mode)); }

    private static void launch(Context c, LaunchPlan plan) {
        ServerPlayer player = c.player;
        if (!plan.reason.isEmpty()) { c.summary = plan.reason; send(c, true); return; }
        if (c.mode == ActivityMode.LIBERTY_GRIND) {
            contexts(player.getServer()).remove(player.getUUID(), c);
            LibertyManager.open(player);
            return;
        }
        if (c.mode == ActivityMode.RIVAL_RUSH) {
            // Consume the reviewed invitation before handoff; Rush's own admission guard sees no local game.
            contexts(player.getServer()).remove(player.getUUID(), c);
            boolean started = RivalRushManager.startCircuit(player, c.target.gangId(), c.target.entityId(), false, null);
            if (!started && !RivalRushManager.active(player) && !RushReturnState.hasJournal(player)
                    && player.serverLevel().dimension().equals(c.dimension)) {
                Context retry = invitation(player, c.anchor, c.boombox, c.selected);
                retry.summary = "No Rival Rush was started. Review the setup's reason, then choose again when ready.";
                contexts(player.getServer()).put(player.getUUID(), retry);
                send(retry, false); // Keep Rush's concrete failure message visible; never reuse the accepted nonce.
            }
            return;
        }
        c.hills.clear();
        c.hills.addAll(plan.hills);
        c.run = c.mix == null ? new ActivityRun(c.mode) : c.mix.newRun();
        c.eventId = c.mix == null ? c.id : c.mix.eventId();
        c.version++; c.expires = clock(player) + c.run.remaining() + 200;
        c.summary = c.mix == null ? c.mode.description : ActivityMix.modifier(c.mode);
        c.spawned = 0; c.attempts = 0; c.nextSpawn = 1; c.relayDelivered = 0; c.finalized = false;
        setBoomboxActive(c, true);
        player.serverLevel().playSound(null, c.anchor, SoundEvents.NOTE_BLOCK_PLING.value(), SoundSource.RECORDS, .6F, 1.2F);
        send(c, true);
    }

    public static void tick(MinecraftServer server) {
        for (Context c : new ArrayList<>(contexts(server).values())) {
            if (c.active()) tick(c);
            else if (clock(c.player) >= c.expires) contexts(server).remove(c.player.getUUID(), c);
        }
    }
    private static void tick(Context c) {
        ServerPlayer player = c.player;
        ServerLevel level = player.serverLevel();
        if (!JetSetConfig.SERVER.enableStreetActivities.get() || !player.isAlive() || player.isSpectator()
                || !level.dimension().equals(c.dimension) || !level.hasChunkAt(c.anchor)
                || player.distanceToSqr(Vec3.atCenterOf(c.anchor)) > RADIUS * RADIUS
                || c.boombox && !level.getBlockState(c.anchor).is(ModBlocks.BOOMBOX.get())) {
            finish(c, false, "Activity ended because its player or loaded play area is no longer available. No completion reward was claimed."); return;
        }
        c.run.tick();
        if (c.run.ended()) { finish(c, false, "Time is up. Try another game when you are ready."); return; }
        int ticks = c.run.ticks();
        if (relayPending(c)) {
            Vec3 handoff = Vec3.atBottomCenterOf(c.anchor);
            if (ticks % 10 == 0) ring(level, handoff, ParticleTypes.ELECTRIC_SPARK, 2);
            if (player.position().distanceToSqr(handoff) <= 2.5 * 2.5) {
                c.relayDelivered = c.run.objectives(); c.version++;
                player.displayClientMessage(Component.literal("Handoff complete · chase the next pair!"), true);
                level.playSound(null, c.anchor, SoundEvents.NOTE_BLOCK_PLING.value(), SoundSource.RECORDS, .5F, 1.5F);
                send(c, false);
            }
        }
        int simultaneous = Math.min(3, Math.max(1, JetSetConfig.SERVER.boomboxMaxActors.get()));
        int desired = c.mode == ActivityMode.KING_OF_HILL ? simultaneous : c.run.objectiveGoal();
        int admitted = c.mix != null && c.mode == ActivityMode.TAG ? Math.min(desired, c.relayDelivered + 2) : desired;
        if (c.mix != null && c.mode == ActivityMode.SHOWDOWN && c.run.objectives() < 3) admitted = 3;
        if (c.spawned < admitted && c.actors.size() < simultaneous && ticks >= c.nextSpawn) {
            c.nextSpawn = ticks + 10; c.attempts++;
            var spawned = GangActorFactory.spawn(level, c.anchor, c.target, c.eventId, c.spawned,
                    level.getGameTime() + c.run.remaining() + 200L, 11.0);
            if (spawned.isPresent()) {
                Mob mob = spawned.get();
                CompoundTag ownership = new CompoundTag(); ownership.putUUID("Owner", player.getUUID()); ownership.putUUID("Event", c.eventId);
                mob.getPersistentData().put(OWNED, ownership); mob.setNoAi(true); mob.setTarget(null); mob.setCanPickUpLoot(false);
                mob.setGlowingTag(true); mob.setCustomName(Component.literal(actorName(c, c.spawned, 0)));
                c.actors.put(mob.getUUID(), new Actor(mob, c.spawned++));
            } else if (c.attempts >= desired * 4) {
                finish(c, false, "The crew could not enter this loaded terrain safely. Choose another location; no reward was claimed."); return;
            }
        }
        for (Actor actor : new ArrayList<>(c.actors.values())) {
            Mob mob = actor.mob;
            if (mob.isRemoved() || !mob.isAlive() || !level.hasChunkAt(mob.blockPosition())
                    || Math.abs(mob.getY() - c.anchor.getY()) > 12 || mob.distanceToSqr(Vec3.atCenterOf(c.anchor)) > 40 * 40) {
                finish(c, false, "A rival left or unloaded from the play area. This is a cancelled round, not a completed objective."); return;
            }
            mob.clearFire(); mob.fallDistance = 0;
            if (ticks % 5 == 0) move(c, actor);
        }
        if (c.mode == ActivityMode.KING_OF_HILL && c.spawned >= desired) {
            Vec3 hill = c.hills.get(Math.min(c.run.objectives(), c.hills.size() - 1));
            c.contested = c.actors.values().stream().anyMatch(a -> a.stunnedUntil <= ticks && a.mob.position().distanceToSqr(hill) < 2.6 * 2.6);
            c.run.holdHill(player.position().distanceToSqr(hill) < 3.5 * 3.5, c.contested);
            if (ticks % 10 == 0) ring(level, hill, c.contested ? ParticleTypes.FLAME : ParticleTypes.HAPPY_VILLAGER, 3);
            if (c.run.won()) { finish(c, true, "All " + c.run.objectiveGoal() + " hills captured."); return; }
        }
        if (ticks % 20 == 0) {
            player.displayClientMessage(Component.literal(c.mode.label + " · " + objective(c) + " · " + c.run.remaining() / 20 + "s"), true);
            send(c, false);
        }
    }

    private static void move(Context c, Actor actor) {
        int time = c.run.ticks();
        if (actor.stunnedUntil > time) { actor.mob.setDeltaMovement(0, actor.mob.getDeltaMovement().y, 0); return; }
        if (actor.stunnedUntil != 0) { actor.stunnedUntil = 0; actor.mob.setCustomName(Component.literal(actorName(c, actor.index, actor.hits))); }
        Vec3 destination;
        if (c.mode == ActivityMode.KING_OF_HILL) destination = c.hills.get(Math.min(c.run.objectives(), c.hills.size() - 1));
        else {
            int wave = c.mix != null && c.mode == ActivityMode.TAG ? Math.min(2, actor.index / 2) : 1;
            double angle = time * (wave == 1 && c.mix != null && c.mode == ActivityMode.TAG ? -.019 : .016) + actor.index * 2.399;
            int radius = c.mix != null && c.mode == ActivityMode.TAG ? 7 + wave * 2 : 9;
            destination = Vec3.atCenterOf(c.anchor).add(Math.cos(angle) * radius, 0, Math.sin(angle) * radius);
        }
        Vec3 delta = destination.subtract(actor.mob.position());
        Vec3 horizontal = new Vec3(delta.x, 0, delta.z);
        if (horizontal.lengthSqr() > .3) horizontal = horizontal.normalize().scale(c.mode == ActivityMode.KING_OF_HILL ? .14
                : captain(c, actor.index) ? .25 : .20);
        else horizontal = Vec3.ZERO;
        actor.mob.setDeltaMovement(horizontal.x, actor.mob.getDeltaMovement().y, horizontal.z);
        if (actor.mob.horizontalCollision && actor.mob.onGround()) actor.mob.setDeltaMovement(horizontal.x, .38, horizontal.z);
        actor.mob.hasImpulse = true;
        if (horizontal.lengthSqr() > 0) actor.mob.setYRot((float) Math.toDegrees(Math.atan2(-horizontal.x, horizontal.z)));
    }

    /** A physical owner hit is one bounded game tag; vanilla/source-provider damage is never applied. */
    public static boolean tag(ServerPlayer player, Entity target, boolean ranged) {
        Context c = context(player);
        if (c == null || !c.active() || !owned(target)) return false;
        Actor actor = c.actors.get(target.getUUID());
        if (actor == null || actor.mob != target || target.isRemoved() || !actor.mob.isAlive()
                || target.level() != player.level() || player.distanceToSqr(target) > (ranged ? 24 * 24 : 4.5 * 4.5)
                || !player.hasLineOfSight(target) || c.run.ticks() - actor.hitAt < 8) return false;
        actor.hitAt = c.run.ticks();
        if (c.mode == ActivityMode.KING_OF_HILL) {
            if (actor.stunnedUntil > c.run.ticks()) return false;
            actor.stunnedUntil = c.run.ticks() + 120;
            actor.mob.setCustomName(Component.literal("Tagged · recovering"));
        } else if (c.mode == ActivityMode.TAG || ++actor.hits >= requiredTags(c, actor.index)) {
            c.actors.remove(target.getUUID()); actor.mob.discard(); c.run.tagged();
        } else actor.mob.setCustomName(Component.literal(actorName(c, actor.index, actor.hits)));
        player.serverLevel().sendParticles(ParticleTypes.ELECTRIC_SPARK, target.getX(), target.getY() + .7, target.getZ(), 10, .2, .3, .2, .03);
        player.serverLevel().playSound(null, target.blockPosition(), SoundEvents.NOTE_BLOCK_HAT.value(), SoundSource.RECORDS, .45F, 1.1F);
        if (c.run.won()) finish(c, true, c.mode.label + " completed.");
        else { c.version++; send(c, false); }
        return true;
    }

    /** Event balloons use the owner's bounded target set; they cannot paint over nearby player builds. */
    public static boolean balloonHit(ServerPlayer player, HitResult hit) {
        Context c = context(player);
        if (c == null || !c.active() || hit.getLocation().distanceToSqr(Vec3.atCenterOf(c.anchor)) > RADIUS * RADIUS)
            return false;
        if (hit instanceof EntityHitResult entityHit) tag(player, entityHit.getEntity(), true);
        for (Actor actor : new ArrayList<>(c.actors.values()))
            if (actor.mob.position().distanceToSqr(hit.getLocation()) <= 3 * 3) tag(player, actor.mob, true);
        return true;
    }

    public static void cancel(ServerPlayer player, String reason) {
        Context c = context(player);
        if (c != null && c.active()) finish(c, false, reason);
        else if (c != null && c.mix != null && c.mix.intermission()) endIntermission(c, reason);
    }
    public static void anchorRemoved(ServerLevel level, BlockPos pos) {
        for (Context c : new ArrayList<>(contexts(level.getServer()).values()))
            if ((c.active() || c.mix != null && c.mix.intermission()) && c.boombox && c.dimension.equals(level.dimension()) && c.anchor.equals(pos)) {
                c.anchorRemoving = true;
                if (c.active()) finish(c, false, "The Boombox was removed. The activity ended without a completion reward.");
                else endIntermission(c, "The Boombox was removed. Street Mix ended.");
            }
    }
    private static void endIntermission(Context c, String message) {
        if (c.mix == null || !c.mix.intermission()) return;
        c.mix.cancel(); c.summary = message + " Cleared rounds keep their saved rewards.";
        c.version++; c.expires = clock(c.player) + 1200;
        c.player.displayClientMessage(Component.literal(c.summary), false); send(c, true);
    }
    private static void finish(Context c, boolean success, String message) {
        if (c.run == null || c.finalized) return;
        c.finalized = true;
        boolean won = success && c.run.won();
        c.run.cancel();
        for (Actor actor : c.actors.values()) if (owned(actor.mob)) actor.mob.discard();
        c.actors.clear(); setBoomboxActive(c, false);
        if (won) {
            c.reward = CrewHouseManager.bankActivity(c.player, c.eventId, c.target.gangId(), c.mode.id,
                    c.run.ticks(), c.run.objectives(), (long) c.run.progress() * 100);
            if (c.reward.accepted()) {
                message += " +" + c.reward.supplies() + " Crew Supplies, +" + c.reward.crewXp() + " crew XP, +"
                        + c.reward.reputation() + " reputation" + (c.reward.memberXp() > 0 ? ", +" + c.reward.memberXp() + " training XP" : "") + ".";
                if (c.reward.pendingHouse()) message += " Saved for your future crew house.";
            }
            c.player.serverLevel().sendParticles(ParticleTypes.FIREWORK, c.player.getX(), c.player.getY() + 1, c.player.getZ(), 20, .5, .5, .5, .03);
            c.player.serverLevel().playSound(null, c.player.blockPosition(), SoundEvents.PLAYER_LEVELUP, SoundSource.RECORDS, .6F, 1.3F);
            if (c.mix != null && c.mix.wonRound(c.mode)) {
                message += c.mix.complete() ? " Street Mix complete: all three modes cleared!"
                        : " Street Mix " + c.mix.cleared() + " / " + c.mix.total() + " cleared. Next: " + c.mix.next().label
                            + ". Return to the anchor and choose Next round when ready.";
            }
        } else if (c.mix != null) {
            c.mix.cancel();
            message += " Earlier cleared rounds keep their saved rewards.";
        }
        c.summary = message; c.version++; c.expires = clock(c.player) + (c.mix != null && c.mix.intermission() ? 6000 : 1200);
        c.player.displayClientMessage(Component.literal(message), false); send(c, true);
    }

    public static CompoundTag snapshot(ServerPlayer player) {
        Context c = context(player);
        CompoundTag out = new CompoundTag(); out.putBoolean("Open", false);
        if (c == null) return out;
        out.putUUID("Context", c.id); out.putInt("Version", c.version); out.putBoolean("Active", c.active());
        out.putUUID("Event", c.eventId);
        out.putString("Format", c.format); out.putBoolean("Mix", c.mix != null);
        out.putBoolean("CanChooseFormat", c.mode.local() && c.run == null && clock(player) < c.expires);
        if (c.mix != null) {
            out.putInt("MixRound", c.mix.round()); out.putInt("MixCleared", c.mix.cleared()); out.putInt("MixTotal", c.mix.total());
            out.putString("MixRoute", String.join(" → ", c.mix.route().stream().map(mode -> mode.label).toList()));
            out.putString("MixModifier", ActivityMix.modifier(c.mode)); out.putBoolean("MixComplete", c.mix.complete());
            out.putBoolean("Intermission", c.mix.intermission());
            if (c.mix.intermission()) {
                String reason = clock(player) >= c.expires ? "This Street Mix invitation expired. Reopen Activities to start a new set."
                        : prepareLaunch(c, c.mix.next()).reason;
                out.putBoolean("CanNextRound", reason.isEmpty());
                out.putString("NextModeName", c.mix.next().label); out.putString("NextObjective", ActivityMix.modifier(c.mix.next()));
                out.putString("NextRoundReason", reason);
            }
        }
        out.putBoolean("FromBoombox", c.boombox); out.putString("SelectedMode", c.selected.id);
        out.putString("Mode", c.mode.id); out.putString("ModeName", c.mode.label);
        out.putString("Gang", c.target == null ? "No local crew" : GangRegistry.byId(c.target.gangId()).map(g -> g.canonicalName()).orElse(c.target.gangId().toString()));
        out.putString("AnchorLabel", (c.boombox ? "Boombox · " : "Nearby street · ") + c.dimension.location() + " · " + c.anchor.getX() + ", " + c.anchor.getY() + ", " + c.anchor.getZ());
        out.putString("Summary", c.summary); out.putString("Objective", objective(c));
        ActivityRun preview = c.run != null ? c.run : c.mix == null ? null : c.mix.newRun();
        out.putLong("RemainingTicks", preview == null ? c.mode.durationTicks : preview.remaining());
        out.putInt("Progress", c.run == null ? 0 : c.run.progress()); out.putInt("Goal", preview == null ? c.mode.goal : preview.goal());
        out.putBoolean("RelayHandoff", relayPending(c));
        out.putDouble("HandoffX", c.anchor.getX() + .5); out.putDouble("HandoffY", c.anchor.getY()); out.putDouble("HandoffZ", c.anchor.getZ() + .5);
        if (c.run != null && c.mode == ActivityMode.KING_OF_HILL && !c.hills.isEmpty()) {
            Vec3 hill = c.hills.get(Math.min(c.run.objectives(), c.hills.size() - 1));
            out.putBoolean("HasHill", true); out.putDouble("HillX", hill.x); out.putDouble("HillY", hill.y); out.putDouble("HillZ", hill.z);
        }
        boolean completed = c.run != null && c.run.ended();
        CrewHouseManager.ActivityReward rewards = completed ? c.reward : JetSetConfig.SERVER.enableCrewHouse.get()
                ? CrewHouseManager.activityReward(c.mode.id) : CrewHouseManager.ActivityReward.NONE;
        out.putInt("Supplies", rewards.supplies()); out.putInt("CrewXp", rewards.crewXp()); out.putInt("Reputation", rewards.reputation());
        out.putString("RewardSummary", c.mode == ActivityMode.LIBERTY_GRIND
                ? "Extract samples for graffiti research and Rival Rush preparations; mission rewards also grow your crew."
                : !JetSetConfig.SERVER.enableCrewHouse.get() ? "Crew progression is disabled on this server."
                : c.mode == ActivityMode.RIVAL_RUSH ? "Arena caches and earned run progress grow your crew."
                : (completed ? "Banked: " : "Completion, up to: ") + rewards.supplies() + " Supplies · "
                        + rewards.crewXp() + " crew XP · " + rewards.reputation() + " reputation · " + rewards.memberXp() + " training XP");
        out.putInt("MemberXp", rewards.memberXp()); out.putBoolean("Completed", completed); out.putBoolean("Won", completed && c.run.won());
        out.putBoolean("CanLaunch", !c.active() && !completed && clock(player) < c.expires && unavailable(c, c.mode).isEmpty());
        out.putString("LaunchLabel", c.mode == ActivityMode.LIBERTY_GRIND ? "Open war map"
                : c.mode == ActivityMode.RIVAL_RUSH ? "Start arena journey" : c.mix != null ? "Start Street Mix · " + c.mode.label : "Start " + c.mode.label);
        out.putBoolean("CanCancel", c.active() || c.mix != null && c.mix.intermission());
        out.putInt("PendingSupplies", CrewHouseData.get(player.getServer()).pendingSupplies(player.getUUID()));
        ListTag games = new ListTag();
        for (ActivityMode mode : ActivityMode.values()) {
            CompoundTag row = new CompoundTag(); row.putString("Id", mode.id); row.putString("Name", mode.label);
            row.putString("Description", mode.description); String reason = unavailable(c, mode);
            row.putBoolean("Available", reason.isEmpty()); row.putString("Reason", reason); games.add(row);
        }
        out.put("Activities", games); return out;
    }
    private static String objective(Context c) {
        if (c.run == null) return c.mix == null ? c.mode.description : ActivityMix.modifier(c.mode);
        return switch (c.mode) {
            case TAG -> relayPending(c) ? "Return to the handoff ring at " + c.anchor.getX() + ", " + c.anchor.getY() + ", " + c.anchor.getZ()
                    : "Rival tags " + c.run.objectives() + " / " + c.run.objectiveGoal()
                        + (c.mix == null ? "" : " · relay pair " + Math.min(3, c.relayDelivered / 2 + 1) + " / 3");
            case SHOWDOWN -> "Rivals out " + c.run.objectives() + " / " + c.run.objectiveGoal()
                    + (c.mix == null ? " · three tags each" : " · crew: 3 tags · captain: 5");
            case KING_OF_HILL -> "Hill " + Math.min(c.run.objectiveGoal(), c.run.objectives() + 1) + " / " + c.run.objectiveGoal()
                    + " · " + c.run.hillTicks() / 20 + " / " + c.run.hillDuration() / 20 + "s" + (c.contested ? " · contested: tag contenders" : "");
            default -> c.mode.description;
        };
    }
    private static boolean relayPending(Context c) {
        return c.mix != null && c.mode == ActivityMode.TAG && c.run != null && !c.run.ended()
                && c.run.objectives() > c.relayDelivered && c.run.objectives() % 2 == 0;
    }
    private static boolean captain(Context c, int index) { return c.mix != null && c.mode == ActivityMode.SHOWDOWN && index == 3; }
    private static int requiredTags(Context c, int index) { return captain(c, index) ? 5 : 3; }
    private static String actorName(Context c, int index, int hits) {
        return c.mode == ActivityMode.TAG ? c.mix == null ? "Tag this skater" : "Relay skater · pair " + (index / 2 + 1)
                : c.mode == ActivityMode.SHOWDOWN ? (captain(c, index) ? "Crew captain · " : "Rival · ") + (requiredTags(c, index) - hits) + " tags"
                : "Hill contender";
    }
    private static void ring(ServerLevel level, Vec3 center, ParticleOptions particle, double radius) {
        for (int index = 0; index < 12; index++) {
            double angle = index * Math.PI / 6;
            level.sendParticles(particle, center.x + Math.cos(angle) * radius, center.y + .15,
                    center.z + Math.sin(angle) * radius, 1, 0, .05, 0, 0);
        }
    }
    private static void send(Context c, boolean open) { CompoundTag tag = snapshot(c.player); tag.putBoolean("Open", open); JetSetNetwork.syncActivity(c.player, tag); }
    private static boolean budget(ServerPlayer player) {
        long now = clock(player);
        Map<UUID, Long> requests = REQUESTS.computeIfAbsent(player.getServer(), ignored -> new HashMap<>());
        if (now - requests.getOrDefault(player.getUUID(), now - 4) < 4) return false;
        requests.put(player.getUUID(), now); return true;
    }
    private static long clock(ServerPlayer player) { return player.getServer().overworld().getGameTime(); }
    private static Vec3 ground(ServerLevel level, BlockPos column, int anchorY) {
        if (!level.getChunkSource().hasChunk(column.getX() >> 4, column.getZ() >> 4) || !level.getWorldBorder().isWithinBounds(column)) return null;
        int y = level.getHeight(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, column.getX(), column.getZ());
        BlockPos feet = new BlockPos(column.getX(), y, column.getZ());
        if (Math.abs(y - anchorY) > 5 || !level.getBlockState(feet.below()).isFaceSturdy(level, feet.below(), Direction.UP)) return null;
        AABB space = new AABB(feet).inflate(1.0, 0, 1.0).expandTowards(0, 1, 0);
        for (int x = ((int) Math.floor(space.minX)) >> 4; x <= ((int) Math.floor(space.maxX)) >> 4; x++)
            for (int z = ((int) Math.floor(space.minZ)) >> 4; z <= ((int) Math.floor(space.maxZ)) >> 4; z++)
                if (!level.getChunkSource().hasChunk(x, z)) return null;
        if (!level.noCollision(space) || level.containsAnyLiquid(space)) return null;
        return Vec3.atBottomCenterOf(feet);
    }
    private static void setBoomboxActive(Context c, boolean active) {
        if (!c.boombox || c.anchorRemoving) return;
        ServerLevel level = c.player.getServer().getLevel(c.dimension);
        if (level == null || !level.hasChunkAt(c.anchor)) return;
        var state = level.getBlockState(c.anchor);
        if (state.is(ModBlocks.BOOMBOX.get()) && state.getValue(BoomboxBlock.ACTIVE) != active)
            level.setBlock(c.anchor, state.setValue(BoomboxBlock.ACTIVE, active), Block.UPDATE_ALL);
    }
    public static void forget(ServerPlayer player) {
        Context c = context(player);
        if (c != null && c.active()) finish(c, false, "Street activity ended on disconnect. No completion reward was claimed.");
        contexts(player.getServer()).remove(player.getUUID());
        REQUESTS.computeIfAbsent(player.getServer(), ignored -> new HashMap<>()).remove(player.getUUID());
    }
    public static void stop(MinecraftServer server) {
        for (Context c : new ArrayList<>(contexts(server).values())) if (c.active()) finish(c, false, "Street activity ended because the server is stopping.");
        CONTEXTS.remove(server); REQUESTS.remove(server);
    }
    private ActivityManager() {}
}
