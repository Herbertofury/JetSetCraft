package com.herberto.jetsetcraft.movement;

import com.herberto.jetsetcraft.config.JetSetConfig;
import com.herberto.jetsetcraft.data.JetSetData;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.phys.Vec3;

import com.herberto.jetsetcraft.movement.VanillaWorldPhysics.*;

final class VanillaRailPhysics {
    static void applyGroundRailInteractions(ServerPlayer player, JetSetData data, Surface surface) {
        if (!JetSetConfig.SERVER.enableVanillaWorldPhysics.get()) return;
        BlockState state = surface.state();
        Block block = state.getBlock();
        long posKey = surface.pos().asLong();
        boolean powered = state.hasProperty(BlockStateProperties.POWERED) && state.getValue(BlockStateProperties.POWERED);

        if (block == Blocks.DETECTOR_RAIL) {
            if (data.lastDetectorRailPos() != posKey) {
                data.setLastDetectorRailPos(posKey);
                pulseDetector(player, surface.pos(), state);
            }
        } else {
            data.setLastDetectorRailPos(Long.MIN_VALUE);
        }

        if (block == Blocks.ACTIVATOR_RAIL && powered) {
            if (data.lastActivatorRailPos() != posKey) {
                data.setLastActivatorRailPos(posKey);
                Vec3 v = player.getDeltaMovement();
                player.setDeltaMovement(v.x, Math.max(v.y, 0.27 * VanillaImpulsePhysics.jumpMultiplier(player)), v.z);
                player.hurtMarked = true;
                data.setComboGrace(Math.max(data.comboGrace(), 58));
            }
        } else {
            data.setLastActivatorRailPos(Long.MIN_VALUE);
        }
    }

    /** Apply block-specific landing behavior while preserving incoming horizontal momentum. */
    static RailEffect applyVanillaRail(ServerPlayer player, JetSetData data, GrindTarget target, double speed) {
        if (!JetSetConfig.SERVER.enableVanillaWorldPhysics.get() || !target.kind().rail()) return new RailEffect(speed, false);
        Surface surface = VanillaSurfacePhysics.at(player, target.point());
        BlockState state = surface.state();
        Block block = state.getBlock();
        boolean powered = state.hasProperty(BlockStateProperties.POWERED) && state.getValue(BlockStateProperties.POWERED);

        if (block == Blocks.POWERED_RAIL) {
            if (powered) {
                double cap = data.style().boostCap() * JetSetConfig.SERVER.speedScale.get() * 1.32;
                if (speed < cap) speed = Math.min(cap, speed + JetSetConfig.SERVER.poweredRailBoostPerTick.get());
            } else {
                speed *= JetSetConfig.SERVER.unpoweredRailRetention.get();
            }
        }

        long posKey = surface.pos().asLong();
        if (block == Blocks.DETECTOR_RAIL) {
            if (data.lastDetectorRailPos() != posKey) {
                data.setLastDetectorRailPos(posKey);
                pulseDetector(player, surface.pos(), state);
            }
        } else {
            data.setLastDetectorRailPos(Long.MIN_VALUE);
        }

        if (block == Blocks.ACTIVATOR_RAIL && powered) {
            if (data.lastActivatorRailPos() != posKey) {
                data.setLastActivatorRailPos(posKey);
                return new RailEffect(speed, true);
            }
        } else {
            data.setLastActivatorRailPos(Long.MIN_VALUE);
        }
        return new RailEffect(speed, false);
    }

    static void pulseDetector(ServerPlayer player, BlockPos pos, BlockState state) {
        if (!(player.level() instanceof ServerLevel level) || !state.hasProperty(BlockStateProperties.POWERED)) return;
        if (!state.getValue(BlockStateProperties.POWERED)) {
            BlockState powered = state.setValue(BlockStateProperties.POWERED, true);
            level.setBlock(pos, powered, 3);
            level.updateNeighborsAt(pos, powered.getBlock());
            level.updateNeighborsAt(pos.below(), powered.getBlock());
            level.scheduleTick(pos, powered.getBlock(), 8);
        }
    }

    private VanillaRailPhysics() {}
}
