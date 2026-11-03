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
import com.herberto.jetsetcraft.entity.LibertyCoreEntity;
import com.herberto.jetsetcraft.entity.LibertySupportEntity;
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
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.Level;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.AABB;
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
    private static final int SHARED_ACTOR_LIMIT = 384;
    private static final int RETIRE_DISTANCE = 160;
    private static final int OBSERVER_DISTANCE = 96;
    private static final String MISSION_PAINT = missionPaint();
    private static String missionPaint() {
        byte[] pixels=new byte[160];
        for (int i=0;i<pixels.length;i++) pixels[i]=(byte)(((i%16+i/16)%13<3)?7:11);
        return com.herberto.jetsetcraft.graffiti.CustomGraffiti.encode(pixels);
    }
    private static final class State {
        final Map<UUID, Session> sessions = new HashMap<>();
        final Map<UUID, Menu> menus = new HashMap<>();
        final Map<UUID, Long> actions = new HashMap<>();
        final Map<UUID, Hub> hubs = new HashMap<>();
        long nextMenu = 1, nextSession;
    }
    private record Menu(long revision, long expires) {}
    private static final class Hub {
        final ServerPlayer player; final BlockPos origin; boolean built;
        Hub(ServerPlayer player, int slot) { this.player=player; this.origin=LibertyWorld.shipOrigin(slot); }
    }
    private static final class Session {
        final ServerPlayer player;
        final LibertyOperation operation;
        final Map<UUID, Entity> entities = new LinkedHashMap<>();
        final Map<UUID, Integer> actors = new LinkedHashMap<>();
        final Set<Integer> built = new HashSet<>(), spawnedCaches = new HashSet<>();
        final Set<UUID> paidDefeats = new HashSet<>();
        final Map<String, UUID> cores = new LinkedHashMap<>();
        final Map<String, net.minecraft.world.entity.decoration.ArmorStand> markers = new LinkedHashMap<>();
        final Map<UUID, Long> allies = new LinkedHashMap<>();
        final Map<Long, Vec3> missionBlasts = new LinkedHashMap<>();
        final Set<String> missionCommands = new HashSet<>();
        final Set<String> sitesBuilt = new HashSet<>(), grounded = new HashSet<>();
        final Map<String, Long> nextGroundAttempt = new HashMap<>();
        final ResourceKey<Level> dimension;
        LibertyStratagemEngine stratagems;
        String sector = "", defense = "";
        int reinforcements = 5, spawned, retired, spawnSequence, waveRemaining, spawnsThisTick, artilleryShells;
        boolean artilleryUnlocked;
        long nextWave;
        LibertyTransportEntity transport;
        Vec3 previous;
        long lastSpawn = -100, lastResupply = -600;
        boolean exiting, landingBuilt;
        String status = "Pod incoming. Drop hard. Paint loud. Skate home.";
        Session(ServerPlayer player, LibertyOperation operation) {
            this.player = player; this.operation = operation;
            this.dimension = operation.homeDefense ? player.serverLevel().dimension() : LibertyWorld.DIMENSION;
            if (operation.campaign()) sector=operation.plan.theaterId();
        }
    }
    private static State state(MinecraftServer server) { return SERVERS.computeIfAbsent(server, ignored -> new State()); }
    private static Session session(Player player) {
        return player instanceof ServerPlayer server && server.getServer() != null ? state(server.getServer()).sessions.get(player.getUUID()) : null;
    }
    public static boolean active(Player player) { return session(player) != null || inShip(player); }
    public static boolean inShip(Player player) { return player instanceof ServerPlayer p && p.getServer()!=null && state(p.getServer()).hubs.containsKey(p.getUUID()); }
    private static boolean rightLevel(Session s) { return s.player.serverLevel().dimension().equals(s.dimension); }
    private record CallContext(Session s) implements LibertyStratagemEngine.Context {
        @Override public ServerPlayer player() { return s.player; }
        @Override public UUID operation() { return s.operation.id; }
        @Override public boolean active() { return session(s.player)==s && !s.exiting && rightLevel(s)
                && (s.operation.field() || s.operation.phase()==LibertyOperation.Phase.DOWNED
                || s.operation.phase()==LibertyOperation.Phase.DROPPING); }
        @Override public String phase() { return s.operation.phase().name(); }
        @Override public boolean validTarget(Vec3 point) { return point!=null && Double.isFinite(point.x) && Double.isFinite(point.y)
                && Double.isFinite(point.z) && s.player.distanceToSqr(point)<=160*160 && LibertyWorld.inside(s.operation,BlockPos.containing(point))
                && s.player.serverLevel().getChunkSource().getChunkNow(BlockPos.containing(point).getX()>>4,BlockPos.containing(point).getZ()>>4)!=null; }
        @Override public boolean unlocked(String id) { return LibertyShipProgress.unlocked(s.player,id); }
        @Override public List<LivingEntity> ownedEnemiesWithin(Vec3 point,double radius) {
            return s.entities.values().stream().filter(e->e instanceof Mob && (s.actors.containsKey(e.getUUID()) || e instanceof LibertyCoreEntity) && e.isAlive()
                    && e.position().distanceToSqr(point)<=radius*radius).map(e->(LivingEntity)e).toList();
        }
        @Override public void track(Entity entity) { mark(entity,s,-1); }
        @Override public boolean damageOwned(Entity entity,float damage) {
            return entity instanceof LivingEntity && belongsTo(entity,s.player) && !(entity instanceof LibertySupportEntity)
                    && entity.hurt(s.player.damageSources().playerAttack(s.player),Math.max(0,Math.min(2000,damage)));
        }
        @Override public int paintArea(Vec3 point,double radius) { return LibertyManager.paintArea(s,point,radius); }
        @Override public boolean terrainSafe() { return true; }
        @Override public boolean nativeProviderSafe() { return !s.operation.homeDefense && LibertyWorld.isLiberty(s.player.level()); }
        @Override public boolean supply() {
            if (s.operation.resupplies()<=0 || !LibertyArmory.resupply(s.player)) return false;
            s.operation.spendResupply();
            s.player.heal(8); s.player.getCapability(JetSetDataProvider.CAPABILITY).ifPresent(v->v.setBoost(100)); return true;
        }
        @Override public boolean personalSupply() {
            if(!LibertyArmory.resupply(s.player))return false;
            s.player.heal(8);s.player.getCapability(JetSetDataProvider.CAPABILITY).ifPresent(v->v.setBoost(100));return true;
        }
        @Override public boolean reinforce() { return LibertyManager.reinforce(s); }
        @Override public boolean missionAction(String id,Vec3 point) {
            return LibertyManager.missionAction(s,id,point);
        }
        @Override public boolean missionReady(String id) { return LibertyManager.missionReady(s,id); }
        @Override public boolean canSupply() { return s.operation.field()&&s.operation.resupplies()>0; }
        @Override public boolean canReinforce() { return s.operation.phase()==LibertyOperation.Phase.DOWNED&&s.reinforcements>0; }
        @Override public double cooldownMultiplier() { return LibertyShipProgress.cooldownMultiplier(s.player); }
        @Override public int bonusEagleCharges() { return LibertyShipProgress.bonusEagleCharges(s.player); }
    }
    private static int paintArea(Session s,Vec3 point,double radius) {
        if (s.operation.mission==null || !s.operation.field() || !rightLevel(s)) return 0;
        ServerLevel level=s.player.serverLevel(); int stride=radius>15?8:3;
        int bound=(int)Math.ceil(Math.min(64,radius));List<BlockPos> applied=new ArrayList<>();
        int minX=Math.floorDiv((int)Math.floor(point.x-bound),stride)*stride,maxX=(int)Math.ceil(point.x+bound);
        int minZ=Math.floorDiv((int)Math.floor(point.z-bound),stride)*stride,maxZ=(int)Math.ceil(point.z+bound);
        for (int x=minX;x<=maxX;x+=stride) for (int z=minZ;z<=maxZ;z+=stride) {
            if ((x+.5-point.x)*(x+.5-point.x)+(z+.5-point.z)*(z+.5-point.z)>radius*radius) continue;
            BlockPos p=BlockPos.containing(x,point.y,z);
            if (level.getChunkSource().getChunkNow(p.getX()>>4,p.getZ()>>4)==null) continue;
            p=surface(s,p); if (p==null) continue;
            if(addPaint(s,p,Direction.UP,stride+.08f))applied.add(p);
        }
        int changed=s.operation.mission.paintArea(point,Math.max(.5,Math.min(96,radius)),cell->applied.stream().anyMatch(p->
                Math.abs(p.getX()-cell.getX())<=stride*.5 && Math.abs(p.getZ()-cell.getZ())<=stride*.5 && Math.abs(p.getY()-cell.getY())<=12));
        if (changed>0 && s.operation.awardObjectives()) checkpoint(s);
        return changed;
    }
    private static boolean addPaint(Session s,BlockPos support,Direction face,float size) {
        for(Entity entity:s.entities.values())if(entity instanceof GraffitiEntity old&&!old.isRemoved()&&old.getFace()==face&&old.getSupportPos()!=null) {
            if(old.getSupportPos().equals(support)&&Math.abs(old.getRenderWidth()-size)<.1)return true;
            double fits=(size-old.getRenderWidth())*.5;
            if(face==Direction.UP&&fits>0&&old.getSupportPos().getY()==support.getY()
                    &&Math.abs(old.getSupportPos().getX()-support.getX())<=fits&&Math.abs(old.getSupportPos().getZ()-support.getZ())<=fits)old.discard();
        }
        long count=s.entities.values().stream().filter(e->e instanceof GraffitiEntity && !e.isRemoved()).count();
        if (count>=256) s.entities.values().stream().filter(e->e instanceof GraffitiEntity && !e.isRemoved()).findFirst().ifPresent(Entity::discard);
        GraffitiEntity paint=new GraffitiEntity(ModEntities.GRAFFITI.get(),s.player.serverLevel()); mark(paint,s,-1);
        // The strict 16x10 palette payload is generated server-side, with two fixed crew colors.
        paint.configureMissionSplash(support,face,MISSION_PAINT,size);
        if (!s.player.serverLevel().addFreshEntity(paint)) { s.entities.remove(paint.getUUID()); paint.discard(); return false; }
        return true;
    }
    private static BlockPos surface(Session s,BlockPos point) {
        ServerLevel level=s.player.serverLevel();
        if (level.getChunkSource().getChunkNow(point.getX()>>4,point.getZ()>>4)==null) return null;
        int y=s.operation.homeDefense ? level.getHeight(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES,point.getX(),point.getZ())-1 : s.operation.origin.getY();
        BlockPos surface=new BlockPos(point.getX(),y,point.getZ());
        return level.getBlockState(surface).isFaceSturdy(level,surface,Direction.UP) ? surface : null;
    }
    public static boolean safeHomeSpray(ServerPlayer player,BlockPos support,Direction face) {
        Session s=session(player);
        if (s==null || !s.operation.homeDefense || !s.operation.field() || player.distanceToSqr(Vec3.atCenterOf(support))>36
                || !player.serverLevel().getBlockState(support).isFaceSturdy(player.level(),support,face)) return false;
        if(!addPaint(s,support,face,2.5f))return false;tagWall(player,support,face); return true;
    }
    public static boolean homeDefense(Player player) { Session s=session(player); return s!=null && s.operation.homeDefense; }
    public static boolean adoptProviderEntity(Entity entity) {
        if (!(entity.level() instanceof ServerLevel level)) return false;
        for (Session s:state(level.getServer()).sessions.values()) if (s.stratagems!=null && rightLevel(s)
                && s.stratagems.adoptProviderEntity(entity)) return true;
        return false;
    }
    public static boolean useEquipment(ServerPlayer player,net.minecraft.world.InteractionHand hand,boolean alternate) {
        Session s=session(player); if (s==null || s.stratagems==null) return false;
        var result=s.stratagems.useEquipment(hand,alternate); if (!result.accepted()) player.displayClientMessage(Component.literal(result.message()),true);
        return result.accepted();
    }
    public static float protectedDamage(ServerPlayer player,net.minecraft.world.damagesource.DamageSource source,float damage) {
        Session s=session(player); return s==null || s.stratagems==null ? damage : s.stratagems.protectedDamage(source,damage);
    }
    public static boolean downed(ServerPlayer player) {
        Session s=session(player); if (s==null || !s.operation.campaign() || !s.operation.field() || s.reinforcements<=0) return false;
        s.operation.phase(LibertyOperation.Phase.DOWNED); player.setHealth(1); player.clearFire(); player.stopRiding();
        s.status="Crew member down. Call Reinforce from the field radio. Remaining drops: "+s.reinforcements; send(player,true,""); return true;
    }
    private static boolean reinforce(Session s) {
        if (s.operation.phase()!=LibertyOperation.Phase.DOWNED || s.reinforcements<=0) return false;
        s.reinforcements--; s.player.setHealth(s.player.getMaxHealth()); s.player.clearFire();
        if (s.transport!=null) s.transport.discard();
        s.operation.phase(LibertyOperation.Phase.DROPPING);
        relocate(s.player,s.operation.drop.getX()+.5,s.operation.drop.getY()+66,s.operation.drop.getZ()+.5);
        s.transport=transport(s,false,s.player.position()); s.player.startRiding(s.transport,true); s.previous=null;
        s.status="Reinforcement inbound. Objectives remain secured."; return true;
    }
    private static LibertyMissionRuntime.Site commandSite(Session s,String command) {
        if(s.operation.mission==null)return null;
        String template=s.operation.plan.template().id();
        return s.operation.mission.sites().stream().filter(site->site.active()&&!site.complete()
                && s.player.distanceToSqr(Vec3.atCenterOf(site.center()))<=32*32
                && !s.missionCommands.contains(command+"/"+site.id())).filter(site->switch(command) {
                    case "nux_223_hellbomb","hive_breaker_drill","tectonic_drill" -> site.kind()==LibertyMissionCatalog.ObjectiveKind.DEMOLITION
                            && (command.equals("nux_223_hellbomb")||s.operation.plan.faction()==LibertyMissionCatalog.Faction.TERMINID);
                    case "tactical_video_camera" -> site.kind()==LibertyMissionCatalog.ObjectiveKind.SCAN
                            && s.operation.plan.faction()==LibertyMissionCatalog.Faction.ILLUMINATE;
                    case "aquifer_drill" -> site.kind()==LibertyMissionCatalog.ObjectiveKind.SCAN&&template.equals("pigment_pipeline");
                    case "prospecting_drill","seismic_probe","activate_e_711_extraction_drill" -> site.kind()==LibertyMissionCatalog.ObjectiveKind.SCAN
                            && (template.equals("sample_heist")||template.equals("seismic_session")||template.equals("nest_diving"));
                    case "dark_fluid_vessel" -> template.equals("dark_ink_protocol") && (site.kind()==LibertyMissionCatalog.ObjectiveKind.SCAN||site.kind()==LibertyMissionCatalog.ObjectiveKind.DEFEND);
                    case "sssd_delivery","cargo_container" -> site.kind()==LibertyMissionCatalog.ObjectiveKind.COURIER||site.kind()==LibertyMissionCatalog.ObjectiveKind.ESCORT;
                    case "portable_comms_relay","upload_data" -> site.kind()==LibertyMissionCatalog.ObjectiveKind.RELAY_HOLD||site.kind()==LibertyMissionCatalog.ObjectiveKind.SCAN;
                    case "super_earth_flag" -> site.kind()==LibertyMissionCatalog.ObjectiveKind.RELAY_HOLD&&template.equals("raise_the_colors");
                    case "reinforcement_pods" -> site.kind()==LibertyMissionCatalog.ObjectiveKind.ESCORT&&template.equals("evacuation_rush");
                    default -> false;
                }).findFirst().orElse(null);
    }
    private static boolean missionReady(Session s,String id) {
        if(!s.operation.field()||s.operation.mission==null)return false;
        if(id.equals("sos_beacon"))return s.allies.isEmpty();
        if(id.equals("seaf_artillery"))return s.artilleryShells>0;
        return commandSite(s,id)!=null;
    }
    private static boolean missionAction(Session s,String id,Vec3 point) {
        if(!missionReady(s,id))return false;
        if(id.equals("sos_beacon"))return reliefCrew(s);
        if(id.equals("seaf_artillery")) {
            s.artilleryShells--; s.missionBlasts.put(s.operation.ticks()+40,point);
            s.status="Artillery shell inbound. Shells remaining: "+s.artilleryShells; return true;
        }
        var site=commandSite(s,id); if(site==null)return false;
        if(id.equals("reinforcement_pods")) {
            if(!reliefCrew(s))return false;
            s.operation.mission.interact(s.player.position());
        } else if(site.kind()==LibertyMissionCatalog.ObjectiveKind.DEMOLITION) {
            if(point.distanceToSqr(Vec3.atCenterOf(site.center()))>24*24)return false;
            s.missionBlasts.put(s.operation.ticks()+100,point);
            missionMarker(s,point,id.contains("hellbomb")?"Hellbomb armed • 5 seconds":"Breach drill • 5 seconds",Items.ANVIL);
        } else {
            boolean needsActivation=site.kind()==LibertyMissionCatalog.ObjectiveKind.SCAN||site.kind()==LibertyMissionCatalog.ObjectiveKind.COURIER||site.kind()==LibertyMissionCatalog.ObjectiveKind.ESCORT;
            if(needsActivation&&!s.operation.mission.interact(s.player.position()))return false;
            ItemStack token=new ItemStack(id.equals("tactical_video_camera")?Items.SPYGLASS:id.contains("flag")?Items.WHITE_BANNER:Items.PAPER);
            token.setHoverName(Component.literal(LibertyStratagemCatalog.find(id).name()));LibertyTaczCompat.markLoan(token,s.player.getUUID());
            int slot=s.player.getInventory().getItem(5).isEmpty()?5:s.player.getInventory().getFreeSlot();
            if(slot>=0)s.player.getInventory().setItem(slot,token);
            missionMarker(s,Vec3.atCenterOf(site.center()).add(2,1,2),token.getHoverName().getString(),token.getItem());
        }
        s.missionCommands.add(id+"/"+site.id());s.player.inventoryMenu.broadcastChanges();return true;
    }
    private static void missionMarker(Session s,Vec3 position,String name,net.minecraft.world.item.Item item) {
        var marker=new net.minecraft.world.entity.decoration.ArmorStand(EntityType.ARMOR_STAND,s.player.serverLevel());
        marker.setNoGravity(true);marker.setInvulnerable(true);marker.setCustomName(Component.literal(name));marker.setCustomNameVisible(true);
        marker.setItemSlot(EquipmentSlot.HEAD,new ItemStack(item));marker.setPos(position.x,position.y,position.z);mark(marker,s,-1);
        marker.getPersistentData().getCompound(OWNED).putLong("Expires",s.operation.ticks()+400);
        if(!s.player.serverLevel().addFreshEntity(marker)){s.entities.remove(marker.getUUID());marker.discard();}
    }
    private static boolean reliefCrew(Session s) {
        if(!s.allies.isEmpty())return false;
        for(int offset:new int[]{-4,4}) {
            BlockPos pos=surface(s,s.player.blockPosition().offset(offset,0,4));if(pos==null)continue;
            Entity rawAlly=LibertyMobSafety.spawnType(EntityType.SKELETON,s.operation.homeDefense).create(s.player.serverLevel());
            if(!(rawAlly instanceof Mob ally))continue;
            ally.moveTo(pos.getX()+.5,pos.getY()+1.1,pos.getZ()+.5,0,0);
            if(!s.player.serverLevel().noCollision(ally)){ally.discard();continue;}
            LibertyMobSafety.initialize(ally,s.player.serverLevel(),pos);
            ally.setPersistenceRequired();ally.setCanPickUpLoot(false);ally.setItemSlot(EquipmentSlot.HEAD,new ItemStack(Items.IRON_HELMET));
            for(EquipmentSlot slot:EquipmentSlot.values())ally.setDropChance(slot,0);
            ally.setCustomName(Component.literal("Liberty relief crew"));ally.setCustomNameVisible(true);mark(ally,s,-1);
            if(s.player.serverLevel().addFreshEntity(ally))s.allies.put(ally.getUUID(),s.operation.ticks()+2400);
            else{s.entities.remove(ally.getUUID());ally.discard();}
        }
        return !s.allies.isEmpty();
    }
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
        if (session(player)==null) state(player.getServer()).menus.put(player.getUUID(), new Menu(state(player.getServer()).nextMenu++, now(player) + 2400));
        send(player, true, "");
    }

    public static void handleAction(ServerPlayer player, String action, String value, UUID missionId, long revision) {
        if (player == null || player.getServer() == null || action == null || value == null || action.length() > 32 || value.length() > 128) return;
        State data = state(player.getServer()); long tick = now(player);
        if (tick < data.actions.getOrDefault(player.getUUID(), tick - 2) + 2) return;
        data.actions.put(player.getUUID(), tick);
        if (action.equals("open")) { open(player); return; }
        Session s = session(player);
        if (action.equals("armory_select")) {
            if (s!=null || hasReturn(player)&&!inShip(player)) { send(player,true,"Choose a loadout before launching your pod.");return; }
            String[] parts=value.split("\\|",-1);
            boolean accepted=parts.length==2 && LibertyArmory.select(player,parts[0],parts[1],revision);
            if (accepted&&!RushPlayerStorage.flush(player)) {send(player,true,"Your loadout is retained; player saves need to recover before launch.");return;}
            send(player,true,accepted?"Pre-drop loadout saved.":"That equipment selection is unavailable or stale.");return;
        }
        if (action.equals("ship_purchase") || action.equals("ship_equip")) {
            if (s!=null || hasReturn(player) && !inShip(player)) { send(player,true,"Manage your ship equipment after extraction."); return; }
            boolean accepted=false;
            if (action.equals("ship_purchase")) accepted=LibertyShipProgress.purchase(player,value,revision);
            else {
                String[] parts=value.split("\\|",-1);
                try { if (parts.length==2) accepted=LibertyShipProgress.equip(player,parts[0],Integer.parseInt(parts[1]),revision); }
                catch (NumberFormatException ignored) { }
            }
            if (accepted && !RushPlayerStorage.flush(player)) { send(player,true,"Ship changes are retained; the server needs to save your account before deployment."); return; }
            send(player,true,accepted?"Ship equipment updated.":"That purchase or loadout is stale, locked or unaffordable."); return;
        }
        if (action.equals("research") || action.equals("research_select")) {
            boolean accepted = action.equals("research") ? LibertyResearch.purchase(player, value, revision)
                    : LibertyResearch.selectPreparation(player, value, revision);
            if (accepted && !RushPlayerStorage.flush(player)) { send(player, true, "Research is updated in memory; reconnect after the server can save player data."); return; }
            send(player, true, accepted ? action.equals("research") ? "Research completed. Your new technique is ready." : "Next Rush preparation selected."
                    : "That research choice is stale, locked, unaffordable or unavailable during an expedition. Review the refreshed ledger."); return;
        }
        if (action.equals("deploy") || action.equals("campaign_deploy") || action.equals("defense_deploy") || action.equals("ship")) {
            Menu menu = data.menus.get(player.getUUID());
            if (missionId != null || menu == null || revision != menu.revision || tick > menu.expires) {
                open(player); send(player, true, "Review the refreshed war map before deploying."); return;
            }
            switch(action) {
                case "campaign_deploy" -> startCampaign(player,value);
                case "defense_deploy" -> startDefense(player,value);
                case "ship" -> enterShip(player,-1);
                default -> start(player,value);
            }
            return;
        }
        if (s == null) { if (action.equals("abort")) recover(player); else open(player); return; }
        if (!s.operation.matches(missionId, revision)) { send(player, true, "Mission state changed. Review the current operation."); return; }
        switch (action) {
            case "abort" -> leave(player, false, "Operation aborted. Earned super credits and objective crew progress were retained; field samples were left behind.");
            case "extract" -> {
                if (s.operation.extract(player.position())) { s.status = "Extraction called. Hold within 48 blocks of the pad for 30 seconds."; send(player, false, ""); }
                else send(player, true, "Secure every required objective, then call extraction within 12 blocks of the landing pad.");
            }
            case "board" -> {
                if (s.transport != null && s.transport.isAlive() && s.operation.board(player.position())) {
                    s.status = "All aboard. Liberty is leaving the station.";
                    player.startRiding(s.transport, true); send(player, false, "");
                } else send(player, true, "Approach the landed ship within eight blocks to board.");
            }
            case "resupply" -> resupply(s);
            case "callin" -> {
                if (s.stratagems==null) break;
                Vec3 start=player.getEyePosition(), end=start.add(player.getLookAngle().scale(128));
                BlockHitResult hit=player.serverLevel().clip(new ClipContext(start,end,ClipContext.Block.COLLIDER,ClipContext.Fluid.NONE,player));
                Vec3 target=hit.getType()==HitResult.Type.MISS ? player.position() : hit.getLocation();
                var result=s.stratagems.call(value,target);
                s.status=result.message(); send(player,true,"");
            }
            case "terminal" -> {
                boolean accepted=s.operation.mission!=null && s.operation.field() && s.operation.mission.interact(player.position());
                s.status=accepted?"Terminal synchronized. Keep the zone secure.":"Approach an active terminal or escort beacon to interact.";
                send(player,true,"");
            }
            case "reinforce" -> { if (reinforce(s)) send(player,false,""); else send(player,true,"No reinforcement is currently available."); }
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
        if(!LibertyTaczCompat.available())return LibertyTaczCompat.status();
        return com.herberto.jetsetcraft.compat.LibertyHelldiversCompat.terrainSafeTaczApi()?""
                :"This TaCZ version cannot safely run Liberty weapons. Install the supported TaCZ 1.1.8-hotfix2 with matching JetSetCraft on both sides.";
    }

    /** Explicit server command and war-map deployment share this entry path. */
    public static boolean start(ServerPlayer player, String theaterId) {
        return deploy(player, theaterId, null, false, "", null);
    }
    public static boolean startCampaign(ServerPlayer player, String selection) {
        if (player==null || player.getServer()==null || selection==null) return false;
        String[] parts=selection.split("\\|",-1);
        if (parts.length!=4) return fail(player,"Choose a region, mission, difficulty and operation length.");
        try {
            var sector=LibertyCampaignData.sector(parts[0]); int difficulty=Integer.parseInt(parts[2]), minutes=Integer.parseInt(parts[3]);
            var campaign=LibertyCampaignData.get(player.getServer());
            if (sector==null || !campaign.canDeploy(parts[0],difficulty)) return fail(player,campaign.deployReason(parts[0],difficulty));
            var faction=LibertyMissionCatalog.faction(sector.factionId());
            var plan=LibertyMissionPlan.generate(player.getRandom().nextLong(),parts[0],faction,parts[1],difficulty,minutes);
            if (inShip(player) && !leave(player,false,"Deploying from the ship.")) return false;
            return deploy(player,sector.theaterId(),plan,false,parts[0],null);
        } catch (IllegalArgumentException error) { return fail(player,"The selected operation is unavailable. Refresh the war map."); }
    }
    public static boolean startDefense(ServerPlayer player, String selection) {
        String[] parts=selection.split("\\|",-1);
        if (parts.length!=3) return fail(player,"Choose a defense offer and difficulty.");
        try {
            UUID offerId=UUID.fromString(parts[0]); int difficulty=Integer.parseInt(parts[1]), minutes=Integer.parseInt(parts[2]);
            var campaign=LibertyCampaignData.get(player.getServer()); var offer=campaign.offer(player.getUUID(),offerId).orElse(null);
            if (offer==null) return fail(player,"That defense has changed. Refresh the war map.");
            var sector=LibertyCampaignData.sector(offer.sectorId());
            var faction=LibertyMissionCatalog.faction(offer.factionId());
            var plan=LibertyMissionPlan.defense(offer.id(),sector.id(),faction,difficulty,minutes);
            if (inShip(player) && !leave(player,false,"Deploying to defend your home front.")) return false;
            return deploy(player,sector.theaterId(),plan,offer.homeWorld(),sector.id(),offerId);
        } catch (IllegalArgumentException error) { return fail(player,"The defense offer is no longer available."); }
    }
    private static boolean deploy(ServerPlayer player, String theaterId, LibertyMissionPlan plan, boolean home,
                                  String sector, UUID defense) {
        if (player == null || player.getServer() == null) return false;
        String blocked = unavailable(player);
        if (!blocked.isEmpty()) return fail(player, blocked);
        if (plan!=null && (!LibertyArmory.ready(player) || !LibertyShipProgress.canReceiveResult(player)
                || !com.herberto.jetsetcraft.compat.LibertyHelldiversCompat.terrainSafeTaczApi()))
            return fail(player,!LibertyArmory.ready(player)?LibertyArmory.readyReason(player):"Your ship account or the pinned weapon safety bridge needs recovery before deployment.");
        LibertyOperation.Theater theater = LibertyOperation.theater(theaterId);
        if (theater == null) return fail(player, "Choose an available war-map district.");
        if (home && !LibertyMobSafety.homeGuardReady(player))
            return fail(player, "Home defense needs the verified Minecraft 1.20.1 contact guards. Your offer and belongings are unchanged.");
        BlockPos homeOrigin=home?homeOrigin(player):null;
        if (home&&homeOrigin==null) return fail(player,"Home defense needs an open, solid landing area near you. Move outdoors and deploy again.");
        if (home&&!homePlanReady(player.serverLevel(),homeOrigin,plan))
            return fail(player,"This home front needs more loaded, open land for its objectives and extraction. Move to a larger outdoor area; your offer and belongings are unchanged.");
        State data = state(player.getServer()); int slot = data.sessions.size() >= 32 ? -1 : LibertyDistricts.get(player.getServer()).allocate();
        if (slot < 0) return fail(player, "All expedition districts are occupied. Wait for a crew to return.");
        if (!LibertyDistricts.saveAcknowledged(player.getServer()))
            return fail(player, "The server could not save your district allocation. No gear or location was changed.");
        UUID mission = UUID.randomUUID();
        if (defense!=null) {
            var campaign=LibertyCampaignData.get(player.getServer());
            if (!campaign.claimDefense(player.getUUID(),defense,mission,campaign.revision()) || !LibertyCampaignData.saveAcknowledged(player.getServer())) {
                releaseDefense(player,mission);return fail(player,"Could not save the accepted defense. Your ordinary state was not changed.");
            }
        }
        if (!RushReturnState.capture(player, RETURN_KEY)) {releaseDefense(player,mission);return fail(player, "Could not preserve your return checkpoint. No gear or location was changed.");}
        if (!LibertyResearch.begin(player, mission)) {
            persisted(player).remove(RETURN_KEY);releaseDefense(player,mission); return fail(player, "Research settlement needs recovery before another expedition.");
        }
        BlockPos origin = home ? homeOrigin : plan!=null ? LibertyWorld.campaignOrigin(slot)
                : new BlockPos(1_000_008 + slot % 512 * 1024, LibertyWorld.FLOOR_Y, 1_000_008 + slot / 512 * 1024);
        LibertyOperation op = plan==null ? new LibertyOperation(mission, player.getUUID(), theater, origin, slot,
                LibertyResearch.unlocked(player, "signal_lacquer"), LibertyResearch.unlocked(player, "supply_skates"))
                : new LibertyOperation(mission,player.getUUID(),theater,origin,slot,plan,home,LibertyResearch.unlocked(player,"supply_skates"));
        Session s = new Session(player, op); s.sector=sector; s.defense=defense==null?"":defense.toString(); writeOutcome(s, false);
        if (plan!=null) op.addResupplies(LibertyShipProgress.bonusResupplies(player));
        // The ordinary inventory is durable before any item is lent or dimension transfer occurs.
        if (!RushPlayerStorage.flush(player)) {
            LibertyResearch.abandonPending(player); persisted(player).remove(RETURN_KEY); persisted(player).remove(OUTCOME_KEY);
            releaseDefense(player,mission);
            return fail(player, "The server could not save your checkpoint. Your Minecraft belongings were not changed.");
        }
        data.sessions.put(player.getUUID(), s); data.menus.remove(player.getUUID());
        if (plan!=null) { s.stratagems=new LibertyStratagemEngine(new CallContext(s)); s.stratagems.setLoadout(LibertyShipProgress.equipped(player)); }
        try {
            ItemStack ride = player.getCapability(JetSetDataProvider.CAPABILITY).map(value -> value.rideGear().copy()).orElse(ItemStack.EMPTY);
            if (ride.isEmpty()) ride = new ItemStack(ModItems.INLINE_SKATES.get());
            LibertyTaczCompat.markLoan(ride, player.getUUID());
            player.getInventory().clearContent(); player.containerMenu.setCarried(ItemStack.EMPTY);
            if (!(plan==null ? LibertyTaczCompat.provision(player) : LibertyArmory.provision(player)))
                throw new IllegalStateException("Helldivers loadout changed while deploying");
            ItemStack spray = new ItemStack(ModItems.SPRAY_CAN.get()); spray.getOrCreateTag().putBoolean("Unbreakable", true);
            if (plan!=null) spray.getOrCreateTag().putBoolean(SprayCanItem.FREE_PAINT_TAG,true);
            LibertyTaczCompat.markLoan(spray, player.getUUID()); player.getInventory().setItem(2, spray);
            ItemStack food = new ItemStack(Items.COOKED_BEEF, 16); LibertyTaczCompat.markLoan(food, player.getUUID()); player.getInventory().setItem(3, food);
            ItemStack radio = new ItemStack(Items.COMPASS); radio.setHoverName(Component.literal("Liberty field radio"));
            LibertyTaczCompat.markLoan(radio, player.getUUID()); radio.getOrCreateTag().putBoolean("LibertyFieldRadio", true);
            player.getInventory().setItem(4, radio);
            ItemStack helmet = new ItemStack(Items.IRON_HELMET), chest = new ItemStack(Items.IRON_CHESTPLATE), legs = new ItemStack(Items.IRON_LEGGINGS);
            for (ItemStack stack : List.of(helmet, chest, legs)) LibertyTaczCompat.markLoan(stack, player.getUUID());
            if (plan==null) { player.setItemSlot(EquipmentSlot.HEAD, helmet); player.setItemSlot(EquipmentSlot.CHEST, chest); player.setItemSlot(EquipmentSlot.LEGS, legs); }
            ItemStack equipped = ride;
            player.getCapability(JetSetDataProvider.CAPABILITY).ifPresent(value -> {
                value.load(new CompoundTag()); value.setRideGear(equipped);
                if (equipped.getItem() instanceof RideGearItem gear) value.setStyle(gear.style());
                value.setActive(true); value.setBoost(100);
            });
            player.setGameMode(GameType.SURVIVAL); player.removeAllEffects(); player.setHealth(player.getMaxHealth());
            player.setAbsorptionAmount(0); player.clearFire(); player.getFoodData().setFoodLevel(20); player.getFoodData().setSaturation(6);
            player.fallDistance = 0; player.getInventory().selected = 0;
            ServerLevel district = home ? player.serverLevel() : player.getServer().getLevel(LibertyWorld.DIMENSION);
            player.teleportTo(district, op.drop.getX() + .5, op.drop.getY() + 66, op.drop.getZ() + .5, 0, 0);
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
    private static BlockPos homeOrigin(ServerPlayer player) {
        ServerLevel level=player.serverLevel();
        for (int radius=0;radius<=48;radius+=4) for (int dx=-radius;dx<=radius;dx+=4) for (int dz=-radius;dz<=radius;dz+=4) {
            if (Math.max(Math.abs(dx),Math.abs(dz))!=radius) continue;
            BlockPos pos=player.blockPosition().offset(dx,0,dz);
            if (level.getChunkSource().getChunkNow(pos.getX()>>4,pos.getZ()>>4)==null) continue;
            int y=level.getHeight(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES,pos.getX(),pos.getZ())-1; pos=pos.atY(y);
            if (Math.abs(y+1-player.getY())>24 || !level.getBlockState(pos).isFaceSturdy(level,pos,Direction.UP)) continue;
            if (level.noCollision(new AABB(pos.getX()-.5,y+1,pos.getZ()-.5,pos.getX()+1.5,y+4,pos.getZ()+1.5))) return pos;
        }
        return null;
    }

    /** Inspect only resident chunks before capturing a player or claiming a defense offer. */
    private static boolean homePlanReady(ServerLevel level,BlockPos origin,LibertyMissionPlan plan) {
        if (plan==null) return false;
        var preview=new LibertyMissionRuntime(plan,origin,true);
        double scale=96.0/plan.arenaRadius();
        BlockPos extraction=origin.offset((int)(plan.extraction().x()*scale),0,(int)(plan.extraction().z()*scale));
        if(LibertyHomeTerrain.footing(level,extraction)==null)return false;
        for(var site:preview.sites()) {
            if(site.optional())continue;
            var ground=LibertyHomeTerrain.resolve(level,site);
            if(ground==null || !preview.relocateSite(site.id(),ground))return false;
            BlockPos center=site.center();
            if(site.kind()==LibertyMissionCatalog.ObjectiveKind.TERRITORY) {
                int available=0;
                for(int x=-site.radius();x<=site.radius();x++)for(int z=-site.radius();z<=site.radius();z++) {
                    if((x+.5)*(x+.5)+(z+.5)*(z+.5)>site.radius()*site.radius())continue;
                    BlockPos support=LibertyHomeTerrain.footing(level,center.offset(x,0,z));
                    if(support!=null&&Math.abs(support.getY()-center.getY())<=7)available++;
                }
                if(available<site.goal())return false;
            }
            for(var core:preview.cores())if(core.siteId().equals(site.id())&&LibertyHomeTerrain.footing(level,core.position())==null)return false;
        }
        return true;
    }

    public static void tick(MinecraftServer server) {
        State data=state(server);
        LibertyCampaignData.get(server).tick(server.overworld().getGameTime(),data.sessions.values().stream().anyMatch(s->s.operation.campaign()&&s.operation.field()));
        for (Hub hub:List.copyOf(data.hubs.values())) {
            if (!LibertyWorld.isLiberty(hub.player.level())) { leave(hub.player,false,"Returned from your ship."); continue; }
            if (!hub.built) hub.built=LibertyWorld.buildShip(hub.player.serverLevel(),hub.origin);
            if (hub.player.distanceToSqr(Vec3.atCenterOf(hub.origin))>60*60 || hub.player.getY()<hub.origin.getY())
                relocate(hub.player,hub.origin.getX()+.5,hub.origin.getY()+1.1,hub.origin.getZ()+14.5);
            hub.player.setHealth(hub.player.getMaxHealth()); hub.player.getFoodData().setFoodLevel(20);
        }
        // Give low-pressure operations first access to shared admission slots, rotating ties
        // instead of favoring a fixed HashMap order. Every session still ticks once per tick.
        List<Session> sessions = new ArrayList<>(data.sessions.values());
        sessions.sort((left, right) -> left.player.getUUID().compareTo(right.player.getUUID()));
        int first = sessions.isEmpty() ? 0 : (int) Math.floorMod(data.nextSession++, (long) sessions.size());
        java.util.Collections.rotate(sessions, -first);
        sessions.sort((left, right) -> Double.compare(residentPressure(left), residentPressure(right)));
        for (int offset = 0; offset < sessions.size(); offset++) {
            Session s = sessions.get(offset);
            if (s.exiting || data.sessions.get(s.player.getUUID()) != s) continue;
            try { tick(s); }
            catch (RuntimeException error) { JetSetCraft.LOGGER.error("Liberty Grind interrupted for {}", s.player.getUUID(), error);
                leave(s.player, false, "Operation interrupted; your return checkpoint was preserved."); }
        }
    }
    private static double residentPressure(Session s) {
        int cap = s.operation.campaign() ? s.operation.plan.difficulty().residentCap() : ACTOR_LIMIT;
        return (double) s.actors.size() / cap;
    }
    private static void tick(Session s) {
        ServerPlayer player = s.player; LibertyOperation op = s.operation; ServerLevel level = player.serverLevel();
        if (!rightLevel(s)) { leave(player, false, "Left the expedition district. Your Minecraft state was restored."); return; }
        if (op.phase() == LibertyOperation.Phase.RETURNING) { leave(player, false, "Retrying your saved return."); return; }
        op.tick();
        if (op.expired()) { leave(player, false, "Mission time expired. Earned credits and crew progress retained; field samples lost."); return; }
        if (!op.homeDefense) {
            LibertyWorld.decorateNear(level, player.blockPosition(), op);
            if (!s.landingBuilt) s.landingBuilt = LibertyWorld.buildLanding(level, op.extraction);
        }
        s.entities.entrySet().removeIf(entry -> entry.getValue().isRemoved());
        s.actors.entrySet().removeIf(entry -> !s.entities.containsKey(entry.getKey()));
        if (op.campaign() && op.field() && op.ticks() % 20 == 0) retireDistantCombatants(s);
        if (op.homeDefense) {
            BlockPos ground=surface(s,op.extraction); if (ground!=null) op.extraction=ground;
        }
        switch (op.phase()) {
            case DROPPING -> {
                double t = Math.min(1, op.phaseTicks() / 80.0), y = op.origin.getY() + 1 + 65 * (1 - t) * (1 - t);
                moveTransport(s, op.drop, y, true);
                if (op.phaseTicks() >= 80) {
                    op.phase(LibertyOperation.Phase.ACTIVE);
                    player.stopRiding(); relocate(player, op.drop.getX() + .5, op.drop.getY() + 1.1, op.drop.getZ() + .5);
                    s.transport.discard(); s.transport = null; player.setDeltaMovement(Vec3.ZERO); player.fallDistance = 0;
                    level.sendParticles(ParticleTypes.CLOUD, player.getX(), LibertyWorld.FLOOR_Y + 1, player.getZ(), 32, 2, .2, 2, .02);
                    level.playSound(null, player.blockPosition(), SoundEvents.GENERIC_EXPLODE, SoundSource.PLAYERS, .65f, .8f);
                    s.status = op.campaign() ? "Operation live: "+op.plan.template().name()+". Follow active objectives; optional strongholds reduce pressure."
                            : "Find the yellow mural panels. Clear rivals, tag the walls and keep moving."; send(player, false, "");
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
                moveTransport(s, op.extraction, extractionY(s) + 1 + 16 * (1 - Math.min(1, op.phaseTicks() / 60.0)), false);
                if (op.phaseTicks() >= 1800) { s.transport.discard(); s.transport = null; op.phase(LibertyOperation.Phase.ACTIVE); s.status = "Ship departed empty. Your objectives remain complete; call again."; }
            }
            case DEPARTING -> {
                moveTransport(s, op.extraction, extractionY(s) + 1 + op.phaseTicks() * .6, true);
                if (op.phaseTicks() >= 100) {
                    boolean ship=op.campaign(); int shipSlot=op.slot;
                    if (leave(player, true, "Extraction complete. Samples delivered, research stocked and your crew rewarded.") && ship) enterShip(player,shipSlot);
                    return;
                }
            }
            default -> { }
        }
        if (op.field()) {
            s.spawnsThisTick=0;
            if (!LibertyWorld.inside(op, player.blockPosition()) || !op.homeDefense && (player.getY() < 50 || player.getY() > 180)) {
                relocate(player, op.drop.getX() + .5, op.drop.getY() + 1.1, op.drop.getZ() + .5);
                player.setDeltaMovement(Vec3.ZERO); player.fallDistance = 0; s.previous = null; s.status = "Stay inside the expedition district. Returned to the drop zone.";
            }
            boolean skating = player.getCapability(JetSetDataProvider.CAPABILITY).map(value -> value.active() && !value.rideGear().isEmpty()).orElse(false)
                    && !player.isPassenger() && !player.getAbilities().flying;
            for (LibertyOperation.Objective objective : op.objectives) {
                if (!s.built.contains(objective.index()) && player.distanceToSqr(Vec3.atCenterOf(objective.center())) < 100 * 100
                        && LibertyWorld.buildObjective(level, objective)) s.built.add(objective.index());
                objective.advance(player.position(), s.previous, skating);
            }
            if (op.campaign()) tickCampaign(s,skating);
            s.previous = player.position();
            if (op.awardObjectives()) { s.status = op.complete() ? "All objectives complete. Head to the landing pad to extract your samples." : "Objective liberated. Research gathered and one resupply charge restored."; checkpoint(s); }
            if (session(player) != s || !op.field()) return;
            if (op.campaign()) spawnCampaign(s);
            else if (op.ticks() - s.lastSpawn >= 20) { s.lastSpawn = op.ticks(); spawnRival(s); }
            if (op.ticks() % 10 == 0) {
                caches(s);
                if (session(player) != s || !op.field() || !rightLevel(s)) return;
                guidance(s);
            }
            if (op.ticks() % 20 == 0) { player.getFoodData().setFoodLevel(20); player.getFoodData().setSaturation(6); }
        }
        if (s.stratagems!=null) s.stratagems.tick();
        if (op.ticks() % 10 == 0) send(player, false, "");
    }
    private static double extractionY(Session s) {
        BlockPos ground=surface(s,s.operation.extraction); return ground==null?s.operation.extraction.getY():ground.getY();
    }
    private static void tickCampaign(Session s,boolean skating) {
        LibertyOperation op=s.operation; ServerLevel level=s.player.serverLevel();
        s.allies.entrySet().removeIf(entry->{Entity entity=s.entities.get(entry.getKey());
            if(entity==null||!entity.isAlive())return true;
            if(op.ticks()>=entry.getValue()){entity.discard();return true;}
            if(entity instanceof Mob mob)mob.setTarget(s.actors.keySet().stream().map(s.entities::get).filter(e->e instanceof LivingEntity&&e.isAlive())
                    .min(java.util.Comparator.comparingDouble(mob::distanceToSqr)).map(e->(LivingEntity)e).orElse(null));
            return false;});
        s.entities.values().stream().filter(e->e.getPersistentData().getCompound(OWNED).contains("Expires")
                && op.ticks()>=e.getPersistentData().getCompound(OWNED).getLong("Expires")).forEach(Entity::discard);
        for(var shot:List.copyOf(s.missionBlasts.entrySet()))if(op.ticks()>=shot.getKey()) {
            s.missionBlasts.remove(shot.getKey());var context=new CallContext(s);
            for(var target:context.ownedEnemiesWithin(shot.getValue(),22))context.damageOwned(target,target instanceof LibertyCoreEntity?600:160);
            level.sendParticles(ParticleTypes.EXPLOSION_EMITTER,shot.getValue().x,shot.getValue().y+1,shot.getValue().z,4,5,1,5,.1);
            level.playSound(null,BlockPos.containing(shot.getValue()),SoundEvents.GENERIC_EXPLODE,SoundSource.PLAYERS,2,.65f);
        }
        if(!s.artilleryUnlocked&&op.mission.sites().stream().anyMatch(site->site.optional()&&site.complete()
                &&(site.kind()==LibertyMissionCatalog.ObjectiveKind.SCAN||site.kind()==LibertyMissionCatalog.ObjectiveKind.RELAY_HOLD))) {
            s.artilleryUnlocked=true;s.artilleryShells=6;s.status="Support uplink secured: six SEAF artillery shells available in the field radio.";
        }
        for (var site:op.mission.sites()) {
            if (op.homeDefense && !s.grounded.contains(site.id())
                    && op.ticks()>=s.nextGroundAttempt.getOrDefault(site.id(),0L)) {
                s.nextGroundAttempt.put(site.id(),op.ticks()+100);
                var ground=LibertyHomeTerrain.resolve(level,site);
                if (ground!=null && op.mission.relocateSite(site.id(),ground)) s.grounded.add(site.id());
            }
            if (op.homeDefense && !s.grounded.contains(site.id())) continue;
            if (!site.active() || site.complete()) continue;
            if (s.player.distanceToSqr(Vec3.atCenterOf(site.center()))>110*110) continue;
            if (!s.sitesBuilt.contains(site.id()) && (op.homeDefense || LibertyWorld.buildSite(level,site,op.plan.faction()))) {
                s.sitesBuilt.add(site.id());
            }
            if (!s.sitesBuilt.contains(site.id())) continue;
            if (site.priorityTarget() && (site.priorityTargetId()==null || !s.entities.containsKey(site.priorityTargetId()))) spawnPriority(s,site);
            var marker=s.markers.get(site.id());
            if (marker==null || marker.isRemoved()) {
                marker=new net.minecraft.world.entity.decoration.ArmorStand(EntityType.ARMOR_STAND,level);
                marker.setNoGravity(true); marker.setInvulnerable(true); marker.setCustomNameVisible(true);
                marker.setCustomName(Component.literal(site.spec().name()));
                marker.setItemSlot(EquipmentSlot.HEAD,new ItemStack(site.kind()==LibertyMissionCatalog.ObjectiveKind.ESCORT?Items.JUKEBOX:Items.SEA_LANTERN));
                mark(marker,s,-1); marker.setPos(site.marker().x,site.marker().y+1,site.marker().z);
                if (level.addFreshEntity(marker)) s.markers.put(site.id(),marker); else {s.entities.remove(marker.getUUID());marker.discard();}
            }
            if (marker!=null) marker.setPos(site.marker().x,site.marker().y+1,site.marker().z);
        }
        for (var core:op.mission.cores()) {
            var site=op.mission.sites().stream().filter(v->v.id().equals(core.siteId())).findFirst().orElse(null);
            if (!core.alive() || site==null || !site.active() || !s.sitesBuilt.contains(site.id())) continue;
            UUID id=s.cores.get(core.id());
            if (id!=null && s.entities.containsKey(id)) continue;
            BlockPos pos=surface(s,core.position()); if (pos==null) continue;
            LibertyCoreEntity entity=new LibertyCoreEntity(ModEntities.LIBERTY_CORE.get(),level);
            entity.kind(op.plan.faction().baseKind().name()); entity.moveTo(pos.getX()+.5,pos.getY()+1,pos.getZ()+.5,0,0);
            entity.getAttribute(Attributes.MAX_HEALTH).setBaseValue(core.health()); entity.setHealth(entity.getMaxHealth());
            if (op.plan.faction()==LibertyMissionCatalog.Faction.ILLUMINATE) entity.setAbsorptionAmount(30);
            entity.setCustomName(Component.literal(op.plan.faction().baseKind().name().replace('_',' ') + " • weak point")); entity.setCustomNameVisible(true);
            mark(entity,s,-1); entity.getPersistentData().getCompound(OWNED).putString("Core",core.id());
            if (level.addFreshEntity(entity)) s.cores.put(core.id(),entity.getUUID()); else {s.entities.remove(entity.getUUID());entity.discard();}
        }
        op.mission.tick(s.player.position(),s.previous,skating,center->(int)s.actors.keySet().stream().map(s.entities::get)
                .filter(e->e!=null && e.isAlive() && e.distanceToSqr(Vec3.atCenterOf(center))<24*24).count(),
                feet->!op.homeDefense || LibertyHomeTerrain.clear(level,feet));
        s.markers.forEach((id,e)-> { var site=op.mission.sites().stream().filter(v->v.id().equals(id)).findFirst().orElse(null);
            if (site!=null && site.complete()) e.discard(); });
    }
    /** Recycle abandoned infantry pressure without granting defeats or refunding the lifetime budget. */
    private static void retireDistantCombatants(Session s) {
        Set<UUID> priorityTargets = new HashSet<>();
        for (var site : s.operation.mission.sites())
            if (site.priorityTargetId() != null) priorityTargets.add(site.priorityTargetId());
        for (UUID id : List.copyOf(s.actors.keySet())) {
            Entity actor = s.entities.get(id);
            if (!(actor instanceof Mob) || !actor.isAlive() || priorityTargets.contains(id)
                    || actor.distanceToSqr(s.player) <= RETIRE_DISTANCE * RETIRE_DISTANCE) continue;
            // Do not visibly remove a combatant in front of another player in the same world.
            if (s.player.serverLevel().players().stream().anyMatch(player -> player.isAlive()
                    && actor.distanceToSqr(player) <= OBSERVER_DISTANCE * OBSERVER_DISTANCE)) continue;
            actor.discard();
            s.actors.remove(id);
            s.entities.remove(id);
            s.retired++;
        }
    }

    private static void spawnCampaign(Session s) {
        LibertyOperation op=s.operation; var difficulty=op.plan.difficulty();
        if (!op.field() || !rightLevel(s) || s.actors.size()>=difficulty.residentCap()
                || s.spawned>=difficulty.allowanceAt(op.ticks(),op.plan.durationMinutes())) return;
        int global=state(s.player.getServer()).sessions.values().stream().mapToInt(v->v.actors.size()).sum();
        if (global>=SHARED_ACTOR_LIMIT) return;
        if (s.waveRemaining==0 && op.ticks()>=s.nextWave) {
            int bases=(int)op.mission.sites().stream().filter(v->v.kind()==LibertyMissionCatalog.ObjectiveKind.DEMOLITION&&v.complete()).count();
            s.waveRemaining=difficulty.waveMin()+s.player.getRandom().nextInt(difficulty.waveMax()-difficulty.waveMin()+1);
            int interval=difficulty.waveIntervalTicks()+Math.min(200,bases*20);
            if (op.phase()!=LibertyOperation.Phase.ACTIVE) interval=Math.max(40,interval*2/3);
            s.nextWave=op.ticks()+interval;
        }
        for (int i=0;i<difficulty.maxSpawnsPerTick() && s.spawnsThisTick<difficulty.maxSpawnsPerTick() && s.waveRemaining>0
                && s.actors.size()<difficulty.residentCap() && global<SHARED_ACTOR_LIMIT && s.spawned<difficulty.allowanceAt(op.ticks(),op.plan.durationMinutes());i++) {
            int number=s.spawnSequence++; double angle=number*2.399963229728653;
            double radius=30+(number%4)*4;
            BlockPos raw=s.player.blockPosition().offset((int)(Math.cos(angle)*radius),0,(int)(Math.sin(angle)*radius));
            if (!LibertyWorld.inside(op,raw)) continue;
            BlockPos ground=surface(s,raw); if (ground==null) continue;
            boolean elite=Math.floorMod(number*37,100)<difficulty.elitePercent();
            EntityType<?> type=switch(op.plan.faction()) {
                case AUTOMATON -> elite ? number%2==0?EntityType.IRON_GOLEM:EntityType.RAVAGER : number%3==0?EntityType.PILLAGER:EntityType.SKELETON;
                case TERMINID -> elite ? EntityType.SPIDER : number%3==0?EntityType.CAVE_SPIDER:EntityType.SILVERFISH;
                case ILLUMINATE -> elite ? number%2==0?EntityType.SHULKER:EntityType.WITCH : number%3==0?EntityType.VINDICATOR:EntityType.ZOMBIE;
            };
            Entity rawEntity=LibertyMobSafety.spawnType(type,op.homeDefense).create(s.player.serverLevel()); if (!(rawEntity instanceof Mob mob)) continue;
            mob.moveTo(ground.getX()+.5,ground.getY()+1.1,ground.getZ()+.5,0,0);
            if (!s.player.serverLevel().noCollision(mob)) {mob.discard();continue;}
            LibertyMobSafety.initialize(mob,s.player.serverLevel(),ground);
            mob.setPersistenceRequired(); mob.setCanPickUpLoot(false); for (EquipmentSlot slot:EquipmentSlot.values()) mob.setDropChance(slot,0);
            if (mob.getAttribute(Attributes.MAX_HEALTH)!=null) {
                double nativeHealth=mob.getMaxHealth(); mob.getAttribute(Attributes.MAX_HEALTH).setBaseValue(nativeHealth*(1+.025*(difficulty.level()-1))); mob.setHealth(mob.getMaxHealth());
            }
            mob.setTarget(s.player); mark(mob,s,-1);
            if (!s.player.serverLevel().addFreshEntity(mob)) {s.entities.remove(mob.getUUID());mob.discard();continue;}
            s.actors.put(mob.getUUID(),-1); s.waveRemaining--; s.spawned++; s.spawnsThisTick++; global++;
            MobStreetGear.equip(mob,GangGearSelector.forActor(mob,number),StreetGearAcquisition.GANG_EVENT,false);
            GangMemberState.attach(mob,op.theater.gangId(),"liberty_front",op.id,true,s.player.serverLevel().getGameTime()+50_000);
        }
    }
    private static void spawnPriority(Session s,LibertyMissionRuntime.Site site) {
        var difficulty=s.operation.plan.difficulty();
        if(s.actors.size()>=difficulty.residentCap()||s.spawnsThisTick>=difficulty.maxSpawnsPerTick()
                ||s.spawned>=difficulty.allowanceAt(s.operation.ticks(),s.operation.plan.durationMinutes())
                ||state(s.player.getServer()).sessions.values().stream().mapToInt(v->v.actors.size()).sum()>=SHARED_ACTOR_LIMIT)return;
        BlockPos point=surface(s,site.center().offset(0,0,8)); if(point==null)return;
        EntityType<?> type=switch(s.operation.plan.faction()) {case AUTOMATON->EntityType.RAVAGER;case TERMINID->EntityType.SPIDER;case ILLUMINATE->EntityType.SHULKER;};
        Entity raw=LibertyMobSafety.spawnType(type,s.operation.homeDefense).create(s.player.serverLevel()); if(!(raw instanceof Mob mob))return;
        mob.moveTo(point.getX()+.5,point.getY()+1.1,point.getZ()+.5,0,0);
        if(!s.player.serverLevel().noCollision(mob)){mob.discard();return;}
        LibertyMobSafety.initialize(mob,s.player.serverLevel(),point);
        mob.setPersistenceRequired();mob.setCanPickUpLoot(false);for(EquipmentSlot slot:EquipmentSlot.values())mob.setDropChance(slot,0);
        mob.getAttribute(Attributes.MAX_HEALTH).setBaseValue(160+s.operation.plan.difficulty().level()*8);mob.setHealth(mob.getMaxHealth());
        mob.setCustomName(Component.literal("Priority target • "+site.spec().name()));mob.setCustomNameVisible(true);mob.setGlowingTag(true);mob.setTarget(s.player);
        mark(mob,s,-1);
        if(s.player.serverLevel().addFreshEntity(mob)&&s.operation.mission.assignPriorityTarget(site.id(),mob.getUUID())) {
            s.actors.put(mob.getUUID(),-1);s.spawned++;s.spawnsThisTick++;
        }
        else{s.entities.remove(mob.getUUID());mob.discard();}
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
        if (s.actors.size() >= ACTOR_LIMIT || state(s.player.getServer()).sessions.values().stream()
                .mapToInt(other -> other.actors.size()).sum() >= SHARED_ACTOR_LIMIT) return;
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
        LibertyMobSafety.initialize(mob, level, pos);
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
        boolean owner = killer instanceof ServerPlayer player && player.getUUID().equals(s.player.getUUID()) || ownedSession(killer)==s;
        if (s.operation.mission!=null && owner) {
            if (mob instanceof LibertyCoreEntity) {
                String core=mob.getPersistentData().getCompound(OWNED).getString("Core");
                s.operation.mission.damageCore(core,Float.MAX_VALUE);
                s.player.serverLevel().sendParticles(ParticleTypes.EXPLOSION,mob.getX(),mob.getY()+1,mob.getZ(),8,2,1,2,.01);
            } else if (objective!=null && !(mob instanceof LibertySupportEntity)) s.operation.mission.defeated(mob.getUUID(),mob.position());
        }
        if (objective != null && objective >= 0 && owner) s.operation.objectives.get(objective).defeated(mob.getUUID());
        // Uncredited environmental/foreign deaths are replaced; they never complete an objective.
    }

    public static boolean tagWall(ServerPlayer player, BlockPos support, Direction face) {
        Session s = session(player); if (s == null || !s.operation.field() || support == null) return false;
        if (s.operation.mission!=null) {
            boolean painted=s.operation.mission.paint(support,face,player.position());
            if (painted && s.operation.awardObjectives()) checkpoint(s);
            return painted;
        }
        for (LibertyOperation.Objective objective : s.operation.objectives) {
            if (s.built.contains(objective.index()) && objective.paint(support, face, player.position())) {
                s.status = objective.description(); s.operation.awardObjectives();
                if (checkpoint(s)) send(player, false, ""); return true;
            }
        }
        return false;
    }

    private static void caches(Session s) {
        if (!rightLevel(s) || !s.operation.field() || session(s.player) != s) return;
        s.spawnedCaches.removeIf(index -> !s.operation.cacheClaimed(index) && s.entities.values().stream().noneMatch(entity ->
                entity instanceof ItemEntity && !entity.isRemoved() && entity.getPersistentData().getCompound(OWNED).getInt("Cache") == index));
        for (int i = 0; i < 9; i++) {
            if (s.operation.cacheClaimed(i) || s.spawnedCaches.contains(i)) continue;
            BlockPos pos = s.operation.cachePosition(i);
            if (s.player.distanceToSqr(Vec3.atCenterOf(pos)) > 70 * 70
                    || s.player.serverLevel().getChunkSource().getChunkNow(pos.getX() >> 4, pos.getZ() >> 4) == null) continue;
            if (s.operation.homeDefense) { pos=surface(s,pos); if(pos==null)continue; }
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
        if(s.stratagems!=null) {var result=s.stratagems.call("resupply",s.player.position());s.status=result.message();send(s.player,true,"");return;}
        if (!s.operation.field() || s.operation.resupplies() <= 0 || s.operation.ticks() < s.lastResupply + 600) {
            send(s.player, true, "Resupply needs a charge and a 30-second cooldown. Complete objectives to recover charges."); return;
        }
        if (!LibertyTaczCompat.resupply(s.player)) { send(s.player, true, "Make room for magazine supplies, and check the installed Helldivers pack."); return; }
        s.operation.spendResupply(); s.lastResupply = s.operation.ticks(); s.player.heal(8);
        s.player.getCapability(JetSetDataProvider.CAPABILITY).ifPresent(value -> value.setBoost(100));
        s.status = "Supply signal answered: reserve magazines topped up, health +8 and boost refilled. Reload normally."; send(s.player, false, "");
    }
    private static void guidance(Session s) {
        if (s.operation.mission!=null) for (var site:s.operation.mission.sites()) if (site.active()&&!site.complete()) {
            Vec3 target=site.marker();
            if (s.player.distanceToSqr(target)<120*120) s.player.serverLevel().sendParticles(s.player,
                    site.contested()?ParticleTypes.ANGRY_VILLAGER:ParticleTypes.END_ROD,true,target.x,target.y+3,target.z,5,.3,1.5,.3,.01);
        }
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
        return session(s.player) == s && s.operation.field() && rightLevel(s);
    }
    private static void writeOutcome(Session s, boolean extracted) {
        CompoundTag tag = new CompoundTag(); LibertyOperation op = s.operation;
        tag.putInt("Schema", 1); tag.putUUID("Owner", s.player.getUUID()); tag.putUUID("Run", op.id);
        tag.putString("Gang", op.theater.gang()); tag.putString("Theater", op.theater.name());
        tag.putInt("Completed", op.completed()); tag.putInt("Credits", op.credits());
        tag.putInt("Common", Math.min(1000,op.common()+(op.campaign()?LibertyShipProgress.bonusCommonSamples(s.player,op.mission.completedPrimary(),extracted):0)));
        tag.putInt("Rare", op.rare()); tag.putInt("Super", op.supers()); tag.putBoolean("Extracted", extracted);
        if (op.campaign()) {
            var result=new LibertyCampaignData.Result(op.id,s.player.getUUID(),s.sector,op.plan.faction().id(),op.plan.difficulty().level(),
                    op.mission.completedPrimary(),op.mission.totalPrimary(),(int)op.mission.sites().stream().filter(v->v.optional()&&v.complete()).count(),
                    (int)op.mission.sites().stream().filter(v->v.kind()==LibertyMissionCatalog.ObjectiveKind.DEMOLITION&&v.complete()).count(),
                    extracted,s.defense.isBlank()?null:UUID.fromString(s.defense));
            tag.put("CampaignResult",result.save());
        }
        persisted(s.player).put(OUTCOME_KEY, tag);
    }
    private static boolean validOutcome(ServerPlayer player, CompoundTag tag) {
        if (tag.getInt("Schema") != 1 || !tag.hasUUID("Owner") || !tag.getUUID("Owner").equals(player.getUUID()) || !tag.hasUUID("Run")
                || ResourceLocation.tryParse(tag.getString("Gang")) == null) return false;
        for (String key : List.of("Completed", "Credits", "Common", "Rare", "Super"))
            if (!tag.contains(key, Tag.TAG_INT) || tag.getInt(key) < 0 || tag.getInt(key) > 1000) return false;
        if (tag.contains("CampaignResult") && LibertyCampaignData.Result.load(tag.getCompound("CampaignResult"))
                .filter(result->result.owner().equals(player.getUUID()) && result.runId().equals(tag.getUUID("Run"))
                        && result.extracted()==tag.getBoolean("Extracted")).isEmpty()) return false;
        return tag.getInt("Completed") <= 3 && (!tag.getBoolean("Extracted") || tag.getInt("Completed") == 3);
    }

    public static boolean leave(ServerPlayer player, boolean extracted, String message) {
        if (player == null || player.getServer() == null) return false;
        state(player.getServer()).hubs.remove(player.getUUID());
        Session s = session(player);
        if (s != null && s.exiting) return false;
        if (s != null) {
            s.exiting = true;
            if (s.operation.phase() != LibertyOperation.Phase.RETURNING) {
                extracted = extracted && s.operation.phase() == LibertyOperation.Phase.DEPARTING && s.operation.complete();
                writeOutcome(s, extracted); s.operation.phase(LibertyOperation.Phase.RETURNING);
            }
            player.stopRiding(); if (s.stratagems!=null) s.stratagems.close();
            s.entities.values().forEach(Entity::discard); s.entities.clear(); s.actors.clear();
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
                if (outcome.contains("CampaignResult")) {
                    var result=LibertyCampaignData.Result.load(outcome.getCompound("CampaignResult")).orElseThrow();
                    if (!LibertyCampaignData.get(player.getServer()).settle(result) || !LibertyCampaignData.saveAcknowledged(player.getServer()))
                        throw new IllegalStateException("Campaign reward checkpoint was not acknowledged on disk");
                    if (!LibertyShipProgress.settle(player,result.runId(),result.completedObjectives(),result.difficulty(),result.extracted()))
                        throw new IllegalStateException("Ship account could not acknowledge expedition result");
                }
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
    public static boolean enterShip(ServerPlayer player,int slot) {
        if (player==null || player.getServer()==null || active(player) || hasReturn(player)) return false;
        String problem=unavailable(player); if (!problem.isEmpty()) return fail(player,problem);
        if (slot<0) { slot=LibertyDistricts.get(player.getServer()).allocate();
            if (slot<0 || !LibertyDistricts.saveAcknowledged(player.getServer())) return fail(player,"The ship berth could not be saved."); }
        if (!RushReturnState.capture(player,RETURN_KEY) || !RushPlayerStorage.flush(player)) return fail(player,"Your ship return checkpoint could not be saved.");
        Hub hub=new Hub(player,slot); state(player.getServer()).hubs.put(player.getUUID(),hub);
        try {
            player.getInventory().clearContent(); player.containerMenu.setCarried(ItemStack.EMPTY);
            player.getCapability(JetSetDataProvider.CAPABILITY).ifPresent(v->v.load(new CompoundTag()));
            ItemStack radio=new ItemStack(Items.COMPASS); radio.setHoverName(Component.literal("Liberty ship command"));
            LibertyTaczCompat.markLoan(radio,player.getUUID()); radio.getOrCreateTag().putBoolean("LibertyFieldRadio",true); player.getInventory().setItem(4,radio);
            player.setGameMode(GameType.SURVIVAL); player.removeAllEffects(); player.setHealth(player.getMaxHealth()); player.clearFire();
            player.teleportTo(player.getServer().getLevel(LibertyWorld.DIMENSION),hub.origin.getX()+.5,hub.origin.getY()+1.1,hub.origin.getZ()+14.5,180,0);
            player.setDeltaMovement(Vec3.ZERO); player.fallDistance=0; syncRide(player); player.inventoryMenu.broadcastChanges();
            open(player); send(player,true,"Welcome aboard. Unlock equipment, choose your next loadout and redeploy—or return to Minecraft."); return true;
        } catch(RuntimeException error) { JetSetCraft.LOGGER.error("Liberty ship transfer interrupted",error); leave(player,false,"Ship transfer interrupted; returned safely."); return false; }
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
        if(!active(player)&&!hasReturn(player)) {
            for(var offer:LibertyCampaignData.get(player.getServer()).offers(player.getUUID()))
                if(offer.claimedRun()!=null)releaseDefense(player,offer.claimedRun());
        }
    }
    private static void releaseDefense(ServerPlayer player,UUID run) {
        if(LibertyCampaignData.get(player.getServer()).releaseDefense(player.getUUID(),run))LibertyCampaignData.saveAcknowledged(player.getServer());
    }
    public static void dimensionChanged(ServerPlayer player) {
        Session s = session(player);
        if (s != null && !s.exiting && !rightLevel(s)) leave(player, false, "Returned after leaving the expedition district.");
    }
    public static void movementDiscontinuity(ServerPlayer player) { Session s = session(player); if (s != null) s.previous = null; }
    public static void forget(ServerPlayer player) {
        if (player.getServer() == null) return;
        Session s = state(player.getServer()).sessions.remove(player.getUUID());
        if (s != null) { if(s.stratagems!=null)s.stratagems.close(); s.entities.values().forEach(Entity::discard); }
        state(player.getServer()).hubs.remove(player.getUUID());
        state(player.getServer()).menus.remove(player.getUUID()); state(player.getServer()).actions.remove(player.getUUID());
    }
    public static void stop(MinecraftServer server) {
        for (Session s : List.copyOf(state(server).sessions.values())) leave(s.player, false, "Server stopped. Your original world and earned credits were preserved.");
        for (Hub hub:List.copyOf(state(server).hubs.values())) leave(hub.player,false,"Server stopped. Returned from your ship.");
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
    public static boolean validOwned(Entity entity) { Session s=ownedSession(entity); return s!=null && entity.level().dimension().equals(s.dimension); }
    /** Used only by the pinned home block-contact guard and public Forge side-effect hooks. */
    public static boolean protectedHomeActor(Entity entity) {
        Session s=entity instanceof ServerPlayer player ? session(player) : ownedSession(entity);
        return s!=null && s.operation.homeDefense && !s.exiting && entity.level().dimension().equals(s.dimension);
    }
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
        for (Session s : state(level.getServer()).sessions.values()) if (!s.exiting && !s.operation.homeDefense
                && level.dimension().equals(s.dimension) && LibertyWorld.inside(s.operation, graffiti.blockPosition())) { mark(graffiti, s, -1); return; }
    }
    public static boolean allowedDamage(Entity target, Entity attacker, Entity direct) {
        if (target instanceof ServerPlayer player && inShip(player)) return false;
        Session receiver = target instanceof ServerPlayer player ? session(player) : ownedSession(target);
        Session source = attacker instanceof ServerPlayer player ? session(player) : ownedSession(attacker);
        if (source == null) source = ownedSession(direct);
        if (receiver != null) {
            if (receiver.exiting || !receiver.operation.field()) return false;
            if (target instanceof ServerPlayer && attacker instanceof Player) return false;
            if (target instanceof ServerPlayer && attacker!=null && receiver.allies.containsKey(attacker.getUUID())) return false;
            return attacker == null && direct == null || source == receiver;
        }
        return source == null; // expedition actors/bullets cannot damage anyone outside their mission
    }

    public static CompoundTag snapshot(ServerPlayer player) {
        return snapshot(player,true);
    }
    private static CompoundTag snapshot(ServerPlayer player,boolean catalog) {
        CompoundTag tag = new CompoundTag(); Session s = session(player); tag.putBoolean("Active", s != null);
        tag.putBoolean("InShip",inShip(player)); tag.putBoolean("CanManage",s==null&&(!hasReturn(player)||inShip(player)));
        catalog|=s==null;tag.putBoolean("Partial",!catalog);
        if(catalog) {
        tag.put("Campaign",LibertyCampaignData.get(player.getServer()).snapshot(player));
        tag.put("Ship",LibertyShipProgress.snapshot(player,tag.getBoolean("CanManage")));
        tag.put("Armory",LibertyArmory.snapshot(player,tag.getBoolean("CanManage")));
        ListTag missions=new ListTag();
        for (var template:LibertyMissionCatalog.templates()) {
            CompoundTag row=new CompoundTag(); row.putString("Id",template.id()); row.putString("Name",template.name()); row.putString("Description",template.description());
            ListTag factions=new ListTag(); template.factions().forEach(f->factions.add(net.minecraft.nbt.StringTag.valueOf(f.id()))); row.put("Factions",factions); missions.add(row);
        }
        tag.put("Missions",missions);
        tag.put("Research", LibertyResearch.snapshot(player));
        }
        if (s == null) {
            Menu menu = state(player.getServer()).menus.get(player.getUUID()); tag.putLong("Revision", menu == null ? 0 : menu.revision);
            String blocked = inShip(player)?"":unavailable(player);
            if(blocked.isEmpty()&&!LibertyArmory.ready(player))blocked=LibertyArmory.readyReason(player);
            tag.putString("Status", blocked.isEmpty() ? "Choose your operation, loadout and difficulty. Super credits are earned in play; samples need extraction." : blocked);
            tag.putBoolean("CanDeploy",blocked.isEmpty());
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
        tag.putBoolean("HomeDefense",op.homeDefense); tag.putInt("Reinforcements",s.reinforcements);
        if (op.campaign()) {
            tag.putString("MissionName",op.plan.template().name()); tag.putString("Faction",op.plan.faction().id());
            tag.putInt("Difficulty",op.plan.difficulty().level()); tag.putInt("ArenaRadius",op.theater.radius()); tag.putLong("Seed",op.plan.seed());
            tag.putInt("Enemies",s.actors.size()); tag.putInt("Spawned",s.spawned); tag.putInt("Retired",s.retired); tag.putInt("Stage",op.mission.stage());
            ListTag callins=new ListTag();
            for (var entry:s.stratagems.snapshot()) {
                if(!s.stratagems.loadout().contains(entry.id())&&!entry.category().equals("SHARED")
                        &&!(entry.category().equals("MISSION")&&missionReady(s,entry.id())))continue;
                CompoundTag row=new CompoundTag(); row.putString("Id",entry.id()); row.putString("Name",entry.name()); row.putString("Category",entry.category());
                row.putString("Description",entry.description()); row.putString("Effect",entry.effect()); row.putString("Controls",entry.controls());
                row.putString("Backend",entry.backend()); row.putDouble("Range",entry.range()); row.putLong("Cooldown",entry.cooldownTicks());
                row.putInt("Uses",entry.uses()); row.putInt("Ammunition",entry.ammunition()); row.putBoolean("Ready",entry.ready());
                row.putBoolean("Equipped",s.stratagems.loadout().contains(entry.id())); callins.add(row);
            }
            tag.put("Stratagems",callins);
        }
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
        Session session=session(player);
        CompoundTag snapshot = snapshot(player,open||session==null||session.operation.ticks()==0); snapshot.putBoolean("Open", open);
        if (message != null && !message.isBlank()) snapshot.putString("Status", message);
        JetSetNetwork.syncLiberty(player, snapshot);
    }
    private static boolean fail(ServerPlayer player, String message) { send(player, true, message); return false; }
    private static void syncRide(ServerPlayer player) { player.getCapability(JetSetDataProvider.CAPABILITY).ifPresent(value -> JetSetNetwork.sync(player, value)); }
    private LibertyManager() {}
}
