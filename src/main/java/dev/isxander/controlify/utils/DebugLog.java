package dev.isxander.controlify.utils;

import dev.isxander.controlify.Controlify;
import dev.isxander.controlify.debug.DebugProperties;

public class DebugLog {
    public static void log(String message, Object... args) {
        if (DebugProperties.DEBUG_LOGGING) {
            Controlify.LOGGER.info(message, args);
        }
    }
}
