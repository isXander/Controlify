/*
 * Copyright (C) 2026 isXander
 * This file is part of Controlify.
 *
 * SPDX-License-Identifier: LGPL-3.0-or-later
 */
package dev.isxander.controlify.api.action;

/// A channel is a way of interpreting the output state of a given {@link Action}.
/// An {@link Action action's} bindings sends output to any of these channels.
public sealed interface Channel {
	/// A channel which gives a continuous float value, such as an axis/joystick.
	non-sealed interface Continuous extends Channel {
		float getContinuous();
	}

	/// A channel which gives single pulses, for example a button press causing a jump.
	non-sealed interface Pulse extends Channel {
		/// Consumes one available pulse
		/// Typically you would use this with a `while` statement.
		/// @return true if there was a pulse to consume
		boolean consumePulse();

		/// Consumes all available pulses
		/// Typically you would use this with an `if` statement.
		/// @return true if there was a pulse to consume
		boolean consumeAllPulses();
	}

	/// A channel which gives a stateful digital output of the action, for example a physical switch's state.
	non-sealed interface Latch extends Channel {
		boolean isLatchActive();
	}
}
