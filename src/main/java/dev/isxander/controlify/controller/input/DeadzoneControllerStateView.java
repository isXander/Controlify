package dev.isxander.controlify.controller.input;

import dev.isxander.controlify.utils.ControllerUtils;
import net.minecraft.resources.Identifier;

import java.util.Optional;
import java.util.Set;

public class DeadzoneControllerStateView implements ControllerStateView {
    private final ControllerStateView view;
    private final InputComponent input;

    public DeadzoneControllerStateView(ControllerStateView view, InputComponent input) {
        this.view = view;
        this.input = input;
    }

    @Override
    public boolean isButtonDown(Identifier button) {
        return view.isButtonDown(button);
    }

    @Override
    public Set<Identifier> getButtons() {
        return view.getButtons();
    }

    @Override
    public float getAxisState(Identifier axis) {
        float rawAxis = view.getAxisState(axis);
        Optional<Identifier> deadzoneId = input.getDeadzoneForAxis(axis);
        float deadzone = deadzoneId.map(id -> input.settings().deadzones.get(id)).orElse(0f);

        return ControllerUtils.deadzone(rawAxis, deadzone);
    }

    @Override
    public Set<Identifier> getAxes() {
        return view.getAxes();
    }

    @Override
    public float getAxisResting(Identifier axis) {
        return view.getAxisResting(axis);
    }

    @Override
    public HatState getHatState(Identifier hat) {
        return view.getHatState(hat);
    }

    @Override
    public Set<Identifier> getHats() {
        return view.getHats();
    }
}
