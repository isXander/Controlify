package dev.isxander.controlify.controller;

import net.minecraft.resources.Identifier;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public interface ECSEntity {
    Map<Identifier, ECSComponent> getAllComponents();

    <T extends ECSComponent> boolean setComponent(T component);

    boolean removeComponent(Identifier id);

    <T extends ECSComponent> Optional<T> getComponent(Identifier id);
}
