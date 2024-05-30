package dev.isxander.controlify.compatibility;

import dev.isxander.controlify.platform.main.PlatformMainUtil;
import dev.isxander.controlify.utils.CUtil;
import net.minecraft.Util;

import java.util.HashSet;
import java.util.Set;
import java.util.function.Function;

public class ControlifyCompat {
    private static final Function<String, Boolean> modsLoaded = Util.memoize(modid ->
            PlatformMainUtil.isModLoaded(modid));
    private static final Set<String> disabledMods = new HashSet<>();

    public static final String IMMEDIATELY_FAST = "immediatelyfast";
    public static final String SIMPLE_VOICE_CHAT = "voicechat";

    public static void init() {
        /*? if simple-voice-chat {*/
        wrapCompatCall(
                SIMPLE_VOICE_CHAT,
                dev.isxander.controlify.compatibility.simplevoicechat.SimpleVoiceChatCompat::init
        );
        /*?}*/
    }

    public static void ifBeginHudBatching() {
        /*? if immediately-fast {*/
        wrapCompatCall(
                IMMEDIATELY_FAST,
                dev.isxander.controlify.compatibility.immediatelyfast.ImmediatelyFastCompat::beginHudBatching
        );
        /*?}*/
    }

    public static void ifEndHudBatching() {
        /*? if immediately-fast {*/
        wrapCompatCall(
                IMMEDIATELY_FAST,
                dev.isxander.controlify.compatibility.immediatelyfast.ImmediatelyFastCompat::endHudBatching
        );
        /*?}*/
    }

    private static void wrapCompatCall(String modid, Runnable runnable) {
        if (modsLoaded.apply(modid) && !disabledMods.contains(modid)) {
            try {
                runnable.run();
            } catch (Throwable t) {
                CUtil.LOGGER.error("Failed to run compatibility code for {}, potentially unsupported version? Disabling '{}' compat for this instance.", modid, modid, t);
                disabledMods.add(modid);
            }
        }
    }
}
