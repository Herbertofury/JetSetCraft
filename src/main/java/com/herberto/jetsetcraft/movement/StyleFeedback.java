package com.herberto.jetsetcraft.movement;

import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;

/** Lightweight Minecraft-native sound and particle punctuation for style actions. */
final class StyleFeedback {
    static void trick(ServerPlayer player, TrickCatalog.Trick trick, boolean boosted, boolean fresh) {
        ServerLevel level = player.serverLevel();
        level.sendParticles(boosted ? ParticleTypes.ELECTRIC_SPARK : ParticleTypes.CRIT,
                player.getX(), player.getY() + 0.85, player.getZ(), fresh ? 12 : 7,
                0.32, 0.24, 0.32, boosted ? 0.14 : 0.06);
        level.playSound(null, player.blockPosition(), SoundEvents.EXPERIENCE_ORB_PICKUP, SoundSource.PLAYERS,
                boosted ? 0.72f : 0.45f, 0.85f + trick.animationIndex() * 0.055f);
    }

    static void dance(ServerPlayer player, DanceCatalog.Move move, int cypherSize) {
        ServerLevel level = player.serverLevel();
        level.sendParticles(ParticleTypes.NOTE, player.getX(), player.getY() + 0.18, player.getZ(),
                5 + Math.min(12, cypherSize * 2), 0.48, 0.08, 0.48, 0.05);
        level.playSound(null, player.blockPosition(), SoundEvents.NOTE_BLOCK_HAT.value(), SoundSource.PLAYERS,
                0.38f + Math.min(0.35f, cypherSize * 0.045f), 0.72f + move.animationIndex() * 0.035f);
    }

    static void landing(ServerPlayer player, int grade) {
        ServerLevel level = player.serverLevel();
        level.sendParticles(grade == 3 ? ParticleTypes.ELECTRIC_SPARK : ParticleTypes.HAPPY_VILLAGER,
                player.getX(), player.getY() + 0.08, player.getZ(), 5 + grade * 4,
                0.38, 0.06, 0.38, 0.05);
        level.playSound(null, player.blockPosition(), SoundEvents.AMETHYST_BLOCK_CHIME, SoundSource.PLAYERS,
                0.35f + grade * 0.12f, 0.78f + grade * 0.16f);
    }

    private StyleFeedback() {}
}
