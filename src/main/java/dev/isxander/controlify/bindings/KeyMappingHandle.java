package dev.isxander.controlify.bindings;

import dev.isxander.controlify.controller.ControllerEntity;

import java.util.function.BooleanSupplier;

public interface KeyMappingHandle {
    void controlify$setPressed(boolean isDown);

    void controlify$forceSetPressed(boolean isDown);

    void controlify$addToggleCondition(ControllerEntity controller, BooleanSupplier condition);
}
