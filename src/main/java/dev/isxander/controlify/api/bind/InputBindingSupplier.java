package dev.isxander.controlify.api.bind;

import dev.isxander.controlify.controller.ControllerEntity;
import net.minecraft.resources.ResourceLocation;

public interface InputBindingSupplier {
    InputBinding on(ControllerEntity controller);

    ResourceLocation bindId();
}
