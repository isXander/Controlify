package dev.isxander.controlify.platform.server.fabric;

import dev.isxander.controlify.platform.server.PlatformServerUtilImpl;
import dev.isxander.controlify.platform.server.events.CommandRegistrationCallbackEvent;
import dev.isxander.controlify.platform.server.events.InitPlayConnectionEvent;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;

public class FabricPlatformServerImpl implements PlatformServerUtilImpl {
    @Override
    public void registerCommandRegistrationCallback(CommandRegistrationCallbackEvent callback) {
        CommandRegistrationCallback.EVENT.register(callback::onRegister);
    }

    @Override
    public void registerInitPlayConnectionEvent(InitPlayConnectionEvent event) {
        ServerPlayConnectionEvents.INIT.register(event::onInit);
    }
}
