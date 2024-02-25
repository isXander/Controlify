package dev.isxander.controlify.controller.input;

import dev.isxander.controlify.controller.impl.ControllerStateImpl;

public interface ControllerState extends ControllerStateView {
    ControllerState EMPTY = new ControllerStateImpl();

    void clearState();

    default void close() {}
}
