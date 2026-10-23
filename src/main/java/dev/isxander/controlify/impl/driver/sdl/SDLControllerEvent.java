/*
 * Copyright (C) 2026 isXander
 * This file is part of Controlify.
 *
 * SPDX-License-Identifier: LGPL-3.0-or-later
 */
package dev.isxander.controlify.impl.driver.sdl;

import dev.isxander.sdl.SdlJoystickId;

public sealed interface SDLControllerEvent {

	long timeNanos();

	/// Emitted once after each backend poll and delivered to every controller.
	/// Uses the same SDL clock as native event timestamps; the first frame has a zero delta.
	record Frame(long timeNanos, long deltaTimeNanos) implements SDLControllerEvent {}

	sealed interface DeviceScopedEvent extends SDLControllerEvent {
		SdlJoystickId which();
	}

	sealed interface JoystickEvent extends DeviceScopedEvent {}
	sealed interface GamepadEvent extends DeviceScopedEvent {}

	sealed interface DeviceEvent extends DeviceScopedEvent {
		Type type();

		enum Type {
			ADDED,
			REMOVED,
			UPDATE_COMPLETE,
			/// Gamepad only
			REMAPPED,
			/// Gamepad only
			STEAM_HANDLE_UPDATED,
		}
	}

	sealed interface AxisEvent extends DeviceScopedEvent {
		/// The axis. It may be an index or a constant, depending on the event type.
		byte axis();

		/// The axis value, in the range [-32768, 32767]
		short value();
	}

	sealed interface ButtonEvent extends DeviceScopedEvent {
		/// The button. It may be an index or a constant, depending on the event type.
		byte button();

		/// True if the button is pressed, false if released.
		boolean down();
	}

	/// A joystick device event.
	/// @param which the joystick ID
	/// @param timeNanos the event timestamp in nanoseconds
	/// @param type the type of device event
	record JoyDevice(
			SdlJoystickId which,
			long timeNanos,
			Type type
	) implements JoystickEvent, DeviceEvent {}

	/// A joystick axis motion event.
	/// @param which the joystick ID
	/// @param timeNanos the event timestamp in nanoseconds
	/// @param axis the axis index
	/// @param value the axis value, in the range [-32768, 32767]
	record JoyAxis(
			SdlJoystickId which,
			long timeNanos,
			byte axis,
			short value
	) implements JoystickEvent, AxisEvent {}

	/// A joystick button event.
	/// @param which the joystick ID
	/// @param timeNanos the event timestamp in nanoseconds
	/// @param button the button index
	/// @param down true if the button is pressed, false if released
	record JoyButton(
			SdlJoystickId which,
			long timeNanos,
			byte button,
			boolean down
	) implements JoystickEvent, ButtonEvent {}

	/// A raw joystick hat bitmask; converted to directional buttons before entering the input pipeline.
	record JoyHat(SdlJoystickId which, long timeNanos, byte hat, byte value) implements JoystickEvent {}

	/// A controller battery level change event.
	/// @param which the joystick ID
	/// @param timeNanos the event timestamp in nanoseconds
	/// @param state the power state
	/// @param percent the battery percentage, in the range [0, 100], or -1 if unknown
	record Battery(
			SdlJoystickId which,
			long timeNanos,
			PowerState state,
			int percent
	) implements DeviceScopedEvent {
		public enum PowerState {
			ERROR,
			UNKNOWN,
			ON_BATTERY,
			NO_BATTERY,
			CHARGING,
			CHARGED,
		}
	}

	/// A gamepad device event.
	/// @param which the joystick ID
	/// @param timeNanos the event timestamp in nanoseconds
	/// @param type the type of device event
	record GamepadDevice(
			SdlJoystickId which,
			long timeNanos,
			Type type
	) implements GamepadEvent, DeviceEvent {}

	/// A gamepad axis motion event.
	/// @param which the joystick ID
	/// @param timeNanos the event timestamp in nanoseconds
	/// @param axis the axis
	/// @param value the axis value, in the range [-32768, 32767]
	record GamepadAxis(
			SdlJoystickId which,
			long timeNanos,
			byte axis,
			short value
	) implements GamepadEvent, AxisEvent {}

	/// A gamepad button event.
	/// @param which the joystick ID
	/// @param timeNanos the event timestamp in nanoseconds
	/// @param button the button
	/// @param down true if the button is pressed, false if released
	record GamepadButton(
			SdlJoystickId which,
			long timeNanos,
			byte button,
			boolean down
	) implements GamepadEvent, ButtonEvent {}

	/// A gamepad touchpad event.
	/// @param which the joystick ID
	/// @param timeNanos the event timestamp in nanoseconds
	/// @param action the action this event represents
	/// @param touchpad the index of the touchpad
	/// @param finger the index of the finger on the touchpad
	/// @param x normalized in the range [0, 1] with 0 being on the left
	/// @param y normalized in the range [0, 1] with 0 being at the top
	/// @param pressure normalized in the range [0, 1]
	record GamepadTouchpad(
			SdlJoystickId which,
			long timeNanos,
			Action action,
			int touchpad,
			int finger,
			float x,
			float y,
			float pressure
	) implements GamepadEvent {
		public enum Action {
			DOWN,
			MOTION,
			UP,
		}
	}

	/// A gamepad capsense event.
	/// Monitors capacitive touch sensors on controllers.
	/// @param which the joystick ID
	/// @param timeNanos the event timestamp in nanoseconds
	/// @param capsense the capsense type
	/// @param down true if the capsense is touched
	record GamepadCapSense(
			SdlJoystickId which,
			long timeNanos,
			byte capsense,
			boolean down
	) implements GamepadEvent {}

	/// A gamepad sensor event
	/// @param which the joystick ID
	/// @param timeNanos the event timestamp in nanoseconds
	/// @param sensor the sensor type
	/// @param x the first component of the sensor data
	/// @param y the second component of the sensor data,
	///          may be 0 if the sensor type is 1-dimensional
	/// @param z the third component of the sensor data,
	///          may be 0 if the sensor type is not 3-dimensional
	/// @param sensorTimestamp the timestamp of the sensor reading in nanoseconds,
	///                        not necessarily synchronized with the system clock
	record GamepadSensor(
			SdlJoystickId which,
			long timeNanos,
			int sensor,
			float x,
			float y,
			float z,
			long sensorTimestamp
	) implements GamepadEvent {}
}
