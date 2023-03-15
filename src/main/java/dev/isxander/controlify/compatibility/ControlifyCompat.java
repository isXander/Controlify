package dev.isxander.controlify.compatibility;

import dev.isxander.controlify.compatibility.immediatelyfast.ImmediatelyFastCompat;
import net.fabricmc.loader.api.FabricLoader;

public class ControlifyCompat {
    public static final boolean IMMEDIATELY_FAST = mod("immediatelyfast");

    public static void ifBeginHudBatching() {
        if (IMMEDIATELY_FAST) {
            ImmediatelyFastCompat.beginHudBatching();
        }
    }

    public static void ifEndHudBatching() {
        if (IMMEDIATELY_FAST) {
            ImmediatelyFastCompat.endHudBatching();
        }
    }

    private static boolean mod(String id) {
        return FabricLoader.getInstance().isModLoaded(id);
    }
}
