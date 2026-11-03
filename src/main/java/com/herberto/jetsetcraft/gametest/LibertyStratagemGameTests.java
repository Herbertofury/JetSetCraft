package com.herberto.jetsetcraft.gametest;

import com.herberto.jetsetcraft.JetSetCraft;
import com.herberto.jetsetcraft.compat.LibertyHelldiversCompat;
import com.herberto.jetsetcraft.entity.GraffitiEntity;
import com.herberto.jetsetcraft.entity.LibertySupportEntity;
import com.herberto.jetsetcraft.liberty.LibertyStratagemCatalog;
import com.herberto.jetsetcraft.liberty.LibertyStratagemEngine;
import com.herberto.jetsetcraft.registry.ModEntities;
import com.mojang.authlib.GameProfile;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestAssertException;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.level.TicketType;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.ChestBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.util.FakePlayerFactory;
import net.minecraftforge.event.entity.EntityJoinLevelEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.gametest.GameTestHolder;
import net.minecraftforge.gametest.PrefixGameTestTemplate;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.IntConsumer;

/** Real world effects through the production engine. Only the operation admission adapter is a fixture. */
@GameTestHolder(JetSetCraft.MOD_ID)
@PrefixGameTestTemplate(false)
public final class LibertyStratagemGameTests {
    private static final AtomicInteger SEQUENCE = new AtomicInteger();
    private static final TicketType<UUID> TICKET = TicketType.create("jetsetcraft_stratagem_test", UUID::compareTo, 20);

    @GameTest(template="hoverboard_empty", timeoutTicks=100)
    public static void catalogueSeparatesRealSelectableSharedConditionalAndUnavailableCalls(GameTestHelper helper) {
        var rows=LibertyStratagemCatalog.all();
        check(rows.size()==125 && LibertyStratagemCatalog.selectable().size()==100, "The dated catalogue lost a researched or original entry");
        check(rows.stream().map(LibertyStratagemCatalog.Entry::id).distinct().count()==rows.size(), "The catalogue has duplicate IDs");
        check(rows.stream().filter(e->e.category()==LibertyStratagemCatalog.Category.SIGNATURE).count()==6
                && rows.stream().filter(e->e.category()==LibertyStratagemCatalog.Category.HISTORICAL).noneMatch(LibertyStratagemCatalog.Entry::selectable),
                "Original signatures or historical availability were misclassified");
        for(var row:LibertyStratagemCatalog.selectable()) check(!row.description().isBlank()&&!row.controls().isBlank()
                        &&!row.source().isBlank()&&row.unlockCost()>0&&row.effect()!=LibertyStratagemCatalog.Effect.MISSION
                        &&row.effect()!=LibertyStratagemCatalog.Effect.UNAVAILABLE,
                "A selectable call lacks a real effect, cost, provenance or controls: "+row.id());
        boolean nativeExpected=Boolean.getBoolean("jetsetcraft.libertyHelldivers");
        check(ModList.get().isLoaded("mcdivers")==nativeExpected && LibertyHelldiversCompat.available()==nativeExpected,
                "MCDivers presence/API disagree with the selected optional lane: "+LibertyHelldiversCompat.status());
        if(ModList.get().isLoaded("tacz")) check(LibertyHelldiversCompat.terrainSafeTaczApi(),
                "The pinned native bullet API is missing its actual optional pre-hit safety mixin");
        pass(helper,"liberty_stratagem_catalog native="+nativeExpected+" selectable=100 conditional=25");
    }

