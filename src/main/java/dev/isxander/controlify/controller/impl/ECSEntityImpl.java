package dev.isxander.controlify.controller.impl;

import com.google.common.collect.ImmutableMap;
import dev.isxander.controlify.controller.ECSComponent;
import dev.isxander.controlify.controller.ECSEntity;
import net.minecraft.resources.Identifier;
import org.jetbrains.annotations.Contract;

import java.util.*;

public class ECSEntityImpl implements ECSEntity {
    private final Map<Identifier, ECSComponent> components;

    public ECSEntityImpl() {
        this.components = new HashMap<>();
    }

    @SuppressWarnings("unchecked")
    @Override
    @Contract(pure = true)
    public <T extends ECSComponent> Optional<T> getComponent(Identifier id) {
        return Optional.ofNullable((T) this.components.get(id));
    }

    @Override
    public <T extends ECSComponent> boolean setComponent(T component) {
        return this.components.put(component.id(), component) != null;
    }

    @Override
    public boolean removeComponent(Identifier id) {
        return this.components.remove(id) != null;
    }

    @Override
    public Map<Identifier, ECSComponent> getAllComponents() {
        return ImmutableMap.copyOf(this.components);
    }
}
