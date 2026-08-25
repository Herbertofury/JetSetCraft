package com.herberto.jetsetcraft.data;

import com.herberto.jetsetcraft.item.RideGearItem;
import com.herberto.jetsetcraft.movement.DanceCatalog;
import com.herberto.jetsetcraft.movement.DanceStyle;
import com.herberto.jetsetcraft.movement.GrindKind;
import com.herberto.jetsetcraft.movement.RideStyle;
import com.herberto.jetsetcraft.network.InputFlags;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
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
    private int strideTicks;
    private double driftTurn;
    private double bestDriftTurn;
    private float wallSide;
    private Vec3 grindDirection = Vec3.ZERO;
    private Vec3 wallNormal = Vec3.ZERO;
    private int grindGrace;
    private int grindStuckTicks;
    private double grindCurveFactor = 1.0;
    private int wallRideTicks;
    private int wallKicksRemaining = 3;
    private int windTicks;
    private Vec3 windBias = Vec3.ZERO;
    private long wallPlane = Long.MIN_VALUE;
    private long lastWallPlane = Long.MIN_VALUE;
    private int parkourCooldown;
    private int inputMask;
    private int previousInputMask;
    private float inputForward;
    private float inputStrafe;
    private int inputAgeTicks;
    private boolean inputWatchdogArmed;
    private int trickBufferTicks;
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
    public int strideTicks() { return strideTicks; }
    public void setStrideTicks(int value) { strideTicks = Math.max(0, value); }
    public double driftTurn() { return driftTurn; }
    public void setDriftTurn(double value) { driftTurn = Double.isFinite(value) ? value : 0.0; }
    public double bestDriftTurn() { return bestDriftTurn; }
    public void setBestDriftTurn(double value) {
        bestDriftTurn = Double.isFinite(value) ? Math.max(0.0, value) : 0.0;
    }
    public float wallSide() { return wallSide; }
    public void setWallSide(float value) { wallSide = clamp(value, -1f, 1f); }
    public Vec3 grindDirection() { return grindDirection; }
    public void setGrindDirection(Vec3 value) { grindDirection = finiteVector(value); }
    public Vec3 wallNormal() { return wallNormal; }
    public void setWallNormal(Vec3 value) { wallNormal = finiteVector(value); }
    public int grindGrace() { return grindGrace; }
    public void setGrindGrace(int value) { grindGrace = Math.max(0, value); }
    public int grindStuckTicks() { return grindStuckTicks; }
    public void setGrindStuckTicks(int value) { grindStuckTicks = Math.max(0, value); }
    public double grindCurveFactor() { return grindCurveFactor; }
    public void setGrindCurveFactor(double value) {
        grindCurveFactor = Double.isFinite(value) ? Math.max(0.65, Math.min(1.0, value)) : 1.0;
    }
    public int wallRideTicks() { return wallRideTicks; }
    public void setWallRideTicks(int value) { wallRideTicks = Math.max(0, value); }
    public int wallKicksRemaining() { return wallKicksRemaining; }
    public void setWallKicksRemaining(int value) { wallKicksRemaining = Math.max(0, Math.min(3, value)); }
    public int windTicks() { return windTicks; }
    public void setWindTicks(int value) { windTicks = Math.max(0, value); }
    public Vec3 windBias() { return windBias; }
    public void setWindBias(Vec3 value) { windBias = finiteVector(value); }
    public long wallPlane() { return wallPlane; }
    public void setWallPlane(long value) { wallPlane = value; }
    public long lastWallPlane() { return lastWallPlane; }
    public void setLastWallPlane(long value) { lastWallPlane = value; }
    public int parkourCooldown() { return parkourCooldown; }
    public void setParkourCooldown(int value) { parkourCooldown = Math.max(0, value); }
    public int inputMask() { return inputMask; }
    public void setInputMask(int value) {
        int sanitized = value & InputFlags.ALL;
        if ((sanitized & InputFlags.TRICK) != 0 && (inputMask & InputFlags.TRICK) == 0) {
            trickBufferTicks = 6;
        }
        inputMask = sanitized;
    }
    public int previousInputMask() { return previousInputMask; }
    public void setPreviousInputMask(int value) { previousInputMask = value & InputFlags.ALL; }
    public float inputForward() { return inputForward; }
    public void setInputForward(float value) { inputForward = sanitizeUnit(value); }
    public float inputStrafe() { return inputStrafe; }
    public void setInputStrafe(float value) { inputStrafe = sanitizeUnit(value); }
    public int inputAgeTicks() { return inputAgeTicks; }
    public int trickBufferTicks() { return trickBufferTicks; }

    /** Accept one complete client input sample at the server-authoritative boundary. */
    public void acceptInput(int mask, float forward, float strafe) {
        setInputMask(mask);
        setInputForward(forward);
        setInputStrafe(strafe);
        inputAgeTicks = 0;
        inputWatchdogArmed = true;
    }

    /**
     * Prevent a lost release packet or disconnected client from leaving boost/grind/manual/dance latched forever.
     * Direct server-side tests and automation remain unaffected until a real network sample arms the watchdog.
     */
    public void tickInputWatchdog() {
        if (trickBufferTicks > 0) trickBufferTicks--;
        if (!inputWatchdogArmed) return;
        inputAgeTicks++;
        if (inputAgeTicks > 20) clearInputState();
    }

    public void clearInputState() {
        inputMask = 0;
        previousInputMask = 0;
        inputForward = 0.0f;
        inputStrafe = 0.0f;
        inputAgeTicks = 0;
        inputWatchdogArmed = false;
        trickBufferTicks = 0;
    }

    public void consumeTrickBuffer() { trickBufferTicks = 0; }
    public boolean wasGrounded() { return wasGrounded; }
    public void setWasGrounded(boolean value) { wasGrounded = value; }
    public int airTicks() { return airTicks; }
    public void setAirTicks(int value) { airTicks = Math.max(0, value); }
    public long lastSyncTick() { return lastSyncTick; }
    public void setLastSyncTick(long value) { lastSyncTick = value; }
    public double momentum() { return momentum; }
    public void setMomentum(double value) { momentum = Double.isFinite(value) ? Math.max(0.0, value) : 0.0; }
    public double lastVerticalVelocity() { return lastVerticalVelocity; }
    public void setLastVerticalVelocity(double value) { lastVerticalVelocity = Double.isFinite(value) ? value : 0.0; }
    public long lastDetectorRailPos() { return lastDetectorRailPos; }
    public void setLastDetectorRailPos(long value) { lastDetectorRailPos = value; }
    public long lastActivatorRailPos() { return lastActivatorRailPos; }
    public void setLastActivatorRailPos(long value) { lastActivatorRailPos = value; }
    public long lastSideBouncePos() { return lastSideBouncePos; }
    public void setLastSideBouncePos(long value) { lastSideBouncePos = value; }
    public int surfaceInteractionCooldown() { return surfaceInteractionCooldown; }
    public void setSurfaceInteractionCooldown(int value) { surfaceInteractionCooldown = Math.max(0, value); }
    public Vec3 lastSolverVelocity() { return lastSolverVelocity; }
    public void setLastSolverVelocity(Vec3 value) { lastSolverVelocity = finiteVector(value); }
    public Vec3 externalImpulse() { return externalImpulse; }
    public void setExternalImpulse(Vec3 value) { externalImpulse = finiteVector(value); }
    public int externalImpulseTicks() { return externalImpulseTicks; }
    public void setExternalImpulseTicks(int value) { externalImpulseTicks = Math.max(0, value); }
    public int terrainAssistCooldown() { return terrainAssistCooldown; }
    public void setTerrainAssistCooldown(int value) { terrainAssistCooldown = Math.max(0, value); }
    public ItemStack rideGear() { return rideGear; }
    public void setRideGear(ItemStack stack) {
        if (stack == null || stack.isEmpty() || !(stack.getItem() instanceof RideGearItem)) {
            rideGear = ItemStack.EMPTY;
            return;
        }
        rideGear = stack.copy();
        rideGear.setCount(1);
    }
    public ItemStack takeRideGear() { ItemStack stack = rideGear; rideGear = ItemStack.EMPTY; return stack; }
    public boolean pressed(int flag) { return (inputMask & flag) != 0; }
    public boolean justPressed(int flag) {
        if (flag == InputFlags.TRICK && trickBufferTicks > 0) return true;
        return (inputMask & flag) != 0 && (previousInputMask & flag) == 0;
    }

    public void resetTransientRideState() {
        grinding = false;
        grindKind = GrindKind.NONE;
        grindReattachCooldown = 0;
        wallRiding = false;
        manual = false;
        boosting = false;
        powersliding = false;
        powerslideTicks = 0;
        strideTicks = 0;
        driftTurn = 0.0;
        bestDriftTurn = 0.0;
        wallSide = 0f;
        grindDirection = Vec3.ZERO;
        wallNormal = Vec3.ZERO;
        grindGrace = 0;
        grindStuckTicks = 0;
        grindCurveFactor = 1.0;
        wallRideTicks = 0;
        wallKicksRemaining = 3;
        windTicks = 0;
        windBias = Vec3.ZERO;
        wallPlane = Long.MIN_VALUE;
        lastWallPlane = Long.MIN_VALUE;
        parkourCooldown = 0;
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
        if (other == null) {
            load(new CompoundTag());
            return;
        }
        setRideGear(other.rideGear);
        RideStyle physicalStyle = rideGear.getItem() instanceof RideGearItem gear ? gear.style() : RideStyle.NONE;
        setStyle(physicalStyle);
        setActive(other.active && physicalStyle != RideStyle.NONE && other.style == physicalStyle);
        setBoost(other.boost);
        setComboScore(other.comboScore);
        setComboMultiplier(other.comboMultiplier);
        setComboGrace(other.comboGrace);
        setFlow(other.flow);
        setUniqueTrickMask(other.uniqueTrickMask);
        setUniqueDanceMask(other.uniqueDanceMask);
        setDanceStyle(other.danceStyle);
        setDanceMoveId(other.danceMoveId);
        resetTransientRideState();
        dancing = false;
        groundStunt = false;
        boostTrick = false;
        clearInputState();
        wasGrounded = true;
        airTicks = 0;
        momentum = 0.0;
        lastVerticalVelocity = 0.0;
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
        CompoundTag safe = tag == null ? new CompoundTag() : tag;
        setBoost(safe.contains("Boost") ? safe.getFloat("Boost") : 100.0F);
        setComboScore(safe.getInt("ComboScore"));
        setComboMultiplier(safe.contains("ComboMultiplier") ? safe.getFloat("ComboMultiplier") : 1.0F);
        setFlow(safe.contains("Flow") ? safe.getFloat("Flow") : 0.0F);
        setUniqueTrickMask(safe.contains("UniqueTricks") ? safe.getLong("UniqueTricks") : 0L);
        setUniqueDanceMask(safe.contains("UniqueDances") ? safe.getLong("UniqueDances") : 0L);
        setDanceStyle(safe.contains("DanceStyle") ? safe.getInt("DanceStyle") : DanceStyle.BREAKING.id());
        setDanceMoveId(safe.contains("DanceMove") ? safe.getInt("DanceMove") : 0);
        setRideGear(safe.contains("RideGear", Tag.TAG_COMPOUND)
                ? ItemStack.of(safe.getCompound("RideGear")) : ItemStack.EMPTY);

        // The physical item is the source of truth. A stale/corrupt style integer can never activate a different ride.
        RideStyle storedStyle = RideStyle.byId(safe.getInt("Style"));
        RideStyle physicalStyle = rideGear.getItem() instanceof RideGearItem gear ? gear.style() : RideStyle.NONE;
        setStyle(physicalStyle == RideStyle.NONE ? RideStyle.NONE : physicalStyle);
        setActive(safe.getBoolean("Active") && physicalStyle != RideStyle.NONE
                && (storedStyle == RideStyle.NONE || storedStyle == physicalStyle));

        resetTransientRideState();
        clearInputState();
        dancing = false;
        groundStunt = false;
        boostTrick = false;
        trickTicks = 0;
        landingTicks = 0;
        wasGrounded = true;
        airTicks = 0;
        momentum = 0.0;
        lastVerticalVelocity = 0.0;
    }

    private static Vec3 finiteVector(Vec3 value) {
        if (value == null || !Double.isFinite(value.x) || !Double.isFinite(value.y) || !Double.isFinite(value.z)) {
            return Vec3.ZERO;
        }
        return value;
    }

    private static float sanitizeUnit(float value) {
        return Float.isFinite(value) ? Math.max(-1.0f, Math.min(1.0f, value)) : 0.0f;
    }

    private static float clamp(float value, float min, float max) {
        if (!Float.isFinite(value)) return min <= 0.0f && max >= 0.0f ? 0.0f : min;
        return Math.max(min, Math.min(max, value));
    }
}
