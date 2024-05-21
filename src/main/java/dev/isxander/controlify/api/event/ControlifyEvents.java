package dev.isxander.controlify.api.event;

import dev.isxander.controlify.InputMode;
import dev.isxander.controlify.api.ingameinput.LookInputModifier;
import dev.isxander.controlify.api.ingameguide.IngameGuideRegistry;
import dev.isxander.controlify.controller.ControllerEntity;
import dev.isxander.controlify.platform.Event;

public final class ControlifyEvents {
    public static final Event<ControllerConnected> CONTROLLER_CONNECTED = Event.createPlatformBackedEvent();

    public static final Event<ControllerDisconnected> CONTROLLER_DISCONNECTED = Event.createPlatformBackedEvent();

    /**
     * Triggers when the input mode is changed from keyboard to controller or vice versa.
     */
    public static final Event<InputModeChanged> INPUT_MODE_CHANGED = Event.createPlatformBackedEvent();

    /**
     * Triggers every tick when the current controller state has been updated.
     */
    public static final Event<ControllerStateUpdate> ACTIVE_CONTROLLER_TICKED = Event.createPlatformBackedEvent();

    /**
     * @deprecated Use {@link #ACTIVE_CONTROLLER_TICKED} instead.
     */
    @Deprecated
    public static final Event<ControllerStateUpdate> CONTROLLER_STATE_UPDATED = ACTIVE_CONTROLLER_TICKED;

    /**
     * Triggers every tick when any connected controller's state has been updated before the active controller is ticked.
     */
    public static final Event<ControllerStateUpdate> CONTROLLER_STATE_UPDATE = Event.createPlatformBackedEvent();

    /**
     * Triggers when the button guide entries are being populated, so you can add more of your own.
     */
    public static final Event<IngameGuideRegistryEvent> INGAME_GUIDE_REGISTRY = Event.createPlatformBackedEvent();

    /**
     * Triggers in a GUI when the virtual mouse is toggled on or off.
     */
    public static final Event<VirtualMouseToggled> VIRTUAL_MOUSE_TOGGLED = Event.createPlatformBackedEvent();

    /**
     * Allows you to modify the look input before it is applied to the player.
     * These modifiers are called before the look input is multiplied by the sensitivity.
     */
    public static final Event<LookInputModifier> LOOK_INPUT_MODIFIER = Event.createPlatformBackedEvent();

    public record ControllerConnected(ControllerEntity controller, boolean hotplugged, boolean newController) {
    }

    public record ControllerDisconnected(ControllerEntity controller) {
    }

    public record InputModeChanged(InputMode mode) {
    }

    public record ControllerStateUpdate(ControllerEntity controller) {
    }

    public record IngameGuideRegistryEvent(ControllerEntity bindings, IngameGuideRegistry registry) {
    }

    public record VirtualMouseToggled(boolean enabled) {
    }

}
