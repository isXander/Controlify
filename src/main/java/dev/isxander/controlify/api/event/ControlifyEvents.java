package dev.isxander.controlify.api.event;

import dev.isxander.controlify.InputMode;
import dev.isxander.controlify.api.ingameinput.LookInputModifier;
import dev.isxander.controlify.api.ingameguide.IngameGuideRegistry;
import dev.isxander.controlify.controller.ControllerEntity;
import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.event.EventFactory;

public final class ControlifyEvents {
    public static final Event<ControllerConnected> CONTROLLER_CONNECTED = EventFactory.createArrayBacked(ControllerConnected.class, callbacks -> (controller, hotplugged, newController) -> {
        for (ControllerConnected callback : callbacks) {
            callback.onControllerConnected(controller, hotplugged, newController);
        }
    });

    public static final Event<ControllerDisconnected> CONTROLLER_DISCONNECTED = EventFactory.createArrayBacked(ControllerDisconnected.class, callbacks -> controller -> {
        for (ControllerDisconnected callback : callbacks) {
            callback.onControllerDisconnected(controller);
        }
    });

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
    public static final Event<ControllerStateUpdate> ACTIVE_CONTROLLER_TICKED = EventFactory.createArrayBacked(ControllerStateUpdate.class, callbacks -> controller -> {
        for (ControllerStateUpdate callback : callbacks) {
            callback.onControllerStateUpdate(controller);
        }
    });

    /**
     * @deprecated Use {@link #ACTIVE_CONTROLLER_TICKED} instead.
     */
    @Deprecated
    public static final Event<ControllerStateUpdate> CONTROLLER_STATE_UPDATED = ACTIVE_CONTROLLER_TICKED;

    /**
     * Triggers every tick when any connected controller's state has been updated before the active controller is ticked.
     */
    public static final Event<ControllerStateUpdate> CONTROLLER_STATE_UPDATE = EventFactory.createArrayBacked(ControllerStateUpdate.class, callbacks -> controller -> {
        for (ControllerStateUpdate callback : callbacks) {
            callback.onControllerStateUpdate(controller);
        }
    });

    /**
     * Triggers when the button guide entries are being populated, so you can add more of your own.
     */
    public static final Event<IngameGuideRegistryEvent> INGAME_GUIDE_REGISTRY = EventFactory.createArrayBacked(IngameGuideRegistryEvent.class, callbacks -> (bindings, registry) -> {
        for (IngameGuideRegistryEvent callback : callbacks) {
            callback.onRegisterIngameGuide(bindings, registry);
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

    /**
     * Allows you to modify the look input before it is applied to the player.
     * These modifiers are called before the look input is multiplied by the sensitivity.
     */
    public static final Event<LookInputModifier> LOOK_INPUT_MODIFIER = EventFactory.createArrayBacked(LookInputModifier.class, callbacks -> new LookInputModifier() {
        @Override
        public float modifyX(float x, ControllerEntity controller) {
            for (LookInputModifier callback : callbacks) {
                x = callback.modifyX(x, controller);
            }
            return x;
        }

        @Override
        public float modifyY(float y, ControllerEntity controller) {
            for (LookInputModifier callback : callbacks) {
                y = callback.modifyY(y, controller);
            }
            return y;
        }
    });

    @FunctionalInterface
    public interface ControllerConnected {
        void onControllerConnected(ControllerEntity controller, boolean hotplugged, boolean newController);
    }

    @FunctionalInterface
    public interface ControllerDisconnected {
        void onControllerDisconnected(ControllerEntity controller);
    }

    @FunctionalInterface
    public interface InputModeChanged {
        void onInputModeChanged(InputMode mode);
    }

    @FunctionalInterface
    public interface ControllerStateUpdate {
        void onControllerStateUpdate(ControllerEntity controller);
    }

    @FunctionalInterface
    public interface IngameGuideRegistryEvent {
        void onRegisterIngameGuide(ControllerEntity controller, IngameGuideRegistry registry);
    }

    @FunctionalInterface
    public interface VirtualMouseToggled {
        void onVirtualMouseToggled(boolean enabled);
    }

}
