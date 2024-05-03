package dev.isxander.controlify.utils;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;

import java.util.function.Function;

public interface InitialScreenRegistryDuck {
    void controlify$registerInitialScreen(Function<Runnable, Screen> screenFactory);

    static void registerInitialScreen(Function<Runnable, Screen> screenFactory) {
        ((InitialScreenRegistryDuck) Minecraft.getInstance()).controlify$registerInitialScreen(screenFactory);
    }
}
