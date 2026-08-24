package com.herberto.jetsetcraft.gametest;

import com.herberto.jetsetcraft.JetSetCraft;
import com.herberto.jetsetcraft.gang.HeadGangTargetResolver;
import com.herberto.jetsetcraft.mob.MobRideRig;
import com.herberto.jetsetcraft.mob.MobRideRigResolver;
import com.herberto.jetsetcraft.mob.MobStreetGear;
import com.herberto.jetsetcraft.mob.StreetGearAcquisition;
import com.herberto.jetsetcraft.movement.RideStyle;
import com.herberto.jetsetcraft.registry.ModItems;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestAssertException;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.monster.Slime;
import net.minecraft.world.entity.monster.Spider;
import net.minecraft.world.entity.monster.Zombie;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraftforge.gametest.GameTestHolder;
import net.minecraftforge.gametest.PrefixGameTestTemplate;

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

        System.out.println("JETSETCRAFT_GAMETEST_PASS street_gear");
        helper.succeed();
    }

    private StreetGearGameTests() {}
}
