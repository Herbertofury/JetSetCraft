package com.herberto.jetsetcraft.movement;

import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockState;

import com.herberto.jetsetcraft.movement.VanillaWorldPhysics.*;

final class VanillaGrindPhysics {
    static GrindMaterialProfile grindMaterial(ServerPlayer player, GrindTarget target) {
        BlockPos pos = BlockPos.containing(target.point().x, target.point().y - 0.055, target.point().z);
        BlockState state = player.level().getBlockState(pos);
        if (state.isAir()) {
            pos = pos.below();
            state = player.level().getBlockState(pos);
        }
        SoundType sound = state.getSoundType(player.level(), pos, player);
        GrindMaterialKind kind;
        double cap;
        double retention;
        double gain;
        boolean sparks;
        float pitch;

        if (state.is(Blocks.ICE) || state.is(Blocks.PACKED_ICE) || state.is(Blocks.BLUE_ICE) || state.is(Blocks.FROSTED_ICE)) {
            kind = GrindMaterialKind.ICE; cap = 1.12; retention = 0.99982; gain = 0.0016; sparks = false; pitch = 1.42f;
        } else if (sound == SoundType.COPPER) {
            kind = GrindMaterialKind.COPPER; cap = 1.035; retention = 0.99952; gain = 0.0010; sparks = true; pitch = 1.22f;
        } else if (sound == SoundType.METAL || sound == SoundType.ANVIL || sound == SoundType.CHAIN || sound == SoundType.NETHERITE_BLOCK) {
            kind = GrindMaterialKind.METAL; cap = 1.055; retention = 0.99958; gain = 0.0012; sparks = true; pitch = 1.30f;
        } else if (sound == SoundType.GLASS) {
            kind = GrindMaterialKind.GLASS; cap = 1.045; retention = 0.99964; gain = 0.0007; sparks = false; pitch = 1.48f;
        } else if (state.is(BlockTags.LOGS) || state.is(BlockTags.PLANKS) || sound == SoundType.WOOD
                || sound == SoundType.BAMBOO_WOOD || sound == SoundType.NETHER_WOOD || sound == SoundType.CHERRY_WOOD) {
            kind = GrindMaterialKind.WOOD; cap = 0.955; retention = 0.99820; gain = 0.0002; sparks = false; pitch = 1.03f;
        } else if (sound == SoundType.SLIME_BLOCK) {
            kind = GrindMaterialKind.SLIME; cap = 0.91; retention = 0.9965; gain = 0.0; sparks = false; pitch = 1.10f;
        } else if (sound == SoundType.HONEY_BLOCK) {
            kind = GrindMaterialKind.HONEY; cap = 0.78; retention = 0.972; gain = 0.0; sparks = false; pitch = 0.82f;
        } else if (sound == SoundType.STONE || sound == SoundType.DEEPSLATE || sound == SoundType.DEEPSLATE_BRICKS
                || sound == SoundType.DEEPSLATE_TILES || sound == SoundType.POLISHED_DEEPSLATE || sound == SoundType.TUFF
                || sound == SoundType.NETHER_BRICKS || sound == SoundType.NETHERRACK) {
            kind = GrindMaterialKind.STONE; cap = 0.985; retention = 0.99885; gain = 0.00035; sparks = false; pitch = 0.94f;
        } else {
            kind = GrindMaterialKind.GENERIC; cap = 1.0; retention = 0.99920; gain = 0.00055; sparks = false; pitch = 1.0f;
        }
        return new GrindMaterialProfile(kind, cap, retention, gain, sparks, pitch, sound);
    }

    static void emitGrindFeedback(ServerPlayer player, GrindTarget target, GrindMaterialProfile material) {
        if (!(player.level() instanceof ServerLevel level)) return;
        if (material.sparks() && player.tickCount % 3 == 0) {
            level.sendParticles(ParticleTypes.ELECTRIC_SPARK, target.point().x, target.point().y + 0.03, target.point().z,
                    2, 0.055, 0.025, 0.055, 0.012);
        }
        if (player.tickCount % 5 == 0) {
            float volume = material.kind() == GrindMaterialKind.METAL || material.kind() == GrindMaterialKind.COPPER ? 0.34f : 0.24f;
            level.playSound(null, player.blockPosition(), material.sound().getStepSound(), SoundSource.PLAYERS, volume, material.soundPitch());
        }
    }

    /**
     * Preserve Minecraft's side-contact language while airborne. Honey becomes a controlled sticky
     * wall stall/slide and slime can rebound a rider without deleting the incoming combo/momentum.
     * These are deliberately contact driven instead of global movement modes.
     */

    private VanillaGrindPhysics() {}
}
