package dev.isxander.controlify.controller;

import net.minecraft.resources.Identifier;

public interface ECSComponent {
    Identifier id();

    default void finalise() {

    }
}
