package com.herberto.jetsetcraft.data;

import com.herberto.jetsetcraft.movement.DanceCatalog;
import com.herberto.jetsetcraft.movement.DanceStyle;
import com.herberto.jetsetcraft.movement.GrindKind;
import com.herberto.jetsetcraft.movement.RideStyle;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;

public final class JetSetData {
    private RideStyle style = RideStyle.NONE;
    private boolean active;
    private float boost = 100.0f;
    private int comboScore;
    private float comboMultiplier = 1.0f;
    private int comboGrace;
    private float flow;
    private int trickIndex;
    private int trickTicks;
    private int lastTrickId = -1;
    private int repeatCount;
    private long uniqueTrickMask;
    private boolean groundStunt;
    private boolean boostTrick;
    private int landingGrade;
    private int landingTicks;
    private boolean dancing;
    private int danceStyle = DanceStyle.BREAKING.id();
    private int danceMoveId;
    private int danceTicks;
    private int danceChain;
    private int cypherSize;
    private long uniqueDanceMask;
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
    private double lastVerticalVelocity;
    private long lastDetectorRailPos = Long.MIN_VALUE;
    private long lastActivatorRailPos = Long.MIN_VALUE;
    private long lastSideBouncePos = Long.MIN_VALUE;
    private int surfaceInteractionCooldown;
    private Vec3 lastSolverVelocity = Vec3.ZERO;
    private Vec3 externalImpulse = Vec3.ZERO;
    private int externalImpulseTicks;
    private int terrainAssistCooldown;
    private ItemStack rideGear = ItemStack.EMPTY;

