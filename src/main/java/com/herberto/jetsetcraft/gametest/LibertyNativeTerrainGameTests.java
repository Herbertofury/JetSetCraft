package com.herberto.jetsetcraft.gametest;

import com.herberto.jetsetcraft.JetSetCraft;
import com.herberto.jetsetcraft.compat.LibertyHelldiversCompat;
import com.herberto.jetsetcraft.compat.LibertyNativeBulletGuard;
import com.herberto.jetsetcraft.compat.LibertyTaczCompat;
import com.herberto.jetsetcraft.liberty.LibertyCampaignData;
import com.herberto.jetsetcraft.liberty.LibertyManager;
import com.herberto.jetsetcraft.liberty.LibertyOperation;
import com.mojang.authlib.GameProfile;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestAssertException;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.level.TicketType;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.monster.Witch;
import net.minecraft.world.entity.monster.Zombie;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.entity.projectile.Arrow;
import net.minecraft.world.entity.projectile.ThrownPotion;
import net.minecraft.world.entity.vehicle.AbstractMinecart;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.alchemy.Potion;
import net.minecraft.world.item.alchemy.PotionUtils;
import net.minecraft.world.item.alchemy.Potions;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.ChestBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.common.util.FakePlayerFactory;
import net.minecraftforge.gametest.GameTestHolder;
import net.minecraftforge.gametest.PrefixGameTestTemplate;
import net.minecraftforge.registries.ForgeRegistries;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Supplier;

