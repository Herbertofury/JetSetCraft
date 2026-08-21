package com.herberto.jetsetcraft.event;

import com.herberto.jetsetcraft.data.JetSetData;
import net.minecraftforge.common.capabilities.RegisterCapabilitiesEvent;

public final class CapabilityEvents {
    public static void register(RegisterCapabilitiesEvent event) { event.register(JetSetData.class); }
    private CapabilityEvents() {}
}
