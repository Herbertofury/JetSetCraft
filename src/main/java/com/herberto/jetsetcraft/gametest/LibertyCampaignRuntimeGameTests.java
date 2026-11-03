package com.herberto.jetsetcraft.gametest;

import com.herberto.jetsetcraft.JetSetCraft;
import com.herberto.jetsetcraft.compat.LibertyTaczCompat;
import com.herberto.jetsetcraft.data.JetSetDataProvider;
import com.herberto.jetsetcraft.entity.GraffitiEntity;
import com.herberto.jetsetcraft.entity.LibertyCoreEntity;
import com.herberto.jetsetcraft.entity.LibertySupportEntity;
import com.herberto.jetsetcraft.entity.LibertyTransportEntity;
import com.herberto.jetsetcraft.item.SprayCanItem;
import com.herberto.jetsetcraft.item.RideGearItem;
import com.herberto.jetsetcraft.liberty.LibertyArmory;
import com.herberto.jetsetcraft.liberty.LibertyCampaignData;
import com.herberto.jetsetcraft.liberty.LibertyManager;
import com.herberto.jetsetcraft.liberty.LibertyMobSafety;
import com.herberto.jetsetcraft.liberty.LibertyMissionPlan;
import com.herberto.jetsetcraft.liberty.LibertyMissionRuntime;
import com.herberto.jetsetcraft.liberty.LibertyOperation;
import com.herberto.jetsetcraft.liberty.LibertyStratagemEngine;
import com.herberto.jetsetcraft.liberty.LibertyWorld;
import com.herberto.jetsetcraft.network.C2SLibertyActionPacket;
import com.herberto.jetsetcraft.registry.ModEntities;
import com.herberto.jetsetcraft.registry.ModItems;
import com.herberto.jetsetcraft.rush.RushReturnState;
import com.mojang.authlib.GameProfile;
import io.netty.buffer.Unpooled;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestAssertException;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.Connection;
import net.minecraft.network.PacketSendListener;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.PacketFlow;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.level.TicketType;
import net.minecraft.server.network.ServerGamePacketListenerImpl;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.ChestBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.util.FakePlayerFactory;
import net.minecraftforge.event.entity.EntityJoinLevelEvent;
import net.minecraftforge.event.entity.living.LivingAttackEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.gametest.GameTestHolder;
import net.minecraftforge.gametest.PrefixGameTestTemplate;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.function.Supplier;

/**
 * Real campaign entry and ordinary Forge ticks; native TaCZ firing and ordinary free-spray actions.
 * Explicit fixture controls: loaded-region tickets, player relocation, one-health bullet target and
 * one skipped gun draw animation. No mission completion fields, fake core deaths or spawn quotas are injected.
 */
@GameTestHolder(JetSetCraft.MOD_ID)
@PrefixGameTestTemplate(false)
public final class LibertyCampaignRuntimeGameTests {
    private static final TicketType<UUID> TICKET = TicketType.create("jetsetcraft_liberty_campaign_test", UUID::compareTo, 20);

    private static final class Fixture {
        final GameTestHelper helper;
        final ServerPlayer owner;
        final ServerLevel source;
        Vec3 originalPosition;
        ListTag originalInventory;
        CompoundTag originalRide;
        final CampaignRuntimeCapture capture;
        final boolean nativeDamageOwner;
        final Set<ChunkPos> chunks = new LinkedHashSet<>();
        ServerLevel district;
        LibertyOperation operation;
        boolean closed, monitorPressure, registeredHomeOwner;
        int ticks, nextAt, peakEnemies;
        Runnable next;
        Fixture(GameTestHelper helper, String name) { this(helper, name, false); }
        Fixture(GameTestHelper helper, String name, boolean nativeDamageOwner) {
            this.helper = helper; source = helper.getLevel();
            this.nativeDamageOwner = nativeDamageOwner;
            GameProfile profile = new GameProfile(UUID.randomUUID(), name);
            owner = nativeDamageOwner ? nativeDamagePlayer(source, profile) : FakePlayerFactory.get(source, profile);
            owner.setGameMode(GameType.ADVENTURE); owner.setHealth(owner.getMaxHealth());
            owner.getInventory().clearContent(); owner.getInventory().setItem(7, new ItemStack(Items.EMERALD, 13));
            ItemStack container = new ItemStack(Items.SHULKER_BOX);
            CompoundTag inside = new CompoundTag(); ListTag contents = new ListTag();
            CompoundTag nested = new ItemStack(Items.DIAMOND, 3).save(new CompoundTag()); nested.putByte("Slot", (byte) 4); contents.add(nested);
            inside.put("Items", contents); container.getOrCreateTag().put("BlockEntityTag", inside);
            container.getOrCreateTag().putString("CampaignOriginal", owner.getUUID().toString()); owner.getInventory().setItem(2, container);
            ItemStack armor = new ItemStack(Items.DIAMOND_CHESTPLATE); armor.getOrCreateTag().putString("CampaignArmor", "keep");
            owner.setItemSlot(EquipmentSlot.CHEST, armor);
            owner.experienceLevel = 17; owner.totalExperience = 321; owner.experienceProgress = .375f;
            owner.setHealth(17); owner.setAbsorptionAmount(2); owner.getFoodData().setFoodLevel(13); owner.getFoodData().setSaturation(4);
            ItemStack skates = new ItemStack(ModItems.QUAD_SKATES.get()); skates.getOrCreateTag().putString("CampaignRide", "original physical gear");
            owner.getCapability(JetSetDataProvider.CAPABILITY).ifPresent(data -> {
                data.setRideGear(skates); data.setStyle(((RideGearItem) skates.getItem()).style()); data.setActive(false); data.setBoost(43);
            });
            owner.getInventory().selected = 7; owner.containerMenu.setCarried(ItemStack.EMPTY);
            owner.moveTo(28000.5, 96, 28000.5, 27, 8); owner.setDeltaMovement(Vec3.ZERO);
            rememberOriginal(this);
            capture = new CampaignRuntimeCapture(owner);
            MinecraftForge.EVENT_BUS.register(capture);
            helper.onEachTick(() -> tick(this));
        }
    }

