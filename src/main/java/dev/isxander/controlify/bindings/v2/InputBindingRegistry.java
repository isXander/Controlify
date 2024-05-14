package dev.isxander.controlify.bindings.v2;

import dev.isxander.controlify.controller.ControllerEntity;

import java.util.function.Predicate;

public interface InputBindingRegistry {
    InputBindingSupplier registerBinding(RegistryCallback callback);

    InputBindingSupplier registerBinding(Predicate<ControllerEntity> filter, RegistryCallback callback);

    @FunctionalInterface
    interface RegistryCallback {
        InputBindingBuilder buildBinding(InputBindingBuilder builder, ControllerEntity controller);
    }
}
