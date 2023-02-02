package dev.isxander.controlify.compatibility.screen;

import net.minecraft.client.gui.screens.Screen;

public interface ScreenProcessorProvider {
    ScreenProcessor<?> screenProcessor();

    static ScreenProcessor<?> provide(Screen screen) {
        return ((ScreenProcessorProvider) screen).screenProcessor();
    }
}
