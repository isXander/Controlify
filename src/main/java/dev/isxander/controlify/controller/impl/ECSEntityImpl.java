package dev.isxander.controlify.controller.impl;

import com.google.common.collect.ImmutableMap;
import dev.isxander.controlify.controller.ECSComponent;
import dev.isxander.controlify.controller.ECSEntity;
import net.minecraft.resources.ResourceLocation;

import java.util.*;

public class ECSEntityImpl implements ECSEntity {
    private final Map<ResourceLocation, ECSComponent> components;

    public ECSEntityImpl() {
        this.components = new HashMap<>();
    }

    @SuppressWarnings("unchecked")
    @Override
    public <T extends ECSComponent> Optional<T> getComponent(ResourceLocation id) {
        return Optional.ofNullable((T) this.components.get(id));
    }

    @Override
    public <T extends ECSComponent> boolean setComponent(T component, ResourceLocation id) {
        return this.components.put(id, component) != null;
    }

    @Override
    public boolean removeComponent(ResourceLocation id) {
        return this.components.remove(id) != null;
    }

    @Override
    public Map<ResourceLocation, ECSComponent> getAllComponents() {
        return ImmutableMap.copyOf(this.components);
    }
}
