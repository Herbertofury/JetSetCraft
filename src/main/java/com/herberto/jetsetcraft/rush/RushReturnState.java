package com.herberto.jetsetcraft.rush;

import com.herberto.jetsetcraft.JetSetCraft;
import com.herberto.jetsetcraft.data.JetSetData;
import com.herberto.jetsetcraft.data.JetSetDataProvider;
import com.herberto.jetsetcraft.network.JetSetNetwork;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ClientboundSetExperiencePacket;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.GameType;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.HashSet;
import java.util.Set;

/** A per-player return journal, saved beside player state before the mode lends any items or moves the player. */
public final class RushReturnState {
    public static final String KEY = "jetsetcraft:RivalRushReturn";
    private static final int SCHEMA = 1;

    /** Refuses to overwrite a pending recovery. The caller must recover first instead of nesting sessions. */
    public static boolean capture(ServerPlayer player) {
        return capture(player, KEY);
    }

    /** Shared serialization with distinct mode ownership; the existing Rush key remains unchanged. */
    public static boolean capture(ServerPlayer player, String key) {
        if (!allowedKey(key) || player == null || !player.isAlive() || hasJournal(player)
                || hasJournal(player, "jetsetcraft:LibertyReturn")) return false;
        JetSetData ride = player.getCapability(JetSetDataProvider.CAPABILITY).resolve().orElse(null);
        if (ride == null || !finite(player.getX(), player.getY(), player.getZ())) return false;
        // Vanilla returns a container's carried stack before the snapshot. External container contents stay owned
        // by that container; no foreign menu or capability is serialized into this journal.
        player.closeContainer();
        CompoundTag snapshot = new CompoundTag();
        snapshot.putInt("Schema", SCHEMA);
        snapshot.putUUID("Owner", player.getUUID());
        snapshot.putString("Dimension", player.serverLevel().dimension().location().toString());
        snapshot.putDouble("X", player.getX());
        snapshot.putDouble("Y", player.getY());
        snapshot.putDouble("Z", player.getZ());
        snapshot.putFloat("Yaw", player.getYRot());
        snapshot.putFloat("Pitch", player.getXRot());
        snapshot.put("Inventory", player.getInventory().save(new ListTag()));
        snapshot.putInt("Selected", player.getInventory().selected);
        ItemStack cursor = player.containerMenu.getCarried();
        if (!cursor.isEmpty()) snapshot.put("Cursor", cursor.save(new CompoundTag()));
        snapshot.put("Ride", ride.save());
        snapshot.putInt("XpLevel", player.experienceLevel);
        snapshot.putInt("XpTotal", player.totalExperience);
        snapshot.putFloat("XpProgress", player.experienceProgress);
        snapshot.putFloat("Health", player.getHealth());
        snapshot.putFloat("Absorption", player.getAbsorptionAmount());
        CompoundTag food = new CompoundTag();
        player.getFoodData().addAdditionalSaveData(food);
        snapshot.put("Food", food);
        ListTag effects = new ListTag();
        for (MobEffectInstance effect : player.getActiveEffects()) effects.add(effect.save(new CompoundTag()));
        snapshot.put("Effects", effects);
        snapshot.putInt("GameMode", player.gameMode.getGameModeForPlayer().getId());
        CompoundTag abilities = new CompoundTag();
        player.getAbilities().addSaveData(abilities);
        snapshot.put("Abilities", abilities);
        snapshot.putInt("Fire", player.getRemainingFireTicks());
        snapshot.putInt("Air", player.getAirSupply());
        CompoundTag persisted = persisted(player, true);
        persisted.put(key, snapshot);
        player.getPersistentData().put(Player.PERSISTED_NBT_TAG, persisted);
        return true;
    }

    /**
     * Restores only the captured vanilla/ride state. The album and all unrelated persisted data survive.
     * The journal is removed last, so an exception leaves an idempotent recovery source available on login.
     */
    public static boolean restore(ServerPlayer player) {
        return restore(player, KEY);
    }

