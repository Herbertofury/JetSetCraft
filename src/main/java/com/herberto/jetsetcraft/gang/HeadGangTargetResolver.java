package com.herberto.jetsetcraft.gang;

import com.herberto.jetsetcraft.JetSetCraft;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

/**
 * Resolves a mob-head-like item into the source mob that JetSetCraft should target.
 *
 * <p>The resolver is intentionally provider-agnostic: it never hard-links another mod's classes. It supports
 * vanilla heads, explicit NBT/datapack-adapter metadata, and conservative registry-name conventions used by
 * common head mods. Unknown or ambiguous heads fail closed instead of guessing the wrong gang.</p>
 */
public final class HeadGangTargetResolver {
    public static final String TARGET_ENTITY_KEY = JetSetCraft.MOD_ID + ":target_entity";
    public static final String TARGET_GANG_KEY = JetSetCraft.MOD_ID + ":target_gang";

    private static final Set<String> VANILLA_HEAD_PROVIDER_NAMESPACES = Set.of(
            "minecraft", "heads", "moremobheads", "mobsheads", "mobs_heads", "mobheads", "mob_heads",
            "alltheheads", "more_heads"
    );

    public record Target(ResourceLocation entityId, ResourceLocation gangId, ResolutionSource source) {}

    public enum ResolutionSource {
        EXPLICIT_METADATA,
        DATA_PACK_MAPPING,
        VANILLA,
        REGISTRY_CONVENTION
    }

    public static Optional<Target> resolve(ItemStack stack) {
        if (stack == null || stack.isEmpty()) return Optional.empty();

        Optional<Target> explicit = resolveExplicitMetadata(stack);
        if (explicit.isPresent()) return explicit;

        Optional<Target> mapped = resolveDataPackMapping(stack.getItem());
        if (mapped.isPresent()) return mapped;

        Optional<Target> vanilla = resolveVanilla(stack.getItem());
        if (vanilla.isPresent()) return vanilla;

        return resolveRegistryConvention(stack.getItem());
    }

    public static Optional<EntityType<?>> resolveEntityType(ItemStack stack) {
        return resolve(stack).map(Target::entityId).map(ForgeRegistries.ENTITY_TYPES::getValue)
                .filter(type -> type != null);
    }

    private static Optional<Target> resolveDataPackMapping(Item item) {
        ResourceLocation itemId = ForgeRegistries.ITEMS.getKey(item);
        return HeadTargetMappingRegistry.find(itemId).map(mapping -> new Target(mapping.entityId(),
                mapping.gangId() == null ? defaultGangId(mapping.entityId()) : mapping.gangId(),
                ResolutionSource.DATA_PACK_MAPPING));
    }

    private static Optional<Target> resolveVanilla(Item item) {
        ResourceLocation entityId = null;
        if (item == Items.ZOMBIE_HEAD) entityId = id("minecraft:zombie");
        else if (item == Items.SKELETON_SKULL) entityId = id("minecraft:skeleton");
        else if (item == Items.WITHER_SKELETON_SKULL) entityId = id("minecraft:wither_skeleton");
        else if (item == Items.CREEPER_HEAD) entityId = id("minecraft:creeper");
        else if (item == Items.PIGLIN_HEAD) entityId = id("minecraft:piglin");
        else if (item == Items.DRAGON_HEAD) entityId = id("minecraft:ender_dragon");
        if (entityId == null) return Optional.empty();
        return Optional.of(new Target(entityId, defaultGangId(entityId), ResolutionSource.VANILLA));
    }

    private static Optional<Target> resolveExplicitMetadata(ItemStack stack) {
        CompoundTag tag = stack.getTag();
        if (tag == null) return Optional.empty();

        ResourceLocation entityId = firstValidEntityId(
                readString(tag, TARGET_ENTITY_KEY),
                readString(tag, "JetSetCraftTargetEntity"),
                readNestedString(tag, "EntityTag", "id")
        );
        if (entityId == null) return Optional.empty();

        ResourceLocation gangId = firstValidResourceLocation(
                readString(tag, TARGET_GANG_KEY),
                readString(tag, "JetSetCraftTargetGang")
        );
        if (gangId == null) gangId = defaultGangId(entityId);
        return Optional.of(new Target(entityId, gangId, ResolutionSource.EXPLICIT_METADATA));
    }

    private static Optional<Target> resolveRegistryConvention(Item item) {
        ResourceLocation itemId = ForgeRegistries.ITEMS.getKey(item);
        if (itemId == null) return Optional.empty();

        String path = itemId.getPath().toLowerCase();
        if (!(path.endsWith("_head") || path.endsWith("_skull") || path.startsWith("head_")
                || path.startsWith("skull_"))) {
            return Optional.empty();
        }

        List<String> candidates = candidatePaths(path);
        List<String> namespaces = new ArrayList<>();
        namespaces.add(itemId.getNamespace());
        if (VANILLA_HEAD_PROVIDER_NAMESPACES.contains(itemId.getNamespace()) && !"minecraft".equals(itemId.getNamespace())) {
            namespaces.add("minecraft");
        }

        for (String namespace : namespaces) {
            for (String candidate : candidates) {
                ResourceLocation entityId = ResourceLocation.tryParse(namespace + ":" + candidate);
                if (isRegisteredEntity(entityId)) {
                    return Optional.of(new Target(entityId, defaultGangId(entityId), ResolutionSource.REGISTRY_CONVENTION));
                }
            }
        }
        return Optional.empty();
    }

    private static List<String> candidatePaths(String originalPath) {
        String path = originalPath;
        if (path.endsWith("_head")) path = path.substring(0, path.length() - 5);
        else if (path.endsWith("_skull")) path = path.substring(0, path.length() - 6);
        if (path.startsWith("head_")) path = path.substring(5);
        else if (path.startsWith("skull_")) path = path.substring(6);

        LinkedHashSet<String> candidates = new LinkedHashSet<>();
        candidates.add(path);

        String current = path;
        while (current.contains("_")) {
            current = current.substring(0, current.lastIndexOf('_'));
            candidates.add(current);
        }
        return List.copyOf(candidates);
    }

    public static ResourceLocation defaultGangId(ResourceLocation entityId) {
        return GangRegistry.gangIdForEntity(entityId);
    }

    private static ResourceLocation firstValidEntityId(String... values) {
        for (String value : values) {
            ResourceLocation id = ResourceLocation.tryParse(value);
            if (isRegisteredEntity(id)) return id;
        }
        return null;
    }

    private static ResourceLocation firstValidResourceLocation(String... values) {
        for (String value : values) {
            ResourceLocation id = ResourceLocation.tryParse(value);
            if (id != null) return id;
        }
        return null;
    }

    private static boolean isRegisteredEntity(ResourceLocation id) {
        return id != null && ForgeRegistries.ENTITY_TYPES.containsKey(id);
    }

    private static String readString(CompoundTag tag, String key) {
        return tag.contains(key, Tag.TAG_STRING) ? tag.getString(key) : "";
    }

    private static String readNestedString(CompoundTag tag, String parentKey, String childKey) {
        if (!tag.contains(parentKey, Tag.TAG_COMPOUND)) return "";
        CompoundTag nested = tag.getCompound(parentKey);
        return nested.contains(childKey, Tag.TAG_STRING) ? nested.getString(childKey) : "";
    }

    private static ResourceLocation id(String value) {
        ResourceLocation id = ResourceLocation.tryParse(value);
        if (id == null) throw new IllegalArgumentException("Invalid built-in resource location: " + value);
        return id;
    }

    private HeadGangTargetResolver() {}
}
