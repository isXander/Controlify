package dev.isxander.controlify.screenop;

import net.minecraft.client.gui.screens.Screen;
import org.jetbrains.annotations.NotNull;

import java.util.function.Function;

public interface ScreenProcessorProvider {
    ScreenProcessor<?> screenProcessor();

    static ScreenProcessor<?> provide(@NotNull Screen screen) {
        return ((ScreenProcessorProvider) screen).screenProcessor();
    }

    static <T extends Screen> void registerProvider(@NotNull Class<T> screenClass, @NotNull ScreenProcessorFactory.Factory<T> factory) {
        ScreenProcessorFactory.registerProvider(screenClass, factory);
    }
}
