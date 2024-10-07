package dev.isxander.controlify.driver;

import dev.isxander.controlify.controller.ControllerEntity;
import dev.isxander.controlify.controller.ECSComponent;

import java.util.Collection;
import java.util.function.Consumer;

public interface Driver {
    String getDriverName();

    void addComponents(ControllerEntity controller);

    void update(ControllerEntity controller, boolean outOfFocus);

    void close();
}
