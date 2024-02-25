package dev.isxander.controlify.controller.input;

import dev.isxander.controlify.controller.IConfig;
import dev.isxander.controlify.utils.ControllerUtils;
import net.minecraft.resources.ResourceLocation;

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
    public boolean isButtonDown(ResourceLocation button) {
        return view.isButtonDown(button);
    }

    @Override
    public Set<ResourceLocation> getButtons() {
        return view.getButtons();
    }

    @Override
    public float getAxisState(ResourceLocation axis) {
        float rawAxis = view.getAxisState(axis);
        Optional<ResourceLocation> deadzoneId = input.getDeadzoneForAxis(axis);
        float deadzone = deadzoneId.map(id -> input.confObj().deadzones.get(id)).orElse(0f);

        return ControllerUtils.deadzone(rawAxis, deadzone);
    }

    @Override
    public Set<ResourceLocation> getAxes() {
        return view.getAxes();
    }

    @Override
    public float getAxisResting(ResourceLocation axis) {
        return view.getAxisResting(axis);
    }

    @Override
    public HatState getHatState(ResourceLocation hat) {
        return view.getHatState(hat);
    }

    @Override
    public Set<ResourceLocation> getHats() {
        return view.getHats();
    }
}