    @GameTest(template = "hoverboard_empty", timeoutTicks = 300)
    public static void realPodAndReinforcementRetainCallEngineResourcesAndCooldownsUntilReturn(GameTestHelper helper) {
        if (skipAbsent(helper, "stratagem_lifecycle")) return;
        Fixture f = begin(helper, "JSC_campaigncalls", "neon_docks|turf_liberation|1|20", true);
        LibertyStratagemEngine engine = LibertyStratagemEngine.forOperation(f.operation.id);
        check(engine != null, "Actual campaign deployment did not register its call engine");
        List<String> loadout = engine.loadout();
        int initialSupplies = f.operation.resupplies();
        later(f, 10, () -> {
            check(f.operation.phase() == LibertyOperation.Phase.DROPPING
                            && LibertyStratagemEngine.forOperation(f.operation.id) == engine
                            && engine.context().active() && engine.snapshot().stream().noneMatch(LibertyStratagemEngine.Row::ready),
                    "The first real pod ticks closed its call engine or exposed callable support during descent");
            missionPacket(f, "callin", "mg_43_machine_gun");
            check(callRow(engine, "mg_43_machine_gun").cooldownTicks() == 0 && engine.activeHardware() == 0
                            && f.operation.resupplies() == initialSupplies,
                    "A real call packet during descent created hardware or spent operation resources");
            later(f, 80, () -> {
                landed(f);
                check(LibertyStratagemEngine.forOperation(f.operation.id) == engine
                                && callRow(engine, "mg_43_machine_gun").ready() && callRow(engine, "resupply").ready(),
                        "Actual landing did not unlock the preserved engine's equipped/shared calls");
                missionPacket(f, "callin", "mg_43_machine_gun");
                LibertySupportEntity delivery = f.capture.owned.stream().filter(e -> e instanceof LibertySupportEntity)
                        .map(e -> (LibertySupportEntity) e).filter(engine::owns).findFirst().orElseThrow(
                                () -> new GameTestAssertException("The accepted post-landing packet did not create a real owned support delivery"));
                long gunDeadline = f.district.getGameTime() + callRow(engine, "mg_43_machine_gun").cooldownTicks();
                check(engine.activeHardware() == 1 && gunDeadline > f.district.getGameTime(),
                        "The actual support call failed to retain its hardware and cooldown");
                later(f, 3, () -> {
                    missionPacket(f, "resupply", "");
                    long supplyDeadline = f.district.getGameTime() + callRow(engine, "resupply").cooldownTicks();
                    check(f.operation.resupplies() == initialSupplies - 1 && supplyDeadline > f.district.getGameTime(),
                            "Actual shared resupply failed to consume one charge and start its cooldown");
                    int drops = LibertyManager.snapshot(f.owner).getInt("Reinforcements");
                    Mob attacker = f.capture.owned.stream().filter(e -> e instanceof Mob && e.isAlive()
                                    && !(e instanceof LibertyCoreEntity) && !(e instanceof LibertySupportEntity)
                                    && LibertyManager.belongsTo(e, f.owner)).map(e -> (Mob) e).findFirst().orElseThrow(
                                            () -> new GameTestAssertException("The real director did not admit an owned attacker for the lethal-hit fixture"));
                    // No client is connected: acknowledge this controlled transfer through the native public hook.
                    // Unlike Forge FakePlayer (always invulnerable, die() is a no-op), this owner has unmodified
                    // ServerPlayer hurt/death. Actual native ticks have elapsed through its initial spawn shield.
                    f.owner.hasChangedDimension();
                    boolean hurt = f.owner.hurt(f.owner.damageSources().mobAttack(attacker), 10_000);
                    check(hurt && f.operation.phase() == LibertyOperation.Phase.DOWNED && f.owner.isAlive(),
                            "Native owned lethal damage did not enter the campaign's recoverable downed state; accepted=" + hurt
                                    + "; phase=" + f.operation.phase() + "; health=" + f.owner.getHealth()
                                    + "; owned=" + LibertyManager.belongsTo(attacker, f.owner));
                    later(f, 5, () -> {
                        check(LibertyStratagemEngine.forOperation(f.operation.id) == engine && engine.owns(delivery)
                                        && callRow(engine, "reinforce").ready()
                                        && engine.snapshot().stream().filter(LibertyStratagemEngine.Row::ready)
                                                .allMatch(row -> row.id().equals("reinforce")),
                                "Downed native ticks lost the engine/delivery or allowed ordinary field calls");
                        missionPacket(f, "reinforce", "");
                        check(f.operation.phase() == LibertyOperation.Phase.DROPPING
                                        && f.owner.getVehicle() instanceof LibertyTransportEntity
                                        && LibertyManager.snapshot(f.owner).getInt("Reinforcements") == drops - 1,
                                "Actual reinforcement packet did not consume one drop and create its pod");
                        later(f, 10, () -> {
                            check(f.operation.phase() == LibertyOperation.Phase.DROPPING
                                            && LibertyStratagemEngine.forOperation(f.operation.id) == engine && engine.owns(delivery)
                                            && engine.snapshot().stream().noneMatch(LibertyStratagemEngine.Row::ready),
                                    "Reinforcement pod ticks closed/reset the engine or exposed support before landing");
                            missionPacket(f, "resupply", "");
                            check(f.operation.resupplies() == initialSupplies - 1
                                            && f.district.getGameTime() + callRow(engine, "resupply").cooldownTicks() == supplyDeadline,
                                    "Rejected reinforcement-pod supply changed its charge count or cooldown deadline");
                            later(f, 80, () -> {
                                landed(f);
                                check(LibertyStratagemEngine.forOperation(f.operation.id) == engine && engine.owns(delivery)
                                                && engine.loadout().equals(loadout) && f.operation.resupplies() == initialSupplies - 1
                                                && f.district.getGameTime() + callRow(engine, "mg_43_machine_gun").cooldownTicks() == gunDeadline
                                                && f.district.getGameTime() + callRow(engine, "resupply").cooldownTicks() == supplyDeadline
                                                && !callRow(engine, "resupply").ready() && callRow(engine, "orbital_precision_strike").ready(),
                                        "Second landing reset engine/loadout/resources/cooldowns or failed to unlock unused calls");
                                missionPacket(f, "callin", "orbital_precision_strike");
                                check(callRow(engine, "orbital_precision_strike").cooldownTicks() > 0,
                                        "An actual unused call remained unusable after reinforcement landing");
                                returned(f);
                                check(LibertyStratagemEngine.forOperation(f.operation.id) == null && delivery.isRemoved()
                                                && engine.activeHardware() == 0 && engine.pendingEffects() == 0
                                                && engine.snapshot().stream().noneMatch(LibertyStratagemEngine.Row::ready),
                                        "Return left the registered call engine, support hardware or scheduled effects alive");
                                pass(helper, "liberty_campaign_native_stratagem_lifecycle");
                            });
                        });
                    });
                });
            });
        });
    }

    private static LibertyStratagemEngine.Row callRow(LibertyStratagemEngine engine, String id) {
        return engine.snapshot().stream().filter(row -> row.id().equals(id)).findFirst().orElseThrow(
                () -> new GameTestAssertException("Actual operation omitted the call row " + id));
    }

