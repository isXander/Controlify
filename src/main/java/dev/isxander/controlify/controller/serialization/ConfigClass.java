package dev.isxander.controlify.controller.serialization;

import dev.isxander.controlify.controller.ControllerEntity;

public interface ConfigClass {
    default void onConfigSaveLoad(ControllerEntity controller) {}
}
