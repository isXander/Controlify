package dev.isxander.controlify.bindings;

import dev.isxander.controlify.controller.Controller;
import dev.isxander.controlify.controller.ControllerState;

@FunctionalInterface
public interface BindingSupplier<T extends ControllerState> {
    ControllerBinding<T> get(Controller<T, ?> controller);
}
