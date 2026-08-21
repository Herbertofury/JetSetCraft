package com.herberto.jetsetcraft.movement;

import com.herberto.jetsetcraft.config.JetSetConfig;
import com.herberto.jetsetcraft.data.JetSetData;
import com.herberto.jetsetcraft.data.JetSetDataProvider;
import com.herberto.jetsetcraft.network.InputFlags;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.phys.Vec3;

/** Server-authoritative street-dance and multiplayer cypher loop. */
final class DanceSystem {
    static boolean tick(ServerPlayer player, JetSetData data) {
        boolean startedOrChanged = false;
        if (data.justPressed(InputFlags.DANCE)) {
            if (!JetSetConfig.SERVER.allowDancing.get() || data.pressed(InputFlags.SNEAK)) {
                data.setDancing(false);
            } else if (player.onGround() && !player.isPassenger() && !player.isFallFlying()
                    && !player.isInWater() && !player.isInLava() && !player.isUsingItem() && !player.swinging) {
                boolean wasDancing = data.dancing();
                DanceStyle current = data.danceStyle();
                boolean neutral = Math.hypot(data.inputForward(), data.inputStrafe()) <= 0.25
                        && !data.pressed(InputFlags.MANUAL) && !data.pressed(InputFlags.BRAKE);
                DanceStyle selected = DanceStyle.select(current, data.inputForward(), data.inputStrafe(),
                        data.pressed(InputFlags.MANUAL), data.pressed(InputFlags.BRAKE));
                if (!wasDancing && neutral) {
                    selected = DanceStyle.BREAKING;
                } else if (wasDancing && neutral) {
                    selected = DanceStyle.values()[(current.id() + 1) % DanceStyle.values().length];
                }
                if (!wasDancing) data.setDanceChain(0);
                data.setDanceStyle(selected.id());
                DanceCatalog.Move move = DanceCatalog.select(selected, data.danceChain(),
                        data.inputForward(), data.inputStrafe());
                data.setDanceMoveId(move.id());
                data.setDancing(true);
                data.setDanceTicks(0);
                data.setCypherSize(1);
                data.setGroundStunt(false);
                data.setBoostTrick(false);
                data.setTrickTicks(0);
                StyleFeedback.dance(player, move, 1);
                startedOrChanged = true;
            }
        }

        if (!data.dancing()) return false;
        boolean actionCancel = data.pressed(InputFlags.JUMP) || data.pressed(InputFlags.BOOST)
                || data.pressed(InputFlags.GRIND) || data.pressed(InputFlags.TRICK)
                || player.isUsingItem() || player.swinging;
        boolean movementCancel = !startedOrChanged && Math.hypot(data.inputForward(), data.inputStrafe()) > 0.22;
        if (!player.onGround() || player.isPassenger() || player.isFallFlying()
                || player.isInWater() || player.isInLava() || actionCancel || movementCancel) {
            data.setDancing(false);
            return false;
        }

        data.resetTransientRideState();
        data.setMomentum(0.0);
        Vec3 velocity = player.getDeltaMovement();
        player.setDeltaMovement(velocity.x * 0.32, velocity.y, velocity.z * 0.32);
        player.hurtMarked = true;
        data.setDanceTicks(data.danceTicks() + 1);

        if (data.danceTicks() == 1 || data.danceTicks() % 10 == 0) updateCypher(player, data);

        DanceCatalog.Move currentMove = DanceCatalog.byId(data.danceMoveId());
        if (data.danceTicks() >= currentMove.duration()) {
            int crew = Math.max(0, data.cypherSize() - 1);
            long bit = 1L << currentMove.id();
            boolean fresh = (data.uniqueDanceMask() & bit) == 0L;
            data.setUniqueDanceMask(data.uniqueDanceMask() | bit);
            float variety = 1.0f + Math.min(0.28f, Long.bitCount(data.uniqueDanceMask()) * 0.02f);
            int points = Math.round((currentMove.basePoints() + crew * 24) * variety * (fresh ? 1.12f : 1.0f));
            TrickCombo.addStyle(data, points, currentMove.multiplierGain() + crew * 0.007f + (fresh ? 0.025f : 0.0f));
            data.setFlow(data.flow() + 4.0f + crew * 1.35f + (fresh ? 1.5f : 0.0f));
            data.setBoost(data.boost() + (currentMove.boostReward() + crew * 1.1f)
                    * JetSetConfig.SERVER.styleBoostScale.get().floatValue());
            data.setDanceChain(data.danceChain() + 1);
            DanceCatalog.Move next = DanceCatalog.select(data.danceStyle(), data.danceChain(),
                    data.inputForward(), data.inputStrafe());
            data.setDanceMoveId(next.id());
            data.setDanceTicks(0);
            StyleFeedback.dance(player, next, data.cypherSize());
        }
        return true;
    }

    private static void updateCypher(ServerPlayer player, JetSetData data) {
        int cypher = 1;
        if (JetSetConfig.SERVER.enableCyphers.get()) {
            double radius = JetSetConfig.SERVER.cypherRadius.get();
            for (ServerPlayer other : player.serverLevel().getEntitiesOfClass(ServerPlayer.class,
                    player.getBoundingBox().inflate(radius), candidate -> candidate != player && !candidate.isSpectator())) {
                boolean dancing = other.getCapability(JetSetDataProvider.CAPABILITY).resolve()
                        .map(JetSetData::dancing).orElse(false);
                if (dancing) cypher++;
            }
        }
        data.setCypherSize(cypher);
    }

    private DanceSystem() {}
}
