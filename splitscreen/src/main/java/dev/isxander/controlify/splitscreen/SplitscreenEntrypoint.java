package dev.isxander.controlify.splitscreen;

import net.fabricmc.api.ClientModInitializer;
import net.minecraft.client.Minecraft;

public class SplitscreenEntrypoint implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        SplitscreenBootstrapper.bootstrap(Minecraft.getInstance());
    }
}
