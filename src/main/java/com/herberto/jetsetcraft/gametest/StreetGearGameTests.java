package com.herberto.jetsetcraft.gametest;

import com.herberto.jetsetcraft.JetSetCraft;
import com.herberto.jetsetcraft.blockentity.BoomboxBlockEntity;
import com.herberto.jetsetcraft.gang.GangMemberState;
import com.herberto.jetsetcraft.gang.HeadGangTargetResolver;
import com.herberto.jetsetcraft.mob.MobRideRig;
import com.herberto.jetsetcraft.mob.MobRideRigResolver;
import com.herberto.jetsetcraft.mob.MobStreetGear;
import com.herberto.jetsetcraft.mob.StreetGearAcquisition;
import com.herberto.jetsetcraft.movement.RideStyle;
import com.herberto.jetsetcraft.registry.ModBlocks;
import com.herberto.jetsetcraft.registry.ModItems;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestAssertException;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.nbt.CompoundTag;
import com.mojang.authlib.GameProfile;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.monster.Slime;
import net.minecraft.world.entity.monster.Spider;
import net.minecraft.world.entity.monster.Zombie;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.AABB;
import net.minecraftforge.common.util.FakePlayerFactory;
import net.minecraftforge.gametest.GameTestHolder;
import net.minecraftforge.gametest.PrefixGameTestTemplate;

import java.nio.charset.StandardCharsets;
import java.util.UUID;

