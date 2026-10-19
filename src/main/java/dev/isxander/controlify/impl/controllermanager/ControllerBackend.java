package dev.isxander.controlify.impl.controllermanager;

import dev.isxander.controlify.impl.Controlify;
import dev.isxander.controlify.impl.hid.HIDID;

/// A controller backend is responsible for listening to controller events
/// and creating controllers using the referenced {@link ControllerManager controller manager}.
public abstract class ControllerBackend {
	protected final Controlify controlify;
	protected final ControllerManager controllerManager;

	public ControllerBackend(Controlify controlify, ControllerManager controllerManager) {
		this.controlify = controlify;
		this.controllerManager = controllerManager;
	}

	/// Called once and allows the backend to do any first-time setup
	/// before beginning to poll events and discover controllers.
	public abstract void setup();

	/// Called once after {@link setup}, discovers already-connected
	/// controllers before ever polling events.
	public abstract void discoverControllers();

	/// Called once per frame, polls events from the backend and processes
	/// them, potentially creating controllers and adding them to {@link ControllerManager}.
	public abstract void pollEvents();

	protected ControllerType getControllerType(HIDID hidId) {
		return this.controlify.controllerTypeManager().getControllerType(hidId);
	}
}
