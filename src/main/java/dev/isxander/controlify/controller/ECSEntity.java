package dev.isxander.controlify.controller;

import net.minecraft.resources.ResourceLocation;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public interface ECSEntity {
    Map<ResourceLocation, ECSComponent> getAllComponents();

    <T extends ECSComponent> boolean setComponent(T component, ResourceLocation id);

    boolean removeComponent(ResourceLocation id);

    <T extends ECSComponent> Optional<T> getComponent(ResourceLocation id);
}
