package com.herberto.jetsetcraft.compat;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.registries.ForgeRegistries;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

/** Optional bridge to the actual published MCDivers 1.0.3 JAR. No provider classes enter signatures. */
public final class LibertyHelldiversCompat {
    public static final String VERSION = "1.0.3";
    public static final String SHA256 = "49c4ab153c50dab8766fca15ab7affcc14b1c9a8e7e52d58a9683c06ac4ceac0";
    public static final String SOURCE = "https://www.curseforge.com/minecraft/mc-mods/mcdiver/files/9307472";
    private static final Set<String> ACTORS = Set.of("stratagem_beacon", "ordnance", "eagle_jet", "orbital_laser", "machine_gun_sentry", "gatling_sentry", "guard_dog", "planted_flag", "supply_pod");
    private static final Set<String> CALLS = Set.of("ORBITAL_PRECISION_STRIKE", "ORBITAL_120MM_BARRAGE", "ORBITAL_380MM_BARRAGE", "ORBITAL_LASER", "EAGLE_STRAFING_RUN", "EAGLE_AIRSTRIKE", "EAGLE_500KG_BOMB", "SENTRY_MACHINE_GUN", "SENTRY_GATLING");
    private static volatile Method planted;
    private static volatile Class<?> stratagemType;

    public static boolean available() {
        if (ModList.get() == null || !ModList.get().isLoaded("mcdivers") || !ModList.get().getModContainerById("mcdivers")
                .orElseThrow().getModInfo().getVersion().toString().equals(VERSION)) return false;
        if (!ForgeRegistries.ENTITY_TYPES.containsKey(new ResourceLocation("mcdivers", "stratagem_beacon"))) return false;
        try {
            if (planted == null) {
                Class<?> type = Class.forName("com.shiroaki193.mcdivers.stratagem.Stratagem", false, LibertyHelldiversCompat.class.getClassLoader());
                Method method = Class.forName("com.shiroaki193.mcdivers.entity.StratagemBeaconEntity", false, LibertyHelldiversCompat.class.getClassLoader())
                        .getMethod("planted", Level.class, type, Vec3.class, Vec3.class, Entity.class);
                stratagemType = type; planted = method;
            }
            return true;
        } catch (ReflectiveOperationException | LinkageError error) { return false; }
    }
    public static String status() { return available() ? "MCDivers 1.0.3 native beacons, airstrikes and sentries ready."
            : "Optional native support requires MCDivers Forge 1.20.1 version 1.0.3 (file 9307472)."; }
    public static boolean supports(String nativeId) { return CALLS.contains(nativeId) && available(); }
    /** Returns an unspawned real provider entity so the caller can mark ownership before entity-join events. */
    public static Entity beacon(ServerPlayer owner, String nativeId, Vec3 target, Vec3 direction) {
        if (owner == null || !supports(nativeId)) return null;
        try {
            Object definition = null;
            for (Object constant : stratagemType.getEnumConstants()) if (((Enum<?>)constant).name().equals(nativeId)) definition=constant;
            if (definition == null) return null;
            Object result = planted.invoke(null, owner.serverLevel(), definition, target, direction, owner);
            return result instanceof Projectile projectile && projectile.getOwner() == owner && actor(projectile) ? projectile : null;
        } catch (ReflectiveOperationException | RuntimeException | LinkageError error) { return null; }
    }
    public static boolean actor(Entity entity) {
        if (entity == null || !entity.getClass().getName().startsWith("com.shiroaki193.mcdivers.entity.")) return false;
        ResourceLocation id=ForgeRegistries.ENTITY_TYPES.getKey(entity.getType());
        return id!=null && id.getNamespace().equals("mcdivers") && ACTORS.contains(id.getPath());
    }
    /**
     * Most projectiles expose vanilla getOwner. The pinned provider keeps Eagle/laser/sentry ownership
     * private; exact owner/ownerId fields are read, never changed. No persistent tag can claim ownership.
     */
    public static UUID ownerId(Entity entity) {
        if (!actor(entity) || !available()) return null;
        if (entity instanceof Projectile projectile && projectile.getOwner()!=null) return projectile.getOwner().getUUID();
        String fieldName=entity.getClass().getSimpleName().equals("SentryEntity") ? "ownerId" : "owner";
        try {
            Field field=entity.getClass().getDeclaredField(fieldName); field.setAccessible(true);
            Object value=field.get(entity);
            return value instanceof Entity owner ? owner.getUUID() : value instanceof UUID id ? id : null;
        } catch (ReflectiveOperationException | RuntimeException error) { return null; }
    }

