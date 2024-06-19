package dev.isxander.controlify.controller.config;

import dev.isxander.controlify.controller.ControllerEntity;

public interface ConfigObject {
    default void onApply(ControllerEntity controller) {}

    default void onSave(ControllerEntity controller) {}

    default void applyControllerSpecificDefaults(ControllerEntity controller) {
    }
}
