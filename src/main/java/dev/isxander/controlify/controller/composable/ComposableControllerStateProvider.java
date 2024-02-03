package dev.isxander.controlify.controller.composable;

import dev.isxander.controlify.controller.ControllerConfig;

public interface ComposableControllerStateProvider {
    ComposableControllerState stateNow();
    ComposableControllerState stateThen();

    int buttonCount();
    int axisCount();
    int hatCount();

    boolean supportsGyro();

    void updateState(ControllerConfig config);
    void clearState();

    default void close() {}
}
