package dev.isxander.controlify.controller.input;

import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;

import java.util.Collection;

public final class Inputs {
    private Inputs() {
    }

    public static MutableComponent getInputComponent(ResourceLocation input) {
        return Component.translatable("controlify.input." + input.getNamespace() + "." + input.getPath());
    }

    public static MutableComponent getInputComponentAnd(Collection<ResourceLocation> inputs) {
        if (inputs.isEmpty())
            return Component.empty();

        MutableComponent component = inputs.stream()
                .map(Inputs::getInputComponent)
                .reduce(Component.empty(), (a, b) -> a.append(b).append(" + "));

        // remove the last ' + '
        component.getSiblings().remove(component.getSiblings().size() - 1);

        return component;
    }
}
