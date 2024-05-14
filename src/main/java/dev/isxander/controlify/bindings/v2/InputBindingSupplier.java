package dev.isxander.controlify.bindings.v2;

import dev.isxander.controlify.controller.ControllerEntity;

@FunctionalInterface
public interface InputBindingSupplier {
    InputBinding onController(ControllerEntity controller);
}
