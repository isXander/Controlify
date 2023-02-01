package dev.isxander.controlify.bindings;

import dev.isxander.controlify.controller.Controller;

@FunctionalInterface
public interface BindingSupplier {
    ControllerBinding get(Controller controller);
}
