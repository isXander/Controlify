/*
 * Copyright (C) 2026 isXander
 * This file is part of Controlify.
 *
 * SPDX-License-Identifier: LGPL-3.0-or-later
 */
package dev.isxander.controlify.api;

import dev.isxander.controlify.impl.api.ingameinput.LookInputModifier;
import dev.isxander.controlify.impl.controller.ControllerImpl;
import dev.isxander.controlify.impl.platform.EventHandler;
import org.jetbrains.annotations.Nullable;

public final class ControlifyEvents {
	public static final EventHandler<ControllerConnected> CONTROLLER_CONNECTED = EventHandler.createPlatformBackedEvent();

	public static final EventHandler<ControllerDisconnected> CONTROLLER_DISCONNECTED = EventHandler.createPlatformBackedEvent();

	/**
	 * Triggers after the current controller changes and its activation has been updated.
	 * The controller is null when the current selection is cleared.
	 */
	public static final EventHandler<CurrentControllerChanged> CURRENT_CONTROLLER_CHANGED = EventHandler.createPlatformBackedEvent();

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
	 * Triggers in a GUI when the virtual mouse is toggled on or off.
	 */
	public static final EventHandler<VirtualMouseToggled> VIRTUAL_MOUSE_TOGGLED = EventHandler.createPlatformBackedEvent();

	/**
	 * Allows you to modify the look input before it is applied to the player.
	 * These modifiers are called before the look input is multiplied by the sensitivity.
	 */
	public static final EventHandler<LookInputModifier> LOOK_INPUT_MODIFIER = EventHandler.createPlatformBackedEvent();

	public record ControllerConnected(ControllerImpl controller, boolean hotplugged) {
	}

	public record ControllerDisconnected(ControllerImpl controller) {
	}

	public record CurrentControllerChanged(@Nullable ControllerImpl controller, boolean changeInputMode) {
	}

	public record InputModeChanged(InputMode mode) {
	}

	public record ControllerStateUpdate(ControllerImpl controller) {
	}

	public record VirtualMouseToggled(boolean enabled) {
	}

}
