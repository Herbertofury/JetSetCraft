package com.herberto.jetsetcraft.movement;

import com.herberto.jetsetcraft.config.JetSetConfig;
import com.herberto.jetsetcraft.data.JetSetData;
import com.herberto.jetsetcraft.network.InputFlags;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.phys.Vec3;

final class TrickCombo {

    static void handleTricksAndCombo(ServerPlayer player, JetSetData data, boolean grounded) {
        if (data.justPressed(InputFlags.TRICK) && data.grinding() && JetSetConfig.SERVER.allowRailTricks.get()) {
            int next = (data.trickIndex() + trickFromInput(data.inputForward(), data.inputStrafe()) + 1) % 12;
            data.setTrickIndex(next);
            data.setTrickTicks(14);
            int base = data.grindKind().rail() ? 245 : 190;
            addStyle(data, base + next * 13, data.grindKind().rail() ? 0.22f : 0.17f);
            if (data.grindKind().rail()) data.setBoost(data.boost() + 2.5f);
        } else if (data.justPressed(InputFlags.TRICK) && !grounded && !data.grinding() && data.airTicks() >= 2) {
            int next = (data.trickIndex() + trickFromInput(data.inputForward(), data.inputStrafe()) + 1) % 12;
            data.setTrickIndex(next);
            data.setTrickTicks(18);
            addStyle(data, 180 + next * 18, 0.18f);
            Vec3 v = player.getDeltaMovement();
            player.setDeltaMovement(v.x, Math.max(v.y, -0.08) + 0.035, v.z);
            player.hurtMarked = true;
        }
        if (data.trickTicks() > 0) data.setTrickTicks(data.trickTicks() - 1);
        if (!grounded) data.setAirTicks(data.airTicks() + 1);
        else if (!data.wasGrounded()) {
            if (data.airTicks() >= 5 && data.comboScore() > 0) {
                data.setBoost(data.boost() + Math.min(24f, 4f + data.comboScore() / 350f));
                data.setComboGrace(Math.max(data.comboGrace(), 70));
            }
            data.setAirTicks(0);
        }
        if (data.manual()) {
            data.setComboGrace(MovementTuning.COMBO_GRACE_TICKS);
            if (player.tickCount % 6 == 0) addStyle(data, 14, 0.01f);
        } else if (!data.grinding() && !data.wallRiding() && data.trickTicks() == 0 && data.comboGrace() > 0) {
            data.setComboGrace(data.comboGrace() - 1);
        }
        if (grounded && data.comboGrace() == 0 && data.comboScore() > 0 && !data.manual() && player.tickCount % 20 == 0) {
            data.setComboScore(0);
            data.setComboMultiplier(1f);
        }
        data.setWasGrounded(grounded);
    }

    static void addStyle(JetSetData data, int base, float multi) {
        data.setComboScore(data.comboScore() + Math.max(1, Math.round(base * data.comboMultiplier())));
        data.setComboMultiplier(data.comboMultiplier() + multi);
        data.setComboGrace(MovementTuning.COMBO_GRACE_TICKS);
    }

    static int trickFromInput(float f, float s) {
        if (Math.abs(s) > Math.abs(f)) return s > 0 ? 2 : 4;
        if (f < -0.25f) return 5;
        if (f > 0.25f) return 1;
        return 0;
    }

    private TrickCombo() {}
}
