package dev.isxander.controlify.controller;

import net.minecraft.resources.Identifier;

public class SingleValueComponent<T> implements ECSComponent {
    private final T value;
    private final Identifier id;

    public SingleValueComponent(T value, Identifier id) {
        this.value = value;
        this.id = id;
    }

    public T value() {
        return this.value;
    }

    @Override
    public Identifier id() {
        return this.id;
    }
}
