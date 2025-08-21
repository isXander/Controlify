package dev.isxander.controlify.screenop;

import dev.isxander.controlify.controller.ControllerEntity;
import dev.isxander.controlify.screenop.keyboard.ComponentKeyboardBehaviour;

public interface ComponentProcessor extends ComponentProcessorProvider {
    ComponentProcessor EMPTY = new ComponentProcessor(){};

    default boolean overrideControllerNavigation(ScreenProcessor<?> screen, ControllerEntity controller) {
        return false;
    }

    default boolean overrideControllerButtons(ScreenProcessor<?> screen, ControllerEntity controller) {
        return false;
    }

    default void onFocusGained(ScreenProcessor<?> screen, ControllerEntity controller) {
    }

    default boolean shouldKeepFocusOnKeyboardMode(ScreenProcessor<?> screen) {
        return false;
    }

    default ComponentKeyboardBehaviour getKeyboardBehaviour(ScreenProcessor<?> screen, ControllerEntity controller) {
        return ComponentKeyboardBehaviour.UNDEFINED;
    }

    @Override
    default ComponentProcessor componentProcessor() {
        return this;
    }
}
