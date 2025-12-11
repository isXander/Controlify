package dev.isxander.controlify.controller.input;

import net.minecraft.resources.Identifier;

public interface ModifiableControllerState extends ControllerState {
    void setButton(Identifier button, boolean pressed);

    void setAxis(Identifier axis, float value);

    void setHat(Identifier hat, HatState state);
}