    @GameTest(template = "hoverboard_empty", timeoutTicks = 1000)
    public static void superHelldiveUsesTheRealCampaignDirectorWithBoundedResidentAndTickPressure(GameTestHelper helper) {
        if (skipAbsent(helper, "director_pressure")) return;
        Fixture f = begin(helper, "JSC_campaign10", "overpass_uprising|blitz_blockbuster|10|20");
        f.monitorPressure = true;
        later(f, 90, () -> {
            landed(f);
            check(f.operation.plan.difficulty().residentCap() == 96 && f.operation.theater.radius() == 768
                            && f.operation.plan.totalTicks() == 24000 && f.operation.objectives.isEmpty(),
                    "A selected Super Helldive used the legacy small-arena mission instead of its actual20-minute plan");
            later(f, 650, () -> {
                CompoundTag snapshot = LibertyManager.snapshot(f.owner);
                check(f.capture.enemyIds.size() >= 50 && f.peakEnemies >= 40 && snapshot.getInt("Spawned") == f.capture.enemyIds.size(),
                        "Native high-difficulty pressure never materialized or omitted real admissions from Spawned"
                                + "; admitted=" + f.capture.enemyIds.size() + "; peak=" + f.peakEnemies + "; snapshot=" + snapshot.getInt("Spawned"));
                check(f.operation.mission.completedPrimary() == 0 && !f.operation.complete(), "Passive survival fabricated campaign objective completion");
                Vec3 nextDistrict = f.owner.position().add(0, 0, -256);
                List<Mob> abandoned = f.capture.owned.stream().filter(e -> e instanceof Mob && e.isAlive()
                                && !(e instanceof LibertyCoreEntity) && !(e instanceof LibertySupportEntity)
                                && e.distanceToSqr(nextDistrict) > 160 * 160)
                        .map(e -> (Mob) e).toList();
                check(!abandoned.isEmpty(), "Director relocation fixture had no live infantry to retire");
                // Keep the real spawned actors at the old front while relocating the fixture player;
                // production ticks, retirement and replacement admissions remain unmodified.
                abandoned.forEach(mob -> { mob.setNoAi(true); mob.setNoGravity(true); mob.setInvulnerable(true); mob.setDeltaMovement(Vec3.ZERO); });
                int admitted = snapshot.getInt("Spawned"), credits = f.operation.credits();
                load(f, BlockPos.containing(nextDistrict)); position(f, nextDistrict);
                later(f, 160, () -> {
                    CompoundTag relocated = LibertyManager.snapshot(f.owner);
                    check(abandoned.stream().allMatch(Entity::isRemoved) && relocated.getInt("Retired") >= abandoned.size(),
                            "Distant persistent infantry kept occupying the resident budget after moving districts");
                    check(relocated.getInt("Spawned") > admitted && f.capture.owned.stream().anyMatch(e ->
                                    e instanceof Mob && e.isAlive() && !(e instanceof LibertyCoreEntity)
                                            && !(e instanceof LibertySupportEntity) && e.distanceToSqr(f.owner) < 80 * 80),
                            "The production director failed to re-establish real pressure near the new district");
                    check(relocated.getInt("Spawned") == f.capture.enemyIds.size()
                                    && f.operation.credits() == credits && f.operation.mission.completedPrimary() == 0,
                            "Distance retirement refunded lifetime admissions or manufactured objective/reward credit");
                    returned(f);
                    JetSetCraft.LOGGER.info("JETSETCRAFT_LIBERTY_CAMPAIGN_RUNTIME_PASS liberty_campaign_native_director_relocation");
                    pass(helper, "liberty_campaign_native_director_caps");
                });
            });
        });
    }

    @GameTest(template = "hoverboard_empty", timeoutTicks = 350)
    public static void campaignBaseCoresTakeRealTaczBulletsAndOnlyPhysicalDeathsAdvanceDemolition(GameTestHelper helper) {
        if (skipAbsent(helper, "native_base_cores")) return;
        Fixture f = begin(helper, "JSC_campaigncore", "overpass_uprising|blitz_blockbuster|4|20");
        later(f, 90, () -> {
            landed(f);
            LibertyMissionRuntime.Site base = f.operation.mission.sites().get(0);
            load(f, base.center()); position(f, base.marker().add(0, 0, 10));
            later(f, 5, () -> awaitCores(f, base, 0));
        });
    }

    private static void awaitCores(Fixture f, LibertyMissionRuntime.Site base, int attempts) {
        List<LibertyCoreEntity> cores = cores(f, base);
        if (cores.size() != base.goal()) {
            check(attempts < 24, "The campaign did not build its actual faction cores after loaded native ticks; expected=" + base.goal() + "; actual=" + cores.size());
            later(f, 5, () -> awaitCores(f, base, attempts + 1)); return;
        }
        LibertyCoreEntity victim = cores.get(0);
        check(victim.kind().equals("FABRICATOR") && victim.getMaxHealth() > 1 && base.progress() == 0,
                "The supposed faction base was a cosmetic marker or had already earned destruction credit");
        victim.setHealth(1); // Native damage/ownership fixture, not a claim about full-health weapon balance.
        later(f, 3, () -> {
            try {
                check(victim.position().distanceToSqr(new Vec3(victim.xOld, victim.yOld, victim.zOld)) < .000001,
                        "Native core interpolation history did not settle before the provider sweep");
                int ammo = rounds(f.owner.getInventory().getItem(0));
                f.capture.target = victim;
                Projectile bullet = shoot(f);
                check(ResourceLocation.fromNamespaceAndPath("tacz", "bullet").equals(ForgeRegistries.ENTITY_TYPES.getKey(bullet.getType()))
                                && LibertyManager.belongsTo(bullet, f.owner) && bullet.getOwner() == f.owner
                                && rounds(f.owner.getInventory().getItem(0)) == ammo - 1
                                && ResourceLocation.tryParse(LibertyArmory.selection(f.owner).primary()).equals(bullet.getClass().getMethod("getGunId").invoke(bullet)),
                        "Campaign firing substituted a projectile or failed real selected-weapon ammunition consumption");
                Vec3 center = victim.getBoundingBox().getCenter();
                bullet.setPos(center.x - 2, center.y, center.z); bullet.setDeltaMovement(3, 0, 0); bullet.tick();
                check(!victim.isAlive() && base.progress() == 1 && f.capture.ownerBulletDamage,
                        "Actual provider bullet sweep did not cause one owned physical core death; health=" + victim.getHealth() + "; progress=" + base.progress());
                check(!base.complete() && f.operation.mission.stage() == 0, "One of multiple cores completed the entire demolition stage");
                for (LibertyCoreEntity core : cores) if (core.isAlive())
                    check(core.hurt(f.owner.damageSources().playerAttack(f.owner), 10_000), "The owner's native damage could not destroy a remaining core");
                later(f, 3, () -> {
                    check(base.complete() && f.operation.mission.completedPrimary() == 1 && f.operation.mission.stage() == 0
                                    && f.operation.credits() > 0, "Physical base completion failed to pay once or skipped the other simultaneous primary base");
                    returned(f);
                    pass(f.helper, "liberty_campaign_native_tacz_core");
                });
            } catch (ReflectiveOperationException error) { throw new GameTestAssertException("Pinned native TaCZ API failed: " + error + "; cause=" + error.getCause()); }
        });
    }