    @GameTest(template="hoverboard_empty", timeoutTicks=230)
    public static void fourSlotsPhasesCooldownsAndEagleChargesRemainAuthoritative(GameTestHelper helper) {
        Fixture f=new Fixture(helper,false,16);
        try {
            check(!f.engine.setLoadout(List.of("eagle_airstrike","eagle_airstrike")).accepted()
                    && !f.engine.setLoadout(List.of("orbital_confetti")).accepted(), "Duplicate or historical equipment entered a loadout");
            check(f.engine.setLoadout(List.of("eagle_500kg_bomb","orbital_precision_strike","chromageddon","tag_team")).accepted(), "Four valid calls could not equip");
            check(!row(f,"eagle_rearm").ready()&&!row(f,"reinforce").ready()&&!row(f,"seaf_artillery").ready(),
                    "Full Eagle ammunition or unauthorized mission actions were advertised as ready");
            check(!f.engine.call("eagle_500kg_bomb",f.owner.position().add(500,0,0)).accepted()&&row(f,"eagle_500kg_bomb").ammunition()==1,
                    "An invalid target consumed Eagle resources");
            check(f.engine.call("eagle_500kg_bomb",f.point).accepted() && row(f,"eagle_500kg_bomb").ammunition()==0
                    && !row(f,"eagle_500kg_bomb").ready()&&!f.engine.call("eagle_500kg_bomb",f.point).accepted()
                    && !f.engine.setLoadout(LibertyStratagemCatalog.starters()).accepted(), "A consumed charge or deployed loadout could be reset");
            f.phase="DOWNED";f.reinforce=true;
            check(row(f,"reinforce").ready()&&f.engine.snapshot().stream().filter(LibertyStratagemEngine.Row::ready).allMatch(r->r.id().equals("reinforce")),
                    "Downed state exposed field calls instead of only a usable reinforcement");
            check(!f.engine.rearm().accepted(), "A direct rearm call bypassed the downed phase gate");
            f.phase="ACTIVE";f.reinforce=false;
            f.run(tick->{if(tick==175){
                check(row(f,"eagle_rearm").ready()&&f.engine.rearm().accepted()&&!row(f,"eagle_rearm").ready()
                        &&row(f,"eagle_rearm").cooldownTicks()>=2399&&!f.engine.rearm().accepted(), "Eagle rearm failed to lock the shared turnaround");
                check(f.engine.call("resupply",f.point).accepted()&&f.supplies==1&&!row(f,"resupply").ready(), "Shared supply was not consumed once with a cooldown");
                f.close();pass(helper,"liberty_stratagem_resources");
            }});
        } catch(RuntimeException|Error error){f.close();throw error;}
    }

