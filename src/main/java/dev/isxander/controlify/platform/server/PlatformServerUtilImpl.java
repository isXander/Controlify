package dev.isxander.controlify.platform.server;

import dev.isxander.controlify.platform.server.events.CommandRegistrationCallbackEvent;
import dev.isxander.controlify.platform.server.events.InitPlayConnectionEvent;

public interface PlatformServerUtilImpl {
    void registerCommandRegistrationCallback(CommandRegistrationCallbackEvent callback);

    void registerInitPlayConnectionEvent(InitPlayConnectionEvent event);
}
