package dev.isxander.controlify;

import net.fabricmc.api.ClientModInitializer;

public class ControlifyEntrypoint implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        Controlify.instance().preInitialiseControlify();
    }
}
