package com.herberto.jetsetcraft.liberty;

import com.herberto.jetsetcraft.compat.LibertyHelldiversCompat;
import com.herberto.jetsetcraft.compat.LibertyTaczCompat;
import com.herberto.jetsetcraft.data.JetSetDataProvider;
import com.herberto.jetsetcraft.entity.LibertySupportEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.MoverType;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.registries.ForgeRegistries;
import org.joml.Vector3f;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static com.herberto.jetsetcraft.liberty.LibertyStratagemCatalog.*;

/** Server-owned, finite-resource call-ins. World effects are overlays; only Context-approved enemies take damage. */
public final class LibertyStratagemEngine {
    public static final String EQUIPMENT_TAG="jetsetcraft_liberty_equipment";
    private static final Map<UUID,LibertyStratagemEngine> ACTIVE=new HashMap<>();
    private static final int SUPPORT_SLOT=3, BACKPACK_SLOT=7, CONTROLLER_SLOT=6;
    private static final int MAX_HARDWARE=48, MAX_JOBS=256, MAX_PROVIDER_ACTORS=512;
    public interface Context {
        ServerPlayer player(); UUID operation(); boolean active(); String phase();
        boolean validTarget(Vec3 target); boolean unlocked(String id);
        List<LivingEntity> ownedEnemiesWithin(Vec3 center,double radius);
        void track(Entity actor); boolean damageOwned(Entity enemy,float amount);
        int paintArea(Vec3 center,double radius);
        /** True means terrain destruction is forbidden, including in disposable mission arenas. */
        boolean terrainSafe(); boolean supply(); boolean reinforce(); boolean missionAction(String id,Vec3 target);
        /** Read-only admission used by the field panel; execution still revalidates through the callbacks above. */
        default boolean missionReady(String id){return false;}
        default boolean canSupply(){return false;}
        default boolean canReinforce(){return false;}
        /** Equipment supplies use their own finite charges rather than the shared mission supply pool. */
        default boolean personalSupply(){return supply();}
        default boolean nativeProviderSafe(){return false;}
        default double cooldownMultiplier(){return 1;}
        default int bonusEagleCharges(){return 0;}
    }
    public record CallResult(boolean accepted,String message) {}
    public record Row(String id,String name,String category,String description,String effect,String controls,
                      String source,String backend,double range,int cost,long cooldownTicks,int uses,int ammunition,boolean ready) {}
    private record Job(long at,Runnable action) {}
    private record Zone(Effect effect,Vec3 center,Vec3 end,double radius,float damage,long ends) {}
    private record Hardware(Entry entry,Vec3 anchor,long created) {}
    private record Weapon(Effect effect,int ammo,int delay,float damage,double range,int rounds) {}
    private final Context context;
    private final Map<String,Long> cooldowns=new HashMap<>();
    private final Map<String,Integer> used=new HashMap<>(),eagleAmmo=new HashMap<>();
    private final Map<UUID,Entity> actors=new LinkedHashMap<>();
    private final Map<UUID,Hardware> hardware=new HashMap<>();
    private final Map<UUID,Entry> deliveries=new HashMap<>();
    private final Map<BlockPos,net.minecraft.world.level.block.state.BlockState> nativeLightBefore=new HashMap<>();
    private final List<Job> jobs=new ArrayList<>();
    private final List<Zone> zones=new ArrayList<>();
    private List<String> loadout=List.copyOf(starters());
    private boolean closed,called;
    private long ticked=Long.MIN_VALUE,eagleTurnaround,rearmUntil,weaponReady,backpackReady,shieldHit,nativeUntil;
    private Entry weapon,backpack;
    private int weaponAmmo,backpackAmmo;
    private float personalShield=60;

