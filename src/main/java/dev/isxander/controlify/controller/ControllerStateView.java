package dev.isxander.controlify.controller;

import net.minecraft.resources.ResourceLocation;

import java.util.Set;

public interface ControllerStateView {
    boolean isButtonDown(ResourceLocation button);

    Set<ResourceLocation> getButtons();

    float getAxisState(ResourceLocation axis);
    Set<ResourceLocation> getAxes();

    float getAxisResting(ResourceLocation axis);

    HatState getHatState(ResourceLocation hat);
    Set<ResourceLocation> getHats();
}
