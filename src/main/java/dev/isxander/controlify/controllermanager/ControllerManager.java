package dev.isxander.controlify.controllermanager;

import dev.isxander.controlify.controller.ControllerEntity;
import dev.isxander.controlify.hid.ControllerHIDService;

import java.util.List;
import java.util.Optional;

public interface ControllerManager {
    void discoverControllers();

    void tick(boolean outOfFocus);

    boolean probeConnectedControllers();

    List<ControllerEntity> getConnectedControllers();

    boolean isControllerConnected(String uid);

    boolean isControllerGamepad(UniqueControllerID ucid);

    Optional<ControllerEntity> reinitController(ControllerEntity controller, ControllerHIDService.ControllerHIDInfo hidInfo);

    void closeController(String uid);

    void close();
}
