package dev.isxander.controlify.controller;

import dev.isxander.controlify.utils.ControllerUtils;
import net.minecraft.resources.ResourceLocation;

import java.util.Set;

public class DeadzoneControllerStateView implements ControllerStateView {
    private final ControllerStateView view;
    private final IConfig<InputComponent.Config> config;

    public DeadzoneControllerStateView(ControllerStateView view, IConfig<InputComponent.Config> config) {
        this.view = view;
        this.config = config;
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
        float deadzone = config.config().deadzones.getOrDefault(axis, 0f);

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