    public LibertyStratagemEngine(Context context) {
        this.context=context;
        if(context==null||context.player()==null||context.operation()==null)throw new IllegalArgumentException("A real owned operation is required");
        LibertyStratagemEngine previous=ACTIVE.put(context.operation(),this);
        if(previous!=null&&previous!=this)previous.close();
        for(String id:loadout){Entry e=find(id);if(e.eagleCharges()>0)eagleAmmo.put(id,e.eagleCharges()+Math.max(0,context.bonusEagleCharges()));}
    }
    public static LibertyStratagemEngine forOperation(UUID operation){return operation==null?null:ACTIVE.get(operation);}
    public static LibertyStratagemEngine forActor(Entity actor){
        if(actor==null)return null;
        for(LibertyStratagemEngine engine:ACTIVE.values())if(engine.actors.get(actor.getUUID())==actor)return engine;
        return null;
    }
    public Context context(){return context;}
    public boolean owns(Entity actor){return actor!=null&&!closed&&context.active()&&actor.level()==level()&&actors.get(actor.getUUID())==actor;}
    public List<String> loadout(){return loadout;}
    public CallResult setLoadout(List<String> ids){
        if(called)return no("The deployed four-slot loadout is fixed for this operation.");
        if(ids==null||ids.size()>4||ids.stream().distinct().count()!=ids.size())return no("Choose up to four distinct stratagems.");
        for(String id:ids){Entry e=find(id);if(e==null||!e.selectable()||!context.unlocked(id))return no("That stratagem is not unlocked selectable equipment.");}
        loadout=List.copyOf(ids);
        for(String id:ids){Entry e=find(id);if(e.eagleCharges()>0)eagleAmmo.putIfAbsent(id,e.eagleCharges()+Math.max(0,context.bonusEagleCharges()));}
        return yes("Operation loadout equipped.");
    }
    public List<Row> snapshot(){
        long now=now();List<Row> rows=new ArrayList<>();
        for(Entry e:all()){
            long left=Math.max(0,cooldowns.getOrDefault(e.id(),0L)-now);
            if(e.category()==Category.EAGLE||e.effect()==Effect.REARM)left=Math.max(left,Math.max(rearmUntil,eagleTurnaround)-now);
            int remaining=e.missionUses()==0?-1:Math.max(0,e.missionUses()-used.getOrDefault(e.id(),0));
            String backend=!e.nativeProviderId().isEmpty()&&context.nativeProviderSafe()&&LibertyHelldiversCompat.supports(e.nativeProviderId())?"MCDivers":!e.taczGunId().isEmpty()&&LibertyHelldiversCompat.supportWeaponAvailable(e.taczGunId())?"TaCZ gun pack":"JetSetCraft edition";
            if(e.availability()==Availability.HISTORICAL)backend="Historical reference; not callable";
            boolean ready=!closed&&context.active()&&left<=0&&remaining!=0&&phaseAllows(e)
                    &&e.availability()!=Availability.HISTORICAL;
            if(e.selectable())ready&=loadout.contains(e.id())&&context.unlocked(e.id());
            if(e.category()==Category.EAGLE)ready&=eagleAmmo.getOrDefault(e.id(),0)>0;
            if(e.category()==Category.MISSION||e.effect()==Effect.SOS)ready&=context.missionReady(e.id());
            if(e.effect()==Effect.RESUPPLY)ready&=context.canSupply();
            if(e.effect()==Effect.REINFORCE)ready&=context.canReinforce();
            if(e.effect()==Effect.REARM)ready&=eagleNeedsRearm();
            rows.add(new Row(e.id(),e.name(),e.category().name(),e.description(),e.effect().name(),e.controls(),e.source(),backend,e.range(),e.unlockCost(),Math.max(0,left),remaining,eagleAmmo.getOrDefault(e.id(),-1),ready));
        }
        return List.copyOf(rows);
    }
    private boolean phaseAllows(Entry entry){
        String phase=context.phase()==null?"":context.phase().toUpperCase(java.util.Locale.ROOT);
        return phase.equals("DOWNED")?entry.effect()==Effect.REINFORCE
                :phase.equals("ACTIVE")||phase.equals("EXTRACTING")||phase.equals("BOARDING");
    }
    private boolean eagleNeedsRearm(){
        return loadout.stream().map(LibertyStratagemCatalog::find).filter(e->e.category()==Category.EAGLE)
                .anyMatch(e->eagleAmmo.getOrDefault(e.id(),0)<e.eagleCharges()+Math.max(0,context.bonusEagleCharges()));
    }
    public CallResult call(String id,Vec3 target){
        if(closed||!context.active())return no("No active operation owns this call.");
        Entry entry=find(id);if(entry==null)return no("Unknown stratagem.");
        if(!phaseAllows(entry))return no("This support call is unavailable in the current deployment phase.");
        if(entry.availability()==Availability.HISTORICAL)return no("This is historical or unavailable equipment, not a permanent call-in.");
        if(entry.selectable()&&(!loadout.contains(id)||!context.unlocked(id)))return no("Equip this unlocked stratagem in one of the four operation slots.");
        if(entry.effect()==Effect.REARM)return rearm();
        long now=now();
        if(cooldowns.getOrDefault(id,0L)>now)return no("Support is cooling down for "+((cooldowns.get(id)-now+19)/20)+" seconds.");
        if(entry.missionUses()>0&&used.getOrDefault(id,0)>=entry.missionUses())return no("No mission uses remain for this stratagem.");
        if(entry.category()==Category.EAGLE&&(now<eagleTurnaround||now<rearmUntil||eagleAmmo.getOrDefault(id,0)<=0))return no("Eagle is turning around, rearming, or needs Eagle Rearm.");
        if(entry.effect()==Effect.REINFORCE){if(!context.reinforce())return no("No reinforcement is available or needed.");consume(entry);return yes("Reinforcement pod inbound.");}
        if(entry.effect()==Effect.RESUPPLY){if(!context.supply())return no("This operation cannot resupply right now.");refillEquipment();consume(entry);return yes("Supplies delivered.");}
        if(entry.effect()==Effect.SOS){if(!context.missionAction(id,context.player().position()))return no("No eligible SOS response is available in this operation.");consume(entry);return yes("SOS support requested.");}
        if(!valid(target))return no("Aim within 128 blocks at loaded terrain inside this operation.");
        if(entry.category()==Category.MISSION){if(!context.missionAction(id,target))return no("The active objective does not authorize this command here.");consume(entry);return yes("Objective command accepted.");}
        if(jobs.size()>MAX_JOBS-40)return no("Too much support is already active; let the current salvo finish.");
        if(entry.category()==Category.SUPPORT_WEAPON||entry.category()==Category.BACKPACK){
            if(!spawnDelivery(entry,target))return no("No safe delivery slot or hardware capacity is available.");
        } else if(!entry.nativeProviderId().isEmpty()&&context.nativeProviderSafe()&&LibertyHelldiversCompat.supports(entry.nativeProviderId())) {
            Entity beacon=LibertyHelldiversCompat.beacon(context.player(),entry.nativeProviderId(),target,look());
            if(beacon==null)return no("The installed MCDivers release could not create this call-in.");
            nativeUntil=Math.max(nativeUntil,now+12000);track(beacon);
            if(!level().addFreshEntity(beacon)){actors.remove(beacon.getUUID());return no("The provider beacon could not enter this operation.");}
        } else if(isHardware(entry.effect())) {
            if(!deploy(entry,target))return no("Hardware capacity reached or the support entity registry is unavailable.");
        } else {
            warning(target,entry.name());Vec3 facing=look();
            schedule(40,()->execute(entry.effect(),target,facing));
        }
        consume(entry);return yes(entry.name()+" inbound.");
    }
    public CallResult rearm(){
        if(closed||!context.active())return no("No active operation.");
        if(!phaseAllows(find("eagle_rearm")))return no("Eagle rearm is unavailable in this deployment phase.");
        if(now()<rearmUntil)return no("Eagle is already rearming.");
        if(loadout.stream().map(LibertyStratagemCatalog::find).noneMatch(e->e.category()==Category.EAGLE))return no("No Eagle stratagem is equipped.");
        if(now()<eagleTurnaround)return no("Wait for the current Eagle sortie to clear.");
        if(loadout.stream().map(LibertyStratagemCatalog::find).filter(e->e.category()==Category.EAGLE).allMatch(e->eagleAmmo.getOrDefault(e.id(),0)>=e.eagleCharges()+Math.max(0,context.bonusEagleCharges())))return no("Eagle ammunition is already full.");
        rearmUntil=now()+cooldown(2400);called=true;return yes("Eagle rearming; all equipped Eagle charges return together.");
    }
    private void consume(Entry entry){
        called=true;cooldowns.put(entry.id(),now()+cooldown(entry.cooldownTicks()));used.merge(entry.id(),1,Integer::sum);
        if(entry.category()==Category.EAGLE){eagleAmmo.compute(entry.id(),(id,n)->Math.max(0,(n==null?0:n)-1));eagleTurnaround=now()+cooldown(160);}
    }
    private int cooldown(int ticks){return Math.max(1,(int)Math.ceil(ticks*Math.max(.5,Math.min(1.5,context.cooldownMultiplier()))));}
    public void tick(){
        if(closed)return;if(!context.active()){close();return;}long time=now();if(ticked==time)return;ticked=time;
        if(rearmUntil>0&&time>=rearmUntil){for(String id:loadout){Entry e=find(id);if(e.category()==Category.EAGLE)eagleAmmo.put(id,e.eagleCharges()+Math.max(0,context.bonusEagleCharges()));}rearmUntil=0;}
        List<Job> due=jobs.stream().filter(j->j.at()<=time).toList();jobs.removeAll(due);for(Job job:due)if(!closed&&context.active())job.action().run();
        zones.removeIf(z->z.ends()<time);
        if(time%5==0)for(Zone zone:List.copyOf(zones))tickZone(zone,time);
        actors.entrySet().removeIf(e->e.getValue().isRemoved());hardware.keySet().removeIf(id->!actors.containsKey(id));deliveries.keySet().removeIf(id->!actors.containsKey(id));
        if(time-shieldHit>100)personalShield=Math.min(60,personalShield+.3f);
        if(backpack!=null)tickBackpack(time);
    }
    public boolean adoptProviderEntity(Entity entity){
        if(closed||!context.active()||!context.nativeProviderSafe()||entity.level()!=level()||!LibertyHelldiversCompat.actor(entity))return false;
        if(actors.get(entity.getUUID())==entity)return true;
        if(now()>nativeUntil||actors.size()>=MAX_PROVIDER_ACTORS||!context.player().getUUID().equals(LibertyHelldiversCompat.ownerId(entity)))return false;
        // Native Eagle flight plans begin outside the arena; its exact owner and an active accepted call establish causality.
        if(entity.distanceToSqr(context.player())>1800.0*1800.0)return false;
        if(entity.getClass().getName().equals("com.shiroaki193.mcdivers.entity.SentryEntity")) {
            for(BlockPos pos:List.of(entity.blockPosition(),entity.blockPosition().above())) {
                var before=level().getBlockState(pos);if(before.isAir())nativeLightBefore.putIfAbsent(pos.immutable(),before);
            }
        }
        track(entity);return true;
    }
    private void track(Entity entity){actors.put(entity.getUUID(),entity);entity.getPersistentData().putUUID("jetsetcraft_stratagem_operation",context.operation());context.track(entity);}
    public void close(){
        if(closed)return;closed=true;ACTIVE.remove(context.operation(),this);jobs.clear();zones.clear();
        for(Entity actor:List.copyOf(actors.values()))if(!actor.isRemoved())actor.discard();actors.clear();hardware.clear();deliveries.clear();
        for(var entry:nativeLightBefore.entrySet()) {
            ResourceLocation block=ForgeRegistries.BLOCKS.getKey(level().getBlockState(entry.getKey()).getBlock());
            if(new ResourceLocation("mcdivers","sentry_light").equals(block))level().setBlock(entry.getKey(),entry.getValue(),3);
        }
        nativeLightBefore.clear();
    }
    private boolean valid(Vec3 pos){return pos!=null&&Double.isFinite(pos.x)&&Double.isFinite(pos.y)&&Double.isFinite(pos.z)&&pos.distanceToSqr(context.player().position())<=128*128&&context.validTarget(pos)&&level().hasChunkAt(BlockPos.containing(pos));}
    private ServerLevel level(){return context.player().serverLevel();}
    private long now(){return level().getGameTime();}
    private Vec3 look(){Vec3 v=context.player().getLookAngle();return new Vec3(v.x,0,v.z).lengthSqr()<1e-5?new Vec3(0,0,1):new Vec3(v.x,0,v.z).normalize();}
    private void schedule(int ticks,Runnable action){if(jobs.size()<MAX_JOBS)jobs.add(new Job(now()+Math.max(1,ticks),action));}
    private List<LivingEntity> enemies(Vec3 center,double range){return context.ownedEnemiesWithin(center,Math.min(160,Math.max(0,range))).stream().filter(e->e!=null&&e.isAlive()&&e.level()==level()).limit(128).toList();}
    private boolean sight(Vec3 origin,LivingEntity target){return level().clip(new ClipContext(origin,target.getEyePosition(),ClipContext.Block.COLLIDER,ClipContext.Fluid.NONE,context.player())).getType()==HitResult.Type.MISS;}
    private LivingEntity hardest(Vec3 center,double range){return enemies(center,range).stream().max(Comparator.comparingDouble(LivingEntity::getMaxHealth)).orElse(null);}
    private void beam(Vec3 from,Vec3 to,int color){Vec3 delta=to.subtract(from);int n=Math.max(2,Math.min(64,(int)(delta.length()*2)));Vector3f rgb=new Vector3f(((color>>16)&255)/255f,((color>>8)&255)/255f,(color&255)/255f);for(int i=0;i<=n;i+=2){Vec3 p=from.add(delta.scale((double)i/n));level().sendParticles(new DustParticleOptions(rgb,1.25f),p.x,p.y,p.z,1,0,0,0,0);}}
    private void warning(Vec3 pos,String name){level().sendParticles(ParticleTypes.END_ROD,pos.x,pos.y+1,pos.z,14,.3,2,.3,0);level().playSound(null,BlockPos.containing(pos),SoundEvents.NOTE_BLOCK_PLING.value(),SoundSource.PLAYERS,1f,.65f);}
    private void blast(Vec3 center,double radius,float damage,double push){
        if(!context.active()||!context.validTarget(center))return;
        level().sendParticles(ParticleTypes.EXPLOSION,center.x,center.y+1,center.z,Math.max(1,Math.min(20,(int)radius)),radius*.2,.3,radius*.2,0);
        level().playSound(null,BlockPos.containing(center),SoundEvents.GENERIC_EXPLODE,SoundSource.PLAYERS,Math.min(3,(float)radius/3),1.1f);
        for(LivingEntity enemy:enemies(center,radius)){double distance=enemy.position().distanceTo(center);if(distance>radius)continue;
            if(damage>0)context.damageOwned(enemy,damage*(float)Math.max(.2,1-distance/(radius*1.4)));
            if(push>0){Vec3 v=enemy.position().subtract(center);if(v.lengthSqr()<.01)v=new Vec3(1,0,0);v=v.normalize().scale(push);enemy.push(v.x,.25,v.z);enemy.hurtMarked=true;}
        }
    }
    private void zone(Effect effect,Vec3 center,double radius,float damage,int duration){if(zones.size()<64)zones.add(new Zone(effect,center,center,radius,damage,now()+duration));}
    private void tickZone(Zone z,long time){
        if(!context.validTarget(z.center()))return;
        if(z.effect()==Effect.FRESH_TRACKS){
            beam(z.center().add(0,.15,0),z.end().add(0,.15,0),0x21ffbb);
            Vec3 line=z.end().subtract(z.center());double t=line.lengthSqr()<.1?0:Math.max(0,Math.min(1,context.player().position().subtract(z.center()).dot(line)/line.lengthSqr()));
            if(context.player().position().distanceToSqr(z.center().add(line.scale(t)))<16){
                context.player().getCapability(JetSetDataProvider.CAPABILITY).ifPresent(data->{data.setBoost(Math.min(100,data.boost()+4));data.setMomentum(Math.max(data.momentum(),.7));});
                context.player().push(line.normalize().x*.08,0,line.normalize().z*.08);context.player().hurtMarked=true;
            }return;
        }
        boolean gas=z.effect()==Effect.GAS||z.effect()==Effect.GAS_LINE||z.effect()==Effect.SENTRY_GAS||z.effect()==Effect.MINE_GAS||z.effect()==Effect.DOG_GAS;
        boolean smoke=z.effect()==Effect.SMOKE||z.effect()==Effect.SMOKE_LINE||z.effect()==Effect.INKSTORM;
        boolean stun=z.effect()==Effect.EMS||z.effect()==Effect.SENTRY_EMS||z.effect()==Effect.STUN_GRENADE;
        level().sendParticles(gas?ParticleTypes.SPORE_BLOSSOM_AIR:smoke?ParticleTypes.CAMPFIRE_COSY_SMOKE:stun?ParticleTypes.ELECTRIC_SPARK:ParticleTypes.FLAME,z.center().x,z.center().y+1,z.center().z,8,z.radius()*.65,.6,z.radius()*.65,.01);
        for(LivingEntity enemy:enemies(z.center(),z.radius())){
            if(smoke||gas){if(enemy instanceof Mob mob&&mob.getTarget()==context.player())mob.setTarget(null);enemy.addEffect(new MobEffectInstance(MobEffects.BLINDNESS,35,0));}
            if(stun||z.effect()==Effect.INKSTORM){enemy.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN,35,stun?5:2));enemy.setDeltaMovement(enemy.getDeltaMovement().multiply(.15,1,.15));}
            if(time%20==0&&z.damage()>0)context.damageOwned(enemy,z.damage());
        }
    }
    private void execute(Effect effect,Vec3 point,Vec3 facing){
        if(closed||!context.active()||!context.validTarget(point))return;
        Vec3 side=new Vec3(-facing.z,0,facing.x);
        switch(effect){
            case PRECISION -> blast(point,6,85,1.1);
            case HEAVY_BOMB -> {warning(point,"Heavy bomb");schedule(35,()->blast(point,14,145,1.5));}
            case RAILCANNON -> {LivingEntity target=hardest(point,25);if(target!=null){beam(point.add(0,45,0),target.getBoundingBox().getCenter(),0xff9944);context.damageOwned(target,260);}}
            case ROCKET_PODS -> {for(int i=0;i<3;i++)schedule(8+i*12,()->{LivingEntity target=hardest(point,22);if(target!=null){beam(point.add(0,35,0),target.position(),0xff7722);blast(target.position(),4,60,.4);}});}
            case GATLING_BARRAGE -> {for(int i=0;i<18;i++){Vec3 p=point.add(side.scale(((i%6)-2.5)*2)).add(facing.scale((i/6-1)*3));schedule(i*4+1,()->{beam(p.add(0,30,0),p,0xffff77);blast(p,3,12,.1);});}}
            case BARRAGE_120,BARRAGE_380,NAPALM_BARRAGE -> {int shots=effect==Effect.BARRAGE_380?12:15;double spread=effect==Effect.BARRAGE_380?22:effect==Effect.NAPALM_BARRAGE?19:13;for(int i=0;i<shots;i++){double angle=i*2.399963;Vec3 p=point.add(Math.cos(angle)*spread*Math.sqrt((i+.5)/shots),0,Math.sin(angle)*spread*Math.sqrt((i+.5)/shots));schedule(i*(effect==Effect.BARRAGE_380?18:12)+1,()->{blast(p,effect==Effect.BARRAGE_380?9:6,effect==Effect.BARRAGE_380?95:50,.6);if(effect==Effect.NAPALM_BARRAGE)zone(effect,p,6,6,220);});}}
            case WALKING_BARRAGE -> {for(int i=0;i<6;i++)for(int j=-1;j<=1;j++){Vec3 p=point.add(facing.scale(i*8)).add(side.scale(j*5));schedule(i*20+1,()->blast(p,7,65,.5));}}
            case AIRBURST -> {for(int i=0;i<3;i++){int n=i;schedule(i*18+1,()->{level().sendParticles(ParticleTypes.FIREWORK,point.x,point.y+6,point.z,60,5,1,5,.1);blast(point,8+n*3,28,.3);});}}
            case ORBITAL_LASER -> {for(int i=0;i<40;i++)schedule(i*10+1,()->{LivingEntity target=hardest(point,30);if(target!=null){beam(target.position().add(0,40,0),target.position(),0xffdd33);context.damageOwned(target,14);}});}
            case STRAFING -> {for(int i=0;i<15;i++){Vec3 p=point.add(facing.scale(i*2-10));schedule(i*2+1,()->{beam(p.add(0,18,0),p,0xffff77);blast(p,2.8,18,.1);});}}
            case AIRSTRIKE,CLUSTER,NAPALM_LINE,GAS_LINE,SMOKE_LINE -> {int n=effect==Effect.CLUSTER?15:7;for(int i=0;i<n;i++){Vec3 p=point.add(side.scale((i-(n-1)/2.0)*(effect==Effect.CLUSTER?2.3:3.5))).add(facing.scale(effect==Effect.CLUSTER?(i%3-1)*5:0));schedule(i*4+1,()->{if(effect==Effect.GAS_LINE||effect==Effect.SMOKE_LINE)zone(effect,p,5,effect==Effect.GAS_LINE?4:0,240);else{blast(p,effect==Effect.CLUSTER?4:6,effect==Effect.CLUSTER?24:48,.4);if(effect==Effect.NAPALM_LINE)zone(effect,p,5,6,240);}});}}
            case GAS,SMOKE,EMS -> zone(effect,point,effect==Effect.SMOKE?12:10,effect==Effect.GAS?5:0,300);
            case CHROMAGEDDON -> {warning(point,"CHROMAGEDDON");for(int ring=0;ring<8;ring++){int r=ring;schedule(30+ring*8,()->{double radius=6+r*6;context.paintArea(point,radius);level().sendParticles(ParticleTypes.FIREWORK,point.x,point.y+2,point.z,100,radius*.5,3,radius*.5,.15);for(LivingEntity enemy:enemies(point,radius))if(enemy.distanceToSqr(point)>Math.pow(Math.max(0,radius-7),2))context.damageOwned(enemy,140);});}}
            case BASSQUAKE -> {for(int i=0;i<5;i++){int beat=i;schedule(i*16+1,()->{level().playSound(null,BlockPos.containing(point),SoundEvents.NOTE_BLOCK_BASEDRUM.value(),SoundSource.PLAYERS,3,.5f+beat*.1f);blast(point,10+beat*2,14,1.3);for(LivingEntity enemy:enemies(point,18))enemy.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN,30,3));});}}
            case INKSTORM -> {for(int i=-3;i<=3;i++){Vec3 p=point.add(side.scale(i*5));zone(effect,p,5,1,360);context.paintArea(p,5);}}
            case FRESH_TRACKS -> {Vec3 end=point.add(facing.scale(35));if(!context.validTarget(end))end=point.add(facing.scale(10));zones.add(new Zone(effect,point,end,4,0,now()+400));context.paintArea(point,5);context.paintArea(end,5);}
            default -> { /* Equipment/hardware/conditional actions have their own concrete dispatch paths. */ }
        }
    }
    private boolean isHardware(Effect e){return e.name().startsWith("SENTRY_")||e.name().startsWith("MINE_")||e.name().startsWith("EMPLACEMENT_")||switch(e){case TESLA,SHIELD_RELAY,PATRIOT,EMANCIPATOR,LUMBERER,BREAKTHROUGH,GUNNER_FRV,SUPPLY_FRV,MAELSTROM,BASTION,TAG_TEAM,GRAFFITI_GHOST->true;default->false;};}
    private boolean deploy(Entry e,Vec3 pos){
        int count=e.effect().name().startsWith("MINE_")?(e.effect()==Effect.MINE_AT?8:12):1;
        if(hardware.size()+count>MAX_HARDWARE)return false;
        List<LibertySupportEntity> staged=new ArrayList<>();
        for(int i=0;i<count;i++){
            Vec3 p=count==1?pos:pos.add(Math.cos(i*2.399963)*Math.sqrt(i+.5)*2.2,0,Math.sin(i*2.399963)*Math.sqrt(i+.5)*2.2);
            p=ground(p);if(!context.validTarget(p))return false;
            LibertySupportEntity actor=create(e,e.effect()==Effect.TAG_TEAM?"DOG_PAINT":e.effect().name(),p);if(actor==null)return false;staged.add(actor);
        }
        for(LibertySupportEntity actor:staged){track(actor);hardware.put(actor.getUUID(),new Hardware(e,actor.position(),now()));level().addFreshEntity(actor);}return true;
    }
    private boolean spawnDelivery(Entry entry,Vec3 position){
        if(hardware.size()>=MAX_HARDWARE)return false;
        LibertySupportEntity pod=create(entry,"SUPPLY",ground(position));if(pod==null)return false;
        track(pod);hardware.put(pod.getUUID(),new Hardware(entry,pod.position(),now()));deliveries.put(pod.getUUID(),entry);level().addFreshEntity(pod);warning(position,entry.name());return true;
    }
    @SuppressWarnings("unchecked") private LibertySupportEntity create(Entry entry,String role,Vec3 position){
        EntityType<?> type=ForgeRegistries.ENTITY_TYPES.getValue(new ResourceLocation("jetsetcraft","liberty_support"));
        if(type==null)return null;Entity raw=type.create(level());if(!(raw instanceof LibertySupportEntity entity))return null;
        int ammo=role.startsWith("MINE_")?1:role.equals("SUPPLY_FRV")?4:role.contains("GATLING")||role.equals("MAELSTROM")?600:role.contains("LASER")?1000:role.contains("MORTAR")||role.contains("ROCKET")||role.contains("AUTOCANNON")?40:180;
        entity.configure(role,context.player().getUUID(),context.operation(),ammo,entry.category()==Category.VEHICLE?260:role.equals("GRAFFITI_GHOST")?100:70,entry.category()==Category.VEHICLE?6000:2400);
        entity.setPos(position);entity.setYRot(context.player().getYRot());return entity;
    }
    private Vec3 ground(Vec3 pos){
        BlockPos p=BlockPos.containing(pos);for(int y=3;y>=-5;y--){BlockPos b=p.offset(0,y,0);if(level().getBlockState(b).isSolidRender(level(),b)&&level().getBlockState(b.above()).getCollisionShape(level(),b.above()).isEmpty())return new Vec3(pos.x,b.getY()+1.01,pos.z);}return pos;
    }
    public CallResult collectDelivery(LibertySupportEntity pod){
        Entry entry=deliveries.get(pod.getUUID());if(entry==null||!owns(pod)||pod.tickCount<20||pod.distanceToSqr(context.player())>25)return no("Approach your landed delivery to collect it.");
        CallResult result=equip(entry);if(result.accepted()){deliveries.remove(pod.getUUID());pod.discard();}return result;
    }
    private CallResult equip(Entry entry){
        boolean pack=entry.category()==Category.BACKPACK;int slot=pack?BACKPACK_SLOT:SUPPORT_SLOT;
        ItemStack existing=context.player().getInventory().getItem(slot);
        if(!existing.isEmpty()&&!LibertyTaczCompat.isLoan(existing,context.player().getUUID()))return no("Clear the support equipment slot before collecting this delivery.");
        if(!pack&&!entry.taczGunId().isEmpty()){
            var nativeGear=LibertyHelldiversCompat.supportWeapon(entry.taczGunId(),context.player().getUUID());
            if(nativeGear.ready()){
                List<Integer> free=new ArrayList<>();for(int i=9;i<36;i++)if(context.player().getInventory().getItem(i).isEmpty())free.add(i);
                if(free.size()<nativeGear.reserve().size())return no("Make room for the support magazines before collecting.");
                context.player().getInventory().setItem(slot,nativeGear.gun());for(int i=0;i<nativeGear.reserve().size();i++)context.player().getInventory().setItem(free.get(i),nativeGear.reserve().get(i));
                weapon=entry;weaponAmmo=0;syncInventory();return yes(entry.name()+" ready. Use your TaCZ fire/reload controls.");
            }
        }
        ItemStack tool=new ItemStack(pack?Items.LEATHER:entry.effect()==Effect.FLAG?Items.WHITE_BANNER:entry.effect()==Effect.HAMMER?Items.IRON_AXE:entry.effect()==Effect.SAW?Items.SHEARS:Items.CROSSBOW);
        tool.setHoverName(Component.literal(entry.name()+" • Liberty support"));tool.getOrCreateTag().putString(EQUIPMENT_TAG,entry.id());LibertyTaczCompat.markLoan(tool,context.player().getUUID());
        context.player().getInventory().setItem(slot,tool);
        if(pack){backpack=entry;backpackAmmo=entry.effect()==Effect.SUPPLY_PACK?4:entry.effect()==Effect.HELLBOMB_PACK?1:100;personalShield=60;
            actors.values().stream().filter(a->a instanceof LibertySupportEntity support&&support.drone()
                    &&hardware.containsKey(a.getUUID())&&hardware.get(a.getUUID()).entry().category()==Category.BACKPACK).toList().forEach(Entity::discard);
            if(entry.effect().name().startsWith("DOG_")){LibertySupportEntity drone=create(entry,entry.effect().name(),context.player().position().add(0,2,0));if(drone!=null){track(drone);hardware.put(drone.getUUID(),new Hardware(entry,drone.position(),now()));level().addFreshEntity(drone);}}
        }else{weapon=entry;weaponAmmo=weapon(entry.effect()).ammo();}
        syncInventory();return yes(entry.name()+" equipped.");
    }
    private void syncInventory(){context.player().getInventory().setChanged();context.player().inventoryMenu.broadcastChanges();}
    public void mounted(LibertySupportEntity entity){
        ItemStack current=context.player().getInventory().getItem(CONTROLLER_SLOT);if(!current.isEmpty()&&!LibertyTaczCompat.isLoan(current,context.player().getUUID()))return;
        ItemStack remote=new ItemStack(Items.CARROT_ON_A_STICK);remote.setHoverName(Component.literal("Liberty vehicle controls"));remote.getOrCreateTag().putString(EQUIPMENT_TAG,"vehicle_control");LibertyTaczCompat.markLoan(remote,context.player().getUUID());context.player().getInventory().setItem(CONTROLLER_SLOT,remote);syncInventory();
    }
    public CallResult useEquipment(InteractionHand hand,boolean alternate){
        if(closed||!context.active()||"DOWNED".equalsIgnoreCase(context.phase()))return no("Equipment requires an active deployed skater.");
        if(context.player().getVehicle() instanceof LibertySupportEntity support&&owns(support))return vehicleFire(support,alternate);
        ItemStack held=context.player().getItemInHand(hand);String heldId=held.hasTag()?held.getTag().getString(EQUIPMENT_TAG):"";
        if(backpack!=null&&(heldId.equals(backpack.id())||(alternate&&heldId.isEmpty())))return activateBackpack();
        if(weapon==null)return no("Collect a support weapon first.");
        ItemStack equipped=context.player().getInventory().getItem(SUPPORT_SLOT);
        if(equipped.hasTag()&&equipped.getTag().contains("jetsetcraft_liberty_native_support"))return no("Use your TaCZ fire and reload controls.");
        if(!LibertyTaczCompat.isLoan(equipped,context.player().getUUID())||!equipped.hasTag()||!weapon.id().equals(equipped.getTag().getString(EQUIPMENT_TAG)))return no("Equip your delivered support weapon first.");
        if(now()<weaponReady)return no("Support weapon is cycling or cooling.");
        Weapon spec=weapon(weapon.effect());
        if(weapon.effect()==Effect.C4&&alternate){List<Entity> charges=actors.values().stream().filter(a->a instanceof LibertySupportEntity support&&support.role().equals("C4")).toList();if(charges.isEmpty())return no("Plant a C4 charge first.");for(Entity charge:charges){blast(charge.position(),8,95,.8);charge.discard();}weaponReady=now()+20;return yes("Owned C4 charges detonated.");}
        if(weaponAmmo<=0&&spec.ammo()>0)return no("Support ammunition is empty; resupply or call another delivery.");
        int rounds=Math.min(Math.max(1,spec.rounds()),spec.ammo()>0?weaponAmmo:spec.rounds());
        if(weapon.effect()==Effect.C4){long planted=actors.values().stream().filter(a->a instanceof LibertySupportEntity support&&support.role().equals("C4")&&!a.isRemoved()).count();if(planted>=4||hardware.size()>=MAX_HARDWARE)return no("Detonate planted charges or clear active hardware first.");Vec3 p=aimPoint(20);if(!valid(p))return no("No permitted surface for C4.");LibertySupportEntity charge=create(weapon,"C4",ground(p));if(charge==null)return no("Support registry unavailable.");track(charge);hardware.put(charge.getUUID(),new Hardware(weapon,charge.position(),now()));level().addFreshEntity(charge);}
        else if(weapon.effect()==Effect.FLAG&&alternate){if(hardware.size()>=MAX_HARDWARE)return no("Clear active hardware before planting another flag.");Vec3 p=ground(context.player().position().add(look().scale(2)));if(!valid(p))return no("The rally flag must stay inside this operation.");LibertySupportEntity flag=create(weapon,"RALLY_FLAG",p);if(flag==null)return no("Support registry unavailable.");track(flag);hardware.put(flag.getUUID(),new Hardware(weapon,p,now()));level().addFreshEntity(flag);}
        else fire(spec,context.player().getEyePosition(),context.player().getLookAngle(),alternate,rounds);
        if(spec.ammo()>0)weaponAmmo=Math.max(0,weaponAmmo-rounds);weaponReady=now()+spec.delay();return yes(weapon.name()+" fired; "+(spec.ammo()>0?weaponAmmo+" rounds remain.":"cooling."));
    }
    private Vec3 aimPoint(double range){Vec3 start=context.player().getEyePosition(),end=start.add(context.player().getLookAngle().scale(range));return level().clip(new ClipContext(start,end,ClipContext.Block.COLLIDER,ClipContext.Fluid.NONE,context.player())).getLocation();}
    private Weapon weapon(Effect effect){return switch(effect){
        case MELTA -> new Weapon(effect,30,25,55,9,1);case AUTOCANNON->new Weapon(effect,40,12,28,70,1);case APEX->new Weapon(effect,12,45,125,110,1);case SNIPER->new Weapon(effect,30,16,42,110,1);
        case ARC->new Weapon(effect,0,25,24,28,1);case HEAVY_FLAME->new Weapon(effect,280,4,5,15,4);case C4->new Weapon(effect,4,12,95,20,1);case FLAG->new Weapon(effect,0,13,20,5,1);case HAMMER->new Weapon(effect,0,28,45,4,1);case SAW->new Weapon(effect,200,3,8,3.5,2);
        case DISPOSABLE_AT->new Weapon(effect,2,25,150,85,1);case LEVELER->new Weapon(effect,1,35,180,80,1);case NAPALM_ROCKET->new Weapon(effect,1,25,40,65,1);case SPEAR->new Weapon(effect,4,50,170,110,1);case FLAME->new Weapon(effect,120,5,5,10,3);
        case GRENADE->new Weapon(effect,24,12,30,45,1);case BELT_GRENADE->new Weapon(effect,80,9,25,45,1);case STUN_GRENADE->new Weapon(effect,30,15,3,45,1);case RECOILLESS->new Weapon(effect,7,40,135,90,1);
        case NOVA->new Weapon(effect,0,30,48,100,1);case LASER->new Weapon(effect,0,6,9,70,1);case QUASAR->new Weapon(effect,0,180,150,100,1);case MAXIGUN->new Weapon(effect,750,4,5,65,10);case STALWART->new Weapon(effect,300,5,5,60,4);case HMG->new Weapon(effect,120,6,16,75,3);case MACHINE_GUN->new Weapon(effect,220,6,9,65,3);case BULLET_STORM->new Weapon(effect,300,3,4,55,15);
        case COMMANDO->new Weapon(effect,4,18,95,90,1);case SILO->new Weapon(effect,8,45,100,110,1);case PLASMA->new Weapon(effect,24,24,55,70,1);case AIRBURST_ROCKET->new Weapon(effect,6,40,35,80,1);case RAILGUN->new Weapon(effect,25,25,65,105,1);case SPEARGUN->new Weapon(effect,20,18,42,65,1);case WASP->new Weapon(effect,28,30,18,90,4);case STERILIZER->new Weapon(effect,140,5,2,12,3);
        default->new Weapon(effect,100,10,8,45,1);
    };}
    private void fire(Weapon spec,Vec3 origin,Vec3 direction,boolean alternate,int rounds){
        Effect e=spec.effect();Vec3 dir=direction.normalize();Vec3 end=origin.add(dir.scale(spec.range()));HitResult wall=level().clip(new ClipContext(origin,end,ClipContext.Block.COLLIDER,ClipContext.Fluid.NONE,context.player()));end=wall.getLocation();double reach=origin.distanceTo(end);
        List<LivingEntity> targets=enemies(origin,spec.range()).stream().filter(t->{Vec3 d=t.getBoundingBox().getCenter().subtract(origin);double along=d.dot(dir);return along>=0&&along<=reach+1&&d.subtract(dir.scale(along)).length()<Math.max(.65,t.getBbWidth()*.7);}).sorted(Comparator.comparingDouble(t->t.distanceToSqr(origin))).toList();
        LivingEntity target=targets.isEmpty()?null:targets.get(0);Vec3 hit=target==null?end:target.getBoundingBox().getCenter();
        if(e==Effect.SPEAR||e==Effect.WASP||e==Effect.SILO){LivingEntity selected=hardest(hit,18);if(selected!=null&&sight(origin,selected)){target=selected;hit=selected.getBoundingBox().getCenter();}}
        final Vec3 impact=hit;
        switch(e){
            case FLAME,HEAVY_FLAME,MELTA,STERILIZER -> {for(LivingEntity enemy:enemies(origin,spec.range())){Vec3 offset=enemy.getBoundingBox().getCenter().subtract(origin);if(offset.lengthSqr()>.1&&offset.normalize().dot(dir)>.78&&sight(origin,enemy)){context.damageOwned(enemy,spec.damage()*rounds);if(e==Effect.STERILIZER){enemy.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN,50,1));if(enemy instanceof Mob mob)mob.setTarget(null);}}}beam(origin,origin.add(dir.scale(spec.range())),e==Effect.STERILIZER?0x77dd33:0xff6611);}
            case ARC -> {if(target!=null){List<LivingEntity> chain=new ArrayList<>();chain.add(target);for(LivingEntity next:enemies(hit,8))if(!chain.contains(next)&&chain.size()<4)chain.add(next);Vec3 from=origin;for(LivingEntity enemy:chain){beam(from,enemy.getBoundingBox().getCenter(),0x99ccff);context.damageOwned(enemy,spec.damage());enemy.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN,20,3));from=enemy.getBoundingBox().getCenter();}}}
            case GRENADE,BELT_GRENADE,AUTOCANNON,DISPOSABLE_AT,LEVELER,RECOILLESS,COMMANDO,SPEAR,QUASAR,PLASMA,AIRBURST_ROCKET,NAPALM_ROCKET,WASP,SILO,STUN_GRENADE -> {
                beam(origin,impact,e==Effect.QUASAR||e==Effect.PLASMA?0x66ccff:0xffaa44);
                int delay=e==Effect.SILO?35:e==Effect.GRENADE||e==Effect.BELT_GRENADE?12:e==Effect.SPEAR?15:3;
                schedule(delay,()->{if(e==Effect.STUN_GRENADE){zone(e,impact,6,0,100);return;}double r=e==Effect.LEVELER?14:e==Effect.AIRBURST_ROCKET?12:e==Effect.NAPALM_ROCKET?8:e==Effect.PLASMA?5:4;blast(impact,r,spec.damage()*(e==Effect.WASP?rounds:1),.4);if(e==Effect.NAPALM_ROCKET)zone(Effect.NAPALM_LINE,impact,10,8,260);});}
            case HAMMER -> {for(LivingEntity enemy:enemies(origin,4))if(enemy.position().subtract(origin).normalize().dot(dir)>.15){context.damageOwned(enemy,spec.damage());enemy.push(dir.x*.8,.3,dir.z*.8);enemy.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN,35,3));}level().sendParticles(ParticleTypes.CRIT,origin.x,origin.y,origin.z,15,1,.2,1,.1);}
            default -> {beam(origin,hit,e==Effect.NOVA||e==Effect.LASER?0xffdd44:e==Effect.RAILGUN?0x66eeff:0xffeeaa);int pierce=e==Effect.APEX?3:e==Effect.RAILGUN?(alternate?4:2):1;for(int i=0;i<Math.min(pierce,targets.size());i++){LivingEntity enemy=targets.get(i);context.damageOwned(enemy,spec.damage()*rounds*(e==Effect.RAILGUN&&alternate?1.4f:1));if(e==Effect.SPEARGUN){Vec3 pull=origin.subtract(enemy.position()).normalize().scale(.65);enemy.push(pull.x,.12,pull.z);}}}
        }
    }
    public void tickSupport(LibertySupportEntity actor){
        if(!owns(actor))return;Hardware state=hardware.get(actor.getUUID());if(state==null){actor.discard();return;}
        actor.setFiring(false);String role=actor.role();long time=now();
        if(role.equals("SUPPLY")){if(actor.tickCount%20==0)level().sendParticles(ParticleTypes.END_ROD,actor.getX(),actor.getY()+1.3,actor.getZ(),3,.25,.3,.25,0);return;}
        if(actor.vehicle()){
            if(actor.getControllingPassenger()==context.player()){
                ServerPlayer rider=context.player();Vec3 forward=new Vec3(-Math.sin(Math.toRadians(rider.getYRot())),0,Math.cos(Math.toRadians(rider.getYRot())));
                double speed=role.endsWith("FRV")?.52:role.equals("BASTION")||role.equals("MAELSTROM")?.2:.28;
                Vec3 move=forward.scale(rider.zza*speed).add(new Vec3(forward.z,0,-forward.x).scale(rider.xxa*speed*.55));
                Vec3 next=actor.position().add(move);if(context.validTarget(next)&&level().hasChunkAt(BlockPos.containing(next))){actor.setYRot(rider.getYRot());actor.move(MoverType.SELF,new Vec3(move.x,actor.onGround()?0:-.15,move.z));actor.setDeltaMovement(Vec3.ZERO);}
                rider.fallDistance=0;
            }else if(!actor.onGround())actor.move(MoverType.SELF,new Vec3(0,-.15,0));
            return;
        }
        if(role.startsWith("EMPLACEMENT_"))return;
        if(role.startsWith("DOG_")){
            double a=time*.07;Vec3 desired=context.player().position().add(Math.cos(a)*1.7,2.1,Math.sin(a)*1.7);
            actor.setPos(actor.position().lerp(desired,.3));
            if(actor.distanceToSqr(context.player())>400)actor.setPos(desired);
        }
        if(role.equals("RALLY_FLAG")){
            if(time%40==0&&context.player().distanceToSqr(actor)<64){context.player().heal(1);context.player().getCapability(JetSetDataProvider.CAPABILITY).ifPresent(d->d.setBoost(Math.min(100,d.boost()+5)));}
            return;
        }
        if(role.equals("GRAFFITI_GHOST")){
            if(time%10==0)for(LivingEntity enemy:enemies(actor.position(),24))if(enemy instanceof Mob mob)mob.setTarget(actor);
            if(time%40==0){context.paintArea(actor.position(),3);level().sendParticles(ParticleTypes.NOTE,actor.getX(),actor.getY()+2,actor.getZ(),3,.4,.3,.4,0);}return;
        }
        if(role.equals("SHIELD_RELAY")){
            actor.setEnergy(Math.min(100,actor.energy()+.05f));
            if(time%10==0)for(int i=0;i<16;i++){double a=i*Math.PI/8;Vec3 p=actor.position().add(Math.cos(a)*8,1,Math.sin(a)*8);level().sendParticles(ParticleTypes.END_ROD,p.x,p.y,p.z,1,0,0,0,0);}return;
        }
        if(role.equals("C4"))return;
        if(role.startsWith("MINE_")){
            if(actor.tickCount<30||time%5!=0)return;
            LivingEntity trigger=enemies(actor.position(),role.equals("MINE_AT")?2.5:1.6).stream().filter(e->!role.equals("MINE_AT")||e.getMaxHealth()>=45||e.getBbWidth()>1.2).findFirst().orElse(null);
            if(trigger!=null){Vec3 center=actor.position();if(role.equals("MINE_GAS"))zone(Effect.MINE_GAS,center,6,4,180);else{blast(center,role.equals("MINE_AT")?6:4,role.equals("MINE_AT")?120:40,.4);if(role.equals("MINE_FIRE"))zone(Effect.NAPALM_LINE,center,5,7,180);}actor.discard();}return;
        }
        if(actor.ammo()<=0)return;
        int interval=switch(role){case "SENTRY_GATLING"->4;case "SENTRY_LASER","DOG_LASER"->6;case "SENTRY_FLAME","DOG_FLAME","DOG_GAS"->8;case "SENTRY_MORTAR","SENTRY_EMS","SENTRY_GAS"->45;case "SENTRY_ROCKET"->35;case "SENTRY_AUTOCANNON"->22;case "TESLA","DOG_ARC"->25;case "DOG_PAINT"->18;default->12;};
        if(time%interval!=0)return;
        double range=role.equals("TESLA")?12:role.endsWith("FLAME")?11:role.startsWith("DOG_")?24:role.contains("MORTAR")||role.equals("SENTRY_EMS")||role.equals("SENTRY_GAS")?60:48;
        List<LivingEntity> eligible=enemies(actor.position(),range).stream().filter(e->role.contains("MORTAR")||role.equals("SENTRY_EMS")||role.equals("SENTRY_GAS")||sight(actor.getEyePosition(),e)).toList();
        LivingEntity enemy=(role.contains("ROCKET")||role.contains("AUTOCANNON")?eligible.stream().max(Comparator.comparingDouble(LivingEntity::getMaxHealth)):eligible.stream().min(Comparator.comparingDouble(e->e.distanceToSqr(actor)))).orElse(null);
        if(enemy==null)return;actor.setFiring(true);actor.setAmmo(actor.ammo()-1);Vec3 target=enemy.getBoundingBox().getCenter();actor.setYRot((float)Math.toDegrees(Math.atan2(-(target.x-actor.getX()),target.z-actor.getZ())));
        switch(role){
            case "SENTRY_MORTAR","SENTRY_EMS","SENTRY_GAS" -> {Vec3 landing=enemy.position();beam(actor.getEyePosition(),landing.add(0,6,0),0xffdd88);schedule(25,()->{if(role.equals("SENTRY_EMS"))zone(Effect.SENTRY_EMS,landing,7,0,100);else if(role.equals("SENTRY_GAS"))zone(Effect.SENTRY_GAS,landing,7,4,160);else blast(landing,6,40,.4);});}
            case "SENTRY_ROCKET","SENTRY_AUTOCANNON" -> {beam(actor.getEyePosition(),target,0xffbb55);blast(target,role.equals("SENTRY_ROCKET")?5:3.5,role.equals("SENTRY_ROCKET")?70:35,.2);}
            case "TESLA","DOG_ARC" -> {Vec3 from=actor.getEyePosition();for(LivingEntity chained:enemies(target,7).stream().limit(role.equals("TESLA")?5:3).toList()){beam(from,chained.getBoundingBox().getCenter(),0x88ccff);context.damageOwned(chained,role.equals("TESLA")?28:12);chained.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN,30,2));from=chained.getBoundingBox().getCenter();}}
            case "SENTRY_FLAME","DOG_FLAME" -> {beam(actor.getEyePosition(),target,0xff6622);for(LivingEntity hit:enemies(target,3))context.damageOwned(hit,7);}
            case "DOG_GAS" -> zone(Effect.DOG_GAS,enemy.position(),4,2,45);
            case "DOG_PAINT" -> {beam(actor.getEyePosition(),target,0xee55ff);context.damageOwned(enemy,5);enemy.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN,45,1));context.paintArea(enemy.position(),2);}
            case "SENTRY_LASER","DOG_LASER" -> {if(actor.energy()<=0){actor.setEnergy(100);return;}actor.setEnergy(actor.energy()-2);beam(actor.getEyePosition(),target,0xffff44);context.damageOwned(enemy,role.startsWith("SENTRY")?9:5);}
            default -> {beam(actor.getEyePosition(),target,0xffeeaa);context.damageOwned(enemy,role.equals("SENTRY_GATLING")?5:role.equals("DOG_BULLET")?6:12);}
        }
    }
    private CallResult vehicleFire(LibertySupportEntity actor,boolean alternate){
        if(now()<weaponReady)return no("Mounted weapon is cycling.");
        if(actor.role().equals("SUPPLY_FRV")){if(actor.ammo()<=0)return no("Supply FRV is empty.");if(!context.personalSupply())return no("Make room for the supply vehicle's ammunition first.");refillEquipment();context.player().heal(5);actor.setAmmo(actor.ammo()-1);weaponReady=now()+40;return yes("Supply FRV refill collected.");}
        Effect effect=switch(actor.role()){
            case "PATRIOT"->alternate?Effect.COMMANDO:Effect.HMG;
            case "EMANCIPATOR"->Effect.AUTOCANNON;
            case "LUMBERER"->alternate?Effect.HEAVY_FLAME:Effect.RECOILLESS;
            case "BREAKTHROUGH"->alternate?Effect.HAMMER:Effect.AIRBURST_ROCKET;
            case "GUNNER_FRV","EMPLACEMENT_HMG"->Effect.HMG;
            case "MAELSTROM"->Effect.MAXIGUN;
            case "BASTION"->alternate?Effect.HMG:Effect.RECOILLESS;
            case "EMPLACEMENT_GRENADE"->Effect.BELT_GRENADE;
            case "EMPLACEMENT_AT"->Effect.DISPOSABLE_AT;
            default->Effect.MACHINE_GUN;
        };
        Weapon spec=weapon(effect);boolean secondary=alternate&&actor.vehicle();
        if(secondary&&actor.energy()<8)return no("The vehicle's secondary reserve is empty.");
        if(!secondary&&actor.ammo()<=0)return no("The mounted weapon's ammunition is empty.");
        int rounds=secondary?Math.max(1,spec.rounds()):Math.min(actor.ammo(),Math.max(1,spec.rounds()));
        fire(spec,context.player().getEyePosition().add(context.player().getLookAngle().scale(1.5)),context.player().getLookAngle(),alternate,rounds);
        if(secondary)actor.setEnergy(actor.energy()-8);else actor.setAmmo(actor.ammo()-rounds);
        actor.setFiring(true);weaponReady=now()+Math.max(4,spec.delay());return yes("Mounted "+effect.name().toLowerCase(java.util.Locale.ROOT).replace('_',' ')+" fired.");
    }
    private void tickBackpack(long time){
        ItemStack pack=context.player().getInventory().getItem(BACKPACK_SLOT);
        if(!LibertyTaczCompat.isLoan(pack,context.player().getUUID())||!pack.hasTag()||!backpack.id().equals(pack.getTag().getString(EQUIPMENT_TAG))){backpack=null;return;}
        if(backpack.effect()==Effect.HOVER&&time<backpackReady-180){Vec3 v=context.player().getDeltaMovement();context.player().setDeltaMovement(v.x,Math.max(v.y,-.025),v.z);context.player().fallDistance=0;context.player().hurtMarked=true;}
        if((backpack.effect()==Effect.DIRECTIONAL_SHIELD||backpack.effect()==Effect.BALLISTIC_SHIELD)&&time%2==0){
            for(Projectile projectile:level().getEntitiesOfClass(Projectile.class,context.player().getBoundingBox().inflate(4))){
                Entity shooter=projectile.getOwner();if(shooter==null||enemies(context.player().position(),128).stream().noneMatch(e->e==shooter))continue;
                Vec3 d=projectile.position().subtract(context.player().position()).normalize();if(d.dot(context.player().getLookAngle())>.25&&personalShield>=3){projectile.discard();personalShield-=3;shieldHit=time;}
            }
        }
    }
    private CallResult activateBackpack(){
        if(backpack==null)return no("Collect a backpack first.");if(now()<backpackReady)return no("Backpack is recharging.");ServerPlayer p=context.player();
        switch(backpack.effect()){
            case SUPPLY_PACK -> {if(backpackAmmo<=0)return no("Supply backpack is empty.");if(!context.personalSupply())return no("Make room for the backpack's ammunition first.");refillEquipment();backpackAmmo--;backpackReady=now()+40;return yes("Personal supply used; "+backpackAmmo+" remain.");}
            case HELLBOMB_PACK -> {if(backpackAmmo<=0)return no("Portable Hellbomb is spent.");backpackAmmo=0;Vec3 center=p.position();warning(center,"Portable Hellbomb");schedule(100,()->blast(center,22,220,2));backpackReady=now()+200;return yes("Portable Hellbomb armed: five seconds. Move away.");}
            case JUMP -> {Vec3 forward=look();p.push(forward.x*.9,1.05,forward.z*.9);p.hurtMarked=true;p.fallDistance=0;backpackReady=now()+220;return yes("Jump pack fired.");}
            case WARP -> {Vec3 goal=ground(p.position().add(p.getLookAngle().scale(12)));AABB body=p.getBoundingBox().move(goal.subtract(p.position()));if(!valid(goal)||!level().noCollision(p,body)||level().getBlockState(BlockPos.containing(goal).below()).getCollisionShape(level(),BlockPos.containing(goal).below()).isEmpty())return no("Warp destination has no safe floor or is obstructed.");level().sendParticles(ParticleTypes.PORTAL,p.getX(),p.getY()+1,p.getZ(),30,.4,.8,.4,.1);p.teleportTo(level(),goal.x,goal.y,goal.z,p.getYRot(),p.getXRot());backpackReady=now()+160;return yes("Warp complete.");}
            case HOVER -> {p.push(0,.6,0);p.hurtMarked=true;backpackReady=now()+300;return yes("Hover pack active for six seconds.");}
            default -> {return yes("This backpack works automatically while equipped.");}
        }
    }
    public float protectedDamage(DamageSource source,float amount){
        if(closed||!context.active()||amount<=0)return amount;
        float remaining=amount;
        if(context.player().getVehicle() instanceof LibertySupportEntity mounted&&owns(mounted))remaining*=mounted.role().equals("BREAKTHROUGH")?.3f:mounted.emplacement()?.65f:.5f;
        boolean protectedByPack=false;
        if(backpack!=null){Effect e=backpack.effect();if(e==Effect.PERSONAL_SHIELD)protectedByPack=true;else if(e==Effect.BALLISTIC_SHIELD||e==Effect.DIRECTIONAL_SHIELD){Vec3 origin=source.getSourcePosition();if(origin==null&&source.getDirectEntity()!=null)origin=source.getDirectEntity().position();protectedByPack=origin!=null&&origin.subtract(context.player().position()).normalize().dot(context.player().getLookAngle())>.25;}}
        if(protectedByPack&&personalShield>0){float absorb=Math.min(personalShield,remaining);personalShield-=absorb;remaining-=absorb;shieldHit=now();}
        for(Entity actor:actors.values())if(actor instanceof LibertySupportEntity relay&&relay.role().equals("SHIELD_RELAY")&&relay.isAlive()&&relay.distanceToSqr(context.player())<=64&&relay.energy()>0){float absorb=Math.min(relay.energy(),remaining);relay.setEnergy(relay.energy()-absorb);remaining-=absorb;break;}
        return Math.max(0,remaining);
    }
    private void refillEquipment(){
        if(weapon!=null&&weapon.effect()!=Effect.DISPOSABLE_AT&&weapon.effect()!=Effect.LEVELER&&weapon.effect()!=Effect.NAPALM_ROCKET&&weapon.effect()!=Effect.BULLET_STORM&&weapon.effect()!=Effect.COMMANDO)weaponAmmo=weapon(weapon.effect()).ammo();
        personalShield=60;
    }
    public boolean authorizedEnemy(Entity entity){return entity instanceof LivingEntity living&&enemies(living.position(),1).stream().anyMatch(e->e==living);}
    public int activeHardware(){return hardware.size();}
    public int pendingEffects(){return jobs.size()+zones.size();}
    public int currentWeaponAmmo(){return weaponAmmo;}
    public static CallResult yes(String message){return new CallResult(true,message);}
    public static CallResult no(String message){return new CallResult(false,message);}
}
