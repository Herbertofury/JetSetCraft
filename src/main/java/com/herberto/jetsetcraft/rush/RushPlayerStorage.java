package com.herberto.jetsetcraft.rush;

import com.herberto.jetsetcraft.JetSetCraft;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.players.PlayerList;
import net.minecraftforge.common.util.FakePlayer;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.util.ObfuscationReflectionHelper;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.UUID;

/**
 * A narrow synchronous player-save bridge for the return journal. Resolution is lazy and uses Forge's SRG mapper;
 * invoking the method on the real PlayerList preserves integrated-server overrides and normal Forge save hooks.
 *
 * <p>The 1.20.1 PlayerList save method is protected {@code m_6765_(ServerPlayer)}. Its PlayerDataStorage delegate
 * catches I/O errors internally, so a returning Java call alone is insufficient. The synchronous SaveToFile event
 * acknowledges completion of Minecraft's replacement of that player's data file; it is not an fsync guarantee.</p>
 *
 * @see <a href="https://github.com/MinecraftForge/MinecraftForge/blob/1.20.1/patches/minecraft/net/minecraft/server/players/PlayerList.java.patch">Forge 1.20.1 PlayerList patch</a>
 * @see <a href="https://github.com/MinecraftForge/MinecraftForge/blob/1.20.1/patches/minecraft/net/minecraft/world/level/storage/PlayerDataStorage.java.patch">Forge save acknowledgement hook</a>
 * @see <a href="https://github.com/MinecraftForge/MinecraftForge/blob/1.20.1/fmlcore/src/main/java/net/minecraftforge/fml/util/ObfuscationReflectionHelper.java">Forge SRG reflection helper</a>
 */
@Mod.EventBusSubscriber(modid = JetSetCraft.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public final class RushPlayerStorage {
    private static final ThreadLocal<SaveAttempt> PENDING = new ThreadLocal<>();
    private static volatile Method individualSave;
    private static volatile boolean resolved;

    private static final class SaveAttempt {
        final UUID owner;
        boolean acknowledged;

        SaveAttempt(UUID owner) { this.owner = owner; }
    }

    /**
     * Saves only the requested real player in the normal case. If access mapping is unavailable, the public saveAll
     * path is a logged compatibility fallback. Both paths require the requested player's own completion event.
     * FakePlayers have no player-file durability contract and participate only in in-memory production tests.
     */
    public static boolean flush(ServerPlayer player) {
        if (player == null || player.getServer() == null) return false;
        if (player instanceof FakePlayer) return true;
        if (!player.getServer().isSameThread()) {
            JetSetCraft.LOGGER.error("Refusing an off-thread Rival Rush player checkpoint for {}", player.getUUID());
            return false;
        }
        SaveAttempt previous = PENDING.get();
        SaveAttempt attempt = new SaveAttempt(player.getUUID());
        PENDING.set(attempt);
        try {
            Method method = resolveSave();
            boolean fallback = method == null;
            if (method != null) {
                try {
                    method.invoke(player.getServer().getPlayerList(), player);
                } catch (InvocationTargetException error) {
                    // The actual save/override threw. Repeating it for every player would multiply side effects.
                    JetSetCraft.LOGGER.error("Rival Rush player checkpoint failed for {}", player.getUUID(), error.getCause());
                    return false;
                } catch (ReflectiveOperationException | RuntimeException error) {
                    JetSetCraft.LOGGER.warn("Individual player-save access failed; using public saveAll for Rival Rush checkpoint", error);
                    fallback = true;
                }
            }
            if (fallback) {
                try {
                    player.getServer().getPlayerList().saveAll();
                } catch (RuntimeException error) {
                    JetSetCraft.LOGGER.error("Public player-save fallback failed for Rival Rush checkpoint {}", player.getUUID(), error);
                    return false;
                }
            }
            if (!attempt.acknowledged) {
                JetSetCraft.LOGGER.error("No completed player-file save was acknowledged for Rival Rush player {}; keeping the return state recoverable",
                        player.getUUID());
                return false;
            }
            return true;
        } finally {
            if (previous == null) PENDING.remove();
            else PENDING.set(previous);
        }
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public static void saved(PlayerEvent.SaveToFile event) {
        SaveAttempt attempt = PENDING.get();
        if (attempt != null && attempt.owner.equals(event.getEntity().getUUID())) attempt.acknowledged = true;
    }

    private static Method resolveSave() {
        if (resolved) return individualSave;
        synchronized (RushPlayerStorage.class) {
            if (!resolved) {
                try {
                    individualSave = ObfuscationReflectionHelper.findMethod(PlayerList.class, "m_6765_", ServerPlayer.class);
                } catch (RuntimeException error) {
                    JetSetCraft.LOGGER.warn("Individual player-save mapping is unavailable; Rival Rush checkpoints will use public saveAll", error);
                } finally {
                    resolved = true;
                }
            }
            return individualSave;
        }
    }

    private RushPlayerStorage() {}
}
