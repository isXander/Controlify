package dev.isxander.controlify.api.controllermanager;

import dev.isxander.controlify.api.ControlifyApi;
import dev.isxander.controlify.api.controller.Controller;
import dev.isxander.controlify.api.utils.event.EventSource;
import dev.isxander.controlify.impl.controllermanager.ControllerCoordinator;
import org.jspecify.annotations.Nullable;

public final class ControllerEvents {
	/// Fires once after initial discovery, selection and config saving, including an empty
	/// result. Does not fire if the backend failed to initialize.
	public static final EventSource<ControlifyApi> DISCOVERY_COMPLETED = ControllerCoordinator.Events.DISCOVERY_COMPLETED;

	/// Fires after insertion and internal connection setup. Selection notifications may fire first.
	public static final EventSource<ControllerConnected> CONTROLLER_CONNECTED = ControllerCoordinator.Events.CONTROLLER_CONNECTED;

	/// Fires after removal and selection updates, before closing the controller. It is
	/// absent from queries and must not be used after the callback returns.
	public static final EventSource<ControllerDisconnected> CONTROLLER_DISCONNECTED = ControllerCoordinator.Events.CONTROLLER_DISCONNECTED;

	/// Fires after activation, input-mode and handler updates. Null means selection was cleared.
	public static final EventSource<CurrentControllerChanged> CURRENT_CONTROLLER_CHANGED = ControllerCoordinator.Events.CURRENT_CONTROLLER_CHANGED;

	public record ControllerConnected(ControlifyApi controlify, Controller controller, boolean hotplugged) {}
	public record ControllerDisconnected(ControlifyApi controlify, Controller controller) {}
	public record CurrentControllerChanged(ControlifyApi controlify, @Nullable Controller controller) {}

	private ControllerEvents() {}
}
