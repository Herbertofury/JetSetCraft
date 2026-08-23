package com.herberto.jetsetcraft.movement;

import com.herberto.jetsetcraft.config.JetSetConfig;
import com.herberto.jetsetcraft.data.JetSetData;
import com.herberto.jetsetcraft.network.InputFlags;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.phys.Vec3;

final class TrickCombo {
    static void handleTricksAndCombo(ServerPlayer player, JetSetData data, boolean grounded) {
        if (!data.groundStunt() && data.justPressed(InputFlags.TRICK)) {
            if (data.grinding() && JetSetConfig.SERVER.allowRailTricks.get()) {
                perform(player, data, TrickCatalog.GRIND, 18);
            } else if (!grounded && !data.grinding() && data.airTicks() >= 2) {
                perform(player, data, TrickCatalog.AIR, 22);
                Vec3 velocity = player.getDeltaMovement();
                double lift = data.boostTrick() ? 0.065 : 0.035;
                player.setDeltaMovement(velocity.x, Math.max(velocity.y, -0.08) + lift, velocity.z);
                player.hurtMarked = true;
            } else if (grounded && JetSetConfig.SERVER.allowGroundStunts.get()
                    && !player.isUsingItem() && !player.swinging && !data.wallRiding()
                    && (data.momentum() <= 0.30 || data.pressed(InputFlags.BRAKE)
                    || data.pressed(InputFlags.MANUAL) || data.powersliding())) {
                perform(player, data, TrickCatalog.GROUND, 34);
                data.setGroundStunt(true);
                Vec3 velocity = player.getDeltaMovement();
                player.setDeltaMovement(velocity.x * 0.35, velocity.y, velocity.z * 0.35);
                player.hurtMarked = true;
            }
        }

        if (data.trickTicks() > 0) {
            data.setTrickTicks(data.trickTicks() - 1);
            if (data.trickTicks() == 0) {
                data.setGroundStunt(false);
                data.setBoostTrick(false);
            }
        }
        if (data.landingTicks() > 0) data.setLandingTicks(data.landingTicks() - 1);

        if (!grounded) {
            data.setAirTicks(data.airTicks() + 1);
        } else if (!data.wasGrounded()) {
            scoreLanding(player, data);
            data.setAirTicks(0);
        }

        if (data.manual()) {
            data.setComboGrace(MovementTuning.COMBO_GRACE_TICKS);
            if (player.tickCount % 6 == 0) addStyle(data, 14, 0.01f);
        } else if (!data.grinding() && !data.wallRiding() && data.trickTicks() == 0 && data.comboGrace() > 0) {
            data.setComboGrace(data.comboGrace() - 1);
        }

        if (grounded && data.comboGrace() == 0 && data.comboScore() > 0 && !data.manual()
                && !data.dancing() && player.tickCount % 20 == 0) {
            data.resetCombo();
        }
        if (data.comboGrace() == 0 && !data.dancing() && !data.grinding() && !data.manual()) {
            data.setFlow(data.flow() - 0.035f);
        }
        data.setWasGrounded(grounded);
    }

    private static void perform(ServerPlayer player, JetSetData data, int context, int duration) {
        data.consumeTrickBuffer();
        int seed = data.comboScore() / 180 + Math.max(0, data.lastTrickId()) + player.tickCount / 7;
        TrickCatalog.Trick trick = TrickCatalog.select(context, data.style(), data.inputForward(), data.inputStrafe(), seed);
        boolean boostTrick = JetSetConfig.SERVER.allowBoostTricks.get() && data.pressed(InputFlags.BOOST)
                && data.boost() >= 6.0f;
        data.setTrickIndex(trick.id());
        data.setTrickTicks(duration);
        data.setBoostTrick(boostTrick);
        if (context != TrickCatalog.GROUND) data.setGroundStunt(false);

        int repeats = data.lastTrickId() == trick.id() ? data.repeatCount() + 1 : 0;
        data.setRepeatCount(repeats);
        data.setLastTrickId(trick.id());
        long bit = 1L << trick.id();
        boolean fresh = (data.uniqueTrickMask() & bit) == 0L;
        data.setUniqueTrickMask(data.uniqueTrickMask() | bit);

        float repetitionScale = repetitionScale(repeats);
        float varietyScale = 1.0f + Math.min(0.32f, Long.bitCount(data.uniqueTrickMask()) * 0.025f);
        float boostScale = boostTrick ? 1.38f : 1.0f;
        int points = Math.max(1, Math.round(trick.basePoints() * repetitionScale * varietyScale * boostScale));
        float multiplier = trick.multiplierGain() * repetitionScale + (fresh ? 0.035f : 0.0f)
                + (boostTrick ? 0.065f : 0.0f);
        addStyle(data, points, multiplier);
        data.setFlow(data.flow() + 4.0f + trick.animationIndex() * 0.65f + (fresh ? 2.5f : 0.0f)
                + (boostTrick ? 3.0f : 0.0f));
        float reward = trick.boostReward() * repetitionScale * (boostTrick ? 0.25f : 1.0f)
                * JetSetConfig.SERVER.styleBoostScale.get().floatValue();
        if (context == TrickCatalog.GRIND && data.grindKind().rail()) reward += 1.5f;
        data.setBoost(data.boost() + reward - (boostTrick ? 3.0f : 0.0f));
        StyleFeedback.trick(player, trick, boostTrick, fresh);
    }

    private static void scoreLanding(ServerPlayer player, JetSetData data) {
        if (data.airTicks() < 4 || data.comboScore() <= 0) return;
        double impact = Math.abs(Math.min(0.0, data.lastVerticalVelocity()));
        int grade;
        if (data.airTicks() >= 9 && impact <= 0.68) grade = 3;
        else if ((data.pressed(InputFlags.MANUAL) && data.airTicks() >= 7 && impact <= 1.05) || impact <= 0.92) grade = 2;
        else grade = 1;
        data.setLandingGrade(grade);
        data.setLandingTicks(34);
        data.setComboGrace(Math.max(data.comboGrace(), 70));
        data.setFlow(data.flow() + grade * 3.5f);
        data.setBoost(data.boost() + grade * 3.0f * JetSetConfig.SERVER.styleBoostScale.get().floatValue());
        addStyle(data, 75 + grade * 55, 0.025f * grade);
        StyleFeedback.landing(player, grade);
    }

    static float repetitionScale(int repeats) {
        return Math.max(0.35f, 1.0f - Math.max(0, repeats) * 0.16f);
    }

    static void addStyle(JetSetData data, int base, float multiplierGain) {
        int award = Math.max(1, Math.round(base * data.comboMultiplier()));
        long total = (long) data.comboScore() + award;
        data.setComboScore(total >= Integer.MAX_VALUE ? Integer.MAX_VALUE : (int) total);
        data.setComboMultiplier(data.comboMultiplier() + multiplierGain);
        data.setComboGrace(MovementTuning.COMBO_GRACE_TICKS);
        data.setFlow(data.flow() + Math.min(1.25f, Math.max(0.08f, base / 260.0f)));
    }

    private TrickCombo() {}
}
