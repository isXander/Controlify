package dev.isxander.controlify.controller.composable;

import net.minecraft.resources.ResourceLocation;

public interface ModifiableControllerState extends ComposableControllerState {
    void setButton(ResourceLocation button, boolean pressed);

    void setAxis(ResourceLocation axis, float value);

    void setHat(ResourceLocation hat, HatState state);
}
