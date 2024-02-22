package dev.isxander.controlify.controller;

import net.minecraft.resources.ResourceLocation;

public interface ModifiableControllerState extends ControllerState {
    void setButton(ResourceLocation button, boolean pressed);

    void setAxis(ResourceLocation axis, float value);

    void setHat(ResourceLocation hat, HatState state);
}
