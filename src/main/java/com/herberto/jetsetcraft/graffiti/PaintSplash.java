package com.herberto.jetsetcraft.graffiti;

import com.herberto.jetsetcraft.config.JetSetConfig;
import com.herberto.jetsetcraft.entity.GraffitiEntity;
import com.herberto.jetsetcraft.registry.ModEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import org.joml.Vector3f;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Server-authoritative, bounded surface-paint engine based on Street Art's Fibonacci-sphere exposure pass.
 * JetSetCraft stores the result as removable decals, preserving blocks and remaining compatible with 1.20.1.
 */
public final class PaintSplash {
    private static final double GOLDEN_RATIO = 1.61803398875D;
    private static final int BALLOON_RAYS = 1000;
    private static final int SPRAY_RAYS = 240;

    public static int throwBalloon(ServerLevel level, Entity source, Player owner, Vec3 origin, PaintColor color) {
        int placed = castAndPlace(level, source, owner, origin, color, 3.0D, BALLOON_RAYS, 18);
        playSplash(level, origin, color, 32, 0.55D);
        return placed;
    }

    public static int freeSpray(ServerLevel level, Player player, BlockHitResult clicked, PaintColor color,
                                ItemStack sprayCan) {
        Vec3 origin = clicked.getLocation().add(Vec3.atLowerCornerOf(clicked.getDirection().getNormal()).scale(0.14D));
        int placed = castAndPlace(level, player, player, origin, color, 1.35D, SPRAY_RAYS, 7);
        playSplash(level, clicked.getLocation(), color, 10, 0.16D);
        level.playSound(null, clicked.getBlockPos(), SoundEvents.FIRE_EXTINGUISH, SoundSource.PLAYERS,
                0.22F, 1.55F + level.random.nextFloat() * 0.16F);
        return placed;
    }

    static int castAndPlace(ServerLevel level, Entity source, Player owner, Vec3 origin, PaintColor color,
                            double range, int rays, int maxPatches) {
        if (!JetSetConfig.SERVER.allowGraffiti.get() || source == null || rays <= 0 || maxPatches <= 0) return 0;
        Map<FaceKey, Integer> exposure = collectExposure(level, source, origin, range, rays);
        List<Map.Entry<FaceKey, Integer>> candidates = new ArrayList<>(exposure.entrySet());
        candidates.sort(Map.Entry.<FaceKey, Integer>comparingByValue(Comparator.reverseOrder())
                .thenComparingLong(entry -> entry.getKey().pos().asLong())
                .thenComparingInt(entry -> entry.getKey().face().get3DDataValue()));
        int strongest = candidates.isEmpty() ? 1 : Math.max(1, candidates.get(0).getValue());
        int placed = 0;
        for (Map.Entry<FaceKey, Integer> candidate : candidates) {
            if (placed >= maxPatches) break;
            FaceKey key = candidate.getKey();
            BlockPos pos = key.pos();
            Direction face = key.face();
            if (!level.hasChunk(pos.getX() >> 4, pos.getZ() >> 4) || level.getBlockState(pos).isAir()
                    || !level.getBlockState(pos).isFaceSturdy(level, pos, face)) continue;
            if (owner != null && (!level.mayInteract(owner, pos)
                    || !owner.mayUseItemAt(pos, face, ItemStack.EMPTY))) continue;

            float intensity = Math.max(0.18F, Math.min(1.0F, candidate.getValue() / (float) strongest));
            String pattern = createSplatPattern(color, intensity,
                    pos.asLong() ^ ((long) face.get3DDataValue() << 56) ^ level.getSeed());
            if (pattern.isEmpty()) continue;
            GraffitiEntity decal = new GraffitiEntity(ModEntities.GRAFFITI.get(), level);
            decal.configureSplash(pos, face, pattern, 0.62F + intensity * 0.40F);

            AABB samePatch = new AABB(decal.position(), decal.position()).inflate(0.18D);
            List<GraffitiEntity> replaced = level.getEntitiesOfClass(GraffitiEntity.class, samePatch,
                    existing -> existing.getFace() == face && pos.equals(existing.getSupportPos()));
            ChunkPos chunk = new ChunkPos(pos);
            AABB chunkBounds = new AABB(chunk.getMinBlockX(), level.getMinBuildHeight(), chunk.getMinBlockZ(),
                    chunk.getMaxBlockX() + 1, level.getMaxBuildHeight(), chunk.getMaxBlockZ() + 1);
            int outsidePatch = level.getEntitiesOfClass(GraffitiEntity.class, chunkBounds).size() - replaced.size();
            if (outsidePatch >= JetSetConfig.SERVER.maxGraffitiPerChunk.get()) continue;
            if (level.addFreshEntity(decal)) {
                replaced.forEach(GraffitiEntity::discard);
                placed++;
            }
        }
        return placed;
    }

