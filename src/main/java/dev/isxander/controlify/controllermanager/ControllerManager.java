package dev.isxander.controlify.controllermanager;

import dev.isxander.controlify.controller.Controller;

import java.util.List;

public interface ControllerManager {
    void discoverControllers();

    void tick(boolean outOfFocus);

    boolean probeConnectedControllers();

    List<Controller<?>> getConnectedControllers();

    boolean isControllerConnected(String uid);

    boolean isControllerGamepad(UniqueControllerID ucid);

    void close();
}
