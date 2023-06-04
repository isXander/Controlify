package dev.isxander.controlify.screenop;

import dev.isxander.controlify.controller.Controller;

public interface ScreenControllerEventListener {
    default void onControllerInput(Controller<?, ?> controller) {}
}
