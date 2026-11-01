package com.herberto.jetsetcraft.rush;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/** Persistent, mode-only collection. It deliberately lives outside the temporary return journal. */
public final class RushAlbum {
    public static final String KEY = "jetsetcraft:RivalRushAlbum";
    private static final long LIMIT = Long.MAX_VALUE / 4L;
    private static final long[] THRESHOLDS = {1L, 5L, 15L, 40L, 100L, 250L, 600L};

    public record CollectionReward(long count, int tier, boolean newTier, int runXp, long score) {}
    public record Entry(String entityId, String gangId, long count, int tier, long nextThreshold) {}

    /** The manager must claim a defeated actor once before calling this method. */
    public static CollectionReward collect(ServerPlayer player, String entityId, String gangId) {
        if (player == null || ResourceLocation.tryParse(entityId == null ? "" : entityId) == null
                || ResourceLocation.tryParse(gangId == null ? "" : gangId) == null) {
            return new CollectionReward(0L, 0, false, 0, 0L);
        }
        CompoundTag persisted = persisted(player, true);
        CompoundTag album = persisted.getCompound(KEY);
        CompoundTag cards = album.getCompound("Cards");
        String key = entityId + "|" + gangId;
        CompoundTag card = cards.getCompound(key);
        long previous = clamp(card.getLong("Count"));
        long count = Math.min(LIMIT, previous + 1L);
        int tier = tierFor(count);
        boolean newTier = tier > tierFor(previous);
        card.putString("Entity", entityId);
        card.putString("Gang", gangId);
        card.putLong("Count", count);
        cards.put(key, card);
        album.putInt("Schema", 1);
        album.put("Cards", cards);
        album.putLong("Total", Math.min(LIMIT, clamp(album.getLong("Total")) + (count > previous ? 1L : 0L)));
        persisted.put(KEY, album);
        player.getPersistentData().put(Player.PERSISTED_NBT_TAG, persisted);
        int xp = 4 + 2 * tier + (newTier ? 12 * tier * tier : 0);
        long score = 10L + 5L * tier + (newTier ? 60L * tier * tier : 0L);
        return new CollectionReward(count, tier, newTier, xp, score);
    }

    public static long total(ServerPlayer player) {
        if (player == null) return 0L;
        return clamp(persisted(player, false).getCompound(KEY).getLong("Total"));
    }

    public static int uniqueCount(ServerPlayer player) {
        if (player == null) return 0;
        return persisted(player, false).getCompound(KEY).getCompound("Cards").getAllKeys().size();
    }

    /** Returns how many mode-only starter choices the collection has unlocked: 0, 1, 2, or 3. */
    public static int startingPerkChoices(ServerPlayer player) {
        long cards = total(player);
        return cards >= 150L ? 3 : cards >= 60L ? 2 : cards >= 20L ? 1 : 0;
    }

    /** Stable, read-only pagination; browsing an album never creates or mutates player data. */
    public static List<Entry> entries(ServerPlayer player, int offset, int limit) {
        if (player == null || limit <= 0) return List.of();
        CompoundTag cards = persisted(player, false).getCompound(KEY).getCompound("Cards");
        List<Entry> result = new ArrayList<>();
        for (String key : cards.getAllKeys()) {
            if (!cards.contains(key, Tag.TAG_COMPOUND)) continue;
            CompoundTag card = cards.getCompound(key);
            String entity = card.getString("Entity");
            String gang = card.getString("Gang");
            long count = clamp(card.getLong("Count"));
            if (count == 0L || ResourceLocation.tryParse(entity) == null || ResourceLocation.tryParse(gang) == null) continue;
            int tier = tierFor(count);
            result.add(new Entry(entity, gang, count, tier, nextThreshold(count)));
        }
        result.sort(Comparator.comparing(Entry::gangId).thenComparing(Entry::entityId));
        int from = Math.min(result.size(), Math.max(0, offset));
        int to = Math.min(result.size(), from + Math.min(100, limit));
        return List.copyOf(result.subList(from, to));
    }

    public static int tierFor(long count) {
        int tier = 0;
        for (long threshold : THRESHOLDS) {
            if (count < threshold) break;
            tier++;
        }
        return tier;
    }

    public static long nextThreshold(long count) {
        for (long threshold : THRESHOLDS) if (count < threshold) return threshold;
        return 0L;
    }

    public static List<Long> thresholds() {
        List<Long> result = new ArrayList<>(THRESHOLDS.length);
        for (long threshold : THRESHOLDS) result.add(threshold);
        return List.copyOf(result);
    }

    private static CompoundTag persisted(ServerPlayer player, boolean create) {
        CompoundTag data = player.getPersistentData();
        if (!data.contains(Player.PERSISTED_NBT_TAG, Tag.TAG_COMPOUND)) {
            CompoundTag tag = new CompoundTag();
            if (create) data.put(Player.PERSISTED_NBT_TAG, tag);
            return tag;
        }
        return data.getCompound(Player.PERSISTED_NBT_TAG);
    }

    private static long clamp(long count) { return Math.max(0L, Math.min(LIMIT, count)); }
    private RushAlbum() {}
}
