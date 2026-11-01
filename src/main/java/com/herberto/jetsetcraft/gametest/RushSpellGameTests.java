package com.herberto.jetsetcraft.gametest;

import com.herberto.jetsetcraft.JetSetCraft;
import com.herberto.jetsetcraft.compat.RushSpellCompat;
import com.herberto.jetsetcraft.rush.RivalRushManager;
import com.herberto.jetsetcraft.rush.RushReturnState;
import com.mojang.authlib.GameProfile;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestAssertException;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.nbt.ListTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.Arrow;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.GameType;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.util.FakePlayerFactory;
import net.minecraftforge.event.entity.EntityJoinLevelEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.gametest.GameTestHolder;
import net.minecraftforge.gametest.PrefixGameTestTemplate;
import net.minecraftforge.registries.ForgeRegistries;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/** Real native projectile proof; deliberately does not claim spellbook casting or capability rollback. */
@GameTestHolder(JetSetCraft.MOD_ID)
@PrefixGameTestTemplate(false)
public final class RushSpellGameTests {
    @GameTest(template = "hoverboard_empty", timeoutTicks = 350)
    public static void optionalFireboltUsesProviderCollisionAndRushOwnership(GameTestHelper helper) {
        ServerPlayer owner = player(helper, "relay_owner");
        ServerPlayer foreign = player(helper, "relay_foreign");
        try {
            Arrow ordinary = new Arrow(helper.getLevel(), foreign);
            check(!RushSpellCompat.allowed(ordinary), "An ordinary foreign projectile entered the spell allowlist");
            ordinary.discard();
            check(!RushSpellCompat.firebolt(foreign, foreign.position().add(4, 0, 0), 4),
                    "A nonparticipant could launch a Rush relay");
            check(RivalRushManager.start(owner, new ResourceLocation("jetsetcraft", "dead_beat"),
                    new ResourceLocation("minecraft", "zombie"), false), "Could not start the relay proof session");
            ListTag inventory = owner.getInventory().save(new ListTag());
            if (!ModList.get().isLoaded("irons_spellbooks")) {
                check(!RushSpellCompat.fireboltAvailable(), "Absent Iron provider advertised a Firebolt relay");
                check(!RushSpellCompat.firebolt(owner, owner.getEyePosition().add(4, 0, 0), 4),
                        "Absent provider manufactured a substitute projectile");
                check(inventory.equals(owner.getInventory().save(new ListTag())) && RivalRushManager.active(owner),
                        "Absent provider changed the player's loadout or session");
                cleanup(owner);
                pass(helper, "absent");
                return;
            }
            check(RushSpellCompat.fireboltAvailable(), "Installed Iron provider lacks the pinned Firebolt API or registry");
            int x = owner.blockPosition().getX() >> 4, z = owner.blockPosition().getZ() >> 4;
            for (int dx = -2; dx <= 2; dx++) for (int dz = -2; dz <= 2; dz++)
                owner.serverLevel().getChunk(x + dx, z + dz);
            for (int tick = 0; tick < 45; tick++) RivalRushManager.tick(owner.getServer());
        } catch (RuntimeException error) { cleanup(owner); throw error; }

        helper.runAfterDelay(2, () -> {
            try {
                List<Mob> actors = owner.serverLevel().getEntitiesOfClass(Mob.class,
                        owner.getBoundingBox().inflate(80), mob -> RivalRushManager.belongsTo(mob, owner));
                check(!actors.isEmpty(), "Director did not create an eligible real rival for native collision");
                Mob target = actors.get(0);
                // Keep test geometry clear; the real provider's sweep, damage source and Forge event still run.
                target.moveTo(owner.getX() + 4.0D, owner.getY() + 2.0D, owner.getZ(), 0, 0);
                for (Projectile old : owner.serverLevel().getEntitiesOfClass(Projectile.class,
                        owner.getBoundingBox().inflate(80), p -> RivalRushManager.belongsTo(p, owner))) old.discard();
                ListTag inventory = owner.getInventory().save(new ListTag());
                float mana = providerMana(owner);
                int vanillaXp = owner.totalExperience;
                float nativeHealth = target.getHealth();
                long defeats = RivalRushManager.run(owner).defeats();
                SpawnCapture capture = new SpawnCapture(owner);
                MinecraftForge.EVENT_BUS.register(capture);
                Projectile shot;
                try {
                    check(!RushSpellCompat.firebolt(owner, target.position(), Float.NaN)
                                    && !RushSpellCompat.firebolt(owner, new Vec3(Double.POSITIVE_INFINITY, 0, 0), 4),
                            "Non-finite relay parameters were accepted");
                    check(RushSpellCompat.firebolt(owner, target.getBoundingBox().getCenter(), Float.MAX_VALUE),
                            "Native Firebolt was not admitted by the production Rush join hook");
                    check(capture.shots.size() == 1, "One relay activation did not create exactly one native projectile");
                    shot = capture.shots.get(0);
                } finally { MinecraftForge.EVENT_BUS.unregister(capture); }
                check(RushSpellCompat.FIREBOLT_ID.equals(ForgeRegistries.ENTITY_TYPES.getKey(shot.getType()))
                                && shot.getOwner() == owner && RivalRushManager.belongsTo(shot, owner)
                                && RushSpellCompat.allowed(shot),
                        "Native projectile lost its provider identity, owner, or run tracking");
                float boundedDamage = ((Number) shot.getClass().getMethod("getDamage").invoke(shot)).floatValue();
                check(boundedDamage == RushSpellCompat.MAX_DAMAGE, "Native projectile damage was not clamped before admission");
                // Copy only in this adversarial test to model an old relay whose owner/session is no longer valid.
                var type = ForgeRegistries.ENTITY_TYPES.getValue(RushSpellCompat.FIREBOLT_ID);
                Projectile foreignShot = (Projectile) type.create(owner.serverLevel());
                check(foreignShot != null, "Provider registry factory could not create a negative-case projectile");
                foreignShot.getPersistentData().merge(shot.getPersistentData().copy());
                foreignShot.getPersistentData().remove(RivalRushManager.OWNED);
                foreignShot.setOwner(foreign);
                foreignShot.setPos(shot.getX(), shot.getY(), shot.getZ());
                foreignShot.getClass().getMethod("setDamage", float.class).invoke(foreignShot, 4.0F);
                check(!RushSpellCompat.allowed(foreignShot) && !owner.serverLevel().addFreshEntity(foreignShot),
                        "A foreign owner bypassed projectile admission");
                foreignShot.discard();

                Vec3 direction = shot.getDeltaMovement().normalize();
                Vec3 origin = target.getBoundingBox().getCenter().subtract(direction);
                shot.setPos(origin.x, origin.y, origin.z);
                // Runs Iron's real ProjectileUtil collision -> onHitEntity -> DamageSources -> Forge hook.
                // It does not simulate a hit by posting LivingAttackEvent or calling tagEnemy directly.
                shot.getClass().getMethod("handleHitDetection").invoke(shot);
                check(shot.isRemoved() && target.isRemoved() && RivalRushManager.run(owner).defeats() == defeats + 1,
                        "Real provider collision did not produce exactly one virtual rival defeat");
                check(target.getHealth() == nativeHealth && owner.totalExperience == vanillaXp
                                && inventory.equals(owner.getInventory().save(new ListTag()))
                                && Float.compare(mana, providerMana(owner)) == 0,
                        "Relay changed native health, Minecraft XP, loadout, or provider mana");
                check(!RivalRushManager.tagEnemy(owner, target, 40), "Native defeat could be claimed twice");

                SpawnCapture cleanupCapture = new SpawnCapture(owner);
                MinecraftForge.EVENT_BUS.register(cleanupCapture);
                try {
                    check(RushSpellCompat.firebolt(owner, owner.getEyePosition().add(20, 8, 0), 3),
                            "Could not create an in-flight cleanup proof projectile");
                } finally { MinecraftForge.EVENT_BUS.unregister(cleanupCapture); }
                check(cleanupCapture.shots.size() == 1, "Cleanup proof did not create one tracked projectile");
                Projectile inFlight = cleanupCapture.shots.get(0);
                check(!inFlight.isRemoved(), "Cleanup proof projectile was removed before exit");
                cleanup(owner);
                check(inFlight.isRemoved() && !RushSpellCompat.allowed(inFlight),
                        "Leaving the run retained a native projectile or its authority");
                pass(helper, "provider=irons_spellbooks");
            } catch (ReflectiveOperationException error) {
                throw new GameTestAssertException("Pinned provider API changed during native Firebolt proof: " + error);
            } finally { cleanup(owner); }
        });
    }

