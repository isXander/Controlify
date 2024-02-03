package dev.isxander.controlify.controller.composable;

import dev.isxander.controlify.controller.Controller;
import dev.isxander.controlify.rumble.RumbleManager;

public interface ComposableControllerRumble {
    RumbleManager rumbleManager();

    boolean supportsRumble();

    void bindController(Controller<?> controller);

    default void close() {}
}
