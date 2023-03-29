package dev.isxander.controlify.api.bind;

import dev.isxander.controlify.bindings.ControllerBinding;
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
        return new ControllerBinding.ControllerBindingBuilderImpl<>(controller);
    }

    ControllerBindingBuilder<T> identifier(ResourceLocation id);

    ControllerBindingBuilder<T> identifier(String namespace, String path);

    ControllerBindingBuilder<T> defaultBind(IBind<T> bind);

    ControllerBindingBuilder<T> defaultBind(GamepadBinds gamepadBind);

    ControllerBindingBuilder<T> name(Component name);

    ControllerBindingBuilder<T> description(Component description);

    ControllerBindingBuilder<T> category(Component category);

    ControllerBindingBuilder<T> vanillaOverride(KeyMapping keyMapping, BooleanSupplier toggleable);

    ControllerBindingBuilder<T> vanillaOverride(KeyMapping keyMapping);

    ControllerBinding<T> build();
}
