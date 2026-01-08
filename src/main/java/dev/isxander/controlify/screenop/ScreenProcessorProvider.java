package dev.isxander.controlify.screenop;

import net.minecraft.client.gui.screens.Screen;
import org.jspecify.annotations.NonNull;

import java.util.function.Function;

public interface ScreenProcessorProvider {
    ScreenProcessor<?> screenProcessor();

    static ScreenProcessor<?> provide(@NonNull Screen screen) {
        return ((ScreenProcessorProvider) screen).screenProcessor();
    }

    static <T extends Screen> void registerProvider(@NonNull Class<T> screenClass, ScreenProcessorFactory.@NonNull Factory<T> factory) {
        ScreenProcessorFactory.registerProvider(screenClass, factory);
    }
}
