package dev.isxander.controlify.config.gui;

import dev.isxander.controlify.api.event.ControlifyEvents;

public class ControllerBindHandler {
    public static ControlifyEvents.ControllerStateUpdate CURRENT_BIND_LISTENER = controller -> {};

    public static void setBindListener(ControlifyEvents.ControllerStateUpdate listener) {
        CURRENT_BIND_LISTENER = listener;
    }

    public static void clearBindListener() {
        CURRENT_BIND_LISTENER = controller -> {};
    }

    public static void setup() {
        ControlifyEvents.CONTROLLER_STATE_UPDATE.register(controller -> {
            CURRENT_BIND_LISTENER.onControllerStateUpdate(controller);
        });
    }
}
