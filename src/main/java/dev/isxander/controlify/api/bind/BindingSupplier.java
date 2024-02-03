package dev.isxander.controlify.api.bind;

import dev.isxander.controlify.controller.Controller;
import org.jetbrains.annotations.NotNull;

@FunctionalInterface
public interface BindingSupplier {
    ControllerBinding onController(@NotNull Controller<?> controller);
}
