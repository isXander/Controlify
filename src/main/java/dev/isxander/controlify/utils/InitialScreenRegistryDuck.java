package dev.isxander.controlify.utils;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;

import java.util.function.Function;

public interface InitialScreenRegistryDuck {
    void controlify$registerInitialScreen(Function<Runnable, Screen> screenFactory);

    static InitialScreenRegistryDuck get() {
        //? if >=26.2 {
        return (InitialScreenRegistryDuck) Minecraft.getInstance().gui;
        //?} else {
        /*return (InitialScreenRegistryDuck) Minecraft.getInstance();
        *///?}
    }
    
    static void registerInitialScreen(Function<Runnable, Screen> screenFactory) {
        get().controlify$registerInitialScreen(screenFactory);
    }
}
