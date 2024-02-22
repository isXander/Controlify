package dev.isxander.controlify.api.bind;

import dev.isxander.controlify.controller.ControllerEntity;
import org.jetbrains.annotations.NotNull;

@FunctionalInterface
public interface BindingSupplier {
    ControllerBinding onController(@NotNull ControllerEntity controller);
}
