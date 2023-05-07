package dev.isxander.controlify.api.bind;

import dev.isxander.controlify.bindings.BindContext;
import dev.isxander.controlify.bindings.ControllerBindingImpl;
import dev.isxander.controlify.bindings.GamepadBinds;
import dev.isxander.controlify.bindings.IBind;
import dev.isxander.controlify.controller.Controller;
import dev.isxander.controlify.controller.ControllerState;
import net.minecraft.client.KeyMapping;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;

import java.util.function.BooleanSupplier;

public interface ControllerBindingBuilder<T extends ControllerState> {
    static <T extends ControllerState> ControllerBindingBuilder<T> create(Controller<T, ?> controller) {
        return new ControllerBindingImpl.ControllerBindingBuilderImpl<>(controller);
    }

    /**
     * Sets the identifier for the binding.
     * @param id the identifier for the binding, the namespace should be your modid.
     */
    ControllerBindingBuilder<T> identifier(ResourceLocation id);

    /**
     * Sets the identifier for the binding.
     * @param namespace the namespace for the binding, should be your modid.
     * @param path the path for the binding.
     */
    ControllerBindingBuilder<T> identifier(String namespace, String path);

    /**
     * The default bind for the binding. This is usually inaccessible due to unknown
     * generics and {@link ControllerBindingBuilder#defaultBind(GamepadBinds)} should be used instead.
     * @param bind the default bind
     */
    ControllerBindingBuilder<T> defaultBind(IBind<T> bind);

    /**
     * Sets the default gamepad bind for the binding.
     * If the controller is not a gamepad, the default is unbound.
     * @param gamepadBind the default gamepad bind
     */
    ControllerBindingBuilder<T> defaultBind(GamepadBinds gamepadBind);

    /**
     * Sets the name of the binding.
     * <p>
     * If left unset, the default translation location is
     * <p>
     * {@code controlify.binding.<namespace>.<path>}.
     *
     * @param name the name of the binding
     */
    ControllerBindingBuilder<T> name(Component name);

    /**
     * Sets the description of the binding.
     * <p>
     * If left unset, the default translation location is
     * <p>
     * {@code controlify.binding.<namespace>.<path>.desc}.
     *
     * @param description the description of the binding
     */
    ControllerBindingBuilder<T> description(Component description);

    /**
     * Sets the category of the binding.
     * Must be set.
     *
     * @param category the category of the binding
     */
    ControllerBindingBuilder<T> category(Component category);

    ControllerBindingBuilder<T> context(BindContext... contexts);

    /**
     * Specifies are vanilla override for the binding.
     * Will emulate presses of the vanilla keybind when the controller binding is pressed.
     * Though usage of this is discouraged as it can have funny behaviours.
     *
     * @param keyMapping the vanilla keybind to emulate
     * @param toggleable if the binding should be toggleable
     */
    ControllerBindingBuilder<T> vanillaOverride(KeyMapping keyMapping, BooleanSupplier toggleable);

    /**
     * Specifies are vanilla override for the binding.
     * Will emulate presses of the vanilla keybind when the controller binding is pressed.
     * Though usage of this is discouraged as it can have funny behaviours.
     *
     * @param keyMapping the vanilla keybind to emulate
     */
    ControllerBindingBuilder<T> vanillaOverride(KeyMapping keyMapping);

    ControllerBinding build();
}