    @GameTest(template="hoverboard_empty", timeoutTicks=320)
    public static void sentriesMinesShieldsAndChromageddonUseDistinctRealEffects(GameTestHelper helper) {
        Fixture f=new Fixture(helper,false,50);
        try {
            // Separate the targets so early sentry knockback cannot remove the mine's later trigger,
            // and damage from one call cannot satisfy another call's assertion.
            Vec3 sentryPosition=f.point.add(-24,0,-24),minePosition=f.point.add(26,0,25);
            Mob sentryTarget=f.enemy(sentryPosition.add(3,0,0));
            sentryTarget.getAttribute(Attributes.KNOCKBACK_RESISTANCE).setBaseValue(1);
            float sentryHealth=sentryTarget.getHealth();
            LivingEntity foreign=f.foreign(f.point.add(-2,0,0));
            LivingEntity sentryForeign=f.foreign(sentryPosition.add(3,0,2));
            float protectedHealth=foreign.getHealth(),sentryForeignHealth=sentryForeign.getHealth();
            check(f.engine.setLoadout(List.of("a_mg_43_machine_gun_sentry","md_17_anti_tank_mines","fx_12_shield_generator_relay","chromageddon")).accepted(),"Hardware fixture loadout failed");
            check(f.engine.call("a_mg_43_machine_gun_sentry",sentryPosition).accepted()
                    &&f.engine.call("md_17_anti_tank_mines",minePosition).accepted()
                    &&f.engine.call("fx_12_shield_generator_relay",f.owner.position().add(0,0,3)).accepted(),
                    "One of the distinct hardware calls was rejected");
            var sentry=f.support("SENTRY_MG");var relay=f.support("SHIELD_RELAY");
            List<LibertySupportEntity> mines=f.tracked.stream().filter(e->e instanceof LibertySupportEntity s&&s.mine())
                    .map(e->(LibertySupportEntity)e).toList();
            check(mines.size()==8,"The anti-tank field did not deploy eight individual mines");
            LibertySupportEntity triggerMine=mines.get(0);
            check(triggerMine.distanceToSqr(sentry)>48*48,"Mine probe overlaps the sentry's firing range");
            LivingEntity mineForeign=f.foreign(triggerMine.position().add(0,0,-2));
            float mineForeignHealth=mineForeign.getHealth();
            check(f.engine.protectedDamage(sentryTarget.damageSources().mobAttack(sentryTarget),25)==0&&relay.energy()==75,
                    "The actual relay did not spend its finite energy to protect its owner");
            class Progress {int phase,started;Mob mineTarget,paintTarget;float mineHealth,paintHealth;}
            Progress progress=new Progress();
            f.run(tick->{
                if(progress.phase==0){
                    check(tick<100,"Hardware did not receive native world ticks: sentry="+sentry.tickCount+", mine="+triggerMine.tickCount);
                    if(sentry.tickCount<35||triggerMine.tickCount<35)return;
                    check(sentry.ammo()<180,"The real sentry never consumed ammunition: ammo="+sentry.ammo());
                    check(sentryTarget.getHealth()<sentryHealth,"The real sentry never damaged its isolated owned target");
                    check(mines.stream().noneMatch(Entity::isRemoved),"A mine triggered before an eligible owned target arrived");
                    progress.mineTarget=f.enemy(triggerMine.position().add(.5,0,0));
                    progress.mineHealth=progress.mineTarget.getHealth();progress.started=tick;progress.phase=1;
                    return;
                }
                if(progress.phase==1){
                    check(tick-progress.started<30,"An armed mine did not trigger on the real owned target: mineTicks="
                            +triggerMine.tickCount+", targetDistance="+triggerMine.distanceTo(progress.mineTarget));
                    if(!triggerMine.isRemoved())return;
                    check(progress.mineTarget.getHealth()<progress.mineHealth,"The triggered mine never damaged its isolated owned target");
                    check(mines.stream().anyMatch(e->!e.isRemoved()),"One local trigger removed the entire minefield");
                    progress.paintTarget=f.enemy(f.point.add(45,0,0));progress.paintHealth=progress.paintTarget.getHealth();
                    check(progress.paintTarget.distanceToSqr(sentry)>48*48,"Outer paint probe overlaps the sentry's firing range");
                    check(f.engine.call("chromageddon",f.point).accepted(),"CHROMAGEDDON was rejected after the hardware probes");
                    progress.started=tick;progress.phase=2;
                    return;
                }
                check(tick-progress.started<150,"CHROMAGEDDON never completed its scheduled rings: radius="+f.largestPaint);
                if(f.largestPaint<48)return;
                check(f.decals.size()>=8&&f.decals.stream().anyMatch(e->!e.isRemoved()),
                        "CHROMAGEDDON's full radius did not leave real overlays");
                check(progress.paintTarget.getHealth()<progress.paintHealth,"CHROMAGEDDON's outer ring never damaged its isolated owned target");
                check(foreign.getHealth()==protectedHealth&&!foreign.isOnFire()
                                &&sentryForeign.getHealth()==sentryForeignHealth&&!sentryForeign.isOnFire()
                                &&mineForeign.getHealth()==mineForeignHealth&&!mineForeign.isOnFire(),
                        "Call-in effects injured a foreign resident");
                f.assertWorld();f.close();
                check(f.tracked.stream().allMatch(Entity::isRemoved)&&f.engine.pendingEffects()==0,"Closing support left hardware or delayed effects active");
                pass(helper,"liberty_stratagem_effects");
            });
        } catch(RuntimeException|Error error){f.close();throw error;}
    }

