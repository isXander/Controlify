package dev.isxander.controlify.api.event;

import dev.isxander.controlify.InputMode;
import dev.isxander.controlify.bindings.ControllerBindings;
import dev.isxander.controlify.controller.Controller;
import dev.isxander.controlify.api.buttonguide.ButtonGuideRegistry;
import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.event.EventFactory;

public final class ControlifyEvents {
    /**
     * Triggers when the input mode is changed from keyboard to controller or vice versa.
     */
    public static final Event<InputModeChanged> INPUT_MODE_CHANGED = EventFactory.createArrayBacked(InputModeChanged.class, callbacks -> mode -> {
        for (InputModeChanged callback : callbacks) {
            callback.onInputModeChanged(mode);
        }
    });

    /**
     * Triggers every tick when the current controller state has been updated.
     */
    public static final Event<ControllerStateUpdate> CONTROLLER_STATE_UPDATED = EventFactory.createArrayBacked(ControllerStateUpdate.class, callbacks -> controller -> {
        for (ControllerStateUpdate callback : callbacks) {
            callback.onControllerStateUpdate(controller);
        }
    });

    /**
     * Triggers when the button guide entries are being populated, so you can add more of your own.
     */
    public static final Event<ButtonGuideRegistryEvent> BUTTON_GUIDE_REGISTRY = EventFactory.createArrayBacked(ButtonGuideRegistryEvent.class, callbacks -> (bindings, registry) -> {
        for (ButtonGuideRegistryEvent callback : callbacks) {
            callback.onRegisterButtonGuide(bindings, registry);
        }
    });

    /**
     * Triggers in a GUI when the virtual mouse is toggled on or off.
     */
    public static final Event<VirtualMouseToggled> VIRTUAL_MOUSE_TOGGLED = EventFactory.createArrayBacked(VirtualMouseToggled.class, callbacks -> enabled -> {
        for (VirtualMouseToggled callback : callbacks) {
            callback.onVirtualMouseToggled(enabled);
        }
    });

    @FunctionalInterface
    public interface InputModeChanged {
        void onInputModeChanged(InputMode mode);
    }

    @FunctionalInterface
    public interface ControllerStateUpdate {
        void onControllerStateUpdate(Controller<?, ?> controller);
    }

    @FunctionalInterface
    public interface ButtonGuideRegistryEvent {
        void onRegisterButtonGuide(ControllerBindings<?> bindings, ButtonGuideRegistry registry);
    }

    @FunctionalInterface
    public interface VirtualMouseToggled {
        void onVirtualMouseToggled(boolean enabled);
    }
}