    /** Captures the actual synchronous add; no replacement entity or production test hook is used. */
    public static final class SpawnCapture {
        private final ServerPlayer owner;
        private final List<Projectile> shots = new ArrayList<>();
        public SpawnCapture(ServerPlayer owner) { this.owner = owner; }
        @SubscribeEvent(priority = EventPriority.LOWEST)
        public void join(EntityJoinLevelEvent event) {
            if (event.getEntity() instanceof Projectile projectile && projectile.getOwner() == owner
                    && RushSpellCompat.FIREBOLT_ID.equals(ForgeRegistries.ENTITY_TYPES.getKey(projectile.getType())))
                shots.add(projectile);
        }
    }

    private static float providerMana(ServerPlayer player) throws ReflectiveOperationException {
        Class<?> magic = Class.forName("io.redspace.ironsspellbooks.api.magic.MagicData");
        Object data = magic.getMethod("getPlayerMagicData", LivingEntity.class).invoke(null, player);
        return ((Number) magic.getMethod("getMana").invoke(data)).floatValue();
    }

    private static ServerPlayer player(GameTestHelper helper, String name) {
        UUID uuid = UUID.nameUUIDFromBytes(("jetsetcraft:gametest:" + name).getBytes(StandardCharsets.UTF_8));
        ServerPlayer player = FakePlayerFactory.get(helper.getLevel(), new GameProfile(uuid, "JSC_" + name));
        if (RivalRushManager.active(player)) cleanup(player);
        player.getPersistentData().remove(Player.PERSISTED_NBT_TAG);
        player.getInventory().clearContent();
        player.containerMenu.setCarried(ItemStack.EMPTY);
        player.setGameMode(GameType.SURVIVAL);
        player.removeAllEffects();
        player.setHealth(player.getMaxHealth());
        player.setAbsorptionAmount(0);
        BlockPos pos = helper.absolutePos(new BlockPos(1, 2, 1));
        player.moveTo(pos.getX() + 0.5D, pos.getY(), pos.getZ() + 0.5D, 0, 0);
        return player;
    }

    private static void cleanup(ServerPlayer player) {
        if (RivalRushManager.active(player) || RushReturnState.hasJournal(player))
            check(RivalRushManager.leave(player, "Spell relay GameTest cleanup"), "Relay test could not restore the player");
    }

    private static void check(boolean condition, String message) {
        if (!condition) throw new GameTestAssertException(message);
    }

    private static void pass(GameTestHelper helper, String state) {
        System.out.println("JETSETCRAFT_GAMETEST_PASS rush_spell_relay " + state);
        helper.succeed();
    }

    private RushSpellGameTests() {}
}
