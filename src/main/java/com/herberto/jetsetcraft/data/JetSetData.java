package com.herberto.jetsetcraft.data;

import com.herberto.jetsetcraft.movement.GrindKind;
import com.herberto.jetsetcraft.movement.RideStyle;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.phys.Vec3;

public final class JetSetData {
    private RideStyle style = RideStyle.NONE;
    private boolean active;
    private float boost = 100.0f;
    private int comboScore;
    private float comboMultiplier = 1.0f;
    private int comboGrace;
    private int trickIndex;
    private int trickTicks;
    private boolean grinding;
    private GrindKind grindKind = GrindKind.NONE;
    private int grindReattachCooldown;
    private boolean wallRiding;
    private boolean manual;
    private boolean boosting;
    private boolean powersliding;
    private int powerslideTicks;
    private float wallSide;
    private Vec3 grindDirection = Vec3.ZERO;
    private Vec3 wallNormal = Vec3.ZERO;
    private int grindGrace;
    private int grindStuckTicks;
    private double grindCurveFactor = 1.0;
    private int wallRideTicks;
    private int inputMask;
    private int previousInputMask;
    private float inputForward;
    private float inputStrafe;
    private boolean wasGrounded = true;
    private int airTicks;
    private long lastSyncTick;
    private double momentum;

    public RideStyle style() { return style; }
    public void setStyle(RideStyle v) { style = v == null ? RideStyle.NONE : v; }
    public boolean active() { return active; }
    public void setActive(boolean v) { active = v && style != RideStyle.NONE; }
    public float boost() { return boost; }
    public void setBoost(float v) { boost = Math.max(0f, Math.min(100f, v)); }
    public int comboScore() { return comboScore; }
    public void setComboScore(int v) { comboScore = Math.max(0, v); }
    public float comboMultiplier() { return comboMultiplier; }
    public void setComboMultiplier(float v) { comboMultiplier = Math.max(1f, Math.min(20f, v)); }
    public int comboGrace() { return comboGrace; }
    public void setComboGrace(int v) { comboGrace = Math.max(0, v); }
    public int trickIndex() { return trickIndex; }
    public void setTrickIndex(int v) { trickIndex = Math.max(0, v); }
    public int trickTicks() { return trickTicks; }
    public void setTrickTicks(int v) { trickTicks = Math.max(0, v); }
    public boolean grinding() { return grinding; }
    public void setGrinding(boolean v) { grinding = v; if (!v) grindKind = GrindKind.NONE; }
    public GrindKind grindKind() { return grindKind; }
    public void setGrindKind(GrindKind v) { grindKind = v == null ? GrindKind.NONE : v; }
    public int grindReattachCooldown() { return grindReattachCooldown; }
    public void setGrindReattachCooldown(int v) { grindReattachCooldown = Math.max(0, v); }
    public boolean wallRiding() { return wallRiding; }
    public void setWallRiding(boolean v) { wallRiding = v; }
    public boolean manual() { return manual; }
    public void setManual(boolean v) { manual = v; }
    public boolean boosting() { return boosting; }
    public void setBoosting(boolean v) { boosting = v; }
    public boolean powersliding() { return powersliding; }
    public void setPowersliding(boolean v) { powersliding = v; }
    public int powerslideTicks() { return powerslideTicks; }
    public void setPowerslideTicks(int v) { powerslideTicks = Math.max(0, v); }
    public float wallSide() { return wallSide; }
    public void setWallSide(float v) { wallSide = Math.max(-1f, Math.min(1f, v)); }
    public Vec3 grindDirection() { return grindDirection; }
    public void setGrindDirection(Vec3 v) { grindDirection = v == null ? Vec3.ZERO : v; }
    public Vec3 wallNormal() { return wallNormal; }
    public void setWallNormal(Vec3 v) { wallNormal = v == null ? Vec3.ZERO : v; }
    public int grindGrace() { return grindGrace; }
    public void setGrindGrace(int v) { grindGrace = Math.max(0, v); }
    public int grindStuckTicks() { return grindStuckTicks; }
    public void setGrindStuckTicks(int v) { grindStuckTicks = Math.max(0, v); }
    public double grindCurveFactor() { return grindCurveFactor; }
    public void setGrindCurveFactor(double v) { grindCurveFactor = Math.max(0.65, Math.min(1.0, v)); }
    public int wallRideTicks() { return wallRideTicks; }
    public void setWallRideTicks(int v) { wallRideTicks = Math.max(0, v); }
    public int inputMask() { return inputMask; }
    public void setInputMask(int v) { inputMask = v; }
    public int previousInputMask() { return previousInputMask; }
    public void setPreviousInputMask(int v) { previousInputMask = v; }
    public float inputForward() { return inputForward; }
    public void setInputForward(float v) { inputForward = Math.max(-1f, Math.min(1f, v)); }
    public float inputStrafe() { return inputStrafe; }
    public void setInputStrafe(float v) { inputStrafe = Math.max(-1f, Math.min(1f, v)); }
    public boolean wasGrounded() { return wasGrounded; }
    public void setWasGrounded(boolean v) { wasGrounded = v; }
    public int airTicks() { return airTicks; }
    public void setAirTicks(int v) { airTicks = Math.max(0, v); }
    public long lastSyncTick() { return lastSyncTick; }
    public void setLastSyncTick(long v) { lastSyncTick = v; }
    public double momentum() { return momentum; }
    public void setMomentum(double v) { momentum = Math.max(0.0, v); }
    public boolean pressed(int flag) { return (inputMask & flag) != 0; }
    public boolean justPressed(int flag) { return (inputMask & flag) != 0 && (previousInputMask & flag) == 0; }

    public void resetTransientRideState() {
        grinding = false;
        grindKind = GrindKind.NONE;
        grindReattachCooldown = 0;
        wallRiding = false;
        manual = false;
        boosting = false;
        powersliding = false;
        powerslideTicks = 0;
        wallSide = 0f;
        grindDirection = Vec3.ZERO;
        wallNormal = Vec3.ZERO;
        grindGrace = 0;
        grindStuckTicks = 0;
        grindCurveFactor = 1.0;
        wallRideTicks = 0;
    }

    public void copyFrom(JetSetData o) {
        style = o.style; active = o.active; boost = o.boost; comboScore = o.comboScore;
        comboMultiplier = o.comboMultiplier; comboGrace = o.comboGrace;
        resetTransientRideState(); inputMask = 0; previousInputMask = 0; inputForward = 0; inputStrafe = 0;
        wasGrounded = true; airTicks = 0; momentum = 0;
    }

    public CompoundTag save() {
        CompoundTag t = new CompoundTag();
        t.putInt("Style", style.id()); t.putBoolean("Active", active); t.putFloat("Boost", boost);
        t.putInt("ComboScore", comboScore); t.putFloat("ComboMultiplier", comboMultiplier);
        return t;
    }

    public void load(CompoundTag t) {
        style = RideStyle.byId(t.getInt("Style")); active = t.getBoolean("Active") && style != RideStyle.NONE;
        boost = t.contains("Boost") ? t.getFloat("Boost") : 100f;
        comboScore = t.getInt("ComboScore"); comboMultiplier = t.contains("ComboMultiplier") ? t.getFloat("ComboMultiplier") : 1f;
    }
}
