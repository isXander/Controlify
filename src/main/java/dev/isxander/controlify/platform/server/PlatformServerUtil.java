package dev.isxander.controlify.platform.server;

import dev.isxander.controlify.platform.server.events.CommandRegistrationCallbackEvent;
import dev.isxander.controlify.platform.server.events.InitPlayConnectionEvent;
import dev.isxander.controlify.platform.server.fabric.FabricPlatformServerImpl;

public final class PlatformServerUtil {

    private static final PlatformServerUtilImpl IMPL = new FabricPlatformServerImpl();

    public static void registerCommandRegistrationCallback(CommandRegistrationCallbackEvent callback) {
        IMPL.registerCommandRegistrationCallback(callback);
    }

    public static void registerInitPlayConnectionEvent(InitPlayConnectionEvent event) {
        IMPL.registerInitPlayConnectionEvent(event);
    }
}
