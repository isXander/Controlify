package dev.isxander.controlify.api.v1.bindings;

import dev.isxander.controlify.api.v1.ControlifyController;

import java.util.function.Predicate;
import java.util.function.UnaryOperator;

public interface InputBindingRegistry {
    /**
     * Registers a new binding that will be created on all controllers.
     *
     * @param callback the callback that populates the binding builder. will only be called once
     * @return the supplier for the binding
     */
    InputBindingSupplier register(RegistryCallback callback);

    /**
     * Registers a new binding that will be created on only the controllers that pass the filter.
     *
     * @param callback the callback that populates the binding builder. will only be called once
     * @param filter the filter that determines if the binding should be created for the controller
     * @return the supplier for the binding
     */
    InputBindingSupplier register(RegistryCallback callback, Predicate<ControlifyController> filter);

    @FunctionalInterface
    interface RegistryCallback extends UnaryOperator<InputBindingBuilder> {
    }
}
