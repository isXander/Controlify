package dev.isxander.controlify.controller.input;

import net.minecraft.resources.ResourceLocation;

import java.util.List;

public record DeadzoneGroup(
        ResourceLocation name,
        List<ResourceLocation> axes
) {
}
