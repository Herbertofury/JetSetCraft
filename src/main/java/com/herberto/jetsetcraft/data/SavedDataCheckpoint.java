package com.herberto.jetsetcraft.data;

import com.herberto.jetsetcraft.JetSetCraft;
import net.minecraft.SharedConstants;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtIo;
import net.minecraft.nbt.Tag;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.level.saveddata.SavedData;
import net.minecraft.world.level.storage.LevelResource;

import java.io.IOException;
import java.nio.file.Path;

/** Vanilla SavedData format with an explicit read-back acknowledgement; this is not a power-loss/fsync guarantee. */
public final class SavedDataCheckpoint {
    public static boolean saveAndAcknowledge(MinecraftServer server, String name, SavedData record) {
        if (server == null || record == null || name == null || !name.matches("[a-z0-9_]+") || !server.isSameThread()) return false;
        CompoundTag expected;
        try {
            expected = record.save(new CompoundTag()).copy();
            // Vanilla catches write failures internally and clears dirty even on IOException. Always request a save,
            // then inspect the real file before allowing the caller to consume a separate recovery intent.
            record.setDirty();
            server.overworld().getDataStorage().save();
        } catch (RuntimeException error) {
            record.setDirty();
            JetSetCraft.LOGGER.warn("Saved-data checkpoint {} could not be written: {}", name, error.toString());
            return false;
        }
        return acknowledge(server.getWorldPath(LevelResource.ROOT).resolve("data").resolve(name + ".dat"), record, expected);
    }

    /** Reads an existing vanilla .dat file; failed or mismatched writes stay dirty for normal server retry. */
    public static boolean acknowledge(Path file, SavedData record, CompoundTag expected) {
        if (record == null) return false;
        if (file != null && expected != null) try {
            CompoundTag stored = NbtIo.readCompressed(file.toFile());
            if (stored != null && stored.contains("data", Tag.TAG_COMPOUND)
                    && stored.contains("DataVersion", Tag.TAG_INT)
                    && stored.getInt("DataVersion") == SharedConstants.getCurrentVersion().getDataVersion().getVersion()
                    && expected.equals(stored.getCompound("data"))) return true;
            JetSetCraft.LOGGER.warn("Saved-data checkpoint {} did not match the complete current record; retaining recovery intent", file);
        } catch (IOException | RuntimeException error) {
            JetSetCraft.LOGGER.warn("Saved-data checkpoint {} could not be acknowledged: {}", file, error.toString());
        }
        record.setDirty();
        return false;
    }

    private SavedDataCheckpoint() {}
}
