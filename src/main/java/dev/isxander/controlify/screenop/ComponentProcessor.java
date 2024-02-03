package dev.isxander.controlify.screenop;

import dev.isxander.controlify.controller.Controller;

public interface ComponentProcessor extends ComponentProcessorProvider {
    ComponentProcessor EMPTY = new ComponentProcessor(){};

    default boolean overrideControllerNavigation(ScreenProcessor<?> screen, Controller<?> controller) {
        return false;
    }

    default boolean overrideControllerButtons(ScreenProcessor<?> screen, Controller<?> controller) {
        return false;
    }

    default void onFocusGained(ScreenProcessor<?> screen, Controller<?> controller) {
    }

    default boolean shouldKeepFocusOnKeyboardMode(ScreenProcessor<?> screen) {
        return false;
    }

    @Override
    default ComponentProcessor componentProcessor() {
        return this;
    }
}
