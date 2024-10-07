package dev.isxander.controlify.controller;

import net.minecraft.resources.ResourceLocation;

public interface ECSComponent {
    ResourceLocation id();

    default void finalise() {

    }
}