    @GameTest(template = "hoverboard_empty", timeoutTicks = 250)
    public static void campaignLoanedFreeSprayCreatesRealFloorPaintAndDistinctTurfWithoutChangingBlocks(GameTestHelper helper) {
        if (skipAbsent(helper, "native_turf")) return;
        Fixture f = begin(helper, "JSC_campaignturf", "neon_docks|turf_liberation|1|20");
        later(f, 90, () -> {
            landed(f);
            LibertyMissionRuntime.Site turf = f.operation.mission.sites().get(0);
            load(f, turf.center()); position(f, turf.marker().add(2, 0, 2));
            later(f, 8, () -> {
                ItemStack can = f.owner.getInventory().getItem(2);
                check(can.getItem() instanceof SprayCanItem && SprayCanItem.isFreePaint(can)
                                && LibertyTaczCompat.isLoan(can, f.owner.getUUID()), "Campaign did not lend its actual free-paint spray can");
                Map<BlockPos, BlockState> before = new HashMap<>();
                for (int x = -2; x <= 8; x++) for (int z = -2; z <= 4; z++) {
                    BlockPos block = turf.center().offset(x, 0, z); before.put(block, f.district.getBlockState(block));
                }
                spray(f, turf.center());
                check(turf.progress() == 1 && !f.district.getEntitiesOfClass(GraffitiEntity.class,
                                f.owner.getBoundingBox().inflate(8), e -> LibertyManager.belongsTo(e, f.owner) && e.getFace() == Direction.UP).isEmpty(),
                        "A real free-spray action did not create owned visible floor decals and one turf cell");
                spray(f, turf.center());
                check(turf.progress() == 1, "Repainting the same actual floor patch manufactured another turf cell");
                BlockPos second = turf.center().east(4); position(f, Vec3.atCenterOf(second).add(0, 1, 2)); spray(f, second);
                check(turf.progress() == 2 && turf.paintedCells().size() == 2 && !turf.complete(), "A distinct actual spray failed to add exactly one second cell");
                check(before.entrySet().stream().allMatch(entry -> entry.getValue().equals(f.district.getBlockState(entry.getKey()))),
                        "Graffiti objective painting rewrote the supporting terrain");
                returned(f);
                pass(helper, "liberty_campaign_native_floor_turf");
            });
        });
    }

