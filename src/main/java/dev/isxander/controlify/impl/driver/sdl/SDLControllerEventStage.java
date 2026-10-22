package dev.isxander.controlify.impl.driver.sdl;

import dev.isxander.controlify.api.utils.event.EventSink;
import dev.isxander.controlify.api.utils.event.EventStage;
import dev.isxander.sdl.*;
import org.jspecify.annotations.NonNull;

public class SDLControllerEventStage implements EventStage<SdlEvent, SDLControllerEvent> {
	@Override
	public void onEvent(@NonNull SdlEvent event, @NonNull EventSink<? super SDLControllerEvent> downstream) {
		long timeNanos = event.timestamp();

		switch (event.data()) {
			case SdlEvent.JoyAxis(SdlJoystickId which, byte axis, short value) ->
				downstream.accept(new SDLControllerEvent.JoyAxis(which, timeNanos, axis, value));

			case SdlEvent.JoyButton(SdlJoystickId which, byte button, boolean down) ->
				downstream.accept(new SDLControllerEvent.JoyButton(which, timeNanos, button, down));

			case SdlEvent.JoyDevice(SdlJoystickId which) -> {
				var type = SDLInputConversions.getDeviceEventType(event.type());
				if (type != null) {
					downstream.accept(new SDLControllerEvent.JoyDevice(which, timeNanos, type));
				}
			}

			case SdlEvent.JoyBattery(SdlJoystickId which, int state, int percent) -> {
				var stateEnum = SDLInputConversions.getPowerState(state);
				if (stateEnum != null) {
					downstream.accept(new SDLControllerEvent.Battery(which, timeNanos, stateEnum, percent));
				}
			}

			case SdlEvent.GamepadAxis(SdlJoystickId which, byte axis, short value) ->
				downstream.accept(new SDLControllerEvent.GamepadAxis(which, timeNanos, axis, value));

			case SdlEvent.GamepadButton(SdlJoystickId which, byte button, boolean down) ->
				downstream.accept(new SDLControllerEvent.GamepadButton(which, timeNanos, button, down));

			case SdlEvent.GamepadDevice(SdlJoystickId which) -> {
				var type = SDLInputConversions.getDeviceEventType(event.type());
				if (type != null) {
					downstream.accept(new SDLControllerEvent.GamepadDevice(which, timeNanos, type));
				}
			}

			case SdlEvent.GamepadTouchpad(SdlJoystickId which, int touchpad, int finger, float x, float y, float pressure) -> {
				var action = SDLInputConversions.getTouchpadAction(event.type());
				if (action != null) {
					downstream.accept(new SDLControllerEvent.GamepadTouchpad(which, timeNanos, action, touchpad, finger, x, y, pressure));
				}
			}

			case SdlEvent.GamepadSensor(SdlJoystickId which, int sensor, float[] values, long sensorTimestamp) -> {
				float x = values.length >= 1 ? values[0] : 0f;
				float y = values.length >= 2 ? values[1] : 0f;
				float z = values.length >= 3 ? values[2] : 0f;
				downstream.accept(new SDLControllerEvent.GamepadSensor(which, timeNanos, sensor, x, y, z, sensorTimestamp));
			}

			default -> {}
		}
	}


}
