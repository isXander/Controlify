package dev.isxander.controlify.input.action;

/// A channel is a way of interpreting the output state of a given {@link Action}.
/// An {@link Action action's} bindings sends output to any of these channels.
public sealed interface Channel {
	/// A channel which gives a continuous float value, such as an axis/joystick.
	non-sealed interface Continuous extends Channel {
		float getContinuous();
	}

	/// A channel which gives single pulses, for example a button press causing a jump.
	non-sealed interface Pulse extends Channel {
		boolean consumePulse();
	}

	/// A channel which gives a stateful digital output of the action, for example a physical switch's state.
	non-sealed interface Latch extends Channel {
		boolean isLatchActive();
	}
}
