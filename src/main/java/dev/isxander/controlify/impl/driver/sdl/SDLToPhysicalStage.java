/*
 * Copyright (C) 2026 isXander
 * This file is part of Controlify.
 *
 * SPDX-License-Identifier: LGPL-3.0-or-later
 */
package dev.isxander.controlify.impl.driver.sdl;

import dev.isxander.controlify.impl.input.mapping.JoystickInputs;
import dev.isxander.controlify.impl.input.SensorType;
import dev.isxander.controlify.impl.input.signal.PhysicalInputSignal;
import dev.isxander.controlify.impl.input.signal.PhysicalSensorSignal;
import dev.isxander.controlify.impl.input.signal.PhysicalSignal;
import dev.isxander.controlify.api.utils.event.EventSink;
import dev.isxander.controlify.api.utils.event.EventStage;
import net.minecraft.resources.Identifier;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import java.util.Arrays;
import java.util.function.Function;

import static dev.isxander.sdl.SdlJoystick.*;

public class SDLToPhysicalStage implements EventStage<SDLControllerEvent, PhysicalSignal> {
	private final float[] prevGamepadAxisValues = new float[256];
	private final float[] prevJoystickAxisValues = new float[256];
	private final byte[] prevHatValues = new byte[256];

	public SDLToPhysicalStage() {
		Arrays.fill(this.prevGamepadAxisValues, 0f);
		Arrays.fill(this.prevJoystickAxisValues, 0f);
	}

	@Override
	public void onEvent(@NonNull SDLControllerEvent event, @NonNull EventSink<? super PhysicalSignal> downstream) {
		switch (event) {
			case SDLControllerEvent.Frame(long timeNanos, long deltaTimeNanos) ->
					downstream.accept(new PhysicalSignal.Frame(timeNanos, deltaTimeNanos));

			case SDLControllerEvent.JoyAxis(_, long timeNanos, byte axis, short value) -> {
				int index = Byte.toUnsignedInt(axis);
				emitSplitAxis(timeNanos, this.prevJoystickAxisValues, index, value,
						positive -> JoystickInputs.axis(index, positive), downstream);
			}

			case SDLControllerEvent.JoyButton(_, long timeNanos, byte button, boolean down) ->
				downstream.accept(new PhysicalInputSignal.ButtonChanged(timeNanos, JoystickInputs.button(Byte.toUnsignedInt(button)), down));

			case SDLControllerEvent.JoyHat(_, long timeNanos, byte hat, byte value) -> {
				int index = Byte.toUnsignedInt(hat);
				byte previous = this.prevHatValues[index];
				this.prevHatValues[index] = value;
				if (previous != value) {
					int released = previous & ~value;
					int pressed = value & ~previous;
					var directions = JoystickInputs.HatDirection.values();
					// Release changed directions before pressing new ones; retain shared directions.
					for (var direction : directions) {
						if ((released & hatMask(direction)) != 0) {
							downstream.accept(new PhysicalInputSignal.ButtonChanged(
									timeNanos, JoystickInputs.hat(index, direction), false));
						}
					}
					for (var direction : directions) {
						if ((pressed & hatMask(direction)) != 0) {
							downstream.accept(new PhysicalInputSignal.ButtonChanged(
									timeNanos, JoystickInputs.hat(index, direction), true));
						}
					}
				}
			}

			case SDLControllerEvent.GamepadAxis(_, long timeNanos, byte axis, short value) -> {
				int index = Byte.toUnsignedInt(axis);
				emitSplitAxis(timeNanos, this.prevGamepadAxisValues, index, value,
						positive -> SDLInputConversions.getGamepadAxis(index, positive), downstream);
			}

			case SDLControllerEvent.GamepadButton(_, long timeNanos, byte button, boolean down) -> {
				Identifier input = SDLInputConversions.getGamepadButton(button);
				if (input != null) {
					downstream.accept(new PhysicalInputSignal.ButtonChanged(timeNanos, input, down));
				}
			}

			case SDLControllerEvent.GamepadSensor(_, long timeNanos, int sensor, float x, float y, float z, long sensorTimestamp) -> {
				SensorType sensorType = SDLInputConversions.getSensorType(sensor);
				if (sensorType != null) {
					downstream.accept(new PhysicalSensorSignal(timeNanos, sensorTimestamp, sensorType, x, y, z));
				}
			}

			default -> {}
		}
	}

	/// Splits a signed axis into two half-axes.
	/// When the sign changes, the new half-axis is emitted before the old one is zeroed,
	/// so downstream radial deadzones never observe a transiently collapsed magnitude.
	private static void emitSplitAxis(
			long timeNanos, float[] prevValues, int index, short value,
			Function<Boolean, @Nullable Identifier> halfAxis,
			EventSink<? super PhysicalSignal> downstream
	) {
		float normalisedValue = normaliseAxis(value);

		float prevNormalisedValue = prevValues[index];
		prevValues[index] = normalisedValue;

		boolean positive = normalisedValue > 0;
		boolean prevPositive = prevNormalisedValue > 0;

		Identifier input = halfAxis.apply(positive);
		if (input != null) {
			downstream.accept(new PhysicalInputSignal.AxisMoved(timeNanos, input, Math.abs(normalisedValue)));
		}

		if (prevPositive != positive) {
			Identifier prevInput = halfAxis.apply(prevPositive);
			if (prevInput != null && !prevInput.equals(input)) {
				downstream.accept(new PhysicalInputSignal.AxisMoved(timeNanos, prevInput, 0f));
			}
		}
	}

	private static float normaliseAxis(short v) {
		return v < 0 ? (v / 32768f) : (v / 32767f);
	}

	private static int hatMask(JoystickInputs.HatDirection direction) {
		return switch (direction) {
			case UP -> SDL_HAT_UP;
			case RIGHT -> SDL_HAT_RIGHT;
			case DOWN -> SDL_HAT_DOWN;
			case LEFT -> SDL_HAT_LEFT;
		};
	}
}