    @GameTest(template="hoverboard_empty", timeoutTicks=220)
    public static void deliveredWeaponsBackpacksAndMountedVehiclesPerformOwnedActions(GameTestHelper helper) {
        Fixture f=new Fixture(helper,false,20);
        try {
            f.enemy(f.owner.position().add(0,0,12));
            check(f.engine.setLoadout(List.of("mg_43_machine_gun","lift_850_jump_pack","exo_45_patriot_exosuit","tag_team")).accepted(),"Equipment fixture loadout failed");
            check(f.engine.call("mg_43_machine_gun",f.owner.position().add(1,0,0)).accepted()
                    &&f.engine.call("lift_850_jump_pack",f.owner.position().add(-1,0,0)).accepted(),"Real support deliveries were not created");
            List<LibertySupportEntity> pods=f.tracked.stream().filter(e->e instanceof LibertySupportEntity s&&s.role().equals("SUPPLY"))
                    .map(e->(LibertySupportEntity)e).toList();
            check(pods.size()==2,"Both real equipment deliveries must exist before collection");
            class Progress {LibertySupportEntity vehicle;Vec3 start;int mountedAt,vehicleTicks;}
            Progress progress=new Progress();
            f.run(tick->{
                if(progress.vehicle==null){
                    check(tick<90,"Equipment pods did not land through native ticking: ticks="+pods.stream().map(p->p.tickCount).toList());
                    if(pods.stream().anyMatch(p->p.tickCount<20))return;
                    for(LibertySupportEntity pod:pods)
                        check(f.engine.collectDelivery(pod).accepted(),"An arrived owner delivery could not equip its real tool");
                    f.owner.getInventory().selected=3;int ammo=f.engine.currentWeaponAmmo();
                    check(f.engine.useEquipment(InteractionHand.MAIN_HAND,false).accepted()&&f.engine.currentWeaponAmmo()<ammo,
                            "The collected machine gun did not fire or consume ammunition");
                    f.owner.getInventory().selected=7;
                    check(f.engine.useEquipment(InteractionHand.MAIN_HAND,false).accepted()&&f.owner.getDeltaMovement().y>0
                            &&!f.engine.useEquipment(InteractionHand.MAIN_HAND,false).accepted(),"Jump backpack was cosmetic or ignored its recharge");
                    f.owner.setDeltaMovement(Vec3.ZERO);
                    check(f.engine.call("exo_45_patriot_exosuit",f.owner.position().add(3,0,0)).accepted(),"The physical walker could not deploy");
                    LibertySupportEntity vehicle=f.support("PATRIOT");check(f.owner.startRiding(vehicle,true),"Owner could not mount the actual support vehicle");
                    f.engine.mounted(vehicle);f.owner.zza=1;
                    progress.vehicle=vehicle;progress.start=vehicle.position();progress.mountedAt=tick;progress.vehicleTicks=vehicle.tickCount;
                    return;
                }
                // A real rider continues sending held input. Passenger ticks may clear a one-shot
                // FakePlayer input, and chunk readiness must not be mistaken for vehicle ticks.
                LibertySupportEntity vehicle=progress.vehicle;f.owner.zza=1;
                check(vehicle.getControllingPassenger()==f.owner,"The deployed walker lost its controlling owner passenger");
                check(tick-progress.mountedAt<100,"The mounted walker did not receive native world ticks: ticks="+vehicle.tickCount);
                if(vehicle.tickCount-progress.vehicleTicks>=20){
                    check(vehicle.position().z>progress.start.z+1.5,
                            "The rideable walker did not move from held owner input after twenty native ticks: start="
                                    +progress.start+", actual="+vehicle.position()+", forward="+f.owner.zza);
                    int ammo=vehicle.ammo();check(f.engine.useEquipment(InteractionHand.MAIN_HAND,false).accepted()&&vehicle.ammo()<ammo,
                            "The mounted weapon did not consume its actual finite ammunition");
                    f.owner.stopRiding();f.owner.zza=0;f.assertWorld();f.close();pass(helper,"liberty_stratagem_equipment");
                }
            });
        } catch(RuntimeException|Error error){f.close();throw error;}
    }

