package dev.isxander.controlify.driver.sdl;

import dev.isxander.controlify.controller.input.JoystickInputs;
import dev.isxander.controlify.input.SensorType;
import dev.isxander.controlify.input.physical.PhysicalInputSignal;
import dev.isxander.controlify.input.physical.PhysicalSensorSignal;
import dev.isxander.controlify.input.physical.PhysicalSignal;
import dev.isxander.controlify.utils.event.EventSink;
import dev.isxander.controlify.utils.event.EventStage;
import net.minecraft.resources.Identifier;

import java.util.Arrays;

public class SDLToPhysicalStage implements EventStage<SDLControllerEvent, PhysicalSignal> {
	private final float[] prevGamepadAxisValues = new float[64];
	private final float[] prevJoystickAxisValues = new float[64];

	public SDLToPhysicalStage() {
		Arrays.fill(this.prevGamepadAxisValues, 0f);
		Arrays.fill(this.prevJoystickAxisValues, 0f);
	}

	@Override
	public void onEvent(SDLControllerEvent event, EventSink<? super PhysicalSignal> downstream) {
		switch (event) {
			case SDLControllerEvent.JoyAxis(_, long timeNanos, byte axis, short value) -> {
				float normalisedValue = normaliseAxis(value);

				float prevNormalisedValue = this.prevJoystickAxisValues[axis];
				this.prevJoystickAxisValues[axis] = normalisedValue;

				boolean positive = normalisedValue > 0;
				boolean prevPositive = prevNormalisedValue > 0;

				if (prevPositive != positive) {
					// sign changed
					Identifier prevInput = JoystickInputs.axis(axis, prevPositive);
					downstream.accept(new PhysicalInputSignal.AxisMoved(timeNanos, prevInput, 0f));
				}

				Identifier input = JoystickInputs.axis(axis, positive);
				downstream.accept(new PhysicalInputSignal.AxisMoved(timeNanos, input, Math.abs(normalisedValue)));
			}

			case SDLControllerEvent.JoyButton(_, long timeNanos, byte button, boolean down) ->
				downstream.accept(new PhysicalInputSignal.ButtonChanged(timeNanos, JoystickInputs.button(button), down));

			case SDLControllerEvent.GamepadAxis(_, long timeNanos, byte axis, short value) -> {
				float normalisedValue = normaliseAxis(value);

				float prevNormalisedValue = this.prevGamepadAxisValues[axis];
				this.prevGamepadAxisValues[axis] = normalisedValue;

				boolean positive = normalisedValue > 0;
				boolean prevPositive = prevNormalisedValue > 0;

				if (prevPositive != positive) {
					// sign changed
					Identifier prevInput = SDLInputConversions.getGamepadAxis(axis, prevPositive);
					if (prevInput != null) {
						downstream.accept(new PhysicalInputSignal.AxisMoved(timeNanos, prevInput, 0f));
					}
				}

				Identifier input = SDLInputConversions.getGamepadAxis(axis, positive);
				if (input != null) {
					downstream.accept(new PhysicalInputSignal.AxisMoved(timeNanos, input, Math.abs(normalisedValue)));
				}
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

	private static float normaliseAxis(short v) {
		return v < 0 ? (v / 32768f) : (v / 32767f);
	}
}
