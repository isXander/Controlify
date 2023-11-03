package dev.isxander.controlify.controllermanager;

import dev.isxander.controlify.controller.Controller;

import java.util.List;
import java.util.Optional;

public interface ControllerManager {
    void discoverControllers();

    void tick(boolean outOfFocus);

    boolean probeConnectedControllers();

    List<Controller<?, ?>> getConnectedControllers();

    Optional<Controller<?, ?>> getController(int jid);

    boolean isControllerConnected(String uid);

    boolean isControllerGamepad(int jid);

    void close();
}
