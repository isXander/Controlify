package dev.isxander.controlify.controller.composable;

import dev.isxander.controlify.controller.ControllerConfig;

public interface ControllerStateModifier {
    void modifyState(ModifiableControllerState stateProvider, ControllerConfig config);

    ControllerStateModifier NOOP = (stateProvider, config) -> {};
}
