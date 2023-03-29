package dev.isxander.controlify.api.bind;

import dev.isxander.controlify.api.ControlifyApi;
import dev.isxander.controlify.bindings.BindingSupplier;
import dev.isxander.controlify.bindings.ControllerBindings;
import dev.isxander.controlify.bindings.GamepadBinds;
import net.minecraft.client.KeyMapping;
import net.minecraft.resources.ResourceLocation;

import java.util.function.BooleanSupplier;
import java.util.function.UnaryOperator;

/**
 * Handles registering new bindings for controllers.
 * <p>
 * Should be called within {@link dev.isxander.controlify.api.entrypoint.ControlifyEntrypoint#onControlifyPreInit(ControlifyApi)}
 */
public interface ControlifyBindingsApi {
    BindingSupplier registerBind(ResourceLocation id, UnaryOperator<ControllerBindingBuilder<?>> builder);

    /**
     * Registers a custom binding for all available controllers.
     * If the controller is not a gamepad, the binding with be empty by default.
     *
     * @param bind the default gamepad bind - joysticks are unset by default
     * @param id the identifier for the binding, the namespace should be your modid.
     * @return the binding supplier to fetch the binding for a specific controller.
     */
    @Deprecated
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
    @Deprecated
    BindingSupplier registerBind(GamepadBinds bind, ResourceLocation id, KeyMapping override, BooleanSupplier toggleOverride);

    void excludeVanillaBind(KeyMapping... keyMapping);

    static ControlifyBindingsApi get() {
        return ControllerBindings.Api.INSTANCE;
    }
}
