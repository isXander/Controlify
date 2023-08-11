package dev.isxander.controlify.compatibility;

import dev.isxander.controlify.compatibility.immediatelyfast.ImmediatelyFastCompat;
import dev.isxander.controlify.compatibility.simplevoicechat.SimpleVoiceChatCompat;
import dev.isxander.controlify.utils.Log;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.Util;

import java.util.function.Function;

public class ControlifyCompat {
    private static final Function<String, Boolean> modsLoaded = Util.memoize(modid ->
            FabricLoader.getInstance().isModLoaded(modid));

    public static final String IMMEDIATELY_FAST = "immediatelyfast";
    public static final String SIMPLE_VOICE_CHAT = "voicechat";

    public static void init() {
        wrapCompatCall(SIMPLE_VOICE_CHAT, SimpleVoiceChatCompat::init);
    }

    public static void ifBeginHudBatching() {
        wrapCompatCall(IMMEDIATELY_FAST, ImmediatelyFastCompat::beginHudBatching);
    }

    public static void ifEndHudBatching() {
        wrapCompatCall(IMMEDIATELY_FAST, ImmediatelyFastCompat::endHudBatching);
    }

    private static void wrapCompatCall(String modid, Runnable runnable) {
        if (modsLoaded.apply(modid)) {
            try {
                runnable.run();
            } catch (Throwable t) {
                Log.LOGGER.error("Failed to run compatibility code for %s, potentially unsupported version?".formatted(modid), t);
            }
        }
    }
}
