package dev.isxander.controlify.screenop;

import dev.isxander.controlify.controller.ControllerEntity;

public interface ScreenControllerEventListener {
    default void onControllerInput(ControllerEntity controller) {}
}