    public RideStyle style() { return style; }
    public void setStyle(RideStyle value) { style = value == null ? RideStyle.NONE : value; }
    public boolean active() { return active; }
    public void setActive(boolean value) { active = value && style != RideStyle.NONE; }
    public float boost() { return boost; }
    public void setBoost(float value) { boost = clamp(value, 0f, 100f); }
    public int comboScore() { return comboScore; }
    public void setComboScore(int value) { comboScore = Math.max(0, value); }
    public float comboMultiplier() { return comboMultiplier; }
    public void setComboMultiplier(float value) { comboMultiplier = clamp(value, 1f, 20f); }
    public int comboGrace() { return comboGrace; }
    public void setComboGrace(int value) { comboGrace = Math.max(0, value); }
    public float flow() { return flow; }
    public void setFlow(float value) { flow = clamp(value, 0f, 100f); }
    public int trickIndex() { return trickIndex; }
    public void setTrickIndex(int value) { trickIndex = Math.max(0, value); }
    public int trickTicks() { return trickTicks; }
    public void setTrickTicks(int value) { trickTicks = Math.max(0, value); }
    public int lastTrickId() { return lastTrickId; }
    public void setLastTrickId(int value) { lastTrickId = value; }
    public int repeatCount() { return repeatCount; }
    public void setRepeatCount(int value) { repeatCount = Math.max(0, value); }
    public long uniqueTrickMask() { return uniqueTrickMask; }
    public void setUniqueTrickMask(long value) { uniqueTrickMask = value; }
    public boolean groundStunt() { return groundStunt; }
    public void setGroundStunt(boolean value) { groundStunt = value; }
    public boolean boostTrick() { return boostTrick; }
    public void setBoostTrick(boolean value) { boostTrick = value; }
    public int landingGrade() { return landingGrade; }
    public void setLandingGrade(int value) { landingGrade = Math.max(0, Math.min(3, value)); }
    public int landingTicks() { return landingTicks; }
    public void setLandingTicks(int value) { landingTicks = Math.max(0, value); }
    public boolean dancing() { return dancing; }
    public void setDancing(boolean value) {
        dancing = value;
        if (!value) {
            danceTicks = 0;
            danceChain = 0;
            cypherSize = 0;
        }
    }
    public DanceStyle danceStyle() { return DanceStyle.byId(danceStyle); }
    public int danceStyleId() { return danceStyle; }
    public void setDanceStyle(int value) { danceStyle = DanceStyle.byId(value).id(); }
    public int danceMoveId() { return danceMoveId; }
    public void setDanceMoveId(int value) { danceMoveId = Math.floorMod(value, DanceCatalog.MOVE_COUNT); }
    public int danceTicks() { return danceTicks; }
    public void setDanceTicks(int value) { danceTicks = Math.max(0, value); }
    public int danceChain() { return danceChain; }
    public void setDanceChain(int value) { danceChain = Math.max(0, value); }
    public int cypherSize() { return cypherSize; }
    public void setCypherSize(int value) { cypherSize = Math.max(0, Math.min(16, value)); }
    public long uniqueDanceMask() { return uniqueDanceMask; }
    public void setUniqueDanceMask(long value) { uniqueDanceMask = value; }
    public boolean grinding() { return grinding; }
    public void setGrinding(boolean value) { grinding = value; if (!value) grindKind = GrindKind.NONE; }
    public GrindKind grindKind() { return grindKind; }
    public void setGrindKind(GrindKind value) { grindKind = value == null ? GrindKind.NONE : value; }
    public int grindReattachCooldown() { return grindReattachCooldown; }
    public void setGrindReattachCooldown(int value) { grindReattachCooldown = Math.max(0, value); }
    public boolean wallRiding() { return wallRiding; }
    public void setWallRiding(boolean value) { wallRiding = value; }
    public boolean manual() { return manual; }
    public void setManual(boolean value) { manual = value; }
    public boolean boosting() { return boosting; }
    public void setBoosting(boolean value) { boosting = value; }
    public boolean powersliding() { return powersliding; }
    public void setPowersliding(boolean value) { powersliding = value; }
    public int powerslideTicks() { return powerslideTicks; }
    public void setPowerslideTicks(int value) { powerslideTicks = Math.max(0, value); }
    public float wallSide() { return wallSide; }
    public void setWallSide(float value) { wallSide = clamp(value, -1f, 1f); }
    public Vec3 grindDirection() { return grindDirection; }
    public void setGrindDirection(Vec3 value) { grindDirection = value == null ? Vec3.ZERO : value; }
    public Vec3 wallNormal() { return wallNormal; }
    public void setWallNormal(Vec3 value) { wallNormal = value == null ? Vec3.ZERO : value; }
    public int grindGrace() { return grindGrace; }
    public void setGrindGrace(int value) { grindGrace = Math.max(0, value); }
    public int grindStuckTicks() { return grindStuckTicks; }
    public void setGrindStuckTicks(int value) { grindStuckTicks = Math.max(0, value); }
    public double grindCurveFactor() { return grindCurveFactor; }
    public void setGrindCurveFactor(double value) { grindCurveFactor = Math.max(0.65, Math.min(1.0, value)); }
    public int wallRideTicks() { return wallRideTicks; }
    public void setWallRideTicks(int value) { wallRideTicks = Math.max(0, value); }
    public int inputMask() { return inputMask; }
    public void setInputMask(int value) { inputMask = value; }
    public int previousInputMask() { return previousInputMask; }
    public void setPreviousInputMask(int value) { previousInputMask = value; }
    public float inputForward() { return inputForward; }
    public void setInputForward(float value) { inputForward = clamp(value, -1f, 1f); }
    public float inputStrafe() { return inputStrafe; }
    public void setInputStrafe(float value) { inputStrafe = clamp(value, -1f, 1f); }
    public boolean wasGrounded() { return wasGrounded; }
    public void setWasGrounded(boolean value) { wasGrounded = value; }
    public int airTicks() { return airTicks; }
    public void setAirTicks(int value) { airTicks = Math.max(0, value); }
    public long lastSyncTick() { return lastSyncTick; }
    public void setLastSyncTick(long value) { lastSyncTick = value; }
    public double momentum() { return momentum; }
    public void setMomentum(double value) { momentum = Math.max(0.0, value); }
    public double lastVerticalVelocity() { return lastVerticalVelocity; }
    public void setLastVerticalVelocity(double value) { lastVerticalVelocity = value; }
    public long lastDetectorRailPos() { return lastDetectorRailPos; }
    public void setLastDetectorRailPos(long value) { lastDetectorRailPos = value; }
    public long lastActivatorRailPos() { return lastActivatorRailPos; }
    public void setLastActivatorRailPos(long value) { lastActivatorRailPos = value; }
    public long lastSideBouncePos() { return lastSideBouncePos; }
    public void setLastSideBouncePos(long value) { lastSideBouncePos = value; }
    public int surfaceInteractionCooldown() { return surfaceInteractionCooldown; }
    public void setSurfaceInteractionCooldown(int value) { surfaceInteractionCooldown = Math.max(0, value); }
    public Vec3 lastSolverVelocity() { return lastSolverVelocity; }
    public void setLastSolverVelocity(Vec3 value) { lastSolverVelocity = value == null ? Vec3.ZERO : value; }
    public Vec3 externalImpulse() { return externalImpulse; }
    public void setExternalImpulse(Vec3 value) { externalImpulse = value == null ? Vec3.ZERO : value; }
    public int externalImpulseTicks() { return externalImpulseTicks; }
    public void setExternalImpulseTicks(int value) { externalImpulseTicks = Math.max(0, value); }
    public int terrainAssistCooldown() { return terrainAssistCooldown; }
    public void setTerrainAssistCooldown(int value) { terrainAssistCooldown = Math.max(0, value); }
    public ItemStack rideGear() { return rideGear; }
    public void setRideGear(ItemStack stack) { rideGear = stack == null ? ItemStack.EMPTY : stack; }
    public ItemStack takeRideGear() { ItemStack stack = rideGear; rideGear = ItemStack.EMPTY; return stack; }
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
        lastDetectorRailPos = Long.MIN_VALUE;
        lastActivatorRailPos = Long.MIN_VALUE;
        lastSideBouncePos = Long.MIN_VALUE;
        surfaceInteractionCooldown = 0;
        lastSolverVelocity = Vec3.ZERO;
        externalImpulse = Vec3.ZERO;
        externalImpulseTicks = 0;
        terrainAssistCooldown = 0;
    }

    public void resetCombo() {
        comboScore = 0;
        comboMultiplier = 1f;
        comboGrace = 0;
        uniqueTrickMask = 0L;
        uniqueDanceMask = 0L;
        lastTrickId = -1;
        repeatCount = 0;
        groundStunt = false;
        boostTrick = false;
    }

    public void copyFrom(JetSetData other) {
        style = other.style;
        active = other.active;
        boost = other.boost;
        comboScore = other.comboScore;
        comboMultiplier = other.comboMultiplier;
        comboGrace = other.comboGrace;
        flow = other.flow;
        uniqueTrickMask = other.uniqueTrickMask;
        uniqueDanceMask = other.uniqueDanceMask;
        danceStyle = other.danceStyle;
        danceMoveId = other.danceMoveId;
        rideGear = other.rideGear.copy();
        resetTransientRideState();
        dancing = false;
        groundStunt = false;
        boostTrick = false;
        inputMask = 0;
        previousInputMask = 0;
        inputForward = 0;
        inputStrafe = 0;
        wasGrounded = true;
        airTicks = 0;
        momentum = 0;
        lastVerticalVelocity = 0;
    }

    public CompoundTag save() {
        CompoundTag tag = new CompoundTag();
        tag.putInt("Style", style.id());
        tag.putBoolean("Active", active);
        tag.putFloat("Boost", boost);
        tag.putInt("ComboScore", comboScore);
        tag.putFloat("ComboMultiplier", comboMultiplier);
        tag.putFloat("Flow", flow);
        tag.putLong("UniqueTricks", uniqueTrickMask);
        tag.putLong("UniqueDances", uniqueDanceMask);
        tag.putInt("DanceStyle", danceStyle);
        tag.putInt("DanceMove", danceMoveId);
        if (!rideGear.isEmpty()) tag.put("RideGear", rideGear.save(new CompoundTag()));
        return tag;
    }

    public void load(CompoundTag tag) {
        style = RideStyle.byId(tag.getInt("Style"));
        active = tag.getBoolean("Active") && style != RideStyle.NONE;
        boost = tag.contains("Boost") ? tag.getFloat("Boost") : 100f;
        comboScore = tag.getInt("ComboScore");
        comboMultiplier = tag.contains("ComboMultiplier") ? tag.getFloat("ComboMultiplier") : 1f;
        flow = tag.contains("Flow") ? tag.getFloat("Flow") : 0f;
        uniqueTrickMask = tag.contains("UniqueTricks") ? tag.getLong("UniqueTricks") : 0L;
        uniqueDanceMask = tag.contains("UniqueDances") ? tag.getLong("UniqueDances") : 0L;
        danceStyle = tag.contains("DanceStyle") ? DanceStyle.byId(tag.getInt("DanceStyle")).id() : DanceStyle.BREAKING.id();
        danceMoveId = tag.contains("DanceMove") ? Math.floorMod(tag.getInt("DanceMove"), DanceCatalog.MOVE_COUNT) : 0;
        rideGear = tag.contains("RideGear", 10) ? ItemStack.of(tag.getCompound("RideGear")) : ItemStack.EMPTY;
        if (rideGear.isEmpty()) active = false;
    }

    private static float clamp(float value, float min, float max) {
        return Math.max(min, Math.min(max, value));
    }
}