    @GameTest(template="hoverboard_empty", timeoutTicks=420)
    public static void realMCDiversOrdnanceAndSentryPreserveForeignEntitiesTerrainAndLightCleanup(GameTestHelper helper) {
        if(!LibertyHelldiversCompat.available()) {
            check(!Boolean.getBoolean("jetsetcraft.libertyHelldivers"),"The native call-in lane cannot skip its provider");
            System.out.println("JETSETCRAFT_GAMETEST_SKIP liberty_mcdivers_native optional-provider-absent");helper.succeed();return;
        }
        Fixture f=new Fixture(helper,true,20);
        try {
            Mob enemy=f.enemy(f.point.add(2,0,0)); LivingEntity foreign=f.foreign(f.point.add(-2,0,0));
            float hp=enemy.getHealth(),protectedHp=foreign.getHealth();
            check(f.engine.call("orbital_precision_strike",f.point).accepted()
                    &&f.engine.call("a_mg_43_machine_gun_sentry",f.point.add(0,0,8)).accepted(), "Real native beacons were not admitted");
            check(f.tracked.stream().filter(e->LibertyHelldiversCompat.actor(e)).count()==2,"The provider branch substituted non-native beacons");
            f.run(tick->{if(tick==320){
                check(f.nativeKinds.contains("ordnance")&&f.nativeKinds.contains("machine_gun_sentry")&&enemy.getHealth()<hp,
                        "Real native beacon timelines did not create ordnance, a sentry and actual enemy damage; actors="+f.nativeKinds+"; health="+enemy.getHealth());
                check(foreign.getHealth()==protectedHp&&!foreign.isOnFire(),"Native MCDivers hurt a foreign resident");
                f.assertWorld();
                List<BlockPos> light=f.tracked.stream().filter(e->e.getClass().getName().endsWith("SentryEntity"))
                        .flatMap(e->List.of(e.blockPosition(),e.blockPosition().above()).stream()).toList();
                f.close();
                check(f.tracked.stream().allMatch(Entity::isRemoved)&&light.stream().noneMatch(p->new ResourceLocation("mcdivers","sentry_light").equals(ForgeRegistries.BLOCKS.getKey(f.level.getBlockState(p).getBlock()))),
                        "Native provider cleanup retained an actor or its temporary light");
                pass(helper,"liberty_mcdivers_native");
            }});
        } catch(RuntimeException|Error error){f.close();throw error;}
    }

