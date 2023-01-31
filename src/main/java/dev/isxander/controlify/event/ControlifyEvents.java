package dev.isxander.controlify.event;

import dev.isxander.controlify.InputMode;
import dev.isxander.controlify.controller.Controller;
import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.event.EventFactory;

public class ControlifyEvents {
    public static final Event<InputModeChanged> INPUT_MODE_CHANGED = EventFactory.createArrayBacked(InputModeChanged.class, callbacks -> mode -> {
        for (InputModeChanged callback : callbacks) {
            callback.onInputModeChanged(mode);
        }
    });

    public static final Event<ControllerStateUpdate> CONTROLLER_STATE_UPDATED = EventFactory.createArrayBacked(ControllerStateUpdate.class, callbacks -> controller -> {
        for (ControllerStateUpdate callback : callbacks) {
            callback.onControllerStateUpdate(controller);
        }
    });

    @FunctionalInterface
    public interface InputModeChanged {
        void onInputModeChanged(InputMode mode);
    }

    @FunctionalInterface
    public interface ControllerStateUpdate {
        void onControllerStateUpdate(Controller controller);
    }
}