    @GameTest(template = "hoverboard_empty", timeoutTicks = 350)
    public static void acceptedHomeDefensePreservesTerrainChestOldGraffitiForeignMobsAndDeepBelongings(GameTestHelper helper) {
        if (skipAbsent(helper, "home_world_safety")) return;
        Fixture f = new Fixture(helper, "JSC_campaignhome");
        f.district = f.source;
        final int x = 40064, z = 40064;
        // Explicit disposable flat fixture: admission must inspect every primary site and extraction.
        // Prepare the land before adding old belongings and before taking the preservation snapshot.
        ChunkPos homeChunk = new ChunkPos(x >> 4, z >> 4);
        for (int cx = -9; cx <= 9; cx++) for (int cz = -9; cz <= 9; cz++) f.source.getChunk(homeChunk.x + cx, homeChunk.z + cz);
        int highest = f.source.getMinBuildHeight();
        for (int dx = -128; dx <= 128; dx++) for (int dz = -128; dz <= 128; dz++)
            highest = Math.max(highest, f.source.getHeight(Heightmap.Types.WORLD_SURFACE, x + dx, z + dz));
        // A floor at an arbitrary Y leaves taller generated surfaces in the admission heightmap.
        // Raise this disposable platform above them; its preservation volume also starts in air.
        int groundY = Math.max(100, highest + 5);
        check(groundY <= f.source.getMaxBuildHeight() - 12,
                "Home fixture has no build-height room above its loaded terrain: highest=" + highest + "; platform=" + groundY);
        BlockPos center = new BlockPos(x, groundY, z);
        for (int dx = -128; dx <= 128; dx++) for (int dz = -128; dz <= 128; dz++)
            f.source.setBlock(center.offset(dx, 0, dz), Blocks.STONE.defaultBlockState(), 3);
        load(f, center);
        f.owner.moveTo(x + .5, groundY + 1.1, z + .5, 27, 8); rememberOriginal(f);
        // Offer-seeded escort endpoints can overlap the old near-origin mural wall. Keep all
        // controls inside the home front but beyond the mapped objectives and their route detours.
        BlockPos preservedArea = center.offset(110, 0, 0); load(f, preservedArea);
        BlockPos chestPosition = preservedArea.above();
        f.source.setBlock(chestPosition, Blocks.CHEST.defaultBlockState(), 3);
        ChestBlockEntity chest = (ChestBlockEntity) f.source.getBlockEntity(chestPosition);
        check(chest != null, "Disposable home fixture chest was not created");
        ItemStack heirloom = new ItemStack(Items.WRITTEN_BOOK); heirloom.getOrCreateTag().putString("HomeHeirloom", "never borrow or overwrite");
        chest.setItem(0, heirloom); CompoundTag chestBefore = chest.saveWithFullMetadata();
        BlockPos oldSupport = preservedArea.offset(0, 2, 6);
        for (int dx = -1; dx <= 1; dx++) for (int dy = -1; dy <= 1; dy++) f.source.setBlock(oldSupport.offset(dx, dy, 0), Blocks.WHITE_CONCRETE.defaultBlockState(), 3);
        GraffitiEntity oldTag = new GraffitiEntity(ModEntities.GRAFFITI.get(), f.source); oldTag.configure(oldSupport, Direction.SOUTH, 0);
        check(f.source.addFreshEntity(oldTag) && !LibertyManager.owned(oldTag), "Existing ordinary graffiti fixture was already mission-owned");
        Mob neighbor = EntityType.PIG.create(f.source); check(neighbor != null, "Existing ordinary neighbor fixture was unavailable");
        neighbor.setNoAi(true); neighbor.setNoGravity(true); neighbor.setPersistenceRequired();
        neighbor.moveTo(preservedArea.getX() + 4.5, groundY + 1.1, preservedArea.getZ() + 5.5, 0, 0);
        float neighborHealth = neighbor.getHealth(); Vec3 neighborPosition = neighbor.position();
        check(f.source.addFreshEntity(neighbor) && !LibertyManager.owned(neighbor), "Existing ordinary neighbor was claimed by the mission");
        // Exact state references over the protected home area, including the seeded chest and wall.
        BlockVolume before = BlockVolume.capture(f.source, center.offset(-128, -4, -128), 257, 16, 257);
        var offer = homeOffer(f.owner);
        var expectedPlan = LibertyMissionPlan.defense(offer.id(), offer.sectorId(),
                com.herberto.jetsetcraft.liberty.LibertyMissionCatalog.faction(offer.factionId()), 1, 20);
        try {
            // Same-world deployment does not perform the native player registration that a
            // dimension transfer supplies. Register this no-socket owner at its prepared home
            // so real chunk movement can remove/update an existing distance-manager entry.
            f.source.addNewPlayer(f.owner); f.registeredHomeOwner = true;
            check(f.source.getEntity(f.owner.getUUID()) == f.owner && f.source.players().contains(f.owner),
                    "Home fixture owner was not registered with its native level before deployment");
            menuPacket(f.owner, "defense_deploy", offer.id() + "|1|20");
            if (LibertyManager.operation(f.owner) == null)
                throw new GameTestAssertException("Actual encoded home-defense action could not deploy on its explicit flat fixture"
                        + "; unavailable=" + LibertyManager.unavailable(f.owner) + "; armory=" + LibertyArmory.readyReason(f.owner)
                        + "; contactGuard=" + LibertyMobSafety.homeGuardReady(f.owner) + "; source=" + f.owner.blockPosition()
                        + "; platform=" + groundY + "; preservedArea=" + preservedArea + "; offer=" + offer.id()
                        + "; template=" + expectedPlan.template().id() + "; seed=" + expectedPlan.seed()
                        + homeTerrainDiagnostic(f.owner, expectedPlan));
            f.operation = LibertyManager.operation(f.owner);
            check(f.operation != null && f.operation.homeDefense && f.owner.serverLevel() == f.source
                            && offer.id().equals(LibertyCampaignData.get(f.source.getServer()).offer(f.owner.getUUID(), offer.id()).orElseThrow().id())
                            && f.operation.id.equals(LibertyCampaignData.get(f.source.getServer()).offer(f.owner.getUUID(), offer.id()).orElseThrow().claimedRun()),
                    "Home defense changed dimension or failed to claim exactly its real offer");
            check(f.operation.plan.equals(expectedPlan),
                    "Actual home deployment did not use its stable offer-selected defense family and geometry");
            check(LibertyWorld.inside(f.operation, chestPosition) && LibertyWorld.inside(f.operation, oldSupport)
                            && LibertyWorld.inside(f.operation, neighbor.blockPosition()),
                    "Protected home controls moved outside the actual defense area: margin=" + preservedArea + "; origin=" + f.operation.origin);
            check(!LibertyManager.startDefense(f.owner, offer.id() + "|1|20"), "A second home deployment reused an already claimed offer");
            load(f, f.operation.drop);
            later(f, 90, () -> {
                landed(f);
                var objective = f.operation.mission.sites().get(0); load(f, objective.center()); position(f, objective.marker().add(2, 0, 2));
                later(f, 5, () -> {
                    check(neighbor.tickCount > 0 && oldTag.tickCount > 0
                                    && f.source.isPositionEntityTicking(neighbor.blockPosition()) && f.source.isPositionEntityTicking(oldTag.blockPosition()),
                            "Protected home controls did not retain native ticking at the loaded margin: " + preservedArea);
                    f.owner.getInventory().selected = 2;
                    BlockPos support = objective.center(); int previousProgress = objective.progress();
                    BlockHitResult hit = new BlockHitResult(Vec3.atCenterOf(support).add(0, .5, 0), Direction.UP, support, false);
                    PlayerInteractEvent.RightClickBlock event = new PlayerInteractEvent.RightClickBlock(f.owner, InteractionHand.MAIN_HAND, support, hit);
                    MinecraftForge.EVENT_BUS.post(event);
                    int expectedProgress = previousProgress + (objective.kind()
                            == com.herberto.jetsetcraft.liberty.LibertyMissionCatalog.ObjectiveKind.TERRITORY ? 1 : 0);
                    check(event.isCanceled() && event.getCancellationResult().consumesAction() && objective.progress() == expectedProgress
                                    && f.capture.owned.stream().anyMatch(e -> e instanceof GraffitiEntity && !e.isRemoved()),
                            "Home right-click failed its actual overlay or credited paint to an unrelated objective kind");
                    check(!neighbor.hurt(f.owner.damageSources().playerAttack(f.owner), 1000) && neighbor.getHealth() == neighborHealth,
                            "Home-defense player damage reached an unrelated existing mob");
                    var base = f.operation.mission.sites().stream().filter(s -> s.optional()
                            && s.kind() == com.herberto.jetsetcraft.liberty.LibertyMissionCatalog.ObjectiveKind.DEMOLITION).findFirst().orElseThrow();
                    load(f, base.center()); position(f, base.marker().add(0, 0, 6));
                    later(f, 5, () -> homeCore(f, base, before, chestPosition, chestBefore, oldTag, oldSupport, neighbor, neighborHealth, neighborPosition, offer.id(), 0));
                });
            });
        } catch (RuntimeException | Error error) { oldTag.discard(); neighbor.discard(); cleanup(f); throw error; }
    }

    /** No-socket players drop outbound status packets; inspect the same read-only admission gates on failure. */
    private static String homeTerrainDiagnostic(ServerPlayer player, LibertyMissionPlan plan) {
        try {
            var originMethod = LibertyManager.class.getDeclaredMethod("homeOrigin", ServerPlayer.class);
            originMethod.setAccessible(true); BlockPos origin = (BlockPos) originMethod.invoke(null, player);
            if (origin == null) return "; homeLanding=unavailable";
            var readyMethod = LibertyManager.class.getDeclaredMethod("homePlanReady", ServerLevel.class, BlockPos.class, LibertyMissionPlan.class);
            readyMethod.setAccessible(true);
            return "; homeLanding=" + origin + "; homePlanReady=" + readyMethod.invoke(null, player.serverLevel(), origin, plan);
        } catch (ReflectiveOperationException error) { return "; homeAdmissionDiagnostic=" + error; }
    }