    public static boolean restore(ServerPlayer player, String key) {
        if (!allowedKey(key) || player == null || !hasJournal(player, key)) return false;
        CompoundTag snapshot = journal(player, key);
        if (!validJournal(snapshot, player)) {
            JetSetCraft.LOGGER.error("Retaining invalid Rival Rush return journal for {}", player.getUUID());
            return false;
        }
        JetSetData ride = player.getCapability(JetSetDataProvider.CAPABILITY).resolve().orElse(null);
        if (ride == null || player.getServer() == null) return false;
        ResourceLocation dimensionId = ResourceLocation.tryParse(snapshot.getString("Dimension"));
        ServerLevel destination = dimensionId == null ? null
                : player.getServer().getLevel(ResourceKey.create(Registries.DIMENSION, dimensionId));
        boolean fallback = destination == null;
        if (destination == null) destination = player.getServer().overworld();
        if (destination == null) return false;
        double x = snapshot.getDouble("X");
        double y = snapshot.getDouble("Y");
        double z = snapshot.getDouble("Z");
        if (Math.abs(x) > 29_999_984.0D || Math.abs(z) > 29_999_984.0D || Math.abs(y) > 20_000_000.0D) fallback = true;
        if (fallback) {
            BlockPos spawn = destination.getSharedSpawnPos();
            x = spawn.getX() + 0.5D;
            y = spawn.getY();
            z = spawn.getZ() + 0.5D;
        }
        float yaw = finiteFloat(snapshot.getFloat("Yaw"), 0.0F);
        float pitch = Math.max(-90.0F, Math.min(90.0F, finiteFloat(snapshot.getFloat("Pitch"), 0.0F)));
        try {
            player.closeContainer();
            player.stopRiding();
            // FakePlayers can exercise a same-level restoration without manufacturing a client connection.
            if (destination == player.serverLevel() && !JetSetNetwork.canSync(player)) player.moveTo(x, y, z, yaw, pitch);
            else player.teleportTo(destination, x, y, z, yaw, pitch);
            player.setGameMode(GameType.byId(snapshot.getInt("GameMode")));
            player.getAbilities().loadSaveData(snapshot.getCompound("Abilities"));
            player.getInventory().load(snapshot.getList("Inventory", Tag.TAG_COMPOUND));
            player.getInventory().selected = Math.max(0, Math.min(8, snapshot.getInt("Selected")));
            player.inventoryMenu.setCarried(snapshot.contains("Cursor", Tag.TAG_COMPOUND)
                    ? ItemStack.of(snapshot.getCompound("Cursor")) : ItemStack.EMPTY);
            ride.load(snapshot.getCompound("Ride"));
            player.experienceLevel = Math.max(0, snapshot.getInt("XpLevel"));
            player.totalExperience = Math.max(0, snapshot.getInt("XpTotal"));
            player.experienceProgress = Math.max(0.0F, Math.min(1.0F, finiteFloat(snapshot.getFloat("XpProgress"), 0.0F)));
            player.getFoodData().readAdditionalSaveData(snapshot.getCompound("Food"));
            player.removeAllEffects();
            ListTag effects = snapshot.getList("Effects", Tag.TAG_COMPOUND);
            for (int index = 0; index < effects.size(); index++) {
                MobEffectInstance effect = MobEffectInstance.load(effects.getCompound(index));
                if (effect != null) player.addEffect(effect);
            }
            player.setHealth(Math.max(0.01F, Math.min(player.getMaxHealth(), finiteFloat(snapshot.getFloat("Health"), 20.0F))));
            player.setAbsorptionAmount(Math.max(0.0F, finiteFloat(snapshot.getFloat("Absorption"), 0.0F)));
            player.setRemainingFireTicks(snapshot.getInt("Fire"));
            player.setAirSupply(snapshot.getInt("Air"));
            player.fallDistance = 0.0F;
            player.setDeltaMovement(Vec3.ZERO);
            player.hurtMarked = true;
            player.getInventory().setChanged();
            if (JetSetNetwork.canSync(player)) {
                player.onUpdateAbilities();
                player.connection.send(new ClientboundSetExperiencePacket(player.experienceProgress,
                        player.totalExperience, player.experienceLevel));
                player.inventoryMenu.broadcastChanges();
                JetSetNetwork.sync(player, ride);
                if (fallback) player.sendSystemMessage(Component.literal(
                        "Your belongings were restored at the world spawn because your original return location was unavailable."));
            }
            persisted(player, false).remove(key);
            return true;
        } catch (RuntimeException error) {
            JetSetCraft.LOGGER.error("Rival Rush could not restore {} yet; retaining the return journal",
                    player.getUUID(), error);
            return false;
        }
    }

    public static boolean hasJournal(ServerPlayer player) {
        return hasJournal(player, KEY);
    }
    public static boolean hasJournal(ServerPlayer player, String key) {
        return allowedKey(key) && player != null && persisted(player, false).contains(key);
    }

    /** Defensive copy for recovery inspection and regression tests. */
    public static CompoundTag journal(ServerPlayer player) {
        return journal(player, KEY);
    }
    public static CompoundTag journal(ServerPlayer player, String key) {
        return !allowedKey(key) || player == null ? new CompoundTag() : persisted(player, false).getCompound(key).copy();
    }

    private static boolean allowedKey(String key) { return KEY.equals(key) || "jetsetcraft:LibertyReturn".equals(key); }

    private static CompoundTag persisted(ServerPlayer player, boolean create) {
        CompoundTag data = player.getPersistentData();
        if (!data.contains(Player.PERSISTED_NBT_TAG, Tag.TAG_COMPOUND)) {
            CompoundTag tag = new CompoundTag();
            if (create) data.put(Player.PERSISTED_NBT_TAG, tag);
            return tag;
        }
        return data.getCompound(Player.PERSISTED_NBT_TAG);
    }

    private static boolean finite(double x, double y, double z) {
        return Double.isFinite(x) && Double.isFinite(y) && Double.isFinite(z);
    }

