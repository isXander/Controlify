package dev.isxander.controlify.api;

import dev.isxander.controlify.api.utils.event.EventSource;
import dev.isxander.controlify.impl.Controlify;

public final class ControlifyEvents {
	/// Fires once when runtime managers and queries are usable, before backend setup
	/// and discovery. Queries initially return no controllers. Still fires if backend setup fails.
	public static final EventSource<ControlifyApi> INITIALIZED = Controlify.Events.INITIALIZED;

	/// Fires after Controlify has applied the new input mode and updated its handlers.
	public static final EventSource<InputModeChanged> INPUT_MODE_CHANGED = Controlify.Events.INPUT_MODE_CHANGED;

	public record InputModeChanged(ControlifyApi controlify, InputMode mode) {}

	private ControlifyEvents() {}
}
