package com.herberto.jetsetcraft.data;

import com.herberto.jetsetcraft.JetSetCraft;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityManager;
import net.minecraftforge.common.capabilities.CapabilityToken;
import net.minecraftforge.common.capabilities.ICapabilitySerializable;
import net.minecraftforge.common.util.LazyOptional;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public final class JetSetDataProvider implements ICapabilitySerializable<CompoundTag> {
    public static final ResourceLocation ID = ResourceLocation.fromNamespaceAndPath(JetSetCraft.MOD_ID, "ride_data");
    public static final Capability<JetSetData> CAPABILITY = CapabilityManager.get(new CapabilityToken<>() {});
    private final JetSetData data = new JetSetData();
    private LazyOptional<JetSetData> optional = LazyOptional.of(() -> data);

    @Override
    public <T> @NotNull LazyOptional<T> getCapability(@NotNull Capability<T> cap, @Nullable Direction side) {
        return cap == CAPABILITY ? optional.cast() : LazyOptional.empty();
    }
    @Override public CompoundTag serializeNBT() { return data.save(); }
    @Override public void deserializeNBT(CompoundTag nbt) { data.load(nbt); }
    public void invalidate() {
        // Forge invalidates and then revives the same ServerPlayer during a dimension transfer. Its outer
        // CapabilityProvider blocks queries while invalid, but reviveCaps() does not recreate attached providers.
        // Invalidate every previously issued handle and prepare a fresh handle to the SAME durable data. This
        // does not revive the owning entity: dead/removed hosts still return empty until Forge explicitly revives.
        LazyOptional<JetSetData> previous = optional;
        optional = LazyOptional.of(() -> data);
        previous.invalidate();
    }
}
