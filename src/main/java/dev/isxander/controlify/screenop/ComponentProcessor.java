package dev.isxander.controlify.screenop;

import dev.isxander.controlify.controller.Controller;

public interface ComponentProcessor {
    ComponentProcessor EMPTY = new ComponentProcessor(){};

    default boolean overrideControllerNavigation(ScreenProcessor<?> screen, Controller controller) {
        return false;
    }

    default boolean overrideControllerButtons(ScreenProcessor<?> screen, Controller controller) {
        return false;
    }

    default void onFocusGained(ScreenProcessor<?> screen, Controller controller) {
    }
}
