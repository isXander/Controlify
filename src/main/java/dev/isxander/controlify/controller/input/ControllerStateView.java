package dev.isxander.controlify.controller.input;

import net.minecraft.resources.Identifier;

import java.util.Set;

public interface ControllerStateView {
    boolean isButtonDown(Identifier button);

    Set<Identifier> getButtons();

    float getAxisState(Identifier axis);
    Set<Identifier> getAxes();

    float getAxisResting(Identifier axis);

    HatState getHatState(Identifier hat);
    Set<Identifier> getHats();
}