    private static void homeCore(Fixture f, LibertyMissionRuntime.Site base, BlockVolume before, BlockPos chestPosition,
                                 CompoundTag chestBefore, GraffitiEntity oldTag, BlockPos oldSupport, Mob neighbor,
                                 float neighborHealth, Vec3 neighborPosition, UUID offerId, int attempts) {
        List<LibertyCoreEntity> live = cores(f, base);
        if (live.isEmpty()) {
            check(attempts < 24, "Accepted home defense did not create its own native optional base cores on loaded terrain");
            later(f, 5, () -> homeCore(f, base, before, chestPosition, chestBefore, oldTag, oldSupport, neighbor, neighborHealth, neighborPosition, offerId, attempts + 1)); return;
        }
        check(live.get(0).hurt(f.owner.damageSources().playerAttack(f.owner), 10_000) && base.progress() == 1,
                "Home native core death failed to credit its actual owned base");
        UUID mission = f.operation.id;
        returned(f);
        check(before.matches(f.source) && chestBefore.equals(f.source.getBlockEntity(chestPosition).saveWithFullMetadata())
                        && neighbor.isAlive() && neighbor.getHealth() == neighborHealth && neighbor.position().equals(neighborPosition)
                        && !LibertyManager.owned(neighbor) && !oldTag.isRemoved() && !LibertyManager.owned(oldTag)
                        && oldSupport.equals(oldTag.getSupportPos()) && oldTag.getFace() == Direction.SOUTH && oldTag.getVariant() == 0,
                "Home defense altered protected blocks, chest inventory/NBT, an old mural or an unrelated native mob");
        var campaign = LibertyCampaignData.get(f.source.getServer());
        var retry = campaign.offer(f.owner.getUUID(), offerId).orElseThrow();
        check(campaign.settled(mission) && retry.homeWorld() && retry.claimedRun() == null
                        && campaign.offers(f.owner.getUUID()).stream().filter(LibertyCampaignData.DefenseOffer::homeWorld).count() == 1,
                "Failed home defense duplicated or consumed its retry offer instead of unclaiming the same one");
        long revision = campaign.revision(); LibertyManager.recover(f.owner);
        check(campaign.revision() == revision && !LibertyManager.hasReturn(f.owner), "Repeating home recovery settled the same result again");
        oldTag.discard(); neighbor.discard();
        pass(f.helper, "liberty_campaign_native_home_preservation");
    }

    @GameTest(template = "hoverboard_empty", timeoutTicks = 250)
    public static void realShipReviewRedeploymentAndReturnKeepTheOriginalWorldAndNestedInventory(GameTestHelper helper) {
        if (skipAbsent(helper, "ship_redeploy")) return;
        Fixture f = new Fixture(helper, "JSC_campaignship");
        try {
            menuPacket(f.owner, "ship", "");
            check(LibertyManager.inShip(f.owner), "Real encoded ship entry failed before deployment");
            f.district = f.owner.serverLevel(); load(f, f.owner.blockPosition());
            CompoundTag firstJournal = f.owner.getPersistentData().getCompound(Player.PERSISTED_NBT_TAG).getCompound(LibertyManager.RETURN_KEY).copy();
            check(LibertyManager.inShip(f.owner) && LibertyWorld.isLiberty(f.owner.level()) && !firstJournal.isEmpty()
                            && LibertyTaczCompat.isLoan(f.owner.getInventory().getItem(4), f.owner.getUUID()),
                    "Ship entry did not preserve the actual source return journal and lend its command radio");
            LibertyManager.open(f.owner);
            CompoundTag review = LibertyManager.snapshot(f.owner);
            check(review.getBoolean("InShip") && review.getBoolean("CanManage") && review.getCompound("Armory").getBoolean("Ready"),
                    "Ship review did not expose usable actual predeployment loadout controls");
            later(f, 5, () -> {
                menuPacket(f.owner, "campaign_deploy", "blackout_borough|lair_documentary|2|25");
                check(LibertyManager.operation(f.owner) != null, "Actual encoded ship-to-campaign redeployment failed");
                f.operation = LibertyManager.operation(f.owner);
                check(f.operation != null && f.operation.campaign() && !LibertyManager.inShip(f.owner)
                                && RushReturnState.hasJournal(f.owner, LibertyManager.RETURN_KEY)
                                && !f.owner.getPersistentData().getCompound(Player.PERSISTED_NBT_TAG).getCompound(LibertyManager.RETURN_KEY).isEmpty(),
                        "Redeployment lost its original source checkpoint or left a simultaneous hub session");
                load(f, f.operation.drop);
                later(f, 90, () -> {
                    landed(f); returned(f);
                    pass(helper, "liberty_campaign_native_ship_redeploy_return");
                });
            });
        } catch (RuntimeException | Error error) { cleanup(f); throw error; }
    }

    private record BlockVolume(BlockPos min, int width, int height, int depth, BlockState[] states) {
        static BlockVolume capture(ServerLevel level, BlockPos min, int width, int height, int depth) {
            BlockState[] states = new BlockState[width * height * depth]; int index = 0;
            BlockPos.MutableBlockPos at = new BlockPos.MutableBlockPos();
            for (int x = 0; x < width; x++) for (int z = 0; z < depth; z++) for (int y = 0; y < height; y++)
                states[index++] = level.getBlockState(at.set(min.getX() + x, min.getY() + y, min.getZ() + z));
            return new BlockVolume(min.immutable(), width, height, depth, states);
        }
        boolean matches(ServerLevel level) {
            int index = 0; BlockPos.MutableBlockPos at = new BlockPos.MutableBlockPos();
            for (int x = 0; x < width; x++) for (int z = 0; z < depth; z++) for (int y = 0; y < height; y++)
                if (!states[index++].equals(level.getBlockState(at.set(min.getX() + x, min.getY() + y, min.getZ() + z)))) return false;
            return true;
        }
    }

