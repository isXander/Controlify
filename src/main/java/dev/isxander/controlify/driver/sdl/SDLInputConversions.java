package dev.isxander.controlify.driver.sdl;

import dev.isxander.controlify.input.mapping.GamepadInputs;
import dev.isxander.controlify.input.SensorType;
import dev.isxander.sdl.SdlEvents;
import dev.isxander.sdl.SdlGamepad;
import net.minecraft.resources.Identifier;
import org.jspecify.annotations.Nullable;

import java.util.stream.IntStream;

import static dev.isxander.sdl.SdlGamepad.*;

public final class SDLInputConversions {
	private static final Identifier[] GAMEPAD_AXES = IntStream.range(0, SDL_GAMEPAD_AXIS_COUNT << 1)
			.mapToObj(i -> switch (i) {
				case (SDL_GAMEPAD_AXIS_LEFTX << 1) -> GamepadInputs.LEFT_STICK_AXIS_RIGHT;
				case (SDL_GAMEPAD_AXIS_LEFTX << 1) | 1 -> GamepadInputs.LEFT_STICK_AXIS_LEFT;
				case (SDL_GAMEPAD_AXIS_LEFTY << 1) -> GamepadInputs.LEFT_STICK_AXIS_DOWN;
				case (SDL_GAMEPAD_AXIS_LEFTY << 1) | 1 -> GamepadInputs.LEFT_STICK_AXIS_UP;
				case (SDL_GAMEPAD_AXIS_RIGHTX << 1) -> GamepadInputs.RIGHT_STICK_AXIS_RIGHT;
				case (SDL_GAMEPAD_AXIS_RIGHTX << 1) | 1 -> GamepadInputs.RIGHT_STICK_AXIS_LEFT;
				case (SDL_GAMEPAD_AXIS_RIGHTY << 1) -> GamepadInputs.RIGHT_STICK_AXIS_DOWN;
				case (SDL_GAMEPAD_AXIS_RIGHTY << 1) | 1 -> GamepadInputs.RIGHT_STICK_AXIS_UP;
				case (SDL_GAMEPAD_AXIS_LEFT_TRIGGER << 1),
					 (SDL_GAMEPAD_AXIS_LEFT_TRIGGER << 1) | 1 -> GamepadInputs.LEFT_TRIGGER_AXIS;
				case (SDL_GAMEPAD_AXIS_RIGHT_TRIGGER << 1),
					 (SDL_GAMEPAD_AXIS_RIGHT_TRIGGER << 1) | 1 -> GamepadInputs.RIGHT_TRIGGER_AXIS;
				default -> throw new IllegalArgumentException("Unknown axis");
			})
			.toArray(Identifier[]::new);
	private static final Identifier[] GAMEPAD_BUTTONS = IntStream.range(0, SDL_GAMEPAD_BUTTON_COUNT)
			.mapToObj(i -> switch (i) {
				case SDL_GAMEPAD_BUTTON_SOUTH -> GamepadInputs.SOUTH_BUTTON;
				case SDL_GAMEPAD_BUTTON_EAST -> GamepadInputs.EAST_BUTTON;
				case SDL_GAMEPAD_BUTTON_WEST -> GamepadInputs.WEST_BUTTON;
				case SDL_GAMEPAD_BUTTON_NORTH -> GamepadInputs.NORTH_BUTTON;
				case SDL_GAMEPAD_BUTTON_BACK -> GamepadInputs.BACK_BUTTON;
				case SDL_GAMEPAD_BUTTON_GUIDE -> GamepadInputs.GUIDE_BUTTON;
				case SDL_GAMEPAD_BUTTON_START -> GamepadInputs.START_BUTTON;
				case SDL_GAMEPAD_BUTTON_LEFT_STICK -> GamepadInputs.LEFT_STICK_BUTTON;
				case SDL_GAMEPAD_BUTTON_RIGHT_STICK -> GamepadInputs.RIGHT_STICK_BUTTON;
				case SDL_GAMEPAD_BUTTON_LEFT_SHOULDER -> GamepadInputs.LEFT_SHOULDER_BUTTON;
				case SDL_GAMEPAD_BUTTON_RIGHT_SHOULDER -> GamepadInputs.RIGHT_SHOULDER_BUTTON;
				case SDL_GAMEPAD_BUTTON_DPAD_UP -> GamepadInputs.DPAD_UP_BUTTON;
				case SDL_GAMEPAD_BUTTON_DPAD_DOWN -> GamepadInputs.DPAD_DOWN_BUTTON;
				case SDL_GAMEPAD_BUTTON_DPAD_LEFT -> GamepadInputs.DPAD_LEFT_BUTTON;
				case SDL_GAMEPAD_BUTTON_DPAD_RIGHT -> GamepadInputs.DPAD_RIGHT_BUTTON;
				case SDL_GAMEPAD_BUTTON_MISC1 -> GamepadInputs.MISC_1_BUTTON;
				case SDL_GAMEPAD_BUTTON_RIGHT_PADDLE1 -> GamepadInputs.RIGHT_PADDLE_1_BUTTON;
				case SDL_GAMEPAD_BUTTON_LEFT_PADDLE1 -> GamepadInputs.LEFT_PADDLE_1_BUTTON;
				case SDL_GAMEPAD_BUTTON_RIGHT_PADDLE2 -> GamepadInputs.RIGHT_PADDLE_2_BUTTON;
				case SDL_GAMEPAD_BUTTON_LEFT_PADDLE2 -> GamepadInputs.LEFT_PADDLE_2_BUTTON;
				case SDL_GAMEPAD_BUTTON_TOUCHPAD -> GamepadInputs.TOUCHPAD_1_BUTTON;
				case SDL_GAMEPAD_BUTTON_MISC2 -> GamepadInputs.MISC_2_BUTTON;
				case SDL_GAMEPAD_BUTTON_MISC3 -> GamepadInputs.MISC_3_BUTTON;
				case SDL_GAMEPAD_BUTTON_MISC4 -> GamepadInputs.MISC_4_BUTTON;
				case SDL_GAMEPAD_BUTTON_MISC5 -> GamepadInputs.MISC_5_BUTTON;
				case SDL_GAMEPAD_BUTTON_MISC6 -> GamepadInputs.MISC_6_BUTTON;
				default -> throw new IllegalArgumentException("Unknown button");

			})
			.toArray(Identifier[]::new);

