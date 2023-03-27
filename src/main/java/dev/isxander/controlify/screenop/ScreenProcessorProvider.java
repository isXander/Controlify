package dev.isxander.controlify.screenop;

import net.minecraft.client.gui.screens.Screen;

import java.util.Optional;

public interface ScreenProcessorProvider {
    ScreenProcessor<?> screenProcessor();

    static ScreenProcessor<?> provide(Screen screen) {
        Optional<ScreenProcessor<?>> optional = REGISTRY.get(screen);
        if (optional.isPresent()) return optional.get();

        return ((ScreenProcessorProvider) screen).screenProcessor();
    }

    /**
     * Register a screen processor for a screen from an entrypoint
     */
    Registry<Screen, ScreenProcessor<?>> REGISTRY = new Registry<>();
}
