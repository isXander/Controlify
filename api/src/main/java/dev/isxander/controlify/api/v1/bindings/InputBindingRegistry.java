package dev.isxander.controlify.api.v1.bindings;

import java.util.function.UnaryOperator;

public interface InputBindingRegistry {
    InputBindingSupplier register(RegistryCallback callback);

    @FunctionalInterface
    interface RegistryCallback extends UnaryOperator<InputBindingBuilder> {
    }
}
