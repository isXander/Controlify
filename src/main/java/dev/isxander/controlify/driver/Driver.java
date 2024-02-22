package dev.isxander.controlify.driver;

import dev.isxander.controlify.controller.ControllerEntity;

public interface Driver {
    void update(boolean outOfFocus);

    ControllerEntity getController();

    void close();
}
