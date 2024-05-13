package dev.isxander.controlify.api.bind;

import dev.isxander.controlify.bindings.*;
import dev.isxander.controlify.controller.ControllerEntity;
import dev.isxander.controlify.controller.input.ControllerStateView;
import net.minecraft.client.KeyMapping;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;

import java.util.function.BooleanSupplier;
import java.util.function.Function;

public interface ControllerBindingBuilder {
    static ControllerBindingBuilder create(ControllerEntity controller) {
        return new ControllerBindingImpl.ControllerBindingBuilderImpl(controller);
    }

    /**
     * Sets the identifier for the binding.
     * @param id the identifier for the binding, the namespace should be your modid.
     */
    ControllerBindingBuilder identifier(ResourceLocation id);

    /**
     * Sets the identifier for the binding.
     * @param namespace the namespace for the binding, should be your modid.
     * @param path the path for the binding.
     */
    ControllerBindingBuilder identifier(String namespace, String path);

    /**
     * The default bind for the binding.
     * @param bind the default bind
     */
    ControllerBindingBuilder defaultBind(Bind bind);

    ControllerBindingBuilder hardcodedBind(Function<ControllerStateView, Float> bind);

    /**
     * Sets the name of the binding.
     * <p>
     * If left unset, the default translation location is
     * <p>
     * {@code controlify.binding.<namespace>.<path>}.
     *
     * @param name the name of the binding
     */
    ControllerBindingBuilder name(Component name);

    /**
     * Sets the description of the binding.
     * <p>
     * If left unset, the default translation location is
     * <p>
     * {@code controlify.binding.<namespace>.<path>.desc}.
     *
     * @param description the description of the binding
     */
    ControllerBindingBuilder description(Component description);

    /**
     * Sets the category of the binding.
     * Must be set.
     *
     * @param category the category of the binding
     */
    ControllerBindingBuilder category(Component category);

    ControllerBindingBuilder context(BindContext... contexts);

    ControllerBindingBuilder radialCandidate(ResourceLocation icon);

    /**
     * Specifies are vanilla override for the binding.
     * Will emulate presses of the vanilla keybind when the controller binding is pressed.
     * Though usage of this is discouraged as it can have funny behaviours.
     *
     * @param keyMapping the vanilla keybind to emulate
     * @param toggleable if the binding should be toggleable
     */
    ControllerBindingBuilder vanillaOverride(KeyMapping keyMapping, BooleanSupplier toggleable);

    /**
     * Specifies are vanilla override for the binding.
     * Will emulate presses of the vanilla keybind when the controller binding is pressed.
     * Though usage of this is discouraged as it can have funny behaviours.
     *
     * @param keyMapping the vanilla keybind to emulate
     */
    ControllerBindingBuilder vanillaOverride(KeyMapping keyMapping);

    ControllerBinding build();
}