    private static void spray(Fixture f, BlockPos support) {
        f.owner.getInventory().selected = 2;
        BlockHitResult hit = new BlockHitResult(Vec3.atCenterOf(support).add(0, .5, 0), Direction.UP, support, false);
        ItemStack can = f.owner.getMainHandItem();
        check(can.getItem().useOn(new UseOnContext(f.owner, InteractionHand.MAIN_HAND, hit)).consumesAction(), "Native spray action was rejected");
    }
    private static List<LibertyCoreEntity> cores(Fixture f, LibertyMissionRuntime.Site site) {
        return f.district.getEntitiesOfClass(LibertyCoreEntity.class, f.owner.getBoundingBox().inflate(55), core -> core.isAlive()
                && LibertyManager.belongsTo(core, f.owner)
                && core.getPersistentData().getCompound(LibertyManager.OWNED).getString("Core").startsWith(site.id() + "_core_"));
    }
    private static Fixture begin(GameTestHelper helper, String name, String selection) {
        return begin(helper, name, selection, false);
    }
    private static Fixture begin(GameTestHelper helper, String name, String selection, boolean nativeDamageOwner) {
        Fixture f = new Fixture(helper, name, nativeDamageOwner);
        try {
            menuPacket(f.owner, "campaign_deploy", selection);
            check(LibertyManager.operation(f.owner) != null, "Actual encoded campaign entry failed: " + LibertyManager.snapshot(f.owner).getString("Status")
                    + "; provider=" + LibertyTaczCompat.status() + "; armory=" + LibertyArmory.readyReason(f.owner));
            f.district = f.owner.serverLevel(); f.operation = LibertyManager.operation(f.owner);
            check(f.operation != null && f.operation.campaign() && !f.operation.homeDefense && LibertyWorld.isLiberty(f.district)
                            && f.owner.getVehicle() instanceof LibertyTransportEntity pod && !pod.ship()
                            && LibertyTaczCompat.isLoan(f.owner.getInventory().getItem(0), f.owner.getUUID())
                            && LibertyTaczCompat.isLoan(f.owner.getInventory().getItem(1), f.owner.getUUID()),
                    "Campaign entry did not create a genuine pod and live owned weapon loadout");
            load(f, f.operation.drop);
            return f;
        } catch (RuntimeException | Error error) { cleanup(f); throw error; }
    }
    private static void landed(Fixture f) {
        check(LibertyManager.operation(f.owner) == f.operation && f.operation.phase() == LibertyOperation.Phase.ACTIVE
                        && !f.owner.isPassenger() && f.owner.position().distanceToSqr(Vec3.atCenterOf(f.operation.drop).add(0, .6, 0)) < 4,
                "Ordinary native ticks did not complete the selected campaign's real pod descent");
    }
    private static void load(Fixture f, BlockPos center) {
        ChunkPos chunk = new ChunkPos(center); f.chunks.add(chunk);
        f.district.getChunkSource().addRegionTicket(TICKET, chunk, 4, f.owner.getUUID());
        for (int x = -3; x <= 3; x++) for (int z = -3; z <= 3; z++) f.district.getChunk(chunk.x + x, chunk.z + z);
    }
    private static void position(Fixture f, Vec3 point) {
        LibertyManager.movementDiscontinuity(f.owner);
        f.owner.moveTo(point.x, point.y, point.z, 0, 0); f.owner.setDeltaMovement(Vec3.ZERO);
        f.district.getChunkSource().move(f.owner);
    }
    private static void later(Fixture f, int delay, Runnable task) {
        check(f.next == null, "Campaign fixture attempted to replace a pending assertion");
        f.nextAt = f.ticks + Math.max(1, delay); f.next = task;
    }
    private static void tick(Fixture f) {
        if (f.closed) return;
        try {
            f.ticks++;
            if (f.district != null) for (ChunkPos chunk : f.chunks) f.district.getChunkSource().addRegionTicket(TICKET, chunk, 4, f.owner.getUUID());
            if (f.monitorPressure && f.operation != null && f.ticks % 10 == 0) {
                CompoundTag state = LibertyManager.snapshot(f.owner); int enemies = state.getInt("Enemies"), spawned = state.getInt("Spawned");
                f.peakEnemies = Math.max(f.peakEnemies, enemies);
                check(enemies <= f.operation.plan.difficulty().residentCap() && spawned <= f.operation.plan.difficulty().allowanceAt(f.operation.ticks(), 20)
                                && f.capture.admissions.values().stream().allMatch(count -> count <= 8),
                        "Native director breached resident, cumulative or per-tick budget; enemies=" + enemies + "; spawned=" + spawned
                                + "; allowance=" + f.operation.plan.difficulty().allowanceAt(f.operation.ticks(), 20) + "; births=" + f.capture.admissions);
            }
            if (f.next != null && f.ticks >= f.nextAt) { Runnable task = f.next; f.next = null; task.run(); }
        } catch (RuntimeException | Error error) { cleanup(f); throw error; }
    }
    private static void returned(Fixture f) {
        check(LibertyManager.leave(f.owner, false, "Campaign native fixture return"), "Campaign could not settle its actual early return");
        check(!LibertyManager.active(f.owner) && !LibertyManager.hasReturn(f.owner) && f.owner.serverLevel() == f.source
                        && f.owner.position().distanceToSqr(f.originalPosition) < .0001
                        && f.originalInventory.equals(f.owner.getInventory().save(new ListTag()))
                        && f.originalRide.equals(f.owner.getCapability(JetSetDataProvider.CAPABILITY).resolve().orElseThrow().save())
                        && f.owner.gameMode.getGameModeForPlayer() == GameType.ADVENTURE && f.owner.experienceLevel == 17
                        && f.owner.totalExperience == 321 && f.owner.experienceProgress == .375f
                        && f.owner.getHealth() == 17 && f.owner.getAbsorptionAmount() == 2
                        && f.owner.getFoodData().getFoodLevel() == 13 && f.owner.getFoodData().getSaturationLevel() == 4
                        && f.capture.owned.stream().allMatch(Entity::isRemoved),
                "Campaign return retained a native actor/decal/projectile, lost belongings or missed the source world");
        cleanup(f);
    }
    private static void rememberOriginal(Fixture f) {
        f.originalPosition = f.owner.position(); f.originalInventory = f.owner.getInventory().save(new ListTag());
        f.originalRide = f.owner.getCapability(JetSetDataProvider.CAPABILITY).resolve().orElseThrow().save();
    }
    /** The production codec/allowlist/nonce path at the current real server tick; no socket is simulated. */
    private static void menuPacket(ServerPlayer player, String action, String value) {
        LibertyManager.open(player);
        long revision = LibertyManager.snapshot(player).getLong("Revision");
        FriendlyByteBuf bytes = new FriendlyByteBuf(Unpooled.buffer());
        try {
            C2SLibertyActionPacket.encode(new C2SLibertyActionPacket(action, value, null, revision), bytes);
            C2SLibertyActionPacket decoded = C2SLibertyActionPacket.decode(bytes);
            check(bytes.readableBytes() == 0 && decoded.action().equals(action) && decoded.value().equals(value)
                    && decoded.missionId() == null && decoded.revision() == revision, "Native campaign packet codec lost its actual menu intent");
            C2SLibertyActionPacket.handleForPlayer(decoded, player);
        } finally { bytes.release(); }
    }
    /** Actual current-operation nonce and production codec, at naturally separated server ticks. */
    private static void missionPacket(Fixture fixture, String action, String value) {
        CompoundTag current = LibertyManager.snapshot(fixture.owner);
        check(current.hasUUID("Mission") && current.getUUID("Mission").equals(fixture.operation.id),
                "The fixture lost its actual active operation before sending a field action");
        FriendlyByteBuf bytes = new FriendlyByteBuf(Unpooled.buffer());
        try {
            C2SLibertyActionPacket.encode(new C2SLibertyActionPacket(action, value, current.getUUID("Mission"), current.getLong("Revision")), bytes);
            C2SLibertyActionPacket decoded = C2SLibertyActionPacket.decode(bytes);
            check(bytes.readableBytes() == 0 && decoded.action().equals(action) && decoded.value().equals(value)
                            && fixture.operation.id.equals(decoded.missionId()) && decoded.revision() == current.getLong("Revision"),
                    "Native field-action codec lost its action or actual mission/revision");
            C2SLibertyActionPacket.handleForPlayer(decoded, fixture.owner);
        } finally { bytes.release(); }
    }
    private static void cleanup(Fixture f) {
        if (f.closed) return; f.closed = true;
        MinecraftForge.EVENT_BUS.unregister(f.capture);
        try {
            if (LibertyManager.active(f.owner) || LibertyManager.hasReturn(f.owner)) LibertyManager.leave(f.owner, false, "Campaign fixture cleanup");
            LibertyManager.forget(f.owner);
        } finally {
            if (f.district != null) for (ChunkPos chunk : f.chunks) f.district.getChunkSource().removeRegionTicket(TICKET, chunk, 4, f.owner.getUUID());
            if (f.registeredHomeOwner) {
                f.owner.serverLevel().removePlayerImmediately(f.owner, Entity.RemovalReason.DISCARDED);
                f.registeredHomeOwner = false;
            } else if (f.nativeDamageOwner) f.owner.discard();
        }
    }
    /** Real native damage/death, with only outbound socket delivery omitted by this disposable fixture. */
    private static ServerPlayer nativeDamagePlayer(ServerLevel source, GameProfile profile) {
        ServerPlayer player = new ServerPlayer(source.getServer(), source, profile);
        player.connection = new ServerGamePacketListenerImpl(source.getServer(), new Connection(PacketFlow.SERVERBOUND), player) {
            @Override public void send(Packet<?> packet) { }
            @Override public void send(Packet<?> packet, PacketSendListener callback) { }
        };
        return player;
    }
    private static Projectile shoot(Fixture f) throws ReflectiveOperationException {
        f.owner.getInventory().selected = 0; ItemStack gun = f.owner.getMainHandItem();
        Class<?> api = Class.forName("com.tacz.guns.api.entity.IGunOperator");
        Object operator = api.getMethod("fromLivingEntity", LivingEntity.class).invoke(null, f.owner);
        api.getMethod("initialData").invoke(operator); api.getMethod("draw", Supplier.class).invoke(operator, (Supplier<ItemStack>) () -> gun);
        Object data = api.getMethod("getDataHolder").invoke(operator); data.getClass().getField("drawTimestamp").setLong(data, -1L);
        int before = f.capture.shots.size();
        Object result = api.getMethod("shoot", Supplier.class, Supplier.class).invoke(operator, (Supplier<Float>) () -> 0F, (Supplier<Float>) () -> -90F);
        check(result instanceof Enum<?> value && value.name().equals("SUCCESS"), "Real campaign gun did not fire: " + result);
        List<Projectile> shots = f.capture.shots.subList(before, f.capture.shots.size());
        check(shots.size() == 1, "One actual campaign gun shot did not spawn one provider projectile");
        return shots.get(0);
    }
    private static int rounds(ItemStack stack) throws ReflectiveOperationException {
        Class<?> api = Class.forName("com.tacz.guns.api.item.IGun");
        return ((Number) api.getMethod("getCurrentAmmoCount", ItemStack.class).invoke(stack.getItem(), stack)).intValue()
                + ((Boolean) api.getMethod("hasBulletInBarrel", ItemStack.class).invoke(stack.getItem(), stack) ? 1 : 0);
    }
    public static final class CampaignRuntimeCapture {
        final ServerPlayer owner;
        final List<Entity> owned = new ArrayList<>();
        final Set<UUID> enemyIds = new HashSet<>();
        final Map<Long, Integer> admissions = new HashMap<>();
        final List<Projectile> shots = new ArrayList<>();
        LivingEntity target;
        boolean ownerBulletDamage;
        CampaignRuntimeCapture(ServerPlayer owner) { this.owner = owner; }
        @SubscribeEvent(priority = EventPriority.LOWEST)
        public void join(EntityJoinLevelEvent event) {
            Entity entity = event.getEntity();
            if (!LibertyManager.belongsTo(entity, owner)) return;
            owned.add(entity);
            if (entity instanceof Mob && !(entity instanceof LibertyCoreEntity) && !(entity instanceof LibertySupportEntity)
                    && enemyIds.add(entity.getUUID())) admissions.merge(entity.level().getGameTime(), 1, Integer::sum);
            if (entity instanceof Projectile projectile && projectile.getClass().getName().equals("com.tacz.guns.entity.EntityKineticBullet")) shots.add(projectile);
        }
        @SubscribeEvent(priority = EventPriority.LOWEST, receiveCanceled = true)
        public void damage(LivingAttackEvent event) {
            if (event.getEntity() == target && event.getSource().getEntity() == owner && event.getSource().getDirectEntity() instanceof Projectile
                    && !event.isCanceled()) ownerBulletDamage = true;
        }
    }
    private static boolean skipAbsent(GameTestHelper helper, String test) {
        if (LibertyTaczCompat.available()) return false;
        check(!Boolean.getBoolean("jetsetcraft.libertyPackExpected"), "Required campaign provider lane is unavailable: " + LibertyTaczCompat.status());
        JetSetCraft.LOGGER.info("JETSETCRAFT_LIBERTY_CAMPAIGN_RUNTIME_SKIP {} optional-provider-or-pack-absent", test); helper.succeed(); return true;
    }
    /** Explicit failed-result fixture transactions; no reflected score fields or shared clock compression. */
    static LibertyCampaignData.DefenseOffer homeOffer(ServerPlayer player) {
        var campaign = LibertyCampaignData.get(player.getServer());
        var existing = campaign.offers(player.getUUID()).stream().filter(LibertyCampaignData.DefenseOffer::homeWorld).findFirst();
        if (existing.isPresent()) return existing.get();
        for (int i = 0; i < 2; i++) check(campaign.settle(new LibertyCampaignData.Result(UUID.randomUUID(), player.getUUID(),
                "neon_docks", "terminid", 10, 0, 3, 0, 0, false, null)), "Could not establish the explicit failed-expedition fixture outcome");
        var regional = campaign.offers(player.getUUID()).stream().filter(offer -> !offer.homeWorld()
                && offer.sectorId().equals("neon_docks") && offer.claimedRun() == null).findFirst().orElseThrow();
        UUID failedDefense = UUID.randomUUID();
        check(campaign.claimDefense(player.getUUID(), regional.id(), failedDefense, campaign.revision())
                        && campaign.settle(new LibertyCampaignData.Result(failedDefense, player.getUUID(), "neon_docks", "terminid",
                        1, 0, 3, 0, 0, false, regional.id())), "The legitimate failed regional-defense transaction did not create a home offer");
        return campaign.offers(player.getUUID()).stream().filter(LibertyCampaignData.DefenseOffer::homeWorld).findFirst().orElseThrow();
    }
    private static void check(boolean condition, String message) { if (!condition) throw new GameTestAssertException(message); }
    private static void pass(GameTestHelper helper, String name) { JetSetCraft.LOGGER.info("JETSETCRAFT_LIBERTY_CAMPAIGN_RUNTIME_PASS {}", name); helper.succeed(); }
    private LibertyCampaignRuntimeGameTests() { }
}