    public static boolean terrainSafeTaczApi() {
        if(ModList.get()==null||!ModList.get().isLoaded("tacz"))return false;
        String version=ModList.get().getModContainerById("tacz").orElseThrow().getModInfo().getVersion().toString();
        if(!version.equals("1.1.8-hotfix2"))return false;
        try {
            Class<?> type=tacz("entity.EntityKineticBullet");
            type.getDeclaredMethod("onHitEntity",tacz("util.TacHitResult"),Vec3.class,Vec3.class);
            type.getDeclaredMethod("onHitBlock",net.minecraft.world.phys.BlockHitResult.class,Vec3.class,Vec3.class);
            Field ignite=type.getDeclaredField("igniteBlock"),destroy=type.getDeclaredField("explosionDestroyBlock");
            return LibertyNativeBulletGuard.class.isAssignableFrom(type)&&ignite.getType()==boolean.class&&destroy.getType()==boolean.class
                    &&ignite.trySetAccessible()&&destroy.trySetAccessible();
        }
        catch(ReflectiveOperationException|LinkageError error){return false;}
    }
    /** Suppress only this owned native bullet's block fire and terrain destruction; keep native damage and visuals. */
    public static boolean protectTaczTerrain(Entity entity) {
        if (entity == null || !entity.getClass().getName().equals("com.tacz.guns.entity.EntityKineticBullet")) return true;
        if(!terrainSafeTaczApi())return false;
        try {
            for (String name : List.of("igniteBlock", "explosionDestroyBlock")) {
                Field field=entity.getClass().getDeclaredField(name);
                if (field.getType()!=boolean.class) return false;
                field.setAccessible(true); field.setBoolean(entity,false);
            }
            return true;
        } catch (ReflectiveOperationException | RuntimeException error) { return false; }
    }
    /** Preserve the provider's genuine entity-damaging explosion while skipping projectile-reactive blocks. */
    public static boolean nativeSafeBlockImpact(Projectile bullet,Vec3 point) {
        if(!protectTaczTerrain(bullet))return false;
        try {
            Class<?> type=bullet.getClass();Field explosive=type.getDeclaredField("explosion");explosive.setAccessible(true);
            if(!explosive.getBoolean(bullet))return true;
            Field damage=type.getDeclaredField("explosionDamage"),radius=type.getDeclaredField("explosionRadius"),knockback=type.getDeclaredField("explosionKnockback");
            damage.setAccessible(true);radius.setAccessible(true);knockback.setAccessible(true);
            tacz("util.ExplodeUtil").getMethod("createExplosion",Entity.class,Entity.class,float.class,float.class,boolean.class,boolean.class,Vec3.class)
                    .invoke(null,bullet.getOwner(),bullet,damage.getFloat(bullet),radius.getFloat(bullet),knockback.getBoolean(bullet),false,point);
            return true;
        }catch(ReflectiveOperationException|RuntimeException|LinkageError error){return false;}
    }

