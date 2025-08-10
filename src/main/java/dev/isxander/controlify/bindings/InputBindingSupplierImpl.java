package dev.isxander.controlify.bindings;

import dev.isxander.controlify.api.bind.InputBinding;
import dev.isxander.controlify.api.bind.InputBindingSupplier;
import dev.isxander.controlify.controller.ControllerEntity;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.Nullable;

public record InputBindingSupplierImpl(ResourceLocation bindId) implements InputBindingSupplier {
    @Override
    public @Nullable InputBinding onOrNull(ControllerEntity controller) {
        return controller.input().map(c -> c.getBinding(bindId)).orElse(null);
    }
}
