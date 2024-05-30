package dev.isxander.controlify;

import dev.isxander.controlify.server.ControlifyServer;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.api.DedicatedServerModInitializer;
import net.fabricmc.api.ModInitializer;

public class ControlifyBootstrap implements ClientModInitializer, ModInitializer, DedicatedServerModInitializer {
    @Override
    public void onInitializeClient() {
        Controlify.instance().preInitialiseControlify();
    }

    @Override
    public void onInitializeServer() {
        ControlifyServer.getInstance().onInitializeServer();
    }

    @Override
    public void onInitialize() {
        ControlifyServer.getInstance().onInitialize();
    }
}
