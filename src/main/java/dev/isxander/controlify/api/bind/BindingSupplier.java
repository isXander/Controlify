package dev.isxander.controlify.api.bind;

import dev.isxander.controlify.controller.Controller;

@FunctionalInterface
public interface BindingSupplier {
    ControllerBinding onController(Controller<?, ?> controller);
}
