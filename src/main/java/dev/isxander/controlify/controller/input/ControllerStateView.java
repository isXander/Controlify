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

    default boolean isGivingInput() {
        return getButtons().stream().anyMatch(this::isButtonDown)
                || getAxes().stream().map(this::getAxisState).anyMatch(axis -> Math.abs(axis) > 0.5f)
                || getHats().stream().map(this::getHatState).anyMatch(hat -> hat != HatState.CENTERED);
    }
}