/** Real Forge acceptance for additive, same-entity, equipment-bound mob Street Gear. */
@GameTestHolder(JetSetCraft.MOD_ID)
@PrefixGameTestTemplate(false)
public final class StreetGearGameTests {
    @GameTest(template = "hoverboard_empty", timeoutTicks = 80)
    public static void streetGearPreservesSourceMobAndPhysicalItem(GameTestHelper helper) {
        Zombie zombie = EntityType.ZOMBIE.create(helper.getLevel());
        if (zombie == null) throw new GameTestAssertException("Could not construct vanilla zombie");
        BlockPos feet = helper.absolutePos(new BlockPos(1, 1, 1));
        zombie.moveTo(feet.getX() + 0.5D, feet.getY(), feet.getZ() + 0.5D, 0.0F, 0.0F);
        helper.getLevel().addFreshEntity(zombie);

        UUID originalUuid = zombie.getUUID();
        EntityType<?> originalType = zombie.getType();
        ItemStack offered = new ItemStack(ModItems.INLINE_SKATES.get());
        MobStreetGear.EquipResult result = MobStreetGear.equip(zombie, offered,
                StreetGearAcquisition.COMMAND, false);
        MobStreetGear.Snapshot snapshot = MobStreetGear.snapshot(zombie);
        if (!result.equipped() || !snapshot.equipped() || snapshot.style() != RideStyle.INLINE
                || snapshot.rig() != MobRideRig.BIPED || snapshot.acquisition() != StreetGearAcquisition.COMMAND) {
            throw new GameTestAssertException("Street Gear did not attach with the expected physical stack/rig/source");
        }
        if (zombie.getType() != originalType || !zombie.getUUID().equals(originalUuid)) {
            throw new GameTestAssertException("Street Gear replaced or re-identified the source-owned mob");
        }
        GangMemberState.Snapshot gangSnapshot = GangMemberState.snapshot(zombie);
        if (!gangSnapshot.present()
                || !gangSnapshot.gangId().equals(new ResourceLocation("jetsetcraft", "dead_beat"))
                || gangSnapshot.ephemeral() || gangSnapshot.inChallenge()) {
            throw new GameTestAssertException("Street Gear did not persist the canonical Dead Beat gang identity");
        }
        if (snapshot.stack().getCount() != 1 || offered.getCount() != 1) {
            throw new GameTestAssertException("Street Gear persistence did not normalize/copy exactly one physical item");
        }

        CompoundTag serialized = new CompoundTag();
        zombie.saveWithoutId(serialized);
        Zombie restored = EntityType.ZOMBIE.create(helper.getLevel());
        if (restored == null) throw new GameTestAssertException("Could not construct restored zombie");
        restored.load(serialized);
        MobStreetGear.Snapshot restoredSnapshot = MobStreetGear.snapshot(restored);
        if (!restoredSnapshot.equipped() || restoredSnapshot.style() != RideStyle.INLINE
                || restoredSnapshot.rig() != MobRideRig.BIPED) {
            throw new GameTestAssertException("Street Gear did not survive the real entity NBT save/load path");
        }

        ItemStack recovered = MobStreetGear.unequip(zombie);
        if (recovered.isEmpty() || recovered.getItem() != ModItems.INLINE_SKATES.get()
                || MobStreetGear.hasStoredState(zombie) || zombie.getType() != originalType
                || !zombie.getUUID().equals(originalUuid)) {
            throw new GameTestAssertException("Unequipping did not return the physical item and original mob unchanged");
        }
        if (GangMemberState.snapshot(zombie).present()) {
            throw new GameTestAssertException("Removing Street Gear did not de-gangify the original source mob");
        }

        Spider spider = EntityType.SPIDER.create(helper.getLevel());
        Slime slime = EntityType.SLIME.create(helper.getLevel());
        if (spider == null || slime == null || MobRideRigResolver.resolve(spider) != MobRideRig.MULTI_LEG
                || MobRideRigResolver.resolve(slime) != MobRideRig.BODY_CONTACT) {
            throw new GameTestAssertException("Species-aware rig resolver regressed for vanilla stress-test anatomies");
        }

        HeadGangTargetResolver.Target zombieHead = HeadGangTargetResolver.resolve(new ItemStack(Items.ZOMBIE_HEAD))
                .orElseThrow(() -> new GameTestAssertException("Vanilla zombie head did not resolve a gang target"));
        if (!zombieHead.entityId().equals(new ResourceLocation("minecraft", "zombie"))
                || !zombieHead.gangId().equals(new ResourceLocation("jetsetcraft", "dead_beat"))) {
            throw new GameTestAssertException("Vanilla zombie head resolved the wrong entity/gang identity");
        }

        ItemStack adapterHead = new ItemStack(Items.PLAYER_HEAD);
        adapterHead.getOrCreateTag().putString(HeadGangTargetResolver.TARGET_ENTITY_KEY, "minecraft:bee");
        HeadGangTargetResolver.Target beeHead = HeadGangTargetResolver.resolve(adapterHead)
                .orElseThrow(() -> new GameTestAssertException("Explicit head compatibility metadata did not resolve"));
        if (!beeHead.entityId().equals(new ResourceLocation("minecraft", "bee"))
                || beeHead.source() != HeadGangTargetResolver.ResolutionSource.EXPLICIT_METADATA) {
            throw new GameTestAssertException("Explicit head compatibility metadata resolved incorrectly");
        }

        if (HeadGangTargetResolver.resolve(new ItemStack(Items.DIAMOND)).isPresent()) {
            throw new GameTestAssertException("Non-head item was incorrectly accepted as a gang target");
        }

        // Exercise the real physical Boombox block entity and its no-cooldown gang-session state machine.
        BlockPos boomboxRelative = new BlockPos(3, 1, 3);
        helper.setBlock(boomboxRelative, ModBlocks.BOOMBOX.get());
        BlockPos boomboxWorld = helper.absolutePos(boomboxRelative);
        if (!(helper.getLevel().getBlockEntity(boomboxWorld) instanceof BoomboxBlockEntity boombox)) {
            throw new GameTestAssertException("Placed Boombox did not create its authoritative block entity");
        }
        if (!boombox.setTarget(new ItemStack(Items.ZOMBIE_HEAD))) {
            throw new GameTestAssertException("Boombox refused a vanilla Zombie Head target");
        }
        HeadGangTargetResolver.Target tuned = boombox.resolvedTarget()
                .orElseThrow(() -> new GameTestAssertException("Boombox lost its tuned Zombie target"));
        if (!tuned.entityId().equals(new ResourceLocation("minecraft", "zombie"))
                || !tuned.gangId().equals(new ResourceLocation("jetsetcraft", "dead_beat"))) {
            throw new GameTestAssertException("Boombox tuned to the wrong source entity/gang");
        }

        UUID playerUuid = UUID.nameUUIDFromBytes((JetSetCraft.MOD_ID + ":gametest:boombox")
                .getBytes(StandardCharsets.UTF_8));
        ServerPlayer player = FakePlayerFactory.get(helper.getLevel(), new GameProfile(playerUuid, "JSC_boombox"));
        boombox.toggleChallenge(player);
        UUID firstChallenge = boombox.challengeId();
        if (!boombox.isChallengeActive() || firstChallenge == null || boombox.plannedActors() < 1
                || !new ResourceLocation("minecraft", "zombie").equals(boombox.challengeEntityId())
                || !new ResourceLocation("jetsetcraft", "dead_beat").equals(boombox.challengeGangId())) {
            throw new GameTestAssertException("Zombie Head did not start a real Dead Beat Boombox session");
        }
        boombox.cancelChallenge(true);
        if (boombox.isChallengeActive()) {
            throw new GameTestAssertException("Cancelling a Boombox session did not clear authoritative state");
        }
        boombox.toggleChallenge(player);
        if (!boombox.isChallengeActive() || boombox.challengeId() == null
                || firstChallenge.equals(boombox.challengeId())) {
            throw new GameTestAssertException("Boombox could not start a fresh session immediately after cancel");
        }

        // Breaking an active Boombox must clean up the session without fighting the replacement block state,
        // and the physical target head must still be returned to the world rather than duplicated or deleted.
        helper.setBlock(boomboxRelative, Blocks.AIR);
        if (!helper.getLevel().getBlockState(boomboxWorld).isAir()) {
            throw new GameTestAssertException("Breaking an active Boombox caused it to survive/reappear");
        }
        AABB droppedTargetArea = new AABB(boomboxWorld).inflate(1.25D);
        boolean returnedPhysicalTarget = !helper.getLevel().getEntitiesOfClass(ItemEntity.class, droppedTargetArea,
                item -> item.getItem().is(Items.ZOMBIE_HEAD)).isEmpty();
        if (!returnedPhysicalTarget) {
            throw new GameTestAssertException("Breaking an active Boombox did not return its physical target head");
        }

        helper.setBlock(boomboxRelative, ModBlocks.BOOMBOX.get());
        if (!(helper.getLevel().getBlockEntity(boomboxWorld) instanceof BoomboxBlockEntity replacementBoombox)) {
            throw new GameTestAssertException("Re-placed Boombox did not restore its block entity");
        }
        if (!replacementBoombox.setTarget(new ItemStack(Items.ZOMBIE_HEAD))) {
            throw new GameTestAssertException("Re-placed Boombox refused a valid target");
        }

        ItemStack recoveredHead = replacementBoombox.removeTarget();
        if (recoveredHead.getItem() != Items.ZOMBIE_HEAD || !replacementBoombox.targetStack().isEmpty()) {
            throw new GameTestAssertException("Boombox target slot did not return its physical mob head intact");
        }

        System.out.println("JETSETCRAFT_GAMETEST_PASS street_gear");
        helper.succeed();
    }

    private StreetGearGameTests() {}
}