    /** Refuse partial/corrupt journals before touching position, inventory, or survival state. */
    private static boolean validJournal(CompoundTag tag, ServerPlayer player) {
        if (!tag.contains("Schema", Tag.TAG_INT) || tag.getInt("Schema") != SCHEMA
                || !tag.hasUUID("Owner") || !player.getUUID().equals(tag.getUUID("Owner"))
                || !tag.contains("Dimension", Tag.TAG_STRING)
                || !tag.contains("Inventory", Tag.TAG_LIST) || !tag.contains("Effects", Tag.TAG_LIST)
                || !tag.contains("Ride", Tag.TAG_COMPOUND) || !tag.contains("Food", Tag.TAG_COMPOUND)
                || !tag.contains("Abilities", Tag.TAG_COMPOUND)) return false;
        for (String key : new String[]{"X", "Y", "Z"})
            if (!tag.contains(key, Tag.TAG_DOUBLE) || !Double.isFinite(tag.getDouble(key))) return false;
        for (String key : new String[]{"Yaw", "Pitch", "XpProgress", "Health", "Absorption"})
            if (!tag.contains(key, Tag.TAG_FLOAT) || !Float.isFinite(tag.getFloat(key))) return false;
        for (String key : new String[]{"Selected", "XpLevel", "XpTotal", "GameMode", "Fire", "Air"})
            if (!tag.contains(key, Tag.TAG_INT)) return false;
        if (tag.getInt("Selected") < 0 || tag.getInt("Selected") > 8
                || tag.getInt("XpLevel") < 0 || tag.getInt("XpTotal") < 0
                || tag.getFloat("XpProgress") < 0.0F || tag.getFloat("XpProgress") > 1.0F
                || tag.getFloat("Health") <= 0.0F || tag.getFloat("Absorption") < 0.0F
                || tag.getInt("GameMode") < 0 || tag.getInt("GameMode") > 3) return false;
        ListTag inventory = (ListTag) tag.get("Inventory");
        ListTag effects = (ListTag) tag.get("Effects");
        if ((!inventory.isEmpty() && inventory.getElementType() != Tag.TAG_COMPOUND)
                || (!effects.isEmpty() && effects.getElementType() != Tag.TAG_COMPOUND)) return false;
        Set<Integer> occupied = new HashSet<>();
        for (int index = 0; index < inventory.size(); index++) {
            CompoundTag stack = inventory.getCompound(index);
            if (!stack.contains("Slot", Tag.TAG_BYTE) || !validStack(stack)) return false;
            int slot = stack.getByte("Slot") & 255;
            boolean validSlot = slot < player.getInventory().items.size()
                    || slot >= 100 && slot < 100 + player.getInventory().armor.size()
                    || slot >= 150 && slot < 150 + player.getInventory().offhand.size();
            if (!validSlot || !occupied.add(slot)) return false;
        }
        if (tag.contains("Cursor") && (!tag.contains("Cursor", Tag.TAG_COMPOUND)
                || !validStack(tag.getCompound("Cursor")))) return false;
        CompoundTag ride = tag.getCompound("Ride");
        if (ride.contains("RideGear") && (!ride.contains("RideGear", Tag.TAG_COMPOUND)
                || !validStack(ride.getCompound("RideGear")))) return false;
        CompoundTag food = tag.getCompound("Food");
        if (!food.contains("foodLevel", Tag.TAG_INT) || !food.contains("foodTickTimer", Tag.TAG_INT)
                || !food.contains("foodSaturationLevel", Tag.TAG_FLOAT)
                || !food.contains("foodExhaustionLevel", Tag.TAG_FLOAT)
                || !Float.isFinite(food.getFloat("foodSaturationLevel"))
                || !Float.isFinite(food.getFloat("foodExhaustionLevel"))) return false;
        CompoundTag abilityRoot = tag.getCompound("Abilities");
        if (!abilityRoot.contains("abilities", Tag.TAG_COMPOUND)) return false;
        CompoundTag abilities = abilityRoot.getCompound("abilities");
        for (String key : new String[]{"invulnerable", "flying", "mayfly", "instabuild", "mayBuild"})
            if (!abilities.contains(key, Tag.TAG_BYTE)) return false;
        return abilities.contains("flySpeed", Tag.TAG_FLOAT) && abilities.contains("walkSpeed", Tag.TAG_FLOAT)
                && Float.isFinite(abilities.getFloat("flySpeed")) && Float.isFinite(abilities.getFloat("walkSpeed"));
    }

    private static boolean validStack(CompoundTag tag) {
        if (!tag.contains("id", Tag.TAG_STRING) || !tag.contains("Count", Tag.TAG_BYTE) || tag.getByte("Count") <= 0) return false;
        ResourceLocation item = ResourceLocation.tryParse(tag.getString("id"));
        return item != null && ForgeRegistries.ITEMS.containsKey(item) && !item.equals(ResourceLocation.tryParse("minecraft:air"));
    }

    private static float finiteFloat(float value, float fallback) { return Float.isFinite(value) ? value : fallback; }
    private RushReturnState() {}
}
