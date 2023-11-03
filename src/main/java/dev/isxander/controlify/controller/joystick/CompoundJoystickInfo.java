package dev.isxander.controlify.controller.joystick;

import dev.isxander.controlify.Controlify;
import dev.isxander.controlify.controller.Controller;
import dev.isxander.controlify.controller.ControllerType;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

public record CompoundJoystickInfo(Collection<String> joystickUids, String friendlyName) {
    public ControllerType type() {
        return new ControllerType(friendlyName, createUID(joystickUids), "generic", true, false);
    }

    public boolean canBeUsed() {
        List<Controller<?, ?>> joysticks = Controlify.instance().getControllerManager().orElseThrow().getConnectedControllers().stream().filter(c -> joystickUids.contains(c.uid())).toList();
        if (joysticks.size() != joystickUids().size()) {
            return false; // not all controllers are connected
        }
        if (joysticks.stream().anyMatch(c -> !c.canBeUsed())) {
            return false; // not all controllers can be used
        }

        return true;
    }

    public boolean isLoaded() {
        return Controlify.instance().getControllerManager().orElseThrow().isControllerConnected(createUID(joystickUids));
    }

    public Optional<CompoundJoystickController> attemptCreate() {
        if (!canBeUsed()) return Optional.empty();

        List<Integer> joystickIDs = Controlify.instance().getControllerManager().orElseThrow().getConnectedControllers().stream()
                .filter(c -> joystickUids.contains(c.uid()))
                .map(Controller::joystickId)
                .toList();

        ControllerType type = type();
        return Optional.of(new CompoundJoystickController(joystickIDs, type.mappingId(), type));
    }

    public static String createUID(Collection<String> joystickUIDs) {
        return "compound-" + String.join("_", joystickUIDs);
    }
}
