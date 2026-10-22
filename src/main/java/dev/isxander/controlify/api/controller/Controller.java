package dev.isxander.controlify.api.controller;

import dev.isxander.controlify.api.controller.rumble.RumbleComponent;
import dev.isxander.controlify.api.controller.input.InputComponent;
import dev.isxander.controlify.api.controller.gyro.GyroComponent;
import net.minecraft.resources.Identifier;

import java.util.Optional;

/// The API-safe controller instance.
public interface Controller {
	/// The unique identifier of this controller.
	/// The same physical controller should have the same
	/// identifier after being unplugged and replugged.
	String uid();

	/// The friendly name of this controller.
	///
	/// This may use {@link driverReportedName}, but the
	/// controller's type may override it, or the user may override
	/// it.
	///
	/// Because the user may override this, it is not a
	/// guarantee that this name is constant for the lifetime
	/// of this controller object.
	String name();

	/// The friendly name of this controller reported by
	/// the underlying controller driver.
	///
	/// To get the display name of this controller, you
	/// should use {@link name}.
	String driverReportedName();

	/// The identifier of this controller's type.
	/// This is the same ID defined by `controller_identification.json5`.
	Identifier typeId();

	/// Input capabilities and current actions.
	InputComponent input();

	/// Present if this controller supports gyroscope input.
	Optional<? extends GyroComponent> gyro();

	/// Get the rumble component that may be attached to this controller.
	/// If the returned optional is empty, then the controller is not capable of rumble.
	Optional<? extends RumbleComponent> rumble();
}
