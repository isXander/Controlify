package dev.isxander.controlify.controller.impl;

import dev.isxander.controlify.controller.ControllerEntity;
import dev.isxander.controlify.controller.ECSComponent;

public abstract class ECSComponentImpl implements ECSComponent {
    private ControllerEntity controller;

    protected final ControllerEntity controller() {
        return this.controller;
    }

    @Override
    public void attach(ControllerEntity controller) {
        this.controller = controller;
    }
}
