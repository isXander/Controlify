package dev.isxander.controlify.bindings.v2;

import dev.isxander.controlify.controller.ControllerEntity;

import java.util.function.Predicate;

public class ControllerBindHolder implements InputBindingRegistry {
    @Override
    public InputBindingSupplier registerBinding(RegistryCallback callback) {
        return null;
    }

    @Override
    public InputBindingSupplier registerBinding(Predicate<ControllerEntity> filter, RegistryCallback callback) {
        return null;
    }
}
