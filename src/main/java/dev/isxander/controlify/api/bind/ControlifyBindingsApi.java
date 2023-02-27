package dev.isxander.controlify.api.bind;

import dev.isxander.controlify.bindings.BindingSupplier;
import dev.isxander.controlify.bindings.GamepadBinds;
import net.minecraft.client.KeyMapping;
import net.minecraft.resources.ResourceLocation;

import java.util.function.BooleanSupplier;

public interface ControlifyBindingsApi {
    /**
     * Registers a custom binding for all available controllers.
     * If the controller is not a gamepad, the binding with be empty by default.
     *
     * @param bind the default gamepad bind
     * @param id the identifier for the binding, the namespace should be your modid.
     * @return the binding supplier to fetch the binding for a specific controller.
     */
    BindingSupplier registerBind(GamepadBinds bind, ResourceLocation id);

    /**
     * Registers a custom binding for all available controllers.
     * If the controller is not a gamepad, the binding with be empty by default.
     *
     * @param bind the default gamepad bind
     * @param id the identifier for the binding, the namespace should be your modid.
     * @param override the minecraft keybind to imitate.
     * @param toggleOverride a supplier that returns true if the vanilla keybind should be treated as a {@link net.minecraft.client.ToggleKeyMapping}
     * @return the binding supplier to fetch the binding for a specific controller.
     */
    BindingSupplier registerBind(GamepadBinds bind, ResourceLocation id, KeyMapping override, BooleanSupplier toggleOverride);
}