    private static LibertyStratagemEngine.Row row(Fixture f,String id){return f.engine.snapshot().stream().filter(r->r.id().equals(id)).findFirst().orElseThrow();}
    private static final class Fixture implements LibertyStratagemEngine.Context {
        final GameTestHelper helper;final ServerLevel level;final ServerPlayer owner;final UUID operation=UUID.randomUUID();
        final BlockPos center,chest;final ChunkPos chunk;final Vec3 point;final LibertyStratagemEngine engine;
        final List<Entity> tracked=new ArrayList<>(),residents=new ArrayList<>();final List<LivingEntity> enemies=new ArrayList<>();
        final List<GraffitiEntity> decals=new ArrayList<>();final Map<BlockPos,BlockState> fixtureBlocks=new LinkedHashMap<>();
        final Set<String> nativeKinds=new java.util.HashSet<>();final StratagemProviderCapture capture;final boolean nativeProvider;
        final CompoundTag chestBefore;boolean closed,reinforce;int elapsed,supplies=2;double largestPaint;String phase="ACTIVE";
        Fixture(GameTestHelper helper,boolean nativeProvider,int radius){
            this.helper=helper;this.level=helper.getLevel();this.nativeProvider=nativeProvider;
            center=new BlockPos(32000+SEQUENCE.getAndIncrement()*256,100,-32000);chunk=new ChunkPos(center);
            for(int x=-4;x<=4;x++)for(int z=-4;z<=4;z++)level.getChunk(chunk.x+x,chunk.z+z);
            for(int x=-radius;x<=radius;x++)for(int z=-radius;z<=radius;z++){BlockPos p=center.offset(x,0,z);level.setBlock(p,Blocks.STONE.defaultBlockState(),3);fixtureBlocks.put(p,Blocks.STONE.defaultBlockState());}
            chest=center.offset(7,1,2);level.setBlock(chest,Blocks.CHEST.defaultBlockState(),3);
            ((ChestBlockEntity)level.getBlockEntity(chest)).setItem(0,new ItemStack(Items.DIAMOND,17));chestBefore=level.getBlockEntity(chest).saveWithFullMetadata();fixtureBlocks.put(chest,level.getBlockState(chest));
            owner=FakePlayerFactory.get(level,new GameProfile(UUID.randomUUID(),"JSC_support"));owner.getInventory().clearContent();owner.setGameMode(GameType.SURVIVAL);
            owner.moveTo(center.getX()+.5,101.05,center.getZ()-5.5,0,0);owner.setDeltaMovement(Vec3.ZERO);point=Vec3.atCenterOf(center).add(0,.55,5);
            engine=new LibertyStratagemEngine(this);capture=new StratagemProviderCapture(this);MinecraftForge.EVENT_BUS.register(capture);
            level.getChunkSource().addRegionTicket(TICKET,chunk,5,operation);
        }
        Mob enemy(Vec3 pos){Mob mob=EntityType.HUSK.create(level);mob.getAttribute(Attributes.MAX_HEALTH).setBaseValue(1000);mob.setHealth(1000);mob.setNoAi(true);mob.setNoGravity(true);mob.setPersistenceRequired();mob.setPos(pos);enemies.add(mob);residents.add(mob);check(level.addFreshEntity(mob),"Owned-effect fixture target could not spawn");return mob;}
        LivingEntity foreign(Vec3 pos){Mob mob=EntityType.COW.create(level);mob.setNoAi(true);mob.setNoGravity(true);mob.setPos(pos);residents.add(mob);check(level.addFreshEntity(mob),"Foreign resident fixture could not spawn");return mob;}
        LibertySupportEntity support(String role){return tracked.stream().filter(e->e instanceof LibertySupportEntity s&&s.role().equals(role)).map(e->(LibertySupportEntity)e).findFirst().orElseThrow();}
        void run(IntConsumer assertion){helper.onEachTick(()->{if(closed)return;try{elapsed++;level.getChunkSource().addRegionTicket(TICKET,chunk,5,operation);engine.tick();assertion.accept(elapsed);}catch(RuntimeException|Error error){close();throw error;}});}
        void assertWorld(){for(var e:fixtureBlocks.entrySet())check(level.getBlockState(e.getKey()).equals(e.getValue()),"A call-in altered a protected fixture block at "+e.getKey());check(chestBefore.equals(level.getBlockEntity(chest).saveWithFullMetadata()),"A call-in changed chest contents/NBT");}
        void close(){if(closed)return;engine.close();closed=true;MinecraftForge.EVENT_BUS.unregister(capture);residents.forEach(Entity::discard);decals.forEach(Entity::discard);level.getChunkSource().removeRegionTicket(TICKET,chunk,5,operation);owner.getInventory().clearContent();}
        @Override public ServerPlayer player(){return owner;}@Override public UUID operation(){return operation;}@Override public boolean active(){return !closed;}@Override public String phase(){return phase;}
        @Override public boolean validTarget(Vec3 p){return p.distanceToSqr(Vec3.atCenterOf(center))<150*150;}@Override public boolean unlocked(String id){return true;}
        @Override public List<LivingEntity> ownedEnemiesWithin(Vec3 p,double r){return enemies.stream().filter(e->e.isAlive()&&e.distanceToSqr(p)<=r*r).toList();}
        @Override public void track(Entity e){tracked.add(e);if(LibertyHelldiversCompat.actor(e))nativeKinds.add(ForgeRegistries.ENTITY_TYPES.getKey(e.getType()).getPath());}
        @Override public boolean damageOwned(Entity e,float amount){return enemies.contains(e)&&e.hurt(owner.damageSources().playerAttack(owner),amount);}
        @Override public int paintArea(Vec3 p,double r){largestPaint=Math.max(largestPaint,r);BlockPos support=center.offset((int)Math.min(45,r-3),0,5);GraffitiEntity paint=ModEntities.GRAFFITI.get().create(level);paint.configureSplash(support,Direction.UP,"",2.5f);if(level.addFreshEntity(paint)){decals.add(paint);return 1;}return 0;}
        @Override public boolean terrainSafe(){return true;}@Override public boolean nativeProviderSafe(){return nativeProvider;}
        @Override public boolean supply(){if(supplies<=0)return false;supplies--;return true;}@Override public boolean canSupply(){return supplies>0;}
        @Override public boolean reinforce(){return reinforce;}@Override public boolean canReinforce(){return reinforce;}
        @Override public boolean missionAction(String id,Vec3 target){return false;}
    }
    public static final class StratagemProviderCapture {
        private final Fixture fixture;StratagemProviderCapture(Fixture fixture){this.fixture=fixture;}
        @SubscribeEvent(priority=EventPriority.HIGHEST) public void join(EntityJoinLevelEvent event){if(event.getLevel()==fixture.level&&LibertyHelldiversCompat.actor(event.getEntity()))fixture.engine.adoptProviderEntity(event.getEntity());}
    }
    private static void check(boolean ok,String message){if(!ok)throw new GameTestAssertException(message);}
    private static void pass(GameTestHelper helper,String marker){System.out.println("JETSETCRAFT_GAMETEST_PASS "+marker);helper.succeed();}
    private LibertyStratagemGameTests(){}
}
