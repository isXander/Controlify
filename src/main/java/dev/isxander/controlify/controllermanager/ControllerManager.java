package dev.isxander.controlify.controllermanager;

import dev.isxander.controlify.controller.ControllerEntity;

import java.util.List;

public interface ControllerManager {
    void discoverControllers();

    void tick(boolean outOfFocus);

    boolean probeConnectedControllers();

    List<ControllerEntity> getConnectedControllers();

    boolean isControllerConnected(String uid);

    boolean isControllerGamepad(UniqueControllerID ucid);

    void closeController(String uid);

    void close();
}