/** Real home admission, native TaCZ constructor/data and native collision. No synthetic hurt/explosion events. */
@GameTestHolder(JetSetCraft.MOD_ID)
@PrefixGameTestTemplate(false)
public final class LibertyNativeTerrainGameTests {
    private static final TicketType<UUID> TICKET=TicketType.create("jetsetcraft_native_terrain_test",UUID::compareTo,20);
    @GameTest(template="hoverboard_empty",timeoutTicks=350)
    public static void homeBulletsKeepNativeOwnedDamageButCannotIgniteResidentsOrMutateTheirWorld(GameTestHelper helper){
        if(!LibertyTaczCompat.available()){
            check(!Boolean.getBoolean("jetsetcraft.libertyPackExpected"),"The full pack lane cannot skip native home safety");
            System.out.println("JETSETCRAFT_GAMETEST_SKIP liberty_tacz_home_safety optional-provider-or-pack-absent");helper.succeed();return;
        }
        Fixture f=new Fixture(helper);
        try{
            check(LibertyHelldiversCompat.terrainSafeTaczApi(),"Pinned native hit interception was not actually installed");
            var offer=illuminateHomeOffer(f.owner);
            check(LibertyManager.startDefense(f.owner,offer.id()+"|6|20")&&LibertyManager.homeDefense(f.owner),
                    "Real home-defense admission did not accept the safe native API and prepared landing area");
            helper.onEachTick(()->{
                if(f.closed)return;
                try{
                    f.ticks++;f.level.getChunkSource().addRegionTicket(TICKET,f.chunk,5,f.owner.getUUID());
                    if(f.ticks==95){
                        check(LibertyManager.operation(f.owner).phase()==LibertyOperation.Phase.ACTIVE&&!f.owner.isPassenger(),"Real home pod did not land");
                        List<Mob> enemies=f.level.getEntitiesOfClass(Mob.class,f.owner.getBoundingBox().inflate(70),m->LibertyManager.belongsTo(m,f.owner)&&m.isAlive());
                        check(!enemies.isEmpty(),"The genuine home director did not create its own enemy");
                        enemies.forEach(mob->{mob.setNoAi(true);mob.setNoGravity(true);mob.setTarget(null);mob.setDeltaMovement(Vec3.ZERO);});
                        f.witch=enemies.stream().filter(mob->mob instanceof Witch).map(mob->(Witch)mob).findFirst()
                                .orElseThrow(()->new GameTestAssertException("The real Illuminate director did not create its owned Witch"));
                        f.enemy=enemies.stream().filter(mob->mob instanceof Zombie).findFirst()
                                .orElseThrow(()->new GameTestAssertException("The real Illuminate director did not create its owned infantry"));
                        f.enemy.setPos(Vec3.atCenterOf(f.center).add(6,1,4));f.enemy.setDeltaMovement(Vec3.ZERO);
                    }
                    if(f.ticks==100){nativeHits(f);f.close();System.out.println("JETSETCRAFT_GAMETEST_PASS liberty_tacz_home_safety native_fire_explosion_potion_arrow_practice_target_chest_return");helper.succeed();}
                }catch(ReflectiveOperationException error){f.close();throw new GameTestAssertException("Native home collision API failed: "+error+"; cause="+error.getCause());}
                catch(RuntimeException|Error error){f.close();throw error;}
            });
        }catch(RuntimeException|Error error){f.close();throw error;}
    }
    private static void nativeHits(Fixture f)throws ReflectiveOperationException{
        Mob cow=f.foreign;float cowHealth=cow.getHealth();Vec3 cowVelocity=cow.getDeltaMovement();
        Projectile fire=bullet(f,"zeta:flam66",true);check(flag(fire,"igniteEntity"),"The real Torcher data did not create an incendiary native bullet");
        hitEntity(fire,cow);
        check(cow.getHealth()==cowHealth&&!cow.isOnFire()&&cow.getDeltaMovement().equals(cowVelocity)&&cow.getActiveEffects().isEmpty(),
                "Native incendiary collision applied a foreign resident's fire, damage, velocity or status before cancellation");
        Projectile practice=bullet(f,"helldiver2:ar23_liberator",false);
        float oldDamage=f.practice.getDamage();int oldHurt=f.practice.getHurtTime();hitEntity(practice,f.practice);
        check(f.practice.getDamage()==oldDamage&&f.practice.getHurtTime()==oldHurt&&practice.isRemoved(),
                "TaCZ's early ITargetEntity callback bypassed the optional native guard");
        Projectile blockFire=bullet(f,"zeta:flam66",true);hitBlock(blockFire,f.wood);
        check(!flag(blockFire,"igniteBlock")&&!flag(blockFire,"explosionDestroyBlock"),"The admitted per-bullet terrain flags were not disabled");
        f.assertWorld();
        potionAndArrowHits(f,cowHealth);
        Projectile explosive=bullet(f,"zeta:gl21",true);check(flag(explosive,"explosion"),"The exact GL-21 data did not create an explosive native bullet");
        float health=f.enemy.getHealth();hitBlock(explosive,f.wood);
        check(f.enemy.getHealth()<health,"Terrain-safe native grenade impact stopped damaging a real owned enemy");
        check(cow.getHealth()==cowHealth&&!cow.isOnFire(),"The native grenade explosion affected a foreign resident");
        f.assertWorld();
        List<Projectile> capacity=new ArrayList<>();
        for(int i=0;i<128;i++){
            Arrow arrow=new Arrow(f.level,f.owner);arrow.setPos(f.owner.getEyePosition());arrow.setNoGravity(true);arrow.setDeltaMovement(Vec3.ZERO);
            check(f.level.addFreshEntity(arrow)&&LibertyManager.belongsTo(arrow,f.owner),"The owned projectile capacity fixture could not admit its bounded resident shots");
            capacity.add(arrow);f.projectiles.add(arrow);
        }
        Projectile rejected=bullet(f,"zeta:flam66",true,false);
        hitEntity(rejected,cow);
        check(rejected.isRemoved()&&cow.getHealth()==cowHealth&&!cow.isOnFire(),
                "An unadmitted native bullet from an active owner escaped the projectile cap through home-world collision");
        capacity.forEach(Entity::discard);
        Projectile inFlight=bullet(f,"helldiver2:ar23_liberator",false);
        check(LibertyManager.leave(f.owner,false,"Native home safety fixture return")&&inFlight.isRemoved()
                        &&f.original.equals(f.owner.getInventory().save(new ListTag()))&&!LibertyManager.hasReturn(f.owner),
                "Real home return retained a provider projectile, borrowed inventory or unsettled return state");
        f.assertWorld();
    }
    private static void potionAndArrowHits(Fixture f,float foreignHealth){
        check(LibertyManager.belongsTo(f.witch,f.owner)&&LibertyManager.belongsTo(f.enemy,f.owner),
                "Native potion source and target are not actual director-owned actors");
        ThrownPotion slow=potion(f,Potions.SLOWNESS);hitBlock(slow,f.wood);
        check(slow.isRemoved()&&slow.getOwner()==f.witch&&f.enemy.hasEffect(MobEffects.MOVEMENT_SLOWDOWN)
                        &&f.enemy.getEffect(MobEffects.MOVEMENT_SLOWDOWN).getDuration()>20,
                "A real owned Witch's block-hit potion lost its native finite effect or exact source identity");
        check(f.foreign.getHealth()==foreignHealth&&f.foreign.getActiveEffects().isEmpty(),
                "Owned native splash applied status to a foreign resident");
        float beforeHealing=f.enemy.getHealth();
        ThrownPotion healing=potion(f,Potions.HEALING);hitBlock(healing,f.wood);
        check(healing.isRemoved()&&f.enemy.getHealth()<beforeHealing,
                "Scoped native healing did not retain vanilla's real damage to owned undead infantry");
        check(f.foreign.getHealth()==foreignHealth&&f.foreign.getActiveEffects().isEmpty(),
                "Owned native splash healed an injured foreign resident before the damage guard");
        f.enemy.invulnerableTime=0; // Separate collisions exercise effects independently of vanilla hit immunity.
        Arrow arrow=new Arrow(f.level,f.owner);arrow.setNoGravity(true);f.projectiles.add(arrow);
        check(f.level.addFreshEntity(arrow)&&LibertyManager.belongsTo(arrow,f.owner),"Actual owned block-impact arrow was not admitted");
        Vec3 rayStart = new Vec3(f.target.getX()-2, f.target.getY()+.5, f.target.getZ()+.5);
        var ray = f.level.clip(new net.minecraft.world.level.ClipContext(rayStart, rayStart.add(3,0,0),
                net.minecraft.world.level.ClipContext.Block.COLLIDER, net.minecraft.world.level.ClipContext.Fluid.NONE, arrow));
        check(ray.getType() == net.minecraft.world.phys.HitResult.Type.BLOCK && ray.getBlockPos().equals(f.target),
                "Native arrow fixture ray did not reach its target block: " + ray.getBlockPos());
        hitBlock(arrow,f.target);
        check(arrow.isRemoved(), "Owned native arrow was not removed at its verified home block impact");
        check(f.level.getBlockState(f.target).equals(Blocks.TARGET.defaultBlockState()),
                "Owned native arrow invoked a home target block's redstone callback");
        f.assertWorld();
    }
    private static ThrownPotion potion(Fixture f,Potion type){
        ThrownPotion shot=new ThrownPotion(f.level,f.witch);
        shot.setItem(PotionUtils.setPotion(new ItemStack(Items.SPLASH_POTION),type));
        shot.setNoGravity(true);f.projectiles.add(shot);
        check(f.level.addFreshEntity(shot)&&LibertyManager.belongsTo(shot,f.owner)&&shot.getOwner()==f.witch,
                "The native Witch potion did not inherit actual live mission ownership");
        return shot;
    }
    /** Public campaign transactions select an Illuminate home offer; no actor tags or score fields are forged. */
    static LibertyCampaignData.DefenseOffer illuminateHomeOffer(ServerPlayer player){
        var campaign=LibertyCampaignData.get(player.getServer());
        for(int i=0;i<2;i++)check(campaign.settle(new LibertyCampaignData.Result(UUID.randomUUID(),player.getUUID(),
                "blackout_borough","illuminate",10,0,3,0,0,false,null)),"Could not establish failed Illuminate fixture expeditions");
        var regional=campaign.offers(player.getUUID()).stream().filter(offer->!offer.homeWorld()
                &&offer.sectorId().equals("blackout_borough")&&offer.claimedRun()==null).findFirst().orElseThrow();
        UUID failed=UUID.randomUUID();
        check(campaign.claimDefense(player.getUUID(),regional.id(),failed,campaign.revision())
                        &&campaign.settle(new LibertyCampaignData.Result(failed,player.getUUID(),"blackout_borough","illuminate",
                        1,0,3,0,0,false,regional.id())),"A real failed Illuminate defense did not produce its home offer");
        return campaign.offers(player.getUUID()).stream().filter(LibertyCampaignData.DefenseOffer::homeWorld).findFirst().orElseThrow();
    }
    private static Projectile bullet(Fixture f,String id,boolean unsafeFlags)throws ReflectiveOperationException{
        return bullet(f,id,unsafeFlags,true);
    }
    private static Projectile bullet(Fixture f,String id,boolean unsafeFlags,boolean expectedAdmission)throws ReflectiveOperationException{
        var loadout=LibertyHelldiversCompat.supportWeapon(id,f.owner.getUUID());check(loadout.ready(),"Missing live native regression weapon: "+id);
        ItemStack gun=loadout.gun();f.owner.getInventory().setItem(0,gun);f.owner.getInventory().selected=0;
        Class<?> operators=type("api.entity.IGunOperator");Object operator=operators.getMethod("fromLivingEntity",LivingEntity.class).invoke(null,f.owner);
        operators.getMethod("initialData").invoke(operator);operators.getMethod("draw",Supplier.class).invoke(operator,(Supplier<ItemStack>)()->gun);
        Object index=((Optional<?>)type("api.TimelessAPI").getMethod("getCommonGunIndex",ResourceLocation.class).invoke(null,new ResourceLocation(id))).orElseThrow();
        Object data=index.getClass().getMethod("getGunData").invoke(index),bulletData=index.getClass().getMethod("getBulletData").invoke(index);
        ResourceLocation ammo=(ResourceLocation)data.getClass().getMethod("getAmmoId").invoke(data);
        // This is the provider's genuine public projectile constructor with its live gun and bullet
        // definitions. Charging/draw animations are outside this collision regression's scope.
        Projectile shot=(Projectile)type("entity.EntityKineticBullet").getConstructor(Level.class,LivingEntity.class,ItemStack.class,
                        ResourceLocation.class,ResourceLocation.class,boolean.class,type("resource.pojo.data.gun.GunData"),type("resource.pojo.data.gun.BulletData"))
                .newInstance(f.level,f.owner,gun,ammo,new ResourceLocation(id),false,data,bulletData);
        if(unsafeFlags){setFlag(shot,"igniteBlock",true);setFlag(shot,"explosionDestroyBlock",true);}
        shot.setPos(f.owner.getEyePosition());shot.setDeltaMovement(Vec3.ZERO);f.projectiles.add(shot);
        boolean admitted=f.level.addFreshEntity(shot);
        if(expectedAdmission)check(admitted&&shot instanceof LibertyNativeBulletGuard&&LibertyManager.belongsTo(shot,f.owner)
                        &&!flag(shot,"igniteBlock")&&!flag(shot,"explosionDestroyBlock"),
                "Actual home projectile join did not prove owner, injected guard and safe per-bullet fields");
        else check(!admitted&&!LibertyManager.belongsTo(shot,f.owner),"The full operation projectile pool admitted an untracked native shot");
        return shot;
    }
    private static void hitEntity(Projectile shot,Entity target){Vec3 p=target.getBoundingBox().getCenter();shot.setPos(p.x-2,p.y,p.z);shot.setDeltaMovement(3,0,0);shot.tick();}
    private static void hitBlock(Projectile shot,BlockPos target){shot.setPos(target.getX()-2,target.getY()+.5,target.getZ()+.5);shot.setDeltaMovement(3,0,0);shot.tick();}
    private static boolean flag(Entity e,String name)throws ReflectiveOperationException{Field f=e.getClass().getDeclaredField(name);f.setAccessible(true);return f.getBoolean(e);}
    private static void setFlag(Entity e,String name,boolean value)throws ReflectiveOperationException{Field f=e.getClass().getDeclaredField(name);f.setAccessible(true);f.setBoolean(e,value);}
    private static Class<?> type(String suffix)throws ClassNotFoundException{return Class.forName("com.tacz.guns."+suffix);}
    /** These remote test sites are outside the template and therefore contain seeded world terrain.
     * Lay the authored floor only after clearing each column to its actual surface, not just a
     * fixed headroom. Otherwise mountains/trees can change heightmap admission or block a shot.
     * This is disposable GameTest setup, never production home-world terrain mutation.
     */
    static void prepareHomeFloor(ServerLevel level, BlockPos center) {
        for (int x=-128; x<=128; x++) for (int z=-128; z<=128; z++) {
            BlockPos floor = center.offset(x,0,z);
            int top = level.getHeight(net.minecraft.world.level.levelgen.Heightmap.Types.WORLD_SURFACE,
                    floor.getX(), floor.getZ());
            for (int y=top; y>floor.getY(); y--) level.setBlock(floor.atY(y), Blocks.AIR.defaultBlockState(), 2);
            level.setBlock(floor, Blocks.STONE.defaultBlockState(), 3);
            check(level.getHeight(net.minecraft.world.level.levelgen.Heightmap.Types.MOTION_BLOCKING_NO_LEAVES,
                    floor.getX(), floor.getZ()) == floor.getY()+1, "Home fixture retained terrain above its authored floor: " + floor);
        }
    }
    private static final class Fixture{
        final ServerLevel level;final ServerPlayer owner;final BlockPos center=new BlockPos(37000,100,-37000),wood=center.offset(8,1,2),chest=center.offset(9,1,2),target=center.offset(-6,1,3);
        final ChunkPos chunk=new ChunkPos(center);final ListTag original;final Map<BlockPos,BlockState> protectedBlocks=new LinkedHashMap<>();
        final List<Entity> projectiles=new ArrayList<>();final Mob foreign;final AbstractMinecart practice;final CompoundTag chestNbt;Mob enemy;Witch witch;boolean closed;int ticks;
        Fixture(GameTestHelper helper){
            level=helper.getLevel();for(int x=-9;x<=9;x++)for(int z=-9;z<=9;z++)level.getChunk(chunk.x+x,chunk.z+z);
            prepareHomeFloor(level, center);
            for(int x=-128;x<=128;x++)for(int z=-128;z<=128;z++){BlockPos pos=center.offset(x,0,z);protectedBlocks.put(pos,Blocks.STONE.defaultBlockState());}
            level.setBlock(wood,Blocks.OAK_PLANKS.defaultBlockState(),3);protectedBlocks.put(wood,level.getBlockState(wood));
            level.setBlock(chest,Blocks.CHEST.defaultBlockState(),3);protectedBlocks.put(chest,level.getBlockState(chest));
            level.setBlock(target,Blocks.TARGET.defaultBlockState(),3);protectedBlocks.put(target,level.getBlockState(target));
            ((ChestBlockEntity)level.getBlockEntity(chest)).setItem(0,new ItemStack(Items.EMERALD,23));chestNbt=level.getBlockEntity(chest).saveWithFullMetadata();
            for(int x=-2;x<=2;x++)for(int y=0;y<=2;y++)for(int z=-2;z<=2;z++){BlockPos p=wood.offset(x,y,z);protectedBlocks.putIfAbsent(p,level.getBlockState(p));}
            owner=FakePlayerFactory.get(level,new GameProfile(UUID.randomUUID(),"JSC_home_gun"));owner.setGameMode(GameType.ADVENTURE);owner.getInventory().clearContent();owner.getInventory().setItem(7,new ItemStack(Items.DIAMOND,11));owner.moveTo(center.getX()+.5,101.1,center.getZ()+.5,0,0);original=owner.getInventory().save(new ListTag());
            foreign=EntityType.COW.create(level);foreign.setNoAi(true);foreign.setNoGravity(true);foreign.setHealth(foreign.getMaxHealth()-3);foreign.setPos(Vec3.atCenterOf(center).add(8,1,0));check(level.addFreshEntity(foreign),"Foreign home cow could not spawn");
            EntityType<?> targetType=ForgeRegistries.ENTITY_TYPES.getValue(new ResourceLocation("tacz","target_minecart"));check(targetType!=null,"Real TaCZ target minecart is absent");
            practice=(AbstractMinecart)targetType.create(level);practice.setNoGravity(true);practice.setPos(Vec3.atCenterOf(center).add(0,1,8));check(level.addFreshEntity(practice),"Real foreign practice target could not spawn");
            level.getChunkSource().addRegionTicket(TICKET,chunk,5,owner.getUUID());
        }
        void assertWorld(){for(var e:protectedBlocks.entrySet())check(e.getValue().equals(level.getBlockState(e.getKey())),"A native shot changed home block "+e.getKey());check(chestNbt.equals(level.getBlockEntity(chest).saveWithFullMetadata()),"A native shot changed real chest contents/NBT");}
        void close(){if(closed)return;closed=true;try{if(LibertyManager.active(owner)||LibertyManager.hasReturn(owner))LibertyManager.leave(owner,false,"Native terrain fixture cleanup");LibertyManager.forget(owner);}finally{projectiles.forEach(Entity::discard);foreign.discard();practice.discard();level.getChunkSource().removeRegionTicket(TICKET,chunk,5,owner.getUUID());}}
    }
    private static void check(boolean ok,String message){if(!ok)throw new GameTestAssertException(message);}
    private LibertyNativeTerrainGameTests(){}
}
