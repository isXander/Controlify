package dev.isxander.controlify.api.v1;

import dev.isxander.controlify.api.v1.bindings.InputBindingRegistry;

public interface ControlifyExtension {
    void registerBindings(ControlifyApi api, InputBindingRegistry registry);
}
