package dev.isxander.controlify.api.event;

import dev.isxander.controlify.InputMode;
import dev.isxander.controlify.api.ingameinput.LookInputModifier;
import dev.isxander.controlify.api.ingameguide.IngameGuideRegistry;
import dev.isxander.controlify.controller.ControllerEntity;
import dev.isxander.controlify.platform.EventHandler;

public final class ControlifyEvents {
    public static final EventHandler<ControllerConnected> CONTROLLER_CONNECTED = EventHandler.createPlatformBackedEvent();

    public static final EventHandler<ControllerDisconnected> CONTROLLER_DISCONNECTED = EventHandler.createPlatformBackedEvent();

    /**
     * Triggers when the input mode is changed from keyboard to controller or vice versa.
     */
    public static final EventHandler<InputModeChanged> INPUT_MODE_CHANGED = EventHandler.createPlatformBackedEvent();

    /**
     * Triggers every tick when the current controller state has been updated.
     */
    public static final EventHandler<ControllerStateUpdate> ACTIVE_CONTROLLER_TICKED = EventHandler.createPlatformBackedEvent();

    /**
     * @deprecated Use {@link #ACTIVE_CONTROLLER_TICKED} instead.
     */
    @Deprecated
    public static final EventHandler<ControllerStateUpdate> CONTROLLER_STATE_UPDATED = ACTIVE_CONTROLLER_TICKED;

    /**
     * Triggers every tick when any connected controller's state has been updated before the active controller is ticked.
     */
    public static final EventHandler<ControllerStateUpdate> CONTROLLER_STATE_UPDATE = EventHandler.createPlatformBackedEvent();

    /**
     * Triggers when the button guide entries are being populated, so you can add more of your own.
     */
    public static final EventHandler<IngameGuideRegistryEvent> INGAME_GUIDE_REGISTRY = EventHandler.createPlatformBackedEvent();

    /**
     * Triggers in a GUI when the virtual mouse is toggled on or off.
     */
    public static final EventHandler<VirtualMouseToggled> VIRTUAL_MOUSE_TOGGLED = EventHandler.createPlatformBackedEvent();

    /**
     * Allows you to modify the look input before it is applied to the player.
     * These modifiers are called before the look input is multiplied by the sensitivity.
     */
    public static final EventHandler<LookInputModifier> LOOK_INPUT_MODIFIER = EventHandler.createPlatformBackedEvent();

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
