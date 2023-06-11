package dev.isxander.controlify.utils;

import dev.isxander.controlify.debug.DebugProperties;

public class DebugLog {
    public static void log(String message, Object... args) {
        if (DebugProperties.DEBUG_LOGGING) {
            Log.LOGGER.info(message, args);
        }
    }
}
