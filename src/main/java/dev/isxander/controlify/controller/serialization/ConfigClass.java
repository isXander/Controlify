package dev.isxander.controlify.controller.serialization;

import dev.isxander.controlify.config.ValueInput;
import dev.isxander.controlify.config.ValueOutput;
import dev.isxander.controlify.controller.ControllerEntity;

public interface ConfigClass {
    void save(ValueOutput output, ControllerEntity controller);

    void load(ValueInput input, ControllerEntity controller);
}