    public record WeaponLoadout(ItemStack gun, List<ItemStack> reserve, String problem) {
        public boolean ready() { return problem.isEmpty() && !gun.isEmpty(); }
    }
    /** Live index lookup only; field-panel rendering must not create or lend inventory stacks. */
    public static boolean supportWeaponAvailable(String id) {
        if(id==null||id.isBlank()||ModList.get()==null||!ModList.get().isLoaded("tacz"))return false;
        try {
            Class<?> api=tacz("api.TimelessAPI");
            Object index=((Optional<?>)api.getMethod("getCommonGunIndex",ResourceLocation.class).invoke(null,new ResourceLocation(id))).orElse(null);
            if(index==null)return false;
            Object data=index.getClass().getMethod("getGunData").invoke(index);
            int capacity=((Number)data.getClass().getMethod("getAmmoAmount").invoke(data)).intValue();
            List<?> modes=(List<?>)data.getClass().getMethod("getFireModeSet").invoke(data);
            ResourceLocation ammo=(ResourceLocation)data.getClass().getMethod("getAmmoId").invoke(data);
            return capacity>0&&!modes.isEmpty()&&((Optional<?>)api.getMethod("getCommonAmmoIndex",ResourceLocation.class).invoke(null,ammo)).isPresent();
        } catch(ReflectiveOperationException|RuntimeException|LinkageError error){return false;}
    }
    /** Build real support weapons from their exact live pack index; no guessed or renamed substitute IDs. */
    public static WeaponLoadout supportWeapon(String id, UUID owner) {
        if (id==null || id.isBlank() || !ModList.get().isLoaded("tacz")) return failed("TaCZ support definition is unavailable.");
        try {
            ResourceLocation gunId=new ResourceLocation(id);
            Class<?> api=tacz("api.TimelessAPI");
            Object index=((Optional<?>)api.getMethod("getCommonGunIndex",ResourceLocation.class).invoke(null,gunId)).orElse(null);
            if(index==null)return failed("Missing live TaCZ gun " + id);
            Object data=index.getClass().getMethod("getGunData").invoke(index);
            ResourceLocation ammo=(ResourceLocation)data.getClass().getMethod("getAmmoId").invoke(data);
            if(((Optional<?>)api.getMethod("getCommonAmmoIndex",ResourceLocation.class).invoke(null,ammo)).isEmpty())return failed("Missing magazines for " + id);
            int capacity=((Number)data.getClass().getMethod("getAmmoAmount").invoke(data)).intValue();
            List<?> modes=(List<?>)data.getClass().getMethod("getFireModeSet").invoke(data);
            String bolt=((Enum<?>)data.getClass().getMethod("getBolt").invoke(data)).name();
            // The pack's seven heat-fed lasers use Integer.MAX_VALUE as their real native
            // magazine sentinel. Keep that definition intact; only reserve item counts are capped.
            if(capacity<1||modes.isEmpty())return failed("Unsupported support weapon definition " + id);
            Class<?> builderType=tacz("api.item.builder.GunItemBuilder");Object builder=builderType.getMethod("create").invoke(null);
            builderType.getMethod("setId",ResourceLocation.class).invoke(builder,gunId);
            boolean chamber=bolt.equals("CLOSED_BOLT");
            builderType.getMethod("setAmmoCount",int.class).invoke(builder,capacity-(chamber?1:0));
            builderType.getMethod("setAmmoInBarrel",boolean.class).invoke(builder,chamber);
            builderType.getMethod("setFireMode",tacz("api.item.gun.FireMode")).invoke(builder,modes.get(0));
            ItemStack gun=(ItemStack)builderType.getMethod("build").invoke(builder);
            if(gun.isEmpty())return failed("Provider returned no support weapon.");
            // Pinned GunItemBuilder.build() does not apply setHeatData(), unlike forceBuild().
            // Initialize actual provider heat NBT through IGun, without changing its script or capacity.
            if((Boolean)data.getClass().getMethod("hasHeatData").invoke(data))
                tacz("api.item.IGun").getMethod("setHeatAmount",ItemStack.class,float.class).invoke(gun.getItem(),gun,0F);
            Object reload=data.getClass().getMethod("getReloadData").invoke(data);
            String feed=((Enum<?>)reload.getClass().getMethod("getType").invoke(reload)).name();
            Class<?> ammoBuilder=tacz("api.item.builder.AmmoItemBuilder");Object ab=ammoBuilder.getMethod("create").invoke(null);
            ammoBuilder.getMethod("setId",ResourceLocation.class).invoke(ab,ammo);ammoBuilder.getMethod("setCount",int.class).invoke(ab,1);
            ItemStack round=(ItemStack)ammoBuilder.getMethod("build").invoke(ab);
            if(round.isEmpty())return failed("Provider returned no support ammunition.");
            int quantity=feed.equals("FUEL")?4:(int)Math.min(256L,(long)capacity*3L);
            List<ItemStack> reserves=new ArrayList<>();
            while(quantity>0){int amount=Math.min(quantity,round.getMaxStackSize());ItemStack stack=round.copy();stack.setCount(amount);LibertyTaczCompat.markLoan(stack,owner);reserves.add(stack);quantity-=amount;}
            LibertyTaczCompat.markLoan(gun,owner);gun.getOrCreateTag().putString("jetsetcraft_liberty_native_support",id);
            return new WeaponLoadout(gun,List.copyOf(reserves),"");
        } catch (ReflectiveOperationException | RuntimeException | LinkageError error) { return failed("The pinned TaCZ API could not create " + id); }
    }
    private static WeaponLoadout failed(String message) { return new WeaponLoadout(ItemStack.EMPTY,List.of(),message); }
    private static Class<?> tacz(String name)throws ClassNotFoundException{return Class.forName("com.tacz.guns."+name,false,LibertyHelldiversCompat.class.getClassLoader());}
    private LibertyHelldiversCompat() {}
}
