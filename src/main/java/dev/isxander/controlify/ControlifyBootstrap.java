package dev.isxander.controlify;

import net.fabricmc.api.ClientModInitializer;

public class ControlifyBootstrap implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        Controlify.instance().preInitialiseControlify();
    }
}