	private static final SensorType[] GAMEPAD_SENSOR_TYPES = IntStream.range(0, SDL_SENSOR_COUNT)
			.mapToObj(i -> switch (i) {
				case SDL_SENSOR_GYRO -> SensorType.GYROSCOPE;
				case SDL_SENSOR_GYRO_L ->  SensorType.GYROSCOPE_L;
				case SDL_SENSOR_GYRO_R ->  SensorType.GYROSCOPE_R;
				case SDL_SENSOR_ACCEL -> SensorType.ACCELEROMETER;
				case SDL_SENSOR_ACCEL_L -> SensorType.ACCELEROMETER_L;
				case SDL_SENSOR_ACCEL_R -> SensorType.ACCELEROMETER_R;
				default -> null;
			})
			.toArray(SensorType[]::new);

	public static @Nullable Identifier getGamepadButton(int button) {
		if (button < 0 || button >= GAMEPAD_BUTTONS.length) {
			return null;
		}
		return GAMEPAD_BUTTONS[button];
	}

	public static @Nullable Identifier getGamepadAxis(int axis, boolean positive) {
		int index = (axis << 1) | (positive ? 0 : 1);
		if (index < 0 || index >= GAMEPAD_AXES.length) {
			return null;
		}
		return GAMEPAD_AXES[index];
	}

	public static @Nullable SensorType getSensorType(int sensor) {
		if (sensor < 0 || sensor >= GAMEPAD_SENSOR_TYPES.length) {
			return null;
		}
		return GAMEPAD_SENSOR_TYPES[sensor];
	}

	public static SDLControllerEvent.DeviceEvent.@Nullable Type getDeviceEventType(int event) {
		return switch (event) {
			case SdlEvents.SDL_EVENT_JOYSTICK_ADDED -> SDLControllerEvent.DeviceEvent.Type.ADDED;
			case SdlEvents.SDL_EVENT_JOYSTICK_REMOVED -> SDLControllerEvent.DeviceEvent.Type.REMOVED;
			case SdlEvents.SDL_EVENT_JOYSTICK_UPDATE_COMPLETE -> SDLControllerEvent.DeviceEvent.Type.UPDATE_COMPLETE;
			case SdlEvents.SDL_EVENT_GAMEPAD_REMAPPED -> SDLControllerEvent.DeviceEvent.Type.REMAPPED;
			case SdlEvents.SDL_EVENT_GAMEPAD_STEAM_HANDLE_UPDATED -> SDLControllerEvent.DeviceEvent.Type.STEAM_HANDLE_UPDATED;
			default -> null;
		};
	}

	public static SDLControllerEvent.Battery.@Nullable PowerState getPowerState(int powerState) {
		return switch (powerState) {
			case SdlGamepad.SDL_POWERSTATE_ERROR -> SDLControllerEvent.Battery.PowerState.ERROR;
			case SdlGamepad.SDL_POWERSTATE_UNKNOWN -> SDLControllerEvent.Battery.PowerState.UNKNOWN;
			case SdlGamepad.SDL_POWERSTATE_ON_BATTERY -> SDLControllerEvent.Battery.PowerState.ON_BATTERY;
			case SdlGamepad.SDL_POWERSTATE_NO_BATTERY -> SDLControllerEvent.Battery.PowerState.NO_BATTERY;
			case SdlGamepad.SDL_POWERSTATE_CHARGING -> SDLControllerEvent.Battery.PowerState.CHARGING;
			case SdlGamepad.SDL_POWERSTATE_CHARGED -> SDLControllerEvent.Battery.PowerState.CHARGED;
			default -> null;
		};
	}

	public static SDLControllerEvent.GamepadTouchpad.@Nullable Action getTouchpadAction(int event) {
		return switch (event) {
			case SdlEvents.SDL_EVENT_GAMEPAD_TOUCHPAD_DOWN -> SDLControllerEvent.GamepadTouchpad.Action.DOWN;
			case SdlEvents.SDL_EVENT_GAMEPAD_TOUCHPAD_MOTION -> SDLControllerEvent.GamepadTouchpad.Action.MOTION;
			case SdlEvents.SDL_EVENT_GAMEPAD_TOUCHPAD_UP -> SDLControllerEvent.GamepadTouchpad.Action.UP;
			default -> null;
		};
	}

	private SDLInputConversions() {
	}
}
