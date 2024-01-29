package dev.isxander.controlify.compatibility;

import dev.isxander.controlify.compatibility.immediatelyfast.ImmediatelyFastCompat;
import dev.isxander.controlify.compatibility.simplevoicechat.SimpleVoiceChatCompat;
import dev.isxander.controlify.utils.CUtil;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.Util;

import java.util.HashSet;
import java.util.Set;
import java.util.function.Function;

public class ControlifyCompat {
    private static final Function<String, Boolean> modsLoaded = Util.memoize(modid ->
            FabricLoader.getInstance().isModLoaded(modid));
    private static final Set<String> disabledMods = new HashSet<>();

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
        if (modsLoaded.apply(modid) && !disabledMods.contains(modid)) {
            try {
                runnable.run();
            } catch (Throwable t) {
                CUtil.LOGGER.error("Failed to run compatibility code for {}, potentially unsupported version? Disabling '{}' compat for this instance.", modid, modid, t);
            }
        }
    }
}