    /** Fibonacci-sphere ray coverage, kept public for deterministic headless acceptance tests. */
    public static Map<FaceKey, Integer> collectExposure(ServerLevel level, Entity source, Vec3 origin,
                                                        double range, int rays) {
        Map<FaceKey, Integer> exposure = new HashMap<>();
        for (int i = 0; i < rays; i++) {
            double theta = Math.PI * 2.0D * i / GOLDEN_RATIO;
            double phi = Math.acos(1.0D - 2.0D * (i + 0.5D) / rays);
            Vec3 direction = new Vec3(Math.cos(theta) * Math.sin(phi), Math.cos(phi),
                    Math.sin(theta) * Math.sin(phi));
            BlockHitResult hit = level.clip(new ClipContext(origin, origin.add(direction.scale(range)),
                    ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, source));
            if (hit.getType() == HitResult.Type.BLOCK) {
                exposure.merge(new FaceKey(hit.getBlockPos().immutable(), hit.getDirection()), 1, Integer::sum);
            }
        }
        return exposure;
    }

    /** Deterministic bounded splat canvas used by both projectiles and free-paint spray. */
    public static String createSplatPattern(PaintColor color, float intensity, long seed) {
        byte[] pixels = new byte[CustomGraffiti.PIXELS];
        double strength = Math.max(0.12D, Math.min(1.0D, intensity));
        for (int y = 0; y < CustomGraffiti.HEIGHT; y++) {
            for (int x = 0; x < CustomGraffiti.WIDTH; x++) {
                double nx = (x + 0.5D - CustomGraffiti.WIDTH * 0.5D) / (CustomGraffiti.WIDTH * 0.5D);
                double ny = (y + 0.5D - CustomGraffiti.HEIGHT * 0.5D) / (CustomGraffiti.HEIGHT * 0.5D);
                double radius = Math.sqrt(nx * nx + ny * ny);
                double noise = hash01(seed + x * 73428767L + y * 912931L) * 0.30D - 0.15D;
                double edge = 0.34D + strength * 0.48D + noise;
                boolean droplet = hash01(seed ^ (x * 1199540897363517985L + y * 101507243978917818L))
                        > 0.965D - strength * 0.025D;
                if (radius <= edge || droplet) pixels[y * CustomGraffiti.WIDTH + x] = (byte) color.paletteIndex();
            }
        }
        pixels[(CustomGraffiti.HEIGHT / 2) * CustomGraffiti.WIDTH + CustomGraffiti.WIDTH / 2]
                = (byte) color.paletteIndex();
        return CustomGraffiti.encode(pixels);
    }

    private static double hash01(long value) {
        long mixed = value;
        mixed ^= mixed >>> 33;
        mixed *= 0xff51afd7ed558ccdL;
        mixed ^= mixed >>> 33;
        mixed *= 0xc4ceb9fe1a85ec53L;
        mixed ^= mixed >>> 33;
        return (mixed >>> 11) * 0x1.0p-53;
    }

    private static void playSplash(ServerLevel level, Vec3 origin, PaintColor color, int particles, double spread) {
        Vector3f rgb = new Vector3f(((color.rgb() >>> 16) & 255) / 255.0F,
                ((color.rgb() >>> 8) & 255) / 255.0F, (color.rgb() & 255) / 255.0F);
        level.sendParticles(new DustParticleOptions(rgb, 1.0F), origin.x, origin.y, origin.z,
                particles, spread, spread, spread, 0.08D);
        level.playSound(null, BlockPos.containing(origin), SoundEvents.GENERIC_SPLASH,
                SoundSource.PLAYERS, 0.85F, 0.92F + level.random.nextFloat() * 0.18F);
    }

    public record FaceKey(BlockPos pos, Direction face) { }

    private PaintSplash() { }
}
