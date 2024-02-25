package dev.isxander.controlify.controller.input;

import net.minecraft.resources.ResourceLocation;

import java.util.Set;

public record DeadzoneGroup(
        ResourceLocation name,
        Set<ResourceLocation> axes
) {
}
